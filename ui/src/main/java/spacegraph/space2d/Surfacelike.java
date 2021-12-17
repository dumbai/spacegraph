package spacegraph.space2d;

import org.jetbrains.annotations.Nullable;


/** component of a Surface hierarchy; at minimum it is able to discover its root */
@FunctionalInterface
public interface Surfacelike {

    @Nullable SurfaceGraph root();

}
