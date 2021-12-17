package spacegraph.space2d.widget.meter.audio;

/**
 * *   __ __|_  ___________________________________________________________________________  ___|__ __
 * *
 * *
 * *  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /
 * *   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/
 * *  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\
 * * /  \____\                       http:
 * * \  /   "' _________________________________________________________________________ `"   \  /
 * *  \/____.                                                                             .____\/
 * *
 * * Utility methods dealing with texture input/output and miscellaneous related topics
 * * like filtering, texture-compression and mipmaps.
 * *
 **/

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.jogamp.opengl.GL2.*;

enum TextureUtils {
	;

//    public static Texture loadImageAsTexture_UNMODIFIED(GL inGL, String inFileName) {
//
//
//        try {
//            Texture tTexture = TextureIO.newTexture(new BufferedInputStream((new Object()).getClass().getResourceAsStream(inFileName)), true, null);
//            tTexture.setTexParameterf(inGL, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
//            tTexture.setTexParameterf(inGL, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//            tTexture.setTexParameterf(inGL, GL_TEXTURE_WRAP_S, GL_REPEAT);
//            tTexture.setTexParameterf(inGL, GL_TEXTURE_WRAP_T, GL_REPEAT);
//
//            return tTexture;
//        } catch (Exception e) {
//
//        }
//
//        return null;
//    }


    private static int generateTextureID(GL inGL) {
        int[] result = new int[1];
        inGL.glGenTextures(1, result, 0);

        return result[0];
    }

    public static void deleteTextureID(GL inGL, int inTextureID) {

        inGL.glDeleteTextures(1, new int[]{inTextureID}, 0);
    }

    private static ByteBuffer convertARGBBufferedImageToJOGLRGBADirectByteBuffer(BufferedImage inBufferedImage) {
        return convertARGBBufferedImageToJOGLDirectByteBuffer(inBufferedImage, true, true, true, true);
    }

    public static ByteBuffer convertARGBBufferedImageToJOGLRDirectByteBuffer(BufferedImage inBufferedImage) {
        return convertARGBBufferedImageToJOGLDirectByteBuffer(inBufferedImage, true, false, false, false);
    }

    private static ByteBuffer convertARGBBufferedImageToJOGLDirectByteBuffer(BufferedImage inBufferedImage, boolean inPutR, boolean inPutG, boolean inPutB, boolean inPutA) {

        int tSizeMultiplier = 0;
        if (inPutR) {
            tSizeMultiplier++;
        }
        if (inPutG) {
            tSizeMultiplier++;
        }
        if (inPutB) {
            tSizeMultiplier++;
        }
        if (inPutA) {
            tSizeMultiplier++;
        }
        ByteBuffer tBufferedImageByteBuffer = ByteBuffer.allocateDirect(inBufferedImage.getWidth() * inBufferedImage.getHeight() * tSizeMultiplier);
        tBufferedImageByteBuffer.order(ByteOrder.nativeOrder());
        int[] tBufferedImage_ARGB = ((DataBufferInt) inBufferedImage.getRaster().getDataBuffer()).getData();
        for (int aTBufferedImage_ARGB : tBufferedImage_ARGB) {
            if (inPutR) {
                byte tRed = (byte) ((aTBufferedImage_ARGB >> 16) & 0xFF);
                tBufferedImageByteBuffer.put(tRed);
            }
            if (inPutG) {
                byte tGreen = (byte) ((aTBufferedImage_ARGB >> 8) & 0xFF);
                tBufferedImageByteBuffer.put(tGreen);
            }
            if (inPutB) {
                byte tBlue = (byte) ((aTBufferedImage_ARGB) & 0xFF);
                tBufferedImageByteBuffer.put(tBlue);
            }
            if (inPutA) {
                byte tAlpha = (byte) ((aTBufferedImage_ARGB >> 24) & 0xFF);
                tBufferedImageByteBuffer.put(tAlpha);
            }
        }
        tBufferedImageByteBuffer.rewind();
        return tBufferedImageByteBuffer;
    }

    public static BufferedImage createARGBBufferedImage(int inWidth, int inHeight) {

        BufferedImage tARGBImageIntermediate = new BufferedImage(inWidth, inHeight, BufferedImage.TYPE_INT_ARGB);
        fillImageWithTransparentColor(tARGBImageIntermediate);
        return tARGBImageIntermediate;
    }

    private static void fillImageWithTransparentColor(Image inImage) {
        Color TRANSPARENT = new Color(0, 0, 0, 0);
        fillImageWithColor(inImage, TRANSPARENT);
    }

