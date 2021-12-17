package spacegraph.audio.synth.granular.envelope;

/** https://en.wikipedia.org/wiki/Hann_function */
public class HannEnvelope implements GrainEnvelope {

	private final float[] factors;

	@Override
	public final int getSize() {
		return factors.length;
	}

	@Override
	public final float getFactor(int offset) {
		int index = offset;
		if (offset < 0) { 
			index = -offset;
		}
		return index < factors.length ? factors[index] : 0;
	}


	public HannEnvelope(int size) {
		factors = buildTable(size);
	}

	private static float[] buildTable(int size) {
		float[] result = new float[size];
		for(int i = 0; i < size; i++) {
			result[i] = (float) (jcog.Util.fma(0.5, Math.cos((i / (double)size) * Math.PI), 0.5));
		}
		return result;
	}

}