package spacegraph.space2d.widget.button;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.meta.OKSurface;
import spacegraph.space2d.widget.textedit.TextEdit;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * generic input submission widget
 * accepts some kind of widget to wrap and an accessor for its value.
 * accepts a callback of what to do with that value when the
 * input is submitted, ie. an attached labeled pushbutton is clicked
 * where invocation demands explicit user volition.
 *
 * TODO cancel/reset option etc
 * TODO graphics input type (for sketches)
 */
public class Submitter extends Bordering {


    public static Submitter text(String label, Consumer<String> input) {
        return new Submitter(label, new TextEdit(16), TextEdit::text, input);
    }

    public <S extends Surface, X> Submitter(String label, S editable, Function<S,X> valueAccessor, Consumer<X> input) {
        this(new PushButton(label), editable, valueAccessor, (s, x)->input.accept(x));
    }

    public <S extends Surface, X> Submitter(PushButton submitButton, S editable, Function<S,X> valueAccessor, BiConsumer<S,X> input) {
        super(submitButton);
        north(editable);
        submitButton.clicked(()->{
            try {
                input.accept(editable, valueAccessor.apply(editable));
            } catch (Exception e) {
                south(new OKSurface(e));
            }
        });
    }
}