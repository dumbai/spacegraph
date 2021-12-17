/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTermSwingFrame
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

import com.jcraft.jsch.*;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.stream.IntStream;

import static com.jcraft.jcterm.Terminal.SFTP;
import static com.jcraft.jcterm.Terminal.SHELL;

public class JCTermSwingFrame extends JFrame implements ActionListener, Runnable {
	private static final String COPYRIGHT = "JCTerm 0.0.11\nCopyright (C) 2002,2012 ymnk<ymnk@jcraft.com>, JCraft,Inc.\n"
		+ "Official Homepage: http://www.jcraft.com/jcterm/\n"
		+ "This software is licensed under GNU LGPL.";

	private static int counter = 1;
	private final JCTermSwing term;
	private int mode = SHELL;
	private String xhost = "127.0.0.1";
	private int xport = 0;
	private boolean xforwarding = false;
	private String proxy_http_host;
	private int proxy_http_port = 0;
	private String proxy_socks5_host;
	private int proxy_socks5_port = 0;
	private JSchSession jschsession;
	private Proxy proxy;
	private int compression = 0;
	private Connection connection;
	private Channel channel;
	private String configName = "default";
	private Thread thread;

	public JCTermSwingFrame() {
		this("");
	}

	private JCTermSwingFrame(String name) {
		this(name, "default");
	}

