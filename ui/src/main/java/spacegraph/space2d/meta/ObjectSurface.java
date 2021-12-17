package spacegraph.space2d.meta;

import com.google.common.collect.Streams;
import jcog.data.list.Lst;
import jcog.exe.Loop;
import jcog.math.FloatSupplier;
import jcog.pri.PLink;
import jcog.reflect.AutoBuilder;
import jcog.signal.DoubleRange;
import jcog.signal.FloatRange;
import jcog.signal.IntRange;
import jcog.signal.MutableEnum;
import jcog.thing.Part;
import jcog.thing.Parts;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.container.grid.Containers;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.button.EnumSwitch;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.port.FloatPort;
import spacegraph.space2d.widget.port.util.Wiring;
import spacegraph.space2d.widget.slider.FloatSlider;
import spacegraph.space2d.widget.slider.IntSlider;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.space2d.widget.text.Labelling;
import spacegraph.space2d.widget.text.VectorLabel;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;

/**
 * constructs a representative surface for an object by reflection analysis
 */
public class ObjectSurface extends MutableUnitContainer<Surface> {

    private static final AutoBuilder.AutoBuilding<Object, Surface> DefaultObjectSurfaceBuilder = (ctx, target, obj) -> /*@Nullable*/ /*@Nullable*/ {

        List<Surface> outer = new Lst(0, EmptySurfaceArray);

        for (Pair<Object, Iterable<Surface>> p : target) {
            Surface l = collectionSurface(Streams.stream(p.getTwo()).filter(Objects::nonNull).collect(toList()));
            if (l!=null)
                outer.add(l);
        }

        return collectionSurface(outer);
    };


    private static @Nullable Surface collectionSurface(List<Surface> x) {
        Surface y = null;
        int xs = x.size();
        switch (xs) {
            case 0:
                return null; //TODO shouldnt happen
            case 1:
                //                //outer.add(new Scale(cx.get(0), Widget.marginPctDefault));
                y = x.get(0);
                break;

            default:
                if (xs == 2) {
                    y = new Splitting(x.get(0), 0.5f, x.get(1)).resizeable();
                }

                //TODO selector
                if (y == null)
                    y = new Gridding(x);
                break;
        }
        return y;
        //return new ObjectMetaFrame(obj, y, context);

    }

    public final AutoBuilder<Object, Surface> builder;

    /**
     * root of the object graph
     */
    private final Object obj;

    public ObjectSurface(Object x) {
        this(x, 1);
        if (x == null)
            throw new NullPointerException();
    }


    public ObjectSurface(Object x, int depth) {
        this(x, depth, builtin);
    }

    @SafeVarargs
    public ObjectSurface(Object x, int depth, Map<Class, BiFunction<?, Object, Surface>>... classers) {
        this(x, DefaultObjectSurfaceBuilder, depth, classers);
    }

    @SafeVarargs
    public ObjectSurface(Object x, AutoBuilder.AutoBuilding<Object, Surface> builder, int maxDepth, Map<Class, BiFunction<?, Object, Surface>>... classers) {
        this(x, new AutoBuilder(maxDepth, builder, classers));
    }

    public ObjectSurface(Object x, AutoBuilder<Object, Surface> builder) {
        super();
        this.obj = x;
        this.builder = builder;
    }


    public final Surface build(Object x) {
        if (x instanceof Surface)
            return ((Surface)x);

        return builder.build(x);
    }

    public static String objLabel(Object x, Object relation) {
        return relation == null ? x.toString() : relationLabel(relation);
    }

    public static String relationLabel(@Nullable Object relation) {
        if (relation == null) return "";
        else if (relation instanceof Field) {
            return ((Field) relation).getName();
        } else {
            return relation.toString(); //???
        }
    }


    @Override
    protected void starting() {

        builder.clear();

        set(wrap(built()));

        super.starting();
    }

