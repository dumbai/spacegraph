package spacegraph.space2d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import jcog.Research;
import jcog.Util;
import jcog.data.list.Lst;
import jcog.math.v2;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.hud.Zoomed;
import spacegraph.video.JoglWindow;

import java.util.function.DoubleConsumer;

import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static java.lang.Math.round;

/** surface rendering context */
public class ReSurface extends SurfaceCamera {

    public float minVisibilityPixelPct =
            //0.5f;
            1.0f;

    /** time since last frame (seconds) */
    public float frameDT;

    /** can be used to calculate frame latency */
    public float frameDTideal;

    /** load metric as a temporal level-of-detail (QoS criteria)
     *    (0 = perfectly on-time, >= 1  stalled )
     *  TODO use EWMA
     */
    @Research
    public final DoubleConsumer load =
            null; //new FloatAveragedWindow(3, 0.5f);


    public long frameNS;


    public transient GL2 gl;

//    @Deprecated private boolean scaleChanged;

    /** cached pixel to surface scale factor */
    public transient float psw;
    public transient float psh;

    /** ortho restart */
    public ReSurface start(@Deprecated long startNS, float pw, float ph, float dtS, float fps, GL2 gl) {
        assert(pw >= 1 && ph >= 1);

        this.frameNS = startNS;
        this.frameDTideal = (float) (1.0/ Util.max(1.0E-9, fps));
        this.gl = gl;
        this.pw = pw;
        this.ph = ph;

        this.frameDT = dtS;

        if (load!=null)
            this.load.accept( Util.max(0, dtS - frameDTideal) / frameDTideal );

        set(pw/2, ph/2, 1, 1);
        return this;
    }

    public ReSurface set(Zoomed.Camera cam, v2 scale) {
        return set(cam.x, cam.y, scale.x, scale.y);
    }

    public ReSurface set(float cx, float cy, float sx, float sy) {

        this.scaleX = sx;
        float sxw = (this.w = pw / sx)/2;
        this.x1 = cx - sxw;
        this.x2 = cx + sxw;
        this.psw = sx;

        this.scaleY = sy;
        float syh = (this.h = ph / sy)/2;
        this.y1 = cy - syh;
        this.y2 = cy + syh;
        this.psh = sy;

        return this;
    }

    public RectF pixelVisible() {
        return RectF.XYXY(0, 0, pw, ph);
    }

    final boolean visibleByCamera(RectF r) {
        return r.intersectsX1Y1X2Y2(x1, y1, x2, y2);
    }

    final boolean visibleByPixels(RectF r) {
        return isVis(r, minVisibilityPixelPct);
    }

    public final float visP(RectF bounds, float minPixelsToBeVisible) {
        float p = bounds.w * psw;
        if (p < minPixelsToBeVisible) return 0;

        float q = bounds.h * psh;
        if (q < minPixelsToBeVisible) return 0;

        return Util.min(p, q);
    }

    private boolean isVis(RectF bounds, float minPixelsToBeVisible) {
        return bounds.w >= minPixelsToBeVisible / psw && bounds.h >= minPixelsToBeVisible / psh;
    }

//    public float load() {
//        return load.asFloat();
//    }


    final Lst<SurfaceCamera> stack = new Lst();

    public void push(Zoomed.Camera cam, v2 scale) {
        SurfaceCamera prev = clone();
        stack.add(prev);
        set(cam, prev.scaleX!=1 || prev.scaleY!=1 ?
            scale.scaleClone(prev.scaleX, prev.scaleY) :
            scale);
    }

    public void pop() {
        set(stack.removeLast());
    }

    public void end() {
        gl = null;
    }

    public void viewOrtho() {
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glViewport(0, 0, round(pw), round(ph));
        gl.glOrtho(0, pw, 0, ph, -1.5, 1.5);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
    }

    public void viewVolume(RectF s) {
        float l = s.left();
        float b = s.bottom();
        float px1 = ((l - x1) * scaleX);
        float py1 = ((b - y1) * scaleY);
        float px2 = ((l - x1 + s.w) * scaleX);
        float py2 = ((b - y1 + s.h) * scaleY);
        gl.glViewport(round(s.x), round(s.y), round(px2 - px1), round(py2 - py1));
    }

    public void render(Surface root, int x0, int y0, int x1, int y1, long startNS, float dtS, float fps, GL2 g) {
        int w = x1-x0, h = y1-y0;

        this.pw = w;  this.ph = h;

        this.x1 = 0; this.y1 = 0;
        this.x2 = root.w(); this.y2 = root.h();

        g.glViewport(x0, y0, w, h);

        g.glMatrixMode(GL_PROJECTION);
        g.glLoadIdentity();

        g.glOrtho(x0, x1, y0, y1, -1.5, 1.5);
        g.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        g.glLoadIdentity();

        start(startNS, w, h, dtS, fps, g);

        root.renderIfVisible(this);

        end();
    }

    public final void render(Surface root, JoglWindow w, long startNS, float dtS) {
        render(root, 0, 0, w.W(), w.H(), startNS, dtS, w.renderFPS, w.gl);
    }


    //    /** adapts the world coordinates to a new virtual local coordinate system */
//    public SurfaceRender virtual(RectFloat xywh) {
//
//
//    }
}