package spacegraph.space2d.widget.textedit.view;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.util.texture.Texture;
import spacegraph.space2d.widget.textedit.buffer.BufferChar;
import spacegraph.video.Tex;

public class CharView extends BufferChar {

    private Tex tt;

    public CharView() {

    }

    public CharView(char c) {
        super(c);
    }


    public void set(BufferChar c) {
        char C = c.c;
        synchronized(this) {
            if (this.c != C) {
                if (tt != null) {
                    tt.delete();
                    this.tt = null;
                }
                this.c = C;
            }
        }

        //TODO other setting
    }

    public void delete() {
        if (tt!=null) {
            tt.delete();
            this.tt = null;
        }
    }

    public static float width() {
        return 1; //textureProvider.getWidth(String.valueOf(bufferChar.getChar()));
    }

    @Override
    @Deprecated
    protected void innerDraw(GL2 gl) {

        if (tt == null || tt.texture == null)
            tt = TextureProvider.the.getTexture(String.valueOf(c), tt, gl);

        Texture texture = tt.texture;
        if (texture == null)
            return; //HACK

        texture.enable(gl);
        texture.bind(gl);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2ES3.GL_TEXTURE_ALPHA_TYPE, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

        gl.glBegin(GL2.GL_POLYGON);
        gl.glTexCoord2f(0, 1);
        gl.glVertex2d(-0.5, -0.5);
        gl.glTexCoord2f(0, 0);
        gl.glVertex2d(-0.5, 0.5);
        gl.glTexCoord2f(1, 0);
        gl.glVertex2d(0.5, 0.5);
        gl.glTexCoord2f(1, 1);
        gl.glVertex2d(0.5, -0.5);
        gl.glEnd();

        texture.disable(gl);

    }


}
