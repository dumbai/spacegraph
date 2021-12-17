package spacegraph.space2d.widget.textedit.keybind;

import com.jogamp.newt.event.KeyEvent;
import spacegraph.space2d.widget.textedit.TextEditActions;
import spacegraph.space2d.widget.textedit.TextEditModel;

import java.util.HashMap;
import java.util.Map;

import static com.jogamp.newt.event.KeyEvent.*;


public abstract class TextEditKeys {

    static final Map<String, Short> code = new HashMap<>();
    public final TextEditActions actions;
    static {
        // alpha num
        code.put("a", VK_A);
        code.put("b", VK_B);
        code.put("c", VK_C);
        code.put("d", VK_D);
        code.put("e", VK_E);
        code.put("f", VK_F);
        code.put("g", VK_G);
        code.put("h", VK_H);
        code.put("i", VK_I);
        code.put("j", VK_J);
        code.put("k", VK_K);
        code.put("l", VK_L);
        code.put("m", VK_M);
        code.put("n", VK_N);
        code.put("o", VK_O);
        code.put("p", VK_P);
        code.put("q", VK_Q);
        code.put("r", VK_R);
        code.put("s", VK_S);
        code.put("t", VK_T);
        code.put("u", VK_U);
        code.put("v", VK_V);
        code.put("w", VK_W);
        code.put("x", VK_X);
        code.put("y", VK_Y);
        code.put("z", VK_Z);
        code.put("0", VK_0);
        code.put("1", VK_1);
        code.put("2", VK_2);
        code.put("3", VK_3);
        code.put("4", VK_4);
        code.put("5", VK_5);
        code.put("6", VK_6);
        code.put("7", VK_7);
        code.put("8", VK_8);
        code.put("9", VK_9);

        code.put(";", VK_SEMICOLON);
        code.put(":", VK_COLON);
        code.put("{", VK_LEFT_BRACE);
        code.put("}", VK_RIGHT_BRACE);
        code.put("[", VK_OPEN_BRACKET);
        code.put("]", VK_CLOSE_BRACKET);
        code.put("minus", VK_MINUS);
        code.put("+", VK_PLUS);
        code.put("@", VK_AT);
        code.put("(", VK_LEFT_PARENTHESIS);
        code.put(")", VK_RIGHT_PARENTHESIS);
        code.put("#", VK_NUMBER_SIGN);
        code.put("!", VK_EXCLAMATION_MARK);
        code.put("^", VK_CIRCUMFLEX);
        code.put("$", VK_DOLLAR);
        code.put("=", VK_EQUALS);
        code.put(",", VK_COMMA);
        code.put(".", VK_PERIOD);
        code.put("/", VK_SLASH);
        code.put("\\", VK_BACK_SLASH);
        code.put("'", VK_QUOTE);
        code.put("\"", VK_QUOTEDBL);
        code.put("`", VK_BACK_QUOTE);

        // key
        code.put("backspace", VK_BACK_SPACE);
        code.put("insert", VK_INSERT);
        code.put("delete", VK_DELETE);
        code.put("home", VK_HOME);
        code.put("end", VK_END);
        code.put("pageup", VK_PAGE_UP);
        code.put("pagedown", VK_DELETE);
        code.put("space", VK_SPACE);
        code.put("tab", VK_TAB);
        code.put("enter", VK_ENTER);

        code.put("up", VK_UP);
        code.put("down", VK_DOWN);
        code.put("left", VK_LEFT);
        code.put("right", VK_RIGHT);

        // function keys
        code.put("f1", VK_F1);
        code.put("f2", VK_F2);
        code.put("f3", VK_F3);
        code.put("f4", VK_F4);
        code.put("f5", VK_F5);
        code.put("f6", VK_F6);
        code.put("f7", VK_F7);
        code.put("f8", VK_F8);
        code.put("f9", VK_F9);
        code.put("f10", VK_F10);
        code.put("f11", VK_F11);
        code.put("f12", VK_F12);
    }


    protected TextEditKeys(TextEditActions actions) {
        this.actions = actions;
    }

    protected void execute(TextEditModel model, String name, String... args) {
        actions.run(model, name, args);
    }

    public abstract boolean key(KeyEvent e, boolean pressedOrReleased, TextEditModel editor);

}