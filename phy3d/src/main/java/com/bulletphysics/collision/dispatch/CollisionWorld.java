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

package com.bulletphysics.collision.dispatch;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.*;
import com.bulletphysics.collision.narrowphase.*;
import com.bulletphysics.collision.narrowphase.ConvexCast.CastResult;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.convex.ConcaveShape;
import com.bulletphysics.collision.shapes.convex.ConvexShape;
import com.bulletphysics.collision.shapes.mesh.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.mesh.TriangleMeshShape;
import com.bulletphysics.collision.shapes.simple.SphereShape;
import com.bulletphysics.linearmath.*;
import com.bulletphysics.util.ObjectArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import static com.bulletphysics.BulletGlobals.FLT_EPSILON;

/**
 * CollisionWorld is interface and container for the collision detection.
 * 
 * @author jezek2
 */
public class CollisionWorld {

	//protected final BulletStack stack = BulletStack.get();
	
	protected final ObjectArrayList<CollisionObject> collisionObjects = new ObjectArrayList<>();
	protected final Dispatcher dispatcher;
	private final DispatcherInfo dispatchInfo = new DispatcherInfo();
	//protected btStackAlloc*	m_stackAlloc;
	private final BroadphaseInterface broadphase;
	protected IDebugDraw debugDrawer;
	
	/**
	 * This constructor doesn't own the dispatcher and paircache/broadphase.
	 */
	public CollisionWorld(Dispatcher dispatcher, BroadphaseInterface broadphase, CollisionConfiguration collisionConfiguration) {
		this.dispatcher = dispatcher;
		this.broadphase = broadphase;
	}
	
	public void destroy() {
		// clean up remaining objects
		for (int i = 0; i < collisionObjects.size(); i++) {
			CollisionObject collisionObject = collisionObjects.get(i);

			BroadphaseProxy bp = collisionObject.getBroadphaseHandle();
			if (bp != null) {
				//
				// only clear the cached algorithms
				//
				broadphase().getOverlappingPairCache().cleanProxyFromPairs(bp, dispatcher);
				broadphase().destroyProxy(bp, dispatcher);
			}
		}
	}
	
	protected void addCollisionObject(CollisionObject collisionObject) {
		addCollisionObject(collisionObject, CollisionFilterGroups.DEFAULT_FILTER, CollisionFilterGroups.ALL_FILTER);
	}

	public void addCollisionObject(CollisionObject collisionObject, short collisionFilterGroup, short collisionFilterMask) {
		// check that the object isn't already added
		assert (!collisionObjects.contains(collisionObject));

		collisionObjects.add(collisionObject);

		// calculate new AABB
		// TODO: check if it's overwritten or not
		Transform trans = collisionObject.getWorldTransform(new Transform());

		Vector3f minAabb = new Vector3f();
		Vector3f maxAabb = new Vector3f();
		collisionObject.getCollisionShape().getAabb(trans, minAabb, maxAabb);

		BroadphaseNativeType type = collisionObject.getCollisionShape().getShapeType();
		collisionObject.setBroadphaseHandle(broadphase().createProxy(
				minAabb,
				maxAabb,
				type,
				collisionObject,
				collisionFilterGroup,
				collisionFilterMask,
				dispatcher, null));
	}

	protected void performDiscreteCollisionDetection() {
		BulletStats.pushProfile("performDiscreteCollisionDetection");
		try {
			//DispatcherInfo dispatchInfo = getDispatchInfo();

			updateAabbs();

			BulletStats.pushProfile("calculateOverlappingPairs");
			try {
				broadphase.calculateOverlappingPairs(dispatcher);
			}
			finally {
				BulletStats.popProfile();
			}

			Dispatcher dispatcher = dispatcher();
			{
				BulletStats.pushProfile("dispatchAllCollisionPairs");
				try {
					if (dispatcher != null) {
						dispatcher.dispatchAllCollisionPairs(broadphase.getOverlappingPairCache(), dispatchInfo, this.dispatcher);
					}
				}
				finally {
					BulletStats.popProfile();
				}
			}
		}
		finally {
			BulletStats.popProfile();
		}
	}
	
