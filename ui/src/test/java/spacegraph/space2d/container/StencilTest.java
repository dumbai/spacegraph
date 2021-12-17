package spacegraph.space2d.container;

import com.jogamp.opengl.GL2;
import spacegraph.SpaceGraph;
import spacegraph.space2d.ReSurface;
import spacegraph.video.Draw;

/** https://gist.github.com/daltonks/4c2d1c5e6fd5017ea9f0 */
public class StencilTest extends PaintSurface {

    public static void main(String[] args) {
        SpaceGraph.window(new StencilTest(), 800, 800);
    }


    @Override
    protected void paint(GL2 gl, ReSurface reSurface) {

        gl.glColor4f(1,1,1, 0.75f);
        Draw.rect(bounds, gl);

        Draw.stencilMask(gl, true, (g)-> {
            
            
            gl.glColor3f(0,0,1);
            Draw.rect(bounds.scale(0.75f).move(0.5f, 0.5f), gl);
        }, (g)->{
            gl.glColor3f(1,0,0);
            Draw.rect(bounds.scale(0.75f).move(Math.random()*w(), Math.random()*h()), gl);
        });

        
        
        gl.glColor4f(0,1,0, 0.2f);
        Draw.rect(bounds.scale(0.75f).move(Math.random()*w(), Math.random()*h()), gl);

    }
}
