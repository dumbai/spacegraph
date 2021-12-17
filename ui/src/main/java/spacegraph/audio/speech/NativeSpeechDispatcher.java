package spacegraph.audio.speech;

import com.google.common.base.Joiner;
import jcog.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;

/** 'speechd' speech dispatcher - executes via command line */
public class NativeSpeechDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(NativeSpeechDispatcher.class);

    public final Semaphore voiceCount = new Semaphore(3);

    public NativeSpeechDispatcher() {
    }

    private static String[] command(String s, float amp, float speed) {
//        String voice =
//            "en-us";
//            //"mb-us1";
        return new String[]{
            //"/usr/bin/espeak",
            "/usr/bin/espeak-ng",
            "-a " + Math.round(amp*100) /* max 200 */,
            "-s " + Util.lerpInt(speed, 70, 200),
            "-p " + Util.lerpInt(1 - speed, 24, 75),
//            "-v " + voice,
            '"' + s + '"',
        };
    }

    private static String stringify(Object x) {
        return x instanceof Object[] ? Joiner.on(" ").join((Object[]) x) : x.toString();
    }

    public void speak(Object x) {
        speak(x, 1, 1);
    }

    public void speak(Object x, float amp, float speed) {
        String s = stringify(x);

        logger.info(s);

        if (voiceCount.tryAcquire()) {

            speak(s, amp, speed);

        } else {
            unspoken(s, amp);
        }
    }

    protected void unspoken(String s, float amp) {
    }

    protected void speak(String s, float amp, float speed) {
        try {

            String[] cmd = command(s, amp, speed);
            Process p = new ProcessBuilder()
                    .command(cmd)
                    .start();

            p.onExit().handle((process, throwable) -> {
//                try {
//                    String i = new String(p.getErrorStream().readAllBytes());
//                    System.out.println(i);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                voiceCount.release();
                if (throwable!=null)
                    logger.error("{} {}", process, throwable);
                return null;
            });

        } catch (IOException e) {
            logger.warn("speech error: {} {}", s, e);
        }
    }

    public int voicesAvailable() {
        return voiceCount.availablePermits();
    }

}