package spacegraph.audio.modem.stopcollaboratelisten;

/**
 * Copyright 2002 by the authors. All rights reserved.
 * <p>
 * Author: Cristina V Lopes
 * (Modified by Jonas Michel, 2012)
 */


import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * This starts a Thread which decodes data in an AudioBuffer and writes it to an OutputStream.
 * StreamDecoder holds the buffer where the MicrophoneListener puts bytes.
 */
public abstract class StreamDecoder implements Runnable {

    public static String kThreadName = "StreamDecoder";

    private final Object runLock = new Object();
    private boolean running = false;

    private final AudioUtils.ByteArrayAudioOutputStream buffer = new AudioUtils.ByteArrayAudioOutputStream(); // THE buffer where bytes are being put
    private ByteArrayOutputStream out;

    private boolean hasKey = false;
    private byte[] receivedBytes;

    private boolean contendingForSOS = false;

//    private Handler handler = null;

    /**
     * This creates and starts the decoding Thread
     *
     * @param _out the OutputStream which will receive the decoded data
     */
    public StreamDecoder(ByteArrayOutputStream _out) {
        out = _out;
        Thread myThread = new Thread(this, kThreadName);
        myThread.start();
    }

    public String status() {
        String s = "";

        int backlog = (int) ((1000 * buffer.size()) / Constants.kSamplingFrequency);

        if (backlog > 0)
            s += "Backlog: " + backlog + " mS ";

        if (hasKey)
            s += "Found key sequence ";

        return s;
    }

    public AudioUtils.ByteArrayAudioOutputStream input() {
        return buffer;
    }

    public boolean getHasKey() {
        return hasKey;
    }

    public byte[] getReceivedBytes() {
        return receivedBytes;
    }

