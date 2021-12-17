package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 24-Feb-16.
 */
public abstract class VariableDeltaString extends KarplusStrongString {

    public int getTics() {
        return tics;
    }

    public void setTics(int tics) {
        this.tics = tics;
    }




    private int tics;
    private double initialVolume;

    public VariableDeltaString(double frequency) {
        super(frequency);
        tics = 0;
    }

    public VariableDeltaString(double frequency, int tuned) {
        super(frequency, tuned);
        tics = 0;
    }

    public abstract void calcDelta();

    public void resetTics() {
        tics = 0;
    }

    public void oneTic() {
        tics++;
    }
}
