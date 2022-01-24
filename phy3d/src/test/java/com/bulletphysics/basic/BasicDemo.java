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

package com.bulletphysics.basic;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.JOGL;

import javax.vecmath.Vector3f;

/**
 * BasicDemo is good starting point for learning the code base and porting.
 * 
 * @author jezek2
 */
public class BasicDemo extends DemoApplication {

	// create 125 (5x5x5) dynamic object
	private static final int ARRAY_SIZE_X = 9;
	private static final int ARRAY_SIZE_Y = 9;
	private static final int ARRAY_SIZE_Z = 9;

	// maximum number of objects (and allow user to shoot additional boxes)
//	private static final int MAX_PROXIES = (ARRAY_SIZE_X*ARRAY_SIZE_Y*ARRAY_SIZE_Z + 1024);

	private static final int START_POS_X = -5;
	private static final int START_POS_Y = -5;
	private static final int START_POS_Z = -3;
	
	private BroadphaseInterface broadphase;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver solver;

	private BasicDemo() {
		super();
	}
	

	public DiscreteDynamicsWorld physics() {
		setCameraDistance(30);

		// collision configuration contains default setup for memory, collision setup
		var collisionConfiguration = new DefaultCollisionConfiguration();

		// use the default collision dispatcher. For parallel processing you can use a diffent dispatcher (see Extras/BulletMultiThreaded)
		dispatcher = new CollisionDispatcher(collisionConfiguration);

		broadphase = new DbvtBroadphase();

		// the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
		solver = new SequentialImpulseConstraintSolver();
		
		// TODO: needed for SimpleDynamicsWorld
		//sol.setSolverMode(sol.getSolverMode() & ~SolverMode.SOLVER_CACHE_FRIENDLY.getMask());

		DiscreteDynamicsWorld world = new DiscreteDynamicsWorld(dispatcher, broadphase, solver);

		world.setGravity(new Vector3f(0, -10, 0));

		// create a few basic rigid bodies
		CollisionShape groundShape = new BoxShape(new Vector3f(50, 50, 50));
		//CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 50);

		collisionShapes.add(groundShape);

		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(0, -56, 0);

		// We can also use DemoApplication::localCreateRigidBody, but for clarity it is provided here:
		{
			float mass = 0;

			// rigidbody is dynamic if and only if mass is non zero, otherwise static
			boolean isDynamic = (mass != 0);

			Vector3f localInertia = new Vector3f();
			if (isDynamic) {
				groundShape.calculateLocalInertia(mass, localInertia);
			}

			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, groundShape, localInertia);
			RigidBody body = new RigidBody(rbInfo);

			// add the body to the dynamics world
			world.addRigidBody(body);
		}

		{
			// create a few dynamic rigidbodies
			// Re-using the same collision is better for memory usage and performance

			CollisionShape colShape = new BoxShape(new Vector3f(1, 1, 1));
			//CollisionShape colShape = new SphereShape(1f);
			collisionShapes.add(colShape);

			// Create Dynamic Objects
			Transform startTransform = new Transform();
			startTransform.setIdentity();

			float mass = 1.0f;

			// rigidbody is dynamic if and only if mass is non zero, otherwise static
			boolean isDynamic = (mass != 0);

			Vector3f localInertia = new Vector3f();
			if (isDynamic) {
				colShape.calculateLocalInertia(mass, localInertia);
			}

			float start_x = START_POS_X - ARRAY_SIZE_X / 2.0f;
			float start_z = START_POS_Z - ARRAY_SIZE_Z / 2.0f;

			for (int k = 0; k < ARRAY_SIZE_Y; k++) {
				for (int i = 0; i < ARRAY_SIZE_X; i++) {
					for (int j = 0; j < ARRAY_SIZE_Z; j++) {
						startTransform.origin.set(
                                2.0f * i + start_x,
                                10 + 2.0f * k + START_POS_Y,
                                2.0f * j + start_z);

						// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
						DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
						RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
						RigidBody body = new RigidBody(rbInfo);
						body.setActivationState(RigidBody.ISLAND_SLEEPING);

						world.addRigidBody(body);
						body.setActivationState(RigidBody.ISLAND_SLEEPING);
					}
				}
			}
		}

		return world;
	}
	
	public static void main(String... args)  {

		new JOGL(new BasicDemo(), 800, 600);
//		SpaceGraph.window(new WorldSurface(new BasicDemo()), 800, 600);
	}
	
}