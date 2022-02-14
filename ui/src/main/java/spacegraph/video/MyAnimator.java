package spacegraph.video;

import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.AnimatorBase;
import jcog.exe.InstrumentedLoop;
import jcog.exe.realtime.ThreadTimer;

/* from: Jake2's */
abstract public class MyAnimator extends AnimatorBase {

    final InstrumentedLoop loop;

    public MyAnimator(float FPS_init) {
        super();

        setIgnoreExceptions(false);
        setPrintExceptions(true);

        this.loop = new DisplayLoop();
        fps(FPS_init);

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

    public final void fps(float fps) {
        loop.fps(fps);
    }

    /**
     * returns whether window is visible
     */
    abstract protected void run();

    private final class DisplayLoop extends InstrumentedLoop {

        /**
         * initially true to force initial invisibility change
         */
        private boolean wasVisible = true;

        DisplayLoop() {
            super(new ThreadTimer());
        }

//        @Override
//        public String toString() {
//            return JoglWindow.this + ".render";
//        }

        @Override
        public boolean next() {

            try {

                MyAnimator.this.run();

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