	private JCTermSwingFrame(String name, String configName) {
		super(name);

		JCTermSwing.setCR(new ConfigurationRepositoryFS());

		String s = System.getProperty("jcterm.config.use_ssh_agent");
		if (s != null && "true".equals(s))
			JSchSession.useSSHAgent(true);

		this.configName = configName;

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		enableEvents(AWTEvent.KEY_EVENT_MASK);


		JMenuBar mb = getJMenuBar();
		setJMenuBar(mb);

		term = new JCTermSwing();
		getContentPane().add(term);
		pack();

		ComponentListener l = new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Component c = e.getComponent();
				Container cp = ((RootPaneContainer) c).getContentPane();
				int cw = c.getWidth();
				int ch = c.getHeight();
				int cwm = c.getWidth() - cp.getWidth();
				int chm = c.getHeight() - cp.getHeight();
				cw -= cwm;
				ch -= chm;
				JCTermSwingFrame.this.term.setSize(cw, ch);
			}
		};
		addComponentListener(l);

		applyConfig(configName);

		openSession();
	}

	public static void main(String[] arg) {
		JCTermSwingFrame frame = new JCTermSwingFrame("JCTerm");
		frame.setVisible(true);
	}


	public void run() {
		String destination = null;
		while (thread != null) {
			try {
				int port = 22;
				String host1 = "127.0.0.1";
				String user1 = "";
				try {
					String[] destinations = JCTermSwing.getCR().load(configName).destinations;
					String _host = promptDestination(term, destinations);
					destination = _host;
					if (_host == null) {
						break;
					}
					String _user = _host.substring(0, _host.indexOf('@'));
					_host = _host.substring(_host.indexOf('@') + 1);
					if (_host.isEmpty()) {
						continue;
					}
					if (_host.indexOf(':') != -1) {
						try {
							port = Integer.parseInt(_host.substring(_host.indexOf(':') + 1));
						} catch (Exception eee) {
						}
						_host = _host.substring(0, _host.indexOf(':'));
					}
					user1 = _user;
					host1 = _host;
				} catch (Exception ee) {
					continue;
				}

				String user = user1;
				String host = host1;


				Configuration conf = JCTermSwing.getCR().load(configName);
				conf.addDestination(destination);
				JCTermSwing.getCR().save(conf);

				connection = connect(user, host, port);

				setTitle("[" + (counter++) + "] " + user + '@' + host + (port == 22 ? "" : (":" + port)));
				term.requestFocus();
				term.start(connection);
			} catch (Exception e) {

			}
			break;
		}
		thread = null;

		dispose_connection();

		setVisible(false);
	}

	public Connection connect(String user, String host, int port) throws JSchException, IOException {
		return connect(user, host, port, new MyUserInfo());
	}

	public Connection connect(String user, String host, int port, UserInfo userInfo) throws JSchException, IOException {
		try {

			jschsession = JSchSession.session(user, null, host, port, userInfo, proxy);
			setCompression(compression);


		} catch (Throwable e) {

			e.printStackTrace();
			return null;
		}

		Channel channel = null;
		OutputStream out = null;
		InputStream in = null;

        switch (mode) {
            case SHELL -> {
                channel = jschsession.session.openChannel("shell");
                if (xforwarding) {
                    jschsession.session.setX11Host(xhost);
                    jschsession.session.setX11Port(xport + 6000);
                    channel.setXForwarding(true);
                }
                out = channel.getOutputStream();
                in = channel.getInputStream();
                channel.connect();
            }
            case SFTP -> {
                out = new PipedOutputStream();
                in = new PipedInputStream();
                channel = jschsession.session.openChannel("sftp");
                channel.connect();
                (new Sftp((ChannelSftp) channel, new PipedInputStream(
                        (PipedOutputStream) out), new PipedOutputStream(
                        (PipedInputStream) in))).kick();
            }
        }

		OutputStream fout = out;
		InputStream fin = in;
		Channel fchannel = channel;

		return new Connection() {
			public InputStream getInputStream() {
				return fin;
			}

			public OutputStream getOutputStream() {
				return fout;
			}

			public void requestResize(Terminal term) {
				if (fchannel instanceof ChannelShell) {
					int c = term.getColumnCount();
					int r = term.getRowCount();
					((ChannelShell) fchannel).setPtySize(c, r, c * term.getCharWidth(),
						r * term.getCharHeight());
				}
			}

			public void close() {
				fchannel.disconnect();
			}
		};

	}

	private void dispose_connection() {
		synchronized (this) {
			if (channel != null) {
				channel.disconnect();
				channel = null;
			}
		}
	}

	private void setProxyHttp(String host, int port) {
		proxy_http_host = host;
		proxy_http_port = port;
        proxy = proxy_http_host != null && proxy_http_port != 0 ? new ProxyHTTP(proxy_http_host, proxy_http_port) : null;
	}

	private String getProxyHttpHost() {
		return proxy_http_host;
	}

	private int getProxyHttpPort() {
		return proxy_http_port;
	}

	private void setProxySOCKS5(String host, int port) {
		proxy_socks5_host = host;
		proxy_socks5_port = port;
        proxy = proxy_socks5_host != null && proxy_socks5_port != 0 ? new ProxySOCKS5(proxy_socks5_host, proxy_socks5_port) : null;
	}

	private String getProxySOCKS5Host() {
		return proxy_socks5_host;
	}

	private int getProxySOCKS5Port() {
		return proxy_socks5_port;
	}

	private void setXHost(String xhost) {
		this.xhost = xhost;
	}

	private void setXPort(int xport) {
		this.xport = xport;
	}

	private void setXForwarding(boolean foo) {
		this.xforwarding = foo;
	}

	private void setFontSize(int size) {
		Configuration conf = JCTermSwing.getCR().load(configName);
		conf.font_size = size;
		JCTermSwing.getCR().save(conf);
		_setFontSize(size);
	}

	private void _setFontSize(int size) {
		int mwidth = getWidth() - term.getTermWidth();
		int mheight = getHeight() - term.getTermHeight();
		term.setFont("Monospaced-" + size);
		setSize(mwidth + term.getTermWidth(), mheight + term.getTermHeight());
		term.clear();
		term.redraw(0, 0, term.getWidth(), term.getHeight());
	}

	public int getCompression() {
		return this.compression;
	}

	private void setCompression(int compression) {
		if (compression < 0 || 9 < compression)
			return;
		this.compression = compression;
		if (jschsession != null) {
			if (compression == 0) {
                jschsession.session.setConfig("compression.s2c", "none");
                jschsession.session.setConfig("compression.c2s", "none");
                jschsession.session.setConfig("compression_level", "0");
			} else {
                jschsession.session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
                jschsession.session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
                jschsession.session.setConfig("compression_level",
					Integer.valueOf(compression).toString());
			}
			try {
                jschsession.session.rekey();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}


	private boolean getAntiAliasing() {
		return term.getAntiAliasing();
	}

	private void setAntiAliasing(boolean foo) {
		term.setAntiAliasing(foo);
	}


	private void openSession() {
		this.thread = new Thread(this);
		this.thread.start();
	}

	private void setPortForwardingL(int port1, String host, int port2) {
		if (jschsession == null)
			return;
		try {
            jschsession.session.setPortForwardingL(port1, host, port2);
		} catch (JSchException e) {
		}
	}

	private void setPortForwardingR(int port1, String host, int port2) {
		if (jschsession == null)
			return;
		try {
            jschsession.session.setPortForwardingR(port1, host, port2);
		} catch (JSchException e) {
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		int _mode = switch (action) {
            case "Open SFTP Session..." -> SFTP;
            default -> SHELL;
        };

        switch (action) {
			case "Open SHELL Session...":
			case "Open SFTP Session...":
				if (thread == null) {
					mode = _mode;
					openSession();
				} else {
					openFrame(_mode, configName);
				}
				break;
			case "HTTP...": {
				String foo = getProxyHttpHost();
				int bar = getProxyHttpPort();
				String proxy = JOptionPane.showInputDialog(this,
					"HTTP proxy server (hostname:port)", ((foo != null && bar != 0) ? foo + ':'
						+ bar : ""));
				if (proxy == null)
					return;
				if (proxy.isEmpty()) {
					setProxyHttp(null, 0);
					return;
				}

				try {
					foo = proxy.substring(0, proxy.indexOf(':'));
					bar = Integer.parseInt(proxy.substring(proxy.indexOf(':') + 1));
					setProxyHttp(foo, bar);
				} catch (Exception ee) {
				}
				break;
			}
			case "SOCKS5...": {
				String foo = getProxySOCKS5Host();
				int bar = getProxySOCKS5Port();
				String proxy = JOptionPane.showInputDialog(this,
					"SOCKS5 server (hostname:1080)", ((foo != null && bar != 0) ? foo + ':' + bar
						: ""));
				if (proxy == null)
					return;
				if (proxy.isEmpty()) {
					setProxySOCKS5(null, 0);
					return;
				}

				try {
					foo = proxy.substring(0, proxy.indexOf(':'));
					bar = Integer.parseInt(proxy.substring(proxy.indexOf(':') + 1));
					setProxySOCKS5(foo, bar);
				} catch (Exception ee) {
				}
				break;
			}
			case "X11 Forwarding...":
				String display = JOptionPane.showInputDialog(this,
					"XDisplay name (hostname:0)", (xhost == null) ? "" : (xhost + ':' + xport));
				try {
					if (display != null) {
						xhost = display.substring(0, display.indexOf(':'));
						xport = Integer.parseInt(display.substring(display.indexOf(':') + 1));
						xforwarding = true;
					}
				} catch (Exception ee) {
					xforwarding = false;
					xhost = null;
				}
				break;
			case "AntiAliasing":
				setAntiAliasing(!getAntiAliasing());
				break;
			case "Compression...": {
				String foo = JOptionPane
					.showInputDialog(
						this,
						"Compression level(0-9)\n0 means no compression.\n1 means fast.\n9 means slow, but best.",
						Integer.valueOf(compression).toString());
				try {
					if (foo != null) {
						compression = Integer.parseInt(foo);
						setCompression(compression);
					}
				} catch (Exception ee) {
				}
				break;
			}
			case "About...":
				JOptionPane.showMessageDialog(this, COPYRIGHT);
				break;
			case "Local Port...":
			case "Remote Port...":
				if (jschsession == null) {
					JOptionPane.showMessageDialog(this,
						"Establish the connection before this setting.");
					return;
				}

				try {
					String title = "";
                    title += "Local Port...".equals(action) ? "Local port forwarding" : "remote port forwarding";
					title += "(port:host:hostport)";

					String foo = JOptionPane.showInputDialog(this, title, "");
					if (foo == null)
						return;
					int port1 = Integer.parseInt(foo.substring(0, foo.indexOf(':')));
					foo = foo.substring(foo.indexOf(':') + 1);
					String host = foo.substring(0, foo.indexOf(':'));
					int port2 = Integer.parseInt(foo.substring(foo.indexOf(':') + 1));

					if ("Local Port...".equals(action)) {
						setPortForwardingL(port1, host, port2);
					} else {
						setPortForwardingR(port1, host, port2);
					}
				} catch (Exception ee) {
				}
				break;
			case "Quit":
				quit();
				break;
		}
	}

	public JMenuBar getJMenuBar() {
		JMenuBar mb = new JMenuBar();

		JMenu m = new JMenu("File");
		JMenuItem mi = new JMenuItem("Open SHELL Session...");
		mi.addActionListener(this);
		mi.setActionCommand("Open SHELL Session...");
		m.add(mi);
		mi = new JMenuItem("Open SFTP Session...");
		mi.addActionListener(this);
		mi.setActionCommand("Open SFTP Session...");
		m.add(mi);
		mi = new JMenuItem("Quit");
		mi.addActionListener(this);
		mi.setActionCommand("Quit");
		m.add(mi);
		mb.add(m);

		m = new JMenu("Proxy");
		mi = new JMenuItem("HTTP...");
		mi.addActionListener(this);
		mi.setActionCommand("HTTP...");
		m.add(mi);
		mi = new JMenuItem("SOCKS5...");
		mi.addActionListener(this);
		mi.setActionCommand("SOCKS5...");
		m.add(mi);
		mb.add(m);

		m = new JMenu("PortForwarding");
		mi = new JMenuItem("Local Port...");
		mi.addActionListener(this);
		mi.setActionCommand("Local Port...");
		m.add(mi);
		mi = new JMenuItem("Remote Port...");
		mi.addActionListener(this);
		mi.setActionCommand("Remote Port...");
		m.add(mi);
		mi = new JMenuItem("X11 Forwarding...");
		mi.addActionListener(this);
		mi.setActionCommand("X11 Forwarding...");
		m.add(mi);
		mb.add(m);

		m = new JMenu("Etc");

		mi = new JMenuItem("AntiAliasing");
		mi.addActionListener(this);
		mi.setActionCommand("AntiAliasing");
		m.add(mi);

		mi = new JMenuItem("Compression...");
		mi.addActionListener(this);
		mi.setActionCommand("Compression...");
		m.add(mi);

		JMenu mcolor = new JMenu("Color");
		ActionListener mcolor_action = e -> setFgBg(e.getActionCommand());
		mcolor.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent me) {
				JMenu jm = (JMenu) me.getSource();
				String[] fg_bg = JCTermSwing.getCR().load(configName).fg_bg;
				for (String aFg_bg : fg_bg) {
					String[] tmp = aFg_bg.split(":");
					JMenuItem mi = new JMenuItem("ABC");
					mi.setForeground(JCTermSwing.toColor(tmp[0]));
					mi.setBackground(JCTermSwing.toColor(tmp[1]));
					mi.setActionCommand(aFg_bg);
					mi.addActionListener(mcolor_action);
					jm.add(mi);
				}
			}

			public void menuDeselected(MenuEvent me) {
				JMenu jm = (JMenu) me.getSource();
				jm.removeAll();
			}

			public void menuCanceled(MenuEvent arg) {
			}
		});
		m.add(mcolor);

		JMenu mfsize = new JMenu("Font size");
		ActionListener mfsize_action = e -> {
			String _font_size = e.getActionCommand();
			try {
				setFontSize(Integer.parseInt(_font_size));
			} catch (NumberFormatException nfe) {
			}
		};
		mfsize.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent me) {
				JMenu jm = (JMenu) me.getSource();
				int font_size = JCTermSwing.getCR().load(configName).font_size;
				JMenuItem mi = new JMenuItem("Smaller (" + (font_size - 1) + ')');
				mi.setActionCommand(String.valueOf(font_size - 1));
				mi.addActionListener(mfsize_action);
				jm.add(mi);
				mi = new JMenuItem("Larger (" + (font_size + 1) + ')');
				mi.setActionCommand(String.valueOf(font_size + 1));
				mi.addActionListener(mfsize_action);
				jm.add(mi);
			}

			public void menuDeselected(MenuEvent me) {
				JMenu jm = (JMenu) me.getSource();
				jm.removeAll();
			}

			public void menuCanceled(MenuEvent arg) {
			}
		});
		m.add(mfsize);

		mb.add(m);

		m = new JMenu("Help");
		mi = new JMenuItem("About...");
		mi.addActionListener(this);
		mi.setActionCommand("About...");
		m.add(mi);
		mb.add(m);

		return mb;
	}

	private void quit() {
		thread = null;
		if (connection != null) {
			connection.close();
			connection = null;
		}
    /*
    if(jschsession!=null){
      jschsession.dispose();
      jschsession=null;
    }
    */
	}

	public Terminal getTerm() {
		return term;
	}

	private void openFrame(int _mode, String configName) {
		JCTermSwingFrame c = new JCTermSwingFrame("JCTerm", configName);
		c.mode = _mode;
		c.setXForwarding(true);
		c.setXPort(xport);
		c.setXHost(xhost);
		c.setLocationRelativeTo(null);
		c.setVisible(true);
		c.setResizable(true);
	}

	private void setFgBg(String fg_bg) {
		Configuration conf = JCTermSwing.getCR().load(configName);
		conf.addFgBg(fg_bg);
		JCTermSwing.getCR().save(conf);
		_setFgBg(fg_bg);
	}

	private void _setFgBg(String fg_bg) {
		String[] tmp = fg_bg.split(":");
		Color fg = JCTermSwing.toColor(tmp[0]);
		Color bg = JCTermSwing.toColor(tmp[1]);
		term.setForeGround(fg);
		term.setDefaultForeGround(fg);
		term.setBackGround(bg);
		term.setDefaultBackGround(bg);
		term.resetCursorGraphics();
		term.clear();
		term.redraw(0, 0, term.getWidth(), term.getHeight());
	}

	private String promptDestination(JComponent term, String[] destinations) {
		JComboBox jb = new JComboBox();
		jb.setEditable(true);

		for (String destination : destinations) {
			jb.addItem(destination);
		}


		jb.requestFocusInWindow();
		JOptionPane pane = new JOptionPane(jb,
			JOptionPane.QUESTION_MESSAGE,
			JOptionPane.OK_CANCEL_OPTION) {
			public void selectInitialValue() {
			}
		};

		JDialog dialog = pane.createDialog(JCTermSwingFrame.this.term,
			"Enter username@hostname");
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		Object o = pane.getValue();

		String d = null;
		if (o != null && (Integer) o == JOptionPane.OK_OPTION) {
			d = (String) jb.getSelectedItem();
		}
        return d.isEmpty() ? null : d;
	}

	private void applyConfig(String configName) {
		this.configName = configName;
		Configuration conf = JCTermSwing.getCR().load(configName);
		_setFontSize(conf.font_size);
		_setFgBg(conf.fg_bg[0]);
	}

	class MyUserInfo implements UserInfo, UIKeyboardInteractive {
		final String passphrase = null;
		final JTextField pword = new JPasswordField(20);
		final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
			GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0,
			0, 0), 0, 0);
		String passwd;
		private Container panel;

		public boolean promptYesNo(String str) {
			Object[] options = {"yes", "no"};
			int foo = JOptionPane.showOptionDialog(JCTermSwingFrame.this.term, str,
				"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
				null, options, options[0]);
			return foo == 0;
		}

		public String getPassword() {
			return passwd;
		}

		public String getPassphrase() {
			return passphrase;
		}

		public boolean promptPassword(String message) {
			Object[] ob = {pword};
			JPanel panel = new JPanel();
			panel.add(pword);
			pword.requestFocusInWindow();
			JOptionPane pane = new JOptionPane(panel,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION) {
				public void selectInitialValue() {
				}
			};

			JDialog dialog = pane.createDialog(JCTermSwingFrame.this.term,
				message);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			Object o = pane.getValue();

			if (o != null && (Integer) o == JOptionPane.OK_OPTION) {
				passwd = pword.getText();
				return true;
			} else {
				return false;
			}
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public void showMessage(String message) {
			JOptionPane.showMessageDialog(null, message);
		}

		public String[] promptKeyboardInteractive(String destination, String name,
												  String instruction, String[] prompt, boolean[] echo) {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			panel.add(new JLabel(instruction), gbc);
			gbc.gridy++;

			gbc.gridwidth = GridBagConstraints.RELATIVE;

			JTextField[] texts = new JTextField[prompt.length];
			for (int i = 0; i < prompt.length; i++) {
				gbc.fill = GridBagConstraints.NONE;
				gbc.gridx = 0;
				gbc.weightx = 1;
				panel.add(new JLabel(prompt[i]), gbc);

				gbc.gridx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weighty = 1;
				if (echo[i]) {
					texts[i] = new JTextField(20);
				} else {
					texts[i] = new JPasswordField(20);
					texts[i].requestFocusInWindow();
				}
				panel.add(texts[i], gbc);
				gbc.gridy++;
			}
			for (int i = prompt.length - 1; i > 0; i--) {
				texts[i].requestFocusInWindow();
			}
			JOptionPane pane = new JOptionPane(panel,
				JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION) {
				public void selectInitialValue() {
				}
			};
			JDialog dialog = pane.createDialog(JCTermSwingFrame.this.term,
				destination + ": " + name);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			Object o = pane.getValue();
			if (o != null && (Integer) o == JOptionPane.OK_OPTION) {
				String[] response = IntStream.range(0, prompt.length).mapToObj(i -> texts[i].getText()).toArray(String[]::new);
				return response;
			} else {
				return null;
			}
		}
	}
}