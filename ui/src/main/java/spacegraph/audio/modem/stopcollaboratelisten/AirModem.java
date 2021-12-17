package spacegraph.audio.modem.stopcollaboratelisten;

/**
 * Copyright 2102 by the authors. All rights reserved.
 * <p>
 * Author: Jonas Michel
 * <p>
 * This is the application's main service. This service maintains
 * the app's state machine and interacts with the playing and listening
 * threads accordingly.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static spacegraph.audio.modem.stopcollaboratelisten.AudioUtils.kDefaultFormat;


public class AirModem {

    private final MicrophoneListener microphoneListener;
    private StreamDecoder decoder;
    private final ByteArrayOutputStream decodedStream = new ByteArrayOutputStream();

    private SessionStatus state;

    /** TODO use Loop */
    @Deprecated private final Timer mTimer = new Timer();

    private TimeoutTimerTask timeoutTask;

    private int sessionTimeout = -1;
    private int timeoutCounter = -1;

    private String correctBroadcast = "";
    private boolean haveCorrectBroadcast = false;
    private boolean amInitiator = false;

    // possible status states of a collaborative sharing session
    public enum SessionStatus {
        PLAYING,
        LISTENING,
        HELPING,
        SOS,
        FINISHED,
        NONE
    }

    public AirModem() {

        stop();

        state = SessionStatus.LISTENING;

        decodedStream.reset();

        // the StreamDecoder uses the Decoder to decode samples put in its
        // AudioBuffer
        // StreamDecoder starts a thread
        decoder = new StreamDecoder(decodedStream) {

            @Override
            protected void onError() {
                if (haveCorrectBroadcast) {
                    resetTimeout(false);
                } else {
                    long tSOS = playSOS();

                    if (tSOS > -1)
                        // start listening when playing is finished
                        mTimer.schedule(new StatusUpdateTimerTask(SessionStatus.LISTENING), tSOS);
                }
            }

            @Override
            protected void onSOS() {
                if (haveCorrectBroadcast) {
                    resetTimeout(true);

                    long tWait = (long) (Constants.kSetupJitter * Constants.kSamplesPerDuration / Constants.kSamplingFrequency * 1000);

                    mTimer.schedule(new TimerTask() {
                        @Override public void run() {
                            playData(correctBroadcast, SessionStatus.HELPING);


                            long tHelp = tWait;

                            mTimer.schedule(new StatusUpdateTimerTask(SessionStatus.LISTENING), tHelp);
                        }
                    }, tWait);

                }
            }

            @Override
            protected void onBroadcast(byte[] receivedBytes) {
                if (!haveCorrectBroadcast) {
                    correctBroadcast = new String(receivedBytes);
                    setTimeout(correctBroadcast, false);
                }
                haveCorrectBroadcast = true;
            }
        };

        // the MicrophoneListener feeds the microphone samples into the
        // AudioBuffer
        // MicrophoneListener starts a thread
        microphoneListener = new MicrophoneListener(decoder.input());

    }

    private void stop() {
        if (microphoneListener != null)
            microphoneListener.stop();

        if (decoder != null)
            decoder.stop();

        decoder = null;

        state = SessionStatus.NONE;
    }

    private long playData(String input, SessionStatus playStatus) {
        stop();

        long millisPlayTime = -1;
        try {
            state = playStatus;

            // try to play the file
//            System.out.println("Performing " + input);
            byte[] inputBytes = input.getBytes();


            byte[] inputEncoded = Encoder.appendCRC(inputBytes);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Encoder.encodeStream(new ByteArrayInputStream(inputEncoded), baos);
            byte[] outBytes = baos.toByteArray();

            AudioUtils.performData(outBytes);

            AudioUtils.writeWav(new File("/tmp/test.wav"), outBytes, kDefaultFormat);




            /**
             *  length of play time (ms) =
             *  nDurations * samples/duration * 1/fs * 1000
             */
            millisPlayTime = (long) ((Constants.kPlayJitter + Constants.kDurationsPerHail + Constants.kBytesPerDuration * inputEncoded.length + Constants.kDurationsPerCRC) *
                    Constants.kSamplesPerDuration / Constants.kSamplingFrequency * 1000);

        } catch (Exception e) {
            System.err.println("Could not encode " + input + " because of " + e);
        }

        return millisPlayTime;
    }

    public long playSOS() {
        stop();

        long millisPlayTime = -1;
        try {
            state = SessionStatus.SOS;

            // try to play the file
            AudioUtils.performSOS();

            /**
             *  length of play time (ms) =
             *  nDurations * samples/duration * 1/fs * 1000
             */
            millisPlayTime = (long) ((Constants.kPlayJitter + Constants.kDurationsPerSOS) *
                    Constants.kSamplesPerDuration / Constants.kSamplingFrequency * 1000);

        } catch (Exception e) {
            System.out.println("Could not perform SOS because of " + e);
        }

        return millisPlayTime;
    }

    /* Start a collaborative sharing session */
    public void say(String input) {
        stop();

        // we are the session initiator, so we start with the "correct" broadcast
        correctBroadcast = input;
        haveCorrectBroadcast = true;
        amInitiator = true;

		mTimer.purge();

		long tPlay = playData(correctBroadcast, SessionStatus.PLAYING);

        if (tPlay > -1)
            // start listening when playing is finished
            mTimer.schedule(new StatusUpdateTimerTask(SessionStatus.LISTENING), tPlay);

        setTimeout(correctBroadcast, true);
    }

    private void sessionFinished() {
        stop();
        mTimer.purge();
        state = SessionStatus.FINISHED;
    }

    private void sessionReset() {

        mTimer.purge();


        correctBroadcast = "";
        haveCorrectBroadcast = false;
        amInitiator = false;
    }

    private void setTimeout(String input, boolean afterPlay) {
        int secondsPlay = (int) ((Constants.kPlayJitter + Constants.kDurationsPerHail + Constants.kBytesPerDuration * input.length() + Constants.kDurationsPerCRC)
                * Constants.kSamplesPerDuration / Constants.kSamplingFrequency);
        // set timeout to be 3x time to play the broadcast
        sessionTimeout = 3 * secondsPlay;
        timeoutCounter = sessionTimeout;
        timeoutTask = new TimeoutTimerTask();
        if (afterPlay)
            mTimer.schedule(timeoutTask, secondsPlay * 1000, 1000);
        else
            mTimer.schedule(timeoutTask, 0, 1000);
    }

    private void resetTimeout(boolean afterPlay) {
        if (afterPlay) {
            timeoutTask.cancel();
            timeoutTask = null;
            timeoutTask = new TimeoutTimerTask();
            timeoutCounter = sessionTimeout;
            mTimer.schedule(timeoutTask, sessionTimeout / 3 * 1000, 1000);
        } else
            timeoutCounter = sessionTimeout;
    }

    /**
     * TimerTask to schedule status updates
     */
    private class StatusUpdateTimerTask extends TimerTask {
        SessionStatus newStatus_;

        StatusUpdateTimerTask(SessionStatus newStatus) {
            newStatus_ = newStatus;
        }

        @Override
        public void run() {
            state = newStatus_;
            System.out.println(this + " " + state);

//            if (state == SessionStatus.LISTENING)
//                listen();

            this.cancel();
        }
    }

    /**
     * TimerTask to manage session timeout
     */
    private class TimeoutTimerTask extends TimerTask {

        public void run() {
            if (--timeoutCounter == 0) {
                sessionFinished();
                this.cancel();
            }
        }

    }



}


