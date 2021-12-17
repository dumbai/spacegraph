package com.jujutsu.tsne.barneshut;


import org.hipparchus.linear.ArrayRealVector;

import static java.lang.Math.min;

@Deprecated public class DataPoint extends ArrayRealVector {
	
	private final int _ind;
	final int _D;


	public DataPoint(int D, int ind, double [] x) {
		super(x.clone());
		_D = D;
		_ind = ind;
	}
	
	@Override
	public String toString() {
		StringBuilder xStr = new StringBuilder();
		int c = min(20, getDimension());
		for (int i = 0; i < c; i++) {
			xStr.append(getEntry(i)).append(", ");
		}
		return xStr.append("DataPoint (index=").append(_ind).append(", Dim=").append(_D).append(", point=").append(xStr).append(')').toString();
	}

	public int index() { return _ind; }


}
