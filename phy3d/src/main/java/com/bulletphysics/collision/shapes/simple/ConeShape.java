/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.bulletphysics.collision.shapes.simple;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.shapes.convex.ConvexInternalShape;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

import javax.vecmath.Vector3f;

/**
 * ConeShape implements a cone shape primitive, centered around the origin and
 * aligned with the Y axis. The {@link ConeShapeX} is aligned around the X axis
 * and {@link ConeShapeZ} around the Z axis.
 *
 * @author jezek2
 */
public class ConeShape extends ConvexInternalShape {

    private final float sinAngle;
    private final float radius;
    private final float height;
    private final int[] coneIndices = new int[3];

    ConeShape(float radius, float height) {
        this.radius = radius;
        this.height = height;
        setConeUpIndex(1);
        sinAngle = (radius / (float) Math.sqrt(this.radius * this.radius + this.height * this.height));
    }

    public float getRadius() {
        return radius;
    }

    public float getHeight() {
        return height;
    }

    private Vector3f coneLocalSupport(Vector3f v, Vector3f out) {
        float halfHeight = height * 0.5f;

        if (VectorUtil.coord(v, coneIndices[1]) > v.length() * sinAngle) {
            VectorUtil.coord(out, coneIndices[0], 0.0f);
            VectorUtil.coord(out, coneIndices[1], halfHeight);
            VectorUtil.coord(out, coneIndices[2], 0.0f);
            return out;
        } else {
            float v0 = VectorUtil.coord(v, coneIndices[0]);
            float v2 = VectorUtil.coord(v, coneIndices[2]);
            float s = (float) Math.sqrt(v0 * v0 + v2 * v2);
            if (s > BulletGlobals.FLT_EPSILON) {
                float d = radius / s;
                VectorUtil.coord(out, coneIndices[0], VectorUtil.coord(v, coneIndices[0]) * d);
                VectorUtil.coord(out, coneIndices[1], -halfHeight);
                VectorUtil.coord(out, coneIndices[2], VectorUtil.coord(v, coneIndices[2]) * d);
            } else {
                VectorUtil.coord(out, coneIndices[0], 0.0f);
                VectorUtil.coord(out, coneIndices[1], -halfHeight);
                VectorUtil.coord(out, coneIndices[2], 0.0f);
            }
            return out;
        }
    }

    @Override
    public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out) {
        return coneLocalSupport(vec, out);
    }

    @Override
    public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors) {
        for (int i = 0; i < numVectors; i++) {
            Vector3f vec = vectors[i];
            coneLocalSupport(vec, supportVerticesOut[i]);
        }
    }

    @Override
    public Vector3f localGetSupportingVertex(Vector3f vec, Vector3f out) {
        Vector3f supVertex = coneLocalSupport(vec, out);
        if (getMargin() != 0.0f) {
            Vector3f vecnorm = new Vector3f(vec);
            if (vecnorm.lengthSquared() < (BulletGlobals.FLT_EPSILON * BulletGlobals.FLT_EPSILON)) {
                vecnorm.set(-1.0f, -1.0f, -1.0f);
            }
            vecnorm.normalize();
            supVertex.scaleAdd(getMargin(), vecnorm, supVertex);
        }
        return supVertex;
    }

    @Override
    public BroadphaseNativeType getShapeType() {
        return BroadphaseNativeType.CONE_SHAPE_PROXYTYPE;
    }

    @Override
    public void calculateLocalInertia(float mass, Vector3f inertia) {
        Transform identity = new Transform();
        identity.setIdentity();
        Vector3f aabbMin = new Vector3f(), aabbMax = new Vector3f();
        getAabb(identity, aabbMin, aabbMax);

        Vector3f halfExtents = new Vector3f();
        halfExtents.sub(aabbMax, aabbMin);
        halfExtents.scale(0.5f);

        float margin = getMargin();

        float lx = 2.0f * (halfExtents.x + margin);
        float ly = 2.0f * (halfExtents.y + margin);
        float lz = 2.0f * (halfExtents.z + margin);
        float x2 = lx * lx;
        float y2 = ly * ly;
        float z2 = lz * lz;
        float scaledmass = mass * 0.08333333f;

        inertia.set(y2 + z2, x2 + z2, x2 + y2);
        inertia.scale(scaledmass);

        //inertia.x() = scaledmass * (y2+z2);
        //inertia.y() = scaledmass * (x2+z2);
        //inertia.z() = scaledmass * (x2+y2);
    }

    @Override
    public String getName() {
        return "Cone";
    }

    public int getConeUpIndex() {
        return coneIndices[1];
    }

    // choose upAxis index
    void setConeUpIndex(int upIndex) {
        switch (upIndex) {
            case 0:
                coneIndices[0] = 1;
                coneIndices[1] = 0;
                coneIndices[2] = 2;
                break;

            case 1:
                coneIndices[0] = 0;
                coneIndices[1] = 1;
                coneIndices[2] = 2;
                break;

            case 2:
                coneIndices[0] = 0;
                coneIndices[1] = 2;
                coneIndices[2] = 1;
                break;

            default:
                assert (false);
        }
    }

    /**
     * ConeShape implements a cone shape, around the X axis.
     *
     * @author jezek2
     */
    private static class ConeShapeX extends ConeShape {

        ConeShapeX(float radius, float height) {
            super(radius, height);
            setConeUpIndex(0);
        }

    }

    /**
     * ConeShape implements a cone shape, around the Z axis.
     *
     * @author jezek2
     */
    private static class ConeShapeZ extends ConeShape {

        ConeShapeZ(float radius, float height) {
            super(radius, height);
            setConeUpIndex(2);
        }

    }
}