package spacegraph.space2d.phys.fracture.poly2Tri.splayTree;

public interface SplayTreeItem {

    Comparable keyValue();

    void increaseKeyValue(double delta);
}
