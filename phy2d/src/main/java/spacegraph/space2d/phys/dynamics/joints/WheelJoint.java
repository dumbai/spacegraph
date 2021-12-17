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
import spacegraph.space2d.phys.common.MathUtils;
import spacegraph.space2d.phys.common.Rot;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.dynamics.Body2D;
import spacegraph.space2d.phys.dynamics.SolverData;
import spacegraph.space2d.phys.pooling.IWorldPool;


/**
 * A wheel joint. This joint provides two degrees of freedom: translation along an axis fixed in
 * bodyA and rotation in the plane. You can use a joint limit to restrict the range of motion and a
 * joint motor to drive the rotation or to model rotational friction. This joint is designed for
 * vehicle suspensions.
 *
 * @author Daniel Murphy
 */
public class WheelJoint extends Joint {

    private float m_frequencyHz;
    private float m_dampingRatio;

    
    private final v2 m_localAnchorA = new v2();
    private final v2 m_localAnchorB = new v2();
    private final v2 m_localXAxisA = new v2();
    private final v2 m_localYAxisA = new v2();

    private float m_impulse;
    private float m_motorImpulse;
    private float m_springImpulse;

    private float m_maxMotorTorque;
    private float m_motorSpeed;
    private boolean m_enableMotor;

    
    private int m_indexA;
    private int m_indexB;
    private final v2 m_localCenterA = new v2();
    private final v2 m_localCenterB = new v2();
    private float m_invMassA;
    private float m_invMassB;
    private float m_invIA;
    private float m_invIB;

    private final v2 m_ax = new v2();
    private final v2 m_ay = new v2();
    private float m_sAx;
    private float m_sBx;
    private float m_sAy;
    private float m_sBy;

    private float m_mass;
    private float m_motorMass;
    private float m_springMass;

    private float m_bias;
    private float m_gamma;

    WheelJoint(IWorldPool argPool, WheelJointDef def) {
        super(argPool, def);
        m_localAnchorA.set(def.localAnchorA);
        m_localAnchorB.set(def.localAnchorB);
        m_localXAxisA.set(def.localAxisA);
        v2.crossToOutUnsafe(1.0f, m_localXAxisA, m_localYAxisA);


        m_motorMass = 0.0f;
        m_motorImpulse = 0.0f;

        m_maxMotorTorque = def.maxMotorTorque;
        m_motorSpeed = def.motorSpeed;
        m_enableMotor = def.enableMotor;

        m_frequencyHz = def.frequencyHz;
        m_dampingRatio = def.dampingRatio;
    }

    public v2 getLocalAnchorA() {
        return m_localAnchorA;
    }

    public v2 getLocalAnchorB() {
        return m_localAnchorB;
    }

    @Override
    public void anchorA(v2 argOut) {
        A.getWorldPointToOut(m_localAnchorA, argOut);
    }

    @Override
    public void anchorB(v2 argOut) {
        B.getWorldPointToOut(m_localAnchorB, argOut);
    }

    @Override
    public void reactionForce(float inv_dt, v2 argOut) {
        v2 temp = pool.popVec2();
        temp.set(m_ay).scaled(m_impulse);
        argOut.set(m_ax).scaled(m_springImpulse).added(temp).scaled(inv_dt);
        pool.pushVec2(1);
    }

    @Override
    public float reactionTorque(float inv_dt) {
        return inv_dt * m_motorImpulse;
    }

    public float getJointTranslation() {
        Body2D b1 = A;
        Body2D b2 = B;

        v2 p1 = pool.popVec2();
        v2 p2 = pool.popVec2();
        v2 axis = pool.popVec2();
        b1.getWorldPointToOut(m_localAnchorA, p1);
        b2.getWorldPointToOut(m_localAnchorA, p2);
        p2.subbed(p1);
        b1.getWorldVectorToOut(m_localXAxisA, axis);

        float translation = v2.dot(p2, axis);
        pool.pushVec2(3);
        return translation;
    }

    /**
     * For serialization
     */
    public v2 getLocalAxisA() {
        return m_localXAxisA;
    }

    public float getJointSpeed() {
        return A.velAngular - B.velAngular;
    }

    public boolean isMotorEnabled() {
        return m_enableMotor;
    }

    public void enableMotor(boolean flag) {
        A.setAwake(true);
        B.setAwake(true);
        m_enableMotor = flag;
    }

