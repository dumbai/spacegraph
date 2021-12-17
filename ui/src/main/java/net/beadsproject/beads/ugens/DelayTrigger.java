package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Auvent;

/**
 * A DelayTrigger waits for a specified duration and then notifies a receiver.
 *
 * @author ollie
 * @author Benito Crawford
 * @version 0.9.5
 * @beads.category utilities
 */
public class DelayTrigger extends DelayEvent {

    /**
     * The Bead that responds to this DelayMessage.
     */
    private Auvent receiver;

    /**
     * The message to send; is set by default to this DelayMessage.
     */
    private Auvent message;

    /**
     * Instantiates a new DelayTrigger with the specified millisecond delay and
     * receiver. By default, a DelayTrigger object will send itself as the
     * message.
     *
     * @param context  the AudioContext.
     * @param delay    the delay in milliseconds.
     * @param receiver the receiver.
     */
    public DelayTrigger(AudioContext context, double delay, Auvent receiver) {
        super(context, delay);
        this.receiver = receiver;
        this.message = this;
    }

    /**
     * Instantiates a new DelayTrigger with the specified millisecond delay,
     * receiver, and message.
     *
     * @param context  The audio context.
     * @param delay    The delay in milliseconds.
     * @param receiver The receiver.
     */
    public DelayTrigger(AudioContext context, double delay, Auvent receiver,
                        Auvent message) {
        super(context, delay);
        this.receiver = receiver;
        this.message = message;
    }

    @Override
    public void trigger() {
        if (receiver != null) {
            receiver.accept(message);
        }
        stop();
    }

    /**
     * Gets this DelayMessage's receiver.
     *
     * @return the receiver.
     */
    public Auvent getReceiver() {
        return receiver;
    }

    /**
     * Sets this DelayMessage's receiver.
     *
     * @param receiver the new receiver.
     * @return This DelayMessage instance.
     */
    public DelayTrigger setReceiver(Auvent receiver) {
        this.receiver = receiver;
        return this;
    }

    /**
     * Gets the message Bead that will be sent when the DelayMessage fires.
     *
     * @return The message Bead.
     */
    public Auvent getMessage() {
        return message;
    }

    /**
     * Sets the message to send when the DelayMessage fires.
     *
     * @param message The message Bead.
     * @return This DelayMessage instance.
     */
    public DelayTrigger setMessage(Auvent message) {
        this.message = message;
        return this;
    }

}
