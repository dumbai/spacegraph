package spacegraph.space2d.hud;

import com.jogamp.opengl.GL2;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.PaintSurface;
import spacegraph.util.math.Color4f;
import spacegraph.video.Draw;

public abstract class Overlay extends PaintSurface {

    protected final Zoomed.Camera cam;

    protected float thick = 3;
    protected final Color4f color = new Color4f(1.0f, 1.0f, 1.0f, 0.5f);

    public Overlay(Zoomed.Camera cam) {
        this.cam = cam;
        clipBounds = false;
    }


    @Override
    protected final void paint(GL2 gl, ReSurface reSurface) {
        if (!enabled())
            return;

        Surface t = target();

        if (t!=null) {
            if (!t.showing())
                t = null;
            else {

                paint(t, gl, reSurface);

            }
        }

    }

    protected abstract void paint(Surface t, GL2 gl, ReSurface reSurface);


//    String caption = null;
//    float captionFlashtimeS = 0.5f;
//    float captionFlashRemain = 0;
//    private void paintCaption(Surface t, ReSurface s, GL2 gl) {
//        if (t != last) {
//            //update
//            caption = t.toString();
//            captionFlashRemain = captionFlashtimeS;
//        } else {
//            captionFlashRemain = Math.max(0, captionFlashRemain - s.dtS());
//        }
//
//        //if (captionFlashRemain > 0) {
////        gl.glEnable(GL_COLOR_LOGIC_OP);
//        //            gl.glLogicOp(
////                    GL_XOR
////                    //GL_INVERT
////                    //GL_OR_INVERTED
////                    //GL_EQUIV
////            );
//        gl.glLineWidth(1f);
//        float i = Util.lerp((captionFlashRemain / ((float) captionFlashtimeS)), 0.25f, 0.9f);
//        gl.glColor3f(i,i,i);
//
//        float w = s.pw, h = s.ph;
//        float scale = Math.min(w, h) / 80f;
//        HersheyFont.hersheyText(gl, caption, scale, w / 2, scale, 0, Draw.TextAlignment.Center);
//        //        gl.glDisable(GL_COLOR_LOGIC_OP);
//        //}
//    }

    public void drawBoundsFrame(Surface t, GL2 gl) {
        RectF pq = cam.globalToPixel(t.bounds);
        if (pq.w > Float.MIN_NORMAL && pq.h > Float.MIN_NORMAL) {
            color.apply(gl);
            gl.glLineWidth(thick);
            Draw.rectStroke(pq.x, pq.y, pq.w, pq.h, gl);
        }

        //Draw.rectFrame((p.x + q.x) / 2, (p.y + q.y) / 2, q.x - p.x, q.y - p.y, thick, gl);
    }


    protected boolean enabled() {
        return true;
    }

    protected abstract Surface target();
}