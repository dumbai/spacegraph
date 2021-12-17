package com.jujutsu.tsne.barneshut;

import org.hipparchus.linear.ArrayRealVector;

import java.util.Comparator;

public class DistanceComparator implements Comparator<ArrayRealVector> {
	private final ArrayRealVector refItem;
	private final Distance dist;
	
	DistanceComparator(ArrayRealVector refItem) {
		this(refItem, new EuclideanDistance());
	}
	
	DistanceComparator(ArrayRealVector refItem, Distance dist) {
		this.refItem = refItem;
		this.dist = dist;
	}

	@Override
	public int compare(ArrayRealVector o1, ArrayRealVector o2) {
		return Double.compare(dist.distanceSq(o1, refItem), dist.distanceSq(o2, refItem));
	}
}