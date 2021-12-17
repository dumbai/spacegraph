/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import jcog.signal.Tensor;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * An OscillatorBank sums the output of a set of oscillators with assignable frequencies and amplitudes.
 * The frequencies and amplitudes of the set of oscillators can be assigned using arrays.
 *
 * @author ollie
 * @beads.category synth
 */
public class OscillatorBank extends UGen {

    /**
     * The array of frequencies of individual oscillators.
     */
    private final Tensor frequency;

    /**
     * The array of gains of individual oscillators.
     */
    private final Tensor gain;

    /**
     * The array of current positions of individual oscillators.
     */
    private final float[] point;

    /**
     * The array of increment rates of individual oscillators, given their frequencies.
     */
    private final double[] increment;

    /**
     * The buffer used by all oscillators.
     */
    private final Tensor sampled;

    /**
     * The number of oscillators.
     */
    private final int size;

    /**
     * The sample rate and master gain of the OscillatorBank.
     */
    private final float gainMaster;

    /**
     * Instantiates a new OscillatorBank.
     *
     * @param context        the AudioContext.
     * @param sampled         the buffer used as a lookup table by the oscillators.
     * @param size the number of oscillators.
     */
    public OscillatorBank(AudioContext context, Tensor sampled, Tensor frequency, Tensor gain) {
        super(context, 1);

        this.sampled = sampled;

        this.frequency = frequency;
        this.size = frequency.volume();

        this.gain = gain;
        assert(gain.volume() == size);

        gainMaster = 1.0f / size;

        increment = new double[size];
        point = new float[size];
    }


















































    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        float[] chan = bufOut[0];

        float sampleRate = context.getSampleRate();
        for (int i = 0; i < size; i++) {
            increment[i] = frequency.getAt(i) / sampleRate;
        }

        for (int i = 0; i < bufferSize; i++) {
            float x =0;
            for (int j = 0; j < size; j++) {

                float p = (float) (point[j] + increment[j]);
                while (p > 1)
                    p -= 1;
                point[j] = p;

                x += gain.getAt(j) * sampled.getFractInterp(p);
            }
            chan[i] = x * gainMaster;
        }
    }


}