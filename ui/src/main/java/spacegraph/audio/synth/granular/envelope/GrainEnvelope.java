package spacegraph.audio.synth.granular.envelope;

public interface GrainEnvelope {

	/** in samples */
	int getSize();

	/** amplitude factor */
	float getFactor(int offset);

}
