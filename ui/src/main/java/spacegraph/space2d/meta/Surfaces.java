package spacegraph.space2d.meta;

import jcog.data.list.Lst;
import jcog.exe.RunUnique;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.text.BitmapLabel;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/** generic collection view w/ multi-mode choice */
public class Surfaces<X> extends Bordering {

    private static Map<PushButton,Function> renderersDefault(){ return new LinkedHashMap<>() {{
        put(PushButton.iconAwesome("sitemap" /* Object */),
            z -> z instanceof Surface ? z : new ObjectSurface(z));
        put(PushButton.iconAwesome("cog" /* Description*/),
            z -> new BitmapLabel(z.toString()));
    }};}


    private static Map<PushButton,Function<Collection<Surface>,Surface>> containersDefault() { return new LinkedHashMap<>() {{
        put(PushButton.iconAwesome("th" /* grid */), Gridding::new);
        put(PushButton.iconAwesome("th" /* grid */), ScrollGrid::fromList);
        put(PushButton.iconAwesome("ellipsis-h" /* row */),
            c -> new Gridding(c).aspect(0));
        put(PushButton.iconAwesome("ellipsis-v" /* col */),
            c -> new Gridding(c).aspect(Float.POSITIVE_INFINITY));
        put(PushButton.iconAwesome("mouse-pointer" /* graph */),
            c -> Surface.TODO.get());
    }};}

    private Iterable<X> source = Collections.EMPTY_SET;

    private final Map<PushButton,Function<Collection<Surface>,Surface>> containers = new LinkedHashMap();

    /** item renderers */
    private final Map<PushButton,Function<X,Surface>> renderers = new LinkedHashMap<>();

    private final transient Lst<Surface> _surfaces = new Lst();
    private final transient Lst<X> _source = new Lst();
    private PushButton renderer;
    private PushButton container;

    public Surfaces() {
        this(Surfaces.containersDefault(), Surfaces.renderersDefault());
    }

    public Surfaces(Map containersDefault, Map renderersDefault) {
        renderers.putAll(renderersDefault);
        containers.putAll(containersDefault);
        renderer = (PushButton) renderersDefault.keySet().iterator().next();
        container = (PushButton) containersDefault.keySet().iterator().next();
        //set(List.of(), renderer, container);
    }

    public Surfaces(Stream<X> x) {
        this(x.collect(toList()));
    }

    public Surfaces(Iterable<X> x) {
        this();
        set(x);
    }

    public final Surfaces<X> set(Iterable<X> c) {
        return set(c, renderer, container);
    }

    private Surfaces<X> set(Iterable<X> c, PushButton renderer, PushButton container) {
        RunUnique.runUnique(this, ()-> _set(c, renderer, container));
        return this;
    }

    private void _set(Iterable<X> x, PushButton renderer, PushButton container) {
        _source.clear();
        _surfaces.clear();
        (this.source = x).forEach(_source::add);

        Surface y = containers.get(this.container = container).apply(_source.stream()
                .map(renderers.get(this.renderer = renderer))
                .collect(Collectors.toCollection(() -> _surfaces)));
        if (_surfaces.size() <= 1) {
            south(null); west(null);
        } else {
            //HACK
            //borderSize(S, _surfaces.size() <= 1 ? 0 : BORDER_SIZE_DEFAULT);
            borderSouth = borderWest = 0.05f;
            west(new Gridding(renderers.keySet().stream().peek(r ->
                    r.clicked(() -> set(source, r, container))).toList()));
            south(new Gridding(containers.keySet().stream().peek(c ->
                    c.clicked(() -> set(source, renderer, c))).toList()));
        }
        set(y);

    }

//    private void rerender() {
//        set(source, renderer, container);
//    }

}