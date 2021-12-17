/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Counts and outputs as a signal the number of zero crossings in its input
 * signal over a specified time frame.
 *
 * @author Benito Crawford
 * @version 0.9.5
 * @beads.category lowlevel
 */
public class ZeroCross extends UGen {

    private boolean above;
    private final boolean[] cross;
    private int sum;
    private int index;
    private final int memSize;

    /**
     * Constructor. The specified memory size indicates the time frame over
     * which zero crossings are counted.
     *
     * @param context     The audio context.
     * @param memSizeInMS The time frame in milliseconds.
     */
    public ZeroCross(AudioContext context, float memSizeInMS) {
        super(context, 1, 1);
        memSize = (int) (context.msToSamples(memSizeInMS) + 1);
        cross = new boolean[memSize];
    }

    @Override
    public void gen() {

        float[] bi = bufIn[0];
        float[] bo = bufOut[0];

        for (int i = 0; i < bufferSize; i++) {

            if (cross[index]) {
                sum--;
                cross[index] = false;
            }

            if (bi[i] < 0) {
                if (above) {
                    cross[index] = true;
                    sum++;
                    above = false;
                }
            } else {
                if (!above) {
                    cross[index] = true;
                    sum++;
                    above = true;
                }
            }

            bo[i] = sum;
            index = (index + 1) % memSize;
        }
    }

    /**
     * Gets the memory size.
     *
     * @return The memory size in milliseconds.
     */
    public float getMemorySize() {
        return (float) context.samplesToMs(memSize);
    }

}
