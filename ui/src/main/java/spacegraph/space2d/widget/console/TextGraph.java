package spacegraph.space2d.widget.console;

import spacegraph.SpaceGraph;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.space2d.meta.ClassReloadingSurface;
import spacegraph.space2d.meta.MetaFrame;
import spacegraph.space2d.widget.text.VectorLabel;

import java.util.List;

/** experimental text editor based on character-level graphical representation
 *  see: https://github.com/koo5/new_shit lemon
 */
public class TextGraph extends Graph2D { //extends Graph2D {

    public TextGraph() {
        super();
        render((node, graph) -> node.set(new VectorLabel("x")));
        update((g,dt)->{
           //System.out.println(g.nodes());
        });
        add(List.of("x"));
    }


    public static void main(String[] args)  {

        SpaceGraph.window(new MetaFrame(new ClassReloadingSurface<>(TextGraph.class)), 800, 800);

//        Loop.of(c::reload).setFPS(0.25f);

//        Class c0 = TextGraph.class;
//
//        ClassReloader r = ClassReloader.inClassPathOf(TextGraph.class); //"/home/me/n/ui/out/production/classes/");
//        //Class<?> c1 = r.loadClassAsReloadable(TextGraph.class);
//        Class<?> c1 = r.reloadClass(TextGraph.class);
//        System.out.println(c1);
//        System.in.read();
//        Class<?> c2 = r.reloadClass(TextGraph.class);
//        System.out.println(c2);
    }
}