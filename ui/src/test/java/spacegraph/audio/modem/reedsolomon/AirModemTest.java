package spacegraph.audio.modem.reedsolomon;

import jcog.Util;
import org.junit.jupiter.api.Test;
import spacegraph.audio.modem.stopcollaboratelisten.AirModem;
import spacegraph.audio.modem.stopcollaboratelisten.AudioUtils;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AirModemTest {

    @Test
    void testDecodeMessageFromWAV() throws IOException, UnsupportedAudioFileException {
//        AirModem a = new AirModem();
        AudioUtils.decodeWavFile(new File("/tmp/test.wav"), new ByteArrayOutputStream());
    }

    public static class Speaker {

        public static void main(String[] args) {
            AirModem m = new AirModem();

            m.playSOS();

            Util.sleepMS(1*1000);

            m.say("test");

            Util.sleepMS(10 * 1000);
        }
    }

    public static class Listener {
        public static void main(String[] args) {
            AirModem m = new AirModem();

            Util.sleepMS(10*1000);
        }

    }
}
