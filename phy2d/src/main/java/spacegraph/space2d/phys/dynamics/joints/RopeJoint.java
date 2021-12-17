package spacegraph.space2d.phys.dynamics.joints;

import jcog.math.v2;
import spacegraph.space2d.phys.common.Rot;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.dynamics.SolverData;
import spacegraph.space2d.phys.dynamics.contacts.Velocity;
import spacegraph.space2d.phys.pooling.IWorldPool;

/**
 * A rope joint enforces a maximum distance between two points on two bodies. It has no other
 * effect. Warning: if you attempt to change the maximum length during the simulation you will get
 * some non-physical behavior. A model that would allow you to dynamically modify the length would
 * have some sponginess, so I chose not to implement it that way. See DistanceJoint if you want to
 * dynamically control length.
 *
 * @author Daniel Murphy
 */
public class RopeJoint extends Joint {
    
    private final v2 localAnchorA = new v2();
    private final v2 localAnchorB = new v2();
    protected float targetLength;
    private float length;
    private float m_impulse;

    
    private int indexA;
    private int indexB;
    private final v2 m_u = new v2();
    private final v2 m_rA = new v2();
    private final v2 m_rB = new v2();
    private final v2 m_localCenterA = new v2();
    private final v2 m_localCenterB = new v2();
    private float m_invMassA;
    private float m_invMassB;
    private float m_invIA;
    private float m_invIB;
    private float m_mass;
    private LimitState state;
    private float positionFactor = 1.0f;

    public RopeJoint(IWorldPool worldPool, RopeJointDef def) {
        super(worldPool, def);
        localAnchorA.set(def.localAnchorA);
        localAnchorB.set(def.localAnchorB);

        targetLength = def.maxLength;

        m_mass = 0.0f;
        m_impulse = 0.0f;
        state = LimitState.INACTIVE;
        length = 0.0f;
    }

    protected float targetLength() {
        return targetLength;
    }

    @Override
    public void initVelocityConstraints(SolverData data) {
        indexA = A.island;
        indexB = B.island;
        m_localCenterA.set(A.sweep.localCenter);
        m_localCenterB.set(B.sweep.localCenter);
        m_invMassA = A.m_invMass;
        m_invMassB = B.m_invMass;
        m_invIA = A.m_invI;
        m_invIB = B.m_invI;

        v2 cA = data.positions[indexA];
        float aA = data.positions[indexA].a;
        v2 vA = data.velocities[indexA];
        float wA = data.velocities[indexA].w;

        v2 cB = data.positions[indexB];
        float aB = data.positions[indexB].a;
        v2 vB = data.velocities[indexB];
        float wB = data.velocities[indexB].w;

        Rot qA = new Rot();
        Rot qB = new Rot();
        v2 temp = new v2();

        qA.set(aA);
        qB.set(aB);

        
        Rot.mulToOutUnsafe(qA, temp.set(localAnchorA).subbed(m_localCenterA), m_rA);
        Rot.mulToOutUnsafe(qB, temp.set(localAnchorB).subbed(m_localCenterB), m_rB);

        m_u.set(cB).added(m_rB).subbed(cA).subbed(m_rA);

        length = m_u.length();

        float C = length - targetLength();
//        float ca = Math.abs(C);

        if (C > Settings.linearSlop) {

            m_u.scaled(1.0f / length);

                state = LimitState.AT_UPPER;











        } else {
            state = LimitState.INACTIVE;
            m_u.setZero();
            m_mass = 0.0f;
            m_impulse = 0.0f;
            length = 0;
            return;
        }

        
        float crA = v2.cross(m_rA, m_u);
        float crB = v2.cross(m_rB, m_u);
        float invMass = m_invMassA + m_invIA * crA * crA + m_invMassB + m_invIB * crB * crB;

        m_mass = invMass == 0.0f ? 0.0f : 1.0f / invMass;


            
            m_impulse *= data.step.dtRatio * positionFactor;

            float Px = m_impulse * m_u.x;
            float Py = m_impulse * m_u.y;
            vA.x -= m_invMassA * Px;
            vA.y -= m_invMassA * Py;
            wA -= m_invIA * (m_rA.x * Py - m_rA.y * Px);

            vB.x += m_invMassB * Px;
            vB.y += m_invMassB * Py;
            wB += m_invIB * (m_rB.x * Py - m_rB.y * Px);






        
        data.velocities[indexA].w = wA;
        
        data.velocities[indexB].w = wB;
    }



