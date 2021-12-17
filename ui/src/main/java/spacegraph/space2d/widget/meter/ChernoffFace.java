package spacegraph.space2d.widget.meter;

import com.jogamp.opengl.GL2;
import spacegraph.SpaceGraph;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.AspectAlign;
import spacegraph.space2d.widget.slider.FloatSlider;
import spacegraph.video.Tex;
import spacegraph.video.TexSurface;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.pow;

/**
 * TODO
 * https://images.all-free-download.com/images/graphiclarge/cute_animal_faces_cartoon_kids_drawing_6838379.jpg
 * https://i.pinimg.com/originals/d5/36/10/d5361017aaa18078462c1373871d6e89.jpg
 * https://thumbs.dreamstime.com/z/seamless-animal-face-pattern-18917561.jpg
 * https://i.ytimg.com/vi/efCOU4ELrx8/maxresdefault.jpg
 */
abstract public class ChernoffFace {

//    protected abstract void xCircle(double x, double y, double radius);

    protected abstract void xOval(double x, double y, double height_r, double width_r);

    protected abstract void xFillOval(double x, double y, double height_r, double width_r);

    protected abstract void xLine(double x1, double y1, double x2, double y2);

    public static class ChernoffFaceG2D extends ChernoffFace {

        private final Graphics g;

        public ChernoffFaceG2D(Graphics g) {
            this.g = g;
        }

//        /** Draws a scaled and translated circle. */
//        @Override protected void xCircle(double x, double y, double radius) {
//            g.drawOval(sx(x - radius) + dx, sy(y - radius) + dy,
//                    sx(radius * 2), sy(radius * 2));
//        }

        /** Draws a scaled and translated oval. */
        @Override protected void xOval(double x, double y, double height_r, double width_r) {
            g.drawOval(sx(x - width_r) + dx, sy(y - height_r) + dy,
                    sx(width_r * 2), sy(height_r * 2));
        }

        /** Draw a scaled, translated and filled oval. */
        @Override protected void xFillOval(double x, double y, double height_r, double width_r) {
            g.fillOval(sx(x - width_r) + dx, sy(y - height_r) + dy,
                    sx(width_r * 2), sy(height_r * 2));
        }

        /** Draws a scaled and translated line. */
        @Override protected void xLine(double x1, double y1, double x2, double y2) {
            g.drawLine(sx(x1) + dx, sy(y1) + dx,
                    sx(x2) + dx, sy(y2) + dy);
        }


    }

    public static class ChernoffFaceGL extends ChernoffFace {
        private GL2 gl;

        public void gl(GL2 gl) {
            this.gl = gl;
        }

        @Override
        protected void xOval(double x, double y, double height_r, double width_r) {

        }

        @Override
        protected void xFillOval(double x, double y, double height_r, double width_r) {

        }

        @Override
        protected void xLine(double x1, double y1, double x2, double y2) {

        }
    }

    /** Various parameters that adjust the appearance of the face. */
    private int head_radius = 30;
    private int eye_radius = 5;
    private int eye_left_x = 40;
    private int eye_right_x = 60;
    private int eye_y = 40;
    public double pupil_radius = 0.2;
    private int eyebrow_l_l_x = 35;
    private int eyebrow_r_l_x = 55;
    private int eyebrow_l_r_x = 45;
    private int eyebrow_r_r_x = 65;
    private int eyebrow_y = 30;
    private int nose_apex_x = 50;
    private int nose_apex_y = 45;
    public int nose_height = 16;
    private int nose_width = 8;
    private int mouth_y = 65;

    // Used for scaling and translating face.
    protected double sx, sy;
    protected int dx, dy;

    /** Draws a Chernoff face.

     This code draws the face into a logical space with dimensions 100x100, and
     scales it to the actual size specified by width and height.

     @param    g      The Graphics context to draw the face into.
     @param    v      The FaceVector describing the face to draw.
     @param    width  The width of the Graphics context.
     @param    height The height of the Graphics context.
     */
    public void draw(double[] p, int x, int y, int width, int height) {
        sx = width/100;
        sy = height/100;
        dx = x;
        dy = y;
        draw_head(p[1]);
        draw_eye(p[2], p[7], p[8]);
        draw_pupil(p[3], p[7]);
        draw_eyebrow(p[4]);
        draw_nose(p[5]);
        draw_mouth(p[6], p[9], p[10]);
    }


    private void draw_head(double p1) {
        int[] e;

        e = eccentricities(p1);
        xOval(50, 50, head_radius + e[0], head_radius + e[1]);
    }


    private void draw_eye(double p2, double p7, double p8) {
        int[] e;
        int eye_spacing = (int)((p7 - 0.5) * 10);
        int eye_size = (int)(((p8 - 0.5) / 2.0) * 10);
        e = eccentricities(p2);

        xOval(eye_left_x - eye_spacing, eye_y, eye_radius + eye_size + e[0], eye_radius + eye_size + e[1]);
        xOval(eye_right_x + eye_spacing, eye_y, eye_radius + eye_size + e[0], eye_radius + eye_size + e[1]);
    }


    private void draw_pupil(double p3, double p7) {
        int pupil_size = (int)(Math.max(1, p3 * 0.2) * 2);

        xFillOval(eye_left_x - (int)((p7 - 0.5) * 10), eye_y, pupil_size, pupil_size);
        xFillOval(eye_right_x + (int)((p7 - 0.5) * 10), eye_y, pupil_size, pupil_size);
    }


