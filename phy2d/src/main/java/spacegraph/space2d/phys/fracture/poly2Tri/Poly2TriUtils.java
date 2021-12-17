package spacegraph.space2d.phys.fracture.poly2Tri;

import java.util.concurrent.atomic.AtomicInteger;

enum Poly2TriUtils {
	;


	public static final double PI = Math.PI;

    
    public static final int UNKNOWN = 1;
    static final int INPUT = 2;
    static final int INSERT = 3;
    public static final int START = 4;
    public static final int END = 5;
    static final int MERGE = 6;
    static final int SPLIT = 7;
    static final int REGULAR_UP = 8;
    static final int REGULAR_DOWN = 9;

    static String typeToString(int type) {
        String result = switch (type) {
            case UNKNOWN -> "UNKNOWN";
            case INPUT -> "INPUT";
            case INSERT -> "INERT";
            case START -> "START";
            case END -> "END";
            case MERGE -> "MERGE";
            case SPLIT -> "SPLIT";
            case REGULAR_UP -> "REGULAR_UP";
            case REGULAR_DOWN -> "REGULAR_DOWN";
            default -> "??? (" + type + ')';
        };
        return result;
    }

    

    /**
     * In original poly2tri there is an exact arithemtic from
     * Jonathan Shewchuk ... sorry I didn't have time to reimplement
     * that (also I don't know if you can reimplement it 1:1 ...)
     */
    static double orient2d(double[] pa, double[] pb, double[] pc) {

        double pc0 = pc[0];
        double pc1 = pc[1];
        double detleft = (pa[0] - pc0) * (pb[1] - pc1);
        double detright = (pa[1] - pc1) * (pb[0] - pc0);

        return detleft - detright;
    }

    static final AtomicInteger l_id = new AtomicInteger();
    static final AtomicInteger p_id = new AtomicInteger();

}
