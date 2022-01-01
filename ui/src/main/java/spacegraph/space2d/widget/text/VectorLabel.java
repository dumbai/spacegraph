package spacegraph.space2d.widget.text;

import jcog.Util;
import spacegraph.space2d.ReSurface;

/**
 * Created by me on 7/29/16.
 */
public class VectorLabel extends AbstractLabel {

    static final float MIN_PIXELS_TO_BE_VISIBLE = 2.0f;
    static final float MIN_THICK = 0.5F;
    static final float THICKNESSES_PER_PIXEL = 1 / 30.0f;
    static final float MAX_THICK = 16;

    //TODO MutableRect textBounds and only update on doLayout
    protected float textScaleX = 1, textScaleY = 1;
    protected transient float textThickness;

    protected float textY;

    final LabelRenderer renderer = LabelRenderer.
                                        Hershey;
                                        //AWTBitmap;
                                        //NewtGraph;

    public VectorLabel() {
        this("");
    }

    public VectorLabel(String s) {
        text(s);
    }

    @Override protected void doLayout(float dtS) {

        int len = text.length();
        if (len == 0) {
            textScaleX = textScaleY = 0;
            return;
        }

        this.textScaleX = 1.0f / len;
//        this.textScaleY = charAspect;

        float tw = w(), th = h();
        float visAspect = th / tw;
//        if (textScaleY / textScaleX <= visAspect) {
//            this.textScaleX = 1f / (charAspect * len);
//            this.textScaleY = textScaleX * charAspect;
//        } else {
            this.textScaleY = textScaleX / visAspect;
//        }

        if (textScaleY > 1) {
            textScaleX = 1 / (len * textScaleY);
            textScaleY = 1;
        }

        textY = 0.5f - textScaleY / 2;
    }
    
    @Override protected void renderContent(ReSurface r) {
        renderer(r).accept(this, r.gl);
    }

    private LabelRenderer renderer(ReSurface r) {
        float p = r.visP(bounds.scale(textScaleX, textScaleY), MIN_PIXELS_TO_BE_VISIBLE);
        if (p <= 0)
            return LabelRenderer.LineBox;
        else {
			textThickness = Util.min(p * THICKNESSES_PER_PIXEL + MIN_THICK, MAX_THICK);
            //TODO apply translation for alignment
            return renderer;
        }
    }

//    @Override
//    protected final boolean canRender(ReSurface r) {
//        float p = r.visP(bounds, 7);
//        if (p < 7)
//            return false;
//
//        textThickness = Math.min(3, 0.5f + (p / 70f));
//
//        //return super.preRender(r);
//        return true;
//    }


}