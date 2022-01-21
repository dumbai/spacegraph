package spacegraph.video;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.AnimatorBase;
import jcog.Log;
import jcog.Util;
import jcog.data.list.FastCoWList;
import jcog.data.map.ConcurrentFastIteratingHashSet;
import jcog.event.ListTopic;
import jcog.event.Off;
import jcog.event.Topic;
import jcog.exe.InstrumentedLoop;
import jcog.exe.realtime.ThreadTimer;
import jcog.signal.meter.SafeAutoCloseable;
import jcog.util.ArrayUtil;
import org.slf4j.Logger;
import spacegraph.UI;
import spacegraph.layer.Layer;
import spacegraph.util.animate.Animated;
import spacegraph.video.font.HersheyFont;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;


public class JoglWindow implements GLEventListener, WindowListener {

    static {
        System.setProperty("java.awt.headless", "true"); //HACK
    }

    public static final Logger logger = Log.log(JoglWindow.class);
    private static final Collection<JoglWindow> windows = new ConcurrentFastIteratingHashSet<>(new JoglWindow[0]);
    private static GLCapabilitiesImmutable config;

    static {
        Threading.invokeOnOpenGLThread(true, JoglWindow::config);
    }

    public final Topic<JoglWindow> eventClosed = new ListTopic<>();
    public final Topic<JoglWindow> onUpdate = new ListTopic<>();
    /**
     * render loop
     */
    private final GameAnimatorControl animator = new GameAnimatorControl();
    //        synchronized (JoglWindow.class) {
//            if (JoglWindow.config == null) {
//                //lazy instantiate
//                JoglWindow.config = config();
//            }
//        }
    private final AtomicBoolean updateWindowPos = new AtomicBoolean(false);
//    public float renderFPSInvisibleRate = 0;
    private final AtomicBoolean updateWindowSize = new AtomicBoolean(false);
    public GLWindow window;
    public GL2 gl;
    /**
     * update time since last cycle (S)
     */
    public float dtS = 0;
    public float renderFPS = UI.FPS_default;
    private long lastRenderNS = System.nanoTime();
    private int nx = -1;
    private int ny = -1;
    private int nw = -1;
    private int nh = -1;

    final AtomicBoolean changed = new AtomicBoolean(true);

    private final FastCoWList<Layer> layers = new FastCoWList<>(Layer.class);

    public JoglWindow() {
//		Threading.invokeOnOpenGLThread(false, () -> {

        //Util.time(logger, "GLWindow.create", () -> {
        this.window = GLWindow.create(config);
        //});
        window.addWindowListener(this);
        window.addGLEventListener(this);
        window.setAutoSwapBufferMode(false);
//		});

        animator.loop.fps(UI.FPS_init); //wait for startup

    }

    public JoglWindow(int pw, int ph) {
        this();
        if (pw > 0 && ph > 0)
            showInit(pw, ph);
    }

    private static void config() {
        Util.time(logger, "GLCapabilities", () -> {

            GLCapabilities c = new GLCapabilities(

                    GLProfile.getMaximum(true)
                    //GLProfile.getGL2GL3()
                    //GLProfile.getDefault()
                    //GLProfile.getMinimum(true)


            );


            c.setStencilBits(1);
            c.setSampleBuffers(true);
            c.setNumSamples(2);
//		c.setAlphaBits(4);

            config = c;
        });


    }

    private void visibleUpdate() {

        dtS = (float) animator.loop.cycleTimeS; //HACK

        onUpdate.emit(JoglWindow.this);

        GLWindow w = window;

        if (updateWindowPos.compareAndSet(true, false))
            w.setPosition(nx, ny);
        if (updateWindowSize.compareAndSet(true, false))
            w.setSurfaceSize(nw, nh);


        JoglWindow.this.isVisible(true);

    }

    public void off() {
        GLWindow w = this.window;
        if (w != null)
            w.destroy();
        //Exe.invokeLater(w::destroy);

        this.window = null;
        this.gl = null;

    }

    //protected abstract void showInit(GL2 gl);

    public void printHardware() {


        System.err.print("GL:");
        System.err.println(gl);
        System.err.print("GL_VERSION=");
        System.err.println(gl.glGetString(GL.GL_VERSION));
        System.err.print("GL_EXTENSIONS: ");
        System.err.println(gl.glGetString(GL.GL_EXTENSIONS));
    }

