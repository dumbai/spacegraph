package spacegraph.space2d.widget.meter;


import jcog.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;


public class HumanoidFacePanel extends JPanel implements Runnable, KeyListener, MouseMotionListener, MouseListener {


    private static final int POLYGON = 0, POLYLINE = 1, CIRCLE = 2;

    private double t;
    private int cycle;

    private final long start = System.currentTimeMillis();

    private final int pupil, eyeBall;

    long cycleDelay = 30;

    double momentum = 0.95;

    double nextNod;

    private boolean mouseMove, mouseDown, mouseDrag, mouseUp, mouseIsDown;


    private boolean firstVertices, isVectors;
    private boolean addNoise = true, doShade = false;
    private final boolean shiftAxis;
    private final int nVertices;
    private int nShapes;
    private int nFlexes;
    private final int[] keyFlex = new int[256];
    private final double[] keyValue = new double[256];
    private double[][] flexValue;
    private double[][] flexTarget;
    private double[] jitterAmpl;
    private double[] jitterFreq;
    private boolean firstTime;
    private boolean kludge;

    private double blinkValue;
    private double spin;
    private int noRotateIndex;
    private boolean leftArrow;
    private boolean rightArrow;
    private int prevKey;
    private long lastTime;
    private int frameCount;
    private int dispCount;

    private double[][][] flexData;
    private int[][] flexShape;
    private int[] flexSymmetry;
    private final double[][][] pts;
    private int[][][] shape;
    private double[][] vertexArray;
    private final Vector<Color> colorVector = new Vector();
    private final Vector<String> flexNamesVector = new Vector();
    private final Vector<Vector<Vector>> flexVector = new Vector();
    private Vector<Vector> flx;
    private Vector<Double> ixyz;
    private final Vector<Integer> flexSymmetryVector = new Vector();
    private final Vector<Vector<Vector<Integer>>> shapesVector = new Vector();
    private Vector<Vector<Integer>> shapeVector;
    private Vector<Integer> face;
    private final Vector<Integer> typeVector = new Vector();
    private final Vector<Double> vertexVector = new Vector();

    private BufferedImage img;
    private Graphics buffer;

    private Thread thread;

    private int key;
    private boolean keyDown;

    private int width, height;
    private boolean resizing;
    private final boolean debugging;

    private boolean mouseLeft, mouseMiddle, mouseRight;
    private int mousex, mousey;
    private int mouselx, mousely;
    private Date mouseTime;
    private MouseEvent mouseEvent;

    private Rectangle r = new Rectangle(0, 0, 0, 0);
    private Color bgcolor = Color.white;
    private final Color fgcolor = Color.black;
    private int loX, loY;
    private int hiX, hiY;
    //    Kernel kernel = new Kernel(3, 3,
//            new float[]{
//                    1f / 9f, 1f / 9f, 1f / 9f,
//                    1f / 9f, 1f / 9f, 1f / 9f,
//                    1f / 9f, 1f / 9f, 1f / 9f});
//    BufferedImageOp op = new ConvolveOp(kernel);
    private final int[] ai = new int[100];
    private final int[] ai1 = new int[100];
    private final int[] ai2 = new int[100];
    private double nextSpin;
    private int talk = -1;

    /** <= 13 */
    private float eyeballSize = 13;

    private float pupilSize = 8;

    private boolean unhappy, happy, nod, shake;

    private HumanoidFacePanel() {
        super(new BorderLayout());

        setIgnoreRepaint(true);

        this.img = null;
        this.buffer = null;
        this.mouseMove = false;
        this.mouseDown = false;
        this.mouseDrag = false;
        this.mouseUp = false;
        this.keyDown = false;
        this.mouseIsDown = false;
        this.resizing = true;
        this.debugging = false;
        this.mouseTime = null;
        this.mouseEvent = null;
        firstVertices = true;
        isVectors = false;
        shiftAxis = false;
        firstTime = true;
        kludge = true;
        noRotateIndex = 1000;
        leftArrow = false;
        rightArrow = false;
        prevKey = -1;
        setBackground(Color.white);
        lastTime = System.currentTimeMillis();
        frameCount = 0;
        dispCount = 0;

        vertices("14,99,0");

        vertices("27,85,0        30,70,0    15,30,-12  19,31.5,10  6.1,16.8,44.5"); //  1- 5 face
        vertices("5,70,44        12,72,42   22,68,35    12,70,42  5,68,44.5");    //  6-10 brows

        vertices("6,60,43.7      12,62,44   19,60,38    12,58,43  2,45,52");      // 11-15 eyes/nose
        vertices("5,44,47        1,42.5,49  4,38,50     8,30.2,46 3,23.5,46");    // 16-20 nose/lips

        vertices("2.1,35,48      5,31,47    1.5,29,47   12,60,37  14.1,62.1,37"); // 21-25 mouth/eyeballs
        vertices("13.15,61.15,37 7,34,44    7,29.2,42   4,47,48   26.5,55,14");   // 26-30 teeth,nostrils,cheeks,hair

        vertices("28,30,0        12,95,27    22,80,24  12,99,13  16,36,33");     // 31-35 forehead,face,headtop
        vertices("2,48.5,52.5    5,20.7,46.5 3,23,45  15,36,39  22,51,37");     // 36-40 misc

        vertices("13.6,26,38     10,84,34    6,22,20    22,35,15  12,97,-5");     // 41-45
        vertices("15,91,-17      18,80,-26   16,60,-29  10,40,-23 30,52,-4");     // 46-50

        vertices("16,86,39     20,-40,-23  23.3,51.3,7   26.5,50.7,7 23,59,9"); // 51-55
        vertices("26,61,7      27.6,59.5,7");                                     // 56-57
        noRotate();
        vertices("13,50,0      16,16,0 40,7,0 40,0,0"); // 58-61 neck,shoulders

        polygons(.1, 0, 0, "58,59 59,60 60,61");
        polygons(0, 0, 0, "15,16,17");
        polygons(0, 0, 0, "39,41");
        polygons(.56, .56, .56, "11,29,36");
        polygons(.5, .5, .5, "15,29,16");
        polygons(0, 0, 0, "3,49 49,48 48,47 47,46 46,45 1,45,46 1,46,47 50,47,48 48,49,3 30,3,4");
        polygons(0, 0, 0, "34,32 34,1,33,32 1,30,33 45,34 34,45,1 50,1,47 50,48,3 50,3,30 50,30,1");
        polygons(0, 0, 0, "32,51 51,42 32,33,51 51,33,42");
        circles(.56, .56, .56, "53,54");
        polygons(.56, .56, .56, "54,53,55,56,57");
        polygons(.50, .50, .50, "54,53,55,57");
        polygons(0, 0, 0, "18,27 28,20");
        polygons(0, 0, 0, "11,12,13,14");
        eyeBall = circles(.9, .9, .9, "24,25");
        pupil = circles(0, 0, 0, "24,26");
        polygons(.56, .56, .56, "16,29,11");
        polygons(.50, .50, .50, "13,12,9,8 12,11,10,9 5,43 5,4,43");
        polygons(0, 0, 0, "6,7,9,10 7,8,9");
        polygons(.56, .56, .56, "39,40,41 4,41,40 4,5,41 5,37,41 37,19,41 19,39,41 19,37,38,20");
        polygons(.56, .56, .56, "6,10 10,11 11,36 36,15 17,18 20,38 38,37 37,5");
        polygons(.56, .56, .56, "17,16,18 29,15,36");
        polygons(.56, .56, .56, "42,6 42,7,6 42,33,8,7");
        polygons(.56, .56, .56, "30,4,40 30,40,8 13,8,40 8,33,30 11,14,16");
        polygons(.56, .56, .56, "40,16,14 13,40,14 40,39,16 16,39,18 18,39,19");
        polygons(.4, .4, .45, "15,17 18,21 23,20 18,19,22,21 23,22,19,20");
        polylines(0, 0, 0, "11,12,13,14,11");

        flex("sayAh", "19 20 21 22 23 4 5 28 16 37 38 39 41",
                "0,-3,-.6 0,-6,-1.2 0,.3 -1,-2.8,-.6 .6,-5.9,-1.2 1,-1.5,-.3 0,-4,-.8 0,-4.8,-1 .2,-.3 0,-5,-.8 0,-5,-1 0,-1,-.2 0,-5,-1");
        flex("blink", "12 14", "0,-2 0,2");
        flex("brows", "6 7 8 9 10 13 1 2 42", "-1,4 0,1 0,-2.3 0,1 0,4 0,-1 .4,.2 0,-1 -.5,1");
        flex("lookX", "24 25 26", "2.8 3 3");
        assymetric();
        flex("lookY", "24 25 26", "0,2 0,2 0,2");
        flex("lids", "12 14 60", "0,2 0,2 0,3");
        flex("sayOo", "18 19 20 21 22 23 3 4 18 27 28 39 41",
                ".5,.2 4,-.5,2 .5 1.6 4.7,.2,1 1.6 .2 .25,.4 .4 4 4 3 2");
        flex("smile", "19 21 22 23 4 27 28 39 41", "0,4.2 -.8,1 -.8,2.6 -.8,1 0,1 0,1 0,.7 0,3 0,2");
        flex("sneer", "18 21 16 17 29 27 39", "-1.5,2 -1.5,2 -.2,1 -.3 0,.8 0,.6 0,.4");

        map("'", "brows", 1);
        map("_", "brows", 0);
        map("`", "brows", -1);
        map("@", "blink", -.9);
        map("+", "blink", 0);
        map("=", "blink", .5);
        map("-", "blink", 1);
        map("t", "lids", 1);
        map("c", "lids", 0);
        map("b", "lids", -1);

        map("l", "lookX", -1);
        map(".", "lookX", 0);
        map("r", "lookX", 1);
        map("u", "lookY", 1);
        map(":", "lookY", 0);
        map("d", "lookY", -1);
        map("<", "turn", 1);
        map("!", "turn", 0);
        map(">", "turn", -1);
        map("^", "nod", 1);
        map("~", "nod", 0);
        map("v", "nod", -1);
        map("\\", "tilt", -1);
        map("|", "tilt", 0);
        map("/", "tilt", 1);

        map("m", "sayAh", -1);
        map("e", "sayAh", -.5);
        map("o", "sayAh", 0);
        map("O", "sayAh", .5);
        map("P", "sayOo", .8);
        map("p", "sayOo", .4);
        map("a", "sayOo", 0);
        map("w", "sayOo", -.4);
        map("W", "sayOo", -.7);
        map("S", "smile", 1.5);
        map("s", "smile", 1);
        map("n", "smile", 0);
        map("f", "smile", -.7);
        map("i", "sneer", -.5);
        map("x", "sneer", 0);
        map("h", "sneer", .5);
        map("z", "sneer", 1);


        nVertices = vertexVector.size() / 3;
        pts = new double[2][nVertices][3];

    }

    public static void main(String[] arg) {
        HumanoidFacePanel f = new HumanoidFacePanel();
        JFrame w = new JFrame();
        w.setContentPane(f);
        w.setSize(250, 400);
        w.setVisible(true);

        w.addKeyListener(f);
        w.addMouseListener(f);
        w.addMouseMotionListener(f);


        f.start();

    }

    private static int area(int[] ai, int[] ai1, int i) {
        int j = 0;
        for (int l = 0; l < i; l++) {
            int k = (l + 1) % i;
            j += (ai[l] - ai[k]) * (ai1[l] + ai1[k]);
        }

        return j / 2;
    }

    private static double pulse(double d, double d1) {
        return (d - (int) d >= d1 ? 0 : 1);
    }

    private static int getShade(int[] ai, int[] ai1, int[] ai2) {
        int i = ai[1] - ai[0];
        int j = ai1[1] - ai1[0];
        int k = ai2[1] - ai2[0];
        int l = ai[2] - ai[1];
        int i1 = ai1[2] - ai1[1];
        int j1 = ai2[2] - ai2[1];
        int k1 = j * j1 - k * i1;
        int l1 = k * l - i * j1;
        int i2 = i * i1 - j * l;
        int j2 = (k1 - l1 / 2) + i2;
        j2 = (8 * j2 * j2) / (k1 * k1 + l1 * l1 + i2 * i2);
        return 192 + 8 * j2;
    }

    public static void drawArrow(Graphics g, int i, int j, int k, int l, int i1) {
        int j1 = k - i;
        int k1 = l - j;
        int l1 = (int) Math.sqrt((j1 * j1 + k1 * k1) + 0.5D);
        if (l1 > 0) {
            int i2 = (-k1 * i1) / l1 / 2;
            int j2 = (j1 * i1) / l1 / 2;
            int k2 = k - 3 * j2;
            int l2 = l + 3 * i2;
            int[] ai = {
                    i - i2, i + i2, k2 + i2, k2 + 3 * i2, k, k2 - 3 * i2, k2 - i2
            };
            int[] ai1 = {
                    j - j2, j + j2, l2 + j2, l2 + 3 * j2, l, l2 - 3 * j2, l2 - j2
            };
            g.fillPolygon(ai, ai1, 7);
        }
    }

    public static void drawBar(Graphics g, int i, int j, int k, int l, int i1) {
        int j1 = k - i;
        int k1 = l - j;
        int l1 = (int) Math.sqrt((j1 * j1 + k1 * k1) + 0.5D);
        if (l1 > 0) {
            int i2 = (-k1 * i1) / l1 / 2;
            int j2 = (j1 * i1) / l1 / 2;
            if (i2 == 0 && j2 == 0)
                if (j1 * j1 > k1 * k1)
                    j2 = 1;
                else
                    i2 = 1;
            int[] ai = {
                    i - i2, i + i2, k + i2, k - i2
            };
            int[] ai1 = {
                    j - j2, j + j2, l + j2, l - j2
            };
            g.fillPolygon(ai, ai1, 4);
        }
    }

    public static void drawCircle(Graphics g, int i, int j, int k) {
        g.drawOval(i - k, j - k, 2 * k, 2 * k);
    }

    public static void fillCircle(Graphics g, int i, int j, int k) {
        g.fillOval(i - k, j - k, 2 * k, 2 * k);
    }


    private void update(double t) {


        if (talk == 0 || talk == -1) {
            setFlex('m');
            talk = -1;
        }
        if (talk != -1) {
            talk--;
            setFlex('o');
        }

        setFlex('_'); //neutral brows
        setFlex('a');

        if (nod && shake) {
            //confused
            setFlex('\'');
            setFlex('~');
            setFlex('P');
            nextSpin = 0;
        } else if (shake) {
            nextSpin = Math.sin(t * 4f) * 6f;
        } else if (nod) {
            setFlex(Math.sin(t * 6f) < 0 ? 'v' : '^');
        } else {
            setFlex('~');
            nextSpin = 0;
        }

        if (unhappy) {
            setFlex('z');
            setFlex('`');
            setFlex('b');
            setFlex('u');
        } else {
            setFlex('x');
            setFlex('t');
            setFlex(':');
            setFlex('_');
            setFlex('c');
        }

        if (happy) {
            setFlex('S');
        } else {
            setFlex('n');
        }
    }

    public void setPupil(float radius, float r, float g, float b, float a) {
        colorVector.set(pupil, new Color(r, g, b, a));
        pupilSize = radius;
    }

    public void setEyeball(float radius, float r, float g, float b, float a) {
        colorVector.set(eyeBall, new Color(r, g, b, a));
        eyeballSize = radius;
    }

    private void map(String s, String s1, double d) {
        for (int i = 0; i < flexNamesVector.size(); i++) {
            if (flexNamesVector.elementAt(i).equals(s1)) {
                char c = s.charAt(0);
                keyFlex[c] = i;
                keyValue[c] = d;
                return;
            }
        }

    }

    public void toggleShade() {
        doShade = isVectors || !doShade;
        if (isVectors)
            toggleVectors();
    }

    private void toggleNoise() {
        addNoise = !addNoise;
    }

