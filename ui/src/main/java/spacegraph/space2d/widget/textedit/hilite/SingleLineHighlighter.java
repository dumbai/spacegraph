package spacegraph.space2d.widget.textedit.hilite;

import spacegraph.space2d.widget.textedit.view.LineView;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class SingleLineHighlighter {
    public static void main(String[] args) {
//        JPCSimpleTextEditor textDisplay = new JPCSimpleTextEditor() {
//            @Override public ContentEditor createContentEditor(JPC_Connector c) {

        //java syntax example

        //what doesn't work::
        //    1 this for example:: if(s.contains("/*")                            // /* itself will be grey
        //    2 this for example:: if(s.contains("/*") && bs.contains("*/")) {}   ////everything between /* and */ will be greyed out
        //    ((3 multi line comments(not really supposed to work either).
        //    ((4 braces highlighting ((that actually doesn't even work with this idea, since recalculateDisplayLines isn't called for that
        SingleLineHighlighter javaSyntaxHilite = new SingleLineHighlighter();
        javaSyntaxHilite.addWordBeforeMatchRule(new TextStyle(Color.MAGENTA.darker().darker(), null, null), "(");
        javaSyntaxHilite.addWordMatchRules(new TextStyle(Color.blue, null, null),
                "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double",
                "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
                "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized",
                "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "false", "null", "true");
        javaSyntaxHilite.addBetweenMatchesRule(new TextStyle(Color.ORANGE.darker(), null, null), "\"", "\"", "\\");
        javaSyntaxHilite.addBetweenMatchesRule(new TextStyle(Color.ORANGE.darker(), null, null), "'", "'", "\\");
        javaSyntaxHilite.addEverythingAfterMatchExceptInBetweenMatchRule(new TextStyle(new Color(160, 160, 160), null, null), "//", "\"");
        javaSyntaxHilite.addBetweenMatchesRule(new TextStyle(new Color(160, 160, 160), null, null), "/*", "*/", null);
//                return lighter;
//            }
//        };
//        JPC_Scroller scroller = new JPC_Scroller(textDisplay);
//
//        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_SIZE_UP(textDisplay));
//        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_SIZE_DOWN(textDisplay));
//        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_CYCLE_STYLE(textDisplay));
//        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_CYCLE_FG_COLOR(textDisplay));
//        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_CYCLE_BG_COLOR(textDisplay));
//
//        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
//
//        FrameStarter.start("JPC Editor - this edition scrolls and highlights simplest java syntax", scroller);
    }

    private final List<SyntaxRule> rules;

    private SingleLineHighlighter(SyntaxRule... rules) {
        this.rules = java.util.List.of(rules);
    }

    private void addWordMatchRule(TextStyle highlight, String word) {
        rules.add(new WordMatchRule(word, highlight));
    }

    private void addWordMatchRules(TextStyle highlight, String... words) {
        for (String word : words) addWordMatchRule(highlight, word);
    }

    private void addBetweenMatchesRule(TextStyle highlight, String start, String end, String escape) {
        rules.add(new BetweenMatchesRule(start, end, escape, highlight));
    }

    public void addEverythingAfterMatchRule(TextStyle highlight, String match) {
        rules.add(new EverythingAfterMatchRule(match, highlight));
    }

    private void addEverythingAfterMatchExceptInBetweenMatchRule(TextStyle highlight, String after_match, String in_between_match) {
        rules.add(new EverythingAfterMatchExceptInBetweenMatchRule(after_match, in_between_match, highlight));
    }

    private void addWordBeforeMatchRule(TextStyle highlight, String match) {
        rules.add(new WordBeforeMatchRule(match, highlight));
    }

    //override possible::
    private static final String wordMatcher = "[A-Za-z0-9_]+";


//    @Override public void recalculateDisplayLine(int line)  {
////        if (block_recalculateDisplayLines) return;
//        if(displayLines.size()==getLineCount()) {
////            for (int i = firstAffectedLine; i <= lastAffectedLine; i++) {
//                LineView disp_line = getLine(line);
//                for(SyntaxRule r:rules)
//                    disp_line=r.apply(disp_line); //entails a ton of line copying
//                disp_line.updatePixelKnowledge(jpc_connector, getStandardLayout());
//                displayLines.setAt(line, disp_line);
////            }
//        } else {
//            displayLines = new ArrayList<>(getLineCount());
//            for (int i = 0; i < getLineCount(); i++) {
//                LineView disp_line = getLine(i);
//                for(SyntaxRule r:rules)
//                    disp_line=r.apply(disp_line); //entails a ton of line copying
//                disp_line.updatePixelKnowledge(jpc_connector, getStandardLayout());
//                displayLines.addAt(disp_line);
//            }
//        }
//        jpc_connector.repaint(); //usually not required, but sometimes(and in not easily detectable situations) it is.
//    }


    abstract static class SyntaxRule {
        abstract LineView apply(LineView line);
    }

    private static class WordMatchRule extends SyntaxRule {
        private static final Pattern wordMatcherer = Pattern.compile(wordMatcher);
        private final String word;
        private final TextStyle highlight;

        private WordMatchRule(String word, TextStyle highlight) {
            this.word = word;
            this.highlight = highlight;
        }

        @Override
        LineView apply(LineView line) {
            String s = line.toString();
            int index_of_word = -1;
            while ((index_of_word = s.indexOf(word, index_of_word + 1)) != -1) {
                if (isFiniteWord(s, index_of_word, index_of_word + word.length()))
                    line = LineView.apply(index_of_word, index_of_word + word.length(), highlight);
            }
            return line;
        }

        private static boolean isFiniteWord(String line, int s, int e) {
            return (s <= 0 || !Character.toString(line.charAt(s - 1)).matches(wordMatcher)) &&
                    (e >= line.length() - 1 || !Character.toString(line.charAt(e)).matches(wordMatcher));
        }
    }

    private static class BetweenMatchesRule extends SyntaxRule {
        private final String start;
        private final String end;
        private final String escape; //for example in java a " can be escaped with a \
        private final TextStyle highlight;

        private BetweenMatchesRule(String start, String end, String escape, TextStyle highlight) {
            this.start = start;
            this.end = end;
            this.escape = escape;
            this.highlight = highlight;
        }

        @Override
        LineView apply(LineView line) {
            int start_index;
            int end_index = 0;
            while (true) {
                start_index = line.toString().indexOf(start, end_index);
                int escapeLen = escape != null ? escape.length() : -1;
                while (start_index != -1 && escape != null && start_index > escapeLen && LineView.substring(start_index - escapeLen, start_index).equals(escape)) {//skip escapes
                    start_index = line.toString().indexOf(start, start_index + 1);
                }
                end_index = line.toString().indexOf(end, Math.max(end_index, start_index + start.length()));
                while (end_index != -1 && escape != null && end_index > escapeLen && LineView.substring(end_index - escapeLen, end_index).equals(escape)) {//skip escapes
                    end_index = line.toString().indexOf(end, end_index + 1);
                }
                if (start_index != -1 && end_index != -1) {
                    line = LineView.apply(start_index, end_index += end.length(), highlight);
                } else
                    break;
            }
            if (start_index != -1 && end_index == -1)
                line = LineView.apply(start_index, start_index + start.length(), highlight);

            return line;
        }
    }

    private static class EverythingAfterMatchRule extends SyntaxRule {
        private final String match;
        private final TextStyle highlight;

        private EverythingAfterMatchRule(String match, TextStyle highlight) {
            this.match = match;
            this.highlight = highlight;
        }

        @Override
        LineView apply(LineView line) {
            int indexOf = line.toString().indexOf(match);
            if (indexOf != -1)
                line = LineView.apply(indexOf, line.length(), highlight);
            return line;
        }
    }

    private static class EverythingAfterMatchExceptInBetweenMatchRule extends SyntaxRule {
        private final String after_match;
        private final String in_between_match;
        private final TextStyle highlight;

        private EverythingAfterMatchExceptInBetweenMatchRule(String after_match, String in_between_match, TextStyle highlight) {
            this.after_match = after_match;
            this.in_between_match = in_between_match;
            this.highlight = highlight;
        }

        @Override
        LineView apply(LineView line) {
            int indexOf = line.toString().indexOf(after_match);
            if (indexOf != -1 && count_occurrences(line.toString(), in_between_match, indexOf) % 2 == 0)
                line = LineView.apply(indexOf, line.length(), highlight);
            return line;
        }

        private static int count_occurrences(String string, String occur, int upUntil) {
            int index = string.indexOf(occur);
            int count = 0;
            while (index != -1 && index < upUntil) {
                count++;
                index = string.indexOf(occur, index + occur.length());
            }
            return count;
        }
    }

    private static class WordBeforeMatchRule extends SyntaxRule {
        private final String match;
        private final TextStyle highlight;

        private WordBeforeMatchRule(String match, TextStyle highlight) {
            this.match = match;
            this.highlight = highlight;
        }

        @Override
        LineView apply(LineView line) {
            String s = line.toString();
            int index_of_match = -1;
            while ((index_of_match = s.indexOf(match, index_of_match + 1)) != -1) {
                int index_of_word = index_of_match;
                while (index_of_word >= 0 && !Character.toString(s.charAt(index_of_word)).matches(wordMatcher))
                    index_of_word--;
                int end_index_of_word = index_of_word;
                while (index_of_word >= 0 && Character.toString(s.charAt(index_of_word)).matches(wordMatcher))
                    index_of_word--;
                int start_index_of_word = index_of_word;
                if (start_index_of_word >= 0 && end_index_of_word > 0 && start_index_of_word != end_index_of_word)
                    line = LineView.apply(start_index_of_word, end_index_of_word + 1, highlight);
            }
            return line;
        }
    }
}