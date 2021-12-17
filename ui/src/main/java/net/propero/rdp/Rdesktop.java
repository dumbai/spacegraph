/* Rdesktop.java
 * Component: ProperJavaRDP
 *
 * Revision: $Revision: #2 $
 * Author: $Author: tvkelley $
 * Date: $Date: 2009/09/15 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Main class, launches session
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
import net.propero.rdp.keymapping.KeyCode_FileBased;
import net.propero.rdp.rdp5.Rdp5;
import net.propero.rdp.rdp5.VChannels;
import net.propero.rdp.rdp5.cliprdr.ClipChannel;
import net.propero.rdp.rdp5.snd.SoundChannel;
import net.propero.rdp.tools.SendEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;



public enum Rdesktop {
    ;

    /* RDP5 disconnect PDU */
    private static final int exDiscReasonNoInfo = 0x0000;
    private static final int exDiscReasonAPIInitiatedDisconnect = 0x0001;
    private static final int exDiscReasonAPIInitiatedLogoff = 0x0002;
    private static final int exDiscReasonServerIdleTimeout = 0x0003;
    private static final int exDiscReasonServerLogonTimeout = 0x0004;
    private static final int exDiscReasonReplacedByOtherConnection = 0x0005;
    private static final int exDiscReasonOutOfMemory = 0x0006;
    private static final int exDiscReasonServerDeniedConnection = 0x0007;
    private static final int exDiscReasonServerDeniedConnectionFips = 0x0008;
    private static final int exDiscReasonLicenseInternal = 0x0100;
    private static final int exDiscReasonLicenseNoLicenseServer = 0x0101;
    private static final int exDiscReasonLicenseNoLicense = 0x0102;
    private static final int exDiscReasonLicenseErrClientMsg = 0x0103;
    private static final int exDiscReasonLicenseHwidDoesntMatchLicense = 0x0104;
    private static final int exDiscReasonLicenseErrClientLicense = 0x0105;
    private static final int exDiscReasonLicenseCantFinishProtocol = 0x0106;
    private static final int exDiscReasonLicenseClientEndedProtocol = 0x0107;
    private static final int exDiscReasonLicenseErrClientEncryption = 0x0108;
    private static final int exDiscReasonLicenseCantUpgradeLicense = 0x0109;
    private static final int exDiscReasonLicenseNoRemoteConnections = 0x010a;
    private static final Logger logger = LoggerFactory.getLogger("net.propero.rdp");
    private static final String keyMapPath = "keymaps/";
    private static boolean keep_running;

    static boolean loggedon;

    static int gReconnectLogonid;

    static boolean readytosend;

    private static boolean showTools;
    private static String mapFile = "en-us";
    private static SendEvent toolFrame;

    /**
     * Translate a disconnect code into a textual description of the reason for
     * the disconnect
     *
     * @param reason Integer disconnect code received from server
     * @return Text description of the reason for disconnection
     */
    private static String textDisconnectReason(int reason) {
        String text = switch (reason) {
            case exDiscReasonNoInfo -> "No information available";
            case exDiscReasonAPIInitiatedDisconnect -> "Server initiated disconnect";
            case exDiscReasonAPIInitiatedLogoff -> "Server initiated logoff";
            case exDiscReasonServerIdleTimeout -> "Server idle timeout reached";
            case exDiscReasonServerLogonTimeout -> "Server logon timeout reached";
            case exDiscReasonReplacedByOtherConnection -> "Another user connected to the session";
            case exDiscReasonOutOfMemory -> "The server is out of memory";
            case exDiscReasonServerDeniedConnection -> "The server denied the connection";
            case exDiscReasonServerDeniedConnectionFips -> "The server denied the connection for security reason";
            case exDiscReasonLicenseInternal -> "Internal licensing error";
            case exDiscReasonLicenseNoLicenseServer -> "No license server available";
            case exDiscReasonLicenseNoLicense -> "No valid license available";
            case exDiscReasonLicenseErrClientMsg -> "Invalid licensing message";
            case exDiscReasonLicenseHwidDoesntMatchLicense -> "Hardware id doesn't match software license";
            case exDiscReasonLicenseErrClientLicense -> "Client license error";
            case exDiscReasonLicenseCantFinishProtocol -> "Network error during licensing protocol";
            case exDiscReasonLicenseClientEndedProtocol -> "Licensing protocol was not completed";
            case exDiscReasonLicenseErrClientEncryption -> "Incorrect client license enryption";
            case exDiscReasonLicenseCantUpgradeLicense -> "Can't upgrade license";
            case exDiscReasonLicenseNoRemoteConnections -> "The server is not licensed to accept remote connections";
            default -> reason > 0x1000 && reason < 0x7fff ? "Internal protocol error: 0x" + Integer.toHexString(reason) : "Unknown reason";
        };

        return text;
    }

    /**
     * Outputs version and usage information via System.err
     */
    private static void usage() {
        System.err.println("properJavaRDP version " + Version.version);
        System.err
                .println("Usage: java net.propero.rdp.Rdesktop [options] server[:port]");
        System.err
                .println("	-b 							bandwidth saving (good for 56k modem, but higher latency");
        System.err.println("	-c DIR						working directory");
        System.err.println("	-d DOMAIN					logon domain");
        System.err
                .println("	-f[l]						full-screen mode [with Linux KDE optimization]");
        System.err.println("	-g WxH						desktop geometry");
        System.err
                .println("	-m MAPFILE					keyboard mapping file for terminal server");
        System.err
                .println("	-l LEVEL					logging level {DEBUG, INFO, WARN, ERROR, FATAL}");
        System.err.println("	-n HOSTNAME					client hostname");
        System.err.println("	-p PASSWORD					password");
        System.err.println("	-s SHELL					shell");
        System.err.println("	-t NUM						RDP port (default 3389)");
        System.err.println("	-T TITLE					window title");
        System.err.println("	-u USERNAME					user name");
        System.err.println("	-o BPP						bits-per-pixel for display");
        System.err
                .println("    -r path                     path to load licence from (requests and saves licence from server if not found)");
        System.err
                .println("    --save_licence              request and save licence from server");
        System.err
                .println("    --load_licence              load licence from file");
        System.err
                .println("    --console                   connect to console");
        System.err
                .println("	--debug_key 				show scancodes sent for each keypress etc");
        System.err.println("	--debug_hex 				show bytes sent and received");
        System.err.println("	--no_remap_hash 			disable hash remapping");
        System.err.println("	--quiet_alt 				enable quiet alt fix");
        System.err
                .println("	--no_encryption				disable encryption from client to server");
        System.err.println("	--use_rdp4					use RDP version 4");
        
//        System.err
//                .println("	--log4j_config=FILE			use FILE for log4j configuration");
        System.err
                .println("Example: java net.propero.rdp.Rdesktop -g 800x600 -l WARN m52.propero.int");
        Rdesktop.exit(0, null, null, true);
    }

    /**
     * @param args
     * @throws OrderException
     * @throws RdesktopException
     */
    public static void main(String... args) throws RdesktopException {
        RDPwindow(args);
    }

    public static RdesktopFrame RDPwindow(String... args) throws RdesktopException {
        
        keep_running = true;
        loggedon = false;
        readytosend = false;
        showTools = false;
        mapFile = "en-us";
        String keyMapLocation = "";
        toolFrame = null;




        


        if (RDPClientChooser.RunNativeRDPClient(args)) {
            if (!Common.underApplet)
                System.exit(0);
        }


        StringBuffer sb = new StringBuffer();
        LongOpt[] alo = new LongOpt[15];
        alo[0] = new LongOpt("debug_key", LongOpt.NO_ARGUMENT, null, 0);
        alo[1] = new LongOpt("debug_hex", LongOpt.NO_ARGUMENT, null, 0);
        alo[2] = new LongOpt("no_paste_hack", LongOpt.NO_ARGUMENT, null, 0);
//        alo[3] = new LongOpt("log4j_config", LongOpt.REQUIRED_ARGUMENT, sb, 0);
        alo[4] = new LongOpt("packet_tools", LongOpt.NO_ARGUMENT, null, 0);
        alo[5] = new LongOpt("quiet_alt", LongOpt.NO_ARGUMENT, sb, 0);
        alo[6] = new LongOpt("no_remap_hash", LongOpt.NO_ARGUMENT, null, 0);
        alo[7] = new LongOpt("no_encryption", LongOpt.NO_ARGUMENT, null, 0);
        alo[8] = new LongOpt("use_rdp4", LongOpt.NO_ARGUMENT, null, 0);
        alo[9] = new LongOpt("use_ssl", LongOpt.NO_ARGUMENT, null, 0);
        alo[10] = new LongOpt("enable_menu", LongOpt.NO_ARGUMENT, null, 0);
        alo[11] = new LongOpt("console", LongOpt.NO_ARGUMENT, null, 0);
        alo[12] = new LongOpt("load_licence", LongOpt.NO_ARGUMENT, null, 0);
        alo[13] = new LongOpt("save_licence", LongOpt.NO_ARGUMENT, null, 0);
        alo[14] = new LongOpt("persistent_caching", LongOpt.NO_ARGUMENT, null,
                0);

        String progname = "properJavaRDP";

        Getopt g = new Getopt("properJavaRDP", args,
                "bc:d:f::g:k:l:m:n:p:s:t:T:u:o:r:", alo);

        ClipChannel clipChannel = new ClipChannel();
        SoundChannel soundChannel = new SoundChannel();


        int c;
        boolean fKdeHack = false;
        int logonflags = Rdp.RDP_LOGON_NORMAL;
        while ((c = g.getopt()) != -1) {
            String arg;
            switch (c) {

                case 0:
                    switch (g.getLongind()) {
                        case 0:
                            Options.debug_keyboard = true;
                            break;
                        case 1:
                            Options.debug_hexdump = true;
                            break;
                        case 2:
                            break;





                        case 4:
                            showTools = true;
                            break;
                        case 5:
                            Options.altkey_quiet = true;
                            break;
                        case 6:
                            Options.remap_hash = false;
                            break;
                        case 7:
                            Options.packet_encryption = false;
                            break;
                        case 8:
                            Options.use_rdp5 = false;
                            
                            Options.set_bpp(8);
                            break;
                        case 9:
                            Options.use_ssl = true;
                            break;
                        case 10:
                            Options.enable_menu = true;
                            break;
                        case 11:
                            Options.console_session = true;
                            break;
                        case 12:
                            Options.load_licence = true;
                            break;
                        case 13:
                            Options.save_licence = true;
                            break;
                        case 14:
                            Options.persistent_bitmap_caching = true;
                            break;
                        default:
                            usage();
                    }
                    break;

                case 'o':
                    Options.set_bpp(Integer.parseInt(g.getOptarg()));
                    break;
                case 'b':
                    Options.low_latency = false;
                    break;
                case 'm':
                    mapFile = g.getOptarg();
                    break;
                case 'c':
                    Options.directory = g.getOptarg();
                    break;
                case 'd':
                    Options.domain = g.getOptarg();
                    break;
                case 'f':
                    Dimension screen_size = Toolkit.getDefaultToolkit()
                            .getScreenSize();
                    
                    Options.width = screen_size.width & ~3;
                    Options.height = screen_size.height;
                    Options.fullscreen = true;
                    arg = g.getOptarg();
                    if (arg != null) {
                        if (arg.charAt(0) == 'l')
                            fKdeHack = true;
                        else {
                            System.err.println(progname
                                    + ": Invalid fullscreen option '" + arg + '\'');
                            usage();
                        }
                    }
                    break;
                case 'g':
                    arg = g.getOptarg();
                    int cut = arg.indexOf('x');
                    if (cut == -1) {
                        System.err.println(progname + ": Invalid geometry: " + arg);
                        usage();
                    }
                    Options.width = Integer.parseInt(arg.substring(0, cut)) & ~3;
                    Options.height = Integer.parseInt(arg.substring(cut + 1));
                    break;
                case 'k':
                    arg = g.getOptarg();
                    
                    if (Options.keylayout == -1) {
                        System.err.println(progname + ": Invalid key layout: "
                                + arg);
                        usage();
                    }
                    break;





























                case 'n':
                    Options.hostname = g.getOptarg();
                    break;
                case 'p':
                    Options.password = g.getOptarg();
                    logonflags |= Rdp.RDP_LOGON_AUTO;
                    break;
                case 's':
                    Options.command = g.getOptarg();
                    break;
                case 'u':
                    Options.username = g.getOptarg();
                    break;
                case 't':
                    arg = g.getOptarg();
                    try {
                        Options.port = Integer.parseInt(arg);
                    } catch (NumberFormatException nex) {
                        System.err.println(progname + ": Invalid port number: "
                                + arg);
                        usage();
                    }
                    break;
                case 'T':
                    Options.windowTitle = g.getOptarg().replace('_', ' ');
                    break;
                case 'r':
                    Options.licence_path = g.getOptarg();
                    break;

                case '?':
                default:
                    usage();
                    break;

            }
        }

        if (fKdeHack) {
            Options.height -= 46;
        }

        VChannels channels = new VChannels();

        String[] server = {null};

        if (g.getOptind() < args.length) {
            int colonat = args[args.length - 1].indexOf(':');
            if (colonat == -1) {
                server[0] = args[args.length - 1];
            } else {
                server[0] = args[args.length - 1].substring(0, colonat);
                Options.port = Integer.parseInt(args[args.length - 1]
                        .substring(colonat + 1));
            }
        } else {
            System.err.println(progname + ": A server name is required!");
            usage();
        }


        
        if (Options.use_rdp5) {
            
            if (Options.map_clipboard) {
                channels.register(clipChannel);
            }
            channels.register(soundChannel);

        }

        

        logger.info("properJavaRDP version " + Version.version);

        if (args.length == 0)
            usage();

        String java = System.getProperty("java.specification.version");
        logger.info("Java version is {}", java);

        String os = System.getProperty("os.name");

        if ("Windows 2000".equals(os) || "Windows XP".equals(os))
            Options.built_in_licence = true;

        String osvers = System.getProperty("os.version");
        logger.info("Operating System is {} version {}", os, osvers);

        if (os.startsWith("Linux"))
            Constants.OS = Constants.LINUX;
        else if (os.startsWith("Windows"))
            Constants.OS = Constants.WINDOWS;
        else if (os.startsWith("Mac"))
            Constants.OS = Constants.MAC;

        if (Constants.OS == Constants.MAC)
            Options.caps_sends_up_and_down = false;

        RdesktopFrame window = new RdesktopFrame_Localised();
        int finalLogonflags = logonflags;
        new Thread(() -> {
            Rdp5 RdpLayer = null;
            Common.rdp = RdpLayer;

            window.setClip(clipChannel);

            
            KeyCode_FileBased keyMap = null;
            try {
                

                InputStream istr = Rdesktop.class.getClassLoader().getResourceAsStream(
                        keyMapPath + mapFile);





                logger.debug("Loading keymap from InputStream");
                keyMap = new KeyCode_FileBased_Localised(istr);

                if (istr != null)
                    istr.close();
                Options.keylayout = keyMap.getMapCode();
            } catch (Exception kmEx) {
                String[] msg = {(kmEx.getClass() + ": " + kmEx.getMessage())};
                window.showErrorDialog(msg);
                kmEx.printStackTrace();
                Rdesktop.exit(0, null, null, true);
            }

            logger.debug("Registering keyboard...");
            if (keyMap != null)
                window.registerKeyboard(keyMap);

            logger.debug("keep_running = {}", keep_running);
            int[] ext_disc_reason = new int[1];
            boolean[] deactivated = new boolean[1];
            while (keep_running) {
                logger.debug("Initialising RDP layer...");
                RdpLayer = new Rdp5(channels);
                Common.rdp = RdpLayer;
                logger.debug("Registering drawing surface...");
                RdpLayer.registerDrawingSurface(window);
                logger.debug("Registering comms layer...");
                window.registerCommLayer(RdpLayer);
                loggedon = false;
                readytosend = false;
                logger.info("Connecting to {}" + ':' + "{} ...", server[0], Options.port);

                if ("localhost".equalsIgnoreCase(server[0]))
                    server[0] = "127.0.0.1";

                if (RdpLayer != null) {
                    
                    try {
                        RdpLayer.connect(Options.username, InetAddress
                                        .getByName(server[0]), finalLogonflags, Options.domain,
                                Options.password, Options.command,
                                Options.directory);

                        
                        if (showTools) {
                            toolFrame = new SendEvent(RdpLayer);
                            toolFrame.show();
                        }
                        

                        if (keep_running) {

                            /*
                             * By setting encryption to False here, we have an
                             * encrypted login packet but unencrypted transfer of
                             * other packets
                             */
                            if (!Options.packet_encryption)
                                Options.encryption = false;

                            logger.info("Connection successful");
                            
                            RdpLayer.mainLoop(deactivated, ext_disc_reason);

                            if (deactivated[0]) {
                                /* clean disconnect */
                                Rdesktop.exit(0, RdpLayer, window, true);
                                
                            } else {
                                if (ext_disc_reason[0] == exDiscReasonAPIInitiatedDisconnect
                                        || ext_disc_reason[0] == exDiscReasonAPIInitiatedLogoff) {
                                    /*
                                     * not so clean disconnect, but nothing to worry
                                     * about
                                     */
                                    Rdesktop.exit(0, RdpLayer, window, true);
                                    
                                }

                                if (ext_disc_reason[0] >= 2) {
                                    String reason = textDisconnectReason(ext_disc_reason[0]);
                                    String[] msg = {"Connection terminated",
                                            reason};
                                    window.showErrorDialog(msg);
                                    logger.warn("Connection terminated: {}", reason);
                                    Rdesktop.exit(0, RdpLayer, window, true);
                                }

                            }

                            keep_running = false; 
                            if (!readytosend) {
                                
                                
                                String msg1 = "The terminal server disconnected before licence negotiation completed.";
                                logger.warn(msg1);
                                String msg2 = "Possible cause: terminal server could not issue a licence.";
                                logger.warn(msg2);
                                String[] msg = {msg1, msg2};
                                window.showErrorDialog(msg);
                            }
                        } 

                        
                        if (showTools)
                            toolFrame.dispose();
                        

                    } catch (ConnectionException e) {
                        String[] msg = {"Connection Exception", e.getMessage()};
                        window.showErrorDialog(msg);
                        Rdesktop.exit(0, RdpLayer, window, true);
                    } catch (UnknownHostException e) {
                        error(e, RdpLayer, window, true);
                    } catch (SocketException s) {
                        if (RdpLayer.isConnected()) {
                            logger.error("{}" + ' ' + "{}", s.getClass().getName(), s.getMessage());
                            s.printStackTrace();
                            error(s, RdpLayer, window, true);
                            Rdesktop.exit(0, RdpLayer, window, true);
                        }
                    } catch (RdesktopException e) {
                        String msg1 = e.getClass().getName();
                        String msg2 = e.getMessage();
                        logger.error("{}: {}", msg1, msg2);

                        e.printStackTrace(System.err);

                        if (!readytosend) {


                            String[] msg = {
                                    "The terminal server reset connection before licence negotiation completed.",
                                    "Possible cause: terminal server could not connect to licence server.",
                                    "Retry?"};
                            boolean retry = window.showYesNoErrorDialog(msg);
                            if (!retry) {
                                logger.info("Selected not to retry.");
                                Rdesktop.exit(0, RdpLayer, window, true);
                            } else {
                                if (RdpLayer != null && RdpLayer.isConnected()) {
                                    logger.info("Disconnecting ...");
                                    RdpLayer.disconnect();
                                    logger.info("Disconnected");
                                }
                                logger.info("Retrying connection...");
                                keep_running = true; 
                                continue;
                            }
                        } else {
                            String[] msg = {e.getMessage()};
                            window.showErrorDialog(msg);
                            Rdesktop.exit(0, RdpLayer, window, true);
                        }
                    } catch (Exception e) {
                        logger.warn("{}" + ' ' + "{}", e.getClass().getName(), e.getMessage());
                        e.printStackTrace();
                        error(e, RdpLayer, window, true);
                    }
                } else { 
                    logger.error("The communications layer could not be initiated!");
                }
            }
            Rdesktop.exit(0, RdpLayer, window, true);

        }).start();
        return window;
    }

    /**
     * Disconnects from the server connected to through rdp and destroys the
     * RdesktopFrame window.
     * <p>
     * Exits the application iff sysexit == true, providing return value n to
     * the operating system.
     *
     * @param n
     * @param rdp
     * @param window
     * @param sysexit
     */
    public static void exit(int n, Rdp rdp, RdesktopFrame window,
                            boolean sysexit) {
        keep_running = false;

        
        if ((showTools) && (toolFrame != null))
            toolFrame.dispose();
        

        if (rdp != null && rdp.isConnected()) {
            logger.info("Disconnecting ...");
            rdp.disconnect();
            logger.info("Disconnected");
        }
        if (window != null) {
            window.setVisible(false);
            window.dispose();
        }



        if (sysexit && Constants.SystemExit) {
            if (!Common.underApplet)
                System.exit(n);
        }
    }

    /**
     * Displays an error dialog via the RdesktopFrame window containing the
     * customised message emsg, and reports this through the logging system.
     * <p>
     * The application then exits iff sysexit == true
     *
     * @param emsg
     * @param RdpLayer
     * @param window
     * @param sysexit
     */
    public static void customError(String emsg, Rdp RdpLayer,
                                   RdesktopFrame window, boolean sysexit) {
        logger.error(emsg);
        String[] msg = {emsg};
        window.showErrorDialog(msg);
        Rdesktop.exit(0, RdpLayer, window, true);
    }

    /**
     * Displays details of the Exception e in an error dialog via the
     * RdesktopFrame window and reports this through the logger, then prints a
     * stack trace.
     * <p>
     * The application then exits iff sysexit == true
     *
     * @param e
     * @param RdpLayer
     * @param window
     * @param sysexit
     */
    public static void error(Exception e, Rdp RdpLayer, RdesktopFrame window,
                             boolean sysexit) {
        try {

            String msg1 = e.getClass().getName();
            String msg2 = e.getMessage();

            logger.error("{}: {}", msg1, msg2);

            String[] msg = {msg1, msg2};
            window.showErrorDialog(msg);

            
        } catch (Exception ex) {
            logger.warn("Exception in Rdesktop.error: {}: {}", ex.getClass().getName(), ex.getMessage());
        }

        Rdesktop.exit(0, RdpLayer, window, sysexit);
    }
}