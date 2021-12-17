/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import jcog.math.VecMathUtil;
import jcog.math.v3;

import java.io.Serializable;

/**
 * A single precision floating point 4 by 4 matrix.
 * Primarily to support 3D rotations.
 */
public class Matrix4f implements Serializable, Cloneable {


    static final long serialVersionUID = -8405036035410109353L;
    /*
    double[] tmp = new double[9];
    double[] tmp_scale = new double[3];
    double[] tmp_rot = new double[9];
    */
    private static final double EPS = 1.0E-8;
    /**
     * The first element of the first row.
     */
    public float m00;
    /**
     * The second element of the first row.
     */
    public float m01;
    /**
     * The third element of the first row.
     */
    public float m02;
    /**
     * The fourth element of the first row.
     */
    public float m03;
    /**
     * The first element of the second row.
     */
    public float m10;
    /**
     * The second element of the second row.
     */
    public float m11;
    /**
     * The third element of the second row.
     */
    public float m12;
    /**
     * The fourth element of the second row.
     */
    public float m13;
    /**
     * The first element of the third row.
     */
    public float m20;
    /**
     * The second element of the third row.
     */
    public float m21;
    /**
     * The third element of the third row.
     */
    public float m22;
    /**
     * The fourth element of the third row.
     */
    public float m23;
    /**
     * The fourth element of the fourth row.
     */
    public float m33;
    /**
     * The first element of the fourth row.
     */
    private float m30;
    /**
     * The second element of the fourth row.
     */
    private float m31;
    /**
     * The third element of the fourth row.
     */
    private float m32;

    /**
     * Constructs and initializes a Matrix4f from the specified 16 values.
     *
     * @param m00 the [0][0] element
     * @param m01 the [0][1] element
     * @param m02 the [0][2] element
     * @param m03 the [0][3] element
     * @param m10 the [1][0] element
     * @param m11 the [1][1] element
     * @param m12 the [1][2] element
     * @param m13 the [1][3] element
     * @param m20 the [2][0] element
     * @param m21 the [2][1] element
     * @param m22 the [2][2] element
     * @param m23 the [2][3] element
     * @param m30 the [3][0] element
     * @param m31 the [3][1] element
     * @param m32 the [3][2] element
     * @param m33 the [3][3] element
     */
    public Matrix4f(float m00, float m01, float m02, float m03,
                    float m10, float m11, float m12, float m13,
                    float m20, float m21, float m22, float m23,
                    float m30, float m31, float m32, float m33) {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;

        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;

        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;

        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;

    }

    /**
     * Constructs and initializes a Matrix4f from the specified 16
     * element array.  this.m00 =v[0], this.m01=v[1], etc.
     *
     * @param v the array of length 16 containing in order
     */
    public Matrix4f(float[] v) {
        this.m00 = v[0];
        this.m01 = v[1];
        this.m02 = v[2];
        this.m03 = v[3];

        this.m10 = v[4];
        this.m11 = v[5];
        this.m12 = v[6];
        this.m13 = v[7];

        this.m20 = v[8];
        this.m21 = v[9];
        this.m22 = v[10];
        this.m23 = v[11];

        this.m30 = v[12];
        this.m31 = v[13];
        this.m32 = v[14];
        this.m33 = v[15];

    }

    /**
     * Constructs and initializes a Matrix4f from the quaternion,
     * translation, and scale values; the scale is applied only to the
     * rotational components of the matrix (upper 3x3) and not to the
     * translational components.
     *
     * @param q1 the quaternion value representing the rotational component
     * @param t1 the translational component of the matrix
     * @param s  the scale value applied to the rotational components
     */
    public Matrix4f(Quat4f q1, v3 t1, float s) {
        m00 = (float) (s * (1.0 - 2.0 * q1.y * q1.y - 2.0 * q1.z * q1.z));
        m10 = (float) (s * (2.0 * (q1.x * q1.y + q1.w * q1.z)));
        m20 = (float) (s * (2.0 * (q1.x * q1.z - q1.w * q1.y)));

        m01 = (float) (s * (2.0 * (q1.x * q1.y - q1.w * q1.z)));
        m11 = (float) (s * (1.0 - 2.0 * q1.x * q1.x - 2.0 * q1.z * q1.z));
        m21 = (float) (s * (2.0 * (q1.y * q1.z + q1.w * q1.x)));

        m02 = (float) (s * (2.0 * (q1.x * q1.z + q1.w * q1.y)));
        m12 = (float) (s * (2.0 * (q1.y * q1.z - q1.w * q1.x)));
        m22 = (float) (s * (1.0 - 2.0 * q1.x * q1.x - 2.0 * q1.y * q1.y));

        m03 = t1.x;
        m13 = t1.y;
        m23 = t1.z;

        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        m33 = 1.0f;

    }


    /**
     * Constructs a new matrix with the same values as the
     * Matrix4f parameter.
     *
     * @param m1 the source matrix
     */
    public Matrix4f(Matrix4f m1) {
        this.m00 = m1.m00;
        this.m01 = m1.m01;
        this.m02 = m1.m02;
        this.m03 = m1.m03;

        this.m10 = m1.m10;
        this.m11 = m1.m11;
        this.m12 = m1.m12;
        this.m13 = m1.m13;

        this.m20 = m1.m20;
        this.m21 = m1.m21;
        this.m22 = m1.m22;
        this.m23 = m1.m23;

        this.m30 = m1.m30;
        this.m31 = m1.m31;
        this.m32 = m1.m32;
        this.m33 = m1.m33;

    }


    /**
     * Constructs and initializes a Matrix4f from the rotation matrix,
     * translation, and scale values; the scale is applied only to the
     * rotational components of the matrix (upper 3x3) and not to the
     * translational components of the matrix.
     *
     * @param m1 the rotation matrix representing the rotational components
     * @param t1 the translational components of the matrix
     * @param s  the scale value applied to the rotational components
     */
    public Matrix4f(Matrix3f m1, v3 t1, float s) {
        this.m00 = m1.m00 * s;
        this.m01 = m1.m01 * s;
        this.m02 = m1.m02 * s;
        this.m03 = t1.x;

        this.m10 = m1.m10 * s;
        this.m11 = m1.m11 * s;
        this.m12 = m1.m12 * s;
        this.m13 = t1.y;

        this.m20 = m1.m20 * s;
        this.m21 = m1.m21 * s;
        this.m22 = m1.m22 * s;
        this.m23 = t1.z;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;

    }


    /**
     * Constructs and initializes a Matrix4f to all zeros.
     */
    public Matrix4f() {
        this.m00 = 0.0f;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m03 = 0.0f;

        this.m10 = 0.0f;
        this.m11 = 0.0f;
        this.m12 = 0.0f;
        this.m13 = 0.0f;

        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = 0.0f;
        this.m23 = 0.0f;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 0.0f;

    }

    /**
     * Given a 4x4 array "matrix0", this function replaces it with the
     * LU decomposition of a row-wise permutation of itself.  The input
     * parameters are "matrix0" and "dimen".  The array "matrix0" is also
     * an output parameter.  The vector "row_perm[4]" is an output
     * parameter that contains the row permutations resulting from partial
     * pivoting.  The output parameter "even_row_xchg" is 1 when the
     * number of row exchanges is even, or -1 otherwise.  Assumes data
     * type is always double.
     * <p>
     * This function is similar to luDecomposition, except that it
     * is tuned specifically for 4x4 matrices.
     *
     * @return true if the matrix is nonsingular, or false otherwise.
     */


    private static boolean luDecomposition(double[] matrix0,
                                           int[] row_perm) {

        double[] row_scale = new double[4];


        {

            int ptr = 0;
            int rs = 0;


            int i = 4;
            while (i-- != 0) {


                int j = 4;
                double big = 0.0;
                while (j-- != 0) {
                    double temp = matrix0[ptr++];
                    temp = Math.abs(temp);
                    if (temp > big) {
                        big = temp;
                    }
                }


                if (big == 0.0) {
                    return false;
                }
                row_scale[rs++] = 1.0 / big;
            }
        }

        int mtx = 0;


        for (int j = 0; j < 4; j++) {
            int i, k;
            int target, p1, p2;
            double sum;


            for (i = 0; i < j; i++) {
                target = mtx + (4 * i) + j;
                sum = matrix0[target];
                k = i;
                p1 = mtx + (4 * i);
                p2 = mtx + j;
                while (k-- != 0) {
                    sum -= matrix0[p1] * matrix0[p2];
                    p1++;
                    p2 += 4;
                }
                matrix0[target] = sum;
            }


            double big = 0.0;
            int imax = -1;
            double temp;
            for (i = j; i < 4; i++) {
                target = mtx + (4 * i) + j;
                sum = matrix0[target];
                k = j;
                p1 = mtx + (4 * i);
                p2 = mtx + j;
                while (k-- != 0) {
                    sum -= matrix0[p1] * matrix0[p2];
                    p1++;
                    p2 += 4;
                }
                matrix0[target] = sum;


                if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
                    big = temp;
                    imax = i;
                }
            }

            if (imax < 0) {
                throw new RuntimeException("Matrix4f13");
            }


            if (j != imax) {

                k = 4;
                p1 = mtx + (4 * imax);
                p2 = mtx + (4 * j);
                while (k-- != 0) {
                    temp = matrix0[p1];
                    matrix0[p1++] = matrix0[p2];
                    matrix0[p2++] = temp;
                }


                row_scale[imax] = row_scale[j];
            }


            row_perm[j] = imax;


            if (matrix0[(mtx + (4 * j) + j)] == 0.0) {
                return false;
            }


            if (j != (4 - 1)) {
                temp = 1.0 / (matrix0[(mtx + (4 * j) + j)]);
                target = mtx + (4 * (j + 1)) + j;
                i = 3 - j;
                while (i-- != 0) {
                    matrix0[target] *= temp;
                    target += 4;
                }
            }
        }

