package spacegraph.space2d.phys.fracture.poly2Tri;

import spacegraph.space2d.phys.fracture.poly2Tri.splayTree.BTreeNode;
import spacegraph.space2d.phys.fracture.poly2Tri.splayTree.SplayTreeAction;

public class UpdateKey implements SplayTreeAction {

    public void action(BTreeNode node, double y) {
        ((Linebase) node.data()).setKeyValue(y);
    }

}
