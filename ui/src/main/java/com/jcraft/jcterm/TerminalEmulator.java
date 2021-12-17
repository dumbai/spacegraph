/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2002,2007 ymnk, JCraft,Inc.
 *
 * Written by: ymnk<ymnk@jcaft.com>
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jcterm;

import java.io.IOException;
import java.io.InputStream;

public abstract class TerminalEmulator {
    final Terminal term;
    private final InputStream in;
    private final byte[] buf = new byte[1024];
    private final byte[] b2 = new byte[2];
    private final byte[] b1 = new byte[1];
    private int term_width = 80;
    private int term_height = 24;
    int x = 0;
    int y = 0;
    private int char_width;
    int char_height;
    private int bufs = 0;
    private int buflen = 0;
    private int region_y2;
    private int region_y1;

    TerminalEmulator(Terminal term, InputStream in) {
        this.term = term;
        this.in = in;
    }

    public abstract void start();

    public abstract byte[] getCodeENTER();

    public abstract byte[] getCodeUP();

    public abstract byte[] getCodeDOWN();

    public abstract byte[] getCodeRIGHT();

    public abstract byte[] getCodeLEFT();

    public abstract byte[] getCodeF1();

    public abstract byte[] getCodeF2();

    public abstract byte[] getCodeF3();

    public abstract byte[] getCodeF4();

    public abstract byte[] getCodeF5();

    public abstract byte[] getCodeF6();

    public abstract byte[] getCodeF7();

    public abstract byte[] getCodeF8();

    public abstract byte[] getCodeF9();

    public abstract byte[] getCodeF10();

    public abstract byte[] getCodeTAB();

    public void reset() {
        term_width = term.getColumnCount();
        term_height = term.getRowCount();
        char_width = term.getCharWidth();
        char_height = term.getCharHeight();
        region_y1 = 0;
        region_y2 = term_height-1;
    }

    byte getChar() throws IOException {
        if (buflen == 0) {
            fillBuf();
        }
        buflen--;

        

        return buf[bufs++];
    }

    private void fillBuf() throws IOException {
        buflen = bufs = 0;
        buflen = in.read(buf, bufs, buf.length - bufs);
    /*
    System.out.println("fillBuf: ");
    for(int i=0; i<buflen; i++){
    byte b=buf[i];
    System.out.print(new Character((char)b)+"["+Integer.toHexString(b&0xff)+"], ");
    }
    System.out.println("");
    */
        if (buflen <= 0) {
            buflen = 0;
            throw new IOException("fillBuf");
        }
    }

    void pushChar(byte foo) {
        
        buflen++;
        buf[--bufs] = foo;
    }

    private int getASCII(int len) throws IOException {
        
        if (buflen == 0) {
            fillBuf();
        }
        if (len > buflen)
            len = buflen;
        int foo = len;
        while (len > 0) {
            byte tmp = buf[bufs++];
            if (0x20 <= tmp) {
                buflen--;
                len--;
                continue;
            }
            bufs--;
            break;
        }
        
        return foo - len;
    }

    
    void scroll_reverse() {
        term.draw_cursor();
        term.scroll_area(0, (region_y1 - 1) * char_height, term_width * char_width,
                (region_y2 - region_y1) * char_height, 0, char_height);
        term.clear_area(x, y - char_height, term_width * char_width, y);
        term.redraw(0, 0, term_width * char_width, term_height * char_height
                - char_height);
        
        term.draw_cursor();
    }

    
    void scroll_forward() {
        term.draw_cursor();
        term.scroll_area(0, (region_y1 - 1) * char_height, term_width * char_width,
                (region_y2 - region_y1 + 1) * char_height, 0, -char_height);
        term.clear_area(0, region_y2 * char_height - char_height,
                term_width * char_width, region_y2 * char_height);
        term.redraw(0, (region_y1 - 1) * char_height, term_width * char_width, (region_y2
                - region_y1 + 1)
                * char_height);
        term.draw_cursor();
    }

    
    void save_cursor() {
        
        
    }

    
    void ena_acs() {
        
        
    }

    void exit_alt_charset_mode() {
        
        
    }

    void enter_alt_charset_mode() {
        
        
    }

    void reset_2string() {
        
        
    }

    void exit_attribute_mode() {
        
        
        term.resetAllAttributes();
    }

    void exit_standout_mode() {
        term.resetAllAttributes();
    }

    void exit_underline_mode() {
        
    }

    void enter_bold_mode() {
        term.setBold();
    }

    void enter_underline_mode() {
        term.setUnderline();
    }

    void enter_reverse_mode() {
        term.setReverse();
    }

