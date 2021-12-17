package spacegraph.space2d.meta;

import jcog.Util;
import jcog.util.ClassReloader;
import spacegraph.space2d.MenuSupplier;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.text.VectorLabel;

import java.lang.reflect.Constructor;
import java.net.URISyntaxException;

public class ClassReloadingSurface<C extends Surface> extends MutableUnitContainer implements MenuSupplier {

    private final ClassReloader reloader;
    private final String className;
    private final Object[] args;
    private Class<? extends C> currentClass;

    public ClassReloadingSurface(Class<? extends C> c, Object... args) {
        super();

        busy();

        this.args = args;
        this.className = c.getName();

        ClassReloader r;
        try {
            r = ClassReloader.inClassPathOf(c);

        } catch (URISyntaxException e) {
//            e.printStackTrace();
            error(e);
            r = null;
        }
        this.reloader = r;

        reload();

    }

    void busy() {
        set(new VectorLabel(".."));
    }

    void error(Throwable e) {
        e.printStackTrace();
        String m = e.getMessage();
        set(new VectorLabel(m!=null ? m : e.toString()));
    }

    public void reload() {
        reload(false);
    }

    public synchronized void reload(boolean forceIfSameClass) {
        if (reloader == null) return;

        Surface previous = the();
        busy();

        try {
            Class<?> cc = reloader.reloadClass(className);
            if (forceIfSameClass || currentClass != cc) {
                Constructor<?> ctor = cc.getConstructor(Util.typesOfArray(args));
                Surface s = (Surface) ctor.newInstance(args);
                set(s);
                currentClass = (Class<? extends C>) cc;
            } else {
                set(previous);
            }
        } catch (Exception e) {
            error(e);
        }

    }

    @Override
    public Surface menu() {
        return new Gridding(new PushButton("Reload", this::reload), new VectorLabel(className));
    }
}