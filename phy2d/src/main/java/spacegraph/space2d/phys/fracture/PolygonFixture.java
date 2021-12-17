package spacegraph.space2d.phys.fracture;

import jcog.math.v2;
import spacegraph.space2d.phys.dynamics.Fixture;
import spacegraph.space2d.phys.fracture.util.MyList;

import java.util.List;

/**
 * Sluzi na aplikovanie konkavnych polygonov do jbox2d enginu. Fixtury telesa,
 * ktore su produktom konvexnej dekompozicie, maju v sebe ulozenu referenciu na
 * PolygonFixture instanciu, ktory reprezentuje povodne konkavne teleso.
 * PolygonFixture zaroven obsahuje List referencii na jeho jednotlive konvexne
 * fixtures.
 *
 * @author Marek Benovic
 */
public class PolygonFixture extends Polygon {
    /**
     * Inicializuje inicializujuci prazdny polygon.
     */
    public PolygonFixture() {
        super();
    }

    /**
     * Inicializuje polygon s mnozinou vrcholov z pamatera (referencne zavisle).
     *
     * @param v
     */
    public PolygonFixture(v2[] v) {
        super(v);
    }

    /**
     * Kopirovaci konstruktor. Inicializuje jednoduchym kopirovanim referencie.
     *
     * @param pg
     */
    PolygonFixture(Polygon pg) {
        vertices = pg.vertices();
        vertexCount = pg.size();
    }

    /**
     * Mnozina fixture, z ktorych dany polygon pozostava (reprezentuju konvexnu
     * dekompoziciu).
     */
    public final List<Fixture> fixtureList = new MyList<>();
}
