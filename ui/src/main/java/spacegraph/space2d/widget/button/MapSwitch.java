package spacegraph.space2d.widget.button;

import java.util.Map;

public enum MapSwitch { ;
    /** TODO decide initialization semantics */
    public static <X> ButtonSet the(Map<X,Runnable> x) {

        ToggleButton[] b = new ToggleButton[x.size()];

        int[] i = {0};
        for (Map.Entry<X, Runnable> entry : x.entrySet()) {
            X xx = entry.getKey();
            Runnable r = entry.getValue();
            CheckBox tb = new CheckBox(xx.toString());
            tb.on((c, enabled) -> {
                if (enabled)
                    r.run();
            });
//                if (xx == initialValue)
//                    initialButton = i;
            b[i[0]++] = tb;
        }


        int[] initialButton = {-1};
        return EnumSwitch.newSwitch(b, initialButton[0]);

    }

}
