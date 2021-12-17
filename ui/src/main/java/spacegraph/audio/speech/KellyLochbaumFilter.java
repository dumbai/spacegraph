package spacegraph.audio.speech;

/** Kelly-Lochbaum filter. Follow sample code from
    http://people.ee.ethz.ch/~jniederh/VocSynth/
    @author Kees van den Doel (kvdoel@cs.ubc.ca)
	https://persianney.com/kvdoelcsubc/jass/doc/jass/generators/package-summary.html

	https://ccrma.stanford.edu/~jos/pasp/Singing_Kelly_Lochbaum_Vocal_Tract.html
*/

public class KellyLochbaumFilter /*implements Filter*/ {


	/**
   Interface defining a filter with one input and one output.
   @author Kees van den Doel (kvdoel@cs.ubc.ca)
*/
	/** Proces input.
	 @param output user provided buffer for returned result.
	 @param input user provided input buffer.
	 @param nsamples number of samples written to output buffer.
	 @param inputOffset where to start in circular buffer input.
	 */
	/*
	public interface Filter {

		public void filter(float [] output, float[] input, int nsamples, int inputOffset);
	}

	 */
    private static final double DEFAULT_dampingCoeff = 1;
    
    /** How much damping in system (1 == no damping)*/
    protected double dampingCoeff=DEFAULT_dampingCoeff;

    /** Sampling rate in Hertz. */
    protected float srate;

    /** State of filter. */
    protected double[] li;
    protected double[] lo;
    protected double[] gi;
    protected double[] go;

    /** This many cylinder segments */
    protected int nTubeSections;

    /** Radii of the segments */
    protected double[] cylRadius;

    /** Filter coefficients derived form cylinder radii */
    protected double[] kCoeff;
    
    /** Create and initialize.
        @param srate sampling rate in Hertz.
        @param nTubeSection number of sections
     */
    public KellyLochbaumFilter(float srate, int nTubeSections) {
        this.srate = srate;
        this.nTubeSections = nTubeSections;
        allocate();
        resetFilter();
        System.out.println("ns="+nTubeSections);
    }

    public KellyLochbaumFilter() {}

    private void allocate() {
		li=new double[nTubeSections+1]; 	//to lips input to reflection --(z-1)--li----lo--
		lo=new double[nTubeSections+1]; 	//to lips output of reflection          |refl|
		gi=new double[nTubeSections+1]; 	//to glottis input to reflection ------go----gi--
		go=new double[nTubeSections+1]; 	//to glottis output of reflection
        cylRadius = new double[nTubeSections+1];
        kCoeff = new double[nTubeSections+1]; //reflections coefficients
        for(int i=0;i<=nTubeSections;i++) {
            cylRadius[i] = 1;
            li[i]=lo[i]=gi[i]=go[i]=0;
            kCoeff[i]=0;
        }

        computeKCoeff();
    }

    /** Compute low level filter values from geometry */
    protected void computeKCoeff() {
        kCoeff[0]=1.0; //Zgl=0
        for(int i=1;i<nTubeSections;i++) {
            kCoeff[i] = (cylRadius[i]*cylRadius[i]-cylRadius[i-1]*
                         cylRadius[i-1])/(cylRadius[i]*cylRadius[i]+cylRadius[i-1]*cylRadius[i-1]);
        }
        kCoeff[nTubeSections]=1.0; //Zl=inf
    }

    /** Set an individual segment radius
        @param k index of segment (0,...)
        @param r radius to set
     */
    public void setCylinderRadius(int k,double r) {
        cylRadius[k]=r;
        computeKCoeff();
    }
    
    /** Set all radii
        @param array of r radii 
     */
    public void setAllCylinderRadii(double[] r) {
        if (nTubeSections >= 0) System.arraycopy(r, 0, cylRadius, 0, nTubeSections);
        computeKCoeff();
    }

    /** Set damping coeff. (1 == no damping)
        @param val damping coefficient
    */
    public void setDampingCoeff(double val) {
        dampingCoeff = val;
    }

