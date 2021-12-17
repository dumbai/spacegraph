package spacegraph.space2d.meta;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jogamp.common.util.IOUtil;
import jcog.signal.Tensor;
import jcog.signal.tensor.ArrayTensor;
import jcog.signal.tensor.TensorChain;
import jcog.signal.wave1d.HaarWaveletTensor;
import org.eclipse.collections.api.tuple.Pair;
import spacegraph.space2d.SafeSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.button.ToggleButton;
import spacegraph.space2d.widget.chip.*;
import spacegraph.space2d.widget.menu.TabMenu;
import spacegraph.space2d.widget.menu.view.GridMenuView;
import spacegraph.space2d.widget.port.*;
import spacegraph.space2d.widget.slider.XYSlider;
import spacegraph.space2d.widget.text.Labelling;
import spacegraph.space2d.widget.text.VectorLabel;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;

import static org.eclipse.collections.impl.tuple.Tuples.pair;

/**
 * widget which can become "anything"
 */
public class ProtoWidget extends Bordering {

    public static class WidgetLibrary {

        final Map<String,Supplier<Surface>> byName = new HashMap();
        final Multimap<String,Pair<String,Supplier<Surface>>> byTag = HashMultimap.create();


        WidgetLibrary add(String name, Supplier<Surface> s, String... tags) {
            byName.put(name, s);
            for (String t : tags) {
                byTag.put(t, pair(name, s));
            }
            return this;
        }
    }


    private static final WidgetLibrary LIBRARY  = new WidgetLibrary() {{

        add("Keyboard", KeyboardChip::new, "Input");
        add("ArrowKeys", KeyboardChip.ArrowKeysChip::new, "Input");
        add("QwertyPiano", TODO, "Input");
        add("Mouse", TODO, "Input");
        add("Gamepad", TODO, "Input");

        add("WebCam", WebcamChip::new, "Video");
        add("Microphone", AudioCaptureChip::new, "Audio");

        add("java", ()->new ReplChip((cmd,done)-> done.accept("TODO")), "Value"); //java expression evaluation
        add("shell", ()->new ReplChip((cmd,done)->{
            try {
                Process proc = new ProcessBuilder().command(cmd.split(" "))
                        .redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start();
                InputStream is = proc.getInputStream();
                proc.onExit().whenComplete((p, t) -> {
                    try {
                        done.accept(new String(IOUtil.copyStream2ByteArray(is)));
                    } catch (IOException e) {
                        done.accept(e + Arrays.toString(e.getStackTrace()));
                    }
                });
                proc.waitFor();


            } catch (Exception e) {
                done.accept(e + Arrays.toString(e.getStackTrace()));
            }

        }), "Value"); //system shell command evaluation
        add("bool", BoolPort::new, "Value"); //Generic Toggle Switch
        add("int", IntPort::new, "Value");
        add("float", FloatPort::new, "Value");
        add("text", TextPort::new, "Value"); //single or multi-line toggleable
        add("float[-1..1]", ()->new FloatRangePort(0.5f, -1, 1.0f), "Value");
        add("float[0..1]", ()->new FloatRangePort(0.5f, 0, 1.0f), "Value");
        add("f(x)", TODO, "Value");
        add("f(x,y)", TODO, "Value");
        add("v2", ()->new XYSlider().chip(), "Value"); //2D vector (0..1.0 x 0..1.0)
        add("v3", TODO, "Value");
        add("color", TODO, "Value");

//        addAt("random float[0..1]", TODO, "Noise");
//        addAt("random float[-1..+1]", TODO, "Noise");


        add("Recognizer", TODO, "Video");
        add("Tracker", TODO, "Video");
        add("Snapshotter", TODO, "Video");
        add("Vectorize", TODO, "Video");
        add("ShapeDetect", TODO, "Video");

        add("split", TODO, "Math");
        add("concat", ()-> new BiFunctionChip<>(Tensor.class, Tensor.class, Tensor.class, TensorChain::get), "Math");
        add("OneHotBit", ()-> new BiFunctionChip<>(Integer.class, Integer.class, Tensor.class, (signal, range) -> {
            //TODO optimize with a special Tensor impl
            if (signal >= 0 && signal < range) {
                float[] x = new float[range];
                x[signal] = 1;
                return new ArrayTensor(x);
            } else {
                return null;
            }
        }), "Math");
        add("HaarWavelet", ()->new FunctionChip<>(Tensor.class, HaarWaveletTensor.class, t -> new HaarWaveletTensor(t, 64)).buffered(), "Math");

//        add("SlidingDFT", ()->new AccumulatorChip<>(Tensor.class, SlidingDFTTensor.class, (Tensor t)->{
//            return new SlidingDFTTensor( 64, true).;
//        }).buffered(), "Math");

        add("mix", TODO, "Audio");
        add("EQ", TODO, "Audio");
        add("play", TODO, "Audio");
        add("sonify", TODO, "Audio");

        add("MLP", TODO, "Control");
        add("QLearn", TODO, "Control");
//        add("PID", PIDChip::new, "Control");

        add("Text", LabeledPort::generic, "Meter");
        add("Plot", PlotChip::new, "Meter"); //Line, Bar plots
        add("Wave", WaveViewChip::new, "Meter"); //waveform view
        add("VU", TODO, "Meter"); //VU meter
        add("Spectrogram", SpectrogramChip::new, "Meter"); //frequency domain view
        add("MatrixView", MatrixViewChip::new, "Meter");
//        add("Cluster2D", Cluster2DChip::new, "Meter");
        add("Histogram", TODO, "Meter"); //accepts scalar and an integer # of bins
        add("Count", TODO, "Meter"); //count of items passed through
        add("Uniques", TODO, "Meter"); //bag of unique items passed through
        add("Frequencies", TODO, "Meter"); //count of unique items passed through


//        addAt("Geo", () -> new OsmSpace(new IRL(User.the())).surface().go(-80.63f, 28.60f), "Reality");
        add("Weather", TODO, "Reality");

        add("File", TODO, "Data"); //and directory too
        add("CSV", TODO, "Data");
        add("ARFF", TODO, "Data");
        add("SQL", TODO, "Data");
        add("SSH", TODO, "Data");
        add("RDP", TODO, "Data");
        add("HTTP", TODO, "Data");
        add("FTP", TODO, "Data");

        add("CPU", TODO, "Data"); //cpu/memory/disk/network information

        //add("WidgetTest", ()->new WidgetTest(), "Reality");
    }};

