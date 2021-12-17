package spacegraph.audio.synth.granular;

import spacegraph.audio.synth.granular.envelope.GrainEnvelope;
import spacegraph.audio.synth.granular.envelope.NullEnvelope;

class Granulator {

	private final float[] sourceBuffer;
	private final int grainSizeSamples;
	private final GrainEnvelope window;


	Granulator(float[] sourceBuffer, float sampleRate,
               float grainSizeSecs, float windowSizeFactor) {
		this.sourceBuffer = sourceBuffer;
		grainSizeSamples = Math.round(sampleRate * grainSizeSecs);

		int windowSamples = Math.round(grainSizeSamples * windowSizeFactor);
		window =
//				new HannEnvelope(windowSamples);
				new NullEnvelope(windowSamples);
		
	}

	boolean hasMoreSamples(long[] grain, double now) {
		long length = grain[1];
		long showTime = grain[2];
		return now < showTime + length;
	}

	float sample(long[] grain, double now) {

		float[] b = sourceBuffer;

		long showTime = grain[2];
		double offset = now - showTime;

		int n = b.length;
		int i = (int) Math.round(grain[0] + offset);
		//if (i < 0) i = -i;
		while (i < 0)
			i += n;
		return b[i % n] * window.getFactor((int) Math.round(offset));
	}

	static boolean isFading(long[] grain, double now) {
		long length = grain[1];
		long showTime = grain[2];
		return now >= showTime + length;
	}

	long[] nextGrain(long[] grain, int startIndex, double fadeInTime) {
		if (grain == null)
			grain = new long[3];
		int ws = window.getSize();
		grain[0] = (startIndex + ws) % sourceBuffer.length;
		grain[1] = grainSizeSamples;
		grain[2] = Math.round(fadeInTime + ws);
		return grain;
	}

}