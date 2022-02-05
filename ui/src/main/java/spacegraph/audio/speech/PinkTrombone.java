package spacegraph.audio.speech;

import jcog.Util;
import jcog.random.XoRoShiRo128PlusRandom;
import spacegraph.audio.Audio;
import spacegraph.audio.SoundProducer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/*
S A C K B U T (Pink Trombone)
forked from: https://github.com/allesspassig/sackbut

Bare-handed procedural speech synthesis

JAVA REIMPLEMENTATION by Adam Mendenhall

version 1.2, June 2019
by Neil Thapen
venuspatrol.nfshost.com


Bibliography

Julius O. Smith III, "Physical audio signal processing for virtual musical instruments and audio effects."
https://ccrma.stanford.edu/~jos/pasp/

Story, Brad H. "A parametric model of the vocal tract area function for vowel and consonant simulation." 
The Journal of the Acoustical Society of America 117.5 (2005): 3231-3254.

Lu, Hui-Ling, and J. O. Smith. "Glottal source modeling for singing voice synthesis." 
Proceedings of the 2000 International Computer Music Conference. 2000.

Mullen, Jack. Physical modelling of the vocal tract with the 2D digital waveguide mesh. 
PhD thesis, University of York, 2006.


Copyright 2017 Neil Thapen 

Permission is hereby granted, free of charge, to any person obtaining a 
copy of this software and associated documentation files (the "Software"), 
to deal in the Software without restriction, including without limitation 
the rights to use, copy, modify, merge, publish, distribute, sublicense, 
and/or sell copies of the Software, and to permit persons to whom the 
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in 
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
IN THE SOFTWARE.
*/

public class PinkTrombone extends JPanel implements SoundProducer {

    static final double maxMaxAmplitude = 0.2f;
    private static final Color orchid = new Color(218, 112, 214);
    private static final Color fillColor = new Color(255, 192, 203);
    private static final Color lineColor = new Color(192, 112, 198);
    @Deprecated
    private static final int sampleRate = 44100;
    public final double[] aspirateConsts;
    public final double[] fricativeConsts;
    private final double radius = 298;
    private final double scale = 60;
    private final double innerTongueControlRadius = 2.05;
    private final double outerTongueControlRadius = 3.5;
    private final int tongueLowerIndexBound;
    private final int tongueUpperIndexBound;
    private final double tongueIndexCentre;
    private final double angleScale = 0.64;
    private final double angleOffset = -0.24;
    private final double noseOffset = 0.8;
    private final double gridOffset = 1.7;
    private final ArrayList<Point> points;
    private final int n;
    private final int bladeStart;
    private final int tipStart;
    private final int lipStart;
    private final double[] R;                                                  // component going right
    private final double[] L;                                                  // component going left
    private final double[] reflection;
    private final double[] newReflection;
    private final double[] junctionOutputR;
    private final double[] junctionOutputL;
    private final double[] maxAmplitude;
    private final double[] diameter;
    private final double[] restDiameter;
    private final double[] targetDiameter;
    private final double[] newDiameter;
    private final double[] A;
    private final double glottalReflection = 0.75;
    private final double lipReflection = -0.85;
    private final double fade = 1.0;                     // 0.9999;
    private final double movementSpeed = 15;                      // cm per second
    private final ArrayList<Transient> transients;
    private final int noseLength;
    private final int noseStart;
    private final double[] noseL;
    private final double[] noseR;
    private final double[] noseJunctionOutputR;
    private final double[] noseJunctionOutputL;
    private final double[] noseReflection;
    private final double[] noseDiameter;
    private final double[] noseA;
    private final double[] noseMaxAmplitude;
    private final double vibratoAmount = 0.005;
    private final double vibratoFrequency = 6;
    //    private final int                  frameSize;
//    private final byte[]               frame;
//    private final SourceDataLine       sdl;
    private final double[] aspirateMemory;
    private final double[] fricativeMemory;
    private final Random rng = new XoRoShiRo128PlusRandom();
    public double UIFrequency = 140;
    public double smoothFrequency = 140;
    public double UITenseness = 0.6;
    public double intensity = 0;
    public double loudness = 1;
    public boolean alwaysVoice = true;
    long now;
    private double blockTime;
    private int blockSize;
    private final Set<Touch> touches = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Touch tongueTouch;
    private Touch mouseTouch = new Touch();
    private boolean isTouched = false;
    private double originX = 340;
    private double originY = 449;
    private double tongueIndex = 12.9;
    private double tongueDiameter = 2.43;
    private int lastObstruction = -1;
    private double lipOutput = 0;
    private double noseOutput = 0;
    private double velumTarget = 0.01;
    private double newReflectionLeft, newReflectionRight, newReflectionNose;
    private double reflectionLeft, reflectionRight, reflectionNose;
    private double timeInWaveform = 0;
    private double oldFrequency = 140;
    private double newFrequency = 140;
    private double oldTenseness = 0.6;
    private double newTenseness = 0.6;
    private double totalTime = 0;
    private boolean autoWobble = false;
    private double frequency;
    private double Rd;
    private double waveformLength;
    private double alpha;
    private double E0;
    private double epsilon;
    private double shift;
    private double Delta;
    private double Te;
    private double omega;
    private boolean started;
    private boolean soundOn;
    //    private double[] randBuffer;
//    private int      randBufferIdx;
    private int sampleIdx;

