package spacegraph.audio;

public interface SoundSource {

	SoundSource center = new SoundSource() {

        @Override
        public float getX(float alpha) {
            return 0;
        }

        @Override
        public float getY(float alpha) {
            return 0;
        }
    };

	float getX(float alpha);
	float getY(float alpha);
}