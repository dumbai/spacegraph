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
/*
 * JBox2D - A Java Port of Erin Catto's Box2D
 *
 * JBox2D homepage: http:
 * Box2D homepage: http:
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package spacegraph.space2d.phys.common;

import jcog.math.v2;

/**
 * A few math methods that don't fit very well anywhere else.
 */
public enum MathUtils  {
    ;
    public static final float PI = (float) Math.PI;
    public static final float TWOPI = (float) (Math.PI * 2);
//    public static final float INV_PI = 1f / PI;
    private static final float SHIFT23 = 1 << 23;
//    public static final float INV_SHIFT23 = 1.0f / SHIFT23;
    private static final double DEFICIT = 0.0001;
    private static final float HALF_PI = PI / 2;
//    public static final float QUARTER_PI = PI / 4;
//    public static final float THREE_HALVES_PI = TWOPI - HALF_PI;

    /**
     * Degrees to radians conversion factor
     */
//    public static final float DEG2RAD = PI / 180;

    /**
     * Radians to degrees conversion factor
     */
//    public static final float RAD2DEG = 180 / PI;
//
//    private static final float[] sinLUT = new float[Settings.SINCOS_LUT_LENGTH];
//
//    static {
//        for (int i = 0; i < Settings.SINCOS_LUT_LENGTH; i++) {
//            sinLUT[i] = (float) Math.sin(i * Settings.SINCOS_LUT_PRECISION);
//        }
//    }

//    public static float sinLUT(float x) {
//        x %= TWOPI;
//
//        if (x < 0) {
//            x += TWOPI;
//        }
//
//        if (Settings.SINCOS_LUT_LERP) {
//
//            x /= Settings.SINCOS_LUT_PRECISION;
//
//            int index = (int) x;
//
//            if (index != 0) {
//                x %= index;
//            }
//
//
//			return (1 - x) * sinLUT[index] + x * sinLUT[index == Settings.SINCOS_LUT_LENGTH - 1 ? 0 : index + 1];
//
//        } else {
//            return sinLUT[Math.round(x / Settings.SINCOS_LUT_PRECISION) % Settings.SINCOS_LUT_LENGTH];
//        }
//    }

    //    private static int fastFloor(float x) {
//        int y = (int) x;
//        if (x < y) {
//            return y - 1;
//        }
//        return y;
//    }

    //    private static int fastCeil(float x) {
//        int y = (int) x;
//        if (x > y) {
//            return y + 1;
//        }
//        return y;
//    }

//    /**
//     * Rounds up the value to the nearest higher power^2 value.
//     *
//     * @param x
//     * @return power^2 value
//     */
//    public static int ceilPowerOf2(int x) {
//        int pow2 = 1;
//        while (pow2 < x) {
//            pow2 <<= 1;
//        }
//        return pow2;
//    }

    public static float map(float val, float fromMin, float fromMax,
                            float toMin, float toMax) {
        float mult = (val - fromMin) / (fromMax - fromMin);
        return toMin + mult * (toMax - toMin);
    }

