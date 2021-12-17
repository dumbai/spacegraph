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

package com.bulletphysics.concaveconvexcast;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestConvexResultCallback;
import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.render.IGL;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import static com.bulletphysics.render.IGL.GL_LIGHTING;
import static com.bulletphysics.render.IGL.GL_LINES;

/**
 * Scrolls back and forth over terrain.
 *
 * @author jezek2
 */
class ConvexcastBatch {

	private static final int NUMRAYS_IN_BAR = 100;

	private final Vector3f[] source = new Vector3f[NUMRAYS_IN_BAR];
	private final Vector3f[] dest = new Vector3f[NUMRAYS_IN_BAR];
	private final Vector3f[] direction = new Vector3f[NUMRAYS_IN_BAR];
	private final Vector3f[] hit_com = new Vector3f[NUMRAYS_IN_BAR];
	private final Vector3f[] hit_surface = new Vector3f[NUMRAYS_IN_BAR];
	private final float[] hit_fraction = new float[NUMRAYS_IN_BAR];
	private final Vector3f[] normal = new Vector3f[NUMRAYS_IN_BAR];

	{
		for (int i=0; i<NUMRAYS_IN_BAR; i++) {
			source[i] = new Vector3f();
			dest[i] = new Vector3f();
			direction[i] = new Vector3f();
			hit_com[i] = new Vector3f();
			hit_surface[i] = new Vector3f();
			normal[i] = new Vector3f();
		}
	}

	private int frame_counter;
	private int ms;
	private int sum_ms;
	private int sum_ms_samples;
	private int min_ms;
	private int max_ms;

	//#ifdef USE_BT_CLOCK
	private final Clock frame_timer = new Clock();
	//#endif //USE_BT_CLOCK

	private float dx;
	private float min_x;
	private float max_x;
	private float min_y;
	private float max_y;
	private float sign;

	private final Vector3f boxShapeHalfExtents = new Vector3f();
	private final BoxShape boxShape;

	ConvexcastBatch() {
		boxShape = new BoxShape(new Vector3f());
		ms = 0;
		max_ms = 0;
		min_ms = 9999;
		sum_ms_samples = 0;
		sum_ms = 0;
	}

	ConvexcastBatch(boolean unused, float ray_length, float min_z, float max_z) {
		this(unused, ray_length, min_z, max_z, -10, 10);
	}

	private ConvexcastBatch(boolean unused, float ray_length, float min_z, float max_z, float min_y, float max_y) {
		boxShapeHalfExtents.set(1.0f, 1.0f, 1.0f);
		boxShape = new BoxShape(boxShapeHalfExtents);
		frame_counter = 0;
		ms = 0;
		max_ms = 0;
		min_ms = 9999;
		sum_ms_samples = 0;
		sum_ms = 0;
		dx = 10.0f;
		min_x = -40;
		max_x = 20;
		this.min_y = min_y;
		this.max_y = max_y;
		sign = 1.0f;
		float dalpha = 2.0f * (float)Math.PI / NUMRAYS_IN_BAR;
		for (int i=0; i<NUMRAYS_IN_BAR; i++) {
			float z = (max_z - min_z) / NUMRAYS_IN_BAR * i + min_z;
			source[i].set(min_x, max_y, z);
			dest[i].set(min_x + ray_length, min_y, z);
			normal[i].set(1.0f, 0.0f, 0.0f);
		}
	}

	ConvexcastBatch (float ray_length, float z) {
		this(ray_length, z, -1000, 10);
	}

	ConvexcastBatch(float ray_length, float z, float min_y, float max_y) {
		boxShapeHalfExtents.set(1.0f, 1.0f, 1.0f);
		boxShape = new BoxShape(boxShapeHalfExtents);
		frame_counter = 0;
		ms = 0;
		max_ms = 0;
		min_ms = 9999;
		sum_ms_samples = 0;
		sum_ms = 0;
		dx = 10.0f;
		min_x = -40;
		max_x = 20;
		this.min_y = min_y;
		this.max_y = max_y;
		sign = 1.0f;
		float dalpha = 2.0f * (float)Math.PI / NUMRAYS_IN_BAR;
		for (int i=0; i<NUMRAYS_IN_BAR; i++) {
			float alpha = dalpha * i;
			// rotate around by alpha degrees y
			Quat4f q = new Quat4f(0.0f, 1.0f, 0.0f, alpha);
			direction[i].set(1.0f, 0.0f, 0.0f);
			Quat4f tmpQuat = new Quat4f(q);
			QuaternionUtil.mul(tmpQuat, direction[i]);
			direction[i].set(tmpQuat.x, tmpQuat.y, tmpQuat.z);
			//direction[i].set(direction[i]);
			source[i].set(min_x, max_y, z);
			dest[i].scaleAdd(ray_length, direction[i], source[i]);
			dest[i].y = min_y;
			normal[i].set(1.0f, 0.0f, 0.0f);
		}
	}

