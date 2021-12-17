package spacegraph.space2d.dyn2d.jbox2d;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.common.MathUtils;
import spacegraph.space2d.phys.dynamics.Body2D;
import spacegraph.space2d.phys.dynamics.BodyDef;
import spacegraph.space2d.phys.dynamics.Dynamics2D;
import spacegraph.space2d.phys.dynamics.FixtureDef;
import spacegraph.space2d.phys.dynamics.joints.ConstantVolumeJointDef;

import java.util.function.Consumer;

import static spacegraph.space2d.phys.dynamics.BodyType.DYNAMIC;

public class BlobTest4 implements Consumer<Dynamics2D> {

    @Override
    public void accept(Dynamics2D w) {


        {
            PolygonShape sd = new PolygonShape();
            sd.setAsBox(50.0f, 0.4f);

            BodyDef bd = new BodyDef();
            bd.position.set(0.0f, 0.0f);
            Body2D ground = w.addBody(bd);
            ground.addFixture(sd, 0.0f);

            sd.setAsBox(0.4f, 50.0f, new v2(-10.0f, 0.0f), 0.0f);
            ground.addFixture(sd, 0.0f);
            sd.setAsBox(0.4f, 50.0f, new v2(10.0f, 0.0f), 0.0f);
            ground.addFixture(sd, 0.0f);
        }

        ConstantVolumeJointDef cvjd = new ConstantVolumeJointDef();

        float cx = 0.0f;
        float cy = 10.0f;
        float rx = 5.0f;
        float ry = 5.0f;
        int nBodies = 40;
        float bodyRadius = 0.25f;
        for (int i = 0; i < nBodies; ++i) {
            float angle = MathUtils.map(i, 0, nBodies, 0, 2 * 3.1415f);
            float x = cx + rx * (float) Math.sin(angle);
            float y = cy + ry * (float) Math.cos(angle);
            BodyDef bd = new BodyDef(DYNAMIC, new v2(x, y));
            bd.fixedRotation = true;

            Body2D body = w.addBody(bd);

            CircleShape cd = new CircleShape();
            cd.skinRadius = bodyRadius;

            FixtureDef fd = new FixtureDef();
            fd.shape = cd;
            fd.density = 1.0f;
            body.addFixture(fd);

            cvjd.addBody(body);
        }

        cvjd.frequencyHz = 10.0f;
        cvjd.dampingRatio = 0.9f;
        cvjd.collideConnected = false;
        w.addJoint(cvjd);

        {
            w.addBody(
                    new BodyDef(DYNAMIC, new v2(cx, cy + 15.0f)))
                    .addFixture(
                            new PolygonShape().setAsBox(3.0f, 1.5f, new v2(cx, cy + 15.0f), 0.0f),
                            1.0f);
        }
    }


}