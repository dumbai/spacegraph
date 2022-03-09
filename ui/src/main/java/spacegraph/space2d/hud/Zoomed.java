package spacegraph.space2d.hud;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.math.v2;
import jcog.math.v3;
import jcog.pri.Prioritized;
import jcog.sort.RankedN;
import jcog.tree.rtree.rect.RectF;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.Fingering;
import spacegraph.input.finger.state.Dragging;
import spacegraph.input.finger.state.FingerMoveWindow;
import spacegraph.input.key.KeyPressed;
import spacegraph.layer.AbstractLayer;
import spacegraph.layer.OrthoSurfaceGraph;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.Surfacelike;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.util.SurfaceTransform;
import spacegraph.util.animate.Animated;
import spacegraph.util.animate.v3Anim;
import spacegraph.video.JoglWindow;

import java.util.ArrayDeque;
import java.util.Deque;

import static java.lang.Math.sin;
import static jcog.Util.fma;

/**
 * manages a moveable and zoomable view camera for interaction and display of a target virtual surface.
 * <p>
 * orthographic widget adapter. something which goes on the "face" of a HUD ("head"s-up-display)
 */
public class Zoomed<S extends Surface> extends MutableUnitContainer<S> implements /*Deprecated*/ KeyPressed {

    public static final short PAN_BUTTON = 0;

    /**
     * middle mouse button (wheel when pressed, apart from its roll which are detected in the wheel/wheelabsorber)
     */
    public static final short ZOOM_BUTTON = 2;

    private static final int ZOOM_STACK_MAX = 8;
    private static final float focusAngle = (float) Math.toRadians(45);
    /**
     * current view area, in absolute world coords
     */
    public final v2 scale = new v2(1, 1);
    public Camera cam;
    /**
     * parent
     */
    private AbstractLayer space;


    private final Deque<Surface> zoomStack = new ArrayDeque();

    private static final float wheelZoomRate = 0.6f;

    private final Fingering zoomDrag = new Dragging(ZOOM_BUTTON) {

        final v2 start = new v2();
        static final float maxIterationChange = 0.25f;
        static final float rate = 0.4f;

        @Override
        protected boolean starting(Finger f) {
            start.set(f.posPixel);
            return super.starting(f);
        }

        @Override
        protected boolean drag(Finger f) {

            v2 current = f.posPixel;

            float dy = current.y - start.y;
            float dx = current.x - start.x;
            float d = (float) Math.sqrt(dy*dy + dx*dx);

            zoomDelta(f.posGlobal(), Util.clamp((float) Math.pow(d * rate, 1), -maxIterationChange, +maxIterationChange));

            start.set(current); //incremental

            return true;
        }
    };
    private final Fingering contentPan = new FingerMoveWindow(PAN_BUTTON) {

        static final float speed = 1.0f;
        private v3 camStart;

        @Override
        protected AbstractLayer window() {
            return space;
        }

        @Override
        protected boolean starting(Finger f) {
            if (f.fingering() == Fingering.Idle) {
                camStart = new v3(cam);
                return super.starting(f);
            } else {
                return false;
            }
        }

        @Override
        public void move(float dx, float dy) {
            cam.set(camStart.x - dx / scale.x * speed,
                    camStart.y - dy / scale.y * speed);
        }

    };

//    float CORNER_RADIUS = 4;
    private float camXmin = -1;
    private float camXmax = +1;
    private float camYmin = -1;
    private float camYmax = +1;

    public Zoomed(S content) {
        super(content);

    }


    @Override
    protected void doLayout(float dtS) {
        if (autosize()) {
            zoomStackReset();
            unzoom();
        }
        super.doLayout(dtS);
    }

    /**
     * full unzoom
     */
    public void unzoom() {
        cam.set(bounds.w / 2.0f, bounds.h / 2.0f, camZMax());
    }

    private float camZMax() {
        return targetDepth(Math.min(space.window.W(), space.window.H()));
    }

    @Override
    public final void renderContent(ReSurface render) {
        if (!visible())
            return;

        GL2 gl = render.gl;

        float zoom = (float) (sin(Math.PI / 2 - focusAngle / 2) / (cam.z * sin(focusAngle / 2)));
        float H = h();
        float W = w();
        float s = zoom * Math.min(W, H);

        boolean scaleChanged;
        float scaleChangeTolerance = Prioritized.EPSILON;
        if (parent instanceof Surface) {
            Surface ps = (Surface) this.parent;
            scaleChanged = scale.setIfChanged(s * W /ps.w(), s * H /ps.h(), scaleChangeTolerance);
        } else {
            scaleChanged = scale.setIfChanged(s, s, scaleChangeTolerance);
        }

        if (scaleChanged) {
            //TODO invalidate pixel-visibility LOD
            //necessary?
        }


        render.push(cam, scale);

        gl.glPushMatrix();

        gl.glScalef(s, s, 1);
        gl.glTranslatef((W / 2) / s - cam.x, (H / 2) / s - cam.y, 0);

        super.renderContent(render);

        gl.glPopMatrix();

        render.pop();
    }

