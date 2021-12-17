package spacegraph.space2d.widget.chip;

import spacegraph.audio.Audio;
import spacegraph.audio.AudioBuffer;
import spacegraph.audio.Sound;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.button.IconToggleButton;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.space2d.widget.text.Labelling;

public class AudioOutPort extends Gridding  {

    private final TypedPort<AudioBuffer> in;
    private final TypedPort<AudioBuffer> passThru;
    private final IconToggleButton enableButton;


    public AudioOutPort() {
        super();

        enableButton = CheckBox.iconAwesome("play");
        enableButton.set(true);

        set(Labelling.the("in", in = new TypedPort<>(AudioBuffer.class)),
            enableButton,
                Labelling.the("passthru", passThru = new TypedPort<>(AudioBuffer.class)));
    }

    @Override
    protected void starting() {
        super.starting();
        /* TODO buffer mix command object */
        //input fill
        //passthru WARNING the downstream access to pass through can modify the buffer unless it is cloned
        //TODO
        //buffer.skip(..);
        //System.out.println("skip " + samplesToSkip);
        //            @Override
//            public void skip(int samplesToSkip, int readRate) {
//                //TODO
//                //buffer.skip(..);
//                //System.out.println("skip " + samplesToSkip);
//            }
        Sound playback = Audio.the().play((buf, readRate) -> {
            if (enableButton.get() && in.active()) {

                AudioBuffer mixDown = new AudioBuffer(buf, readRate);

                //input fill
                in.out(mixDown);

                if (passThru.active()) {
                    //passthru WARNING the downstream access to pass through can modify the buffer unless it is cloned
                    passThru.out(mixDown);
                }
            }
            return true;
        });
    }



}