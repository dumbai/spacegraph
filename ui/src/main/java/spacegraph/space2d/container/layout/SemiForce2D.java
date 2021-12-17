package spacegraph.space2d.container.layout;

import jcog.data.graph.MinTree;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.container.graph.NodeVis;
import spacegraph.util.MutableRectFloat;

/** partially enforces an explicit structure while applying some degree of Force2D constraints */
public abstract class SemiForce2D<X> extends Force2D<X> {

    @Override
    protected abstract void post();

    public static class TreeForce2D<X> extends SemiForce2D<X> {

        int j = -1;

        @Override
        protected synchronized void post() {


            MinTree.Graph g = new MinTree.Graph(graph.nodes());
            j = 0;
            for (MutableRectFloat<X> s : nn) {
                if (s == null) break;
                s.node.i = j++;
            }
            for (MutableRectFloat<X> s : nn) {
                if (s == null) break;
                NodeVis<X> S = s.node;
                S.outs.forEach((t, e) -> g.edge(S.i, e.to.i, 1));
            }

            float nodeSpacing = 1 + this.nodeSpacing.floatValue();

//            float sx = 50, sy = 50;
            g.apply().forEach(p -> apply(p, nodeSpacing, nodeSpacing));

//            System.out.println(tree);
        }

        /** recursive */
        private void apply(MinTree.Graph.IntTree p, float nodeSpacingX, float nodeSpacingY) {
            int size = p.size(); if (size==0) return;

            MutableRectFloat<X> P = nn[p.id];

            MinTree.Graph.@Nullable IntTree[] children = p.children;
            if (children!=null) {

                double r = P.radius();


                int n = children.length;
                double sx = nodeSpacingX * r * Math.sqrt(n);
                double sy = nodeSpacingY * r;

                int cn = 0;
                float pushParentX = 0, pushParentY = 0;
                for (MinTree.Graph.IntTree c : children) {
                    double xi = P.x + sx * (size == 1 ? 0 : size * (((float) cn) / (size - 1) - 0.5f) * 1);
                    double yi = P.y + sy;

                    MutableRectFloat<X> C = nn[c.id];
                    double xd = xi - C.x, yd = yi - C.y;

                    pushParentX += -xd/2/n; pushParentY += -yd/2/n;

                    C.move(xd/2, yd/2);

                    //C.posLERP(P.x + xi, P.y + yi, 0.25f);
                    apply(c, nodeSpacingX, nodeSpacingY);
                    cn++;
                }

                P.move(pushParentX, pushParentY);
            }
        }


    }
}