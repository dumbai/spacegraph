/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.buffers;

import jcog.signal.tensor.ArrayTensor;
import net.beadsproject.beads.data.WaveFactory;

/**
 * A filter used for smoothing data.
 *
 * @author ben
 */
public class RampWave extends WaveFactory {

    @Override
    public ArrayTensor get(int bufferSize) {
        int size = bufferSize;
        ArrayTensor b = new ArrayTensor(size);
        for (int i = 0; i < bufferSize; i++) {
            b.data[i] = ramp((i + 0.5f) / bufferSize) / bufferSize;
        }
        return b;
    }

    private static float ramp(float x) {
        return 2 * x;
    }

    @Override
    public String getName() {
        return "Ramp";
    }

}