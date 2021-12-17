package spacegraph.util.state;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import jcog.TODO;
import jcog.data.list.Lst;

import java.util.Iterator;
import java.util.function.Function;

public final class StatePath extends Lst<Context> {

    public static StatePath toRoot(Context here) {
        StatePath p = new StatePath(8);
        Context at = here;
        do {
            p.add(at);
        } while ((at = at.parent())!=null);
        p.reverseThis();
        return p;
    }

    private StatePath(int estimatedSize) {
        super(estimatedSize);
    }

    public MatchPath types(boolean innerGlob) {
        return match((c)->c.getClass().getSimpleName(), innerGlob);
    }

    public MatchPath ids(boolean innerGlob) {
        return match(Context::id, innerGlob);
    }

    private MatchPath match(Function<Context, String> pattern, boolean innerGlob) {
        int s = this.size();
        MatchPath m = new MatchPath(innerGlob ? s + (s-1) : s);
        for (int i = 0; i < s; i++) {
            Context x = this.get(i);
            m.add(pattern.apply(x));
            if (innerGlob && i < s-1) m.add(MatchPath.STAR);
        }
        return m;
    }

    private static Iterator all(String key) {
        throw new TODO();
    }

    public static <X> X first(String key) {
        return (X)(all(key).next());
    }

    @Override
    public String toString() {
        return Joiner.on('/').join(Iterables.transform(this, Context::id));
    }

}
