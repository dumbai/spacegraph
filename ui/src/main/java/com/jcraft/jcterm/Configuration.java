/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 20012 ymnk, JCraft,Inc.
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

/**
 * This class abstracts settings from JCTerm.
 * - font size
 * - pairs of foreground and background color
 * - list of destinations for the prompt.
 *
 * @see ConfigurationRepository
 */
public class Configuration {
    private static final int FONT_SIZE = 18;
    private static final String[] FG_BG = {"#000000:#ffffff", "#ffffff:#000000"};
    private static final String[] DESTINATIONS = new String[0];

    public String name = "default";
    public int font_size = FONT_SIZE;
    public String[] fg_bg = FG_BG.clone();
    public String[] destinations = DESTINATIONS;

    private static String[] add(String d, String[] array) {
        int i = 0;
        while (i < array.length) {
            if (d.equals(array[i])) {
                if (i != 0) {
                    System.arraycopy(array, 0, array, 1, i);
                    array[0] = d;
                }
                return array;
            }
            i++;
        }
        String[] foo = new String[array.length + 1];
        if (array.length > 0) {
            System.arraycopy(array, 0, foo, 1, array.length);
        }
        foo[0] = d;
        array = foo;
        return array;
    }

    public synchronized void addDestination(String d) {
        destinations = add(d, destinations);
    }

    public synchronized void addFgBg(String d) {
        fg_bg = add(d, fg_bg);
    }
























}
