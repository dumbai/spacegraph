/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2007 Erwin Coumans  http:
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

package spacegraph.video;


import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import jcog.math.v2;
import jcog.math.v3;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.Surface;
import spacegraph.util.ParticleColor;
import spacegraph.util.math.Color3f;
import toxi.geom.Vec2D;
import toxi.math.MathUtils;

import java.util.function.Consumer;

import static com.jogamp.opengl.GL.*;
import static jcog.Util.*;

public enum Draw {
    ;

    public static final GLU glu = new GLU();

    @Deprecated
    public static void lind(double x1, double y1, double x2, double y2, GL2 gl) {
        linf((float) x1, (float) y1, (float) x2, (float) y2, gl);
    }

    public static void lini(int x1, int y1, int x2, int y2, GL2 gl) {
        gl.glBegin(GL_LINES);
        gl.glVertex2i(x1, y1);
        gl.glVertex2i(x2, y2);
        gl.glEnd();
    }

    public static void linf(float x1, float y1, float x2, float y2, GL2 gl) {
        gl.glBegin(GL_LINES);
        gl.glVertex2f(x1, y1);
        gl.glVertex2f(x2, y2);
        gl.glEnd();
    }

    public static void trid(int x1, int y1, int x2, int y2, int x3, int y3, GL2 gl) {
        gl.glBegin(GL_TRIANGLES);
        gl.glVertex2i(x1, y1);
        gl.glVertex2i(x2, y2);
        gl.glVertex2i(x3, y3);
        gl.glEnd();
    }

    public static void trif(GL2 gl, float x1, float y1, float x2, float y2, float x3, float y3) {
        gl.glBegin(GL_TRIANGLES);
        gl.glVertex2f(x1, y1);
        gl.glVertex2f(x2, y2);
        gl.glVertex2f(x3, y3);
        gl.glEnd();
    }

    public static void quaf(GL2 gl, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        gl.glBegin(GL2ES3.GL_QUADS);
        gl.glVertex2f(x1, y1);
        gl.glVertex2f(x2, y2);
        gl.glVertex2f(x3, y3);
        gl.glVertex2f(x4, y4);
        gl.glEnd();
    }

    public static void quai(GL2 gl, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        gl.glBegin(GL2ES3.GL_QUADS);
        gl.glVertex2i(x1, y1);
        gl.glVertex2i(x2, y2);
        gl.glVertex2i(x3, y3);
        gl.glVertex2i(x4, y4);
        gl.glEnd();
    }

    public static void line(GL2 gl, v3 a, v3 b) {
        gl.glBegin(GL_LINES);
        gl.glVertex3f(a.x, a.y, a.z);
        gl.glVertex3f(b.x, b.y, b.z);
        gl.glEnd();
    }

    public static void rectStroke(float left, float bottom, float w, float h, GL2 gl) {
        float right = left + w;
        float top = bottom + h;
        gl.glBegin(GL_LINE_STRIP);
        gl.glVertex2f(left, bottom);
        gl.glVertex2f(right, bottom);
        gl.glVertex2f(right, top);
        gl.glVertex2f(left, top);
        gl.glVertex2f(left, bottom);
        gl.glEnd();
    }


    public static void rectFrame(float cx, float cy, float wi, float hi, float thick, GL2 gl) {
        rectFrame(cx, cy, wi, hi, wi + thick, hi + thick, gl);
    }

    public static void rectCross(float cx, float cy, float wi, float hi, float thick, GL2 gl) {
        rectCross(cx, cy, wi, hi, wi + thick, hi + thick, gl);
    }

    /**
     * wi,hi - inner width/height
     * wo,ho - outer width/height
     */
    public static void rectFrame(float cx, float cy, float wi, float hi, float wo, float ho, GL2 gl) {
        float vthick = (ho - hi) / 2;
        //N
        float l = cx - wo / 2;
        rect(l, cy - ho / 2, wo, vthick, gl);
        //S
        rect(l, cy + ho / 2 - vthick, wo, vthick, gl);

        float hthick = (wo - wi) / 2;
        //W
        float b = cy - hi / 2;
        rect(l, b, hthick, hi, gl);
        //E
        rect(cx + wo / 2 - hthick, b, hthick, hi, gl);
    }

