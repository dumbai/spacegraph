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

package com.bulletphysics.character;

import com.bulletphysics.bsp.BspConverter;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.convex.ConvexHullShape;
import com.bulletphysics.collision.shapes.convex.ConvexShape;
import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.collision.shapes.simple.CapsuleShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.JoglWindow3D;
import com.bulletphysics.util.ObjectArrayList;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GLAutoDrawable;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.List;

/**
 * 
 * @author tomrbryn
 */
public class CharacterDemo extends DemoApplication {

	private final int maxProxies = 32766;
	private final int maxOverlap = 65535;
	private static int gForward = 0;
	private static int gBackward = 0;
	private static int gLeft = 0;
	private static int gRight = 0;
	private static int gJump = 0;

	private KinematicCharacterController character;
	private PairCachingGhostObject ghostObject;

	public float cameraHeight = 4.0f;

	public float minCameraDistance = 3.0f;
	public float maxCameraDistance = 10.0f;

	// JAVA NOTE: the original demo scaled the bsp room, we scale up the character
	private final float characterScale = 2.0f;
	
	// keep the collision shapes, for deletion/cleanup
	private final ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<>();
	private BroadphaseInterface overlappingPairCache;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver constraintSolver;
	private DefaultCollisionConfiguration collisionConfiguration;

	private CharacterDemo() {
		super();
	}
	
