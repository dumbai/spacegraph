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

import java.util.stream.IntStream;

/**
 * A convex polygon shape. Polygons have a maximum number of vertices equal to _maxPolygonVertices.
 * In most cases you should not need many vertices for a convex polygon.
 */
public class PolygonShape extends Shape {
    /**
     * Dump lots of debug information.
     */
    private static final boolean m_debug = false;

    /**
     * Local position of the shape centroid in parent body frame.
     */
    public final v2 centroid = new v2();

    /**
     * vertices in local coordinates.
     * The vertices of the shape. Note: use getVertexCount(), not m_vertices.length, to get number of
     * active vertices.
     */
    public final v2[] vertex;

    /**
     * The normals of the shape. Note: use getVertexCount(), not m_normals.length, to get number of
     * active normals.
     */
    public final v2[] normals;

    /**
     * Number of active vertices in the shape.
     */
    public int vertices;

    private final Transform poolt1 = new Transform();

    public PolygonShape() {
        this(Settings.maxPolygonVertices);
    }

    public PolygonShape(int maxVertices) {
        super(ShapeType.POLYGON);

        vertices = 0;
        vertex = new v2[maxVertices];
        for (int i = 0; i < vertex.length; i++) {
            vertex[i] = new v2(0, 0);
        }
        normals = new v2[maxVertices];
        for (int i = 0; i < normals.length; i++) {
            normals[i] = new v2(0, 0);
        }
        setSkinRadius(Settings.polygonRadius);
        centroid.setZero();
    }

    public PolygonShape(float... xy) {
        this(xy.length / 2);
        v2[] t = new v2[xy.length / 2];
        int j = 0;
        for (int i = 0; i < xy.length; i += 2) {
            t[j++] = new v2(xy[i], xy[i + 1]);
        }
        set(t, xy.length / 2);
    }

    public final Shape clone() {
        PolygonShape shape = new PolygonShape(vertex.length);
        shape.centroid.set(this.centroid);
        for (int i = 0; i < shape.normals.length; i++) {
            shape.normals[i].set(normals[i]);
            shape.vertex[i].set(vertex[i]);
        }
        shape.setSkinRadius(this.getSkinRadius());
        shape.vertices = this.vertices;
        return shape;
    }

    /**
     * Create a convex hull from the given array of points. The count must be in the range [3,
     * Settings.maxPolygonVertices]. This method takes an arraypool for pooling.
     *
     * @param verts
     * @param num
     * @warning the points may be re-ordered, even if they form a convex polygon.
     * @warning collinear points are removed.
     */
    public final PolygonShape set(v2[] verts, int num) {
        assert (3 <= num && num <= Settings.maxPolygonVertices);

        v2 pool1 = new v2(), pool2 = new v2();

        int i0 = 0;
        float x0 = verts[0].x;
        for (int i = 1; i < num; ++i) {
            float x = verts[i].x;
            if (x > x0 || (x == x0 && verts[i].y < verts[i0].y)) {
                i0 = i;
                x0 = x;
            }
        }

        int[] hull = new int[Settings.maxPolygonVertices];
        int m = 0;
        int ih = i0;

        while (true) {
            hull[m] = ih;

            int ie = 0;
            for (int j = 1; j < num; ++j) {
                if (ie == ih) {
                    ie = j;
                    continue;
                }

                v2 r = pool1.set(verts[ie]).subbed(verts[hull[m]]);
                v2 v = pool2.set(verts[j]).subbed(verts[hull[m]]);
                float c = v2.cross(r, v);
                if (c < 0.0f) {
                    ie = j;
                }


                if (c == 0.0f && v.lengthSquaredDouble() > r.lengthSquaredDouble()) {
                    ie = j;
                }
            }

            ++m;
            ih = ie;

            if (ie == i0)
                break;
        }

        this.vertices = m;


        for (int i = 0; i < vertices; ++i) {
            if (vertex[i] == null)
                vertex[i] = new v2();
            vertex[i].set(verts[hull[i]]);
        }

        v2 edge = pool1;
        for (int i = 0; i < vertices; ++i) {
            int i1 = i;
            int i2 = i + 1 < vertices ? i + 1 : 0;
            edge.set(vertex[i2]).subbed(vertex[i1]);

            //assert (edge.lengthSquared() > Settings.EPSILONsqr);
            v2.crossToOutUnsafe(edge, 1.0f, normals[i]);
            normals[i].normalize();
        }


        computeCentroidToOut(vertex, vertices, centroid);

        return this;
    }

    public static PolygonShape box(float hx, float hy) {

        return new PolygonShape(4).setAsBox(hx, hy);
    }

