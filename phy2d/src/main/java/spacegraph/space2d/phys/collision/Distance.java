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
import spacegraph.space2d.phys.collision.shapes.*;
import spacegraph.space2d.phys.common.Rot;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.common.Transform;

import java.util.stream.IntStream;


/**
 * This is non-static for faster pooling. To get an instance, use the {@link SingletonPool}, don't
 * construct a distance object.
 *
 * @author Daniel Murphy
 */
public class Distance {
    private static final int MAX_ITERS = 20;

    private static int GJK_CALLS = 0;
    private static int GJK_ITERS = 0;
    private static int GJK_MAX_ITERS = 20;

    /**
     * GJK using Voronoi regions (Christer Ericson) and Barycentric coordinates.
     */
    private static class SimplexVertex {
        final v2 wA = new v2();
        final v2 wB = new v2();
        final v2 w = new v2();
        float a;
        int indexA;
        int indexB;

        void set(SimplexVertex sv) {
            wA.set(sv.wA);
            wB.set(sv.wB);
            w.set(sv.w);
            a = sv.a;
            indexA = sv.indexA;
            indexB = sv.indexB;
        }
    }

    /**
     * Used to warm start Distance. Set count to zero on first call.
     *
     * @author daniel
     */
    public static class SimplexCache {
        /**
         * length or area
         */
        float metric;
        public int count;
        /**
         * vertices on shape A
         */
        public final int[] indexA = new int[3];
        /**
         * vertices on shape B
         */
        public final int[] indexB = new int[3];

        public SimplexCache() {
            metric = 0;
            count = 0;
            indexA[0] = Integer.MAX_VALUE;
            indexA[1] = Integer.MAX_VALUE;
            indexA[2] = Integer.MAX_VALUE;
            indexB[0] = Integer.MAX_VALUE;
            indexB[1] = Integer.MAX_VALUE;
            indexB[2] = Integer.MAX_VALUE;
        }

        public void set(SimplexCache sc) {
            System.arraycopy(sc.indexA, 0, indexA, 0, indexA.length);
            System.arraycopy(sc.indexB, 0, indexB, 0, indexB.length);
            metric = sc.metric;
            count = sc.count;
        }
    }

    private static class Simplex {
        final SimplexVertex m_v1 = new SimplexVertex();
        final SimplexVertex m_v2 = new SimplexVertex();
        final SimplexVertex m_v3 = new SimplexVertex();
        final SimplexVertex[] vertices = {m_v1, m_v2, m_v3};
        int m_count;

        void readCache(SimplexCache cache, DistanceProxy proxyA, Transform transformA,
                       DistanceProxy proxyB, Transform transformB) {
            assert (cache.count <= 3);

            
            m_count = cache.count;

            for (int i = 0; i < m_count; ++i) {
                SimplexVertex v = vertices[i];
                v.indexA = cache.indexA[i];
                v.indexB = cache.indexB[i];
                v2 wALocal = proxyA.vertex(v.indexA);
                v2 wBLocal = proxyB.vertex(v.indexB);
                Transform.mulToOutUnsafe(transformA, wALocal, v.wA);
                Transform.mulToOutUnsafe(transformB, wBLocal, v.wB);
                v.w.set(v.wB).subbed(v.wA);
                v.a = 0.0f;
            }

            
            
            if (m_count > 1) {
                float metric1 = cache.metric;
                float metric2 = metric();
                if (metric2 < 0.5f * metric1 || 2.0f * metric1 < metric2 || metric2 < Settings.EPSILON) {
                    
                    m_count = 0;
                }
            }

            
            if (m_count == 0) {
                SimplexVertex v = vertices[0];
                v.indexA = 0;
                v.indexB = 0;
                v2 wALocal = proxyA.vertex(0);
                v2 wBLocal = proxyB.vertex(0);
                Transform.mulToOutUnsafe(transformA, wALocal, v.wA);
                Transform.mulToOutUnsafe(transformB, wBLocal, v.wB);
                v.w.set(v.wB).subbed(v.wA);
                m_count = 1;
            }
        }

