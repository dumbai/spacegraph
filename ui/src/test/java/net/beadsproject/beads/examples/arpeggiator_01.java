package net.beadsproject.beads.examples;

import jcog.Util;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Auvent;
import net.beadsproject.beads.data.WaveFactory;
import net.beadsproject.beads.ugens.*;

public class arpeggiator_01 {
    private float frequency = 100.0f;
    private int tick = 0;

    private Envelope gainEnvelope;

    private int lastKeyPressed = -1;

    private Clock beatClock;

    public static void main(String[] args) {
        new arpeggiator_01().setup();
    }


    private void setup() {
        AudioContext ac = new AudioContext();


        gainEnvelope = new Envelope(ac, 0.0f);


        FuncGen arpeggiator = new FuncGen(gainEnvelope) {

            @Override
            public float floatValueOf(float[] anObject) {
                return frequency * (1 + tick);
            }

            @Override
            public void on(Auvent msg) {
                tick++;
                if (tick >= 4) tick = 0;
            }
        };

        ac.out(arpeggiator);


        WavePlayer wav = new WavePlayer(ac, arpeggiator, WaveFactory.SINE);


        beatClock = ac.clock(800.0f, arpeggiator).ticksPerBeat(4);


        Gain gain = new Gain(ac, 1, gainEnvelope);
        gain.setGain(1.0f);
        gain.in(wav);


        ac.out.dependsOn(beatClock);
        ac.out.in(gain);


        keyDown(79);

        beatClock.start();
        Util.sleepMS(100000L);
    }

    private static float midiPitchToFrequency(int midiPitch) {
        /*
         *  MIDI pitch number to frequency conversion equation from
         *  http:
         */
        double exponent = (midiPitch - 69.0) / 12.0;
        return (float) (Math.pow(2, exponent) * 440.0f);
    }

    private void keyDown(int midiPitch) {
        if (gainEnvelope != null) {
            lastKeyPressed = midiPitch;


            frequency = midiPitchToFrequency(midiPitch);
            tick = -1;
            beatClock.reset();


            gainEnvelope.clear();

            gainEnvelope.add(0.5f, 10.0f);
        }
    }

    public void keyUp(int midiPitch) {

        if (midiPitch == lastKeyPressed && gainEnvelope != null)
            gainEnvelope.add(0.0f, 50.0f);
    }
}