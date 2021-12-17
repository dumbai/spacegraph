/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
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

package com.bulletphysics.render;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Hashtable;

/**
 *
 * @author jezek2
 */
public class FontRender {
	
	//private static final File cacheDir = new File("/path/to/font/cache/dir/");
	private static GL2 gl;
	private static GLU glu;
	
	public static void init(GL2 gl,GLU glu){
		FontRender.gl=gl;
		FontRender.glu=glu;
	}
	
	private FontRender() {
	}
	
	protected static class Glyph {
		int x,y,w,h;		
		int list = -1;
	}
	
	public static class GLFont {
		int texture;
		int width;
		int height;
		final Glyph[] glyphs = new Glyph[128-32];
		
		GLFont() {
			for (int i=0; i<glyphs.length; i++) glyphs[i] = new Glyph();
		}
		
		public GLFont(InputStream in) throws IOException {
			this();
			load(in);
		}

		public void destroy() {
			FontRender.gl.glDeleteTextures(1,IntBuffer.wrap(new int[] { texture }));
		}
		
		protected void save(File f) throws IOException {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
			out.writeInt(width);
			out.writeInt(height);

			gl.glPixelStorei(GL2.GL_PACK_ROW_LENGTH, 0);
			gl.glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
			gl.glPixelStorei(GL2.GL_PACK_SKIP_ROWS, 0);
			gl.glPixelStorei(GL2.GL_PACK_SKIP_PIXELS, 0);
			
			int size = width*height*4;
			ByteBuffer buf = Buffers.newDirectByteBuffer(size);
			byte[] data = new byte[size];
			gl.glBindTexture(GL2.GL_TEXTURE_2D, texture);
			gl.glGetTexImage(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, buf.position(0));
			buf.get(data);
			out.write(data);

			for (int i=0; i<glyphs.length; i++) {
				out.writeShort(glyphs[i].x);
				out.writeShort(glyphs[i].y);
				out.writeShort(glyphs[i].w);
				out.writeShort(glyphs[i].h);
			}
			
			out.close();
		}

		protected void load(File f) throws IOException {
			load(new FileInputStream(f));
		}
		
		void load(InputStream _in) throws IOException {
			DataInputStream in = new DataInputStream(_in);
			int w = in.readInt();
			int h = in.readInt();
			int size = w*h*4;
			
			gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, 0);
			gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
			gl.glPixelStorei(GL2.GL_UNPACK_SKIP_ROWS, 0);
			gl.glPixelStorei(GL2.GL_UNPACK_SKIP_PIXELS, 0);
			
			ByteBuffer buf = Buffers.newDirectByteBuffer(size);
			byte[] data = new byte[size];
			in.read(data);
			buf.put(data);

			int[] id = new int[1];
			gl.glGenTextures(1,IntBuffer.wrap(id));
			texture = id[0];
			width = w;
			height = h;
			
			gl.glBindTexture(GL2.GL_TEXTURE_2D, texture);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, w, h, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, buf.position(0));
			
			for (int i=0; i<glyphs.length; i++) {
				glyphs[i].x = in.readShort();
				glyphs[i].y = in.readShort();
				glyphs[i].w = in.readShort();
				glyphs[i].h = in.readShort();
			}
			
