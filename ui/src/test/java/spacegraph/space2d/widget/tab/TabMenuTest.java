package spacegraph.space2d.widget.tab;

import spacegraph.SpaceGraph;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.menu.TabMenu;
import spacegraph.space2d.widget.sketch.Sketch2DBitmap;

import java.util.Map;

class TabMenuTest {
    public static class TestDefault {
        public static void main(String[] args) {
            SpaceGraph.window(new TabMenu(Map.of(
                    "a", () -> new Sketch2DBitmap(40, 40),
                    "b", () -> new PushButton("x"))), 800, 800);
        }
    }
//    public static class TestAlternateContentMode {
//        public static void main(String[] args) {
//            SpaceGraph.window(new TabPane.TabWall().addToggles(Map.of(
//                    "a", () -> new Sketch2DBitmap(40, 40),
//                    "b", () -> new PushButton("x")))
//
//                    , 800, 800);
//        }
//    }
}