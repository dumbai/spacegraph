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

package com.bulletphysics.forklift;

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
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SliderConstraint;
import com.bulletphysics.dynamics.vehicle.*;
import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.render.DemoApplication;
import com.bulletphysics.render.GLShapeDrawer;
import com.bulletphysics.render.JoglWindow3D;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GLAutoDrawable;

import javax.vecmath.Vector3f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author jezek2
 */
public class ForkLiftDemo extends DemoApplication {

	private static final float PI = 3.14159265358979323846f;
	private static final float PI_2 = 1.57079632679489661923f;
	public static final float PI_4 = 0.785398163397448309616f;

	private static final float LIFT_EPS = 0.0000001f;
	// By default, Bullet Vehicle uses Y as up axis.
	// You can override the up axis, for example Z-axis up. Enable this define to see how to:
	////#define FORCE_ZAXIS_UP 1

	//#ifdef FORCE_ZAXIS_UP
	//private int rightIndex = 0;
	//private int upIndex = 2;
	//private int forwardIndex = 1;
	//private btVector3 wheelDirectionCS0(0,0,-1);
	//private btVector3 wheelAxleCS(1,0,0);
	//#else
	private final int rightIndex = 0;
	private final int upIndex = 1;
	private final int forwardIndex = 2;
	private final Vector3f wheelDirectionCS0 = new Vector3f(0, -1, 0);
	private final Vector3f wheelAxleCS = new Vector3f(-1, 0, 0);
	//#endif

	private static final int maxProxies = 32766;
	private static final int maxOverlap = 65535;

	// RaycastVehicle is the interface for the constraint that implements the raycast vehicle
	// notice that for higher-quality slow-moving vehicles, another approach might be better
	// implementing explicit hinged-wheel constraints with cylinder collision, rather then raycasts
	private float gEngineForce = 0.0f;

	private final float defaultBreakingForce = 10.0f;
	private float gBreakingForce = 10.0f;

	private final float maxEngineForce = 1000.0f;//this should be engine/velocity dependent
	private float maxBreakingForce = 100.0f;

	private float gVehicleSteering = 0.0f;
	private final float steeringIncrement = 0.04f;
	private final float steeringClamp = 0.3f;
	private final float wheelRadius = 0.5f;
	private final float wheelWidth = 0.4f;
	private final float wheelFriction = 1000;//1e30f;
	private final float suspensionStiffness = 20.0f;
	private final float suspensionDamping = 2.3f;
	private final float suspensionCompression = 4.4f;
	private final float rollInfluence = 0.1f;//1.0f;

	private final float suspensionRestLength = 0.6f;

	private static final float CUBE_HALF_EXTENTS = 1.0f;

	////////////////////////////////////////////////////////////////////////////


	private RigidBody carChassis;

	//----------------------------

	private RigidBody liftBody;
	private final Vector3f liftStartPos = new Vector3f();
	private HingeConstraint liftHinge;

	private RigidBody forkBody;
	private final Vector3f forkStartPos = new Vector3f();
	private SliderConstraint forkSlider;

	private RigidBody loadBody;
	private final Vector3f loadStartPos = new Vector3f();

	private boolean useDefaultCamera;

	//----------------------------

	private BroadphaseInterface overlappingPairCache;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver constraintSolver;
	private DefaultCollisionConfiguration collisionConfiguration;
	private TriangleIndexVertexArray indexVertexArrays;
	private ByteBuffer vertices;

	private final VehicleTuning tuning = new VehicleTuning();
	private VehicleRaycaster vehicleRayCaster;
	private RaycastVehicle vehicle;

	private final float cameraHeight = 4.0f;
	private final float minCameraDistance = 3.0f;
	private final float maxCameraDistance = 10.0f;

	private ForkLiftDemo() {
		super();
		useDefaultCamera = false;
//		cameraPosition.set(30, 30, 30);
	}

	private void lockLiftHinge() {
		float hingeAngle = liftHinge.getHingeAngle();
		float lowLim = liftHinge.getLowerLimit();
		float hiLim = liftHinge.getUpperLimit();
		liftHinge.enableAngularMotor(false, 0, 0);
		if (hingeAngle < lowLim) {
			liftHinge.setLimit(lowLim, lowLim + LIFT_EPS);
		}
		else if (hingeAngle > hiLim) {
			liftHinge.setLimit(hiLim - LIFT_EPS, hiLim);
		}
		else {
			liftHinge.setLimit(hingeAngle - LIFT_EPS, hingeAngle + LIFT_EPS);
		}
	}

