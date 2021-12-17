package spacegraph.space2d.phys.fracture;

import jcog.math.v2;
import spacegraph.space2d.phys.common.MathUtils;

/**
 * Polygon pre voronoi diagram. Funguje ako ArrayList 2D bodov typu Point2D,
 * ktory potom zotriedim podla ohniska na konvexny polygon. V pripade potreby si
 * moze uzivatel dodefinovat dalsie funkcie - oddedit od polygonu a nadefinovat
 * si funkcie na vypocet obsahu, alebo taziska. Jedna sa o specificky pripad polygonu.
 * Reprezentuje uz konkretny ulomok povodneho telesa.
 *
 * @author Marek Benovic
 * @version 1.0
 */
public class Fragment extends Polygon {
    /**
     * Inicializuje prazdny fragment
     */
    public Fragment() {
    }
    public Fragment(int sides) {
        super(sides);
    }










    /**
     * Ohnisko fragmentu
     */
    public v2 focus;

    /**
     * Pomocna premenna pre vypocet do geometry kniznice.
     */
    public boolean visited;

    /**
     * Zotriedi konvexny polygon podla bodu focus na zaklade uhlov jednotlivych
     * vrcholov
     */
    public void resort() {
        int size = size();
        double[] comparer = new double[size];
        for (int i = 0; i != size; ++i) {
            comparer[i] = MathUtils.angle(get(i), focus);
        }
        for (int i = 0; i != size; ++i) {
            int maxIndex = i;
            for (int j = i + 1; j != size; ++j) {
                if (comparer[j] < comparer[maxIndex]) {
                    maxIndex = j;
                }
            }
            double swap = comparer[i];
            comparer[i] = comparer[maxIndex];
            comparer[maxIndex] = swap;
            swap(i, maxIndex);
        }
    }
















    /**
     * Vymeni 2 vrcholy polygonu
     *
     * @param i
     * @param j
     */
    private void swap(int i, int j) {
        v2[] a = this.vertices;
        v2 item = a[i];
        a[i] = a[j];
        a[j] = item;
    }
}
