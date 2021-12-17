package spacegraph.audio;

import jcog.data.list.Lst;
import jcog.signal.wave1d.DigitizedSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AudioSource implements DigitizedSignal {
    private static final Logger logger = LoggerFactory.getLogger(AudioSource.class);
    protected final int bytesPerSample;
    protected final AtomicBoolean busy = new AtomicBoolean(false);
    public final TargetDataLine line;
    public volatile int audioBytesRead;
    /** system ms time at start */
    private long _start;
    protected final byte[] preByteBuffer;

    public AudioSource(TargetDataLine line) {
        this.line = line;

        //int numChannels = line.getFormat().getChannels();
        bytesPerSample = line.getFormat().getFrameSize();//line.getFormat().getSampleSizeInBits()/8;

        int audioBufferSamples = line.getBufferSize();
        preByteBuffer = new byte[audioBufferSamples * bytesPerSample];

    }

    @Override
    public boolean hasNext(int atleastSamples) {

        int availableBytes = line.available();


        int toDrain = availableBytes - (atleastSamples /* n buffers */ * bytesPerSample);
        if (toDrain > 0) {
            while (toDrain % bytesPerSample > 0) toDrain--;

            //drain excess TODO optional
            //line.drain();
            line.read(new byte[toDrain], 0, toDrain); //HACK TODO use line fast forward method if exist otherwise shared buffer
            availableBytes = line.available();
        }

        return availableBytes >= atleastSamples * bytesPerSample;
    }

    public static void print() {
        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();

        for (Mixer.Info i : minfoSet)
            System.out.println(i);
    }

    public static List<AudioSource> all() {

        List<AudioSource> ll = new Lst();

        Mixer.Info[] ii = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : ii) {
            Mixer mi = AudioSystem.getMixer(mixerInfo);
            Line.Info[] mm = mi.getTargetLineInfo();
            for (Line.Info M : mm) {
                if (TargetDataLine.class.isAssignableFrom(M.getLineClass())) {
                    //System.out.println(mixerInfo + " " + M + " " + M.getLineClass());
                    try {
                        TargetDataLine lm = (TargetDataLine) mi.getLine(M);


                        //lm.open();



                        AudioFormat f = lm.getFormat();
//                        f = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, f.getSampleRate(),
//                                f.getSampleSizeInBits(),
//                                1 /* channels */,
//                                f.getFrameSize(), f.getFrameRate(), f.isBigEndian());
                        lm.open(f); //attempts to open, will fail if it can't


                        AudioSource ss =
                                new AudioSourcePCM16( mixerInfo, lm);
                                //new AudioSourcePCMFloat(lm);
                        ll.add(ss);

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return ll;
    }

    public int sampleRate() {
        return (int) line.getFormat().getSampleRate();
    }

    @Override
    public long time() {
        //return _start + Math.round(line.getMicrosecondPosition()/1000.0);
        return System.currentTimeMillis();
    }

    public int channelsPerSample() {
        return line.getFormat().getChannels();
    }

    public final AudioSource start() throws LineUnavailableException {
        logger.info("start {} {}", line, line.getLineInfo());

        synchronized (this) {
            if (!line.isOpen()) {
                line.open();
                //line.open(audioFormat/*, line.getBufferSize()*/);
            }

            this._start = System.currentTimeMillis();

            line.start();
        }


        //TODO
        //line.addLineListener();

        return this;


    }

    public void stop() {
        logger.info("stop {} {}", line, line.getLineInfo());

        synchronized (this) {
            line.stop();
            line.close();
        }
    }

    public String name() {
        return line.getLineInfo().toString();
    }


    @Override
    public int next(float[] target, int targetIndex, int capacitySamples) {

        if (!busy.compareAndSet(false, true))
            return 0;


        try {

//            do {


            int availableBytes = line.available();

            //logger.trace
            //System.out.println(available + "/" + capacity + " @ " + line.getMicrosecondPosition());

//                if (available > capacity) {
//                    int excess = available - capacity;
//
//                    while (excess % bytesPerSample != 0) {
//                        excess--; //pad
//                    }
//                    if (excess > 0) {
//                        if (excess <= preByteBuffer.length) {
//                            //small skip. just read more
//                            line.read(preByteBuffer, 0, excess);
//                            System.err.println(this + " buffer skip: available=" + available + " capacity=" + capacity);
//                            available -= excess;
//                        } else {
//                            System.err.println(this + " buffer skip flush: available=" + available + " capacity=" + capacity);
//                            line.flush();
//                            return 0;
//                        }
//                    }
//                }

            //pad to bytes per sample


            int readAtMost = capacitySamples * bytesPerSample;



            int toRead = Math.min(readAtMost, availableBytes);
            if (toRead <= 0)
                return 0;

            while (toRead % bytesPerSample > 0) toRead--;

            if (!read(toRead)) return 0;

            //synch time to realtime
            this._start = ((System.currentTimeMillis()*1000L) - line.getMicrosecondPosition())/1000L;

            int nSamplesRead = audioBytesRead / bytesPerSample;

            decode(target, nSamplesRead);


            return nSamplesRead;

//            } while (line.available() > 0);

        } finally {
            busy.set(false);
        }


    }

    public boolean read(int toRead) {
        return (audioBytesRead = line.read(preByteBuffer, 0, toRead)) != 0;
    }

    protected abstract void decode(float[] target, int nSamplesRead);


    //
//        @Override
//        public int next(float[] target, int targetIndex, int capacitySamples) {
//
//            if (!busy.compareAndSet(false, true))
//                return 0;
//
//            try {
//
////            do {
//
//
//                int availableBytes = line.available();
//
//                //logger.trace
//                //System.out.println(available + "/" + capacity + " @ " + line.getMicrosecondPosition());
//
////                if (available > capacity) {
////                    int excess = available - capacity;
////
////                    while (excess % bytesPerSample != 0) {
////                        excess--; //pad
////                    }
////                    if (excess > 0) {
////                        if (excess <= preByteBuffer.length) {
////                            //small skip. just read more
////                            line.read(preByteBuffer, 0, excess);
////                            System.err.println(this + " buffer skip: available=" + available + " capacity=" + capacity);
////                            available -= excess;
////                        } else {
////                            System.err.println(this + " buffer skip flush: available=" + available + " capacity=" + capacity);
////                            line.flush();
////                            return 0;
////                        }
////                    }
////                }
//
//
//                int toRead = Math.min(capacitySamples * bytesPerSample, availableBytes);
////            int toDrain = availableBytes - toRead;
////            if (toDrain > 0) {
////                //drain excess TODO optional
////                line.read(new byte[toDrain], 0, toDrain); //HACK TODO use line fast forward method if exist otherwise shared buffer
////            }
//
//                //pad to bytes per sample
//                while (toRead % bytesPerSample != 0) toRead--;
//
//
//                //int availableBytes = Math.min(capacity, line.available());
//                audioBytesRead = line.read(preByteBuffer, 0, toRead);
//                if (audioBytesRead == 0)
//                    return 0;
//                int nSamplesRead = audioBytesRead / bytesPerSample;
//
//
//                int start = 0;
//                int end = nSamplesRead;
//                int j = 0;
////                short min = Short.MAX_VALUE, max = Short.MIN_VALUE;
//                double gain =
//                        1.0 / shortRange;
//                //this.gain.floatValue() / shortRange;
//                for (int i = start; i < end; ) {
//                    short s = preShortBuffer[i++];
////                    if (s < min) min = s;
////                    if (s > max) max = s;
//                    target[j++] = (float) (s * gain); //compute in double for exra precision
//                }
//
//                return nSamplesRead;
//
////            } while (line.available() > 0);
//
//            } finally {
//                busy.set(false);
//            }
//
//
//        }
//    }
}