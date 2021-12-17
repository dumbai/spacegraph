package spacegraph.layer;

import com.jogamp.opengl.GL2;
import jcog.signal.meter.SafeAutoCloseable;

/** a top-level render stage */
public interface Layer extends SafeAutoCloseable {
    void init(GL2 gl);

    void render(long startNS, float dtS, GL2 gl);

    void visible(boolean b);

    /** return whether the layer has changed and needs re-rendered */
    boolean changed();
}