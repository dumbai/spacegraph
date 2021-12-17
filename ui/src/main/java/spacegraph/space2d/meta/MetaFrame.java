package spacegraph.space2d.meta;

import spacegraph.space2d.Labeled;
import spacegraph.space2d.MenuSupplier;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.widget.text.VectorLabel;

/**
 * a dynamic frame for attaching to widgets providing access to context menus, controls, and display
 */
public class MetaFrame extends Bordering  {

//    private static final float WIDE_FACTOR_THRESHOLD = Util.PHIf;

    private Surface menu, label;

    private MetaFrame() {
        super();
    }

    public MetaFrame(Object o) {
        this(new ObjectSurface(o));
    }

    public MetaFrame(Surface surface) {
        super(surface);
    }



    //    protected Surface newMetaMenu() {
//        return new Gridding(
//
//            new PushButton("^").click(()->{
//                //pop-out
//                Surface s = get(0);
//                if (s.remove()) {
//                    SpaceGraph.window(s, 500, 500); //TODO size by window TODO child window
//                }
//            }),
//            new PushButton("=").click(()->{
//                //tag
//                throw new TODO();
//            }),
//            new PushButton("?").click(()->{
//                //pop-up inspection view
//                Object tt = the();
//                if (tt instanceof Surface) {
//                    SpaceGraph.window(new Inspector((Surface) tt), 500, 500); //TODO size by window TODO child window
//                }
//            })
//            //copy, paste, ...
//            //modal view lock to bounds
//            //etc
//        );
//    }

    boolean expanded;

//    private SatelliteMetaFrame satellite = null;

    @Override
    protected boolean canRender(ReSurface r) {
        if (expanded) {
            clipBounds = false;

            pos(r.pixelVisible().scale(0.8f));
            //pos(0, 0, r.pw, r.ph);

            //renderExpanded.setAt(r);
            //renderExpanded.restart(r.pw, r.ph, r.dtMS);
//            renderExpanded.dtMS = r.dtMS;
//            renderExpanded.scaleX = 1;
//            renderExpanded.scaleY = 1;
//            renderExpanded.x1 = 0;
//            renderExpanded.y1 = 0;
//            renderExpanded.x2 = r.pw;
//            renderExpanded.y2 = r.ph;
//            renderExpanded.pw = r.pw;
//            renderExpanded.ph = r.ph;
            //renderExpanded = r;

            //r.overlay(this::paintLater);
            return true;
        } else {
//            clipBounds = true;
            return super.canRender(r);
        }
    }

//    @Override
//    public boolean showing() {
//        return expanded || super.showing(); //HACK
//    }

//    @Override
//    public Surface finger(Finger f) {
//        Surface s = super.finger(f);
//        if ((s == null || s == this)  && f.intersects(bounds)) {
//
////            v2 ss = f.globalToNumScreens(bounds);
////            if (ss.x > 0.9f || ss.y > 0.9f) {
////                //if any dimension consumes 90% of a screen dimension..
////                System.out.println(this + " " + f.posRelative(bounds) + " " + ss );
////
////            }
//
//        }
//        return s;
//    }

    @Override
    protected boolean _remove(Surface surface) {
        center(null); //HACK ?
        return true;
    }

//    @Override
//    protected void doLayout(float dtS) {
//        north(label);
//        if (w() > h()*WIDE_FACTOR_THRESHOLD) {
//            //extra wide
//            south(null);
//            east(menu);
//        } else {
//            east(null);
//            south(menu);
//        }
//
//        super.doLayout(dtS);
//    }

    @Override
    protected void starting() {
        super.starting();

        Surface c = center();

        menu = (c instanceof MenuSupplier) ? ((MenuSupplier) c).menu() : null;
        label = label();
        north(label);
        south(menu);

//        northwest(ToggleButton.iconAwesome("upload" /* HACK */).on((x)->{
//            expanded = x;
//        }));

    }

//    /**
//     * titlebar clicked
//     */
//    protected void click() {
//        synchronized (this) {
//
//            boolean e = expanded;
//
//            if (!e) {
//                //TODO unexpand any other MetaFrame popup that may be expanded.  check the root context's singleton map
//
//                undock();
//            } else {
//
//
//                dock();
//
//                //
////
////                    SurfaceBase p = parent;
////                    if (p!=null) {
////                        ((Container) p).layout();
////                    }
//
//
//            }
//        }
//    }

//    private void dock() {
//        synchronized (this) {
//            if (expanded) {
//                assert (satellite != null);
//
//                if (satellite.the().reattach(this)) {
//                    expanded = false;
//
//                    if (!satellite.delete())
//                        throw new WTF();
//
//                    satellite = null;
//
//                    ((ContainerSurface)parent).layout();
//                } else
//                    throw new WTF();
//            }
//        }
//    }


//    private void undock() {
//        MutableListContainer hud = hud();
//
//        /*if (hud.isEmpty())*/ {
//
//            Surface content = the();
//            if (content != null) {
//                SatelliteMetaFrame wrapper = new SatelliteMetaFrame(content);
//                hud.add(wrapper);
//                wrapper.pos(hud.bounds.scale(0.8f));
//                expanded = true;
//                satellite = wrapper;
//            }
//
//        }
//
//    }


//        Surface m = grid(
//                PushButton.awesome("tag"),
//                PushButton.awesome("sitemap")
//        );
//        setAt(E, m);

//        PushButton hideButton = PushButton.awesome("times");
//        setAt(NE, new Scale(hideButton, 0.8f));


    @Deprecated private String name() {
        //try to avoid
        return childrenCount() == 0 ? "" : center().toString();
    }

    protected Surface label() {
        Surface x = center();
        if (x!=null)
            return x instanceof Labeled ? ((Labeled) x).label() : new VectorLabel(name());
        else
            return null;
    }

    @Override
    public String toString() {
        return name();
    }

//    @Override
//    public Surface hover(RectFloat screenBounds, Finger f) {
//        return new BitmapLabel(screenBounds.toString()).pos(screenBounds.scale(1.5f));
//    }


//    private static class SatelliteMetaFrame extends MetaFrame {
//
//        public SatelliteMetaFrame(Surface surface) {
//            super(null);
//            surface.reattach(this);
//        }
//
////        @Override
////        protected void click() {
////            MetaFrame.this.dock();
////        }
//    }

}