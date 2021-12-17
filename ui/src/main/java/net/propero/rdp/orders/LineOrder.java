/* LineOrder.java
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
package net.propero.rdp.orders;

public class LineOrder implements Order {

    private final Pen pen;
    private int mixmode;
    private int startx;
    private int starty;
    private int endx;
    private int endy;
    private int bgcolor;
    private int opcode;

    public LineOrder() {
        pen = new Pen();
    }

    public int getMixmode() {
        return this.mixmode;
    }

    public void setMixmode(int mixmode) {
        this.mixmode = mixmode;
    }

    public int getStartX() {
        return this.startx;
    }

    public void setStartX(int startx) {
        this.startx = startx;
    }

    public int getStartY() {
        return this.starty;
    }

    public void setStartY(int starty) {
        this.starty = starty;
    }

    public int getEndX() {
        return this.endx;
    }

    public void setEndX(int endx) {
        this.endx = endx;
    }

    public int getEndY() {
        return this.endy;
    }

    public void setEndY(int endy) {
        this.endy = endy;
    }

    public int getBackgroundColor() {
        return this.bgcolor;
    }

    public void setBackgroundColor(int bgcolor) {
        this.bgcolor = bgcolor;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public Pen getPen() {
        return this.pen;
    }

    public void reset() {
        mixmode = 0;
        startx = 0;
        starty = 0;
        endx = 0;
        endy = 0;
        bgcolor = 0;
        opcode = 0;
        pen.reset();
    }
}
