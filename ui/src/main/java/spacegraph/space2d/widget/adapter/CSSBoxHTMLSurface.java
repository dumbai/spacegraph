//package spacegraph.space2d.widget.adapter;
//
//import com.jogamp.opengl.GL2;
//import cz.vutbr.web.css.MediaSpec;
//import jcog.exe.Exe;
//import org.apache.commons.io.output.NullWriter;
//import org.cyberneko.html.HTMLConfiguration;
//import org.cyberneko.html.parsers.DOMParser;
//import org.eclipse.collections.api.tuple.Twin;
//import org.fit.cssbox.css.CSSNorm;
//import org.fit.cssbox.css.DOMAnalyzer;
//import org.fit.cssbox.io.DOMSource;
//import org.fit.cssbox.io.DefaultDOMSource;
//import org.fit.cssbox.io.DefaultDocumentSource;
//import org.fit.cssbox.io.DocumentSource;
//import org.fit.cssbox.layout.*;
//import org.fit.cssbox.render.SVGRenderer;
//import org.fit.layout.cssbox.CSSBoxTreeBuilder;
//import org.w3c.dom.Document;
//import org.xml.sax.SAXException;
//import spacegraph.space2d.Surface;
//import spacegraph.space2d.SurfaceBase;
//import spacegraph.space2d.SurfaceRender;
//import spacegraph.space2d.container.Stacking;
//import spacegraph.space2d.container.collection.MutableListContainer;
//import spacegraph.space2d.widget.text.BitmapLabel;
//import spacegraph.util.math.v2;
//import spacegraph.video.Draw;
//
//import java.awt.*;
//import java.awt.font.FontRenderContext;
//import java.awt.font.GlyphVector;
//import java.awt.geom.AffineTransform;
//import java.awt.image.BufferedImage;
//import java.awt.image.BufferedImageOp;
//import java.awt.image.ImageObserver;
//import java.awt.image.RenderedImage;
//import java.awt.image.renderable.RenderableImage;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.text.AttributedCharacterIterator;
//import java.util.Map;
//import java.util.WeakHashMap;
//
//import static org.eclipse.collections.impl.tuple.Tuples.twin;
//import static spacegraph.SpaceGraph.window;
//
///**
// * https://github.com/FitLayout/layout-cssbox/blob/master/src/main/java/org/fit/layout/cssbox/CSSBoxTreeBuilder.java
// * https://github.com/radkovo/CSSBox/blob/master/src/main/java/org/fit/cssbox/demo/ImageRenderer.java
// * https://github.com/radkovo/CSSBox/blob/master/src/main/java/org/fit/cssbox/demo/SimpleBrowser.java
// */
//public class CSSBoxHTMLSurface extends MutableListContainer {
//
//    private final v2 pixels;
//    private Twin<String> url;
//
////    private final BufferedImage buffer;
////    private final Graphics2D g;
//
//    public CSSBoxHTMLSurface(String url, int pw, int ph) {
//        super();
//
//        this.pixels = new v2(pw, ph);
//        go(url);
//
//    }
//
//    public void go(String url) {
//        synchronized (this) {
//            this.url = twin(null, url);
//        }
//    }
//
//    protected synchronized void refresh() {
//        //        buffer = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_ARGB);
////        g = buffer.createGraphics();
//
//        synchronized (this) {
//            Twin<String> u = url;
//            String next = u.getTwo();
//            if (u.getOne()!= next && next !=null) {
//                url = twin(next, next);
//                Exe.invoke(()-> {
//                    render(next);
//                });
//            }
//        }
//
//    }
//
//    protected void render(String url) {
//        CSSBoxTreeBuilder treeBuilder = new CSSBoxTreeBuilder(new Dimension(pixels.xInt(), pixels.yInt()), true, true, false) {
//
//            protected BrowserCanvas renderUrl(URL url, Dimension pageSize) throws IOException, SAXException
//            {
//                DocumentSource src = new DefaultDocumentSource(url);
//                pageUrl = src.getURL();
//                InputStream is = src.getInputStream();
//                String mime = src.getContentType();
//                if (mime == null)
//                    mime = "text/html";
//                int p = mime.indexOf(';');
//                if (p != -1)
//                    mime = mime.substring(0, p).trim();
//                //log.info("File type: " + mime);
//
////                if (mime.equals("application/pdf"))
////                {
////                    PDDocument doc = loadPdf(is);
////                    BrowserCanvas canvas = new PdfBrowserCanvas(doc, null, pageSize, src.getURL());
////                    doc.close();
////                    pageTitle = "";
////                    return canvas;
////                }
////                else
//                {
//                    DOMSource parser = new DefaultDOMSource(src) {
//                        @Override
//                        public Document parse() throws SAXException, IOException
//                        {
//                            //temporay NekoHTML fix until nekohtml gets fixed
////                            if (!neko_fixed)
////                            {
////                                HTMLElements.Element li = HTMLElements.getElement(HTMLElements.LI);
////                                HTMLElements.Element[] oldparents = li.parent;
////                                li.parent = new HTMLElements.Element[oldparents.length + 1];
////                                for (int i = 0; i < oldparents.length; i++)
////                                    li.parent[i] = oldparents[i];
////                                li.parent[oldparents.length] = HTMLElements.getElement(HTMLElements.MENU);
////                                neko_fixed = true;
////                            }
//
//
//                            DOMParser parser = new DOMParser(new HTMLConfiguration());
//
////                            parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
////                            if (charset != null)
////                                parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charset);
//                            parser.parse(new org.xml.sax.InputSource(getDocumentSource().getInputStream()));
//                            return parser.getDocument();
//                        }
//                    };
//                    Document doc = parser.parse();
////                    pageTitle = findPageTitle(doc);
//
//                    String encoding = parser.getCharset();
//
//                    MediaSpec media = new MediaSpec("screen");
//                    //updateCurrentMedia(media);
//
//                    DOMAnalyzer da = new DOMAnalyzer(doc, src.getURL());
//                    if (encoding == null)
//                        encoding = da.getCharacterEncoding();
//                    da.setDefaultEncoding(encoding);
//                    da.setMediaSpec(media);
//                    da.attributesToStyles();
//                    da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
//                    da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
//                    da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT);
//                    da.getStyleSheets();
//
//
//                    BrowserCanvas contentCanvas = new BrowserCanvas(da.getRoot(), da, src.getURL()) {
//                        @Override
//                        public void createLayout(Dimension dim, Rectangle visibleRect) {
//
////                            if (autoMediaUpdate)
////                            {
////                                decoder.getMediaSpec().setDimensions(visibleRect.width, visibleRect.height);
////                                decoder.recomputeStyles();
////                            }
//
//                            //log.trace("Creating boxes");
//                            BoxFactory factory = new BoxFactory(decoder, baseurl);
//                            factory.setConfig(config);
//                            factory.reset();
//                            VisualContext ctx = new VisualContext(null, factory);
//                            viewport = factory.createViewportTree(root, new DummyGraphics2D(), ctx, dim.width, dim.height);
//                            //log.trace("We have " + factory.next_order + " boxes");
//                            viewport.setVisibleRect(visibleRect);
//                            viewport.initSubtree();
//
//                            //log.trace("Layout for "+dim.width+"px");
//                            viewport.doLayout(dim.width, true, true);
//                            //log.trace("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");
//
////                            if (autoSizeUpdate)
////                            {
////                                //log.trace("Updating viewport size");
////                                viewport.updateBounds(dim);
////                                //log.trace("Resulting size: " + viewport.getWidth() + "x" + viewport.getHeight() + " (" + viewport + ")");
////                            }
//
//
//                            //log.trace("Positioning for "+viewport.getWidth()+"x"+viewport.getHeight()+"px");
//                            viewport.absolutePositions();
//
//
//                            //setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
//                            revalidate();
//                            viewport.draw(new MyBoxRenderer(pixels.xInt(), pixels.yInt()));
//                        }
//                    };
//
//                    contentCanvas.getConfig().setLoadImages(false);
//                    contentCanvas.getConfig().setLoadBackgroundImages(false);
//                    contentCanvas.getConfig().setReplaceImagesWithAlt(replaceImagesWithAlt);
//                    contentCanvas.createLayout(pageSize);
//
//
//
//
//                    src.close();
//
//                    return contentCanvas;
//                }
//
//            }
//            @Override
//            public void parse(URL url) throws IOException, SAXException {
//                //render the page
//                BrowserCanvas canvas =
//                        renderUrl(url, pageSize);
////                viewport = canvas.getViewport();
//
////
////                ElementBox rootbox = canvas.getViewport(); //construct the box tree
////
////                BoxNode root = buildTree(rootbox);
//
//
////                PageImpl pg = page = new PageImpl(pageUrl);
////                pg.setTitle(pageTitle);
////                pg.setRoot(root);
////                pg.setWidth(rootbox.getWidth());
////                pg.setHeight(rootbox.getHeight());
//            }
//        };
//
//        try {
//            treeBuilder.parse(url);
//
////            Vector<Box> boxes = treeBuilder.getPage().getBoxesInRegion(new Rectangular(0, 0, pw, ph));
////            boxes.forEach(b -> {
////               if (b instanceof BoxNode) {
////                   org.fit.cssbox.layout.Box bb = ((BoxNode) b).getBox();
////                   render(bb);
////               } else {
////                   throw new TODO("render " + b + " (" + b.getClass());
////               }
////            });
//        } catch (IOException | SAXException e) {
//            e.printStackTrace();
//        }
//
//    }
//    @Override
//    public boolean start(SurfaceBase parent) {
//        if (super.start(parent)) {
//            refresh();
//            return true;
//        }
//        return false;
//    }
//
//    public static void main(String[] args) {
//        window(
//                new CSSBoxHTMLSurface("http://w3c.org", 400, 400)
//        .pos(200, 200), 900, 700);
//    }
//
//    @Override
//    protected void doLayout(int dtMS) {
////        for (Surface s : children())
////            s.//doLayout(dtMS);
//    }
//
//    private static class DummyGraphics2D extends Graphics2D {
//
//        @Override
//        public void draw(Shape shape) {
//
//        }
//
//        @Override
//        public boolean drawImage(Image image, AffineTransform affineTransform, ImageObserver imageObserver) {
//            return false;
//        }
//
//        @Override
//        public void drawImage(BufferedImage bufferedImage, BufferedImageOp bufferedImageOp, int i, int i1) {
//
//        }
//
//        @Override
//        public void drawRenderedImage(RenderedImage renderedImage, AffineTransform affineTransform) {
//
//        }
//
//        @Override
//        public void drawRenderableImage(RenderableImage renderableImage, AffineTransform affineTransform) {
//
//        }
//
//        @Override
//        public void drawString(String s, int i, int i1) {
//
//        }
//
//        @Override
//        public void drawString(String s, float v, float v1) {
//
//        }
//
//        @Override
//        public void drawString(AttributedCharacterIterator attributedCharacterIterator, int i, int i1) {
//
//        }
//
//        @Override
//        public boolean drawImage(Image image, int i, int i1, ImageObserver imageObserver) {
//            return false;
//        }
//
//        @Override
//        public boolean drawImage(Image image, int i, int i1, int i2, int i3, ImageObserver imageObserver) {
//            return false;
//        }
//
//        @Override
//        public boolean drawImage(Image image, int i, int i1, Color color, ImageObserver imageObserver) {
//            return false;
//        }
//
//        @Override
//        public boolean drawImage(Image image, int i, int i1, int i2, int i3, Color color, ImageObserver imageObserver) {
//            return false;
//        }
//
//        @Override
//        public boolean drawImage(Image image, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, ImageObserver imageObserver) {
//            return false;
//        }
//
//        @Override
//        public boolean drawImage(Image image, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Color color, ImageObserver imageObserver) {
//            return false;
//        }
//
//        @Override
//        public void dispose() {
//
//        }
//
//        @Override
//        public void drawString(AttributedCharacterIterator attributedCharacterIterator, float v, float v1) {
//
//        }
//
//        @Override
//        public void drawGlyphVector(GlyphVector glyphVector, float v, float v1) {
//
//        }
//
//        @Override
//        public void fill(Shape shape) {
//
//        }
//
//        @Override
//        public boolean hit(Rectangle rectangle, Shape shape, boolean b) {
//            return false;
//        }
//
//        @Override
//        public GraphicsConfiguration getDeviceConfiguration() {
//            return null;
//        }
//
//        @Override
//        public void setComposite(Composite composite) {
//
//        }
//
//        @Override
//        public void setPaint(Paint paint) {
//
//        }
//
//        @Override
//        public void setStroke(Stroke stroke) {
//
//        }
//
//        @Override
//        public void setRenderingHint(RenderingHints.Key key, Object o) {
//
//        }
//
//        @Override
//        public Object getRenderingHint(RenderingHints.Key key) {
//            return null;
//        }
//
//        @Override
//        public void setRenderingHints(Map<?, ?> map) {
//
//        }
//
//        @Override
//        public void addRenderingHints(Map<?, ?> map) {
//
//        }
//
//        @Override
//        public RenderingHints getRenderingHints() {
//            return null;
//        }
//
//        @Override
//        public Graphics create() {
//            return this;
//        }
//
//        @Override
//        public void translate(int i, int i1) {
//
//        }
//
//        @Override
//        public Color getColor() {
//            return null;
//        }
//
//        @Override
//        public void setColor(Color color) {
//
//        }
//
//        @Override
//        public void setPaintMode() {
//
//        }
//
//        @Override
//        public void setXORMode(Color color) {
//
//        }
//
//        @Override
//        public Font getFont() {
//            return null;
//        }
//
//        @Override
//        public void setFont(Font font) {
//
//        }
//
//        @Override
//        public FontMetrics getFontMetrics(Font font) {
//            if (font == null)
//                font = new Font("monospace", 0, 16);
//
//            return new FontMetrics(font) {
//                @Override
//                public int charWidth(char ch) {
//                    return 16;
//                }
//
//                @Override
//                public int getHeight() {
//                    return super.getHeight();
//                }
//
//                @Override
//                public int charsWidth(char[] data, int off, int len) {
//                    return charWidth('x') * len;
//                }
//            };
//        }
//
//        @Override
//        public Rectangle getClipBounds() {
//            return null;
//        }
//
//        @Override
//        public void clipRect(int i, int i1, int i2, int i3) {
//
//        }
//
//        @Override
//        public void setClip(int i, int i1, int i2, int i3) {
//
//        }
//
//        @Override
//        public Shape getClip() {
//            return null;
//        }
//
//        @Override
//        public void setClip(Shape shape) {
//
//        }
//
//        @Override
//        public void copyArea(int i, int i1, int i2, int i3, int i4, int i5) {
//
//        }
//
//        @Override
//        public void drawLine(int i, int i1, int i2, int i3) {
//
//        }
//
//        @Override
//        public void fillRect(int i, int i1, int i2, int i3) {
//
//        }
//
//        @Override
//        public void clearRect(int i, int i1, int i2, int i3) {
//
//        }
//
//        @Override
//        public void drawRoundRect(int i, int i1, int i2, int i3, int i4, int i5) {
//
//        }
//
//        @Override
//        public void fillRoundRect(int i, int i1, int i2, int i3, int i4, int i5) {
//
//        }
//
//        @Override
//        public void drawOval(int i, int i1, int i2, int i3) {
//
//        }
//
//        @Override
//        public void fillOval(int i, int i1, int i2, int i3) {
//
//        }
//
//        @Override
//        public void drawArc(int i, int i1, int i2, int i3, int i4, int i5) {
//
//        }
//
//        @Override
//        public void fillArc(int i, int i1, int i2, int i3, int i4, int i5) {
//
//        }
//
//        @Override
//        public void drawPolyline(int[] ints, int[] ints1, int i) {
//
//        }
//
//        @Override
//        public void drawPolygon(int[] ints, int[] ints1, int i) {
//
//        }
//
//        @Override
//        public void fillPolygon(int[] ints, int[] ints1, int i) {
//
//        }
//
//        @Override
//        public void translate(double v, double v1) {
//
//        }
//
//        @Override
//        public void rotate(double v) {
//
//        }
//
//        @Override
//        public void rotate(double v, double v1, double v2) {
//
//        }
//
//        @Override
//        public void scale(double v, double v1) {
//
//        }
//
//        @Override
//        public void shear(double v, double v1) {
//
//        }
//
//        @Override
//        public void transform(AffineTransform affineTransform) {
//
//        }
//
//        @Override
//        public void setTransform(AffineTransform affineTransform) {
//
//        }
//
//        @Override
//        public AffineTransform getTransform() {
//            return null;
//        }
//
//        @Override
//        public Paint getPaint() {
//            return null;
//        }
//
//        @Override
//        public Composite getComposite() {
//            return null;
//        }
//
//        @Override
//        public void setBackground(Color color) {
//
//        }
//
//        @Override
//        public Color getBackground() {
//            return null;
//        }
//
//        @Override
//        public Stroke getStroke() {
//            return null;
//        }
//
//        @Override
//        public void clip(Shape shape) {
//
//        }
//
//        @Override
//        public FontRenderContext getFontRenderContext() {
//            return null;
//        }
//    }
//
//    //    class MyBoxRenderer extends GraphicsRenderer {
////
////        public MyBoxRenderer(Graphics2D g) {
////            super(g);
////        }
////
////        @Override
////        public void finishElementContents(ElementBox elem) {
////            super.finishElementContents(elem);
////
////            Rectangle b = elem.getAbsoluteContentBounds();
////            int x1 = Math.max(0, b.x);
////            int x2 = Math.min(buffer.getWidth(), b.width + b.x);
////            int w = x2 - x1;
////            if (w > 0) {
////                int y1 = Math.max(0, b.y);
////                int y2 = Math.min(buffer.getHeight(), b.height + b.y);
////                int h = y2 - y1;
////                if (h > 0) {
////
////                    BufferedImage bb = buffer.getSubimage(x1, y1, w, h);
////                    Tex.TexSurface tt = Tex.view(bb);
////                    tt.pos(x1, y1, x2, y2);
////
////
////
////                    addAt(new Windo(tt).pos(0,0, w, h));
////                }
////            }
////        }
////    }
//    class MyBoxRenderer extends SVGRenderer {
//
//        public MyBoxRenderer(int rootWidth, int rootHeight) {
//            super(rootWidth, rootHeight, new NullWriter());
//        }
//
//        final Map<Box, Surface> cache = new WeakHashMap<>();
//
//        Surface the(Box b) {
//            Surface s = cache.computeIfAbsent(b, this::addNewSurface);
//            if (s != null)
//                ((ElementSurface) s).refresh();
//
//            return s;
//        }
//
//        private Surface addNewSurface(Box box) {
//            Surface s = newSurface(box);
//            if (s!=null) {
//                //addAt(s);
//                addAt(s);
//            }
//            return s;
//        }
//
//        private Surface newSurface(Box box) {
//            if (box instanceof ElementBox) {
//                return new ElementSurface(box);
//            } else if (box instanceof TextBox) {
//                return new TextSurface((TextBox) box);
//            }
//            return null;
//        }
//
//
//        @Override
//        public void renderElementBackground(ElementBox eb) {
//            the(eb);
//        }
//
//        @Override
//        public void renderTextContent(TextBox text) {
//            the(text);
//        }
//
//        @Override
//        public void renderReplacedContent(ReplacedBox box) {
//            //TODO
//        }
//    }
//
//    private static class TextSurface extends ElementSurface<TextBox> {
//
//        private final BitmapLabel label;
//
//        public TextSurface(TextBox box) {
//            super(box);
//            this.label = new BitmapLabel("");
//            addAt(label);
//        }
//
//        @Override
//        public void refresh() {
//            super.refresh();
//
//            Color c = box.getVisualContext().color;
//            //box.drawContent();
//            label.
//                    //colorText(c.getRGB())
//                            textColor(c.getRed(), c.getGreen(), c.getBlue())
//                    .text(box.getText());
//            label.pos(bounds);
//        }
//    }
//
//    private static class ElementSurface<B extends Box> extends Stacking {
//        protected final B box;
//        float[] bgColor = null;
//
//        public ElementSurface(B box) {
//            this.box = box;
//        }
//
//        @Override
//        protected void paintIt(GL2 gl, SurfaceRender r) {
//            float[] bg = bgColor;
//            if (bg!=null) {
//                gl.glColor4fv(bg, 0);
//                Draw.rect(bounds, gl);
//            }
//
//            gl.glColor4f(0, 0, 0, 0.5f);
//            gl.glLineWidth(4);
//            Draw.rectStroke(bounds, gl);
//        }
//
//        public void refresh() {
//            Rectangle bb = box instanceof ElementBox ? ((ElementBox)box).getAbsoluteBorderBounds() : box.getAbsoluteBounds();
//            if (box instanceof Viewport)
//                bb = box.getClippedBounds(); //for the root box (Viewport), use the whole clipped content
//
//            pos(bb.x, bb.y, bb.x + bb.width, bb.y + bb.height);
//
//            if (box instanceof ElementBox) {
//                Color bg = ((ElementBox)box).getBgcolor();
//                if (bg != null) {
//                    //String style = "stroke:none;fill-opacity:1;fill:" + colorString(bg);
//                    //out.println("<rect x=\"" + bb.x + "\" y=\"" + bb.y + "\" width=\"" + bb.width + "\" height=\"" + bb.height + "\" style=\"" + style + "\" />");
//                    bgColor = bg.getComponents(bgColor);
//                }
//            }
//
//
////            //background image
////            if (eb.getBackgroundImages() != null && eb.getBackgroundImages().size() > 0)
////            {
////                for (BackgroundImage bimg : eb.getBackgroundImages())
////                {
////                    BufferedImage img = bimg.getBufferedImage();
////                    if (img != null)
////                    {
////                        ByteArrayOutputStream os = new ByteArrayOutputStream();
////                        try
////                        {
////                            ImageIO.write(img, "png", os);
////                        } catch (IOException e) {
////                            out.println("<!-- I/O error: " + e.getMessage() + " -->");
////                        }
////                        char[] data = Base64Coder.encode(os.toByteArray());
////                        String imgdata = "data:image/png;base64," + new String(data);
////                        int ix = bb.x + eb.getBorder().left;
////                        int iy = bb.y + eb.getBorder().top;
////                        int iw = bb.width - eb.getBorder().right - eb.getBorder().left;
////                        int ih = bb.height - eb.getBorder().bottom - eb.getBorder().top;
////                        out.println("<image x=\"" + ix + "\" y=\"" + iy + "\" width=\"" + iw + "\" height=\"" + ih + "\" xlink:href=\"" + imgdata + "\" />");
////                    }
////                }
////
////            }
////
////            //border
////            LengthSet borders = eb.getBorder();
////            if (borders.top > 0)
////                writeBorderSVG(eb, bb.x, bb.y, bb.x + bb.width, bb.y, "top", borders.top, 0, borders.top/2);
////            if (borders.right > 0)
////                writeBorderSVG(eb, bb.x + bb.width, bb.y, bb.x + bb.width, bb.y + bb.height, "right", borders.right, -borders.right/2, 0);
////            if (borders.bottom > 0)
////                writeBorderSVG(eb, bb.x, bb.y + bb.height, bb.x + bb.width, bb.y + bb.height, "bottom", borders.bottom, 0, -borders.bottom/2);
////            if (borders.left > 0)
////                writeBorderSVG(eb, bb.x, bb.y, bb.x, bb.y + bb.height, "left", borders.left, borders.left/2, 0);
//
//        }
//    }
//}
