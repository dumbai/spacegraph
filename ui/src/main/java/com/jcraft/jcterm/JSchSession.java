/* -*-mode:java; c-basic-offset:2; -*- */
/* JSchSession
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JSchSession {
    private static final Map<String,JSchSession> pool = new ConcurrentHashMap();
    private static JSch jsch;
    private static SessionFactory sessionFactory;
    private String key;
    public final Session session;

    private JSchSession(Session session, String key) {
        this.session = session;
        this.key = key;
    }

    /** TODO use pool.compute to make fully concurrent and removed synchronized */
    public static synchronized JSchSession session(String username, String password,
                                                   String hostname, int port, UserInfo userinfo, Proxy proxy)
            throws JSchException {
        String key = poolKey(username, hostname, port);
        JSchSession j = pool.remove(key);
        if (j != null && !j.session.isConnected())
            j = null;

        if (j == null) {
            Session session;
            try {
                session = _session(username, password, hostname, port, userinfo, proxy);
            } catch (JSchException e) {
                if (isAuthenticationFailure(e)) {
                    session = _session(username, password, hostname, port, userinfo, proxy);
                } else {
                    throw e;
                }
            }

            pool.put(key, new JSchSession(session, key));

            return new JSchSession(session, key);
        }
        return j;

    }

    private static synchronized JSch getJSch() {
        if (jsch == null)
            jsch = new JSch();

        return jsch;
    }

    private static Session _session(String username, String password,
                                    String hostname, int port, UserInfo userinfo, Proxy proxy)
            throws JSchException {
        Session s = sessionFactory == null ? getJSch().getSession(username, hostname, port) : sessionFactory.getSession(username, hostname, port);
        if (password != null)
            s.setPassword(password);
        s.setUserInfo(userinfo);
        if (proxy != null)
            s.setProxy(proxy);
        s.setTimeout(60000);
        s.setServerAliveInterval(60000);
        try {
            s.connect(60000);
            return s;
        } catch (Throwable e) {
            throw new JSchException("connect", e);
        }
    }

    private static String poolKey(String username, String hostname, int port) {
        return username + '@' + hostname + ':' + port;
    }

    private static boolean isAuthenticationFailure(JSchException ee) {
        return "Auth fail".equals(ee.getMessage());
    }

    public static void setSessionFactory(SessionFactory sf) {
        sessionFactory = sf;
    }

    static void useSSHAgent(boolean use) {
        IdentityRepository ir = null;
        if (use) {
            try {
                Class c = Class.forName("com.jcraft.jcterm.JCTermIdentityRepository");
                ir = (IdentityRepository) (c.getConstructor().newInstance());
            } catch (NoClassDefFoundError | Exception e) {
                System.err.println(e);
            }
            if (ir == null) {
                System.err.println("JCTermIdentityRepository is not available.");
            }
        }
        if (ir != null)
            getJSch().setIdentityRepository(ir);
    }

    public void dispose() {
        if (session.isConnected()) {
            session.disconnect();
        }
        pool.remove(key);
    }

    @FunctionalInterface
    interface SessionFactory {
        Session getSession(String username, String hostname, int port)
                ;
    }
}