    public PinkTrombone() {
        points = new ArrayList<>();

        frequency = oldFrequency;
        double tenseness = oldTenseness;
        Rd = 3 * (1 - tenseness);
        waveformLength = 1.0 / frequency;

        if (Rd < 0.5) Rd = 0.5;
        if (Rd > 2.7) Rd = 2.7;
        // normalized to time = 1, Ee = 1
        double Ra = -0.01 + 0.048 * Rd;
        double Rk = 0.224 + 0.118 * Rd;
        double Rg = (Rk / 4) * (0.5 + 1.2 * Rk) / (0.11 * Rd - Ra * (0.5 + 1.2 * Rk));

        double Ta = Ra;
        double Tp = 1 / (2 * Rg);
        Te = Tp + Tp * Rk;

        epsilon = 1 / Ta;
        shift = Math.exp(-epsilon * (1 - Te));
        Delta = 1 - shift; // divide by this to scale RHS

        double RHSIntegral = (1 / epsilon) * (shift - 1) + (1 - Te) * shift;
        RHSIntegral = RHSIntegral / Delta;

        double totalLowerIntegral = -(Te - Tp) / 2 + RHSIntegral;
        double totalUpperIntegral = -totalLowerIntegral;

        omega = Math.PI / Tp;
        double s = Math.sin(omega * Te);
        // need E0*e^(alpha*Te)*s = -1 (to meet the return at -1)
        // and E0*e^(alpha*Tp/2) * Tp*2/pi = totalUpperIntegral
        // (our approximation of the integral up to Tp)
        // writing x for e^alpha,
        // have E0*x^Te*s = -1 and E0 * x^(Tp/2) * Tp*2/pi = totalUpperIntegral
        // dividing the second by the first,
        // letting y = x^(Tp/2 - Te),
        // y * Tp*2 / (pi*s) = -totalUpperIntegral;
        double y = -Math.PI * s * totalUpperIntegral / (Tp * 2);
        double z = Math.log(y);
        alpha = z / (Tp / 2 - Te);
        E0 = -1 / (s * Math.exp(alpha * Te));

        n = 44;
        bladeStart = 10;
        tipStart = 32;
        lipStart = 39;
        diameter = new double[n];
        restDiameter = new double[n];
        targetDiameter = new double[n];
        newDiameter = new double[n];
        for (int i = 0; i < diameter.length; ++i) {
            if (i < 6.5)
                diameter[i] = restDiameter[i] = targetDiameter[i] = newDiameter[i] = 0.6;
            else if (i < 12)
                diameter[i] = restDiameter[i] = targetDiameter[i] = newDiameter[i] = 1.1;
            else
                diameter[i] = restDiameter[i] = targetDiameter[i] = newDiameter[i] = 1.5;
        }
        R = new double[n];
        L = new double[n];
        reflection = new double[n + 1];
        newReflection = new double[n + 1];
        junctionOutputR = new double[n + 1];
        junctionOutputL = new double[n + 1];
        A = new double[n + 1];
        maxAmplitude = new double[n];

        noseLength = 28;
        noseStart = n - noseLength + 1;
        noseR = new double[noseLength];
        noseL = new double[noseLength];
        noseJunctionOutputR = new double[noseLength + 1];
        noseJunctionOutputL = new double[noseLength + 1];
        noseReflection = new double[noseLength + 1];
        noseDiameter = new double[noseLength];
        noseA = new double[noseLength];
        noseMaxAmplitude = new double[noseLength];
        for (int i = 0; i < noseDiameter.length; ++i) {
            double d = 2d * i / noseDiameter.length;
            if (d < 1)
                noseDiameter[i] = Math.min(1.9, 0.4 + 1.6 * d);
            else
                noseDiameter[i] = Math.min(1.9, 0.5 + 1.5 * (2 - d));
        }
        newReflectionLeft = newReflectionRight = newReflectionNose = 0;
        for (int i = 0; i < n; ++i) {
            A[i] = diameter[i] * diameter[i]; // ignoring PI etc.
        }
        for (int i = 1; i < n; ++i) {
            reflection[i] = newReflection[i];
            if (A[i] == 0)
                newReflection[i] = 0.999; // to prevent some bad behaviour if 0
            else
                newReflection[i] = (A[i - 1] - A[i]) / (A[i - 1] + A[i]);
        }

        // now at junction with nose

        reflectionLeft = newReflectionLeft;
        reflectionRight = newReflectionRight;
        reflectionNose = newReflectionNose;
        double sum = A[noseStart] + A[noseStart + 1] + noseA[0];
        newReflectionLeft = (2 * A[noseStart] - sum) / sum;
        newReflectionRight = (2 * A[noseStart + 1] - sum) / sum;
        newReflectionNose = (2 * noseA[0] - sum) / sum;
        for (int i = 0; i < noseLength; ++i) {
            noseA[i] = noseDiameter[i] * noseDiameter[i];
        }
        for (int i = 1; i < noseLength; ++i) {
            noseReflection[i] = (noseA[i - 1] - noseA[i]) / (noseA[i - 1] + noseA[i]);
        }
        noseDiameter[0] = velumTarget;
        transients = new ArrayList<>();

        for (int i = bladeStart; i < lipStart; i++) {
            final double t = 1.1 * Math.PI * (tongueIndex - i) / (tipStart - bladeStart);
            final double fixedTongueDiameter = 2 + (tongueDiameter - 2) / 1.5;
            double curve = (1.5 - fixedTongueDiameter + gridOffset) * Math.cos(t);
            if (i == bladeStart - 2 || i == lipStart - 1) curve *= 0.8;
            if (i == bladeStart || i == lipStart - 2) curve *= 0.94;
            restDiameter[i] = 1.5 - curve;
        }
        tongueLowerIndexBound = bladeStart + 2;
        tongueUpperIndexBound = tipStart - 3;
        tongueIndexCentre = 0.5 * (tongueLowerIndexBound + tongueUpperIndexBound);

        soundOn = false;
        started = false;

//        AudioFormat    format      = new AudioFormat(sampleRate, 16, 1, true, true);
//        SourceDataLine nonFinalSdl = null;
//        try {
//            //AudioJSound.printMixerInfo();
//            Mixer.Info[] m = AudioSystem.getMixerInfo();
//            nonFinalSdl = AudioSystem.getSourceDataLine(format, m[4]);
////            nonFinalSdl = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
//            System.out.println("source: " + nonFinalSdl.getLineInfo());
//        } catch (final LineUnavailableException e) {
//            e.printStackTrace();
//            System.exit(0);
//        }

//        sdl = j.out;
//        try {
//            sdl.open();
//        } catch (final LineUnavailableException e) {
//            e.printStackTrace();
//            System.exit(0);
//        }
//        frameSize = sdl.getFormat().getFrameSize();
//        frame     = new byte[frameSize];

        aspirateConsts = new double[5];
        fricativeConsts = new double[5];
        aspirateMemory = new double[6];
        fricativeMemory = new double[6];
        double omega = 2 * Math.PI * 500 / 44100d;
        double sinOmega = Math.sin(omega);
        aspirateConsts[0] = sinOmega / (1 + sinOmega); // a_1
        aspirateConsts[1] = 0; // a_2
        aspirateConsts[2] = -sinOmega / (1 + sinOmega); // b_0
        aspirateConsts[3] = -2 * Math.cos(omega) / (1 + sinOmega); // b_1
        aspirateConsts[4] = (1 - sinOmega) / (1 + sinOmega); // b_2
        omega = 2 * Math.PI * 1000 / 44100d;
        sinOmega = Math.sin(omega);
        fricativeConsts[0] = sinOmega / (1 + sinOmega); // a_1
        fricativeConsts[1] = 0; // a_2
        fricativeConsts[2] = -sinOmega / (1 + sinOmega); // b_0
        fricativeConsts[3] = -2 * Math.cos(omega) / (1 + sinOmega); // b_1
        fricativeConsts[4] = (1 - sinOmega) / (1 + sinOmega); // b_2

        sampleIdx = -1;
        startSound();
        mute();
        unmute();

        setPreferredSize(new Dimension(600, 600));
        addComponentListener(new ComponentListener() {
            public void componentShown(final ComponentEvent e) {
            }

            public void componentResized(final ComponentEvent e) {
                originX = 340 / 2 + e.getComponent().getWidth() / 2;
                originY = 449 / 2 + e.getComponent().getHeight() / 2;
            }

            public void componentMoved(final ComponentEvent e) {
            }

            public void componentHidden(final ComponentEvent e) {
            }
        });
        addMouseListener(new MouseListener() {
            public void mouseReleased(MouseEvent e) {
                endMouse(e);
            }

            public void mousePressed(MouseEvent e) {
                startMouse(e);
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
                //moveMouse(e);
            }

            public void mouseDragged(MouseEvent e) {
                moveMouse(e);
            }
        });
    }

