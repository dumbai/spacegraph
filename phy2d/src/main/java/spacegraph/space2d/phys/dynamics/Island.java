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
package spacegraph.space2d.phys.dynamics;

import com.google.common.base.Joiner;
import jcog.math.v2;
import spacegraph.space2d.phys.callbacks.ContactImpulse;
import spacegraph.space2d.phys.callbacks.ContactListener;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.common.Sweep;
import spacegraph.space2d.phys.common.Timer;
import spacegraph.space2d.phys.dynamics.contacts.*;
import spacegraph.space2d.phys.dynamics.contacts.ContactSolver.ContactSolverDef;
import spacegraph.space2d.phys.dynamics.joints.Joint;
import spacegraph.space2d.phys.fracture.fragmentation.Smasher;

/*
 Position Correction Notes
 =========================
 I tried the several algorithms for position correction of the 2D revolute joint.
 I looked at these systems:
 - simple pendulum (1m diameter sphere on massless 5m stick) with initial angular velocity of 100 rad/s.
 - suspension bridge with 30 1m long planks of length 1m.
 - multi-link chain with 30 1m long links.

 Here are the algorithms:

 Baumgarte - A fraction of the position error is added to the velocity error. There is no
 separate position solver.

 Pseudo Velocities - After the velocity solver and position integration,
 the position error, Jacobian, and effective mass are recomputed. Then
 the velocity constraints are solved with pseudo velocities and a fraction
 of the position error is added to the pseudo velocity error. The pseudo
 velocities are initialized to zero and there is no warm-starting. After
 the position solver, the pseudo velocities are added to the positions.
 This is also called the First Order World method or the Position LCP method.

 Modified Nonlinear Gauss-Seidel (NGS) - Like Pseudo Velocities except the
 position error is re-computed for each raint and the positions are updated
 after the raint is solved. The radius vectors (aka Jacobians) are
 re-computed too (otherwise the algorithm has horrible instability). The pseudo
 velocity states are not needed because they are effectively zero at the beginning
 of each iteration. Since we have the current position error, we allow the
 iterations to terminate early if the error becomes smaller than Settings.linearSlop.

 Full NGS or just NGS - Like Modified NGS except the effective mass are re-computed
 each time a raint is solved.

 Here are the results:
 Baumgarte - this is the cheapest algorithm but it has some stability problems,
 especially with the bridge. The chain links separate easily close to the root
 and they jitter as they struggle to pull together. This is one of the most common
 methods in the field. The big drawback is that the position correction artificially
 affects the momentum, thus leading to instabilities and false bounce. I used a
 bias factor of 0.2. A larger bias factor makes the bridge less stable, a smaller
 factor makes joints and contacts more spongy.

 Pseudo Velocities - the is more stable than the Baumgarte method. The bridge is
 stable. However, joints still separate with large angular velocities. Drag the
 simple pendulum in a circle quickly and the joint will separate. The chain separates
 easily and does not recover. I used a bias factor of 0.2. A larger value lead to
 the bridge collapsing when a heavy cube drops on it.

 Modified NGS - this algorithm is better in some ways than Baumgarte and Pseudo
 Velocities, but in other ways it is worse. The bridge and chain are much more
 stable, but the simple pendulum goes unstable at high angular velocities.

 Full NGS - stable in all tests. The joints display good stiffness. The bridge
 still sags, but this is better than infinite forces.

 Recommendations
 Pseudo Velocities are not really worthwhile because the bridge and chain cannot
 recover from joint separation. In other cases the benefit over Baumgarte is small.

 Modified NGS is not a robust method for the revolute joint due to the violent
 instability seen in the simple pendulum. Perhaps it is viable with other raint
 types, especially scalar constraints where the effective mass is a scalar.

 This leaves Baumgarte and Full NGS. Baumgarte has small, but manageable instabilities
 and is very fast. I don't think we can escape Baumgarte, especially in highly
 demanding cases where high raint fidelity is not needed.

 Full NGS is robust and easy on the eyes. I recommend this as an option for
 higher fidelity simulation and certainly for suspension bridges and long chains.
 Full NGS might be a good choice for ragdolls, especially motorized ragdolls where
 joint separation can be problematic. The number of NGS iterations can be reduced
 for better performance without harming robustness much.

 Each joint in a can be handled differently in the position solver. So I recommend
 a system where the user can select the algorithm on a per joint basis. I would
 probably default to the slower Full NGS and let the user select the faster
 Baumgarte method in performance critical scenarios.
 */

