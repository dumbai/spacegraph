/*
 *   __               .__       .__  ._____.           
 * _/  |_  _______  __|__| ____ |  | |__\_ |__   ______
 * \   __\/  _ \  \/  /  |/ ___\|  | |  || __ \ /  ___/
 *  |  | (  <_> >    <|  \  \___|  |_|  || \_\ \\___ \ 
 *  |__|  \____/__/\_ \__|\___  >____/__||___  /____  >
 *                   \/       \/             \/     \/ 
 *
 * Copyright (c) 2006-2011 Karsten Schmidt
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * http://creativecommons.org/licenses/LGPL/2.1/
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */

package toxi.geom;

import jcog.Util;
import jcog.data.set.ArrayHashSet;
import jcog.pri.Prioritized;
import jcog.tree.rtree.rect.RectF;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Implements a spatial subdivision tree to work efficiently with large numbers
 * of 2D particles. This quadtree can only be used for particle type objects and
 * does NOT support 2D mesh geometry as other forms of quadtree might do.
 * 
 * For further reference also see the QuadtreeDemo in the /examples folder.
 * 
 */
public class QuadtreeIndex<V extends Vec2D> extends Rect implements SpatialIndex<V> {

    private static final int LeafCapacity = 8;
    private static final float MinLeafDimension = 0.0000001f;

    public enum Type {
        EMPTY,
        BRANCH,
        LEAF
    }

    private final QuadtreeIndex parent;
    private QuadtreeIndex nw;
    private QuadtreeIndex ne;
    private QuadtreeIndex sw;
    private QuadtreeIndex se;

    private Type type;

    private Set<V> values;
    private float mx;
    private float my;

    public QuadtreeIndex() {
        this(null, 0.5f, 0.5f, 1, 1);
    }

    public QuadtreeIndex(float x, float y, float w, float h) {
        this(null, x, y, w, h);
    }

    public QuadtreeIndex(QuadtreeIndex parent, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.parent = parent;
        this.type = Type.EMPTY;
        mx = x + w * 0.5f;
        my = y + h * 0.5f;
    }

    public QuadtreeIndex(Rect r) {
        this(null, r.x, r.y, r.width, r.height);
    }

    private void balance() {
        switch (type) {
            case EMPTY:
            case LEAF:
                if (parent != null) parent.balance();
                break;

            case BRANCH:
                QuadtreeIndex<V> leaf = null;
                if (nw.type != Type.EMPTY) leaf = nw;
                if (ne.type != Type.EMPTY) {
                    if (leaf != null) break;
                    leaf = ne;
                }
                if (sw.type != Type.EMPTY) {
                    if (leaf != null) break;
                    leaf = sw;
                }
                if (se.type != Type.EMPTY) {
                    if (leaf != null) break;
                    leaf = se;
                }
                if (leaf == null) {
                    type = Type.EMPTY;
                    nw = ne = sw = se = null;
                } else if (leaf.type == Type.BRANCH) break;
                else {
                    type = Type.LEAF;
                    nw = ne = sw = se = null;
                    //value = leaf.value;
                    values = leaf.values;
                }
                if (parent != null) parent.balance();
        }
    }


    @Override
    public void clear() {
        nw = ne = sw = se = null;
        type = Type.EMPTY;
        values = null;
    }

    public QuadtreeIndex findNode(V p) {
        return switch (type) {
            case EMPTY -> null;
//value.x == x && value.y == y ? this : null;
            case LEAF -> contains(p) ? this : null;
            case BRANCH -> quadrant(p.x, p.y).findNode(p);
            default -> throw new IllegalStateException("Invalid node type");
        };
    }

    boolean contains(V p) {
        return (values!=null && values.contains(p));
    }


    private QuadtreeIndex quadrant(float x, float y) {
        if (x < mx) return y < my ? nw : sw;
        else return y < my ? ne : se;
    }

    /**
     *
     * @param p
     * @return
     */
    @Override
    public boolean index(V p) {
        if (containsPoint(p)) switch (type) {
            case EMPTY:
                setPoint(p);
                return true;

            case LEAF:
                if (size() < LeafCapacity || Math.max(width, height) <= MinLeafDimension) return values.add(p);
                else if (values.contains(p))
                    return false;
                else {
                    split();
                    return quadrant(p.x, p.y).index(p);
                }


            case BRANCH:
                return quadrant(p.x, p.y).index(p);
        }
        else {
//            if (parent == null && stretchPoint(p, MinLeafArea)) {
//                //resize the entire tree
//                //experimental
//                split();
//                boolean added = quadrant(p.x, p.y).index(p);
//                return added;
//            }
        }
        return false;
    }

