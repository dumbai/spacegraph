package spacegraph.audio;

import com.jogamp.openal.AL;
import com.jogamp.openal.sound3d.*;
import jcog.Util;

import java.nio.ByteBuffer;

import static com.jogamp.openal.sound3d.AudioSystem3D.generateBuffers;

/**
 * not working yet
 */
public class JOALAudio extends Audio {

    private final Buffer bb;
    private final Context context;
    private final Device device;
    private Source s;
    private byte[] b;

    public static float lerp(final float v1, final float v2, final float t) {
        return v1 + ((v2 - v1) * t);
    }

    static Buffer buffer(ByteBuffer data, int format, int freq) {
        Buffer[] tmp = generateBuffers(1);
        Buffer result = tmp[0];

        result.configure(data, format, freq);

        return result;
    }

    public JOALAudio() {
        super(32, 512);

        AudioSystem3D.init();

        this.device = AudioSystem3D.openDevice(null);
        this.context = AudioSystem3D.createContext(device);
        AudioSystem3D.makeContextCurrent(context);

        Listener listener = AudioSystem3D.getListener();
        listener.setPosition(0, 0, 0);

        bb = buffer(soundBuffer, AL.AL_FORMAT_STEREO16, 44100);
        s =
//                    //AudioSystem3D.loadSource( new FileInputStream("/home/me/d/r1.wav"));
                AudioSystem3D.generateSource(bb);

        s.setPosition(0, 0, 0);
//        s.setLooping(true);
        s.play();

        s.setPosition(1, 1, 1);
//
//            // move the listener
//            for (int i = 0; i < 1000; i++) {
//                final float t = (i) / 1000f;
//                final float lp = lerp(0f, 2f, t);
//                listener.setPosition(lp, lp, lp);
//                Thread.sleep(100);
//            }
//
//            // fade listener out.
//            for (int i = 0; i < 1000; i++) {
//                final float t = (i) / 1000f;
//                final float lp = lerp(1f, 0f, t);
//                listener.setGain(lp);
//                Thread.sleep(10);
//            }


    }

    @Override
    public void close() {
        s.stop();
        s.delete();
        context.destroy();
        device.close();
        super.close();
    }

    @Override
    protected void push(byte[] ba) {
//        while (s.isPlaying())
//            Util.sleepMS(1);
        Buffer next = buffer(ByteBuffer.wrap(ba), AL.AL_FORMAT_STEREO16, 44100);
        s.rewind();
        s.setBuffer(next);
        s.play();

        Util.sleepMS((int)(44100.0/(4 * ba.length)));
        //s.queueBuffers(new Buffer[] { next });

//        System.out.println(s.isPlaying());
//            if (!s.isPlaying())
//                s.play();

    }

//    /**
//     * @author Athomas Goldberg, Michael Bien, et.al.
//     */
//    static class OpenALTest  {
//        private ALC alc = null;
//        private ALCdevice device = null;
//        private ALCcontext context = null;
//        private AL al = null;
//        private int[] sources = null;
//
//
//        public OpenALTest() throws IOException {
//            alc = ALFactory.getALC();
//            device = alc.alcOpenDevice(null);
//            context = alc.alcCreateContext(device, null);
//            alc.alcMakeContextCurrent(context);
//            al = ALFactory.getAL();
//            System.out.println("output devices:");
//            {
//                final String[] outDevices = alc.alcGetDeviceSpecifiers();
//                if (null != outDevices) for (final String name : outDevices) System.out.println("    " + name);
//            }
//            System.out.println("capture devices:");
//            {
//                final String[] inDevices = alc.alcGetCaptureDeviceSpecifiers();
//                if (null != inDevices) for (final String name : inDevices) System.out.println("    " + name);
//            }
//
////            final boolean eaxPresent = al.alIsExtensionPresent("EAX2.0");
////            final EAX eax = (eaxPresent) ? EAXFactory.getEAX() : null;
////            System.err.println("EAX present:" + eaxPresent + ", EAX retrieved: " + (null != eax));
//
//            int[] buffers = new int[1];
//            al.alGenBuffers(1, buffers, 0);
//
//            // WAVData wd = WAVData.loadFromStream(ResourceLocation.getTestStream0(), ResourceLocation.getTestStream0Size(), 1, 8, 22050, ByteOrder.LITTLE_ENDIAN, true);
//            // WAVData wd = WAVData.loadFromStream(ResourceLocation.getTestStream1(), ResourceLocation.getTestStream1Size(), 2, 16, 44100, ByteOrder.BIG_ENDIAN, true);
////            final WAVData wd = WAVData.loadFromStream(ResourceLocation.getTestStream2(), ResourceLocation.getTestStream2Size(), 2, 16, 44100, ByteOrder.LITTLE_ENDIAN, true, 0);
//            FileInputStream s = new FileInputStream("/home/me/d/r1.wav");
//            WAVData wd = WAVData.loadFromStream(s, s.available(), 1, 16, 44100, ByteOrder.LITTLE_ENDIAN, false);
////            System.out.println("*** size "+wd.data.limit());
////
////            al.alBufferData(buffers[0], wd.format, wd.data, wd.size, wd.freq);
//            al.alBufferData(buffers[0], wd.format, wd.data, wd.size, wd.freq);
//
//            sources = new int[1];
//            al.alGenSources(1, sources, 0);
//            al.alSourcei(sources[0], ALConstants.AL_BUFFER, buffers[0]);
//
////            final int[] loopArray = new int[1];
////            al.alGetSourcei(sources[0], ALConstants.AL_LOOPING, loopArray, 0);
////            System.err.println("Looping 1: " + (loopArray[0] == ALConstants.AL_TRUE));
////
////            final int[] loopBuffer = new int[1];
////            al.alGetSourcei(sources[0], ALConstants.AL_LOOPING, loopBuffer, 0);
////            System.err.println("Looping 2: " + (loopBuffer[0] == ALConstants.AL_TRUE));
//
////            if (eaxPresent && null != eax) {
////                final IntBuffer env = Buffers.newDirectIntBuffer(1);
////                env.put(EAXConstants.EAX_ENVIRONMENT_BATHROOM);
////                eax.setListenerProperty(EAXConstants.DSPROPERTY_EAXLISTENER_ENVIRONMENT, env);
////            }
//        }
//
//        public void play() {
//            al.alSourceRewind(sources[0]);
//            al.alSourcePlay(sources[0]);
//        }
//
////        public void play3f(final float x, final float y, final float z) {
////            if (!initialized)
////                return;
////
////            System.out.println("play3f " + x + ", " + y + ", " + z);
////            int s = sources[0];
////            al.alSourceRewind(s);
////            al.alSourcePlay(s);
////            al.alSource3f(s, ALConstants.AL_POSITION, x, y, z);
////        }
//
////        public void pause() {
////            if (!initialized) return;
////            al.alSourcePause(sources[0]);
////        }
//
//        public void dispose() {
//            if (null != sources) {
//                al.alSourceStop(sources[0]);
//                al.alDeleteSources(1, sources, 0);
//                sources = null;
//            }
//            if (null != context) {
//                alc.alcDestroyContext(context);
//                context = null;
//            }
//            if (null != device) {
//                alc.alcCloseDevice(device);
//                device = null;
//            }
//        }
//
//        public static void main(final String[] args) throws InterruptedException, UnsupportedAudioFileException, IOException {
//            OpenALTest demo = new OpenALTest();
//
//            demo.play();
//            Thread.sleep(5000);
//
//            demo.dispose();
//        }
//
//    }


}