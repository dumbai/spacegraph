//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package spacegraph.audio.modem.chirp.transceive.util;

//import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;

import java.util.Arrays;

public class AudioEvent {
    private final AudioFormat format;
    //    private final TarsosDSPAudioFloatConverter converter;
    private float[] floatBuffer;
    private byte[] byteBuffer;
    private int overlap;
    private long frameLength;
    private long bytesProcessed;
    private int bytesProcessing;

    public AudioEvent(AudioFormat var1) {
        this.format = var1;
//        this.converter = TarsosDSPAudioFloatConverter.getConverter(var1);
        this.overlap = 0;
    }
    public AudioEvent(AudioFormat var1, float[] data) {
        this(var1);

        setFloatBuffer(data);
    }

    private static double calculateRMS(float[] var0) {
        double var1 = 0.0D;

        for (float v : var0) {
            var1 += v * v;
        }

        var1 /= var0.length;
        var1 = Math.sqrt(var1);
        return var1;
    }

    private static double localEnergy(float[] var1) {
        double var2 = 0.0D;
        float[] var4 = var1;
        int var5 = var1.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            float var7 = var4[var6];
            var2 += var7 * var7;
        }

        return var2;
    }

    private static double linearToDecibel(double var1) {
        return 20.0D * Math.log10(var1);
    }

    public float getSampleRate() {
        return this.format.getSampleRate();
    }

    public int getBufferSize() {
        return this.getFloatBuffer().length;
    }

    public long getFrameLength() {
        return this.frameLength;
    }

    public int getOverlap() {
        return this.overlap;
    }

    public void setOverlap(int var1) {
        this.overlap = var1;
    }

    public void setBytesProcessed(long var1) {
        this.bytesProcessed = var1;
    }

    public double getTimeStamp() {
        return (this.bytesProcessed / (double) this.format.getFrameSize()) / this.format.getSampleRate();
    }

//    public byte[] getByteBuffer() {
//        int var1 = this.getFloatBuffer().length * this.format.getFrameSize();
//        if (this.byteBuffer == null || this.byteBuffer.length != var1) {
//            this.byteBuffer = new byte[var1];
//        }
//
//        this.converter.toByteArray(this.getFloatBuffer(), this.byteBuffer);
//        return this.byteBuffer;
//    }

//    public double getEndTimeStamp() {
//        return ((this.bytesProcessed + (double) this.bytesProcessing) / (long) this.format.getFrameSize()) / this.format.getSampleRate();
//    }
//
//    public long getSamplesProcessed() {
//        return this.bytesProcessed / (long) this.format.getFrameSize();
//    }

    public double getProgress() {
        return(this.bytesProcessed / (double) this.format.getFrameSize()) / this.frameLength;
    }

    public float[] getFloatBuffer() {
        return this.floatBuffer;
    }

    public void setFloatBuffer(float[] var1) {
        this.floatBuffer = var1;
    }

    public double getRMS() {
        return calculateRMS(this.floatBuffer);
    }

    public double getdBSPL() {
        return AudioEvent.soundPressureLevel(this.floatBuffer);
    }

    public void clearFloatBuffer() {
        Arrays.fill(this.floatBuffer, 0.0F);
    }

    private static double soundPressureLevel(float[] var1) {
        double var2 = Math.pow(AudioEvent.localEnergy(var1), 0.5D);
        var2 /= var1.length;
        return AudioEvent.linearToDecibel(var2);
    }

    public boolean isSilence(double var1) {
        return AudioEvent.soundPressureLevel(this.floatBuffer) < var1;
    }

    public void setBytesProcessing(int var1) {
        this.bytesProcessing = var1;
    }
}
