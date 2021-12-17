/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import jcog.signal.Tensor;
import net.beadsproject.beads.buffers.CosineWindow;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;

import java.util.LinkedList;

/**
 * GranularSamplePlayer plays back a {@link Sample} using granular synthesis. GranularSamplePlayer inherits its main behaviour from {@link SamplePlayer} but replaces the direct {@link Sample} lookup with a granular process.
 * {@link UGen}s can be used to control playback rate, pitch, loop points, grain size, grain interval, grain randomness and position (this last case assumes that the playback rate is zero).
 *
 * @author ollie
 * @beads.category sample players
 * @see SamplePlayer Sample
 */
public class GranularSamplePlayer extends SamplePlayer {

    /**
     * The pitch envelope.
     */
    private UGen pitchEnvelope;

    /**
     * The grain interval envelope.
     */
    private UGen grainIntervalEnvelope;

    /**
     * The grain size envelope.
     */
    private UGen grainSizeEnvelope;

    /**
     * The randomness envelope.
     */
    private UGen randomnessEnvelope;

    /**
     * The random pan envelope.
     */
    private UGen randomPanEnvelope;

    /**
     * The time in milliseconds since the last grain was activated.
     */
    private float timeSinceLastGrain;

    /**
     * The length of one sample in milliseconds.
     */
    private final double msPerSample;

    /**
     * The pitch, bound to the pitch envelope.
     */
    private float pitch;

    /**
     * The list of current grains.
     */
    private final LinkedList<Grain> grains;

    /**
     * A list of free grains.
     */
    private final LinkedList<Grain> freeGrains;

    /**
     * A list of dead grains.
     */
    private final LinkedList<Grain> deadGrains;

    /**
     * The window used by grains.
     */
    private Tensor window;

    /**
     * Flag to determine whether, looping occurs within individual grains.
     */
    private final boolean loopInsideGrains;

    /**
     * The nested class Grain. Stores information about the start time, current position, age, and grain size of the grain.
     */
    private static class Grain {

        /**
         * The position in millseconds.
         */
        double position;

        /**
         * The age of the grain in milliseconds.
         */
        double age;

        /**
         * The grain size of the grain. Fixed at instantiation.
         */
        double grainSize;

        /**
         * The pan level for each channel. Currently only 2 channel is supported.
         */
        float[] pan;
    }

    /**
     * Instantiates a new GranularSamplePlayer.
     *
     * @param context the AudioContext.
     * @param outs    the number of outputs.
     */
    private GranularSamplePlayer(AudioContext context, int outs) {
        super(context, outs);
        grains = new LinkedList<>();
        freeGrains = new LinkedList<>();
        deadGrains = new LinkedList<>();
        pitchEnvelope = new Static(context, 1.0f);
        setGrainInterval(new Static(context, 70.0f));
        setGrainSize(new Static(context, 100.0f));
        setRandomness(new Static(context, 0.0f));
        setRandomPan(new Static(context, 0.0f));
        setWindow(new CosineWindow().the());
        msPerSample = context.samplesToMs(1.0f);
        loopInsideGrains = false;
    }

    /**
     * Instantiates a new GranularSamplePlayer.
     *
     * @param context the AudioContext.
     * @param buffer  the Sample played by the GranularSamplePlayer.
     */
    public GranularSamplePlayer(AudioContext context, Sample buffer) {
        this(context, buffer.getNumChannels());
        setSample(buffer);
        loopStartEnvelope = new Static(context, 0.0f);
        loopEndEnvelope = new Static(context, (float) buffer.getLength());
    }

    /**
     * Gets the pitch envelope.
     *
     * @return the pitch envelope.
     * @deprecated use {@link #getPitchUGen()}.
     */
    @Override
    @Deprecated
    public UGen getPitchEnvelope() {
        return pitchEnvelope;
    }


    /**
     * Gets the pitch UGen.
     *
     * @return the pitch UGen.
     */
    @Override
    public UGen getPitchUGen() {
        return pitchEnvelope;
    }

