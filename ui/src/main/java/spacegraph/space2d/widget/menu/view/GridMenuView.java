package spacegraph.space2d.widget.menu.view;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.menu.Menu;

public class GridMenuView extends Menu.MenuView {


    final Gridding view = new Gridding();

    public GridMenuView() {

    }

    public GridMenuView aspect(float aspect) {
        view.aspect(aspect);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return view.isEmpty();
    }

    @Override
    public Surface view() {
        return view;
    }

    @Override
    public void active(Surface surface) {
        view.add(surface);
    }

    @Override
    public boolean inactive(Surface surface) {
        return view.remove(surface);
    }
}
