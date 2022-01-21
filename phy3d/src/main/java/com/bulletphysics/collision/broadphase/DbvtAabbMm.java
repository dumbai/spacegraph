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

import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Vector3f;

import static com.bulletphysics.linearmath.VectorUtil.coord;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;

/**
 *
 * @author jezek2
 */
public class DbvtAabbMm {

	public final Vector3f min = new Vector3f();
	public final Vector3f max = new Vector3f();

	public DbvtAabbMm() {
	}

	public DbvtAabbMm(DbvtAabbMm o) {
		set(o);
	}
	
	public void set(DbvtAabbMm o) {
		min.set(o.min);
		max.set(o.max);
	}
	
	public static void swap(DbvtAabbMm p1, DbvtAabbMm p2) {
		Vector3f tmp = new Vector3f();
		
		tmp.set(p1.min);
		p1.min.set(p2.min);
		p2.min.set(tmp);

		tmp.set(p1.max);
		p1.max.set(p2.max);
		p2.max.set(tmp);
	}

	public Vector3f center(Vector3f out) {
		out.add(min, max);
		out.scale(0.5f);
		return out;
	}
	
	public Vector3f lengths(Vector3f out) {
		out.sub(max, min);
		return out;
	}
	
	public Vector3f Extents(Vector3f out) {
		out.sub(max, min);
		out.scale(0.5f);
		return out;
	}

	public final DbvtAabbMm set(Vector3f min, Vector3f max) {
		fromMinMax(min, max, this);
		return this;
	}
	public final DbvtAabbMm setRadius(Vector3f center, float rad) {
		fromRadius(center, rad, this);
		return this;
	}
	public final DbvtAabbMm setExtents(Vector3f center, Vector3f extents) {
		fromExtents(center, extents, this);
		return this;
	}

	private static DbvtAabbMm fromExtents(Vector3f center, Vector3f e, DbvtAabbMm out) {
		out.min.sub(center, e);
		out.max.add(center, e);
		return out;
	}

	public static DbvtAabbMm fromRadius(Vector3f center, float r, DbvtAabbMm out) {
		Vector3f tmp = new Vector3f();
		tmp.set(r, r, r);
		return fromExtents(center, tmp, out);
	}

	public static DbvtAabbMm fromMinMax(Vector3f mi, Vector3f mx, DbvtAabbMm out) {
		out.min.set(mi);
		out.max.set(mx);
		return out;
	}
	
	//public static  DbvtAabbMm	FromPoints( btVector3* pts,int n);
	//public static  DbvtAabbMm	FromPoints( btVector3** ppts,int n);
	
	public void expand(Vector3f e) {
		min.sub(e);
		max.add(e);
	}

	public void expandSigned(Vector3f e) {
		if (e.x > 0) {
			max.x += e.x;
		}
		else {
			min.x += e.x;
		}
		
		if (e.y > 0) {
			max.y += e.y;
		}
		else {
			min.y += e.y;
		}
		
		if (e.z > 0) {
			max.z += e.z;
		}
		else {
			min.z += e.z;
		}
	}

	public boolean contains(DbvtAabbMm a) {
		return ((min.x <= a.min.x) &&
		        (min.y <= a.min.y) &&
		        (min.z <= a.min.z) &&
		        (max.x >= a.max.x) &&
		        (max.y >= a.max.y) &&
		        (max.z >= a.max.z));
	}

	public int Classify(Vector3f n, float o, int s) {
		Vector3f pi = new Vector3f();
		Vector3f px = new Vector3f();

		switch (s) {
			case (0 + 0 + 0) -> {
				px.set(min.x, min.y, min.z);
				pi.set(max.x, max.y, max.z);
			}
			case (1 + 0 + 0) -> {
				px.set(max.x, min.y, min.z);
				pi.set(min.x, max.y, max.z);
			}
			case (0 + 2 + 0) -> {
				px.set(min.x, max.y, min.z);
				pi.set(max.x, min.y, max.z);
			}
			case (1 + 2 + 0) -> {
				px.set(max.x, max.y, min.z);
				pi.set(min.x, min.y, max.z);
			}
			case (0 + 0 + 4) -> {
				px.set(min.x, min.y, max.z);
				pi.set(max.x, max.y, min.z);
			}
			case (1 + 0 + 4) -> {
				px.set(max.x, min.y, max.z);
				pi.set(min.x, max.y, min.z);
			}
			case (0 + 2 + 4) -> {
				px.set(min.x, max.y, max.z);
				pi.set(max.x, min.y, min.z);
			}
			case (1 + 2 + 4) -> {
				px.set(max.x, max.y, max.z);
				pi.set(min.x, min.y, min.z);
			}
		}
		
		if ((n.dot(px) + o) < 0) {
			return -1;
		}
		if ((n.dot(pi) + o) >= 0) {
			return +1;
		}
		return 0;
	}

	public float ProjectMinimum(Vector3f v, int signs) {
		Vector3f[] b = {max, min};
		return new Vector3f(
			 b[(signs >> 0) & 1].x,
		      b[(signs >> 1) & 1].y,
		      b[(signs >> 2) & 1].z).dot(v);
	}
	 
