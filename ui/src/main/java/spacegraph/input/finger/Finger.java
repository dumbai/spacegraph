package spacegraph.input.finger;

import com.jogamp.opengl.GL2;
import jcog.TODO;
import jcog.Util;
import jcog.data.bit.AtomicMetalBitSet;
import jcog.data.list.Lst;
import jcog.math.v2;
import jcog.signal.MutableFloat;
import jcog.thing.Part;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.SpaceGraph;
import spacegraph.layer.OrthoSurfaceGraph;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.space2d.hud.Overlay;
import spacegraph.space2d.hud.Zoomed;
import spacegraph.util.SurfaceTransform;
import spacegraph.video.JoglWindow;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import static spacegraph.input.finger.Fingering.Idle;

/**
 * gestural generalization of mouse cursor's (or touchpad's, etc)
 * possible intersection with a surface and/or its sub-surfaces.
 * <p>
 * tracks state changes and signals widgets of these
 * <p>
 * TODO differentiate this between subclasses which touch only one surface at a time, and those which can touch several (multi-select)
 */
public abstract class Finger extends Part<SpaceGraph> implements Predicate<Fingering> {


    public final v2 posPixel = new v2();
    public final v2 posScreen = new v2();
    public final v2[] posPixelPress;
    public final v2 posGlobal = new v2();
    float dragThresholdPx = 10;


    public final AtomicMetalBitSet prevButtonDown = new AtomicMetalBitSet();
    /**
     * last local and global positions on press (downstroke).
     * TODO is it helpful to also track upstroke position?
     */
    /**
     * widget above which this finger currently hovers
     */
    public final AtomicReference<Surface> touching = new AtomicReference<>();
    /**
     * a exclusive locking/latching state which may be requested by a surface
     */
    public final AtomicReference<Fingering> fingering = new AtomicReference<>(Idle);
    /**
     * ex: true when finger enters the window, false when it leaves
     */
    protected final AtomicBoolean focused = new AtomicBoolean(false);
    final Lst<SurfaceTransform> transforms = new Lst();

    public final int buttons;

    //@Deprecated protected transient UnaryOperator<v2> _screenToGlobalRect;
    public final AtomicMetalBitSet buttonDown = new AtomicMetalBitSet();
    private final MutableFloat[] rotation = Util.arrayOf(i->new MutableFloat(), new MutableFloat[3]);
    public RectF boundsScreen;


    protected Finger(int buttons) {
        assert (buttons < 32);
        this.buttons = buttons;
        posPixelPress = new v2[this.buttons];
        for (int i = 0; i < this.buttons; i++)
            posPixelPress[i] = new v2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    }


    /** global position of the cursor center
     * warning: the vector instance returned by this and other methods are mutable.  so they may need to be cloned when accessed to record the state across time.
     */
    public v2 posGlobal() {
        return posGlobal;
    }


    public static v2 normalize(v2 pPixel, JoglWindow win) {
        v2 y = new v2(pPixel);
        y.sub(win.getX(), win.getY());
        y.scaled(1.0f / win.W(), 1.0f / win.H());
        return y;
    }

    public static v2 normalize(v2 p, RectF b) {
        v2 y = new v2(p);
        y.sub(b.x, b.y);
        y.scaled(1.0f / b.w, 1.0f / b.h);
        return y;
    }

    public final @Nullable Surface touching() {
        return focused() ? touching.getOpaque() : null;
    }

    /**
     * call when finger exits the window / screen, the window becomes unfingerable, etc..
     */
    public final void exit() {
        focused.set(false);
        stop((Fingering)null);
    }

    /**
     * call when finger enters the window
     */
    public final void enter() {
        focused.set(true);
    }

    /**
     * commit all buttons
     */
    protected void commitButtons() {
        prevButtonDown.copyFrom(buttonDown);
    }

    /**
     * commit one button
     */
    private void commitButton(int button) {
        prevButtonDown.set(button, buttonDown.test(button));
    }

    public String buttonSummary() {
        return prevButtonDown.toBitString() + " -> " + buttonDown.toBitString();
    }

