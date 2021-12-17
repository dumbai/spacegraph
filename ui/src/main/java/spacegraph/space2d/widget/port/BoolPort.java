package spacegraph.space2d.widget.port;


import org.eclipse.collections.api.block.procedure.primitive.BooleanProcedure;

public class BoolPort extends TypedPort<Boolean> {

    public BoolPort() {
        super(Boolean.class);
    }

    public BoolPort(BooleanProcedure b) {
        this();
        on(b::value);
    }
}