    private static byte double2byte(final double in) {
        return (byte) (64 * in);
    }

    public static void main(String[] args) {
        final PinkTrombone pk = new PinkTrombone();
        final JButton autoWobbleButton = new JButton("Auto wobble");
        autoWobbleButton.setForeground(pk.getAutoWobble() ? Color.GREEN : Color.RED);
        autoWobbleButton.addActionListener(e -> {
            if (autoWobbleButton.getForeground() == Color.GREEN) {
                autoWobbleButton.setForeground(Color.RED);
                pk.setAutoWobble(false);
            } else {
                autoWobbleButton.setForeground(Color.GREEN);
                pk.setAutoWobble(true);
            }
        });
        final JButton muteButton = new JButton("Mute");
        muteButton.setForeground(pk.muted() ? Color.GREEN : Color.RED);
        muteButton.addActionListener(e -> {
            if (muteButton.getForeground() == Color.GREEN) {
                muteButton.setForeground(Color.RED);
                muteButton.setText("Unmute");
                pk.mute();
            } else {
                muteButton.setForeground(Color.GREEN);
                muteButton.setText("Mute");
                pk.unmute();
            }
        });
        final JSpinner loudnessspinner = new JSpinner(new SpinnerNumberModel(1., 0., 1., .001));
        loudnessspinner.addChangeListener(e -> pk.loudness = (Double) loudnessspinner.getValue());
        final JSlider note = new JSlider(JSlider.HORIZONTAL, -30, 50, 10);
        note.addChangeListener(e -> {
            double semitone = note.getValue();
            pk.UIFrequency = 87.3071 /*F*/ * Math.pow(2, semitone / 12d);
            if (pk.intensity == 0) pk.smoothFrequency = pk.UIFrequency;
        });
        final JSlider power = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
        power.addChangeListener(e -> {
            // UIRd = 3*local_y / (keyboardHeight-20);
            pk.UITenseness = 1 - Math.cos(power.getValue() / 1000d * Math.PI * 0.5);
            pk.loudness = Math.pow(pk.UITenseness, 0.25);
            if (pk.intensity == 0) pk.smoothFrequency = pk.UIFrequency;
        });
        JPanel controls = new JPanel(new GridLayout(0, 1));
        controls.add(autoWobbleButton);
        controls.add(muteButton);
        controls.add(note);
        controls.add(power);

        JFrame frame = new JFrame("sackbut");
        frame.add(controls, BorderLayout.WEST);
        frame.add(pk);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        while (frame.isVisible()) {
            frame.repaint();
        }
    }

    private void runStep(final double glottalOutput, double turbulenceNoise, final double lambda) {
        boolean updateAmplitudes = (Math.random() < 0.1);

        // mouth
        for (Transient t : transients) {
            double amplitude = t.strength * Math.pow(2, -t.exponent * t.timeAlive);
            R[t.position] += amplitude / 2;
            L[t.position] += amplitude / 2;
            t.timeAlive += 1.0 / (sampleRate * 2);
        }
        synchronized (transients) {
            transients.removeIf(trans -> trans.timeAlive > trans.lifeTime);
        }
        var mouseTouch = this.mouseTouch;
        if (mouseTouch != null) {
            if (mouseTouch.index >= 2 && mouseTouch.index < n - 2 && mouseTouch.diameter > 0 && mouseTouch.fricative_intensity != 0) {
                int i = (int) Math.floor(mouseTouch.index);
                double delta = mouseTouch.index - i;
                turbulenceNoise *= 0.66 * mouseTouch.fricative_intensity * UITenseness * intensity * (0.1 + 0.2 * Math.max(0, Math.sin(Math.PI * 2 * timeInWaveform / waveformLength)))
                        + (1 - UITenseness * intensity) * 0.3;
                double thinness0 = Util.clamp(8 * (0.7 - mouseTouch.diameter), 0, 1);
                double openness = Util.clamp(30 * (mouseTouch.diameter - 0.3), 0, 1);
                double noise0 = turbulenceNoise * (1 - delta) * thinness0 * openness;
                double noise1 = turbulenceNoise * delta * thinness0 * openness;
                noise0 /= 2;
                noise1 /= 2;
                L[i + 1] += noise0;
                L[i + 2] += noise1;
                R[i + 1] += noise0;
                R[i + 2] += noise1;
            }
        }
        // glottalReflection = -0.8 + 1.6 * newTenseness;
        junctionOutputR[0] = L[0] * glottalReflection + glottalOutput;
        junctionOutputL[n] = R[n - 1] * lipReflection;

        for (int i = 1; i < n; ++i) {
            double r = reflection[i] * (1 - lambda) + newReflection[i] * lambda;
            double w = r * (R[i - 1] + L[i]);
            junctionOutputR[i] = R[i - 1] - w;
            junctionOutputL[i] = L[i] + w;
        }

        // now at junction with nose
        double r = newReflectionLeft * (1 - lambda) + reflectionLeft * lambda;
        junctionOutputL[noseStart] = r * R[noseStart - 1] + (1 + r) * (noseL[0] + L[noseStart]);
        r = newReflectionRight * (1 - lambda) + reflectionRight * lambda;
        junctionOutputR[noseStart] = r * L[noseStart] + (1 + r) * (R[noseStart - 1] + noseL[0]);
        r = newReflectionNose * (1 - lambda) + reflectionNose * lambda;
        noseJunctionOutputR[0] = r * noseL[0] + (1 + r) * (L[noseStart] + R[noseStart - 1]);

        for (int i = 0; i < n; ++i) {
            R[i] = junctionOutputR[i] * 0.999;
            L[i] = junctionOutputL[i + 1] * 0.999;

            if (updateAmplitudes) {
                double amplitude =
                        //Math.abs(R[i] + L[i]);
                        Math.abs(R[i]) + Math.abs(L[i]);
                if (amplitude > maxAmplitude[i])
                    maxAmplitude[i] = Util.clamp(amplitude, 0, maxMaxAmplitude);
                else
                    maxAmplitude[i] *= 0.999;
            }
        }

        lipOutput = R[n - 1];

        // nose
        noseJunctionOutputL[noseLength] = noseR[noseLength - 1] * lipReflection;

        for (int i = 1; i < noseLength; ++i) {
            double w = noseReflection[i] * (noseR[i - 1] + noseL[i]);
            noseJunctionOutputR[i] = noseR[i - 1] - w;
            noseJunctionOutputL[i] = noseL[i] + w;
        }

        for (int i = 0; i < noseLength; ++i) {
            noseR[i] = noseJunctionOutputR[i] * fade;
            noseL[i] = noseJunctionOutputL[i + 1] * fade;

            // noseR[i] = Math.clamp(noseJunctionOutputR[i] * fade, -1, 1);
            // noseL[i] = Math.clamp(noseJunctionOutputL[i+1] * fade, -1, 1);

            if (updateAmplitudes) {
                double amplitude = Math.abs(noseR[i] + noseL[i]);
                if (amplitude > noseMaxAmplitude[i])
                    noseMaxAmplitude[i] = amplitude;
                else
                    noseMaxAmplitude[i] *= 0.999;
            }
        }

        noseOutput = noseR[noseLength - 1];
    }