    /**
     * a rectangle but instead of drawing the outside edges, draws a cross-hair cross through the center
     * TODO rotation angle param
     */
    public static void rectCross(float cx, float cy, float wi, float hi, float wo, float ho, GL2 gl) {
        float vthick = (ho - hi) / 2;
        rect(cx - wo / 2, cy - vthick / 2, wo, vthick, gl); //horiz
        float hthick = (wo - wi) / 2;
        rect(cx - hthick / 2, cy - hi / 2, hthick, hi, gl); //vert
    }


    public static void circle(GL2 gl, v2 center, boolean solid, float radius, int NUM_CIRCLE_POINTS) {


        float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
        float c = (float) Math.cos(theta);
        float s = (float) Math.sin(theta);
        float cx = center.x;
        float cy = center.y;
        gl.glBegin(solid ? GL_TRIANGLE_FAN : GL_LINE_LOOP);

        float y = 0;
        float x = radius;
        for (int i = 0; i < NUM_CIRCLE_POINTS; i++) {
            gl.glVertex3f(x + cx, y + cy, 0);

            float temp = x;
            x = c * x - s * y;
            y = s * temp + c * y;
        }
        gl.glEnd();

    }

    public static void particles(GL2 gl, v2[] centers, float radius, int NUM_CIRCLE_POINTS, ParticleColor[] colors, int count) {


        float theta = 2 * MathUtils.PI / NUM_CIRCLE_POINTS;
        float c = (float) Math.cos(theta);
        float s = (float) Math.sin(theta);

        float x = radius;
        float y = 0;

        for (int i = 0; i < count; i++) {
            v2 center = centers[i];

            gl.glBegin(GL_TRIANGLE_FAN);
            if (colors == null) {
                gl.glColor4f(1, 1, 1, 0.4f);
            } else {
                ParticleColor color = colors[i];
                gl.glColor4b(color.r, color.g, color.b, color.a);
            }
            float cx = center.x;
            float cy = center.y;
            for (int j = 0; j < NUM_CIRCLE_POINTS; j++) {
                gl.glVertex3f(x + cx, y + cy, 0);
                float temp = x;
                x = c * x - s * y;
                y = s * temp + c * y;
            }
            gl.glEnd();
        }

    }

    public static void rect(float left, float bottom, float w, float h, GL2 gl) {
        gl.glRectf(left, bottom, left + w, bottom + h);
    }

    public static void rectAlphaCorners(float left, float bottom, float x2, float y2, float[] color, float[] cornerAlphas, GL2 gl) {
        gl.glBegin(GL2ES3.GL_QUADS);
        float r = color[0], g = color[1], b = color[2];
        gl.glColor4f(r, g, b, cornerAlphas[0]);
        gl.glVertex3f(left, bottom, 0);
        gl.glColor4f(r, g, b, cornerAlphas[1]);
        gl.glVertex3f(x2, bottom, 0);
        gl.glColor4f(r, g, b, cornerAlphas[2]);
        gl.glVertex3f(x2, y2, 0);
        gl.glColor4f(r, g, b, cornerAlphas[3]);
        gl.glVertex3f(left, y2, 0);
        gl.glEnd();

    }

    public static void rect(GL2 gl, int x1, int y1, int w, int h) {

        gl.glRecti(x1, y1, x1 + w, y1 + h);

    }

    public static void rect(float x1, float y1, float w, float h, float z, GL2 gl) {
        if (z == 0) {
            rect(x1, y1, w, h, gl);
        } else {

            gl.glBegin(GL2ES3.GL_QUADS);
            gl.glNormal3i(0, 0, 1); //gl.glNormal3f(0, 0, 1);
            gl.glVertex3f(x1, y1, z);
            float x2 = x1 + w;
            gl.glVertex3f(x2, y1, z);
            float y2 = y1 + h;
            gl.glVertex3f(x2, y2, z);
            gl.glVertex3f(x1, y2, z);
            gl.glEnd();
        }
    }

