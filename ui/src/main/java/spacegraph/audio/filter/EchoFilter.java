package spacegraph.audio.filter;

public class EchoFilter {

    private final double volumeFraction;
    private final int frameOffset;
    private final double[] previousFrames;
    private int frameIndex = 0;

    public EchoFilter(double volumeFraction, double offsetMillis, int sampleRate) {
        this(volumeFraction, (int) (offsetMillis * sampleRate / 1000.0));
    }

    public EchoFilter(double volumeFraction, int frameOffset) {
        this.volumeFraction = volumeFraction;
        this.frameOffset = frameOffset;
        this.previousFrames = new double[frameOffset];
    }

    public double apply(double input) {
        double output = previousFrames[frameIndex];
        previousFrames[frameIndex] = input * volumeFraction;
        frameIndex = (frameIndex + 1) % frameOffset;
        return output;
    }
}