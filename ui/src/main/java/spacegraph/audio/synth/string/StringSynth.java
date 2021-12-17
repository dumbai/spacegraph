package spacegraph.audio.synth.string;


import jcog.Util;
import jcog.signal.buffer.CircularFloatBuffer;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class StringSynth {
    private static final double CONCERT_A = 440.0;
    private static final double base = indexToET(0, 33, 12); // 65.4064 = C2, 87.3071 = F2, 97.9989 = G2, 110 = A2
    //    private static final String keyboard = " `1qaz2wsx3edc4rfv5tgb6yhn7ujm8ik,9ol.0p;/-['=]\\~!QAZ@WSX#EDC$RFV%TGB^YHN&UJM*IK<(OL>)P:?_{\"+}|";
    private static final List<Integer> keycodes = Arrays.asList(192, 9, 20, 16, 49, 81, 65, 90, 50, 87, 83, 88, 51, 69, 68, 67, 52, 82, 70, 86, 53, 84, 71, 66, 54,
            89, 72, 78, 55, 85, 74, 77, 56, 73, 75, 44, 57, 79, 76, 46, 48, 80, 59, 47, 45, 91, 222, 16, 61, 93, 10, 8, 92);
    private final int keyboardSize = keycodes.size()/2;
    private final KarplusStrongString[] strings = new KarplusStrongString[keyboardSize];
    private int alt = 0;
//    private boolean hold = false;
    private float amp = 1;

    public StringSynth() {
//        this.setPreferredSize(new Dimension(500, 500));
//        addKeyListener(this);
//
//        JFrame f = new JFrame();
//        f.getContentPane().addAt(this);
//        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        f.pack();
//        f.setVisible(true);
//        f.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());

        double[] asymmetric_5limit = {1, (float) 16 / 15, (float) 9 / 8, (float) 6 / 5, (float) 5 / 4, (float) 4 / 3, (float) 45 / 32, (float) 3 / 2, (float) 8 / 5, (float) 5 / 3, (float) 9 / 5, (float) 15 / 8};
        double[] symmetric_5limit = {1, (float) 16 / 15, (float) 9 / 8, (float) 6 / 5, (float) 5 / 4, (float) 4 / 3, (float) 45 / 32, (float) 3 / 2, (float) 8 / 5, (float) 5 / 3, (float) 16 / 9, (float) 15 / 8};
        double[] just_2minor = {1, (float) 16 / 15, (float) 9 / 8, (float) 6 / 5, (float) 5 / 4, (float) 27 / 20, (float) 45 / 32, (float) 3 / 2, (float) 8 / 5, (float) 27 / 16, (float) 9 / 5, (float) 15 / 8};
        double[] pythagorean_6 = {1, (float) 16 / 15, (float) 9 / 8, (float) 6 / 5, (float) 5 / 4, (float) 4 / 3, (float) 45 / 32, (float) 3 / 2, (float) 8 / 5, (float) 27 / 16, (float) 9 / 5, (float) 15 / 8};
        double[] just_major = {1, (float) 25 / 24, (float) 9 / 8, (float) 5 / 4, (float) 4 / 3, (float) 27 / 20, (float) 45 / 32, (float) 3 / 2, (float) 25 / 16, (float) 5 / 3, (float) 27 / 16, (float) 15 / 8};
        double[] harmonic12 = {1, (float) 17 / 16, (float) 9 / 8, (float) 19 / 16, (float) 5 / 4, (float) 21 / 16, (float) 11 / 8, (float) 23 / 16, (float) 3 / 2, (float) 13 / 8, (float) 7 / 4, (float) 15 / 8};
        double[] harmonic16 = {1, (float) 17 / 16, (float) 9 / 8, (float) 19 / 16, (float) 5 / 4, (float) 21 / 16, (float) 11 / 8, (float) 23 / 16, (float) 3 / 2, (float) 25 / 16, (float) 13 / 8, (float) 27 / 16, (float) 7 / 4, (float) 29 / 16, (float) 15 / 8, (float) 31 / 16};
        double[] harmonic8 = {1, (float) 9 / 8, (float) 5 / 4, (float) 11 / 8, (float) 3 / 2, (float) 13 / 8, (float) 7 / 4, (float) 15 / 8};
        double[] seventh = {1, 135.0 / 128, 9.0 / 8, 5.0 / 4, 21.0 / 16, 45.0 / 32, 189.0 / 128, 3.0 / 2, 27.0 / 16, 7.0 / 4, 15.0 / 8, 63.0 / 32};
        double[] partch = {1, 81.0 / 80, 33.0 / 32, 21.0 / 20, 16.0 / 15, 12.0 / 11, 11.0 / 10, 10.0 / 9, 9.0 / 8, 8.0 / 7, 7.0 / 6, 32.0 / 27, 6.0 / 5, 11.0 / 9, 5.0 / 4, 14.0 / 11,
                9.0 / 7, 21.0 / 16, 4.0 / 3, 27.0 / 20, 11.0 / 8, 7.0 / 5, 10.0 / 7, 16.0 / 11, 40.0 / 27, 3.0 / 2, 32.0 / 21, 14.0 / 9, 11.0 / 7, 8.0 / 5, 18.0 / 11, 5.0 / 3,
                27.0 / 16, 12.0 / 7, 7.0 / 4, 16.0 / 9, 9.0 / 5, 20.0 / 11, 11.0 / 6, 15.0 / 8, 40.0 / 21, 64.0 / 33, 160.0 / 81};

        for (int i = 0; i < keyboardSize; i++) {
            double F = indexToET(i, 35, 12);
            strings[i] =
                    //new SquareVolumeWave(F);
                    new GuitarString(F);
            //new ViolinString(F);
            //new ViolinDiffDecayString(F);
            //strings[i + keyboardSize] = new SquareVolumeWave(indexToCustom(i, 2, asymmetric_5limit));
        }
    }

    public void keyPressed(KeyEvent e) {
        int index = keycodes.indexOf(e.getKeyCode());
        if (index == 3 && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
            index = 47;
        }
        if (index != -1) {
            keyPress(index, false);
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            for (KarplusStrongString x : strings) {
                x.increaseVolume();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            for (KarplusStrongString x : strings) {
                x.decreaseVolume();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            // decrease pitch
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            // increase pitch
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            alt = keyboardSize - alt;
        } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            releaseHeld();
            //hold = true;
        }
    }

    public void keyPress(int stringIndex, boolean hold) {
        KarplusStrongString string = strings[stringIndex + alt];
//        int status = string.status();
//        if (status == 3 || (status == 0 && hold)) {
//            string.setStatus(2);
//        } else if (status == 0) {
            string.setStatus(1);
//        }
//
//        if (hold) {
//            string.release();
//        }
    }

    public void keyReleased(KeyEvent e) {
        int index = keycodes.indexOf(e.getKeyCode());
        if (index == 3 && e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
            index = 47;
        }
        keyRelease(index, e.getKeyCode() == KeyEvent.VK_CONTROL);
    }

    public void keyRelease(int index, boolean cut) {
        if (index != -1) {
            if (strings[index].status() == -1) {
                strings[index].release();
                strings[index].setStatus(0);
            } else if (strings[index].status() == -2) {
                strings[index].setStatus(3);
            }
            if (strings[index + keyboardSize].status() == -1) {
                strings[index + keyboardSize].release();
                strings[index + keyboardSize].setStatus(0);
            } else if (strings[index + keyboardSize].status() == -2) {
                strings[index + keyboardSize].setStatus(3);
            }
        } else if (cut) {
            //hold = false;
        }
    }

    public void holdKeys() {
        for (KarplusStrongString x : strings) {
            if (x.status() == -1) {
                x.setStatus(-2);
            }
        }
    }

    public void releaseHeld() {
        for (KarplusStrongString x : strings) {
            if (x.status() == -2 || x.status() == 3) {
                x.setStatus(0);
                x.release();
            }
        }
    }


    // returns frequency in the octave above root
    private static double normalize(double root, double frequency) {
        if (frequency < root) {
            while (frequency < root) {
                frequency *= 2;
            }
        } else {
            while (frequency > root * 2) {
                frequency /= 2;
            }
        }
        return frequency;
    }

    private static double indexToET(int index, int indexOfA, int divisions) {
        return CONCERT_A * Math.pow(2.0, ((index - indexOfA) / (float) divisions));
    }

    // assumes base is in lowest octave
    private static double indexToGenerator(int index, int indexOfBase, double generator) {
        int note = (index + 12 - indexOfBase) % 12;
        int fifths = 0;
        while (note != 0) {
            note = (note - 7) % 12;
            fifths++;
        }
        if (fifths >= 7) {
            fifths -= 12;
        }
        double frequency = base * Math.pow(generator, fifths);
        frequency = normalize(base, frequency);
        int octaves = Math.floorDiv(index - indexOfBase, 12);
        return frequency * Math.pow(2.0, octaves);
    }

    private static double indexToCustom(int index, int indexOfBase, double[] ratios) {
        int octaveSize = ratios.length;
        int note = (index + octaveSize - indexOfBase) % octaveSize;
        int octaves = Math.floorDiv(index - indexOfBase, octaveSize);
        return base * ratios[note] * Math.pow(2.0, octaves);
    }


    public static void main(String[] args) {


        //EchoFilter echo = new spacegraph.audio.filter.EchoFilter(0.75, 10, 44100);

        StringSynth h = new StringSynth();

        while (true) {

            double sample = h.next();

            /* play the sample on standard audio */


//            sample = Distortion.distortion(sample, 32, 1);
//            sample = echo.apply(sample);


            StdAudio.play(sample);
        }
    }

    public void next(float[] target) {
        int n = target.length;
        for (int i = 0; i < n; )
            target[i++] = (float) next();
    }

    float[] tmp = new float[0];

    public void next(CircularFloatBuffer target, int num) {
        if (tmp.length != num)
            tmp = new float[num];
        next(tmp);
        target.write(tmp);
    }

    public double next() {

        double sample = 0.0;

        for (KarplusStrongString x : strings) {
            int xs = x.status();
            if (xs == 1 || xs == 2) {
                x.pluck();
                x.setStatus(-1 * xs);
            }
            sample += x.sample() * amp;
        }

        return Util.clampSafe(sample, -1, +1);
    }

    public void amp(float a) {
        this.amp = a;
    }

    public float amp() {
        return amp;
    }
}