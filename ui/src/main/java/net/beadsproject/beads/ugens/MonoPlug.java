/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * MonoPlug performs the simple task of channelling a single output from a multi-channel
 * {@link UGen}.
 *
 * @author ollie
 * @beads.category utilities
 */
public class MonoPlug extends UGen {

    /**
     * Instantiates a new MonoPlug.
     *
     * @param context the AudioContext.
     */
    public MonoPlug(AudioContext context) {
        super(context, 1, 1);
        outputInitializationRegime = OutputInitializationRegime.RETAIN;
        outputPauseRegime = OutputPauseRegime.ZERO;
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        bufOut[0] = bufIn[0];
    }

}
