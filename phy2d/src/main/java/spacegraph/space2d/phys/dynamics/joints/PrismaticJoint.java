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
import spacegraph.space2d.phys.dynamics.Body2D;
import spacegraph.space2d.phys.dynamics.SolverData;
import spacegraph.space2d.phys.pooling.IWorldPool;

import static java.lang.Math.max;
import static java.lang.Math.min;


/**
 * A prismatic joint. This joint provides one degree of freedom: translation along an axis fixed in
 * bodyA. Relative rotation is prevented. You can use a joint limit to restrict the range of motion
 * and a joint motor to drive the motion or to model joint friction.
 *
 * @author Daniel
 */
public class PrismaticJoint extends Joint {

    
    final v2 m_localAnchorA;
    final v2 m_localAnchorB;
    final v2 m_localXAxisA;
    private final v2 m_localYAxisA;
    final float m_referenceAngle;
    private final Vec3 m_impulse;
    private float m_motorImpulse;
    private float m_lowerTranslation;
    private float m_upperTranslation;
    private float m_maxMotorForce;
    private float m_motorSpeed;
    private boolean m_enableLimit;
    private boolean m_enableMotor;
    private LimitState m_limitState;

    
    private int m_indexA;
    private int m_indexB;
    private final v2 m_localCenterA = new v2();
    private final v2 m_localCenterB = new v2();
    private float m_invMassA;
    private float m_invMassB;
    private float m_invIA;
    private float m_invIB;
    private final v2 m_axis;
    private final v2 m_perp;
    private float m_s1;
    private float m_s2;
    private float m_a1;
    private float m_a2;
    private final Mat33 m_K;
    private float m_motorMass; 

    PrismaticJoint(IWorldPool argWorld, PrismaticJointDef def) {
        super(argWorld, def);
        m_localAnchorA = new v2(def.localAnchorA);
        m_localAnchorB = new v2(def.localAnchorB);
        m_localXAxisA = new v2(def.localAxisA);
        m_localXAxisA.normalize();
        m_localYAxisA = new v2();
        v2.crossToOutUnsafe(1.0f, m_localXAxisA, m_localYAxisA);
        m_referenceAngle = def.referenceAngle;

        m_impulse = new Vec3();
        m_motorMass = 0.0f;
        m_motorImpulse = 0.0f;

        m_lowerTranslation = def.lowerTranslation;
        m_upperTranslation = def.upperTranslation;
        m_maxMotorForce = def.maxMotorForce;
        m_motorSpeed = def.motorSpeed;
        m_enableLimit = def.enableLimit;
        m_enableMotor = def.enableMotor;
        m_limitState = LimitState.INACTIVE;

        m_K = new Mat33();
        m_axis = new v2();
        m_perp = new v2();
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
        temp.set(m_axis).scaled(m_motorImpulse + m_impulse.z);
        argOut.set(m_perp).scaled(m_impulse.x).added(temp).scaled(inv_dt);
        pool.pushVec2(1);
    }

    @Override
    public float reactionTorque(float inv_dt) {
        return inv_dt * m_impulse.y;
    }

    /**
     * Get the current joint translation, usually in meters.
     */
    public float getJointSpeed() {
        Body2D bA = A;
        Body2D bB = B;

        v2 temp = pool.popVec2();
        v2 rA = pool.popVec2();
        v2 rB = pool.popVec2();
        v2 p1 = pool.popVec2();
        v2 p2 = pool.popVec2();
        v2 d = pool.popVec2();
        v2 axis = pool.popVec2();
        v2 temp2 = pool.popVec2();
        v2 temp3 = pool.popVec2();

        temp.set(m_localAnchorA).subbed(bA.sweep.localCenter);
        Rot.mulToOutUnsafe(bA, temp, rA);

        temp.set(m_localAnchorB).subbed(bB.sweep.localCenter);
        Rot.mulToOutUnsafe(bB, temp, rB);

        p1.set(bA.sweep.c).added(rA);
        p2.set(bB.sweep.c).added(rB);

        d.set(p2).subbed(p1);
        Rot.mulToOutUnsafe(bA, m_localXAxisA, axis);

        v2 vA = bA.vel;
        v2 vB = bB.vel;
        float wA = bA.velAngular;
        float wB = bB.velAngular;


        v2.crossToOutUnsafe(wA, axis, temp);
        v2.crossToOutUnsafe(wB, rB, temp2);
        v2.crossToOutUnsafe(wA, rA, temp3);

        temp2.added(vB).subbed(vA).subbed(temp3);
        float speed = v2.dot(d, temp) + v2.dot(axis, temp2);

        pool.pushVec2(9);

        return speed;
    }

