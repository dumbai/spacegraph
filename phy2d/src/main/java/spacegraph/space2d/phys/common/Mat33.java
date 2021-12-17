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
import jcog.math.v3;

import java.io.Serializable;
import java.util.Objects;

/**
 * A 3-by-3 matrix. Stored in column-major order.
 *
 * @author Daniel Murphy
 */
public class Mat33 implements Serializable {

    public static final Mat33 IDENTITY = new Mat33(new Vec3(1, 0, 0), new Vec3(0, 1, 0), new Vec3(0,
            0, 1));

    public final v3 ex;
    public final v3 ey;
    public final v3 ez;

    public Mat33() {
        ex = new Vec3();
        ey = new Vec3();
        ez = new Vec3();
    }

    public Mat33(float exx, float exy, float exz, float eyx, float eyy, float eyz, float ezx,
                 float ezy, float ezz) {
        ex = new Vec3(exx, exy, exz);
        ey = new Vec3(eyx, eyy, eyz);
        ez = new Vec3(ezx, ezy, ezz);
    }

    private Mat33(v3 argCol1, v3 argCol2, v3 argCol3) {
        ex = argCol1.clone();
        ey = argCol2.clone();
        ez = argCol3.clone();
    }

    public void setZero() {
        ex.zero();
        ey.zero();
        ez.zero();
    }

    public void set(float exx, float exy, float exz, float eyx, float eyy, float eyz, float ezx,
                    float ezy, float ezz) {
        ex.x = exx;
        ex.y = exy;
        ex.z = exz;
        ey.x = eyx;
        ey.y = eyy;
        ey.z = eyz;
        ez.x = ezx;
        ez.y = ezy;
        ez.z = ezz;
    }

    public void set(Mat33 mat) {
        v3 vec = mat.ex;
        ex.x = vec.x;
        ex.y = vec.y;
        ex.z = vec.z;
        v3 vec1 = mat.ey;
        ey.x = vec1.x;
        ey.y = vec1.y;
        ey.z = vec1.z;
        v3 vec2 = mat.ez;
        ez.x = vec2.x;
        ez.y = vec2.y;
        ez.z = vec2.z;
    }

    public void setIdentity() {
        ex.x = 1;
        ex.y = 0;
        ex.z = 0;
        ey.x = 0;
        ey.y = 1;
        ey.z = 0;
        ez.x = 0;
        ez.y = 0;
        ez.z = 1;
    }

    
    public static v3 mul(Mat33 A, v3 v) {
        return new Vec3(v.x * A.ex.x + v.y * A.ey.x + v.z + A.ez.x, v.x * A.ex.y + v.y * A.ey.y + v.z
                * A.ez.y, v.x * A.ex.z + v.y * A.ey.z + v.z * A.ez.z);
    }

    public static v2 mul22(Mat33 A, v2 v) {
        return new v2(A.ex.x * v.x + A.ey.x * v.y, A.ex.y * v.x + A.ey.y * v.y);
    }

    public static void mul22ToOut(Mat33 A, v2 v, v2 out) {
        float tempx = A.ex.x * v.x + A.ey.x * v.y;
        out.y = A.ex.y * v.x + A.ey.y * v.y;
        out.x = tempx;
    }

    public static void mul22ToOutUnsafe(Mat33 A, v2 v, v2 out) {
        assert (v != out);
        out.y = A.ex.y * v.x + A.ey.y * v.y;
        out.x = A.ex.x * v.x + A.ey.x * v.y;
    }

    public static void mulToOut(Mat33 A, v3 v, v3 out) {
        float tempy = v.x * A.ex.y + v.y * A.ey.y + v.z * A.ez.y;
        float tempz = v.x * A.ex.z + v.y * A.ey.z + v.z * A.ez.z;
        out.x = v.x * A.ex.x + v.y * A.ey.x + v.z * A.ez.x;
        out.y = tempy;
        out.z = tempz;
    }

    public static void mulToOutUnsafe(Mat33 A, v3 v, v3 out) {
        assert (out != v);
        out.x = v.x * A.ex.x + v.y * A.ey.x + v.z * A.ez.x;
        out.y = v.x * A.ex.y + v.y * A.ey.y + v.z * A.ez.y;
        out.z = v.x * A.ex.z + v.y * A.ey.z + v.z * A.ez.z;
    }