	public void removeCollisionObject(CollisionObject collisionObject) {
		//bool removeFromBroadphase = false;

		{
			BroadphaseProxy bp = collisionObject.getBroadphaseHandle();
			if (bp != null) {
				//
				// only clear the cached algorithms
				//
				broadphase().getOverlappingPairCache().cleanProxyFromPairs(bp, dispatcher);
				broadphase().destroyProxy(bp, dispatcher);
				collisionObject.setBroadphaseHandle(null);
			}
		}

		//swapremove
		collisionObjects.remove(collisionObject);
	}

	
	public BroadphaseInterface broadphase() {
		return broadphase;
	}
	
	public OverlappingPairCache getPairCache() {
		return broadphase.getOverlappingPairCache();
	}

	public Dispatcher dispatcher() {
		return dispatcher;
	}

	public DispatcherInfo getDispatchInfo() {
		return dispatchInfo;
	}
	
	private static boolean updateAabbs_reportMe = true;

	// JAVA NOTE: ported from 2.74, missing contact threshold stuff
	private void updateSingleAabb(CollisionObject colObj) {

		Transform tmpTrans = new Transform();

		Vector3f minAabb = new Vector3f(), maxAabb = new Vector3f();
		colObj.getCollisionShape().getAabb(colObj.getWorldTransform(tmpTrans), minAabb, maxAabb);

		// need to increase the aabb for contact thresholds
		Vector3f contactThreshold = new Vector3f();
		contactThreshold.set(BulletGlobals.getContactBreakingThreshold(), BulletGlobals.getContactBreakingThreshold(), BulletGlobals.getContactBreakingThreshold());

		minAabb.sub(contactThreshold);
		maxAabb.add(contactThreshold);

		// moving objects should be moderately sized, probably something wrong if not
		Vector3f tmp = new Vector3f();
		tmp.sub(maxAabb, minAabb); // TODO: optimize
		if (colObj.isStaticObject() || (tmp.lengthSquared() < 1.0f /FLT_EPSILON /*1e12f*/)) {
			broadphase.setAabb(colObj.getBroadphaseHandle(), minAabb, maxAabb, dispatcher);
		} else {
			// something went wrong, investigate
			// this assert is unwanted in 3D modelers (danger of loosing work)
			colObj.setActivationState(CollisionObject.DISABLE_SIMULATION);

			if (updateAabbs_reportMe && debugDrawer != null) {
				updateAabbs_reportMe = false;
				debugDrawer.reportErrorWarning("Overflow in AABB, object removed from simulation");
				debugDrawer.reportErrorWarning("If you can reproduce this, please email bugs@continuousphysics.com\n");
				debugDrawer.reportErrorWarning("Please include above information, your Platform, version of OS.\n");
				debugDrawer.reportErrorWarning("Thanks.\n");
			}
		}
	}

	protected void updateAabbs() {
		BulletStats.pushProfile("updateAabbs");
		try {
			int n = collisionObjects.size();
			for (int i = 0; i< n; i++) {
				CollisionObject colObj = collisionObjects.get(i);
				if (colObj.isActive())
					updateSingleAabb(colObj); // only update aabb of active objects
			}
		} finally {
			BulletStats.popProfile();
		}
	}

	public IDebugDraw getDebugDrawer() {
		return debugDrawer;
	}

	public void setDebugDrawer(IDebugDraw debugDrawer) {
		this.debugDrawer = debugDrawer;
	}
	
	public int getNumCollisionObjects() {
		return collisionObjects.size();
	}

