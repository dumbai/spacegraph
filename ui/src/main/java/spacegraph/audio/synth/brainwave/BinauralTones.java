package spacegraph.audio.synth.brainwave;

import spacegraph.audio.SoundProducer;

/**
 * Binaural Beats for Human Brainwave Entrainment and Hemispheric Synchronization
 * http:
 *
 * TODO make this a set of 2 SoundProducer's, set at fixed ambient Left/Right positions
 */
public class BinauralTones implements SoundProducer {

    private final float carrier;
    private final float beat;
    private float x;

    public BinauralTones(float initialBeat, float initialCarrier) {
        beat = initialBeat;
        carrier = initialCarrier;
        x = 0;
    }

    @Override public boolean read(float[] buf, int readRate) {
        float dt = 1.0f / readRate;

        float leftRate = (carrier - (beat / 2.0f)) * (float)(Math.PI* 2.0f);
        float rigtRate = (carrier + (beat / 2.0f)) * (float)(Math.PI* 2.0f);
        for (int i = 0; i < buf.length-1; /*stereo*/) {
            buf[i++] = (float)Math.sin( x * leftRate );
            buf[i++] = (float)Math.sin( x * rigtRate );
            x += dt;
        }

        return true;
    }

    @Override
    public void skip(int samplesToSkip, int readRate) {
        float dt = 1.0f / readRate;
        x += dt * samplesToSkip;
    }

}
