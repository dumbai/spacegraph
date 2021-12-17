///*
// *
// * This Java port of Barnes Hut t-SNE is Copyright (c) Leif Jonsson 2016 and
// * Copyright (c) 2014, Laurens van der Maaten (Delft University of Technology)
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// * 1. Redistributions of source code must retain the above copyright
// *    notice, this list of conditions and the following disclaimer.
// * 2. Redistributions in binary form must reproduce the above copyright
// *    notice, this list of conditions and the following disclaimer in the
// *    documentation and/or other materials provided with the distribution.
// * 3. All advertising materials mentioning features or use of this software
// *    must display the following acknowledgement:
// *    This product includes software developed by the Delft University of Technology.
// * 4. Neither the name of the Delft University of Technology nor the names of
// *    its contributors may be used to endorse or promote products derived from
// *    this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS
// * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
// * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
// * EVENT SHALL LAURENS VAN DER MAATEN BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
// * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
// * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
// * OF SUCH DAMAGE.
// *
// */
//package com.jujutsu.tsne;
//
//import com.jujutsu.tsne.barneshut.*;
//import com.jujutsu.tsne.matrix.MatrixOps;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.ThreadLocalRandom;
//
//import static java.lang.Math.exp;
//import static java.lang.Math.log;
//
//public class BHTSne implements BarnesHutTSne {
//
//	final Distance distance = new EuclideanDistance();
//	private volatile boolean abort = false;
//
//	@Override
//	public double[][] tsne(TSneConfiguration config) {
//		return run(config);
//	}
//
//	private static double[] flatten(double[][] x) {
//		int noCols = x[0].length;
//		double [] flat = new double[x.length*x[0].length];
//		for (int i = 0; i < x.length; i++) {
//            System.arraycopy(x[i], 0, flat, i * noCols, x[i].length);
//		}
//		return flat;
//	}
//
//	private static double [][] expand(double[] x, int N, int D) {
//		double [][] expanded = new double[N][D];
//		for (int row = 0; row < N; row++) {
//            System.arraycopy(x, row * D, expanded[row], 0, D);
//		}
//		return expanded;
//	}
//
//	static double sign_tsne(double x) { return (x == .0 ? .0 : (x < .0 ? -1.0 : 1.0)); }
//
//
//	double [][] run(TSneConfiguration parameterObject) {
//		int D = parameterObject.getXStartDim();
//		double[][] Xin = parameterObject.getXin();
//		boolean exact = (parameterObject.getTheta() == .0);
//
//		if(exact) throw new IllegalArgumentException("The Barnes Hut implementation does not support exact inference yet (theta==0.0), if you want exact t-SNE please use one of the standard t-SNE implementations (FastTSne for instance)");
//
//		if(parameterObject.usePca() && D > parameterObject.getInitialDims() && parameterObject.getInitialDims() > 0) {
//			PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis();
//			Xin = pca.pca(Xin, parameterObject.getInitialDims());
//			D = parameterObject.getInitialDims();
//			System.out.println("X:Shape after PCA is = " + Xin.length + " x " + Xin[0].length);
//		}
//
//		double [] X = flatten(Xin);
//		int N = parameterObject.getNrRows();
//		int no_dims = parameterObject.getOutputDims();
//
//		double [] Y = new double[N*no_dims];
//		System.out.println("X:Shape is = " + N + " x " + D);
//
//		double perplexity = parameterObject.getPerplexity();
//		if(N - 1 < 3 * perplexity) { throw new IllegalArgumentException("Perplexity too large for the number of data points!\n"); }
//		System.out.printf("Using no_dims = %d, perplexity = %f, and theta = %f\n", no_dims, perplexity, parameterObject.getTheta());
//
//
//		double total_time = 0;
//		int stop_lying_iter = 250, mom_switch_iter = 250;
//		double momentum = .5, final_momentum = .8;
//		double eta = 200.0;
//
//
//		double [] dY    = new double[N * no_dims];
//		double [] uY    = new double[N * no_dims];
//		double [] gains = new double[N * no_dims];
//		for(int i = 0; i < N * no_dims; i++) gains[i] = 1.0;
//
//
//		System.out.println("Computing input similarities...");
//		long start = System.currentTimeMillis();
//
//		double max_X = .0;
//		for(int i = 0; i < N * D; i++) {
//			if(X[i] > max_X) max_X = X[i];
//		}
//
//		for(int i = 0; i < N * D; i++) X[i] /= max_X;
//
//		double [] P = null;
//		int K  = (int) Math.round(3 * perplexity);
//		int [] row_P = new int[N+1];
//		int [] col_P = new int[N*K];
//		double [] val_P = new double[N*K];
//		/**_row_P = (int*)    malloc((N + 1) * sizeof(int));
//		 *_col_P = (int*)    calloc(N * K, sizeof(int));
//		 *_val_P = (double*) calloc(N * K, sizeof(double));*/
//
//		if(exact) {
//
//
//			P = new double[N * N];
//			computeGaussianPerplexity(X, N, D, P, perplexity);
//
//
//			System.out.println("Symmetrizing...");
//			int nN = 0;
//			for(int n = 0; n < N; n++) {
//				int mN = 0;
//				for(int m = n + 1; m < N; m++) {
//					P[nN + m] += P[mN + n];
//					P[mN + n]  = P[nN + m];
//					mN += N;
//				}
//				nN += N;
//			}
//			double sum_P = .0;
//			for(int i = 0; i < N * N; i++) sum_P += P[i];
//			for(int i = 0; i < N * N; i++) P[i] /= sum_P;
//		}
//
//
//		else {
//
//
//			computeGaussianPerplexity(X, N, D, row_P, col_P, val_P, perplexity, K);
//
//
//
//
//			SymResult res = symmetrizeMatrix(row_P, col_P, val_P, N);
//			row_P = res.sym_row_P;
//			col_P = res.sym_col_P;
//			val_P = res.sym_val_P;
//
//			double sum_P = .0;
//			for(int i = 0; i < row_P[N]; i++) sum_P += val_P[i];
//			for(int i = 0; i < row_P[N]; i++) val_P[i] /= sum_P;
//		}
//		long end = System.currentTimeMillis();
//
//
//		if(exact) { for(int i = 0; i < N * N; i++)        P[i] *= 12.0; }
//		else {      for(int i = 0; i < row_P[N]; i++) val_P[i] *= 12.0; }
//
//
//		for(int i = 0; i < N * no_dims; i++) Y[i] = ThreadLocalRandom.current().nextDouble() * 0.0001;
//
//
//		if(exact) System.out.printf("Done in %4.2f seconds!\nLearning embedding...\n", (end - start) / 1000.0);
//		else System.out.printf("Done in %4.2f seconds (sparsity = %f)!\nLearning embedding...\n", (end - start) / 1000.0, row_P[N] / ((double) N * N));
//		start = System.currentTimeMillis();
//		for(int iter = 0; iter < parameterObject.getMaxIter() && !abort; iter++) {
//
//			if(exact) computeExactGradient(P, Y, N, no_dims, dY);
//
//			else computeGradient(P, row_P, col_P, val_P, Y, N, no_dims, dY, parameterObject.getTheta());
//
//			updateGradient(N, no_dims, Y, momentum, eta, dY, uY, gains);
//
//
//			zeroMean(Y, N, no_dims);
//
//
//			if(iter == stop_lying_iter) {
//				if(exact) { for(int i = 0; i < N * N; i++)        P[i] /= 12.0; }
//				else      { for(int i = 0; i < row_P[N]; i++) val_P[i] /= 12.0; }
//			}
//			if(iter == mom_switch_iter) momentum = final_momentum;
//
//
//			if(((iter > 0 && iter % 50 == 0) || iter == parameterObject.getMaxIter() - 1) && !parameterObject.silent() ) {
//				end = System.currentTimeMillis();
//				String err_string = "not_calculated";
//				if(parameterObject.printError()) {
//					double C = .0;
//					if(exact) C = evaluateError(P, Y, N, no_dims);
//					else      C = evaluateError(row_P, col_P, val_P, Y, N, no_dims, parameterObject.getTheta());
//					err_string = String.valueOf(C);
//				}
//				if(iter == 0)
//					System.out.printf("Iteration %d: error is %s\n", iter + 1, err_string);
//				else {
//					total_time += (end - start) / 1000.0;
//					System.out.printf("Iteration %d: error is %s (50 iterations in %4.2f seconds)\n", iter, err_string, (end - start) / 1000.0);
//				}
//				start = System.currentTimeMillis();
//			}
//		}
//		end = System.currentTimeMillis(); total_time += (end - start) / 1000.0;
//
//		System.out.printf("Fitting performed in %4.2f seconds.\n", total_time);
//		return expand(Y,N,no_dims);
//	}
//
//	static void updateGradient(int N, int no_dims, double[] Y, double momentum, double eta, double[] dY, double[] uY,
//							   double[] gains) {
//		for(int i = 0; i < N * no_dims; i++)  {
//
//			gains[i] = (sign_tsne(dY[i]) != sign_tsne(uY[i])) ? (gains[i] + .2) : (gains[i] * .8);
//			if(gains[i] < .01) gains[i] = .01;
//
//
//			Y[i] = Y[i] + uY[i];
//			uY[i] = momentum * uY[i] - eta * gains[i] * dY[i];
//		}
//	}
//
//
//	static void computeGradient(double[] P, int[] inp_row_P, int[] inp_col_P, double[] inp_val_P, double[] Y, int N, int D, double[] dC, double theta)
//	{
//
//		SPTree tree = new SPTree(D, Y, N);
//
//
//		double [] sum_Q = new double[1];
//		double [] pos_f = new double[N * D];
//		double [][] neg_f = new double[N][D];
//
//		tree.computeEdgeForces(inp_row_P, inp_col_P, inp_val_P, N, pos_f);
//		for(int n = 0; n < N; n++)
//			tree.computeNonEdgeForces(n, theta, neg_f[n], sum_Q);
//
//
//		double sq0 = sum_Q[0];
//		for(int n = 0; n < N; n++) {
//			for(int d = 0; d < D; d++) {
//				dC[n*D+d] = pos_f[n*D+d] - (neg_f[n][d] / sq0);
//			}
//		}
//	}
//
//
//	private static void computeExactGradient(double[] P, double[] Y, int N, int D, double[] dC) {
//
//
//		Arrays.fill(dC, 0, N*D, 0.0);
//
//
//		double [] DD = new double[N * N];
//		computeSquaredEuclideanDistance(Y, N, D, DD);
//
//
//		double [] Q    = new double[N * N];
//		double sum_Q = .0;
//		int nN = 0;
//		for(int n = 0; n < N; n++) {
//			for(int m = 0; m < N; m++) {
//				if(n != m) {
//					sum_Q += (Q[nN + m] = 1 / (1 + DD[nN + m]));
//				}
//			}
//			nN += N;
//		}
//
//
//		nN = 0;
//		int nD = 0;
//		for(int n = 0; n < N; n++) {
//			int mD = 0;
//			for(int m = 0; m < N; m++) {
//				if(n != m) {
//					double mult = (P[nN + m] - (Q[nN + m] / sum_Q)) * Q[nN + m];
//					for(int d = 0; d < D; d++) {
//						dC[nD + d] += (Y[nD + d] - Y[mD + d]) * mult;
//					}
//				}
//				mD += D;
//			}
//			nN += N;
//			nD += D;
//		}
//	}
//
//
//	private static double evaluateError(double[] P, double[] Y, int N, int D) {
//
//
//		double [] DD = new double[N * N];
//		double [] Q = new double[N * N];
//		computeSquaredEuclideanDistance(Y, N, D, DD);
//
//
//		int nN = 0;
//		double sum_Q = Double.MIN_VALUE;
//		for(int n = 0; n < N; n++) {
//			for(int m = 0; m < N; m++) {
//				if(n != m) {
//					Q[nN + m] = 1 / (1 + DD[nN + m]);
//					sum_Q += Q[nN + m];
//				}
//				else Q[nN + m] = Double.MIN_VALUE;
//			}
//			nN += N;
//		}
//		for(int i = 0; i < N * N; i++) Q[i] /= sum_Q;
//
//
//		double C = .0;
//		for(int n = 0; n < N * N; n++) {
//			C += P[n] * log((P[n] + Double.MIN_VALUE) / (Q[n] + Double.MIN_VALUE));
//		}
//
//		return C;
//	}
//
//
//	private static double evaluateError(int[] row_P, int[] col_P, double[] val_P, double[] Y, int N, int D, double theta)
//	{
//
//		SPTree tree = new SPTree(D, Y, N);
//		double [] buff = new double[D];
//		double [] sum_Q = new double[1];
//		for(int n = 0; n < N; n++) tree.computeNonEdgeForces(n, theta, buff, sum_Q);
//
//
//		int ind1, ind2;
//		double C = .0, Q;
//		for(int n = 0; n < N; n++) {
//			ind1 = n * D;
//			for(int i = row_P[n]; i < row_P[n + 1]; i++) {
//				Q = .0;
//				ind2 = col_P[i] * D;
//                System.arraycopy(Y, ind1, buff, 0, D);
//				for(int d = 0; d < D; d++) buff[d] -= Y[ind2 + d];
//				for(int d = 0; d < D; d++) Q += buff[d] * buff[d];
//				Q = (1.0 / (1.0 + Q)) / sum_Q[0];
//				C += val_P[i] * log((val_P[i] + Double.MIN_VALUE) / (Q + Double.MIN_VALUE));
//			}
//		}
//
//		return C;
//	}
//
//
//	private static void computeGaussianPerplexity(double[] X, int N, int D, double[] P, double perplexity) {
//
//
//		double [] DD = new double[N * N];
//		computeSquaredEuclideanDistance(X, N, D, DD);
//
//
//		int nN = 0;
//		for(int n = 0; n < N; n++) {
//
//
//			boolean found = false;
//			double beta = 1.0;
//			double min_beta = -Double.MAX_VALUE;
//			double max_beta =  Double.MAX_VALUE;
//			double tol = 1e-5;
//			double sum_P = Double.MIN_VALUE;
//
//
//			int iter = 0;
//			while(!found && iter < 200) {
//
//
//				for(int m = 0; m < N; m++) P[nN + m] = exp(-beta * DD[nN + m]);
//				P[nN + n] = Double.MIN_VALUE;
//
//
//				sum_P = Double.MIN_VALUE;
//				for(int m = 0; m < N; m++) sum_P += P[nN + m];
//				double H = 0.0;
//				for(int m = 0; m < N; m++) H += beta * (DD[nN + m] * P[nN + m]);
//				H = (H / sum_P) + log(sum_P);
//
//
//				double Hdiff = H - log(perplexity);
//				if(Hdiff < tol && -Hdiff < tol) {
//					found = true;
//				}
//				else {
//					if(Hdiff > 0) {
//						min_beta = beta;
//						if(max_beta == Double.MAX_VALUE || max_beta == -Double.MAX_VALUE)
//							beta *= 2.0;
//						else
//							beta = (beta + max_beta) / 2.0;
//					}
//					else {
//						max_beta = beta;
//						if(min_beta == -Double.MAX_VALUE || min_beta == Double.MAX_VALUE)
//							beta /= 2.0;
//						else
//							beta = (beta + min_beta) / 2.0;
//					}
//				}
//
//
//				iter++;
//			}
//
//
//			for(int m = 0; m < N; m++) P[nN + m] /= sum_P;
//			nN += N;
//		}
//	}
//
//
//	void computeGaussianPerplexity(double [] X, int N, int D, int [] _row_P, int [] _col_P, double [] _val_P, double perplexity, int K) {
//		if(perplexity > K) System.out.println("Perplexity should be lower than K!");
//
//
//		/**_row_P = (int*)    malloc((N + 1) * sizeof(int));
//		 *_col_P = (int*)    calloc(N * K, sizeof(int));
//		 *_val_P = (double*) calloc(N * K, sizeof(double));
//		    if(*_row_P == null || *_col_P == null || *_val_P == null) { Rcpp::stop("Memory allocation failed!\n"); }*/
//		double [] cur_P = new double[N - 1];
//
//		_row_P[0] = 0;
//		for(int n = 0; n < N; n++) _row_P[n + 1] = _row_P[n] + K;
//
//
//		VpTree<DataPoint> tree = new VpTree<>(distance);
//		final DataPoint [] obj_X = new DataPoint [N];
//		for(int n = 0; n < N; n++) {
//			double [] row = MatrixOps.extractRowFromFlatMatrix(X,n,D);
//			obj_X[n] = new DataPoint(D, n, row);
//		}
//		tree.create(obj_X);
//
//
//
//
//
//
//
//
//
//
//
//
//
//		System.out.println("Building tree...");
//		List<DataPoint> indices = new ArrayList<>();
//		List<Double> distances = new ArrayList<>();
//		for(int n = 0; n < N; n++) {
//
//			if(n % 10000 == 0) System.out.printf(" - point %d of %d\n", n, N);
//
//
//			indices.clear();
//			distances.clear();
//
//			tree.search(obj_X[n], K + 1, indices, distances);
//
//
//			boolean found = false;
//			double beta = 1.0;
//			double min_beta = -Double.MAX_VALUE;
//			double max_beta =  Double.MAX_VALUE;
//			double tol = 1e-5;
//
//
//			int iter = 0;
//			double sum_P = 0.;
//			while(!found && iter < 200) {
//
//
//				sum_P = Double.MIN_VALUE;
//				double H = .0;
//				for(int m = 0; m < K; m++) {
//					cur_P[m] = exp(-beta * distances.get(m + 1));
//					sum_P += cur_P[m];
//					H += beta * (distances.get(m + 1) * cur_P[m]);
//				}
//				H = (H / sum_P) + log(sum_P);
//
//
//				double Hdiff = H - log(perplexity);
//				if(Hdiff < tol && -Hdiff < tol) {
//					found = true;
//				}
//				else {
//					if(Hdiff > 0) {
//						min_beta = beta;
//						if(max_beta == Double.MAX_VALUE || max_beta == -Double.MAX_VALUE)
//							beta *= 2.0;
//						else
//							beta = (beta + max_beta) / 2.0;
//					}
//					else {
//						max_beta = beta;
//						if(min_beta == -Double.MAX_VALUE || min_beta == Double.MAX_VALUE)
//							beta /= 2.0;
//						else
//							beta = (beta + min_beta) / 2.0;
//					}
//				}
//
//
//				iter++;
//			}
//
//
//			for(int m = 0; m < K; m++) {
//				cur_P[m] /= sum_P;
//				_col_P[_row_P[n] + m] = indices.get(m + 1).index();
//				_val_P[_row_P[n] + m] = cur_P[m];
//			}
//		}
//	}
//
//	static void computeGaussianPerplexity(double[] X, int N, int D, int[] _row_P, int[] _col_P, double[] _val_P, double perplexity, double threshold) {
//
//		double [] buff  = new double[D];
//		double [] DD    = new double[N];
//		double [] cur_P = new double[N];
//
//
//		int total_count = 0;
//		for(int n = 0; n < N; n++) {
//
//
//			for(int m = 0; m < N; m++) {
//                System.arraycopy(X, n * D, buff, 0, D);
//				for(int d = 0; d < D; d++) buff[d] -= X[m * D + d];
//				DD[m] = .0;
//				for(int d = 0; d < D; d++) DD[m] += buff[d] * buff[d];
//			}
//
//
//			boolean found = false;
//			double beta = 1.0;
//			double min_beta = -Double.MAX_VALUE;
//			double max_beta =  Double.MAX_VALUE;
//			double tol = 1e-5;
//
//
//			int iter = 0;
//			double sum_P = 0.;
//			while(!found && iter < 200) {
//
//
//				for(int m = 0; m < N; m++) cur_P[m] = exp(-beta * DD[m]);
//				cur_P[n] = Double.MIN_VALUE;
//
//
//				sum_P = Double.MIN_VALUE;
//				for(int m = 0; m < N; m++) sum_P += cur_P[m];
//				double H = 0.0;
//				for(int m = 0; m < N; m++) H += beta * (DD[m] * cur_P[m]);
//				H = (H / sum_P) + log(sum_P);
//
//
//				double Hdiff = H - log(perplexity);
//				if(Hdiff < tol && -Hdiff < tol) {
//					found = true;
//				}
//				else {
//					if(Hdiff > 0) {
//						min_beta = beta;
//						if(max_beta == Double.MAX_VALUE || max_beta == -Double.MAX_VALUE)
//							beta *= 2.0;
//						else
//							beta = (beta + max_beta) / 2.0;
//					}
//					else {
//						max_beta = beta;
//						if(min_beta == -Double.MAX_VALUE || min_beta == Double.MAX_VALUE)
//							beta /= 2.0;
//						else
//							beta = (beta + min_beta) / 2.0;
//					}
//				}
//
//
//				iter++;
//			}
//
//
//			for(int m = 0; m < N; m++) cur_P[m] /= sum_P;
//			for(int m = 0; m < N; m++) {
//				if(cur_P[m] > threshold / N) total_count++;
//			}
//		}
//
//
//		_row_P = new int[N + 1];
//		_col_P = new int[total_count];
//		_val_P = new double[total_count];
//		int [] row_P = _row_P;
//		int [] col_P = _col_P;
//		double [] val_P = _val_P;
//		row_P[0] = 0;
//
//
//		int count = 0;
//		for(int n = 0; n < N; n++) {
//
//
//			for(int m = 0; m < N; m++) {
//                System.arraycopy(X, n * D, buff, 0, D);
//				for(int d = 0; d < D; d++) buff[d] -= X[m * D + d];
//				DD[m] = .0;
//				for(int d = 0; d < D; d++) DD[m] += buff[d] * buff[d];
//			}
//
//
//			boolean found = false;
//			double beta = 1.0;
//			double min_beta = -Double.MAX_VALUE;
//			double max_beta =  Double.MAX_VALUE;
//			double tol = 1e-5;
//
//
//			int iter = 0;
//			double sum_P = 0.;
//			while(!found && iter < 200) {
//
//
//				for(int m = 0; m < N; m++) cur_P[m] = exp(-beta * DD[m]);
//				cur_P[n] = Double.MIN_VALUE;
//
//
//				sum_P = Double.MIN_VALUE;
//				for(int m = 0; m < N; m++) sum_P += cur_P[m];
//				double H = 0.0;
//				for(int m = 0; m < N; m++) H += beta * (DD[m] * cur_P[m]);
//				H = (H / sum_P) + log(sum_P);
//
//
//				double Hdiff = H - log(perplexity);
//				if(Hdiff < tol && -Hdiff < tol) {
//					found = true;
//				}
//				else {
//					if(Hdiff > 0) {
//						min_beta = beta;
//						if(max_beta == Double.MAX_VALUE || max_beta == -Double.MAX_VALUE)
//							beta *= 2.0;
//						else
//							beta = (beta + max_beta) / 2.0;
//					}
//					else {
//						max_beta = beta;
//						if(min_beta == -Double.MAX_VALUE || min_beta == Double.MAX_VALUE)
//							beta /= 2.0;
//						else
//							beta = (beta + min_beta) / 2.0;
//					}
//				}
//
//
//				iter++;
//			}
//
//
//			for(int m = 0; m < N; m++) cur_P[m] /= sum_P;
//			for(int m = 0; m < N; m++) {
//				if(cur_P[m] > threshold / N) {
//					col_P[count] = m;
//					val_P[count] = cur_P[m];
//					count++;
//				}
//			}
//			row_P[n + 1] = count;
//		}
//
//	}
//
//	static class SymResult {
//		final int []    sym_row_P;
//		final int []    sym_col_P;
//		final double [] sym_val_P;
//
//		SymResult(int[] sym_row_P, int[] sym_col_P, double[] sym_val_P) {
//			super();
//			this.sym_row_P = sym_row_P;
//			this.sym_col_P = sym_col_P;
//			this.sym_val_P = sym_val_P;
//		}
//	}
//
//	private static SymResult symmetrizeMatrix(int[] _row_P, int[] _col_P, double[] _val_P, int N) {
//
//
//
//
//		int [] row_counts = new int[N];
//		for(int n = 0; n < N; n++) {
//			for(int i = _row_P[n]; i < _row_P[n + 1]; i++) {
//
//
//				boolean present = false;
//				for(int m = _row_P[_col_P[i]]; m < _row_P[_col_P[i] + 1]; m++) {
//					if(_col_P[m] == n) present = true;
//				}
//				if(present) row_counts[n]++;
//				else {
//					row_counts[n]++;
//					row_counts[_col_P[i]]++;
//				}
//			}
//		}
//		int no_elem = 0;
//		for(int n = 0; n < N; n++) no_elem += row_counts[n];
//
//
//		int []    sym_row_P = new int[N + 1];
//		int []    sym_col_P = new int[no_elem];
//		double [] sym_val_P = new double[no_elem];
//
//
//		sym_row_P[0] = 0;
//		for(int n = 0; n < N; n++) sym_row_P[n + 1] = sym_row_P[n] + row_counts[n];
//
//
//		int [] offset = new int[N];
//		for(int n = 0; n < N; n++) {
//			for(int i = _row_P[n]; i < _row_P[n + 1]; i++) {
//
//
//				boolean present = false;
//				for(int m = _row_P[_col_P[i]]; m < _row_P[_col_P[i] + 1]; m++) {
//					if(_col_P[m] == n) {
//						present = true;
//						if(n <= _col_P[i]) {
//							sym_col_P[sym_row_P[n]        + offset[n]]        = _col_P[i];
//							sym_col_P[sym_row_P[_col_P[i]] + offset[_col_P[i]]] = n;
//							sym_val_P[sym_row_P[n]        + offset[n]]        = _val_P[i] + _val_P[m];
//							sym_val_P[sym_row_P[_col_P[i]] + offset[_col_P[i]]] = _val_P[i] + _val_P[m];
//						}
//					}
//				}
//
//
//				if(!present) {
//					sym_col_P[sym_row_P[n]        + offset[n]]        = _col_P[i];
//					sym_col_P[sym_row_P[_col_P[i]] + offset[_col_P[i]]] = n;
//					sym_val_P[sym_row_P[n]        + offset[n]]        = _val_P[i];
//					sym_val_P[sym_row_P[_col_P[i]] + offset[_col_P[i]]] = _val_P[i];
//				}
//
//
//				if(!present || (present && n <= _col_P[i])) {
//					offset[n]++;
//					if(_col_P[i] != n) offset[_col_P[i]]++;
//				}
//			}
//		}
//
//
//		for(int i = 0; i < no_elem; i++) sym_val_P[i] /= 2.0;
//
//		return new SymResult(sym_row_P, sym_col_P, sym_val_P);
//	}
//
//
//	private static void computeSquaredEuclideanDistance(double[] X, int N, int D, double[] DD) {
//		double [] dataSums = new double[N];
//		for(int n = 0; n < N; n++) {
//			for(int d = 0; d < D; d++) {
//				dataSums[n] += (X[n * D + d] * X[n * D + d]);
//			}
//		}
//		for(int n = 0; n < N; n++) {
//			for(int m = 0; m < N; m++) {
//				DD[n * N + m] = dataSums[n] + dataSums[m];
//			}
//		}
//
//
//
//
//
//		/* DGEMM - perform one of the matrix-matrix operations    */
//		/* C := alpha*op( A )*op( B ) + beta*C */
//		/* BLAS_extern void
//		 * R BLAS declaration:
//		    F77_NAME(dgemm)(const char *transa, const char *transb, const int *m,
//		    		const int *n, const int *k, const double *alpha,
//		    		const double *a, const int *lda,
//		    		const double *b, const int *ldb,
//		    		const double *beta, double *c, const int *ldc);*/
//
//
//	}
//
//
//
//	private static void zeroMean(double[] X, int N, int D) {
//
//
//		double [] mean = new double[D];
//		for(int n = 0; n < N; n++) {
//			for(int d = 0; d < D; d++) {
//				mean[d] += X[n * D + d];
//			}
//		}
//		for(int d = 0; d < D; d++) {
//			mean[d] /= N;
//		}
//
//
//		for(int n = 0; n < N; n++) {
//			for(int d = 0; d < D; d++) {
//				X[n * D + d] -= mean[d];
//			}
//		}
//	}
//
//
//	static void zeroMean(double[][] X, int N, int D) {
//
//
//		double [] mean = new double[D];
//		for(int n = 0; n < N; n++) {
//			for(int d = 0; d < D; d++) {
//				mean[d] += X[n][d];
//			}
//		}
//		for(int d = 0; d < D; d++) {
//			mean[d] /= N;
//		}
//
//
//		for(int n = 0; n < N; n++) {
//			for(int d = 0; d < D; d++) {
//				X[n][d] -= mean[d];
//			}
//		}
//	}
//
//	@Override
//	public void stop() {
//		abort = true;
//	}
//
//}