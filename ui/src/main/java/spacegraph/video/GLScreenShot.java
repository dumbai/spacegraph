package spacegraph.video;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLDrawable;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.jogamp.opengl.GL.*;

/**
 * depth mode still needs tested
 */
public class GLScreenShot implements Supplier<BufferedImage> {
    Buffer seen;
    int width;
    int height;

    static final int[] rgbBits = {8, 8, 8};
    static final int[] rgbOffsets = {2, 1, 0};
    static final ComponentColorModel rgbColorModel = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB), rgbBits, false, false, 1, 0);

    static final int[] grayBits = {8};
    static final int[] grayOffsets = {0};
    static final ComponentColorModel grayColorModel = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_GRAY), grayBits, false, false, 1, 0);

    private volatile BufferedImage current;

    final boolean rgbOrDepth;

    public GLScreenShot(boolean rgbOrDepth) {
        this.rgbOrDepth = rgbOrDepth;
    }

    /**
     * may be vertically flipped, sorry
     */
    @Override
    public BufferedImage get() {
        synchronized (this) {
            if (seen == null || width == 0 || height == 0)
                return null;

            BufferedImage current = this.current;
            if (current != null)
                return current;

            BufferedImage b;
            byte[] bb = ((ByteBuffer) seen).array();
            if (rgbOrDepth) {

                WritableRaster raster1 = Raster.createInterleavedRaster(
                        new DataBufferByte(bb, bb.length), width, height, width * 3, 3, rgbOffsets, new Point());

                b = new BufferedImage(rgbColorModel, raster1, false, null);
            } else {
                //                GrayU8 g = new GrayU8();
//                g.setWidth(width);
//                g.setHeight(height);
//                g.setData(bb);
//                return ConvertBufferedImage.convertTo(g, null);


                WritableRaster raster1 = Raster.createInterleavedRaster(
                        new DataBufferByte(bb, bb.length), width, height, width, 1, grayOffsets, new Point());

                b = new BufferedImage(grayColorModel, raster1, false, null);

//                float[] f = ((FloatBuffer) seen).array();
//                BandedSampleModel sampleModel = new BandedSampleModel(4 /* float */, width, height, 1);
//                ColorModel colorModel = new ComponentColorModel(rgbRasterColor, nBits, false, false, 0, 0);
//                WritableRaster rasterDepth = Raster.createWritableRaster(sampleModel, new DataBufferFloat(f, f.length), new Point());
//                b = new BufferedImage(colorModel, rasterDepth, false, null);
            }
            b.setAccelerationPriority(1);
            return this.current = b;
        }
    }

    final AtomicBoolean busy = new AtomicBoolean(false);

    public void update(GL gl) {


        if (!busy.compareAndSet(false,true))
            return;

        try {
            GLDrawable drawable = gl.getContext().getGLDrawable();
            width = drawable.getSurfaceWidth();
            height = drawable.getSurfaceHeight();

            int pixels = width * height;


            if (width % 4 != 0)
                gl.glPixelStorei(GL_PACK_ALIGNMENT, 1);

            current = null;
            int bytes = rgbOrDepth ? pixels * 3 : pixels;

            if (seen == null || seen.capacity() != bytes) {
                seen = ByteBuffer.allocate(bytes);
            } else {
                seen.rewind();
            }

            if (rgbOrDepth) {
                gl.glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, seen);
            } else {
                gl.glReadPixels(0, 0, width, height, GL2ES2.GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, seen);
            }

            gl.glPixelStorei(GL_PACK_ALIGNMENT, 4);

        } finally {
            busy.set(false);
        }

    }
}