    public void setMotorSpeed(float speed) {
        A.setAwake(true);
        B.setAwake(true);
        m_motorSpeed = speed;
    }

    public float getMotorSpeed() {
        return m_motorSpeed;
    }

    public float getMaxMotorTorque() {
        return m_maxMotorTorque;
    }

    public void setMaxMotorTorque(float torque) {
        A.setAwake(true);
        B.setAwake(true);
        m_maxMotorTorque = torque;
    }

    public float getMotorTorque(float inv_dt) {
        return m_motorImpulse * inv_dt;
    }

    public void setSpringFrequencyHz(float hz) {
        m_frequencyHz = hz;
    }

    public float getSpringFrequencyHz() {
        return m_frequencyHz;
    }

    public void setSpringDampingRatio(float ratio) {
        m_dampingRatio = ratio;
    }

    public float getSpringDampingRatio() {
        return m_dampingRatio;
    }

    
    private final v2 rA = new v2();
    private final v2 rB = new v2();
    private final v2 d = new v2();

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

        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        v2 cA = data.positions[m_indexA];
        float aA = data.positions[m_indexA].a;
        v2 vA = data.velocities[m_indexA];
        float wA = data.velocities[m_indexA].w;

        v2 cB = data.positions[m_indexB];
        float aB = data.positions[m_indexB].a;
        v2 vB = data.velocities[m_indexB];
        float wB = data.velocities[m_indexB].w;

        Rot qA = pool.popRot();
        Rot qB = pool.popRot();
        v2 temp = pool.popVec2();

        qA.set(aA);
        qB.set(aB);

        
        Rot.mulToOutUnsafe(qA, temp.set(m_localAnchorA).subbed(m_localCenterA), rA);
        Rot.mulToOutUnsafe(qB, temp.set(m_localAnchorB).subbed(m_localCenterB), rB);
        d.set(cB).added(rB).subbed(cA).subbed(rA);

        
        {
            Rot.mulToOut(qA, m_localYAxisA, m_ay);
            m_sAy = v2.cross(temp.set(d).added(rA), m_ay);
            m_sBy = v2.cross(rB, m_ay);

            m_mass = mA + mB + iA * m_sAy * m_sAy + iB * m_sBy * m_sBy;

            if (m_mass > 0.0f) {
                m_mass = 1.0f / m_mass;
            }
        }

        
        m_springMass = 0.0f;
        m_bias = 0.0f;
        m_gamma = 0.0f;
        if (m_frequencyHz > 0.0f) {
            Rot.mulToOut(qA, m_localXAxisA, m_ax);
            m_sAx = v2.cross(temp.set(d).added(rA), m_ax);
            m_sBx = v2.cross(rB, m_ax);

            float invMass = mA + mB + iA * m_sAx * m_sAx + iB * m_sBx * m_sBx;

            if (invMass > 0.0f) {
                m_springMass = 1.0f / invMass;

                float C = v2.dot(d, m_ax);

                
                float omega = 2.0f * MathUtils.PI * m_frequencyHz;

                
                float d = 2.0f * m_springMass * m_dampingRatio * omega;

                
                float k = m_springMass * omega * omega;

                
                float h = data.step.dt;
                m_gamma = h * (d + h * k);
                if (m_gamma > 0.0f) {
                    m_gamma = 1.0f / m_gamma;
                }

                m_bias = C * h * k * m_gamma;

                m_springMass = invMass + m_gamma;
                if (m_springMass > 0.0f) {
                    m_springMass = 1.0f / m_springMass;
                }
            }
        } else {
            m_springImpulse = 0.0f;
        }

        
        if (m_enableMotor) {
            m_motorMass = iA + iB;
            if (m_motorMass > 0.0f) {
                m_motorMass = 1.0f / m_motorMass;
            }
        } else {
            m_motorMass = 0.0f;
            m_motorImpulse = 0.0f;
        }

