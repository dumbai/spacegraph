package spacegraph.space2d.dyn2d.fracture;

import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.dynamics.*;
import spacegraph.space2d.phys.fracture.materials.Diffusion;

import java.util.function.Consumer;

/**
 * Testovaci scenar
 *
 * @author Marek Benovic
 */
public class Cube implements Consumer<Dynamics2D> {
    @Override
    public void accept(Dynamics2D w) {
        {
            BodyDef bodyDef2 = new BodyDef();
            bodyDef2.type = BodyType.DYNAMIC;
            bodyDef2.position.set(10.0f, 10.0f); 
            bodyDef2.angle = -0.6f; 
            bodyDef2.linearVelocity = new v2(0.0f, 0.0f);
            bodyDef2.angularVelocity = 0.0f; 
            Body2D newBody = w.addBody(bodyDef2);
            PolygonShape shape2 = new PolygonShape();
            shape2.setAsBox(2.5f, 2.5f);
            Fixture f = newBody.addFixture(shape2, 1.0f);
            f.friction = 0.5f; 
            f.restitution = 0.0f; 
            f.material = new Diffusion();
            f.material.m_rigidity = 8.0f;
        }
    }

    @Override
    public String toString() {
        return "Cube";
    }
}
