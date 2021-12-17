package spacegraph.audio.synth.granular.envelope;

/**
 * Created by me on 9/11/15.
 */
public class NullEnvelope implements GrainEnvelope {

    private final int samples;

    public NullEnvelope(int samples) {
        this.samples = samples;
    }

    @Override
    public int getSize() {
        return samples;
    }

    @Override
    public float getFactor(int offset) {
        return 1;
    }
}
