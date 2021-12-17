package spacegraph.space2d.widget.button;

import spacegraph.video.ImageTexture;

public class IconToggleButton extends ColorToggle {

    public IconToggleButton(ImageTexture icon) {
        super(0.5f,0.5f,0.5f);
        set(icon.view(1.0f));
    }


}