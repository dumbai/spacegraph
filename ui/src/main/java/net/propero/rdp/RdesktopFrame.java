/* RdesktopFrame.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Window for RDP session
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

import net.propero.rdp.keymapping.KeyCode_FileBased;
import net.propero.rdp.menu.RdpMenu;
import net.propero.rdp.rdp5.cliprdr.ClipChannel;

import java.awt.*;
import java.awt.event.*;



public abstract class RdesktopFrame extends Frame {

    public final RdesktopCanvas canvas;

    private Rdp rdp;

    private RdpMenu menu;
    boolean inFullscreen;
    private boolean menuVisible;

    /**
     * Create a new RdesktopFrame. Size defined by Options.width and
     * Options.height Creates RdesktopCanvas occupying entire frame
     */
    RdesktopFrame() {
        
        String java_version = System.getProperty("java.specification.version");
        if (java_version.compareTo("1.6") == 0) {
            this.setSize(Options.width + 6, Options.height + 30);
        }

        Common.frame = this;
        this.canvas = new RdesktopCanvas_Localised(Options.width,
                Options.height);
        add(this.canvas);
        setTitle(Options.windowTitle);

        if (Constants.OS == Constants.WINDOWS)
            setResizable(false);
        
        

        if (Options.fullscreen) {
            goFullScreen();
            pack();
            setLocation(0, 0);
        } else {
            pack();
            centreWindow();
        }

        if (Constants.OS != Constants.WINDOWS)
            setResizable(false);
        

        addWindowListener(new RdesktopWindowAdapter());
        canvas.addFocusListener(new RdesktopFocusListener());
        if (Constants.OS == Constants.WINDOWS) {
            
            addComponentListener(new RdesktopComponentAdapter());
        }

        canvas.requestFocus();
    }

    /**
     * Centre a window to the screen
     *
     * @param f Window to be centred
     */
    private static void centreWindow(Window f) {
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window_size = f.getSize();
        int x = (screen_size.width - window_size.width) / 2;
        if (x < 0)
            x = 0; 
        int y = (screen_size.height - window_size.height) / 2;
        if (y < 0)
            y = 0; 
        f.setLocation(x, y);
    }

    /**
     * Register the clipboard channel
     *
     * @param c ClipChannel object for controlling clipboard mapping
     */
    public void setClip(ClipChannel c) {
        canvas.addFocusListener(c);
    }

    @Override
    public boolean action(Event event, Object arg) {
        if (menu != null)
            return menu.action(event, arg);
        return false;
    }

    /**
     * Switch to fullscreen mode
     */
    public void goFullScreen() {
        inFullscreen = true;
    }

    /**
     * Exit fullscreen mode
     */
    public void leaveFullScreen() {
        inFullscreen = false;
    }

    /**
     * Switch in/out of fullscreen mode
     */
    public void toggleFullScreen() {
        if (inFullscreen)
            leaveFullScreen();
        else
            goFullScreen();
    }

    /**
     * Display the menu bar
     */
    public void showMenu() {







    }

    /**
     * Hide the menu bar
     */
    public void hideMenu() {
        if (menuVisible && Options.enable_menu)
            this.setMenuBar(null);
        
        canvas.repaint();
        menuVisible = false;
    }

    /**
     * Toggle the menu on/off (show if hidden, hide if visible)
     */
    public void toggleMenu() {
        if (!menuVisible)
            showMenu();
        else
            hideMenu();
    }

    /**
     * Retrieve the canvas contained within this frame
     *
     * @return RdesktopCanvas object associated with this frame
     */
    public RdesktopCanvas getCanvas() {
        return this.canvas;
    }

    /**
     * Register the RDP communications layer with this frame
     *
     * @param rdp Rdp object encapsulating the RDP comms layer
     */
    public void registerCommLayer(Rdp rdp) {
        this.rdp = rdp;
        canvas.registerCommLayer(rdp);
    }

    /**
     * Register keymap
     *
     * @param keys Keymapping object for use in handling keyboard events
     */
    public void registerKeyboard(KeyCode_FileBased keys) {
        canvas.registerKeyboard(keys);
    }

    /**
     * Display an error dialog with "Yes" and "No" buttons and the title
     * "properJavaRDP error"
     *
     * @param msg Array of message lines to display in dialog box
     * @return True if "Yes" was clicked to dismiss box
     */
    public boolean showYesNoErrorDialog(String[] msg) {

        YesNoDialog d = new YesNoDialog(this, "properJavaRDP error", msg);
        d.show();
        return d.retry;
    }

    /**
     * Display an error dialog with the title "properJavaRDP error"
     *
     * @param msg Array of message lines to display in dialog box
     */
    public void showErrorDialog(String[] msg) {
        Dialog d = new OKDialog(this, "properJavaRDP error", msg);
        d.show();
    }

    /**
     * Notify the canvas that the connection is ready for sending messages
     */
    public void triggerReadyToSend() {
        this.show();
        canvas.triggerReadyToSend();
    }

    /**
     * Centre this window
     */
    private void centreWindow() {
        centreWindow(this);
    }

    class RdesktopFocusListener implements FocusListener {

        @Override
        public void focusGained(FocusEvent arg0) {
            if (Constants.OS == Constants.WINDOWS) {
                
                canvas.repaint(0, 0, Options.width, Options.height);
            }
            
            canvas.gainedFocus();
        }

        @Override
        public void focusLost(FocusEvent arg0) {
            
            canvas.lostFocus();
        }
    }

    class RdesktopWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            hide();
            Rdesktop.exit(0, rdp, (RdesktopFrame) e.getWindow(), true);
        }

        @Override
        public void windowLostFocus(WindowEvent e) {

            
            canvas.lostFocus();
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
            if (Constants.OS == Constants.WINDOWS) {
                
                canvas.repaint(0, 0, Options.width, Options.height);
            }
            canvas.gainedFocus();
        }

        @Override
        public void windowActivated(WindowEvent e) {
            if (Constants.OS == Constants.WINDOWS) {
                
                canvas.repaint(0, 0, Options.width, Options.height);
            }
            
            canvas.gainedFocus();
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            if (Constants.OS == Constants.WINDOWS) {
                
                canvas.repaint(0, 0, Options.width, Options.height);
            }
            
            canvas.gainedFocus();
        }
    }

    class RdesktopComponentAdapter extends ComponentAdapter {
        @Override
        public void componentMoved(ComponentEvent e) {
            canvas.repaint(0, 0, Options.width, Options.height);
        }
    }

    static class YesNoDialog extends Dialog implements ActionListener {

        private static final long serialVersionUID = 5491261266068232056L;

        final Button yes;
        final Button no;

        boolean retry;

        YesNoDialog(Frame parent, String title, String[] message) {
            super(parent, title, true);
            
            
            
            
            Panel msg = new Panel();
            msg.setLayout(new GridLayout(message.length, 1));
            for (String aMessage : message) msg.add(new Label(aMessage, Label.CENTER));
            this.add("Center", msg);

            Panel p = new Panel();
            p.setLayout(new FlowLayout());
            yes = new Button("Yes");
            yes.addActionListener(this);
            p.add(yes);
            no = new Button("No");
            no.addActionListener(this);
            p.add(no);
            this.add("South", p);
            this.pack();
            if (getSize().width < 240)
                setSize(new Dimension(240, getSize().height));

            centreWindow(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            retry = e.getSource() == yes;
            this.hide();
            this.dispose();
        }
    }

    static class OKDialog extends Dialog implements ActionListener {
        private static final long serialVersionUID = 100978821816327378L;

        OKDialog(Frame parent, String title, String[] message) {

            super(parent, title, true);
            
            
            
            

            Panel msg = new Panel();
            msg.setLayout(new GridLayout(message.length, 1));
            for (String aMessage : message) msg.add(new Label(aMessage, Label.CENTER));
            this.add("Center", msg);

            Panel p = new Panel();
            p.setLayout(new FlowLayout());
            Button ok = new Button("OK");
            ok.addActionListener(this);
            p.add(ok);
            this.add("South", p);
            this.pack();

            if (getSize().width < 240)
                setSize(new Dimension(240, getSize().height));

            centreWindow(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.hide();
            this.dispose();
        }
    }

}
