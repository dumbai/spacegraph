/* ClipChannel.java
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

import net.propero.rdp.*;
import net.propero.rdp.crypto.CryptoException;
import net.propero.rdp.rdp5.VChannel;
import net.propero.rdp.rdp5.VChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Arrays;

public class ClipChannel extends VChannel implements ClipInterface,
        ClipboardOwner, FocusListener {

    
    private static final int CLIPRDR_CONNECT = 1;
    private static final int CLIPRDR_FORMAT_ANNOUNCE = 2;
    private static final int CLIPRDR_FORMAT_ACK = 3;
    private static final int CLIPRDR_DATA_REQUEST = 4;
    public static final int CLIPRDR_DATA_RESPONSE = 5;
    
    private static final int CLIPRDR_REQUEST = 0;
    private static final int CLIPRDR_RESPONSE = 1;
    public static final int CLIPRDR_ERROR = 2;
    private static final Logger logger = LoggerFactory.getLogger(ClipChannel.class);
    private final Clipboard clipboard;
    
    private final TypeHandlerList allHandlers;
    String[] types = {"unused", "CF_TEXT", "CF_BITMAP", "CF_METAFILEPICT",
            "CF_SYLK", "CF_DIF", "CF_TIFF", "CF_OEMTEXT", "CF_DIB",
            "CF_PALETTE", "CF_PENDATA", "CF_RIFF", "CF_WAVE", "CF_UNICODETEXT",
            "CF_ENHMETAFILE", "CF_HDROP", "CF_LOCALE", "CF_MAX"};
    
    private TypeHandler currentHandler;
    byte[] localClipData;

    public ClipChannel() {
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        
        allHandlers = new TypeHandlerList();
        allHandlers.add(new UnicodeHandler());
        allHandlers.add(new TextHandler());
        allHandlers.add(new DIBHandler());
        
    }

    /*
     * Support methods
     */
    private static void reset_bool(boolean[] x) {
        Arrays.fill(x, false);
    }

    /*
     * VChannel inherited abstract methods
     */
    @Override
    public String name() {
        return "cliprdr";
    }

    @Override
    public int flags() {
        return VChannels.CHANNEL_OPTION_INITIALIZED
                | VChannels.CHANNEL_OPTION_ENCRYPT_RDP
                | VChannels.CHANNEL_OPTION_COMPRESS_RDP
                | VChannels.CHANNEL_OPTION_SHOW_PROTOCOL;
    }

    /*
     * Data processing methods
     */
    @Override
    public void process(RdpPacket data) throws RdesktopException, IOException,
            CryptoException {

        int type = data.getLittleEndian16();
        int status = data.getLittleEndian16();
        int length = data.getLittleEndian32();

        if (status == CLIPRDR_ERROR) {
            if (type == CLIPRDR_FORMAT_ACK) {
                send_format_announce();
                return;
            }

            return;
        }

        switch (type) {
            case CLIPRDR_CONNECT:
                send_format_announce();
                break;
            case CLIPRDR_FORMAT_ANNOUNCE:
                handle_clip_format_announce(data, length);
                return;
            case CLIPRDR_FORMAT_ACK:
            case 7:
                break;
            case CLIPRDR_DATA_REQUEST:
                handle_data_request(data);
                break;
            case CLIPRDR_DATA_RESPONSE:
                handle_data_response(data, length);
                break;
            default:
                
        }

    }

    @Override
    public void send_null(int type, int status) {

        RdpPacket_Localised s = new RdpPacket_Localised(12);
        s.setLittleEndian16(type);
        s.setLittleEndian16(status);
        s.setLittleEndian32(0);
        s.setLittleEndian32(0); 
        s.markEnd();

        try {
            this.send_packet(s);
        } catch (RdesktopException | CryptoException | IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void send_format_announce() {
























    }

    private void handle_clip_format_announce(RdpPacket data, int length)
            throws RdesktopException, IOException, CryptoException {
        TypeHandlerList serverTypeList = new TypeHandlerList();

        
        for (int c = length; c >= 36; c -= 36) {
            int typeCode = data.getLittleEndian32();
            
            
            data.incrementPosition(32);
            serverTypeList.add(allHandlers.getHandlerForFormat(typeCode));
        }
        

        send_null(CLIPRDR_FORMAT_ACK, CLIPRDR_RESPONSE);
        currentHandler = serverTypeList.getFirst();

        if (currentHandler != null)
            request_clipboard_data(currentHandler.preferredFormat());
    }

    private void handle_data_request(RdpPacket data) {
        int format = data.getLittleEndian32();
        Transferable clipData = clipboard.getContents(this);

        TypeHandler outputHandler = allHandlers.getHandlerForFormat(format);
        if (outputHandler != null) {
            outputHandler.send_data(clipData, this);
            
            
            
            
            
            
        }

        
    }

    private void handle_data_response(RdpPacket data, int length) {
        
        
        
        
        if (currentHandler != null)
            currentHandler.handleData(data, length, this);
        currentHandler = null;
    }

    private void request_clipboard_data(int formatcode) throws RdesktopException,
            IOException, CryptoException {

        RdpPacket_Localised s = Common.secure.init(
                Constants.encryption ? Secure.SEC_ENCRYPT : 0, 24);
        s.setLittleEndian32(16); 

        int flags = VChannels.CHANNEL_FLAG_FIRST | VChannels.CHANNEL_FLAG_LAST;
        if ((this.flags() & VChannels.CHANNEL_OPTION_SHOW_PROTOCOL) != 0)
            flags |= VChannels.CHANNEL_FLAG_SHOW_PROTOCOL;

        s.setLittleEndian32(flags);
        s.setLittleEndian16(CLIPRDR_DATA_REQUEST);
        s.setLittleEndian16(CLIPRDR_REQUEST);
        s.setLittleEndian32(4); 
        s.setLittleEndian32(formatcode);
        s.setLittleEndian32(0); 
        s.markEnd();

        Common.secure.send_to_channel(s,
                Constants.encryption ? Secure.SEC_ENCRYPT : 0, this.mcs_id());
    }

    @Override
    public void send_data(byte[] data, int length) {
        CommunicationMonitor.lock(this);

        RdpPacket_Localised all = new RdpPacket_Localised(12 + length);

        all.setLittleEndian16(CLIPRDR_DATA_RESPONSE);
        all.setLittleEndian16(CLIPRDR_RESPONSE);
        all.setLittleEndian32(length + 4); 
        
        
        
        all.copyFromByteArray(data, 0, all.getPosition(), length);
        all.incrementPosition(length);
        all.setLittleEndian32(0);

        try {
            this.send_packet(all);
        } catch (RdesktopException | CryptoException | IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            if (!Common.underApplet)
                System.exit(-1);
        }

        CommunicationMonitor.unlock(this);
    }

    /*
     * FocusListener methods
     */
    @Override
    public void focusGained(FocusEvent arg0) {
        
        
        if (Options.use_rdp5) {
            send_format_announce();

        }
    }

    @Override
    public void focusLost(FocusEvent arg0) {
    }

    /*
     * ClipboardOwner methods
     */
    @Override
    public void lostOwnership(Clipboard arg0, Transferable arg1) {
        logger.debug("Lost clipboard ownership");
    }

    @Override
    public void copyToClipboard(Transferable t) {
        clipboard.setContents(t, this);
    }

}
