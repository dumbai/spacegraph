package spacegraph.test;

import spacegraph.SpaceGraph;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.hud.Hover;
import spacegraph.space2d.hud.HoverModel;
import spacegraph.space2d.widget.button.AbstractButton;
import spacegraph.space2d.widget.text.BitmapLabel;

/** hover / tooltip tests */
public enum HoverTest {;

	public static void main(String[] args) {
        SpaceGraph.window(hoverTest(), 500, 500);
    }

    static Gridding hoverTest() {
        return new Gridding(
            new HoverButton("x", new HoverModel.Exact()),
            new HoverButton("y", new HoverModel.Maximum()),
            new HoverButton("z", new HoverModel.ToolTip()),
            new HoverButton("w", new HoverModel.Cursor())
        );
    }

    private static class HoverButton extends AbstractButton {

        final Hover hover;

        HoverButton(String label, HoverModel m) {
            super();
            this.hover = new Hover<>(this, this::hover, m);
        }

        protected Surface hover(HoverButton b) {
            return new BitmapLabel("yes").backgroundColor(0.9f, 0.5f, 0.0f, 0.5f);
        }

        @Override
        public Surface finger(Finger finger) {
            Surface s = super.finger(finger);
            if (s == this)
                finger.test(hover);

            return s;
        }

        @Override
        protected void onClick() {

        }
    }
}