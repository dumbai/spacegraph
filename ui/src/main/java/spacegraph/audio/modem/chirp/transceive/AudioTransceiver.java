package spacegraph.audio.modem.chirp.transceive;


import org.eclipse.collections.impl.list.mutable.primitive.FloatArrayList;
import spacegraph.audio.modem.chirp.transceive.reedsolomon.GenericGF;
import spacegraph.audio.modem.chirp.transceive.reedsolomon.ReedSolomonDecoder;
import spacegraph.audio.modem.chirp.transceive.reedsolomon.ReedSolomonEncoder;
import spacegraph.audio.modem.chirp.transceive.util.AudioEvent;
import spacegraph.audio.modem.chirp.transceive.util.AudioFormat;
import spacegraph.audio.modem.chirp.transceive.util.PitchProcessor;

import java.util.*;

/**
 * adapted from
 * https://github.com/Cawfree/OpenChirp
 *   https://github.com/JorenSix/TarsosDSP
 *   https://github.com/zxing/zxing/
 * TODO test
 * see http://spatula.net/mt/blog/2011/02/modulating-and-demodulating-signals-in-java.html
 */
public class AudioTransceiver {

    /**
     * TODO: How to function based on the symbol period?
     */
    private static final Synth synth = new Synth.Builder()
            .setSymbolPeriodMs(85).build();

    /* Sampling Declarations. */
    private static final int WRITE_AUDIO_RATE_SAMPLE_HZ = 44100; // (Guaranteed for all devices!)
    private static final int WRITE_NUMBER_OF_SAMPLES = (int) (synth.encodedLen() * (synth.symPeriodMS / 1000.0f) * WRITE_AUDIO_RATE_SAMPLE_HZ);
    private static final int READ_NUMBER_OF_SAMPLES = ((int) ((synth.symPeriodMS / 1000.0f) * WRITE_AUDIO_RATE_SAMPLE_HZ));
    private static final int READ_SUBSAMPLING_FACTOR = 9;


    public final AudioFormat fmt;

    private final GenericGF lGenericGF = new GenericGF(synth.range.mGaloisPolynomial, synth.range.mFrameLength + 1, 1);
    /**
     * Declare the Galois Field. (5-bit, using root polynomial a^5 + a^2 + 1.)
     * Allocate the ReedSolomonEncoder and ReedSolomonDecoder.
     */
    private final ReedSolomonEncoder mReedSolomonEncoder = new ReedSolomonEncoder(lGenericGF);
    private final ReedSolomonDecoder mReedSolomonDecoder = new ReedSolomonDecoder(lGenericGF);

    private final boolean mSampleSelf;
    private FloatArrayList pitchBuffer;
    private FloatArrayList confBuffer;
    private final PitchProcessor mPitchProcessor = new PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
            WRITE_AUDIO_RATE_SAMPLE_HZ,
            (READ_NUMBER_OF_SAMPLES / READ_SUBSAMPLING_FACTOR),

            (pPitchDetectionResult, pAudioEvent) -> {

                if (isActive()) {
                    // Are we not allowed to sample ourself?
                    if (!isSampleSelf()) {
                        // Don't log the transactions.
                        return;
                    }
                }
                // Buffer the Pitch and the corresponding Confidence.
                pitchBuffer.add(pPitchDetectionResult.getPitch());
                confBuffer.add(pPitchDetectionResult.getProbability());
                // Process the signal.
                receive(mReedSolomonDecoder, pitchBuffer, confBuffer, READ_SUBSAMPLING_FACTOR, this::_onMessage);
            });


    private void _onMessage(String pMessage) {
        // Clear the buffer; prevent multiple transmissions coming through.
        pitchBuffer.clear();
        confBuffer.clear();
        onMessage(pMessage);
    }

    protected void onMessage(String msg) {

    }


    //    = new TarsosDSPAudioFormat(
