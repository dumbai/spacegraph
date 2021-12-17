package spacegraph;

import jcog.Is;
import jcog.thing.Thing;
import spacegraph.layer.AbstractLayer;
import spacegraph.layer.OrthoSurfaceGraph;
import spacegraph.layer.WindowControlLayer;
import spacegraph.space2d.Surface;
import spacegraph.space2d.hud.Zoomed;
import spacegraph.space2d.meta.ObjectSurface;
import spacegraph.video.JoglWindow;


@Is("Direct_manipulation_interface")
public class SpaceGraph extends Thing<SpaceGraph, Object> {

//    public static void main(String[] args) throws Exception {
//        //https://www.infoq.com/articles/jshell-java-repl
//        /*
//        You can utilize this directory to store any startup or initialization code. This is a feature that's directly supported by JShell with the --startup parameter.
//
//$ jshell --startup startups/custom-startup
//         */
//        JavaShellToolBuilder.builder()
//                .start(
//                //        "--classpath=\"*\""
//                );
//
//    }


    /**
     * generic window creation entry point
     */
    public static AbstractLayer window(Object o, int w, int h) {
        if (o instanceof AbstractLayer s) {
            s.window.showInit(w, h);
            return s;
        } /*else if (o instanceof Spatial) {
            SpaceGraph3D win = new SpaceGraph3D(((Spatial) o));
            win.video.showInit(w, h);
            return win;
        } */else if (o instanceof Surface) {
            return window((Surface) o, w, h);
        } else {
            return window(new ObjectSurface(o, 2), w, h);
        }
    }

    /**
     * creates window with 2d with single surface layer, maximized to the size of the window
     */
    public static OrthoSurfaceGraph window(Surface s, int w, int h) {
        JoglWindow j = new JoglWindow(w, h);
        new WindowControlLayer(j);
        OrthoSurfaceGraph g = new OrthoSurfaceGraph(
                new Zoomed(s),
                j);
        return g;
    }
}