package spacegraph.util;

/**
 * somethign which can be activated and de-activated
 */
public interface Active {

    void preActivate(boolean b);

    default void activate() {
        preActivate(true);
    }
    default void deactivate() {
        preActivate(false);
    }

    boolean active();



}