        void writeCache(SimplexCache cache) {
            cache.metric = metric();
            cache.count = m_count;

            for (int i = 0; i < m_count; ++i) {
                SimplexVertex vi = vertices[i];
                cache.indexA[i] = vi.indexA;
                cache.indexB[i] = vi.indexB;
            }
        }

        private final v2 e12 = new v2();

        final void getSearchDirection(v2 out) {
            switch (m_count) {
                case 1 -> {
                    out.set(m_v1.w).negated();
                    return;
                }
                case 2 -> {
                    e12.set(m_v2.w).subbed(m_v1.w);
                    out.set(m_v1.w).negated();
                    float sgn = v2.cross(e12, out);
                    if (sgn > 0.0f) {

                        v2.crossToOutUnsafe(1.0f, e12, out);
                    } else {

                        v2.crossToOutUnsafe(e12, 1.0f, out);
                    }
                    return;
                }
                default -> {
                    assert (false);
                    out.setZero();
                }
            }
        }

        
        private final v2 case2 = new v2();
        private final v2 case22 = new v2();

        /**
         * this returns pooled objects. don't keep or modify them
         *
         * @return
         */
        void closestPoint(v2 out) {
            switch (m_count) {
                case 0 -> {
                    assert (false);
                    out.setZero();
                    return;
                }
                case 1 -> {
                    out.set(m_v1.w);
                    return;
                }
                case 2 -> {
                    case22.set(m_v2.w).scaled(m_v2.a);
                    case2.set(m_v1.w).scaled(m_v1.a).added(case22);
                    out.set(case2);
                    return;
                }
                case 3 -> {
                    out.setZero();
                    return;
                }
                default -> {
                    assert (false);
                    out.setZero();
                }
            }
        }

        
        private final v2 case3 = new v2();
        private final v2 case33 = new v2();

        void witnessPoints(v2 pA, v2 pB) {
            switch (m_count) {

                case 1:
                    pA.set(m_v1.wA);
                    pB.set(m_v1.wB);
                    break;

                case 2:
                    case2.set(m_v1.wA).scaled(m_v1.a);
                    pA.set(m_v2.wA).scaled(m_v2.a).added(case2);
                    
                    
                    case2.set(m_v1.wB).scaled(m_v1.a);
                    pB.set(m_v2.wB).scaled(m_v2.a).added(case2);

                    break;

                case 3:
                    pA.set(m_v1.wA).scaled(m_v1.a);
                    case3.set(m_v2.wA).scaled(m_v2.a);
                    case33.set(m_v3.wA).scaled(m_v3.a);
                    pA.added(case3).added(case33);
                    pB.set(pA);
                    
                    
                    break;
                case 0:

                default:
                    assert (false);
                    break;
            }
        }

        
        float metric() {
            switch (m_count) {

                case 1:
                    return 0.0f;

                case 2:
                    return m_v1.w.distance(m_v2.w);

                case 3:
                    case3.set(m_v2.w).subbed(m_v1.w);
                    case33.set(m_v3.w).subbed(m_v1.w);
                    
                    return v2.cross(case3, case33);
                case 0:

                default:
                    assert (false);
                    return 0.0f;
            }
        }

        

        /**
         * Solve a line segment using barycentric coordinates.
         */
        void solve2() {
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            v2 w1 = m_v1.w;
            v2 w2 = m_v2.w;
            e12.set(w2).subbed(w1);

            
            float d12_2 = -v2.dot(w1, e12);
            if (d12_2 <= 0.0f) {
                
                m_v1.a = 1.0f;
                m_count = 1;
                return;
            }

            
            float d12_1 = v2.dot(w2, e12);
            if (d12_1 <= 0.0f) {
                
                m_v2.a = 1.0f;
                m_count = 1;
                m_v1.set(m_v2);
                return;
            }

            
            float inv_d12 = 1.0f / (d12_1 + d12_2);
            m_v1.a = d12_1 * inv_d12;
            m_v2.a = d12_2 * inv_d12;
            m_count = 2;
        }

        
        private final v2 e13 = new v2();
        private final v2 e23 = new v2();
        private final v2 w1 = new v2();
        private final v2 w2 = new v2();
        private final v2 w3 = new v2();

