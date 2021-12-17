/*
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 127 $
 * $Date: 2008-02-28 17:18:51 -0300 (Thu, 28 Feb 2008) $
 * $State$
 */

package spacegraph.util.math;

import jcog.math.v3;

import java.awt.*;


/**
 * A three-element color value represented by single precision floating
 * point x,y,z values.  The x,y,z values represent the red, green, and
 * blue color values, respectively. Color components should be in the
 * range of [0.0, 1.0].
 * <p>
 * Java 3D assumes that a linear (gamma-corrected) visual is used for
 * all colors.
 */
public class Color3f extends v3 {


    /**
     * Constructs and initializes a Color3f from the three xyz values.
     *
     * @param x the red color value
     * @param y the green color value
     * @param z the blue color value
     */
    public Color3f(float x, float y, float z) {
        super(x, y, z);
    }


    /**
     * Constructs and initializes a Color3f from the array of length 3.
     *
     * @param v the array of length 3 containing xyz in order
     */
    public Color3f(float[] v) {
        super(v);
    }


    /**
     * Constructs and initializes a Color3f from the specified Color3f.
     *
     * @param v1 the Color3f containing the initialization x y z data
     */
    public Color3f(Color3f v1) {
        super(v1);
    }


    /**
     * Constructs and initializes a Color3f from the specified v3.
     *
     * @param t1 the v3 containing the initialization x y z data
     */
    public Color3f(v3 t1) {
        super(t1);
    }


    /**
     * Constructs and initializes a Color3f from the specified AWT
     * Color object.  The alpha value of the AWT color is ignored.
     * No conversion is done on the color to compensate for
     * gamma correction.
     *
     * @param color the AWT color with which to initialize this
     *              Color3f object
     * @since vecmath 1.2
     */
    public Color3f(Color color) {
        super(color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f);
    }


    /**
     * Constructs and initializes a Color3f to (0.0, 0.0, 0.0).
     */
    public Color3f() {
        super();
    }


    /**
     * Sets the r,g,b values of this Color3f object to those of the
     * specified AWT Color object.
     * No conversion is done on the color to compensate for
     * gamma correction.
     *
     * @param color the AWT color to copy into this Color3f object
     * @since vecmath 1.2
     */
    public final void set(Color color) {
        x = color.getRed() / 255.0f;
        y = color.getGreen() / 255.0f;
        z = color.getBlue() / 255.0f;
    }


    /**
     * Returns a new AWT color object initialized with the r,g,b
     * values of this Color3f object.
     *
     * @return a new AWT Color object
     * @since vecmath 1.2
     */
    public final Color get() {
        int r = (int)(x * 255.9f);
        int g = (int)(y * 255.9f);
        int b = (int)(z * 255.9f);

        return new Color(r, g, b);
    }


    public final void set(float gray) {
        set(gray,gray,gray);
    }
}
