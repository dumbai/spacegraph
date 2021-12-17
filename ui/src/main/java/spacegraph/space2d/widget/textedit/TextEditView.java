package spacegraph.space2d.widget.textedit;

import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.widget.textedit.buffer.CursorPosition;
import spacegraph.space2d.widget.textedit.view.CursorView;
import spacegraph.space2d.widget.textedit.view.LineView;
import spacegraph.util.MutableRectFloat;

class TextEditView {

    public static final LineView[] EmptyLineviewArray = new LineView[0];
    public final CursorView cursor;
    public LineView[] lines;


    TextEditView() {
//        realloc(rows, cols);
        lines = EmptyLineviewArray;
        this.cursor = new CursorView();
    }

    public boolean realloc(int rows, int cols) {
        synchronized (this) {
            if (lines == null || lines.length < rows) {

                delete();

                lines = new LineView[rows];
                for (int r = 0; r < rows; r++)
                    lines[r] = new LineView(cols);

                return true;

            } else {
                if (lines.length > 0 && lines[0].length() < cols) {
                    for (LineView v : lines)
                        v.realloc(cols);
                    return true;
                }
            }
            return false;
        }
    }

    void delete() {
        synchronized (this) {
            if (lines == null) return;
            for (LineView l : lines)
                l.delete();
            lines = null;
        }
    }


    RectF updateCursor(CursorPosition c, @Nullable RectF v) {
//        LineView lv = lines[c.getRow()];
//        if (lv == null)
//            return; //HACK

//        int lineChars = lv.length();

//        float x;
////        if (document.isLineStart()) {
//////            x = (float) lv.getChars().stream().mapToDouble(cv -> cv.width() / 2).findFirst()
//////                    .orElse(cursor.getWidth() / 2);
////            x = CursorView.getWidth()/2; //lv.getChars().get(0).width()/2;
////        } else
//        int cx = c.getCol();
//        if (cx >= lineChars) {
//            x = lv.getWidth() + (CursorView.getWidth() / 2);
//        } else {
//            CharView cc = lv.chars[cx];
//            if (cc == null)
//                x = 0;
//            else
//                x = cc.position.x;
//        }

        int cx = c.col;
        int cy = c.row;
        if (!v.contains(cx, cy, cx, cy)) {
            //view follows cursor
            float dx = 0, dy = 0;
            if (cx > v.right()) dx = (cx-(v.right()));
            else if (cx < v.left()) dx = (v.left()-cx);
            if (cy > v.top()) dy = (cy - v.top());
            else if (cy < v.bottom()) dy = (v.bottom()-cy);
            v = v.move((dx), (dy));
            ((MutableRectFloat)v).commitLerp(1);
        }

        cursor.position.set(cx, cy, 0);
        cursor.cursor = c;

        return v;
    }

    //
//    @Override
//    public void update(Buffer buffer) {
//        updateY();
//        updateCursor(cursorPos);
//    }

//    @Override
//    public void addLine(BufferLine bufferLine) {
//        _addLine(bufferLine);
//        updateY();
//    }
//
//    private void _addLine(BufferLine bufferLine) {
//        lines.add(new LineView(bufferLine));
//    }

//    @Override
//    @Deprecated public void removeLine(BufferLine bufferLine) {
//        if (lines.removeIf(lineView -> lineView.bufferLine == bufferLine))
//            updateY();
//    }

//    @Override
//    public void moveChar(BufferLine fromLine, BufferLine toLine, BufferChar c) {
//        int[] k = {0};
//        lines.stream().filter(l -> l.bufferLine == fromLine).findFirst().ifPresent(
//                (from) -> lines.stream().filter(l -> l.bufferLine == toLine).findFirst().ifPresent((to) -> {
//                    float fromY = from.position.y, toY = to.position.y;
//                    CharView leaveChar = from.leaveChar(c);
//                    leaveChar.position.y = -(toY - fromY);
//                    to.addChar(leaveChar, k[0]++);
//                    to.update();
//                }));
//    }



}

//
//    private double documentHeight() {
//        double height = 0;
//        //for (LineView line : lines) {
//        height += lines.size() * LineView.getHeight();
//        //}
//        return height;
//    }
//
//    private double documentWidth() {
//        double maxWidth = 0;
//        for (LineView line : lines) {
//            double width = line.getWidth();
//            maxWidth = (maxWidth < width) ? width : maxWidth;
//        }
//        return maxWidth;
//    }