    /** Clear filter of past history */
    public void resetFilter() {
        for(int i=0;i<=nTubeSections;i++) {
            li[i]=lo[i]=gi[i]=go[i]=0;
        }
    }

    /** Set the glottal reflection coeff. 
        @param val glottal reflection coefficient
    */
    public void setGlottalReflectionCoeff(double val) {
    }
    
    /** Proces input (may be same as output). Implements Filter interface
        @param output user provided buffer for returned result.
        @param input user provided input buffer.
        @param nsamples number of samples written to output buffer.
        @param inputOffset where to start in circular buffer input.
    */
    public void filter(float [] output, float[] input, int nsamples, int inputOffset) {
        int inputLen = input.length;
        int ii = inputOffset;
        for (int k=0;k<nsamples;k++) {
			//Input into system
			li[0]=input[k]/2.0;
			//Calculate all reflections
			for (int i=nTubeSections;i>=0;i--)
			{
		  		//to lips
		  		lo[i]=dampingCoeff*((1+kCoeff[i])*li[i]+kCoeff[i]*gi[i]);
		  		//to glottis
		  		go[i]=dampingCoeff*((1-kCoeff[i])*gi[i]-kCoeff[i]*li[i]);
		  		//To glottis without delay!
		  		if(i>1)
		  		{
		  			gi[i-1]=dampingCoeff*go[i];
		  		}
			}
			//calculate delays towards lips
			for (int i=0;i<nTubeSections;i++) {
				li[i+1]=dampingCoeff*lo[i];
			}
			//Lip output
			output[k]=(float)lo[nTubeSections];
		}
    }


	/**
	 Output glottal wave. See Rubin et al JASA vol 70 no 2 1981 p323
	 @author Kees van den Doel (kvdoel@cs.ubc.ca)
	 */

	public static class GlottalWave /*extends Out*/ {
        /** Sampling rate in Hertz of Out. */
		public float srate;

		/** Amplitude or volume*/
		protected float volume = 1;

		/** Current phase */
		protected float phase = 0;
		protected boolean odd = true; // flip to alternate sign

		/** Freq. in Hertz */
		protected float freq = 440;

		protected float openQuotient = 0.5f;

		protected float speedQuotient = 4.0f;

		private float T;
        private float Tp;
        private float Tn;

		public GlottalWave(float srate,int bufferSize) {
			//super(bufferSize);
            this.srate = srate;
			computePars();
		}

		/** Set amplitude
		 @param val Volume.
		 */
		public void setVolume(float val) {
			volume = val;
		}

		public float getVolume() {
			return volume;
		}

		/** Set frequency
		 @param f frequency.
		 */
		public void setFrequency(float f) {
			freq = f;
			computePars();
		}

		public float getFrequency() {
			return freq;
		}

		/** Set Speed Quotient
		 @param sq Speed Quotient
		 */
		public void setSpeedQuotient(float sq) {
			speedQuotient = sq;
			computePars();
		}

		public float getSpeedQuotient() {
			return speedQuotient;
		}

		/** Set Open Quotient
		 @param oq Open Quotient
		 */
		public void setOpenQuotient(float oq) {
			openQuotient = oq;
			computePars();
		}

		public float getOpenQuotient() {
			return openQuotient;
		}

		private void computePars() {
			T = 1/freq;
			Tn = T*openQuotient/(1+speedQuotient);
			Tp = speedQuotient * Tn;
		}

		protected void computeBuffer(float[] buf, int bufsz) {
			//int bufsz = getBufferSize();
			for(int i=0;i<bufsz;i++) {
				float y;
				if(phase > (Tn+Tp)) {
					y=0;
				} else if(phase<Tp) {
					float tmp = phase/Tp;
					y = (3-2*tmp)*tmp*tmp;
				} else {
					float tmp = (phase-Tp)/T;
					y = 1-tmp*tmp;
				}
				phase += 1/srate;
				if(phase > T) {
					phase -= T;
					odd = ! odd;
				}
				buf[i] = volume * y;
			}
		}

	}


}