/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

import java.util.Arrays;

/**
 * TapIn stores and serves sound data. Can be used with TapOut to implement
 * delays, etc.
 *
 * @author ben
 * @author Benito Crawford
 * @version 0.9
 * @beads.category effect
 */
public class TapIn extends UGen {
    private final float[] mem;
    private final int memLength;
    private int maxDelay;
    private int counter;
    private final float sampsPerMS;

    /**
     * @param ac           AudioContext
     * @param maxDelayInMS The size of the tapin memory buffer in milliseconds.
     */
    public TapIn(AudioContext ac, float maxDelayInMS) {
        super(ac, 1, 0);
        sampsPerMS = (float) ac.msToSamples(1);
        maxDelay = (int) ac.msToSamples(maxDelayInMS) + 1;
        if (maxDelay < bufferSize) {
            maxDelay = bufferSize;
        }
        memLength = maxDelay + 1;
        mem = new float[memLength];
        Arrays.fill(mem, 0.0f);
        counter = 0;
    }

    public float getMaxDelayMS() {
        return (float) context.samplesToMs(maxDelay);
    }

    @Override
    public void gen() {
        float[] bi = bufIn[0];
        for (int i = 0; i < bufferSize; i++) {
            mem[counter] = bi[i];
            counter = (counter + 1) % memLength;
        }
    }

    public void fillBufferLinear(float[] buf, UGen env) {
        int base = (counter - bufferSize + memLength) % memLength;
        for (int i = 0; i < buf.length; i++) {
            float numSamplesBack;
            if ((numSamplesBack = env.getValue(0, i) * sampsPerMS) < 0) {
                numSamplesBack = 0;
            } else if (numSamplesBack > maxDelay) {
                numSamplesBack = maxDelay;
            }

            float frac = numSamplesBack % 1;
            int d1 = ((base + i - ((int) numSamplesBack) - 1) + memLength)
                    % memLength;
            int d2 = (d1 + 1) % memLength;

            buf[i] = mem[d1] * frac + mem[d2] * (1 - frac);

        }
    }

    public void fillBufferLinear(float[] buf, float numSamplesBack) {
        if (numSamplesBack < 0) {
            numSamplesBack = 0;
        } else if (numSamplesBack > maxDelay) {
            numSamplesBack = maxDelay;
        }
        float frac = numSamplesBack % 1;

        int base = (counter - bufferSize - ((int) numSamplesBack) - 1
                + memLength + memLength);

        for (int i = 0; i < buf.length; i++) {
            int d1 = (base + i) % memLength;
            buf[i] = mem[d1] * frac + mem[(d1 + 1) % memLength] * (1 - frac);
        }

    }

    public void fillBufferNoInterp(float[] buf, UGen env) {
        int base = (counter - bufferSize + memLength + memLength);
        for (int i = 0; i < buf.length; i++) {
            int numSamplesBack;
            if ((numSamplesBack = (int) (env.getValue(0, i) * sampsPerMS + 0.5)) < 0) {
                numSamplesBack = 0;
            } else if (numSamplesBack > maxDelay) {
                numSamplesBack = maxDelay;
            }

            buf[i] = mem[(base + i - numSamplesBack) % memLength];
        }
    }

    public void fillBufferNoInterp(float[] buf, int numSamplesBack) {
        if (numSamplesBack < 0) {
            numSamplesBack = 0;
        } else if (numSamplesBack > maxDelay) {
            numSamplesBack = maxDelay;
        }
        int base = (counter - bufferSize - numSamplesBack + memLength + memLength);
        for (int i = 0; i < buf.length; i++) {
            buf[i] = mem[(base + i) % memLength];
        }
    }

    public float fillBufferAllpass(float[] buf, UGen env, float lastY) {
        int base = counter - bufferSize + memLength + memLength;
        for (int i = 0; i < buf.length; i++) {
            float numSamplesBack;
            if ((numSamplesBack = env.getValue(0, i) * sampsPerMS) < 0) {
                numSamplesBack = 0;
            } else if (numSamplesBack > maxDelay) {
                numSamplesBack = maxDelay;
            }

            float frac = numSamplesBack % 1;
            float g = (1 - frac) / (1 + frac);
            int d1 = ((base + i - ((int) numSamplesBack) - 1) + memLength)
                    % memLength;

            buf[i] = lastY = mem[d1] + g * (mem[(d1 + 1) % memLength] - lastY);

        }
        return lastY;
    }

    /**
     * @param buf
     * @param sampDel
     * @param g
     * @param lastY
     * @return The last output value.
     */

    public float fillBufferAllpass(float[] buf, int sampDel, float g,
                                   float lastY) {
        if (sampDel < 0) {
            sampDel = 0;
        } else if (sampDel > maxDelay) {
            sampDel = maxDelay;
        }
        int base = counter - bufferSize - sampDel - 1 + memLength + memLength;

        for (int i = 0; i < buf.length; i++) {
            int d1 = (base + i) % memLength;
            buf[i] = lastY = mem[d1] + g * (mem[(d1 + 1) % memLength] - lastY);
        }
        return lastY;
    }
}