    /** width in pixels */
    public final int W() {
        return window.getSurfaceWidth();
    }

    /** height in pixels */
    public final int H() {
        return window.getSurfaceHeight();
    }

    @Override
    public void dispose(GLAutoDrawable arg0) {
        animator.loop.stop();
    }

    @Override
    public void windowResized(WindowEvent windowEvent) {
        //if (!preRenderTasks.contains(windowUpdater)) {
        this.nw = W();
        this.nh = H();
        changed();
        //}
    }

    private void changed() {
        changed.set(true);
    }

    @Override
    public void windowMoved(WindowEvent windowEvent) {
        //if (!preRenderTasks.contains(windowUpdater)) {
        this.nx = getX();
        this.ny = getY();
        //}
    }

    @Override
    public void windowDestroyNotify(WindowEvent windowEvent) {
        animator.stop();
        eventClosed.emit(this);

        layers.forEach(SafeAutoCloseable::close);
        layers.clear();
    }

    @Override
    public void windowDestroyed(WindowEvent windowEvent) {
        windows.remove(this);
    }

    @Override
    public void windowGainedFocus(WindowEvent windowEvent) {
        animator.loop.fps(renderFPS);
    }

    @Override
    public void windowLostFocus(WindowEvent windowEvent) {
        animator.loop.fps(UI.renderFPSUnfocusedRate * renderFPS);
    }

    @Override
    public void windowRepaint(WindowUpdateEvent windowUpdateEvent) {
//        if (!window.isVisible())
//            Util.nop();
        //if (!updater.isRunning()) {
//        updater.setFPS(updateFPS /* window.hasFocus() ? updateFPS : updateFPS * updateFPSUnfocusedMultiplier */);
        //}
    }

    private static void clear(GL2 gl) {
        //clearMotionBlur(0.5f, gl);
        clearComplete(gl);
    }

    protected static void clearComplete(GL2 gl) {
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private static void clearMotionBlur(float rate /* TODO */, GL2 gl) {


        gl.glAccum(GL2.GL_LOAD, 0.5f);

        gl.glAccum(GL2.GL_ACCUM, 0.5f);


        gl.glAccum(GL2.GL_RETURN, rate);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);


    }

    final ConcurrentLinkedQueue<Runnable> runLater = new ConcurrentLinkedQueue<>();
    public void runLater(Runnable r) {
        runLater.add(r);
    }

