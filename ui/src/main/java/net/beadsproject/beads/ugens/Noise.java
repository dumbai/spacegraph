/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import jcog.signal.tensor.ArrayTensor;
import net.beadsproject.beads.buffers.NoiseWave;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.WaveFactory;

/**
 * Noise generates white noise. It uses a long buffer of sampled noise.
 *
 * @author ollie
 * @beads.category synth
 */
public class Noise extends UGen {

    private final ArrayTensor noiseTensor;
    private int index;

    /**
     * Instantiates a new Noise.
     *
     * @param context the AudioContext.
     */
    public Noise(AudioContext context) {
        super(context, 1);
        if (!WaveFactory.staticBufs.containsKey("noise")) {
            noiseTensor = new NoiseWave().get(200000);
            WaveFactory.staticBufs.put("noise", noiseTensor);
        } else {
            noiseTensor = WaveFactory.staticBufs.get("noise");
        }
        index = (int) (Math.random() * noiseTensor.data.length);
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        for (int i = 0; i < bufferSize; i++) {
            bufOut[0][i] = noiseTensor.getAt(index);
            index++;
            if (index == noiseTensor.data.length) {
                index = 0;
            }
        }
    }

}