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
import spacegraph.space2d.phys.collision.Distance.DistanceProxy;
import spacegraph.space2d.phys.collision.Distance.SimplexCache;
import spacegraph.space2d.phys.common.Rot;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.common.Sweep;
import spacegraph.space2d.phys.common.Transform;
import spacegraph.space2d.phys.pooling.IWorldPool;

/**
 * Class used for computing the time of impact. This class should not be constructed usually, just
 * retrieve from the {@link SingletonPool#getTOI()}.
 *
 * @author daniel
 */
public class TimeOfImpact {
    private static final int MAX_ITERATIONS = 3;

    private static int toiCalls = 0;
    private static int toiIters = 0;
    private static int toiMaxIters = 0;
    private static int toiRootIters = 0;
    private static int toiMaxRootIters = 0;

    /**
     * Input parameters for TOI
     *
     * @author Daniel Murphy
     */
    public static class TOIInput {
        public final DistanceProxy proxyA = new DistanceProxy();
        public final DistanceProxy proxyB = new DistanceProxy();
        public final Sweep sweepA = new Sweep();
        public final Sweep sweepB = new Sweep();
        /**
         * defines sweep interval [0, tMax]
         */
        public float tMax;
    }

    public enum TOIOutputState {
        UNKNOWN, FAILED, OVERLAPPED, TOUCHING, SEPARATED
    }

    /**
     * Output parameters for TimeOfImpact
     *
     * @author daniel
     */
    public static class TOIOutput {
        public TOIOutputState state;
        public float t;
    }


    
    private final SimplexCache cache = new SimplexCache();
    private final DistanceInput distanceInput = new DistanceInput();
    private final Transform xfA = new Transform();
    private final Transform xfB = new Transform();
    private final DistanceOutput distanceOutput = new DistanceOutput();
    private final SeparationFunction fcn = new SeparationFunction();
    private final int[] indexes = new int[2];
    private final Sweep sweepA = new Sweep();
    private final Sweep sweepB = new Sweep();


    private final IWorldPool pool;

    public TimeOfImpact(IWorldPool argPool) {
        pool = argPool;
    }

    /**
     * Compute the upper bound on time before two shapes penetrate. Time is represented as a fraction
     * between [0,tMax]. This uses a swept separating axis and may miss some intermediate,
     * non-tunneling collision. If you change the time interval, you should call this function again.
     * Note: use Distance to compute the contact point and normal at the time of impact.
     *
     * @param output
     * @param input
     */
    public final void timeOfImpact(TOIOutput output, TOIInput input) {
        
        

        ++toiCalls;

        output.state = TOIOutputState.UNKNOWN;
        output.t = input.tMax;

        DistanceProxy proxyA = input.proxyA;
        DistanceProxy proxyB = input.proxyB;

        sweepA.set(input.sweepA);
        sweepB.set(input.sweepB);

        
        
        sweepA.normalize();
        sweepB.normalize();

        float tMax = input.tMax;

        float totalRadius = proxyA.m_radius + proxyB.m_radius;

        float target = Math.max(Settings.linearSlop, totalRadius - 3.0f * Settings.linearSlop);
        float tolerance = 0.25f * Settings.linearSlop;

        assert (target > tolerance);

        cache.count = 0;
        distanceInput.proxyA = input.proxyA;
        distanceInput.proxyB = input.proxyB;
        distanceInput.useRadii = false;


        int iter = 0;
        for (float t1 = 0.0f; ; ) {
            sweepA.getTransform(xfA, t1);
            sweepB.getTransform(xfB, t1);
            
            
            
            
            distanceInput.transformA = xfA;
            distanceInput.transformB = xfB;
            pool.getDistance().distance(distanceOutput, cache, distanceInput);

            
            
            
            

            
            if (distanceOutput.distance <= 0.0f) {
                
                output.state = TOIOutputState.OVERLAPPED;
                output.t = 0.0f;
                break;
            }

            if (distanceOutput.distance < target + tolerance) {
                
                output.state = TOIOutputState.TOUCHING;
                output.t = t1;
                break;
            }

            
            fcn.initialize(cache, proxyA, sweepA, proxyB, sweepB, t1);

            
            
            
            boolean done = false;
            float t2 = tMax;
            for (int pushBackIter = 0; ; ) {

                
                float s2 = fcn.findMinSeparation(indexes, t2);
                
                
                if (s2 > target + tolerance) {
                    
                    output.state = TOIOutputState.SEPARATED;
                    output.t = tMax;
                    done = true;
                    break;
                }

                
                if (s2 > target - tolerance) {
                    
                    t1 = t2;
                    break;
                }

                
                float s1 = fcn.evaluate(indexes[0], indexes[1], t1);
                
                
                
                
                if (s1 < target - tolerance) {
                    output.state = TOIOutputState.FAILED;
                    output.t = t1;
                    done = true;
                    break;
                }

                
                if (s1 <= target + tolerance) {
                    
                    output.state = TOIOutputState.TOUCHING;
                    output.t = t1;
                    done = true;
                    break;
                }

                
                int rootIterCount = 0;
                float a1 = t1, a2 = t2;
                for (; ; ) {
                    
                    float t;
					t = (rootIterCount & 1) == 1 ? a1 + (target - s1) * (a2 - a1) / (s2 - s1) : 0.5f * (a1 + a2);

                    float s = fcn.evaluate(indexes[0], indexes[1], t);

                    if (Math.abs(s - target) < tolerance) {
                        
                        t2 = t;
                        break;
                    }

                    
                    if (s > target) {
                        a1 = t;
                        s1 = s;
                    } else {
                        a2 = t;
                        s2 = s;
                    }

                    ++rootIterCount;
                    ++toiRootIters;

                    
                    if (rootIterCount == 50) {
                        break;
                    }
                }

                toiMaxRootIters = Math.max(toiMaxRootIters, rootIterCount);

                ++pushBackIter;

                if (pushBackIter == Settings.maxPolygonVertices) {
                    break;
                }
            }

            ++iter;
            ++toiIters;

            if (done) {
                
                break;
            }

            if (iter == MAX_ITERATIONS) {
                
                
                output.state = TOIOutputState.FAILED;
                output.t = t1;
                break;
            }
        }


        toiMaxIters = Math.max(toiMaxIters, iter);
    }
}


