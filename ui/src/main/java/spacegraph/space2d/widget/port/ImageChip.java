package spacegraph.space2d.widget.port;

import spacegraph.video.Tex;

import java.awt.image.BufferedImage;

class ImageChip extends ConstantPort<BufferedImage> {

    ImageChip(BufferedImage img) {
        super(img);
        set(Tex.view(img));
    }

}