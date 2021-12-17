/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2002,2007 ymnk, JCraft,Inc.
 *
 * Written by: ymnk<ymnk@jcaft.com>
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jcterm;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

class Sftp implements Runnable {
    private static final String[] help = {
            "      Available commands:",
            "      * means unimplemented command.",
            "cd [path]                     Change remote directory to 'path'",
            "lcd [path]                    Change local directory to 'path'",
            "chgrp grp path                Change group of file 'path' to 'grp'",
            "chmod mode path               Change permissions of file 'path' to 'mode'",
            "chown own path                Change owner of file 'path' to 'own'",
            "help                          Display this help text",
            "get remote-path [local-path]  Download file",
            "lls [path]                    Display local directory listing",
            "ln oldpath newpath            Symlink remote file",
            "lmkdir path                   Create local directory",
            "lpwd                          Print local working directory",
            "ls [path]                     Display remote directory listing",
            "*lumask umask                 Set local umask to 'umask'",
            "mkdir path                    Create remote directory",
            "put local-path [remote-path]  Upload file",
            "pwd                           Display remote working directory",
            "stat path                     Display info about path\n"
                    + "exit                          Quit sftp",
            "stop                          Quit sftp",
            "rename oldpath newpath        Rename remote file",
            "rmdir path                    Remove remote directory",
            "rm path                       Delete remote file",
            "symlink oldpath newpath       Symlink remote file",
            "version                       Show SFTP version",
            "?                             Synonym for help"};
    private final InputStream in;
    private final OutputStream out;
    private final ChannelSftp c;
    private final byte[] lf = {0x0a, 0x0d};
    private final byte[] del = {0x08, 0x20, 0x08};
    private Thread thread;

