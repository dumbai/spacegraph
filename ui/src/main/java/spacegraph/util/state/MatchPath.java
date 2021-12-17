package spacegraph.util.state;

import jcog.data.list.Lst;

public class MatchPath extends Lst<String> {

    public static final String STAR = "*";

    public MatchPath(int estSize) {
        super(estSize);
    }
}
