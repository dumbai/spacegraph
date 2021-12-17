package spacegraph.audio.sample;

public class SoundSample {
	public final float[] buf;
	public final float rate;

	/** range within the buffer to actually play */
	public final int start;
    public final int end;

	public SoundSample(float[] buf, float rate) {
		this(buf, 0, buf.length, rate);
	}

	public SoundSample(float[] buf, int start, int end, float rate) {
		this.buf = buf;
		this.rate = rate;
		this.start = start; this.end = end;
	}
}