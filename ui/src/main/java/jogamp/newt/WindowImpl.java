package jogamp.newt;

import com.jogamp.common.ExceptionUtils;
import com.jogamp.common.util.ArrayHashSet;
import com.jogamp.common.util.Bitfield;
import com.jogamp.common.util.PropertyAccess;
import com.jogamp.common.util.ReflectionUtil;
import com.jogamp.common.util.locks.LockFactory;
import com.jogamp.common.util.locks.RecursiveLock;
import com.jogamp.nativewindow.*;
import com.jogamp.nativewindow.util.*;
import com.jogamp.newt.Display.PointerIcon;
import com.jogamp.newt.*;
import com.jogamp.newt.event.*;
import com.jogamp.newt.event.MouseEvent.PointerType;
import jcog.Util;
import jcog.data.list.FastCoWList;
import jcog.data.list.Lst;
import jogamp.nativewindow.SurfaceScaleUtils;
import jogamp.nativewindow.SurfaceUpdatedHelper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;

public abstract class WindowImpl implements Window, NEWTEventConsumer {

    private static final boolean DEBUG_TEST_REPARENT_INCOMPATIBLE;

    private static final List<WeakReference<WindowImpl>> windowList = new Lst<>();
    /**
     * Bug 1249 and Bug 1250: Visibility issues on X11
     * <ul>
     * <li>setVisible(false) IconicState not listening to _NET_WM_STATE_HIDDEN</li>
     * <li>setVisible(true) not restoring from _NET_WM_STATE_HIDDEN</li>
     * </ul>
     * <p>
     * If {@code true} fall back to traditional visibility state,
     * i.e. {@code fast=true}.
     * </p>
     */
    private static final int QUIRK_BIT_VISIBILITY = 0;
    /**
     * Quirk mask
     */
    private static final Bitfield quirks = Bitfield.Factory.synchronize(Bitfield.Factory.create(32));
    /**
     * Number of all public state bits, {@value}.
     * <p>Defaults to {@code false}.</p>
     *
     * @see #getStateMask()
     * @since 2.3.2
     */
    private static final int STATE_BIT_COUNT_ALL_PUBLIC = STATE_BIT_POINTERCONFINED + 1;
    /**
     * Bitmask for {@link #STATE_BIT_COUNT_ALL_PUBLIC}
     */
    private static final int STATE_MASK_ALL_PUBLIC = (1 << STATE_BIT_COUNT_ALL_PUBLIC) - 1;
    //
    // Additional private reconfigure state-mask bits and mask values
    //
    private static final int STATE_BIT_FULLSCREEN_SPAN = STATE_BIT_COUNT_ALL_PUBLIC;
    private static final int STATE_BIT_COUNT_ALL_RECONFIG = STATE_BIT_FULLSCREEN_SPAN + 1;
    /**
     * Bitmask for {@link #STATE_BIT_COUNT_ALL_RECONFIG}
     */
    protected static final int STATE_MASK_ALL_RECONFIG = (1 << STATE_BIT_COUNT_ALL_RECONFIG) - 1;
    private static final int STATE_MASK_ALL_PUBLIC_SUPPORTED = STATE_MASK_ALL_PUBLIC & ~STATE_MASK_AUTOPOSITION;
    /**
     * Reconfig mask for createNativeImpl(..) taking out from {@link #getStateMask()}:
     * <ul>
     *   <li>{@link #STATE_MASK_FULLSCREEN}</li>
     *   <li>{@link #STATE_MASK_POINTERVISIBLE}</li>
     *   <li>{@link #STATE_MASK_POINTERCONFINED}</li>
     * </ul>
     * Above taken out states are achieved from caller createNative() 'manually'.
     *
     * @since 2.3.2
     */
    protected static final int STATE_MASK_CREATENATIVE = STATE_MASK_ALL_PUBLIC &
            ~(STATE_MASK_FULLSCREEN |
                    STATE_MASK_POINTERVISIBLE |
                    STATE_MASK_POINTERCONFINED
            );
    //
    // Additional private state-mask mask values for reconfiguration only
    // (keep in sync w/ src/newt/native/Window.h)
    //
    private static final int CHANGE_MASK_VISIBILITY = 1 << 31;
    private static final int CHANGE_MASK_VISIBILITY_FAST = 1 << 30; // fast visibility change, i.e. skip WM
    private static final int CHANGE_MASK_PARENTING = 1 << 29;
    private static final int CHANGE_MASK_DECORATION = 1 << 28;
    protected static final int CHANGE_MASK_ALWAYSONTOP = 1 << 27;
    private static final int CHANGE_MASK_ALWAYSONBOTTOM = 1 << 26;
    private static final int CHANGE_MASK_STICKY = 1 << 25;
    private static final int CHANGE_MASK_RESIZABLE = 1 << 24;
    private static final int CHANGE_MASK_MAXIMIZED_VERT = 1 << 23;
    private static final int CHANGE_MASK_MAXIMIZED_HORZ = 1 << 22;
    protected static final int CHANGE_MASK_FULLSCREEN = 1 << 21;
    /**
     * See {@link #getSupportedStateMask()}, i.e. {@link #STATE_MASK_VISIBLE} | {@link #STATE_MASK_FOCUSED} | {@link STATE_MASK_FULLSCREEN}.
     */
    protected static final int minimumReconfigStateMask = STATE_MASK_VISIBLE | STATE_MASK_FOCUSED | STATE_MASK_FULLSCREEN;
    /**
     * Timeout of queued events (repaint and resize)
     */
    private static final long QUEUED_EVENT_TO = 1200; // ms
    /* pp */ private static final int PSTATE_BIT_FOCUS_CHANGE_BROKEN = 30;
    /* pp */ private static final int PSTATE_BIT_FULLSCREEN_MAINMONITOR = 31; // true
    /**
     * Bitmask for {@link #STATE_BIT_FULLSCREEN_SPAN}, {@value}.
     */
    /* pp */ private static final int STATE_MASK_FULLSCREEN_SPAN = 1 << STATE_BIT_FULLSCREEN_SPAN;
    /* pp */ static final int PSTATE_MASK_FOCUS_CHANGE_BROKEN = 1 << PSTATE_BIT_FOCUS_CHANGE_BROKEN;
    /* pp */ private static final int PSTATE_MASK_FULLSCREEN_MAINMONITOR = 1 << PSTATE_BIT_FULLSCREEN_MAINMONITOR;
    private static final int sizePosWaitMS =
            //10;
            //2;
            1;
    private static final boolean DEBUG_FREEZE_AT_VISIBILITY_FAILURE;
    private static final PointerType[] constMousePointerTypes = {PointerType.Mouse};
    /**
     * Mask covering all preserved non-fullscreen (NFS) states
     * while in fullscreen mode.
     */
    private static final int STATE_MASK_FULLSCREEN_NFS = STATE_MASK_ALWAYSONTOP |
            STATE_MASK_RESIZABLE |
            STATE_MASK_MAXIMIZED_VERT |
            STATE_MASK_MAXIMIZED_HORZ;

    //
    // Quirks
    //
    private static final MouseListener[] EmptyMouseListenerArray = new MouseListener[0];
    //
    // KeyListener/Event Support
    //
    private static final int keyTrackingRange = 255;

    //
    // State Mask
    //

    static {
        Debug.initSingleton();
        DEBUG_TEST_REPARENT_INCOMPATIBLE = PropertyAccess.isPropertyDefined("newt.test.Window.reparent.incompatible", true);
        DEBUG_FREEZE_AT_VISIBILITY_FAILURE = PropertyAccess.isPropertyDefined("newt.debug.Window.visibility.failure.freeze", true);
        ScreenImpl.initSingleton();
    }

    private final float[] minPixelScale = {ScalableSurface.IDENTITY_PIXELSCALE, ScalableSurface.IDENTITY_PIXELSCALE};
    private final float[] maxPixelScale = {ScalableSurface.IDENTITY_PIXELSCALE, ScalableSurface.IDENTITY_PIXELSCALE};
    private final float[] hasPixelScale = {ScalableSurface.IDENTITY_PIXELSCALE, ScalableSurface.IDENTITY_PIXELSCALE};
    private final float[] reqPixelScale = {ScalableSurface.AUTOMAX_PIXELSCALE, ScalableSurface.AUTOMAX_PIXELSCALE};
    /**
     * Regular state mask
     */
    /* pp */ private final Bitfield stateMask = Bitfield.Factory.synchronize(Bitfield.Factory.create(32));

