package spacegraph.input.finger;

import jcog.exe.Loop;
import jcog.math.v2;
import spacegraph.SpaceGraph;
import spacegraph.layer.OrthoSurfaceGraph;

/** satellite cursor attached to a Finger */
public abstract class SubFinger extends Finger {

    public final Finger parent;
    final v2 posRel = new v2();
    private SpaceGraph root;

    public SubFinger(Finger parent, int buttons) {
        super(buttons);
        this.parent = parent;
    }

    @Override
    public final v2 posGlobal() {
        return parent.posGlobal().addToNew(posRel);
    }

    @Override
    protected void start(SpaceGraph x) {
        this.root = x;
    }

    public void update() {
        if (isOn()) {
            float p = ((OrthoSurfaceGraph) root).window.getWidthHeightMin();
            posPixel.set(posRel.clone().scaled(p).added(parent.posPixel));
        }
    }

    /** satellite sub-finger orbiting a parent finger */
    public static class PolarSubFinger extends SubFinger {

        private Loop on;
//        final Random rng = new XoRoShiRo128PlusRandom();

        /** radius in proportion to of min window dimension */
        float radius = 0.2f;
        public float theta = 0;

        public PolarSubFinger(Finger parent) {
            super(parent, 0);
        }

        @Override
        protected void start(SpaceGraph x) {
            super.start(x);
            on = Loop.of(this::update).fps(20.0f);
        }

        @Override public void update() {

            focused.set(parent.focused());

            float rotSpeed = 0.05f;
            theta += rotSpeed; //rotSpeed * (rng.nextFloat() * 2 - 1);
            posRel.set( radius * Math.cos(theta), radius * Math.sin(theta) );

            super.update();
        }

        @Override
        protected void stop(SpaceGraph x) {
            on.stop();
            on = null;
        }
    }
}