    private static void fillImageWithColor(Image inImage, Color inColor) {
        Graphics2D tGraphics2D = (Graphics2D) inImage.getGraphics();
        tGraphics2D.setColor(inColor);
        tGraphics2D.setComposite(AlphaComposite.Src);
        tGraphics2D.fillRect(0, 0, inImage.getWidth(null), inImage.getHeight(null));
        tGraphics2D.dispose();
    }

    public static int generateTexture1DFromBufferedImage(GL2 inGL, BufferedImage inBufferedImage, int inBorderMode) {

        inGL.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        int t1DTextureID = TextureUtils.generateTextureID(inGL);
        inGL.glEnable(GL_TEXTURE_1D);
        inGL.glBindTexture(GL_TEXTURE_1D, t1DTextureID);
        inGL.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, inBufferedImage.getWidth(), 0, GL_RGBA, GL_UNSIGNED_BYTE, TextureUtils.convertARGBBufferedImageToJOGLRGBADirectByteBuffer(inBufferedImage));
        inGL.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, inBorderMode);
        inGL.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_T, inBorderMode);
        return t1DTextureID;
    }

//    public static BufferedImage[] loadARGBImageSequence(String inARGBImageSequenceFileName) {
//
//        try {
//            ZipInputStream tZipInputStream = new ZipInputStream(new BufferedInputStream((new Object()).getClass().getResourceAsStream(inARGBImageSequenceFileName)));
//            Hashtable<String,BufferedImage> tHashtable = new Hashtable<String,BufferedImage>();
//            List<String> tZipEntryFileNames = new ArrayList<String>();
//            ZipEntry tZipEntry;
//            while((tZipEntry = tZipInputStream.getNextEntry())!=null) {
//                String inZipEntryName = tZipEntry.getName();
//
//                if (!tZipEntry.isDirectory()) {
//                    BufferedImage tARGBImage = ImageIO.read(tZipInputStream);
//
//                    BufferedImage tARGBImageIntermediate = new BufferedImage(tARGBImage.getWidth(),tARGBImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
//                    tARGBImageIntermediate.getGraphics().drawImage(tARGBImage, 0,0, null);
//                    tHashtable.put(inZipEntryName,tARGBImageIntermediate);
//                    tZipEntryFileNames.addAt(inZipEntryName);
//                } else {
//
//                }
//            }
//            tZipInputStream.close();
//
//
//            Collections.sort(tZipEntryFileNames);
//            BufferedImage[] tBufferedImages = new BufferedImage[tZipEntryFileNames.size()];
//            for (int i=0; i<tZipEntryFileNames.size(); i++) {
//
//                tBufferedImages[i] = tHashtable.get(tZipEntryFileNames.get(i));
//            }
//
//            return tBufferedImages;
//        } catch (Exception e) {
//
//            return null;
//        }
//    }

    public static void loadBufferedImageAs_GL_TEXTURE_2D_WithTextureDXT1Compression(BufferedImage inBufferedImage, int[] inTextureID, GL2ES3 inGL) {
        int tWidth = inBufferedImage.getWidth();
        int tHeight = inBufferedImage.getHeight();
        inGL.glGenTextures(1, inTextureID, 0);
        inGL.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        inGL.glBindTexture(GL_TEXTURE_2D, inTextureID[0]);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_COMPRESSED_RGBA_S3TC_DXT1_EXT, tWidth, tHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, convertARGBBufferedImageToJOGLRGBADirectByteBuffer(inBufferedImage));
        int[] tIsCompressed = new int[1];
        inGL.glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_COMPRESSED, tIsCompressed, 0);

        int[] tCompressedTextureSize = new int[1];
        inGL.glGetTexLevelParameteriv(GL_TEXTURE_2D, 0, GL_TEXTURE_COMPRESSED_IMAGE_SIZE, tCompressedTextureSize, 0);


    }

//    public static byte[] readRawFileAsByteArray(String inFilename) {
//        try {
//            InputStream tInputStream = (new Object()).getClass().getResourceAsStream(inFilename);
//            ByteArrayOutputStream tByteArrayOutputStream = new ByteArrayOutputStream(4096);
//            byte[] tBuffer = new byte[1024];
//            int tBytesRead;
//            while ((tBytesRead = tInputStream.read(tBuffer)) > 0) {
//                tByteArrayOutputStream.write(tBuffer, 0, tBytesRead);
//            }
//            tInputStream.close();
//            return tByteArrayOutputStream.toByteArray();
//        } catch (Exception e) {
//
//        }
//        return null;
//    }

}
