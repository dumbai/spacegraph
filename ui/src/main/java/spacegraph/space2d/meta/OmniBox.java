package spacegraph.space2d.meta;

import com.jogamp.newt.event.KeyEvent;
import jcog.Log;
import jcog.Str;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.execution.LocalExecutionControlProvider;
import org.slf4j.Logger;
import spacegraph.SpaceGraph;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.collection.MutableListContainer;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.text.Labelling;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.space2d.widget.textedit.TextEdit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import jcog.User;
//import org.apache.lucene.document.Document;

/**
 * super repl
 */
public class OmniBox extends Widget {


    private final Gridding content;

    //    @Deprecated
//    public OmniBox() {
//        this(new JShellModel());
//    }


    public OmniBox(OmniBoxModel m) {
        super();

        content = new Gridding();

        Gridding results = new Gridding();// {
//            @Override
//            public void add(Surface... s) {
//                synchronized (content) {
//                    int sizeBefore = size();
//
//                    super.add(s);
//
//                    if (size() > 0 && sizeBefore == 0) {
//                        content.add(this);
//                    }
//                }
//            }
//
//            @Override
//            public Gridding clear() {
//                super.clear();
//                synchronized (content) {
//                    //content.detachChild(this);
//                    content.remove(this);
//                }
//                return this;
//            }

        //};


        //            @Override
        //            public TextEdit onKey(Consumer<KeyEvent> e) {
        //                return super.onKey(e);
        //            }
        //
        //            @Override
        //            protected void onKeyCtrlEnter() {
        //                String t = text();
        //                model.onTextChangeControlEnter(t, results);
        //                clear();
        //            }
        //
        //            @Override
        //            protected void cursorChange(String next, TerminalPosition cp) {
        //                if (cp.getRow() == 0) {
        //                    model.onTextChange(next, cp.getColumn(), results);
        //                } else {
        //                    results.clear();
        //                }
        //            }
        //
        //            @Override
        //            protected void textChange(String next) {
        //                super.textChange(next);
        //                TerminalPosition cp = getCursorPosition();
        //                cursorChange(next, cp);
        //            }
        TextEdit edit = new TextEdit(40);

//            @Override
//            public TextEdit onKey(Consumer<KeyEvent> e) {
//                return super.onKey(e);
//            }
//
//            @Override
//            protected void onKeyCtrlEnter() {
//                String t = text();
//                model.onTextChangeControlEnter(t, results);
//                clear();
//            }
//
//            @Override
//            protected void cursorChange(String next, TerminalPosition cp) {
//                if (cp.getRow() == 0) {
//                    model.onTextChange(next, cp.getColumn(), results);
//                } else {
//                    results.clear();
//                }
//            }
//
//            @Override
//            protected void textChange(String next) {
//                super.textChange(next);
//                TerminalPosition cp = getCursorPosition();
//                cursorChange(next, cp);
//            }
        edit.onChange.on(()->{
            int cx = (int) edit.cursor().x;
            m.onTextChange(edit.text(), cx, results);
        });
        edit.onKeyPress(k -> {
            if (k.isControlDown() && k.getKeyCode() == KeyEvent.VK_ENTER) {
                m.onTextChangeControlEnter(edit.text(),  results);
                edit.clear();
            }
        });

//        TextEdit0 te = new TextEdit0(edit);
//        te.resize(40, 1);

        content.add(edit, results);
        set(content);

    }

    @FunctionalInterface public interface OmniBoxModel {
        void onTextChange(String text, int cursorPos, MutableListContainer target);

        /** default: nothing */
        default void onTextChangeControlEnter(String t, MutableListContainer target) {

        }
    }

    public static class JShellModel implements OmniBoxModel {

        static final Logger logger = Log.log(JShellModel.class);

        private final JShell js;
        private final SourceCodeAnalysis jsAnalyze;

        private transient volatile String currentText = "";
        private transient volatile int currentPos;

        public JShellModel() {
            JShell.Builder builder = JShell.builder();
            Map<String, String> params = new HashMap<>();
            builder.executionEngine(new LocalExecutionControlProvider(), params);

            js = builder.build();

            jsAnalyze = js.sourceCodeAnalysis();
        }

        @Override
        public void onTextChangeControlEnter(String t, MutableListContainer target) {
            String text = t.trim();
            if (text.isEmpty())
                return;

            target.clear();

            String cmd = OmniBox.class.getName() + ".popup(" + Str.quote(text) + "," + text + ");";

            js.eval(cmd).forEach(e -> logger.info("{}:\n\t{}", text, e));
        }

        @Override
        public void onTextChange(String text, int cursorPos, MutableListContainer target) {
            currentText = text;
            currentPos = cursorPos;

            if (text.isEmpty()) {
                target.clear();
            } else {

                //Exe.runLater(() -> {
                if (cursorPos != currentPos || !text.equals(currentText))
                    return;  //early exit

                List<SourceCodeAnalysis.Suggestion> sugg = jsAnalyze.completionSuggestions(text,
                        cursorPos /* TODO take actual cursor pos */,
                        new int[1]);

                if (cursorPos != currentPos || !text.equals(currentText))
                    return; //early exit

                target.set(sugg.stream().map(SourceCodeAnalysis.Suggestion::continuation).sorted().map(PushButton::new).toArray(Surface[]::new));
//                System.out.println(Arrays.toString(target.children()));

                //});
            }
        }
    }


    public static void popup(String src, Object x) {
        Surface surface;
		surface = x instanceof String || x.getClass().isPrimitive() || x instanceof Number ? new VectorLabel(x.toString()) : new ObjectSurface(x);

        SpaceGraph.window(Labelling.the(src, surface), 800, 800);
    }


    /*protected void in(String s) {
        user.notice.emit("omnibox: " + s);
    }*/

}