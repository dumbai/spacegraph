/* TextHandler.java
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
import net.propero.rdp.Utilities_Localised;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class TextHandler extends TypeHandler {

    public static Transferable handleData(RdpPacket data, int length) {
        String thingy = "";
        for (int i = 0; i < length; i++) {
            int aByte = data.get8();
            if (aByte != 0)
                thingy += (char) (aByte & 0xFF);
        }
        return (new StringSelection(thingy));
    }

    @Override
    public boolean formatValid(int format) {
        return (format == CF_TEXT);
    }

    @Override
    public boolean mimeTypeValid(String mimeType) {
        return "text".equals(mimeType);
    }

    @Override
    public int preferredFormat() {
        return CF_TEXT;
    }

    @Override
    public String name() {
        return "CF_TEXT";
    }

    public static byte[] fromTransferable(Transferable in) {
        if (in != null) {
            String s;
            try {
                s = (String) (in.getTransferData(DataFlavor.stringFlavor));
            } catch (Exception e) {
                s = e.toString();
            }

            
            s = s.replace('\n', (char) 0x0a);
            
            
            s = Utilities_Localised.strReplaceAll(s, "" + (char) 0x0a, ""
                    + (char) 0x0d + (char) 0x0a);
            return s.getBytes();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.propero.rdp.rdp5.cliprdr.TypeHandler#handleData(net.propero.rdp.RdpPacket,
     *      int, net.propero.rdp.rdp5.cliprdr.ClipInterface)
     */
    @Override
    public void handleData(RdpPacket data, int length, ClipInterface c) {
        String thingy = "";
        for (int i = 0; i < length; i++) {
            int aByte = data.get8();
            if (aByte != 0)
                thingy += (char) (aByte & 0xFF);
        }
        c.copyToClipboard(new StringSelection(thingy));
    }

    /*
     * (non-Javadoc)
     *
     * @see net.propero.rdp.rdp5.cliprdr.TypeHandler#send_data(java.awt.datatransfer.Transferable,
     *      net.propero.rdp.rdp5.cliprdr.ClipInterface)
     */
    @Override
    public void send_data(Transferable in, ClipInterface c) {
        if (in != null) {
            String s;
            try {
                s = (String) (in.getTransferData(DataFlavor.stringFlavor));
            } catch (Exception e) {
                s = e.toString();
            }

            
            s = s.replace('\n', (char) 0x0a);
            
            
            s = Utilities_Localised.strReplaceAll(s, "" + (char) 0x0a, ""
                    + (char) 0x0d + (char) 0x0a);

            
            c.send_data(s.getBytes(), s.length());
        }
    }

}
