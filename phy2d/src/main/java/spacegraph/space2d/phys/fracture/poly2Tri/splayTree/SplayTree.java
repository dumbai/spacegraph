package spacegraph.space2d.phys.fracture.poly2Tri.splayTree;

public class SplayTree {

    private BTreeNode root;
    private long size;

    public SplayTree() {
        root = null;
        size = 0;
    }

    public SplayTree(SplayTree rhs) {
        SplayTree st = SplayTree.clone(rhs);
        root = st.root;
        size = st.size;
    }

    void makeEmpty() {
        root = null;
        size = 0;
    }

    private boolean isEmpty() {
        return size == 0;
    }

    long size() {
        return size;
    }

    public BTreeNode root() {
        return root;
    }

    
    public void insert(SplayTreeItem x) {

        BTreeNode newNode = new BTreeNode();
        newNode._data = x;

        if (root == null) {
            root = newNode;
            size++;
        } else {
            Comparable keys = x.keyValue();
            while (true) { 
                root = splay(keys, root);
                Comparable rootk = root.keyValue();
                if (keys.compareTo(rootk) < 0) {
                    newNode._left = root._left;
                    newNode._right = root;
                    root._left = null;
                    root = newNode;
                    size++;
                    return;
                } else if (keys.compareTo(rootk) > 0) {
                    newNode._right = root._right;
                    newNode._left = root;
                    root._right = null;
                    root = newNode;
                    size++;
                    return;
                } else {
                    
                    
                    x.increaseKeyValue(1.0e-10);
                    keys = x.keyValue();
                }
            }
        }
    }

    
    public BTreeNode delete(Comparable keys) {

        root = splay(keys, root);

        if (!(root.keyValue()).equals(keys)) {
            return null;
        } 

        BTreeNode result = root;

        BTreeNode newTree;
        if (root._left == null)
            newTree = root._right;
        else {
            
            
            newTree = root._left;
            newTree = splay(keys, newTree);
            newTree._right = root._right;
        }

        root = newTree;
        size--;
        return result;
    }

    /**
     * Returns deleted max. BTreeNode.
     *
     * @return
     */
    public BTreeNode deleteMax() {
        if (isEmpty()) return null;

        double keys = Double.MAX_VALUE;
        root = splay(keys, root);

        BTreeNode maxResult = root;

        BTreeNode newTree;
        if (root._left == null) newTree = root._right;
        else {
            newTree = root._left;
            newTree = splay(keys, newTree);
            newTree._right = root._right;
        }
        size--;
        root = newTree;
        return maxResult;
    }

    
    private static SplayTree clone(SplayTree rhs) {
        SplayTree st = new SplayTree();
        st.root = SplayTree.clone(rhs.root);
        st.size = rhs.size;
        return st;
    }

    public BTreeNode find(Comparable keys) {
        if (isEmpty()) return null;
        root = splay(keys, root);
		return root.keyValue().equals(keys) ? root : null;
    }

    /**
     * Find the maximum node smaller than or equal to the given key.
     * This function specially designed for polygon Triangulation to
     * find the direct left edge at event vertex;
     */
    
    public BTreeNode findMaxSmallerThan(Comparable keys) {
        if (isEmpty()) return null;

        root = splay(keys, root);

        if (root.data().keyValue().compareTo(keys) < 0) return root;
        else if (root._left != null) {
            BTreeNode result = root._left;
            while (result._right != null) result = result._right;
            return result;
        } else {
            assert (false);
            return null;
        }
    }


    
    public void inOrder(SplayTreeAction action, double y) {
        inOrder(action, root, y);
    }


    
    public int height() {
        return height(root);
    }

    
    private static int height(BTreeNode t) {
        if (t == null) return 0;
        int lh = height(t._left);
        int rh = height(t._right);

        return (lh > rh) ? (++lh) : (++rh);
    }


    public static BTreeNode left(BTreeNode node) {
        return node.left();
    }

    public static BTreeNode right(BTreeNode node) {
        return node.right();
    }

    private static BTreeNode clone(BTreeNode t) {
        if (t == null)
            return null;
        
        if (t == t._left)  
            return null;

        return new BTreeNode(t._data, clone(t._left), clone(t._right));
    }

    private static void inOrder(SplayTreeAction action, BTreeNode t, double y) {
        if (t != null) {
            inOrder(action, t._left, y);
            action.action(t, y);
            inOrder(action, t._right, y);
        }
    }


    

    
    private static BTreeNode rotateWithLeftChild(BTreeNode k2) {
        BTreeNode k1 = k2._left;
        k2._left = k1._right;
        k1._right = k2;
        return k1;
    }

    
    private static BTreeNode rotateWithRightChild(BTreeNode k1) {
        BTreeNode k2 = k1._right;
        k1._right = k2._left;
        k2._left = k1;
        return k2;
    }

    private static final BTreeNode header = new BTreeNode();

    /**
     * Internal method to perform a top-down splay.
     * x is the key of target node to splay around.
     * t is the root of the subtree to splay.
     *
     * @param keys
     * @param t
     * @return
     */
    
    private static BTreeNode splay(Comparable keys, BTreeNode t) {

        header._left = header._right = null;
        BTreeNode _rightTreeMin;
        BTreeNode _leftTreeMax = _rightTreeMin = header;

        for (; ; ) {
            Comparable rKey = t.keyValue();
            if (keys.compareTo(rKey) < 0) {
                if (t._left == null) break;
                if (keys.compareTo(t._left.keyValue()) < 0) t = rotateWithLeftChild(t);
                if (t._left == null) break;

                
                _rightTreeMin._left = t;
                _rightTreeMin = t;
                t = t._left;
            } else if (keys.compareTo(rKey) > 0) {
                if (t._right == null) break;
                if (keys.compareTo(t._right.keyValue()) > 0) t = rotateWithRightChild(t);
                if (t._right == null) break;

                
                _leftTreeMax._right = t;
                _leftTreeMax = t;
                t = t._right;
            } else break;
        }

        _leftTreeMax._right = t._left;
        _rightTreeMin._left = t._right;
        t._left = header._right;
        t._right = header._left;
        return t;
    }

}
