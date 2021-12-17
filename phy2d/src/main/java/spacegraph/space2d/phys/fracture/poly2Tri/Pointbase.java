package spacegraph.space2d.phys.fracture.poly2Tri;

public class Pointbase implements Comparable {

    /**
     * id of point;
     */
    public int id = -1;

    /**
     * coordinates;
     */
    public double x = 0;
    public double y = 0;

    /**
     * type of points;
     */
    
    public int type = Poly2TriUtils.UNKNOWN;

    /**
     * left chain or not;
     */
    public boolean left = false;

    public Pointbase() {

    }

    public Pointbase(Pointbase pb) {
        this.id = pb.id;
        this.x = pb.x;
        this.y = pb.y;
        this.type = pb.type;
        this.left = pb.left;
    }

    public Pointbase(double xx, double yy) {
        x = xx;
        y = yy;
    }

    public Pointbase(int idd, double xx, double yy) {
        id = idd;
        x = xx;
        y = yy;
    }

    public Pointbase(double xx, double yy, int ttype) {
        id = 0;
        x = xx;
        y = yy;
        type = ttype;
    }

    public Pointbase(int idd, double xx, double yy, int ttype) {
        id = idd;
        x = xx;
        y = yy;
        type = ttype;
    }

    
    public void rotate(double theta) {
        double cosa = Math.cos(theta), sina = Math.sin(theta);
        double newx = x * cosa - y * sina;
        double newy = x * sina + y * cosa;
        x = newx;
        y = newy;
    }

    
    
    public boolean equals(Object o) {
        if (!(o instanceof Pointbase)) return false;
        return equals((Pointbase) o);
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean equals(Pointbase pb) {
        return (this.x == pb.x) && (this.y == pb.y);
    }

    
    
    public int compareTo(Object o) {
        
        
        
        
        if (!(o instanceof Pointbase pb)) return -1;
        if (this.equals(pb)) return 0;
        if (this.y > pb.y) return 1;
        else if (this.y < pb.y) return -1;
        else if (this.x < pb.x) return 1;
        else return -1;
    }

    
    

    
    public String toString() {
        return "Pointbase([" + x + ", " + y + "], ID = " + id + ", " + Poly2TriUtils.typeToString(type) + ')';
    }
}