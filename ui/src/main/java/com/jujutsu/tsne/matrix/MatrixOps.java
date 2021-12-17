package com.jujutsu.tsne.matrix;

import jcog.Str;
import jcog.util.ArrayUtil;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;


public enum MatrixOps { ;

	//private static final DecimalFormat mydecimalFormat = new DecimalFormat("00.###E0");

	//private static final String DEFAULT_TITLE  = "Vector";
	//private static final int noDigits = 4;
































	public static String doubleArrayToPrintString(double[][] m) {
		return doubleArrayToPrintString(m, ", ", Integer.MAX_VALUE, m.length, Integer.MAX_VALUE, "\n");
	}













	public static String doubleArrayToPrintString(double[][] m, int toprowlim, int btmrowlim) {
		return doubleArrayToPrintString(m, ", ", toprowlim, btmrowlim, Integer.MAX_VALUE, "\n");
	}













	private static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim, int btmrowlim, int collim, String sentenceDelimiter) {
		StringBuilder str = new StringBuilder(m.length * m[0].length);

		str.append("Dim:").append(m.length).append(" x ").append(m[0].length).append('\n');

		int i = 0;
		for (; i < m.length && i < toprowlim; i++) {
			String rowPref = String.format(i < 1000 ? "%03d" : "%04d", i);
			str.append(rowPref).append(": [");
			for (int j = 0; j < m[i].length - 1 && j < collim; j++) {
				str.append(formatDouble(m[i][j])).append(colDelimiter);
			}
			str.append(formatDouble(m[i][m[i].length - 1])).append(collim == Integer.MAX_VALUE ? "]" : "...]");

			if (i < m.length - 1)
				str.append(sentenceDelimiter);

		}
		if(btmrowlim<0) return str.toString();
		while(i<(m.length-btmrowlim)) i++;
		if( i < m.length) str.append("\t.\n\t.\n\t.\n");
		for (; i < m.length; i++) {
			String rowPref = String.format(i < 1000 ? "%03d" : "%04d", i);
			str.append(rowPref).append(": [");
			for (int j = 0; j < m[i].length - 1 && j < collim; j++) {
				str.append(formatDouble(m[i][j])).append(colDelimiter);
			}
			str.append(formatDouble(m[i][m[i].length - 1]));

			str.append( collim > m[i].length ? "]" : ", ...]");

			if (i < m.length - 1) {
				str.append(sentenceDelimiter);
			}
		}
		return str.toString();
	}

	@Deprecated
	private static String formatDouble(double d) {
	    return Str.n4(d);







	}




























































































































	/**
	 * Returns a new matrix which is the transpose of input matrix
	 * @param matrix
	 * @return
	 */
	public static double[][] transposeSerial(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		double[][] transpose = new double[cols][rows];
		for (int col = 0; col < cols; col++) {
			double[] tc = transpose[col];
			for (int row = 0; row < rows; row++) {
				tc[row] = matrix[row][col];
			}
		}
		return transpose;
	}

	
	public static double[][] transpose(double[][] matrix) {
		return transpose(matrix, 1000);
	}
	
	
	/**
	 * Returns a new matrix which is the transpose of input matrix
	 * @param matrix
	 * @return
	 */
	public static double[][] transpose(double[][] matrix, int ll) {
		int cols = matrix[0].length;
		int rows = matrix.length;
		double[][] transpose = new double[cols][rows];

			for (int i = 0; i < cols; i++)
				for (int j = 0; j < rows; j++)
					transpose[i][j] = matrix[j][i];




		return transpose;
	}


















































	
	/**
	 * Returns a new matrix with values exponentiated
	 * @param matrix
	 * @return new matrix with values exponentiated
	 */
	public static double [][] exp(double [][] m1) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.exp(m1[i][j]);
			}
		}
		return matrix;
	}

	
	/**
	 * Returns new vetcor with vals sqrt'ed
	 * @param vector
	 * @return new vector with values sqrt'ed
	 */
	public static double [] sqrt(double [] v1) {
		double [] vector = new double[v1.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = Math.sqrt(v1[i]);
		}
		return vector;
	}

	
	/**
	 * @param vector
	 * @return mean of values in vector
	 */
	public static double mean(double [] vector) {
		double sum = 0.0;
		for (double aVector : vector) {
			sum += aVector;
		}
		return sum/vector.length;
	}

	
	/**
	 * Returns a new matrix with values that are the log of the input matrix
	 * @param matrix
	 * @return  same matrix with values log'ed
	 */
	public static double [][] log(double [][] m1) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.log(m1[i][j]);
			}
		}
		return matrix;
	}

	/**
	 * Returns a new matrix with values that are taken to the power of the input matrix
	 * @param matrix
	 * @param power
	 * @return  same matrix with values pow'ed
	 */
	public static double [][] pow(double [][] m1, double power) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.pow(m1[i][j], power);
			}
		}
		return matrix;
	}

	/**
	 * Returns a new matrix with values that are taken to the power of the input matrix
	 * @param matrix
	 * @param power
	 * @return  same matrix with values pow'ed
	 */
	public static double [] pow(double [] m1, double power) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = Math.pow(m1[i], power);
		}
		return matrix;
	}

	/**
	 * Returns a new matrix with values that are the log of the input matrix
	 * @param matrix
	 * @param infAsZero treat +- Infinity as zero, i.e replaces Infinity with 0.0
	 * if set to true
	 * @return  same matrix with values log'ed
	 */
	public static double [][] log(double [][] m1, boolean infAsZero) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.log(m1[i][j]);
				if(infAsZero && Double.isInfinite(matrix[i][j]))
					matrix[i][j] = 0.0;
			}
		}
		return matrix;
	}

	
	/**
	 * @param matrix
	 * @return scalar inverse of matrix
	 */
	public static double [][] scalarInverse(double [][] m1) {
		return scalarInverse(m1, null);
	}

	public static double [][] scalarInverse(double [][] x, double[][] y) {
		if (y == null)
			y = new double[x.length][x[0].length];

		for (int i = 0; i < y.length; i++) {
			double[] mi = x[i], ri = y[i];
			for (int j = 0; j < y[0].length; j++)
				ri[j] = 1 / mi[j];
		}
		return y;
	}
	
	/**
	 * @param vector
	 * @return scalar inverse of vector
	 */
	public static double [] scalarInverse(double [] v1) {
		double [] vector = new double[v1.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = 1/v1[i];
		}
		return vector;
	}

	/**
	 * @param m
	 * @param n
	 * @return new 2D matrix with normal random values with mean 0 and std. dev 1
	 */
	public static double[][] rnorm(int m, int n) {
		double[][] array = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < array[i].length; j++) {				
				array[i][j] = rnorm(0.0,1.0);
			}
		}
		return array;
	}
	
	public static double [] rnorm(int n, double [] mus, double [] sigmas) {
		double [] res = new double[n];
		for (int i = 0; i < res.length; i++) {
			res[i] = mus[i] + (ThreadLocalRandom.current().nextGaussian() * sigmas[i]);
		}
		return res;
	}

	public static double [] rnorm(int n, double mu, double [] sigmas) {
		double [] res = new double[n];
		for (int i = 0; i < res.length; i++) {
			res[i] = mu + (ThreadLocalRandom.current().nextGaussian() * sigmas[i]);
		}
		return res;
	}

	public static double rnorm() {
		return ThreadLocalRandom.current().nextGaussian();
	}

	/**
	 * Generate random draw from Normal with mean mu and std. dev sigma
	 * @param mu
	 * @param sigma
	 * @return random sample
	 */
	private static double rnorm(double mu, double sigma) {
		return mu + (ThreadLocalRandom.current().nextGaussian() * sigma);
	}

	
	/**
	 * Returns a new matrix of booleans where true is set if the values to the two matrices are
	 * the same at that index
	 * @param matrix1
	 * @param matrix2
	 * @return new matrix with booelans with values matrix1[i,j] == matrix2[i,j]
	 */
	public static boolean [][] equal(double [][] matrix1, double [][] matrix2) {
		boolean [][] equals = new boolean[matrix1.length][matrix1[0].length];
		if( matrix1.length != matrix2.length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		if( matrix1[0].length != matrix2[0].length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		for (int i = 0; i < matrix1.length; i++) {
			for (int j = 0; j < matrix1[0].length; j++) {
				equals[i][j] = Double.compare(matrix1[i][j], matrix2[i][j]) == 0;
			}
		}
		return equals;
	}

	/**
	 * Returns a new matrix of booleans where true is set if the values to the two matrices are
	 * the same at that index
	 * @param matrix1
	 * @param matrix2
	 * @return new matrix with booelans with values matrix1[i,j] == matrix2[i,j]
	 */
	public static boolean [][] equal(boolean [][] matrix1, boolean [][] matrix2) {
		boolean [][] equals = new boolean[matrix1.length][matrix1[0].length];
		if( matrix1.length != matrix2.length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		if( matrix1[0].length != matrix2[0].length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		for (int i = 0; i < matrix1.length; i++) {
			for (int j = 0; j < matrix1[0].length; j++) {
				equals[i][j] = (matrix1[i][j] == matrix2[i][j]);
			}
		}
		return equals;
	}

	/**
	 * Returns a new matrix of booleans where true is set if the value in the matrix is
	 * bigger than value
	 * @param matrix
	 * @param value
	 * @return new matrix with booelans with values matrix1[i,j] == matrix2[i,j]
	 */
	public static boolean [][] biggerThan(double [][] matrix, double value) {
		boolean [][] equals = new boolean[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				equals[i][j] = Double.compare(matrix[i][j], value) == 1;
			}
		}
		return equals;
	}

	/**
	 * @param booleans
	 * @return new matrix with booleans which are the negations of the input
	 */
	public static boolean [][] negate(boolean [][] booleans) {
		boolean [][] negates = new boolean[booleans.length][booleans[0].length];
		for (int i = 0; i < booleans.length; i++) {
			for (int j = 0; j < booleans[0].length; j++) {
				negates[i][j] = !booleans[i][j];
			}
		}
		return negates;
	}

	/**
	 * @param booleans
	 * @return
	 */
	public static double [][] abs(boolean [][] booleans) {
		double [][] absolutes = new double[booleans.length][booleans[0].length];
		for (int i = 0; i < booleans.length; i++) {
			for (int j = 0; j < booleans[0].length; j++) {
				absolutes[i][j] = booleans[i][j] ? 1 : 0;
			}
		}
		return absolutes;
	}

	/**
	 * @param vals
	 * @return
	 */
	public static double [][] abs(double [][] vals) {
		double [][] absolutes = new double[vals.length][vals[0].length];
		for (int i = 0; i < vals.length; i++) {
			for (int j = 0; j < vals[0].length; j++) {
				absolutes[i][j] = Math.abs(vals[i][j]);
			}
		}
		return absolutes;
	}

	/**
	 * @param absolutes
	 * @return
	 */
	public static double [] abs(double [] vals) {
		double [] absolutes = new double[vals.length];
		for (int i = 0; i < vals.length; i++) {
			absolutes[i] = Math.abs(vals[i]);
		}
		return absolutes;
	}

	public static double [][] sign(double [][] matrix) {
		double [][] signs = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				signs[i][j] = matrix[i][j] >= 0 ? 1 : -1;
			}
		}
		return signs;
	}

	private static double mean(double[][] matrix) {
		return mean(matrix,2)[0][0];
	}
	
	
	public static double [][] mean(double [][] matrix, int axis) {
		
		
		
		double [][] result;
        switch (axis) {
            case 0 -> {
                result = new double[1][matrix[0].length];
                for (int j = 0; j < matrix[0].length; j++) {
                    double colsum = 0.0;
                    for (double[] aMatrix : matrix) {
                        colsum += aMatrix[j];
                    }
                    result[0][j] = colsum / matrix.length;
                }
            }
            case 1 -> {
                result = new double[matrix.length][1];
                for (int i = 0; i < matrix.length; i++) {
                    double rowsum = 0.0;
                    for (int j = 0; j < matrix[0].length; j++) {
                        rowsum += matrix[i][j];
                    }
                    result[i][0] = rowsum / matrix[0].length;
                }
            }
            case 2 -> {
                result = new double[1][1];
                for (int j = 0; j < matrix[0].length; j++) {
                    for (double[] aMatrix : matrix) {
                        result[0][0] += aMatrix[j];
                    }
                }
                result[0][0] /= (matrix[0].length * matrix.length);
            }
            default -> throw new IllegalArgumentException("Axes other than 0,1,2 is unsupported");
        }
		return result;
	}

	
	
	public static double [][] sum(double [][] matrix, int axis) {
		
		
		double [][] result;
        switch (axis) {
            case 0 -> {
                result = new double[1][matrix[0].length];
                for (int j = 0; j < matrix[0].length; j++) {
                    double rowsum = 0.0;
                    for (double[] aMatrix : matrix) {
                        rowsum += aMatrix[j];
                    }
                    result[0][j] = rowsum;
                }
            }
            case 1 -> {
                result = new double[matrix.length][1];
                for (int i = 0; i < matrix.length; i++) {
                    double colsum = 0.0;
                    for (int j = 0; j < matrix[0].length; j++) {
                        colsum += matrix[i][j];
                    }
                    result[i][0] = colsum;
                }
            }
            default -> throw new IllegalArgumentException("Axes other than 0,1 is unsupported");
        }
		return result;
	}

	
	/**
	 * Returns a new matrix which is the transpose of input matrix
	 * @param matrix
	 * @return
	 */
	public static double sumPar(double[][] matrix) {

		int cols = matrix[0].length;
		int rows = matrix.length;
		double sum = 0;


        for (double[] aMatrix : matrix)
            for (int col = 0; col < cols; col++)
                sum += aMatrix[col];








		return sum;
	}



















































	
	/**
	 * @param matrix
	 * @return sum of all values in the matrix
	 */
	public static double sum(double [][] matrix) {
		double sum = 0.0;
		for (double[] r : matrix)
			sum += sum(r);
		return sum;
	}

	public static double sum(double [] vector) {
		double res = 0.0;
		for (double aVector : vector) {
			res += aVector;
		}
		return res;
	}

	/**
	 * Return a new matrix with the max value of either the value in the matrix 
	 * or maxval otherwise 
	 * @param matrix
	 * @param maxval
	 * @return
	 */
	public static double [][] maximum(double [][] matrix, double maxval) {
		int l = matrix[0].length;
		double [][] maxed = new double[matrix.length][l];
		for (int i = 0; i < matrix.length; i++) {
			double[] maxI = maxed[i];
			double[] mmi = matrix[i];
			for (int j = 0; j < l; j++) {
				maxI[j] = Math.max(mmi[j], maxval);
			}
		}
		return maxed;
	}

	
	/**
	 * All values in matrix that is less than <code>lessthan</code> is assigned
	 * the value <code>assign</code>
	 * @param matrix
	 * @param lessthan
	 * @param assign
	 * @return
	 */
	public static void assignAllLessThan(double[][] matrix, double lessthan, double assign) {
		int l = matrix[0].length;
		for (double[] mi : matrix) {
			for (int j = 0; j < l; j++) {
				if (mi[j] < lessthan) {
					mi[j] = assign;
				}
			}
		}
	}

	
	/**
	 * @param matrix
	 * @return a new matrix with the values of matrix squared
	 */
	public static double [][] square(double [][] matrix) {
		return scalarPow(matrix,2);
	}

	/** 
	 * Replaces NaN's with repl
	 * @param matrix
	 * @param repl
	 * @return
	 */
	public static double [][] replaceNaN(double [][] matrix, double repl) {
		int l = matrix[0].length;
		double [][] result = new double[matrix.length][l];
		for (int i = 0; i < matrix.length; i++) {
			double[] mi = matrix[i];
			double[] ri = result[i];
			for (int j = 0; j < l; j++) {
				double mij = mi[j];
				ri[j] = Double.isNaN(mij) ? repl : mij;
			}
		}
		return result;
	}

	/** 
	 * Replaces Infinity's with repl
	 * @param matrix
	 * @param repl
	 * @return
	 */
	public static double [][] replaceInf(double [][] matrix, double repl) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = Double.isInfinite(matrix[i][j]) ? repl : matrix[i][j];
			}
		}
		return result;
	}

	
	private static double [][] scalarPow(double[][] matrix, double power) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] += Math.pow(matrix[i][j],power);
			}
		}
		return result;
	}

	public static double [][] addColumnVector(double [][] matrix, double [][] colvector) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = matrix[i][j] + colvector[i][0];
			}
		}
		return result;
	}

	public static double [][] addRowVector(double [][] matrix, double [][] rowvector) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = matrix[i][j] + rowvector[0][j];
			}
		}
		return result;
	}

	public static double [][] fillWithRowOld(double [][] matrix, int row) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[row], 0, result[i], 0, matrix[0].length);
		}
		return result;
	}

	public static double [][] fillWithRow(double [][] matrix, int row) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		double [][] result = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			System.arraycopy(matrix[row], 0, result[i], 0, cols);
		}
		return result;
	}

	
	public static double [][] tile(double [][] matrix, int rowtimes, int coltimes) {
		double [][] result = new double[matrix.length*rowtimes][matrix[0].length*coltimes];
		for (int i = 0, resultrow = 0; i < rowtimes; i++) {
			for (double[] aMatrix : matrix) {
				for (int k = 0, resultcol = 0; k < coltimes; k++) {
					for (int l = 0; l < matrix[0].length; l++) {
						result[resultrow][resultcol++] = aMatrix[l];
					}
				}
				resultrow++;
			}
		}

		return result;
	}

	public static double[][] normalize(double[][] x, double[] meanX, double[] stdevX) {
		double[][] y = new double[x.length][x[0].length];
		for (int i = 0; i < y.length; i++)
			for (int j = 0; j < y[i].length; j++)
				y[i][j] = (x[i][j] - meanX[j]) / stdevX[j];
		return y;
	}
	
	public static int [] range(int n) {
		int [] result = new int[n];
		for (int i = 0; i < n; i++)
			result[i] = i;
		return result;
	}

	public static int [] range(int a, int b) {
		if( b < a ) {
			throw new IllegalArgumentException("b has to be larger than a");
		}
		int val = a;
		int [] result = new int[b-a];
		for (int i = 0; i < (b-a); i++) {
			result[i] = val++;
		}
		return result;
	}

	
	public static int [] concatenate(int [] v1,int [] v2) {
		int [] result = new int[v1.length+v2.length];
		int index = 0;
		for (int i = 0; i < v1.length; i++, index++) {
			result[index] = v1[index];
		}
		for (int i = 0; i < v2.length; i++, index++) {
			result[index] = v2[i];
		}
		return result;
	}
	
	
		public static double [] concatenate(double [] v1,double [] v2) {
			double [] result = new double[v1.length+v2.length];
			int index = 0;
			for (int i = 0; i < v1.length; i++, index++) {
				result[index] = v1[index];
			}
			for (int i = 0; i < v2.length; i++, index++) {
				result[index] = v2[i];
			}
			return result;
		}

	
	public static double [][] concatenate(double [][] m1,double[][] m2) {
		if(m1.length!=m2.length) throw new IllegalArgumentException("m1 and m2 must have the same number of rows:" + m1.length + " != " + m2.length);
		double [][] result = new double[m1.length][m1[0].length+m2[0].length];
		int resCol = 0;
		for (int i = 0; i < m1.length; i++) {
			resCol = 0;
			for (int j = 0; j < m1[i].length; j++) {
				result[i][resCol++] = m1[i][j];
			}
			for (int j = 0; j < m2[i].length; j++) {
				result[i][resCol++] = m2[i][j];
			}
		}	
		return result;
	}

	
	public static double [][] concatenate(double [][] m1,double[] v2) {
		if(m1.length!=v2.length) throw new IllegalArgumentException("m1 and v2 must have the same number of rows:" + m1.length + " != " + v2.length);
		double [][] result = new double[m1.length][m1[0].length+1];
		int resCol = 0;
		for (int i = 0; i < m1.length; i++) {
			resCol = 0;
			for (int j = 0; j < m1[i].length; j++) {
				result[i][resCol++] = m1[i][j];
			}
			result[i][resCol++] = v2[i];
		}	
		return result;
	}

	public static double [][] scalarMultiply(double [][] m1,double [][] m2) {
		return parScalarMultiply(m1, m2);
	}
	
	
	private static double [][] sMultiply(double[][] v1, double[][] v2) {
		if( v1.length != v2.length || v1[0].length != v2[0].length ) {
			throw new IllegalArgumentException("a and b has to be of equal dimensions");
		}
		double [][] result = new double[v1.length][v1[0].length];
		for (int i = 0; i < v1.length; i++) {
			for (int j = 0; j < v1[0].length; j++) {
				result[i][j] = v1[i][j] * v2[i][j];
			}
		}
		return result;
	}
	
	public static double[][] parScalarMultiply(double[][] m1, double[][] m2) {
		return sMultiply(m1, m2);






	}

	public static double[][] parScalarMinus(double[][] m1, double[][] m2) {
		return sMinus(m1, m2);






	}

























































	public static void assignAtIndex(double[][] num, int[] range, int[] range1, double value) {
		for (int j = 0; j < range.length; j++) {
			num[range[j]][range1[j]] = value;
		}
	}

	public static double [][] getValuesFromRow(double[][] matrix, int row, int[] indicies) {
		double [][] values = new double[1][indicies.length];
		for (int j = 0; j < indicies.length; j++) {
			values[0][j] = matrix[row][indicies[j]];
		}
		return values;
	}

	public static void assignValuesToRow(double[][] matrix, int row, int[] indicies, double [] values) {
		if( indicies.length != values.length ) {
			throw new IllegalArgumentException("Length of indicies and values have to be equal");
		}
		for (int j = 0; j < indicies.length; j++) {
			matrix[row][indicies[j]] = values[j];
		}
	}

	private static double stdev(double[][] matrix) {
		double m = mean(matrix);

        double total = 0;

        int N = matrix.length * matrix[0].length;

		for (double[] aMatrix : matrix) {
			for (double x : aMatrix) {
				total += (x - m) * (x - m);
			}
		}

        return Math.sqrt(total / (N-1));
	}
	
	private static double[] colStddev(double[][] v) {
		double[] var = variance(v);
		for (int i = 0; i < var.length; i++)
			var[i] = Math.sqrt(var[i]);
		return var;
	}

	private static double[] variance(double[][] v) {
		int m = v.length;
		int n = v[0].length;
		double[] var = new double[n];
		int degrees = (m - 1);
		for (int j = 0; j < n; j++) {
            double s = 0;
			for (double[] aV1 : v) s += aV1[j];
			s /= m;
            double c = 0;
            for (double[] aV : v) c += (aV[j] - s) * (aV[j] - s);
			var[j] = c / degrees;
		}
		return var;
	}

	/**
	 * @param matrix
	 * @return a new vector with the column means of matrix
	 */
	private static double[] colMeans(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		double[] mean = new double[cols];
		for (double[] aMatrix : matrix)
			for (int j = 0; j < cols; j++)
				mean[j] += aMatrix[j];
		for (int j = 0; j < cols; j++)
			mean[j] /= rows;
		return mean;
	}

	public static double[][] copyRows(double[][] input, int... indices) {
		double[][] matrix = new double[indices.length][input[0].length];
		for (int i = 0; i < indices.length; i++)
			System.arraycopy(input[indices[i]], 0, matrix[i], 0, input[indices[i]].length);
		return matrix;
	}

	public static double[][] copyCols(double[][] input, int... indices) {
		double[][] matrix = new double[indices.length][input.length];
		for (int i = 0; i < indices.length; i++)
			for (int j = 0; j < input.length; j++) {
				matrix[i][j] = input[j][indices[i]];
			}
		return matrix;
	}

	public static double[][] fillMatrix(int rows, int cols, double fillvalue) {
		double[][] matrix = new double[rows][cols];
		for (double[] doubles : matrix) Arrays.fill(doubles, fillvalue);
		return matrix;
	}

	public static double[][] plus(double[][] m1, double[][] m2) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] + m2[i][j];
		return matrix;
	}

	public static double[][] plusLerp(double[][] a, double[][] b, double ab) {
		if (ab == 0) return a;
		else if (ab == 1) return b;
		double ba = 1-ab;
		double[][] c = new double[a.length][a[0].length];
		for (int i = 0; i < a.length; i++) {
			double[] ci = c[i];
			double[] ai = a[i];
			double[] bi = b[i];
			for (int j = 0; j < a[0].length; j++) {
				ci[j] = (ab* ai[j]) + (ba* bi[j]);
			}
		}
		return c;
	}
	
	public static double[][] scalarPlus(double[][] m1, double m2) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] + m2;
		return matrix;
	}

	
	public static double [] scalarPlus(double[] m1, double m2) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			matrix[i] = m1[i] + m2;
		return matrix;
	}

	public static double[][] minus(double[][] m1, double[][] m2) {
		return parScalarMinus(m1, m2);
	}

	
	public static double[][] sMinus(double[][] m1, double[][] m2) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] - m2[i][j];
		return matrix;
	}

	
	public static double[][] scalarDivide(double[][] numerator, double denom) {
		double[][] matrix = new double[numerator.length][numerator[0].length];
		for (int i = 0; i < numerator.length; i++)
			for (int j = 0; j < numerator[i].length; j++)
				matrix[i][j] = numerator[i][j] / denom;
		return matrix;
	}

	
	public static double [] scalarDivide(double numerator, double[] denom) {
		double[] vector = new double[denom.length];
		for (int i = 0; i < denom.length; i++)
			vector[i] = numerator / denom[i] ;
		return vector;
	}

	
	public static double [] scalarDivide(double[] numerator, double denom) {
		double[] vector = new double[numerator.length];
		for (int i = 0; i < numerator.length; i++)
			vector[i] = numerator[i] / denom;
		return vector;
	}

	
	public static double [] scalarDivide(double [] numerator, double[] denom) {
		double[] vector = new double[denom.length];
		for (int i = 0; i < denom.length; i++)
			vector[i] = numerator[i] / denom[i] ;
		return vector;
	}

	
	public static double[][] scalarDivide(double[][] numerator, double[][] denom) {
		double[][] matrix = new double[numerator.length][numerator[0].length];
		for (int i = 0; i < numerator.length; i++)
			for (int j = 0; j < numerator[i].length; j++)
				matrix[i][j] = numerator[i][j] / denom[i][j];
		return matrix;
	}
	
	
	public static double[][] scalarMult(double[][] m1, double mul) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[i].length; j++)
				matrix[i][j] = m1[i][j] * mul;
		return matrix;
	}
	
	
	public static double[][] times(double[][] a, double[][] b){
		if(a.length == 0) return ArrayUtil.EMPTY_DOUBLE_DOUBLE;
		if(a[0].length != b.length) return null; 

		int n = a[0].length;
		int m = a.length;
		int p = b[0].length;
        double[][] ans = new double[m][p];

		for(int i = 0;i < m;i++){
			for(int j = 0;j < p;j++){
				for(int k = 0;k < n;k++){
					ans[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return ans;
	}

	public static double [] scalarMultiply(double[] m1, double mul) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			matrix[i] = m1[i] * mul;
		return matrix;
	}

	public static double [] scalarMultiply(double[] m1, double [] m2) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			matrix[i] = m1[i] * m2[i];
		return matrix;
	}

	public static double[][] diag(double[][] ds) {
		boolean isLong = ds.length > ds[0].length;
		int dim = Math.max(ds.length,ds[0].length);
		double [][] result = new double [dim][dim];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result.length; j++) {
				if(i==j) {
					result[i][j] = isLong ? ds[i][0] : ds[0][i];
				}
			}
		}

		return result;
	}

	public static double [][] dot(double [][] a, double [][] b) {
		if(a[0].length!=b.length) throw new IllegalArgumentException("Dims does not match: " + a[0].length  + "!=" + b.length);
		double [][] res = new double[a.length][b[0].length];
		for (int row = 0; row < a.length; row++) {
			for (int col = 0; col < b[row].length; col++) {
				for (int i = 0; i < a[0].length; i++) {
					res[row][col] = a[row][i] * b[i][col];
				}
			}
		}
		return res;
	}

	public static double dot(double [] a, double [] b) {
		if(a.length!=b.length) {
			throw new IllegalArgumentException("Vectors are not of equal length");
		}
		double res = 0.0;
		for (int i = 0; i < b.length; i++) {
			res += a[i] * b[i];
		}
		return res;
	}
	
	public static double dot2P1(double [] a1, double [] a2, double [] b) {
		if((a1.length+a2.length)!=b.length) {
			throw new IllegalArgumentException("Vectors are not of equal length");
		}
		double res = 0.0;
		int bidx = 0;
		for (int i = 0; i < a1.length; i++, bidx++) {
			res += a1[i] * b[bidx];
		}
		for (int i = 0; i < a2.length; i++, bidx++) {
			res += a2[i] * b[bidx];
		}
		return res;
	}

	public static int maxIdx(double[] probs) {
		int maxIdx = 0;
		double max = probs[maxIdx];
		for (int i = 0; i < probs.length; i++) {
			if(probs[i]>max) {
				max = probs[i];
				maxIdx = i;
			}
		}
		return maxIdx;
	}

