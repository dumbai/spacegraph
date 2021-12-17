package spacegraph.space2d.phys.fracture.voronoi;

import jcog.math.v2;
import jcog.random.XoRoShiRo128PlusRandom;

import java.util.Arrays;
import java.util.Random;

/**
 * Voronoi diagram - perfektna cista dokonala implementacia ktora zozerie akykolvek
 * vstup, vrati chybu, ak su tam duplicity, alebo null, alebo notFinite cisla
 * max 256 ohnisk, super optimalizacia, minimalizacia alokacie pamate, moznost
 * multithreadingu - kazda instancia Voronoi sa bude volat v samostatnom vlakne.
 * GPU akceleracia alebo parallelizmus tam moc nehra. vsetko bude na CPU.
 * Minimalisticke rozhranie, minimalny pocet tried, co najjednoduchsie.
 * Hlavne pamatove naroky ma tabulka Edges, ktora je kvadraticka -> ta spolu
 * s hranami robi cca 1MB. Vdaka tomu vsak kniznica je relativne vykonna:
 * <p>
 * Vstup: body musia mat Float.finite hodnoty, nesmu byt duplicitne a pre voronoi
 * diagram sa musia nachadzat v ohraniceni.
 * <p>
 * 23.0 kf (0.0000434s) na generovanie 100 bodov pre delaunay triangulaciu
 * 15.6 kF (0.0000641s) na generovanie 100 bodov voronoi diagramu
 *
 * @author Marek Benovic
 */
public class SingletonVD {
    /**
     * Body voronoi diagramu. Rozmedzie platnych bodov: (0, pCount - 1).
     * Nenastavovat hodnotam null value.
     */
    public final v2[] points = new v2[0x404];

    /**
     * Pocet bodov voronoi diagramu.
     */
    public int pCount = 0;

    /**
     * Vystupne indexy bodov vo voronoi diagrame. Voronoi obsahuje pole polygonov (int[]).
     * Kazdy z nich ma podla prislusneho indexu definovanu dlzku v vCount. Pocet
     * polygonov je rovnaky ako pocet ohnisk parametra vo volani calculateVoronoi.
     * Nenastavovat ziadnemu prvku hodnotu null.
     * Ked bude ze {2,1,4}, {1,2,0}, {1,2,5,6} - su tam 3 polygony a body su indexy z pola points - pre kazdy polygon focee su ohniska.
     */
    public final int[][] voronoi = new int[0x100][0x104];

    /**
     * Pocty vrcholov polygonov voronoi diagramu.
     */
    public final int[] vCount = new int[0x100];

    /**
     * Delaunay triangulacia - triangles su trojuholniky triangulacie. Rozmedzie
     * platnych trojuholnikov: (0, triangC - 1). Nenastavovat hodnotam null value.
     */
    private final Triangle[] triangles = new Triangle[0x300];

    /**
     * Vstupne pole ohnisk.
     */
    private v2[] ar;

    /**
     * Pocet trojuholnikov triangulacie.
     */
    private int triangC = 0;

    /**
     * Obojsmerny spojovy zoznam prvkov konvexneho obalu.
     */
    private Hull hull;

    private final Random rng = new XoRoShiRo128PlusRandom(1);

    private double RND() { return rng.nextDouble() + 0.5; }


    private final Edge[] edges = new Edge[0x10000]; 
    private final int[] boundaries = new int[0x200];
    private int boundariesCount = 0;
    private float maxX;
    private float minX;
    private float minY;
    private float maxY;
    private final double[] comparer = new double[0x302]; 
    private final boolean[] validCorners = new boolean[4]; 
    private int[] polygon; 

    /**
     * Inicializuje Factory. Samotny geometricky vypocet je potom osetreny
     * o pomalu alokaciu pamate.
     */
    public SingletonVD() {
        for (int i = 0; i < triangles.length; ++i) {
            triangles[i] = new Triangle(i); 
        }
        for (int i = 0; i < points.length; ++i) {
            points[i] = new v2();
        }
        for (int i = 0; i < 0x100; ++i) {
            for (int j = i + 1; j < 0x100; ++j) {
                edges[(i << 8) | j] = new Edge();
            }
        }
    }

