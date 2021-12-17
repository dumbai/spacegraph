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
import spacegraph.space2d.phys.dynamics.Body2D;

/**
 * Created at 3:38:52 AM Jan 15, 2011
 */

/**
 * @author Daniel Murphy
 */
public class WeldJointDef extends JointDef {
    /**
     * The local anchor point relative to body1's origin.
     */
    public final v2 localAnchorA;

    /**
     * The local anchor point relative to body2's origin.
     */
    public final v2 localAnchorB;

    /**
     * The body2 angle minus body1 angle in the reference state (radians).
     */
    public float referenceAngle;

    /**
     * The mass-spring-damper frequency in Hertz. Rotation only. Disable softness with a value of 0.
     */
    public float frequencyHz;

    /**
     * The damping ratio. 0 = no damping, 1 = critical damping.
     */
    public float dampingRatio;

    public WeldJointDef() {
        super(JointType.WELD);
        localAnchorA = new v2();
        localAnchorB = new v2();
        referenceAngle = 0.0f;
    }

    /** center to center */
    public WeldJointDef(Body2D a, Body2D b) {
        this();
        this.bodyA = a;
        this.bodyB = b;
        this.referenceAngle = 0;
    }


    /**
     * Initialize the bodies, anchors, and reference angle using a world anchor point.
     *
     * @param bA
     * @param bB
     * @param anchor
     */
    public void initialize(Body2D bA, Body2D bB, v2 anchor) {
        bodyA = bA;
        bodyB = bB;
        bodyA.getLocalPointToOut(anchor, localAnchorA);
        bodyB.getLocalPointToOut(anchor, localAnchorB);
        referenceAngle = bodyB.getAngle() - bodyA.getAngle();
    }
}
