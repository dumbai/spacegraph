package spacegraph.space2d.container;

import spacegraph.input.finger.Finger;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class EmptyContainer extends ContainerSurface {

    @Override
    public int childrenCount() {
        return 0;
    }

    @Override
    public final void forEach(Consumer<Surface> o) {

    }

    @Override
    protected void doLayout(float dtS) {

    }

    @Override
    public Surface finger(Finger finger) {
        return null;
    }

    @Override
    protected void renderContent(ReSurface r) {

    }

    @Override
    public final boolean whileEach(Predicate<Surface> o) {
        return true;
    }

    @Override
    public final boolean whileEachReverse(Predicate<Surface> o) {
        return true;
    }
}
