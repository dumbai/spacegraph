package spacegraph.space2d.widget.textedit.buffer;

import com.jogamp.opengl.GL2;
import spacegraph.space2d.widget.textedit.view.TextEditRenderable;

public abstract class BufferChar extends TextEditRenderable {

  public char c;

  protected BufferChar() {
    this.c = ' ';
  }

  protected BufferChar(char c) {
    this.c = c;
//    this.row = row;
//    this.col = col;
  }
  public void update(BufferChar b) {
    c = b.c;
  }



//  void updatePosition(int row, int col) {
//    this.row = row;
//    this.col = col;
//    observer.accept(this);
//  }

  @Override
  protected abstract void innerDraw(GL2 gl);

//  @Override
//  public int compareTo(BufferChar o) {
//    int rowCompare = Integer.compare(row, o.row);
//    if (rowCompare != 0) {
//      return rowCompare;
//    }
//    return Integer.compare(col, o.col);
//  }
}