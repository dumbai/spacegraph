package spacegraph.space3d.raytrace;

import jcog.math.v3d;

import static jcog.math.v3d.Z_AXIS;

public final class Camera extends Ray3 {
    public double fov;
    private double size;

    public Camera(v3d position, v3d direction, double fov, double size) {
        super(position, direction.normalize());
        this.fov = fov;
        this.size = size;
    }

    
    public Ray3 ray(double x, double y, double aspectRatio) {
        v3d xAxis = direction.cross(v3d.Z_AXIS).normalizeThis();
        v3d yAxis = xAxis.cross(direction);

        double widthNear = size;
        double heightNear = widthNear / aspectRatio;

        double widthFar = jcog.Util.fma(2, Math.tan(fov / 2 / 180 * Math.PI), widthNear);
        double heightFar = widthFar / aspectRatio;

        v3d originNear = position.
                addScale(xAxis, -widthNear / 2).
                addScale(yAxis, -heightNear / 2);

        v3d originFar = direction.add(position).
            addScale(xAxis, -widthFar/2).
            addScale(yAxis, -heightFar/2);

        v3d pointNear = originNear.
                addScale(xAxis,x * widthNear).
                addScale(yAxis,y * heightNear);
        v3d pointFar = originFar.
                addScale(xAxis, x * widthFar).
                addScale(yAxis, y * heightFar);

        return new Ray3(pointNear, pointFar.minus(pointNear).normalizeThis());
    }

    public void move(v3d keyboardVector) {
        position.addThis(direction.scale(keyboardVector.y));
        position.addThis(direction.cross(v3d.Z_AXIS).normalize().scale(keyboardVector.x));
    }

    public void rotate(double dx, double dy) {
        double sin = direction.z;
        double verticalAngle = Math.asin(sin) / Math.PI * 180;
        
        if (verticalAngle + dy > 89) {
            dy = 89 - verticalAngle;
        } else if (verticalAngle + dy < -89) {
            dy = -89 - verticalAngle;
        }
        double cos = Math.sqrt(1 - sin*sin);
        double sinSliver = Math.sin(dx / 2 / 180 * Math.PI);
        double cosSliver = Math.cos(dx / 2 / 180 * Math.PI);
        v3d hRotTangent = direction.cross(Z_AXIS).normalizeThis(2 * cos * cosSliver * sinSliver);
        v3d hRotRadius = hRotTangent.cross(Z_AXIS).normalizeThis(2 * cos * sinSliver * sinSliver);
        sinSliver = Math.sin(dy / 2 / 180 * Math.PI);
        cosSliver = Math.cos(dy / 2 / 180 * Math.PI);
        v3d vRotTangent = direction.cross(direction.cross(Z_AXIS)).normalizeThis(-2 * cosSliver * sinSliver);
        v3d vRotRadius = direction.scale(2 * sinSliver * sinSliver);
        direction.addThis(hRotTangent);
        direction.addThis(hRotRadius);
        direction.addThis(vRotTangent);
        direction.addThis(vRotRadius);
        direction.normalizeThis();
    }

    public boolean update(Input input, double CAMERA_EPSILON) {
        v3d cameraPos = position.clone(), cameraDir = direction.clone();
        rotate(input.getDeltaMouseX() / 2.0, -input.getDeltaMouseY() / 2.0);
        move(input.getKeyboardVector());

        return !cameraPos.equals(position, CAMERA_EPSILON) || !cameraDir.equals(direction, CAMERA_EPSILON);
    }
}