package spacegraph.video;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.util.concurrent.MoreExecutors;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import jcog.data.map.UnifriedMap;
import jcog.io.bzip2.BZip2InputStream;
import jcog.io.tar.TarEntry;
import jcog.io.tar.TarInputStream;
import jcog.tree.rtree.rect.RectF;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** general purpose texture cache */
public class ImageTexture extends Tex {

    private static final String fa_prefix = "fontawesome://";

    private static Map<String, byte[]> fontAwesomeIcons;

    /**
     * pair(gl context, texture id)
     */
    private static final LoadingCache<Pair<GLContext, String>, TextureData> textureCache =
            //new SoftMemoize<>(cu -> {
            Caffeine.newBuilder().softValues().executor(MoreExecutors.directExecutor()).
                    removalListener((RemovalListener<Pair<GLContext, String>, TextureData>)
                            (c, t, cause) -> {
                                if (t!=null)
                                    t.destroy();
                            })
                    .build(cu -> {
                        try {
                            String u = cu.getTwo();
                            GLProfile profile = cu.getOne().getGL().getGLProfile();
                            if (u.startsWith(fa_prefix)) {
                                String icon = u.substring(fa_prefix.length());
                                byte[] b = fontAwesomeIcons.get("x128/" + icon + "-fs8.png");
                                if (b == null)
                                    throw new UnsupportedOperationException("unrecognized FontAwesome icon: " + u);

                                try (InputStream in = new ByteArrayInputStream(b)) {
                                    return TextureIO.newTextureData(profile, in, true, "png");
                                }
                            } else {
                                return TextureIO.newTextureData(profile, new URL(u), true, null);
                            }
                        } catch (IOException e) {
                            return null;
                        }
                    });

    static {
        //synchronized (ImageTexture.class) {
        UnifriedMap<String, byte[]> fontAwesomeIcons = new UnifriedMap(1024);
//            final int bufferSize = 512 * 1024;
        ClassLoader classLoader = ImageTexture.class.getClassLoader();
        InputStream cs = classLoader.getResourceAsStream("fontawesome_128.bzip2");
        BufferedInputStream bcs = new BufferedInputStream(cs);
        try (TarInputStream fa = new TarInputStream(new BZip2InputStream(true, bcs))) {
            TarEntry e;
            while ((e = fa.getNextEntry()) != null) {
                if (!e.isDirectory())
                    fontAwesomeIcons.put(e.getName(), fa.readAllBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        fontAwesomeIcons.trimToSize();
        ImageTexture.fontAwesomeIcons = fontAwesomeIcons;
    }


    private final String u;
    private final AtomicBoolean loading = new AtomicBoolean(false);
    TextureData textureData;

    private ImageTexture(URL path) {
        this(path.toString());
    }

    public ImageTexture(String path) {
        this.u = path;
        inverted = true;
    }

    /**
     * http://fontawesome.com/icons?d=gallery&m=free
     */
    public static ImageTexture awesome(String icon) {
        return new ImageTexture("fontawesome://" + icon);
    }

    public void paint(GL2 gl, RectF b, float alpha) {
        if (texture == null) {
            if (loading.compareAndSet(false, true)) {

                //Exe.invokeLater(() -> {

                try {
                    textureData = textureCache.get(Tuples.pair(gl.getContext(), u));
                    if (textureData == null)
                        throw new NullPointerException(); //TODO logger.warn
                } catch (NullPointerException e) {
                    e.printStackTrace(); //TODO
                }

                loading.set(false);
            }
            if (textureData != null) {
                texture = TextureIO.newTexture(gl, textureData);
            } else {
                return;
            }
        }

        super.paint(gl, b, alpha);
    }

}