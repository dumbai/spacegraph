package spacegraph.space2d.widget.textedit.keybind;

import spacegraph.space2d.widget.textedit.TextEditModel;

public class NoopAction implements Action {

  @Override
  public String name() {
    return "noop";
  }

  @Override
  public void execute(TextEditModel editor, String... args) {
    // noop
  }

}
