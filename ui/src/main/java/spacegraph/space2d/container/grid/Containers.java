package spacegraph.space2d.container.grid;

import com.google.common.collect.Iterables;
import jcog.Util;
import jcog.data.list.Lst;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Splitting;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class Containers {

    public static Gridding grid(Iterable<? extends Surface> content) {
        return grid(Iterables.toArray(content, Surface.class));
    }

    public static Gridding grid(Stream<? extends Surface> content) {
        return grid(content.toArray(Surface[]::new));
    }

    public static Gridding grid(Surface... content) {
        return new Gridding(new Lst<>(content));
    }

    public static <S> Gridding grid(Collection<S> c, Function<S, Surface> builder) {
        List<Surface> ss = new Lst(c.size());
        for (S x : c)
            ss.add(builder.apply(x));
        return new Gridding(ss);
    }

    public static Surface row(Collection<? extends Surface> content) {
        return row(array(content));
    }

    public static Surface col(Collection<? extends Surface> content) {
        return col(array(content));
    }

    public static Splitting<?,?> row(Surface a, float split, Surface b) {
        return new Splitting<>(a, split, false, b);
    }

    public static Splitting<?,?> col(Surface a, float split, Surface b) {
        return new Splitting<>(a, split, true, b);
    }

    public static Surface row(Surface... content) {
        if (content.length == 2)
            return row(content[0], 0.5f, content[1]);
        else
            return new Gridding(Gridding.HORIZONTAL, content);
    }

    public static Surface col(Surface... content) {
        if (content.length == 2)
            return col(content[0], 0.5f, content[1]);
        else
            return new Gridding(Gridding.VERTICAL, content);
    }

    public static Gridding grid(int num, IntFunction<Surface> build) {
        return new Gridding(Util.arrayOf(build, 0, num, Surface[]::new));
    }

    public static Surface[] array(Collection<? extends Surface> content) {
        return content.toArray(Surface.EmptySurfaceArray);
    }
}