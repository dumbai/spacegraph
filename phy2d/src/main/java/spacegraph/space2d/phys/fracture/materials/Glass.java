package spacegraph.space2d.phys.fracture.materials;

import jcog.math.v2;
import jcog.random.XoRoShiRo128PlusRandom;
import spacegraph.space2d.phys.common.Transform;
import spacegraph.space2d.phys.fracture.Material;

import java.util.Random;

/**
 * Material simulujuci sklo. Ohniska su generovane nahodne v n-kruzniciach s
 * malou odchylkou.
 *
 * @author Marek Benovic
 */
public class Glass extends Material {
    private final Random r = new XoRoShiRo128PlusRandom(1);

    /**
     * Konstruktor inicializujuci sklo.
     */
    public Glass() {
        m_shattering = 1.0f;
        m_radius = 4.0f;
    }

    @Override
    public v2[] focee(v2 startPoint, v2 vektor) {
        Transform t = new Transform();
        t.set(startPoint, 0);

        /**
         * Pocet prvkov v jednej kruznici.
         */
        int count = 30; /**
         * Pocet prvkov kruznice.
         */int levels = 4;
        int allCount = count * levels;

        v2[] va = new v2[allCount];
        for (int l = 0; l < levels; l++) {
            for (int c = 0; c < count; c++) {
                int i = l * count + c;

                double u = r.nextDouble() * Math.PI * 2; 
                double deficit = (r.nextDouble() - 0.5) * m_shattering / 20;
                double r = (l + 1) * m_shattering + deficit;

                double x = Math.sin(u) * r;
                double y = Math.cos(u) * r;

                v2 v = new v2((float) x, (float) y);

                va[i] = Transform.mul(t, v);
            }
        }

        return va;
    }

    @Override
    public String toString() {
        return "Glass";
    }
}