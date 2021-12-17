/*
 * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright (c) 2010 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 */

package jogamp.newt.driver.x11;

import com.jogamp.common.ExceptionUtils;
import com.jogamp.common.nio.Buffers;
import com.jogamp.nativewindow.*;
import com.jogamp.nativewindow.VisualIDHolder.VIDType;
import com.jogamp.nativewindow.util.InsetsImmutable;
import com.jogamp.nativewindow.util.Point;
import com.jogamp.nativewindow.x11.X11GraphicsDevice;
import com.jogamp.nativewindow.x11.X11GraphicsScreen;
import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import jogamp.nativewindow.x11.X11Lib;
import jogamp.nativewindow.x11.X11Util;
import jogamp.newt.DisplayImpl.DisplayRunnable;
import jogamp.newt.PointerIconImpl;
import jogamp.newt.WindowImpl;

import java.nio.Buffer;

public class WindowDriver extends WindowImpl {
    private static final String WINDOW_CLASS_NAME = "NewtWindow";
    private static final int X11_WHEEL_ONE_UP_BUTTON   = 4;
    private static final int X11_WHEEL_ONE_DOWN_BUTTON = 5;
    private static final int X11_WHEEL_TWO_UP_BUTTON   = 6;
    private static final int X11_WHEEL_TWO_DOWN_BUTTON = 7;

//    private static final int defaultIconDataSize;
//    private static final Buffer defaultIconData;
//
//    static {
//        ScreenDriver.initSingleton();
//
//        int _icon_data_size=0, _icon_elem_bytesize=0;
//        Buffer _icon_data=null;
//        if( PNGIcon.isAvailable() ) {
//            try {
//                // NOTE: MUST BE DIRECT BUFFER, since _NET_WM_ICON Atom uses buffer directly!
//                final int[] data_size = { 0 }, elem_bytesize = { 0 };
//                _icon_data = PNGIcon.arrayToX11BGRAImages(NewtFactory.getWindowIcons(), data_size, elem_bytesize);
//                _icon_data_size = data_size[0];
//                _icon_elem_bytesize = elem_bytesize[0];
//            } catch (final Exception e) {
//                e.printStackTrace();
//            }
//        }
//        defaultIconDataSize = _icon_data_size;
//        defaultIconData = _icon_data;
//        if(DEBUG_IMPLEMENTATION) {
//            System.err.println("Def. Icon: data_size "+defaultIconDataSize+" * elem_size "+_icon_elem_bytesize+" = data "+defaultIconData);
//        }
//    }

    public WindowDriver() {
    }

    @Override
    protected void createNativeImpl() {
        ScreenDriver screen = (ScreenDriver) getScreen();
        DisplayDriver display = (DisplayDriver) screen.getDisplay();
        AbstractGraphicsDevice edtDevice = display.getGraphicsDevice();

        // Decoupled X11 Device/Screen allowing X11 display lock-free off-thread rendering
        long renderDeviceHandle = X11Util.openDisplay(edtDevice.getConnection());
        if( 0 == renderDeviceHandle ) {
            throw new RuntimeException("Error creating display(GfxCfg/Render): "+edtDevice.getConnection());
        }
        renderDevice = new X11GraphicsDevice(renderDeviceHandle, AbstractGraphicsDevice.DEFAULT_UNIT, true /* owner */);
        AbstractGraphicsScreen renderScreen = new X11GraphicsScreen(renderDevice, screen.getIndex());

        GraphicsConfigurationFactory factory = GraphicsConfigurationFactory.getFactory(display.getGraphicsDevice(), capsRequested);
        AbstractGraphicsConfiguration cfg = factory.chooseGraphicsConfiguration(
                capsRequested, capsRequested, capabilitiesChooser, renderScreen, VisualIDHolder.VID_UNDEFINED);
        if(DEBUG_IMPLEMENTATION) {
            System.err.println("X11Window.createNativeImpl() factory: "+factory+", chosen config: "+cfg);
        }
        if (null == cfg) {
            throw new NativeWindowException("Error choosing GraphicsConfiguration creating window: "+this);
        }
        int visualID = cfg.getVisualID(VIDType.NATIVE);
        if(VisualIDHolder.VID_UNDEFINED == visualID) {
            throw new NativeWindowException("Chosen Configuration w/o native visual ID: "+cfg);
        }
        setGraphicsConfiguration(cfg);
        int flags = getReconfigureMask(0, true) & STATE_MASK_CREATENATIVE;
        edtDevice.lock();
        try {
            long[] handles = CreateWindow(getParentWindowHandle(),
                                                edtDevice.getHandle(), screen.getIndex(), visualID,
                                                display.getJavaObjectAtom(), display.getWindowDeleteAtom(),
                                                getX(), getY(), getWidth(), getHeight(), flags,
                                                //defaultIconDataSize, defaultIconData,
                                    0,null,
                                                DEBUG_IMPLEMENTATION);
            if (null == handles || 2 != handles.length || 0 == handles[0] || 0 == handles[1] ) {
                throw new NativeWindowException("Error creating window");
            }
            if(DEBUG_IMPLEMENTATION) {
                System.err.println("X11Window.createNativeImpl() handles "+toHexString(handles[0])+", "+toHexString(handles[1]));
            }
            setWindowHandle(handles[0]);
            javaWindowHandle = handles[1];
        } finally {
            edtDevice.unlock();
        }
    }

