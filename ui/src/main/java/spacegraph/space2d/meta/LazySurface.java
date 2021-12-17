package spacegraph.space2d.meta;

import jcog.exe.Exe;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.SafeSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.video.ImageTexture;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class LazySurface extends MutableUnitContainer<Surface> {

    private final Supplier<Surface> async;

    /*
    state:
        0 = constructed
        1 = async content build dispatch to executor
        2 = content ready
     */
    final AtomicInteger state = new AtomicInteger(0);

    public LazySurface(Supplier<Surface> async) {
        this(async, ImageTexture.awesome("refresh" /* fa5: sync */).view(1)); //can this be shared?)
    }

    public LazySurface(Supplier<Surface> async, String msgWhileWaiting) {
        this(async, new VectorLabel(msgWhileWaiting));
    }

    public LazySurface(Supplier<Surface> async, Surface whileWaiting) {
        super(whileWaiting);
        this.async = async;
    }

    @Override
    protected void renderContent(ReSurface r) {
        //invoke on becoming visible

        if (state.compareAndSet(0, 1)) {
            build();
        }

        super.renderContent(r);
    }

    private void build() {
        Exe.run/*Later*/(()->{
            //TODO profile option
            Surface next = SafeSurface.safe(async);
            //RectFloat b = the().bounds;

            set(next);

            state.set(2);
            //TODO if possible try to reattach the view to the parent of this, eliminating this intermediary
            //((Container)p).replace
        });
    }

}