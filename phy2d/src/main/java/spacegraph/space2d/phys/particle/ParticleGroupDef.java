package spacegraph.space2d.phys.particle;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.Shape;
import spacegraph.util.ParticleColor;

/**
 * A particle group definition holds all the data needed to construct a particle group. You can
 * safely re-use these definitions.
 */
public class ParticleGroupDef {

    /**
     * The particle-behavior flags.
     */
    public int flags;

    /**
     * The group-construction flags.
     */
    public final int groupFlags;

    /**
     * The world position of the group. Moves the group's shape a distance equal to the value of
     * position.
     */
    public final v2 position = new v2();

    /**
     * The world angle of the group in radians. Rotates the shape by an angle equal to the value of
     * angle.
     */
    public final float angle;

    /**
     * The linear velocity of the group's origin in world co-ordinates.
     */
    public final v2 linearVelocity = new v2();

    /**
     * The angular velocity of the group.
     */
    public final float angularVelocity;

    /**
     * The color of all particles in the group.
     */
    public ParticleColor color;

    /**
     * The strength of cohesion among the particles in a group with flag b2_elasticParticle or
     * b2_springParticle.
     */
    public final float strength;

    /**
     * Shape containing the particle group.
     */
    public Shape shape;

    /**
     * If true, destroy the group automatically after its last particle has been destroyed.
     */
    public final boolean destroyAutomatically;

    /**
     * Use this to store application-specific group data.
     */
    public Object userData;

    public ParticleGroupDef() {
        flags = 0;
        groupFlags = 0;
        angle = 0;
        angularVelocity = 0;
        strength = 1;
        destroyAutomatically = true;
    }
}
