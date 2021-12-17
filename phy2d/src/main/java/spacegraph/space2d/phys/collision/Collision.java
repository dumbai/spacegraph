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
package spacegraph.space2d.phys.collision;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.Distance.SimplexCache;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.EdgeShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.collision.shapes.Shape;
import spacegraph.space2d.phys.common.Rot;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.common.Transform;
import spacegraph.space2d.phys.pooling.IWorldPool;

import java.util.stream.IntStream;

/**
 * Functions used for computing contact points, distance queries, and TOI queries. Collision methods
 * are non-static for pooling speed, retrieve a collision object from the {@link SingletonPool}.
 * Should not be finalructed.
 *
 * @author Daniel Murphy
 */
public class Collision {
    public static final int NULL_FEATURE = Integer.MAX_VALUE;

    private final IWorldPool pool;

    public Collision(IWorldPool argPool) {
        incidentEdge[0] = new ClipVertex();
        incidentEdge[1] = new ClipVertex();
        clipPoints1[0] = new ClipVertex();
        clipPoints1[1] = new ClipVertex();
        clipPoints2[0] = new ClipVertex();
        clipPoints2[1] = new ClipVertex();
        pool = argPool;
    }

    private final DistanceInput input = new DistanceInput();
    private final SimplexCache cache = new SimplexCache();
    private final DistanceOutput output = new DistanceOutput();

    /**
     * Determine if two generic shapes overlap.
     *
     * @param shapeA
     * @param shapeB
     * @param xfA
     * @param xfB
     * @return
     */
    public final boolean testOverlap(Shape shapeA, int indexA, Shape shapeB, int indexB,
                                     Transform xfA, Transform xfB) {
        input.proxyA.set(shapeA, indexA);
        input.proxyB.set(shapeB, indexB);
        input.transformA.set(xfA);
        input.transformB.set(xfB);
        input.useRadii = true;

        cache.count = 0;

        pool.getDistance().distance(output, cache, input);
        
        return output.distance < Settings.EPSILON;
    }

    /**
     * Compute the point states given two manifolds. The states pertain to the transition from
     * manifold1 to manifold2. So state1 is either persist or remove while state2 is either add or
     * persist.
     *
     * @param state1
     * @param state2
     * @param manifold1
     * @param manifold2
     */
    public static void getPointStates(PointState[] state1, PointState[] state2,
                                      Manifold manifold1, Manifold manifold2) {

        for (int i = 0; i < Settings.maxManifoldPoints; i++) {
            state1[i] = PointState.NULL_STATE;
            state2[i] = PointState.NULL_STATE;
        }

        
        for (int i = 0; i < manifold1.pointCount; i++) {
            ContactID id = manifold1.points[i].id;

            state1[i] = PointState.REMOVE_STATE;

            int bound = manifold2.pointCount;
            if (IntStream.range(0, bound).anyMatch(j -> manifold2.points[j].id.isEqual(id))) {
                state1[i] = PointState.PERSIST_STATE;
            }
        }

        
        for (int i = 0; i < manifold2.pointCount; i++) {
            ContactID id = manifold2.points[i].id;

            state2[i] = PointState.ADD_STATE;

            int bound = manifold1.pointCount;
            if (IntStream.range(0, bound).anyMatch(j -> manifold1.points[j].id.isEqual(id))) {
                state2[i] = PointState.PERSIST_STATE;
            }
        }
    }

    /**
     * Clipping for contact manifolds. Sutherland-Hodgman clipping.
     *
     * @param vOut
     * @param vIn
     * @param normal
     * @param offset
     * @return
     */
    private static int clipSegmentToLine(ClipVertex[] vOut, ClipVertex[] vIn,
                                         v2 normal, float offset, int vertexIndexA) {

        
        int numOut = 0;
        ClipVertex vIn0 = vIn[0];
        ClipVertex vIn1 = vIn[1];
        v2 vIn0v = vIn0.v;
        v2 vIn1v = vIn1.v;

        
        float distance0 = v2.dot(normal, vIn0v) - offset;
        float distance1 = v2.dot(normal, vIn1v) - offset;

        
        if (distance0 <= 0.0f) {
            vOut[numOut++].set(vIn0);
        }
        if (distance1 <= 0.0f) {
            vOut[numOut++].set(vIn1);
        }

        
        if (distance0 * distance1 < 0.0f) {
            
            float interp = distance0 / (distance0 - distance1);

            ClipVertex vOutNO = vOut[numOut];
            
            vOutNO.v.x = vIn0v.x + interp * (vIn1v.x - vIn0v.x);
            vOutNO.v.y = vIn0v.y + interp * (vIn1v.y - vIn0v.y);

            
            vOutNO.id.indexA = (byte) vertexIndexA;
            vOutNO.id.indexB = vIn0.id.indexB;
            vOutNO.id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
            vOutNO.id.typeB = (byte) ContactID.Type.FACE.ordinal();
            ++numOut;
        }

        return numOut;
    }

    

    
    private static final v2 d = new v2();

