package spacegraph.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioSourcePCMFloat extends AudioSource {

    private final AudioInputStream in;

    public AudioSourcePCMFloat(TargetDataLine lm) {
        super(lm);

        AudioFormat f = lm.getFormat();
        AudioInputStream _ub = new AudioInputStream(lm);

        f = new AudioFormat(AudioFormat.Encoding.PCM_FLOAT/*f.getEncoding()*/, f.getSampleRate(),
                32,
                1 /* channels */,
                f.getFrameSize(), f.getFrameRate(), f.isBigEndian());
        in = new AudioInputStream(_ub, f, _ub.getFrameLength());
    }
    public boolean read(int toRead) {
        //int availableBytes = Math.min(capacity, line.available());
        //audioBytesRead = line.read(preByteBuffer, 0, toRead);
        try {
            audioBytesRead = in.read(preByteBuffer, 0, toRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioBytesRead != 0;
    }
    @Override
    protected void decode(float[] target, int nSamplesRead) {

        ByteBuffer.wrap(preByteBuffer).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(target, 0, nSamplesRead);
    }
}
