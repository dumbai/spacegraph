package spacegraph.space2d.widget.textedit.keybind.basic;

import spacegraph.space2d.widget.textedit.TextEditModel;
import spacegraph.space2d.widget.textedit.keybind.Action;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class PasteAction implements Action {

  @Override
  public String name() {
    return "paste";
  }

  @Override
  public void execute(TextEditModel editor, String... args) {
    editor.buffer().insert(getClipboardString());
  }

  private static String getClipboardString() {
    Toolkit toolKit = Toolkit.getDefaultToolkit();
    Clipboard clipboard = toolKit.getSystemClipboard();
    try {
      return clipboard.getData(DataFlavor.stringFlavor).toString();
    } catch (UnsupportedFlavorException | IOException e1) {
      e1.printStackTrace();
    }
    return "";
  }
}
