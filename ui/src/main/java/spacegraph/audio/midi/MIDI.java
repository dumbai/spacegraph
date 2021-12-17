package spacegraph.audio.midi;

import javax.sound.midi.*;
import java.util.Arrays;



/**
 * generic MIDI input interface
 */
class MIDI {

    private final float[] volume = new float[128];

    public MIDI() {


        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        MidiInReceiver receiver = null;
        for (MidiDevice.Info info : infos) {
            try {
                MidiDevice.Info ii = info;

                MidiDevice device = MidiSystem.getMidiDevice(ii);

                System.out.println(device + "\t" + device.getClass());
                System.out.println("\t" + device.getDeviceInfo());
                System.out.println("\ttx: " + device.getTransmitters());
                System.out.println("\trx: " + device.getReceivers());

                if (receive(device)) {
                    receiver = new MidiInReceiver(device);
                    break;
                }

                /*if (device instanceof Synthesizer) {
                    synthInfos.addAt((Synthesizer) ii);
                } else if (device instanceof MidiDevice) {
                    midis.addAt((MidiDevice) ii);
                }*/
            } catch (MidiUnavailableException e) {

            }
        }

        MidiInReceiver receiver1 = receiver;

        Arrays.fill(volume, Float.NaN);

    }


    private static boolean receive(MidiDevice device) {
        return device.getDeviceInfo().getName().startsWith("MPD218");
    }

    class MidiInReceiver implements Receiver {


        MidiInReceiver(MidiDevice device) throws MidiUnavailableException {

            if (!device.isOpen()) {
                device.open();
            }

            device.getTransmitter().setReceiver(this);
        }

        @Override
        public void send(MidiMessage m, long timeStamp) {


            if (m instanceof ShortMessage s) {
                int cmd = s.getCommand();
                switch (cmd) {
                    case ShortMessage.NOTE_OFF:
                        if ((volume[s.getData1()] == volume[s.getData1()]) && (volume[s.getData1()] > 0))
                            volume[s.getData1()] = 0;




                        
                        break;
                    case ShortMessage.NOTE_ON:
                        volume[s.getData1()] = 0.6f + 0.4f * s.getData2() / 128.0f;



                        
                        
                        break;
                    default:
                        
                        break;
                    
                }
            }

        }











        @Override
        public void close() {

        }
    }





}