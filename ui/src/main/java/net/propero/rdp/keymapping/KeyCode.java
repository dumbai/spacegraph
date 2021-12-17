/* KeyCode.java
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
package net.propero.rdp.keymapping;

public class KeyCode {
    /**
     * X scancodes for the printable keys of a standard 102 key MF-II Keyboard
     */
    public static final int SCANCODE_EXTENDED = 0x80;
    private static final String[] main_key_US = {"`~", "1!", "2@", "3#", "4$",
            "5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "qQ", "wW", "eE",
            "rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{", "]}", "aA", "sS",
            "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:", "''\"", 
            
            "\\|", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?"};
    /**
     * United States keyboard layout (phantom key version)
     */
    /* (XFree86 reports the <> key even if it's not physically there) */
    private static final String[] main_key_US_phantom = {"`~", "1!", "2@",
            "3#", "4$", "5%", "6^", "7&", "8*", "9(", "0)", "-_", "=+", "qQ",
            "wW", "eE", "rR", "tT", "yY", "uU", "iI", "oO", "pP", "[{", "]}",
            "aA", "sS", "dD", "fF", "gG", "hH", "jJ", "kK", "lL", ";:", "'\"",
            "\\|", "zZ", "xX", "cC", "vV", "bB", "nN", "mM", ",<", ".>", "/?",
            "<>" /* the phantom key */
    };
    private final int[] main_key_scan_qwerty = {0x29, 0x02, 0x03, 0x04, 0x05,
            0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x10, 0x11, 0x12,
            0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1E, 0x1F,
            0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x2B, 0x2C,
            0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x56};



































































































































































































































































}
