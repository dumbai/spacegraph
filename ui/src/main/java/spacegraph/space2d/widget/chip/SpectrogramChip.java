package spacegraph.space2d.widget.chip;

import spacegraph.space2d.widget.meter.Spectrogram;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.video.Draw;

public class SpectrogramChip extends TypedPort<float[]> {

    private transient Spectrogram s;

    int history = 128;

    public SpectrogramChip() {
        super(float[].class);
        on(row ->{
            Spectrogram s = this.s;
            if (s == null || s.N != row.length) {
                set(s = this.s = new Spectrogram(true, history, row.length));
            }
            s.next((int i)-> Draw.colorHSB(row[i], 0.5f, 0.5f));
        });
    }
//
//    @Override
//    protected void paintWidget(RectFloat bounds, GL2 gl) {
//    }

}