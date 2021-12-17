package spacegraph.audio.modem.stopcollaboratelisten;

/**
 * Copyright 2002 by the authors. All rights reserved.
 * <p>
 * Author: Cristina V Lopes
 */


import jcog.Util;

import javax.sound.sampled.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author CVL
 */

public enum AudioUtils {
    ;

	/*
    //the default format for reading and writing audio information
    public static AudioFormat kDefaultFormat = new AudioFormat((float) Encoder.kSamplingFrequency,
							       (int) 8, (int) 1, true, false);
	 */
    
	/*
    public static void decodeWavFile(File inputFile, OutputStream out)
	throws UnsupportedAudioFileException,
	IOException {
	StreamDecoder sDecoder = new StreamDecoder(out);
	AudioBuffer aBuffer = sDecoder.input();

	AudioInputStream audioInputStream = 
	    AudioSystem.getAudioInputStream(kDefaultFormat, 
					    AudioSystem.getAudioInputStream(inputFile));
	int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
	// Set an arbitrary buffer size of 1024 frames.
	int numBytes = 1024 * bytesPerFrame; 
	byte[] audioBytes = new byte[numBytes];
	int numBytesRead = 0;
	// Try to read numBytes bytes from the file and write it to the buffer
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
	  
	    aBuffer.write(audioBytes, 0, numBytesRead);
	}
    }
	 */
	
	/*
    public static void writeWav(File file, byte[] data, AudioFormat format)
	throws IllegalArgumentException,
	IOException {
	ByteArrayInputStream bais = new ByteArrayInputStream(data);
	AudioInputStream ais = new AudioInputStream(bais, format, data.length);
	AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
    }
    */
	
    /*
    public static void displayMixerInfo(){
	Mixer.Info[] mInfos = AudioSystem.getMixerInfo();
	if(mInfos == null){
	    System.out.println("No Mixers found");
	    return;
	}

	for(int i=0; i < mInfos.length; i++){
	    System.out.println("Mixer Info: " + mInfos[i]);
	    Mixer mixer = AudioSystem.getMixer(mInfos[i]);
	    Line.Info[] lines = mixer.getSourceLineInfo();
	    for(int j = 0; j < lines.length; j++){
		System.out.println("\tSource: " + lines[j]);
	    }
	    lines = mixer.getTargetLineInfo();
	    for(int j = 0; j < lines.length; j++){
		System.out.println("\tTarget: " + lines[j]);
	    }
	}
    }
	*/
	
	/*
    public static void displayAudioFileTypes(){
	AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
	for(int i=0; i < types.length; i++){
	    System.out.println("Audio File Type:" + types[i].toString());
	}
    }
	*/
	
	/*
    //This never returns, which is kind of lame.
    // NOT USED!! - replaced by MicrophoneListener.run()
    public static void listenToMicrophone(AudioBuffer buff){
	try {
	    int buffSize = 4096;
	    TargetDataLine line = getTargetDataLine(kDefaultFormat);
	    line.open(kDefaultFormat, buffSize);

	    ByteArrayOutputStream out  = new ByteArrayOutputStream();
	    int numBytesRead;
	    byte[] data = new byte[line.getBufferSize() / 5];
	    line.start();
	    while(true){
		numBytesRead =  line.read(data, 0, data.length);
		buff.write(data, 0, numBytesRead);
	    }
	    
	} catch (Exception e){
	    System.out.println(e.toString());
	}
    }
	*/
	
	/*
    public static void recordToFile(File file, int length){
	try {
	    int buffSize = 4096;
	    TargetDataLine line = getTargetDataLine(kDefaultFormat);
	    line.open(kDefaultFormat, buffSize);

	    ByteArrayOutputStream out  = new ByteArrayOutputStream();
	    int numBytesRead;
	    byte[] data = new byte[line.getBufferSize() / 5];
	    line.start();
	    for(int i=0; i < length; i++) {
		numBytesRead =  line.read(data, 0, data.length);
		out.write(data, 0, numBytesRead);
	    }
	    line.drain();
	    line.stop();
	    line.close();
	    
	    writeWav(file, out.toByteArray(), kDefaultFormat);

	} catch (Exception e){
	    System.out.println(e.toString());
	}
    }
	*/
	
