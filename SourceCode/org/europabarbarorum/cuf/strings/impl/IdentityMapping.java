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
package org.europabarbarorum.cuf.strings.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.europabarbarorum.cuf.macro.Macro;
import org.europabarbarorum.cuf.strings.impl.StringMapping.ConfiguredMapping;
import org.europabarbarorum.cuf.support.NotEditableException;
import org.europabarbarorum.cuf.support.ResourceHelp.BooleanResource;

/**
 * A {@link StringMapping.ConfiguredMapping} which does not map input text,
 * except for escape sequences. Resolving escape sequences can be disabled by
 * specifying a false value for the “process”
 * {@link StringMapping.ConfigurationKey} attribute.
 *
 * @author Johan Ouwerkerk
 */
public class IdentityMapping extends EscapeMapping implements ConfiguredMapping {

    private final String uri;
    private boolean process = true;

    /**
     * Create a new {@link IdentityMapping}.
     *
     * @param uri namespace URI used for identification purposes.
     */
    public IdentityMapping (String uri) {
        super(null);
        this.uri = uri;
    }

    @Override
    protected String process (String text) {
        return process ? super.process(text) : text;
    }

    @Override
    protected Map<String, Macro> init (File macrofile) {
        Map<String, Macro> map = new HashMap<String, Macro>();
        map.put(macroName, macroImpl);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Implementation of the {@link Iterable} contract required by
     * {@link ConfiguredMapping}. This method allows other objects to walk the
     * configuration attributes of this {@link IdentityMapping} in a for-each
     * loop.
     *
     * @return an {@link Iterator} of {@link StringMapping.ConfigurationKey}
     * instances that represent the attributes used for configuring this {@link IdentityMapping}.
     */
    @Override
    public Iterator<ConfigurationKey> iterator () {
        return new Iterator<ConfigurationKey>() {

            private boolean toVisit = true;

            @Override
            public boolean hasNext () {
                return toVisit;
            }

            @Override
            public ConfigurationKey next () throws NoSuchElementException {
                if (toVisit) {
                    toVisit = false;
                    return key;
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove () {
                throw new NotEditableException();
            }
        };
    }
    private ConfigurationKey key = new ConfigurationKey() {

        @Override
        public String name () {
            return "process";
        }

        @Override
        public String uri () {
            return uri;
        }

        @Override
        public void set (String value) throws Exception {
            BooleanResource b = new BooleanResource();
            if (value != null) {
                process = b.parse(value);
            }
        }

        @Override
        public boolean required () {
            return false;
        }
    };
    /**
     * Name of the only {@link Macro} recognised by a {@link IdentityMapping}.
     */
    public static final String macroName = "id";
    private static final Macro macroImpl = new Macro() {

        @Override
        public Character map (Character in) {
            return in;
        }

        @Override
        public String map (String in) {
            return in;
        }

        @Override
        public String name () {
            return macroName;
        }

        @Override
        public int size () {
            return Character.MAX_CODE_POINT;
        }

        @Override
        public Iterator<Mapping> iterator () {
            return new Iterator<Macro.Mapping>() {

                @Override
                public boolean hasNext () {
                    return false;
                }

                @Override
                public Mapping next () {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove () {
                    throw new NotEditableException();
                }
            };
        }
    };
}
