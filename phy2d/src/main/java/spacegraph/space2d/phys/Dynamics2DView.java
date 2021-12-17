package spacegraph.space2d.phys;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import jcog.math.v2;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.container.PaintSurface;
import spacegraph.space2d.phys.callbacks.DebugDraw;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.EdgeShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.collision.shapes.Shape;
import spacegraph.space2d.phys.common.IViewportTransform;
import spacegraph.space2d.phys.common.Mat22;
import spacegraph.space2d.phys.common.MathUtils;
import spacegraph.space2d.phys.common.Transform;
import spacegraph.space2d.phys.dynamics.Body2D;
import spacegraph.space2d.phys.dynamics.Dynamics2D;
import spacegraph.space2d.phys.dynamics.Fixture;
import spacegraph.space2d.phys.fracture.PolygonFixture;
import spacegraph.util.ParticleColor;
import spacegraph.util.math.Color3f;
import spacegraph.video.Draw;

public class Dynamics2DView extends PaintSurface {


    private Dynamics2D world;
    final Dynamics2DRenderer renderer = new Dynamics2DRenderer();
    private GL2 gl;

    public Dynamics2DView(Dynamics2D world) {
        world(world);
    }

    public Dynamics2DView world(Dynamics2D world) {
        this.world = world;

        renderer.setCamera(-30, -30, 10);
        return this;
    }

    @Override
    protected void paint(GL2 gl, ReSurface reSurface) {
        this.gl = gl;

        drawParticles();

        world.bodies(this::drawBody);

        //world.joints(this::drawJoint);

    }

    private void drawBody(Body2D body) {
//        if (body.getType() == BodyType.DYNAMIC) {
//            //g.setColor(Color.LIGHT_GRAY);
//            gl.glColor3f(0.75f,0.75f,0.75f);
//        } else {
//            gl.glColor3f(0.5f,0.5f,0.5f);
//        }
//        v2 v = new v2();
        for (Fixture f = body.fixtures; f != null; f = f.next) {
            PolygonFixture pg = f.polygon;
            if (pg != null) {
                renderer.drawSolidPolygon(body, pg.vertices(), pg.size(), new Color3f(0.5f, 0.25f, 0.25f));

            } else {
                Shape shape = f.shape();
                //                        for (int i = 0; i < poly.vertices; ++i) {
                //                            body.getWorldPointToOut(poly.vertex[i], v);
                //                            Point p = getPoint(v);
                //                            x[i] = p.x;
                //                            y[i] = p.y;
                //                        }
                //                        g.fillPolygon(x, y, poly.vertices);
                //                        Point p1 = getPoint(v1);
                //                        Point p2 = getPoint(v2);
                //                        g.drawLine(p1.x, p1.y, p2.x, p2.y);
                switch (shape.m_type) {
                    case POLYGON -> {
                        PolygonShape poly = (PolygonShape) shape;
                        renderer.drawSolidPolygon(body, poly.vertex, poly.vertices, new Color3f(0.25f, 0.5f, 0.25f));
                    }
                    case CIRCLE -> {
                        CircleShape circle = (CircleShape) shape;
                        float r = circle.skinRadius;
                        v2 v = new v2();
                        body.getWorldPointToOut(circle.center, v);
                        renderer.drawCircle(v, r, new Color3f(0.25f, 0.25f, 0.5f));
                    }
                    case EDGE -> {
                        EdgeShape edge = (EdgeShape) shape;
                        v2 v1 = edge.m_vertex1;
                        v2 v2 = edge.m_vertex2;
                        renderer.drawSegment(body.getWorldPoint(v1), body.getWorldPoint(v2), new Color3f(0.75f, 0.75f, 0.75f));
                    }
                }
            }
        }

    }


    private void drawParticles() {
//            v2[] vec = w.getParticlePositionBuffer();
//            if (vec == null) {
//                return;
//            }
//            g.setColor(Color.MAGENTA);
//            float radius = w.getParticleRadius();
//            int size = w.getParticleCount();
//            for (int i = 0; i < size; i++) {
//                v2 vx = vec[i];
//                Point pp = getPoint(vx);
//                float r = radius * zoom;
//
//                if (r < 0.5f) {
//                    g.drawLine(pp.x, pp.y, pp.x, pp.y);
//                } else {
//
//                    g.fillOval(pp.x - (int) r, pp.y - (int) r, (int) (r * 2), (int) (r * 2));
//                }
//            }


    }

    private class Dynamics2DRenderer extends DebugDraw {


        private static final int NUM_CIRCLE_POINTS = 13;


        private final float[] mat = new float[16];

        Dynamics2DRenderer() {
            mat[8] = 0;
            mat[9] = 0;
            mat[2] = 0;
            mat[6] = 0;
            mat[10] = 1;
            mat[14] = 0;
            mat[3] = 0;
            mat[7] = 0;
            mat[11] = 0;
            mat[15] = 1;
        }

