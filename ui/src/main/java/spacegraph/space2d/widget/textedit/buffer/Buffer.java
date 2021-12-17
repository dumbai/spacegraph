package spacegraph.space2d.widget.textedit.buffer;


import jcog.TODO;
import jcog.data.list.FastCoWList;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Buffer {


    public CursorPosition cursor;
    private CursorPosition mark;

    public final FastCoWList<BufferLine> lines = new FastCoWList<>(BufferLine[]::new);
    public final AtomicBoolean changed = new AtomicBoolean(false);

    public Buffer(String value) {
        cursor = new CursorPosition(0, 0, changed);
        mark = new CursorPosition(0, 0, changed);
        text(value);
    }

    private boolean isEmpty() {
        return lines.isEmpty() || height() == 1 && lines.get(0).length() == 0;
    }

    public void clear() {

        synchronized (this) {
            int l = height();
            if (l == 1 && lines.get(0).length() == 0)
                return; //no change

            if (l > 0)
                lines.clear();

            cursor.set(0, 0);
            mark.set(0, 0);

            addLine(new BufferLine());

        }

        update();
    }


    @Deprecated
    private void update() {
        changed.set(true);
    }

    public void insert(String string) {
        switch (string) {
            case "":
                break;
            case "\r":
            case "\n":
            case "\r\n":
                insertEnter(true);
                break;
            default:
                if (string.contains("\n")) {
                    String[] values = string.split("\n");
                    synchronized (this) {
                        for (String x : values) {
                            insertChars(x, false);
                            insertEnter(false);
                        }
                        update();
                    }
                } else {
                    insertChars(string, true);
                }
//                String[] values = string.split("(\r\n|\n|\r)");
//                if (values.length == 1) {
//                    values[0].codePoints().forEach(
//                            codePoint -> insertChar(new String(Character.toChars(codePoint))));
//                } else {
//                    Arrays.stream(values).forEach(v -> {
//                        v.codePoints().forEach(codePoint -> insertChar(new String(Character.toChars(codePoint))));
//                        insertEnter();
//                    });
//                }
                break;
        }

    }

    public void insert(CharSequence c) {
        throw new TODO();
    }
    private void insertChars(CharSequence string, boolean update) {
        synchronized (this) {
            int n = string.length();
            if (n > 0) {
                BufferLine line = currentLine();
                int c = cursor.col;
                for (int i = 0; i < n; i++)
                    line.insertChar(string.charAt(i), c++);
                cursor.incCol(n);
                if (update)
                    update();
            }
        }
    }

    /**
     * carriage return
     */
    public void insertEnter(boolean update) {
        synchronized (this) {
            BufferLine currentRow = currentLine();

            List<BufferChar> nextLineChars = currentRow.splitReturn(cursor.col);

            cursor.setCol(0);
            cursor.incRow(1);
            BufferLine nextLine = new BufferLine();
            addLine(nextLine);

//            if (update) {
//                update();
//                //observer.update(this); //light
//            }

            //List<BufferChar> nlc = nextLine.getChars();
            int[] k = {0};
            nextLineChars.forEach(c -> {
                //nlc.add(c);
                //observer.moveChar(currentLine, nextLine, c);
                nextLine.insertChar(k[0]++, c);

            });

//            nextLine.update();

            if (update)
                update();
        }
    }

    private void addLine(BufferLine nextLine) {
        lines.add(cursor.row, nextLine);
//        observer.addLine(nextLine);
    }

    /**
     * width in columns
     */
    public int width() {
        return lines.stream().mapToInt(BufferLine::length).max().orElse(0);
    }

    /**
     * height in lines aka rows
     */
    public int height() {
        return lines.size();
    }

    private BufferLine currentLine() {
        //synchronized (this) {
        return lines.get(cursor.row);
        //}
    }

//    public BufferLine preLine() {
//        if (currentCursor.getRow() == 0) {
//            return null;
//        }
//        return lines.get(currentCursor.getRow() - 1);
//    }
//
//    public BufferLine postLine() {
//        if (currentCursor.getRow() == lines.size() - 1) {
//            return null;
//        }
//        return lines.get(currentCursor.getRow() - 1);
//    }


//    public boolean textEquals(String s) {
//        if (s.isEmpty()) {
//            return isEmpty();
//        } else {
//			//TODO optimize without necessarily constructing String
//			return !lines.isEmpty() && text().equals(s);
//        }
//    }

    public String text() {
        return switch (height()) {
            case 0 -> "";
            case 1 -> lines.get(0).toLineString();
            default -> lines.stream().map(BufferLine::toLineString).collect(Collectors.joining("\n"));
        };
    }

    @Override
    public String toString() {
        return text() +
                String.format("Caret:[%d,%d]", cursor.col, cursor.row);
    }

    public void backspace() {
        synchronized (this) {
            if (!isBufferHead() || !isLineStart()) {
                back();
                delete();
            }
        }
    }

    public void delete() {
        synchronized (this) {
            if (!isEmpty() && (!isBufferLast() || !isLineEnd())) {
                BufferLine currentLine = currentLine();
                if (isLineEnd()) {
                    if (!isBufferLast()) {
                        BufferLine removedLine = lines.remove(cursor.row + 1);
//                        observer.removeLine(removedLine);
                        currentLine.join(removedLine);
//                        removedLine.getChars().forEach(c -> {
//                        //TODO fix where characters are appended here. to not use moveChar() then remove that method
//                        //see splitReturn() for similarity
//                            observer.moveChar(removedLine, currentLine, c);
//                        });
                    }
                } else {
                    currentLine.removeChar(cursor.col);
                }
                update();
            }
        }
    }

    public void head() {
        cursor.setCol(0);
    }

    public void last() {
        last(true);
    }

    private void last(boolean update) {
        cursor.setCol(currentLine().length());
    }

    public void back() {
        if (isLineStart()) {
            boolean isDocHead = isBufferHead();
            previous();
            if (!isDocHead)
                last();
        } else {
            cursor.decCol(1);
        }
    }

    public void forward() {
        if (isLineEnd()) {
            boolean isDocLast = isBufferLast();
            next();
            if (!isDocLast) {
                head();
            }
        } else {
            cursor.incCol(1);
        }
    }

    public void previous() {
        if (!isBufferHead()) {
            if (cursor.decRow(1)) {
                if (cursor.col > currentLine().length()) {
                    last(false);
                }
            }
        }
    }

    public void next() {
        if (!isBufferLast()) {
            if (cursor.incRow(1)) {
                if (cursor.col > currentLine().length()) {
                    last(false);
                }
            }

        }
    }

    public void bufferHead() {
        cursor.setRow(0);
        cursor.setCol(0);
    }

    public void bufferLast() {
        cursor.setRow(height() - 1);
        cursor.setCol(currentLine().length());
    }

    private boolean isBufferHead() {
        return cursor.row == 0;
    }

    private boolean isLineStart() {
        return cursor.col == 0;
    }

    private boolean isBufferLast() {
        return cursor.row == height() - 1;
    }

    public boolean isLineEnd() {

        int ll = currentLine().length();
        int cc = cursor.col;
        if (cc > ll) {
            cursor.setCol(ll);
            cc = ll;
        }
        return cc == ll;
    }


    public void mark() {
        mark.setCol(cursor.col);
        mark.setRow(cursor.row);
    }

    public String copy() {
        StringBuilder buf = new StringBuilder();
        if (mark.compareTo(cursor) == 0) {
            return buf.toString();
        }

        CursorPosition head = mark;
        CursorPosition tail = cursor;
        if (mark.compareTo(cursor) > 0) {
            head = cursor;
            tail = mark;
        }

        int rows = tail.row;
        if (head.row == rows) {
            buf.append(lines.get(head.row).toLineString(), head.col, tail.col);
        } else {
            buf.append(lines.get(head.row).toLineString().substring(head.col));
            if (rows - head.row > 1) {
                for (int i = head.row + 1; i < rows; i++) {
                    buf.append('\n');
                    buf.append(lines.get(i).toLineString());
                }
            }
            buf.append('\n');
            buf.append(lines.get(rows).toLineString(), 0, tail.col);
        }
        return buf.toString();
    }

    public void cut() {
        if (mark.compareTo(cursor) == 0) {
            return;
        }
        if (mark.compareTo(cursor) > 0) {
            CursorPosition tmp = mark;
            mark = cursor;
            cursor = tmp;
        }
        while (mark.compareTo(cursor) != 0) {
            backspace();
        }
    }


    public void text(String text) {
        synchronized (this) {
            clear();
            insert(text);
        }
    }

}