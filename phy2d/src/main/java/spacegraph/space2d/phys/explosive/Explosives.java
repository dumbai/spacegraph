//package spacegraph.space2d.phys.explosive;
//
//import com.jogamp.opengl.GL2;
//import jcog.exe.Loop;
//import jcog.random.XoRoShiRo128PlusRandom;
//import spacegraph.SpaceGraph;
//import spacegraph.space2d.phys.callbacks.ContactImpulse;
//import spacegraph.space2d.phys.callbacks.ContactListener;
//import spacegraph.space2d.phys.collision.Manifold;
//import spacegraph.space2d.phys.collision.shapes.CircleShape;
//import spacegraph.space2d.phys.collision.shapes.PolygonShape;
//import spacegraph.space2d.phys.dynamics.*;
//import spacegraph.space2d.phys.dynamics.contacts.Contact;
//import spacegraph.space2d.widget.windo.Dyn2DSurface;
//import spacegraph.util.math.Tuple2f;
//import spacegraph.util.math.v2;
//import spacegraph.video.Draw;
//
//import java.util.Random;
//import java.util.function.Consumer;
//
//import static spacegraph.space2d.phys.dynamics.Dynamics2D.staticBox;
//
//public class Explosives {
//
//    private final static Random rng = new XoRoShiRo128PlusRandom(1);
//
//    /**
//     * TODO bullet hard TTL in case it goes off to infinity
//     */
//    public static class Gun {
//
//        public final Body2D barrel;
//        public float barrelLength;
//        float barrelThick;
//
//        /**
//         * in radians; more inaccuracy = more randomized direction spraying
//         */
//        float inaccuracy = 0.03f;
//
//
//        long lastFire;
//        long minFirePeriodMS = 200;
//
//        public Gun(float barrelLength, Dynamics2D world) {
//            this.barrelLength = barrelLength;
//            this.barrelThick = barrelLength * 0.2f;
//            this.lastFire = world.realtimeMS;
//            barrel = world.addBody(new BodyDef(BodyType.DYNAMIC),
//                    new FixtureDef(PolygonShape.box(barrelLength / 2, barrelThick / 2), 0.1f, 0f));
//
//        }
//
//
//        public void fire() {
//
//
//            if (barrel.W.realtimeMS - lastFire < minFirePeriodMS) {
//                return;
//            } else {
//                this.lastFire = barrel.W.realtimeMS;
//            }
//
//            float bulletLength = barrelThick * 2f;
//
//
//
//            float heading = barrel.angle() + 2 * (rng.nextFloat() - 0.5f) * inaccuracy;
//            v2 direction = new v2((float) Math.cos(heading), (float) Math.sin(heading));
//
//            float power = barrelThick*2000;
//
//            float bulletThick = barrelThick / 2 * 0.25f;
//
//            PolygonShape pos = PolygonShape.box(bulletLength / 2, bulletThick);
//            Body2D projectile = new Projectile(barrel.W);
//
//            projectile.setBullet(true);
//
//            FixtureDef ff = new FixtureDef(pos, 0.01f, 0f);
//            ff.restitution = 0.9f;
//
//            barrel.W.addBody(projectile, ff);
//
//
//
//
//            projectile.setTransform(barrel.pos.addAt(direction.scaled((barrelLength / 2f) + bulletLength)), heading);
//
//
//
//            {
//
//                projectile.applyForceToCenter(direction.scaled(power));
//
//
//                barrel.applyForceToCenter(direction.scaled(-1));
//            }
//
//
//
//        }
//
//    }
//
//    public static class Projectile extends Body2D implements Consumer<GL2> {
//        long start, end;
//
//        Projectile(Dynamics2D w) {
//            super(BodyType.DYNAMIC, w);
//
//            start = System.currentTimeMillis();
//            long ttl = 3 * 1000;
//            end = start + ttl;
//        }
//
//        @Override
//        public boolean preUpdate() {
//            return W.realtimeMS <= end;
//        }
//
//        @Override
//        protected void onRemoval() {
//
//            W.invoke(() -> {
//                    int blasts = 1;
//                    float bulletRadius = 0.2f;
//                    float blastScatter = bulletRadius * (blasts - 1);
//                    float blastRadius = bulletRadius;
//                    for (int i = 0; i < blasts; i++) {
//                        W.addBody(new Fireball(W, getWorldCenter().addAt(
//                                new v2((float) rng.nextGaussian() * blastScatter, (float) rng.nextGaussian() * blastScatter)),
//                                blastRadius));
//                    }
//                });
//        }
//
//        @Override
//        public void accept(GL2 gl) {
//            Fixture f = fixtures;
//            if (f != null) {
//                gl.glColor3f(0.75f, 0.75f, 0.75f);
//                Draw.poly(this, gl, 1f /* TODO W.scaling */, (PolygonShape) f.shape);
//            }
//        }
//    }
//
//    /**
//     * expanding shockwave, visible
//     */
//    public static class Fireball extends Body2D implements Consumer<GL2> {
//
//        private final float maxRad;
//        private final CircleShape shape;
//        private float rad;
//
//        Fireball(Dynamics2D w, Tuple2f center, float maxRad) {
//            super(new BodyDef(
//                    BodyType.KINEMATIC
//
//            ), w);
//
//            this.maxRad = maxRad;
//
//            shape = new CircleShape();
//            rad = shape.radius = 0.05f;
//
//            w.addBody(this,
//                    new FixtureDef(shape, 0.001f, 0.1f));
//            this.setTransform(center, 0);
//        }
//
//        @Override
//        public boolean preUpdate() {
//            if (rad < maxRad) {
//                Fireball.this.rad *= 1.2f;
//
//                    updateFixtures((f) -> {
//                        shape.radius = rad;
//                        f.setShape(shape);
//                    });
//
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        @Override
//        public void accept(GL2 gl) {
//
//            CircleShape circle = shape;
//            float r = circle.radius;
//            v2 v = new v2();
//            getWorldPointToOut(circle.center, v);
//
//
//
//            Draw.colorUnipolarHue(gl, rng.nextFloat(), 0.1f, 0.3f, 0.8f);
//            Draw.circle(gl, v, true, r, 9);
//
//        }
//    }
//
//
//    public static void main(String[] args) {
//        Dyn2DSurface p = SpaceGraph.wall(1200, 1000);
//
//        Dynamics2D w = p.W;
//        w.setContactListener(new ExplosionContacts());
//
//
//
//        staticBox(w, -8, -4, 8, 4);
//
//
//        Gun g = new Gun(1f, w);
//        Loop.of(g::fire).setFPS(10f);
//
//
//    }
//
//    public static class ExplosionContacts implements ContactListener {
//
//        void explode(Body2D b, Body2D hit) {
//
//
//
//
//            b.remove();
//
//        }
//
//        @Override
//        public boolean beginContact(Contact contact) {
//            Body2D a = contact.aFixture.body;
//            Body2D b = contact.bFixture.body;
//
//            if (a instanceof Projectile) {
//                if (b instanceof Fireball || b instanceof Projectile) {
//
//                    return false;
//                }
//                explode(a, b);
//            }
//            if (b instanceof Projectile) {
//                if (a instanceof Fireball || a instanceof Projectile) {
//
//                    return false;
//                }
//                explode(b, a);
//            }
//            return true;
//        }
//
//        @Override
//        public void endContact(Contact contact) {
//
//        }
//
//        @Override
//        public void preSolve(Contact contact, Manifold oldManifold) {
//
//        }
//
//        @Override
//        public void postSolve(Contact contact, ContactImpulse impulse) {
//
//        }
//    }
//}
