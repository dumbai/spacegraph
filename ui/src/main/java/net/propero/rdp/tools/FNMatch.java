package net.propero.rdp.tools;

import java.io.File;

class FNMatch {

    

    /**
     * No wildcard can ever match `/'. A constant for bits set in the FLAGS
     * argument to fnmatch().
     */
    private static final int FNM_PATHNAME = 1;

    
    /**
     * Backslashes don't quote special chars. A constant for bits set in the
     * FLAGS argument to fnmatch().
     */
    private static final int FNM_NOESCAPE = 1 << 1;

    
    /**
     * Leading `.' is matched only explicitly. A constant for bits set in the
     * FLAGS argument to fnmatch().
     */
    private static final int FNM_PERIOD = 1 << 2;
    /**
     * Preferred GNU name. A constant for bits set in the FLAGS argument to
     * fnmatch().
     */
    private static final int FNM_FILE_NAME = FNM_PATHNAME;
    /**
     * Ignore `/...' after a match. A constant for bits set in the FLAGS
     * argument to fnmatch().
     */
    private static final int FNM_LEADING_DIR = 1 << 3;
    /**
     * Compare without regard to case. A constant for bits set in the FLAGS
     * argument to fnmatch().
     */
    private static final int FNM_CASEFOLD = 1 << 4;
    /**
     * Value returned by fnmatch() if STRING does not match PATTERN.
     */
    private static final boolean FNM_NOMATCH = false;
    /**
     * Value returned by fnmatch() if STRING matches PATTERN.
     */
    private static final boolean FNM_MATCH = true;

