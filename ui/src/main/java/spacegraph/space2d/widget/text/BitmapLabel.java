package spacegraph.space2d.widget.text;


import jcog.Str;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.container.unit.AspectAlign;
import spacegraph.space2d.widget.console.BitmapTextGrid;

import static spacegraph.space2d.widget.text.VectorLabel.MIN_PIXELS_TO_BE_VISIBLE;

public class BitmapLabel extends AbstractLabel {

    private BitmapTextGrid view;
//    static final float minPixelsToBeVisible = 7;

    private RectF textBounds;

    @Deprecated public boolean mipmap = true, antialias = true;

    public BitmapLabel() {
        this("");
    }

    public BitmapLabel(String text, int maxLen) {
        this(text.length() > maxLen ? text.substring(0, maxLen) : text);
    }

    public BitmapLabel(String text) {
        super();
        this.textBounds = bounds;
        text(text);
    }

    @Override
    protected void starting() {
        assert(view==null);
        view = new MyBitmapTextGrid(mipmap, antialias);
        view.start(this);
        //text(this.text);
        textChanged(); //force
        super.starting();
    }
    @Override
    protected void stopping() {
        super.stopping();
        view.delete();
        view = null;
    }

    @Override
    protected void textChanged() {
        BitmapTextGrid v = view;
        if (v!=null) {
            int rows = 1 + Str.countRows(text, '\n');
//            boolean resized;
            //HACK do better
            //int cols = Arrays.stream(next.split("\n")).mapToInt(String::length).max().getAsInt();
            if (rows == 1)
                v.resize(text.length(), 1);
            else
                v.resize(Str.countCols(text), rows);

//            v.invalidate();
        }
    }

    @Override
    protected void doLayout(float dtS) {
        RectF b = bounds;
        int r = view.rows; if (r > 0) {
            int c = view.cols; if (c > 0) {
                b = AspectAlign.innerBounds(b,
                (r * characterAspectRatio) / c, align);
            }
        }
        textBounds = b;
        view.pos(bounds);
    }

    private String textShown;

    @Override
    protected final void renderContent(ReSurface r) {
        float p = r.visP(textBounds, MIN_PIXELS_TO_BE_VISIBLE);
        String textShownBefore = textShown;
        if (p <= 0) {
            //TODO re-use existing result
            textShown = String.valueOf(text.charAt(0));
        } else {
            textShown = text;
        }

        if (textShown != textShownBefore)
            doLayout(0);

        this.view.renderIfVisible(r);
    }


    public BitmapLabel alpha(float a) {
        this.view.alpha(a);
        return this;
    }


    public AbstractLabel textColor(float rr, float gg, float bb, float aa) {
        fgColor.set((rr), (gg), (bb), 1.0f);
        return this;
    }

    public final AbstractLabel textColor(float rr, float gg, float bb) {
        return textColor(rr, gg, bb, 1.0f);
    }

    public AbstractLabel backgroundColor(float rr, float gg, float bb, float aa) {
        bgColor.set((rr), (gg), (bb), aa);
        return this;
    }

    public final AbstractLabel backgroundColor(float rr, float gg, float bb) {
        return backgroundColor(rr, gg, bb, 1.0f);
    }


    private final class MyBitmapTextGrid extends BitmapTextGrid {

        MyBitmapTextGrid(boolean mipmap, boolean antialias) {
            super(mipmap, antialias);
            cursorCol = cursorRow = -1; //hidden
        }

        @Override
        protected RectF textBounds() {
            return textBounds;
        }

        @Override
        protected boolean renderText(ReSurface r) {

            clearBackground(); //may not be necessary if only one line and all characters are used but in multiline the matrix currently isnt regular so some chars will not be redrawn

            String s = text(r);
            int n = s.length();
            int row = 0, col =0;
            for (int i = 0; i < n; i++) {
                char c = s.charAt(i);
                if (c == '\n') {
                    row++;
                    col = 0;
                } else {
                    redraw(c, col++, row, BitmapLabel.this.fgColor, BitmapLabel.this.bgColor);
                }
            }
            return true;
        }


    }

    protected String text(ReSurface r) {
        return BitmapLabel.this.textShown;
    }
}