package spacegraph.space2d;

public class SurfaceCamera {
    public float scaleX;
    public float scaleY;
    public float x1;
    public float x2;
    public float y1;
    public float y2;
    /**
     * viewable pixel resolution
     */
    public float pw;
    public float ph;
    public transient float w;
    public transient float h;

    public SurfaceCamera clone() {
        SurfaceCamera s = new SurfaceCamera();
        s.scaleX = scaleX;
        s.scaleY = scaleY;
        s.x1 = x1;
        s.x2 = x2;
        s.y1 = y1;
        s.y2 = y2;
        s.pw = pw;
        s.ph = ph;
        s.w = w;
        s.h = h;
        return s;
    }

    public SurfaceCamera set(SurfaceCamera s) {
        if (this != s) {
            this.scaleX = s.scaleX;
            this.scaleY = s.scaleY;
            this.x1 = s.x1;
            this.x2 = s.x2;
            this.y1 = s.y1;
            this.y2 = s.y2;
            this.pw = s.pw;
            this.ph = s.ph;
            this.w = s.w;
            this.h = s.h;
        }
        return this;
    }

}