    public float getJointTranslation() {
        v2 pA = pool.popVec2(), pB = pool.popVec2(), axis = pool.popVec2();
        A.getWorldPointToOut(m_localAnchorA, pA);
        B.getWorldPointToOut(m_localAnchorB, pB);
        A.getWorldVectorToOutUnsafe(m_localXAxisA, axis);
        pB.subbed(pA);
        float translation = v2.dot(pB, axis);
        pool.pushVec2(3);
        return translation;
    }

    /**
     * Is the joint limit enabled?
     *
     * @return
     */
    public boolean isLimitEnabled() {
        return m_enableLimit;
    }

    /**
     * Enable/disable the joint limit.
     *
     * @param flag
     */
    public void enableLimit(boolean flag) {
        if (flag != m_enableLimit) {
            A.setAwake(true);
            B.setAwake(true);
            m_enableLimit = flag;
            m_impulse.z = 0.0f;
        }
    }

    /**
     * Get the lower joint limit, usually in meters.
     *
     * @return
     */
    public float getLowerLimit() {
        return m_lowerTranslation;
    }

    /**
     * Get the upper joint limit, usually in meters.
     *
     * @return
     */
    public float getUpperLimit() {
        return m_upperTranslation;
    }

    /**
     * Set the joint limits, usually in meters.
     *
     * @param lower
     * @param upper
     */
    public void setLimits(float lower, float upper) {
        assert (lower <= upper);
        if (lower != m_lowerTranslation || upper != m_upperTranslation) {
            A.setAwake(true);
            B.setAwake(true);
            m_lowerTranslation = lower;
            m_upperTranslation = upper;
            m_impulse.z = 0.0f;
        }
    }

    /**
     * Is the joint motor enabled?
     *
     * @return
     */
    public boolean isMotorEnabled() {
        return m_enableMotor;
    }

    /**
     * Enable/disable the joint motor.
     *
     * @param flag
     */
    public void enableMotor(boolean flag) {
        A.setAwake(true);
        B.setAwake(true);
        m_enableMotor = flag;
    }

    /**
     * Set the motor speed, usually in meters per second.
     *
     * @param speed
     */
    public void setMotorSpeed(float speed) {
        A.setAwake(true);
        B.setAwake(true);
        m_motorSpeed = speed;
    }

    /**
     * Get the motor speed, usually in meters per second.
     *
     * @return
     */
    public float getMotorSpeed() {
        return m_motorSpeed;
    }

    /**
     * Set the maximum motor force, usually in N.
     *
     * @param force
     */
    public void setMaxMotorForce(float force) {
        A.setAwake(true);
        B.setAwake(true);
        m_maxMotorForce = force;
    }

    /**
     * Get the current motor force, usually in N.
     *
     * @param inv_dt
     * @return
     */
    public float getMotorForce(float inv_dt) {
        return m_motorImpulse * inv_dt;
    }

    public float getMaxMotorForce() {
        return m_maxMotorForce;
    }

    public float getReferenceAngle() {
        return m_referenceAngle;
    }

