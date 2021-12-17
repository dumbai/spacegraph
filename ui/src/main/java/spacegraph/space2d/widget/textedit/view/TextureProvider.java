package spacegraph.space2d.widget.textedit.view;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jogamp.opengl.GL2;
import jcog.exe.Exe;
import org.jetbrains.annotations.Nullable;
import spacegraph.video.Tex;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.IOException;

public final class TextureProvider {
	private static final String DEFAULT_FONT_PATH = "font/CourierPrimeCode.ttf";
	@Deprecated
	public static final TextureProvider the = new TextureProvider();
	private static final int FONT_SIZE = 64;
    private static final int FONT_BITMAP_WIDTH = 48;
	public Font font;
	private LoadingCache<String, BufferedImage> glyphCache;

	private TextureProvider() {
		Exe.run(()->{
			try {
				font = Font.createFont(Font.PLAIN,
					this.getClass().getClassLoader().getResourceAsStream(DEFAULT_FONT_PATH)).deriveFont(
					(float) FONT_SIZE);
			} catch (FontFormatException | IOException e) {
				throw new RuntimeException(e);
			}

			glyphCache = CacheBuilder.newBuilder().maximumSize(2048)
					.build(new CacheLoader<>() {
						@Override
						public BufferedImage load(String c) {
							return getTexture(c);
						}
					});
		});
	}

	Tex getTexture(String c, GL2 gl) {
		return getTexture(c, null, gl);
	}

	Tex getTexture(String c, @Nullable Tex t, GL2 gl) {
		if (t == null) {
			t = new Tex();
			t.gl = gl;
		}

		LoadingCache<String, BufferedImage> cache = this.glyphCache;
		if (cache!=null) {
			BufferedImage charTex = cache.getUnchecked(c);
			t.set(charTex);
			t.commit(gl); //HACK  repeat commit() necessary for some reason
		}
		return t;
	}


//    private float rawGetWidth(String singleCharString) {
//        BufferedImage image = new BufferedImage(FONT_SIZE, FONT_SIZE, BufferedImage.TYPE_BYTE_GRAY);
//        Graphics2D g2d = (Graphics2D) image.getGraphics();
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        FontMetrics fm = g2d.getFontMetrics(font);
//        return fm.charWidth(singleCharString.codePointAt(0)) / FONT_SIZE;
//    }

	private BufferedImage getTexture(String c) {

		int sizeX = FONT_BITMAP_WIDTH, sizeY = FONT_SIZE;
		BufferedImage image = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		FontRenderContext fontRenderContext = g2d.getFontRenderContext();

		Font f = this.font;
		GlyphVector gv = f.createGlyphVector(fontRenderContext, c.toCharArray());

		FontMetrics fm = g2d.getFontMetrics(f);
		g2d.drawGlyphVector(gv, 0 /*(FONT_SIZE - fm.charWidth(c.codePointAt(0)))*/, fm.getMaxAscent());
		return image;
	}
}