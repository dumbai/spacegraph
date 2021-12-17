package spacegraph.space2d.container.grid;

import jcog.TODO;
import jcog.Util;
import jcog.data.list.Lst;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.container.ScrollXY;

import java.util.List;

public abstract class ListModel<X> implements GridModel<X> {

    /** orientation, dynamically changeable. true=vertical, false=horizontal. default=vertical */
    private static final boolean vertical = true;

    private ScrollXY<X> surface;

    @SafeVarargs
    public static <X> ListModel<X> of(X... items) {
        return of(List.of(items));
    }

    /** ideally a random-access list like Lst<>, ArrayList<> etc */
    public static <X> ListModel<X> of(List<X> items) {
        return new ListListModel<>(items);
    }

    @Override
    public void start(ScrollXY<X> x) {
        if (surface!=null)
            throw new TODO("support multiple observers");
        this.surface = x;
    }

    @Override
    public void stop(ScrollXY<X> x) {
        this.surface = null;
    }

//    public void onChange() {
//        surface.refresh();
//    }
//
//    public void setOrientation(boolean vertical) {
//        this.vertical = vertical;
//    }

    protected abstract X get(int index);
    protected abstract int size();

    /** thickenss of the table, one by default */
    private static int depth() {
        return 1;
    }

    @Override
    public final int cellsX() {
        return vertical ? depth() : size();
    }

    @Override
    public final int cellsY() {
        return vertical ? size() : depth();
    }

    @Override
    public final @Nullable X get(int x, int y) {
        if ((vertical ? x : y) != 0)
            return null;
        return get(vertical ? y : x);
    }


    public static class ListListModel<X> extends ListModel<X> {

        protected final List<X> items;

        public ListListModel(List<X> items) {
            this.items = items;
        }

        @Override
        public X get(int index) {
            try {
                return items.get(index);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("warning: " + e.getMessage());
                return null;
            }
        }

        @Override
        public int size() {
            return items.size();
        }
    }

    public static class AsyncListModel<X> extends ListListModel<X> implements ScrollXY.ScrolledXY {

        private final Iterable<X> lister;
        int size;

        public AsyncListModel(Iterable<X> lister, int size) {
            super(new Lst<>());
            this.lister = lister;
            this.size = size;
        }

        @Override
        public void update(ScrollXY s) {

            this.items.clear();
            lister.forEach(items::add);
//			items.ensureCapacity(bag.capacity());
//			bag.forEach(items::addFast);

            s.viewMin(1,1);
            s.viewMax(1, Util.clamp(items.size(), 1, size));

//			s.update();
        }

    }
}