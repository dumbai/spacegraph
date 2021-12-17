package spacegraph.test;

import jcog.Util;
import jcog.exe.Exe;
import org.jetbrains.annotations.NotNull;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ContainerSurface;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.container.grid.Containers;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.meta.MetaFrame;
import spacegraph.space2d.meta.ProtoWidget;
import spacegraph.space2d.meta.WizardFrame;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.button.HexButton;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.chip.NoiseVectorChip;
import spacegraph.space2d.widget.chip.SpeakChip;
import spacegraph.space2d.widget.menu.ListMenu;
import spacegraph.space2d.widget.menu.TabMenu;
import spacegraph.space2d.widget.menu.view.GridMenuView;
import spacegraph.space2d.widget.port.LabeledPort;
import spacegraph.space2d.widget.port.Port;
import spacegraph.space2d.widget.port.TogglePort;
import spacegraph.space2d.widget.sketch.Sketch2DBitmap;
import spacegraph.space2d.widget.slider.FloatSlider;
import spacegraph.space2d.widget.slider.SliderModel;
import spacegraph.space2d.widget.slider.XYSlider;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.space2d.widget.text.Labelling;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.space2d.widget.textedit.TextEdit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static spacegraph.SpaceGraph.window;

public enum WidgetTest {
    ;

    static final Map<String, Supplier<Surface>> menu;

    static {
        Map<String, Supplier<Surface>> m = Map.of(
                "Container", () -> Containers.grid(
                        Labelling.the("grid",
                                Containers.grid(iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton())
                        ),
                        Labelling.the("grid wide",
                                new Gridding(Util.PHI_min_1f, iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton())
                        ),
                        Labelling.the("grid tall",
                                new Gridding(Util.PHIf, iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton())
                        ),
                        Labelling.the("column",
                                Containers.col(iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton())
                        ),
                        Labelling.the("row",
                                Containers.row(iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton(), iconButton())
                        ),
                        Labelling.the("vsplit",
                                Containers.col(iconButton(), 0.618f, iconButton())
                        ),
                        Labelling.the("hsplit",
                                Containers.row(iconButton(), 0.618f, iconButton())
                        )
                ),
                "Button", () -> Containers.grid(
                        new PushButton("PushButton"),
                        new CheckBox("CheckBox"),
                        new HexButton("gears", "HexButton")
                ),
                "Slider", () -> Containers.grid(
                        Containers.row(Containers.grid(new FloatSlider("solid slider", 0.25f, 0, 1    /* pause */),
                                new FloatSlider("knob slider", 0.75f, 0, 1).type(SliderModel.KnobHoriz)), 0.9f, new FloatSlider(0.33f, 0, 1).type(SliderModel.KnobVert)),
                        new XYSlider()
                ),
//                "Dialog", () -> grid(
//                        new TextEdit0("xyz").show(),
//                        new FloatSlider(0.33f, 0.25f, 1, "Level"),
//                        new ButtonSet(ButtonSet.Mode.One, new CheckBox("X"), new CheckBox("y"), new CheckBox("z")),
//
//                        Submitter.text("OK", (String result) -> {
//                        })
//                ),

                "Wizard", ProtoWidget::new,
                "Label", () -> Containers.grid(
                        new VectorLabel("vector"),
                        new BitmapLabel("bitmap")
                ),
                "TextEdit", () ->
                        Containers.col(
                            new TextEdit("Edit this\n...").focus(),
                            new TextEdit(16, 1).text("One Line Only").background(0.5f, 0, 0, 1),
                            new TextEdit(16, 4).text("Multi-line").background(0.0f, 0.25f, 0, 0.8f)

                        ),
                "Graph2D", () -> new TabMenu(graph2dDemos()),

                "Wiring", () -> new TabMenu(wiringDemos())

                //"Geo", OSMTest::osmTest
        );

        m = new HashMap<>(m); //escape arg limitation of Map.of()
        m.put("Sketch", () -> new MetaFrame(new Sketch2DBitmap(256, 256)));
        m.put("Speak", SpeakChip::new);
        m.put("Resplit", () -> new Splitting(
                        new Splitting<>(iconButton(), Util.PHI_min_1f, true, iconButton()).resizeable(),
                        Util.PHI_min_1f,
                        new Splitting<>(iconButton(), Util.PHI_min_1f, false, iconButton()).resizeable()
                ).resizeable()
        );
        m.put("Timeline", Timeline2DTest::timeline2dTest);
//        m.put("Tsne", TsneTest::testTsneModel);
//        m.put("Signal", SignalViewTest::newSignalView);
        m.put("Hover", HoverTest::hoverTest);
        menu = m;
    }