    public static void rectTex(Texture t, float x, float y, float w, float h, float z, float repeatScale, float alpha, boolean mipmap, boolean inverted, GL2 gl) {

        gl.glColor4f(1, 1, 1, alpha);

        t.enable(gl);
        gl.glBindTexture(t.getTarget(), t.getTextureObject()); //t.bind(gl);

        texInit(repeatScale, mipmap, gl);

        if (repeatScale < 0)
            repeatScale = 1;

        gl.glBegin(GL2ES3.GL_QUADS);

        float s = repeatScale;
        float x2 = x + w;
        float y2 = y + h;
        if (inverted) {
            gl.glTexCoord2i(0, 0); //gl.glTexCoord2f(0, 0);
            gl.glVertex3f(x, y, z);
            gl.glTexCoord2f(s, 0);
            gl.glVertex3f(x2, y, z);
            gl.glTexCoord2f(s, s);
            gl.glVertex3f(x2, y2, z);
            gl.glTexCoord2f(0, s);
        } else {
            gl.glTexCoord2f(0, s);
            gl.glVertex3f(x, y, z);
            gl.glTexCoord2f(s, s);
            gl.glVertex3f(x2, y, z);
            gl.glTexCoord2f(s, 0);
            gl.glVertex3f(x2, y2, z);
            gl.glTexCoord2i(0, 0); //gl.glTexCoord2f(0, 0);
        }
        gl.glVertex3f(x, y2, z);

        gl.glEnd();

        gl.glDisable(t.getTarget()); //t.disable(gl);
    }

    public static void texInit(float repeatScale, boolean mipmap, GL2 gl) {


        if (mipmap) {
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
//            gl.glGenerateMipmap(GL_TEXTURE_2D);
        } else {
            //gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }

        boolean repeat = repeatScale > 0;
        if (repeat) {
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        } else {
//            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        }


    }


    //    public static void renderHalfTriEdge(GL2 gl, SimpleSpatial src, EDraw<?> e, float width, float twist, Quat4f tmpQ) {
//
//
//        Transform st = src.transform;
//        Transform tt = e.tgt().transform;
//
//        st.getRotation(tmpQ);
//
//        if (twist != 0)
//            tmpQ.setAngle(0, 1, 0, twist);
//
//        v3 ww = new v3(); ww.z = 1;
//        tmpQ.rotateVector(ww, ww);
//
//
//        float sx = st.x;
//        float tx = tt.x;
//        float dx = tx - sx;
//        float sy = st.y;
//        float ty = tt.y;
//        float dy = ty - sy;
//        float sz = st.z;
//        float tz = tt.z;
//        float dz = tz - sz;
//        v3 vv = new v3(dx, dy, dz).cross(ww).normalized(width);
//
//
//        gl.glBegin(GL_TRIANGLES);
//
//        gl.glColor4f(e.r, e.g, e.b, e.a);
//        gl.glNormal3f(ww.x, ww.y, ww.z);
//
//        gl.glVertex3f(sx + vv.x, sy + vv.y, sz + vv.z);
//
//        gl.glVertex3f(
//                sx + -vv.x, sy + -vv.y, sz + -vv.z
//
//        );
//
//        gl.glColor4f(e.r / 2f, e.g / 2f, e.b / 2f, e.a * 2 / 3);
//        gl.glVertex3f(tx, ty, tz);
//
//        gl.glEnd();
//
//
//    }
//
//    public static void renderLineEdge(GL2 gl, SimpleSpatial src, SimpleSpatial tgt, float width) {
//        gl.glLineWidth(width);
//        gl.glBegin(GL.GL_LINES);
//        v3 s = src.transform();
//        gl.glVertex3f(s.x, s.y, s.z);
//        v3 t = tgt.transform();
//        gl.glVertex3f(t.x, t.y, t.z);
//        gl.glEnd();
//    }
//
    public static void hsl(GL2 gl, float hue, float saturation, float brightness, float a) {
        float[] f = new float[4];

        //hsb(f, hue, saturation, brightness, a);
        hsl(hue, saturation, brightness, f);
        f[3] = a;

        gl.glColor4fv(f, 0);
    }

    public static int hsb(float hue, float saturation, float brightness) {
        float[] f = new float[4];
        hsb(f, hue, saturation, brightness, 1);
        return rgbInt(f[0], f[1], f[2]);
    }

    public static float[] hsb(@Nullable float[] target, float hue, float saturation, float brightness, float a) {
        if (target == null || target.length < 4)
            target = new float[4];

        float r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255 + 0.5f);
        } else {
            float h = (hue - (int) hue) * 6.0f;
            float f = h - (int) h;
            float p = brightness * (1 - saturation);
            float q = brightness * (1 - saturation * f);
            float t = brightness * (1 - saturation * (1 - f));
            switch ((int) h) {
                case 0 -> {
                    r = brightness;
                    g = t;
                    b = p;
                }
                case 1 -> {
                    r = q;
                    g = brightness;
                    b = p;
                }
                case 2 -> {
                    r = p;
                    g = brightness;
                    b = t;
                }
                case 3 -> {
                    r = p;
                    g = q;
                    b = brightness;
                }
                case 4 -> {
                    r = t;
                    g = p;
                    b = brightness;
                }
                case 5 -> {
                    r = brightness;
                    g = p;
                    b = q;
                }
            }
        }
        target[0] = r;
        target[1] = g;
        target[2] = b;
        target[3] = a;
        return target;
    }


