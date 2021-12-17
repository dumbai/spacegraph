package spacegraph.space2d.widget.textedit.view;

import com.jogamp.opengl.GL2;
import jcog.TODO;
import spacegraph.space2d.widget.textedit.buffer.BufferChar;
import spacegraph.space2d.widget.textedit.buffer.BufferLine;
import spacegraph.space2d.widget.textedit.hilite.TextStyle;

import java.util.List;

public class LineView extends TextEditRenderable  {

    public CharView[] chars;
    //public final FastCoWList<CharView> chars;


    /** current caret x position? */
//    private float width;

    public LineView(int capacity) {
        realloc(capacity);
//        for (int i = 0; i < capacity; i++)
//            chars[i] = null;
    }

    @Override
    @Deprecated public void innerDraw(GL2 gl) {
        throw new UnsupportedOperationException();
//        for (CharView c : chars) {
//            if (c != null)
//                c.draw(gl);
//        }
    }

//    public void update() {
//        update((c) -> {  /* */ });
//    }
//
//    private void update(Consumer<Lst<CharView>> with) {
//        chars.synchDirect((cc) -> {
//
//            with.accept(cc);
//
////            if (cc.size() > 1)
////                cc.sortThis();
//
//            float width = 0;
//            for (CharView c : cc) {
//                float w = CharView.width() / 2;
//                width += w;
//                c.position.set(width, 0, 0);
//                width += w;
//            }
//            this.width = width;
//
//            return true; //TODO commit only if sort changed the order
//        });
//    }

//    @Override
//    public void update(BufferLine bl) {
//        update();
//    }
//
//    @Override
//    public void addChar(BufferChar bufferChar, int col) {
//        addChar(new CharView(bufferChar), col);
//    }
//
//    @Override
//    public void removeChar(BufferChar removed) {
//        update((chars) -> chars.removeIf(x -> x.bufferChar() == removed));
//    }
//
//    @Override
//    public int compareTo(LineView o) {
//        return bufferLine.compareTo(o.bufferLine);
//    }

//    public CharView leaveChar(BufferChar bc) {
//
//        CharView[] leaved = new CharView[1];
//        update((chars) -> {
//            CharView leave = chars.stream().filter(c -> c.bufferChar() == bc).findFirst().orElse(null);
//            leaved[0] = leave;
//            chars.remove(leave);
//        });
//        return leaved[0];
//    }
//
//    public void addChar(CharView cv, int col) {
//        chars.add(col, cv);
////        update((chars)->{
////            chars.add(col, cv);
////        });
//    }

    public static LineView apply(int from, int to, TextStyle highlight) {
        throw new TODO();
    }

    public static String substring(int from, int to) {
        throw new TODO();
    }

    public int length() {
        return chars.length;
        //return width;
    }

    public void update(BufferLine l, int x1, int x2) {

        List<BufferChar> ll = l.chars;
        int s = ll.size();
        x1 = Math.min(s, x1);
        x2 = Math.min(s, x2);

        for (int c = x1; c < x2; c++) {
            int i = c - x1;
            CharView v = this.chars[i];
            if (v == null) chars[i] = v = new CharView();
            v.set(ll.get(c));
        }
        for (int j = x2; j < chars.length; j++) {
            if (chars[j]!=null) {
                chars[j].delete();
                chars[j] = null;
            }
        }

//        width = x2;

//        if (chars.length > i+1)
//            Arrays.fill(chars, i+1, chars.length, null);
    }

    public void realloc(int l) {
        synchronized(this) {
            if (chars != null)
                delete();
            chars = new CharView[l];
        }
    }

    public void delete() {
        synchronized(this) {
            for (CharView c : chars) {
                if (c != null)
                    c.delete();
            }
            chars = null;
        }
    }
}