    /**
     * Compute the collision manifold between two circles.
     *
     * @param manifold
     * @param circle1
     * @param xfA
     * @param circle2
     * @param xfB
     */
    public static void collideCircles(Manifold manifold, CircleShape circle1,
                                      Transform xfA, CircleShape circle2, Transform xfB) {
        manifold.pointCount = 0;
        
        
        
        
        

        
        v2 circle1p = circle1.center;
        v2 circle2p = circle2.center;
        float pAx = (xfA.c * circle1p.x - xfA.s * circle1p.y) + xfA.pos.x;
        float pAy = (jcog.Util.fma(xfA.s, circle1p.x, xfA.c * circle1p.y)) + xfA.pos.y;
        float pBx = (xfB.c * circle2p.x - xfB.s * circle2p.y) + xfB.pos.x;
        float pBy = (jcog.Util.fma(xfB.s, circle2p.x, xfB.c * circle2p.y)) + xfB.pos.y;
        float dx = pBx - pAx;
        float dy = pBy - pAy;
        float distSqr = jcog.Util.fma(dx, dx, dy * dy);
        

        float radius = circle1.skinRadius + circle2.skinRadius;
        if (distSqr > radius * radius) {
            return;
        }

        manifold.type = ManifoldType.CIRCLES;
        manifold.localPoint.set(circle1p);
        manifold.localNormal.setZero();
        manifold.pointCount = 1;

        manifold.points[0].localPoint.set(circle2p);
        manifold.points[0].id.zero();
    }

    

    /**
     * Compute the collision manifold between a polygon and a circle.
     *
     * @param manifold
     * @param polygon
     * @param xfA
     * @param circle
     * @param xfB
     */
    public static void collidePolygonAndCircle(Manifold manifold, PolygonShape polygon,
                                               Transform xfA, CircleShape circle, Transform xfB) {
        manifold.pointCount = 0;
        

        
        
        
        
        
        
        
        v2 circlep = circle.center;
        Rot xfBq = xfB;
        Rot xfAq = xfA;
        float cx = (xfBq.c * circlep.x - xfBq.s * circlep.y) + xfB.pos.x;
        float cy = (jcog.Util.fma(xfBq.s, circlep.x, xfBq.c * circlep.y)) + xfB.pos.y;
        float px = cx - xfA.pos.x;
        float py = cy - xfA.pos.y;
        float cLocalx = (jcog.Util.fma(xfAq.c, px, xfAq.s * py));
        float cLocaly = (jcog.Util.fma(-xfAq.s, px, xfAq.c * py));
        

        
        int normalIndex = 0;
        float separation = -Float.MAX_VALUE;
        float radius = polygon.skinRadius + circle.skinRadius;
        int vertexCount = polygon.vertices;
        v2[] vertices = polygon.vertex;
        v2[] normals = polygon.normals;

        for (int i = 0; i < vertexCount; i++) {
            
            
            
            
            v2 vertex = vertices[i];
            float tempx = cLocalx - vertex.x;
            float tempy = cLocaly - vertex.y;
            float s = jcog.Util.fma(normals[i].x, tempx, normals[i].y * tempy);


            if (s > radius) {
                
                return;
            }

            if (s > separation) {
                separation = s;
                normalIndex = i;
            }
        }

        
        int vertIndex1 = normalIndex;
        int vertIndex2 = vertIndex1 + 1 < vertexCount ? vertIndex1 + 1 : 0;
        v2 v1 = vertices[vertIndex1];
        v2 v2 = vertices[vertIndex2];

        
        if (separation < Settings.EPSILON) {
            manifold.pointCount = 1;
            manifold.type = ManifoldType.FACE_A;

            
            
            
            
            
            jcog.math.v2 normal = normals[normalIndex];
            manifold.localNormal.x = normal.x;
            manifold.localNormal.y = normal.y;
            manifold.localPoint.x = (v1.x + v2.x) * 0.5f;
            manifold.localPoint.y = (v1.y + v2.y) * 0.5f;
            ManifoldPoint mpoint = manifold.points[0];
            mpoint.localPoint.x = circlep.x;
            mpoint.localPoint.y = circlep.y;
            mpoint.id.zero();
            

            return;
        }

        
        
        
        
        
        
        
        
        
        float tempX = cLocalx - v1.x;
        float tempY = cLocaly - v1.y;
        float temp2X = v2.x - v1.x;
        float temp2Y = v2.y - v1.y;
        float u1 = jcog.Util.fma(tempX, temp2X, tempY * temp2Y);

        float temp3X = cLocalx - v2.x;
        float temp3Y = cLocaly - v2.y;
        float temp4X = v1.x - v2.x;
        float temp4Y = v1.y - v2.y;
        float u2 = jcog.Util.fma(temp3X, temp4X, temp3Y * temp4Y);
        

        if (u1 <= 0.0f) {

            updateManifold(manifold, circlep, cLocalx, cLocaly, radius, v1);
        } else if (u2 <= 0.0f) {

            updateManifold(manifold, circlep, cLocalx, cLocaly, radius, v2);
        } else {
            
            
            
            
            
            
            
            
            
            
            
            float fcx = (v1.x + v2.x) * 0.5f;
            float fcy = (v1.y + v2.y) * 0.5f;

            float tx = cLocalx - fcx;
            float ty = cLocaly - fcy;
            jcog.math.v2 normal = normals[vertIndex1];
            separation = jcog.Util.fma(tx, normal.x, ty * normal.y);
            if (separation > radius) {
                return;
            }
            

            manifold.pointCount = 1;
            manifold.type = ManifoldType.FACE_A;
            manifold.localNormal.set(normals[vertIndex1]);
            manifold.localPoint.x = fcx; 
            manifold.localPoint.y = fcy;
            manifold.points[0].localPoint.set(circlep);
            manifold.points[0].id.zero();
        }
    }

