package spacegraph.space2d.phys.particle;

enum ParticleGroupType {
	;
	/**
     * resists penetration
     */
    public static final int b2_solidParticleGroup = 1;
    /**
     * keeps its shape
     */
    public static final int b2_rigidParticleGroup = 1 << 1;
}
