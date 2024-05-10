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
package org.europabarbarorum.cuf.font;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.europabarbarorum.cuf.font.impl.WrappedSource.CharSource;
import org.europabarbarorum.cuf.font.impl.FormatConstants;
import org.europabarbarorum.cuf.font.impl.WrappedSource;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 * This class remaps the characters in an underlying {@link CUFSource},
 * disconnecting glyphs from the characters they visually correspond with.
 * This is useful for example in combining multiple {@link CUFSource} object into one: by
 * remapping one chartable you can prevent collisions from occurring when mixing the fonts ensuring that
 * all glyphs are preserved in the resulting font.
 * @param <C> type of object bound to characters in the chartable of the mapped source font.
 * @see WrappedSource
 * @author Johan Ouwerkerk
 */
public class MappedSource<C> extends CharSource<C> {

    private final Mapping map;

    /**
     * Create a {@link Mapping} which uses a predefined {@link Map} as lookup table.
     * Characters in the source font that do not appear as
     * keys in the lookup table are silently dropped from the result.
     * @param map a {@link Map} that provides a lookup table to map characters from the font to their
     * new values.
     * @return a {@link Mapping} that remaps a {@link MappedSource} as specified.
     */
    public static Mapping map (final Map<Character, Character> map) {
        return new MappedSource.Mapping() {

            @Override
            public Character map (Character in) {
                return map.get(in);
            }
        };
    }

    /**
     * Create a {@link Mapping} which shifts entries in a chartable.
     * The {@link Mapping result} discards characters which would fall outside of the range of characters
     * supported by the CUF file format as a result of shifting them by the given amount.
     * @param amount the amount to shift positions in the chartable by.
     * @return a {@link Mapping} that remaps a {@link MappedSource} as specified.
     */
    public static Mapping shift (final int amount) {
        return new Mapping() {

            @Override
            public Character map (Character in) {
                int code = amount + IOHelp.codeOf(in);
                return code < FormatConstants.__LIMIT__.value() && code > -1
                        ? IOHelp.fromCode(code)
                        : null;
            }
        };
    }

    /**
     * Create a {@link Mapping} which remaps a given array of characters in a {@link MappedSource} to its counterpart.
     * This method may throw an {@link IllegalArgumentException} if the size of the values array is
     * not large enough to cover for all the given keys.
     * <p>Each character from the chartable in the given font that is
     * also present in the array of given keys is mapped to a corresponding value obtained from the given
     * array of values. Characters that appear in the source font but not among the given keys are silently
     * dropped. Furthermore if a key appears more than once in the given keys this method will silently drop all but the
     * first of those key-value pairs which use that key.</p>
     * @param keys the array of keys to re-map.
     * @param values the array of values to use for the corresponding keys in the result font
     * @return a {@link Mapping} that remaps a {@link MappedSource} as specified.
     */
    public static Mapping arrays (Character[] keys, Character[] values) {
        return collections(Arrays.asList(keys), Arrays.asList(values));
    }

    /**
     * Create a {@link Mapping} which remaps a given collection characters in a {@link MappedSource} to its counterpart.
     * This method may throw an {@link IllegalArgumentException} if the size of the values {@link Collection} is
     * not large enough to cover for all the given keys.
     * <p>Each character from the chartable in the given font that is
     * also present in the {@link Collection} of given keys is mapped to a corresponding value obtained from the given
     * {@link Collection} of values. Characters that appear in the source font but not in the given keys are silently
     * dropped. Furthermore if a key appears more than once in the given keys this method will silently drop the
     * previous key-value pair.
     * <p>
     * Due to the fact that this method relies on {@link Iterator} objects for its implementation; the precise
     * order of in which keys and values are retrieved from their corresponding {@link Collection} objects is
     * ill-defined, if the given {@link Collection} does not specify such an order. This means that {@link Collection} objects
     * which rely on hashing and similar lookup methods are typically not suitable parameters to this method.
     * List-style or sorted {@link Collection} types should be fine.
     * </p>
     * @param keys the {@link Collection} of keys to re-map.
     * @param values the {@link Collection} of values to use for the corresponding keys in the result font
     * @return a {@link Mapping} that remaps a {@link MappedSource} as specified.
     */
    public static Mapping collections (final Collection<Character> keys,
                                       final Collection<Character> values) {
        int v = values.size(), k = keys.size();
        if (v < k || k <= 0) {
            throw new IllegalArgumentException(
                    Messages.TooFewEntriesError.format(k, v));
        }
        TreeMap<Character, Character> map = new TreeMap<Character, Character>();
        Iterator<Character> kIterator = keys.iterator();
        Iterator<Character> vIterator = values.iterator();
        while (kIterator.hasNext()) {
            map.put(kIterator.next(), vIterator.next());
        }
        return map(map);
    }

    /**
     * Create a {@link Mapping} which removes gaps in the chartable of a 
     * {@link CUFSource font}. The result of this {@link Mapping} is that
     * all characters form a single contiguous block in the chartable of a
     * {@link MappedSource}.
     * @return a {@link Mapping} to create a compacted {@link MappedSource}.
     */
    public static Mapping compact () {
        return new MappedSource.Mapping() {

            private int numChars = 0;

            @Override
            public Character map (Character in) {
                return IOHelp.fromCode(numChars++);
            }
        };
    }

    /**
     * Create a new mapped font.
     * @param src the underlying font to map.
     * @param map the {@link Mapping} to apply.
     */
    public MappedSource (CUFSource<C> src, Mapping map) {
        super(src);
        this.map = map;
    }

    @Override
    protected void deriveEntry (Character key, C value) {
        Character mapped = map.map(key);
        if (mapped != null) {
            addEntry(mapped, key);
        }
    }

    /**
     * Provides a callback so you can mask input characters as different (output) characters; for
     * the purpose of providing different/duplicate sets of glyphs for the input characters.
     */
    public interface Mapping {

        /**
         * Maps an input character to an output character. This is used to store, say,
         * duplicate ASCII characters as <abbr title="Common Chinese/Japanese/Korean">CJK</abbr> ones so you
         * can have multiple styles for the ASCII range in one font.
         * @param in the character to map.
         * @return null if this {@link Mapping} does not support the character, or the output character that
         * masks the input.
         */
        Character map (Character in);
    }
}
