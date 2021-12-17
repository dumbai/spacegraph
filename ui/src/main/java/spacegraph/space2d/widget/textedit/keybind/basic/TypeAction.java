package spacegraph.space2d.widget.textedit.keybind.basic;

import spacegraph.space2d.widget.textedit.TextEditModel;
import spacegraph.space2d.widget.textedit.buffer.Buffer;
import spacegraph.space2d.widget.textedit.keybind.Action;

public class TypeAction implements Action {

    @Override
    public String name() {
        return "type";
    }

    @Override
    public void execute(TextEditModel editor, String... args) {
        Buffer buffer = editor.buffer();
        for (String string : args) {
            buffer.insert(string);
        }
    }

}