    public v2 getLocalAxisA() {
        return m_localXAxisA;
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
        v2 d = pool.popVec2();
        v2 temp = pool.popVec2();
        v2 rA = pool.popVec2();
        v2 rB = pool.popVec2();

        qA.set(aA);
        qB.set(aB);

        
        Rot.mulToOutUnsafe(qA, d.set(m_localAnchorA).subbed(m_localCenterA), rA);
        Rot.mulToOutUnsafe(qB, d.set(m_localAnchorB).subbed(m_localCenterB), rB);
        d.set(cB).subbed(cA).added(rB).subbed(rA);

        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        
        {
            Rot.mulToOutUnsafe(qA, m_localXAxisA, m_axis);
            temp.set(d).added(rA);
            m_a1 = v2.cross(temp, m_axis);
            m_a2 = v2.cross(rB, m_axis);

            m_motorMass = mA + mB + iA * m_a1 * m_a1 + iB * m_a2 * m_a2;
            if (m_motorMass > 0.0f) {
                m_motorMass = 1.0f / m_motorMass;
            }
        }

        
        {
            Rot.mulToOutUnsafe(qA, m_localYAxisA, m_perp);

            temp.set(d).added(rA);
            m_s1 = v2.cross(temp, m_perp);
            m_s2 = v2.cross(rB, m_perp);

            float k11 = mA + mB + iA * m_s1 * m_s1 + iB * m_s2 * m_s2;
            float k12 = iA * m_s1 + iB * m_s2;
            float k13 = iA * m_s1 * m_a1 + iB * m_s2 * m_a2;
            float k22 = iA + iB;
            if (k22 == 0.0f) {
                
                k22 = 1.0f;
            }
            float k23 = iA * m_a1 + iB * m_a2;
            float k33 = mA + mB + iA * m_a1 * m_a1 + iB * m_a2 * m_a2;

            m_K.ex.set(k11, k12, k13);
            m_K.ey.set(k12, k22, k23);
            m_K.ez.set(k13, k23, k33);
        }

        
        if (m_enableLimit) {

            float jointTranslation = v2.dot(m_axis, d);
            if (Math.abs(m_upperTranslation - m_lowerTranslation) < 2.0f * Settings.linearSlop) {
                m_limitState = LimitState.EQUAL;
            } else if (jointTranslation <= m_lowerTranslation) {
                if (m_limitState != LimitState.AT_LOWER) {
                    m_limitState = LimitState.AT_LOWER;
                    m_impulse.z = 0.0f;
                }
            } else if (jointTranslation >= m_upperTranslation) {
                if (m_limitState != LimitState.AT_UPPER) {
                    m_limitState = LimitState.AT_UPPER;
                    m_impulse.z = 0.0f;
                }
            } else {
                m_limitState = LimitState.INACTIVE;
                m_impulse.z = 0.0f;
            }
        } else {
            m_limitState = LimitState.INACTIVE;
            m_impulse.z = 0.0f;
        }

        if (!m_enableMotor) {
            m_motorImpulse = 0.0f;
        }

        if (data.step.warmStarting) {

            m_impulse.scaled(data.step.dtRatio);
            m_motorImpulse *= data.step.dtRatio;

            v2 P = pool.popVec2();
            temp.set(m_axis).scaled(m_motorImpulse + m_impulse.z);
            P.set(m_perp).scaled(m_impulse.x).added(temp);

            float LA = m_impulse.x * m_s1 + m_impulse.y + (m_motorImpulse + m_impulse.z) * m_a1;
            float LB = m_impulse.x * m_s2 + m_impulse.y + (m_motorImpulse + m_impulse.z) * m_a2;

            vA.x -= mA * P.x;
            vA.y -= mA * P.y;
            wA -= iA * LA;

            vB.x += mB * P.x;
            vB.y += mB * P.y;
            wB += iB * LB;

            pool.pushVec2(1);
        } else {
            m_impulse.zero();
            m_motorImpulse = 0.0f;
        }

        
        data.velocities[m_indexA].w = wA;
        
        data.velocities[m_indexB].w = wB;

        pool.pushRot(2);
        pool.pushVec2(4);
    }

