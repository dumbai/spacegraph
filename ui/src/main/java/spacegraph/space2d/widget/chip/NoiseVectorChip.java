package spacegraph.space2d.widget.chip;

import jcog.exe.Loop;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.FloatRange;
import jcog.signal.IntRange;
import jcog.signal.Tensor;
import jcog.signal.tensor.TensorFunc;
import jcog.signal.tensor.TensorLERP;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.meter.BitmapMatrixView;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.space2d.widget.slider.FloatSlider;
import spacegraph.space2d.widget.slider.IntSlider;
import spacegraph.space2d.widget.text.Labelling;

import java.util.Random;

public class NoiseVectorChip extends Splitting {

    final IntRange size = new IntRange(1, 1, 64);
    private final TypedPort<Tensor> out;
    private BitmapMatrixView view;

    //TODO move to SmoothingChip
    @Deprecated final FloatRange momentum = new FloatRange(0.05f, 0, 1.0f);

    @Nullable Loop updater;
    @Nullable TensorLERP outputVector;

    final Random rng;

    public NoiseVectorChip() {
        this(new XoRoShiRo128PlusRandom(System.nanoTime()));
    }

    public NoiseVectorChip(Random rng) {
        super();/*0.25f, */

        this.rng = rng;

        updater = Loop.of(this::next);
        R( new Gridding(
                Labelling.the("fps", new FloatSlider(1.0f, 0, 120).on((f)-> updater.fps(f))),

                Labelling.the("size", new IntSlider(size)),

                Labelling.the("momentum", new FloatSlider(0.5f, 0, 1).on(momentum::set)),

                Labelling.the("out", out = new TypedPort<>(Tensor.class))
        ));

        next();

    }

    protected void next() {
        Tensor o = this.outputVector;
        if (o == null || o.volume()!=size.intValue()) {

            //TODO abstract;
            TensorFunc oNext = Tensor.randomVectorGauss(size.intValue(), 0, 1, rng);


            L(view = new BitmapMatrixView((this.outputVector = new TensorLERP(oNext, momentum)).data));
        }

        outputVector.update();
        out.out(outputVector);
        view.updateIfShowing();
    }
}