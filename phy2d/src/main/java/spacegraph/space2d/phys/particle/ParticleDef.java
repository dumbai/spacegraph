package spacegraph.space2d.phys.particle;

import jcog.math.v2;
import spacegraph.util.ParticleColor;

public class ParticleDef {
    /**
     * Specifies the type of particle. A particle may be more than one type. Multiple types are
     * chained by logical sums, for example: pd.flags = ParticleType.b2_elasticParticle |
     * ParticleType.b2_viscousParticle.
     */
    int flags;

    /**
     * The world position of the particle.
     */
    public final v2 position = new v2();

    /**
     * The linear velocity of the particle in world co-ordinates.
     */
    public final v2 velocity = new v2();

    /**
     * The color of the particle.
     */
    public ParticleColor color;

    /**
     * Use this to store application-specific body data.
     */
    public Object userData;
}
