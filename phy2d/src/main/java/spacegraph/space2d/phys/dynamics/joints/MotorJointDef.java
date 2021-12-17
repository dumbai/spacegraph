package spacegraph.space2d.phys.dynamics.joints;

import jcog.math.v2;
import spacegraph.space2d.phys.dynamics.Body2D;

/**
 * Motor joint definition.
 *
 * @author dmurph
 */
class MotorJointDef extends JointDef {
    /**
     * Position of bodyB minus the position of bodyA, in bodyA's frame, in meters.
     */
    public final v2 linearOffset = new v2();

    /**
     * The bodyB angle minus bodyA angle in radians.
     */
    public float angularOffset;

    /**
     * The maximum motor force in N.
     */
    public final float maxForce;

    /**
     * The maximum motor torque in N-m.
     */
    public final float maxTorque;

    /**
     * Position correction factor in the range [0,1].
     */
    public final float correctionFactor;

    MotorJointDef() {
        super(JointType.MOTOR);
        angularOffset = 0;
        maxForce = 1;
        maxTorque = 1;
        correctionFactor = 0.3f;
    }

    public void initialize(Body2D bA, Body2D bB) {
        bodyA = bA;
        bodyB = bB;
        v2 xB = bodyB.getPosition();
        bodyA.getLocalPointToOut(xB, linearOffset);

        float angleA = bodyA.getAngle();
        float angleB = bodyB.getAngle();
        angularOffset = angleB - angleA;
    }
}