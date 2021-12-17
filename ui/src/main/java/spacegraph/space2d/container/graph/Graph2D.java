package spacegraph.space2d.container.graph;

import com.google.common.collect.Iterables;
import com.jogamp.opengl.GL2;
import jcog.data.graph.AdjGraph;
import jcog.data.graph.NodeGraph;
import jcog.data.map.CellMap;
import jcog.data.pool.MetalPool;
import jcog.data.pool.Pool;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.container.collection.MutableListContainer;
import spacegraph.space2d.container.collection.MutableMapContainer;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.Clipped;
import spacegraph.space2d.meta.ObjectSurface;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.space2d.widget.textedit.TextEdit;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 2D directed/undirected graph widget
 * designed for high-performance realtime animated visualization of many graph nodes and edges
 * that can appear, disappear, and re-appear between frames
 * <p>
 * TODO generify for use in Dynamics3D
 */
public class Graph2D<X> extends MutableMapContainer<X, NodeVis<X>> {


	private static final Graph2DUpdater NullUpdater = (c, d) -> {
	};
	/**
	 * invalidates all edges by setting their dirty flag
	 */
	protected static Graph2DRenderer InvalidateEdges = (n, g) -> n.invalidateEdges();
	protected final GraphEditor<X> edit = new GraphEditor<>(this);
//	private final AtomicBoolean busy = new AtomicBoolean(false);

	private final Pool<EdgeVis<X>> edgePool = new MetalPool<>() {
		@Override public EdgeVis<X> create() {
			return new EdgeVis<>();
		}
	};

	private List<Graph2DRenderer<X>> renderers = Collections.EMPTY_LIST;

	private Consumer<NodeVis<X>> builder = x -> {
		//generic default builder
		//x.set(new PushButton(x.id.toString()));
		x.set(new VectorLabel(x.id.toString()));
	};

	private volatile Graph2DUpdater<X> updater = NullUpdater;

	private final transient Set<NodeVis<X>> wontRemain = Collections.newSetFromMap(new IdentityHashMap<>());

	@Nullable
    private transient volatile Iterable<X> nextNodes;
	private transient volatile boolean nextAddOrReplace;


	public Graph2D() {
		this(NullUpdater);
	}

	public Graph2D(Graph2DUpdater<X> updater) {
		super();
//        clipBounds = false;
		update(updater);
	}

	@Override
	public boolean delete() {
		if (super.delete()) {
			edgePool.delete();
//            nodeCache.clear();
			//TODO anything else?
			return true;
		}
		return false;
	}

	@Override
	protected void hide(NodeVis<X> key, Surface s) {
		s.hide();
	}

	public Surface widget() {
		return widget(null);
	}

	/**
	 * TODO impl using MetaFrame menu
	 */
	public Surface widget(Object controls) {
		var cfg = configWidget();

		if (controls != null) {
			cfg.add(new ObjectSurface(controls));
		}

		addControls(cfg);

		return new Splitting(new Clipped(
			this
		)/* {
			@Override
			protected void paintIt(GL2 gl, ReSurface r) {

				gl.glColor4f(0, 0, 0, 0.9f);
				Draw.rect(bounds, gl);

				super.paintIt(gl, r);
			}
		}*/, 0.1f, cfg).resizeable();
	}

	protected void addControls(MutableListContainer cfg) {

	}

	/**
	 * adds a rendering stage.  these are applied successively at each visible node
	 */
	@SafeVarargs
	public final Graph2D<X> render(Graph2DRenderer<X>... renderStages) {
		synchronized(this) {
			List<Graph2DRenderer<X>> nextRenderStages = List.of(renderStages);
			if (!renderers.equals(nextRenderStages)) {
				renderers = nextRenderStages;
				layout();
			}
		}
		return this;
	}

	public Graph2D<X> build(Consumer<NodeVis<X>> builder) {
		synchronized(this) {
			if (this.builder != builder) {
				this.builder = builder;
				layout();
			}
		}
		return this;
	}

	/**
	 * TODO to support dynamically changing updater, apply the updater's init procdure to all existing nodes.  do this in between frames so there is no interruption if rendering current frame.  this means saving a 'nextUpdater' reference to delay application
	 */
	public Graph2D<X> update(Graph2DUpdater<X> u) {
		synchronized(this) {
			if (this.updater != u) {
				this.updater = u;
				layout();
			}
		}
		return this;
	}

	public MutableListContainer configWidget() {
		Gridding g = new Gridding();
		g.add(new ObjectSurface(updater));
		for (Graph2DRenderer l : renderers)
			g.add(new ObjectSurface(l));
		return g;
	}

	public int nodes() {
		return cells.size();
	}

	public void nodes(Consumer<NodeVis<X>> each) {
		cells.map.values().forEach(c -> each.accept(c.value));
	}

	@Override
	protected void doLayout(float dtS) {
		super.doLayout(dtS);

		@Nullable Iterable<X> nextNodes = this.nextNodes;
		if (nextNodes!=null) {
			this.nextNodes = null; //TODO CAS
			updateNodes(nextNodes, nextAddOrReplace);
			render();
		}
	}

	@Override
	protected boolean canRender(ReSurface r) {
		if (super.canRender(r)) {

			forEachValue(NodeVis::pre);
			updater.update(this, r.frameDT);
			forEachValue(NodeVis::post);

			return true;
		}
		return false;
	}

	@Override
	protected void paintIt(GL2 gl, ReSurface r) {
		cells.forEachValue(n -> {
			if (n.visible())
				n.paintEdges(gl);
		});
	}

