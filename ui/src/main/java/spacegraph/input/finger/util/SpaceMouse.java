//package spacegraph.input.finger.util;
//
//import com.jogamp.newt.event.MouseAdapter;
//import jcog.TODO;
//import jcog.math.v2;
//import jcog.math.v3;
//import org.jetbrains.annotations.Nullable;
//import spacegraph.input.finger.Finger;
//import spacegraph.space2d.widget.SpaceSurface;
//import spacegraph.space3d.SpaceGraph3D;
//import spacegraph.space3d.Spatial;
//import spacegraph.space3d.phys.Body3D;
//import spacegraph.space3d.phys.Collidable;
//import spacegraph.space3d.phys.collision.ClosestRay;
//import spacegraph.space3d.phys.collision.narrow.VoronoiSimplexSolver;
//
///**
// * 3D camera control
// */
//public abstract class SpaceMouse extends MouseAdapter {
//
//    final SpaceGraph3D space;
//    protected final ClosestRay rayCallback = new ClosestRay(((short) (1 << 7)));
//    public v3 hitPoint;
//    private final VoronoiSimplexSolver simplexSolver = new VoronoiSimplexSolver();
//    public Body3D pickedBody;
//
//    public final v3 target = new v3();
//    public final v3 origin = new v3();
//
//    protected SpaceMouse(SpaceGraph3D g) {
//        this.space = g;
//    }
//
//    public static Spatial pickSpatial(float x, float y) {
//        throw new TODO();
//    }
//
//    public @Nullable Collidable pickCollidable(float x, float y) {
//        ClosestRay c = pickRay(x, y);
//        return c.hasHit() ? c.collidable : null;
//    }
//
//    public ClosestRay pickRay(float x, float y) {
//
//        v3 fwd = space.camFwd.clone();
//        fwd.normalize();
//
//        v3 up = space.camUp.clone();
//        up.normalize();
//
//        v3 right = new v3();
//        right.cross(fwd, up);
//        right.normalized();
//
//
////        double nearScale = tan((space.fov * Math.PI / 180) ) * space.zNear;
////        double vLength = nearScale;
////        double hLength = nearScale * (space.right - space.left)/(space.top - space.bottom);
//        origin.set(space.camPos);
//
//        target.set(fwd);
//        target.scaled(space.zNear);
//        final double W = space.right - space.left;
//        final double H = space.top - space.bottom;
//
//        target.addScaled(right,
//            (float) ((x * W)/2)
//        );
//        target.addScaled(up,
//            (float) ((y * H)/2)
//        );
//
//        target.scaled(space.zFar / space.zNear);
//
//        target.added(origin);
//
//
//
//        ClosestRay r = new ClosestRay(origin, target);
//        space.dyn.rayTest(origin, target, r, simplexSolver);
//
//        pickedBody = null;
//        hitPoint = null;
//
//        if (r.hasHit()) {
////            System.out.println("ray: " + x + "," + y  + "\t => " + target + " " + r.hitPointWorld);
//            Body3D body = Body3D.ifDynamic(r.collidable);
//            if (body != null && (!(body.isStaticObject() || body.isKinematicObject()))) {
//                pickedBody = body;
//                hitPoint = r.hitPointWorld;
//            }
//        }
//
//        return r;
//    }
//
//    public ClosestRay pickRay(Finger fingerFrom2D, SpaceSurface spaceSurface) {
//        final v2 p = fingerFrom2D.posRelative(spaceSurface);
//        return pickRay((p.x - 0.5f) * 2, (p.y - 0.5f) * 2);
//    }
//}
