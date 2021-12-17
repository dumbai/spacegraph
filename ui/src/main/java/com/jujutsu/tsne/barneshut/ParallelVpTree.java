package com.jujutsu.tsne.barneshut;

import org.hipparchus.linear.ArrayRealVector;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

public class ParallelVpTree<StorageType> extends VpTree<StorageType> {

	private final ForkJoinPool searcherPool;
	
	public ParallelVpTree(ForkJoinPool pool, Distance distance) {
		super(distance);
		searcherPool = pool;
	}
	
	public ParallelVpTree(ForkJoinPool pool) {
		searcherPool = pool;
	}
	
//	public List<Future<ParallelTreeNode.TreeSearchResult>> searchMultiple(ParallelVpTree<StorageType> tree, DataPoint [] targets, int k) {
//		Collection<ParallelTreeNode.ParallelTreeSearcher> searchers = new ArrayList<>(targets.length);
//		for(int n = 0; n < targets.length; n++) {
//			@SuppressWarnings("unchecked")
//			ParallelTreeNode node = (ParallelTreeNode) tree.getRoot();
//			searchers.add(node.new ParallelTreeSearcher(node,_items,targets[n], k, n));
//		}
//		return searcherPool.invokeAll(searchers);
//	}

	@Override
	protected VpTree<StorageType>.Node createNode() {
		return new ParallelTreeNode();
	}

	public class ParallelTreeNode extends VpTree<StorageType>.Node {
		
		public class TreeSearchResult {
			final int n;
			final List<Double> distances;
			final List<ArrayRealVector> indices;
			
			TreeSearchResult(List<ArrayRealVector> indices, List<Double> distances, int n) {
				this.indices = indices;
				this.distances = distances;
				this.n = n;
			}

			public List<ArrayRealVector> getIndices() {
				return indices;
			}

			public List<Double> getDistances() {
				return distances;
			}

			
			public int getIndex() {
				return n;
			}

		}

		class ParallelTreeSearcher implements Callable<TreeSearchResult> {
			final Node node;
			Queue<HeapItem> heap;
			final ArrayRealVector target;
			final int k;
			final int n;
			final ArrayRealVector [] items;

			ParallelTreeSearcher(Node tree, ArrayRealVector[] items, ArrayRealVector target, int k, int n) {
				this.node = tree;
				this.target = target;
				this.k = k;
				this.items = items;
				this.n = n;
			}

			@Override
			public TreeSearchResult call() {
                PriorityQueue<HeapItem> heap = new PriorityQueue<>(k, (o1, o2) -> -1 * o1.compareTo(o2));

				double tau = Double.MAX_VALUE;
				
				node.search(node, target, k, heap, tau);


                List<Double> distances = new ArrayList<>();
                List<ArrayRealVector> indices = new ArrayList<>();
                while(!heap.isEmpty()) {
					HeapItem h = heap.remove();
					indices.add(items[h.index]);
					distances.add(h.dist);
				}
				
				
				Collections.reverse(indices);
				Collections.reverse(distances);

				return new TreeSearchResult(indices, distances,n);
			}   
		}
	}
}