/*
 Cache Performance

 The Box2D solvers are dominated by cache misses. Data structures are designed
 to increase the number of cache hits. Much of misses are due to random access
 to body data. The raint structures are iterated over linearly, which leads
 to few cache misses.

 The bodies are not accessed during iteration. Instead read only data, such as
 the mass values are stored with the constraints. The mutable data are the raint
 impulses and the bodies velocities/positions. The impulses are held inside the
 raint structures. The body velocities/positions are held in compact, temporary
 arrays to increase the number of cache hits. Linear and angular velocity are
 stored in a single array since multiple arrays lead to multiple misses.
 */

/*
 2D Rotation

 R = [cos(theta) -sin(theta)]
 [sin(theta) cos(theta) ]

 thetaDot = omega

 Let q1 = cos(theta), q2 = sin(theta).
 R = [q1 -q2]
 [q2  q1]

 q1Dot = -thetaDot * q2
 q2Dot = thetaDot * q1

 q1_new = q1_old - dt * w * q2
 q2_new = q2_old + dt * w * q1
 then normalize.

 This might be faster than computing sin+cos.
 However, we can compute sin+cos of the same angle fast.
 */

/**
 * This is an internal class.
 *
 * @author Daniel Murphy
 */
class Island {

    public static final Velocity[] VELOCITIES = new Velocity[0];
    public static final Position[] POSITIONS = new Position[0];
    private ContactListener m_listener;

    public Body2D[] bodies;
    private Contact[] contacts;
    private Joint[] joints;

    private Position[] positions;
    private Velocity[] velocities;

    public int m_bodyCount;
    private int m_jointCount;
    public int m_contactCount;

    public int m_bodyCapacity;
    public int m_contactCapacity;
    private int m_jointCapacity;

    private final Smasher smasher;

    private final ContactImpulse impulse = new ContactImpulse();

    Island(Smasher smasher) {
        this.smasher = smasher;
    }

    void init(int bodyCapacity, int contactCapacity, int jointCapacity,
              ContactListener listener) {

        m_bodyCapacity = bodyCapacity;
        m_contactCapacity = contactCapacity;
        m_jointCapacity = jointCapacity;
        m_bodyCount = 0;
        m_contactCount = 0;
        m_jointCount = 0;

        m_listener = listener;

        if (bodies == null || m_bodyCapacity > bodies.length) {
            bodies = new Body2D[m_bodyCapacity];
        }
        if (joints == null || m_jointCapacity > joints.length) {
            joints = new Joint[m_jointCapacity];
        }
        if (contacts == null || m_contactCapacity > contacts.length) {
            contacts = new Contact[m_contactCapacity];
        }


        if (velocities == null || m_bodyCapacity > velocities.length) {
            Velocity[] old = velocities == null ? VELOCITIES : velocities;
            velocities = new Velocity[m_bodyCapacity];
            System.arraycopy(old, 0, velocities, 0, old.length);
            for (int i = old.length; i < velocities.length; i++)
                velocities[i] = new Velocity();
        }


        if (positions == null || m_bodyCapacity > positions.length) {
            v2[] old = positions == null ? POSITIONS : positions;
            positions = new Position[m_bodyCapacity];
            System.arraycopy(old, 0, positions, 0, old.length);
            for (int i = old.length; i < positions.length; i++) {
                positions[i] = new Position();
            }
        }
    }

    public void clear() {
        m_bodyCount = 0;
        m_contactCount = 0;
        m_jointCount = 0;
    }

    private final ContactSolver contactSolver = new ContactSolver();
    private final Timer timer = new Timer();
    private final SolverData solverData = new SolverData();
    private final ContactSolverDef solverDef = new ContactSolverDef();