    //    public static v2 clamp(v2 a, v2 low, v2 high) {
//        v2 min = new v2();
//        min.x = Math.min(a.x, high.x);
//        min.y = Math.min(a.y, high.y);
//        min.x = Math.max(low.x, min.x);
//        min.y = Math.max(low.y, min.y);
//        return min;
//    }
//
//    public static void clampToOut(v2 a, v2 low, v2 high, v2 dest) {
//        dest.x = Math.min(a.x, high.x);
//        dest.y = Math.min(a.y, high.y);
//        dest.x = Math.max(low.x, dest.x);
//        dest.y = Math.max(low.y, dest.y);
//    }

//    /**
//     * Next Largest Power of 2: Given a binary integer value x, the next largest power of 2 can be
//     * computed by a SWAR algorithm that recursively "folds" the upper bits into the lower bits. This
//     * process yields a bit vector with the same most significant 1 as x, but all 1's below it. Adding
//     * 1 to that value yields the next largest power of 2.
//     */
//    public static int nextPowerOfTwo(int x) {
//        x |= x >> 1;
//        x |= x >> 2;
//        x |= x >> 4;
//        x |= x >> 8;
//        x |= x >> 16;
//        return x + 1;
//    }

//    public static boolean isPowerOfTwo(int x) {
//        return x > 0 && (x & x - 1) == 0;
//    }
//
//    public static float fastAtan2(float y, float x) {
//        if (x == 0.0f) {
//            if (y > 0.0f) return HALF_PI;
//            if (y == 0.0f) return 0.0f;
//            return -HALF_PI;
//        }
//        float atan;
//        float z = y / x;
//        if (Math.abs(z) < 1.0f) {
//            atan = z / (1.0f + 0.28f * z * z);
//            if (x < 0.0f) {
//                if (y < 0.0f) return atan - PI;
//                return atan + PI;
//            }
//        } else {
//            atan = HALF_PI - z / (z * z + 0.28f);
//            if (y < 0.0f) return atan - PI;
//        }
//        return atan;
//    }
//
//    public static float reduceAngle(float theta) {
//        theta %= TWOPI;
//        if (Math.abs(theta) > PI) {
//            theta -= TWOPI;
//        }
//        if (Math.abs(theta) > HALF_PI) {
//            theta = PI - theta;
//        }
//        return theta;
//    }
//
//    public static float randomFloat(float argLow, float argHigh) {
//        return (float) Math.random() * (argHigh - argLow) + argLow;
//    }
//
//    public static float randomFloat(Random r, float argLow, float argHigh) {
//        return r.nextFloat() * (argHigh - argLow) + argLow;
//    }

    //
//    public static float fastPow(float a, float b) {
//        float x = Float.floatToRawIntBits(a);
//        x *= INV_SHIFT23;
//        x -= 127;
//        float y = x - (x >= 0 ? (int) x : (int) x - 1);
//        b *= x + (y - y * y) * 0.346607f;
//        y = b - (b >= 0 ? (int) b : (int) b - 1);
//        y = (y - y * y) * 0.33971f;
//        return Float.intBitsToFloat((int) ((b + 127 - y) * SHIFT23));
//    }

    /**
     * @param a
     * @param b
     * @return Kvadraticky uhol v rozmedzi (0-4) medzi vektorom (b - a) a vektorom (0, 1).
     */
    public static double angle(v2 a, v2 b) {
        double vx = b.x - a.x;
        double vy = b.y - a.y;
        double x = vx * vx;
        double cos = x / (x + vy * vy);
        return vx > 0 ? vy > 0 ? 3 + cos : 1 - cos : vy > 0 ? 3 - cos : 1 + cos;
    }

    /**
     * @param a 1. bod usecky
     * @param b 2. bod usecky
     * @param v Bod, u ktoreho sa rozhoduje, na ktorej strane sa nachadza.
     * @return <tt>-1</tt>, ak sa bod <tt>v</tt> nachadza na lavo od usecky |ab|<br>
     * <tt>0</tt>, ak body <tt>a, b, v</tt> lezia na jednej priamke.<br>
     * <tt>1</tt>, ak sa bod <tt>v</tt> nachadza na pravo od usecky |ab|<br>
     */
    public static int site(v2 a, v2 b, v2 v) {
        double g = (b.x - a.x) * (v.y - b.y);
        double h = (v.x - b.x) * (b.y - a.y);
        return Double.compare(g, h);
    }

    /**
     * @param a 1. bod usecky
     * @param b 2. bod usecky
     * @param v Bod, u ktoreho sa rozhoduje, na ktorej strane sa nachadza.
     * @return Rovnako ako funkcia site, s tym rozdielom, ze zohladnuje deficit.
     */
    public static int siteDef(v2 a, v2 b, v2 v) {
        double ux = b.x - a.x;
        double uy = b.y - a.y;
        double wx = b.x - v.x;
        double wy = b.y - v.y;
        double sin = (ux * wy - wx * uy) / Math.sqrt((ux * ux + uy * uy) * (wx * wx + wy * wy));
        if (!Double.isFinite(sin) || Math.abs(sin) < DEFICIT)
            return 0;

        return sin < 0 ? 1 : -1;
    }
}