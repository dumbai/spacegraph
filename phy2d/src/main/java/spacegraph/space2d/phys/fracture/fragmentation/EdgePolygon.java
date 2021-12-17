package spacegraph.space2d.phys.fracture.fragmentation;

import jcog.math.v2;
import spacegraph.space2d.phys.fracture.util.MyList;

import java.util.List;

/**
 * Hrana obecneho polygonu.
 *
 * @author Marek Benovic
 */
class EdgePolygon extends AEdge {
    public EdgePolygon(v2 v1, v2 v2) {
        super(v1, v2);
    }

    /**
     * List prienikovych bodov, ktore sa nachadzaju na danej hrane.
     */
    public final List<Vec2Intersect> list = new MyList<>();
}
