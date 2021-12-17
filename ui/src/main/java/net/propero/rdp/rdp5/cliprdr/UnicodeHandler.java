/* UnicodeHandler.java
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
import net.propero.rdp.RdpPacket_Localised;
import net.propero.rdp.Utilities_Localised;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class UnicodeHandler extends TypeHandler {

    @Override
    public boolean formatValid(int format) {
        return (format == CF_UNICODETEXT);
    }

    @Override
    public boolean mimeTypeValid(String mimeType) {
        return "text".equals(mimeType);
    }

    @Override
    public int preferredFormat() {
        return CF_UNICODETEXT;
    }

    @Override
    public void handleData(RdpPacket data, int length, ClipInterface c) {
        String thingy = "";
        for (int i = 0; i < length; i += 2) {
            int aByte = data.getLittleEndian16();
            if (aByte != 0)
                thingy += (char) (aByte);
        }
        c.copyToClipboard(new StringSelection(thingy));
        
    }

    @Override
    public String name() {
        return "CF_UNICODETEXT";
    }

    private static byte[] fromTransferable(Transferable in) {
        if (in != null) {
            String s;
            try {
                s = (String) (in.getTransferData(DataFlavor.stringFlavor));
            } catch (Exception e) {
                s = e.toString();
            }

            s = Utilities_Localised.strReplaceAll(s, "" + (char) 0x0a, ""
                    + (char) 0x0d + (char) 0x0a);
            byte[] sBytes = s.getBytes();
            int length = sBytes.length;
            int lengthBy2 = length * 2;
            RdpPacket p = new RdpPacket_Localised(lengthBy2);
            for (byte sByte : sBytes) {
                p.setLittleEndian16(sByte);
            }
            sBytes = new byte[length * 2];
            p.copyToByteArray(sBytes, 0, 0, lengthBy2);
            return sBytes;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.propero.rdp.rdp5.cliprdr.TypeHandler#send_data(java.awt.datatransfer.Transferable)
     */
    @Override
    public void send_data(Transferable in, ClipInterface c) {
        byte[] data = fromTransferable(in);
        c.send_data(data, data.length);
    }

}