        /**
         * Solve a line segment using barycentric coordinates.<br/>
         * Possible regions:<br/>
         * - points[2]<br/>
         * - edge points[0]-points[2]<br/>
         * - edge points[1]-points[2]<br/>
         * - inside the triangle
         */
        void solve3() {
            w1.set(m_v1.w);
            w2.set(m_v2.w);
            w3.set(m_v3.w);

            
            
            
            
            e12.set(w2).subbed(w1);
            float w1e12 = v2.dot(w1, e12);
            float w2e12 = v2.dot(w2, e12);
            float d12_2 = -w1e12;

            
            
            
            
            e13.set(w3).subbed(w1);
            float w1e13 = v2.dot(w1, e13);
            float w3e13 = v2.dot(w3, e13);
            float d13_2 = -w1e13;

            
            
            
            
            e23.set(w3).subbed(w2);
            float w2e23 = v2.dot(w2, e23);
            float w3e23 = v2.dot(w3, e23);
            float d23_2 = -w2e23;

            
            float n123 = v2.cross(e12, e13);

            float d123_1 = n123 * v2.cross(w2, w3);
            float d123_2 = n123 * v2.cross(w3, w1);
            float d123_3 = n123 * v2.cross(w1, w2);

            
            if (d12_2 <= 0.0f && d13_2 <= 0.0f) {
                m_v1.a = 1;
                m_count = 1;
                return;
            }


            float d12_1 = w2e12;
            if (d12_1 > 0.0f && d12_2 > 0.0f && d123_3 <= 0.0f) {
                float inv_d12 = 1 / (d12_1 + d12_2);
                m_v1.a = d12_1 * inv_d12;
                m_v2.a = d12_2 * inv_d12;
                m_count = 2;
                return;
            }


            float d13_1 = w3e13;
            if (d13_1 > 0.0f && d13_2 > 0.0f && d123_2 <= 0.0f) {
                float inv_d13 = 1 / (d13_1 + d13_2);
                m_v1.a = d13_1 * inv_d13;
                m_v3.a = d13_2 * inv_d13;
                m_count = 2;
                m_v2.set(m_v3);
                return;
            }

            
            if (d12_1 <= 0.0f && d23_2 <= 0.0f) {
                m_v2.a = 1;
                m_count = 1;
                m_v1.set(m_v2);
                return;
            }


            float d23_1 = w3e23;
            if (d13_1 <= 0.0f && d23_1 <= 0.0f) {
                m_v3.a = 1;
                m_count = 1;
                m_v1.set(m_v3);
                return;
            }

            
            if (d23_1 > 0.0f && d23_2 > 0.0f && d123_1 <= 0.0f) {
                float inv_d23 = 1 / (d23_1 + d23_2);
                m_v2.a = d23_1 * inv_d23;
                m_v3.a = d23_2 * inv_d23;
                m_count = 2;
                m_v1.set(m_v3);
                return;
            }

            
            float inv_d123 = 1 / (d123_1 + d123_2 + d123_3);
            m_v1.a = d123_1 * inv_d123;
            m_v2.a = d123_2 * inv_d123;
            m_v3.a = d123_3 * inv_d123;
            m_count = 3;
        }
    }

    /**
     * A distance proxy is used by the GJK algorithm. It encapsulates any shape. TODO: see if we can
     * just do assignments with m_vertices, instead of copying stuff over
     *
     * @author daniel
     */
    public static class DistanceProxy {
        final v2[] m_vertices;
        int m_count;
        public float m_radius;
        final v2[] m_buffer;

