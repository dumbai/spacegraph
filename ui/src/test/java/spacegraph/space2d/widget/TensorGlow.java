//package spacegraph.space2d.widget;
//
//import com.jogamp.opengl.GL2;
//import jcog.exe.Loop;
//import jcog.learn.ql.HaiQae;
//import jcog.signal.Tensor;
//import jcog.tree.rtree.rect.RectFloat;
//import spacegraph.space2d.SurfaceRender;
//import spacegraph.space2d.container.Bordering;
//import spacegraph.space2d.container.Gridding;
//import spacegraph.space2d.widget.chip.NoiseVectorChip;
//import spacegraph.space2d.widget.chip.SwitchChip;
//import spacegraph.space2d.widget.meta.ObjectSurface;
//import spacegraph.space2d.widget.meter.BitmapMatrixView;
//import spacegraph.space2d.widget.meter.PaintUpdateMatrixView;
//import spacegraph.space2d.widget.port.BoolPort;
//import spacegraph.space2d.widget.port.FloatPort;
//import spacegraph.space2d.widget.port.IntPort;
//import spacegraph.space2d.widget.port.TypedPort;
//import spacegraph.space2d.widget.text.LabeledPane;
//import spacegraph.space2d.widget.text.VectorLabel;
//import spacegraph.space2d.widget.windo.GraphEdit;
//import spacegraph.space2d.widget.windo.Windo;
//import spacegraph.video.Draw;
//
//import static spacegraph.space2d.container.Gridding.VERTICAL;
//
//public class TensorGlow {
//
////    static final Random rng = new XoRoShiRo128PlusRandom(1);
//
//
//    public static void main(String[] args) {
//
//        GraphEdit p = GraphEditTest.newWallWindow();
//
////        p.W.setGravity(new v2(0, -2.8f));
////        staticBox(p.W, -5, -8, +5, 2f, false, true, true, true);
////
////        for (int j = 0; j < 3; j++)        {
////            BodyDef bodyDef2 = new BodyDef();
////            bodyDef2.type = BodyType.DYNAMIC;
////            bodyDef2.angle = -0.6f;
////            bodyDef2.linearVelocity = new v2(0.0f, 0.0f);
////            bodyDef2.angularVelocity = 0.0f;
////            Body2D newBody = p.W.addBody(bodyDef2);
////            PolygonShape shape2 = new PolygonShape();
////            shape2.setAsBox(0.25f, 0.25f);
////            Fixture f = newBody.addFixture(shape2, 1.0f);
////            f.friction = 0.5f;
////            f.restitution = 0.0f;
////            f.material = new Uniform();
////            f.material.m_rigidity = 1.0f;
////        }
//
//
////        {
////            p.W.setContactListener(new Explosives.ExplosionContacts());
////
////            TheoJansen t = new TheoJansen(p.W, 0.35f);
////            Dyn2DSurface.PhyWindow pw = p.put(new Gridding(0.5f, new Port((float[] v) -> {
////
////                t.motorJoint.setMotorSpeed(v[0]*2 - v[1]*2);
////                t.motorJoint.setMaxMotorTorque(v[2]);
////                t.motorJoint.enableLimit(true);
////                t.motorJoint.setLimits((float) (-v[3]*Math.PI), (float) (+v[4]*Math.PI));
////                if (v[5] > 0.5f) {
////                    t.gun.fire();
////                }
////                t.turretJoint.setLimits((float) (+Math.PI/2 + v[6] * Math.PI -0.1f), (float) (+Math.PI/2 + v[6] * Math.PI +0.1f));
////            })), 0.8f, 0.4f);
////            p.W.addJoint(new RevoluteJoint(p.W, new RevoluteJointDef(pw.body, t.chassis)));
////        }
////
////        {
////            p.W.setParticleRadius(0.05f);
////            p.W.setParticleDamping(0.1f);
////
////            CircleShape shape = new CircleShape();
////            shape.center.setAt(0, 10);
////            shape.radius = 2f;
////            ParticleGroupDef pd = new ParticleGroupDef();
////            pd.flags = ParticleType.
////                    b2_waterParticle;
////
////
////
////
////            pd.color = new ParticleColor(0.7f, 0.1f, 0.1f, 0.8f);
////            pd.shape = shape;
////            p.W.addParticles(pd);
////        }
//
//        HaiQae q = new HaiQae(8, 4);
//        float[] in = new float[q.ae.inputs()];
//
//
//        {
//            TrackXY track = new TrackXY(4, 4);
//            BitmapMatrixView trackView = new BitmapMatrixView(track.W, track.H, (x, y) -> Draw.rgbInt(track.grid.brightness(x, y), 0, 0)) {
//                @Override
//                protected void paint(GL2 gl, SurfaceRender surfaceRender) {
//                    super.paint(gl, surfaceRender);
//
//                    RectFloat at = cellRect(track.cx, track.cy, 0.5f, 0.5f);
//                    gl.glColor4f(0, 0, 1, 0.9f);
//                    Draw.rect(at.move(x(), y(), 0.01f), gl);
//                }
//            };
//            Loop.of(() -> {
//                track.act();
//                trackView.update();
//            }).setFPS(10f);
//
//            TypedPort<Tensor> state = new TypedPort<>(Tensor.class);
//
//            FloatPort reward = new FloatPort();
//
//            Windo trackWin = p.addAt(new Bordering(trackView).setAt(Bordering.S, state, 0.05f).setAt(Bordering.E, reward, 0.05f));
//            trackWin.pos(500, 500, 600, 600);
//
//            p.sprout(trackWin, new BoolPort(z->{ if (z) track.control(-1, 0); }), 0.25f);
//            p.sprout(trackWin, new BoolPort(z->{ if (z) track.control(0, -1); }), 0.25f);
//            p.sprout(trackWin, new BoolPort(z->{ if (z) track.control(+1, 0); }), 0.25f);
//            p.sprout(trackWin, new BoolPort(z->{ if (z) track.control(0, +1); }), 0.25f);
//        }
//
//
//        {
//
//            //lerpVector.update();
//            p.addAt(new NoiseVectorChip()).pos(100, 100, 200, 200);
//
//        }
//
//        //p.addAt(new TogglePort()).pos(200, 200, 300, 300);
//
//        IntPort outs;
//        Gridding hw = new Gridding(
//                new VectorLabel("HaiQ"),
//                new ObjectSurface(q),
//                new Gridding(VERTICAL,
//                        new PaintUpdateMatrixView(in),
//                        new PaintUpdateMatrixView(q.ae.x),
//                        new PaintUpdateMatrixView(q.ae.W),
//                        new PaintUpdateMatrixView(q.ae.y)
//                ),
//                new Gridding(VERTICAL,
//                        new PaintUpdateMatrixView(q.q),
//                        new PaintUpdateMatrixView(q.et)
//                )
//        );
//        hw.addAt(new LabeledPane("input", new TypedPort<>(float[].class, (i) -> {
//            System.arraycopy(i, 0, in, 0, i.length);
//        })));
//        hw.addAt(new LabeledPane("act", outs = new IntPort()));
//
//        p.addAt(hw).pos(350, 350, 500, 500);
//
//        Loop.of(() -> {
//
//            int a = q.act((((float) Math.random()) - 0.5f) * 2, in);
//            outs.out(a);
////            int n = outs.size();
////            for (int i = 0; i < n; i++) {
////                outs.out(i, (i == a));
////            }
//        }).setFPS(25);
//
//        SwitchChip outDemultiplexer = new SwitchChip (4);
//        p.addAt(outDemultiplexer).pos(450, 450, 510, 510);
//    }
//
//
//}