    //    public void run() {

    private void startSound() {
        started = true;
        Audio.the().play(this);
    }

    @Override
    public boolean read(float[] buf, int readRate) {
        blockSize = buf.length;
        blockTime = (double) blockSize / sampleRate;

        ui();
        for (int i = 0; i < buf.length; i++) {
            double v =
                    glottisTract(fricative(aspirate(rng.nextFloat())));
            buf[i] = (float) v;
        }
        return true;
    }

    //        while (started) {
////            while (soundOn) {
////                while (sdl.available() > (int) (.6 * sdl.getBufferSize())) {
////                    for (int i = 0; i < frameSize; ++i) {
////                        frame[i] = double2byte(doGlottisTractStuff(fricative(aspirate(randBuffer[randBufferIdx = (randBufferIdx + 1) % randBuffer.length]))));
////                    }
////                    sdl.write(frame, 0, frameSize);
////                }
////                updateTouches();
////            }
//            updateTouches();
//            // TODO: perhaps don't constantly wait for soundOn; pause the thread for a sec instead
//        }
//    }
//
    private double aspirate(final double in) {
        aspirateMemory[2] = in;
        aspirateMemory[5] = aspirateConsts[2] * aspirateMemory[2] + aspirateConsts[3] * aspirateMemory[1] + aspirateConsts[4] * aspirateMemory[0] - aspirateConsts[0] * aspirateMemory[4]
                - aspirateConsts[1] * aspirateMemory[3];
        aspirateMemory[0] = aspirateMemory[1];
        aspirateMemory[1] = aspirateMemory[2];
        aspirateMemory[3] = aspirateMemory[4];
        aspirateMemory[4] = aspirateMemory[5];
        return aspirateMemory[5];
    }

    private double fricative(final double in) {
        fricativeMemory[2] = in;
        fricativeMemory[5] = fricativeConsts[2] * fricativeMemory[2] + fricativeConsts[3] * fricativeMemory[1] + fricativeConsts[4] * fricativeMemory[0] - fricativeConsts[0] * fricativeMemory[4]
                - fricativeConsts[1] * fricativeMemory[3];
        fricativeMemory[0] = fricativeMemory[1];
        fricativeMemory[1] = fricativeMemory[2];
        fricativeMemory[3] = fricativeMemory[4];
        fricativeMemory[4] = fricativeMemory[5];
        return fricativeMemory[5];
    }

