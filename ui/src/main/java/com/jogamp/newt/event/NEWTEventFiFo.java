package com.jogamp.newt.event;

import jcog.data.list.MetalConcurrentQueue;
import jcog.data.list.MetalRing;

/** enhanced NWTEventFiFo implementation using un-synchronized MetalConcurrentQueue.
 * for smoother input control */
public class NEWTEventFiFo
{
    static final int CAPACITY = 128;
    private final MetalRing<com.jogamp.newt.event.NEWTEvent> events = new MetalConcurrentQueue<>(CAPACITY)/*<NEWTEvent>*/;

    /** Add NEWTEvent to tail */
    public void put(NEWTEvent event) {
        events.add(event);

        //notifyAll();
    }

    /** Remove NEWTEvent from head */
    public NEWTEvent get() {
//        if (0 == events.size()) {
//            return null;
//        }
//
//        return (NEWTEvent) events.removeFirst();
        return events.poll();
    }

    /** Get NEWTEvents in queue */
    public int size() {
        return events.size();
    }

    /** Clear all NEWTEvents from queue */
    public void clear() {
        events.clear();
    }

}