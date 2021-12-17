package spacegraph.space2d.widget.button;

import com.jogamp.opengl.GL2;
import jcog.tree.rtree.rect.RectF;
import spacegraph.video.Draw;
import spacegraph.video.ImageTexture;

import static jcog.Fuzzy.or;

/** hexagonal (circular) PushButton with (optional):
 *     icon, caption, color, and tooltip */
public class HexButton extends PushButton {

    public HexButton(String icon /* TODO abstraction for semantically reference icon from various sources including user sketches */,
                     String tooltip) {
        super(ImageTexture.awesome(icon).view(1));
    }

    @Override
    protected void paintWidget(RectF bounds, GL2 gl) {
//        super.paintWidget(bounds, gl);

        //copied from: Widget.java paintWidget
        float dim = 1.0f - (dz /* + if disabled, dim further */) / 3.0f;
        float bri = 0.25f * dim;
        color.glBri(or(bri,pri/4), gl);
        float rad =
                Math.min(w(), h())/2;
                //bounds.radius()
                //bounds.radius() * 0.5f;

        gl.glPushMatrix();
        gl.glTranslatef(cx(), cy(), 0);
        Draw.poly(6, rad, true, gl);
        gl.glPopMatrix();
    }
}