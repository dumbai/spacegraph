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

/**
 * Represents a rotation
 *
 * @author Daniel
 */
public class Rot {

    public float s = 0, c = 1;

    public Rot() {
    }

    public Rot(float angle) {
        set(angle);
    }

    static void mul(Rot q, Rot r, Rot out) {
        float tempc = q.c * r.c - q.s * r.s;
        out.s = q.s * r.c + q.c * r.s;
        out.c = tempc;
    }

    static void mulUnsafe(Rot q, Rot r, Rot out) {
        assert (r != out && q != out);
        out.s = q.s * r.c + q.c * r.s;
        out.c = q.c * r.c - q.s * r.s;
    }

    static void mulTransUnsafe(Rot q, Rot r, Rot out) {
        out.s = q.c * r.s - q.s * r.c;
        out.c = q.c * r.c + q.s * r.s;
    }

    public static void mulToOut(Rot q, v2 v, v2 out) {
        float tempy = q.s * v.x + q.c * v.y;
        out.x = q.c * v.x - q.s * v.y;
        out.y = tempy;
    }

    public static void mulToOutUnsafe(Rot q, v2 v, v2 out) {
        float vx = v.x, vy = v.y;
        float qc = q.c, qs = q.s;
        out.x = qc * vx - qs * vy;
        out.y = qs * vx + qc * vy;
    }

    public static void mulTrans(Rot q, v2 v, v2 out) {
        float tempy = -q.s * v.x + q.c * v.y;
        out.x = q.c * v.x + q.s * v.y;
        out.y = tempy;
    }

    public static void mulTransUnsafe(Rot q, v2 v, v2 out) {
        out.x = q.c * v.x + q.s * v.y;
        out.y = -q.s * v.x + q.c * v.y;
    }

    @Override
    public String toString() {
        return "Rot(s:" + s + ", c:" + c + ')';
    }
//
//    public static void mulTrans(Rot q, Rot r, Rot out) {
//        float tempc = q.c * r.c + q.s * r.s;
//        out.s = q.c * r.s - q.s * r.c;
//        out.c = tempc;
//    }

    public Rot set(float angle) {
        s = (float) Math.sin(angle);
        c = (float) Math.cos(angle);
        return this;
    }

    public Rot set(Rot other) {
        s = other.s;
        c = other.c;
        return this;
    }

    void setIdentity() {
        s = 0;
        c = 1;
    }

    @Deprecated /* slow */ public float angle() {
        return (float) Math.atan2(s, c);
    }

    //
//    public void getXAxis(v2 xAxis) {
//        xAxis.set(c, s);
//    }
//
//    public void getYAxis(v2 yAxis) {
//        yAxis.set(-s, c);
//    }
//
//
    public Rot clone() {
        Rot copy = new Rot();
        copy.s = s;
        copy.c = c;
        return copy;
    }


}