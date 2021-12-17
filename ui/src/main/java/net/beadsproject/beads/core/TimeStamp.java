/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.core;

/**
 * TimeStamps store time with respect to the current {@link AudioContext}. Specifically, the TimeStamp stores a time step and an index into a buffer.
 */
public class TimeStamp {

    /**
     * The context.
     */
    private final AudioContext context;

    /**
     * The time step (AudioContext's count of sample frames).
     */
    private final long timeStep;

    /**
     * The index into the sample frame.
     */
    private final int index;

    /**
     * The time samples.
     */
    private long timeSamples;

    /**
     * Instantiates a new TimeStamp with the given time step, context and buffer index. Use {@link AudioContext#generateTimeStamp(int)} to generate a
     * TimeStamp for the current time.
     *
     * @param context  the AudioContext.
     * @param timeStep the time step.
     * @param index    the index.
     */
    public TimeStamp(AudioContext context, long timeStep, int index) {
        this.context = context;
        this.timeStep = timeStep;
        this.index = index;
    }

    /**
     * Instantiates a new TimeStamp with the given time step, context and buffer index. Use {@link AudioContext#generateTimeStamp(int)} to generate a
     * TimeStamp for the current time.
     *
     * @param context  the AudioContext.
     * @param timeStep the time step.
     * @param index    the index.
     */
    private TimeStamp(AudioContext context, long timeInSamples) {
        this.context = context;
        timeStep = timeInSamples / context.getBufferSize();
        index = (int) (timeInSamples % context.getBufferSize());
    }

    /**
     * Gets the time of the TimeStamp in milliseconds.
     *
     * @return the time in milliseconds.
     */
    private double getTimeMS() {
        /**
         * The time ms.
         */
        double timeMs = context.samplesToMs(getTimeSamples());
        return timeMs;
    }

    /**
     * Gets the time in samples.
     *
     * @return the time in samples.
     */
    private long getTimeSamples() {
        timeSamples = timeStep * context.getBufferSize() + index;
        return timeSamples;
    }

    public double since(TimeStamp oldest) {
        return getTimeMS() - oldest.getTimeMS();
    }

    public boolean isBefore(TimeStamp other) {
		return timeStep < other.timeStep || timeStep == other.timeStep && timeSamples < other.timeSamples;
    }

    public boolean isAfter(TimeStamp other) {
		return timeStep > other.timeStep || timeStep == other.timeStep && timeSamples > other.timeSamples;
    }

    /**
     * Returns the time stamp formatted as <tt>timeStep-index</tt>, useful
     * for debugging purposes.
     */
    public String toString() {
        return timeStep + "-" + index;
    }

    public static TimeStamp subtract(AudioContext ac, TimeStamp a, TimeStamp b) {
        return new TimeStamp(ac, a.getTimeSamples() - b.getTimeSamples());
    }

    public static TimeStamp add(AudioContext ac, TimeStamp a, TimeStamp b) {
        return new TimeStamp(ac, a.getTimeSamples() + b.getTimeSamples());
    }

}
