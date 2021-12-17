package spacegraph.space2d.dyn2d;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.common.MathUtils;
import spacegraph.space2d.phys.dynamics.*;
import spacegraph.space2d.phys.dynamics.joints.DistanceJointDef;
import spacegraph.space2d.phys.dynamics.joints.RevoluteJoint;
import spacegraph.space2d.phys.dynamics.joints.RevoluteJointDef;

import static java.lang.Math.abs;

public class TheoJansen {

    private final Dynamics2D world;
    private final v2 center;


    private final Body2D wheel;
    protected final Body2D chassis;
    public final RevoluteJoint motorJoint;
//
//    public final RevoluteJoint turretJoint;
//    protected final Explosives.Gun gun;

    public TheoJansen(Dynamics2D w, float scale) {

        this.center = new v2(0, 8 * scale);

        this.world = w;


        v2 pivot = new v2(scale * 0.0f, scale * 0.8f);















        
        {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(scale * 2.5f, scale * 1.0f);

            FixtureDef sd = new FixtureDef();
            sd.density = 1.0f;
            sd.shape = shape;
            sd.filter.groupIndex = -1;





            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.set(pivot).added(center);
            chassis = world.addBody(bd);
            chassis.addFixture(sd);
        }
//        {
//            gun = new Explosives.Gun(5*scale, world);
//            Body2D b = gun.barrel;
//            RevoluteJointDef jj = new RevoluteJointDef(chassis, b);
//            jj.referenceAngle = (float) (Math.PI);
//            jj.enableMotor = true;
//            jj.localAnchorB.setAt(b.getWorldPoint(new v2(-gun.barrelLength/4f, 0)));
//            jj.collideConnected = false;
//            turretJoint = (RevoluteJoint) world.addJoint(jj);
//            turretJoint.setMotorSpeed(1f);
//        }

        {
            CircleShape shape = new CircleShape();
            shape.skinRadius = scale * 1.6f;

            FixtureDef sd = new FixtureDef();
            sd.density = 1.0f;
            sd.shape = shape;
            sd.filter.groupIndex = -1;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.set(pivot).added(center);
            wheel = world.addBody(bd);
            wheel.addFixture(sd);
        }

        {
            RevoluteJointDef jd = new RevoluteJointDef(true);
            jd.initialize(wheel, chassis, pivot.addToNew(center));
            jd.motorSpeed = 0;
            jd.maxMotorTorque = 40.0f;
            motorJoint = world.addJoint(jd);
        }

        v2 wheelAnchor = pivot.addToNew(new v2(scale * 0.0f, scale * -0.8f));

        createLeg(-scale, wheelAnchor);
        createLeg(scale, wheelAnchor);

        wheel.setTransform(wheel.getPosition(), 120.0f * MathUtils.PI / 180.0f);
        createLeg(-scale, wheelAnchor);
        createLeg(scale, wheelAnchor);

        wheel.setTransform(wheel.getPosition(), -120.0f * MathUtils.PI / 180.0f);
        createLeg(-scale, wheelAnchor);
        createLeg(scale, wheelAnchor);
    }

    void createLeg(float s, v2 wheelAnchor) {
        v2 p1 = new v2(5.4f * s, -6.1f * abs(s));
        v2 p2 = new v2(7.2f * s, -1.2f * abs(s));
        v2 p3 = new v2(4.3f * s, -1.9f * abs(s));
        v2 p4 = new v2(3.1f * s, 0.8f * abs(s));
        v2 p5 = new v2(6.0f * s, 1.5f * abs(s));
        v2 p6 = new v2(2.5f * s, 3.7f * abs(s));

        FixtureDef fd1 = new FixtureDef();
        FixtureDef fd2 = new FixtureDef();
        fd1.filter.groupIndex = -1;
        fd2.filter.groupIndex = -1;
        fd1.density = 1.0f;
        fd2.density = 1.0f;

        PolygonShape poly1 = new PolygonShape();
        PolygonShape poly2 = new PolygonShape();

        v2[] vertices = new v2[3];
        if (s > 0.0f) {

            vertices[0] = p1;
            vertices[1] = p2;
            vertices[2] = p3;
            poly1.set(vertices, 3);

            vertices[0] = new v2();
            vertices[1] = p5.subClone(p4);
            vertices[2] = p6.subClone(p4);
        } else {

            vertices[0] = p1;
            vertices[1] = p3;
            vertices[2] = p2;
            poly1.set(vertices, 3);

            vertices[0] = new v2();
            vertices[1] = p6.subClone(p4);
            vertices[2] = p5.subClone(p4);
        }
        poly2.set(vertices, 3);

        fd1.shape = poly1;
        fd2.shape = poly2;



        BodyDef bd1 = new BodyDef(), bd2 = new BodyDef();
        bd1.type = BodyType.DYNAMIC;
        bd2.type = BodyType.DYNAMIC;
        bd1.position = center;
        bd2.position = p4.addToNew(center);

        bd1.angularDamping = 10.0f;
        bd2.angularDamping = 10.0f;


        Body2D body1 = world.addBody(bd1);
        Body2D body2 = world.addBody(bd2);

        body1.addFixture(fd1);
        body2.addFixture(fd2);

        DistanceJointDef djd = new DistanceJointDef();

        
        
        
        djd.dampingRatio = 0.5f;
        djd.frequencyHz = 10.0f;

        djd.initialize(body1, body2, p2.addToNew(center), p5.addToNew(center));
        world.addJoint(djd);

        djd.initialize(body1, body2, p3.addToNew(center), p4.addToNew(center));
        world.addJoint(djd);

        djd.initialize(body1, wheel, p3.addToNew(center), wheelAnchor.addToNew(center));
        world.addJoint(djd);

        djd.initialize(body2, wheel, p6.addToNew(center), wheelAnchor.addToNew(center));
        world.addJoint(djd);

        RevoluteJointDef rjd = new RevoluteJointDef(false);
        rjd.initialize(body2, chassis, p4.addToNew(center));
        world.addJoint(rjd);
    }
}