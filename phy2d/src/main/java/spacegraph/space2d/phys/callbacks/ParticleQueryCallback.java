package spacegraph.space2d.phys.callbacks;

import spacegraph.space2d.phys.collision.AABB;
import spacegraph.space2d.phys.dynamics.Dynamics2D;
import spacegraph.space2d.phys.dynamics.Fixture;

import java.util.function.Predicate;

/**
 * Callback class for AABB queries. See
 * {@link Dynamics2D#queryAABB(Predicate< Fixture >, AABB )}.
 *
 * @author dmurph
 */
@FunctionalInterface
public interface ParticleQueryCallback {
    /**
     * Called for each particle found in the query AABB.
     *
     * @return false to terminate the query.
     */
    boolean reportParticle(int index);
}
