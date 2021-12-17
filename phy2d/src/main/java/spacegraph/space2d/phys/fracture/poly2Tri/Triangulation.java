package spacegraph.space2d.phys.fracture.poly2Tri;

import jcog.math.v2;

import java.util.ArrayList;

public enum Triangulation {
	;
//    /**
//     * Set this to TRUE to obtain file with name debugFileName with the
//     * log about triangulation
//     */
//    private static final boolean debug = false;

    /**
     * If debug == true file with this name will be created during triangulation.
     */
    public static final String debugFileName = "polygon_triangulation_log.txt";

    /**
     * Vola funkciu trangulate na polygone bez dier. Vracia rovnaku hodnotu
     * ako funkcia triangulate(int, int[], double[][])
     *
     * @param vertices
     * @param count
     * @return
     */
    public static ArrayList<int[]> triangulate(v2[] vertices, int count) {
        return triangulate(1, new int[]{count}, vertices);
    }

    /**
     * numContures == number of contures of polygon (1 OUTER + n INNER)
     * numVerticesInContures == array numVerticesInContures[x] == number of vertices in x. contures, 0-based
     * vertices == array of vertices, each item of array contains doubl[2] ~ {x,y}
     * First conture is OUTER -> vertices must be COUNTER CLOCKWISE!
     * Other contures must be INNER -> vertices must be CLOCKWISE!
     * Example:
     * numContures = 1 (1 OUTER CONTURE, 1 INNER CONTURE)
     * numVerticesInContures = { 3, 3 } 
     * vertices = { {0, 0}, {7, 0}, {4, 4}, 
     * {2, 2}, {2, 3}, {3, 3}  
     * }
     * <p>
     * If error occurs during triangulation, null is returned.
     *
     * @param numContures           number of contures of polygon (1 OUTER + n INNER)
     * @param numVerticesInContures array numVerticesInContures[x] == number of vertices in x. contures, 0-based
     * @param vertices              array of vertices, each item of array contains doubl[2] ~ {x,y}
     * @return ArrayList of ArrayLists which are triangles in form of indexes into array vertices
     */
    private static ArrayList<int[]> triangulate(int numContures, int[] numVerticesInContures, v2[] vertices) {
        Polygon p = new Polygon(numContures, numVerticesInContures, vertices);
//        if (debug) {
//
//            p.setDebugOption(debug);
//        } else {
//            p.setDebugOption(false);
//        }
        return p.triangulation() ? p.triangles() : null;
    }
}