	public void move(float dt) {
		if (dt > (1.0f / 60.0f)) {
			dt = 1.0f / 60.0f;
		}
		
		for (int i=0; i<NUMRAYS_IN_BAR; i++) {
			source[i].x += dx * dt * sign;
			dest[i].x += dx * dt * sign;
		}

		if (source[0].x < min_x) {
			sign = 1.0f;
		}
		else if (source[0].x > max_x) {
			sign = -1.0f;
		}
	}

	public void cast(CollisionWorld cw) {
		//#ifdef USE_BT_CLOCK
		frame_timer.reset();
		//#endif //USE_BT_CLOCK

		for (int i=0; i<NUMRAYS_IN_BAR; i++) {
			ClosestConvexResultCallback cb = new ClosestConvexResultCallback(source[i], dest[i]);

			Quat4f qFrom = new Quat4f();
			Quat4f qTo = new Quat4f();
			QuaternionUtil.setRotation(qFrom, new Vector3f(1.0f, 0.0f, 0.0f), 0.0f);
			QuaternionUtil.setRotation(qTo, new Vector3f(1.0f, 0.0f, 0.0f), 0.7f);

			Transform from = new Transform();
			Transform to = new Transform();
			from.basis.set(qFrom);
			from.origin.set(source[i]);
			to.basis.set(qTo);
			to.origin.set(dest[i]);
			
			cw.convexSweepTest(boxShape, from, to, cb);

			if (cb.hasHit()) {
				hit_surface[i].set(cb.hitPointWorld);
				VectorUtil.setInterpolate3(hit_com[i], source[i], dest[i], cb.closestHitFraction);
				hit_fraction[i] = cb.closestHitFraction;
				normal[i].set(cb.hitNormalWorld);
				normal[i].normalize();
			}
			else {
				hit_com[i].set(dest[i]);
				hit_surface[i].set(dest[i]);
				hit_fraction[i] = 1.0f;
				normal[i].set(1.0f, 0.0f, 0.0f);
			}

		}

		//#ifdef USE_BT_CLOCK
		ms += frame_timer.getTimeMilliseconds();
		//#endif //USE_BT_CLOCK
		
		frame_counter++;
		if (frame_counter > 50) {
			min_ms = Math.min(ms, min_ms);
			max_ms = Math.max(ms, max_ms);
			sum_ms += ms;
			sum_ms_samples++;
			float mean_ms = (float) sum_ms / sum_ms_samples;
			System.out.printf("%d rays in %d ms %d %d %f\n", NUMRAYS_IN_BAR * frame_counter, ms, min_ms, max_ms, mean_ms);
			ms = 0;
			frame_counter = 0;
		}
	}

	private void drawCube(IGL gl, Transform T) {
		float[] m = new float[16];
		T.getOpenGLMatrix(m);
		gl.glPushMatrix();
		gl.glMultMatrix(m);
		gl.glScalef(2.0f * boxShapeHalfExtents.x, 2.0f * boxShapeHalfExtents.y, 2.0f * boxShapeHalfExtents.z);
		gl.drawCube(1.0f);
		gl.glPopMatrix();
	}

	public void draw(IGL gl) {
		gl.glDisable(GL_LIGHTING);
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glBegin(GL_LINES);
		for (int i=0; i<NUMRAYS_IN_BAR; i++) {
			gl.glVertex3f(source[i].x, source[i].y, source[i].z);
			gl.glVertex3f(hit_com[i].x, hit_com[i].y, hit_com[i].z);
		}
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		//gl.glBegin(GL_LINES);
		float normal_scale = 10.0f; // easier to see if this is big
		for (int i=0; i<NUMRAYS_IN_BAR; i++) {
			gl.glVertex3f(hit_surface[i].x, hit_surface[i].y, hit_surface[i].z);
			gl.glVertex3f(hit_surface[i].x + normal_scale * normal[i].x, hit_surface[i].y + normal_scale * normal[i].y, hit_surface[i].z + normal_scale * normal[i].z);
		}
		gl.glEnd();
		gl.glColor3f(0.0f, 1.0f, 1.0f);
		Quat4f qFrom = new Quat4f();
		Quat4f qTo =new Quat4f();
		QuaternionUtil.setRotation(qFrom, new Vector3f(1.0f, 0.0f, 0.0f), 0.0f);
		QuaternionUtil.setRotation(qTo, new Vector3f(1.0f, 0.0f, 0.0f), 0.7f);
		for (int i=0; i<NUMRAYS_IN_BAR; i++) {
			Transform from = new Transform();
			from.basis.set(qFrom);
			from.origin.set(source[i]);

			Transform to = new Transform();
			to.basis.set(qTo);
			to.origin.set(dest[i]);

			Vector3f linVel = new Vector3f();
			Vector3f angVel = new Vector3f();

			TransformUtil.calculateVelocity(from, to, 1.0f, linVel, angVel);
			Transform T = new Transform();
			TransformUtil.integrateTransform(from, linVel, angVel, hit_fraction[i], T);
			drawCube(gl, T);
		}
		gl.glEnable(GL_LIGHTING);
	}

}