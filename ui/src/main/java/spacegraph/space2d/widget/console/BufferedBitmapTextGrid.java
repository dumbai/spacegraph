package spacegraph.space2d.widget.console;

import jcog.Util;
import spacegraph.util.math.Color4f;

import java.util.Arrays;

public class BufferedBitmapTextGrid extends BitmapTextGrid {

    public char[][] chars;

    public BufferedBitmapTextGrid(boolean mipmap, boolean antialias) {
        super(mipmap, antialias);
    }

    @Override
    public boolean resize(int cols, int rows) {
        if (super.resize(cols, rows)) {
            chars = new char[cols][rows];
            for (char[] cc : chars)
                Arrays.fill(cc, ' ');
            return true;
        }
        return false;
    }

    @Override
    protected void charAt(int x, int y, char c) {
        super.charAt(x, y, c);
        chars[x][y] = c;
    }

    public void copy(int x0, int y0, int w, int h, int dx, int dy, @Deprecated Color4f fore, @Deprecated Color4f back) {
        x0 = Util.clamp(x0, 0, cols); y0 = Util.clamp(y0, 0, rows);
        int x1 = Util.clamp(x0+w, 0, cols), y1 = Util.clamp(y0+h,0,rows);
        int ix = dx> 0? -1 : +1, iy = dy> 0 ? -1 : +1;
        if (ix < 0) { var t = x0; x0 = x1; x1 = t; }
        if (iy < 0) { var t = y0; y0 = y1; y1 = t; }

        for (int y = y0; y != y1; y+=iy) {
            for (int x = x0; x != x1; x+=ix) {
                redraw(chars[x][y], x + dx, y + dy, fore, back);
            }
        }
    }
}