    // usually in the range [-1,1]
    // almost always in the range [-2,2]
    private double glottisTract(final double in) {
        double lambda1 = ((double) sampleIdx) / blockSize;
        double lambda2 = (sampleIdx + 0.5) / blockSize;
        double timeStep = 1.0 / sampleRate;
        timeInWaveform += timeStep;
        totalTime += timeStep;
        if (timeInWaveform > waveformLength) {
            timeInWaveform -= waveformLength;
            frequency = oldFrequency * (1 - lambda1) + newFrequency * lambda1;
            double tenseness = oldTenseness * (1 - lambda1) + newTenseness * lambda1;
            Rd = 3 * (1 - tenseness);
            waveformLength = 1.0 / frequency;

            if (Rd < 0.5) Rd = 0.5;
            if (Rd > 2.7) Rd = 2.7;
            // normalized to time = 1, Ee = 1
            double Ra = -0.01 + 0.048 * Rd;
            double Rk = 0.224 + 0.118 * Rd;
            double Rg = (Rk / 4) * (0.5 + 1.2 * Rk) / (0.11 * Rd - Ra * (0.5 + 1.2 * Rk));

            double Ta = Ra;
            double Tp = 1 / (2 * Rg);
            Te = Tp + Tp * Rk;

            epsilon = 1 / Ta;
            shift = Math.exp(-epsilon * (1 - Te));
            Delta = 1 - shift; // divide by this to scale RHS

            double RHSIntegral = (1 / epsilon) * (shift - 1) + (1 - Te) * shift;
            RHSIntegral = RHSIntegral / Delta;

            double totalLowerIntegral = -(Te - Tp) / 2 + RHSIntegral;
            double totalUpperIntegral = -totalLowerIntegral;

            omega = Math.PI / Tp;
            double s = Math.sin(omega * Te);
            // need E0*e^(alpha*Te)*s = -1 (to meet the return at -1)
            // and E0*e^(alpha*Tp/2) * Tp*2/pi = totalUpperIntegral
            // (our approximation of the integral up to Tp)
            // writing x for e^alpha,
            // have E0*x^Te*s = -1 and E0 * x^(Tp/2) * Tp*2/pi = totalUpperIntegral
            // dividing the second by the first,
            // letting y = x^(Tp/2 - Te),
            // y * Tp*2 / (pi*s) = -totalUpperIntegral;
            double y = -Math.PI * s * totalUpperIntegral / (Tp * 2);
            double z = Math.log(y);
            alpha = z / (Tp / 2 - Te);
            E0 = -1 / (s * Math.exp(alpha * Te));
        }
        double glottalOutput;
        double t = timeInWaveform / waveformLength;
        if (t > Te)
            glottalOutput = (-Math.exp(-epsilon * (t - Te)) + shift) / Delta;
        else
            glottalOutput = E0 * Math.exp(alpha * t) * Math.sin(omega * t);
        glottalOutput *= intensity * loudness;
        double aspiration = intensity * (1 - Math.sqrt(UITenseness)) * in * UITenseness * intensity * (0.1 + 0.2 * Math.max(0, Math.sin(Math.PI * 2 * timeInWaveform / waveformLength)))
                + (1 - UITenseness * intensity) * 0.3;
        aspiration *= 0.2 + 0.02 * MathUtil.getInstance().simplex1(totalTime * 1.99);
        glottalOutput += aspiration;

        double vocalOutput = 0;
        // Tract runs at twice the sample rate
        runStep(glottalOutput, in, lambda1);
        vocalOutput += lipOutput + noseOutput;
        runStep(glottalOutput, in, lambda2);
        vocalOutput += lipOutput + noseOutput;

        if ((sampleIdx = (sampleIdx + 1) % blockSize) % blockSize == 0) {
            double amount = blockTime * movementSpeed;
            int newLastObstruction = -1;
            for (int i = 0; i < n; i++) {
                double diameter = this.diameter[i];
                double targetDiameter = this.targetDiameter[i];
                if (diameter <= 0) newLastObstruction = i;
                double slowReturn;
                if (i < noseStart)
                    slowReturn = 0.6;
                else if (i >= tipStart)
                    slowReturn = 1.0;
                else
                    slowReturn = 0.6 + 0.4 * (i - noseStart) / (tipStart - noseStart);
                this.diameter[i] = MathUtil.moveTowards(diameter, targetDiameter, slowReturn * amount, 2 * amount);
            }
            if (lastObstruction > -1 && newLastObstruction == -1 && noseA[0] < 0.05) {
                Transient trans = new Transient();
                trans.position = lastObstruction;
                trans.timeAlive = 0;
                trans.lifeTime = 0.2;
                trans.strength = 0.3;
                trans.exponent = 200;
                transients.add(trans);
            }
            lastObstruction = newLastObstruction;

            amount = blockTime * movementSpeed;
            noseDiameter[0] = MathUtil.moveTowards(noseDiameter[0], velumTarget, amount * 0.25, amount * 0.1);
            noseA[0] = noseDiameter[0] * noseDiameter[0];
            for (int i = 0; i < n; ++i) {
                A[i] = diameter[i] * diameter[i]; // ignoring PI etc.
            }
            for (int i = 1; i < n; ++i) {
                reflection[i] = newReflection[i];
                if (A[i] == 0)
                    newReflection[i] = 0.999; // to prevent some bad behaviour if 0
                else
                    newReflection[i] = (A[i - 1] - A[i]) / (A[i - 1] + A[i]);
            }

            // now at junction with nose

            reflectionLeft = newReflectionLeft;
            reflectionRight = newReflectionRight;
            reflectionNose = newReflectionNose;
            double sum = A[noseStart] + A[noseStart + 1] + noseA[0];
            newReflectionLeft = (2 * A[noseStart] - sum) / sum;
            newReflectionRight = (2 * A[noseStart + 1] - sum) / sum;
            newReflectionNose = (2 * noseA[0] - sum) / sum;
            double vibrato = 0;
            vibrato += vibratoAmount * Math.sin(2 * Math.PI * totalTime * vibratoFrequency);
            vibrato += 0.02 * MathUtil.getInstance().simplex1(totalTime * 4.07);
            vibrato += 0.04 * MathUtil.getInstance().simplex1(totalTime * 2.15);
            if (autoWobble) {
                vibrato += 0.2 * MathUtil.getInstance().simplex1(totalTime * 0.98);
                vibrato += 0.4 * MathUtil.getInstance().simplex1(totalTime * 0.5);
            }
            if (UIFrequency > smoothFrequency) smoothFrequency = Math.min(smoothFrequency * 1.1, UIFrequency);
            if (UIFrequency < smoothFrequency) smoothFrequency = Math.max(smoothFrequency / 1.1, UIFrequency);
            oldFrequency = newFrequency;
            newFrequency = smoothFrequency * (1 + vibrato);
            oldTenseness = newTenseness;
            newTenseness = UITenseness + 0.1 * MathUtil.getInstance().simplex1(totalTime * 0.46) + 0.05 * MathUtil.getInstance().simplex1(totalTime * 0.36);
            if (!isTouched && alwaysVoice) newTenseness += (3 - UITenseness) * (1 - intensity);
            if (isTouched || alwaysVoice)
                intensity += 0.13;
            else
                intensity -= 0.05;
            intensity = Util.clamp(intensity, 0, 1);
        }

        return vocalOutput * 0.125;
    }

    public void mute() {
        //sdl.stop();
        soundOn = false;
    }

    public void unmute() {
        //sdl.start();
        soundOn = true;
    }

    public boolean muted() {
        return soundOn;
    }

    public boolean started() {
        return started;
    }

    public boolean getAutoWobble() {
        return autoWobble;
    }

    public void setAutoWobble(final boolean autoWobble) {
        this.autoWobble = autoWobble;
    }

    private void startMouse(final MouseEvent event) {
        if (mouseTouch != null) {
            mouseTouch.stop();
            mouseTouch = null;
        }
        Touch touch = new Touch();
        touch.startTime = System.currentTimeMillis() / 1000.0;
        touch.fricative_intensity = 0;
        touch.endTime = -1;
        touch.alive = true;
        touch.x = event.getX();
        touch.y = event.getY();
        touch.index = getIndex(touch.x, touch.y);
        touch.diameter = getDiameter(touch.x, touch.y);
        mouseTouch = touch;
        touches.add(touch);
//        handleTouches();
    }

    private void moveMouse(final MouseEvent event) {
        Touch touch = mouseTouch;
        if (!touch.alive) return;
        touch.x = event.getX();
        touch.y = event.getY();
        touch.index = getIndex(touch.x, touch.y);
        touch.diameter = getDiameter(touch.x, touch.y);
        //handleTouches();
    }

    private void endMouse(final MouseEvent event) {
        Touch touch = mouseTouch;
        touch.stop();
        mouseTouch = null;
        //handleTouches();
    }

