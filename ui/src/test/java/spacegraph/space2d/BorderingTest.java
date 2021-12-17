package spacegraph.space2d;

import spacegraph.SpaceGraph;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.widget.button.PushButton;

public class BorderingTest {

    public static void main(String[] args) {
        SpaceGraph.window(new Bordering()

                .south(PushButton.iconAwesome("times"))
                .east(PushButton.iconAwesome("times"))
                .west(PushButton.iconAwesome("times")), 800, 800);
    }
}