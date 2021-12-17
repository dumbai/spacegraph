package spacegraph.space2d.widget.textedit.keybind;

import spacegraph.space2d.widget.textedit.TextEditModel;

public interface Action {
  String name();

  void execute(TextEditModel editor, String... args);
}
