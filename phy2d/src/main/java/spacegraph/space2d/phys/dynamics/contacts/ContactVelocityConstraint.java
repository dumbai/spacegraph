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
package spacegraph.space2d.phys.dynamics.contacts;

import jcog.math.v2;
import spacegraph.space2d.phys.common.Mat22;
import spacegraph.space2d.phys.common.Settings;

public class ContactVelocityConstraint {
    public final VelocityConstraintPoint[] points = new VelocityConstraintPoint[Settings.maxManifoldPoints];
    public final v2 normal = new v2();
    public final Mat22 normalMass = new Mat22();
    public final Mat22 K = new Mat22();
    public int indexA;
    public int indexB;
    public float invMassA;
    public float invMassB;
    public float invIA;
    public float invIB;
    public float friction;
    public float restitution;
    public float tangentSpeed;
    public int pointCount;
    public int contactIndex;

    public ContactVelocityConstraint() {
        for (int i = 0; i < points.length; i++) {
            points[i] = new VelocityConstraintPoint();
        }
    }

    public static class VelocityConstraintPoint {
        public final v2 rA = new v2();
        public final v2 rB = new v2();
        public float normalImpulse;
        public float tangentImpulse;
        public float normalMass;
        public float tangentMass;
        public float velocityBias;
    }
}
