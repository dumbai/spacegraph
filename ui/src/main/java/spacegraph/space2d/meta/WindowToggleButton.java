package spacegraph.space2d.meta;

import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import jcog.exe.Exe;
import org.jetbrains.annotations.Nullable;
import spacegraph.SpaceGraph;
import spacegraph.input.finger.Finger;
import spacegraph.layer.AbstractLayer;
import spacegraph.space2d.widget.button.CheckBox;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * toggle button, which when actived, creates a window, and when inactivated destroys it
 * TODO window width, height parameters
 */
public class WindowToggleButton extends CheckBox implements WindowListener {

    private final Supplier spacer;

    private int width = 600;
    private int height = 300;

    private volatile AbstractLayer space;

    public WindowToggleButton(String text, Object o) {
        this(text, () -> o);
    }

    public WindowToggleButton(String text, Supplier spacer) {
        super(text);
        this.spacer = spacer;
    }

    public WindowToggleButton(String text, Supplier spacer, int w, int h) {
        this(text, spacer);
        this.width = w;
        this.height = h;
    }


    private final AtomicBoolean busy = new AtomicBoolean(false);

    @Override
    protected void onClick() {
        onClick(null);
    }

    @Override
    protected void onClick(@Nullable Finger f) {
        if (!enabled() || !busy.compareAndSet(false, true))
            return;

        set(space == null);

        synchronized(this) {
            if (this.space == null) {

                this.space = SpaceGraph.window(spacer.get(), width, height);

//                space.pre(s -> {
                Exe.runLater(()->{
                    GLWindow w = space.window.window;
                    
                        w.addWindowListener(this);
                        if (f!=null) {
                            int nx = Math.round(f.posPixel.x - width / 2.0f);
                            int ny = Math.round(f.posPixel.y - height / 2.0f);
                            space.window.setPosition(nx, ny);
                        }
                    
                        busy.set(false); 
                    
                });

                
                

            } else if (space != null) {

                busy.set(false);

                set(false);

                this.space.window.off();
                this.space = null;

            }

        }
    }

    @Override
    public void windowResized(WindowEvent e) {

    }

    @Override
    public void windowMoved(WindowEvent e) {

    }

    @Override
    public void windowDestroyNotify(WindowEvent e) {

    }

    @Override
    public void windowDestroyed(WindowEvent e) {
        this.space = null;
        set(false);
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {

    }

    @Override
    public void windowLostFocus(WindowEvent e) {

    }

    @Override
    public void windowRepaint(WindowUpdateEvent e) {

    }


}