package spacegraph.layer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import jcog.data.list.FastCoWList;
import jcog.event.Off;
import jcog.math.v3;
import jcog.tree.rtree.rect.RectF;
import spacegraph.SpaceGraph;
import spacegraph.input.finger.Finger;
import spacegraph.input.key.KeyXYZ;
import spacegraph.input.key.WindowKeyControls;
import spacegraph.util.animate.Animated;
import spacegraph.util.animate.v3Anim;
import spacegraph.video.Draw;
import spacegraph.video.JoglWindow;

import java.util.function.Consumer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.GL2ES3.GL_STENCIL;
import static com.jogamp.opengl.GL2GL3.GL_POLYGON_SMOOTH;
import static com.jogamp.opengl.GL2GL3.GL_POLYGON_SMOOTH_HINT;
import static com.jogamp.opengl.GLES2.GL_MAX;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.*;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static jcog.math.v3.v;

/** JOGL display implementation */
public abstract class AbstractLayer extends SpaceGraph implements Layer {

    /** field of view, in degrees */
    public float fov = 45;

    /**
     * the hardware input/output implementation
     */
    public JoglWindow window;

    public final v3Anim camPos, camFwd, camUp;

    public float zNear = 0.5f;
    public float zFar = 1000;

    protected int debug;
    public double top;
    public double bottom;
    public double left;
    public double right;

    public final FastCoWList<Finger> fingers = new FastCoWList<>(Finger.class);

    public AbstractLayer() {
        float cameraSpeed = 100.0f;
        float cameraRotateSpeed = cameraSpeed;
        camPos = new v3Anim(0, 0, 5, cameraSpeed);
        camFwd = new v3Anim(0, 0, -1, cameraRotateSpeed);
        camUp = new v3Anim(0, 1, 0, cameraRotateSpeed);
    }

    public void addFinger(Finger f) {
        add(f, f);
        fingers.add(f);
    }

    /** TODO reverse callee's control flow */
    @Deprecated public void setWindow(JoglWindow w) {
        w.runLater(()-> {
            synchronized (this) {
                if (this.window != null) {
                    if (this.window == w) return;
                    this.window.remove(this);
                }

                this.window = w;

                w.add(this);
            }
        });
    }

    @Override public void visible(boolean visible) {

    }

    public static void initDepth(GL2 gl) {
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LEQUAL);

        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepth(1); //TODO may not be necessary
    }

    public static void initBlend(GL gl) {
        gl.glEnable(GL_BLEND);
        gl.glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);
        gl.glBlendEquationSeparate(GL_FUNC_ADD, GL_MAX);
    }

    protected void initLighting(GL2 gl) {

    }

    protected void initInput() {
        window.onUpdate(camPos);
        window.onUpdate(camFwd);
        window.onUpdate(camUp);
        window.addKeyListener(new WindowKeyControls(AbstractLayer.this));

        window.addKeyListener(new KeyXYZ(AbstractLayer.this));

    }

    @Deprecated public void camera(v3 target, float radius) {
        v3 fwd = v();
        fwd.subbed(target, camPos);
        fwd.normalize();
        camFwd.set(fwd);

        fwd.scaled(radius * 1.25f + zNear * 1.25f);
        camPos.subbed(target, fwd);

    }

    protected void renderVolume(float dtS, GL2 gl, float aspect) {
    }

    protected void renderOrthos(long startNS, float dtS) {

    }



//    public void updateCamera(GL2 gl) {
//        int glutScreenWidth = video.getWidth(), glutScreenHeight = video.getHeight();
//
//        updateCamera(gl, ((float) glutScreenWidth)/glutScreenHeight);
//    }

    public void updateCamera(GL2 gl, float aspect) {
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();



        top = zNear * Math.tan(fov * Math.PI / 360.0);
        bottom = -top;

        left = bottom * aspect;
        right = top * aspect;

//            xmin += -(2 * gl_state.camera_separation) / zNear;
//            xmax += -(2 * gl_state.camera_separation) / zNear;

        gl.glFrustum(left, right, bottom, top, zNear, zFar);

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        Draw.glu.gluLookAt(
            camPos.x, camPos.y, camPos.z,
            camPos.x + camFwd.x, camPos.y + camFwd.y, camPos.z + camFwd.z,
            camUp.x, camUp.y, camUp.z);

    }
    @Override
    public void init(GL2 gl) {



        initInput();
    }


    public final Off onUpdate(Consumer<JoglWindow> c) {
        return window.onUpdate(c);
    }

    public final Off onUpdate(Animated c) {
        return window.onUpdate(c);
    }

    public final Off onUpdate(Runnable c) {
        return window.onUpdate(c);
    }

    public void delete() {
        window.off();
    }

    public void renderVolumeEmbedded(RectF bounds, float dtS, GL2 gl) {
        window.dtS = dtS;
        window.onUpdate.emit(window); //HACK
        renderVolume(dtS, gl, bounds.w/bounds.h);
    }

    @Override
    public void render(long startNS, float dtS, GL2 g) {

        renderVolume(dtS, g, ((float) window.W())/ window.H());

        //TODO if (ortho()) {
        preOrtho(g);
        renderOrthos(startNS, dtS);
    }

    private void preOrtho(GL2 g) {
        g.glEnable(GL_STENCIL);

        g.glEnable(GL_LINE_SMOOTH);
        g.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

//        g.glEnable(GL_POLYGON_SMOOTH);
//        g.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

        g.glEnable(GL_MULTISAMPLE);

        g.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        g.glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);
        g.glEnable(GL_COLOR_MATERIAL);
        g.glEnable(GL_NORMALIZE);

        initDepth(g);
        initBlend(g);
        initLighting(g);
    }


    @Override public void close() {

    }


}