    Sftp(ChannelSftp c, InputStream in, OutputStream out) {
        this.c = c;
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            Vector cmds = new Vector();
            byte[] buf = new byte[1024];
            String lhome = c.lpwd();

            StringBuilder sb = new StringBuilder();
            while (true) {
                
                out.write("sftp> ".getBytes());
                cmds.removeAllElements();

                sb.setLength(0);

                int i;
                loop:
                while (true) {
                    i = in.read(buf, 0, 1024);
                    if (i <= 0)
                        break;
                    if (i != 1)
                        continue;
                    
                    if (buf[0] == 0x08) {
                        if (sb.length() > 0) {
                            sb.setLength(sb.length() - 1);
                            out.write(del, 0, del.length);
                            out.flush();
                        }
                        continue;
                    }

                    if (buf[0] == 0x0d) {
                        out.write(lf, 0, lf.length);
                    } else if (buf[0] == 0x0a) {
                        out.write(lf, 0, lf.length);
                    } else if (buf[0] < 0x20 || (buf[0] & 0x80) != 0) {
                        continue;
                    } else {
                        out.write(buf, 0, i);
                    }
                    out.flush();

                    for (int j = 0; j < i; j++) {
                        sb.append((char) buf[j]);
                        if (buf[j] == 0x0d) {
                            System
                                    .arraycopy(sb.toString().getBytes(), 0, buf, 0, sb.length());
                            i = sb.length();
                            break loop;
                        }
                        if (buf[j] == 0x0a) {
                            System
                                    .arraycopy(sb.toString().getBytes(), 0, buf, 0, sb.length());
                            i = sb.length();
                            break loop;
                        }
                    }
                }
                if (i <= 0)
                    break;

                i--;
                if (i > 0 && buf[i - 1] == 0x0d)
                    i--;
                if (i > 0 && buf[i - 1] == 0x0a)
                    i--;
                
                
                int s = 0;
                for (int ii = 0; ii < i; ii++) {
                    if (buf[ii] == ' ') {
                        if (ii - s > 0) {
                            cmds.addElement(new String(buf, s, ii - s));
                        }
                        while (ii < i) {
                            if (buf[ii] != ' ')
                                break;
                            ii++;
                        }
                        s = ii;
                    }
                }
                if (s < i) {
                    cmds.addElement(new String(buf, s, i - s));
                }
                if (cmds.isEmpty())
                    continue;

                String cmd = (String) cmds.elementAt(0);
                if ("stop".equals(cmd)) {
                    c.quit();
                    break;
                }
                if ("exit".equals(cmd)) {
                    c.exit();
                    break;
                }
                if ("cd".equals(cmd) || "lcd".equals(cmd)) {
                    String path = null;
                    if (cmds.size() < 2) {
                        path = "cd".equals(cmd) ? c.getHome() : lhome;
                    } else {
                        path = (String) cmds.elementAt(1);
                    }
                    try {
                        if ("cd".equals(cmd))
                            c.cd(path);
                        else
                            c.lcd(path);
                    } catch (SftpException e) {
                        
                        out.write(e.getMessage().getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    continue;
                }
                if (List.of("rm", "rmdir", "mkdir").contains(cmd)) {
                    if (cmds.size() < 2)
                        continue;
                    String path = (String) cmds.elementAt(1);
                    try {
                        switch (cmd) {
                            case "rm" -> c.rm(path);
                            case "rmdir" -> c.rmdir(path);
                            default -> c.mkdir(path);
                        }
                    } catch (SftpException e) {
                        
                        out.write(e.getMessage().getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    continue;
                }
                if ("lmkdir".equals(cmd)) {
                    if (cmds.size() < 2)
                        continue;
                    String path = (String) cmds.elementAt(1);

                    File d = new File(c.lpwd(), path);
                    if (!d.mkdir()) {

                        
                        out.write("failed to make directory".getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    continue;
                }

                if (List.of("chgrp", "chown", "chmod").contains(cmd)) {
                    if (cmds.size() != 3)
                        continue;
                    String path = (String) cmds.elementAt(2);
                    int foo = 0;
                    if ("chmod".equals(cmd)) {
                        byte[] bar = ((String) cmds.elementAt(1)).getBytes();
                        for (byte aBar : bar) {
                            int k = aBar;
                            if (k < '0' || k > '7') {
                                foo = -1;
                                break;
                            }
                            foo <<= 3;
                            foo |= (k - '0');
                        }
                        if (foo == -1)
                            continue;
                    } else {
                        try {
                            foo = Integer.parseInt((String) cmds.elementAt(1));
                        } catch (Exception e) {
                            continue;
                        }
                    }
                    try {
                        switch (cmd) {
                            case "chgrp" -> c.chgrp(foo, path);
                            case "chown" -> c.chown(foo, path);
                            case "chmod" -> c.chmod(foo, path);
                        }
                    } catch (SftpException e) {
                        
                        out.write(e.getMessage().getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    continue;
                }
                if ("pwd".equals(cmd) || "lpwd".equals(cmd)) {
                    String str = ("pwd".equals(cmd) ? "Remote" : "Local");
                    str += " working directory: ";
                    str += "pwd".equals(cmd) ? c.pwd() : c.lpwd();
                    
                    out.write(str.getBytes());
                    out.write(lf);
                    out.flush();
                    continue;
                }
                if ("ls".equals(cmd) || "dir".equals(cmd)) {
                    String path = ".";
                    if (cmds.size() == 2)
                        path = (String) cmds.elementAt(1);
                    try {
                        Vector vv = c.ls(path);
                        if (vv != null) {
                            for (int ii = 0; ii < vv.size(); ii++) {
                                
                                
                                out.write(vv.elementAt(ii).toString().getBytes());
                                out.write(lf);
                            }
                            out.flush();
                        }
                    } catch (SftpException e) {
                        
                        out.write(e.getMessage().getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    continue;
                }
                if ("lls".equals(cmd)) {
                    String path = c.lpwd();
                    if (cmds.size() == 2)
                        path = (String) cmds.elementAt(1);
                    try {
                        File d = new File(path);
                        String[] list = d.list();
                        for (String aList : list) {
                            out.write(aList.getBytes());
                            out.write(lf);
                        }
                        out.flush();
                    } catch (IOException e) {
                        
                        out.write(e.getMessage().getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    continue;
                }
                if ("get".equals(cmd) || "put".equals(cmd)) {
                    if (cmds.size() != 2 && cmds.size() != 3)
                        continue;
                    String p1 = (String) cmds.elementAt(1);
                    
                    String p2 = ".";
                    if (cmds.size() == 3)
                        p2 = (String) cmds.elementAt(2);
                    try {
                        SftpProgressMonitor monitor = new MyProgressMonitor(out);
                        if ("get".equals(cmd))
                            c.get(p1, p2, monitor);
                        else
                            c.put(p1, p2, monitor);
                    } catch (SftpException e) {
                        
                        out.write(e.getMessage().getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    continue;
                }
                if (List.of("ln", "symlink", "rename").contains(cmd)) {
                    if (cmds.size() != 3)
                        continue;
                    String p1 = (String) cmds.elementAt(1);
                    String p2 = (String) cmds.elementAt(2);
                    try {
                        if ("rename".equals(cmd))
                            c.rename(p1, p2);
                        else
                            c.symlink(p1, p2);
                    } catch (SftpException e) {
                        
                        out.write(e.getMessage().getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    continue;
                }
                if ("stat".equals(cmd) || "lstat".equals(cmd)) {
                    if (cmds.size() != 2)
                        continue;
                    String p1 = (String) cmds.elementAt(1);
                    SftpATTRS attrs = null;
                    try {
                        attrs = "stat".equals(cmd) ? c.stat(p1) : c.lstat(p1);
                    } catch (SftpException e) {
                        
                        out.write(e.getMessage().getBytes());
                        out.write(lf);
                        out.flush();
                    }
                    if (attrs != null) {
                        
                        out.write(attrs.toString().getBytes());
                        out.write(lf);
                        out.flush();
                    } else {
                    }
                    continue;
                }
                if ("version".equals(cmd)) {
                    
                    out.write(("SFTP protocol version " + c.version()).getBytes());
                    out.write(lf);
                    out.flush();
                    continue;
                }
                if ("help".equals(cmd)) {
                    for (String aHelp : help) {
                        out.write((aHelp).getBytes());
                        out.write(lf);
                    }
                    out.flush();
                    continue;
                }
                
                out.write(("unimplemented command: " + cmd).getBytes());
                out.write(lf);
                out.flush();
            }
            try {
                in.close();
            } catch (Exception ee) {
            }
            try {
                out.close();
            } catch (Exception ee) {
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void kick() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    static class MyProgressMonitor implements SftpProgressMonitor {
        final OutputStream out;
        
        long count = 0;
        long max = 0;
        String src;
        int percent = 0;

        MyProgressMonitor(OutputStream out) {
            this.out = out;
        }

        public void init(int op, String src, String dest, long max) {
            this.max = max;
            this.src = src;
            count = 0;
            percent = 0;
            status();
            
            
            
            
            
            
        }

        public boolean count(long count) {
            this.count += count;
            
            
            
            percent = (int) (((((float) this.count) / max)) * 100.0);
            status();
            return true;
        }

        public void end() {
            
            percent = (int) (((((float) count) / max)) * 100.0);
            status();
            try {
                out.write(0x0d);
                out.write(0x0a);
                out.flush();
            } catch (Exception e) {
            }
        }

        private void status() {
            try {
                out.write(0x0d);

                out.write(0x1b);
                out.write((byte) '[');
                out.write((byte) 'K');

                out.write((src + ": " + percent + "% " + count + '/' + max).getBytes());
                out.flush();
            } catch (Exception e) {
            }
        }
    }
}