    @Override
    public final /*synchronized*/ void display(GLAutoDrawable drawable) {

        //drain run queue
        runLater.removeIf(t->{
            t.run();
            return true;
        });

        long nowNS = System.nanoTime();
        long renderDtNS = nowNS - lastRenderNS;
        this.lastRenderNS = nowNS;

        boolean changed = this.changed.getAndSet(false);
        if (!changed) {
            for (Layer l : layers) {
                if (l.changed()) {
                    changed = true;
                    break;
                }
            }
        }
        if (!changed)
            return;

        clear(gl); //TODO move out to top level so its once

        float dtS = (float) (renderDtNS / 1.0E9);

        for (Layer l : layers)
            l.render(nowNS, dtS, gl);

        window.swapBuffers();

        //gl.glFlush();  //<- not helpful
        //gl.glFinish(); //<- not helpful
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    public void showInit(int w, int h) {
        showInit("", w, h);
    }

    private void showInit(String title, int w, int h, int x, int y) {
        GLWindow W = this.window;


        this.nw = w; this.nh = h; //HACK
        W.setSurfaceSize(w, h); //force init

        if (x != Integer.MIN_VALUE) {
            this.nx = x; this.ny = y; //HACK
            W.setPosition(x, y);
        }

        W.setTitle(title);

        W.setVisible(false, true);
    }

    public void isVisible(boolean b) {
        layers.forEach(l -> l.visible(b));
//        if (!b) {
//            setSize(0, 0);
//        } else {
//            setSize(Math.max(1, getWidth()), Math.max(1, getHeight()));
//        }
    }

    public void setPosition(int x, int y) {
        if (nx != x || ny != y) {
            nx = x;
            ny = y;
            updateWindowPos.set(true);
        }
    }

    public void setSize(int w, int h) {
        if (nw != w || nh != h) {
            nw = w;
            nh = h;
            updateWindowSize.set(true);
        }
    }

    @Override
    public final void init(GLAutoDrawable drawable) {

        GL2 gl = drawable.getGL().getGL2();
        this.gl = gl;
        windows.add(this);

        if (gl.getGLProfile().isHardwareRasterizer()) {
            gl.setSwapInterval(0);
        } else {
            gl.setSwapInterval(2); //lower framerate
        }

        HersheyFont.load(gl);

        //showInit(gl);

        animator.add(window);

        //ready

        setFPS(renderFPS);
    }

    public void setFPS(float render) {
        animator.loop.fps(renderFPS = render);
//        updateFPS = update;
//        if (updater.isRunning()) {
//            updater.setFPS(updateFPS);
//        }

    }

    private void showInit(String title, int w, int h) {
        showInit(title, w, h, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public void addKeyListener(KeyListener m) {
        if (ArrayUtil.indexOf(window.getKeyListeners(), m) != -1)
            return;
        window.addKeyListener(m);
    }

    public Off onUpdate(Consumer<JoglWindow> c) {
        return onUpdate.on(c);
    }

    public Off onUpdate(Animated c) {
        return onUpdate.on(s -> c.animate(dtS));
    }

    public Off onUpdate(Runnable c) {
        return onUpdate.on(s -> c.run());
    }

    /**
     * x-pixel coordinate of window left edge
     */
    public int getX() {
        return window.getX();
    }

    /**
     * y-pixel coordinate of window top edge.
     * note: this is the reverse direction of the generally-expected cartesian upward-pointing y-axis
     */
    public int getY() {
        return window.getY();
    }

    public float getScreenW() {
        return window.getScreen().getWidth();
    }

    public float getScreenH() {
        return window.getScreen().getHeight();
    }

    /**
     * min dimension
     */
    public float getWidthHeightMin() {
        return Math.min(W(), H());
    }

    /**
     * max dimension
     */
    public float getWidthHeightMax() {
        return Math.min(W(), H());
    }

    public boolean add(Layer l) {
        synchronized(layers) {
            if (layers.containsInstance(l))
                return false;

            l.init(gl);

            layers.add(l);
        }

        return true;
    }

    public boolean remove(Layer l) {
        return layers.remove(l);
    }

    /* from: Jake2's */
    private final class GameAnimatorControl extends AnimatorBase {

        final InstrumentedLoop loop;

        GameAnimatorControl() {
            super();

            setIgnoreExceptions(false);
            setPrintExceptions(true);

            this.loop = new DisplayLoop();
        }

        @Override
        protected String getBaseName(String prefix) {
            return prefix;
        }

        @Override
        public final boolean start() {
            return false;
        }

        @Override
        public final boolean stop() {
            pause();
            return true;
        }


        @Override
        public final boolean pause() {
            loop.stop();
            return true;
        }

        @Override
        public final boolean resume() {
            return true;
        }

        @Override
        public final boolean isStarted() {
            return loop.isRunning();
        }

        @Override
        public final boolean isAnimating() {
            return loop.isRunning();
        }

        @Override
        public final boolean isPaused() {
            return !loop.isRunning();
        }

        private final class DisplayLoop extends InstrumentedLoop {

            /** initially true to force initial invisibility change */
            private boolean wasVisible = true;

            DisplayLoop() {
                super(new ThreadTimer());
            }

            @Override
            public String toString() {
                return JoglWindow.this + ".render";
            }

            @Override
            public boolean next() {

                try {

                    GLWindow w = window;
                    boolean visible = w!=null && w.isVisible();
                    if (visible) {
                        visibleUpdate();

                        GLAutoDrawable d = drawables.isEmpty() ? null : drawables.get(0);
                        if (d != null)
                            d.display();
                    } else {
                        if (wasVisible && !visible)
                            JoglWindow.this.isVisible(false);
                    }
                    wasVisible = visible;

                    ((ThreadTimer) timer).setPeriodMS(periodMS()); //HACK

                    return true;
                } catch (GLException /*| InterruptedException*/ e) {
                    Throwable c = e.getCause();
                    ((c != null) ? c : e).printStackTrace();
                    stop();
                    return false;
                }


            }
        }
    }

}