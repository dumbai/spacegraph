package spacegraph.space2d.phys.fracture.hertelmehlhorn;

/**
 * Prvok spojoveho zoznamu reprezentujuci vrchol konvexneho polygonu.
 *
 * @author Marek Benovic
 */
class NodeHM {
    /**
     * Index mnohostenu
     */
    final int index;


    /**
     * Susedny vrchol mnohostenu
     */
    NodeHM prev;
    NodeHM next;

    /**
     * Inicializuje uzol vrcholu.
     *
     * @param index Index mnohostenu
     */
    NodeHM(int index) {
        this.index = index;
    }
}