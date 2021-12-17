/* Input.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Handles input events and sends relevant input data to server
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * (See gpl.txt for details of the GNU General Public License.)
 *
 */

package net.propero.rdp;

import net.propero.rdp.keymapping.KeyCode;
import net.propero.rdp.keymapping.KeyCode_FileBased;
import net.propero.rdp.keymapping.KeyMapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.*;
import java.util.Vector;

public abstract class Input {

    static final Logger logger = LoggerFactory.getLogger(Input.class);
    
    
    
    protected static final int KBD_FLAG_RIGHT = 0x0001;
    private static final int KBD_FLAG_EXT = 0x0100;
    
    private static final int KBD_FLAG_QUIET = 0x200;
    private static final int KBD_FLAG_DOWN = 0x4000;
    private static final int KBD_FLAG_UP = 0x8000;
    static final int RDP_KEYPRESS = 0;
    static final int RDP_KEYRELEASE = KBD_FLAG_DOWN | KBD_FLAG_UP;
    private static final int MOUSE_FLAG_MOVE = 0x0800;
    private static final int MOUSE_FLAG_BUTTON1 = 0x1000;
    private static final int MOUSE_FLAG_BUTTON2 = 0x2000;
    private static final int MOUSE_FLAG_BUTTON3 = 0x4000;
    static final int MOUSE_FLAG_BUTTON4 = 0x0280;
    
    static final int MOUSE_FLAG_BUTTON5 = 0x0380;
    
    static final int MOUSE_FLAG_DOWN = 0x8000;
    protected static final int RDP_INPUT_SYNCHRONIZE = 0;
    protected static final int RDP_INPUT_CODEPOINT = 1;
    protected static final int RDP_INPUT_VIRTKEY = 2;
    private static final int RDP_INPUT_SCANCODE = 4;
    static final int RDP_INPUT_MOUSE = 0x8001;
    static boolean capsLockOn;
    static boolean numLockOn;
    static boolean scrollLockOn;
    private static boolean serverAltDown;
    static boolean altDown;
    static boolean ctrlDown;
    protected static long last_mousemove;
    private static int time;
    private final Vector pressedKeys;
    final RdesktopCanvas canvas;
    final Rdp rdp;
    KeyEvent lastKeyEvent;
    private boolean modifiersValid;
    public boolean keyDownWindows;
    private KeyCode_FileBased newKeyMapper;
    KeyCode keys;

    /**
     * Create a new Input object with a given keymap object
     *
     * @param c Canvas on which to listen for input events
     * @param r Rdp layer on which to send input messages
     * @param k Key map to use in handling keyboard events
     */
    Input(RdesktopCanvas c, Rdp r, KeyCode_FileBased k) {
        newKeyMapper = k;
        canvas = c;
        rdp = r;


        addInputListeners();
        pressedKeys = new Vector();
    }

    /**
     * Create a new Input object, using a keymap generated from a specified file
     *
     * @param c          Canvas on which to listen for input events
     * @param r          Rdp layer on which to send input messages
     * @param keymapFile Path to file containing keymap data
     */
    Input(RdesktopCanvas c, Rdp r, String keymapFile) {
        try {
            newKeyMapper = new KeyCode_FileBased_Localised(keymapFile);
        } catch (KeyMapException kmEx) {
            System.err.println(kmEx.getMessage());
            if (!Common.underApplet)
                System.exit(-1);
        }

        canvas = c;
        rdp = r;


        addInputListeners();
        pressedKeys = new Vector();
    }

    /**
     * Retrieve the next "timestamp", by incrementing previous stamp (up to the
     * maximum value of an integer, at which the timestamp is reverted to 1)
     *
     * @return New timestamp value
     */
    public static int getTime() {
        time++;
        if (time == Integer.MAX_VALUE)
            time = 1;
        return time;
    }

