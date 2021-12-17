package spacegraph.space2d.meta;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.space2d.container.layout.Force2D;
import spacegraph.space2d.container.unit.Scale;
import spacegraph.space2d.widget.port.Surplier;
import spacegraph.space2d.widget.text.Labelling;

import java.util.Map;
import java.util.function.BiFunction;

public class ObjectGraphs extends Bordering {
    public static final float INNER_CONTENT_SCALE = 0.9f;
    final Iterable content;
    final Graph2D graph;
    private final Map<Class, BiFunction<?, Object, Surface>> builders;

    public ObjectGraphs(Iterable content, Map<Class, BiFunction<?, Object, Surface>> builders, Graph2D.Graph2DRenderer renderer) {

        this.builders = builders;
        this.content = content;

        this.graph = graph(objectGraph(renderer));
        set(graph.widget());
    }
    public static Graph2D objectGraph(Graph2D.Graph2DRenderer<Object> renderer) {
        return new Graph2D<>()
                .render(renderer)
                .update(new Force2D<>());// {
//				@Override
//				protected void size(MutableRectFloat m, float a) {
//					float q = m.node.pri;
//					float s = (float) (Math.sqrt((Math.max(0, q))));
//					//w = Util.clamp(s * a, 2, 32);
//					float w = a * (0.1f + s);
//					m.size(w, w);
//				}
        //});


    }

    public synchronized void update() {
        graph.set(content);
    }


//
//		public void layer(Object key) {
//
//			if (content != null)
//				computeIfAbsent(key, oo -> graph(objectGraph(renderer)).widget().pos(bounds));
//			else
//				remove(key);
//
//
//		}

    boolean lazy;

    private Graph2D<Object> graph(Graph2D<Object> g) {
        g.build(x -> {
                    Object xid = x.id;
                    String label = xid.toString();

                    x.set(lazy ? Surplier.button(label, () -> icon(xid, label)) : icon(xid, label));


                    //new CheckBox(xid.toString()).on((boolean v) -> {
//if (v) {
//							Surface o = new ObjectSurface(x, new AutoBuilder<Object, Surface>(
//								2, ObjectSurface.DefaultObjectSurfaceBuilder,
//								new Map[] { ObjectSurface.builtin }));
//
//							if (o instanceof ContainerSurface) {
//								ContainerSurface c = (ContainerSurface)o;
//								int cc = c.childrenCount();
//								if (cc == 1) {
//									c.forEach(ccc -> {
//										layer(xid, List.of(ccc));
//									});
//								} else {
//									List<Surface> components = new FasterList(cc);
//
//									c.forEach(components::add);
//									layer(xid, components);
//								}
//							} else if (o!=null) {
//								layer(xid, List.of(o));
//							}
//						} else
//							layer(xid, null);


                }
        );
        return g;
    }


    private Scale icon(Object xid, String label) {
        return new Scale(
                Labelling.the(label,
                        new ObjectSurface(xid, 1,
                                builders, ObjectSurface.builtin)
                ), INNER_CONTENT_SCALE);
    }


}