    public static void updateManifold(Manifold manifold, v2 circlep, float cLocalx, float cLocaly, float radius, v2 v1) {
        float dx = cLocalx - v1.x;
        float dy = cLocaly - v1.y;
        if (jcog.Util.fma(dx, dx, dy * dy) > radius * radius) {
            return;
        }

        manifold.pointCount = 1;
        manifold.type = ManifoldType.FACE_A;


        manifold.localNormal.x = cLocalx - v1.x;
        manifold.localNormal.y = cLocaly - v1.y;

        manifold.localNormal.normalize();
        manifold.localPoint.set(v1);
        manifold.points[0].localPoint.set(circlep);
        manifold.points[0].id.zero();
    }


    private final v2 temp = new v2();
    private final Transform xf = new Transform();
    private final v2 n = new v2();
    private final v2 v1 = new v2();

    /**
     * Find the max separation between poly1 and poly2 using edge normals from poly1.
     *
     * @param edgeIndex
     * @param poly1
     * @param xf1
     * @param poly2
     * @param xf2
     * @return
     */
    private void findMaxSeparation(EdgeResults results, PolygonShape poly1,
                                   Transform xf1, PolygonShape poly2, Transform xf2) {
        int count1 = poly1.vertices;
        int count2 = poly2.vertices;
        v2[] n1s = poly1.normals;
        v2[] v1s = poly1.vertex;
        v2[] v2s = poly2.vertex;

        Transform.mulTransToOutUnsafe(xf2, xf1, xf);
        Rot xfq = xf;

        int bestIndex = 0;
        float maxSeparation = -Float.MAX_VALUE;
        for (int i = 0; i < count1; i++) {
            
            Rot.mulToOutUnsafe(xfq, n1s[i], n);
            Transform.mulToOutUnsafe(xf, v1s[i], v1);

            
            float si = Float.MAX_VALUE;
            for (int j = 0; j < count2; ++j) {
                v2 v2sj = v2s[j];
                float sij = jcog.Util.fma(n.x, (v2sj.x - v1.x), n.y * (v2sj.y - v1.y));
                if (sij < si) {
                    si = sij;
                }
            }

            if (si > maxSeparation) {
                maxSeparation = si;
                bestIndex = i;
            }
        }

        results.edgeIndex = bestIndex;
        results.separation = maxSeparation;
    }

    private static void findIncidentEdge(ClipVertex[] c, PolygonShape poly1,
                                         Transform xf1, int edge1, PolygonShape poly2, Transform xf2) {
        int count1 = poly1.vertices;
        v2[] normals1 = poly1.normals;

        int count2 = poly2.vertices;
        v2[] vertices2 = poly2.vertex;
        v2[] normals2 = poly2.normals;

        assert (0 <= edge1 && edge1 < count1);

        ClipVertex c0 = c[0];
        ClipVertex c1 = c[1];
        Rot xf1q = xf1;
        Rot xf2q = xf2;

        
        
        
        
        
        
        v2 v = normals1[edge1];
        float tempx = xf1q.c * v.x - xf1q.s * v.y;
        float tempy = jcog.Util.fma(xf1q.s, v.x, xf1q.c * v.y);
        float normal1x = jcog.Util.fma(xf2q.c, tempx, xf2q.s * tempy);
        float normal1y = jcog.Util.fma(-xf2q.s, tempx, xf2q.c * tempy);

        

        
        int index = 0;
        float minDot = Float.MAX_VALUE;
        for (int i = 0; i < count2; ++i) {
            v2 b = normals2[i];
            float dot = jcog.Util.fma(normal1x, b.x, normal1y * b.y);
            if (dot < minDot) {
                minDot = dot;
                index = i;
            }
        }

        
        int i1 = index;
        int i2 = i1 + 1 < count2 ? i1 + 1 : 0;

        
        v2 v1 = vertices2[i1];
        v2 out = c0.v;
        out.x = (xf2q.c * v1.x - xf2q.s * v1.y) + xf2.pos.x;
        out.y = (jcog.Util.fma(xf2q.s, v1.x, xf2q.c * v1.y)) + xf2.pos.y;
        c0.id.indexA = (byte) edge1;
        c0.id.indexB = (byte) i1;
        c0.id.typeA = (byte) ContactID.Type.FACE.ordinal();
        c0.id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

        
        v2 v2 = vertices2[i2];
        jcog.math.v2 out1 = c1.v;
        out1.x = (xf2q.c * v2.x - xf2q.s * v2.y) + xf2.pos.x;
        out1.y = (jcog.Util.fma(xf2q.s, v2.x, xf2q.c * v2.y)) + xf2.pos.y;
        c1.id.indexA = (byte) edge1;
        c1.id.indexB = (byte) i2;
        c1.id.typeA = (byte) ContactID.Type.FACE.ordinal();
        c1.id.typeB = (byte) ContactID.Type.VERTEX.ordinal();
    }

    private final EdgeResults results1 = new EdgeResults();
    private final EdgeResults results2 = new EdgeResults();
    private final ClipVertex[] incidentEdge = new ClipVertex[2];
    private final v2 localTangent = new v2();
    private final v2 localNormal = new v2();
    private final v2 planePoint = new v2();
    private final v2 tangent = new v2();
    private final v2 v11 = new v2();
    private final v2 v12 = new v2();
    private final ClipVertex[] clipPoints1 = new ClipVertex[2];
    private final ClipVertex[] clipPoints2 = new ClipVertex[2];

