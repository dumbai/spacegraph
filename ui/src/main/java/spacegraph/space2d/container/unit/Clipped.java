package spacegraph.space2d.container.unit;

import com.jogamp.opengl.GL2;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.video.Draw;

/** renders contents only within the bounds of the container */
public class Clipped extends UnitContainer<Surface> {

    public Clipped(Surface the) {
        super(the);
    }


    @Override
    protected void renderContent(ReSurface r) {
        GL2 gl = r.gl;
        Draw.stencilStart(gl);
        Draw.rect(bounds, gl);
        Draw.stencilUse(gl, true);
        super.renderContent(r);
        Draw.stencilEnd(gl);
    }

}
