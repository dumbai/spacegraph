/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

// Dbvt implementation by Nathanael Presson

package com.bulletphysics.collision.broadphase;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.linearmath.MiscUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.IntArrayList;
import com.bulletphysics.util.ObjectArrayList;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.bulletphysics.collision.broadphase.DbvtAabbMm.proximity;

/**
 * @author jezek2
 */
public class Dbvt<X> {

    private static final int SIMPLE_STACKSIZE = 64;
    private static final int DOUBLE_STACKSIZE = SIMPLE_STACKSIZE * 2;
    private static final Vector3f[] axis = {new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1)};
    private final int lkhd = -1;
    public Node<X> root;
    public final AtomicInteger leaves = new AtomicInteger();
    private Node<X> free;
    private/*unsigned*/ int opath = 0;

    public Dbvt() {
    }

    public static void write(IWriter iwriter) {
        throw new UnsupportedOperationException();
    }

    public static void clone(Dbvt dest) {
        clone(dest, null);
    }

    private static void clone(Dbvt dest, IClone iclone) {
        throw new UnsupportedOperationException();
    }

    private static int countLeaves(Node node) {
        if (node.isinternal()) {
            return countLeaves(node.childs[0]) + countLeaves(node.childs[1]);
        } else {
            return 1;
        }
    }

    private static void extractLeaves(Node node, ObjectArrayList<Node> leaves) {
        if (node.isinternal()) {
            extractLeaves(node.childs[0], leaves);
            extractLeaves(node.childs[1], leaves);
        } else {
            leaves.add(node);
        }
    }

    private static void enumNodes(Node root, ICollide policy) {
        //DBVT_CHECKTYPE
        policy.accept(root);
        if (root.isinternal()) {
            enumNodes(root.childs[0], policy);
            enumNodes(root.childs[1], policy);
        }
    }

    private static void visit(Node root, ICollide policy) {
        //DBVT_CHECKTYPE
        if (root.isinternal()) {
            visit(root.childs[0], policy);
            visit(root.childs[1], policy);
        } else {
            policy.accept(root);
        }
    }

    public static void collideTT(Node root0, Node root1, ICollide policy) {
        //DBVT_CHECKTYPE
        if (root0 != null && root1 != null) {
            ObjectArrayList<sStkNN> stack = new ObjectArrayList<>(DOUBLE_STACKSIZE);
            stack.add(new sStkNN(root0, root1));
            do {
                sStkNN p = stack.remove(stack.size() - 1);
                if (p.a == p.b) {
                    if (p.a.isinternal()) {
                        stack.add(new sStkNN(p.a.childs[0], p.a.childs[0]));
                        stack.add(new sStkNN(p.a.childs[1], p.a.childs[1]));
                        stack.add(new sStkNN(p.a.childs[0], p.a.childs[1]));
                    }
                } else if (DbvtAabbMm.intersects(p.a.volume, p.b.volume)) {
                    if (p.a.isinternal()) {
                        if (p.b.isinternal()) {
                            stack.add(new sStkNN(p.a.childs[0], p.b.childs[0]));
                            stack.add(new sStkNN(p.a.childs[1], p.b.childs[0]));
                            stack.add(new sStkNN(p.a.childs[0], p.b.childs[1]));
                            stack.add(new sStkNN(p.a.childs[1], p.b.childs[1]));
                        } else {
                            stack.add(new sStkNN(p.a.childs[0], p.b));
                            stack.add(new sStkNN(p.a.childs[1], p.b));
                        }
                    } else {
                        if (p.b.isinternal()) {
                            stack.add(new sStkNN(p.a, p.b.childs[0]));
                            stack.add(new sStkNN(p.a, p.b.childs[1]));
                        } else {
                            policy.accept(p.a, p.b);
                        }
                    }
                }
            }
            while (!stack.isEmpty());
        }
    }

    private static void collideTT(Node root0, Node root1, Transform xform, ICollide policy) {
        //DBVT_CHECKTYPE
        if (root0 != null && root1 != null) {
            ObjectArrayList<sStkNN> stack = new ObjectArrayList<>(DOUBLE_STACKSIZE);
            stack.add(new sStkNN(root0, root1));
            do {
                sStkNN p = stack.remove(stack.size() - 1);
                if (p.a == p.b) {
                    if (p.a.isinternal()) {
                        stack.add(new sStkNN(p.a.childs[0], p.a.childs[0]));
                        stack.add(new sStkNN(p.a.childs[1], p.a.childs[1]));
                        stack.add(new sStkNN(p.a.childs[0], p.a.childs[1]));
                    }
                } else if (DbvtAabbMm.intersects(p.a.volume, p.b.volume, xform)) {
                    if (p.a.isinternal()) {
                        if (p.b.isinternal()) {
                            stack.add(new sStkNN(p.a.childs[0], p.b.childs[0]));
                            stack.add(new sStkNN(p.a.childs[1], p.b.childs[0]));
                            stack.add(new sStkNN(p.a.childs[0], p.b.childs[1]));
                            stack.add(new sStkNN(p.a.childs[1], p.b.childs[1]));
                        } else {
                            stack.add(new sStkNN(p.a.childs[0], p.b));
                            stack.add(new sStkNN(p.a.childs[1], p.b));
                        }
                    } else {
                        if (p.b.isinternal()) {
                            stack.add(new sStkNN(p.a, p.b.childs[0]));
                            stack.add(new sStkNN(p.a, p.b.childs[1]));
                        } else {
                            policy.accept(p.a, p.b);
                        }
                    }
                }
            }
            while (!stack.isEmpty());
        }
    }

    public static void collideTT(Node root0, Transform xform0, Node root1, Transform xform1, ICollide policy) {
        Transform xform = new Transform();
        xform.inverse(xform0);
        xform.mul(xform1);
        collideTT(root0, root1, xform, policy);
    }


    public static <X> void collide(Node<X> root, DbvtAabbMm volume, ICollide<X> policy) {
        //DBVT_CHECKTYPE
        if (root != null) {
            ObjectArrayList<Node> stack = new ObjectArrayList<>(SIMPLE_STACKSIZE);
            stack.add(root);
            do {
                Node<X> n = stack.pop();
                if (volume.intersects(n.volume)) {
                    if (n.isinternal()) {
                        stack.add(n.childs[0]);
                        stack.add(n.childs[1]);
                    } else {
                        policy.accept(n);
                    }
                }
            } while (!stack.isEmpty());
        }
    }

    public static void collideRAY(Node root, Vector3f origin, Vector3f direction, ICollide policy) {
        //DBVT_CHECKTYPE
        if (root != null) {
            Vector3f normal = new Vector3f();
            normal.normalize(direction);
            Vector3f invdir = new Vector3f();
            invdir.set(1.0f / normal.x, 1.0f / normal.y, 1.0f / normal.z);
            int[] signs = {direction.x < 0 ? 1 : 0, direction.y < 0 ? 1 : 0, direction.z < 0 ? 1 : 0};
            ObjectArrayList<Node> stack = new ObjectArrayList<>(SIMPLE_STACKSIZE);
            stack.add(root);
            do {
                Node node = stack.remove(stack.size() - 1);
                if (DbvtAabbMm.intersects(node.volume, origin, invdir, signs)) {
                    if (node.isinternal()) {
                        stack.add(node.childs[0]);
                        stack.add(node.childs[1]);
                    } else {
                        policy.accept(node);
                    }
                }
            }
            while (!stack.isEmpty());
        }
    }

    public static void collideKDOP(Node root, Vector3f[] normals, float[] offsets, int count, ICollide policy) {
        //DBVT_CHECKTYPE
        if (root != null) {
            int inside = (1 << count) - 1;
            ObjectArrayList<sStkNP> stack = new ObjectArrayList<>(SIMPLE_STACKSIZE);
            int[] signs = new int[4 * 8];
            assert (count < (/*sizeof(signs)*/128 / /*sizeof(signs[0])*/ 4));
            for (int i = 0; i < count; ++i) {
                signs[i] = ((normals[i].x >= 0) ? 1 : 0) +
                        ((normals[i].y >= 0) ? 2 : 0) +
                        ((normals[i].z >= 0) ? 4 : 0);
            }
            stack.add(new sStkNP(root, 0));
            do {
                sStkNP se = stack.remove(stack.size() - 1);
                Node seNode = se.node;

                boolean out = false;
                for (int i = 0, j = 1; (!out) && (i < count); ++i, j <<= 1) {
                    if (0 == (se.mask & j)) {
                        int side = seNode.volume.Classify(normals[i], offsets[i], signs[i]);
                        switch (side) {
                            case -1 -> out = true;
                            case +1 -> se.mask |= j;
                        }
                    }
                }
                if (!out) {
                    if ((se.mask != inside) && (seNode.isinternal())) {
                        stack.add(new sStkNP(seNode.childs[0], se.mask));
                        stack.add(new sStkNP(seNode.childs[1], se.mask));
                    } else {
                        if (policy.allLeaves(seNode))
                            visit(seNode, policy);
                    }
                }
            }
            while (!stack.isEmpty());
        }
    }

    public static void collideOCL(Node root, Vector3f[] normals, float[] offsets, Vector3f sortaxis, int count, ICollide policy) {
        collideOCL(root, normals, offsets, sortaxis, count, policy, true);
    }

    private static void collideOCL(Node root, Vector3f[] normals, float[] offsets, Vector3f sortaxis, int count, ICollide policy, boolean fullsort) {
        //DBVT_CHECKTYPE
        if (root != null) {
            int srtsgns = (sortaxis.x >= 0 ? 1 : 0) +
                    (sortaxis.y >= 0 ? 2 : 0) +
                    (sortaxis.z >= 0 ? 4 : 0);
            int inside = (1 << count) - 1;
            ObjectArrayList<sStkNPS> stock = new ObjectArrayList<>();
            IntArrayList ifree = new IntArrayList();
            IntArrayList stack = new IntArrayList();
            int[] signs = new int[/*sizeof(unsigned)*8*/4 * 8];
            assert (count < (/*sizeof(signs)*/128 / /*sizeof(signs[0])*/ 4));
            for (int i = 0; i < count; i++) {
                signs[i] = ((normals[i].x >= 0) ? 1 : 0) +
                        ((normals[i].y >= 0) ? 2 : 0) +
                        ((normals[i].z >= 0) ? 4 : 0);
            }
            //stock.reserve(SIMPLE_STACKSIZE);
            //stack.reserve(SIMPLE_STACKSIZE);
            //ifree.reserve(SIMPLE_STACKSIZE);
            stack.add(allocate(ifree, stock, new sStkNPS(root, 0, root.volume.ProjectMinimum(sortaxis, srtsgns))));
            do {
                // JAVA NOTE: check
                int id = stack.remove(stack.size() - 1);
                sStkNPS se = stock.get(id);
                ifree.add(id);
                if (se.mask != inside) {
                    boolean out = false;
                    for (int i = 0, j = 1; (!out) && (i < count); ++i, j <<= 1) {
                        if (0 == (se.mask & j)) {
                            int side = se.node.volume.Classify(normals[i], offsets[i], signs[i]);
                            switch (side) {
                                case -1 -> out = true;
                                case +1 -> se.mask |= j;
                            }
                        }
                    }
                    if (out) {
                        continue;
                    }
                }
                if (policy.descend(se.node)) {
                    if (se.node.isinternal()) {
                        Node[] pns = {se.node.childs[0], se.node.childs[1]};
                        sStkNPS[] nes = {new sStkNPS(pns[0], se.mask, pns[0].volume.ProjectMinimum(sortaxis, srtsgns)),
                                new sStkNPS(pns[1], se.mask, pns[1].volume.ProjectMinimum(sortaxis, srtsgns))
                        };
                        int q = nes[0].value < nes[1].value ? 1 : 0;
                        int j = stack.size();
                        if (fullsort && (j > 0)) {
                            /* Insert 0	*/
                            j = nearest(stack, stock, nes[q].value, 0, stack.size());
                            stack.add(0);
                            //#if DBVT_USE_MEMMOVE
                            //memmove(&stack[j+1],&stack[j],sizeof(int)*(stack.size()-j-1));
                            //#else
                            for (int k = stack.size() - 1; k > j; --k) {
                                stack.set(k, stack.get(k - 1));
                                //#endif
                            }
                            stack.set(j, allocate(ifree, stock, nes[q]));
                            /* Insert 1	*/
                            j = nearest(stack, stock, nes[1 - q].value, j, stack.size());
                            stack.add(0);
                            //#if DBVT_USE_MEMMOVE
                            //memmove(&stack[j+1],&stack[j],sizeof(int)*(stack.size()-j-1));
                            //#else
                            for (int k = stack.size() - 1; k > j; --k) {
                                stack.set(k, stack.get(k - 1));
                                //#endif
                            }
                            stack.set(j, allocate(ifree, stock, nes[1 - q]));
                        } else {
                            stack.add(allocate(ifree, stock, nes[q]));
                            stack.add(allocate(ifree, stock, nes[1 - q]));
                        }
                    } else {
                        policy.accept(se.node, se.value);
                    }
                }
            }
            while (stack.size() != 0);
        }
    }

    public static <X> void collideTU(Node<X> root, ICollide<X> policy) {
        //DBVT_CHECKTYPE
        if (root != null) {
            ObjectArrayList<Node<X>> stack = new ObjectArrayList<>(SIMPLE_STACKSIZE);
            stack.add(root);
            do {
                Node<X> n = stack.remove(stack.size() - 1);
                if (policy.descend(n)) {
                    if (n.isinternal()) {
                        stack.add(n.childs[0]);
                        stack.add(n.childs[1]);
                    } else {
                        policy.accept(n);
                    }
                }
            }
            while (!stack.isEmpty());
        }
    }

    private static int nearest(IntArrayList i, List<sStkNPS> a, float v, int l, int h) {
        int m;
        while (l < h) {
            m = (l + h) >> 1;
            if (a.get(i.get(m)).value >= v) {
                l = m + 1;
            } else {
                h = m;
            }
        }
        return h;
    }

    private static int allocate(IntArrayList ifree, ObjectArrayList<sStkNPS> stock, sStkNPS value) {
        int i;
        if (ifree.size() > 0) {
            i = ifree.get(ifree.size() - 1);
            ifree.remove(ifree.size() - 1);
            stock.get(i).set(value);
        } else {
            i = stock.size();
            stock.add(value);
        }
        return (i);
    }

    private static int indexOf(Node node) {
        return (node.parent.childs[1] == node) ? 1 : 0;
    }

    private static DbvtAabbMm merge(DbvtAabbMm a, DbvtAabbMm b, DbvtAabbMm out) {
        DbvtAabbMm.merge(a, b, out);
        return out;
    }

    // volume+edge lengths
    private static float size(DbvtAabbMm a) {
        Vector3f edges = a.lengths(new Vector3f());
        return (edges.x * edges.y * edges.z +
                edges.x + edges.y + edges.z);
    }

    private static void deletenode(Dbvt pdbvt, Node node) {
        //btAlignedFree(pdbvt->m_free);
        pdbvt.free = node;
    }

    private static void recursedeletenode(Dbvt pdbvt, Node node) {
        if (!node.isleaf()) {
            recursedeletenode(pdbvt, node.childs[0]);
            recursedeletenode(pdbvt, node.childs[1]);
        }
        if (node == pdbvt.root) {
            pdbvt.root = null;
        }
        deletenode(pdbvt, node);
    }

    private static <X> Node<X> node(Dbvt<X> pdbvt, Node<X> parent, DbvtAabbMm volume, X data) {
        Node<X> node;
        if (pdbvt.free != null) {
            node = pdbvt.free;
            pdbvt.free = null;
        } else {
            node = new Node<>();
        }
        node.parent = parent;
        node.volume.set(volume);
        node.data = data;
        node.childs[1] = null;
        return node;
    }

    private static void insertleaf(Dbvt pdbvt, Node root, Node leaf) {
        if (pdbvt.root == null) {
            pdbvt.root = leaf;
            leaf.parent = null;
        } else {

            DbvtAabbMm leafVolume = leaf.volume;

            while (!root.isleaf()) {
                Node[] rc = root.childs;
                root = rc[proximity(rc[0].volume, leafVolume) <
                        proximity(rc[1].volume, leafVolume) ? 0 : 1];
            }

            Node prev = root.parent;
            Node node = node(pdbvt, prev, merge(leafVolume, root.volume, new DbvtAabbMm()), null);
            if (prev != null) {
                prev.childs[indexOf(root)] = node;
                node.childs[0] = root;
                root.parent = node;
                node.childs[1] = leaf;
                leaf.parent = node;
                do {
                    if (prev.volume.contains(node.volume))
                        break;

                    DbvtAabbMm.merge(prev.childs[0].volume, prev.childs[1].volume, prev.volume);
                    node = prev;
                } while (null != (prev = node.parent));
            } else {
                node.childs[0] = root;
                root.parent = node;
                node.childs[1] = leaf;
                leaf.parent = node;
                pdbvt.root = node;
            }
        }
    }

    private static Node removeleaf(Dbvt pdbvt, Node leaf) {
        if (leaf == pdbvt.root) {
            pdbvt.root = null;
            return null;
        } else {
            Node parent = leaf.parent;
            Node prev = parent.parent;
            Node sibling = parent.childs[1 - indexOf(leaf)];
            if (prev != null) {
                prev.childs[indexOf(parent)] = sibling;
                sibling.parent = prev;
                deletenode(pdbvt, parent);
                while (prev != null) {
                    DbvtAabbMm pb = prev.volume;
                    DbvtAabbMm.merge(prev.childs[0].volume, prev.childs[1].volume, prev.volume);
                    if (!DbvtAabbMm.notEqual(pb, prev.volume))
                        break;

                    prev = prev.parent;
                }
                return (prev != null ? prev : pdbvt.root);
            } else {
                pdbvt.root = sibling;
                sibling.parent = null;
                deletenode(pdbvt, parent);
                return pdbvt.root;
            }
        }
    }

    private static void fetchleaves(Dbvt pdbvt, Node root, ObjectArrayList<Node> leaves) {
        fetchleaves(pdbvt, root, leaves, -1);
    }

    private static void fetchleaves(Dbvt pdbvt, Node root, ObjectArrayList<Node> leaves, int depth) {
        if (root.isinternal() && depth != 0) {
            fetchleaves(pdbvt, root.childs[0], leaves, depth - 1);
            fetchleaves(pdbvt, root.childs[1], leaves, depth - 1);
            deletenode(pdbvt, root);
        } else {
            leaves.add(root);
        }
    }

    private static void split(List<Node> leaves, ObjectArrayList<Node> left, ObjectArrayList<Node> right, Vector3f org, Vector3f axis) {
        Vector3f tmp = new Vector3f();
        MiscUtil.resize(left, 0, Node.class);
        MiscUtil.resize(right, 0, Node.class);
        for (int i = 0, ni = leaves.size(); i < ni; i++) {
            leaves.get(i).volume.center(tmp);
            tmp.sub(org);
            if (axis.dot(tmp) < 0.0f) {
                left.add(leaves.get(i));
            } else {
                right.add(leaves.get(i));
            }
        }
    }

    private static DbvtAabbMm bounds(List<Node> leaves) {
        DbvtAabbMm volume = new DbvtAabbMm(leaves.get(0).volume);
        for (int i = 1, ni = leaves.size(); i < ni; i++) {
            merge(volume, leaves.get(i).volume, volume);
        }
        return volume;
    }

    private static void bottomup(Dbvt pdbvt, ObjectArrayList<Node> leaves) {
        DbvtAabbMm tmpVolume = new DbvtAabbMm();
        int s;
        while ((s = leaves.size()) > 1) {
            float minsize = BulletGlobals.SIMD_INFINITY;
            int[] minidx = {-1, -1};
            for (int i = 0; i < s; i++) {
                for (int j = i + 1; j < s; j++) {
                    float sz = size(merge(leaves.get(i).volume, leaves.get(j).volume, tmpVolume));
                    if (sz < minsize) {
                        minsize = sz;
                        minidx[0] = i;
                        minidx[1] = j;
                    }
                }
            }
            Node a = leaves.get(minidx[0]);
            Node b = leaves.get(minidx[1]);

            Node p = node(pdbvt, null, merge(a.volume, b.volume, new DbvtAabbMm()), null);
            p.childs[0] = a;
            p.childs[1] = b;
            a.parent = p;
            b.parent = p;
            // JAVA NOTE: check
            leaves.setQuick(minidx[0], p);
            Collections.swap(leaves, minidx[1], s - 1);
            leaves.removeQuick(s - 1);
        }
    }

    private static Node topdown(Dbvt pdbvt, ObjectArrayList<Node> leaves, int bu_treshold) {
        if (leaves.size() > 1) {
            if (leaves.size() > bu_treshold) {
                DbvtAabbMm vol = bounds(leaves);
                Vector3f org = vol.center(new Vector3f());
                ObjectArrayList[] sets = new ObjectArrayList[2];
                for (int i = 0; i < sets.length; i++) {
                    sets[i] = new ObjectArrayList();
                }
                int bestaxis = -1;
                int bestmidp = leaves.size();
                /*3*/
                /*2*/
                int[][] splitcount = {{0, 0}, {0, 0}, {0, 0}};

                Vector3f x = new Vector3f();

                for (int i = 0; i < leaves.size(); i++) {
                    leaves.get(i).volume.center(x);
                    x.sub(org);
                    for (int j = 0; j < 3; j++) {
                        splitcount[j][x.dot(axis[j]) > 0.0f ? 1 : 0]++;
                    }
                }
                for (int i = 0; i < 3; i++) {
                    if ((splitcount[i][0] > 0) && (splitcount[i][1] > 0)) {
                        int midp = Math.abs(splitcount[i][0] - splitcount[i][1]);
                        if (midp < bestmidp) {
                            bestaxis = i;
                            bestmidp = midp;
                        }
                    }
                }
                if (bestaxis >= 0) {
                    //sets[0].reserve(splitcount[bestaxis][0]);
                    //sets[1].reserve(splitcount[bestaxis][1]);
                    split(leaves, sets[0], sets[1], org, axis[bestaxis]);
                } else {
                    //sets[0].reserve(leaves.size()/2+1);
                    //sets[1].reserve(leaves.size()/2);
                    for (int i = 0, ni = leaves.size(); i < ni; i++)
                        sets[i & 1].add(leaves.get(i));
                }
                Node node = node(pdbvt, null, vol, null);
                node.childs[0] = topdown(pdbvt, sets[0], bu_treshold);
                node.childs[1] = topdown(pdbvt, sets[1], bu_treshold);
                node.childs[0].parent = node;
                node.childs[1].parent = node;
                return node;
            } else {
                bottomup(pdbvt, leaves);
                return leaves.get(0);
            }
        }
        return leaves.get(0);
    }

    ////////////////////////////////////////////////////////////////////////////

    private static Node sort(Node n, Node[] r) {
        Node p = n.parent;
        if (p!=null) {
            Node[] pp = p.childs;
            Node q = p.parent;
            DbvtAabbMm pVolume = p.volume;

            assert (n.isinternal());
            // JAVA TODO: fix this
            if (p.hashCode() > n.hashCode()) {
                int i = indexOf(n);
                int j = 1 - i;
                Node s = pp[j];
                assert (n == pp[i]);
                if (q != null) {
                    q.childs[indexOf(p)] = n;
                } else {
                    r[0] = n;
                }
                s.parent = n;
                p.parent = n;
                n.parent = q;
                Node[] nn = n.childs;
                pp[0] = nn[0];
                pp[1] = nn[1];
                nn[0].parent = p;
                nn[1].parent = p;
                nn[i] = p;
                nn[j] = s;

                DbvtAabbMm.swap(pVolume, n.volume);
                return p;
            }
        }
        return n;
    }

    private static Node walkup(Node n, int count) {
        while (n != null && (count--) != 0) {
            n = n.parent;
        }
        return n;
    }

    public final void collide(DbvtAabbMm volume, ICollide<X> policy) {
        if (root!=null)
            collide(root, volume, policy);
    }

    public void clear() {
        if (root != null) {
            recursedeletenode(this, root);
        }
        //btAlignedFree(m_free);
        free = null;
    }

    public boolean empty() {
        return (root == null);
    }

    public void optimizeBottomUp() {
        if (root != null) {
            ObjectArrayList<Node> leaves = new ObjectArrayList<>(this.leaves.get());
            fetchleaves(this, root, leaves);
            bottomup(this, leaves);
            root = leaves.get(0);
        }
    }

    public void optimizeTopDown() {
        optimizeTopDown(128);
    }

    private void optimizeTopDown(int bu_treshold) {
        if (root != null) {
            ObjectArrayList<Node> leaves = new ObjectArrayList<>(this.leaves.get());
            fetchleaves(this, root, leaves);
            root = topdown(this, leaves, bu_treshold);
        }
    }

    public void optimizeIncremental(int passes) {
        if (passes < 0) {
            passes = leaves.get();
        }

        if (root != null && (passes > 0)) {
            Node[] root_ref = new Node[1];
            do {
                Node node = root;
                int bit = 0;
                while (node.isinternal()) {
                    root_ref[0] = root;
                    node = sort(node, root_ref).childs[(opath >>> bit) & 1];
                    root = root_ref[0];

                    bit = (bit + 1) & (/*sizeof(unsigned)*/4 * 8 - 1);
                }
                update(node);
                ++opath;
            }
            while ((--passes) != 0);
        }
    }

    public Node<X> put(X data, DbvtAabbMm box) {
        Node<X> leaf = node(this, null, box, data);
        synchronized(this) {
            insertleaf(this, root, leaf);
            leaves.getAndIncrement();
        }
        return leaf;
    }

    public Node<X> remove(X data, DbvtAabbMm box) {
        throw new UnsupportedOperationException("TODO");
    }

    private void update(Node leaf) {
        update(leaf, -1);
    }

    private void update(Node leaf, int lookahead) {
        Node root = removeleaf(this, leaf);
        if (root != null) {
            if (lookahead >= 0) {
                for (int i = 0; (i < lookahead) && root.parent != null; i++) {
                    root = root.parent;
                }
            } else {
                root = this.root;
            }
        }
        insertleaf(this, root, leaf);
    }

    public void update(Node leaf, DbvtAabbMm volume) {
        Node root = removeleaf(this, leaf);
        if (root != null) {
            if (lkhd >= 0) {
                for (int i = 0; (i < lkhd) && root.parent != null; i++) {
                    root = root.parent;
                }
            } else {
                root = this.root;
            }
        }
        leaf.volume.set(volume);
        insertleaf(this, root, leaf);
    }

    public boolean update(Node leaf, DbvtAabbMm volume, Vector3f velocity, float margin) {
        if (leaf.volume.contains(volume)) {
            return false;
        }
        Vector3f tmp = new Vector3f();
        tmp.set(margin, margin, margin);
        volume.expand(tmp);
        volume.expandSigned(velocity);
        update(leaf, volume);
        return true;
    }

    public boolean update(Node leaf, DbvtAabbMm volume, Vector3f velocity) {
        if (leaf.volume.contains(volume)) {
            return false;
        }
        volume.expandSigned(velocity);
        update(leaf, volume);
        return true;
    }

    public boolean update(Node leaf, DbvtAabbMm volume, float margin) {
        if (leaf.volume.contains(volume)) {
            return false;
        }
        Vector3f tmp = new Vector3f();
        tmp.set(margin, margin, margin);
        volume.expand(tmp);
        update(leaf, volume);
        return true;
    }

    public void remove(Node leaf) {
        removeleaf(this, leaf);
        deletenode(this, leaf);
        leaves.getAndDecrement();
    }

    ////////////////////////////////////////////////////////////////////////////

    public static class Node<X> {
        public final DbvtAabbMm volume = new DbvtAabbMm();
        final Node<X>[] childs = new Node[2];
        public X data;
        Node<X> parent;

        boolean isleaf() {
            return childs[1] == null;
        }

        boolean isinternal() {
            return !isleaf();
        }
    }

    /**
     * Stack element
     */
    static class sStkNN {
        final Node a;
        final Node b;

        sStkNN(Node na, Node nb) {
            a = na;
            b = nb;
        }
    }

    static class sStkNP {
        final Node node;
        int mask;

        sStkNP(Node n, int m) {
            node = n;
            mask = m;
        }
    }

    static class sStkNPS {
        Node node;
        int mask;
        float value;

        sStkNPS() {
        }

        sStkNPS(Node n, int m, float v) {
            node = n;
            mask = m;
            value = v;
        }

        void set(sStkNPS o) {
            node = o.node;
            mask = o.mask;
            value = o.value;
        }
    }

    static class sStkCLN {
        final Node node;
        final Node parent;

        sStkCLN(Node n, Node p) {
            node = n;
            parent = p;
        }
    }

    public static class ICollide<X> {

        protected boolean descend(Node<X> n) {
            return true;
        }

        protected boolean allLeaves(Node<X> n) {
            return true;
        }

        protected void accept(Node<X> n1, Node<X> n2) { }

        protected void accept(Node<X> n) { }

        protected void accept(Node<X> n, float f) {
            accept(n);
        }
    }

    public abstract static class IWriter {
        public abstract void Prepare(Node root, int numnodes);

        public abstract void WriteNode(Node n, int index, int parent, int child0, int child1);

        public abstract void WriteLeaf(Node n, int index, int parent);
    }

    private static class IClone {
        public void CloneLeaf(Node n) {
        }
    }

}