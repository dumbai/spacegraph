/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * Calculates and outputs the RMS (root-mean-squares) power factor for a signal
 * over a given time frame. The algorithm accounts for multi-channel input by
 * summing the squares of each channel and dividing by the square root of the
 * number of channels.
 *
 * @author Benito Crawford
 * @version 0.9.5
 * @beads.category lowlevel
 */
public class RMS extends UGen {

    private final float[] rmsMem;
    private float sum;
    private final float channelScale;
    private final float memScale;
    private final int channels;
    private int index;
    private final int memorySize;

    /**
     * Constructor.
     *
     * @param context    The audio context.
     * @param channels   The number of channels.
     * @param memorySize The number of samples over which to compute the RMS.
     */
    public RMS(AudioContext context, int channels, int memorySize) {
        super(context, channels, 1);
        this.channels = channels;
        channelScale = 1.0f / channels;
        rmsMem = new float[memorySize];
        this.memorySize = memorySize;
        memScale = 1.0f / memorySize;
    }

    @Override
    public void gen() {
        float[] bo = bufOut[0];
        for (int i = 0; i < bufferSize; i++) {
            float newMem = 0;
            for (int j = 0; j < channels; j++) {
                float x = bufIn[j][i];
                newMem += x * x;
            }
            sum -= rmsMem[index];
            rmsMem[index] = newMem * channelScale;
            sum += rmsMem[index];
            if (sum < 0)
                sum = 0;
            index = (index + 1) % memorySize;
            bo[i] = (float) Math.sqrt(sum * memScale);
        }
        
    }

    /**
     * Gets the number of channels.
     *
     * @return The number of channels.
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Gets the number of samples over which the RMS is calculated.
     *
     * @return The number of samples.
     */
    public int getMemorySize() {
        return memorySize;
    }

}