/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.ugens;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Auvent;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.DataAuvent;
import net.beadsproject.beads.data.DataBeadReceiver;

/**
 * A simple panning object that takes a mono input and gives stereo output.
 * Power is kept constant regardless of position; note that center-panning a
 * signal will yield the input signal multiplied by 1 / sqrt(2) in each output
 * channel as a result. A pan value of -1 pans completely to the left, 1 pans
 * completely to the right, and 0 results in center panning. It uses an array to
 * approximate square roots for efficiency.
 *
 * @author Benito Crawford
 * @version 0.9.1
 * @beads.category utilities
 */
public class Panner extends UGen implements DataBeadReceiver {

    private static final int rootSize = 1024;
    private static final float[] ROOTS = buildRoots(rootSize);
    private float pos;
    private float p1;
    private float p2;
    private UGen posUGen;
    private boolean isPosStatic;

    /**
     * Constructor that sets the pan to the middle by default.
     *
     * @param con The audio context.
     */
    public Panner(AudioContext con) {
        this(con, 0);
    }

    /**
     * Constructor that sets the pan to a static value.
     *
     * @param con  The audio context.
     * @param ipos The initial pan value.
     */
    public Panner(AudioContext con, float ipos) {
        super(con, 1, 2);
        setPos(ipos);
    }

    /**
     * Constructor that sets a UGen to specify the pan value.
     *
     * @param con     The audio context.
     * @param posUGen The pan UGen.
     */
    public Panner(AudioContext con, UGen posUGen) {
        super(con, 1, 2);
        setPos(posUGen);
    }

    @Override
    public void gen() {

        float[] bi = bufIn[0];
        float[] bo1 = bufOut[0];
        float[] bo2 = bufOut[1];

        if (isPosStatic) {

            for (int currsample = 0; currsample < bufferSize; currsample++) {
                bo1[currsample] = p1 * bi[currsample];
                bo2[currsample] = p2 * bi[currsample];
            }

        } else {

            posUGen.update();

            for (int currsample = 0; currsample < bufferSize; currsample++) {

                if ((pos = posUGen.getValue(0, currsample)) >= 1) {
                    p1 = 0;
                    p2 = 1;
                } else if (pos <= -1) {
                    p1 = 1;
                    p2 = 0;
                } else {
                    int n1;
                    float f = (pos + 1) * 0.5f * rootSize;
                    f -= (n1 = (int) Math.floor(f));
                    p2 = ROOTS[n1] * (1 - f) + ROOTS[n1 + 1] * f;
                    p1 = ROOTS[rootSize - n1] * (1 - f)
                            + ROOTS[rootSize - (n1 + 1)] * f;
                }

                bo1[currsample] = p1 * bi[currsample];
                bo2[currsample] = p2 * bi[currsample];
            }
        }
    }

    /**
     * Calculates an array of square-roots from 0 to 1.
     *
     * @param rs The size of the array minus 2.
     * @return The array.
     */
    private static float[] buildRoots(int rs) {
        float[] roots = new float[rs + 2];
        for (int i = 0; i < rs + 1; i++) {
            roots[i] = (float) Math.sqrt((float) i / rs);
        }
        roots[rs + 1] = 1;
        return roots;
    }

    /**
     * Gets the current pan position.
     *
     * @return The pan position.
     */
    public float getPos() {
        return pos;
    }

    /**
     * Sets the pan position to a static float value.
     *
     * @param pos The pan position.
     * @return This Panner instance.
     */
    private Panner setPos(float pos) {
        if ((this.pos = pos) >= 1) {
            p1 = 0;
            p2 = 1;
        } else if (pos <= -1) {
            p1 = 1;
            p2 = 0;
        } else {
            int n1;
            float f = (pos + 1) * 0.5f * rootSize;
            f -= (n1 = (int) Math.floor(f));
            p2 = ROOTS[n1] * (1 - f) + ROOTS[n1 + 1] * f;
            p1 = ROOTS[rootSize - n1] * (1 - f) + ROOTS[rootSize - (n1 + 1)]
                    * f;
        }
        isPosStatic = true;
        posUGen = null;
        return this;
    }

    /**
     * Sets a UGen to specify the pan position.
     *
     * @param posUGen The pan UGen.
     * @return This Panner instance.
     */
    private Panner setPos(UGen posUGen) {
        if (posUGen == null) {
            setPos(pos);
        } else {
            this.posUGen = posUGen;
            posUGen.update();
            pos = posUGen.getValue();
            isPosStatic = false;
        }

        return this;
    }

    /**
     * Gets the pan UGen, if it exists.
     *
     * @return The pan UGen.
     */
    public UGen getPosUGen() {
        return isPosStatic ? null : posUGen;
    }

    /**
     * Sets the parameter with a DataBead.
     * <p>
     * Use the "position" property to specify pan position.
     *
     * @param paramBead The DataBead specifying parameters.
     * @return This filter instance.
     */
    private Panner setParams(DataAuvent paramBead) {
        if (paramBead != null) {
            Object o;

            if ((o = paramBead.get("position")) != null) {
                if (o instanceof UGen) {
                    setPos((UGen) o);
                } else {
                    setPos(paramBead.getFloat("position", pos));
                }
            }

        }
        return this;
    }

    @Override
    public void on(Auvent message) {
        if (message instanceof DataAuvent) {
            setParams((DataAuvent) message);
        }
    }

    /**
     * Gets a DataBead with the pan position (whether float or UGen), stored in
     * the key "position".
     *
     * @return The DataBead with the stored parameter.
     */
    public DataAuvent getParams() {
        DataAuvent db = new DataAuvent();

        if (isPosStatic) {
            db.put("position", pos);
        } else {
            db.put("position", posUGen);
        }

        return db;
    }

    /**
     * Gets a DataBead with property "position" set to its current float value.
     *
     * @return The DataBead with the static float parameter value.
     */
    public DataAuvent getStaticParams() {
        DataAuvent db = new DataAuvent();
        db.put("position", pos);
        return db;
    }

    /**
     * Sets the pan position with a DataBead.
     *
     * @see #setParams(DataAuvent)
     */
    @Override
    public DataBeadReceiver sendData(DataAuvent db) {
        setParams(db);
        return this;
    }

}