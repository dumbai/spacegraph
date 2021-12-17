package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 21-Feb-16.
 */
public abstract class AbstractBoundedQueue<T> implements BoundedQueue<T> {
    protected int fillCount;
    protected int capacity;

    public int capacity() {
        return capacity;
    }
    public int fillCount() {
        return fillCount;
    }
}
