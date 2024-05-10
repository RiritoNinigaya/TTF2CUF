/*
 *
 *  Copyright (C) 2010, 2011, 2012 The Europa Barbarorum Team
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of The Europa Barbarorum Team nor the
 *        names of other contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL The Europa Barbarorum Team BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.europabarbarorum.cuf.support;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import org.europabarbarorum.cuf.support.ResourceHelp.ResourceType;

/**
 * Interface to describe program configuration settings.
 * @author Johan Ouwerkerk
 * @param <T> type of the value use for this setting
 */
public class Setting<T> {

    /**
     * Create a new {@link Setting} object.
     * @param key name of the setting.
     * @param def default value of the setting.
     * @param type a {@link ResourceType} to parse strings into values for this setting.
     * @param mod the {@link Modifiable} setting for this preference.
     */
    public Setting (String key, String def, ResourceType type, Modifiable mod) {
        this.mod = mod;
        this.type = type;
        this.key = key;
        this.def = def;
        this.init();
        if (!prefsMap.containsKey(key)) {
            prefsMap.put(key, this);
        }
    }

    /**
     * Initialize this preference with the value from a preference, a cli argument or
     * the default value.
     */
    private void init () {
        String val = System.getProperty(key);
        xset(def, false);
        if (mod == Modifiable.Cli) {
            if (val != null) {
                set(val, false);
            }
            mod = Modifiable.No;
        }
        else {
            set(val == null ? prefs.get(name(), def) : val, false);
        }
    }
    /**
     * Default value of the setting.
     * @see #defaultValue()
     */
    private final String def;
    /**
     * Propery key for commandline configuration of the setting via the -D switch of the JVM.
     * @see #init()
     */
    private final String key;
    /**
     * Used to determine how to parse a string.
     * @see #parseString(java.lang.String)
     * @see #set(java.lang.Object, boolean)
     */
    private final ResourceType type;
    /**
     * Current value of the setting.
     * @see #get()
     */
    private T value;
    /**
     * How modifiable this setting currently is.
     * @see #isModifiable()
     * @see #init()
     */
    private Modifiable mod;

    /**
     * Get a default value for a setting.
     * @return the default value of a setting in string form.
     */
    public String defaultValue () {
        return def;
    }

    /**
     * Set the value of a setting. The parameter must either be a string
     * which can be parsed as an object of the type of the Setting value, or alternatively
     * it should be an instance of that type. This method should do nothing if this setting is not modifiable.
     * It should not throw exceptions.
     * @param value either a string or an instance of desired type.
     * @param store whether or not this change should be made persistent (stored)
     * @see #get()
     * @see #isModifiable()
     */
    public void set (String value, boolean store) {
        if (isModifiable()) {
            xset(value, store);
        }
    }

    /**
     * Set the value of a setting. The parameter must either be a string
     * which can be parsed as an object of the type of the Setting value, or alternatively
     * it should be an instance of that type. This method should do nothing if this setting is not modifiable.
     * It should not throw exceptions.
     * @param value either a string or an instance of desired type.
     * @param store whether or not this change should be made persistent (stored)
     * @see #get()
     * @see #isModifiable()
     */
    public void set (T value, boolean store) {
        if (isModifiable()) {
            xset(value, store);
        }
    }

    /**
     * Clears all settings that use the same backing store as this {@link Setting}.
     * This method does not reset defaults.
     * @see #clear()
     */
    public static void clearAll () {
        try {
            prefs.removeNode();
            prefs.flush();
            prefs = Preferences.userNodeForPackage(Setting.class);
        }
        catch (Exception e) {
            IOHelp.handleExceptions(Setting.class, "clearAll", e,
                                    Messages.SettingCleanError, e.getMessage());
        }
    }

    /**
     * Get the value of the setting.
     * @return the value the setting currently has.
     * @see #set(java.lang.Object, boolean)
     */
    public T get () {
        return value;
    }

    /**
     * Clears the setting from any backing store.
     * This method does not reset a setting to a default value.
     * Use code like this to do both:
     * <blockquote><pre>
     * {@code void remove(Setting setting) {
     *      setting.clear();
     *      setting.set(setting.defaultValue(), false);
     * }
     * }</pre</blockquote>
     * @see #defaultValue()
     * @see #set(java.lang.Object, boolean)
     */
    public void clear () {
        try {
            prefs.remove(key);
            prefs.sync();
        }
        catch (Exception e) {
            IOHelp.handleExceptions(Setting.class, "clear", e,
                                    Messages.SettingClearError, key, e.
                    getMessage());
        }
    }

    /**
     * Returns whether or not this {@link Setting} is configurable. Some settings may be
     * unavailable in a given environment, in which case this method returns false.
     * @return wether or not this {@link Setting} can be altered.
     */
    public boolean isModifiable () {
        return mod != Modifiable.No;
    }

    /**
     * Get the setting name. This name should uniquely identify this setting within its
     * type/group/category. If all settings of the same type/category/group
     * are stored in a single file, then this name must be a reliable indentifier of this setting.
     * @return a name which indentifies this setting.
     */
    public String name () {
        return key;
    }

    /**
     * Shorthand for {@link #set(java.lang.Object, boolean) } with “true” as second parameter.
     * @param value the new value for this setting.
     */
    public void set (T value) {
        set(value, true);
    }

    /**
     * Shorthand for {@link #set(java.lang.String, boolean) } with “true” as second parameter.
     * @param value the new value for this setting.
     */
    public void set (String value) {
        set(value, true);
    }

    /**
     * Low level method that implements the core of both {@link #init() } and {@link #set(java.lang.Object, boolean) }.
     * @param value value to set
     * @param save whether or not to save the new setting to the {@link Preferences} backing store.
     */
    @SuppressWarnings("unchecked")
    private void xset (Object value, boolean save) {
        try {
            if (value == null) {
                throw new IllegalArgumentException(Messages.SettingValueInvalid.
                        getText());
            }
            Class cls = value.getClass();
            if (cls.equals(type.resourceType())) {
                this.value = (T) value;
                if (save) {
                    prefs.put(name(), value.toString());
                }
                return;
            }
            if (cls.equals(String.class)) {
                String s = value.toString();
                this.value = (T) type.parse(s);
                if (save) {
                    prefs.put(name(), s);
                }
                return;
            }
            throw new IllegalArgumentException(Messages.SettingValueInvalid.
                    getText());
        }
        catch (Exception e) {
            IOHelp.handleExceptions(Setting.class, "xset", e,
                                    Messages.SettingParseError, key,
                                    this.value, value, e.getMessage());
        }
    }
    private static Preferences prefs =
            Preferences.userNodeForPackage(Setting.class);
    private static ConcurrentHashMap<String, Setting> prefsMap =
            new ConcurrentHashMap<String, Setting>();

    /**
     * Get a list of preference names (used so far).
     * @return a {@link Set} of preference names.
     */
    public static Set<String> getPrefNames () {
        return prefsMap.keySet();
    }

    /**
     * Get a preference by name.
     * @param key the name of the preference to lookup.
     * @return a {@link Setting} object for the given name, or null if not found.
     */
    public static Setting forName (String key) {
        return prefsMap.get(key);
    }

    /**
     * Enumeration of how modifiable a setting can be.
     */
    public static enum Modifiable {

        /**
         * Not modifiable at all.
         */
        No,
        /**
         * Modifiable at start up time only.
         * @see Setting#init()
         */
        Cli,
        /**
         * Modifiable throughout the program. Values may be loaded from and written to a
         * {@link Preferences} object.
         */
        Conf
    }
}
