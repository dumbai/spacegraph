package spacegraph.audio.speech;

import jcog.io.Serials;
import jcog.random.XoRoShiRo128PlusRandom;
import org.eclipse.collections.api.map.primitive.ImmutableByteObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.ByteObjectHashMap;
import spacegraph.audio.Audio;
import spacegraph.audio.sample.SamplePlayer;
import spacegraph.audio.sample.SoundSample;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Java port of tss.js -- Tiny Speech Synthesizer in JavaScript
 * <p>
 * Original code: stan_1901 (Andrey Stephanov)
 * http://pouet.net/prod.php?which=50530
 * <p>
 * JavaScript port: losso/code red (Alexander Grupe)
 * http://heckmeck.de/demoscene/tiny-speech-synth-js/
 *
 * see:
 *  https://github.com/formant/audio-formant
 *  https://github.com/chdh/klatt-syn
 */
public class TinySpeech {

    public static final int SAMPLE_FREQUENCY = 44100;
    private static final float PI = (float) Math.PI;
    private static final float PI_2 = 2 * (float) Math.PI;
    private final Random rng = new XoRoShiRo128PlusRandom(1);

    float f0 = 120;
    float period = 1.2f;

    private static float sawtooth(float x) {
        return 0.5f - (x - (int)(x / PI_2) * PI_2) / PI_2;
    }

    static class Phoneme {

        final float amp;
        final float len;
        final int[] f;
        final int[] w;
        final boolean osc;
        final boolean plosive;

        Phoneme(Map x) {
            this.amp = (int) x.get("amp");
            this.len = (int) x.get("len");
            this.osc = ((int) x.get("osc")) == 1;
            this.plosive = ((int) x.get("plosive")) == 1;

            this.f = new int[3]; this.w = new int[3];
            List<Integer> F = (List<Integer>) x.get("f");
            List<Integer> W = (List<Integer>) x.get("w");
            for (int i  = 0; i < 3; i++) {
                f[i] = F.get(i); w[i] = W.get(i);
            }
        }
    }

    /** HACK */
    @Deprecated static final Map g_phonemes_json;
    @Deprecated static final ImmutableByteObjectMap<Phoneme> g_phonemes;

    static {
        Map g;
        try {
            g = Serials.jsonMapper.readValue(
                    "{           o: { f:[12,  15,  0], w:[ 10,  10,  0], len:3, amp: 6, osc:0, plosive:0 },\n" +
                            "            i: { f:[ 5,  56,  0], w:[ 10,  10,  0], len:3, amp: 3, osc:0, plosive:0 },\n" +
                            "            j: { f:[ 5,  56,  0], w:[ 10,  10,  0], len:1, amp: 3, osc:0, plosive:0 },\n" +
                            "            u: { f:[ 5,  14,  0], w:[ 10,  10,  0], len:3, amp: 3, osc:0, plosive:0 },\n" +
                            "            a: { f:[18,  30,  0], w:[ 10,  10,  0], len:3, amp:15, osc:0, plosive:0 },\n" +
                            "            e: { f:[14,  50,  0], w:[ 10,  10,  0], len:3, amp:15, osc:0, plosive:0 },\n" +
                            "            E: { f:[20,  40,  0], w:[ 10,  10,  0], len:3, amp:12, osc:0, plosive:0 },\n" +
                            "            w: { f:[ 3,  14,  0], w:[ 10,  10,  0], len:3, amp: 1, osc:0, plosive:0 },\n" +
                            "            v: { f:[ 2,  20,  0], w:[ 20,  10,  0], len:3, amp: 3, osc:0, plosive:0 },\n" +
                            "            T: { f:[ 2,  20,  0], w:[ 40,   1,  0], len:3, amp: 5, osc:0, plosive:0 },\n" +
                            "            z: { f:[ 5,  28, 80], w:[ 10,   5, 10], len:3, amp: 3, osc:0, plosive:0 },\n" +
                            "            Z: { f:[ 4,  30, 60], w:[ 50,   1,  5], len:3, amp: 5, osc:0, plosive:0 },\n" +
                            "            b: { f:[ 4,   0,  0], w:[ 10,   0,  0], len:1, amp: 2, osc:0, plosive:0 },\n" +
                            "            d: { f:[ 4,  40, 80], w:[ 10,  10, 10], len:1, amp: 2, osc:0, plosive:0 },\n" +
                            "            m: { f:[ 4,  20,  0], w:[ 10,  10,  0], len:3, amp: 2, osc:0, plosive:0 },\n" +
                            "            n: { f:[ 4,  40,  0], w:[ 10,  10,  0], len:3, amp: 2, osc:0, plosive:0 },\n" +
                            "            r: { f:[ 3,  10, 20], w:[ 30,   8,  1], len:3, amp: 3, osc:0, plosive:0 },\n" +
                            "            l: { f:[ 8,  20,  0], w:[ 10,  10,  0], len:3, amp: 5, osc:0, plosive:0 },\n" +
                            "            g: { f:[ 2,  10, 26], w:[ 15,   5,  2], len:2, amp: 1, osc:0, plosive:0 },\n" +
                            "            f: { f:[ 8,  20, 34], w:[ 10,  10, 10], len:3, amp: 4, osc:1, plosive:0 },\n" +
                            "            h: { f:[22,  26, 32], w:[ 30,  10, 30], len:1, amp:10, osc:1, plosive:0 },\n" +
                            "            s: { f:[80, 110,  0], w:[ 80,  40,  0], len:3, amp: 5, osc:1, plosive:0 },\n" +
                            "            S: { f:[20,  30,  0], w:[100, 100,  0], len:3, amp:10, osc:1, plosive:0 },\n" +
                            "            p: { f:[ 4,  10, 20], w:[  5,  10, 10], len:1, amp: 2, osc:1, plosive:1 },\n" +
                            "            t: { f:[ 4,  20, 40], w:[ 10,  20,  5], len:1, amp: 3, osc:1, plosive:1 },\n" +
                            "            k: { f:[20,  80,  0], w:[ 10,  10,  0], len:1, amp: 3, osc:1, plosive:1 } }\n"
                    , Map.class);
        } catch (IOException e) {
            g = null;
        }
        g_phonemes_json = g;

        ByteObjectHashMap gg = new ByteObjectHashMap(g_phonemes_json.size());
        g_phonemes_json.forEach((c, p)-> gg.put((byte) ((String)c).charAt(0), new Phoneme((Map)p)));
        g_phonemes = gg.toImmutable();
    }


//    public float[] _say(String text) {
//        throw new TODO();
//        //return _say(text, 80, 1f);
//    }

