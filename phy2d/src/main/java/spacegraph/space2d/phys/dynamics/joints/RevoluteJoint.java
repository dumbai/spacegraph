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

import jcog.Util;
import jcog.math.v2;
import jcog.math.v3;
import spacegraph.space2d.phys.common.*;
import spacegraph.space2d.phys.dynamics.Dynamics2D;
import spacegraph.space2d.phys.dynamics.SolverData;
import spacegraph.space2d.phys.dynamics.contacts.Position;
import spacegraph.space2d.phys.dynamics.contacts.Velocity;
import spacegraph.space2d.phys.pooling.IWorldPool;

import static spacegraph.space2d.phys.common.Settings.EPSILON;


/**
 * A revolute joint constrains two bodies to share a common point while they are free to rotate
 * about the point. The relative rotation about the shared point is the joint angle. You can limit
 * the relative rotation with a joint limit that specifies a lower and upper angle. You can use a
 * motor to drive the relative rotation about the shared point. A maximum motor torque is provided
 * so that infinite forces are not generated.
 *
 * @author Daniel Murphy
 */
public class RevoluteJoint extends Joint {

    
    protected final v2 localAnchorA = new v2();
    protected final v2 localAnchorB = new v2();
    private final Vec3 m_impulse = new Vec3();
    private float m_motorImpulse;

    private boolean m_enableMotor;
    private float m_maxMotorTorque;
    private float m_motorSpeed;

    private boolean m_enableLimit;
    final float m_referenceAngle;
    private float m_lowerAngle;
    private float m_upperAngle;

    
    private int m_indexA;
    private int m_indexB;
    private final v2 m_rA = new v2();
    private final v2 m_rB = new v2();
    private final v2 m_localCenterA = new v2();
    private final v2 m_localCenterB = new v2();
    private float m_invMassA;
    private float m_invMassB;
    private float m_invIA;
    private float m_invIB;
    private final Mat33 m_mass = new Mat33();
    private float m_motorMass; 
    private LimitState m_limitState;

    /** how important it is to resolve position 'error' (distance from point-point).
     * 1 = normal revolute joint behavior
     * ~ = somewhat solve it
     * 0 = does not resolve point-to-point distance 'error'
     */
    public float positionFactor = 1.0f;

    public RevoluteJoint(Dynamics2D world, RevoluteJointDef def) {
        this(world.pool, def);
    }

    public RevoluteJoint(IWorldPool argWorld, RevoluteJointDef def) {
        super(argWorld, def);
        localAnchorA.set(def.localAnchorA);
        localAnchorB.set(def.localAnchorB);
        m_referenceAngle = def.referenceAngle;

        m_motorImpulse = 0;

        m_lowerAngle = def.lowerAngle;
        m_upperAngle = def.upperAngle;
        m_maxMotorTorque = def.maxMotorTorque;
        m_motorSpeed = def.motorSpeed;
        m_enableLimit = def.enableLimit;
        m_enableMotor = def.enableMotor;
        m_limitState = LimitState.INACTIVE;
    }

