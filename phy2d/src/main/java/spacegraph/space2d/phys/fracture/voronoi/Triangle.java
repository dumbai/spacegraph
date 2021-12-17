package spacegraph.space2d.phys.fracture.voronoi;

import jcog.math.v2;

/**
 * Trojuholnik delaunay triangulacie
 *
 * @author Marek Benovic
 */
public class Triangle {
    /**
     * Index bodu trojuholnika
     */
    public int i;
    public int j;
    public int k;

    /**
     * Ak je nastavena referencia, ukazuje na trojuholnik, ktory ma rovnaky focus (v Vec2 formate). focusCorelation.focusCorelation je vzdy null.
     */
    private Triangle focusCorelation;

    /**
     * Suradnice stredu opisanej kruznice.
     */
    public double dX;
    public double dY;

    int index; 
    private double r;

    Triangle(int index) {
        this.index = index;
    }

    /**
     * Inicializuje trojuholnik
     *
     * @param i 1. index vrcholu
     * @param j 2. index vrcholu
     * @param k 3. index vrcholu
     * @param p Pole vrcholov
     * @param t protilahly trojuholnik
     */
    final void init(int i, int j, int k, v2[] p, Triangle t) {
        this.i = i;
        this.j = j;
        this.k = k;

        
        v2 a = p[i];
        v2 b = p[j];
        v2 c = p[k];

        double Bx = (double) b.x - a.x;
        double By = (double) b.y - a.y;
        double Cx = (double) c.x - a.x;
        double Cy = (double) c.y - a.y;
        double D = 1.0 / (2 * (Bx * Cy - By * Cx)); 
        double Bs = Bx * Bx + By * By;
        double Cs = Cx * Cx + Cy * Cy;
        double x = (Cy * Bs - By * Cs) * D;
        dX = x + a.x;
        double y = (Bx * Cs - Cx * Bs) * D;
        dY = y + a.y;

        
        
        r = (x * x + y * y) * (1.0 - 1.0E-15);

        
        if (t != null && (float) dX == (float) t.dX && (float) dY == (float) t.dY) {
            focusCorelation = t.focusCorelation != null ? t.focusCorelation : t; 
        } else {
            focusCorelation = null;
        }
    }

    /**
     * @param I
     * @param J
     * @return Vrati index toho vrcholu, ktory nieje v niektorom z parametrov
     */
    final int get(int I, int J) {
        return i == I ? j == J ? k : j : i == J ? j == I ? k : j : i;
    }

    /**
     * @param v
     * @return Vrati true, ak sa bod v nachadza vo vnutri opisanej kruznice
     */
    final boolean inside(v2 v) {
        return dis(v) < r;
    }

    private double dis(v2 v) {
        double x = dX - v.x;
        double y = dY - v.y;
        return x * x + y * y;
    }
}