	public DynamicsWorld physics() {
		CollisionShape groundShape = new BoxShape(new Vector3f(50, 3, 50));
		collisionShapes.add(groundShape);

		collisionConfiguration = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldMin = new Vector3f(-1000.0f,-1000.0f,-1000.0f);
		Vector3f worldMax = new Vector3f(1000.0f, 1000.0f, 1000.0f);
		AxisSweep3 sweepBP = new AxisSweep3(worldMin, worldMax);
		overlappingPairCache = sweepBP;

		constraintSolver = new SequentialImpulseConstraintSolver();
		DynamicsWorld world = new DiscreteDynamicsWorld(dispatcher,overlappingPairCache,constraintSolver,collisionConfiguration);

		Transform startTransform = new Transform();
		startTransform.setIdentity();
		startTransform.origin.set(0.0f, 4.0f, 0.0f);

		ghostObject = new PairCachingGhostObject();
		ghostObject.setWorldTransform(startTransform);
		sweepBP.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		float characterHeight = 1.75f * characterScale;
		float characterWidth = 1.75f * characterScale;
		ConvexShape capsule = new CapsuleShape(characterWidth, characterHeight);
		ghostObject.setCollisionShape(capsule);
		ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

		float stepHeight = 0.35f * characterScale;
		character = new KinematicCharacterController(ghostObject, capsule, stepHeight);

		try {
			new BspToBulletConverter(world).convertBsp(getClass().getResourceAsStream("/exported.bsp.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		world.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));

		world.addAction(character);
		

		setCameraDistance(56.0f);
		return world;
	}
	@Override
	protected void renderVolume(GLAutoDrawable drawable) {
		super.renderVolume(drawable);
		//gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		float dt = getDeltaTimeMicroseconds() * 0.000001f;
//		poll();
		if (world != null) {
			// during idle mode, just run 1 simulation step maximum
			int maxSimSubSteps = idle ? 1 : 2;
			if (idle) {
				dt = 1.0f / 420.0f;
			}

			// set walkDirection for our character
			Transform xform = ghostObject.getWorldTransform(new Transform());

			Vector3f forwardDir = new Vector3f();
			xform.basis.getRow(2, forwardDir);
			//printf("forwardDir=%f,%f,%f\n",forwardDir[0],forwardDir[1],forwardDir[2]);
			Vector3f upDir = new Vector3f();
			xform.basis.getRow(1, upDir);
			Vector3f strafeDir = new Vector3f();
			xform.basis.getRow(0, strafeDir);
			forwardDir.normalize();
			upDir.normalize();
			strafeDir.normalize();

			Vector3f walkDirection = new Vector3f(0.0f, 0.0f, 0.0f);
			float walkVelocity = 1.1f * 4.0f; // 4 km/h -> 1.1 m/s
			float walkSpeed = walkVelocity * dt * characterScale;

			if (gLeft != 0) {
				walkDirection.add(strafeDir);
			}

			if (gRight != 0) {
				walkDirection.sub(strafeDir);
			}

			if (gForward != 0) {
				walkDirection.add(forwardDir);
			}

			if (gBackward != 0) {
				walkDirection.sub(forwardDir);
			}

			walkDirection.scale(walkSpeed);
			character.setWalkDirection(walkDirection);

			int numSimSteps = world.next(dt, maxSimSubSteps);

			// optional but useful: debug drawing
			world.debugDrawWorld();
		}


		//glFlush();
		//glutSwapBuffers();
	}

	@Override
	public void reset() {
		world.broadphase().getOverlappingPairCache().cleanProxyFromPairs(
				ghostObject.getBroadphaseHandle(), world().dispatcher());

		character.reset();
		///WTF
		character.warp(new Vector3f(0, -2, 0));
	}

	@Override
	public void specialKeyboardUp(int key) {
		switch (key) {
			case KeyEvent.VK_UP -> gForward = 0;
			case KeyEvent.VK_DOWN -> gBackward = 0;
			case KeyEvent.VK_LEFT -> gLeft = 0;
			case KeyEvent.VK_RIGHT -> gRight = 0;
			default -> super.specialKeyboardUp(key);
		}
	}

	@Override
	public void specialKeyboard(int key) {
		switch (key) {
			case KeyEvent.VK_UP -> gForward = 1;
			case KeyEvent.VK_DOWN -> gBackward = 1;
			case KeyEvent.VK_LEFT -> gLeft = 1;
			case KeyEvent.VK_RIGHT -> gRight = 1;
			case KeyEvent.VK_F1 -> {
				if (character != null && character.canJump()) {
					gJump = 1;
				}
			}
			default -> super.specialKeyboard(key);
		}
	}

//	@Override
//	public void updateCamera(long dtNS) {
//		//if (useDefaultCamera) {
////		if (false) {
////			super.updateCamera();
////			return;
////		}
//
//		gl.glMatrixMode(gl.GL_PROJECTION);
//		gl.glLoadIdentity();
//
//		// look at the vehicle
//		Transform characterWorldTrans = ghostObject.getWorldTransform(new Transform());
//		Vector3f up = new Vector3f();
//		characterWorldTrans.basis.getRow(1, up);
//		Vector3f backward = new Vector3f();
//		characterWorldTrans.basis.getRow(2, backward);
//		backward.scale(-1);
//		up.normalize ();
//		backward.normalize ();
//
//		cameraTargetPosition.set(characterWorldTrans.origin);
//
//		Vector3f cameraPosition = new Vector3f();
//		cameraPosition.scale(2, up);
//		cameraPosition.add(cameraTargetPosition);
//		backward.scale(12);
//		cameraPosition.add(backward);
//
//		// update OpenGL camera settings
//		gl.glFrustum(-1.0, 1.0, -1.0, 1.0, 1.0, 10000.0);
//
//		gl.glMatrixMode(IGL.GL_MODELVIEW);
//		gl.glLoadIdentity();
//
//		gl.gluLookAt(cameraPosition.x, cameraPosition.y, cameraPosition.z,
//		             cameraTargetPosition.x, cameraTargetPosition.y, cameraTargetPosition.z,
//		             cameraUp.x, cameraUp.y, cameraUp.z);
//	}

	public static void main(String... args) {
		CharacterDemo demo = new CharacterDemo();

		new JoglWindow3D(demo, 800, 600);
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	private class BspToBulletConverter extends BspConverter {
		private final DynamicsWorld world;

		public BspToBulletConverter(DynamicsWorld world) {
			this.world = world;
		}

		@Override
		public void addConvexVerticesCollider(List<Vector3f> vertices) {
			if (!vertices.isEmpty()) {
				float mass = 0;
				Transform startTransform = new Transform();
				// can use a shift
				startTransform.setIdentity();
				// JAVA NOTE: port change, we want y to be up.
				startTransform.basis.rotX((float) -Math.PI / 2.0f);
				startTransform.origin.set(0, -10, 0);
				//startTransform.origin.set(0, 0, -10f);
				
				// this create an internal copy of the vertices
				CollisionShape shape = new ConvexHullShape(vertices);
				collisionShapes.add(shape);

				//btRigidBody* body = m_demoApp->localCreateRigidBody(mass, startTransform,shape);
				world.localCreateRigidBody(shape, mass, startTransform);
			}
		}
	}
	
}