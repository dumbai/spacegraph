package spacegraph.space2d.phys.dynamics.joints;

import jcog.math.v2;
import spacegraph.space2d.phys.dynamics.Body2D;

/**
 * Rope joint definition. This requires two body anchor points and a maximum lengths. Note: by
 * default the connected objects will not collide. see collideConnected in b2JointDef.
 *
 * @author Daniel Murphy
 */
public class RopeJointDef extends JointDef {

    /**
     * The local anchor point relative to bodyA's origin.
     */
    public final v2 localAnchorA = new v2();

    /**
     * The local anchor point relative to bodyB's origin.
     */
    public final v2 localAnchorB = new v2();

    /**
     * The maximum length of the rope. Warning: this must be larger than b2_linearSlop or the joint
     * will have no effect.
     */
    public float maxLength;


    public RopeJointDef(Body2D a, Body2D b) {
        super(JointType.ROPE);
        this.bodyA = a;
        this.bodyB = b;
        localAnchorA.set(0.0f, 0.0f);
        localAnchorB.set(0.0f, 0.0f);
    }
}
