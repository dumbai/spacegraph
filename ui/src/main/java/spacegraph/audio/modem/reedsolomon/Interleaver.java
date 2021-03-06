package spacegraph.audio.modem.reedsolomon;

/**
 * Copyright Jonas Michel (jonasrmichel@gmail.com) 2012
 * <p>
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * Commercial licensing is available under a separate license, please contact
 * author for details.
 * <p>
 * Source code is available at http://code.google.com/p/mobile-acoustic-modems-in-action/
 * <p>
 * <p>
 * Simple interleaver that uses a "skip factor"
 */
public enum Interleaver {
    ;
    public static final int kSkipFactor = 4;

    public static void interleave(byte[] items) {
        int jump = (int) Math.ceil(items.length / kSkipFactor);
        byte[] result = new byte[items.length];
        int k = 0;
        for (int i = 0; i < jump; ++i) {
            for (int j = i; j < items.length; j += jump) {
                result[k++] = items[j];
            }
        }
        System.arraycopy(result, 0, items, 0, result.length);
    }

    public static void deinterleave(byte[] items) {
        int jump = (int) Math.ceil(items.length / kSkipFactor);
        byte[] result = new byte[items.length];
        int k = 0;
        for (int i = 0; i < jump; ++i) {
            for (int j = i; j < items.length; j += jump) {
                result[j] = items[k++];
            }
        }
        System.arraycopy(result, 0, items, 0, result.length);
    }
}