    protected Surface wrap(Surface b) {
        return Containers.col(Containers.row(new BitmapLabel(name()), 0.9f,
            Containers.grid(
                PushButton.iconAwesome("download").clicked(()->{
                    throw new jcog.TODO(); /*load*/
                }),
                PushButton.iconAwesome("upload").clicked(()->{
                    throw new jcog.TODO(); /*save*/
                })
            )
        ), 0.9f, b);
    }

    private String name() {
        return obj.toString();
    }

    protected Surface built() {
        return builder.build(obj);
    }

    protected static String label(Object obj) {
        return obj.toString();
    }

    public static final Map<Class, BiFunction<?, Object, Surface>> builtin = new HashMap<>()
    {{
//        builder.annotation(Essence.class, (x, xv, e) -> {
//           return xv; //forward  //TODO
//        });

            put(Map.Entry.class, (Map.Entry x, Object relation) -> new VectorLabel(x.toString()));

            put(FloatRange.class, (FloatRange x, Object relation) -> new LiveFloatSlider(objLabel(x, relation), x.min, x.max, x, x::set));
            put(DoubleRange.class, (DoubleRange x, Object relation) -> new LiveFloatSlider(objLabel(x, relation), (float) x.min, (float) x.max, () -> (float) x.get(), x::set));

            put(PLink.class, (PLink x, Object relation) -> new LiveFloatSlider(objLabel(x, relation), 0, 1, x, x::pri));

            put(IntRange.class, (IntRange x, Object relation) -> x instanceof MutableEnum ? null : new MyIntSlider(x, relationLabel(relation)));

            put(Runnable.class, (Runnable x, Object relation) -> new PushButton(objLabel(x, relation), x));
            put(AtomicBoolean.class, (AtomicBoolean x, Object relation) -> new MyAtomicBooleanCheckBox(objLabel(x, relation), x));

            put(Loop.class, (Loop l, Object relation) -> new LoopPanel(l));

            put(MutableEnum.class, (MutableEnum<?> x, Object relation) -> EnumSwitch.the(x, relationLabel(relation)));

            put(String.class, (String x, Object relation) -> new VectorLabel(x)); //TODO support multi-line word wrap etc

            put(Part.class, (Part<?> p, Object rel) -> {
//                var P = new BitmapLabel((rel!=null ? rel + " -> " : "") + p.toString()); //ObjectSurface(p);

                //TODO make dynamic
                /*, 1 */
                List<Surface> subs = p instanceof Parts ?
                        ((Parts) p).subs().map(ObjectSurface::new)
                                .toList() : null;
                if (subs.isEmpty()) {
                    return null;
                } else {
                    //return new Splitting(P, 0.5f, new Surfaces(subs)).resizeable();
                    return new Surfaces<>(subs);
                }
            });

            put(Collection.class, (Collection<?> cx, Object relation) -> {
                if (cx.isEmpty())
                    return null;

                List<Surface> yy = new Lst<>(cx.size());

                //return SupplierPort.button(relationLabel(relation), ()-> {


//                for (Object cxx : cx) {
//                    if (cxx == null)
//                        continue;

//                    Surface yyy = build(cxx);
//                    if (yyy != null)
//                        yy.add(yyy); //TODO depth, parent, ..
//                }
                if (yy.isEmpty())
                    return null;

                Surface xx = collectionSurface(yy);

                String l = relationLabel(relation);

                return l.isEmpty() ? xx : Labelling.the(l, xx);
                //});
            });
//        classer.put(Surface.class, (Surface x, Object relation) -> {
//            return x.parent==null ? LabeledPane.the(relationLabel(relation), x) : x;
//        });

//        classer.put(Pair.class, (p, rel)->{
//           return new Splitting(build(p.getOne()), 0.5f, build(p.getTwo())).resizeable();
//        });
        }};

//        if (yLabel == null)
//            yLabel = x.toString();
//

//
//
//        //TODO rewrite these as pluggable onClass handlers
//
//        if (x instanceof Services) {
//            target.addAt(new AutoServices((Services) x));
//            return;
//        }
//
//        if (x instanceof Collection) {
//            Surface cx = collectElements((Iterable) x, depth + 1);
//            if (cx != null) {
//                target.addAt(new LabeledPane(yLabel, cx));
//            }
//        }
//
//


//    public static class ObjectMetaFrame extends MetaFrame {
//        public final Object instance;
//        public final Surface surface;
//        private final int instanceHash;
//        private final Object context;
//
//        public ObjectMetaFrame(Object instance, Surface surface, Object context) {
//            super(surface);
//            if (instance instanceof Surface)
//                throw new TODO();
//            this.context = context;
//            this.instance = instance;
//            this.instanceHash = instance.hashCode();
//            this.surface = surface;
//        }
//
//
//        @Override
//        protected void paintIt(GL2 gl, ReSurface r) {
//            super.paintIt(gl, r);
//            Draw.colorHash(gl, instanceHash, 0.25f);
//            Draw.rect(bounds, gl);
//        }
//
//        @Override
//        @Nullable
//        protected Surface label() {
//            String s;
//            if (context instanceof Field) {
//                s = ((Field) context).getName();
//            } else {
//                s = context != null ? context.toString() : (instance != null ? instance.toString() : null);
//            }
//
//            Surface provided = super.label();
//            if (s == null) {
//                return provided;
//            } else {
//                AbstractLabel l = new VectorLabel(s);
//                if (provided == null) return l;
//                else return Splitting.row(l, 0.3f, provided);
//            }
//        }
//
//
//        //TODO other inferred features
//    }



