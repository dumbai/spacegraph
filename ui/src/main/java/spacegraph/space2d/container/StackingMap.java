//package spacegraph.space2d.container;
//
//import jcog.data.map.ConcurrentFastIteratingHashMap;
//import org.jetbrains.annotations.Nullable;
//import spacegraph.space2d.Surface;
//import spacegraph.space2d.container.collection.MutableContainer;
//
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.function.Predicate;
//
//public class StackingMap<X,Y extends Surface> extends MutableContainer {
//
//	private final ConcurrentFastIteratingHashMap<X, Y> map;
//
//	public StackingMap(Y[] emptyArray) {
//		super();
//		this.map = new ConcurrentFastIteratingHashMap<>(emptyArray);
//	}
//
//	@Override
//	public void add(Surface... s) {
//		throw new UnsupportedOperationException();
//	}
//
//	@Override
//	protected boolean _remove(Y y) {
//		throw new jcog.TODO();
//	}
//
//	public Y computeIfAbsent(X x, Function<X, Y> s) {
//		return map.compute(x, (xx, xp) -> {
//			Y xn = s.apply(xx);
//			if (xp != null) {
//				if (xp == xn) return xn;
//				else {
//					stop(xp);
//				}
//			}
//			return start(xn);
//		});
//	}
//
//	private Y start(@Nullable Y s) { if (s!=null) s.start(this); return s;}
//	private Y stop(@Nullable Y s) { if (s!=null) s.stop(); return s; }
//
//	public Surface put(X x, Y s) {
//		return stop(map.put(x, start(s)));
//	}
//
//	public Surface remove(Object x) {
//		return stop(map.remove(x));
//	}
//
//	@Override
//	protected MutableContainer clear() {
//		map.clear();
//		return this;
//	}
//
//	@Override
//	public void doLayout(float dtS) {
//		forEachWith(Surface::pos, bounds);
//	}
//
//	@Override
//	public int childrenCount() {
//		return map.size();
//	}
//
//	@Override
//	public void forEach(Consumer<Surface> o) {
//		map.forEachValue(o);
//	}
//
//	@Override
//	public boolean whileEach(Predicate<Surface> o) {
//		return map.whileEachValue(o);
//	}
//
//	@Override
//	public boolean whileEachReverse(Predicate<Surface> o) {
//		return map.whileEachValueReverse(o);
//	}
//}