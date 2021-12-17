package spacegraph.space2d.widget.textedit.buffer;


import jcog.data.list.Lst;
import spacegraph.space2d.widget.textedit.view.CharView;

import java.util.Collections;
import java.util.List;

public class BufferLine {

    private int rowNum;
    public final List<BufferChar> chars = new Lst<>();
  //  private final BufferLineListener.BufferLineObserver observer = new BufferLineListener.BufferLineObserver();

//    public void addListener(BufferLineListener listener) {
    //    observer.addListener(listener);
    //}

    public void updatePosition(int row) {
        this.rowNum = row;
//        int cols = chars.size();
//        for (int col = 0; col < cols; col++) {
//            chars.get(col).updatePosition(row, col);
//        }
//        update();
    }

    public int length() {
        return chars.size();
    }

    public String toLineString() {
        int l = length();
        if (l == 0)
            return "";

        StringBuilder buf = new StringBuilder(l);
        for (BufferChar bc : chars)
            buf.append(bc.c);
        return buf.toString();
    }

    void insertChar(char c, int col) {
        insertChar(col, new CharView(c));
    }

    public void insertChar(int col, BufferChar bc) {
        chars.add(col, bc);
//        updatePosition(rowNum); //, col, col+1);
//        observer.addChar(bc, col);
    }

    /** returns the right=most substring intended to be moved to the new line */
    List<BufferChar> splitReturn(int col) {

        int cs = length();
        if (col == cs)
            return Collections.EMPTY_LIST; //EOL, nothing

        Lst<BufferChar> results = new Lst<>(cs-col);
        while (cs-- > col) {
            results.addFast(removeChar(col));
        }
//        update();
        return results;
    }

//    public void update() {
//        observer.update(this);
//    }

    BufferChar removeChar(int col) {
        //        observer.removeChar(removedChar);
        return chars.remove(col);
    }

    public void join(BufferLine line) {
        chars.addAll(line.chars);
        updatePosition(rowNum);
    }

    //    public int getRowNum() {
//        return rowNum;
//    }

//    @Override
//    public int compareTo(BufferLine o) {
//        return Integer.compare(rowNum, o.rowNum);
//    }
}