    /**
     * asynch updates: buttons and motion
     * if a surface is touched, calls its
     * event handler.  this could mean that there is either mouse
     * motion or button status has changed.
     */
    protected void updateButtons(short[] nextButtonDown) {

        for (short b : nextButtonDown) {

            boolean pressed = (b > 0);

            if (!pressed) b = (short) -b;

            updateButton(b-1, pressed);
        }
//        System.out.println(buttonSummary());
    }

    public void copyButtons(Finger toCopy) {
        for (int i = 0; i < buttons; i++)
            updateButton(i, toCopy.buttonDown.test(i));
    }

    private void updateButton(int button, boolean pressed) {
        buttonDown.set(button, pressed);

        if (pressed && !wasPressed(button))
            posPixelPress[button].set(posPixel);
    }




    /**
     * call once per frame
     */
    protected final void clearRotation() {
        for (MutableFloat r : rotation)
            r.set(0);
    }


    private boolean _dragging(int button) {
        v2 g = posPixelPress[button];
        /*
         * drag threshold (in screen pixels)
         */

        return (g.distanceSq(posPixel) > Util.sqr(dragThresholdPx));
    }

    public boolean dragging(int button) {
        return pressed(button) && _dragging(button);
    }

    /**
     * allows a fingered object to push the finger off it
     */
    public boolean off(Surface fingered) {
        return touching.compareAndSet(fingered, null);
    }

    public boolean released(int button) {
        return !pressed(button);
    }

    public boolean pressed(int button) {
        return buttonDown.test(button);
    }

    private boolean wasPressed(int button) {
        return prevButtonDown.test(button);
    }

    public boolean wasReleased(int button) {
        return !wasPressed(button);
    }

    public boolean releasedNow(int button) {
        return !pressed(button) && wasPressed(button);
    }

    public boolean pressedNow(int button) {
        return pressed(button) && !wasPressed(button);
    }

    /**
     * additionally tests for no dragging while pressed
     */
    public final boolean clickedNow(int button) {
        return clickedNow(button, null);
    }

    public boolean clickedNow(int button, @Nullable Surface c) {

//        System.out.println(pressing(i) + "<-" + wasPressed(i));

        if (releasedNow(button) && !_dragging(button)) {
            if (c == null || intersects(c.bounds)) {
                commitButton(button); //absorb the event
                return true;
            }
        }

        return false;
    }

    /**
     * acquire an exclusive fingering state
     */
    @Override
    public final boolean test(Fingering next) {

        Fingering prev = this.fingering.get();

        if (!prev.update(this)) {
            stop(prev);
            prev = Idle;
        }

        if (prev != next) {

            if (prev.defer(this)) {

                if (next.start(this)) {

                    prev.stop(this);

                    fingering.setRelease(next);

                    return true;
                }
            }
        }

        return false;
    }

    private void stop(Fingering prev) {
        if (prev==null)
            prev = fingering.get();
        if (prev!=Idle) {
            prev.stop(this);
            fingering.set(Idle);
        }
    }


    protected void rotationAdd(float[] next) {
        MutableFloat[] rr = this.rotation;
        for (int i = 0; i < next.length; i++) {
            float r = next[i];
            if (r != 0) rr[i].set(r);
        }
    }

    public final float rotationX(boolean take) {
        return rotation(0, take);
    }

    public final float rotationY(boolean take) {
        return rotation(1, take);
    }

    public final float rotationZ(boolean take) {
        return rotation(2, take);
    }

    protected final float rotation(int which, boolean take) {
        MutableFloat r = rotation[which];
        return take ? r.getAndSet(0) : r.asFloat();
    }

//    /**
//     * visual overlay representation of the Finger; ie. cursor
//     */
//    public Surface overlayCursor() {
//        return new FingerRendererSurface();
//    }

    public final boolean focused() {
        return focused.getOpaque();
    }

    public final v2 posRelative(Surface s) {
        return posRelative(s.bounds);
    }

    public v2 posRelative(RectF b) {
        //return normalize(posGlobal(), b);
//        RectFloat r = globalToPixel(b);
//        return new v2(
//        (posPixel.x - r.x)/r.w, (posPixel.y - r.y)/r.h
//        );

        v2 g = posGlobal();
        return new v2((g.x - b.x) / b.w, (g.y - b.y) / b.h);
    }


    public Fingering fingering() {
        return fingering.getOpaque();
    }

