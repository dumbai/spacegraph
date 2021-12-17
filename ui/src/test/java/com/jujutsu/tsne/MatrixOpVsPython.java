package com.jujutsu.tsne;

import com.jujutsu.tsne.matrix.MatrixOps;
import com.jujutsu.tsne.matrix.MatrixUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static com.jujutsu.tsne.EJMLVsPython.s;
import static com.jujutsu.tsne.matrix.MatrixOps.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatrixOpVsPython {
			
	static final double epsilon = 0.0000001;
	
	static void assertEqualDoubleArrays(double[][] a1, double[][] a2, double tol) {
		for (int i = 0; i < a2.length; i++) {
			assertArrayEquals(a1[i], a2[i], tol);
		}
	}

	@Test
	public void testSum() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		System.out.println("TSne.sum(X) = " + MatrixOps.sum(X));
		assertEquals(172.0,MatrixOps.sum(X), epsilon);
	}

	@Test
	public void testMSum() throws IOException {
		double [] pysum0 = {30.0, 30.0, 38.0, 42.0, 32.0};
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		double [][] sum0 = MatrixOps.sum(X,0);
		for (double[] aSum0 : sum0) {
			for (int j = 0; j < aSum0.length; j++) {
				assertEquals(pysum0[j], aSum0[j], epsilon);
			}
		}
		double [] pysum1 = {15.0, 35.0, 19.0, 26.0, 30.0, 18.0, 29.0};
		
		double [][] sum1 = MatrixOps.sum(X,1);
		for (int i = 0; i < sum1.length; i++) {
			for (int j = 0; j < sum1[i].length; j++) {
				assertEquals(pysum1[i], sum1[i][j],epsilon);
			}
		}
	}

	@Test
	public void testTranspose() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pytranspose = {
				{1.0, 6.0, 3.0, 7.0, 2.0, 3.0, 8.0},
				{2.0, 7.0, 4.0, 3.0, 4.0, 4.0, 6.0},
				{3.0, 8.0, 2.0, 6.0, 7.0, 3.0, 9.0},
				{4.0, 9.0, 7.0, 7.0, 8.0, 3.0, 4.0},
				{5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0},};
		double [][] transpose = transpose(X);
		assertEqualDoubleArrays(pytranspose, transpose, epsilon);
	}

	@Test
	public void testSquare() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pysquare = {
				{1.0, 4.0, 9.0, 16.0, 25.0},
				{36.0, 49.0, 64.0, 81.0, 25.0},
				{9.0, 16.0, 4.0, 49.0, 9.0},
				{49.0, 9.0, 36.0, 49.0, 9.0},
				{4.0, 16.0, 49.0, 64.0, 81.0},
				{9.0, 16.0, 9.0, 9.0, 25.0},
				{64.0, 36.0, 81.0, 16.0, 4.0},
				};
		double [][] square = MatrixOps.square(X);
		assertEqualDoubleArrays(pysquare, square, epsilon);
	}
	
	@Test
	public void testTimes() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pydot = {
				{55.0, 105.0, 60.0, 74.0, 108.0, 57.0, 73.0},
				{105.0, 255.0, 140.0, 189.0, 213.0, 122.0, 208.0},
				{60.0, 140.0, 87.0, 103.0, 119.0, 67.0, 100.0},
				{74.0, 189.0, 103.0, 152.0, 151.0, 87.0, 162.0},
				{108.0, 213.0, 119.0, 151.0, 214.0, 112.0, 153.0},
				{57.0, 122.0, 67.0, 87.0, 112.0, 68.0, 97.0},
				{73.0, 208.0, 100.0, 162.0, 153.0, 97.0, 201.0}
				};
		double [][] times = MatrixOps.times(X,transpose(X));
		assertEqualDoubleArrays(pydot, times, epsilon);
	}

	@Test
	public void testScaleTimes() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pyscle = 
				{{ -2.0,  -4.0,  -6.0,  -8.0, -10.0},
				 {-12.0, -14.0, -16.0, -18.0, -10.0},
				 { -6.0,  -8.0,  -4.0, -14.0,  -6.0},
				 {-14.0,  -6.0, -12.0, -14.0,  -6.0},
				 { -4.0,  -8.0, -14.0, -16.0, -18.0},
				 { -6.0,  -8.0,  -6.0,  -6.0, -10.0},
				 {-16.0, -12.0, -18.0,  -8.0,  -4.0},
				 };
		double [][] scale = MatrixOps.scalarMult(X,-2);
		assertEqualDoubleArrays(pyscle, scale, epsilon);
	}

	@Test
	public void testScalarPlus() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pyplus = 
				{{3.0, 4.0, 5.0, 6.0, 7.0},
				 {8.0, 9.0, 10.0, 11.0, 7.0},
				 {5.0, 6.0, 4.0, 9.0, 5.0},
				 {9.0, 5.0, 8.0, 9.0, 5.0},
				 {4.0, 6.0, 9.0, 10.0, 11.0},
				 {5.0, 6.0, 5.0, 5.0, 7.0},
				 {10.0, 8.0, 11.0, 6.0, 4.0},
				 };
		double [][] plus = MatrixOps.scalarPlus(X,2);
		assertEqualDoubleArrays(pyplus, plus, epsilon);
	}
	
	@Test
	public void testScalarInverse() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pyinv = 
				{{1.0,          0.5,         0.33333333,  0.25,        0.2       },
				 { 0.16666667,  0.14285714,  0.125,       0.11111111,  0.2       },
				 { 0.33333333,  0.25,        0.5,         0.14285714,  0.33333333},
				 { 0.14285714,  0.33333333,  0.16666667,  0.14285714,  0.33333333},
				 { 0.5,         0.25,        0.14285714,  0.125,       0.11111111},
				 { 0.33333333,  0.25,        0.33333333,  0.33333333,  0.2       },
				 { 0.125,       0.16666667,  0.11111111,  0.25,        0.5       }
				 };
		double [][] inv = MatrixOps.scalarInverse(X);
		assertEqualDoubleArrays(pyinv, inv, epsilon);
	}
	
	@Test
	public void testScalarInverseVector() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [] pyinv = { 0.14285714,  0.33333333,  0.16666667,  0.14285714,  0.33333333 };
		double [] inv = MatrixOps.scalarInverse(X[3]);
		assertArrayEquals(pyinv, inv, epsilon);
	}

	@Test
	public void testScalarDivide() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] div = MatrixOps.scalarDivide(X, 2);
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
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pysm = 
				{{1.0, 4.0, 9.0, 16.0, 25.0},
				 {36.0, 49.0, 64.0, 81.0, 25.0},
				 {9.0, 16.0, 4.0, 49.0, 9.0},
				 {49.0, 9.0, 36.0, 49.0, 9.0},
				 {4.0, 16.0, 49.0, 64.0, 81.0},
				 {9.0, 16.0, 9.0, 9.0, 25.0},
				 {64.0, 36.0, 81.0, 16.0, 4.0},
				 };
		double [][] sm = scalarMultiply(X, X);
		assertEqualDoubleArrays(pysm, sm, epsilon);
	}

	@Test
	public void testRangeAssign() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		MatrixOps.assignAtIndex(X, MatrixOps.range(4), MatrixOps.range(4), 0);
		
		
		double [][] pyasgn = 
				{{0.0, 2.0, 3.0, 4.0, 5.0},
				 {6.0, 0.0, 8.0, 9.0, 5.0},
				 {3.0, 4.0, 0.0, 7.0, 3.0},
				 {7.0, 3.0, 6.0, 0.0, 3.0},
				 {2.0, 4.0, 7.0, 8.0, 9.0},
				 {3.0, 4.0, 3.0, 3.0, 5.0},
				 {8.0, 6.0, 9.0, 4.0, 2.0},
				 };
		assertEqualDoubleArrays(pyasgn, X, epsilon);
	}

	@Test
	public void testMinus() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pymin = 
				{{0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 };
		double [][] min = MatrixOps.sMinus(X, X);
		assertEqualDoubleArrays(pymin, min, epsilon);
	}

	@Test
	public void testParMinus() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		
		
		double [][] pymin = 
				{{0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 {0.0, 0.0, 0.0, 0.0, 0.0},
				 };
		double [][] min = parScalarMinus(X, X);
		assertEqualDoubleArrays(pymin, min, epsilon);
	}

	@Test
	public void testTile() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		double [][] PQrowi  = MatrixOps.copyCols(X,4);
		
		
		double [][] pytile1 = 
				{{5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0,},
				 {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0,},
				 {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0,},
				};
		double [][] tile1 = MatrixOps.tile(PQrowi, 3, 1);
		assertEqualDoubleArrays(pytile1, tile1, epsilon);
		
		
		double [][] pytile2 =
				{{5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0, 5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0},
				 {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0, 5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0},
				 {5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0, 5.0, 5.0, 3.0, 3.0, 9.0, 5.0, 2.0},
				 };
		double [][] tile2 = MatrixOps.tile(PQrowi, 3, 2);
		assertEqualDoubleArrays(pytile2, tile2, epsilon);
	}
	
	@Test
	public void testAssignCol() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		X[3] = MatrixOps.sum(X,0)[0];
		
		
		double [][] pyasgn = 
				{{1.0, 2.0, 3.0, 4.0, 5.0},
				 {6.0, 7.0, 8.0, 9.0, 5.0},
				 {3.0, 4.0, 2.0, 7.0, 3.0},
				 {30.0, 30.0, 38.0, 42.0, 32.0},
				 {2.0, 4.0, 7.0, 8.0, 9.0},
				 {3.0, 4.0, 3.0, 3.0, 5.0},
				 {8.0, 6.0, 9.0, 4.0, 2.0},
				 };
		assertEqualDoubleArrays(pyasgn, X, epsilon);
	}

	@Test
	public void testAssignAllLessThan() throws IOException {
		double [][] X = MatrixUtils.simpleRead2DMatrix(new StringReader(s), " ");
		MatrixOps.assignAllLessThan(X,3,-1);
		
		
		double [][] pylt =
				{{-1.0, -1.0, 3.0, 4.0, 5.0},
				 {6.0, 7.0, 8.0, 9.0, 5.0},
				 {3.0, 4.0, -1.0, 7.0, 3.0},
				 {7.0, 3.0, 6.0, 7.0, 3.0},
				 {-1.0, 4.0, 7.0, 8.0, 9.0},
				 {3.0, 4.0, 3.0, 3.0, 5.0},
				 {8.0, 6.0, 9.0, 4.0, -1.0},
				 };
		assertEqualDoubleArrays(pylt, X, epsilon);
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
		assertArrayEquals(pymean0, mean0[0], epsilon);

        double [][] mean1mtrx = MatrixOps.mean(X,1);
		double [] mean1 = new double [mean1mtrx.length];
		for (int i = 0; i < mean1mtrx.length; i++) {
			for (int j = 0; j < mean1mtrx[i].length; j++) {				
				mean1[i] = mean1mtrx[i][j];
			}
		}
        double[] pymean1 = {3.0, 7.0, 3.8, 5.2, 6.0, 3.6, 5.8};
        assertArrayEquals(pymean1, mean1, epsilon);
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
		
		assertArrayEquals(pysqrt, MatrixOps.sqrt(X[3]), epsilon);
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
	
	@Test 
	public void testDiagWide() {
		double [][] diag = {{1,2,3,4,5}};
		double [][] dmatrix = MatrixOps.diag(diag);
		for (int i = 0; i < dmatrix.length; i++) {
			for (int j = 0; j < dmatrix[0].length; j++) {
				if(i==j) assertEquals(dmatrix[i][j],diag[0][i],epsilon);
			}
		}
	}

	@Test 
	public void testDiagLong() {
		double [][] diag = {{1},{2},{3},{4},{5}};
		double [][] dmatrix = MatrixOps.diag(diag);
		for (int i = 0; i < dmatrix.length; i++) {
			for (int j = 0; j < dmatrix[0].length; j++) {
				if(i==j) assertEquals(dmatrix[i][j],diag[i][0],epsilon);
			}
		}
	}

}