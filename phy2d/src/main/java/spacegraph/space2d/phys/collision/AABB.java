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
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.pooling.IWorldPool;
import spacegraph.space2d.phys.pooling.normal.DefaultWorldPool;

/**
 * An axis-aligned bounding box.
 */
public class AABB {
    /**
     * Bottom left vertex of bounding box.
     */
    public final v2 lowerBound;
    /**
     * Top right vertex of bounding box.
     */
    public final v2 upperBound;

    /**
     * Creates the default object, with vertices at 0,0 and 0,0.
     */
    public AABB() {
        lowerBound = new v2();
        upperBound = new v2();
    }

    /**
     * Copies from the given object
     *
     * @param copy the object to copy from
     */




    /**
     * Creates an AABB object using the given bounding vertices.
     *
     * @param lowerVertex the bottom left vertex of the bounding box
     * @param maxVertex   the top right vertex of the bounding box
     */
    public AABB(v2 lowerVertex, v2 upperVertex, boolean clone) {
        if (clone) {
            this.lowerBound = new v2(lowerVertex);
            this.upperBound = new v2(upperVertex);
        } else {
            this.lowerBound = lowerVertex;
            this.upperBound = upperVertex;
        }
    }

    /**
     * Sets this object from the given object
     *
     * @param aabb the object to copy from
     */
    public final void set(AABB aabb) {
        v2 v = aabb.lowerBound;
        lowerBound.x = v.x;
        lowerBound.y = v.y;
        v2 v1 = aabb.upperBound;
        upperBound.x = v1.x;
        upperBound.y = v1.y;
    }

    /**
     * Verify that the bounds are sorted
     */
    public final boolean isValid() {
        float dx = upperBound.x - lowerBound.x;
        if (dx < 0.0f) {
            return false;
        }
        float dy = upperBound.y - lowerBound.y;
        if (dy < 0) {
            return false;
        }
        return lowerBound.isValid() && upperBound.isValid();
    }

    /**
     * Get the center of the AABB
     *
     * @return
     */
    public final v2 center() {
        return new v2(0.5f * (upperBound.x + lowerBound.x), 0.5f * (upperBound.y + lowerBound.y));
    }
    public final void centerToOut(v2 out) {
        out.x = (lowerBound.x + upperBound.x) * 0.5f;
        out.y = (lowerBound.y + upperBound.y) * 0.5f;
    }

    /**
     * Get the extents of the AABB (half-widths).
     *
     * @return
     */
    public final v2 extents() {
        return new v2(0.5f * (upperBound.x - lowerBound.x), 0.5f * (upperBound.y - lowerBound.y));
    }

    public final void extentsToOut(v2 out) {
        out.x = (upperBound.x - lowerBound.x) * 0.5f;
        out.y = (upperBound.y - lowerBound.y) * 0.5f;
    }

    public final void vertices(v2[] argRay) {
        argRay[0].set(lowerBound);
        argRay[1].set(lowerBound);
        argRay[1].x += upperBound.x - lowerBound.x;
        argRay[2].set(upperBound);
        argRay[3].set(upperBound);
        argRay[3].x -= upperBound.x - lowerBound.x;
    }

    /**
     * Combine two AABBs into this one.
     *
     * @param aabb1
     * @param aab
     */
    public final void combine(AABB aabb1, AABB aab) {
        lowerBound.x = Math.min(aabb1.lowerBound.x, aab.lowerBound.x);
        lowerBound.y = Math.min(aabb1.lowerBound.y, aab.lowerBound.y);
        upperBound.x = Math.max(aabb1.upperBound.x, aab.upperBound.x);
        upperBound.y = Math.max(aabb1.upperBound.y, aab.upperBound.y);
    }

    /**
     * Gets the perimeter length
     *
     * @return
     */
    public final float getPerimeter() {
        return 2.0f * (upperBound.x - lowerBound.x + upperBound.y - lowerBound.y);
    }

    /**
     * Combines another aabb with this one
     *
     * @param aabb
     */
    public final void combine(AABB aabb) {
        lowerBound.x = Math.min(lowerBound.x, aabb.lowerBound.x);
        lowerBound.y = Math.min(lowerBound.y, aabb.lowerBound.y);
        upperBound.x = Math.max(upperBound.x, aabb.upperBound.x);
        upperBound.y = Math.max(upperBound.y, aabb.upperBound.y);
    }

