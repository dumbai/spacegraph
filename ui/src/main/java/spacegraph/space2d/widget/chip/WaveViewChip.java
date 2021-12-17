package spacegraph.space2d.widget.chip;

import jcog.signal.buffer.CircularFloatBuffer;
import spacegraph.audio.AudioBuffer;
import spacegraph.space2d.widget.meter.WaveBitmap;
import spacegraph.space2d.widget.port.TypedPort;

public class WaveViewChip extends TypedPort<AudioBuffer> {

    final CircularFloatBuffer buffer = new CircularFloatBuffer(44100 * 2);
    final WaveBitmap wave = new WaveBitmap(buffer, 600, 400);

    public WaveViewChip() {
        super(AudioBuffer.class);

        set(wave);

        zero();

        on(nextBuffer ->{
            float[] b = nextBuffer.data;
            buffer.freeHead(b.length);
            buffer.write(b);
            wave.setTime(0, buffer.capacity() /*b.length*/);
            wave.update();
        });
    }

    public void zero() {
        int c = buffer.capacity();
        for (int i = 0; i < c; i++) //HACK TODO use bulk array fill method
            buffer.write(new float[] { 0 });
    }

}
