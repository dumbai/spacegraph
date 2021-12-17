package spacegraph.space2d.widget.button;

import jcog.data.iterator.ArrayIterator;
import jcog.data.set.ArrayHashSet;
import org.eclipse.collections.api.block.procedure.primitive.ObjectBooleanProcedure;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.container.grid.Gridding;

/** set of buttons, which may be linked behaviorally in various ways */
public class ButtonSet<T extends ToggleButton> extends Gridding {


    /** uses both set and list (for ordering) aspects of the ArrayHashSet */
    public final ArrayHashSet<T> buttons = new ArrayHashSet<>();


    private ObjectBooleanProcedure<T> action;

    public enum Mode {
        /**  all disabled */
        Disabled,

        /** only one can be enabled at any time */
        One,

        /** multiple can be enabled */
        Multi
    }

    @SafeVarargs
    public ButtonSet(Mode mode, T... buttons) {
        this(mode, ArrayIterator.iterable(buttons));
    }

    public ButtonSet(Mode mode, Iterable<T> buttons) {
        super();

        /* TODO */

        for (T b : buttons) {
            this.buttons.add(b);
            @Nullable ObjectBooleanProcedure<ToggleButton> outerAction = b.action;
            b.on((bb,e) -> {
                if (e) {
                    switch (mode) {
                        case Multi:

                            break;
                        case One:
                            for (T cc : this.buttons) {
                                if (cc != bb) {
                                    cc.set(false);
                                }
                            }
                            break;
                    }


                    if (outerAction != null)
                        outerAction.value(bb, true);
                    if (action!=null)
                        action.value((T)bb, true);
                } else {

                    if (this.buttons.AND(cc -> cc==bb || !cc.get())) {
                        //no other buttons are toggled, so re-toggle this one
                        //Exe.invoke(()->{ //HACK
                           bb.set(true);
                        //});
                    }

                }

            });

        }

        this.set(this.buttons.list);


    }

//    @Override
//    protected void starting() {
//        super.starting();
//
////        Exe.later...
////        if (mode == Mode.One && buttons.AND(b -> !b.on())) {
////            this.buttons.first().on(true);
////        }
//    }

    public void on(ObjectBooleanProcedure<T> action) {
        this.action = action;
    }
}