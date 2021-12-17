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
package spacegraph.space2d.phys.dynamics.contacts;

import jcog.Util;
import jcog.math.v2;
import spacegraph.space2d.phys.collision.Manifold;
import spacegraph.space2d.phys.collision.ManifoldPoint;
import spacegraph.space2d.phys.collision.WorldManifold;
import spacegraph.space2d.phys.collision.shapes.Shape;
import spacegraph.space2d.phys.common.Mat22;
import spacegraph.space2d.phys.common.Rot;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.common.Transform;
import spacegraph.space2d.phys.dynamics.Body2D;
import spacegraph.space2d.phys.dynamics.Fixture;
import spacegraph.space2d.phys.dynamics.TimeStep;

/**
 * @author Daniel
 */
public class ContactSolver {

    private static final boolean DEBUG_SOLVER = false;
    private static final float k_errorTol = 1.0e-3f;
    /**
     * For each solver, this is the initial number of constraints in the array, which expands as
     * needed.
     */
    private static final int INITIAL_NUM_CONSTRAINTS = 256;

    /**
     * Ensure a reasonable condition number. for the block solver
     */
    private static final float k_maxConditionNumber = 100.0f;

    private Position[] m_positions;
    private Velocity[] m_velocities;
    private ContactPositionConstraint[] m_positionConstraints;
    public ContactVelocityConstraint[] m_velocityConstraints;
    private Contact[] m_contacts;
    private int m_count;

    public ContactSolver() {
        m_positionConstraints = new ContactPositionConstraint[INITIAL_NUM_CONSTRAINTS];
        m_velocityConstraints = new ContactVelocityConstraint[INITIAL_NUM_CONSTRAINTS];
        for (int i = 0; i < INITIAL_NUM_CONSTRAINTS; i++) {
            m_positionConstraints[i] = new ContactPositionConstraint();
            m_velocityConstraints[i] = new ContactVelocityConstraint();
        }
    }