    /**
     * Solve A * x = b, where b is a column vector. This is more efficient than computing the inverse
     * in one-shot cases.
     *
     * @param b
     * @return
     */
    public final v2 solve22(v2 b) {
        v2 x = new v2();
        solve22ToOut(b, x);
        return x;
    }

    /**
     * Solve A * x = b, where b is a column vector. This is more efficient than computing the inverse
     * in one-shot cases.
     *
     * @param b
     * @return
     */
    public final void solve22ToOut(v2 b, v2 out) {
        float a11 = ex.x, a12 = ey.x, a21 = ex.y, a22 = ey.y;
        float det = a11 * a22 - a12 * a21;
        if (Math.abs(det) > Float.MIN_NORMAL) {
            det = 1.0f / det;
        }
        out.x = det * (a22 * b.x - a12 * b.y);
        out.y = det * (a11 * b.y - a21 * b.x);
    }

    

    /**
     * Solve A * x = b, where b is a column vector. This is more efficient than computing the inverse
     * in one-shot cases.
     *
     * @param b
     * @return
     */
    public final v3 solve33(v3 b) {
        v3 x = new Vec3();
        solve33ToOut(b, x);
        return x;
    }

    /**
     * Solve A * x = b, where b is a column vector. This is more efficient than computing the inverse
     * in one-shot cases.
     *
     * @param b
     * @param out the result
     */
    public final void solve33ToOut(v3 b, v3 out) {
        assert (b != out);
        v3.crossToOutUnsafe(ey, ez, out);
        float det = ex.dot(out);
        if (Math.abs(det) > Float.MIN_NORMAL) {
            det = 1 / det;
        }
        v3.crossToOutUnsafe(ey, ez, out);
        float x = det * b.dot(out);
        v3.crossToOutUnsafe(b, ez, out);
        float y = det * ex.dot(out);
        v3.crossToOutUnsafe(ey, b, out);
        out.set(x, y, det * ex.dot(out));
    }

    public void getInverse22(Mat33 M) {
        float a = ex.x, b = ey.x, c = ex.y, d = ey.y;
        float det = a * d - b * c;
        if (Math.abs(det) > Float.MIN_NORMAL)
            det = 1 / det;
        
        M.ex.x =  det * d;
        M.ey.x = -det * b;
        M.ex.z = 0;
        M.ex.y = -det * c;
        M.ey.y =  det * a;
        M.ey.z = 0;
        M.ez.x = 0;
        M.ez.y = 0;
        M.ez.z = 0;
    }

    
    public void getSymInverse33(Mat33 M) {
        float bx = ey.y * ez.z - ey.z * ez.y;
        float by = ey.z * ez.x - ey.x * ez.z;
        float bz = ey.x * ez.y - ey.y * ez.x;
        float det = ex.x * bx + ex.y * by + ex.z * bz;
        if (Math.abs(det) > Float.MIN_NORMAL) {
            det = 1 / det;
        }

        float a11 = ex.x, a12 = ey.x, a13 = ez.x;
        float a22 = ey.y, a23 = ez.y;
        float a33 = ez.z;

        M.ex.x = det * (a22 * a33 - a23 * a23);
        M.ex.y = det * (a13 * a23 - a12 * a33);
        M.ex.z = det * (a12 * a23 - a13 * a22);

        M.ey.x = M.ex.y;
        M.ey.y = det * (a11 * a33 - a13 * a13);
        M.ey.z = det * (a13 * a12 - a11 * a23);

        M.ez.x = M.ex.z;
        M.ez.y = M.ey.z;
        M.ez.z = det * (a11 * a22 - a12 * a12);
    }


    public static void setScaleTransform(float scale, Mat33 out) {
        out.ex.x = scale;
        out.ey.y = scale;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((ex == null) ? 0 : ex.hashCode());
        result = prime * result + ((ey == null) ? 0 : ey.hashCode());
        result = prime * result + ((ez == null) ? 0 : ez.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Mat33 other = (Mat33) obj;
        if (ex == null) {
            if (other.ex != null) return false;
        } else if (!ex.equals(other.ex)) return false;
        if (ey == null) {
            if (other.ey != null) return false;
        } else if (!ey.equals(other.ey)) return false;
		return Objects.equals(ez, other.ez);
    }
}