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

import java.util.Collection;
import org.europabarbarorum.cuf.font.MappedSource.Mapping;
import org.europabarbarorum.cuf.font.impl.WrappedSource;

/**
 * This class provides a way to restrict a {@link CUFSource} to a subset of its entire character range.
 * This can be useful to trim the fat in a given font or for later re-mapping of the character range.
 * @param <C> type of object bound to characters in the chartable of the restricted source font.
 * @see WrappedSource
 * @author Johan Ouwerkerk
 */
public class RestrictedSource<C> extends MappedSource<C> {

    /**
     * This interface describes a method to declare a restriction on the chartable of a font.
     * The idea is that a restricted font is supplied with a {@link Restriction} which is used by the
     * font to restrict its character set to the subset marked as supported by the supplied {@link Restriction}.
     * <p>
     * A {@link Restriction} is equivalent to a {@link Mapping} which returns its input if
     * it is to be accepted, or null if the input is to be discarded.
     */
    public static interface Restriction {

        /**
         * Check if a character is allowed.
         * @param c character to check
         * @return true if the character is supported, false if not
         */
        boolean isSupported (Character c);
    }

    /**
     * Simple {@link Restriction} to accept any character.
     * @return a {@link Restriction} that accepts any character.
     */
    public static Restriction acceptAll () {
        return new Restriction() {

            @Override
            public boolean isSupported (Character c) {
                return true;
            }
        };
    }

    /**
     * Simple {@link Restriction} to discard all characters.
     * @return a {@link Restriction} that discards all characters.
     */
    public static Restriction discardAll () {
        return new Restriction() {

            @Override
            public boolean isSupported (Character c) {
                return false;
            }
        };
    }

    /**
     * Create a {@link Restriction} to limit a {@link RestrictedSource font} to include/exclude
     * a given range of characters. The range is defined as
     * all characters between ‘start’ and ‘end’ inclusive; characters are compared to each other according to
     * {@link Character#compareTo(java.lang.Character) }.
     * @param start the first character (inclusive) of the subset that the font is to be restricted to
     * @param end the last character (inclusive) of the subset that the font is to be restricted to
     * @param includeRange controls whether or not the given range defines the characters to include.
     * @return a {@link Restriction} to limit a {@link RestrictedSource font} as specified.
     */
    public static Restriction range (final Character start, final Character end,
                                     final boolean includeRange) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException(
                    Messages.InvalidCharRange.format(start, end));
        }
        return new Restriction() {

            @Override
            public boolean isSupported (Character c) {
                return includeRange
                        == (c.compareTo(start) >= 0 && c.compareTo(end) <= 0);
            }
        };
    }

    /**
     * Create a {@link Restriction} to limit a {@link RestrictedSource font} to include/exclude
     * a given collection of characters.
     * @param set the {@link Collection} of characters to restrict the font to.
     * @param includeSet controls whether or not the range parameter defines the characters to include.
     * @return a {@link Restriction} to limit a {@link RestrictedSource font} as specified.
     */
    public static Restriction collection (final Collection<Character> set,
                                          final boolean includeSet) {
        return new Restriction() {

            @Override
            public boolean isSupported (Character c) {
                return set.contains(c) == includeSet;
            }
        };
    }

    /**
     * Create a {@link CUFSource} that provides only a subset of the characters used by 
     * its backing font.
     * @param toRestrict the {@link CUFSource} to restrict to a given subset of characters.
     * @param restriction {@link Restriction} to use when selecting characters from the wrapped {@link CUFSource}.
     */
    public RestrictedSource (CUFSource<C> toRestrict,
                             final Restriction restriction) {
        super(toRestrict, new Mapping() {

            @Override
            public Character map (Character in) {
                return restriction.isSupported(in) ? in : null;
            }
        });
    }
}
