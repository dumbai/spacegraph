//package spacegraph.video;
//
//import com.jogamp.opengl.GL;
//import com.jogamp.opengl.GL2;
//import com.jogamp.opengl.util.ImmModeSink;
//import com.jogamp.opengl.util.gl2.GLUT;
//import jcog.data.list.Lst;
//import jcog.math.v3;
//import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
//import spacegraph.space3d.phys.collision.broad.BroadphaseNativeType;
//import spacegraph.space3d.phys.math.Transform;
//import spacegraph.space3d.phys.math.VectorUtil;
//import spacegraph.space3d.phys.shape.*;
//import spacegraph.space3d.phys.util.BulletStack;
//
//import static jcog.math.v3.v;
//import static spacegraph.video.Draw.pop;
//import static spacegraph.video.Draw.push;
//
//public class Draw3D {
//
//    private static final GLSRT glsrt = new GLSRT(Draw.glu);
//    public static final GLUT glut = new GLUT();
//    @Deprecated static final BulletStack stack = new BulletStack();
//    private static final v3 a = new v3();
//    private static final v3 b = new v3();
//    private static final float[] glMat = new float[16];
//
//    public static final int colorBLACK = Draw.rgbInt(0,0,0);
//    public static final int colorWHITE = Draw.rgbInt(1,1,1);
//
//    public static void transform(GL2 gl, Transform trans) {
//        gl.glMultMatrixf(trans.getOpenGLMatrix(glMat), 0);
//    }
//
//
//    public static void drawCoordSystem(GL gl) {
//        ImmModeSink vbo = ImmModeSink.createFixed(3 * 4,
//                3, GL.GL_FLOAT,
//                4, GL.GL_FLOAT,
//                0, GL.GL_FLOAT,
//                0, GL.GL_FLOAT,
//                GL.GL_STATIC_DRAW);
//        vbo.glBegin(GL.GL_LINES);
//        vbo.glColor4f(1f, 1f, 1f, 1f);
//        vbo.glVertex3f(0f, 0f, 0f);
//        vbo.glColor4f(1f, 1f, 1f, 1f);
//        vbo.glVertex3f(1f, 0f, 0f);
//        vbo.glColor4f(1f, 1f, 1f, 1f);
//        vbo.glVertex3f(0f, 0f, 0f);
//        vbo.glColor4f(1f, 1f, 1f, 1f);
//        vbo.glVertex3f(0f, 1f, 0f);
//        vbo.glColor4f(1f, 1f, 1f, 1f);
//        vbo.glVertex3f(0f, 0f, 0f);
//        vbo.glColor4f(1f, 1f, 1f, 1f);
//        vbo.glVertex3f(0f, 0f, 1f);
//        vbo.glEnd(gl);
//    }
//    public static void draw(GL2 gl, CollisionShape shape) {
//
//
//        BroadphaseNativeType shapeType = shape.getShapeType();
//        if (shapeType == BroadphaseNativeType.COMPOUND_SHAPE_PROXYTYPE) {
//            CompoundShape compoundShape = (CompoundShape) shape;
//            //Transform childTrans = new Transform();
//            for (int i = compoundShape.size() - 1; i >= 0; i--) {
////                stack.transforms.get(
////                    compoundShape.getChildTransform(i)
////                );
//
//                CollisionShape colShape = compoundShape.getChildShape(i);
//
//                push(gl);
//                stack.pushCommonMath();
//                draw(gl, colShape);
//                stack.popCommonMath();
//                pop(gl);
//            }
//        } else {
//            boolean useWireframeFallback = true;
//            switch (shapeType) {
//                case BOX_SHAPE_PROXYTYPE:
//                    SimpleBoxShape boxShape = (SimpleBoxShape) shape;
//                    v3 a = boxShape.implicitShapeDimensions;
//
//                    gl.glScalef(2f * a.x, 2f * a.y, 2f * a.z);
//
//                    glut.glutSolidCube(1f);
//
//                    useWireframeFallback = false;
//                    break;
//                case CONVEX_HULL_SHAPE_PROXYTYPE:
//                case TRIANGLE_SHAPE_PROXYTYPE:
//                case TETRAHEDRAL_SHAPE_PROXYTYPE:
//
//
//                    if (shape.isConvex()) {
//                        ConvexShape convexShape = (ConvexShape) shape;
//                        if (shape.getUserPointer() == null) {
//
//                            ShapeHull hull = new ShapeHull(convexShape);
//
//
//                            float margin = shape.getMargin();
//                            hull.buildHull(margin);
//                            convexShape.setUserPointer(hull);
//
//
//                        }
//
//                        if (shape.getUserPointer() != null) {
//                            ShapeHull hull = (ShapeHull) shape.getUserPointer();
//
//                            int tris = hull.numTriangles();
//                            if (tris > 0) {
//                                IntArrayList idx = hull.getIndexPointer();
//                                Lst<v3> vtx = hull.getVertexPointer();
//
//                                v3 normal = v();
//                                v3 tmp1 = v();
//                                v3 tmp2 = v();
//
//                                gl.glBegin(GL.GL_TRIANGLES);
//
//                                int index = 0;
//                                for (int i = 0; i < tris; i++) {
//
//                                    v3 v1 = vtx.get(idx.get(index++));
//                                    v3 v2 = vtx.get(idx.get(index++));
//                                    v3 v3 = vtx.get(idx.get(index++));
//
//                                    tmp1.subbed(v3, v1);
//                                    tmp2.subbed(v2, v1);
//                                    normal.cross(tmp1, tmp2);
//                                    normal.normalize();
//
//                                    gl.glNormal3f(normal.x, normal.y, normal.z);
//                                    gl.glVertex3f(v1.x, v1.y, v1.z);
//                                    gl.glVertex3f(v2.x, v2.y, v2.z);
//                                    gl.glVertex3f(v3.x, v3.y, v3.z);
//
//                                }
//
//                                gl.glEnd();
//                            }
//                        }
//                    }
//
//                    useWireframeFallback = false;
//                    break;
//                case SPHERE_SHAPE_PROXYTYPE: {
//                    SphereShape sphereShape = (SphereShape) shape;
//                    float radius = sphereShape.getMargin();
//
//
//                    glsrt.drawSphere(gl, radius);
//                            /*
//                            glPointSize(10f);
//							glBegin(gl.GL_POINTS);
//							glVertex3f(0f, 0f, 0f);
//							glEnd();
//							glPointSize(1f);
//							*/
//                    useWireframeFallback = false;
//                    break;
//                }
//                case CAPSULE_SHAPE_PROXYTYPE: {
//                    CapsuleShape capsuleShape = (CapsuleShape) shape;
//                    float radius = capsuleShape.getRadius();
//                    float halfHeight = capsuleShape.getHalfHeight();
//                    int upAxis = 1;
//
//                    glsrt.drawCylinder(gl, radius, halfHeight, upAxis);
//
//                    gl.glTranslatef(0f, -halfHeight, 0f);
//
//
//                    glsrt.drawSphere(gl, radius);
//                    gl.glTranslatef(0f, 2f * halfHeight, 0f);
//
//
//                    glsrt.drawSphere(gl, radius);
//                    useWireframeFallback = false;
//                    break;
//                }
//                case MULTI_SPHERE_SHAPE_PROXYTYPE:
//                    break;
//
//
//                case CONVEX_TRIANGLEMESH_SHAPE_PROXYTYPE:
//                    useWireframeFallback = false;
//                    break;
//
//                case CONVEX_SHAPE_PROXYTYPE:
//                case CYLINDER_SHAPE_PROXYTYPE:
//                    CylinderShape cylinder = (CylinderShape) shape;
//                    int upAxis = cylinder.getUpAxis();
//
//                    float radius = cylinder.getRadius();
//                    float halfHeight = VectorUtil.coord(cylinder.getHalfExtentsWithMargin(new v3()), upAxis);
//
//                    glsrt.drawCylinder(gl, radius, halfHeight, upAxis);
//
//                    break;
//                default:
//
//            }
//
//
//            if (useWireframeFallback) {
//
//                if (shape.isPolyhedral()) {
//                    PolyhedralConvexShape polyshape = (PolyhedralConvexShape) shape;
//
//                    ImmModeSink vbo = ImmModeSink.createFixed(polyshape.getNumEdges() + 3,
//                            3, GL.GL_FLOAT,
//                            0, GL.GL_FLOAT,
//                            0, GL.GL_FLOAT,
//                            0, GL.GL_FLOAT, GL.GL_STATIC_DRAW);
//
//                    vbo.glBegin(GL.GL_LINES);
//
//
//                    for (int i = 0; i < polyshape.getNumEdges(); i++) {
//                        polyshape.getEdge(i, a, b);
//
//                        vbo.glVertex3f(a.x, a.y, a.z);
//                        vbo.glVertex3f(b.x, b.y, b.z);
//                    }
//                    vbo.glEnd(gl);
//
//
//                }
//            }
//
//
//            if (shape.isConcave()) {
//                ConcaveShape concaveMesh = (ConcaveShape) shape;
//
//
//                a.set(1e30f, 1e30f, 1e30f);
//                b.set(-1e30f, -1e30f, -1e30f);
//
//                GlDrawcallback drawCallback = new GlDrawcallback(gl);
//                drawCallback.wireframe = false;
//
//                concaveMesh.processAllTriangles(drawCallback, b, a);
//            }
//        }
//
//
//    }
//
//    private static class GlDrawcallback extends TriangleCallback {
//        private final GL gl;
//        boolean wireframe;
//
//        GlDrawcallback(GL gl) {
//            this.gl = gl;
//        }
//
//        @Override
//        public void processTriangle(v3[] triangle, int partId, int triangleIndex) {
//            ImmModeSink vbo = ImmModeSink.createFixed(10,
//                    3, GL.GL_FLOAT,
//                    4, GL.GL_FLOAT,
//                    0, GL.GL_FLOAT,
//                    0, GL.GL_FLOAT, GL.GL_STATIC_DRAW);
//            if (wireframe) {
//                vbo.glBegin(GL.GL_LINES);
//                vbo.glColor4f(1, 0, 0, 1);
//                vbo.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//                vbo.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//                vbo.glColor4f(0, 1, 0, 1);
//                vbo.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//                vbo.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//                vbo.glColor4f(0, 0, 1, 1);
//                vbo.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//                vbo.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//            } else {
//                vbo.glBegin(GL.GL_TRIANGLES);
//                vbo.glColor4f(1, 0, 0, 1);
//                vbo.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
//                vbo.glColor4f(0, 1, 0, 1);
//                vbo.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
//                vbo.glColor4f(0, 0, 1, 1);
//                vbo.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
//            }
//            vbo.glEnd(gl);
//        }
//    }
//
//}
//