	// TODO
	public static void rayTestSingle(Transform rayFromTrans, Transform rayToTrans,
			CollisionObject collisionObject,
			CollisionShape collisionShape,
			Transform colObjWorldTransform,
			RayResultCallback resultCallback, double eps) {


		if (collisionShape.isConvex()) {
			CastResult castResult = new CastResult();
			castResult.fraction = resultCallback.closestHitFraction;

			ConvexShape convexShape = (ConvexShape) collisionShape;
			VoronoiSimplexSolver simplexSolver = new VoronoiSimplexSolver();

			//#define USE_SUBSIMPLEX_CONVEX_CAST 1
			//#ifdef USE_SUBSIMPLEX_CONVEX_CAST
			SphereShape pointShape = new SphereShape(0.0f);
			pointShape.setMargin(0.0f);
			SubsimplexConvexCast convexCaster = new SubsimplexConvexCast(pointShape, convexShape, simplexSolver);
			//#else
			//btGjkConvexCast	convexCaster(castShape,convexShape,&simplexSolver);
			//btContinuousConvexCollision convexCaster(castShape,convexShape,&simplexSolver,0);
			//#endif //#USE_SUBSIMPLEX_CONVEX_CAST

			if (convexCaster.timeOfImpact(rayFromTrans, rayToTrans, colObjWorldTransform, colObjWorldTransform, castResult)) {
				//add hit
				if (castResult.fraction < resultCallback.closestHitFraction && castResult.normal.lengthSquared() > eps * eps) {
					//#ifdef USE_SUBSIMPLEX_CONVEX_CAST
					//rotate normal into worldspace
					rayFromTrans.basis.transform(castResult.normal);
					//#endif //USE_SUBSIMPLEX_CONVEX_CAST

					castResult.normal.normalize();

					resultCallback.addSingleResult(new LocalRayResult(
							collisionObject,
							null,
							castResult.normal,
							castResult.fraction), true);
				}
			}
		}
		else {
			if (collisionShape.isConcave()) {
				if (collisionShape.getShapeType() == BroadphaseNativeType.TRIANGLE_MESH_SHAPE_PROXYTYPE) {
					// optimized version for BvhTriangleMeshShape
					BvhTriangleMeshShape triangleMesh = (BvhTriangleMeshShape)collisionShape;
					Transform worldTocollisionObject = new Transform();
					worldTocollisionObject.inverse(colObjWorldTransform);
					Vector3f rayFromLocal = new Vector3f(rayFromTrans.origin);
					worldTocollisionObject.transform(rayFromLocal);
					Vector3f rayToLocal = new Vector3f(rayToTrans.origin);
					worldTocollisionObject.transform(rayToLocal);

					BridgeTriangleRaycastCallback rcb = new BridgeTriangleRaycastCallback(rayFromLocal, rayToLocal, resultCallback, collisionObject, triangleMesh);
					rcb.hitFraction = resultCallback.closestHitFraction;
					triangleMesh.performRaycast(rcb, rayFromLocal, rayToLocal);
				}
				else {
					ConcaveShape triangleMesh = (ConcaveShape)collisionShape;

					Transform worldTocollisionObject = new Transform();
					worldTocollisionObject.inverse(colObjWorldTransform);

					Vector3f rayFromLocal = new Vector3f(rayFromTrans.origin);
					worldTocollisionObject.transform(rayFromLocal);
					Vector3f rayToLocal = new Vector3f(rayToTrans.origin);
					worldTocollisionObject.transform(rayToLocal);

					BridgeTriangleRaycastCallback rcb = new BridgeTriangleRaycastCallback(rayFromLocal, rayToLocal, resultCallback, collisionObject, triangleMesh);
					rcb.hitFraction = resultCallback.closestHitFraction;

					Vector3f rayAabbMinLocal = new Vector3f(rayFromLocal);
					VectorUtil.setMin(rayAabbMinLocal, rayToLocal);
					Vector3f rayAabbMaxLocal = new Vector3f(rayFromLocal);
					VectorUtil.setMax(rayAabbMaxLocal, rayToLocal);

					triangleMesh.processAllTriangles(rcb, rayAabbMinLocal, rayAabbMaxLocal);
				}
			}
			else {
				// todo: use AABB tree or other BVH acceleration structure!
				if (collisionShape.isCompound()) {
					CompoundShape compoundShape = (CompoundShape) collisionShape;
					int i;
					Transform childTrans = new Transform();
					int n = compoundShape.getNumChildShapes();
					for (i = 0; i < n; i++) {
						compoundShape.getChildTransform(i, childTrans);
						CollisionShape childCollisionShape = compoundShape.getChildShape(i);
						Transform childWorldTrans = new Transform(colObjWorldTransform);
						childWorldTrans.mul(childTrans);
						// replace collision shape so that callback can determine the triangle
						CollisionShape saveCollisionShape = collisionObject.getCollisionShape();
						collisionObject.internalSetTemporaryCollisionShape(childCollisionShape);
						rayTestSingle(rayFromTrans, rayToTrans,
								collisionObject,
								childCollisionShape,
								childWorldTrans,
								resultCallback, eps);
						// restore
						collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
					}
				}
			}
		}
	}

