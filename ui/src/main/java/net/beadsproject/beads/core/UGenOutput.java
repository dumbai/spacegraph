package net.beadsproject.beads.core;

import javax.sound.sampled.*;

public class UGenOutput extends AudioIO {

    /**
     * The default system buffer size.
     */
//
//
//    /**
//     * The system buffer size in frames.
//     */
//    private int systemBufferSizeInFrames;
//
//    /**
//     * The current byte buffer.
//     */
//    private int channels;

    public UGenOutput() {

    }


    /**
     * Starts the audio system running.
     */
    @Override
    protected boolean start() {

//        IOAudioFormat ioAudioFormat = getContext().getAudioFormat();
//        AudioFormat audioFormat =
//                new AudioFormat(ioAudioFormat.sampleRate, ioAudioFormat.bitDepth, ioAudioFormat.outputs, ioAudioFormat.signed, ioAudioFormat.bigEndian);

//        this.channels = audioFormat.getChannels();


        return true;
    }


    @Override
    protected UGen getAudioInput(int[] channels) {

        AudioContext ctx = getContext();
        IOAudioFormat ioAudioFormat = ctx.getAudioFormat();
        AudioFormat audioFormat =
                new AudioFormat(ioAudioFormat.sampleRate, ioAudioFormat.bitDepth, ioAudioFormat.inputs, ioAudioFormat.signed, ioAudioFormat.bigEndian);
        return new JavaSoundRTInput(ctx, audioFormat);
    }

    @Override
    public boolean read(float[] buf, int readRate) {

        int samples = buf.length;
        context.setBufferSize(samples);

        update();

        int c = 0;
        for (int i = 0; i < samples; i++) {
            int j = 0;
            buf[c++] = context.out.getValue(j, i);
        }
        return !context.stopped;
    }

//    @Override
//    public void stop() {
//        super.stop();
//        context.stop();
//    }

    /**
     * JavaSoundRTInput gathers audio from the JavaSound audio input device.
     *
     * @beads.category input
     */
    private static class JavaSoundRTInput extends UGen {

        /**
         * The audio format.
         */
        private final AudioFormat audioFormat;

        /**
         * The target data line.
         */
        private TargetDataLine targetDataLine;

        /**
         * Flag to tell whether JavaSound has been initialised.
         */
        private boolean javaSoundInitialized;

        private float[] interleavedSamples;
        private byte[] bbuf;

        /**
         * Instantiates a new RTInput.
         *
         * @param context     the AudioContext.
         * @param audioFormat the AudioFormat.
         */
        JavaSoundRTInput(AudioContext context, AudioFormat audioFormat) {
            super(context, audioFormat.getChannels());
            this.audioFormat = audioFormat;
            javaSoundInitialized = false;
        }

        /**
         * Set up JavaSound. Requires that JavaSound has been set up in AudioContext.
         */
        void initJavaSound() {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            try {
                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
                int inputBufferSize = 5000;
                targetDataLine.open(audioFormat, inputBufferSize);
                if (targetDataLine == null) System.out.println("no line");
                else
                    System.out.println("CHOSEN INPUT: " + targetDataLine.getLineInfo() + ", buffer size in bytes: " + inputBufferSize);
            } catch (LineUnavailableException ex) {
                System.out.println(getClass().getName() + " : Error getting line\n");
            }
            targetDataLine.start();
            javaSoundInitialized = true;
            interleavedSamples = new float[bufferSize * audioFormat.getChannels()];
            bbuf = new byte[bufferSize * audioFormat.getFrameSize()];
        }


        /* (non-Javadoc)
         * @see com.olliebown.beads.core.UGen#calculateBuffer()
         */
        @Override
        public void gen() {
            if (!javaSoundInitialized) {
                initJavaSound();
            }
            targetDataLine.read(bbuf, 0, bbuf.length);
            AudioUtils.byteToFloat(interleavedSamples, bbuf, audioFormat.isBigEndian());
            AudioUtils.deinterleave(interleavedSamples, audioFormat.getChannels(), bufferSize, bufOut);
        }


    }


}