    @Override
    public Surface finger(Finger finger) {

        RectF b = this.bounds;
        finger.boundsScreen = b;

        return finger.push(cam, f->{
            Surface innerTouched = super.finger(f);

            if (!(innerTouched instanceof Finger.ScrollWheelConsumer)) {
                //wheel zoom: absorb remaining rotationY
                float dy = f.rotationY(true);
                if (dy != 0) {
                    zoomDelta(f.posGlobal(), dy * wheelZoomRate);
                    //zoomDelta(dy * wheelZoomRate);
                    zoomStackReset();
                }
            }


            if (innerTouched != null && f.clickedNow(2 /*right button*/)) {
                /** click-zoom */
                zoomNext(f, innerTouched);
            }


            if (innerTouched == null) {
                /*if (f.tryFingering(zoomDrag)) {
                    zoomStackReset();
                } else */
                if (f.test(contentPan)) {
                    zoomStackReset();
                }
                //}
            }

            //TODO
//            if (innerTouched instanceof MetaFrame) {
//                RectFloat p = cam.globalToPixel(innerTouched.bounds);
//                RectFloat i= b.intersection(p);
//                if (i!=null) {
//                    float pct = Math.max((i.w / b.w), (i.h / b.h));
//                    //or contained by metaframe...
//                    if (pct > 0.5f) {
//                        MetaFrame m = (MetaFrame) innerTouched;
//                        //m.get(S)
//                        System.out.println(m);
//                    }
//                }
//            }

            return innerTouched;
        });
    }

    private void zoomStackReset() {
        synchronized (zoomStack) {
            zoomStack.clear();
            zoomStack.add(this);
        }
    }

//    private boolean corner(v2 p) {
//        //TODO other 3 corners
//        return (p.x < CORNER_RADIUS && p.y  < CORNER_RADIUS);
//    }


    public AutoCloseable animate(Animated c) {
        return space.onUpdate(c);
    }

    private AutoCloseable animate(Runnable c) {
        return space.onUpdate(c);
    }

    @Override
    protected void starting() {

        OrthoSurfaceGraph p = (OrthoSurfaceGraph) rootParent();

        space = p;
        this.cam = new Camera();

        animate(cam);

        JoglWindow v = ((AbstractLayer) root()).window;
        v.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_PAGE_UP -> zoomParent();
                    case KeyEvent.VK_DOWN -> zoomNear(0, +1);
                    case KeyEvent.VK_UP -> zoomNear(0, -1);
                    case KeyEvent.VK_LEFT -> zoomNear(-1, 0);
                    case KeyEvent.VK_RIGHT -> zoomNear(+1, 0);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        super.starting();
    }

    private void zoomParent() {
        Surface at = touching();
        if (at!=null) {
            boolean kontinue;
            RectF a = at.bounds;
            do { //go up one level at least..
                Surfacelike atp = at.parent;
                if (atp instanceof Surface nextAT) {
                    kontinue = nextAT.bounds.equals(a);
                    at = nextAT;
                } else
                    kontinue = false;
            } while (kontinue);
            zoom(at.bounds);
        }
    }
    private void zoomNear(float dx, float dy) {

        Surface at = touching();
        if (at!=null) {
            boolean kontinue;
            RectF a = at.bounds;
            do { //go up one level at least..
                Surfacelike atp = at.parent;
                if (atp instanceof Surface nextAT) {
                    kontinue = nextAT.bounds.equals(a);
                    at = nextAT;
                } else
                    kontinue = false;
            } while (kontinue);
            zoomNear(at, cam.x + (dx * a.w/2), cam.y + (dy * a.h/2));
        }
    }

    private Surface touching() {
        //zoomStack.peek();
        //((OrthoSurfaceGraph)this.space).fingers.stream().map(x -> x.touching.getOpaque()).findFirst().ifPresentOrElse(null);

        return this.space.fingers.get(0).touching.getOpaque();
    }

    private void zoomNear(Surface at, float cx, float cy) {
        RectF a = at.bounds;
        RankedN<Surface> y = at.rank((r, min) ->{
            if (r == at) return Float.NaN;
            RectF b = r.bounds;
            if (a.equals(b) /*|| a.contains(b) || b.contains(a)*/) return Float.NaN;
            double dist = b.distanceTo(cx, cy);
            return (float)(-dist);
        }, 1);
        if (y!=null) {
            Surface s = y.first();
            if (s != null) {
                zoom(s.bounds);
            }
        }
    }

    @Override
    public Zoomed move(float x, float y) {
        throw new UnsupportedOperationException();
    }




