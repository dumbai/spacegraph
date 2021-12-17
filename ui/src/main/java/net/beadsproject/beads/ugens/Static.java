/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Static represents a {@link UGen} with a fixed value. Since the value is fixed, Static doesn't actually calculate anything, and overrides the methods {@link #getValue()} and {@link #getValue(int, int)} to return its fixed value.
 *
 * @author ollie
 * @beads.category utilities
 */
public class Static extends UGen {

    /**
     * The stored value.
     */
    private float x;

    /**
     * Instantiates a new Static with the given value.
     *
     * @param context the AudioContext.
     * @param x       the value.
     */
    public Static(AudioContext context, float x) {
        super(context, 1);
        this.x = x;
        outputInitializationRegime = OutputInitializationRegime.NULL;
        outputPauseRegime = OutputPauseRegime.NULL;
        pause(true); 
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#setValue(float)
     */
    @Override
    public void setValue(float value) {
        x = value;
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#getValue(int, int)
     */
    @Override
    public float getValue(int a, int b) {
        return x;    
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#getValue()
     */
    @Override
    public float getValue() {
        return x;
    }


}
