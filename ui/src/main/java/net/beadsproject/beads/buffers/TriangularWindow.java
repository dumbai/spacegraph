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
public class TriangularWindow extends WaveFactory {

    @Override
    public ArrayTensor get(int bufferSize) {
        int size = bufferSize;
        ArrayTensor b = new ArrayTensor(size);

        for (int i = 0; i < bufferSize; i++) {
            b.data[i] = tri((i + 0.5f) / bufferSize) / bufferSize;
        }
        return b;
    }

    private static float tri(float x) {
        return x < 0.5 ? 4 * x : 4 * (1 - x);
    }

    @Override
    public String getName() {
        return "TriangularBuffer";
    }

}