    /**
     * Sets the pitch envelope.
     *
     * @param pitchEnvelope the new pitch envelope.
     * @deprecated Use {@link #setPitch(UGen)} instead.
     */
    @Override
    @Deprecated
    public void setPitchEnvelope(UGen pitchEnvelope) {
        this.pitchEnvelope = pitchEnvelope;
    }

    /**
     * Sets the pitch UGen.
     *
     * @param pitchUGen the new pitch Ugen.
     */
    @Override
    public void setPitch(UGen pitchUGen) {
        this.pitchEnvelope = pitchUGen;
    }

    /**
     * Gets the grain interval envelope.
     *
     * @return the grain interval envelope.
     * @deprecated Use {@link #getGrainIntervalUGen()} instead.
     */
    @Deprecated
    public UGen getGrainIntervalEnvelope() {
        return grainIntervalEnvelope;
    }

    /**
     * Gets the grain interval UGen.
     *
     * @return the grain interval UGen.
     */
    public UGen getGrainIntervalUGen() {
        return grainIntervalEnvelope;
    }

    /**
     * Sets the grain interval envelope.
     *
     * @param grainIntervalEnvelope the new grain interval envelope.
     * @deprecated Use {@link #setGrainInterval(UGen)} instead.
     */
    @Deprecated
    public void setGrainIntervalEnvelope(UGen grainIntervalEnvelope) {
        this.grainIntervalEnvelope = grainIntervalEnvelope;
    }

    /**
     * Sets the grain interval UGen.
     *
     * @param grainIntervalUGen the new grain interval UGen.
     */
    public void setGrainInterval(UGen grainIntervalUGen) {
        this.grainIntervalEnvelope = grainIntervalUGen;
    }

    /**
     * Gets the grain size envelope.
     *
     * @return the grain size envelope.
     * @deprecated Use {@link #getGrainSizeUGen()} instead.
     */
    @Deprecated
    public UGen getGrainSizeEnvelope() {
        return grainSizeEnvelope;
    }


    /**
     * Gets the grain size UGen.
     *
     * @return the grain size UGen.
     */
    public UGen getGrainSizeUGen() {
        return grainSizeEnvelope;
    }

    /**
     * Sets the grain size envelope.
     *
     * @param grainSizeEnvelope the new grain size envelope.
     * @deprecated Use {@link #setGrainSize(UGen)} instead.
     */
    @Deprecated
    public void setGrainSizeEnvelope(UGen grainSizeEnvelope) {
        this.grainSizeEnvelope = grainSizeEnvelope;
    }

    /**
     * Sets the grain size UGen.
     *
     * @param grainSizeUGen the new grain size UGen.
     */
    public void setGrainSize(UGen grainSizeUGen) {
        this.grainSizeEnvelope = grainSizeUGen;
    }

    public Tensor getWindow() {
        return window;
    }


    private void setWindow(Tensor window) {
        this.window = window;
    }

    /**
     * Gets the randomness envelope.
     *
     * @return the randomness envelope.
     * @deprecated Use {@link #getRandomnessUGen()} instead.
     */
    @Deprecated
    public UGen getRandomnessEnvelope() {
        return randomnessEnvelope;
    }

    /**
     * Gets the randomness UGen.
     *
     * @return the randomness UGen.
     */
    public UGen getRandomnessUGen() {
        return randomnessEnvelope;
    }

    /**
     * Sets the randomness envelope.
     *
     * @param randomnessEnvelope the new randomness envelope.
     * @deprecated Use {@link #setRandomness(UGen)} instead.
     */
    @Deprecated
    public void setRandomnessEnvelope(UGen randomnessEnvelope) {
        this.randomnessEnvelope = randomnessEnvelope;
    }

    /**
     * Sets the randomness UGen.
     *
     * @param randomnessUGen the new randomness UGen.
     */
    public void setRandomness(UGen randomnessUGen) {
        this.randomnessEnvelope = randomnessUGen;
    }