        return true;
    }

    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostionD4x4 and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a 4x4 matrix of floating-point values.  The procedure takes each
     * column of "matrix2" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     * <p>
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     */


    private static void luBacksubstitution(double[] matrix1,
                                           int[] row_perm,
                                           double[] matrix2) {


        int rp = 0;


        for (int k = 0; k < 4; k++) {

            int cv = k;
            int ii = -1;


            int rv;
            for (int i = 0; i < 4; i++) {

                int ip = row_perm[rp + i];
                double sum = matrix2[cv + 4 * ip];
                matrix2[cv + 4 * ip] = matrix2[cv + 4 * i];
                if (ii >= 0) {

                    rv = i * 4;
                    for (int j = ii; j <= i - 1; j++) {
                        sum -= matrix1[rv + j] * matrix2[cv + 4 * j];
                    }
                } else if (sum != 0.0) {
                    ii = i;
                }
                matrix2[cv + 4 * i] = sum;
            }


            rv = 3 * 4;
            matrix2[cv + 4 * 3] /= matrix1[rv + 3];

            rv -= 4;
            matrix2[cv + 4 * 2] = (matrix2[cv + 4 * 2] -
                    matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv + 2];

            rv -= 4;
            matrix2[cv + 4] = (matrix2[cv + 4] -
                    matrix1[rv + 2] * matrix2[cv + 4 * 2] -
                    matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv + 1];

            rv -= 4;
            matrix2[cv] = (matrix2[cv] -
                    matrix1[rv + 1] * matrix2[cv + 4] -
                    matrix1[rv + 2] * matrix2[cv + 4 * 2] -
                    matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv];
        }
    }

    /**
     * Returns a string that contains the values of this Matrix4f.
     *
     * @return the String representation
     */
    public String toString() {
        return
                this.m00 + ", " + this.m01 + ", " + this.m02 + ", " + this.m03 + '\n' +
                        this.m10 + ", " + this.m11 + ", " + this.m12 + ", " + this.m13 + '\n' +
                        this.m20 + ", " + this.m21 + ", " + this.m22 + ", " + this.m23 + '\n' +
                        this.m30 + ", " + this.m31 + ", " + this.m32 + ", " + this.m33 + '\n';
    }

    /**
     * Sets this Matrix4f to identity.
     */
    public final void setIdentity() {
        this.m00 = 1.0f;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m03 = 0.0f;

        this.m10 = 0.0f;
        this.m11 = 1.0f;
        this.m12 = 0.0f;
        this.m13 = 0.0f;

        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = 1.0f;
        this.m23 = 0.0f;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the specified element of this matrix4f to the value provided.
     *
     * @param row    the row number to be modified (zero indexed)
     * @param column the column number to be modified (zero indexed)
     * @param value  the new value
     */
    public final void setElement(int row, int column, float value) {
        switch (row) {
            case 0:
                switch (column) {
                    case 0 -> this.m00 = value;
                    case 1 -> this.m01 = value;
                    case 2 -> this.m02 = value;
                    case 3 -> this.m03 = value;
                    default -> new ArrayIndexOutOfBoundsException("Matrix4f");
                }
                break;

            case 1:
                switch (column) {
                    case 0 -> this.m10 = value;
                    case 1 -> this.m11 = value;
                    case 2 -> this.m12 = value;
                    case 3 -> this.m13 = value;
                    default -> new ArrayIndexOutOfBoundsException("Matrix4f");
                }
                break;

            case 2:
                switch (column) {
                    case 0 -> this.m20 = value;
                    case 1 -> this.m21 = value;
                    case 2 -> this.m22 = value;
                    case 3 -> this.m23 = value;
                    default -> new ArrayIndexOutOfBoundsException("Matrix4f");
                }
                break;

            case 3:
                switch (column) {
                    case 0 -> this.m30 = value;
                    case 1 -> this.m31 = value;
                    case 2 -> this.m32 = value;
                    case 3 -> this.m33 = value;
                    default -> new ArrayIndexOutOfBoundsException("Matrix4f");
                }
                break;

            default:
                throw new ArrayIndexOutOfBoundsException("Matrix4f0");
        }
    }

    /**
     * Retrieves the value at the specified row and column of this matrix.
     *
     * @param row    the row number to be retrieved (zero indexed)
     * @param column the column number to be retrieved (zero indexed)
     * @return the value at the indexed element
     */
    public final float getElement(int row, int column) {
        float result = 0;
        boolean finished = false;
        switch (row) {
            case 0:
                switch (column) {
                    case 0:
                        result = (this.m00);
                        finished = true;
                        break;
                    case 1:
                        result = (this.m01);
                        finished = true;
                        break;
                    case 2:
                        result = (this.m02);
                        finished = true;
                        break;
                    case 3:
                        result = (this.m03);
                        finished = true;
                        break;
                    default:
                        break;
                }
                if (finished) break;

                break;
            case 1:
                switch (column) {
                    case 0:
                        result = (this.m10);
                        finished = true;
                        break;
                    case 1:
                        result = (this.m11);
                        finished = true;
                        break;
                    case 2:
                        result = (this.m12);
                        finished = true;
                        break;
                    case 3:
                        result = (this.m13);
                        finished = true;
                        break;
                    default:
                        break;
                }
                if (finished) break;

                break;

            case 2:
                switch (column) {
                    case 0:
                        result = (this.m20);
                        finished = true;
                        break;
                    case 1:
                        result = (this.m21);
                        finished = true;
                        break;
                    case 2:
                        result = (this.m22);
                        finished = true;
                        break;
                    case 3:
                        result = (this.m23);
                        finished = true;
                        break;
                    default:
                        break;
                }
                if (finished) break;

                break;

            case 3:
                switch (column) {
                    case 0:
                        result = (this.m30);
                        finished = true;
                        break;
                    case 1:
                        result = (this.m31);
                        finished = true;
                        break;
                    case 2:
                        result = (this.m32);
                        finished = true;
                        break;
                    case 3:
                        result = (this.m33);
                        finished = true;
                        break;
                    default:
                        break;
                }
                if (finished) break;

                break;

            default:
                break;
        }
        if (!finished) {
            throw new ArrayIndexOutOfBoundsException("Matrix4f1");
        }
        return result;
    }

    /**
     * Copies the matrix values in the specified row into the vector parameter.
     *
     * @param row the matrix row
     * @param v   the vector into which the matrix row values will be copied
     */
    public final void getRow(int row, Vector4f v) {
        switch (row) {
            case 0 -> {
                v.x = m00;
                v.y = m01;
                v.z = m02;
                v.w = m03;
            }
            case 1 -> {
                v.x = m10;
                v.y = m11;
                v.z = m12;
                v.w = m13;
            }
            case 2 -> {
                v.x = m20;
                v.y = m21;
                v.z = m22;
                v.w = m23;
            }
            case 3 -> {
                v.x = m30;
                v.y = m31;
                v.z = m32;
                v.w = m33;
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }

    }

    /**
     * Copies the matrix values in the specified row into the array parameter.
     *
     * @param row the matrix row
     * @param v   the array into which the matrix row values will be copied
     */
    public final void getRow(int row, float[] v) {
        switch (row) {
            case 0 -> {
                v[0] = m00;
                v[1] = m01;
                v[2] = m02;
                v[3] = m03;
            }
            case 1 -> {
                v[0] = m10;
                v[1] = m11;
                v[2] = m12;
                v[3] = m13;
            }
            case 2 -> {
                v[0] = m20;
                v[1] = m21;
                v[2] = m22;
                v[3] = m23;
            }
            case 3 -> {
                v[0] = m30;
                v[1] = m31;
                v[2] = m32;
                v[3] = m33;
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }

    }

    /**
     * Copies the matrix values in the specified column into the vector
     * parameter.
     *
     * @param column the matrix column
     * @param v      the vector into which the matrix row values will be copied
     */
    public final void getColumn(int column, Vector4f v) {
        switch (column) {
            case 0 -> {
                v.x = m00;
                v.y = m10;
                v.z = m20;
                v.w = m30;
            }
            case 1 -> {
                v.x = m01;
                v.y = m11;
                v.z = m21;
                v.w = m31;
            }
            case 2 -> {
                v.x = m02;
                v.y = m12;
                v.z = m22;
                v.w = m32;
            }
            case 3 -> {
                v.x = m03;
                v.y = m13;
                v.z = m23;
                v.w = m33;
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }

    }

    /**
     * Copies the matrix values in the specified column into the array
     * parameter.
     *
     * @param column the matrix column
     * @param v      the array into which the matrix row values will be copied
     */
    public final void getColumn(int column, float[] v) {
        switch (column) {
            case 0 -> {
                v[0] = m00;
                v[1] = m10;
                v[2] = m20;
                v[3] = m30;
            }
            case 1 -> {
                v[0] = m01;
                v[1] = m11;
                v[2] = m21;
                v[3] = m31;
            }
            case 2 -> {
                v[0] = m02;
                v[1] = m12;
                v[2] = m22;
                v[3] = m32;
            }
            case 3 -> {
                v[0] = m03;
                v[1] = m13;
                v[2] = m23;
                v[3] = m33;
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }

    }

    /**
     * Retrieves the translational components of this matrix.
     *
     * @param trans the vector that will receive the translational component
     */
    public final void get(v3 trans) {
        trans.x = m03;
        trans.y = m13;
        trans.z = m23;
    }

    /**
     * Gets the upper 3x3 values of this matrix and places them into
     * the matrix m1.
     *
     * @param m1 the matrix that will hold the values
     */
    public final void getRotationScale(Matrix3f m1) {
        m1.m00 = m00;
        m1.m01 = m01;
        m1.m02 = m02;
        m1.m10 = m10;
        m1.m11 = m11;
        m1.m12 = m12;
        m1.m20 = m20;
        m1.m21 = m21;
        m1.m22 = m22;
    }

    /**
     * Replaces the upper 3x3 matrix values of this matrix with the
     * values in the matrix m1.
     *
     * @param m1 the matrix that will be the new upper 3x3
     */
    public final void setRotationScale(Matrix3f m1) {
        m00 = m1.m00;
        m01 = m1.m01;
        m02 = m1.m02;
        m10 = m1.m10;
        m11 = m1.m11;
        m12 = m1.m12;
        m20 = m1.m20;
        m21 = m1.m21;
        m22 = m1.m22;
    }

    /**
     * Sets the specified row of this matrix4f to the four values provided.
     *
     * @param row the row number to be modified (zero indexed)
     * @param x   the first column element
     * @param y   the second column element
     * @param z   the third column element
     * @param w   the fourth column element
     */
    public final void setRow(int row, float x, float y, float z, float w) {
        switch (row) {
            case 0 -> {
                this.m00 = x;
                this.m01 = y;
                this.m02 = z;
                this.m03 = w;
            }
            case 1 -> {
                this.m10 = x;
                this.m11 = y;
                this.m12 = z;
                this.m13 = w;
            }
            case 2 -> {
                this.m20 = x;
                this.m21 = y;
                this.m22 = z;
                this.m23 = w;
            }
            case 3 -> {
                this.m30 = x;
                this.m31 = y;
                this.m32 = z;
                this.m33 = w;
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }
    }

    /**
     * Sets the specified row of this matrix4f to the Vector provided.
     *
     * @param row the row number to be modified (zero indexed)
     * @param v   the replacement row
     */
    public final void setRow(int row, Vector4f v) {
        switch (row) {
            case 0 -> {
                this.m00 = v.x;
                this.m01 = v.y;
                this.m02 = v.z;
                this.m03 = v.w;
            }
            case 1 -> {
                this.m10 = v.x;
                this.m11 = v.y;
                this.m12 = v.z;
                this.m13 = v.w;
            }
            case 2 -> {
                this.m20 = v.x;
                this.m21 = v.y;
                this.m22 = v.z;
                this.m23 = v.w;
            }
            case 3 -> {
                this.m30 = v.x;
                this.m31 = v.y;
                this.m32 = v.z;
                this.m33 = v.w;
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }
    }

    /**
     * Sets the specified row of this matrix4f to the four values provided
     * in the passed array.
     *
     * @param row the row number to be modified (zero indexed)
     * @param v   the replacement row
     */
    public final void setRow(int row, float[] v) {
        switch (row) {
            case 0 -> {
                this.m00 = v[0];
                this.m01 = v[1];
                this.m02 = v[2];
                this.m03 = v[3];
            }
            case 1 -> {
                this.m10 = v[0];
                this.m11 = v[1];
                this.m12 = v[2];
                this.m13 = v[3];
            }
            case 2 -> {
                this.m20 = v[0];
                this.m21 = v[1];
                this.m22 = v[2];
                this.m23 = v[3];
            }
            case 3 -> {
                this.m30 = v[0];
                this.m31 = v[1];
                this.m32 = v[2];
                this.m33 = v[3];
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }
    }

    /**
     * Sets the specified column of this matrix4f to the four values provided.
     *
     * @param column the column number to be modified (zero indexed)
     * @param x      the first row element
     * @param y      the second row element
     * @param z      the third row element
     * @param w      the fourth row element
     */
    public final void setColumn(int column, float x, float y, float z, float w) {
        switch (column) {
            case 0 -> {
                this.m00 = x;
                this.m10 = y;
                this.m20 = z;
                this.m30 = w;
            }
            case 1 -> {
                this.m01 = x;
                this.m11 = y;
                this.m21 = z;
                this.m31 = w;
            }
            case 2 -> {
                this.m02 = x;
                this.m12 = y;
                this.m22 = z;
                this.m32 = w;
            }
            case 3 -> {
                this.m03 = x;
                this.m13 = y;
                this.m23 = z;
                this.m33 = w;
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }
    }

    /**
     * Sets the specified column of this matrix4f to the vector provided.
     *
     * @param column the column number to be modified (zero indexed)
     * @param v      the replacement column
     */
    public final void setColumn(int column, Vector4f v) {
        switch (column) {
            case 0 -> {
                this.m00 = v.x;
                this.m10 = v.y;
                this.m20 = v.z;
                this.m30 = v.w;
            }
            case 1 -> {
                this.m01 = v.x;
                this.m11 = v.y;
                this.m21 = v.z;
                this.m31 = v.w;
            }
            case 2 -> {
                this.m02 = v.x;
                this.m12 = v.y;
                this.m22 = v.z;
                this.m32 = v.w;
            }
            case 3 -> {
                this.m03 = v.x;
                this.m13 = v.y;
                this.m23 = v.z;
                this.m33 = v.w;
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }
    }

    /**
     * Sets the specified column of this matrix4f to the four values provided.
     *
     * @param column the column number to be modified (zero indexed)
     * @param v      the replacement column
     */
    public final void setColumn(int column, float[] v) {
        switch (column) {
            case 0 -> {
                this.m00 = v[0];
                this.m10 = v[1];
                this.m20 = v[2];
                this.m30 = v[3];
            }
            case 1 -> {
                this.m01 = v[0];
                this.m11 = v[1];
                this.m21 = v[2];
                this.m31 = v[3];
            }
            case 2 -> {
                this.m02 = v[0];
                this.m12 = v[1];
                this.m22 = v[2];
                this.m32 = v[3];
            }
            case 3 -> {
                this.m03 = v[0];
                this.m13 = v[1];
                this.m23 = v[2];
                this.m33 = v[3];
            }
            default -> new ArrayIndexOutOfBoundsException("Matrix4f");
        }
    }

    /**
     * Adds a scalar to each component of this matrix.
     *
     * @param scalar the scalar adder
     */
    public final void add(float scalar) {
        m00 += scalar;
        m01 += scalar;
        m02 += scalar;
        m03 += scalar;
        m10 += scalar;
        m11 += scalar;
        m12 += scalar;
        m13 += scalar;
        m20 += scalar;
        m21 += scalar;
        m22 += scalar;
        m23 += scalar;
        m30 += scalar;
        m31 += scalar;
        m32 += scalar;
        m33 += scalar;
    }

    /**
     * Adds a scalar to each component of the matrix m1 and places
     * the result into this.  Matrix m1 is not modified.
     *
     * @param scalar the scalar adder
     * @param m1     the original matrix values
     */
    public final void add(float scalar, Matrix4f m1) {
        this.m00 = m1.m00 + scalar;
        this.m01 = m1.m01 + scalar;
        this.m02 = m1.m02 + scalar;
        this.m03 = m1.m03 + scalar;
        this.m10 = m1.m10 + scalar;
        this.m11 = m1.m11 + scalar;
        this.m12 = m1.m12 + scalar;
        this.m13 = m1.m13 + scalar;
        this.m20 = m1.m20 + scalar;
        this.m21 = m1.m21 + scalar;
        this.m22 = m1.m22 + scalar;
        this.m23 = m1.m23 + scalar;
        this.m30 = m1.m30 + scalar;
        this.m31 = m1.m31 + scalar;
        this.m32 = m1.m32 + scalar;
        this.m33 = m1.m33 + scalar;
    }

    /**
     * Sets the value of this matrix to the matrix sum of matrices m1 and m2.
     *
     * @param m1 the first matrix
     * @param m2 the second matrix
     */
    public final void add(Matrix4f m1, Matrix4f m2) {
        this.m00 = m1.m00 + m2.m00;
        this.m01 = m1.m01 + m2.m01;
        this.m02 = m1.m02 + m2.m02;
        this.m03 = m1.m03 + m2.m03;

        this.m10 = m1.m10 + m2.m10;
        this.m11 = m1.m11 + m2.m11;
        this.m12 = m1.m12 + m2.m12;
        this.m13 = m1.m13 + m2.m13;

        this.m20 = m1.m20 + m2.m20;
        this.m21 = m1.m21 + m2.m21;
        this.m22 = m1.m22 + m2.m22;
        this.m23 = m1.m23 + m2.m23;

        this.m30 = m1.m30 + m2.m30;
        this.m31 = m1.m31 + m2.m31;
        this.m32 = m1.m32 + m2.m32;
        this.m33 = m1.m33 + m2.m33;
    }

    /**
     * Sets the value of this matrix to the sum of itself and matrix m1.
     *
     * @param m1 the other matrix
     */
    public final void add(Matrix4f m1) {
        this.m00 += m1.m00;
        this.m01 += m1.m01;
        this.m02 += m1.m02;
        this.m03 += m1.m03;

        this.m10 += m1.m10;
        this.m11 += m1.m11;
        this.m12 += m1.m12;
        this.m13 += m1.m13;

        this.m20 += m1.m20;
        this.m21 += m1.m21;
        this.m22 += m1.m22;
        this.m23 += m1.m23;

        this.m30 += m1.m30;
        this.m31 += m1.m31;
        this.m32 += m1.m32;
        this.m33 += m1.m33;
    }

    /**
     * Performs an element-by-element subtraction of matrix m2 from
     * matrix m1 and places the result into matrix this (this =
     * m2 - m1).
     *
     * @param m1 the first matrix
     * @param m2 the second matrix
     */
    public final void sub(Matrix4f m1, Matrix4f m2) {
        this.m00 = m1.m00 - m2.m00;
        this.m01 = m1.m01 - m2.m01;
        this.m02 = m1.m02 - m2.m02;
        this.m03 = m1.m03 - m2.m03;

        this.m10 = m1.m10 - m2.m10;
        this.m11 = m1.m11 - m2.m11;
        this.m12 = m1.m12 - m2.m12;
        this.m13 = m1.m13 - m2.m13;

        this.m20 = m1.m20 - m2.m20;
        this.m21 = m1.m21 - m2.m21;
        this.m22 = m1.m22 - m2.m22;
        this.m23 = m1.m23 - m2.m23;

        this.m30 = m1.m30 - m2.m30;
        this.m31 = m1.m31 - m2.m31;
        this.m32 = m1.m32 - m2.m32;
        this.m33 = m1.m33 - m2.m33;
    }

    /**
     * Sets this matrix to the matrix difference of itself and
     * matrix m1 (this = this - m1).
     *
     * @param m1 the other matrix
     */
    public final void sub(Matrix4f m1) {
        this.m00 -= m1.m00;
        this.m01 -= m1.m01;
        this.m02 -= m1.m02;
        this.m03 -= m1.m03;

        this.m10 -= m1.m10;
        this.m11 -= m1.m11;
        this.m12 -= m1.m12;
        this.m13 -= m1.m13;

        this.m20 -= m1.m20;
        this.m21 -= m1.m21;
        this.m22 -= m1.m22;
        this.m23 -= m1.m23;

        this.m30 -= m1.m30;
        this.m31 -= m1.m31;
        this.m32 -= m1.m32;
        this.m33 -= m1.m33;
    }

    /**
     * Sets the value of this matrix to its transpose in place.
     */
    private void transpose() {

        float temp = this.m10;
        this.m10 = this.m01;
        this.m01 = temp;

        temp = this.m20;
        this.m20 = this.m02;
        this.m02 = temp;

        temp = this.m30;
        this.m30 = this.m03;
        this.m03 = temp;

        temp = this.m21;
        this.m21 = this.m12;
        this.m12 = temp;

        temp = this.m31;
        this.m31 = this.m13;
        this.m13 = temp;

        temp = this.m32;
        this.m32 = this.m23;
        this.m23 = temp;
    }

    /**
     * Sets the value of this matrix to the transpose of the argument matrix.
     *
     * @param m1 the matrix to be transposed
     */
    public final void transpose(Matrix4f m1) {
        if (this != m1) {
            this.m00 = m1.m00;
            this.m01 = m1.m10;
            this.m02 = m1.m20;
            this.m03 = m1.m30;

            this.m10 = m1.m01;
            this.m11 = m1.m11;
            this.m12 = m1.m21;
            this.m13 = m1.m31;

            this.m20 = m1.m02;
            this.m21 = m1.m12;
            this.m22 = m1.m22;
            this.m23 = m1.m32;

            this.m30 = m1.m03;
            this.m31 = m1.m13;
            this.m32 = m1.m23;
            this.m33 = m1.m33;
        } else
            this.transpose();
    }

    /**
     * Sets the value of this matrix to the matrix conversion of the
     * single precision quaternion argument.
     *
     * @param q1 the quaternion to be converted
     */
    public final void set(Quat4f q1) {
        this.m00 = (1.0f - 2.0f * q1.y * q1.y - 2.0f * q1.z * q1.z);
        this.m10 = (2.0f * (q1.x * q1.y + q1.w * q1.z));
        this.m20 = (2.0f * (q1.x * q1.z - q1.w * q1.y));

        this.m01 = (2.0f * (q1.x * q1.y - q1.w * q1.z));
        this.m11 = (1.0f - 2.0f * q1.x * q1.x - 2.0f * q1.z * q1.z);
        this.m21 = (2.0f * (q1.y * q1.z + q1.w * q1.x));

        this.m02 = (2.0f * (q1.x * q1.z + q1.w * q1.y));
        this.m12 = (2.0f * (q1.y * q1.z - q1.w * q1.x));
        this.m22 = (1.0f - 2.0f * q1.x * q1.x - 2.0f * q1.y * q1.y);

        this.m03 = 0.0f;
        this.m13 = 0.0f;
        this.m23 = 0.0f;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the value of this matrix to the matrix conversion of the
     * (single precision) axis and angle argument.
     *
     * @param a1 the axis and angle to be converted
     */
    public final void set(AxisAngle4f a1) {
        float mag = (float) Math.sqrt(a1.x * a1.x + a1.y * a1.y + a1.z * a1.z);
        if (mag < EPS) {
            m00 = 1.0f;
            m01 = 0.0f;
            m02 = 0.0f;

            m10 = 0.0f;
            m11 = 1.0f;
            m12 = 0.0f;

            m20 = 0.0f;
            m21 = 0.0f;
            m22 = 1.0f;
        } else {
            mag = 1.0f / mag;
            float ax = a1.x * mag;
            float ay = a1.y * mag;
            float az = a1.z * mag;

            float sinTheta = (float) Math.sin(a1.angle);
            float cosTheta = (float) Math.cos(a1.angle);
            float t = 1.0f - cosTheta;

            m00 = t * ax * ax + cosTheta;
            float xy = ax * ay;
            m01 = t * xy - sinTheta * az;
            float xz = ax * az;
            m02 = t * xz + sinTheta * ay;

            m10 = t * xy + sinTheta * az;
            m11 = t * ay * ay + cosTheta;
            float yz = ay * az;
            m12 = t * yz - sinTheta * ax;

            m20 = t * xz - sinTheta * ay;
            m21 = t * yz + sinTheta * ax;
            m22 = t * az * az + cosTheta;
        }
        m03 = 0.0f;
        m13 = 0.0f;
        m23 = 0.0f;

        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        m33 = 1.0f;
    }

    /**
     * Sets the value of this matrix from the rotation expressed
     * by the quaternion q1, the translation t1, and the scale s.
     *
     * @param q1 the rotation expressed as a quaternion
     * @param t1 the translation
     * @param s  the scale value
     */
    public final void set(Quat4f q1, v3 t1, float s) {
        this.m00 = (s * (1.0f - 2.0f * q1.y * q1.y - 2.0f * q1.z * q1.z));
        this.m10 = (s * (2.0f * (q1.x * q1.y + q1.w * q1.z)));
        this.m20 = (s * (2.0f * (q1.x * q1.z - q1.w * q1.y)));

        this.m01 = (s * (2.0f * (q1.x * q1.y - q1.w * q1.z)));
        this.m11 = (s * (1.0f - 2.0f * q1.x * q1.x - 2.0f * q1.z * q1.z));
        this.m21 = (s * (2.0f * (q1.y * q1.z + q1.w * q1.x)));

        this.m02 = (s * (2.0f * (q1.x * q1.z + q1.w * q1.y)));
        this.m12 = (s * (2.0f * (q1.y * q1.z - q1.w * q1.x)));
        this.m22 = (s * (1.0f - 2.0f * q1.x * q1.x - 2.0f * q1.y * q1.y));

        this.m03 = t1.x;
        this.m13 = t1.y;
        this.m23 = t1.z;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the value of this matrix to a copy of the
     * passed matrix m1.
     *
     * @param m1 the matrix to be copied
     */
    public final void set(Matrix4f m1) {
        this.m00 = m1.m00;
        this.m01 = m1.m01;
        this.m02 = m1.m02;
        this.m03 = m1.m03;

        this.m10 = m1.m10;
        this.m11 = m1.m11;
        this.m12 = m1.m12;
        this.m13 = m1.m13;

        this.m20 = m1.m20;
        this.m21 = m1.m21;
        this.m22 = m1.m22;
        this.m23 = m1.m23;

        this.m30 = m1.m30;
        this.m31 = m1.m31;
        this.m32 = m1.m32;
        this.m33 = m1.m33;
    }

    /**
     * Sets the value of this matrix to the matrix inverse
     * of the passed (user declared) matrix m1.
     *
     * @param m1 the matrix to be inverted
     */
    public final void invert(Matrix4f m1) {

        invertGeneral(m1);
    }

    /**
     * Inverts this matrix in place.
     */
    public final void invert() {
        invertGeneral(this);
    }

    /**
     * General invert routine.  Inverts m1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     * <p>
     * Also note that since this routine is slow anyway, we won't worry
     * about allocating a little bit of garbage.
     */
    private void invertGeneral(Matrix4f m1) {
        double[] temp = new double[16];


        temp[0] = m1.m00;
        temp[1] = m1.m01;
        temp[2] = m1.m02;
        temp[3] = m1.m03;

        temp[4] = m1.m10;
        temp[5] = m1.m11;
        temp[6] = m1.m12;
        temp[7] = m1.m13;

        temp[8] = m1.m20;
        temp[9] = m1.m21;
        temp[10] = m1.m22;
        temp[11] = m1.m23;

        temp[12] = m1.m30;
        temp[13] = m1.m31;
        temp[14] = m1.m32;
        temp[15] = m1.m33;


        int[] row_perm = new int[4];
        if (!luDecomposition(temp, row_perm)) {

            throw new SingularMatrixException("Matrix4f12");
        }


        double[] result = new double[16];
        for (int i = 0; i < 16; i++) result[i] = 0.0;
        result[0] = 1.0;
        result[5] = 1.0;
        result[10] = 1.0;
        result[15] = 1.0;
        luBacksubstitution(temp, row_perm, result);

        this.m00 = (float) result[0];
        this.m01 = (float) result[1];
        this.m02 = (float) result[2];
        this.m03 = (float) result[3];

        this.m10 = (float) result[4];
        this.m11 = (float) result[5];
        this.m12 = (float) result[6];
        this.m13 = (float) result[7];

        this.m20 = (float) result[8];
        this.m21 = (float) result[9];
        this.m22 = (float) result[10];
        this.m23 = (float) result[11];

        this.m30 = (float) result[12];
        this.m31 = (float) result[13];
        this.m32 = (float) result[14];
        this.m33 = (float) result[15];

    }

    /**
     * Computes the determinate of this matrix.
     *
     * @return the determinate of the matrix
     */
    public final float determinant() {


        float det = m00 * (m11 * m22 * m33 + m12 * m23 * m31 + m13 * m21 * m32
                - m13 * m22 * m31 - m11 * m23 * m32 - m12 * m21 * m33);
        det -= m01 * (m10 * m22 * m33 + m12 * m23 * m30 + m13 * m20 * m32
                - m13 * m22 * m30 - m10 * m23 * m32 - m12 * m20 * m33);
        det += m02 * (m10 * m21 * m33 + m11 * m23 * m30 + m13 * m20 * m31
                - m13 * m21 * m30 - m10 * m23 * m31 - m11 * m20 * m33);
        det -= m03 * (m10 * m21 * m32 + m11 * m22 * m30 + m12 * m20 * m31
                - m12 * m21 * m30 - m10 * m22 * m31 - m11 * m20 * m32);

        return (det);
    }

    /**
     * Sets the rotational component (upper 3x3) of this matrix to the
     * matrix values in the single precision Matrix3f argument; the other
     * elements of this matrix are initialized as if this were an identity
     * matrix (i.e., affine matrix with no translational component).
     *
     * @param m1 the single-precision 3x3 matrix
     */
    public final void set(Matrix3f m1) {
        m00 = m1.m00;
        m01 = m1.m01;
        m02 = m1.m02;
        m03 = 0.0f;
        m10 = m1.m10;
        m11 = m1.m11;
        m12 = m1.m12;
        m13 = 0.0f;
        m20 = m1.m20;
        m21 = m1.m21;
        m22 = m1.m22;
        m23 = 0.0f;
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        m33 = 1.0f;
    }


    /**
     * Sets the value of this matrix to a scale matrix with the
     * the passed scale amount.
     *
     * @param scale the scale factor for the matrix
     */
    public final void set(float scale) {
        this.m00 = scale;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m03 = 0.0f;

        this.m10 = 0.0f;
        this.m11 = scale;
        this.m12 = 0.0f;
        this.m13 = 0.0f;

        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = scale;
        this.m23 = 0.0f;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the values in this Matrix4f equal to the row-major
     * array parameter (ie, the first four elements of the
     * array will be copied into the first row of this matrix, etc.).
     *
     * @param m the single precision array of length 16
     */
    public final void set(float[] m) {
        m00 = m[0];
        m01 = m[1];
        m02 = m[2];
        m03 = m[3];
        m10 = m[4];
        m11 = m[5];
        m12 = m[6];
        m13 = m[7];
        m20 = m[8];
        m21 = m[9];
        m22 = m[10];
        m23 = m[11];
        m30 = m[12];
        m31 = m[13];
        m32 = m[14];
        m33 = m[15];
    }

    /**
     * Sets the value of this matrix to a translate matrix with
     * the passed translation value.
     *
     * @param v1 the translation amount
     */
    public final void set(v3 v1) {
        this.m00 = 1.0f;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m03 = v1.x;

        this.m10 = 0.0f;
        this.m11 = 1.0f;
        this.m12 = 0.0f;
        this.m13 = v1.y;

        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = 1.0f;
        this.m23 = v1.z;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the value of this transform to a scale and translation matrix;
     * the scale is not applied to the translation and all of the matrix
     * values are modified.
     *
     * @param scale the scale factor for the matrix
     * @param t1    the translation amount
     */
    public final void set(float scale, v3 t1) {
        this.m00 = scale;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m03 = t1.x;

        this.m10 = 0.0f;
        this.m11 = scale;
        this.m12 = 0.0f;
        this.m13 = t1.y;

        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = scale;
        this.m23 = t1.z;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the value of this transform to a scale and translation matrix;
     * the translation is scaled by the scale factor and all of the matrix
     * values are modified.
     *
     * @param t1    the translation amount
     * @param scale the scale factor for the matrix
     */
    public final void set(v3 t1, float scale) {
        this.m00 = scale;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m03 = scale * t1.x;

        this.m10 = 0.0f;
        this.m11 = scale;
        this.m12 = 0.0f;
        this.m13 = scale * t1.y;

        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = scale;
        this.m23 = scale * t1.z;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the value of this matrix from the rotation expressed by
     * the rotation matrix m1, the translation t1, and the scale factor.
     * The translation is not modified by the scale.
     *
     * @param m1    the rotation component
     * @param t1    the translation component
     * @param scale the scale component
     */
    public final void set(Matrix3f m1, v3 t1, float scale) {
        this.m00 = m1.m00 * scale;
        this.m01 = m1.m01 * scale;
        this.m02 = m1.m02 * scale;
        this.m03 = t1.x;

        this.m10 = m1.m10 * scale;
        this.m11 = m1.m11 * scale;
        this.m12 = m1.m12 * scale;
        this.m13 = t1.y;

        this.m20 = m1.m20 * scale;
        this.m21 = m1.m21 * scale;
        this.m22 = m1.m22 * scale;
        this.m23 = t1.z;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }


    /**
     * Modifies the translational components of this matrix to the values
     * of the Vector3f argument; the other values of this matrix are not
     * modified.
     *
     * @param trans the translational component
     */
    public final void setTranslation(v3 trans) {
        m03 = trans.x;
        m13 = trans.y;
        m23 = trans.z;
    }


    /**
     * Sets the value of this matrix to a counter clockwise rotation
     * about the x axis.
     *
     * @param angle the angle to rotate about the X axis in radians
     */
    public final void rotX(float angle) {

        float sinAngle = (float) Math.sin(angle);
        float cosAngle = (float) Math.cos(angle);

        this.m00 = 1.0f;
        this.m01 = 0.0f;
        this.m02 = 0.0f;
        this.m03 = 0.0f;

        this.m10 = 0.0f;
        this.m11 = cosAngle;
        this.m12 = -sinAngle;
        this.m13 = 0.0f;

        this.m20 = 0.0f;
        this.m21 = sinAngle;
        this.m22 = cosAngle;
        this.m23 = 0.0f;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation
     * about the y axis.
     *
     * @param angle the angle to rotate about the Y axis in radians
     */
    public final void rotY(float angle) {

        float sinAngle = (float) Math.sin(angle);
        float cosAngle = (float) Math.cos(angle);

        this.m00 = cosAngle;
        this.m01 = 0.0f;
        this.m02 = sinAngle;
        this.m03 = 0.0f;

        this.m10 = 0.0f;
        this.m11 = 1.0f;
        this.m12 = 0.0f;
        this.m13 = 0.0f;

        this.m20 = -sinAngle;
        this.m21 = 0.0f;
        this.m22 = cosAngle;
        this.m23 = 0.0f;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Sets the value of this matrix to a counter clockwise rotation
     * about the z axis.
     *
     * @param angle the angle to rotate about the Z axis in radians
     */
    public final void rotZ(float angle) {

        float sinAngle = (float) Math.sin(angle);
        float cosAngle = (float) Math.cos(angle);

        this.m00 = cosAngle;
        this.m01 = -sinAngle;
        this.m02 = 0.0f;
        this.m03 = 0.0f;

        this.m10 = sinAngle;
        this.m11 = cosAngle;
        this.m12 = 0.0f;
        this.m13 = 0.0f;

        this.m20 = 0.0f;
        this.m21 = 0.0f;
        this.m22 = 1.0f;
        this.m23 = 0.0f;

        this.m30 = 0.0f;
        this.m31 = 0.0f;
        this.m32 = 0.0f;
        this.m33 = 1.0f;
    }

    /**
     * Multiplies each element of this matrix by a scalar.
     *
     * @param scalar the scalar multiplier.
     */
    public final void mul(float scalar) {
        m00 *= scalar;
        m01 *= scalar;
        m02 *= scalar;
        m03 *= scalar;
        m10 *= scalar;
        m11 *= scalar;
        m12 *= scalar;
        m13 *= scalar;
        m20 *= scalar;
        m21 *= scalar;
        m22 *= scalar;
        m23 *= scalar;
        m30 *= scalar;
        m31 *= scalar;
        m32 *= scalar;
        m33 *= scalar;
    }

    /**
     * Multiplies each element of matrix m1 by a scalar and places
     * the result into this.  Matrix m1 is not modified.
     *
     * @param scalar the scalar multiplier.
     * @param m1     the original matrix.
     */
    public final void mul(float scalar, Matrix4f m1) {
        this.m00 = m1.m00 * scalar;
        this.m01 = m1.m01 * scalar;
        this.m02 = m1.m02 * scalar;
        this.m03 = m1.m03 * scalar;
        this.m10 = m1.m10 * scalar;
        this.m11 = m1.m11 * scalar;
        this.m12 = m1.m12 * scalar;
        this.m13 = m1.m13 * scalar;
        this.m20 = m1.m20 * scalar;
        this.m21 = m1.m21 * scalar;
        this.m22 = m1.m22 * scalar;
        this.m23 = m1.m23 * scalar;
        this.m30 = m1.m30 * scalar;
        this.m31 = m1.m31 * scalar;
        this.m32 = m1.m32 * scalar;
        this.m33 = m1.m33 * scalar;
    }

    /**
     * Sets the value of this matrix to the result of multiplying itself
     * with matrix m1.
     *
     * @param m1 the other matrix
     */
    public final void mul(Matrix4f m1) {

        float m00 = this.m00 * m1.m00 + this.m01 * m1.m10 +
                this.m02 * m1.m20 + this.m03 * m1.m30;
        float m01 = this.m00 * m1.m01 + this.m01 * m1.m11 +
                this.m02 * m1.m21 + this.m03 * m1.m31;
        float m02 = this.m00 * m1.m02 + this.m01 * m1.m12 +
                this.m02 * m1.m22 + this.m03 * m1.m32;
        float m03 = this.m00 * m1.m03 + this.m01 * m1.m13 +
                this.m02 * m1.m23 + this.m03 * m1.m33;

        float m10 = this.m10 * m1.m00 + this.m11 * m1.m10 +
                this.m12 * m1.m20 + this.m13 * m1.m30;
        float m11 = this.m10 * m1.m01 + this.m11 * m1.m11 +
                this.m12 * m1.m21 + this.m13 * m1.m31;
        float m12 = this.m10 * m1.m02 + this.m11 * m1.m12 +
                this.m12 * m1.m22 + this.m13 * m1.m32;
        float m13 = this.m10 * m1.m03 + this.m11 * m1.m13 +
                this.m12 * m1.m23 + this.m13 * m1.m33;

        float m20 = this.m20 * m1.m00 + this.m21 * m1.m10 +
                this.m22 * m1.m20 + this.m23 * m1.m30;
        float m21 = this.m20 * m1.m01 + this.m21 * m1.m11 +
                this.m22 * m1.m21 + this.m23 * m1.m31;
        float m22 = this.m20 * m1.m02 + this.m21 * m1.m12 +
                this.m22 * m1.m22 + this.m23 * m1.m32;
        float m23 = this.m20 * m1.m03 + this.m21 * m1.m13 +
                this.m22 * m1.m23 + this.m23 * m1.m33;

        float m30 = this.m30 * m1.m00 + this.m31 * m1.m10 +
                this.m32 * m1.m20 + this.m33 * m1.m30;
        float m31 = this.m30 * m1.m01 + this.m31 * m1.m11 +
                this.m32 * m1.m21 + this.m33 * m1.m31;
        float m32 = this.m30 * m1.m02 + this.m31 * m1.m12 +
                this.m32 * m1.m22 + this.m33 * m1.m32;
        float m33 = this.m30 * m1.m03 + this.m31 * m1.m13 +
                this.m32 * m1.m23 + this.m33 * m1.m33;

        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    /**
     * Sets the value of this matrix to the result of multiplying
     * the two argument matrices together.
     *
     * @param m1 the first matrix
     * @param m2 the second matrix
     */
    public final void mul(Matrix4f m1, Matrix4f m2) {
        if (this != m1 && this != m2) {

            this.m00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 +
                    m1.m02 * m2.m20 + m1.m03 * m2.m30;
            this.m01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 +
                    m1.m02 * m2.m21 + m1.m03 * m2.m31;
            this.m02 = m1.m00 * m2.m02 + m1.m01 * m2.m12 +
                    m1.m02 * m2.m22 + m1.m03 * m2.m32;
            this.m03 = m1.m00 * m2.m03 + m1.m01 * m2.m13 +
                    m1.m02 * m2.m23 + m1.m03 * m2.m33;

            this.m10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 +
                    m1.m12 * m2.m20 + m1.m13 * m2.m30;
            this.m11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 +
                    m1.m12 * m2.m21 + m1.m13 * m2.m31;
            this.m12 = m1.m10 * m2.m02 + m1.m11 * m2.m12 +
                    m1.m12 * m2.m22 + m1.m13 * m2.m32;
            this.m13 = m1.m10 * m2.m03 + m1.m11 * m2.m13 +
                    m1.m12 * m2.m23 + m1.m13 * m2.m33;

            this.m20 = m1.m20 * m2.m00 + m1.m21 * m2.m10 +
                    m1.m22 * m2.m20 + m1.m23 * m2.m30;
            this.m21 = m1.m20 * m2.m01 + m1.m21 * m2.m11 +
                    m1.m22 * m2.m21 + m1.m23 * m2.m31;
            this.m22 = m1.m20 * m2.m02 + m1.m21 * m2.m12 +
                    m1.m22 * m2.m22 + m1.m23 * m2.m32;
            this.m23 = m1.m20 * m2.m03 + m1.m21 * m2.m13 +
                    m1.m22 * m2.m23 + m1.m23 * m2.m33;

            this.m30 = m1.m30 * m2.m00 + m1.m31 * m2.m10 +
                    m1.m32 * m2.m20 + m1.m33 * m2.m30;
            this.m31 = m1.m30 * m2.m01 + m1.m31 * m2.m11 +
                    m1.m32 * m2.m21 + m1.m33 * m2.m31;
            this.m32 = m1.m30 * m2.m02 + m1.m31 * m2.m12 +
                    m1.m32 * m2.m22 + m1.m33 * m2.m32;
            this.m33 = m1.m30 * m2.m03 + m1.m31 * m2.m13 +
                    m1.m32 * m2.m23 + m1.m33 * m2.m33;
        } else {
            float m00 = m1.m00 * m2.m00 + m1.m01 * m2.m10 + m1.m02 * m2.m20 + m1.m03 * m2.m30;
            float m01 = m1.m00 * m2.m01 + m1.m01 * m2.m11 + m1.m02 * m2.m21 + m1.m03 * m2.m31;
            float m02 = m1.m00 * m2.m02 + m1.m01 * m2.m12 + m1.m02 * m2.m22 + m1.m03 * m2.m32;
            float m03 = m1.m00 * m2.m03 + m1.m01 * m2.m13 + m1.m02 * m2.m23 + m1.m03 * m2.m33;

            float m10 = m1.m10 * m2.m00 + m1.m11 * m2.m10 + m1.m12 * m2.m20 + m1.m13 * m2.m30;
            float m11 = m1.m10 * m2.m01 + m1.m11 * m2.m11 + m1.m12 * m2.m21 + m1.m13 * m2.m31;
            float m12 = m1.m10 * m2.m02 + m1.m11 * m2.m12 + m1.m12 * m2.m22 + m1.m13 * m2.m32;
            float m13 = m1.m10 * m2.m03 + m1.m11 * m2.m13 + m1.m12 * m2.m23 + m1.m13 * m2.m33;

            float m20 = m1.m20 * m2.m00 + m1.m21 * m2.m10 + m1.m22 * m2.m20 + m1.m23 * m2.m30;
            float m21 = m1.m20 * m2.m01 + m1.m21 * m2.m11 + m1.m22 * m2.m21 + m1.m23 * m2.m31;
            float m22 = m1.m20 * m2.m02 + m1.m21 * m2.m12 + m1.m22 * m2.m22 + m1.m23 * m2.m32;
            float m23 = m1.m20 * m2.m03 + m1.m21 * m2.m13 + m1.m22 * m2.m23 + m1.m23 * m2.m33;

            float m30 = m1.m30 * m2.m00 + m1.m31 * m2.m10 + m1.m32 * m2.m20 + m1.m33 * m2.m30;
            float m31 = m1.m30 * m2.m01 + m1.m31 * m2.m11 + m1.m32 * m2.m21 + m1.m33 * m2.m31;
            float m32 = m1.m30 * m2.m02 + m1.m31 * m2.m12 + m1.m32 * m2.m22 + m1.m33 * m2.m32;
            float m33 = m1.m30 * m2.m03 + m1.m31 * m2.m13 + m1.m32 * m2.m23 + m1.m33 * m2.m33;

            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }
    }

    /**
     * Multiplies the transpose of matrix m1 times the transpose of matrix
     * m2, and places the result into this.
     *
     * @param m1 the matrix on the left hand side of the multiplication
     * @param m2 the matrix on the right hand side of the multiplication
     */
    public final void mulTransposeBoth(Matrix4f m1, Matrix4f m2) {
        if (this != m1 && this != m2) {
            this.m00 = m1.m00 * m2.m00 + m1.m10 * m2.m01 + m1.m20 * m2.m02 + m1.m30 * m2.m03;
            this.m01 = m1.m00 * m2.m10 + m1.m10 * m2.m11 + m1.m20 * m2.m12 + m1.m30 * m2.m13;
            this.m02 = m1.m00 * m2.m20 + m1.m10 * m2.m21 + m1.m20 * m2.m22 + m1.m30 * m2.m23;
            this.m03 = m1.m00 * m2.m30 + m1.m10 * m2.m31 + m1.m20 * m2.m32 + m1.m30 * m2.m33;

            this.m10 = m1.m01 * m2.m00 + m1.m11 * m2.m01 + m1.m21 * m2.m02 + m1.m31 * m2.m03;
            this.m11 = m1.m01 * m2.m10 + m1.m11 * m2.m11 + m1.m21 * m2.m12 + m1.m31 * m2.m13;
            this.m12 = m1.m01 * m2.m20 + m1.m11 * m2.m21 + m1.m21 * m2.m22 + m1.m31 * m2.m23;
            this.m13 = m1.m01 * m2.m30 + m1.m11 * m2.m31 + m1.m21 * m2.m32 + m1.m31 * m2.m33;

            this.m20 = m1.m02 * m2.m00 + m1.m12 * m2.m01 + m1.m22 * m2.m02 + m1.m32 * m2.m03;
            this.m21 = m1.m02 * m2.m10 + m1.m12 * m2.m11 + m1.m22 * m2.m12 + m1.m32 * m2.m13;
            this.m22 = m1.m02 * m2.m20 + m1.m12 * m2.m21 + m1.m22 * m2.m22 + m1.m32 * m2.m23;
            this.m23 = m1.m02 * m2.m30 + m1.m12 * m2.m31 + m1.m22 * m2.m32 + m1.m32 * m2.m33;

            this.m30 = m1.m03 * m2.m00 + m1.m13 * m2.m01 + m1.m23 * m2.m02 + m1.m33 * m2.m03;
            this.m31 = m1.m03 * m2.m10 + m1.m13 * m2.m11 + m1.m23 * m2.m12 + m1.m33 * m2.m13;
            this.m32 = m1.m03 * m2.m20 + m1.m13 * m2.m21 + m1.m23 * m2.m22 + m1.m33 * m2.m23;
            this.m33 = m1.m03 * m2.m30 + m1.m13 * m2.m31 + m1.m23 * m2.m32 + m1.m33 * m2.m33;
        } else {

            float m00 = m1.m00 * m2.m00 + m1.m10 * m2.m01 + m1.m20 * m2.m02 + m1.m30 * m2.m03;
            float m01 = m1.m00 * m2.m10 + m1.m10 * m2.m11 + m1.m20 * m2.m12 + m1.m30 * m2.m13;
            float m02 = m1.m00 * m2.m20 + m1.m10 * m2.m21 + m1.m20 * m2.m22 + m1.m30 * m2.m23;
            float m03 = m1.m00 * m2.m30 + m1.m10 * m2.m31 + m1.m20 * m2.m32 + m1.m30 * m2.m33;

            float m10 = m1.m01 * m2.m00 + m1.m11 * m2.m01 + m1.m21 * m2.m02 + m1.m31 * m2.m03;
            float m11 = m1.m01 * m2.m10 + m1.m11 * m2.m11 + m1.m21 * m2.m12 + m1.m31 * m2.m13;
            float m12 = m1.m01 * m2.m20 + m1.m11 * m2.m21 + m1.m21 * m2.m22 + m1.m31 * m2.m23;
            float m13 = m1.m01 * m2.m30 + m1.m11 * m2.m31 + m1.m21 * m2.m32 + m1.m31 * m2.m33;

            float m20 = m1.m02 * m2.m00 + m1.m12 * m2.m01 + m1.m22 * m2.m02 + m1.m32 * m2.m03;
            float m21 = m1.m02 * m2.m10 + m1.m12 * m2.m11 + m1.m22 * m2.m12 + m1.m32 * m2.m13;
            float m22 = m1.m02 * m2.m20 + m1.m12 * m2.m21 + m1.m22 * m2.m22 + m1.m32 * m2.m23;
            float m23 = m1.m02 * m2.m30 + m1.m12 * m2.m31 + m1.m22 * m2.m32 + m1.m32 * m2.m33;

            float m30 = m1.m03 * m2.m00 + m1.m13 * m2.m01 + m1.m23 * m2.m02 + m1.m33 * m2.m03;
            float m31 = m1.m03 * m2.m10 + m1.m13 * m2.m11 + m1.m23 * m2.m12 + m1.m33 * m2.m13;
            float m32 = m1.m03 * m2.m20 + m1.m13 * m2.m21 + m1.m23 * m2.m22 + m1.m33 * m2.m23;
            float m33 = m1.m03 * m2.m30 + m1.m13 * m2.m31 + m1.m23 * m2.m32 + m1.m33 * m2.m33;

            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }

    }

    /**
     * Multiplies matrix m1 times the transpose of matrix m2, and
     * places the result into this.
     *
     * @param m1 the matrix on the left hand side of the multiplication
     * @param m2 the matrix on the right hand side of the multiplication
     */
    public final void mulTransposeRight(Matrix4f m1, Matrix4f m2) {
        if (this != m1 && this != m2) {
            this.m00 = m1.m00 * m2.m00 + m1.m01 * m2.m01 + m1.m02 * m2.m02 + m1.m03 * m2.m03;
            this.m01 = m1.m00 * m2.m10 + m1.m01 * m2.m11 + m1.m02 * m2.m12 + m1.m03 * m2.m13;
            this.m02 = m1.m00 * m2.m20 + m1.m01 * m2.m21 + m1.m02 * m2.m22 + m1.m03 * m2.m23;
            this.m03 = m1.m00 * m2.m30 + m1.m01 * m2.m31 + m1.m02 * m2.m32 + m1.m03 * m2.m33;

            this.m10 = m1.m10 * m2.m00 + m1.m11 * m2.m01 + m1.m12 * m2.m02 + m1.m13 * m2.m03;
            this.m11 = m1.m10 * m2.m10 + m1.m11 * m2.m11 + m1.m12 * m2.m12 + m1.m13 * m2.m13;
            this.m12 = m1.m10 * m2.m20 + m1.m11 * m2.m21 + m1.m12 * m2.m22 + m1.m13 * m2.m23;
            this.m13 = m1.m10 * m2.m30 + m1.m11 * m2.m31 + m1.m12 * m2.m32 + m1.m13 * m2.m33;

            this.m20 = m1.m20 * m2.m00 + m1.m21 * m2.m01 + m1.m22 * m2.m02 + m1.m23 * m2.m03;
            this.m21 = m1.m20 * m2.m10 + m1.m21 * m2.m11 + m1.m22 * m2.m12 + m1.m23 * m2.m13;
            this.m22 = m1.m20 * m2.m20 + m1.m21 * m2.m21 + m1.m22 * m2.m22 + m1.m23 * m2.m23;
            this.m23 = m1.m20 * m2.m30 + m1.m21 * m2.m31 + m1.m22 * m2.m32 + m1.m23 * m2.m33;

            this.m30 = m1.m30 * m2.m00 + m1.m31 * m2.m01 + m1.m32 * m2.m02 + m1.m33 * m2.m03;
            this.m31 = m1.m30 * m2.m10 + m1.m31 * m2.m11 + m1.m32 * m2.m12 + m1.m33 * m2.m13;
            this.m32 = m1.m30 * m2.m20 + m1.m31 * m2.m21 + m1.m32 * m2.m22 + m1.m33 * m2.m23;
            this.m33 = m1.m30 * m2.m30 + m1.m31 * m2.m31 + m1.m32 * m2.m32 + m1.m33 * m2.m33;
        } else {

            float m00 = m1.m00 * m2.m00 + m1.m01 * m2.m01 + m1.m02 * m2.m02 + m1.m03 * m2.m03;
            float m01 = m1.m00 * m2.m10 + m1.m01 * m2.m11 + m1.m02 * m2.m12 + m1.m03 * m2.m13;
            float m02 = m1.m00 * m2.m20 + m1.m01 * m2.m21 + m1.m02 * m2.m22 + m1.m03 * m2.m23;
            float m03 = m1.m00 * m2.m30 + m1.m01 * m2.m31 + m1.m02 * m2.m32 + m1.m03 * m2.m33;

            float m10 = m1.m10 * m2.m00 + m1.m11 * m2.m01 + m1.m12 * m2.m02 + m1.m13 * m2.m03;
            float m11 = m1.m10 * m2.m10 + m1.m11 * m2.m11 + m1.m12 * m2.m12 + m1.m13 * m2.m13;
            float m12 = m1.m10 * m2.m20 + m1.m11 * m2.m21 + m1.m12 * m2.m22 + m1.m13 * m2.m23;
            float m13 = m1.m10 * m2.m30 + m1.m11 * m2.m31 + m1.m12 * m2.m32 + m1.m13 * m2.m33;

            float m20 = m1.m20 * m2.m00 + m1.m21 * m2.m01 + m1.m22 * m2.m02 + m1.m23 * m2.m03;
            float m21 = m1.m20 * m2.m10 + m1.m21 * m2.m11 + m1.m22 * m2.m12 + m1.m23 * m2.m13;
            float m22 = m1.m20 * m2.m20 + m1.m21 * m2.m21 + m1.m22 * m2.m22 + m1.m23 * m2.m23;
            float m23 = m1.m20 * m2.m30 + m1.m21 * m2.m31 + m1.m22 * m2.m32 + m1.m23 * m2.m33;

            float m30 = m1.m30 * m2.m00 + m1.m31 * m2.m01 + m1.m32 * m2.m02 + m1.m33 * m2.m03;
            float m31 = m1.m30 * m2.m10 + m1.m31 * m2.m11 + m1.m32 * m2.m12 + m1.m33 * m2.m13;
            float m32 = m1.m30 * m2.m20 + m1.m31 * m2.m21 + m1.m32 * m2.m22 + m1.m33 * m2.m23;
            float m33 = m1.m30 * m2.m30 + m1.m31 * m2.m31 + m1.m32 * m2.m32 + m1.m33 * m2.m33;

            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }

    }


    /**
     * Multiplies the transpose of matrix m1 times matrix m2, and
     * places the result into this.
     *
     * @param m1 the matrix on the left hand side of the multiplication
     * @param m2 the matrix on the right hand side of the multiplication
     */
    public final void mulTransposeLeft(Matrix4f m1, Matrix4f m2) {
        if (this != m1 && this != m2) {
            this.m00 = m1.m00 * m2.m00 + m1.m10 * m2.m10 + m1.m20 * m2.m20 + m1.m30 * m2.m30;
            this.m01 = m1.m00 * m2.m01 + m1.m10 * m2.m11 + m1.m20 * m2.m21 + m1.m30 * m2.m31;
            this.m02 = m1.m00 * m2.m02 + m1.m10 * m2.m12 + m1.m20 * m2.m22 + m1.m30 * m2.m32;
            this.m03 = m1.m00 * m2.m03 + m1.m10 * m2.m13 + m1.m20 * m2.m23 + m1.m30 * m2.m33;

            this.m10 = m1.m01 * m2.m00 + m1.m11 * m2.m10 + m1.m21 * m2.m20 + m1.m31 * m2.m30;
            this.m11 = m1.m01 * m2.m01 + m1.m11 * m2.m11 + m1.m21 * m2.m21 + m1.m31 * m2.m31;
            this.m12 = m1.m01 * m2.m02 + m1.m11 * m2.m12 + m1.m21 * m2.m22 + m1.m31 * m2.m32;
            this.m13 = m1.m01 * m2.m03 + m1.m11 * m2.m13 + m1.m21 * m2.m23 + m1.m31 * m2.m33;

            this.m20 = m1.m02 * m2.m00 + m1.m12 * m2.m10 + m1.m22 * m2.m20 + m1.m32 * m2.m30;
            this.m21 = m1.m02 * m2.m01 + m1.m12 * m2.m11 + m1.m22 * m2.m21 + m1.m32 * m2.m31;
            this.m22 = m1.m02 * m2.m02 + m1.m12 * m2.m12 + m1.m22 * m2.m22 + m1.m32 * m2.m32;
            this.m23 = m1.m02 * m2.m03 + m1.m12 * m2.m13 + m1.m22 * m2.m23 + m1.m32 * m2.m33;

            this.m30 = m1.m03 * m2.m00 + m1.m13 * m2.m10 + m1.m23 * m2.m20 + m1.m33 * m2.m30;
            this.m31 = m1.m03 * m2.m01 + m1.m13 * m2.m11 + m1.m23 * m2.m21 + m1.m33 * m2.m31;
            this.m32 = m1.m03 * m2.m02 + m1.m13 * m2.m12 + m1.m23 * m2.m22 + m1.m33 * m2.m32;
            this.m33 = m1.m03 * m2.m03 + m1.m13 * m2.m13 + m1.m23 * m2.m23 + m1.m33 * m2.m33;
        } else {


            float m00 = m1.m00 * m2.m00 + m1.m10 * m2.m10 + m1.m20 * m2.m20 + m1.m30 * m2.m30;
            float m01 = m1.m00 * m2.m01 + m1.m10 * m2.m11 + m1.m20 * m2.m21 + m1.m30 * m2.m31;
            float m02 = m1.m00 * m2.m02 + m1.m10 * m2.m12 + m1.m20 * m2.m22 + m1.m30 * m2.m32;
            float m03 = m1.m00 * m2.m03 + m1.m10 * m2.m13 + m1.m20 * m2.m23 + m1.m30 * m2.m33;

            float m10 = m1.m01 * m2.m00 + m1.m11 * m2.m10 + m1.m21 * m2.m20 + m1.m31 * m2.m30;
            float m11 = m1.m01 * m2.m01 + m1.m11 * m2.m11 + m1.m21 * m2.m21 + m1.m31 * m2.m31;
            float m12 = m1.m01 * m2.m02 + m1.m11 * m2.m12 + m1.m21 * m2.m22 + m1.m31 * m2.m32;
            float m13 = m1.m01 * m2.m03 + m1.m11 * m2.m13 + m1.m21 * m2.m23 + m1.m31 * m2.m33;

            float m20 = m1.m02 * m2.m00 + m1.m12 * m2.m10 + m1.m22 * m2.m20 + m1.m32 * m2.m30;
            float m21 = m1.m02 * m2.m01 + m1.m12 * m2.m11 + m1.m22 * m2.m21 + m1.m32 * m2.m31;
            float m22 = m1.m02 * m2.m02 + m1.m12 * m2.m12 + m1.m22 * m2.m22 + m1.m32 * m2.m32;
            float m23 = m1.m02 * m2.m03 + m1.m12 * m2.m13 + m1.m22 * m2.m23 + m1.m32 * m2.m33;

            float m30 = m1.m03 * m2.m00 + m1.m13 * m2.m10 + m1.m23 * m2.m20 + m1.m33 * m2.m30;
            float m31 = m1.m03 * m2.m01 + m1.m13 * m2.m11 + m1.m23 * m2.m21 + m1.m33 * m2.m31;
            float m32 = m1.m03 * m2.m02 + m1.m13 * m2.m12 + m1.m23 * m2.m22 + m1.m33 * m2.m32;
            float m33 = m1.m03 * m2.m03 + m1.m13 * m2.m13 + m1.m23 * m2.m23 + m1.m33 * m2.m33;

            this.m00 = m00;
            this.m01 = m01;
            this.m02 = m02;
            this.m03 = m03;
            this.m10 = m10;
            this.m11 = m11;
            this.m12 = m12;
            this.m13 = m13;
            this.m20 = m20;
            this.m21 = m21;
            this.m22 = m22;
            this.m23 = m23;
            this.m30 = m30;
            this.m31 = m31;
            this.m32 = m32;
            this.m33 = m33;
        }

    }


    /**
     * Returns true if all of the data members of Matrix4f m1 are
     * equal to the corresponding data members in this Matrix4f.
     *
     * @param m1 the matrix with which the comparison is made.
     * @return true or false
     */
    public boolean equals(Matrix4f m1) {
        try {
            return (this.m00 == m1.m00 && this.m01 == m1.m01 && this.m02 == m1.m02
                    && this.m03 == m1.m03 && this.m10 == m1.m10 && this.m11 == m1.m11
                    && this.m12 == m1.m12 && this.m13 == m1.m13 && this.m20 == m1.m20
                    && this.m21 == m1.m21 && this.m22 == m1.m22 && this.m23 == m1.m23
                    && this.m30 == m1.m30 && this.m31 == m1.m31 && this.m32 == m1.m32
                    && this.m33 == m1.m33);
        } catch (NullPointerException e2) {
            return false;
        }

    }

    /**
     * Returns true if the Object t1 is of type Matrix4f and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Matrix4f.
     *
     * @param t1 the matrix with which the comparison is made.
     * @return true or false
     */
    public boolean equals(Object t1) {
        try {
            Matrix4f m2 = (Matrix4f) t1;
            return (this.m00 == m2.m00 && this.m01 == m2.m01 && this.m02 == m2.m02
                    && this.m03 == m2.m03 && this.m10 == m2.m10 && this.m11 == m2.m11
                    && this.m12 == m2.m12 && this.m13 == m2.m13 && this.m20 == m2.m20
                    && this.m21 == m2.m21 && this.m22 == m2.m22 && this.m23 == m2.m23
                    && this.m30 == m2.m30 && this.m31 == m2.m31 && this.m32 == m2.m32
                    && this.m33 == m2.m33);
        } catch (ClassCastException | NullPointerException e1) {
            return false;
        }
    }

    /**
     * Returns true if the L-infinite distance between this matrix
     * and matrix m1 is less than or equal to the epsilon parameter,
     * otherwise returns false.  The L-infinite
     * distance is equal to
     * MAX[i=0,1,2,3 ; j=0,1,2,3 ; abs(this.m(i,j) - m1.m(i,j)]
     *
     * @param m1      the matrix to be compared to this matrix
     * @param epsilon the threshold value
     */
    public boolean epsilonEquals(Matrix4f m1, float epsilon) {

        boolean status = true;

        if (Math.abs(this.m00 - m1.m00) > epsilon) status = false;
        if (Math.abs(this.m01 - m1.m01) > epsilon) status = false;
        if (Math.abs(this.m02 - m1.m02) > epsilon) status = false;
        if (Math.abs(this.m03 - m1.m03) > epsilon) status = false;

        if (Math.abs(this.m10 - m1.m10) > epsilon) status = false;
        if (Math.abs(this.m11 - m1.m11) > epsilon) status = false;
        if (Math.abs(this.m12 - m1.m12) > epsilon) status = false;
        if (Math.abs(this.m13 - m1.m13) > epsilon) status = false;

        if (Math.abs(this.m20 - m1.m20) > epsilon) status = false;
        if (Math.abs(this.m21 - m1.m21) > epsilon) status = false;
        if (Math.abs(this.m22 - m1.m22) > epsilon) status = false;
        if (Math.abs(this.m23 - m1.m23) > epsilon) status = false;

        if (Math.abs(this.m30 - m1.m30) > epsilon) status = false;
        if (Math.abs(this.m31 - m1.m31) > epsilon) status = false;
        if (Math.abs(this.m32 - m1.m32) > epsilon) status = false;
        if (Math.abs(this.m33 - m1.m33) > epsilon) status = false;

        return (status);

    }


    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different Matrix4f objects with identical data values
     * (i.e., Matrix4f.equals returns true) will return the same hash
     * code value.  Two objects with different data members may return the
     * same hash value, although this is not likely.
     *
     * @return the integer hash code value
     */
    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + VecMathUtil.floatToIntBits(m00);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m01);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m02);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m03);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m10);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m11);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m12);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m13);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m20);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m21);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m22);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m23);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m30);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m31);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m32);
        bits = 31L * bits + VecMathUtil.floatToIntBits(m33);
        return (int) (bits ^ (bits >> 32));
    }


    /**
     * Transform the vector vec using this Matrix4f and place the
     * result into vecOut.
     *
     * @param vec    the single precision vector to be transformed
     * @param vecOut the vector into which the transformed values are placed
     */
    public final void transform(Tuple4f vec, Tuple4f vecOut) {
        float x = m00 * vec.x + m01 * vec.y
                + m02 * vec.z + m03 * vec.w;
        float y = m10 * vec.x + m11 * vec.y
                + m12 * vec.z + m13 * vec.w;
        float z = m20 * vec.x + m21 * vec.y
                + m22 * vec.z + m23 * vec.w;
        vecOut.w = m30 * vec.x + m31 * vec.y
                + m32 * vec.z + m33 * vec.w;
        vecOut.x = x;
        vecOut.y = y;
        vecOut.z = z;
    }


    /**
     * Transform the vector vec using this Transform and place the
     * result back into vec.
     *
     * @param vec the single precision vector to be transformed
     */
    public final void transform(Tuple4f vec) {

        float x = m00 * vec.x + m01 * vec.y
                + m02 * vec.z + m03 * vec.w;
        float y = m10 * vec.x + m11 * vec.y
                + m12 * vec.z + m13 * vec.w;
        float z = m20 * vec.x + m21 * vec.y
                + m22 * vec.z + m23 * vec.w;
        vec.w = m30 * vec.x + m31 * vec.y
                + m32 * vec.z + m33 * vec.w;
        vec.x = x;
        vec.y = y;
        vec.z = z;
    }

//    /**
//     * Transforms the point parameter with this Matrix4f and
//     * places the result into pointOut.  The fourth element of the
//     * point input paramter is assumed to be one.
//     *
//     * @param point    the input point to be transformed.
//     * @param pointOut the transformed point
//     */
//    public final void transform(Point3f point, Point3f pointOut) {
//        float y;
//        float x = m00 * point.x + m01 * point.y + m02 * point.z + m03;
//        y = m10 * point.x + m11 * point.y + m12 * point.z + m13;
//        pointOut.z = m20 * point.x + m21 * point.y + m22 * point.z + m23;
//        pointOut.x = x;
//        pointOut.y = y;
//    }

//
//    /**
//     * Transforms the point parameter with this Matrix4f and
//     * places the result back into point.  The fourth element of the
//     * point input paramter is assumed to be one.
//     *
//     * @param point the input point to be transformed.
//     */
//    public final void transform(Point3f point) {
//        float y;
//        float x = m00 * point.x + m01 * point.y + m02 * point.z + m03;
//        y = m10 * point.x + m11 * point.y + m12 * point.z + m13;
//        point.z = m20 * point.x + m21 * point.y + m22 * point.z + m23;
//        point.x = x;
//        point.y = y;
//    }


    /**
     * Transforms the normal parameter by this Matrix4f and places the value
     * into normalOut.  The fourth element of the normal is assumed to be zero.
     *
     * @param normal    the input normal to be transformed.
     * @param normalOut the transformed normal
     */
    public final void transform(v3 normal, v3 normalOut) {
        float x = m00 * normal.x + m01 * normal.y + m02 * normal.z;
        float y = m10 * normal.x + m11 * normal.y + m12 * normal.z;
        normalOut.z = m20 * normal.x + m21 * normal.y + m22 * normal.z;
        normalOut.x = x;
        normalOut.y = y;
    }


    /**
     * Transforms the normal parameter by this transform and places the value
     * back into normal.  The fourth element of the normal is assumed to be zero.
     *
     * @param normal the input normal to be transformed.
     */
    public final void transform(v3 normal) {

        float x = m00 * normal.x + m01 * normal.y + m02 * normal.z;
        float y = m10 * normal.x + m11 * normal.y + m12 * normal.z;
        normal.z = m20 * normal.x + m21 * normal.y + m22 * normal.z;
        normal.x = x;
        normal.y = y;
    }


    /**
     * Sets this matrix to all zeros.
     */
    public final void setZero() {
        m00 = 0.0f;
        m01 = 0.0f;
        m02 = 0.0f;
        m03 = 0.0f;
        m10 = 0.0f;
        m11 = 0.0f;
        m12 = 0.0f;
        m13 = 0.0f;
        m20 = 0.0f;
        m21 = 0.0f;
        m22 = 0.0f;
        m23 = 0.0f;
        m30 = 0.0f;
        m31 = 0.0f;
        m32 = 0.0f;
        m33 = 0.0f;
    }

    /**
     * Negates the value of this matrix: this = -this.
     */
    public final void negate() {
        m00 = -m00;
        m01 = -m01;
        m02 = -m02;
        m03 = -m03;
        m10 = -m10;
        m11 = -m11;
        m12 = -m12;
        m13 = -m13;
        m20 = -m20;
        m21 = -m21;
        m22 = -m22;
        m23 = -m23;
        m30 = -m30;
        m31 = -m31;
        m32 = -m32;
        m33 = -m33;
    }

    /**
     * Sets the value of this matrix equal to the negation of
     * of the Matrix4f parameter.
     *
     * @param m1 the source matrix
     */
    public final void negate(Matrix4f m1) {
        this.m00 = -m1.m00;
        this.m01 = -m1.m01;
        this.m02 = -m1.m02;
        this.m03 = -m1.m03;
        this.m10 = -m1.m10;
        this.m11 = -m1.m11;
        this.m12 = -m1.m12;
        this.m13 = -m1.m13;
        this.m20 = -m1.m20;
        this.m21 = -m1.m21;
        this.m22 = -m1.m22;
        this.m23 = -m1.m23;
        this.m30 = -m1.m30;
        this.m31 = -m1.m31;
        this.m32 = -m1.m32;
        this.m33 = -m1.m33;
    }


    /**
     * Creates a new object of the same class as this object.
     *
     * @return a clone of this instance.
     * @throws OutOfMemoryError if there is not enough memory.
     * @see Cloneable
     * @since vecmath 1.3
     */
    @Override
    public Object clone() {
        Matrix4f m1 = null;
        try {
            m1 = (Matrix4f) super.clone();
        } catch (CloneNotSupportedException e) {

            throw new InternalError();
        }

        return m1;
    }

    /**
     * Get the first matrix element in the first row.
     *
     * @return Returns the m00.
     * @since vecmath 1.5
     */
    public final float getM00() {
        return m00;
    }

    /**
     * Set the first matrix element in the first row.
     *
     * @param m00 The m00 to setAt.
     * @since vecmath 1.5
     */
    public final void setM00(float m00) {
        this.m00 = m00;
    }

    /**
     * Get the second matrix element in the first row.
     *
     * @return Returns the m01.
     * @since vecmath 1.5
     */
    public final float getM01() {
        return m01;
    }

    /**
     * Set the second matrix element in the first row.
     *
     * @param m01 The m01 to setAt.
     * @since vecmath 1.5
     */
    public final void setM01(float m01) {
        this.m01 = m01;
    }

    /**
     * Get the third matrix element in the first row.
     *
     * @return Returns the m02.
     * @since vecmath 1.5
     */
    public final float getM02() {
        return m02;
    }

    /**
     * Set the third matrix element in the first row.
     *
     * @param m02 The m02 to setAt.
     * @since vecmath 1.5
     */
    public final void setM02(float m02) {
        this.m02 = m02;
    }

    /**
     * Get first matrix element in the second row.
     *
     * @return Returns the m10.
     * @since vecmath 1.5
     */
    public final float getM10() {
        return m10;
    }

    /**
     * Set first matrix element in the second row.
     *
     * @param m10 The m10 to setAt.
     * @since vecmath 1.5
     */
    public final void setM10(float m10) {
        this.m10 = m10;
    }

    /**
     * Get second matrix element in the second row.
     *
     * @return Returns the m11.
     * @since vecmath 1.5
     */
    public final float getM11() {
        return m11;
    }

    /**
     * Set the second matrix element in the second row.
     *
     * @param m11 The m11 to setAt.
     * @since vecmath 1.5
     */
    public final void setM11(float m11) {
        this.m11 = m11;
    }

    /**
     * Get the third matrix element in the second row.
     *
     * @return Returns the m12.
     * @since vecmath 1.5
     */
    public final float getM12() {
        return m12;
    }

    /**
     * Set the third matrix element in the second row.
     *
     * @param m12 The m12 to setAt.
     * @since vecmath 1.5
     */
    public final void setM12(float m12) {
        this.m12 = m12;
    }

    /**
     * Get the first matrix element in the third row.
     *
     * @return Returns the m20.
     * @since vecmath 1.5
     */
    public final float getM20() {
        return m20;
    }

    /**
     * Set the first matrix element in the third row.
     *
     * @param m20 The m20 to setAt.
     * @since vecmath 1.5
     */
    public final void setM20(float m20) {
        this.m20 = m20;
    }

    /**
     * Get the second matrix element in the third row.
     *
     * @return Returns the m21.
     * @since vecmath 1.5
     */
    public final float getM21() {
        return m21;
    }

    /**
     * Set the second matrix element in the third row.
     *
     * @param m21 The m21 to setAt.
     * @since vecmath 1.5
     */
    public final void setM21(float m21) {
        this.m21 = m21;
    }

    /**
     * Get the third matrix element in the third row.
     *
     * @return Returns the m22.
     * @since vecmath 1.5
     */
    public final float getM22() {
        return m22;
    }

    /**
     * Set the third matrix element in the third row.
     *
     * @param m22 The m22 to setAt.
     * @since vecmath 1.5
     */
    public final void setM22(float m22) {
        this.m22 = m22;
    }

    /**
     * Get the fourth element of the first row.
     *
     * @return Returns the m03.
     * @since vecmath 1.5
     */
    public final float getM03() {
        return m03;
    }

    /**
     * Set the fourth element of the first row.
     *
     * @param m03 The m03 to setAt.
     * @since vecmath 1.5
     */
    public final void setM03(float m03) {
        this.m03 = m03;
    }

    /**
     * Get the fourth element of the second row.
     *
     * @return Returns the m13.
     * @since vecmath 1.5
     */
    public final float getM13() {
        return m13;
    }

    /**
     * Set the fourth element of the second row.
     *
     * @param m13 The m13 to setAt.
     * @since vecmath 1.5
     */
    public final void setM13(float m13) {
        this.m13 = m13;
    }

    /**
     * Get the fourth element of the third row.
     *
     * @return Returns the m23.
     * @since vecmath 1.5
     */
    public final float getM23() {
        return m23;
    }

    /**
     * Set the fourth element of the third row.
     *
     * @param m23 The m23 to setAt.
     * @since vecmath 1.5
     */
    public final void setM23(float m23) {
        this.m23 = m23;
    }

    /**
     * Get the first element of the fourth row.
     *
     * @return Returns the m30.
     * @since vecmath 1.5
     */
    public final float getM30() {
        return m30;
    }

    /**
     * Set the first element of the fourth row.
     *
     * @param m30 The m30 to setAt.
     * @since vecmath 1.5
     */
    public final void setM30(float m30) {
        this.m30 = m30;
    }

    /**
     * Get the second element of the fourth row.
     *
     * @return Returns the m31.
     * @since vecmath 1.5
     */
    public final float getM31() {
        return m31;
    }

    /**
     * Set the second element of the fourth row.
     *
     * @param m31 The m31 to setAt.
     * @since vecmath 1.5
     */
    public final void setM31(float m31) {
        this.m31 = m31;
    }

    /**
     * Get the third element of the fourth row.
     *
     * @return Returns the m32.
     * @since vecmath 1.5
     */
    public final float getM32() {
        return m32;
    }

    /**
     * Set the third element of the fourth row.
     *
     * @param m32 The m32 to setAt.
     * @since vecmath 1.5
     */
    public final void setM32(float m32) {
        this.m32 = m32;
    }

    /**
     * Get the fourth element of the fourth row.
     *
     * @return Returns the m33.
     * @since vecmath 1.5
     */
    public final float getM33() {
        return m33;
    }

    /**
     * Set the fourth element of the fourth row.
     *
     * @param m33 The m33 to setAt.
     * @since vecmath 1.5
     */
    public final void setM33(float m33) {
        this.m33 = m33;
    }
}
