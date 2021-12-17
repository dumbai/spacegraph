package spacegraph.space2d.meta;

import jcog.Util;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ScrollXY;
import spacegraph.space2d.container.grid.GridModel;
import spacegraph.space2d.container.grid.GridRenderer;
import spacegraph.space2d.widget.text.VectorLabel;

import java.util.Collection;
import java.util.List;

import static spacegraph.SpaceGraph.window;

public abstract class ScrollGrid extends ScrollXY<Surface> implements GridModel, GridRenderer {

    public ScrollGrid() {
        super(null, null);
        setScrollBar(true, true, false);
    }

    public ScrollGrid(int width, int height) {
        this();
        view(width,height);
    }

    public static void main(String[] args) {

        //List.of("a", "b", "c", "d")
//        ScrollGrid grid = new ScrollGrid(3, 2) {
//
//            @Override
//            public Surface apply(int x, int y, Object s) {
//                return new VectorLabel(s.toString());
//            }
//
//            @Override
//            public String get(int x, int y) {
//                return x + "," + y;
//            }
//
//            @Override
//            public int cellsX() {
//                return 64;
//            }
//
//            @Override
//            public int cellsY() {
//                return 64;
//            }
//        };
        //window(grid, 500, 400);
        window(ScrollGrid.fromList(List.of(new VectorLabel("a"), new VectorLabel("b"), new VectorLabel("c"))), 500, 400);

//        grid.scroll(0,0,8,4);


    }

    public static ScrollGrid fromList(Collection<Surface> abc) {

        var g = new ScrollGrid() {

            private int cw, ch;
            Surface[] surfaces;

            public void update(Collection<Surface> abc) {
                this.surfaces = abc.toArray(Surface.EmptySurfaceArray);
                int n = surfaces.length;

                //share this computation with how Gridding does it
                int w, h;
                w = Math.max(1, (int) Math.ceil(Util.sqrt(n)));
                h = Math.max(1, (int) Math.ceil(((float) n) / w));
                cw = w;
                ch = h;
                viewMax(w, h);
                view(w, h);
                layout();
            }

            @Nullable
            @Override
            public Surface get(int x, int y) {
                int i = (y * cw) + x;
                return i < surfaces.length ? surfaces[i] : missing();
            }

            @Nullable
            private Surface missing() {
                return null;
            }

            @Override
            public Surface apply(int x, int y, Object value) {
                return (Surface) value;
            }

            @Override
            public int cellsX() {
                return cw;
            }

            @Override
            public int cellsY() {
                return ch;
            }


        };
        g.update(abc);
        return g;
    }
}