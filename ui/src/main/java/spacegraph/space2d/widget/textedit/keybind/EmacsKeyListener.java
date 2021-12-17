package spacegraph.space2d.widget.textedit.keybind;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.jogamp.newt.event.KeyEvent;
import jcog.data.list.Lst;
import spacegraph.space2d.widget.textedit.TextEditActions;
import spacegraph.space2d.widget.textedit.TextEditModel;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jogamp.newt.event.KeyEvent.*;
import static spacegraph.space2d.widget.textedit.keybind.SupportKey.*;

public class EmacsKeyListener extends TextEditKeys {
    private static final Pattern ACTION_PATTERN = Pattern.compile("-?([^\\-]+)\\z");

    /** TODO use Trie */
    private final Map<List<Stroke>, String> keybinds = new LinkedHashMap();

//    private static final long delta = 100;


    public EmacsKeyListener() {
        super(TextEditActions.DEFAULT_ACTIONS);
        try {
            URL url = Resources.getResource("spacegraph/space2d/widget/textedit/emacs.setting");
            Resources.readLines(url, Charsets.UTF_8).forEach(this::parseSetting);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //private long when;
    //private boolean executed;
    //private boolean inStroke;


    //TODO
    //private final ArrayDeque<Stroke> currentStrokes = new ArrayDeque(4);


    private void parseSetting(String line) {
        if (line.isEmpty() || !line.isEmpty() && line.charAt(0) == '#') {
            return;
        }
        String[] split = line.split(" ");
        String[] keys = Arrays.copyOfRange(split, 0, split.length - 1);
        String action = split[split.length - 1];

        List<Stroke> strokes = new Lst<>(keys.length);
        for (String key : keys)
            strokes.add(getStroke(key));
        keybinds.put(strokes, action);
    }

    private static Stroke getStroke(String key) {
        Matcher m = ACTION_PATTERN.matcher(key);
        if (!m.find())
            throw new RuntimeException("invalid config.");

        String actionString = m.group(1);
        int code = TextEditKeys.code.get(actionString);
        if (key.startsWith("C-A-S-")) {
            return new Stroke(CTRL_ALT_SHIFT, code);
        } else if (key.startsWith("C-A-")) {
            return new Stroke(CTRL_ALT, code);
        } else if (key.startsWith("C-S-")) {
            return new Stroke(CTRL_SHIFT, code);
        } else if (key.startsWith("A-S-")) {
            return new Stroke(ALT_SHIFT, code);
        } else if (key.startsWith("C-")) {
            return new Stroke(CTRL, code);
        } else if (key.startsWith("A-")) {
            return new Stroke(ALT, code);
        } else if (key.startsWith("S-")) {
            return new Stroke(SHIFT, code);
        } else {
            return new Stroke(NONE, code);
        }
    }

    @Override
    public boolean key(KeyEvent e, boolean pressedOrReleased, TextEditModel editor) {
        //System.out.println(e + " " + pressedOrReleased);

//        if (pressedOrReleased) {
//            if (!e.isPrintableKey())
//                model.keyPressed(null, e.getKeyCode(), e.getWhen());
//        } else {
//            //if (e.isPrintableKey())
//            model.keyTyped(e.getKeyChar(), e.getWhen());
//            //else  editor.keyReleased...
//        }
        if (pressedOrReleased) {

            if (!keyPressed(SupportKey.NONE /* TODO null */, e.getKeyCode(), e.getWhen(), editor)) {

                if (e.isPrintableKey())
                    actions.TYPE.execute(editor, String.valueOf(e.getKeyChar()));
                else
                    return false;

            }
        }
        return true;
    }


    private boolean keyPressed(SupportKey supportKey, int keyCode, long when, TextEditModel model) {
//        this.when = when;

        if (keyCode == VK_SHIFT || keyCode == VK_ALT || keyCode == VK_CONTROL) {
//            this.executed = true;
            return true;
        }

        Stroke stroke = new Stroke(supportKey, keyCode);

//        synchronized(this) {
//            currentStrokes.addAt(stroke);
            String actionName = getActionName(stroke);
            if (actionName != null) {
                execute(model, actionName);
                //this.executed = true;
                return true;
            } else {
//                this.executed = inStroke;
            }
            return false;
            //return executed;
//        }
    }

    /** single stroke */
    private String getActionName(Stroke s) {
        //inStroke = false;
        return keybinds.get(List.of(s));
//        if (v!=null) {
//            //currentStrokes.clear();
//            return v;
//        }
//        for (Entry<List<Stroke>, String> keybind : keybinds.entrySet()) {
//            List<Stroke> keybindStrokes = keybind.getKey();
//            //if (((FasterList)keybindStrokes).indexOf..
//            if (containStroke(keybindStrokes, currentStrokes)) {
//                inStroke = true;
//                break;
//            }
//        }
//        if (!inStroke) {
//            currentStrokes.clear();
//        }
//        return null;
    }

    private static boolean containStroke(List<Stroke> keybinding, List<Stroke> current) {
        int cs = current.size();
        if (cs > keybinding.size()) {
            return false;
        }
        for (int i = 0; i < cs; i++)
            if (current.get(i).equals(keybinding.get(i))) {
                return true;
        }
        return false;
    }


}
