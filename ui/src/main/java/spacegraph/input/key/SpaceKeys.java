package spacegraph.input.key;

import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import jcog.data.list.MetalConcurrentQueue;
import jcog.data.list.MetalRing;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;
import org.eclipse.collections.api.map.primitive.MutableShortObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import org.jetbrains.annotations.Nullable;
import spacegraph.layer.AbstractLayer;
import spacegraph.video.JoglWindow;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;


public class SpaceKeys extends KeyAdapter implements Consumer<JoglWindow> {

    final AbstractLayer space;

    private final Queue<Consumer<SpaceKeys>> pending = new ConcurrentLinkedQueue<>();
    private final MetalRing<Short> queue = new MetalConcurrentQueue<>(64);


    private final MutableShortObjectMap<FloatProcedure> keyPressed = new ShortObjectHashMap<>();
    private final MutableShortObjectMap<FloatProcedure> keyReleased = new ShortObjectHashMap<>();

    public SpaceKeys(AbstractLayer g) {
        this.space = g;


        AutoCloseable on = g.window.onUpdate(this);
    }

    @Override
    public void accept(JoglWindow j) {
        if (!pending.isEmpty()) {
            synchronized(this) {
                pending.removeIf(x -> {
                    x.accept(this);
                    return true;
                });
            }
        }

        if (!queue.isEmpty()) {
            float dt = j.dtS;
            queue.clear(k -> {
                FloatProcedure f = ((k >= 0) ? keyPressed : keyReleased).get((short) Math.abs(k));
                if (f != null)
                    f.value(dt);
            });
        }
    }

    /** add a handler */
    public void on(int keyCode, @Nullable FloatProcedure ifPressed, @Nullable FloatProcedure ifReleased) {
        pending.add((k)->{
            if (ifPressed != null) {
                k.keyPressed.put((short) keyCode, ifPressed);
            }
            if (ifReleased != null) {
                k.keyReleased.put((short) keyCode, ifReleased);
            }
        });
    }

    

    @Override
    public void keyReleased(KeyEvent e) {
        setKey(e, false);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        setKey(e, true);
    }

    private void setKey(KeyEvent e, boolean pressOrRelease) {
        if (e.isConsumed())
            return;

        if (setKey(e.getKeyCode(), pressOrRelease)) {
            e.setConsumed(true);
        }
    }

    private boolean setKey(short c, boolean state) {
        if ((state ? keyPressed : keyReleased).containsKey(c)) {
            queue.push(state ? c : (short)-c);
            return true;
        }
        return false;
    }
}