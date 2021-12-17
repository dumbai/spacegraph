package spacegraph.audio.modem.chirp.transceive.util;

public class PitchProcessor {
    private final PitchDetector detector;
    private final PitchDetectionHandler handler;

    public PitchProcessor(PitchEstimationAlgorithm var1, float var2, int var3, PitchDetectionHandler var4) {
        this.detector = PitchEstimationAlgorithm.getDetector(var2, var3);
        this.handler = var4;
    }

    public boolean process(AudioEvent var1) {
        float[] var2 = var1.getFloatBuffer();
        PitchDetectionResult var3 = this.detector.getPitch(var2);
        this.handler.handlePitch(var3, var1);
        return true;
    }


    public enum PitchEstimationAlgorithm {
        YIN,
        MPM,
        FFT_YIN,
        DYNAMIC_WAVELET,
        FFT_PITCH,
        AMDF;

        PitchEstimationAlgorithm() {
        }

        static PitchDetector getDetector(float var1, int var2) {
            //            if (this == MPM) {
//                var3 = new McLeodPitchMethod(var1, var2);
//            } else if (this == DYNAMIC_WAVELET) {
//                var3 = new DynamicWavelet(var1, var2);
//            } else if (this == FFT_YIN) {
            PitchDetector var3 = new Yin(var1, var2);
//            } else if (this == AMDF) {
//                var3 = new AMDF(var1, var2);
//            } else if (this == FFT_PITCH) {
//                var3 = new FFTPitch(Math.round(var1), var2);
//            } else {
//                var3 = new Yin(var1, var2);
//            }

            return var3;
        }
    }

    @FunctionalInterface
    public interface PitchDetector {
        PitchDetectionResult getPitch(float[] var1);
    }

    @FunctionalInterface
    public interface PitchDetectionHandler {
        void handlePitch(PitchDetectionResult var1, AudioEvent var2);
    }


    static final class Yin implements PitchDetector {
        /**
         * The default YIN threshold value. Should be around 0.10~0.15. See YIN
         * paper for more information.
         */
        private static final double DEFAULT_THRESHOLD = 0.20;

        /**
         * The default size of an audio buffer (in samples).
         */
        public static final int DEFAULT_BUFFER_SIZE = 2048;

        /**
         * The default overlap of two consecutive audio buffers (in samples).
         */
        public static final int DEFAULT_OVERLAP = 1536;

        /**
         * The actual YIN threshold.
         */
        private final double threshold;

        /**
         * The audio sample rate. Most audio has a sample rate of 44.1kHz.
         */
        private final float sampleRate;

        /**
         * The buffer that stores the calculated values. It is exactly half the size
         * of the input buffer.
         */
        private final float[] yinBuffer;

        /**
         * The result of the pitch detection iteration.
         */
        private final PitchDetectionResult result;

        /**
         * Create a new pitch detector for a stream with the defined sample rate.
         * Processes the audio in blocks of the defined size.
         *
         * @param audioSampleRate
         *            The sample rate of the audio stream. E.g. 44.1 kHz.
         * @param bufferSize
         *            The size of a buffer. E.g. 1024.
         */
        Yin(float audioSampleRate, int bufferSize) {
            this(audioSampleRate, bufferSize, DEFAULT_THRESHOLD);
        }

        /**
         * Create a new pitch detector for a stream with the defined sample rate.
         * Processes the audio in blocks of the defined size.
         *
         * @param audioSampleRate
         *            The sample rate of the audio stream. E.g. 44.1 kHz.
         * @param bufferSize
         *            The size of a buffer. E.g. 1024.
         * @param yinThreshold
         *            The parameter that defines which peaks are kept as possible
         *            pitch candidates. See the YIN paper for more details.
         */
        Yin(float audioSampleRate, int bufferSize, double yinThreshold) {
            this.sampleRate = audioSampleRate;
            this.threshold = yinThreshold;
            yinBuffer = new float[bufferSize / 2];
            result = new PitchDetectionResult();
        }

