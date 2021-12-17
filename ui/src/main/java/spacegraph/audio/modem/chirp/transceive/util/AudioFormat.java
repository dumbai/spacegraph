package spacegraph.audio.modem.chirp.transceive.util;

import java.util.*;

public class AudioFormat {
    private final Encoding encoding;
    private final float sampleRate;
    private final int sampleSizeInBits;
    private final int channels;
    private final int frameSize;
    private final float frameRate;
    private final boolean bigEndian;
    private HashMap<String, Object> properties;

    public AudioFormat(Encoding var1, float var2, int var3, int var4, int var5, float var6, boolean var7) {
        this.encoding = var1;
        this.sampleRate = var2;
        this.sampleSizeInBits = var3;
        this.channels = var4;
        this.frameSize = var5;
        this.frameRate = var6;
        this.bigEndian = var7;
        this.properties = null;
    }

    public AudioFormat(Encoding var1, float var2, int var3, int var4, int var5, float var6, boolean var7, Map<String, Object> var8) {
        this(var1, var2, var3, var4, var5, var6, var7);
        this.properties = new HashMap(var8);
    }

    public AudioFormat(float var1, int var2, int var3, boolean var4, boolean var5) {
        this(var4 ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED, var1, var2, var3, var3 != -1 && var2 != -1 ? (var2 + 7) / 8 * var3 : -1, var1, var5);
    }

    private Encoding getEncoding() {
        return this.encoding;
    }

    public float getSampleRate() {
        return this.sampleRate;
    }

    private int getSampleSizeInBits() {
        return this.sampleSizeInBits;
    }

    private int getChannels() {
        return this.channels;
    }

    public int getFrameSize() {
        return this.frameSize;
    }

    private float getFrameRate() {
        return this.frameRate;
    }

    private boolean isBigEndian() {
        return this.bigEndian;
    }

    public Map<String, Object> properties() {
        Object var1;
        var1 = this.properties == null ? new HashMap(0) : this.properties.clone();

        return Collections.unmodifiableMap((Map) var1);
    }

    public Object getProperty(String var1) {
        return Optional.ofNullable(this.properties).map(stringObjectHashMap -> stringObjectHashMap.get(var1)).orElse(null);
    }

    public boolean matches(AudioFormat var1) {
        return Objects.equals(var1.encoding, this.encoding) && (var1.sampleRate == -1.0F || var1.sampleRate == this.sampleRate) && var1.sampleSizeInBits == this.sampleSizeInBits && var1.channels == this.channels && var1.frameSize == this.frameSize && (var1.frameRate == -1.0F || var1.frameRate == this.frameRate) && (var1.sampleSizeInBits <= 8 || var1.bigEndian == this.bigEndian);
    }

    public String toString() {
        String var1 = "";
        if (this.encoding != null) {
            var1 = this.encoding + " ";
        }

        String var2;
        var2 = this.sampleRate == -1.0 ? "unknown sample rate, " : this.sampleRate + " Hz, ";

        String var3;
        var3 = this.sampleSizeInBits == -1.0 ? "unknown bits per sample, " : this.sampleSizeInBits + " bit, ";

        String var4 = switch (this.channels) {
            case 1 -> "mono, ";
            case 2 -> "stereo, ";
            case -1 -> " unknown number of channels, ";
            default -> this.channels + " channels, ";
        };

        String var5;
        var5 = this.frameSize == -1.0 ? "unknown frame size, " : this.frameSize + " bytes/frame, ";

        String var6 = "";
        if (Math.abs(this.sampleRate - this.frameRate) > 1.0E-5D) {
            var6 = this.frameRate == -1.0 ? "unknown frame rate, " : this.frameRate + " frames/second, ";
        }

        String var7 = "";
        if ((this.encoding.equals(Encoding.PCM_SIGNED) || this.encoding.equals(Encoding.PCM_UNSIGNED)) && (this.sampleSizeInBits > 8 || this.sampleSizeInBits == -1)) {
            var7 = this.bigEndian ? "big-endian" : "little-endian";
        }

        return var1 + var2 + var3 + var4 + var5 + var6 + var7;
    }

    public static class Encoding {
        public static final Encoding ULAW = new Encoding("ULAW");
        public static final Encoding ALAW = new Encoding("ALAW");
        static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");
        static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");
        private final String name;

        public Encoding(String var1) {
            this.name = var1;
        }

        public final boolean equals(Object var1) {
            return this.toString() != null ? var1 instanceof Encoding && this.toString().equals(var1.toString()) : var1 != null && var1.toString() == null;
        }

        public final int hashCode() {
            return this.toString() == null ? 0 : this.toString().hashCode();
        }

        public final String toString() {
            return this.name;
        }
    }
}
