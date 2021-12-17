/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGenChain;
import net.beadsproject.beads.data.DataAuvent;
import net.beadsproject.beads.data.DataBeadReceiver;

/**
 * A basic reverb unit with adjustable room size, high-frequency damping, and
 * early reflections and late reverb levels. If specified, creates a
 * de-correlated multi-channel effect.
 *
 * @author Benito Crawford
 * @version 0.9.5
 * @beads.category effect
 */
public class Reverb extends UGenChain implements DataBeadReceiver {
    private float size;
    private float damping;
    private float earlyLevel;
    private float lateLevel;
    private final Gain earlyGain;
    private final Gain lateGain;
    private final AllpassFilter eAPF1;
    private final AllpassFilter eAPF2;
    private final AllpassFilter eAPF3;
    private final AllpassFilter lAPF1;
    private final AllpassFilter lAPF2;
    private final AllpassFilter lAPF3;
    private final AllpassFilter lAPF4;
    private final AllpassFilter[] apfOuts;
    private final float[] outDelayScale;
    private final OnePoleFilter lpf;
    private final OnePoleFilter src;
    private final RandomPWM delayModulator;
    private float lateDelay1;
    private float lateDelay2;
    private float lateDelay3;
    private float lateDelay4;
    private final float sampsPerMS;
    private final TapOut earlyTapOut;

    /**
     * Constructor for a reverb unit with one output channel.
     *
     * @param context The audio context.
     */
    public Reverb(AudioContext context) {
        this(context, 1);
    }

    /**
     * Constructor for a reverb unit with the specified number of output
     * channels.
     *
     * @param context     The audio context.
     * @param outChannels The number of output channels.
     */
    private Reverb(AudioContext context, int outChannels) {
        super(context, 1, outChannels);

        sampsPerMS = (float) context.msToSamples(1);

        
        src = new OnePoleFilter(context, 4000);


        TapIn earlyTapIn = new TapIn(context, 125);
        earlyTapOut = new TapOut(context, earlyTapIn, 10);
        eAPF1 = new AllpassFilter(context, (int) (12.812 * sampsPerMS), 113,
                0.3f);
        eAPF2 = new AllpassFilter(context, (int) (12.812 * sampsPerMS * 3),
                337, 0.4f);
        eAPF3 = new AllpassFilter(context, (int) (12.812 * sampsPerMS * 9.4),
                1051, 0.5f);
        Gain earlyGainEcho = new Gain(context, 1, -0.3f);
        

        
        
        
        lAPF1 = new AllpassFilter(context, (int) (140.0f * sampsPerMS), 19, 0.72f);
        lAPF2 = new AllpassFilter(context, (int) (140.0f * sampsPerMS), 23, 0.7f);
        lAPF3 = new AllpassFilter(context, (int) (140.0f * sampsPerMS), 29, 0.65f);
        lAPF4 = new AllpassFilter(context, (int) (140.0f * sampsPerMS), 37, 0.6f);
        lpf = new OnePoleFilter(context, 1000);
        TapIn lateTapIn = new TapIn(context, 1000);
        TapOut lateTapOut1 = new TapOut(context, lateTapIn, 10);
        TapOut lateTapOut2 = new TapOut(context, lateTapIn, 31.17f);
        Gain lateGainEcho = new Gain(context, 1, -0.25f);
        

        
        earlyGain = new Gain(context, 1, 1);
        lateGain = new Gain(context, 1, 1);
        Gain collectedGain = new Gain(context, 1, 1);

        
        delayModulator = new RandomPWM(context, RandomPWM.RAMPED_NOISE, 4000,
                15000, 1);

        drawFromChainInput(src);
        earlyTapIn.in(src);
        earlyTapIn.in(earlyGain);
        eAPF1.in(earlyTapOut);
        eAPF2.in(eAPF1);
        eAPF3.in(eAPF2);
        earlyGainEcho.in(eAPF3);
        earlyGain.in(earlyGainEcho);
        lAPF1.in(earlyGainEcho);
        lAPF1.in(lateGainEcho);
        lAPF1.in(src);
        lAPF2.in(lAPF1);
        lAPF3.in(lAPF2);
        lAPF4.in(lAPF3);
        lpf.in(lAPF4);
        lateTapIn.in(lpf);
        lateGainEcho.in(lateTapOut1);
        lateGainEcho.in(lateTapOut2);
        lateGain.in(lateGainEcho);
        collectedGain.in(earlyGain);
        collectedGain.in(lateGain);

        apfOuts = new AllpassFilter[outChannels];
        outDelayScale = new float[outChannels];
        for (int i = 0; i < outChannels; i++) {
            float g = 0.3f + ((float) i / (i + 1)) * 0.1f + (float) Math.sin(i)
                    * 0.05f;
            outDelayScale[i] = (3.0f * i + 5) / (5.0f * i + 5);
            apfOuts[i] = new AllpassFilter(context, (int) (60.0f * sampsPerMS),
                    20, g);
            apfOuts[i].in(collectedGain);
            addToChainOutput(i, apfOuts[i]);
        }

        setSize(0.5f).setDamping(0.7f).setEarlyReflectionsLevel(1)
                .setLateReverbLevel(1);
    }

