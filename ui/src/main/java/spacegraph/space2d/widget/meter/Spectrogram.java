package spacegraph.space2d.widget.meter;

import com.jogamp.opengl.GL2;
import jcog.tree.rtree.rect.RectF;
import org.eclipse.collections.api.block.function.primitive.IntToIntFunction;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.RingContainer;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.text.VectorLabel;

import java.util.Iterator;
import java.util.function.ToIntFunction;

/**
 * displays something resembling a "spectrogram" to represent the changing contents of a bag
 * TODO abstract to general-purpose spectrogram aka waterfall plot
 */
public class Spectrogram extends RingContainer<BitmapMatrixView> implements BitmapMatrixView.ViewFunction2D {


	//    static final ToIntFunction HASHCODE = x ->
//            Draw.colorHSB(Math.abs(x.hashCode() % 1000) / 1000.0f, 0.5f, 0.5f);
	static final IntToIntFunction BLACK = (i) -> 0;
	/**
	 * N item axis capacity ("bins", "states", "frequencies", etc..)
	 */
	public int N;
	public IntToIntFunction _color;

	public Spectrogram(boolean leftOrDown, int T, int N) {
		super(new BitmapMatrixView[T]);
		this.horizOrVert = leftOrDown;
		this._color = BLACK;
		this.N = N;
	}

	public static Surface newControlPanel() {
		return new Gridding(new VectorLabel("TODO"));
	}

	@Override
	protected void reallocate(BitmapMatrixView[] x) {
		int n = this.N;
		// {
		//        //|| xy[0]==null  (xy[0].w * xy[0].h) != n
		//    }
		for (int i = 0; i < x.length; i++) {
			BitmapMatrixView yi = this.x[i];
			if (yi != null && yi.w * yi.h == n)
				continue;

			int W, H;
			if (horizOrVert) {
				W = 1;
				H = n;
			} else {
				W = n;
				H = 1;
			}
			BitmapMatrixView r = new BitmapMatrixView(W, H, this);
			r.cellTouch(false);
			r.pos(RectF.Unit);
			r.start(this);
			this.x[i] = r;
		}
	}

	@Override
	public final int color(int x, int y) {
		return _color.applyAsInt(horizOrVert ? y : x);
	}

	@Override
	public void renderContent(ReSurface r) {
		GL2 gl = r.gl;
		forEach((z, b) -> {
			z.show();
			z.tex.commit(gl);
			z.tex.paint(gl, b);
		});
	}

	public void next(IntToIntFunction color) {
		this._color = color;
		next(BitmapMatrixView::update);
	}
//    public void next(Tensor data, FloatToIntFunction color) {
//        this._color = color;
//        next((BitmapMatrixView b)->b.update());
//    }


    public final <X> void next(Iterable<X> items, ToIntFunction<X> colorFn) {
        Iterator<X> ii = items.iterator();
        //int j = 0;
//        //could be black, white, any color, or noise
//        protected int colorMissing() {
//            return 0;
//        }
        next((i)->{
            if (ii.hasNext())
                return colorFn.applyAsInt(ii.next());
            else
                return 0;
        });
    }

}