    public void zoomDelta(v2 target, float deltaPct) {
        if (Math.abs(deltaPct) < Float.MIN_NORMAL)
            return; //no effect
        cam.set(target.x, target.y, cam.z * (1.0f + deltaPct));
    }

    private void zoomNext(Finger finger, Surface x) {

        synchronized (zoomStack) {

            int s = zoomStack.size();
            Surface top = zoomStack.peekLast();
            if (top == x) {
                if (s > 1)
                    zoomStack.removeLast(); //POP

                zoom(zoomStack.peekLast().bounds);
            } else {

                if (s + 1 >= ZOOM_STACK_MAX)
                    zoomStack.removeFirst(); //EVICT

                zoomStack.addLast(x); //PUSH

                zoom(x.bounds);
            }
        }
    }


    public void zoom(RectF b) {
        float zoomMargin = 0.1f;
        zoom(b.cx(), b.cy(), b.w, b.h, zoomMargin);
    }

    /**
     * choose best zoom radius for the target rectangle according to current view aspect ratio
     */
    private static float targetDepth(float w, float h, float margin) {
        float d = Math.max(w, h);
//        if (((((float) pw()) / ph()) >= 1) == ((w / h) >= 1))
//            d = h; //limit by height
//        else
//            d = w; //limit by width

        return targetDepth(d * (1 + margin));
    }


    private static float targetDepth(float viewDiameter) {
        return (float) ((viewDiameter * sin(Math.PI / 2 - focusAngle / 2)) / sin(focusAngle / 2));
    }

    private void zoom(float x, float y, float sx, float sy, float margin) {
        zoom(x, y, targetDepth(sx, sy, margin));
    }

    public final void zoom(v3 v) {
        zoom(v.x, v.y, v.z);
    }

    public void zoom(float x, float y, float z) {
        cam.set(x, y, z);
    }



    public boolean autosize() {
        return true;
    }

    public Surface overlayZoomBounds(Finger finger) {
        return new Finger.TouchOverlay(finger, cam);
    }

    public class Camera extends v3Anim implements SurfaceTransform {

        private static final float CHANGE_EPSILON = 0.001f;
        protected boolean change = true;

        {
            setDirect(0, 0, 1); //(camZmin + camZmax) / 2);
        }

        public Camera() {
            super(1);
        }

//        public v3 snapshot() {
//            return new v3(target.x, target.y, target.z);
//        }

        private transient final v3 before = new v3(), after = new v3();
        @Override
        public boolean animate(float dt) {
            //System.out.println(this);
            before.set(this);
            if (super.animate(dt)) {
                after.set(this);
                change = !before.equals(after, CHANGE_EPSILON);
                update();
                return true;
            }
            return false;
        }

        /**
         * TODO atomic
         */
        protected void update() {

            //System.out.println(z);
            RectF b = Zoomed.this.bounds;

            float W = b.w, H = b.h;

            float CAM_RATE = 3.0f;
            speed.set(Math.max(W, H) * CAM_RATE);

            float visW = W / scale.x / 2, visH = H / scale.y / 2; //TODO optional extra margin
            camXmin = b.x + visW;
            camYmin = b.y + visH;
            camXmax = b.x + W - visW;
            camYmax = b.y + H - visH;
        }

        @Override
        public final void setDirect(float x, float y, float z) {
            super.setDirect(camX(x), camY(y), camZ(z));
        }

        public float camZ(float z) {
            float camZmin = 1;
            return Util.clampSafe(z, camZmin, camZMax());
        }

        public float camY(float y) {
            return Util.clampSafe(y, camYmin, camYmax);
        }

        public float camX(float x) {
            return Util.clampSafe(x, camXmin, camXmax);
        }

//        public float motionSq() {
//            v3 t = new v3(target);
//            t.add(-x, -y, -z);
//            return t.lengthSquared();
//        }


        public v2 globalToPixel(float gx, float gy) {
            return new v2(
                    fma((gx - cam.x), scale.x, (w() / 2)),
                    fma((gy - cam.y), scale.y, (h() / 2))
            );
        }

        public void pixelToGlobal(float px, float py, v2 target) {
            target.set(
                    fma((px - (w() / 2)), 1/scale.x, cam.x),
                    fma((py - (h() / 2)), 1/scale.y, cam.y)
            );
        }


        /**
         * immediately get to where its going
         */
        public void complete() {
            setDirect(target.x, target.y, target.z);
        }

        public RectF globalToPixel(float x1, float y1, float x2, float y2) {
            v2 p = cam.globalToPixel(x1, y1);
            v2 q = cam.globalToPixel(x2, y2);
            return RectF.XYXY(p.x, p.y, q.x, q.y);
        }

        public RectF globalToPixel(RectF t) {
            float tx = t.x, ty = t.y;
            return globalToPixel(tx, ty, tx + t.w, ty+t.h);
        }

//
//    public final RectFloat globalToPixel(Surface t) {
//        return globalToPixel(t.bounds);
//    }
//
//

    }

}