//package spacegraph.audio.modem.stopcollaboratelisten;///**
// * Copyright 2002 by the authors. All rights reserved.
// * <p>
// * Author: Cristina V Lopes
// */
//
//import com.jonas.stopcollaboratelisten.AudioUtils;
//import com.jonas.stopcollaboratelisten.MicrophoneListener;
//import com.jonas.stopcollaboratelisten.StreamDecoder;
//
//import java.io.*;
//
///**
// * <p>This class gives example uses of the digital voices code.</p>
// * <ul>
// * <li> usage: dv.Main -hardware : displays audio hardware info
// * <li> usage: dv.Main -decode <file.wav> : decodes the wav file
// * <li> usage: dv.Main -listen : listens on the microphone for audio bits
// * <li> usage: dv.Main inputFile.txt : plays inputFile
// * <li> usage: dv.Main input.txt output.wav : encodes input.txt into output.wav
// * </ul>
// *
// * @author CVL
// */
//public class Main {
//
//    public static void main(String[] args) {
//        //if there are no arguments or -h or -?, print usage and exit
//        if (args.length < 1
//                || "-h".equals(args[0])
//                || "-?".equals(args[0])) {
//            printUsage();
//            System.exit(0);
//        }
//
//        //reads the microphone input in real time and decodes it to System.out
//        if ("-listen".equals(args[0])) {
//            //the StreamDecoder uses the codec.Decoder to decode samples put in its AudioBuffer
//            // StreamDecoder starts a thread
//            StreamDecoder decoder = new StreamDecoder(System.out);
//
//            //the MicrophoneListener feeds the microphone samples into the AudioBuffer
//            // MicrophoneListener starts a thread
//            MicrophoneListener listener = new MicrophoneListener(decoder.input());
//            System.out.println("Listening");
//
//            // The main thread does nothing more, just waits for the others to die
//        } else if ("-decode".equals(args[0])) {
//
//            //decodes the file named in args[1] and prints it to System.out
//
//            //we must have a file name to decode
//            if (args.length < 2) {
//                printUsage();
//                System.exit(0);
//            }
//
//            try {
//                File inputFile = new File(args[1]);
//                if (inputFile.exists()) {
//                    //now decode the inputFile to System.out
//                    AudioUtils.decodeWavFile(inputFile, System.out);
//                } else {
//                    System.out.println("Cannot find file " + args[1]);
//                }
//            } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
//                System.out.println("Error reading " + args[1] + ":" + e);
//            } catch (IOException e) {
//                System.out.println("IO Error reading " + args[1] + ":" + e);
//            }
//        } else if ("-hardware".equals(args[0])) {
//            //this is a little utility to show what hardware the JVM finds
//
//            AudioUtils.displayMixerInfo();
//            System.exit(0);
//        } else if ("-record".equals(args[0])) {
//            AudioUtils.recordToFile(new File(args[1]), 100);
//            System.exit(0);
//        } else {
//            //Try to perform or encode the file named in args[0]
//            String inputFile = args[0];
//            String outputFile = null;
//            if (args.length > 1 && args[1].length() > 0) {
//                //the existence of a second argument indicates that we should encode to args[1]
//                outputFile = args[1];
//            }
//            try {
//                if (outputFile == null) {
//                    //try to play the file
//                    System.out.println("Performing " + args[0]);
//                    AudioUtils.performFile(new File(inputFile));
//                } else {
//                    //There was an output file specified, so we should write the wav
//                    System.out.println("Encoding " + args[0]);
//                    AudioUtils.encodeFileToWav(new File(inputFile), new File(outputFile));
//                }
//            } catch (Exception e) {
//                System.out.println("Could not encode " + inputFile + " because of " + e);
//            }
//            System.exit(0);
//        }
//    }
//
//    /**
//     * Prints the help text to System.out
//     */
//    public static void printUsage() {
//        System.out.println("usage: dv.Main -hardware : displays audio hardware info");
//        System.out.println("usage: dv.Main -decode <file.wav> : decodes the wav file");
//        System.out.println("usage: dv.Main -listen : listens on the microphone for audio bits");
//        System.out.println("usage: dv.Main <inputFile.txt> : plays the encoded file");
//        System.out.println("usage: dv.Main <input.txt> <output.wav> : encodes input.txt into output.wav");
//    }
//
//}