//	public static double[][] extractCol(int col, double[][] matrix) {
//		double [][] res = new double[matrix.length][1];
//		for (int row = 0; row < matrix.length; row++) {
//			res[row][0] = matrix[row][col];
//		}
//		return res;
//	}
//
//	public static double[] extractColVector(int col, double[][] matrix) {
//		double [] res = new double[matrix.length];
//		for (int row = 0; row < matrix.length; row++) {
//			res[row] = matrix[row][col];
//		}
//		return res;
//	}
//
//	public static double [][] extractDoubleArray(DMatrix p) {
//		int rows = p.getNumRows();
//		int cols = p.getNumCols();
//		double [][] result = new double[rows][cols];
//		for (int i = 0; i < rows; i++) {
//			for (int j = 0; j < cols; j++) {
//				result[i][j] = p.get(i, j);
//			}
//		}
//		return result;
//	}
//
//	public static double [] extractDoubleVector(DMatrixRBlock p) {
//		int rows = p.getNumRows();
//		int cols = p.getNumCols();
//
//		if(rows != 1 && cols != 1) {
//			throw new IllegalArgumentException("Cannot convert a " + rows + 'x' + cols + " matrix to a vector");
//		}
//
//		double [] result;
//		if(cols == 1) {
//			result = new double[rows];
//			for (int j = 0; j < rows; j++) {
//				result[j] = p.get(j,0);
//			}
//		} else {
//			result = new double[cols];
//			for (int j = 0; j < cols; j++) {
//				result[j] = p.get(0,j);
//			}
//		}
//		return result;
//	}
//
//	public static double[][] extractRowCols(int col, double[][] zs2, int[] cJIdxs) {
//		double [][] res = new double[cJIdxs.length][1];
//		for (int row = 0; row < cJIdxs.length; row++) {
//			res[row][0] = zs2[cJIdxs[row]][col];
//		}
//		return res;
//	}
//










	public static double[][] makeDesignMatrix(double[][] xstmp) {
		double [][] xs = new double[xstmp.length][xstmp[0].length+1];
		for (int row = 0; row < xs.length; row++) {
			for (int col = 0; col < xs[0].length; col++) {
				if(col==0) {
					xs[row][0] = 1.0;
				} else {
					xs[row][col] = xstmp[row][col-1];
				}
			}
		}
		return xs;
	}

	public static double[][] addIntercept(double[][] xs) {
		double [][] result = new double [xs.length][xs[0].length+1];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				if(j==0) {
					result[i][0] = 1.0;
				} else {
					result[i][j] = xs[i][j-1];
				}
			}
		}
		return result;
	}

	public static double[] toPrimitive(Double[] ds) {
		double [] result = new double[ds.length];
		for (int i = 0; i < ds.length; i++) {
			result[i] = ds[i];
		}
		return result;
	}
	
	public static double [] extractRowFromFlatMatrix(double[] flatMatrix, int rowIdx, int dimension) {
		double [] point = new double[dimension];
		int offset = rowIdx * dimension;
        System.arraycopy(flatMatrix, offset, point, 0, dimension);
		return point;
	}


}