	/*
    public static TargetDataLine getTargetDataLine(AudioFormat format)
	throws LineUnavailableException {
	DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
	if (!AudioSystem.isLineSupported(info)) {
	    throw new LineUnavailableException();
	}	
	return (TargetDataLine) AudioSystem.getLine(info);
    }

    public static SourceDataLine getSourceDataLine(AudioFormat format)
	throws LineUnavailableException {
	DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
	if (!AudioSystem.isLineSupported(info)) {
	    throw new LineUnavailableException();
	}
	return (SourceDataLine) AudioSystem.getLine(info);
    }
	*/


//    public static void encodeFileToWav(File inputFile, File outputFile)
//            throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Encoder.encodeStream(new FileInputStream(inputFile), baos);
//        // patched out for Android
//        // writeWav(outputFile, baos.toByteArray());
//    }


    //the default format for reading and writing audio information
    public static AudioFormat kDefaultFormat = new AudioFormat(Constants.kSamplingFrequency,
            8, 1, true, false);

    public static void performData(byte[] data) {
        //For some reason line.write seems to affect the data
        //to avoid the side effect, we copy it
        byte[] dataCopy = new byte[data.length];
        System.arraycopy(data, 0, dataCopy, 0, data.length);

        SourceDataLine line = null;
        try {
            line = getSourceDataLine(kDefaultFormat);
            line.open(kDefaultFormat);
        } catch (LineUnavailableException ex) {
            System.out.println("Line Unavailable: " + ex);
            return;
        }
        line.start();
        line.write(dataCopy, 0, dataCopy.length);
        line.drain();
        line.stop();
        line.close();
    }

