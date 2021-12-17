/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.events;

/**
 * Interface used to indicate a Bead that stores a single integer value.
 */
@FunctionalInterface
public interface IntegerBead {

    /**
     * Gets the intger value.
     *
     * @return the integer value.
     */
    int getInt();

}
