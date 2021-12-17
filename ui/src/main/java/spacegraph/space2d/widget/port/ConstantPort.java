package spacegraph.space2d.widget.port;

public class ConstantPort<X> extends TypedPort<X> {

    private volatile X value;

    private ConstantPort(X value, Class<? super X> klass) {
        super(klass);
        set(value);
    }

    public ConstantPort(Class<? super X> klass) {
        super(klass);
        set((X)null);
    }

    public ConstantPort(X value) {
        this(value, (Class<? super X>) value.getClass());
    }

    void set(X value) {
        out(this.value = value);
    }


    @Override public void connected(Port other) {
        boolean outOnConnect = true;
        if (outOnConnect) {
            In oi = other.in;
            if (oi!=null)
                oi.accept(null, value);
        }
    }

    @Override
    void disconnected(Port a) {
        //TODO if no other inputs
        set((X)null);
    }

    public final void out() {
        out(value);
    }
}