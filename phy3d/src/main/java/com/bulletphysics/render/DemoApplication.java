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

package com.bulletphysics.render;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.CProfileIterator;
import com.bulletphysics.linearmath.CProfileManager;
import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.Transform;
import com.jogamp.newt.event.KeyEvent;

import javax.vecmath.Color3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author jezek2
 */
public abstract  class DemoApplication extends SpaceGraph3D {


    private CollisionShape shootBoxShape;
	protected float ShootBoxInitialSpeed = 40.0f;

    private int lastKey;

	private final CProfileIterator profileIterator;

	public DemoApplication() {
        super();
        BulletStats.setProfileEnabled(true);
		profileIterator = CProfileManager.getIterator();
	}


	@Override
    public void keyboardCallback(char key) {
		lastKey = 0;

		if (key >= 0x31 && key < 0x37) {
			int child = key - 0x31;
			profileIterator.enterChild(child);
		}
		if (key == 0x30) {
			profileIterator.enterParent();
		}

		switch (key) {
			case 'l':
				stepLeft();
				break;
			case 'r':
				stepRight();
				break;
			case 'f':
				stepFront();
				break;
			case 'b':
				stepBack();
				break;
			case 'z':
				zoomIn();
				break;
			case 'x':
				zoomOut();
				break;
			case 'i':
				toggleIdle();
				break;
			case 'h':
				if ((debugMode & DebugDrawModes.NO_HELP_TEXT) != 0) {
					debugMode = debugMode & (~DebugDrawModes.NO_HELP_TEXT);
				}
				else {
					debugMode |= DebugDrawModes.NO_HELP_TEXT;
				}
				break;

			case 'w':
				if ((debugMode & DebugDrawModes.DRAW_WIREFRAME) != 0) {
					debugMode = debugMode & (~DebugDrawModes.DRAW_WIREFRAME);
				}
				else {
					debugMode |= DebugDrawModes.DRAW_WIREFRAME;
				}
				break;

			case 'p':
				if ((debugMode & DebugDrawModes.PROFILE_TIMINGS) != 0) {
					debugMode = debugMode & (~DebugDrawModes.PROFILE_TIMINGS);
				}
				else {
					debugMode |= DebugDrawModes.PROFILE_TIMINGS;
				}
				break;

			case 'm':
				if ((debugMode & DebugDrawModes.ENABLE_SAT_COMPARISON) != 0) {
					debugMode = debugMode & (~DebugDrawModes.ENABLE_SAT_COMPARISON);
				}
				else {
					debugMode |= DebugDrawModes.ENABLE_SAT_COMPARISON;
				}
				break;

			case 'n':
				if ((debugMode & DebugDrawModes.DISABLE_BULLET_LCP) != 0) {
					debugMode = debugMode & (~DebugDrawModes.DISABLE_BULLET_LCP);
				}
				else {
					debugMode |= DebugDrawModes.DISABLE_BULLET_LCP;
				}
				break;

			case 't':
				if ((debugMode & DebugDrawModes.DRAW_TEXT) != 0) {
					debugMode = debugMode & (~DebugDrawModes.DRAW_TEXT);
				}
				else {
					debugMode |= DebugDrawModes.DRAW_TEXT;
				}
				break;
			case 'y':
				if ((debugMode & DebugDrawModes.DRAW_FEATURES_TEXT) != 0) {
					debugMode = debugMode & (~DebugDrawModes.DRAW_FEATURES_TEXT);
				}
				else {
					debugMode |= DebugDrawModes.DRAW_FEATURES_TEXT;
				}
				break;
			case 'a':
				if ((debugMode & DebugDrawModes.DRAW_AABB) != 0) {
					debugMode = debugMode & (~DebugDrawModes.DRAW_AABB);
				}
				else {
					debugMode |= DebugDrawModes.DRAW_AABB;
				}
				break;
			case 'c':
				if ((debugMode & DebugDrawModes.DRAW_CONTACT_POINTS) != 0) {
					debugMode = debugMode & (~DebugDrawModes.DRAW_CONTACT_POINTS);
				}
				else {
					debugMode |= DebugDrawModes.DRAW_CONTACT_POINTS;
				}
				break;

			case 'd':
				if ((debugMode & DebugDrawModes.NO_DEACTIVATION) != 0) {
					debugMode = debugMode & (~DebugDrawModes.NO_DEACTIVATION);
				}
				else {
					debugMode |= DebugDrawModes.NO_DEACTIVATION;
				}
				BulletGlobals.setDeactivationDisabled((debugMode & DebugDrawModes.NO_DEACTIVATION) != 0);
				break;

			case 'o': {
				stepping = !stepping;
				break;
			}
			case 's':
				//moveAndDisplay();
				break;
			//    case ' ' : newRandom(); break;
			case ' ':
				reset();
				break;
			case '1': {
				if ((debugMode & DebugDrawModes.ENABLE_CCD) != 0) {
					debugMode = debugMode & (~DebugDrawModes.ENABLE_CCD);
				}
				else {
					debugMode |= DebugDrawModes.ENABLE_CCD;
				}
				break;
			}

			case '.': {
				shootBox(cameraTarget());
				break;
			}

			case '+': {
				ShootBoxInitialSpeed += 10.0f;
				break;
			}
			case '-': {
				ShootBoxInitialSpeed -= 10.0f;
				break;
			}

			default:
				// std::cout << "unused key : " << key << std::endl;
				break;
		}

		if (world() != null && world().getDebugDrawer() != null) {
			world().getDebugDrawer().setDebugMode(debugMode);
		}

		//LWJGL.postRedisplay();
	}

