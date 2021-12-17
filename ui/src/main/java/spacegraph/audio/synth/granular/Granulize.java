package spacegraph.audio.synth.granular;

import jcog.signal.MutableFloat;
import jcog.signal.NumberX;
import spacegraph.audio.SoundProducer;
import spacegraph.audio.sample.SoundSample;

import java.util.Random;

public class Granulize extends SoundProducer.Amplifiable {

	final Granulator grains;
	private final float[] sourceBuffer;
    private double now;


    /** this actually represents the target amplitude which the current amplitude will continuously interpolate towards */
	public final NumberX stretchFactor = new MutableFloat(1.0f);
	public final NumberX pitchFactor = new MutableFloat(1.0f);

    private float currentAmplitude = amp.floatValue();


    /** grains are represented as a triple of long integers (see Granulator.createGrain() which constructs these) */
	private long[] currentGrain;
	private long[] fadingGrain;

	private final int playOffset;

    public Granulize(SoundSample s, float grainSizeSecs, float windowSizeFactor, Random rng) {
        this(s.buf, s.rate, grainSizeSecs, windowSizeFactor, rng);
    }

	public Granulize(float[] buffer, float sampleRate, float grainSizeSecs, float windowSizeFactor, Random rng) {
		grains = new Granulator(buffer, sampleRate, grainSizeSecs, windowSizeFactor);

		sourceBuffer = buffer;

        playOffset = Math.abs(rng.nextInt());
	}


	@Override
	public String toString() {
		return "Granulize{" +
				", now=" + now +
				", amplitude=" + amp() +
				", stretchFactor=" + stretchFactor +
				", pitchFactor=" + pitchFactor +
				", playOffset=" + playOffset +
				'}';
	}


	private long[] nextGrain(long[] targetGrain) {
        return grains.nextGrain(targetGrain, calculateCurrentBufferIndex(), now);
	}

	private int calculateCurrentBufferIndex() {

		return (int) (Math.abs(playOffset + Math.round(now / stretchFactor.floatValue()))) % sourceBuffer.length;
	}

	public Granulize setStretchFactor(float stretchFactor) {
		this.stretchFactor.set(stretchFactor);
        return this;
	}

    @Override
    public boolean read(float[] buf, int readRate) {

		if (currentGrain == null) {
			currentGrain = nextGrain(null);
		}

		float amp = currentAmplitude;
		float dAmp = (amp() - amp) / buf.length;


//		if (!p)
//			dAmp = (0 - amp) / buf.length;

		long samples = buf.length;

		long[] cGrain = currentGrain;
		long[] fGrain = fadingGrain;
		double dNow = pitchFactor.floatValue();

		for (int i = 0; i < samples; i++ ) {
			double now = this.now;
            float nextSample;
			if (cGrain != null) {
				nextSample = grains.sample(cGrain, now);
				if (Granulator.isFading(cGrain, now)) {
					fGrain = cGrain;
					cGrain = nextGrain(cGrain);
				}
			} else
				nextSample = 0;

			if (fGrain != null) {
                nextSample += grains.sample(fGrain, now);
				if (!grains.hasMoreSamples(fGrain, now))
					fGrain = null;
			}

            buf[i] = nextSample * amp;
            amp += dAmp;
			this.now += dNow;
		}


		currentGrain = cGrain;
		fadingGrain = fGrain;
		currentAmplitude = amp;

		return true;
	}

    @Override
    public void skip(int samplesToSkip, int readRate) {
        now += pitchFactor.floatValue() * samplesToSkip;// / readRate;
    }


	public final Granulize setPitchFactor(float v) {
		pitchFactor.set(v);
		return this;
	}
}