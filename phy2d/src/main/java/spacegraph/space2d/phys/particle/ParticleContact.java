package spacegraph.space2d.phys.particle;

import jcog.math.v2;

public class ParticleContact {
    /**
     * Indices of the respective particles making contact.
     */
    public int indexA;
    public int indexB;
    /**
     * The logical sum of the particle behaviors that have been setAt.
     */
    public int flags;
    /**
     * Weight of the contact. A value between 0.0f and 1.0f.
     */
    public float weight;
    /**
     * The normalized direction from A to B.
     */
    public final v2 normal = new v2();
}
