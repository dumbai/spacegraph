/* WrappedImage.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Adds functionality to the BufferedImage class, allowing
 *          manipulation of colour indices, making the RGB values
 *          invisible (in the case of Indexed Colour only).
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
package net.propero.rdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

public class WrappedImage {
    static final Logger logger = LoggerFactory.getLogger(WrappedImage.class);
    private final BufferedImage bi;
    private final Graphics gfx;
    private IndexColorModel cm;

    public WrappedImage(int arg0, int arg1, int arg2) {
        bi = new BufferedImage(arg0, arg1, arg2);
        this.gfx = bi.getGraphics();
        this.bi.setAccelerationPriority(1.0f);
    }

    public WrappedImage(int arg0, int arg1, int arg2, IndexColorModel cm) {
        bi = new BufferedImage(arg0, arg1, BufferedImage.TYPE_INT_RGB); 
        this.bi.setAccelerationPriority(1.0f);
        this.gfx = bi.getGraphics();
        
        
        this.cm = cm;
    }

    public int getWidth() {
        return bi.getWidth();
    }

    public int getHeight() {
        return bi.getHeight();
    }

    public BufferedImage getBufferedImage() {
        return bi;
    }

    public Graphics getGraphics() {
        return gfx;
    }

    public BufferedImage getSubimage(int x, int y, int width, int height) {
        return bi.getSubimage(x, y,
                Math.min(bi.getWidth() - x, width),
                Math.min(bi.getHeight() - y, height));
    }

    /**
     * Force a colour to its true RGB representation (extracting from colour
     * model if indexed colour)
     *
     * @param color
     * @return
     */
    public int checkColor(int color) {
        if (cm != null)
            return cm.getRGB(color);
        return color;
    }

    /**
     * Set the colour model for this Image
     *
     * @param cm Colour model for use with this image
     */
    public void setIndexColorModel(IndexColorModel cm) {
        this.cm = cm;
    }

    public void setRGB(int x, int y, int color) {
        
        

        if (cm != null)
            color = cm.getRGB(color);
        bi.setRGB(x, y, color);
    }

    /**
     * Apply a given array of colour values to an area of pixels in the image,
     * do not convert for colour model
     *
     * @param x      x-coordinate for left of area to setAt
     * @param y      y-coordinate for top of area to setAt
     * @param cx     width of area to setAt
     * @param cy     height of area to setAt
     * @param data   array of pixel colour values to apply to area
     * @param offset offset to pixel data in data
     * @param w      width of a line in data (measured in pixels)
     */
    public void setRGBNoConversion(int x, int y, int cx, int cy, int[] data,
                                   int offset, int w) {
        bi.setRGB(x, y, cx, cy, data, offset, w);
    }

    public void setRGB(int x, int y, int cx, int cy, int[] data, int offset,
                       int w) {
        if (cm != null && data != null && data.length > 0) {


            cm.getRGBs(data);
        }

        bi.setRGB(x, y, cx, cy, data, offset, w);
    }

    public int[] getRGB(int x, int y, int cx, int cy, int[] data, int offset,
                        int width) {
        return bi.getRGB(x, y, cx, cy, data, offset, width);
    }

    public int getRGB(int x, int y) {
        
        

        if (cm == null)
            return bi.getRGB(x, y);
        else {
            int pix = bi.getRGB(x, y) & 0xFFFFFF;
            int[] vals = {(pix >> 16) & 0xFF, (pix >> 8) & 0xFF, (pix) & 0xFF};
            int out = cm.getDataElement(vals, 0);




            return out;
        }
    }

}