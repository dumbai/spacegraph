/* Options.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Global static storage of user-definable options
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

import java.awt.image.DirectColorModel;

public enum Options {
    ;

    public static final int DIRECT_BITMAP_DECOMPRESSION = 0;

    public static final int BUFFEREDIMAGE_BITMAP_DECOMPRESSION = 1;

    public static final int INTEGER_BITMAP_DECOMPRESSION = 2;

    public static final int bitmap_decompression_store = INTEGER_BITMAP_DECOMPRESSION;
    public static final boolean bitmap_compression = true;
    public static final boolean map_clipboard = true;
    public static final int rdp5_performanceflags = Rdp.RDP5_NO_CURSOR_SHADOW
            | Rdp.RDP5_NO_CURSORSETTINGS | Rdp.RDP5_NO_FULLWINDOWDRAG
            | Rdp.RDP5_NO_MENUANIMATIONS /*| Rdp.RDP5_NO_THEMING*/
            | Rdp.RDP5_NO_WALLPAPER;
    
    public static boolean low_latency = true;
    public static int keylayout = 0x409; 
    public static String username = "Administrator"; 
    public static String domain = ""; 
    public static String password = ""; 
    public static String hostname = ""; 
    public static String command = ""; 
    public static String directory = ""; 
    public static String windowTitle = "properJavaRDP"; 
    public static int width = 1024; 
    public static int height = 768; 
    public static int port = 3389; 
    public static boolean fullscreen;
    public static boolean built_in_licence;
    public static boolean load_licence;
    public static boolean save_licence;
    public static String licence_path = "./";

    
    public static boolean debug_keyboard;
    public static boolean debug_hexdump;
    public static boolean enable_menu = true;
    public static boolean altkey_quiet;
    public static boolean caps_sends_up_and_down = true;
    public static boolean remap_hash = true;
    public static boolean useLockingKeyState = true;
    public static boolean use_rdp5 = true;
    public static int server_bpp = 24; 
    public static int Bpp = (server_bpp + 7) / 8; 
    
    
    public static int bpp_mask = 0xFFFFFF >> 8 * (3 - Bpp);
    public static int imgCount;
    public static int server_rdp_version;
    public static int win_button_size; /* If zero, disable single app mode */
    public static boolean persistent_bitmap_caching;
    public static boolean bitmap_caching;
    public static boolean precache_bitmaps;
    public static boolean polygon_ellipse_orders;
    public static boolean sendmotion = true;
    public static boolean orders = true;
    public static boolean encryption = true;
    public static boolean packet_encryption = true;
    public static boolean desktop_save = true;
    public static boolean grab_keyboard = true;
    public static boolean hide_decorations;
    public static boolean console_session;
    public static boolean owncolmap;
    public static boolean use_ssl;
    public static boolean save_graphics;

    /**
     * Set a new value for the server's bits per pixel
     *
     * @param server_bpp New bpp value
     */
    public static void set_bpp(int server_bpp) {
        Options.server_bpp = server_bpp;
        Options.Bpp = (server_bpp + 7) / 8;
		bpp_mask = server_bpp == 8 ? 0xFF : 0xFFFFFF;

        DirectColorModel colour_model = new DirectColorModel(24, 0xFF0000, 0x00FF00, 0x0000FF);
    }

}
