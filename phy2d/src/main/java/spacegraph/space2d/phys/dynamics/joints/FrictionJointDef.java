/**
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
 * <p>
 * Created at 7:23:39 AM Jan 20, 2011
 */
/**
 * Created at 7:23:39 AM Jan 20, 2011
 */
package spacegraph.space2d.phys.dynamics.joints;

import jcog.math.v2;
import spacegraph.space2d.phys.dynamics.Body2D;

/**
 * Friction joint definition.
 *
 * @author Daniel Murphy
 */
class FrictionJointDef extends JointDef {


    /**
     * The local anchor point relative to bodyA's origin.
     */
    public final v2 localAnchorA;

    /**
     * The local anchor point relative to bodyB's origin.
     */
    public final v2 localAnchorB;

    /**
     * The maximum friction force in N.
     */
    public final float maxForce;

    /**
     * The maximum friction torque in N-m.
     */
    public final float maxTorque;

    FrictionJointDef() {
        super(JointType.FRICTION);
        localAnchorA = new v2();
        localAnchorB = new v2();
        maxForce = 0.0f;
        maxTorque = 0.0f;
    }

    /**
     * Initialize the bodies, anchors, axis, and reference angle using the world anchor and world
     * axis.
     */
    public void initialize(Body2D bA, Body2D bB, v2 anchor) {
        bodyA = bA;
        bodyB = bB;
        bA.getLocalPointToOut(anchor, localAnchorA);
        bB.getLocalPointToOut(anchor, localAnchorB);
    }
}