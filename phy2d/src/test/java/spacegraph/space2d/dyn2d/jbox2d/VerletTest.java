package spacegraph.space2d.dyn2d.jbox2d;

/*
/**
 * <p>Softbody square demo is showing how to create a 2D square mesh out of
 * verlet particles and make it stable enough to avoid total structural
 * deformation by including an inner skeleton.</p>
 *
 * <p>Usage: move mouse to drag/deform the square</p>
 */

/*
 * Copyright (c) 2008-2009 Karsten Schmidt
 *
 * This demo & library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * http:
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import jcog.data.list.Lst;
import jcog.math.v2;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.dynamics.*;
import spacegraph.space2d.phys.dynamics.joints.RopeJoint;
import spacegraph.space2d.phys.dynamics.joints.RopeJointDef;

import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.sqrt;
import static jcog.Util.sqr;

public class VerletTest implements Consumer<Dynamics2D> {

    int DIM=4;
    float REST_LENGTH=3;
    float STRENGTH = 0.0125f;
    float INNER_STRENGTH = 0.013f;

    @Override
    public void accept(Dynamics2D w) {
        
        
        List<Body2D> particles = new Lst();
        for(int y=0,idx=0; y<DIM; y++) {
            for(int x=0; x<DIM; x++) {
                Body2D p = w.addBody(new BodyDef(BodyType.DYNAMIC),
                        new FixtureDef(new CircleShape(REST_LENGTH/ 4.0f), 0.1f, 0.0f));
                p.setTransform(new v2(x * REST_LENGTH, y * REST_LENGTH), 0);
                particles.add(p);

                if (x>0) {
                    spring(w, p, particles.get(idx - 1), STRENGTH, REST_LENGTH);
                }
                if (y>0) {
                    spring(w, p, particles.get(idx - DIM), STRENGTH, REST_LENGTH);
                }
                idx++;
            }
        }

        spring(w, particles.get(0), particles.get(particles.size()-1),
                INNER_STRENGTH, (float)sqrt(sqr(REST_LENGTH*(DIM-1))*2));
        spring(w, particles.get(DIM-1), particles.get(particles.size()-DIM),
                INNER_STRENGTH, (float)sqrt(sqr(REST_LENGTH*(DIM-1))*2));




    }

    public static void spring(Dynamics2D w, Body2D p, Body2D q, float STRENGTH, float REST_LENGTH) {





        RopeJoint j = w.addJoint(new RopeJointDef(p, q));
        j.setTargetLength(REST_LENGTH);
        j.setPositionFactor(STRENGTH);
        
        






    }

}