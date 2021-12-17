//package spacegraph.space2d.widget.meter.audio;
//
///**
// **   __ __|_  ___________________________________________________________________________  ___|__ __
// **
// **
// **  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /
// **   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/
// **  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\
// ** /  \____\                       http:
// ** \  /   "' _________________________________________________________________________ `"   \  /
// **  \/____.                                                                             .____\/
// **
// ** Provides synchronization between a digital signal processor and speaker output. Slightly
// ** adapted, stripped down and modified ripoff from KJ-DSS project by Kristofer Fudalewski.
// ** Web: http:
// **
// **/
//
//import spacegraph.audio.AudioSource;
//
//import javax.sound.sampled.AudioFormat;
//import java.util.ArrayList;
//import java.util.List;
//
//public class BaseMusic_DigitalSignalSynchronizer {
//
//    private static final int DEFAULT_OVERRUN_PROTECTION = 4096;
//    private static final int DEFAULT_WRITE_CHUNK_SIZE   = 4096;
//
//    private AudioSource src;
//    private int mSampleSize;
//    private byte[] mAudioDataBuffer;
//    private final int mFramesPerSecond;
//    private final int mFrameRateRatioHintCalibration;
//    private int mPosition;
//    private Context mContext;
//    private Normalizer mNormalizer;
//    private Synchronizer mSynchronizer;
//    private final List<BaseMusic_DigitalSignalProcessorInterface> dsp = new ArrayList<>();
//
//
//    public BaseMusic_DigitalSignalSynchronizer(int inFramesPerSecond) {
//        this( inFramesPerSecond, inFramesPerSecond );
//    }
//
//    private BaseMusic_DigitalSignalSynchronizer(int inFramesPerSecond, int inFrameRateRatioHintCalibration) {
//        mFramesPerSecond = inFramesPerSecond;
//        mFrameRateRatioHintCalibration = inFrameRateRatioHintCalibration;
//    }
//
//    public Synchronizer getInternalSynchronizer() {
//        return mSynchronizer;
//    }
//
//    public void addAt(BaseMusic_DigitalSignalProcessorInterface inSignalProcessor) {
//        if (mSynchronizer!=null) {
//            inSignalProcessor.initialize(mSampleSize, src.audioFormat);
//        }
//        dsp.addAt(inSignalProcessor);
//    }
//
//    private Normalizer getNormalizer() {
//        if (mNormalizer == null) {
//            if (mNormalizer == null && mSynchronizer != null) {
//                mNormalizer = new Normalizer(src.audioFormat);
//            }
//        }
//        return mNormalizer;
//    }
//
//    public void remove( BaseMusic_DigitalSignalProcessorInterface pSignalProcessor ) {
//        dsp.remove( pSignalProcessor );
//    }
//
//
//    public void start(AudioSource src) {
//        if (mSynchronizer != null)
//            return;
//
//        this.src = src;
//        mSampleSize = Math.round(this.src.audioFormat.getFrameRate()/(float)mFramesPerSecond);
//        mContext = new Context(mSampleSize);
//        mAudioDataBuffer = new byte[src.bufferSamples()+DEFAULT_OVERRUN_PROTECTION];
//        mPosition = 0;
//        mNormalizer = null;
//        for (BaseMusic_DigitalSignalProcessorInterface wDsp : dsp) {
//            wDsp.initialize(mSampleSize, src.audioFormat);
//        }
//        mSynchronizer = new Synchronizer(mFramesPerSecond,mFrameRateRatioHintCalibration);
//    }
//
//    private void storeAudioData(byte[] pAudioData, int pOffset, int pLength) {
//        if (mAudioDataBuffer == null) {
//            return;
//        }
//        int wOverrun = 0;
//        if (mPosition + pLength > mAudioDataBuffer.length - 1) {
//            wOverrun = (mPosition + pLength) - mAudioDataBuffer.length;
//            pLength = mAudioDataBuffer.length - mPosition;
//        }
//        System.arraycopy(pAudioData, pOffset, mAudioDataBuffer, mPosition,pLength);
//        if (wOverrun > 0) {
//            System.arraycopy(pAudioData, pOffset + pLength, mAudioDataBuffer,0, wOverrun);
//            mPosition = wOverrun;
//        } else {
//            mPosition += pLength;
//        }
//    }
//
//
//
//
//
//
//
//    public void writeAudioData(byte[] pAudioData, int pOffset, int pLength) {
//
//
//
//            storeAudioData(pAudioData, pOffset, pLength);
//
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    public class Context {
//
//        private int    mBufferOffset;
//        private final int    mSampleLength;
//        private float  mFrameRatioHint;
//
//
//        Context(int pLength) {
//            mSampleLength = pLength;
//        }
//
//
//        public byte[] getDataBuffer() {
//            return mAudioDataBuffer;
//        }
//
//
//        public float[][] getDataNormalized() {
//            return getNormalizer().normalize( mAudioDataBuffer, mBufferOffset, mSampleLength );
//        }
//
//        public float getFrameRatioHint() {
//            return mFrameRatioHint;
//        }
//
//
//        public int getLength() {
//            return mSampleLength;
//        }
//
//
//
//
//        public int getOffset() {
//            return mBufferOffset;
//        }
//
//
//
//    }
//
//
//
//    public class Normalizer {
//
//        private final AudioFormat audioFormat;
//        private final float[][] channels;
//        private final int  channelSize;
//        private final long audioSampleSize;
//
//        Normalizer(AudioFormat pFormat) {
//            audioFormat = pFormat;
//            channels = new float[pFormat.getChannels()][];
//            for (int c = 0; c < pFormat.getChannels(); c++) {
//                channels[c] = new float[mSampleSize];
//            }
//            channelSize = audioFormat.getFrameSize() / audioFormat.getChannels();
//            audioSampleSize = (1 << (audioFormat.getSampleSizeInBits() - 1));
//        }
//
//        float[][] normalize(byte[] pData, int pPosition, int pLength) {
//            int wChannels  = audioFormat.getChannels();
//            int wSsib      = audioFormat.getSampleSizeInBits();
//            int wFrameSize = audioFormat.getFrameSize();
//
//            for( int sp = 0; sp < mSampleSize; sp++ ) {
//                if ( pPosition >= pData.length ) {
//                    pPosition = 0;
//                }
//                int cdp = 0;
//
//                for( int ch = 0; ch < wChannels; ch++ ) {
//
//                    long sm = ( pData[ pPosition + cdp ] & 0xFF ) - 128;
//                    for( int bt = 8, bp = 1; bt < wSsib; bt += 8 ) {
//                        sm += pData[ pPosition + cdp + bp ] << bt;
//                        bp++;
//                    }
//
//                    channels[ ch ][ sp ] = (float)sm / audioSampleSize;
//                    cdp += channelSize;
//                }
//                pPosition += wFrameSize;
//            }
//            return channels;
//        }
//    }
//
//
//
//    public class Synchronizer {
//
//        private final int mFrameSize;
//        private final long mCurrentFramesPerSecondInNanoSeconds;
//        private final long mFramesPerSecondInNanoSeconds;
//        private final long mFrameRateRatioHintCalibrationInNanoSeconds;
//
//        Synchronizer(int inFramesPerSecond, int inFrameRateRatioHintCalibration) {
//            mFramesPerSecondInNanoSeconds = 1000000000L /(long)inFramesPerSecond;
//            mCurrentFramesPerSecondInNanoSeconds = mFramesPerSecondInNanoSeconds;
//            mFrameRateRatioHintCalibrationInNanoSeconds = 1000000000L / inFrameRateRatioHintCalibration;
//            mFrameSize = src.audioFormat.getFrameSize();
//        }
//
//        private int calculateSamplePosition() {
//            return (int) (src.sampleNum() * mFrameSize % (mAudioDataBuffer.length));
//        }
//
//        public void synchronize() {
//            mContext.mBufferOffset = calculateSamplePosition();
//
//
//
//            mContext.mFrameRatioHint = mCurrentFramesPerSecondInNanoSeconds / (float) mFrameRateRatioHintCalibrationInNanoSeconds;
//
//            for (BaseMusic_DigitalSignalProcessorInterface aDsp : dsp) {
//                aDsp.process(mContext);
//            }
//        }
//
//    }
//}
