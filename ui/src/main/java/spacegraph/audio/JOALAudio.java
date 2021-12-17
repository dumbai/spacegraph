package spacegraph.audio;

import com.jogamp.openal.sound3d.*;

import java.io.IOException;
import java.io.InputStream;

/** not working yet */
public class JOALAudio extends Audio {
    private final Device device;

    static {
        AudioSystem3D.init();
    }

    private final Context context;
    private final Listener listener;
    private final Source o;
    volatile private byte[] nextBuffer = new byte[1];
//    private final Buffer[] b;


    public JOALAudio() {
        super(32);
        device = AudioSystem3D.openDevice(null);
        context = AudioSystem3D.createContext(device);
        AudioSystem3D.makeContextCurrent(context);

        listener = AudioSystem3D.getListener();
        listener.setPosition(0, 0, 0);

//        b = AudioSystem3D.generateBuffers(2);
//        for (Buffer bb : b) {
//            var outData = ByteBuffer.allocateDirect(2 * 2 * bufferSamples);
//            bb.configure(outData, FORMAT_STEREO16, Audio.rate);
//            //bb.getData().position(bb.getData().capacity());
//        }

//        o = AudioSystem3D.generateSources(1)[0];
//        o.queueBuffers(b);
        try {
            o = AudioSystem3D.loadSource(new InputStream() {

                int i = 0;

                @Override
                public int read() throws IOException {
                    byte[] b = JOALAudio.this.nextBuffer;
                    return b[(i++) % b.length];
                }
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        o.setPosition(0, 0, 0);
        o.setLooping(true);
        o.play();
    }

    @Override
    public void close() {
        o.stop(); //TODO other shutdown

        super.close();
        context.destroy();
        device.close();
    }

    @Override
    protected void push(byte[] ba) {
        this.nextBuffer = ba;
//        System.out.println(o.getBuffersProcessed() + " "+ o.getBuffersQueued());
////        if (o.getBuffersProcessed() < 1)
////            return;
//
//        for (Buffer bb : b)
//            System.out.println(bb.getData().position());
//
//        for (Buffer bb : b) {
//            if (bb.getData().position() == 0) {
//                o.unqueueBuffers(new Buffer[] { bb });
//                push(ba, bb);
//                o.queueBuffers(new Buffer[] { bb });
//                return;
//            } else
//                Util.nop();
//        }


    }

    private void push(byte[] ba, Buffer bb) {
//        if (o.getBuffersQueued() > 1) {
//            return;
//        }

        var data = bb.getData();
        data.rewind();
//        o.rewind();
        data.put(ba, 0, Math.min(ba.length, bufferSamples * 2 * 2));
        data.rewind();

//        ooData.rewind();
//        o.queueBuffers(new Buffer[] { out });

        //o.queueBuffers(new Buffer[] { oo });
        //o.setBuffer(oo);

//        System.out.println(oo.getNumChannels() + " " + o + " " + o.isPlaying() + " " +
//                o.getBuffersQueued() + " | " +
//                o.getBuffersProcessed());
    }

}