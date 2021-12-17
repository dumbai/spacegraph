package spacegraph;

import jcog.Config;

/** spacegraph global / static parameters */
public enum UI {
	;

	/** economic "standby" FPS for newly-created uninitialized windows */
    public static final float FPS_init = 15;

    public static float FPS_default = Config.INT("FPS",
		30
			//	60
	);

	/**
	 * factor to decrease FPS of unfocused windows
	 */
	public static float renderFPSUnfocusedRate = 0.5f;
}