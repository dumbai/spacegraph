package spacegraph.input.key;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.opengl.GLWindow;
import spacegraph.input.key.util.HotKeyMap;
import spacegraph.layer.AbstractLayer;

/** standard JoglSpace keyboard window controls */
public class WindowKeyControls extends HotKeyMap {



    public WindowKeyControls(AbstractLayer window) {

        add(KeyEvent.VK_F1, "Help", ()->{
            System.out.println(window);
            System.out.println(window.window.window);
            print(System.out);
        });

        add(KeyEvent.VK_F2, "Window Decoration Toggle", ()-> window.window.window.setUndecorated(!window.window.window.isUndecorated()));

        add(KeyEvent.VK_F3, "Window Full-Screen Toggle", ()->{
            GLWindow w = window.window.window;
            boolean fullscreen = w.isFullscreen();
            w.setAlwaysOnBottom(!fullscreen);
            w.setFullscreen(!fullscreen);
        });

        add(KeyEvent.VK_F4, "Window Always-On-Top Toggle", ()-> {
            GLWindow w = window.window.window;
            w.setAlwaysOnTop(w.isAlwaysOnTop());
        });

        final float MAX_FPS = 100;

        add(KeyEvent.VK_F4, "Window FPS Faster", ()-> {
            float nextFPS = Math.min(MAX_FPS, window.window.renderFPS * 1.25f);
            window.window.setFPS(nextFPS);
        });
        final float MIN_FPS = 1;
        add(KeyEvent.VK_F5, "Window FPS Slower", ()-> {
            float nextFPS = Math.max(MIN_FPS, window.window.renderFPS * 0.8f);
            window.window.setFPS(nextFPS);
        });

    }

}
