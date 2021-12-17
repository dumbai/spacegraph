package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 14-Mar-16.
 */
public class SquareDiffDecayWave extends SquareWave {

    public SquareDiffDecayWave(double frequency) {
        super(frequency);
        setDeltaVolume(calcDecay(frequency));
    }

    private static double calcDecay(double frequency) {
        double factor = 440 / frequency;
        return Math.pow(0.996, factor);
    }
}