	private static class BridgeTriangleConvexcastCallback extends TriangleConvexcastCallback {
		final ConvexResultCallback resultCallback;
		final CollisionObject collisionObject;
		final TriangleMeshShape triangleMesh;
		boolean normalInWorldSpace;

		BridgeTriangleConvexcastCallback(ConvexShape castShape, Transform from, Transform to, ConvexResultCallback resultCallback, CollisionObject collisionObject, TriangleMeshShape triangleMesh, Transform triangleToWorld) {
			super(castShape, from, to, triangleToWorld, triangleMesh.getMargin());
			this.resultCallback = resultCallback;
			this.collisionObject = collisionObject;
			this.triangleMesh = triangleMesh;
		}

		@Override
		public float reportHit(Vector3f hitNormalLocal, Vector3f hitPointLocal, float hitFraction, int partId, int triangleIndex) {
			LocalShapeInfo shapeInfo = new LocalShapeInfo();
			shapeInfo.shapePart = partId;
			shapeInfo.triangleIndex = triangleIndex;
			if (hitFraction <= resultCallback.closestHitFraction) {
				LocalConvexResult convexResult = new LocalConvexResult(collisionObject, shapeInfo, hitNormalLocal, hitPointLocal, hitFraction);
				return resultCallback.addSingleResult(convexResult, normalInWorldSpace);
			}
			return hitFraction;
		}
	}

