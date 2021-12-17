package spacegraph.space2d.phys.fracture;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.AABB;
import spacegraph.space2d.phys.common.MathUtils;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.fracture.hertelmehlhorn.SingletonHM;
import spacegraph.space2d.phys.fracture.poly2Tri.Triangulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * Polygon - je reprezentovany postupnostou vrcholov
 *
 * @author Marek Benovic
 */
public class Polygon implements Iterable<v2>, Cloneable {
    private static final float AABBConst = 1;
    private static final SingletonHM HM = new SingletonHM();

    /**
     * Pole vrcholov.
     */
    v2[] vertices;

    /**
     * Pocet vrcholov.
     */
    int vertexCount;

    /**
     * Vytvori prazdny polygon bez vrcholov. Polygon moze byt konvexny,
     * konkavny aj nonsimple.
     */
    public Polygon() {
        this(8);
    }
    public Polygon(int capacity) {
        vertices = new v2[capacity];
        vertexCount = 0;
    }

    /**
     * Vytvori polygon z danych vrcholov. Vlozi dane body do polygonu. Pole
     * z parametra sa preda referenciou (nedochadza ku klonovaniu).
     *
     * @param va Vstupne vrcholy.
     */
    Polygon(v2[] va) {
        vertices = va;
        vertexCount = va.length;
    }

    /**
     * Vytvori polygon z danych vrcholov. Vlozi dane body do polygonu. Pole
     * z parametra sa preda referenciou (nedochadza ku klonovaniu).
     *
     * @param va Vstupne vrcholy.
     * @param n  Pocet aktivnych vrcholov
     */
    private Polygon(v2[] va, int n) {
        vertices = va;
        vertexCount = n;
    }

    /**
     * Vlozi do Polygonu prvky z kolekcie.
     *
     * @param c Kolekcia s vrcholmi.
     */
    public void add(Iterable<? extends v2> c) {
        for (v2 v : c) {
            add(v);
        }
    }

    /**
     * Prida vrchol do polygonu
     *
     * @param v Pridavany vrchol
     */
    public void add(v2 v) {
        if (vertices.length == vertexCount) {
            v2[] newArray = new v2[vertexCount * 2];
            System.arraycopy(vertices, 0, newArray, 0, vertexCount);
            vertices = newArray;
        }
        vertices[vertexCount++] = v;
    }

    /**
     * @param index
     * @return Vrati prvok na danom mieste
     */
    public v2 get(int index) {
        return vertices[index];
    }

    /**
     * @return Vrati pocet prvkov
     */
    public int size() {
        return vertexCount;
    }

    /**
     * @param index Index bodu
     * @return Vrati vrchol podla poradia s osetrenim pretecenia.
     */
    public v2 cycleGet(int index) {
        return get(index % vertexCount);
    }

    /**
     * @return Vrati v poli vrcholy polygonu - vrati referenciu na interne pole,
     * preto pri iterovani treba brat pocet cez funkciu size a nie
     * cez array.length.
     */
    public v2[] vertices() {
        return vertices;
    }

    /**
     * Existuje efektivnejsia implementacia v pripade, ze bodov je viacej.
     * http:
     * Este upravena by bola vziat vsetky hrany
     *
     * @param p
     * @return Vrati true.
     */
    public boolean inside(v2 p) {
        int i, j;
        boolean c = false;
        v2 v = new v2();
        for (i = 0, j = vertexCount - 1; i < vertexCount; j = i++) {
            v2 a = get(i);
            v2 b = get(j);
            v.set(b);
            v.subbed(a);
            if (((a.y >= p.y) != (b.y >= p.y)) && (p.x <= v.x * (p.y - a.y) / v.y + a.x)) {
                c = !c;
            }
        }
        return c;
    }

    /**
     * @return Vrati hmotnost telesa.
     */
    private double mass() {
        double m = 0;
        for (int i = 0, j = 1; i != vertexCount; i = j, j++) {
            v2 b1 = get(i);
            v2 b2 = get(j == vertexCount ? 0 : j);
            m += v2.cross(b1, b2);
        }
        m = Math.abs(m / 2);
        return m;
    }

    /**
     * @return Vrati tazisko polygonu.
     */
    public v2 centroid() {
        v2 C = new v2();
        double m = 0;
        v2 g = new v2();
        for (int i = 0, j = 1; i != vertexCount; i = j, j++) {
            v2 b1 = get(i);
            v2 b2 = get(j == vertexCount ? 0 : j);
            float s = v2.cross(b1, b2);
            m += s;
            g.set(b1);
            g.added(b2);
            g.scaled(s);
            C.added(g);
        }
        C.scaled((float) (1 / (3 * m)));
        return C;
    }

