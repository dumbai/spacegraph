/* RDPClientChooser.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Selects and loads a native RDP client if available
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

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


class RDPClientChooser {
    private static final Logger logger = LoggerFactory.getLogger(RDPClientChooser.class);

    /**
     * Initialise a client chooser, set logging level to DEBUG
     */
    RDPClientChooser() {

        logger.info("RDPClientChooser");
    }

    /**
     * Public method to locate and run a native RDP client (currently only on
     * Mac OS)
     *
     * @param args Arguments to provide to native client
     */
    public static boolean RunNativeRDPClient(String[] args) {
        logger.info("RDPClientChooser.RunNativeRDPClient");

        String os = System.getProperty("os.name");

        

        if (os.startsWith("Linux")) {

            

        }

        return false;
    }

    
    
    
    

    /**
     * Private method to run the Mac OS RDP client provided by Microsoft
     *
     * @param args Arguments to provide to native client
     */
    private static boolean RunMacRemoteDesktopConnection(String[] args)
            throws IOException {
        logger.info("RunMacRemoteDesktopConnection()");

        Options.windowTitle = "Remote Desktop Connection";


        LongOpt[] alo = new LongOpt[4];
        Getopt g = new Getopt("properJavaRDP", args,
                "bc:d:f::g:k:l:n:p:s:t:T:u:", alo);

        int c;
        while ((c = g.getopt()) != -1) {
            String arg;
            switch (c) {

                case 'd':
                    Options.domain = g.getOptarg();
                    break;

                case 'n':
                    Options.hostname = g.getOptarg();
                    break;

                case 'p':
                    Options.password = g.getOptarg();
                    break;

                case 't':
                    arg = g.getOptarg();
                    try {
                        Options.port = Integer.parseInt(arg);
                    } catch (Exception e) {
                    }
                    break;

                case 'T':
                    Options.windowTitle = g.getOptarg().replace('_', ' ');
                    break;

                case 'u':
                    Options.username = g.getOptarg();
                    break;

                case '?':
                default:
                    break;

            }
        }

        

        String server = null;

        if (g.getOptind() < args.length) {
            int colonat = args[args.length - 1].indexOf(':');
            if (colonat == -1) {
                server = args[args.length - 1];
            } else {
                server = args[args.length - 1].substring(0, colonat);
                Options.port = Integer.parseInt(args[args.length - 1]
                        .substring(colonat + 1));
            }
        } else {
            logger.warn("Server name required");
            return false;
        }

        
        
        

        String rdproot = "/var/tmp/RDP-" + Options.hostname + '-'
                + Options.port;

        try {
            new File(rdproot).mkdir();
        } catch (Exception e) {
            logger.warn("Failed to create directory {}", rdproot);
            return false;
        }

        

        logger.info("Creating RDP Config in {}", rdproot);

        FileWriter rdpConfigFile = new FileWriter(rdproot + "/Default.rdp");

        rdpConfigFile.write("screen mode id:i:0\n");
        rdpConfigFile.write("startdisplay:i:0\n");
        rdpConfigFile.write("desktop size id:i:6\n"); 
        
        
        
        rdpConfigFile.write("desktopwidth:i:1280\n");
        rdpConfigFile.write("desktopheight:i:854\n");
        rdpConfigFile.write("autoshowmenu:i:1\n");
        rdpConfigFile.write("desktopallowresize:i:1\n");
        rdpConfigFile.write("session bpp:i:8\n"); 
        rdpConfigFile.write("winposstr:s:0,3,0,0,800,600\n");
        rdpConfigFile.write("auto connect:i:1\n");
        rdpConfigFile.write("full address:s:" + server + ':' + Options.port
                + '\n');
        rdpConfigFile.write("compression:i:1\n");
        rdpConfigFile.write("rightclickmodifiers:i:4608\n");
        rdpConfigFile.write("altkeyreplacement:i:0\n");
        rdpConfigFile.write("audiomode:i:1\n");
        rdpConfigFile.write("redirectdrives:i:1\n");
        rdpConfigFile.write("redirectprinters:i:1\n");
        rdpConfigFile.write("username:s:" + Options.username + '\n');
        rdpConfigFile.write("clear password:s:" + Options.password + '\n');
        rdpConfigFile.write("domain:s:" + Options.domain + '\n');
        rdpConfigFile.write("alternate shell:s:\n");
        rdpConfigFile.write("shell working directory:s:\n");
        rdpConfigFile.write("preference flag id:i:2\n");
        rdpConfigFile.write("disable wallpaper:i:1\n");
        rdpConfigFile.write("disable full window drag:i:0\n");
        rdpConfigFile.write("disable menu anims:i:0\n");
        rdpConfigFile.write("disable themes:i:0\n");
        rdpConfigFile.write("disable cursor setting:i:0\n");
        rdpConfigFile.write("bitmapcachepersistenable:i:1\n");

        rdpConfigFile.write("Min Send Interval:i:5\n");
        rdpConfigFile.write("Order Draw Threshold:i:5\n");
        rdpConfigFile.write("Max Event Count:i:150\n");
        rdpConfigFile.write("Normal Event Count:i:150\n");
        rdpConfigFile.write("BitMapCacheSize:i:3500\n");

        rdpConfigFile.write("Keyboard Layout:i:en-uk\n");

        rdpConfigFile.close();

        
        
        
        
        

        if (new File(System.getProperty("user.home")
                + "/Library/Preferences/Microsoft/RDC Client").exists()) {
            FileWriter recentServersFile = new FileWriter(
                    System.getProperty("user.home")
                            + "/Library/Preferences/Microsoft/RDC Client/Recent Servers");
            recentServersFile.write(server + "\r1\r");
            recentServersFile.close();
        }

        
        
        
        
        
        

        String[] appcopycmd = {
                "/bin/sh",
                "-c",
                "ditto -rsrc /Applications/Remote\\ Desktop\\ Connection/Remote\\ Desktop\\ Connection "
                        + rdproot + "/ >/dev/null 2>/dev/null"};

        try {
            Runtime.getRuntime().exec(appcopycmd);
        } catch (IOException e) {
            logger.warn("Unable to copy application to temporary directory");
            return false;
        }

        try {
            Process p = Runtime.getRuntime().exec(appcopycmd);
            logger.warn("RDP Client copied to {}", rdproot);
            try {
                p.waitFor(); 
            } catch (InterruptedException e) {
                logger.warn("Unable to wait for application to copy");
                return false;
            }
        } catch (IOException e) {
            logger.warn("Unable to copy application to temporary directory");
            return false;
        }

        
        

        String[] mvcmd = {
                "/bin/sh",
                "-c",
                "mv " + rdproot + "/Remote\\ Desktop\\ Connection '" + rdproot
                        + '/' + Options.windowTitle
                        + "' >/dev/null 2>/dev/null"};

        try {
            Process p = Runtime.getRuntime().exec(mvcmd);
            try {
                p.waitFor(); 
            } catch (InterruptedException e) {
                logger.warn("Unable to wait for application to run");
                return false;
            }
        } catch (IOException e) {
            logger.warn("Unable to move application");
            return false;
        }

        

        String[] rdpcmd = {
                "/bin/sh",
                "-c",
                "open -a '" + rdproot + '/' + Options.windowTitle + "' "
                        + rdproot + "/Default.rdp >/dev/null 2>/dev/null"};

        try {
            Process p = Runtime.getRuntime().exec(rdpcmd);
            logger.info("RDP Client Launched from {}", rdproot);
            try {
                p.waitFor(); 
            } catch (InterruptedException e) {
                logger.warn("Unable to wait for application to run");
                return false;
            }
        } catch (IOException e) {
            logger.warn("Unable to open (run) application");
            return false;
        }

        
        

        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            logger.info("Unable to wait for 10 seconds");
            return false;
        }


        try {
            String[] rmcmd = {"/bin/sh", "-c",
                    "rm -r " + rdproot + " >/dev/null 2>/dev/null"};
            Runtime.getRuntime().exec(rmcmd);
        } catch (IOException e) {
            logger.warn("Unable to remove temporary directory {}", rdproot);
            return true;
        }

        logger.warn("RDP Client Completed");
        return true;
    }
}