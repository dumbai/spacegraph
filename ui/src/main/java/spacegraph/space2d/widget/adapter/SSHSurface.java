package spacegraph.space2d.widget.adapter;

import com.jcraft.jcterm.*;
import com.jcraft.jsch.JSchException;
import spacegraph.SpaceGraph;
import spacegraph.input.finger.Finger;
import spacegraph.input.key.KeyPressed;
import spacegraph.input.key.impl.Keyboard;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.space2d.meta.MetaFrame;
import spacegraph.space2d.widget.console.BufferedBitmapTextGrid;
import spacegraph.util.math.Color4f;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by me on 11/13/16.
 */
public class SSHSurface extends MutableUnitContainer implements Terminal, KeyPressed {

//    private TexSurface tex;

    public static void main(String[] args) throws IOException, JSchException {

        SSHSurface s = new SSHSurface();
        SpaceGraph.window(new MetaFrame(s), 800, 600);

        s.start(new JCTermSwingFrame().connect("me", "localhost", 22));
    }


    private static final ConfigurationRepository defaultCR =
            new ConfigurationRepository() {
                private final Configuration conf = new Configuration();

                public Configuration load(String name) {
                    return conf;
                }

                public void save(Configuration conf) {
                }
            };
    private static ConfigurationRepository cr = defaultCR;

    final BufferedBitmapTextGrid grid = new BufferedBitmapTextGrid(true,true);

    private final Color[] colors = {Color.black, Color.red, Color.green,
            Color.yellow, Color.blue, Color.magenta, Color.cyan, Color.white};
    private OutputStream out;
    private TerminalEmulator emulator;
//    private Connection connection = null;
//    private BufferedImage img;
//    private BufferedImage background;
//    private Graphics2D cursor_graphics;
//    private Graphics2D graphics;

    private final Color4f bground = new Color4f(0,0,0,1), fground = new Color4f(1,1,1, 1);
    private final Color4f defaultbground = new Color4f(bground);
    private final Color4f defaultfground = new Color4f(fground);

//    private Font font;
    private boolean bold;
    private boolean underline;
    private boolean reverse;
    private final int term_width = 80;
    private final int term_height = 24;
//    private int descent = 0;
    private int x;
    private int y;
//    private int char_width;
//    private int char_height;

//    private int line_space = -2;
//    private boolean antialiasing = true;

    public SSHSurface() {
        super();
//        setFont("Monospace-18");
        grid.resize(term_width, term_height);
        set(grid);
    }

    @Override
    protected void starting() {
        super.starting();
//        pixelSize(getTermWidth(), getTermHeight());
        clear();
        focus(); //TODO autofocus flag
    }

    @Override
    public Surface finger(Finger finger) {
        return this;
    }

    static Color toColor(Object o) {
        if (o instanceof String) {
            try {
                return Color.decode(((String) o).trim());
            } catch (NumberFormatException e) {
            }
            return Color.getColor(((String) o).trim());
        }
        if (o instanceof Color) return (Color) o;
        return Color.white;
    }

    public static synchronized ConfigurationRepository getCR() {
        return cr;
    }

    public static synchronized void setCR(ConfigurationRepository _cr) {
        if (_cr == null)
            _cr = defaultCR;
        cr = _cr;
    }

//    void setFont(String fname) {
//        font = Font.decode(fname);
////        font.
//        char_width = font.getSize();
//        char_height = (int) (font.getSize()*1.6f);
//        background = new BufferedImage(char_width, char_height, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = (Graphics2D) (background.getGraphics());
//        g.setFont(font);
//
//        {
//            FontMetrics fo = g.getFontMetrics();
//            descent = fo.getDescent();
//
////            char_width = fo.charWidth('@');
////            char_height = fo.getHeight() + (line_space * 2);
//            descent += line_space;
//        }
//
//        g.setColor(getBackGround());
//        g.fillRect(0, 0, char_width, char_height);
//        g.dispose();
//    }

//    public void pixelSize(int w, int h) {
//
////        Dimension pixelSize = new Dimension(getTermWidth(), getTermHeight());
//
//        BufferedImage imgOrg = img;
//        if (graphics != null)
//            graphics.dispose();
//
//        int column = w / getCharWidth();
//        int row = h / getCharHeight();
//        term_width = column;
//        term_height = row;
//
//        if (emulator != null)
//            emulator.reset();
//
//        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
//        img.setAccelerationPriority(1.0f);
//        set(tex = Tex.view(img));
//
//        graphics = (Graphics2D) (img.getGraphics());
//        graphics.setFont(font);
//
//
//        clear_area(0, 0, w, h);
//        redraw(0, 0, w, h);
//
//        setAntiAliasing(antialiasing);
//
//        if (imgOrg != null) {
//            Shape clip = graphics.getClip();
//            graphics.setClip(0, 0, getTermWidth(), getTermHeight());
//            graphics.drawImage(imgOrg, 0, 0, null);
//            graphics.setClip(clip);
//        }
//
//        resetCursorGraphics();
//
//
//        if (connection != null) connection.requestResize(this);
//
//        if (imgOrg != null) imgOrg.flush();
//    }

