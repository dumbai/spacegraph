package spacegraph.space2d.widget.port;

import jcog.Util;

public class IntRangePort extends IntPort {
    private final int min;
    private final int max;

    public IntRangePort(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Integer process(Integer v) {
        return v!=null ? Util.clamp(v, min, max) : null;
    }
}
