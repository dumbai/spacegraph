/* PstCache.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Handle persistent caching
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

enum PstCache {
	;

	private static final int MAX_CELL_SIZE = 0x1000; /* pixels */
    private static final Logger logger = LoggerFactory.getLogger(PstCache.class);
    private static final File[] g_pstcache_fd = new File[8];
    static int g_stamp;
    private static int g_pstcache_Bpp;
    private static boolean g_pstcache_enumerated;

    static boolean IS_PERSISTENT(int id) {
        return (id < 8 && g_pstcache_fd[id] != null);
    }

    /* Update usage info for a bitmap */
    static void touchBitmap(int cache_id, int cache_idx, int stamp) {
        logger.info("PstCache.touchBitmap");

        if (!IS_PERSISTENT(cache_id) || cache_idx >= Rdp.BMPCACHE2_NUM_PSTCELLS)
            return;

        try {
            FileOutputStream fd = new FileOutputStream(g_pstcache_fd[cache_id]);

            fd.write(toBigEndian32(stamp), 12 + cache_idx
                    * (g_pstcache_Bpp * MAX_CELL_SIZE + CELLHEADER.size()), 4);
            
            
            
            
            

        } catch (IOException e) {
            return;
        }
    }

    private static byte[] toBigEndian32(int value) {
        byte[] out = new byte[4];
        out[0] = (byte) (value & 0xFF);
        out[1] = (byte) (value & 0xFF00);
        out[2] = (byte) (value & 0xFF0000);
        out[3] = (byte) (value & 0xFF000000);
        return out;
    }

    /* Load a bitmap from the persistent cache */
    private static boolean pstcache_load_bitmap(int cache_id, int cache_idx)
            throws IOException, RdesktopException {
        logger.info("PstCache.pstcache_load_bitmap");

        if (!Options.persistent_bitmap_caching)
            return false;

        if (!IS_PERSISTENT(cache_id) || cache_idx >= Rdp.BMPCACHE2_NUM_PSTCELLS)
            return false;

        FileInputStream fd = new FileInputStream(g_pstcache_fd[cache_id]);
        int offset = cache_idx
                * (g_pstcache_Bpp * MAX_CELL_SIZE + CELLHEADER.size());
        byte[] cellHead = null;
        fd.read(cellHead, offset, CELLHEADER.size());
        CELLHEADER c = new CELLHEADER(cellHead);


        byte[] celldata = new byte[c.length];
        fd.read(celldata);
        logger.debug("Loading bitmap from disk ({}" + ':' + "{})\n", cache_id, cache_idx);

        Bitmap bitmap = new Bitmap(celldata, c.width, c.height, 0, 0, Options.Bpp);
        
        Orders.cache.putBitmap(cache_id, cache_idx, bitmap, c.stamp);

        
        return true;
    }

    /* Store a bitmap in the persistent cache */
    static boolean pstcache_put_bitmap(int cache_id, int cache_idx,
                                       byte[] bitmap_id, int width, int height, int length, byte[] data)
            throws IOException {
        logger.info("PstCache.pstcache_put_bitmap");
        CELLHEADER cellhdr = new CELLHEADER();

        if (!IS_PERSISTENT(cache_id) || cache_idx >= Rdp.BMPCACHE2_NUM_PSTCELLS)
            return false;

        cellhdr.bitmap_id = bitmap_id;
        

        cellhdr.width = width;
        cellhdr.height = height;
        cellhdr.length = length;
        cellhdr.stamp = 0;

        FileOutputStream fd = new FileOutputStream(g_pstcache_fd[cache_id]);
        int offset = cache_idx
                * (Options.Bpp * MAX_CELL_SIZE + CELLHEADER.size());
        fd.write(CELLHEADER.toBytes(), offset, CELLHEADER.size());
        fd.write(data);
        
        
        
        
        return true;
    }

    /* list the bitmaps from the persistent cache file */
    static int pstcache_enumerate(int cache_id, int[] idlist)
            throws IOException, RdesktopException {
        logger.info("PstCache.pstcache_enumerate");

        if (!(Options.bitmap_caching && Options.persistent_bitmap_caching && IS_PERSISTENT(cache_id)))
            return 0;

        /*
         * The server disconnects if the bitmap cache content is sent more than
         * once
         */
        if (g_pstcache_enumerated)
            return 0;

        logger.debug("pstcache enumeration... ");
        CELLHEADER cellhdr = null;
        int c = 0;
        int n;
        for (n = 0; n < Rdp.BMPCACHE2_NUM_PSTCELLS; n++) {
            FileInputStream fd = new FileInputStream(g_pstcache_fd[cache_id]);

            byte[] cellhead_data = new byte[CELLHEADER.size()];
            if (fd.read(cellhead_data, n
                            * (g_pstcache_Bpp * MAX_CELL_SIZE + CELLHEADER.size()),
                    CELLHEADER.size()) <= 0)
                break;

            cellhdr = new CELLHEADER(cellhead_data);

            int result = 0;
            for (int i = 0; i < cellhdr.bitmap_id.length; i++) {
                result += cellhdr.bitmap_id[i];
            }

            if (result != 0) {
                for (int i = 0; i < 8; i++) {
                    idlist[(n * 8) + i] = cellhdr.bitmap_id[i];
                }

                if (cellhdr.stamp != 0) {
                    /*
                     * Pre-caching is not possible with 8bpp because a colourmap
                     * is needed to load them
                     */
                    if (Options.precache_bitmaps && (Options.server_bpp > 8)) {
                        if (pstcache_load_bitmap(cache_id, n))
                            c++;
                    }

                    g_stamp = Math.max(g_stamp, cellhdr.stamp);
                }
            } else {
                break;
            }
        }

        logger.info("{} bitmaps in persistent cache, {} bitmaps loaded in memory\n", n, c);
        g_pstcache_enumerated = true;
        return n;
    }

    /* initialise the persistent bitmap cache */
    static boolean pstcache_init(int cache_id) {

        if (g_pstcache_enumerated)
            return true;

        g_pstcache_fd[cache_id] = null;

        if (!(Options.bitmap_caching && Options.persistent_bitmap_caching))
            return false;

        g_pstcache_Bpp = Options.Bpp;
        String filename = "./cache/pstcache_" + cache_id + '_' + g_pstcache_Bpp;
        logger.debug("persistent bitmap cache file: {}", filename);

        File cacheDir = new File("./cache/");
        if (!cacheDir.exists() && !cacheDir.mkdir()) {
            logger.warn("failed to get/make cache directory");
            return false;
        }

        File f = new File(filename);

        try {
            if (!f.exists() && !f.createNewFile()) {
                logger.warn("Could not create cache file");
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        /*
         * if (!rd_lock_file(fd, 0, 0)) { logger.warn("Persistent bitmap caching
         * is disabled. (The file is already in use)\n"); rd_close_file(fd);
         * return false; }
         */

        g_pstcache_fd[cache_id] = f;
        return true;
    }

}

/* Header for an entry in the persistent bitmap cache file */
class CELLHEADER {
    byte[] bitmap_id = new byte[8]; 

    int width;
    int height;

    int length; 

    int stamp; 

    CELLHEADER() {

    }

    CELLHEADER(byte[] data) {
        System.arraycopy(data, 0, bitmap_id, 0, bitmap_id.length);

        width = data[bitmap_id.length];
        height = data[bitmap_id.length + 1];
        length = (data[bitmap_id.length + 2] >> 8) + data[bitmap_id.length + 3];
        stamp = (data[bitmap_id.length + 6] >> 24)
                + (data[bitmap_id.length + 6] >> 16)
                + (data[bitmap_id.length + 6] >> 8)
                + data[bitmap_id.length + 7];
    }

    static int size() {
        return 8 * 8 + 8 * 2 + 16 + 32;
    }

    public static byte[] toBytes() {
        return null;
    }
}