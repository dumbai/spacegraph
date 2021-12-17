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
import java.util.Objects;

/**
 * A 2-by-2 matrix. Stored in column-major order.
 */
public class Mat22 implements Serializable {
    private static final long serialVersionUID = 2L;

    public final v2 ex;
    public final v2 ey;

    /**
     * Convert the matrix to printable format.
     */
    @Override
    public String toString() {
        String s = "";
        s += "[" + ex.x + ',' + ey.x + "]\n";
        s += "[" + ex.y + ',' + ey.y + ']';
        return s;
    }

    /**
     * Construct zero matrix. Note: this is NOT an identity matrix! djm fixed double allocation
     * problem
     */
    public Mat22() {
        ex = new v2();
        ey = new v2();
    }

    /**
     * Create a matrix with given vectors as columns.
     *
     * @param c1 Column 1 of matrix
     * @param c2 Column 2 of matrix
     */
    private Mat22(v2 c1, v2 c2) {
        ex = c1.clone();
        ey = c2.clone();
    }

    /**
     * Create a matrix from four floats.
     *
     * @param exx
     * @param col2x
     * @param exy
     * @param col2y
     */
    public Mat22(float exx, float col2x, float exy, float col2y) {
        ex = new v2(exx, exy);
        ey = new v2(col2x, col2y);
    }

    /**
     * Set as a copy of another matrix.
     *
     * @param m Matrix to copy
     */
    public final Mat22 set(Mat22 m) {
        ex.x = m.ex.x;
        ex.y = m.ex.y;
        ey.x = m.ey.x;
        ey.y = m.ey.y;
        return this;
    }

    public final Mat22 set(float exx, float col2x, float exy, float col2y) {
        ex.x = exx;
        ex.y = exy;
        ey.x = col2x;
        ey.y = col2y;
        return this;
    }

    /**
     * Return a clone of this matrix. djm fixed double allocation
     */
    
    public final Mat22 clone() {
        return new Mat22(ex, ey);
    }

