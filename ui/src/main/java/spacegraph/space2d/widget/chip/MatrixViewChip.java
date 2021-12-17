package spacegraph.space2d.widget.chip;

import jcog.Util;
import jcog.signal.Tensor;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.widget.meter.BitmapMatrixView;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.video.Draw;

public class MatrixViewChip extends Bordering {
    BitmapMatrixView matrix;
    final TypedPort<Tensor> in = new TypedPort<>(Tensor.class);

    private Tensor last;

    {

        set(S, new Stacking(in, new VectorLabel("in")));

        in.on(x -> {
            synchronized (MatrixViewChip.this) {
           //if (matrix == null || !matrix.equalShape(x)) {
//                       if (matrix!=null)
//                            matrix.remove();
                if (matrix!=null && last == x) {
                    matrix.updateIfShowing();
                    return; //no change
                }

                last = x;

               int[] shape = x.shape();
               if (shape.length ==3 ) {
                   //HACK RGB
                   assert(shape[2]==3);
                   matrix = new BitmapMatrixView(shape[0], shape[1], (cx, cy) -> Draw.rgbInt(x.get(cx, cy, 0), x.get(cx, cy, 1), x.get(cx, cy, 2)));
               } else {
                   matrix = //shape.length == 2 ? new BitmapMatrixView(shape[0], shape[1]) : new BitmapMatrixView(shape[0], 1);
                           shape.length == 2 ? new BitmapMatrixView(shape[0], shape[1],
                                   (cx, cy) -> Draw.rgbInt(x.get(cx, cy), 0, 0)) :
                                   new BitmapMatrixView(shape[0], 1,
                                           (cx, cy) -> Draw.rgbInt(Util.tanhFast(x.get(cx))/2+0.5f, 0, 0));
               }
               set(matrix);
               matrix.updateIfShowing();
//                       matrix.up
            }
           //}
        });

    }

}