    public static PolygonShape box(float x1, float y1, float x2, float y2) {
        return new PolygonShape(4).setAsBox(x1, y1, x2, y2);
    }

    /**
     * Build vertices to represent an axis-aligned box.
     *
     * @param hx the half-width.
     * @param hy the half-height.
     */
    public final PolygonShape setAsBox(float hx, float hy) {
        vertices = 4;
        vertex[0].set(-hx, -hy);
        vertex[1].set(hx, -hy);
        vertex[2].set(hx, hy);
        vertex[3].set(-hx, hy);
        normals[0].set(0.0f, -1.0f);
        normals[1].set(1.0f, 0.0f);
        normals[2].set(0.0f, 1.0f);
        normals[3].set(-1.0f, 0.0f);
        centroid.setZero();
        return this;
    }


    public static PolygonShape regular(int n, float r) {
        PolygonShape p = new PolygonShape(n);
        p.vertices = n;
        for (int i = 0; i < n; i++) {
            double theta = i / (float) n * 2 * Math.PI;
            p.vertex[i].set(r * Math.cos(theta), r * Math.sin(theta));
        }
        p.centroid.setZero();
        p.set(p.vertex, n);
        return p;
    }

    /**
     * Build vertices to represent an oriented box.
     *
     * @param hx     the half-width.
     * @param hy     the half-height.
     * @param center the center of the box in local coordinates.
     * @param angle  the rotation of the box in local coordinates.
     */
    public final PolygonShape setAsBox(float hx, float hy, v2 center, float angle) {
        vertices = 4;
        vertex[0].set(-hx, -hy);
        vertex[1].set(hx, -hy);
        vertex[2].set(hx, hy);
        vertex[3].set(-hx, hy);
        normals[0].set(0.0f, -1.0f);
        normals[1].set(1.0f, 0.0f);
        normals[2].set(0.0f, 1.0f);
        normals[3].set(-1.0f, 0.0f);
        centroid.set(center);

        Transform xf = poolt1;
        xf.pos.set(center);
        xf.set(angle);


        for (int i = 0; i < vertices; ++i) {
            Transform.mulToOut(xf, vertex[i], vertex[i]);
            Rot.mulToOut(xf, normals[i], normals[i]);
        }
        return this;
    }

    private PolygonShape setAsBox(float x1, float y1, float x2, float y2) {
        vertices = 4;
        vertex[0].set(x1, y1);
        vertex[1].set(x2, y1);
        vertex[2].set(x2, y2);
        vertex[3].set(x1, y2);
        normals[0].set(0.0f, -1.0f);
        normals[1].set(1.0f, 0.0f);
        normals[2].set(0.0f, 1.0f);
        normals[3].set(-1.0f, 0.0f);

        v2 center = new v2((x1 + x2) / 2, (y1 + y2) / 2);
        centroid.set(center);

        Transform xf = poolt1;
        xf.pos.set(center);
        xf.set(0);


        for (int i = 0; i < vertices; ++i) {
            Transform.mulToOut(xf, vertex[i], vertex[i]);
            Rot.mulToOut(xf, normals[i], normals[i]);
        }
        return this;
    }

    public int getChildCount() {
        return 1;
    }

    @Override
    public final boolean testPoint(Transform xf, v2 p) {
        Rot xfq = xf;

        float tempx = p.x - xf.pos.x;
        float tempy = p.y - xf.pos.y;
        float pLocalx = xfq.c * tempx + xfq.s * tempy;
        float pLocaly = -xfq.s * tempx + xfq.c * tempy;

        if (m_debug) {
            System.out.println("--testPoint debug--");
            System.out.println("Vertices: ");
            for (int i = 0; i < vertices; ++i) {
                System.out.println(vertex[i]);
            }
            System.out.println("pLocal: " + pLocalx + ", " + pLocaly);
        }

        for (int i = 0; i < vertices; ++i) {
            v2 vertex = this.vertex[i];
            v2 normal = normals[i];
            tempx = pLocalx - vertex.x;
            tempy = pLocaly - vertex.y;
            float dot = normal.x * tempx + normal.y * tempy;
            if (dot > 0.0f) {
                return false;
            }
        }

        return true;
    }

