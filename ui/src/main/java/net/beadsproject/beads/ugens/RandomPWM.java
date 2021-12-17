/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Auvent;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.DataAuvent;
import net.beadsproject.beads.data.DataBeadReceiver;

/**
 * A simple random-length pulse wave modulator. This UGen generates constant
 * pulses of lengths randomly distributed between a minimum length and a maximum
 * length (specified in samples). Additionally, the distribution of the randomly
 * controlled by setting the pulse length exponent parameter (see
 * {@link #setLengthExponent(float) setLengthExponent}).
 * <p>
 * A RandomPWM instance has three modes:
 * <ul>
 * <li>{@link #ALTERNATING} (default) - pulses alternate between -1 and 1.</li>
 * <li>{@link #PULSING} (default) - pulses alternate between 0 and 1.</li>
 * <li>{@link #NOISE} - pulses are distributed continuously between -1 and 1.</li>
 * <li>{@link #SAW} - for random-length ramps between -1 and 1.</li>
 * <li>{@link #RAMPED_NOISE} - for random-length ramps between random values between -1 and 1.</li>
 * <li>{@link #NOISE_ENVELOPE} - for random-length ramps between random values between 0 and 1.</li>
 * </ul>
 *
 * @author Benito Crawford
 * @version 0.9.6
 * @beads.category synth
 */
public class RandomPWM extends UGen implements DataBeadReceiver {
    private static final Mode ALTERNATING = Mode.ALTERNATING;
    public static final Mode NOISE = Mode.NOISE;
    private static final Mode PULSING = Mode.PULSING;
    private static final Mode SAW = Mode.SAW;
    public static final Mode RAMPED_NOISE = Mode.RAMPED_NOISE;
    private static final Mode NOISE_ENVELOPE = Mode.NOISE_ENVELOPE;

    public enum Mode {
        ALTERNATING, NOISE, PULSING, SAW, RAMPED_NOISE, NOISE_ENVELOPE
    }

    private Mode mode = ALTERNATING;
    private float targetVal;
    private float baseVal;
    private float valDiff;
    private float count;
    private float pulseLen;
    private float minLength = 10;
    private float maxLength = 100;
    private float lengthExponent = 1;
    private float lengthDiff;

    /**
     * Constructor specifying mode, and minumum and maximum pulse lengths.
     *
     * @param context The audio context.
     * @param mode    The pulse mode; see {@link #setMode(Mode) setMode}.
     * @param minl    The minimum pulse length.
     * @param maxl    The maximum pulse length.
     */
    public RandomPWM(AudioContext context, Mode mode, float minl, float maxl) {
        this(context, mode, minl, maxl, 1);
    }

    /**
     * Constructor specifying all parameters
     *
     * @param context The audio context.
     * @param mode    The pulse mode; see {@link #setMode(Mode) setMode}.
     * @param minl    The minimum pulse length.
     * @param maxl    The maximum pulse length.
     * @param lexp    The pulse length exponent.
     */
    public RandomPWM(AudioContext context, Mode mode, float minl, float maxl,
                     float lexp) {
        super(context, 0, 1);
        setParams(mode, minl, maxl, lexp);
    }

    @Override
    public void gen() {
        float[] bo = bufOut[0];

        if (mode == PULSING) {
            for (int i = 0; i < bo.length; i++) {
                if (count <= 0) {
                    calcVals();
                    targetVal = targetVal > 0 ? 0 : 1;
                    valDiff = targetVal - baseVal;
                }
                bo[i] = targetVal;
                count--;
            }
        } else if (mode == ALTERNATING) {
            for (int i = 0; i < bo.length; i++) {
                if (count <= 0) {
                    calcVals();
                    targetVal = targetVal > 0 ? -1 : 1;
                    valDiff = targetVal - baseVal;
                }
                bo[i] = targetVal;
                count--;
            }
        } else if (mode == SAW) {
            for (int i = 0; i < bo.length; i++) {
                if (count <= 0) {
                    calcVals();
                    targetVal = targetVal > 0 ? -1 : 1;
                    valDiff = targetVal - baseVal;
                }
                bo[i] = targetVal - (count / pulseLen) * valDiff;
                count--;
            }
        } else if (mode == RAMPED_NOISE) {
            for (int i = 0; i < bo.length; i++) {
                if (count <= 0) {
                    calcVals();
                    targetVal = (float) (Math.random() * 2 - 1);
                    valDiff = targetVal - baseVal;
                }
                bo[i] = targetVal - (count / pulseLen) * valDiff;
                count--;
            }
        } else if (mode == NOISE_ENVELOPE) {
            for (int i = 0; i < bo.length; i++) {
                if (count <= 0) {
                    calcVals();
                    targetVal = (float) Math.random();
                    valDiff = targetVal - baseVal;
                }
                bo[i] = targetVal - (count / pulseLen) * valDiff;
                count--;
            }
        } else {
            
            for (int i = 0; i < bo.length; i++) {
                if (count <= 0) {
                    calcVals();
                    targetVal = (float) (Math.random() * 2 - 1);
                    valDiff = targetVal - baseVal;
                }
                bo[i] = targetVal;
                count--;
            }
        }

    }

