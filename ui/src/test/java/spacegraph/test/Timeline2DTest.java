package spacegraph.test;

import jcog.signal.buffer.CircularFloatBuffer;
import spacegraph.SpaceGraph;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.time.Timeline2D;
import spacegraph.space2d.container.time.Timeline2DEvents;
import spacegraph.space2d.container.time.Timeline2DSequence;
import spacegraph.space2d.widget.meter.WaveBitmap;

import java.util.concurrent.ThreadLocalRandom;

public enum Timeline2DTest {
	;

	public static void main(String[] args) {
        Surface t = timeline2dTest();
        SpaceGraph.window(t, 800, 600);
    }

    protected static Surface timeline2dTest() {
        int range = 50;

        Timeline2D t = new Timeline2D(0, range + 1);
        t.add(new Timeline2D.TimelineGrid());
        t.add(wave(range));
        t.add(events(range));
        //t.add(waveEvents());
        return t.withControls();
    }

    private static Surface wave(int range) {
        int samplesPerRange = 10;
        CircularFloatBuffer c = new CircularFloatBuffer(range * samplesPerRange);
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < range * samplesPerRange; i++) {
            c.write(new float[] {rng.nextFloat()});
        }

        WaveBitmap wv = new WaveBitmap(c, 500, 500);
        wv.height.set(0.25f);
        wv.alpha.set(0.5f);
        return wv;
    }

    protected static Timeline2DEvents<Timeline2D.SimpleEvent> events(int range) {
        Timeline2D.SimpleEventBuffer m = new Timeline2D.SimpleEventBuffer();
        int events = 30;
        for (int i = 0; i < events; i++) {
            long start = (long) (Math.random() * range);
            long length = (long) (Math.random() * 10) + 1;
            m.add(new Timeline2D.SimpleEvent("x" + i, start, start + length));
        }
        m.add(new Timeline2D.SimpleEvent(seq(), 15, 50));

        return new Timeline2DEvents<>(m);
    }

//    protected static Timeline2DEvents<Pair<LongInterval,Tensor>> waveEvents() {
//
//
//
//        return new Timeline2DEvents<>(s, e ->
//            e.set(
//                    new Widget(new WaveBitmap(e.id.getTwo(), 0, sampleRate, 128, 32))), new Timeline2DEvents.LaneTimelineUpdater()
//        );
//    }

    protected static WaveBitmap seq() {
        int sampleRate = 100;
        Timeline2DSequence s = new Timeline2DSequence(sampleRate, 32);
        for (int i = 0; i < 32; i++) {
            float[] noise = new float[s.buffer.width];
            for (int j = 0; j < noise.length; j++)
                noise[j] = ((float) Math.random() - 0.5f)*2;
            s.buffer.set(noise);
        }
        WaveBitmap w = new WaveBitmap(s.buffer, 0, sampleRate, 512, 128);
        //w.height.set(0.1f);
        return w;

    }
}