    public synchronized void start(Connection connection) {

        if (emulator != null)
            emulator.reset();

//        this.connection = connection;
        if (connection != null) {
            InputStream in = connection.getInputStream();
            out = connection.getOutputStream();
            emulator = new EmulatorVT100(this, in);
            emulator.start();
        } else {
            //stop
        }

        clear();
//        redraw(0, 0, getTermWidth(), getTermHeight());
        focus();
    }


    @Override
    public boolean key(com.jogamp.newt.event.KeyEvent e, boolean pressedOrReleased) {
        int code = Keyboard.newtKeyCode2AWTKeyCode(e.getKeyCode());
        if (pressedOrReleased) keyPressed(code, e.getKeyChar());
        else keyTyped(e.getKeyChar());

        return true;
    }

    public void processKeyEvent(KeyEvent e) {

        int id = e.getID();
        switch (id) {
            case KeyEvent.KEY_PRESSED:
                keyPressed(e);
                break;
            case KeyEvent.KEY_RELEASED:
                /*keyReleased(e);*/
                keyTyped(e);
                break;
            case KeyEvent.KEY_TYPED:
                //keyTyped(e);/*keyTyped(e);*/
                break;
        }
        e.consume();
    }

    public void keyPressed(KeyEvent e) {
        keyPressed(e.getKeyCode(), e.getKeyChar());
    }

    public void keyPressed(int keycode, char keychar) {
        byte[] code = null;
        switch (keycode) {
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_SHIFT:
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_CAPS_LOCK:
                return;
            case KeyEvent.VK_ENTER:
                code = emulator.getCodeENTER();
                break;
            case KeyEvent.VK_UP:
                code = emulator.getCodeUP();
                break;
            case KeyEvent.VK_DOWN:
                code = emulator.getCodeDOWN();
                break;
            case KeyEvent.VK_RIGHT:
                code = emulator.getCodeRIGHT();
                break;
            case KeyEvent.VK_LEFT:
                code = emulator.getCodeLEFT();
                break;
            case KeyEvent.VK_F1:
                code = emulator.getCodeF1();
                break;
            case KeyEvent.VK_F2:
                code = emulator.getCodeF2();
                break;
            case KeyEvent.VK_F3:
                code = emulator.getCodeF3();
                break;
            case KeyEvent.VK_F4:
                code = emulator.getCodeF4();
                break;
            case KeyEvent.VK_F5:
                code = emulator.getCodeF5();
                break;
            case KeyEvent.VK_F6:
                code = emulator.getCodeF6();
                break;
            case KeyEvent.VK_F7:
                code = emulator.getCodeF7();
                break;
            case KeyEvent.VK_F8:
                code = emulator.getCodeF8();
                break;
            case KeyEvent.VK_F9:
                code = emulator.getCodeF9();
                break;
            case KeyEvent.VK_F10:
                code = emulator.getCodeF10();
                break;
            case KeyEvent.VK_TAB:
                code = emulator.getCodeTAB();
                break;
        }
        if (code != null) {
            try {
                out.write(code, 0, code.length);
                out.flush();
            } catch (Exception ee) {
            }
            return;
        }


        if ((keychar & 0xff00) == 0) try {
            out.write(keychar);
            out.flush();
        } catch (Exception ee) {
        }
    }

    public void keyTyped(KeyEvent e) {
        char keychar = e.getKeyChar();
        keyTyped(keychar);
    }

    public void keyTyped(char keychar) {
        if ((keychar & 0xff00) != 0) {
            char[] foo = new char[1];
            foo[0] = keychar;
            try {
                byte[] goo = new String(foo).getBytes("EUC-JP");
                out.write(goo, 0, goo.length);
                out.flush();
            } catch (Exception eee) {
            }
        }
    }

//    @Override public int getTermWidth() {
//        return term_width;
//    }
//
//    @Override public int getTermHeight() {
//        return term_height;
//    }

    public int getCharWidth() {
        return 1;
    }

    public int getCharHeight() {
        return 1;
    }

    public int getColumnCount() {
        return term_width;
    }

    public int getRowCount() {
        return term_height;
    }

    public void clear() {
        clear_area(0,0,term_width,term_height);
    }

    public void setCursor(int x, int y) {
        if (this.x!=x || this.y!=y) {
            grid.cursorCol = x;
            grid.cursorRow = y;

            if (this.x >= 0)
                redraw(this.x, this.y, 1, 1);

            this.x = x;
            this.y = y;

            redraw(this.x, this.y, 1, 1);

        }
    }

