package spacegraph.space2d.widget.textedit.buffer;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BufferTest {

  private final Buffer buf= new Buffer("");


  @Test
  public void initialState() {
    assertEquals(buf.lines.size(), (1));
    assertEquals(buf.lines.get(0).length(), (0));
      assertEquals(buf.cursor, (new CursorPosition(0, 0)));
    buf.forward();
      assertEquals(buf.cursor, (new CursorPosition(0, 0)));
    buf.back();
      assertEquals(buf.cursor, (new CursorPosition(0, 0)));
    buf.previous();
      assertEquals(buf.cursor, (new CursorPosition(0, 0)));
    buf.next();
      assertEquals(buf.cursor, (new CursorPosition(0, 0)));
  }

  @Test
  public void insertString1() {
    buf.insert("abcde");
      assertEquals(buf.cursor, (new CursorPosition(0, 5)));
    buf.insertEnter(true);
      assertEquals(buf.cursor, (new CursorPosition(1, 0)));
  }

  @Test
  public void insertCRLF() {
    buf.insert("\r\n");
      assertEquals(buf.cursor, (new CursorPosition(1, 0)));
  }

  @Test
  public void insertLF() {
    buf.insert("\n");
      assertEquals(buf.cursor, (new CursorPosition(1, 0)));
  }

  @Test
  public void insertEmpty() {
    buf.insert("");
      assertEquals(buf.cursor, (new CursorPosition(0, 0)));
  }

  @Test
  public void insertEnter() {
    buf.insert("abcde\n12345\n");
      assertEquals(buf.cursor, (new CursorPosition(2, 0)));
    buf.back();
      assertEquals(buf.cursor, (new CursorPosition(1, 5)));
    buf.head();
    buf.previous();
      assertEquals(buf.cursor, (new CursorPosition(0, 0)));
    buf.forward();
    buf.forward();
    buf.insertEnter(true);
      assertEquals(buf.cursor, (new CursorPosition(1, 0)));
    assertEquals(buf.text(), ("ab\ncde\n12345\n"));
  }

  @Test
  public void backspace() {
    buf.insert("abcde\n12345\n");
      assertEquals(buf.cursor, (new CursorPosition(2, 0)));
    buf.backspace();
    assertEquals(buf.text(), ("abcde\n12345"));
    for (int i = 0; i < 5; i++) {
      buf.backspace();
    }
    assertEquals(buf.text(), ("abcde\n"));
    for (int i = 0; i < 5; i++) {
      buf.backspace();
    }
    assertEquals(buf.text(), ("a"));
    buf.backspace();
    assertEquals(buf.text(), (""));
  }

  @Test
  public void delete() {
    buf.insert("abcde\n12345\n");
      assertEquals(buf.cursor, (new CursorPosition(2, 0)));
    buf.previous();
    buf.forward();
    buf.forward();
      assertEquals(buf.cursor, (new CursorPosition(1, 2)));
    buf.delete();
    buf.delete();
    buf.delete();
    assertEquals(buf.text(), ("abcde\n12\n"));
    buf.delete();
    buf.delete();
    buf.delete();
    assertEquals(buf.text(), ("abcde\n12"));
      assertEquals(buf.cursor, (new CursorPosition(1, 2)));
    buf.bufferHead();
    buf.delete();
    buf.delete();
    buf.delete();
    buf.delete();
    buf.delete();
    buf.delete();
    assertEquals(buf.text(), ("12"));
    buf.delete();
    buf.delete();
    assertEquals(buf.text(), (""));
    buf.delete();
    assertEquals(buf.text(), (""));
  }

}