	private void lockForkSlider() {
		float linDepth = forkSlider.getLinearPos();
		float lowLim = forkSlider.getLowerLinLimit();
		float hiLim = forkSlider.getUpperLinLimit();
		forkSlider.setPoweredLinMotor(false);
		if (linDepth <= lowLim) {
			forkSlider.setLowerLinLimit(lowLim);
			forkSlider.setUpperLinLimit(lowLim);
		}
		else if (linDepth > hiLim) {
			forkSlider.setLowerLinLimit(hiLim);
			forkSlider.setUpperLinLimit(hiLim);
		}
		else {
			forkSlider.setLowerLinLimit(linDepth);
			forkSlider.setUpperLinLimit(linDepth);
		}
	}

	@Override
	public void display(GLAutoDrawable d) {
		if (gl == null) return;
		gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
//		poll();
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

		float dt = getDeltaTimeMicroseconds() * 0.000001f;

		if (world != null)
		{
			// during idle mode, just run 1 simulation step maximum
			int maxSimSubSteps = idle ? 1 : 2;
			if (idle)
				dt = 1.0f / 420.0f;

			int numSimSteps = world.next(dt,maxSimSubSteps);

			//#define VERBOSE_FEEDBACK
			//#ifdef VERBOSE_FEEDBACK
			//if (!numSimSteps)
			//	printf("Interpolated transforms\n");
			//else
			//{
			//	if (numSimSteps > maxSimSubSteps)
			//	{
			//		//detect dropping frames
			//		printf("Dropped (%i) simulation steps out of %i\n",numSimSteps - maxSimSubSteps,numSimSteps);
			//	} else
			//	{
			//		printf("Simulated (%i) steps\n",numSimSteps);
			//	}
			//}
			//#endif //VERBOSE_FEEDBACK
		}

		//#ifdef USE_QUICKPROF
		//btProfiler::beginBlock("render");
		//#endif //USE_QUICKPROF

		super.display(d);
		//renderme();

		// optional but useful: debug drawing
		if (world != null) {
			world.debugDrawWorld();
		}

		//#ifdef USE_QUICKPROF
		//btProfiler::endBlock("render");
		//#endif
	}

	@Override
	public void reset() {
		gVehicleSteering = 0.0f;
		Transform tr = new Transform();
		tr.setIdentity();
		carChassis.setCenterOfMassTransform(tr);
		carChassis.setLinearVelocity(new Vector3f(0,0,0));
		carChassis.setAngularVelocity(new Vector3f(0,0,0));
		world.broadphase().getOverlappingPairCache().cleanProxyFromPairs(carChassis.getBroadphaseHandle(), world().dispatcher());
		if (vehicle != null)
		{
			vehicle.resetSuspension();
			for (int i=0;i<vehicle.getNumWheels();i++)
			{
				// synchronize the wheels with the (interpolated) chassis worldtransform
				vehicle.updateWheelTransform(i,true);
			}
		}
		Transform liftTrans = new Transform();
		liftTrans.setIdentity();
		liftTrans.origin.set(liftStartPos);
		liftBody.activate();
		liftBody.setCenterOfMassTransform(liftTrans);
		liftBody.setLinearVelocity(new Vector3f(0,0,0));
		liftBody.setAngularVelocity(new Vector3f(0,0,0));

		Transform forkTrans = new Transform();
		forkTrans.setIdentity();
		forkTrans.origin.set(forkStartPos);
		forkBody.activate();
		forkBody.setCenterOfMassTransform(forkTrans);
		forkBody.setLinearVelocity(new Vector3f(0,0,0));
		forkBody.setAngularVelocity(new Vector3f(0,0,0));

		liftHinge.setLimit(-LIFT_EPS, LIFT_EPS);
		liftHinge.enableAngularMotor(false, 0, 0);

		forkSlider.setLowerLinLimit(0.1f);
		forkSlider.setUpperLinLimit(0.1f);
		forkSlider.setPoweredLinMotor(false);

		Transform loadTrans = new Transform();
		loadTrans.setIdentity();
		loadTrans.origin.set(loadStartPos);
		loadBody.activate();
		loadBody.setCenterOfMassTransform(loadTrans);
		loadBody.setLinearVelocity(new Vector3f(0,0,0));
		loadBody.setAngularVelocity(new Vector3f(0,0,0));
	}



//	///a very basic camera following the vehicle
//	@Override
//	public void updateCamera(long dtNS) {
//		if (gl == null) return;
//		if(useDefaultCamera) {
//			super.updateCamera(dtNS);
//			return;
//		}
//
//		// //#define DISABLE_CAMERA 1
//		//#ifdef DISABLE_CAMERA
//		//DemoApplication::updateCamera();
//		//return;
//		//#endif //DISABLE_CAMERA
//
//		Transform chassisWorldTrans = new Transform();
//
//		// look at the vehicle
//		carChassis.getMotionState().getWorldTransform(chassisWorldTrans);
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

