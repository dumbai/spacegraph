package spacegraph.audio;

import spacegraph.audio.synth.SineWave;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class AudioJOALTest {

    public static void main(final String[] args) throws IOException, InterruptedException, UnsupportedAudioFileException, com.jogamp.openal.UnsupportedAudioFileException {

        Audio a = Audio.the();


        a.play(new SineWave(1000));

//        Source src = AudioSystem3D.loadSource(new FileInputStream(
//                "/home/me/d/dream.wav"));
//        src.setPosition(0, 0, 0);
//        src.setLooping(true);
//        src.play();

        Thread.sleep(62000);

//        src.setPosition(1, 1, 1);
//
//        // move the listener
//        for (int i = 0; i < 1000; i++) {
//            final float t = (i) / 1000f;
//            final float lp = lerp(0f, 2f, t);
//            listener.setPosition(lp, lp, lp);
//            Thread.sleep(10);
//        }
//
//        // fade listener out.
//        for (int i = 0; i < 1000; i++) {
//            final float t = (i) / 1000f;
//            final float lp = lerp(1f, t, 0);
//            listener.setGain(lp);
//            Thread.sleep(10);
//        }
//
//        src.stop();
//        src.delete();
//        context.destroy();
//        device.close();

    }
}