package spacegraph.util;

import jcog.math.v2;

/** maps one 2D coordinate to another
 * TODO rotation?  tolerance, gestures etc etc
 * TODO use matrix and allow for rotation etc
 * */
@FunctionalInterface public interface SurfaceTransform {

	void pixelToGlobal(float px, float py, v2 target);
}
