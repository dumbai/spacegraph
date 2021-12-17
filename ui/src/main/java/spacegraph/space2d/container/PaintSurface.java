package spacegraph.space2d.container;

import com.jogamp.opengl.GL2;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;

/** leaf node */
public abstract class PaintSurface extends Surface {

    @Override
    protected final void render(ReSurface r) {
        paint(r.gl, r);
    }

    protected abstract void paint(GL2 gl, ReSurface reSurface);


}