    /**
     * @return the random pan envelope.
     * @deprecated Use {@link #getRandomPanUGen()} instead.
     */
    @Deprecated
    public UGen getRandomPanEnvelope() {
        return randomPanEnvelope;
    }

    /**
     * Gets the random pan UGen.
     *
     * @return the random pan Ugen.
     */
    public UGen getRandomPanUGen() {
        return randomPanEnvelope;
    }

    /**
     * @param randomPanEnvelope
     * @deprecated Use {@link #setRandomPan(UGen)} instead.
     */
    @Deprecated
    public void setRandomPanEnvelope(UGen randomPanEnvelope) {
        this.randomPanEnvelope = randomPanEnvelope;
    }

    private void setRandomPan(UGen randomPanEnvelope) {
        this.randomPanEnvelope = randomPanEnvelope;
    }


    /**
     * @deprecated Use {@link #setSample(Sample)} instead.
     */
    @Override
    @Deprecated
    public synchronized void setBuffer(Sample buffer) {
        super.setSample(buffer);
        grains.clear();
        timeSinceLastGrain = 0.0f;
    }

    /* (non-Javadoc)
     * @see net.beadsproject.beads.ugens.SamplePlayer#setBuffer(net.beadsproject.beads.data.Sample)
     */
    @Override
    synchronized void setSample(Sample buffer) {
        super.setSample(buffer);
        grains.clear();
        timeSinceLastGrain = 0.0f;
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#start()
     */
    @Override
    public void start() {
        super.start();
        timeSinceLastGrain = 0;
    }

    /**
     * Sets the given Grain to start immediately.
     *
     * @param g    the g
     * @param time the time
     */
    private void resetGrain(Grain g, int time) {
        final float a = grainSizeEnvelope.getValue(0, time);
        g.position = position + (a * randomnessEnvelope.getValue(0, time) * (Math.random() * 2.0 - 1.0));
        g.age = 0.0f;
        g.grainSize = a;
    }

    private void setGrainPan(Grain g, float panRandomness) {
        g.pan = new float[outs];
        if (outs == 2) {
            float pan = (float) Math.random() * Math.min(1, Math.max(0, panRandomness)) * 0.5f;
            pan = Math.random() < 0.5f ? 0.5f + pan : 0.5f - pan;
            g.pan[0] = pan > 0.5f ? 1.0f : 2.0f * pan;
            g.pan[1] = pan < 0.5f ? 1.0f : 2.0f * (1 - pan);
        } else {
            for (int i = 0; i < outs; i++) {
                g.pan[i] = 1;
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        firstGrain = true;
    }

    /**
     * Flag to indicate special case for the first grain.
     */
    private boolean firstGrain = true;

    /**
     * Special case method for playing first grain.
     */
    private void firstGrain() {
        if (firstGrain) {
            Grain g = new Grain();
            g.position = position;
            g.age = grainSizeEnvelope.getValue() / 4.0f;
            g.grainSize = grainSizeEnvelope.getValue(0, 0);
            grains.add(g);
            firstGrain = false;
            timeSinceLastGrain = grainIntervalEnvelope.getValue() / 2.0f;
            setGrainPan(g, randomPanEnvelope.getValue(0, 0));
        }
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.ugens.SamplePlayer#calculateBuffer()
     */
    @Override
    public synchronized void gen() {
        
        
        if (sample != null) {
            rateEnvelope.update();
            if (positionEnvelope != null) {
                positionEnvelope.update();
            }
            loopStartEnvelope.update();
            loopEndEnvelope.update();
            pitchEnvelope.update();
            grainIntervalEnvelope.update();
            grainSizeEnvelope.update();
            randomnessEnvelope.update();
            randomPanEnvelope.update();
            firstGrain();
            
            for (int i = 0; i < bufferSize; i++) {
                
                if (timeSinceLastGrain > grainIntervalEnvelope.getValue(0, i)) {
                    Grain g = freeGrains.isEmpty() ? new Grain() : freeGrains.pollFirst();
                    resetGrain(g, i);
                    setGrainPan(g, randomPanEnvelope.getValue(0, i));
                    grains.add(g);
                    timeSinceLastGrain = 0.0f;
                }
                
                for (int j = 0; j < outs; j++) {
                    bufOut[j][i] = 0.0f;
                }
                
                for (Grain g : grains) {
                    
                    float windowScale = window.getFractInterp((float) (g.age / g.grainSize));
                    
                    
                    switch (interpolationType) {
                        case ADAPTIVE:
                            if (pitch > ADAPTIVE_INTERP_HIGH_THRESH) {
                                sample.getFrameNoInterp(g.position, frame);
                            } else if (pitch > ADAPTIVE_INTERP_LOW_THRESH) {
                                sample.getFrameLinear(g.position, frame);
                            } else {
                                sample.getFrameCubic(g.position, frame);
                            }
                            break;
                        case LINEAR:
                            sample.getFrameLinear(g.position, frame);
                            break;
                        case CUBIC:
                            sample.getFrameCubic(g.position, frame);
                            break;
                        case NONE:
                            sample.getFrameNoInterp(g.position, frame);
                            break;
                    }
                    
                    for (int j = 0; j < outs; j++) {
                        bufOut[j][i] += g.pan[j] * windowScale * frame[j % sample.getNumChannels()];
                    }
                }
                
                calculateNextPosition(i);
                pitch = Math.abs(pitchEnvelope.getValue(0, i));
                for (Grain g : grains) {
                    calculateNextGrainPosition(g);
                }
                
                timeSinceLastGrain += msPerSample;
                
                for (Grain g : grains) {
                    if (g.age > g.grainSize) {
                        freeGrains.add(g);
                        deadGrains.add(g);
                    }
                }
                grains.removeAll(deadGrains);
                deadGrains.clear();
            }
        }
    }

    /**
     * Calculate next position for the given Grain.
     *
     * @param g the Grain.
     */
    private void calculateNextGrainPosition(Grain g) {
        int direction = rate >= 0 ? 1 : -1;    
        g.age += msPerSample;
        if (loopInsideGrains) {
            switch (loopType) {
                case NO_LOOP_FORWARDS -> g.position += direction * positionIncrement * pitch;
                case NO_LOOP_BACKWARDS -> g.position -= direction * positionIncrement * pitch;
                case LOOP_FORWARDS -> {
                    g.position += direction * positionIncrement * pitch;
                    if (rate > 0 && g.position > Math.max(loopStart, loopEnd)) {
                        g.position = Math.min(loopStart, loopEnd);
                    } else if (rate < 0 && g.position < Math.min(loopStart, loopEnd)) {
                        g.position = Math.max(loopStart, loopEnd);
                    }
                }
                case LOOP_BACKWARDS -> {
                    g.position -= direction * positionIncrement * pitch;
                    if (rate > 0 && g.position < Math.min(loopStart, loopEnd)) {
                        g.position = Math.max(loopStart, loopEnd);
                    } else if (rate < 0 && g.position > Math.max(loopStart, loopEnd)) {
                        g.position = Math.min(loopStart, loopEnd);
                    }
                }
                case LOOP_ALTERNATING -> {
                    g.position += direction * (forwards ? positionIncrement * pitch : -positionIncrement * pitch);
                    if (forwards ^ (rate < 0)) {
                        if (g.position > Math.max(loopStart, loopEnd)) {
                            g.position = 2 * Math.max(loopStart, loopEnd) - g.position;
                        }
                    } else if (g.position < Math.min(loopStart, loopEnd)) {
                        g.position = 2 * Math.min(loopStart, loopEnd) - g.position;
                    }
                }
            }
        } else {
            g.position += direction * positionIncrement * pitch;
        }
    }

    /**
     * Calculates the average number of Grains given the current grain size and grain interval.
     *
     * @return the average number of Grains.
     */
    public float getAverageNumberOfGrains() {
        return grainSizeEnvelope.getValue() / grainIntervalEnvelope.getValue();
    }






















}