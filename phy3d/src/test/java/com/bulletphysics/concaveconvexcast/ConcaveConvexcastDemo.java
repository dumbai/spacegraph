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

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.mesh.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.mesh.TriangleIndexVertexArray;
import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.JoglWindow3D;
import com.bulletphysics.util.ObjectArrayList;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 * @author jezek2
 */
public class ConcaveConvexcastDemo extends DemoApplication {

	// keep the collision shapes, for deletion/cleanup
	private final ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<>();
	private TriangleIndexVertexArray indexVertexArrays;
	private BroadphaseInterface broadphase;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver solver;
	private DefaultCollisionConfiguration collisionConfiguration;
	private boolean animatedMesh = false;
	
	private static ByteBuffer gVertices;
	private static ByteBuffer gIndices;
	private static BvhTriangleMeshShape trimeshShape;
	private static RigidBody staticBody;
	private static final float waveheight = 5.0f;

	private static final float TRIANGLE_SIZE= 8.0f;
	private static final int NUM_VERTS_X = 30;
	private static final int NUM_VERTS_Y = 30;
	private static final int totalVerts = NUM_VERTS_X*NUM_VERTS_Y;

	private ConvexcastBatch convexcastBatch;

	private ConcaveConvexcastDemo() {
		super();
	}

	private static void setVertexPositions(float waveheight, float offset) {
		int i;
		int j;
		Vector3f tmp = new Vector3f();

		for (i = 0; i < NUM_VERTS_X; i++) {
			for (j = 0; j < NUM_VERTS_Y; j++) {
				tmp.set(
						(i - NUM_VERTS_X * 0.5f) * TRIANGLE_SIZE,
						//0.f,
						waveheight * (float) Math.sin(i + offset) * (float) Math.cos(j + offset),
						(j - NUM_VERTS_Y * 0.5f) * TRIANGLE_SIZE);

				int index = i + j * NUM_VERTS_X;
				gVertices.putFloat((index*3 + 0) * 4, tmp.x);
				gVertices.putFloat((index*3 + 1) * 4, tmp.y);
				gVertices.putFloat((index*3 + 2) * 4, tmp.z);
			}
		}
	}

	@Override
	public void keyboardCallback(char key) {
		if (key == 'g') {
			animatedMesh = !animatedMesh;
			if (animatedMesh) {
				staticBody.setCollisionFlags(staticBody.getCollisionFlags() | CollisionFlags.KINEMATIC_OBJECT);
				staticBody.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
			}
			else {
				staticBody.setCollisionFlags(staticBody.getCollisionFlags() & ~CollisionFlags.KINEMATIC_OBJECT);
				staticBody.forceActivationState(CollisionObject.ACTIVE_TAG);
			}
		}

		super.keyboardCallback(key);
	}

	public DynamicsWorld physics() {
		final float TRISIZE = 10.0f;

		//#define USE_TRIMESH_SHAPE 1
		//#ifdef USE_TRIMESH_SHAPE

		int vertStride = 3 * 4;
		int indexStride = 3 * 4;

		int totalTriangles = 2 * (NUM_VERTS_X - 1) * (NUM_VERTS_Y - 1);

		gVertices = ByteBuffer.allocateDirect(totalVerts * 3 * 4).order(ByteOrder.nativeOrder());
		gIndices = ByteBuffer.allocateDirect(totalTriangles * 3 * 4).order(ByteOrder.nativeOrder());

		int i;

		setVertexPositions(waveheight, 0.0f);

		//int index=0;
		gIndices.clear();
		for (i = 0; i < NUM_VERTS_X - 1; i++) {
			for (int j = 0; j < NUM_VERTS_Y - 1; j++) {
				gIndices.putInt(j * NUM_VERTS_X + i);
				gIndices.putInt(j * NUM_VERTS_X + i + 1);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i + 1);

				gIndices.putInt(j * NUM_VERTS_X + i);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i + 1);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i);
			}
		}
		gIndices.flip();

		indexVertexArrays = new TriangleIndexVertexArray(totalTriangles,
				gIndices,
				indexStride,
				totalVerts, gVertices, vertStride);

		boolean useQuantizedAabbCompression = true;

		//comment out the next line to read the BVH from disk (first run the demo once to create the BVH)
		//#define SERIALIZE_TO_DISK 1
		//#ifdef SERIALIZE_TO_DISK
		trimeshShape = new BvhTriangleMeshShape(indexVertexArrays, useQuantizedAabbCompression);
		collisionShapes.add(trimeshShape);

		CollisionShape groundShape = trimeshShape;

		collisionConfiguration = new DefaultCollisionConfiguration();

		dispatcher = new CollisionDispatcher(collisionConfiguration);

		Vector3f worldMin = new Vector3f(-1000.0f, -1000.0f, -1000.0f);
		Vector3f worldMax = new Vector3f(1000.0f, 1000.0f, 1000.0f);
		//broadphase = new AxisSweep3(worldMin, worldMax);
		broadphase = new DbvtBroadphase();
		solver = new SequentialImpulseConstraintSolver();
		DynamicsWorld world = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);

		float mass = 0.0f;
		Transform startTransform = new Transform();
		startTransform.setIdentity();
		startTransform.origin.set(0.0f, -2.0f, 0.0f);

		CollisionShape colShape = new BoxShape(new Vector3f(1.0f, 1.0f, 1.0f));
		collisionShapes.add(colShape);

		{
			for (i = 0; i < 10; i++) {
				//btCollisionShape* colShape = new btCapsuleShape(0.5,2.0);//boxShape = new btSphereShape(1.f);
				startTransform.origin.set(2.0f * i, 10.0f, 1.0f);
				world.localCreateRigidBody(colShape, 1.0f, startTransform);
			}
		}

		startTransform.setIdentity();
		staticBody = world.localCreateRigidBody(groundShape, mass, startTransform);

		staticBody.setCollisionFlags(staticBody.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);

		// enable custom material callback
		staticBody.setCollisionFlags(staticBody.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);

		convexcastBatch = new ConvexcastBatch(40.0f, 0.0f, -10.0f, 10.0f);
		return world;
	}

	private static float offset = 0.0f;
	
//	@Override
//	public void display(GLAutoDrawable arg) {
//		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//		poll();
//		float dt = getDeltaTimeMicroseconds() * 0.000001f;
//
//		if (animatedMesh) {
//			long t0 = System.nanoTime();
//
//			offset += 0.01f;
//
//			setVertexPositions(waveheight, offset);
//
//			// JAVA NOTE: 2.70b1: replace with proper code
//			trimeshShape.refitTree(null, null);
//
//			// clear all contact points involving mesh proxy. Note: this is a slow/unoptimized operation.
//			world.getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(staticBody.getBroadphaseHandle(), getWorld().getDispatcher());
//
//			BulletStats.updateTime = (System.nanoTime() - t0) / 1000000;
//		}
//
//		world.stepSimulation(dt);
//
//		// optional but useful: debug drawing
//		world.debugDrawWorld();
//
//		convexcastBatch.move(dt);
//		convexcastBatch.cast(world);
//
//		renderme();
//		convexcastBatch.draw(gl);
//
//		//glFlush();
//		//glutSwapBuffers();
//	}
//
	
	public static void main(String... args)  {
		ConcaveConvexcastDemo concaveConvexcastDemo = new ConcaveConvexcastDemo();
		concaveConvexcastDemo.setCameraDistance(30.0f);

		new JoglWindow3D(concaveConvexcastDemo, 800, 600);
	}
	
}