    public static int colorHSB(float hue, float saturation, float brightness) {

        float r, g, b;
        if (saturation < Float.MIN_NORMAL) {
            r = g = b = (int) (brightness * 255 + 0.5f);
        } else if (brightness < Float.MIN_NORMAL) {
            return 0;
        } else {
            //if (hue < 0) hue+=1;
            float h = (hue - (int) hue) * 6;
            int hh = (int) h;
            float f = h - hh;
            float p = brightness * (1 - saturation);
            float q = brightness * (1 - saturation * f);
            float t = brightness * (1 - saturation * (1 - f));
            switch (hh) {
                case 0:
                    r = brightness;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = brightness;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = brightness;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = brightness;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = brightness;
                    break;
                case 5:
                    r = brightness;
                    g = p;
                    b = q;
                    break;
                default:
                    return 0; //should not happen
            }
        }
        return rgbInt(r, g, b);
    }

    /**
     * Converts an HSL color value to RGB. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes h, s, and l are contained in the set [0, 1] and
     * returns r, g, and b in the set [0, 255].
     *
     * @param h The hue
     * @param s The saturation
     * @param l The lightness
     * @return int array, the RGB representation
     */
    public static int colorHSL(float h, float s, float l) {
        float r, g, b;

        if (s == 0) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1 / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1 / 3f);
        }
        return rgbInt(r, g, b);
    }

    public static void hsl(float h, float s, float l, float[] target) {
        hsl(h, s, l, (r, g, b, a) -> {
            target[0] = r;
            target[1] = g;
            target[2] = b;
        });
    }

//    public static void hsl(float h, float s, float l, GL2 gl){
//        hsl(h, s, l, gl::glColor4f);
//    }

    public static void hsl(float h, float s, float l, ColorConsumer target) {
        float r, g, b;

        if (s <= Float.MIN_NORMAL /* TODO threshold amount, ex: 1/256f */) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1 / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1 / 3f);
        }
        target.accept(r, g, b, 1);
        //return rgbInt(r, g, b);
