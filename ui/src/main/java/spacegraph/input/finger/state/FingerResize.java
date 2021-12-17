package spacegraph.input.finger.state;

import jcog.math.v2;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.FingerRenderer;
import spacegraph.space2d.widget.windo.util.DragEdit;

public abstract class FingerResize extends Dragging {
    private static final float aspectRatioRatioLimit = 0.1f;
    private final boolean invY;
    private RectF before;

    private final v2 posStart = new v2();

    protected DragEdit mode = DragEdit.MOVE; //null

    FingerResize(int button) {
        this(button, false);
    }

    FingerResize(int button, boolean invertY) {
        super(button);
        this.invY = invertY;
    }

    @Override
    public @Nullable FingerRenderer cursor() {
        DragEdit m = this.mode;
        return m!=null ? m.cursor() : null;
    }

    public abstract DragEdit mode(Finger finger);

    @Override
    protected boolean starting(Finger f) {
        if (super.starting(f)) {
            this.before = size();
            this.posStart.set(pos(f));
            return true;
        }
        return false;
    }

    @Override
    public boolean drag(Finger f) {

        DragEdit m = mode(f);
        if (m == null)
            return false;

        v2 pos = this.pos(f);
        float fx = pos.x;
        float fy = pos.y;

        switch (m) {

            case MOVE:
                return false;

            case RESIZE_S: {
                float pmy = before.top();
                float bh = before.h;
                float ty = (fy - posStart.y);
                resize(before.left(), pmy - bh + ty, before.right(), pmy);
                break;
            }

            case RESIZE_SW: {
                float pmx = before.right();
                float pmy = before.top();
                float bw = before.w;
                float bh = before.h;
                float tx = (fx - posStart.x);
                float ty = (fy - posStart.y);
                resize(pmx - bw + tx, pmy - bh + ty, pmx, pmy);
                break;
            }

            case RESIZE_NE: {
                float pmx = before.left();
                float pmy = before.bottom();
                float bw = before.w;
                float bh = before.h;
                float tx = (fx - posStart.x);
                float ty = (fy - posStart.y);
                resize(pmx, pmy,
                        Math.max(pmx + aspectRatioRatioLimit * bw, bw + pmx + tx),
                        Math.max(pmy + aspectRatioRatioLimit * bh, bh + pmy + ty));
                break;
            }
            case RESIZE_SE: {
                float pmx = before.left();
                float pmy = before.top();
                float bw = before.w;
                float bh = before.h;
                float tx = (fx - posStart.x);
                float ty = (fy - posStart.y);
                resize(pmx, pmy - bh + ty, Math.max(pmx + aspectRatioRatioLimit * bw, bw + pmx + tx), pmy);
                break;
            }
            case RESIZE_N: {
                float top, bottom;
                float bh = before.h;
                float ty = (fy - posStart.y);
                if (!invY) {
                    top = before.bottom();
                    bottom = Math.max(top + aspectRatioRatioLimit * bh, top + bh + ty);
                } else {
                    bottom = before.top();
                    top = Math.min(bottom - aspectRatioRatioLimit * bh, bottom - bh - ty);
                }
                resize(
                        before.left(),
                        top,
                        before.right(),
                        bottom
                );
                break;
            }

            case RESIZE_NW: {
                float pmx = before.right();
                float pmy = before.bottom();
                float bw = before.w;
                float bh = before.h;
                float tx = (fx - posStart.x);
                float ty = (fy - posStart.y);
                resize(pmx - bw + tx, pmy,
                        pmx,
                        Math.max(pmy + aspectRatioRatioLimit * bh, bh + pmy + ty));
                break;
            }


            case RESIZE_E: {
                float pmx = before.left();
                float bw = before.w;
                float tx = (fx - posStart.x);
                resize(pmx, before.bottom(),
                        pmx + Math.max(aspectRatioRatioLimit * bw, bw + tx), before.top());
                break;
            }
            case RESIZE_W: {
                float pmx = before.right();
                float bw = before.w;
                float tx = (posStart.x - fx);
                resize(pmx - Math.max(aspectRatioRatioLimit * bw, bw + tx), before.bottom(),
                        pmx, before.top());
                break;
            }
        }
        return true;

    }

    protected abstract v2 pos(Finger finger);

    /** current size */
    protected abstract RectF size();

    protected abstract void resize(float x1, float y1, float x2, float y2);


    @Override
    public void stop(Finger finger) {
        super.stop(finger);
        this.mode = null;
    }

    public @Nullable FingerResize mode(DragEdit mode) {
		return null == (this.mode = mode) ? null : this;
    }
}
