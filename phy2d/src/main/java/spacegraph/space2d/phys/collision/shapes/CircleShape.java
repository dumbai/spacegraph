/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package spacegraph.space2d.phys.collision.shapes;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.AABB;
import spacegraph.space2d.phys.collision.RayCastInput;
import spacegraph.space2d.phys.collision.RayCastOutput;
import spacegraph.space2d.phys.common.Rot;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.common.Transform;

/**
 * A circle shape.
 */
public class CircleShape extends Shape {

    public final v2 center;

    public CircleShape() {
        super(ShapeType.CIRCLE);
        center = new v2();
        skinRadius = 0;
    }

    public CircleShape(float radius) {
        this();
        this.skinRadius = radius;

    }

    public final Shape clone() {
        CircleShape shape = new CircleShape();
        shape.center.x = center.x;
        shape.center.y = center.y;
        shape.skinRadius = skinRadius;
        return shape;
    }

    public final int getChildCount() {
        return 1;
    }

    /**
     * Get the supporting vertex index in the given direction.
     *
     * @param d
     * @return
     */
    public static int getSupport(v2 d) {
        return 0;
    }

    /**
     * Get the supporting vertex in the given direction.
     *
     * @param d
     * @return
     */
    public final v2 getSupportVertex(v2 d) {
        return center;
    }

    /**
     * Get the vertex count.
     *
     * @return
     */
    public static int getVertexCount() {
        return 1;
    }

    /**
     * Get a vertex by index.
     *
     * @param index
     * @return
     */
    public final v2 getVertex(int index) {
        assert (index == 0);
        return center;
    }

    @Override
    public final boolean testPoint(Transform transform, v2 p) {
        
        
        
        
        
        Rot q = transform;
        v2 tp = transform.pos;
        float centerx = -(q.c * center.x - q.s * center.y + tp.x - p.x);
        float centery = -(q.s * center.x + q.c * center.y + tp.y - p.y);

        return centerx * centerx + centery * centery <= skinRadius * skinRadius;
    }

    @Override
    public float distance(Transform xf, v2 p, int childIndex, v2 normalOut) {
        Rot xfq = xf;
        float centerx = xfq.c * center.x - xfq.s * center.y + xf.pos.x;
        float centery = xfq.s * center.x + xfq.c * center.y + xf.pos.y;
        float dx = p.x - centerx;
        float dy = p.y - centery;
        float d1 = (float) Math.sqrt(dx * dx + dy * dy);
        normalOut.x = dx * 1 / d1;
        normalOut.y = dy * 1 / d1;
        return d1 - skinRadius;
    }

    
    
    
    
    @Override
    public final boolean raycast(RayCastOutput output, RayCastInput input, Transform transform,
                                 int childIndex) {

        v2 inputp1 = input.p1;
        v2 inputp2 = input.p2;
        Rot tq = transform;
        v2 tp = transform.pos;

        
        
        float positionx = tq.c * center.x - tq.s * center.y + tp.x;
        float positiony = tq.s * center.x + tq.c * center.y + tp.y;

        float sx = inputp1.x - positionx;
        float sy = inputp1.y - positiony;
        
        float b = sx * sx + sy * sy - skinRadius * skinRadius;

        
        float rx = inputp2.x - inputp1.x;
        float ry = inputp2.y - inputp1.y;
        
        
        float c = sx * rx + sy * ry;
        float rr = rx * rx + ry * ry;
        float sigma = c * c - rr * b;

        
        if (sigma < 0.0f || rr < Settings.EPSILON) {
            return false;
        }

        
        float a = -(c + (float) Math.sqrt(sigma));

        
        if (0.0f <= a && a <= input.maxFraction * rr) {
            a /= rr;
            output.fraction = a;
            output.normal.x = rx * a + sx;
            output.normal.y = ry * a + sy;
            output.normal.normalize();
            return true;
        }

        return false;
    }

    @Override
    public final void computeAABB(AABB aabb, Transform transform, int childIndex) {
        Rot tq = transform;
        v2 tp = transform.pos;
        float px = tq.c * center.x - tq.s * center.y + tp.x;
        float py = tq.s * center.x + tq.c * center.y + tp.y;

        aabb.lowerBound.x = px - skinRadius;
        aabb.lowerBound.y = py - skinRadius;
        aabb.upperBound.x = px + skinRadius;
        aabb.upperBound.y = py + skinRadius;
    }

    @Override
    public final void computeMass(MassData massData, float density) {
        massData.mass = density * Settings.PI * skinRadius * skinRadius;
        massData.center.x = center.x;
        massData.center.y = center.y;

        
        
        massData.I = massData.mass * (0.5f * skinRadius * skinRadius + (center.x * center.x + center.y * center.y));
    }
}
