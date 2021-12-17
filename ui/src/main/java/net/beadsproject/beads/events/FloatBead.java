/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.events;

/**
 * Interface used to indicate a Bead that stores a single float value.
 */
@FunctionalInterface
public interface FloatBead {

    /**
     * Gets the float value.
     *
     * @return the float value.
     */
    float getFloat();
}
