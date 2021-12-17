///**
// * Copyright 2010 JogAmp Community. All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without modification, are
// * permitted provided that the following conditions are met:
// *
// *    1. Redistributions of source code must retain the above copyright notice, this list of
// *       conditions and the following disclaimer.
// *
// *    2. Redistributions in binary form must reproduce the above copyright notice, this list
// *       of conditions and the following disclaimer in the documentation and/or other materials
// *       provided with the distribution.
// *
// * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
// * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
// * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// *
// * The views and conclusions contained in the software and documentation are those of the
// * authors and should not be interpreted as representing official policies, either expressed
// * or implied, of JogAmp Community.
// */
//package spacegraph.video.font;
//
//import com.jogamp.graph.curve.Region;
//import com.jogamp.graph.curve.opengl.GLRegion;
//import com.jogamp.graph.curve.opengl.RegionRenderer;
//import com.jogamp.graph.curve.opengl.RenderState;
//import com.jogamp.graph.curve.opengl.TextRegionUtil;
//import com.jogamp.graph.font.Font;
//import com.jogamp.graph.font.FontFactory;
//import com.jogamp.graph.geom.SVertex;
//import com.jogamp.newt.Window;
//import com.jogamp.newt.event.KeyEvent;
//import com.jogamp.newt.event.KeyListener;
//import com.jogamp.newt.opengl.GLWindow;
//import com.jogamp.opengl.*;
//import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
//import com.jogamp.opengl.math.geom.AABBox;
//import com.jogamp.opengl.util.PMVMatrix;
//import jogamp.graph.font.typecast.TypecastFontConstructor;
//import spacegraph.SpaceGraph;
//import spacegraph.space2d.Surface;
//import spacegraph.space2d.SurfaceRender;
//import spacegraph.space2d.hud.Ortho;
//import spacegraph.video.JoglSpace;
//
//import java.io.File;
//import java.io.IOException;
//
//public class GPUTextNewtDemo {
//    /**
//     * FIXME:
//     *
//     * If DEBUG is enabled:
//     *
//     * Caused by: com.jogamp.opengl.GLException: Thread[main-Display-X11_:0.0-1-EDT-1,5,main] glGetError() returned the following error codes after a call to glFramebufferRenderbuffer(<int> 0x8D40, <int> 0x1902, <int> 0x8D41, <int> 0x1): GL_INVALID_ENUM ( 1280 0x500),
//     * at com.jogamp.opengl.DebugGL4bc.checkGLGetError(DebugGL4bc.java:33961)
//     * at com.jogamp.opengl.DebugGL4bc.glFramebufferRenderbuffer(DebugGL4bc.java:33077)
//     * at jogamp.graph.curve.opengl.VBORegion2PGL3.initFBOTexture(VBORegion2PGL3.java:295)
//     */
//    private static final boolean DEBUG = false;
//    private static final boolean TRACE = false;
//
//    private static int SceneMSAASamples = 0;
//    private static int GraphVBAASamples = 1;
//    private static int GraphMSAASamples = 0;
//
//    public static void main(final String[] args) {
//        int width = 800, height = 400;
//        int x = 10, y = 10;
//        if( 0 != args.length ) {
//            SceneMSAASamples = 0;
//            GraphMSAASamples = 0;
//            GraphVBAASamples = 0;
//
//            for(int i=0; i<args.length; i++) {
//                if(args[i].equals("-smsaa")) {
//                    i++;
//                    SceneMSAASamples = atoi(args[i], SceneMSAASamples);
//                } else  if(args[i].equals("-gmsaa")) {
//                    i++;
//                    GraphMSAASamples = atoi(args[i], GraphMSAASamples);
//                    GraphVBAASamples = 0;
//                } else if(args[i].equals("-gvbaa")) {
//                    i++;
//                    GraphMSAASamples = 0;
//                    GraphVBAASamples = atoi(args[i], GraphVBAASamples);
//                } else if(args[i].equals("-width")) {
//                    i++;
//                    width = atoi(args[i], width);
//                } else if(args[i].equals("-height")) {
//                    i++;
//                    height = atoi(args[i], height);
//                } else if(args[i].equals("-x")) {
//                    i++;
//                    x = atoi(args[i], x);
//                } else if(args[i].equals("-y")) {
//                    i++;
//                    y = atoi(args[i], y);
//                }
//            }
//        }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        int rmode = 0;
//        int sampleCount = 0;
//        if( GraphVBAASamples > 0 ) {
//            rmode |= Region.VBAA_RENDERING_BIT;
//            sampleCount += GraphVBAASamples;
//        } else if( GraphMSAASamples > 0 ) {
//            rmode |= Region.MSAA_RENDERING_BIT;
//            sampleCount += GraphMSAASamples;
//        }
//
//
//
//
//
//
//        final RenderState rs = RenderState.createRenderState(SVertex.factory());
//        final GPUTextGLListener0A textGLListener = new GPUTextGLListener0A(rs, rmode, sampleCount, true, DEBUG, TRACE);
//
//        JoglSpace s = SpaceGraph.window(new Surface() {
//            @Override
//            protected void paint(GL2 gl, SurfaceRender surfaceRender) {
//
//                float sx =
//
//                        ((Ortho)root()).scale.x;
//                float sy =
//
//                        ((Ortho)root()).scale.y;
//                textGLListener.render(gl, x(), y(), sx, sy);
//            }
//        }, 800, 800 );
//
//
//        s.window.addGLEventListener(textGLListener);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    }
//
//    private static int atoi(final String str, final int def) {
//        try {
//            return Integer.parseInt(str);
//        } catch (final Exception ex) {
//            ex.printStackTrace();
//        }
//        return def;
//    }
//
//    public static class GPUTextGLListener0A extends GPUTextRendererListenerBase01 {
//
//        public GPUTextGLListener0A() {
//            super(RenderState.createRenderState(SVertex.factory()), Region.VBAA_RENDERING_BIT, 4, true, false, false);
//        }
//
//        GPUTextGLListener0A(final RenderState rs, final int renderModes, final int sampleCount, final boolean blending, final boolean debug, final boolean trace) {
//            super(rs, renderModes, sampleCount, blending, debug, trace);
//        }
//
//        public void init(final GLAutoDrawable drawable) {
//            if(drawable instanceof GLWindow) {
//                final GLWindow glw = (GLWindow) drawable;
//
//            }
//            super.init(drawable);
//
//            final GL2ES2 gl = drawable.getGL().getGL2ES2();
//
//            final RenderState rs = renderer.getRenderState();
//
//            gl.setSwapInterval(1);
//            gl.glEnable(GL.GL_DEPTH_TEST);
//            gl.glEnable(GL.GL_BLEND);
//            rs.setColorStatic(0.1f, 0.1f, 0.1f, 1.0f);
//        }
//
//
//
//
//
//
//
//
//    }
//
//    /**
//     *
//     * GPURendererListenerBase01 Keys:
//     * - 1/2: zoom in/out
//     * - 6/7: 2nd pass texture size
//     * - 0/9: rotate
//     * - v: toggle v-sync
//     * - s: screenshot
//     *
//     * Additional Keys:
//     * - 3/4: font +/-
//     * - h: toogle draw 'font setAt'
//     * - f: toggle draw fps
//     * - space: toggle font (ubuntu/java)
//     * - i: live input text input (CR ends it, backspace supported)
//     */
//    abstract static class GPUTextRendererListenerBase01 implements GLEventListener {
//        TextRegionUtil textRegionUtil;
//
//    RegionRenderer renderer;
//        private final int renderModes;
//        private final GLRegion regionBottom;
//        int fontSet = FontFactory.JAVA;
//        Font font;
//
//        int headType = 0;
//
//        final float fontSizeFName = 8f;
//        final float fontSizeFPS = 10f;
//        final int[] sampleCountFPS = new int[] { 8 };
//        float fontSizeHead = 12f;
//        float fontSizeBottom = 16f;
//        float dpiH = 96;
//        final int fontSizeModulo = 100;
//        String fontName;
//        AABBox fontNameBox;
//        String headtext;
//        AABBox headbox;
//
//        static final String text1 = "abcdefghijklmnopqrstuvwxyz\nABCDEFGHIJKLMNOPQRSTUVWXYZ\n0123456789.:,;(*!?/\\\")$%^&-+@~#<>{}[]";
//        static final String text2 = "The quick brown fox jumps over the lazy dog";
//        static final String textX =
//            "JOGAMP graph demo using Resolution Independent NURBS\n"+
//            "JOGAMP JOGL - OpenGL ES2 profile\n"+
//            "Press 1/2 to zoom in/out the below text\n"+
//            "Press 3/4 to incr/decs font size (alt: head, w/o bottom)\n"+
//            "Press 6/7 to edit texture size if using VBAA\n"+
//            "Press 0/9 to rotate the below string\n"+
//            "Press v to toggle vsync\n"+
//            "Press i for live input text input (CR ends it, backspace supported)\n"+
//            "Press f to toggle fps. H for different text, space for font type\n";
//
//        static final String textX2 =
//            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec nec sapien tellus. \n"+
//            "Ut purus odio, rhoncus sit amet commodo eget, ullamcorper vel urna. Mauris ultricies \n"+
//            "quam iaculis urna cursus ornare. Nullam ut felis a ante ultrices ultricies nec a elit. \n"+
//            "In hac habitasse platea dictumst. Vivamus et mi a quam lacinia pharetra at venenatis est.\n"+
//            "Morbi quis bibendum nibh. Donec lectus orci, sagittis in consequat nec, volutpat nec nisi.\n"+
//            "Donec ut dolor et nulla tristique varius. In nulla magna, fermentum id tempus quis, semper \n"+
//            "in lorem. Maecenas in ipsum ac justo scelerisque sollicitudin. Quisque sit amet neque lorem,\n" +
//            "-------Press H to change text---------";
//
//        StringBuilder userString = new StringBuilder();
//        boolean userInput = false;
//
//        GPUTextRendererListenerBase01(final RenderState rs, final int renderModes, final int sampleCount, final boolean blending, final boolean debug, final boolean trace) {
//
//            this.renderModes = renderModes;
//            this.renderer = RegionRenderer.create(rs,
//                                        blending ? RegionRenderer.defaultBlendEnable : null,
//                                        blending ? RegionRenderer.defaultBlendDisable : null);
//
//
//            this.textRegionUtil = new TextRegionUtil(renderModes);
//
//            this.regionBottom = GLRegion.create(renderModes, null);
//            try {
//                File fontfile = new File(
//
//
//                        "/usr/share/fonts/truetype/hack/Hack-Regular.ttf"
//                );
//
//                this.font = new TypecastFontConstructor().create(
//                        fontfile);
//                dumpFontNames();
//
//                this.fontName = font.toString();
//            } catch (final IOException ioe) {
//                System.err.println("Caught: "+ioe.getMessage());
//                ioe.printStackTrace();
//            }
//
//        }
//
//        void dumpFontNames() {
//            System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//            System.err.println(font.getAllNames(null, "\n"));
//            System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//        }
//
//        void switchHeadBox() {
//            headType = ( headType + 1 ) % 4 ;
//            switch(headType) {
//              case 0:
//                  headtext = null;
//                  break;
//
//              case 1:
//                  headtext= textX2;
//                  break;
//              case 2:
//                  headtext= textX;
//                  break;
//
//              default:
//                  headtext = text1;
//            }
//            if(null != headtext) {
//                headbox = font.getMetricBounds(headtext, font.getPixelSize(fontSizeHead, dpiH));
//            }
//        }
//
//        @Override
//        public void init(final GLAutoDrawable drawable) {
//
//
//            GL2ES2 gl = drawable.getGL().getGL2ES2();
//
//            renderer.init(gl, renderModes);
//
//            final Object upObj = drawable.getUpstreamWidget();
//            if( upObj instanceof Window) {
//                final Window window = (Window) upObj;
//                final float[] sDPI = window.getPixelsPerMM(new float[2]);
//                sDPI[0] *= 25.4f;
//                sDPI[1] *= 25.4f;
//                dpiH = sDPI[1];
//                System.err.println("Using screen DPI of "+dpiH);
//            } else {
//                System.err.println("Using default DPI of "+dpiH);
//            }
//            fontNameBox = font.getMetricBounds(fontName, font.getPixelSize(fontSizeFName, dpiH));
//            switchHeadBox();
//
//        }
//
//        @Override
//        public void reshape(final GLAutoDrawable drawable, final int xstart, final int ystart, final int width, final int height) {
//            float zNear = 0.1f, zFar = 7000f;
//
//
//
//
//            renderer.reshapeNotify(width,height);
//            final PMVMatrix p = renderer.getMatrix();
//            p.glLoadIdentity();
//            p.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
//
//            p.glOrthof(0, width, 0, height, zNear, zFar);
//            p.glTranslatef(0,0,-1);
//
//
//
//
//
//
//
//
//
//
//
//
//
//        }
//
//
//
//        @Override
//        public void dispose(final GLAutoDrawable drawable) {
//
//
//
//        }
//
//        @Override
//        public void display(final GLAutoDrawable drawable) {
//        }
//
//        void render(GL2 gl, float x, float y, float sx, float sy) {
//
//
//
//            if (!renderer.isInitialized())
//                return;
//
//
//
//
//
//
//
//
//
//
//            final RegionRenderer renderer = this.renderer;
//            final RenderState rs = renderer.getRenderState();
//            final PMVMatrix pmv = renderer.getMatrix();
//
//
//
//            rs.setColorStatic(0.1f, 0.1f, 0.1f, 1.0f);
//            final float pixelSizeFName = font.getPixelSize(fontSizeFName, dpiH);
//            final float pixelSizeHead = font.getPixelSize(fontSizeHead, dpiH);
//            final float pixelSizeBottom = font.getPixelSize(fontSizeBottom, dpiH);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//            pmv.glPushMatrix();
//            pmv.glScalef(sx, sy, 1f);
//            pmv.glTranslatef(x ,y , 0);
//
//
//            textRegionUtil.drawString3D(gl, renderer, font,  pixelSizeFName, fontName, null, getSampleCount());
//
//
//
//
//            if(null != headtext) {
//
//
//
//
//
//                textRegionUtil.drawString3D(gl, renderer, font,
//                        pixelSizeHead, headtext, null, getSampleCount());
//            }
//
//
//
//
//
//
//
//
//
//
//            rs.setColorStatic(0.9f, 0.0f, 0.0f, 1.0f);
//
//
//
//
//            if(!userInput) {
//                if( bottomTextUseFrustum ) {
//                    TextRegionUtil.drawString3D(gl, regionBottom, renderer, font, pixelSizeBottom, text2, null, getSampleCount(),
//                                                textRegionUtil.tempT1, textRegionUtil.tempT2);
//                } else {
//                    textRegionUtil.drawString3D(gl, renderer, font,  pixelSizeBottom, text2, null, getSampleCount());
//                }
//            } else {
//                if( bottomTextUseFrustum ) {
//                    TextRegionUtil.drawString3D(gl, regionBottom, renderer, font,  pixelSizeBottom, userString.toString(), null, getSampleCount(),
//                                                textRegionUtil.tempT1, textRegionUtil.tempT2);
//                } else {
//                    textRegionUtil.drawString3D(gl, renderer, font,  pixelSizeBottom, userString.toString(), null, getSampleCount());
//                }
//            }
//            pmv.glPopMatrix();
//        }
//
//        private final int[] sampleCount = new int[] { 4 };
//
//        private int[] getSampleCount() {
//            return sampleCount;
//        }
//
//        final boolean bottomTextUseFrustum = true;
//
//        void fontBottomIncr(final int v) {
//            fontSizeBottom = Math.abs((fontSizeBottom + v) % fontSizeModulo) ;
//
//        }
//
//        void fontHeadIncr(final int v) {
//            fontSizeHead = Math.abs((fontSizeHead + v) % fontSizeModulo) ;
//            if(null != headtext) {
//                headbox = font.getMetricBounds(headtext, font.getPixelSize(fontSizeHead, dpiH));
//            }
//        }
//
//        boolean nextFontSet() {
//            try {
//                final int set = ( fontSet == FontFactory.UBUNTU ) ? FontFactory.JAVA : FontFactory.UBUNTU ;
//                final Font _font = FontFactory.get(setAt).getDefault();
//                if(null != _font) {
//                    fontSet = setAt;
//                    font = _font;
//                    fontName = font.getFullFamilyName(null).toString();
//                    fontNameBox = font.getMetricBounds(fontName, font.getPixelSize(fontSizeFName, dpiH));
//                    dumpFontNames();
//                    return true;
//                }
//            } catch (final IOException ex) {
//                System.err.println("Caught: "+ex.getMessage());
//            }
//            return false;
//        }
//
//        public boolean setFontSet(final int setAt, final int family, final int stylebits) {
//            try {
//                final Font _font = FontFactory.get(setAt).get(family, stylebits);
//                if(null != _font) {
//                    fontSet = setAt;
//                    font = _font;
//                    fontName = font.getFullFamilyName(null).toString();
//                    fontNameBox = font.getMetricBounds(fontName, font.getPixelSize(fontSizeFName, dpiH));
//                    dumpFontNames();
//                    return true;
//                }
//            } catch (final IOException ex) {
//                System.err.println("Caught: "+ex.getMessage());
//            }
//            return false;
//        }
//
//        public boolean isUserInputMode() { return userInput; }
//
//
//
//
//
//
//
//
//        KeyAction keyAction = null;
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        float fontHeadScale = 1f;
//
//        class KeyAction implements KeyListener {
//            @Override
//            public void keyPressed(final KeyEvent e) {
//                if(userInput) {
//                    return;
//                }
//                final short s = e.getKeySymbol();
//                if(s == KeyEvent.VK_3) {
//                    if( e.isAltDown() ) {
//                        fontHeadIncr(1);
//                    } else {
//                        fontBottomIncr(1);
//                    }
//                }
//                else if(s == KeyEvent.VK_4) {
//                    if( e.isAltDown() ) {
//                        fontHeadIncr(-1);
//                    } else {
//                        fontBottomIncr(-1);
//                    }
//                }
//                else if(s == KeyEvent.VK_H) {
//                    switchHeadBox();
//                }
//
//
//
//                else if(s == KeyEvent.VK_SPACE) {
//                    nextFontSet();
//                }
//                else if(s == KeyEvent.VK_I) {
//                    userInput = true;
//
//                }
//            }
//
//            @Override
//            public void keyReleased(final KeyEvent e) {
//                if( !e.isPrintableKey() || e.isAutoRepeat() ) {
//                    return;
//                }
//                if(userInput) {
//                    final short k = e.getKeySymbol();
//                    if( KeyEvent.VK_ENTER == k ) {
//                        userInput = false;
//
//                    } else if( KeyEvent.VK_BACK_SPACE == k && userString.length()>0) {
//                        userString.deleteCharAt(userString.length()-1);
//                    } else {
//                        final char c = e.getKeyChar();
//                        if( font.isPrintableChar( c ) ) {
//                            userString.append(c);
//                        }
//                    }
//                }
//            }
//        }
//    }
//}