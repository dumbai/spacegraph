//package spacegraph.video.font;
//
//import com.jogamp.opengl.GL;
//import com.jogamp.opengl.GL2;
//import com.jogamp.opengl.GL2ES3;
//import com.jogamp.opengl.util.texture.TextureData;
//import com.jogamp.opengl.util.texture.TextureIO;
//import spacegraph.SpaceGraph;
//import spacegraph.space2d.ReSurface;
//import spacegraph.space2d.Surface;
//import spacegraph.video.Draw;
//
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//
//public class BmpFont {
//
//
//    private TextureData texture;
//
//
//    private int base;
//    private final int[] textures = new int[2];
//
//
//    private ByteBuffer stringBuffer = ByteBuffer.allocate(256);
//
//    private static final ThreadLocal<BmpFont> fonts = ThreadLocal.withInitial(BmpFont::new);
//
//    private GL2 gl;
//
//    private static BmpFont the(GL2 g) {
//        BmpFont f = fonts.get();
//        if (!f.init)
//            f.init(g);
//        return f;
//    }
//
//
//
//    private void loadGLTextures() {
//
//        String[] tileNames =
//                {"font2.png"/*, "bumps.png"*/};
//
//
//        gl.glGenTextures(2, textures, 0);
//
//        for (int i = 0; i < 1; i++) {
//
//            InputStream r = Draw.class.getClassLoader().getResourceAsStream(tileNames[i]);
//            try {
//
//                boolean mipmap = false;
//                texture = TextureIO.newTextureData(gl.getGLProfile(), r,
//                        mipmap, "png");
//            } catch (Throwable t) {
//                t.printStackTrace();
//                System.exit(1);
//            }
//
//            gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
//
//            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
//            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
//
//            gl.glTexImage2D(GL.GL_TEXTURE_2D,
//                    0,
//
//                    3,
//                    texture.getWidth(),
//                    texture.getHeight(),
//                    0,
//                    GL.GL_RGBA,
//                    GL.GL_UNSIGNED_BYTE,
//                    texture.getBuffer());
//
//
//        }
//    }
//
//    private void buildFont()
//    {
//        float cx;
//        float cy;
//
//        base = gl.glGenLists(256);
//        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]);
//        for (int loop = 0; loop < 256; loop++)
//        {
//            cx = (loop % 16) / 16.0f;
//            cy = (loop / 16) / 16.0f;
//
//            gl.glNewList(base + loop, GL2.GL_COMPILE);
//            gl.glBegin(GL2ES3.GL_QUADS);
//            gl.glTexCoord2f(cx, 1 - cy - 0.0625f);
//            gl.glVertex2i(0, 0);
//            gl.glTexCoord2f(cx + 0.0625f, 1 - cy - 0.0625f);
//            gl.glVertex2i(10, 0);
//            gl.glTexCoord2f(cx + 0.0625f, 1 - cy);
//            gl.glVertex2i(10, 16);
//            gl.glTexCoord2f(cx, 1 - cy);
//            gl.glVertex2i(0, 16);
//            gl.glEnd();
//            gl.glTranslated(10, 0, 0);
//            gl.glEndList();
//        }
//    }
//
//
//    private void write(int x, int y, String string, int set) {
//
//        if (set > 1) {
//            set = 1;
//        }
//        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[0]);
//
//
//
//
//
//
//
//
//
//        gl.glListBase(base - 32 + (128 * set));
//
//        if (stringBuffer.capacity() < string.length()) {
//            stringBuffer = ByteBuffer.allocate(string.length());
//        }
//
//        stringBuffer.clear();
//        stringBuffer.put(string.getBytes());
//        stringBuffer.flip();
//
//
//        gl.glCallLists(string.length(), GL.GL_BYTE, stringBuffer);
//
//
//
//
//
//
//
//        gl.glDisable(textures[0]);
//    }
//
//    private boolean init;
//
//    private synchronized void init(GL2 gl) {
//
//        if (this.init)
//            return;
//
//        this.gl = gl;
//
//        loadGLTextures();
//
//        buildFont();
//
//        this.init = true;
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        gl.glEnable(GL.GL_TEXTURE_2D);
//    }
//
//    public static void main(String[] args) {
//        SpaceGraph.window(new Surface() {
//
//            BmpFont f;
//            private float cnt1;
//            private float cnt2;
//
//
//            @Override
//            protected void paint(GL2 gl, ReSurface reSurface) {
//                if (f == null)
//                    f = BmpFont.the(gl);
//
//                gl.glScalef(0.05f, 0.08f, 1f);
//
//                gl.glColor3f((float) (Math.cos(cnt1)), (float)
//                        (Math.sin(cnt2)), 1.0f - 0.5f * (float) (Math.cos(cnt1 + cnt2)));
//
//
//                f.write( (int) ((280 + 250 * Math.cos(cnt1))),
//                        (int) (235 + 200 * Math.sin(cnt2)), "NeHe", 0);
//
//                gl.glColor3f((float) (Math.sin(cnt2)), 1.0f - 0.5f *
//                        (float) (Math.cos(cnt1 + cnt2)), (float) (Math.cos(cnt1)));
//
//
//                f.write((int) ((280 + 230 * Math.cos(cnt2))),
//                        (int) (235 + 200 * Math.sin(cnt1)), "OpenGL", 1);
//
//                gl.glColor3f(0.0f, 0.0f, 1.0f);
//                f.write( (int) (240 + 200 * Math.cos((cnt2 + cnt1) / 5)),
//                        2, "Giuseppe D'Agata", 0);
//
//                gl.glColor3f(1.0f, 1.0f, 1.0f);
//                f.write( (int) (242 + 200 * Math.cos((cnt2 + cnt1) / 5)),
//                        2, "Giuseppe D'Agata", 0);
//
//                cnt1 += 0.01f;
//                cnt2 += 0.0081f;
//            }
//        }, 800, 600);
//    }
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
