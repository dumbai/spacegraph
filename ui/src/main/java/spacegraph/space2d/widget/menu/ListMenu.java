package spacegraph.space2d.widget.menu;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.EmptySurface;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.container.grid.Containers;
import spacegraph.space2d.widget.button.ButtonSet;
import spacegraph.space2d.widget.button.CheckBox;

import java.util.Map;
import java.util.function.Supplier;

/** selectable list, views one item at a time */
public class ListMenu extends Menu {

    private final Splitting wrap;

    /** TODO
     *      options for the border/size of the menu
     *      One/Multi mode
     *
     */
    public ListMenu(Map<String, Supplier<Surface>> menu, MenuView view) {
        super(menu, view);
        ButtonSet index = new ButtonSet(ButtonSet.Mode.One, menu.entrySet().stream().map(e -> new CheckBox(e.getKey()).on(() ->
                view(e.getValue())))::iterator);
        index.vertical();
        wrap = Containers.row(index, 0.2f, new EmptySurface());
        set(wrap);
    }

    private void view(Supplier<Surface> value) {
        Surface v = value.get();
        wrap.R(v);
    }

}