package com.bulletphysics.input;

/**
 * Represents an input of an input device.
 * @author:Mahesh kurmi
 */
public class InputKey {
		
	/**
	 * This enumeration represents the state of an input.
	 */
	public enum State {
		/** the key has been released */
		RELEASED,
		/** the key has been pressed */
		PRESSED
	}


	/** The input state */
	private State state = State.RELEASED;

	/** The keyCode id */
	private short keyCode;

	/** The input count  (the number of times key event  is generated) */
	private short value = 0;

	/**key char associated with key if any*/
	private char keyChar;

	/**modifier associated with keyevent*/
	private int modifiers=0;

	/** The awt event id needed for gui @see java.awt.event.MouseEvent*/
	private int id=0;


	private boolean isActionKey=false;

	private boolean consumed=true;
	/**
	 * constructor.
	 * @param keyCode keyCode constant defined in {@link com.jogamp.newt.event.KeyEvent} for the key
	 * @param keyChar unicode char for the key
	 */
	public InputKey(short keyCode,char keyChar) {
		this.keyCode = keyCode;
		this.keyChar = keyChar;
	}


	/**
	 * Resets the input.
	 */
	public synchronized void reset() {
		this.state = State.RELEASED;
		consumed=true;
		this.value = 0;
		this.modifiers=0;
		this.id=0;
		this.isActionKey=false;
	}

	/**
	 * Notify that the input has been released.
	 */
	public synchronized void release(char keyChar, int id, int modifiers, boolean actionKey) {
		this.state = State.RELEASED;
		 if(getKeyCode()==9){
    		//System.out.println("//id="+getId()+", consumed="+consumed);
    	 }
		this.id=id;
		consumed=false;
		this.keyChar=keyChar;
		this.modifiers=modifiers;
		this.isActionKey=actionKey;
	}

	/**
	 * Notify that the input was pressed.
	 */
	public synchronized void press(char keyChar, int id, int modifiers, boolean actionKey) {
		if(this.state == State.RELEASED)this.value ++;
		this.id=id;
		consumed=false;
		this.keyChar=keyChar;
		this.state = State.PRESSED;
		this.modifiers=modifiers;
		this.isActionKey=actionKey;
	}


	/**
	 * Returns true if the input has been pressed since the last check.
	 * <p>
	 * Calling this method will clear the value of this input if the input
	 * has already been released or if the input is {@link Hold#NO_HOLD} and
	 * the input has not been released.
	 * @return boolean
	 */
	public synchronized boolean isPressed() {
		return  this.state == State.PRESSED;
	}

	/**
	 * Checks if key  is set to be consumed, and set it consumed (used for gui events only)
	 * @return
	 */
	public boolean isConsumed(){
		boolean f= consumed;
		consumed=true;
		return f;
	}

	/**
	 * <p>
	 * Returns the value of the input, number of times key is pressed, since {@method InputKey.reset} is called
	 * @return int
	 */
	public synchronized int getValue() {
		return this.value;
	}


	/**
	 * Returns the state.
	 * @return {@link State}
	 */
	public State getState() {
		return state;
	}
	

	/**
	 * returns unicode keychar for the event
	 * @return
	 */
	public char getKeyChar() {
		return keyChar;
	}
	
	/**
	 * Returns the keyCode.
	 * @return int
	 */
	public short getKeyCode() {
		return this.keyCode;
	}
	
	public int getModifier(){
		return modifiers;
	}
	
	/**
	 * returns AWtEvent id (keypressed, keytyped or keyreleased)
	 * @return
	 */
	public int getId(){
		return id;
	}
	
	 /**
     * Returns whether the key in this event is an "action" key.
     * Typically an action key does not fire a unicode character and is
     * not a modifier key.
     *
     * @return <code>true</code> if the key is an "action" key,
     *         <code>false</code> otherwise
     */
    public  boolean isActionKey() {
    	return isActionKey;
    }
    
  
}
