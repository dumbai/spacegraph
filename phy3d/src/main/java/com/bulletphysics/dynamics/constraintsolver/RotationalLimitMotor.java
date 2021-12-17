/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

/*
2007-09-09
btGeneric6DofConstraint Refactored by Francisco Le�n
email: projectileman@yahoo.com
http://gimpact.sf.net
*/

package com.bulletphysics.dynamics.constraintsolver;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.dynamics.RigidBody;

import javax.vecmath.Vector3f;

/**
 * Rotation limit structure for generic joints.
 *
 * @author jezek2
 */
public class RotationalLimitMotor {

    //protected final BulletStack stack = BulletStack.get();

    private final float targetVelocity; //!< target motor velocity
    private final float maxMotorForce; //!< max force on motor
    private final float limitSoftness; //! Relaxation factor
    private final float ERP; //!< Error tolerance factor when joint is at limit
    private final float bounce; //!< restitution factor
    private final boolean enableMotor;
    public float loLimit; //!< joint limit
    public float hiLimit; //!< joint limit
    private float maxLimitForce; //!< max force on limit
    private float damping; //!< Damping.
    private float currentLimitError;//!  How much is violated this limit
    private int currentLimit;//!< 0=free, 1=at lo limit, 2=at hi limit
    public float accumulatedImpulse;

    public RotationalLimitMotor() {
        accumulatedImpulse = 0.0f;
        targetVelocity = 0;
        maxMotorForce = 0.1f;
        maxLimitForce = 300.0f;
        loLimit = -BulletGlobals.SIMD_INFINITY;
        hiLimit = BulletGlobals.SIMD_INFINITY;
        ERP = 0.5f;
        bounce = 0.0f;
        damping = 1.0f;
        limitSoftness = 0.5f;
        currentLimit = 0;
        currentLimitError = 0;
        enableMotor = false;
    }

    public RotationalLimitMotor(RotationalLimitMotor limot) {
        targetVelocity = limot.targetVelocity;
        maxMotorForce = limot.maxMotorForce;
        limitSoftness = limot.limitSoftness;
        loLimit = limot.loLimit;
        hiLimit = limot.hiLimit;
        ERP = limot.ERP;
        bounce = limot.bounce;
        currentLimit = limot.currentLimit;
        currentLimitError = limot.currentLimitError;
        enableMotor = limot.enableMotor;
    }

    /**
     * Is limited?
     */
    public boolean isLimited() {
        return !(loLimit >= hiLimit);
    }

    /**
     * Need apply correction?
     */
    public boolean needApplyTorques() {
        return currentLimit != 0 || enableMotor;
    }

    /**
     * Calculates error. Calculates currentLimit and currentLimitError.
     */
    public int testLimitValue(float test_value) {
        if (loLimit > hiLimit) {
            currentLimit = 0; // Free from violation
            return 0;
        }

        if (test_value < loLimit) {
            currentLimit = 1; // low limit violation
            currentLimitError = test_value - loLimit;
            return 1;
        } else if (test_value > hiLimit) {
            currentLimit = 2; // High limit violation
            currentLimitError = test_value - hiLimit;
            return 2;
        }

        currentLimit = 0; // Free from violation
        return 0;
    }

    /**
     * Apply the correction impulses for two bodies.
     */
    public float solveAngularLimits(float timeStep, Vector3f axis, float jacDiagABInv, RigidBody body0, RigidBody body1) {
        if (!needApplyTorques()) {
            return 0.0f;
        }

        float target_velocity = this.targetVelocity;
        float maxMotorForce = this.maxMotorForce;

        // current error correction
        if (currentLimit != 0) {
            target_velocity = -ERP * currentLimitError / (timeStep);
            maxMotorForce = maxLimitForce;
        }

        maxMotorForce *= timeStep;

        // current velocity difference
        Vector3f vel_diff = body0.getAngularVelocity(new Vector3f());
        if (body1 != null) {
            vel_diff.sub(body1.getAngularVelocity(new Vector3f()));
        }

        float rel_vel = axis.dot(vel_diff);

        // correction velocity
        float motor_relvel = limitSoftness * (target_velocity - damping * rel_vel);

        if (motor_relvel < BulletGlobals.FLT_EPSILON && motor_relvel > -BulletGlobals.FLT_EPSILON) {
            return 0.0f; // no need for applying force
        }

        // correction impulse
        float unclippedMotorImpulse = (1 + bounce) * motor_relvel * jacDiagABInv;

        // clip correction impulse
        float clippedMotorImpulse;

        // todo: should clip against accumulated impulse
        if (unclippedMotorImpulse > 0.0f) {
            clippedMotorImpulse = Math.min(unclippedMotorImpulse, maxMotorForce);
        } else {
            clippedMotorImpulse = Math.max(unclippedMotorImpulse, -maxMotorForce);
        }

        // sort with accumulated impulses
        float lo = -1.0e30f;
        float hi = 1.0e30f;

        float oldaccumImpulse = accumulatedImpulse;
        float sum = oldaccumImpulse + clippedMotorImpulse;
        accumulatedImpulse = sum > hi ? 0.0f : sum < lo ? 0.0f : sum;

        clippedMotorImpulse = accumulatedImpulse - oldaccumImpulse;

        Vector3f motorImp = new Vector3f();
        motorImp.scale(clippedMotorImpulse, axis);

        body0.applyTorqueImpulse(motorImp);
        if (body1 != null) {
            motorImp.negate();
            body1.applyTorqueImpulse(motorImp);
        }

        return clippedMotorImpulse;
    }

}