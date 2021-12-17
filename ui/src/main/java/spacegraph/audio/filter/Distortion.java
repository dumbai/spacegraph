package spacegraph.audio.filter;


import static java.lang.Math.atan;

public enum Distortion {
	;

	public static double distortion(double x, double gain_pre, double gain_post) {
//        double y;
//        if (x > 0)
//            y = 1 - exp(-x * gain);
//        else
//            y = -1 + exp(x * gain);
//        return y;


        double max = 1.0 / atan(gain_pre);

        return atan(x * gain_pre) * max * gain_post;

    }
}
