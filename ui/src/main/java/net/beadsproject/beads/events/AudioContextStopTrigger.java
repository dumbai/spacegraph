/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.events;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Auvent;

/**
 * Use AudioContextStopTrigger to cause the {@link AudioContext} to stop in response to a given event.
 * <p>
 * <p/>For example, to cause the {@link AudioContext} to stop when a sample has finished playing:
 * <code>
 * <br/>    AudioContext context = new AudioContext();
 * <br/>    SamplePlayer samplePlayer = new SamplePlayer(SampleManager.sample(pathToAudioFile));
 * <br/>    context.out.addInput(samplePlayer);
 * <br/>    samplePlayer.setKillListener(new AudioContextStopTrigger(context));
 * <br/>    context.start();
 * </code>
 */
public class AudioContextStopTrigger extends Auvent {

    /**
     * The AudioContext.
     */
	private final AudioContext ac;

    /**
     * Creates a new audio context stop trigger.
     *
     * @param ac the AudioContext.
     */
    public AudioContextStopTrigger(AudioContext ac) {
        this.ac = ac;
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.Bead#message(com.olliebown.beads.core.Bead)
     */
    @Override
    public void on(Auvent message) {
        stop();
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.Bead#stop()
     */
    @Override
    public void stop() {
        ac.stop();
    }

}