	@Override
    public void specialKeyboard(int key) {
		switch (key) {
			case KeyEvent.VK_F1, KeyEvent.VK_F2 -> {
			}
			case KeyEvent.VK_END -> {
				int numObj = world().getNumCollisionObjects();
				if (numObj != 0) {
					CollisionObject obj = world().getCollisionObjectArray().get(numObj - 1);

					world().removeCollisionObject(obj);
					RigidBody body = RigidBody.upcast(obj);
					if (body != null && body.getMotionState() != null) {
						//delete body->getMotionState();
					}
					//delete obj;
				}
			}
			case KeyEvent.VK_LEFT -> stepLeft();
			case KeyEvent.VK_RIGHT -> stepRight();
			case KeyEvent.VK_UP -> stepFront();
			case KeyEvent.VK_DOWN -> stepBack();
			case KeyEvent.VK_PAGE_UP /* TODO: check PAGE_UP */ -> zoomIn();
			case KeyEvent.VK_PAGE_DOWN/* TODO: checkPAGE_DOWN */ -> zoomOut();
			case KeyEvent.VK_HOME -> toggleIdle();
			default -> {
			}
			// std::cout << "unused (special) key : " << key << std::endl;
		}

		//LWJGL.postRedisplay();
	}


	private void shootBox(Vector3f destination) {
		if (world != null) {
			float mass = 10.0f;
			Transform startTransform = new Transform();
			startTransform.setIdentity();
			Vector3f camPos = new Vector3f(cameraPosition());
			startTransform.origin.set(camPos);

			if (shootBoxShape == null) {
				//#define TEST_UNIFORM_SCALING_SHAPE 1
				//#ifdef TEST_UNIFORM_SCALING_SHAPE
				//btConvexShape* childShape = new btBoxShape(btVector3(1.f,1.f,1.f));
				//m_shootBoxShape = new btUniformScalingShape(childShape,0.5f);
				//#else
				shootBoxShape = new BoxShape(new Vector3f(1.0f, 1.0f, 1.0f));
				//#endif//
			}

			RigidBody body = world.localCreateRigidBody(shootBoxShape, mass, startTransform);

			Vector3f linVel = new Vector3f(destination.x - camPos.x, destination.y - camPos.y, destination.z - camPos.z);
			linVel.normalize();
			linVel.scale(ShootBoxInitialSpeed);

			Transform worldTrans = body.getWorldTransform(new Transform());
			worldTrans.origin.set(camPos);
			worldTrans.setRotation(new Quat4f(0.0f, 0.0f, 0.0f, 1.0f));
			body.setWorldTransform(worldTrans);

			body.setLinearVelocity(linVel);
			body.setAngularVelocity(new Vector3f());

			body.setCcdMotionThreshold(1.0f);
			body.setCcdSweptSphereRadius(0.2f);
		}
	}