    /**
     * Does this aabb contain the provided AABB.
     *
     * @return
     */
    public final boolean contains(AABB aabb) {
        /*
         * boolean result = true; result = result && lowerBound.x <= aabb.lowerBound.x; result = result
         * && lowerBound.y <= aabb.lowerBound.y; result = result && aabb.upperBound.x <= upperBound.x;
         * result = result && aabb.upperBound.y <= upperBound.y; return result;
         */
        
        
        return lowerBound.x <= aabb.lowerBound.x && lowerBound.y <= aabb.lowerBound.y
                && aabb.upperBound.x <= upperBound.x && aabb.upperBound.y <= upperBound.y;
    }

    /**
     * @param output
     * @param input
     * @return
     * @deprecated please use {@link #raycast(RayCastOutput, RayCastInput, IWorldPool)} for better
     * performance
     */
    public final boolean raycast(RayCastOutput output, RayCastInput input) {
        return raycast(output, input, new DefaultWorldPool(4, 4));
    }

    /**
     * From Real-time Collision Detection, p179.
     *
     * @param output
     * @param input
     */
    private boolean raycast(RayCastOutput output, RayCastInput input,
                            IWorldPool argPool) {

        v2 p = argPool.popVec2();
                                v2 d = argPool.popVec2();
                                v2 absD = argPool.popVec2();
                                v2 normal = argPool.popVec2();

                                p.set(input.p1);
                                d.set(input.p2).subbed(input.p1);
                                v2.absToOut(d, absD);


        float tmax = Float.MAX_VALUE;
        float tmin = -Float.MAX_VALUE;
        boolean result = true;
        if (absD.x < Settings.EPSILON) {

            if (p.x < lowerBound.x || upperBound.x < p.x) {
                                        argPool.pushVec2(4);
                result = false;
            }
                                } else {
                                    float inv_d = 1.0f / d.x;
                                    float t1 = (lowerBound.x - p.x) * inv_d;
                                    float t2 = (upperBound.x - p.x) * inv_d;


            float s = -1.0f;

                                    if (t1 > t2) {
                                        float temp = t1;
                                        t1 = t2;
                                        t2 = temp;
                                        s = 1.0f;
                                    }


            if (t1 > tmin) {
                                        normal.setZero();
                                        normal.x = s;
                                        tmin = t1;
                                    }


            tmax = Math.min(tmax, t2);

                                    if (tmin > tmax) {
                                        argPool.pushVec2(4);
                                        result = false;
                                    }
                                }
        if (result) {
            if (absD.y < Settings.EPSILON) {

                if (p.y < lowerBound.y || upperBound.y < p.y) {
                    argPool.pushVec2(4);
                    result = false;
                }
            } else {
                float inv_d = 1.0f / d.y;
                float t1 = (lowerBound.y - p.y) * inv_d;
                float t2 = (upperBound.y - p.y) * inv_d;


                float s = -1.0f;

                if (t1 > t2) {
                    float temp = t1;
                    t1 = t2;
                    t2 = temp;
                    s = 1.0f;
                }


                if (t1 > tmin) {
                    normal.setZero();
                    normal.y = s;
                    tmin = t1;
                }


                tmax = Math.min(tmax, t2);

                if (tmin > tmax) {
                    argPool.pushVec2(4);
                    result = false;
                }
            }
            if (result) {
                if (tmin < 0.0f || input.maxFraction < tmin) {
                    argPool.pushVec2(4);
                    result = false;
                } else {
                    output.fraction = tmin;
                    output.normal.x = normal.x;
                    output.normal.y = normal.y;
                    argPool.pushVec2(4);
                }


            }


        }

        return result;
    }

    public static boolean testOverlap(AABB a, AABB b) {
        if (b.lowerBound.x - a.upperBound.x > 0.0f || b.lowerBound.y - a.upperBound.y > 0.0f) {
            return false;
        }

        return !(a.lowerBound.x - b.upperBound.x > 0.0f) && !(a.lowerBound.y - b.upperBound.y > 0.0f);
    }

    @Override
    public final String toString() {
        return "AABB[" + lowerBound + " . " + upperBound + ']';
    }
}