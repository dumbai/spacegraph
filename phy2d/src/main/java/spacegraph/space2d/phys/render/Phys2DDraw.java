package spacegraph.space2d.phys.render;

import com.jogamp.opengl.GL2;
import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.dynamics.Body2D;

import static com.jogamp.opengl.GL.GL_TRIANGLE_FAN;

public class Phys2DDraw {
    public static void poly(Body2D body, GL2 gl, float preScale, PolygonShape shape) {
        PolygonShape poly = shape;

        gl.glBegin(GL_TRIANGLE_FAN);
        int n = poly.vertices;
        v2[] pv = poly.vertex;

        for (int i = 0; i < n; ++i)
            body.getWorldPointToGL(pv[i], preScale, gl);

        body.getWorldPointToGL(pv[0], preScale, gl);

        gl.glEnd();
    }





}
