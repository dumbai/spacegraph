package spacegraph.space2d.widget.port;

import spacegraph.space2d.widget.textedit.TextEdit;

public abstract class EditablePort<X> extends TypedPort<X> {

    public final TextEdit edit;

    EditablePort(X initialValue, Class<? super X> type) {
        super(type);
        process(initialValue);

        edit = new TextEdit(32, 1);
        //TODO txt = new TextEdit(8, 1);
        edit.onChange.on(z -> out(parse(z.text())));
        set(edit);
    }


//
//    public final void out(X x) {
//        if (x == null)
//            return;
//
////        try {
//                super.out(next);
//            //}
////
////        } catch (Throwable t) {
////
////        }
//    }

    @Override
    public boolean out(X _next) {
        X next = process(_next);
        if (next!=null)
            return super.out(next);

        return false;
    }

    protected String toString(X next) {
        return next.toString();
    }




    /** returns true if the value is valid and can set the port, override in subclasses to filter input */
    X process(X x) {
        return x;
    }

    protected abstract X parse(String x);

}
