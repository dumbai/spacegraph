package spacegraph.space2d.widget.textedit.buffer;

import java.util.concurrent.atomic.AtomicBoolean;

public class CursorPosition implements Comparable<CursorPosition> {
    private final AtomicBoolean whenChanged;
    public int col;
    public int row;

    @Deprecated CursorPosition(int row, int col) {
        this(row, col, new AtomicBoolean());
    }

    public CursorPosition(int row, int col, AtomicBoolean whenChanged) {
        set(row, col);
        this.whenChanged = whenChanged;
    }

    public final void set(int row, int col) {
        setRow(row); setCol(col);
    }

    @Override
    public int compareTo(CursorPosition o) {
        int rowCompare = Integer.compare(this.row, o.row);
        return rowCompare == 0 ? Integer.compare(this.col, o.col) : rowCompare;
    }

    @Override
    public boolean equals(Object o) {
        return compareTo((CursorPosition) o) == 0;
    }

    @Override
    public String toString() {
        return "[row:" + row + ", col:" + col + ']';
    }

    public boolean incCol(int gain) {
        return setCol(col + gain);
    }

    public boolean incRow(int gain) {
        return setRow(row + gain);
    }

    public boolean decCol(int gain) {
        return incCol(-gain);
    }

    public boolean decRow(int gain) {
        return incRow(-gain);
    }

    public boolean setCol(int col) {
        if (this.col != col) {
            this.col = col;
            whenChanged.set(true);
            return true;
        }
        return false;
    }

    public boolean setRow(int row) {
        if (this.row != row) {
            this.row = row;
            whenChanged.set(true);
            return true;
        }
        return false;
    }
}
