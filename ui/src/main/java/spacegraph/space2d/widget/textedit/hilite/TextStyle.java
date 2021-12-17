package spacegraph.space2d.widget.textedit.hilite;

import java.awt.*;
import java.util.Objects;

/** TODO */
public class TextStyle {

    //TODO change these to Color4f, etc..
    private final Color fg;
    private final Color bg;
    private final Font font;

    public TextStyle(Color fg, Color bg, Font font) {
        this.fg = fg;
        this.bg = bg;
        this.font = font;
    }

    @Override
    public String toString() {
        return "[LinePartLayout: " + fg + ", " + bg + ", " + font + ']';
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TextStyle &&
                Objects.equals(fg, ((TextStyle) o).fg) &&
                Objects.equals(bg, ((TextStyle) o).bg) &&
                Objects.equals(font, ((TextStyle) o).font);
    }

}