enum Type {
    POINTS, FACE_A, FACE_B
}


class SeparationFunction {

    private DistanceProxy m_proxyA;
    private DistanceProxy m_proxyB;
    private Type m_type;
    private final v2 m_localPoint = new v2();
    private final v2 m_axis = new v2();
    private Sweep m_sweepA;
    private Sweep m_sweepB;

    
    private final v2 localPointA = new v2();
    private final v2 localPointB = new v2();
    private final v2 pointA = new v2();
    private final v2 pointB = new v2();
    private final v2 localPointA1 = new v2();
    private final v2 localPointA2 = new v2();
    private final v2 normal = new v2();
    private final v2 localPointB1 = new v2();
    private final v2 localPointB2 = new v2();
    private final v2 temp = new v2();
    private final Transform xfa = new Transform();
    private final Transform xfb = new Transform();

    

    public float initialize(SimplexCache cache, DistanceProxy proxyA, Sweep sweepA,
                            DistanceProxy proxyB, Sweep sweepB, float t1) {
        m_proxyA = proxyA;
        m_proxyB = proxyB;
        int count = cache.count;
        assert (0 < count && count < 3);

        m_sweepA = sweepA;
        m_sweepB = sweepB;

        m_sweepA.getTransform(xfa, t1);
        m_sweepB.getTransform(xfb, t1);

        
        
        

        if (count == 1) {
            m_type = Type.POINTS;
            /*
             * Vec2 localPointA = m_proxyA.GetVertex(cache.indexA[0]); Vec2 localPointB =
             * m_proxyB.GetVertex(cache.indexB[0]); Vec2 pointA = Mul(transformA, localPointA); Vec2
             * pointB = Mul(transformB, localPointB); m_axis = pointB - pointA; m_axis.Normalize();
             */
            localPointA.set(m_proxyA.vertex(cache.indexA[0]));
            localPointB.set(m_proxyB.vertex(cache.indexB[0]));
            Transform.mulToOutUnsafe(xfa, localPointA, pointA);
            Transform.mulToOutUnsafe(xfb, localPointB, pointB);
            m_axis.set(pointB).subbed(pointA);
            float s = m_axis.normalize();
            return s;
        } else if (cache.indexA[0] == cache.indexA[1]) {
            
            m_type = Type.FACE_B;

            localPointB1.set(m_proxyB.vertex(cache.indexB[0]));
            localPointB2.set(m_proxyB.vertex(cache.indexB[1]));

            temp.set(localPointB2).subbed(localPointB1);
            v2.crossToOutUnsafe(temp, 1.0f, m_axis);
            m_axis.normalize();

            Rot.mulToOutUnsafe(xfb, m_axis, normal);

            m_localPoint.set(localPointB1).added(localPointB2).scaled(0.5f);
            Transform.mulToOutUnsafe(xfb, m_localPoint, pointB);

            localPointA.set(proxyA.vertex(cache.indexA[0]));
            Transform.mulToOutUnsafe(xfa, localPointA, pointA);

            temp.set(pointA).subbed(pointB);
            float s = v2.dot(temp, normal);
            if (s < 0.0f) {
                m_axis.negated();
                s = -s;
            }
            return s;
        } else {
            
            m_type = Type.FACE_A;

            localPointA1.set(m_proxyA.vertex(cache.indexA[0]));
            localPointA2.set(m_proxyA.vertex(cache.indexA[1]));

            temp.set(localPointA2).subbed(localPointA1);
            v2.crossToOutUnsafe(temp, 1.0f, m_axis);
            m_axis.normalize();

            Rot.mulToOutUnsafe(xfa, m_axis, normal);

            m_localPoint.set(localPointA1).added(localPointA2).scaled(0.5f);
            Transform.mulToOutUnsafe(xfa, m_localPoint, pointA);

            localPointB.set(m_proxyB.vertex(cache.indexB[0]));
            Transform.mulToOutUnsafe(xfb, localPointB, pointB);

            temp.set(pointB).subbed(pointA);
            float s = v2.dot(temp, normal);
            if (s < 0.0f) {
                m_axis.negated();
                s = -s;
            }
            return s;
        }
    }