//        int[] rgb = {(int) (r * 255), (int) (g * 255), (int) (b * 255)};
//        return rgb;
    }

    /**
     * Helper method that converts hue to rgb
     */
    static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        else if (t > 1) t -= 1;

        if (t < 1 / 6f)
            return p + (q - p) * 6 * t;
        else if (t < 1 / 2f)
            return q;
        else if (t < 2 / 3f)
            return p + (q - p) * (2 / 3f - t) * 6;
        else
            return p;
    }

    /**
     * uses the built-in color scheme for displaying values in the range -1..+1
     */
    public static void colorBipolar(GL2 gl, float v) {
        float r, g, b;
        if (v < 0) {
            r = -v / 2;
            g = 0;
            b = -v;
        } else {
            r = v;
            g = +v / 2;
            b = 0;
        }
        gl.glColor3f(r, g, b);
    }

    public static int colorHue(float hue) {
        //return Draw.colorHSL(hue, 0.9f, 0.5f);
        return Draw.colorHSB(hue, 0.9f, 0.5f);
    }

    public static int colorBipolar(float x) {

        float r, g, b;

//        if (x < 0) {
//            x = x < -1 ? -1 : x;
//            r = -x;
//            g = 0;
//            b = -x / 2;
//        } else {
//            x = x > +1 ? +1 : x;
//            r = x / 2;
//            g = x;
//            b = 0;
//        }
        /* highlights for out-of-unit-range values */
        @Deprecated int extendedRange = 4;
        if (x < 0) {
            x = -x;
            if (x > 1) {
                r = 1;
                b = (x - 1) / extendedRange;
                if (b > 1) b = 1;
                b /= 2;
                g = 0;
            } else {
                r = x;
                g = b = 0;
            }
        } else {
            if (x > +1) {
                g = 1;
                b = (x - 1) / extendedRange;
                if (b > 1) b = 1;
                b /= 2;
                r = 0;
            } else {
                g = x;
                r = b = 0;
            }
        }
        return rgbInt(r, g, b);
    }

    public static int rgbInt(float r, float g, float b) {
        return (int) (255 * r) << 16 |
                (int) (255 * g) << 8 |
                (int) (255 * b);
    }

    public static void colorUnipolarHue(GL2 gl, float v, float hueMin, float hueMax) {
        colorUnipolarHue(gl, v, hueMin, hueMax, 1.0f);
    }

    public static void colorUnipolarHue(GL2 gl, float v, float hueMin, float hueMax, float alpha) {
        hsl(gl, lerpSafe(v, hueMin, hueMax), 0.7f, 0.7f, alpha);
    }

    public static void colorUnipolarHue(float[] c, float v) {
        colorUnipolarHue(c, v, 0, 1);
    }

    public static void colorUnipolarHue(float[] c, float v, float hueMin, float hueMax) {
        colorUnipolarHue(c, v, hueMin, hueMax, 1.0f);
    }

    public static void colorUnipolarHue(float[] c, float v, float hueMin, float hueMax, float alpha) {
        hsb(c, lerpSafe(v, hueMin, hueMax), 0.7f, 0.7f, alpha);
    }

    public static void colorHash(Object x, float[] color) {
        colorHash(x.hashCode(), color, 1.0f);
    }

    public static void colorHash(int hash, float[] color, float sat, float bri, float alpha) {
        hsb(color, (Math.abs(hash) % 500) / 500.0f * 360.0f, sat, bri, alpha);
    }

    public static void colorHash(int hash, float[] color, float alpha) {
        colorHash(hash, color, 0.5f, 0.5f, alpha);
    }

    public static void colorHash(GL2 gl, int hash, float sat, float bri, float alpha) {
        float[] f = new float[4];
        colorHash(hash, f, sat, bri, alpha);
        gl.glColor4fv(f, 0);
    }

    public static void colorHash(GL2 gl, int hash, float alpha) {
        colorHash(gl, hash, 0.7f, 0.7f, alpha);
    }

    public static void colorHashRange(GL2 gl, int hash, float hueStart, float hueEnd, float alpha) {
        float h = lerpSafe((float) Math.abs(hash) / Integer.MAX_VALUE, hueStart, hueEnd);
        hsl(gl, h, 0.7f, 0.7f, alpha);
    }

    private static void colorHash(GL2 gl, Object o, float alpha) {
        colorHash(gl, o.hashCode(), alpha);
    }

    public static void colorHash(GL2 gl, Object o) {
        colorHash(gl, o, 1.0f);
    }

    public static void colorGrays(GL2 gl, float x) {
        gl.glColor3f(x, x, x);
    }

    public static void colorGrays(GL2 gl, float x, float a) {
        gl.glColor4f(x, x, x, a);
    }

    public static void bounds(GL2 gl, Surface s, Consumer<GL2> c) {
        bounds(s.bounds, gl, c);
    }

    public static void bounds(RectF s, GL2 gl, Consumer<GL2> c) {
        bounds(gl, s.x, s.y, s.w, s.h, c);
    }

    private static void bounds(GL2 gl, float x1, float y1, float w, float h, Consumer<GL2> c) {
        gl.glPushMatrix();
        gl.glTranslatef(x1, y1, 0);
        gl.glScalef(w, h, 1);
        c.accept(gl);
        gl.glPopMatrix();
    }

    public static void rect(RectF bounds, GL2 gl) {
        rect(bounds.x, bounds.y, bounds.w, bounds.h, gl);
    }

    public static void rectStroke(RectF bounds, float thickPixels, GL2 gl) {
        gl.glLineWidth(thickPixels);
        rectStroke(bounds, gl);
    }

    public static void rectStroke(RectF bounds, GL2 gl) {
        rectStroke(bounds.x, bounds.y, bounds.w, bounds.h, gl);
    }

    public static void colorRGBA(float[] c, float r, float g, float b, float a) {
        c[0] = r;
        c[1] = g;
        c[2] = b;
        c[3] = a;
    }

    public static void poly(int n, float rad, boolean fill, GL2 gl) {
        poly(n, rad, 0, fill, gl);
    }

    /**
     * TODO https://stackoverflow.com/questions/8779570/opengl-drawing-a-hexigon-with-vertices#8779622
     */
    public static void poly(int n, float rad, float angle, boolean fill, GL2 gl) {

        assert n > 2;

        gl.glBegin(fill ? GL_TRIANGLE_FAN : GL_LINE_LOOP);

        for (int i = 0; i < n; ++i) {
            double theta = fma(i / (float) n, (float) (2 * Math.PI), angle);
            gl.glVertex2f(rad * (float) Math.cos(theta), rad * (float) Math.sin(theta));
        }

        gl.glEnd();
    }

    /**
     * utility for stencil painting
     * include = only draw inside the stencil
     * exclude = only draw outside the stencil
     * adapted from: https:
     */
    public static void stencilMask(GL2 gl, boolean includeOrExclude, Consumer<GL2> paintTheStencilRegion, Consumer<GL2> paintStenciled) {

        stencilStart(gl);

        paintTheStencilRegion.accept(gl);

        stencilUse(gl, includeOrExclude);

        paintStenciled.accept(gl);

        stencilEnd(gl);
    }

    public static void stencilStart(GL gl) {
        boolean wasStencil = gl.glIsEnabled(gl.GL_STENCIL_TEST);

        if (!wasStencil) gl.glEnable(gl.GL_STENCIL_TEST);


        gl.glColorMask(false, false, false, false);
        gl.glDepthMask(false);

        /*
            gl.GL_NEVER: For every pixel, fail the stencil test (so we automatically overwrite the pixel's stencil buffer value)
            1: write a '1' to the stencil buffer for every drawn pixel because glStencilOp has 'gl.GL_REPLACE'
            0xFF: function mask, you'll usually use 0xFF
        */

        gl.glStencilFunc(gl.GL_NEVER, 1, 0xFF);
        gl.glStencilOp(gl.GL_REPLACE, gl.GL_KEEP, gl.GL_KEEP);


        gl.glStencilMask(0xFF);


        if (!wasStencil) gl.glClear(gl.GL_STENCIL_BUFFER_BIT);


    }

    public static void stencilEnd(GL gl) {

        gl.glDisable(gl.GL_STENCIL_TEST);
    }

    public static void stencilUse(GL gl, boolean includeOrExclude) {

        gl.glColorMask(true, true, true, true);
        gl.glDepthMask(true);


        gl.glStencilMask(0x00);


        gl.glStencilFunc(includeOrExclude ? GL_NOTEQUAL : GL_EQUAL, 0, 0xFF);


    }





























    /*
     * http:
     * Hershey Fonts
     * http:
     *
     * Drawn in Processing.
     *
     */

    public static void push(GL2 gl) {
        gl.glPushMatrix();
    }

    public static void pop(GL2 gl) {
        gl.glPopMatrix();
    }

    public static void rectRGBA(RectF bounds, float r, float g, float b, float a, GL2 gl) {
        gl.glColor4f(r, g, b, a);
        rect(bounds, gl);
    }

    public static void halfTriEdge2D(Vec2D f, Vec2D t, float base, GL2 gl) {
        halfTriEdge2D(f.x, f.y, t.x, t.y, base, gl);
    }

    public static void halfTriEdge2D(float fx, float fy, float tx, float ty, float base, GL2 gl) {
        float len = (float) Math.sqrt(sqr(fx - tx) + sqr(fy - ty));
        float theta = (float) fma(Math.atan2(ty - fy, tx - fx), 180 / Math.PI, 270);

        //isosceles triangle
        gl.glPushMatrix();
        gl.glTranslatef((tx + fx) / 2, (ty + fy) / 2, 0);
        gl.glRotatef(theta, 0, 0, 1);
        trif(gl, -base / 2, -len / 2, +base / 2, -len / 2, 0, +len / 2);
        gl.glPopMatrix();
    }

    /**
     * draws unit rectangle
     */
    public static void rectUnit(GL2 g) {
        rect(g, 0, 0, 1, 1);
    }

    public static void color(Color3f c, GL2 gl) {
        gl.glColor3f(c.x, c.y, c.z);
    }

    public static int colorHueInfra(float x, float grayMargin) {
        float colorRange = 1 - grayMargin;
        if (x < colorRange)
            return colorHue(x / colorRange);
        else
            return colorGray((x - colorRange) / grayMargin);
    }

    public static int colorGray(float v) {
        return rgbInt(v,v,v);
    }

    //    public static int colorBipolarHSB(float v) {
//        return hsb(v / 2f + 0.5f, 0.7f, 0.75f);
//    }

    public enum TextAlignment {
        Left, Center, Right
    }


}