    public final void init(ContactSolverDef def) {

        TimeStep m_step = def.step;
        m_count = def.count;

        if (m_positionConstraints.length < m_count) {
            ContactPositionConstraint[] old = m_positionConstraints;
            m_positionConstraints = new ContactPositionConstraint[Math.max(old.length * 2, m_count)];
            System.arraycopy(old, 0, m_positionConstraints, 0, old.length);
            for (int i = old.length; i < m_positionConstraints.length; i++) {
                m_positionConstraints[i] = new ContactPositionConstraint();
            }
        }

        if (m_velocityConstraints.length < m_count) {
            ContactVelocityConstraint[] old = m_velocityConstraints;
            m_velocityConstraints = new ContactVelocityConstraint[Math.max(old.length * 2, m_count)];
            System.arraycopy(old, 0, m_velocityConstraints, 0, old.length);
            for (int i = old.length; i < m_velocityConstraints.length; i++) {
                m_velocityConstraints[i] = new ContactVelocityConstraint();
            }
        }

        m_positions = def.positions;
        m_velocities = def.velocities;
        m_contacts = def.contacts;

        for (int i = 0; i < m_count; ++i) {
            
            Contact contact = m_contacts[i];

            Fixture fixtureA = contact.aFixture;
            Fixture fixtureB = contact.bFixture;
            Shape shapeA = fixtureA.shape();
            Shape shapeB = fixtureB.shape();
            float radiusA = shapeA.skinRadius;
            float radiusB = shapeB.skinRadius;
            Body2D bodyA = fixtureA.getBody();
            Body2D bodyB = fixtureB.getBody();
            Manifold manifold = contact.getManifold();

            int pointCount = manifold.pointCount;
            assert (pointCount > 0);

            ContactVelocityConstraint vc = m_velocityConstraints[i];
            vc.friction = contact.m_friction;
            vc.restitution = contact.m_restitution;
            vc.tangentSpeed = contact.m_tangentSpeed;
            vc.indexA = bodyA.island;
            vc.indexB = bodyB.island;
            vc.invMassA = bodyA.m_invMass;
            vc.invMassB = bodyB.m_invMass;
            vc.invIA = bodyA.m_invI;
            vc.invIB = bodyB.m_invI;
            vc.contactIndex = i;
            vc.pointCount = pointCount;
            vc.K.setZero();
            vc.normalMass.setZero();

            ContactPositionConstraint pc = m_positionConstraints[i];
            pc.indexA = bodyA.island;
            pc.indexB = bodyB.island;
            pc.invMassA = bodyA.m_invMass;
            pc.invMassB = bodyB.m_invMass;
            pc.localCenterA.set(bodyA.sweep.localCenter);
            pc.localCenterB.set(bodyB.sweep.localCenter);
            pc.invIA = bodyA.m_invI;
            pc.invIB = bodyB.m_invI;
            pc.localNormal.set(manifold.localNormal);
            pc.localPoint.set(manifold.localPoint);
            pc.pointCount = pointCount;
            pc.radiusA = radiusA;
            pc.radiusB = radiusB;
            pc.type = manifold.type;

            
            for (int j = 0; j < pointCount; j++) {
                ManifoldPoint cp = manifold.points[j];
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];

                if (m_step.warmStarting) {
                    
                    
                    vcp.normalImpulse = m_step.dtRatio * cp.normalImpulse;
                    vcp.tangentImpulse = m_step.dtRatio * cp.tangentImpulse;
                } else {
                    vcp.normalImpulse = 0;
                    vcp.tangentImpulse = 0;
                }

                vcp.rA.setZero();
                vcp.rB.setZero();
                vcp.normalMass = 0;
                vcp.tangentMass = 0;
                vcp.velocityBias = 0;
                pc.localPoints[j].x = cp.localPoint.x;
                pc.localPoints[j].y = cp.localPoint.y;
            }
        }
    }

    public void warmStart() {
        
        for (int i = 0; i < m_count; ++i) {
            ContactVelocityConstraint vc = m_velocityConstraints[i];

            int indexA = vc.indexA;
            int indexB = vc.indexB;
            float mA = vc.invMassA;
            float iA = vc.invIA;
            float mB = vc.invMassB;
            float iB = vc.invIB;
            int pointCount = vc.pointCount;

            v2 vA = m_velocities[indexA];
            float wA = m_velocities[indexA].w;
            v2 vB = m_velocities[indexB];
            float wB = m_velocities[indexB].w;

            v2 normal = vc.normal;
            float tangentx = 1.0f * normal.y;
            float tangenty = -1.0f * normal.x;

            for (int j = 0; j < pointCount; ++j) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                float Px = tangentx * vcp.tangentImpulse + normal.x * vcp.normalImpulse;
                float Py = tangenty * vcp.tangentImpulse + normal.y * vcp.normalImpulse;

                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);
                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
                vB.x += Px * mB;
                vB.y += Py * mB;
            }
            m_velocities[indexA].w = wA;
            m_velocities[indexB].w = wB;
        }
    }

    
    private final Transform xfA = new Transform();
    private final Transform xfB = new Transform();
    private final WorldManifold worldManifold = new WorldManifold();

    public final void initializeVelocityConstraints() {

        
        for (int i = 0; i < m_count; ++i) {
            ContactVelocityConstraint vc = m_velocityConstraints[i];
            ContactPositionConstraint pc = m_positionConstraints[i];

            float radiusA = pc.radiusA;
            float radiusB = pc.radiusB;
            Manifold manifold = m_contacts[vc.contactIndex].getManifold();

            int indexA = vc.indexA;
            int indexB = vc.indexB;

            float mA = vc.invMassA;
            float mB = vc.invMassB;
            float iA = vc.invIA;
            float iB = vc.invIB;
            v2 localCenterA = pc.localCenterA;
            v2 localCenterB = pc.localCenterB;

            v2 cA = m_positions[indexA];
            float aA = m_positions[indexA].a;
            v2 vA = m_velocities[indexA];
            float wA = m_velocities[indexA].w;

            v2 cB = m_positions[indexB];
            float aB = m_positions[indexB].a;
            v2 vB = m_velocities[indexB];
            float wB = m_velocities[indexB].w;

            assert (manifold.pointCount > 0);

            Rot xfAq = xfA;
            Rot xfBq = xfB;
            xfAq.set(aA);
            xfBq.set(aB);
            xfA.pos.x = cA.x - (xfAq.c * localCenterA.x - xfAq.s * localCenterA.y);
            xfA.pos.y = cA.y - (xfAq.s * localCenterA.x + xfAq.c * localCenterA.y);
            xfB.pos.x = cB.x - (xfBq.c * localCenterB.x - xfBq.s * localCenterB.y);
            xfB.pos.y = cB.y - (xfBq.s * localCenterB.x + xfBq.c * localCenterB.y);

            worldManifold.initialize(manifold, xfA, radiusA, xfB, radiusB);

            v2 vcnormal = vc.normal;
            vcnormal.x = worldManifold.normal.x;
            vcnormal.y = worldManifold.normal.y;

            int pointCount = vc.pointCount;
            for (int j = 0; j < pointCount; ++j) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                v2 wmPj = worldManifold.points[j];
                v2 vcprA = vcp.rA;
                v2 vcprB = vcp.rB;
                vcprA.x = wmPj.x - cA.x;
                vcprA.y = wmPj.y - cA.y;
                vcprB.x = wmPj.x - cB.x;
                vcprB.y = wmPj.y - cB.y;

                float rnA = vcprA.x * vcnormal.y - vcprA.y * vcnormal.x;
                float rnB = vcprB.x * vcnormal.y - vcprB.y * vcnormal.x;

                float kNormal = mA + mB + iA * rnA * rnA + iB * rnB * rnB;

                vcp.normalMass = kNormal > 0.0f ? 1.0f / kNormal : 0.0f;

                float tangentx = 1.0f * vcnormal.y;
                float tangenty = -1.0f * vcnormal.x;

                float rtA = vcprA.x * tangenty - vcprA.y * tangentx;
                float rtB = vcprB.x * tangenty - vcprB.y * tangentx;

                float kTangent = mA + mB + iA * rtA * rtA + iB * rtB * rtB;

                vcp.tangentMass = kTangent > 0.0f ? 1.0f / kTangent : 0.0f;

                
                vcp.velocityBias = 0.0f;
                float tempx = vB.x + -wB * vcprB.y - vA.x - (-wA * vcprA.y);
                float tempy = vB.y + wB * vcprB.x - vA.y - (wA * vcprA.x);
                float vRel = vcnormal.x * tempx + vcnormal.y * tempy;
                if (vRel < -Settings.velocityThreshold) {
                    vcp.velocityBias = -vc.restitution * vRel;
                }
            }

            
            if (vc.pointCount == 2) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp1 = vc.points[0];
                ContactVelocityConstraint.VelocityConstraintPoint vcp2 = vc.points[1];
                float rn1A = vcp1.rA.x * vcnormal.y - vcp1.rA.y * vcnormal.x;
                float rn1B = vcp1.rB.x * vcnormal.y - vcp1.rB.y * vcnormal.x;
                float rn2A = vcp2.rA.x * vcnormal.y - vcp2.rA.y * vcnormal.x;
                float rn2B = vcp2.rB.x * vcnormal.y - vcp2.rB.y * vcnormal.x;

                float k11 = mA + mB + iA * rn1A * rn1A + iB * rn1B * rn1B;
                float k22 = mA + mB + iA * rn2A * rn2A + iB * rn2B * rn2B;
                float k12 = mA + mB + iA * rn1A * rn2A + iB * rn1B * rn2B;
                if (k11 * k11 < k_maxConditionNumber * (k11 * k22 - k12 * k12)) {
                    
                    vc.K.ex.x = k11;
                    vc.K.ex.y = k12;
                    vc.K.ey.x = k12;
                    vc.K.ey.y = k22;
                    vc.K.invertToOut(vc.normalMass);
                } else {
                    
                    
                    vc.pointCount = 1;
                }
            }
        }
    }


    public final void solveVelocityConstraints() {
        for (int i = 0; i < m_count; ++i) {
            ContactVelocityConstraint vc = m_velocityConstraints[i];

            int indexA = vc.indexA;
            int indexB = vc.indexB;

            float mA = vc.invMassA;
            float mB = vc.invMassB;
            float iA = vc.invIA;
            float iB = vc.invIB;
            int pointCount = vc.pointCount;

            v2 vA = m_velocities[indexA];
            float wA = m_velocities[indexA].w;
            v2 vB = m_velocities[indexB];
            float wB = m_velocities[indexB].w;

            v2 normal = vc.normal;
            float normalx = normal.x;
            float normaly = normal.y;
            float tangentx = 1.0f * vc.normal.y;
            float tangenty = -1.0f * vc.normal.x;
            float friction = vc.friction;

            assert (pointCount == 1 || pointCount == 2);

            
            for (int j = 0; j < pointCount; ++j) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[j];
                v2 a = vcp.rA;
                float dvx = -wB * vcp.rB.y + vB.x - vA.x + wA * a.y;
                float dvy = wB * vcp.rB.x + vB.y - vA.y - wA * a.x;

                
                float vt = dvx * tangentx + dvy * tangenty - vc.tangentSpeed;
                float lambda = vcp.tangentMass * (-vt);

                
                float maxFriction = friction * vcp.normalImpulse;
                float low = -maxFriction;
                float newImpulse =
                        Util.clamp(vcp.tangentImpulse + lambda, low, maxFriction);
                lambda = newImpulse - vcp.tangentImpulse;
                vcp.tangentImpulse = newImpulse;

                
                

                float Px = tangentx * lambda;
                float Py = tangenty * lambda;

                
                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);

                
                vB.x += Px * mB;
                vB.y += Py * mB;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
            }

            
            if (vc.pointCount == 1) {
                ContactVelocityConstraint.VelocityConstraintPoint vcp = vc.points[0];

                
                

                float dvx = -wB * vcp.rB.y + vB.x - vA.x + wA * vcp.rA.y;
                float dvy = wB * vcp.rB.x + vB.y - vA.y - wA * vcp.rA.x;

                
                float vn = dvx * normalx + dvy * normaly;
                float lambda = -vcp.normalMass * (vn - vcp.velocityBias);

                
                float a = vcp.normalImpulse + lambda;
                float newImpulse = (Math.max(a, 0.0f));
                lambda = newImpulse - vcp.normalImpulse;
                vcp.normalImpulse = newImpulse;

                
                float Px = normalx * lambda;
                float Py = normaly * lambda;

                
                vA.x -= Px * mA;
                vA.y -= Py * mA;
                wA -= iA * (vcp.rA.x * Py - vcp.rA.y * Px);

                
                vB.x += Px * mB;
                vB.y += Py * mB;
                wB += iB * (vcp.rB.x * Py - vcp.rB.y * Px);
            } else {
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                

                ContactVelocityConstraint.VelocityConstraintPoint cp1 = vc.points[0];
                ContactVelocityConstraint.VelocityConstraintPoint cp2 = vc.points[1];
                v2 cp1rA = cp1.rA;
                v2 cp1rB = cp1.rB;
                v2 cp2rA = cp2.rA;
                v2 cp2rB = cp2.rB;
                float ax = cp1.normalImpulse;
                float ay = cp2.normalImpulse;

                assert (ax >= 0.0f && ay >= 0.0f);
                
                
                float dv1x = -wB * cp1rB.y + vB.x - vA.x + wA * cp1rA.y;
                float dv1y = wB * cp1rB.x + vB.y - vA.y - wA * cp1rA.x;

                
                float dv2x = -wB * cp2rB.y + vB.x - vA.x + wA * cp2rA.y;
                float dv2y = wB * cp2rB.x + vB.y - vA.y - wA * cp2rA.x;

                
                float vn1 = dv1x * normalx + dv1y * normaly;
                float vn2 = dv2x * normalx + dv2y * normaly;

                float bx = vn1 - cp1.velocityBias;
                float by = vn2 - cp2.velocityBias;

                
                Mat22 R = vc.K;
                bx -= R.ex.x * ax + R.ey.x * ay;
                by -= R.ex.y * ax + R.ey.y * ay;

                
                
                for (; ; ) {
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    
                    Mat22 R1 = vc.normalMass;
                    float xx = R1.ex.x * bx + R1.ey.x * by;
                    float xy = R1.ex.y * bx + R1.ey.y * by;
                    xx *= -1;
                    xy *= -1;

                    if (xx >= 0.0f && xy >= 0.0f) {
                        
                        
                        float dx = xx - ax;
                        float dy = xy - ay;

                        
                        
                        
                        float P1x = dx * normalx;
                        float P2x = dy * normalx;

                        /*
                         * vA -= invMassA * (P1 + P2); wA -= invIA * (Cross(cp1.rA, P1) + Cross(cp2.rA, P2));
                         *
                         * vB += invMassB * (P1 + P2); wB += invIB * (Cross(cp1.rB, P1) + Cross(cp2.rB, P2));
                         */

                        vA.x -= mA * (P1x + P2x);
                        float P2y = dy * normaly;
                        float P1y = dx * normaly;
                        vA.y -= mA * (P1y + P2y);
                        vB.x += mB * (P1x + P2x);
                        vB.y += mB * (P1y + P2y);

                        wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
                        wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

                        
                        cp1.normalImpulse = xx;
                        cp2.normalImpulse = xy;

                        /*
                         * #if B2_DEBUG_SOLVER == 1 
                         * Cross(wA, cp1.rA); dv2 = vB + Cross(wB, cp2.rB) - vA - Cross(wA, cp2.rA);
                         *
                         * 
                         *
                         * assert(Abs(vn1 - cp1.velocityBias) < k_errorTol); assert(Abs(vn2 - cp2.velocityBias)
                         * < k_errorTol); #endif
                         */
                        if (DEBUG_SOLVER) {
                            
                            v2 dv1 = vB.addToNew(v2.cross(wB, cp1rB).subbed(vA).subbed(v2.cross(wA, cp1rA)));
                            v2 dv2 = vB.addToNew(v2.cross(wB, cp2rB).subbed(vA).subbed(v2.cross(wA, cp2rA)));
                            
                            vn1 = v2.dot(dv1, normal);
                            vn2 = v2.dot(dv2, normal);

                            assert (Math.abs(vn1 - cp1.velocityBias) < k_errorTol);
                            assert (Math.abs(vn2 - cp2.velocityBias) < k_errorTol);
                        }
                        break;
                    }

                    
                    
                    
                    
                    
                    
                    xx = -cp1.normalMass * bx;
                    xy = 0.0f;
                    vn1 = 0.0f;
                    vn2 = vc.K.ex.y * xx + by;

                    if (xx >= 0.0f && vn2 >= 0.0f) {
                        
                        float dx = xx - ax;
                        float dy = xy - ay;

                        
                        
                        
                        float P1x = normalx * dx;
                        float P2x = normalx * dy;

                        /*
                         * Vec2 P1 = d.x * normal; Vec2 P2 = d.y * normal; vA -= invMassA * (P1 + P2); wA -=
                         * invIA * (Cross(cp1.rA, P1) + Cross(cp2.rA, P2));
                         *
                         * vB += invMassB * (P1 + P2); wB += invIB * (Cross(cp1.rB, P1) + Cross(cp2.rB, P2));
                         */

                        vA.x -= mA * (P1x + P2x);
                        float P2y = normaly * dy;
                        float P1y = normaly * dx;
                        vA.y -= mA * (P1y + P2y);
                        vB.x += mB * (P1x + P2x);
                        vB.y += mB * (P1y + P2y);

                        wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
                        wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

                        
                        cp1.normalImpulse = xx;
                        cp2.normalImpulse = xy;

                        /*
                         * #if B2_DEBUG_SOLVER == 1 
                         * Cross(wA, cp1.rA);
                         *
                         * 
                         *
                         * assert(Abs(vn1 - cp1.velocityBias) < k_errorTol); #endif
                         */
                        if (DEBUG_SOLVER) {
                            
                            v2 dv1 = vB.addToNew(v2.cross(wB, cp1rB).subbed(vA).subbed(v2.cross(wA, cp1rA)));
                            
                            vn1 = v2.dot(dv1, normal);

                            assert (Math.abs(vn1 - cp1.velocityBias) < k_errorTol);
                        }
                        break;
                    }

                    
                    
                    
                    
                    
                    
                    xx = 0.0f;
                    xy = -cp2.normalMass * by;
                    vn1 = vc.K.ey.x * xy + bx;
                    vn2 = 0.0f;

                    if (xy >= 0.0f && vn1 >= 0.0f) {
                        
                        float dx = xx - ax;
                        float dy = xy - ay;

                        
                        /*
                         * Vec2 P1 = d.x * normal; Vec2 P2 = d.y * normal; vA -= invMassA * (P1 + P2); wA -=
                         * invIA * (Cross(cp1.rA, P1) + Cross(cp2.rA, P2));
                         *
                         * vB += invMassB * (P1 + P2); wB += invIB * (Cross(cp1.rB, P1) + Cross(cp2.rB, P2));
                         */

                        float P1x = normalx * dx;
                        float P2x = normalx * dy;

                        vA.x -= mA * (P1x + P2x);
                        float P2y = normaly * dy;
                        float P1y = normaly * dx;
                        vA.y -= mA * (P1y + P2y);
                        vB.x += mB * (P1x + P2x);
                        vB.y += mB * (P1y + P2y);

                        wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
                        wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

                        
                        cp1.normalImpulse = xx;
                        cp2.normalImpulse = xy;

                        /*
                         * #if B2_DEBUG_SOLVER == 1 
                         * Cross(wA, cp2.rA);
                         *
                         * 
                         *
                         * assert(Abs(vn2 - cp2.velocityBias) < k_errorTol); #endif
                         */
                        if (DEBUG_SOLVER) {
                            
                            v2 dv2 = vB.addToNew(v2.cross(wB, cp2rB).subbed(vA).subbed(v2.cross(wA, cp2rA)));
                            
                            vn2 = v2.dot(dv2, normal);

                            assert (Math.abs(vn2 - cp2.velocityBias) < k_errorTol);
                        }
                        break;
                    }

                    
                    
                    
                    
                    
                    xx = 0.0f;
                    xy = 0.0f;
                    vn1 = bx;
                    vn2 = by;

                    if (vn1 >= 0.0f && vn2 >= 0.0f) {
                        
                        float dx = xx - ax;
                        float dy = xy - ay;

                        
                        /*
                         * Vec2 P1 = d.x * normal; Vec2 P2 = d.y * normal; vA -= invMassA * (P1 + P2); wA -=
                         * invIA * (Cross(cp1.rA, P1) + Cross(cp2.rA, P2));
                         *
                         * vB += invMassB * (P1 + P2); wB += invIB * (Cross(cp1.rB, P1) + Cross(cp2.rB, P2));
                         */

                        float P1x = normalx * dx;
                        float P2x = normalx * dy;

                        vA.x -= mA * (P1x + P2x);
                        float P2y = normaly * dy;
                        float P1y = normaly * dx;
                        vA.y -= mA * (P1y + P2y);
                        vB.x += mB * (P1x + P2x);
                        vB.y += mB * (P1y + P2y);

                        wA -= iA * (cp1rA.x * P1y - cp1rA.y * P1x + (cp2rA.x * P2y - cp2rA.y * P2x));
                        wB += iB * (cp1rB.x * P1y - cp1rB.y * P1x + (cp2rB.x * P2y - cp2rB.y * P2x));

                        
                        cp1.normalImpulse = xx;
                        cp2.normalImpulse = xy;

                        break;
                    }

                    
                    break;
                }
            }

            
            m_velocities[indexA].w = wA;
            
            m_velocities[indexB].w = wB;
        }
    }

    public void storeImpulses() {
        for (int i = 0; i < m_count; i++) {
            ContactVelocityConstraint vc = m_velocityConstraints[i];
            Manifold manifold = m_contacts[vc.contactIndex].getManifold();

            for (int j = 0; j < vc.pointCount; j++) {
                manifold.points[j].normalImpulse = vc.points[j].normalImpulse;
                manifold.points[j].tangentImpulse = vc.points[j].tangentImpulse;
            }
        }
    }

    /*
     * #if 0 
     * float minSeparation = 0.0f;
     *
     * for (int i = 0; i < m_constraintCount; ++i) { ContactConstraint* c = m_constraints + i; Body*
     * bodyA = c.bodyA; Body* bodyB = c.bodyB; float invMassA = bodyA.m_mass * bodyA.m_invMass; float
     * invIA = bodyA.m_mass * bodyA.m_invI; float invMassB = bodyB.m_mass * bodyB.m_invMass; float
     * invIB = bodyB.m_mass * bodyB.m_invI;
     *
     * Vec2 normal = c.normal;
     *
     * 
     * ccp = c.points + j;
     *
     * Vec2 r1 = Mul(bodyA.GetXForm().R, ccp.localAnchorA - bodyA.GetLocalCenter()); Vec2 r2 =
     * Mul(bodyB.GetXForm().R, ccp.localAnchorB - bodyB.GetLocalCenter());
     *
     * Vec2 p1 = bodyA.m_sweep.c + r1; Vec2 p2 = bodyB.m_sweep.c + r2; Vec2 dp = p2 - p1;
     *
     * 
     *
     * 
     *
     * 
     * _linearSlop), -_maxLinearCorrection, 0.0f);
     *
     * 
     *
     * Vec2 P = impulse * normal;
     *
     * bodyA.m_sweep.c -= invMassA * P; bodyA.m_sweep.a -= invIA * Cross(r1, P);
     * bodyA.SynchronizeTransform();
     *
     * bodyB.m_sweep.c += invMassB * P; bodyB.m_sweep.a += invIB * Cross(r2, P);
     * bodyB.SynchronizeTransform(); } }
     *
     * 
     * -_linearSlop. return minSeparation >= -1.5f * _linearSlop; }
     */

    private final PositionSolverManifold psolver = new PositionSolverManifold();

    /**
     * Sequential solver.
     */
    public final boolean solvePositionConstraints() {
        float minSeparation = 0.0f;

        for (int i = 0; i < m_count; ++i) {
            ContactPositionConstraint pc = m_positionConstraints[i];

            int indexA = pc.indexA;
            int indexB = pc.indexB;

            float mA = pc.invMassA;
            float iA = pc.invIA;
            v2 localCenterA = pc.localCenterA;
            float localCenterAx = localCenterA.x;
            float localCenterAy = localCenterA.y;
            float mB = pc.invMassB;
            float iB = pc.invIB;
            v2 localCenterB = pc.localCenterB;
            float localCenterBx = localCenterB.x;
            float localCenterBy = localCenterB.y;
            int pointCount = pc.pointCount;

            v2 cA = m_positions[indexA];
            float aA = m_positions[indexA].a;
            v2 cB = m_positions[indexB];
            float aB = m_positions[indexB].a;

            
            for (int j = 0; j < pointCount; ++j) {
                Rot xfAq = xfA;
                Rot xfBq = xfB;
                xfAq.set(aA);
                xfBq.set(aB);
                xfA.pos.x = cA.x - xfAq.c * localCenterAx + xfAq.s * localCenterAy;
                xfA.pos.y = cA.y - xfAq.s * localCenterAx - xfAq.c * localCenterAy;
                xfB.pos.x = cB.x - xfBq.c * localCenterBx + xfBq.s * localCenterBy;
                xfB.pos.y = cB.y - xfBq.s * localCenterBx - xfBq.c * localCenterBy;

                PositionSolverManifold psm = psolver;
                psm.initialize(pc, xfA, xfB, j);
                v2 normal = psm.normal;
                v2 point = psm.point;
                float separation = psm.separation;

                float rAx = point.x - cA.x;
                float rAy = point.y - cA.y;
                float rBx = point.x - cB.x;
                float rBy = point.y - cB.y;


                minSeparation = Math.min(minSeparation, separation);


                float low = -Settings.maxLinearCorrection;
                float C =
                        Util.clamp(Settings.baumgarte * (separation + Settings.linearSlop), low, 0.0f);

                
                float rnA = rAx * normal.y - rAy * normal.x;
                float rnB = rBx * normal.y - rBy * normal.x;
                float K = mA + mB + iA * rnA * rnA + iB * rnB * rnB;

                
                float impulse = K > 0.0f ? -C / K : 0.0f;

                float Px = normal.x * impulse;
                float Py = normal.y * impulse;

                cA.x -= Px * mA;
                cA.y -= Py * mA;
                aA -= iA * (rAx * Py - rAy * Px);

                cB.x += Px * mB;
                cB.y += Py * mB;
                aB += iB * (rBx * Py - rBy * Px);
            }

            
            m_positions[indexA].a = aA;

            
            m_positions[indexB].a = aB;
        }

        
        
        return minSeparation >= -3.0f * Settings.linearSlop;
    }

    
    public boolean solveTOIPositionConstraints(int toiIndexA, int toiIndexB) {
        float minSeparation = 0.0f;

        for (int i = 0; i < m_count; ++i) {
            ContactPositionConstraint pc = m_positionConstraints[i];

            int indexA = pc.indexA;
            int indexB = pc.indexB;
            v2 localCenterA = pc.localCenterA;
            v2 localCenterB = pc.localCenterB;
            float localCenterAx = localCenterA.x;
            float localCenterAy = localCenterA.y;
            float localCenterBx = localCenterB.x;
            float localCenterBy = localCenterB.y;
            int pointCount = pc.pointCount;

            float mA = 0.0f;
            float iA = 0.0f;
            if (indexA == toiIndexA || indexA == toiIndexB) {
                mA = pc.invMassA;
                iA = pc.invIA;
            }

            float mB = 0.0f;
            float iB = 0.0f;
            if (indexB == toiIndexA || indexB == toiIndexB) {
                mB = pc.invMassB;
                iB = pc.invIB;
            }

            v2 cA = m_positions[indexA];
            float aA = m_positions[indexA].a;

            v2 cB = m_positions[indexB];
            float aB = m_positions[indexB].a;

            
            for (int j = 0; j < pointCount; ++j) {
                Rot xfAq = xfA;
                Rot xfBq = xfB;
                xfAq.set(aA);
                xfBq.set(aB);
                xfA.pos.x = cA.x - xfAq.c * localCenterAx + xfAq.s * localCenterAy;
                xfA.pos.y = cA.y - xfAq.s * localCenterAx - xfAq.c * localCenterAy;
                xfB.pos.x = cB.x - xfBq.c * localCenterBx + xfBq.s * localCenterBy;
                xfB.pos.y = cB.y - xfBq.s * localCenterBx - xfBq.c * localCenterBy;

                PositionSolverManifold psm = psolver;
                psm.initialize(pc, xfA, xfB, j);
                v2 normal = psm.normal;

                v2 point = psm.point;
                float separation = psm.separation;

                float rAx = point.x - cA.x;
                float rAy = point.y - cA.y;
                float rBx = point.x - cB.x;
                float rBy = point.y - cB.y;


                minSeparation = Math.min(minSeparation, separation);


                float low = -Settings.maxLinearCorrection;
                float C =
                        Util.clamp(Settings.toiBaugarte * (separation + Settings.linearSlop), low, 0.0f);

                
                double rnA = rAx * normal.y - rAy * normal.x;
                double rnB = rBx * normal.y - rBy * normal.x;
                double K = mA + mB + iA * rnA * rnA + iB * rnB * rnB;


                double impulse = K > 0.0f ? -C / K : 0.0;

                double Px = normal.x * impulse;
                double Py = normal.y * impulse;

                cA.x -= Px * mA;
                cA.y -= Py * mA;
                aA -= iA * (rAx * Py - rAy * Px);

                cB.x += Px * mB;
                cB.y += Py * mB;
                aB += iB * (rBx * Py - rBy * Px);
            }

            
            m_positions[indexA].a = aA;

            
            m_positions[indexB].a = aB;
        }

        
        
        return minSeparation >= -1.5f * Settings.linearSlop;
    }

    public static class ContactSolverDef {
        public TimeStep step;
        public Contact[] contacts;
        public int count;
        public Position[] positions;
        public Velocity[] velocities;
    }
}


