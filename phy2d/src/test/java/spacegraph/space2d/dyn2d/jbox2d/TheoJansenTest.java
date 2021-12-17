package spacegraph.space2d.dyn2d.jbox2d;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.EdgeShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.common.MathUtils;
import spacegraph.space2d.phys.dynamics.*;
import spacegraph.space2d.phys.dynamics.contacts.Position;
import spacegraph.space2d.phys.dynamics.joints.DistanceJointDef;
import spacegraph.space2d.phys.dynamics.joints.RevoluteJoint;
import spacegraph.space2d.phys.dynamics.joints.RevoluteJointDef;

import java.util.function.Consumer;

public class TheoJansenTest implements Consumer<Dynamics2D> {
//    private static final long CHASSIS_TAG = 1;
//    private static final long WHEEL_TAG = 2;
//    private static final long MOTOR_TAG = 8;

    v2 m_offset = new Position();
    Body2D m_chassis;
    Body2D m_wheel;
    RevoluteJoint m_motorJoint;
    boolean m_motorOn;
    float m_motorSpeed;
    private Dynamics2D w;






































    @Override
    public void accept(Dynamics2D w) {

        this.w = w;
        
        m_offset.set(0.0f, 8.0f);
        m_motorSpeed = 2.0f;
        m_motorOn = true;
        v2 pivot = new Position(0.0f, 0.8f);

        
        {
            BodyDef bd = new BodyDef();
            Body2D ground = w.addBody(bd);

            EdgeShape shape = new EdgeShape();
            shape.set(new Position(-50.0f, 0.0f), new Position(50.0f, 0.0f));
            ground.addFixture(shape, 0.0f);

            shape.set(new Position(-50.0f, 0.0f), new Position(-50.0f, 10.0f));
            ground.addFixture(shape, 0.0f);

            shape.set(new Position(50.0f, 0.0f), new Position(50.0f, 10.0f));
            ground.addFixture(shape, 0.0f);
        }

        
        for (int i = 0; i < 40; ++i) {
            CircleShape shape = new CircleShape();
            shape.skinRadius = 0.25f;

            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.set(-40.0f + 2.0f * i, 0.5f);

            Body2D body = w.addBody(bd);
            body.addFixture(shape, 1.0f);
        }

        
        {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(2.5f, 1.0f);

            FixtureDef sd = new FixtureDef();
            sd.density = 1.0f;
            sd.shape = shape;
            sd.filter.groupIndex = -1;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.set(pivot).added(m_offset);
            m_chassis = w.addBody(bd);
            m_chassis.addFixture(sd);
        }

        {
            CircleShape shape = new CircleShape();
            shape.skinRadius = 1.6f;

            FixtureDef sd = new FixtureDef();
            sd.density = 1.0f;
            sd.shape = shape;
            sd.filter.groupIndex = -1;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.set(pivot).added(m_offset);
            m_wheel = w.addBody(bd);
            m_wheel.addFixture(sd);
        }

        {
            RevoluteJointDef jd = new RevoluteJointDef(m_motorOn);

            jd.initialize(m_wheel, m_chassis, pivot.addToNew(m_offset));
            jd.collideConnected = false;
            jd.motorSpeed = m_motorSpeed;
            jd.maxMotorTorque = 400.0f;
            jd.enableMotor = m_motorOn;
            m_motorJoint = w.addJoint(jd);
        }

        v2 wheelAnchor = pivot.addToNew(new Position(0.0f, -0.8f));
        createLeg(-1, wheelAnchor);
        createLeg(1, wheelAnchor);

        m_wheel.setTransform(m_wheel.getPosition(), 120.0f * MathUtils.PI / 180.0f);
        createLeg(-1, wheelAnchor);
        createLeg(1, wheelAnchor);

        m_wheel.setTransform(m_wheel.getPosition(), -120.0f * MathUtils.PI / 180.0f);
        createLeg(-1, wheelAnchor);
        createLeg(1, wheelAnchor);
    }

    void createLeg(float s, v2 wheelAnchor) {
        v2 p1 = new Position(5.4f * s, -6.1f);
        v2 p2 = new Position(7.2f * s, -1.2f);
        v2 p3 = new Position(4.3f * s, -1.9f);
        v2 p4 = new Position(3.1f * s, 0.8f);
        v2 p5 = new Position(6.0f * s, 1.5f);
        v2 p6 = new Position(2.5f * s, 3.7f);

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

            vertices[0] = new Position();
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
        bd1.position = m_offset;
        bd2.position = p4.addToNew(m_offset);

        bd1.angularDamping = 10.0f;
        bd2.angularDamping = 10.0f;

        Body2D body1 = w.addBody(bd1);
        Body2D body2 = w.addBody(bd2);

        body1.addFixture(fd1);
        body2.addFixture(fd2);

        DistanceJointDef djd = new DistanceJointDef();

        
        
        
        djd.dampingRatio = 0.5f;
        djd.frequencyHz = 10.0f;

        djd.initialize(body1, body2, p2.addToNew(m_offset), p5.addToNew(m_offset));
        w.addJoint(djd);

        djd.initialize(body1, body2, p3.addToNew(m_offset), p4.addToNew(m_offset));
        w.addJoint(djd);

        djd.initialize(body1, m_wheel, p3.addToNew(m_offset), wheelAnchor.addToNew(m_offset));
        w.addJoint(djd);

        djd.initialize(body2, m_wheel, p6.addToNew(m_offset), wheelAnchor.addToNew(m_offset));
        w.addJoint(djd);

        RevoluteJointDef rjd = new RevoluteJointDef(false);
        rjd.initialize(body2, m_chassis, p4.addToNew(m_offset));
        w.addJoint(rjd);
    }






















}