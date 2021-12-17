package spacegraph.space2d.widget.textedit;

import jcog.math.v2;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ScrollXY;
import spacegraph.space2d.widget.textedit.buffer.Buffer;

public abstract class TextEditModel extends Surface implements ScrollXY.ScrolledXY {


    /** current buffer */
    Buffer buffer;

    public final TextEditView view = new TextEditView(  /* HACK*/);


    TextEditModel() {
        this(new Buffer(""));
    }

//    @Override
//    protected void render(ReSurface r) {
//
//    }

    @Override
    protected void stopping() {
        view.delete();
        super.stopping();
    }

    private TextEditModel(Buffer buf) {
        setBuffer(buf);
    }

    /** TODO synchronization and managed disposal of existing buffer and its resources */
    private/*synchronized*/ void setBuffer(Buffer buf) {
        buffer = buf;
    }


    @Override
    public void update(ScrollXY s) {

        //calculate min,max scales, with appropriate aspect ratio restrictions

        int w = Math.max(1, Math.min(buffer.width(), 80));
        int h = Math.max(1, Math.min(buffer.height(), 20));
        s.viewMinMax(new v2(1, 1), new v2(w, h));

        s.scroll(0, 0, w, h);
    }




    public final Buffer buffer() {
        return this.buffer;
    }


    public void createNewBuffer() {
        setBuffer(new Buffer(""));
    }


//    public void viewCursor() {
//        if (!visible(cursor.cursor.col, cursor.cursor.col)) {
//
//        }
//    }
//
//    private boolean visible(int col, int col1) {
//        return this.
//    }

}