package spacegraph.space2d.container.grid;

import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.collection.MutableListContainer;
import spacegraph.space2d.widget.Widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static jcog.Util.PHI_min_1f;
import static jcog.Util.lerp;

/**
 * TODO parameterize DX/DY to choose between row, column, or grid of arbitrary aspect ratio
 * aspect ratio=0: row (x)
 * aspect ratio=+inf: col (x)
 * else: grid( %x, %(ratio * x) )
 */
public class Gridding extends MutableListContainer {


	public static final float HORIZONTAL = 0.0f;
	public static final float VERTICAL = Float.POSITIVE_INFINITY;

	/**
	 * https://en.wikipedia.org/wiki/Golden_ratio
	 */
	public static final float PHI = PHI_min_1f;

	protected float margin = Widget.marginPctDefault;

	private float aspect;

	public Gridding() {
		this(PHI);
	}

	public Gridding(Surface... children) {
		this(PHI, children);
	}

	public Gridding(float margin, float aspect, Surface/*...*/ children) {
		this(aspect);
		set(children);
		this.margin = margin;
	}

	public Gridding(Stream<? extends Surface> c) {
		this(c.collect(toList()));
	}

	public Gridding(Collection<? extends Surface> c) {
		this(c instanceof List ? ((List)c) : new ArrayList<Surface>(c));
	}

	public Gridding(List<? extends Surface> children) {
		this(PHI, children);
	}

	public Gridding(float aspect, Surface... children) {
		this(aspect);
		set(children);
	}

	protected Gridding(float aspect, List<? extends Surface> children) {
		this(aspect);
		set(children);
	}

	private Gridding(float aspect) {
		super();
		this.aspect = (aspect);
	}

	protected int layoutIndex(int i) {
		return i;
	}

	public boolean isGrid() {
		float a = aspect;
		return a != 0 && a != Float.POSITIVE_INFINITY;
	}

	public Gridding aspect(float gridAspect) {
		this.aspect = gridAspect;
		layout(); //TODO only if gridAspect change
		return this;
	}

	@Override
	public void doLayout(float dtS) {

		Surface[] children = this.children();

		int n = children.length;
		if (n == 0) return;

		float a = aspect;


		if (a != 0 && Float.isFinite(a)) {


			float h = h();
			float w = w();
			if (w < Float.MIN_NORMAL || h < Float.MIN_NORMAL)
				return;

			float actualAspect = h / w;

			int x;
			int s = (int) Math.ceil(Math.sqrt(n));
			if (actualAspect >= a) {
				x = Math.round(lerp(actualAspect / n, s, 1));
			} else if (actualAspect < a) {
				x = Math.round(lerp(1.0f -(1.0f /actualAspect)/n, n, s));
			} else {
				x = s;
			}

			x = Math.max(1, x);
			int y = (int) Math.max(1, Math.ceil((float) n / x));

			assert (y * x >= s);
			if (x!=1 && y!=1) {
				int xyWaste = (x * y) - n;
				if (xyWaste > 0 && y > 1) {
					int xPlus1yWaste = (x + 1) * (y - 1) - n;
					if (xPlus1yWaste >= 0 && xPlus1yWaste < xyWaste) {
						x++;
						y--;
						xyWaste = (x * y) - n;
					}
				}
				if (xyWaste > 0 && x > 1) {
					int xyPlus1Waste = (x - 1) * (y + 1) - n;
					if (xyPlus1Waste >= 0 && xyPlus1Waste < xyWaste) {
						x--;
						y++;
						xyWaste = (x * y) - n;
					}
				}
			}

			if (y == 1) {
				a = 0;
			} else if (x == 1) {
				a = Float.POSITIVE_INFINITY;
			} else {
				//System.out.println(this + " "+ x + " " + y + " " + xyWaste);
				layoutGrid(children, x, y, margin);
				return;
			}
		}

		if (a == 0) {

			layoutGrid(children, n, 1, margin);
		} else /*if (!Float.isFinite(aa))*/ {

			layoutGrid(children, 1, n, margin);
		}


	}

	protected void layoutGrid(Surface[] children, int nx, int ny, float margin) {
		int i = 0;

		float hm = margin / 2.0f;

		float mx = (1 + 1 + nx / 2.0f) * hm;
		float my = (1 + 1 + ny / 2.0f) * hm;

		float dx = nx > 0 ? (1.0f - hm) / nx : 0;
		float dxc = (1.0f - mx) / nx;
		float dy = ny > 0 ? (1.0f - hm) / ny : 0;
		float dyc = (1.0f - my) / ny;


		int n = children.length;


		float X = x(), Y = y(), W = w(), H = h();

		for (int y = 0; y < ny; y++) {

			float px = hm;

			float py = jcog.Util.fma(((ny - 1) - y), dy, hm);
			float y1 = py * H;

			for (int x = 0; x < nx; x++) {


				Surface c = children[layoutIndex(i++)];

				float x1 = px * W;
				c.pos(RectF.X0Y0WH(X + x1, Y + y1, dxc * W, dyc * H));

				px += dx;

				if (i >= n) break;
			}


			if (i >= n) break;

		}
	}

	public void vertical() {
		aspect(VERTICAL);
	}

	public void horizontal() {
		aspect(HORIZONTAL);
	}

	public void square() {
		aspect(PHI);
	}

	public Gridding margin(float i) {
		this.margin = i;
		return this;
	}
}