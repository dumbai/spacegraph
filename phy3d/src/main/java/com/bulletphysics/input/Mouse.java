package com.bulletphysics.input;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseEvent.PointerType;
import com.jogamp.newt.event.MouseListener;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represents a polled Mouse input device.
 *
 * @author:Mahesh kurmi
 */
public class Mouse implements MouseListener, MouseWheelListener {
    /**
     * The map of mouse buttons
     */
    private final Map<Integer, MouseButton> buttons = new Hashtable<>();

    /**
     * The current mouse location
     */
    private Point location = new Point(0, 0);

    /**
     * Whether the mouse has moved
     */
    private boolean moved;

    /**
     * The scroll amount
     */
    private int scroll;
    private double zoom = 1;
    private double angle = 0;


    public Map<Integer, MouseButton> getActiveButtons() {
        return this.buttons;
    }


    /**
     * Returns true if the given MouseEvent code was clicked.
     *
     * @param code the MouseEvent code
     * @return boolean
     */
    public boolean wasClicked(int code) {
        MouseButton mb = this.buttons.get(code);
        // check if the mouse button exists
        if (mb == null) {
            return false;
        }
        // return the clicked state
        return mb.wasClicked();
    }


    /**
     * Returns true if the given MouseEvent code was double clicked.
     *
     * @param code the MouseEvent code
     * @return boolean
     */
    public boolean wasDoubleClicked(int code) {
        MouseButton mb = this.buttons.get(code);
        // check if the mouse button exists
        if (mb == null) {
            return false;
        }
        // return the double clicked state
        return mb.wasDoubleClicked();
    }


    /**
     * Returns true if the given MouseEvent code was clicked.
     *
     * @param code the MouseEvent code
     * @return boolean
     */
    public boolean isClicked() {
        for (MouseButton mb : this.buttons.values()) {
            if (mb != null && mb.wasClicked()) return true;
        }
        return false;
    }

    /**
     * Returns true if any of button is currently pressed and is waiting to be released.
     */
    public boolean isPressed() {
        for (MouseButton mb : this.buttons.values()) {
            if (mb != null && mb.isPressed()) return true;
        }
        return false;
    }

    /**
     * Returns true if the given MouseEvent code was clicked and is waiting to be released.
     *
     * @param code the MouseEvent code
     * @return boolean
     */
    public boolean isPressed(int code) {
        MouseButton mb = this.buttons.get(code);
        // check if the mouse button exists
        if (mb == null) {
            return false;
        }
        // return the double clicked state
        return mb.isPressed();
    }

    /**
     * Returns true if any of button is  released.
     */
    public boolean wasReleased() {
        for (MouseButton mb : this.buttons.values()) {
            if (mb != null && mb.wasReleased()) return true;
        }
        return false;
    }

    /**
     * Returns true if the given MouseEvent code was clicked and was waiting to be released
     * but is now released.
     *
     * @param code the MouseEvent code
     * @return boolean
     */
    public boolean wasReleased(int code) {
        MouseButton mb = this.buttons.get(code);
        // check if the mouse button exists
        if (mb == null) {
            return false;
        }
        // return the double clicked state
        return mb.wasReleased();
    }

    /**
     * Returns the current location of the mouse relative to
     * the listening component.
     *
     * @return Point
     */
    public Point getLocation() {
        return this.location;
    }

    /**
     * Returns true if the mouse has moved.
     *
     * @return boolean
     */
    public boolean hasMoved() {
        return this.moved;
    }

    /**
     * Clears the state of the given MouseEvent code.
     *
     * @param code the MouseEvent code
     */
    public void clear(int code) {
        MouseButton mb = this.buttons.get(code);
        // check if the mouse button exists
        if (mb == null) {
            return;
        }
        // clear the state
        mb.clear();
    }