    private void ui() {
        double fricativeAttackTime = 0.1;
        double time = System.currentTimeMillis() / 1000.0;
        touches.removeIf(t -> {
            if (!(t.alive) && (time > t.endTime + 1)) {
                return true;
            } else if (t.alive) {
                t.fricative_intensity = Util.clamp((time - t.startTime) / fricativeAttackTime, 0, 1);
            } else {
                t.fricative_intensity = Util.clamp(1 - (time - t.endTime) / fricativeAttackTime, 0, 1);
            }
            return false;
        });

        isTouched = !touches.isEmpty();

        if (tongueTouch == null) {
            for (final Touch touch : touches) {
                if (!touch.alive) continue;
                if (touch.fricative_intensity == 1) continue; // only new touches will pass this
                double x = touch.x, y = touch.y;
                double index = getIndex(x, y);
                if (index >= tongueLowerIndexBound - 4 && index <= tongueUpperIndexBound + 4) {
                    double diameter1 = getDiameter(x, y);
                    if (diameter1 >= innerTongueControlRadius - 0.5 && diameter1 <= outerTongueControlRadius + 0.5) {
                        tongueTouch = touch;
                        break;
                    }
                }
            }
        }

        if (tongueTouch != null) {
            double x = tongueTouch.x;
            double y = tongueTouch.y;
            double index = getIndex(x, y);
            double diameter1 = getDiameter(x, y);
            double fromPoint = Util.unitize((outerTongueControlRadius - diameter1) / (outerTongueControlRadius - innerTongueControlRadius));
            fromPoint = Math.pow(fromPoint, 0.58) - 0.2 * (fromPoint * fromPoint - fromPoint); // horrible kludge to fit curve to straight line
            fromPoint = Util.unitize(fromPoint);
            tongueDiameter = Util.clamp(diameter1, innerTongueControlRadius, outerTongueControlRadius);
            // this.tongueIndex = MathUtil.clamp(index, this.tongueLowerIndexBound, this.tongueUpperIndexBound);
            double out = fromPoint * 0.5 * (tongueUpperIndexBound - tongueLowerIndexBound);
            tongueIndex = Util.clamp(index, tongueIndexCentre - out, tongueIndexCentre + out);
        }

        for (int i = bladeStart; i < lipStart; i++) {
            final double t = 1.1 * Math.PI * (tongueIndex - i) / (tipStart - bladeStart);
            final double fixedTongueDiameter = 2 + (tongueDiameter - 2) / 1.5;
            double curve = (1.5 - fixedTongueDiameter + gridOffset) * Math.cos(t);
            if (i == bladeStart - 2 || i == lipStart - 1) curve *= 0.8;
            if (i == bladeStart || i == lipStart - 2) curve *= 0.94;
            restDiameter[i] = 1.5 - curve;
        }
        if (n >= 0) System.arraycopy(restDiameter, 0, targetDiameter, 0, n);

        // other constrictions and nose
        velumTarget = 0.01;

        for (final Touch touch : touches) {
            if (!touch.alive) continue;
            double x = touch.x;
            double y = touch.y;
            double index = getIndex(x, y);
            double diameter1 = getDiameter(x, y);
            if (index > noseStart && diameter1 < -noseOffset) {
                velumTarget = 0.4;
            }
            if (diameter1 < -0.85 - noseOffset) continue;
            diameter1 -= 0.3;
            if (diameter1 < 0) diameter1 = 0;
            double width;
            if (index < 25)
                width = 10;
            else if (index >= tipStart)
                width = 5;
            else
                width = 10 - 5 * (index - 25) / (tipStart - 25);
            if (index >= 2 && index < n && diameter1 < 3) {
                int intIndex = (int) Math.round(index);
                for (int i = -(int) Math.ceil(width) - 1; i < width + 1; i++) {
                    if (intIndex + i < 0 || intIndex + i >= n) continue;
                    double relpos = (intIndex + i) - index;
                    relpos = Math.abs(relpos) - 0.5;
                    double shrink;
                    if (relpos <= 0)
                        shrink = 0;
                    else if (relpos > width)
                        shrink = 1;
                    else
                        shrink = 0.5 * (1 - Math.cos(Math.PI * relpos / width));
                    if (diameter1 < targetDiameter[intIndex + i]) {
                        targetDiameter[intIndex + i] = diameter1 + (targetDiameter[intIndex + i] - diameter1) * shrink;
                    }
                }
            }
        }

    }

    private int getIndex(final double x, final double y) {
        final double xx = x - originX;
        final double yy = y - originY;
        double angle = Math.atan2(yy, xx);
        while (angle > 0) angle -= 2 * Math.PI;
        return (int) ((Math.PI + angle - angleOffset) * (lipStart - 1) / (angleScale * Math.PI));
    }

    private double getDiameter(final double x, final double y) {
        final double xx = x - originX;
        final double yy = y - originY;
        return (radius - Math.sqrt(xx * xx + yy * yy)) / scale;
    }

    private Point indexDia2G2DPoint(final double e, final double d) {
        double angle = angleOffset + e * angleScale * Math.PI / (lipStart - 1);
        double wobble = (maxAmplitude[n - 1] + noseMaxAmplitude[noseLength - 1]);
        wobble *= 0.03 * Math.sin(2 * e - now / 20.0) * e / n;
        angle += wobble;
        double r = radius - scale * d + 100 * wobble;
        return new Point((int) (originX - r * Math.cos(angle)), (int) (originY - r * Math.sin(angle)));
    }

