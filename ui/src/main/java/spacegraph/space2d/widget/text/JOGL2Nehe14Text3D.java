//package spacegraph.space2d.widget.text;
//
//import com.jogamp.opengl.GL2;
//import com.jogamp.opengl.glu.GLU;
//import spacegraph.space2d.Surface;
//import spacegraph.space2d.SurfaceRender;
//import spacegraph.video.Draw;
//
//import java.awt.*;
//import java.text.DecimalFormat;
//
//import static spacegraph.SpaceGraph.window;
//
///**
// * NeHe Lesson #14: 3D Text Rendering using TextRenderer
// */
//public class JOGL2Nehe14Text3D extends Surface {  // Renderer
//
//    private static final String TITLE = "Nehe #14: Outline Fonts";  // window's title
//    private static final int CANVAS_WIDTH = 640;  // width of the drawable
//    private static final int CANVAS_HEIGHT = 480; // height of the drawable
//    private static final int FPS = 60; // animator's target frames per second
//
//    private GLU glu;  // for the GL Utility
//
//    private TextRenderer textRenderer;
//    private String msg = "NeHe - ";
//    private DecimalFormat formatter = new DecimalFormat("###0.00");
//
//    //private float textPosX; // x-position of the 3D text
//    //private float textPosY; // y-position of the 3D text
//    //private float textScaling; // scaling factor for 3D text
//
//    private static float rotateAngle = 0.0f;
//
//    public JOGL2Nehe14Text3D() {
//        super();
//    }
//
//    public static void main(String[] args) {
//        window(new JOGL2Nehe14Text3D(), 800, 800);
////        // Create the OpenGL rendering canvas
////        GLCanvas canvas = new GLCanvas();  // heavy-weight GLCanvas
////        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
////        canvas.addGLEventListener(new JOGL2Nehe14Text3D());
////
////        // Create a animator that drives canvas' display() at the specified FPS.
////        final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);
////
////        // Create the top-level container frame
////        final JFrame frame = new JFrame(); // Swing's JFrame or AWT's Frame
////        frame.getContentPane().addAt(canvas);
////        frame.addWindowListener(new WindowAdapter() {
////            @Override
////            public void windowClosing(WindowEvent e) {
////                // Use a dedicate thread to run the stop() to ensure that the
////                // animator stops before program exits.
////                new Thread() {
////                    @Override
////                    public void run() {
////                        animator.stop(); // stop the animator loop
////                        System.exit(0);
////                    }
////                }.start();
////            }
////        });
////        frame.setTitle(TITLE);
////        frame.pack();
////        frame.setVisible(true);
////        animator.start(); // start the animation loop
//    }
//
//    // ------ Implement methods declared in GLEventListener ------
//
//    GL2 gl = null;
//
//    @Override
//    public void paint(GL2 gl, SurfaceRender r) {
//        if (this.gl == null) {
//            init(this.gl = gl);
//        }
//
////        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers
////        gl.glLoadIdentity();  // reset the model-view matrix
//
//        // ----- Rendering 3D text using TextRenderer class -----
//        textRenderer.begin3DRendering();
//
//
//        gl.glPushMatrix();
////        gl.glTranslatef(50.0f, 50.0f, 0.0f);
////        gl.glRotatef(rotateAngle, 1.0f, 0.0f, 0.0f);
////        gl.glRotatef(rotateAngle * 1.5f, 0.0f, 1.0f, 0.0f);
////        gl.glRotatef(rotateAngle * 1.4f, 0.0f, 0.0f, 1.0f);
////        gl.glScalef(100,100,100f);
//
//        // Pulsing Colors Based On Text Position
//        textRenderer.setColor(1,1,1,0.9f); // B
//
//        // String, x, y, z, and scaling - need to scale down
//        // Not too sure how to compute the x, y and scaling - trial and error!
//        textRenderer.draw3D("HELLO",
//                0.0f,
//                (float) Math.random() , 0.0f, 100 *  (float) Math.random() /* 0.4 */);
////        textRenderer.draw("HELLO",
////                500, 500);
//
//
//        // Clean up rendering
//        textRenderer.end3DRendering();
//
//        // Update the rotate angle
//        rotateAngle += 0.1f;
//
//        Draw.colorGrays(gl, 0.5f);
//        Draw.rect(gl, 0, 0, 100, 100);
//
//        gl.glPopMatrix();
//
//
//    }
//
//    /*
//     * Called back immediately after the OpenGL context is initialized. Can be used
//     * to perform one-time initialization. Run only once.
//     */
//    public void init(GL2 gl) {
//
////        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
////        gl.glClearDepth(1.0f);      // set clear depth value to farthest
////        gl.glEnable(GL_DEPTH_TEST); // enables depth testing
////        gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
////        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
////        gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
////
////        gl.glEnable(GL_LIGHT0); // Enable default light (quick and dirty)
////        gl.glEnable(GL_LIGHTING); // Enable lighting
////        gl.glEnable(GL_COLOR_MATERIAL); // Enable coloring of material
//
//        // Allocate textRenderer with the chosen font
//        textRenderer = new TextRenderer(new Font("Monospace", Font.BOLD, 12));
//
//        // Calculate the position and scaling factor
//        // Rectangle2D bounds = textRenderer.getBounds(msg + "00.00");
//        // int textWidth = (int)bounds.getWidth();
//        // int textHeight = (int)bounds.getHeight();
//        // System.out.println("w = " + textWidth);
//        // System.out.println("h = " + textHeight);
//        // 104 x 14
//    }
//
////    /*
////     * Call-back handler for window re-size event. Also called when the drawable is
////     * first set to visible.
////     */
////    @Override
////    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
////        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
////
////        if (height == 0) height = 1;   // prevent divide by zero
////        float aspect = (float)width / height;
////
////        // Set the view port (display area) to cover the entire window
////        gl.glViewport(0, 0, width, height);
////
////        // Setup perspective projection, with aspect ratio matches viewport
////        gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
////        gl.glLoadIdentity();             // reset projection matrix
////        glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar
////
////        // Enable the model-view transform
////        gl.glMatrixMode(GL_MODELVIEW);
////        gl.glLoadIdentity(); // reset
////    }
//
//}
//
