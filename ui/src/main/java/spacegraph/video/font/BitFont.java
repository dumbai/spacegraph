package spacegraph.video.font;

import java.util.Arrays;

/**
 * https:
 * TODO render Glyphs, this currently only decodes the base64 font present in the strings
 */
public class BitFont {

    public static final String standard58base64 = "AakACQBgACAEAgQGBggGAgMDBAYDBAIGBQMFBQUFBQUFBQICBAUEBQgFBQUFBQUFBQIFBQQGBQUFBQUFBAUGCAUGBQMFAwYGAwQEBAQEBAQEAgQEAgYEBAQEAwQEBAQGBAQEBAIEBQKgUgghIaUAAIiRMeiZZwwAAANgjjnvmRRKESVzzDGXoqQUvYURQCCAQCCSCAAAAAgAAABEqECleCVFkRAAiLSUWEgoJQAAiSOllEJIKVRiSymllCRFSSlCEVIAQQBBQAARAAAAEAAAACQpgeALJASiIwAQSQipE1BKRS+QSEohhRBSqES1UkopSIqSkkIiFAGwEZOwSaplZGx2VVXVSQIAgeIgSETy4RCSCEnoEONAgJCkd0I6p73QiKilk46RpCQZQoQIAFBVVVOVVFVVVUKqqiqKCACCDyKpiIoAICQJ9FAiCUE8ElUphRRCSqESUUohJSRJSUpECBEAoCrqoiqZqqqqiFRVUiIJAADKI5UQASEgSAoJpSRSCgECUlJKKYSUSiWilEJKSRKRlIgQJABAVVVEVVJVVVUhqaqqQhIACBQixEIBQFBg9AwyRhhDBEIIpGPOCyZl0kXJBJOMGMImEW9owAcbMQmrpKpKxjJiopQdFQAAAAAAAABAAAAAAAAAAIAAAOAAAAAAAAAAAAAACAAAAAAAAAAAAAAAAAQIAAAEAQAAAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAgAAAgCAAAAAgAA";
    public static final String standard56base64 = "AeYACQBgACAEAgQGBggHAgMDBgYDBQIFBgMGBgYGBgYGBgIDBAYEBggGBgYGBgYGBgIGBgUIBgYGBgYGBgYGCAYGBgMFAwYHAwUFBQUFAwUFAgMFAggFBQUFBAQEBQUIBQUFBAMEBQKgUgghRwoBAIAcOQ7yOZ/jAADAAXAe5/k+JwqKQlDkPM7jfFGUFEXfwghAQAAICIQUAgAAAAABAAAAQAkVqBSvJFJUEQCQaFHEBBEURQAAiDiiKIqCIIqCkjAWRVEURUQUJUURFCEFIBAAAgEBhAAAAABAAAAAAEikBIIvkFAQOQQAJBIEKU8ARVGiLyCRKAqiIAiioCJUTVEURQERRUmKgkQoAsAd40zcSambY447u5SSUnoSAYBAcRBMRNWHh4iEMAn0II4HBBAk6XuC6HmyL2gISVX0RI9DREoSQRAhAgBIKaW0lFIpKaWUIiSlpJRQhAAg+CCSFBFBACAiEdAHRUgEgfiIqIqiIAqCKAoqQlAWBVEBEZGSpBBCiAAAUgrpJaU0SkoppRBJKckkIxEAAJRHKkIEEACESEKERBERRUEAAVKiKIqCIIqKkhAURUGUREREJEVEECQAgJRSCkkplZJSSilIUkpKKUgEAAKFCHGhAIBAwdHnII5DOA4iIAiB6HGeL3CinOgFRU7gRA7hEDYR8QUJ+MEd40xcSqmkZI6LEWdsknsSAQAAAAAAAAAgAAAAAAAAAACAAACAAwAAAAAAAAAAAAAAQAAAAAAAAAADAwAAAAAABBAAAICAAAAAAIAAJQAAAAAAAAAABAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwAACAAAgIAAAAAAYAAA=";
    public static final String grixelbase64 = "AnoADABgACAFAgQICAoIAgQEBgYDBQIKCQMICAgICAcICAIDBQYFBwkICAgIBwcICAYHCAcJCAgICAgICAgICggICAQKBAQHBAcHBwcHBQcHAgUHBAoHBwcHBgcGBwcKBwcHBQIFCAJAJeIjkENBAAAAQHzk4wPz5/Pz8QEAAB4ePj8+Pz6fX9AHCgoECvL58fnx+QsKiigo6C8CIAEIIAAAARwgEAoEAAAAAAAABAAAAAAAICIAAZVIUiERBQEAAIAIWlAQSkAQKCgIICCEhAQFBQUFAgFBBCgoMGwoKCgoKAghKCiioCCgEIAKQIAAAAQIgAAgEAAAAAAAABAAAAAAAICIsAUEfwlCRBCkEAAAIUhAQCQBAaCgIEAAAcoUFBQQFAgEBBGgoECpoqCgoKAAhKCgiEREQIIAAgAAAgAQIAACgEAAAAAAAABAAAAAAAAAIrIBEIgkgBBBEEEAAIIgAQGJ/ARAgoKS+AioVFBQQFAgEBBEgEICmZKCgoKCAhCCgiKioIAIBAgA4Pl4fJ7n+YRC8c7H8/F5ni8UiigU+okIAEAg4gOBA0HfhwcEguTDEwL0g/DxAwFAoFJ/PwFBv1/eHwH6CASKCgoKCvJBCAqKCAEBISAgAAAoFAqFQigUikREoVAoFISEUCgiSQgSQgAAgQgSAlEEEQQACAhSANAfUBAhCAiIj2BKBQUFBAUCQUEEKCQQKCzoJ+gHCCEoKCIKBIIAgQAAvlAg9AuhUOgREYVCoVBgEEKhiBghhIgAAAB/SITEEKQQABAgSAFAIEBBhCAgQABByBMUFBAUCAQFEaGgQKCgoICgECCEIJGIRBAEAggCAIRCgVAghEKhSEQUCoVCAUYIhSJihAgiAgAAiCQJFUMQAAgggCAFBIEEBRGCghACAkBAUFBQUCAQFESEggKBgoICkoKCEIIoIgpCCAhACAAQCoVCoRAKhUIRUSgUCgUhISSJSBISiAgAQCDiE4gTQQAgUAB89OcD4uND8PFJAAAEfkE/Pj++gF/Q5wn6BQryCfAJ8kHwQXAnCOEvACIAgM/j8XiCLxQKWUQhz8cXeDgPw52Q7yciAAAAAAIAANgAQAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAAgAPg4AcAAAAAACAACAAAAAABEAAAAAAAACAAawAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB4ABgAAAAABEAAAAAAAAB4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    static final int defaultChar = 32;
    final int[] charWidth = new int[255];
    private final float[] texture;
    private final Glyph[] glyphs;
    int characters;
    int charHeight;
    int[][] chars;
    int lineHeight = 10;
    protected int kerning;
    protected int wh;
    private int textureHeight;
    private int textureWidth;

