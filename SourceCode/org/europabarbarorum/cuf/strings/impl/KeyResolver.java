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

import java.util.HashMap;
import java.util.Map;

/**
 * Interface similar to {@link URIResolver} which resolves Strings keys to
 * {@link StringMapping} and macro name.
 * @author Johan Ouwerkerk
 */
public interface KeyResolver {

    /**
     * Get the {@link StringMapping} to use for resolving macro transformation on the corresponding value
     * part of the Strings data.
     * @param key the given Strings key
     * @return a {@link StringMapping} suitably initialised for performing its task as defined in the interface.
     * @throws Exception if an error occurs
     */
    StringMapping forKey (String key) throws Exception;

    /**
     * Get the name of the macro to select in a given {@link StringMapping} for resolving macro transformation
     * on the corresponding part of the Strings data.
     * @param key the given Strings key.
     * @return the name of a macro recognized by the {@link StringMapping} returned by {@link #forKey(java.lang.String) }.
     */
    String getMacro (String key);

    /**
     * Skeleton implementation of {@link KeyResolver}. It works by mapping a key string to
     * an URI string value, and using {@link URIResolver} to
     */
    public abstract static class KeyResolverImpl implements KeyResolver {

        /**
         * Alternative implementation: simply use a single {@link StringMapping} and
         * macro for all Strings values.
         * @param impl the {@link StringMapping} to use.
         * @param macro the name of the macro to use, must be recognized by the given
         * {@link StringMapping}.
         * @return a {@link KeyResolver} that uses a single {@link StringMapping} and macro
         * for all Strings values.
         */
        public static KeyResolver wrap (final StringMapping impl,
                                        final String macro) {
            return new KeyResolver() {

                @Override
                public StringMapping forKey (String key) throws Exception {
                    return impl;
                }

                @Override
                public String getMacro (String key) {
                    return macro;
                }
            };
        }
        private final URIResolver resolver;

        /**
         * Create a {@link KeyResolverImpl}.
         * @param resolver the {@link URIResolver} used for converting String uri values to
         * {@link StringMapping} instances.
         * @see URIResolver#resolve(java.lang.String)
         */
        public KeyResolverImpl (URIResolver resolver) {
            this.resolver = resolver;
            this.solveMap = new HashMap<String, StringMapping>();
        }
        private HashMap<String, StringMapping> solveMap;

        @Override
        public StringMapping forKey (String key) throws Exception {
            String uri = getURI(key);
            if (uri == null) {
                throw new IllegalArgumentException(
                        Messages.NoURIForKey.format(key));
            }
            if (!solveMap.containsKey(uri)) {
                solveMap.put(uri, resolver.resolve(uri));
            }
            return solveMap.get(uri);
        }

        /**
         * Link a Strings key to the URI associated with a {@link StringMapping} by the
         * internal {@link URIResolver}.
         * @param key the key to trace to an URI.
         * @return an URI suitable to obtain a {@link StringMapping} from the
         * internal {@link URIResolver}
         * @see URIResolver#resolve(java.lang.String)
         */
        protected abstract String getURI (String key);

        /**
         * Link a given String URI to a macro for use in text transformations.
         * @param uri a String produced by {@link #getURI(java.lang.String) }.
         * @return the name of the macro to be used in the context of the given URI.
         */
        protected abstract String macro (String uri);

        @Override
        public String getMacro (String key) {
            String m = macro(getURI(key));
            if (m == null) {
                throw new IllegalArgumentException(
                        Messages.NoMacroForKey.format(key));
            }
            else {
                return m;
            }
        }
    }

    /**
     * Expands on {@link KeyResolverImpl} to provide a straightforward mapping for
     * macro names as well.
     * @see #macro(java.lang.String)
     */
    public static abstract class SimpleKeyResolver extends KeyResolverImpl {

        private final Map<String, String> macros;

        /**
         * Create a {@link SimpleKeyResolver}.
         * @param resolver the {@link URIResolver} to use for resolving String URIs to
         * {@link StringMapping} instances.
         * @param macros a {@link Map} of URI string keys paired to macro name values.
         * @see URIResolver#resolve(java.lang.String)
         */
        public SimpleKeyResolver (URIResolver resolver,
                                  Map<String, String> macros) {
            super(resolver);
            this.macros = macros;
        }

        @Override
        protected String macro (String uri) {
            return macros.get(uri);
        }
    }
}
