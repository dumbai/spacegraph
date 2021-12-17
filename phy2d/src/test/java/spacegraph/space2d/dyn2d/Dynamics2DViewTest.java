package spacegraph.space2d.dyn2d;

import com.jogamp.opengl.GL2;
import jcog.math.v2;
import spacegraph.SpaceGraph;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.dyn2d.jbox2d.BlobTest4;
import spacegraph.space2d.phys.Dynamics2DView;
import spacegraph.space2d.phys.dynamics.Dynamics2D;

class Dynamics2DViewTest {

    public static void main(String[] args) {
        Dynamics2D w = new Dynamics2D();
        w.setGravity(new v2(0, -0.9f));
//        w.setContinuousPhysics(true);
        w.setWarmStarting(true);
//        w.setSubStepping(true);

        //new CarTest().accept(w);
//        new TheoJansenTest().accept(w);
        new BlobTest4().accept(w);
//        new ChainTest().accept(w);

        SpaceGraph.window(new Dynamics2DView(w) {
            @Override
            protected void paint(GL2 gl, ReSurface reSurface) {
                w.step(0.04f, 8, 4);
                super.paint(gl, reSurface);
            }
        }, 800, 800);


    }
}