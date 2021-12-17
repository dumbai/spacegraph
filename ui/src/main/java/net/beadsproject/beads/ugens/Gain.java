/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.DataAuvent;
import net.beadsproject.beads.data.DataBeadReceiver;
import org.jetbrains.annotations.Nullable;

/**
 * Gain modifies the gain of a multi-channel audio signal. The gain value can be
 * controlled by an audio signal.
 *
 * @author ollie
 * @beads.category effect
 */
public class Gain extends UGen implements DataBeadReceiver {

    /**
     * The gain envelope.
     */
    private UGen gainUGen;
    private float gain = 1;

    /**
     * Instantiates a new Gain.
     *
     * @param context      the AudioContext.
     * @param inouts       the number of inputs (= number of outputs).
     * @param gainEnvelope the gain envelope.
     */
    public Gain(AudioContext context, int inouts, UGen gainEnvelope) {
        super(context, inouts, inouts);
        setGain(gainEnvelope);
    }





    /**
     * Instantiates a new Gain with a {@link Static} gain envelop with the given
     * value.
     *
     * @param context the AudioContext.
     * @param inouts  the number of inputs (= number of outputs).
     * @param gain    the fixed gain level.
     */
    public Gain(AudioContext context, int inouts, float gain) {
        super(context, inouts, inouts);
        setGain(gain);
    }

    /**
     * Instantiates a new Gain with {@link Static} gain envelop set to 1.
     *
     * @param context the AudioContext.
     * @param inouts  the number of inputs (= number of outputs).
     */
    public Gain(AudioContext context, int inouts) {
        this(context, inouts, 1.0f);
    }

    /**
     * Gets the gain envelope.
     *
     * @return the gain envelope.
     * @deprecated As of version 1.0, replaced by {@link #getGainUGen()}.
     */
    @Deprecated
    public UGen getGainEnvelope() {
        return gainUGen;
    }

    /**
     * Sets the gain envelope.
     *
     * @param gainEnvelope the new gain envelope.
     * @deprecated As of version 1.0, replaced by {@link #setGain(UGen)}.
     */
    @Deprecated
    public void setGainEnvelope(UGen gainEnvelope) {
        this.gainUGen = gainEnvelope;
    }

    /**
     * Gets the current gain value.
     *
     * @return The gain value.
     */
    public float getGain() {
        return gain;
    }

    /**
     * Sets the gain to a static float value.
     *
     * @param gain The gain value.
     * @return This gain instance.
     */
    public Gain setGain(float gain) {
        this.gainUGen = null;
        this.gain = gain;
        return this;
    }

    /**
     * Sets a UGen to control the gain amount.
     *
     * @param gainUGen The gain UGen.
     * @return This gain instance.
     */
    private Gain setGain(UGen gainUGen) {
        if (gainUGen == null) {
            setGain(gain);
        } else {
            this.gainUGen = gainUGen;
            gainUGen.update();
            gain = gainUGen.getValue();
        }
        return this;
    }

    /**
     * Gets the gain UGen, if it exists.
     *
     * @return The gain UGen.
     */
    public UGen getGainUGen() {
        return gainUGen;
    }

    public @Nullable Envelope envelope() {
        UGen g = this.gainUGen;
		return g instanceof Envelope ? (Envelope) g : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        if (gainUGen == null) {
            for (int channel = 0; channel < ins; channel++) {
                float[] bi = bufIn[channel];
                float[] bo = bufOut[channel];

                float gain = this.gain;
                for (int i = 0; i < bufferSize; ++i) {
                    bo[i] = gain * bi[i];
                }
            }
        } else {
            gainUGen.update();
            for (int i = 0; i < bufferSize; ++i) {
                float gain = this.gain = gainUGen.getValue(0, i);
                for (int channel = 0; channel < ins; channel++) {
                    bufOut[channel][i] = gain * bufIn[channel][i];
                }
            }
        }
    }

    @Override
    public DataBeadReceiver sendData(DataAuvent db) {
        if (db != null) {
            UGen u = db.getUGen("gain");
            if (u == null) {
                setGain(db.getFloat("gain", gain));
            } else {
                setGain(u);
            }
        }
        return this;
    }

}