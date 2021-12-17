package spacegraph.space2d;

import jcog.event.Off;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;
import spacegraph.util.SpaceLogger;
import spacegraph.util.animate.Animated;
import spacegraph.video.JoglWindow;

import java.util.function.Consumer;

/** global UI context for a hierarchy of surfaces */
public interface SurfaceGraph extends Surfacelike, SpaceLogger {

    default SurfaceGraph root() {
        return this;
    }


//    /**
//     * puts value into singleton table
//     * can provide special handling for lifecycle states of stored entries
//     * by providing a callback which will be invoked when the value is replaced.
//     * <p>
//     * if 'added' == null, it will attempt to remove any set value.
//     */
//    void the(String key, @Nullable Object added, @Nullable Runnable onRemove);

//    /**
//     * gets value from the singleton table
//     */
//    Object the(String key);

//    default void the(Class key, @Nullable Object added, @Nullable Runnable onRemove) {
//        the(key.toString(), added, onRemove);
//    }

//    default Object the(Class key) {
//        return the(key.toString());
//    }


    /**
     * attaches an event handler for updates (less frequent than render cycle)
     */
    Off onUpdate(Consumer<JoglWindow> c);

    Off animate(Animated c);

    default Off animate(FloatProcedure c) {
        return animate(dt ->{c.value(dt); return true; });
    }

    default boolean keyFocus(Surface textEdit) {
        return false;
    }


}
