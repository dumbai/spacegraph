package spacegraph.space2d.widget.port;

import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.button.CheckBox;

import static javax.swing.UIManager.put;

/** TODO finish */
class EnabledPort extends Gridding {

    private final CheckBox enable = new CheckBox("");

    EnabledPort(Port p) {
        enable.set(true);
        put(0, enable.on(this::enable));
    }

    private void enable(boolean b) {

    }
}