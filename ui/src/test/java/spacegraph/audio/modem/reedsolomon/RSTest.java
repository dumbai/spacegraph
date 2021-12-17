package spacegraph.audio.modem.reedsolomon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example use of Reed-Solomon library
 * <p>
 * Copyright Henry Minsky (hqm@alum.mit.edu) 1991-2009
 * (Ported to Java by Jonas Michel 2012)
 * <p>
 * This is a direct port of RSCODE by Henry Minsky
 * http://rscode.sourceforge.net/
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * <p>
 * Commercial licensing is available under a separate license, please
 * contact author for details.
 * <p>
 * Source code is available at http://code.google.com/p/mobile-acoustic-modems-in-action/
 * <p>
 * This same code demonstrates the use of the encoder and
 * decoder/error-correction routines.
 * <p>
 * We are assuming we have at least four bytes of parity
 * (kParityBytes >= 4).
 * <p>
 * This gives us the ability to correct up to two errors, or
 * four erasures.
 * <p>
 * In general, with E errors, and K erasures, you will need
 * 2E + K bytes of parity to be able to correct the codeword
 * back to recover the original message data.
 * <p>
 * You could say that each error 'consumes' two bytes of the parity,
 * whereas each erasure 'consumes' one byte.
 * <p>
 * Thus, as demonstrated below, we can inject one error (location unknown)
 * and two erasures (with their locations specified) and the
 * error-correction routine will be able to correct the codeword
 * back to the original message.
 */
class RSTest implements Settings {


    @Test
    void testEncodeDecode() {

        /* Initialization the ECC library */

        RS rs = new RS();

        /* ************** */
        byte[] codeword = new byte[256];

        /* Encode data into codeword, adding kParityBytes parity bytes */
        rs.encode(msg.getBytes(), msg.length(), codeword);

        System.out.println("Encoded data is: " + new String(rtrim(codeword)));

        int ML = msg.length() + Settings.kParityBytes;

        /* Add one error and two erasures */
        byte_err(0x35, 3, codeword);

        byte_erasure(17, codeword);
        byte_erasure(19, codeword);

        String errStr = new String(rtrim(codeword));
        //System.out.println("With some errors: " + errStr);
        assertNotEquals(msg, errStr);

        /*
         * We need to indicate the position of the erasures. Erasure positions
         * are indexed (1 based) from the end of the message...
         */

        int nerasures = 0;
        int[] erasures = new int[16];
        erasures[nerasures++] = ML - 17;
        erasures[nerasures++] = ML - 19;

        /* Now decode -- encoded codeword size must be passed */
        rs.decode(codeword, ML);

        /* check if syndrome is all zeros */
        if (rs.check_syndrome() != 0) {
            Berlekamp.correct_errors_erasures(rs, codeword, ML, nerasures, erasures);

            String decoded = new String(rtrim(codeword));
            System.out.println("Corrected codeword: " + decoded);
            assertEquals(msg, decoded);
        } else
            fail();

    }


    static String msg = "The fat cat in the hat sat on a rat.";

    /* Some debugging routines to introduce errors or erasures into a codeword. */

    /* Introduce a byte error at LOC */
    static void byte_err(int err, int loc, byte[] dst) {
        System.out.println("Adding Error at loc " + loc + ", data 0x"
                + Integer.toHexString(dst[loc - 1]));
        dst[loc - 1] ^= err;
    }

    /*
     * Pass in location of error (first byte position is labeled starting at 1,
     * not 0), and the codeword.
     */
    static void byte_erasure(int loc, byte[] dst) {
        System.out.println("Erasure at loc " + loc + ", data 0x"
                + Integer.toHexString(dst[loc - 1]));
        dst[loc - 1] = 0;
    }

    /*
     * Trim off excess zeros and parity bytes.
     */
    static byte[] rtrim(byte[] bytes) {
        int t = bytes.length - 1;
        while (bytes[t] == 0)
            t -= 1;
        byte[] trimmed = new byte[(t + 1) - Settings.kParityBytes];
        System.arraycopy(bytes, 0, trimmed, 0, trimmed.length);
        return trimmed;
    }

}