    /**
     * Pocita voronoi diagram na zaklade vstupnych parametrov a vklada ich do
     * vyslednych prvkov.
     *
     * @param focee
     * @param a
     * @param b
     */
    public void calculateVoronoi(v2[] focee, v2 a, v2 b) {
        pCount = 0;

        maxX = Math.max(a.x, b.x);
        maxY = Math.max(a.y, b.y);
        minX = Math.min(a.x, b.x);
        minY = Math.min(a.y, b.y);

        calculateDelaunay(focee);
        int count = focee.length;

        if (count == 0) {
            return;
        }

        Arrays.fill(vCount, 0, count, 0);
        boundariesCount = 0;

        points[0].set(maxX, maxY);
        points[1].set(maxX, minY);
        points[2].set(minX, minY);
        points[3].set(minX, maxY);
        pCount = 4;

        if (count == 1) {
            int[] p = voronoi[0];
            p[0] = 0;
            p[1] = 1;
            p[2] = 2;
            p[3] = 3;
            vCount[0] = 4;
        } else {
            Arrays.fill(validCorners, true);

            for (int i = 0; i != triangC; ++i) {
                Triangle triangle = triangles[i];
                double x = triangle.dX;
                double y = triangle.dY;

                if (x <= maxX && x >= minX && y <= maxY && y >= minY) { 
                    int ti = triangle.i;
                    int tj = triangle.j;
                    int tk = triangle.k;

                    voronoi[ti][vCount[ti]++] = voronoi[tj][vCount[tj]++] = voronoi[tk][vCount[tk]++] = pCount;
                    points[pCount++].set(triangle.dX, triangle.dY);
                }
            }

            Hull left = hull;
            do {
                Hull right = left.prev;
                addBoundary(left.i, right.i, null); 
                left = right;
            } while (left != hull);

            for (int i = 0; i != boundariesCount; ++i) {
                int li = boundaries[(i == 0 ? boundariesCount : i) - 1];
                int ri = boundaries[i];
                v2 l = ar[li];
                v2 r = ar[ri];

                float sy = l.x > r.x ? minY : maxY;
                float y = sy; 
                float x = (float) x(l, r, y);

                if (x >= maxX || x <= minX) { 
                    x = l.y > r.y ? maxX : minX;
                    y = (float) y(l, r, x);

                    if (y == sy) { 
                        for (int j = 0; j != 4; ++j) {
                            v2 c = points[j];
                            if (c.x == x && c.y == y) {
                                validCorners[j] = false;
                                break;
                            }
                        }

                    }
                }

                voronoi[li][vCount[li]++] = voronoi[ri][vCount[ri]++] = pCount;
                points[pCount++].set(x, y);

            }

            for (int i = 0; i != 4; ++i) {
                if (validCorners[i]) {
                    v2 corner = points[i];

                    double x = corner.x;
                    double y = corner.y;

                    int min = boundaries[0];
                    double distMin = Double.MAX_VALUE;
                    for (int j = 0; j != boundariesCount; ++j) {
                        int point = boundaries[j];
                        double dist = distSq(x, y, ar[point]);
                        if (dist < distMin) {
                            distMin = dist;
                            min = point;
                        }
                    }

                    voronoi[min][vCount[min]++] = i;
                }
            }

            for (int i = 0; i < focee.length; ++i) {
                sort(i);
            }
        }
    }

    /**
     * Zotriedi vrcholy v polygone podla uhlu
     *
     * @param indexPolygon
     */
    private void sort(int indexPolygon) {
        int size = vCount[indexPolygon];
        if (size != 0) {
            polygon = voronoi[indexPolygon];
            v2 focus = ar[indexPolygon];
            for (int i = 0; i != size; ++i) {
                comparer[i] = angle(points[polygon[i]], focus);
            }
            quicksortPoly(0, size - 1);
        }
    }

