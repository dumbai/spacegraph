/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The Class PolyLimit is a mixer which can be used to keep a limit on the number of UGens connected to it.
 * An upper limit is given. If a new UGen is added but this takes the number of connected UGens over that
 * upper limit then the oldest connected UGen is dropped in order to allow
 * the new UGen to be connected.
 *
 * @beads.category utilities
 */
public class PolyLimit extends UGen {

    /**
     * The max inputs.
     */
    private int maxInputs;

    /**
     * Whether notes are stolen or not, true by default.
     */
    private boolean steal = true;

    /**
     * The existing inputs.
     */
    private final Queue<UGen> existingInputs;

    /**
     * Instantiates a new PolyLimit.
     *
     * @param context   the context.
     * @param inouts    the number of channels.
     * @param maxInputs the max number of connected inputs.
     */
    public PolyLimit(AudioContext context, int inouts, int maxInputs) {
        super(context, inouts, inouts);
        setMaxInputs(maxInputs);
        existingInputs = new LinkedList<>();
    }

    /**
     * Overrides {@link UGen#in(UGen)} such that if a new UGen pushes the total number of
     * connected UGens above the upper limit, the oldest UGen is removed.
     */
    @Override
    public UGen in(UGen sourceUGen) {
        if (existingInputs.contains(sourceUGen)) {
            existingInputs.remove(sourceUGen);
            existingInputs.add(sourceUGen);
        } else {
            if (steal) {
                if (existingInputs.size() >= maxInputs) {
                    UGen deadUGen = existingInputs.poll();
                    removeAllConnections(deadUGen);
                }
                existingInputs.add(sourceUGen);
                super.in(sourceUGen);
            } else {

                Iterable<UGen> copy = new LinkedList<>(existingInputs);
                for (UGen ug : copy) {
                    if (ug.isDeleted()) existingInputs.remove(ug);
                }
                if (existingInputs.size() < maxInputs) {
                    existingInputs.add(sourceUGen);
                    super.in(sourceUGen);
                }
            }
        }
        return this;
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.core.UGen#removeAllConnections(net.beadsproject.beads.core.UGen)
     */
    @Override
    public void removeAllConnections(UGen sourceUGen) {
        super.removeAllConnections(sourceUGen);
        existingInputs.remove(sourceUGen);
    }


    /**
     * Gets the max inputs.
     *
     * @return the max inputs
     */
    public int getMaxInputs() {
        return maxInputs;
    }

    /**
     * Sets the max inputs.
     *
     * @param maxInputs the new max inputs
     */
    private void setMaxInputs(int maxInputs) {
        this.maxInputs = maxInputs;
    }


    /**
     * Determines whether steal mode is true. This means that old sounds are replaced by new ones. If false, new sounds are refused.
     *
     * @return
     */
    public boolean isSteal() {
        return steal;
    }

    /**
     * Sets whether steal mode is true. This means that old sounds are replaced by new ones. If false, new sounds are refused.
     *
     * @param steal
     */
    public void setSteal(boolean steal) {
        this.steal = steal;
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        System.arraycopy(bufIn, 0, bufOut, 0, ins);
    }

}