package spacegraph.space2d.widget.port.util;

import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.space2d.widget.text.Labelling;

import java.util.List;
import java.util.function.Function;

/** holds a pair of function lists */
class PortAdapter<X,Y> extends Widget {

    private final List<Function/*<X,Y>*/> fxy;
    private final List<Function/*<Y,X>*/> fyx;

    private final Class<? super X> xClass;
    private final Class<? super Y> yClass;
    private final TypedPort<X> x; //HACK
    private final TypedPort<Y> y; //HACK

    /** current enabled strategy (selection index) */
    private volatile int whichXY = -1;
    private volatile int whichYX = -1;

    PortAdapter( Class<? super X> xClass, List<Function/*<X,Y>*/> fxy, Class<? super Y> yClass, List<Function/*<Y,X>*/> fyx) {
        super();

        this.xClass = xClass; this.yClass = yClass;

        TypedPort<X> x; TypedPort<Y> y;
        Gridding g = new Gridding(
            Labelling.the(xClass.getName(), x = new TypedPort<>(xClass)),
            Labelling.the(yClass.getName(), y = new TypedPort<>(yClass))
        );
        this.x = x; this.y = y;

        if (!fxy.isEmpty()) {
            whichXY = 0;
            this.fxy = fxy;
            x.on(xx->out(xx, true));
        } else { this.fxy = null; }
        if (!fyx.isEmpty()) {
            whichYX = 0;
            this.fyx = fyx;
            y.on(yy->out(yy, false));
        } else this.fyx = null;

        set(g);
    }

//    @Override
//    protected boolean canRender(ReSurface r) {
//        if ((x!=null && !x.active()) || (y!=null && !y.active())) {
//            //done
//            parentOrSelf(Windo.class).delete();
//            //remove();
//            return false;
//        }
//        return super.canRender(r);
//    }

    final boolean out(Object o, boolean sender) {
        TypedPort src = port(!sender);
        return src.out((sender? fxy : fyx).get(sender? whichXY : whichYX).apply(o));
    }

    public TypedPort<?> port(boolean xOrY) {
        return (xOrY ? this.x : this.y);
    }

//    @Override
//    protected Object transfer(Surface sender, Object x) {
//        if (sender == a && whichAB >= 0) {
//            x = (abAdapters.get(whichAB)).apply(x);
//        } else if (sender == b && whichBA >= 0) {
//            x = (baAdapters.get(whichBA)).apply(x);
//        }
//        return x;
//    }
}