    /**
     * @return Vrati najvacsiu vzdialenost 2 bodov.
     */
    private double radius() {
        double ln = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < vertexCount; ++i) {
            ln = Math.max(get(i).distanceSq(cycleGet(i + 1)), ln);
        }
        return Math.sqrt(ln);
    }

    /**
     * @return Ak je polygon priliz maly, alebo tenky (nieje dobre ho zobrazovat), vrati false.
     */
    public boolean isCorrect() {
        double r = radius();
        double mass = mass();
        return (r > Material.MINFRAGMENTSIZE && mass > Material.MINFRAGMENTSIZE && mass / r > Material.MINFRAGMENTSIZE);
    }

    /**
     * @return Vrati AABB pre Polygon sluziaci na rozsah generovanych ohnisk pre
     * fraktury. Preto je to umelo nafunknute o konstantu 1.
     */
    public AABB getAABB() {

        if (vertexCount == 0) {
            return null;
        } else {
            float minX = Float.POSITIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxX = Float.NEGATIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            for (int i = 0; i < vertexCount; ++i) {
                v2 v = get(i);
                minX = Math.min(v.x, minX);
                maxX = Math.max(v.x, maxX);
                minY = Math.min(v.y, minY);
                maxY = Math.max(v.y, maxY);
            }
            return new AABB(
                    new v2(minX - AABBConst, minY - AABBConst),
                    new v2(maxX + AABBConst, maxY + AABBConst), false);
        }
    }

    /**
     * @return Vezme dany polygon a urobi konvexnu dekompoziciu - rozpad na konvexne polygony
     * s referencnou zavislostou (spolocne vrcholy polygonov su rovnake instancie).
     */
    public Polygon[] convexDecomposition() {
        if (isSystemPolygon()) { 
            return new Polygon[]{this};
        }


        int bound = vertexCount;
        v2[] reverseArray = IntStream.range(0, bound).mapToObj(i1 -> get(vertexCount - i1 - 1)).toArray(v2[]::new);

        ArrayList<int[]> triangles = Triangulation.triangulate(reverseArray, vertexCount);

        int c = triangles.size();

        int[][] list = new int[c][3];
        for (int i = 0; i < c; i++) {
            int[] t = triangles.get(i);
            list[i][0] = t[0];
            list[i][1] = t[1];
            list[i][2] = t[2];
        }

        HM.calculate(list, reverseArray, Settings.maxPolygonVertices);
        return HM.dekomposition;
    }

    /**
     * Otoci poradie prvkov v poli.
     */
    public void flip() {
        int size = size();
        int n = size() >> 1;
        for (int i = 0; i < n; i++) {
            v2 temp = vertices[i];
            int j = size - 1 - i;
            vertices[i] = vertices[j];
            vertices[j] = temp;
        }
    }

    /**
     * @return Vrati true, pokial je polygon konvexny a pocet vrcholov je maxPolygonVertices
     */
    private boolean isSystemPolygon() {
        return isConvex() && vertexCount <= Settings.maxPolygonVertices;
    }

    /**
     * @return Vrati true, pokial je polygon konvexny.
     */
    private boolean isConvex() {
        for (int i = 0; i < vertexCount; i++) {
            v2 a = get(i);
            v2 b = cycleGet(i + 1);
            v2 c = cycleGet(i + 2);
            if (MathUtils.siteDef(a, b, c) == 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Vrati true, pokial je postupnost vrcholov v smere hodinovych ruciciek
     */
    public boolean isClockwise() {
        double signedArea = 0;
        for (int i = 0; i < size(); ++i) {
            v2 v1 = get(i);
            v2 v2 = cycleGet(i + 1);
            double v1x = v1.x;
            double v1y = v1.y;
            double v2x = v2.x;
            double v2y = v2.y;
            signedArea += v1x * v2y - v2x * v1y;
        }
        return signedArea < 0;
    }


    /**
     * @return Vrati novy polygon. Pole je realokovane, ale referencie na
     * body (Tuple2f) su povodne.
     */
    @Override
    public Polygon clone() {
        v2[] newArray = new v2[vertexCount];
        int newCount = vertexCount;
        System.arraycopy(vertices, 0, newArray, 0, vertexCount);
        return new Polygon(newArray, newCount);
    }

    /**
     * @return Vrati iterator na vrcholy polygonu
     */
    @Override
    public Iterator<v2> iterator() {
        return new MyIterator();
    }

    private class MyIterator implements Iterator<v2> {
        private int index;

        MyIterator() {
            index = 0;
        }

        MyIterator(int index) {
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < vertexCount;
        }

        @Override
        public v2 next() {
            return get(index++);
        }
    }
}