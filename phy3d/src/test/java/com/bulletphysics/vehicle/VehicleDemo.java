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

package com.bulletphysics.vehicle;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.mesh.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.mesh.TriangleIndexVertexArray;
import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.collision.shapes.simple.CylinderShapeX;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.vehicle.*;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.GLShapeDrawer;
import com.bulletphysics.render.JoglWindow3D;
import com.bulletphysics.util.ObjectArrayList;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GLAutoDrawable;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * VehicleDemo shows how to setup and use the built-in raycast vehicle.
 * 
 * @author jezek2
 */
public class VehicleDemo extends DemoApplication {

	// By default, Bullet Vehicle uses Y as up axis.
	// You can override the up axis, for example Z-axis up. Enable this define to see how to:
	// //#define FORCE_ZAXIS_UP 1
	
	//#ifdef FORCE_ZAXIS_UP
	//int rightIndex = 0; 
	//int upIndex = 2; 
	//int forwardIndex = 1;
	//btVector3 wheelDirectionCS0(0,0,-1);
	//btVector3 wheelAxleCS(1,0,0);
	//#else
	private static final int rightIndex = 0;
	private static final int upIndex = 1;
	private static final int forwardIndex = 2;
	private static final Vector3f wheelDirectionCS0 = new Vector3f(0,-1,0);
	private static final Vector3f wheelAxleCS = new Vector3f(-1,0,0);
	//#endif
	
	private static final int maxProxies = 32766;
	private static final int maxOverlap = 65535;

	// RaycastVehicle is the interface for the constraint that implements the raycast vehicle
	// notice that for higher-quality slow-moving vehicles, another approach might be better
	// implementing explicit hinged-wheel constraints with cylinder collision, rather then raycasts
	private static float gEngineForce = 0.0f;
	private static float gBreakingForce = 0.0f;

	private static final float maxEngineForce = 1000.0f;//this should be engine/velocity dependent
	private static final float maxBreakingForce = 100.0f;

	private static float gVehicleSteering = 0.0f;
	private static final float steeringIncrement = 0.04f;
	private static final float steeringClamp = 0.3f;
	private static final float wheelRadius = 0.5f;
	private static final float wheelWidth = 0.4f;
	private static final float wheelFriction = 1000;//1e30f;
	private static final float suspensionStiffness = 20.0f;
	private static final float suspensionDamping = 2.3f;
	private static final float suspensionCompression = 4.4f;
	private static final float rollInfluence = 0.1f;//1.0f;

	private static final float suspensionRestLength = 0.6f;

	private static final int CUBE_HALF_EXTENTS = 1;
	
	////////////////////////////////////////////////////////////////////////////
	
	private RigidBody carChassis;
	private final ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<>();
	private BroadphaseInterface overlappingPairCache;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver constraintSolver;
	private DefaultCollisionConfiguration collisionConfiguration;
	private TriangleIndexVertexArray indexVertexArrays;

	private ByteBuffer vertices;

	private final VehicleTuning tuning = new VehicleTuning();
	private VehicleRaycaster vehicleRayCaster;
	private RaycastVehicle vehicle;

	private final float cameraHeight;

	private final float minCameraDistance;
	private final float maxCameraDistance;

	private VehicleDemo() {
		super();
		carChassis = null;
		cameraHeight = 4.0f;
		minCameraDistance = 3.0f;
		maxCameraDistance = 10.0f;
		indexVertexArrays = null;
		vertices = null;
		vehicle = null;
//		cameraPosition.set(30, 30, 30);
	}

	public DynamicsWorld physics() {
		//#ifdef FORCE_ZAXIS_UP
		//m_cameraUp = btVector3(0,0,1);
		//m_forwardAxis = 1;
		//#endif

		CollisionShape groundShape = new BoxShape(new Vector3f(50, 3, 50));
		collisionShapes.add(groundShape);
		collisionConfiguration = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldMin = new Vector3f(-1000, -1000, -1000);
		Vector3f worldMax = new Vector3f(1000, 1000, 1000);
		//overlappingPairCache = new AxisSweep3(worldMin, worldMax);
		//overlappingPairCache = new SimpleBroadphase();
		overlappingPairCache = new DbvtBroadphase();
		constraintSolver = new SequentialImpulseConstraintSolver();
		DynamicsWorld world = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache, constraintSolver, collisionConfiguration);
		//#ifdef FORCE_ZAXIS_UP
		//dynamicsWorld.setGravity(new Vector3f(0, 0, -10));
		//#endif 