    @Override
    public final void computeAABB(AABB aabb, Transform xf, int childIndex) {
        v2 lower = aabb.lowerBound;
        v2 upper = aabb.upperBound;
        v2 v1 = vertex[0];
        float xfqc = xf.c;
        float xfqs = xf.s;
        float xfpx = xf.pos.x;
        float xfpy = xf.pos.y;
        lower.x = (xfqc * v1.x - xfqs * v1.y) + xfpx;
        lower.y = (xfqs * v1.x + xfqc * v1.y) + xfpy;
        upper.x = lower.x;
        upper.y = lower.y;

        for (int i = 1; i < vertices; ++i) {
            v2 v2 = vertex[i];

            float vx = (xfqc * v2.x - xfqs * v2.y) + xfpx;
            float vy = (xfqs * v2.x + xfqc * v2.y) + xfpy;
            lower.x = Math.min(lower.x, vx);
            lower.y = Math.min(lower.y, vy);
            upper.x = Math.max(upper.x, vx);
            upper.y = Math.max(upper.y, vy);
        }

        lower.x -= skinRadius;
        lower.y -= skinRadius;
        upper.x += skinRadius;
        upper.y += skinRadius;
    }

    /**
     * Get the vertex count.
     *
     * @return
     */
    public final int getVertexCount() {
        return vertices;
    }

    /**
     * Get a vertex by index.
     *
     * @param index
     * @return
     */
    public final v2 getVertex(int index) {
        assert (0 <= index && index < vertices);
        return vertex[index];
    }

    @Override
    public float distance(Transform xf, v2 p, int childIndex, v2 normalOut) {
        float xfqc = xf.c;
        float xfqs = xf.s;
        float tx = p.x - xf.pos.x;
        float ty = p.y - xf.pos.y;
        float pLocalx = xfqc * tx + xfqs * ty;
        float pLocaly = -xfqs * tx + xfqc * ty;

        float maxDistance = -Float.MAX_VALUE;
        float normalForMaxDistanceX = pLocalx;
        float normalForMaxDistanceY = pLocaly;

        for (int i = 0; i < vertices; ++i) {
            v2 vertex = this.vertex[i];
            v2 normal = normals[i];
            tx = pLocalx - vertex.x;
            ty = pLocaly - vertex.y;
            float dot = normal.x * tx + normal.y * ty;
            if (dot > maxDistance) {
                maxDistance = dot;
                normalForMaxDistanceX = normal.x;
                normalForMaxDistanceY = normal.y;
            }
        }

        float distance;
        if (maxDistance > 0) {
            float minDistanceX = normalForMaxDistanceX;
            float minDistanceY = normalForMaxDistanceY;
            float minDistance2 = maxDistance * maxDistance;
            for (int i = 0; i < vertices; ++i) {
                v2 vertex = this.vertex[i];
                float distanceVecX = pLocalx - vertex.x;
                float distanceVecY = pLocaly - vertex.y;
                float distance2 = (distanceVecX * distanceVecX + distanceVecY * distanceVecY);
                if (minDistance2 > distance2) {
                    minDistanceX = distanceVecX;
                    minDistanceY = distanceVecY;
                    minDistance2 = distance2;
                }
            }
            distance = (float) Math.sqrt(minDistance2);
            normalOut.x = xfqc * minDistanceX - xfqs * minDistanceY;
            normalOut.y = xfqs * minDistanceX + xfqc * minDistanceY;
            normalOut.normalize();
        } else {
            distance = maxDistance;
            normalOut.x = xfqc * normalForMaxDistanceX - xfqs * normalForMaxDistanceY;
            normalOut.y = xfqs * normalForMaxDistanceX + xfqc * normalForMaxDistanceY;
        }

        return distance;
    }

    @Override
    public final boolean raycast(RayCastOutput output, RayCastInput input, Transform xf,
                                 int childIndex) {
        float xfqc = xf.c;
        float xfqs = xf.s;
        v2 xfp = xf.pos;


        float tempx = input.p1.x - xfp.x;
        float tempy = input.p1.y - xfp.y;
        float p1x = xfqc * tempx + xfqs * tempy;
        float p1y = -xfqs * tempx + xfqc * tempy;

        tempx = input.p2.x - xfp.x;
        tempy = input.p2.y - xfp.y;
        float p2x = xfqc * tempx + xfqs * tempy;
        float p2y = -xfqs * tempx + xfqc * tempy;

        float dx = p2x - p1x;
        float dy = p2y - p1y;

        float lower = 0, upper = input.maxFraction;

        int index = -1;

        for (int i = 0; i < vertices; ++i) {
            v2 normal = normals[i];
            v2 vertex = this.vertex[i];


            float tempxn = vertex.x - p1x;
            float tempyn = vertex.y - p1y;
            float numerator = normal.x * tempxn + normal.y * tempyn;
            float denominator = normal.x * dx + normal.y * dy;

            if (denominator == 0.0f) {
                if (numerator < 0.0f) {
                    return false;
                }
            } else {


                if (denominator < 0.0f && numerator < lower * denominator) {


                    lower = numerator / denominator;
                    index = i;
                } else if (denominator > 0.0f && numerator < upper * denominator) {


                    upper = numerator / denominator;
                }
            }

            if (upper < lower) {
                return false;
            }
        }

        assert (0.0f <= lower && lower <= input.maxFraction);

        if (index >= 0) {
            output.fraction = lower;

            v2 normal = normals[index];
            v2 out = output.normal;
            out.x = xfqc * normal.x - xfqs * normal.y;
            out.y = xfqs * normal.x + xfqc * normal.y;
            return true;
        }
        return false;
    }