    public boolean intersects(RectF bounds) {
        //System.out.println(bounds + " contains " + posGlobal() + " ? " + bounds.contains(posGlobal()));
        //return globalToPixel(bounds).contains(posPixel);
        return posRelative(bounds).inUnit();
    }

//    public v2 globalToNumScreens(RectFloat bounds) {
//        RectFloat p = globalToPixel(bounds);
//        RectFloat b = this.boundsScreen;
//        return new v2(p.w / b.w, p.h / b.h);
//    }

    public RectF globalToPixel(RectF bounds) {
        int n = transforms.size();
        return switch (n) {
            case 0 -> bounds;
            case 1 -> ((Zoomed.Camera) transforms.getLast()).globalToPixel(bounds);
            default -> throw new TODO();
        };
    }

    public final <S extends Surface> S push(SurfaceTransform t, Function<Finger, S> fingering) {
        v2 p = posGlobal.clone();
        transforms.add(t);
        try {
            t.pixelToGlobal(posGlobal.x, posGlobal.y, posGlobal);
            return fingering.apply(this);
        } finally {
            posGlobal.set(p);
            transforms.removeLastFast();
        }
    }
    
    public final <S extends Surface> S push(v2 g, Function<Finger, S> fingering) {
        v2 p = posGlobal.clone();
        posGlobal.set(g);
        S result = fingering.apply(this);
        posGlobal.set(p);
        return result;
    }

    public Surface cursorSurface() {
        return null;
    }

    /**
     * marker interface for surfaces which absorb wheel motion, to prevent other system handling from it (ex: camera zoom)
     */
    public interface ScrollWheelConsumer {
    }

    public static final class TouchOverlay extends Overlay {

        private final Finger f;

        public TouchOverlay(Finger f, Zoomed.Camera cam) {
//            this(f::touching, cam);
            super(cam);
            this.f = f;
        }

//        public ZoomBoundsOverlay(Supplier<Surface> touching, Zoomed.Camera cam) {
//            super(cam);
//            this.touching = touching;
//        }

        @Override
        protected boolean enabled() {
            return f.focused();
        }

        @Override
        protected Surface target() {
            Surface s = f.touching();
            if (s != null) {
                //color HASH
                //color.setAt()
            }
            return s;
        }

        @Override
        protected void paint(Surface t, GL2 gl, ReSurface reSurface) {
            drawBoundsFrame(t, gl);

            //paintCaption(t, reSurface, gl);

        }
    }

// /**
//     * dummy intermediate placeholder state
//     */
//    private final Fingering STARTING = new Fingering() {
//
//        @Override
//        public boolean start(Finger f) {
//            return false;
//        }
//
//        @Override
//        public boolean update(Finger f) {
//            return true;
//        }
//    };

    public static class CursorSurface extends MutableUnitContainer {

        private final Finger f;
        @Deprecated
        public FingerRenderer renderer = FingerRenderer.rendererCrossHairs1;

        {
            clipBounds = false;
        }

        public CursorSurface(Finger f) {
            super();
            this.f = f;
        }

        @Override
        protected void renderContent(ReSurface r) {
            doLayout(r.frameDT);
            super.renderContent(r);
            paint(r);
        }

        @Override
        protected RectF innerBounds() {
            JoglWindow win = ((OrthoSurfaceGraph) root()).window;
            float sw = win.W(), sh = win.H();
            return RectF.XYWH(f.posPixel, sw*0.1f, sh*0.1f);
        }

        protected void paint(ReSurface renderer) {
            if (f.focused()) {

                FingerRenderer r = this.renderer;

                Fingering ff = f.fingering();
                if (ff != Idle) {
                    @Nullable FingerRenderer specialRenderer = ff.cursor();
                    if (specialRenderer != null)
                        r = specialRenderer;
                }

                if (r!=null) {
                    r.paint(f.posPixel, f, renderer.frameDT, renderer.gl);
                }

                //for debugging:
//                if (ortho!=null) {
//                    renderer.paint(
//                        ortho.cam.worldToScreen(ortho.cam.screenToWorld(posPixel)),
//                            Finger.this, surfaceRender.dtMS, gl);
//                }
            }

        }
    }
}