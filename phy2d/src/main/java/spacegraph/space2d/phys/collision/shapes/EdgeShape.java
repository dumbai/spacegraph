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
 * A line segment (edge) shape. These can be connected in chains or loops to other edge shapes. The
 * connectivity information is used to ensure correct contact normals.
 *
 * @author Daniel
 */
public class EdgeShape extends Shape {

    /**
     * edge vertex 1
     */
    public final v2 m_vertex1 = new v2();
    /**
     * edge vertex 2
     */
    public final v2 m_vertex2 = new v2();

    /**
     * optional adjacent vertex 1. Used for smooth collision
     */
    public final v2 m_vertex0 = new v2();
    /**
     * optional adjacent vertex 2. Used for smooth collision
     */
    public final v2 m_vertex3 = new v2();
    public boolean m_hasVertex0 = false;
    public boolean m_hasVertex3 = false;


    public EdgeShape() {
        super(ShapeType.EDGE);
        skinRadius = Settings.polygonRadius;
    }

    public EdgeShape(float x1, float y1, float x2, float y2) {
        this();
        set(new v2(x1, y1), new v2(x2, y2));
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    public void set(v2 v1, v2 v2) {
        m_vertex1.set(v1);
        m_vertex2.set(v2);
        m_hasVertex0 = m_hasVertex3 = false;
    }

    @Override
    public boolean testPoint(Transform xf, v2 p) {
        return false;
    }

    
    private final v2 normal = new v2();

    @Override
    public float distance(Transform xf, v2 p, int childIndex, v2 normalOut) {
        float xfqc = xf.c;
        float xfqs = xf.s;
        float xfpx = xf.pos.x;
        float xfpy = xf.pos.y;
        float v1x = (xfqc * m_vertex1.x - xfqs * m_vertex1.y) + xfpx;
        float v1y = (xfqs * m_vertex1.x + xfqc * m_vertex1.y) + xfpy;
        float v2x = (xfqc * m_vertex2.x - xfqs * m_vertex2.y) + xfpx;
        float v2y = (xfqs * m_vertex2.x + xfqc * m_vertex2.y) + xfpy;

        float dx = p.x - v1x;
        float dy = p.y - v1y;
        float sx = v2x - v1x;
        float sy = v2y - v1y;
        float ds = dx * sx + dy * sy;
        if (ds > 0) {
            float s2 = sx * sx + sy * sy;
            if (ds > s2) {
                dx = p.x - v2x;
                dy = p.y - v2y;
            } else {
                dx -= ds / s2 * sx;
                dy -= ds / s2 * sy;
            }
        }

        float d1 = (float) Math.sqrt(dx * dx + dy * dy);
        if (d1 > 0) {
            normalOut.x = 1 / d1 * dx;
            normalOut.y = 1 / d1 * dy;
        } else {
            normalOut.x = 0;
            normalOut.y = 0;
        }
        return d1;
    }

    
    
    
    
    @Override
    public boolean raycast(RayCastOutput output, RayCastInput input, Transform xf, int childIndex) {

        v2 v1 = m_vertex1;
        v2 v2 = m_vertex2;
        Rot xfq = xf;
        jcog.math.v2 xfp = xf.pos;


        float tempx = input.p1.x - xfp.x;
        float tempy = input.p1.y - xfp.y;
        float p1x = xfq.c * tempx + xfq.s * tempy;
        float p1y = -xfq.s * tempx + xfq.c * tempy;

        tempx = input.p2.x - xfp.x;
        tempy = input.p2.y - xfp.y;
        float p2x = xfq.c * tempx + xfq.s * tempy;
        float p2y = -xfq.s * tempx + xfq.c * tempy;


        normal.x = v2.y - v1.y;
        normal.y = v1.x - v2.x;
        normal.normalize();
        float normalx = normal.x;
        float normaly = normal.y;

        
        
        
        tempx = v1.x - p1x;
        tempy = v1.y - p1y;
        float numerator = normalx * tempx + normaly * tempy;
        float dy = p2y - p1y;
        float dx = p2x - p1x;
        float denominator = normalx * dx + normaly * dy;

        if (denominator == 0.0f) {
            return false;
        }

        float t = numerator / denominator;
        if (t < 0.0f || 1.0f < t) {
            return false;
        }


        float rx = v2.x - v1.x;
        float ry = v2.y - v1.y;
        float rr = rx * rx + ry * ry;
        if (rr == 0.0f) {
            return false;
        }
        float qx = p1x + t * dx;
        tempx = qx - v1.x;
        float qy = p1y + t * dy;
        tempy = qy - v1.y;
        
        float s = (tempx * rx + tempy * ry) / rr;
        if (s < 0.0f || 1.0f < s) {
            return false;
        }

        output.fraction = t;
        if (numerator > 0.0f) {
            
            output.normal.x = -xfq.c * normal.x + xfq.s * normal.y;
            output.normal.y = -xfq.s * normal.x - xfq.c * normal.y;
        } else {
            
            output.normal.x = xfq.c * normal.x - xfq.s * normal.y;
            output.normal.y = xfq.s * normal.x + xfq.c * normal.y;
        }
        return true;
    }

    @Override
    public void computeAABB(AABB aabb, Transform xf, int childIndex) {
        v2 lowerBound = aabb.lowerBound;
        v2 upperBound = aabb.upperBound;
        Rot xfq = xf;

        float v1x = (xfq.c * m_vertex1.x - xfq.s * m_vertex1.y) + xf.pos.x;
        float v1y = (xfq.s * m_vertex1.x + xfq.c * m_vertex1.y) + xf.pos.y;
        float v2x = (xfq.c * m_vertex2.x - xfq.s * m_vertex2.y) + xf.pos.x;
        float v2y = (xfq.s * m_vertex2.x + xfq.c * m_vertex2.y) + xf.pos.y;

        lowerBound.x = Math.min(v1x, v2x);
        lowerBound.y = Math.min(v1y, v2y);
        upperBound.x = Math.max(v1x, v2x);
        upperBound.y = Math.max(v1y, v2y);

        lowerBound.x -= skinRadius;
        lowerBound.y -= skinRadius;
        upperBound.x += skinRadius;
        upperBound.y += skinRadius;
    }

    @Override
    public void computeMass(MassData massData, float density) {
        massData.mass = 0.0f;
        massData.center.set(m_vertex1).added(m_vertex2).scaled(0.5f);
        massData.I = 0.0f;
    }

    @Override
    public Shape clone() {
        EdgeShape edge = new EdgeShape();
        edge.skinRadius = this.skinRadius;
        edge.m_hasVertex0 = this.m_hasVertex0;
        edge.m_hasVertex3 = this.m_hasVertex3;
        edge.m_vertex0.set(this.m_vertex0);
        edge.m_vertex1.set(this.m_vertex1);
        edge.m_vertex2.set(this.m_vertex2);
        edge.m_vertex3.set(this.m_vertex3);
        return edge;
    }
}
