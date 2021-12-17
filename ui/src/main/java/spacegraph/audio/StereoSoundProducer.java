package spacegraph.audio;

interface StereoSoundProducer {
	void read(float[] leftBuf, float[] rightBuf, int readRate);
	void skip(int samplesToSkip, int readRate);
}