    @Override
    public void bounds(RectF bounds) {
        if (bounds.equals(x, y, width, height, Prioritized.EPSILON))
            return; //no change

        x = bounds.x;
        y = bounds.y;
        width = bounds.w;
        height = bounds.h;
        mx = x + width * 0.5f;
        my = y + height * 0.5f;
        split();
        //            if (parent == null && stretchPoint(p, MinLeafArea)) {
//                //resize the entire tree
//                split();
//                boolean added = quadrant(p.x, p.y).index(p);
//                return added;

    }

    @Override
    public boolean isIndexed(V p) {
        return findNode(p) != null;
    }

    @Override
    public void itemsWithinRadius(Vec2D p, float radius,
                                  Consumer<V> results) {
        if (!intersectsCircle(p, radius))
            return;

        switch (type) {
            case LEAF -> {
                Set<V> v = this.values;
                if (v != null) {
                    float rsquare = Util.sqr(radius);
                    for (V value : v)
                        if (value != null /* HACK */ && value.distanceToSquared(p) < rsquare) results.accept(value);
                }
            }
            case BRANCH -> {
                if (nw != null) nw.itemsWithinRadius(p, radius, results);
                if (ne != null) ne.itemsWithinRadius(p, radius, results);
                if (sw != null) sw.itemsWithinRadius(p, radius, results);
                if (se != null) se.itemsWithinRadius(p, radius, results);
            }
        }
    }

//    public List<Vec2D> itemsWithinRect(Rect bounds, List<Vec2D> results) {
//        if (bounds.intersectsRect(this)) {
//            if (type == Type.LEAF) {
//                if (bounds.containsPoint(value)) {
//                    if (results == null) {
//                        results = new ArrayList<>();
//                    }
//                    results.addAt(value);
//                }
//            } else if (type == Type.BRANCH) {
//                PointQuadtree[] children = new PointQuadtree[] {
//                        childNW, childNE, childSW, childSE
//                };
//                for (int i = 0; i < 4; i++) {
//                    if (children[i] != null) {
//                        results = children[i].itemsWithinRect(bounds, results);
//                    }
//                }
//            }
//        }
//        return results;
//    }

    public void prewalk(Consumer<QuadtreeIndex> visitor) {
        switch (type) {
            case LEAF -> visitor.accept(this);
            case BRANCH -> {
                visitor.accept(this);
                nw.prewalk(visitor);
                ne.prewalk(visitor);
                sw.prewalk(visitor);
                se.prewalk(visitor);
            }
        }
    }

    @Override
    public boolean reindex(V p, Consumer<V> each) {
        unindex(p);
        each.accept(p);
        return index(p);
    }


    private void setPoint(V p) {
        assert(values == null);
        if (type == Type.BRANCH) throw new IllegalStateException("invalid node type: BRANCH");
        type = Type.LEAF;
        values = //new UnifiedSet(LeafCapacity, 0.99f);
            new ArrayHashSet(LeafCapacity);
        values.add(p);
    }

    @Override
    public int size() {
        return values!=null ? values.size() : 0;
    }

    private void split() {
        split(x, y, width, height);
    }

    private void split(float x, float y, float w, float h) {
        Set<V> oldPoints = values;
        values = null;

        type = Type.BRANCH;

        QuadtreeIndex<V> nw = this.nw, ne = this.ne, se = this.se, sw = this.sw;
        float h2 = h / 2;
        float w2 = w / 2;
        this.nw = new QuadtreeIndex(this, x, y, w2, h2);
        this.ne = new QuadtreeIndex(this, mx, y, w2, h2);
        this.sw = new QuadtreeIndex(this, x, my, w2, h2);
        this.se = new QuadtreeIndex(this, mx, my, w2, h2);

        //            oldPoints.forEach((p)->{
        //                if (!this.quadrant(p.x, p.y).index(p)) {
        ////                    if (!parent.index(p))
        //                        throw new WTF();
        //                }
        //            });
        if (oldPoints!=null) for (V oldPoint : oldPoints) index(oldPoint);

        if (nw!=null) nw.forEach(this::index);
        if (ne!=null) ne.forEach(this::index);
        if (sw!=null) sw.forEach(this::index);
        if (se!=null) se.forEach(this::index);
    }

    public void forEach(Consumer<V> v) {
        if (values!=null) for (V value : values) v.accept(value);
        if (nw !=null)
            nw.forEach(v);
        if (ne !=null)
            ne.forEach(v);
        if (se !=null)
            se.forEach(v);
        if (sw !=null)
            sw.forEach(v);
    }


    @Override
    public boolean unindex(V p) {
        QuadtreeIndex node = findNode(p);
        if (node != null) {
            boolean removed = node.values.remove(p);
            assert(removed);
            if (node.values.isEmpty()) {
                node.type = Type.EMPTY;
                node.values = null;
            } else node.balance();
            return true;
        } else return false;
    }
}