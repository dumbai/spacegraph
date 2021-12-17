/* RdpPacket_Localised.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Java 1.4 specific extension of RdpPacket class
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RdpPacket_Localised extends RdpPacket {

    ByteBuffer bb;

    private int size;

    public RdpPacket_Localised(int capacity) {
        bb = ByteBuffer.allocate(capacity);
        size = capacity;
    }

    public void reset(int length) {
        
        
        this.end = 0;
        this.start = 0;
        if (bb.capacity() < length)
            bb = ByteBuffer.allocateDirect(length);
        size = length;
        bb.clear();
    }

    @Override
    public void set8(int where, int what) {
        if (where < 0 || where >= bb.capacity()) {
            throw new ArrayIndexOutOfBoundsException(
                    "memory accessed out of Range!");
        }
        bb.put(where, (byte) what);
    }

    
    @Override
    public int get8(int where) {
        if (where < 0 || where >= bb.capacity()) {
            throw new ArrayIndexOutOfBoundsException(
                    "memory accessed out of Range!");
        }
        return bb.get(where) & 0xff; 
    }

    
    @Override
    public int get8() {
        if (bb.position() >= bb.capacity()) {
            throw new ArrayIndexOutOfBoundsException(
                    "memory accessed out of Range!");
        }
        return bb.get() & 0xff; 
    }

    @Override
    public void set8(int what) {
        if (bb.position() >= bb.capacity()) {
            throw new ArrayIndexOutOfBoundsException(
                    "memory accessed out of Range!");
        }
        bb.put((byte) what);
    }

    @Override
    public void copyFromByteArray(byte[] array, int array_offset,
                                  int mem_offset, int len) {
        if ((array_offset >= array.length)
                || (array_offset + len > array.length)
                || (mem_offset + len > bb.capacity())) {
            throw new ArrayIndexOutOfBoundsException(
                    "memory accessed out of Range!");
        }
        
        int oldpos = getPosition();

        setPosition(mem_offset);
        bb.put(array, array_offset, len);

        
        setPosition(oldpos);
    }

    @Override
    public void copyToByteArray(byte[] array, int array_offset, int mem_offset,
                                int len) {
        if ((array_offset >= array.length))
            throw new ArrayIndexOutOfBoundsException(
                    "Array offset beyond end of array!");
        if (array_offset + len > array.length)
            throw new ArrayIndexOutOfBoundsException(
                    "Not enough bytes in array to copy!");
        if (mem_offset + len > bb.capacity())
            throw new ArrayIndexOutOfBoundsException(
                    "Memory accessed out of Range!");

        int oldpos = getPosition();
        setPosition(mem_offset);
        bb.get(array, array_offset, len);
        setPosition(oldpos);
    }

    @Override
    public void copyToPacket(RdpPacket_Localised dst, int srcOffset,
                             int dstOffset, int len) {
        int olddstpos = dst.getPosition();
        int oldpos = getPosition();
        dst.setPosition(dstOffset);
        setPosition(srcOffset);
        for (int i = 0; i < len; i++)
            dst.set8(bb.get());
        dst.setPosition(olddstpos);
        setPosition(oldpos);
    }

    @Override
    public void copyFromPacket(RdpPacket_Localised src, int srcOffset,
                               int dstOffset, int len) {
        int oldsrcpos = src.getPosition();
        int oldpos = getPosition();
        src.setPosition(srcOffset);
        setPosition(dstOffset);
        for (int i = 0; i < len; i++)
            bb.put((byte) src.get8());
        src.setPosition(oldsrcpos);
        setPosition(oldpos);
    }

    @Override
    public int capacity() {
        return bb.capacity();
    }

    
    @Override
    public int size() {
        return size;
        
    }

    @Override
    public int getPosition() {
        return bb.position();
    }

    @Override
    public void setPosition(int position) {
        if (position > bb.capacity() || position < 0) {
            logger.warn("stream position ={} end ={} capacity ={}", getPosition(), getEnd(), capacity());
            logger.warn("setPosition({}) failed", position);
            throw new ArrayIndexOutOfBoundsException();
        }
        bb.position(position);
    }

    @Override
    public int getLittleEndian16(int where) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort(where);
    }

    @Override
    public int getLittleEndian16() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort();
    }

    @Override
    public void setLittleEndian16(int what) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) what);
    }

    @Override
    public int getBigEndian16(int where) {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getShort(where);
    }

    @Override
    public int getBigEndian16() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getShort();
    }

    @Override
    public void setBigEndian16(int what) {
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort((short) what);
    }

    @Override
    public void setLittleEndian16(int where, int what) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(where, (short) what);
    }

    @Override
    public void setBigEndian16(int where, int what) {
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort(where, (short) what);
    }

    @Override
    public int getLittleEndian32(int where) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt(where);
    }

    @Override
    public int getLittleEndian32() {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    @Override
    public void setLittleEndian32(int what) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(what);
    }

    @Override
    public int getBigEndian32(int where) {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt(where);
    }

    @Override
    public int getBigEndian32() {
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }

    @Override
    public void setBigEndian32(int what) {
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(what);
    }

    @Override
    public void setLittleEndian32(int where, int what) {
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(where, what);
    }

    @Override
    public void setBigEndian32(int where, int what) {
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(where, what);
    }

    @Override
    public void incrementPosition(int length) {

        if (length > bb.capacity() || length + bb.position() > bb.capacity()
                || length < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        bb.position(bb.position() + length);
    }

}
