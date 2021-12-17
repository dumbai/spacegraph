package spacegraph.space2d.widget.windo;

import jcog.event.Off;
import jcog.event.RunThese;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ContainerSurface;
import spacegraph.space2d.container.graph.GraphEdit2D;

public class DependentWindow extends Windo {


    private final Surface content;
    private Off off;
//    public transient RectFloat posNext = null;


    public DependentWindow(Surface content) {
        super(content);
        this.content = content;
    }

    @Override
    protected void starting() {
        super.starting();

        GraphEdit2D g = parentOrSelf(GraphEdit2D.class);

        this.off = new RunThese(()->{
            g.physics.remove(this);

            //remove any associated links, recursively
            if (content instanceof ContainerSurface) {
                ((ContainerSurface) content).forEachRecursively(g::removeComponent);
            } else {
                g.removeComponent(content);
            }
        });

    }

    @Override
    protected void stopping() {
        off.close();
        off = null;
        super.stopping();
    }

//    @Override
//    public void posFinger(RectFloat p) {
//        this.posNext = p;
//    }
}
