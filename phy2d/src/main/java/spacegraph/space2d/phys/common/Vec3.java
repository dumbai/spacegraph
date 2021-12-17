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
package spacegraph.space2d.phys.common;

import jcog.math.v3;

/**
 * @author Daniel Murphy
 */
@Deprecated public class Vec3 extends v3 {

    public Vec3() {
        super();
    }

    public Vec3(float argX, float argY, float argZ) {
        super(argX, argY, argZ);
    }

    public v3 addLocal(v3 argVec) {
        x += argVec.x;
        y += argVec.y;
        z += argVec.z;
        return this;
    }
//    public v3 subLocal(v3 argVec) {
//        x -= argVec.x;
//        y -= argVec.y;
//        z -= argVec.z;
//        return this;
//    }
//    public v3 add(v3 argVec) {
//        return new Vec3(x + argVec.x, y + argVec.y, z + argVec.z);
//    }
//
//    public v3 sub(v3 argVec) {
//        return new Vec3(x - argVec.x, y - argVec.y, z - argVec.z);
//    }
//
//    public v3 mul(float argScalar) {
//        return new Vec3(x * argScalar, y * argScalar, z * argScalar);
//    }

//    public v3 negate() {
//        return new Vec3(-x, -y, -z);
//    }

    //    public static void crossToOut(Vec3 a, Vec3 b, Vec3 out) {
//        final float tempy = a.z * b.x - a.x * b.z;
//        final float tempz = a.x * b.y - a.y * b.x;
//        out.x = a.y * b.z - a.z * b.y;
//        out.y = tempy;
//        out.z = tempz;
//    }

}
