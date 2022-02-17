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

package com.bulletphysics.bsp;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.convex.ConvexHullShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.JoglWindow3D;
import com.bulletphysics.util.ObjectArrayList;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.List;

/**
 * BspDemo shows the convex collision detection, by converting a Quake BSP file
 * into convex objects and allowing interaction with boxes.
 * 
 * @author jezek2
 */
public class BspDemo extends DemoApplication {

	private static final float CUBE_HALF_EXTENTS = 1;
	private static final float EXTRA_HEIGHT      = -20.0f;
	
	// keep the collision shapes, for deletion/cleanup
	private final ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<>();
	private BroadphaseInterface broadphase;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver solver;
	private DefaultCollisionConfiguration collisionConfiguration;

	private BspDemo() {
		super();
	}
	
	public DynamicsWorld physics() {
		//cameraUp.set(0.0f, 0.0f, 1.0f);
//		forwardAxis = 1;

		setCameraDistance(22.0f);

		// Setup a Physics Simulation Environment

		collisionConfiguration = new DefaultCollisionConfiguration();
		// btCollisionShape* groundShape = new btBoxShape(btVector3(50,3,50));
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldMin = new Vector3f(-1000.0f,-1000.0f,-1000.0f);
		Vector3f worldMax = new Vector3f(1000.0f, 1000.0f, 1000.0f);
		//broadphase = new AxisSweep3(worldMin, worldMax);
		//broadphase = new SimpleBroadphase();
		broadphase = new DbvtBroadphase();
		//btOverlappingPairCache* broadphase = new btSimpleBroadphase();
		solver = new SequentialImpulseConstraintSolver();
		//ConstraintSolver* solver = new OdeConstraintSolver;
		DiscreteDynamicsWorld world = new DiscreteDynamicsWorld(dispatcher,broadphase,solver,collisionConfiguration);

		Vector3f gravity = new Vector3f();
		//gravity.negate(cameraUp);
		gravity.scale(10.0f);
		world.setGravity(gravity);

		try {
			new BspToBulletConverter(world).convertBsp(getClass().getResourceAsStream("/exported.bsp.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}


		return world;
	}
	
	

	public static void main(String... args) {
		BspDemo demo = new BspDemo();

		new JoglWindow3D(demo, 800, 600);
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	private class BspToBulletConverter extends BspConverter {
		private final DiscreteDynamicsWorld world;

		BspToBulletConverter(DiscreteDynamicsWorld world) {
			this.world = world;
		}

		@Override
		public void addConvexVerticesCollider(List<Vector3f> vertices) {
			if (!vertices.isEmpty()) {
				float mass = 0.0f;
				Transform startTransform = new Transform();
				// can use a shift
				startTransform.setIdentity();
				startTransform.origin.set(0, 0, -10.0f);
				
				// this create an internal copy of the vertices
				CollisionShape shape = new ConvexHullShape(vertices);
				collisionShapes.add(shape);

				//btRigidBody* body = m_demoApp->localCreateRigidBody(mass, startTransform,shape);
				world.localCreateRigidBody(shape, mass, startTransform);
			}
		}
	}
	
}