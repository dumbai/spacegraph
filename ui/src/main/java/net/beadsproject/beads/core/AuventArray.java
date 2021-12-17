/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.core;

import jcog.data.list.Lst;

import java.util.List;

/**
 * BeadArray represents an array of Beads (and is itself a subclass of Bead). Its purpose is to forward messages to its array members. A BeadArray detects whether or not its members are deleted, and removes them if they are. For this reason it should be used in any situations where a Bead needs to be automatically disposed of. Note, however, that a BeadArray does not forward {@link Auvent#stop()}, {@link Auvent#start()} and {@link Auvent#pause(boolean)} messages to its component Beads unless told to do so by setting {@link #setForwardKillCommand(boolean)} and {@link AuventArray#setForwardPauseCommand(boolean)} respectively.
 *
 * @author ollie
 */
public class AuventArray extends Auvent {

    /**
     * The beads.
     */
    private final List<Auvent> beads;

    /**
     * Flag to forward kill commands.
     */
    private boolean forwardKillCommand;

    /**
     * Flag to forward pause commands.
     */
    private boolean forwardPauseCommand;

    /**
     * Creates an empty BeadArray.
     */
    public AuventArray() {
        beads = new Lst<>();
        forwardKillCommand = false;
        forwardPauseCommand = false;
    }
    public AuventArray(int initialSize) {
        beads = new Lst<>();
        forwardKillCommand = false;
        forwardPauseCommand = false;
    }
    /**
     * Adds a new Bead to the list of receivers.
     *
     * @param bead Bead to addAt.
     */
    public void add(Auvent bead) {
        beads.add(bead);
    }

    /**
     * Removes a Bead from the list of receivers.
     *
     * @param bead Bead to remove.
     */
    public void remove(Auvent bead) {
        beads.remove(bead);
    }

    /**
     * Gets the ith Bead from the list of receivers.
     *
     * @param i index of Bead to retrieve.
     * @return the Bead at the ith index.
     */
    public Auvent get(int i) {
        return beads.get(i);
    }

    /**
     * Clears the list of receivers.
     */
    public void clear() {
        beads.clear();
    }

    /**
     * Gets the size of the list of receivers.
     *
     * @return size of list.
     */
    public int size() {
        return beads.size();
    }

    /**
     * Gets the contents of this BeadArrays as an ArrayList of Beads.
     *
     * @return the beads.
     */
    public List<Auvent> getBeads() {
        return beads;
    }

    /**
     * Forwards incoming message to all receivers.
     *
     * @param message incoming message.
     */
    @Override
    public void on(Auvent message) {
        beads.removeIf(bead -> {
            if (bead.isDeleted()) {
                return true;
            } else {
                bead.accept(message);
                return false;
            }
        });
    }

    /**
     * Creates a shallow copy of itself.
     *
     * @return shallow copy of this Bead.
     */
    @Override
    public AuventArray clone() {
        AuventArray clone = new AuventArray();
        for (Auvent bead : beads) {
            clone.add(bead);
        }
        return clone;
    }

    /**
     * Checks if this BeadArray forwards kill commands.
     *
     * @return true if this BeadArray forwards kill commands.
     */
    public boolean doesForwardKillCommand() {
        return forwardKillCommand;
    }

    /**
     * Determines whether or not this BeadArray forwards kill commands.
     *
     * @param forwardKillCommand true if this BeadArray forwards kill commands.
     */
    public void setForwardKillCommand(boolean forwardKillCommand) {
        this.forwardKillCommand = forwardKillCommand;
    }

    /**
     * Checks if this BeadArray forwards pause commands.
     *
     * @return true if this BeadArray forwards pause commands.
     */
    public boolean doesForwardPauseCommand() {
        return forwardPauseCommand;
    }

    /**
     * Determines whether or not this BeadArray forwards pause commands.
     *
     * @param forwardPauseCommand true if this BeadArray forwards pause commands.
     */
    public void setForwardPauseCommand(boolean forwardPauseCommand) {
        this.forwardPauseCommand = forwardPauseCommand;
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.core.Bead#kill()
     */
    @Override
    public void stop() {
        super.stop();
        if (forwardKillCommand) {
            beads.removeIf(bead -> {
                if (bead.isDeleted()) {
                    return true;
                } else {
                    bead.stop();
                    return false;
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.core.Bead#pause(boolean)
     */
    @Override
    public void pause(boolean paused) {
        super.pause(paused);
        if (forwardPauseCommand) {
            beads.removeIf(bead -> {
                if (bead.isDeleted()) {
                    return true;
                } else {
                    bead.pause(paused);
                    return false;
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.core.Bead#start()
     */
    @Override
    public void start() {
        super.start();
        if (forwardPauseCommand) {
            beads.removeIf(bead -> {
                if (bead.isDeleted()) {
                    return true;
                } else {
                    bead.start();
                    return false;
                }
            });
        }
    }


}