class PositionSolverManifold {

    public final v2 normal = new v2();
    public final v2 point = new v2();
    public float separation;

    public void initialize(ContactPositionConstraint pc, Transform xfA, Transform xfB, int index) {
        assert (pc.pointCount > 0);

        Rot xfAq = xfA;
        Rot xfBq = xfB;
        v2 pcLocalPointsI = pc.localPoints[index];
        switch (pc.type) {
            case CIRCLES -> {


                v2 plocalPoint = pc.localPoint;
                v2 pLocalPoints0 = pc.localPoints[0];
                float pointAx = (xfAq.c * plocalPoint.x - xfAq.s * plocalPoint.y) + xfA.pos.x;
                float pointAy = (xfAq.s * plocalPoint.x + xfAq.c * plocalPoint.y) + xfA.pos.y;
                float pointBx = (xfBq.c * pLocalPoints0.x - xfBq.s * pLocalPoints0.y) + xfB.pos.x;
                float pointBy = (xfBq.s * pLocalPoints0.x + xfBq.c * pLocalPoints0.y) + xfB.pos.y;
                normal.x = pointBx - pointAx;
                normal.y = pointBy - pointAy;
                normal.normalize();

                point.x = (pointAx + pointBx) * 0.5f;
                point.y = (pointAy + pointBy) * 0.5f;
                float tempx = pointBx - pointAx;
                float tempy = pointBy - pointAy;
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
                break;
            }
            case FACE_A -> {


                v2 pcLocalNormal = pc.localNormal;
                v2 pcLocalPoint = pc.localPoint;
                normal.x = xfAq.c * pcLocalNormal.x - xfAq.s * pcLocalNormal.y;
                normal.y = xfAq.s * pcLocalNormal.x + xfAq.c * pcLocalNormal.y;
                float planePointx = (xfAq.c * pcLocalPoint.x - xfAq.s * pcLocalPoint.y) + xfA.pos.x;
                float planePointy = (xfAq.s * pcLocalPoint.x + xfAq.c * pcLocalPoint.y) + xfA.pos.y;

                float clipPointx = (xfBq.c * pcLocalPointsI.x - xfBq.s * pcLocalPointsI.y) + xfB.pos.x;
                float clipPointy = (xfBq.s * pcLocalPointsI.x + xfBq.c * pcLocalPointsI.y) + xfB.pos.y;
                float tempx = clipPointx - planePointx;
                float tempy = clipPointy - planePointy;
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
                point.x = clipPointx;
                point.y = clipPointy;
                break;
            }
            case FACE_B -> {


                v2 pcLocalNormal = pc.localNormal;
                v2 pcLocalPoint = pc.localPoint;
                normal.x = xfBq.c * pcLocalNormal.x - xfBq.s * pcLocalNormal.y;
                normal.y = xfBq.s * pcLocalNormal.x + xfBq.c * pcLocalNormal.y;
                float planePointx = (xfBq.c * pcLocalPoint.x - xfBq.s * pcLocalPoint.y) + xfB.pos.x;
                float planePointy = (xfBq.s * pcLocalPoint.x + xfBq.c * pcLocalPoint.y) + xfB.pos.y;

                float clipPointx = (xfAq.c * pcLocalPointsI.x - xfAq.s * pcLocalPointsI.y) + xfA.pos.x;
                float clipPointy = (xfAq.s * pcLocalPointsI.x + xfAq.c * pcLocalPointsI.y) + xfA.pos.y;
                float tempx = clipPointx - planePointx;
                float tempy = clipPointy - planePointy;
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA - pc.radiusB;
                point.x = clipPointx;
                point.y = clipPointy;
                normal.x *= -1;
                normal.y *= -1;
            }
        }
    }
}