    private void draw_eyebrow(double p4) {
        int y1 = eyebrow_y + (int)((p4 - 0.5) * 10);
        int y2 = eyebrow_y - (int)((p4 - 0.5) * 10);

        xLine(eyebrow_l_l_x, y1, eyebrow_l_r_x, y2);
        xLine(eyebrow_r_l_x, y2, eyebrow_r_r_x, y1);
    }


    private void draw_nose(double p5) {
        int y = 55 + (int)(((p5 - 0.5) / 2.0) * 10);

        xLine(nose_apex_x, nose_apex_y, nose_apex_x - (nose_width / 2), y);
        xLine(nose_apex_x - (nose_width / 2), y, nose_apex_x + (nose_width / 2), y);
        xLine(nose_apex_x + (nose_width / 2), y, nose_apex_x, nose_apex_y);
    }


    private void draw_lip(double x1, double y1, double x2, double y2, double x3, double y3) {
        double i, new_x, new_y, last_x, last_y;

        // This is some nasty parabolic stuff.  It doesn't look that good because of the stupid
        // way we scale to non- 100x100 displays.
        double denom = (pow(x1, 2) * (x2 - x3))
                +  (x1 * (pow(x3, 2) - pow(x2, 2)))
                +  (pow(x2, 2) * x3)
                + -(pow(x3, 2) * x2);

        double a     = ((y1 * (x2 - x3))
                +  (x1 * (y3 - y2))
                +  (y2 * x3)
                + -(y3 * x2))
                / denom;

        double bb    = ((pow(x1, 2) * (y2 - y3))
                +  (y1 * (pow(x3, 2) - pow(x2, 2)))
                +  (pow(x2, 2) * y3)
                + -(pow(x3, 2) * y2))
                / denom;

        double c     = ((pow(x1, 2) * ((x2 * y3) - (x3 * y2)))
                +  (x1 * ((pow(x3, 2) * y2) - (pow(x2, 2) * y3)))
                +  (y1 * ((pow(x2, 2) * x3) - (pow(x3, 2) * x2))))
                / denom;

        for(i = x1, last_x = x1, last_y = y1; i <= x2; i += 1.0 / sx) {
            new_x = i;
            new_y = ((a * pow(i, 2)) + (bb * i) + c);
            xLine(last_x, last_y, new_x, new_y);
            last_x = new_x;
            last_y = new_y;
        }
    }


    private void draw_mouth(double p6, double p9, double p10) {
        double mouth_size = ((p9 - 0.5) * 10);
        double x1 = 40 - mouth_size;
        double y1 = mouth_y;
        double x2 = 60 + mouth_size;
        double y2 = mouth_y;
        double x3 = ((x2 - x1) / 2) + x1;
        double y3 = ((p6 - 0.5) * 10) + mouth_y;

        draw_lip(x1, y1, x2, y2, x3, y3);
        draw_lip(x1, y1, x2, y2, x3, y3 + ((p10 / 2.0) * 10));
    }



    protected int sx(double x) {
        return (int) Math.round(x(x));
    }
    protected int sy(double y) {
        return (int) Math.round(y(y));
    }

    protected double x(double x) {
        return (x * sx);
    }

    protected double y(double y) {
        return (y * sy);
    }



    /** Takes a number between 0 and 1 and returns a 2-vector that should be added to the
     dimensions of a circle to create an oval. */
    private int[] eccentricities(double p) {
        int[] a = new int[2];

        if (p > .5) {
            a[0] = (int)((p - 0.5) * 20.0);
            a[1] = 0;
            return a;
        } else {
            a[0] = 0;
            a[1] = (int)(Math.abs(p - 0.5) * 20.0);
            return a;
        }
    }

    public static void main(String[] args) {
        SpaceGraph.window(new FaceSurface(), 1024, 800);
    }

    public static class FaceSurface extends Bordering {

        final static int dims = 11;
        final static int pw = 512, ph = 512;
        private final BufferedImage i;
        private final TexSurface face;
        private final FloatSlider[] s;
        double[] d = new double[dims];
        private final AtomicBoolean update = new AtomicBoolean(false);
        private final Gridding controls = new Gridding();

        static final private boolean debug = false;

        Color bgColor = Color.BLACK;
        Color fgColor = Color.ORANGE;

        public FaceSurface() {
            s = new FloatSlider[dims];
            Arrays.fill(d, 0.5f);
            for (int i = 0; i < dims; i++) {
                int I = i;
                controls.add(s[I] = new FloatSlider((float) d[I], 0, 1).on(x -> {
                    if (debug)
                        System.out.println(I + " " + x);
                    d[I] = x;
                    update.set(true);
                }));
            }
            west(controls);

            i = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
            face = Tex.view(i);
        }

        @Override
        protected void starting() {
            super.starting();
            set(new AspectAlign(face, 1));
            repaint();
            //update.set(true);
        }

        public void set(int dim, float v) {
            if (!Float.isFinite(v)) v = 0.5f; //HACK
            s[dim].set(v);
        }

        @Override
        protected void paintIt(GL2 gl, ReSurface r) {
            if (update.getAndSet(false)) {
                repaint();
            }

            super.paintIt(gl, r);
        }

        private synchronized void repaint() {
            Graphics2D g = (Graphics2D) i.getGraphics();
            int pw = i.getWidth(), ph = i.getHeight();

            g.setColor(bgColor);
            g.fillRect(0, 0, pw, ph);

            g.setColor(fgColor);
            g.setStroke(new BasicStroke(4));
            new ChernoffFaceG2D(g).draw(d, 0, 0, pw, ph);
            face.set(i);

        }

    }
}