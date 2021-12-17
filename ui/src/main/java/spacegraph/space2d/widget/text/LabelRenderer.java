package spacegraph.space2d.widget.text;

import com.jogamp.opengl.GL2;
import jcog.tree.rtree.rect.RectF;
import spacegraph.video.Draw;
import spacegraph.video.font.HersheyFont;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface LabelRenderer extends BiConsumer<VectorLabel, GL2> {

    /**
     * hershey vector font renderer
     */
    LabelRenderer Hershey = (label, gl2) -> Draw.bounds(label.bounds, gl2, (gl) -> {

        label.fgColor.apply(gl);
        gl.glLineWidth(label.textThickness);

        HersheyFont.hersheyText(gl, label.text, label.textScaleX, label.textScaleY, 0, label.textY, 0, Draw.TextAlignment.Left);
    });


    /** draws a filled box only, useful for LOD cases */
    LabelRenderer LineBox = (label, gl) -> {

        label.fgColor.apply(gl);
        //gl.glLineWidth(0.5f);
        RectF b = label.bounds;
        float x = b.x;
        float y = b.y;
        float W = b.w;
        float H = b.h;
        float w = W * label.textScaleX * label.text.length();
        float h = H * label.textScaleY;
        float wm = (W - w) / 2;
        float hm = (H - h) / 2;
        Draw.rect(x + wm, y + hm, w, h, gl);

    };

//    /** TODO not ready */
//    LabelRenderer AWTBitmap = new LabelRenderer() {
//        //HACK
//        final WeakHashMap<Label,StringBitmapSurface> surfaces = new WeakHashMap<>();
//        @Override
//        public void accept(Label label, GL2 gl) {
//            StringBitmapSurface s = surfaces.computeIfAbsent(label, (x) -> new StringBitmapSurface() {
//                @Override
//                public void render(GL2 gl, SurfaceRender r) {
//                    pos(x.bounds);
//                    text(x.text);
//                    paint(gl, r);
//                }
//            });
//            s.render(gl, new SurfaceRender(1000, 1000, 1) {
//                @Override
//                public boolean visible(RectFloat2D r) {
//                    return true;
//                }
//            });
//        }
//    };

//    /** TODO not ready */
//    LabelRenderer NewtGraph = new LabelRenderer() {
//
//
//        final float fontSize = 0.1f;
//        /* 2nd pass texture size antialias SampleCount
//           4 is usually enough */
//        private final int[] sampleCount = new int[]{
//                //2
//                1
//        };
//        private final int renderModes =
//                0;
//
//
//        final TextRegionUtil textRegionUtil;
//        final RenderState renderState;
//        final RegionRenderer regionRenderer;
//
//
//        Font font;
//
//
//        {
//            /* load a ttf font */
//            try {
//                /* JogAmp FontFactory will load a true type font
//                 *
//                 * fontSet == 0 loads
//                 * jogamp.graph.font.fonts.ubuntu found inside jogl-fonts-p0.jar
//                 * http:
//                 *
//                 * fontSet == 1 tries loads LucidaBrightRegular from the JRE.
//                 */
//
//
//                File fontfile = new File(
//                        //TODO include Hack
//                        //"/usr/share/fonts/truetype/hack/Hack-Regular.ttf"
//                        //"/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
//                        "/usr/share/fonts/TTF/Vera.ttf"
//                );
//
//                this.font = new TypecastFontConstructor().create(fontfile);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.exit(1);
//            }
//
//
//            /* initialize OpenGL specific classes that know how to render the graph API shapes */
//            renderState = RenderState.createRenderState(SVertex.factory());
//
//            //renderState.setColorStatic(1.0f, 0.0f, 0.0f, 1.0f);
//            //renderState.setHintMask(RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED);
//
//            regionRenderer = RegionRenderer.create(renderState,
//                    /* GLCallback */ RegionRenderer.defaultBlendEnable,
//                    /* GLCallback */ RegionRenderer.defaultBlendDisable);
//
//            textRegionUtil = new TextRegionUtil(renderModes);
//
//
//        }
//
////            @Override
////            public boolean stop() {
////                if (super.stop()) {
////
////
////                    renderState.destroy(gl);
////                    gl = null;
////                    return true;
////                }
////                return false;
////            }
//
//
//        @Override
//        public void accept(VectorLabel label, GL2 gl) {
//
//            if (this.font == null)
//                return;
//
//            regionRenderer.init(gl, renderModes);
//            //regionRenderer.enable(gl, false);
//
//            RectFloat bounds = label.bounds;
//
////            gl.glColor3f(0.25f, 0.25f, 0.25f);
////            Draw.rect(bounds, gl);
//
//            String text = label.text;
//
////            int pixelGranularity = 100;
////            int iw = pixelGranularity;
////            int ih = (int) Math.ceil(bounds.h / bounds.w * pixelGranularity);
//            final PMVMatrix Pmv = regionRenderer.getMatrix();
//
//
////
////
//            Pmv.glLoadIdentity();
//            Pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
//
//
//
//            //float s = Math.min(bounds.w, bounds.h);
//            float sx =
//                    //s * (pixelGranularity);
//                    //(float) (Math.random()*10);
//                    5;
//            //float sy = s * (pixelGranularity);
//            float cx = bounds.cx();
//            float cy = bounds.cy();
//            //Pmv.glTranslatef(cx/sx, cy/sx, -0.2f);
//
//            Pmv.glTranslatef(-( bounds.cx()/1000), 0, 0f);
//            //Pmv.glScalef(sx, sx, 1f);
//
//
//            regionRenderer.enable(gl, true);
//
//
//            //label.textColor.apply(gl);
//            renderState.setColorStatic(label.textColor.x, label.textColor.y, label.textColor.z, label.textColor.w);
//
//
//            textRegionUtil.drawString3D(gl, regionRenderer, font, fontSize, text, null, sampleCount);
//            regionRenderer.enable(gl, false);
//
//
//
//            //regionRenderer.destroy(gl);
//
//        }
//    };

}