    public void solve(Dynamics2D.Profile profile, TimeStep step, v2 gravity, boolean allowSleep) {


        float h = step.dt;


        int bodies = this.m_bodyCount;
        for (int i = 0; i < bodies; ++i) {
            Body2D b = this.bodies[i];
            Sweep bm_sweep = b.sweep;
            v2 c = bm_sweep.c;
            float a = bm_sweep.a;
            v2 v = b.vel;
            float w = b.velAngular;


            bm_sweep.c0.set(bm_sweep.c);
            bm_sweep.a0 = bm_sweep.a;

            positions[i].x = c.x;
            positions[i].y = c.y;
            positions[i].a = a;

            Velocity vi = velocities[i];
            if (b.type == BodyType.DYNAMIC) {


                v.x += h * (b.m_gravityScale * gravity.x + b.m_invMass * b.force.x);
                v.y += h * (b.m_gravityScale * gravity.y + b.m_invMass * b.force.y);
                w += h * b.m_invI * b.torque;


                v.x *= 1.0f / (1.0f + h * b.m_linearDamping);
                v.y *= 1.0f / (1.0f + h * b.m_linearDamping);
                w *= 1.0f / (1.0f + h * b.m_angularDamping);

                vi.x = v.x;
                vi.y = v.y;
                vi.w = w;
            } else {
                vi.x = vi.y = vi.w = 0;
            }



        }

        timer.reset();


        solverData.step = step;
        solverData.positions = positions;
        solverData.velocities = velocities;


        solverDef.step = step;
        solverDef.contacts = contacts;
        solverDef.count = m_contactCount;
        solverDef.positions = positions;
        solverDef.velocities = velocities;

        contactSolver.init(solverDef);

        contactSolver.initializeVelocityConstraints();

        if (step.warmStarting)
            contactSolver.warmStart();

        int joints = this.m_jointCount;
        for (int i = 0; i < joints; ++i)
            this.joints[i].initVelocityConstraints(solverData);

        profile.solveInit.accum(timer::getMilliseconds);

        timer.reset();

        for (int i = 0; i < step.velocityIterations; ++i) {
            for (int j = 0; j < joints; ++j)
                this.joints[j].solveVelocityConstraints(solverData);

            contactSolver.solveVelocityConstraints();
        }


        contactSolver.storeImpulses();
        profile.solveVelocity.accum(timer::getMilliseconds);


        for (int i = 0; i < bodies; ++i) {
            Position c = positions[i];
            float a = c.a;
            Velocity v = velocities[i];
            float w = v.w;


            float translationx = v.x * h;
            float translationy = v.y * h;

            if (translationx * translationx + translationy * translationy > Settings.maxTranslationSquared) {
                double ratio = Settings.maxTranslation
                        / Math.sqrt(translationx * translationx + translationy * translationy);
                v.x *= ratio;
                v.y *= ratio;
            }

            double rotation = h * w;
            if (rotation * rotation > Settings.maxRotationSquared) {
                double ratio = Settings.maxRotation / Math.abs(rotation);
                w *= ratio;
            }


            c.x += h * v.x;
            c.y += h * v.y;
            a += h * w;

            c.a = a;
            v.w = w;
        }


        timer.reset();
        boolean positionSolved = false;
        for (int i = 0; i < step.positionIterations; ++i) {
            boolean contactsOkay = contactSolver.solvePositionConstraints();

            boolean jointsOkay = true;
            for (int j = 0; j < joints; ++j) {
                boolean jointOkay = this.joints[j].solvePositionConstraints(solverData);
                jointsOkay = jointsOkay && jointOkay;
            }

            if (contactsOkay && jointsOkay) {
                positionSolved = true;
                break;
            }
        }


        for (int i = 0; i < bodies; ++i) {
            Body2D body = this.bodies[i];

            Position pi = positions[i];
            Sweep bs = body.sweep;
            Velocity vi = velocities[i];

            bs.c.x = pi.x;
            bs.c.y = pi.y;
            bs.a = pi.a;

            v2 bv = body.vel;
            if (body.getType()!= BodyType.DYNAMIC) {
                bv.x = vi.x = 0;
                bv.y = vi.y = 0;
                body.velAngular = vi.w = 0;
            } else {
                bv.x = vi.x;
                bv.y = vi.y;
                body.velAngular = vi.w;
            }
            body.synchronizeTransform();
        }

        profile.solvePosition.accum(timer::getMilliseconds);

        report(contactSolver.m_velocityConstraints);

        if (allowSleep) {
            float minSleepTime = Float.POSITIVE_INFINITY;

            final float linTolSqr = Settings.linearSleepTolerance * Settings.linearSleepTolerance;
            final float angTolSqr = Settings.angularSleepTolerance * Settings.angularSleepTolerance;

            for (int i = 0; i < bodies; ++i) {
                Body2D b = this.bodies[i];
                if (b.getType() == BodyType.STATIC) {
                    continue;
                }

                if ((b.flags & Body2D.e_autoSleepFlag) == 0
                        || b.velAngular * b.velAngular > angTolSqr
                        || v2.dot(b.vel, b.vel) > linTolSqr) {
                    b.m_sleepTime = 0.0f;
                    minSleepTime = 0.0f;
                } else {
                    b.m_sleepTime += h;
                    minSleepTime = Math.min(minSleepTime, b.m_sleepTime);
                }
            }

            if (minSleepTime >= Settings.timeToSleep && positionSolved) {
                for (int i = 0; i < bodies; ++i) {
                    Body2D b = this.bodies[i];
                    b.setAwake(false);
                }
            }
        }
    }