    private void toggleVectors() {
        isVectors = !isVectors;
    }

    private void noRotate() {
        noRotateIndex = vertexVector.size() / 3;
    }

    private int polygons(double d, double d1, double d2, String s) {
        return addFaces(0, d, d1, d2, s);
    }

    private void polylines(double d, double d1, double d2, String s) {
        addFaces(1, d, d1, d2, s);
    }

    private int circles(double d, double d1, double d2, String s) {
        return addFaces(2, d, d1, d2, s);
    }

//        public double[][] getTargets() {
//            double[][] ad = new double[2][flexTarget[0].length];
//            for (int i = 0; i < 2; i++)
//                System.arraycopy(flexTarget[i], 0, ad[i], 0, flexTarget[0].length);
//            return ad;
//        }
//        public void setTargets(double[][] ad) {
//            for (int i = 0; i < 2; i++) {
//                System.arraycopy(ad[i], 0, flexTarget[i], 0, flexTarget[0].length);
//            }
//        }

    private void vertices(String s) {
        if (firstVertices) {
            firstVertices = false;
            flex("turn", "", "");
            flex("nod", "", "");
            flex("tilt", "", "");
        }
        if (s != null) {
            StringTokenizer stringtokenizer1;
            for (StringTokenizer stringtokenizer = new StringTokenizer(s); stringtokenizer.hasMoreTokens();
                 vertexVector.addElement(Double.parseDouble(stringtokenizer1.nextToken()))) {
                stringtokenizer1 = new StringTokenizer(stringtokenizer.nextToken(), ",");
                vertexVector.addElement(Double.parseDouble(stringtokenizer1.nextToken()));
                vertexVector.addElement(Double.parseDouble(stringtokenizer1.nextToken()));
            }

        }
    }

    private int type(int i) {
        return typeVector.elementAt(i);
    }

    private Color color(int i, boolean flag) {
        return flag ? Color.black : colorVector.elementAt(i);
    }

    private Vector shapes(int i) {
        return shapesVector.elementAt(i);
    }

    private int addFaces(int i, double d, double d1, double d2, String s) {
        int index = typeVector.size();
        typeVector.addElement(i);
        colorVector.addElement(new Color((float) d1, (float) d1, (float) d1));
        shapesVector.addElement(shapeVector = new Vector<>());
        if (s != null) {
            for (StringTokenizer stringtokenizer = new StringTokenizer(s); stringtokenizer.hasMoreTokens(); ) {
                shapeVector.addElement(face = new Vector());
                for (StringTokenizer stringtokenizer1 = new StringTokenizer(stringtokenizer.nextToken(), ","); stringtokenizer1.hasMoreTokens();
                     face.addElement(Integer.valueOf(stringtokenizer1.nextToken())))
                    ;
            }
        }
        return index;
    }

    private void assymetric() {
        int i = flexVector.size() - 1;
        flexSymmetryVector.setElementAt(1, i);
    }

    private void flex(String s, String s1, String s2) {
        flexNamesVector.addElement(s);
        flexSymmetryVector.addElement(-1);
        flexVector.addElement(flx = new Vector());
        for (StringTokenizer stringtokenizer = new StringTokenizer(s1); stringtokenizer.hasMoreTokens(); ixyz.addElement((double) Integer.parseInt(stringtokenizer.nextToken())))
            flx.addElement(ixyz = new Vector());

        int i = 0;
        for (StringTokenizer stringtokenizer1 = new StringTokenizer(s2); stringtokenizer1.hasMoreTokens(); ) {
            ixyz = flx.elementAt(i++);
            for (StringTokenizer stringtokenizer2 = new StringTokenizer(stringtokenizer1.nextToken(), ","); stringtokenizer2.hasMoreTokens(); ixyz.addElement(Double.parseDouble(stringtokenizer2.nextToken())))
                ;
        }

    }

    //
    //    public void openAnim()
    //    {
    //        if(f != null)
    //            if(!f.isShowing())
    //                f = null;
    //            else
    //                f.toFront();
    //        if(f == null)
    //            f = new GraphAppFrame("Animator", this, getCodeBase().toString(), null);
    //    }
    @Override
    public void keyReleased(KeyEvent e) {
        //To change body of generated methods, choose Tools | Templates.
        keyDown = false;
        key = e.getKeyChar();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyDown = true;
        key = e.getKeyChar();

        int i = e.getKeyChar();
        //System.out.println(i);

        switch (i) {
            case '2' -> {
                leftArrow = true;
                prevKey = -1;
            }
            case '3' -> {
                rightArrow = true;
                prevKey = -1;
            }
            case '{' -> // '{'
                    spin++;
            case '}' -> // '}'
                    spin--;
            case '1' -> // '\t'
                    toggleNoise();
            default -> {
                if (i != prevKey) {
                    setFlex(i);
                    leftArrow = false;
                    rightArrow = false;
                }
                prevKey = i;
            }
        }
    }

    private void render(Graphics g) {
        if (cycle++ > 0) {
            long now = System.currentTimeMillis();

            update(((double) now - start) / 1000.0);

            spin = (spin * momentum) + (nextSpin * (1.0 - momentum));

        }

//            try {
        boolean firstTime = this.firstTime;
        float f1 = 0.9019608F;
        bgcolor = new Color(50, 50, 50); //isVectors ? Color.white : new Color(f1, f1, f1);
        if (this.firstTime)
            init();

        int j = flexValue[0].length;
        for (int i1 = 0; i1 < 2; i1++) {
            for (int l1 = 0; l1 < j; l1++) {
                if (flexTarget[i1][l1] != flexValue[i1][l1] || addNoise && jitterAmpl[l1] != 0) {
                    firstTime = true;
                    break;
                }
            }

        }

        if (!firstTime)
            return;

        double d = 0.94999999999999996D;
        double d1 = 1;
        long l3 = System.currentTimeMillis();
        long l4 = l3 - lastTime;
        double d2 = (d1 * 1000D) / l4;
        double d3 = 1 - Math.pow(1 - d, 1 / d2);
        frameCount++;
        long l5 = l3 / 1000L - lastTime / 1000L;
        if (l5 > 0L) {
            dispCount = (int) (frameCount / l5);
            frameCount = 0;
        }
        lastTime = l3;
        for (int i3 = 0; i3 < 2; i3++) {
            for (int j3 = 0; j3 < j; j3++) {
                flexValue[i3][j3] += d3 * (flexTarget[i3][j3] - flexValue[i3][j3]);
                if (addNoise && jitterAmpl[j3] != 0)
                    flexValue[i3][j3] += jitterAmpl[j3] * ImprovMath.noise(jitterFreq[j3] * t + (10 * j3));

                t += 0.005;
            }

        }

        blinkValue = addNoise ? pulse(t / 2D + ImprovMath.noise(t / 1.5D) + 0.5D * ImprovMath.noise(t / 3D), 0.050000000000000003D) : 0;
        doRender(g, flexValue, blinkValue, isVectors, doShade, shiftAxis);
//            } catch (RuntimeException e) {
//                e.printStackTrace();
//            }
    }

    private void init() {
        firstTime = false;
        vertexArray = new double[nVertices][3];
        for (int i = 0; i < nVertices; i++) {
            for (int k = 0; k < 3; k++)
                vertexArray[i][k] = vertexVector.elementAt(3 * i + k);
        }

        nShapes = shapesVector.size();
        shape = new int[nShapes][][];
        for (int l = 0; l < nShapes; l++) {
            shapeVector = shapes(l);
            shape[l] = new int[shapeVector.size()][];
            for (int j1 = 0; j1 < shape[l].length; j1++) {
                face = shapeVector.elementAt(j1);
                shape[l][j1] = new int[face.size()];
                for (int i2 = 0; i2 < shape[l][j1].length; i2++) {
                    shape[l][j1][i2] = face.elementAt(i2);
                }

            }

        }

        nFlexes = flexVector.size();
        flexData = new double[nFlexes][][];
        flexShape = new int[nFlexes][];
        flexSymmetry = new int[nFlexes];
        flexTarget = new double[2][nFlexes];
        flexValue = new double[2][nFlexes];
        for (int k1 = 0; k1 < nFlexes; k1++) {
            flx = flexVector.elementAt(k1);
            flexData[k1] = new double[flx.size()][];
            flexShape[k1] = new int[flx.size()];
            flexSymmetry[k1] = flexSymmetryVector.elementAt(k1);
            int fl = flexShape[k1].length;
            for (int j2 = 0; j2 < fl; j2++) {
                ixyz = flx.elementAt(j2);
                flexShape[k1][j2] = ixyz.elementAt(0).intValue();
                flexData[k1][j2] = new double[3];
                int xyzs = ixyz.size();
                for (int k2 = 1; k2 < xyzs; k2++)
                    flexData[k1][j2][k2 - 1] = ixyz.elementAt(k2);
            }

        }

        flexTarget[0][3] = flexTarget[1][3] = -1D;
        double[] ad = {
                0.080000000000000002D, 0.040000000000000001D, 0, 0, 0, 0.10000000000000001D, 0.070000000000000007D, 0.070000000000000007D, 0.14999999999999999D
        };
        double[] ad1 = {
                0.25D, 0.25D, 0, 0, 0, 0.5D, 0.5D, 0.5D, 0.5D
        };
        jitterAmpl = new double[nFlexes];
        jitterFreq = new double[nFlexes];
        for (int l2 = 0; l2 < ad.length && l2 < nFlexes; l2++) {
            jitterAmpl[l2] = ad[l2];
            jitterFreq[l2] = ad1[l2];
        }


    }

