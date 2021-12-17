package spacegraph.space2d.hud;

import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.Fingering;
import spacegraph.layer.OrthoSurfaceGraph;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.SurfaceGraph;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.container.unit.UnitContainer;

import java.util.function.Function;

public class Hover<X extends Surface, Y extends Surface> extends Fingering {

    public final HoverModel model;
    final X source;
    private Function<X, Y> targetBuilder;
    volatile @Nullable Surface target;
    private RectF tgtBoundsPx;
    private long startTime;

    public Hover(X source, Function<X, Y> target, HoverModel model) {
        this.source = source;
        target(target);
        this.model = model;
        this.target = null;
    }

    public final Hover target(Function<X, Y> target) {
        //TODO invalidate target?
        this.targetBuilder = target;
        return this;
    }

    @Override
    protected boolean start(Finger f) {
        //TODO reset delay time

        startTime = System.nanoTime();
        if (update(f)) {
            return show();
        }
        return false;
    }

    @Override
    public boolean update(Finger f) {
        //update
        boolean focused = f.focused();
        if (focused && source.showing() && f.touching() == source) {

            float hoverTimeS = (float) ((System.nanoTime() - startTime) / 1.0E9);
            model.set(source, f, hoverTimeS);
            tgtBoundsPx = model.pos();

            Surface t = this.target;
            if (t != null) {
                if (tgtBoundsPx != null)
                    updatePos(t);
                else
                    t.hide();
            }

            return (tgtBoundsPx != null);
        } else {
            hide();
            return false;
        }
    }

//    public RectF sourceBounds(Finger f) {
//        return f.globalToPixel(source.bounds);
//    }


    protected boolean show() {
        SurfaceGraph r = source.root();
        if (r instanceof OrthoSurfaceGraph) {
            Stacking root = ((OrthoSurfaceGraph) r).root;

            Surface t = target = targetBuilder.apply(source);

            if (t != null) {
                t.hide();
                root.add(new WeakContainer(t));
                //updatePos();
                return true;
            }
        }
        return false;
    }

    private void updatePos(Surface t) {
        //Exe.invokeLater(()->{
        t.pos(tgtBoundsPx); //HACK TODO dont allow root Stacking to maximize pos that this resets
        t.show();
        //});
    }

    @Override
    public boolean defer(Finger finger) {
        return true;
    }


    protected void hide() {

        model.set(null, null, 0);
        if (target != null) {
            target.delete();
            target = null;
        }

    }

    @Override
    public void stop(Finger finger) {
        hide();
        super.stop(finger);
    }

    /** HACK ensures the hover is eventually removed */
    private final class WeakContainer extends UnitContainer {
        WeakContainer(Surface t) {
            super(t);
        }

        @Override
        protected boolean canRender(ReSurface r) {
            if (target==null) {
                delete();
                return false;
            } else
                return super.canRender(r);
        }
    }
}