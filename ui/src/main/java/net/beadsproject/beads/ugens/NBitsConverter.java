/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Converts a signal to have n-bits, useful for 8-bit synthesis.
 * PRE: Signal must be in (-1,1) range
 * POST: Signal is in (-1,1) range
 *
 * @author ben
 * @beads.category effect
 */
public class NBitsConverter extends UGen {
    private final int toRange;
    private final float invToRange;

    /**
     * Creates a new NBitsConverter with the specified {@link AudioContext} and number of bits to convert to.
     *
     * @param ac the AudioContext.
     * @param n  the number of bits to convert to.
     */
    public NBitsConverter(AudioContext ac, int n) {
        super(ac, 1, 1);
        toRange = 1 << (n - 1);
        invToRange = (float) (1.0 / toRange);

    }

    @Override
    public void gen() {
        
        
        
        

        for (int i = 0; i < bufferSize; i++) {
            bufOut[0][i] = invToRange * (int) (bufIn[0][i] * toRange);
        }
    }
}