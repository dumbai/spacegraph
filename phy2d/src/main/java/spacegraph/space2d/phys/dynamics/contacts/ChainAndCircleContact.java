/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 	* Redistributions of source code must retain the above copyright notice,
 * 	  this list of conditions and the following disclaimer.
 * 	* Redistributions in binary form must reproduce the above copyright notice,
 * 	  this list of conditions and the following disclaimer in the documentation
 * 	  and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package spacegraph.space2d.phys.dynamics.contacts;

import spacegraph.space2d.phys.collision.Manifold;
import spacegraph.space2d.phys.collision.shapes.ChainShape;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.EdgeShape;
import spacegraph.space2d.phys.collision.shapes.ShapeType;
import spacegraph.space2d.phys.common.Transform;
import spacegraph.space2d.phys.dynamics.Fixture;
import spacegraph.space2d.phys.pooling.IWorldPool;

public class ChainAndCircleContact extends Contact {

    public ChainAndCircleContact(IWorldPool argPool) {
        super(argPool);
    }

    @Override
    public void init(Fixture fA, int indexA, Fixture fB, int indexB) {
        super.init(fA, indexA, fB, indexB);
        assert (aFixture.type() == ShapeType.CHAIN);
        assert (bFixture.type() == ShapeType.CIRCLE);
    }

    private final EdgeShape edge = new EdgeShape();

    @Override
    public void evaluate(Manifold manifold, Transform xfA, Transform xfB) {
        ChainShape chain = (ChainShape) aFixture.shape();
        chain.getChildEdge(edge, aIndex);
        pool.getCollision().collideEdgeAndCircle(manifold, edge, xfA,
                (CircleShape) bFixture.shape(), xfB);
    }
}