    private void doRender(Graphics g, double[][] ad, double d, boolean flag, boolean flag1, boolean flag2) {

        synchronized (pts) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < vertexArray.length; j++) {
                    pts[i][j][0] = vertexArray[j][0];
                    pts[i][j][1] = vertexArray[j][1];
                    pts[i][j][2] = vertexArray[j][2];
                    if (i == 1) {
                        pts[i][j][0] = -pts[i][j][0];
                    }
                }

            }

            for (int k = 0; k < nFlexes; k++) {
                for (int l = 0; l < flexShape[k].length; l++) {
                    int i1 = flexShape[k][l];
                    double d2 = ad[0][k];
                    double d4 = ad[1][k];
                    if (k == 4 && d == 1) {
                        d2 = d4 = 1;
                    }
                    pts[0][i1][0] += d2 * flexData[k][l][0] * flexSymmetry[k];
                    pts[1][i1][0] += d4 * flexData[k][l][0];
                    pts[0][i1][1] += d2 * flexData[k][l][1];
                    pts[1][i1][1] += d4 * flexData[k][l][1];
                    pts[0][i1][2] += d2 * flexData[k][l][2];
                    pts[1][i1][2] += d4 * flexData[k][l][2];
                }

            }

            double d1 = ad[0][0];
            double d3 = ad[0][1];
            double d5 = ad[0][2];
            if (kludge) {
                kludge = false;
            }
            double d6 = -0.20000000000000001D * d1 - 0.10000000000000001D * spin;
            d6 = (d6 + 3.1415926535897931D + 3141.5926535897929D) % 6.2831853071795862D - 3.1415926535897931D;
            Matrix3D matrix3d = new Matrix3D();
            matrix3d.scale(height / 110D, (-height) / 110D, height / 110D);
            matrix3d.translate(35D, -80D, flag2 ? 20 : 5);
            matrix3d.rotateX(-0.20000000000000001D * d3);
            matrix3d.rotateY(d6);
            matrix3d.rotateZ(-0.20000000000000001D * d5);
            matrix3d.translate(0, -30D, flag2 ? -20 : -5);
            Matrix3D matrix3d1 = new Matrix3D();
            matrix3d1.scale(height / 110D, (-height) / 110D, height / 110D);
            matrix3d1.translate(35D, -110D, 0);
            Vector3D vector3d = new Vector3D();
            int j1 = d6 >= 0 ? 0 : 1;
            for (int k1 = 0; k1 <= 1; ) {
                for (int l1 = 0; l1 < shape.length; l1++) {
                    int i2 = type(l1);
                    g.setColor(color(l1, flag));
                    for (int j2 = 0; j2 < shape[l1].length; j2++) {
                        int k2 = shape[l1][j2].length;
                        if (i2 != 0 || k2 != 2 || k1 != 1) {
                            for (int l2 = 0; l2 < k2; l2++) {
                                int i3 = shape[l1][j2][l2];
                                vector3d.set(pts[j1][i3][0], pts[j1][i3][1], pts[j1][i3][2]);
                                vector3d.transform(i3 >= noRotateIndex ? matrix3d1 : matrix3d);
                                ai[l2] = (int) vector3d.get(0);
                                ai1[l2] = (int) vector3d.get(1);
                                if (flag1) {
                                    ai2[l2] = (int) vector3d.get(2);
                                }
                            }

                            if (i2 == 0 && k2 == 2) {
                                k2 = 4;
                                for (int j3 = 0; j3 < 2; j3++) {
                                    int l3 = shape[l1][j2][j3];
                                    vector3d.set(pts[1 - j1][l3][0], pts[1 - j1][l3][1], pts[1 - j1][l3][2]);
                                    vector3d.transform(l3 >= noRotateIndex ? matrix3d1 : matrix3d);
                                    ai[3 - j3] = (int) vector3d.get(0);
                                    ai1[3 - j3] = (int) vector3d.get(1);
                                    if (flag1) {
                                        ai2[3 - j3] = (int) vector3d.get(2);
                                    }
                                }

                            }
                            switch (i2) {
                                default:
                                    break;

                                case 2: // '\002'
                                            /*if (Math.abs(d6) > 2D || d6 > 0.90000000000000002D && j1 == 0 || d6 < -0.90000000000000002D && j1 == 1) {
                                                break;
                                            }*/
                                    int k3 = ai[0] - ai[1];
                                    int i4 = ai1[0] - ai1[1];


                                    //byte byte0 = ((byte) (k3 * k3 + i4 * i4 < 51 ? 6 : 12));
                                    //HACK remove the ear circles, whatever they are
                                    if ((l1 != 13) && (l1 != 14)) continue;

                                    float radius = l1 == 14 ? pupilSize : eyeballSize;


                                    float rscale = height / 220f;
                                    int cr = (int) (radius * rscale);

                                    int cx = ai[0] - cr / 2;
                                    int cy = ai1[0] - cr / 2;
                                    if (flag) {
                                        g.drawOval(cx, cy, cr, cr);
                                    } else {
                                        g.fillOval(cx, cy, cr, cr);
                                    }
                                    break;

                                case 1: // '\001'
                                    if (!flag && Math.abs(d6) <= 2D && (d6 <= 1.1000000000000001D || j1 != 0) && (d6 >= -1.1000000000000001D || j1 != 1)) {
                                        g.drawPolygon(ai, ai1, k2);
                                    }
                                    break;

                                case 0: // '\0'
                                    if (flag && !colorVector.elementAt(l1).equals(Color.black) && shape[l1][j2][0] < noRotateIndex) {
                                        ai[k2] = ai[0];
                                        ai1[k2] = ai1[0];
                                        g.drawPolygon(ai, ai1, k2 + 1);
                                    }
                                    if (flag || (area(ai, ai1, k2) > 0) != (j1 == 0)) {
                                        break;
                                    }
                                    if (flag1) {
                                        Color color1 = color(l1, flag);
                                        int j4 = color1.getRed();
                                        int k4 = color1.getGreen();
                                        int l4 = color1.getBlue();
                                        int i5 = getShade(ai, ai1, ai2);
                                        j4 = Math.min(255, i5 * j4 >> 8);
                                        k4 = Math.min(255, i5 * k4 >> 8);
                                        l4 = Math.min(255, i5 * l4 >> 8);
                                        g.setColor(new Color(j4, k4, l4));
                                    }

                                    g.fillPolygon(ai, ai1, k2);
                                    break;
                            }
                        }
                    }

                }

                k1++;
                j1 = 1 - j1;
            }

        }

    }

    private void setFlex(int i) {
        try {

            if (!leftArrow) {
                flexTarget[0][keyFlex[i]] = keyValue[i];
            }
            if (!rightArrow) {
                flexTarget[1][keyFlex[i]] = keyValue[i];
            }
        } catch (RuntimeException e) { //HACK
            //e.printStackTrace();
        }
    }

    private void damage(int i, int j, int k, int l) {
        loX = Math.min(loX, i);
        loY = Math.min(loY, j);
        hiX = Math.max(hiX, k);
        hiY = Math.max(hiY, l);
    }

    private boolean isDamage() {
        return hiX > loX && hiY > loY;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseDrag = mouseIsDown = true;
        mouselx = mousex;
        mousely = mousey;
        mousex = e.getX();
        mousey = e.getY();
        mouseTime = new Date();
        mouseEvent = e;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseMove = true;
        mouseIsDown = false;
        mouselx = mousex;
        mousely = mousey;
        mousex = e.getX();
        mousey = e.getY();
        mouseTime = new Date();
        mouseEvent = e;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = mouseIsDown = true;
        mouseMiddle = e.getModifiersEx() == InputEvent.ALT_MASK;
        mouseRight = e.getModifiersEx() == InputEvent.META_MASK;
        mouseLeft = !mouseMiddle && !mouseRight;
        mouselx = mousex;
        mousely = mousey;
        mousex = e.getX();
        mousey = e.getY();
        mouseTime = new Date();
        mouseEvent = e;
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseUp = true;
        mouseIsDown = false;
        mouselx = mousex;
        mousely = mousey;
        mousex = e.getX();
        mousey = e.getY();
        mouseTime = new Date();
        mouseEvent = e;
    }

    public void noDamage() {
        loX = 1000;
        loY = 1000;
        hiX = -1;
        hiY = -1;
    }

    @Override
    public void paint(Graphics g) {
        if (img != null)
            g.drawImage(img, 0, 0, this);
    }

    public void run() {
//        try {
            double d = 0;
            do {
                width = getWidth();
                height = getHeight();
                if (width == 0 || height == 0) {
                    Util.sleepMS(cycleDelay);
                    continue;
                }

                if (r.width != width || r.height != height) {

                    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    buffer = img.getGraphics();
                        /*((Graphics2D)buffer).setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);*/
                    r = bounds();
                    resizing = true;
                    damage(0, 0, width, height);
                }
                if (isDamage()) {
                    buffer.setColor(bgcolor);
                    buffer.fillRect(loX, loY, hiX - loX, hiY - loY);
                    buffer.setColor(fgcolor);
                }

                render(buffer);
                //mouseDown = mouseDrag = mouseUp = mouseMove = resizing = false;

                repaint();
                d += 0.10000000000000001D;
                Util.sleepMS(cycleDelay);
            } while (true);
//        } catch (InterruptedException _ex) {
//        }
    }

    private void start() {

        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }


    static final class ImprovMath {

        public static final int XYZ = 0;
        public static final int XZY = 1;
        public static final int YXZ = 2;
        public static final int YZX = 3;
        public static final int ZXY = 4;
        public static final int ZYX = 5;
        private static final double LOG_HALF = Math.log(0.5D);
        private static final int[] p = new int[514];
        private static final double[][] g3 = new double[514][3];
        private static final double[][] g2 = new double[514][2];
        private static final double[] g1 = new double[514];
        private static int start = 1;

        static {
            init();
        }

        private ImprovMath() {
        }

        public static double angleBetween(double d, double d1, double d2, double d3,
                                          double d4, double d5) {
            double d6 = d2 - d;
            double d7 = d3 - d1;
            double d8 = d4 - d;
            double d9 = d5 - d1;
            return (Math.acos(dot(d6, d7, d8, d9) / (magnitude(d6, d7) * magnitude(d8, d9))) / 3.1415926535897931D) * 180D;
        }

        public static double bias(double d, double d1) {
            if (d < 0.001D)
                return 0;
            if (d > 0.999D)
                return 1;
            if (d1 < 0.001D)
                return 0;
            if (d1 > 0.999D)
                return 1;
            else
                return Math.pow(d, Math.log(d1) / LOG_HALF);
        }

        public static double[] cross(double[] ad, double[] ad1) {
            double[] ad2 = new double[3];
            ad2[0] = ad[1] * ad1[2] - ad[2] * ad1[1];
            ad2[1] = ad[2] * ad1[0] - ad[0] * ad1[2];
            ad2[2] = ad[0] * ad1[1] - ad[1] * ad1[0];
            return ad2;
        }

        public static double dot(double d, double d1) {
            return d * d1;
        }

        static double dot(double d, double d1, double d2, double d3) {
            return d * d2 + d1 * d3;
        }

        public static double dot(double d, double d1, double d2, double d3,
                                 double d4, double d5) {
            return d * d3 + d1 * d4 + d2 * d5;
        }

        static double dot(double[] ad, double[] ad1) {
            double d = 0;
            int i;
            if (ad.length <= ad1.length)
                i = ad.length;
            else
                i = ad1.length;
            for (int j = 0; j < i; j++)
                d += ad[j] * ad1[j];

            return d;
        }

        public static String doubleToString(double d, int i) {
            double d1 = d - (long) d;
            if (d1 == 0)
                return String.valueOf((long) d);
            boolean flag = false;
            if (d < 0) {
                flag = true;
                d = -d;
            }
            double d2 = Math.pow(10D, i);
            d *= d2;
            if (d - (long) d >= 0.49998999999999999D)
                d++;
            d /= d2;
            String s = String.valueOf(d);
            int j = s.indexOf('.');
            if (j != -1) {
                int k = j + i + 1;
                if (k < s.length())
                    s = s.substring(0, k);
            }
            int l = s.length() - 1;
            boolean flag1 = false;
            for (; l > 0 && s.charAt(l) == '0' && s.charAt(l - 1) != '.'; l--)
                flag1 = true;

            if (flag1)
                s = s.substring(0, l);
            return (flag ? "-" : "") + s;
        }

        public static double gain(double d, double d1) {
            if (d < 0.001D)
                return 0;
            if (d > 0.999D)
                return 1;
            d1 = d1 >= 0.001D ? d1 <= 0.999D ? d1 : 0.999D : 0.0001D;
            double d2 = Math.log(1 - d1) / LOG_HALF;
            if (d < 0.5D)
                return Math.pow(2D * d, d2) / 2D;
            else
                return 1 - Math.pow(2D * (1 - d), d2) / 2D;
        }

        static double[] getEulers(double[] ad, int i) {
            double[] ad1 = new double[3];
            double[] ad2 = new double[4];
            double[][] ad3 = new double[3][3];
            byte byte0 = 0;
            byte byte1 = 1;
            byte byte2 = 2;
            switch (i) {
                case 0 -> { // '\0'
                    byte0 = 0;
                    byte1 = 1;
                    byte2 = 2;
                }
                case 1 -> { // '\001'
                    byte0 = 0;
                    byte1 = 2;
                    byte2 = 1;
                }
                case 2 -> { // '\002'
                    byte0 = 1;
                    byte1 = 0;
                    byte2 = 2;
                }
                case 3 -> { // '\003'
                    byte0 = 1;
                    byte1 = 2;
                    byte2 = 0;
                }
                case 4 -> { // '\004'
                    byte0 = 2;
                    byte1 = 0;
                    byte2 = 1;
                }
                case 5 -> { // '\005'
                    byte0 = 2;
                    byte1 = 1;
                    byte2 = 0;
                }
            }
            ad2[0] = ad[0];
            ad2[1] = ad[1];
            ad2[2] = ad[2];
            ad2[3] = ad[3];
            if (ad2[0] == 0 && ad2[1] == 0 && ad2[2] == 0)
                return ad1;
            double d1 = ad2[0] * ad2[0] + ad2[1] * ad2[1] + ad2[2] * ad2[2];
            if (!(d1 < 0.0001D)) {
                double d3 = 1 / Math.sqrt(d1);
                ad2[0] *= d3;
                ad2[1] *= d3;
                ad2[2] *= d3;
                double d = Math.sin(ad2[3] * 0.5D);
                ad2[0] *= d;
                ad2[1] *= d;
                ad2[2] *= d;
                ad2[3] = Math.cos(ad2[3] * 0.5D);
                double d2 = ad2[0] * ad2[0] + ad2[1] * ad2[1] + ad2[2] * ad2[2] + ad2[3] * ad2[3];
                double d4 = 2D / d2;
                double d5 = ad2[0] * d4;
                double d6 = ad2[1] * d4;
                double d7 = ad2[2] * d4;
                double d8 = ad2[3] * d5;
                double d9 = ad2[3] * d6;
                double d10 = ad2[3] * d7;
                double d11 = ad2[0] * d5;
                double d12 = ad2[0] * d6;
                double d13 = ad2[0] * d7;
                double d14 = ad2[1] * d6;
                double d15 = ad2[1] * d7;
                double d16 = ad2[2] * d7;
                ad3[0][0] = 1 - d14 - d16;
                ad3[0][1] = d12 - d10;
                ad3[0][2] = d13 + d9;
                ad3[1][0] = d12 + d10;
                ad3[1][1] = 1 - d11 - d16;
                ad3[1][2] = d15 - d8;
                ad3[2][0] = d13 - d9;
                ad3[2][1] = d15 + d8;
                ad3[2][2] = 1 - d11 - d14;
                int j = byte1 % 3 <= byte0 % 3 ? -1 : 1;
                double d17;
                double d18;
                ad1[byte0] = Math.atan2(d18 = ad3[byte1][byte2] * (-j), d17 = ad3[byte2][byte2]);
                ad1[byte1] = Math.atan2(ad3[byte0][byte2] * j, Math.sqrt(d17 * d17 + d18 * d18));
                ad1[byte2] = Math.atan2(ad3[byte0][byte1] * (-j), ad3[byte0][byte0]);
                ad1[0] /= 0.017453292519943295D;
                ad1[1] /= 0.017453292519943295D;
                ad1[2] /= 0.017453292519943295D;
            }
            return ad1;
        }

        public static double[] getEulers(float[] af, int i) {
            double[] ad = {
                    af[0], af[1], af[2], af[3]
            };
            return getEulers(ad, i);
        }

        public static float[] getQuaternion(double[] ad) {
            return getQuaternion(ad, 0);
        }

        static float[] getQuaternion(double[] ad, int i) {
            double[][] ad1 = new double[4][4];
            float[] af = new float[4];
            return getQuaternion(ad, i, ad1, af);
        }

        static float[] getQuaternion(double[] ad, int i, double[][] ad1, float[] af) {
            double[] ad3 = ad1[0];
            double[] ad4 = ad1[1];
            double[] ad5 = ad1[2];
            double[] ad6 = ad1[3];
            ad3[0] = 1;
            ad3[1] = 0;
            ad3[2] = 0;
            ad3[3] = ad[0] * 0.017453292519943295D;
            ad4[0] = 0;
            ad4[1] = 1;
            ad4[2] = 0;
            ad4[3] = ad[1] * 0.017453292519943295D;
            ad5[0] = 0;
            ad5[1] = 0;
            ad5[2] = 1;
            ad5[3] = ad[2] * 0.017453292519943295D;
            byte byte0;
            byte byte1;
            byte byte2;
            switch (i) {
                case 1 -> { // '\001'
                    byte0 = 0;
                    byte1 = 2;
                    byte2 = 1;
                }
                case 2 -> { // '\002'
                    byte0 = 1;
                    byte1 = 0;
                    byte2 = 2;
                }
                case 3 -> { // '\003'
                    byte0 = 1;
                    byte1 = 2;
                    byte2 = 0;
                }
                case 4 -> { // '\004'
                    byte0 = 2;
                    byte1 = 0;
                    byte2 = 1;
                }
                case 5 -> { // '\005'
                    byte0 = 2;
                    byte1 = 1;
                    byte2 = 0;
                }
                default -> {
                    byte0 = 0;
                    byte1 = 1;
                    byte2 = 2;
                }
            }
            ad1[byte0] = ad3;
            ad1[byte1] = ad4;
            ad1[byte2] = ad5;
            prepAngles(ad1[0]);
            prepAngles(ad1[1]);
            prepAngles(ad1[2]);
            mult(ad1[2], ad1[1], ad6);
            mult(ad6, ad1[0], ad1[2]);
            double[] ad7 = ad1[2];
            double d = ad7[0];
            double d1 = ad7[1];
            double d2 = ad7[2];
            double d3 = ad7[3];
            double d4 = Math.sqrt(d * d + d1 * d1 + d2 * d2);
            if (d4 > 0) {
                af[0] = (float) (d * (1 / d4));
                af[1] = (float) (d1 * (1 / d4));
                af[2] = (float) (d2 * (1 / d4));
                af[3] = 2.0F * (float) Math.acos(d3);
            } else {
                af[0] = 0.0F;
                af[1] = 1.0F;
                af[2] = 0.0F;
                af[3] = 0.0F;
            }
            return af;
        }

        private static void init() {
            Random random1 = new Random();
            int i;
            for (i = 0; i < 256; i++) {
                p[i] = i;
                double d = (random1.nextLong() & 255L) / 256D;
                g1[i] = 2D * d - 1;
                for (int k = 0; k < 2; k++)
                    g2[i][k] = (random1.nextLong() % 512L - 256L) / 256D;

                normalize2(g2[i]);
                for (int l = 0; l < 3; l++)
                    g3[i][l] = (random1.nextLong() % 512L - 256L) / 256D;

                normalize3(g3[i]);
            }

            while (--i > 0) {
                int l1 = p[i];
                int i1 = (int) (random1.nextLong() & 255L);
                p[i] = p[i1];
                p[i1] = l1;
            }
            for (int j = 0; j < 258; j++) {
                p[256 + j] = p[j];
                g1[256 + j] = g1[j];
                System.arraycopy(g2[j], 0, g2[256 + j], 0, 2);
                System.arraycopy(g3[j], 0, g3[256 + j], 0, 3);

            }

        }

        static double lerp(double d, double d1, double d2) {
            return d1 + d * (d2 - d1);
        }

        static double magnitude(double d, double d1) {
            return Math.sqrt(dot(d, d1, d, d1));
        }

        public static double magnitude(double[] ad) {
            return Math.sqrt(dot(ad, ad));
        }

        private static void mult(double[] ad, double[] ad1, double[] ad2) {
            double d = ad[0];
            double d1 = ad[1];
            double d2 = ad[2];
            double d3 = ad[3];
            double d4 = ad1[0];
            double d5 = ad1[1];
            double d6 = ad1[2];
            double d7 = ad1[3];
            double d8 = (d7 * d + d4 * d3 + d5 * d2) - d6 * d1;
            double d9 = (d7 * d1 + d5 * d3 + d6 * d) - d4 * d2;
            double d10 = (d7 * d2 + d6 * d3 + d4 * d1) - d5 * d;
            double d11 = d7 * d3 - d4 * d - d5 * d1 - d6 * d2;
            d8 *= 1 / Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11);
            d9 *= 1 / Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11);
            d10 *= 1 / Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11);
            d11 *= 1 / Math.sqrt(d8 * d8 + d9 * d9 + d10 * d10 + d11 * d11);
            ad2[0] = d8;
            ad2[1] = d9;
            ad2[2] = d10;
            ad2[3] = d11;
        }

        private static double[] multBy(double[] ad, double d) {
            ad[0] *= d;
            ad[1] *= d;
            ad[2] *= d;
            return ad;
        }

        static double noise(double d) {
            if (start == 1) {
                start = 0;
                init();
            }
            double d4 = d + 4096D;
            int i = (int) d4 & 0xff;
            int j = i + 1 & 0xff;
            double d1 = d4 - (int) d4;
            double d2 = d1 - 1;
            double d3 = s_curve(d1);
            double d5 = d1 * g1[p[i]];
            double d6 = d2 * g1[p[j]];
            return lerp(d3, d5, d6);
        }

        private static double[] norm(double[] ad) {
            double[] ad1 = new double[4];
            ad1[0] = ad[0];
            ad1[1] = ad[1];
            ad1[2] = ad[2];
            ad1[3] = ad[3];
            double d = Math.sqrt(ad1[0] * ad1[0] + ad1[1] * ad1[1] + ad1[2] * ad1[2]);
            if (d != 0) {
                ad1[0] *= d;
                ad1[1] *= d;
                ad1[2] *= d;
            } else {
                ad1[0] = 0;
                ad1[1] = 0;
                ad1[2] = 0;
            }
            return ad1;
        }

        private static void normalize2(double[] ad) {
            double d = Math.sqrt(ad[0] * ad[0] + ad[1] * ad[1]);
            ad[0] /= d;
            ad[1] /= d;
        }

        private static void normalize3(double[] ad) {
            double d = Math.sqrt(ad[0] * ad[0] + ad[1] * ad[1] + ad[2] * ad[2]);
            ad[0] /= d;
            ad[1] /= d;
            ad[2] /= d;
        }

        private static void prepAngles(double[] ad) {
            norm(ad);
            multBy(ad, Math.sin(ad[3] / 2));
            ad[3] = Math.cos(ad[3] / 2);
        }

        public static double random(double d, double d1) {
            return Math.min(d, d1) + Math.random() * Math.abs(d1 - d);
        }

        private static double s_curve(double d) {
            return d * d * (3D - 2D * d);
        }
    }

    protected static class FaceFrame {
        private final double[][] targets;
        private Image snapshot;
        private double when;

        public FaceFrame(double[][] t, Image snap, double w) {
            targets = new double[2][t[0].length];
            for (int s = 0; s < 2; s++)
                System.arraycopy(t[s], 0, targets[s], 0, t[0].length);
            snapshot = snap;
            when = w;
        }

        public FaceFrame(FaceFrame f) {
            targets = f.getTargets();
            snapshot = f.snapshot;
            when = f.when;
        }

        public FaceFrame(String s) {
            StringTokenizer outside = new StringTokenizer(s, "|\n");
            String w = outside.nextToken();
            when = Double.parseDouble(w);

            String val = outside.nextToken();
            StringTokenizer inside = new StringTokenizer(val, ",");
            targets = new double[2][inside.countTokens()];
            int i = 0;
            String v;
            while (inside.hasMoreTokens()) {
                v = inside.nextToken();
                targets[0][i] = Double.parseDouble(v);
                i++;
            }

            val = outside.nextToken();
            inside = new StringTokenizer(val, ",");
            i = 0;
            while (inside.hasMoreTokens()) {
                v = inside.nextToken();
                targets[1][i] = Double.parseDouble(v);
                i++;
            }
            snapshot = null;
        }

        double[][] getTargets() {
            return targets;
        }

        Image getSnapshot() {
            return snapshot;
        }

        public void setSnapshot(Image snap) {
            snapshot = snap;
        }

        double getTime() {
            return when;
        }

        public void setTime(double t) {
            when = t;
        }

        public String toString() {
            String retVal = "";
            retVal += when + "\n";
            for (int s = 0; s < 2; s++) {
                for (int v = 0; v < targets[s].length; v++) {
                    if (v != 0)
                        retVal += ",";
                    retVal += String.valueOf(targets[s][v]);
                }
                retVal += "\n";
            }
            return retVal;
        }
    }

    /**
     * @author me
     */


    static class MatrixN {

        private final VectorN[] v;

        MatrixN(int i) {
            v = new VectorN[i];
            for (int j = 0; j < i; j++)
                v[j] = new VectorN(i);

        }

        VectorN get(int i) {
            return v[i];
        }

        double get(int i, int j) {
            return get(i).get(j);
        }

        void identity() {
            for (int i = 0; i < size(); i++) {
                for (int j = 0; j < size(); j++)
                    set(j, i, j == i ? 1 : 0);

            }

        }

        void postMultiply(MatrixN matrixn) {
            MatrixN matrixn1 = new MatrixN(size());
            for (int i = 0; i < size(); i++) {
                for (int j = 0; j < size(); j++) {
                    double d = 0;
                    for (int k = 0; k < size(); k++)
                        d += get(j, k) * matrixn.get(k, i);

                    matrixn1.set(j, i, d);
                }

            }

            set(matrixn1);
        }

        void preMultiply(MatrixN matrixn) {
            MatrixN matrixn1 = new MatrixN(size());
            for (int i = 0; i < size(); i++) {
                for (int j = 0; j < size(); j++) {
                    double d = 0;
                    for (int k = 0; k < size(); k++)
                        d += matrixn.get(j, k) * get(k, i);

                    matrixn1.set(j, i, d);
                }

            }

            set(matrixn1);
        }

        void set(int i, int j, double d) {
            v[i].set(j, d);
        }

        void set(int i, VectorN vectorn) {
            v[i].set(vectorn);
        }

        void set(MatrixN matrixn) {
            for (int i = 0; i < size(); i++)
                set(i, matrixn.get(i));

        }

        int size() {
            return v.length;
        }

        public String toString() {
            String s = "{";
            for (int i = 0; i < size(); i++)
                s = s + (i != 0 ? "," : "") + get(i);

            return s + "}";
        }
    }

    /**
     * @author me
     */


    static class VectorN {

        private final double[] v;

        VectorN(int i) {
            v = new double[i];
        }

        double distance(VectorN vectorn) {
            double d2 = 0;
            for (int i = 0; i < size(); i++) {
                double d = vectorn.get(0) - get(0);
                double d1 = vectorn.get(1) - get(1);
                d2 += d * d + d1 * d1;
            }

            return Math.sqrt(d2);
        }

        double get(int i) {
            return v[i];
        }

        void set(int i, double d) {
            v[i] = d;
        }

        void set(VectorN vectorn) {
            for (int i = 0; i < size(); i++)
                set(i, vectorn.get(i));

        }

        int size() {
            return v.length;
        }

        public String toString() {
            String s = "{";
            for (int i = 0; i < size(); i++)
                s = s + (i != 0 ? "," : "") + get(i);

            return s + "}";
        }

        void transform(MatrixN matrixn) {
            VectorN vectorn = new VectorN(size());
            for (int i = 0; i < size(); i++) {
                double d = 0;
                for (int j = 0; j < size(); j++)
                    d += matrixn.get(i, j) * get(j);

                vectorn.set(i, d);
            }

            set(vectorn);
        }
    }

    static class Vector3D extends VectorN {

        Vector3D() {
            super(4);
        }

        void set(double d, double d1, double d2) {
            set(d, d1, d2, 1);
        }

        void set(double d, double d1, double d2, double d3) {
            set(0, d);
            set(1, d1);
            set(2, d2);
            set(3, d3);
        }
    }

    static class Matrix3D extends MatrixN {

        Matrix3D() {
            super(4);
            identity();
        }

        void rotateX(double d) {
            Matrix3D matrix3d = new Matrix3D();
            double d1 = Math.cos(d);
            double d2 = Math.sin(d);
            matrix3d.set(1, 1, d1);
            matrix3d.set(1, 2, -d2);
            matrix3d.set(2, 1, d2);
            matrix3d.set(2, 2, d1);
            postMultiply(matrix3d);
        }

        void rotateY(double d) {
            Matrix3D matrix3d = new Matrix3D();
            double d1 = Math.cos(d);
            double d2 = Math.sin(d);
            matrix3d.set(2, 2, d1);
            matrix3d.set(2, 0, -d2);
            matrix3d.set(0, 2, d2);
            matrix3d.set(0, 0, d1);
            postMultiply(matrix3d);
        }

        void rotateZ(double d) {
            Matrix3D matrix3d = new Matrix3D();
            double d1 = Math.cos(d);
            double d2 = Math.sin(d);
            matrix3d.set(0, 0, d1);
            matrix3d.set(0, 1, -d2);
            matrix3d.set(1, 0, d2);
            matrix3d.set(1, 1, d1);
            postMultiply(matrix3d);
        }

        void scale(double d) {
            Matrix3D matrix3d = new Matrix3D();
            matrix3d.set(0, 0, d);
            matrix3d.set(1, 1, d);
            matrix3d.set(2, 2, d);
            postMultiply(matrix3d);
        }

        void scale(double d, double d1, double d2) {
            Matrix3D matrix3d = new Matrix3D();
            matrix3d.set(0, 0, d);
            matrix3d.set(1, 1, d1);
            matrix3d.set(2, 2, d2);
            postMultiply(matrix3d);
        }

        void scale(Vector3D vector3d) {
            scale(vector3d.get(0), vector3d.get(1), vector3d.get(2));
        }

        void translate(double d, double d1, double d2) {
            Matrix3D matrix3d = new Matrix3D();
            matrix3d.set(0, 3, d);
            matrix3d.set(1, 3, d1);
            matrix3d.set(2, 3, d2);
            postMultiply(matrix3d);
        }

        void translate(Vector3D vector3d) {
            translate(vector3d.get(0), vector3d.get(1), vector3d.get(2));
        }
    }
}
//package automenta.vivisect.face;