    /**
     * Clears the state of all MouseEvents.
     */
    public void clear() {
        for (MouseButton mouseButton : this.buttons.values()) {
            mouseButton.clear();
        }

        this.moved = false;
        this.scroll = 0;
        this.zoom = 1;
        this.angle = 0;

    }

    /**
     * Returns true if the user has scrolled the mouse wheel.
     *
     * @return boolean
     */
    public boolean hasScrolled() {
        return this.scroll != 0;
    }


    /**
     * Returns the number of 'clicks' the mouse wheel has scrolled.
     *
     * @return int
     */
    public int getScrollAmount() {
        return this.scroll;
    }

    /**
     * Returns the zoom due to pinchScreen gesture
     */
    public double getZoom() {
        return this.zoom;
    }

    /**
     * return angle due to due to pinchScreen gesture
     */
    public double getAngle() {
        return this.angle;
    }


    /*s
     * (non-Javadoc)
     * @see com.jogamp.newt.event.MouseListener#mouseClicked(com.jogamp.newt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        //if(org.shikhar.simphy.Simphy.DEBUG )System.out.println("Mouse clicked :"+ "type="+e.getPointerType(0)+"ptr_id "+e.getPointerId(0)+" activeid="+activePointerId);
        this.location = new Point(e.getX(), e.getY());

        int code = e.getButton();
        if (e.getPointerType(0) != PointerType.Mouse) {
            code = MouseEvent.BUTTON1;
        }
        MouseButton mb = this.buttons.get(code);
        // check if the mouse event is in the map
        if (mb == null) {
            // if not, then add it
            mb = new MouseButton(code, java.awt.event.MouseEvent.MOUSE_CLICKED, e.getModifiers());
            this.buttons.put(code, mb);
        }

        // set the value directly (since this can be a single/double/triple etc click)
        mb.setValue(e.getClickCount());
        //mb.updateEventData(java.awt.event.MouseEvent.MOUSE_CLICKED,e.getModifiers());

    }

    /*
     * (non-Javadoc)
     * @see com.jogamp.newt.event.MouseListener#mousePressed(com.jogamp.newt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // called when a mouse button is pressed and is waiting for release
        //if(org.shikhar.simphy.Simphy.DEBUG )System.out.println("Mouse pressed :"+ "type="+e.getPointerType(0)+"ptr_id "+e.getPointerId(0)+" activeid="+activePointerId);
        this.location = new Point(e.getX(), e.getY());

        //	if(org.shikhar.simphy.Simphy.DEBUG )System.out.println(e.toString());//
        // set the mouse state to pressed + held for the button
        int code = e.getButton();
        MouseButton mb = this.buttons.get(code);
        // check if the mouse event is in the map
        if (mb == null) {
            // if not, then add it
            mb = new MouseButton(code, java.awt.event.MouseEvent.MOUSE_PRESSED, e.getModifiers());
            this.buttons.put(code, mb);
        }
        mb.setPressed(true);
        mb.updateEventData(java.awt.event.MouseEvent.MOUSE_PRESSED, e.getModifiers());
        //mb.setValue(e.getClickCount());
    }

    /*
     * (non-Javadoc)
     * @see com.jogamp.newt.event.MouseListener#mouseReleased(com.jogamp.newt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        // called when a mouse button is waiting for release and was released
        //if(org.shikhar.simphy.Simphy.DEBUG )System.out.println("Mouse released :"+ "type="+e.getPointerType(0)+"ptr_id "+e.getPointerId(0)+" activeid="+activePointerId);
        this.location = new Point(e.getX(), e.getY());

        // set the mouse state to released for the button

        int code = e.getButton();

        // set the mouse state to released for the button
        MouseButton mb = this.buttons.get(code);
        // check if the mouse event is in the map
        if (mb == null) {
            // if not, then add it
            mb = new MouseButton(code, java.awt.event.MouseEvent.MOUSE_RELEASED, e.getModifiers());
            this.buttons.put(code, mb);
        }
        mb.setPressed(false);
        mb.setWasReleased(true);
        mb.updateEventData(java.awt.event.MouseEvent.MOUSE_RELEASED, e.getModifiers());
        //mb.setValue(e.getClickCount());
    }


    /*
     * (non-Javadoc)
     * @see com.jogamp.newt.event.MouseListener#mouseDragged(com.jogamp.newt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        // called when a mouse button is waiting for release and the mouse is moving
        //if(org.shikhar.simphy.Simphy.DEBUG )System.out.println("Mouse dragged :"+ "type="+e.getPointerType(0)+"ptr_id "+e.getPointerId(0)+" activeid="+activePointerId);
        //this.location = new Point(e.getX(), e.getY());

        int code = e.getButton();
        // set the mouse location
        this.moved = true;
        this.location = new Point(e.getX(), e.getY());
        // set the mouse button pressed flag

        MouseButton mb = this.buttons.get(code);
        if (mb == null) {
            // check if the mouse event is in the map
            // if not, then add it
            mb = new MouseButton(code, java.awt.event.MouseEvent.MOUSE_DRAGGED, e.getModifiers());
            this.buttons.put(code, mb);
        }

        mb.setPressed(true);
        mb.updateEventData(java.awt.event.MouseEvent.MOUSE_DRAGGED, e.getModifiers());
        //mb.setValue(e.getClickCount());
    }

    /*
     * (non-Javadoc)
     * @see com.jogamp.newt.event.MouseListener#mouseMoved(com.jogamp.newt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        this.moved = true;
        this.location = new Point(e.getX(), e.getY());

        if (!e.isButtonDown(MouseEvent.BUTTON1) && this.buttons.get(MouseEvent.BUTTON1) != null) {
            if (this.buttons.get(MouseEvent.BUTTON1).isPressed()) {
                this.buttons.get(MouseEvent.BUTTON1).setPressed(false);
                this.buttons.get(MouseEvent.BUTTON1).setWasReleased(true);
            }
        }
        int code = e.getButton();
        // check if the mouse event is in the map
        MouseButton mb = this.buttons.get(code);
        if (mb == null) {
            // if not, then add it
            mb = new MouseButton(code, java.awt.event.MouseEvent.MOUSE_MOVED, e.getModifiers());
            this.buttons.put(code, mb);
        }

        mb.updateEventData(java.awt.event.MouseEvent.MOUSE_MOVED, e.getModifiers());

    }

    /*
     * (non-Javadoc)
     * @see com.jogamp.newt.event.MouseListener#mouseWheelMoved(com.jogamp.newt.event.MouseEvent)
     */
    @Override
    public void mouseWheelMoved(MouseEvent e) {
        this.scroll += e.getRotation()[1];
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.scroll += e.getWheelRotation();
    }

    // not used

    /* (non-Javadoc)
     * @see com.jogamp.newt.event.MouseListener#mouseEntered(com.jogamp.newt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        int code = e.getButton();
        // check if the mouse event is in the map
        MouseButton mb = this.buttons.get(code);
        if (mb != null) {
            mb.updateEventData(java.awt.event.MouseEvent.MOUSE_ENTERED, e.getModifiers());
        }
    }

    /*
     * (non-Javadoc)
     * @see com.jogamp.newt.event.MouseListener#mouseExited(com.jogamp.newt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
        int code = e.getButton();
        // check if the mouse event is in the map
        MouseButton mb = this.buttons.get(code);
        if (mb != null) {
            mb.updateEventData(java.awt.event.MouseEvent.MOUSE_EXITED, e.getModifiers());
        }
    }

	/*
	public void onZoomAndScale(Vector2 center,double scale, double angle) {
		// TODO Auto-generated method stub
		//if(org.shikhar.simphy.Simphy.DEBUG )System.out.println(zoomGesture.toString());
		this.zoom=scale;
		this.angle=angle;
		this.center=center;
		
		//gesturePoints=zoomGesture.getGesturePoints();
	}
*/

}