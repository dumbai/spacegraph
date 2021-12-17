package spacegraph.util;

import spacegraph.util.math.Color3f;
import spacegraph.util.math.Color4f;

/**
 * Small color object for each particle
 *
 * @author dmurph
 */
@Deprecated public class ParticleColor {
    public byte r;
    public byte g;
    public byte b;
    public byte a;

    public ParticleColor() {
        r = 127;
        g = 127;
        b = 127;
        a = 50;
    }


    public ParticleColor(float r, float g, float b, float a) {
        set(new Color4f(r, g, b, a));
    }

    public ParticleColor(byte r, byte g, byte b, byte a) {
        set(r, g, b, a);
    }

    public ParticleColor(Color4f color) {
        set(color);
    }
    public ParticleColor(Color3f color) {
        set(color);
    }


    private void set(Color4f color) {
        r = (byte) (127 * color.x);
        g = (byte) (127 * color.y);
        b = (byte) (127 * color.z);
        a = (byte) (127 * color.w);
    }

    private void set(Color3f color) {
        r = (byte) (127 * color.x);
        g = (byte) (127 * color.y);
        b = (byte) (127 * color.z);
        a = 127;
    }

    public void set(ParticleColor color) {
        r = color.r;
        g = color.g;
        b = color.b;
        a = color.a;
    }

    public boolean isZero() {
        return r == 0 && g == 0 && b == 0 && a == 0;
    }

    private void set(byte r, byte g, byte b, byte a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
