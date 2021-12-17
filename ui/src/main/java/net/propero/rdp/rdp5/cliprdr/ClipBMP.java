/* ClipBMP.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * (See gpl.txt for details of the GNU General Public License.)
 *
 */
package net.propero.rdp.rdp5.cliprdr;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ClipBMP extends Component {

    private static final Logger logger = LoggerFactory.getLogger(ClipBMP.class);
    private static final long serialVersionUID = -756738379924520867L;
    
    private static final int BITMAPFILEHEADER_SIZE = 14;

    private static final int BITMAPINFOHEADER_SIZE = 40;
    private static final int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
    private static final int biSize = BITMAPINFOHEADER_SIZE;
    private static final int biPlanes = 1;
    private static final int biBitCount = 24;
    
    
    private final byte[] bitmapFileHeader = new byte[14];
    private final byte[] bfType = {'B', 'M'};
    
    private final byte[] bitmapInfoHeader = new byte[40];
    private int bfSize;
    private int bfReserved1;
    private int bfReserved2;
    private int biWidth;
    private int biHeight;
    private int biCompression;

    private int biSizeImage = 0x030000;

    private int biXPelsPerMeter;

    private int biYPelsPerMeter;

    private int biClrUsed;

    private int biClrImportant;


    private int[] bitmap;

    
    private OutputStream fo;

    
    public ClipBMP() {
    }

    /*
     *
     * intToWord converts an int to a word, where the return value is stored in
     * a 2-byte array.
     *
     */
    private static byte[] intToWord(int parValue) {
        byte[] retValue = new byte[2];
        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x00FF);
        return (retValue);
    }

    /*
     *
     * intToDWord converts an int to a double word, where the return value is
     * stored in a 4-byte array.
     *
     */
    private static byte[] intToDWord(int parValue) {
        byte[] retValue = new byte[4];
        retValue[0] = (byte) (parValue & 0x00FF);
        retValue[1] = (byte) ((parValue >> 8) & 0x000000FF);
        retValue[2] = (byte) ((parValue >> 16) & 0x000000FF);
        retValue[3] = (byte) ((parValue >> 24) & 0x000000FF);
        return (retValue);
    }

    /**
     * loadbitmap() method converted from Windows C code. Reads only
     * uncompressed 24- and 8-bit images. Tested with images saved using
     * Microsoft Paint in Windows 95. If the image is not a 24- or 8-bit image,
     * the program refuses to even try. I guess one could include 4-bit images
     * by masking the byte by first 1100 and then 0011. I am not really
     * interested in such images. If a compressed image is attempted, the
     * routine will probably fail by generating an IOException. Look for
     * variable ncompression to be different from 0 to indicate compression is
     * present.
     * <p>
     * Arguments: sdir and sfile are the result of the FileDialog()
     * getDirectory() and getFile() methods.
     * <p>
     * Returns: Image Object, be sure to check for (Image)null !!!!
     */
    public static Image loadbitmap(InputStream fs) {
        try {
            
            
            
            int bilen = 40;
            byte[] bi = new byte[bilen];
            fs.read(bi, 0, bilen);

            
            
            
            
            

            
            
            
            

            int nwidth = ((bi[7] & 0xff) << 24)
                    | ((bi[6] & 0xff) << 16)
                    | ((bi[5] & 0xff) << 8) | bi[4] & 0xff;
            

            int nheight = ((bi[11] & 0xff) << 24)
                    | ((bi[10] & 0xff) << 16)
                    | ((bi[9] & 0xff) << 8) | bi[8] & 0xff;
            

            
            

            int nbitcount = ((bi[15] & 0xff) << 8) | bi[14] & 0xff;
            

            
            
            
            
            

            int nsizeimage = ((bi[23] & 0xff) << 24)
                    | ((bi[22] & 0xff) << 16)
                    | ((bi[21] & 0xff) << 8) | bi[20] & 0xff;
            

            
            
            
            

            
            
            
            

            int nclrused = ((bi[35] & 0xff) << 24)
                    | ((bi[34] & 0xff) << 16)
                    | ((bi[33] & 0xff) << 8) | bi[32] & 0xff;


            Image image;
            switch (nbitcount) {
                case 24 -> {
                    int npad = (nsizeimage / nheight) - nwidth * 3;
                    byte[] brgb = new byte[(nwidth + npad) * 3 * nheight];
                    fs.read(brgb, 0, (nwidth + npad) * 3 * nheight);
                    int nindex = 0;
                    int[] ndata = new int[nheight * nwidth];
                    for (int j = 0; j < nheight; j++) {
                        for (int i = 0; i < nwidth; i++) {
                            ndata[nwidth * (nheight - j - 1) + i] = (0xff) << 24
                                    | ((brgb[nindex + 2] & 0xff) << 16)
                                    | ((brgb[nindex + 1] & 0xff) << 8)
                                    | brgb[nindex] & 0xff;


                            nindex += 3;
                        }
                        nindex += npad;
                    }
                    image = Toolkit.getDefaultToolkit()
                            .createImage(
                                    new MemoryImageSource(nwidth, nheight, ndata,
                                            0, nwidth));
                }
                case 16 -> {


                    int nNumColors = 0;
                    nNumColors = nclrused > 0 ? nclrused : (1 & 0xff) << 16;


                    if (nsizeimage == 0) {
                        nsizeimage = ((((nwidth * nbitcount) + 31) & ~31) >> 3);
                        nsizeimage *= nheight;


                    }


                    int[] npalette = new int[nNumColors];
                    byte[] bpalette = new byte[nNumColors * 4];
                    fs.read(bpalette, 0, nNumColors * 4);
                    int nindex8 = 0;
                    for (int n = 0; n < nNumColors; n++) {
                        npalette[n] = (0xff) << 24
                                | ((bpalette[nindex8 + 2] & 0xff) << 16)
                                | ((bpalette[nindex8 + 1] & 0xff) << 8)
                                | bpalette[nindex8] & 0xff;


                        nindex8 += 4;
                    }


                    int npad8 = (nsizeimage / nheight) - nwidth;


                    byte[] bdata = new byte[(nwidth + npad8) * nheight];
                    fs.read(bdata, 0, (nwidth + npad8) * nheight);
                    nindex8 = 0;
                    int[] ndata8 = new int[nwidth * nheight];
                    for (int j8 = 0; j8 < nheight; j8++) {
                        for (int i8 = 0; i8 < nwidth; i8++) {
                            ndata8[nwidth * (nheight - j8 - 1) + i8] = npalette[(bdata[nindex8] & 0xff)]
                                    | npalette[(bdata[nindex8 + 1] & 0xff)] << 8;
                            nindex8 += 2;
                        }
                        nindex8 += npad8;
                    }

                    image = Toolkit.getDefaultToolkit().createImage(
                            new MemoryImageSource(nwidth, nheight, ndata8, 0,
                                    nwidth));
                    break;
                }
                case 8 -> {


                    int nNumColors = 0;
                    nNumColors = nclrused > 0 ? nclrused : (1 & 0xff) << 8;


                    if (nsizeimage == 0) {
                        nsizeimage = ((((nwidth * nbitcount) + 31) & ~31) >> 3);
                        nsizeimage *= nheight;


                    }


                    int[] npalette = new int[nNumColors];
                    byte[] bpalette = new byte[nNumColors * 4];
                    fs.read(bpalette, 0, nNumColors * 4);
                    int nindex8 = 0;
                    for (int n = 0; n < nNumColors; n++) {
                        npalette[n] = (0xff) << 24
                                | ((bpalette[nindex8 + 2] & 0xff) << 16)
                                | ((bpalette[nindex8 + 1] & 0xff) << 8)
                                | bpalette[nindex8] & 0xff;


                        nindex8 += 4;
                    }


                    int npad8 = (nsizeimage / nheight) - nwidth;


                    byte[] bdata = new byte[(nwidth + npad8) * nheight];
                    fs.read(bdata, 0, (nwidth + npad8) * nheight);
                    nindex8 = 0;
                    int[] ndata8 = new int[nwidth * nheight];
                    for (int j8 = 0; j8 < nheight; j8++) {
                        for (int i8 = 0; i8 < nwidth; i8++) {
                            ndata8[nwidth * (nheight - j8 - 1) + i8] = npalette[(bdata[nindex8] & 0xff)];
                            nindex8++;
                        }
                        nindex8 += npad8;
                    }

                    image = Toolkit.getDefaultToolkit().createImage(
                            new MemoryImageSource(nwidth, nheight, ndata8, 0,
                                    nwidth));
                    break;
                }
                case 4 -> {
                    int nNumColors = 0;
                    nNumColors = nclrused > 0 ? nclrused : (1 & 0xff) << 4;
                    if (nsizeimage == 0) {
                        nsizeimage = ((((nwidth * nbitcount) + 31) & ~31) >> 3);
                        nsizeimage *= nheight;


                    }
                    int[] npalette = new int[nNumColors + 1];
                    byte[] bpalette = new byte[nNumColors * 4];
                    fs.read(bpalette, 0, nNumColors * 4);
                    int nindex8 = 0;
                    for (int n = 0; n < nNumColors; n++) {
                        npalette[n] = (0xff) << 24
                                | ((bpalette[nindex8 + 2] & 0xff) << 16)
                                | ((bpalette[nindex8 + 1] & 0xff) << 8)
                                | bpalette[nindex8] & 0xff;
                        nindex8 += 4;
                    }
                    int npad8 = (nsizeimage * 2 / nheight) - nwidth;
                    if (npad8 == 4)
                        npad8 = 0;
                    byte[] bdata = new byte[(nwidth / 2 + npad8) * nheight];
                    fs.read(bdata, 0, (nwidth / 2 + npad8) * nheight);
                    nindex8 = 0;
                    int[] ndata8 = new int[nwidth * nheight];
                    for (int j8 = 0; j8 < nheight; j8++) {
                        for (int i8 = 0; i8 < (nwidth) - 1; i8 += 2) {
                            ndata8[nwidth * (nheight - j8 - 1) + i8] = npalette[bdata[nindex8] & 0x0f];
                            ndata8[nwidth * (nheight - j8 - 1) + i8 + 1] = npalette[((bdata[nindex8] & 0xf0) / 0xf)];
                            System.out.print("1:" + (bdata[nindex8] & 0x0f) + '\t');
                            System.out.print("2:" + ((bdata[nindex8] & 0xf0) / 0xf)
                                    + '\t');


                            nindex8++;
                        }

                    }
                    image = Toolkit.getDefaultToolkit().createImage(
                            new MemoryImageSource(nwidth, nheight, ndata8, 0,
                                    nwidth));
                }
                default -> {
                    logger
                            .warn("Not a 24-bit or 8-bit Windows Bitmap, aborting...");
                    image = null;
                }
            }

            fs.close();
            return image;
        } catch (Exception e) {
            
            
        }
        return null;
    }

    public byte[] getBitmapAsBytes(Image parImage, int parWidth, int parHeight) {
        try {
            fo = new ByteArrayOutputStream();
            save(parImage, parWidth, parHeight);
            fo.close();
        } catch (Exception saveEx) {
            saveEx.printStackTrace();
        }
        return ((ByteArrayOutputStream) fo).toByteArray();
    }

    public void saveBitmap(String parFilename, Image parImage, int parWidth,
                           int parHeight) {
        try {
            fo = new FileOutputStream(parFilename);
            save(parImage, parWidth, parHeight);
            fo.close();
        } catch (Exception saveEx) {
            saveEx.printStackTrace();
        }
    }

    /*
     * writeBitmapFileHeader writes the bitmap file header to the file.
     *
     */
    
    
    
    
    
    
    
    
    
    
    

    /*
     * The saveMethod is the main method of the process. This method will call
     * the convertImage method to convert the memory image to a byte array;
     * method writeBitmapFileHeader creates and writes the bitmap file header;
     * writeBitmapInfoHeader creates the information header; and writeBitmap
     * writes the image.
     *
     */
    private void save(Image parImage, int parWidth, int parHeight) {
        try {
            convertImage(parImage, parWidth, parHeight);
            
            writeBitmapInfoHeader();
            writeBitmap();
        } catch (Exception saveEx) {
            saveEx.printStackTrace();
        }
    }

    /*
     * convertImage converts the memory image to the bitmap format (BRG). It
     * also computes some information for the bitmap info header.
     *
     */
    private boolean convertImage(Image parImage, int parWidth, int parHeight) {
        bitmap = new int[parWidth * parHeight];
        PixelGrabber pg = new PixelGrabber(parImage, 0, 0, parWidth, parHeight,
                bitmap, 0, parWidth);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return (false);
        }
        int pad = (4 - ((parWidth * 3) % 4)) * parHeight;
        biSizeImage = ((parWidth * parHeight) * 3) + pad;
        bfSize = biSizeImage + BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
        biWidth = parWidth;
        biHeight = parHeight;
        return (true);
    }

    /*
     * writeBitmap converts the image returned from the pixel grabber to the
     * format required. Remember: scan lines are inverted in a bitmap file!
     *
     * Each scan line must be padded to an even 4-byte boundary.
     */
    private void writeBitmap() {
        int size = (biWidth * biHeight) - 1;
        int pad = 4 - ((biWidth * 3) % 4);
        if (pad == 4) 
            pad = 0;
        int rowIndex = size - biWidth;
        int lastRowIndex = rowIndex;
        try {
            int padCount = 0;
            int rowCount = 1;
            byte[] rgb = new byte[3];
            for (int j = 0; j < size; j++) {
                int value = bitmap[rowIndex];
                rgb[0] = (byte) (value & 0xFF);
                rgb[1] = (byte) ((value >> 8) & 0xFF);
                rgb[2] = (byte) ((value >> 16) & 0xFF);
                fo.write(rgb);
                if (rowCount == biWidth) {
                    padCount += pad;
                    for (int i = 1; i <= pad; i++) {
                        fo.write(0x00);
                    }
                    rowCount = 1;
                    rowIndex = lastRowIndex - biWidth;
                    lastRowIndex = rowIndex;
                } else
                    rowCount++;
                rowIndex++;
            }
            
            bfSize += padCount - pad;
            biSizeImage += padCount - pad;
        } catch (Exception wb) {
            wb.printStackTrace();
        }
    }

    /*
     *
     * writeBitmapInfoHeader writes the bitmap information header to the file.
     *
     */
    private void writeBitmapInfoHeader() {
        try {
            fo.write(intToDWord(biSize));
            fo.write(intToDWord(biWidth));
            fo.write(intToDWord(biHeight));
            fo.write(intToWord(biPlanes));
            fo.write(intToWord(biBitCount));
            fo.write(intToDWord(biCompression));
            fo.write(intToDWord(biSizeImage));
            fo.write(intToDWord(biXPelsPerMeter));
            fo.write(intToDWord(biYPelsPerMeter));
            fo.write(intToDWord(biClrUsed));
            fo.write(intToDWord(biClrImportant));
        } catch (Exception wbih) {
            wbih.printStackTrace();
        }
    }

}
