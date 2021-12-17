package spacegraph.space2d.phys.fracture.fragmentation;

import jcog.math.v2;
import spacegraph.space2d.phys.fracture.Fragment;

/**
 * Hrana Voronoi diagramu.
 *
 * @author Marek Benovic
 */
class EdgeDiagram extends AEdge {
    EdgeDiagram(v2 v1, v2 v2) {
        super(v1, v2);
    }

    /**
     * Fragmenty, ktore ohranicuje dana hrana.
     */
    public Fragment d1;
    public Fragment d2;
}