    public void draw_cursor() {
//        grid.redraw(grid.chars[x][y], )
//        cursor_graphics.fillRect(x, y - char_height, char_width, char_height);
//        redraw(x, y - char_height, char_width, char_height);
    }

    @Override
    public void redraw(int x0, int y0, int width, int height) {
        //graphics.drawImage(img, 0, 0, null);
        //repaint(x, y, width, height);
        //tex.set(img);
        int x1 = x0 + width, y1 = y0 + height;

        Color4f fg = fg(), bg = bg();
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++)
                if (x >= 0 && y >= 0 && y < term_height && x < term_width)
                    grid.redraw(grid.chars[x][y], x, y, fg, bg, false, false);
            }

    }


    public void clear_area(int x1, int y1, int x2, int y2) {
        y1++;// y2++;
        for (int y = y1; y < y2; y++)
            for (int x = x1; x < x2; x++)
                grid.redraw(' ', x, y, fground, bground);

        grid.invalidate();
    }


    public void scroll_area(int x, int y, int w, int h, int dx, int dy) {
        grid.copy(x, y, w, h, dx, dy, fg(), bg());
        redraw(x + dx, y + dy, w, h);
        grid.invalidate();
    }

    public void drawBytes(byte[] buf, int s, int l, int x, int y) {
        Color4f fground = fg(), bground = bg();
        String b = new String(buf, s, l);
        int len = b.length();
        for (int i = 0; i < len; i++) {
            grid.redraw(b.charAt(i), x++, y, fground, bground, underline, false);
//            grid.redraw((char)buf[i+s], x++, y, fground, bground, underline, false);
        }
        grid.invalidate();

//        graphics.drawBytes(buf, s, len, x, y - descent);
//        if (bold)
//            graphics.drawBytes(buf, s, len, x + 1, y - descent);
//
//        if (underline) graphics.drawLine(x, y - 1, x + len * char_width, y - 1);

    }

    public void drawString(String str, int x, int y) {
        Color4f fground = fg(), bground = bg();
        int n = str.length();
        for (int i = 0; i < n; i++)
            grid.redraw(str.charAt(i), x++, y, fground, bground);
        grid.invalidate();

//        graphics.drawString(str, x, y - descent);
//        if (bold)
//            graphics.drawString(str, x + 1, y - descent);
//
//        if (underline) graphics.drawLine(x, y - 1, x + str.length() * char_width, y - 1);

    }

    public void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Ignores key released events.
     */
    public void keyReleased(KeyEvent event) {
    }

//    public void setLineSpace(int foo) {
//        this.line_space = foo;
//    }
//
//    public boolean getAntiAliasing() {
//        return antialiasing;
//    }
//
//    public void setAntiAliasing(boolean foo) {
//        if (graphics == null)
//            return;
//        antialiasing = foo;
//        Object mode = foo ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
//                : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
//        Map<Object, Object> hints = new RenderingHints(
//                RenderingHints.KEY_TEXT_ANTIALIASING, mode);
//        graphics.setRenderingHints(hints);
//    }

//    public static void setCompression(int compression) {
//        if (compression < 0 || 9 < compression)
//            return;
//    }

    public void setDefaultForeGround(Object f) {
        defaultfground.set(toColor(f));
    }

    public void setDefaultBackGround(Object f) {
        defaultbground.set(toColor(f));
    }

    private Color4f fg() {
        return reverse ? bground : fground;
    }
    private Color4f bg() {
        return reverse ? fground : bground;
    }

    public void setForeGround(Object f) {
        fground.set(toColor(f));
    }

    public void setBackGround(Object b) {
        bground.set(toColor(b));
//        Graphics2D foog = (Graphics2D) (background.getGraphics());
//        foog.setColor(getBackGround());
//        foog.fillRect(0, 0, char_width, char_height);
//        foog.dispose();
    }

//    void resetCursorGraphics() {
//        if (cursor_graphics != null)
//            cursor_graphics.dispose();
//
//        cursor_graphics = (Graphics2D) (img.getGraphics());
//        cursor_graphics.setColor(getForeGround());
//        cursor_graphics.setXORMode(getBackGround());
//    }

    public Object getColor(int index) {
        if (colors == null || index < 0 || colors.length <= index)
            return null;
        return colors[index];
    }

    public void setBold() {
        bold = true;
    }

    public void setUnderline() {
        underline = true;
    }

    public void setReverse() {
        reverse = true;
    }

    public void resetAllAttributes() {
        bold = false;
        underline = false;
        reverse = false;
        bground.set(defaultbground);
        fground.set(defaultfground);
//        if (graphics != null)
//            graphics.setColor(getForeGround());
    }
}