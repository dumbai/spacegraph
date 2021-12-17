package spacegraph.space2d.widget.textedit.view;

import com.jogamp.opengl.GL2;
import spacegraph.space2d.widget.textedit.buffer.CursorPosition;
import spacegraph.video.Draw;

public class CursorView extends TextEditRenderable {

  public CursorPosition cursor;

  public CursorView() {
    color.set(1.0f, 0.5f, 0, 0.5f);
  }

  @Override
  public void innerDraw(GL2 gl) {
    Draw.rect(-0.5f, -0.5f, 1.0f, 1.0f, 0, gl);
//    Texture texture = textureProvider.getTexture(gl, "◆");
//    texture.enable(gl);
//    texture.bind(gl);
//    gl.glColor4d(0.4, 0.4, 1, 0.5);
//
//    gl.glRotated((System.currentTimeMillis() / 5f) % 360, 0, 1, 0);
//
//    gl.glBegin(GL2.GL_POLYGON);
//    gl.glTexCoord2f(0, 1);
//    gl.glVertex2d(-0.5, -0.5);
//    gl.glTexCoord2f(0, 0);
//    gl.glVertex2d(-0.5, 0.5);
//    gl.glTexCoord2f(1, 0);
//    gl.glVertex2d(0.5, 0.5);
//    gl.glTexCoord2f(1, 1);
//    gl.glVertex2d(0.5, -0.5);
//    gl.glEnd();
  }
}