/*
 * This file is part of Beads. See http:
 * CREDIT: This class uses portions of code taken from JASS. See readme/CREDITS.txt.
 *
 */
package net.beadsproject.beads.core;

import jcog.data.list.FastCoWList;
import jcog.data.list.Lst;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.events.AudioContextStopTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.FuncGen;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.RecordToSample;
import org.jctools.queues.MpmcArrayQueue;
import spacegraph.audio.Audio;
import spacegraph.audio.SoundSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * TODO this needs stereo support for the SoNAR sound system
 * effectively it needs to provide 2 inputs to it because each of its channels
 * is designed for mono, because it mixes its own 3d stereo
 * <p>
 * <p>
 * AudioContext provides the core audio set up for running audio in a Beads
 * project. An AudioContext determines the JavaSound {@link IOAudioFormat} used,
 * the IO device, the audio buffer size and the system IO buffer size. An
 * AudioContext also provides a {@link UGen} called {@link #out}, which is
 * the output point for networks of UGens in a Beads project.
 *
 * @author ollie
 * @beads.category control
 */
public class AudioContext {

    /**
     * The audio IO device.
     */
    private final AudioIO audioIO;

    /**
     * The Beads audio format.
     */
    private final IOAudioFormat audioFormat;

    /**
     * The stop flag.
     */
    public boolean stopped;

    /**
     * The root {@link UGen}.
     */
    public final Gain out;

    /**
     * The current time step.
     */
    private long timeStep;

//    /**
//     * Flag for logging time to System.out.
//     */
//    private boolean logTime;

    /**
     * The buffer size in frames.
     */
    private int bufferSizeInFrames;

    /**
     * Used for allocating buffers to UGens.
     */
    private final int maxReserveBufs;
    private List<float[]> bufferStore;
    private int bufStoreIndex;
    private float[] zeroBuf;

//    /**
//     * Used for testing for dropped frames.
//     */
//    @SuppressWarnings("unused")
//    private long nanoLeap;

    @SuppressWarnings("unused")


    private static Queue newQueue() {
        return new MpmcArrayQueue<>(64);
    }

    /**
     * Used for concurrency-friendly method execution.
     */
    private final Queue<Auvent> beforeFrameQueue = newQueue();
    private final Queue<Auvent> afterFrameQueue = newQueue();
    private final FastCoWList<Auvent> beforeEveryFrameList = new FastCoWList(Auvent.class);
    private final FastCoWList<Auvent> afterEveryFrameList = new FastCoWList(Auvent.class);


    public AudioContext() {
        this(Audio.the(), defaultAudioFormat(2, 2));
    }

    /**
     * This constructor creates the default AudioContext, which means net.beadsproject.beads.core.io.JavaSoundAudioIO if it can find it, or net.beadsproject.beads.core.io.NonrealtimeIO otherwise.
     * To get the former, link to the jaudiolibs-beads.jar file.
     * <p>
     * The libraries are decoupled like this so that the core beads library doesn't depend on JavaSound, which is not supported in various contexts, such as Android. At the moment there are in fact some
     * JavaSound dependencies still to be removed before this process is complete. Pro-users should familiarise themselves with the different IO options, particularly Jack.
     */
    private AudioContext(Audio audio, IOAudioFormat audioFormat) {
        //super(null, audioFormat.outputs);

        maxReserveBufs = 32;
        setBufferSize(audio.bufferSizeInFrames());


        this.audioFormat = audioFormat;
        out = new Gain(this, audioFormat.outputs);

        stopped = true;

        UGenOutput ioSystem = new UGenOutput();
        ioSystem.context = this;
        this.audioIO = ioSystem;

        start();

        audio.play(ioSystem, SoundSource.center, 1.0f, 1.0f);

    }


//    /**
//     * Creates a new AudioContext with the specified buffer size, AudioIO and audio format.
//     *
//     * @param bufferSizeInFrames the buffer size in samples.
//     * @param ioSystem           the AudioIO system.
//     * @param audioFormat        the audio format, which specifies sample rate, bit depth,
//     *                           number of channels, signedness and byte order.
//     */
//    public AudioContext(AudioIO ioSystem, int bufferSizeInFrames, IOAudioFormat audioFormat) {
//
//        this.audioIO = ioSystem;
//        this.audioIO.context = this;
//
//        this.audioFormat = audioFormat;
//
//        setBufferSize(bufferSizeInFrames);
//
//        logTime = false;
//        maxReserveBufs = 50;
//        stopped = true;
//
//        out = new Gain(this, audioFormat.outputs);
//        AudioIO.prepare();
//    }


    /**
     * Sets up the reserve of buffers.
     */
    private void setupBufs() {
        bufferStore = new Lst<>(maxReserveBufs);
        while (bufferStore.size() < maxReserveBufs) {
            bufferStore.add(new float[bufferSizeInFrames]);
        }
        zeroBuf = new float[bufferSizeInFrames];
    }

    /**
     * callback from AudioIO.
     */

    public void update() {
//        try {
            bufStoreIndex = 0;
            Arrays.fill(zeroBuf, 0.0f);
            sendBeforeFrameMessages();
            out.update();
            sendAfterFrameMessages();
            timeStep++;
//            if (Thread.interrupted()) {
//                System.out.println("Thread interrupted");
//            }
//            if (logTime && timeStep % 100 == 0) {
//                System.out.println(samplesToMs(timeStep * bufferSizeInFrames)
//                        / 1000f + " (seconds)");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Gets a buffer from the buffer reserve. This buffer will be owned by you
     * until the next time step, and you shouldn't attempt to use it outside of
     * the current time step. The length of the buffer is bufferSize, but there
     * is no guarantee as to its contents.
     *
     * @return buffer of size bufSize, unknown contents.
     */
    public float[] getBuf() {
        if (bufStoreIndex < bufferStore.size()) {
            return bufferStore.get(bufStoreIndex++);
        } else {
            float[] buf = new float[bufferSizeInFrames];
            bufferStore.add(buf);
            bufStoreIndex++;
            return buf;
        }
    }



    /**
     * Gets a zero initialised buffer from the buffer reserve. This buffer will
     * be owned by you until the next time step, and you shouldn't attempt to
     * use it outside of the current time step. The length of the buffer is
     * bufferSize, and the buffer is full of zeros.
     *
     * @return buffer of size bufSize, all zeros.
     */
    public float[] getCleanBuf() {
        float[] buf = getBuf();
        Arrays.fill(buf, 0.0f);
        return buf;
    }

    /**
     * Gets a pointer to a buffer of length bufferSize, full of zeros. Changing
     * the contents of this buffer would be completely disastrous. If you want a
     * buffer of zeros that you can actually do something with, use {@link
     * #getCleanBuf()}.
     *
     * @return buffer of size bufSize, all zeros.
     */
    public float[] getZeroBuf() {
        return zeroBuf;
    }

//    /**
//     * Starts the AudioContext running in non-realtime. This occurs in the
//     * current Thread.
//     */
//    private void runNonRealTime() {
//        if (stopped) {
//            stopped = false;
//            reset();
//            while (out != null && !stopped) {
//                bufStoreIndex = 0;
//                Arrays.fill(zeroBuf, 0f);
//                if (!out.isPaused()) {
//                    sendBeforeFrameMessages();
//                    out.update();
//                    sendAfterFrameMessages();
//                }
//                timeStep++;
//
//            }
//        }
//    }

//    /**
//     * Runs the AudioContext in non-realtime for n milliseconds (that's n
//     * non-realtime milliseconds).
//     *
//     * @param n number of milliseconds.
//     */
//    public void runForNMillisecondsNonRealTime(double n) {
//
//        DelayTrigger dt = new DelayTrigger(this, n,
//                new AudioContextStopTrigger(this));
//        out.dependsOn(dt);
//        runNonRealTime();
//    }

    /**
     * Sets the buffer size.
     *
     * @param bufferSize the new buffer size.
     */
    public void setBufferSize(int bufferSize) {
        if (bufferSizeInFrames != bufferSize) {
            bufferSizeInFrames = bufferSize;
            setupBufs();
        }
    }

    /**
     * Gets the buffer size for this AudioContext.
     *
     * @return Buffer size in samples.
     */
    public int getBufferSize() {
        return bufferSizeInFrames;
    }


    /**
     * Gets the sample rate for this AudioContext.
     *
     * @return sample rate in samples per second.
     */
    public float getSampleRate() {
        return audioFormat.sampleRate;
    }

    /**
     * Gets the AudioFormat for this AudioContext.
     *
     * @return AudioFormat used by this AudioContext.
     */
    public IOAudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Generates a new AudioFormat with the same everything as the
     * AudioContext's AudioFormat except for the number of channels.
     *
     * @param numChannels the number of channels.
     * @return a new AudioFormat with the given number of channels, all other
     * properties coming from the original AudioFormat.
     */
    public IOAudioFormat getAudioFormat(int inputs, int outputs) {
        return new IOAudioFormat(audioFormat.sampleRate, audioFormat.bitDepth, inputs, outputs);
    }

    /**
     * Generates the default {@link IOAudioFormat} for AudioContext, with the
     * given number of channels. The default values are: sampleRate=44100,
     * sampleSizeInBits=16, signed=true, bigEndian=true.
     *
     * @param numChannels the number of channels to use.
     * @return the generated AudioFormat.
     */
    private static IOAudioFormat defaultAudioFormat(int inputs, int outputs) {
        return new IOAudioFormat(44100, 16, inputs, outputs, true, true);
    }

    /**
     * Prints AudioFormat information to System.out.
     */
    public void postAudioFormatInfo() {
        System.out.println("Sample Rate: " + audioFormat.sampleRate);
        System.out.println("Inputs: " + audioFormat.inputs);
        System.out.println("Outputs: " + audioFormat.outputs);
        System.out.println("Bit Depth: " + audioFormat.bitDepth);
        System.out.println("Big Endian: " + audioFormat.bigEndian);
        System.out.println("Signed: " + audioFormat.signed);
    }

    /**
     * Prints a representation of the audio signal chain stemming upwards from
     * the specified UGen to System.out, indented by the specified depth.
     *
     * @param current UGen to start from.
     * @param depth   depth by which to indent.
     */
    private static void printCallChain(UGen current, int depth) {
        Set<UGen> children = current.getConnectedInputs();
        for (int i = 0; i < depth; i++) {
            System.out.print("  ");
        }
        System.out.println("- " + current);
        for (UGen child : children) {
            printCallChain(child, depth + 1);
        }
    }

    /**
     * Prints the entire call chain to System.out (equivalent to
     * AudioContext.printCallChain(this.out, 0);)
     */
    public void printCallChain() {
        printCallChain(out, 0);
    }

    /**
     * Converts samples to milliseconds at the current sample rate.
     *
     * @param msTime duration in milliseconds.
     * @return number of samples.
     */
    public double msToSamples(double msTime) {
        return msTime * (audioFormat.sampleRate / 1000.0);
    }

    /**
     * Converts milliseconds to samples at the current sample rate.
     *
     * @param sampleTime number of samples.
     * @return duration in milliseconds.
     */
    public double samplesToMs(double sampleTime) {
        return (sampleTime / audioFormat.sampleRate) * 1000.0;
    }

    /**
     * Gets the current time step of this AudioContext. The time step begins at
     * zero when the AudioContext is started and is incremented by 1 for each
     * update of the audio buffer.
     *
     * @return current time step.
     */
    public long getTimeStep() {
        return timeStep;
    }

    /**
     * Generates a TimeStamp with the current time step and the given index into
     * the time step.
     *
     * @param index the index into the current time step.
     * @return a TimeStamp.
     */
    public TimeStamp generateTimeStamp(int index) {
        return new TimeStamp(this, timeStep, index);
    }

    /**
     * Get the runtime (in ms) since starting.
     */
    public double getTime() {
        return samplesToMs(getTimeStep() * getBufferSize());
    }


    /**
     * Tells the AudioContext to record all output for the given millisecond
     * duration, kill the AudioContext, and save the recording to the given file
     * path. This is a convenient way to make quick recordings, but may not suit
     * every circumstance.
     *
     * @param timeMS   the time in milliseconds to record for.
     * @param filename the filename to save the recording to.
     * @throws IOException Signals that an I/O exception has occurred.
     * @see RecordToSample recorder
     * @see Sample sample
     **/
    public void record(double timeMS, String filename) throws IOException {
        Sample s = new Sample(timeMS, audioFormat.outputs, audioFormat.sampleRate);
        try {
            RecordToSample r = new RecordToSample(this, s);
            r.in(out);
            out.dependsOn(r);
            r.start();
            r.after(new AudioContextStopTrigger(this));
        } catch (Exception e) { /* won't happen */
        }
        while (isRunning()) {
        }
        s.write(filename);
    }

    /**
     * Starts the AudioContext running in realtime. Only happens if not already
     * running. Resets time.
     */

    public void start() {


        if (stopped) {

//            nanoLeap = Math.round(1000000000.0 * (bufferSizeInFrames / audioFormat.sampleRate));

            reset();
            stopped = false;

            audioIO.start();

        }
    }

    /**
     * Simply resets the timeStep to zero.
     */
    private void reset() {
        timeStep = 0;
    }

    /**
     * Stops the AudioContext if running either in realtime or non-realtime.
     */

    public void stop() {
        stopped = true;
    }

    /**
     * Checks if this AudioContext is running.
     *
     * @return true if running.
     */
    private boolean isRunning() {
        return !stopped;
    }


    /**
     * Queues the specified Bead to be messaged upon the next audio frame
     * completion. The Bead will be messaged only once.
     *
     * @param target The Bead to message.
     * @return This AudioContext.
     */
    public AudioContext invokeAfterFrame(Auvent target) {
        afterFrameQueue.offer(target);
        return this;
    }

    /**
     * Queues the specified Bead to be messaged after every audio frame.
     *
     * @param target The Bead to message.
     * @return This AudioContext.
     */
    public AudioContext invokeAfterEveryFrame(Auvent target) {
        afterEveryFrameList.add(target);
        return this;
    }

    /**
     * Removes the specified Bead from the list of Beads that are messaged after
     * every audio frame.
     *
     * @param target The Bead to stop messaging.
     * @return Whether the Bead was being messaged.
     */
    public boolean stopInvokingAfterEveryFrame(Auvent target) {
        return afterEveryFrameList.remove(target);
    }

    /**
     * Queues the specified bead to be messaged before the next audio frame. The
     * Bead will be messaged only once.
     *
     * @param target The Bead to message.
     * @return This AudioContext.
     */
    public AudioContext invokeBeforeFrame(Auvent target) {
        beforeFrameQueue.add(target);
        return this;
    }

    /**
     * Queues the specified Bead to be messaged before every audio frame.
     *
     * @param target The Bead to message.
     * @return This AudioContext.
     */
    public AudioContext invokeBeforeEveryFrame(Auvent target) {
        beforeEveryFrameList.add(target);
        return this;
    }

    /**
     * Removes the specified Bead from the list of Beads that are messaged
     * before every audio frame.
     *
     * @param target The Bead to stop messaging.
     * @return Whether the Bead was being messaged.
     */
    public boolean stopInvokingBeforeEveryFrame(Auvent target) {
        return beforeEveryFrameList.remove(target);
    }

    /**
     * Used to send messages before the audio frame is done.
     */
    private void sendBeforeFrameMessages() {
        Auvent target;
        while ((target = beforeFrameQueue.poll()) != null) {
            target.accept(null);
        }
        for (Auvent bead : beforeEveryFrameList) {
            bead.accept(null);
        }
    }

    /**
     * Used to send messages after the audio frame is done.
     */
    private void sendAfterFrameMessages() {
        Auvent target;
        while ((target = afterFrameQueue.poll()) != null) {
            target.accept(null);
        }
        for (Auvent bead : afterEveryFrameList) {
            bead.accept(null);
        }
    }

    public void out(FuncGen f) {
        out.dependsOn(f);
    }

    private Clock clock(float intervalMS) {
        Clock c = new Clock(this, intervalMS);
        out.dependsOn(c);
        return c;
    }
    public Clock clock(float intervalMS, Auvent e) {
        Clock x = clock(intervalMS);
        x.on(e);
        return x;
    }
}