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

import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.JoglWindow3D;
import jcog.random.XoRoShiRo128PlusRandom;

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

	private static final int START_POS_X = -5;
	private static final int START_POS_Y = -5;
	private static final int START_POS_Z = -3;

	public DynamicsWorld physics() {

		var w = new DiscreteDynamicsWorld();
		w.setGravity(
			new Vector3f(0, -10, 0)
			//new Vector3f(0, 0, 0) //zero-gravity
		);

		ground(w);

		objects(w);

		return w;
	}

	private void objects(DynamicsWorld w) {
		// Re-using the same CollisionShape is better for memory usage and performance

		var s =
			new BoxShape(new Vector3f(1, 1, 1));
			//new SphereShape(1f);

		Transform t = Transform.identity();

		float mass = 1;

		float x = START_POS_X - ARRAY_SIZE_X / 2.0f;
		float y = START_POS_Z - ARRAY_SIZE_Z / 2.0f;

		var rng = new XoRoShiRo128PlusRandom(1);

		for (int k = 0; k < ARRAY_SIZE_Y; k++) {
			for (int i = 0; i < ARRAY_SIZE_X; i++) {
				for (int j = 0; j < ARRAY_SIZE_Z; j++) {
					t.pos(
							2 * i + x,
							2 * k + START_POS_Y + 10,
							2 * j + y);

					var b = new RigidBody(s, t, mass);
					b.color.set(rng.nextFloat(), rng.nextFloat(), rng.nextFloat());
					w.addBody(b);
				}
			}
		}
	}

	private void ground(DynamicsWorld world) {
		world.addBody(new RigidBody(
				new BoxShape(new Vector3f(50, 50, 50))
				//new StaticPlaneShape(new Vector3f(0, 1, 0), 50)
		, Transform.identity().pos(0, -56, 0), 0));
	}

	public static void main(String... args)  {
		new JoglWindow3D(new BasicDemo(), 800, 600);
	}


}