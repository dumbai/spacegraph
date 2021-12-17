/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.buffers;

import jcog.signal.tensor.ArrayTensor;
import net.beadsproject.beads.data.WaveFactory;

/**
 * Creates a {@link Buffer} of random floats.
 *
 * @author ben
 */
public class NoiseWave extends WaveFactory {
    @Override
    public ArrayTensor get(int bufferSize) {
        int size = bufferSize;
        ArrayTensor b = new ArrayTensor(size);
        for (int i = 0; i < bufferSize; i++) {
            b.data[i] = (float) (1.0 - 2.0 * Math.random());
        }
        return b;
    }

    @Override
    public String getName() {
        return "Noise";
    }

}