	public static boolean intersects(DbvtAabbMm a, DbvtAabbMm b) {
		Vector3f aMin = a.min;
		Vector3f aMax = a.max;
		Vector3f bMax = b.max;
		Vector3f bMin = b.min;
		return ((aMin.x <= bMax.x) &&
		        (aMax.x >= bMin.x) &&
		        (aMin.y <= bMax.y) &&
		        (aMax.y >= bMin.y) &&
		        (aMin.z <= bMax.z) &&
		        (aMax.z >= bMin.z));
	}

	public static boolean intersects(DbvtAabbMm a, DbvtAabbMm b, Transform xform) {
		Vector3f d0 = new Vector3f();
		Vector3f d1 = new Vector3f();
		Vector3f tmp = new Vector3f();

		// JAVA NOTE: check
		b.center(d0);
		xform.transform(d0);
		d0.sub(a.center(tmp));

		MatrixUtil.transposeTransform(d1, d0, xform.basis);

		float[] s0 = { 0, 0 };
		float[] s1 = new float[2];
		s1[0] = xform.origin.dot(d0);
		s1[1] = s1[0];

		a.AddSpan(d0, s0, 0, s0, 1);
		b.AddSpan(d1, s1, 0, s1, 1);
		return !(s0[0] > (s1[1])) && !(s0[1] < (s1[0]));
	}

	public static boolean intersects(DbvtAabbMm a, Vector3f b) {
		Vector3f aMin = a.min, aMax = a.max;
		return ((b.x >= aMin.x) &&
		        (b.y >= aMin.y) &&
		        (b.z >= aMin.z) &&
		        (b.x <= aMax.x) &&
		        (b.y <= aMax.y) &&
		        (b.z <= aMax.z));
	}

	public static boolean intersects(DbvtAabbMm a, Vector3f org, Vector3f invdir, int[] signs) {
		Vector3f[] bounds = {a.min, a.max};
		float txmin = (bounds[signs[0]].x - org.x) * invdir.x;
		float txmax = (bounds[1 - signs[0]].x - org.x) * invdir.x;
		float tymin = (bounds[signs[1]].y - org.y) * invdir.y;
		float tymax = (bounds[1 - signs[1]].y - org.y) * invdir.y;
		if ((txmin > tymax) || (tymin > txmax)) {
			return false;
		}
		
		if (tymin > txmin) {
			txmin = tymin;
		}
		if (tymax < txmax) {
			txmax = tymax;
		}
		float tzmin = (bounds[signs[2]].z - org.z) * invdir.z;
		float tzmax = (bounds[1 - signs[2]].z - org.z) * invdir.z;
		if ((txmin > tzmax) || (tzmin > txmax)) {
			return false;
		}
		
		if (tzmin > txmin) {
        }
		if (tzmax < txmax) {
			txmax = tzmax;
		}
		return (txmax > 0);
	}

	public static float proximity(DbvtAabbMm a, DbvtAabbMm b) {
		Vector3f aMin = a.min, aMax = a.max, bMin = b.min, bMax = b.max;
		return  Math.abs((aMin.x + aMax.x) - (bMin.x + bMax.x)) +
				Math.abs((aMin.y + aMax.y) - (bMin.y + bMax.y)) +
				Math.abs((aMin.z + aMax.z) - (bMin.z + bMax.z));
	}

	public DbvtAabbMm merge(Vector3f v) {
		for (int i=0; i<3; i++) {
			float vi = coord(v, i);
			coord(min, i, Math.min(coord(min, i), vi));
			coord(max, i, Math.max(coord(max, i), vi));
		}
		return this;
	}

	public static void merge(DbvtAabbMm a, DbvtAabbMm b, DbvtAabbMm r) {
		for (int i=0; i<3; i++) {
			coord(r.min, i, Math.min(coord(a.min, i), coord(b.min, i)));

			coord(r.max, i, Math.max(coord(a.max, i), coord(b.max, i)));
		}
	}

	public static boolean notEqual(DbvtAabbMm a, DbvtAabbMm b) {
		return ((a.min.x != b.min.x) ||
		        (a.min.y != b.min.y) ||
		        (a.min.z != b.min.z) ||
		        (a.max.x != b.max.x) ||
		        (a.max.y != b.max.y) ||
		        (a.max.z != b.max.z));
	}
	
	private void AddSpan(Vector3f d, float[] smi, int smi_idx, float[] smx, int smx_idx) {
		for (int i=0; i<3; i++) {
			if (coord(d, i) < 0) {
				smi[smi_idx] += coord(max, i) * coord(d, i);
				smx[smx_idx] += coord(min, i) * coord(d, i);
			}
			else {
				smi[smi_idx] += coord(min, i) * coord(d, i);
				smx[smx_idx] += coord(max, i) * coord(d, i);
			}
		}
	}

	public boolean intersects(DbvtAabbMm v) {
		return this==v || intersects(this, v);
	}

	public DbvtAabbMm merge(Iterable<Vector3f> vv) {
		min.set(POSITIVE_INFINITY,POSITIVE_INFINITY,POSITIVE_INFINITY);
		max.set(NEGATIVE_INFINITY,NEGATIVE_INFINITY,NEGATIVE_INFINITY);
		for (Vector3f v : vv)
			merge(v);
		return this;
	}
}