    private final ContactSolver toiContactSolver = new ContactSolver();
    private final ContactSolverDef toiSolverDef = new ContactSolverDef();

    void solveTOI(TimeStep subStep, int toiIndexA, int toiIndexB) {
        assert (toiIndexA < m_bodyCount);
        assert (toiIndexB < m_bodyCount);


        for (int i = 0; i < m_bodyCount; ++i) {
            Body2D b = bodies[i];
            positions[i].x = b.sweep.c.x;
            positions[i].y = b.sweep.c.y;
            positions[i].a = b.sweep.a;
            velocities[i].x = b.vel.x;
            velocities[i].y = b.vel.y;
            velocities[i].w = b.velAngular;
        }

        toiSolverDef.contacts = contacts;
        toiSolverDef.count = m_contactCount;
        toiSolverDef.step = subStep;
        toiSolverDef.positions = positions;
        toiSolverDef.velocities = velocities;
        toiContactSolver.init(toiSolverDef);


        for (int i = 0; i < subStep.positionIterations; ++i) {
            boolean contactsOkay = toiContactSolver.solveTOIPositionConstraints(toiIndexA, toiIndexB);
            if (contactsOkay) {
                break;
            }
        }


        bodies[toiIndexA].sweep.c0.x = positions[toiIndexA].x;
        bodies[toiIndexA].sweep.c0.y = positions[toiIndexA].y;
        bodies[toiIndexA].sweep.a0 = positions[toiIndexA].a;
        bodies[toiIndexB].sweep.c0.set(positions[toiIndexB]);
        bodies[toiIndexB].sweep.a0 = positions[toiIndexB].a;


        toiContactSolver.initializeVelocityConstraints();


        for (int i = 0; i < subStep.velocityIterations; ++i) {
            toiContactSolver.solveVelocityConstraints();
        }


        float h = subStep.dt;


        for (int i = 0; i < m_bodyCount; ++i) {
            v2 c = positions[i];
            float a = positions[i].a;
            v2 v = velocities[i];
            float w = velocities[i].w;


            float translationx = v.x * h;
            float translationy = v.y * h;
            if (translationx * translationx + translationy * translationy > Settings.maxTranslationSquared) {
                float ratio =
                        Settings.maxTranslation
                                / (float) Math.sqrt(translationx * translationx + translationy * translationy);
                v.scaled(ratio);
            }

            float rotation = h * w;
            if (rotation * rotation > Settings.maxRotationSquared) {
                float ratio = Settings.maxRotation / Math.abs(rotation);
                w *= ratio;
            }


            c.x += v.x * h;
            c.y += v.y * h;
            a += h * w;

            positions[i].x = c.x;
            positions[i].y = c.y;
            positions[i].a = a;
            velocities[i].x = v.x;
            velocities[i].y = v.y;
            velocities[i].w = w;


            Body2D body = bodies[i];
            body.sweep.c.x = c.x;
            body.sweep.c.y = c.y;
            body.sweep.a = a;
            body.vel.x = v.x;
            body.vel.y = v.y;
            body.velAngular = w;
            body.synchronizeTransform();
        }

        report(toiContactSolver.m_velocityConstraints);
    }

    void add(Body2D body) {
        assert (m_bodyCount+1 <= m_bodyCapacity) : "island overcapacity: " + (m_bodyCount+1) + '/' + m_bodyCapacity;
        body.island = m_bodyCount;
        bodies[m_bodyCount++] = body;
    }

    void add(Contact contact) {
        assert (m_contactCount < m_contactCapacity);
        contacts[m_contactCount++] = contact;
    }

    void add(Joint joint) {
        assert (m_jointCount < m_jointCapacity) : this + " has too many joints: " + Joiner.on('\n').join(joints);
        joints[m_jointCount++] = joint;
    }


    private void report(ContactVelocityConstraint[] constraints) {
        for (int i = 0; i < m_contactCount; ++i) {
            Contact c = contacts[i];

            ContactVelocityConstraint vc = constraints[i];
            impulse.count = vc.pointCount;
            for (int j = 0; j < vc.pointCount; ++j) {
                impulse.normalImpulses[j] = vc.points[j].normalImpulse;
                impulse.tangentImpulses[j] = vc.points[j].tangentImpulse;
            }

            smasher.init(c, impulse);

            if (m_listener != null) {
                m_listener.postSolve(c, impulse);
            }
        }
    }
}