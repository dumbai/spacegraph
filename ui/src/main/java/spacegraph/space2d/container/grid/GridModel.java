package spacegraph.space2d.container.grid;

import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.container.ScrollXY;

public interface GridModel<X> {
    int cellsX();

    int cellsY();

    /**
     * return null to remove the content of a displayed cell
     */
    @Nullable X get(int x, int y);

    default void start(ScrollXY<X> x) {
    }

    default void stop(ScrollXY<X> x) {
    }
}
