package spacegraph.space2d.widget.port;

import jcog.reflect.ExtendedCastGraph;

import java.util.function.Consumer;

public class TypedPort<X> extends Port<X> {

    public static final ExtendedCastGraph CAST = new ExtendedCastGraph();

    public final Class<? super X> type;
//    private final transient X lastValue;

    public TypedPort(Class<? super X> type) {
        super();
        this.type = type;
    }

    public TypedPort(Class<? super X> type, In<? super X> o) {
        super(o);
        this.type = type;
    }

    public TypedPort(Class<? super X> type, Consumer<? super X> o) {
        super(o);
        this.type = type;
    }
}
