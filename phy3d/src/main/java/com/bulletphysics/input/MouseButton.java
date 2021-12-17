package com.bulletphysics.input;


import java.awt.event.MouseEvent;

/**
 * Represents a mouse button used to hold its current state.
 * @author:Mahesh kurmi
 */
public class MouseButton {
	
	/** The awt event id @see java.awt.event.MouseEvent*/
	private int id;
	
	/** The MouseEvent code */
	private final int code;
	
	/** The current value (number of clicks) */
	private int value;
	
	/** True if the button is pressed and waiting for release */
	private boolean pressed;
	
	/** True if the button was waiting to be released and it was released */
	private boolean released;
	
	/** Shift mask of event*/
	private int modifiers;
	
	private boolean consumed=true;
	/**
	 * Full constructor.
	 * @param code the MouseEvent code
	 */
	public MouseButton(int code,int id,  int modifiers) {
		this.code = code;
		this.value = 0;
		this.pressed = false;
		this.released = false;
		this.id=id;
		this.modifiers=modifiers;
	}
	
	private int prevId=-1;
	/**
	 * updates AWT data(needed for gui)
	 * @param eventID AWT Mouse event id
	 * @param modifiers shift modifiers for mouse event 
	 */
	void updateEventData(int eventID, int modifiers){
		if(this.prevId!= MouseEvent.MOUSE_DRAGGED && eventID== MouseEvent.MOUSE_DRAGGED){
			//before 12/4/19
			//this.id=java.awt.event.MouseEvent.MOUSE_PRESSED;
			//this.prevId=java.awt.event.MouseEvent.MOUSE_DRAGGED;
			
			//after on 12/4/19 (needs testing)
			this.id= MouseEvent.MOUSE_DRAGGED;
			this.prevId= MouseEvent.MOUSE_PRESSED;
			//if(org.shikhar.simphy.Simphy.DEBUG )System.out.println("Button id changed to pressed from "+prevId);
		}else{
			this.id=eventID;
			prevId=this.id;
		}
		this.modifiers=modifiers;
		consumed=false;

	}
	
	/**
	 * Checks if key  is et to be consumed, and set it consumed (used for gui events only)
	 * @return
	 */
	public boolean isConsumed(){
		boolean f= consumed;
		consumed=true;
		return f;
	}
	
	/**
	 * Sets the value of this mouse button.
	 * <p>
	 * The value indicates the number of clicks issued.
	 * @param value the value or number of clicks
	 */
	public synchronized void setValue(int value) {
		this.value = value;
		consumed=false;
	}
	
	/**
	 * Returns true if this mouse button was clicked once.
	 * <p>
	 * Returns false if the button was double/triple/etc clicked or is currently
	 * waiting to be released.
	 * @return boolean
	 */
	public synchronized boolean wasClicked() {
		return this.value >0;
	}
	
	/**
	 * Returns true if this mouse button was double clicked.
	 * <p>
	 * Returns false if the button was single/triple/etc clicked or is currently
	 * waiting to be released.
	 * @return boolean
	 */
	public synchronized boolean wasDoubleClicked() {
		return this.value == 2;
	}
	
	/**
	 * Returns the current value of this mouse button.
	 * <p>
	 * This represents the number of clicks.
	 * @return int
	 */
	public synchronized int getValue() {
		return this.value;
	}
	
	/**
	 * Sets the pressed flag.
	 * <p>
	 * The pressed flag indicates the button is currently
	 * pressed and waiting for release.
	 * @param flag true if the button is pressed and waiting for release
	 */
	public synchronized void setPressed(boolean flag) {
		this.pressed = flag;
		//if(org.shikhar.simphy.Simphy.DEBUG )System.out.println("Mouse pressed="+flag);

	}
	
	/**
	 * Returns true if this button is currently pressed and is waiting for release.
	 * @return boolean
	 */
	public synchronized boolean isPressed() {
		return this.pressed;
	}
	
	/**
	 * Sets the was released flag.
	 * <p>
	 * The was released flag indicates the button was pressed and
	 * waiting for release and is now released.
	 * <p>
	 * This state should be cleared after use.
	 * @param flag true if the button was pressed and waiting for release and is now released
	 */
	public synchronized void setWasReleased(boolean flag) {
		this.released = flag;
	}
	
	/**
	 * Returns true if this button was pressed and waiting for release but is now released.
	 * @return boolean
	 */
	public synchronized boolean wasReleased() {
		return this.released;
	}
	
	/**
	 * Clears the state of the button.
	 */
	public synchronized void clear() {
		this.value = 0;
		this.released = false;
		this.id=0;
		this.modifiers=0;
		
	}
	
	/**
	 * Returns the MouseEvent code for this button.
	 * @return int
	 */
	public int getCode() {
		return this.code;
	}
	
	/**
	 * Returns the MouseEvent code for this button.
	 * @return int
	 */
	public int getShiftMask() {
		return this.code;
	}
	
	/**
	 * Returns the MouseEvent code for this button.
	 * @return int
	 */
	public int getAWTEventId() {
		return this.id;
	}
	
	   /**
     * Returns whether or not the Shift modifier is down on this event.
     */
    public boolean isShiftDown() {
        return (modifiers & MouseEvent.SHIFT_MASK) != 0;
    }

    /**
     * Returns whether or not the Control modifier is down on this event.
     */
    public boolean isControlDown() {
        return (modifiers & MouseEvent.CTRL_MASK) != 0;
    }

//    /** {@link #getModifiers()} contains {@link #ALT_MASK}. */
    public final boolean isAltDown() {
       return (modifiers&MouseEvent.ALT_MASK)!=0;
    }
    
//    /** Return the modifier bits of this event, e.g. see {@link #SHIFT_MASK} .. etc. */
    public final int getModifiers() {
       return modifiers;
    }
}