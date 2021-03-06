/* RdpApplet.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Provide an applet interface to ProperJavaRDP
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

package net.propero.rdp.applet;

import net.propero.rdp.Common;
import net.propero.rdp.Rdesktop;
import net.propero.rdp.RdesktopException;

import java.applet.Applet;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

public class RdpApplet extends Applet {

    private static final long serialVersionUID = 583386592743649642L;

    private TextArea aTextArea;

    private boolean redirectOutput;
    private RdpThread rThread;

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(0xFFFFFF));
        g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
        g.setColor(new Color(0x000000));
        int width = g.getFontMetrics().stringWidth(
                "Launching properJavaRDP session...");
        int x = (int) (g.getClipBounds().getWidth() / 2) - (width / 2);
        int y = (int) (g.getClipBounds().getHeight() / 2);
        if (!redirectOutput)
            g.drawString("Launching properJavaRDP session...", x, y);
        width = g.getFontMetrics().stringWidth(
                "Connect to:" + getParameter("server"));
        x = (int) (g.getClipBounds().getWidth() / 2) - (width / 2);
        y = (int) (g.getClipBounds().getHeight() / 2) + 20;
        if (!redirectOutput)
            g.drawString("Connecting to:" + getParameter("server"), x, y);
    }

    @Override
    public void init() {
        redirectOutput = isSet("redirectOutput");
        if (redirectOutput) {
            PrintStream aPrintStream = new PrintStream(new FilteredStream(
                    new ByteArrayOutputStream()));
            System.setOut(aPrintStream);
            System.setErr(aPrintStream);
            aTextArea = new TextArea();
            setLayout(new BorderLayout());
            add("Center", aTextArea);
        }
    }

    @Override
    public void start() {

        Common.underApplet = true;

        String[] args = new String[40];
        int index = 0;
        index = genArgS("-m", "keymap", args, index);
        index = genArgS("-u", "username", args, index);
        index = genArgS("-p", "password", args, index);
        index = genArgS("-n", "hostname", args, index);
        index = genArgS("-l", "debug_level", args, index);
        index = genArgS("-d", "shell", args, index);
        index = genArgS("-T", "title", args, index);
        index = genArgS("-c", "command", args, index);
        index = genArgS("-d", "domain", args, index);
        index = genArgS("-o", "bpp", args, index);
        index = genArgS("-g", "geometry", args, index);
        index = genArgS("-s", "shell", args, index);
        index = genArgF("--use_ssl", "use_ssl", args, index);
        index = genArgF("--console", "console", args, index);
        index = genArgF("--use_rdp4", "rdp4", args, index);
        index = genArgF("--debug_key", "debug_key", args, index);
        index = genArgF("--debug_hex", "debug_hex", args, index);
        index = genArgF("--no_remap_hash", "no_remap_hash", args, index);

        String rdpserver = this.getParameter("server");
        String rdpport = this.getParameter("port");
        if (rdpserver != null)
            args[index++] = rdpserver + ((rdpport == null) ? "" : (':' + rdpport));


        String[] rargs = new String[index];
        System.arraycopy(args, 0, rargs, 0, index);
        for (int i = 0; i < rargs.length; i++)
            System.out.println("args[" + i + "]=\"" + rargs[i] + '"');

        rThread = new RdpThread(rargs, this.getParameter("redirect_on_exit"), this);
        new Thread(rThread).start();
        //this.setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void stop() {
        rThread = null;

        try {
            synchronized(this) {
                notifyAll();
            }
        } catch (RuntimeException e) {
            System.out.println("Stop-notifyAll");
        }

        
    }

    private boolean isSet(String parameter) {
        String s = this.getParameter(parameter);
        if (s != null) {
            return "yes".equalsIgnoreCase(s);
        }
        return false;
    }

    private int genArgF(String flag, String parameter, String[] args, int i) {
        String s = this.getParameter(parameter);
        if (s != null) {
            if ("yes".equalsIgnoreCase(s)) {
                args[i] = flag;
                i++;
            }
        }
        return i;
    }

    private int genArgS(String name, String parameter, String[] args, int i) {
        String s = this.getParameter(parameter);
        if (s != null) {
            
            if (!name.isEmpty()) {
                args[i] = name;
                i++;
			}
			args[i] = s;
			i++;
		}
        return i;
    }

    class FilteredStream extends FilterOutputStream {
        FilteredStream(OutputStream aStream) {
            super(aStream);
        }

        @Override
        public void write(byte[] b) {
            String aString = new String(b);
            aTextArea.append(aString);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            String aString = new String(b, off, len);
            aTextArea.append(aString);
        }
    }

}

class RdpThread implements Runnable {

    private final String[] args;

    private final String redirect;

    private final Applet parentApplet;

    RdpThread(String[] args, String redirect, Applet a) {
        parentApplet = a;
        this.args = args;
        this.redirect = redirect;
    }

    @Override
    public void run() {

        try {
            Rdesktop.main(args);
            if (redirect != null) {
                URL u = new URL(redirect);
                parentApplet.getAppletContext().showDocument(u);
            }
        } catch (RdesktopException e) {
            
            e.printStackTrace();
        } catch (MalformedURLException e) {
            
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        Common.underApplet = false;
    }
}