		//m_dynamicsWorld->setGravity(btVector3(0,0,0));
		Transform tr = new Transform();
		tr.setIdentity();

		// either use heightfield or triangle mesh
		//#define  USE_TRIMESH_GROUND 1
		//#ifdef USE_TRIMESH_GROUND

		final float TRIANGLE_SIZE = 20.0f;

		// create a triangle-mesh ground
		int vertStride = 4 * 3 /* sizeof(btVector3) */;
		int indexStride = 3 * 4 /* 3*sizeof(int) */;

		final int NUM_VERTS_X = 20;
		final int NUM_VERTS_Y = 20;
		final int totalVerts = NUM_VERTS_X * NUM_VERTS_Y;

		final int totalTriangles = 2 * (NUM_VERTS_X - 1) * (NUM_VERTS_Y - 1);

		vertices = ByteBuffer.allocateDirect(totalVerts * vertStride).order(ByteOrder.nativeOrder());
		ByteBuffer gIndices = ByteBuffer.allocateDirect(totalTriangles * 3 * 4).order(ByteOrder.nativeOrder());

		Vector3f tmp = new Vector3f();
		for (int i = 0; i < NUM_VERTS_X; i++) {
			for (int j = 0; j < NUM_VERTS_Y; j++) {
				float wl = 0.2f;
				// height set to zero, but can also use curved landscape, just uncomment out the code
				float height = 0.0f; // 20f * (float)Math.sin(i * wl) * (float)Math.cos(j * wl);
				
				//#ifdef FORCE_ZAXIS_UP
				//m_vertices[i+j*NUM_VERTS_X].setValue(
				//	(i-NUM_VERTS_X*0.5f)*TRIANGLE_SIZE,
				//	(j-NUM_VERTS_Y*0.5f)*TRIANGLE_SIZE,
				//	height
				//	);
				//#else
				tmp.set(
						(i - NUM_VERTS_X * 0.5f) * TRIANGLE_SIZE,
						height,
						(j - NUM_VERTS_Y * 0.5f) * TRIANGLE_SIZE);

				int index = i + j * NUM_VERTS_X;
				vertices.putFloat((index * 3 + 0) * 4, tmp.x);
				vertices.putFloat((index * 3 + 1) * 4, tmp.y);
				vertices.putFloat((index * 3 + 2) * 4, tmp.z);
				//#endif
			}
		}

		//int index=0;
		gIndices.clear();
		for (int i = 0; i < NUM_VERTS_X - 1; i++) {
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
				totalVerts, vertices, vertStride);

		boolean useQuantizedAabbCompression = true;
		groundShape = new BvhTriangleMeshShape(indexVertexArrays, useQuantizedAabbCompression);

		tr.origin.set(0, -4.5f, 0);

		//#else
//		//testing btHeightfieldTerrainShape
//		int width=128;
//		int length=128;
//		unsigned char* heightfieldData = new unsigned char[width*length];
//		{
//			for (int i=0;i<width*length;i++)
//			{
//				heightfieldData[i]=0;
//			}
//		}
//
//		char*	filename="heightfield128x128.raw";
//		FILE* heightfieldFile = fopen(filename,"r");
//		if (!heightfieldFile)
//		{
//			filename="../../heightfield128x128.raw";
//			heightfieldFile = fopen(filename,"r");
//		}
//		if (heightfieldFile)
//		{
//			int numBytes =fread(heightfieldData,1,width*length,heightfieldFile);
//			//btAssert(numBytes);
//			if (!numBytes)
//			{
//				printf("couldn't read heightfield at %s\n",filename);
//			}
//			fclose (heightfieldFile);
//		}
//
//
//		btScalar maxHeight = 20000.f;
//
//		bool useFloatDatam=false;
//		bool flipQuadEdges=false;
//
//		btHeightfieldTerrainShape* heightFieldShape = new btHeightfieldTerrainShape(width,length,heightfieldData,maxHeight,upIndex,useFloatDatam,flipQuadEdges);;
//		groundShape = heightFieldShape;
//
//		heightFieldShape->setUseDiamondSubdivision(true);
//
//		btVector3 localScaling(20,20,20);
//		localScaling[upIndex]=1.f;
//		groundShape->setLocalScaling(localScaling);
//
//		tr.setOrigin(btVector3(0,-64.5f,0));
//
		//#endif

