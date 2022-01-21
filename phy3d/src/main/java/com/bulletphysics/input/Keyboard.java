package com.bulletphysics.input;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a keyboard input device.
 * 
 * @author:Mahesh kurmi
 */
public class Keyboard implements KeyListener {
	/** The keys */
	private final Map<Integer, InputKey> keys = new HashMap<>();

	private boolean consumed=true;
	
	public Map<Integer, InputKey> getActiveKeys() {
		return this.keys;
	}

	public Keyboard() {
		for (int i = 0; i < 256; i++) {
			this.keys.put(i, new InputKey((short) i, (char) i));
		}
	}

	/**
	 * Adds the given key.
	 * @param key the key to listen for
	 */
	public void add(InputKey key) {
		synchronized (this.keys) {
			this.keys.put((int) key.getKeyCode(), key);
		}
	}
	
	/**
	 * Removes the input with the given event code.
	 * @param code the event code
	 * @return {@link Input}
	 */
	public InputKey remove(int code) {
		synchronized (this.keys) {
			return this.keys.remove(code);
		}
	}
	
	/**
	 * Returns true if the given input has been signaled.
	 * <p>
	 * Calling this method will clear the value of the input if the input has
	 * already been released or if the input is {@link Hold#NO_HOLD} and the
	 * input has not been released.
	 * 
	 * @param code
	 *            the event code
	 * @return boolean
	 */
	public boolean isPressed(int keyCode) {
		synchronized (this.keys) {
			if (this.keys.containsKey(keyCode)) {
				return this.keys.get(keyCode).isPressed();
			}
		}
		return false;
	}

	/**
	 * Returns the the number of times key is called since last reset() of the
	 * given input.
	 * 
	 * @param code
	 *            the event code
	 * @return int
	 */
	public int getValue(int keyCode) {
		synchronized (this.keys) {
			if (this.keys.containsKey(keyCode)) {
				return this.keys.get(keyCode).getValue();
			}
		}
		return 0;
	}

	/**
	 * Resets the given input, and assumed as consumed
	 * 
	 * @param code
	 *            the event code
	 */
	public void clear(int keyCode) {
		synchronized (this.keys) {
			this.keys.remove(keyCode);
		}
	}
	
//	/**
//	 * Resets all the keys for which holdis false.
//	 */
//	public void resetUnholdKeys() {
////		synchronized (this.keys) {
////			//for(InputKey key:this.keys.values())if(key.getHoldType()==Hold.NO_HOLD)key.reset();
////		}
//	}
	

	/**
	 * Resets all the keys, assumes are are consumed
	 */
	public void reset() {
		synchronized (this.keys) {
			for (InputKey key : this.keys.values())
				key.reset();
		}
		consumed=true;
	}

	/*
	 * Resets the given input, and assumed as consumed
	 * 
	 * @param code the event code
	 */
	public void reset(int keyCode) {
		consumed=true;
		synchronized (this.keys) {
			if (this.keys.containsKey(keyCode)) {
				this.keys.get(keyCode).reset();
			}
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();

		// do not listen auto repeat keys
		//if (0 == (InputEvent.AUTOREPEAT_MASK & e.getModifiers())) {
		// if( !e.isPrintableKey() || e.isAutoRepeat() ) {
			if (this.keys.containsKey(keyCode)) {
				this.keys.get(keyCode).press(e.getKeyChar(),java.awt.event.KeyEvent.KEY_PRESSED,e.getModifiers(),e.isActionKey());
		         //if(org.shikhar.simphy.Simphy.DEBUG )System.out.println("key df="+e.getKeyChar()+", code="+e.getKeyCode()+", pressed=");//+input.isPressed()+", handled="+handled);
			}
		// }
		e.setConsumed(true);	
		consumed=false;
		//}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		
		// do not listen auto repeat keys
		if (0 == (InputEvent.AUTOREPEAT_MASK & e.getModifiers())) {
			int keyCode = e.getKeyCode();
			
			if (this.keys.containsKey(keyCode)) {
				this.keys.get(keyCode).release(e.getKeyChar(),java.awt.event.KeyEvent.KEY_RELEASED,e.getModifiers(),e.isActionKey());
			}
			e.setConsumed(true);
		}
		
		 if( !e.isPrintableKey() || e.isAutoRepeat() ) {
	          //  return;
	     }else{
	    	// this.keys.get(keyCode).release(e.getKeyChar(),java.awt.event.KeyEvent.KEY_RELEASED,e.getModifiers(),e.isActionKey());
	    	 //this.keys.get(keyCode).press(e.getKeyChar(),java.awt.event.KeyEvent.KEY_TYPED,e.getModifiers(),e.isActionKey());
		     
	     }
		 
		 //this.keys.get(keyCode).release(e.getKeyChar(),java.awt.event.KeyEvent.KEY_RELEASED,e.getModifiers(),e.isActionKey());
		 //e.setConsumed(true);
		 consumed=false;
	}

	public boolean isActive() {
		return !consumed;
	}

	/*
	public void keyTyped(KeyEvent e) {
		To simulated the removed keyTyped(KeyEvent e) semantics, simply apply the following constraints 
		upfront and bail out if not matched, i.e.:

        if( !e.isPrintableKey() || e.isAutoRepeat() ) {
            return;
        }
 
	}
	*/


}