package spacegraph.space2d.widget.textedit.buffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CursorTest {

  @Test
  public void testCompareTo() {
    assertEquals(0, new CursorPosition(10, 10).compareTo(new CursorPosition(10, 10)));
    assertEquals(-1, new CursorPosition(10, 10).compareTo(new CursorPosition(10, 11)));
    assertEquals(1, new CursorPosition(10, 10).compareTo(new CursorPosition(10, 9)));

    assertEquals(-1, new CursorPosition(10, 10).compareTo(new CursorPosition(11, 10)));
    assertEquals(1, new CursorPosition(10, 10).compareTo(new CursorPosition(9, 10)));
  }

}
