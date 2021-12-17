//package spacegraph.audio;
//
//import jcog.Util;
//import jcog.signal.buffer.CircularBuffer;
//
///**
// * Created by me on 10/28/15.
// */
//public class SineSource implements WaveSource {
//
//    private final int SAMPLE_RATE = 44100;
//
//    private final double freq;
//    private int samples;
//    private float t;
//
//    public SineSource(double freq) {
//        this.freq = freq;
//
//        samples = Util.largestPowerOf2NoGreaterThan((int) Math.ceil(freq * 2));
//    }
//
//    @Override
//    public int start() {
//        return samples;
//    }
//
//    @Override
//    public void stop() {
//
//    }
//
//    @Override
//    public int next(CircularBuffer buffer) {
//        float t = this.t;
//        float dt = buffer.length / SAMPLE_RATE  / (float)(Math.PI*2);
//        double f = freq;
//        int num = buffer.length;
//        for (int i = 0; i < num; i++) {
//            buffer[i] = (float) FastMath.sin(f * t);
//            t += dt;
//        }
//        this.t = t;
//        return 0;
//    }
//
//    @Override
//    public int samplesPerSecond() {
//        return SAMPLE_RATE;
//    }
//}
