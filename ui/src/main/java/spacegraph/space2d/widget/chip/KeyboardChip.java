package spacegraph.space2d.widget.chip;

import com.jogamp.newt.event.KeyEvent;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.space2d.widget.text.AbstractLabel;
import spacegraph.space2d.widget.text.VectorLabel;

public class KeyboardChip extends Widget {

    private final AbstractLabel txt;
    private final TypedPort<Integer> out;

    public KeyboardChip() {
        super();

        set(new Stacking(out = new TypedPort<>(Integer.class), txt = new VectorLabel()));
    }

    @Override
    public Surface finger(Finger f) {
        //TODO request focus
    focus();
        return super.finger(f);
    }

    @Override
    public boolean key(KeyEvent e, boolean pressedOrReleased) {
        //FIFO, 0=unpressed
        if (pressedOrReleased)
            out((e.isPrintableKey() ? e.getKeyChar() : e.getKeyCode()));
        else
            out(0);

        return true;

    }

    protected void out(int keyCode) {

        int kc = map(keyCode);
        if (kc!=0)
            txt.text(label(kc));
        else
            txt.text("");


        out.out(kc /*new ArrayTensor(new float[] { kc })*/);
    }

    /** return 0 to filter */
    protected int map(int keyCode) {
        return keyCode;
    }

    protected String label(int keyCode) {
        return String.valueOf(keyCode);
    }

    public static class ArrowKeysChip extends KeyboardChip {
        @Override
        protected String label(int keyCode) {
            return switch (keyCode) {
                case 1 -> "left";
                case 2 -> "right";
                case 3 -> "up";
                case 4 -> "down";
                default -> "";
            };
        }

        @Override
        protected int map(int keyCode) {
            switch (keyCode) {
                case KeyEvent.VK_LEFT -> {
                    keyLeft();
                    return 1;
                }
                case KeyEvent.VK_RIGHT -> {
                    keyRight();
                    return 2;
                }
                case KeyEvent.VK_UP -> {
                    keyUp();
                    return 3;
                }
                case KeyEvent.VK_DOWN -> {
                    keyDown();
                    return 4;
                }
            }
            keyNone();
            return 0;
        }


        protected void keyLeft() { }
        protected void keyRight() { }
        protected void keyUp() { }
        protected void keyDown() { }
        protected void keyNone() { }
    }
}