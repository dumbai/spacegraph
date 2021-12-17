package spacegraph.space2d;

import spacegraph.space2d.meta.ErrorPanel;

import java.util.function.Supplier;

public enum SafeSurface {;

    //ImageTexture.awesome("exclamation-triangle").view(1)

    public static Surface safe(Supplier<Surface> s) {
        Surface r;
        try {
            r = s.get();
        } catch (RuntimeException e) {
            r = new ErrorPanel(e, s);
            e.printStackTrace();
        }

        return r == null ? new ErrorPanel(new NullPointerException(), s) : r;
    }
}