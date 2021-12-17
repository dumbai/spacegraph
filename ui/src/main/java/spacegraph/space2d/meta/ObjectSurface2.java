package spacegraph.space2d.meta;

import com.google.common.base.Joiner;
import jcog.TODO;
import jcog.data.bit.AtomicMetalBitSet;
import jcog.data.list.Lst;
import jcog.reflect.CastGraph;
import jcog.signal.FloatRange;
import spacegraph.SpaceGraph;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.space2d.widget.textedit.TextEdit;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectSurface2 extends MutableUnitContainer {


    public static void main(String[] args) {
        Object x = List.of(new FloatRange(0.6f, 0.0f, 1.0f), new Lst<>(new String[] { "x", "y" }));
        SpaceGraph.window(new ObjectSurface2(x), 500, 500);
    }

    private static final CastGraph DefaultBuilder = new CastGraph();

    private static <X> void build(Class<? extends X> from, Function<? extends X,Surface> builder) {
        DefaultBuilder.addEdge(from, builder, Surface.class);
    }
    {
        build(List.class, (List<Object> x) -> new Gridding(x.stream().map(this::build).toArray(Surface[]::new)));
        build(String.class, VectorLabel::new);
//        build(FloatRange.class, FloatRangePort::new);
//        build(FloatRange.class, x -> new VectorLabel(x.toString()));
        build(AnyOf.class, x -> new TextEdit(x.toString()));
    }


    final CastGraph the;

    public ObjectSurface2(Object x) {
        this(x, DefaultBuilder);
    }

    public ObjectSurface2(Object x, CastGraph builder) {
        this.the = builder;
        set(x);
    }

    public void set(Object x) {
        set(build(x));
    }

    protected Surface build(Object x) {
        List<Function<Object, Surface>> xy = the.applicable(x.getClass(), Surface.class);
        Surface y;
        switch (xy.size()) {
            case 0 -> y =
                    //new VectorLabel(x.toString()); //TODO more info
                    new ObjectSurface(x, 1);
            case 1 -> y = xy.get(0).apply(x);
            default -> {
                List<Function<AnyOf, Surface>> xyz = the.applicable(AnyOf.class, Surface.class); //warning, could recurse
                assert (xyz.size() == 1) : "multiple materializations of " + AnyOf.class;
                y = xyz.get(0).apply(new AnyOf(x, xy));
            }
        }
        return y;
    }

    //interface FunctionOftheObject..
    //interface FunctionOftheSurface..


    public static class AnyOf<X,Y>  {

        final List<Function<Object, Y>> f;
        final X x;

        public AnyOf(X x, List<Function<Object, Y>> f) {
            this.f = f;
            this.x = x;
        }

        @Override
        public String toString() {
            return Joiner.on("\n").join(f) + "\n\t x " + x;
        }
    }

    public abstract static class Way<X> implements Supplier<X> {
        public String name;
    }

    /** supplies zero or more chocies from a set */
    public static class Some<X> implements Supplier<X[]> {
        final Way<X>[] way;
        final AtomicMetalBitSet enable = new AtomicMetalBitSet();

        public Some(Way<X>[] way) {
            this.way = way;
            assert(way.length > 1 && way.length <= 31 /* AtomicMetalBitSet limit */);
        }

        public Some<X> set(int which, boolean enable) {
            this.enable.set(which, enable);
            return this;
        }

        @Override
        public X[] get() {
            throw new TODO();
        }

        public int size() {
            return way.length;
        }
    }

//    public static class Best<X> extends RankedN implements Supplier<X> {
//        final Some<X> how;
//        final FloatRank<X> rank;
//
//        public Best(Some<X> how, FloatRank<X> rank) {
//            super(new Object[how.size()], rank);
//            this.how = how;
//            this.rank = rank;
//        }
//
//        @Override
//        public X get() {
//            clear();
//            X[] xx = how.get();
//            if (xx.length == 0)
//                return null;
//            for (X x : xx)
//                add(x);
//            return (X) top();
//        }
//    }

    /** forces a one or none choice from a set */
    public static class Either<X> implements Supplier<X> {
        final Way<X>[] way;
        volatile int which = -1;

        @SafeVarargs
        public Either(Way<X>... way) {
            assert(way.length > 1);
            this.way = way;
        }

        public Either<X> set(int which) {
            this.which = which;
            return this;
        }

        public final Either<X> disable() {
            set(-1);
            return this;
        }

        @Override
        public X get() {
            int c = this.which;
            return c >=0 ? way[c].get() : null;
        }
    }

}