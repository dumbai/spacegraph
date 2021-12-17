package spacegraph.space2d.widget.menu.view;

import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ContainerSurface;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.container.unit.Clipped;
import spacegraph.space2d.widget.menu.Menu;

/** TODO */
public class WallMenuView extends Menu.MenuView {

    private final GraphEdit2D wall;

    public WallMenuView() {
        super();
//            setContent(new GraphEdit());
//            setWrapper(x -> new Windo(new MetaFrame(x)).size(w()/2, h()/2));

        this.wall = new GraphEdit2D() {
//            @Override
//            public void doLayout(int dtMS) {
//                super.doLayout(dtMS);
//                //TODO move windows which are now outside of the view into view
//            }
        };
    }

    @Override
    public Surface view() {
        return new Clipped(wall);
    }

    @Override
    public void active(Surface surface) {
        ContainerSurface w = wall.add(surface);
        //TODO
        RectF r = RectF.XYXY(10, 10, 400, 300);
        w.pos(r);
    }

    @Override
    public boolean inactive(Surface surface) {
        /* HACK */
        return wall.remove(((Surface)surface.parent).parent)!=null;
    }

    @Override
    public boolean isEmpty() {
        return wall.childrenCount()==0;
    }
}