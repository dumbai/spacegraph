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

package toxi.physics2d;

import jcog.WTF;
import jcog.data.list.FastCoWList;
import jcog.data.map.ConcurrentFastIteratingHashMap;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import toxi.geom.Rect;
import toxi.geom.SpatialIndex;
import toxi.geom.Vec2D;
import toxi.physics2d.behavior.ParticleBehavior2D;
import toxi.physics2d.constraint.ParticleConstraint2D;
import toxi.physics2d.spring.VerletSpring2D;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 3D particle physics engine using Verlet integration based on:
 * http://en.wikipedia.org/wiki/Verlet_integration
 * http://www.teknikus.dk/tj/gdc2001.htm
 */
public class VerletPhysics2D {


    /** maximum distance to consider moved */
    float epsilon = VerletSpring2D.EPS;

    private final Map<Object,Iterable<VerletParticle2D>> entities = new ConcurrentFastIteratingHashMap<>(new Iterable[0]);

    public void putEntity(Object k, Iterable<VerletParticle2D> e) {
        Iterable<VerletParticle2D> removed = entities.put(k, e);
        if (removed!=e) {
            if (removed!=null) removeParticles(removed);
            addParticles(e);
        }
    }

    public boolean removeEntity(Object x) {
        Iterable<VerletParticle2D> r = entities.remove(x);
        if (r!=null) {
            removeParticles(r);
            return true;
        }
        return false;
    }

    public final void addParticles(Iterable<VerletParticle2D> xx) {
        for (VerletParticle2D x : xx) addParticle(x);
    }

    private void removeParticles(Iterable<VerletParticle2D> xx) {
        for (VerletParticle2D x : xx) removeParticle(x);
    }



    /**
     * TODO use FastIteratingConcurrentHashMap indexed by particle ID
     */
    public final FastCoWList<VerletParticle2D> particles = new FastCoWList<>(VerletParticle2D[]::new);

    //public final FastCoWList<VerletSpring2D> springs = new FastCoWList<>(VerletSpring2D[]::new);
    //TODO public final ConcurrentFastIteratingHashMap<Long,VerletSpring2D> springs = new ConcurrentFastIteratingHashMap<>(new VerletSpring2D[]::new);

    public final FastCoWList<ParticleBehavior2D> behaviors = new FastCoWList<>(ParticleBehavior2D[]::new);

    public final FastCoWList<ParticleConstraint2D> constraints = new FastCoWList<>(ParticleConstraint2D[]::new);

    /**
     * Default iterations for verlet solver = 50
     */
    protected int maxIterations;

    /**
     * Optional bounding rect to constrain particles too
     */
    public RectF bounds;

    protected float drag = 0;

    public SpatialIndex<VerletParticle2D> index;


    /**
     * Initializes an Verlet engine instance with the passed in configuration.
     *
     * @param numIterations iterations per time step for verlet solver
     * @param drag          drag value 0...1
     */
    public VerletPhysics2D(int numIterations) {

        this.maxIterations = numIterations;

//        if (gravity != null)      addBehavior(new GravityBehavior2D(gravity));
    }

    public final ParticleBehavior2D addBehavior(ParticleBehavior2D behavior) {
        behaviors.add(behavior);
        return behavior;
    }

    public void addConstraint(ParticleConstraint2D constraint) {
        constraints.add(constraint);
    }

    /**
     * Adds a particle to the list
     *
     * @param p
     * @return itself
     */
    public @Nullable VerletPhysics2D addParticle(VerletParticle2D p) {

        p.constrainAll(bounds);

        if (!index.index(p))
            throw new WTF("could not index: " + p);

        particles.add(p);
        return this;
    }

    /**
     * Adds a spring connector
     *
     * @param s
     * @return itself
     */
    public VerletPhysics2D addSpring(VerletSpring2D s) {
        s.a.out.add(s);
        s.b.in.add(s);
        return this;
    }


    public VerletPhysics2D clear() {
        behaviors.clear();
        constraints.clear();
        particles.clear();
        return this;
    }

