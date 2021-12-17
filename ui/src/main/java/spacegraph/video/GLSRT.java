///*
// * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
// *
// * Bullet Continuous Collision Detection and Physics Library
// * Copyright (c) 2003-2007 Erwin Coumans  http:
// *
// * This software is provided 'as-is', without any express or implied warranty.
// * In no event will the authors be held liable for any damages arising from
// * the use of this software.
// *
// * Permission is granted to anyone to use this software for any purpose,
// * including commercial applications, and to alter it and redistribute it
// * freely, subject to the following restrictions:
// *
// * 1. The origin of this software must not be misrepresented; you must not
// *    claim that you wrote the original software. If you use this software
// *    in a product, an acknowledgment in the product documentation would be
// *    appreciated but is not required.
// * 2. Altered source versions must be plainly marked as such, and must not be
// *    misrepresented as being the original software.
// * 3. This notice may not be removed or altered from any source distribution.
// */
//
//package spacegraph.video;
//
//import com.jogamp.common.nio.Buffers;
//import com.jogamp.opengl.GL;
//import com.jogamp.opengl.GL2;
//import com.jogamp.opengl.glu.GLU;
//import com.jogamp.opengl.glu.GLUquadric;
//import com.jogamp.opengl.util.ImmModeSink;
//
//import java.nio.ByteBuffer;
//import java.util.HashMap;
//import java.util.Map;
//
//
//
///**
// *
// * @author jezek2
// */
//public class GLSRT {
//
//    private static final boolean VBO_CACHE = true;
//
//
//    /*static {
//        ImmModeSink.setVBOUsage(false);
//    }*/
//
//	private final GLU    glu;
//
//
//	public GLSRT(GLU glu) {
//        System.out.println("VBO_CACHE: "+VBO_CACHE);
//        this.glu = glu;
//        /*
//		try {
//			font = new GLFont(gl, DemoApplication.class.getResourceAsStream("DejaVu_Sans_11.fnt"));
//            URL fontURL = DemoApplication.class.getResource("DejaVu_Sans_11.fnt");
//            if(fontURL!=null) {
//                font = new GLFont(gl, fontURL.openStream());
//            }
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		} */
//	}
//
//    private ImmModeSink vboCube;
//
//	public void drawCube(GL gl, float extent) {
//        extent *= 0.5f;
//
//        if(vboCube==null) {
//
//            vboCube = /*ImmModeSink
//					.createFixed(
//							GL.GL_STATIC_DRAW, 24,
//                                  3, GL.GL_FLOAT,  // vertex
//                                  0, GL.GL_FLOAT,  // color
//                                  3, GL.GL_FLOAT,  // normal
//                                  0, GL.GL_FLOAT); // texture*/
//					ImmModeSink.createFixed(24,
//					3, GL.GL_FLOAT,
//					0, GL.GL_FLOAT,
//					3, GL.GL_FLOAT,
//					0, GL.GL_FLOAT,
//					GL.GL_STATIC_DRAW);
//            vboCube.glBegin(GL.GL_TRIANGLES);
//            vboCube.glNormal3f(0.0f, 0.0f, 1.0f);
//            vboCube.glNormal3f(0.0f, 0.0f, 1.0f);
//            vboCube.glNormal3f(0.0f, 0.0f, 1.0f);
//            vboCube.glNormal3f(0.0f, 0.0f, 1.0f);
//            vboCube.glVertex3f(-extent,+extent,+extent);
//            vboCube.glVertex3f(+extent,-extent,+extent);
//            vboCube.glVertex3f(+extent,+extent,+extent);
//            vboCube.glVertex3f(-extent,-extent,+extent);
//            vboCube.glNormal3f(0.0f, 0.0f,-1.0f);
//            vboCube.glNormal3f(0.0f, 0.0f,-1.0f);
//            vboCube.glNormal3f(0.0f, 0.0f,-1.0f);
//            vboCube.glNormal3f(0.0f, 0.0f,-1.0f);
//            vboCube.glVertex3f(-extent,+extent,-extent);
//            vboCube.glVertex3f(+extent,-extent,-extent);
//            vboCube.glVertex3f(+extent,+extent,-extent);
//            vboCube.glVertex3f(+extent,+extent,+extent);
//            vboCube.glNormal3f(0.0f, -1.0f, 0.0f);
//            vboCube.glNormal3f(0.0f, -1.0f, 0.0f);
//            vboCube.glNormal3f(0.0f, -1.0f, 0.0f);
//            vboCube.glNormal3f(0.0f, -1.0f, 0.0f);
//            vboCube.glVertex3f(-extent,-extent,+extent);
//            vboCube.glVertex3f(+extent,-extent,-extent);
//            vboCube.glVertex3f(+extent,-extent,+extent);
//            vboCube.glVertex3f(-extent,-extent,-extent);
//            vboCube.glNormal3f(0.0f, 1.0f, 0.0f);
//            vboCube.glNormal3f(0.0f, 1.0f, 0.0f);
//            vboCube.glNormal3f(0.0f, 1.0f, 0.0f);
//            vboCube.glNormal3f(0.0f, 1.0f, 0.0f);
//            vboCube.glVertex3f(-extent,+extent,+extent);
//            vboCube.glVertex3f(+extent,+extent,-extent);
//            vboCube.glVertex3f(+extent,+extent,+extent);
//            vboCube.glVertex3f(-extent,+extent,-extent);
//            vboCube.glNormal3f(1.0f, 0.0f, 0.0f);
//            vboCube.glNormal3f(1.0f, 0.0f, 0.0f);
//            vboCube.glNormal3f(1.0f, 0.0f, 0.0f);
//            vboCube.glNormal3f(1.0f, 0.0f, 0.0f);
//            vboCube.glVertex3f(+extent,-extent,+extent);
//            vboCube.glVertex3f(+extent,+extent,-extent);
//            vboCube.glVertex3f(+extent,+extent,+extent);
//            vboCube.glVertex3f(+extent,-extent,-extent);
//            vboCube.glNormal3f( -1.0f, 0.0f, 0.0f);
//            vboCube.glNormal3f( -1.0f, 0.0f, 0.0f);
//            vboCube.glNormal3f( -1.0f, 0.0f, 0.0f);
//            vboCube.glNormal3f( -1.0f, 0.0f, 0.0f);
//            vboCube.glVertex3f(-extent,-extent,+extent);
//            vboCube.glVertex3f(-extent,+extent,-extent);
//            vboCube.glVertex3f(-extent,+extent,+extent);
//            vboCube.glVertex3f(-extent,-extent,-extent);
//            vboCube.glEnd(gl, false);
//        }
//        vboCube.draw(gl, cubeIndices, true);
//	}
//    private static final byte[] s_cubeIndices =
//        {
//            0, 3, 1, 2, 0, 1, /* front  */
//            6, 5, 4, 5, 7, 4, /* back   */
//            8, 11, 9, 10, 8, 9, /* top    */
//            15, 12, 13, 12, 14, 13, /* bottom */
//            16, 19, 17, 18, 16, 17, /* right  */
//            23, 20, 21, 20, 22, 21 /* left   */
//    };
//    private final ByteBuffer cubeIndices= Buffers.newDirectByteBuffer(s_cubeIndices);
//
//
//
//	private static GLUquadric cylinder;
//	private static GLUquadric sphere;
//
//	private static class SphereKey {
//		float radius;
//
//		SphereKey() {
//		}
//
//		SphereKey(SphereKey key) {
//			radius = key.radius;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (!(obj instanceof SphereKey other)) return false;
//			return radius == other.radius;
//		}
//
//		@Override
//		public int hashCode() {
//			return Float.floatToIntBits(radius);
//		}
//	}
//
//	private static final Map<SphereKey,ImmModeSink> sphereDisplayLists = new HashMap<>();
//	private static final SphereKey sphereKey = new SphereKey();
//
//	public void drawSphere(GL gl, float radius) {
//        if(sphere==null) {
//            sphere = glu.gluNewQuadric();
//            sphere.setImmMode(!VBO_CACHE);
//        }
//		sphereKey.radius = radius;
//		ImmModeSink vbo = sphereDisplayLists.get(sphereKey);
//		if (vbo == null) {
//			glu.gluSphere(sphere, radius, 8, 8);
//            if(VBO_CACHE) {
//                vbo = sphere.replaceImmModeSink();
//                sphereDisplayLists.put(new SphereKey(sphereKey), vbo);
//            }
//		}
//
//        if(VBO_CACHE && null!=vbo) {
//            vbo.draw(gl, true);
//        }
//	}
//
//
//
//
//	private static class CylinderKey {
//		float radius;
//		float halfHeight;
//
//		CylinderKey() {
//		}
//
//		CylinderKey(CylinderKey key) {
//			radius = key.radius;
//			halfHeight = key.halfHeight;
//		}
//
//		void set(float radius, float halfHeight) {
//			this.radius = radius;
//			this.halfHeight = halfHeight;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (!(obj instanceof CylinderKey other)) return false;
//			if (radius != other.radius) return false;
//            return halfHeight == other.halfHeight;
//        }
//
//		@Override
//		public int hashCode() {
//			int hash = 7;
//			hash = 23 * hash + Float.floatToIntBits(radius);
//			hash = 23 * hash + Float.floatToIntBits(halfHeight);
//			return hash;
//		}
//	}
//
//	private static final Map<CylinderKey,ImmModeSink> cylinderDisplayLists = new HashMap<>();
//	private static final CylinderKey cylinderKey = new CylinderKey();
//
//	public void drawCylinder(GL2 gl, float radius, float halfHeight, int upAxis) {
//        if(cylinder==null) {
//            cylinder = glu.gluNewQuadric();
//            cylinder.setImmMode(!VBO_CACHE);
//        }
//		gl.glPushMatrix();
//		switch (upAxis) {
//			case 0:
//				gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
//				gl.glTranslatef(0.0f, 0.0f, -halfHeight);
//				break;
//			case 1:
//				gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
//				gl.glTranslatef(0.0f, 0.0f, -halfHeight);
//				break;
//			case 2:
//				gl.glTranslatef(0.0f, 0.0f, -halfHeight);
//				break;
//			default:
//                assert (false);
//        }
//
//
//
//
//
//		cylinderKey.set(radius, halfHeight);
//		ImmModeSink vbo = cylinderDisplayLists.get(cylinderKey);
//		if (vbo == null) {
//			glu.gluQuadricDrawStyle(cylinder, GLU.GLU_FILL);
//			glu.gluQuadricNormals(cylinder, GLU.GLU_SMOOTH);
//			glu.gluCylinder(cylinder, radius, radius, 2.0f * halfHeight, 15, 10);
//            if(VBO_CACHE) {
//                vbo = cylinder.replaceImmModeSink();
//                cylinderDisplayLists.put(new CylinderKey(cylinderKey), vbo);
//            }
//		}
//
//        if(VBO_CACHE && null!=vbo) {
//            vbo.draw(gl, true);
//        }
//
//		gl.glPopMatrix();
//	}
//
//
//
//	public void drawString() {
//        /*
//		if (font != null) {
//			FontRender.drawString(gl, font, s, x, y, red, green, blue);
//		} */
//	}
//
//}