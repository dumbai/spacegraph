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

import java.io.InputStream;

public final class EmulatorVT100 extends TerminalEmulator {

    private static final byte[] ENTER = {(byte) 0x0d};
    private static final byte[] UP = {(byte) 0x1b, (byte) 0x4f, (byte) 0x41};
    private static final byte[] DOWN = {(byte) 0x1b, (byte) 0x4f, (byte) 0x42};
    private static final byte[] RIGHT = {(byte) 0x1b, (byte)/*0x5b*/0x4f, (byte) 0x43};
    private static final byte[] LEFT = {(byte) 0x1b, (byte)/*0x5b*/0x4f, (byte) 0x44};
    private static final byte[] F1 = {(byte) 0x1b, (byte) 0x4f, (byte) 'P'};
    private static final byte[] F2 = {(byte) 0x1b, (byte) 0x4f, (byte) 'Q'};
    private static final byte[] F3 = {(byte) 0x1b, (byte) 0x4f, (byte) 'R'};
    private static final byte[] F4 = {(byte) 0x1b, (byte) 0x4f, (byte) 'S'};
    private static final byte[] F5 = {(byte) 0x1b, (byte) 0x4f, (byte) 't'};
    private static final byte[] F6 = {(byte) 0x1b, (byte) 0x4f, (byte) 'u'};
    private static final byte[] F7 = {(byte) 0x1b, (byte) 0x4f, (byte) 'v'};
    private static final byte[] F8 = {(byte) 0x1b, (byte) 0x4f, (byte) 'I'};
    private static final byte[] F9 = {(byte) 0x1b, (byte) 0x4f, (byte) 'w'};
    private static final byte[] F10 = {(byte) 0x1b, (byte) 0x4f, (byte) 'x'};
    private static final byte[] tab = {(byte) 0x09};
    public EmulatorVT100(Terminal term, InputStream in) {
        super(term, in);
    }