        if (data.step.warmStarting) {
            v2 P = pool.popVec2();
            
            m_impulse *= data.step.dtRatio;
            m_springImpulse *= data.step.dtRatio;
            m_motorImpulse *= data.step.dtRatio;

            P.x = m_impulse * m_ay.x + m_springImpulse * m_ax.x;
            P.y = m_impulse * m_ay.y + m_springImpulse * m_ax.y;
            float LA = m_impulse * m_sAy + m_springImpulse * m_sAx + m_motorImpulse;
            float LB = m_impulse * m_sBy + m_springImpulse * m_sBx + m_motorImpulse;

            vA.x -= m_invMassA * P.x;
            vA.y -= m_invMassA * P.y;
            wA -= m_invIA * LA;

            vB.x += m_invMassB * P.x;
            vB.y += m_invMassB * P.y;
            wB += m_invIB * LB;
            pool.pushVec2(1);
        } else {
            m_impulse = 0.0f;
            m_springImpulse = 0.0f;
            m_motorImpulse = 0.0f;
        }
        pool.pushRot(2);
        pool.pushVec2(1);

        
        data.velocities[m_indexA].w = wA;
        
        data.velocities[m_indexB].w = wB;
    }

    @Override
    public void solveVelocityConstraints(SolverData data) {
        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        v2 vA = data.velocities[m_indexA];
        float wA = data.velocities[m_indexA].w;
        v2 vB = data.velocities[m_indexB];
        float wB = data.velocities[m_indexB].w;

        v2 temp = pool.popVec2();
        v2 P = pool.popVec2();

        
        {
            float Cdot = v2.dot(m_ax, temp.set(vB).subbed(vA)) + m_sBx * wB - m_sAx * wA;
            float impulse = -m_springMass * (Cdot + m_bias + m_gamma * m_springImpulse);
            m_springImpulse += impulse;

            P.x = impulse * m_ax.x;
            P.y = impulse * m_ax.y;
            float LA = impulse * m_sAx;
            float LB = impulse * m_sBx;

            vA.x -= mA * P.x;
            vA.y -= mA * P.y;
            wA -= iA * LA;

            vB.x += mB * P.x;
            vB.y += mB * P.y;
            wB += iB * LB;
        }

        
        {
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

        
        {
            float Cdot = v2.dot(m_ay, temp.set(vB).subbed(vA)) + m_sBy * wB - m_sAy * wA;
            float impulse = -m_mass * Cdot;
            m_impulse += impulse;

            P.x = impulse * m_ay.x;
            P.y = impulse * m_ay.y;
            float LA = impulse * m_sAy;
            float LB = impulse * m_sBy;

            vA.x -= mA * P.x;
            vA.y -= mA * P.y;
            wA -= iA * LA;

            vB.x += mB * P.x;
            vB.y += mB * P.y;
            wB += iB * LB;
        }
        pool.pushVec2(2);

        
        data.velocities[m_indexA].w = wA;
        
        data.velocities[m_indexB].w = wB;
    }

    @Override
    public boolean solvePositionConstraints(SolverData data) {
        v2 cA = data.positions[m_indexA];
        float aA = data.positions[m_indexA].a;
        v2 cB = data.positions[m_indexB];
        float aB = data.positions[m_indexB].a;

        Rot qA = pool.popRot();
        Rot qB = pool.popRot();
        v2 temp = pool.popVec2();

        qA.set(aA);
        qB.set(aB);

        Rot.mulToOut(qA, temp.set(m_localAnchorA).subbed(m_localCenterA), rA);
        Rot.mulToOut(qB, temp.set(m_localAnchorB).subbed(m_localCenterB), rB);
        d.set(cB).subbed(cA).added(rB).subbed(rA);

        v2 ay = pool.popVec2();
        Rot.mulToOut(qA, m_localYAxisA, ay);

        float sAy = v2.cross(temp.set(d).added(rA), ay);
        float sBy = v2.cross(rB, ay);

        float C = v2.dot(d, ay);

        float k = m_invMassA + m_invMassB + m_invIA * m_sAy * m_sAy + m_invIB * m_sBy * m_sBy;

        float impulse;
		impulse = k == 0.0f ? 0.0f : -C / k;

        v2 P = pool.popVec2();
        P.x = impulse * ay.x;
        P.y = impulse * ay.y;
        float LA = impulse * sAy;
        float LB = impulse * sBy;

        cA.x -= m_invMassA * P.x;
        cA.y -= m_invMassA * P.y;
        aA -= m_invIA * LA;
        cB.x += m_invMassB * P.x;
        cB.y += m_invMassB * P.y;
        aB += m_invIB * LB;

        pool.pushVec2(3);
        pool.pushRot(2);
        
        data.positions[m_indexA].a = aA;
        
        data.positions[m_indexB].a = aB;

        return Math.abs(C) <= Settings.linearSlop;
    }
}
