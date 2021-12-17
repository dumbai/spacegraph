/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import jcog.signal.Tensor;
import net.beadsproject.beads.buffers.SawWave;
import net.beadsproject.beads.buffers.SineWave;
import net.beadsproject.beads.buffers.SquareWave;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * WavePlayer iterates over wave data stored in a {@link Buffer}. The frequency
 * of the WavePlayer is controlled by a {@link UGen}, meaning that WavePlayers
 * can easily be combined to perform FM synthesis or ring modulation.
 * <p>
 * The simplest use of WavePlayer is: <code>
 * WavePlayer wp = new WavePlayer(ac, 440.f, Buffer.SINE);
 * </code>
 *
 * @author ollie
 * @beads.category synth
 * @see Buffer
 * @see SineWave
 * @see SawWave
 * @see SquareWave
 */
public class WavePlayer extends UGen {

    /**
     * The playback point in the Buffer, expressed as a fraction.
     */
    private double phase;

    /**
     * The frequency envelope.
     */
    private UGen frequencyEnvelope;

    /**
     * The phase envelope.
     */
    private UGen phaseEnvelope;

    /**
     * The Buffer.
     */
    private Tensor tensor;

    /**
     * The oscillation frequency.
     */
    private float frequency;

    /**
     * To store the inverse of the sampling frequency.
     */
    private final float one_over_sr;

    private boolean isFreqStatic;

    private WavePlayer(AudioContext context, Tensor tensor) {
        super(context, 1);
        this.tensor = tensor;
        phase = 0;
        one_over_sr = 1.0f / context.getSampleRate();
    }

    /**
     * Instantiates a new WavePlayer with given frequency envelope and Buffer.
     *
     * @param context             the AudioContext.
     * @param frequencyController the frequency envelope.
     * @param tensor              the Buffer.
     */
    public WavePlayer(AudioContext context, UGen frequencyController,
                      Tensor tensor) {
        this(context, tensor);
        setFrequency(frequencyController);
    }

    /**
     * Instantiates a new WavePlayer with given static frequency and Buffer.
     *
     * @param context   the AudioContext.
     * @param frequency the frequency in Hz.
     * @param tensor    the Buffer.
     */
    public WavePlayer(AudioContext context, float frequency, Tensor tensor) {
        this(context, tensor);
        setFrequency(frequency);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.olliebown.beads.core.UGen#start()
     */
    @Override
    public void start() {
        super.start();
        phase = 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        frequencyEnvelope.update();
        float[] bo = bufOut[0];
        if (phaseEnvelope == null) {
            for (int i = 0; i < bufferSize; i++) {
                frequency = frequencyEnvelope.getValue(0, i);
                phase = (((phase + frequency * one_over_sr) % 1.0f) + 1.0f) % 1.0f;
                bo[i] = tensor.getFractInterp((float) phase);
            }
        } else {
            phaseEnvelope.update();
            for (int i = 0; i < bufferSize; i++) {
                bo[i] = tensor.getFractInterp(phaseEnvelope.getValue(0, i));
            }
        }
    }

    /**
     * Gets the frequency envelope.
     *
     * @return the frequency envelope.
     * @deprecated Use {@link #getFrequencyUGen()}.
     */
    @Deprecated
    public UGen getFrequencyEnvelope() {
        return frequencyEnvelope;
    }

    /**
     * Gets the UGen that controls the frequency.
     *
     * @return The frequency controller UGen.
     */
    public UGen getFrequencyUGen() {
        return isFreqStatic ? null : frequencyEnvelope;
    }

    /**
     * Gets the current frequency.
     *
     * @return The current frequency.
     */
    public float getFrequency() {
        return frequency;
    }

    /**
     * Sets the frequency envelope. Note, if the phase envelope is not null, the
     * frequency envelope will have no effect.
     *
     * @param frequencyEnvelope the new frequency envelope.
     * @deprecated Use {@link #setFrequency(UGen)}.
     */
    @Deprecated
    public void setFrequencyEnvelope(UGen frequencyEnvelope) {
        setFrequency(frequencyEnvelope);
    }

    /**
     * Sets a UGen to control the frequency. Note that if the phase envelope is
     * not null, the frequency controller will have no effect.
     *
     * @param frequencyUGen The new frequency controller.
     * @return This WavePlayer instance.
     */
    private WavePlayer setFrequency(UGen frequencyUGen) {
        if (frequencyUGen == null) {
            setFrequency(frequency);
        } else {
            this.frequencyEnvelope = frequencyUGen;


            isFreqStatic = false;
        }
        return this;
    }

    /**
     * Sets the frequency to a static value. Note that if the phase envelope is
     * not null, the frequency will have no effect.
     *
     * @param frequency The new frequency value.
     * @return This WavePlayer instance.
     */
    private WavePlayer setFrequency(float frequency) {
        if (isFreqStatic) {
            frequencyEnvelope.setValue(frequency);
        } else {
            frequencyEnvelope = new Static(context, frequency);
            isFreqStatic = true;
        }
        this.frequency = frequency;
        return this;
    }

    /**
     * Gets the phase envelope.
     *
     * @return the phase envelope.
     * @deprecated Use {@link #getPhaseUGen()}.
     */
    @Deprecated
    public UGen getPhaseEnvelope() {
        return phaseEnvelope;
    }

    /**
     * Gets the phase controller UGen, if there is one.
     *
     * @return The phase controller UGen.
     */
    public UGen getPhaseUGen() {
        return phaseEnvelope;
    }

    /**
     * Gets the current phase;
     *
     * @return The current phase.
     */
    public float getPhase() {
        return (float) phase;
    }

    /**
     * Sets the phase envelope.
     *
     * @param phaseEnvelope the new phase envelope.
     * @deprecated Use {@link #setPhase(UGen)}.
     */
    @Deprecated
    public void setPhaseEnvelope(UGen phaseEnvelope) {
        setPhase(phaseEnvelope);
    }

    /**
     * Sets a UGen to control the phase. This will override any frequency
     * controllers.
     *
     * @param phaseController The new phase controller.
     * @return This WavePlayer instance.
     */
    private WavePlayer setPhase(UGen phaseController) {
        this.phaseEnvelope = phaseController;
        if (phaseController != null) {
            phase = phaseController.getValue();
        }
        return this;
    }

    /**
     * Sets the phase. This will clear the phase controller UGen, if there is
     * one.
     *
     * @param phase The new phase.
     * @return This WavePlayer instance.
     */
    public WavePlayer setPhase(float phase) {
        this.phase = phase;
        this.phaseEnvelope = null;
        return this;
    }

    /**
     * Sets the Buffer.
     *
     * @param b The new Buffer.
     */
    public WavePlayer setBuffer(Tensor b) {
        this.tensor = b;
        return this;
    }

    /**
     * Gets the Buffer.
     *
     * @return The Buffer.
     */
    public Tensor getBuffer() {
        return this.tensor;
    }

}