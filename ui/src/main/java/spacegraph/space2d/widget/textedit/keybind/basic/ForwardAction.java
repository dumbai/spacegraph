package spacegraph.space2d.widget.textedit.keybind.basic;

import spacegraph.space2d.widget.textedit.TextEditModel;
import spacegraph.space2d.widget.textedit.keybind.Action;

public class ForwardAction implements Action {

  @Override
  public String name() {
    return "forward";
  }

  @Override
  public void execute(TextEditModel editor, String... args) {
    editor.buffer().forward();
  }

}
