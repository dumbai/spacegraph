package spacegraph.audio.midi;

import jcog.Str;

import javax.sound.midi.*;

/** realtime MIDI synthesizer interface */
public class MIDISynth {

	public final MidiChannel channel;
	public final Synthesizer synth;
	public final Instrument[] soundbank;
//	private final Receiver recv;

	public MIDISynth() throws MidiUnavailableException {

		(synth = MidiSystem.getSynthesizer()).open();

		soundbank = synth.getAvailableInstruments();//getDefaultSoundbank().getInstruments();

//		synth.loadInstrument(soundbank[0]);

		System.out.println(synth + "\nmax polyphony=" + synth.getMaxPolyphony() + ", latency=" + Str.timeStr(1000.0 * synth.getLatency() /*uS*/));

		channel = synth.getChannels()[0];

//		recv = MidiSystem.getReceiver();


//		Sequencer sequencer = MidiSystem.getSequencer();

//		Sequence s = new Sequence(Sequence.PPQ, 96);


//		sequencer.open();
//		sequencer.setSequence(s);
		//		sequencer.start();
//		sequencer.startRecording();


		try {
			Transmitter t = MidiSystem.getTransmitter();


			t.setReceiver(new Receiver() {
				@Override
				public void send(MidiMessage m, long l) {
//				System.out.println("receive: " + m + " " + l);
					ShortMessage s = (ShortMessage) m;
					int data1 = s.getData1(), data2 = s.getData2();

					//see: SoftMainMixer.java in JDK
                    switch (s.getCommand()) {
                        case ShortMessage.NOTE_ON -> press(data1, data2);
                        case ShortMessage.NOTE_OFF -> release(data1, data2);
                    }

				}

				@Override
				public void close() {

				}
			});
		} catch (Exception e) {
			System.err.println("no MIDI transmitter");
		}
	}

	public void press(int key, int force) {
		channel.noteOn(key, force);
	}

	public void release(int key, int force) {
		channel.noteOff(key, force);
	}
}