    //
    // Additional private non-reconfigure state-mask bits and mask values
    //
    private final Insets insets = new Insets(); // insets of decoration (if top-level && decorated)
    private final RecursiveLock windowLock = LockFactory.createRecursiveLock();  // Window instance wide lock
    /**
     * Non fullscreen state mask
     */
    private final Bitfield stateMaskNFS = Bitfield.Factory.synchronize(Bitfield.Factory.create(32));
    private final SurfaceUpdatedHelper surfaceUpdatedHelper = new SurfaceUpdatedHelper();
    private final Object childWindowsLock = new Object();
    private final Lst<NativeWindow> childWindows = new Lst<>();
    private final PointerState0 pState0 = new PointerState0();
    private final PointerState1 pState1 = new PointerState1();
    /**
     * Pointer names -> pointer ID (consecutive index, starting w/ 0)
     */
    private final ArrayHashSet<Integer> pName2pID = new ArrayHashSet<>(false, ArrayHashSet.DEFAULT_INITIAL_CAPACITY, ArrayHashSet.DEFAULT_LOAD_FACTOR);
    //----------------------------------------------------------------------
    // WindowClosingProtocol implementation
    //
    private final Object closingListenerLock = new Object();
    private final int[] normPosSize = {0, 0, 0, 0};
    private final boolean[] normPosSizeStored = {false, false};
    private final FullScreenAction fullScreenAction = new FullScreenAction();
    private final MonitorModeListenerImpl monitorModeListenerImpl = new MonitorModeListenerImpl();
    private final Bitfield keyPressedState = Bitfield.Factory.create(keyTrackingRange + 1);
    protected CapabilitiesImmutable capsRequested;
    protected CapabilitiesChooser capabilitiesChooser; // default null -> default
    /**
     * Default is all but {@link #STATE_MASK_FULLSCREEN_SPAN}.
     */
    private int supportedReconfigStateMask = 0;
    private boolean keyboardVisible = false;
    //
    // Volatile: Multithreaded Mutable Access
    //
    private volatile long windowHandle = 0; // lifecycle critical
    private volatile int pixWidth = 128, pixHeight = 128; // client-area size w/o insets in pixel units, default: may be overwritten by user
    private volatile int winWidth = 128, winHeight = 128; // client-area size w/o insets in window units, default: may be overwritten by user
    private volatile int x = 64, y = 64; // client-area pos w/o insets in window units
    private boolean blockInsetsChange = false; // block insets change (from same thread)
    private int surfaceLockCount = 0; // surface lock recursion count
    private ScreenImpl screen; // never null after create - may change reference though (reparent)
    private boolean screenReferenceAdded = false;
    private NativeWindow parentWindow;
    private long parentWindowHandle = 0;
    private final Runnable requestFocusAction = () -> {
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window.RequestFocusAction: force 0 - (" + getThreadName() + "): state " + getStateMaskString() + " -> focus true - windowHandle " + toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle));
        }
        WindowImpl.this.requestFocusImpl(false);
    };
    private final Runnable requestFocusActionForced = () -> {
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window.RequestFocusAction: force 1 - (" + getThreadName() + "): state " + getStateMaskString() + " -> focus true - windowHandle " + toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle));
        }
        WindowImpl.this.requestFocusImpl(true);
    };
    private AbstractGraphicsConfiguration config; // control access due to delegation
    private List<MonitorDevice> fullscreenMonitors;
    private int nfs_width, nfs_height, nfs_x, nfs_y; // non fullscreen client-area size/pos w/o insets
    private NativeWindow nfs_parent;          // non fullscreen parent, in case explicit reparenting is performed (offscreen)
    private String title = "Newt Window";
    private PointerIconImpl pointerIcon;
    private LifecycleHook lifecycleHook;
    private Runnable windowDestroyNotifyAction;
    private FocusRunnable focusAction;
    private KeyListener keyboardFocusHandler;
    private Lst<MouseListener> mouseListeners = new Lst<>();
    private boolean defaultGestureHandlerEnabled = true;
    private DoubleTapScrollGesture gesture2PtrTouchScroll;
    private Lst<GestureHandler> pointerGestureHandler = new Lst<>();

    private Lst<GestureHandler.GestureListener> gestureListeners = new Lst<>();

    private Lst<KeyListener> keyListeners = new Lst<>();

    private final FastCoWList<WindowListener> windowListeners = new FastCoWList<>(WindowListener[]::new);

    private final Runnable reparentActionRecreate = () -> {
        RecursiveLock _lock = windowLock;
        _lock.lock();
        try {
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.reparent: ReparentActionRecreate (" + getThreadName() + ") state " + getStateMaskString() + ", windowHandle " + toHexString(windowHandle) + ", parentWindowHandle " + toHexString(parentWindowHandle) + ", parentWindow " + Display.hashCodeNullSafe(parentWindow));
            }
            setVisibleActionImpl(true); // native creation
            requestFocusInt(0 == parentWindowHandle /* skipFocusAction if top-level */);
        } finally {
            _lock.unlock();
        }
    };

    //
    // Construction Methods
    //
    private boolean repaintQueued = false;
    private WindowClosingMode defaultCloseOperation = WindowClosingMode.DISPOSE_ON_CLOSE;
    private final Runnable destroyAction = new Runnable() {
        @Override
        public final void run() {
            boolean animatorPaused = false;
            if (null != lifecycleHook) {
                animatorPaused = lifecycleHook.pauseRenderingAction();
            }
            if (null != lifecycleHook) {
                lifecycleHook.destroyActionPreLock();
            }
            RuntimeException lifecycleCaughtInLock = null;
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window DestroyAction() hasScreen " + (null != screen) + ", isNativeValid " + isNativeValid() + " - " + getThreadName());
                }

                // send synced destroy-notify notification
                sendWindowEvent(WindowEvent.EVENT_WINDOW_DESTROY_NOTIFY);

                // Childs first ..
                synchronized (childWindowsLock) {
                    if (!childWindows.isEmpty()) {
                        // avoid ConcurrentModificationException: parent -> child -> parent.removeChild(this)
                        @SuppressWarnings("unchecked") List<NativeWindow> clonedChildWindows = childWindows.clone();
                        while (!clonedChildWindows.isEmpty()) {
                            NativeWindow nw = clonedChildWindows.remove(0);
                            if (nw instanceof WindowImpl) {
                                ((WindowImpl) nw).windowDestroyNotify(true);
                            } else {
                                nw.destroy();
                            }
                        }
                    }
                }

                if (null != lifecycleHook) {
                    // send synced destroy notification for proper cleanup, eg GLWindow/OpenGL
                    try {
                        lifecycleHook.destroyActionInLock();
                    } catch (RuntimeException re) {
                        lifecycleCaughtInLock = re;
                    }
                }

                if (isNativeValid()) {
                    screen.removeMonitorModeListener(monitorModeListenerImpl);
                    closeNativeImpl();
                    AbstractGraphicsDevice cfgADevice = config.getScreen().getDevice();
                    if (cfgADevice != screen.getDisplay().getGraphicsDevice()) { // don't pull display's device
                        cfgADevice.close(); // ensure a cfg's device is closed
                    }
                    setGraphicsConfiguration(null);
                }
                removeScreenReference();
                com.jogamp.newt.Display dpy = screen.getDisplay();
                if (null != dpy) {
                    dpy.validateEDTStopped();
                }

                // send synced destroyed notification
                sendWindowEvent(WindowEvent.EVENT_WINDOW_DESTROYED);

                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window.destroy() END " + getThreadName()/*+", "+WindowImpl.this*/);
                    if (null != lifecycleCaughtInLock) {
                        System.err.println("Window.destroy() caught: " + lifecycleCaughtInLock.getMessage());
                        lifecycleCaughtInLock.printStackTrace();
                    }
                }
                if (null != lifecycleCaughtInLock) {
                    throw lifecycleCaughtInLock;
                }
            } finally {
                // update states before release window lock
                setWindowHandle(0);
                resetStateMask();
                fullscreenMonitors = null;
                parentWindowHandle = 0;
                hasPixelScale[0] = ScalableSurface.IDENTITY_PIXELSCALE;
                hasPixelScale[1] = ScalableSurface.IDENTITY_PIXELSCALE;
                minPixelScale[0] = ScalableSurface.IDENTITY_PIXELSCALE;
                minPixelScale[1] = ScalableSurface.IDENTITY_PIXELSCALE;
                maxPixelScale[0] = ScalableSurface.IDENTITY_PIXELSCALE;
                maxPixelScale[1] = ScalableSurface.IDENTITY_PIXELSCALE;

                _lock.unlock();
            }
            if (animatorPaused) {
                lifecycleHook.resumeRenderingAction();
            }

            // these refs shall be kept alive - resurrection via setVisible(true)
            /**
             if(null!=parentWindow && parentWindow instanceof Window) {
             ((Window)parentWindow).removeChild(WindowImpl.this);
             }
             childWindows = null;
             surfaceUpdatedListeners = null;
             mouseListeners = null;
             keyListeners = null;
             capsRequested = null;
             lifecycleHook = null;

             screen = null;
             windowListeners = null;
             parentWindow = null;
             */
        }
    };

    /**
     * Maybe utilized at a shutdown hook, impl. does not block.
     */
    public static void shutdownAll() {
        int wCount = windowList.size();
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window.shutdownAll " + wCount + " instances, on thread " + getThreadName());
        }
        for (int i = 0; i < wCount && !windowList.isEmpty(); i++) { // be safe ..
            WindowImpl w = windowList.remove(0).get();
            if (DEBUG_IMPLEMENTATION) {
                long wh = null != w ? w.getWindowHandle() : 0;
                System.err.println("Window.shutdownAll[" + (i + 1) + "/" + wCount + "]: " + toHexString(wh) + ", GCed " + (null == w));
            }
            if (null != w) {
                w.shutdown();
            }
        }
    }

    private static void addWindow2List(WindowImpl window) {
        synchronized (windowList) {
            // GC before add
            int i = 0, gced = 0;
            while (i < windowList.size()) {
                if (null == windowList.get(i).get()) {
                    gced++;
                    windowList.remove(i);
                } else {
                    i++;
                }
            }
            windowList.add(new WeakReference<>(window));
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.addWindow2List: GCed " + gced + ", size " + windowList.size());
            }
        }
    }

    private static StringBuilder appendStateBits(StringBuilder sb, int mask, boolean showChangeFlags) {
        sb.append("[");

        if (showChangeFlags) {
            if (0 != (CHANGE_MASK_VISIBILITY & mask)) {
                sb.append("*");
            }
            if (0 != (CHANGE_MASK_VISIBILITY_FAST & mask)) {
                sb.append("*");
            }
        }
        sb.append((0 != (STATE_MASK_VISIBLE & mask)) ? "visible" : "invisible");
        sb.append(", ");

        sb.append((0 != (STATE_MASK_AUTOPOSITION & mask)) ? "autopos, " : "");

        if (showChangeFlags) {
            if (0 != (CHANGE_MASK_PARENTING & mask)) {
                sb.append("*");
            }
            sb.append((0 != (STATE_MASK_CHILDWIN & mask)) ? "child" : "toplevel");
            sb.append(", ");
        } else if (0 != (STATE_MASK_CHILDWIN & mask)) {
            sb.append("child");
            sb.append(", ");
        }

        sb.append((0 != (STATE_MASK_FOCUSED & mask)) ? "focused, " : "");

        if (showChangeFlags) {
            if (0 != (CHANGE_MASK_DECORATION & mask)) {
                sb.append("*");
            }
            sb.append((0 != (STATE_MASK_UNDECORATED & mask)) ? "undecor" : "decor");
            sb.append(", ");
        } else if (0 != (STATE_MASK_UNDECORATED & mask)) {
            sb.append("undecor");
            sb.append(", ");
        }

        if (showChangeFlags) {
            if (0 != (CHANGE_MASK_ALWAYSONTOP & mask)) {
                sb.append("*");
            }
            sb.append((0 != (STATE_MASK_ALWAYSONTOP & mask)) ? "aontop" : "!aontop");
            sb.append(", ");
        } else if (0 != (STATE_MASK_ALWAYSONTOP & mask)) {
            sb.append("aontop");
            sb.append(", ");
        }

        if (showChangeFlags) {
            if (0 != (CHANGE_MASK_ALWAYSONBOTTOM & mask)) {
                sb.append("*");
            }
            sb.append((0 != (STATE_MASK_ALWAYSONBOTTOM & mask)) ? "aonbottom" : "!aonbottom");
            sb.append(", ");
        } else if (0 != (STATE_MASK_ALWAYSONBOTTOM & mask)) {
            sb.append("aonbottom");
            sb.append(", ");
        }

        if (showChangeFlags) {
            if (0 != (CHANGE_MASK_STICKY & mask)) {
                sb.append("*");
            }
            sb.append((0 != (STATE_MASK_STICKY & mask)) ? "sticky" : "unsticky");
            sb.append(", ");
        } else if (0 != (STATE_MASK_STICKY & mask)) {
            sb.append("sticky");
            sb.append(", ");
        }

        if (showChangeFlags) {
            if (0 != (CHANGE_MASK_RESIZABLE & mask)) {
                sb.append("*");
            }
            sb.append((0 != (STATE_MASK_RESIZABLE & mask)) ? "resizable" : "unresizable");
            sb.append(", ");
        } else if (0 == (STATE_MASK_RESIZABLE & mask)) {
            sb.append("unresizable");
            sb.append(", ");
        }

        if (showChangeFlags) {
            sb.append("max[");
            if (0 != (CHANGE_MASK_MAXIMIZED_HORZ & mask)) {
                sb.append("*");
            }
            if (0 == (STATE_MASK_MAXIMIZED_HORZ & mask)) {
                sb.append("!");
            }
            sb.append("h");
            sb.append(", ");
            if (0 != (CHANGE_MASK_MAXIMIZED_VERT & mask)) {
                sb.append("*");
            }
            if (0 == (STATE_MASK_MAXIMIZED_VERT & mask)) {
                sb.append("!");
            }
            sb.append("v");
            sb.append("], ");
        } else if (0 != ((STATE_MASK_MAXIMIZED_HORZ | STATE_MASK_MAXIMIZED_VERT) & mask)) {
            sb.append("max[");
            if (0 != (STATE_MASK_MAXIMIZED_HORZ & mask)) {
                sb.append("h");
            }
            if (0 != (STATE_MASK_MAXIMIZED_VERT & mask)) {
                sb.append("v");
            }
            sb.append("], ");
        }

        if (showChangeFlags) {
            if (0 != (CHANGE_MASK_FULLSCREEN & mask)) {
                sb.append("*");
            }
            sb.append("fullscreen[");
            sb.append(0 != (STATE_MASK_FULLSCREEN & mask));
            sb.append((0 != (STATE_MASK_FULLSCREEN_SPAN & mask)) ? ", span" : "");
            sb.append("], ");
        } else if (0 != (STATE_MASK_FULLSCREEN & mask)) {
            sb.append("fullscreen");
            sb.append(", ");
        }

        if (showChangeFlags) {
            sb.append("pointer[");
            if (0 == (STATE_MASK_POINTERVISIBLE & mask)) {
                sb.append("invisible");
            } else {
                sb.append("visible");
            }
            sb.append(", ");
            if (0 != (STATE_MASK_POINTERCONFINED & mask)) {
                sb.append("confined");
            } else {
                sb.append("free");
            }
            sb.append("]");
        } else {
            if (0 == (STATE_MASK_POINTERVISIBLE & mask) ||
                    0 != (STATE_MASK_POINTERCONFINED & mask)) {
                sb.append("pointer[");
                if (0 == (STATE_MASK_POINTERVISIBLE & mask)) {
                    sb.append("invisible");
                    sb.append(", ");
                }
                if (0 != (STATE_MASK_POINTERCONFINED & mask)) {
                    sb.append("confined");
                }
                sb.append("]");
            }
        }
        sb.append("]");
        return sb;
    }

    private static Class<?> getWindowClass(String type)
            throws ClassNotFoundException {
        Class<?> windowClass = NewtFactory.getCustomClass(type, "WindowDriver");
        if (null == windowClass) {
            throw new ClassNotFoundException("Failed to find NEWT Window Class <" + type + ".WindowDriver>");
        }
        return windowClass;
    }

    public static WindowImpl create(NativeWindow parentWindow, long parentWindowHandle, Screen screen, CapabilitiesImmutable caps) {
        try {
            Class<?> windowClass;
            if (caps.isOnscreen()) {
                windowClass = getWindowClass(screen.getDisplay().getType());
            } else {
                windowClass = OffscreenWindow.class;
            }
            WindowImpl window = (WindowImpl) windowClass.getConstructor().newInstance();
            window.parentWindow = parentWindow;
            window.parentWindowHandle = parentWindowHandle;
            window.screen = (ScreenImpl) screen;
            window.capsRequested = (CapabilitiesImmutable) caps.cloneMutable();
            window.instantiationFinished();
            addWindow2List(window);
            return window;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new NativeWindowException(t);
        }
    }

    public static WindowImpl create(Object[] cstrArguments, Screen screen, CapabilitiesImmutable caps) {
        try {
            Class<?> windowClass = getWindowClass(screen.getDisplay().getType());
            Class<?>[] cstrArgumentTypes = getCustomConstructorArgumentTypes(windowClass);
            if (null == cstrArgumentTypes) {
                throw new NativeWindowException("WindowClass " + windowClass + " doesn't support custom arguments in constructor");
            }
            int argsChecked = verifyConstructorArgumentTypes(cstrArgumentTypes, cstrArguments);
            if (argsChecked < cstrArguments.length) {
                throw new NativeWindowException("WindowClass " + windowClass + " constructor mismatch at argument #" + argsChecked + "; Constructor: " + getTypeStrList(cstrArgumentTypes) + ", arguments: " + getArgsStrList(cstrArguments));
            }
            WindowImpl window = (WindowImpl) ReflectionUtil.createInstance(windowClass, cstrArgumentTypes, cstrArguments);
            window.screen = (ScreenImpl) screen;
            window.capsRequested = (CapabilitiesImmutable) caps.cloneMutable();
            window.instantiationFinished();
            addWindow2List(window);
            return window;
        } catch (Throwable t) {
            throw new NativeWindowException(t);
        }
    }

    private static long getNativeWindowHandle(NativeWindow nativeWindow) {
        long handle = 0;
        if (null != nativeWindow) {
            boolean wasLocked = false;
            if (NativeSurface.LOCK_SURFACE_NOT_READY < nativeWindow.lockSurface()) {
                wasLocked = true;
                try {
                    handle = nativeWindow.getWindowHandle();
                    if (0 == handle) {
                        throw new NativeWindowException("Parent native window handle is NULL, after succesful locking: " + nativeWindow);
                    }
                } catch (NativeWindowException nwe) {
                    if (DEBUG_IMPLEMENTATION) {
                        System.err.println("Window.getNativeWindowHandle: not successful yet: " + nwe);
                    }
                } finally {
                    nativeWindow.unlockSurface();
                }
            }
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.getNativeWindowHandle: locked " + wasLocked + ", " + nativeWindow);
            }
        }
        return handle;
    }


    //----------------------------------------------------------------------
    // NativeSurface: Native implementation
    //

    private static int lockSurfaceImpl() {
        return LOCK_SUCCESS;
    }

    private static boolean canCreateNativeImpl() {
        return true; // default: always able to be created
    }

    protected static String getReconfigStateMaskString(int flags) {
        return appendStateBits(new StringBuilder(), flags, true).toString();
    }

    protected boolean setPointerVisibleImpl(boolean pointerVisible) {
        return false;
    }

    protected boolean confinePointerImpl(boolean confine) {
        return false;
    }

    /**
     * @param cWin child window, must not be null
     * @param pWin parent window, may be null
     * @return true if at least one of both window's configurations is offscreen
     */
    private static boolean isOffscreenInstance(NativeWindow cWin, NativeWindow pWin) {
        boolean ofs = false;
        AbstractGraphicsConfiguration cWinCfg = cWin.getGraphicsConfiguration();
        if (null != cWinCfg) {
            ofs = !cWinCfg.getChosenCapabilities().isOnscreen();
        }
        if (!ofs && null != pWin) {
            AbstractGraphicsConfiguration pWinCfg = pWin.getGraphicsConfiguration();
            if (null != pWinCfg) {
                ofs = !pWinCfg.getChosenCapabilities().isOnscreen();
            }
        }
        return ofs;
    }

    //----------------------------------------------------------------------
    // Window: Native implementation
    //

    private static boolean isUndecorated(int smask) {
        return 0 != (smask & (STATE_MASK_CHILDWIN | STATE_MASK_UNDECORATED | STATE_MASK_FULLSCREEN));
    }

    private static long NOW() {
        return System.nanoTime() / 1_000_000L;
    }

    private static int step(int lower, int edge, int value) {
        return value < edge ? lower : value;
    }

    protected static boolean isKeyCodeTracked(short keyCode) {
        return (0xFFFF & keyCode) <= keyTrackingRange;
    }

    /**
     * Returns <code>true</code> if operation was successful, otherwise <code>false</code>.
     * <p>
     * We assume that a failed invisible operation is due to an already invisible keyboard,
     * hence even if an invisible operation failed, the keyboard is considered invisible!
     * </p>
     */
    private static boolean setKeyboardVisibleImpl(boolean visible) {
        return false; // nop
    }

    private static boolean propagateKeyEvent(KeyEvent e, KeyListener l) {
        switch (e.getEventType()) {
            case KeyEvent.EVENT_KEY_PRESSED -> l.keyPressed(e);
            case KeyEvent.EVENT_KEY_RELEASED -> l.keyReleased(e);
            default -> throw new NativeWindowException("Unexpected key event type " + e.getEventType());
        }
        return e.isConsumed();
    }

    private static Class<?>[] getCustomConstructorArgumentTypes(Class<?> windowClass) {
        Class<?>[] argTypes = null;
        try {
            Method m = windowClass.getDeclaredMethod("getCustomConstructorArgumentTypes");
            argTypes = (Class[]) m.invoke(null, (Object[]) null);
        } catch (Throwable t) {
        }
        return argTypes;
    }

    private static int verifyConstructorArgumentTypes(Class<?>[] types, Object[] args) {
        if (types.length != args.length) {
            return -1;
        }
        for (int i = 0; i < args.length; i++) {
            if (!types[i].isInstance(args[i])) {
                return i;
            }
        }
        return args.length;
    }

    private static String getArgsStrList(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg.getClass());
            sb.append(", ");
        }
        return sb.toString();
    }

    private static String getTypeStrList(Class<?>[] types) {
        StringBuilder sb = new StringBuilder();
        for (Class<?> type : types) {
            sb.append(type);
            sb.append(", ");
        }
        return sb.toString();
    }

    public static String getThreadName() {
        return Display.getThreadName();
    }

    public static String toHexString(int hex) {
        return Display.toHexString(hex);
    }

    public static String toHexString(long hex) {
        return Display.toHexString(hex);
    }

    /* pp */
    private void resetStateMask() {
        stateMask.clearField(false);
        stateMask.put32(0, 32,
                STATE_MASK_AUTOPOSITION |
                        (null != parentWindow ? STATE_MASK_CHILDWIN : 0) |
                        STATE_MASK_RESIZABLE |
                        STATE_MASK_POINTERVISIBLE |
                        PSTATE_MASK_FULLSCREEN_MAINMONITOR);
        stateMaskNFS.clearField(false);
        normPosSizeStored[0] = false;
        normPosSizeStored[1] = false;
        supportedReconfigStateMask = STATE_MASK_ALL_RECONFIG;
    }

    @Override
    public final int getStatePublicBitCount() {
        return STATE_BIT_COUNT_ALL_PUBLIC;
    }

    @Override
    public final int getStatePublicBitmask() {
        return STATE_MASK_ALL_PUBLIC;
    }

    @Override
    public final int getStateMask() {
        return stateMask.get32(0, STATE_BIT_COUNT_ALL_PUBLIC);
    }

    //----------------------------------------------------------------------
    // NativeSurface
    //

    @Override
    public final String getStateMaskString() {
        return appendStateBits(new StringBuilder(), stateMask.get32(0, STATE_BIT_COUNT_ALL_PUBLIC), false).toString();
    }

    @Override
    public final int getSupportedStateMask() {
        return supportedReconfigStateMask & STATE_MASK_ALL_PUBLIC_SUPPORTED;
    }

    @Override
    public final String getSupportedStateMaskString() {
        return appendStateBits(new StringBuilder(), getSupportedStateMask(), true).toString();
    }

    /**
     * Fast invalidation of instance w/o any blocking function call.
     */
    private void shutdown() {
        if (null != lifecycleHook) {
            lifecycleHook.shutdownRenderingAction();
        }
        setWindowHandle(0);
        resetStateMask();
        fullscreenMonitors = null;
        parentWindowHandle = 0;
    }

    private boolean createNative() {
        long tStart;
        if (DEBUG_IMPLEMENTATION) {
            tStart = System.nanoTime();
            System.err.println("Window.createNative() START (" + getThreadName() + ", " + this + ")");
        } else {
            tStart = 0;
        }

        if (null != parentWindow &&
                NativeSurface.LOCK_SURFACE_NOT_READY >= parentWindow.lockSurface()) {
            throw new NativeWindowException("Parent surface lock: not ready: " + parentWindow);
        }

        boolean hasParent = null != parentWindow || 0 != this.parentWindowHandle;

        // child window: position defaults to 0/0, no auto position, no negative position
        if (hasParent && (stateMask.get(STATE_BIT_AUTOPOSITION) || 0 > getX() || 0 > getY())) {
            definePosition(0, 0);
        }
        boolean postParentlockFocus = false;
        try {
            if (validateParentWindowHandle()) {
                if (!screenReferenceAdded) {
                    screen.addReference();
                    screenReferenceAdded = true;
                }
                if (canCreateNativeImpl()) {
                    int wX, wY;
                    boolean usePosition;
                    if (stateMask.get(STATE_BIT_AUTOPOSITION)) {
                        wX = 0;
                        wY = 0;
                        usePosition = false;
                    } else {
                        wX = getX();
                        wY = getY();
                        usePosition = true;
                    }
                    long t0 = NOW();
                    createNativeImpl();
                    supportedReconfigStateMask = getSupportedReconfigMaskImpl() & STATE_MASK_ALL_RECONFIG;
                    if (DEBUG_IMPLEMENTATION) {
                        boolean minimumOK = minimumReconfigStateMask == (minimumReconfigStateMask & supportedReconfigStateMask);
                        System.err.println("Supported Reconfig (minimum-ok " + minimumOK + "): " + appendStateBits(new StringBuilder(), supportedReconfigStateMask, true));
                    }
                    screen.addMonitorModeListener(monitorModeListenerImpl);
                    setTitleImpl(title);
                    setPointerIconIntern(pointerIcon);
                    if (!stateMask.get(STATE_BIT_POINTERVISIBLE)) {
                        // non default action
                        if (isReconfigureMaskSupported(STATE_MASK_POINTERVISIBLE)) {
                            setPointerVisibleIntern(stateMask.get(STATE_BIT_POINTERVISIBLE));
                        } else {
                            stateMask.set(STATE_BIT_POINTERVISIBLE);
                        }
                    }
                    if (stateMask.get(STATE_BIT_POINTERCONFINED)) {
                        // non default action
                        if (isReconfigureMaskSupported(STATE_MASK_POINTERCONFINED)) {
                            confinePointerImpl(true);
                        } else {
                            stateMask.clear(STATE_BIT_POINTERCONFINED);
                        }
                    }
                    setKeyboardVisible(keyboardVisible);
                    long remainingV = waitForVisible(true, false);
                    if (0 <= remainingV) {
                        if (stateMask.get(STATE_BIT_FULLSCREEN) && !isReconfigureMaskSupported(STATE_MASK_FULLSCREEN)) {
                            stateMask.clear(STATE_BIT_FULLSCREEN);
                        }
                        if (stateMask.get(STATE_BIT_FULLSCREEN)) {
                            synchronized (fullScreenAction) {
                                stateMask.clear(STATE_BIT_FULLSCREEN); // trigger a state change
                                fullScreenAction.init(true);
                                fullScreenAction.run();
                            }
                        } else {
                            if (!hasParent) {
                                // Wait until position is reached within tolerances, either auto-position or custom position.
                                waitForPosition(usePosition, wX, wY, Window.TIMEOUT_NATIVEWINDOW);
                            }
                        }
                        if (DEBUG_IMPLEMENTATION) {
                            System.err.println("Window.createNative(): elapsed " + (NOW() - t0) + " ms");
                        }
//                        postParentlockFocus = true;
                    }
                }
            }
        } finally {
            if (null != parentWindow) {
                parentWindow.unlockSurface();
            }
        }
        if (postParentlockFocus) {
            // harmonize focus behavior for all platforms: focus on creation
            requestFocusInt(isFullscreen() /* skipFocusAction if fullscreen */);
            ((DisplayImpl) screen.getDisplay()).dispatchMessagesNative(); // status up2date
        }
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window.createNative() END (" + getThreadName() + ", " + this + ") total " + (System.nanoTime() - tStart) / 1.0e6 + "ms");
        }
        return isNativeValid();
    }

    private void removeScreenReference() {
        if (screenReferenceAdded) {
            // be nice, probably already called recursive via
            //   closeAndInvalidate() -> closeNativeIml() -> .. -> windowDestroyed() -> closeAndInvalidate() !
            // or via reparentWindow .. etc
            screenReferenceAdded = false;
            screen.removeReference();
        }
    }

    private boolean validateParentWindowHandle() {
        if (null != parentWindow) {
            parentWindowHandle = getNativeWindowHandle(parentWindow);
            return 0 != parentWindowHandle;
        } else {
            return true;
        }
    }

    private void unlockSurfaceImpl() {
    }

    @Override
    public final WindowClosingMode getDefaultCloseOperation() {
        synchronized (closingListenerLock) {
            return defaultCloseOperation;
        }
    }

    @Override
    public final WindowClosingMode setDefaultCloseOperation(WindowClosingMode op) {
        synchronized (closingListenerLock) {
            WindowClosingMode _op = defaultCloseOperation;
            defaultCloseOperation = op;
            return _op;
        }
    }

    /**
     * Notifies the driver impl. that the instantiation is finished,
     * ie. instance created and all fields set.
     */
    private void instantiationFinished() {
        resetStateMask();
        instantiationFinishedImpl();
    }

    private void instantiationFinishedImpl() {
        // nop
    }

    /**
     * The native implementation must set the native windowHandle.<br>
     *
     * <p>
     * The implementation shall respect the states {@link #isAlwaysOnTop()}/{@link #FLAG_IS_ALWAYSONTOP} and
     * {@link #isUndecorated()}/{@link #FLAG_IS_UNDECORATED}, ie. the created window shall reflect those settings.
     * </p>
     *
     * <p>
     * The implementation should invoke the referenced java state callbacks
     * to notify this Java object of state changes.</p>
     *
     * @see #windowDestroyNotify(boolean)
     * @see #focusChanged(boolean, boolean)
     * @see #visibleChanged(boolean, boolean)
     * @see #sizeChanged(int, int)
     * @see #positionChanged(boolean, int, int)
     * @see #windowDestroyNotify(boolean)
     */
    protected abstract void createNativeImpl();

    protected abstract void closeNativeImpl();

    //----------------------------------------------------------------------
    // NativeWindow
    //

    // public final void destroy() - see below

    /**
     * Async request which shall be performed within {@link #TIMEOUT_NATIVEWINDOW}.
     * <p>
     * If if <code>force == false</code> the native implementation
     * may only request focus if not yet owner.</p>
     * <p>
     * {@link #focusChanged(boolean, boolean)} should be called
     * to notify about the focus traversal.
     * </p>
     *
     * @param force if true, bypass {@link #focusChanged(boolean, boolean)} and force focus request
     */
    protected abstract void requestFocusImpl(boolean force);

    /**
     * Returns the reconfigure state-mask supported by the implementation.
     * <p>
     * Default value is {@link #STATE_MASK_VISIBLE} | {@link #STATE_MASK_FOCUSED},
     * i.e. the <b>minimum requirement</b> for all implementations.
     * </p>
     *
     * @see #getSupportedStateMask()
     * @see #reconfigureWindowImpl(int, int, int, int, int)
     */
    protected abstract int getSupportedReconfigMaskImpl();

    /**
     * The native implementation should invoke the referenced java state callbacks
     * to notify this Java object of state changes.
     *
     * <p>
     * Implementations shall set x/y to 0, in case it's negative. This could happen due
     * to insets and positioning a decorated window to 0/0, which would place the frame
     * outside of the screen.</p>
     *
     * @param x      client-area position in window units, or <0 if unchanged
     * @param y      client-area position in window units, or <0 if unchanged
     * @param width  client-area size in window units, or <=0 if unchanged
     * @param height client-area size in window units, or <=0 if unchanged
     * @param flags  bitfield of change and status flags
     * @see #getSupportedReconfigMaskImpl()
     * @see #sizeChanged(int, int)
     * @see #positionChanged(boolean, int, int)
     */
    protected abstract boolean reconfigureWindowImpl(int x, int y, int width, int height, int flags);

    /**
     * Tests whether the given reconfigure state-mask is supported by implementation.
     */
    private boolean isReconfigureMaskSupported(int reconfigMask) {
        return reconfigMask == (reconfigMask & supportedReconfigStateMask);
    }

    //----------------------------------------------------------------------
    // Window
    //

    protected int getReconfigureMask(int changeFlags, boolean visible) {
        int smask = stateMask.get32(0, STATE_BIT_COUNT_ALL_RECONFIG);
        return changeFlags
                | (smask & ~(STATE_MASK_VISIBLE | STATE_MASK_UNDECORATED | STATE_MASK_CHILDWIN))
                | (visible ? STATE_MASK_VISIBLE : 0)
                | (isUndecorated(smask) ? STATE_MASK_UNDECORATED : 0)
                | (0 != getParentWindowHandle() ? STATE_MASK_CHILDWIN : 0)
                ;
    }

    protected void setTitleImpl(String title) {
    }

    /**
     * Translates the given window client-area coordinates with top-left origin
     * to screen coordinates in window units.
     * <p>
     * Since the position reflects the client area, it does not include the insets.
     * </p>
     * <p>
     * May return <code>null</code>, in which case the caller shall traverse through the NativeWindow tree
     * as demonstrated in {@link #getLocationOnScreen(Point)}.
     * </p>
     *
     * @return if not null, the screen location of the given coordinates
     */
    protected abstract Point getLocationOnScreenImpl(int x, int y);

    protected void warpPointerImpl(int x, int y) {
    }

    protected void setPointerIconImpl(PointerIconImpl pi) {
    }

    @Override
    public final int lockSurface() throws RuntimeException {
        RecursiveLock _wlock = windowLock;
        _wlock.lock();
        surfaceLockCount++;
        int res = (1 == surfaceLockCount) ? LOCK_SURFACE_NOT_READY : LOCK_SUCCESS; // new lock ?

        if (LOCK_SURFACE_NOT_READY == res) {
            try {
                if (isNativeValid()) {
                    AbstractGraphicsDevice adevice = getGraphicsConfiguration().getScreen().getDevice();
                    adevice.lock();
                    try {
                        res = lockSurfaceImpl();
                    } finally {
                        if (LOCK_SURFACE_NOT_READY >= res) {
                            adevice.unlock();
                        }
                    }
                }
            } finally {
                if (LOCK_SURFACE_NOT_READY >= res) {
                    surfaceLockCount--;
                    _wlock.unlock();
                }
            }
        }
        return res;
    }

    @Override
    public final void unlockSurface() {
        RecursiveLock _wlock = windowLock;
        _wlock.validateLocked();

        if (1 == surfaceLockCount) {
            AbstractGraphicsDevice adevice = getGraphicsConfiguration().getScreen().getDevice();
            try {
                unlockSurfaceImpl();
            } finally {
                adevice.unlock();
            }
        }
        surfaceLockCount--;
        _wlock.unlock();
    }

    @Override
    public final boolean isSurfaceLockedByOtherThread() {
        return windowLock.isLockedByOtherThread();
    }

    @Override
    public final Thread getSurfaceLockOwner() {
        return windowLock.getOwner();
    }

    public final RecursiveLock getLock() {
        return windowLock;
    }

    @Override
    public long getSurfaceHandle() {
        return windowHandle; // default: return window handle
    }

    @Override
    public boolean surfaceSwap() {
        return false;
    }

    @Override
    public final void addSurfaceUpdatedListener(SurfaceUpdatedListener l) {
        surfaceUpdatedHelper.addSurfaceUpdatedListener(l);
    }

    @Override
    public final void addSurfaceUpdatedListener(int index, SurfaceUpdatedListener l) throws IndexOutOfBoundsException {
        surfaceUpdatedHelper.addSurfaceUpdatedListener(index, l);
    }

    @Override
    public final void removeSurfaceUpdatedListener(SurfaceUpdatedListener l) {
        surfaceUpdatedHelper.removeSurfaceUpdatedListener(l);
    }

    @Override
    public final void surfaceUpdated(Object updater, NativeSurface ns, long when) {
        surfaceUpdatedHelper.surfaceUpdated(updater, ns, when);
    }

    @Override
    public final AbstractGraphicsConfiguration getGraphicsConfiguration() {
        return config.getNativeGraphicsConfiguration();
    }

    protected final void setGraphicsConfiguration(AbstractGraphicsConfiguration cfg) {
        config = cfg;
    }

    @Override
    public final long getDisplayHandle() {
        return config.getNativeGraphicsConfiguration().getScreen().getDevice().getHandle();
    }

    @Override
    public final int getScreenIndex() {
        return screen.getIndex();
    }

    @Override
    public final NativeSurface getNativeSurface() {
        return this;
    }

    @Override
    public final NativeWindow getParent() {
        return parentWindow;
    }

    @Override
    public final long getWindowHandle() {
        return windowHandle;
    }

    protected final void setWindowHandle(long handle) {
        windowHandle = handle;
    }

    @Override
    public Point getLocationOnScreen(Point storage) {
        if (isNativeValid()) {
            Point d;
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                d = getLocationOnScreenImpl(0, 0);
            } finally {
                _lock.unlock();
            }
            if (null != d) {
                if (null != storage) {
                    storage.translate(d.getX(), d.getY());
                    return storage;
                }
                return d;
            }
            // fall through intended ..
        }

        if (null != storage) {
            storage.translate(getX(), getY());
        } else {
            storage = new Point(getX(), getY());
        }
        if (null != parentWindow) {
            // traverse through parent list ..
            parentWindow.getLocationOnScreen(storage);
        }
        return storage;
    }

    @Override
    public final boolean isNativeValid() {
        return 0 != windowHandle;
    }

    @Override
    public final Screen getScreen() {
        return screen;
    }

    private void setScreen(ScreenImpl newScreen) { // never null !
        removeScreenReference();
        screen = newScreen;
    }

    @Override
    public final MonitorDevice getMainMonitor() {
        return screen.getMainMonitor(getBounds());
    }

    /**
     * @param visible
     * @param fast    {@code true} hints that the WM shall be skiped (no animation)
     * @param x       client-area position in window units, or <0 if unchanged
     * @param y       client-area position in window units, or <0 if unchanged
     * @param width   client-area size in window units, or <=0 if unchanged
     * @param height  client-area size in window units, or <=0 if unchanged
     */
    private void setVisibleImpl(boolean visible, boolean fast, int x, int y, int width, int height) {
        int mask;
        if (fast) {
            mask = getReconfigureMask(CHANGE_MASK_VISIBILITY | CHANGE_MASK_VISIBILITY_FAST, visible);
        } else {
            mask = getReconfigureMask(CHANGE_MASK_VISIBILITY, visible);
        }
        reconfigureWindowImpl(x, y, width, height, mask);
    }

    private void setVisibleActionImpl(boolean visible) {
        boolean nativeWindowCreated = false;
        int madeVisible = -1;

        RecursiveLock _lock = windowLock;
        _lock.lock();
        try {
            if (!visible && !childWindows.isEmpty()) {
                synchronized (childWindowsLock) {
                    for (NativeWindow nw : childWindows) {
                        if (nw instanceof WindowImpl) {
                            ((WindowImpl) nw).setVisible(false);
                        }
                    }
                }
            }
            if (!isNativeValid() && visible) {
                if (0 < getWidth() * getHeight()) {
                    nativeWindowCreated = createNative();
                    madeVisible = nativeWindowCreated ? 1 : -1;
                }
                // always flag visible, allowing a retry ..
                stateMask.set(STATE_BIT_VISIBLE);
            } else if (stateMask.get(STATE_BIT_VISIBLE) != visible) {
                if (isNativeValid()) {
                    // Skip WM if child-window!
                    boolean hasVisibilityQuirk = quirks.get(QUIRK_BIT_VISIBILITY);
                    setVisibleImpl(visible /* visible */, hasVisibilityQuirk || isChildWindow() /* fast */,
                            getX(), getY(), getWidth(), getHeight());
                    if (0 > WindowImpl.this.waitForVisible(visible, false)) {
                        if (!hasVisibilityQuirk) {
                            quirks.set(QUIRK_BIT_VISIBILITY);
                            if (DEBUG_IMPLEMENTATION) {
                                System.err.println("Setting VISIBILITY QUIRK, due to setVisible(" + visible + ") failure");
                            }
                            setVisibleImpl(visible /* visible */, true /* fast */,
                                    getX(), getY(), getWidth(), getHeight());
                            if (0 <= WindowImpl.this.waitForVisible(visible, false)) {
                                madeVisible = visible ? 1 : 0;
                            } // else: still not working .. bail out
                        } // else: no other remedy known .. bail out
                    } else {
                        madeVisible = visible ? 1 : 0;
                    }
                } else {
                    stateMask.set(STATE_BIT_VISIBLE);
                }
            }

            if (null != lifecycleHook) {
                lifecycleHook.setVisibleActionPost(visible, nativeWindowCreated);
            }

            if (isNativeValid() && visible && !childWindows.isEmpty()) {
                synchronized (childWindowsLock) {
                    for (NativeWindow nw : childWindows) {
                        if (nw instanceof WindowImpl) {
                            ((WindowImpl) nw).setVisible(true);
                        }
                    }
                }
            }
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window setVisible: END (" + getThreadName() + ") state " + getStateMaskString() +
                        ", nativeWindowCreated: " + nativeWindowCreated + ", madeVisible: " + madeVisible +
                        ", geom " + getX() + "/" + getY() + " " + getWidth() + "x" + getHeight() +
                        ", windowHandle " + toHexString(windowHandle));
            }
        } finally {
            if (null != lifecycleHook) {
                lifecycleHook.resetCounter();
            }
            _lock.unlock();
        }
        if (nativeWindowCreated || 1 == madeVisible) {
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
        }
    }

    @Override
    public final void setVisible(boolean wait, boolean visible) {
        if (!isReconfigureMaskSupported(STATE_MASK_VISIBLE) && isNativeValid()) {
            return;
        }
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window setVisible: START (" + getThreadName() + ") " + getX() + "/" + getY() + " " + getWidth() + "x" + getHeight() + ", windowHandle " + toHexString(windowHandle) + ", state " + getStateMaskString() + " -> visible " + visible + ", parentWindowHandle " + toHexString(parentWindowHandle) + ", parentWindow " + (null != parentWindow));
        }
        runOnEDTIfAvail(wait, new VisibleAction(visible));
    }

    private void setSize(int width, int height, boolean force) {
        runOnEDTIfAvail(true, new SetSizeAction(width, height, force));
    }

    @Override
    public final void setSize(int width, int height) {
        runOnEDTIfAvail(true, new SetSizeAction(width, height, false));
    }

    @Override
    public final void setSurfaceSize(int pixelWidth, int pixelHeight) {
        setSize(SurfaceScaleUtils.scaleInv(pixelWidth, getPixelScaleX()),
                SurfaceScaleUtils.scaleInv(pixelHeight, getPixelScaleY()));
    }

    @Override
    public final void setTopLevelSize(int width, int height) {
        InsetsImmutable insets = getInsets();
        setSize(width - insets.getTotalWidth(), height - insets.getTotalHeight());
    }

    @Override
    public void destroy() {
        stateMask.clear(STATE_BIT_VISIBLE); // Immediately mark synchronized visibility flag, avoiding possible recreation
        runOnEDTIfAvail(true, destroyAction);
    }

    private void destroy(boolean preserveResources) {
        if (null != lifecycleHook) {
            lifecycleHook.preserveGLStateAtDestroy(preserveResources);
        }
        destroy();
    }

    @Override
    public final ReparentOperation reparentWindow(NativeWindow newParent, int x, int y, int hints) {
        if (!isReconfigureMaskSupported(STATE_MASK_CHILDWIN) && isNativeValid()) {
            return ReparentOperation.ACTION_INVALID;
        }
        ReparentAction reparentAction = new ReparentAction(newParent, x, y, hints);
        runOnEDTIfAvail(true, reparentAction);
        return reparentAction.getOp();
    }

    @Override
    public final boolean isChildWindow() {
        return stateMask.get(STATE_BIT_CHILDWIN);
    }

    @Override
    public final CapabilitiesChooser setCapabilitiesChooser(CapabilitiesChooser chooser) {
        CapabilitiesChooser old = this.capabilitiesChooser;
        this.capabilitiesChooser = chooser;
        return old;
    }

    @Override
    public final CapabilitiesImmutable getChosenCapabilities() {
        return getGraphicsConfiguration().getChosenCapabilities();
    }

    @Override
    public final CapabilitiesImmutable getRequestedCapabilities() {
        return capsRequested;
    }

    @Override
    public final boolean isUndecorated() {
        return isUndecorated(getStateMask());
    }

    @Override
    public final void setUndecorated(boolean value) {
        if (isNativeValid()) {
            if (!isReconfigureMaskSupported(STATE_MASK_UNDECORATED)) {
                return;
            }
            if (isFullscreen()) {
                stateMaskNFS.put(STATE_MASK_UNDECORATED, value);
                return;
            }
        }
        runOnEDTIfAvail(true, new DecorationAction(value));
    }

    @Override
    public final boolean isAlwaysOnTop() {
        return stateMask.get(STATE_BIT_ALWAYSONTOP);
    }

    @Override
    public final void setAlwaysOnTop(boolean value) {
        if (isChildWindow()) {
            return; // ignore for child windows
        }
        if (isNativeValid()) {
            if (!isReconfigureMaskSupported(STATE_MASK_ALWAYSONTOP)) {
                return;
            }
            if (isFullscreen()) {
                if (value && isAlwaysOnBottom()) {
                    setAlwaysOnBottom(false);
                }
                stateMaskNFS.put(STATE_BIT_ALWAYSONTOP, value);
                return;
            }
        }
        if (value && isAlwaysOnBottom()) {
            setAlwaysOnBottom(false);
        }
        runOnEDTIfAvail(true, new AlwaysOnTopAction(value));
    }

    @Override
    public final boolean isAlwaysOnBottom() {
        return stateMask.get(STATE_BIT_ALWAYSONBOTTOM);
    }

    @Override
    public final void setAlwaysOnBottom(boolean value) {
        if (isChildWindow()) {
            return; // ignore for child windows
        }
        if (!isReconfigureMaskSupported(STATE_MASK_ALWAYSONBOTTOM) && isNativeValid()) {
            return;
        }
        if (value && isAlwaysOnTop()) {
            setAlwaysOnTop(false);
        }
        runOnEDTIfAvail(true, new AlwaysOnBottomAction(value));
    }

    @Override
    public final boolean isResizable() {
        return stateMask.get(STATE_BIT_RESIZABLE);
    }

    @Override
    public final void setResizable(boolean value) {
        if (isChildWindow()) {
            return; // ignore for child windows
        }
        if (isNativeValid()) {
            if (!isReconfigureMaskSupported(STATE_MASK_RESIZABLE)) {
                return;
            }
            if (isFullscreen()) {
                stateMaskNFS.put(STATE_BIT_RESIZABLE, value);
                return;
            }
        }
        runOnEDTIfAvail(true, new ResizableAction(value));
    }

    @Override
    public final boolean isSticky() {
        return stateMask.get(STATE_BIT_STICKY);
    }

    @Override
    public final void setSticky(boolean value) {
        if (isChildWindow()) {
            return; // ignore for child windows
        }
        if (!isReconfigureMaskSupported(STATE_MASK_STICKY) && isNativeValid()) {
            return;
        }
        runOnEDTIfAvail(true, new StickyAction(value));
    }

    @Override
    public final void setMaximized(boolean horz, boolean vert) {
        if (isNativeValid()) {
            if (horz && !isReconfigureMaskSupported(STATE_MASK_MAXIMIZED_HORZ)) {
                horz = false;
            }
            if (vert && !isReconfigureMaskSupported(STATE_MASK_MAXIMIZED_VERT)) {
                vert = false;
            }
        }
        if (isChildWindow()) {
            return; // ignore for child windows
        }
        if (isFullscreen()) {
            stateMaskNFS.put(STATE_BIT_MAXIMIZED_HORZ, horz);
            stateMaskNFS.put(STATE_BIT_MAXIMIZED_VERT, vert);
        } else {
            runOnEDTIfAvail(true, new MaximizeAction(horz, vert));
        }
    }

    @Override
    public final boolean isMaximizedVert() {
        return stateMask.get(STATE_BIT_MAXIMIZED_VERT);
    }

    @Override
    public final boolean isMaximizedHorz() {
        return stateMask.get(STATE_BIT_MAXIMIZED_HORZ);
    }

    /**
     * Triggered by implementation's WM events to update maximized window state.
     */
    private void maximizedChanged(boolean newMaxHorz, boolean newMaxVert) {
        if (!isFullscreen()) {
            String stateMask0 = DEBUG_IMPLEMENTATION ? getStateMaskString() : null;
            boolean changedHorz = stateMask.put(STATE_BIT_MAXIMIZED_HORZ, newMaxHorz) != newMaxHorz;
            boolean changedVert = stateMask.put(STATE_BIT_MAXIMIZED_VERT, newMaxVert) != newMaxVert;
            if (DEBUG_IMPLEMENTATION) {
                if (changedHorz || changedVert) {
                    System.err.println("Window.maximizedChanged.accepted: " + stateMask0 + " -> " + getStateMaskString());
                }
            }
        } else if (DEBUG_IMPLEMENTATION) {
            String stateMask0 = getStateMaskString();
            boolean changedHorz = stateMask.get(STATE_BIT_MAXIMIZED_HORZ) != newMaxHorz;
            boolean changedVert = stateMask.get(STATE_BIT_MAXIMIZED_VERT) != newMaxVert;
            if (changedHorz || changedVert) {
                System.err.println("Window.maximizedChanged.ignored: " + stateMask0 + " -> max[" + (newMaxHorz ? "" : "!") + "h, " + (newMaxVert ? "" : "!") + "v]");
            }
        }
    }

    /**
     * Manually calculate maximized and de-maximized position and size
     * not regarding a fixed taskbar etc.
     * <p>
     * Use only if:
     * <code>
     * 0 != ( ( CHANGE_MASK_MAXIMIZED_HORZ | CHANGE_MASK_MAXIMIZED_VERT ) & flags )
     * </code>
     * </p>
     *
     * @param flags
     * @param posSize
     */
    protected void reconfigMaximizedManual(int flags, int[] posSize, InsetsImmutable insets) {
        //if( 0 != ( ( CHANGE_MASK_MAXIMIZED_HORZ | CHANGE_MASK_MAXIMIZED_VERT ) & flags ) ) {
        MonitorMode mm = getMainMonitor().getCurrentMode();
        // FIXME HiDPI: Shortcut, may need to adjust if we change scaling methodology
        int mmWidth = SurfaceScaleUtils.scaleInv(mm.getRotatedWidth(), getPixelScaleX());
        int mmHeight = SurfaceScaleUtils.scaleInv(mm.getRotatedHeight(), getPixelScaleY());

        if (0 != (CHANGE_MASK_MAXIMIZED_HORZ & flags)) {
            if (0 != (STATE_MASK_MAXIMIZED_HORZ & flags)) {
                // max-h on
                normPosSizeStored[0] = true;
                normPosSize[0] = posSize[0];
                normPosSize[2] = posSize[2];
                posSize[0] = insets.getLeftWidth();
                posSize[2] = mmWidth - insets.getTotalWidth();
            } else {
                // max-h off
                normPosSizeStored[0] = false;
                posSize[0] = normPosSize[0];
                posSize[2] = normPosSize[2];
            }
        }
        if (0 != (CHANGE_MASK_MAXIMIZED_VERT & flags)) {
            if (0 != (STATE_MASK_MAXIMIZED_VERT & flags)) {
                // max-v on
                normPosSizeStored[1] = true;
                normPosSize[1] = posSize[1];
                normPosSize[3] = posSize[3];
                posSize[1] = insets.getTopHeight();
                posSize[3] = mmHeight - insets.getTotalHeight();
            } else {
                // max-v off
                normPosSizeStored[1] = false;
                posSize[1] = normPosSize[1];
                posSize[3] = normPosSize[3];
            }
        }
        //}
    }

    protected void resetMaximizedManual(int[] posSize) {
        if (normPosSizeStored[0]) {
            // max-h off
            normPosSizeStored[0] = false;
            posSize[0] = normPosSize[0];
            posSize[2] = normPosSize[2];
        }
        if (normPosSizeStored[1]) {
            // max-v off
            normPosSizeStored[1] = false;
            posSize[1] = normPosSize[1];
            posSize[3] = normPosSize[3];
        }
    }

    @Override
    public final String getTitle() {
        return title;
    }

    @Override
    public final void setTitle(String title) {
        if (title == null) {
            title = "";
        }
        this.title = title;
        if (0 != getWindowHandle()) {
            setTitleImpl(title);
        }
    }

    @Override
    public final boolean isPointerVisible() {
        return stateMask.get(STATE_BIT_POINTERVISIBLE);
    }

    @Override
    public void setPointerVisible(boolean pointerVisible) {
        if (!isReconfigureMaskSupported(STATE_MASK_POINTERVISIBLE) && isNativeValid()) {
            return;
        }
        if (stateMask.get(STATE_BIT_POINTERVISIBLE) != pointerVisible) {
            boolean setVal = 0 == getWindowHandle();
            if (!setVal) {
                setVal = setPointerVisibleIntern(pointerVisible);
            }
            if (setVal) {
                stateMask.put(STATE_BIT_POINTERVISIBLE, pointerVisible);
            }
        }
    }

    private boolean setPointerVisibleIntern(boolean pointerVisible) {
        boolean res = setOffscreenPointerVisible(pointerVisible, pointerIcon);
        return setPointerVisibleImpl(pointerVisible) || res; // accept onscreen or offscreen positive result!
    }

    /**
     * Helper method to delegate {@link #setPointerVisibleImpl(boolean)} to
     * {@link OffscreenLayerSurface#hideCursor()} or {@link OffscreenLayerSurface#setCursor(PixelRectangle, PointImmutable)}.
     * <p>
     * Note: JAWTWindow is an OffscreenLayerSurface.
     * </p>
     * <p>
     * Performing OffscreenLayerSurface's setCursor(..)/hideCursor(), if available,
     * gives same behavior on all platforms.
     * </p>
     * <p>
     * If visible, implementation invokes {@link #setOffscreenPointerIcon(OffscreenLayerSurface, PointerIconImpl)} using the
     * given <code>defaultPointerIcon</code>, otherwise {@link OffscreenLayerSurface#hideCursor()} is invoked.
     * </p>
     *
     * @param pointerVisible     true for visible, otherwise invisible.
     * @param defaultPointerIcon default PointerIcon for visibility
     * @param ols                the {@link OffscreenLayerSurface} instance, if null method does nothing.
     */
    private boolean setOffscreenPointerVisible(boolean pointerVisible, PointerIconImpl defaultPointerIcon) {
        if (pointerVisible) {
            return setOffscreenPointerIcon(defaultPointerIcon);
        } else {
            NativeWindow parent = getParent();
            if (parent instanceof final OffscreenLayerSurface ols) {
                try {
                    return ols.hideCursor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public final PointerIcon getPointerIcon() {
        return pointerIcon;
    }

    @Override
    public final void setPointerIcon(PointerIcon pi) {
        PointerIconImpl piImpl = (PointerIconImpl) pi;
        if (this.pointerIcon != piImpl) {
            if (isNativeValid()) {
                runOnEDTIfAvail(true, () -> setPointerIconIntern(piImpl));
            }
            this.pointerIcon = piImpl;
        }
    }

    private void setPointerIconIntern(PointerIconImpl pi) {
        setOffscreenPointerIcon(pi);
        setPointerIconImpl(pi);
    }

    /**
     * Helper method to delegate {@link #setPointerIconIntern(PointerIconImpl)} to
     * {@link OffscreenLayerSurface#setCursor(PixelRectangle, PointImmutable)}
     * <p>
     * Note: JAWTWindow is an OffscreenLayerSurface.
     * </p>
     * <p>
     * Performing OffscreenLayerSurface's setCursor(..), if available,
     * gives same behavior on all platforms.
     * </p>
     * <p>
     * Workaround for AWT/Windows bug within browser,
     * where the PointerIcon gets periodically overridden
     * by the AWT Component's icon.
     * </p>
     *
     * @param ols the {@link OffscreenLayerSurface} instance, if null method does nothing.
     * @param pi  the {@link PointerIconImpl} instance, if null PointerIcon gets reset.
     */
    private boolean setOffscreenPointerIcon(PointerIconImpl pi) {
        NativeWindow parent = getParent();
        if (parent instanceof final OffscreenLayerSurface ols) {
            try {
                if (null != pi) {
                    return ols.setCursor(pi, pi.getHotspot());
                } else {
                    return ols.setCursor(null, null); // default
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public final boolean isPointerConfined() {
        return stateMask.get(STATE_BIT_POINTERCONFINED);
    }

    @Override
    public final void confinePointer(boolean confine) {
        if (!isReconfigureMaskSupported(STATE_MASK_POINTERCONFINED) && isNativeValid()) {
            return;
        }
        if (stateMask.get(STATE_BIT_POINTERCONFINED) != confine) {
            boolean setVal = 0 == getWindowHandle();
            if (!setVal) {
                if (confine) {
                    requestFocus();
                    warpPointer(getSurfaceWidth() / 2, getSurfaceHeight() / 2);
                }
                setVal = confinePointerImpl(confine);
                if (confine) {
                    // give time to deliver mouse movements w/o confinement,
                    // this allows user listener to sync previous position value to the new centered position
                    try {
                        Thread.sleep(3 * screen.getDisplay().getEDTUtil().getPollPeriod());
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (setVal) {
                stateMask.put(STATE_BIT_POINTERCONFINED, confine);
            }
        }
    }

    @Override
    public final void warpPointer(int x, int y) {
        if (0 != getWindowHandle()) {
            warpPointerImpl(x, y);
        }
    }

    @Override
    public final InsetsImmutable getInsets() {
        return isUndecorated() ? Insets.getZero() : insets;
    }

    @Override
    public final int getX() {
        return x;
    }

    @Override
    public final int getY() {
        return y;
    }

    @Override
    public final int getWidth() {
        return winWidth;
    }

    @Override
    public final int getHeight() {
        return winHeight;
    }

    @Override
    public final Rectangle getBounds() {
        return new Rectangle(x, y, winWidth, winHeight);
    }

    @Override
    public final int getSurfaceWidth() {
        return pixWidth;
    }

    @Override
    public final int getSurfaceHeight() {
        return pixHeight;
    }

    @Override
    public final int[] convertToWindowUnits(int[] pixelUnitsAndResult) {
        return SurfaceScaleUtils.scaleInv(pixelUnitsAndResult, pixelUnitsAndResult, hasPixelScale);
    }

    @Override
    public final int[] convertToPixelUnits(int[] windowUnitsAndResult) {
        return SurfaceScaleUtils.scale(windowUnitsAndResult, windowUnitsAndResult, hasPixelScale);
    }

    protected final Point convertToWindowUnits(Point pixelUnitsAndResult) {
        return pixelUnitsAndResult.scaleInv(getPixelScaleX(), getPixelScaleY());
    }

    protected final Point convertToPixelUnits(Point windowUnitsAndResult) {
        return windowUnitsAndResult.scale(getPixelScaleX(), getPixelScaleY());
    }

    /**
     * HiDPI: We currently base scaling of window units to pixel units on an integer scale factor per component.
     */
    private float getPixelScaleX() {
        return hasPixelScale[0];
    }

    /**
     * HiDPI: We currently base scaling of window units to pixel units on an integer scale factor per component.
     */
    private float getPixelScaleY() {
        return hasPixelScale[1];
    }

    @Override
    public boolean setSurfaceScale(float[] pixelScale) {
        System.arraycopy(pixelScale, 0, reqPixelScale, 0, 2);
        return false;
    }

    @Override
    public final float[] getRequestedSurfaceScale(float[] result) {
        System.arraycopy(reqPixelScale, 0, result, 0, 2);
        return result;
    }

    //----------------------------------------------------------------------
    // Window
    //

    @Override
    public final float[] getCurrentSurfaceScale(float[] result) {
        System.arraycopy(hasPixelScale, 0, result, 0, 2);
        return result;
    }

    //----------------------------------------------------------------------
    // WindowImpl
    //

    @Override
    public final float[] getMinimumSurfaceScale(float[] result) {
        System.arraycopy(minPixelScale, 0, result, 0, 2);
        return result;
    }

    @Override
    public final float[] getMaximumSurfaceScale(float[] result) {
        System.arraycopy(maxPixelScale, 0, result, 0, 2);
        return result;
    }

    @Override
    public final float[] getPixelsPerMM(float[] ppmmStore) {
        getMainMonitor().getPixelsPerMM(ppmmStore);
        ppmmStore[0] *= hasPixelScale[0] / maxPixelScale[0];
        ppmmStore[1] *= hasPixelScale[1] / maxPixelScale[1];
        return ppmmStore;
    }

    protected final boolean autoPosition() {
        return stateMask.get(STATE_BIT_AUTOPOSITION);
    }

    /**
     * Sets the position fields {@link #x} and {@link #y} in window units to the given values and {@link #autoPosition} to false.
     */
    private void definePosition(int x, int y) {
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("definePosition: " + this.x + "/" + this.y + " -> " + x + "/" + y);
            // ExceptionUtils.dumpStackTrace(System.err);
        }
        stateMask.clear(STATE_BIT_AUTOPOSITION);
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the size fields {@link #winWidth} and {@link #winHeight} in window units to the given values
     * and {@link #pixWidth} and {@link #pixHeight} in pixel units according to {@link #convertToPixelUnits(int[])}.
     */
    private void defineSize(int winWidth, int winHeight) {
        // FIXME HiDPI: Shortcut, may need to adjust if we change scaling methodology
        int pixWidth = SurfaceScaleUtils.scale(winWidth, getPixelScaleX());
        int pixHeight = SurfaceScaleUtils.scale(winHeight, getPixelScaleY());

        if (DEBUG_IMPLEMENTATION) {
            System.err.println("defineSize: win[" + this.winWidth + "x" + this.winHeight + " -> " + winWidth + "x" + winHeight +
                    "], pixel[" + this.pixWidth + "x" + this.pixHeight + " -> " + pixWidth + "x" + pixHeight + "]");
            // ExceptionUtils.dumpStackTrace(System.err);
        }
        this.winWidth = winWidth;
        this.winHeight = winHeight;
        this.pixWidth = pixWidth;
        this.pixHeight = pixHeight;
    }

    @Override
    public final boolean isVisible() {
        return stateMask.get(STATE_BIT_VISIBLE);
    }

    @Override
    public final void setVisible(boolean visible) {
        setVisible(true, visible);
    }

    @Override
    public final boolean isFullscreen() {
        return stateMask.get(STATE_BIT_FULLSCREEN);
    }

    @Override
    public final Window getDelegatedWindow() {
        return this;
    }

    /**
     * If the implementation is capable of detecting a device change
     * return true and clear the status/reason of the change.
     */
    public boolean hasDeviceChanged() {
        return false;
    }

    public final LifecycleHook getLifecycleHook() {
        return lifecycleHook;
    }

    public final LifecycleHook setLifecycleHook(LifecycleHook hook) {
        LifecycleHook old = lifecycleHook;
        lifecycleHook = hook;
        return old;
    }

    /**
     * If this Window actually wraps a {@link NativeSurface} from another instance or toolkit,
     * it will return such reference. Otherwise returns null.
     */
    @SuppressWarnings("WeakerAccess")
    public NativeSurface getWrappedSurface() {
        return null;
    }

    @Override
    public final void setWindowDestroyNotifyAction(Runnable r) {
        windowDestroyNotifyAction = r;
    }

    protected final long getParentWindowHandle() {
        return isFullscreen() ? 0 : parentWindowHandle;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getName()).append("[State ").append(getStateMaskString()).append(",\n ").append(screen).append(",\n window[").append(getX()).append("/").append(getY()).append(" ").append(getWidth()).append("x").append(getHeight()).append(" wu, ").append(getSurfaceWidth()).append("x").append(getSurfaceHeight()).append(" pixel]").append(",\n Config ").append(config).append(",\n ParentWindow ").append(parentWindow).append(",\n ParentWindowHandle ").append(toHexString(parentWindowHandle)).append(" (").append(0 != getParentWindowHandle()).append(")").append(",\n WindowHandle ").append(toHexString(getWindowHandle())).append(",\n SurfaceHandle ").append(toHexString(getSurfaceHandle())).append(" (lockedExt window ").append(windowLock.isLockedByOtherThread()).append(", surface ").append(isSurfaceLockedByOtherThread()).append(")").append(",\n WrappedSurface ").append(getWrappedSurface()).append(",\n ChildWindows ").append(childWindows.size());

        sb.append(", SurfaceUpdatedListeners num ").append(surfaceUpdatedHelper.size()).append(" [");
        for (int i = 0; i < surfaceUpdatedHelper.size(); i++) {
            sb.append(surfaceUpdatedHelper.get(i)).append(", ");
        }
        sb.append("], WindowListeners num ").append(windowListeners.size()).append(" [");
        for (WindowListener windowListener : windowListeners) {
            sb.append(windowListener).append(", ");
        }
        sb.append("], MouseListeners num ").append(mouseListeners.size()).append(" [");
        for (MouseListener mouseListener : mouseListeners) {
            sb.append(mouseListener).append(", ");
        }
        sb.append("], PointerGestures default ").append(defaultGestureHandlerEnabled).append(", custom ").append(pointerGestureHandler.size()).append(" [");
        for (GestureHandler gestureHandler : pointerGestureHandler) {
            sb.append(gestureHandler).append(", ");
        }
        sb.append("], KeyListeners num ").append(keyListeners.size()).append(" [");
        for (KeyListener keyListener : keyListeners) {
            sb.append(keyListener).append(", ");
        }
        sb.append("], windowLock ").append(windowLock).append(", surfaceLockCount ").append(surfaceLockCount).append("]");
        return sb.toString();
    }

    @Override
    public final void runOnEDTIfAvail(boolean wait, Runnable task) {
        if (windowLock.isOwner(Thread.currentThread())) {
            task.run();
        } else {
            ((DisplayImpl) screen.getDisplay()).runOnEDTIfAvail(wait, task);
        }
    }

    @Override
    public final boolean hasFocus() {
        return stateMask.get(STATE_BIT_FOCUSED);
    }

    @Override
    public final void requestFocus() {
        requestFocus(true);
    }

    @Override
    public final void requestFocus(boolean wait) {
        requestFocus(wait /* wait */, false /* skipFocusAction */, stateMask.get(PSTATE_BIT_FOCUS_CHANGE_BROKEN) /* force */);
    }

    private void requestFocus(boolean wait, boolean skipFocusAction, boolean force) {
        if (isNativeValid() &&
                (force || !hasFocus()) &&
                (skipFocusAction || !focusAction())) {
            runOnEDTIfAvail(wait, force ? requestFocusActionForced : requestFocusAction);
        }
    }

    /**
     * Internally forcing request focus on current thread
     */
    private void requestFocusInt(boolean skipFocusAction) {
        if (skipFocusAction || !focusAction()) {
            if (!isReconfigureMaskSupported(STATE_MASK_FOCUSED)) {
                return;
            }
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.RequestFocusInt: forcing - (" + getThreadName() + "): skipFocusAction " +
                        skipFocusAction + ", state " + getStateMaskString() + " -> focus true - windowHandle " +
                        toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle));
            }
            requestFocusImpl(true);
        }
    }

    @Override
    public final void setFocusAction(FocusRunnable focusAction) {
        this.focusAction = focusAction;
    }

    private boolean focusAction() {
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window.focusAction() START - " + getThreadName() + ", focusAction: " + focusAction + " - windowHandle " + toHexString(getWindowHandle()));
        }
        boolean res;
        if (null != focusAction) {
            res = focusAction.run();
        } else {
            res = false;
        }
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window.focusAction() END - " + getThreadName() + ", focusAction: " + focusAction + " - windowHandle " + toHexString(getWindowHandle()) + ", res: " + res);
        }
        return res;
    }

    protected final void setBrokenFocusChange(boolean v) {
        stateMask.put(PSTATE_BIT_FOCUS_CHANGE_BROKEN, v);
    }

    @Override
    public final void setKeyboardFocusHandler(KeyListener l) {
        keyboardFocusHandler = l;
    }

    @Override
    public void setPosition(int x, int y) {
        stateMask.clear(STATE_BIT_AUTOPOSITION);
        runOnEDTIfAvail(true, new SetPositionAction(x, y));
    }

    @Override
    public final void setTopLevelPosition(int x, int y) {
        InsetsImmutable insets = getInsets();
        setPosition(x + insets.getLeftWidth(), y + insets.getTopHeight());
    }

    @Override
    public boolean setFullscreen(boolean fullscreen) {
        return setFullscreenImpl(fullscreen, true, null);
    }

    @Override
    public boolean setFullscreen(List<MonitorDevice> monitors) {
        return setFullscreenImpl(true, false, monitors);
    }


    //----------------------------------------------------------------------
    // Child Window Management
    //

    private boolean setFullscreenImpl(boolean fullscreen, boolean useMainMonitor, List<MonitorDevice> monitors) {
        synchronized (fullScreenAction) {
            fullscreenMonitors = monitors;

            stateMask.put(PSTATE_BIT_FULLSCREEN_MAINMONITOR, useMainMonitor);
            if (fullScreenAction.init(fullscreen)) {
                if (fullScreenAction.fsOn() && isOffscreenInstance(WindowImpl.this, parentWindow)) {
                    // enable fullscreen on offscreen instance
                    if (null != parentWindow) {
                        nfs_parent = parentWindow;
                        reparentWindow(null, -1, -1, REPARENT_HINT_FORCE_RECREATION | REPARENT_HINT_BECOMES_VISIBLE);
                    } else {
                        throw new InternalError("Offscreen instance w/o parent unhandled");
                    }
                }

                runOnEDTIfAvail(true, fullScreenAction);

                if (!fullScreenAction.fsOn() && null != nfs_parent) {
                    // disable fullscreen on offscreen instance
                    reparentWindow(nfs_parent, -1, -1, REPARENT_HINT_FORCE_RECREATION | REPARENT_HINT_BECOMES_VISIBLE);
                    nfs_parent = null;
                }
            }
            return stateMask.get(STATE_BIT_FULLSCREEN);
        }
    }

    /**
     * Notify WindowDriver about the finished monitor mode change.
     */
    private void monitorModeChanged(MonitorEvent me, boolean success) {
    }

    @Override
    public final boolean removeChild(NativeWindow win) {
        synchronized (childWindowsLock) {
            return childWindows.remove(win);
        }
    }

    @Override
    public final boolean addChild(NativeWindow win) {
        if (win == null) {
            return false;
        }
        synchronized (childWindowsLock) {
            return childWindows.add(win);
        }
    }

    //----------------------------------------------------------------------
    // Generic Event Support
    //
    private void doEvent(boolean enqueue, boolean wait, NEWTEvent event) {
        boolean done = false;

        if (!enqueue) {
            done = consumeEvent(event);
            wait = done; // don't wait if event can't be consumed now
        }

        if (!done) {
            enqueueEvent(wait, event);
        }
    }

    @Override
    public final void enqueueEvent(boolean wait, NEWTEvent event) {
        if (isNativeValid()) {
            ((DisplayImpl) screen.getDisplay()).enqueueEvent(wait, event);
        }
    }

    //
    // MouseListener/Event Support
    //

    //
    // Native MouseEvents pre-processed to be enqueued or consumed directly
    //

    @Override
    public final boolean consumeEvent(NEWTEvent e) {
        switch (e.getEventType()) {
            // special repaint treatment
            case WindowEvent.EVENT_WINDOW_REPAINT:
                // queue repaint event in case window is locked, ie in operation
                if (windowLock.isLockedByOtherThread()) {
                    // make sure only one repaint event is queued
                    if (!repaintQueued) {
                        repaintQueued = true;
                        boolean discardTO = QUEUED_EVENT_TO <= NOW() - e.getWhen();
//                        if(DEBUG_IMPLEMENTATION) {
//                            System.err.println("Window.consumeEvent: REPAINT [me "+Thread.currentThread().getName()+", owner "+windowLock.getOwner()+"] - queued "+e+", discard-to "+discardTO);
//                            // ExceptionUtils.dumpStackTrace(System.err);
//                        }
                        return discardTO; // discardTO:=true -> consumed
                    }
                    return true;
                }
                repaintQueued = false; // no repaint event queued
                break;

            // common treatment
            case WindowEvent.EVENT_WINDOW_RESIZED:
                // queue event in case window is locked, ie in operation
                if (windowLock.isLockedByOtherThread()) {
                    boolean discardTO = QUEUED_EVENT_TO <= NOW() - e.getWhen();
//                    if(DEBUG_IMPLEMENTATION) {
//                        System.err.println("Window.consumeEvent: RESIZED [me "+Thread.currentThread().getName()+", owner "+windowLock.getOwner()+"] - queued "+e+", discard-to "+discardTO);
//                        // ExceptionUtils.dumpStackTrace(System.err);
//                    }
                    return discardTO; // discardTO:=true -> consumed
                }
                break;
            default:
                break;
        }
        if (e instanceof WindowEvent) {
            consumeWindowEvent((WindowEvent) e);
        } else if (e instanceof KeyEvent) {
            consumeKeyEvent((KeyEvent) e);
        } else if (e instanceof MouseEvent) {
            consumePointerEvent((MouseEvent) e);
        } else {
            throw new NativeWindowException("Unexpected NEWTEvent type " + e);
        }
        return true;
    }

    private void sendMouseEvent(short eventType, int modifiers,
                                int x, int y, short button, float rotation) {
        doMouseEvent(false, false, eventType, modifiers, x, y, button, MouseEvent.getRotationXYZ(rotation, modifiers), 1.0f);
    }

    private void enqueueMouseEvent(boolean wait, short eventType, int modifiers,
                                   int x, int y, short button, float rotation) {
        doMouseEvent(true, wait, eventType, modifiers, x, y, button, MouseEvent.getRotationXYZ(rotation, modifiers), 1.0f);
    }
    /**
     public final void sendMouseEvent(final short eventType, final int modifiers,
     final int x, final int y, final short button, final float[] rotationXYZ, final float rotationScale) {
     doMouseEvent(false, false, eventType, modifiers, x, y, button, rotationXYZ, rotationScale);
     }
     public final void enqueueMouseEvent(final boolean wait, final short eventType, final int modifiers,
     final int x, final int y, final short button, final float[] rotationXYZ, final float rotationScale) {
     doMouseEvent(true, wait, eventType, modifiers, x, y, button, rotationXYZ, rotationScale);
     } */

    protected final void doMouseEvent(boolean enqueue, boolean wait, short eventType, int modifiers,
                                      int x, int y, short button, float rotation) {
        doMouseEvent(enqueue, wait, eventType, modifiers, x, y, button, MouseEvent.getRotationXYZ(rotation, modifiers), 1.0f);
    }

    /**
     * Send mouse event (one-pointer) either to be directly consumed or to be enqueued
     *
     * @param enqueue if true, event will be {@link #enqueueEvent(boolean, NEWTEvent) enqueued},
     *                otherwise {@link #consumeEvent(NEWTEvent) consumed} directly.
     * @param wait    if true wait until {@link #consumeEvent(NEWTEvent) consumed}.
     */
    protected void doMouseEvent(boolean enqueue, boolean wait, short eventType, int modifiers,
                                int x, int y, short button, float[] rotationXYZ, float rotationScale) {
        if (0 > button || button > MouseEvent.BUTTON_COUNT) {
            throw new NativeWindowException("Invalid mouse button number" + button);
        }
        doPointerEvent(enqueue, wait, constMousePointerTypes, eventType, modifiers,
                0 /*actionIdx*/, new short[]{(short) 0}, button,
                new int[]{x}, new int[]{y}, new float[]{0.0f} /*pressure*/,
                1.0f /*maxPressure*/, rotationXYZ, rotationScale);
    }

    /**
     * Send multiple-pointer event either to be directly consumed or to be enqueued
     * <p>
     * The index for the element of multiple-pointer arrays represents the pointer which triggered the event
     * is passed via <i>actionIdx</i>.
     * </p>
     * <p>
     * The given pointer names, <code>pNames</code>, are mapped to consecutive pointer IDs starting w/ 0
     * using a hash-map if <code>normalPNames</code> is <code>false</code>.
     * Otherwise a simple <code>int</code> to <code>short</code> type cast is performed.
     * </p>
     * <p>
     * See {@link #doPointerEvent(boolean, boolean, PointerType[], short, int, int, short[], short, int[], int[], float[], float, float[], float)}
     * for details!
     * </p>
     *
     * @param enqueue      if true, event will be {@link #enqueueEvent(boolean, NEWTEvent) enqueued},
     *                     otherwise {@link #consumeEvent(NEWTEvent) consumed} directly.
     * @param wait         if true wait until {@link #consumeEvent(NEWTEvent) consumed}.
     * @param pTypes       {@link MouseEvent.PointerType} for each pointer (multiple pointer)
     * @param eventType
     * @param modifiers
     * @param actionIdx    index of multiple-pointer arrays representing the pointer which triggered the event
     * @param normalPNames see pName below.
     * @param pNames       Pointer name for each pointer (multiple pointer).
     *                     We assume consecutive pointer names starting w/ 0 if <code>normalPIDs</code> is <code>true</code>.
     *                     Otherwise we hash-map the values during state pressed to retrieve the normal ID.
     * @param pX           X-axis for each pointer (multiple pointer)
     * @param pY           Y-axis for each pointer (multiple pointer)
     * @param pPressure    Pressure for each pointer (multiple pointer)
     * @param maxPressure  Maximum pointer pressure for all pointer
     */
    public final void doPointerEvent(boolean enqueue, boolean wait,
                                     PointerType[] pTypes, short eventType, int modifiers,
                                     int actionIdx, boolean normalPNames, int[] pNames,
                                     int[] pX, int[] pY, float[] pPressure,
                                     float maxPressure, float[] rotationXYZ, float rotationScale) {
        int pCount = pNames.length;
        short[] pIDs = new short[pCount];
        for (int i = 0; i < pCount; i++) {
            if (!normalPNames) {
                // hash map int name -> short idx
                int sz0 = pName2pID.size();
                Integer pNameI1 = pName2pID.getOrAdd(pNames[i]);
                short pID = (short) pName2pID.indexOf(pNameI1);
                pIDs[i] = pID;
                if (DEBUG_MOUSE_EVENT) {
                    int sz1 = pName2pID.size();
                    if (sz0 != sz1) {
                        System.err.println("PointerName2ID[sz " + sz1 + "]: Map " + pNameI1 + " == " + pID);
                    }
                }
                if (MouseEvent.EVENT_MOUSE_RELEASED == eventType) {
                    pName2pID.remove(pNameI1);
                    if (DEBUG_MOUSE_EVENT) {
                        System.err.println("PointerName2ID[sz " + pName2pID.size() + "]: Unmap " + pNameI1 + " == " + pID);
                    }
                }
            } else {
                // simple type cast
                pIDs[i] = (short) pNames[i];
            }
        }
        short button = 0 < pCount ? (short) (pIDs[0] + 1) : (short) 0;
        doPointerEvent(enqueue, wait, pTypes, eventType, modifiers, actionIdx, pIDs, button,
                pX, pY, pPressure, maxPressure, rotationXYZ, rotationScale);
    }

    /**
     * Send multiple-pointer event either to be directly consumed or to be enqueued.
     * <p>
     * Pointer/Mouse Processing Pass 1 (Pass 2 is performed in {@link #consumePointerEvent(MouseEvent)}.
     * </p>
     * <p>
     * Usually directly called by event source to enqueue and process event.
     * </p>
     * <p>
     * The index for the element of multiple-pointer arrays represents the pointer which triggered the event
     * is passed via <i>actionIdx</i>.
     * </p>
     * <p>
     * <ul>
     * <li>Determine ENTERED/EXITED state</li>
     * <li>Remove redundant move/drag events</li>
     * <li>Reset states if applicable</li>
     * <li>Drop exterior events</li>
     * <li>Determine CLICK COUNT</li>
     * <li>Ignore sent CLICKED</li>
     * <li>Track buttonPressed incl. buttonPressedMask</li>
     * <li>Synthesize DRAGGED event (from MOVED if pointer is pressed)</li>
     * </ul>
     * </p>
     *
     * @param enqueue     if true, event will be {@link #enqueueEvent(boolean, NEWTEvent) enqueued},
     *                    otherwise {@link #consumeEvent(NEWTEvent) consumed} directly.
     * @param wait        if true wait until {@link #consumeEvent(NEWTEvent) consumed}.
     * @param pTypes      {@link MouseEvent.PointerType} for each pointer (multiple pointer)
     * @param eventType
     * @param modifiers
     * @param pActionIdx  index of multiple-pointer arrays representing the pointer which triggered the event
     * @param pID         Pointer ID for each pointer (multiple pointer). We assume consecutive pointerIDs starting w/ 0.
     * @param button      Corresponding mouse-button, a button of 0 denotes no activity, i.e. {@link PointerType#Mouse} move.
     * @param pX          X-axis for each pointer (multiple pointer)
     * @param pY          Y-axis for each pointer (multiple pointer)
     * @param pPressure   Pressure for each pointer (multiple pointer)
     * @param maxPressure Maximum pointer pressure for all pointer
     */
    private void doPointerEvent(boolean enqueue, boolean wait,
                                PointerType[] pTypes, short eventType, int modifiers,
                                int pActionIdx, short[] pID, short buttonIn, int[] pX, int[] pY,
                                float[] pPressure, float maxPressure, float[] rotationXYZ, float rotationScale) {
        long when = NOW();
        int pCount = pTypes.length;

        if (0 > pActionIdx || pActionIdx >= pCount) {
            throw new IllegalArgumentException("actionIdx out of bounds [0.." + (pCount - 1) + "]");
        }
        if (0 < pActionIdx) {
            // swap values to make idx 0 the triggering pointer
            {
                PointerType aType = pTypes[pActionIdx];
                pTypes[pActionIdx] = pTypes[0];
                pTypes[0] = aType;
            }
            {
                short s = pID[pActionIdx];
                pID[pActionIdx] = pID[0];
                pID[0] = s;
            }
            {
                {
                    int s = pX[pActionIdx];
                    pX[pActionIdx] = pX[0];
                    pX[0] = s;
                }
                {
                    int s = pY[pActionIdx];
                    pY[pActionIdx] = pY[0];
                    pY[0] = s;
                }
            }
            {
                float aPress = pPressure[pActionIdx];
                pPressure[pActionIdx] = pPressure[0];
                pPressure[0] = aPress;
            }
        }
        short button;
        {
            // validate button
            if (0 <= buttonIn && buttonIn <= MouseEvent.BUTTON_COUNT) { // we allow button==0 for no button, i.e. mouse-ptr move
                button = buttonIn;
            } else {
                button = MouseEvent.BUTTON1;
            }
        }

        //
        // - Determine ENTERED/EXITED state
        // - Remove redundant move/drag events
        // - Reset states if applicable
        //
        int x = pX[0];
        int y = pY[0];
        boolean insideSurface = x >= 0 && y >= 0 && x < getSurfaceWidth() && y < getSurfaceHeight();
        Point movePositionP0 = pState1.getMovePosition(pID[0]);
        switch (eventType) {
            case MouseEvent.EVENT_MOUSE_EXITED:
                if (pState1.dragging) {
                    // Drop mouse EXIT if dragging, i.e. due to exterior dragging outside of window.
                    // NOTE-1: X11 produces the 'premature' EXIT, however it also produces 'EXIT' after exterior dragging!
                    // NOTE-2: consumePointerEvent(MouseEvent) will synthesize a missing EXIT event!
                    if (DEBUG_MOUSE_EVENT) {
                        System.err.println("doPointerEvent: drop " + MouseEvent.getEventTypeString(eventType) + " due to dragging: " + pState1);
                    }
                    return;
                }
                if (null != movePositionP0) {
                    if (x == -1 && y == -1) {
                        x = movePositionP0.getX();
                        y = movePositionP0.getY();
                    }
                    movePositionP0.set(0, 0);
                }
                // Fall through intended!

            case MouseEvent.EVENT_MOUSE_ENTERED:
                if (eventType == MouseEvent.EVENT_MOUSE_ENTERED) {
                    pState1.insideSurface = true;
                    pState1.exitSent = false;
                } else {
                    pState1.insideSurface = false;
                    pState1.exitSent = true;
                }
                pState1.clearButton();
                if (pTypes[0] != PointerType.Mouse) {
                    // Drop !MOUSE ENTER/EXIT Events - Safeguard for non compliant implementations only.
                    if (DEBUG_MOUSE_EVENT) {
                        System.err.println("doPointerEvent: drop " + MouseEvent.getEventTypeString(eventType) + " due to !Mouse but " + pTypes[0] + ": " + pState1);
                    }
                    return;
                }
                // clip coordinates to window dimension
                x = Math.min(Math.max(x, 0), getSurfaceWidth() - 1);
                y = Math.min(Math.max(y, 0), getSurfaceHeight() - 1);
                break;

            case MouseEvent.EVENT_MOUSE_MOVED:
            case MouseEvent.EVENT_MOUSE_DRAGGED:
                if (null != movePositionP0) {
                    if (movePositionP0.getX() == x && movePositionP0.getY() == y) {
                        // Drop same position
                        if (DEBUG_MOUSE_EVENT) {
                            System.err.println("doPointerEvent: drop " + MouseEvent.getEventTypeString(eventType) + " w/ same position: " + movePositionP0 + ", " + pState1);
                        }
                        return;
                    }
                    movePositionP0.set(x, y);
                }

                // Fall through intended !

            default:
                if (pState1.insideSurface != insideSurface) {
                    // ENTER/EXIT!
                    pState1.insideSurface = insideSurface;
                    if (insideSurface) {
                        pState1.exitSent = false;
                    }
                    pState1.clearButton();
                }
        }

        //
        // Drop exterior events if not dragging pointer and not EXIT event
        // Safeguard for non compliant implementations!
        //
        if (!pState1.dragging && !insideSurface && MouseEvent.EVENT_MOUSE_EXITED != eventType) {
            if (DEBUG_MOUSE_EVENT) {
                System.err.println("doPointerEvent: drop: " + MouseEvent.getEventTypeString(eventType) + ", mod " + modifiers + ", pos " + x + "/" + y + ", button " + button + ", lastMousePosition: " + movePositionP0 + ", insideWindow " + ", " + pState1);
            }
            return; // .. invalid ..
        }
        if (DEBUG_MOUSE_EVENT) {
            System.err.println("doPointerEvent: enqueue " + enqueue + ", wait " + wait + ", " + MouseEvent.getEventTypeString(eventType) +
                    ", mod " + modifiers + ", pos " + x + "/" + y + ", button " + button + ", lastMousePosition: " + movePositionP0 + ", " + pState1);
        }

        int buttonMask = InputEvent.getButtonMask(button);
        modifiers |= buttonMask; // Always add current button to modifier mask (Bug 571)
        modifiers |= pState1.buttonPressedMask; // Always add currently pressed mouse buttons to modifier mask

        if (isPointerConfined()) {
            modifiers |= InputEvent.CONFINED_MASK;
        }
        if (!isPointerVisible()) {
            modifiers |= InputEvent.INVISIBLE_MASK;
        }

        pX[0] = x;
        pY[0] = y;

        //
        // - Determine CLICK COUNT
        // - Ignore sent CLICKED
        // - Track buttonPressed incl. buttonPressedMask
        // - Synthesize DRAGGED event (from MOVED if pointer is pressed)
        //
        MouseEvent e;
        switch (eventType) {
            case MouseEvent.EVENT_MOUSE_CLICKED:
                // swallow CLICK event
                return;

            case MouseEvent.EVENT_MOUSE_PRESSED:
                if (0 >= pPressure[0]) {
                    pPressure[0] = maxPressure;
                }
                pState1.buttonPressedMask |= buttonMask;
                if (1 == pCount) {
                    if (when - pState1.lastButtonPressTime < MouseEvent.getClickTimeout()) {
                        pState1.lastButtonClickCount++;
                    } else {
                        pState1.lastButtonClickCount = 1;
                    }
                    pState1.lastButtonPressTime = when;
                    pState1.buttonPressed = button;
                    e = new MouseEvent(eventType, this, when, modifiers, pTypes, pID,
                            pX, pY, pPressure, maxPressure, button, pState1.lastButtonClickCount, rotationXYZ, rotationScale);
                } else {
                    e = new MouseEvent(eventType, this, when, modifiers, pTypes, pID,
                            pX, pY, pPressure, maxPressure, button, (short) 1, rotationXYZ, rotationScale);
                }
                break;
            case MouseEvent.EVENT_MOUSE_RELEASED:
                pState1.buttonPressedMask &= ~buttonMask;
                if (1 == pCount) {
                    e = new MouseEvent(eventType, this, when, modifiers, pTypes, pID,
                            pX, pY, pPressure, maxPressure, button, pState1.lastButtonClickCount, rotationXYZ, rotationScale);
                    if (when - pState1.lastButtonPressTime >= MouseEvent.getClickTimeout()) {
                        pState1.lastButtonClickCount = 0;
                        pState1.lastButtonPressTime = 0;
                    }
                    pState1.buttonPressed = 0;
                    pState1.dragging = false;
                } else {
                    e = new MouseEvent(eventType, this, when, modifiers, pTypes, pID,
                            pX, pY, pPressure, maxPressure, button, (short) 1, rotationXYZ, rotationScale);
                    if (0 == pState1.buttonPressedMask) {
                        pState1.clearButton();
                    }
                }
                if (null != movePositionP0) {
                    movePositionP0.set(0, 0);
                }
                break;
            case MouseEvent.EVENT_MOUSE_MOVED:
                if (0 != pState1.buttonPressedMask) { // any button or pointer move -> drag
                    e = new MouseEvent(MouseEvent.EVENT_MOUSE_DRAGGED, this, when, modifiers, pTypes, pID,
                            pX, pY, pPressure, maxPressure, pState1.buttonPressed, (short) 1, rotationXYZ, rotationScale);
                    pState1.dragging = true;
                } else {
                    e = new MouseEvent(eventType, this, when, modifiers, pTypes, pID,
                            pX, pY, pPressure, maxPressure, button, (short) 0, rotationXYZ, rotationScale);
                }
                break;
            case MouseEvent.EVENT_MOUSE_DRAGGED:
                if (0 >= pPressure[0]) {
                    pPressure[0] = maxPressure;
                }
                pState1.dragging = true;
                // Fall through intended!
            default:
                e = new MouseEvent(eventType, this, when, modifiers, pTypes, pID,
                        pX, pY, pPressure, maxPressure, button, (short) 0, rotationXYZ, rotationScale);
        }

        doEvent(enqueue, wait, e); // actual mouse event
    }

    /**
     * Consume the {@link MouseEvent}.
     * <p>
     * Pointer/Mouse Processing Pass 2 (Pass 1 is performed in {@link #doPointerEvent(boolean, boolean, PointerType[], short, int, int, short[], short, int[], int[], float[], float, float[], float)}).
     * </p>
     * <p>
     * Invoked before dispatching the dequeued event.
     * </p>
     * <p>
     * <ul>
     * <li>Validate</li>
     * <li>Handle gestures</li>
     * <li>Synthesize events [ENTERED, EXIT, CLICK] and gestures.</li>
     * <li>Drop exterior events</li>
     * <li>Dispatch event to listener</li>
     * </ul>
     * </p>
     */
    private void consumePointerEvent(MouseEvent pe) {
        if (DEBUG_MOUSE_EVENT) {
            System.err.println("consumePointerEvent.in: " + pe + ", " + pState0 + ", pos " + pe.getX() + "/" + pe.getY() + ", win[" + getX() + "/" + getY() + " " + getWidth() + "x" + getHeight() +
                    "], pixel[" + getSurfaceWidth() + "x" + getSurfaceHeight() + "]");
        }

        //
        // - Determine ENTERED/EXITED state
        // - Synthesize ENTERED and EXIT event
        // - Reset states if applicable
        //
        long when = pe.getWhen();
        int eventType = pe.getEventType();
        boolean insideSurface;
        boolean eExitAllowed = false;
        MouseEvent eEntered = null, eExited = null;
        switch (eventType) {
            case MouseEvent.EVENT_MOUSE_EXITED:
                if (pState0.exitSent || pState0.dragging) {
                    if (DEBUG_MOUSE_EVENT) {
                        System.err.println("consumePointerEvent: drop " + (pState0.exitSent ? "already sent" : "due to dragging") + ": " + pe + ", " + pState0);
                    }
                    return;
                }
                // Fall through intended !
            case MouseEvent.EVENT_MOUSE_ENTERED:
                // clip coordinates to window dimension
                // final int pe_x = Math.min(Math.max(pe.getX(),  0), getSurfaceWidth()-1);
                // final int pe_y = Math.min(Math.max(pe.getY(),  0), getSurfaceHeight()-1);
                pState0.clearButton();
                if (eventType == MouseEvent.EVENT_MOUSE_ENTERED) {
                    insideSurface = true;
                    pState0.insideSurface = true;
                    pState0.exitSent = false;
                    pState0.dragging = false;
                } else {
                    insideSurface = false;
                    pState0.insideSurface = false;
                    pState0.exitSent = true;
                }
                break;

            case MouseEvent.EVENT_MOUSE_MOVED:
            case MouseEvent.EVENT_MOUSE_RELEASED:
                if (1 >= pe.getButtonDownCount()) { // MOVE or RELEASE last button
                    eExitAllowed = !pState0.exitSent;
                    pState0.dragging = false;
                }
                // Fall through intended !

            default:
                int pe_x = pe.getX();
                int pe_y = pe.getY();
                insideSurface = pe_x >= 0 && pe_y >= 0 && pe_x < getSurfaceWidth() && pe_y < getSurfaceHeight();
                if (pe.getPointerType(0) == PointerType.Mouse) {
                    if (!pState0.insideSurface && insideSurface) {
                        // ENTER .. use clipped coordinates
                        eEntered = new MouseEvent(MouseEvent.EVENT_MOUSE_ENTERED, pe.getSource(), pe.getWhen(), pe.getModifiers(),
                                Math.min(pe_x, getSurfaceWidth() - 1),
                                Math.min(pe_y, getSurfaceHeight() - 1),
                                (short) 0, (short) 0, pe.getRotation(), pe.getRotationScale());
                        pState0.exitSent = false;
                    } else if (!insideSurface && eExitAllowed) {
                        // EXIT .. use clipped coordinates
                        eExited = new MouseEvent(MouseEvent.EVENT_MOUSE_EXITED, pe.getSource(), pe.getWhen(), pe.getModifiers(),
                                Math.min(Math.max(pe_x, 0), getSurfaceWidth() - 1),
                                Math.min(Math.max(pe_y, 0), getSurfaceHeight() - 1),
                                (short) 0, (short) 0, pe.getRotation(), pe.getRotationScale());
                        pState0.exitSent = true;
                    }
                }
                if (pState0.insideSurface != insideSurface || null != eEntered || null != eExited) {
                    pState0.clearButton();
                }
                pState0.insideSurface = insideSurface;
        }
        if (null != eEntered) {
            if (DEBUG_MOUSE_EVENT) {
                System.err.println("consumePointerEvent.send.0: " + eEntered + ", " + pState0);
            }
            dispatchMouseEvent(eEntered);
        } else if (DEBUG_MOUSE_EVENT && !insideSurface) {
            System.err.println("INFO consumePointerEvent.exterior: " + pState0 + ", " + pe);
        }

        //
        // Handle Default Gestures
        //
        if (defaultGestureHandlerEnabled &&
                pe.getPointerType(0).getPointerClass() == MouseEvent.PointerClass.Onscreen) {
            if (null == gesture2PtrTouchScroll) {
                int scaledScrollSlop;
                int scaledDoubleTapSlop;
                MonitorDevice monitor = getMainMonitor();
                if (null != monitor) {
                    DimensionImmutable mm = monitor.getSizeMM();
                    float pixWPerMM = (float) monitor.getCurrentMode().getRotatedWidth() / mm.getWidth();
                    float pixHPerMM = (float) monitor.getCurrentMode().getRotatedHeight() / mm.getHeight();
                    float pixPerMM = Math.min(pixHPerMM, pixWPerMM);
                    scaledScrollSlop = Math.round(DoubleTapScrollGesture.SCROLL_SLOP_MM * pixPerMM);
                    scaledDoubleTapSlop = Math.round(DoubleTapScrollGesture.DOUBLE_TAP_SLOP_MM * pixPerMM);
                    if (DEBUG_MOUSE_EVENT) {
                        System.err.println("consumePointerEvent.gscroll: scrollSlop " + scaledScrollSlop + ", doubleTapSlop " + scaledDoubleTapSlop + ", pixPerMM " + pixPerMM + ", " + monitor + ", " + pState0);
                    }
                } else {
                    scaledScrollSlop = DoubleTapScrollGesture.SCROLL_SLOP_PIXEL;
                    scaledDoubleTapSlop = DoubleTapScrollGesture.DOUBLE_TAP_SLOP_PIXEL;
                }
                gesture2PtrTouchScroll = new DoubleTapScrollGesture(step(DoubleTapScrollGesture.SCROLL_SLOP_PIXEL, DoubleTapScrollGesture.SCROLL_SLOP_PIXEL / 2, scaledScrollSlop),
                        step(DoubleTapScrollGesture.DOUBLE_TAP_SLOP_PIXEL, DoubleTapScrollGesture.DOUBLE_TAP_SLOP_PIXEL / 2, scaledDoubleTapSlop));
            }
            if (gesture2PtrTouchScroll.process(pe)) {
                pe = (MouseEvent) gesture2PtrTouchScroll.getGestureEvent();
                gesture2PtrTouchScroll.clear(false);
                if (DEBUG_MOUSE_EVENT) {
                    System.err.println("consumePointerEvent.gscroll: " + pe + ", " + pState0);
                }
                dispatchMouseEvent(pe);
                return;
            }
            if (gesture2PtrTouchScroll.isWithinGesture()) {
                return; // within gesture .. need more input ..
            }
        }
        //
        // Handle Custom Gestures
        //
        {
            int pointerGestureHandlerCount = pointerGestureHandler.size();
            if (pointerGestureHandlerCount > 0) {
                boolean withinGesture = false;
                for (int i = 0; !pe.isConsumed() && i < pointerGestureHandlerCount; i++) {
                    GestureHandler gh = pointerGestureHandler.get(i);
                    if (gh.process(pe)) {
                        InputEvent ieG = gh.getGestureEvent();
                        gh.clear(false);
                        if (ieG instanceof MouseEvent) {
                            dispatchMouseEvent((MouseEvent) ieG);
                        } else if (ieG instanceof final GestureHandler.GestureEvent ge) {
                            for (int j = 0; !ge.isConsumed() && j < gestureListeners.size(); j++) {
                                gestureListeners.get(j).gestureDetected(ge);
                            }
                        }
                        return;
                    }
                    withinGesture |= gh.isWithinGesture();
                }
                if (withinGesture) {
                    return;
                }
            }
        }

        //
        // - Synthesize mouse CLICKED
        // - Ignore sent CLICKED
        //
        MouseEvent eClicked = null;
        switch (eventType) {
            case MouseEvent.EVENT_MOUSE_PRESSED:
                if (1 == pe.getPointerCount()) {
                    pState0.lastButtonPressTime = when;
                }
                break;
            case MouseEvent.EVENT_MOUSE_RELEASED:
                if (1 == pe.getPointerCount() && when - pState0.lastButtonPressTime < MouseEvent.getClickTimeout()) {
                    eClicked = pe.createVariant(MouseEvent.EVENT_MOUSE_CLICKED);
                } else {
                    pState0.lastButtonPressTime = 0;
                }
                break;
            case MouseEvent.EVENT_MOUSE_CLICKED:
                // ignore - synthesized here ..
                if (DEBUG_MOUSE_EVENT) {
                    System.err.println("consumePointerEvent: drop recv'ed (synth here) " + pe + ", " + pState0);
                }
                pe = null;
                break;

            case MouseEvent.EVENT_MOUSE_DRAGGED:
                pState0.dragging = true;
                break;
        }

        if (null != pe) {
            if (DEBUG_MOUSE_EVENT) {
                System.err.println("consumePointerEvent.send.1: " + pe + ", " + pState0);
            }
            dispatchMouseEvent(pe); // actual mouse event
        }
        if (null != eClicked) {
            if (DEBUG_MOUSE_EVENT) {
                System.err.println("consumePointerEvent.send.2: " + eClicked + ", " + pState0);
            }
            dispatchMouseEvent(eClicked);
        }
        if (null != eExited) {
            if (DEBUG_MOUSE_EVENT) {
                System.err.println("consumePointerEvent.send.3: " + eExited + ", " + pState0);
            }
            dispatchMouseEvent(eExited);
        }
    }

    @Override
    public final void addMouseListener(MouseListener l) {
        addMouseListener(-1, l);
    }

    @Override
    public final void addMouseListener(int index, MouseListener l) {
        if (l == null) {
            return;
        }
        @SuppressWarnings("unchecked") Lst<MouseListener> clonedListeners = mouseListeners.clone();
        if (0 > index) {
            index = clonedListeners.size();
        }
        clonedListeners.add(index, l);
        mouseListeners = clonedListeners;
    }

    @Override
    public final void removeMouseListener(MouseListener l) {
        if (l == null) {
            return;
        }
        @SuppressWarnings("unchecked") Lst<MouseListener> clonedListeners = mouseListeners.clone();
        clonedListeners.remove(l);
        mouseListeners = clonedListeners;
    }

    @Override
    public final MouseListener getMouseListener(int index) {
        @SuppressWarnings("unchecked") Lst<MouseListener> clonedListeners = mouseListeners.clone();
        if (0 > index) {
            index = clonedListeners.size() - 1;
        }
        return clonedListeners.get(index);
    }

    @Override
    public final MouseListener[] getMouseListeners() {
        return mouseListeners.toArray(EmptyMouseListenerArray);
    }

    @Override
    public final void setDefaultGesturesEnabled(boolean enable) {
        defaultGestureHandlerEnabled = enable;
    }

    @Override
    public final boolean areDefaultGesturesEnabled() {
        return defaultGestureHandlerEnabled;
    }

    @Override
    public final void addGestureHandler(GestureHandler gh) {
        addGestureHandler(-1, gh);
    }

    @Override
    public final void addGestureHandler(int index, GestureHandler gh) {
        if (gh == null) {
            return;
        }
        @SuppressWarnings("unchecked") Lst<GestureHandler> cloned = pointerGestureHandler.clone();
        if (0 > index) {
            index = cloned.size();
        }
        cloned.add(index, gh);
        pointerGestureHandler = cloned;
    }

    @Override
    public final void removeGestureHandler(GestureHandler gh) {
        if (gh == null) {
            return;
        }
        @SuppressWarnings("unchecked") Lst<GestureHandler> cloned = pointerGestureHandler.clone();
        cloned.remove(gh);
        pointerGestureHandler = cloned;
    }

    @Override
    public final void addGestureListener(GestureHandler.GestureListener gl) {
        addGestureListener(-1, gl);
    }

    @Override
    public final void addGestureListener(int index, GestureHandler.GestureListener gl) {
        if (gl == null) {
            return;
        }
        @SuppressWarnings("unchecked") Lst<GestureHandler.GestureListener> cloned = gestureListeners.clone();
        if (0 > index) {
            index = cloned.size();
        }
        cloned.add(index, gl);
        gestureListeners = cloned;
    }

    @Override
    public final void removeGestureListener(GestureHandler.GestureListener gl) {
        if (gl == null) return;

        @SuppressWarnings("unchecked") Lst<GestureHandler.GestureListener> cloned = gestureListeners.clone();
        cloned.remove(gl);
        gestureListeners = cloned;
    }

    private void dispatchMouseEvent(MouseEvent e) {
        for (int i = 0; !e.isConsumed() && i < mouseListeners.size(); i++) {
            MouseListener l = mouseListeners.get(i);
            switch (e.getEventType()) {
                case MouseEvent.EVENT_MOUSE_CLICKED -> l.mouseClicked(e);
                case MouseEvent.EVENT_MOUSE_ENTERED -> l.mouseEntered(e);
                case MouseEvent.EVENT_MOUSE_EXITED -> l.mouseExited(e);
                case MouseEvent.EVENT_MOUSE_PRESSED -> l.mousePressed(e);
                case MouseEvent.EVENT_MOUSE_RELEASED -> l.mouseReleased(e);
                case MouseEvent.EVENT_MOUSE_MOVED -> l.mouseMoved(e);
                case MouseEvent.EVENT_MOUSE_DRAGGED -> l.mouseDragged(e);
                case MouseEvent.EVENT_MOUSE_WHEEL_MOVED -> l.mouseWheelMoved(e);
                default -> throw new NativeWindowException("Unexpected mouse event type " + e.getEventType());
            }
        }
    }

    /**
     * @param keyCode the keyCode to set pressed state
     * @param pressed true if pressed, otherwise false
     * @return the previous pressed value
     */
    protected final boolean setKeyPressed(short keyCode, boolean pressed) {
        int v = 0xFFFF & keyCode;
        if (v <= keyTrackingRange) {
            return keyPressedState.put(v, pressed);
        }
        return false;
    }

    /**
     * @param keyCode the keyCode to test pressed state
     * @return true if pressed, otherwise false
     */
    protected final boolean isKeyPressed(short keyCode) {
        int v = 0xFFFF & keyCode;
        if (v <= keyTrackingRange) {
            return keyPressedState.get(v);
        }
        return false;
    }

    public void sendKeyEvent(short eventType, int modifiers, short keyCode, short keySym, char keyChar) {
        // Always add currently pressed mouse buttons to modifier mask
        consumeKeyEvent(KeyEvent.create(eventType, this, NOW(), modifiers | pState1.buttonPressedMask, keyCode, keySym, keyChar));
    }

    public void enqueueKeyEvent(boolean wait, short eventType, int modifiers, short keyCode, short keySym, char keyChar) {
        // Always add currently pressed mouse buttons to modifier mask
        enqueueEvent(wait, KeyEvent.create(eventType, this, NOW(), modifiers | pState1.buttonPressedMask, keyCode, keySym, keyChar));
    }

    @Override
    public final boolean isKeyboardVisible() {
        return keyboardVisible;
    }

    @Override
    public final void setKeyboardVisible(boolean visible) {
        if (isNativeValid()) {
            // We don't skip the impl. if it seems that there is no state change,
            // since we cannot assume the impl. reliably gives us it's current state.
            boolean ok = setKeyboardVisibleImpl(visible);
            if (DEBUG_IMPLEMENTATION || DEBUG_KEY_EVENT) {
                System.err.println("setKeyboardVisible(native): visible " + keyboardVisible + " -- op[visible:" + visible + ", ok " + ok + "] -> " + (visible && ok));
            }
            keyboardVisibilityChanged(visible && ok);
        } else {
            keyboardVisibilityChanged(visible); // earmark for creation
        }
    }

    /**
     * Triggered by implementation's WM events to update the virtual on-screen keyboard's visibility state.
     */
    private void keyboardVisibilityChanged(boolean visible) {
        if (keyboardVisible != visible) {
            if (DEBUG_IMPLEMENTATION || DEBUG_KEY_EVENT) {
                System.err.println("keyboardVisibilityChanged: " + keyboardVisible + " -> " + visible);
            }
            keyboardVisible = visible;
        }
    }

    @Override
    public final void addKeyListener(KeyListener l) {
        addKeyListener(-1, l);
    }

    @Override
    public final void addKeyListener(int index, KeyListener l) {
        if (l == null) {
            return;
        }
        @SuppressWarnings("unchecked") Lst<KeyListener> clonedListeners = keyListeners.clone();
        if (0 > index) {
            index = clonedListeners.size();
        }
        clonedListeners.add(index, l);
        keyListeners = clonedListeners;
    }

    @Override
    public final void removeKeyListener(KeyListener l) {
        if (l == null) {
            return;
        }
        @SuppressWarnings("unchecked") Lst<KeyListener> clonedListeners = keyListeners.clone();
        clonedListeners.remove(l);
        keyListeners = clonedListeners;
    }

    @Override
    public final KeyListener getKeyListener(int index) {
        @SuppressWarnings("unchecked") Lst<KeyListener> clonedListeners = keyListeners.clone();
        if (0 > index) {
            index = clonedListeners.size() - 1;
        }
        return clonedListeners.get(index);
    }

    private static final KeyListener[] EmptyKeyListenerArray = new KeyListener[0];

    @Override
    public final KeyListener[] getKeyListeners() {
        return keyListeners.toArray(EmptyKeyListenerArray);
    }

    private void consumeKeyEvent(KeyEvent e) {
        boolean consumedE = false;
        if (null != keyboardFocusHandler && !e.isAutoRepeat()) {
            consumedE = propagateKeyEvent(e, keyboardFocusHandler);
//            if(DEBUG_KEY_EVENT) {
//                if( consumedE ) {
//                    System.err.println("consumeKeyEvent(kfh): "+e+", consumed: "+consumedE);
//                }
//            }
        }
        if (!consumedE) {
            int s = keyListeners.size();
            for (int i = 0; !consumedE && i < s; i++) {
                consumedE = propagateKeyEvent(e, keyListeners.get(i));
            }
            if (DEBUG_KEY_EVENT) {
                System.err.println("consumeKeyEvent(usr): " + e + ", consumed: " + consumedE);
            }
        }
    }

    //
    // WindowListener/Event Support
    //
    @Override
    public final void sendWindowEvent(int eventType) {
        consumeWindowEvent(new WindowEvent((short) eventType, this, NOW()));
    }

    private void enqueueWindowEvent(boolean wait, int eventType) {
        enqueueEvent(wait, new WindowEvent((short) eventType, this, NOW()));
    }

    @Override
    public final void addWindowListener(WindowListener l) {
        windowListeners.add(l);
    }

    @Override
    public final void addWindowListener(int index, WindowListener l) throws IndexOutOfBoundsException {
        windowListeners.add(index, l);
    }

    @Override
    public final void removeWindowListener(WindowListener l) {
        windowListeners.remove(l);
    }

    @Override
    public final WindowListener getWindowListener(int index) {
        return windowListeners.get(index);
    }


    @Override
    public final WindowListener[] getWindowListeners() {
        return windowListeners.array();
    }

    private void consumeWindowEvent(WindowEvent e) {
//        if(DEBUG_IMPLEMENTATION) {
//            System.err.println("consumeWindowEvent: "+e+", visible "+isVisible()+" "+getX()+"/"+getY()+", win["+getX()+"/"+getY()+" "+getWidth()+"x"+getHeight()+
//                    "], pixel["+getSurfaceWidth()+"x"+getSurfaceHeight()+"]");
//        }
        short type = e.getEventType();
        for (WindowListener l : windowListeners) {
            if (e.isConsumed())
                break;
            switch (type) {
                case WindowEvent.EVENT_WINDOW_RESIZED -> l.windowResized(e);
                case WindowEvent.EVENT_WINDOW_MOVED -> l.windowMoved(e);
                case WindowEvent.EVENT_WINDOW_DESTROY_NOTIFY -> l.windowDestroyNotify(e);
                case WindowEvent.EVENT_WINDOW_DESTROYED -> l.windowDestroyed(e);
                case WindowEvent.EVENT_WINDOW_GAINED_FOCUS -> l.windowGainedFocus(e);
                case WindowEvent.EVENT_WINDOW_LOST_FOCUS -> l.windowLostFocus(e);
                case WindowEvent.EVENT_WINDOW_REPAINT -> l.windowRepaint((WindowUpdateEvent) e);
                default -> throw new NativeWindowException("Unexpected window event type " + type);
            }

        }
    }

    /**
     * Triggered by implementation's WM events to update the focus state.
     */
    protected void focusChanged(boolean defer, boolean focusGained) {
        if (stateMask.get(PSTATE_BIT_FOCUS_CHANGE_BROKEN) ||
                stateMask.get(STATE_BIT_FOCUSED) != focusGained) {
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.focusChanged: (" + getThreadName() + "): (defer: " + defer + ") state " +
                        getStateMaskString() + " -> focus " + focusGained +
                        " - windowHandle " + toHexString(windowHandle) + " parentWindowHandle " +
                        toHexString(parentWindowHandle));
            }
            stateMask.put(STATE_BIT_FOCUSED, focusGained);
            int evt = focusGained ? WindowEvent.EVENT_WINDOW_GAINED_FOCUS : WindowEvent.EVENT_WINDOW_LOST_FOCUS;
            if (!defer) {
                sendWindowEvent(evt);
            } else {
                enqueueWindowEvent(false, evt);
            }
        }
    }

    /**
     * Triggered by implementation's WM events to update the visibility state.
     */
    private void visibleChanged(boolean defer, boolean visible) {
        if (stateMask.put(STATE_BIT_VISIBLE, visible) != visible) {
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.visibleChanged (" + getThreadName() + "): (defer: " + defer + ") visible " + (!visible) + " -> state " + getStateMaskString() + " - windowHandle " + toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle));
            }
        }
    }

    /**
     * Returns -1 if failed, otherwise remaining time until {@link #TIMEOUT_NATIVEWINDOW}, maybe zero.
     */
    private long waitForVisible(boolean visible, boolean failFast) {
        return waitForVisible(visible, failFast, TIMEOUT_NATIVEWINDOW);
    }

    /**
     * Returns -1 if failed, otherwise remaining time until <code>timeOut</code>, maybe zero.
     */
    private long waitForVisible(boolean visible, boolean failFast, long timeOut) {
        DisplayImpl display = (DisplayImpl) screen.getDisplay();
        display.dispatchMessagesNative(); // status up2date
        long remaining;
        boolean _visible = stateMask.get(STATE_BIT_VISIBLE);
        for (remaining = timeOut; 0 < remaining && _visible != visible; remaining -= sizePosWaitMS) {
            //try { Thread.sleep(10); } catch (final InterruptedException ie) {}
            if (remaining < timeOut)
                Util.sleepMS(sizePosWaitMS);
            display.dispatchMessagesNative(); // status up2date
            _visible = stateMask.get(STATE_BIT_VISIBLE);
        }
        if (visible != _visible) {
            String msg = "Visibility not reached as requested within " + timeOut + "ms : requested " + visible + ", is " + _visible;
            if (DEBUG_FREEZE_AT_VISIBILITY_FAILURE) {
//                System.err.println("XXXX: "+msg);
//                System.err.println("XXXX: FREEZE");
                try {
                    while (true) {
                        Thread.sleep(sizePosWaitMS);
                        display.dispatchMessagesNative(); // status up2date
                    }
                } catch (InterruptedException e) {
                    ExceptionUtils.dumpThrowable("", e);
                    Thread.currentThread().interrupt(); // keep state
                }
                throw new NativeWindowException(msg);
            } else {
                if (failFast) {
                    throw new NativeWindowException(msg);
                } else {
                    if (DEBUG_IMPLEMENTATION) {
//                        System.err.println(msg);
                        ExceptionUtils.dumpStack(System.err);
                    }
                    return -1;
                }
            }
        } else if (0 < remaining) {
            return remaining;
        } else {
            return 0;
        }
    }

    /**
     * Notify to update the pixel-scale values.
     *
     * @param minPixelScale
     * @param maxPixelScale
     * @param reset         if {@code true} {@link #setSurfaceScale(float[]) reset pixel-scale} w/ {@link #getRequestedSurfaceScale(float[]) requested values}
     *                      value to reflect the new minimum and maximum values.
     */
    public final void pixelScaleChangeNotify(float[] minPixelScale, float[] maxPixelScale, boolean reset) {
        System.arraycopy(minPixelScale, 0, this.minPixelScale, 0, 2);
        System.arraycopy(maxPixelScale, 0, this.maxPixelScale, 0, 2);
        if (reset) {
            setSurfaceScale(reqPixelScale);
        }
    }

    /**
     * Triggered by implementation's WM events to update the client-area size in window units w/o insets/decorations.
     */
    private void sizeChanged(boolean defer, int newWidth, int newHeight, boolean force) {
        if (force || getWidth() != newWidth || getHeight() != newHeight) {
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.sizeChanged: (" + getThreadName() + "): (defer: " + defer + ") force " + force + ", " +
                        getWidth() + "x" + getHeight() + " -> " + newWidth + "x" + newHeight +
                        ", state " + getStateMaskString() +
                        " - windowHandle " + toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle));
            }
            if (0 > newWidth || 0 > newHeight) {
                throw new NativeWindowException("Illegal width or height " + newWidth + "x" + newHeight + " (must be >= 0)");
            }
            defineSize(newWidth, newHeight);
            if (isNativeValid()) {
                if (!defer) {
                    sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED);
                } else {
                    enqueueWindowEvent(false, WindowEvent.EVENT_WINDOW_RESIZED);
                }
            }
        }
    }

    private boolean waitForSize(int w, int h, boolean failFast, long timeOut) {
        DisplayImpl display = (DisplayImpl) screen.getDisplay();
        long sleep;
        for (sleep = timeOut; 0 < sleep && w != getWidth() && h != getHeight(); sleep -= sizePosWaitMS) {
            display.dispatchMessagesNative(); // status up2date
            if (sleep!=timeOut)
                Util.sleepMS(sizePosWaitMS);
        }
        if (0 >= sleep) {
            String msg = "Size/Pos not reached as requested within " + timeOut + "ms : requested " + w + "x" + h + ", is " + getWidth() + "x" + getHeight();
            if (failFast) {
                throw new NativeWindowException(msg);
            } else if (DEBUG_IMPLEMENTATION) {
                System.err.println(msg);
                ExceptionUtils.dumpStack(System.err);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Triggered by implementation's WM events to update the position.
     */
    private void positionChanged(boolean defer, int newX, int newY) {
        if (getX() != newX || getY() != newY) {
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.positionChanged: (" + getThreadName() + "): (defer: " + defer + ") " + getX() + "/" + getY() + " -> " + newX + "/" + newY + " - windowHandle " + toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle));
            }
            definePosition(newX, newY);
            if (!defer) {
                sendWindowEvent(WindowEvent.EVENT_WINDOW_MOVED);
            } else {
                enqueueWindowEvent(false, WindowEvent.EVENT_WINDOW_MOVED);
            }
        } else {
            stateMask.clear(STATE_BIT_AUTOPOSITION); // ensure it's off even w/ same position
        }
    }

    /**
     * Wait until position is reached within tolerances, either auto-position or custom position.
     * <p>
     * Since WM may not obey our positional request exactly, we allow a tolerance of 2 times insets[left/top], or 64 pixels, whatever is greater.
     * </p>
     */
    private boolean waitForPosition(boolean useCustomPosition, int x, int y, long timeOut) {
        DisplayImpl display = (DisplayImpl) screen.getDisplay();
        int maxDX, maxDY;
        {
            InsetsImmutable insets = getInsets();
            maxDX = Math.max(64, insets.getLeftWidth() * 2);
            maxDY = Math.max(64, insets.getTopHeight() * 2);
        }
        long remaining = timeOut;
        boolean _autopos = false;
        boolean ok;
        do {
            if (useCustomPosition) {
                ok = Math.abs(x - getX()) <= maxDX && Math.abs(y - getY()) <= maxDY;
            } else {
                _autopos = stateMask.get(STATE_BIT_AUTOPOSITION);
                ok = !_autopos;
            }
            if (!ok) {
                try {
                    Thread.sleep(sizePosWaitMS);
                } catch (InterruptedException ie) {
                }
                display.dispatchMessagesNative(); // status up2date
                remaining -= sizePosWaitMS;
            }
        } while (0 < remaining && !ok);
        if (DEBUG_IMPLEMENTATION) {
            if (!ok) {
                if (useCustomPosition) {
                    System.err.println("Custom position " + x + "/" + y + " not reached within timeout, has " + getX() + "/" + getY() + ", remaining " + remaining);
                } else {
                    System.err.println("Auto position not reached within timeout, has " + getX() + "/" + getY() + ", autoPosition " + ", remaining " + remaining);
                }
                ExceptionUtils.dumpStack(System.err);
            }
        }
        return ok;
    }

    /**
     * Triggered by implementation's WM events to update the insets.
     *
     * @param defer
     * @param left   insets, -1 ignored
     * @param right  insets, -1 ignored
     * @param top    insets, -1 ignored
     * @param bottom insets, -1 ignored
     * @see #getInsets()
     * @see #updateInsetsImpl(Insets)
     */
    private void insetsChanged(boolean defer, int left, int right, int top, int bottom) {
        if (left >= 0 && right >= 0 && top >= 0 && bottom >= 0) {
            boolean changed = left != insets.getLeftWidth() || right != insets.getRightWidth() ||
                    top != insets.getTopHeight() || bottom != insets.getBottomHeight();

            if (blockInsetsChange || isUndecorated()) {
                if (DEBUG_IMPLEMENTATION) {
                    if (changed) {
                        System.err.println("Window.insetsChanged (defer: " + defer + "): Skip insets change " + insets + " -> " + new Insets(left, right, top, bottom) + " (blocked " + blockInsetsChange + ", undecoration " + isUndecorated() + ")");
                    }
                }
            } else if (changed) {
                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window.insetsChanged (defer: " + defer + "): Changed " + insets + " -> " + new Insets(left, right, top, bottom));
                }
                insets.set(left, right, top, bottom);
            }
        }
    }

    /**
     * Triggered by implementation's WM events or programmatic while respecting {@link #getDefaultCloseOperation()}.
     *
     * @param force if true, overrides {@link #setDefaultCloseOperation(WindowClosingMode)} with {@link WindowClosingProtocol#DISPOSE_ON_CLOSE}
     *              and hence force destruction. Otherwise is follows the user settings.
     * @return true if this window is no more valid and hence has been destroyed, otherwise false.
     */
    private boolean windowDestroyNotify(boolean force) {
        WindowClosingMode defMode = getDefaultCloseOperation();
        WindowClosingMode mode = force ? WindowClosingMode.DISPOSE_ON_CLOSE : defMode;
        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window.windowDestroyNotify(isNativeValid: " + isNativeValid() + ", force: " + force + ", mode " + defMode + " -> " + mode + ") " + getThreadName() + ": " + this);
            // ExceptionUtils.dumpStackTrace(System.err);
        }

        boolean destroyed;

        if (isNativeValid()) {
            if (WindowClosingMode.DISPOSE_ON_CLOSE == mode) {
                if (force) {
                    setDefaultCloseOperation(mode);
                }
                try {
                    if (null == windowDestroyNotifyAction) {
                        destroy();
                    } else {
                        windowDestroyNotifyAction.run();
                    }
                } finally {
                    if (force) {
                        setDefaultCloseOperation(defMode);
                    }
                }
            } else {
                // send synced destroy notifications
                sendWindowEvent(WindowEvent.EVENT_WINDOW_DESTROY_NOTIFY);
            }

            destroyed = !isNativeValid();
        } else {
            destroyed = true;
        }

        if (DEBUG_IMPLEMENTATION) {
            System.err.println("Window.windowDestroyNotify(isNativeValid: " + isNativeValid() + ", force: " + force + ", mode " + mode + ") END " + getThreadName() + ": destroyed " + destroyed + ", " + this);
        }

        return destroyed;
    }

    @Override
    public final void windowRepaint(int x, int y, int width, int height) {
        windowRepaint(false, x, y, width, height);
    }

    /**
     * Triggered by implementation's WM events to update the content
     *
     * @param defer  if true sent event later, otherwise wait until processed.
     * @param x      dirty-region y-pos in pixel units
     * @param y      dirty-region x-pos in pixel units
     * @param width  dirty-region width in pixel units
     * @param height dirty-region height in pixel units
     */
    private void windowRepaint(boolean defer, int x, int y, int width, int height) {
        width = (0 >= width) ? getSurfaceWidth() : width;
        height = (0 >= height) ? getSurfaceHeight() : height;
//        if(DEBUG_IMPLEMENTATION) {
//            System.err.println("Window.windowRepaint "+getThreadName()+" (defer: "+defer+") "+x+"/"+y+" "+width+"x"+height);
//        }

        if (isNativeValid()) {
            doEvent(defer, false, new WindowUpdateEvent(WindowEvent.EVENT_WINDOW_REPAINT, this, NOW(),
                    new Rectangle(x, y, width, height)));
        }
    }

    /**
     * Triggered by implementation.
     */
    protected final void sendMouseEventRequestFocus(short eventType, int modifiers,
                                                    int x, int y, short button, float rotation) {
        sendMouseEvent(eventType, modifiers, x, y, button, rotation);
        requestFocus(false /* wait */);
    }

    /**
     * Triggered by implementation's WM events to update the visibility state and send- or enqueue one mouse event
     *
     * @param defer
     * @param visibleChange  -1 ignored, 0 invisible, > 0 visible
     * @param entranceChange -1 ignored, 0 exit, > 0 enter
     * @param eventType      0 ignored, > 0 [send|enqueue]MouseEvent
     * @param modifiers
     * @param x
     * @param y
     * @param button
     * @param rotation
     */
    protected final void visibleChangedSendMouseEvent(boolean defer, int visibleChange,
                                                      short eventType, int modifiers,
                                                      int x, int y, short button, float rotation) {
        if (0 <= visibleChange) { // ignore visible < 0
            visibleChanged(defer, 0 < visibleChange);
        }
        if (0 < eventType) {
            if (defer) {
                enqueueMouseEvent(false /* wait */, eventType, modifiers, x, y, button, rotation);
            } else {
                sendMouseEvent(eventType, modifiers, x, y, button, rotation);
            }
        }
    }

    /**
     * Triggered by implementation's WM events to update the content
     *
     * @param defer         if true sent event later, otherwise wait until processed.
     * @param visibleChange -1 ignored, 0 invisible, > 0 visible
     * @param x             dirty-region y-pos in pixel units
     * @param y             dirty-region x-pos in pixel units
     * @param width         dirty-region width in pixel units
     * @param height        dirty-region height in pixel units
     */
    protected final void visibleChangedWindowRepaint(boolean defer, int visibleChange,
                                                     int x, int y, int width, int height) {
        if (0 <= visibleChange) { // ignore visible < 0
            visibleChanged(defer, 0 < visibleChange);
        }
        windowRepaint(defer, x, y, width, height);
    }

    /**
     * Triggered by implementation's WM events to update the focus and visibility state
     *
     * @param defer
     * @param focusChange   -1 ignored, 0 unfocused, > 0 focused
     * @param visibleChange -1 ignored, 0 invisible, > 0 visible
     */
    protected final void focusVisibleChanged(boolean defer,
                                             int focusChange,
                                             int visibleChange) {
        if (0 <= focusChange) { // ignore focus < 0
            focusChanged(defer, 0 < focusChange);
        }
        if (0 <= visibleChange) { // ignore visible < 0
            visibleChanged(defer, 0 < visibleChange);
        }
    }

    /**
     * Triggered by implementation's WM events to update the client-area position, size, insets and maximized flags.
     *
     * @param defer
     * @param left          insets, -1 ignored
     * @param right         insets, -1 ignored
     * @param top           insets, -1 ignored
     * @param bottom        insets, -1 ignored
     * @param visibleChange -1 ignored, 0 invisible, > 0 visible
     */
    protected final void insetsVisibleChanged(boolean defer,
                                              int left, int right, int top, int bottom,
                                              int visibleChange) {
        insetsChanged(defer, left, right, top, bottom);
        if (0 <= visibleChange) { // ignore visible < 0
            visibleChanged(defer, 0 < visibleChange);
        }
    }

    /**
     * Triggered by implementation's WM events to update the client-area position, size, insets and maximized flags.
     *
     * @param defer
     * @param newX
     * @param newY
     * @param newWidth
     * @param newHeight
     * @param left          insets, -1 ignored
     * @param right         insets, -1 ignored
     * @param top           insets, -1 ignored
     * @param bottom        insets, -1 ignored
     * @param focusChange   -1 ignored, 0 unfocused, > 0 focused
     * @param visibleChange -1 ignored, 0 invisible, > 0 visible
     * @param force
     */
    protected final void sizePosInsetsFocusVisibleChanged(boolean defer,
                                                          int newX, int newY,
                                                          int newWidth, int newHeight,
                                                          int left, int right, int top, int bottom,
                                                          int focusChange,
                                                          int visibleChange,
                                                          boolean force) {
        sizeChanged(defer, newWidth, newHeight, force);
        positionChanged(defer, newX, newY);
        insetsChanged(defer, left, right, top, bottom);
        if (0 <= focusChange) { // ignore focus < 0
            focusChanged(defer, 0 < focusChange);
        }
        if (0 <= visibleChange) { // ignore visible < 0
            visibleChanged(defer, 0 < visibleChange);
        }
    }

    /**
     * Triggered by implementation's WM events to update the client-area position, size, insets and maximized flags.
     *
     * @param defer
     * @param newX
     * @param newY
     * @param newWidth
     * @param newHeight
     * @param maxHorzChange -1 ignored, 0 !maximized, > 0 maximized
     * @param maxVertChange -1 ignored, 0 !maximized, > 0 maximized
     * @param left          insets, -1 ignored
     * @param right         insets, -1 ignored
     * @param top           insets, -1 ignored
     * @param bottom        insets, -1 ignored
     * @param visibleChange -1 ignored, 0 invisible, > 0 visible
     * @param force
     */
    protected final void sizePosMaxInsetsVisibleChanged(boolean defer,
                                                        int newX, int newY,
                                                        int newWidth, int newHeight,
                                                        int maxHorzChange, int maxVertChange,
                                                        int left, int right, int top, int bottom,
                                                        int visibleChange,
                                                        boolean force) {
        sizeChanged(defer, newWidth, newHeight, force);
        positionChanged(defer, newX, newY);
        if (0 <= maxHorzChange && 0 <= maxVertChange) {
            maximizedChanged(0 < maxHorzChange, 0 < maxVertChange);
        }
        insetsChanged(defer, left, right, top, bottom);
        if (0 <= visibleChange) { // ignore visible < 0
            visibleChanged(defer, 0 < visibleChange);
        }
    }

    public interface LifecycleHook {
        /**
         * Reset of internal state counter, ie totalFrames, etc.
         * Called from EDT while window is locked.
         */
        void resetCounter();

        /**
         * Invoked after Window setVisible,
         * allows allocating resources depending on the native Window.
         * Called from EDT while window is locked.
         */
        void setVisibleActionPost(boolean visible, boolean nativeWindowCreated);

        /**
         * Notifies the receiver to preserve resources (GL, ..)
         * for the next destroy*() calls (only), if supported and if <code>value</code> is <code>true</code>, otherwise clears preservation flag.
         *
         * @param value <code>true</code> to set the one-shot preservation if supported, otherwise clears it.
         */
        void preserveGLStateAtDestroy(boolean value);

        /**
         * Invoked before Window destroy action,
         * allows releasing of resources depending on the native Window.<br>
         * Surface not locked yet.<br>
         * Called not necessarily from EDT.
         */
        void destroyActionPreLock();

        /**
         * Invoked before Window destroy action,
         * allows releasing of resources depending on the native Window.<br>
         * Surface locked.<br>
         * Called from EDT while window is locked.
         */
        void destroyActionInLock();

        /**
         * Invoked for expensive modifications, ie while reparenting and MonitorMode change.<br>
         * No lock is hold when invoked.<br>
         *
         * @return true is paused, otherwise false. If true {@link #resumeRenderingAction()} shall be issued.
         * @see #resumeRenderingAction()
         */
        boolean pauseRenderingAction();

        /**
         * Invoked for expensive modifications, ie while reparenting and MonitorMode change.
         * No lock is hold when invoked.<br>
         *
         * @see #pauseRenderingAction()
         */
        void resumeRenderingAction();

        /**
         * Shutdown rendering action (thread) abnormally.
         * <p>
         * Should be called only at shutdown, if necessary.
         * </p>
         */
        void shutdownRenderingAction();
    }

    //
    // Accumulated actions
    //

    /**
     * from event passing: {@link WindowImpl#consumePointerEvent(MouseEvent)}.
     */
    private static class PointerState0 {
        /**
         * Pointer entered window - is inside the window (may be synthetic)
         */
        boolean insideSurface = false;
        /**
         * Mouse EXIT has been sent (only for MOUSE type enter/exit)
         */
        boolean exitSent = false;

        /**
         * last time when a pointer button was pressed
         */
        long lastButtonPressTime = 0;

        /**
         * Pointer in dragging mode
         */
        boolean dragging = false;

        void clearButton() {
            lastButtonPressTime = 0;
        }

        public String toString() {
            return "PState0[inside " + insideSurface + ", exitSent " + exitSent + ", lastPress " + lastButtonPressTime + ", dragging " + dragging + "]";
        }
    }

    /**
     * from direct input: {@link WindowImpl#doPointerEvent(boolean, boolean, int[], short, int, int, boolean, short[], int[], int[], float[], float, float[], float)}.
     */
    private static class PointerState1 extends PointerState0 {
        /**
         * Last pointer-move position for 8 touch-down pointers
         */
        final Point[] movePositions = {
                new Point(), new Point(), new Point(), new Point(),
                new Point(), new Point(), new Point(), new Point()};
        /**
         * Current pressed mouse button number
         */
        short buttonPressed = 0;
        /**
         * Current pressed mouse button modifier mask
         */
        int buttonPressedMask = 0;
        /**
         * Last mouse button click count
         */
        short lastButtonClickCount = 0;

        @Override
        final void clearButton() {
            super.clearButton();
            lastButtonClickCount = 0;
            if (!dragging || 0 == buttonPressedMask) {
                buttonPressed = 0;
                buttonPressedMask = 0;
                dragging = false;
            }
        }

        final Point getMovePosition(int id) {
            if (0 <= id && id < movePositions.length) {
                return movePositions[id];
            }
            return null;
        }

        public final String toString() {
            return "PState1[inside " + insideSurface + ", exitSent " + exitSent + ", lastPress " + lastButtonPressTime +
                    ", pressed [button " + buttonPressed + ", mask " + buttonPressedMask + ", dragging " + dragging + ", clickCount " + lastButtonClickCount + "]";
        }
    }

    private class VisibleAction implements Runnable {
        final boolean visible;

        private VisibleAction(boolean visible) {
            this.visible = visible;
        }

        @Override
        public final void run() {
            setVisibleActionImpl(visible);
        }
    }

    private class SetSizeAction implements Runnable {
        final int width;
        final int height;
        final boolean force;

        private SetSizeAction(int w, int h, boolean disregardFS) {
            this.width = w;
            this.height = h;
            this.force = disregardFS;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                if (force || (!isFullscreen() && (getWidth() != width || getHeight() != height))) {
                    if (DEBUG_IMPLEMENTATION) {
                        System.err.println("Window setSize: START force " + force + ", " + getWidth() + "x" + getHeight() + " -> " + width + "x" + height + ", windowHandle " + toHexString(windowHandle) + ", state " + getStateMaskString());
                    }
                    boolean _visible = stateMask.get(STATE_BIT_VISIBLE);
                    int visibleAction; // 0 nop, 1 invisible, 2 visible (create)
                    if (_visible && isNativeValid() && (0 >= width || 0 >= height)) {
                        visibleAction = 1; // invisible
                        defineSize(0, 0);
                    } else if (_visible && !isNativeValid() && 0 < width && 0 < height) {
                        visibleAction = 2; // visible (create)
                        defineSize(width, height);
                    } else if (_visible && isNativeValid()) {
                        visibleAction = 0;
                        // this width/height will be set by windowChanged, called by the native implementation
                        reconfigureWindowImpl(getX(), getY(), width, height, getReconfigureMask(0, isVisible()));
                        WindowImpl.this.waitForSize(width, height, false, TIMEOUT_NATIVEWINDOW);
                    } else {
                        // invisible or invalid w/ 0 size
                        visibleAction = 0;
                        defineSize(width, height);
                    }
                    if (DEBUG_IMPLEMENTATION) {
                        System.err.println("Window setSize: END " + getWidth() + "x" + getHeight() + ", visibleAction " + visibleAction);
                    }
                    switch (visibleAction) {
                        case 1 -> setVisibleActionImpl(false);
                        case 2 -> setVisibleActionImpl(true);
                    }
                }
            } finally {
                _lock.unlock();
            }
        }
    }

    private class ReparentAction implements Runnable {
        final NativeWindow newParentWindow;
        final int topLevelX, topLevelY;
        final int hints;
        ReparentOperation operation;

        private ReparentAction(NativeWindow newParentWindow, int topLevelX, int topLevelY, int hints) {
            this.newParentWindow = newParentWindow;
            this.topLevelX = topLevelX;
            this.topLevelY = topLevelY;
            if (DEBUG_TEST_REPARENT_INCOMPATIBLE) {
                hints |= REPARENT_HINT_FORCE_RECREATION;
            }
            this.hints = hints;
            this.operation = ReparentOperation.ACTION_INVALID; // ensure it's set
        }

        private ReparentOperation getOp() {
            return operation;
        }

        @Override
        public final void run() {
            if (WindowImpl.this.isFullscreen()) {
                // Bug 924: Ignore reparent when in fullscreen - otherwise may confuse WM
                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window.reparent: NOP (in fullscreen, " + getThreadName() + ") valid " + isNativeValid() +
                            ", windowHandle " + toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle) + ", state " + getStateMaskString());
                }
                return;
            }
            boolean animatorPaused = false;
            if (null != lifecycleHook) {
                animatorPaused = lifecycleHook.pauseRenderingAction();
            }
            reparent();
            if (animatorPaused) {
                lifecycleHook.resumeRenderingAction();
            }
        }

        private void reparent() {
            // mirror pos/size so native change notification can get overwritten
            int oldX = getX();
            int oldY = getY();
            int oldWidth = getWidth();
            int oldHeight = getHeight();
            int x, y;
            int width = oldWidth;
            int height = oldHeight;

            boolean wasVisible;
            boolean becomesVisible;
            boolean forceDestroyCreate;

            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                {
                    boolean v = 0 != (REPARENT_HINT_FORCE_RECREATION & hints);
                    if (isNativeValid()) {
                        // force recreation if offscreen, since it may become onscreen
                        v |= isOffscreenInstance(WindowImpl.this, newParentWindow);
                    }
                    forceDestroyCreate = v;
                }

                wasVisible = isVisible();
                becomesVisible = wasVisible || 0 != (REPARENT_HINT_BECOMES_VISIBLE & hints);

                Window newParentWindowNEWT = null;
                if (newParentWindow instanceof Window) {
                    newParentWindowNEWT = (Window) newParentWindow;
                }

                long newParentWindowHandle = 0;

                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window.reparent: START (" + getThreadName() + ") valid " + isNativeValid() +
                            ", windowHandle " + toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle) +
                            ", state " + getStateMaskString() + " -> visible " + becomesVisible +
                            ", forceDestroyCreate " + forceDestroyCreate +
                            ", DEBUG_TEST_REPARENT_INCOMPATIBLE " + DEBUG_TEST_REPARENT_INCOMPATIBLE +
                            ", HINT_FORCE_RECREATION " + (0 != (REPARENT_HINT_FORCE_RECREATION & hints)) +
                            ", HINT_BECOMES_VISIBLE " + (0 != (REPARENT_HINT_BECOMES_VISIBLE & hints)) +
                            ", old parentWindow: " + Display.hashCodeNullSafe(parentWindow) +
                            ", new parentWindow: " + Display.hashCodeNullSafe(newParentWindow));
                }

                if (null != newParentWindow) {
                    // REPARENT TO CHILD WINDOW

                    // reset position to 0/0 within parent space
                    x = 0;
                    y = 0;

                    // refit if size is bigger than parent
                    if (width > newParentWindow.getWidth()) {
                        width = newParentWindow.getWidth();
                    }
                    if (height > newParentWindow.getHeight()) {
                        height = newParentWindow.getHeight();
                    }

                    // Case: Child Window
                    newParentWindowHandle = getNativeWindowHandle(newParentWindow);
                    if (0 == newParentWindowHandle) {
                        // Case: Parent's native window not realized yet
                        if (null == newParentWindowNEWT) {
                            throw new NativeWindowException("Reparenting with non NEWT Window type only available after it's realized: " + newParentWindow);
                        }
                        // Destroy this window and use parent's Screen.
                        // It may be created properly when the parent is made visible.
                        destroy(becomesVisible);
                        setScreen((ScreenImpl) newParentWindowNEWT.getScreen());
                        operation = ReparentOperation.ACTION_NATIVE_CREATION_PENDING;
                    } else if (newParentWindow != getParent()) {
                        // Case: Parent's native window realized and changed
                        if (!isNativeValid()) {
                            // May create a new compatible Screen/Display and
                            // mark it for creation.
                            if (null != newParentWindowNEWT) {
                                setScreen((ScreenImpl) newParentWindowNEWT.getScreen());
                            } else {
                                Screen newScreen = NewtFactory.createCompatibleScreen(newParentWindow, screen);
                                if (screen != newScreen) {
                                    // auto destroy on-the-fly created Screen/Display
                                    setScreen((ScreenImpl) newScreen);
                                }
                            }
                            if (0 < width && 0 < height) {
                                operation = ReparentOperation.ACTION_NATIVE_CREATION;
                            } else {
                                operation = ReparentOperation.ACTION_NATIVE_CREATION_PENDING;
                            }
                        } else if (forceDestroyCreate || !NewtFactory.isScreenCompatible(newParentWindow, screen)) {
                            // Destroy this window, may create a new compatible Screen/Display, while trying to preserve resources if becoming visible again.
                            destroy(becomesVisible);
                            if (null != newParentWindowNEWT) {
                                setScreen((ScreenImpl) newParentWindowNEWT.getScreen());
                            } else {
                                setScreen((ScreenImpl) NewtFactory.createCompatibleScreen(newParentWindow, screen));
                            }
                            operation = ReparentOperation.ACTION_NATIVE_CREATION;
                        } else {
                            // Mark it for native reparenting
                            operation = ReparentOperation.ACTION_NATIVE_REPARENTING;
                        }
                    } else {
                        // Case: Parent's native window realized and not changed
                        operation = ReparentOperation.ACTION_NOP;
                    }
                } else {
                    // REPARENT TO TOP-LEVEL WINDOW
                    if (0 <= topLevelX && 0 <= topLevelY) {
                        x = topLevelX;
                        y = topLevelY;
                    } else if (null != parentWindow) {
                        // child -> top
                        // put client to current parent+child position
                        Point p = getLocationOnScreen(null);
                        x = p.getX();
                        y = p.getY();
                    } else {
                        x = oldX;
                        y = oldY;
                    }

                    // Case: Top Window
                    if (0 == parentWindowHandle) {
                        // Already Top Window
                        operation = ReparentOperation.ACTION_NOP;
                    } else if (!isNativeValid() || forceDestroyCreate) {
                        // Destroy this window and mark it for [pending] creation.
                        // If isNativeValid() and becoming visible again - try to preserve resources, i.e. b/c on-/offscreen switch.
                        destroy(becomesVisible);
                        if (0 < width && 0 < height) {
                            operation = ReparentOperation.ACTION_NATIVE_CREATION;
                        } else {
                            operation = ReparentOperation.ACTION_NATIVE_CREATION_PENDING;
                        }
                    } else {
                        // Mark it for native reparenting
                        operation = ReparentOperation.ACTION_NATIVE_REPARENTING;
                    }
                }
                parentWindowHandle = newParentWindowHandle;

                if (ReparentOperation.ACTION_INVALID == operation) {
                    throw new NativeWindowException("Internal Error: reparentAction not set");
                }

                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window.reparent: ACTION (" + getThreadName() + ") windowHandle " + toHexString(windowHandle) + " new parentWindowHandle " + toHexString(newParentWindowHandle) + ", reparentAction " + operation + ", pos/size " + x + "/" + y + " " + width + "x" + height + ", visible " + wasVisible);
                }

                if (ReparentOperation.ACTION_NOP == operation) {
                    return;
                }

                if (null == newParentWindow) {
                    // CLIENT -> TOP: Reset Parent's Pointer State
                    setOffscreenPointerIcon(null);
                    setOffscreenPointerVisible(true, null);
                }

                // rearrange window tree
                if (null != parentWindow && parentWindow instanceof Window) {
                    ((Window) parentWindow).removeChild(WindowImpl.this);
                }
                parentWindow = newParentWindow;
                stateMask.put(STATE_BIT_CHILDWIN, null != parentWindow);
                if (parentWindow instanceof Window) {
                    ((Window) parentWindow).addChild(WindowImpl.this);
                }

                if (ReparentOperation.ACTION_NATIVE_REPARENTING == operation) {
                    DisplayImpl display = (DisplayImpl) screen.getDisplay();
                    display.dispatchMessagesNative(); // status up2date

                    // TOP -> CLIENT: !visible first (fixes X11 unsuccessful return to parent window)
                    if (null != parentWindow && wasVisible && NativeWindowFactory.TYPE_X11 == NativeWindowFactory.getNativeWindowType(true)) {
                        setVisibleImpl(false /* visible */, true /* fast */, oldX, oldY, oldWidth, oldHeight);
                        WindowImpl.this.waitForVisible(false, false);
                        // FIXME: Some composite WM behave slacky .. give 'em chance to change state -> invisible,
                        // even though we do exactly that (KDE+Composite)
                        //try { Thread.sleep(100); } catch (final InterruptedException e) { }
                        display.dispatchMessagesNative(); // status up2date
                    }

                    // Lock parentWindow only during reparenting (attempt)
                    NativeWindow parentWindowLocked;
                    if (null != parentWindow) {
                        parentWindowLocked = parentWindow;
                        if (NativeSurface.LOCK_SURFACE_NOT_READY >= parentWindowLocked.lockSurface()) {
                            throw new NativeWindowException("Parent surface lock: not ready: " + parentWindowLocked);
                        }
                        // update native handle, locked state
                        parentWindowHandle = parentWindowLocked.getWindowHandle();
                    } else {
                        parentWindowLocked = null;
                    }
                    boolean ok = false;
                    try {
                        ok = reconfigureWindowImpl(x, y, width, height, getReconfigureMask(CHANGE_MASK_PARENTING | CHANGE_MASK_DECORATION, isVisible()));
                    } finally {
                        if (null != parentWindowLocked) {
                            parentWindowLocked.unlockSurface();
                        }
                    }
                    definePosition(x, y); // position might not get updated by WM events (SWT parent apparently)

                    // set visible again
                    if (ok) {
                        display.dispatchMessagesNative(); // status up2date
                        if (wasVisible) {
                            setVisibleImpl(true /* visible */, true /* fast */, x, y, width, height);
                            ok = 0 <= WindowImpl.this.waitForVisible(true, false);
                            if (ok) {
                                if (isAlwaysOnTop() && 0 == parentWindowHandle && NativeWindowFactory.TYPE_X11 == NativeWindowFactory.getNativeWindowType(true)) {
                                    // Reinforce ALWAYSONTOP when CHILD -> TOP reparenting, since reparenting itself cause X11 WM to loose it's state.
                                    reconfigureWindowImpl(x, y, width, height, getReconfigureMask(CHANGE_MASK_ALWAYSONTOP, isVisible()));
                                }
                                ok = WindowImpl.this.waitForSize(width, height, false, TIMEOUT_NATIVEWINDOW);
                            }
                            if (ok) {
                                if (0 == parentWindowHandle) {
                                    // Position mismatch shall not lead to reparent failure
                                    WindowImpl.this.waitForPosition(true, x, y, TIMEOUT_NATIVEWINDOW);
                                }

                                requestFocusInt(0 == parentWindowHandle /* skipFocusAction if top-level */);
                                display.dispatchMessagesNative(); // status up2date
                            }
                        }
                    }

                    if (!ok || !wasVisible) {
                        // make size and position persistent manual,
                        // since we don't have a WM feedback (invisible or recreation)
                        definePosition(x, y);
                        defineSize(width, height);
                    }

                    if (!ok) {
                        // native reparent failed -> try creation, while trying to preserve resources if becoming visible again.
                        if (DEBUG_IMPLEMENTATION) {
                            System.err.println("Window.reparent: native reparenting failed (" + getThreadName() + ") windowHandle " + toHexString(windowHandle) + " parentWindowHandle " + toHexString(parentWindowHandle) + " -> " + toHexString(newParentWindowHandle) + " - Trying recreation");
                        }
                        destroy(becomesVisible);
                        operation = ReparentOperation.ACTION_NATIVE_CREATION;
                    } else {
                        if (null != parentWindow) {
                            // TOP -> CLIENT: Setup Parent's Pointer State
                            setOffscreenPointerIcon(pointerIcon);
                            setOffscreenPointerVisible(stateMask.get(STATE_BIT_POINTERVISIBLE), pointerIcon);
                        }
                    }
                } else {
                    // Case
                    //   ACTION_NATIVE_CREATION
                    //   ACTION_NATIVE_CREATION_PENDING;

                    // make size and position persistent for proper [re]creation
                    definePosition(x, y);
                    defineSize(width, height);
                }

                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window.reparent: END-1 (" + getThreadName() + ") state " + getStateMaskString() +
                            ", windowHandle " + toHexString(windowHandle) +
                            ", parentWindowHandle " + toHexString(parentWindowHandle) +
                            ", parentWindow " + Display.hashCodeNullSafe(parentWindow) + " " +
                            getX() + "/" + getY() + " " + getWidth() + "x" + getHeight());
                }
            } finally {
                if (null != lifecycleHook) {
                    lifecycleHook.resetCounter();
                }
                _lock.unlock();
            }
            if (wasVisible) {
                switch (operation) {
                    case ACTION_NATIVE_REPARENTING:
                        // trigger a resize/relayout and repaint to listener
                        sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED);
                        break;

                    case ACTION_NATIVE_CREATION:
                        // This may run on the new Display/Screen connection, hence a new EDT task
                        runOnEDTIfAvail(true, reparentActionRecreate);
                        break;

                    default:
                }
            }
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.reparent: END-X (" + getThreadName() + ") state " + getStateMaskString() +
                        ", windowHandle " + toHexString(windowHandle) +
                        ", parentWindowHandle " + toHexString(parentWindowHandle) +
                        ", parentWindow " + Display.hashCodeNullSafe(parentWindow) + " " +
                        getX() + "/" + getY() + " " + getWidth() + "x" + getHeight());
            }
        }
    }

    private class DecorationAction implements Runnable {
        final boolean undecorated;

        private DecorationAction(boolean undecorated) {
            this.undecorated = undecorated;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                if (stateMask.put(STATE_BIT_UNDECORATED, undecorated) != undecorated) {
                    if (isNativeValid() && !isFullscreen()) {
                        // Mirror pos/size so native change notification can get overwritten
                        int x = getX();
                        int y = getY();
                        int width = getWidth();
                        int height = getHeight();

                        DisplayImpl display = (DisplayImpl) screen.getDisplay();
                        display.dispatchMessagesNative(); // status up2date
                        reconfigureWindowImpl(x, y, width, height, getReconfigureMask(CHANGE_MASK_DECORATION, isVisible()));
                        display.dispatchMessagesNative(); // status up2date
                    }
                }
            } finally {
                _lock.unlock();
            }
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
        }
    }

    private class AlwaysOnTopAction implements Runnable {
        final boolean alwaysOnTop;

        private AlwaysOnTopAction(boolean alwaysOnTop) {
            this.alwaysOnTop = alwaysOnTop;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                if (stateMask.put(STATE_BIT_ALWAYSONTOP, alwaysOnTop) != alwaysOnTop) {
                    if (isNativeValid() && !isFullscreen()) {
                        // Mirror pos/size so native change notification can get overwritten
                        int x = getX();
                        int y = getY();
                        int width = getWidth();
                        int height = getHeight();

                        DisplayImpl display = (DisplayImpl) screen.getDisplay();
                        display.dispatchMessagesNative(); // status up2date
                        reconfigureWindowImpl(x, y, width, height, getReconfigureMask(CHANGE_MASK_ALWAYSONTOP, isVisible()));
                        display.dispatchMessagesNative(); // status up2date
                    }
                }
            } finally {
                _lock.unlock();
            }
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
        }
    }

    //
    // Reflection helper ..
    //

    private class AlwaysOnBottomAction implements Runnable {
        final boolean alwaysOnBottom;

        private AlwaysOnBottomAction(boolean alwaysOnBottom) {
            this.alwaysOnBottom = alwaysOnBottom;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                if (stateMask.put(STATE_BIT_ALWAYSONBOTTOM, alwaysOnBottom) != alwaysOnBottom) {
                    if (isNativeValid()) {
                        // Mirror pos/size so native change notification can get overwritten
                        int x = getX();
                        int y = getY();
                        int width = getWidth();
                        int height = getHeight();

                        DisplayImpl display = (DisplayImpl) screen.getDisplay();
                        display.dispatchMessagesNative(); // status up2date
                        reconfigureWindowImpl(x, y, width, height, getReconfigureMask(CHANGE_MASK_ALWAYSONBOTTOM, isVisible()));
                        display.dispatchMessagesNative(); // status up2date
                    }
                }
            } finally {
                _lock.unlock();
            }
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
        }
    }

    private class ResizableAction implements Runnable {
        final boolean resizable;

        private ResizableAction(boolean resizable) {
            this.resizable = resizable;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                if (stateMask.put(STATE_BIT_RESIZABLE, resizable) != resizable) {
                    if (isNativeValid()) {
                        // Mirror pos/size so native change notification can get overwritten
                        int x = getX();
                        int y = getY();
                        int width = getWidth();
                        int height = getHeight();

                        DisplayImpl display = (DisplayImpl) screen.getDisplay();
                        display.dispatchMessagesNative(); // status up2date
                        reconfigureWindowImpl(x, y, width, height, getReconfigureMask(CHANGE_MASK_RESIZABLE, isVisible()));
                        display.dispatchMessagesNative(); // status up2date
                    }
                }
            } finally {
                _lock.unlock();
            }
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
        }
    }

    private class StickyAction implements Runnable {
        final boolean sticky;

        private StickyAction(boolean sticky) {
            this.sticky = sticky;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                if (stateMask.put(STATE_BIT_STICKY, sticky) != sticky) {
                    if (isNativeValid()) {
                        // Mirror pos/size so native change notification can get overwritten
                        int x = getX();
                        int y = getY();
                        int width = getWidth();
                        int height = getHeight();

                        DisplayImpl display = (DisplayImpl) screen.getDisplay();
                        display.dispatchMessagesNative(); // status up2date
                        reconfigureWindowImpl(x, y, width, height, getReconfigureMask(CHANGE_MASK_STICKY, isVisible()));
                        display.dispatchMessagesNative(); // status up2date
                    }
                }
            } finally {
                _lock.unlock();
            }
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
        }
    }

    private class MaximizeAction implements Runnable {
        final boolean horz;
        final boolean vert;

        private MaximizeAction(boolean horz, boolean vert) {
            this.horz = horz;
            this.vert = vert;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                int cmask = 0;
                if (stateMask.put(STATE_BIT_MAXIMIZED_VERT, vert) != vert) {
                    cmask |= CHANGE_MASK_MAXIMIZED_VERT;
                }
                if (stateMask.put(STATE_BIT_MAXIMIZED_HORZ, horz) != horz) {
                    cmask |= CHANGE_MASK_MAXIMIZED_HORZ;
                }
                if (0 != cmask) {
                    if (isNativeValid()) {
                        boolean focused = hasFocus();
                        // Mirror pos/size so native change notification can get overwritten
                        int x = getX();
                        int y = getY();
                        int width = getWidth();
                        int height = getHeight();

                        DisplayImpl display = (DisplayImpl) screen.getDisplay();
                        display.dispatchMessagesNative(); // status up2date

                        reconfigureWindowImpl(x, y, width, height, getReconfigureMask(cmask, isVisible()));
                        display.dispatchMessagesNative(); // status up2date

                        if (focused) {
                            requestFocusInt(0 == parentWindowHandle /* skipFocusAction if top-level */);
                        }
                    }
                }
            } finally {
                _lock.unlock();
            }
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
        }
    }

    private class SetPositionAction implements Runnable {
        final int x;
        final int y;

        private SetPositionAction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            try {
                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window setPosition: " + getX() + "/" + getY() + " -> " + x + "/" + y + ", fs " + stateMask.get(STATE_BIT_FULLSCREEN) + ", windowHandle " + toHexString(windowHandle));
                }
                // Let the window be positioned if !fullscreen and position changed or being a child window.
                if (!isFullscreen() && (getX() != x || getY() != y || null != getParent())) {
                    if (isNativeValid()) {
                        // this.x/this.y will be set by sizeChanged, triggered by windowing event system
                        reconfigureWindowImpl(x, y, getWidth(), getHeight(), getReconfigureMask(0, isVisible()));
                        if (null == parentWindow) {
                            // Wait until custom position is reached within tolerances
                            waitForPosition(true, x, y, Window.TIMEOUT_NATIVEWINDOW);
                        }
                    } else {
                        definePosition(x, y); // set pos for createNative(..)
                    }
                }
            } finally {
                _lock.unlock();
            }
        }
    }

    private class FullScreenAction implements Runnable {
        boolean _fullscreen;

        private boolean init(boolean fullscreen) {
            if (isNativeValid()) {
                if (!isReconfigureMaskSupported(STATE_MASK_FULLSCREEN)) {
                    return false;
                } else {
                    this._fullscreen = fullscreen;
                    return isFullscreen() != fullscreen;
                }
            } else {
                stateMask.put(STATE_BIT_FULLSCREEN, fullscreen); // set current state for createNative(..)
                return false;
            }
        }

        boolean fsOn() {
            return _fullscreen;
        }

        @Override
        public final void run() {
            RecursiveLock _lock = windowLock;
            _lock.lock();
            blockInsetsChange = true;
            try {
                int oldX = getX();
                int oldY = getY();
                int oldWidth = getWidth();
                int oldHeight = getHeight();

                int x, y, w, h;

                RectangleImmutable sviewport = screen.getViewportInWindowUnits(); // window units
                RectangleImmutable viewport; // window units
                boolean alwaysOnTopChange, resizableChange;

                if (_fullscreen) {
                    if (null == fullscreenMonitors) {
                        if (stateMask.get(PSTATE_BIT_FULLSCREEN_MAINMONITOR)) {
                            fullscreenMonitors = new Lst<>();
                            fullscreenMonitors.add(getMainMonitor());
                        } else {
                            fullscreenMonitors = getScreen().getMonitorDevices();
                        }
                    }
                    {
                        Rectangle viewportInWindowUnits = new Rectangle();
                        MonitorDevice.unionOfViewports(null, viewportInWindowUnits, fullscreenMonitors);
                        viewport = viewportInWindowUnits;
                    }
                    if (isReconfigureMaskSupported(STATE_MASK_FULLSCREEN_SPAN) &&
                            (fullscreenMonitors.size() > 1 || sviewport.compareTo(viewport) > 0)) {
                        stateMask.set(STATE_BIT_FULLSCREEN_SPAN);
                    } else {
                        stateMask.clear(STATE_BIT_FULLSCREEN_SPAN);
                    }
                    nfs_x = oldX;
                    nfs_y = oldY;
                    nfs_width = oldWidth;
                    nfs_height = oldHeight;
                    stateMaskNFS.put32(0, 32, stateMask.get32(0, 32) & STATE_MASK_FULLSCREEN_NFS);
                    x = viewport.getX();
                    y = viewport.getY();
                    w = viewport.getWidth();
                    h = viewport.getHeight();
                    stateMask.clear(STATE_BIT_ALWAYSONTOP); // special aontop handling for fullscreen
                    stateMask.set(STATE_BIT_RESIZABLE);     // allow fullscreen to resize to max
                    alwaysOnTopChange = stateMaskNFS.get(STATE_BIT_ALWAYSONTOP);
                    resizableChange = !stateMaskNFS.get(STATE_BIT_RESIZABLE);
                } else {
                    int _x, _y, _w, _h;
                    stateMask.set(PSTATE_BIT_FULLSCREEN_MAINMONITOR);
                    fullscreenMonitors = null;
                    stateMask.clear(STATE_BIT_FULLSCREEN_SPAN);
                    viewport = null;
                    _x = nfs_x;
                    _y = nfs_y;
                    _w = nfs_width;
                    _h = nfs_height;
                    alwaysOnTopChange = stateMaskNFS.get(STATE_BIT_ALWAYSONTOP) != stateMask.get(STATE_BIT_ALWAYSONTOP);
                    resizableChange = stateMaskNFS.get(STATE_BIT_RESIZABLE) != stateMask.get(STATE_BIT_RESIZABLE);
                    stateMask.put32(0, 32, stateMaskNFS.get32(0, 32) | (stateMask.get32(0, 32) & ~STATE_MASK_FULLSCREEN_NFS));

                    if (null != parentWindow) {
                        // reset position to 0/0 within parent space
                        x = 0;
                        y = 0;

                        // refit if size is bigger than parent
                        w = Math.min(_w, parentWindow.getWidth());
                        h = Math.min(_h, parentWindow.getHeight());
                    } else {
                        x = _x;
                        y = _y;
                        w = _w;
                        h = _h;
                    }
                }

                DisplayImpl display = (DisplayImpl) screen.getDisplay();
                display.dispatchMessagesNative(); // status up2date
                boolean wasVisible = isVisible();
                boolean tempInvisible = !_fullscreen && wasVisible && NativeWindowFactory.TYPE_X11 == NativeWindowFactory.getNativeWindowType(true);

                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window " + x + "/" + y + " " + w + "x" + h +
                            ", virtl-screenSize: " + sviewport + " [wu], monitorsViewport " + viewport + " [wu]" +
                            ", wasVisible " + wasVisible + ", tempInvisible " + tempInvisible +
                            ", hasParent " + (null != parentWindow) +
                            ", state " + getStateMaskString() +
                            " @ " + Thread.currentThread().getName());
                }

                // fullscreen off: !visible first (fixes X11 unsuccessful return to parent window _and_ wrong window size propagation)
                if (tempInvisible) {
                    setVisibleImpl(false /* visible */, true /* fast */, oldX, oldY, oldWidth, oldHeight);
                    WindowImpl.this.waitForVisible(false, false);
                    //try { Thread.sleep(100); } catch (final InterruptedException e) { }
                    display.dispatchMessagesNative(); // status up2date
                }

                // Lock parentWindow only during reparenting (attempt)
                NativeWindow parentWindowLocked;
                if (null != parentWindow) {
                    parentWindowLocked = parentWindow;
                    if (NativeSurface.LOCK_SURFACE_NOT_READY >= parentWindowLocked.lockSurface()) {
                        throw new NativeWindowException("Parent surface lock: not ready: " + parentWindow);
                    }
                } else {
                    parentWindowLocked = null;
                }
                int changeMask;
                try {
                    {
                        // Enter fullscreen - Disable alwaysOnTop/resizableChange
                        int cm = 0;
                        if (alwaysOnTopChange) {
                            cm = CHANGE_MASK_ALWAYSONTOP;
                        }
                        if (resizableChange) {
                            cm |= CHANGE_MASK_RESIZABLE;
                        }
                        changeMask = cm;
                    }
                    if (_fullscreen && 0 != changeMask) {
                        // Enter fullscreen - Disable alwaysOnTop/resizableChange
                        reconfigureWindowImpl(oldX, oldY, oldWidth, oldHeight, getReconfigureMask(changeMask, isVisible()));
                    }

                    stateMask.put(STATE_BIT_FULLSCREEN, _fullscreen);
                    // Note CHANGE_MASK_PARENTING: STATE_MASK_CHILDWIN is refined in getReconfigureMask()
                    // Note CHANGE_MASK_DECORATION: STATE_MASK_UNDECORATED is refined in getReconfigureMask()
                    reconfigureWindowImpl(x, y, w, h,
                            getReconfigureMask(((null != parentWindowLocked) ? CHANGE_MASK_PARENTING : 0) |
                                    CHANGE_MASK_FULLSCREEN | CHANGE_MASK_DECORATION, isVisible()));
                } finally {
                    if (null != parentWindowLocked) {
                        parentWindowLocked.unlockSurface();
                    }
                }
                display.dispatchMessagesNative(); // status up2date

                if (wasVisible) {
                    if (NativeWindowFactory.TYPE_X11 == NativeWindowFactory.getNativeWindowType(true)) {
                        // Give sluggy WM's (e.g. Unity) a chance to properly restore window ..
                        //try { Thread.sleep(100); } catch (final InterruptedException e) { }
                        display.dispatchMessagesNative(); // status up2date
                    }
                    setVisibleImpl(true /* visible */, true /* fast */, x, y, w, h);
                    boolean ok = 0 <= WindowImpl.this.waitForVisible(true, false);
                    if (ok) {
                        ok = WindowImpl.this.waitForSize(w, h, false, TIMEOUT_NATIVEWINDOW);
                    }
                    if (ok && !_fullscreen && null == parentWindow) {
                        // Position mismatch shall not lead to fullscreen failure
                        WindowImpl.this.waitForPosition(true, x, y, TIMEOUT_NATIVEWINDOW);
                    }
                    if (ok) {
                        // Restore certain states ..
                        if (!_fullscreen && 0 != changeMask) {
                            // Restore alwaysOnTop/resizableChange when leaving fullscreen
                            reconfigureWindowImpl(x, y, w, h, getReconfigureMask(changeMask, isVisible()));
                        }
                        if (isAlwaysOnBottom()) {
                            // Re-Init alwaysOnBottom always
                            reconfigureWindowImpl(x, y, w, h, getReconfigureMask(CHANGE_MASK_ALWAYSONBOTTOM, isVisible()));
                        }
                        if (isSticky()) {
                            // Re-Init sticky always
                            reconfigureWindowImpl(x, y, w, h, getReconfigureMask(CHANGE_MASK_STICKY, isVisible()));
                        }
                    }
                    if (ok) {
                        requestFocusInt(_fullscreen /* skipFocusAction if fullscreen */);
                        display.dispatchMessagesNative(); // status up2date
                    }
                    if (DEBUG_IMPLEMENTATION) {
                        System.err.println("Window fs done: ok " + ok + ", " + WindowImpl.this);
                    }
                }
            } finally {
                blockInsetsChange = false;
                _lock.unlock();
            }
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
        }
    }

    private class MonitorModeListenerImpl implements MonitorModeListener {
        boolean animatorPaused = false;
        boolean hidden = false;
        boolean hadFocus = false;
        boolean fullscreenPaused = false;
        List<MonitorDevice> _fullscreenMonitors;
        boolean _fullscreenUseMainMonitor = true;

        @Override
        public void monitorModeChangeNotify(MonitorEvent me) {
            hadFocus = hasFocus();
            boolean fullscreen = stateMask.get(STATE_BIT_FULLSCREEN);
            boolean isOSX = NativeWindowFactory.TYPE_MACOSX == NativeWindowFactory.getNativeWindowType(true);
            boolean quirkFSPause = fullscreen && isReconfigureMaskSupported(STATE_MASK_FULLSCREEN_SPAN);
            boolean quirkHide = !quirkFSPause && !fullscreen && isVisible() && isOSX;
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.monitorModeChangeNotify: hadFocus " + hadFocus + ", qFSPause " + quirkFSPause + ", qHide " + quirkHide + ", " + me + " @ " + Thread.currentThread().getName());
            }

            if (null != lifecycleHook) {
                animatorPaused = lifecycleHook.pauseRenderingAction();
            }
            if (quirkFSPause) {
                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window.monitorModeChangeNotify: FS Pause");
                }
                fullscreenPaused = true;
                _fullscreenMonitors = fullscreenMonitors;
                _fullscreenUseMainMonitor = stateMask.get(PSTATE_BIT_FULLSCREEN_MAINMONITOR);
                setFullscreenImpl(false, true, null);
            }
            if (quirkHide) {
                // hiding & showing the window around mode-change solves issues w/ OSX,
                // where the content would be black until a resize.
                hidden = true;
                WindowImpl.this.setVisible(false);
            }
        }

        @Override
        public void monitorModeChanged(MonitorEvent me, boolean success) {
            if (!animatorPaused && success && null != lifecycleHook) {
                // Didn't pass above notify method. probably detected screen change after it happened.
                animatorPaused = lifecycleHook.pauseRenderingAction();
            }
            boolean fullscreen = stateMask.get(STATE_BIT_FULLSCREEN);
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.monitorModeChanged.0: success: " + success + ", hadFocus " + hadFocus + ", animPaused " + animatorPaused +
                        ", hidden " + hidden + ", FS " + fullscreen + ", FS-paused " + fullscreenPaused +
                        " @ " + Thread.currentThread().getName());
                System.err.println("Window.monitorModeChanged.0: " + getScreen());
                System.err.println("Window.monitorModeChanged.0: " + me);
            }
            WindowImpl.this.monitorModeChanged(me, success);

            if (success && !fullscreen && !fullscreenPaused) {
                // Simply move/resize window to fit in virtual screen if required
                RectangleImmutable viewport = screen.getViewportInWindowUnits();
                if (viewport.getWidth() > 0 && viewport.getHeight() > 0) { // failsafe
                    RectangleImmutable rect = new Rectangle(getX(), getY(), getWidth(), getHeight());
                    RectangleImmutable isect = viewport.intersection(rect);
                    if (getHeight() > isect.getHeight() ||
                            getWidth() > isect.getWidth()) {
                        if (DEBUG_IMPLEMENTATION) {
                            System.err.println("Window.monitorModeChanged.1: Non-FS - Fit window " + rect + " into screen viewport " + viewport +
                                    ", due to minimal intersection " + isect);
                        }
                        definePosition(viewport.getX(), viewport.getY()); // set pos for setVisible(..) or createNative(..) - reduce EDT roundtrip
                        setSize(viewport.getWidth(), viewport.getHeight(), true /* force */);
                    }
                }
            } else if (fullscreenPaused) {
                if (DEBUG_IMPLEMENTATION) {
                    System.err.println("Window.monitorModeChanged.2: FS Restore");
                }
                setFullscreenImpl(true, _fullscreenUseMainMonitor, _fullscreenMonitors);
                fullscreenPaused = false;
                _fullscreenMonitors = null;
                _fullscreenUseMainMonitor = true;
            } else if (success && null != fullscreenMonitors) {
                // If changed monitor is part of this fullscreen mode, reset size! (Bug 771)
                MonitorDevice md = me.getMonitor();
                if (fullscreenMonitors.contains(md)) {
                    Rectangle viewportInWindowUnits = new Rectangle();
                    MonitorDevice.unionOfViewports(null, viewportInWindowUnits, fullscreenMonitors);
                    if (DEBUG_IMPLEMENTATION) {
                        RectangleImmutable winBounds = WindowImpl.this.getBounds();
                        System.err.println("Window.monitorModeChanged.3: FS Monitor Match: Fit window " + winBounds + " into new viewport union " + viewportInWindowUnits + " [window], provoked by " + md);
                    }
                    definePosition(viewportInWindowUnits.getX(), viewportInWindowUnits.getY()); // set pos for setVisible(..) or createNative(..) - reduce EDT roundtrip
                    setSize(viewportInWindowUnits.getWidth(), viewportInWindowUnits.getHeight(), true /* force */);
                }
            }
            if (hidden) {
                WindowImpl.this.setVisible(true);
                hidden = false;
            }
            sendWindowEvent(WindowEvent.EVENT_WINDOW_RESIZED); // trigger a resize/relayout and repaint to listener
            if (animatorPaused) {
                lifecycleHook.resumeRenderingAction();
            }
            if (hadFocus) {
                requestFocus(true);
            }
            if (DEBUG_IMPLEMENTATION) {
                System.err.println("Window.monitorModeChanged.X: @ " + Thread.currentThread().getName() + ", this: " + WindowImpl.this);
            }
        }
    }
}