package com.bulletphysics.collision.dispatch;

import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;

/**
 * RayResultCallback is used to report new raycast results.
 */
public abstract class RayResultCallback {

    public static final double RAY_EPSILON = 0.0001f;

    public float closestHitFraction = 1.0f;
    public CollisionObject collisionObject;
    private final short collisionFilterGroup = CollisionFilterGroups.DEFAULT_FILTER;
    private final short collisionFilterMask = CollisionFilterGroups.ALL_FILTER;

    public boolean hasHit() {
        return (collisionObject != null);
    }

    public boolean needsCollision(BroadphaseProxy proxy0) {
        boolean collides = ((proxy0.collisionFilterGroup & collisionFilterMask) & 0xFFFF) != 0;
        collides = collides && ((collisionFilterGroup & proxy0.collisionFilterMask) & 0xFFFF) != 0;
        return collides;
    }

    public abstract float addSingleResult(CollisionWorld.LocalRayResult rayResult, boolean normalInWorldSpace);
}