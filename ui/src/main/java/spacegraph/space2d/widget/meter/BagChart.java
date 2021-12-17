package spacegraph.space2d.widget.meter;

import jcog.pri.NLink;
import jcog.pri.Prioritized;
import spacegraph.SpaceGraph;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.space2d.container.graph.NodeVis;
import spacegraph.space2d.container.layout.Force2D;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.text.VectorLabel;

import java.util.function.Consumer;

/**
 * TreeChart visualization of items in a collection
 * TODO
 */
public class BagChart<X extends Prioritized> extends Graph2D<X> {

    private final Iterable<X> input;

    public BagChart(Iterable<X> b) {
        this(b, n -> {
            Prioritized p = n.id;

            if (!(n.the() instanceof PushButton)) {
                String label = (p instanceof NLink ? ((NLink) p).id : p).toString();
                n.set(new PushButton(new VectorLabel(label)).clicked(()-> SpaceGraph.window(p, 500, 500)));
            }
        });
    }
    /** decorator should also assign pri to each node vis */
    public BagChart(Iterable<X> b, Consumer<NodeVis<X>> decorator) {
        super();
        this.input = b;
        build(decorator);

        //update(new TreeMap2D<>());
        update(new Force2D<>());

        render((n, graph) -> {
            Prioritized p = n.id;
            float pri = n.pri = p.priElseZero();// = Math.max(p.priElseZero(), 1f / (2 * bag.capacity()));
            n.color(pri, 0.25f, 0.25f);
        });
        update();
    }

    public void update() {
        set(input);
    }



    protected String label(X i, int MAX_LEN) {
        String s = i.toString();
        if (s.length() > MAX_LEN)
            s = s.substring(0, MAX_LEN);
        return s;
    }


}