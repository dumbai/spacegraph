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

package jogamp.newt;

import com.jogamp.common.ExceptionUtils;
import com.jogamp.common.nio.Buffers;
import com.jogamp.common.util.IOUtil;
import com.jogamp.common.util.InterruptedRuntimeException;
import com.jogamp.common.util.ReflectionUtil;
import com.jogamp.nativewindow.AbstractGraphicsDevice;
import com.jogamp.nativewindow.NativeWindowException;
import com.jogamp.nativewindow.NativeWindowFactory;
import com.jogamp.nativewindow.util.*;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.event.NEWTEventConsumer;
import com.jogamp.newt.util.EDTUtil;
import com.jogamp.opengl.util.PNGPixelRect;
import jcog.data.list.Lst;
import jogamp.newt.event.NEWTEventTask;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DisplayImpl extends Display {
    private static final AtomicInteger serialno = new AtomicInteger(1);
    private static final boolean pngUtilAvail;

    static {
        NativeWindowFactory.addCustomShutdownHook(true /* head */, () -> {
            WindowImpl.shutdownAll();
            ScreenImpl.shutdownAll();
            DisplayImpl.shutdownAll();
        });

        ClassLoader cl = DisplayImpl.class.getClassLoader();
        pngUtilAvail = ReflectionUtil.isClassAvailable("com.jogamp.opengl.util.PNGPixelRect", cl);
    }

    public static boolean isPNGUtilAvailable() { return pngUtilAvail; }

    private final List<PointerIconImpl> pointerIconList = new Lst<>();

    /** Executed from EDT! */
    private void destroyAllPointerIconFromList(long dpy) {
        synchronized(pointerIconList) {
            int count = pointerIconList.size();
            for( int i=0; i < count; i++ ) {
                PointerIconImpl item = pointerIconList.get(i);
                if(DEBUG) {
                    System.err.println("destroyAllPointerIconFromList: dpy "+toHexString(dpy)+", # "+i+"/"+count+": "+item+" @ "+getThreadName());
                }
                if( null != item && item.isValid() ) {
                    item.destroyOnEDT(dpy);
                }
            }
            pointerIconList.clear();
        }
    }

    @Override
    public PixelFormat getNativePointerIconPixelFormat() { return PixelFormat.BGRA8888; }
    @Override
    public boolean getNativePointerIconForceDirectNIO() { return false; }

    @Override
    public final PointerIcon createPointerIcon(IOUtil.ClassResources pngResource, int hotX, int hotY)
            throws IllegalArgumentException, IllegalStateException, IOException
    {
        if( null == pngResource || 0 >= pngResource.resourceCount() ) {
            throw new IllegalArgumentException("Null or invalid pngResource "+pngResource);
        }
        if( !pngUtilAvail ) {
            return null;
        }
        PointerIconImpl[] res = { null };
        Exception[] ex = { null };
        String exStr = "Could not resolve "+pngResource.resourcePaths[0];
        runOnEDTIfAvail(true, () -> {
            try {
                if( !DisplayImpl.this.isNativeValidAsync() ) {
                    throw new IllegalStateException("Display.createPointerIcon: Display invalid "+DisplayImpl.this);
                }
                URLConnection urlConn = pngResource.resolve(0);
                if( null == urlConn ) {
                    throw new IOException(exStr);
                }
                PNGPixelRect image = PNGPixelRect.read(urlConn.getInputStream(),
                                                             getNativePointerIconPixelFormat(),
                                                             getNativePointerIconForceDirectNIO(),
                                                             0 /* destMinStrideInBytes */, false /* destIsGLOriented */);
                long handle = createPointerIconImplChecked(image.getPixelformat(), image.getSize().getWidth(), image.getSize().getHeight(),
                                                                 image.getPixels(), hotX, hotY);
                PointImmutable hotspot = new Point(hotX, hotY);
                if( DEBUG_POINTER_ICON ) {
                    System.err.println("createPointerIconPNG.0: "+image+", handle: "+toHexString(handle)+", hot "+hotspot);
                }
                if( 0 == handle ) {
                    throw new IOException(exStr);
                }
                res[0] = new PointerIconImpl(DisplayImpl.this, image, hotspot, handle);
                if( DEBUG_POINTER_ICON ) {
                    System.err.println("createPointerIconPNG.0: "+res[0]);
                }
            } catch (Exception e) {
                ex[0] = e;
            }
        });
        if( null != ex[0] ) {
            Exception e = ex[0];
            if( e instanceof IllegalArgumentException) {
                throw new IllegalArgumentException(e);
            }
            if( e instanceof IllegalStateException) {
                throw new IllegalStateException(e);
            }
            throw new IOException(e);
        }
        if( null == res[0] ) {
            throw new IOException(exStr);
        }
        synchronized(pointerIconList) {
            pointerIconList.add(res[0]);
        }
        return res[0];
    }

    @Override
    public final PointerIcon createPointerIcon(PixelRectangle pixelrect, int hotX, int hotY)
            throws IllegalArgumentException, IllegalStateException
    {
        if( null == pixelrect ) {
            throw new IllegalArgumentException("Null or pixelrect");
        }
        PixelRectangle fpixelrect;
        if( getNativePointerIconPixelFormat() != pixelrect.getPixelformat() || pixelrect.isGLOriented() ) {
            // conversion !
            fpixelrect = PixelFormatUtil.convert(pixelrect, getNativePointerIconPixelFormat(),
                                                      0 /* ddestStride */, false /* isGLOriented */, getNativePointerIconForceDirectNIO() );
            if( DEBUG_POINTER_ICON ) {
                System.err.println("createPointerIconRES.0: Conversion-FMT "+pixelrect+" -> "+fpixelrect);
            }
        } else if( getNativePointerIconForceDirectNIO() && !Buffers.isDirect(pixelrect.getPixels()) ) {
            // transfer to direct NIO
            ByteBuffer sBB = pixelrect.getPixels();
            ByteBuffer dBB = Buffers.newDirectByteBuffer(sBB.array(), sBB.arrayOffset());
            fpixelrect = new PixelRectangle.GenericPixelRect(pixelrect.getPixelformat(), pixelrect.getSize(), pixelrect.getStride(), pixelrect.isGLOriented(), dBB);
            if( DEBUG_POINTER_ICON ) {
                System.err.println("createPointerIconRES.0: Conversion-NIO "+pixelrect+" -> "+fpixelrect);
            }
        } else {
            fpixelrect = pixelrect;
            if( DEBUG_POINTER_ICON ) {
                System.err.println("createPointerIconRES.0: No conversion "+fpixelrect);
            }
        }
        PointerIconImpl[] res = { null };
        runOnEDTIfAvail(true, () -> {
            try {
                if( !DisplayImpl.this.isNativeValidAsync() ) {
                    throw new IllegalStateException("Display.createPointerIcon: Display invalid "+DisplayImpl.this);
                }
                if( null != fpixelrect ) {
                long handle = createPointerIconImplChecked(fpixelrect.getPixelformat(),
                                                                 fpixelrect.getSize().getWidth(),
                                                                 fpixelrect.getSize().getHeight(),
                                                                 fpixelrect.getPixels(), hotX, hotY);
                if( 0 != handle ) {
                    res[0] = new PointerIconImpl(DisplayImpl.this, fpixelrect, new Point(hotX, hotY), handle);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        if( null != res[0] ) {
            synchronized(pointerIconList) {
                pointerIconList.add(res[0]);
            }
        }
        return res[0];
    }

    /**
     * Executed from EDT!
     *
     * @param pixelformat the <code>pixels</code>'s format
     * @param width the <code>pixels</code>'s width
     * @param height the <code>pixels</code>'s height
     * @param pixels the <code>pixels</code>
     * @param hotX the PointerIcon's hot-spot x-coord
     * @param hotY the PointerIcon's hot-spot x-coord
     * @return if successful a valid handle (not null), otherwise null.
     */
    protected final long createPointerIconImplChecked(PixelFormat pixelformat, int width, int height, ByteBuffer pixels, int hotX, int hotY) {
        if( getNativePointerIconPixelFormat() != pixelformat ) {
            throw new IllegalArgumentException("Pixelformat no "+getNativePointerIconPixelFormat()+", but "+pixelformat);
        }
        if( getNativePointerIconForceDirectNIO() && !Buffers.isDirect(pixels) ) {
            throw new IllegalArgumentException("pixel buffer is not direct "+pixels);
        }
        return createPointerIconImpl(pixelformat, width, height, pixels, hotX, hotY);
    }

    /**
     * Executed from EDT!
     *
     * @param pixelformat the <code>pixels</code>'s format
     * @param width the <code>pixels</code>'s width
     * @param height the <code>pixels</code>'s height
     * @param pixels the <code>pixels</code>
     * @param hotX the PointerIcon's hot-spot x-coord
     * @param hotY the PointerIcon's hot-spot x-coord
     * @return if successful a valid handle (not null), otherwise null.
     */
    protected long createPointerIconImpl(PixelFormat pixelformat, int width, int height, ByteBuffer pixels, int hotX, int hotY) {
        return 0;
    }

    /** Executed from EDT! */
    protected void destroyPointerIconImpl(long displayHandle, long piHandle) { }

    /** Ensure static init has been run. */
    /* pp */static void initSingleton() { }

    private static Class<?> getDisplayClass(String type)
        throws ClassNotFoundException
    {
        Class<?> displayClass = NewtFactory.getCustomClass(type, "DisplayDriver");
        if(null==displayClass) {
            throw new ClassNotFoundException("Failed to find NEWT Display Class <"+type+".DisplayDriver>");
        }
        return displayClass;
    }

    /** Make sure to reuse a Display with the same name */
    public static Display create(String type, String name, long handle, boolean reuse) {
        try {
            Class<?> displayClass = getDisplayClass(type);
            DisplayImpl display = (DisplayImpl) displayClass.getConstructor().newInstance();
            name = display.validateDisplayName(name, handle);
            synchronized(displayList) {
                if(reuse) {
                    Display display0 = Display.getLastDisplayOf(type, name, -1, true /* shared only */);
                    if(null != display0) {
                        if(DEBUG) {
                            System.err.println("Display.create() REUSE: "+display0+" "+getThreadName());
                        }
                        return display0;
                    }
                }
                display.exclusive = !reuse;
                display.name = name;
                display.type=type;
                display.refCount=0;
                display.id = serialno.getAndIncrement();
                display.fqname = getFQName(display.type, display.name, display.id);
                display.hashCode = display.fqname.hashCode();
                display.setEDTUtil( display.edtUtil ); // device's default if EDT is used, or null
                Display.addDisplay2List(display);
            }

            if(DEBUG) {
                System.err.println("Display.create() NEW: "+display+" "+getThreadName());
            }
            return display;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DisplayImpl other = (DisplayImpl) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.type, other.type);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public final synchronized void createNative()
        throws NativeWindowException
    {
        if( null == aDevice ) {
            if(DEBUG) {
                System.err.println("Display.createNative() START ("+getThreadName()+", "+this+")");
            }
            DisplayImpl f_dpy = this;
            try {
                runOnEDTIfAvail(true, f_dpy::createNativeImpl);
            } catch (Throwable t) {
                throw new NativeWindowException(t);
            }
            if( null == aDevice ) {
                throw new NativeWindowException("Display.createNative() failed to instanciate an AbstractGraphicsDevice");
            }
            synchronized(displayList) {
                displaysActive++;
                if(DEBUG) {
                    System.err.println("Display.createNative() END ("+getThreadName()+", "+this+", active "+displaysActive+")");
                }
            }
        }
    }

    protected EDTUtil createEDTUtil() {
        EDTUtil def;
        if(NewtFactory.useEDT()) {
            def = new DefaultEDTUtil(Thread.currentThread().getThreadGroup(), "Display-"+getFQName(), dispatchMessagesRunnable);
            if(DEBUG) {
                System.err.println("Display.createEDTUtil("+getFQName()+"): "+def.getClass().getName());
            }
        } else {
            def = null;
        }
        return def;
    }

    @Override
    public synchronized EDTUtil setEDTUtil(EDTUtil usrEDTUtil) {
        EDTUtil oldEDTUtil = edtUtil;
        if( null != usrEDTUtil && usrEDTUtil == oldEDTUtil ) {
            if( DEBUG ) {
                System.err.println("Display.setEDTUtil: "+usrEDTUtil+" - keep!");
            }
            return oldEDTUtil;
        }
        if(DEBUG) {
            String msg = ( null == usrEDTUtil ) ? "default" : "custom";
            System.err.println("Display.setEDTUtil("+msg+"): "+oldEDTUtil+" -> "+usrEDTUtil);
        }
        stopEDT( oldEDTUtil, null );
        edtUtil = ( null == usrEDTUtil ) ? createEDTUtil() : usrEDTUtil;
        return oldEDTUtil;
    }

    @Override
    public final EDTUtil getEDTUtil() {
        return edtUtil;
    }

    private static void stopEDT(EDTUtil edtUtil, Runnable task) {
        if( null != edtUtil ) {
            if( edtUtil.isRunning() ) {
                boolean res = edtUtil.invokeStop(true, task);
                if( DEBUG ) {
                    if ( !res ) {
//                        System.err.println("Warning: invokeStop() failed");
                        ExceptionUtils.dumpStack(System.err);
                    }
                }
            }
            edtUtil.waitUntilStopped();
            // ready for restart ..
        } else if( null != task ) {
            task.run();
        }
    }

    public void runOnEDTIfAvail(boolean wait, Runnable task) {
        EDTUtil _edtUtil = edtUtil;
        if( !_edtUtil.isRunning() ) { // start EDT if not running yet
            synchronized( this ) {
                if( !_edtUtil.isRunning() ) { // // volatile dbl-checked-locking OK
                    if( DEBUG ) {
//                        System.err.println("Info: EDT start "+Thread.currentThread().getName()+", "+this);
                        ExceptionUtils.dumpStack(System.err);
                    }
                    _edtUtil.start();
                }
            }
        }
        if( !_edtUtil.isCurrentThreadEDT() ) {
            if( _edtUtil.invoke(wait, task) ) {
                return; // done
            }
            if( DEBUG ) {
//                System.err.println("Warning: invoke(wait "+wait+", ..) on EDT failed .. invoke on current thread "+Thread.currentThread().getName());
                ExceptionUtils.dumpStack(System.err);
            }
        }
        task.run();
    }

    @Override
    public boolean validateEDTStopped() {
        if( 0==refCount && null == aDevice ) {
            EDTUtil _edtUtil = edtUtil;
            if( null != _edtUtil && _edtUtil.isRunning() ) {
                synchronized( this ) {
                    if( null != edtUtil && edtUtil.isRunning() ) { // // volatile dbl-checked-locking OK
                        stopEDT( edtUtil, null );
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public final synchronized void destroy() {
        if(DEBUG) {
            dumpDisplayList("Display.destroy("+getFQName()+") BEGIN");
        }
        synchronized(displayList) {
            if(0 < displaysActive) {
                displaysActive--;
            }
            if(DEBUG) {
                System.err.println("Display.destroy(): "+this+", active "+displaysActive+" "+getThreadName());
            }
        }
        DisplayImpl f_dpy = this;
        AbstractGraphicsDevice f_aDevice = aDevice;
        aDevice = null;
        refCount=0;
        // blocks!
        stopEDT( edtUtil, () -> {
            if ( null != f_aDevice ) {
                f_dpy.destroyAllPointerIconFromList(f_aDevice.getHandle());
                f_dpy.closeNativeImpl(f_aDevice);
            }
        });
        if(DEBUG) {
            dumpDisplayList("Display.destroy("+getFQName()+") END");
        }
    }

    /** May be utilized at a shutdown hook, impl. does not block. */
    /* pp */ private static void shutdownAll() {
        int dCount = displayList.size();
        if(DEBUG) {
            dumpDisplayList("Display.shutdownAll "+dCount+" instances, on thread "+getThreadName());
        }
        for(int i = 0; i<dCount && !displayList.isEmpty(); i++) { // be safe ..
            DisplayImpl d = (DisplayImpl) displayList.remove(0).get();
            if(DEBUG) {
                System.err.println("Display.shutdownAll["+(i+1)+"/"+dCount+"]: "+d+", GCed "+(null==d));
            }
            if( null != d ) { // GC'ed ?
                if(0 < displaysActive) {
                    displaysActive--;
                }
                EDTUtil edtUtil = d.getEDTUtil();
                AbstractGraphicsDevice f_aDevice = d.aDevice;
                d.aDevice = null;
                d.refCount=0;
                Runnable closeNativeTask = () -> {
                    if ( null != d.getGraphicsDevice() ) {
                        d.destroyAllPointerIconFromList(f_aDevice.getHandle());
                        d.closeNativeImpl(f_aDevice);
                    }
                };
                if(null != edtUtil) {
                    long coopSleep = edtUtil.getPollPeriod() * 2;
                    if( edtUtil.isRunning() ) {
                        edtUtil.invokeStop(false, closeNativeTask); // don't block
                    }
                    try {
                        Thread.sleep( coopSleep < 50 ? coopSleep : 50 );
                    } catch (InterruptedException e) { }
                } else {
                    closeNativeTask.run();
                }
            }
        }
    }

    @Override
    public final synchronized int addReference() {
        if(DEBUG) {
            System.err.println("Display.addReference() ("+Display.getThreadName()+"): "+refCount+" -> "+(refCount+1));
        }
        if ( 0 == refCount ) {
            createNative();
        }
        if(null == aDevice) {
            throw new NativeWindowException ("Display.addReference() (refCount "+refCount+") null AbstractGraphicsDevice");
        }
        return refCount++;
    }


    @Override
    public final synchronized int removeReference() {
        if(DEBUG) {
            System.err.println("Display.removeReference() ("+Display.getThreadName()+"): "+refCount+" -> "+(refCount-1));
        }
        refCount--; // could become < 0, in case of manual destruction without actual creation/addReference
        if(0>=refCount) {
            destroy();
            refCount=0; // fix < 0
        }
        return refCount;
    }

    @Override
    public final synchronized int getReferenceCount() {
        return refCount;
    }

    protected abstract void createNativeImpl();
    protected abstract void closeNativeImpl(AbstractGraphicsDevice aDevice);

    @Override
    public final int getId() {
        return id;
    }

    @Override
    public final String getType() {
        return type;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getFQName() {
        return fqname;
    }

    @Override
    public final boolean isExclusive() {
        return exclusive;
    }

    public static final String nilString = "nil" ;

    public String validateDisplayName(String name, long handle) {
        if(null==name && 0!=handle) {
            name="wrapping-"+toHexString(handle);
        }
        return ( null == name ) ? nilString : name ;
    }

    private static String getFQName(String type, String name, int id) {
        if(null==type) type=nilString;
        if(null==name) name=nilString;
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append("_");
        sb.append(name);
        sb.append("-");
        sb.append(id);
        return sb.toString();
    }

    @Override
    public final long getHandle() {
        if(null!=aDevice) {
            return aDevice.getHandle();
        }
        return 0;
    }

    @Override
    public final AbstractGraphicsDevice getGraphicsDevice() {
        return aDevice;
    }

    @Override
    public final synchronized boolean isNativeValid() {
        return null != aDevice;
    }
    protected final boolean isNativeValidAsync() {
        return null != aDevice;
    }

    @Override
    public boolean isEDTRunning() {
        EDTUtil _edtUtil = edtUtil;
        if( null != _edtUtil ) {
            return _edtUtil.isRunning();
        }
        return false;
    }

    @Override
    public String toString() {
        EDTUtil _edtUtil = edtUtil;
        boolean _edtUtilRunning = null != _edtUtil && _edtUtil.isRunning();
        return "NEWT-Display["+getFQName()+", excl "+exclusive+", refCount "+refCount+", hasEDT "+(null!=_edtUtil)+", edtRunning "+_edtUtilRunning+", "+aDevice+"]";
    }

    /** Dispatch native Toolkit messageges */
    protected abstract void dispatchMessagesNative();


    private final AtomicReference<ArrayList<NEWTEventTask>> events = new AtomicReference(new ArrayList<>(1));
//    private volatile boolean haveEvents = false;

    protected final Runnable dispatchMessagesRunnable = DisplayImpl.this::dispatchMessages;

    final void dispatchMessage(NEWTEvent event) {
        try {
            Object source = event.getSource();
            if(source instanceof final NEWTEventConsumer consumer) {
                if(!consumer.consumeEvent(event)) {
                    // enqueue for later execution
                    enqueueEvent(false, event);
                }
            } else {
                throw new RuntimeException("Event source not NEWT: "+source.getClass().getName()+", "+source);
            }
        } catch (Throwable t) {
            RuntimeException re;
            if(t instanceof RuntimeException) {
                re = (RuntimeException) t;
            } else {
                re = new RuntimeException(t);
            }
            throw re;
        }
    }

    final void dispatchMessage(NEWTEventTask eventTask) {
        NEWTEvent event = eventTask.get();
        try {
            if(null == event) {
                // Ooops ?
                System.err.println("Warning: event of eventTask is NULL");
                ExceptionUtils.dumpStack(System.err);
                return;
            }
            dispatchMessage(event);
        } catch (RuntimeException re) {
            if( eventTask.isCallerWaiting() ) {
                // propagate exception to caller
                eventTask.setException(re);
            } else {
                throw re;
            }
        } finally {
            eventTask.notifyCaller();
        }
    }

    @Override
    public void dispatchMessages() {
        // System.err.println("Display.dispatchMessages() 0 "+this+" "+getThreadName());
        if(0==refCount || // no screens
           null==getGraphicsDevice() // no native device
          )
        {
            return;
        }


        boolean haveEvents = !events.getOpaque().isEmpty();
        if(haveEvents) { // volatile: ok
            // swap events list to free ASAP
            ArrayList<NEWTEventTask> _events = this.events.getAndSet(new ArrayList<>(1));
            if (_events.isEmpty())
                haveEvents = false; //no, actually there arent any
            //eventsLock.notifyAll();
            if( haveEvents ) {
                for (NEWTEventTask e : _events) {
                    if (!e.isDispatched())
                        dispatchMessage(e);
                }
            }
        }

        // System.err.println("Display.dispatchMessages() NATIVE "+this+" "+getThreadName());
        dispatchMessagesNative();
    }

    public void enqueueEvent(boolean wait, NEWTEvent e) {
        EDTUtil _edtUtil = edtUtil;
        if( !_edtUtil.isRunning() ) {
            // oops .. we are already dead
//            if(DEBUG) {
//                System.err.println("Warning: EDT already stopped: wait:="+wait+", "+e);
//                ExceptionUtils.dumpStack(System.err);
//            }
            return;
        }

        // can't wait if we are on EDT or NEDT -> consume right away
        if(wait && _edtUtil.isCurrentThreadEDTorNEDT() ) {
            dispatchMessage(e);
        } else {

            Object lock = new Object();
            NEWTEventTask eTask = new NEWTEventTask(e, wait ? lock : null);
            synchronized (lock) {
                //synchronized (eventsLock) {
                    events.get().add(eTask);
//                    haveEvents = true;
                    //eventsLock.notifyAll();
                //}
                while (wait && !eTask.isDispatched()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ie) {
                        eTask.setDispatched(); // Cancels NEWTEvent ..
                        throw new InterruptedRuntimeException(ie);
                    }
                    RuntimeException ee = eTask.getException();
                    if (null != ee)
                        throw ee;
                }
            }
        }
    }

    public interface DisplayRunnable<T> {
        T run(long dpy);
    }
    public static <T> T runWithLockedDevice(AbstractGraphicsDevice device, DisplayRunnable<T> action) {
        T res;
        device.lock();
        try {
            res = action.run(device.getHandle());
        } finally {
            device.unlock();
        }
        return res;
    }
    public final <T> T runWithLockedDisplayDevice(DisplayRunnable<T> action) {
        AbstractGraphicsDevice device = getGraphicsDevice();
        if(null == device) {
            throw new RuntimeException("null device - not initialized: "+this);
        }
        return runWithLockedDevice(device, action);
    }

    protected volatile EDTUtil edtUtil;
    protected int id;
    protected String name;
    protected String type;
    protected String fqname;
    protected int hashCode;
    protected int refCount; // number of Display references by Screen
    protected boolean exclusive; // do not share this display, uses NullLock!
    protected AbstractGraphicsDevice aDevice;
}