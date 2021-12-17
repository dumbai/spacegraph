package spacegraph.space2d.container;

import jcog.signal.IntRange;
import org.eclipse.collections.api.block.procedure.primitive.BooleanProcedure;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.collection.MutableListContainer;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.AspectAlign;
import spacegraph.space2d.meta.LazySurface;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.button.ToggleButton;
import spacegraph.video.ImageTexture;

import java.util.function.Supplier;

public class BorderingView extends Bordering {

    public final IntRange capacity = new IntRange(2, 1, 16);

    public final MutableListContainer center;

    public BorderingView() {
        this(new Gridding());
    }

    public BorderingView(MutableListContainer center) {
        center(this.center = center);
    }

    public Surface toggler(String s, Supplier<Surface> x) {
        return _add(new CheckBox(s), x);
    }

    public Surface toggler(Surface s, Supplier<Surface> x) {
        return _add(new ToggleButton(s), x);
    }

    public Surface togglerIcon(String icon, Supplier<Surface> i) {
        return toggler(new AspectAlign(ImageTexture.awesome(icon).view()), i);
    }

    private ToggleButton _add(ToggleButton t, Supplier<Surface> x) {
        return t.on(new BooleanProcedure() {
            
            Surface active;
            
            @Override
            public void value(boolean active) {
                synchronized (t) {
                    if (active) {
                        assert(this.active == null);
                        this.active = BorderingView.this.setCenter(x, t);
                    } else {
                        assert(this.active!=null);
                        if (this.active.visible()) {
                            BorderingView.this.clearCenter(this.active);
                        }
                        this.active = null;
                    }
                }
            }
        });
    }


    private Surface setCenter(Supplier<Surface> x, ToggleButton nextActive) {
        synchronized (this) {
            int excess = (center.size()+1) - capacity.intValue();
            for (int i = 0; i < excess; i++)
                center.remove(0);

            LazySurface l;
            center.add(l = new LazySurface(x) {
                        @Override
                        public boolean delete() {
                            if (super.delete()) {
                                nextActive.value(false);
                                return true;
                            }
                            return false;
                        }
                    }/*).northeast(new PushButton("X", this::clearCenter)*/
            );
            return l;
        }
    }

    private void clearCenter(Surface s) {
        synchronized (this) {
//            if (active != null) {
//                active.set(false);
//                active = null;
//            }
            center.remove(s);
        }
    }
}