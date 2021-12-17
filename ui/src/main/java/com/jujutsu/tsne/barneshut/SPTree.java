package com.jujutsu.tsne.barneshut;

import com.jujutsu.tsne.matrix.MatrixOps;

import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class SPTree {

	  
    private static final int QT_NODE_CAPACITY = 1;
        
	private SPTree parent;
	int dimension;
	boolean is_leaf;
	int size;
	int cum_size;
	
	 
    Cell boundary;
    
    
    double[] data;
    double[] center_of_mass;
    final int [] index = new int[QT_NODE_CAPACITY];
    
    
    SPTree [] children;
    int no_children;

	public SPTree(int D, double[] inp_data, int N) {

        double []  min_Y = new double [D];
		double []  max_Y = new double [D]; 
		for(int d = 0; d < D; d++)  {
			min_Y[d] = Double.POSITIVE_INFINITY;
			max_Y[d] = Double.NEGATIVE_INFINITY;
		}
        double[] mean_Y = new double[D];
        int nD = 0;
        for(int n = 0; n < N; n++) {
			for( int d = 0; d < D; d++) {
				mean_Y[d] += inp_data[n * D + d];
				if(inp_data[nD + d] < min_Y[d]) min_Y[d] = inp_data[nD + d];
				if(inp_data[nD + d] > max_Y[d]) max_Y[d] = inp_data[nD + d];
			}
			nD += D;
		}
		for(int d = 0; d < D; d++) mean_Y[d] /= N;

		// Construct SPTree
		double [] width = new double [D];
		for(int d = 0; d < D; d++) width[d] = max(max_Y[d] - mean_Y[d], mean_Y[d] - min_Y[d]) + 1.0e-5;
		init(null, D, inp_data, mean_Y, width);
		fill(N);
	}

	
	private void init(SPTree inp_parent, int D, double[] inp_data, double[] inp_corner, double[] inp_width)
	{
		parent = inp_parent;
		dimension = D;
		no_children = 2;
		for(int d = 1; d < D; d++) no_children *= 2;
		data = inp_data;
		is_leaf = true;
		size = 0;
		cum_size = 0;

		center_of_mass = new double[D];
		boundary = new Cell(dimension);
		for(int d = 0; d < D; d++) {
			boundary.setCorner(d, inp_corner[d]);
			boundary.setWidth( d, inp_width[d]);
			center_of_mass[d] = 0.0;
		}

		children = getTreeArray(no_children);
		for(int i = 0; i < no_children; i++) children[i] = null;
	}
	
	
	SPTree(int D, double [] inp_data, int N, double [] inp_corner, double [] inp_width)
	{
		init(null, D, inp_data, inp_corner, inp_width);
		fill(N);
	}


	
	SPTree(int D, double [] inp_data, double [] inp_corner, double [] inp_width)
	{
		init(null, D, inp_data, inp_corner, inp_width);
	}


	
	SPTree(SPTree inp_parent, int D, double [] inp_data, double [] inp_corner, double [] inp_width) {
		init(inp_parent, D, inp_data, inp_corner, inp_width);
	}


	
	SPTree(SPTree inp_parent, int D, double [] inp_data, int N, double [] inp_corner, double [] inp_width)
	{
		init(inp_parent, D, inp_data, inp_corner, inp_width);
		fill(N);
	}

	
	void setData(double [] inp_data)
	{
		data = inp_data;
	}


	
	SPTree getParent()
	{
		return parent;
	}

	SPTree[] getTreeArray(int no_children) {
		return new SPTree[no_children];
	}

	
	private boolean insert(int new_index)
	{
		
		double [] point = MatrixOps.extractRowFromFlatMatrix(data,new_index,dimension);

		if(!boundary.containsPoint(point))
			return false;

		
		cum_size++;
		double mult1 = (double) (cum_size - 1) / cum_size;
		double mult2 = 1.0 / cum_size;
		for(int d = 0; d < dimension; d++) {
			center_of_mass[d] *= mult1;
			center_of_mass[d] += mult2 * point[d];
		}

		
		if(is_leaf && size < QT_NODE_CAPACITY) {
			index[size] = new_index;
			size++;
			return true;
		}

		
		boolean any_duplicate = false;
		for(int n = 0; n < size; n++) {
			boolean duplicate = true;
			for(int d = 0; d < dimension; d++) {
				if(point[d] != data[index[n] * dimension + d]) { duplicate = false; break; }
			}
			any_duplicate = any_duplicate || duplicate;
		}
		if(any_duplicate) return true;

		
		if(is_leaf) subdivide();

		
		for(int i = 0; i < no_children; i++) {
			if(children[i].insert(new_index)) return true;
		}

		
		assert false;
		return false;
	}

	
	private void subdivide() {

		
		double [] new_corner = new double[dimension];
		double [] new_width  = new double[dimension];
		for(int i = 0; i < no_children; i++) {
			int div = 1;
			for(int d = 0; d < dimension; d++) {
				new_width[d] = 0.5 * boundary.getWidth(d);
				new_corner[d] = (i / div) % 2 == 1 ? boundary.getCorner(d) - 0.5 * boundary.getWidth(d) : boundary.getCorner(d) + 0.5 * boundary.getWidth(d);
				div *= 2;
			}
			children[i] = getNewTree(this, new_corner, new_width);
		}

		
		for(int i = 0; i < size; i++) {
			boolean success = false;
			for(int j = 0; j < no_children; j++) {
				if(!success) success = children[j].insert(index[i]);
			}
			index[i] = -1;
		}

		
		size = 0;
		is_leaf = false;
	}

	SPTree getNewTree(SPTree root, double[] new_corner, double[] new_width) {
		return new SPTree(root, dimension, data, new_corner, new_width);
	}

	
	private void fill(int N)
	{
		for(int i = 0; i < N; i++) insert(i);
	}


	
	private boolean isCorrect()
	{
        int bound = size;
		if (IntStream.range(0, bound).mapToObj(n -> MatrixOps.extractRowFromFlatMatrix(data, index[n], dimension)).anyMatch(point -> !boundary.containsPoint(point))) {
			return false;
		}
        if(!is_leaf) {
			boolean correct = true;
			for(int i = 0; i < no_children; i++) correct = correct && children[i].isCorrect();
			return correct;
		}
		else return true;
	}



	
	void getAllIndices(int [] indices)
	{
		getAllIndices(indices, 0);
	}


	
	private int getAllIndices(int[] indices, int loc)
	{

		
        System.arraycopy(index, 0, indices, loc, size);
		loc += size;

		
		if(!is_leaf) {
			for(int i = 0; i < no_children; i++) loc = children[i].getAllIndices(indices, loc);
		}
		return loc;
	}


	private int getDepth() {
		if(is_leaf) return 1;
		int depth = 0;
		for(int i = 0; i < no_children; i++) depth = max(depth, children[i].getDepth());
		return 1 + depth;
	}


	
	public double computeNonEdgeForces(int point_index, double theta, double[] neg_f, Object accumulator)
	{
		double [] sum_Q = (double []) accumulator;
		double [] buff = new double[dimension];
		
		if(cum_size == 0 || (is_leaf && size == 1 && index[0] == point_index)) return 0.0;

		
		double D = 0.0;
		int ind = point_index * dimension;
		
		double max_width = 0.0;
        for(int d = 0; d < dimension; d++) {
			buff[d] = data[ind + d] - center_of_mass[d];
			D += buff[d] * buff[d];
            double cur_width = boundary.getWidth(d);
            max_width = Math.max(max_width, cur_width);
		} 

		if(is_leaf || max_width / sqrt(D) < theta) {
			
			D = 1.0 / (1.0 + D);
			double mult = cum_size * D;
			sum_Q[0] += mult;
			mult *= D;
			for(int d = 0; d < dimension; d++) neg_f[d] += mult * buff[d];
		}
		else {

			
			for(int i = 0; i < no_children; i++) children[i].computeNonEdgeForces(point_index, theta, neg_f, sum_Q);
		}
		return sum_Q[0];
	}


	
	public void computeEdgeForces(int [] row_P, int [] col_P, double [] val_P, int N, double [] pos_f)
	{
		
		double [] buff = new double[dimension];
		int ind1 = 0;
		int ind2 = 0;
        for(int n = 0; n < N; n++) {
			for(int i = row_P[n]; i < row_P[n + 1]; i++) {


                ind2 = col_P[i] * dimension;
                double D = 1.0;
                for(int d = 0; d < dimension; d++) {
					buff[d] = data[ind1 + d] - data[ind2 + d];
					D += buff[d] * buff[d];
				} 
				D = val_P[i] / D;

				
				for(int d = 0; d < dimension; d++) pos_f[ind1 + d] += D * buff[d];
			}
			ind1 += dimension;
		}
	}


	
	private void print()
	{
		if(cum_size == 0) {
			System.out.print("Empty node\n");
			return;
		}

		if(is_leaf) {
			System.out.print("Leaf node; data = [");
			for(int i = 0; i < size; i++) {
				double [] point = MatrixOps.extractRowFromFlatMatrix(data, index[i], dimension);
				for(int d = 0; d < dimension; d++) System.out.printf("%f, ", point[d]);
				System.out.printf(" (index = %d)", index[i]);
				if(i < size - 1) System.out.print("\n");
				else System.out.print("]\n");
			}        
		}
		else {
			System.out.print("Intersection node with center-of-mass = [");
			for(int d = 0; d < dimension; d++) System.out.printf("%f, ", center_of_mass[d]);
			System.out.print("]; children are:\n");
			for(int i = 0; i < no_children; i++) children[i].print();
		}
	}
	
	static class Cell {
		final int dimension;
		final double [] corner;
		final double [] width;
		    
		
		Cell(int inp_dimension) {
			dimension = inp_dimension;
			corner = new double[dimension];
			width  = new double[dimension];
		}

		Cell(int inp_dimension, double [] inp_corner, double [] inp_width) {
			dimension = inp_dimension;
			corner = new double[dimension];
			width  = new double[dimension];
			for(int d = 0; d < dimension; d++) setCorner(d, inp_corner[d]);
			for(int d = 0; d < dimension; d++) setWidth( d,  inp_width[d]);
		}

		double getCorner(int d) {
			return corner[d];
		}

		double getWidth(int d) {
			return width[d];
		}

		void setCorner(int d, double val) {
			corner[d] = val;
		}

		void setWidth(int d, double val) {
			width[d] = val;
		}

		
		boolean containsPoint(double[] point)
		{
			for(int d = 0; d < dimension; d++) {
				if(corner[d] - width[d] > point[d]) return false;
				if(corner[d] + width[d] < point[d]) return false;
			}
			return true;
		}
	}

}