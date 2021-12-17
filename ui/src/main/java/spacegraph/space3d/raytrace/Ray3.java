package spacegraph.space3d.raytrace;

import jcog.math.v3d;

public class Ray3 {

    public v3d position;
    public v3d direction;

    Ray3() {
        this(new v3d(0,0,0), new v3d(0,0,0));
    }

    Ray3(v3d position, v3d direction) {
        this.position = position;
        this.direction = direction;
    }
}
