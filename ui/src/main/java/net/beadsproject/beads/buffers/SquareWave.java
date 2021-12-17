/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.buffers;

import jcog.signal.tensor.ArrayTensor;
import net.beadsproject.beads.data.WaveFactory;

/**
 * Creates a {@link Buffer} consisting of a square wave in the range [-1,1].
 *
 * @author ollie
 * @see Buffer BufferFactory
 */
public class SquareWave extends WaveFactory {

    /* (non-Javadoc)
     * @see net.beadsproject.beads.data.BufferFactory#generateBuffer(int)
     */
    @Override
    public ArrayTensor get(int bufferSize) {
        int size = bufferSize;
        ArrayTensor b = new ArrayTensor(size);
        int halfBufferSize = bufferSize / 2;
        for (int i = 0; i < halfBufferSize; i++) {
            b.data[i] = 1.0f;
        }
        for (int i = halfBufferSize; i < bufferSize; i++) {
            b.data[i] = -1.0f;
        }
        return b;
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.data.BufferFactory#getName()
     */
    @Override
    public String getName() {
        return "Square";
    }


}