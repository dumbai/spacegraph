/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.events;

import net.beadsproject.beads.core.Auvent;

/**
 * Use StartTrigger to cause a specific {@link Auvent} to start (unpause) in response to a specific event.
 */
public class StartTrigger extends Auvent {

    /**
     * The Bead that will be started.
     */
	private final Auvent receiver;

    /**
     * Instantiates a new StartTrigger which will start the given {@link Auvent} when triggered.
     *
     * @param receiver the receiver.
     */
    public StartTrigger(Auvent receiver) {
        this.receiver = receiver;
    }

    /**
     * Any incoming message will cause the specified {@link Auvent} to start.
     *
     * @see #accept(Auvent)
     */
    @Override
    public void on(Auvent message) {
        receiver.start();

    }
}