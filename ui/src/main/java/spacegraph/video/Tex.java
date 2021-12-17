package spacegraph.video;


import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import jcog.TODO;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.unit.AspectAlign;

import java.awt.image.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2GL3.GL_UNSIGNED_INT_8_8_8_8_REV;

/**
 * https://www.khronos.org/opengl/wiki/Image_Format
 */
public class Tex {

    private final AtomicBoolean updated = new AtomicBoolean(false);
    public Texture texture;

    /**
     * necessary weird rotation correction.. dunno why yet
     */
    boolean inverted = false;
    private boolean mipmap = false;
    private TextureData data;
    private Object src;

    @Deprecated
    public transient GL2 gl;
    private float repeatScale = -1;

    public static TexSurface view(BufferedImage b) {
        return new BufferedImageTexSurface(b);
    }


    public Tex mipmap(boolean mipmap) {
        this.mipmap = mipmap;
        return this;
    }

    /** HACK may need to call '.commit(gl)' before painting if the image has been updated */
    public final void paint(GL2 gl, RectF bounds) {
        paint(gl, bounds, 1.0f);
    }


    public void paint(GL2 gl, RectF b, float alpha) {

        this.gl = gl;
        Texture t = texture;
        if (t != null) {
            Draw.rectTex(t,
                b.x,
                b.y,
                b.w,
                b.h, 0, repeatScale, alpha, mipmap, inverted, gl);
        }
    }

    /**
     * try to commit
     */
    public final void commit(GL2 gl) {
        TextureData data = this.data;
        if (data == null)
            return;

        Texture texture = this.texture;

        if (texture == null)
            texture = this.texture = TextureIO.newTexture(gl, data);

        if (updated.compareAndSet(true, false))
            texture.updateImage(gl, data);
    }

    public boolean set(BufferedImage i, GL2 gl) {
        if (set(i)) {
           commit(gl);
           return true;
       } else
           return false;
    }


    public boolean set(BufferedImage i) {

        if (gl != null) {
            DataBuffer x = i.getRaster().getDataBuffer();

            Object y = x instanceof DataBufferInt ?
                    ((DataBufferInt) x).getData() :
                    ((DataBufferByte) x).getData();

            TextureData data = this.data;
            if (src != y || (data != null && (data.getWidth() != i.getWidth() || data.getHeight() != i.getHeight()))) {
                _set(y, i.getWidth(), i.getHeight(), i.getColorModel(), gl);
                this.src = y;
            }
        }

        updated.set(true);

        return true;
    }

//    public void set(int[] iimage, int width, int height) {
//        if (!ready())
//            return;
//
//        if (data == null) {
//            _set(iimage, width, height, true);
//        }
//
//        updated.set(true);
//    }

//    private void _set(byte[] iimage, int width, int height) {
//
//        this.src = iimage;
//
//        ByteBuffer buffer = ByteBuffer.wrap(iimage);
//
//
//    }

    private synchronized void _set(Object x, int width, int height, ColorModel color, GL2 gl) {




        Buffer buffer = x instanceof int[] ? IntBuffer.wrap((int[]) x) : ByteBuffer.wrap((byte[]) x);
            /*if (this.data != null) {
                data.setWidth(width);
                data.setHeight(height);
                data.setBuffer(buffer);
            } else */{

                GLProfile profile = gl.getGLProfile();
                if (color.getNumColorComponents()==1) {
                    //grayscale

                    if (x instanceof byte[]) {
                        data = new TextureData(profile, GL_LUMINANCE,
                                width, height,
                                0 /* border */,
                                GL_LUMINANCE, GL_UNSIGNED_BYTE,
                                mipmap,
                                false,
                                false,
                                buffer, null
                        );
                    } else {
                        throw new TODO();
                    }
                } else {

                    //assume RGB/RGBA

                    if (x instanceof int[]) {
                        boolean alpha = color.hasAlpha();

                        data = new TextureData(profile, alpha ? GL_RGBA : GL_RGB,
                                width, height,
                                0 /* border */,
                                GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV,
                                mipmap,
                                false,
                                false,
                                buffer, null
                        );
                    } else {
                        data = new TextureData(profile, GL_RGB,
                                width, height,
                                0 /* border */,
                                GL_RGB,
                                GL_UNSIGNED_BYTE,
                                mipmap,
                                false,
                                false,
                                buffer, null
                        );
                    }
                }
            }

    }

    public TexSurface view() {
        return new TexSurface(this);
    }

    public Surface view(float aspect) {
        return new AspectAlign(view(), aspect);
    }

    /**
     * less efficient than: b = update(x, b)
     */
    public BufferedImage set(GrayU8 x) {
        return set(x, null);
    }

    public BufferedImage set(GrayU8 x, BufferedImage b) {
        this.src = x;

        if (data == null) {
            if (b == null || b.getWidth() != x.width || b.getHeight() != x.height)
                b = new BufferedImage(x.width, x.height, BufferedImage.TYPE_INT_ARGB);

            set(ConvertBufferedImage.convertTo(x, b));
        }

        return b;


    }

    public void stop(Surface x) {
//        Zoomed r = (Zoomed) x.root();
//        if (r != null) {
//            JoglDisplay s = r.space;
//            if (s != null) {
//                if (texture != null) {
//                    //TODO if texure is shared, dont?
//                    this.texture.destroy(s.gl());
//                    this.texture = null;
//                }
//            }
//        }
    }

    public void delete() {
        Texture tt = this.texture;
        GL2 gl = this.gl;
        if (gl != null && tt != null)
            tt.destroy(gl);

        this.src = null;
        this.texture = null;
        this.gl = null;
        this.data = null;
        this.src = null;
    }


    @Deprecated
    private static class BufferedImageTexSurface extends TexSurface {
        private final BufferedImage b;

        BufferedImageTexSurface(BufferedImage b) {
            this.b = b;
        }

        @Override
        protected void paint(GL2 gl, ReSurface reSurface) {
            Tex t = this.tex;
            if (t != null && t.data == null)
                t.set(b);
            super.paint(gl, reSurface);
        }

		@Override
		public boolean delete() {
            if (super.delete()) {
                this.tex.delete();
                return true;
            }
			return false;
		}
	}


}