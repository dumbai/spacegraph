/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.core;


/**
 * Encapsulates data bout audio format for IO. Also has number of
 * input channels, in addition to the standard number of channels, so this
 * defines the format for an input/output system.
 * <p>
 * We have elected to use our own AudioFormat instead of
 * javax.sound.sampled.AudioFormat as javasound is not supported everywhere.
 *
 * @author ben
 */
public class IOAudioFormat {

    public final int inputs;
    public final int outputs;
    public final int bitDepth;
    public final float sampleRate;
    public final boolean bigEndian;
    public final boolean signed;


    public IOAudioFormat(float sampleRate,
                         int bitDepth,
                         int inputs,
                         int outputs,
                         boolean signed,
                         boolean bigEndian
    ) {
        this.sampleRate = sampleRate;
        this.bitDepth = bitDepth;
        this.signed = signed;
        this.bigEndian = bigEndian;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public IOAudioFormat(float sampleRate, int bitDepth, int inputs, int outputs) {
        this(sampleRate, bitDepth, inputs, outputs, true, true);
    }


}
