/* PolyLineOrder.java
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

public class PolyLineOrder implements Order {

    private byte[] data = new byte[256];
    private int x;
    private int y;
    private int flags;
    private int fgcolor;
    private int lines;
    private int opcode;
    private int datasize;

    public PolyLineOrder() {
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getForegroundColor() {
        return this.fgcolor;
    }

    public void setForegroundColor(int fgcolor) {
        this.fgcolor = fgcolor;
    }

    public int getLines() {
        return this.lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public int getDataSize() {
        return this.datasize;
    }

    public void setDataSize(int datasize) {
        this.datasize = datasize;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public void reset() {
        x = 0;
        y = 0;
        flags = 0;
        fgcolor = 0;
        lines = 0;
        datasize = 0;
        opcode = 0;
        data = new byte[256];
    }
}
