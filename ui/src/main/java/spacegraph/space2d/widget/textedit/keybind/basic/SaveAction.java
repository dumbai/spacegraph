package spacegraph.space2d.widget.textedit.keybind.basic;

import spacegraph.space2d.widget.textedit.TextEditModel;
import spacegraph.space2d.widget.textedit.keybind.Action;

public class SaveAction implements Action {

  @Override
  public String name() {
    return "save-buffer";
  }

  @Override
  public void execute(TextEditModel editor, String... args) {
    //editor.getCurrentBuffer().save();
    throw new UnsupportedOperationException();
  }

}
