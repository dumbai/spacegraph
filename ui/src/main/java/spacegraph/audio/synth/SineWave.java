package spacegraph.audio.synth;

import spacegraph.audio.SoundProducer;

/**
 * Created by me on 2/4/15.
 */
public class SineWave extends SoundProducer.Amplifiable {

    private final float freq;
    private double x;

    public SineWave(float freq) {
        this.freq = freq;
        x = 0;
    }


    @Override public boolean read(float[] buf, int readRate) {
        float dt = 1.0f / readRate;
        float r = freq * (float)(Math.PI * 2);
        float A = amp();
        double X = x, XX = X;
        for (int i = 0; i < buf.length;) {
            XX = X + i * dt;
            buf[i++] = (float)Math.sin(XX * r) * A;
        }
        x = XX;
        return true;
    }

    @Override
    public void skip(int samplesToSkip, int readRate) {
        x += ((double)samplesToSkip) / readRate;
    }



}