    public void run() {
        synchronized (runLock) {
            running = true;
        }


        hasKey = false;

        byte[] samples = null;
        double[] startSignals = new double[Constants.kBitsPerByte * Constants.kBytesPerDuration];
        int skipped = 0;
        int durationsToRead = Constants.kDurationsPerHail;
        while (running) {

            //System.out.println(status());

            boolean notEnoughSamples = true;
            while (notEnoughSamples) {
                samples = buffer.read(Constants.kSamplesPerDuration * durationsToRead, 0.9f);
                if (samples != null)
                    notEnoughSamples = false;
                else
                    Thread.yield();
            }

            if (hasKey) {
                //we found the key, so decode this duration
                byte[] decoded = Decoder.decode(startSignals, samples);
                try {
                    buffer.delete(samples.length);
                    skipped += samples.length;
                    out.write(decoded);

                    //System.out.println("decoded " + decoded.length + " bytes");

                    //if (decoded[0] == 0) { //we are receiving no signal, so go back to key detection mode
                    {
                        byte[] signal = out.toByteArray();

                        if (Decoder.crcCheckOk(signal)) {
                            // signal received correctly
                            receivedBytes = Decoder.removeCRC(signal);

                            onBroadcast(receivedBytes);

                            out.reset();
                            hasKey = false;
                            durationsToRead = Constants.kDurationsPerHail;

                        } else {
                            // enter contention for an SOS slot
//                            contendingForSOS = true;
                        }


                    }
                } catch (IOException e) {
                    System.out.println("IOException while decoding:" + e);
                    break;
                }

//                try {
//                    //this provides the audio sampling mechanism a chance to maintain continuity
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    System.out.println("Stream Decoding thread interrupted:" + e);
//                    break;
//                }
                continue;
            }

            //we don't have the key, so we are in key detection mode from this point on

            //System.out.println("Search Start: " + deletedSamples + " End: " + (deletedSamples + samples.length));
            //System.out.println("Search Time: " + ((float)deletedSamples / Constants.kSamplingFrequency) + " End: "
            //		       + ((float)(deletedSamples + samples.length) / Constants.kSamplingFrequency));

            // detect SOS key
            int sosIndex = Decoder.findKeySequence(samples, startSignals, Constants.initialGranularity, Constants.kSOSFrequency);
            // detect Hail key
            int hailIndex = Decoder.findKeySequence(samples, startSignals, Constants.initialGranularity, Constants.kHailFrequency);

            if (sosIndex > -1
                    && ((hailIndex > -1 && sosIndex < hailIndex) || hailIndex == -1)) {
                try {
                    buffer.delete(Constants.kSamplesPerDuration * durationsToRead);
                    skipped += Constants.kSamplesPerDuration * durationsToRead;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (contendingForSOS) // someone else beat us to it
                    contendingForSOS = false;
                else {
                    onSOS();
                }

                continue;
            }

            if (hailIndex > -1) {
                //System.out.println("\nRough Start Index: " + (deletedSamples + hailIndex));
                //System.out.println("Rough Start Time: "
                //	   + (deletedSamples + startIndex) / (float)Constants.kSamplingFrequency);

                int shiftAmount = hailIndex /* - (Constants.kSamplesPerDuration)*/;
                if (shiftAmount < 0) {
                    shiftAmount = 0;
                }
                //System.out.println("Shift amount: " + shiftAmount);
                try {
                    buffer.delete(shiftAmount);
                } catch (IOException e) {
                }
                skipped += shiftAmount;

                durationsToRead = Constants.kDurationsPerHail;
                notEnoughSamples = true;
                while (notEnoughSamples) {
                    samples = buffer.read(Constants.kSamplesPerDuration * durationsToRead);
                    if (samples != null)
                        notEnoughSamples = false;
                    else Thread.yield();
                }

                //System.out.println("Search Start: " + deletedSamples + " End: " + (deletedSamples + samples.length));
                //System.out.println("Search Time: " + ((float)deletedSamples / Constants.kSamplesPerDuration) + " End: "
                //		   + ((float)(deletedSamples + samples.length) / Constants.kSamplingFrequency));

                hailIndex = Decoder.findKeySequence(samples, startSignals, Constants.finalGranularity, Constants.kHailFrequency);
                //System.out.println("Refined Start Index: " + (deletedSamples + hailIndex));
                //System.out.println("Start Time: " +
                //	   (deletedSamples + startIndex) / (float)Constants.kSamplingFrequency);
                try {
                    notEnoughSamples = true;
                    while (notEnoughSamples) {
                        samples = buffer.read(hailIndex + (Constants.kSamplesPerDuration * Constants.kDurationsPerHail));
                        if (samples != null)
                            notEnoughSamples = false;
                        else Thread.yield();
                    }

                    samples = ArrayUtils.subarray(samples, hailIndex + Constants.kSamplesPerDuration,
                            2 * Constants.kSamplesPerDuration);
                    Decoder.getKeySignalStrengths(samples, startSignals);
	      /*
		System.out.println(" f(0): " + startSignals[0] + " f(1): " + startSignals[1] +
		" f(2): " + startSignals[2] + " f(3): " + startSignals[3] +
		" f(4): " + startSignals[4] + " f(5): " + startSignals[5] +
		" f(6): " + startSignals[6] + " f(7): " + startSignals[7]);
	      */

                    buffer.delete(hailIndex + (Constants.kSamplesPerDuration * Constants.kDurationsPerHail));
                    skipped += hailIndex + (Constants.kSamplesPerDuration * Constants.kDurationsPerHail);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                hasKey = true;
                contendingForSOS = false;

                //System.out.println(">>>>>>>>>>>>>>>>>>>>>    found key <<<<<<<<<<<<<<<<<<<<<");


                durationsToRead = 1;
            } else {
                if (contendingForSOS) {
                    onError();

                }

                try {
                    buffer.delete(Constants.kSamplesPerDuration);
                    skipped += Constants.kSamplesPerDuration;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                contendingForSOS = false;
            }
        }
    }

    /** handler.sendEmptyMessage(AirModem.MSG_RECEIVED_BAD_BROADCAST); */
    protected abstract void onError();

    /** heard an cry for help */
    protected abstract void onSOS();


    /** received good broadcast
     * @param receivedBytes*/
    protected abstract void onBroadcast(byte[] receivedBytes);



    public void stop() {
        synchronized (runLock) {
            running = false;
        }
    }
}