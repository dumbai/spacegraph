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
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

import javax.vecmath.Vector3f;

/**
 * CylinderShape class implements a cylinder shape primitive, centered around
 * the origin. Its central axis aligned with the Y axis. {@link CylinderShapeX}
 * is aligned with the X axis and {@link CylinderShapeZ} around the Z axis.
 *
 * @author jezek2
 */
public class CylinderShape extends BoxShape {

    int upAxis;

    public CylinderShape(Vector3f halfExtents) {
        super(halfExtents);
        upAxis = 1;
        recalcLocalAabb();
    }

    CylinderShape(Vector3f halfExtents, boolean unused) {
        super(halfExtents);
    }

    static Vector3f cylinderLocalSupportX(Vector3f halfExtents, Vector3f v, Vector3f out) {
        return cylinderLocalSupport(halfExtents, v, 0, 1, 0, 2, out);
    }

    private static Vector3f cylinderLocalSupportY(Vector3f halfExtents, Vector3f v, Vector3f out) {
        return cylinderLocalSupport(halfExtents, v, 1, 0, 1, 2, out);
    }

    static Vector3f cylinderLocalSupportZ(Vector3f halfExtents, Vector3f v, Vector3f out) {
        return cylinderLocalSupport(halfExtents, v, 2, 0, 2, 1, out);
    }

    private static Vector3f cylinderLocalSupport(Vector3f halfExtents, Vector3f v, int cylinderUpAxis, int XX, int YY, int ZZ, Vector3f out) {
        //mapping depends on how cylinder local orientation is
        // extents of the cylinder is: X,Y is for radius, and Z for height

        float radius = VectorUtil.coord(halfExtents, XX);
        float halfHeight = VectorUtil.coord(halfExtents, cylinderUpAxis);

        float d;

        float s = (float) Math.sqrt(VectorUtil.coord(v, XX) * VectorUtil.coord(v, XX) + VectorUtil.coord(v, ZZ) * VectorUtil.coord(v, ZZ));
        if (s != 0.0f) {
            d = radius / s;
            VectorUtil.coord(out, XX, VectorUtil.coord(v, XX) * d);
            VectorUtil.coord(out, YY, VectorUtil.coord(v, YY) < 0.0f ? -halfHeight : halfHeight);
            VectorUtil.coord(out, ZZ, VectorUtil.coord(v, ZZ) * d);
        } else {
            VectorUtil.coord(out, XX, radius);
            VectorUtil.coord(out, YY, VectorUtil.coord(v, YY) < 0.0f ? -halfHeight : halfHeight);
            VectorUtil.coord(out, ZZ, 0.0f);
        }
        return out;
    }

    @Override
    public void getAabb(Transform t, Vector3f aabbMin, Vector3f aabbMax) {
        _PolyhedralConvexShape_getAabb(t, aabbMin, aabbMax);
    }

    @Override
    public Vector3f localGetSupportingVertexWithoutMargin(Vector3f vec, Vector3f out) {
        return cylinderLocalSupportY(getHalfExtentsWithoutMargin(new Vector3f()), vec, out);
    }

    @Override
    public void batchedUnitVectorGetSupportingVertexWithoutMargin(Vector3f[] vectors, Vector3f[] supportVerticesOut, int numVectors) {
        for (int i = 0; i < numVectors; i++) {
            cylinderLocalSupportY(getHalfExtentsWithoutMargin(new Vector3f()), vectors[i], supportVerticesOut[i]);
        }
    }

    @Override
    public Vector3f localGetSupportingVertex(Vector3f vec, Vector3f out) {
        localGetSupportingVertexWithoutMargin(vec, out);

        if (getMargin() != 0.0f) {
            Vector3f vecnorm = new Vector3f(vec);
            if (vecnorm.lengthSquared() < (BulletGlobals.SIMD_EPSILON * BulletGlobals.SIMD_EPSILON)) {
                vecnorm.set(-1.0f, -1.0f, -1.0f);
            }
            vecnorm.normalize();
            out.scaleAdd(getMargin(), vecnorm, out);
        }
        return out;
    }

    @Override
    public BroadphaseNativeType getShapeType() {
        return BroadphaseNativeType.CYLINDER_SHAPE_PROXYTYPE;
    }

    public int getUpAxis() {
        return upAxis;
    }

    public float getRadius() {
        return getHalfExtentsWithMargin(new Vector3f()).x;
    }

    @Override
    public String getName() {
        return "CylinderY";
    }

}