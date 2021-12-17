package spacegraph.space2d.widget.chip;

import com.jogamp.newt.event.KeyEvent;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.port.TextPort;

import java.util.function.Consumer;

public class ReplChip extends Gridding {

    private final ReplModel model;
    private final TextPort in;
//    private final TextPort out;

    @FunctionalInterface
    public interface ReplModel {
        void input(String cmd, Consumer<String> receive);
    }

    public ReplChip(ReplModel m) {
        this.model = m;
        set(
                in = new TextPort() //TODO a Port
//                out = new TextPort() //TODO a Port
        );

//        out.edit.view(40, 8);

        //in.on(z -> {
           //TODO
        //});

        in.edit.onKeyPress(x -> {

            //System.out.println(x.getKeyCode() + "\t" + x.getKeyChar() + "\t" + x.getKeySymbol());

            if (x.getKeyCode() == KeyEvent.VK_ENTER && (!enterOrControlEnter() || x.isControlDown())) {
                String cmd = in.edit.text();

                if (clearOnEnter())
                    in.edit.text("");

                //HACK
                //                    if (appendOrReplace()) {
                ////                        out.edit.text(e); //append?
                ////                        out.out(out.edit.text());
                //                    } else {
                ////                        out.edit.text(e);
                //                    }
                model.input(cmd, System.err::println);
            }
        });
    }

//    public boolean appendOrReplace() { //append mode will require a clear button
//        return false;
//    }

    protected static boolean clearOnEnter() {
        return true;
    }

    protected static boolean enterOrControlEnter() {
        return true;
    }
}
