package net.beadsproject.beads.ugens;


import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.DataAuvent;
import net.beadsproject.beads.data.DataBeadReceiver;

/**
 * DelayData waits for a specified duration and then sends a DataBead message to
 * a receiver.
 *
 * @author Benito Crawford
 * @version 0.9.5
 */
public class DelayData extends DelayEvent {

    /**
     * The Bead that responds to this DelayMessage.
     */
    private DataBeadReceiver receiver;

    /**
     * The message to send; is set by default to this DelayMessage
     */
    private DataAuvent dataBead;


    /**
     * Instantiates a new DelayMessage with the specified millisecond delay,
     * receiver, and DataBead message.
     *
     * @param context  The audio context.
     * @param delay    The delay time in milliseconds.
     * @param receiver The DataBead receiver.
     * @param db       The DataBead to send.
     */
    public DelayData(AudioContext context, double delay,
                     DataBeadReceiver receiver, DataAuvent db) {
        super(context, delay);
        this.receiver = receiver;
        this.dataBead = db;
    }

    @Override
    public void trigger() {
        if (receiver != null) {
            receiver.sendData(dataBead);
        }
        stop();
    }

    /**
     * Gets this DelayTrigger's receiver.
     *
     * @return the receiver.
     */
    public DataBeadReceiver getReceiver() {
        return receiver;
    }

    /**
     * Sets this DelayData's receiver.
     *
     * @param receiver the new receiver.
     * @return This DelayData instance.
     */
    public DelayData setReceiver(DataBeadReceiver receiver) {
        this.receiver = receiver;
        return this;
    }

    /**
     * Gets the DataBead that will be sent when the DelayData fires.
     *
     * @return The DataBead to be sent.
     */
    public DataAuvent getData() {
        return dataBead;
    }

    /**
     * Sets the message to send when the DelayData fires.
     *
     * @param dataBead The DataBead to be sent.
     * @return This DelayData instance.
     */
    public DelayData setData(DataAuvent db) {
        this.dataBead = db;
        return this;
    }

}
