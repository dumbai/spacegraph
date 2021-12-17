/*
 *   __               .__       .__  ._____.           
 * _/  |_  _______  __|__| ____ |  | |__\_ |__   ______
 * \   __\/  _ \  \/  /  |/ ___\|  | |  || __ \ /  ___/
 *  |  | (  <_> >    <|  \  \___|  |_|  || \_\ \\___ \ 
 *  |__|  \____/__/\_ \__|\___  >____/__||___  /____  >
 *                   \/       \/             \/     \/ 
 *
 * Copyright (c) 2006-2011 Karsten Schmidt
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * http://creativecommons.org/licenses/LGPL/2.1/
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */

package toxi.physics2d.behavior;

import jcog.Util;
import toxi.geom.SpatialIndex;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AttractionBehavior2D<V extends Vec2D> implements ParticleBehavior2D {

    protected V attractor;

    protected float radius;
    protected float strength;
    protected float jitter;
    protected float timeStep;

    final Random rng;

    public AttractionBehavior2D(V attractor, float radius, float strength) {
        this(attractor, radius, strength, 0, null);
    }

    public AttractionBehavior2D(V attractor, float radius, float strength, float jitter, Random rng) {
        this.attractor = attractor;
        this.strength = strength;
        this.jitter = jitter;
        this.rng = rng!=null ? rng : ThreadLocalRandom.current();
        setRadius(radius);
    }

    @Override
    public void accept(VerletParticle2D p) {
        if (p == attractor)
            return;
        Vec2D delta = attractor.sub(p);
        float distSq = delta.magSquared();
        if (distSq < Util.sqr(radius))
            move(p, delta, distSq);
    }


    private void move(VerletParticle2D p, Vec2D delta, float distSq) {
        Vec2D f;
        boolean repel = strength < 0;
        if (!repel && distSq <= radius) {
            //random direction
            float theta = (float) ((rng.nextFloat()) * Math.PI * 2);
            float rx = (float) Math.cos(theta);
            float ry = (float) Math.sin(theta);
            f = new Vec2D(rx, ry);
        } else {
            f = delta.normalizeTo(
                    //1.0f - distSq / (radius * radius)
                    (float) (1.0 / (1+(distSq - Math.max(0,(repel ? radius : 0)))/(radius*radius)))*p.mass
                    )
                    .jitter(rng, jitter);
        }
        p.addForce(f.scaleSelf(strength*timeStep));
    }

    @Override
    public void applyWithIndex(SpatialIndex<VerletParticle2D> spaceHash) {
        spaceHash.itemsWithinRadius(attractor, radius, this);
    }

    public void configure(float timeStep) {
        this.timeStep = timeStep;
    }

    /**
     * @return the attractor
     */
    public Vec2D getAttractor() {
        return attractor;
    }

    /**
     * @return the jitter
     */
    public float getJitter() {
        return jitter;
    }

    /**
     * @return the radius
     */
    public float getRadius() {
        return radius;
    }

    /**
     * @return the strength
     */
    public float getStrength() {
        return strength;
    }

    /**
     * @param attractor
     *            the attractor to setAt
     */
    public void setAttractor(V attractor) {
        this.attractor = attractor;
    }

    /**
     * @param jitter
     *            the jitter to setAt
     */
    public void setJitter(float jitter) {
        this.jitter = jitter;
    }

    public final void setRadius(float r) {
        this.radius = r;
    }

    /**
     * @param strength
     *            the strength to setAt
     */
    public void setStrength(float strength) {
        this.strength = strength;
    }

    @Override
    public boolean supportsSpatialIndex() {
        return true;
    }

}