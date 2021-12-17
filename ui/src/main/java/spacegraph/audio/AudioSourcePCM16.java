package spacegraph.audio;

import jcog.TODO;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Signal sampled from system sound devices (via Java Media)
 * assume that internal timing refers to durations of ms (milliseconds)
 */
public class AudioSourcePCM16 extends AudioSource {

    private static final float shortRange = Short.MAX_VALUE;


//    private final short[] preShortBuffer;
    private final Mixer.Info mixer;

    /**
     * the constructor does not call start()
     * frameRate determines buffer size and frequency that events are emitted; can also be considered a measure of latency
     *
     * line may be already open or not.
     */
    public AudioSourcePCM16(Mixer.Info m, TargetDataLine line) {
        super(line);

        this.mixer = m;
        assert(line.getFormat().getEncoding()== AudioFormat.Encoding.PCM_SIGNED);
        assert(line.getFormat().getSampleSizeInBits() == 16);
//        assert(line.getFormat().getChannels() == 1);


//        int audioBufferSamples = line.getBufferSize();
//        preShortBuffer = new short[audioBufferSamples];

    }

    @Override
    public String name() {
        return mixer.toString();// + " " + super.name();
    }

//    @Override protected void decode(float[] target, int nSamplesRead) {
//        ByteBuffer.wrap(preByteBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(preShortBuffer);
//
//        int start = 0;
//        int end = nSamplesRead;
//        int j = 0;
////                short min = Short.MAX_VALUE, max = Short.MIN_VALUE;
//        double gain =
//                1.0 / shortRange;
//        //this.gain.floatValue() / shortRange;
//        for (int i = start; i < end; ) {
//            short s = preShortBuffer[i++];
////                    if (s < min) min = s;
////                    if (s > max) max = s;
//            target[j++] = (float) (s * gain); //compute in double for exra precision
//        }
//
//    }

    @Override protected void decode(float[] target, int samples) {

        double gain = 1.0 / shortRange; //compute in double for exra precision

        int channels = line.getFormat().getChannels();

        ShortBuffer sb = ByteBuffer.wrap(preByteBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

        int r = samples;
        int j = 0;
        switch (channels) {
            case 1:
                while (r > 0) {
                    short s = sb.get();
                    target[j++] = (float) (s * gain);
                    r--;
                }
                break;
            case 2:
                while (r > 0) {
                    short a = sb.get();
                    short b = sb.get();
                    target[j++] = (float) (((a * gain) + (b * gain)) / 2);
                    r--;
                }
                break;
            default:
                throw new TODO();
        }

    }

}
