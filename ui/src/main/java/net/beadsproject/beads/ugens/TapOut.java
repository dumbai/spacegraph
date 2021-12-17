/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

/**
 * TapOut accesses a TapIn object to implement delays. It has three modes:
 * {@link #NO_INTERP} (no interpolation), {@link #LINEAR} (linear
 * interpolation), and {@link #ALLPASS} (all-pass interpolation). Delay time is
 * specified in milliseconds and can be set by either a static float value or a
 * UGen.
 *
 * @author ben
 * @author Benito Crawford
 * @version 0.9
 * @beads.category effect
 */
public class TapOut extends UGen {
    private final TapIn ti;
    private UGen delayUGen;
    private float delay;
    private final float sampsPerMS;
    private InterpolationType mode;
    private int sampDelayInt;
    private int sampDelayAPInt;
    private float lastY;
    private float sampDelayFloat;
    private float g;

    /**
     * The delayed signal will not be interpolated from the memory buffer.
     */
    private static final InterpolationType NO_INTERP = InterpolationType.NO_INTERP;

    /**
     * The delayed signal will be derived using linear interpolation.
     */
    public static final InterpolationType LINEAR = InterpolationType.LINEAR;

    /**
     * The delayed signal will be derived using all-pass interpolation
     */
    public static final InterpolationType ALLPASS = InterpolationType.ALLPASS;

    public enum InterpolationType {
        NO_INTERP, LINEAR, ALLPASS
    }

    private TapOut(AudioContext ac, TapIn ti) {
        super(ac, 0, 1);
        sampsPerMS = (float) ac.msToSamples(1);
        this.ti = ti;
        this.dependsOn(ti);
        setMode(NO_INTERP);
    }

    /**
     * Constructor for a given TapIn object with a static float delay. The mode
     * is set to the default (no interpolation).
     *
     * @param ac    The audio context.
     * @param ti    The TapIn from which to draw the delayed signal.
     * @param delay The delay time in milliseconds.
     */
    public TapOut(AudioContext ac, TapIn ti, float delay) {
        this(ac, ti);
        setDelay(delay);
    }

    /**
     * Constructor for a given TapIn object with a delay time specified by a
     * UGen. The mode is set to the default (no interpolation).
     *
     * @param ac        The audio context.
     * @param ti        The TapIn from which to draw the delayed signal.
     * @param delayUGen The UGen specifying the delay time in milliseconds.
     */
    public TapOut(AudioContext ac, TapIn ti, UGen delayUGen) {
        this(ac, ti);
        setDelay(delayUGen);
    }

    /**
     * Constructor for a given TapIn object with a static float delay, using the
     * specified delay mode.
     *
     * @param ac    The audio context.
     * @param ti    The TapIn from which to draw the delayed signal.
     * @param mode  The delay mode; see {@link #setMode(InterpolationType)}.
     * @param delay The delay time in milliseconds.
     */
    public TapOut(AudioContext ac, TapIn ti, InterpolationType mode, float delay) {
        this(ac, ti);
        setDelay(delay).setMode(mode);
    }

    /**
     * Constructor for a given TapIn object with a delay time specified by a
     * UGen, using the specified delay mode.
     *
     * @param ac        The audio context.
     * @param ti        The TapIn from which to draw the delayed signal.
     * @param mode      The delay mode; see {@link #setMode(InterpolationType)}.
     * @param delayUGen The UGen specifying the delay time in milliseconds.
     */
    public TapOut(AudioContext ac, TapIn ti, InterpolationType mode, UGen delayUGen) {
        this(ac, ti);
        setDelay(delay).setMode(mode);
    }

    @Override
    public void gen() {

        if (delayUGen == null) {

            switch (mode) {
                case NO_INTERP -> {
                    ti.fillBufferNoInterp(bufOut[0], sampDelayInt);
                    lastY = bufOut[0][bufferSize - 1];
                }
                case LINEAR -> {
                    ti.fillBufferLinear(bufOut[0], sampDelayFloat);
                    lastY = bufOut[0][bufferSize - 1];
                }
                case ALLPASS -> lastY = ti.fillBufferAllpass(bufOut[0], sampDelayAPInt, g,
                        lastY);
            }

        } else {

            delayUGen.update();
            switch (mode) {
                case NO_INTERP -> {
                    ti.fillBufferNoInterp(bufOut[0], delayUGen);
                    lastY = bufOut[0][bufferSize - 1];
                }
                case LINEAR -> {
                    ti.fillBufferLinear(bufOut[0], delayUGen);
                    lastY = bufOut[0][bufferSize - 1];
                }
                case ALLPASS -> lastY = ti.fillBufferAllpass(bufOut[0], delayUGen, lastY);
            }

        }
    }

    /**
     * Gets the current delay time.
     *
     * @return The delay time in milliseconds.
     */
    public float getDelay() {
        return delay;
    }

    /**
     * Sets the delay time to a static value.
     *
     * @param delay The delay time in milliseconds.
     * @return This object instance.
     */
    public TapOut setDelay(float delay) {
        this.delay = delay;
        sampDelayFloat = sampsPerMS * delay;
        sampDelayInt = (int) (sampDelayFloat + 0.5);
        sampDelayAPInt = (int) sampDelayFloat;
        float frac = sampDelayFloat % 1;
        g = (1 - frac) / (1 + frac);
        delayUGen = null;
        return this;
    }

    /**
     * Sets a UGen to specify the delay time in milliseconds.
     *
     * @param delayUGen The delay UGen.
     * @return This object instance.
     */
    private TapOut setDelay(UGen delayUGen) {
        if (delayUGen == null) {
            setDelay(delay);
        } else {
            this.delayUGen = delayUGen;
            delayUGen.update();
            delay = delayUGen.getValue();
        }
        return this;
    }

    /**
     * Gets the delay UGen, if there is one. Returns <code>null</code> if delay
     * time is set to a static float.
     *
     * @return This object instance.
     */
    public UGen getDelayUGen() {
        return delayUGen;
    }

    /**
     * Sets the delay mode. Use the following values:
     * <p>
     * <ul>
     * <li>{@value #NO_INTERP} for no interpolation.</li>
     * <li>{@value #LINEAR} for linear interpolation.</li>
     * <li>{@value #ALLPASS} for all-pass interpolation.</li>
     * </ul>
     *
     * @param mode The delay mode.
     * @return This object instance.
     */
    private TapOut setMode(InterpolationType mode) {
        switch (mode) {
            case NO_INTERP, ALLPASS, LINEAR -> this.mode = mode;
        }
        return this;
    }

    /**
     * Gets the delay mode.
     *
     * @return The delay mode.
     */
    public InterpolationType getMode() {
        return mode;
    }

    public TapIn getTapIn() {
        return ti;
    }
}