    public static void performSOS() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Encoder.generateSOS(baos);
        performData(baos.toByteArray());
    }

    public static void decodeWavFile(File inputFile, ByteArrayOutputStream out)
            throws UnsupportedAudioFileException,
            IOException {

        StringBuilder msg = new StringBuilder();

        StreamDecoder sDecoder = new StreamDecoder(out) {

            @Override
            protected void onError() {
                System.err.println("err");
            }

            @Override
            protected void onSOS() {
                System.out.println("recv: SOS");
            }

            @Override
            protected void onBroadcast(byte[] receivedBytes) {
                System.out.println("recv: " + new String(receivedBytes));
                msg.append(new String(receivedBytes));
                stop();
            }
        };

        AudioInputStream audioInputStream =
                AudioSystem.getAudioInputStream(kDefaultFormat,
                        AudioSystem.getAudioInputStream(inputFile));

//        int bytesPerFrame = audioInputStream.getFormat().getFrameSize();
        // Set an arbitrary buffer size of 1024 frames.
//        int numBytes = 1024 * bytesPerFrame;
//        byte[] audioBytes = new byte[numBytes];
//        int numBytesRead = 0;
        // Try to read numBytes bytes from the file and write it to the buffer

        audioInputStream.transferTo(sDecoder.input().baos);

//        while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
//	  /*
//	    for(int i=0; i < numBytesRead; i++){
//		float val = audioBytes[i] / (float)Constants.kFloatToByteShift;
//		//System.out.println("" + val);
//	    }
//	  */
//            sDecoder.input().write(audioBytes, 0, numBytesRead);
//        }

        //audioInputStream.close();

        //sDecoder.stop();
        Util.sleepMS(1000);
        assertEquals("test", msg.toString());
    }

    public static void writeWav(File file, byte[] data, AudioFormat format) throws IllegalArgumentException, IOException {

        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        ais.close();


    }

    public static void displayMixerInfo() {
        Mixer.Info[] mInfos = AudioSystem.getMixerInfo();
        if (mInfos == null) {
            System.out.println("No Mixers found");
            return;
        }

        for (Mixer.Info mInfo : mInfos) {
            System.out.println("Mixer Info: " + mInfo);
            Mixer mixer = AudioSystem.getMixer(mInfo);
            Line.Info[] lines = mixer.getSourceLineInfo();
            for (Line.Info info : lines) {
                System.out.println("\tSource: " + info);
            }
            lines = mixer.getTargetLineInfo();
            for (Line.Info line : lines) {
                System.out.println("\tTarget: " + line);
            }
        }
    }

    public static void displayAudioFileTypes() {
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        for (AudioFileFormat.Type type : types) {
            System.out.println("Audio File Type:" + type);
        }
    }

    //This never returns, which is kind of lame.
    // NOT USED!! - replaced by MicrophoneListener.run()
    public static void listenToMicrophone(ByteArrayAudioOutputStream buff) {
        try {
            int buffSize = 4096;
            TargetDataLine line = getTargetDataLine(kDefaultFormat);
            line.open(kDefaultFormat, buffSize);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] data = new byte[line.getBufferSize() / 5];
            line.start();
            while (true) {
                int numBytesRead = line.read(data, 0, data.length);
                buff.write(data, 0, numBytesRead);
            }
	    /*
	    line.drain();
	    line.stop();
	    line.close();
	    */
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void recordToFile(File file, int length) {
        try {
            int buffSize = 4096;
            TargetDataLine line = getTargetDataLine(kDefaultFormat);
            line.open(kDefaultFormat, buffSize);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] data = new byte[line.getBufferSize() / 5];
            line.start();
            for (int i = 0; i < length; i++) {
                int numBytesRead = line.read(data, 0, data.length);
                out.write(data, 0, numBytesRead);
            }
            line.drain();
            line.stop();
            line.close();

            writeWav(file, out.toByteArray(), kDefaultFormat);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    static TargetDataLine getTargetDataLine(AudioFormat format) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info))
            throw new LineUnavailableException();

        return (TargetDataLine) AudioSystem.getLine(info);
    }

    static SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info))
            throw new LineUnavailableException();

        return (SourceDataLine) AudioSystem.getLine(info);
    }

    public static void encodeFileToWav(File inputFile, File outputFile)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Encoder.encodeStream(new FileInputStream(inputFile), baos);
        writeWav(outputFile, baos.toByteArray(), kDefaultFormat);
    }


    public static void performFile(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Encoder.encodeStream(new FileInputStream(file), baos);
        performData(baos.toByteArray());
    }

    /**
     * A thread safe buffer for audio samples
     * NOTE: This has no hard limits for memory usage
     *
     * @author CVL
     */
    public static class ByteArrayAudioOutputStream {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public ByteArrayAudioOutputStream() {
        }

        /**
         * @param input an array to write to the end of the buffer
         */
        public synchronized void write(byte[] input)
                throws IOException {
            baos.write(input);
        }

        /**
         * @param input  the source array
         * @param offset the offset into the array from which to start copying
         * @param length the length to copy
         */
        public synchronized void write(byte[] input, int offset, int length) {
            baos.write(input, offset, length);
        }

        public synchronized byte[] read(int n) {
            return read(n, 1.0f);
        }

        /**
         * @param n the number of bytes to try to read (nondestructively)
         * @return if the buffer.size >= n, return the requested byte array, otherwise null
         * <p>
         * NOTE: THIS DOES NOT REMOVE BYTES FROM THE BUFFER
         */
        public synchronized byte[] read(int n, float requiredPresent) {
            if (baos.size() < n * requiredPresent) {
                return null;
            }
            byte[] result = ArrayUtils.subarray(baos.toByteArray(), 0, n);
            return result;
        }

        public synchronized byte[] readAll() {
            return baos.toByteArray();
        }

        /**
         * @param n the number of bytes to remove from the buffer.
         *          If n > buffer.size, it has the same effect as n = buffer.size.
         */
        public synchronized void delete(int n)
                throws IOException {
            if (n <= 0) {
                return;
            }
            if (baos.size() < n) {
                baos.reset();
                return;
            }
            byte[] buff = ArrayUtils.subarray(baos.toByteArray(), n - 1, baos.size() - n);
            baos.reset();
            baos.write(buff);
        }

        /**
         * @return the current size of the buffer
         */
        public synchronized int size() {
            return baos.size();
        }
    }
}