package spacegraph.space2d.dyn2d;


import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.dynamics.Body2D;
import spacegraph.space2d.phys.dynamics.BodyDef;
import spacegraph.space2d.phys.dynamics.BodyType;
import spacegraph.space2d.phys.dynamics.Dynamics2D;
import spacegraph.space2d.phys.dynamics.contacts.Position;
import spacegraph.space2d.phys.particle.ParticleGroupDef;
import spacegraph.space2d.phys.particle.ParticleType;

import java.util.function.Consumer;

public class ParticlesTest implements Consumer<Dynamics2D> {
    @Override
    public void accept(Dynamics2D m_world) {

        Body2D ground = m_world.bodies().iterator().next(); 
        
        {
            PolygonShape shape = new PolygonShape();
            v2[] vertices =
                    {new Position(-40, -10), new Position(40, -10), new Position(40, 0), new Position(-40, 0)};
            shape.set(vertices, 4);
            ground.addFixture(shape, 0.0f);
        }

        {
            PolygonShape shape = new PolygonShape();
            v2[] vertices =
                    {new Position(-40, -1), new Position(-20, -1), new Position(-20, 20), new Position(-40, 30)};
            shape.set(vertices, 4);
            ground.addFixture(shape, 0.0f);
        }

        {
            PolygonShape shape = new PolygonShape();
            v2[] vertices = {new v2(20, -1), new v2(40, -1), new v2(40, 30), new v2(20, 20)};
            shape.set(vertices, 4);
            ground.addFixture(shape, 0.0f);
        }

        m_world.setParticleRadius(0.35f);
        m_world.setParticleDamping(0.2f);

        {
            CircleShape shape = new CircleShape();
            shape.center.set(0, 30);
            shape.skinRadius = 20;
            ParticleGroupDef pd = new ParticleGroupDef();
            pd.flags = ParticleType.b2_waterParticle;
            pd.shape = shape;
            m_world.addParticles(pd);
        }

        {
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            Body2D body = m_world.addBody(bd);
            CircleShape shape = new CircleShape();
            shape.center.set(0, 80);
            shape.skinRadius = 5;
            body.addFixture(shape, 0.5f);
        }

    }

}
