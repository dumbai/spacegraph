package com.bulletphysics.render;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.RayResultCallback;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.input.InputKey;
import com.bulletphysics.input.Keyboard;
import com.bulletphysics.input.Mouse;
import com.bulletphysics.input.MouseButton;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import spacegraph.util.animate.AnimFloat;
import spacegraph.util.animate.v3Anim;
import toxi.math.MathUtils;

import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.awt.event.MouseEvent;
import java.util.List;

import static com.bulletphysics.linearmath.QuaternionUtil.setRotation;
import static com.bulletphysics.linearmath.VectorUtil.coord;
import static com.bulletphysics.render.IGL.GL_MODELVIEW;
import static com.bulletphysics.render.IGL.GL_PROJECTION;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES3.GL_STENCIL;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT_AND_DIFFUSE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_NORMALIZE;

/**
 * see: https://github.com/automenta/spacegraphc2/tree/master/bullet-gl
 * see: https://github.com/automenta/spacegraphc2/blob/master/src/Spacegraph.cpp
 */
public abstract class SpaceGraph3D implements GLEventListener {
    private static final float STEPSIZE = 5;
    private static final float mousePickClamping = 3.0f;
    private static RigidBody pickedBody; // for deactivation state


    // keep the collision shapes, for deletion/cleanup
//    protected final ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<>();
    protected final float fps = 30.0f;
    final Mouse mouse = new Mouse();
    final Keyboard keyboard = new Keyboard();
    private final Clock clock = new Clock();
    public DynamicsWorld world;
    @Deprecated public transient IGL gl;
    protected boolean idle = false;
    int debugMode = 0;
    boolean stepping = true;
    private TypedConstraint pickConstraint;


    public abstract static class Camera3D {
        public final Vector3f position = new Vector3f();
        public final Vector3f target = new Vector3f(); // look at
        public final Vector3f up = new Vector3f(0, 1, 0);

        public final void update(long dtNS, SpaceGraph3D s) {
            update(dtNS);
            s.updateCamera(position, target, up);
        }

        public abstract void update(long dtNS);

        @Deprecated
        public abstract void setDistance(float dist);
    }


    public static class FirstPersonCamera extends Camera3D {
        public final v3Anim camPos, camTgt, camUp;

        public FirstPersonCamera() {
            float cameraSpeed = 100;
            float cameraRotateSpeed = cameraSpeed;
            camPos = new v3Anim(0, 0, 5, cameraSpeed);
            camTgt = new v3Anim(0, 0, -1, cameraRotateSpeed);
            camUp =  new v3Anim(0, 1, 0, cameraRotateSpeed);
        }

        @Override
        public void update(long dtNS) {
//            camPos.set(5+Math.sin(System.currentTimeMillis()/1000.0)*10, 0, 40);
            final int dtMS = (int) (dtNS / 1E6 /* ns -> ms */);
            camPos.animate(dtMS);
            camTgt.animate(dtMS);
            camUp.animate(dtMS);
            position.set(camPos.x, camPos.y, camPos.z);
            target.set(camTgt.x, camTgt.y, camTgt.z);
            up.set(camUp.x, camUp.y, camUp.z);
        }

        @Override
        public void setDistance(float dist) {

        }
    }

    public static class PolarCamera extends Camera3D {
        protected int forwardAxis = 2;

        final AnimFloat camEle = new AnimFloat(20, 0.95f),
                camAzi = new AnimFloat(0, 0.95f);
        private float camDist = 15;