	/**
	 * objectQuerySingle performs a collision detection query and calls the resultCallback. It is used internally by rayTest.
	 */
	public static void objectQuerySingle(ConvexShape castShape, Transform convexFromTrans, Transform convexToTrans, CollisionObject collisionObject, CollisionShape collisionShape, Transform colObjWorldTransform, ConvexResultCallback resultCallback, float allowedPenetration) {
		if (collisionShape.isConvex()) {
			CastResult castResult = new CastResult();
			castResult.allowedPenetration = allowedPenetration;
			castResult.fraction = 1.0f; // ??

			ConvexShape convexShape = (ConvexShape) collisionShape;
			VoronoiSimplexSolver simplexSolver = new VoronoiSimplexSolver();
			GjkEpaPenetrationDepthSolver gjkEpaPenetrationSolver = new GjkEpaPenetrationDepthSolver();

			// JAVA TODO: should be convexCaster1
			//ContinuousConvexCollision convexCaster1(castShape,convexShape,&simplexSolver,&gjkEpaPenetrationSolver);
			//btSubsimplexConvexCast convexCaster3(castShape,convexShape,&simplexSolver);

			if (((ConvexCast) new GjkConvexCast(castShape, convexShape, simplexSolver)).timeOfImpact(convexFromTrans, convexToTrans, colObjWorldTransform, colObjWorldTransform, castResult)) {
				// add hit
				if (castResult.normal.lengthSquared() > 0.0001f) {
					if (castResult.fraction < resultCallback.closestHitFraction) {
						castResult.normal.normalize();
						LocalConvexResult localConvexResult = new LocalConvexResult(collisionObject, null, castResult.normal, castResult.hitPoint, castResult.fraction);

						resultCallback.addSingleResult(localConvexResult, true);
					}
				}
			}
		}
		else {
			if (collisionShape.isConcave()) {
				if (collisionShape.getShapeType() == BroadphaseNativeType.TRIANGLE_MESH_SHAPE_PROXYTYPE) {
					BvhTriangleMeshShape triangleMesh = (BvhTriangleMeshShape)collisionShape;
					Transform worldTocollisionObject = new Transform();
					worldTocollisionObject.inverse(colObjWorldTransform);

					Vector3f convexFromLocal = new Vector3f();
					convexFromLocal.set(convexFromTrans.origin);
					worldTocollisionObject.transform(convexFromLocal);

					Vector3f convexToLocal = new Vector3f();
					convexToLocal.set(convexToTrans.origin);
					worldTocollisionObject.transform(convexToLocal);

					// rotation of box in local mesh space = MeshRotation^-1 * ConvexToRotation
					Transform rotationXform = new Transform();
					Matrix3f tmpMat = new Matrix3f();
					tmpMat.mul(worldTocollisionObject.basis, convexToTrans.basis);
					rotationXform.set(tmpMat);

					BridgeTriangleConvexcastCallback tccb = new BridgeTriangleConvexcastCallback(castShape, convexFromTrans, convexToTrans, resultCallback, collisionObject, triangleMesh, colObjWorldTransform);
					tccb.hitFraction = resultCallback.closestHitFraction;
					tccb.normalInWorldSpace = true;
					
					Vector3f boxMinLocal = new Vector3f();
					Vector3f boxMaxLocal = new Vector3f();
					castShape.getAabb(rotationXform, boxMinLocal, boxMaxLocal);
					triangleMesh.performConvexcast(tccb, convexFromLocal, convexToLocal, boxMinLocal, boxMaxLocal);
				}
				else {
					BvhTriangleMeshShape triangleMesh = (BvhTriangleMeshShape)collisionShape;
					Transform worldTocollisionObject = new Transform();
					worldTocollisionObject.inverse(colObjWorldTransform);

					Vector3f convexFromLocal = new Vector3f();
					convexFromLocal.set(convexFromTrans.origin);
					worldTocollisionObject.transform(convexFromLocal);

					Vector3f convexToLocal = new Vector3f();
					convexToLocal.set(convexToTrans.origin);
					worldTocollisionObject.transform(convexToLocal);

					// rotation of box in local mesh space = MeshRotation^-1 * ConvexToRotation
					Transform rotationXform = new Transform();
					Matrix3f tmpMat = new Matrix3f();
					tmpMat.mul(worldTocollisionObject.basis, convexToTrans.basis);
					rotationXform.set(tmpMat);

					BridgeTriangleConvexcastCallback tccb = new BridgeTriangleConvexcastCallback(castShape, convexFromTrans, convexToTrans, resultCallback, collisionObject, triangleMesh, colObjWorldTransform);
					tccb.hitFraction = resultCallback.closestHitFraction;
					tccb.normalInWorldSpace = false;
					Vector3f boxMinLocal = new Vector3f();
					Vector3f boxMaxLocal = new Vector3f();
					castShape.getAabb(rotationXform, boxMinLocal, boxMaxLocal);

					Vector3f rayAabbMinLocal = new Vector3f(convexFromLocal);
					VectorUtil.setMin(rayAabbMinLocal, convexToLocal);
					Vector3f rayAabbMaxLocal = new Vector3f(convexFromLocal);
					VectorUtil.setMax(rayAabbMaxLocal, convexToLocal);
					rayAabbMinLocal.add(boxMinLocal);
					rayAabbMaxLocal.add(boxMaxLocal);
					triangleMesh.processAllTriangles(tccb, rayAabbMinLocal, rayAabbMaxLocal);
				}
			}
			else {
				// todo: use AABB tree or other BVH acceleration structure!
				if (collisionShape.isCompound()) {
					CompoundShape compoundShape = (CompoundShape) collisionShape;
					for (int i = 0; i < compoundShape.getNumChildShapes(); i++) {
						Transform childTrans = compoundShape.getChildTransform(i, new Transform());
						CollisionShape childCollisionShape = compoundShape.getChildShape(i);
						Transform childWorldTrans = new Transform();
						childWorldTrans.mul(colObjWorldTransform, childTrans);
						// replace collision shape so that callback can determine the triangle
						CollisionShape saveCollisionShape = collisionObject.getCollisionShape();
						collisionObject.internalSetTemporaryCollisionShape(childCollisionShape);
						objectQuerySingle(castShape, convexFromTrans, convexToTrans,
						                  collisionObject,
						                  childCollisionShape,
						                  childWorldTrans,
						                  resultCallback, allowedPenetration);
						// restore
						collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
					}
				}
			}
		}
	}