    private final v2 axisA = new v2();
    private final v2 axisB = new v2();

    
    public float findMinSeparation(int[] indexes, float t) {

        m_sweepA.getTransform(xfa, t);
        m_sweepB.getTransform(xfb, t);

        switch (m_type) {
            case POINTS -> {
                Rot.mulTransUnsafe(xfa, m_axis, axisA);
                Rot.mulTransUnsafe(xfb, m_axis.negated(), axisB);
                m_axis.negated();

                indexes[0] = m_proxyA.support(axisA);
                indexes[1] = m_proxyB.support(axisB);

                localPointA.set(m_proxyA.vertex(indexes[0]));
                localPointB.set(m_proxyB.vertex(indexes[1]));

                Transform.mulToOutUnsafe(xfa, localPointA, pointA);
                Transform.mulToOutUnsafe(xfb, localPointB, pointB);

                float separation = v2.dot(pointB.subbed(pointA), m_axis);
                return separation;
            }
            case FACE_A -> {
                Rot.mulToOutUnsafe(xfa, m_axis, normal);
                Transform.mulToOutUnsafe(xfa, m_localPoint, pointA);

                Rot.mulTransUnsafe(xfb, normal.negated(), axisB);
                normal.negated();

                indexes[0] = -1;
                indexes[1] = m_proxyB.support(axisB);

                localPointB.set(m_proxyB.vertex(indexes[1]));
                Transform.mulToOutUnsafe(xfb, localPointB, pointB);

                float separation = v2.dot(pointB.subbed(pointA), normal);
                return separation;
            }
            case FACE_B -> {
                Rot.mulToOutUnsafe(xfb, m_axis, normal);
                Transform.mulToOutUnsafe(xfb, m_localPoint, pointB);

                Rot.mulTransUnsafe(xfa, normal.negated(), axisA);
                normal.negated();

                indexes[1] = -1;
                indexes[0] = m_proxyA.support(axisA);

                localPointA.set(m_proxyA.vertex(indexes[0]));
                Transform.mulToOutUnsafe(xfa, localPointA, pointA);

                float separation = v2.dot(pointA.subbed(pointB), normal);
                return separation;
            }
            default -> {
                assert (false);
                indexes[0] = -1;
                indexes[1] = -1;
                return 0.0f;
            }
        }
    }

    public float evaluate(int indexA, int indexB, float t) {
        m_sweepA.getTransform(xfa, t);
        m_sweepB.getTransform(xfb, t);

        switch (m_type) {
            case POINTS -> {
                Rot.mulTransUnsafe(xfa, m_axis, axisA);
                Rot.mulTransUnsafe(xfb, m_axis.negated(), axisB);
                m_axis.negated();

                localPointA.set(m_proxyA.vertex(indexA));
                localPointB.set(m_proxyB.vertex(indexB));

                Transform.mulToOutUnsafe(xfa, localPointA, pointA);
                Transform.mulToOutUnsafe(xfb, localPointB, pointB);

                float separation = v2.dot(pointB.subbed(pointA), m_axis);
                return separation;
            }
            case FACE_A -> {
                Rot.mulToOutUnsafe(xfa, m_axis, normal);
                Transform.mulToOutUnsafe(xfa, m_localPoint, pointA);

                Rot.mulTransUnsafe(xfb, normal.negated(), axisB);
                normal.negated();

                localPointB.set(m_proxyB.vertex(indexB));
                Transform.mulToOutUnsafe(xfb, localPointB, pointB);
                float separation = v2.dot(pointB.subbed(pointA), normal);
                return separation;
            }
            case FACE_B -> {
                Rot.mulToOutUnsafe(xfb, m_axis, normal);
                Transform.mulToOutUnsafe(xfb, m_localPoint, pointB);

                Rot.mulTransUnsafe(xfa, normal.negated(), axisA);
                normal.negated();

                localPointA.set(m_proxyA.vertex(indexA));
                Transform.mulToOutUnsafe(xfa, localPointA, pointA);

                float separation = v2.dot(pointA.subbed(pointB), normal);
                return separation;
            }
            default -> {
                assert (false);
                return 0.0f;
            }
        }
    }
}