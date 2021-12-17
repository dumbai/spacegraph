package spacegraph.space2d.phys.dynamics.joints;

import jcog.Util;
import jcog.math.v2;
import spacegraph.space2d.phys.common.Mat22;
import spacegraph.space2d.phys.common.Rot;
import spacegraph.space2d.phys.dynamics.SolverData;
import spacegraph.space2d.phys.pooling.IWorldPool;


/**
 * A motor joint is used to control the relative motion between two bodies. A typical usage is to
 * control the movement of a dynamic body with respect to the ground.
 *
 * @author dmurph
 */
public class MotorJoint extends Joint {

    
    private final v2 m_linearOffset = new v2();
    private float m_angularOffset;
    private final v2 m_linearImpulse = new v2();
    private float m_angularImpulse;
    private float m_maxForce;
    private float m_maxTorque;
    private float m_correctionFactor;

    
    private int m_indexA;
    private int m_indexB;
    private final v2 m_rA = new v2();
    private final v2 m_rB = new v2();
    private final v2 m_localCenterA = new v2();
    private final v2 m_localCenterB = new v2();
    private final v2 m_linearError = new v2();
    private float m_angularError;
    private float m_invMassA;
    private float m_invMassB;
    private float m_invIA;
    private float m_invIB;
    private final Mat22 m_linearMass = new Mat22();
    private float m_angularMass;

    public MotorJoint(IWorldPool pool, MotorJointDef def) {
        super(pool, def);
        m_linearOffset.set(def.linearOffset);
        m_angularOffset = def.angularOffset;

        m_angularImpulse = 0.0f;

        m_maxForce = def.maxForce;
        m_maxTorque = def.maxTorque;
        m_correctionFactor = def.correctionFactor;
    }

    @Override
    public void anchorA(v2 out) {
        out.set(A.getPosition());
    }

    @Override
    public void anchorB(v2 out) {
        out.set(B.getPosition());
    }

    public void reactionForce(float inv_dt, v2 out) {
        out.set(m_linearImpulse).scaled(inv_dt);
    }

    public float reactionTorque(float inv_dt) {
        return m_angularImpulse * inv_dt;
    }

    public float getCorrectionFactor() {
        return m_correctionFactor;
    }

    public void setCorrectionFactor(float correctionFactor) {
        this.m_correctionFactor = correctionFactor;
    }

    /**
     * Set the target linear offset, in frame A, in meters.
     */
    public void setLinearOffset(v2 linearOffset) {
        if (linearOffset.x != m_linearOffset.x || linearOffset.y != m_linearOffset.y) {
            A.setAwake(true);
            B.setAwake(true);
            m_linearOffset.set(linearOffset);
        }
    }

    /**
     * Get the target linear offset, in frame A, in meters.
     */
    public void getLinearOffset(v2 out) {
        out.set(m_linearOffset);
    }

    /**
     * Get the target linear offset, in frame A, in meters. Do not modify.
     */
    public v2 getLinearOffset() {
        return m_linearOffset;
    }

    /**
     * Set the target angular offset, in radians.
     *
     * @param angularOffset
     */
    public void setAngularOffset(float angularOffset) {
        if (angularOffset != m_angularOffset) {
            A.setAwake(true);
            B.setAwake(true);
            m_angularOffset = angularOffset;
        }
    }

    public float getAngularOffset() {
        return m_angularOffset;
    }

    /**
     * Set the maximum friction force in N.
     *
     * @param force
     */
    public void setMaxForce(float force) {
        assert (force >= 0.0f);
        m_maxForce = force;
    }

    /**
     * Get the maximum friction force in N.
     */
    public float getMaxForce() {
        return m_maxForce;
    }

    /**
     * Set the maximum friction torque in N*m.
     */
    public void setMaxTorque(float torque) {
        assert (torque >= 0.0f);
        m_maxTorque = torque;
    }

