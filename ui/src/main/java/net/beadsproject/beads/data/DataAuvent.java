/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.data;

import net.beadsproject.beads.core.Auvent;
import net.beadsproject.beads.core.UGen;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A bead that stores properties as key/value pairs. Keys must be Strings, and
 * values may be any Object. Implements the Map interface.
 *
 * @author Benito Crawford
 * @version 0.9.6
 */
public class DataAuvent extends Auvent implements Map<String, Object> {
    private final Map<String, Object> properties;

    /**
     * Creates a DataBead instance with no defined properties. Properties may be
     * added with {@link #put(String, Object) put()}.
     */
    public DataAuvent() {
        properties = new HashMap<>();
    }

    /**
     * Creates a DataBead with one property defined by the specified key and
     * value. Other properties may be added with {@link #put(String, Object)
     * put()}.
     *
     * @param key The property name.
     * @param val The property value.
     */
    public DataAuvent(String key, Object val) {
        properties = new HashMap<>();
        if (key != null)
            properties.put(key, val);
    }

    /**
     * Creates a DataBead instance with properties specified by a String array
     * that are set to corresponding values specified by an Object array. Other
     * properties may be added with {@link #put(String, Object) put()}.
     *
     * @param proparr The array of property names.
     * @param valarr  The array of Object values.
     */
    public DataAuvent(String[] proparr, Object[] valarr) {
        properties = new HashMap<>();
        if (proparr != null && valarr != null) {
            int s = Math.min(proparr.length, valarr.length);
            for (int i = 0; i < s; i++) {
                if (proparr[i] != null)
                    properties.put(proparr[i], valarr[i]);
            }
        }
    }

    /**
     * Creates a DataBead instance that uses a Map (a Hashtable, for example)
     * for its properties. (This does not copy the input Map, so any changes to
     * it will change the properties of the DataBead!) Other properties may be
     * added with {@link #put(String, Object) put()}.
     *
     * @param ht The input Map.
     */
    public DataAuvent(Map<String, Object> ht) {
        properties = ht == null ? new HashMap<>() : ht;
    }

    /**
     * Creates a new DataBead from an interleaved series of key-value pairs,
     * which must be in the form (String, Object, String, Object...), etc.
     *
     * @param objects interleaved series of key-value pairs.
     */
    public DataAuvent(Object... objects) {
        properties = new HashMap<>();
        putAll(objects);
    }

    /**
     * If the input message is a DataBead, this adds the properties from the
     * message Bead to this one. (Equivalent to {@link #putAll(DataAuvent)} .)
     */
    @Override
    public void on(Auvent message) {
        if (message instanceof DataAuvent) {
            putAll(((DataAuvent) message).properties);
        }
    }

    /**
     * Adds the properties from the input DataBead to this one.
     *
     * @param db The input DataBead.
     */
    private void putAll(DataAuvent db) {
        putAll(db.properties);
    }

    /**
     * Adds an interleaved series of key-value pairs to the DataBead, which must
     * be in the form (String, Object, String, Object...), etc.
     *
     * @param objects an interleaved series of key-value pairs.
     */
    private void putAll(Object... objects) {
        for (int i = 0; i < objects.length; i += 2) {
            put((String) objects[i], objects[i + 1]);
        }
    }

//    /**
//     * Uses the parameters stored by this DataBead, this method configures the
//     * given object by using reflection to discover appropriate setter methods.
//     * For example, if the object has a method <code>setX(float f)</code> then
//     * the key-value pair <String "x", float 0.5f> will be used to invoke this
//     * method. Errors are caught and printed (actually, not right now...).
//     * <p>
//     * Be aware that this may not work as expected with all objects. Use with
//     * care...
//     *
//     * @param o the Object to configure.
//     */
//    public void configureObject(Object o) {
//        if (o instanceof DataBeadReceiver) {
//            ((DataBeadReceiver) o).sendData(this);
//        } else {
//            for (Entry<String, Object> stringObjectEntry : properties.entrySet()) {
//
//                String methodName = "setAt" + (stringObjectEntry.getKey()).substring(0, 1).toUpperCase()
//                        + (stringObjectEntry.getKey()).substring(1);
//
//                Object theArg = stringObjectEntry.getValue();
//                try {
//
//
//                    Method m = o.getClass().getMethod(methodName,
//                            theArg.getClass());
//
//                    m.invoke(o, theArg);
//                } catch (Exception e) {
//
//                }
//            }
//        }
//    }

    /**
     * Gets a float representation of the specified property; returns the
     * specified default value if that property doesn't exist or cannot be cast
     * as a float.
     * <p>
     * This method is a useful way to update <code>float</code> parameters in a
     * class:
     * <p>
     * <code>float param = startval;<br>
     * ...<br>
     * <code>param = databead.getFloat("paramKey", param);</code>
     *
     * @param key        The property key.
     * @param defaultVal The value to return if the property does not contain a
     *                   float-convertible value.
     * @return The property value, or the default value if there is no float
     * representation of the property.
     */
    public float getFloat(String key, float defaultVal) {
        Float ret;
        return (ret = getFloatObject(key)) == null ? defaultVal : ret;
    }

