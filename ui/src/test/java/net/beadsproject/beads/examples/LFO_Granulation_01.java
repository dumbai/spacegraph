package net.beadsproject.beads.examples;

import jcog.Util;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.WaveFactory;
import net.beadsproject.beads.ugens.*;

public enum LFO_Granulation_01 {
	;

	public static void main(String[] args) {
        
        AudioContext a = new AudioContext();

        
        Sample sourceSample = null;
        try {
            sourceSample = new Sample("/tmp/x.wav");
        } catch (Exception e) {
            /*
             * If the program exits with an error message,
             * then it most likely can't find the file
             * or can't open it. Make sure it is in the
             * root folder of your project in Eclipse.
             * Also make sure that it is a 16-bit,
             * 44.1kHz audio file. These can be created
             * using Audacity.
             */
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        
        GranularSamplePlayer gsp = new GranularSamplePlayer(a, sourceSample);
        gsp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

        
        WavePlayer wpGrainDurationLFO = new WavePlayer(a, 0.03f, WaveFactory.SINE);
        FuncGen grainDurationLFO = new FuncGen(wpGrainDurationLFO) {
            @Override
            public float floatValueOf(float[] x) {
                return 1 + ((x[0] + 1) * 50.0f);
            }

        };
        
        gsp.setGrainSize(grainDurationLFO);

        
        WavePlayer wpGrainIntervalLFO = new WavePlayer(a, 0.02f, WaveFactory.SINE);
        FuncGen grainIntervalLFO = new FuncGen(wpGrainIntervalLFO) {
            @Override
            public float floatValueOf(float[] x) {
                return 1 + ((x[0] + 1) * 50.0f);
            }
        };
        
        gsp.setGrainInterval(grainIntervalLFO);

        
        gsp.setRandomness(new Static(a, 10.0f));

        
        Gain gain = new Gain(a, 1, 0.5f);
        gain.in(gsp);

        
        a.out.in(gain);

        
        a.start();
        Util.sleepMS((100 * 1000));
    }
}