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

import toxi.geom.SpatialIndex;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;

import java.util.function.Consumer;

@FunctionalInterface public interface ParticleBehavior2D extends Consumer<VerletParticle2D> {

    /**
     * Applies the constraint to the passed in particle. The method is assumed
     * to manipulate the given instance directly.
     * 
     * @param p
     *            particle
     */

    default void applyWithIndex(SpatialIndex<VerletParticle2D> index) {
        throw new UnsupportedOperationException();
    }

    default void configure(float timeStep) {

    }

    default boolean supportsSpatialIndex() {
        return false;
    }

    default void applyGlobal(VerletPhysics2D p) {
        if (p != null && supportsSpatialIndex()) {
            applyWithIndex(p.index);
        } else {
            ParticleBehavior2D c = this;
            for (VerletParticle2D particle : p.particles) {
                c.accept(particle);
            }
        }
    }


}