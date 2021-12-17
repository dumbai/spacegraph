package spacegraph.space2d.container.layout;

import jcog.pri.Prioritized;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.util.MutableRectFloat;

/**
 * Implements the Squarified Treemap layout published by
 * Mark Bruls, Kees Huizing, and Jarke J. van Wijk
 * <p>
 * Squarified Treemaps
 * https://www.win.tue.nl/~vanwijk/stm.pdf
 *
 * adapted from:https://github.com/peterdmv/treemap/
 */
public class TreeMap2D<X> extends DynamicLayout2D<X> {



    @Override
    protected void layout(Graph2D<X> g, float dtS) {
        RectF b = g.bounds;
        //sort descending
        nodes.sortThisByFloat((a)-> {
            a.w = b.w;
            a.h = b.h;
            return -a.node.pri;
        });
        int end = nodes.size() - 1;
        total = areaSum(0, end);

        RectF newBounds = layout(0, end, b);

        //normalize
        float nw = b.w / newBounds.w;
        float nh = b.h / newBounds.h;
        nodes.forEach(n -> {
            n.x = b.x + (n.x - b.x) * nw;
            n.y = b.y + (n.y - b.y) * nh;
            n.w *= nw;
            n.h *= nh;

        });
    }


    int mid;
    float total;
    public RectF layout(int start, int end, RectF bounds) {
        if (start > end) {
            return bounds;
        }
        if (start == end) {
            nodes.get(start).set(bounds);
        }

        RectF newBounds = bounds;
        this.mid = start;
        while (mid < end) {
            if (highestAspect(start, mid, bounds) > highestAspect(start, mid + 1, bounds)) {
                mid++;
            } else {
                newBounds = layoutRow(start, mid, bounds);
                newBounds = layout(mid + 1, end, newBounds);
            }
        }

      return newBounds;
    }

    float highestAspect(int start, int end, RectF bounds) {

        RectF b = layoutRow(start, end, bounds);

        float max = Float.NEGATIVE_INFINITY;
        for (int i = start; i <= end; i++) {
            MutableRectFloat<X> ni = this.nodes.get(i);
            float r = ni.aspectExtreme();
            if (r > max) {
                max = r;
            }
        }
        return max;
    }

    RectF layoutRow(int start, int end, RectF bounds) {
        boolean isHorizontal = bounds.w < bounds.h;

        float rowSize = areaSum(start, end);
        float rowRatio = rowSize / total;
       // assert(rowRatio==rowRatio): start + ".." + end + ": " + rowSize + " " + total;
        float offset = 0;

        for (int i = start; i <= end; i++) {
            MutableRectFloat x = this.nodes.get(i);

            float p = x.node.pri;
            if (p != p || p < Prioritized.EPSILON) {
                x.size(0.1f,0.1f);
                continue;
            }

            float ratio = p / rowSize;


            assert(ratio==ratio): p + " " + rowSize;

            RectF r;
			r = isHorizontal ? RectF.X0Y0WH(bounds.x,
				bounds.y + bounds.h * offset,
				bounds.w * rowRatio,
				bounds.h * ratio) : RectF.X0Y0WH(
				bounds.x + bounds.w * offset,
				bounds.y,
				bounds.w * ratio,
				bounds.h * rowRatio);
            x.set(r);
            offset += ratio;
        }
		return isHorizontal ? RectF.X0Y0WH(bounds.x + bounds.w * rowRatio, bounds.y, bounds.w - bounds.w * rowRatio, bounds.h) : RectF.X0Y0WH(bounds.x, bounds.y + bounds.h * rowRatio, bounds.w, bounds.h - bounds.h * rowRatio);

    }


    float areaSum(int start, int end) {
        float sum = 0;
        for (int i = start; i <= end; i++)
            sum += nodes.get(i).node.pri;
        return sum;
    }

//    private void quickSortDesc(MovingRectFloat2D[] inputArr, int lowerIndex, int higherIndex) {
//
//        int i = lowerIndex;
//        int j = higherIndex;
//        // calculate pivot number
//        float pivot = inputArr[lowerIndex+(higherIndex-lowerIndex)/2].getSize();
//        // Divide into two arrays
//        while (i <= j) {
//            /**
//             * In each iteration, we will identify a number from left side which
//             * is greater then the pivot value, and also we will identify a number
//             * from right side which is less then the pivot value. Once the search
//             * is done, then we exchange both numbers.
//             */
//            while (inputArr[i].getSize() > pivot) {
//                i++;
//            }
//            while (inputArr[j].getSize() < pivot) {
//                j--;
//            }
//            if (i <= j) {
//                MovingRectFloat2D temp = inputArr[i];
//                inputArr[i] = inputArr[j];
//                inputArr[j] = temp;
//                //move index to next position on both sides
//                i++;
//                j--;
//            }
//        }
//        // call quickSort() method recursively
//        if (lowerIndex < j)
//            quickSortDesc(inputArr, lowerIndex, j);
//        if (i < higherIndex)
//            quickSortDesc(inputArr, i, higherIndex);
//    }
}
