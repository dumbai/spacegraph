package spacegraph.space2d.container;

import com.jogamp.opengl.GL2;
import jcog.tree.rtree.rect.RectF;
import spacegraph.SpaceGraph;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.grid.Containers;
import spacegraph.space2d.container.grid.GridModel;
import spacegraph.space2d.container.grid.GridRenderer;
import spacegraph.space2d.container.grid.KeyValueGrid;
import spacegraph.space2d.container.unit.Clipped;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.space2d.widget.textedit.TextEdit;
import spacegraph.video.Draw;

import java.util.Map;

class ScrollXYTest {


    static class ScrollGridTest1 {
        public static void main(String[] args) {

            GridModel<String> model = new GridModel<>() {

                @Override
                public String get(int x, int y) {
                    return x + "," + y;
                }

                @Override
                public int cellsX() {
                    return 64;
                }

                @Override
                public int cellsY() {
                    return 64;
                }
            };

            ScrollXY<String> grid = new ScrollXY<>(model,
                    (x, y, s) -> {
                        if (Math.random() < 0.5f) {
                            Surface p = new PushButton(new VectorLabel(s)) {
                                @Override
                                protected void paintWidget(RectF bounds, GL2 gl) {
                                    Draw.colorHash(gl, x ^ y, 0.2f, 0.3f, 0.85f);
                                    Draw.rect(bounds, gl);
                                }

                                @Override
                                public boolean tangible() {
                                    return false;
                                }
                            };
                            return new Widget(p);
                        } else {
                            return new VectorLabel(s);
                        }
                    });
            grid.scroll(0,0,8,4);


            grid.setScrollBar(true, true, false);
//            grid.setScrollBar(false, false, true);

            SpaceGraph.window(grid, 1024, 800);
        }
    }
    static class ListTest1 {
        public static void main(String[] args) {

            String[] list = {"a", "b", "c", "d", "e", "f"};

            GridRenderer<String> builder = (x, y, n) -> new CheckBox(n);

            SpaceGraph.window(ScrollXY.array(builder, list), 800, 800);
        }

    }

    static class MapTest1 {
        public static void main(String[] args) {
            //                debugScroll(
            //new VectorLabel(n.toString())
            //new CheckBox(n.toString())
            SpaceGraph.window(new ScrollXY(
                            new KeyValueGrid(
                                Map.of("wtf", "ok", "sdfj", "xcv", "sdf", "fdfs")
                            ),
                            (x, y, n)->
                                x == 0 ?
                                    new BitmapLabel(n.toString())
                                    //new VectorLabel(n.toString())
                                    //new CheckBox(n.toString())
                                    : new CheckBox(n.toString())
                ), 800, 800);
        }


    }

    private static Object debugScroll(ScrollXY.ScrolledXY x) {

        ScrollXY<ScrollXY.ScrolledXY> s = new ScrollXY<>(x);
        Surface debug = new TextEdit("x");
        return Containers.row(new Clipped(s), 0.75f, debug);
    }
}