package spacegraph.space2d.widget.button;

import com.jogamp.opengl.GL2;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.ReSurface;
import spacegraph.video.Draw;

public class ColorToggle extends ToggleButton {
    public float r;
    public float g;
    public float b;

    public ColorToggle(float r, float g, float b) {
        this.r = r; this.g = g; this.b = b;
    }

    @Override
    protected void paintWidget(RectF bounds, GL2 gl) {
        gl.glColor4f(r, g, b, 0.95f);
        Draw.rect(bounds, gl);

    }

    @Override
    protected void renderContent(ReSurface r) {
        super.renderContent(r);

        if (on.get()) {
            GL2 gl = r.gl;
            gl.glColor3f(1, 1, 1);
            gl.glLineWidth(3.0f);
            Draw.rectStroke(bounds, gl);
        }
    }

}