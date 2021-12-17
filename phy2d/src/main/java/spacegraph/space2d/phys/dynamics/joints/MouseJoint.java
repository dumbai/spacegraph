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
import spacegraph.space2d.phys.common.*;
import spacegraph.space2d.phys.dynamics.SolverData;
import spacegraph.space2d.phys.pooling.IWorldPool;

/**
 * A mouse joint is used to make a point on a body track a specified world point. This a soft
 * constraint with a maximum force. This allows the constraint to stretch and without applying huge
 * forces. NOTE: this joint is not documented in the manual because it was developed to be used in
 * the testbed. If you want to learn how to use the mouse joint, look at the testbed.
 *
 * @author Daniel
 */
public class MouseJoint extends Joint {

    private final v2 m_localAnchorB = new v2();
    private final v2 m_targetA = new v2();
    private float m_frequencyHz;
    private float m_dampingRatio;
    private float m_beta;

    
    private final v2 m_impulse = new v2();
    private float m_maxForce;
    private float m_gamma;

    
    private int m_indexB;
    private final v2 m_rB = new v2();
    private final v2 m_localCenterB = new v2();
    private float m_invMassB;
    private float m_invIB;
    private final Mat22 m_mass = new Mat22();
    private final v2 m_C = new v2();

    public MouseJoint(IWorldPool argWorld, MouseJointDef def) {
        super(argWorld, def);
        assert (def.target.isValid());
        assert (def.maxForce >= 0);
        assert (def.frequencyHz >= 0);
        assert (def.dampingRatio >= 0);

        m_targetA.set(def.target);
        Transform.mulTransToOutUnsafe(B, m_targetA, m_localAnchorB);

        m_maxForce = def.maxForce;
        m_impulse.setZero();

        m_frequencyHz = def.frequencyHz;
        m_dampingRatio = def.dampingRatio;

        m_beta = 0;
        m_gamma = 0;
    }

    @Override
    public void anchorA(v2 argOut) {
        argOut.set(m_targetA);
    }

    @Override
    public void anchorB(v2 argOut) {
        B.getWorldPointToOut(m_localAnchorB, argOut);
    }

    @Override
    public void reactionForce(float invDt, v2 argOut) {
        argOut.set(m_impulse).scaled(invDt);
    }

    @Override
    public float reactionTorque(float invDt) {
        return invDt * 0.0f;
    }


    public void setTarget(v2 target) {
        if (!B.isAwake()) {
            B.setAwake(true);
        }
        m_targetA.set(target);
    }

    public v2 getTarget() {
        return m_targetA;
    }

    
    public void setMaxForce(float force) {
        m_maxForce = force;
    }

    public float getMaxForce() {
        return m_maxForce;
    }

    
    public void setFrequency(float hz) {
        m_frequencyHz = hz;
    }

    public float getFrequency() {
        return m_frequencyHz;
    }

    
    public void setDampingRatio(float ratio) {
        m_dampingRatio = ratio;
    }

    public float getDampingRatio() {
        return m_dampingRatio;
    }

    @Override
    public void initVelocityConstraints(SolverData data) {
        m_indexB = B.island;
        m_localCenterB.set(B.sweep.localCenter);
        m_invMassB = B.m_invMass;
        m_invIB = B.m_invI;

        v2 cB = data.positions[m_indexB];
        float aB = data.positions[m_indexB].a;
        v2 vB = data.velocities[m_indexB];
        float wB = data.velocities[m_indexB].w;

        Rot qB = pool.popRot();

        qB.set(aB);

        float mass = B.getMass();

        
        float omega = 2.0f * MathUtils.PI * m_frequencyHz;

        
        float d = 2.0f * mass * m_dampingRatio * omega;

        
        float k = mass * (omega * omega);

        
        
        
        float h = data.step.dt;
        assert (d + h * k > Settings.EPSILON);
        m_gamma = h * (d + h * k);
        if (m_gamma != 0.0f) {
            m_gamma = 1.0f / m_gamma;
        }
        m_beta = h * k * m_gamma;

        v2 temp = pool.popVec2();

        
        Rot.mulToOutUnsafe(qB, temp.set(m_localAnchorB).subbed(m_localCenterB), m_rB);

        
        
        
        Mat22 K = pool.popMat22();
        K.ex.x = m_invMassB + m_invIB * m_rB.y * m_rB.y + m_gamma;
        K.ex.y = -m_invIB * m_rB.x * m_rB.y;
        K.ey.x = K.ex.y;
        K.ey.y = m_invMassB + m_invIB * m_rB.x * m_rB.x + m_gamma;

        K.invertToOut(m_mass);

        m_C.set(cB).added(m_rB).subbed(m_targetA);
        m_C.scaled(m_beta);

        
        wB *= 0.98f;

        if (data.step.warmStarting) {
            m_impulse.scaled(data.step.dtRatio);
            vB.x += m_invMassB * m_impulse.x;
            vB.y += m_invMassB * m_impulse.y;
            wB += m_invIB * v2.cross(m_rB, m_impulse);
        } else {
            m_impulse.setZero();
        }


        data.velocities[m_indexB].w = wB;

        pool.pushVec2(1);
        pool.pushMat22(1);
        pool.pushRot(1);
    }

    @Override
    public boolean solvePositionConstraints(SolverData data) {
        return true;
    }

    @Override
    public void solveVelocityConstraints(SolverData data) {

        v2 vB = data.velocities[m_indexB];
        float wB = data.velocities[m_indexB].w;

        
        v2 Cdot = pool.popVec2();
        v2.crossToOutUnsafe(wB, m_rB, Cdot);
        Cdot.added(vB);

        v2 impulse = pool.popVec2();
        v2 temp = pool.popVec2();

        temp.set(m_impulse).scaled(m_gamma).added(m_C).added(Cdot).negated();
        Mat22.mulToOutUnsafe(m_mass, temp, impulse);

        v2 oldImpulse = temp;
        oldImpulse.set(m_impulse);
        m_impulse.added(impulse);
        float maxImpulse = data.step.dt * m_maxForce;
        float mImpulseLenSq = m_impulse.lengthSquared();
        if (mImpulseLenSq > maxImpulse * maxImpulse) {
            m_impulse.scaled((float) (maxImpulse / Math.sqrt(mImpulseLenSq)));
        }
        impulse.set(m_impulse).subbed(oldImpulse);

        vB.x += m_invMassB * impulse.x;
        vB.y += m_invMassB * impulse.y;
        wB += m_invIB * v2.cross(m_rB, impulse);


        data.velocities[m_indexB].w = wB;

        pool.pushVec2(3);
    }

}
