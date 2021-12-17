/* ISO.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: ISO layer of communication
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

abstract class ISO {
    private static final Logger logger = LoggerFactory.getLogger(ISO.class);
    /* this for the ISO Layer */
    private static final int CONNECTION_REQUEST = 0xE0;
    private static final int CONNECTION_CONFIRM = 0xD0;
    private static final int DISCONNECT_REQUEST = 0x80;
    private static final int DATA_TRANSFER = 0xF0;
    private static final int ERROR = 0x70;
    private static final int PROTOCOL_VERSION = 0x03;
    private static final int EOT = 0x80;
    private static int g_packetno;
    Socket rdpsock;
    private DataInputStream in;
    private DataOutputStream out;

    /**
     * Construct ISO object, initialises hex dump
     */
    ISO() {

    }

    /*
     * protected Socket negotiateSSL(Socket sock) throws Exception{ return sock; }
     */

    /**
     * Initialise an ISO PDU
     *
     * @param length Desired length of PDU
     * @return Packet configured as ISO PDU, ready to write at higher level
     */
    public static RdpPacket_Localised init(int length) {
        RdpPacket_Localised data = new RdpPacket_Localised(length + 7);
        data.incrementPosition(7);
        data.setStart(data.getPosition());
        return data;
    }

    /**
     * Create a socket for this ISO object
     *
     * @param host Address of server
     * @param port Port on which to connect socket
     * @throws IOException
     */
    void doSocketConnect(InetAddress host, int port)
            throws IOException {
        this.rdpsock = new Socket(host, port);
    }

    /**
     * Connect to a server
     *
     * @param host Address of server
     * @param port Port to connect to on server
     * @throws IOException
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    public void connect(InetAddress host, int port) throws IOException,
            RdesktopException, OrderException, CryptoException {
        doSocketConnect(host, port);
        rdpsock.setTcpNoDelay(Options.low_latency);
        
        this.in = new DataInputStream(new BufferedInputStream(rdpsock
                .getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(rdpsock
                .getOutputStream()));
        send_connection_request();

        int[] code = new int[1];
        receiveMessage(code);
        if (code[0] != CONNECTION_CONFIRM) {
            throw new RdesktopException("Expected CC got:"
                    + Integer.toHexString(code[0]).toUpperCase());
        }

        /*
         * if(Options.use_ssl){ try { rdpsock = this.negotiateSSL(rdpsock);
         * this.in = new DataInputStream(rdpsock.getInputStream()); this.out=
         * new DataOutputStream(rdpsock.getOutputStream()); } catch (Exception
         * e) { e.printStackTrace(); throw new RdesktopException("SSL
         * negotiation failed: " + e.getMessage()); } }
         */

    }

    /**
     * Send a self contained iso-pdu
     *
     * @param type one of the following CONNECT_RESPONSE, DISCONNECT_REQUEST
     * @throws IOException when an I/O Error occurs
     */
    private void sendMessage(int type) throws IOException {
        RdpPacket_Localised buffer = new RdpPacket_Localised(11);

        buffer.set8(PROTOCOL_VERSION); 
        buffer.set8(0); 
        buffer.setBigEndian16(11); 
        buffer.set8(6); 

        buffer.set8(type); 
        buffer.setBigEndian16(0); 

        buffer.setBigEndian16(0); 
        
        buffer.set8(0);
        byte[] packet = new byte[11];
        buffer.copyToByteArray(packet, 0, 0, packet.length);
        out.write(packet);
        out.flush();
    }

    /**
     * Send a packet to the server, wrapped in ISO PDU
     *
     * @param buffer Packet containing data to send to server
     * @throws RdesktopException
     * @throws IOException
     */
    public void send(RdpPacket_Localised buffer) throws RdesktopException,
            IOException {
        if (rdpsock == null || out == null)
            return;
        if (buffer.getEnd() < 0) {
            throw new RdesktopException("No End Mark!");
        } else {
            int length = buffer.getEnd();

            buffer.setPosition(0);
            buffer.set8(PROTOCOL_VERSION); 
            buffer.set8(0); 
            buffer.setBigEndian16(length); 

            buffer.set8(2); 
            buffer.set8(DATA_TRANSFER);
            buffer.set8(EOT);
            byte[] packet = new byte[length];
            buffer.copyToByteArray(packet, 0, 0, buffer.getEnd());

            if (Options.debug_hexdump) {
                System.out.println("ISO Sending packet:");
                System.out.println(net.propero.rdp.tools.HexDump.dumpHexString(packet));
            }

            out.write(packet);
            out.flush();
        }
    }

    /**
     * Receive a data transfer message from the server
     *
     * @return Packet containing message (as ISO PDU)
     * @throws IOException
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    public RdpPacket_Localised receive() throws IOException, RdesktopException,
            OrderException, CryptoException {
        int[] type = new int[1];
        RdpPacket_Localised buffer = receiveMessage(type);
        if (buffer == null)
            return null;
        if (type[0] != DATA_TRANSFER) {
            throw new RdesktopException("Expected DT got:" + type[0]);
        }

        return buffer;
    }

    /**
     * Receive a specified number of bytes from the server, and store in a
     * packet
     *
     * @param p      Packet to append data to, null results in a new packet being
     *               created
     * @param length Length of data to read
     * @return Packet containing read data, appended to original data if
     * provided
     * @throws IOException
     */
    private RdpPacket_Localised tcp_recv(RdpPacket_Localised p, int length)
            throws IOException {
        logger.debug("ISO.tcp_recv");
        byte[] packet = new byte[length];

        in.readFully(packet, 0, length);

        
        
        
        if (Options.debug_hexdump) {

            System.out.printf("\nISO receive RDP packet # %d%n", ++g_packetno);
            System.out.println(net.propero.rdp.tools.HexDump.dumpHexString(packet));
        }

        RdpPacket_Localised buffer = null;
        if (p == null) {
            buffer = new RdpPacket_Localised(length);
            buffer.copyFromByteArray(packet, 0, 0, packet.length);
            buffer.markEnd(length);
            buffer.setStart(buffer.getPosition());
        } else {
            buffer = new RdpPacket_Localised((p.getEnd() - p.getStart())
                    + length);
            buffer.copyFromPacket(p, p.getStart(), 0, p.getEnd());
            buffer.copyFromByteArray(packet, 0, p.getEnd(), packet.length);
            buffer.markEnd(p.size() + packet.length);
            buffer.setPosition(p.getPosition());
            buffer.setStart(0);
        }

        return buffer;
    }

    /**
     * Receive a message from the server
     *
     * @param type Array containing message type, stored in type[0]
     * @return Packet object containing data of message
     * @throws IOException
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    private RdpPacket_Localised receiveMessageex(int[] type, int[] rdpver) throws IOException,
            RdesktopException, OrderException, CryptoException {
        logger.debug("ISO.receiveMessage");
        RdpPacket_Localised s = null;

        next_packet:
        while (true) {
            logger.debug("next_packet");
            s = tcp_recv(null, 4);
            logger.debug("off next_packet");
            if (s == null)
                return null;

            int version = s.get8();
            rdpver[0] = version;

            int length;
            if (version == 3) {
                s.incrementPosition(1); 
                length = s.getBigEndian16();
            } else {
                length = s.get8();
                if ((length & 0x80) != 0) {
                    length &= ~0x80;
                    length = (length << 8) + s.get8();
                }
            }

            s = tcp_recv(s, length - 4);
            if (s == null)
                return null;
            if ((version & 3) == 0) {
                logger.debug("Processing rdp5 packet");
                Common.rdp.rdp5_process(s, (version & 0x80) != 0);
                continue next_packet;
            } else
                break;
        }

        s.get8();
        type[0] = s.get8();

        if (type[0] == DATA_TRANSFER) {
            logger.debug("Data Transfer Packet");
            s.incrementPosition(1); 
            return s;
        }

        s.incrementPosition(5); 
        return s;
    }

    private RdpPacket_Localised receiveMessage(int[] type) throws IOException,
            RdesktopException, OrderException, CryptoException {
        int[] rdpver = new int[1];
        return receiveMessageex(type, rdpver);
    }

    /**
     * Disconnect from an RDP session, closing all sockets
     */
    public void disconnect() {
        if (rdpsock == null)
            return;
        try {
            sendMessage(DISCONNECT_REQUEST);
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (rdpsock != null)
                rdpsock.close();
        } catch (IOException e) {
            in = null;
            out = null;
            rdpsock = null;
            return;
        }
        in = null;
        out = null;
        rdpsock = null;
    }

    /**
     * Send the server a connection request, detailing client protocol version
     *
     * @throws IOException
     */
    private void send_connection_request() throws IOException {

        String uname = Options.username;


        int length = 11 + (Options.username.isEmpty() ? 0 : ("Cookie: mstshash="
            .length()
            + uname.length() + 2))/* + 8*/;
        RdpPacket_Localised buffer = new RdpPacket_Localised(length);

        buffer.set8(PROTOCOL_VERSION); 
        buffer.set8(0); 
        buffer.setBigEndian16(length); 
        buffer.set8(length - 5); 
        buffer.set8(CONNECTION_REQUEST);
        buffer.setBigEndian16(0); 
        buffer.setBigEndian16(0); 
        
        buffer.set8(0); 
        if (!Options.username.isEmpty()) {
            logger.debug("Including username");
            buffer
                    .out_uint8p("Cookie: mstshash=", "Cookie: mstshash="
                            .length());
            buffer.out_uint8p(uname, uname.length());

            buffer.set8(0x0d); 
            buffer.set8(0x0a); 
        }

        /*
         * 
         * buffer.setLittleEndian16(0x08); 
         * buffer.set8(Options.use_ssl? 0x01 : 0x00);
         * buffer.incrementPosition(3);
         */


        byte[] packet = new byte[length];
        buffer.copyToByteArray(packet, 0, 0, packet.length);
        out.write(packet);
        out.flush();

        if (Options.debug_hexdump) {

            System.out.println("ISO Sending packet:");
            System.out.println(net.propero.rdp.tools.HexDump.dumpHexString(packet));
        }

    }
}