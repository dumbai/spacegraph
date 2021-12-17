package spacegraph.space2d.meta;

import com.jogamp.opengl.GL2;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.video.Draw;

import java.util.function.Function;

/** decoration inference for dynamic representational surfaces of arbitrary objects */
public abstract class IconBuilder<X> implements Function<X,Surface> {

    public static final IconBuilder<Object> simpleBuilder = new IconBuilder<>() {

        @Override
        public Surface apply(Object o) {
            return new VectorLabel(o.toString()) {

                final int classHash = o.getClass().hashCode();

                @Override
                protected void paintIt(GL2 gl, ReSurface r) {
                    super.paintIt(gl, r);
                    Draw.colorHash(gl, classHash);
                    Draw.rect(bounds, gl);
                }
            };
        }
    };

}