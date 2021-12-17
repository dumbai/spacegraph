package com.jujutsu.tsne;

import com.jujutsu.tsne.matrix.EjmlOps;
import com.jujutsu.tsne.matrix.MatrixOps;
import com.jujutsu.tsne.matrix.MatrixUtils;
import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.ejml.dense.row.CommonOps_DDRM.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EJMLVsPython {
	public static final String s = "1 2 3 4 5\n" +
			"6 7 8 9 5\n" +
			"3 4 2 7 3\n" +
			"7 3 6 7 3\n" +
			"2 4 7 8 9\n" +
			"3 4 3 3 5\n" +
			"8 6 9 4 2\n";

	static final double epsilon = 0.0000001;
	
	static void assertEqualDoubleArrays(double[][] a1, double[][] a2, double tol) {
		for (int i = 0; i < a2.length; i++) {
			assertArrayEquals(a1[i], a2[i], tol);
		}
	}

	@Test
	public void testSum() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
		System.out.println("TSne.sum(X) = " + elementSum(X));
		assertEquals(172.0,elementSum(X), epsilon);
	}

	@Test
	public void testMSum() throws IOException {
		double [] pysum0 = {30.0, 30.0, 38.0, 42.0, 32.0};
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
		DMatrixRMaj sumC = new DMatrixRMaj(1,X.numCols);
		double [][] sum0 = EjmlOps.extractDoubleArray(sumCols(X,sumC));
		for (double[] aSum0 : sum0) {
			for (int j = 0; j < aSum0.length; j++) {
				assertEquals(pysum0[j], aSum0[j], epsilon);
			}
		}
		double [] pysum1 = {15.0, 35.0, 19.0, 26.0, 30.0, 18.0, 29.0};
		DMatrixRMaj sumR = new DMatrixRMaj(X.numRows,1);
		double [][] sum1 = EjmlOps.extractDoubleArray(sumRows(X,sumR));
		for (int i = 0; i < sum1.length; i++) {
			for (int j = 0; j < sum1[i].length; j++) {
				assertEquals(pysum1[i], sum1[i][j],epsilon);
			}
		}
	}

	@Test
	public void testTranspose() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
        transpose(X);
		double [][] transpose = EjmlOps.extractDoubleArray(X);
        double[][] pytranspose = {
                {1.0, 6.0, 3.0, 7.0, 2.0, 3.0, 8.0},
                {2.0, 7.0, 4.0, 3.0, 4.0, 4.0, 6.0},
                {3.0, 8.0, 2.0, 6.0, 7.0, 3.0, 9.0},
                {4.0, 9.0, 7.0, 7.0, 8.0, 3.0, 4.0},
                {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0},};
        assertEqualDoubleArrays(pytranspose, transpose, epsilon);
	}

	@Test
	public void testSquare() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
        DMatrixRMaj sq = new DMatrixRMaj(X.numRows,X.numCols);
		elementPower(X,2,sq);
		double [][] square = EjmlOps.extractDoubleArray(sq);
        double[][] pysquare = {
                {1.0, 4.0, 9.0, 16.0, 25.0},
                {36.0, 49.0, 64.0, 81.0, 25.0},
                {9.0, 16.0, 4.0, 49.0, 9.0},
                {49.0, 9.0, 36.0, 49.0, 9.0},
                {4.0, 16.0, 49.0, 64.0, 81.0},
                {9.0, 16.0, 9.0, 9.0, 25.0},
                {64.0, 36.0, 81.0, 16.0, 4.0},
        };
        assertEqualDoubleArrays(pysquare, square, epsilon);
	}
	
	@Test
	public void testTimes() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
        DMatrixRMaj tr = new DMatrixRMaj(X.numCols,X.numRows);
		DMatrixRMaj mult = new DMatrixRMaj(X.numRows,tr.numCols);
		transpose(X,tr);
		mult(X,tr,mult);
		double [][] times = EjmlOps.extractDoubleArray(mult);
        double[][] pydot = {
                {55.0, 105.0, 60.0, 74.0, 108.0, 57.0, 73.0},
                {105.0, 255.0, 140.0, 189.0, 213.0, 122.0, 208.0},
                {60.0, 140.0, 87.0, 103.0, 119.0, 67.0, 100.0},
                {74.0, 189.0, 103.0, 152.0, 151.0, 87.0, 162.0},
                {108.0, 213.0, 119.0, 151.0, 214.0, 112.0, 153.0},
                {57.0, 122.0, 67.0, 87.0, 112.0, 68.0, 97.0},
                {73.0, 208.0, 100.0, 162.0, 153.0, 97.0, 201.0}
        };
        assertEqualDoubleArrays(pydot, times, epsilon);
	}

	@Test
	public void testScaleTimes() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
        scale(-2.0,X);
		double [][] scale = EjmlOps.extractDoubleArray(X);
        double[][] pyscle = {{-2.0, -4.0, -6.0, -8.0, -10.0},
                {-12.0, -14.0, -16.0, -18.0, -10.0},
                {-6.0, -8.0, -4.0, -14.0, -6.0},
                {-14.0, -6.0, -12.0, -14.0, -6.0},
                {-4.0, -8.0, -14.0, -16.0, -18.0},
                {-6.0, -8.0, -6.0, -6.0, -10.0},
                {-16.0, -12.0, -18.0, -8.0, -4.0},
        };
        assertEqualDoubleArrays(pyscle, scale, epsilon);
	}

	@Test
	public void testScalarPlus() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
        add(X,2);
		double [][] plus = EjmlOps.extractDoubleArray(X);
        double[][] pyplus = {{3.0, 4.0, 5.0, 6.0, 7.0},
                {8.0, 9.0, 10.0, 11.0, 7.0},
                {5.0, 6.0, 4.0, 9.0, 5.0},
                {9.0, 5.0, 8.0, 9.0, 5.0},
                {4.0, 6.0, 9.0, 10.0, 11.0},
                {5.0, 6.0, 5.0, 5.0, 7.0},
                {10.0, 8.0, 11.0, 6.0, 4.0},
        };
        assertEqualDoubleArrays(pyplus, plus, epsilon);
	}
	
	@Test
	public void testScalarInverse() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
        divide(1.0, X);
		double [][] inv = EjmlOps.extractDoubleArray(X);
        double[][] pyinv = {{1.0, 0.5, 0.33333333, 0.25, 0.2},
                {0.16666667, 0.14285714, 0.125, 0.11111111, 0.2},
                {0.33333333, 0.25, 0.5, 0.14285714, 0.33333333},
                {0.14285714, 0.33333333, 0.16666667, 0.14285714, 0.33333333},
                {0.5, 0.25, 0.14285714, 0.125, 0.11111111},
                {0.33333333, 0.25, 0.33333333, 0.33333333, 0.2},
                {0.125, 0.16666667, 0.11111111, 0.25, 0.5}
        };
        assertEqualDoubleArrays(pyinv, inv, epsilon);
	}
	
	@Test
	public void testScalarInverseVector() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
		double [] pyinv = { 0.14285714,  0.33333333,  0.16666667,  0.14285714,  0.33333333 };
		DMatrixRMaj row = extract(X,3,4,0,5);
		divide(1.0, row);
		double [] inv = EjmlOps.extractDoubleArray(row)[0];
		assertArrayEquals(inv, inv, epsilon);
	}

	@Test
	public void testScalarDivide() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
		divide(X, 2.0);
		double [][] div = EjmlOps.extractDoubleArray(X); 
		double [][] pydiv = 
				{{ 0.5, 1.0,   1.5, 2.0,   2.5},
				 {3.0,   3.5, 4.0,   4.5,  2.5},
				 { 1.5, 2.0, 1.0,   3.5,  1.5},
				 { 3.5,  1.5, 3.0,   3.5,  1.5},
				 {1.0, 2.0,   3.5, 4.0,   4.5},
				 { 1.5, 2.0,   1.5,  1.5,  2.5},
				 {4.0, 3.0,   4.5, 2.0, 1.0},
				 };
		assertEqualDoubleArrays(pydiv, div, epsilon);
	}

	@Test
	public void testScalarMultiply() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
        elementMult(X,X);
		double [][] sm = EjmlOps.extractDoubleArray(X);
        double[][] pysm = {{1.0, 4.0, 9.0, 16.0, 25.0},
                {36.0, 49.0, 64.0, 81.0, 25.0},
                {9.0, 16.0, 4.0, 49.0, 9.0},
                {49.0, 9.0, 36.0, 49.0, 9.0},
                {4.0, 16.0, 49.0, 64.0, 81.0},
                {9.0, 16.0, 9.0, 9.0, 25.0},
                {64.0, 36.0, 81.0, 16.0, 4.0},
        };
        assertEqualDoubleArrays(pysm, sm, epsilon);
	}

	@Test
	public void testRangeAssign() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
		EjmlOps.assignAtIndex(X, MatrixOps.range(4), MatrixOps.range(4), 0);
		double [][] pyasgn = 
				{{0.0, 2.0, 3.0, 4.0, 5.0},
				 {6.0, 0.0, 8.0, 9.0, 5.0},
				 {3.0, 4.0, 0.0, 7.0, 3.0},
				 {7.0, 3.0, 6.0, 0.0, 3.0},
				 {2.0, 4.0, 7.0, 8.0, 9.0},
				 {3.0, 4.0, 3.0, 3.0, 5.0},
				 {8.0, 6.0, 9.0, 4.0, 2.0},
				 };
		assertEqualDoubleArrays(pyasgn, EjmlOps.extractDoubleArray(X), epsilon);
	}

	@Test
	public void testMinus() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
		DMatrixRMaj mins = new DMatrixRMaj(X.numRows,X.numCols);
        subtract(X, X, mins);
		double [][] min = EjmlOps.extractDoubleArray(mins);
        double[][] pymin = {{0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0, 0.0, 0.0},
        };
        assertEqualDoubleArrays(pymin, min, epsilon);
	}

	@Test
	public void testTile() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		double [][] PQrowi  = MatrixOps.copyCols(Xin,4);
		DMatrixRMaj tile = new DMatrixRMaj(PQrowi);
		DMatrixRMaj X = EjmlOps.tile(tile, 3, 1);
		double [][] pytile1 = 
				{{5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0,},
				 {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0,},
				 {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0,},
				};
		assertEqualDoubleArrays(pytile1, EjmlOps.extractDoubleArray(X), epsilon);
		X = EjmlOps.tile(tile, 3, 2);
		double [][] pytile2 =
				{{5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0, 5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0},
				 {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0, 5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0},
				 {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0, 5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0},
				 };
		assertEqualDoubleArrays(pytile2, EjmlOps.extractDoubleArray(X), epsilon);
	}
	
	@Test
	public void testAssignCol() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
		DMatrixRMaj sumR = new DMatrixRMaj(1,X.numCols);
		sumCols(X,sumR);
        insert(sumR,X,3,0);
        double[][] pyasgn = {{1.0, 2.0, 3.0, 4.0, 5.0},
                {6.0, 7.0, 8.0, 9.0, 5.0},
                {3.0, 4.0, 2.0, 7.0, 3.0},
                {30.0, 30.0, 38.0, 42.0, 32.0},
                {2.0, 4.0, 7.0, 8.0, 9.0},
                {3.0, 4.0, 3.0, 3.0, 5.0},
                {8.0, 6.0, 9.0, 4.0, 2.0},
        };
        assertEqualDoubleArrays(pyasgn, EjmlOps.extractDoubleArray(X), epsilon);
	}

	@Test
	public void testAssignAllLessThan() throws IOException {
		double [][] Xin = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		DMatrixRMaj X = new DMatrixRMaj(Xin);
		EjmlOps.assignAllLessThan(X,3,-1);
		double [][] pylt =
				{{-1.0, -1.0, 3.0, 4.0, 5.0},
				 {6.0, 7.0, 8.0, 9.0, 5.0},
				 {3.0, 4.0, -1.0, 7.0, 3.0},
				 {7.0, 3.0, 6.0, 7.0, 3.0},
				 {-1.0, 4.0, 7.0, 8.0, 9.0},
				 {3.0, 4.0, 3.0, 3.0, 5.0},
				 {8.0, 6.0, 9.0, 4.0, -1.0},
				 };
		assertEqualDoubleArrays(pylt, EjmlOps.extractDoubleArray(X), epsilon);
	}
	
	@Test
	public void testSign() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		MatrixOps.assignAllLessThan(X,3,-1);
		
		
	}
	
	@Test
	public void testEqual() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		double [][] Y = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		MatrixOps.assignAllLessThan(X,3,-1);
		MatrixOps.assignAllLessThan(Y,2,-1);
		System.out.println("equal(sign(X),sign(Y) =");
		printBoolMtx(MatrixOps.equal(MatrixOps.sign(X), MatrixOps.sign(Y)));
	}
	
	public static void printBoolMtx(boolean[][] mtx) {
		for (boolean[] aMtx : mtx) {
			for (int j = 0; j < mtx[0].length; j++) {
				System.out.print(aMtx[j] + ", ");
			}
			System.out.println();
		}
	}

	@Test
	public void testMMean() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		double [] pymean0 = { 4.28571429,  4.28571429,  5.42857143, 6.0,          4.57142857};
		double [][] mean0 = MatrixOps.mean(X,0);
		assertArrayEquals(mean0[0], mean0[0], epsilon);
		double [] pymean1 = {3.0, 7.0,   3.8,  5.2, 6.0,   3.6,  5.8};
		
		double [][] mean1mtrx = MatrixOps.mean(X,1);
		double [] mean1 = new double [mean1mtrx.length];
		for (int i = 0; i < mean1mtrx.length; i++) {
			for (int j = 0; j < mean1mtrx[i].length; j++) {				
				mean1[i] = mean1mtrx[i][j];
			}
		}
		assertArrayEquals(mean1, mean1, epsilon);
	}
	
	@Test
	public void testVMean() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		System.out.println("TSne.mean(X[3,:]) = \n" + MatrixOps.mean(X[3]));
		assertEquals(5.2, MatrixOps.mean(X[3]), epsilon);
	}

	@Test
	public void testElementWiseDivide() throws IOException {
		
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		double [][] Y = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		Y = MatrixOps.scalarDivide(Y, 2);
		
		
		
		double [][] pydiv = 
			{{2.0, 2.0, 2.0, 2.0, 2.0},
			 {2.0, 2.0, 2.0, 2.0, 2.0},
			 {2.0, 2.0, 2.0, 2.0, 2.0},
			 {2.0, 2.0, 2.0, 2.0, 2.0},
			 {2.0, 2.0, 2.0, 2.0, 2.0},
			 {2.0, 2.0, 2.0, 2.0, 2.0},
			 {2.0, 2.0, 2.0, 2.0, 2.0},
			};
		double [][] div = MatrixOps.scalarDivide(X, Y);
		assertEqualDoubleArrays(pydiv, div, epsilon);
	}
	
	@Test
	public void testSqrt() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		double [] pysqrt = { 2.64575131,  1.73205081,  2.44948974,  2.64575131,  1.73205081};
		
		double[] v2 = MatrixOps.sqrt(X[3]);
		assertArrayEquals(v2, v2, epsilon);
	}
	
	@Test
	public void testExp() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		double [][] pyexp = 
				{{  2.71828183e+00,   7.38905610e+00,   2.00855369e+01,   5.45981500e+01,	    1.48413159e+02},
				 {  4.03428793e+02,   1.09663316e+03,   2.98095799e+03,   8.10308393e+03,	    1.48413159e+02},
				 {  2.00855369e+01,   5.45981500e+01,   7.38905610e+00,   1.09663316e+03,	    2.00855369e+01},
				 {  1.09663316e+03,   2.00855369e+01,   4.03428793e+02,   1.09663316e+03,	    2.00855369e+01},
				 {  7.38905610e+00,   5.45981500e+01,   1.09663316e+03,   2.98095799e+03,	    8.10308393e+03},
				 {  2.00855369e+01,   5.45981500e+01,   2.00855369e+01,   2.00855369e+01,	    1.48413159e+02},
				 {  2.98095799e+03,   4.03428793e+02,   8.10308393e+03,   5.45981500e+01,	    7.38905610e+00}};
		double [][] jexp = MatrixOps.exp(X);
		assertEqualDoubleArrays(pyexp, jexp, 0.00001);
	}
	
	@Test
	public void testLog() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		double [][] pylog = 
				{{0.0,          0.69314718,  1.09861229,  1.38629436,  1.60943791},
				 { 1.79175947,  1.94591015,  2.07944154,  2.19722458,  1.60943791},
				 { 1.09861229,  1.38629436,  0.69314718,  1.94591015,  1.09861229},
				 { 1.94591015,  1.09861229,  1.79175947,  1.94591015,  1.09861229},
				 { 0.69314718,  1.38629436,  1.94591015,  2.07944154,  2.19722458},
				 { 1.09861229,  1.38629436,  1.09861229,  1.09861229,  1.60943791},
				 { 2.07944154,  1.79175947,  2.19722458,  1.38629436,  0.69314718},
				 };
		double [][] jlog = MatrixOps.log(X);
		assertEqualDoubleArrays(pylog, jlog, epsilon);
	}
		
	@Test 
	public void testConcatenate() {
		int [] v1 = {1,2,3,4};
		int [] v2 = {3,4,5,6};
		
		
		int [] v3 = MatrixOps.concatenate(v1, v2);
		int [] expct = {1,2,3,4,3,4,5,6};
		assertArrayEquals(expct, v3);
	}

}