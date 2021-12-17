package spacegraph.space2d.widget;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL2;
import jcog.Fuzzy;
import jcog.Is;
import jcog.Util;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.input.key.KeyPressed;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.EmptySurface;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.space2d.hud.Hover;
import spacegraph.space2d.hud.HoverModel;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.util.math.Color4f;
import spacegraph.video.Draw;

/**
 * Base class for GUI widgets, loosely analogous to Swing's JComponent as an abstract hierarchy root
 * https://en.wikipedia.org/wiki/Template:GUI_widgets
 */
@Is({"Widget_(GUI)", "Template:GUI_widgets"})
public class Widget extends MutableUnitContainer<Surface> implements KeyPressed {

    private @Nullable Hover<Surface, Surface> hover;

    public static final float marginPctDefault = 0.04f;



    /** the fundamental current 'color' of the widget. not necessarily to match or conflict
     * with what it actually displays but instead to provide a distinctive color for widgets
     * which otherwise do not specify styling.
     */
    public final Color4f color = new Color4f()
            //default random color tint
            .hsl(System.identityHashCode(this), 0.2f, 0.1f).a(0.9f);

    /**
     * z-raise/depth: a state indicating push/pull (ex: buttons)
     * positive: how lowered the button is: 0= not touched, to 1=push through the screen
     * zero: neutral state, default for components
     * negative: how raised
     */
    protected float dz;

    protected boolean focused;

    /**
     * indicates current level of activity of this component, which can be raised by various
     * user and system actions and expressed in different visual metaphors.
     * positive: active, hot, important
     * zero: neutral
     * negative: disabled, hidden, irrelevant
     */
    protected float pri;

    public Widget() {
        this(new EmptySurface());
    }


    public Widget(Surface content) {
        super(content);
    }


    @Override
    protected void paintIt(GL2 gl, ReSurface r) {
        //pri decay
        //////            1 - (float) Math.exp(-(((double) dt) / n.dur()) / memoryDuration.floatValue());
        float DECAY_PERIOD = 2; //TODO use exponential decay formula
        double decayRate = Math.exp(-(((double) r.frameDT) / DECAY_PERIOD));
        pri *= decayRate;
        //float PRI_DECAY = 0.97f; //TODO use exponential decay formula
        //pri = Util.clamp(pri * PRI_DECAY, 0, 1f);

        paintWidget(bounds, gl);
    }

    @Override
    protected void renderContent(ReSurface r) {
        super.renderContent(r);
        if (focused) {
            float p = this.pri;
            //float th = Math.min(b.w, b.h) * (0.1f + 0.1f * t);
            r.gl.glColor4f(0.5f + 0.5f * p, 0.55f, 0.35f, 0.75f);
            //Draw.rectFrame(b, th, gl);
            float th = 3 + p;
            Draw.rectStroke(bounds, th, r.gl);
        }
    }



    protected void paintWidget(RectF bounds, GL2 gl) {
        float dim = 1.0f - (dz /* + if disabled, dim further */) / 3.0f;
        float bri = Fuzzy.or(0.1f * dim, pri/8);
        color.glBri(bri, gl);
        Draw.rect(bounds, gl);
    }

    public Widget tooltip(String s) {
        if (s == null || s.isEmpty())
            this.hover = null;
        else {
            //BitmapLabel hoverLabel = new BitmapLabel(s);
            //hoverLabel.backgroundColor(0.9f, 0.5f, 0f, 0.5f);
            VectorLabel hoverLabel = new VectorLabel(s);
            hoverLabel.fgColor.set(1,1,1,0.5f);
            this.hover = new MyHover(hoverLabel);
        }
        return this;
    }

    @Override
    public Surface finger(Finger f) {

        Surface s = super.finger(f);
        if ((s == null || s == this) && tangible() && f.intersects(bounds)) {

            boolean pressing = f.pressedNow(0) || f.pressedNow(2);
            if (!focused && pressing) {
                focus();
                priAtleast(0.75f);
            } else {
                priAtleast(0.5f);
            }

//            if (pressing) {
                Hover<Surface, Surface> h = this.hover;
                if (h != null)
                    f.test(h);

                return this;
//            }


        }

        return s;

    }

    public boolean tangible() {

        return true;
    }

    private void priAtleast(float p) {
        pri = Math.max(pri, p);
    }




    /** add temperature to this widget, affecting its display and possibly other behavior.  useful for indicating
     *  transient activity */
    public void pri(float inc) {
        this.pri = Util.unitize(pri + inc);
    }

    public float pri() { return pri; }

    @Override
    public void keyStart() {
        focused = true;
        pri(1.0f);
    }

    @Override
    public void keyEnd() {
        focused = false;
    }


    @Override
    public boolean key(KeyEvent e, boolean pressedOrReleased) {
        if (pressedOrReleased) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_TAB:
                    //TODO
                    // focus traversal
                    break;
//                case KeyEvent.VK_PRINTSCREEN:
//                    System.out.println(this); //TODO debugging
//                    return true;
            }
        }
        return false;
    }

    public final boolean focused() {
        return focused;
    }

    public final Widget color(float r, float g, float b) {
        return color(r,g,b, 1.0f);
    }

    /** sets the background color */
    public final Widget color(float r, float g, float b, float a) {
        color.set(r,g,b,a);
        return this;
    }

	/** temporary */
    private class MyHover extends Hover {

        MyHover(Surface hoverLabel) {
            super(Widget.this, b -> hoverLabel, new MyCursorModel());
        }

//        @Override
//        public boolean update(Finger f) {
////            float s = model.hoverTimeS;
////            float f1 = 0.1f + (s < 4 ? (float) Math.exp(s) : 1) * 0.2f;
//            //hoverLabel.alpha(Util.clamp(f1, 0.1f, 0.5f));
//            return super.update(f);
//        }
    }

    /** attached relative to cursor center and sized relative to element */
    public static class MyCursorModel extends HoverModel {

        @Override
        public RectF pos() {
            //RectFloat ss = f.globalToPixel(s.bounds);
            final Finger f = this.f;
            if (f == null) return null;
            RectF bs = f.boundsScreen;
            if (bs == null)
                return null;
            float scale = Math.max(bs.w, bs.h);
            double pulse = Math.cos(hoverTimeS * 6.0f) * 0.05f;
            float ss = (float) (
                    pulse +
                            Util.clamp(Math.exp(-hoverTimeS* 4.0f)*1.5f, 0.25f, 2.0f)) *
                    scale;
            return RectF.XYWH(f.posPixel, ss, ss);
        }
    }

}