    /**
     * Set as a matrix representing a rotation.
     *
     * @param angle Rotation (in radians) that matrix represents.
     */
    public final void set(float angle) {

        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);
        ex.x = c;
        ey.x = -s;
        ex.y = s;
        ey.y = c;
    }

    /**
     * Set as the identity matrix.
     */
    public final void setIdentity() {
        ex.x = 1.0f;
        ey.x = 0.0f;
        ex.y = 0.0f;
        ey.y = 1.0f;
    }

    /**
     * Set as the zero matrix.
     */
    public final void setZero() {
        ex.x = 0.0f;
        ey.x = 0.0f;
        ex.y = 0.0f;
        ey.y = 0.0f;
    }

    /**
     * Extract the angle from this matrix (assumed to be a rotation matrix).
     *
     * @return
     */
    public final float getAngle() {
        return (float) Math.atan2(ex.y, ex.x);
    }

    /**
     * Set by column vectors.
     *
     * @param c1 Column 1
     * @param c2 Column 2
     */
    public final void set(v2 c1, v2 c2) {
        ex.x = c1.x;
        ey.x = c2.x;
        ex.y = c1.y;
        ey.y = c2.y;
    }

    /**
     * Returns the inverted Mat22 - does NOT invert the matrix locally!
     */
    public final Mat22 invert() {
        float a = ex.x, b = ey.x, c = ex.y, d = ey.y;
        Mat22 B = new Mat22();
        float det = a * d - b * c;
        if (Math.abs(det) > Float.MIN_NORMAL) {
            det = 1.0f / det;
        }
        B.ex.x = det * d;
        B.ey.x = -det * b;
        B.ex.y = -det * c;
        B.ey.y = det * a;
        return B;
    }

    public final Mat22 invertLocal() {
        float a = ex.x, b = ey.x, c = ex.y, d = ey.y;
        float det = a * d - b * c;
        if (Math.abs(det) > Float.MIN_NORMAL) {
            det = 1.0f / det;
        }
        ex.x = det * d;
        ey.x = -det * b;
        ex.y = -det * c;
        ey.y = det * a;
        return this;
    }

    public final void invertToOut(Mat22 out) {
        float a = ex.x, b = ey.x, c = ex.y, d = ey.y;
        float det = a * d - b * c;
        
        det = 1.0f / det;
        out.ex.x = det * d;
        out.ey.x = -det * b;
        out.ex.y = -det * c;
        out.ey.y = det * a;
    }


    /**
     * Return the matrix composed of the absolute values of all elements. djm: fixed double allocation
     *
     * @return Absolute value matrix
     */
    private Mat22 abs() {
        return new Mat22(Math.abs(ex.x), Math.abs(ey.x), Math.abs(ex.y),
                Math.abs(ey.y));
    }


    /**
     * Return the matrix composed of the absolute values of all elements.
     *
     * @return Absolute value matrix
     */
    public static Mat22 abs(Mat22 R) {
        return R.abs();
    }


    /**
     * Multiply a vector by this matrix.
     *
     * @param v Vector to multiply by matrix.
     * @return Resulting vector
     */
    public final v2 mul(v2 v) {
        return new v2(ex.x * v.x + ey.x * v.y, ex.y * v.x + ey.y * v.y);
    }

    public final void mulToOut(v2 v, v2 out) {
        float tempy = ex.y * v.x + ey.y * v.y;
        out.x = ex.x * v.x + ey.x * v.y;
        out.y = tempy;
    }

    public final void mulToOutUnsafe(v2 v, v2 out) {
        assert (v != out);
        out.x = ex.x * v.x + ey.x * v.y;
        out.y = ex.y * v.x + ey.y * v.y;
    }


    /**
     * Multiply another matrix by this one (this one on left). djm optimized
     *
     * @param R
     * @return
     */
    public final Mat22 mul(Mat22 R) {
        /*
         * Mat22 C = new Mat22();C.setAt(this.mul(R.ex), this.mul(R.ey));return C;
         */
        Mat22 C = new Mat22();
        C.ex.x = ex.x * R.ex.x + ey.x * R.ex.y;
        C.ex.y = ex.y * R.ex.x + ey.y * R.ex.y;
        C.ey.x = ex.x * R.ey.x + ey.x * R.ey.y;
        C.ey.y = ex.y * R.ey.x + ey.y * R.ey.y;
        
        return C;
    }

    final Mat22 mulLocal(Mat22 R) {
        mulToOut(R, this);
        return this;
    }

    private void mulToOut(Mat22 R, Mat22 out) {
        float tempy1 = this.ex.y * R.ex.x + this.ey.y * R.ex.y;
        out.ex.x = this.ex.x * R.ex.x + this.ey.x * R.ex.y;
        out.ex.y = tempy1;
        float tempy2 = this.ex.y * R.ey.x + this.ey.y * R.ey.y;
        out.ey.x = this.ex.x * R.ey.x + this.ey.x * R.ey.y;
        out.ey.y = tempy2;
    }

    public final void mulToOutUnsafe(Mat22 R, Mat22 out) {
        assert (out != R);
        assert (out != this);
        out.ex.x = this.ex.x * R.ex.x + this.ey.x * R.ex.y;
        out.ex.y = this.ex.y * R.ex.x + this.ey.y * R.ex.y;
        out.ey.x = this.ex.x * R.ey.x + this.ey.x * R.ey.y;
        out.ey.y = this.ex.y * R.ey.x + this.ey.y * R.ey.y;
    }

    /**
     * Multiply another matrix by the transpose of this one (transpose of this one on left). djm:
     * optimized
     *
     * @param B
     * @return
     */
    public final Mat22 mulTrans(Mat22 B) {
        /*
         * Vec2 c1 = new Vec2(Vec2.dot(this.ex, B.ex), Vec2.dot(this.ey, B.ex)); Vec2 c2 = new
         * Vec2(Vec2.dot(this.ex, B.ey), Vec2.dot(this.ey, B.ey)); Mat22 C = new Mat22(); C.setAt(c1, c2);
         * return C;
         */
        Mat22 C = new Mat22();

        C.ex.x = v2.dot(this.ex, B.ex);
        C.ex.y = v2.dot(this.ey, B.ex);

        C.ey.x = v2.dot(this.ex, B.ey);
        C.ey.y = v2.dot(this.ey, B.ey);
        return C;
    }

    public final Mat22 mulTransLocal(Mat22 B) {
        mulTransToOut(B, this);
        return this;
    }

    private void mulTransToOut(Mat22 B, Mat22 out) {
        /*
         * out.ex.x = Vec2.dot(this.ex, B.ex); out.ex.y = Vec2.dot(this.ey, B.ex); out.ey.x =
         * Vec2.dot(this.ex, B.ey); out.ey.y = Vec2.dot(this.ey, B.ey);
         */
        float x1 = this.ex.x * B.ex.x + this.ex.y * B.ex.y;
        float y1 = this.ey.x * B.ex.x + this.ey.y * B.ex.y;
        float x2 = this.ex.x * B.ey.x + this.ex.y * B.ey.y;
        float y2 = this.ey.x * B.ey.x + this.ey.y * B.ey.y;
        out.ex.x = x1;
        out.ey.x = x2;
        out.ex.y = y1;
        out.ey.y = y2;
    }

    public final void mulTransToOutUnsafe(Mat22 B, Mat22 out) {
        assert (B != out);
        assert (this != out);
        out.ex.x = this.ex.x * B.ex.x + this.ex.y * B.ex.y;
        out.ey.x = this.ex.x * B.ey.x + this.ex.y * B.ey.y;
        out.ex.y = this.ey.x * B.ex.x + this.ey.y * B.ex.y;
        out.ey.y = this.ey.x * B.ey.x + this.ey.y * B.ey.y;
    }

    /**
     * Multiply a vector by the transpose of this matrix.
     *
     * @param v
     * @return
     */
    public final v2 mulTrans(v2 v) {
        
        return new v2((v.x * ex.x + v.y * ex.y), (v.x * ey.x + v.y * ey.y));
    }

    /* djm added */
    public final void mulTransToOut(v2 v, v2 out) {
        /*
         * out.x = Vec2.dot(v, ex); out.y = Vec2.dot(v, col2);
         */
        float tempx = v.x * ex.x + v.y * ex.y;
        out.y = v.x * ey.x + v.y * ey.y;
        out.x = tempx;
    }




    /**
     * Solve A * x = b where A = this matrix.
     *
     * @return The vector x that solves the above equation.
     */
    public final v2 solve(v2 b) {
        float a11 = ex.x, a12 = ey.x, a21 = ex.y, a22 = ey.y;
        float det = a11 * a22 - a12 * a21;
        if (Math.abs(det) > Float.MIN_NORMAL) {
            det = 1.0f / det;
        }
        return new v2(det * (a22 * b.x - a12 * b.y), det * (a11 * b.y - a21 * b.x));
    }

    public final void solveToOut(v2 b, v2 out) {
        float a11 = ex.x, a12 = ey.x, a21 = ex.y, a22 = ey.y;
        float det = a11 * a22 - a12 * a21;
        if (Math.abs(det) > Float.MIN_NORMAL) {
            det = 1.0f / det;
        }
        float tempy = det * (a11 * b.y - a21 * b.x);
        out.x = det * (a22 * b.x - a12 * b.y);
        out.y = tempy;
    }

    public static v2 mul(Mat22 R, v2 v) {
        
        return new v2(R.ex.x * v.x + R.ey.x * v.y, R.ex.y * v.x + R.ey.y * v.y);
    }

    public static void mulToOut(Mat22 R, v2 v, v2 out) {
        float tempy = R.ex.y * v.x + R.ey.y * v.y;
        out.x = R.ex.x * v.x + R.ey.x * v.y;
        out.y = tempy;
    }

    public static void mulToOutUnsafe(Mat22 R, v2 v, v2 out) {
        assert (v != out);
        out.x = R.ex.x * v.x + R.ey.x * v.y;
        out.y = R.ex.y * v.x + R.ey.y * v.y;
    }

    public static Mat22 mul(Mat22 A, Mat22 B) {
        
        Mat22 C = new Mat22();
        C.ex.x = A.ex.x * B.ex.x + A.ey.x * B.ex.y;
        C.ex.y = A.ex.y * B.ex.x + A.ey.y * B.ex.y;
        C.ey.x = A.ex.x * B.ey.x + A.ey.x * B.ey.y;
        C.ey.y = A.ex.y * B.ey.x + A.ey.y * B.ey.y;
        return C;
    }

    public static void mulToOut(Mat22 A, Mat22 B, Mat22 out) {
        float tempy1 = A.ex.y * B.ex.x + A.ey.y * B.ex.y;
        float tempx1 = A.ex.x * B.ex.x + A.ey.x * B.ex.y;
        float tempy2 = A.ex.y * B.ey.x + A.ey.y * B.ey.y;
        float tempx2 = A.ex.x * B.ey.x + A.ey.x * B.ey.y;
        out.ex.x = tempx1;
        out.ex.y = tempy1;
        out.ey.x = tempx2;
        out.ey.y = tempy2;
    }

    public static void mulToOutUnsafe(Mat22 A, Mat22 B, Mat22 out) {
        assert (out != A);
        assert (out != B);
        out.ex.x = A.ex.x * B.ex.x + A.ey.x * B.ex.y;
        out.ex.y = A.ex.y * B.ex.x + A.ey.y * B.ex.y;
        out.ey.x = A.ex.x * B.ey.x + A.ey.x * B.ey.y;
        out.ey.y = A.ex.y * B.ey.x + A.ey.y * B.ey.y;
    }

    static void createScaleTransform(float scale, Mat22 out) {
        out.ex.x = scale;
        out.ey.y = scale;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ex == null) ? 0 : ex.hashCode());
        result = prime * result + ((ey == null) ? 0 : ey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Mat22 other = (Mat22) obj;
        if (ex == null) {
            if (other.ex != null) return false;
        } else if (!ex.equals(other.ex)) return false;
		return Objects.equals(ey, other.ey);
    }
}
