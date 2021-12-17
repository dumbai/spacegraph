package spacegraph.space2d.container.graph;

import jcog.event.Off;
import spacegraph.space2d.Surface;
import spacegraph.space2d.widget.port.Wire;

public abstract class Link {

    public final Wire id;

    public Link(Wire wire) {
        super();
        this.id = wire;
    }

    public Link on(Off r) {
        id.offs.add(r);
        return this;
    }

    public Link on(Surface hostage) {
        return on(hostage::delete);
    }

    public final void remove(GraphEdit2D g) {
        g.removeWire(id); //id.a, id.b);
    }



}
