/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2007 Erwin Coumans  http://continuousphysics.com/Bullet/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.bulletphysics.render;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * @author jezek2
 */

@Deprecated
public class JOGL {
    private static final int FPS = 60; // animator's target frames per second

    private final GLWindow window;

    public JOGL(SpaceGraph3D s, int width, int height) {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilitiesImmutable caps = new GLCapabilities(glp);
        window = GLWindow.create(caps);

        // Create a animator that drives canvas' display() at the specified FPS.
        GLAnimatorControl animator = new FPSAnimator(window, FPS, true);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent arg0) {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread(() -> {
                    if (animator.isStarted())
                        animator.stop();    // stop the animator loop
                    System.exit(0);
                }).start();
            }
        });

        s.start();


        window.addMouseListener(s.mouse);
        window.addKeyListener(s.keyboard);

        window.setSize(width, height);
        window.setVisible(true);

        s.gl = new JoglGL();
        window.addGLEventListener(s);
//        ((JoglGL)s.gl).init(window.getGL().getGL2());


        s.world.setDebugDrawer(new GLDebugDrawer(
                s.gl
        ));


        animator.start();  // start the animator loop

    }

    public void windowResized(WindowEvent e) {
    }

    public void windowMoved(WindowEvent e) {
    }

}