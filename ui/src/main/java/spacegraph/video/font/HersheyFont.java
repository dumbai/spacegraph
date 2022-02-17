package spacegraph.video.font;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.data.list.Lst;
import jcog.util.ArrayUtil;
import org.eclipse.collections.impl.list.mutable.primitive.ByteArrayList;
import spacegraph.video.Draw;

import java.io.IOException;
import java.util.List;

/** TODO http://openimaj.org/apidocs/org/openimaj/image/typography/hershey/HersheyFont.html */
public final class HersheyFont {

    private static final HersheyFont[] fontMono;

    static final int offsetR = ('R');
    /*int idx, verts, */
    final int leftPos;
    final int rightPos;

    final byte[][] segments;
    private int id;

    static {

        String[] lines = null;


        for (int tries = 0; tries < 2; tries++) {
            try {
                String font =
                        "futural";

                lines = new String(Draw.class.getClassLoader().getResourceAsStream("spacegraph/font/hershey/" + font + ".jhf").readAllBytes()).split("\n");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Util.sleepMS(1);
        }
        if (lines == null) {
            lines = ArrayUtil.EMPTY_STRING_ARRAY;
        }

        String scratch = "";
        List<HersheyFont> glyphs = new Lst(256);
        for (String line : lines) {
            String c = line;
            if (c.endsWith("\n"))
                c = c.substring(0, c.length() - 1);


            if (Character.isDigit(c.charAt(4))) {
                glyphs.add(new HersheyFont(c + scratch));
                scratch = "";
            } else {
                scratch += c;
            }
        }


        fontMono = glyphs.toArray(new HersheyFont[0]);
    }

    /**
     * call glPush before this, and after all textNext's. returns the character width to translate by to display the next character (left to right direction)
     */
    public static void textStart(GL2 gl, float scaleX, float scaleY, float x, float y, float z) {

        gl.glTranslatef(x, y, z);
        gl.glScalef(scaleX / 20, scaleY / 20, 1);
    }

    public static void textNext(GL2 gl, char c, float x) {

        int ci = c - 32;
        if (ci >= 0 && ci < fontMono.length) {
            fontMono[ci].draw(gl, x * 20);
        }

    }

    /** load glyphs into GL context */
    public static void load(GL2 gl) {
        for (HersheyFont x : fontMono)
            x.init(gl);
    }

    HersheyFont(String hspec) {


        leftPos = (hspec.charAt(8)) - offsetR;
        rightPos = (hspec.charAt(9)) - offsetR;

        //        boolean penUp = true;
        ByteArrayList currentSeg = new ByteArrayList(8);

        String spec = (hspec.substring(10));
        int ss = spec.length() - 1;
        Lst<byte[]> segments = new Lst();
        for (int i = 0; i < ss; i += 2) {

            char ci = spec.charAt(i), cii = spec.charAt(i + 1);

            if (cii == 'R' && ci == ' ') {
//                penUp = true;
                segments.add(currentSeg.toArray());
                currentSeg = new ByteArrayList(8);
                continue;
            }

            int curX = ci - offsetR;
            currentSeg.add((byte) curX);
            int curY = cii - offsetR;
            currentSeg.add((byte) (10 - curY));
        }
        if (!currentSeg.isEmpty())
            segments.add(currentSeg.toArray());

        this.segments = segments.toArray(new byte[segments.size()][]);


    }

    public static void hersheyText(GL2 gl, CharSequence s, float scale, float x, float y, float z) {
        hersheyText(gl, s, scale, scale, x, y, z, Draw.TextAlignment.Center);
    }

    public static void hersheyText(GL2 gl, CharSequence s, float scale, float x, float y, float z, Draw.TextAlignment a) {
        hersheyText(gl, s, scale, scale, x, y, z, a);
    }

    public static void hersheyText(GL2 gl, CharSequence s, float scaleX, float scaleY, float x, float y, float z, Draw.TextAlignment a) {


        int sl = s.length();
        if (sl == 0)
            return;

        float totalWidth = sl * scaleX;
        switch (a) {
            case Left -> x += scaleX / 2.0f;
            case Right -> x -= totalWidth;
            case Center -> x -= totalWidth / 2.0f;
        }

        Draw.push(gl);

        textStart(gl, scaleX, scaleY, x, y, z);

        for (int i = 0; i < sl; i++) {
            textNext(gl, s.charAt(i), i);
        }

        Draw.pop(gl);
    }

    public static void hersheyText(GL2 gl, char c, float scale, float x, float y, float z) {
        hersheyText(gl, c, scale, scale, x, y, z);
    }

    private static void hersheyText(GL2 gl, char c, float scaleX, float scaleY, float x, float y, float z) {

        int ci = c - 32;
        if (ci >= 0 && (ci < fontMono.length)) {

            Draw.push(gl);

            float sx = scaleX / 20.0f;
            float sy = scaleY / 20.0f;
            gl.glScalef(sx, sy, 1.0f);

            gl.glTranslatef(x / sx, y / sy, z);

            fontMono[ci].draw(gl);
            Draw.pop(gl);
        }
    }

    void draw(GL2 gl, float x) {


        boolean xShifted =
            x!=0;
            //Math.abs(x) > Float.MIN_NORMAL;

        if (xShifted)
            gl.glTranslatef(x, 0, 0);

        draw(gl);

        if (xShifted)
            gl.glTranslatef(-x, 0, 0);
    }

    final void draw(GL2 gl) {
        gl.glCallList(id);
    }

    private void init(GL2 gl) {
        id = gl.glGenLists(1);
        gl.glNewList(id, GL2.GL_COMPILE);

        for (byte[] seg : segments) {

            int ss = seg.length;

            gl.glBegin(GL.GL_LINE_STRIP);
            for (int j = 0; j < ss; )
                gl.glVertex2i(seg[j++], seg[j++]);
            gl.glEnd();
        }

        gl.glEndList();
    }
}