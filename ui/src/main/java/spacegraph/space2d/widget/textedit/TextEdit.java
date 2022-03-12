package spacegraph.space2d.widget.textedit;

import com.jogamp.newt.event.KeyEvent;
import jcog.event.ListTopic;
import jcog.event.Topic;
import jcog.math.v2;
import jcog.math.v3;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.input.key.KeyPressed;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ScrollXY;
import spacegraph.space2d.widget.textedit.buffer.Buffer;
import spacegraph.space2d.widget.textedit.buffer.BufferLine;
import spacegraph.space2d.widget.textedit.keybind.TextEditKeys;
import spacegraph.space2d.widget.textedit.view.CharView;
import spacegraph.space2d.widget.textedit.view.LineView;
import spacegraph.util.math.Color4f;
import spacegraph.video.Draw;

import java.util.function.Consumer;


public class TextEdit extends ScrollXY<TextEditModel> implements KeyPressed {

    //TODO public final AtomicBoolean editable = new AtomicBoolean(true);

    final Color4f backgroundColor = new Color4f(0,0,0,0);
    static final float charAspect = 1.618f;

    public final Topic<TextEdit> onChange = new ListTopic<>();

    private final TextEditText text;

    public final Topic<KeyEvent> keyPress = new ListTopic<>();
    public final TextEditKeys keys;

    public TextEdit(int cols) {
        this(cols, 1, -1, -1);
    }

    public TextEdit(int cols, int rows /*boolean editable*/) {
        this(cols, rows, -1, -1);
    }

    private TextEdit(int cols, int rows, int colsMax, int rowsMax /*boolean editable*/) {
        this();
        viewMax(new v2(Math.max(cols,colsMax), Math.max(rows, rowsMax)));
        view(cols, rows, charAspect);
    }

    private TextEdit() {
        super();

        this.keys = TextEditActions.DEFAULT_KEYS;

        scrollable(this.text = new TextEditText());

        viewMinMax(new v2(1,1), new v2(1, 1));
    }

    public TextEdit(String initialText) {
        this();

        text(initialText);
        viewAll();
    }
//
//    public Appendable appendable() {
//        return new Appendable() {
//            @Override
//            public Appendable append(CharSequence charSequence) {
//                insert(charSequence.toString());
//                return this;
//            }
//
//            @Override
//            public Appendable append(CharSequence charSequence, int a, int b) {
//                insert(charSequence.subSequence(a, b).toString());
//                return this;
//            }
//
//            @Override
//            public Appendable append(char c) {
//                insert(String.valueOf(c));
//                return this;
//            }
//        };
//    }

//    @Deprecated public static Appendable out() {
//        TextEdit e = new TextEdit();
//        return new AppendableUnitContainer<>(e) {
//
//            @Override
//            public AppendableUnitContainer append(CharSequence charSequence) {
//                e.insert(charSequence.toString());
//                return this;
//            }
//
//            @Override
//            public AppendableUnitContainer append(CharSequence charSequence, int a, int b) {
//                e.insert(charSequence.subSequence(a, b).toString());
//                return this;
//            }
//
//            @Override
//            public AppendableUnitContainer append(char c) {
//                e.insert(String.valueOf(c));
//                return this;
//            }
//        };
//    }

    public TextEdit insert(String text) {
        buffer().insert(text);
        //viewAll();
        return this;
    }

    private Buffer buffer() {
        return text.buffer;
    }

    public TextEdit clear() {
        buffer().clear();
        cursor().set(0,0);
        return this;
    }

    public TextEdit text(String text) {
        buffer().text(text);
        return this;
    }