    private void quicksortPoly(int low, int high) {
        int i = low, j = high;
        double pivot = comparer[low + ((high - low) >> 1)]; 
        while (i <= j) {
            while (comparer[i] < pivot) {
                i++;
            }
            while (comparer[j] > pivot) {
                j--;
            }
            if (i <= j) {
                double temp = comparer[i];
                comparer[i] = comparer[j];
                comparer[j] = temp;
                int v = polygon[i];
                polygon[i] = polygon[j];
                polygon[j] = v;

                i++;
                j--;
            }
        }
        if (low < j) {
            quicksortPoly(low, j);
        }
        if (i < high) {
            quicksortPoly(i, high);
        }
    }

    /**
     * @param low
     * @param high
     */
    private void quicksort(int low, int high) {
        int i = low, j = high;
        double pivot = comparer[low + ((high - low) >> 1)]; 
        while (i <= j) {
            while (comparer[i] < pivot) {
                i++;
            }
            while (comparer[j] > pivot) {
                j--;
            }
            if (i <= j) {
                double temp = comparer[i];
                comparer[i] = comparer[j];
                comparer[j] = temp;
                v2 v = ar[i];
                ar[i] = ar[j];
                ar[j] = v;

                i++;
                j--;
            }
        }
        if (low < j) {
            quicksort(low, j);
        }
        if (i < high) {
            quicksort(i, high);
        }
    }

    /**
     * Vypocet delaunay triangulacie
     *
     * @param ar Pole ohnisk
     */
    private void calculateDelaunay(v2[] ar) {
        this.ar = ar;
        triangC = 0;
        hull = null;

        int size = Math.min(this.ar.length, 0x100);

        if (size == 0) {
            return;
        }

        double SIN = Math.sin(RND());
        double COS = Math.cos(RND());
        for (int i = 0; i != size; ++i) {
            v2 v = this.ar[i];
            comparer[i] = SIN * v.x + COS * v.y;
        }
        quicksort(0, size - 1); 
        
        /* 
        Arrays.sort(f, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Vec2 v1 = (Vec2) o1;
                Vec2 v2 = (Vec2) o2;
                return v1.x > v2.x ? 1 : v1.x == v2.x ? v1.y > v2.y ? 1 : -1 : -1;
            }
        });
        */

        hull = new Hull(0);
        hull.prev = hull.next = hull;
        for (int v = 1; v != size; ++v) {
            Hull r = hull;
            Hull l = hull;

            while (site(this.ar[v], this.ar[r.i], this.ar[r.prev.i]) == 1) {
                r = r.prev;
            }
            while (site(this.ar[v], this.ar[l.i], this.ar[l.next.i]) == -1) {
                l = l.next;
            }
            Edge left = edges[Edge.index(l.i, v)];
            left.init();

            
            for (Hull k = l; k != r; k = k.prev) {
                int lp = k.i;
                int rp = k.prev.i;
                Edge right = edges[Edge.index(rp, v)];
                right.init();
                addTriangle(left, right, edges[Edge.index(lp, rp)], lp, rp, v);
                left = right;
            }
            if (r == l && hull.prev != hull) { 
                Hull duplicitny = new Hull(hull.i);
                Hull novy = new Hull(v, duplicitny, hull);
                duplicitny.next = hull.next;
                duplicitny.prev = novy;
                hull.next.prev = duplicitny;
                hull.next = novy;
                hull = novy;
            } else {
                hull = r.next = l.prev = new Hull(v, l, r);
            }
        }
    }

    /**
     * @param a
     * @param b
     * @param t
     */
    private void addBoundary(int a, int b, Triangle t) {
        Triangle opposite = edges[Edge.index(a, b)].get(t);
        v2 va = ar[a];
        v2 vb = ar[b];
        boolean bx = va.x > vb.x; 
        boolean by = va.y > vb.y; 
        if (opposite != null) {
            float x = (float) opposite.dX;
            float y = (float) opposite.dY;
            if (bx && y <= minY || !bx && y >= maxY || by && x >= maxX || !by && x <= minX) { 
                int center = opposite.get(a, b);
                addBoundary(a, center, opposite);
                addBoundary(center, b, opposite);
                return;
            }
            
        }
        boundaries[boundariesCount++] = b;
    }

