/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package spacegraph.space2d.phys.dynamics.joints;

import jcog.math.v2;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.dynamics.Body2D;
import spacegraph.space2d.phys.dynamics.Dynamics2D;
import spacegraph.space2d.phys.dynamics.SolverData;

public class ConstantVolumeJoint extends Joint {

    private final Body2D[] bodies;
    private float targetVolume;

    private final v2[] normals;
    private float m_impulse = 0.0f;

    private final Dynamics2D world;

    private final DistanceJoint[] distanceJoints;

    public Body2D[] getBodies() {
        return bodies;
    }

    public DistanceJoint[] getJoints() {
        return distanceJoints;
    }

    public ConstantVolumeJoint(Dynamics2D argWorld, ConstantVolumeJointDef def) {
        super(argWorld.pool, def);
        world = argWorld;
        int n = def.bodies.size();
        if (n <= 2) {
            throw new IllegalArgumentException(
                    "You cannot create a constant volume joint with less than three bodies.");
        }
        bodies = def.bodies.toArray(new Body2D[n]);

        float[] targetLengths = new float[n];
        for (int i = 0; i < targetLengths.length; ++i) {
            int next = i == targetLengths.length - 1 ? 0 : i + 1;
            float dist = bodies[i].getWorldCenter().subClone(bodies[next].getWorldCenter()).length();
            targetLengths[i] = dist;
        }
        targetVolume = getBodyArea();

        if (def.joints != null && def.joints.size() != n) {
            throw new IllegalArgumentException(
                    "Incorrect joint definition.  Joints have to correspond to the bodies");
        }
        if (def.joints == null) {
            DistanceJointDef djd = new DistanceJointDef();
            distanceJoints = new DistanceJoint[bodies.length];
            for (int i = 0; i < targetLengths.length; ++i) {
                int next = i == targetLengths.length - 1 ? 0 : i + 1;
                djd.frequencyHz = def.frequencyHz;
                djd.dampingRatio = def.dampingRatio;
                djd.collideConnected = def.collideConnected;
                djd.initialize(bodies[i], bodies[next], bodies[i].getWorldCenter(),
                        bodies[next].getWorldCenter());
                distanceJoints[i] = world.addJoint(djd);
            }
        } else {
            distanceJoints = def.joints.toArray(new DistanceJoint[0]);
        }

        normals = new v2[bodies.length];
        for (int i = 0; i < normals.length; ++i) {
            normals[i] = new v2();
        }
    }

    @Override
    public void destructor() {
        for (DistanceJoint distanceJoint : distanceJoints) {
            world.removeJoint(distanceJoint);
        }
    }

    private float getBodyArea() {
        float area = 0.0f;
        for (int i = 0; i < bodies.length; ++i) {
            int next = i == bodies.length - 1 ? 0 : i + 1;
            v2 ic = bodies[i].getWorldCenter();
            v2 nc = bodies[next].getWorldCenter();
            area += ic.x * nc.y - nc.x * ic.y;
        }
        area /= 2;
        return area;
    }

    private float getSolverArea(v2[] positions) {
        float area = 0.0f;
        for (int i = 0; i < bodies.length; ++i) {
            int next = i == bodies.length - 1 ? 0 : i + 1;
            v2 pi = positions[bodies[i].island];
            v2 pn = positions[bodies[next].island];
            area += pi.x * pn.y - pn.x * pi.y;
        }
        area /= 2;
        return area;
    }

    private boolean constrainEdges(v2[] positions) {
        float perimeter = 0.0f;
        int bodyCount = bodies.length;
        for (int i = 0; i < bodyCount; ++i) {
            int next = i == bodyCount - 1 ? 0 : i + 1;
            v2 pn = positions[bodies[next].island];
            v2 pi = positions[bodies[i].island];
            float dx = pn.x - pi.x;
            float dy = pn.y - pi.y;
            float distSq = dx * dx + dy * dy;
            v2 ni = normals[i];
            if (distSq < Settings.EPSILONsqr) {
                ni.setZero();
            } else {
                float dist = (float) Math.sqrt(distSq);

                ni.x = dy / dist;
                ni.y = -dx / dist;
                perimeter += dist;
            }

        }

        v2 delta = pool.popVec2();

        float deltaArea = targetVolume - getSolverArea(positions);
        float toExtrude = 0.5f * deltaArea / perimeter;

        boolean done = true;
        for (int i = 0; i < bodyCount; ++i) {
            int next = i == bodyCount - 1 ? 0 : i + 1;
            delta.set(toExtrude * (normals[i].x + normals[next].x), toExtrude
                    * (normals[i].y + normals[next].y));

            float normSqrd = delta.lengthSquared();
            if (normSqrd > Settings.maxLinearCorrection * Settings.maxLinearCorrection) {
                delta.scaled(Settings.maxLinearCorrection / (float) Math.sqrt(normSqrd));
            }
            if (normSqrd > Settings.linearSlop * Settings.linearSlop) {
                done = false;
            }
            positions[bodies[next].island].x += delta.x;
            positions[bodies[next].island].y += delta.y;


        }

        pool.pushVec2(1);

        return done;
    }

    @Override
    public void initVelocityConstraints(SolverData step) {
        v2[] velocities = step.velocities;
        v2[] positions = step.positions;
        v2[] d = pool.getVec2Array(bodies.length);

        for (int i = 0; i < bodies.length; ++i) {
            int prev = (i == 0 ? bodies.length : i) - 1;
            int next = i == bodies.length - 1 ? 0 : i + 1;
            d[i].set(positions[bodies[next].island]);
            d[i].subbed(positions[bodies[prev].island]);
        }

        if (step.step.warmStarting) {
            m_impulse *= step.step.dtRatio;
            velocityLambda(velocities, d, m_impulse);
        } else {
            m_impulse = 0.0f;
        }
    }

    @Override
    public boolean solvePositionConstraints(SolverData step) {
        return constrainEdges(step.positions);
    }

    @Override
    public void solveVelocityConstraints(SolverData step) {
        float crossMassSum = 0.0f;
        float dotMassSum = 0.0f;

        v2[] velocities = step.velocities;
        v2[] positions = step.positions;
        v2[] d = pool.getVec2Array(bodies.length);

        for (int i = 0; i < bodies.length; ++i) {
            int prev = (i == 0 ? bodies.length : i) - 1;
            int next = i == bodies.length - 1 ? 0 : i + 1;
            d[i].set(positions[bodies[next].island]);
            d[i].subbed(positions[bodies[prev].island]);
            dotMassSum += d[i].lengthSquaredDouble() / bodies[i].getMass();
            crossMassSum += v2.cross(velocities[bodies[i].island], d[i]);
        }
        float lambda = -2.0f * crossMassSum / dotMassSum;


        m_impulse += lambda;

        velocityLambda(velocities, d, lambda);
    }

    public void velocityLambda(v2[] velocities, v2[] d, float lambda) {
        for (int i = 0; i < bodies.length; ++i) {
            velocities[bodies[i].island].x += bodies[i].m_invMass * d[i].y * 0.5f * lambda;
            velocities[bodies[i].island].y += bodies[i].m_invMass * -d[i].x * 0.5f * lambda;
        }
    }

    /**
     * No-op
     */
    @Override
    public void anchorA(v2 argOut) {
    }

    /**
     * No-op
     */
    @Override
    public void anchorB(v2 argOut) {
    }

    /**
     * No-op
     */
    @Override
    public void reactionForce(float inv_dt, v2 argOut) {
    }

    /**
     * No-op
     */
    @Override
    public float reactionTorque(float inv_dt) {
        return 0;
    }
}