    @Override
    public void initVelocityConstraints(SolverData data) {
        m_indexA = A.island;
        m_indexB = B.island;
        m_localCenterA.set(A.sweep.localCenter);
        m_localCenterB.set(B.sweep.localCenter);
        m_invMassA = A.m_invMass;
        m_invMassB = B.m_invMass;
        m_invIA = A.m_invI;
        m_invIB = B.m_invI;


        Position[] P = data.positions;
        Velocity[] V = data.velocities;
        
        float aA = P[m_indexA].a;
        v2 vA = V[m_indexA];
        float wA = V[m_indexA].w;

        
        float aB = P[m_indexB].a;
        v2 vB = V[m_indexB];
        float wB = V[m_indexB].w;
        Rot qA = new Rot();
        Rot qB = new Rot();
        v2 temp = new v2();

        qA.set(aA);
        qB.set(aB);

        
        Rot.mulToOutUnsafe(qA, temp.set(localAnchorA).subbed(m_localCenterA), m_rA);
        Rot.mulToOutUnsafe(qB, temp.set(localAnchorB).subbed(m_localCenterB), m_rB);

        
        
        

        
        
        
        

        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        m_mass.ex.x = mA + mB + m_rA.y * m_rA.y * iA + m_rB.y * m_rB.y * iB;
        m_mass.ey.x = -m_rA.y * m_rA.x * iA - m_rB.y * m_rB.x * iB;
        m_mass.ez.x = -m_rA.y * iA - m_rB.y * iB;
        m_mass.ex.y = m_mass.ey.x;
        m_mass.ey.y = mA + mB + m_rA.x * m_rA.x * iA + m_rB.x * m_rB.x * iB;
        m_mass.ez.y = m_rA.x * iA + m_rB.x * iB;
        m_mass.ex.z = m_mass.ez.x;
        m_mass.ey.z = m_mass.ez.y;
        m_mass.ez.z = iA + iB;

        m_motorMass = iA + iB;
        if (m_motorMass > 0)
            m_motorMass = 1 / m_motorMass;

        boolean fixedRotation = fixedRotation(iA, iB);
        if (!m_enableMotor || fixedRotation)
            m_motorImpulse = 0;


        if (m_enableLimit && !fixedRotation) {
            float jointAngle = aB - aA - m_referenceAngle;
            if (Math.abs(m_upperAngle - m_lowerAngle) < 2.0f * Settings.angularSlop) {
                m_limitState = LimitState.EQUAL;
            } else if (jointAngle <= m_lowerAngle) {
                if (m_limitState != LimitState.AT_LOWER) {
                    m_impulse.z = 0;
                }
                m_limitState = LimitState.AT_LOWER;
            } else if (jointAngle >= m_upperAngle) {
                if (m_limitState != LimitState.AT_UPPER) {
                    m_impulse.z = 0;
                }
                m_limitState = LimitState.AT_UPPER;
            } else {
                m_limitState = LimitState.INACTIVE;
                m_impulse.z = 0;
            }
        } else {
            m_limitState = LimitState.INACTIVE;
        }

        if (data.step.warmStarting) {
            v2 p = new v2();
            
            m_impulse.x *= data.step.dtRatio;
            m_impulse.y *= data.step.dtRatio;
            m_motorImpulse *= data.step.dtRatio;

            p.x = m_impulse.x;
            p.y = m_impulse.y;

            vA.x -= mA * p.x;
            vA.y -= mA * p.y;
            wA -= iA * (v2.cross(m_rA, p) + m_motorImpulse + m_impulse.z);

            vB.x += mB * p.x;
            vB.y += mB * p.y;
            wB += iB * (v2.cross(m_rB, p) + m_motorImpulse + m_impulse.z);
        } else {
            m_impulse.zero();
            m_motorImpulse = 0;
        }
        
        V[m_indexA].w = wA;
        V[m_indexB].w = wB;

    }