		collisionShapes.add(groundShape);

		// create ground object
		world.localCreateRigidBody(groundShape, 0, tr);

		//#ifdef FORCE_ZAXIS_UP
		//	//   indexRightAxis = 0; 
		//	//   indexUpAxis = 2; 
		//	//   indexForwardAxis = 1; 
		//btCollisionShape* chassisShape = new btBoxShape(btVector3(1.f,2.f, 0.5f));
		//btCompoundShape* compound = new btCompoundShape();
		//btTransform localTrans;
		//localTrans.setIdentity();
		// //localTrans effectively shifts the center of mass with respect to the chassis
		//localTrans.setOrigin(btVector3(0,0,1));
		//#else
		CollisionShape chassisShape = new BoxShape(new Vector3f(1.0f, 0.5f, 2.0f));
		collisionShapes.add(chassisShape);

		CompoundShape compound = new CompoundShape();
		collisionShapes.add(compound);
		Transform localTrans = new Transform();
		localTrans.setIdentity();
		// localTrans effectively shifts the center of mass with respect to the chassis
		localTrans.origin.set(0, 1, 0);
		//#endif

		compound.addChildShape(localTrans, chassisShape);

		tr.origin.set(0, 0, 0);

		carChassis = world.localCreateRigidBody(compound, 800, tr); //chassisShape);
		//m_carChassis->setDamping(0.2,0.2);


		// create vehicle
		{
			vehicleRayCaster = new DefaultVehicleRaycaster(world);
			vehicle = new RaycastVehicle(tuning, carChassis, vehicleRayCaster);

			// never deactivate the vehicle
			carChassis.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

			world.addVehicle(vehicle);

			float connectionHeight = 1.2f;

			boolean isFrontWheel = true;

			// choose coordinate system
			vehicle.setCoordinateSystem(rightIndex, upIndex, forwardIndex);

			//#ifdef FORCE_ZAXIS_UP
			//btVector3 connectionPointCS0(CUBE_HALF_EXTENTS-(0.3*wheelWidth),2*CUBE_HALF_EXTENTS-wheelRadius, connectionHeight);
			//#else
			Vector3f connectionPointCS0 = new Vector3f(CUBE_HALF_EXTENTS - (0.3f * wheelWidth), connectionHeight, 2.0f * CUBE_HALF_EXTENTS - wheelRadius);
			//#endif

			vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);
			//#ifdef FORCE_ZAXIS_UP
			//connectionPointCS0 = btVector3(-CUBE_HALF_EXTENTS+(0.3*wheelWidth),2*CUBE_HALF_EXTENTS-wheelRadius, connectionHeight);
			//#else
			connectionPointCS0.set(-CUBE_HALF_EXTENTS + (0.3f * wheelWidth), connectionHeight, 2.0f * CUBE_HALF_EXTENTS - wheelRadius);
			//#endif

			vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);
			//#ifdef FORCE_ZAXIS_UP
			//connectionPointCS0 = btVector3(-CUBE_HALF_EXTENTS+(0.3*wheelWidth),-2*CUBE_HALF_EXTENTS+wheelRadius, connectionHeight);
			//#else
			connectionPointCS0.set(-CUBE_HALF_EXTENTS + (0.3f * wheelWidth), connectionHeight, -2.0f * CUBE_HALF_EXTENTS + wheelRadius);
			//#endif //FORCE_ZAXIS_UP
			isFrontWheel = false;
			vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);
			//#ifdef FORCE_ZAXIS_UP
			//connectionPointCS0 = btVector3(CUBE_HALF_EXTENTS-(0.3*wheelWidth),-2*CUBE_HALF_EXTENTS+wheelRadius, connectionHeight);
			//#else
			connectionPointCS0.set(CUBE_HALF_EXTENTS - (0.3f * wheelWidth), connectionHeight, -2.0f * CUBE_HALF_EXTENTS + wheelRadius);
			//#endif
			vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);

			for (int i = 0; i < vehicle.getNumWheels(); i++) {
				WheelInfo wheel = vehicle.getWheelInfo(i);
				wheel.suspensionStiffness = suspensionStiffness;
				wheel.wheelsDampingRelaxation = suspensionDamping;
				wheel.wheelsDampingCompression = suspensionCompression;
				wheel.frictionSlip = wheelFriction;
				wheel.rollInfluence = rollInfluence;
			}
		}

		setCameraDistance(26.0f);

		return world;
	}

	@Override
	protected void renderVolume(GLAutoDrawable drawable) {

		CylinderShapeX wheelShape = new CylinderShapeX(new Vector3f(wheelWidth, wheelRadius, wheelRadius));
		Vector3f wheelColor = new Vector3f(1, 0, 0);

		for (int i = 0; i < vehicle.getNumWheels(); i++) {
			// synchronize the wheels with the (interpolated) chassis worldtransform
			vehicle.updateWheelTransform(i, true);
			// draw wheels (cylinders)
			Transform trans = vehicle.getWheelInfo(i).worldTransform;
			GLShapeDrawer.draw(null, wheelShape, trans, wheelColor, getDebugMode(), gl);
		}


		{
			int wheelIndex = 2;
			vehicle.applyEngineForce(gEngineForce,wheelIndex);
			vehicle.setBrake(gBreakingForce,wheelIndex);
			wheelIndex = 3;
			vehicle.applyEngineForce(gEngineForce,wheelIndex);
			vehicle.setBrake(gBreakingForce,wheelIndex);

			wheelIndex = 0;
			vehicle.setSteeringValue(gVehicleSteering,wheelIndex);
			wheelIndex = 1;
			vehicle.setSteeringValue(gVehicleSteering,wheelIndex);
		}

		super.renderVolume(drawable);

	}
	
//	public void display(GLAutoDrawable arg) {
//		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//		poll();
//
//		float dt = getDeltaTimeMicroseconds() * 0.000001f;
//
//		if (world != null)
//		{
//			// during idle mode, just run 1 simulation step maximum
//			int maxSimSubSteps = idle ? 1 : 2;
//			if (idle)
//				dt = 1f/420f;
//
//			int numSimSteps = world.stepSimulation(dt,maxSimSubSteps);
//
//			//#define VERBOSE_FEEDBACK
//			//#ifdef VERBOSE_FEEDBACK
//			//if (!numSimSteps)
//			//	printf("Interpolated transforms\n");
//			//else
//			//{
//			//	if (numSimSteps > maxSimSubSteps)
//			//	{
//			//		//detect dropping frames
//			//		printf("Dropped (%i) simulation steps out of %i\n",numSimSteps - maxSimSubSteps,numSimSteps);
//			//	} else
//			//	{
//			//		printf("Simulated (%i) steps\n",numSimSteps);
//			//	}
//			//}
//			//#endif //VERBOSE_FEEDBACK
//		}
//
//		//#ifdef USE_QUICKPROF
//		//btProfiler::beginBlock("render");
//		//#endif //USE_QUICKPROF
//
//		renderme();
//
//		// optional but useful: debug drawing
//		if (world != null) {
//			world.debugDrawWorld();
//		}
//
//		//#ifdef USE_QUICKPROF
//		//btProfiler::endBlock("render");
//		//#endif
//	}

	
	@Override
	public void reset() {
		gVehicleSteering = 0.0f;
		Transform tr = new Transform();
		tr.setIdentity();
		carChassis.setCenterOfMassTransform(tr);
		carChassis.setLinearVelocity(new Vector3f());
		carChassis.setAngularVelocity(new Vector3f());
		world.broadphase().getOverlappingPairCache().cleanProxyFromPairs(carChassis.getBroadphaseHandle(), world().dispatcher());
		if (vehicle != null) {
			vehicle.resetSuspension();
			for (int i = 0; i < vehicle.getNumWheels(); i++) {
				// synchronize the wheels with the (interpolated) chassis worldtransform
				vehicle.updateWheelTransform(i, true);
			}
		}
	}

	@Override
	public void specialKeyboardUp(int key) {
		switch (key) {
			case KeyEvent.VK_UP -> gEngineForce = 0.0f;
			case KeyEvent.VK_DOWN -> gBreakingForce = 0.0f;
			default -> super.specialKeyboardUp(key);
		}
	}

	@Override
	public void specialKeyboard(int key) {
		//	printf("key = %i x=%i y=%i\n",key,x,y);

		switch (key) {
			case KeyEvent.VK_LEFT -> {
				gVehicleSteering += steeringIncrement;
				if (gVehicleSteering > steeringClamp) {
					gVehicleSteering = steeringClamp;
				}
			}
			case KeyEvent.VK_RIGHT -> {
				gVehicleSteering -= steeringIncrement;
				if (gVehicleSteering < -steeringClamp) {
					gVehicleSteering = -steeringClamp;
				}
			}
			case KeyEvent.VK_UP -> {
				gEngineForce = maxEngineForce;
				gBreakingForce = 0.0f;
			}
			case KeyEvent.VK_DOWN -> {
				gBreakingForce = maxBreakingForce;
				gEngineForce = 0.0f;
			}
			default -> super.specialKeyboard(key);
		}

		//glutPostRedisplay();
	}

