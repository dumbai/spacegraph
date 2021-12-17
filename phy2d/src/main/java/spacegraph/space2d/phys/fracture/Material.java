package spacegraph.space2d.phys.fracture;

import jcog.Util;
import jcog.math.v2;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.fracture.fragmentation.Smasher;
import spacegraph.space2d.phys.fracture.materials.Diffusion;
import spacegraph.space2d.phys.fracture.materials.Glass;
import spacegraph.space2d.phys.fracture.materials.Uniform;

/**
 * Material telesa
 *
 * @author Marek Benovic
 */
public abstract class Material {


    /**
     * Najmensi ulomok, ktory je mozne triestit - aby sa zabranilo rekurzivnemu triesteniu.
     */
    public static final float MASS_DESTRUCTABLE_MIN = 0.005f;


    /**
     * Po destrukcii kruhu je kruh transformovany na regular polygon s danym poctom vrcholov.
     */
    public static final int CIRCLEVERTICES = 11;

    /**
     * Objekty musia mat najdlhsiu hranu (radius) vacsiu ako dany limit, vacsi obsah
     * a taktiez mass / radius.
     */
    public static final double MINFRAGMENTSIZE = 0.01;

    /**
     * Material polárneho logaritmického rozptylu
     */
    public static final Material DIFFUSION = new Diffusion();

    /**
     * Materiál rovnomerného rozptylu
     */
    public static final Material UNIFORM = new Uniform();

    /**
     * Sklo
     */
    public static final Material GLASS = new Glass();

    /**
     * Od akeho limitu tangentInertia sa zacne objekt triestit.
     */
    public float m_rigidity = 64.0f;

    /**
     * Na ake drobne kusky sa objekt zvykne triestit (minimalne). Sluzi pre
     * material na urcovanie, do akej vzdialenosti budu fragmenty sucastou
     * povodneho telesa a ktore sa odstiepia. Polomer na ^2
     */
    public float m_shattering = 4.0f;

    /**
     * Polomer kruhu, z ktoreho sa rataju fragmenty (fragmenty mimo kruhu su
     * zjednocovane do povodneho telesa)
     */
    protected float m_radius = 2.0f;








    /**
     * Abstraktna funkcia urcujuca sposob triesenia.
     *
     * @param contactVector
     * @param contactPoint
     * @return Vrati ohniska v ktorych sa bude teleso triestit.
     */
    protected abstract v2[] focee(v2 contactPoint, v2 contactVector);

    /**
     * Fragmentacia telesa.
     *
     * @param p             Teleso
     * @param localVel        Vektor ratajuc aj jeho velkost - ta urcuje rychlost.
     * @param localPos     Lokalny bod narazu na danom telese.
     * @param normalImpulse Intenzita kolizie
     * @return Vrati pole Polygonov, na ktore bude dany polygon rozdeleny
     */
    Polygon[] split(Smasher geom, Polygon p, v2 localPos, v2 localVel, float normalImpulse) {
        v2[] foceeArray = focee(localPos, localVel);

        
        float ln = localVel.length();

        
        float r = m_radius;

        float c = 2;

        float dd = Util.sqr(Math.max(ln * c, r));

        if (ln > Settings.EPSILON) {

            float sin = -localVel.x / ln;
            float cos = -localVel.y / ln;

            float rr = r * r;
            geom.calculate(p, foceeArray, localPos, point -> {
                float x = localPos.x - point.x;
                float y = localPos.y - point.y;

                x = cos * x + -sin * y;
                y = sin * x + cos * y;

                float xx = x * x;
                float yy = y * y;
                return (y < 0 && (xx + yy < rr)) || (y > 0 && (xx / rr + yy / dd < 1));
            });
        }

        return geom.fragments;
    }
}