        @Override
        public void update(long dtNS) {

            int dtMS = (int) (dtNS / 1E6);
            camEle.animate(dtMS);
            camAzi.animate(dtMS);
            float rele = MathUtils.radians(camEle.floatValue());
            float razi = (float) (Math.PI + /* HACK */ MathUtils.radians(camAzi.floatValue()));

            Quat4f rot = new Quat4f();
            setRotation(rot, up, razi);

            position.set(0, 0, 0);
            coord(position, forwardAxis, -camDist);

            Vector3f forward = new Vector3f(position.x, position.y, position.z);
            if (forward.lengthSquared() < BulletGlobals.FLT_EPSILON)
                forward.set(1,0,0);

            Vector3f right = new Vector3f();
            right.cross(up, forward);

            Quat4f roll = new Quat4f();
            setRotation(roll, right, -rele);

            Matrix3f tmpMat1 = new Matrix3f();
            tmpMat1.set(rot);
            Matrix3f tmpMat2 = new Matrix3f();
            tmpMat2.set(roll);
            tmpMat1.mul(tmpMat2);
            tmpMat1.transform(position);

        }


        @Override
        public void setDistance(float dist) {
            camDist = dist;
        }
    }

    private Camera3D camera =
        new PolarCamera();
        //new FirstPersonCamera();

    private int glutScreenWidth = 0, glutScreenHeight = 0;

    protected float zNear = 1, zFar = 10000;

    public SpaceGraph3D() {
    }

    public static void stop() {
        throw new RuntimeException("TODO");
    }

    public void start(final JoglWindow3D.JoglGL j) {
        synchronized (this) {
            gl = j;
            this.world = physics();
            reset();
            world.setDebugDrawer(new GLDebugDrawer(j));
        }
    }

    protected void reset() {
        BulletStats.gNumDeepPenetrationChecks = 0;
        BulletStats.gNumGjkChecks = 0;

        world.next(1 / fps, 0);
        int numObjects = world.getNumCollisionObjects();

        var aa = world.getCollisionObjectArray();
        for (int i = 0; i < numObjects; i++) {
            RigidBody b = RigidBody.upcast(aa.get(i));
            if (b == null) continue;

            if (b.getMotionState() != null) {
                DefaultMotionState myMotionState = (DefaultMotionState) b.getMotionState();
                myMotionState.graphicsWorldTrans.set(myMotionState.startWorldTrans);
                b.setWorldTransform(myMotionState.graphicsWorldTrans);
                b.setInterpolationWorldTransform(myMotionState.startWorldTrans);
                b.activate();
            }
            // removed cached contact points
            world.broadphase().getOverlappingPairCache().cleanProxyFromPairs(b.getBroadphaseHandle(), world().dispatcher());

            if (!b.isStaticObject()) {
                b.setLinearVelocity(new Vector3f());
                b.setAngularVelocity(new Vector3f());
            }

        }
    }

    protected abstract DynamicsWorld physics();

    @Override
    public void init(GLAutoDrawable G) {

        if (((JoglWindow3D.JoglGL) gl).gl == null)
            ((JoglWindow3D.JoglGL) gl).init(G.getGL().getGL2()); //HACK

        float[] light_ambient = {0.2f, 0.2f, 0.2f, 1.0f};
        float[] light_diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] light_specular = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] light_position0 = {1.0f, 10.0f, 1.0f, 0.0f};
        float[] light_position1 = {-1.0f, -10.0f, -1.0f, 0.0f};

        gl.glLight(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light_ambient);
        gl.glLight(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light_diffuse);
        gl.glLight(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light_specular);
        gl.glLight(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position0);

        gl.glLight(GL2.GL_LIGHT1, GL2.GL_AMBIENT, light_ambient);
        gl.glLight(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, light_diffuse);
        gl.glLight(GL2.GL_LIGHT1, GL2.GL_SPECULAR, light_specular);
        gl.glLight(GL2.GL_LIGHT1, GL2.GL_POSITION, light_position1);

        GL2 g = G.getGL().getGL2();
        g.glEnable(GL2.GL_LIGHTING);
        g.glEnable(GL2.GL_LIGHT0);
//        g.glEnable(GL2.GL_LIGHT1);

        g.glEnable(GL_STENCIL);

        //Set blending
        g.glEnable(GL_BLEND);
        g.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        /* Set antialiasing/multisampling */
        g.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        g.glEnable(GL_LINE_SMOOTH);
        g.glEnable(GL_MULTISAMPLE);

        //BAD:
//        gl.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
        //gl.glEnable(GL_POLYGON_SMOOTH);

        g.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        g.glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
        g.glEnable(GL2.GL_COLOR_MATERIAL);
        g.glEnable(GL_NORMALIZE);

        g.glShadeModel(GL2.GL_SMOOTH);

        g.glEnable(GL2.GL_DEPTH_TEST);
        g.glDepthFunc(GL2.GL_LESS);
        //gl.glDepthFunc(GL_LEQUAL);

        g.glClearColor(0,0,0,0);

        //gl.glEnable(GL_CULL_FACE);
        //gl.glCullFace(GL_BACK);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        glutScreenWidth = width;
        glutScreenHeight = height;

        gl.glViewport(x, y, width, height);
    }

    private void updatePhysics(long dtNS) {

        var world = this.world;
        if (isIdle() || world == null)
            return;

        // simple dynamics world doesn't handle fixed-time-stepping
        float uSec = dtNS / 1000f;

        float minFPS = 1000000.0f / fps;
        if (uSec > minFPS) uSec = minFPS;

        world.next(uSec / 1000000.0f);

    }

    @Override
    public void display(GLAutoDrawable drawable) {

        long dtNS = clock.getTimeNanosecondsReset();

        update(dtNS);

        render(drawable);
    }

    private void update(long dtNS) {
        updateMouse(dtNS);
        updateKeyboard(dtNS);

        updatePhysics(dtNS);

        camera.update(dtNS, this);
    }

    private void render(GLAutoDrawable drawable) {
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        {
            gl.glEnable(GL2.GL_LIGHTING);
            renderVolume(drawable);
            gl.glDisable(GL2.GL_LIGHTING);
        }

        {
            setOrthographicProjection();
            renderHUD();
            resetPerspectiveProjection();
        }
    }

    void renderHUD() {

    }

    protected void renderVolume(GLAutoDrawable drawable) {
        // optional but useful: debug drawing
        world.debugDrawWorld();

        int numObjects = world.getNumCollisionObjects();

        List<CollisionObject> xx = world.getCollisionObjectArray();

        final Transform t = new Transform();
        final Vector3f color = new Vector3f();

        for (int i = 0; i < numObjects; i++) {

            CollisionObject x = xx.get(i);

            transform(x, t);
            color(x, color);

            GLShapeDrawer.draw(x, null, t, color, debugMode, gl);
        }

    }

    /**
     * load an object's world transform
     */
    private void transform(CollisionObject x, Transform t) {
        RigidBody body = RigidBody.upcast(x);
        if (body != null && body.getMotionState() != null) {
            t.set(((DefaultMotionState) body.getMotionState()).graphicsWorldTrans);
        } else {
            x.getWorldTransform(t);
        }
    }

    /** color function */
    private void color(CollisionObject x, Vector3f color) {
        if (x instanceof RigidBody B)
            color.set(B.color);
        else
            color.set(0.5f, 0.5f, 0.5f); //HACK
    }

    private void colorDebug(CollisionObject x, Vector3f color) {
        int i = (x instanceof RigidBody B) ? B.ID : System.identityHashCode(x);
        if ((i & 1) != 0)
            color.set(0, 0, 1);
        else
            color.set(1, 1, 0.5f); // wants deactivation

        // color differently for active, sleeping, wantsdeactivation states
        int state = x.getActivationState();
        if (state == 1) { //active
            color.x += (i & 1) != 0 ? 1 : 0.5f;
        } else if (state == 2) { // ISLAND_SLEEPING
            color.y += (i & 1) != 0 ? 1 : 0.5f;
        }

        color.clamp(0, 1);
    }

    public void setCameraDistance(float dist) {
        camera.setDistance(dist);
    }

    void toggleIdle() {
        idle = !idle;
    }


    public void updateCamera(Vector3f pos, Vector3f target, Vector3f up) {
//        this.cameraPosition.set(pos);
//        this.cameraTargetPosition.set(target);
//        this.cameraUp.set(up);

        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        if (glutScreenWidth > glutScreenHeight) {
            float aspect = glutScreenWidth / (float) glutScreenHeight;
            gl.glFrustum(-aspect, aspect, -1.0, 1.0, zNear, zFar);
        } else {
            float aspect = glutScreenHeight / (float) glutScreenWidth;
            gl.glFrustum(-1.0, 1.0, -aspect, aspect, zNear, zFar);
        }

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.gluLookAt(pos.x, pos.y, pos.z,
                target.x, target.y, target.z,
                up.x, up.y, up.z);
    }

    void stepLeft() {
//        camAzi -= STEPSIZE;
//        if (camAzi < 0) {
//            camAzi += 360;
//        }
//        //updateCamera();
    }

    void stepRight() {
//        camAzi += STEPSIZE;
//        if (camAzi >= 360) {
//            camAzi -= 360;
//        }
//        //updateCamera();
    }

    void stepFront() {
//        camEle += STEPSIZE;
//        if (camEle >= 360) {
//            camEle -= 360;
//        }
//        //updateCamera();
    }

    void stepBack() {
//        camEle -= STEPSIZE;
//        if (camEle < 0) {
//            camEle += 360;
//        }
//        //updateCamera();
    }

    void zoomIn() {
//        camDist -= 0.4f;
//        //updateCamera();
//        if (camDist < 0.1f) {
//            camDist = 0.1f;
//        }
    }

    void zoomOut() {
//        camDist += 0.4f;
//        //updateCamera();
    }

    private void updateKeyboard(long dtNS) {
        for (InputKey key : keyboard.getActiveKeys().values()) {
            if (key.isConsumed()) continue;
            if (key.isPressed()) {
                if (key.isActionKey()) {
                    specialKeyboard(key.getKeyCode());
                } else {
                    keyboardCallback(key.getKeyChar());
                }
            } else {
                if (key.isActionKey()) {
                    specialKeyboardUp(key.getKeyCode());
                } else {
                    keyboardCallbackUp(key.getKeyChar());
                }
            }
        }

        keyboard.reset();
    }

    private int x, y;

    private void updateMouse(long dtNS) {
        int px = this.x, py = this.y;
        int x = mouse.getLocation().x, y = mouse.getLocation().y;
        this.x = x; this.y = y;

        for (MouseButton btn : mouse.getActiveButtons().values()) {
            int event = btn.getAWTEventId();
            if (event == 0) continue;
            if (event == MouseEvent.MOUSE_PRESSED) {
                mouseFunc(btn.getCode() - 1, 1, x, y);
            } else if (event == MouseEvent.MOUSE_RELEASED) {
                mouseFunc(btn.getCode() - 1, 0, x, y);
            } else if (event == MouseEvent.MOUSE_DRAGGED) {
                int dx = x - px, dy = y - py;
                mouseMotionFunc(btn.getCode(), x, y, dx, dy);
            }
        }

        mouse.clear();
    }

    protected void keyboardCallbackUp(char key) {

    }

    protected void keyboardCallback(char key) {

    }

    protected int getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(int mode) {
        debugMode = mode;
        if (world() != null && world().getDebugDrawer() != null) {
            world().getDebugDrawer().setDebugMode(mode);
        }
    }

    protected void specialKeyboardUp(int key) {

    }

    protected void specialKeyboard(int key) {

    }

    private Vector3f rayTo(int x, int y) {
        float top = 1.0f;
        float bottom = -1.0f;
        float nearPlane = 1.0f;
        float tanFov = (top - bottom) * 0.5f / nearPlane;
        float fov = 2.0f * (float) Math.atan(tanFov);

        Vector3f rayFrom = new Vector3f(cameraPosition());
        Vector3f rayForward = new Vector3f();
        rayForward.sub(cameraTarget(), cameraPosition());
        rayForward.normalize();
        rayForward.scale(zFar);

//        Vector3f rightOffset = new Vector3f();
        Vector3f vertical = new Vector3f(camera.up);

        Vector3f hor = new Vector3f();
        // TODO: check: hor = rayForward.cross(vertical);
        hor.cross(rayForward, vertical);
        hor.normalize();
        // TODO: check: vertical = hor.cross(rayForward);
        vertical.cross(hor, rayForward);
        vertical.normalize();

        double tanfov = (float) Math.tan(0.5 * fov);

        double aspect = glutScreenHeight / (float) glutScreenWidth;

        float s = (float) (2 * zFar * tanfov);
        hor.scale(s);
        vertical.scale(s);

        if (aspect < 1) {
            hor.scale((float) (1 / aspect));
        } else {
            vertical.scale((float) aspect);
        }

        Vector3f rayToCenter = new Vector3f();
        rayToCenter.add(rayFrom, rayForward);
        Vector3f dHor = new Vector3f(hor);
        dHor.scale(1.0f / glutScreenWidth);
        Vector3f dVert = new Vector3f(vertical);
        dVert.scale(1.0f / glutScreenHeight);

        Vector3f tmp1 = new Vector3f();
        tmp1.scale(0.5f, hor);
        Vector3f tmp2 = new Vector3f();
        tmp2.scale(0.5f, vertical);

        Vector3f rayTo = new Vector3f();
        rayTo.sub(rayToCenter, tmp1);
        rayTo.add(tmp2);

        tmp1.scale(x, dHor);
        tmp2.scale(y, dVert);

        rayTo.add(tmp1);
        rayTo.sub(tmp2);
        return rayTo;
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        // TODO Auto-generated method stub

    }

    private void mouseFunc(int button, int state, int x, int y) {
        //printf("button %i, state %i, x=%i,y=%i\n",button,state,x,y);
        //button 0, state 0 means left mouse down

        Vector3f rayTo = new Vector3f(rayTo(x, y));


        switch (button) {
            case 2 -> {
//                if (state == 0) {
//                    shootBox(rayTo);
//                }
//                break;
            }
            case 1 -> {
                if (state == 0) {
                    // apply an impulse
                    if (world != null) {
                        CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(cameraPosition(), rayTo);
                        world.rayTest(cameraPosition(), rayTo, rayCallback, RayResultCallback.RAY_EPSILON);
                        if (rayCallback.hasHit()) {
                            RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
                            if (body != null) {
                                body.setActivationState(CollisionObject.ACTIVE_TAG);
                                Vector3f impulse = new Vector3f(rayTo);
                                impulse.normalize();
                                float impulseStrength = 10.0f;
                                impulse.scale(impulseStrength);
                                Vector3f relPos = new Vector3f();
                                relPos.sub(rayCallback.hitPointWorld, body.getCenterOfMassPosition(new Vector3f()));
                                body.applyImpulse(impulse, relPos);
                            }
                        }
                    }
                } else {
                }
            }
            case 0 -> {
                if (state == 1) {
                    // add a point to point constraint for picking
                    if (world != null) {
                        CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(cameraPosition(), rayTo);
                        world.rayTest(cameraPosition(), rayTo, rayCallback, RayResultCallback.RAY_EPSILON);
                        if (rayCallback.hasHit()) {
                            RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
                            if (body != null) {
                                // other exclusions?
                                if (!(body.isStaticObject() || body.isKinematicObject())) {
                                    pickedBody = body;
                                    pickedBody.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

                                    Vector3f pickPos = new Vector3f(rayCallback.hitPointWorld);

                                    Transform tmpTrans = body.getCenterOfMassTransform(new Transform());
                                    tmpTrans.inverse();
                                    Vector3f localPivot = new Vector3f(pickPos);
                                    tmpTrans.transform(localPivot);

                                    Point2PointConstraint p2p = new Point2PointConstraint(body, localPivot);
                                    p2p.setting.impulseClamp = mousePickClamping;

                                    world.addConstraint(p2p);
                                    pickConstraint = p2p;
                                    // save mouse position for dragging
                                    BulletStats.gOldPickingPos.set(rayTo);
                                    Vector3f eyePos = new Vector3f(cameraPosition());
                                    Vector3f tmp = new Vector3f();
                                    tmp.sub(pickPos, eyePos);
                                    BulletStats.gOldPickingDist = tmp.length();
                                    // very weak constraint for picking
                                    p2p.setting.tau = 0.1f;
                                }
                            }
                        }
                    }

                } else {

                    if (pickConstraint != null && world != null) {
                        world.removeConstraint(pickConstraint);
                        // delete m_pickConstraint;
                        //printf("removed constraint %i",gPickingConstraintId);
                        pickConstraint = null;
                        pickedBody.forceActivationState(CollisionObject.ACTIVE_TAG);
                        pickedBody.setDeactivationTime(0.0f);
                        pickedBody = null;
                    }
                }
            }
            default -> {
            }
        }
    }

    private void mouseMotionFunc(int button, int x, int y, int dx, int dy) {
        if (pickConstraint != null) {
            mouseMotionFuncPicked(x, y);
        } else {
            if (button == 3) {
                final float rotSpeed = 1/10f, eleSpeed = rotSpeed;
                ((PolarCamera)camera).camAzi.add(dx * rotSpeed);
                ((PolarCamera)camera).camEle.add(dy * eleSpeed);
            }
        }
    }

    private void mouseMotionFuncPicked(int x, int y) {
        // move the constraint pivot
        Point2PointConstraint p2p = (Point2PointConstraint) pickConstraint;
        // keep it at the same picking distance

        Vector3f newRayTo = new Vector3f(rayTo(x, y));
        Vector3f eyePos = new Vector3f(cameraPosition());
        Vector3f dir = new Vector3f();
        dir.sub(newRayTo, eyePos);
        dir.normalize();
        dir.scale(BulletStats.gOldPickingDist);

        Vector3f newPos = new Vector3f();
        newPos.add(eyePos, dir);
        p2p.setPivotB(newPos);
    }


    protected DynamicsWorld world() {
        return world;
    }


    protected Vector3f cameraPosition() {
        return camera.position;
    }

    protected Vector3f cameraTarget() {
        return camera.target;
    }

    protected float getDeltaTimeMicroseconds() {
        //#ifdef USE_BT_CLOCK
        float dt = clock.getTimeMicroseconds();
        clock.reset();
        return dt;
        //#else
        //return btScalar(16666.);
        //#endif
    }

    private boolean isIdle() {
        return idle;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    protected void drawString(CharSequence s, int x, int y, Color3f color) {
        gl.drawString(s, x, y, color.x, color.y, color.z);
    }

    // See http://www.lighthouse3d.com/opengl/glut/index.php?bmpfontortho
    protected void setOrthographicProjection() {
        // switch to projection mode
        gl.glMatrixMode(GL_PROJECTION);


        // save previous matrix which contains the
        //settings for the perspective projection
        gl.glPushMatrix();
        // reset matrix
        gl.glLoadIdentity();
        // set a 2D orthographic projection
        gl.gluOrtho2D(0.0f, glutScreenWidth, 0.0f, glutScreenHeight);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        // invert the y axis, down is positive
        gl.glScalef(1.0f, -1.0f, 1.0f);
        // mover the origin from the bottom left corner
        // to the upper left corner
        gl.glTranslatef(0.0f, -glutScreenHeight, 0.0f);
        gl.glPopMatrix();

    }


    protected void resetPerspectiveProjection() {
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL_MODELVIEW);
    }
}