        /**
         * The main flow of the YIN algorithm. Returns a pitch value in Hz or -1 if
         * no pitch is detected.
         *
         * @return a pitch value in Hz or -1 if no pitch is detected.
         */
        public PitchDetectionResult getPitch(float[] audioBuffer) {

            // step 2
            difference(audioBuffer);

            // step 3
            cumulativeMeanNormalizedDifference();

            // step 4
            int tauEstimate = absoluteThreshold();

            // step 5
            float pitchInHertz;
            if (tauEstimate != -1) {
                float betterTau = parabolicInterpolation(tauEstimate);

                // step 6
                // TODO Implement optimization for the AUBIO_YIN algorithm.
                // 0.77% => 0.5% error rate,
                // using the data of the YIN paper
                // bestLocalEstimate()

                // conversion to Hz
                pitchInHertz = sampleRate / betterTau;
            } else{
                // no pitch found
                pitchInHertz = -1;
            }

            result.setPitch(pitchInHertz);

            return result;
        }

        /**
         * Implements the difference function as described in step 2 of the YIN
         * paper.
         */
        private void difference(float[] audioBuffer) {
            int tau;
            for (tau = 0; tau < yinBuffer.length; tau++) {
                yinBuffer[tau] = 0;
            }
            for (tau = 1; tau < yinBuffer.length; tau++) {
                for (int index = 0; index < yinBuffer.length; index++) {
                    float delta = audioBuffer[index] - audioBuffer[index + tau];
                    yinBuffer[tau] += delta * delta;
                }
            }
        }

        /**
         * The cumulative mean normalized difference function as described in step 3
         * of the YIN paper. <br>
         * <code>
         * yinBuffer[0] == yinBuffer[1] = 1
         * </code>
         */
        private void cumulativeMeanNormalizedDifference() {
            yinBuffer[0] = 1;
            float runningSum = 0;
            for (int tau = 1; tau < yinBuffer.length; tau++) {
                runningSum += yinBuffer[tau];
                yinBuffer[tau] *= tau / runningSum;
            }
        }

        /**
         * Implements step 4 of the AUBIO_YIN paper.
         */
        private int absoluteThreshold() {
            // Uses another loop construct
            // than the AUBIO implementation
            int tau;
            // first two positions in yinBuffer are always 1
            // So start at the third (index 2)
            for (tau = 2; tau < yinBuffer.length; tau++) {
                if (yinBuffer[tau] < threshold) {
                    while (tau + 1 < yinBuffer.length && yinBuffer[tau + 1] < yinBuffer[tau]) {
                        tau++;
                    }
                    // found tau, exit loop and return
                    // store the probability
                    // From the YIN paper: The threshold determines the list of
                    // candidates admitted to the setAt, and can be interpreted as the
                    // proportion of aperiodic power tolerated
                    // within a periodic signal.
                    //
                    // Since we want the periodicity and and not aperiodicity:
                    // periodicity = 1 - aperiodicity
                    result.setProbability(1 - yinBuffer[tau]);
                    break;
                }
            }


            // if no pitch found, tau => -1
            if (tau == yinBuffer.length || yinBuffer[tau] >= threshold) {
                tau = -1;
                result.setProbability(0);
                result.setPitched(false);
            } else {
                result.setPitched(true);
            }

            return tau;
        }

        /**
         * Implements step 5 of the AUBIO_YIN paper. It refines the estimated tau
         * value using parabolic interpolation. This is needed to detect higher
         * frequencies more precisely. See http://fizyka.umk.pl/nrbook/c10-2.pdf and
         * for more background
         * http://fedc.wiwi.hu-berlin.de/xplore/tutorials/xegbohtmlnode62.html
         *
         * @param tauEstimate
         *            The estimated tau value.
         * @return A better, more precise tau value.
         */
        private float parabolicInterpolation(int tauEstimate) {
            int x0;

			x0 = tauEstimate < 1 ? tauEstimate : tauEstimate - 1;
            int x2;
			x2 = tauEstimate + 1 < yinBuffer.length ? tauEstimate + 1 : tauEstimate;
            float betterTau;
            if (x0 == tauEstimate) {
				betterTau = yinBuffer[tauEstimate] <= yinBuffer[x2] ? tauEstimate : x2;
            } else if (x2 == tauEstimate) {
				betterTau = yinBuffer[tauEstimate] <= yinBuffer[x0] ? tauEstimate : x0;
            } else {
                float s0 = yinBuffer[x0];
                float s1 = yinBuffer[tauEstimate];
                float s2 = yinBuffer[x2];
                // fixed AUBIO implementation, thanks to Karl Helgason:
                // (2.0f * s1 - s2 - s0) was incorrectly multiplied with -1
                betterTau = tauEstimate + (s2 - s0) / (2 * (2 * s1 - s2 - s0));
            }
            return betterTau;
        }
    }
}