	@Override
	public void keyboardCallback(char key) {

		switch (key) {
			case 'a' -> {
				liftHinge.setLimit(-PI / 16.0f, PI / 8.0f);
				liftHinge.enableAngularMotor(true, -0.1f, 10.0f);
			}
			case 'd' -> {
				liftHinge.setLimit(-PI / 16.0f, PI / 8.0f);
				liftHinge.enableAngularMotor(true, 0.1f, 10.0f);
			}
			case 's' -> {
				forkSlider.setLowerLinLimit(0.1f);
				forkSlider.setUpperLinLimit(3.9f);
				forkSlider.setPoweredLinMotor(true);
				forkSlider.setMaxLinMotorForce(10.0f);
				forkSlider.setTargetLinMotorVelocity(1.0f);
			}
			case 'w' -> {
				forkSlider.setLowerLinLimit(0.1f);
				forkSlider.setUpperLinLimit(3.9f);
				forkSlider.setPoweredLinMotor(true);
				forkSlider.setMaxLinMotorForce(10.0f);
				forkSlider.setTargetLinMotorVelocity(-1.0f);
			}
			default -> super.keyboardCallback(key);
		}
	}


	@Override
	public void keyboardCallbackUp(char key) {
		switch (key) {
		case 'a':
			break;
		case 'd':
			lockLiftHinge();
			break;
		case 's':
		case 'w':
			lockForkSlider();
			break;
		default:
			super.keyboardCallbackUp(key);
			break;
		}
	}