    void change_scroll_region(int y1, int y2) {
        region_y1 = y1;
        region_y2 = y2;
    }

    void cursor_address(int r, int c) {
        term.draw_cursor();
        x = (c - 1) * char_width;
        y = r * char_height;
        
        term.setCursor(x, y);
        term.draw_cursor();
    }

    void parm_down_cursor(int lines) {
        term.draw_cursor();
        y += (lines) * char_height;
        term.setCursor(x, y);
        term.draw_cursor();
    }

    void parm_left_cursor(int chars) {
        term.draw_cursor();
        x -= (chars) * char_width;
        term.setCursor(x, y);
        term.draw_cursor();
    }

    void parm_right_cursor(int chars) {
        term.draw_cursor();
        x += (chars) * char_width;
        term.setCursor(x, y);
        term.draw_cursor();
    }

    void clr_eol() {
        term.draw_cursor();
        term.clear_area(x, y - char_height, term_width * char_width, y);
        term.redraw(x, y - char_height, (term_width) * char_width - x, char_height);
        term.draw_cursor();
    }

    void clr_bol() {
        term.draw_cursor();
        term.clear_area(0, y - char_height, x, y);
        term.redraw(0, y - char_height, x, char_height);
        term.draw_cursor();
    }

    void clr_eos() {
        term.draw_cursor();
        term.clear_area(x, y - char_height, term_width * char_width, term_height
                * char_height);
        term.redraw(x, y - char_height, term_width * char_width - x, term_height
                * char_height - y + char_height);
        term.draw_cursor();
    }

    void parm_up_cursor(int lines) {
        term.draw_cursor();
        
        
        y -= (lines) * char_height;
        term.setCursor(x, y);
        term.draw_cursor();
    }

    void bell() {
        term.beep();
    }

    void tab() {
        term.draw_cursor();
        int tab = 8;
        x = (((x / char_width) / tab + 1) * tab * char_width);
        if (x >= term_width * char_width) {
            x = 0;
            y += char_height;
        }
        term.setCursor(x, y);
        term.draw_cursor();
    }

    void carriage_return() {
        term.draw_cursor();
        x = 0;
        term.setCursor(x, y);
        term.draw_cursor();
    }

    void cursor_left() {
        term.draw_cursor();
        x -= char_width;
        if (x < 0) {
            y -= char_height;
            x = term_width * char_width - char_width;
        }
        term.setCursor(x, y);
        term.draw_cursor();
    }

    void cursor_down() {
        term.draw_cursor();
        y += char_height;
        term.setCursor(x, y);
        term.draw_cursor();

        check_region();
    }

    void draw_text() throws IOException {

        check_region();

        int rx = x;
        int ry = y;

        byte b = getChar();
        term.draw_cursor();

        int w;
        if ((b & 0x80) != 0) {
            term.clear_area(x, y - char_height, x + char_width * 2, y);
            b2[0] = b;
            b2[1] = getChar();
            term.drawString(new String(b2, 0, 2, "EUC-JP"), x, y);
            x += char_width;
            x += char_width;
            w = char_width * 2;
		} else {
            pushChar(b);
            int ww = getASCII(term_width - (char_width > 0 ? x / char_width : x));
            {
//                int char_width = this.char_width > 0 ? this.char_width : 1;
//                int char_height = this.char_height > 0 ? this.char_height : 1;
                if (ww != 0) {
                    term.clear_area(x, y - char_height, x + ww * char_width, y);
                    term.drawBytes(buf, bufs - ww, ww, x, y);
                } else {
                    ww = 1;
                    term.clear_area(x, y - char_height, x + ww * char_width, y);
                    b1[0] = getChar();
                    term.drawBytes(b1, 0, ww, x, y);
                }
            }
            x += char_width * ww;
            w = char_width * ww;
		}
        int h = char_height;
        term.redraw(rx, ry - char_height, w, h);
        term.setCursor(x, y);
        term.draw_cursor();
    }

    private void check_region() {
        if (x >= term_width * char_width) {
            
            x = 0;
            y += char_height;
            
        }

        if (y > region_y2 * char_height) {
            while (y > region_y2 * char_height) {
                y -= char_height;
            }
            term.draw_cursor();
            term.scroll_area(0, region_y1 * char_height, term_width * char_width,
                (region_y2 - region_y1) * char_height, 0, -char_height);
            term.clear_area(0, y - char_height, term_width * char_width, y);
            term.redraw(0, 0, term_width * char_width, region_y2 * char_height);
            term.setCursor(x, y);
            term.draw_cursor();
        }
    }
}