    /**
     * Compute the collision manifold between two polygons.
     *
     * @param manifold
     * @param polygon1
     * @param xf1
     * @param polygon2
     * @param xf2
     */
    public final void collidePolygons(Manifold manifold, PolygonShape polyA,
                                      Transform xfA, PolygonShape polyB, Transform xfB) {
        
        
        
        
        

        

        manifold.pointCount = 0;
        float totalRadius = polyA.skinRadius + polyB.skinRadius;

        findMaxSeparation(results1, polyA, xfA, polyB, xfB);
        if (results1.separation > totalRadius) {
            return;
        }

        findMaxSeparation(results2, polyB, xfB, polyA, xfA);
        if (results2.separation > totalRadius) {
            return;
        }

        PolygonShape poly1;
        PolygonShape poly2;
        Transform xf1, xf2;
        int edge1;                 
        boolean flip;
        final float k_tol = 0.1f * Settings.linearSlop;

        if (results2.separation > results1.separation + k_tol) {
            poly1 = polyB;
            poly2 = polyA;
            xf1 = xfB;
            xf2 = xfA;
            edge1 = results2.edgeIndex;
            manifold.type = ManifoldType.FACE_B;
            flip = true;
        } else {
            poly1 = polyA;
            poly2 = polyB;
            xf1 = xfA;
            xf2 = xfB;
            edge1 = results1.edgeIndex;
            manifold.type = ManifoldType.FACE_A;
            flip = false;
        }
        Rot xf1q = xf1;

        findIncidentEdge(incidentEdge, poly1, xf1, edge1, poly2, xf2);

        int count1 = poly1.vertices;
        v2[] vertices1 = poly1.vertex;

        int iv1 = edge1;
        int iv2 = edge1 + 1 < count1 ? edge1 + 1 : 0;
        v11.set(vertices1[iv1]);
        v12.set(vertices1[iv2]);
        localTangent.x = v12.x - v11.x;
        localTangent.y = v12.y - v11.y;
        localTangent.normalize();

        
        localNormal.x = 1.0f * localTangent.y;
        localNormal.y = -1.0f * localTangent.x;

        
        planePoint.x = (v11.x + v12.x) * 0.5f;
        planePoint.y = (v11.y + v12.y) * 0.5f;

        
        tangent.x = xf1q.c * localTangent.x - xf1q.s * localTangent.y;
        tangent.y = jcog.Util.fma(xf1q.s, localTangent.x, xf1q.c * localTangent.y);

        
        float normalx = 1.0f * tangent.y;
        float normaly = -1.0f * tangent.x;


        Transform.mulToOut(xf1, v11, v11);
        Transform.mulToOut(xf1, v12, v12);
        
        

        
        
        float frontOffset = jcog.Util.fma(normalx, v11.x, normaly * v11.y);

        
        
        
        float sideOffset1 = -(jcog.Util.fma(tangent.x, v11.x, tangent.y * v11.y)) + totalRadius;
        float sideOffset2 = tangent.x * v12.x + tangent.y * v12.y + totalRadius;


        tangent.negated();
        int np = clipSegmentToLine(clipPoints1, incidentEdge, tangent, sideOffset1, iv1);
        tangent.negated();

        if (np < 2) {
            return;
        }

        
        np = clipSegmentToLine(clipPoints2, clipPoints1, tangent, sideOffset2, iv2);

        if (np < 2) {
            return;
        }

        
        manifold.localNormal.set(localNormal);
        manifold.localPoint.set(planePoint);

        int pointCount = 0;
        for (int i = 0; i < Settings.maxManifoldPoints; ++i) {
            
            float separation = jcog.Util.fma(normalx, clipPoints2[i].v.x, normaly * clipPoints2[i].v.y) - frontOffset;

            if (separation <= totalRadius) {
                ManifoldPoint cp = manifold.points[pointCount];
                
                v2 out = cp.localPoint;
                float px = clipPoints2[i].v.x - xf2.pos.x;
                float py = clipPoints2[i].v.y - xf2.pos.y;
                out.x = (jcog.Util.fma(xf2.c, px, xf2.s * py));
                out.y = (jcog.Util.fma(-xf2.s, px, xf2.c * py));
                cp.id.set(clipPoints2[i].id);
                if (flip) {
                    
                    cp.id.flip();
                }
                ++pointCount;
            }
        }

        manifold.pointCount = pointCount;
    }

    private final v2 Q = new v2();
    private final v2 e = new v2();
    private final ContactID cf = new ContactID();
    private final v2 e1 = new v2();
    private final v2 P = new v2();

    
    
