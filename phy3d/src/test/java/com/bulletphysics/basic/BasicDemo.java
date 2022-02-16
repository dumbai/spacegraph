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

import com.bulletphysics.SpaceGraphCDemo;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.JoglWindow3D;

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

	public DynamicsWorld physics() {

		DynamicsWorld w = new DiscreteDynamicsWorld();
		w.setGravity(
				//new Vector3f(0, 0, 0)
				new Vector3f(0, -10, 0)
		);

		ground(w);

		objects(w);

		return w;
	}

	private void objects(DynamicsWorld world) {
		// Re-using the same CollisionShape is better for memory usage and performance

		CollisionShape s =
				new BoxShape(new Vector3f(1, 1, 1));
		//new SphereShape(1f);

		Transform t = Transform.identity();

		float mass = 1;


		float x = START_POS_X - ARRAY_SIZE_X / 2.0f;
		float y = START_POS_Z - ARRAY_SIZE_Z / 2.0f;

		for (int k = 0; k < ARRAY_SIZE_Y; k++) {
			for (int i = 0; i < ARRAY_SIZE_X; i++) {
				for (int j = 0; j < ARRAY_SIZE_Z; j++) {
					t.origin.set(
							2 * i + x,
							10 + 2 * k + START_POS_Y,
							2 * j + y);

					world.addBody(new RigidBody(s, t, mass
					));
				}
			}
		}
	}

	private void ground(DynamicsWorld world) {
		// create a few basic rigid bodies
		//new StaticPlaneShape(new Vector3f(0, 1, 0), 50);

		world.addBody(new RigidBody(new BoxShape(new Vector3f(50, 50, 50)), Transform.identity().pos(0, -56, 0), 0
		));
	}

	public static void main(String... args)  {

		new JoglWindow3D(new BasicDemo(), 800, 600);
//		SpaceGraph.window(new WorldSurface(new BasicDemo()), 800, 600);
	}


}