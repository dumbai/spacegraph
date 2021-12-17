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

package com.bulletphysics.util.bvh.optimized;

import com.bulletphysics.linearmath.VectorUtil;

import javax.vecmath.Vector3f;
import java.io.Serializable;

/**
 * OptimizedBvhNode contains both internal and leaf node information.
 *
 * @author jezek2
 */
class OptimizedBvhNode implements Serializable {

    /** aabb min/max */
    public final Vector3f min = new Vector3f(), max = new Vector3f();

    public int escapeIndex;

    public int part;
    public int i;

    OptimizedBvhNode() {
        min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }

    OptimizedBvhNode(Vector3f[] points, int partId, int triangleIndex) {
        this();
        for (Vector3f p : points) {
            VectorUtil.setMin(min, p);
            VectorUtil.setMax(max, p);
        }
        escapeIndex = -1;
        part = partId;
        i = triangleIndex;
    }

    public void set(OptimizedBvhNode n) {
        min.set(n.min);
        max.set(n.max);
        escapeIndex = n.escapeIndex;
        part = n.part;
        i = n.i;
    }

}