        @Override
        public void view(IViewportTransform viewportTransform) {
            viewportTransform.setYFlip(false);
            super.view(viewportTransform);
        }


        private void transformViewport(GL2 gl, v2 center) {
            v2 e = viewportTransform.extents();
            v2 vc = viewportTransform.center();
            Mat22 vt = viewportTransform.getMat22Representation();

            int f = viewportTransform.isYFlip() ? -1 : 1;
            mat[0] = vt.ex.x;
            mat[4] = vt.ey.x;
            // mat[8] = 0;
            mat[12] = e.x;
            mat[1] = f * vt.ex.y;
            mat[5] = f * vt.ey.y;
            // mat[9] = 0;
            mat[13] = e.y;
            // mat[2] = 0;
            // mat[6] = 0;
            // mat[10] = 1;
            // mat[14] = 0;
            // mat[3] = 0;
            // mat[7] = 0;
            // mat[11] = 0;
            // mat[15] = 1;

            gl.glMultMatrixf(mat, 0);
            gl.glTranslatef(center.x - vc.x, center.y - vc.y, 0);
        }

        @Override
        public void drawPoint(v2 argPoint, float argRadiusOnScreen, Color3f argColor) {
            v2 vec = getWorldToScreen(argPoint);
            gl.glPointSize(argRadiusOnScreen);
            gl.glBegin(GL.GL_POINTS);
            gl.glVertex2f(vec.x, vec.y);
            gl.glEnd();
        }

        private final v2 zero = new v2();

        @Override
        public void drawPolygon(v2[] vertices, int vertexCount, Color3f color) {

            gl.glPushMatrix();
            transformViewport(gl, zero);
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glColor4f(color.x, color.y, color.z, 1.0f);
            for (int i = 0; i < vertexCount; i++) {
                v2 v = vertices[i];
                gl.glVertex2f(v.x, v.y);
            }
            gl.glEnd();
            gl.glPopMatrix();
        }

        @Override
        public void drawSolidPolygon(Body2D b, v2[] vertices, int vertexCount, Color3f color) {

            gl.glPushMatrix();
            transformViewport(gl, zero);
            gl.glBegin(GL.GL_TRIANGLE_FAN);
            gl.glColor4f(color.x, color.y, color.z, 0.8f);
            for (int i = 0; i < vertexCount; i++) {
                v2 w = b.getWorldPoint(vertices[i]);
                gl.glVertex2f(w.x, w.y);
            }
            gl.glEnd();

//            gl.glBegin(GL2.GL_LINE_LOOP);
//            gl.glColor4f(color.x, color.y, color.z, 1f);
//            for (int i = 0; i < vertexCount; i++) {
//                v2 v = vertices[i];
//                gl.glVertex2f(v.x, v.y);
//            }
//            gl.glEnd();
            gl.glPopMatrix();
        }

        @Override
        public void drawCircle(v2 center, float radius, Color3f color) {

            gl.glPushMatrix();
            transformViewport(gl, zero);
            float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
            float c = (float) Math.cos(theta);
            float s = (float) Math.sin(theta);
            float cx = center.x;
            float cy = center.y;
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glColor4f(color.x, color.y, color.z, 1);
            float y = 0;
            float x = radius;
            for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
                gl.glVertex3f(x + cx, y + cy, 0);
                // apply the rotation matrix
                float temp = x;
                x = c * x - s * y;
                y = s * temp + c * y;
            }
            gl.glEnd();
            gl.glPopMatrix();
        }