    @Override
    protected void closeNativeImpl() {
        if(0!=javaWindowHandle && null!=getScreen() ) {
            DisplayDriver display = (DisplayDriver) getScreen().getDisplay();
            AbstractGraphicsDevice edtDevice = display.getGraphicsDevice();
            edtDevice.lock();
            try {
                CloseWindow0(edtDevice.getHandle(), javaWindowHandle /* , display.getKbdHandle() */, // XKB disabled for now
                             display.getRandREventBase(), display.getRandRErrorBase());
            } catch (Throwable t) {
                if(DEBUG_IMPLEMENTATION) {
                    Exception e = new Exception("Warning: closeNativeImpl failed - "+Thread.currentThread().getName(), t);
                    e.printStackTrace();
                }
            } finally {
                edtDevice.unlock();
                javaWindowHandle = 0;
            }
        }
        if(null != renderDevice) {
            renderDevice.close(); // closes X11 display
            renderDevice = null;
        }
    }

    @Override
    protected final int getSupportedReconfigMaskImpl() {
        return ( minimumReconfigStateMask | GetSupportedReconfigMask0(javaWindowHandle) ) & STATE_MASK_ALL_RECONFIG;
    }

    @Override
    protected boolean reconfigureWindowImpl(int x, int y, int width, int height, int flags) {
        int _x, _y;
        InsetsImmutable _insets;
        if( 0 == ( STATE_MASK_UNDECORATED & flags) ) {
            // client position -> top-level window position
            _insets = getInsets();
            _x = x - _insets.getLeftWidth() ;
            _y = y - _insets.getTopHeight() ;
        } else {
            _insets = null;
            _x = x;
            _y = y;
        }
        if(DEBUG_IMPLEMENTATION) {
            System.err.println("X11Window reconfig.0: "+x+"/"+y+" -> "+_x+"/"+_y+" "+width+"x"+height+", insets "+_insets+
                               ", "+getReconfigStateMaskString(flags));
        }
        if( 0 != ( CHANGE_MASK_FULLSCREEN & flags ) ) {
            if( 0 != ( STATE_MASK_FULLSCREEN & flags) &&
                0 == ( STATE_MASK_ALWAYSONTOP & flags) &&
                0 == ( STATE_MASK_ALWAYSONBOTTOM & flags) ) {
                tempFSAlwaysOnTop = true;
                flags |= STATE_MASK_ALWAYSONTOP;
                if(DEBUG_IMPLEMENTATION) {
                    System.err.println("X11Window reconfig.2: temporary "+getReconfigStateMaskString(flags));
                }
            } else {
                tempFSAlwaysOnTop = false;
            }
        }
        int fflags = flags;
        runWithLockedDisplayDevice(dpy -> {
            reconfigureWindow0( dpy, getScreenIndex(),
                                getParentWindowHandle(), javaWindowHandle, _x, _y, width, height, fflags);
            return null;
        });
        if(DEBUG_IMPLEMENTATION) {
            System.err.println("X11Window reconfig.X: "+getX()+"/"+getY()+" "+getWidth()+"x"+getHeight()+", insets "+getInsets()+", "+getStateMaskString());
        }
        return true;
    }
    volatile boolean tempFSAlwaysOnTop = false;

    /**
     * <p>
     * Deal w/ tempAlwaysOnTop.
     * </p>
     * {@inheritDoc}
     */
    @Override
    protected void focusChanged(boolean defer, boolean focusGained) {
        if( isNativeValid() && isFullscreen() && !isAlwaysOnBottom() && tempFSAlwaysOnTop && hasFocus() != focusGained ) {
            int flags = getReconfigureMask(CHANGE_MASK_ALWAYSONTOP, isVisible()) |
                              ( focusGained ? STATE_MASK_ALWAYSONTOP : 0 );
            if(DEBUG_IMPLEMENTATION) {
                System.err.println("X11Window reconfig.3 (focus): temporary "+getReconfigStateMaskString(flags));
            }
            runWithLockedDisplayDevice(dpy -> {
                reconfigureWindow0( dpy, getScreenIndex(),
                                    getParentWindowHandle(), javaWindowHandle, getX(), getY(), getWidth(), getHeight(), flags);
                return null;
            });
        }
        super.focusChanged(defer, focusGained);
    }

