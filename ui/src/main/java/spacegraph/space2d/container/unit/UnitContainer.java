package spacegraph.space2d.container.unit;

import spacegraph.space2d.Surface;

public abstract class UnitContainer<S extends Surface> extends AbstractUnitContainer<S> {

    public final S the;

    protected UnitContainer(S the) {
        this.the = the;
    }



    @Override
    public final S the() {
        return the;
    }
}
