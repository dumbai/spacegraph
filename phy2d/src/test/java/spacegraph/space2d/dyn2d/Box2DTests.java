package spacegraph.space2d.dyn2d;

import jcog.Util;
import jcog.math.v2;
import spacegraph.space2d.dyn2d.fracture.*;
import spacegraph.space2d.dyn2d.jbox2d.*;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.EdgeShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.collision.shapes.Shape;
import spacegraph.space2d.phys.dynamics.*;
import spacegraph.space2d.phys.dynamics.joints.DistanceJoint;
import spacegraph.space2d.phys.dynamics.joints.Joint;
import spacegraph.space2d.phys.dynamics.joints.MouseJoint;
import spacegraph.space2d.phys.dynamics.joints.MouseJointDef;
import spacegraph.space2d.phys.fracture.Material;
import spacegraph.space2d.phys.fracture.PolygonFixture;
import spacegraph.space2d.phys.fracture.util.MyList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * GUI pre testovacie scenare. Ovladanie:
 * s - start
 * r - reset
 * koliecko mysi - priblizovanie/vzdalovanie
 * prave tlacitko mysi - posuvanie sceny
 * lave tlacitko mysi - hybanie objektami
 *
 * @author Marek Benovic
 */
public class Box2DTests extends JComponent  {
    private final Dimension screenSize = new Dimension(1024, 540);
    private final v2 center = new v2();
    private float zoom;
    private volatile Dynamics2D w;

    private final v2 startCenter = new v2();
    private volatile Point clickedPoint;
    private volatile Graphics2D g;
    private volatile boolean running = false;
    private volatile Consumer<Dynamics2D> testCase;

    private volatile Body2D ground;

    int velocity = 8;
    int iterations = 8;
    float fps = 40;

    /**
     * Pole testovacich scenarov
     */
    private static final Consumer<Dynamics2D>[] cases = new Consumer[]{

            new MainScene(),

            new Cube(),
            new Circle(),

            new RotatedBody(),
            new StaticBody(),
            new Fluid(),
            new Materials(Material.UNIFORM),
            new Materials(Material.DIFFUSION),
            new Materials(Material.GLASS),

            new ChainTest(),
            new BlobTest4(),
            new TheoJansenTest(),
            new ParticlesTest(),
            new VerletTest(),
            new CarTest()
    };


    private Box2DTests() {
        initWorld();

        initMouse();

        zoom = 10;
        center.set(0, 7);
    }

    private volatile MouseJointDef mjdef;
    private volatile MouseJoint mj;
    private volatile boolean destroyMj = false;
    private volatile v2 mousePosition = new v2();

    private void initMouse() {
        addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) {
                zoom *= 1.25f * -e.getWheelRotation();
            } else {
                zoom /= 1.25f * e.getWheelRotation();
            }

