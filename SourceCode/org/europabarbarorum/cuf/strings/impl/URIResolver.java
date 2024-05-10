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
import java.util.HashMap;
import org.europabarbarorum.cuf.macro.Macro;
import org.europabarbarorum.cuf.strings.impl.StringMapping.BasicMapping;
import org.europabarbarorum.cuf.strings.impl.StringMapping.ConfigurationKey;
import org.europabarbarorum.cuf.strings.impl.StringMapping.ConfiguredMapping;
import org.europabarbarorum.cuf.support.PathParser;

/**
 * Interface to define the component which converts an XML namespace/URI into
 * the required {@link StringMapping} objects which are used for generating output
 * from source text.
 * @author Johan Ouwerkerk
 */
public interface URIResolver {

    /**
     * This prefix can be used by namespace URI's to create an alias for a previously
     * used URI. This is useful when multiple versions of the
     * same {@link StringMapping} implementations are required: for instance when
     * compilation requires two differently configured {@link StringMapping mappings} using the same
     * {@link Macro macros}. This prefix can be applied
     * recursively: {@code copy:copy:test} is an alias for {@code test} the same way {@code copy:test} is.
     */
    public static final String URI_COPY_PREFIX = "copy:";

    /**
     * Maps an XML namespace/URI to a {@link StringMapping} instance.
     * If the given namespace/URI starts with the value of {@link #URI_COPY_PREFIX} then
     * this method must provide a <em>new</em> {@link StringMapping} instance
     * resolved from the remainder of the namespace URI (after the prefix).
     * @param uri the given namespace in the source XML document.
     * @return a ready-to-use {@link StringMapping} instance
     * @throws Exception if an error occurs or the URI cannot be resolved.
     */
    public StringMapping resolve (String uri) throws Exception;

    /**
     * A simple {@link URIResolver} that interprets an URI string as file path.
     * This implementation is not able to pass configuration values to the {@link StringMapping} objects
     * it creates.
     */
    public static class URIResolverImpl extends PathParser.Glob implements
            URIResolver {

        /**
         * Creates an {@link URIResolver} that resolves its URI relative to the given source document.
         * @param doc a {@link File} corresponding to the path of the source document that supplies the
         * XML namespaces which this {@link URIResolver} resolves.
         */
        public URIResolverImpl (File doc) {
            super(doc);
            if (doc == null) {
                setContext(getRoot(getPWD()));
            }
        }

        /**
         * Implements XML namespace resolution. It works like this:
         * <ol>
         * <li>If uri denotes an absolute path: use it as-is.</li>
         * <li>If uri denotes a relative path check if it denotes a valid file relative to
         * the base directory. If true, use that path.</li>
         * <li>If the above failed; check if it denotes a valid file relative
         * to the current working directory. If true use that path.</li>
         * <li>If all of the above failed: return null to signal that an exception should be raised.</li>
         * </ol>
         * @param uri the uri to resolve
         * @return a {@link File} that appears to correspond to the namespace given, which is also readable by the program.
         * If no such file can be found, this method returns null to signal the error.
         */
        private File get (String uri) {
            if (uri != null && uri.startsWith(URI_COPY_PREFIX)) {
                return get(uri.substring(URI_COPY_PREFIX.length()));
            }
            File r = new File(uri);
            File f = check(resolveFile(r));
            return f == null ? check(r) : f;
        }

        private File check (File f) {
            return f.exists() && f.canRead() ? f : null;
        }

        @Override
        public StringMapping resolve (String uri) throws Exception {
            File f = get(uri);
            if (f != null) {
                return createMapping(f);
            }
            throw new InvalidURIException(uri);
        }

        /**
         * Hook for subclasses to substitute their own {@link StringMapping} types.
         * By default this method returns an instance of {@link BasicMapping}.
         * @param f the macro file to be used by the {@link StringMapping}.
         * @return a new {@link BasicMapping}.
         * @throws Exception
         */
        protected StringMapping createMapping (File f) throws Exception {
            return new BasicMapping(f);
        }
    }

    /**
     * A {@link URIResolver} which bridges multiple resolvers together.
     * This is useful when a single input document requires different types of {@link URIResolver}
     * for correctly resolving namespaces and these namespaces are known in advance.
     * <p>
     * In other words the result {@link URIResolver} can be used with a {@link KeyResolver}
     * that generates such namespaces from Strings keys.
     */
    public static class MixedURIResolver extends HashMap<String, URIResolver> implements
            URIResolver {

        /**
         * Create a {@link MixedURIResolver} without fallback {@link URIResolver}.
         */
        public MixedURIResolver () {
            this(null);
        }

        /**
         * Create a {@link MixedURIResolver} with fallback {@link URIResolver}.
         * @param defaultResolver the {@link URIResolver} to use as fallback when a given
         * URI was not otherwise bound in this {@link MixedURIResolver}.
         */
        public MixedURIResolver (URIResolver defaultResolver) {
            this.defaultResolver = defaultResolver;
        }
        private final URIResolver defaultResolver;

        @Override
        public StringMapping resolve (String uri) throws Exception {
            URIResolver r = get(uri);
            if (r != null) {
                return r.resolve(uri);
            }
            if (defaultResolver != null) {
                return defaultResolver.resolve(uri);
            }
            throw new InvalidURIException(uri);
        }
    }