    @Override
    public void solveVelocityConstraints(SolverData data) {
        final Velocity[] V = data.velocities;
        var vA = V[m_indexA];
        var vB = V[m_indexB];
        float wA = vA.w;
        float wB = vB.w;

        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        boolean fixedRotation = fixedRotation(iA, iB);

        if (m_enableMotor && m_limitState != LimitState.EQUAL && !fixedRotation) {
            float Cdot = wB - wA - m_motorSpeed;
            float impulse = -m_motorMass * Cdot;
            float oldImpulse = m_motorImpulse;
            float maxImpulse = data.step.dt * m_maxMotorTorque;
            float low = -maxImpulse;
            m_motorImpulse = Util.clamp(m_motorImpulse + impulse, low, maxImpulse);
            impulse = m_motorImpulse - oldImpulse;

            wA -= iA * impulse;
            wB += iB * impulse;
        }

        v2 temp = new v2();

        if (m_enableLimit && m_limitState != LimitState.INACTIVE && !fixedRotation) {

            v2 Cdot1 = new v2();
            v3 Cdot = new v3();

            
            v2.crossToOutUnsafe(wA, m_rA, temp);
            v2.crossToOutUnsafe(wB, m_rB, Cdot1);
            Cdot1.added(vB).subbed(vA).subbed(temp).scaled(positionFactor);
            float Cdot2 = wB - wA;
            Cdot.set(Cdot1.x, Cdot1.y, Cdot2);

            v3 impulse = new v3();
            m_mass.solve33ToOut(Cdot, impulse);
            impulse.negated();

            switch (m_limitState) {
                case EQUAL -> m_impulse.addLocal(impulse);
                case AT_LOWER -> {
                    float newImpulse = m_impulse.z + impulse.z;
                    if (newImpulse < 0) {
                        v2 rhs = new v2();
                        rhs.set(m_mass.ez.x, m_mass.ez.y).scaled(m_impulse.z).subbed(Cdot1);
                        m_mass.solve22ToOut(rhs, temp);
                        impulse.x = temp.x;
                        impulse.y = temp.y;
                        impulse.z = -m_impulse.z;
                        m_impulse.x += temp.x;
                        m_impulse.y += temp.y;
                        m_impulse.z = 0;
                    } else {
                        m_impulse.addLocal(impulse);
                    }
                }
                case AT_UPPER -> {
                    float newImpulse = m_impulse.z + impulse.z;
                    if (newImpulse > 0) {
                        m_mass.solve22ToOut(new v2(m_mass.ez.x, m_mass.ez.y).scaled(m_impulse.z).subbed(Cdot1), temp);
                        impulse.x = temp.x;
                        impulse.y = temp.y;
                        impulse.z = -m_impulse.z;
                        m_impulse.x += temp.x;
                        m_impulse.y += temp.y;
                        m_impulse.z = 0;
                    } else {
                        m_impulse.addLocal(impulse);
                    }
                }
            }
            v2 P = new v2(impulse.x, impulse.y);

            vA.x -= mA * P.x;
            vA.y -= mA * P.y;
            wA -= iA * (v2.cross(m_rA, P) + impulse.z);

            vB.x += mB * P.x;
            vB.y += mB * P.y;
            wB += iB * (v2.cross(m_rB, P) + impulse.z);

        } else {

            
            v2 Cdot = new v2();
            v2 impulse = new v2();

            v2.crossToOutUnsafe(wA, m_rA, temp);
            v2.crossToOutUnsafe(wB, m_rB, Cdot);
            Cdot.added(vB).subbed(vA).subbed(temp).scaled(positionFactor);
            m_mass.solve22ToOut(Cdot.negated(), impulse); 

            m_impulse.x += impulse.x;
            m_impulse.y += impulse.y;

            vA.x -= mA * impulse.x;
            vA.y -= mA * impulse.y;
            wA -= iA * v2.cross(m_rA, impulse);

            vB.x += mB * impulse.x;
            vB.y += mB * impulse.y;
            wB += iB * v2.cross(m_rB, impulse);

        }

        
        V[m_indexA].w = wA;
        
        V[m_indexB].w = wB;


    }

    private boolean fixedRotation(float iA, float iB) {
        return Math.abs(iA) + Math.abs(iB) < EPSILON;
    }

