package spacegraph.audio.sample;

import jcog.Util;
import spacegraph.audio.SoundProducer;

import static java.lang.System.arraycopy;


public class SamplePlayer implements SoundProducer {

    private final SoundSample sample;
    private int pos;

    public SamplePlayer(float[] buf, int rate) {
        this(new SoundSample(buf, rate));
    }

    public SamplePlayer(SoundSample sample) {
        this.sample = sample;
        this.pos = sample.start;
    }

    @Override
    public boolean read(float[] out, int readRate) {
        return Util.equals(sample.rate, readRate, 1.0f / readRate) ? readDirect(out) : readResampled(out, readRate);
    }

    private boolean readDirect(float[] out) {
        int remain = sample.end - pos;
        if (remain <= 0)
            return false;

        int toCopy = Math.min(out.length, remain);
        arraycopy(sample.buf, pos, out, 0, toCopy);
        pos += toCopy;

        return sample.end - pos > toCopy;
    }

    private boolean readResampled(float[] out, int readRate) {
        float step = (sample.rate) / readRate;
        float pos = this.pos;

        float[] in = sample.buf;
        float end = sample.end;

        for (int i = 0; i < out.length && pos < end; i++) {
            int posI = (int) pos;
            float a = in[posI];
            float b = in[posI+1];
            float p = (pos - posI);
            out[i] = ((a * p) + (b * (1-p)))/2;
            pos += step;
        }
        this.pos = Math.round(pos);
        return true;
    }

    @Override
    public void skip(int samplesToSkip, int readRate) {
        float step = sample.rate / readRate;
        pos += step * samplesToSkip;
    }

}