    /**
     * Add all relevant input listeners to the canvas
     */
    void addInputListeners() {
        canvas.addMouseListener(new RdesktopMouseAdapter());
        canvas.addMouseMotionListener(new RdesktopMouseMotionAdapter());
        canvas.addKeyListener(new RdesktopKeyAdapter());
    }

    /**
     * Send a sequence of key actions to the server
     *
     * @param pressSequence String representing a sequence of key actions. Actions are
     *                      represented as a pair of consecutive characters, the first
     *                      character's value (cast to integer) being the scancode to
     *                      send, the second (cast to integer) of the pair representing
     *                      the action (0 == UP, 1 == DOWN, 2 == QUIET UP, 3 == QUIET
     *                      DOWN).
     */
    private void sendKeyPresses(String pressSequence) {
        try {
            String debugString = "Sending keypresses: ";
            for (int i = 0; i < pressSequence.length(); i += 2) {
                int scancode = pressSequence.charAt(i);
                int action = pressSequence.charAt(i + 1);
                int flags = switch (action) {
                    case KeyCode_FileBased.UP -> RDP_KEYRELEASE;
                    case KeyCode_FileBased.DOWN -> RDP_KEYPRESS;
                    case KeyCode_FileBased.QUIETUP -> RDP_KEYRELEASE | KBD_FLAG_QUIET;
                    case KeyCode_FileBased.QUIETDOWN -> RDP_KEYPRESS | KBD_FLAG_QUIET;
                    default -> 0;
                };

                long t = getTime();

                debugString += "(0x"
                        + Integer.toHexString(scancode)
                        + ", "
                        + ((action == KeyCode_FileBased.UP || action == KeyCode_FileBased.QUIETUP) ? "up"
                        : "down")
                        + ((flags & KBD_FLAG_QUIET) != 0 ? " quiet" : "")
                        + " at " + t + ')';

                sendScancode(t, flags, scancode);
            }

            if (!pressSequence.isEmpty())
                logger.debug(debugString);
        } catch (Exception ex) {
            return;
        }
    }

    /**
     * Handle loss of focus to the main canvas. Clears all depressed keys
     * (sending release messages to the server.
     */
    public void lostFocus() {
        clearKeys();
        modifiersValid = false;
    }

    /**
     * Handle the main canvas gaining focus. Check locking key states.
     */
    public void gainedFocus() {
        doLockKeys(); 
    }

    /**
     * Send a keyboard event to the server
     *
     * @param time     Time stamp to identify this event
     * @param flags    Flags defining the nature of the event (eg:
     *                 press/release/quiet/extended)
     * @param scancode Scancode value identifying the key in question
     */
    void sendScancode(long time, int flags, int scancode) {

        if (scancode == 0x38) { 
            if ((flags & RDP_KEYRELEASE) != 0) {
                
                serverAltDown = false;
            }
            if ((flags == RDP_KEYPRESS)) {
                
                serverAltDown = true;
            }
        }

        if ((scancode & KeyCode.SCANCODE_EXTENDED) != 0) {
            rdp.sendInput((int) time, RDP_INPUT_SCANCODE, flags | KBD_FLAG_EXT,
                    scancode & ~KeyCode.SCANCODE_EXTENDED, 0);
        } else
            rdp.sendInput((int) time, RDP_INPUT_SCANCODE, flags, scancode, 0);
    }

    /**
     * Release any modifier keys that may be depressed.
     */
    void clearKeys() {
        if (!modifiersValid)
            return;

        altDown = false;
        ctrlDown = false;

        if (lastKeyEvent == null)
            return;

        if (lastKeyEvent.isShiftDown())
            sendScancode(getTime(), RDP_KEYRELEASE, 0x2a); 
        if (lastKeyEvent.isAltDown() || serverAltDown) {
            sendScancode(getTime(), RDP_KEYRELEASE, 0x38); 
            sendScancode(getTime(), RDP_KEYPRESS | KBD_FLAG_QUIET, 0x38); 
            sendScancode(getTime(), RDP_KEYRELEASE | KBD_FLAG_QUIET, 0x38); 
        }
        if (lastKeyEvent.isControlDown()) {
            sendScancode(getTime(), RDP_KEYRELEASE, 0x1d); 
            
            
            
            
        }

    }