    @Override
    protected void preFrame() {
        delayModulator.update();
        int m = (int) (delayModulator.getValue() * 0.3f * sampsPerMS);
        lAPF1.setDelay((int) lateDelay1 - m);
        lAPF2.setDelay((int) lateDelay2 + m);
        lAPF3.setDelay((int) lateDelay3 - m);
        lAPF4.setDelay((int) lateDelay4 + m);
    }

    /**
     * Gets the "room size".
     *
     * @return The "room size", between 0 and 1.
     */
    public float getSize() {
        return size;
    }

    /**
     * Sets the "room size". Valid value range from 0 to 1 (.5 is the default).
     * The larger the value, the longer the decay time.
     *
     * @param size The "room size".
     * @return This reverb instance.
     */
    private Reverb setSize(float size) {
        if (size > 1)
            size = 1;
        else if (size < 0.01)
            size = 0.01f;
        this.size = size;
        lateDelay1 = 86.0f * size * sampsPerMS;
        lateDelay2 = lateDelay1 * 1.16f;
        lateDelay3 = lateDelay2 * 1.16f;
        lateDelay4 = lateDelay3 * 1.16f;
        earlyTapOut.setDelay(60.0f * size);

        float d = 12.812f * sampsPerMS * size;
        eAPF1.setDelay((int) d);
        eAPF2.setDelay((int) (d * 3 - 2));
        eAPF3.setDelay((int) (d * 9.3 + 1));

        d = 60.0f * sampsPerMS * size;
        for (int i = 0; i < this.outs; i++) {
            apfOuts[i].setDelay((int) (d * outDelayScale[i]));
        }
        return this;
    }

    /**
     * Gets the damping factor.
     *
     * @return The damping factor, between 0 and 1.
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Sets the damping factor. Valid values range from 0 to 1 (.7 is the
     * default). Higher values filter out higher frequencies faster.
     *
     * @param damping The damping factor.
     * @return This reverb instance.
     */
    private Reverb setDamping(float damping) {
        if (damping < 0)
            damping = 0;
        else if (damping > 1)
            damping = 1;
        this.damping = damping;

        float f = 1.0f - (float) Math.sqrt(damping);

        src.setFrequency(f * 10000 + 250);
        lpf.setFrequency(f * 8000 + 200);

        return this;
    }

    /**
     * Gets the early reflections level.
     *
     * @return The early reflections level.
     */
    public float getEarlyReflectionsLevel() {
        return earlyLevel;
    }

    /**
     * Sets the early reflections level (the amount of early reflections heard
     * in the output). The default value is 1.
     *
     * @param earlyLevel The early reflections level.
     * @return This reverb instance.
     */
    private Reverb setEarlyReflectionsLevel(float earlyLevel) {
        this.earlyLevel = earlyLevel;
        earlyGain.setGain(earlyLevel);
        return this;
    }

    /**
     * Gets the late reverb level.
     *
     * @return The late reverb level.
     */
    public float getLateReverbLevel() {
        return lateLevel;
    }

    /**
     * Sets the late reverb level (the amount of late reverb heard in the
     * output). The default value is 1.
     *
     * @param lateLevel The late reverb level.
     * @return This reverb instance.
     */
    private Reverb setLateReverbLevel(float lateLevel) {
        this.lateLevel = lateLevel;
        lateGain.setGain(lateLevel);
        return this;
    }

    /**
     * Sets the reverb parameters with a DataBead, using values stored in the
     * keys "damping", "roomSize", "earlyReflectionsLevel", and
     * "lateReverbLevel".
     *
     * @param db The parameter DataBead.
     */
    @Override
    public DataBeadReceiver sendData(DataAuvent db) {
        if (db != null) {
            setDamping(db.getFloat("damping", damping));
            setSize(db.getFloat("roomSize", size));
            setEarlyReflectionsLevel(db.getFloat("earlyReflectionsLevel",
                    earlyLevel));
            setLateReverbLevel(db.getFloat("lateReverbLevel", lateLevel));

        }
        return this;
    }

    /**
     * Gets a new DataBead filled with parameter values stored in the keys
     * "damping", "roomSize", "earlyReflectionsLevel", and "lateReverbLevel".
     *
     * @return The parameter DataBead.
     */
    public DataAuvent getParams() {
        DataAuvent db = new DataAuvent();
        db.put("damping", damping);
        db.put("roomSize", size);
        db.put("earlyReflectionsLevel", earlyLevel);
        db.put("lateReverbLevel", lateLevel);
        return db;
    }

}