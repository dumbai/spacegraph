/* Licence.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Handles request, receipt and processing of licences
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
import net.propero.rdp.crypto.RC4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

class Licence {
    private static final Logger logger = LoggerFactory.getLogger(Licence.class);
    /* constants for the licence negotiation */
    private static final int LICENCE_TOKEN_SIZE = 10;
    private static final int LICENCE_HWID_SIZE = 20;
    private static final int LICENCE_SIGNATURE_SIZE = 16;
    private static final int LICENCE_TAG_DEMAND = 0x01;
    private static final int LICENCE_TAG_AUTHREQ = 0x02;
    private static final int LICENCE_TAG_ISSUE = 0x03;
    private static final int LICENCE_TAG_REISSUE = 0x04;
    private static final int LICENCE_TAG_PRESENT = 0x12;

    /*
     * private static final int LICENCE_TAG_DEMAND = 0x0201; private static
     * final int LICENCE_TAG_AUTHREQ = 0x0202; private static final int
     * LICENCE_TAG_ISSUE = 0x0203; private static final int LICENCE_TAG_REISSUE =
     * 0x0204; 
     * 0x0212; 
     * 0x0213; private static final int LICENCE_TAG_AUTHRESP = 0x0215; private
     * static final int LICENCE_TAG_RESULT = 0x02ff;
     */
    private static final int LICENCE_TAG_REQUEST = 0x13;
    private static final int LICENCE_TAG_AUTHRESP = 0x15;
    private static final int LICENCE_TAG_RESULT = 0xff;
    private static final int LICENCE_TAG_USER = 0x000f;
    private static final int LICENCE_TAG_HOST = 0x0010;
    private final Secure secure;
    private final byte[] licence_sign_key;
    private byte[] licence_key;
    private byte[] in_token;

    Licence(Secure s) {
        secure = s;
        licence_key = new byte[16];
        licence_sign_key = new byte[16];
    }

    /**
     * Load a licence from disk
     *
     * @return Raw byte data for stored licence
     */
    private static byte[] load_licence() {
        logger.debug("load_licence");
        

        return (new LicenceStore_Localised()).load_licence();
    }

    /**
     * Save a licence to disk
     *
     * @param data   Packet containing licence data
     * @param length Length of licence
     */
    private static void save_licence(RdpPacket_Localised data, int length) {
        logger.debug("save_licence");
        int startpos = data.getPosition();
        data.incrementPosition(2); 
        /* Skip three strings */
        int len;
        for (int i = 0; i < 3; i++) {
            len = data.getLittleEndian32();
            data.incrementPosition(len);
            /*
             * Make sure that we won't be past the end of data after reading the
             * next length value
             */
            if (data.getPosition() + 4 - startpos > length) {
                logger.warn("Error in parsing licence key.");
                return;
            }
        }
        len = data.getLittleEndian32();
        logger.debug("save_licence: len={}", len);
        if (data.getPosition() + len - startpos > length) {
            logger.warn("Error in parsing licence key.");
            return;
        }

        byte[] databytes = new byte[len];
        data.copyToByteArray(databytes, 0, data.getPosition(), len);

        new LicenceStore_Localised().save_licence(databytes);

        /*
         * String dirpath = Options.licence_path;
         * filepath = dirpath +"/licence."+Options.hostname;
         *
         * File file = new File(dirpath); file.mkdir(); try{ FileOutputStream fd =
         * new FileOutputStream(filepath);
         *  
         * data.copyToByteArray(databytes,0,data.getPosition(),len);
         * fd.write(databytes); fd.close(); logger.info("Stored licence at " +
         * filepath); } catch(FileNotFoundException
         * e){logger.info("save_licence: file path not valid!");}
         * catch(IOException e){logger.warn("IOException in save_licence");}
         */
    }

    private static byte[] generate_hwid() {
        byte[] hwid = new byte[LICENCE_HWID_SIZE];
        Secure.setLittleEndian32(hwid, 2);
        byte[] name = Options.hostname.getBytes(StandardCharsets.US_ASCII);

        System.arraycopy(name, 0, hwid, 4, Math.min(name.length, LICENCE_HWID_SIZE - 4));
        return hwid;
    }

    /**
     * Process and handle licence data from a packet
     *
     * @param data Packet containing licence data
     * @throws RdesktopException
     * @throws IOException
     * @throws CryptoException
     */
    public void process(RdpPacket_Localised data) throws RdesktopException,
            IOException, CryptoException {
        int tag = data.get8();
        data.incrementPosition(3); 

        switch (tag) {

            case (LICENCE_TAG_DEMAND):
                this.process_demand(data);
                break;

            case (LICENCE_TAG_AUTHREQ):
                this.process_authreq(data);
                break;

            case (LICENCE_TAG_ISSUE):
                this.process_issue(data);
                break;

            case (LICENCE_TAG_REISSUE):
                logger.debug("Presented licence was accepted!");
                break;

            case (LICENCE_TAG_RESULT):
                break;

            default:
                logger.warn("got licence tag: {}", tag);
        }

    }

    /**
     * Process a demand for a licence. Find a license and transmit to server, or
     * request new licence
     *
     * @param data Packet containing details of licence demand
     * @throws UnsupportedEncodingException
     * @throws RdesktopException
     * @throws IOException
     * @throws CryptoException
     */
    private void process_demand(RdpPacket_Localised data)
            throws RdesktopException,
            IOException, CryptoException {
        byte[] server_random = new byte[Secure.SEC_RANDOM_SIZE];
        byte[] host = Options.hostname.getBytes(StandardCharsets.US_ASCII);
        byte[] user = Options.username.getBytes(StandardCharsets.US_ASCII);

        /* retrieve the server random */
        data.copyToByteArray(server_random, 0, data.getPosition(),
                server_random.length);
        data.incrementPosition(server_random.length);

        /* Null client keys are currently used */
        byte[] null_data = new byte[Secure.SEC_MODULUS_SIZE];
        this.generate_keys(null_data, server_random, null_data);

        if (!Options.built_in_licence && Options.load_licence) {
            byte[] licence_data = load_licence();
            if ((licence_data != null) && (licence_data.length > 0)) {
                logger.debug("licence_data.length = {}", licence_data.length);
                /* Generate a signature for the HWID buffer */
                byte[] hwid = generate_hwid();
                byte[] signature = secure.sign(this.licence_sign_key, 16, 16,
                        hwid, hwid.length);

                /* now crypt the hwid */
                RC4 rc4_licence = new RC4();
                byte[] crypt_key = new byte[this.licence_key.length];
                System.arraycopy(this.licence_key, 0, crypt_key, 0,
                        this.licence_key.length);
                rc4_licence.engineInitEncrypt(crypt_key);
                byte[] crypt_hwid = new byte[LICENCE_HWID_SIZE];
                rc4_licence.crypt(hwid, 0, LICENCE_HWID_SIZE, crypt_hwid, 0);

                present(null_data, null_data, licence_data,
                        licence_data.length, crypt_hwid, signature);
                logger.debug("Presented stored licence to server!");
                return;
            }
        }
        this.send_request(null_data, null_data, user, host);
    }

    /**
     * Handle an authorisation request, based on a licence signature (store
     * signatures in this Licence object
     *
     * @param data Packet containing details of request
     * @return True if signature is read successfully
     * @throws RdesktopException
     */
    private boolean parse_authreq(RdpPacket_Localised data)
            throws RdesktopException {

        data.incrementPosition(6);

        int tokenlen = data.getLittleEndian16();

        if (tokenlen != LICENCE_TOKEN_SIZE) {
            throw new RdesktopException("Wrong Tokenlength!");
        }
        this.in_token = new byte[tokenlen];
        data.copyToByteArray(this.in_token, 0, data.getPosition(), tokenlen);
        data.incrementPosition(tokenlen);
        byte[] in_sig = new byte[LICENCE_SIGNATURE_SIZE];
        data.copyToByteArray(in_sig, 0, data.getPosition(),
                LICENCE_SIGNATURE_SIZE);
        data.incrementPosition(LICENCE_SIGNATURE_SIZE);

        return data.getPosition() == data.getEnd();
    }

    /**
     * Respond to authorisation request, with token, hwid and signature, send
     * response to server
     *
     * @param token      Token data
     * @param crypt_hwid HWID for encryption
     * @param signature  Signature data
     * @throws RdesktopException
     * @throws IOException
     * @throws CryptoException
     */
    private void send_authresp(byte[] token, byte[] crypt_hwid, byte[] signature)
            throws RdesktopException, IOException, CryptoException {
        int sec_flags = Secure.SEC_LICENCE_NEG;
        int length = 58;

        RdpPacket_Localised data = secure.init(sec_flags, length + 2);

        data.set8(LICENCE_TAG_AUTHRESP);
        data.set8(2); 
        data.setLittleEndian16(length);

        data.setLittleEndian16(1);
        data.setLittleEndian16(LICENCE_TOKEN_SIZE);
        data
                .copyFromByteArray(token, 0, data.getPosition(),
                        LICENCE_TOKEN_SIZE);
        data.incrementPosition(LICENCE_TOKEN_SIZE);

        data.setLittleEndian16(1);
        data.setLittleEndian16(LICENCE_HWID_SIZE);
        data.copyFromByteArray(crypt_hwid, 0, data.getPosition(),
                LICENCE_HWID_SIZE);
        data.incrementPosition(LICENCE_HWID_SIZE);

        data.copyFromByteArray(signature, 0, data.getPosition(),
                LICENCE_SIGNATURE_SIZE);
        data.incrementPosition(LICENCE_SIGNATURE_SIZE);
        data.markEnd();
        secure.send(data, sec_flags);
    }

    /**
     * Present a licence to the server
     *
     * @param client_random
     * @param rsa_data
     * @param licence_data
     * @param licence_size
     * @param hwid
     * @param signature
     * @throws RdesktopException
     * @throws IOException
     * @throws CryptoException
     */
    private void present(byte[] client_random, byte[] rsa_data,
                         byte[] licence_data, int licence_size, byte[] hwid, byte[] signature)
            throws RdesktopException, IOException, CryptoException {
        int sec_flags = Secure.SEC_LICENCE_NEG;
        int length = /* rdesktop is 16 not 20, but this must be wrong?! */
                20 + Secure.SEC_RANDOM_SIZE + Secure.SEC_MODULUS_SIZE
                        + Secure.SEC_PADDING_SIZE + licence_size + LICENCE_HWID_SIZE
                        + LICENCE_SIGNATURE_SIZE;

        RdpPacket_Localised s = secure.init(sec_flags, length + 4);

        s.set8(LICENCE_TAG_PRESENT);
        s.set8(2); 
        s.setLittleEndian16(length);

        s.setLittleEndian32(1);
        s.setLittleEndian16(0);
        s.setLittleEndian16(0x0201);

        s.copyFromByteArray(client_random, 0, s.getPosition(),
                Secure.SEC_RANDOM_SIZE);
        s.incrementPosition(Secure.SEC_RANDOM_SIZE);
        s.setLittleEndian16(0);
        s
                .setLittleEndian16((Secure.SEC_MODULUS_SIZE + Secure.SEC_PADDING_SIZE));
        s.copyFromByteArray(rsa_data, 0, s.getPosition(),
                Secure.SEC_MODULUS_SIZE);
        s.incrementPosition(Secure.SEC_MODULUS_SIZE);
        s.incrementPosition(Secure.SEC_PADDING_SIZE);

        s.setLittleEndian16(1);
        s.setLittleEndian16(licence_size);
        s.copyFromByteArray(licence_data, 0, s.getPosition(), licence_size);
        s.incrementPosition(licence_size);

        s.setLittleEndian16(1);
        s.setLittleEndian16(LICENCE_HWID_SIZE);
        s.copyFromByteArray(hwid, 0, s.getPosition(), LICENCE_HWID_SIZE);
        s.incrementPosition(LICENCE_HWID_SIZE);
        s.copyFromByteArray(signature, 0, s.getPosition(),
                LICENCE_SIGNATURE_SIZE);
        s.incrementPosition(LICENCE_SIGNATURE_SIZE);

        s.markEnd();
        secure.send(s, sec_flags);
    }

    /**
     * Process an authorisation request
     *
     * @param data Packet containing request details
     * @throws RdesktopException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws CryptoException
     */
    private void process_authreq(RdpPacket_Localised data)
            throws RdesktopException,
            IOException, CryptoException {

        RC4 rc4_licence = new RC4();

        /* parse incoming packet and save encrypted token */
        if (!parse_authreq(data)) {
            throw new RdesktopException("Authentication Request was corrupt!");
        }
        byte[] out_token = new byte[LICENCE_TOKEN_SIZE];
        System.arraycopy(this.in_token, 0, out_token, 0, LICENCE_TOKEN_SIZE);

        /* decrypt token. It should read TEST in Unicode */
        byte[] crypt_key = new byte[this.licence_key.length];
        System.arraycopy(this.licence_key, 0, crypt_key, 0,
                this.licence_key.length);
        rc4_licence.engineInitDecrypt(crypt_key);
        byte[] decrypt_token = new byte[LICENCE_TOKEN_SIZE];
        rc4_licence.crypt(this.in_token, 0, LICENCE_TOKEN_SIZE, decrypt_token,
                0);

        /* construct HWID */
        byte[] hwid = Licence.generate_hwid();

        /* generate signature for a buffer of token and HWId */
        byte[] sealed_buffer = new byte[LICENCE_TOKEN_SIZE + LICENCE_HWID_SIZE];
        System
                .arraycopy(decrypt_token, 0, sealed_buffer, 0,
                        LICENCE_TOKEN_SIZE);
        System.arraycopy(hwid, 0, sealed_buffer, LICENCE_TOKEN_SIZE,
                LICENCE_HWID_SIZE);

        byte[] out_sig = secure.sign(this.licence_sign_key, 16, 16, sealed_buffer,
                sealed_buffer.length);

        /* deliberately break signature if licencing disabled */
        if (!Constants.licence) {
            out_sig = new byte[LICENCE_SIGNATURE_SIZE]; 
        }

        /* now crypt the hwid */
        System.arraycopy(this.licence_key, 0, crypt_key, 0,
                this.licence_key.length);
        rc4_licence.engineInitEncrypt(crypt_key);
        byte[] crypt_hwid = new byte[LICENCE_HWID_SIZE];
        rc4_licence.crypt(hwid, 0, LICENCE_HWID_SIZE, crypt_hwid, 0);

        this.send_authresp(out_token, crypt_hwid, out_sig);
    }

    /**
     * Handle a licence issued by the server, save to disk if
     * Options.save_licence
     *
     * @param data Packet containing issued licence
     * @throws CryptoException
     */
    private void process_issue(RdpPacket_Localised data) throws CryptoException {
        RC4 rc4_licence = new RC4();
        byte[] key = new byte[this.licence_key.length];
        System.arraycopy(this.licence_key, 0, key, 0, this.licence_key.length);

        data.incrementPosition(2);
        int length = data.getLittleEndian16();

        if (data.getPosition() + length > data.getEnd()) {
            return;
        }

        rc4_licence.engineInitDecrypt(key);
        byte[] buffer = new byte[length];
        data.copyToByteArray(buffer, 0, data.getPosition(), length);
        rc4_licence.crypt(buffer, 0, length, buffer, 0);
        data.copyFromByteArray(buffer, 0, data.getPosition(), length);

        int check = data.getLittleEndian16();
        if (check != 0) {
            
        }
        secure.licenceIssued = true;

        /*
         * data.incrementPosition(2); 
         *  
         * data.incrementPosition(length); 
         * data.getLittleEndian32(length); 
         * (!(data.getPosition() + length <= data.getEnd())) return; }
         */

        secure.licenceIssued = true;
        logger.debug("Server issued Licence");
        if (Options.save_licence)
            save_licence(data, length - 2);
    }

    /**
     * Send a request for a new licence, or to approve a stored licence
     *
     * @param client_random
     * @param rsa_data
     * @param username
     * @param hostname
     * @throws RdesktopException
     * @throws IOException
     * @throws CryptoException
     */
    private void send_request(byte[] client_random, byte[] rsa_data,
                              byte[] username, byte[] hostname) throws RdesktopException,
            IOException, CryptoException {
        int sec_flags = Secure.SEC_LICENCE_NEG;
        int userlen = (username.length == 0 ? 0 : username.length + 1);
        int hostlen = (hostname.length == 0 ? 0 : hostname.length + 1);
        int length = 128 + userlen + hostlen;

        RdpPacket_Localised buffer = secure.init(sec_flags, length);

        buffer.set8(LICENCE_TAG_REQUEST);
        buffer.set8(2); 
        buffer.setLittleEndian16(length);

        buffer.setLittleEndian32(1);

        if (Options.built_in_licence && (!Options.load_licence)
                && (!Options.save_licence)) {
            logger.debug("Using built-in Windows Licence");
            buffer.setLittleEndian32(0x03010000);
        } else {
            logger.debug("Requesting licence");
            buffer.setLittleEndian32(0xff010000);
        }
        buffer.copyFromByteArray(client_random, 0, buffer.getPosition(),
                Secure.SEC_RANDOM_SIZE);
        buffer.incrementPosition(Secure.SEC_RANDOM_SIZE);
        buffer.setLittleEndian16(0);

        buffer.setLittleEndian16(Secure.SEC_MODULUS_SIZE
                + Secure.SEC_PADDING_SIZE);
        buffer.copyFromByteArray(rsa_data, 0, buffer.getPosition(),
                Secure.SEC_MODULUS_SIZE);
        buffer.incrementPosition(Secure.SEC_MODULUS_SIZE);

        buffer.incrementPosition(Secure.SEC_PADDING_SIZE);

        buffer.setLittleEndian16(LICENCE_TAG_USER);
        buffer.setLittleEndian16(userlen);

        if (username.length != 0) {
            buffer.copyFromByteArray(username, 0, buffer.getPosition(),
                    userlen - 1);
        } else {
            buffer
                    .copyFromByteArray(username, 0, buffer.getPosition(),
                            userlen);
        }

        buffer.incrementPosition(userlen);

        buffer.setLittleEndian16(LICENCE_TAG_HOST);
        buffer.setLittleEndian16(hostlen);

        if (hostname.length != 0) {
            buffer.copyFromByteArray(hostname, 0, buffer.getPosition(),
                    hostlen - 1);
        } else {
            buffer
                    .copyFromByteArray(hostname, 0, buffer.getPosition(),
                            hostlen);
        }
        buffer.incrementPosition(hostlen);
        buffer.markEnd();
        secure.send(buffer, sec_flags);
    }

    /**
     * Generate a set of encryption keys
     *
     * @param client_key Array in which to store client key
     * @param server_key Array in which to store server key
     * @param client_rsa Array in which to store RSA data
     * @throws CryptoException
     */
    private void generate_keys(byte[] client_key, byte[] server_key,
                               byte[] client_rsa) {

        byte[] temp_hash = secure.hash48(client_rsa, client_key, server_key, 65);
        byte[] session_key = secure.hash48(temp_hash, server_key, client_key, 65);

        System.arraycopy(session_key, 0, this.licence_sign_key, 0, 16);

        this.licence_key = secure.hash16(session_key, client_key, server_key,
                16);
    }
}
