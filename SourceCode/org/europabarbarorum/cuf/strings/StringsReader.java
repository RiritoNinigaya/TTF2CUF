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
package org.europabarbarorum.cuf.strings;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.europabarbarorum.cuf.support.Setting.Modifiable;
import org.europabarbarorum.cuf.strings.StringsWriter.EmptyStringOption;
import org.europabarbarorum.cuf.strings.impl.SimpleStringsFeeder.StringsData;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.MimeTag;
import org.europabarbarorum.cuf.support.NotEditableException;
import org.europabarbarorum.cuf.support.Preview;
import org.europabarbarorum.cuf.support.ResourceHelp.EnumConstant;
import org.europabarbarorum.cuf.support.Setting;

/**
 * Class to provide a read-only Map like view onto a Strings file.
 * The reader supports lookup of individual keys, and iterating over all key-value pairs in the file
 * though an {@link Iterator}. It does not support altering of data (either in the object representation or
 * in its serialised form).
 * <p>
 * This class assumes that the total amount of data for keys in the Strings file
 * is negligible compared to the total amount of data for values in the same file.
 * As optimisation for that model, keys are explicitly stored in memory whereas values are not.
 * Therefore memory consumption scales with the total amount of data for keys rather than the total amount of
 * data for a given file.
 * </p>
 * @author Johan Ouwerkerk
 */