	public final Graph2D<X> add(Stream<X> nodes) {
		return add(nodes::iterator);
	}

	public final Graph2D<X> set(Stream<X> nodes) {
		return set(nodes::iterator);
	}

	public final Graph2D<X> add(Iterable<X> nodes) {
		return update(nodes, true);
	}

	public final Graph2D<X> set(Iterable<X> nodes) {
		return update(nodes, false);
	}

	public final Graph2D<X> set(AdjGraph g) {
		return set(g.vertices());
	}

	public final Graph2D<X> set(NodeGraph g) {
		return set(g.nodes());
	}

	public final Graph2D<X> add(NodeGraph g) {
		return add(g.nodes());
	}

	@Override
	public TextEdit clear() {
		set(Collections.EMPTY_LIST);
		return null;
	}


	private Graph2D<X> update(Iterable<X> nodes, boolean addOrReplace) {
		nextAddOrReplace = addOrReplace;
		nextNodes = nodes;
		layout();

//		if (!busy.compareAndSet(false, true))
//			return this;
//
//		try {
//			updateNodes(nodes, addOrReplace);
//			render();
//		} finally {
//			busy.set(false);
//		}

		return this;
	}

	@Override
	protected void stopping() {
		edgePool.delete();
		super.stopping();
	}



	private void updateNodes(Iterable<X> nodes, boolean addOrReplace) {


		Set<NodeVis<X>> ww;
		if (!addOrReplace) {
 			ww = this.wontRemain;
 			nodes(nv -> {
				if (nv!=null) wontRemain.add(nv);
			});
			if (ww.isEmpty()) ww = null;
		} else
			ww = null;


		try {
			for (X _x : nodes) {
				if (_x == null) continue; //HACK
				NodeVis<X> n = nodeOrAdd(_x);
				if (ww != null) ww.remove(n);
			}
		} catch (NoSuchElementException e) {
			//TODO HACK
		}

		if (ww!=null) {
			cells.removeAll(Iterables.transform(ww, x ->x.id));
			ww.clear();
		}

	}

	private NodeVis<X> nodeOrAdd(X _x) {

		NodeVis<X> cv = compute(key(_x),
			xx -> xx == null ? materialize(_x) : rematerialize(key(_x), xx)
		).value;
		if (cv.parent == null)
			cv.start(this);

		cv.show();
		return cv;
	}

	protected X key(X x) {
		return x;
	}

	private NodeVis<X> materialize(X x) {
		NodeVis<X> yy = new NodeVis<>();
		yy.start(x);
		builder.accept(yy);
		updater.init(yy, this);
		renderers.forEach(r -> r.node(yy, edit));

		return yy;
	}

	/**
	 * node continues being materialized
	 */
	private NodeVis<X> rematerialize(X key, NodeVis<X> xx) {
		xx.update();
		return xx;
	}

	@Override
	protected final void unmaterialize(NodeVis<X> v) {
		v.end(edgePool);
		v.delete();
	}

	private void render() {
		for (Graph2DRenderer<X> layer : renderers)
			layer.nodes(cells, edit);
	}


	/**
	 * iterative animated geometric update; processes the visual representation of the content
	 */
	@FunctionalInterface
	public interface Graph2DUpdater<X> {

		void update(Graph2D<X> g, float dtS);

		/**
		 * set an initial location (and/or size) for a newly created NodeVis
		 */
		default void init(NodeVis<X> newNode, Graph2D<X> g) {

		}
	}


	/**
	 * one of zero or more sequentially-applied "layers" of the representation of the graph,
	 * responsible for materializing/decorating/rendering each individual node it is called for,
	 * and the graph that holds it (including edges, etc) via the supplied GraphEditing interface.
	 */
	@FunctionalInterface
	public interface Graph2DRenderer<X> {

		/**
		 * called for each node being processed.  can edit the NodeVis
		 * and generate new links from it to target nodes.
		 */
		void node(NodeVis<X> node, GraphEditor<X> graph);

		default void nodes(CellMap<X, ? extends NodeVis<X>> cells, GraphEditor<X> edit) {
			cells.forEachValue(nv -> {
				if (nv.visible())
					node(nv, edit);
			});
		}

	}

	/**
	 * wraps all graph construction procedure in this interface for which layers construct graph with
	 */
	public static final class GraphEditor<X> {

		final Graph2D<X> g;

		GraphEditor(Graph2D<X> g) {
			this.g = g;
		}

		public @Nullable NodeVis<X> node(Object x) {
			return g.cells.get(x);
		}

		public NodeVis<X> nodeOrAdd(X x) {
			return g.nodeOrAdd(x);
		}

		/**
		 * adds a visible edge between two nodes, if they exist and are visible
		 */
		public @Nullable EdgeVis<X> edge(Object from, Object to) {
			@Nullable NodeVis<X> fromNode = from instanceof NodeVis ? ((NodeVis)from) : node(from);
			return fromNode != null ? edge(fromNode, to) : null;
		}

		/**
		 * adds a visible edge between two nodes, if they exist and are visible
		 */
		public @Nullable EdgeVis<X> edge(NodeVis<X> from, Object to) {

			@Nullable NodeVis<X> t = to instanceof NodeVis ? (NodeVis)to : g.cells.getValue(to);
			if (t == null) return null;

			if (from == t) return null; //ignored TOOD support self edges?

			return from.out(t, g.edgePool);
		}

	}

}