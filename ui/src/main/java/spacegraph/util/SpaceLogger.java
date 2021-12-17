package spacegraph.util;

//import ch.qos.logback.classic.Level;

import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;

import java.util.function.Supplier;

/** inspired by logging frameworks like slf4j, the analogy of this class would be slf4j.Logger
 *
 *  TODO extend the logging model to include
 *  more detailed and flexible realtime interactive aspects of user perception,
 *  and supportive defer and remind capabilities.*/
public interface SpaceLogger {

    /** new log message will likely replace an existing log message by the same key. */
    default void log(@Nullable Object key, float duration /* seconds */, Level level, Supplier<String> message) {
        if (logging(level))
            System.out.println(message.get());
    }

    default boolean logging(Level level) {
        return true;
    }

    default void debug(@Nullable Object key, float duration /* seconds */, Supplier<String> message) {
        log(key, duration, Level.DEBUG, message);
    }
    default void debug(@Nullable Object key, float duration /* seconds */, Object x) {
        log(key, duration, Level.DEBUG, x::toString);
    }
    default void debug(@Nullable Object key, float duration /* seconds */, String message) {
        log(key, duration, Level.DEBUG, ()->message);
    }
    default void info(@Nullable Object key, float duration /* seconds */, Supplier<String> message) {
        log(key, duration, Level.INFO, message);
    }
    default void info(@Nullable Object key, float duration /* seconds */, String message) {
        log(key, duration, Level.INFO, ()->message);
    }

    default void error(@Nullable Object key, float duration /* seconds */, Throwable error) {
        log(key, duration, Level.ERROR, error::toString);
    }

    default void error(@Nullable Object key, float duration /* seconds */, Supplier<String> message) {
        log(key, duration, Level.ERROR, message);
    }
    default void error(@Nullable Object key, float duration /* seconds */, String message) {
        log(key, duration, Level.ERROR, ()->message);
    }

}