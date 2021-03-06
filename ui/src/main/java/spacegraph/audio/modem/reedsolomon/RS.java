package spacegraph.audio.modem.reedsolomon;

/**
 * Reed Solomon Encoder/Decoder
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
 */

public class RS implements Settings {
    /* Encoder parity bytes */
    final int[] pBytes = new int[Settings.kMaxDeg];

    /* Decoder syndrome bytes */
    final int[] synBytes = new int[Settings.kMaxDeg];

    /* generator polynomial */
    final int[] genPoly = new int[Settings.kMaxDeg * 2];

    /* Initialize lookup tables, polynomials, etc. */
    public RS() {
        /* Initialize the galois field arithmetic tables */
        Galois.init_galois_tables();

        /* Compute the encoder generator polynomial */
        compute_genpoly(Settings.kParityBytes, genPoly);
    }

    static void zero_fill_from(byte[] buf, int from, int to) {
        for (int i = from; i < to; i++)
            buf[i] = 0;
    }

    /* debugging routines */
    void print_parity() {
        System.out.print("Parity Bytes: ");
        for (int i = 0; i < Settings.kParityBytes; i++)
            System.out.print("[" + i + "]: 0x" + Integer.toHexString(pBytes[i])
                    + ", ");
        System.out.println();
    }

    void print_syndrome() {
        System.out.print("Syndrome Bytes: ");
        for (int i = 0; i < Settings.kParityBytes; i++)
            System.out.print("[" + i + "]: 0x" + Integer.toHexString(synBytes[i])
                    + ", ");
        System.out.println();
    }

    /* Append the parity bytes onto the end of the message */
    void build_codeword(byte[] msg, int nbytes, byte[] codeword) {
        int i;

        for (i = 0; i < nbytes; i++)
            codeword[i] = msg[i];

        for (i = 0; i < Settings.kParityBytes; i++) {
            codeword[i + nbytes] = (byte) pBytes[Settings.kParityBytes - 1 - i];
        }
    }

    /**********************************************************
     * Reed Solomon Decoder
     *
     * Computes the syndrome of a codeword. Puts the results into the synBytes[]
     * array.
     */

    public void decode(byte[] codeword, int nbytes) {
        for (int j = 0; j < Settings.kParityBytes; j++) {
            int sum = 0;
            for (int i = 0; i < nbytes; i++) {
                // !!!: byte-ify
                sum = (0xFF & codeword[i]) ^ Galois.gmult(Galois.gexp[j + 1], sum);
            }
            synBytes[j] = sum;
        }
    }

    /* Check if the syndrome is zero */
    int check_syndrome() {
        int nz = 0;
        for (int i = 0; i < Settings.kParityBytes; i++) {
            if (synBytes[i] != 0) {
                nz = 1;
                break;
            }
        }
        return nz;
    }

    void debug_check_syndrome() {

        for (int i = 0; i < 3; i++) {
            System.out.println(" inv log S["
                    + i
                    + "]/S["
                    + (i + 1)
                    + "] = "
                    + Galois.glog[Galois.gmult(synBytes[i],
                    Galois.ginv(synBytes[i + 1]))]);
        }
    }

    /*
     * Create a generator polynomial for an n byte RS code. The coefficients are
     * returned in the genPoly arg. Make sure that the genPoly array which is
     * passed in is at least n+1 bytes long.
     */

    static void compute_genpoly(int nbytes, int[] genpoly) {
        int[] tp1 = new int[256];

        /* multiply (x + a^n) for n = 1 to nbytes */

        Berlekamp.zero_poly(tp1);
        tp1[0] = 1;

        int[] tp = new int[256];
        for (int i = 1; i <= nbytes; i++) {
            Berlekamp.zero_poly(tp);
            tp[0] = Galois.gexp[i]; /* set up x+a^n */
            tp[1] = 1;

            Berlekamp.mult_polys(genpoly, tp, tp1);
            Berlekamp.copy_poly(tp1, genpoly);
        }
    }

    /*
     * Simulate a LFSR with generator polynomial for n byte RS code. Pass in a
     * pointer to the data array, and amount of data.
     *
     * The parity bytes are deposited into pBytes[], and the whole message and
     * parity are copied to dest to make a codeword.
     */

    public void encode(byte[] msg, int nbytes, byte[] codeword) {
        int i;
        int[] LFSR = new int[Settings.kParityBytes + 1];

        for (i = 0; i < Settings.kParityBytes + 1; i++)
            LFSR[i] = 0;

        for (i = 0; i < nbytes; i++) {
            // !!!: byte-ify
            int dbyte = ((msg[i] ^ LFSR[Settings.kParityBytes - 1]) & 0xFF);
            for (int j = Settings.kParityBytes - 1; j > 0; j--) {
                LFSR[j] = LFSR[j - 1] ^ Galois.gmult(genPoly[j], dbyte);
            }
            LFSR[0] = Galois.gmult(genPoly[0], dbyte);
        }

        for (i = 0; i < Settings.kParityBytes; i++)
            pBytes[i] = LFSR[i];

        build_codeword(msg, nbytes, codeword);
    }
}