    private void drawText(final double e, final double d, final String text, final Graphics g, final boolean rotate) {
        final double angle = angleOffset + e * angleScale * Math.PI / (lipStart - 1);
        final double r = radius - scale * d;
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(originX - r * Math.cos(angle), originY - r * Math.sin(angle) + 2); // +8);
        if (rotate) g2d.rotate(angle - Math.PI / 2);
        g2d.drawString(text, 0, 0);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        now = System.currentTimeMillis();

        // background
        g.setColor(orchid);
        g.setFont(g.getFont().deriveFont(20f));
        drawText(n * 0.44, -0.28, "soft", g, true);
        drawText(n * 0.51, -0.28, "palate", g, true);
        drawText(n * 0.77, -0.28, "hard", g, true);
        drawText(n * 0.84, -0.28, "palate", g, true);
        drawText(n * 0.95, -0.28, " lip", g, true);

        g.setFont(g.getFont().deriveFont(17f));
        // drawText(n * 0.18, 3, " tongue control", g, false);
        drawText(n * 1.03, -1.07, "nasals", g, true);
        drawText(n * 1.03, -0.28, "stops", g, true);
        drawText(n * 1.03, 0.51, "fricatives", g, true);
        drawText(1.5, +0.8, "glottis", g, false);
        // ctx.lineWidth = 2;
        points.add(indexDia2G2DPoint(n * 1.03, 0));
        points.add(indexDia2G2DPoint(n * 1.07, 0));
        g.drawPolygon(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        points.add(indexDia2G2DPoint(n * 1.03, -noseOffset));
        points.add(indexDia2G2DPoint(n * 1.07, -noseOffset));
        g.drawPolygon(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();

        // cavity fills
        g.setColor(fillColor);
        points.add(indexDia2G2DPoint(1, 0));
        // tract
        for (int i = 1; i < n; ++i) points.add(indexDia2G2DPoint(i, diameter[i]));
        for (int i = n - 1; i >= 2; --i) points.add(indexDia2G2DPoint(i, 0));
        g.fillPolygon(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        // nose
        points.add(indexDia2G2DPoint(noseStart, -noseOffset));
        for (int i = 1; i < noseLength; ++i)
            points.add(indexDia2G2DPoint(i + noseStart, -noseOffset - noseDiameter[i] * 0.9));
        for (int i = noseLength - 1; i >= 1; --i) points.add(indexDia2G2DPoint(i + noseStart, -noseOffset));
        g.fillPolygon(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        // velum
        final double velumAngle = noseDiameter[0] * 4;
        points.add(indexDia2G2DPoint(noseStart - 2, 0));
        points.add(indexDia2G2DPoint(noseStart, -noseOffset));
        points.add(indexDia2G2DPoint(noseStart + velumAngle, -noseOffset));
        points.add(indexDia2G2DPoint(noseStart + velumAngle - 2, 0));
        g.fillPolygon(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();

        // sound lines
        g.setColor(orchid);
        for (int i = 2; i < n - 1; i++) {
            // ctx.lineWidth = Math.sqrt(Tract.maxAmplitude[i])*3;
            points.add(indexDia2G2DPoint(i, 0));
            points.add(indexDia2G2DPoint(i, diameter[i]));
            g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
            points.clear();
        }
        for (int i = 1; i < noseLength - 1; i++) {
            // ctx.lineWidth = Math.sqrt(Tract.noseMaxAmplitude[i]) * 3;
            points.add(indexDia2G2DPoint(i + noseStart, -noseOffset));
            points.add(indexDia2G2DPoint(i + noseStart, -noseOffset - noseDiameter[i] * 0.9));
            g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
            points.clear();
        }

        // labels
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(20f));
        drawText(n * 0.10, 0.425, "throat", g, true);
        drawText(n * 0.71, -1.8, "nasal", g, true);
        drawText(n * 0.71, -1.3, "cavity", g, true);
        drawText(n * 0.6, 0.9, "oral", g, true);
        drawText(n * 0.7, 0.9, "cavity", g, true);

        // cavity lines
        // ctx.lineWidth = 5;
        g.setColor(lineColor);
        // tract
        points.add(indexDia2G2DPoint(1, diameter[0]));
        for (int i = 2; i < n; i++) points.add(indexDia2G2DPoint(i, diameter[i]));
        g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        points.add(indexDia2G2DPoint(1, 0));
        for (int i = 2; i <= noseStart - 2; i++) points.add(indexDia2G2DPoint(i, 0));
        g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        points.add(indexDia2G2DPoint(noseStart + velumAngle - 2, 0));
        for (int i = (int) (noseStart + Math.ceil(velumAngle) - 2); i < n; i++) points.add(indexDia2G2DPoint(i, 0));
        g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        // nose
        points.add(indexDia2G2DPoint(noseStart, -noseOffset));
        for (int i = 1; i < noseLength; i++)
            points.add(indexDia2G2DPoint(i + noseStart, -noseOffset - noseDiameter[i] * 0.9));
        g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        points.add(indexDia2G2DPoint(noseStart + velumAngle, -noseOffset));
        for (int i = (int) Math.ceil(velumAngle); i < noseLength; i++)
            points.add(indexDia2G2DPoint(i + noseStart, -noseOffset));
        g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        // velum
        // ctx.globalAlpha = velum*5;
        points.add(indexDia2G2DPoint(noseStart - 2, 0));
        points.add(indexDia2G2DPoint(noseStart, -noseOffset));
        g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();
        points.add(indexDia2G2DPoint(noseStart + velumAngle - 2, 0));
        points.add(indexDia2G2DPoint(noseStart + velumAngle, -noseOffset));
        g.drawPolyline(points.stream().mapToInt(a -> a.x).toArray(), points.stream().mapToInt(a -> a.y).toArray(), points.size());
        points.clear();

        g.setColor(orchid);
        g.setFont(g.getFont().deriveFont(20f));
        drawText(n * 0.95, 0.8 + 0.8 * diameter[n - 1], " lip", g, true);

        g.setColor(Color.RED.brighter());
        g.setFont(g.getFont().deriveFont(24f));
        int a = 2;
        double b = 1.5;
        drawText(15, a + b * 0.60, "" + (char) 0xe6, g, true); // pat
        drawText(13, a + b * 0.27, "" + (char) 0x251, g, true); // part
        drawText(12, a + b * 0.00, "" + (char) 0x252, g, true); // pot
        drawText(17.7, a + b * 0.05, "" + (char) 0x254, g, true); // port (rounded)
        drawText(27, a + b * 0.65, "" + (char) 0x26a, g, true); // pit
        drawText(27.4, a + b * 0.21, "" + 'i', g, true); // peat
        drawText(20, a + b * 1.00, "" + 'e', g, true); // pet
        drawText(18.1, a + b * 0.37, "" + (char) 0x28c, g, true); // putt
        // put ʊ
        drawText(23, a + b * 0.1, "" + 'u', g, true); // poot (rounded)
        drawText(21, a + b * 0.6, "" + (char) 0x259, g, true); // pert [should be ɜ]

        final double nasals = -1.1;
        final double stops = -0.4;
        final double fricatives = 0.3;
        final double approximants = 1.1;

        // approximants
        drawText(38, approximants, "" + 'l', g, true);
        drawText(41, approximants, "" + 'w', g, true);

        // ?
        drawText(4.5, 0.37, "" + 'h', g, true);

        if (isTouched || alwaysVoice) {
            // voiced consonants
            drawText(31.5, fricatives, "" + (char) 0x292, g, true);
            drawText(36, fricatives, "" + 'z', g, true);
            drawText(41, fricatives, "" + 'v', g, true);
            drawText(22, stops, "" + 'g', g, true);
            drawText(36, stops, "" + 'd', g, true);
            drawText(41, stops, "" + 'b', g, true);
            drawText(22, nasals, "" + (char) 0x14b, g, true);
            drawText(36, nasals, "" + 'n', g, true);
            drawText(41, nasals, "" + 'm', g, true);
        } else {
            // unvoiced consonants
            drawText(31.5, fricatives, "" + (char) 0x283, g, true);
            drawText(36, fricatives, "" + 's', g, true);
            drawText(41, fricatives, "" + 'f', g, true);
            drawText(22, stops, "" + 'k', g, true);
            drawText(36, stops, "" + 't', g, true);
            drawText(41, stops, "" + 'p', g, true);
            drawText(22, nasals, "" + (char) 0x14b, g, true);
            drawText(36, nasals, "" + 'n', g, true);
            drawText(41, nasals, "" + 'm', g, true);
        }
    }

    static class Touch {
        int index;
        boolean alive;
        double startTime;
        long endTime;
        double x;
        double y;
        double diameter;
        double fricative_intensity;

        public String toString() {
            return index + ", " + diameter + ", " + fricative_intensity + ", " + startTime + ", " + endTime + ", " + alive + ", " + x + ", " + y;
        }

        public void stop() {
            if (!alive) return;
            alive = false;
            endTime = System.currentTimeMillis() / 1000;
        }
    }

    static class Transient {
        int position;
        double timeAlive, lifeTime, strength, exponent;
    }

    static class MathUtil {

//        public static double moveTowards(final double current, final double target, final double amount) {
//            return current < target ? Math.min(current + amount, target) : Math.max(current - amount, target);
//        }

        private static final MathUtil instance = new MathUtil();
        // Skewing and unskewing factors for 2, 3, and 4 dimensions
        private static final double F2 = 0.5 * (Math.sqrt(3) - 1);
        private static final double G2 = (3 - Math.sqrt(3)) / 6;
        private final Grad[] grad3 = {
                new Grad(1, 1, 0), new Grad(-1, 1, 0), new Grad(1, -1, 0), new Grad(-1, -1, 0), new Grad(1, 0, 1), new Grad(-1, 0, 1), new Grad(1, 0, -1), new Grad(-1, 0, -1), new Grad(0, 1, 1),
                new Grad(0, -1, 1),
                new Grad(0, 1, -1), new Grad(0, -1, -1)
        };
        private final int[] p = {
                151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219,
                203, 117, 35, 11, 32, 57, 177, 33,
                88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40,
                244, 102, 143, 54, 65, 25, 63,
                161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118,
                126, 255, 82, 85, 212, 207, 206, 59,
                227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232,
                178, 185, 112, 104, 218, 246, 97,
                228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150,
                254, 138, 236, 205, 93, 222, 114,
                67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
        };
        // To remove the need for index wrapping, double the permutation table length
        private final int[] perm;
        private final Grad[] gradP;
        private MathUtil() {
            perm = new int[512];
            gradP = new Grad[512];

            long seed = System.currentTimeMillis();
//            if (seed > 0 && seed < 1) {
//                // Scale the seed out
//                seed *= 65536;
//            }

            if (seed < 256) seed |= seed << 8;

            for (int i = 0; i < 256; i++) {
                long v = (i & 1) == 1 ? p[i] ^ (seed & 255) : p[i] ^ ((seed >> 8) & 255);

                perm[i] = perm[i + 256] = (int) v;
                gradP[i] = gradP[i + 256] = grad3[(int) (v % 12)];
            }
        }

        public static double moveTowards(final double current, final double target, final double amountUp, final double amountDown) {
            return current < target ? Math.min(current + amountUp, target) : Math.max(current - amountDown, target);
        }

        public static MathUtil getInstance() {
            //return instance == null ? (instance = new MathUtil()) : instance;
            return instance;
        }

        // 2D simplex noise
        public double simplex2(final double xin, final double yin) {
            double n0, n1, n2; // Noise contributions from the three corners
            // Skew the input space to determine which simplex cell we're in
            final double s = (xin + yin) * F2; // Hairy factor for 2D
            int i = (int) Math.floor(xin + s);
            int j = (int) Math.floor(yin + s);
            final double t = (i + j) * G2;
            final double x0 = xin - i + t; // The x,y distances from the cell origin, unskewed.
            final double y0 = yin - j + t;
            // For the 2D case, the simplex shape is an equilateral triangle.
            // Determine which simplex we are in.
            final int i1, j1; // Offsets for second (middle) corner of simplex in (i,j) coords
            if (x0 > y0) { // lower triangle, XY order: (0,0)->(1,0)->(1,1)
                i1 = 1;
                j1 = 0;
            } else { // upper triangle, YX order: (0,0)->(0,1)->(1,1)
                i1 = 0;
                j1 = 1;
            }
            // A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
            // a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y), where
            // c = (3-sqrt(3))/6
            final double x1 = x0 - i1 + G2; // Offsets for middle corner in (x,y) unskewed coords
            final double y1 = y0 - j1 + G2;
            final double x2 = x0 - 1 + 2 * G2; // Offsets for last corner in (x,y) unskewed coords
            final double y2 = y0 - 1 + 2 * G2;
            // Work out the hashed gradient indices of the three simplex corners
            i &= 255;
            j &= 255;
            Grad gi0 = gradP[i + perm[j]];
            Grad gi1 = gradP[i + i1 + perm[j + j1]];
            Grad gi2 = gradP[i + 1 + perm[j + 1]];
            // Calculate the contribution from the three corners
            double t0 = 0.5 - x0 * x0 - y0 * y0;
            if (t0 < 0) {
                n0 = 0;
            } else {
                t0 *= t0;
                n0 = t0 * t0 * gi0.dot2(x0, y0); // (x,y) of grad3 used for 2D gradient
            }
            double t1 = 0.5 - x1 * x1 - y1 * y1;
            if (t1 < 0) {
                n1 = 0;
            } else {
                t1 *= t1;
                n1 = t1 * t1 * gi1.dot2(x1, y1);
            }
            double t2 = 0.5 - x2 * x2 - y2 * y2;
            if (t2 < 0) {
                n2 = 0;
            } else {
                t2 *= t2;
                n2 = t2 * t2 * gi2.dot2(x2, y2);
            }
            // Add contributions from each corner to get the final noise value.
            // The result is scaled to return values in the interval [-1,1].
            return 70 * (n0 + n1 + n2);
        }

        public double simplex1(final double x) {
            return simplex2(x * 1.2, -x * 0.7);
        }

        record Grad(double x, double y, double z) {

            public double dot2(final double x, final double y) {
                return this.x * x + this.y * y;
            }

//            public double dot3(final double x, final double y, final double z) {
//                return this.x * x + this.y * y + this.z * z;
//            }

        }
    }

}