    private void calcVals() {
        float d = (float) Math.pow(Math.random(), lengthExponent) * lengthDiff
                + minLength;
        count += d;
        pulseLen = count;
        baseVal = targetVal;
    }

    /**
     * Sets the pulse mode (see {@link #setMode(Mode) setMode}), minimum pulse
     * length, maximum pulse length, and pulse length exponent.
     *
     * @param mode The pulse mode.
     * @param minl The minimum pulse length.
     * @param maxl The maximum pulse length.
     * @param lexp The pulse length exponent.
     */
    private RandomPWM setParams(Mode mode, float minl, float maxl, float lexp) {
        setParams(minl, maxl, lexp);
        setMode(mode);
        return this;
    }

    /**
     * Sets the minimum pulse length, maximum pulse length, and pulse length
     * exponent.
     *
     * @param minl The minimum pulse length.
     * @param maxl The maximum pulse length.
     * @param lexp The pulse length exponent.
     */
    private RandomPWM setParams(float minl, float maxl, float lexp) {
        setLengthExponent(lexp);
        minLength = Math.max(minl, 1);
        maxLength = Math.max(minLength, maxl);
        lengthDiff = maxLength - minLength;
        return this;
    }

    /**
     * Sets the minimum pulse length.
     *
     * @param minl The minimum pulse length.
     */
    public RandomPWM setMinLength(float minl) {
        setParams(minl, maxLength, lengthExponent);
        return this;
    }

    /**
     * Gets the minimum pulse length.
     *
     * @return The minimum pulse length.
     */
    public float getMinLength() {
        return minLength;
    }

    /**
     * Sets the maximum pulse length.
     *
     * @param maxl The maximum pulse length.
     */
    public RandomPWM setMaxLength(float maxl) {
        setParams(minLength, maxl, lengthExponent);
        return this;
    }

    /**
     * Gets the maximum pulse length.
     *
     * @return The maximum pulse length.
     */
    public float getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the pulse length exponent. This parameter controls the distribution
     * of pulse lengths: a value of 1 produces a linear distribution; greater
     * than 1 skews the distribution toward the minimum length; less than one
     * skews it toward the maximum length.
     *
     * @param lexp The pulse length exponent.
     */
    private RandomPWM setLengthExponent(float lexp) {
        if ((lengthExponent = lexp) < 0.001f) {
            lengthExponent = 0.001f;
        }
        return this;
    }

    /**
     * Gets the pulse length exponent.
     *
     * @return The pulse length exponent.
     * @see #setLengthExponent(float)
     */
    public float getLengthExponent() {
        return lengthExponent;
    }

    /**
     * Sets the pulse mode.
     * <p>
     * <ul>
     * <li>Use {@link #ALTERNATING} for pulses that alternate between -1 and 1.</li>
     * <li>Use {@link #PULSING} for pulses that alternate between 0 and 1.</li>
     * <li>Use {@link #NOISE} for pulses distributed randomly between -1 and 1.</li>
     * <li>Use {@link #SAW} for random-length ramps between -1 and 1.</li>
     * <li>Use {@link #RAMPED_NOISE} for random-length ramps between random
     * values.</li>
     * </ul>
     *
     * @param mode The pulse mode.
     */
    private RandomPWM setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Gets the pulse mode.
     *
     * @return The pulse mode.
     * @see #setMode(Mode)
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Use the properties "mode", "minLength", "maxLength", and "lengthExponent"
     * to set the corresponding parameters (type Mode for "mode", floats only for the others).
     */
    @Override
    public DataBeadReceiver sendData(DataAuvent db) {
        if (db != null) {
            Object m = db.get("mode");
            Mode mod = mode;
            if (m instanceof Mode) {
                mod = (Mode) m;
            }
            setParams(mod, db.getFloat("minLength",
                    minLength), db.getFloat("maxLength", maxLength), db
                    .getFloat("lengthExponent", lengthExponent));
        }
        return this;
    }

    @Override
    public void on(Auvent message) {
        if (message instanceof DataAuvent) {
            sendData((DataAuvent) message);
        }
    }

    /**
     * Gets a DataBead filled with properties corresponding to this object's
     * parameters.
     *
     * @return The parameter DataBead.
     */
    public DataAuvent getParams() {
        DataAuvent db = new DataAuvent();
        db.put("mode", mode);
        db.put("minLength", minLength);
        db.put("maxLength", maxLength);
        db.put("lengthExponent", lengthExponent);
        return db;
    }
}