        public DistanceProxy() {
            m_vertices = new v2[Settings.maxPolygonVertices];
            for (int i = 0; i < m_vertices.length; i++)
                m_vertices[i] = new v2();
            m_buffer = new v2[2];
            m_count = 0;
            m_radius = 0.0f;
        }

        /**
         * Initialize the proxy using the given shape. The shape must remain in scope while the proxy is
         * in use.
         */
        public final void set(Shape shape, int index) {
            switch (shape.getType()) {
                case CIRCLE:
                    CircleShape circle = (CircleShape) shape;
                    m_vertices[0].set(circle.center);
                    m_count = 1;
                    m_radius = circle.skinRadius;

                    break;
                case POLYGON:
                    PolygonShape poly = (PolygonShape) shape;
                    m_count = poly.vertices;
                    m_radius = poly.skinRadius;
                    for (int i = 0; i < m_count; i++)
                        m_vertices[i].set(poly.vertex[i]);

                    break;
                case CHAIN:
                    ChainShape chain = (ChainShape) shape;
                    assert (0 <= index && index < chain.m_count);

                    m_buffer[0] = chain.m_vertices[index];
                    m_buffer[1] = chain.m_vertices[index + 1 < chain.m_count ? index + 1 : 0];

                    m_vertices[0].set(m_buffer[0]);
                    m_vertices[1].set(m_buffer[1]);
                    m_count = 2;
                    m_radius = chain.skinRadius;
                    break;
                case EDGE:
                    EdgeShape edge = (EdgeShape) shape;
                    m_vertices[0].set(edge.m_vertex1);
                    m_vertices[1].set(edge.m_vertex2);
                    m_count = 2;
                    m_radius = edge.skinRadius;
                    break;
                default:
                    assert (false);
            }
        }

        /**
         * Get the supporting vertex index in the given direction.
         *
         * @param d
         * @return
         */
        public final int support(v2 d) {
            int bestIndex = -1;
            float bestValue = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < m_count; i++) {
                float value = v2.dot(m_vertices[i], d);
                if (value > bestValue) {
                    bestIndex = i;
                    bestValue = value;
                }
            }

            return bestIndex;
        }

//        /**
//         * Get the supporting vertex in the given direction.
//         *
//         * @param d
//         * @return
//         */
//        public final v2 getSupportVertex(v2 d) {
//            int bestIndex = 0;
//            float bestValue = v2.dot(m_vertices[0], d);
//            for (int i = 1; i < m_count; i++) {
//                float value = v2.dot(m_vertices[i], d);
//                if (value > bestValue) {
//                    bestIndex = i;
//                    bestValue = value;
//                }
//            }
//
//            return m_vertices[bestIndex];
//        }

        /**
         * Get the vertex count.
         *
         * @return
         */
        public final int getVertexCount() {
            return m_count;
        }

