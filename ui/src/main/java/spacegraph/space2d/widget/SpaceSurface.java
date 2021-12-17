//package spacegraph.space2d.widget;
//
//import com.jogamp.opengl.GL2;
//import jcog.Util;
//import jcog.math.v2;
//import jcog.math.v3;
//import org.jetbrains.annotations.Nullable;
//import spacegraph.SpaceGraph;
//import spacegraph.input.finger.Finger;
//import spacegraph.input.finger.impl.MouseFinger;
//import spacegraph.input.finger.state.Dragging;
//import spacegraph.input.finger.util.FPSLook;
//import spacegraph.space2d.ReSurface;
//import spacegraph.space2d.Surface;
//import spacegraph.space2d.container.PaintSurface;
//import spacegraph.space3d.SpaceGraph3D;
//import spacegraph.space3d.phys.Body3D;
//import spacegraph.space3d.phys.shape.SimpleBoxShape;
//import spacegraph.space3d.widget.SurfacedCuboid;
//
///**
// * embedded 3d viewport for use on a 2d surface
// */
//public class SpaceSurface extends PaintSurface {
//
//    private final SpaceGraph3D space;
//
//    private final FPSLook fpsLook;
//
//    private final FingerAdapter fingerTo2D = new FingerAdapter();
//    private final Dragging fpsLookDrag = new Dragging(2) {
//
//        float speed = 0.2f;
//        private v2 start;
//
//        @Override
//        protected boolean starting(Finger f) {
//            start = f.posPixel.clone();
//            return true;
//        }
//
//        @Override
//        protected boolean drag(Finger f) {
//            v2 delta = f.posPixel.subClone(start).scaled(speed);
//            fpsLook.drag(delta.x, -delta.y);
//            return true;
//        }
//    };
//
//    public SpaceSurface(SpaceGraph3D space) {
//        this.space = space;
//        this.fpsLook = new FPSLook(space);
//        space.initInput();
//    }
//
//    @Override
//    protected void starting() {
//        fingerTo2D.enter();
//    }
//
//    @Override
//    protected void stopping() {
//        fingerTo2D.exit();
//    }
//
//    @Override
//    protected void paint(GL2 gl, ReSurface s) {
//        gl.glPushMatrix();
//
//        s.viewVolume(bounds);
//
//        space.renderVolumeEmbedded(bounds, s.frameDT, gl);
//
//        s.viewOrtho(); //restore ortho state
//
//        gl.glPopMatrix();
//    }
//
//    @Override
//    public Surface finger(Finger f) {
//        float wheel = f.rotationY(true);
//        if (wheel != 0)
//            space.camPos.addScaled(space.camFwd, wheel);
//
//        if (f.test(fpsLookDrag))
//            return this;
//
//        return fpsLook.pickRay(f, this).hasHit() ? _finger(f) : null;
//    }
//
//    private SpaceSurface _finger(Finger fingerFrom2D) {
//        Body3D body = fpsLook.pickedBody; assert (body != null);
//
//        Object s = body.data();
//        if (s instanceof SurfacedCuboid) {
//			return fingerCuboid((SurfacedCuboid) s, body, fingerFrom2D);
//		} else {
//            //TODO other shapes
//        }
//
//        return null;
//    }
//
//	@Nullable
//	private SpaceSurface fingerCuboid(SurfacedCuboid ss, Body3D co, Finger fingerFrom2D) {
//		Surface front = ss.front;
//		if (front != null) {
//			v3 local = ss.transform.untransform(fpsLook.hitPoint.clone());
//			if (local.x >= -1 && local.x <= +1 && local.y >= -1 && local.y <= +1) {
//
//				SimpleBoxShape sss = (SimpleBoxShape) (ss.shape);
//				float zFront = sss.z() / 2;
//				float radiusTolerance = 0.25f * co.shape().getBoundingRadius();
//
//				if (Util.equals(local.z, zFront, radiusTolerance)) {
//					float localX = (local.x / sss.x()) + 0.5f, localY = (local.y / sss.y()) + 0.5f;
//
//					fingerTo2D.posPixel.set(localX, localY);
//					fingerTo2D.posGlobal.set(localX, localY);
//					fingerTo2D.copyButtons(fingerFrom2D);
//					//Surface fingering = fingerTo2D.push(new v2(localX, localY), front::finger);
//					Surface fingering = fingerTo2D.finger(front::finger);//front.finger(fingerTo2D);
//					//fingerTo2D.exit();
//
//					if (fingering != null) return this; //absorb and shadow internal node
//				}
//			}
//		}
//		return null;
//	}
//
//	private static class FingerAdapter extends MouseFinger {
//
//        FingerAdapter() {
//            super(5);
//        }
//
//        @Override
//        protected void start(SpaceGraph x) {
//        }
//
//        @Override
//        protected void stop(SpaceGraph x) {
//        }
//    }
//}