    public final void viewAll() {
        viewAll(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /** TODO add reasonable limits (too many lines to display etc) */
    public final synchronized void viewAll(int maxX, int maxY) {
        Buffer b = buffer();
        viewMax(new v2(1+b.width(), b.height()));
        view(Math.min(viewMax.x, maxX), Math.min(viewMax.y, maxY), charAspect);
    }

    public String text() {
        return buffer().text();
    }

    public TextEdit onChange(Consumer<TextEdit> e) {
        onChange.on(e);
        return this;
    }

    public TextEdit onKeyPress(Consumer<KeyEvent> e) {
        keyPress.on(e);
        return this;
    }

    @Override
    public Surface finger(Finger f) {
        Surface s = super.finger(f);
        if (s == null && f.intersects(bounds)) {
            if (f.pressedNow(0)) {
                focus();
            }
            return this;
        }
        return s;
    }


    //    private abstract static class AppendableUnitContainer<S extends Surface> extends UnitContainer<S> implements Appendable {
//
//        public AppendableUnitContainer(S x) {
//            super(x);
//        }
//
//    }

    public v3 cursor() {
        return text.view.cursor.position;
    }

    public final TextEdit background(float r, float g, float b, float a) {
        backgroundColor.set(r, g, b, a);
        return this;
    }


    private class TextEditText extends TextEditModel {
        @Override
        protected void render(ReSurface r) {
            @Nullable RectF v = view();
            LineView[] ll = view.lines;

            view(v = text.view.updateCursor(buffer.cursor, v));

            float vx = v.x, vy = v.y, vw = v.w, vh = v.h;

            int x1 = (int)Math.max(0, vx);
            int y1 = (int)Math.max(0, vy);
            int x2 = (int)(x1+vw);
            int y2 = (int)(y1+vh);
            float ox = x1-vx;
            float oy = -(y1-vy);
            if (view.realloc(y2-y1, x2-x1) || text.buffer.changed.compareAndSet(true, false)) {

                for (int dy = y1; dy < y2; dy++) {
                    int visibleRow = dy - y1;
                    BufferLine l = buffer.lines.get(dy);
                    if (l!=null) {
                        text.view.lines[visibleRow].update(l, x1, x2);
                        l.updatePosition(visibleRow);
                    }
                }

                viewMax(new v2(Math.max(viewMax.x, buffer.width()), Math.max(viewMax.y, buffer.height())));
                onChange.accept(TextEdit.this);
            }

            if (backgroundColor.hasOpacity()) {
                backgroundColor.apply(r.gl);
                Draw.rect(content.bounds, r.gl);
            }

            Draw.bounds(content.bounds, r.gl, gg -> Draw.stencilMask(gg, true, Draw::rectUnit, g -> {
                float charsWide = x2-x1;//v.w;
                float charsHigh = y2-y1;////v.h;

                float w = CharView.width();

                g.glPushMatrix();
                g.glTranslatef(0, 1.0f - (0.5f / charsHigh), 0);
                g.glScalef(1.0f / charsWide, 1.0f / charsHigh, 1.0f);

                int Y1 = Math.min(ll.length, y1);
                int Y2 = Math.min(ll.length, y2);

                for (int y = Y1; y < Y2; y++) {
                    LineView line = ll[y];
                    if (line != null && line.chars!=null) {

                        g.glPushMatrix();

                        line.color.apply(g);

                        //TODO line height, margin etc
                        CharView[] cc = line.chars;
                        if (cc !=null) {

                            float Y = oy + (Y1 - y);
                            int X2 = Math.min(cc.length, x2);
                            for (int x = x1; x < X2; x++) {
                                CharView c = cc[x];
                                if (c != null)
                                    c.position(ox + w * (x - x1), Y).draw(g);

                            }
                        }

                        g.glPopMatrix();
                    }
                }

                //g.glTranslatef(line.position.x - x1 + ox, (float) (y1 - y), line.position.z);
                //c.position.set(cursor.cl line.position.x - x1 + ox + width + (x - x1), (y1-y) + 0, 0);

                view.cursor.position.set(ox + w * (view.cursor.cursor.col-x1), oy + (Y1 - view.cursor.cursor.row), 0);

                view.cursor.color.a((float)(Math.sin((r.frameNS) *1.0E-9*(Math.PI*2)*2)/2+0.5)*0.5f + 0.1f);
                view.cursor.draw(g);

                g.glPopMatrix();
            }));
        }

    }


    @Override
    public final boolean key(KeyEvent e, boolean pressedOrReleased) {
        //TODO anything from super.key(..) ?
        boolean b = keys.key(e, pressedOrReleased, text);
        if (b) {
            if (pressedOrReleased)
                keyPress.accept(e);
            return true;
        }
        return false;
    }

}