    /**
     * Gets a Float representation of the specified property; returns
     * <code>null</code> if that property doesn't exist or cannot be cast as a
     * Float.
     *
     * @param key The property key.
     * @return The property value, or the default value if there is no float
     * representation of the property.
     */

    private Float getFloatObject(String key) {
        Object o = get(key);
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else if (o instanceof String) {
            try {
                return Float.parseFloat((String) o);
            } catch (Exception e) {
            }
        } else if (o instanceof Boolean) {
            return (Boolean) o ? 1.0f : 0.0f;
        }
        return null;
    }

    /**
     * Returns the UGen value for the specified key. If the value stored at the
     * key is not a UGen, it returns <code>null</code>.
     *
     * @param key The key.
     * @return The UGen if it exists.
     */
    public UGen getUGen(String key) {
        Object o = get(key);
        return o instanceof UGen ? (UGen) o : null;
    }

    /**
     * Gets a float array from the value stored with the specified key. If the
     * stored value is actually of type <code>float[]</code>, the method returns
     * that object. In the event that the stored value is an array of numbers of
     * some other type, the method will return a new float array filled with
     * values converted to float; an array of doubles, for instance, will be
     * recast as floats. Single numbers will be returned as a one-element float
     * array. If no array can be formed from the stored value, or if there is no
     * stored value, the method returns <code>null</code>.
     *
     * @param key The key.
     * @return The derived float array.
     */
    public float[] getFloatArray(String key) {
        Object o = get(key);
        float[] ret;
        if (o instanceof Number[] n) {
            ret = new float[n.length];
            for (int i = 0; i < n.length; i++) {
                ret[i] = n[i].floatValue();
            }
        } else if (o instanceof float[]) {
            ret = (float[]) o;
        } else if (o instanceof double[] p) {
            ret = new float[p.length];
            for (int i = 0; i < p.length; i++) {
                ret[i] = (float) p[i];
            }
        } else if (o instanceof int[] p) {
            ret = new float[p.length];
            for (int i = 0; i < p.length; i++) {
                ret[i] = p[i];
            }
        } else if (o instanceof long[] p) {
            ret = new float[p.length];
            for (int i = 0; i < p.length; i++) {
                ret[i] = p[i];
            }
        } else if (o instanceof char[] p) {
            ret = new float[p.length];
            for (int i = 0; i < p.length; i++) {
                ret[i] = p[i];
            }
        } else if (o instanceof byte[] p) {
            ret = new float[p.length];
            for (int i = 0; i < p.length; i++) {
                ret[i] = p[i];
            }
        } else if (o instanceof short[] p) {
            ret = new float[p.length];
            for (int i = 0; i < p.length; i++) {
                ret[i] = p[i];
            }
        } else if (o instanceof boolean[] p) {
            ret = new float[p.length];
            for (int i = 0; i < p.length; i++) {
                ret[i] = p[i] ? 1 : 0;
            }
        } else if (o instanceof Number) {
            ret = new float[]{((Number) o).floatValue()};
        } else {
            ret = null;
        }

        return ret;
    }

    /**
     * Gets an array of UGens from the value stored with the specified key. If
     * the value is a UGen object (not an array), the method returns a new
     * one-element UGen array with the value stored in it. If the value is
     * empty, or not a UGen array or UGen, the method returns <code>null</code>.
     *
     * @param key The key.
     * @return The UGen array.
     */
    public UGen[] getUGenArray(String key) {
        Object o = get(key);
        if (o instanceof UGen[]) {
            return (UGen[]) o;
        } else if (o instanceof UGen) {
            return new UGen[]{(UGen) o};
        } else {
            return null;
        }
    }

    /**
     * Returns a new DataBead with a shallow copy of the the original DataBead's
     * properties.
     */
    @Override
    public DataAuvent clone() {
        DataAuvent ret = new DataAuvent();
        ret.setName(getName());
        ret.putAll(properties);
        return ret;
    }

    /**
     * Creates a new DataBead that combines properties from both input
     * DataBeads. If the same key exists in both, the value from the first one
     * is used.
     *
     * @param a The first input DataBead.
     * @param b The second input DataBead.
     * @return The new DataBead.
     */
    public static DataAuvent combine(DataAuvent a, DataAuvent b) {
        DataAuvent c = new DataAuvent();
        c.putAll(b);
        c.putAll(a);
        return c;
    }

    @Override
    public String toString() {
        return super.toString() + ":\n" + properties;
    }

    /*
     * These implement the Map interface methods.
     */

    @Override
    public boolean containsKey(Object key) {
        return properties.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return properties.entrySet();
    }

    @Override
    public Object get(Object key) {
        return properties.get(key);
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return properties.keySet();
    }

    @Override
    public Object put(String key, Object value) {
        return properties.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        properties.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        return properties.remove(key);
    }

    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public Collection<Object> values() {
        return properties.values();
    }

    @Override
    public void clear() {
        properties.clear();
    }

}