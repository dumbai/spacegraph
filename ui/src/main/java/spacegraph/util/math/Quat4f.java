/*
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 127 $
 * $Date: 2008-02-28 17:18:51 -0300 (Thu, 28 Feb 2008) $
 * $State$
 */

package spacegraph.util.math;

import jcog.Util;
import jcog.math.v3;

/**
 * A 4 element unit quaternion represented by single precision floating
 * point x,y,z,w coordinates.  The quaternion is always normalized.
 */
public class Quat4f extends Tuple4f {


    static final long serialVersionUID = 2675933778405442383L;
    static final float PIO2 = 1.57079632679f;
    private static final float EPS = 0.000001f;
    private static final float EPS2 = Float.MIN_NORMAL;

    /**
     * Constructs and initializes a Quat4f from the specified xyzw coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param w the w scalar component
     */
    public Quat4f(float x, float y, float z, float w) {
        float mag = (float) (1.0 / Math.sqrt(x * x + y * y + z * z + w * w));
        this.x = x * mag;
        this.y = y * mag;
        this.z = z * mag;
        this.w = w * mag;

    }

    /**
     * Constructs and initializes a Quat4f from the array of length 4.
     *
     * @param q the array of length 4 containing xyzw in order
     */
    public Quat4f(float[] q) {
        float mag = (float) (1.0 / Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]));
        x = q[0] * mag;
        y = q[1] * mag;
        z = q[2] * mag;
        w = q[3] * mag;

    }


    /**
     * Constructs and initializes a Quat4f from the specified Quat4f.
     *
     * @param q1 the Quat4f containing the initialization x y z w data
     */
    public Quat4f(Quat4f q1) {
        super(q1);
    }


    /**
     * Constructs and initializes a Quat4f from the specified Tuple4f.
     *
     * @param t1 the Tuple4f containing the initialization x y z w data
     */
    public Quat4f(Tuple4f t1) {
        float mag = (float) (1.0 / Math.sqrt(t1.x * t1.x + t1.y * t1.y + t1.z * t1.z + t1.w * t1.w));
        x = t1.x * mag;
        y = t1.y * mag;
        z = t1.z * mag;
        w = t1.w * mag;

    }


    /**
     * Constructs and initializes a Quat4f to (0.0,0.0,0.0,0.0).
     */
    public Quat4f() {
        super();
    }

    public static Quat4f angle(float ax, float ay, float az, float angle) {
        Quat4f q = new Quat4f();
        q.setAngle(ax, ay, az, angle);
        return q;
    }

    /**
     * Sets the value of this quaternion to the conjugate of quaternion q1.
     *
     * @param q1 the source vector
     */
    public final void conjugate(Quat4f q1) {
        this.x = -q1.x;
        this.y = -q1.y;
        this.z = -q1.z;
        this.w = q1.w;
    }

    /**
     * Sets the value of this quaternion to the conjugate of itself.
     */
    public final void conjugate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
    }

    /**
     * Sets the value of this quaternion to the quaternion product of
     * quaternions q1 and q2 (this = q1 * q2).
     * Note that this is safe for aliasing (e.g. this can be q1 or q2).
     *
     * @param q1 the first quaternion
     * @param q2 the second quaternion
     */
    public final void mul(Quat4f q1, Quat4f q2) {
        if (this != q1 && this != q2) {
            this.w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
            this.x = q1.w * q2.x + q2.w * q1.x + q1.y * q2.z - q1.z * q2.y;
            this.y = q1.w * q2.y + q2.w * q1.y - q1.x * q2.z + q1.z * q2.x;
            this.z = q1.w * q2.z + q2.w * q1.z + q1.x * q2.y - q1.y * q2.x;
        } else {

            float w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
            float x = q1.w * q2.x + q2.w * q1.x + q1.y * q2.z - q1.z * q2.y;
            float y = q1.w * q2.y + q2.w * q1.y - q1.x * q2.z + q1.z * q2.x;
            this.z = q1.w * q2.z + q2.w * q1.z + q1.x * q2.y - q1.y * q2.x;
            this.w = w;
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Sets the value of this quaternion to the quaternion product of
     * itself and q1 (this = this * q1).
     *
     * @param q1 the other quaternion
     */
    public final void mul(Quat4f q1) {

        float w = this.w * q1.w - this.x * q1.x - this.y * q1.y - this.z * q1.z;
        float x = this.w * q1.x + q1.w * this.x + this.y * q1.z - this.z * q1.y;
        float y = this.w * q1.y + q1.w * this.y - this.x * q1.z + this.z * q1.x;
        this.z = this.w * q1.z + q1.w * this.z + this.x * q1.y - this.y * q1.x;
        this.w = w;
        this.x = x;
        this.y = y;
    }

    /**
     * Multiplies quaternion q1 by the inverse of quaternion q2 and places
     * the value into this quaternion.  The value of both argument quaternions
     * is preservered (this = q1 * q2^-1).
     *
     * @param q1 the first quaternion
     * @param q2 the second quaternion
     */
    public final void mulInverse(Quat4f q1, Quat4f q2) {
        Quat4f tempQuat = new Quat4f(q2);

        tempQuat.inverse();
        this.mul(q1, tempQuat);
    }

    /**
     * Multiplies this quaternion by the inverse of quaternion q1 and places
     * the value into this quaternion.  The value of the argument quaternion
     * is preserved (this = this * q^-1).
     *
     * @param q1 the other quaternion
     */
    public final void mulInverse(Quat4f q1) {
        Quat4f tempQuat = new Quat4f(q1);

        tempQuat.inverse();
        this.mul(tempQuat);
    }

    /**
     * Sets the value of this quaternion to quaternion inverse of quaternion q1.
     *
     * @param q1 the quaternion to be inverted
     */
    public final void inverse(Quat4f q1) {

        float norm = 1.0f / (q1.w * q1.w + q1.x * q1.x + q1.y * q1.y + q1.z * q1.z);
        this.w = norm * q1.w;
        this.x = -norm * q1.x;
        this.y = -norm * q1.y;
        this.z = -norm * q1.z;
    }

    /**
     * Sets the value of this quaternion to the quaternion inverse of itself.
     */
    private void inverse() {

        float norm = 1.0f / (this.w * this.w + this.x * this.x + this.y * this.y + this.z * this.z);
        this.w *= norm;
        this.x *= -norm;
        this.y *= -norm;
        this.z *= -norm;
    }

    /**
     * Sets the value of this quaternion to the normalized value
     * of quaternion q1.
     *
     * @param q1 the quaternion to be normalized.
     */
    public final void normalize(Quat4f q1) {

        double normd = (((double) q1.x) * q1.x + q1.y * q1.y + q1.z * q1.z + q1.w * q1.w);

        if (normd > 0.0) {
            float n = (float) (1.0 / Math.sqrt(normd));
            this.x = n * q1.x;
            this.y = n * q1.y;
            this.z = n * q1.z;
            this.w = n * q1.w;
        } else {
            this.x = this.y = this.z = this.w = 0.0f;
        }
    }

    /**
     * Normalizes the value of this quaternion in place.
     */
    public final void normalize() {

        double norm = (this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w);

        if (norm > 0.0f) {
            norm = (float) (1.0 / Math.sqrt(norm));
            this.x *= norm;
            this.y *= norm;
            this.z *= norm;
            this.w *= norm;
        } else {
            this.x = 0.0f;
            this.y = 0.0f;
            this.z = 0.0f;
            this.w = 0.0f;
        }
    }


    public void setAngle(float ax, float ay, float az, float angle) {
        double amag = Util.sqr((double) ax) + Util.sqr((double) ay) + Util.sqr((double) az);
        if (amag < EPS * EPS) {
            this.x = this.y = this.z = this.w = 0.0f;
        } else {
            if (Util.equals(angle, 0, EPS)) {
                this.w = 1;
                this.x = this.y = this.z = 0;
            } else {
                amag = (float) (1.0 / Math.sqrt(amag));

                double ha = angle / 2.0;
                double mag = (float) Math.sin(ha) * amag;
                this.w = (float) Math.cos(ha);
                if (mag < EPS * EPS) {
                    this.x = this.y = this.z = 0;
                } else {
                    this.x = (float) (ax * mag);
                    this.y = (float) (ay * mag);
                    this.z = (float) (az * mag);
                }
            }
        }
    }


    public final v3 rotateVector(v3 vecIn, v3 vecOut) {


        float vecX = vecIn.x;
        float vecY = vecIn.y;
        float vecZ = vecIn.z;
        float x_x = x * x;
        float y_y = y * y;
        float z_z = z * z;
        float w_w = w * w;

        float ox = w_w * vecX
                + x_x * vecX
                - z_z * vecX
                - y_y * vecX
                + 2.0f * (y * w * vecZ - z * w * vecY + y * x * vecY + z * x * vecZ);

        float oy = y_y * vecY
                - z_z * vecY
                + w_w * vecY
                - x_x * vecY
                + 2.0f * (x * y * vecX + z * y * vecZ + w * z * vecX - x * w * vecZ);

        float oz = z_z * vecZ
                - y_y * vecZ
                - x_x * vecZ
                + w_w * vecZ
                + 2.0f * (x * z * vecX + y * z * vecY - w * y * vecX + w * x * vecY);
        vecOut.set(ox, oy, oz);
        return vecOut;

    }

    public void set(int i, float v) {
        switch (i) {
            case 0 -> this.x = v;
            case 1 -> this.y = v;
            case 2 -> this.z = v;
            case 3 -> this.w = v;
            default -> throw new UnsupportedOperationException();
        }
    }
}