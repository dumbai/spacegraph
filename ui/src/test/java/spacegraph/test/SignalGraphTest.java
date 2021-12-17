package spacegraph.test;

//import spacegraph.input.finger.impl.WebcamGestures;

import jcog.signal.wave1d.SignalInput;
import spacegraph.audio.AudioSource;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.EmptySurface;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.container.grid.Containers;
import spacegraph.space2d.container.time.SignalView;
import spacegraph.space2d.widget.port.ConstantPort;
import spacegraph.space2d.widget.port.Surplier;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.video.VideoSource;
import spacegraph.video.VideoSurface;
import spacegraph.video.VideoTransform;
import spacegraph.video.WebCam;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.util.function.Function;

public enum SignalGraphTest {
	;

	public static class VideoTransformPort extends Bordering {

        private final ConstantPort<VideoSource> out;

        private transient VideoTransform y;

        public VideoTransformPort(Function<VideoSource,VideoTransform> t) {
            super();
            ConstantPort<VideoSource> in = new ConstantPort<>(VideoSource.class);
            out = new ConstantPort<>(VideoSource.class);
            west(in);
            east(out);
            //in.update((xx)->{
            in.on((x)->{

                if (this.y!=null) {
                    try {
                        y.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    y = null;
                }

                if (x!=null) {
                    this.y = t.apply(x);
                    center(new VideoSurface(y));
                } else {
                    center(new EmptySurface());
                }

                out.out(y);
            });
        }
    }

    public static void main(String[] args) {

        GraphEdit2D g = GraphEdit2D.graphWindow(1024, 1024);

        for (WebCam c : WebCam.theFirst(10)) {
            g.add(new Surplier(c.toString(), Surface.class, () -> {
                return Containers.row(new ConstantPort(c), 0.1f, new VideoSurface(c));
                ///return new PushButton("cam1");
            })).posRel(0, 0, 0.25f, 0.2f);
        }
//        w.add(new VideoTransformPort(WebcamGestures.VideoBackgroundRemoval::new)).posRel(0,0, 0.1f, 0.1f);



        for (AudioSource a : AudioSource.all()) {
            g.add(new Surplier(a.toString(), Surface.class, ()->{

                try {
                    SignalInput A = new SignalInput();
                    A.set(a, 0.05f);
                    A.fps(32.0f);
                    a.start();

                    SignalView v = new SignalView(A) {
                        @Override
                        public boolean delete() {
                            a.stop();
                            A.stop();
                            return super.delete();
                        }
                    };


                    return v;
                } catch (LineUnavailableException e) {
                    return new VectorLabel(e.toString());
                }

            })).posRel(0, 0, 0.1f, 0.1f);


        }

        //g.add(new SpectrogramChip()).posRel(0,0,0.1f,0.2f);

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}