    @NotNull
    private static Map<String, Supplier<Surface>> graph2dDemos() {
        return Map.of(
                "Simple", Graph2DTest::newSimpleGraph,
                "UJMP", Graph2DTest::newUjmpGraph,
                "Types", Graph2DTest::newTypeGraph
        );
    }

    public static void main(String[] args) {
        window(widgetDemo(), 1200, 800);
//            .dev()
    }

    public static ContainerSurface widgetDemo() {
        //return new TabMenu(menu);
        return new ListMenu(menu, new GridMenuView());
    }

    private static Map<String, Supplier<Surface>> wiringDemos() {
        return Map.of(
                "Empty", () -> wiringDemo((g) -> {
                }),
                "Intro", () -> wiringDemo(g -> {
                    g.add(widgetDemo()).posRel(1, 1, 0.5f, 0.25f);
                    for (int i = 1; i < 3; i++)
                        g.add(new WizardFrame(new ProtoWidget())).posRel(0.5f, 0.5f, 0.45f / i, 0.35f / i);
                }),
                //"", ()-> wiringDemo((g)->{})
                "Basic", () -> wiringDemo((g) -> {
                    /** switched signal */

                    NoiseVectorChip A = new NoiseVectorChip();
                    ContainerSurface a = g.add(A).sizeRel(0.25f, 0.25f);


                    Port B = LabeledPort.generic();
                    ContainerSurface b = g.add(B).sizeRel(0.25f, 0.25f);

                    TogglePort AB = new TogglePort();
                    g.add(AB).sizeRel(0.25f, 0.25f);

//                    Loop.of(() -> {
//                        A.out(Texts.n4(Math.random()));
//                    }).setFPS(0.3f);
                })
        );
    }

    private static Surface wiringDemo(Consumer<GraphEdit2D> o) {
        return new GraphEdit2D() {
            @Override
            protected void starting() {
                super.starting();
                pos(((Surface) parent).bounds); //HACK
                Exe.runLater(() -> {
                    physics.invokeLater(() -> o.accept(this)); //() -> {
                });
            }
        };
    }

    private static Surface iconButton() {
        //            case 6: s = "dna"; break;
        String s = switch (ThreadLocalRandom.current().nextInt(6)) {
            case 0 -> "code";
            case 1 -> "trash";
            case 2 -> "wrench";
            case 3 -> "fighter-jet";
            case 4 -> "exclamation-triangle";
            case 5 -> "shopping-cart";
            default -> null;
        };
        return PushButton.iconAwesome(s);


        //            switch (ThreadLocalRandom.current().nextInt(6)) {
//                case 0-> "code";
//                default -> null;
//            });

    }


//    private static class DummyConsole extends TextEdit0.TextEditUI implements Runnable {
//
//        public DummyConsole() {
//            super(15, 15);
//            Thread tt = new Thread(this);
//            tt.setDaemon(true);
//            tt.start();
//        }
//
//        @Override
//        public void run() {
//
//            int i = 0;
//            while (true) {
//
//                addLine((Math.random()) + "");
//                if (++i % 7 == 0) {
//                    text("");
//                }
//
//                Util.sleepMS(400);
//
//            }
//        }
//    }
}