    @Override
    public void solveVelocityConstraints(SolverData data) {
        v2 vA = data.velocities[m_indexA];
        float wA = data.velocities[m_indexA].w;
        v2 vB = data.velocities[m_indexB];
        float wB = data.velocities[m_indexB].w;

        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        v2 temp = pool.popVec2();

        
        if (m_enableMotor && m_limitState != LimitState.EQUAL) {
            temp.set(vB).subbed(vA);
            float Cdot = v2.dot(m_axis, temp) + m_a2 * wB - m_a1 * wA;
            float impulse = m_motorMass * (m_motorSpeed - Cdot);
            float oldImpulse = m_motorImpulse;
            float maxImpulse = data.step.dt * m_maxMotorForce;
            float low = -maxImpulse;
            m_motorImpulse = Util.clamp(m_motorImpulse + impulse, low, maxImpulse);
            impulse = m_motorImpulse - oldImpulse;

            v2 P = pool.popVec2();
            P.set(m_axis).scaled(impulse);
            float LA = impulse * m_a1;
            float LB = impulse * m_a2;

            vA.x -= mA * P.x;
            vA.y -= mA * P.y;
            wA -= iA * LA;

            vB.x += mB * P.x;
            vB.y += mB * P.y;
            wB += iB * LB;

            pool.pushVec2(1);
        }

        v2 Cdot1 = pool.popVec2();
        temp.set(vB).subbed(vA);
        Cdot1.x = v2.dot(m_perp, temp) + m_s2 * wB - m_s1 * wA;
        Cdot1.y = wB - wA;
        

        if (m_enableLimit && m_limitState != LimitState.INACTIVE) {

            temp.set(vB).subbed(vA);
            float Cdot2 = v2.dot(m_axis, temp) + m_a2 * wB - m_a1 * wA;

            v3 Cdot = pool.popVec3();
            Cdot.set(Cdot1.x, Cdot1.y, Cdot2);

            v3 f1 = pool.popVec3();

            v3 df = pool.popVec3();

            f1.set(m_impulse);
            Cdot.negated();
            m_K.solve33ToOut(Cdot, df);
            
            m_impulse.addLocal(df);

            switch (m_limitState) {
                case AT_LOWER -> m_impulse.z = max(m_impulse.z, 0.0f);
                case AT_UPPER -> m_impulse.z = min(m_impulse.z, 0.0f);
            }

            
            
            v2 b = pool.popVec2();
            v2 f2r = pool.popVec2();

            temp.set(m_K.ez.x, m_K.ez.y).scaled(m_impulse.z - f1.z);
            b.set(Cdot1).negated().subbed(temp);

            m_K.solve22ToOut(b, f2r);
            f2r.added(f1.x, f1.y);
            m_impulse.x = f2r.x;
            m_impulse.y = f2r.y;


            df.set(m_impulse);
            df.subbed(f1);

            v2 P = pool.popVec2();
            temp.set(m_axis).scaled(df.z);
            P.set(m_perp).scaled(df.x).added(temp);

            float LA = df.x * m_s1 + df.y + df.z * m_a1;

            vA.x -= mA * P.x;
            vA.y -= mA * P.y;
            wA -= iA * LA;

            float LB = df.x * m_s2 + df.y + df.z * m_a2;
            vB.x += mB * P.x;
            vB.y += mB * P.y;
            wB += iB * LB;

            pool.pushVec2(3);
            pool.pushVec3(3);
        } else {
            
            v2 df = pool.popVec2();
            m_K.solve22ToOut(Cdot1.negated(), df);
            Cdot1.negated();

            m_impulse.x += df.x;
            m_impulse.y += df.y;

            v2 P = pool.popVec2();
            P.set(m_perp).scaled(df.x);
            float LA = df.x * m_s1 + df.y;
            float LB = df.x * m_s2 + df.y;

            vA.x -= mA * P.x;
            vA.y -= mA * P.y;
            wA -= iA * LA;

            vB.x += mB * P.x;
            vB.y += mB * P.y;
            wB += iB * LB;

            pool.pushVec2(2);
        }

        
        data.velocities[m_indexA].w = wA;
        
        data.velocities[m_indexB].w = wB;

        pool.pushVec2(2);
    }


