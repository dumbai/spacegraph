package spacegraph.space3d.raytrace;

import jcog.Util;
import jcog.data.list.Lst;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.FloatRange;
import jcog.signal.anomaly.ewma.Ewma;
import jcog.signal.wave2d.Bitmap2D;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

import static jcog.Util.unitizeSafe;

public final class RayTracer extends JPanel {

    /** regauge this as a fraction of the screen dimension */
    public final FloatRange grainMin = new FloatRange(0.0005f, 0, 1);
    public final FloatRange grainMax = new FloatRange(1, 0, 1);
    float iterPixels = 1;


    float downSample = 1;



    double fps = 30;

    /** update rate (not alpha chanel) */
    float alphaMin = 0.01f;
    float alphaMax = 0.99f;

    double CAMERA_EPSILON = 0.0001;

    private final Scene scene;
    private final Input input;
    private BufferedImage img;
    private int[] pixelCache;


    private Dimension displaySize;
    private int W;
    private int H;
    private double A;



    public RayTracer(Scene scene) {
        this(scene, new Input());
    }

    private RayTracer(Scene scene, Input input) {
        setIgnoreRepaint(true);

        this.scene = scene;
        this.input = input;
    }

    public static void main(String[] args) {



        Scene scene1 = null;
        try {


            scene1 = new Scene(
                "camera:\n" +
                        "    position: (4, 4, 4)\n" +
                        "    direction: (-1, -1, -1)\n" +
                        "    fov: 90\n" +
                        "    size: 0\n" +
                        "cube:\n" +
                        "    position: (0, 0, 0)\n" +
                        "    sideLength: 10\n" +
                        "    surface: diffuse\n" +
                        "    texture: texture2.jpg\n" +
                        "sphere:\n" +
                        "    position: (2, 0, 0)\n" +
                        "    radius: 1\n" +
                        "    surface: specular\n" +
                        "sphere:\n" +
                        "    position: (-2, 0, 0)\n" +
                        "    radius: 1\n" +
                        "    surface: specular\n" +
                        "sphere:\n" +
                        "    position: (0, 2, 0)\n" +
                        "    radius: 1\n" +
                        "    surface: specular\n" +
                        "sphere:\n" +
                        "    position: (0, -2, 0)\n" +
                        "    radius: 1\n" +
                        "    surface: specular\n" +
                        "sphere:\n" +
                        "    position: (0, 0, -2)\n" +
                        "    radius: 1\n" +
                        "    surface: specular\n" +
                        "sphere:\n" +
                        "    position: (0, 0, 2)\n" +
                        "    radius: 1\n" +
                        "    surface: specular\n" +
                        "sphere:\n" +
                        "    position: (0, 0, 0)\n" +
                        "    radius: 1\n" +
                        "    surface: specular\n" +
                        "light:\n" +
                        "    position: (0, -2, 4)\n" +
                        "    color: ff0000\n" +
                        "light:\n" +
                        "    position: (0, 0, 4)\n" +
                        "    color: 00ff00\n" +
                        "light:\n" +
                        "    position: (0, 2, 4)\n" +
                        "    color: 0000ff\n"
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        RayTracer rayTracer = new RayTracer(scene1);
        rayTracer.run();
    }

    public void run() {
        setSize(input.newSize = new Dimension(640, 480));
        addMouseListener(input);
        addMouseMotionListener(input);
        addComponentListener(input);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setSize(input.newSize);
        frame.setIgnoreRepaint(true);
        frame.add(this);
        frame.setVisible(true);

        frame.addKeyListener(input);


        Lst<Renderer> r = new Lst();
        int sw = 16, sh = 16;
        float dw = 1.0f /sw, dh = 1.0f /sh;
        for (int i = 0; i < sw; i++) {
            for (int j= 0;j < sh; j++) {
                r.add(new Renderer(i * dw, j * dh, (i+1)*dw, (j+1)*dh));
            }
        }

        long sceneTimeNS = Math.round((1.0/fps)* 1.0E9);
        long subSceneTimeNS = sceneTimeNS / r.size();

        while (true) {

            updateCamera();


            //r.parallelStream().forEach(x ->
            r.forEach(x ->
                x.render(subSceneTimeNS)
            );
            repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        if (img != null) {
            int pw = displaySize.width, ph = displaySize.height;
            g.drawImage(img, 0, 0, pw, ph, this);
        }
    }

    /** returns true if camera changed */
    private boolean updateCamera() {
        if (!input.newSize.equals(displaySize)) {
            displaySize = input.newSize;
            resize();
        }

        //grainMin = 1f / (Math.max(W, H) * superSampling.floatValue());

        return scene.camera.update(input, CAMERA_EPSILON);
    }


    class Renderer {
        final Random random = new XoRoShiRo128PlusRandom();
        private final float inPixels;




        final Ewma e;
        //FloatAveragedWindow e = new FloatAveragedWindow(window, 0.5f, 0.5f).mode(FloatAveragedWindow.Mode.Mean);
        //FloatNormalized ee = new FloatNormalized(()->eee, 0, 0.000001f) {
//        static private final float MIN_ERROR = 1f / (3*256);
//
//            @Override
//            public float asFloat() {
//
//                normalizer.min = MIN_ERROR; //HACK
//                float v = super.asFloat();
//                normalizer.min = MIN_ERROR; //HACK
//                return v;
//            }
//
//        };


        final float x1;
        final float y1;
        final float x2;
        final float y2;

        Renderer(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            inPixels = (x2 - x1) * (y2 - y1);
            e = new Ewma().period(2).with(0)
                //(int) (inPixels/4)).with(0)
            ;
        }

        private void render(long sceneTimeNS) {
//            float statRelax = 0.004f;  //determines a passive refresh rate, or fundamental duration

            //TODO trigger total reset when bitmap resized
            //ee.relax(statRelax);

            long start = System.nanoTime();



            //iterPixels = (int)Math.ceil(density * inPixels /(W*H));
            //assert(iterPixels > 0);


            float pixelScale = (float) Math.sqrt((W*W)+(H*H));
            float grainMax = RayTracer.this.grainMax.asFloat() * pixelScale,
                  grainMin = RayTracer.this.grainMin.asFloat() * pixelScale;

//            float w = x2-x1, h = y2-y1;
            //float rad = (float) Math.sqrt(Util.sqr(w*W) + Util.sqr(h*H));

            int iterPixels = (int)Math.ceil(RayTracer.this.iterPixels);

            do {
                float ePrev = e.meanFloat();
                ePrev = ePrev*ePrev;

                float grainScale = Util.lerp(unitizeSafe(ePrev/2),
                    grainMin,
                    grainMax);
                float alpha = Util.lerp( unitizeSafe(ePrev),
                        alphaMin,
                        alphaMax);

                double eNext = renderStochastic(
                        x1 * W, y1 * H, x2 * W, y2 * H,
                        alpha,
                        grainScale, iterPixels);

                this.e.accept(eNext);

            } while (System.nanoTime() - start <= sceneTimeNS);

        }

        /** returns average pixel error */
        private double renderStochastic(float x1, float y1, float x2, float y2, float alpha, float grainPixels, int iterPixels) {

            float a = random.nextFloat() + 0.5f; //0.5..1.5
            float pw = Math.max(1, (float) (Math.sqrt(grainPixels) / a));
            float ph = Math.max(1, (grainPixels)/pw); //grainPixels * a;
//            float W = Math.max(1,(x2-x1) - pw), H = Math.max(1, (y2-y1) - ph);


            int SW = RayTracer.this.W, SH = RayTracer.this.H;
            float sW = x2-x1, sH = y2-y1;
            double eAvgSum = 0;
            int renderedPixels = 0;

            /* recursion depth of each grain */
            int grainDepthMin = 0;
            int grainDepthMax = 1;

            while (iterPixels > renderedPixels) {
                //renderedPixels++; //regardless

//            int xy = random.nextInt() & ~(1 << 31);
//            int x = (xy & Short.MAX_VALUE) % W;
//            int y = (xy >> 16) % H;
                float x = x1 + random.nextFloat() * sW;
                float y = y1 + random.nextFloat() * sH;

                float sx1 = Util.clampSafe(x, 0, SW - 1);
                float sx2 = Util.clampSafe(x + pw, 0, SW - 1);
                if (sx1<sx2) {
                    float sy1 = Util.clampSafe(y, 0, SH - 1);
                    float sy2 = Util.clampSafe(y + ph, 0, SH - 1);
                    if (sy1 < sy2) {

                        double e = renderRecurse(alpha,
                            Util.clamp((int) Math.floor(Math.log(grainPixels * grainPixels) / Math.log(2)), grainDepthMin, grainDepthMax),
                            sx1, sy1, sx2, sy2
                        );
                        int renderedArea = Math.max(1, (int) ((sy2 - sy1) * (sx2 - sx1)));
                        renderedPixels += renderedArea;
                        eAvgSum += e * renderedArea;
                    }
                }
            }
            return eAvgSum / renderedPixels;
        }


        double renderRecurse(float alpha, int depth, float x1, float y1, float x2, float y2) {
            if (depth == 0) {
                return render(alpha, x1, y1, x2, y2);
            } else {
                depth--;
                float my = (y2 - y1) / 2.0f + y1;
                float mx = (x2 - x1) / 2.0f + x1;
                return
                    renderRecurse(alpha, depth, x1, y1, mx, my) +
                    renderRecurse(alpha, depth, mx, y1, x2, my) +
                    renderRecurse(alpha, depth, x1, my, mx, y2) +
                    renderRecurse(alpha, depth, mx, my, x2, y2);
            }
        }


        /** returns sum of pixel errors */
        private double render(float alpha, float fx1, float fy1, float fx2, float fy2) {

            double mx = (fx1 + fx2)/ 2.0f;
            double my = (fy1 + fy2)/ 2.0f;

            float subPixelScatter = 0.0f;
            if(subPixelScatter >0) {
                mx += subPixelScatter * (0.5f + (random.nextFloat() - 0.5f) * (fx2 - fx1));
                my += subPixelScatter * (0.5f + (random.nextFloat() - 0.5f) * (fy2 - fy1));
            }

            int color = color(mx, my);


            int ww = Math.max(1, Math.round(fx2 - fx1));
            int W = RayTracer.this.W;
            int[] pixelCache = RayTracer.this.pixelCache;

            long delta = 0;
//            int pixels = 0;
            int x1 = (int)(fx1); int y1 = (int)(fy1); int y2 = (int)(fy2);// int x2 = (int)(fx2)+1;
//            if (x2 < x1 || y2 < y1)
//                return 0;

            for (int y = y1; y <= y2; y++) {
                int start = y * W + x1;
                int end = start + ww;
                for (int i = start; i < end; i++) {
                    int current = pixelCache[i];
                    int d = colorDelta(current, color);
                    if (d == 0)
                        continue;

                    delta += d;
                    pixelCache[i] = blend(current, color, alpha);
                }
                //pixels+=ww;
            }
            return delta / (3.0 * 256 * (y2-y1+1)*ww);
        }
    }






    private void resize() {
        BufferedImage nextImg = new BufferedImage((int) (displaySize.getWidth()/downSample), (int) (displaySize.getHeight()/downSample),
            BufferedImage.TYPE_INT_RGB);
        nextImg.setAccelerationPriority(1.0f);
        W = nextImg.getWidth();
        H = nextImg.getHeight();
        A = ((double)W)/H;
        img = nextImg;
        pixelCache = ((DataBufferInt) (nextImg.getRaster().getDataBuffer())).getData();
    }


//    private void render(int depth) {
//
//
//
//        final int d = depth - 1;
//
//        render(d, 0, 0, W, H);
//
//
//    }



    private static int blend(int current, int next, float alpha) {
        if (alpha >= 1 - (1/ 256.0f))
            return next;

        //TODO
        float cr = Bitmap2D.decode8bRed(current), nr = Bitmap2D.decode8bRed(next);
        float cg = Bitmap2D.decode8bGreen(current), ng = Bitmap2D.decode8bGreen(next);
        float cb = Bitmap2D.decode8bBlue(current), nb = Bitmap2D.decode8bBlue(next);
        return Bitmap2D.encodeRGB8b(
                Util.lerpSafe(alpha, cr, nr),
                Util.lerpSafe(alpha, cg, ng),
                Util.lerpSafe(alpha, cb, nb)
        );
    }


    /** in 8-bit color components, ie. white -> black = 3 * 256 difference */
    static int colorDelta(int a, int b) {
        if (a == b) return 0;
        else return
            Math.abs( Util.intByte(a, 2) - Util.intByte(b, 2)) +
            Math.abs( Util.intByte(a, 1) - Util.intByte(b, 1)) +
            Math.abs( Util.intByte(a, 0) - Util.intByte(b, 0));
    }

    private int color(double x, double y) {
        return scene.rayColor(scene.camera.ray(
                x / W,
                1 - y / H,
                A
        ));
    }

}