    @Override
    public void solveVelocityConstraints(SolverData data) {

        float targetLength = targetLength();

        Velocity VA = data.velocities[indexA];
        v2 vA = VA;
        float wA = VA.w;
        Velocity VB = data.velocities[indexB];
        v2 vB = VB;
        float wB = VB.w;

        
        v2 vpA = pool.popVec2();
        v2 vpB = pool.popVec2();
        v2 temp = pool.popVec2();

        v2.crossToOutUnsafe(wA, m_rA, vpA);
        vpA.added(vA);
        v2.crossToOutUnsafe(wB, m_rB, vpB);
        vpB.added(vB);

        float dLen = length - targetLength;
        float Cdot = v2.dot(m_u, temp.set(vpB).subbed(vpA))
                
        ;

        
        
            Cdot += data.step.inv_dt * Math.abs(dLen) * positionFactor;
        

        float impulse = -m_mass * Cdot;
        float oldImpulse = m_impulse;
        m_impulse = Math.min(0.0f, m_impulse + impulse);
        impulse = m_impulse - oldImpulse;

        float Px = impulse * m_u.x;
        float Py = impulse * m_u.y;

        vA.x -= m_invMassA * Px;
        vA.y -= m_invMassA * Py;

        VA.w = wA - m_invIA * (m_rA.x * Py - m_rA.y * Px);
        vB.x += m_invMassB * Px;
        vB.y += m_invMassB * Py;
        VB.w = wB + m_invIB * (m_rB.x * Py - m_rB.y * Px);

        pool.pushVec2(3);

        
        
    }

    @Override
    public boolean solvePositionConstraints(SolverData data) {

        float targetLength = targetLength();

        v2 cA = data.positions[indexA];
        float aA = data.positions[indexA].a;
        v2 cB = data.positions[indexB];
        float aB = data.positions[indexB].a;

        Rot qA = pool.popRot();
        Rot qB = pool.popRot();
        v2 u = pool.popVec2();
        v2 rA = pool.popVec2();
        v2 rB = pool.popVec2();
        v2 temp = pool.popVec2();

        qA.set(aA);
        qB.set(aB);

        
        Rot.mulToOutUnsafe(qA, temp.set(localAnchorA).subbed(m_localCenterA), rA);
        Rot.mulToOutUnsafe(qB, temp.set(localAnchorB).subbed(m_localCenterB), rB);
        u.set(cB).added(rB).subbed(cA).subbed(rA);

        float length = u.normalize();
        float C = length - targetLength;



        float impulse = -m_mass * C;
        float Px = impulse * u.x;
        float Py = impulse * u.y;

        cA.x -= m_invMassA * Px;
        cA.y -= m_invMassA * Py;
        aA -= m_invIA * (rA.x * Py - rA.y * Px);
        cB.x += m_invMassB * Px;
        cB.y += m_invMassB * Py;
        aB += m_invIB * (rB.x * Py - rB.y * Px);

        pool.pushRot(2);
        pool.pushVec2(4);

        
        data.positions[indexA].a = aA;
        
        data.positions[indexB].a = aB;

        return Math.abs(length - targetLength) < Settings.linearSlop;
    }

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
        argOut.set(m_u).scaled(inv_dt).scaled(m_impulse * positionFactor);
    }

    @Override
    public float reactionTorque(float inv_dt) {
        return 0.0f;
    }

    public v2 getLocalAnchorA() {
        return localAnchorA;
    }

    public v2 getLocalAnchorB() {
        return localAnchorB;
    }

    public void setTargetLength(float targetLength) {
        this.targetLength = targetLength;
    }

    public void setPositionFactor(float positionFactor) {
        this.positionFactor = positionFactor;
    }





}