//            getAudioDispatcher().getFormat().getEncoding(),
//            getAudioDispatcher().getFormat().getSampleRate(),
//            getAudioDispatcher().getFormat().getSampleSizeInBits() / READ_SUBSAMPLING_FACTOR,
//            getAudioDispatcher().getFormat().getChannels(),
//            getAudioDispatcher().getFormat().getFrameSize() / READ_SUBSAMPLING_FACTOR,
//            getAudioDispatcher().getFormat().getFrameRate(),
//            getAudioDispatcher().getFormat().isBigEndian(),
//            getAudioDispatcher().getFormat().properties());

    public AudioTransceiver(int sampleRate) {
        this(new AudioFormat(sampleRate, 16, 1, true, false));

//        int minAudioBufferSize = AudioRecord.getMinBufferSize(sampleRate,
//                android.media.AudioFormat.CHANNEL_IN_MONO,
//                android.media.AudioFormat.ENCODING_PCM_16BIT);
//        int minAudioBufferSizeInSamples =  minAudioBufferSize/2;
//        if(minAudioBufferSizeInSamples <= audioBufferSize ){
//            AudioRecord audioInputStream = new AudioRecord(
//                    MediaRecorder.AudioSource.MIC, sampleRate,
//                    android.media.AudioFormat.CHANNEL_IN_MONO,
//                    android.media.AudioFormat.ENCODING_PCM_16BIT,
//                    audioBufferSize * 2);

//            TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(sampleRate, 16,1, true, false);
    }

    public AudioTransceiver(AudioFormat fmt) {

        this.fmt = fmt;

//        // Implement the Parent.
//        super.onCreate(pSavedInstanceState);
//        // Define the ContentView.
//        this.setContentView(R.layout.activity_main);
        // Allocate the AudioTrack; this is how we'll be generating continuous audio.

//        this.mAudioTrack  = new AudioTrack(AudioManager.STREAM_MUSIC,
        //        MainActivity.WRITE_AUDIO_RATE_SAMPLE_HZ,
        //        AudioFormat.CHANNEL_OUT_MONO,
        //        AudioFormat.ENCODING_PCM_16BIT,
        //        MainActivity.WRITE_NUMBER_OF_SAMPLES,
        //        AudioTrack.MODE_STREAM);

        // Allocate the AudioDispatcher. (Note; requires dangerous permissions!)
//        this.mAudioDispatcher    = AudioDispatcherFactory.fromDefaultMicrophone(
//        MainActivity.WRITE_AUDIO_RATE_SAMPLE_HZ,
//        MainActivity.READ_NUMBER_OF_SAMPLES, 0); /** TODO: Abstract constants. */


        // Define whether we should listen to our own.
        mSampleSelf = true;
        // Declare the SampleBuffer; capable of storing an entire audio, with each symbol sampled at the sub-sampling rate.
        int pitchBufferSize = READ_SUBSAMPLING_FACTOR * synth.encodedLen();
        pitchBuffer = new FloatArrayList(pitchBufferSize);
        // Allocate the ConfidenceBuffer; declares the corresponding confidence for each sample.
        confBuffer = new FloatArrayList(pitchBufferSize);

    }

    private static String decode(int[] coded, int len) {
        StringBuilder s = new StringBuilder();
        // Buffer the initial characters.
        for (int i = 0; i < len; i++) {
            s.append(synth.range.chars.charAt(coded[i]));
        }
        // Iterate skipping over zero-padded region.
        for (int i = (coded.length) - synth.errLen; i < coded.length; i++) {
            s.append(synth.range.chars.charAt(coded[i]));
        }

        return s.toString();
    }


