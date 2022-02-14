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

package com.bulletphysics.movingconcave;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.mesh.TriangleIndexVertexArray;
import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.collision.shapes.simple.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.extras.gimpact.GImpactCollisionAlgorithm;
import com.bulletphysics.extras.gimpact.GImpactMeshShape;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.JoglWindow3D;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * 
 * 
 * @author jezek2
 */
public class MovingConcaveDemo extends DemoApplication {
	
	private BroadphaseInterface overlappingPairCache;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver solver;
	private DefaultCollisionConfiguration collisionConfiguration;
	
	private CollisionShape trimeshShape;

	private MovingConcaveDemo() {
		super();
	}
	


	private void initGImpactCollision() {
		// create trimesh
		TriangleIndexVertexArray indexVertexArrays = new TriangleIndexVertexArray(
				Bunny.NUM_TRIANGLES, Bunny.getIndexBuffer(), 4 * 3,
				Bunny.NUM_VERTICES, Bunny.getVertexBuffer(), 4 * 3);

		GImpactMeshShape trimesh = new GImpactMeshShape(indexVertexArrays);
		trimesh.setLocalScaling(new Vector3f(4.0f, 4.0f, 4.0f));
		trimesh.updateBound();
		trimeshShape = trimesh;

		// register algorithm
		GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);
	}
	
	public DynamicsWorld physics() {
		setCameraDistance(30.0f);

		// collision configuration contains default setup for memory, collision setup
		collisionConfiguration = new DefaultCollisionConfiguration();

		// use the default collision dispatcher. For parallel processing you can use a diffent dispatcher (see Extras/BulletMultiThreaded)
		dispatcher = new CollisionDispatcher(collisionConfiguration);

		overlappingPairCache = new DbvtBroadphase();

		// the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
		solver = new SequentialImpulseConstraintSolver();
		
		// TODO: needed for SimpleDynamicsWorld
		//sol.setSolverMode(sol.getSolverMode() & ~SolverMode.SOLVER_CACHE_FRIENDLY.getMask());

		DynamicsWorld world = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);
		//dynamicsWorld = new SimpleDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfiguration);

		world.setGravity(new Vector3f(0.0f, -10.0f, 0.0f));
		
		initGImpactCollision();

		float mass = 0.0f;
		Transform startTransform = new Transform();
		startTransform.setIdentity();

		CollisionShape staticboxShape1 = new BoxShape(new Vector3f(200.0f, 1.0f, 200.0f)); // floor
		CollisionShape staticboxShape2 = new BoxShape(new Vector3f(1.0f, 50.0f, 200.0f)); // left wall
		CollisionShape staticboxShape3 = new BoxShape(new Vector3f(1.0f, 50.0f, 200.0f)); // right wall
		CollisionShape staticboxShape4 = new BoxShape(new Vector3f(200.0f, 50.0f, 1.0f)); // front wall
		CollisionShape staticboxShape5 = new BoxShape(new Vector3f(200.0f, 50.0f, 1.0f)); // back wall

		CompoundShape staticScenario = new CompoundShape(); // static scenario

		startTransform.origin.set(0.0f, 0.0f, 0.0f);
		staticScenario.addChildShape(startTransform, staticboxShape1);
		startTransform.origin.set(-200.0f, 25.0f, 0.0f);
		staticScenario.addChildShape(startTransform, staticboxShape2);
		startTransform.origin.set(200.0f, 25.0f, 0.0f);
		staticScenario.addChildShape(startTransform, staticboxShape3);
		startTransform.origin.set(0.0f, 25.0f, 200.0f);
		staticScenario.addChildShape(startTransform, staticboxShape4);
		startTransform.origin.set(0.0f, 25.0f, -200.0f);
		staticScenario.addChildShape(startTransform, staticboxShape5);

		startTransform.origin.set(0.0f, 0.0f, 0.0f);

		RigidBody staticBody = world.localCreateRigidBody(staticScenario, mass, startTransform);

		staticBody.setCollisionFlags(staticBody.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);

		// enable custom material callback
		//staticBody.setCollisionFlags(staticBody.getCollisionFlags() | CollisionFlags.CUSTOM_MATERIAL_CALLBACK);

		// static plane
		Vector3f normal = new Vector3f(0.4f, 1.5f, -0.4f);
		normal.normalize();
		CollisionShape staticplaneShape6 = new StaticPlaneShape(normal, 0.0f); // A plane

		startTransform.origin.set(0.0f, 0.0f, 0.0f);

		RigidBody staticBody2 = world.localCreateRigidBody(staticplaneShape6, mass, startTransform);

		staticBody2.setCollisionFlags(staticBody2.getCollisionFlags() | CollisionFlags.STATIC_OBJECT);

		for (int i=0; i<9; i++) {
			CollisionShape boxShape = new BoxShape(new Vector3f(1.0f, 1.0f, 1.0f));
			startTransform.origin.set(2.0f * i - 5.0f, 2.0f, -3.0f);
			world.localCreateRigidBody(boxShape, 1, startTransform);
		}

		return world;
	}
	
	private void shootTrimesh(Vector3f destination) {
		if (world != null) {
			float mass = 4.0f;
			Transform startTransform = new Transform();
			startTransform.setIdentity();
			Vector3f camPos = cameraPosition();
			startTransform.origin.set(camPos);

			RigidBody body = world.localCreateRigidBody(trimeshShape, mass, startTransform);

			Vector3f linVel = new Vector3f(destination.x - camPos.x, destination.y - camPos.y, destination.z - camPos.z);
			linVel.normalize();
			linVel.scale(ShootBoxInitialSpeed * 0.25f);

			Transform tr = new Transform();
			tr.origin.set(camPos);
			tr.setRotation(new Quat4f(0.0f, 0.0f, 0.0f, 1.0f));
			body.setWorldTransform(tr);

			body.setLinearVelocity(linVel);
			body.setAngularVelocity(new Vector3f());
		}
	}

	@Override
	public void keyboardCallback(char key) {
		switch (key) {
			case '.' -> shootTrimesh(cameraTarget());
			default -> super.keyboardCallback(key);
		}
	}
	
	public static void main(String... args) {
		MovingConcaveDemo concaveDemo = new MovingConcaveDemo();

		new JoglWindow3D(concaveDemo, 800, 600);
	}
	
}