        /**
         * Get a vertex by index. Used by Distance.
         *
         * @param index
         * @return
         */
        public final v2 vertex(int index) {
            assert (0 <= index && index < m_count);
            return m_vertices[index];
        }
    }

    private final Simplex simplex = new Simplex();
    private final int[] saveA = new int[3];
    private final int[] saveB = new int[3];
    private final v2 closestPoint = new v2();
    private final v2 d = new v2();
    private final v2 temp = new v2();
    private final v2 normal = new v2();

    public final void distance(DistanceOutput output, DistanceInput input) {
        distance(output, new SimplexCache(), input);
    }

    /**
     * Compute the closest points between two shapes. Supports any combination of: CircleShape and
     * PolygonShape. The simplex cache is input/output. On the first call set SimplexCache.count to
     * zero.
     *
     * @param output
     * @param cache
     * @param input
     */
    public final void distance(DistanceOutput output, SimplexCache cache,
                               DistanceInput input) {
        GJK_CALLS++;

        DistanceProxy proxyA = input.proxyA;
        DistanceProxy proxyB = input.proxyB;

        Transform transformA = input.transformA;
        Transform transformB = input.transformB;

        
        simplex.readCache(cache, proxyA, transformA, proxyB, transformB);

        
        SimplexVertex[] vertices = simplex.vertices;


        simplex.closestPoint(closestPoint);
        float distanceSqr1 = closestPoint.lengthSquared();
        float distanceSqr2 = distanceSqr1;

        
        int iter = 0;
        int saveCount = 0;
        while (iter < MAX_ITERS) {

            
            saveCount = simplex.m_count;
            for (int i = 0; i < saveCount; i++) {
                saveA[i] = vertices[i].indexA;
                saveB[i] = vertices[i].indexB;
            }

            switch (simplex.m_count) {
                case 1:
                    break;
                case 2:
                    simplex.solve2();
                    break;
                case 3:
                    simplex.solve3();
                    break;
                default:
                    assert (false);
            }

            
            if (simplex.m_count == 3) {
                break;
            }

            
            simplex.closestPoint(closestPoint);
            distanceSqr2 = closestPoint.lengthSquared();

            
            if (distanceSqr2 >= distanceSqr1) {
                
            }
            distanceSqr1 = distanceSqr2;

            
            simplex.getSearchDirection(d);

            
            if (d.lengthSquared() < Settings.EPSILON * Settings.EPSILON) {
                
                

                
                
                
                break;
            }
            /*
             * SimplexVertex* vertex = vertices + simplex.m_count; vertex.indexA =
             * proxyA.GetSupport(MulT(transformA.R, -d)); vertex.wA = Mul(transformA,
             * proxyA.GetVertex(vertex.indexA)); Vec2 wBLocal; vertex.indexB =
             * proxyB.GetSupport(MulT(transformB.R, d)); vertex.wB = Mul(transformB,
             * proxyB.GetVertex(vertex.indexB)); vertex.w = vertex.wB - vertex.wA;
             */

            
            SimplexVertex vertex = vertices[simplex.m_count];

            Rot.mulTransUnsafe(transformA, d.negated(), temp);
            vertex.indexA = proxyA.support(temp);
            Transform.mulToOutUnsafe(transformA, proxyA.vertex(vertex.indexA), vertex.wA);
            
            Rot.mulTransUnsafe(transformB, d.negated(), temp);
            vertex.indexB = proxyB.support(temp);
            Transform.mulToOutUnsafe(transformB, proxyB.vertex(vertex.indexB), vertex.wB);
            vertex.w.set(vertex.wB).subbed(vertex.wA);

            
            ++iter;
            ++GJK_ITERS;


            boolean duplicate = IntStream.range(0, saveCount).anyMatch(i -> vertex.indexA == saveA[i] && vertex.indexB == saveB[i]);


            if (duplicate) {
                break;
            }

            
            ++simplex.m_count;
        }

        GJK_MAX_ITERS = Math.max(GJK_MAX_ITERS, iter);

        
        simplex.witnessPoints(output.pointA, output.pointB);
        output.distance = output.pointA.distance(output.pointB);
        output.iterations = iter;

        
        simplex.writeCache(cache);

        
        if (input.useRadii) {
            float rA = proxyA.m_radius;
            float rB = proxyB.m_radius;

            if (output.distance > rA + rB && output.distance > Settings.EPSILON) {
                
                
                output.distance -= rA + rB;
                normal.set(output.pointB).subbed(output.pointA);
                normal.normalize();
                temp.set(normal).scaled(rA);
                output.pointA.added(temp);
                temp.set(normal).scaled(rB);
                output.pointB.subLocal(temp);
            } else {


                output.pointA.added(output.pointB).scaled(0.5f);
                output.pointB.set(output.pointA);
                output.distance = 0.0f;
            }
        }
    }
}