//    /**
//     * Inserts an element at the end of the array, pushing all other elements down towards lesser indices. Returns the popped element.
//     */
//    private static double push(final double[] pBuffer, final double pT) {
//        // Fetch the first element.
////        final double lPopped = pBuffer[0];
//        // Iterate the Buffer.
//        // Offset the entries.
//        System.arraycopy(pBuffer, 1, pBuffer, 0, pBuffer.length - 1);
//        // Append the new sample.
//        pBuffer[pBuffer.length - 1] = pT;
//        // Return the Popped sample.
////        return lPopped;
//    }

    /**
     * Called when the samples have been updated.
     */
    private static void receive(ReedSolomonDecoder pReedSolomonDecoder,
                                FloatArrayList pSamples, FloatArrayList pConfidences,
                                int pSubsamples,
                                Synth.IListener listener) {
        // Calculate the Number of Symbols.
        int lSymbols = (pSamples.size() / pSubsamples);
        // Declare the String.
        StringBuilder lAccumulation = new StringBuilder();
        // Iterate the Samples whilst we're building up the string.
        for (int i = 0; i < lSymbols && (lAccumulation.length() != synth.encodedLen()); i++) {
            // Fetch the Offset for the next Symbol.
            int lOffset = (i * pSubsamples);

            Synth.Result lResult = Synth.DETECTOR_MEAN.getSymbol(synth, pSamples, pConfidences, lOffset, pSubsamples);
            // Is the Result valid?
            if (lResult.mValid) {
                // Buffer the Result's data into the Accumulation.
                lAccumulation.append(lResult.mCharacter);
            }
        }
        // Is the accumulated data long enough?
        if (lAccumulation.length() == synth.encodedLen()) {
            // Declare the Packetized Representation.
            int[] lPacketized = new int[synth.range.mFrameLength];
            // Buffer the Header/Payload.
            int ii = synth.identifier.length() + synth.payloadLen;
            for (int i = 0; i < ii; i++) {
                // Update the Packetized with the corresponding index value.
                lPacketized[i] = synth.range.chars.indexOf(lAccumulation.charAt(i));
            }
            // Iterate the Error Symbols.
            for (int i = 0; i < synth.errLen; i++) {
                // Update the Packetized with the corresponding index value.
                lPacketized[synth.range.mFrameLength - synth.errLen + i] = synth.range.chars.indexOf(lAccumulation.charAt(synth.identifier.length() + synth.payloadLen + i));
            }
            // Attempt to Reed/Solomon Decode.
            try {
                // Decode the Sample.
                pReedSolomonDecoder.decode(lPacketized, synth.errLen);
                // Declare the search metric.
                boolean acc = true;
                int bound = synth.identifier.length();
                for (int i1 = 0; i1 < bound; i1++) {
                    boolean aBoolean = synth.identifier.charAt(i1) == (synth.range.chars.charAt(lPacketized[i1]));
                    acc = acc && aBoolean;
                }
                boolean lIsValid = acc;
                // Iterate the Identifier characters.
                // Update the search metric.
                // Is the message directed to us?
                if (lIsValid) {
                    // Fetch the Message data.
                    String lMessage = "";
                    // Iterate the Packet.
                    for (int i = synth.identifier.length(); i < ii; i++) {
                        // Accumulate the Message.
                        lMessage += synth.range.chars.charAt(lPacketized[i]);
                    }
                    // Call the callback.
                    listener.listen(lMessage);
                }
            } catch (ReedSolomonDecoder.ReedSolomonException pReedSolomonException) {
                /* Do nothing; we're transmitting across a very lossy channel! */
            }
        }
    }

    /**
     * Prints the equivalent information representation of a data string.
     */
    @SuppressWarnings("unused")
    private static void indices(CharSequence pData, int[] pBuffer, int pOffset) {
        // Iterate the Data.
        int len = pData.length();
        for (int i = 0; i < len; i++) {
            // Update the contents of the Array.
            pBuffer[pOffset + i] = synth.range.chars.indexOf(pData.charAt(i));
        }
    }

    /**
     * Generates a tone for the audio stream.
     */
    private static float[] encode(CharSequence pData, int pPeriod) {
        // Calculate the Number of Samples per msg.
        int lNumberOfSamples = (int) (WRITE_AUDIO_RATE_SAMPLE_HZ * (pPeriod / 1000.0f));

        int pdl = pData.length();
        float[] lSampleArray = new float[pdl * lNumberOfSamples];

//        final byte[] lGeneration = new byte[lSampleArray.length * 2];

        int lOffset = 0;

        for (int i = 0; i < pdl; i++) {
            // Fetch the Data.
            Character lData = pData.charAt(i);
            // Fetch the Frequency.
            double lFrequency = synth.mapCharFreq.get(lData);
            // Iterate the NumberOfSamples. (Per msg data.)
            for (int j = 0; j < lNumberOfSamples; j++) {
                // Update the SampleArray.
                lSampleArray[lOffset] = (float) Math.sin(2 * Math.PI * j / (WRITE_AUDIO_RATE_SAMPLE_HZ / lFrequency));
                lOffset++;
            }
        }
        // Reset the Offset.
//        lOffset = 0;
        // Iterate between each sample.
        for (int i = 0; i < pdl; i++) {
            // Fetch the Start and End Indexes of the Sample.
            int lIo = i * lNumberOfSamples;
            int lIa = lIo + lNumberOfSamples;
            // Declare the RampWidth. We'll change it between iterations for more tuneful sound.)
            int lRw = (int) (lNumberOfSamples * 0.3);
            // Iterate the Ramp.
            for (int j = 0; j < lRw; j++) {
                // Calculate the progression of the Ramp.
                double lP = j / (double) lRw;
                // Scale the corresponding samples.
                lSampleArray[lIo + j] *= lP;
                lSampleArray[lIa - j - 1] *= lP;
            }
        }
        return lSampleArray;

//        // Declare the filtering constant.
//        final double lAlpha = 0.3;
//        double lPrevious = 0;

//        // Iterate the SampleArray.
//        for (final double lValue : lSampleArray) {
//            // Fetch the Value.
//            // Filter the Value.
//            final double lFiltered = (lAlpha < 1.0) ? ((lValue - lPrevious) * lAlpha) : lValue;
//            // Assume normalized, so scale to the maximum amplitude.
//            final short lPCM = (short) ((lFiltered * 32767));
//            // Supply the Generation with 16-bit PCM. (The first byte is the low-order byte.)
//            lGeneration[lOffset++] = (byte) (lPCM & 0x00FF);
//            lGeneration[lOffset++] = (byte) ((lPCM & 0xFF00) >>> 8);
//            // Overwrite the Previous with the Filtered value.
//            lPrevious = lFiltered;
//        }
//        // Return the Generation.
//        return lGeneration;
    }

    /**
     * When processing audio...
     */
    public final boolean process(AudioEvent pAudioEvent) {
        // Declare the TarsosDSPFormat; essentially make a safe copy of the existing setup, that recognizes the buffer has been split up.


        // Fetch the Floats.
        float[] lFloats = pAudioEvent.getFloatBuffer();
        // Calculate the FrameSize.
        int lFrameSize = (lFloats.length / READ_SUBSAMPLING_FACTOR);
        // Iterate across the Floats.
        for (int i = 0; i < (lFloats.length - lFrameSize); i += lFrameSize) {
            // Segment the buffer.
            float[] lSegment = Arrays.copyOfRange(lFloats, i, i + lFrameSize);

            AudioEvent lAudioEvent = new AudioEvent(fmt, lSegment);

            // Export the AudioEvent to the PitchProessor.
            mPitchProcessor.process(lAudioEvent);
        }
        // Assert that the event was handled.
        return true;
    }

    public final float[] encode(String pMessage) throws UnsupportedOperationException {
        // Is the message the correct length?
        if (pMessage.length() != synth.payloadLen) {
            // Assert that we can't generate the audio; they need to match the Payload.
            throw new UnsupportedOperationException("Invalid message size (" + pMessage.length() + ")! Expected " + synth.payloadLen + " symbols.");
        }
        // Declare the search metric.
        boolean lIsSupported = true;
        // Iterate through the Message.
        for (char c : pMessage.toCharArray()) {
            // Update the search metric.
            lIsSupported &= synth.range.chars.indexOf(c) != -1;
        }
        // Is the message not supported?
        if (!lIsSupported) {
            // Inform the user.
            throw new UnsupportedOperationException("Message \"" + pMessage + "\" contains illegal characters.");
        }

        // Assert that we're transmitting the Message. (Don't show error checksum codewords or the identifer.)
//        Log.d(TAG, "Tx(" + pMessage + ")");
        // Append the Header.
        pMessage = synth.identifier + pMessage;

        int[] buffer = new int[synth.range.mFrameLength];
        // Fetch the indices of the Message.
        indices(pMessage, buffer, 0);
        // Encode the Bytes.
        mReedSolomonEncoder.encode(buffer, synth.errLen);

        String msg = decode(buffer, pMessage.length()); // "hj050422014jikhif";
        // (Period is in milliseconds.)
        int period = synth.symPeriodMS;


        return AudioTransceiver.encode(msg, period);
    }

    private static boolean isActive() {
        boolean active = true;
        return active;
    }

