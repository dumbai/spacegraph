package spacegraph.space2d.widget.textedit.keybind.basic;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import spacegraph.space2d.widget.textedit.TextEditModel;
import spacegraph.space2d.widget.textedit.keybind.Action;

import javax.swing.*;
import java.io.IOException;

/*
 * 暫定実装。将来捨てるべき。
 */
public class OpenFileAction implements Action {

  @Override
  public String name() {
    return "open-file";
  }

  @Override
  public void execute(TextEditModel editor, String... args) {
    JFileChooser fileChooser = new JFileChooser();
    int selected = fileChooser.showOpenDialog(null);

    if (selected == JFileChooser.APPROVE_OPTION) {
      try {
        String textString = Files.toString(fileChooser.getSelectedFile(), Charsets.UTF_8);
        editor.createNewBuffer();
        editor.buffer().insert(textString);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
