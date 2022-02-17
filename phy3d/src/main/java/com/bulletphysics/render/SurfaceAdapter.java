package com.bulletphysics.render;

import com.bulletphysics.collision.shapes.simple.BoxShape;
import com.jogamp.opengl.GL2;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.SurfaceGraph;
import spacegraph.space2d.Surfacelike;

/**
 * 2d -> 3d surface adapter
 */
public class SurfaceAdapter extends BoxShape.BoxSurface {

    final ReSurface r;
    private final Surface s;
    float virtualPixels;

    public SurfaceAdapter(Surface s) {
        this.s = s;
        s.start(new Surfacelike() {
            @Override
            public @Nullable SurfaceGraph root() {
                return null;
            }
        });
        r = new ReSurface();
        virtualPixels = 800;
    }

    @Override
    protected void renderSurface(GL2 gl2) {
        r.start(1, 1, 0, 1, 30, gl2); //HACK
        r.psw = r.psh = virtualPixels;
        s.renderIfVisible(r);
        r.gl = null; //release
    }
}