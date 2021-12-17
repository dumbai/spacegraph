/* Orders.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Encapsulates an RDP order
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

import net.propero.rdp.orders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.IndexColorModel;
import java.io.IOException;


public class Orders {
    private static final Logger logger = LoggerFactory.getLogger(Orders.class);
    /* RDP_BMPCACHE2_ORDER */
    private static final int ID_MASK = 0x0007;
    private static final int MODE_MASK = 0x0038;
    private static final int SQUARE = 0x0080;
    private static final int PERSIST = 0x0100;
    private static final int FLAG_51_UNKNOWN = 0x0800;
    private static final int MODE_SHIFT = 3;
    private static final int LONG_FORMAT = 0x80;
    private static final int BUFSIZE_MASK = 0x3FFF; /* or 0x1FFF? */
    private static final int RDP_ORDER_STANDARD = 0x01;
    private static final int RDP_ORDER_SECONDARY = 0x02;
    private static final int RDP_ORDER_BOUNDS = 0x04;
    private static final int RDP_ORDER_CHANGE = 0x08;
    private static final int RDP_ORDER_DELTA = 0x10;
    private static final int RDP_ORDER_LASTBOUNDS = 0x20;
    private static final int RDP_ORDER_SMALL = 0x40;
    private static final int RDP_ORDER_TINY = 0x80;
    /* standard order types */
    private static final int RDP_ORDER_DESTBLT = 0;
    private static final int RDP_ORDER_PATBLT = 1;
    private static final int RDP_ORDER_SCREENBLT = 2;
    private static final int RDP_ORDER_LINE = 9;
    private static final int RDP_ORDER_RECT = 10;
    private static final int RDP_ORDER_DESKSAVE = 11;
    private static final int RDP_ORDER_MEMBLT = 13;
    private static final int RDP_ORDER_TRIBLT = 14;
    private static final int RDP_ORDER_POLYLINE = 22;
    private static final int RDP_ORDER_TEXT2 = 27;
    /* secondary order types */
    private static final int RDP_ORDER_RAW_BMPCACHE = 0;
    private static final int RDP_ORDER_COLCACHE = 1;
    private static final int RDP_ORDER_BMPCACHE = 2;
    private static final int RDP_ORDER_FONTCACHE = 3;
    private static final int RDP_ORDER_RAW_BMPCACHE2 = 4;
    private static final int RDP_ORDER_BMPCACHE2 = 5;
    private static final int MIX_TRANSPARENT = 0;
    private static final int MIX_OPAQUE = 1;
    private static final int TEXT2_VERTICAL = 0x04;
    private static final int TEXT2_IMPLICIT_X = 0x20;
    public static Cache cache;
    private final OrderState os;
    private RdesktopCanvas surface;
    private int rect_colour;

    public Orders() {
        os = new OrderState();
    }

    private static int inPresent(RdpPacket_Localised data, int flags, int size) {

        if ((flags & RDP_ORDER_SMALL) != 0) {
            size--;
        }

        if ((flags & RDP_ORDER_TINY) != 0) {

            if (size < 2) {
                size = 0;
            } else {
                size -= 2;
            }
        }

        int i = 0;
        int bits = 0;
        int present = 0;
        for (i = 0; i < size; i++) {
            bits = data.get8();
            present |= (bits << (i * 8));
        }
        return present;
    }

    private static int ROP2_S(int rop3) {
        return (rop3 & 0x0f);
    }

    private static int ROP2_P(int rop3) {
        return ((rop3 & 0x3) | ((rop3 & 0x30) >> 2));
    }

    /**
     * Process a raw bitmap and store it in the bitmap cache
     *
     * @param data Packet containing raw bitmap data
     * @throws RdesktopException
     */
    private static void processRawBitmapCache(RdpPacket_Localised data)
            throws RdesktopException {
        int cache_id = data.get8();
        data.incrementPosition(1); 
        int width = data.get8();
        int height = data.get8();
        int bpp = data.get8();
        int bufsize = data.getLittleEndian16();
        int cache_idx = data.getLittleEndian16();
        int pdata = data.getPosition();
        data.incrementPosition(bufsize);

        int Bpp = (bpp + 7) / 8;
        byte[] inverted = new byte[width * height * Bpp];
        int pinverted = (height - 1) * (width * Bpp);
        for (int y = 0; y < height; y++) {
            data.copyToByteArray(inverted, pinverted, pdata, width * Bpp);
            
            
            pinverted -= width * Bpp;
            pdata += width * Bpp;
        }

        cache.putBitmap(cache_id, cache_idx, new Bitmap(Bitmap.convertImage(
                inverted, Bpp), width, height, 0, 0), 0);
    }

    /**
     * Process and store details of a colour cache
     *
     * @param data Packet containing cache information
     * @throws RdesktopException
     */
    private static void processColorCache(RdpPacket_Localised data)
            throws RdesktopException {

        int cache_id = data.get8();
        int n_colors = data.getLittleEndian16();


        byte[] palette = new byte[n_colors * 4];
        data.copyToByteArray(palette, 0, data.getPosition(), palette.length);
        data.incrementPosition(palette.length);
        byte[] blue = new byte[n_colors];
        byte[] green = new byte[n_colors];
        byte[] red = new byte[n_colors];
        int j = 0;
        for (int i = 0; i < n_colors; i++) {
            blue[i] = palette[j];
            green[i] = palette[j + 1];
            red[i] = palette[j + 2];
            
            j += 4;
        }
        IndexColorModel cm = new IndexColorModel(8, n_colors, red, green, blue);
        cache.put_colourmap(cache_id, cm);
        
    }

    /**
     * Process a compressed bitmap and store in the bitmap cache
     *
     * @param data Packet containing compressed bitmap
     * @throws RdesktopException
     */
    private static void processBitmapCache(RdpPacket_Localised data)
            throws RdesktopException {
        int pad2, row_size, final_size, size;

        int bufsize = pad2 = row_size = final_size = size = 0;

        int cache_id = data.get8();
        int pad1 = data.get8();
        int width = data.get8();
        int height = data.get8();
        int bpp = data.get8();
        bufsize = data.getLittleEndian16();
        int cache_idx = data.getLittleEndian16();

        /*
         * data.incrementPosition(2); 
         * data.getLittleEndian16(); data.incrementPosition(4); 
         * final_size
         */

        if (Options.use_rdp5) {

            /* Begin compressedBitmapData */
            pad2 = data.getLittleEndian16();


		} else {
            data.incrementPosition(2);


		}
		size = data.getLittleEndian16();
		row_size = data.getLittleEndian16();
		final_size = data.getLittleEndian16();


		int Bpp = (bpp + 7) / 8;
        if (Bpp == 1) {
            byte[] pixel = Bitmap.decompress(width, height, size, data, 1);
            cache.putBitmap(cache_id, cache_idx, new Bitmap(Bitmap
                    .convertImage(pixel, 1), width, height, 0, 0), 0);
        } else {
            int[] pixel = Bitmap.decompressInt(width, height, size, data, Bpp);
            cache.putBitmap(cache_id, cache_idx, new Bitmap(pixel, width,
                    height, 0, 0), 0);
        }
    }

    /**
     * Process a bitmap cache v2 order, storing a bitmap in the main cache, and
     * the persistant cache if so required
     *
     * @param data       Packet containing order and bitmap data
     * @param flags      Set of flags defining mode of order
     * @param compressed True if bitmap data is compressed
     * @throws RdesktopException
     * @throws IOException
     */
    private static void process_bmpcache2(RdpPacket_Localised data, int flags,
                                          boolean compressed) throws RdesktopException, IOException {

        int Bpp = Options.Bpp;
        /* prevent compiler warning */
        byte[] bitmap_id = new byte[8];
        if ((flags & PERSIST) != 0) {
            bitmap_id = new byte[8];
            data.copyToByteArray(bitmap_id, 0, data.getPosition(), 8);
        }

        int height;
        int width;
        if ((flags & SQUARE) != 0) {
            width = data.get8(); 
            height = width;
        } else {
            width = data.get8(); 
            height = data.get8(); 
        }

        int bufsize = data.getBigEndian16();
        bufsize &= BUFSIZE_MASK;
        int cache_idx = data.get8();

        if ((cache_idx & LONG_FORMAT) != 0) {
            int cache_idx_low = data.get8();
            cache_idx = ((cache_idx ^ LONG_FORMAT) << 8) + cache_idx_low;
        }


        int cache_id = flags & ID_MASK;
        logger.info("BMPCACHE2(compr={},flags={},cx={},cy={},id={},idx={},Bpp={},bs={}" + ')', compressed, flags, width, height, cache_id, cache_idx, Bpp, bufsize);

        byte[] bmpdata = new byte[width * height * Bpp];
        int[] bmpdataInt = new int[width * height];

        Bitmap bitmap;
        if (compressed) {
			bmpdataInt = Bpp == 1 ? Bitmap.convertImage(Bitmap.decompress(width,
				height, bufsize, data, 1), 1) : Bitmap.decompressInt(width, height, bufsize, data,
				Bpp);

            bitmap = new Bitmap(bmpdataInt, width, height, 0, 0);
        } else {
            for (int y = 0; y < height; y++)
                data.copyToByteArray(bmpdata, y * (width * Bpp),
                        (height - y - 1) * (width * Bpp), width * Bpp); 
            
            
            
            
            
            
            
            
            
            
            
            
            

            bitmap = new Bitmap(Bitmap.convertImage(bmpdata, Bpp), width,
                    height, 0, 0);
        }


        cache.putBitmap(cache_id, cache_idx, bitmap, 0);

        if ((flags & PERSIST) != 0)
            PstCache.pstcache_put_bitmap(cache_id, cache_idx, bitmap_id,
                    width, height, width * height * Bpp, bmpdata);


    }

    /**
     * Process a font caching order, and store font in the cache
     *
     * @param data Packet containing font cache order, with data for a series of
     *             glyphs representing a font
     * @throws RdesktopException
     */
    private static void processFontCache(RdpPacket_Localised data)
            throws RdesktopException {
        Glyph glyph = null;

        int character = 0, offset = 0, baseline = 0, width = 0, height = 0;
        int datasize = 0;
        byte[] fontdata = null;

        int font = data.get8();
        int nglyphs = data.get8();

        for (int i = 0; i < nglyphs; i++) {
            character = data.getLittleEndian16();
            offset = data.getLittleEndian16();
            baseline = data.getLittleEndian16();
            width = data.getLittleEndian16();
            height = data.getLittleEndian16();
            datasize = (height * ((width + 7) / 8) + 3) & ~3;
            fontdata = new byte[datasize];

            data.copyToByteArray(fontdata, 0, data.getPosition(), datasize);
            data.incrementPosition(datasize);
            glyph = new Glyph(font, character, offset, baseline, width, height,
                    fontdata);
            cache.putFont(glyph);
        }
    }

    /**
     * Parse data defining a brush and store brush information
     *
     * @param data    Packet containing brush data
     * @param brush   Brush object in which to store the brush description
     * @param present Flags defining the information available within the packet
     */
    private static void parseBrush(RdpPacket_Localised data, Brush brush, int present) {
        if ((present & 0x01) != 0)
            brush.setXOrigin(data.get8());
        if ((present & 0x02) != 0)
            brush.setXOrigin(data.get8());
        if ((present & 0x04) != 0)
            brush.setStyle(data.get8());
        byte[] pat = brush.getPattern();
        if ((present & 0x08) != 0)
            pat[0] = (byte) data.get8();
        if ((present & 0x10) != 0)
            for (int i = 1; i < 8; i++)
                pat[i] = (byte) data.get8();
        brush.setPattern(pat);
    }

    /**
     * Parse a description for a bounding box
     *
     * @param data   Packet containing order defining bounding box
     * @param bounds BoundsOrder object in which to store description of bounds
     * @throws OrderException
     */
    private static void parseBounds(RdpPacket_Localised data, BoundsOrder bounds)
            throws OrderException {

        int present = data.get8();

        if ((present & 1) != 0) {
            bounds.setLeft(setCoordinate(data, bounds.getLeft(), false));
        } else if ((present & 16) != 0) {
            bounds.setLeft(setCoordinate(data, bounds.getLeft(), true));
        }

        if ((present & 2) != 0) {
            bounds.setTop(setCoordinate(data, bounds.getTop(), false));
        } else if ((present & 32) != 0) {
            bounds.setTop(setCoordinate(data, bounds.getTop(), true));
        }

        if ((present & 4) != 0) {
            bounds.setRight(setCoordinate(data, bounds.getRight(), false));
        } else if ((present & 64) != 0) {
            bounds.setRight(setCoordinate(data, bounds.getRight(), true));
        }

        if ((present & 8) != 0) {
            bounds.setBottom(setCoordinate(data, bounds.getBottom(), false));
        } else if ((present & 128) != 0) {
            bounds.setBottom(setCoordinate(data, bounds.getBottom(), true));
        }

        if (data.getPosition() > data.getEnd()) {
            throw new OrderException("Too far!");
        }
    }

    /* Process a bitmap cache v2 order */

    /**
     * Retrieve a coordinate from a packet and return as an absolute integer
     * coordinate
     *
     * @param data       Packet containing coordinate at current read position
     * @param coordinate Offset coordinate
     * @param delta      True if coordinate being read should be taken as relative to
     *                   offset coordinate, false if absolute
     * @return Integer value of coordinate stored in packet, in absolute form
     */
    private static int setCoordinate(RdpPacket_Localised data, int coordinate,
                                     boolean delta) {

        if (delta) {
            byte change = (byte) data.get8();
            coordinate += change;
		} else {
            coordinate = data.getLittleEndian16();
		}
		return coordinate;
	}

    /**
     * Read a colour value from a packet
     *
     * @param data Packet containing colour value at current read position
     * @return Integer colour value read from packet
     */
    private static int setColor(RdpPacket_Localised data) {

        int i = data.get8();
        int color = i;
        i = data.get8(); 
        color |= i << 8; 
        i = data.get8(); 
        color |= i << 16; 

        
        
        return color;
    }

    /**
     * Set current cache
     *
     * @param cache Cache object to set as current global cache
     */
    public static void registerCache(Cache cache) {
        Orders.cache = cache;
    }

    /**
     * Parse a pen definition
     *
     * @param data    Packet containing pen description at current read position
     * @param pen     Pen object in which to store pen description
     * @param present Flags defining information available within packet
     * @return True if successful
     */
    private static boolean parsePen(RdpPacket_Localised data, Pen pen,
                                    int present) {
        if ((present & 0x01) != 0)
            pen.setStyle(data.get8());
        if ((present & 0x02) != 0)
            pen.setWidth(data.get8());
        if ((present & 0x04) != 0)
            pen.setColor(setColor(data));

        return true; 
    }

    /**
     * Interpret an integer as a 16-bit two's complement number, based on its
     * binary representation
     *
     * @param val Integer interpretation of binary number
     * @return 16-bit two's complement value of input
     */
    private static int twosComplement16(int val) {
        return ((val & 0x8000) != 0) ? -((~val & 0xFFFF) + 1) : val;
    }

    public void resetOrderState() {
        this.os.reset();
        os.setOrderType(RDP_ORDER_PATBLT);
        return;
    }

    /**
     * Process a set of orders sent by the server
     *
     * @param data        Packet packet containing orders
     * @param next_packet Offset of end of this packet (start of next)
     * @param n_orders    Number of orders sent in this packet
     * @throws OrderException
     * @throws RdesktopException
     */
    public void processOrders(RdpPacket_Localised data, int next_packet,
                              int n_orders) throws OrderException, RdesktopException {

        int present = 0;
        
        int order_flags = 0, order_type = 0;
        int size = 0, processed = 0;

        while (processed < n_orders) {

            order_flags = data.get8();

            if ((order_flags & RDP_ORDER_STANDARD) == 0) {
                throw new OrderException("Order parsing failed!");
            }

            if ((order_flags & RDP_ORDER_SECONDARY) != 0) {
                Orders.processSecondaryOrders(data);
            } else {

                if ((order_flags & RDP_ORDER_CHANGE) != 0) {
                    os.setOrderType(data.get8());
                }

                size = switch (os.getOrderType()) {
                    case RDP_ORDER_TRIBLT, RDP_ORDER_TEXT2 -> 3;
                    case RDP_ORDER_PATBLT, RDP_ORDER_MEMBLT, RDP_ORDER_LINE -> 2;
                    default -> 1;
                };

                present = Orders.inPresent(data, order_flags, size);

                if ((order_flags & RDP_ORDER_BOUNDS) != 0) {

                    if ((order_flags & RDP_ORDER_LASTBOUNDS) == 0) {
                        Orders.parseBounds(data, os.getBounds());
                    }

                    surface.setClip(os.getBounds());
                }

                boolean delta = ((order_flags & RDP_ORDER_DELTA) != 0);

                switch (os.getOrderType()) {
                    case RDP_ORDER_DESTBLT -> {
                        logger.debug("DestBlt Order");
                        this.processDestBlt(data, os.getDestBlt(), present, delta);
                    }
                    case RDP_ORDER_PATBLT -> {
                        logger.debug("PatBlt Order");
                        this.processPatBlt(data, os.getPatBlt(), present, delta);
                    }
                    case RDP_ORDER_SCREENBLT -> {
                        logger.debug("ScreenBlt Order");
                        this.processScreenBlt(data, os.getScreenBlt(), present,
                                delta);
                    }
                    case RDP_ORDER_LINE -> {
                        logger.debug("Line Order");
                        this.processLine(data, os.getLine(), present, delta);
                    }
                    case RDP_ORDER_RECT -> {
                        logger.debug("Rectangle Order");
                        this.processRectangle(data, os.getRectangle(), present,
                                delta);
                    }
                    case RDP_ORDER_DESKSAVE -> {
                        logger.debug("Desksave!");
                        this
                                .processDeskSave(data, os.getDeskSave(), present,
                                        delta);
                    }
                    case RDP_ORDER_MEMBLT -> {
                        logger.debug("MemBlt Order");
                        this.processMemBlt(data, os.getMemBlt(), present, delta);
                    }
                    case RDP_ORDER_TRIBLT -> {
                        logger.debug("TriBlt Order");
                        this.processTriBlt(data, os.getTriBlt(), present, delta);
                    }
                    case RDP_ORDER_POLYLINE -> {
                        logger.debug("Polyline Order");
                        this
                                .processPolyLine(data, os.getPolyLine(), present,
                                        delta);
                    }
                    case RDP_ORDER_TEXT2 -> {
                        logger.debug("Text2 Order");
                        this.processText2(data, os.getText2(), present, delta);
                    }
                    default -> {
                        logger.warn("Unimplemented Order type {}", order_type);
                        return;
                    }
                }

                if ((order_flags & RDP_ORDER_BOUNDS) != 0) {
                    surface.resetClip();
                    logger.debug("Reset clip");
                }
            }

            processed++;
        }
        if (data.getPosition() != next_packet) {
            throw new OrderException("End not reached!");
        }
    }

    /**
     * Register an RdesktopCanvas with this Orders object. This surface is where
     * all drawing orders will be carried out.
     *
     * @param surface Surface to register
     */
    public void registerDrawingSurface(RdesktopCanvas surface) {
        this.surface = surface;
        surface.registerCache(cache);
    }

    /**
     * Handle secondary, or caching, orders
     *
     * @param data Packet containing secondary order
     * @throws OrderException
     * @throws RdesktopException
     */
    private static void processSecondaryOrders(RdpPacket_Localised data)
            throws RdesktopException {

        int length = data.getLittleEndian16();
        int flags = data.getLittleEndian16();
        int type = data.get8();

        int next_order = data.getPosition() + length + 7;

        switch (type) {

            case RDP_ORDER_RAW_BMPCACHE:
                logger.debug("Raw BitmapCache Order");
                Orders.processRawBitmapCache(data);
                break;

            case RDP_ORDER_COLCACHE:
                logger.debug("Colorcache Order");
                Orders.processColorCache(data);
                break;

            case RDP_ORDER_BMPCACHE:
                logger.debug("Bitmapcache Order");
                Orders.processBitmapCache(data);
                break;

            case RDP_ORDER_FONTCACHE:
                logger.debug("Fontcache Order");
                Orders.processFontCache(data);
                break;

            case RDP_ORDER_RAW_BMPCACHE2:
                try {
                    Orders.process_bmpcache2(data, flags, false);
                } catch (IOException e) {
                    throw new RdesktopException(e.getMessage());
                } /* uncompressed */
                break;

            case RDP_ORDER_BMPCACHE2:
                try {
                    Orders.process_bmpcache2(data, flags, true);
                } catch (IOException e) {
                    throw new RdesktopException(e.getMessage());
                } /* compressed */
                break;

            default:
                logger.warn("Unimplemented 2ry Order type {}", type);
        }

        data.setPosition(next_order);
    }

    /**
     * Process a dest blit order, and perform blit on drawing surface
     *
     * @param data    Packet containing description of the order
     * @param destblt DestBltOrder object in which to store the blit description
     * @param present Flags defining the information available in the packet
     * @param delta   True if the coordinates of the blit destination are described
     *                as relative to the source
     */
    private void processDestBlt(RdpPacket_Localised data, DestBltOrder destblt,
                                int present, boolean delta) {
        if ((present & 0x01) != 0)
            destblt.setX(setCoordinate(data, destblt.getX(), delta));
        if ((present & 0x02) != 0)
            destblt.setY(setCoordinate(data, destblt.getY(), delta));
        if ((present & 0x04) != 0)
            destblt.setCX(setCoordinate(data, destblt.getCX(), delta));
        if ((present & 0x08) != 0)
            destblt.setCY(setCoordinate(data, destblt.getCY(), delta));
        if ((present & 0x10) != 0)
            destblt.setOpcode(ROP2_S(data.get8()));
        
        
        surface.drawDestBltOrder(destblt);
    }

    /**
     * Parse data describing a pattern blit, and perform blit on drawing surface
     *
     * @param data    Packet containing blit data
     * @param patblt  PatBltOrder object in which to store the blit description
     * @param present Flags defining the information available within the packet
     * @param delta   True if the coordinates of the blit destination are described
     *                as relative to the source
     */
    private void processPatBlt(RdpPacket_Localised data, PatBltOrder patblt,
                               int present, boolean delta) {
        if ((present & 0x01) != 0)
            patblt.setX(setCoordinate(data, patblt.getX(), delta));
        if ((present & 0x02) != 0)
            patblt.setY(setCoordinate(data, patblt.getY(), delta));
        if ((present & 0x04) != 0)
            patblt.setCX(setCoordinate(data, patblt.getCX(), delta));
        if ((present & 0x08) != 0)
            patblt.setCY(setCoordinate(data, patblt.getCY(), delta));
        if ((present & 0x10) != 0)
            patblt.setOpcode(ROP2_P(data.get8()));
        if ((present & 0x20) != 0)
            patblt.setBackgroundColor(setColor(data));
        if ((present & 0x40) != 0)
            patblt.setForegroundColor(setColor(data));
        parseBrush(data, patblt.getBrush(), present >> 7);
        
        surface.drawPatBltOrder(patblt);
    }

    /**
     * Parse data describing a screen blit, and perform blit on drawing surface
     *
     * @param data      Packet containing blit data
     * @param screenblt ScreenBltOrder object in which to store blit description
     * @param present   Flags defining the information available within the packet
     * @param delta     True if the coordinates of the blit destination are described
     *                  as relative to the source
     */
    private void processScreenBlt(RdpPacket_Localised data,
                                  ScreenBltOrder screenblt, int present, boolean delta) {
        if ((present & 0x01) != 0)
            screenblt.setX(setCoordinate(data, screenblt.getX(), delta));
        if ((present & 0x02) != 0)
            screenblt.setY(setCoordinate(data, screenblt.getY(), delta));
        if ((present & 0x04) != 0)
            screenblt.setCX(setCoordinate(data, screenblt.getCX(), delta));
        if ((present & 0x08) != 0)
            screenblt.setCY(setCoordinate(data, screenblt.getCY(), delta));
        if ((present & 0x10) != 0)
            screenblt.setOpcode(ROP2_S(data.get8()));
        if ((present & 0x20) != 0)
            screenblt.setSrcX(setCoordinate(data, screenblt.getSrcX(), delta));
        if ((present & 0x40) != 0)
            screenblt.setSrcY(setCoordinate(data, screenblt.getSrcY(), delta));
        
        
        surface.drawScreenBltOrder(screenblt);
    }

    /**
     * Parse data describing a line order, and draw line on drawing surface
     *
     * @param data    Packet containing line order data
     * @param line    LineOrder object describing the line drawing operation
     * @param present Flags defining the information available within the packet
     * @param delta   True if the coordinates of the end of the line are defined as
     *                relative to the start
     */
    private void processLine(RdpPacket_Localised data, LineOrder line,
                             int present, boolean delta) {
        if ((present & 0x01) != 0)
            line.setMixmode(data.getLittleEndian16());
        if ((present & 0x02) != 0)
            line.setStartX(setCoordinate(data, line.getStartX(), delta));
        if ((present & 0x04) != 0)
            line.setStartY(setCoordinate(data, line.getStartY(), delta));
        if ((present & 0x08) != 0)
            line.setEndX(setCoordinate(data, line.getEndX(), delta));
        if ((present & 0x10) != 0)
            line.setEndY(setCoordinate(data, line.getEndY(), delta));
        if ((present & 0x20) != 0)
            line.setBackgroundColor(setColor(data));
        if ((present & 0x40) != 0)
            line.setOpcode(data.get8());

        parsePen(data, line.getPen(), present >> 7);

        
        
        

        if (line.getOpcode() < 0x01 || line.getOpcode() > 0x10) {
            logger.warn("bad ROP2 0x{}", line.getOpcode());
            return;
        }

        
        surface.drawLineOrder(line);
    }

    /**
     * Parse data describing a rectangle order, and draw the rectangle to the
     * drawing surface
     *
     * @param data    Packet containing rectangle order
     * @param rect    RectangleOrder object in which to store order description
     * @param present Flags defining information available in packet
     * @param delta   True if the rectangle is described as (x,y,width,height), as
     *                opposed to (x1,y1,x2,y2)
     */
    private void processRectangle(RdpPacket_Localised data,
                                  RectangleOrder rect, int present, boolean delta) {
        if ((present & 0x01) != 0)
            rect.setX(setCoordinate(data, rect.getX(), delta));
        if ((present & 0x02) != 0)
            rect.setY(setCoordinate(data, rect.getY(), delta));
        if ((present & 0x04) != 0)
            rect.setCX(setCoordinate(data, rect.getCX(), delta));
        if ((present & 0x08) != 0)
            rect.setCY(setCoordinate(data, rect.getCY(), delta));
        if ((present & 0x10) != 0)
            this.rect_colour = (this.rect_colour & 0xffffff00) | data.get8(); 
        if ((present & 0x20) != 0)
            this.rect_colour = (this.rect_colour & 0xffff00ff)
                    | (data.get8() << 8); 
        if ((present & 0x40) != 0)
            this.rect_colour = (this.rect_colour & 0xff00ffff)
                    | (data.get8() << 16);

        rect.setColor(this.rect_colour);
        surface.drawRectangleOrder(rect);
    }

    /**
     * Parse data describing a desktop save order, either saving the desktop to
     * cache, or drawing a section to screen
     *
     * @param data     Packet containing desktop save order
     * @param desksave DeskSaveOrder object in which to store order description
     * @param present  Flags defining information available within the packet
     * @param delta    True if destination coordinates are described as relative to
     *                 the source
     * @throws RdesktopException
     */
    private void processDeskSave(RdpPacket_Localised data,
                                 DeskSaveOrder desksave, int present, boolean delta)
            throws RdesktopException {

        if ((present & 0x01) != 0) {
            desksave.setOffset(data.getLittleEndian32());
        }

        if ((present & 0x02) != 0) {
            desksave.setLeft(setCoordinate(data, desksave.getLeft(), delta));
        }

        if ((present & 0x04) != 0) {
            desksave.setTop(setCoordinate(data, desksave.getTop(), delta));
        }

        if ((present & 0x08) != 0) {
            desksave.setRight(setCoordinate(data, desksave.getRight(), delta));
        }

        if ((present & 0x10) != 0) {
            desksave
                    .setBottom(setCoordinate(data, desksave.getBottom(), delta));
        }

        if ((present & 0x20) != 0) {
            desksave.setAction(data.get8());
        }

        int width = desksave.getRight() - desksave.getLeft() + 1;
        int height = desksave.getBottom() - desksave.getTop() + 1;

        if (desksave.getAction() == 0) {
            int[] pixel = surface.getImage(desksave.getLeft(), desksave
                    .getTop(), width, height);
            cache.putDesktop(desksave.getOffset(), width, height, pixel);
        } else {
            int[] pixel = cache.getDesktopInt(desksave.getOffset(),
                    width, height);
            surface.putImage(desksave.getLeft(), desksave.getTop(), width,
                    height, pixel);
        }
    }

    /**
     * Process data describing a memory blit, and perform blit on drawing
     * surface
     *
     * @param data    Packet containing mem blit order
     * @param memblt  MemBltOrder object in which to store description of blit
     * @param present Flags defining information available in packet
     * @param delta   True if destination coordinates are described as relative to
     *                the source
     */
    private void processMemBlt(RdpPacket_Localised data, MemBltOrder memblt,
                               int present, boolean delta) {
        if ((present & 0x01) != 0) {
            memblt.setCacheID(data.get8());
            memblt.setColorTable(data.get8());
        }
        if ((present & 0x02) != 0)
            memblt.setX(setCoordinate(data, memblt.getX(), delta));
        if ((present & 0x04) != 0)
            memblt.setY(setCoordinate(data, memblt.getY(), delta));
        if ((present & 0x08) != 0)
            memblt.setCX(setCoordinate(data, memblt.getCX(), delta));
        if ((present & 0x10) != 0)
            memblt.setCY(setCoordinate(data, memblt.getCY(), delta));
        if ((present & 0x20) != 0)
            memblt.setOpcode(ROP2_S(data.get8()));
        if ((present & 0x40) != 0)
            memblt.setSrcX(setCoordinate(data, memblt.getSrcX(), delta));
        if ((present & 0x80) != 0)
            memblt.setSrcY(setCoordinate(data, memblt.getSrcY(), delta));
        if ((present & 0x0100) != 0)
            memblt.setCacheIDX(data.getLittleEndian16());

        
        
        surface.drawMemBltOrder(memblt);
    }

    /**
     * Parse data describing a tri blit order, and perform blit on drawing
     * surface
     *
     * @param data    Packet containing tri blit order
     * @param triblt  TriBltOrder object in which to store blit description
     * @param present Flags defining information available in packet
     * @param delta   True if destination coordinates are described as relative to
     *                the source
     */
    private void processTriBlt(RdpPacket_Localised data, TriBltOrder triblt,
                               int present, boolean delta) {
        if ((present & 0x01) != 0) {
            triblt.setCacheID(data.get8());
            triblt.setColorTable(data.get8());
        }
        if ((present & 0x02) != 0)
            triblt.setX(setCoordinate(data, triblt.getX(), delta));
        if ((present & 0x04) != 0)
            triblt.setY(setCoordinate(data, triblt.getY(), delta));
        if ((present & 0x08) != 0)
            triblt.setCX(setCoordinate(data, triblt.getCX(), delta));
        if ((present & 0x10) != 0)
            triblt.setCY(setCoordinate(data, triblt.getCY(), delta));
        if ((present & 0x20) != 0)
            triblt.setOpcode(ROP2_S(data.get8()));
        if ((present & 0x40) != 0)
            triblt.setSrcX(setCoordinate(data, triblt.getSrcX(), delta));
        if ((present & 0x80) != 0)
            triblt.setSrcY(setCoordinate(data, triblt.getSrcY(), delta));
        if ((present & 0x0100) != 0)
            triblt.setBackgroundColor(setColor(data));
        if ((present & 0x0200) != 0)
            triblt.setForegroundColor(setColor(data));

        parseBrush(data, triblt.getBrush(), present >> 10);

        if ((present & 0x8000) != 0)
            triblt.setCacheIDX(data.getLittleEndian16());
        if ((present & 0x10000) != 0)
            triblt.setUnknown(data.getLittleEndian16());

        surface.drawTriBltOrder(triblt);
    }

    /**
     * Parse data describing a multi-line order, and draw to registered surface
     *
     * @param data     Packet containing polyline order
     * @param polyline PolyLineOrder object in which to store order description
     * @param present  Flags defining information available in packet
     * @param delta    True if each set of coordinates is described relative to
     *                 previous setAt
     */
    private void processPolyLine(RdpPacket_Localised data,
                                 PolyLineOrder polyline, int present, boolean delta) {
        if ((present & 0x01) != 0)
            polyline.setX(setCoordinate(data, polyline.getX(), delta));
        if ((present & 0x02) != 0)
            polyline.setY(setCoordinate(data, polyline.getY(), delta));
        if ((present & 0x04) != 0)
            polyline.setOpcode(data.get8());
        if ((present & 0x10) != 0)
            polyline.setForegroundColor(setColor(data));
        if ((present & 0x20) != 0)
            polyline.setLines(data.get8());
        if ((present & 0x40) != 0) {
            int datasize = data.get8();
            polyline.setDataSize(datasize);
            byte[] databytes = new byte[datasize];
            for (int i = 0; i < datasize; i++)
                databytes[i] = (byte) data.get8();
            polyline.setData(databytes);
        }
        
        
        
        
        
        surface.drawPolyLineOrder(polyline);
    }

    /**
     * Process a text2 order and output to drawing surface
     *
     * @param data    Packet containing text2 order
     * @param text2   Text2Order object in which to store order description
     * @param present Flags defining information available in packet
     * @param delta   Unused
     * @throws RdesktopException
     */
    private void processText2(RdpPacket_Localised data, Text2Order text2,
                              int present, boolean delta) throws RdesktopException {

        if ((present & 0x000001) != 0) {
            text2.setFont(data.get8());
        }
        if ((present & 0x000002) != 0) {
            text2.setFlags(data.get8());
        }

        if ((present & 0x000004) != 0) {
            text2.setOpcode(data.get8()); 
        }

        if ((present & 0x000008) != 0) {
            text2.setMixmode(data.get8());
        }

        if ((present & 0x000010) != 0) {
            text2.setForegroundColor(setColor(data));
        }

        if ((present & 0x000020) != 0) {
            text2.setBackgroundColor(setColor(data));
        }

        if ((present & 0x000040) != 0) {
            text2.setClipLeft(data.getLittleEndian16());
        }

        if ((present & 0x000080) != 0) {
            text2.setClipTop(data.getLittleEndian16());
        }

        if ((present & 0x000100) != 0) {
            text2.setClipRight(data.getLittleEndian16());
        }

        if ((present & 0x000200) != 0) {
            text2.setClipBottom(data.getLittleEndian16());
        }

        if ((present & 0x000400) != 0) {
            text2.setBoxLeft(data.getLittleEndian16());
        }

        if ((present & 0x000800) != 0) {
            text2.setBoxTop(data.getLittleEndian16());
        }

        if ((present & 0x001000) != 0) {
            text2.setBoxRight(data.getLittleEndian16());
        }

        if ((present & 0x002000) != 0) {
            text2.setBoxBottom(data.getLittleEndian16());
        }

        /*
         * Unknown members, seen when connecting to a session that was
         * disconnected with mstsc and with wintach's spreadsheet test.
         */
        if ((present & 0x004000) != 0)
            data.incrementPosition(1);

        if ((present & 0x008000) != 0)
            data.incrementPosition(1);

        if ((present & 0x010000) != 0) {
            data.incrementPosition(1); /* guessing the length here */
            logger
                    .warn("Unknown order state member (0x010000) in text2 order.\n");
        }

        if ((present & 0x020000) != 0)
            data.incrementPosition(4);

        if ((present & 0x040000) != 0)
            data.incrementPosition(4);

        if ((present & 0x080000) != 0) {
            text2.setX(data.getLittleEndian16());
        }

        if ((present & 0x100000) != 0) {
            text2.setY(data.getLittleEndian16());
        }

        if ((present & 0x200000) != 0) {
            text2.setLength(data.get8());

            byte[] text = new byte[text2.getLength()];
            data.copyToByteArray(text, 0, data.getPosition(), text.length);
            data.incrementPosition(text.length);
            text2.setText(text);

            /*
             * if(logger.isInfoEnabled()) logger.info("X: " + text2.getX() + "
             * Y: " + text2.getY() + " Left Clip: " + text2.getClipLeft() + "
             * Top Clip: " + text2.getClipTop() + " Right Clip: " +
             * text2.getClipRight() + " Bottom Clip: " + text2.getClipBottom() + "
             * Left Box: " + text2.getBoxLeft() + " Top Box: " +
             * text2.getBoxTop() + " Right Box: " + text2.getBoxRight() + "
             * Bottom Box: " + text2.getBoxBottom() + " Foreground Color: " +
             * text2.getForegroundColor() + " Background Color: " +
             * text2.getBackgroundColor() + " Font: " + text2.getFont() + "
             * Flags: " + text2.getFlags() + " Mixmode: " + text2.getMixmode() + "
             * Unknown: " + text2.getUnknown() + " Length: " +
             * text2.getLength());
             */
        }

        this.drawText(text2, text2.getClipRight() - text2.getClipLeft(), text2
                        .getClipBottom()
                        - text2.getClipTop(), text2.getBoxRight() - text2.getBoxLeft(),
                text2.getBoxBottom() - text2.getBoxTop());

    }

    /**
     * Draw a text2 order to the drawing surface
     *
     * @param text2  Text2Order describing text to be drawn
     * @param clipcx Width of clipping area
     * @param clipcy Height of clipping area
     * @param boxcx  Width of bounding box (to draw if > 1)
     * @param boxcy  Height of bounding box (to draw if boxcx > 1)
     * @throws RdesktopException
     */
    private void drawText(Text2Order text2, int clipcx, int clipcy, int boxcx,
                          int boxcy) throws RdesktopException {
        byte[] text = text2.getText();
        DataBlob entry = null;
        Glyph glyph = null;
        int offset = 0;
        int ptext = 0;
        int length = text2.getLength();
        int x = text2.getX();
        int y = text2.getY();





















        for (int i = 0; i < length; ) {
            switch (text[ptext + i] & 0x000000ff) {
                case (0xff) -> {
                    if (i + 2 < length) {
                        byte[] data = new byte[text[ptext + i + 2] & 0x000000ff];
                        System.arraycopy(text, ptext, data, 0,
                                text[ptext + i + 2] & 0x000000ff);
                        DataBlob db = new DataBlob(
                                text[ptext + i + 2] & 0x000000ff, data);
                        cache.putText(text[ptext + i + 1] & 0x000000ff, db);
                    } else {
                        throw new RdesktopException();
                    }
                    length -= i + 3;
                    ptext = i + 3;
                    i = 0;
                }
                case (0xfe) -> {
                    entry = cache.getText(text[ptext + i + 1] & 0x000000ff);
                    if (entry != null) {
                        if ((entry.getData()[1] == 0)
                                && ((text2.getFlags() & TEXT2_IMPLICIT_X) == 0)) {
                            if ((text2.getFlags() & 0x04) != 0) {
                                y += text[ptext + i + 2] & 0x000000ff;
                            } else {
                                x += text[ptext + i + 2] & 0x000000ff;
                            }
                        }
                    }
                    i += i + 2 < length ? 3 : 2;
                    length -= i;
                    ptext = i;
                    i = 0;
                    byte[] data = entry.getData();
                    for (int j = 0; j < entry.getSize(); j++) {
                        int character = data[j] & 0x000000ff;
                        glyph = cache.getFont(text2.getFont(), character);
                        if ((text2.getFlags() & TEXT2_IMPLICIT_X) == 0) {
                            offset = data[++j] & 0x000000ff;
                            if ((offset & 0x80) != 0) {
                                int var = Orders
                                        .twosComplement16((data[j + 1] & 0xff)
                                                | ((data[j + 2] & 0xff) << 8));
                                if ((text2.getFlags() & TEXT2_VERTICAL) != 0) {
                                    y += var;
                                } else {
                                    x += var;
                                }
                                j += 2;
                            } else {
                                if ((text2.getFlags() & TEXT2_VERTICAL) != 0) {
                                    y += offset;
                                } else {
                                    x += offset;
                                }
                            }
                        }
                        if (glyph != null) {
                            int xx = x + (short) glyph.getOffset(), yy = y + (short) glyph.getBaseLine();


                            surface.drawGlyph(text2.getMixmode(), xx, yy,
                                    glyph.getWidth(), glyph.getHeight(), glyph
                                            .getFontData(), text2
                                            .getBackgroundColor(), text2
                                            .getForegroundColor());

                            if ((text2.getFlags() & TEXT2_IMPLICIT_X) != 0) {
                                x += glyph.getWidth();
                            }
                        }
                    }
                }
                default -> {
                    glyph = cache.getFont(text2.getFont(),
                            text[ptext + i] & 0x000000ff);
                    if ((text2.getFlags() & TEXT2_IMPLICIT_X) == 0) {
                        offset = text[ptext + (++i)] & 0x000000ff;
                        if ((offset & 0x80) != 0) {
                            int var = Orders
                                    .twosComplement16((text[ptext + i + 1] & 0x000000ff)
                                            | ((text[ptext + i + 2] & 0x000000ff) << 8));
                            if ((text2.getFlags() & TEXT2_VERTICAL) != 0) {


                                y += var;
                            } else {
                                x += var;
                            }
                            i += 2;
                        } else {
                            if ((text2.getFlags() & TEXT2_VERTICAL) != 0) {
                                y += offset;
                            } else {
                                x += offset;
                            }
                        }
                    }
                    if (glyph != null) {
                        surface.drawGlyph(text2.getMixmode(), x
                                        + (short) glyph.getOffset(), y
                                        + (short) glyph.getBaseLine(), glyph.getWidth(),
                                glyph.getHeight(), glyph.getFontData(), text2
                                        .getBackgroundColor(), text2
                                        .getForegroundColor());

                        if ((text2.getFlags() & TEXT2_IMPLICIT_X) != 0)
                            x += glyph.getWidth();
                    }
                    i++;
                }
            }
        }
    }
}