    public void start() {
        reset();

        x = 0;
        y = char_height;

        try {
            int intargi = 0;
            int[] intarg = new int[10];
            while (true) {

                byte b = getChar();

                

                

        /*
                outputs from infocmp on RedHat8.0
        #       Reconstructed via infocmp from file: /usr/share/terminfo/v/vt100
        vt100|vt100-am|dec vt100 (w/advanced video),
                am, msgr, xenl, xon,
                cols#80, it#8, lines#24, vt#3,
        	acsc=``aaffggjjkkllmmnnooppqqrrssttuuvvwwxxyyzz{{||}}~~,
        	bel=^G, blink=\E[5m$<2>, bold=\E[1m$<2>,
        	clear=\E[H\E[J$<50>, cr=^M, csr=\E[%i%p1%d;%p2%dr,
        	cub=\E[%p1%dD, cub1=^H, cud=\E[%p1%dB, cud1=^J,
                cuf=\E[%p1%dC, cuf1=\E[C$<2>,
                cup=\E[%i%p1%d;%p2%dH$<5>, cuu=\E[%p1%dA,
                cuu1=\E[A$<2>, ed=\E[J$<50>, el=\E[K$<3>, el1=\E[1K$<3>,
                enacs=\E(B\E)0, home=\E[H, ht=^I, hts=\EH, ind=^J, ka1=\EOq,
                ka3=\EOs, kb2=\EOr, kbs=^H, kc1=\EOp, kc3=\EOn, kcub1=\EOD,
                kcud1=\EOB, kcuf1=\EOC, kcuu1=\EOA, kent=\EOM, kf0=\EOy,
                kf1=\EOP, kf10=\EOx, kf2=\EOQ, kf3=\EOR, kf4=\EOS, kf5=\EOt,
                kf6=\EOu, kf7=\EOv, kf8=\EOl, kf9=\EOw, rc=\E8,
                rev=\E[7m$<2>, ri=\EM$<5>, rmacs=^O, rmam=\E[?7l,
                rmkx=\E[?1l\E>, rmso=\E[m$<2>, rmul=\E[m$<2>,
                rs2=\E>\E[?3l\E[?4l\E[?5l\E[?7h\E[?8h, sc=\E7,
                sgr=\E[0%?%p1%p6%|%t;1%;%?%p2%t;4%;%?%p1%p3%|%t;7%;%?%p4%t;5%;m%?%p9%t\016%e\017%;$<2>,
                sgr0=\E[m\017$<2>, smacs=^N, smam=\E[?7h, smkx=\E[?1h\E=,
                smso=\E[7m$<2>, smul=\E[4m$<2>, tbc=\E[3g,
        */
        /*
                am    terminal has automatic margnins
                msgr  safe to move while in standout mode
                xenl  newline ignored after 80 cols (concept)
                xon   terminal uses xon/xoff handshake
                cols  number of columns in a line
                it    tabs initially every # spaces
                lines number of lines on screen of page
                vt    virstual terminal number(CB/unix)
                acsc  graphics charset pairs, based on vt100
                bel   bell
                blink turn on blinking
                bold  turn on bold(extra bright) mode
                clear clear screen and home cursor(P*)
                cr    carriage return (P)(P*)
                csr   change region to line #1 to line #2(P)
                cub   move #1 characters to the left (P)
                cub1  move left one space
                cud   down #1 lines (P*)
                cud1  down one line
                cuf   move to #1 characters to the right.
                cuf1  non-destructive space (move right one space)
                cup   move to row #1 columns #2
                cuu   up #1 lines (P*)
                cuu1  up one line
                ed    clear to end of screen (P*)
                el    clear to end of line (P)
                el1   Clear to begining of line
                enacs enable alterate char setAt
                home  home cursor (if no cup)
                ht    tab to next 8-space hardware tab stop
                hts   set a tab in every row, current columns
                ind   scroll text up
                ka1   upper left of keypad
                ka3   upper right of keypad
                kb2   center of keypad
                kbs   backspace key
                kc1   lower left of keypad
                kc3   lower right of keypad
                kcub1 left-arrow key
                kcud1 down-arrow key
                kcuf1 right-arrow key
                kcuu1 up-arrow key
                kent  enter/sekd key
                kf0   F0 function key
                kf1   F1 function key
                kf10  F10 function key
                kf2   F2 function key
                kf3   F3 function key
                kf4   F4 function key
                kf5   F5 function key
                kf6   F6 function key
                kf7   F7 function key
                kf8   F8 function key
                kf9   F9 function key
                rc    restore cursor to position of last save_cursor
                rev   turn on reverse video mode
                ri    scroll text down (P)
                rmacs end alternate character setAt
                rmam  turn off automatic margins
                rmkx  leave 'keybroad_transmit' mode
                rmso  exit standout mode
                rmul  exit underline mode
                rs2   reset string
                sc    save current cursor position (P)
                sgr   define video attribute #1-#9(PG9)
                sgr0  turn off all attributes
                smacs start alternate character set (P)
                smam  turn on automatic margins
                smkx  enter 'keyborad_transmit' mode
                smso  begin standout mode
                smul  begin underline mode
                tbc   clear all tab stops(P)
         */
                if (b == 0) continue;

                if (b == 0x1b) {
                    b = getChar();

                    

                    if (b == 'M') { 
                        scroll_reverse();
                        continue;
                    }

                    if (b == 'D') { 
                        scroll_forward();
                        continue;
                    }

                    if (b == '7') {
                        save_cursor();
                        continue;
                    }

                    if (b == '(') {
                        b = getChar();
                        if (b == 'B') {
                            b = getChar();
                            if (b == 0x1b) {
                                b = getChar();
                                if (b == ')') {
                                    b = getChar();
                                    if (b == '0') { 
                                        ena_acs();
                                        continue;
                                    } else pushChar((byte) '0');
                                } else pushChar((byte) ')');
                            } else pushChar((byte) 0x1b);
                        } else pushChar((byte) 'B');
                    }

                    if (b == '>') {
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 
                        b = getChar(); 

                        reset_2string();
                        continue;
                    }

                    if (b != '[') {
                        System.out.print("@11: " + (char) b + '['
                                + Integer.toHexString(b & 0xff) + ']');
                        pushChar(b);
                        continue;
                    }

                    

                    intargi = 0;
                    intarg[intargi] = 0;
                    int digit = 0;

                    while (true) {
                        b = getChar();
                        
                        if (b == ';') {
                            if (digit > 0) {
                                intargi++;
                                intarg[intargi] = 0;
                                digit = 0;
                            }
                            continue;
                        }

                        if ('0' <= b && b <= '9') {
                            intarg[intargi] = intarg[intargi] * 10 + (b - '0');
                            digit++;
                            continue;
                        }

                        pushChar(b);
                        break;
                    }

                    b = getChar();

                    

                    if (b == 'm') {
            /*
            b=getChar();
            if(b=='$'){
              b=getChar();  
              b=getChar();  
              b=getChar();  
            }
            else{
              pushChar(b);
            }
            */

                        if (digit == 0 && intargi == 0) {
                            b = getChar();
                            if (b == 0x0f) exit_attribute_mode();
                            else {
                                exit_underline_mode();
                                exit_standout_mode();
                                pushChar(b);
                            }
                            continue;
                        }

                        for (int i = 0; i <= intargi; i++) {
                            Object fg = null;
                            Object bg = null;
                            Object tmp = null;

                            switch (intarg[i]) {
                                case 0: 
                                    exit_standout_mode();
                                    continue;
                                case 1: 
                                    enter_bold_mode();
                                    continue;
                                case 4: 
                                    enter_underline_mode();
                                    continue;
                                case 7: 
                                    enter_reverse_mode();
                                    continue;
                                case 30:
                                case 31:
                                case 32:
                                case 33:
                                case 34:
                                case 35:
                                case 36:
                                case 37:
                                    tmp = term.getColor(intarg[i] - 30);
                                    if (tmp != null)
                                        fg = tmp;
                                    break;
                                case 40:
                                case 41:
                                case 42:
                                case 43:
                                case 44:
                                case 45:
                                case 46:
                                case 47:
                                    tmp = term.getColor(intarg[i] - 40);
                                    if (tmp != null)
                                        bg = tmp;
                                    break;
                                case 5:
                                case 8:
                                case 2:
                                default:
                                    break;
                            }
                            if (fg != null)
                                term.setForeGround(fg);
                            if (bg != null)
                                term.setBackGround(bg);
                        }
                        
                        continue;
                    }

                    if (b == 'r') { 
                        change_scroll_region(intarg[0], intarg[1]);
                        
                        continue;
                    }

                    if (b == 'H') { 
            /*
            b=getChar();
            if(b!='$'){      
              pushChar(b);
            }
            else{
              b=getChar();  
              b=getChar();  
              b=getChar();  
            }
            */

                        if (digit == 0 && intargi == 0) intarg[0] = intarg[1] = 1;

                        
                        cursor_address(intarg[0], intarg[1]);
                        continue;
                    }

                    if (b == 'B') { 
                        parm_down_cursor(intarg[0]);
                        continue;
                    }

                    if (b == 'D') { 
                        parm_left_cursor(intarg[0]);
                        continue;
                    }

                    if (b == 'C') { 
                        if (digit == 0 && intargi == 0) intarg[0] = 1;
                        parm_right_cursor(intarg[0]);
                        continue;
                    }

                    if (b == 'K') { 
          /*
          	  b=getChar(); 
          	  if(b=='$'){
          	    b=getChar(); 
          	    b=getChar(); 
          	    b=getChar(); 
          	  }
          	  else{
          	    pushChar(b);
          	  }
          */

                        if (digit == 0 && intargi == 0) clr_eol();
                        else clr_bol();
                        continue;
                    }

                    if (b == 'J') {
                        
                        
                        clr_eos();
                        continue;
                    }

                    if (b == 'A') { 
                        if (digit == 0 && intargi == 0) intarg[0] = 1;
                        parm_up_cursor(intarg[0]);
                        continue;
                    }

                    if (b == '?') {
                        b = getChar();
                        switch (b) {
                            case '1':
                                b = getChar();
                                if (b == 'l' || b == 'h') {
                                    b = getChar();
                                    if (b == 0x1b) {
                                        b = getChar();
                                        if (b == '>' ||
                                                b == '=') continue;
                                    }
                                } else if (b == 'h') {
                                    b = getChar();
                                    if (b == 0x1b) {
                                        b = getChar();
                                        if (b == '=') continue;
                                    }
                                }
                                break;
                            case '7':
                                b = getChar();
                                switch (b) {
                                    case 'h':
                                    case 'l':


                                        continue;
                                }
                                pushChar(b);
                                b = '7';
                                break;
                            default:
                                break;
                        }
                    }

                    if (b == 'h') continue;


                    System.out.println("unknown " + Integer.toHexString(b & 0xff) + ' '
                            + (char) b + ", " + intarg[0] + ", " + intarg[1] + ", "
                            + intarg[2] + ",intargi=" + intargi);
                    continue;
                }

                if (b == 0x07) { 
                    bell();
                    continue;
                }

                if (b == 0x09) { 
                    tab();
                    continue;
                }

                if (b == 0x0f) { 
                    exit_alt_charset_mode();
                    continue;
                }

                if (b == 0x0e) { 
                    enter_alt_charset_mode();
                    continue;
                }

                if (b == 0x0d) {
                    carriage_return();
                    continue;
                }

                if (b == 0x08) {
                    cursor_left();
                    continue;
                }

                if (b == 0x0a) { 
                    
                    cursor_down();

                } else {

                    pushChar(b);
                    draw_text();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getCodeENTER() {
        return ENTER;
    }

    public byte[] getCodeUP() {
        return UP;
    }

    public byte[] getCodeDOWN() {
        return DOWN;
    }

    public byte[] getCodeRIGHT() {
        return RIGHT;
    }

    public byte[] getCodeLEFT() {
        return LEFT;
    }

    public byte[] getCodeF1() {
        return F1;
    }

    public byte[] getCodeF2() {
        return F2;
    }

    public byte[] getCodeF3() {
        return F3;
    }

    public byte[] getCodeF4() {
        return F4;
    }

    public byte[] getCodeF5() {
        return F5;
    }

    public byte[] getCodeF6() {
        return F6;
    }

    public byte[] getCodeF7() {
        return F7;
    }

    public byte[] getCodeF8() {
        return F8;
    }

    public byte[] getCodeF9() {
        return F9;
    }

    public byte[] getCodeF10() {
        return F10;
    }

    public byte[] getCodeTAB() {
        return tab;
    }
}