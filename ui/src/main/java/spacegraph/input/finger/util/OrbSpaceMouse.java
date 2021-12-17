//package spacegraph.input.finger.util;
//
//import com.jogamp.newt.event.KeyEvent;
//import com.jogamp.newt.event.KeyListener;
//import com.jogamp.newt.event.MouseEvent;
//import jcog.math.v3;
//import spacegraph.input.finger.Finger;
//import spacegraph.space3d.SpaceGraph3D;
//import spacegraph.space3d.Spatial;
//import spacegraph.space3d.phys.Body3D;
//import spacegraph.space3d.phys.Collidable;
//import spacegraph.space3d.phys.collision.ClosestRay;
//import spacegraph.space3d.phys.constraint.Point2PointConstraint;
//import spacegraph.space3d.phys.constraint.TypedConstraint;
//import spacegraph.space3d.phys.math.Transform;
//
//import static jcog.math.v3.v;
//
///**
// * Created by me on 11/20/16.
// */
//public class OrbSpaceMouse extends SpaceMouse implements KeyListener {
//
//
//    private int mouseDragPrevX;
//    private int mouseDragPrevY;
//    private int mouseDragDX;
//    private int mouseDragDY;
//    private final v3 gOldPickingPos = v();
//
//    private TypedConstraint pickConstraint;
//
//    private Spatial pickedSpatial;
//    private Collidable picked;
//
//    private final Finger finger;
//
//    public OrbSpaceMouse(SpaceGraph3D g, Finger finger) {
//
//        super(g);
//
//        this.finger = finger;
//        g.video.addKeyListener(this);
//    }
//
//    boolean mouseClick(int button, float x, float y) {
//
//        switch (button) {
//            case MouseEvent.BUTTON3 -> {
//                Collidable co = pickCollidable(x, y);
//                if (co != null)
//                    space.camera(co.transform, co.shape().getBoundingRadius() * 2.5f);
//            }
//        }
//        return false;
//    }
//    @Override
//    public void mouseWheelMoved(MouseEvent e) {
//
//        float y = e.getRotation()[1];
//        if (y != 0) {
//
//        }
//    }
//
//
//    private void pickConstrain(int button, int state, int x, int y) {
//
//
//        switch (button) {
//            case MouseEvent.BUTTON1:
//
//                if (state == 1) {
//                    mouseGrabOn();
//                } else {
//                    mouseGrabOff();
//                }
//                break;
//            case MouseEvent.BUTTON2:
//            case MouseEvent.BUTTON3:
//                break;
//        }
//    }
//
//    private void mouseGrabOff() {
//        if (pickConstraint != null) {
//            space.dyn.removeConstraint(pickConstraint);
//            pickConstraint = null;
//
//            pickedBody.forceActivationState(Collidable.ACTIVE_TAG);
//            pickedBody.setDeactivationTime(0f);
//            pickedBody = null;
//        }
//
//
//    }
//
//
//    private ClosestRay mouseGrabOn() {
//
//
//        if (pickConstraint == null && pickedBody != null) {
//            pickedBody.setActivationState(Collidable.DISABLE_DEACTIVATION);
//
//            Body3D body = pickedBody;
//            v3 pickPos = new v3(rayCallback.hitPointWorld);
//
//            Transform tmpTrans = body.transform;
//            tmpTrans.invert();
//            v3 localPivot = new v3(pickPos);
//            tmpTrans.transform(localPivot);
//
//            Point2PointConstraint p2p = new Point2PointConstraint(body, localPivot);
//            p2p.impulseClamp = 3f;
//
//
//            gOldPickingPos.set(rayCallback.rayToWorld);
//            v3 eyePos = new v3(space.camPos);
//            v3 tmp = new v3();
//            tmp.subbed(pickPos, eyePos);
//            float gOldPickingDist = tmp.length();
//
//            p2p.tau = 0.1f;
//
//            space.dyn.addConstraint(p2p);
//            pickConstraint = p2p;
//
//
//        }
//
//        return rayCallback;
//
//    }
//
//
//    @Deprecated /* TODO probably rewrite */ private boolean mouseMotionFunc(int px, int py, short[] buttons) {
//
//
//        ClosestRay cray = pickRay(px, py);
//
//
//        /*System.out.println(mouseTouch.collisionObject + " touched with " +
//            Arrays.toString(buttons) + " at " + mouseTouch.hitPointWorld
//        );*/
//
//        Spatial prevPick = pickedSpatial;
//
//        picked = cray.collidable;
//        Spatial pickedSpatial = null;
//        if (picked != null) {
//            Object t = picked.data();
//            if (t instanceof Spatial) {
//                pickedSpatial = ((Spatial) t);
//                if (Spatial.onTouch(finger, picked, cray, buttons, space) != null) {
//
//
//                    clearDrag();
//
//                } else {
//
//                }
//
//
//            }
//        }
//
//
//        if ((pickConstraint != null) /*|| (directDrag != null)*/) {
//
//
//        } else {
//
//
//        }
//
//        if (prevPick != pickedSpatial) {
//            if (prevPick != null) {
//                prevPick.onUntouch(space.video);
//            }
//            this.pickedSpatial = pickedSpatial;
//        }
//
//        return false;
//
//    }
//
//    @Deprecated
//    public void clearDrag() {
//        mouseDragDX = mouseDragDY = 0;
//        mouseDragPrevX = mouseDragPrevY = -1;
//    }
//
//
//    @Override
//    public void mousePressed(MouseEvent e) {
//        if (e.isConsumed())
//            return;
//
//        mouseDragDX = mouseDragDY = 0;
//
//        int x = e.getX();
//        int y = e.getY();
//        if (!mouseMotionFunc(x, y, e.getButtonsDown())) {
//            pickConstrain(e.getButton(), 1, x, y);
//
//        }
//
//        e.setConsumed(true);
//    }
//
//    @Override
//    public void mouseReleased(MouseEvent e) {
//        if (e.isConsumed())
//            return;
//
//        int dragThresh = 1;
//        boolean dragging = Math.abs(mouseDragDX) < dragThresh;
//        if (dragging && mouseClick(e.getButton(), e.getX(), e.getY())) {
//
//        } else {
//
//            int x = e.getX();
//            int y = e.getY();
//            if (!mouseMotionFunc(x, y, e.getButtonsDown())) {
//                pickConstrain(e.getButton(), 0, x, y);
//            }
//
//        }
//        if (dragging)
//            clearDrag();
//
//        e.setConsumed(true);
//    }
//
//
//    @Override
//    public void mouseDragged(MouseEvent e) {
//        if (e.isConsumed())
//            return;
//
//        int x = e.getX();
//        int y = e.getY();
//
//        if (mouseDragPrevX >= 0) {
//            mouseDragDX = (x) - mouseDragPrevX;
//            mouseDragDY = (y) - mouseDragPrevY;
//        }
//
//        if (mouseMotionFunc(x, y, e.getButtonsDown())) {
//            e.setConsumed(true);
//        }
//
//        mouseDragPrevX = x;
//        mouseDragPrevY = y;
//
//    }
//
//    @Override
//    public void mouseMoved(MouseEvent e) {
//
//        if (mouseMotionFunc(e.getX(), e.getY(), e.getButtonsDown())) {
//            e.setConsumed(true);
//        }
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        if (pickedSpatial != null) {
//            pickedSpatial.onKey(picked, hitPoint, e.getKeyChar(), true);
//        }
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//        if (pickedSpatial != null) {
//            pickedSpatial.onKey(picked, hitPoint, e.getKeyChar(), false);
//        }
//    }
//}