    /**
     * Get the maximum friction torque in N*m.
     */
    public float getMaxTorque() {
        return m_maxTorque;
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
        v2 temp = new v2();
        Mat22 K = pool.popMat22();

        qA.set(aA);
        qB.set(aB);

        
        
        
        m_rA.x = qA.c * -m_localCenterA.x - qA.s * -m_localCenterA.y;
        m_rA.y = qA.s * -m_localCenterA.x + qA.c * -m_localCenterA.y;
        m_rB.x = qB.c * -m_localCenterB.x - qB.s * -m_localCenterB.y;
        m_rB.y = qB.s * -m_localCenterB.x + qB.c * -m_localCenterB.y;

        
        
        

        
        
        
        
        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        K.ex.x = mA + mB + iA * m_rA.y * m_rA.y + iB * m_rB.y * m_rB.y;
        K.ex.y = -iA * m_rA.x * m_rA.y - iB * m_rB.x * m_rB.y;
        K.ey.x = K.ex.y;
        K.ey.y = mA + mB + iA * m_rA.x * m_rA.x + iB * m_rB.x * m_rB.x;

        K.invertToOut(m_linearMass);

        m_angularMass = iA + iB;
        if (m_angularMass > 0.0f) {
            m_angularMass = 1.0f / m_angularMass;
        }

        
        Rot.mulToOutUnsafe(qA, m_linearOffset, temp);
        m_linearError.x = cB.x + m_rB.x - cA.x - m_rA.x - temp.x;
        m_linearError.y = cB.y + m_rB.y - cA.y - m_rA.y - temp.y;
        m_angularError = aB - aA - m_angularOffset;

        if (data.step.warmStarting) {
            
            m_linearImpulse.x *= data.step.dtRatio;
            m_linearImpulse.y *= data.step.dtRatio;
            m_angularImpulse *= data.step.dtRatio;

            v2 P = m_linearImpulse;
            vA.x -= mA * P.x;
            vA.y -= mA * P.y;
            wA -= iA * (m_rA.x * P.y - m_rA.y * P.x + m_angularImpulse);
            vB.x += mB * P.x;
            vB.y += mB * P.y;
            wB += iB * (m_rB.x * P.y - m_rB.y * P.x + m_angularImpulse);
        } else {
            m_linearImpulse.setZero();
            m_angularImpulse = 0.0f;
        }

        pool.pushMat22(1);
        pool.pushRot(2);

        
        data.velocities[m_indexA].w = wA;
        
        data.velocities[m_indexB].w = wB;
    }

    @Override
    public void solveVelocityConstraints(SolverData data) {
        v2 vA = data.velocities[m_indexA];
        float wA = data.velocities[m_indexA].w;
        v2 vB = data.velocities[m_indexB];
        float wB = data.velocities[m_indexB].w;

        float mA = m_invMassA, mB = m_invMassB;
        float iA = m_invIA, iB = m_invIB;

        float h = data.step.dt;
        float inv_h = data.step.inv_dt;

        v2 temp = new v2();

        
        {
            float Cdot = wB - wA + inv_h * m_correctionFactor * m_angularError;
            float impulse = -m_angularMass * Cdot;

            float oldImpulse = m_angularImpulse;
            float maxImpulse = h * m_maxTorque;
            float low = -maxImpulse;
            m_angularImpulse = Util.clamp(m_angularImpulse + impulse, low, maxImpulse);
            impulse = m_angularImpulse - oldImpulse;

            wA -= iA * impulse;
            wB += iB * impulse;
        }

        v2 Cdot = new v2();

        
        {
            
            
            Cdot.x =
                    vB.x + -wB * m_rB.y - vA.x - -wA * m_rA.y + inv_h * m_correctionFactor * m_linearError.x;
            Cdot.y =
                    vB.y + wB * m_rB.x - vA.y - wA * m_rA.x + inv_h * m_correctionFactor * m_linearError.y;

            v2 impulse = temp;
            Mat22.mulToOutUnsafe(m_linearMass, Cdot, impulse);
            impulse.negated();
            v2 oldImpulse = new v2();
            oldImpulse.set(m_linearImpulse);
            m_linearImpulse.added(impulse);

            float maxImpulse = h * m_maxForce;

            if (m_linearImpulse.lengthSquared() > maxImpulse * maxImpulse) {
                m_linearImpulse.normalize();
                m_linearImpulse.scaled(maxImpulse);
            }

            impulse.x = m_linearImpulse.x - oldImpulse.x;
            impulse.y = m_linearImpulse.y - oldImpulse.y;

            vA.x -= mA * impulse.x;
            vA.y -= mA * impulse.y;
            wA -= iA * (m_rA.x * impulse.y - m_rA.y * impulse.x);

            vB.x += mB * impulse.x;
            vB.y += mB * impulse.y;
            wB += iB * (m_rB.x * impulse.y - m_rB.y * impulse.x);
        }


        
        data.velocities[m_indexA].w = wA;
        
        data.velocities[m_indexB].w = wB;
    }

    @Override
    public boolean solvePositionConstraints(SolverData data) {
        return true;
    }
}
