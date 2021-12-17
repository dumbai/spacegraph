/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.events;

import net.beadsproject.beads.core.Auvent;

/**
 * Use PauseTrigger to cause a specific {@link Auvent} to pause in response to a specific event.
 */
public class PauseTrigger extends Auvent {

    /**
     * The Bead that will be paused.
     */
    private final Auvent receiver;

    /**
     * Instantiates a new PauseTrigger which will pause the given {@link Auvent} when triggered.
     *
     * @param receiver the receiver.
     */
    public PauseTrigger(Auvent receiver) {
        this.receiver = receiver;
    }

    /**
     * Any incoming message will cause the specified {@link Auvent} to get paused.
     *
     * @see #accept(Auvent)
     */
    @Override
    public void on(Auvent message) {
        if (receiver != null) receiver.pause(true);
    }
}
