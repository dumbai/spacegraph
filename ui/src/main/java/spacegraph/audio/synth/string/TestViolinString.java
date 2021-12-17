package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 02-May-16.
 */

public class TestViolinString extends ViolinString {

    public TestViolinString(double frequency) {
        super(frequency / 2);
    }

    public void tic() {
        super.tic();
        super.tic();
    }
}
