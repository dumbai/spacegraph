package spacegraph.space2d.meta;

import jcog.data.graph.MapNodeGraph;
import jcog.data.graph.Node;
import spacegraph.space2d.Surface;
import spacegraph.space2d.Surfacelike;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.ContainerSurface;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.space2d.container.graph.NodeGraphRenderer;
import spacegraph.space2d.container.layout.Force2D;

/** debugging/meta-view */
public class Inspector extends Bordering {

    MapNodeGraph<Surface,Object> graph = new MapNodeGraph();



    public Inspector(Surface s) {
        super();


        int depth = 4;
        include(s, depth);

        Graph2D graphView = new Graph2D<Node<Surface, Object>>()

                .update(new Force2D())

                .render(new NodeGraphRenderer<>() {

                })

                .set(graph.nodes());

        set(graphView.widget());
    }

    public void include(Surface s, int depth) {
        if (!graph.addNewNode(s) || depth <= 0)
            return;

        Surfacelike p = s.parent;
        if (p instanceof Surface pp) {
            include(pp, depth-1);
            graph.addEdgeIfNodesExist(pp, "->", s);
        }

        if (s instanceof ContainerSurface) {
            ((ContainerSurface)s).forEach(x -> {
                include(x, depth-1);
                graph.addEdgeIfNodesExist(s, "->", x);
            });
        }
    }

//    private class SurfaceGrapher implements Graph2D.Graph2DRenderer<Surface> {
//        @Override
//        public void node(Graph2D.NodeVis<Surface> node, Graph2D.GraphEditing<Surface> graph) {
//            node.setAt(new PushButton(node.id.toString()));
//            graph.edge()
//        }
//    }
}