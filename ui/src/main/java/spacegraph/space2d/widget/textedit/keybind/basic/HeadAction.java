package spacegraph.space2d.widget.textedit.keybind.basic;

import spacegraph.space2d.widget.textedit.TextEditModel;
import spacegraph.space2d.widget.textedit.keybind.Action;

public class HeadAction implements Action {

  @Override
  public String name() {
    return "head";
  }

  @Override
  public void execute(TextEditModel editor, String... args) {
    editor.buffer().head();
  }

}
