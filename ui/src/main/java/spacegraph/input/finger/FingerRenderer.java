package spacegraph.input.finger;

import com.jogamp.opengl.GL2;
import jcog.math.v2;
import jcog.signal.anomaly.ewma.Ewma;
import spacegraph.video.Draw;

import static com.jogamp.opengl.GL.GL_COLOR_LOGIC_OP;
import static com.jogamp.opengl.GL.GL_EQUIV;

/** cursor renderer */
@FunctionalInterface public interface FingerRenderer {

    void paint(v2 posPixel, Finger finger, float dtS, GL2 gl);

    FingerRenderer rendererCrossHairs1 = new FingerRenderer() {

        final v2 lastPixel = new v2(0,0);
        final Ewma speedFilter = new Ewma(0.25f, 0.1f);

        @Override
        public void paint(v2 posPixel, Finger finger, float dtS, GL2 gl) {


            float smx = posPixel.x, smy = posPixel.y;

            float speed = lastPixel.distance(posPixel);

            float cw = (float)(32.0f + speedFilter.acceptAndGetMean(speed));

            gl.glPushMatrix();

            {

                gl.glEnable(GL_COLOR_LOGIC_OP);
                gl.glLogicOp(
                        //GL_XOR
                        //GL_INVERT
                        //GL_OR_INVERTED
                        GL_EQUIV
                );

                gl.glTranslatef(smx, smy, 0);


                gl.glColor4f(0.75f, 0.75f, 0.75f, 0.9f);

                gl.glLineWidth(2.0f);
                //Draw.rectStroke(gl, smx - cw / 2f, smy - ch / 2f, cw, ch);
                float theta = (posPixel.x + posPixel.y) / 100.0f;
                Draw.poly(6 /* 6 */, cw / 2, theta, false, gl);

                gl.glColor4f(0.5f, 0.5f, 0.5f, 0.75f);
                gl.glLineWidth(3.0f);
                gl.glColor4f(0.5f, 0.5f, 0.5f, 0.75f);
                float ch = cw;
                Draw.linf(0, -ch, 0, -ch / 2, gl);
                Draw.linf(0, ch / 2, 0, ch, gl);
                Draw.linf(-cw, 0, -cw / 2, 0, gl);
                Draw.linf(cw / 2, 0, cw, 0, gl);

                gl.glLineWidth(1.0f);
                gl.glColor4f(0.1f, 0.1f, 0.1f, 0.5f);
                Draw.rect( -cw/16, -ch/16, cw/16, ch/16, gl);
                //Draw.poly(3, cw / 10, -theta, false, gl);

                gl.glDisable(GL_COLOR_LOGIC_OP);
            }
            gl.glPopMatrix();

            lastPixel.set(posPixel);
        }
    };

    /** virtua cop */
//    FingerRenderer polygon1 = new PolygonCrosshairs();

    class PolygonCrosshairs implements FingerRenderer {

        float angle = 0.0f;
        float alpha = 0.35f;
        float lineWidth = 4;
        float rad = 32.0f;

        float pixelDistSq = 0;
        final v2 lastPixel = new v2();
        final Ewma smoothedRad = new Ewma(0.5, 0.05f);
        double timeMS = 0;

        @Override
        public void paint(v2 posPixel, Finger finger, float dtS, GL2 gl) {

            float smx = posPixel.x, smy = posPixel.y;

            pixelDistSq = lastPixel.distanceSq(posPixel);
            lastPixel.set(posPixel);

            timeMS += dtS*1000;

            float freq = 8.0f;
            float phaseSec = (float) Math.sin(freq * timeMS / (2 * Math.PI * 1000));

            gl.glPushMatrix();
            {
                gl.glTranslatef(smx, smy, 0);
                gl.glRotatef(angle, 0, 0, 1);

                if (finger.pressed(0)) {
                    gl.glColor4f(0.5f, 1, 0.5f, alpha);
                } else if (finger.pressed(2)) {
                    gl.glColor4f(0.5f, 0.5f, 1.0f, alpha);
                } else {
                    gl.glColor4f((phaseSec * 0.5f) + 0.5f, 0.25f, ((1-phaseSec) * 0.5f) + 0.5f, alpha);
                }

                float r = (float) smoothedRad.acceptAndGetMean(this.rad + (pixelDistSq / 50));
                renderOutside(r, gl);
                renderInside(r, gl);
            }
            gl.glPopMatrix();
        }

        protected static void drawTri(float rad, GL2 gl) {
            float w = rad/2;
            float x1 = rad * 0.5f;
            float x2 = rad * 1.0f;
            Draw.trif(gl, x1, -w/2, x1, +w/2,   x2, 0);
        }

        protected void renderInside(float rad, GL2 gl) {
            float radh = rad * 0.75f;
            if (renderHorizontal())
                Draw.linf(0, -radh, 0, +radh, gl);
            if (renderVertical())
                Draw.linf(-radh, 0, +radh, 0, gl);
        }

        /** whether to render the internal crosshair dimension */
        protected boolean renderVertical() { return true; }

        /** whether to render the internal crosshair dimension */
        protected boolean renderHorizontal() { return true; }


        protected void renderOutside(float rad, GL2 gl) {
            gl.glLineWidth(lineWidth);
            Draw.poly(8, rad, false, gl);
        }

        public FingerRenderer angle(float a) {
            this.angle = a;
            return this;
        }
    }
    class PolygonWithArrow extends PolygonCrosshairs {

        final float arrowAngle;

        public PolygonWithArrow(float arrowAngle) {
            this.angle = arrowAngle;
            this.arrowAngle = arrowAngle;
        }

        @Override
        protected void renderInside(float rad, GL2 gl) {
            super.renderInside(rad, gl);

            drawTri(rad, gl);
        }
    }

    /** TODO arrowheads */
    FingerRenderer rendererResizeNS = new PolygonCrosshairs() {
        @Override
        protected boolean renderVertical() {
            return false;
        }
    };
    /** TODO arrowheads */
    FingerRenderer rendererResizeEW = new PolygonCrosshairs() {
        @Override
        protected boolean renderHorizontal() {
            return false;
        }
    };

}