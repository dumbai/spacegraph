package spacegraph.space2d.container;

import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.widget.text.AbstractLabel;
import spacegraph.space2d.widget.text.VectorLabel;

public class LogContainer extends RingContainer<AbstractLabel> {

    public LogContainer(int len) {
        super(new AbstractLabel[len]);
    }

    @Override
    protected void reallocate(AbstractLabel[] x) {
        for (int i = 0; i < x.length; i++) {
            AbstractLabel r = new VectorLabel();
            r.pos(RectF.Unit);
            r.start(this);
            x[i] = r;
        }
    }

    public void append(String s) {
        next(v->v.text(s));
    }

}
