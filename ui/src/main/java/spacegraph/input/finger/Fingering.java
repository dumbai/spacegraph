package spacegraph.input.finger;

import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.Surface;

/** exclusive finger control state which a surface can activate in reaction to input events.
 * it has a specified termination condition (ex: button release) and
 * while active, locks any other surface from receiving interaction events.
 *
 * it can be used to drag move/resize with additional functionality or constraints
 * applied in real-time.
 * */
public abstract class Fingering {

    /** return true to allow begin */
    protected abstract boolean start(Finger f);

    /** return false to finish; called from inside the finger scan */
    public boolean update(Finger finger) {
        return true;
    }

    public void stop(Finger finger) {

    }

    /** whether this is allowed to continue updating the finger's currently
     * touched widget after it activates.
     * TODO maybe invert and name 'capture' or 'exlusive' or 'allowEscape'
     */
    public boolean escapes() {
        return false;
    }

    /** override to provide a custom renderer (cursor)
     * @param finger*/
    public @Nullable FingerRenderer renderer(Finger finger) {
        return null;
    }

    /** whether this state should automatically defer to a new incoming state
     * */
    public boolean defer(Finger finger) {
        return false;
    }

    //TODO just use 'null'
    @Deprecated public static final Fingering Idle = new Fingering() {

        @Override
        public String toString() {
            return "Idle";
        }

        @Override
        protected boolean start(Finger f) {
            return true;
        }

        @Override
        public boolean defer(Finger finger) {
            return true;
        }

        @Override
        public boolean escapes() {
            return true;
        }
    };

    /** post-filter for what will be set next in Finger.touching() while this is Fingering.
     *  by default it is a pass-through for the next
     **/
    public Surface touchNext(Surface prev, Surface next) {
        return next;
    }

    /** optional cursor override */
    public @Nullable FingerRenderer cursor() {
        return null;
    }

}