	@Override
	public void specialKeyboard(int key) {
		if (key == KeyEvent.VK_END) {
			return;
		}

		switch (key) {
			case KeyEvent.VK_J -> {
				liftHinge.setLimit(-PI / 16.0f, PI / 8.0f);
				liftHinge.enableAngularMotor(true, -0.1f, 10.0f);
			}
			case KeyEvent.VK_L -> {
				liftHinge.setLimit(-PI / 16.0f, PI / 8.0f);
				liftHinge.enableAngularMotor(true, 0.1f, 10.0f);
			}
			case KeyEvent.VK_I -> {
				forkSlider.setLowerLinLimit(0.1f);
				forkSlider.setUpperLinLimit(3.9f);
				forkSlider.setPoweredLinMotor(true);
				forkSlider.setMaxLinMotorForce(10.0f);
				forkSlider.setTargetLinMotorVelocity(1.0f);
			}
			case KeyEvent.VK_K -> {
				forkSlider.setLowerLinLimit(0.1f);
				forkSlider.setUpperLinLimit(3.9f);
				forkSlider.setPoweredLinMotor(true);
				forkSlider.setMaxLinMotorForce(10.0f);
				forkSlider.setTargetLinMotorVelocity(-1.0f);
			}
			default -> super.specialKeyboard(key);
		}

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
				gEngineForce = -maxEngineForce;
				gBreakingForce = 0.0f;
			}
			case KeyEvent.VK_F5 -> useDefaultCamera = !useDefaultCamera;
			default -> super.specialKeyboard(key);
		}


		//glutPostRedisplay();
	}

	@Override
	public void specialKeyboardUp(int key) {
		switch (key) {
			case KeyEvent.VK_UP, KeyEvent.VK_DOWN -> {
				lockForkSlider();
				gEngineForce = 0.0f;
				gBreakingForce = defaultBreakingForce;
			}
			case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> lockLiftHinge();
			default -> super.specialKeyboard(key);
		}
	}


	@Override
	protected void renderVolume(GLAutoDrawable drawable) {
		super.renderVolume(drawable);


		CylinderShapeX wheelShape = new CylinderShapeX(new Vector3f(wheelWidth,wheelRadius,wheelRadius));
		Vector3f wheelColor = new Vector3f(1,0,0);

		Vector3f worldBoundsMin = new Vector3f(),worldBoundsMax = new Vector3f();
		world().broadphase().getBroadphaseAabb(worldBoundsMin,worldBoundsMax);

		for (int i=0;i<vehicle.getNumWheels();i++)
		{
			// synchronize the wheels with the (interpolated) chassis worldtransform
			vehicle.updateWheelTransform(i,true);
			// draw wheels (cylinders)
			Transform trans = vehicle.getWheelInfo(i).worldTransform;
			GLShapeDrawer.draw(null, wheelShape, trans, wheelColor, getDebugMode(), gl  /*,worldBoundsMin,worldBoundsMax*/);
		}
	}

	@Override
	protected void renderHUD() {
		if ((getDebugMode() & DebugDrawModes.NO_HELP_TEXT) == 0) {
			gl.glColor3f(0, 0, 0);
			drawString("keys a/d - rotate lift", 350, 20, TEXT_COLOR);
			drawString("keys w/s - move fork up/down", 350, 40, TEXT_COLOR);
			drawString("F5 - toggle camera mode", 350, 60, TEXT_COLOR);
		}
	}

	@Override
	public DynamicsWorld physics() {
		//#ifdef FORCE_ZAXIS_UP
		//cameraUp.set(0,0,1);
		//forwardAxis = 1;
		//#endif

		CollisionShape groundShape = new BoxShape(new Vector3f(50,3,50));
		//collisionShapes.add(groundShape);
		collisionConfiguration = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldMin = new Vector3f(-1000,-1000,-1000);
		Vector3f worldMax = new Vector3f(1000,1000,1000);
		//overlappingPairCache = new AxisSweep3(worldMin,worldMax);
		overlappingPairCache = new DbvtBroadphase();
		constraintSolver = new SequentialImpulseConstraintSolver();
		DynamicsWorld world = new DiscreteDynamicsWorld(dispatcher,overlappingPairCache,constraintSolver,collisionConfiguration);
		//#ifdef FORCE_ZAXIS_UP
		//m_dynamicsWorld->setGravity(btVector3(0,0,-10));
		//#endif

		//m_dynamicsWorld->setGravity(btVector3(0,0,0));
		Transform tr = new Transform();
		tr.setIdentity();

		// either use heightfield or triangle mesh
		//#define  USE_TRIMESH_GROUND 1
		//#ifdef USE_TRIMESH_GROUND
		final float TRIANGLE_SIZE= 20.0f;

		// create a triangle-mesh ground
		int vertStride = 3*4;
		int indexStride = 3*4;

		final int NUM_VERTS_X = 20;
		final int NUM_VERTS_Y = 20;
		final int totalVerts = NUM_VERTS_X*NUM_VERTS_Y;

		final int totalTriangles = 2*(NUM_VERTS_X-1)*(NUM_VERTS_Y-1);

		vertices = ByteBuffer.allocateDirect(totalVerts*vertStride).order(ByteOrder.nativeOrder());
		ByteBuffer gIndices = ByteBuffer.allocateDirect(totalTriangles*3*4).order(ByteOrder.nativeOrder());

		for (int i=0;i<NUM_VERTS_X;i++)
		{
			for (int j=0;j<NUM_VERTS_Y;j++)
			{
				float wl = 0.2f;
				// height set to zero, but can also use curved landscape, just uncomment out the code
				float height = 0.0f;//20.f*sinf(float(i)*wl)*cosf(float(j)*wl);
				//#ifdef FORCE_ZAXIS_UP
				//m_vertices[i+j*NUM_VERTS_X].setValue(
				//	(i-NUM_VERTS_X*0.5f)*TRIANGLE_SIZE,
				//	(j-NUM_VERTS_Y*0.5f)*TRIANGLE_SIZE,
				//	height
				//	);
				//#else
				int idx = (i+j*NUM_VERTS_X)*3*4;
				vertices.putFloat(idx+0*4, (i-NUM_VERTS_X*0.5f)*TRIANGLE_SIZE);
				vertices.putFloat(idx+1*4, height);
				vertices.putFloat(idx+2*4, (j-NUM_VERTS_Y*0.5f)*TRIANGLE_SIZE);
				//#endif
			}
		}

		//int index=0;
		for (int i=0;i<NUM_VERTS_X-1;i++)
		{
			for (int j=0;j<NUM_VERTS_Y-1;j++)
			{
				gIndices.putInt(j*NUM_VERTS_X+i);
				gIndices.putInt(j*NUM_VERTS_X+i+1);
				gIndices.putInt((j+1)*NUM_VERTS_X+i+1);

				gIndices.putInt(j*NUM_VERTS_X+i);
				gIndices.putInt((j+1)*NUM_VERTS_X+i+1);
				gIndices.putInt((j+1)*NUM_VERTS_X+i);
			}
		}
		gIndices.flip();

		indexVertexArrays = new TriangleIndexVertexArray(totalTriangles,
			gIndices,
			indexStride,
			totalVerts,vertices,vertStride);

		boolean useQuantizedAabbCompression = true;
		groundShape = new BvhTriangleMeshShape(indexVertexArrays,useQuantizedAabbCompression);

		tr.origin.set(0,-4.5f,0);
		//#else
		////testing btHeightfieldTerrainShape
		//int width=128;
		//int length=128;
		//unsigned char* heightfieldData = new unsigned char[width*length];
		//{
		//	for (int i=0;i<width*length;i++)
		//	{
		//		heightfieldData[i]=0;
		//	}
		//}
		//
		//char*	filename="heightfield128x128.raw";
		//FILE* heightfieldFile = fopen(filename,"r");
		//if (!heightfieldFile)
		//{
		//	filename="../../heightfield128x128.raw";
		//	heightfieldFile = fopen(filename,"r");
		//}
		//if (heightfieldFile)
		//{
		//	int numBytes =fread(heightfieldData,1,width*length,heightfieldFile);
		//	//btAssert(numBytes);
		//	if (!numBytes)
		//	{
		//		printf("couldn't read heightfield at %s\n",filename);
		//	}
		//	fclose (heightfieldFile);
		//}
		//
		//
		//btScalar maxHeight = 20000.f;
		//
		//bool useFloatDatam=false;
		//bool flipQuadEdges=false;
		//
		//btHeightfieldTerrainShape* heightFieldShape = new btHeightfieldTerrainShape(width,length,heightfieldData,maxHeight,upIndex,useFloatDatam,flipQuadEdges);;
		//groundShape = heightFieldShape;
		//
		//heightFieldShape->setUseDiamondSubdivision(true);
		//
		//btVector3 localScaling(20,20,20);
		//localScaling[upIndex]=1.f;
		//groundShape->setLocalScaling(localScaling);
		//
		//tr.setOrigin(btVector3(0,-64.5f,0));
		//
		//#endif //

		//collisionShapes.add(groundShape);

		// create ground object
		world.localCreateRigidBody(groundShape, 0,tr);

		//#ifdef FORCE_ZAXIS_UP
		////   indexRightAxis = 0;
		////   indexUpAxis = 2;
		////   indexForwardAxis = 1;
		//btCollisionShape* chassisShape = new btBoxShape(btVector3(1.f,2.f, 0.5f));
		//btCompoundShape* compound = new btCompoundShape();
		//btTransform localTrans;
		//localTrans.setIdentity();
		////localTrans effectively shifts the center of mass with respect to the chassis
		//localTrans.setOrigin(btVector3(0,0,1));
		//#else
		CollisionShape chassisShape = new BoxShape(new Vector3f(1.0f,0.5f, 2.0f));
		//collisionShapes.add(chassisShape);

		CompoundShape compound = new CompoundShape();
		//collisionShapes.add(compound);
		Transform localTrans = new Transform();
		localTrans.setIdentity();
		// localTrans effectively shifts the center of mass with respect to the chassis
		localTrans.origin.set(0,1,0);
		//#endif

		compound.addChildShape(localTrans,chassisShape);

		{
			CollisionShape suppShape = new BoxShape(new Vector3f(0.5f,0.1f,0.5f));
			//collisionShapes.add(chassisShape);
			Transform suppLocalTrans = new Transform();
			suppLocalTrans.setIdentity();
			// localTrans effectively shifts the center of mass with respect to the chassis
			suppLocalTrans.origin.set(0.0f,1.0f,2.5f);
			compound.addChildShape(suppLocalTrans, suppShape);
		}

		tr.origin.set(0, 0.0f,0);

		carChassis = world.localCreateRigidBody(compound, 800,tr);//chassisShape);
		//m_carChassis->setDamping(0.2,0.2);


		{
			CollisionShape liftShape = new BoxShape(new Vector3f(0.5f,2.0f,0.05f));
			//collisionShapes.add(liftShape);
			Transform liftTrans = new Transform();
			liftStartPos.set(0.0f, 2.5f, 3.05f);
			liftTrans.setIdentity();
			liftTrans.origin.set(liftStartPos);
			liftBody = world.localCreateRigidBody(liftShape, 10,liftTrans);

			Transform localA = new Transform(), localB = new Transform();
			localA.setIdentity();
			localB.setIdentity();
			MatrixUtil.setEulerZYX(localA.basis, 0, PI_2, 0);
			localA.origin.set(0.0f, 1.0f, 3.05f);
			MatrixUtil.setEulerZYX(localB.basis, 0, PI_2, 0);
			localB.origin.set(0.0f, -1.5f, -0.05f);
			liftHinge = new HingeConstraint(carChassis,liftBody, localA, localB);
			liftHinge.setLimit(-LIFT_EPS, LIFT_EPS);
			world.addConstraint(liftHinge, true);

			CollisionShape forkShapeA = new BoxShape(new Vector3f(1.0f,0.1f,0.1f));
			//collisionShapes.add(forkShapeA);
			CompoundShape forkCompound = new CompoundShape();
			//collisionShapes.add(forkCompound);
			Transform forkLocalTrans = new Transform();
			forkLocalTrans.setIdentity();
			forkCompound.addChildShape(forkLocalTrans, forkShapeA);

			CollisionShape forkShapeB = new BoxShape(new Vector3f(0.1f,0.02f,0.6f));
			//collisionShapes.add(forkShapeB);
			forkLocalTrans.setIdentity();
			forkLocalTrans.origin.set(-0.9f, -0.08f, 0.7f);
			forkCompound.addChildShape(forkLocalTrans, forkShapeB);

			CollisionShape forkShapeC = new BoxShape(new Vector3f(0.1f,0.02f,0.6f));
			//collisionShapes.add(forkShapeC);
			forkLocalTrans.setIdentity();
			forkLocalTrans.origin.set(0.9f, -0.08f, 0.7f);
			forkCompound.addChildShape(forkLocalTrans, forkShapeC);

			Transform forkTrans = new Transform();
			forkStartPos.set(0.0f, 0.6f, 3.2f);
			forkTrans.setIdentity();
			forkTrans.origin.set(forkStartPos);
			forkBody = world.localCreateRigidBody(forkCompound, 5, forkTrans);

			localA.setIdentity();
			localB.setIdentity();
			MatrixUtil.setEulerZYX(localA.basis, 0, 0, PI_2);
			localA.origin.set(0.0f, -1.9f, 0.05f);
			MatrixUtil.setEulerZYX(localB.basis, 0, 0, PI_2);
			localB.origin.set(0.0f, 0.0f, -0.1f);
			forkSlider = new SliderConstraint(liftBody, forkBody, localA, localB, true);
			forkSlider.setLowerLinLimit(0.1f);
			forkSlider.setUpperLinLimit(0.1f);
			forkSlider.setLowerAngLimit(-LIFT_EPS);
			forkSlider.setUpperAngLimit(LIFT_EPS);
			world.addConstraint(forkSlider, true);

			CompoundShape loadCompound = new CompoundShape();
			//collisionShapes.add(loadCompound);
			CollisionShape loadShapeA = new BoxShape(new Vector3f(2.0f,0.5f,0.5f));
			//collisionShapes.add(loadShapeA);
			Transform loadTrans = new Transform();
			loadTrans.setIdentity();
			loadCompound.addChildShape(loadTrans, loadShapeA);
			CollisionShape loadShapeB = new BoxShape(new Vector3f(0.1f,1.0f,1.0f));
			//collisionShapes.add(loadShapeB);
			loadTrans.setIdentity();
			loadTrans.origin.set(2.1f, 0.0f, 0.0f);
			loadCompound.addChildShape(loadTrans, loadShapeB);
			CollisionShape loadShapeC = new BoxShape(new Vector3f(0.1f,1.0f,1.0f));
			//collisionShapes.add(loadShapeC);
			loadTrans.setIdentity();
			loadTrans.origin.set(-2.1f, 0.0f, 0.0f);
			loadCompound.addChildShape(loadTrans, loadShapeC);
			loadTrans.setIdentity();
			loadStartPos.set(0.0f, -3.5f, 7.0f);
			loadTrans.origin.set(loadStartPos);
			loadBody  = world.localCreateRigidBody(loadCompound, 4, loadTrans);
		}



		// create vehicle
		{

			vehicleRayCaster = new DefaultVehicleRaycaster(world);
			vehicle = new RaycastVehicle(tuning,carChassis,vehicleRayCaster);

			// never deactivate the vehicle
			carChassis.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

			world.addVehicle(vehicle);

			float connectionHeight = 1.2f;

			boolean isFrontWheel=true;

			// choose coordinate system
			vehicle.setCoordinateSystem(rightIndex,upIndex,forwardIndex);

			//#ifdef FORCE_ZAXIS_UP
			//btVector3 connectionPointCS0(CUBE_HALF_EXTENTS-(0.3*wheelWidth),2*CUBE_HALF_EXTENTS-wheelRadius, connectionHeight);
			//#else
			Vector3f connectionPointCS0 = new Vector3f(CUBE_HALF_EXTENTS-(0.3f*wheelWidth),connectionHeight, 2.0f *CUBE_HALF_EXTENTS-wheelRadius);
			//#endif

			vehicle.addWheel(connectionPointCS0,wheelDirectionCS0,wheelAxleCS,suspensionRestLength,wheelRadius,tuning,isFrontWheel);
			//#ifdef FORCE_ZAXIS_UP
			//connectionPointCS0 = btVector3(-CUBE_HALF_EXTENTS+(0.3*wheelWidth),2*CUBE_HALF_EXTENTS-wheelRadius, connectionHeight);
			//#else
			connectionPointCS0 = new Vector3f(-CUBE_HALF_EXTENTS+(0.3f*wheelWidth),connectionHeight, 2.0f *CUBE_HALF_EXTENTS-wheelRadius);
			//#endif

			vehicle.addWheel(connectionPointCS0,wheelDirectionCS0,wheelAxleCS,suspensionRestLength,wheelRadius,tuning,isFrontWheel);
			//#ifdef FORCE_ZAXIS_UP
			//connectionPointCS0 = btVector3(-CUBE_HALF_EXTENTS+(0.3*wheelWidth),-2*CUBE_HALF_EXTENTS+wheelRadius, connectionHeight);
			//#else
			connectionPointCS0 = new Vector3f(-CUBE_HALF_EXTENTS+(0.3f*wheelWidth),connectionHeight,-2.0f *CUBE_HALF_EXTENTS+wheelRadius);
			//#endif //FORCE_ZAXIS_UP
			isFrontWheel = false;
			vehicle.addWheel(connectionPointCS0,wheelDirectionCS0,wheelAxleCS,suspensionRestLength,wheelRadius,tuning,isFrontWheel);
			//#ifdef FORCE_ZAXIS_UP
			//connectionPointCS0 = btVector3(CUBE_HALF_EXTENTS-(0.3*wheelWidth),-2*CUBE_HALF_EXTENTS+wheelRadius, connectionHeight);
			//#else
			connectionPointCS0 = new Vector3f(CUBE_HALF_EXTENTS-(0.3f*wheelWidth),connectionHeight,-2.0f *CUBE_HALF_EXTENTS+wheelRadius);
			//#endif
			vehicle.addWheel(connectionPointCS0,wheelDirectionCS0,wheelAxleCS,suspensionRestLength,wheelRadius,tuning,isFrontWheel);

			for (int i=0;i<vehicle.getNumWheels();i++)
			{
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

	public static void main(String... args) {
		ForkLiftDemo forkLiftDemo = new ForkLiftDemo();

		new JoglWindow3D(forkLiftDemo, 800, 600);
	}

}