    private static void computeCentroidToOut(v2[] vs, int count, v2 out) {
        assert (count >= 3);

        out.set(0.0f, 0.0f);

        v2 pool1 = new v2(), pool2 = new v2(), pool3 = new v2();

        v2 pRef = pool1;
        pRef.setZero();

        v2 e1 = pool2;
        v2 e2 = pool3;

        final float inv3 = 1.0f / 3.0f;

        float area = 0.0f;
        for (int i = 0; i < count; ++i) {

            v2 p1 = pRef;
            v2 p2 = vs[i];
            v2 p3 = vs[i + 1 < count ? i + 1 : 0];

            e1.set(p2).subbed(p1);
            e2.set(p3).subbed(p1);

            float D = v2.cross(e1, e2);

            float triangleArea = 0.5f * D;
            area += triangleArea;


            e1.set(p1).added(p2).added(p3).scaled(triangleArea * inv3);
            out.added(e1);
        }


        if (area > Settings.EPSILON) {
            out.scaled(1.0f / area);
        } else {
            out.setZero();
        }
    }

    public void computeMass(MassData massData, float density) {

        v2 pool1 = new v2(), pool2 = new v2(), pool3 = new v2(), pool4 = new v2();

        assert (vertices >= 3);

        v2 center = pool1;
        center.setZero();


        v2 s = pool2;
        s.setZero();

        for (int i = 0; i < vertices; ++i) {
            s.added(vertex[i]);
        }
        s.scaled(1.0f / vertices);

        final float k_inv3 = 1.0f / 3.0f;

        v2 e1 = pool3;
        v2 e2 = pool4;

        float I = 0.0f;
        float area = 0.0f;
        for (int i = 0; i < vertices; ++i) {

            e1.set(vertex[i]).subbed(s);
            e2.set(s).negated().added(vertex[i + 1 < vertices ? i + 1 : 0]);

            float D = v2.cross(e1, e2);

            float triangleArea = 0.5f * D;
            area += triangleArea;


            center.x += triangleArea * k_inv3 * (e1.x + e2.x);
            center.y += triangleArea * k_inv3 * (e1.y + e2.y);

            float ex1 = e1.x, ey1 = e1.y;
            float ex2 = e2.x, ey2 = e2.y;

            float intx2 = ex1 * ex1 + ex2 * ex1 + ex2 * ex2;
            float inty2 = ey1 * ey1 + ey2 * ey1 + ey2 * ey2;

            I += (0.25f * k_inv3 * D) * (intx2 + inty2);
        }


        area = Math.max(Settings.EPSILONsqr, area); //prevent div by zero
        massData.mass = density * area;


        center.scaled(1.0f / area);
        massData.center.set(center).added(s);


        massData.I = I * density + (massData.mass * v2.dot(massData.center, massData.center));
    }

    /**
     * Validate convexity. This is a very time consuming operation.
     *
     * @return
     */
    public boolean validate() {

        v2 pool1 = new v2(), pool2 = new v2();

        for (int i = 0; i < vertices; ++i) {
            int i1 = i;
            int i2 = i < vertices - 1 ? i1 + 1 : 0;
            v2 p = vertex[i1];

            v2 e = pool1.set(vertex[i2]).subbed(p);

            int bound = vertices;
            if (IntStream.range(0, bound).filter(j -> j != i1 && j != i2).anyMatch(j -> v2.cross(e, pool2.set(vertex[j]).subbed(p)) < 0.0f)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the centroid and apply the supplied transform.
     */
    public v2 centroid(Transform xf) {
        return Transform.mul(xf, centroid);
    }

    /**
     * Get the centroid and apply the supplied transform.
     */
    public v2 centroidToOut(Transform xf, v2 out) {
        Transform.mulToOutUnsafe(xf, centroid, out);
        return out;
    }
}