package spacegraph.space2d.phys.fracture.materials;

import jcog.math.v2;
import jcog.random.XoRoShiRo128PlusRandom;
import spacegraph.space2d.phys.fracture.Material;

import java.util.Random;

/**
 * Material, kde ohniska fragmentov su generovane rovnomerne nahodne v priestore.
 *
 * @author Marek Benovic
 */
public class Uniform extends Material {
    private static final Random r = new XoRoShiRo128PlusRandom(1);

    @Override
    public v2[] focee(v2 point, v2 velocity) {
        int num = 32;
        v2[] focee = new v2[num];

        float scale = 2 * m_shattering;

        for (int i = 0; i < num; ++i) {
            float x = r.nextFloat() - 0.5f; 
            float y = r.nextFloat() - 0.5f; 

            focee[i] = new v2(point.x + x * scale, point.y + y * scale);
        }
        return focee;
    }

    @Override
    public String toString() {
        return "Uniform diffusion";
    }
}