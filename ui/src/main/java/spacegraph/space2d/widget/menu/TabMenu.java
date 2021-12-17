package spacegraph.space2d.widget.menu;

import jcog.exe.Exe;
import spacegraph.SpaceGraph;
import spacegraph.layer.AbstractLayer;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.AspectAlign;
import spacegraph.space2d.meta.ErrorPanel;
import spacegraph.space2d.meta.LazySurface;
import spacegraph.space2d.meta.MetaHover;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.button.ToggleButton;
import spacegraph.space2d.widget.menu.view.GridMenuView;
import spacegraph.space2d.widget.text.Labelling;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabMenu extends Menu {

    public static final Function<String, ToggleButton> DEFAULT_BUTTON_BUILDER = CheckBox::new;
    static final Comparator<Map.Entry<String, ?>> keyComparator = Comparator.comparing(Map.Entry::getKey);
    private static final float MenuContentRatio = 0.9f;
    private final Splitting wrap;
    private final Map<String, ToggleButton> buttons = new LinkedHashMap<>();
    private final Gridding tabs = new Gridding();

    public TabMenu() {
        this(Map.of(), new GridMenuView());
    }

    public TabMenu(Map<String, Supplier<Surface>> options) {
        this(options, new GridMenuView());
    }

    public TabMenu(Map<String, Supplier<Surface>> options, MenuView view) {
        this(options, view, DEFAULT_BUTTON_BUILDER);
    }

    public TabMenu(Map<String, Supplier<Surface>> menu, MenuView view, Function<String, ToggleButton> buttonBuilder) {
        super(menu, view);

        set(wrap = new Splitting(tabs, 0, content.view()));
        set(menu, buttonBuilder);
    }

    public final void set(Map<String, Supplier<Surface>> menu) {
        set(menu, DEFAULT_BUTTON_BUILDER);
    }

    public synchronized void set(Map<String, Supplier<Surface>> menu, Function<String, ToggleButton> buttonBuilder) {
        tabs.set(buttons(menu, buttonBuilder).collect(Collectors.toList()));
    }

    private Stream<Surface> buttons(Map<String, Supplier<Surface>> menu, Function<String, ToggleButton> buttonBuilder) {
        return menu.entrySet().stream().sorted(keyComparator)
                .map(x -> toggle(buttonBuilder.apply(x.getKey()), x.getKey(),
                                viewWrapper(x.getKey(), x.getValue())));
    }

    protected Supplier<Surface> viewWrapper(String key, Supplier<Surface> builder) {
        return () -> Labelling.the(key, new LazySurface(builder));
    }

    public final TabMenu set(String item, boolean enable) {
        buttons.get(item).set(enable);
        return this;
    }

    void toggle(ToggleButton button, Supplier<Surface> creator, boolean onOrOff, Surface[] created, boolean inside) {

        //wrap/decorate

        //VectorLabel label = ((ToggleButton) button).label;

//        if (label!=null)
//            cx = LabeledPane.the(button.term() /*buttonBuilder.apply(label.text())*/, cx);

        if (onOrOff) {

            toggleOn(created, inside, build(creator));

        } else {

            toggleOff(created);

        }

    }

    private void toggleOff(Surface[] created) {
        synchronized (this) {
            if (created[0] != null) {
                boolean removed = content.inactive(created[0]);
                assert (removed);
                created[0] = null;

                if (content.isEmpty())
                    unsplit();

            }
        }
    }

    private Surface build(Supplier<Surface> creator) {
        Surface cx;
        try {
            cx = creator.get();
            if (cx == null) throw new NullPointerException(creator.toString());
        } catch (Throwable t) {
            cx = new ErrorPanel(t);
        }
        return cx;
    }

    private void toggleOn(Surface[] created, boolean inside, Surface cx) {
        if (inside) {
            synchronized (this) {
                content.active(created[0] = cx);
                split();
            }
        } else {
            Exe.run(() -> {
                //TODO position to mouse cursor
                //OrthoSurfaceGraph w = (OrthoSurfaceGraph)
                AbstractLayer w = SpaceGraph.window(cx, 800, 800);
                //w.video.setPosition(button.);
            });
        }
    }


//    public void addToggle(String label, Supplier<Surface> creator) {
//        toggle(CheckBox::new, label, creator);
//    }

    public Surface toggle(ToggleButton b, String label, Supplier<Surface> creator) {
        Surface[] created = {null}; //holder for created surface

        ToggleButton bb = b.on((button, onOrOff) -> //spawn inside
                toggle(button, creator, onOrOff, created, true));

        AspectAlign ccc = new AspectAlign(
                PushButton.iconAwesome("external-link")
                        .clicked(() -> toggle(null, creator, true, created, false)), //spawn outside
                1, AspectAlign.Align.TopRight, 0.25f, 0.25f);

        buttons.put(label, bb); //keyed access for external API

        return new MetaHover(bb, () -> ccc);
    }

    protected void split() {
        wrap.split(MenuContentRatio);
    }

    protected void unsplit() {
        wrap.split(0.0f);
    }


}