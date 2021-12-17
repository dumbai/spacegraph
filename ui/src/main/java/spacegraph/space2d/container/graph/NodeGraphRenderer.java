package spacegraph.space2d.container.graph;

import jcog.data.graph.AdjGraph;
import jcog.data.graph.Node;
import jcog.data.graph.path.FromTo;
import org.jetbrains.annotations.Nullable;

/**
 * layer which renders NodeGraph nodes and edges
 */
public class NodeGraphRenderer<N, E> implements Graph2D.Graph2DRenderer<Node<N, E>> {


    public static class AdjGraphRenderer<N, E> implements Graph2D.Graph2DRenderer<N> {

        public final AdjGraph<N,E> g;

        public AdjGraphRenderer(AdjGraph<N,E> graph) {
            g = graph;
        }

        @Override
        public void node(NodeVis<N> node, Graph2D.GraphEditor<N> graph) {
            N x = node.id;

            g.neighborEdges(x, (y, e)->{
                EdgeVis<N> ee = graph.edge(node, y);
                if (ee!=null) updateEdge(x, e, y, ee);
            });

            style(node);

        }

        protected void style(NodeVis<N> node) {
            node.color(0.5f, 0.5f, 0.5f);
            node.resize(20.0f, 10.0f);
        }

        protected void updateEdge(N x, E e, N y, EdgeVis<N> ee) {
            ee.weight(0.1f).color(0.5f, 0.5f, 0.5f, 0.75f);
        }
    }


    @Override
    public void node(NodeVis<Node<N, E>> from, Graph2D.GraphEditor<Node<N, E>> graph) {
        Node<N, E> F = from.id;

        from.colorHash();
        from.resize(20.0f, 10.0f);


        for (FromTo<Node<N, E>, E> e : F.edges(false, true)) {
            Node<N, E> T = e.other(F);
            EdgeVis<Node<N, E>> ee = graph.edge(from, T);
            if (ee!=null)
                edge(from, e, ee, T);
        }

    }

    protected void edge(NodeVis<Node<N, E>> from, FromTo<Node<N, E>, E> edge, @Nullable EdgeVis<Node<N, E>> edgeVis, Node<N, E> to) {
        edgeVis.weight = 0.1f;
        edgeVis.a = 0.75f;
        edgeVis.r = edgeVis.g = edgeVis.b = 0.5f;
    }

}