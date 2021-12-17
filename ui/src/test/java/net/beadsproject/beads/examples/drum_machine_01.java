package net.beadsproject.beads.examples;

import jcog.Util;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.WaveFactory;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

public class drum_machine_01 {
    private Envelope kickGainEnvelope;
    private Envelope snareGainEnvelope;

    public static void main(String[] args) {
        drum_machine_01 synth = new drum_machine_01();
        synth.setup();
        while (true) {
            synth.keyDown(148);
            Util.sleepMS(300);
        }
    }

    
    private void setup() {
        AudioContext ac = new AudioContext();

        
        kickGainEnvelope = new Envelope(ac, 0.0f);

        WavePlayer kick = new WavePlayer(ac, 100.0f, WaveFactory.SINE);

        BiquadFilter kickFilter = new BiquadFilter(ac, BiquadFilter.BESSEL_LP, 500.0f, 1.0f);
        kickFilter.in(kick);

        Gain kickGain = new Gain(ac, 1, kickGainEnvelope);
        kickGain.in(kickFilter);

        
        ac.out.in(kickGain);


        
        snareGainEnvelope = new Envelope(ac, 0.0f);

        WavePlayer snareNoise = new WavePlayer(ac, 1.0f, WaveFactory.NOISE);
        WavePlayer snareTone = new WavePlayer(ac, 200.0f, WaveFactory.SINE);

        BiquadFilter snareFilter = new BiquadFilter(ac, BiquadFilter.BP_SKIRT, 2500.0f, 1.0f);
        snareFilter.in(snareNoise);
        snareFilter.in(snareTone);

        Gain snareGain = new Gain(ac, 1, snareGainEnvelope);
        snareGain.in(snareFilter);

        
        ac.out.in(snareGain);

        

























        ac.start();
    }

    public void keyDown(int midiPitch) {
        
        if (midiPitch % 12 == 0) {
            
            kickGainEnvelope.add(0.5f, 2.0f);
            
            kickGainEnvelope.add(0.2f, 5.0f);
            
            kickGainEnvelope.add(0.0f, 50.0f);
        }

        
        if (midiPitch % 12 == 4) {
            
            snareGainEnvelope.add(0.5f, 2.00f);
            
            snareGainEnvelope.add(0.2f, 8.0f);
            
            snareGainEnvelope.add(0.0f, 80.0f);
        }
    }

    public void keyUp(int midiPitch) {
        
    }
}