public class StringsReader extends Preview.AbstractPreview implements
        Iterable<StringsData> {

    /**
     * A kind of {@link List} which provides a read-only list view onto a conceptual
     * “stream” of formatted Strings. This list does not store entries, except for a list
     * you may provide yourself at a constructor call.
     */
    public static class KeyList extends AbstractSet<String> {

        private final Set<String> keys;

        /**
         * Create a new default {@link KeyList}
         * This method is equivalent to calling the other constructor with an
         * empty list.
         */
        @SuppressWarnings("unchecked")
        public KeyList () {
            this(Collections.EMPTY_SET);
        }

        /**
         * Create a new {@link KeyList} based on a given set of keys.
         * @param keys a {@link Set} of keys to use as keys for the first N entries, where
         * N is the size of the given set. This set may be empty, but not null.
         */
        public KeyList (Set<String> keys) {
            if (keys == null) {
                throw new IllegalArgumentException(
                        Messages.StringsKeyListRequired.getText());
            }
            this.keys = keys;
        }

        /**
         * Create a new {@link KeyList} based on a given list of keys.
         * @param list a {@link List} of keys to use as keys for the first N entries, where
         * N is the size of the given list. This list may be empty, but not null.
         * Duplicate keys are filtered out.
         */
        public KeyList (final List<String> list) {
            if (list == null) {
                throw new IllegalArgumentException(
                        Messages.StringsKeyListRequired.getText());
            }

            // use the order in which the keys occur in the given list
            // instead of natural (lexical) ordering.
            // due to the contract of indexOf() the contract of Set is not broken
            Set<String> strings = new TreeSet<String>(new Comparator<String>() {

                @Override
                public int compare (String o1, String o2) {
                    return list.indexOf(o1) - list.indexOf(o2);
                }
            });
            strings.addAll(list);
            this.keys = strings;
        }

        /**
         * Get the size of this list.
         * @return {@link Integer#MAX_VALUE}
         */
        @Override
        public int size () {
            return Integer.MAX_VALUE;
        }

        /**
         * Get an {@link Iterator} to generate strings in the set.
         * @return an {@link Iterator} that walks the values in the set.
         */
        @Override
        public Iterator<String> iterator () {
            return new Generator();
        }

        /**
         * Implements the stream of automatically generated keys.
         */
        private class Generator implements Iterator<String> {

            private Iterator<String> iter = keys.iterator();
            private int id = 1;
            private String cache = null;

            /**
             * Get the next key in the automatically generated stream.
             * @return the next key if available or null if exhausted.
             */
            private String getNext () {
                if (id == Integer.MAX_VALUE) {
                    return null;
                }
                String can = Messages.StringsKeyAtIndex.format(id);
                if (keys.contains(can)) {
                    ++id;
                    return getNext();
                }
                return can;
            }

            /**
             * Check if a next element is present.
             * @return true if a next element is present, false if not.
             */
            @Override
            public boolean hasNext () {
                if (cache == null) {
                    cache = iter.hasNext() ? iter.next() : getNext();
                    ++id;
                    return cache != null;
                }
                return true;
            }

            /**
             * Return the next element if present.
             * @return the next element.
             * @throws NoSuchElementException if no more elements are present.
             */
            @Override
            public String next () throws NoSuchElementException {
                String r = cache;
                if (r == null) {
                    if (hasNext()) {
                        r = cache;
                    }
                    else {
                        throw new NoSuchElementException();
                    }
                }
                cache = null;
                return r;
            }

            /**
             * Not supported, throws an exception if called.
             */
            @Override
            public void remove () {
                throw new NotEditableException();
            }
        }
    }

    /**
     * Provides a way to iterate over all encoded key-value pairs in a Strings file. Note
     * that the returned iterator operates on a copy of cached data, so any modifications to the iterator
     * do not result in modifications to the source data.
     * @return an {@link Iterator} that can retrieve all key-value pairs from a Strings file.
     * The exact order of the retrieval is consistent with the order in which the records occur
     * in the file.
     */
    @Override
    public Iterator<StringsData> iterator () {
        TreeSet<StringsData> record = new TreeSet<StringsData>();
        record.addAll(offsetMap.values());
        return record.iterator();
    }

    @Override
    public String getPreview () {
        return get(getKey());
    }

    @Override
    public String defaultKey () {
        Iterator<String> keys = offsetMap.keySet().iterator();
        return keys.hasNext() ? keys.next() : null;
    }

    /**
     * Toggle a state flag that marks whether or not {@link #isPrepared()} should
     * return true.
     * @param prepared the new value to return at {@link #isPrepared()}.
     */
    protected void setPrepared (boolean prepared) {
        this.prepared = prepared;
    }

    /**
     * Adds a key to the map of cached data.
     * @param key the string to use as key value for looking up the record
     * @param l the offset at which the record occurs in the file.
     */
    protected void putEntry (String key, long l) {
        offsetMap.put(key, new StringsRecord(key, l));
    }
    private RandomAccessFile reader;
    private int numEntries;
    private TreeMap<String, StringsRecord> offsetMap;
    private boolean prepared;

    /**
     * Create a new {@link StringsReader} for a keyed strings file.
     * @param source the source file that supplies this {@link Preview} with its data.
     */
    public StringsReader (File source) {
        prepared = init(source, MimeTag.KeyedStringsFile, null);
        file = source;
    }

    /**
     * Create a new {@link StringsReader} for an ordered strings file.
     * @param source the file to read
     * @param keys a {@link List} of keys that are used to look up the values in the file.
     * @see MimeTag#OrderedStringsFile
     */
    public StringsReader (File source, Set<String> keys) {
        if (keys == null) {
            throw new IllegalArgumentException(
                    Messages.StringsKeyListRequired.getText());
        }
        prepared = init(source, MimeTag.OrderedStringsFile, keys);
        file = source;
    }

    /**
     * Creates a dummy, empty {@link StringsReader}. Useful for testing purposes.
     */
    protected StringsReader () {
        offsetMap = new TreeMap<String, StringsRecord>();
        prepared = false;
        file = null;
    }

    /**
     * Initalisation logic
     * @param source source file
     * @param mime {@link MimeTag} of the file type the source file should conform to.
     * @param keys a list of keys. Ignored if the
     * @return true if the {@link StringsReader} was initialised successfully, false if not.
     */
    private boolean init (File source, MimeTag mime, Set<String> keys) {
        mime.check(source);
        try {
            reader = new RandomAccessFile(source, "r");
        }
        catch (Exception e) {
            throw new IllegalArgumentException(
                    Messages.StringsFileInvalid.format(source,
                                                       e.getLocalizedMessage()));
        }

        try {
            long cumulativeOffset = mime.tag().length;
            reader.seek(cumulativeOffset);
            numEntries = length(4);
            Iterator<String> list; // iterator of the key list if applicable

            if (keys != null) {
                if (keys.size() < numEntries) {
                    throw new IllegalArgumentException(
                            Messages.StringsKeyListTooShort.format(
                            source,
                            keys.size(),
                            numEntries));
                }
                list = keys.iterator();
            }
            else {
                list = null;
            }

            cumulativeOffset += 4;
            offsetMap = new TreeMap<String, StringsRecord>();
            String curKey;
            for (int n = 0, k = 1; k <= numEntries; ++k) {
                if (list == null) {
                    curKey = readString(cumulativeOffset);
                    cumulativeOffset += (2 + 2 * curKey.length());
                }
                else {
                    reader.seek(cumulativeOffset);
                    curKey = list.next();
                }
                n = 2 + 2 * length(2);
                if (acceptEntry(curKey, n, k)) {
                    putEntry(curKey, cumulativeOffset);
                }
                cumulativeOffset += n;
            }
            return true;
        }
        catch (IOException error) {
            IOHelp.handleExceptions(StringsReader.class,
                                    "init",
                                    error,
                                    Messages.StringsInitError,
                                    source,
                                    error.getLocalizedMessage());
            close();
            return false;
        }
    }

    /**
     * Wraps a Strings key and offset value together so that order of Strings key-value pairs can be
     * programmatically retrieved by comparing offsets. Note that records have no meaning outside of the context
     * of the {@link StringsReader} which created them; they are purely an implementation detail.
     */
    private class StringsRecord implements StringsData,
                                           Comparable<StringsRecord> {

        private final long l;
        private final String key;

        private StringsRecord (String key, Long l) {
            this.l = l;
            this.key = key;
        }

        @Override
        public String key () {
            return key;
        }

        @Override
        public String value () {
            return readString(l);
        }

        /**
         * Compares this {@link StringsRecord} with another by their respective offsets in a Strings file.
         * Note that you cannot use this implementation to compare {@link StringsRecord} objects from different files
         * in any meaningful way.
         * @param o the {@link StringsRecord} to compare this one with.
         * @return an integer describing the position of this record relative to the given one.
         * It is positive if this record occurs “later” in a file, negative if it occurs “earlier” and
         * 0 if both records occur at the same position in a file (are the same record).
         */
        @Override
        public int compareTo (StringsRecord o) {
            return (int) (l - o.l);
        }
    }
    /**
     * {@link EmptyStringOption} preference which determines whether or not to warn about empty
     * strings read from Strings files.
     */
    public static final Setting<EmptyStringOption> EmptyStrings =
            new Setting<EmptyStringOption>(
            "cuf.strings.empty",
            "Warning",
            new EnumConstant(EmptyStringOption.class),
            Modifiable.Conf);

    private boolean acceptEntry (String key, int valLength, int record) {
        int keyLength = key.length();
        EmptyStringOption opt = EmptyStrings.get();
        switch (opt) {
            case Warning:
                if (keyLength < 1) {
                    IOHelp.warn(StringsReader.class, Messages.EmptyKeyRead,
                                record);
                }
                if (valLength < 3) {
                    IOHelp.warn(StringsReader.class, Messages.EmptyValueRead,
                                record, key);
                }
            case Disable:
                return keyLength > 0 && valLength > 2;
            case ValueOnly:
                return keyLength > 0;
            case Enable:
            default:
                return true;
        }
    }

    /**
     * Check if the object can be used to get meaningful results for queries.
     * @return true if the {@link StringsReader} initialised successfully, false if not.
     */
    public final boolean isPrepared () {
        return prepared;
    }

    /**
     * Get all keys encoded by the strings file.
     * @return the keys that this {@link StringsReader} can retrieve a value for in the file.
     */
    public Set<String> keySet () {
        return offsetMap == null ? null : offsetMap.keySet();
    }

    /**
     * Get the number of Strings records that the file declares. This number is supposed
     * to correspond to the number of string keys you can look up using this {@link StringsReader},
     * or to put it differently: the number of values entries encoded in the Strings file.
     * @return the number of records that the file is supposed to contain.
     */
    public int size () {
        return numEntries;
    }

    /**
     * Check if the key occurs in the Strings file.
     * @param key the key to check.
     * @return true if the {@link StringsReader} found the given key in the file, false if not.
     */
    public boolean containsKey (String key) {
        return offsetMap == null ? false : offsetMap.containsKey(key);
    }

    /**
     * Get the value from the Strings file that belongs with the given key.
     * @param key the key to look up.
     * @return the value corresponding to the given key, or null if the key was not found by the {@link StringsReader} or if an error 
     * occurred.
     */
    public String get (String key) {
        if (offsetMap == null) {
            return null;
        }
        StringsRecord sr = offsetMap.get(key);
        return sr == null ? null : readString(sr.l);
    }

    /**
     * Read a length field in the strings file.
     * @param len the length of the field to read.
     * @return the first few bytes at the current position in the Strings file interpreted as little-endian 
     * integer.
     * @throws IOException if an error occurs.
     */
    protected int length (int len) throws IOException {
        byte[] buf = new byte[len];
        reader.read(buf);

        return IOHelp.endianConversion(buf, 0, len);
    }

    /**
     * Read a length prefixed string from the given offset in the Strings file.
     * @param offset the offset to read from.
     * @return the String found, or null if an error occurs.
     */
    protected String readString (final long offset) {
        try {
            reader.seek(offset);
            byte[] buf = new byte[length(2) << 1];
            reader.read(buf);
            return fromBuffer(buf);
        }
        catch (Exception e) {
            IOHelp.handleExceptions(StringsReader.class,
                                    "readString",
                                    e,
                                    Messages.StringsReadError,
                                    offset,
                                    e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Interpret a byte buffer as string.
     * @param buf the data to interpret.
     * @return the encoded string.
     */
    protected String fromBuffer (byte[] buf) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i += 2) {
            sb.append(IOHelp.fromCode(IOHelp.endianConversion(buf, i, 2)));
        }
        return sb.toString();
    }
    private final File file;

    /**
     * Get the file path of the Strings file read by this {@link StringsReader}.
     * @return the {@link File} which is read by this {@link StringsReader}.
     */
    public File getFile () {
        return file;
    }

    /**
     * Closes the {@link StringsReader} and disposes of underlying resources.
     * The object will no longer be usable after this method has been called.
     */
    public void close () {
        numEntries = 0;
        offsetMap = null;
        try {
            reader.close();
        }
        catch (IOException ignored) {
            IOHelp.handleExceptions(StringsReader.class,
                                    "close",
                                    ignored,
                                    ignored.getLocalizedMessage());
        }
        reader = null;
        prepared = false;
    }
}
