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

import java.util.HashSet;
import java.util.Set;
import org.europabarbarorum.cuf.support.Escapes;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * Enumeration of all control characters recognized/reserved by the compiler.
 * This enumeration is used in post-process/validation logic applied to raw output.
 * You can obtain a view onto the {@link Set} of characters that the compiler reserves via
 * the {@link #charset() charset method}.
 * @see RawHandler
 * @see RawHandler#toString()
 * @author Johan Ouwerkerk
 */
public enum ControlCharacter implements BundleKey {

    /**
     * The NULL character (\x00 or &#92;u0000).
     */
    Null(Escapes.Null),
    /**
     * The (horizontal) tab character (\x09 or &#92;u0009).
     */
    Tab(Escapes.Tab),
    /**
     * The non-breaking space character (\x20 or &#92;u0020).
     */
    NonBreakingSpace(IOHelp.fromCode(0x20), false),
    /**
     * The carriage return (CR) character (\x0D or &#92;u000D).
     */
    CarriageReturn(Escapes.Return),
    /**
     * The linefeed (LF) character (\x0A or &#92;u000A).
     */
    LineFeed(Escapes.Newline);
    /**
     * {@link Character} value of this {@link ControlCharacter}; useful for comparisons with characters
     * in strings.
     */
    public final Character charValue;
    /**
     * Whether or not this character is a fixed value. If the value is fixed it means that
     * output strings from macros are not supposed to re-map {@link #charValue} to a different
     * character. If not, such re-mapping is allowed.
     */
    public final boolean fixed;

    private ControlCharacter (Character c, boolean enableMapping) {
        this.charValue = c;
        this.fixed = enableMapping;
    }

    private ControlCharacter (Escapes e) {
        this(e.character, true);
    }

    /**
     * Attempts to match a given character against this {@link ControlCharacter}.
     * @param c the character to match
     * @param map the {@link StringMapping} which provides context for matching characters.
     * @return true if this {@link ControlCharacter} represents the given character false if not.
     */
    public boolean matches (Character c, StringMapping map) {
        return fixed ? c.equals(charValue) : map.mappedEquals(charValue, c);
    }

    /**
     * Finds out if a given character is represented by any {@link ControlCharacter}.
     * @param c the character to compare against control characters.
     * @param map the {@link StringMapping} which provides context for matching characters.
     * @return a {@link ControlCharacter} that represents the given character, or null if no such
     * {@link ControlCharacter} is defined.
     */
    public static ControlCharacter forMappingCharacter (Character c,
                                                        StringMapping map) {
        for (ControlCharacter cc : ControlCharacter.values()) {
            if (cc.matches(c, map)) {
                return cc;
            }
        }
        return null;
    }

    /**
     * Finds out if a given character is represented by any {@link ControlCharacter}.
     * @param c the character to compare against control characters.
     * @return a {@link ControlCharacter} that represents the given character, or null if no such
     * {@link ControlCharacter} is defined.
     */
    public static ControlCharacter forMacroCharacter (Character c) {
        for (ControlCharacter cc : ControlCharacter.values()) {
            if (cc.charValue == c) {
                return cc;
            }
        }
        return null;
    }

    /**
     * Represent all values of this enumeration as a {@link Set} of characters.
     * @return a {@link Set} of characters containing a character for each {@link ControlCharacter}
     * object in the enumeration.
     */
    public static Set<Character> charset () {
        ControlCharacter[] chars = ControlCharacter.values();
        Set<Character> charset = new HashSet<Character>(chars.length);
        for (ControlCharacter chr : chars) {
            charset.add(chr.charValue);
        }
        return charset;
    }

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, ControlCharacter.class);
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, ControlCharacter.class, args);
    }

    @Override
    public Class type () {
        return ControlCharacter.class;
    }
}