    @Override
    public boolean solvePositionConstraints(SolverData data) {
        Rot qA = new Rot();
        Rot qB = new Rot();
        final Position[] P = data.positions;
        var cA = P[m_indexA];
        var cB = P[m_indexB];
        float aA = cA.a;
        float aB = cB.a;

        qA.set(aA);
        qB.set(aB);

        float angularError = 0;

        boolean fixedRotation = fixedRotation(m_invIA, m_invIB);

        if (m_enableLimit && m_limitState != LimitState.INACTIVE && !fixedRotation) {
            float angle = aB - aA - m_referenceAngle;
            float limitImpulse = 0;

            switch (m_limitState) {
                case EQUAL -> {

                    float low = -Settings.maxAngularCorrection;
                    float C =
                            Util.clamp(angle - m_lowerAngle, low, Settings.maxAngularCorrection);
                    limitImpulse = -m_motorMass * C;
                    angularError = Math.abs(C);
                }
                case AT_LOWER -> {
                    float C = angle - m_lowerAngle;
                    angularError = -C;


                    float low = -Settings.maxAngularCorrection;
                    C = Util.clamp(C + Settings.angularSlop, low, 0);
                    limitImpulse = -m_motorMass * C;
                }
                case AT_UPPER -> {
                    float C = angle - m_upperAngle;
                    angularError = C;


                    C = Util.clamp(C - Settings.angularSlop, 0, Settings.maxAngularCorrection);
                    limitImpulse = -m_motorMass * C;
                }
            }

            aA -= m_invIA * limitImpulse;
            aB += m_invIB * limitImpulse;
        }

        float positionError = 0;
        {
            qA.set(aA);
            qB.set(aB);

            v2 rA = new v2();
            v2 rB = new v2();
            v2 C = new v2();
            v2 impulse = new v2();

            Rot.mulToOutUnsafe(qA, C.set(localAnchorA).subbed(m_localCenterA), rA);
            Rot.mulToOutUnsafe(qB, C.set(localAnchorB).subbed(m_localCenterB), rB);
            C.set(cB).added(rB).subbed(cA).subbed(rA).scaled(positionFactor);
            positionError = C.length();


            float mA = m_invMassA, mB = m_invMassB;
            float iA = m_invIA, iB = m_invIB;

            Mat22 K = pool.popMat22();
            K.ex.x = mA + mB + iA * rA.y * rA.y + iB * rB.y * rB.y;
            K.ex.y = -iA * rA.x * rA.y - iB * rB.x * rB.y;
            K.ey.x = K.ex.y;
            K.ey.y = mA + mB + iA * rA.x * rA.x + iB * rB.x * rB.x;
            K.solveToOut(C, impulse);
            impulse.negated();

            cA.x -= mA * impulse.x;
            cA.y -= mA * impulse.y;
            aA -= iA * v2.cross(rA, impulse);

            cB.x += mB * impulse.x;
            cB.y += mB * impulse.y;
            aB += iB * v2.cross(rB, impulse);

            pool.pushMat22(1);
        }
        
        P[m_indexA].a = aA;
        P[m_indexB].a = aB;

        return positionError <= Settings.linearSlop && angularError <= Settings.angularSlop;
    }

    public v2 getLocalAnchorA() {
        return localAnchorA;
    }

    public v2 getLocalAnchorB() {
        return localAnchorB;
    }

//    public float getReferenceAngle() {
//        return m_referenceAngle;
//    }

    @Override
    public void anchorA(v2 argOut) {
        A.getWorldPointToOut(localAnchorA, argOut);
    }

    @Override
    public void anchorB(v2 argOut) {
        B.getWorldPointToOut(localAnchorB, argOut);
    }

    @Override
    public void reactionForce(float inv_dt, v2 argOut) {
        argOut.set(m_impulse.x, m_impulse.y).scaled(inv_dt);
    }

    @Override
    public float reactionTorque(float inv_dt) {
        return inv_dt * m_impulse.z;
    }

    public float getJointAngle() {
        return B.sweep.a - A.sweep.a - m_referenceAngle;
    }

    public float getJointSpeed() {
        return B.velAngular - A.velAngular;
    }

    public boolean isMotorEnabled() {
        return m_enableMotor;
    }

    public void enableMotor(boolean flag) {
        A.setAwake(true);
        B.setAwake(true);
        m_enableMotor = flag;
    }

    public float getMotorTorque(float inv_dt) {
        return m_motorImpulse * inv_dt;
    }

    public void setMotorSpeed(float speed) {
        A.setAwake(true);
        B.setAwake(true);
        m_motorSpeed = speed;
    }

    public void setMaxMotorTorque(float torque) {
        A.setAwake(true);
        B.setAwake(true);
        m_maxMotorTorque = torque;
    }

    public float getMotorSpeed() {
        return m_motorSpeed;
    }

    public float getMaxMotorTorque() {
        return m_maxMotorTorque;
    }

    public boolean isLimitEnabled() {
        return m_enableLimit;
    }

    public void enableLimit(boolean flag) {
        if (flag != m_enableLimit) {
            A.setAwake(true);
            B.setAwake(true);
            m_enableLimit = flag;
            m_impulse.z = 0;
        }
    }

    public float getLowerLimit() {
        return m_lowerAngle;
    }

    public float getUpperLimit() {
        return m_upperAngle;
    }

    public void setLimits(float lower, float upper) {
        //assert (lower <= upper);
        if (upper < lower) {
            //swap
            float t = upper;
            upper = lower;
            lower = t;
        }

        if (lower != m_lowerAngle || upper != m_upperAngle) {
            A.setAwake(true);
            B.setAwake(true);
            m_impulse.z = 0;
            m_lowerAngle = lower;
            m_upperAngle = upper;
        }
    }
}