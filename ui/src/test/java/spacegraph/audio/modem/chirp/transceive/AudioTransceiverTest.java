package spacegraph.audio.modem.chirp.transceive;

import jcog.data.list.Lst;
import org.junit.jupiter.api.Test;
import spacegraph.audio.modem.chirp.transceive.util.AudioEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudioTransceiverTest {

    @Test
    void test1(){

        List<String> recv = new Lst();
        AudioTransceiver x = new AudioTransceiver(44100) {
            @Override
            protected void onMessage(String m) {
                recv.add(m);
            }
        };
        //String msg = "abcdefghij";
        String msg = "aaaaaaaaaa";
        float[] b = x.encode(msg);
        assertTrue(b.length > 100);

        x.process(new AudioEvent(x.fmt, b));
        assertEquals('[' + msg + ']', recv.toString());
    }
}