    /**
     * @param leftEdge
     * @param rightEdge
     * @param centerEdge
     * @param l
     * @param r
     * @param v
     */
    private void addTriangle(
            Edge leftEdge,
            Edge rightEdge,
            Edge centerEdge,
            int l,
            int r,
            int v
    ) {
        Triangle triangle = centerEdge.get();
        if (triangle == null || !triangle.inside(ar[v])) {
            
            Triangle newTriangle = triangles[triangC++];
            newTriangle.init(l, r, v, ar, triangle);
            leftEdge.add(newTriangle);
            rightEdge.add(newTriangle);
            centerEdge.add(newTriangle);
        } else {
            int c = triangle.get(l, r);
            Edge center1 = edges[Edge.index(l, c)];
            Edge center2 = edges[Edge.index(r, c)];
            center1.remove(triangle);
            center2.remove(triangle);
            centerEdge.init();

            
            Edge newCenterEdge = edges[Edge.index(c, v)];
            newCenterEdge.init();

            
            
            int i = triangle.index;
            Triangle deleted = triangles[i];
            Triangle t = triangles[i] = triangles[--triangC];
            triangles[triangC] = deleted;
            deleted.index = triangC;
            t.index = i;

            
            addTriangle(leftEdge, newCenterEdge, center1, l, c, v); 
            addTriangle(newCenterEdge, rightEdge, center2, c, r, v); 
        }
    }

    /**
     * @param a 1. bod priamky
     * @param b 2. bod priamky
     * @param v Bod, u ktoreho sa rozhoduje, na ktorej strane priamky sa nachadza
     * @return -1 ak je v nalavo od priamky a -> b, 1 ak napravo, 0 ak na nej
     */
    private static int site(v2 a, v2 b, v2 v) {
        if (a == b) return 0;
        double ax = a.x;
        double ay = a.y;
        double bx = b.x;
        double by = b.y;
        double vx = v.x;
        double vy = v.y;
        double g = (bx - ax) * (vy - by);
        double h = (vx - bx) * (by - ay);
        //noinspection UseCompareMethod
        return g > h ? 1 : g == h ? 0 : -1;

    }

    /**
     * @param a
     * @param b
     * @return Vrati kvadraticku hodnotu uhlu zvierajucom usecka (a, b) v intervale od 0-4
     */
    private static double angle(v2 a, v2 b) {
        double vx = b.x - a.x;
        double vy = b.y - a.y;
        double x = vx * vx;
        double cos = x / (x + vy * vy); 
        return vx > 0 ? vy > 0 ? 3 + cos : 1 - cos : vy > 0 ? 3 - cos : 1 + cos;
    }

    /**
     * @param a
     * @param b
     * @param y
     * @return Vrati x suradnicu bodu, ktory je rovnako vzdialeny od a, b s y-sradnicou.
     */
    private static double x(v2 a, v2 b, double y) {
        double cx = a.x - b.x;
        double cy = a.y - b.y;
        return ((a.x + b.x) * cx + (a.y + b.y) * cy - 2 * y * cy) / (2 * cx);
    }

    /**
     * @param a
     * @param b
     * @param x
     * @return Vrati y suradnicu bodu, ktory je rovnako vzdialeny od a, b s x-sradnicou.
     */
    private static double y(v2 a, v2 b, double x) {
        double cx = a.x - b.x;
        double cy = a.y - b.y;
        return ((a.x + b.x) * cx + (a.y + b.y) * cy - 2 * x * cx) / (2 * cy);
    }

    /**
     * @param a
     * @param b
     * @return Vrati vzdialenost ^ 2 bodov
     */
    private static double distSq(double x, double y, v2 b) {
        x -= b.x;
        y -= b.y;
        return x * x + y * y;
    }
}