    /**
     * Exception that indicates a given URI is invalid.
     */
    public static class InvalidURIException extends IllegalArgumentException {

        /**
         * Create a new {@link InvalidURIException}.
         * @param uri the URI which is invalid
         */
        public InvalidURIException (String uri) {
            super(Messages.InvalidURI.format(uri));
        }
    }

    /**
     * An {@link URIResolver} wrapper that provides configuration of {@link ConfiguredMapping} instances
     * generated by the wrapped implementation (if applicable).
     */
    public static class PreConfiguredURIResolver implements URIResolver {

        private final ResolverConfiguration configuration;
        private final URIResolver impl;

        /**
         * Create a {@link PreConfiguredURIResolver}.
         * @param impl an {@link URIResolver} that provides implementation of the interface methods.
         * @param configuration a {@link ResolverConfiguration} which provides the data for
         * initialising {@link ConfiguredMapping} objects if they are generated by the
         * wrapped object.
         */
        public PreConfiguredURIResolver (URIResolver impl,
                                         ResolverConfiguration configuration) {
            this.impl = impl;
            this.configuration = configuration;
        }

        @Override
        public StringMapping resolve (String uri) throws Exception {
            StringMapping map = impl.resolve(uri);
            if (map instanceof ConfiguredMapping) {
                configureMapping((ConfiguredMapping) map, configuration);
            }
            return map;
        }

        /**
         * Initialises a {@link ConfiguredMapping}.
         * @param mapping the {@link ConfiguredMapping} to intialise.
         * @param conf the {@link ResolverConfiguration} which provides the values for settings
         * of the given {@link ConfiguredMapping}.
         * @throws Exception if an error occurs
         */
        public static void configureMapping (ConfiguredMapping mapping,
                                             ResolverConfiguration conf) throws
                Exception {
            for (ConfigurationKey key : mapping) {
                key.set(key.required()
                        ? getRequiredValue(key, conf)
                        : getValue(key, conf));
            }
        }

        private static String getValue (ConfigurationKey key,
                                        ResolverConfiguration conf) {
            return conf.get(key.uri(), key.name());
        }

        private static String getRequiredValue (ConfigurationKey key,
                                                ResolverConfiguration attrs) throws
                Exception {
            String val = getValue(key, attrs);
            if (val == null) {
                throw new IllegalArgumentException(
                        Messages.ConfigurationKeyNotFound.format(key.name(),
                                                                 key.uri()));
            }
            return val;
        }
    }

    /**
     * Interface to describe lookup of values for {@link ConfigurationKey} instances; this
     * interface provides a common super-type for the various underlying data objects.
     */
    public static interface ResolverConfiguration {

        /**
         * Lookup a configured value.
         * @param uri the URI for which a setting should be retrieved. This value is
         * analogous to {@link ConfigurationKey#uri()}.
         * @param name the name of the setting to look up. This value is analogous to
         * {@link ConfigurationKey#name() }.
         * @return the associated value, or null if no value was provided/set.
         */
        String get (String uri, String name);
    }

    /**
     * A {@link HashMap} backed implementation of {@link ResolverConfiguration} with
     * additional {@link #put(java.lang.String, java.lang.String, java.lang.String) } operation to
     * set configuration values.
     */
    public static class MapConfiguration implements ResolverConfiguration {

        private final HashMap<String, String> map =
                new HashMap<String, String>();

        @Override
        public String get (String uri, String name) {
            return map.get(mapKey(uri, name));
        }

        /**
         * Set a configuration value.
         * @param uri the URI for which the configuration key must be set.
         * @param key the name of the {@link ConfigurationKey} key to set
         * @param value the (new) value to use.
         */
        public void put (String uri, String key, String value) {
            map.put(mapKey(uri, key), value);
        }

        /**
         * Generates a string from two component strings such that the results obtained from two
         * distinct method calls are equal if and only if the corresponding parameters are.
         * @param c1 the first component.
         * @param c2 the second component.
         * @return a string that uniquely identifies the two distinct components.
         */
        public static String mapKey (String c1, String c2) {
            return String.format("<%1$s[%2$s]>", c1, c2);
        }
    }
}
