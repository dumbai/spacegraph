package spacegraph.test;

import jcog.data.graph.MapNodeGraph;
import jcog.data.graph.Node;
import jcog.reflect.ExtendedCastGraph;
import org.ujmp.core.Matrix;
import org.ujmp.core.util.matrices.SystemEnvironmentMatrix;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.space2d.container.graph.NodeGraphRenderer;
import spacegraph.space2d.container.layout.Force2D;
import spacegraph.space2d.container.layout.SemiForce2D;

import java.util.Map;
import java.util.function.Function;

import static spacegraph.SpaceGraph.window;

public enum Graph2DTest {;


    static final MapNodeGraph<Object,Object> h = new MapNodeGraph();
    static {
        h.addNode(("x"));
        h.addNode(("y"));
        h.addNode(("z"));
        h.addNode(("w"));

        h.addEdgeIfNodesExist(("x"), ("xy"), ("y"));
        h.addEdgeIfNodesExist(("x"), ("xz"), ("z"));
        h.addEdgeIfNodesExist(("y"), ("yz"), ("z"));
        h.addEdgeIfNodesExist(("w"), ("wy"), ("y"));
    }

    enum Graph2DTest1 {;
		public static void main(String[] args) { window(newSimpleGraph(), 800, 800); }
    }
    enum Ujmp1 {;
		public static void main(String[] args) {
            window(newUjmpGraph(), 800, 800);
        }
    }
    enum Tree1 {;
        public static void main(String[] args) { window(newSimpleTreeGraph(), 1600, 1100); }
    }
    enum Type1 {;
        public static void main(String[] args) {
            window(newTypeGraph(), 1600, 1100);
        }
    }

    public static Surface newTypeGraph() {
        return new Graph2D<Node<Class, Function>>()
                //.update(new Force2D<>())
                .update(new SemiForce2D.TreeForce2D())
                .render(new NodeGraphRenderer<>())
                .set(new ExtendedCastGraph()).widget();
    }

    public static Surface newSimpleTreeGraph() {
        MapNodeGraph<Object,Object> h = new MapNodeGraph();
        h.addNode(("x"));
        h.addNode(("y"));
        h.addNode(("z"));
        h.addNode(("w"));
        h.addNode(("a"));
        h.addNode(("b"));
        h.addNode(("c"));
        h.addEdgeIfNodesExist(("x"), ("xy"), ("y"));
        h.addEdgeIfNodesExist(("x"), ("xb"), ("b"));
//        h.addEdgeIfNodesExist(("y"), ("yz"), ("z"));
        h.addEdgeIfNodesExist(("w"), ("wy"), ("y"));
        h.addEdgeIfNodesExist(("z"), ("za"), ("a"));
        h.addEdgeIfNodesExist(("z"), ("zb"), ("b"));
        h.addEdgeIfNodesExist(("z"), ("zc"), ("c"));


        return new Graph2D<Node<Object, Object>>()
                .update(new SemiForce2D.TreeForce2D<>())
                .render(new NodeGraphRenderer<>())
                .set(h).widget();

    }

    public static Graph2D<Node<Object, Object>> newSimpleGraph() {
        return new Graph2D<Node<Object, Object>>()
                .update(new Force2D<>())
                .render(new NodeGraphRenderer<>())
                .set(h);
    }
    public static Surface newUjmpGraph() {
        MapNodeGraph<Object,Object> h = new MapNodeGraph<>();

        SystemEnvironmentMatrix env = Matrix.Factory.systemEnvironment();
        h.addNode("env");
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            h.addNode(v);
            h.addEdgeIfNodesExist("env", k, v);
        }


        return new Graph2D<>()
                .update(new Force2D())
                .render(new NodeGraphRenderer())
                .set(h).widget();
    }

}
