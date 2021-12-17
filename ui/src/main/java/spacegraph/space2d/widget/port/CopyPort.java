package spacegraph.space2d.widget.port;

/** port which functions as a repeater / nop.  everything received is broadcast to attached
 *  targets (except the sender)
 * */
public class CopyPort<X> extends Port<X> {
    public CopyPort() {
        super();
        on((from,what)-> out((Port)from.other(CopyPort.this),what));
    }
}
