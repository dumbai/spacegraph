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
package spacegraph.space2d.phys.common;

import jcog.math.v2;

import java.io.Serializable;

/**
 * This describes the motion of a body/shape for TOI computation. Shapes are defined with respect to
 * the body origin, which may no coincide with the center of mass. However, to support dynamics we
 * must interpolate the center of mass position.
 */
public class Sweep implements Serializable {

    /**
     * Local center of mass position
     */
    public final v2 localCenter;
    /**
     * Center world positions
     */
    public final v2 c0;
    public final v2 c;
    /**
     * World angles
     */
    public float a0;
    public float a;

    /**
     * Fraction of the current time step in the range [0,1] c0 and a0 are the positions at alpha0.
     */
    public float alpha0;

    public String toString() {
        String s = "Sweep:\nlocalCenter: " + localCenter + '\n';
        s += "c0: " + c0 + ", c: " + c + '\n';
        s += "a0: " + a0 + ", a: " + a + '\n';
        s += "alpha0: " + alpha0;
        return s;
    }

    public Sweep() {
        localCenter = new v2();
        c0 = new v2();
        c = new v2();
    }

    public final void normalize() {
        float d = (float)(MathUtils.TWOPI * Math.floor(a0 / MathUtils.TWOPI));
        a0 -= d;
        a -= d;
    }

    public final Sweep set(Sweep other) {
        localCenter.set(other.localCenter);
        c0.set(other.c0);
        c.set(other.c);
        a0 = other.a0;
        a = other.a;
        alpha0 = other.alpha0;
        return this;
    }

    /**
     * Get the interpolated transform at a specific time.
     *
     * @param f the result is placed here - must not be null
     * @param t  the normalized time in [0,1].
     */
    public final void getTransform(Transform f, float beta) {
        //assert (f != null);

        float antiBeta = 1.0f - beta;
        float px = antiBeta * c0.x + beta * c.x;
        float py = antiBeta * c0.y + beta * c.y;
        float angle = antiBeta * a0 + beta * a;
        f.set(angle);


        float lx = localCenter.x, ly = localCenter.y;
        px -= f.c * lx - f.s * ly;
        py -= f.s * lx + f.c * ly;

        v2 p = f.pos;
        p.x = px; p.y = py;
    }

    /**
     * Advance the sweep forward, yielding a new initial state.
     *
     * @param alpha the new initial time.
     */
    public final void advance(float alpha) {
        //assert (alpha0 < 1.0f);

        float beta = (alpha - alpha0) / (1 - alpha0);
        c0.x += beta * (c.x - c0.x);
        c0.y += beta * (c.y - c0.y);
        a0 += beta * (a - a0);
        alpha0 = alpha;
    }
}