    public ProtoWidget() {
        this(LIBRARY);
    }

    private ProtoWidget(WidgetLibrary library) {
        super();

        Map<String,Supplier<Surface>> categories = new TreeMap<>();
        //categories.put("...", OmniBox::new);
        for (Map.Entry<String, Collection<Pair<String, Supplier<Surface>>>> entry : library.byTag.asMap().entrySet()) {
            String t = entry.getKey();
            Collection<Pair<String, Supplier<Surface>>> v = entry.getValue();
            Surface[] fields = v.stream()
                    .sorted(Comparator.comparing(Pair::getOne))
                    .map(x -> {
                        String name = x.getOne();
                        return becoming(name, x.getTwo());
                    })
                    .toArray(Surface[]::new);
            categories.put(t, () -> new Widget(new Labelling(new VectorLabel(t)/* {
//                {  textColor.set(1,1,1,1);   }

//                @Override
//                protected void paintIt(GL2 gl, SurfaceRender r) {
//                    gl.glColor3f(0,0, 0);
//                    Draw.rect(bounds, gl);
//                }
            }*/, new Gridding(fields))));
        }

        set(new TabMenu(categories, new GridMenuView(), (l)->{
            String icon = switch (l) {
                case "Control" -> "cogs";
                case "Value" -> "pencil-square-o";
                case "Math" -> "calculator";
                case "Meter" -> "line-chart";
                case "Video" -> "video-camera";
                case "Audio" -> "volume-up";
                case "Signal" -> "sliders";
                case "Data" -> "database";
                case "Reality" -> "wrench";
                default -> null;
            };

            return icon != null ? ToggleButton.iconAwesome(icon) : new CheckBox(l);
        }));

        set(N, new LazySurface(()->{

            //User u = new User();

//            library.byName.forEach((t,v)-> u.put(t, t));

            return new OmniBox(
                    //new LuceneQueryModel(new User())
                    new OmniBox.JShellModel()
            );

        }, "new User()"));


    }


    private PushButton becoming(String label, Supplier<Surface> replacement) {
        return new PushButton(label,
            () -> parentOrSelf(WizardFrame.class).replace(ProtoWidget.this,
                SafeSurface.safe(replacement)
            ));
    }

}