    public void collideEdgeAndCircle(Manifold manifold, EdgeShape edgeA, Transform xfA,
                                     CircleShape circleB, Transform xfB) {
        manifold.pointCount = 0;


        
        
        Transform.mulToOutUnsafe(xfB, circleB.center, temp);
        Transform.mulTransToOutUnsafe(xfA, temp, Q);

        v2 A = edgeA.m_vertex1;
        v2 B = edgeA.m_vertex2;
        e.set(B).subbed(A);

        
        float u = v2.dot(e, temp.set(B).subbed(Q));
        float v = v2.dot(e, temp.set(Q).subbed(A));

        float radius = edgeA.skinRadius + circleB.skinRadius;

        
        cf.indexB = 0;
        cf.typeB = (byte) ContactID.Type.VERTEX.ordinal();

        
        if (v <= 0.0f) {
            v2 P = A;
            d.set(Q).subbed(P);
            float dd = v2.dot(d, d);
            if (dd > radius * radius) {
                return;
            }

            
            if (edgeA.m_hasVertex0) {
                v2 A1 = edgeA.m_vertex0;
                v2 B1 = A;
                e1.set(B1).subbed(A1);
                float u1 = v2.dot(e1, temp.set(B1).subbed(Q));

                
                if (u1 > 0.0f) {
                    return;
                }
            }

            cf.indexA = 0;
            cf.typeA = (byte) ContactID.Type.VERTEX.ordinal();
            manifold.pointCount = 1;
            manifold.type = ManifoldType.CIRCLES;
            manifold.localNormal.setZero();
            manifold.localPoint.set(P);
            
            manifold.points[0].id.set(cf);
            manifold.points[0].localPoint.set(circleB.center);
            return;
        }

        
        if (u <= 0.0f) {
            v2 P = B;
            d.set(Q).subbed(P);
            float dd = v2.dot(d, d);
            if (dd > radius * radius) {
                return;
            }

            
            if (edgeA.m_hasVertex3) {
                v2 B2 = edgeA.m_vertex3;
                v2 A2 = B;
                v2 e2 = e1;
                e2.set(B2).subbed(A2);
                float v2 = jcog.math.v2.dot(e2, temp.set(Q).subbed(A2));

                
                if (v2 > 0.0f) {
                    return;
                }
            }

            cf.indexA = 1;
            cf.typeA = (byte) ContactID.Type.VERTEX.ordinal();
            manifold.pointCount = 1;
            manifold.type = ManifoldType.CIRCLES;
            manifold.localNormal.setZero();
            manifold.localPoint.set(P);
            
            manifold.points[0].id.set(cf);
            manifold.points[0].localPoint.set(circleB.center);
            return;
        }

        
        float den = v2.dot(e, e);
        assert (den > 0.0f);

        
        P.set(A).scaled(u).added(temp.set(B).scaled(v));
        P.scaled(1.0f / den);
        d.set(Q).subbed(P);
        float dd = v2.dot(d, d);
        if (dd > radius * radius) {
            return;
        }

        n.x = -e.y;
        n.y = e.x;
        if (v2.dot(n, temp.set(Q).subbed(A)) < 0.0f) {
            n.set(-n.x, -n.y);
        }
        n.normalize();

        cf.indexA = 0;
        cf.typeA = (byte) ContactID.Type.FACE.ordinal();
        manifold.pointCount = 1;
        manifold.type = ManifoldType.FACE_A;
        manifold.localNormal.set(n);
        manifold.localPoint.set(A);
        
        manifold.points[0].id.set(cf);
        manifold.points[0].localPoint.set(circleB.center);
    }

    private final EPCollider collider = new EPCollider();

    public void collideEdgeAndPolygon(Manifold manifold, EdgeShape edgeA, Transform xfA,
                                      PolygonShape polygonB, Transform xfB) {
        collider.collide(manifold, edgeA, xfA, polygonB, xfB);
    }


    /**
     * Java-specific class for returning edge results
     */
    private static class EdgeResults {
        float separation;
        int edgeIndex;
    }

    /**
     * Used for computing contact manifolds.
     */
    static class ClipVertex {
        final v2 v;
        final ContactID id;

        ClipVertex() {
            v = new v2();
            id = new ContactID();
        }

        void set(ClipVertex cv) {
            v2 v1 = cv.v;
            v.x = v1.x;
            v.y = v1.y;
            ContactID c = cv.id;
            id.indexA = c.indexA;
            id.indexB = c.indexB;
            id.typeA = c.typeA;
            id.typeB = c.typeB;
        }
    }

    /**
     * This is used for determining the state of contact points.
     *
     * @author Daniel Murphy
     */
    public enum PointState {
        /**
         * point does not exist
         */
        NULL_STATE,
        /**
         * point was added in the update
         */
        ADD_STATE,
        /**
         * point persisted across the update
         */
        PERSIST_STATE,
        /**
         * point was removed in the update
         */
        REMOVE_STATE
    }

    /**
     * This structure is used to keep track of the best separating axis.
     */
    static class EPAxis {
        enum Type {
            UNKNOWN, EDGE_A, EDGE_B
        }

        Type type;
        int index;
        float separation;
    }

    /**
     * This holds polygon B expressed in frame A.
     */
    static class TempPolygon {
        final v2[] vertices = new v2[Settings.maxPolygonVertices];
        final v2[] normals = new v2[Settings.maxPolygonVertices];
        int count;

