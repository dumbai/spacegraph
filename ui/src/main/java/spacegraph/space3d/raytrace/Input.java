package spacegraph.space3d.raytrace;

import jcog.math.v3d;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

final class Input extends ComponentAdapter implements KeyListener, MouseListener, MouseMotionListener {
    public Dimension newSize;

    private boolean forward = false;
    private boolean backward = false;
    private boolean left = false;
    private boolean right = false;
    private boolean shift = false;
    private int currentMouseX;
    private int currentMouseY;
    private int lastMouseX;
    private int lastMouseY;
    private final List<Runnable> onInputInterruptables = new ArrayList<>();

    public void waitForInput() {
        if (!moving()) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
            }
        }
    }

    public void addInterruptable(Runnable i) {
        onInputInterruptables.add(i);
    }

    private void interrupt() {
        for (Runnable i : onInputInterruptables) {
            i.run();
        }
    }

    public int getDeltaMouseX() {
        int dx = currentMouseX - lastMouseX;
        lastMouseX = currentMouseX;
        return dx;
    }

    public int getDeltaMouseY() {
        int dy = currentMouseY - lastMouseY;
        lastMouseY = currentMouseY;
        return dy;
    }

    public v3d getKeyboardVector() {
        v3d kbVector = new v3d(0, 0, 0);

        if (forward) {
            kbVector = kbVector.add(new v3d(0, 1, 0));
        }
        if (backward) {
            kbVector = kbVector.add(new v3d(0, -1, 0));
        }
        if (left) {
            kbVector = kbVector.add(new v3d(-1, 0, 0));
        }
        if (right) {
            kbVector = kbVector.add(new v3d(1, 0, 0));
        }

        kbVector = kbVector.normalize().scale(0.3);

        if (shift) {
            kbVector = kbVector.scale(0.1);
        }

        return kbVector;
    }

    public boolean moving() {
        return forward || backward || left || right;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        newSize = e.getComponent().getSize();
        interrupt();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_W -> forward = true;
            case KeyEvent.VK_S -> backward = true;
            case KeyEvent.VK_SHIFT -> shift = true;
        }
        if (moving()) {
            interrupt();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        boolean oldMoving = moving();
        switch (keyCode) {
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
            case KeyEvent.VK_W -> forward = false;
            case KeyEvent.VK_S -> backward = false;
            case KeyEvent.VK_SHIFT -> shift = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        currentMouseX = e.getX();
        currentMouseY = e.getY();
        interrupt();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
