package spacegraph.space2d.container.grid;



import org.eclipse.collections.api.multimap.Multimap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/** simple 2-column/2-row key->value table */
public class KeyValueGrid implements GridModel {

    private final Function map;

    /** cached keys as an array for fast access */
    private final transient Object[] keys;

    public KeyValueGrid(Map map) {
        this.map = map::get;
        this.keys = map.keySet().toArray();
    }
    public KeyValueGrid(com.google.common.collect.Multimap map) {
        this.map = map::get;
        this.keys = map.keySet().toArray();
    }
    public KeyValueGrid(Multimap map) {
        this.map = map::get;
        this.keys = map.keySet().toArray();
    }

    @Override
    public int cellsX() {
        return 2;
    }

    @Override
    public int cellsY() {
        return keys.length;
    }

    @Override
    public @Nullable Object get(int x, int y) {
        Object[] k = this.keys;
        if ((y < 0) || y >= keys.length)
            return null; //OOB

        return switch (x) {
            case 0 -> k[y];
            case 1 -> map.apply(k[y]);
            default -> null;
        };
    }
}
