package spacegraph.space2d.dyn2d.jbox2d;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.EdgeShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.dynamics.*;
import spacegraph.space2d.phys.dynamics.joints.RevoluteJointDef;

import java.util.function.Consumer;

/**
 * @author Daniel Murphy
 */
public class ChainTest implements Consumer<Dynamics2D> {

    @Override
    public void accept(Dynamics2D w) {


        Body2D ground = null;
        {
            ground = w.addBody(new BodyDef());

            EdgeShape shape = new EdgeShape();
            shape.set(new v2(-40.0f, 0.0f), new v2(40.0f, 0.0f));
            ground.addFixture(shape, 0.0f);
        }

        {

            FixtureDef box = new FixtureDef(
                    PolygonShape.box(0.6f, 0.125f), 20.0f, 0.2f);

            RevoluteJointDef jd = new RevoluteJointDef();
            jd.collideConnected = false;

            final float y = 25.0f;

            Body2D prevBody = ground;

            for (int i = 0; i < 30; ++i) {

                Body2D body = w.addBody(
                        new BodyDef(BodyType.DYNAMIC,
                                new v2(0.5f + i, y)),
                        box);

                w.addJoint(jd.initialize(prevBody, body, /* anchor */ new v2(i, y)));

                prevBody = body;
            }
        }
    }


}