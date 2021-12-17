package spacegraph.space2d.widget.textedit.keybind.basic;

import spacegraph.space2d.widget.textedit.TextEditModel;
import spacegraph.space2d.widget.textedit.keybind.Action;

public class NextAction implements Action {

  @Override
  public String name() {
    return "next";
  }

  @Override
  public void execute(TextEditModel editor, String... args) {
    editor.buffer().next();
  }

}