        TempPolygon() {
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = new v2();
                normals[i] = new v2();
            }
        }
    }

    /**
     * Reference face used for clipping
     */
    static class ReferenceFace {
        int i1;
        int i2;
        final jcog.math.v2 v1 = new v2();
        final jcog.math.v2 v2 = new v2();
        final jcog.math.v2 normal = new v2();

        final jcog.math.v2 sideNormal1 = new v2();
        float sideOffset1;

        final jcog.math.v2 sideNormal2 = new v2();
        float sideOffset2;
    }

    /**
     * This class collides and edge and a polygon, taking into account edge adjacency.
     */
    static class EPCollider {
        enum VertexType {
            ISOLATED, CONCAVE, CONVEX
        }

        final TempPolygon m_polygonB = new TempPolygon();

        final Transform m_xf = new Transform();
        final v2 m_centroidB = new v2();
        v2 m_v0 = new v2();
        v2 m_v1 = new v2();
        v2 m_v2 = new v2();
        v2 m_v3 = new v2();
        final v2 m_normal0 = new v2();
        final v2 m_normal1 = new v2();
        final v2 m_normal2 = new v2();
        final v2 m_normal = new v2();

        VertexType m_type1;
        VertexType m_type2;

        final v2 m_lowerLimit = new v2();
        final v2 m_upperLimit = new v2();
        float m_radius;
        boolean m_front;

        EPCollider() {
            for (int i = 0; i < 2; i++) {
                ie[i] = new ClipVertex();
                clipPoints1[i] = new ClipVertex();
                clipPoints2[i] = new ClipVertex();
            }
        }

        private final v2 edge1 = new v2();
        private final v2 temp = new v2();
        private final v2 edge0 = new v2();
        private final v2 edge2 = new v2();
        private final ClipVertex[] ie = new ClipVertex[2];
        private final ClipVertex[] clipPoints1 = new ClipVertex[2];
        private final ClipVertex[] clipPoints2 = new ClipVertex[2];
        private final ReferenceFace rf = new ReferenceFace();
        private final EPAxis edgeAxis = new EPAxis();
        private final EPAxis polygonAxis = new EPAxis();

        void collide(Manifold manifold, EdgeShape edgeA, Transform xfA,
                     PolygonShape polygonB, Transform xfB) {

            Transform.mulTransToOutUnsafe(xfA, xfB, m_xf);
            Transform.mulToOutUnsafe(m_xf, polygonB.centroid, m_centroidB);

            m_v0 = edgeA.m_vertex0;
            m_v1 = edgeA.m_vertex1;
            m_v2 = edgeA.m_vertex2;
            m_v3 = edgeA.m_vertex3;

            boolean hasVertex0 = edgeA.m_hasVertex0;
            boolean hasVertex3 = edgeA.m_hasVertex3;

            edge1.set(m_v2).subbed(m_v1);
            edge1.normalize();
            m_normal1.set(edge1.y, -edge1.x);
            float offset1 = v2.dot(m_normal1, temp.set(m_centroidB).subbed(m_v1));
            float offset0 = 0.0f;
            boolean convex1 = false;


            if (hasVertex0) {
                edge0.set(m_v1).subbed(m_v0);
                edge0.normalize();
                m_normal0.set(edge0.y, -edge0.x);
                convex1 = v2.cross(edge0, edge1) >= 0.0f;
                offset0 = v2.dot(m_normal0, temp.set(m_centroidB).subbed(m_v0));
            }


            boolean convex2 = false;
            float offset2 = 0.0f;
            if (hasVertex3) {
                edge2.set(m_v3).subbed(m_v2);
                edge2.normalize();
                m_normal2.set(edge2.y, -edge2.x);
                convex2 = v2.cross(edge1, edge2) > 0.0f;
                offset2 = v2.dot(m_normal2, temp.set(m_centroidB).subbed(m_v2));
            }

            
            if (hasVertex0 && hasVertex3) {
                if (convex1 && convex2) {
                    m_front = offset0 >= 0.0f || offset1 >= 0.0f || offset2 >= 0.0f;
                    limit(m_normal0, m_normal2, m_normal1, m_normal1);
                } else if (convex1) {
                    m_front = offset0 >= 0.0f || (offset1 >= 0.0f && offset2 >= 0.0f);
                    limit(m_normal0, m_normal1, m_normal2, m_normal1);
                } else if (convex2) {
                    m_front = offset2 >= 0.0f || (offset0 >= 0.0f && offset1 >= 0.0f);
                    limit(m_normal1, m_normal2, m_normal1, m_normal0);
                } else {
                    m_front = offset0 >= 0.0f && offset1 >= 0.0f && offset2 >= 0.0f;
                    limit(m_normal1, m_normal1.x, m_normal1.y, -m_normal2.x, -m_normal2.y, m_normal0);
                }
            } else if (hasVertex0) {
                if (convex1) {
                    m_front = offset0 >= 0.0f || offset1 >= 0.0f;
                    limit(m_normal0, -m_normal1.x, -m_normal1.y, m_normal1.x, m_normal1.y, m_normal1);
                } else {
                    m_front = offset0 >= 0.0f && offset1 >= 0.0f;
                    limit(m_normal1, -m_normal1.x, -m_normal1.y, m_normal1.x, m_normal1.y, m_normal0);
                }
            } else if (hasVertex3) {
                if (convex2) {
                    m_front = offset1 >= 0.0f || offset2 >= 0.0f;
                    limit(m_normal2, m_normal1);
                } else {
                    m_front = offset1 >= 0.0f && offset2 >= 0.0f;
                    limit(m_normal1, m_normal2);
                }
            } else {
                m_front = offset1 >= 0.0f;
                if (m_front) {
                    m_normal.x = m_normal1.x;
                    m_normal.y = m_normal1.y;
                    m_lowerLimit.x = -m_normal1.x;
                    m_lowerLimit.y = -m_normal1.y;
                    m_upperLimit.x = -m_normal1.x;
                    m_upperLimit.y = -m_normal1.y;
                } else {
                    m_normal.x = -m_normal1.x;
                    m_normal.y = -m_normal1.y;
                    m_lowerLimit.x = m_normal1.x;
                    m_lowerLimit.y = m_normal1.y;
                    m_upperLimit.x = m_normal1.x;
                    m_upperLimit.y = m_normal1.y;
                }
            }

            
            m_polygonB.count = polygonB.vertices;
            for (int i = 0; i < polygonB.vertices; ++i) {
                Transform.mulToOutUnsafe(m_xf, polygonB.vertex[i], m_polygonB.vertices[i]);
                Rot.mulToOutUnsafe(m_xf, polygonB.normals[i], m_polygonB.normals[i]);
            }

            m_radius = 2.0f * Settings.polygonRadius;

            manifold.pointCount = 0;

            computeEdgeSeparation(edgeAxis);

            
            if (edgeAxis.type == EPAxis.Type.UNKNOWN) {
                return;
            }

            if (edgeAxis.separation > m_radius) {
                return;
            }

            computePolygonSeparation(polygonAxis);
            if (polygonAxis.type != EPAxis.Type.UNKNOWN && polygonAxis.separation > m_radius) {
                return;
            }

            
            final float k_relativeTol = 0.98f;
            final float k_absoluteTol = 0.001f;

            EPAxis primaryAxis;
            if (polygonAxis.type == EPAxis.Type.UNKNOWN) {
                primaryAxis = edgeAxis;
            } else if (polygonAxis.separation > jcog.Util.fma(k_relativeTol, edgeAxis.separation, k_absoluteTol)) {
                primaryAxis = polygonAxis;
            } else {
                primaryAxis = edgeAxis;
            }

            ClipVertex ie0 = ie[0];
            ClipVertex ie1 = ie[1];

            if (primaryAxis.type == EPAxis.Type.EDGE_A) {
                manifold.type = ManifoldType.FACE_A;

                
                int bestIndex = 0;
                float bestValue = v2.dot(m_normal, m_polygonB.normals[0]);
                for (int i = 1; i < m_polygonB.count; ++i) {
                    float value = v2.dot(m_normal, m_polygonB.normals[i]);
                    if (value < bestValue) {
                        bestValue = value;
                        bestIndex = i;
                    }
                }

                int i1 = bestIndex;
                int i2 = i1 + 1 < m_polygonB.count ? i1 + 1 : 0;

                ie0.v.set(m_polygonB.vertices[i1]);
                ie0.id.indexA = 0;
                ie0.id.indexB = (byte) i1;
                ie0.id.typeA = (byte) ContactID.Type.FACE.ordinal();
                ie0.id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

                ie1.v.set(m_polygonB.vertices[i2]);
                ie1.id.indexA = 0;
                ie1.id.indexB = (byte) i2;
                ie1.id.typeA = (byte) ContactID.Type.FACE.ordinal();
                ie1.id.typeB = (byte) ContactID.Type.VERTEX.ordinal();

                if (m_front) {
                    rf.i1 = 0;
                    rf.i2 = 1;
                    rf.v1.set(m_v1);
                    rf.v2.set(m_v2);
                    rf.normal.set(m_normal1);
                } else {
                    rf.i1 = 1;
                    rf.i2 = 0;
                    rf.v1.set(m_v2);
                    rf.v2.set(m_v1);
                    rf.normal.set(m_normal1).negated();
                }
            } else {
                manifold.type = ManifoldType.FACE_B;

                ie0.v.set(m_v1);
                ie0.id.indexA = 0;
                ie0.id.indexB = (byte) primaryAxis.index;
                ie0.id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
                ie0.id.typeB = (byte) ContactID.Type.FACE.ordinal();

                ie1.v.set(m_v2);
                ie1.id.indexA = 0;
                ie1.id.indexB = (byte) primaryAxis.index;
                ie1.id.typeA = (byte) ContactID.Type.VERTEX.ordinal();
                ie1.id.typeB = (byte) ContactID.Type.FACE.ordinal();

                rf.i1 = primaryAxis.index;
                rf.i2 = rf.i1 + 1 < m_polygonB.count ? rf.i1 + 1 : 0;
                rf.v1.set(m_polygonB.vertices[rf.i1]);
                rf.v2.set(m_polygonB.vertices[rf.i2]);
                rf.normal.set(m_polygonB.normals[rf.i1]);
            }

            rf.sideNormal1.set(rf.normal.y, -rf.normal.x);
            rf.sideNormal2.set(rf.sideNormal1).negated();
            rf.sideOffset1 = v2.dot(rf.sideNormal1, rf.v1);
            rf.sideOffset2 = v2.dot(rf.sideNormal2, rf.v2);


            int np = clipSegmentToLine(clipPoints1, ie, rf.sideNormal1, rf.sideOffset1, rf.i1);

            if (np < Settings.maxManifoldPoints) {
                return;
            }

            
            np = clipSegmentToLine(clipPoints2, clipPoints1, rf.sideNormal2, rf.sideOffset2, rf.i2);

            if (np < Settings.maxManifoldPoints) {
                return;
            }

            
            if (primaryAxis.type == EPAxis.Type.EDGE_A) {
                manifold.localNormal.set(rf.normal);
                manifold.localPoint.set(rf.v1);
            } else {
                manifold.localNormal.set(polygonB.normals[rf.i1]);
                manifold.localPoint.set(polygonB.vertex[rf.i1]);
            }

            int pointCount = 0;
            for (int i = 0; i < Settings.maxManifoldPoints; ++i) {

                float separation = v2.dot(rf.normal, temp.set(clipPoints2[i].v).subbed(rf.v1));

                if (separation <= m_radius) {
                    ManifoldPoint cp = manifold.points[pointCount];

                    if (primaryAxis.type == EPAxis.Type.EDGE_A) {
                        
                        Transform.mulTransToOutUnsafe(m_xf, clipPoints2[i].v, cp.localPoint);
                        cp.id.set(clipPoints2[i].id);
                    } else {
                        cp.localPoint.set(clipPoints2[i].v);
                        cp.id.typeA = clipPoints2[i].id.typeB;
                        cp.id.typeB = clipPoints2[i].id.typeA;
                        cp.id.indexA = clipPoints2[i].id.indexB;
                        cp.id.indexB = clipPoints2[i].id.indexA;
                    }

                    ++pointCount;
                }
            }

            manifold.pointCount = pointCount;
        }

        public void limit(v2 m_normal2, v2 m_normal1) {
            if (m_front) {
                m_normal.x = m_normal1.x;
                m_normal.y = m_normal1.y;
                m_lowerLimit.x = -m_normal1.x;
                m_lowerLimit.y = -m_normal1.y;
                m_upperLimit.x = m_normal2.x;
                m_upperLimit.y = m_normal2.y;
            } else {
                m_normal.x = -m_normal1.x;
                m_normal.y = -m_normal1.y;
                m_lowerLimit.x = -m_normal1.x;
                m_lowerLimit.y = -m_normal1.y;
                m_upperLimit.x = m_normal1.x;
                m_upperLimit.y = m_normal1.y;
            }
        }

        public void limit(v2 m_normal0, float v, float v2, float x, float y, v2 m_normal1) {
            if (m_front) {
                m_normal.x = m_normal1.x;
                m_normal.y = m_normal1.y;
                m_lowerLimit.x = m_normal0.x;
                m_lowerLimit.y = m_normal0.y;
                m_upperLimit.x = v;
                m_upperLimit.y = v2;
            } else {
                m_normal.x = -m_normal1.x;
                m_normal.y = -m_normal1.y;
                m_lowerLimit.x = x;
                m_lowerLimit.y = y;
                m_upperLimit.x = -m_normal1.x;
                m_upperLimit.y = -m_normal1.y;
            }
        }

        public void limit(v2 m_normal0, v2 m_normal2, v2 m_normal1, v2 m_normal12) {
            if (m_front) {
                m_normal.x = m_normal1.x;
                m_normal.y = m_normal1.y;
                m_lowerLimit.x = m_normal0.x;
                m_lowerLimit.y = m_normal0.y;
                m_upperLimit.x = m_normal2.x;
                m_upperLimit.y = m_normal2.y;
            } else {
                m_normal.x = -m_normal1.x;
                m_normal.y = -m_normal1.y;
                m_lowerLimit.x = -m_normal1.x;
                m_lowerLimit.y = -m_normal1.y;
                m_upperLimit.x = -m_normal12.x;
                m_upperLimit.y = -m_normal12.y;
            }
        }


        void computeEdgeSeparation(EPAxis axis) {
            axis.type = EPAxis.Type.EDGE_A;
            axis.index = m_front ? 0 : 1;
            axis.separation = Float.MAX_VALUE;
            float nx = m_normal.x;
            float ny = m_normal.y;

            for (int i = 0; i < m_polygonB.count; ++i) {
                v2 v = m_polygonB.vertices[i];
                float tempx = v.x - m_v1.x;
                float tempy = v.y - m_v1.y;
                float s = jcog.Util.fma(nx, tempx, ny * tempy);
                if (s < axis.separation) {
                    axis.separation = s;
                }
            }
        }

        private final v2 perp = new v2();
        private final v2 n = new v2();

        void computePolygonSeparation(EPAxis axis) {
            axis.type = EPAxis.Type.UNKNOWN;
            axis.index = -1;
            axis.separation = -Float.MAX_VALUE;

            perp.x = -m_normal.y;
            perp.y = m_normal.x;

            for (int i = 0; i < m_polygonB.count; ++i) {
                v2 normalB = m_polygonB.normals[i];
                v2 vB = m_polygonB.vertices[i];
                n.x = -normalB.x;
                n.y = -normalB.y;

                
                
                float tempx = vB.x - m_v1.x;
                float tempy = vB.y - m_v1.y;
                float s1 = jcog.Util.fma(n.x, tempx, n.y * tempy);
                tempx = vB.x - m_v2.x;
                tempy = vB.y - m_v2.y;
                float s2 = jcog.Util.fma(n.x, tempx, n.y * tempy);
                float s = Math.min(s1, s2);

                if (s > m_radius) {
                    
                    axis.type = EPAxis.Type.EDGE_B;
                    axis.index = i;
                    axis.separation = s;
                    return;
                }

                
                if (jcog.Util.fma(n.x, perp.x, n.y * perp.y) >= 0.0f) {
                    if (v2.dot(temp.set(n).subbed(m_upperLimit), m_normal) < -Settings.angularSlop) {
                        continue;
                    }
                } else {
                    if (v2.dot(temp.set(n).subbed(m_lowerLimit), m_normal) < -Settings.angularSlop) {
                        continue;
                    }
                }

                if (s > axis.separation) {
                    axis.type = EPAxis.Type.EDGE_B;
                    axis.index = i;
                    axis.separation = s;
                }
            }
        }
    }
}