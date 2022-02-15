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

/**
 *
 * @author jezek2
 */
public interface IGL {

	int GL_LIGHT0 = 0x4000;
	int GL_LIGHT1 = 0x4001;
	int GL_AMBIENT = 0x1200;
	int GL_DIFFUSE = 0x1201;
	int GL_SPECULAR = 0x1202;
	int GL_POSITION = 0x1203;
	int GL_LIGHTING = 0xb50;
	int GL_SMOOTH = 0x1d01;
	int GL_DEPTH_TEST = 0xb71;
	int GL_LESS = 0x201;
	int GL_MODELVIEW = 0x1700;
	int GL_PROJECTION = 0x1701;
	int GL_COLOR_BUFFER_BIT = 0x4000;
	int GL_DEPTH_BUFFER_BIT = 0x100;
	int GL_POINTS = 0x0;
	int GL_LINES = 0x1;
	int GL_TRIANGLES = 0x4;
	int GL_COLOR_MATERIAL = 0xb57;
	int GL_QUADS = 0x0007;
	
	void glLight(int light, int pname, float[] params);
	void glEnable(int cap);
	void glDisable(int cap);
	void glShadeModel(int mode);
	void glDepthFunc(int func);
	void glClearColor(float red, float green, float blue, float alpha);
	void glMatrixMode(int mode);
	void glLoadIdentity();
	void glFrustum(double left, double right, double bottom, double top, double zNear, double zFar);
	void gluLookAt(float eyex, float eyey, float eyez, float centerx, float centery, float centerz, float upx, float upy, float upz);
	void glViewport(int x, int y, int width, int height);
	void glPushMatrix();
	void glPopMatrix();
	void gluOrtho2D(float left, float right, float bottom, float top);
	void glScalef(float x, float y, float z);
	void glTranslatef(float x, float y, float z);
	void glColor3f(float red, float green, float blue);
	void glClear(int mask);
	void glBegin(int mode);
	void glEnd();
	void glVertex3f(float x, float y, float z);
	void glLineWidth(float width);
	void glPointSize(float size);
	void glNormal3f(float nx, float ny, float nz);
	void glMultMatrix(float[] m);
	
	@Deprecated void drawCube(float extent);

	default void drawCube(float sx, float sy, float sz) {
		glScalef(sx, sy, sz);
		drawCube(1);
	}

	void drawSphere(float radius, int slices);
	void drawCylinder(float radius, float halfHeight, int upAxis);

	void drawString(CharSequence s, int x, int y, float red, float green, float blue);
	
}