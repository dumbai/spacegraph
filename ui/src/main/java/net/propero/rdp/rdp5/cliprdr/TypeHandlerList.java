/* TypeHandlerList.java
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
package net.propero.rdp.rdp5.cliprdr;

import net.propero.rdp.RdpPacket;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Iterator;

public class TypeHandlerList {

    private final ArrayList handlers = new ArrayList();

    private int count;

    public TypeHandlerList() {
        count = 0;
    }

    public void add(TypeHandler t) {
        if (t != null) {
            handlers.add(t);
            count++;
        }
    }

    public TypeHandler getHandlerForFormat(int format) {
        TypeHandler handler = null;
        for (Object handler1 : handlers) {
            handler = (TypeHandler) handler1;
            if ((handler != null) && handler.formatValid(format))
                return handler;
        }
        return null;
    }

    public TypeHandlerList getHandlersForMimeType(String mimeType) {
        TypeHandlerList outList = new TypeHandlerList();

        TypeHandler handler = null;
        for (Object handler1 : handlers) {
            handler = (TypeHandler) handler1;
            if (handler.mimeTypeValid(mimeType))
                outList.add(handler);
        }
        return outList;
    }

    public TypeHandlerList getHandlersForClipboard(DataFlavor[] dataTypes) {
        TypeHandlerList outList = new TypeHandlerList();

        TypeHandler handler = null;
        for (Object handler1 : handlers) {
            handler = (TypeHandler) handler1;
            if (handler.clipboardValid(dataTypes))
                outList.add(handler);
        }
        return outList;
    }

    public void writeTypeDefinitions(RdpPacket data) {
        TypeHandler handler = null;
        for (Object handler1 : handlers) {
            handler = (TypeHandler) handler1;
            data.setLittleEndian32(handler.preferredFormat());
            data.incrementPosition(32);
        }
    }

    public int count() {
        return count;
    }

    public TypeHandler getFirst() {
		return count > 0 ? (TypeHandler) handlers.get(0) : null;
    }

    public Iterator iterator() {
        return handlers.iterator();
    }
}
