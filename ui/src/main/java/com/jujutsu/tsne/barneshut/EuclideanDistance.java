package com.jujutsu.tsne.barneshut;

import jcog.Util;
import org.hipparchus.linear.ArrayRealVector;

public class EuclideanDistance implements Distance{

	@Deprecated public EuclideanDistance() {
	}

	@Override
	public double distance(ArrayRealVector d1, ArrayRealVector d2) {
		return Math.sqrt(distanceSq(d1,d2));
	}

	@Override
	public double distanceSq(ArrayRealVector d1, ArrayRealVector d2) {
	    double dd = 0.0;
	    double [] x1 = d1.getDataRef();
	    double [] x2 = d2.getDataRef();
	    int dim = x1.length;
        for(int d = 0; d < dim; d++)
            dd += Util.sqr(x1[d] - x2[d]);
	    return dd;
	}

}