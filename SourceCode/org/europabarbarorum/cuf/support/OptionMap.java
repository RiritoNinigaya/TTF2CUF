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

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Map} of {@link DefaultOption} class keys and corresponding values.
 * @see #getOption(java.lang.Class) 
 * @author Johan Ouwerkerk
 */
public class OptionMap extends HashMap<Class<? extends DefaultOption>, DefaultOption> {

    /**
     * Create a new {@link OptionMap} and initialise it with given settings.
     * @param m a {@link Map} of {@link DefaultOption} class keys and corresponding values.
     */
    public OptionMap (
            Map<? extends Class<? extends DefaultOption>, ? extends DefaultOption> m) {
        super(m);
    }

    /**
     * Create a new {@link OptionMap}.
     */
    public OptionMap () {
    }

    /**
     * Looks up a {@link DefaultOption} setting.
     * @param <K> type of {@link DefaultOption} to get
     * @param key the {@link Class} key to the given options map.
     * @return the value corresponding to the given key, or a default value if there is either no such
     * value or the value found is not of the same type (class) as the given key.
     */
    @SuppressWarnings("unchecked")
    public <K extends DefaultOption> K getOption (Class<K> key) {
        Object v = get(key);
        if (!key.isInstance(v)) {
            K def = getDefault(key);
            if (v != null) {
                IOHelp.warn(DefaultOption.class,
                            Messages.InvalidOptionValue,
                            new Object[] { key.getCanonicalName(),
                                           v,
                                           def });
            }
            put(key, def);
            return def;
        }
        return (K) v;
    }

    /**
     * Get the default value for a given option {@link Class}.
     * @param <K> some kind of {@link DefaultOption} to lookup the default value.
     * @param key the {@link Class} of the setting to retrieve the default for.
     * @return the default value (instance) of the given {@link DefaultOption setting}.
     */
    @SuppressWarnings("unchecked")
    public static <K extends DefaultOption> K getDefault (Class<K> key) {
        if (key.isEnum()) {
            K[] opts = key.getEnumConstants();
            return (K) opts[0].defaultOption();
        }
        try {
            return (K) key.newInstance().defaultOption();
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(
                    Messages.IllegalOptionType.format(key.getCanonicalName(),
                                                      ex.getLocalizedMessage()),
                    ex);
        }
    }
}