    //    private class AutoServices extends Widget {
//        AutoServices(Services<?, ?> x) {
//
//            List<Surface> l = new FasterList(x.size());
//
//            x.entrySet().forEach((ks) -> {
//                Service<?> s = ks.getValue();
//
//                if (addService(s)) {
//                    String label = s.toString();
//
//
//                    l.addAt(
//                            new PushButton(IconBuilder.simpleBuilder.apply(s)).click(() -> SpaceGraph.window(
//                                    new LabeledPane(label, new ObjectSurface(s)),
//                                    500, 500))
//
//
//                    );
//                }
//
//
//            });
//
//            setAt(new ObjectMetaFrame(x, new Gridding(l)));
//        }
//    }


    /*private boolean addService(Service<?> x) {
        return addAt(x);
    }*/

    private static class MyIntSlider extends IntSlider {
//        private final String k;

        MyIntSlider(IntRange p, String k) {
            super(p);
            text(k);
        }

//        @Override
//        public String text() {
//            return k;
//            //return k + '=' + super.text();
//        }
    }

    private static class MyAtomicBooleanCheckBox extends CheckBox {
        final AtomicBoolean a;

        public MyAtomicBooleanCheckBox(String yLabel, AtomicBoolean x) {
            super(yLabel, x);
            this.a = x;
        }

        @Override
        public boolean canRender(ReSurface r) {
            set(a.getOpaque()); //load
            return super.canRender(r);
        }
    }

    public static final class LiveFloatSlider extends FloatPort {

        //private static final float EPSILON = 0.001f;


        public final FloatSlider slider;
        private final FloatSupplier get;


        public LiveFloatSlider(String label, float min, float max, FloatSupplier get, FloatProcedure set) {
            super();

            this.get = get;
            slider = new FloatSlider(get.asFloat(), min, max).text(label).on(set);

            //set(LabeledPane.the(label, slider));
            set(slider);

            on(set::value);
        }


        @Override
        protected void renderContent(ReSurface r) {
            //TODO configurable rate
            boolean autoUpdate = true;
            if (autoUpdate) {
                slider.set(this.get.asFloat());
            }

            super.renderContent(r);
        }

        @Override
        protected void onWired(Wiring w) {
            out();
        }
    }

}