        @Override
        public void drawCircle(v2 center, float radius, v2 axis, Color3f color) {

            gl.glPushMatrix();
            transformViewport(gl, zero);
            float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
            float c = (float) Math.cos(theta);
            float s = (float) Math.sin(theta);
            float cx = center.x;
            float cy = center.y;
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glColor4f(color.x, color.y, color.z, 1);
            float y = 0;
            float x = radius;
            for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
                gl.glVertex3f(x + cx, y + cy, 0);
                // apply the rotation matrix
                float temp = x;
                x = c * x - s * y;
                y = s * temp + c * y;
            }
            gl.glEnd();
            gl.glBegin(GL.GL_LINES);
            gl.glVertex3f(cx, cy, 0);
            gl.glVertex3f(cx + axis.x * radius, cy + axis.y * radius, 0);
            gl.glEnd();
            gl.glPopMatrix();
        }

        @Override
        public void drawSolidCircle(v2 center, float radius, v2 axis, Color3f color) {

            gl.glPushMatrix();
            transformViewport(gl, zero);
            float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
            float c = (float) Math.cos(theta);
            float s = (float) Math.sin(theta);
            float cx = center.x;
            float cy = center.y;
            gl.glBegin(GL.GL_TRIANGLE_FAN);
            gl.glColor4f(color.x, color.y, color.z, 0.4f);
            float y = 0;
            float x = radius;
            for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
                gl.glVertex3f(x + cx, y + cy, 0);
                // apply the rotation matrix
                float temp = x;
                x = c * x - s * y;
                y = s * temp + c * y;
            }
            gl.glEnd();
            gl.glBegin(GL.GL_LINE_LOOP);
            gl.glColor4f(color.x, color.y, color.z, 1);
            for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
                gl.glVertex3f(x + cx, y + cy, 0);
                // apply the rotation matrix
                float temp = x;
                x = c * x - s * y;
                y = s * temp + c * y;
            }
            gl.glEnd();
            gl.glBegin(GL.GL_LINES);
            gl.glVertex3f(cx, cy, 0);
            gl.glVertex3f(cx + axis.x * radius, cy + axis.y * radius, 0);
            gl.glEnd();
            gl.glPopMatrix();
        }

        @Override
        public void drawSegment(v2 p1, v2 p2, Color3f color) {

            gl.glPushMatrix();
            transformViewport(gl, zero);
            Draw.color(color, gl);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex3f(p1.x, p1.y, 0);
            gl.glVertex3f(p2.x, p2.y, 0);
            gl.glEnd();
            gl.glPopMatrix();
        }

        @Override
        public void drawParticles(v2[] centers, float radius, ParticleColor[] colors, int count) {

            gl.glPushMatrix();
            transformViewport(gl, zero);

            float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
            float c = (float) Math.cos(theta);
            float s = (float) Math.sin(theta);

            float x = radius;
            float y = 0;

            for (int i = 0; i < count; i++) {
                v2 center = centers[i];
                float cx = center.x;
                float cy = center.y;
                gl.glBegin(GL.GL_TRIANGLE_FAN);
                if (colors == null) {
                    gl.glColor4f(1, 1, 1, 0.4f);
                } else {
                    ParticleColor color = colors[i];
                    gl.glColor4b(color.r, color.g, color.b, color.a);
                }
                for (int j = 0; j < NUM_CIRCLE_POINTS; j++) {
                    gl.glVertex3f(x + cx, y + cy, 0);
                    float temp = x;
                    x = c * x - s * y;
                    y = s * temp + c * y;
                }
                gl.glEnd();
            }
            gl.glPopMatrix();
        }


        @Override
        public void drawParticlesWireframe(v2[] centers, float radius, ParticleColor[] colors, int count) {

            gl.glPushMatrix();
            transformViewport(gl, zero);

            float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
            float c = (float) Math.cos(theta);
            float s = (float) Math.sin(theta);

            float x = radius;
            float y = 0;

            for (int i = 0; i < count; i++) {
                v2 center = centers[i];
                float cx = center.x;
                float cy = center.y;
                gl.glBegin(GL.GL_LINE_LOOP);
                if (colors == null) {
                    gl.glColor4f(1, 1, 1, 1);
                } else {
                    ParticleColor color = colors[i];
                    gl.glColor4b(color.r, color.g, color.b, (byte) 127);
                }
                for (int j = 0; j < NUM_CIRCLE_POINTS; j++) {
                    gl.glVertex3f(x + cx, y + cy, 0);
                    float temp = x;
                    x = c * x - s * y;
                    y = s * temp + c * y;
                }
                gl.glEnd();
            }
            gl.glPopMatrix();
        }

        private final v2 temp = new v2();
        private final v2 temp2 = new v2();

        @Override
        public void drawTransform(Transform xf) {

            getWorldToScreenToOut(xf.pos, temp);
            temp2.setZero();

            gl.glBegin(GL.GL_LINES);
            gl.glColor3f(1, 0, 0);

            float k_axisScale = 0.4f;
            temp2.x = xf.pos.x + k_axisScale * xf.c;
            temp2.y = xf.pos.y + k_axisScale * xf.s;
            getWorldToScreenToOut(temp2, temp2);
            gl.glVertex2f(temp.x, temp.y);
            gl.glVertex2f(temp2.x, temp2.y);

            gl.glColor3f(0, 1, 0);
            temp2.x = xf.pos.x + -k_axisScale * xf.s;
            temp2.y = xf.pos.y + k_axisScale * xf.c;
            getWorldToScreenToOut(temp2, temp2);
            gl.glVertex2f(temp.x, temp.y);
            gl.glVertex2f(temp2.x, temp2.y);
            gl.glEnd();
        }

        @Override
        public void drawString(float x, float y, String s, Color3f color) {
//      text.beginRendering(panel.getWidth(), panel.getHeight());
//      text.setColor(color.x, color.y, color.z, 1);
//      text.draw(s, (int) x, panel.getHeight() - (int) y);
//      text.endRendering();
        }
    }
}