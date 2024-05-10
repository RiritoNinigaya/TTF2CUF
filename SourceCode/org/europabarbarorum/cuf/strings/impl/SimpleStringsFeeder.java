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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import org.europabarbarorum.cuf.strings.StringsWriter.FormatOption;
import org.europabarbarorum.cuf.strings.StringsWriter.StringWriter;
import org.europabarbarorum.cuf.strings.StringsWriter.StringsFeeder;
import org.europabarbarorum.cuf.strings.impl.SimpleStringsFeeder.StringsData;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.NotEditableException;
import org.europabarbarorum.cuf.support.OptionMap;

/**
 * A skeleton {@link SimpleStringsFeeder}
 * @author Johan Ouwerkerk
 */
public abstract class SimpleStringsFeeder implements
        StringsFeeder,
        Iterable<StringsData> {

    private final OptionMap options;
    private final KeyResolver resolver;

    /**
     * Create a {@link SimpleStringsFeeder}.
     * @param options a {@link Map} of {@link DefaultOption} setting types and the correspoding values.
     * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
     * a given Strings key.
     */
    protected SimpleStringsFeeder (OptionMap options,
                                   KeyResolver resolver) {
        this(options, resolver, false);
    }

    /**
     * Create a {@link SimpleStringsFeeder}.
     * @param options a {@link Map} of {@link DefaultOption} setting types and the correspoding values.
     * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
     * a given Strings key.
     * @param forceOrder whether the output Strings file should be written in
     * ordered mode (true), or keyed mode (false).
     */
    protected SimpleStringsFeeder (OptionMap options,
                                   KeyResolver resolver,
                                   boolean forceOrder) {
        this.options = options;
        if (forceOrder) {
            this.options.put(FormatOption.class, FormatOption.Ordered);
        }
        this.resolver = resolver;

    }

    /**
     * Get the object used for resolving {@link StringMapping} instances and macro names for a
     * given Strings key.
     * @return the {@link KeyResolver} this {@link SimpleStringsFeeder} was configured with.
     */
    public KeyResolver resolver () {
        return resolver;
    }

    @Override
    public void dispose () throws Exception {
    }
    /**
     * Field used to store reference to the {@link StringWriter} obtained in
     * {@link #deliverEvents(org.europabarbarorum.cuf.strings.StringsWriter.StringWriter) }.
     */
    protected StringWriter writer;
    /**
     * Field used to store reference to the key of the current key value pair being
     * compiled in {@link #deliverEvents(org.europabarbarorum.cuf.strings.StringsWriter.StringWriter) }.
     */
    protected String key;

    @Override
    public void deliverEvents (StringWriter writer) throws Exception {
        this.writer = writer;
        for (StringsData pair : this) {
            writeKey(pair.key());
            writer.value(pair.value());
        }
    }

    private void writeKey (String key) throws Exception {
        this.key = key;
        writer.key(key);
    }

    /**
     * Writes a value of a key value pair to {@link #writer}.
     * This method allows subclasses to control post processing of the value string;
     * by default it emulates XML->Strings compilation behaviour.
     * @param value the value to write.
     * @throws Exception if an error occurs.
     */
    protected void writeValue (String value) throws Exception {
        StringMapping mapping = resolver.forKey(key);
        mapping.select(resolver.getMacro(key), key);
        mapping.appendString(value, key);
        writer.value(new RawHandler(options, mapping, key).toString());
    }

    /**
     * A {@link StringsFeeder} implementation that compiles a {@link Map} into a
     * Strings file.
     */
    public static class MapSource extends SimpleStringsFeeder {

        private final Map<String, String> map;

        /**
         * Creates a {@link MapSource}.
         * @param sorted a {@link SortedMap} of string key value pairs.
         * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
         * a given Strings key.
         * @param options a {@link Map} of {@link DefaultOption} setting types and the correspoding values.
         */
        public MapSource (SortedMap<String, String> sorted,
                          KeyResolver resolver,
                          OptionMap options) {
            this(true, sorted, options, resolver);
        }

        /**
         * Creates a {@link MapSource}. This constructor assumes that output is written in keyed mode.
         * @param map a {@link Map} of string key value pairs.
         * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
         * a given Strings key.
         * @param options a {@link Map} of {@link DefaultOption} setting types and the correspoding values.
         */
        public MapSource (Map<String, String> map,
                          KeyResolver resolver,
                          OptionMap options) {
            this(false, map, options, resolver);
        }

        private MapSource (boolean forceOrder,
                           Map<String, String> map,
                           OptionMap options,
                           KeyResolver resolver) {
            super(options, resolver, forceOrder);
            this.map = map;
        }

        /**
         * Implements the {@link Iterable} contract.
         * The result is intended for read-only purposes, and is not required to implement
         * {@link Iterator#remove() }.
         * @return an {@link Iterator} which produces {@link StringsData} records.
         */
        @Override
        public Iterator<StringsData> iterator () {
            return IteratorSource.fromMap(map);
        }
    }

    /**
     * A kind of {@link StringsFeeder} which is intended to substitute values in a previously
     * generated stream of Strings records: to edit a Strings file as it were.
     */
    public static class EditorSource extends IteratorSource {

        private final Map<String, String> map;

        /**
         * Creates an {@link EditorSource}.
         * @param edits a {@link Map} of string key value pairs where the keys correspond to keys
         * producted in the given {@link SimpleStringsFeeder} and the values are replacement values for the corresponding
         * equivalents in the given {@link SimpleStringsFeeder}.
         * @param editable the {@link SimpleStringsFeeder} to edit.
         */
        public EditorSource (Map<String, String> edits,
                             SimpleStringsFeeder editable) {
            this(edits,
                 editable.iterator(),
                 editable.resolver(),
                 editable.options);
        }

        /**
         * Creates an {@link EditorSource}.
         * @param edits a {@link Map} of string key value pairs where the keys correspond to keys found in the
         * given collection of {@link StringsData} records and the values are replacement values for the corresponding
         * equivalents in these records.
         * @param editable the {@link Iterable} collection of {@link StringsData} records to edit.
         * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
         * a given Strings key.
         * @param options a {@link Map} of {@link DefaultOption} setting types and the correspoding values.
         */
        public EditorSource (Map<String, String> edits,
                             Iterable<StringsData> editable,
                             KeyResolver resolver,
                             OptionMap options) {
            this(edits, editable.iterator(), resolver, options);
        }

        /**
         * Creates an {@link EditorSource}.
         * @param edits a {@link Map} of string key value pairs where the keys correspond to keys found in the
         * {@link StringsData} records produced by the given {@link Iterator}
         * and the values are replacement values for the corresponding
         * equivalents in these records.
         * @param editable an {@link Iterator} producing the {@link StringsData} records to edit.
         * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
         * a given Strings key.
         * @param options a {@link Map} of {@link DefaultOption} setting types and the correspoding values.
         */
        public EditorSource (Map<String, String> edits,
                             Iterator<StringsData> editable,
                             KeyResolver resolver,
                             OptionMap options) {
            super(editable, resolver, options);
            this.map = edits;
        }

        /**
         * This version of {@link SimpleStringsFeeder#writeValue(java.lang.String) } substitutes values from the
         * underlying data source with those found in the internal {@link Map} of edits.
         * If no such substitute value is found the original value is kept as it is: this value is <em>not</em>
         * submitted for post-processing for it is assumed that it originated from a valid Strings file or similar
         * source.
         * @param value the value to write, or possibly remove in favour of a post-processed version of
         * an edit.
         * @throws Exception if an error occurs.
         * @see SimpleStringsFeeder#writeValue(java.lang.String)
         */
        @Override
        protected void writeValue (String value) throws Exception {
            String edit = map.get(key);
            if (edit == null) {
                writer.value(value);
            }
            else {
                super.writeValue(edit);
            }
        }
    }

    /**
     * A {@link StringsFeeder} implementation that is contstructed directly from a collection of
     * {@link StringsData} records.
     */
    public static class IteratorSource extends SimpleStringsFeeder {

        /**
         * Converts a {@link Map} into an {@link Iterator} of {@link StringsData} records.
         * @param map a {@link Map} of string key value pairs.
         * @return an {@link Iterator} that can be used to iterate over the contents of the map
         * as if these were {@link StringsData} records.
         */
        public static Iterator<StringsData> fromMap (Map<String, String> map) {
            final Iterator<Entry<String, String>> iter =
                    map.entrySet().iterator();
            return new Iterator<StringsData>() {

                @Override
                public boolean hasNext () {
                    return iter.hasNext();
                }

                @Override
                public StringsData next () {
                    final Entry<String, String> entry = iter.next();
                    return new StringsData() {

                        @Override
                        public String key () {
                            return entry.getKey();
                        }

                        @Override
                        public String value () {
                            return entry.getValue();
                        }
                    };
                }

                @Override
                public void remove () {
                    throw new NotEditableException();
                }
            };
        }
        private final Iterator<StringsData> iterator;

        /**
         * Creates an {@link IteratorSource}.
         * @param iterator an {@link Iterator} that produces {@link StringsData} records.
         * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
         * a given Strings key.
         * @param options a {@link Map} of {@link DefaultOption} setting types and the correspoding values.
         */
        public IteratorSource (Iterator<StringsData> iterator,
                               KeyResolver resolver,
                               OptionMap options) {
            super(options, resolver);
            this.iterator = iterator;
        }

        /**
         * Creates an {@link IteratorSource}.
         * @param iterable an {@link Iterable} collection of {@link StringsData} records.
         * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
         * a given Strings key.
         * @param options a {@link Map} of {@link DefaultOption} setting types and the correspoding values.
         */
        public IteratorSource (Iterable<StringsData> iterable,
                               KeyResolver resolver,
                               OptionMap options) {
            this(iterable.iterator(), resolver, options);
        }

        /**
         * Implements the {@link Iterable} contract.
         * The result is intended for read-only purposes, and is not required to implement
         * {@link Iterator#remove() }.
         * @return an {@link Iterator} which produces {@link StringsData} records.
         */
        @Override
        public Iterator<StringsData> iterator () {
            return iterator;
        }
    }

    /**
     * Interface to encapsulate key and value as a signle object.
     */
    public static interface StringsData {

        /**
         * Get the key a string.
         * @return the data that represents the key string.
         */
        String key ();

        /**
         * Get the value as string.
         * @return the data that represents the value string.
         */
        String value ();
    }
}
