package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 14-Mar-16.
 */

public class ViolinDiffDecayString extends ViolinString {

    private final double attackDelta;
    private final double decayDelta;
    private final double frequency;

    public ViolinDiffDecayString(double frequency) {
        super(frequency);
        this.frequency = frequency;
        this.attackDelta = attackDelta();
        this.decayDelta = decayDelta();
        setVolume();
    }

    private void setVolume() {
        if (frequency > 440) {
            double factor = Math.log(frequency / 440) / Math.log(2);
            setMaxVolume(0.2 + (0.1 * factor));
        }
    }

    @Override
    public void calcDelta() {
        if (getTics() < 10000) {
            setDeltaVolume(attackDelta);
        } else if (getTics() < 20000) {
            setDeltaVolume(1.02);
        } else if (getTics() < 80000) {
            setDeltaVolume(1);
        } else {
            setDeltaVolume(decayDelta);
        }
    }

    private double attackDelta() {
        double factor = 440 / frequency;
        return Math.pow(1.2, factor);
    }

    private double decayDelta() {
        double factor = 440 / frequency;
        return Math.pow(0.99, factor);
    }
}
