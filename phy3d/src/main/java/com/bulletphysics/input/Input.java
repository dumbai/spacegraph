package com.bulletphysics.input;

/**
 * Represents an input of an input device.
 */
public class Input {
	/**
	 * This enumeration represents the action taken when the input is held hown.
	 */
	public enum Hold {
		/** The isPressed method will return true as long as the input is held down */
		HOLD,
		/** The isPressed method will return true only when the input has been released and pressed again. */
		NO_HOLD
	}
	
	/**
	 * This enumeration represents the state of an input.
	 */
	public enum State {
		/** the key has been released */
		RELEASED,
		/** the key has been pressed */
		PRESSED,
		/** the key is currenlty pressed */
		WAITING_FOR_RELEASE
	}

	/** What happens when the input is held */
	private Hold holdType;

	/** The input state */
	private State state = State.RELEASED;

	/** The event id */
	private int event;

	/** The input count */
	private int value = 0;

	private char keyChar=0;

	/**
	 * Minimal constructor.
	 * @param event the event id
	 */
	public Input(int event) {
		this(event, Hold.HOLD);
	}

	/**
	 * Full constructor.
	 * @param event the event id
	 * @param holdType the hold type
	 */
	private Input(int event, Hold holdType) {
		super();
		this.event = event;
		this.holdType = holdType;
	}

	/**
	 * Resets the input.
	 */
	public synchronized void reset() {
		this.state = State.RELEASED;
		this.value = 0;
		//if(org.shikhar.simphy.Simphy.DEBUG )System.out.println("key reset");
	}

	/**
	 * Notify that the input has been released.
	 */
	public synchronized void release() {
		this.state = State.RELEASED;
	}

	/**
	 * Notify that the input was pressed.
	 * @param value the number of times pressed
	 */
	private synchronized void press(int value) {
		if (this.state != State.WAITING_FOR_RELEASE) {
			this.value = this.value + value;
			if (this.holdType == Hold.HOLD) {
				this.state = State.WAITING_FOR_RELEASE;
			} else {
				this.state = State.PRESSED;
			}
		}
	}

	/**
	 * Represents an input being signaled once.
	 */
	public synchronized void press() {
		this.press(1);
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
		return this.getValue() > 0;
	}

	/**
	 * Returns the value of the input.
	 * <p>
	 * Calling this method will clear the value of this input if the input
	 * has already been released or if the input is {@link Hold#NO_HOLD} and
	 * the input has not been released.
	 * @return int
	 */
	private synchronized int getValue() {
		int value = this.value;
		// if the value is greater than 0 then this input has been used
		// since the last check
		if (value > 0) {
			if (this.state == State.RELEASED) {
				// if the input state is released then set the value to zero
				// but still return that the input was pressed since the last check
				this.value = 0;
			} else if (this.holdType == Hold.NO_HOLD) {
				// if the input hold type is no hold then set the state to waiting for release
				// and set the value to zero
				this.state = State.WAITING_FOR_RELEASE;
				this.value = 0;
			}
		}
		return value;
	}

	/**
	 * Returns the event id.
	 * @return int
	 */
	public int getEvent() {
		return this.event;
	}

	/**
	 * Returns the hold type.
	 * @return {@link Hold}
	 */
	public Hold getHoldType() {
		return holdType;
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
	 * Called by keyevent to update keyChar
	 * @param c
	 */
	protected void setKeyChar(char c) {
		this.keyChar=c;
	}
}
