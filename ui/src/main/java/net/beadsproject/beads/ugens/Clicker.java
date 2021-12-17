/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * A very simple {@link UGen} that generates one click and then kills itself.
 *
 * @author ollie
 * @beads.category synth
 */
public class Clicker extends UGen {

    private boolean done;
    private final float strength;

    /**
     * Instantiates a new Clicker.
     *
     * @param context  the AudioContext.
     * @param strength the volume of the click (max = 1).
     */
    public Clicker(AudioContext context, float strength) {
        super(context, 0, 1);
        this.strength = Math.min(1.0f, Math.abs(strength));
        done = false;
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        if (done) stop();
        else {
            bufOut[0][0] = strength;
            done = true;
        }
    }

}