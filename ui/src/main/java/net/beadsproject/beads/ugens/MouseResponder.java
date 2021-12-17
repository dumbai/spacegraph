/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;

import java.awt.*;

/**
 * A MouseResponder is a way of getting mouse input to control audio rate data. The mouse doesn't generate audio rate data, but it is interpolated.
 *
 * @beads.category utilities
 * @beads.category control
 */
public class MouseResponder extends UGen {

    /**
     * The current mouse point.
     */
    private Point point;

    private float prevX;

    private float prevY;

    /**
     * The screen width.
     */
    private final int width;

    /**
     * The screen height.
     */
    private final int height;

    /**
     * Instantiates a new MouseResponder.
     *
     * @param context the AudioContext.
     */
    public MouseResponder(AudioContext context) {
        super(context, 2);
        width = Toolkit.getDefaultToolkit().getScreenSize().width;
        height = Toolkit.getDefaultToolkit().getScreenSize().height;
        prevX = 0;
        prevY = 0;
    }

    /**
     * Gets the current point.
     *
     * @return the point.
     */
    public Point getPoint() {
        return point;
    }

    /* (non-Javadoc)
     * @see com.olliebown.beads.core.UGen#calculateBuffer()
     */
    @Override
    public void gen() {
        point = MouseInfo.getPointerInfo().getLocation();
        /**
         * The current x value.
         */
        float x = (float) point.x / width;
        /**
         * The current y value.
         */
        float y = (float) point.y / height;
        for (int i = 0; i < bufferSize; i++) {
            float f = (float) i / bufferSize;
            bufOut[0][i] = f * x + (1.0f - f) * prevX;
            bufOut[1][i] = f * y + (1.0f - f) * prevY;
        }
        prevX = x;
        prevY = y;
    }

}