    public BitFont(byte[] theBytes) {
        super();

        texture = decodeBitFont(theBytes);
        make();

        int size = lineHeight;
        glyphs = new Glyph[256];
        int[] ascii = new int[128];
        Arrays.fill(ascii, -1);
        boolean lazy = false;
        int ascent = 4;
        int descent = 4;
        int glyphCount = 128;
        for (int i = 0; i < 128; i++) {


            glyphs[i] = new Glyph();


            glyphs[i].value = i;

            if (glyphs[i].value < 128) {
                ascii[glyphs[i].value] = i;
            }

            glyphs[i].index = i;
            int id = i - 32;
            if (id >= 0) {
                glyphs[i].image = new float[charWidth[id] * 9];
                for (int n = 0; n < chars[id].length; n++) {
                    glyphs[i].image[n] = (chars[id][n] == 1) ? 0xffffffff : 0x00000000;
                }
                glyphs[i].height = 9;
                glyphs[i].width = charWidth[id];
                glyphs[i].index = i;
                glyphs[i].value = i;
                glyphs[i].setWidth = charWidth[id];
                glyphs[i].topExtent = 4;
                glyphs[i].leftExtent = 0;
            } else {
                glyphs[i].image = new float[1];
            }
        }
    }

    static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < 2; i++) {
            int shift = (2 - 1 - i) * 8;
            value += (b[i] & 0x00FF) << shift;
        }
        return value;
    }

    static int getBit(int theByte, int theIndex) {
        int bitmask = 1 << theIndex;
        return ((theByte & bitmask) > 0) ? 1 : 0;
    }

    public Glyph getGlyph(char c) {
        int n = c;
        /* if c is out of the BitFont-glyph bounds, return
         * the defaultChar glyph (the space char by
         * default). */
        n = (n >= 128) ? defaultChar : n;
        return glyphs[n];
    }

    float[] decodeBitFont(byte[] bytes) {


        int w = byteArrayToInt(new byte[]{bytes[0], bytes[1]});


        int h = byteArrayToInt(new byte[]{bytes[2], bytes[3]});


        int s = byteArrayToInt(new byte[]{bytes[4], bytes[5]});


        int c = byteArrayToInt(new byte[]{bytes[6], bytes[7]});

        textureWidth = w;
        textureHeight = h;


        int off = 8 + s;
        float[] tex = new float[w * h];
        for (int i = off; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                tex[(i - off) * 8 + j] = getBit(bytes[i], j) == 1 ? 0xff000000 : 0xffffffff;
            }
        }

        int cnt = 0, n = 0, i = 0;


        for (i = 0; i < s; i++) {
            while (++cnt != bytes[i + 8]) {
            }
            n += cnt;
            tex[n] = 0xffff0000;
            cnt = 0;
        }

        return tex;
    }

    int getHeight() {
        return textureHeight;
    }

    BitFont make() {

        charHeight = textureHeight;

        lineHeight = charHeight;

        int currWidth = 0;

        for (int i = 0; i < textureWidth; i++) {
            currWidth++;
            if (texture[i] == 0xffff0000) {
                charWidth[characters++] = currWidth;
                currWidth = 0;
            }
        }

        chars = new int[characters][];

        int indent = 0;

        for (int i = 0; i < characters; i++) {
            chars[i] = new int[charWidth[i] * charHeight];
            for (int u = 0; u < charWidth[i] * charHeight; u++) {
                chars[i][u] = texture[indent + (u / charWidth[i]) * textureWidth + (u % charWidth[i])] == 0xff000000 ? 1 : 0;
            }
            indent += charWidth[i];
        }
        return this;
    }

    static class Glyph {

        int value;
        int index;
        float[] image;
        int height;
        int width;
        int setWidth;
        int topExtent;
        int leftExtent;

        public void draw(float x, float y, float w, float h) {

        }
    }
}
