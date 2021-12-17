package spacegraph.space2d.container;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.collection.MutableListContainer;


public class Stacking extends MutableListContainer {


    public Stacking(Surface... children) {
        super(children);
    }

    @Override
    public void doLayout(float dtS) {
        forEachWith(Surface::pos, bounds);
    }

}
