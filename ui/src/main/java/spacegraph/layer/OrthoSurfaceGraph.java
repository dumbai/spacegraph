package spacegraph.layer;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import jcog.event.Off;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.impl.NewtKeyboard;
import spacegraph.input.finger.impl.NewtMouseFinger;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.SurfaceGraph;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.EmptySurface;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.container.grid.Containers;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.Animating;
import spacegraph.space2d.hud.Zoomed;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.util.animate.Animated;
import spacegraph.video.JoglWindow;

public class OrthoSurfaceGraph extends AbstractLayer implements SurfaceGraph {


    public final ReSurface rendering = new ReSurface();

    public final NewtKeyboard keyboard;

    public final Stacking root = new Stacking();

    /** TODO return false when possible */
    @Override public boolean changed() {
        return true;
    }

    /** container for content in (zooming) virtual space */
    private Surface content;

    /**
     * @param x content to interact zoomably
     */
    public OrthoSurfaceGraph(Surface x, JoglWindow w) {
        super();


        keyboard = new NewtKeyboard(/*TODO this */);
        w.addKeyListener(keyboard);

        setWindow(w);

        w.runLater(() -> {

            w.window.addWindowListener(windowResizeListener);
            w.window.setPointerVisible(false);

            resized();

            root.add(this.content = x);

            root.start(this);

            addFinger(new NewtMouseFinger(this, root::finger));

            //addOverlay(this.keyboard.keyFocusSurface(cam));
            //root.add((Surface) hud);
            //root.add(new Menu());
        });

    }

    private final WindowAdapter windowResizeListener = new WindowAdapter() {
        @Override
        public void windowResized(WindowEvent e) {
            resized();
        }
    };


    private void resized() {
        root.resize(window.W(), window.H());
    }

    @Override
    public final void visible(boolean visible) {
        root.visible(visible);
    }

    @Override
    public void addFinger(Finger f) {
        super.addFinger(f);

        if (content instanceof Zoomed) root.add(((Zoomed)content).overlayZoomBounds(f)); //HACK

        Surface cursor = f.cursorSurface();
        if (cursor != null)
            root.add(cursor);
    }

    @Override
    public void close() {
        window.window.removeWindowListener(windowResizeListener);

        root.delete();
    }

    @Override
    protected void renderOrthos(long startNS, float dtS) {
        if (!root.isEmpty())
            rendering.render(root, window, startNS, dtS);
    }


    public final Off animate(Animated c) {
        return onUpdate(c);
    }

    public final Off animate(Runnable c) {
        return onUpdate(c);
    }

    @Override
    public final boolean keyFocus(Surface s) {
        return keyboard.focus(s);
    }


    @Override
    public final SurfaceGraph root() {
        return this;
    }

    /**
     * spawns a developer windonw
     *
     * @return
     */
    public void dev(/** boolean hover mode (undecorated), other options */) {
        Gridding g = new Gridding();

        BitmapLabel fingerInfo = new BitmapLabel();

        g.add(fingerInfo);

        //TODO static Animating.Label(
        window(new Animating<>(g, () -> {
            Finger f = OrthoSurfaceGraph.this.fingers.get(0);
            Surface t = f.touching();
            //"posGl: " + finger.posGlobal(layers.first(Zoomed.class)) + '\n' +
            fingerInfo.text(
                "buttn: " + f.buttonSummary() + '\n' + "state: " + f.fingering() + '\n' + "posPx: " + f.posPixel + '\n' + "touch: " + t + '\n' + "posRl: " + (t != null ? f.posRelative(t.bounds) : "?") + '\n'
            );
        }, 0.1f), 500, 300);
    }

    static class Menu extends Bordering {

        //TODO different modes, etc
        Menu() {
            super(new EmptySurface());
            //animate(new DelayedHover(finger));
            set(SW, Containers.row(
                    PushButton.iconAwesome("mouse-pointer"), //click, wire, etc
                    PushButton.iconAwesome("i-cursor"), //text edit
                    PushButton.iconAwesome("question-circle") //inspect
            ));
            set(NE, new Gridding(
                    PushButton.iconAwesome("bolt") //popup with configuration, options, tasks
            ));
//            set(NE, new Gridding(
//                    new PushButton("X"),
//                    new PushButton("Y"),
//                    new PushButton("Z")
//            ));
//            set(SE, new Gridding(
//                    //new Timeline2D<>()
//            ));

        }

    }


}