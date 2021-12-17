package spacegraph.space2d.widget.textedit.keybind.basic;

import spacegraph.space2d.widget.textedit.TextEditModel;
import spacegraph.space2d.widget.textedit.keybind.Action;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CopyAction implements Action {

  @Override
  public String name() {
    return "copy";
  }

  @Override
  public void execute(TextEditModel editor, String... args) {
    setClipboardString(editor.buffer().copy());
  }

  private static void setClipboardString(String value) {
    StringSelection selection = new StringSelection(value);

    Toolkit toolKit = Toolkit.getDefaultToolkit();
    Clipboard clipboard = toolKit.getSystemClipboard();
    clipboard.setContents(selection, selection);
  }
}
