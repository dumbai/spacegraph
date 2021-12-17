package spacegraph.space2d.phys.fracture.poly2Tri;

import spacegraph.space2d.phys.fracture.poly2Tri.splayTree.SplayTreeItem;

/**
 * base class for polygon boundary
 * Linebase class is a directed line segment with start/end point
 */
public class Linebase implements SplayTreeItem {

    /**
     * Was unsigned int!
     * id of a line segment;
     */
    private int _id = -1;

    /**
     * two end points;
     */
    private final Pointbase[] _endp = {null, null};

    /**
     * type of a line segement, input/insert
     * Type...
     */
    private int _type = Poly2TriUtils.UNKNOWN;

    /**
     * key of a line segment for splay tree searching
     */
    private double _key = 0;

    /**
     * Was unsigned int!
     * helper of a line segemnt
     */
    private int _helper = -1;

    public Linebase() {
        for (int i = 0; i < 2; i++) _endp[i] = null;
        _id = 0;
    }

    public Linebase(Pointbase ep1, Pointbase ep2, int iType) {
        _endp[0] = ep1;
        _endp[1] = ep2;
        _id = (Poly2TriUtils.l_id.incrementAndGet());
        _type = iType;
    }

//    public Linebase(Linebase line) {
//        this._id = line._id;
//        this._endp[0] = line._endp[0];
//        this._endp[1] = line._endp[1];
//        this._key = line._key;
//        this._helper = line._helper;
//    }

    public int id() {
        return _id;
    }

    public Pointbase endPoint(int i) {
        return _endp[i];
    }

    public int type() {
        return _type;
    }

    public Comparable keyValue() {
        return _key;
    }

    public void setKeyValue(double y) {
		_key = _endp[1].y == _endp[0].y ? Math.min(_endp[0].x, _endp[1].x) : (y - _endp[0].y) * (_endp[1].x - _endp[0].x) / (_endp[1].y - _endp[0].y) + _endp[0].x;
    }

    /**
     * reverse a directed line segment; reversable only for inserted diagonals
     */
    public void reverse() {
        assert (_type == Poly2TriUtils.INSERT);
        Pointbase tmp = _endp[0];
        _endp[0] = _endp[1];
        _endp[1] = tmp;
    }


    /**
     * set and return helper of a directed line segment
     */
    public void setHelper(int i) {
        _helper = i;
    }

    public int helper() {
        return _helper;
    }

    public String toString() {
        String sb = "Linebase(" +
                "ID = " + _id +
                ", " + Poly2TriUtils.typeToString(_type) +
                ", [" +
                _endp[0] +
                ", " +
                _endp[1] +
                "], type = " + _type +
                ", keyValue =" + keyValue();
        return sb;
    }

    /**
     * slightly increased the key to avoid duplicated key for searching tree.
     */
    public void increaseKeyValue(double delta) {
        _key += delta;
    }

}