    private void displayProfileString(float xOffset, float yStart, CharSequence message) {
		drawString(message, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
	}

	private static double time_since_reset = 0.0f;

	private float showProfileInfo(float xOffset, float yStart, float yIncr) {
		if (!idle) {
			time_since_reset = CProfileManager.getTimeSinceReset();
		}

		{
			// recompute profiling data, and store profile strings

			double totalTime = 0;

			int frames_since_reset = CProfileManager.getFrameCountSinceReset();

			profileIterator.first();

			double parent_time = profileIterator.isRoot() ? time_since_reset : profileIterator.getCurrentParentTotalTime();

			{
				buf.setLength(0);
				buf.append("--- Profiling: ");
				buf.append(profileIterator.getCurrentParentName());
				buf.append(" (total running time: ");
				FastFormat.append(buf, (float) parent_time, 3);
				buf.append(" ms) ---");
				displayProfileString(xOffset, yStart, buf);
				yStart += yIncr;
				String s = "press number (1,2...) to display child timings, or 0 to go up to parent";
				displayProfileString(xOffset, yStart, s);
				yStart += yIncr;
			}

			double accumulated_time = 0.0f;

			for (int i = 0; !profileIterator.isDone(); profileIterator.next()) {
				double current_total_time = profileIterator.getCurrentTotalTime();
				accumulated_time += current_total_time;
				double fraction = parent_time > BulletGlobals.FLT_EPSILON ? (current_total_time / parent_time) * 100 : 0.0f;

				buf.setLength(0);
				FastFormat.append(buf, ++i);
				buf.append(" -- ");
				buf.append(profileIterator.getCurrentName());
				buf.append(" (");
				FastFormat.append(buf, (float) fraction, 2);
				buf.append(" %) :: ");
				FastFormat.append(buf, (float) (current_total_time / frames_since_reset), 3);
				buf.append(" ms / frame (");
				FastFormat.append(buf, profileIterator.getCurrentTotalCalls());
				buf.append(" calls)");

				displayProfileString(xOffset, yStart, buf);
				yStart += yIncr;
				totalTime += current_total_time;
			}

			buf.setLength(0);
			buf.append("Unaccounted (");
			FastFormat.append(buf, (float) (parent_time > BulletGlobals.FLT_EPSILON ? ((parent_time - accumulated_time) / parent_time) * 100 : 0.0f), 3);
			buf.append(" %) :: ");
			FastFormat.append(buf, (float) (parent_time - accumulated_time), 3);
			buf.append(" ms");

			displayProfileString(xOffset, yStart, buf);
			yStart += yIncr;

			String s = "-------------------------------------------------";
			displayProfileString(xOffset, yStart, s);
			yStart += yIncr;

		}

		return yStart;
	}

	protected final Color3f TEXT_COLOR = new Color3f(0.0f, 0.0f, 0.0f);
	private final StringBuilder buf = new StringBuilder();



	@Override protected void renderHUD() {

		if ((debugMode & DebugDrawModes.NO_HELP_TEXT) == 0) {
			gl.glColor3f(0.0f, 0.0f, 0.0f);
			float xOffset = 10.0f;
			float yStart = 20.0f;
			float yIncr = 20.0f;


			yStart = showProfileInfo(xOffset, yStart, yIncr);

//					#ifdef USE_QUICKPROF
//					if ( getDebugMode() & btIDebugDraw::DBG_ProfileTimings)
//					{
//						static int counter = 0;
//						counter++;
//						std::map<std::string, hidden::ProfileBlock*>::iterator iter;
//						for (iter = btProfiler::mProfileBlocks.begin(); iter != btProfiler::mProfileBlocks.end(); ++iter)
//						{
//							char blockTime[128];
//							sprintf(blockTime, "%s: %lf",&((*iter).first[0]),btProfiler::getBlockTime((*iter).first, btProfiler::BLOCK_CYCLE_SECONDS));//BLOCK_TOTAL_PERCENT));
//							glRasterPos3f(xOffset,yStart,0);
//							BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),blockTime);
//							yStart += yIncr;
//
//						}
//					}
//					#endif //USE_QUICKPROF


			String s = "mouse to interact";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			// JAVA NOTE: added
			s = "LMB=drag, RMB=shoot box, MIDDLE=apply impulse";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			s = "space to reset";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			s = "cursor keys and z,x to navigate";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			s = "i to toggle simulation, s single step";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			s = "q to quit";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			s = ". to shoot box or trimesh (MovingConcaveDemo)";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			// not yet hooked up again after refactoring...

			s = "d to toggle deactivation";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			s = "g to toggle mesh animation (ConcaveDemo)";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			// JAVA NOTE: added
			s = "e to spawn new body (GenericJointDemo)";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			s = "h to toggle help text";
			drawString(s, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			//buf = "p to toggle profiling (+results to file)";
			//drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			//bool useBulletLCP = !(getDebugMode() & btIDebugDraw::DBG_DisableBulletLCP);
			//bool useCCD = (getDebugMode() & btIDebugDraw::DBG_EnableCCD);
			//glRasterPos3f(xOffset,yStart,0);
			//sprintf(buf,"1 CCD mode (adhoc) = %i",useCCD);
			//BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
			//yStart += yIncr;

			//glRasterPos3f(xOffset, yStart, 0);
			//buf = String.format(%10.2f", ShootBoxInitialSpeed);
			buf.setLength(0);
			buf.append("+- shooting speed = ");
			FastFormat.append(buf, ShootBoxInitialSpeed);
			drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			//#ifdef SHOW_NUM_DEEP_PENETRATIONS
			buf.setLength(0);
			buf.append("gNumDeepPenetrationChecks = ");
			FastFormat.append(buf, BulletStats.gNumDeepPenetrationChecks);
			drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			buf.setLength(0);
			buf.append("gNumGjkChecks = ");
			FastFormat.append(buf, BulletStats.gNumGjkChecks);
			drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			buf.setLength(0);
			buf.append("gNumSplitImpulseRecoveries = ");
			FastFormat.append(buf, BulletStats.gNumSplitImpulseRecoveries);
			drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
			yStart += yIncr;

			//buf = String.format("gNumAlignedAllocs = %d", BulletGlobals.gNumAlignedAllocs);
			// TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
			//yStart += yIncr;

			//buf = String.format("gNumAlignedFree= %d", BulletGlobals.gNumAlignedFree);
			// TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
			//yStart += yIncr;

			//buf = String.format("# alloc-free = %d", BulletGlobals.gNumAlignedAllocs - BulletGlobals.gNumAlignedFree);
			// TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
			//yStart += yIncr;

			//enable BT_DEBUG_MEMORY_ALLOCATIONS define in Bullet/src/LinearMath/btAlignedAllocator.h for memory leak detection
			//#ifdef BT_DEBUG_MEMORY_ALLOCATIONS
			//glRasterPos3f(xOffset,yStart,0);
			//sprintf(buf,"gTotalBytesAlignedAllocs = %d",gTotalBytesAlignedAllocs);
			//BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
			//yStart += yIncr;
			//#endif //BT_DEBUG_MEMORY_ALLOCATIONS

			if (world() != null) {
				buf.setLength(0);
				buf.append("# objects = ");
				FastFormat.append(buf, world().getNumCollisionObjects());
				drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
				yStart += yIncr;

				buf.setLength(0);
				buf.append("# pairs = ");
				FastFormat.append(buf, world().broadphase().getOverlappingPairCache().getNumOverlappingPairs());
				drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
				yStart += yIncr;

			}
			//#endif //SHOW_NUM_DEEP_PENETRATIONS

			// JAVA NOTE: added
			int free = (int)Runtime.getRuntime().freeMemory();
			int total = (int)Runtime.getRuntime().totalMemory();
			buf.setLength(0);
			buf.append("heap = ");
			FastFormat.append(buf, (float)(total - free) / (1024*1024));
			buf.append(" / ");
			FastFormat.append(buf, (float)(total) / (1024*1024));
			buf.append(" MB");
			drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);

		}
	}



}