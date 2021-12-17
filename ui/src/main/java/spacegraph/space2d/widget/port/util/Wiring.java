package spacegraph.space2d.widget.port.util;

import jcog.exe.Exe;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.state.Dragging;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.widget.port.Port;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.space2d.widget.port.Wire;
import spacegraph.space2d.widget.shape.PathSurface;
import spacegraph.space2d.widget.windo.Windo;
import spacegraph.util.Path2D;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import static spacegraph.space2d.widget.port.TypedPort.CAST;

/**
 * the process of drawing a wire between two surfaces
 */
public class Wiring extends Dragging {


    public interface Wireable {
        boolean onWireIn(@Nullable Wiring w, boolean active);

        void onWireOut(@Nullable Wiring w, boolean active);

    }

    private Path2D path;

    public final Surface start;
    private PathSurface pathVis;
    private Surface end;

    public Wiring(int button, Surface start) {
        super(button);
        this.start = start;
    }

    @Override
    protected boolean starting(Finger f) {
        if (f.pressedNow(button) && super.starting(f)) {
            if (this.start instanceof Wireable)
                ((Wireable) start).onWireOut(this, true);
            return true;
        }
        return false;
    }



    @Override
    protected boolean drag(Finger f) {
        if (path == null) {

            GraphEdit2D g = graph();
            if (g!=null)
                g.addRaw(pathVis = new PathSurface(path = new Path2D(64)));
            else
                return false; //detached component no longer or never was in graph

        } else {
            updateEnd(f);
        }

        pathVis.add(f.posGlobal(), 64);
        //System.out.println(pathVis.visible() + " " + pathVis.bounds + " " + pathVis.parent + " " + pathVis.path.size());

        return true;
    }

    private GraphEdit2D graph() {
        return start.parentOrSelf(GraphEdit2D.class);
    }

    @Override
    public final void stop(Finger finger) {

        if (start instanceof Port)
            ((Port)start).beingWiredOut = null;
        if (end instanceof Port)
            ((Port)end).beingWiredIn = null;
        if (pathVis != null) {
            pathVis.delete();
            pathVis = null;
        }

        if (start == end) return; //same instance
        if (end == null) return;

        Exe.runLater(this::tryWire);

    }

    private void tryWire() {
        GraphEdit2D g = graph();
//        boolean wired = false;
        if (Port.canConnect((Port)start, (Port)end)) {

            if (start instanceof TypedPort && end instanceof TypedPort) {

                //TODO lazy construct and/or cache these

                //apply type checking and auto-conversion if necessary
                Class aa = ((TypedPort) start).type;
                Class bb = ((TypedPort) end).type;
                if (aa != bb /* TODO && direct ancestor comparison */ ) {

                    List<Function> ab = CAST.applicable(aa, bb), ba = CAST.applicable(bb, aa);

                    if (!ab.isEmpty() || !ba.isEmpty()) {
                        //wire with adapter
                        PortAdapter<?,?> xy = new PortAdapter<>(aa, ab, bb, ba);
                        Windo wxy = g.addUndecorated(xy);
                        g.physics.pos(wxy, start.bounds.mean(end.bounds).scale(0.25f));

                        TypedPort<?> es = xy.port(true);
                        TypedPort<?> se = xy.port(false);

                        g.addWire(new Wire(start, es));
                        g.addWire(new Wire(se, end));
                    }

                    return;
                }
            }

            g.addWire(new Wire(start, end));
        }


    }

    private void updateEnd(Finger finger) {
        Surface nextEnd = finger.touching();
        if (nextEnd != end) {

            if (nextEnd == start) {
                end = null;
            } else {

                if (end instanceof Wireable)
                    ((Wireable) end).onWireIn(this, false);


                if (nextEnd instanceof Wireable)
                    this.end = ((Wireable) nextEnd).onWireIn(this, true) ? nextEnd : null;

            }
        }
    }

}