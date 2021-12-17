package net.beadsproject.beads.examples;

import jcog.Util;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Auvent;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.WaveFactory;
import net.beadsproject.beads.ugens.*;

import java.util.concurrent.ThreadLocalRandom;

class Lesson07_Music {

    public enum Music1 {
        ;

        public static void main(String[] args) {

            AudioContext ac = new AudioContext();
            /*
             * In this example a Clock is used to trigger events. We do this by
             * adding a listener to the Clock (which is of type Bead).
             *
             * The Bead is made on-the-fly. All we have to do is to give the Bead a
             * callback method to make notes.
             *
             * This example is more sophisticated than the previous ones. It uses
             * nested code.
             */
            Clock clock = ac.clock(800.0f, new Auvent() {

                        int pitch;

                        @Override
                        public void on(Auvent message) {
                            Clock c = (Clock) message;
                            if (c.isBeat()) {

                                if (random(1) < 0.5) return;
                                pitch = Pitch.forceToScale((int) random(12), Pitch.dorian);
                                float freq = Pitch.mtof(pitch + (int) random(5) * 12 + 32);
                                WavePlayer wp = new WavePlayer(ac, freq, WaveFactory.SINE);
                                Gain g = new Gain(ac, 1, new Envelope(ac, 0));
                                g.in(wp);
                                ac.out.in(g);
                                ((Envelope) g.getGainUGen()).add(0.1f, random(200));
                                ((Envelope) g.getGainUGen()).add(0, random(7000), g.die());
                            }
                            if (c.getCount() % 4 == 0) {

                                int pitchAlt = pitch;
                                if (random(1) < 0.2)
                                    pitchAlt = Pitch.forceToScale((int) random(12), Pitch.dorian) + (int) random(2) * 12;
                                float freq = Pitch.mtof(pitchAlt + 32);
                                WavePlayer wp = new WavePlayer(ac, freq, WaveFactory.SQUARE);
                                Gain g = new Gain(ac, 1, new Envelope(ac, 0));
                                g.in(wp);
                                Panner p = new Panner(ac, random(1));
                                p.in(g);
                                ac.out.in(p);
                                Envelope gain = (Envelope) g.getGainUGen();
                                gain.add(random(0.1f), random(50));
                                ((Envelope) g.getGainUGen()).add(0, random(400), p.die());
                            }
                            if (c.getCount() % 4 == 0) {
                                Noise n = new Noise(ac);
                                Gain g = new Gain(ac, 1, new Envelope(ac, 0.05f));
                                g.in(n);
                                Panner p = new Panner(ac, random(0.5f) + 0.5f);
                                p.in(g);
                                ac.out.in(p);
                                ((Envelope) g.getGainUGen()).add(0, random(100), p.die());
                            }
                        }
                    }
            );


            ac.start();
            Util.sleepMS(1000000L);
        }
    }

    private static float random(float x) {
        return ThreadLocalRandom.current().nextFloat() * x;
    }
}