//    @SuppressWarnings("unused")
//    public final void setSampleSelf(final boolean pIsSampleSelf) {
//        this.mSampleSelf = pIsSampleSelf;
//    }

    private boolean isSampleSelf() {
        return mSampleSelf;
    }


    static final class Synth {

        /**
         * TODO: optimize (<100ms).
         */

        /* Default Declarations. */
        static final int DEFAULT_FREQUENCY_BASE = 1760;
        static final Range DEFAULT_RANGE = new Range("0123456789abcdefghijklmnopqrstuv",
                0b00100101, 31);
        /**
         * TODO: i.e. 2^5 - 1.
         */
        static final String DEFAULT_IDENTIFIER = "hj";
        static final int DEFAULT_LENGTH_PAYLOAD = 10;
        static final int DEFAULT_LENGTH_CRC = 8;
        static final Result RESULT_UNKNOWN = new Result((char) 0, false);
        /**
         * A default detector, which uses an average to interpret symbols.
         */
        static final IDetector DETECTOR_MEAN = (pSynth, pSamples, pConfidences, pOffset, pLength) -> {
            // Ignore the First/Last 18% of the Samples. (Protected against slew rate.)
            int lIgnore = (int) Math.ceil(pLength * 0.3);
            // Declare buffers to accumulate the sampled frequencies.
            double lFacc = 0.0;
            int lCount = 0;
            // Iterate the Samples.
            for (int i = pOffset + lIgnore; i < pOffset + pLength - lIgnore; i++) { /** TODO: fn */
                // Are we confident in this sample?
                if (pConfidences.get(i) > 0.75) {
                    // Fetch the Sample.
                    double lSample = pSamples.get(i);
                    // Is the Sample valid?
//                    if (lSample != -1) {
                        // Accumulate the Sample.
                        lFacc += lSample;
                        // Update the accumulated count.
                        lCount++;
//                    }
                }
            }
            // Result valid?
            if (lCount != 0) {
                // Calculate the Mean.
                double lMean = lFacc / lCount;
                /** TODO: Frequency tolerance? */
                // Return the Result.
                return new Result(pSynth.character(lMean), true);
            } else {
                // Return the invalid result.
                return Synth.RESULT_UNKNOWN;
            }
        };
        /* Static Declarations. */
        private static final double SEMITONE = 1.05946311;
        private static final int MINIMUM_PERIOD_MS = 120;
        static final int DEFAULT_PERIOD_MS = Synth.MINIMUM_PERIOD_MS;
        private final String identifier;
        private final Range range;
        private final int payloadLen;
        private final int errLen;
        private final int symPeriodMS;
        private final double[] freqs;
        /**
         * TODO: to "tones"
         */
        private final Map<Character, Double> mapCharFreq;
        private final Map<Double, Character> mapFreqChar;
        /**
         * Private construction; force the Builder pattern.
         */
        private Synth(double pBaseFrequency, String pIdentifier, Range pRange, int pPayloadLength, int pErrorLength, int pSymbolPeriodMs) {
            // Allocate the Frequencies.
            double[] lFrequencies = new double[pRange.chars.length()];
            // Declare the Mappings. (It's useful to index via either the Character or the Frequency.)
            Map<Character, Double> lMapCharFreq = new HashMap<>(); /** TODO: Use only a single mapping? */
            Map<Double, Character> lMapFreqChar = new HashMap<>();
            // Generate the frequencies that correspond to each valid symbol.
            for (int i = 0; i < pRange.chars.length(); i++) {
                // Fetch the Character.
                char c = pRange.chars.charAt(i);
                // Calculate the Frequency.
                double lFrequency = pBaseFrequency * Math.pow(Synth.SEMITONE, i);
                // Buffer the Frequency.
                lFrequencies[i] = lFrequency;
                // Buffer the Frequency.
                lMapCharFreq.put(c, lFrequency);
                lMapFreqChar.put(lFrequency, c);
            }
            // Initialize Member Variables.
            /* Member Variables. */
            identifier = pIdentifier;
            range = pRange;
            payloadLen = pPayloadLength;
            errLen = pErrorLength;
            symPeriodMS = pSymbolPeriodMs;
            // Assign the Frequencies.
            freqs = lFrequencies; /** TODO: Move to a fn of the MapFreqChar. */
            // Prepare the Mappings. (Make them unmodifiable after initialization.)
            mapCharFreq = Collections.unmodifiableMap(lMapCharFreq);
            mapFreqChar = Collections.unmodifiableMap(lMapFreqChar);
        }

        /**
         * Returns the Character corresponding to a Frequency.
         */
        final char character(double pPitch) {
            // Declare search metrics.
            double lDistance = Double.POSITIVE_INFINITY;
            int lIndex = -1;
            // Iterate the Frequencies.
            for (int i = 0; i < freqs.length; i++) {
                // Fetch the Frequency.
                double lFrequency = freqs[i];
                // Calculate the Delta.
                double lDelta = Math.abs(pPitch - lFrequency);
                // Is the Delta smaller than the current distance?
                if (lDelta < lDistance) {
                    // Overwrite the Distance.
                    lDistance = lDelta;
                    // Track the Index.
                    lIndex = i;
                }
            }
            // Fetch the corresponding character.
            Character lCharacter = mapFreqChar.get(freqs[lIndex]);
            // Return the Character.
            return Optional.ofNullable(lCharacter).orElseGet(() -> (char) 0);
        }

        /**
         * Returns the total length of an encoded message.
         */
        final int encodedLen() {
            // The total packet is comprised as follows: [ID_SYMBOLS + PAYLOAD_SYMBOLS + ERROR_SYMBOLS].
            return identifier.length() + payloadLen + errLen;
        }
        /**
         * A base interface for a concrete class capable of interpreting data.
         */
        @FunctionalInterface
        interface IDetector {
            /**
             * Detects a Symbol within an array of Samples and Confidences. Callers must define the segment they wish to analyze within the provided buffers.
             */
            Result getSymbol(Synth pSynth, FloatArrayList pSamples, FloatArrayList pConfidences, int pOffset, int pLength);
        }
        /**
         * Called when a transmission has beend detected.
         */
        interface IListener {
            /**
             * Called when a transmission has been detected. Returns the response data.
             */
            void listen(String pMessage);
        }

        /**
         * TODO use CharBoolean pair
         */
        static class Result {
            /* Member Variables. */
            private final char mCharacter;
            private final boolean mValid;

            /**
             * Constructor.
             */
            Result(char pCharacter, boolean pIsValid) {
                // Initialize Member Variables.
                mCharacter = pCharacter;
                mValid = pIsValid;
            }

        }

        /**
         * Defines the set of allowable characters for a given protocol.
         */
        static class Range {
            /* Member Variables. */
            private final String chars;
            private final int mGaloisPolynomial;
            private final int mFrameLength;

            /**
             * Constructor.
             */
            Range(String pCharacters, int pGaloisPolynomial, int pFrameLength) {
                // Initialize Member Variables.
                chars = pCharacters;
                mGaloisPolynomial = pGaloisPolynomial;
                mFrameLength = pFrameLength;
            }

        }

        /**
         * Factory Pattern.
         */
        static final class Builder {
            private final Range mRange = Synth.DEFAULT_RANGE;
            private int mSymbolPeriodMs = Synth.DEFAULT_PERIOD_MS;


            final Synth build() throws IllegalStateException {

                /** TODO: Check lengths etc */
                /* Default Declarations. */
                // Declares the initial frequency which the range of transmissions uses
                return new Synth(Synth.DEFAULT_FREQUENCY_BASE, Synth.DEFAULT_IDENTIFIER, mRange, Synth.DEFAULT_LENGTH_PAYLOAD, Synth.DEFAULT_LENGTH_CRC, getSymbolPeriodMs());
            }

            final int getSymbolPeriodMs() {
                return mSymbolPeriodMs;
            }

            /* Setters. */
            final Builder setSymbolPeriodMs(int pSymbolPeriodMs) {
                mSymbolPeriodMs = pSymbolPeriodMs;
                return this;
            }
        }
    }
}