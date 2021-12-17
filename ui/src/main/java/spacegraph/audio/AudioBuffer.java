package spacegraph.audio;

import jcog.signal.tensor.ArrayTensor;

/** mutable audio buffer for use in mix-down procedures */
public class AudioBuffer extends ArrayTensor {

    public final int sampleRate;
    private final long startNS;
    //TODO long timestamp;

    public AudioBuffer(float[] data, int sampleRate) {
        super(data);
        this.startNS = System.nanoTime();
        this.sampleRate = sampleRate;
    }
}