//package nars.gui.output.face;
//
///* General Notes:  Chris Poultney, 2/1/2000
//   This applet is designed to work with Ken Perlin's responsive face applet, currently called
//   Face2bApplet.  It can run in two ways:  1) as an applet, included directly in an HTML page
//   within an <applet> tag, or 2) as a frame, by declaring a new GraphAnimFrame from within a
//   running applet (or presumably a frame).
//
//   The applet's UI is broken into three major parts:  The snapshot area, the animation area,
//   and the file area.  The snapshot area includes the snapshot icon bar and snapshot display
//   area, located on the left side of the applet.  The animation area includes the animation
//   icon bar, timeline, and keyframe display, located in the upper right.  The file area
//   consists of a card layout located in the bottom right.  The areas which need to be painted
//   by the applet are the snapshot display, timeline, and keyframe display.
// */
//
//import java.applet.Applet;
//import java.awt.BorderLayout;
//import java.awt.Button;
//import java.awt.CardLayout;
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Container;
//import java.awt.Dimension;
//import java.awt.Event;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.FontMetrics;
//import java.awt.Graphics;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Image;
//import java.awt.Insets;
//import java.awt.Label;
//import java.awt.MediaTracker;
//import java.awt.Panel;
//import java.awt.Point;
//import java.awt.Polygon;
//import java.awt.Scrollbar;
//import java.awt.TextArea;
//import java.awt.TextField;
//import java.awt.Toolkit;
//import java.awt.image.ImageObserver;
//import java.awt.image.MemoryImageSource;
//import java.awt.image.PixelGrabber;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.net.URLEncoder;
//import java.util.NoSuchElementException;
//import java.util.StringTokenizer;
//import java.util.Vector;
//
//public class GraphAnim extends Applet {
//    public static final int SNAPHEIGHT = 40;                     // height of snapshot in snap and anim components
//    public static final int TOPSTART = 0;                        // top row within applet
//    public static final int SNAPCOMPWIDTH = 100;                 // width of snapshot component
//    public static final int ICONCOMPHEIGHT = 32;                 // height of icon bars
//    public static final int ANIMTIMECOMPHEIGHT = 20;             // height of timeline
//    public static final int ANIMFACECOMPHEIGHT = SNAPHEIGHT;     // height of keyframe display
//    public static final int ANIMSCRHEIGHT = 15;                  // height of animation scroll bar
//    public static final double TIMEMULTDEFAULT = 0.5;            // default "time multiple" - one keyframe takes TIMEMULTDEFAULT seconds on timeline display
//    public static final int WIDTHDEFAULT = 600;
//    public static final int HEIGHTDEFAULT = 400;
//    public static final int ANIMSECONDS = 60;                    // seconds of animation to provide interface for
//
//    // these numbers are determined in init() and reshape() fns
//    public int ANIMCOMPWIDTH;                                    // width of animation (timeline, keyframe) components, also used for animation icon bar and file panel
//    public int SNAPCOMPHEIGHT;                                   // height of snapshot component
//    public int FILECOMPHEIGHT;                                   // height of file component
//    public int ANIMCOMPHEIGHT;                                   // height of animation components
//
//    // color & font defs
//    public static final Color SNAPBACK = Color.lightGray;
//    public static final Color SELECTBOX = Color.black;
//    public static final Color NUMBACK = Color.gray;
//    public static final Color NUMCOLOR = Color.black;
//    public static final Color ANIMBACK = Color.lightGray;
//    public static final Color ANIMBOX = Color.black;
//    public static final Color ICONBACK = new Color(204, 204, 204);
//    public static final Color ARROWCOLOR = Color.darkGray;
//    public static final Font TIMEFONT = new Font("Helvetica", Font.BOLD, 24);
//    public static final Font PALETTEFONT = new Font("Helvetica", Font.BOLD, 24);
//
//    // flags passed to draw() routine to control drawing of different parts of applet
//    public static final int DRAWSNAP = 1;
//    public static final int DRAWNUM = 1<<1;
//    public static final int DRAWANIM = 2<<2;
//    public static final int DRAWALL = DRAWSNAP | DRAWNUM | DRAWANIM;
//
//    // constants denoting the section of the applet in which a drag operation started
//    public static final int DRAGNONE = 0;
//    public static final int DRAGSNAP = 1;
//    public static final int DRAGANIM = 2;
//
//    protected int snapwidth;                                // width of snapshot in snap and anim components
//    protected int snapcols, snaprows;                       // # of snaps in col, row of snap component
//    protected int maxsnaps;                                 // max # of snaps that can fit in snapshot display
//    protected int framewidth;                               // # of pixels in a second on anim timeline
//    protected int animwidth;                                // # of pixels required for ANIMSECONDS
//    protected double timemult;                              // # of seconds corresponding to snapwidth pixels on anim timeline
//    protected double timediv;
//    protected int xOffset, yOffset;                         // offset with snap of mouse click
//    protected boolean dragdraw;                             // has any drawing been done for drag yet?
//    protected int dragMode;                                 // mouse drag stuff
//    protected int oldx, oldy;                               // x, y positions of last MOUSE_DRAG or MOUSE_UP
//    protected boolean oldinanim;                            // did last drag operation start in keyframe display area?
//    protected boolean wasinanim;                            // did any drag operation enter keyframe display area?
//    protected boolean deleteFrame;                          // should the current keyframe be deleted in do_drag?
//    protected int scrPos;                                   // scrollbar position
//    protected int fullAnimWidth;                            // logical width of scrollable animation area
//
//    protected Face2bApplet faceApplet;
//    protected Vector snaps, frames;                         // lists of snapshots and animation frames
//    protected int currentSnap, currentFrame;                // currently selected snapshot and frame (-1 for none)
//    protected FaceFrame currentFace;                        // useful for drag operations
//    protected boolean doRollOver;
//    protected int rollSnap, rollFrame;                      // current snapshot/frame under mouse (-1 for none)
//    protected double[][] preRollTargets;                    // face setting before rollover
//
//    protected AnimThread athread;                           // thread class for playing animations
//
//    // servlet save/load stuff
//    protected String faceTitle, faceStory;                  // user-supplied title, description for animation
//    protected String servletURL;                            // servlet URL for animation save/load
//    protected Vector seqnos;                                // unique ids of saved animations returned by servlet
//
//    // applet/frame version compatibility vars
//    protected String codeBase = null;                       // URL of codebase, supplied by frame parent or applet calls
//    protected boolean isFrame;
//    protected boolean standalone;                           // set param to true if you want to view as applet in appletviewer
//    protected boolean initdone;
//
//    // GUI stuff
//    Panel snapControlPan, animControlPan;                   // snap and animation section icon bars
//    DrawPanel snapFacePan;                                  // snapshot display panel
//    Panel animPan;                                          // parent panel for animation time and face panels
//    DrawPanel animTimePan, animFacePan;                     // animation timeline and face panels
//    Panel filePan;                                          // remaining area of screen used for file ops & messaging
//    Panel infoPan, loadPan, savePan, msgPan;                // panels displayed in filePan (card layout)
//    Panel saveControlPan;                                   // holds save/cancel buttons for save panel
//    MessagePanel message;
//    GraphicButton snap, delete;                             // snap icons
//    GraphicButton play, stop, clear, save, load, help;      // anim icons
//    OnOffGraphicButton rollOverBut;                         // rollover on/off toggle button
//    Button loadBut, cancelLoadBut, saveBut, cancelSaveBut;  // save/load control buttons
//    Button contBut;                                         // message panel continue button
//    Label titleLab, titleLabDisp, storyLab, storyLabDisp;   // title, story labels for save panel and info panel
//    java.awt.List loadList;                                 // filename list for load
//    TextField titleText, titleTextDisp;                     // title entry/display
//    TextArea storyText, storyTextDisp;                      // story entry/display
//    CardLayout fileCard;                                    // filePan multi-function layout
//    Scrollbar animScr;                                      // anim timeline, face scroll
//
//    GridBagLayout gb;
//    GridBagConstraints gbc;
//
//    Image playIm, pauseIm;                                  // in case pause fn is added and shared with play button
//    Polygon timeArrow, paletteArrow;                        // background arrows for palette & timeline
//
//    // Instantiated as applet
//    public GraphAnim() {
//	super();
//	isFrame = false;
//    }
//
//    // Instantiated as frame
//    public GraphAnim(Face2bApplet fa, String cb, String su) {
//	this(fa, cb, su, false);
//    }
//
//    // Instantiated as standalone frame
//    public GraphAnim(Face2bApplet fa, String cb, String su, boolean sa) {
//	super();
//	codeBase = cb;
//	faceApplet = fa;
//	standalone = sa;
//	isFrame = true;
//	initdone = false;
//	servletURL = su;
//    }
//
//    public void init() {
//	// applet, codeBase not given in constructor
//	if(codeBase == null)
//	    codeBase = getCodeBase().toString();
//
//	//  initialize vars from applet parameters
//	if(!isFrame) {
//	    String sa = getParameter("standalone");
//	    if(sa == null)
//		standalone = false;
//	    else
//		standalone = true;
//	    servletURL = getParameter("servletURL");
//	}
//
//	// wait for face applet to load
//	boolean OK = standalone;
//	while(!OK) {
//	    try {
//		faceApplet = (Face2bApplet)this.getAppletContext().getApplet("A");
//	    }
//	    catch (NullPointerException npe) {
//		;
//	    }
//	    finally {
//		if(faceApplet != null)
//		    OK = true;
//	    }
//	    if(!OK) {
//		try {
//		    Thread.sleep(500);
//		}
//		catch (InterruptedException ie) {
//		    ;
//		}
//	    }
//	}
//
//	if(servletURL == null)
//	    servletURL = new String("http://cat.nyu.edu:8000/saveface");
//
//	// initialize vars formerly from applet parameters
//	timemult = TIMEMULTDEFAULT;
//	timediv = 1.0 / timemult;
//
//	if(faceApplet != null)
//	    snapwidth = (int)((float)faceApplet.bounds().width * (float)SNAPHEIGHT / (float)faceApplet.bounds().height);
//	else
//	    snapwidth = 25;
//	animScr = new Scrollbar(Scrollbar.HORIZONTAL);
//	calcshape(600, 400);
//
//	scrPos = 0;
//	animScr.setValues(scrPos, ANIMCOMPWIDTH, 0, animwidth);
//	animScr.setLineIncrement((int)(snapwidth * timediv));
//	animScr.setPageIncrement(ANIMCOMPWIDTH);
//
//	snaps = new Vector();
//	currentSnap = -1;
//	rollSnap = -1;
//	frames = new Vector();
//	rollFrame = -1;
//	preRollTargets = null;
//	doRollOver = false;
//
//	// LAYOUT SETUP
//
//	// this is the only way I could control component sizes and have methods called properly
//	setLayout(null);
//
//	setBackground(ICONBACK);
//
//	snapControlPan = new Panel();
//	snapFacePan = new DrawPanel(this, DRAWSNAP);
//	animControlPan = new Panel();
//	animTimePan = new DrawPanel(this, DRAWNUM);
//	animFacePan = new DrawPanel(this, DRAWANIM);
//	animPan = new Panel();
//	filePan = new Panel();
//	infoPan = new Panel();
//	savePan = new Panel();
//	loadPan = new Panel();
//	msgPan = new Panel();
//
//	// get images, make buttons from some of them
//	try {
//	    Toolkit tk = Toolkit.getDefaultToolkit();
//	    MediaTracker tracker = new MediaTracker(this);
//
//	    URL u = new URL(codeBase + "snap.gif");
//	    Image im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    snap = new GraphicButton(im);
//	    u = new URL(codeBase + "delete3.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    delete = new GraphicButton(im);
//	    u = new URL(codeBase + "clear2.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//
//	    u = new URL(codeBase + "play.gif");
//	    playIm = tk.getImage(u);
//	    tracker.addImage(playIm, 0);
//	    play = new GraphicButton(playIm);
//	    u = new URL(codeBase + "pause.gif");
//	    pauseIm = tk.getImage(u);
//	    tracker.addImage(pauseIm, 0);
//	    u = new URL(codeBase + "stop.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    stop = new GraphicButton(im);
//	    u = new URL(codeBase + "clear1.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    clear = new GraphicButton(im);
//	    u = new URL(codeBase + "save.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    save = new GraphicButton(im);
//	    u = new URL(codeBase + "load.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    load = new GraphicButton(im);
//	    u = new URL(codeBase + "facepoint.gif");
//	    im = tk.getImage(u);
//	    tracker.addImage(im, 0);
//	    rollOverBut = new OnOffGraphicButton(im, doRollOver);
//
//	    tracker.waitForID(0);
//	}
//	catch (MalformedURLException mue) {
//	    System.out.println(mue);
//	    mue.printStackTrace();
//	}
//	catch (InterruptedException ie) {
//	    System.out.println(ie);
//	    ie.printStackTrace();
//	}
//
//	snapControlPan.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//	snapControlPan.add(snap);
//	snapControlPan.add(delete);
//	snapControlPan.reshape(0, TOPSTART, SNAPCOMPWIDTH, ICONCOMPHEIGHT);
//	add(snapControlPan);
//
//	snapFacePan.reshape(0, ICONCOMPHEIGHT + TOPSTART, SNAPCOMPWIDTH, SNAPCOMPHEIGHT);
//	add(snapFacePan);
//
//	animControlPan.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//	animControlPan.add(play);
//	animControlPan.add(stop);
//	animControlPan.add(new Spacer(40, 32));
//	animControlPan.add(save);
//	animControlPan.add(load);
//	animControlPan.add(clear);
//	animControlPan.add(new Spacer(40, 32));
//	animControlPan.add(rollOverBut);
//	animControlPan.reshape(SNAPCOMPWIDTH, TOPSTART, ANIMCOMPWIDTH, ICONCOMPHEIGHT);
//	add(animControlPan);
//
//	animPan.reshape(SNAPCOMPWIDTH, ICONCOMPHEIGHT + TOPSTART, ANIMCOMPWIDTH, ANIMCOMPHEIGHT);
//	add(animPan);
//
//	animPan.setLayout(null);
//	animPan.add(animTimePan);
//	animTimePan.reshape(0, 0, ANIMCOMPWIDTH, ANIMTIMECOMPHEIGHT);
//	animPan.add(animFacePan);
//	animFacePan.reshape(0, ANIMTIMECOMPHEIGHT, ANIMCOMPWIDTH, ANIMFACECOMPHEIGHT);
//	animPan.add(animScr);
//	animScr.reshape(0, ANIMTIMECOMPHEIGHT + ANIMFACECOMPHEIGHT, ANIMCOMPWIDTH, ANIMSCRHEIGHT);
//
//	fileCard = new CardLayout();
//	filePan.setLayout(fileCard);
//	filePan.reshape(SNAPCOMPWIDTH, ICONCOMPHEIGHT + ANIMCOMPHEIGHT + TOPSTART, ANIMCOMPWIDTH, FILECOMPHEIGHT);
//
//	gb = new GridBagLayout();
//	infoPan.setLayout(gb);
//	titleLabDisp = new Label("Title:");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.EAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 0.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(titleLabDisp, gbc);
//	infoPan.add(titleLabDisp);
//	storyLabDisp = new Label("Story:");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.NORTHEAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 0.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(storyLabDisp, gbc);
//	infoPan.add(storyLabDisp);
//	titleTextDisp = new TextField();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(titleTextDisp, gbc);
//	infoPan.add(titleTextDisp);
//	storyTextDisp = new TextArea();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 6, 3);
//	gb.setConstraints(storyTextDisp, gbc);
//	infoPan.add(storyTextDisp);
//	filePan.add("info", infoPan);
//
//	gb = new GridBagLayout();
//	savePan.setLayout(gb);
//	titleLab = new Label("Title:");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.EAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 0.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(titleLab, gbc);
//	savePan.add(titleLab);
//	storyLab = new Label("Story:");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.NORTHEAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 0.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(storyLab, gbc);
//	savePan.add(storyLab);
//	titleText = new TextField();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(titleText, gbc);
//	savePan.add(titleText);
//	storyText = new TextArea();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 6, 3);
//	gb.setConstraints(storyText, gbc);
//	savePan.add(storyText);
//	saveControlPan = new Panel();
//	saveControlPan.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 2;
//	gbc.gridwidth = 2;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gb.setConstraints(saveControlPan, gbc);
//	saveBut = new Button("Save");
//	saveControlPan.add(saveBut);
//	cancelSaveBut = new Button("Cancel");
//	saveControlPan.add(cancelSaveBut);
//	savePan.add(saveControlPan);
//	filePan.add("save", savePan);
//
//	loadPan.setLayout(gb);
//	loadList = new java.awt.List();
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.gridwidth = 2;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(loadList, gbc);
//	loadPan.add(loadList);
//	loadBut = new Button("Load");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.EAST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(loadBut, gbc);
//	loadPan.add(loadBut);
//	cancelLoadBut = new Button("Cancel");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 1;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.WEST;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(cancelLoadBut, gbc);
//	loadPan.add(cancelLoadBut);
//	filePan.add("load", loadPan);
//
//	msgPan.setLayout(gb);
//	message = new MessagePanel("");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 0;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.BOTH;
//	gbc.weightx = 1.0;
//	gbc.weighty = 1.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(message, gbc);
//	msgPan.add(message);
//	contBut = new Button("Continue");
//	gbc = new GridBagConstraints();
//	gbc.gridx = 0;
//	gbc.gridy = 1;
//	gbc.anchor = GridBagConstraints.CENTER;
//	gbc.fill = GridBagConstraints.NONE;
//	gbc.weightx = 1.0;
//	gbc.weighty = 0.0;
//	gbc.insets = new Insets(3, 3, 3, 3);
//	gb.setConstraints(contBut, gbc);
//	msgPan.add(contBut);
//	filePan.add("msg", msgPan);
//
//	add(filePan);
//
//	//	rollOverBut.setIsOn(doRollOver);
//
//	initdone = true;
//    }
//
//    // take snapshot
//    public void getFaceFrame() {
//	if(snaps.size() < maxsnaps) {
//	    double[][] tar = faceApplet.getTargets();
//	    Image pre = faceApplet.getSnapshotImage(tar);
//	    Image post = scaleSnap(pre);
//	    Graphics ig = post.getGraphics();
//	    ig.drawImage(pre, 0, 0, snapwidth, SNAPHEIGHT, Color.white, null);
//	    pre.flush();
//
//	    double w = 0;
//
//	    FaceFrame ff = new FaceFrame(tar, post, w);
//	    snaps.addElement(ff);
//	    draw(DRAWSNAP);
//	}
//    }
//
//    public Image scaleSnap(Image i) {
//	Image after = this.createImage(snapwidth, SNAPHEIGHT);
//	Graphics ig = after.getGraphics();
//	ig.drawImage(i, 0, 0, snapwidth, SNAPHEIGHT, Color.white, null);
//	return after;
//    }
//
//    public void clearSnaps() {
//	snaps = new Vector();
//	currentSnap = -1;
//	draw(DRAWSNAP);
//    }
//
//    public void clearFrames() {
//	frames = new Vector();
//	draw(DRAWANIM);
//    }
//
//    // convert snapshot list to string for saving
//    protected String getSnapString() {
//	String snapString = new String();
//	FaceFrame ff;
//	for(int i = 0; i < snaps.size(); i++) {
//	    ff = (FaceFrame)snaps.elementAt(i);
//	    snapString += ff.toString();
//	}
//
//	return snapString.substring(0, snapString.length() - 1);
//    }
//
//    // convert animation frame list to string for saving
//    protected String getFrameString() {
//	String frameString = new String();
//	FaceFrame ff;
//	for(int i = 0; i < frames.size(); i++) {
//	    ff = (FaceFrame)frames.elementAt(i);
//	    frameString += ff.toString();
//	}
//
//	return frameString.substring(0, frameString.length() - 1);
//    }
//
//    public boolean save(String title, String story) {
//	URL u;
//	URLConnection conn;
//	OutputStream o;
//	DataOutputStream dout;
//
//	try {
//	    // format save info and print to standard output as form submit
//	    String sub = new String(servletURL);
//	    String fd = new String("");
//	    fd += "snaps=" + URLEncoder.encode(getSnapString());
//	    fd += "&frames=" + URLEncoder.encode(getFrameString());
//	    fd += "&name=" + URLEncoder.encode(titleText.getText());
//	    fd += "&story=" + URLEncoder.encode(storyText.getText());
//	    u = new URL(sub);
//	    conn = u.openConnection();
//	    conn.setDoOutput(true);
//	    o = conn.getOutputStream();
//	    dout = new DataOutputStream(o);
//	    dout.writeBytes(fd);
//	    dout.close();
//
//	    // read servlet output to make &#*%! servlet think it's a POST
//	    DataInputStream in = new DataInputStream(conn.getInputStream());
//	    String line;
//	    while((line = in.readLine()) != null) {
//		; // this line intentionally left blank
//	    }
//	    in.close();
//	}
//	catch (MalformedURLException m) {
//	    System.out.println(m);
//	    m.printStackTrace();
//	    return false;
//	}
//	catch (IOException io) {
//	    System.out.println(io);
//	    io.printStackTrace();
//	    return false;
//	}
//	return true;
//    }
//
//    // build list of saved animations
//    public boolean getList() {
//	URL u;
//	URLConnection conn;
//	DataInputStream in;
//
//	try {
//	    u = new URL(servletURL);
//	    conn = u.openConnection();
//	    in = new DataInputStream(conn.getInputStream());
//	    String line;
//	    loadList.clear();
//	    seqnos = new Vector();
//
//	    // a sample line, and the sed script to extract the desired information
//	    // <A HREF="javascript:opener.loadanim('-1734480678');window.close();">4</A><BR>
//	    // s/.*'\([-0-9]*\)'[^>]*>\([^<]*\).*/\1 \2/
//	    while((line = in.readLine()) != null) {
//		if(!line.startsWith("<A HREF="))
//		    continue;
//		String seqno;
//		try {
//		    StringTokenizer st = new StringTokenizer(line, "'");
//		    String s = st.nextToken();
//		    seqno = st.nextToken();
//		}
//		catch (NoSuchElementException nse) {
//		    continue;
//		}
//
//		int start = line.indexOf(">") + 1;
//		int end = line.lastIndexOf("<");
//		end = line.lastIndexOf("<", end - 1);
//		String title = line.substring(start, end);
//
//		loadList.addItem(title);
//		seqnos.addElement(seqno);
//	    }
//	}
//	catch (MalformedURLException mu) {
//	    System.out.println(mu);
//	    mu.printStackTrace();
//	    return false;
//	}
//	catch (IOException ie) {
//	    System.out.println(ie);
//	    ie.printStackTrace();
//	    return false;
//	}
//
//	return true;
//    }
//
//    public boolean load(String seqno) {
//	URL u;
//	URLConnection conn;
//	DataInputStream in;
//	StringBuffer text = new StringBuffer();
//
//	// get saved text from server
//	try {
//	    u = new URL(servletURL + "?load=" + seqno);
//	    conn = u.openConnection();
//	    in = new DataInputStream(conn.getInputStream());
//	    String line;
//	    while((line = in.readLine()) != null)
//		text.append(line + "\n");
//	    in.close();
//	}
//	catch (Exception e2) {
//	    System.out.println(e2);
//	    e2.printStackTrace();
//	    return false;
//	}
//
//	// separate text into parts: title, snaps, frames, story
//	String sep = "\n\n";
//	String[] els = new String[4];
//	String textString = text.toString();
//	int count = 0, loc = 0;
//	boolean done = false;
//	do {
//	    int newloc = textString.indexOf(sep, loc);
//	    if(newloc == -1 || count == 3)  { // force 4th string to contain rest of input
//		done = true;
//		newloc = textString.length();
//	    }
//	    els[count++] = textString.substring(loc, newloc);
//	    loc = newloc + sep.length();
//	} while(!done);
//
//	currentSnap = -1;
//	snaps = new Vector();
//	frames = new Vector();
//
//	try {
//	    // parse and initialize snaps...
//	    StringTokenizer st = new StringTokenizer(els[1]);
//	    while(st.hasMoreTokens()) {
//		String part = st.nextToken() + "\n";
//		part += st.nextToken() + "\n";
//		part += st.nextToken() + "\n";
//		FaceFrame ff = new FaceFrame(part);
//		ff.setSnapshot(scaleSnap(faceApplet.getSnapshotImage(ff.getTargets())));
//		snaps.addElement(ff);
//	    }
//
//	    // ...and frames
//	    st = new StringTokenizer(els[2]);
//	    while(st.hasMoreTokens()) {
//		String part = st.nextToken() + "\n";
//		part += st.nextToken() + "\n";
//		part += st.nextToken() + "\n";
//		FaceFrame ff = new FaceFrame(part);
//		ff.setSnapshot(scaleSnap(faceApplet.getSnapshotImage(ff.getTargets())));
//		insertInOrder(frames, ff);
//	    }
//	}
//	catch(NoSuchElementException nse) {
//	    System.out.println(nse);
//	    nse.printStackTrace();
//	    return false;
//	}
//	catch(NullPointerException np) {
//	    System.out.println(np);
//	    np.printStackTrace();
//	    return false;
//	}
//
//	titleText.setText(els[0]);
//	storyText.setText(els[3]);
//	titleTextDisp.setText(els[0]);
//	storyTextDisp.setText(els[3]);
//
//	draw(DRAWALL);
//	return true;
//    }
//
//    // common processing for MOUSE_DRAG and MOUSE_UP
//    private void do_drag(Event e, boolean eraseold, boolean drawnew) {
//	// erase old rectangle or face
//	if(eraseold) {
//	    if(oldinanim) {
//		Graphics g = animFacePan.getGraphics();
//		g.setXORMode(Color.white);
//		g.setColor(ANIMBOX);
//		int[] oldim = getLocalCoords(animFacePan, oldx - xOffset, oldy - yOffset);
//		oldim[0] = Math.min(Math.max(oldim[0], 0), ANIMCOMPWIDTH - snapwidth);
//		oldim[1] = 0;
//		g.drawImage(currentFace.getSnapshot(), oldim[0], oldim[1], null);
//		g.setPaintMode();
//	    } else {
//		Graphics g = snapFacePan.getGraphics();
//		g.setXORMode(Color.white);
//		g.setColor(ANIMBOX);
//		int[] oldrect = getLocalCoords(snapFacePan, oldx - xOffset, oldy - yOffset);
//		g.drawRect(oldrect[0], oldrect[1], snapwidth - 1, SNAPHEIGHT - 1);
//		g.setPaintMode();
//	    }
//	}
//
//	// drag new rectangle or face
//	boolean inanim;
//	if(!wasinanim)
//	    inanim = containsGlobal(animFacePan, e.x, e.y);
//	else
//	    inanim = containsGlobal(animFacePan, getLocationGlobal(animFacePan).x, e.y);
//	if(drawnew) {
//	    if(inanim) {
//		if(deleteFrame == true) {
//		    deleteFrame = false;
//		    frames.removeElementAt(currentFrame);
//		    draw(DRAWANIM);
//		}
//		Graphics g = animFacePan.getGraphics();
//		g.setXORMode(Color.white);
//		g.setColor(ANIMBOX);
//		int[] t = getLocalCoords(animFacePan, e.x - xOffset, e.y - yOffset);
//		int im[] = new int[2];
//		im[0] = Math.min(Math.max(t[0], 0), ANIMCOMPWIDTH - snapwidth);
//		im[1] = 0;
//		int diff = t[0] - im[0];
//		scrPos += diff;
//		scrPos = Math.min(Math.max(scrPos, animScr.getMinimum()), animScr.getMaximum() - animScr.getVisible());
//		timeArrow = null;
//		draw(DRAWANIM | DRAWNUM);
//		animScr.setValue(scrPos);
//		g.drawImage(currentFace.getSnapshot(), im[0], im[1], null);
//		g.setPaintMode();
//	    } else {
//		Graphics g = snapFacePan.getGraphics();
//		g.setXORMode(Color.white);
//		g.setColor(ANIMBOX);
//		int[] rect = getLocalCoords(snapFacePan, e.x - xOffset, e.y - yOffset);
//		g.drawRect(rect[0], rect[1], snapwidth - 1, SNAPHEIGHT - 1);
//		g.setPaintMode();
//	    }
//	}
//	oldx = e.x;
//	oldy = e.y;
//	oldinanim = inanim;
//	wasinanim = wasinanim || inanim;
//    }
//
//    public boolean handleEvent(Event e) {
//	// for some reason, a standard Button click generates this code
//	if(e.id == 1001) {
//	    if(e.target == loadBut || e.target == loadList) {
//		int num = loadList.getSelectedIndex();
//		if(num != -1) {
//		    showMsgPan("loading...", false);
//		    if(load((String)seqnos.elementAt(num)))
//			showMsgPan("loading...done", true);
//		    else
//			showMsgPan("load error", true);
//		}
//		return true;
//	    } else if(e.target == cancelLoadBut) {
//		fileCard.show(filePan, "info");
//		return true;
//	    } else if(e.target == saveBut) {
//		String n = titleText.getText();
//		String s = storyText.getText();
//		if(!n.equals("")) {
//		    showMsgPan("saving...", false);
//		    titleTextDisp.setText(n);
//		    storyTextDisp.setText(s);
//		    if(save(n, s))
//			showMsgPan("saving...done", true);
//		    else
//			showMsgPan("save error", true);
//		}
//		return true;
//	    } else if(e.target == cancelSaveBut) {
//		titleTextDisp.setText(titleText.getText());
//		storyTextDisp.setText(storyText.getText());
//		fileCard.show(filePan, "info");
//		return true;
//	    } else if(e.target == contBut) {
//		fileCard.show(filePan, "info");
//		return true;
//	    }
//	} else if(e.id == Event.MOUSE_MOVE) {
//	    if(roll()) {
//		int oldSnap = rollSnap, oldFrame = rollFrame;
//		rollSnap = -1;
//		rollFrame = -1;
//		if(e.target == snapFacePan) {
//		    int[] p = getLocalCoords(snapFacePan, e.x, e.y);
//		    int tempsnap = snapcols * (int)(p[1] / SNAPHEIGHT) + (int)(p[0] / snapwidth);
//		    if(tempsnap < snaps.size()) {
//			rollSnap = tempsnap;
//		    }
//		} else if(e.target == animFacePan) {
//		    int[] p = getLocalCoords(animFacePan, e.x, e.y);
//		    double postime = (double)(p[0] + scrPos) / (timediv * snapwidth);
//		    FaceFrame ff;
//		    for(int i = frames.size() - 1; i >= 0; i--) {
//			ff = (FaceFrame)frames.elementAt(i);
//			if(postime >= ff.getTime() && postime < ff.getTime() + timemult) {
//			    rollFrame = i;
//			    break;
//			}
//		    }
//		}
//		if(rollSnap != oldSnap || rollFrame != oldFrame && !standalone) {
//		    if(rollSnap != -1) {
//			if(oldFrame == -1 && oldSnap == -1)
//			    preRollTargets = faceApplet.getTargets();
//			FaceFrame ff = (FaceFrame)snaps.elementAt(rollSnap);
//			faceApplet.setTargets(ff.getTargets());
//		    } else if(rollFrame != -1) {
//			if(oldSnap == -1 && oldFrame == -1)
//			    preRollTargets = faceApplet.getTargets();
//			FaceFrame ff = (FaceFrame)frames.elementAt(rollFrame);
//			faceApplet.setTargets(ff.getTargets());
//		    } else {
//			if(preRollTargets != null)
//			    faceApplet.setTargets(preRollTargets);
//		    }
//		}
//	    }
//	} else if(e.id == Event.MOUSE_EXIT) {
//	    if(roll()) {
//		if(e.target == snapFacePan || e.target == animFacePan) {
//		    rollSnap = rollFrame = -1;
//		    if(preRollTargets != null && !standalone)
//			faceApplet.setTargets(preRollTargets);
//		}
//	    }
//	} else if(e.id == Event.MOUSE_ENTER) {
//	    if(roll()) {
//		if(e.target == this && !standalone) {
//		    preRollTargets = faceApplet.getTargets();
//		}
//	    }
//	} else if(e.id == Event.MOUSE_UP) {
//	    if(dragMode == DRAGNONE) {
//		if(e.target == snap) {
//		    getFaceFrame();
//		    draw(DRAWSNAP);
//		    return true;
//		} else if(e.target == delete) {
//		    deleteSnap();
//		    return true;
//		} else if(e.target == play) {
//		    startAnim();
//		    return true;
//		} else if(e.target == stop) {
//		    stopAnim();
//		    return true;
//		} else if(e.target == clear) {
//		    clearSnaps();
//		    clearFrames();
//		    titleText.setText("");
//		    titleTextDisp.setText("");
//		    storyText.setText("");
//		    storyTextDisp.setText("");
//		    return true;
//		} else if(e.target == help) {                // legacy stuff, not used
//		    try {
//			URL u = new URL(codeBase + "facehelp.html");
//			//			getAppletContext().showDocument(u, "facehelp");
//		    }
//		    catch (MalformedURLException mu) {
//			System.out.println(mu);
//			mu.printStackTrace();
//		    }
//		    return true;
//		} else if(e.target == load) {
//		    showMsgPan("building list...", false);
//		    if(getList())
//			fileCard.show(filePan, "load");
//		    else
//			showMsgPan("list build error", true);
//		    return true;
//		} else if(e.target == save) {
//		    titleText.setText(titleTextDisp.getText());
//		    storyText.setText(storyTextDisp.getText());
//		    fileCard.show(filePan, "save");
//		    return true;
//		} else if(e.target == rollOverBut) {
//		    doRollOver = rollOverBut.getIsOn();
//		}
//	    } else if(dragMode == DRAGSNAP) {
//		if(dragdraw)
//		    do_drag(e, true, false);
//		if(oldinanim && currentSnap != -1) {
//		    int[] p = getLocalCoords(animFacePan, oldx - xOffset, oldy - yOffset);
//		    p[0] = Math.min(Math.max(p[0], 0), ANIMCOMPWIDTH - snapwidth);
//		    double newTime = (double)(p[0] + scrPos) / (double)(snapwidth * timediv);
//		    FaceFrame newAnim = new FaceFrame((FaceFrame)snaps.elementAt(currentSnap));
//		    newAnim.setTime(newTime);
//		    insertInOrder(frames, newAnim);
//		    draw(DRAWANIM);
//		}
//	    } else if(dragMode == DRAGANIM) {
//		do_drag(e, true, false);
//		if(currentFrame != -1) {
//		    if(oldinanim) {
//			int[] p = getLocalCoords(animFacePan, oldx - xOffset, oldy - yOffset);
//			p[0] = Math.min(Math.max(p[0], 0), ANIMCOMPWIDTH - snapwidth);
//			double newTime = (double)(p[0] + scrPos) / (double)(snapwidth * timediv);
//			currentFace.setTime(newTime);
//			insertInOrder(frames, currentFace);
//		    }
//		    draw(DRAWANIM);
//		}
//	    }
//	} else if(e.id == Event.MOUSE_DRAG) {
//	    if(dragMode != DRAGNONE) {
//		do_drag(e, dragdraw, true);
//		dragdraw = true;
//	    }
//	} else if(e.id == Event.MOUSE_DOWN) {
//	    dragMode = DRAGNONE;
//	    dragdraw = false;
//	    oldinanim = false;
//	    wasinanim = false;
//	    if(e.target == snapFacePan) {
//		int[] p = getLocalCoords(snapFacePan, e.x, e.y);
//		int tempsnap = snapcols * (int)(p[1] / SNAPHEIGHT) + (int)(p[0] / snapwidth);
//		int oldSnap = currentSnap;
//		if(tempsnap < snaps.size()) {
//		    currentSnap = tempsnap;
//		    currentFace = (FaceFrame)snaps.elementAt(currentSnap);
//		    dragMode = DRAGSNAP;
//		    xOffset = p[0] % snapwidth;
//		    yOffset = p[1] % SNAPHEIGHT;
//		    //		    draw(DRAWANIM);
//		}
//		if(oldSnap != currentSnap)
//		    draw(DRAWSNAP);
//		if(e.clickCount > 1) {
//		    FaceFrame ff = (FaceFrame)snaps.elementAt(currentSnap);
//		    if(roll())
//			preRollTargets = ff.getTargets();
//		    else
//			faceApplet.setTargets(ff.getTargets());
//		}
//	    } else if(e.target == animFacePan) {
//		int[] p = getLocalCoords(animFacePan, e.x, e.y);
//		double postime = (double)(p[0] + scrPos) / (timediv * snapwidth);
//		currentFrame = -1;
//		deleteFrame = false;
//		FaceFrame ff;
//		for(int i = frames.size() - 1; i >= 0; i--) {
//		    ff = (FaceFrame)frames.elementAt(i);
//		    if(postime >= ff.getTime() && postime < ff.getTime() + timemult) {
//			currentFrame = i;
//			dragMode = DRAGANIM;
//			xOffset = p[0] - (int)Math.round(ff.getTime() * timediv * snapwidth - scrPos);
//			yOffset = p[1];
//			break;
//		    }
//		}
//		if(currentFrame != -1) {
//		    currentFace = (FaceFrame)frames.elementAt(currentFrame);
//		    deleteFrame = true;
//		    if(e.clickCount > 1) {
//			ff = (FaceFrame)frames.elementAt(currentFrame);
//			if(roll())
//			    preRollTargets = ff.getTargets();
//			else
//			    faceApplet.setTargets(ff.getTargets());
//		    }
//		}
//	    }
//	} else if(e.id == Event.SCROLL_LINE_UP || e.id == Event.SCROLL_PAGE_UP ||
//		  e.id == Event.SCROLL_LINE_DOWN || e.id == Event.SCROLL_PAGE_DOWN ||
//		  e.id == Event.SCROLL_ABSOLUTE) {
//	    synchronized(animScr) {
//		scrPos = Math.min(((Integer)e.arg).intValue(), animScr.getMaximum() - animScr.getVisible());
//		timeArrow = null;
//		draw(DRAWNUM | DRAWANIM);
//	    }
//	}
//
//	return super.handleEvent(e);
//    }
//
//    // handle redraw of custom areas:  snapshot, anim timeline, anim frames
//    public void draw(int type) {
//	if(!initdone)
//	    return;
//	if((type & DRAWSNAP) != 0) {
//	    Graphics g = snapFacePan.getGraphics();
//	    g.setColor(SNAPBACK);
//	    g.fillRect(0, 0, SNAPCOMPWIDTH, SNAPCOMPHEIGHT);
//	    g.setColor(ARROWCOLOR);
//	    g.setFont(PALETTEFONT);
//	    FontMetrics fm = g.getFontMetrics(PALETTEFONT);
//	    String ps = new String("Palette");
//	    int x = (SNAPCOMPWIDTH - fm.stringWidth(ps)) / 2;
//	    int y = fm.getAscent();
//	    int border = 5;
//	    g.drawString(ps, x, y + border);
//	    if(paletteArrow == null) {
//		int lw = (SNAPCOMPWIDTH - 2 * border) / 2;                            // width of arrow line
//		int ax = (SNAPCOMPWIDTH / 2) - (lw / 2);                              // x coord of arrow top right
//		int ay = fm.getHeight() + border;                                     // y coord of arrow top right
//		int wy = SNAPCOMPHEIGHT - border - (SNAPCOMPWIDTH - 2 * border) / 2;  // x coord where arrow head starts
//		paletteArrow = new Polygon();
//		paletteArrow.addPoint(ax, ay);
//		paletteArrow.addPoint(ax, wy);
//		paletteArrow.addPoint(SNAPCOMPWIDTH - border, wy);
//		paletteArrow.addPoint(SNAPCOMPWIDTH / 2, SNAPCOMPHEIGHT - border);
//		paletteArrow.addPoint(border, wy);
//		paletteArrow.addPoint(SNAPCOMPWIDTH - ax, wy);
//		paletteArrow.addPoint(SNAPCOMPWIDTH - ax, ay);
//	    }
//	    g.fillPolygon(paletteArrow);
//	    for(int i = 0; i < snaps.size(); i++) {
//		Image snap = ((FaceFrame)snaps.elementAt(i)).getSnapshot();
//		int[] p = toSnapGrid(i);
//		g.drawImage(snap, p[0], p[1], null);
//	    }
//	    if(currentSnap != -1) {
//		int[] p = toSnapGrid(currentSnap);
//		g.setColor(SELECTBOX);
//		g.drawRect(p[0], p[1], snapwidth-1, SNAPHEIGHT-1);
//	    }
//	}
//
//	if((type & DRAWNUM) != 0) {
//	    Graphics g = animTimePan.getGraphics();
//	    g.setColor(NUMBACK);
//	    g.fillRect(0, 0, ANIMCOMPWIDTH, ANIMTIMECOMPHEIGHT);
//	    g.setColor(NUMCOLOR);
//	    int clicks = ANIMSECONDS * 2 + 1;
//
//	    for(int i = 0; i < clicks; i++) {
//		g.drawLine(i * framewidth / 2 - scrPos, ANIMTIMECOMPHEIGHT - 4, i * framewidth / 2 - scrPos, ANIMTIMECOMPHEIGHT - 1);
//		if(i % 2 == 0)
//		    g.drawString(i / 2 + "", i * framewidth / 2 - scrPos, getFontMetrics(getFont()).getHeight() + 1);
//	    }
//	}
//
//	if((type & DRAWANIM) != 0) {
//	    Graphics g = animFacePan.getGraphics();
//	    g.setColor(ANIMBACK);
//	    g.fillRect(0, 0, ANIMCOMPWIDTH, ANIMFACECOMPHEIGHT);
//	    g.setColor(ARROWCOLOR);
//	    g.setFont(TIMEFONT);
//	    FontMetrics fm = g.getFontMetrics(TIMEFONT);
//	    int border = 5;
//	    int y = SNAPHEIGHT / 2 + fm.getAscent() - fm.getHeight() / 2;
//	    String ts = new String("Timeline");
//	    g.drawString(ts, border - scrPos, y);
//	    if(timeArrow == null) {
//		int lh = (SNAPHEIGHT - 2 * border) / 2;                                  // height of arrow line
//		int ax = border * 2 + fm.stringWidth(ts) - scrPos;                       // x coord of arrow start
//		int ay = (SNAPHEIGHT / 2) - (lh / 2);                                    // y coord of arrow line top
//		int wx = animwidth - scrPos - border - (SNAPHEIGHT - 2 * border) / 2;    // x coord where arrow head starts
//		timeArrow = new Polygon();
//		timeArrow.addPoint(ax, ay);
//		timeArrow.addPoint(wx, ay);
//		timeArrow.addPoint(wx, border);
//		timeArrow.addPoint(animwidth - scrPos - border, SNAPHEIGHT / 2);
//		timeArrow.addPoint(wx, SNAPHEIGHT - border);
//		timeArrow.addPoint(wx, SNAPHEIGHT - ay);
//		timeArrow.addPoint(ax, SNAPHEIGHT - ay);
//	    }
//	    g.fillPolygon(timeArrow);
//	    for(int i = 0; i < frames.size(); i++) {
//		FaceFrame ff = (FaceFrame)frames.elementAt(i);
//		Image snap = ff.getSnapshot();
//		double time = ff.getTime();
//		g.drawImage(snap, (int)Math.round(time * timediv * snapwidth) - scrPos, 0, null);
//	    }
//	}
//    }
//
//    public void paint(Graphics g) {
//	if(initdone)
//	    draw(DRAWALL);
//    }
//
//    // recalculate size, logistical vars which change based on frame/applet size
//    private void calcshape(int w, int h) {
//	ANIMCOMPWIDTH = w - SNAPCOMPWIDTH;
//	SNAPCOMPHEIGHT = h - ICONCOMPHEIGHT - TOPSTART;
//	ANIMCOMPHEIGHT = ANIMTIMECOMPHEIGHT + ANIMFACECOMPHEIGHT + ANIMSCRHEIGHT + 15;
//	FILECOMPHEIGHT = h - (ICONCOMPHEIGHT + ANIMCOMPHEIGHT + TOPSTART);
//
//	snapcols = SNAPCOMPWIDTH / snapwidth;
//	snaprows = SNAPCOMPHEIGHT / snapwidth;
//	maxsnaps = snapcols * snaprows;
//	framewidth = (int)(snapwidth * timediv);
//	animwidth = ANIMSECONDS * framewidth;
//    }
//
//    public synchronized void reshape(int x, int y, int w, int h) {
//	super.reshape(x, y, w, h);
//
//	if(initdone) {
//	    calcshape(w, h);
//	    timeArrow = paletteArrow = null;          // force arrow redraw
//
//	    snapControlPan.reshape(0, TOPSTART, SNAPCOMPWIDTH, ICONCOMPHEIGHT);
//	    snapFacePan.reshape(0, ICONCOMPHEIGHT + TOPSTART, SNAPCOMPWIDTH, SNAPCOMPHEIGHT);
//	    animControlPan.reshape(SNAPCOMPWIDTH, TOPSTART, ANIMCOMPWIDTH, ICONCOMPHEIGHT);
//	    animPan.reshape(SNAPCOMPWIDTH, ICONCOMPHEIGHT + TOPSTART, ANIMCOMPWIDTH, ANIMCOMPHEIGHT);
//	    animTimePan.reshape(0, 0, ANIMCOMPWIDTH, ANIMTIMECOMPHEIGHT);
//	    animFacePan.reshape(0, ANIMTIMECOMPHEIGHT, ANIMCOMPWIDTH, ANIMFACECOMPHEIGHT);
//	    animScr.reshape(0, ANIMTIMECOMPHEIGHT + ANIMFACECOMPHEIGHT, ANIMCOMPWIDTH, ANIMSCRHEIGHT);
//	    filePan.reshape(SNAPCOMPWIDTH, ICONCOMPHEIGHT + ANIMCOMPHEIGHT + TOPSTART, ANIMCOMPWIDTH, FILECOMPHEIGHT);
//	}
//    }
//
//    public void deleteSnap() {
//	if(currentSnap >= 0 && currentSnap < snaps.size())
//	    snaps.removeElementAt(currentSnap);
//	if(currentSnap == snaps.size())
//	    currentSnap -= 1;
//	draw(DRAWSNAP);
//    }
//
//    public void startAnim() {
//	if(athread == null)
//	    athread = new AnimThread(this, frames, faceApplet, animTimePan.getGraphics(), animScr);
//	else if(athread.isAlive()) {
//	    return;
//	} else {
//	    athread.stop();
//	    athread = new AnimThread(this, frames, faceApplet, animTimePan.getGraphics(), animScr);
//	}
//	athread.start();
//    }
//
//    public void stopAnim() {
//	if(athread != null) {
//	    athread.stop();
//	    try {
//		Thread.sleep(250);
//	    }
//	    catch(InterruptedException ie) {
//		;
//	    }
//	}
//	currentFrame = -1;
//	athread = null;
//	draw(DRAWNUM);
//    }
//
//    // scales an image to snapshot size.  not used.
//    private Image scaleImage(Image before) {
//	int bw = before.getWidth(this), bh = before.getHeight(this);
//	int aw = snapwidth, ah = SNAPHEIGHT;
//	int bp[] = new int[bw * bh], ap[] = new int[aw * ah];
//	double rx = (double)bw / (double)aw, ry = (double)bh / (double)ah;
//
//	PixelGrabber bpp = new PixelGrabber(before, 0, 0, bw, bh, bp, 0, bw);
//        try {
//            bpp.grabPixels();
//        } catch (InterruptedException ie) {
//            System.out.println("interrupted waiting for pixels!");
//            return null;
//        }
//        if ((bpp.status() & ImageObserver.ABORT) != 0) {
//            System.out.println("image fetch aborted or errored");
//            return null;
//        }
//
//	for(int ar = 0; ar < ah; ar++) {
//	    for(int ac = 0; ac < aw; ac++) {
//		double ta = 0, tr = 0, tg = 0, tb = 0;
//		for(int br = (int)Math.floor(ar * ry); br < (int)Math.ceil((ar + 1) * ry); br++)
//		    for(int bc = (int)Math.floor(ac * rx); bc < (int)Math.ceil((ac + 1) * rx); bc++) {
//			double xmin = Math.max(bc, ac * rx);
//			double xmax = Math.min(bc + 1, (ac + 1) * rx);
//			double ymin = Math.max(br, ar * ry);
//			double ymax = Math.min(br + 1, (ar + 1) * ry);
//			double fac = (xmax - xmin) * (ymax - ymin);
//			int pix = br * bw + bc;
//			int r = (bp[pix] & 0xff0000) >> 16;
//			int g = (bp[pix] & 0xff00) >> 8;
//			int b = bp[pix] & 0xff;
//			//System.out.println("ar:" + ar + " ac:" + ac + " br:" + br + " bc:" + bc + " pix:" + pix + ":[" + r + ":" + g + ":" + b + "] fac:" + fac);
//			tr += (double)r * fac;
//			tg += (double)g * fac;
//			tb += (double)b * fac;
//		    }
//		double area = rx * ry;
//		ap[ar * aw + ac] = 0xff000000 | ((int)Math.round(tr / area) << 16) | ((int)Math.round(tg / area) << 8) | (int)Math.round(tb / area);
//		//System.out.print((int)(tr / area) + " ");
//	    }
//	    //System.out.println();
//	}
//
//	Image after = createImage(new MemoryImageSource(aw, ah, ap, 0, aw));
//	System.out.println(after + "  " + after.getWidth(this) + "," + after.getHeight(this));
//	return after;
//    }
//
//    // get coords of component relative to top-level frame
//    public Point getLocationGlobal(Component c) {
//	Point p = c.location();
//	Container cn = c.getParent();
//	while(!(cn == this)) {
//	    Point n = cn.location();
//	    p.x += n.x;
//	    p.y += n.y;
//	    cn = cn.getParent();
//	}
//
//	return p;
//    }
//
//    // get local coords within component of x, y which are specified in top-level frame coords
//    public int[] getLocalCoords(Component c, int x, int y) {
//	Point p = getLocationGlobal(c);
//	int[] n = new int[2];
//	n[0] = x - p.x;
//	n[1] = y - p.y;
//
//	return n;
//    }
//
//    // find the x, y coords where the nth snapshot should be drawn
//    public int[] toSnapGrid(int n) {
//	int[] p = new int[2];
//	p[0] = (n % snapcols) * snapwidth;
//	p[1] = (n / snapcols) * SNAPHEIGHT;
//
//	return p;
//    }
//
//    // does component c contain the point x, y in top-level frame coordinates?
//    public boolean containsGlobal(Component c, int x, int y) {
//	Point p = getLocationGlobal(c);
//	Dimension d = c.size();
//
//	return ((x >= p.x) && (x < p.x + d.width) && (y >= p.y) && (y < p.y + d.height));
//    }
//
//    // insert a face frame into the vector v in its proper order based on its time component
//    protected void insertInOrder(Vector v, FaceFrame ff) {
//	int i = 0;
//	while(i < v.size()) {
//	    if(ff.getTime() <= ((FaceFrame)v.elementAt(i)).getTime())
//		break;
//	    i++;
//	}
//	v.insertElementAt(ff, i);
//    }
//
//    // disable rollovers while animation is active
//    private boolean roll() {
//	if(doRollOver)
//	    if(athread == null)
//		return true;
//	    else if(!athread.isAlive())
//		return true;
//	return false;
//    }
//
//    // display string in message panel, and show continue button if c is true
//    private void showMsgPan(String m, boolean c) {
//	Container t = contBut.getParent();
//	if(c && (t == null))
//	    msgPan.add(contBut);
//	else if(!c && (t != null))
//	    msgPan.remove(contBut);
//	message.setText(m);
//	msgPan.layout();
//	fileCard.show(filePan, "msg");
//    }
//}
//
//// Encapsulates functionality for making a 32x32 icon with a gif
//// and separate borders for default state, rollover, and mousedown
//class GraphicButton extends Panel {
//    public static final int DEFAULT = 0;
//    public static final int ROLLOVER = 1;
//    public static final int CLICK = 2;
//    public static final Color BACKGROUNDColor = new Color(204, 204, 204);
//    public static final Color BORDERColor = Color.black;
//    public static final Color LIGHTColor = Color.white;
//    public static final Color DARKColor = new Color(153, 153, 153);
//
//    Image icon;
//    int mode;
//
//    public GraphicButton(Image im) {
//	super();
//	mode = DEFAULT;
//	icon = im;
//    }
//
//    public boolean handleEvent(Event e) {
//	if(e.id == Event.MOUSE_ENTER) {
//	    mode = ROLLOVER;
//	    drawBorder();
//	    return true;
//	} else if(e.id == Event.MOUSE_EXIT) {
//	    mode = DEFAULT;
//	    drawBorder();
//	    return true;
//	} else if(e.id == Event.MOUSE_DOWN) {
//	    mode = CLICK;
//	    drawBorder();
//	    return false;
//	} else if(e.id == Event.MOUSE_UP) {
//	    if(mode == CLICK) {
//		mode = ROLLOVER;
//		drawBorder();
//	    }
//	    return false;
//	}
//
//	return false;
//    }
//
//    public void drawBorder() {
//	Color border, topleft, bottomright;
//
//	switch(mode) {
//	case ROLLOVER:
//	    border = BORDERColor;
//	    topleft = LIGHTColor;
//	    bottomright = DARKColor;
//	    break;
//	case CLICK:
//	    border = BORDERColor;
//	    topleft = DARKColor;
//	    bottomright = LIGHTColor;
//	    break;
//	default:
//	    border = topleft = bottomright = BACKGROUNDColor;
//	    break;
//	}
//
//	Graphics a = this.getGraphics();
//	a.setColor(border);
//	a.drawRect(0, 0, 31, 31);
//	a.setColor(topleft);
//	a.drawLine(1, 1, 30, 1);
//	a.drawLine(1, 2, 29, 2);
//	a.drawLine(1, 1, 1, 29);
//	a.drawLine(2, 1, 2, 28);
//	a.setColor(bottomright);
//	a.drawLine(1, 30, 30, 30);
//	a.drawLine(2, 29, 30, 29);
//	a.drawLine(30, 2, 30, 30);
//	a.drawLine(29, 3, 29, 30);
//    }
//
//    public void setImage(Image im) {
//	icon = im;
//	paint(this.getGraphics());
//    }
//
//    public void paint(Graphics g) {
//	Graphics a = this.getGraphics();
//	a.drawImage(icon, 0, 0, this);
//	drawBorder();
//    }
//
//    public Dimension getPreferredSize() {
//	return new Dimension(32, 32);
//    }
//}
//
//// 32 x 32 icon toggle button
//class OnOffGraphicButton extends GraphicButton {
//    boolean isOn;
//
//    public OnOffGraphicButton(Image im) {
//	this(im, false);
//    }
//
//    public OnOffGraphicButton(Image im, boolean o) {
//	super(im);
//	isOn = o;
//	mode = isOn ? CLICK : DEFAULT;
//    }
//
//    public boolean handleEvent(Event e) {
//	if(e.id == Event.MOUSE_ENTER) {
//	    mode = isOn ? CLICK : ROLLOVER;
//	    drawBorder();
//	    return true;
//	} else if(e.id == Event.MOUSE_EXIT) {
//	    mode = isOn ? CLICK : DEFAULT;
//	    drawBorder();
//	    return true;
//	} else if(e.id == Event.MOUSE_DOWN) {
//	    isOn = !isOn;
//	    mode = isOn ? CLICK : DEFAULT;
//	    drawBorder();
//	    return false;
//	} else if(e.id == Event.MOUSE_UP) {
//	    if(!isOn) {
//		mode = ROLLOVER;
//		drawBorder();
//	    }
//	    return false;
//	}
//
//	return false;
//    }
//
//    public boolean getIsOn() {
//	return isOn;
//    }
//
//    public void setIsOn(boolean on) {
//	isOn = on;
//	mode = isOn ? CLICK : DEFAULT;
//	drawBorder();
//    }
//}
//
//// thread to control 'playing' of animation
//// sends messages to face applet, draws tic on timeline
//class AnimThread extends Thread {
//    private Vector frames;
//    private Face2bApplet faceApplet;
//    private long startTime;
//    private double eTime;
//    private int marker;
//    private boolean running;
//    private Graphics g;
//    private GraphAnim callClass;
//    private Scrollbar sb;
//    private int oldScr;
//
//    public AnimThread(GraphAnim cc, Vector fr, Face2bApplet fa, Graphics gr, Scrollbar s) {
//	frames = fr;
//	faceApplet = fa;
//	startTime = System.currentTimeMillis();
//	eTime = 0;
//	marker = 0;
//	running = true;
//	g = gr;
//	callClass = cc;
//	sb = s;
//    }
//
//    public void run() {
//	boolean first = true;
//	double currTime;
//	FaceFrame ff = (FaceFrame)frames.elementAt(marker);
//	oldScr = sb.getValue();
//	while(marker < frames.size()) {
//	    currTime = (double)(System.currentTimeMillis() - startTime) / 1000.0;
//	    while(eTime <= ff.getTime() && ff.getTime() <= currTime) {
//		faceApplet.setTargets(ff.getTargets());
//		marker++;
//		if(marker == frames.size())
//		    break;
//		ff = (FaceFrame)frames.elementAt(marker);
//	    }
//	    synchronized(sb) {
//		drawTic(eTime, currTime, first, false);
//	    }
//
//	    eTime = currTime;
//	    try {
//		sleep(30);
//	    }
//	    catch(InterruptedException ie) {
//		;
//	    }
//	    first = false;
//	}
//
//	drawTic(eTime, 0, first, true);
//	running = false;
//    }
//
//    public void drawTic(double elapsed, double current, boolean first, boolean last) {
//	g.setXORMode(GraphAnim.NUMBACK);
//	g.setColor(Color.white);
//	if(!first && oldScr == sb.getValue()) {
//	    int x = (int)Math.round(elapsed * callClass.timediv * callClass.snapwidth);
//	    g.drawLine(x - sb.getValue(), callClass.ANIMTIMECOMPHEIGHT - 4, x - sb.getValue(), callClass.ANIMTIMECOMPHEIGHT - 1);
//	}
//	if(!last) {
//	    int x = (int)Math.round(current * callClass.timediv * callClass.snapwidth);
//	    g.drawLine(x - sb.getValue(), callClass.ANIMTIMECOMPHEIGHT - 4, x - sb.getValue(), callClass.ANIMTIMECOMPHEIGHT - 1);
//	}
//	g.setPaintMode();
//	oldScr = sb.getValue();
//    }
//
//    public boolean isRunning() {
//	return running;
//    }
//}
//
//// spacer for use in icon bars
//class Spacer extends Panel {
//    private int width, height;
//
//    public Spacer(int w, int h) {
//	super();
//	width = w;
//	height = h;
//    }
//
//    public Dimension getPreferredSize() {
//	return new Dimension(width, height);
//    }
//}
//
//// need to do this so that snapshot and animation face panels get their paint events
//class DrawPanel extends Panel {
//    private int type;
//    private GraphApp rent;
//
//    public DrawPanel(GraphApp g, int t) {
//	rent = g;
//	type = t;
//    }
//
//    public void paint(Graphics g) {
//	rent.draw(type);
//    }
//}
//
//class MessagePanel extends Panel {
//    private Label lab;
//    private BorderLayout lay;
//
//    public MessagePanel(String s) {
//	if(lab == null) {
//	    lab = new Label();
//	    lab.setAlignment(Label.CENTER);
//	    lay = new BorderLayout();
//	    setLayout(lay);
//	    add("Center", lab);
//	}
//	setText(s);
//    }
//
//    public void setText(String s) {
//	lab.setText(s);
//	//	getLayout().invalidateLayout(this);
//    }
//}
//    static class GraphApp extends JPanel {
//
//        protected int snaprows;
//        protected int maxsnaps;
//        protected int framewidth;
//        protected int animwidth;
//        protected int xOffset;
//        protected int yOffset;
//        protected boolean dragdraw;
//        protected int dragMode;
//        protected int oldx;
//        protected int oldy;
//        protected boolean oldinanim;
//        protected boolean wasinanim;
//        protected boolean deleteFrame;
//        protected int fullAnimWidth;
//        protected int currentFrame;
//        protected FaceFrame currentFace;
//        protected Vector seqnos;
//        protected boolean isFrame;
//        boolean standalone;
//        int snapwidth;
//        int snapcols;
//        double timemult;
//        double timediv;
//        int scrPos;
//
//        Vector frames;
//        int currentSnap;
//        boolean doRollOver;
//        int rollSnap;
//        int rollFrame;
//        double[][] preRollTargets;
//        String codeBase;
//        Panel snapControlPan;
//        Panel animControlPan;
//        DrawGraphAppPanel snapFacePan;
//        Panel animPan;
//
//
//        Image playIm;
//        Image pauseIm;
//        Polygon timeArrow;
//        Polygon paletteArrow;
//
//
//        public GraphApp(String s, String s1) {
//            this(s, s1, false);
//        }
//
//        GraphApp(String s, String s1, boolean flag) {
//            super(new GridBagLayout());
//
//
//            codeBase = s;
//
//            standalone = flag;
//
//
//            standalone = true;
//
//            timemult = 0.5D;
//            timediv = 1 / timemult;
//            snapwidth = (int) ((bounds().width * 40F) / bounds().height);
//            scrPos = 0;
//            currentSnap = -1;
//            rollSnap = -1;
//            frames = new Vector();
//            rollFrame = -1;
//            preRollTargets = null;
//            doRollOver = false;
//
//
//            snapControlPan = new Panel();
//            snapFacePan = new DrawGraphAppPanel(this, 1, this);
//            animControlPan = new Panel();
//            var gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.anchor = 13;
//            gbc.fill = 0;
//            gbc.weightx = 0;
//            gbc.weighty = 0;
//            gbc.insets = new Insets(3, 3, 3, 3);
//            add(snapControlPan, gbc);
//            gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 1;
//            gbc.anchor = 12;
//            gbc.fill = 0;
//            gbc.weightx = 0;
//            gbc.weighty = 1;
//            gbc.insets = new Insets(3, 3, 3, 3);
//            add(snapFacePan, gbc);
//            gbc = new GridBagConstraints();
//            gbc.gridx = 1;
//            gbc.gridy = 0;
//            gbc.anchor = 17;
//            gbc.fill = 1;
//            gbc.weightx = 1;
//            gbc.weighty = 0;
//            add(animControlPan, gbc);
//            gbc = new GridBagConstraints();
//            gbc.gridx = 1;
//            gbc.gridy = 1;
//            gbc.anchor = 17;
//            gbc.fill = 1;
//            gbc.weightx = 1;
//            gbc.weighty = 1;
//            gbc.insets = new Insets(3, 3, 6, 3);
//
//            gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.anchor = 13;
//            gbc.fill = 0;
//            gbc.weightx = 0;
//            gbc.weighty = 0;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 1;
//            gbc.anchor = 12;
//            gbc.fill = 0;
//            gbc.weightx = 0;
//            gbc.weighty = 1;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 1;
//            gbc.gridy = 0;
//            gbc.anchor = 17;
//            gbc.fill = 1;
//            gbc.weightx = 1;
//            gbc.weighty = 0;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 1;
//            gbc.gridy = 1;
//            gbc.anchor = 17;
//            gbc.fill = 1;
//            gbc.weightx = 1;
//            gbc.weighty = 1;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 2;
//            gbc.gridwidth = 2;
//            gbc.anchor = 10;
//            gbc.fill = 0;
//            gbc.weightx = 1;
//            gbc.weighty = 0;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.gridwidth = 2;
//            gbc.anchor = 10;
//            gbc.fill = 1;
//            gbc.weightx = 1;
//            gbc.weighty = 1;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 1;
//            gbc.anchor = 13;
//            gbc.fill = 0;
//            gbc.weightx = 1;
//            gbc.weighty = 0;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 1;
//            gbc.gridy = 1;
//            gbc.anchor = 17;
//            gbc.fill = 0;
//            gbc.weightx = 1;
//            gbc.weighty = 0;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.anchor = 10;
//            gbc.fill = 1;
//            gbc.weightx = 1;
//            gbc.weighty = 1;
//            gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 1;
//            gbc.anchor = 10;
//            gbc.fill = 0;
//            gbc.weightx = 1;
//            gbc.weighty = 0;
//            gbc.insets = new Insets(3, 3, 3, 3);
//
//        }
//
//        protected static void insertInOrder(Vector vector, FaceFrame faceframe) {
//            int i;
//            for (i = 0; i < vector.size(); i++)
//                if (faceframe.getTime() <= ((FaceFrame) vector.elementAt(i)).getTime())
//                    break;
//
//            vector.insertElementAt(faceframe, i);
//        }
//
//        public Image scaleSnap(Image image) {
//            Image image1 = createImage(snapwidth, 40);
//            Graphics g = image1.getGraphics();
//            g.drawImage(image, 0, 0, snapwidth, 40, Color.white, null);
//            return image1;
//        }
//
//        public void clearSnaps() {
//            currentSnap = -1;
//            draw(1);
//        }
//
//        public void clearFrames() {
//            frames = new Vector();
//            draw(8);
//        }
//
//        protected String getFrameString() {
//            String s = "";
//            for (int i = 0; i < frames.size(); i++) {
//                FaceFrame faceframe = (FaceFrame) frames.elementAt(i);
//                s += faceframe.toString();
//            }
//
//            return s.substring(0, s.length() - 1);
//        }
//
//        void draw(int i) {
//        }
//
//        public void paint(Graphics g) {
//            draw(11);
//        }
//
//        public synchronized void reshape(int i, int j, int k, int l) {
//            super.reshape(i, j, k, l);
//            timeArrow = paletteArrow = null;
//            snapControlPan.reshape(0, 0, 100, 32);
//        }
//
//        private Image scaleImage(Image image) {
//            int i = image.getWidth(this);
//            int j = image.getHeight(this);
//            int k = snapwidth;
//            int l = 40;
//            int[] ai = new int[i * j];
//            int[] ai1 = new int[k * l];
//            double d = ((double) i) / k;
//            double d1 = ((double) j) / l;
//            PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, i, j, ai, 0, i);
//            try {
//                pixelgrabber.grabPixels();
//            } catch (InterruptedException _ex) {
//                System.out.println("interrupted waiting for pixels!");
//                return null;
//            }
//            if ((pixelgrabber.status() & 0x80) != 0) {
//                System.out.println("image fetch aborted or errored");
//                return null;
//            }
//            for (int i1 = 0; i1 < l; i1++) {
//                for (int j1 = 0; j1 < k; j1++) {
//                    double d2 = 0;
//                    double d3 = 0;
//                    double d4 = 0;
//                    for (int k1 = (int) Math.floor(i1 * d1); k1 < (int) Math.ceil((i1 + 1) * d1); k1++) {
//                        for (int l1 = (int) Math.floor(j1 * d); l1 < (int) Math.ceil((j1 + 1) * d); l1++) {
//                            double d6 = Math.max(l1, j1 * d);
//                            double d7 = Math.min(l1 + 1, (j1 + 1) * d);
//                            double d8 = Math.max(k1, i1 * d1);
//                            double d9 = Math.min(k1 + 1, (i1 + 1) * d1);
//                            double d10 = (d7 - d6) * (d9 - d8);
//                            int i2 = k1 * i + l1;
//                            int j2 = (ai[i2] & 0xff0000) >> 16;
//                            int k2 = (ai[i2] & 0xff00) >> 8;
//                            int l2 = ai[i2] & 0xff;
//                            d2 += j2 * d10;
//                            d3 += k2 * d10;
//                            d4 += l2 * d10;
//                        }
//
//                    }
//
//                    double d5 = d * d1;
//                    ai1[i1 * k + j1] = 0xff000000 | (int) Math.round(d2 / d5) << 16 | (int) Math.round(d3 / d5) << 8 | (int) Math.round(d4 / d5);
//                }
//
//            }
//
//            Image image1 = createImage(new MemoryImageSource(k, l, ai1, 0, k));
//            System.out.println(image1 + "  " + image1.getWidth(this) + "," + image1.getHeight(this));
//            return image1;
//        }
//
//        Point getLocationGlobal(Component component) {
//            Point point = component.location();
//            for (Container container = component.getParent(); container != this; container = container.getParent()) {
//                Point point1 = container.location();
//                point.x += point1.x;
//                point.y += point1.y;
//            }
//
//            return point;
//        }
//
//        public int[] getLocalCoords(Component component, int i, int j) {
//            Point point = getLocationGlobal(component);
//            int[] ai = new int[2];
//            ai[0] = i - point.x;
//            ai[1] = j - point.y;
//            return ai;
//        }
//
//        public int[] toSnapGrid(int i) {
//            int[] ai = new int[2];
//            ai[0] = (i % snapcols) * snapwidth;
//            ai[1] = (i / snapcols) * 40;
//            return ai;
//        }
//
//        public boolean containsGlobal(Component component, int i, int j) {
//            Point point = getLocationGlobal(component);
//            Dimension dimension = component.size();
//            return i >= point.x && i < point.x + dimension.width && j >= point.y && j < point.y + dimension.height;
//        }
//
//        static class DrawGraphAppPanel extends Panel {
//
//
//            private final GraphApp outer;
//            private final int type;
//
//            DrawGraphAppPanel(GraphApp graphanim, int i, final GraphApp outer) {
//                this.outer = outer;
//                type = i;
//            }
//
//            public void paint(Graphics g) {
//                outer.draw(type);
//            }
//        }
//    }