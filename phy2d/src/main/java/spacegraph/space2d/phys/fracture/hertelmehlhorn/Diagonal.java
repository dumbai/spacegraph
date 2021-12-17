package spacegraph.space2d.phys.fracture.hertelmehlhorn;

/**
 * Hrana polygonu pri Hertel-Mehlhornovom algoritme.
 *
 * @author Marek Benovic
 */
class Diagonal {
    private static final int PRIME = 0x534D3595; 

    /**
     * Index prveho trojuholnika
     */
    int i1 = -1;

    /**
     * Index druheho trojuholnika
     */
    int i2 = -1;

    /**
     * Prvy a druhy node prveho utvaru - n11-n12 tvoria hranu - zachovane poradie.
     */
    NodeHM n11;
    NodeHM n12;

    /**
     * Prvy a druhy node druheho utvaru - n22-n21 tvoria hranu - zachovane poradie.
     */
    NodeHM n21;
    NodeHM n22;

    /**
     * Inicializuje diagonalu.
     *
     * @param index Index jedneho trojuholnika diagonaly
     * @param n1    Prvok spojoveho zoznamu pre 1. bod trojuholnika
     * @param n2    Prvok spojoveho zoznamu pre 2. bod trojuholnika
     */
    public void add(int index, NodeHM n1, NodeHM n2) {
        if (i1 == -1) {
            i1 = index;
            n11 = n1;
            n12 = n2;
        } else {
            i2 = index;
            n21 = n1;
            n22 = n2;
        }
    }

    @Override
    public int hashCode() {
        
        return hashCode(n11.index, n12.index);
    }

    /**
     * @param index1
     * @param index2
     * @return Vrati hashCode.
     */
    public static int hashCode(int index1, int index2) {
        return (index1 * PRIME) ^ (index2 * PRIME);
    }
}