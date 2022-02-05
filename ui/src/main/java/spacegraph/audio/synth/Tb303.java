package spacegraph.audio.synth;

import com.jogamp.newt.event.KeyEvent;
import jcog.Is;
import jcog.signal.FloatRange;
import spacegraph.audio.Audio;
import spacegraph.audio.SoundProducer;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.meta.ObjectSurface;
import spacegraph.space2d.widget.Widget;

import static java.lang.Math.*;
import static spacegraph.SpaceGraph.window;

/**
 * 303-like acid synth
 * translated from: https://github.com/sigflup/tb303-simulator-/
 * <p>
 * http://www.firstpr.com.au/rwi/dfish/303-unique.html
 * https://github.com/andrewcb/toy303/blob/master/A303.cc
 */
@Is("Roland_TB-303")
public class Tb303 implements SoundProducer {


//	public final FloatRange vca_attak = new FloatRange(0.5f, 0, 1);
	public final FloatRange vca_decay = new FloatRange(0.5f, 0, 1);
	public final FloatRange vcf_decay = new FloatRange(0.5f, 0, 1);
	public final FloatRange vcf_cutoff = new FloatRange(0.5f, 0, 1);
	public final FloatRange vcf_envmod = new FloatRange(0.5f, 0, 2);
	public final FloatRange vcf_reso = new FloatRange(0.5f, 0.1f, 2);

	double dt;
	//double t;
	long t;

	//TODO double tNoteOn, tNoteOff;


	float vcf_rescoeff;
	float vcf_e0, vcf_e1;
	float vcf_c0;
	float sp, spp;
	float vcf_a, vcf_b, vcf_c;

	float vca_a;

	public Tb303() {
		reset();
	}

	public static void main(String[] args) {
		Tb303 t = new Tb303();


		window(new Gridding(

			new ObjectSurface(t),

			new PianoKeys() {
				@Override
				protected void play(int note) {
					t.note(note, 0.75f);
				}

				@Override
				protected void release() {
					t.cut();
				}
			}

		), 800, 600);

		Audio.the().play(t);

//		System.in.read();
	}

	public void reset() {
		dt = 0;
		t = 0;
		sp = spp = 0.9f;

		vcf_b = vcf_c0 = vcf_e0 = vcf_e1 = vcf_a = 0.0f;

		cut();
	}
	public void note(int note, float amp) {

		dt = (float) ((440.0 / 44100.0) * pow(2, (note - 57) / 12.0)); //reset oscillator
		vcf_c0 = vcf_e1; //reset filter
		vca_a = amp; //reset amplitude
//		vcf_envpos = ENVINC;
	}
	public void cut() {
		//vca_a = 0;
		//vca_mode = 2;
	}

//	final static int  NOTE = (1);
//	final static int  A  =  (1<<1);
//	final static int  S  =  (1<<2);
//	final static int  CUT = (1<<3);
//	final static int  RES = (1<<4);
//	final static int  ENV = (1<<5);
//	final static int  DEC = (1<<6);
//	final static int  ACC = (1<<7);

	@Override
	public boolean read(float[] y, int samplerate) {
		updateFilter();

//		float vca_attack = this.vca_attak.floatValue()/samplerate;
		float vca_decay = 1 - this.vca_decay.floatValue()/samplerate;
		float vcf_decay = 1 - this.vcf_decay.floatValue()/samplerate;

		double dt = this.dt;
		int i;

		int len = y.length;
		for (i = 0; i < len; i++) {

			//update filter envelope
			double w = vcf_e0 + vcf_c0;
			vcf_c0 *= vcf_decay;

			double k = (float) exp(-w / vcf_rescoeff);
			vcf_a = (float) (2 * cos(2 * w) * k);
			vcf_b = (float) (-k * k);
			vcf_c = 1.0f - vcf_a - vcf_b;


			double vco = oscillator((t+i)*dt);

			//amplifier envelope TODO better
//			if (i == len / 2) vca_mode = 2;

//			if (vca_mode > 0) {
				//attack phase
				//vca_a += (vca_a0 - vca_a) * vca_attack;
//			}
//			if (vca_mode == 1) {
				//decay phase
				vca_a *= vca_decay;
				//if(vca_a < (1/65536.0f)) { vca_a = 0; vca_mode = 2; }
//			}

			float s = (float)(
				vcf_a * sp +
				vcf_b * spp +
				vca_a * vcf_c * vco);

			spp = sp;
			sp = y[i] = s;

		}

		//t += dt * len;
		t += len;

		return true;
	}

	private double oscillator(double t) {
		return Math.sin(t *2*Math.PI) > 0 ? +1 : -1;
		//return (float) Math.sin(t * 2 * Math.PI);
	}

//	static class tb303_event {
//		int note, a, s;
//		float cut, res, env, dec, acc;
//	}

	private void updateFilter() {
		double vcf_reso = this.vcf_reso.doubleValue();

		vcf_rescoeff = (float) exp(-1.20 + 3.455 * vcf_reso);

//		float d = vcf_decay.floatValue();
//		d = 0.2f + (2.3f * d);
//		d *= readRate;
//		vcf_envdecay = (float) pow(0.1, 1.0 / d * ENVINC);

		double r = Math.PI / 44100.0f;
		float vcf_envmod = this.vcf_envmod.floatValue();
		float vcf_cutoff = this.vcf_cutoff.floatValue();
		vcf_e0 = (float) (exp(5.613 - 0.8 * vcf_envmod +
			2.1553 * vcf_cutoff - 0.7696 * (1 - vcf_reso)) * r);
		vcf_e1 = (float) (exp(6.109 + 1.5876 * vcf_envmod +
			2.1553 * vcf_cutoff - 1.2 * (1 - vcf_reso)) * r);

		vcf_e1 -= vcf_e0;
	}



	private abstract static class PianoKeys extends Widget {
		int base =
			//44;
			//22;
			4;

		@Override
		public boolean key(KeyEvent e, boolean pressedOrReleased) {
			if (!pressedOrReleased) {
				release();
			} else {
				char c = e.getKeyChar();
				if ((c >= 'a') && (c <= 'z'))
					play(base + (c - 'a'));
			}
			return super.key(e, pressedOrReleased);
		}

		protected abstract void play(int note);

		protected abstract void release();
	}
}