            zoom = Math.min(zoom, 100);
            zoom = Math.max(zoom, 0.1f);
            repaint();
        });

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                mousePosition = getPoint(p);
                if (clickedPoint != null) {
                    p.x -= clickedPoint.x;
                    p.y -= clickedPoint.y;
                    center.x = startCenter.x - p.x / zoom;
                    center.y = startCenter.y + p.y / zoom;
                } else {
                    if (mj != null) {
                        mj.setTarget(mousePosition);
                    }
                }
                if (!running) {
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                mousePosition = getPoint(p);
                if (!running) {
                    repaint();
                }
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                Point p = new Point(x, y);
                /*synchronized(Tests.this)*/
                switch (e.getButton()) {
                    case 3 -> {
                        startCenter.set(center);
                        clickedPoint = p;
                    }
                    case 1 -> {
                        v2 v = getPoint(p);
                        {
                            w.bodies(b -> {
                                for (Fixture f = b.fixtures(); f != null; f = f.next) {
                                    if (f.testPoint(v)) {
                                        MouseJointDef def = new MouseJointDef();

                                        def.bodyA = ground;
                                        def.bodyB = b;
                                        def.collideConnected = true;

                                        def.target.set(v);

                                        def.maxForce = 500.0f * b.getMass();
                                        def.dampingRatio = 0;

                                        mjdef = def;
                                        return;
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                
                switch (e.getButton()) {
                    case 3:
                        clickedPoint = null;
                        break;
                    case 1:
                        if (mj != null) {
                            destroyMj = true;
                        }
                        break;
                }
                
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    private void initWorld() {
        w = new Dynamics2D(new v2(0, -9.81f));
        w.setParticleRadius(0.2f);
        w.setParticleDensity(1.0f);
        w.setContinuousPhysics(true);
        //w.setSubStepping(true);

        setBox();

        mj = null;
        destroyMj = false;
        mjdef = null;
    }

    private void setBox() {
        ground = w.addBody(new BodyDef(BodyType.STATIC),
                new FixtureDef(PolygonShape.box(40, 5),
                        0, 0));
        ground.setTransform(new v2(0, -5.0f), 0);

        Body2D wallRight = w.addBody(new BodyDef(BodyType.STATIC),
                new FixtureDef(PolygonShape.box(2, 40), 0, 0));
        wallRight.setTransform(new v2(-41, 30.0f), 0);

        Body2D wallLeft = w.addBody(new BodyDef(BodyType.STATIC),
                new FixtureDef(PolygonShape.box(2, 40), 0, 0));
        wallLeft.setTransform(new v2(41, 30.0f), 0);
    }

    private Point getPoint(v2 point) {
        float x = (point.x - center.x) * zoom + (getWidth() >> 1);
        float y = (getHeight() >> 1) - (point.y - center.y) * zoom;
        return new Point((int) x, (int) y);
    }

    private v2 getPoint(Point point) {
        float x = (point.x - (getWidth() >> 1)) / zoom + center.x;
        float y = ((getHeight() >> 1) - point.y) / zoom + center.y;
        return new v2(x, y);
    }

    private MyRunnable t;
    private Thread tt;


    private MyRunnable createThread() {
        return new MyRunnable();
    }

    private void setCase(Consumer<Dynamics2D> testcase) {
        this.testCase = testcase;
        w.invoke(() -> {
            initWorld();
            testCase.accept(w);
            repaint();
        });
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            for (; ; ) {
                long l1 = System.nanoTime();

                if (running) {
                    w.step(1.0f / fps, velocity, iterations);
                }
                if (destroyMj) {
                    if (mj.A().fixtureCount > 0 && mj.B().fixtureCount > 0) {
                        w.removeJoint(mj);
                    }
                    mj = null;
                    destroyMj = false;
                }
                if (mjdef != null && mj == null && mjdef.bodyA!=mjdef.bodyB) {
                    mj = w.addJoint(mjdef);
                    mjdef.bodyA.setAwake(true);
                    mjdef = null;
                }
                

                repaint();

                long l3 = System.nanoTime();
                int fulltime = (int) ((double) (l3 - l1) / 1000000);


                int slowmotion = 1;
                int interval = (int) (1000.0f / fps * slowmotion);
                interval -= fulltime;
                interval = Math.max(interval, 0);
                Util.sleepMS(interval);

            }
        }
    }

    private void drawJoint(Joint joint) {
        g.setColor(Color.GREEN);
        v2 v1 = new v2();
        v2 v2 = new v2();
        switch (joint.type) {
            case DISTANCE -> {
                DistanceJoint dj = (DistanceJoint) joint;
                v1 = joint.A().getWorldPoint(dj.getLocalAnchorA());
                v2 = joint.B().getWorldPoint(dj.getLocalAnchorB());
            }
            case MOUSE -> {
                MouseJoint localMj = (MouseJoint) joint;
                localMj.anchorA(v1);
                localMj.anchorB(v2);
            }
        }
        Point p1 = getPoint(v1);
        Point p2 = getPoint(v2);
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    private void drawParticles() {
        v2[] vec = w.getParticlePositionBuffer();
        if (vec == null) {
            return;
        }
        g.setColor(Color.MAGENTA);
        float radius = w.getParticleRadius();
        int size = w.getParticleCount();
        for (int i = 0; i < size; i++) {
            v2 vx = vec[i];
            Point pp = getPoint(vx);
            float r = radius * zoom;

            if (r < 0.5f) {
                g.drawLine(pp.x, pp.y, pp.x, pp.y); 
            } else {
                
                g.fillOval(pp.x - (int) r, pp.y - (int) r, (int) (r * 2), (int) (r * 2));
            }
        }
    }

    static final int MAX_POLY_EDGES = 32;
    private final int[] x = new int[MAX_POLY_EDGES];
    private final int[] y = new int[MAX_POLY_EDGES];

    private void drawBody(Body2D body) {
        if (body.getType() == BodyType.DYNAMIC) {
            g.setColor(Color.LIGHT_GRAY);
        } else {
            g.setColor(Color.GRAY);
        }
        v2 v = new v2();
        MyList<PolygonFixture> generalPolygons = new MyList<>();
        for (Fixture f = body.fixtures; f != null; f = f.next) {
            PolygonFixture pg = f.polygon;
            if (pg != null) {
                if (!generalPolygons.contains(pg)) {
                    generalPolygons.add(pg);
                }
            } else {
                Shape shape = f.shape();
                switch (shape.m_type) {
                    case POLYGON -> {
                        PolygonShape poly = (PolygonShape) shape;
                        for (int i = 0; i < poly.vertices; ++i) {
                            body.getWorldPointToOut(poly.vertex[i], v);
                            Point p = getPoint(v);
                            x[i] = p.x;
                            y[i] = p.y;
                        }
                        g.fillPolygon(x, y, poly.vertices);
                    }
                    case CIRCLE -> {
                        CircleShape circle = (CircleShape) shape;
                        float r = circle.skinRadius;
                        body.getWorldPointToOut(circle.center, v);
                        Point p = getPoint(v);
                        int wr = (int) (r * zoom);
                        g.fillOval(p.x - wr, p.y - wr, wr * 2, wr * 2);
                    }
                    case EDGE -> {
                        EdgeShape edge = (EdgeShape) shape;
                        v2 v1 = edge.m_vertex1;
                        v2 v2 = edge.m_vertex2;
                        Point p1 = getPoint(v1);
                        Point p2 = getPoint(v2);
                        g.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }
        }

        if (generalPolygons.size() != 0) {
            PolygonFixture[] polygonArray = generalPolygons.toArray(new PolygonFixture[0]);
            for (PolygonFixture poly : polygonArray) {
                int n = poly.size();
                int[] x = new int[n];
                int[] y = new int[n];
                for (int i = 0; i < n; ++i) {
                    body.getWorldPointToOut(poly.get(i), v);
                    Point p = getPoint(v);
                    x[i] = p.x;
                    y[i] = p.y;
                }
                g.fillPolygon(x, y, n);
            }
        }
    }

    static final Color clearColor = new Color(0, 0, 0);

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (w == null) {
            return;
        }

        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        g = (Graphics2D) bi.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);


        


        /*synchronized(this)*/
        {
            g.setColor(clearColor);
            
            g.clearRect(0, 0, getWidth(), getHeight());

            
            drawParticles();

            w.bodies(this::drawBody);

            w.joints(this::drawJoint);

        }

        
        {

            g.setFont(new Font("Courier New", Font.BOLD, 16));
            g.setColor(Color.ORANGE);
            g.drawString("s - start/stop", 20, 20);
            g.drawString("r - reset", 20, 40);
            g.setColor(Color.ORANGE);
            g.drawString("Mouse position:  [" + mousePosition.x + ", " + mousePosition.y + ']', 20, 60);
            g.drawString("Screen position: [" + center.x + ", " + center.y + ']', 20, 80);
            g.drawString("Zoom:      " + zoom, 20, 100);
            g.drawString("Bodies:    " + w.getBodyCount(), 20, 120);

            g.drawString("Contacts:  " + w.getContactCount(), 20, 160);
            g.drawString("Particles: " + w.getParticleCount(), 20, 180);
        }




        graphics.drawImage(bi, 0, 0, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return screenSize;
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * Primarny kod spustajuci framework pre testovacie scenare.
     *
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tests");
            frame.setBackground(Color.BLACK);
            frame.getContentPane().setBackground(Color.BLACK);
            frame.setIgnoreRepaint(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Container pane = frame.getContentPane();

            pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

            int bound = cases.length;
            String[] caseNames = IntStream.range(0, bound).mapToObj(i -> (i + 1) + ". " + cases[i]).toArray(String[]::new);

            JComboBox petList = new JComboBox(caseNames);
            pane.add(petList);

            Dimension dimMax = petList.getMaximumSize();
            petList.setMaximumSize(new Dimension(dimMax.width, 30));

            Box2DTests canvas = new Box2DTests();

            petList.addActionListener(e -> {

                JComboBox cb = (JComboBox) e.getSource();
                int index = cb.getSelectedIndex();
                canvas.setCase(cases[index]);
                pane.requestFocusInWindow();
            });

            canvas.setAlignmentX(Component.CENTER_ALIGNMENT);
            canvas.setIgnoreRepaint(true);
            pane.add(canvas);

            canvas.setCase(cases[0]);

            pane.setFocusable(true);
            pane.requestFocusInWindow();

            canvas.t = canvas.createThread();

            pane.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyChar()) {
                        case 's' -> canvas.running = !canvas.running;
                        case 'r' -> {
                            if (canvas.tt == null) {
//                                try {
                                //canvas.tt.interrupt();
                                //canvas.tt.join();
//                                } catch (InterruptedException ex) {
//                                }
                                canvas.tt = new Thread(canvas.t);
                                canvas.tt.start();

                            }

                            canvas.initWorld();
                            canvas.testCase.accept(canvas.w);
                        }
                    }

                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });

            frame.pack();
            frame.setVisible(true);
        });
    }
}