package spacegraph.space2d.widget.textedit.view;

import com.jogamp.opengl.GL2;
import jcog.math.v3;
import org.jetbrains.annotations.Nullable;
import spacegraph.util.math.Color4f;


public abstract class TextEditRenderable {

    public final v3 position = new v3();
//    public final v3 angle = new v3();
    private final @Nullable v3 scale = null;
    public final Color4f color = new Color4f(1,1,1,1);

    public final TextEditRenderable position(float x, float y) {
        position.set(x, y, 0);
        return this;
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(position.x + 0.5f, position.y, position.z);

        v3 scale = this.scale;
        if (scale!=null)
            gl.glScalef(scale.x, scale.y, scale.z);

        gl.glColor4f(color.x, color.y, color.z, color.w);
        innerDraw(gl);

        gl.glPopMatrix();
    }



    protected abstract void innerDraw(GL2 gl);


}