    public Rect getCurrentBounds() {
        Vec2D min = new Vec2D(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vec2D max = new Vec2D(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (VerletParticle2D p : particles) {
            min.minSelf(p);
            max.maxSelf(p);
        }
        return new Rect(min, max);
    }


    public boolean removeBehavior(ParticleBehavior2D c) {
        return behaviors.remove(c);
    }

    public boolean removeConstraint(ParticleConstraint2D c) {
        return constraints.remove(c);
    }

    /**
     * Removes a particle from the simulation.
     *
     * @param p particle to remove
     * @return true, if removed successfully
     */
    public boolean removeParticle(VerletParticle2D p) {
        index.unindex(p);
        //TODO remove associated springs
        return particles.remove(p);
    }

    /**
     * Removes a spring connector from the simulation instance.
     *
     * @param s spring to remove
     * @return true, if the spring has been removed
     */
    public boolean removeSpring(VerletSpring2D s) {
        if (s.a.out.remove(s)) {
            boolean y = s.b.in.remove(s);
            assert(y);
            return true;
        }
        return false;
    }

    /**
     * Removes a spring connector and its both end point particles from the
     * simulation
     *
     * @param s spring to remove
     * @return true, only if spring AND particles have been removed successfully
     */
    public boolean removeSpringAndItsParticles(VerletSpring2D s) {
        if (removeSpring(s)) {
            removeParticle(s.a);
            removeParticle(s.b);
            return true;
        }
        return false;
    }

    public final void setDrag(float drag) {
        this.drag = drag;
    }

    /**
     * @param index the index to setAt
     */
    public void setIndex(SpatialIndex<VerletParticle2D> index) {
        this.index = index;
    }

    /**
     * @param maxIterations the numIterations to setAt
     */
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }


    /**
     * Sets bounding box
     *
     * @param world
     * @return itself
     */
    public VerletPhysics2D setBounds(RectF world) {
        bounds = world;
        return this;
    }

    /**
     * Progresses the physics simulation by 1 time step and updates all forces
     * and particle positions accordingly
     *
     * @param dt
     * @return itself
     */
    public VerletPhysics2D update(float dt) {


        float maxTimeStep = 1.0f;
        dt = Math.min(dt, maxTimeStep); //hard uppper limit on timestep

        int ii = 0;
        while (++ii < maxIterations) {
            //in seconds
            float minTimeStep = 0.01f;
            if (dt / ii < minTimeStep)
                break;
            //else: further subdivide
        }

        float subDT = dt / ii;

        for (ParticleBehavior2D b : behaviors)
            b.configure(subDT);

        for (int i = ii - 1; i >= 0; i--) {
            preUpdate(subDT);
            spring(subDT);
            postUpdate();
            index(false /*true*/ /* TODO: if bounds changed */);
        }
        return this;
    }

    private void index(boolean force) {
        if (index != null) {
            for (VerletParticle2D p : particles) {
                assert (p != null);
                if (force || p.changed(epsilon)) {
                    index.reindex(p, VerletParticle2D::commit);
                } else {
                    p.commitInactive();
                }
            }
        } else {
            for (VerletParticle2D p : particles)
                p.commit();
        }
    }

    /**
     * Updates all particle positions
     */
    protected void preUpdate(float subDT) {

        //local behaviors
        particles.removeIf((t)-> !t.preUpdate(VerletPhysics2D.this, subDT));

        //global behaviors
        behaviors.forEachWith(ParticleBehavior2D::applyGlobal, this);
    }

    protected void postUpdate() {
        boolean hasGlobalConstraints = !constraints.isEmpty();
        for (VerletParticle2D p : particles) {
            p.postUpdate(drag);

            if (hasGlobalConstraints) {
                constraints.forEachWith(ParticleConstraint2D::accept, p);
            }
            if (p.bounds != null) {
                p.constrain(p.bounds);
            }
            if (bounds != null) {
                p.constrain(bounds);
            }
        }
    }

    /**
     * Updates all spring connections based on new particle positions
     *
     * @param subDT
     */
    protected void spring(float subDT) {
        particles.forEach(p -> p.out.removeIf(s -> !s.update(false)));
    }

    public void forEachSpring(Consumer<VerletSpring2D> each) {
        particles.forEach(p -> p.out.forEach(each));
    }

    public void bounds(RectF bounds) {
        index.bounds(bounds);
        this.bounds = bounds;
    }


}