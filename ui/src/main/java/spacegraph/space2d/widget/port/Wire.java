package spacegraph.space2d.widget.port;

import jcog.Util;
import jcog.event.RunThese;
import spacegraph.space2d.Surface;

import java.lang.reflect.Array;

/** undirected edge
 * see: https://github.com/apache/nifi/blob/master/nifi-api/src/main/java/org/apache/nifi/processor/ProcessContext.java
 * */
public class Wire {

    private final int hash;

    private volatile long aLastActive = Long.MIN_VALUE;
    private volatile long bLastActive = Long.MIN_VALUE;
    private volatile int aTypeHash;
    private volatile int bTypeHash;

    public final Surface a;
    public final Surface b;

    public final RunThese offs = new RunThese();

    public Wire(Surface a, Surface b) {
        assert(a!=b);
        if (a.id > b.id) {

            Surface x = b;
            b = a;
            a = x;
        }

        this.a = a;
        this.b = b;
        this.hash = Util.hashCombine(a, b);
    }
    private Wire(Wire copy) {
        this.a = copy.a;
        this.aTypeHash = copy.aTypeHash;
        this.b = copy.b;
        this.bTypeHash = copy.bTypeHash;
        this.hash = copy.hash;
    }

    @Override
    public String toString() {
        return Wire.class.getSimpleName() + '(' + a + ',' + b + ')';
    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;

        Wire w = ((Wire)obj);
        return w.hash == hash && (w.a.equals(a) && w.b.equals(b));
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    /** sends to target */
    public final boolean send(Surface sender, Port receiver, Object x) {

        //System.out.println(sender + " " + x + " " + receiver);

        if (receiver.recv(this, x)) {

            long now = System.nanoTime();

            int th;
            if (x == null) {
                th = 0;
            } else {
                Class<?> cl = x.getClass();
                th = cl.hashCode();
                if (cl.isArray()) {

                    th = Util.hashCombine(th, Array.getLength(x));
                }
            }

            if (sender == a) {
                this.aLastActive = now;
                this.aTypeHash = th;
            } else if (sender == b) {
                this.bLastActive = now;
                this.bTypeHash = th;
            } else
                throw new UnsupportedOperationException();

            return true;
        }
        return false;
    }

    public Surface other(Surface x) {
        if (x == a) {
            return b;
        } else if (x == b) {
            return a;
        } else {
            throw new RuntimeException();
        }
    }

    /** provides a value between 0 and 1 indicating amount of 'recent' activity.
     * this is entirely relative to itself and not other wires.
     * used for display purposes.
     * time is in nanosconds
     */
    public float activity(boolean aOrB, long now, long window) {
        long l = aOrB ? aLastActive : bLastActive;
        return l == Long.MIN_VALUE ? 0 : (float) (1.0 / (1.0 + (Math.abs(now - l)) / ((double) window)));
    }
    /** combined activity level */
    public final float activity(long now, long window) {
        return activity(true, now, window) + activity(false, now, window);
    }

    public int typeHash(boolean aOrB) {
        int x = aOrB ? aTypeHash : bTypeHash;
        if (x == 0 && (aOrB ? aLastActive : bLastActive)==Long.MIN_VALUE)
            return (aOrB ? bTypeHash : aTypeHash ); 
        else
            return x;
    }

    public final void preRemove() {

        if (a instanceof Port) //HACK
            ((Port) a).disconnected((Port) b);

        if (b instanceof Port)
            ((Port) b).disconnected((Port) a);
    }

    public final void remove() {
        offs.close();

    }

    /** override in subclasses to implement behavior to be executed after wire connection has been established in the graph. */
    public void connected() {
        if (a instanceof Port) //HACK
            ((Port) a).connected((Port) b);
        if (b instanceof Port)
            ((Port) b).connected((Port) a);

        //start.root().debug(start, 1, wire);
    }
}