			in.close();
		}
	}
	
	private static String getFontFileName(String family, int size, boolean bold) {
		return family.replace(' ','_')+"_"+size+(bold? "_bold":"")+".fnt";
	}
	
	public static GLFont createFont(String family, int size, boolean bold, boolean antialiasing) {
		GLFont gf = new GLFont();
		/*File f = new File(cacheDir, getFontFileName(family, size, bold));
		if (f.exists()) {
			gf.load(f);
			return gf;
		}*/
		
		BufferedImage img = renderFont(new Font(family, bold? Font.BOLD : Font.PLAIN, size), antialiasing, gf.glyphs);
		gf.texture = createTexture(img, false);
		gf.width = img.getWidth();
		gf.height = img.getHeight();
		//gf.save(f);
		return gf;
	}
	
	private static BufferedImage renderFont(Font font, boolean antialiasing, Glyph[] glyphs) {
		FontRenderContext frc = new FontRenderContext(null, antialiasing, false);
		
		int imgw = 256;
		if (font.getSize() >= 36) imgw <<= 1;
		if (font.getSize() >= 72) imgw <<= 1;
		
		//BufferedImage img = new BufferedImage(imgw, 1024, BufferedImage.TYPE_INT_ARGB);
		BufferedImage img = createImage(imgw, 1024, true);
		Graphics2D g = (Graphics2D)img.getGraphics();
		
		if (antialiasing) {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}
		
		g.setColor(Color.WHITE);
		g.setFont(font);
		
		int x=0, y=0,rowsize=0;
		for (int c=32; c<128; c++) {
			String s = String.valueOf((char) c);
			Rectangle2D rect = font.getStringBounds(s, frc);
			LineMetrics lm = font.getLineMetrics(s, frc);
			int w = (int)rect.getWidth()+1;
			int h = (int)rect.getHeight()+2;

			if (x+w+2 > img.getWidth()) {
				x = 0;
				y += rowsize;
				rowsize = 0;
			}
			
			g.drawString(s, x+1, y+(int)lm.getAscent()+1);
			
			if (glyphs != null) {
				glyphs[c-32].x = x+1;
				glyphs[c-32].y = y+1;
				glyphs[c-32].w = w;
				glyphs[c-32].h = h;
			}
			
			w += 2;
			h += 2;
			
			x += w;
			rowsize = Math.max(rowsize, h);
		}
		
		y += rowsize;
		g.dispose();
		
		if (y < 128) img = img.getSubimage(0, 0, img.getWidth(), 128);
		else if (y < 256) img = img.getSubimage(0, 0, img.getWidth(), 256);
		else if (y < 512) img = img.getSubimage(0, 0, img.getWidth(), 512);
		
		return img;
	}
	
	private static void renderGlyph(GLFont font, Glyph g) {
		if (g.list != -1) {
			gl.glCallList(g.list);
			return;
		}
		
		g.list = gl.glGenLists(1);
		gl.glNewList(g.list, GL2.GL_COMPILE);
		
		float tw = font.width;
		float th = font.height;
		
		int x=0, y=0;
		
		gl.glBegin(GL2.GL_QUADS);
			gl.glTexCoord2f((g.x) /tw, (g.y) /th);
			gl.glVertex3f(x, y, 1);

			gl.glTexCoord2f((g.x+g.w-1) /tw, (g.y) /th);
			gl.glVertex3f(x+g.w-1, y, 1);

			gl.glTexCoord2f((g.x+g.w-1) /tw, (g.y+g.h-1) /th);
			gl.glVertex3f(x+g.w-1, y+g.h-1, 1);

			gl.glTexCoord2f((g.x) /tw, (g.y+g.h-1) /th);
			gl.glVertex3f(x, y+g.h-1, 1);
			gl.glEnd();
		
		gl.glEndList();
		gl.glCallList(g.list);
	}

	public static void drawString(GLFont font, CharSequence s, int x, int y, float red, float green, float blue) {
		drawString(font, s, x, y, red, green, blue, 1);
	}
	
	private static void drawString(GLFont font, CharSequence s, int x, int y, float red, float green, float blue, float alpha) {
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glPushMatrix();
		gl.glTranslatef(x, y, 0);
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, font.texture);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glColor4f(red, green, blue, alpha);
		//glColor4f(1, 1, 1, 1);
		for (int i=0, n=s.length(); i<n; i++) {
			char c = s.charAt(i);
			if (c < 32 || c > 128) c = '?';
			Glyph g = font.glyphs[c-32];
			renderGlyph(font, g);
			//x += g.w;
			//glTranslatef(g.w, 0, 0);
			gl.glTranslatef(g.w-2, 0, 0);
		}
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		gl.glPopMatrix();

		gl.glDisable(GL2.GL_BLEND);
	}
	
	private static final ColorModel glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8,8,8,0}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
	private static final ColorModel glColorModelAlpha = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8,8,8,8}, true, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
	
	private static int createTexture(BufferedImage img, boolean mipMap) {
		boolean USE_COMPRESSION = false;

		int[] id = new int[1];
		gl.glGenTextures(1,IntBuffer.wrap(id));
		int tex = id[0];
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, tex);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, mipMap? GL2.GL_LINEAR_MIPMAP_LINEAR : GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		
		byte[] data = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
		
		ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
		buf.order(ByteOrder.nativeOrder());
		buf.put(data, 0, data.length);
		buf.flip();
		
		boolean alpha = img.getColorModel().hasAlpha();
		
		//glTexImage2D(GL2.GL_TEXTURE_2D, 0, alpha? GL2.GL_RGBA:GL2.GL_RGB, img.getWidth(), img.getHeight(), 0, alpha? GL2.GL_RGBA:GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, buf);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, USE_COMPRESSION? (alpha? GL2.GL_COMPRESSED_RGBA:GL2.GL_COMPRESSED_RGB) : (alpha? GL2.GL_RGBA:GL2.GL_RGB), img.getWidth(), img.getHeight(), 0, alpha? GL2.GL_RGBA:GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, buf);
		if (mipMap) {
			glu.gluBuild2DMipmaps(GL2.GL_TEXTURE_2D, USE_COMPRESSION? (alpha? GL2.GL_COMPRESSED_RGBA:GL2.GL_COMPRESSED_RGB) : (alpha? GL2.GL_RGBA:GL2.GL_RGB), img.getWidth(), img.getHeight(), alpha? GL2.GL_RGBA:GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, buf);
			//gluBuild2DMipmaps(GL2.GL_TEXTURE_2D, GL2.GL_COMPRESSED_RGB, img.getWidth(), img.getHeight(), GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, buf);
		}
		
		return tex;
	}
	
	private static BufferedImage createImage(int width, int height, boolean alpha) {
		if (alpha) {
			WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 4, null);
			return new BufferedImage(glColorModelAlpha, raster, false, new Hashtable());
		}
		
		WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 3, null);
		return new BufferedImage(glColorModel, raster, false, new Hashtable());
	}
	
}