package spacegraph.space2d.meta;

import jcog.Util;
import jcog.exe.InstrumentedLoop;
import jcog.exe.Loop;
import jcog.signal.MutableInteger;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.meter.Plot2D;
import spacegraph.space2d.widget.slider.IntSpinner;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * control and view statistics of a loop
 */
public class LoopPanel extends Gridding {

    protected final Loop loop;
    private final IntSpinner fpsLabel;
    private final Plot2D cycleTimePlot;
    private final Plot2D heapPlot;
    private final MutableInteger fps;

    private volatile boolean pause = false;

    public LoopPanel(Loop loop) {
        this.loop = loop;
        fps = new MutableInteger(Math.round(loop.fps()));
        fpsLabel = new IntSpinner(fps, f -> f + "fps", 0, 100);

        if (loop instanceof InstrumentedLoop iloop) {
            cycleTimePlot = new Plot2D(128)
                    .add("cycleTime", ()->iloop.cycleTimeS)
                    .add("dutyTime", ()->iloop.dutyTimeS)
            ;
        } else {
            cycleTimePlot = new Plot2D(8); //HACK
        }

        heapPlot = new Plot2D(128)
                .add("heap", Util::memoryUsed, 0, 1);

        set(
                        //new ButtonSet(ButtonSet.Mode.One,
//                                ToggleButton.awesome("play").on((b) -> {
//                                    if (b) {
//                                        if (pause) {
//                                            pause = false;
//                                            update();
//                                        }
//
//                                    }
//                                }), ToggleButton.awesome("pause").on((b) -> {
//                            if (b) {
//
//                                if (!pause) {
//                                    pause = true;
//                                    update();
//                                }
//                            }
//                        })
//                        ),
                                new CheckBox("On").set(loop.isRunning()).on((o)->{
                                    //synchronized(loop) {
                                        //HACK maybe necessary
                                        if (o) {
                                            pause = false;
                                            loop.fps(fps.intValue());
                                        } else {
                                            pause = true;
                                            loop.stop();
                                        }
                                        update(); //HACK shouldnt be needed
                                        //}
                                }
                                //)
                        ),
                        fpsLabel, 
                        cycleTimePlot,
                        heapPlot
                );
    }

    private final AtomicBoolean busy = new AtomicBoolean(false); //HACK

    public void update() {
        if (busy.compareAndSet(false, true)) {
            try {
                if (!pause) {
                    int f = fps.intValue();
                    int g = Math.round(loop.fps());
                    if (f > 0) {

                        if (f != g) {
                            //f = g; //OVERRIDE HACK
                            loop.fps(f);
                            fpsLabel.set(f);
                        }
                    } else {
                        fps.set(g);
                        fpsLabel.set(g);
                    }
                    cycleTimePlot.update();
                    heapPlot.update();
                } else {
                    if (loop.isRunning()) {

                        loop.stop();
                        fpsLabel.set(0);
                    }

                }
            } finally {
                busy.set(false);
            }
        }

    }
}