//	@Override
//	public void updateCamera(long dtNS)
//	{
//
//		// //#define DISABLE_CAMERA 1
//		//#ifdef DISABLE_CAMERA
//		//DemoApplication::updateCamera();
//		//return;
//		//#endif //DISABLE_CAMERA
//
//		gl.glMatrixMode(gl.GL_PROJECTION);
//		gl.glLoadIdentity();
//
//		Transform chassisWorldTrans = new Transform();
//
//		// look at the vehicle
//		carChassis.getMotionState().getWorldTransform(chassisWorldTrans);
//

//		cameraTargetPosition.set(chassisWorldTrans.origin);
//
//		// interpolate the camera height
//		//#ifdef FORCE_ZAXIS_UP
//		//m_cameraPosition[2] = (15.0*m_cameraPosition[2] + m_cameraTargetPosition[2] + m_cameraHeight)/16.0;
//		//#else
//		cameraPosition.y = (15.0f*cameraPosition.y + cameraTargetPosition.y + cameraHeight) / 16.0f;
//		//#endif
//
//		Vector3f camToObject = new Vector3f();
//		camToObject.sub(cameraTargetPosition, cameraPosition);
//
//		// keep distance between min and max distance
//		float cameraDistance = camToObject.length();
//		float correctionFactor = 0.0f;
//		if (cameraDistance < minCameraDistance)
//		{
//			correctionFactor = 0.15f*(minCameraDistance-cameraDistance)/cameraDistance;
//		}
//		if (cameraDistance > maxCameraDistance)
//		{
//			correctionFactor = 0.15f*(maxCameraDistance-cameraDistance)/cameraDistance;
//		}
//		Vector3f tmp = new Vector3f();
//		tmp.scale(correctionFactor, camToObject);
//		cameraPosition.sub(tmp);
//
//		// update OpenGL camera settings
//		gl.glFrustum(-1.0, 1.0, -1.0, 1.0, 1.0, 10000.0);
//
//		gl.glMatrixMode(IGL.GL_MODELVIEW);
//		gl.glLoadIdentity();
//
//		gl.gluLookAt(cameraPosition.x,cameraPosition.y,cameraPosition.z,
//				  cameraTargetPosition.x,cameraTargetPosition.y, cameraTargetPosition.z,
//				  cameraUp.x,cameraUp.y,cameraUp.z);
//	}
	
	public static void main(String... args) {
		VehicleDemo vehicleDemo = new VehicleDemo();

		new JoglWindow3D(vehicleDemo, 800, 600);
	}
	
}