	/**
	 * rayTest performs a raycast on all objects in the CollisionWorld, and calls the resultCallback.
	 * This allows for several queries: first hit, all hits, any hit, dependent on the value returned by the callback.
	 */
	public void rayTest(Vector3f rayFromWorld, Vector3f rayToWorld, RayResultCallback resultCallback, double eps) {
		Transform rayFromTrans = new Transform(), rayToTrans = new Transform();
		rayFromTrans.setIdentity();
		rayFromTrans.origin.set(rayFromWorld);
		rayToTrans.setIdentity();

		rayToTrans.origin.set(rayToWorld);

		// go over all objects, and if the ray intersects their aabb, do a ray-shape query using convexCaster (CCD)
		Vector3f collisionObjectAabbMin = new Vector3f(), collisionObjectAabbMax = new Vector3f();
		float[] hitLambda = new float[1];

		Transform tmpTrans = new Transform();
		
		for (int i = 0; i < collisionObjects.size(); i++) {
			// terminate further ray tests, once the closestHitFraction reached zero
			if (resultCallback.closestHitFraction == 0.0f) {
				break;
			}

			CollisionObject collisionObject = collisionObjects.get(i);
			// only perform raycast if filterMask matches
			if (resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {
				//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
				CollisionShape shape = collisionObject.getCollisionShape();
				shape.getAabb(collisionObject.getWorldTransform(tmpTrans), collisionObjectAabbMin, collisionObjectAabbMax);

				hitLambda[0] = resultCallback.closestHitFraction;
				Vector3f hitNormal = new Vector3f();
				if (AabbUtil2.rayAabb(rayFromWorld, rayToWorld, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
					rayTestSingle(rayFromTrans, rayToTrans,
							collisionObject,
							shape,
							collisionObject.getWorldTransform(tmpTrans),
							resultCallback, eps);
				}
			}

		}
	}

	/**
	 * convexTest performs a swept convex cast on all objects in the {@link CollisionWorld}, and calls the resultCallback
	 * This allows for several queries: first hit, all hits, any hit, dependent on the value return by the callback.
	 */
	public void convexSweepTest(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, ConvexResultCallback resultCallback) {
		Transform convexFromTrans = new Transform();
		Transform convexToTrans = new Transform();

		convexFromTrans.set(convexFromWorld);
		convexToTrans.set(convexToWorld);

		Vector3f castShapeAabbMin = new Vector3f();
		Vector3f castShapeAabbMax = new Vector3f();

		// Compute AABB that encompasses angular movement
		{
			Vector3f linVel = new Vector3f();
			Vector3f angVel = new Vector3f();
			TransformUtil.calculateVelocity(convexFromTrans, convexToTrans, 1.0f, linVel, angVel);
			Transform R = new Transform();
			R.setIdentity();
			R.setRotation(convexFromTrans.getRotation(new Quat4f()));
			castShape.calculateTemporalAabb(R, linVel, angVel, 1.0f, castShapeAabbMin, castShapeAabbMax);
		}

		Transform tmpTrans = new Transform();
		Vector3f collisionObjectAabbMin = new Vector3f();
		Vector3f collisionObjectAabbMax = new Vector3f();
		float[] hitLambda = new float[1];

		// go over all objects, and if the ray intersects their aabb + cast shape aabb,
		// do a ray-shape query using convexCaster (CCD)
		for (int i = 0; i < collisionObjects.size(); i++) {
			CollisionObject collisionObject = collisionObjects.get(i);

			// only perform raycast if filterMask matches
			if (resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {
				//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
				collisionObject.getWorldTransform(tmpTrans);
				collisionObject.getCollisionShape().getAabb(tmpTrans, collisionObjectAabbMin, collisionObjectAabbMax);
				AabbUtil2.aabbExpand(collisionObjectAabbMin, collisionObjectAabbMax, castShapeAabbMin, castShapeAabbMax);
				hitLambda[0] = 1.0f; // could use resultCallback.closestHitFraction, but needs testing
				Vector3f hitNormal = new Vector3f();
				if (AabbUtil2.rayAabb(convexFromWorld.origin, convexToWorld.origin, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
					objectQuerySingle(castShape, convexFromTrans, convexToTrans,
					                  collisionObject,
					                  collisionObject.getCollisionShape(),
					                  tmpTrans,
					                  resultCallback,
					                  getDispatchInfo().allowedCcdPenetration);
				}
			}
		}
	}

	public ObjectArrayList<CollisionObject> getCollisionObjectArray() {
		return collisionObjects;
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * LocalShapeInfo gives extra information for complex shapes.
	 * Currently, only btTriangleMeshShape is available, so it just contains triangleIndex and subpart.
	 */
	public static class LocalShapeInfo {
		int shapePart;
		int triangleIndex;
		//const btCollisionShape*	m_shapeTemp;
		//const btTransform*	m_shapeLocalTransform;
	}
	
	public static class LocalRayResult {
		public final CollisionObject collisionObject;
		final LocalShapeInfo localShapeInfo;
		final Vector3f hitNormalLocal = new Vector3f();
		final float hitFraction;

		LocalRayResult(CollisionObject collisionObject, LocalShapeInfo localShapeInfo, Vector3f hitNormalLocal, float hitFraction) {
			this.collisionObject = collisionObject;
			this.localShapeInfo = localShapeInfo;
			this.hitNormalLocal.set(hitNormalLocal);
			this.hitFraction = hitFraction;
		}
	}

	public static class ClosestRayResultCallback extends RayResultCallback {
		final Vector3f rayFromWorld = new Vector3f(); //used to calculate hitPointWorld from hitFraction
		final Vector3f rayToWorld = new Vector3f();

		public final Vector3f hitNormalWorld = new Vector3f();
		public final Vector3f hitPointWorld = new Vector3f();
		
		public ClosestRayResultCallback(Vector3f rayFromWorld, Vector3f rayToWorld) {
			this.rayFromWorld.set(rayFromWorld);
			this.rayToWorld.set(rayToWorld);
		}
		
		@Override
		public float addSingleResult(LocalRayResult rayResult, boolean normalInWorldSpace) {
			// caller already does the filter on the closestHitFraction
			assert (rayResult.hitFraction <= closestHitFraction);

			closestHitFraction = rayResult.hitFraction;
			collisionObject = rayResult.collisionObject;
			if (normalInWorldSpace) {
				hitNormalWorld.set(rayResult.hitNormalLocal);
			}
			else {
				// need to transform normal into worldspace
				hitNormalWorld.set(rayResult.hitNormalLocal);
				collisionObject.getWorldTransform(new Transform()).basis.transform(hitNormalWorld);
			}

			VectorUtil.setInterpolate3(hitPointWorld, rayFromWorld, rayToWorld, rayResult.hitFraction);
			return rayResult.hitFraction;
		}
	}
	
	public static class LocalConvexResult {
		public final CollisionObject hitCollisionObject;
		final LocalShapeInfo localShapeInfo;
		public final Vector3f hitNormalLocal = new Vector3f();
		final Vector3f hitPointLocal = new Vector3f();
		final float hitFraction;

		LocalConvexResult(CollisionObject hitCollisionObject, LocalShapeInfo localShapeInfo, Vector3f hitNormalLocal, Vector3f hitPointLocal, float hitFraction) {
			this.hitCollisionObject = hitCollisionObject;
			this.localShapeInfo = localShapeInfo;
			this.hitNormalLocal.set(hitNormalLocal);
			this.hitPointLocal.set(hitPointLocal);
			this.hitFraction = hitFraction;
		}
	}
	
	public abstract static class ConvexResultCallback {
		public float closestHitFraction = 1.0f;
		public short collisionFilterGroup = CollisionFilterGroups.DEFAULT_FILTER;
		public short collisionFilterMask = CollisionFilterGroups.ALL_FILTER;
		
		public boolean hasHit() {
			return (closestHitFraction < 1.0f);
		}
		
		public boolean needsCollision(BroadphaseProxy proxy0) {
			boolean collides = ((proxy0.collisionFilterGroup & collisionFilterMask) & 0xFFFF) != 0;
			collides = collides && ((collisionFilterGroup & proxy0.collisionFilterMask) & 0xFFFF) != 0;
			return collides;
		}
		
		protected abstract float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace);
	}
	
	public static class ClosestConvexResultCallback extends ConvexResultCallback {
		public final Vector3f convexFromWorld = new Vector3f(); // used to calculate hitPointWorld from hitFraction
		public final Vector3f convexToWorld = new Vector3f();
		public final Vector3f hitNormalWorld = new Vector3f();
		public final Vector3f hitPointWorld = new Vector3f();
		public CollisionObject hitCollisionObject;

		public ClosestConvexResultCallback(Vector3f convexFromWorld, Vector3f convexToWorld) {
			this.convexFromWorld.set(convexFromWorld);
			this.convexToWorld.set(convexToWorld);
			this.hitCollisionObject = null;
		}

		@Override
		public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace) {
			// caller already does the filter on the m_closestHitFraction
			assert (convexResult.hitFraction <= closestHitFraction);

			closestHitFraction = convexResult.hitFraction;
			hitCollisionObject = convexResult.hitCollisionObject;
			if (normalInWorldSpace) {
				hitNormalWorld.set(convexResult.hitNormalLocal);
			} else {
				// need to transform normal into worldspace
				hitNormalWorld.set(convexResult.hitNormalLocal);
				hitCollisionObject.getWorldTransform(new Transform()).basis.transform(hitNormalWorld);
			}
			if (hitNormalWorld.length() > 2) {
				System.out.println("CollisionWorld.addSingleResult world " + hitNormalWorld);
			}

			hitPointWorld.set(convexResult.hitPointLocal);
			return convexResult.hitFraction;
		}
	}
	
	private static class BridgeTriangleRaycastCallback extends TriangleRaycastCallback {
		final RayResultCallback resultCallback;
		final CollisionObject collisionObject;
		final ConcaveShape triangleMesh;

		BridgeTriangleRaycastCallback(Vector3f from, Vector3f to, RayResultCallback resultCallback, CollisionObject collisionObject, ConcaveShape triangleMesh) {
			super(from, to);
			this.resultCallback = resultCallback;
			this.collisionObject = collisionObject;
			this.triangleMesh = triangleMesh;
		}
	
		public float reportHit(Vector3f hitNormalLocal, float hitFraction, int partId, int triangleIndex) {
			LocalShapeInfo shapeInfo = new LocalShapeInfo();
			shapeInfo.shapePart = partId;
			shapeInfo.triangleIndex = triangleIndex;

			return resultCallback.addSingleResult(new LocalRayResult(collisionObject, shapeInfo, hitNormalLocal, hitFraction), false);
		}
	}
	
}