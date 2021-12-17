package spacegraph.space2d.hud;

import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.Surface;

/**
 * mutable hover state
 * used to compute display parameters
 */
public abstract class HoverModel {
    @Nullable public Surface s;
    @Nullable public Finger f;
    public float hoverTimeS;

    public void set(Surface root, Finger finger, float hoverTimeS) {
        this.s = root;
        this.f = finger;
        this.hoverTimeS = hoverTimeS;
    }

    public abstract RectF pos();

    /** attached relative to cursor center and sized relative to element */
    public static class Cursor extends HoverModel {
        float scale = 0.25f;

        @Override
        public RectF pos() {
            final Finger f = this.f;
            float resolution = Math.max(f.boundsScreen.w, f.boundsScreen.h);
            float ss = scale * resolution;
            return RectF.XYWH(f.posPixel, ss, ss);
        }
    }

    /** smaller and to the side so as not to cover any of the visible extents of the source.
     *  uses global screen pos as a heuristic of what direction to shift towards to prevent
     *  clipping beyond the screen edge
     * */
    public static class ToolTip extends HoverModel {

        @Override
        public RectF pos() {
            RectF ss = f.globalToPixel(s.bounds);
            return ss.scale(0.25f).move(ss.w/2, ss.h / 2);
        }
    }

    /** maximized to screen extents */
    public static class Maximum extends HoverModel {

        @Override
        public RectF pos() {
            return f.boundsScreen;
        }
    }

    public static class Exact extends HoverModel {

        @Override
        public RectF pos() {
            return s.bounds;
        }
    }
}