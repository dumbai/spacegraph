package spacegraph.space2d.widget.menu;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.unit.MutableUnitContainer;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by me on 12/2/16.
 */
public abstract class Menu extends MutableUnitContainer {

    protected final MenuView content;

    public final Map<String, Supplier<Surface>> menu;





    /** view model */
    public abstract static class MenuView {
        public abstract Surface view();

        public abstract void active(Surface surface);
        public abstract boolean inactive(Surface surface);

        public abstract boolean isEmpty();
    }

    public Menu(Map<String, Supplier<Surface>> menu, MenuView view) {
        super();
        this.menu = menu;
        this.content = view;
    }

//    public Menu setWrapper(UnaryOperator<Surface> wrapper) {
//        synchronized (this) {
//            this.wrapper = wrapper;
//            return this;
//        }
//    }


}
