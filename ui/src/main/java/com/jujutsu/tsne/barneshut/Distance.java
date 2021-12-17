package com.jujutsu.tsne.barneshut;

import org.hipparchus.linear.ArrayRealVector;

@FunctionalInterface
public interface Distance {
	double distance(ArrayRealVector d1, ArrayRealVector d2);

	default double distanceSq(ArrayRealVector d1, ArrayRealVector d2) {
		double x = distance(d1, d2);
		return x*x;
	}
}