    protected void reparentNotify(long newParentWindowHandle) {
        if(DEBUG_IMPLEMENTATION) {
            long p0 = getParentWindowHandle();
            System.err.println("Window.reparentNotify ("+getThreadName()+"): "+toHexString(p0)+" -> "+toHexString(newParentWindowHandle));
        }
    }

    @Override
    protected void requestFocusImpl(boolean force) {
        runWithLockedDisplayDevice(dpy -> {
            requestFocus0(dpy, javaWindowHandle, force);
            return null;
        });
    }

    @Override
    protected void setTitleImpl(String title) {
        runWithLockedDisplayDevice(dpy -> {
            setTitle0(dpy, javaWindowHandle, title);
            return null;
        });
    }

    @Override
    protected void setPointerIconImpl(PointerIconImpl pi) {
        runWithLockedDisplayDevice(dpy -> {
            try {
                setPointerIcon0(dpy, javaWindowHandle, null != pi ? pi.validatedHandle() : 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    protected boolean setPointerVisibleImpl(boolean pointerVisible) {
        return runWithLockedDisplayDevice(dpy -> {
            PointerIconImpl pi = (PointerIconImpl) getPointerIcon();
            boolean res;
            if (pointerVisible && null != pi) {
                setPointerIcon0(dpy, javaWindowHandle, pi.validatedHandle());
                res = true;
            } else {
                res = setPointerVisible0(dpy, javaWindowHandle, pointerVisible);
            }
            return res;
        });
    }

    @Override
    protected boolean confinePointerImpl(boolean confine) {
        return runWithLockedDisplayDevice(dpy -> confinePointer0(dpy, javaWindowHandle, confine));
    }

    @Override
    protected void warpPointerImpl(int x, int y) {
        runWithLockedDisplayDevice(dpy -> {
            warpPointer0(dpy, javaWindowHandle, x, y);
            return null;
        });
    }

    @Override
    protected Point getLocationOnScreenImpl(int x, int y) {
        return runWithLockedDisplayDevice(dpy -> X11Lib.GetRelativeLocation(dpy, getScreenIndex(), getWindowHandle(), 0 /*root win*/, x, y));
    }

    @Override
    protected final void doMouseEvent(boolean enqueue, boolean wait, short eventType, int modifiers,
                                      int x, int y, short button, float[] rotationXYZ, float rotationScale) {
        switch(eventType) {
            case MouseEvent.EVENT_MOUSE_PRESSED:
                switch(button) {
                    case X11_WHEEL_ONE_UP_BUTTON:
                    case X11_WHEEL_ONE_DOWN_BUTTON:
                    case X11_WHEEL_TWO_UP_BUTTON:
                    case X11_WHEEL_TWO_DOWN_BUTTON:
                        // ignore wheel pressed !
                        return;
                }
                break;
            case MouseEvent.EVENT_MOUSE_RELEASED:
                boolean shiftPressed = 0 != ( modifiers & InputEvent.SHIFT_MASK );
                switch (button) {
                    case X11_WHEEL_ONE_UP_BUTTON -> { // vertical scroll up
                        eventType = MouseEvent.EVENT_MOUSE_WHEEL_MOVED;
                        button = 1;
                        rotationXYZ[shiftPressed ? 0 : 1] = 1;
                    }
                    case X11_WHEEL_ONE_DOWN_BUTTON -> { // vertical scroll down
                        eventType = MouseEvent.EVENT_MOUSE_WHEEL_MOVED;
                        button = 1;
                        rotationXYZ[shiftPressed ? 0 : 1] = -1;
                    }
                    case X11_WHEEL_TWO_UP_BUTTON -> { // horizontal scroll left
                        eventType = MouseEvent.EVENT_MOUSE_WHEEL_MOVED;
                        button = 1;
                        rotationXYZ[0] = 1;
                        modifiers |= InputEvent.SHIFT_MASK;
                    }
                    case X11_WHEEL_TWO_DOWN_BUTTON -> { // horizontal scroll right
                        eventType = MouseEvent.EVENT_MOUSE_WHEEL_MOVED;
                        button = 1;
                        rotationXYZ[0] = -1;
                        modifiers |= InputEvent.SHIFT_MASK;
                    }
                }
                break;
        }
        super.doMouseEvent(enqueue, wait, eventType, modifiers, x, y, button, rotationXYZ, rotationScale);
    }

    /** Called by native TK */
    protected final void sendKeyEvent(short eventType, int modifiers, short keyCode, short keySym, char keyChar0, String keyString) {
        // handleKeyEvent(true, false, eventType, modifiers, keyCode, keyChar);
        boolean isModifierKey = KeyEvent.isModifierKey(keyCode);
        boolean isAutoRepeat = 0 != ( InputEvent.AUTOREPEAT_MASK & modifiers );
        char keyChar =  ( null != keyString ) ? keyString.charAt(0) : keyChar0;
        // System.err.println("*** sendKeyEvent: event "+KeyEvent.getEventTypeString(eventType)+", keyCode "+toHexString(keyCode)+", keyChar <"+keyChar0+">/<"+keyChar+">, keyString "+keyString+", mods "+toHexString(modifiers)+
        //                    ", isKeyCodeTracked "+isKeyCodeTracked(keyCode)+", was: pressed "+isKeyPressed(keyCode)+", repeat "+isAutoRepeat+", [modifierKey "+isModifierKey+"] - "+System.currentTimeMillis());

        if( !isAutoRepeat || !isModifierKey ) { // ! (  isModifierKey && isAutoRepeat )
            switch (eventType) {
                case KeyEvent.EVENT_KEY_PRESSED -> super.sendKeyEvent(KeyEvent.EVENT_KEY_PRESSED, modifiers, keyCode, keySym, keyChar);
                case KeyEvent.EVENT_KEY_RELEASED -> super.sendKeyEvent(KeyEvent.EVENT_KEY_RELEASED, modifiers, keyCode, keySym, keyChar);
            }
        }
    }

    @Override
    public final void sendKeyEvent(short eventType, int modifiers, short keyCode, short keySym, char keyChar) {
        throw new InternalError("XXX: Adapt Java Code to Native Code Changes");
    }
    @Override
    public final void enqueueKeyEvent(boolean wait, short eventType, int modifiers, short keyCode, short keySym, char keyChar) {
        throw new InternalError("XXX: Adapt Java Code to Native Code Changes");
    }

    //----------------------------------------------------------------------
    // Internals only
    //
    private static String getCurrentThreadName() { return Thread.currentThread().getName(); } // Callback for JNI
    private static void dumpStack() { ExceptionUtils.dumpStack(System.err); } // Callback for JNI

    private <T> T runWithLockedDisplayDevice(DisplayRunnable<T> action) {
        return ((DisplayDriver) getScreen().getDisplay()).runWithLockedDisplayDevice(action);
    }

    protected static native boolean initIDs0();

    private long[] CreateWindow(long parentWindowHandle, long display, int screen_index,
                                int visualID, long javaObjectAtom, long windowDeleteAtom,
                                int x, int y, int width, int height, int flags,
                                int pixelDataSize, Buffer pixels, boolean verbose) {
        // NOTE: MUST BE DIRECT BUFFER, since _NET_WM_ICON Atom uses buffer directly!
        if( !Buffers.isDirect(pixels) ) {
            throw new IllegalArgumentException("data buffer is not direct "+pixels);
        }
        return CreateWindow0(parentWindowHandle, display, screen_index,
                             visualID, javaObjectAtom, windowDeleteAtom,
                             x, y, width, height, flags,
                             pixelDataSize, pixels, Buffers.getDirectBufferByteOffset(pixels), true /* pixels_is_direct */, verbose);
    }
    /** returns long[2] { X11-window-handle, JavaWindow-handle } */
    private native long[] CreateWindow0(long parentWindowHandle, long display, int screen_index,
                                        int visualID, long javaObjectAtom, long windowDeleteAtom,
                                        int x, int y, int width, int height, int flags,
                                        int pixelDataSize, Object pixels, int pixels_byte_offset, boolean pixels_is_direct,
                                        boolean verbose);
    private static native int GetSupportedReconfigMask0(long javaWindowHandle);
    private native void CloseWindow0(long display, long javaWindowHandle /*, long kbdHandle*/, // XKB disabled for now
                                     int randr_event_base, int randr_error_base);
    private static native void reconfigureWindow0(long display, int screen_index, long parentWindowHandle, long javaWindowHandle,
                                                  int x, int y, int width, int height, int flags);
    private static native void requestFocus0(long display, long javaWindowHandle, boolean force);

    private static native void setTitle0(long display, long javaWindowHandle, String title);

    private static native void setPointerIcon0(long display, long javaWindowHandle, long handle);

    private static native boolean setPointerVisible0(long display, long javaWindowHandle, boolean visible);
    private static native boolean confinePointer0(long display, long javaWindowHandle, boolean grab);
    private static native void warpPointer0(long display, long javaWindowHandle, int x, int y);

    /** Of native type JavaWindow, containing 'jobject window', X11 Window, beside other states. */
    private volatile long javaWindowHandle = 0; // lifecycle critical
    private X11GraphicsDevice renderDevice;
}