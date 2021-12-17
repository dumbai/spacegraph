package spacegraph.space2d.container.collection;

import jcog.data.map.CellMap;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.Surface;

import java.util.Collection;
import java.util.Objects;
import java.util.function.*;

public class MutableMapContainer<K, V> extends MutableContainer {

    /**
     * "cells" of the container; maintain the mapping between the indexed keys and their "materialized" representations or projections
     */
    protected final CellMap<K, V> cells = new CellMap<>() {
        @Override
        protected CacheCell<K, V> newCell() {
            return new SurfaceCacheCell<>();
        }

        @Override
        protected final void unmaterialize(CacheCell<K, V> entry) {
            MutableMapContainer.this.unmaterialize(entry.value);
        }
        //        @Override
//        protected void added(CacheCell<K, V> entry) {
//            if (parent == null)
//                return;
//
//            Surface es = ((SurfaceCacheCell) entry).surface;
//            //if (es != null && es.parent == null)
//            es.start(MutableMapContainer.this);
//        }


//        @Override
//        protected void invalidated() {
//            super.invalidated();
//        }
    };


    protected void unmaterialize(V v) {

    }


    @Override
    public void forEach(Consumer<Surface> each) {
        cells.map.forEachValueWith((e, EACH) -> {
            Surface s = ((SurfaceCacheCell) e).surface;
            if (s == null) {
                if (e.value instanceof Surface)
                    s = (Surface) e.value; //HACK
            }
            if (s != null)
                EACH.accept(s);
        }, each);
    }


    public void forEachValue(Consumer<? super V> each) {
        cells.forEachValue(each);
    }


    @Override
    protected void doLayout(float dtS) {

    }

    @Override
    public void add(Surface... s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int childrenCount() {
        return Math.max(1, cells.size());
    }

    @Override
    protected MutableContainer clear() {
        cells.clear();
        return null;
    }

    protected void removeAll(Iterable<K> x) {
        cells.removeAll(x);
    }

    public Collection<K> keySet() {
        return cells.map.keySet();
    }

    public @Nullable V getValue(K x) {
        return cells.getValue(x);
    }

    public @Nullable Surface getSurface(K x) {
        SurfaceCacheCell<K, V> c = (SurfaceCacheCell<K, V>) cells.map.get(x);
        return c!=null ? c.surface : null;
    }

    public CellMap.CacheCell<K, V> compute(K key, UnaryOperator<V> builder) {
        CellMap.CacheCell<K, V> y = cells.compute(key, builder);
        V v = y.value;
        if(v instanceof Surface)
            ((SurfaceCacheCell)y).surface = (Surface) v;
        return y;
    }

    public CellMap.CacheCell<K, V> computeIfAbsent(K key, Function<K, V> builder) {
        CellMap.CacheCell<K, V> y = cells.computeIfAbsent(key, builder);
        V v = y.value;
        if(v instanceof Surface)
            ((SurfaceCacheCell)y).surface = (Surface) v;
        return y;
    }

    protected CellMap.CacheCell<K, V> put(K key, V nextValue, BiFunction<K, V, Surface> renderer) {

        CellMap.CacheCell<K, V> entry = cells.map.computeIfAbsent(key, k -> cells.cellPool.get());

        ((SurfaceCacheCell<K,V>) entry).update(key, nextValue, renderer, this::hide);

        return cells.update(key, entry, entry.key != null);

    }


    /** default behavior is to call Surface.stop() but caching can be implemented here */
    protected void hide(V key, Surface s) {
//        if (cache == null) {
            s.stop();
//        } else {
//            s.hide();
//        }
    }

    public V remove(Object key) {
        CellMap.CacheCell<K, V> c = cells.remove(key);
        return c!=null ? c.value : null;
    }

    @Override
    protected boolean _remove(Surface s) {
        K k = cells.firstByIdentity((V)s);
        return k!=null && remove(k)!=null;
   }

    //    @Override
//    public boolean detachChild(Surface s) {
//        K k = cells.firstByIdentity((V)s);
//        return k!=null && remove(k)!=null;
//    }


    public void getValues(Collection<V> l) {
        cells.getValues(l);
    }

    @Override
    public boolean whileEach(Predicate<Surface> o) {
        return cells.whileEach(e -> {
            Surface s = ((SurfaceCacheCell) e).surface;
            return s == null || o.test(s);
        });
    }

    @Override
    public boolean whileEachReverse(Predicate<Surface> o) {
        return cells.whileEachReverse(e -> {
            Surface s = ((SurfaceCacheCell) e).surface;
            return s == null || o.test(s);
        });
    }


    public static class SurfaceCacheCell<K, V> extends CellMap.CacheCell<K, V> {

        public transient volatile Surface surface;

        @Override
        public void clear() {
            super.clear();

            Surface s = surface;
            surface = null;

            if (s != null) {
                s.stop();
            }
        }


        /** returns previous surface, or null if unchanged */
        private Surface setSurface(Surface next) {
            ///assert (surface == null);
            Surface prev = this.surface;
            if (next != prev) {
                this.surface = next;
                return prev;
            }
            else
                return null;
        }

        /**
         * return true to keep or false to remove from the map
         */
        void update(K nextKey, V nextValue, BiFunction<K, V, Surface> renderer, BiConsumer<V, Surface> hider) {

            Surface removed;
            if (nextValue == null) {
                this.key = null;
                set(null);
                removed = setSurface(null);
            } else {

                if (surface == null || !Objects.equals(this.value, nextValue)) {
                    Surface nextSurface = renderer.apply(nextKey, nextValue);
                    set(nextValue);
                    removed = setSurface(nextSurface);
                } else {
                    removed = null;
                }

                this.key = nextKey; //ready
            }

            if (removed!=null) {
                hider.accept(nextValue, removed);
            }

        }
    }
}