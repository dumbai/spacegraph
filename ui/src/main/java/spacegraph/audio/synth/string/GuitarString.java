package spacegraph.audio.synth.string;

public class GuitarString extends KarplusStrongString {

//    private final double lowPass = 0.5;
    private final double pluckDelta;
    private final double releaseDelta;
    private double filterIn;
    private double filterOut;

    double[] seedNoise;

    public GuitarString(double frequency) {
        super(frequency, 1);

        this.seedNoise = new double[buffer.capacity()];
        for (int i = 0; i < seedNoise.length; i++)
            seedNoise[i] = -1 + 2*Math.random();




        setMaxVolume(0.7);
        pluckDelta =
                //.996;
                0.9999f;
        releaseDelta =
                //.9;
                0.999f;
        filterIn = 0;
        filterOut = 0;
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        setDeltaVolume(pluckDelta);
        clear();
        int c = buffer.capacity();
        for (int i = 0; i < c; i++) {
            buffer.enqueue(
                    (Math.random() - 0.5) * 2
                    //rng.nextGaussian()/2
                    //Math.tanh(rng.nextGaussian())
                    * getMaxVolume());
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm. 
     */
    public void tic() {
        double first = buffer.dequeue();
        double second = buffer.peek();
        double x = (first + second) / 2; // lowpass filter
        filterOut = C * x + filterIn - C * filterOut; // allpass tuning filter
        filterIn = x;
        buffer.enqueue(filterOut * deltaVolume);
    }

//    /** https://github.com/mrahtz/javascript-karplus-strong/blob/master/karplus-strong/guitarstring.js */
//    void resonate() {
//
//
//
//    // explicitly initialise all variables so types are declared
//    float r00 = 0.0;
//    var f00 = 0.0;
//    var r10 = 0.0;
//    var f10 = 0.0;
//    var f0 = 0.0;
//    var c0 = 0.0;
//    var c1 = 0.0;
//    var r0 = 0.0;
//    var r1 = 0.0;
//    var resonatedSample = 0.0;
//    var resonatedSamplePostHighPass = 0.0;
//    // by making the smoothing factor large, we make the cutoff
//    // frequency very low, acting as just an offset remover
//    var highPassSmoothingFactor = 0.999;
//
//
//    // +x indicates that x is a double
//    // (asm.js Math functions take doubles as arguments)
//    c0 = 2.0 * sin(Math.PI * 3.4375 / 44100.0);
//    c1 = 2.0 * sin(Math.PI * 6.124928687214833 / 44100.0);
//    r0 = 0.98;
//    r1 = 0.98;
//
//    // asm.js seems to require byte addressing of the heap...?
//    // http://asmjs.org/spec/latest/#validateheapaccess-e
//    // yeah, when accessing the heap with an index which is an expression,
//    // the total index expression is validated in a way that
//    // forces the index to be a byte
//    // and apparently '|0' coerces to signed when not in the context
//    // of parameters
//    // http://asmjs.org/spec/latest/#binary-operators
//    double lastOutput = 0.0;
//    double lastInput = 0.0;
////        int c = buffer.capacity();
//    for (int i = 0; i < 2; i++) {
//        r00 = r00 * r0;
//        r00 = r00 + (f0 - f00) * c0;
//        f00 = f00 + r00;
//        f00 = f00 - f00 * f00 * f00 * 0.166666666666666;
//        r10 = r10 * r1;
//        r10 = r10 + (f0 - f10) * c1;
//        f10 = f10 + r10;
//        f10 = f10 - f10 * f10 * f10 * 0.166666666666666;
//        f0 = buffer.get(i); // +heap[i >> 2];
//        resonatedSample = f0 + (f00 + f10) * 2.0;
//
//        // I'm not sure why, but the resonating process plays
//        // havok with the DC offset - it jumps around everywhere.
//        // We put it back to zero DC offset by adding a high-pass
//        // filter with a super low cutoff frequency.
//        resonatedSamplePostHighPass = +highPass(
//                lastOutput,
//                lastInput,
//                resonatedSample,
//                highPassSmoothingFactor
//        );
//        buffer.setAt(i,resonatedSamplePostHighPass);
////        System.out.println(f0 + " " + resonatedSample + " " + resonatedSamplePostHighPass);
//        lastInput = resonatedSample;
//        lastOutput = resonatedSamplePostHighPass;
//
//
//    }
//
//}
//    // simple discrete-time high-pass filter from Wikipedia
//    static double highPass(double lastOutput, double lastInput, double currentInput, double smoothingFactor) {
//        lastOutput = +lastOutput;
//        lastInput = +lastInput;
//        currentInput = +currentInput;
//        smoothingFactor = +smoothingFactor;
//
//        var currentOutput = 0.0;
//        currentOutput =
//                smoothingFactor * lastOutput +
//                        smoothingFactor * (currentInput - lastInput);
//
//        return +currentOutput;
//    }
//
//    // simple discrete-time low-pass filter from Wikipedia
//    static double lowPass(double lastOutput, double currentInput, double smoothingFactor) {
//
//        var currentOutput = 0.0;
//        currentOutput =
//                smoothingFactor * currentInput +
//                        (1.0 - smoothingFactor) * lastOutput;
//
//        return +currentOutput;
//    }

    public void release() {
        setDeltaVolume(releaseDelta);
    }

//    double v0;
//    @Override
//    public double sample() {
//        //double v = lowPass(v0, super.sample(), lowPass);
//        //resonate();
//        double v = super.sample();
//        return v0 = v;
//    }
}