    /**
     * Match STRING against the filename pattern PATTERN, returning FNM_MATCH if
     * it matches, FNM_NOMATCH if not.
     *
     * @param pattern A string with a wildcard (* . ? [ ], etc.; \ is the escape).
     * @param string  The string to check for wildcards.
     * @param flags   Behavior modifiers.
     * @return Constant int value FNM_MATCH or FNM_NOMATCH.
     */
    private static boolean fnmatch(String pattern, String string,
                                   int flags) {
        boolean result = FNM_NOMATCH;
        boolean finished = false;

        int len = pattern.length();
                                       int n = 0;
                                       for (int p = 0; p < len; p++) {
                                           char c = pattern.charAt(p);
                                           c = fold(c, flags);
                                           char c1;
                                           switch (c) {
                                               case '?':
                                                   if (string.length() == n) {
                                                       finished = true;
                                                       break;
                                                   } else if ((flags & FNM_FILE_NAME) != 0
                                                           && string.charAt(n) == File.separatorChar) {
                                                       finished = true;
                                                       break;
                                                   } else if ((flags & FNM_PERIOD) != 0
                                                           && string.charAt(n) == '.'
                                                           && (n == 0 || (flags & FNM_FILE_NAME) != 0
                                                           && string.charAt(n - 1) == File.separatorChar)) {
                                                       finished = true;
                                                       break;
                                                   }
                                                   break;

                                               case '\\':
                                                   if ((flags & FNM_NOESCAPE) != 0) c = fold(pattern.charAt(p++), flags);
                                                   if (fold(string.charAt(n), flags) != c) {
                                                       finished = true;
                                                       break;
                                                   }
                                                   break;

                                               case '*':
                                                   if ((flags & FNM_PERIOD) != 0
                                                           && string.charAt(n) == '.'
                                                           && (n == 0 || (flags & FNM_FILE_NAME) != 0
                                                           && string.charAt(n - 1) == File.separatorChar)) {
                                                       finished = true;
                                                       break;
                                                   }
                                                   for (c = pattern.charAt(p++); c == '?' || c == '*'; c = pattern
                                                           .charAt(p++), ++n) {
                                                       if (p == pattern.length()) {
                                                           result = FNM_MATCH;
                                                           finished = true;
                                                           break;
                                                       }
                                                       if (((flags & FNM_FILE_NAME) != 0 && string.charAt(n) == File.separatorChar)
                                                               || (c == '?' && string.length() == n)) {
                                                           finished = true;
                                                           break;
                                                       }
                                                   }
                                                   if (finished) break;
                                                   if (p == pattern.length()) {
                                                       result = FNM_MATCH;
                                                       finished = true;
                                                       break;
                                                   }

                                                   c1 = ((flags & FNM_NOESCAPE) == 0 && c == '\\') ? pattern.charAt(p) : c;
                                                   c1 = fold(c1, flags);
                                                   for (--p; string.length() != n; ++n)
                                                       if ((c == '[' || fold(string.charAt(n), flags) == c1)
                                                               && fnmatch(pattern.substring(p),
                                                               string.substring(n), flags & ~FNM_PERIOD)) {
                                                           result = FNM_MATCH;
                                                           finished = true;
                                                           break;
                                                       }
                                                   if (finished) break;
                                                   finished = true;
                                                   break;

                                               case '[':


                                                   if (string.length() == n) {
                                                       finished = true;
                                                       break;
                                                   }

                                                   if ((flags & FNM_PERIOD) != 0
                                                           && string.charAt(n) == '.'
                                                           && (n == 0 || (flags & FNM_FILE_NAME) != 0
                                                           && string.charAt(n - 1) == File.separatorChar)) {
                                                       finished = true;
                                                       break;
                                                   }
                                                   boolean not = (pattern.charAt(p) == '!' || pattern.charAt(p) == '^');
                                                   if (not)
                                                       ++p;


                                                   c = pattern.charAt(++p);
                                                   boolean matched = false;
                                                   for (; ; ) {
                                                       char cstart = c, cend = c;
                                                       if ((flags & FNM_NOESCAPE) == 0 && c == '\\')
                                                           cstart = cend = pattern.charAt(p++);
                                                       cstart = cend = fold(cstart, flags);
                                                       if (p == pattern.length()) {
                                                           finished = true;
                                                           break;
                                                       }
                                                       c = fold(pattern.charAt(++p), flags);

                                                       if ((flags & FNM_FILE_NAME) != 0 && c == File.separatorChar) {
                                                           finished = true;
                                                           break;
                                                       }
                                                       if (c == '-' && pattern.charAt(p) != ']') {
                                                           cend = pattern.charAt(p++);
                                                           if ((flags & FNM_NOESCAPE) == 0 && cend == '\\') cend = pattern.charAt(p++);
                                                           if (p == pattern.length()) {
                                                               finished = true;
                                                               break;
                                                           }
                                                           cend = fold(cend, flags);
                                                           c = pattern.charAt(p++);
                                                       }

                                                       c1 = fold(string.charAt(n), flags);
                                                       if (c1 >= cstart && c1 <= cend) {
                                                           matched = true;
                                                           break;
                                                       }
                                                       if (c == ']')
                                                           break;
                                                   }
                                                   if (finished) break;
                                                   if (!not && !matched) {
                                                       finished = true;
                                                       break;
                                                   }
                                                   if (!matched)
                                                       break;


                                                   while (c != ']') {
                                                       if (p == pattern.length()) {
                                                           finished = true;
                                                           break;
                                                       }
                                                       c = pattern.charAt(p++);
                                                       if ((flags & FNM_NOESCAPE) == 0 && c == '\\') ++p;
                                                   }
                                                   if (finished) break;
                                                   if (not) {
                                                       finished = true;
                                                       break;
                                                   }
                                                   break;

                                               default:
                                                   if (n >= string.length() || c != fold(string.charAt(n), flags)) {
                                                       finished = true;
                                                       break;
                                                   }
                                           }
                                           if (finished) break;

                                           ++n;
                                       }
        if (!finished) {
            if (string.length() == n) {
                result = FNM_MATCH;
            } else if ((flags & FNM_LEADING_DIR) != 0
                    && string.charAt(n) == File.separatorChar) {
                result = FNM_MATCH;
            }

        }

        return result;
    }

    /**
     * If flags has its FNM_CASEFOLD bit setAt, then returns the lowercase of c;
     * otherwise returns c.
     *
     * @param c     A character to fold.
     * @param flags Bits, set or not, to modify behavior.
     * @return A `folded' character.
     */
    private static char fold(char c, int flags) {
        return (flags & FNM_CASEFOLD) != 0 ? Character.toLowerCase(c) : c;
    }

}