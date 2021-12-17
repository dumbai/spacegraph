/* MCS.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: MCS Layer of communication
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

import net.propero.rdp.crypto.CryptoException;
import net.propero.rdp.rdp5.VChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.stream.IntStream;

public class MCS {
    public static final int MCS_GLOBAL_CHANNEL = 1003;
    private static final int MCS_USERCHANNEL_BASE = 1001;
    private static final Logger logger = LoggerFactory.getLogger(MCS.class);
    /* this for the MCS Layer */
    private static final int CONNECT_INITIAL = 0x7f65;

    private static final int CONNECT_RESPONSE = 0x7f66;

    private static final int BER_TAG_BOOLEAN = 1;

    private static final int BER_TAG_INTEGER = 2;

    private static final int BER_TAG_OCTET_STRING = 4;

    private static final int BER_TAG_RESULT = 10;

    private static final int TAG_DOMAIN_PARAMS = 0x30;
    private static final int EDRQ = 1; /* Erect Domain Request */
    private static final int DPUM = 8; /* Disconnect Provider Ultimatum */
    private static final int AURQ = 10; /* Attach User Request */
    private static final int AUCF = 11; /* Attach User Confirm */
    private static final int CJRQ = 14; /* Channel Join Request */
    private static final int CJCF = 15; /* Channel Join Confirm */
    private static final int SDRQ = 25; /* Send Data Request */
    private static final int SDIN = 26; /* Send Data Indication */
    private final ISO IsoLayer;
    private final VChannels channels;
    private int McsUserID;

    /**
     * Initialise the MCS layer (and lower layers) with provided channels
     *
     * @param channels Set of available MCS channels
     */
    public MCS(VChannels channels) {
        this.channels = channels;
        IsoLayer = new ISO_Localised();
    }

    /**
     * send an Integer encoded according to the ISO ASN.1 Basic Encoding Rules
     *
     * @param buffer Packet in which to store encoded value
     * @param value  Integer value to store
     */
    private static void sendBerInteger(RdpPacket_Localised buffer, int value) {






        sendBerHeader(buffer, BER_TAG_INTEGER, 2);
        buffer.setBigEndian16(value);







    }

    /**
     * Determine the size of a BER header encoded for the specified tag and data
     * length
     *
     * @param tagval Value of tag identifying data type
     * @param length Length of data header will precede
     * @return
     */
    private static int berHeaderSize(int tagval, int length) {
        int total = 0;
		total += tagval > 0xff ? 2 : 1;

		total += length >= 0x80 ? 3 : 1;
        return total;
    }

    /**
     * Send a Header encoded according to the ISO ASN.1 Basic Encoding rules
     *
     * @param buffer Packet in which to send the header
     * @param tagval Data type for header
     * @param length Length of data header precedes
     */
    private static void sendBerHeader(RdpPacket_Localised buffer, int tagval, int length) {
        if (tagval > 0xff) {
            buffer.setBigEndian16(tagval);
        } else {
            buffer.set8(tagval);
        }

        if (length >= 0x80) {
            buffer.set8(0x82);
            buffer.setBigEndian16(length);
        } else {
            buffer.set8(length);
        }
    }

    /**
     * Determine the size of a BER encoded integer with specified value
     *
     * @param value Value of integer
     * @return Number of bytes the encoded data would occupy
     */
    private static int BERIntSize(int value) {
		return value > 0xff ? 4 : 3;
    }

    /**
     * Determine the size of the domain parameters, encoded according to the ISO
     * ASN.1 Basic Encoding Rules
     *
     * @param max_channels Maximum number of channels
     * @param max_users    Maximum number of users
     * @param max_tokens   Maximum number of tokens
     * @param max_pdusize  Maximum size of an MCS PDU
     * @return Number of bytes the domain parameters would occupy
     */
    private static int domainParamSize(int max_channels, int max_users,
                                       int max_tokens, int max_pdusize) {
        int endSize = IntStream.of(max_channels, max_users, max_tokens, 1, 0, 1, max_pdusize, 2).map(MCS::BERIntSize).sum();
        return berHeaderSize(TAG_DOMAIN_PARAMS, endSize) + endSize;
    }

    /**
     * Parse a BER header and determine data length
     *
     * @param data   Packet containing header at current read position
     * @param tagval Tag ID for data type
     * @return Length of following data
     * @throws RdesktopException
     */
    private static int berParseHeader(RdpPacket_Localised data, int tagval)
            throws RdesktopException {
        int tag = 0;

		tag = tagval > 0x000000ff ? data.getBigEndian16() : data.get8();

        if (tag != tagval) {
            throw new RdesktopException("Unexpected tag got " + tag
                    + " expected " + tagval);
        }

        int len = data.get8();

        int length = 0;
        if ((len & 0x00000080) != 0) {
            len &= ~0x00000080; 
            length = 0;
            while (len-- != 0) {
                length = (length << 8) + data.get8();
            }
        } else {
            length = len;
        }

        return length;
    }

    /**
     * Connect to a server
     *
     * @param host Address of server
     * @param port Port to connect to on server
     * @param data Packet to use for sending connection data
     * @throws IOException
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    public void connect(InetAddress host, int port, RdpPacket_Localised data)
            throws IOException, RdesktopException, OrderException,
            CryptoException {
        logger.debug("MCS.connect");
        IsoLayer.connect(host, port);

        this.sendConnectInitial(data);
        this.receiveConnectResponse(data);

        logger.debug("connect response received");

        send_edrq();
        send_aurq();

        this.McsUserID = receive_aucf();
        send_cjrq(this.McsUserID + MCS_USERCHANNEL_BASE);
        receive_cjcf();
        send_cjrq(MCS_GLOBAL_CHANNEL);
        receive_cjcf();

        for (int i = 0; i < channels.num_channels(); i++) {
            send_cjrq(VChannels.mcs_id(i));
            receive_cjcf();
        }

    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        IsoLayer.disconnect();
        
        
    }

    /**
     * Initialise a packet as an MCS PDU
     *
     * @param length Desired length of PDU
     * @return
     * @throws RdesktopException
     */
    public static RdpPacket_Localised init(int length) throws RdesktopException {
        RdpPacket_Localised data = ISO.init(length + 8);
        
        data.setHeader(RdpPacket.MCS_HEADER);
        data.incrementPosition(8);
        data.setStart(data.getPosition());
        return data;
    }

    /**
     * Send a packet to the global channel
     *
     * @param buffer Packet to send
     * @throws RdesktopException
     * @throws IOException
     */
    public void send(RdpPacket_Localised buffer) throws RdesktopException,
            IOException {
        send_to_channel(buffer, MCS_GLOBAL_CHANNEL);
    }

    /**
     * Send a packet to a specified channel
     *
     * @param buffer  Packet to send to channel
     * @param channel Id of channel on which to send packet
     * @throws RdesktopException
     * @throws IOException
     */
    public void send_to_channel(RdpPacket_Localised buffer, int channel)
            throws RdesktopException, IOException {
        buffer.setPosition(buffer.getHeader(RdpPacket.MCS_HEADER));

        int length = buffer.getEnd() - buffer.getHeader(RdpPacket.MCS_HEADER) - 8;
        length |= 0x8000;

        buffer.set8((SDRQ << 2));
        buffer.setBigEndian16(this.McsUserID);
        buffer.setBigEndian16(channel);
        buffer.set8(0x70); 
        buffer.setBigEndian16(length);
        IsoLayer.send(buffer);
    }

    /**
     * Receive an MCS PDU from the next channel with available data
     *
     * @param channel ID of channel will be stored in channel[0]
     * @return Received packet
     * @throws IOException
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    public RdpPacket_Localised receive(int[] channel) throws IOException,
            RdesktopException, OrderException, CryptoException {
        logger.debug("receive");
        RdpPacket_Localised buffer = IsoLayer.receive();
        if (buffer == null)
            return null;
        buffer.setHeader(RdpPacket.MCS_HEADER);
        int opcode = buffer.get8();

        int appid = opcode >> 2;

        if (appid != SDIN) {
            if (appid != DPUM) {
                throw new RdesktopException("Expected data got" + opcode);
            }
            throw new EOFException("End of transmission!");
        }

        buffer.incrementPosition(2); 
        channel[0] = buffer.getBigEndian16(); 
        logger.debug("Channel ID = {}", channel[0]);
        buffer.incrementPosition(1);

        int length = buffer.get8();

        if ((length & 0x80) != 0) {
            buffer.incrementPosition(1);
        }
        buffer.setStart(buffer.getPosition());
        return buffer;
    }

    /**
     * send a DOMAIN_PARAMS structure encoded according to the ISO ASN.1 Basic
     * Encoding rules
     *
     * @param buffer       Packet in which to send the structure
     * @param max_channels Maximum number of channels
     * @param max_users    Maximum number of users
     * @param max_tokens   Maximum number of tokens
     * @param max_pdusize  Maximum size for an MCS PDU
     */
    private static void sendDomainParams(RdpPacket_Localised buffer, int max_channels,
                                         int max_users, int max_tokens, int max_pdusize) {





        sendBerHeader(buffer, TAG_DOMAIN_PARAMS, 32);
        sendBerInteger(buffer, max_channels);
        sendBerInteger(buffer, max_users);
        sendBerInteger(buffer, max_tokens);

        sendBerInteger(buffer, 1); 
        sendBerInteger(buffer, 0); 
        sendBerInteger(buffer, 1); 

        sendBerInteger(buffer, max_pdusize);
        sendBerInteger(buffer, 2); 
    }

    /**
     * Send an MCS_CONNECT_INITIAL message (encoded as ASN.1 Ber)
     *
     * @param data Packet in which to send the message
     * @throws IOException
     * @throws RdesktopException
     */
    private void sendConnectInitial(RdpPacket_Localised data)
            throws IOException, RdesktopException {
        logger.debug("MCS.sendConnectInitial");

        int datalen = data.getEnd();



        int length = 9 + 3 * 34 + 4 + datalen;
        

        RdpPacket_Localised buffer = ISO.init(length + 5);

        sendBerHeader(buffer, CONNECT_INITIAL, length);
        sendBerHeader(buffer, BER_TAG_OCTET_STRING, 1); 
        buffer.set8(1); 
        sendBerHeader(buffer, BER_TAG_OCTET_STRING, 1); 
        buffer.set8(1); 

        sendBerHeader(buffer, BER_TAG_BOOLEAN, 1);
        buffer.set8(0xff); 

        sendDomainParams(buffer, 34, 2, 0, 0xffff); 
        
        sendDomainParams(buffer, 1, 1, 1, 0x420); 
        sendDomainParams(buffer, 0xffff, 0xfc17, 0xffff, 0xffff); 
        

        sendBerHeader(buffer, BER_TAG_OCTET_STRING, datalen);

        data.copyToPacket(buffer, 0, buffer.getPosition(), data.getEnd());
        buffer.incrementPosition(data.getEnd());
        buffer.markEnd();
        IsoLayer.send(buffer);
    }

    /**
     * Receive and handle a connect response from the server
     *
     * @param data Packet containing response data
     * @throws IOException
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    private void receiveConnectResponse(RdpPacket_Localised data)
            throws IOException, RdesktopException, OrderException,
            CryptoException {

        logger.debug("MCS.receiveConnectResponse");

        RdpPacket_Localised buffer = IsoLayer.receive();
        logger.debug("Received buffer");
        int length = berParseHeader(buffer, CONNECT_RESPONSE);
        length = berParseHeader(buffer, BER_TAG_RESULT);

        int result = buffer.get8();
        if (result != 0) {
            String[] connect_results = {"Successful", "Domain Merging",
                    "Domain not Hierarchical", "No Such Channel", "No Such Domain",
                    "No Such User", "Not Admitted", "Other User ID",
                    "Parameters Unacceptable", "Token Not Available",
                    "Token Not Possessed", "Too Many Channels", "Too Many Tokens",
                    "Too Many Users", "Unspecified Failure", "User Rejected"};
            throw new RdesktopException("MCS Connect failed: "
                    + connect_results[result]);
        }
        length = berParseHeader(buffer, BER_TAG_INTEGER);
        length = buffer.get8(); 
        parseDomainParams(buffer);
        length = berParseHeader(buffer, BER_TAG_OCTET_STRING);

        Common.secure.processMcsData(buffer);

        /*
         * if (length > data.size()) { logger.warn("MCS Datalength exceeds
         * size!"+length); length=data.size(); } data.copyFromPacket(buffer,
         * buffer.getPosition(), 0, length); data.setPosition(0);
         * data.markEnd(length); buffer.incrementPosition(length);
         *
         * if (buffer.getPosition() != buffer.getEnd()) { throw new
         * RdesktopException(); }
         */
    }

    /**
     * Transmit an EDrq message
     *
     * @throws IOException
     * @throws RdesktopException
     */
    private void send_edrq() throws IOException, RdesktopException {
        logger.debug("send_edrq");
        RdpPacket_Localised buffer = ISO.init(5);
        buffer.set8(EDRQ << 2);
        buffer.setBigEndian16(1); 
        buffer.setBigEndian16(1); 
        buffer.markEnd();
        IsoLayer.send(buffer);
    }

    /**
     * Transmit a CJrq message
     *
     * @param channelid Id of channel to be identified in request
     * @throws IOException
     * @throws RdesktopException
     */
    private void send_cjrq(int channelid) throws IOException, RdesktopException {
        RdpPacket_Localised buffer = ISO.init(5);
        buffer.set8(CJRQ << 2);
        buffer.setBigEndian16(this.McsUserID); 
        buffer.setBigEndian16(channelid); 
        buffer.markEnd();
        IsoLayer.send(buffer);
    }

    /**
     * Transmit an AUcf message
     *
     * @throws IOException
     * @throws RdesktopException
     */
    public void send_aucf() throws IOException, RdesktopException {
        RdpPacket_Localised buffer = ISO.init(2);

        buffer.set8(AUCF << 2);
        buffer.set8(0);
        buffer.markEnd();
        IsoLayer.send(buffer);
    }

    /**
     * Transmit an AUrq mesage
     *
     * @throws IOException
     * @throws RdesktopException
     */
    private void send_aurq() throws IOException, RdesktopException {
        RdpPacket_Localised buffer = ISO.init(1);

        buffer.set8(AURQ << 2);
        buffer.markEnd();
        IsoLayer.send(buffer);
    }

    /**
     * Receive and handle a CJcf message
     *
     * @throws IOException
     * @throws RdesktopException
     */
    private void receive_cjcf() throws IOException, RdesktopException,
            OrderException, CryptoException {
        logger.debug("receive_cjcf");
        RdpPacket_Localised buffer = IsoLayer.receive();

        int opcode = buffer.get8();
        if ((opcode >> 2) != CJCF) {
            throw new RdesktopException("Expected CJCF got" + opcode);
        }

        int result = buffer.get8();
        if (result != 0) {
            throw new RdesktopException("Expected CJRQ got " + result);
        }

        buffer.incrementPosition(4); 

        if ((opcode & 2) != 0) {
            buffer.incrementPosition(2); 
        }

        if (buffer.getPosition() != buffer.getEnd()) {
            throw new RdesktopException();
        }
    }

    /**
     * Receive an AUcf message
     *
     * @return UserID specified in message
     * @throws IOException
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    private int receive_aucf() throws IOException, RdesktopException,
            OrderException, CryptoException {
        logger.debug("receive_aucf");
        RdpPacket_Localised buffer = IsoLayer.receive();

        int opcode = buffer.get8();
        if ((opcode >> 2) != AUCF) {
            throw new RdesktopException("Expected AUCF got " + opcode);
        }

        int result = buffer.get8();
        if (result != 0) {
            throw new RdesktopException("Expected AURQ got " + result);
        }

        int UserID = 0;
        if ((opcode & 2) != 0) {
            UserID = buffer.getBigEndian16();
        }

        if (buffer.getPosition() != buffer.getEnd()) {
            throw new RdesktopException();
        }
        return UserID;
    }

    /**
     * Parse domain parameters sent by server
     *
     * @param data Packet containing domain parameters at current read position
     * @throws RdesktopException
     */
    private static void parseDomainParams(RdpPacket_Localised data)
            throws RdesktopException {

        int length = MCS.berParseHeader(data, TAG_DOMAIN_PARAMS);
        data.incrementPosition(length);

        if (data.getPosition() > data.getEnd()) {
            throw new RdesktopException();
        }
    }

    /**
     * Retrieve the user ID stored by this MCS object
     *
     * @return User ID
     */
    public int getUserID() {
        return this.McsUserID;
    }
}