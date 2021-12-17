//package spacegraph.space2d.widget.meta;
//
//import ch.qos.logback.classic.Level;
//import com.googlecode.lanterna.TextColor;
//import com.googlecode.lanterna.graphics.SimpleTheme;
//import org.jetbrains.annotations.Nullable;
//import spacegraph.space2d.container.grid.Gridding;
//import spacegraph.space2d.widget.console.ConsoleGUI;
//import spacegraph.space2d.widget.console.TextEdit0;
//import spacegraph.util.SpaceLogger;
//
//import java.util.function.Supplier;
//
//public class SpaceLogConsole extends Gridding implements SpaceLogger {
//
//    private final int MAX_LINES = 5;
//    private final TextEdit0.TextEditUI text = new TextEdit0.TextEditUI(40, MAX_LINES);
//    private ConsoleGUI textGUI;
//
//    public SpaceLogConsole() {
//        super();
//        text.textBox.setReadOnly(true);
//
//
//        SimpleTheme theme = new SimpleTheme(TextColor.ANSI.WHITE, TextColor.ANSI.BLACK);
//
//        text.textBox.setTheme(theme);
//
//
//    }
//
//    @Override
//    protected void starting() {
//        super.starting();
//            textGUI = new TextEdit0(text);
//
//            set(textGUI);
//        }
//
//    @Override
//    public void log(@Nullable Object key, float duration, Level level, Supplier<String> message) {
//
//        synchronized(text) {
//            int lines = text.getBufferLineCount();
//
//            text.limitLines(MAX_LINES);
//
//            text.setCursorPosition(0,lines);
//            text.addLine(key + " " + message.get());
//        }
//    }
//}