    @Override
    public boolean solvePositionConstraints(SolverData data) {

        Rot qA = pool.popRot();
        Rot qB = pool.popRot();
        v2 rA = pool.popVec2();
        v2 rB = pool.popVec2();
        v2 d = pool.popVec2();
        v2 axis = pool.popVec2();
        v2 perp = pool.popVec2();
        v2 temp = pool.popVec2();
        v2 C1 = pool.popVec2();

        v3 impulse = pool.popVec3();

        v2 cA = data.positions[m_indexA];
        float aA = data.positions[m_indexA].a;
        v2 cB = data.positions[m_indexB];
        float aB = data.positions[m_indexB].a;

        qA.set(aA);
        qB.set(aB);

        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        
        Rot.mulToOutUnsafe(qA, temp.set(m_localAnchorA).subbed(m_localCenterA), rA);
        Rot.mulToOutUnsafe(qB, temp.set(m_localAnchorB).subbed(m_localCenterB), rB);
        d.set(cB).added(rB).subbed(cA).subbed(rA);

        Rot.mulToOutUnsafe(qA, m_localXAxisA, axis);
        float a1 = v2.cross(temp.set(d).added(rA), axis);
        float a2 = v2.cross(rB, axis);
        Rot.mulToOutUnsafe(qA, m_localYAxisA, perp);

        float s1 = v2.cross(temp.set(d).added(rA), perp);
        float s2 = v2.cross(rB, perp);

        C1.x = v2.dot(perp, d);
        C1.y = aB - aA - m_referenceAngle;

        float linearError = Math.abs(C1.x);
        float angularError = Math.abs(C1.y);

        boolean active = false;
        float C2 = 0.0f;
        if (m_enableLimit) {
            float translation = v2.dot(axis, d);
            if (Math.abs(m_upperTranslation - m_lowerTranslation) < 2.0f * Settings.linearSlop) {

                float low = -Settings.maxLinearCorrection;
                C2 =
                        Util.clamp(translation, low, Settings.maxLinearCorrection);
                linearError = max(linearError, Math.abs(translation));
                active = true;
            } else if (translation <= m_lowerTranslation) {

                float low = -Settings.maxLinearCorrection;
                C2 =
                        Util.clamp(translation - m_lowerTranslation + Settings.linearSlop, low, 0.0f);
                linearError = max(linearError, m_lowerTranslation - translation);
                active = true;
            } else if (translation >= m_upperTranslation) {

                C2 =
                        Util.clamp(translation - m_upperTranslation - Settings.linearSlop, 0.0f, Settings.maxLinearCorrection);
                linearError = max(linearError, translation - m_upperTranslation);
                active = true;
            }
        }

        if (active) {
            float k22 = iA + iB;
            if (k22 == 0.0f) {
                
                k22 = 1.0f;
            }

            Mat33 K = pool.popMat33();
            float k13 = iA * s1 * a1 + iB * s2 * a2;
            float k12 = iA * s1 + iB * s2;
            float k11 = mA + mB + iA * s1 * s1 + iB * s2 * s2;
            K.ex.set(k11, k12, k13);
            float k23 = iA * a1 + iB * a2;
            K.ey.set(k12, k22, k23);
            float k33 = mA + mB + iA * a1 * a1 + iB * a2 * a2;
            K.ez.set(k13, k23, k33);

            v3 C = pool.popVec3();
            C.x = C1.x;
            C.y = C1.y;
            C.z = C2;

            C.negated();
            K.solve33ToOut(C, impulse);
            pool.pushVec3(1);
            pool.pushMat33(1);
        } else {
            float k22 = iA + iB;
            if (k22 == 0.0f) {
                k22 = 1.0f;
            }

            Mat22 K = pool.popMat22();
            float k12 = iA * s1 + iB * s2;
            float k11 = mA + mB + iA * s1 * s1 + iB * s2 * s2;
            K.ex.set(k11, k12);
            K.ey.set(k12, k22);

            
            K.solveToOut(C1.negated(), temp);
            C1.negated();

            impulse.x = temp.x;
            impulse.y = temp.y;
            impulse.z = 0.0f;

            pool.pushMat22(1);
        }

        float Px = impulse.x * perp.x + impulse.z * axis.x;
        float Py = impulse.x * perp.y + impulse.z * axis.y;
        float LA = impulse.x * s1 + impulse.y + impulse.z * a1;
        float LB = impulse.x * s2 + impulse.y + impulse.z * a2;

        cA.x -= mA * Px;
        cA.y -= mA * Py;
        aA -= iA * LA;
        cB.x += mB * Px;
        cB.y += mB * Py;
        aB += iB * LB;

        
        data.positions[m_indexA].a = aA;
        
        data.positions[m_indexB].a = aB;

        pool.pushVec2(7);
        pool.pushVec3(1);
        pool.pushRot(2);

        return linearError <= Settings.linearSlop && angularError <= Settings.angularSlop;
    }
}