    private SoundSample _say(String text) {
        float[] buf = new float[10 * SAMPLE_FREQUENCY];
        int bufPos = 0;
        float amp = 0.01f;
        return say(text, amp, f0, period, buf, bufPos);
    }

    // Synthesizes speech and adds it to specified buffer
    private SoundSample say(String text, float amp, float f0, float speed, float[] buf, int bufPos) {


        int tlen = text.length();
        for (int textPos = 0; textPos < tlen; textPos++)
            bufPos = say(text.charAt(textPos), f0, speed, buf, bufPos, amp);

//        for (int i = 0; i < bufPos; i++)
//            buf[i] /= Short.MAX_VALUE;

        return new SoundSample(buf, 0, bufPos, SAMPLE_FREQUENCY);
    }

    private int say(char c, float f0, float speed, float[] buf, int bufPos, float amp) {

        if (c == ' ' || c == '\n') {
            bufPos += (int)(SAMPLE_FREQUENCY * 0.2f * speed); //skip
        }

        Phoneme p = g_phonemes.get((byte) c);
        if (p==null)
            return bufPos;

        float v = p.amp * amp;
        int sl = Math.round(speed * p.len * SAMPLE_FREQUENCY / 15);
        int[] pf = p.f;
        int[] pw = p.w;
        boolean osc = p.osc;
        for (int formant = 0; formant < 3; formant++) {
            float ff = pf[formant];
            if (ff == 0)
                continue;

            float freq = ff * 50.0f / SAMPLE_FREQUENCY;

            float xx = 0, xxx = 0;
            float q =
                1.0f - pw[formant] * (PI * 10/ SAMPLE_FREQUENCY); //adjust 10 to tune resonance
                //(float) (1 / (pw[formant] * tan(2 * PI * 0.5)));

            int pos = bufPos;
            float xp = 0;

            for (int s = 0; s < sl; s++) {
                float x;

                if (!osc) {
                    xp = 0;
                    x = resonator(f0, s);
                } else {
                    x = noise();
                }

                // Apply formant filter
                x = (float) ((x + (2 * Math.cos(PI_2 * freq) * xx * q)) - (xxx * q * q));
                xxx = xx;
                xx = x;
                x = x * v + xp;
                xp = x;

                // envelope
                double e = Math.sin((PI * s) / sl); //sine  /-\

                x *= e;



                x *= amp;

                // Mix
                //buf[pos] = buf[pos]/2 + x;
                buf[pos] += x;

                pos++;
            }
        }

        float overlap =
            //0.5f;
            0.25f;
            //0.1f;
            //0;

        bufPos += Math.round( (1-overlap)*sl + (p.plosive ? (sl & 0xfffffe) : 0));
        return bufPos;
    }

    private static float resonator(float f0, int s) {
        float x = sawtooth(s * (f0 * PI_2 / SAMPLE_FREQUENCY));
        return x;
    }

    private float noise() {
        float x = (rng.nextFloat() - 0.5f);
        return x;
    }

    private SamplePlayer saySample(String text) {
        return new SamplePlayer(_say(text));
    }

    public void say(String text) {
        Audio.the().play( saySample(text  ) );
    }

    public static void main(String[] args) throws IOException {
        Audio.the().play(new TinySpeech().saySample("hello hello hello this is a spEIE ch test"));
        System.in.read();
    }
}