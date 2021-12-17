/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.events;


import net.beadsproject.beads.core.Auvent;

/**
 * Use KillTrigger to cause a specific {@link Auvent} to be killed ({@link Auvent#stop()}) in response to a specific event.
 */
public class KillTrigger extends Auvent {

    /**
     * The Bead that will be killed.
     */
    private Auvent receiver;

    /**
     * Instantiates a new KillTrigger which will stop the given {@link Auvent} when triggered.
     *
     * @param receiver the receiver.
     */
    public KillTrigger(Auvent receiver) {
        this.receiver = receiver;
    }

    /**
     * Any incoming message will cause the specified {@link Auvent} to be killed.
     *
     * @see #accept(Auvent)
     */
    @Override
    public void on(Auvent message) {
        if (receiver != null) { receiver.stop(); receiver = null; }
    }


}
