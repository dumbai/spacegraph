package spacegraph.space2d.widget.button;

import jcog.signal.MutableEnum;
import spacegraph.space2d.Surface;
import spacegraph.space2d.widget.text.Labelling;

import java.util.EnumSet;

public enum EnumSwitch { ;

    protected static ButtonSet newSwitch(ToggleButton[] b, int i2) {
        ButtonSet editPane = new ButtonSet(ButtonSet.Mode.One, b);

        if (i2 != -1) {
            b[i2].set(true);
        }

        return editPane;
    }

    public static <C extends Enum<C>> Surface the(MutableEnum x, String label) {


        EnumSet<C> s = EnumSet.allOf(x.klass);

        int initialButton = -1;

        ToggleButton[] b = new ToggleButton[s.size()];

        Enum initialValue = x.get();

        int i = 0;
        for (C xx : s) {
            CheckBox tb = new CheckBox(xx.name());
            tb.on((c, enabled) -> {
                if (enabled)
                    x.set(xx);
            });
            if (xx == initialValue)
                initialButton = i;
            b[i] = tb;
            i++;
        }


        return Labelling.the(label, newSwitch(b, initialButton));


    }
}