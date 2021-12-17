package spacegraph.space2d.dyn2d.fracture;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.dynamics.*;
import spacegraph.space2d.phys.fracture.PolygonFixture;
import spacegraph.space2d.phys.fracture.materials.Diffusion;

import java.util.function.Consumer;

/**
 * Testovaci scenar
 *
 * @author Marek Benovic
 */
public class StaticBody implements Consumer<Dynamics2D> {
    @Override
    public void accept(Dynamics2D w) {
        {
            BodyDef bodyDef2 = new BodyDef();
            bodyDef2.type = BodyType.STATIC;
            bodyDef2.position.set(10.0f, 0.0f); 
            bodyDef2.linearVelocity = new v2(0.0f, 0.0f);
            bodyDef2.angularVelocity = 0.0f; 
            Body2D newBody = w.addBody(bodyDef2);

            PolygonFixture pf = new PolygonFixture(new v2[]{
                    new v2(0.0f, 3.7f),
                    new v2(6.3f, 3.7f),
                    new v2(6.3f, 16.0f),
                    new v2(3.8f, 16.0f),
                    new v2(3.7f, 20.0f),
                    new v2(8.2f, 20.0f),
                    new v2(8.2f, 0.0f),
                    new v2(0.0f, 0.0f)
            });
            FixtureDef fd = new FixtureDef();
            fd.friction = 0.2f; 
            fd.restitution = 0.0f; 
            fd.density = 1.0f;
            fd.material = new Diffusion();
            fd.material.m_rigidity = 32.0f;
            newBody.addFixture(pf, fd);
        }

        {
            BodyDef bodyDefBullet = new BodyDef();
            bodyDefBullet.type = BodyType.DYNAMIC;
            bodyDefBullet.position.set(-20.0f, 18.0f); 
            bodyDefBullet.linearVelocity = new v2(100.0f, 0.0f);
            bodyDefBullet.angularVelocity = 0.0f; 
            Body2D bodyBullet = w.addBody(bodyDefBullet);

            CircleShape circleShape = new CircleShape();
            circleShape.skinRadius = 1.0f;
            Fixture fixtureBullet = bodyBullet.addFixture(circleShape, 2.0f);
            fixtureBullet.friction = 0.4f; 
            fixtureBullet.restitution = 0.1f; 
        }
    }

    @Override
    public String toString() {
        return "Static body";
    }
}
