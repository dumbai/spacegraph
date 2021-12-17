package spacegraph.input.key;

import com.jogamp.newt.event.KeyEvent;
import spacegraph.layer.AbstractLayer;

/**
 * Created by me on 11/20/16.
 */
class KeyXY extends SpaceKeys {
    private static final float speed = 8.0f;

    KeyXY(AbstractLayer g) {
        super(g);


        on(KeyEvent.VK_NUMPAD4, (dt)-> moveX(speed), null);
        on(KeyEvent.VK_NUMPAD6, (dt)-> moveX(-speed), null);

        on(KeyEvent.VK_NUMPAD8, (dt)-> moveY(speed), null);
        on(KeyEvent.VK_NUMPAD2, (dt)-> moveY(-speed), null);


        on(KeyEvent.VK_NUMPAD5, (dt)-> moveZ(speed), null);
        on(KeyEvent.VK_NUMPAD0, (dt)-> moveZ(-speed), null);

    }

    void moveX(float speed) {
        space.camPos.add(speed, 0, 0);
    }

    void moveY(float speed) {
        space.camPos.add(0, speed, 0);
    }

    void moveZ(float speed) {
        space.camPos.add(0, 0, speed);
    }

}