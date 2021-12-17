package spacegraph.video;

import jcog.event.Off;
import jcog.signal.wave2d.RGBBufImgBitmap2D;

import java.awt.image.BufferedImage;
import java.util.function.UnaryOperator;

public abstract class VideoTransform<T extends VideoSource> extends VideoSource {
    public final T src;
    private Off off;

    public VideoTransform(T src) {
        this.src = src;
        off = src.tensor.on(this::update);
    }

    protected void update() {
        tensor.emit(this::next);
    }

    /** TODO collect iteration perf metrics */
    private RGBBufImgBitmap2D next() {
        return new RGBBufImgBitmap2D( setAndGet( apply(src.image) ) );
    }

    /** process a frame */
    protected abstract BufferedImage apply(BufferedImage image);

    @Override
    public void close() {
        off.close();
        off = null;
        //src.close();
    }

    public static VideoTransform<?> the(VideoSource src, UnaryOperator<BufferedImage> frameOp) {
        return new VideoTransform(src) {
            @Override
            protected BufferedImage apply(BufferedImage frame) {
                return frameOp.apply(frame);
            }
        };
    }


}
