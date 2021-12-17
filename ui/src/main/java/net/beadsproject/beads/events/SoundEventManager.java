/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.events;

import net.beadsproject.beads.core.UGen;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * SoundEventManager allows {@link SoundEvent}s to be played where the class of the {@link SoundEvent}
 * is specified by an entry in the {@link Map} argument referenced by the key "class".
 */
enum SoundEventManager {
	;


	/**
     * Play the {@link SoundEvent} specified by the {@link Map} argument. The {@link SoundEvent}
     * to be played is specified by an entry in the {@link Map} with key "class" and value the class of
     * the {@link SoundEvent}.
     *
     * @param output     the {@link UGen} to which this {@link SoundEvent} should connect.
     * @param parameters the parameters for the {@link SoundEvent}.
     * @return the {@link UGen} at the root of the {@link SoundEvent}.
     */
    @SuppressWarnings("unchecked")
    public static UGen play(UGen output, Map<String, Object> parameters) {
        try {
            Class<? extends SoundEvent> soundEventClass = (Class<? extends SoundEvent>) parameters.get("class");
            if (soundEventClass == null) System.out.println("could not find class for SoundEvent");
            Method playMethod = soundEventClass.getMethod("play", UGen.class, Map.class);
            SoundEvent event = soundEventClass.getConstructor().newInstance();
            return (UGen) playMethod.invoke(event, output, parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
