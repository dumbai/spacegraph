package spacegraph.space2d.meta;

import org.eclipse.collections.api.block.procedure.primitive.BooleanProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spacegraph.audio.Audio;
import spacegraph.audio.Sound;
import spacegraph.audio.SoundProducer;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.button.CheckBox;

public abstract class SonificationPanel extends Gridding {

	static final Logger logger = LoggerFactory.getLogger(SonificationPanel.class);

	final CheckBox sonifyButton = new CheckBox("Sonify");
	private Sound<SoundProducer> sound;
	int framesRead;

	final SoundProducer soundProducer = (buf, readRate) -> {
		if (SonificationPanel.this.parent == null) {
			stopAudio();
			return false;
		} else {
			sound(buf, readRate);

			framesRead++;
			sonifyButton.color.set(0, 0.2f + 0.05f * (framesRead/4 % 8), 0, 0.75f);
			return true;
		}
	};

	public SonificationPanel() {
		super();

		set(sonifyButton);

		sonifyButton.on((BooleanProcedure) play -> {
			synchronized (SonificationPanel.this) {
				if (play) startAudio();
				else stopAudio();
			}
		});

		stopAudio();
	}

	protected abstract void sound(float[] buf, float readRate);

	protected void startAudio() {
		sonifyButton.color.set(0, 0.25f, 0, 0.75f);
		sound = Audio.the().play(soundProducer);
		logger.info("{} sonify START {}", this, sound);
	}

	protected void stopAudio() {
		sonifyButton.color.set(0.1f, 0.0f, 0, 0.75f);
		if (sound != null) {
			sound.stop();
			logger.info("{} sonify STOP {}", this, sound);
			sound = null;
		}
	}

}