    /**
     * Send keypress events for any modifier keys that are currently down
     */
    void setKeys() {
        if (!modifiersValid)
            return;

        if (lastKeyEvent == null)
            return;

        if (lastKeyEvent.isShiftDown())
            sendScancode(getTime(), RDP_KEYPRESS, 0x2a); 
        if (lastKeyEvent.isAltDown())
            sendScancode(getTime(), RDP_KEYPRESS, 0x38); 
        if (lastKeyEvent.isControlDown())
            sendScancode(getTime(), RDP_KEYPRESS, 0x1d); 
    }

    /**
     * Act on any keyboard shortcuts that a specified KeyEvent may describe
     *
     * @param time    Time stamp for event to send to server
     * @param e       Keyboard event to be checked for shortcut keys
     * @param pressed True if key was pressed, false if released
     * @return True if a shortcut key combination was detected and acted upon,
     * false otherwise
     */
    boolean handleShortcutKeys(long time, KeyEvent e, boolean pressed) {
        if (!e.isAltDown())
            return false;

        if (!altDown)
            return false; 

        switch (e.getKeyCode()) {

            /*
             * case KeyEvent.VK_M: if(pressed) ((RdesktopFrame_Localised)
             * canvas.getParent()).toggleMenu(); break;
             */

            case KeyEvent.VK_ENTER:
                sendScancode(time, RDP_KEYRELEASE, 0x38);
                altDown = false;
                ((RdesktopFrame) canvas.getParent()).toggleFullScreen();
                break;

            /*
             * The below case block handles "real" ALT+TAB events. Once the TAB in
             * an ALT+TAB combination has been pressed, the TAB is sent to the
             * server with the quiet flag on, as is the subsequent ALT-up.
             *
             * This ensures that the initial ALT press is "undone" by the server.
             *
             * --- Tom Elliott, 7/04/05
             */

            case KeyEvent.VK_TAB: 

                sendScancode(time, (pressed ? RDP_KEYPRESS : RDP_KEYRELEASE)
                        | KBD_FLAG_QUIET, 0x0f);
                if (!pressed) {
                    sendScancode(time, RDP_KEYRELEASE | KBD_FLAG_QUIET, 0x38); 
                    
                }

                if (pressed)
                    logger.debug("Alt + Tab pressed, ignoring, releasing tab");
                break;
            case KeyEvent.VK_PAGE_UP: 
                sendScancode(time, pressed ? RDP_KEYPRESS : RDP_KEYRELEASE, 0x0f); 
                if (pressed)
                    logger.debug("shortcut pressed: sent ALT+TAB");
                break;
            case KeyEvent.VK_PAGE_DOWN: 
                if (pressed) {
                    sendScancode(time, RDP_KEYPRESS, 0x2a); 
                    sendScancode(time, RDP_KEYPRESS, 0x0f); 
                    logger.debug("shortcut pressed: sent ALT+SHIFT+TAB");
                } else {
                    sendScancode(time, RDP_KEYRELEASE, 0x0f); 
                    sendScancode(time, RDP_KEYRELEASE, 0x2a); 
                }

                break;
            case KeyEvent.VK_INSERT: 
                sendScancode(time, pressed ? RDP_KEYPRESS : RDP_KEYRELEASE, 0x01); 
                if (pressed)
                    logger.debug("shortcut pressed: sent ALT+ESC");
                break;
            case KeyEvent.VK_HOME: 
                if (pressed) {
                    sendScancode(time, RDP_KEYRELEASE, 0x38); 
                    sendScancode(time, RDP_KEYPRESS, 0x1d); 
                    sendScancode(time, RDP_KEYPRESS, 0x01); 
                    logger.debug("shortcut pressed: sent CTRL+ESC (Start)");

                } else {
                    sendScancode(time, RDP_KEYRELEASE, 0x01); 
                    sendScancode(time, RDP_KEYRELEASE, 0x1d); 
                    
                }

                break;
            case KeyEvent.VK_END: 
                if (ctrlDown) {
                    sendScancode(time, pressed ? RDP_KEYPRESS : RDP_KEYRELEASE,
                            0x53 | KeyCode.SCANCODE_EXTENDED); 
                    if (pressed)
                        logger.debug("shortcut pressed: sent CTRL+ALT+DEL");
                }
                break;
            case KeyEvent.VK_DELETE: 
                if (pressed) {
                    sendScancode(time, RDP_KEYRELEASE, 0x38); 
                    
                    
                    sendScancode(time, RDP_KEYPRESS, 0x38); 
                    sendScancode(time, RDP_KEYRELEASE, 0x38); 
                    sendScancode(time, RDP_KEYPRESS,
                            0x5d | KeyCode.SCANCODE_EXTENDED); 
                    logger.debug("shortcut pressed: sent MENU");
                } else {
                    sendScancode(time, RDP_KEYRELEASE,
                            0x5d | KeyCode.SCANCODE_EXTENDED); 
                    
                }
                break;
            case KeyEvent.VK_SUBTRACT: 
                
                if (ctrlDown) {
                    if (pressed) {
                        sendScancode(time, RDP_KEYRELEASE, 0x1d); 
                        sendScancode(time, RDP_KEYPRESS,
                                0x37 | KeyCode.SCANCODE_EXTENDED); 
                        logger.debug("shortcut pressed: sent ALT+PRTSC");
                    } else {
                        sendScancode(time, RDP_KEYRELEASE,
                                0x37 | KeyCode.SCANCODE_EXTENDED); 
                        sendScancode(time, RDP_KEYPRESS, 0x1d); 
                    }
                }
                break;
            case KeyEvent.VK_ADD: 
            case KeyEvent.VK_EQUALS: 
                if (ctrlDown) {
                    if (pressed) {
                        sendScancode(time, RDP_KEYRELEASE, 0x38); 
                        sendScancode(time, RDP_KEYRELEASE, 0x1d); 
                        sendScancode(time, RDP_KEYPRESS,
                                0x37 | KeyCode.SCANCODE_EXTENDED); 
                        logger.debug("shortcut pressed: sent PRTSC");
                    } else {
                        sendScancode(time, RDP_KEYRELEASE,
                                0x37 | KeyCode.SCANCODE_EXTENDED); 
                        sendScancode(time, RDP_KEYPRESS, 0x1d); 
                        sendScancode(time, RDP_KEYPRESS, 0x38); 
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Deal with modifier keys as control, alt or caps lock
     *
     * @param time    Time stamp for key event
     * @param e       Key event to check for special keys
     * @param pressed True if key was pressed, false if released
     * @return
     */
    private boolean handleSpecialKeys(long time, KeyEvent e, boolean pressed) {
        if (handleShortcutKeys(time, e, pressed))
            return true;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
                ctrlDown = pressed;
                return false;
            case KeyEvent.VK_ALT:
                altDown = pressed;
                return false;
            case KeyEvent.VK_CAPS_LOCK:
                if (pressed && Options.caps_sends_up_and_down)
                    capsLockOn = !capsLockOn;
                if (!Options.caps_sends_up_and_down) {
                    capsLockOn = pressed;
                }
                return false;
            case KeyEvent.VK_NUM_LOCK:
                if (pressed)
                    numLockOn = !numLockOn;
                return false;
            case KeyEvent.VK_SCROLL_LOCK:
                if (pressed)
                    scrollLockOn = !scrollLockOn;
                return false;
            case KeyEvent.VK_PAUSE: 
                if (ctrlDown) {
                    if (pressed) {
                        sendScancode(time, RDP_KEYPRESS, (RDP_INPUT_SCANCODE | 0x46));
                        sendScancode(time, RDP_KEYPRESS, (RDP_INPUT_SCANCODE | 0x46));
                    }
                    break;
                }
                if (pressed) { 
                    rdp.sendInput((int) time, RDP_INPUT_SCANCODE, RDP_KEYPRESS,
                            0xe1, 0);
                    rdp.sendInput((int) time, RDP_INPUT_SCANCODE, RDP_KEYPRESS,
                            0x1d, 0);
                    rdp.sendInput((int) time, RDP_INPUT_SCANCODE, RDP_KEYPRESS,
                            0x45, 0);
                    rdp.sendInput((int) time, RDP_INPUT_SCANCODE, RDP_KEYPRESS,
                            0xe1, 0);
                    rdp.sendInput((int) time, RDP_INPUT_SCANCODE, RDP_KEYPRESS,
                            0x9d, 0);
                    rdp.sendInput((int) time, RDP_INPUT_SCANCODE, RDP_KEYPRESS,
                            0xc5, 0);
                } else { 
                    rdp.sendInput((int) time, RDP_INPUT_SCANCODE, RDP_KEYRELEASE,
                            0x1d, 0);
                }
                break;

            
            /*
             * case KeyEvent.VK_META: 
             * received"); if(pressed){ sendScancode(time, RDP_KEYPRESS, 0x1d); 
             * left ctrl sendScancode(time, RDP_KEYPRESS, 0x01); 
             * sendScancode(time, RDP_KEYRELEASE, 0x01); 
             * sendScancode(time, RDP_KEYRELEASE, 0x1d); 
             */

            
            
            /*
             * case KeyEvent.VK_BREAK: if(pressed){
             * sendScancode(time,RDP_KEYPRESS,(KeyCode.SCANCODE_EXTENDED | 0x46));
             * sendScancode(time,RDP_KEYPRESS,(KeyCode.SCANCODE_EXTENDED | 0xc6)); } 
             * do nothing on release break;
             */
            default:
                return false; 
        }
        return true; 
    }

    /**
     * Turn off any locking key, check states if available
     */
    public void triggerReadyToSend() {
        capsLockOn = false;
        numLockOn = false;
        scrollLockOn = false;
        doLockKeys(); 
    }

    void doLockKeys() {
    }

    /**
     * Handle pressing of the middle mouse button, sending relevent event data
     * to the server
     *
     * @param e MouseEvent detailing circumstances under which middle button
     *          was pressed
     */
    private void middleButtonPressed(MouseEvent e) {
        /*
         * if (Options.paste_hack && ctrlDown){ try{ canvas.setBusyCursor();
         * }catch (RdesktopException ex){ logger.warn(ex.getMessage()); } if
         * (capsLockOn){ logger.debug("Turning caps lock off for paste"); 
         * turn caps lock off sendScancode(getTime(), RDP_KEYPRESS, 0x3a); 
         * caps lock sendScancode(getTime(), RDP_KEYRELEASE, 0x3a); 
         * paste(); if (capsLockOn){ 
         * logger.debug("Turning caps lock back on after paste");
         * sendScancode(getTime(), RDP_KEYPRESS, 0x3a); 
         * sendScancode(getTime(), RDP_KEYRELEASE, 0x3a); 
         * canvas.unsetBusyCursor(); } else
         */
        rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON3
                | MOUSE_FLAG_DOWN, e.getX(), e.getY());
    }

    /**
     * Handle release of the middle mouse button, sending relevent event data to
     * the server
     *
     * @param e MouseEvent detailing circumstances under which middle button
     *          was released
     */
    private void middleButtonReleased(MouseEvent e) {
        /* if (!Options.paste_hack || !ctrlDown) */
        rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON3, e.getX(), e
                .getY());
    }

    class RdesktopKeyAdapter extends KeyAdapter {

        /**
         * Construct an RdesktopKeyAdapter based on the parent KeyAdapter class
         */
        RdesktopKeyAdapter() {
        }

        /**
         * Handle a keyPressed event, sending any relevant keypresses to the
         * server
         */
        @Override
        public void keyPressed(KeyEvent e) {
            lastKeyEvent = e;
            modifiersValid = true;
            long time = getTime();

            
            
            pressedKeys.addElement(e.getKeyCode());

            logger.debug("PRESSED keychar='{}' keycode=0x{} char='{}" + '\'', e.getKeyChar(), Integer.toHexString(e.getKeyCode()), (char) e.getKeyCode());

            if (rdp != null) {
                if (!handleSpecialKeys(time, e, true)) {
                    sendKeyPresses(newKeyMapper.getKeyStrokes(e));
                }
                
            }
        }

        /**
         * Handle a keyTyped event, sending any relevant keypresses to the
         * server
         */
        @Override
        public void keyTyped(KeyEvent e) {
            lastKeyEvent = e;
            modifiersValid = true;
            long time = getTime();

            
            
            pressedKeys.addElement(e.getKeyCode());

            logger.debug("TYPED keychar='{}' keycode=0x{} char='{}" + '\'', e.getKeyChar(), Integer.toHexString(e.getKeyCode()), (char) e.getKeyCode());

            if (rdp != null) {
                if (!handleSpecialKeys(time, e, true))
                    sendKeyPresses(newKeyMapper.getKeyStrokes(e));
                
            }
        }

        /**
         * Handle a keyReleased event, sending any relevent key events to the
         * server
         */
        @Override
        public void keyReleased(KeyEvent e) {
            
            
            
            Integer keycode = e.getKeyCode();
            if (!pressedKeys.contains(keycode)) {
                this.keyPressed(e);
            }

            pressedKeys.removeElement(keycode);

            lastKeyEvent = e;
            modifiersValid = true;
            long time = getTime();

            logger.debug("RELEASED keychar='{}' keycode=0x{} char='{}" + '\'', e.getKeyChar(), Integer.toHexString(e.getKeyCode()), (char) e.getKeyCode());
            if (rdp != null) {
                if (!handleSpecialKeys(time, e, false))
                    sendKeyPresses(newKeyMapper.getKeyStrokes(e));
                
            }
        }

    }

    class RdesktopMouseAdapter extends MouseAdapter {

        RdesktopMouseAdapter() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getY() != 0)
                ((RdesktopFrame) canvas.getParent()).hideMenu();

            int time = getTime();
            if (rdp != null) {
                if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    logger.debug("Mouse Button 1 Pressed.");
                    rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON1
                            | MOUSE_FLAG_DOWN, e.getX(), e.getY());
                } else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                    logger.debug("Mouse Button 3 Pressed.");
                    rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON2
                            | MOUSE_FLAG_DOWN, e.getX(), e.getY());
                } else if ((e.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) {
                    logger.debug("Middle Mouse Button Pressed.");
                    middleButtonPressed(e);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int time = getTime();
            if (rdp != null) {
                if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON1, e
                            .getX(), e.getY());
                } else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                    rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_BUTTON2, e
                            .getX(), e.getY());
                } else if ((e.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) {
                    middleButtonReleased(e);
                }
            }
        }
    }

    class RdesktopMouseMotionAdapter extends MouseMotionAdapter {

        RdesktopMouseMotionAdapter() {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int time = getTime();

            
            
            
            
            

            
            

            
            if (e.getY() == 0)
                ((RdesktopFrame) canvas.getParent()).showMenu();
            

            if (rdp != null) {
                rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_MOVE, e.getX(),
                        e.getY());
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int time = getTime();
            
            
            if (rdp != null) {
                rdp.sendInput(time, RDP_INPUT_MOUSE, MOUSE_FLAG_MOVE, e.getX(),
                        e.getY());
            }
        }
    }

}
