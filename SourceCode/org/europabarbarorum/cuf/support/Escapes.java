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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * List of characters that can be escaped in text parsed by the CUF program.
 * This enumeration lists essentially those characters which have special meaning to the
 * Strings compiler and the {@link #escape escape sign} itself.
 * @author Johan Ouwerkerk
 */
public enum Escapes {

    /**
     * The linefeed or newline (LF) character (\x0A or &#92;u000A): \n.
     */
    Newline('n', (char) 0x0A),
    /**
     * The carriage return (CR) character (\x0D or &#92;u000D): \r.
     */
    Return('r', (char) 0x0D),
    /**
     * The (horizontal) tab character (\x09 or &#92;u0009): \t.
     */
    Tab('t', (char) 0x09),
    /**
     * The NULL character (\x00 or &#92;u0000): \0.
     */
    Null('0', (char) 0x00),
    /**
     * Character representing an escape symbol: the double escape symbol: \\.
     * @see #escape
     */
    Escape();
    /**
     * Character used for the escape notation to encode the represented 
     * {@link #character character}.
     */
    public final Character sequence;
    /**
     * Character represented by the {@link #sequence escape notation}.
     */
    public final Character character;

    private Escapes (char p, char r) {
        this.sequence = p;
        this.character= r;
    }

    private Escapes () {
        this.sequence = escape.charAt(0);
        this.character= this.sequence;
    }

    /**
     * Find an {@link Escapes escape} character by the character value it refers to.
     * @param c the literal character (e.g. a linefeed to retrieve Newline).
     * @return the {@link Escapes escape} character which corresponds to the given 
     * character.
     */
    public static Escapes forCharacter(Character c) {
        for(Escapes esc: Escapes.values()){
            if(esc.character.equals(c)) {
                return esc;
            }
        }
        return null;
    }
    
    /**
     * Find an {@link Escapes escape} character by the escaped character value (sequence).
     * @param c the character which is escaped (e.g. 'n' to retrieve Newline).
     * @return the {@link Escapes escape} character corresponding to the given 
     * character.
     */
    public static Escapes forSequence(Character c) {
        for(Escapes esc: Escapes.values()) {
            if(esc.sequence.equals(c)) {
                return esc;
            }
        }
        return null;
    }

    /**
     * Substitute any escape sequences found for their un-escaped counterparts.
     * Note that this method also strips some characters from the input before doing the substitution,
     * corresponding to the litteral versions of the following control characters:
     * <ul>
     * <li>{@link #Return}</li>
     * <li>{@link #Newline}</li>
     * <li>{@link #Tab}</li>
     * <li>{@link #Null}</li>
     * </ul>
     * This means that this method cannot (in general)
     * be used to recursively substitute escaped escape sequences.
     * @param text the source text to process
     * @return the modified result.
     */
    public static String substitute (String text) {
        return mapper.substitute(text.replace(Return.character.toString(), "").
                replace(Newline.character.toString(), "").
                replace(Tab.character.toString(), "").
                replace(Null.character.toString(), ""));
    }
    private static final EscapeMapper mapper = new EscapeMapper() {

        private final Map<Character, Character> substituteMap = map();

        @Override
        protected String mapEscape (Character code) {
            Character v = substituteMap.get(code);
            if (v == null) {
                return escape + code;
            }
            else {
                return v.toString();
            }
        }

        private Map<Character, Character> map () {
            Escapes[] list = Escapes.values();
            HashMap<Character, Character> map = new HashMap<Character, Character>(
                    list.length);
            for (Escapes esc : list) {
                map.put(esc.sequence, esc.character);
            }
            return Collections.unmodifiableMap(map);
        }
    };
    /**
     * The escape symbol, a backslash: \.
     */
    public static final String escape = "\\";

    /**
     * Class which encapsulates the escape substitution algorithm used in
     * {@link Escapes#substitute(java.lang.String) }. This enables reuse for
     * expanding text such as Shell prompts that may use escape characters of their own.
     */
    public static abstract class EscapeMapper {

        /**
         * This method is called to expand escape sequences to the text that should be substituted.
         * Note that this method is called for all characters that are prefixed with an
         * {@link #escape escape character}, so this method must take care to handle erroneous sequences also.
         * @param code the character which is used as escape sequence.
         * @return the text that should be substituted. 
         * Must not be null (but this check is not enforced).
         */
        protected abstract String mapEscape (Character code);

        /**
         * Substitute any escape sequences found for their un-escaped counterparts.
         * Escape sequences are defined as single characters prefixed with an {@link #escape escape character}.
         * This method can be used to recursively substitute escaped escape sequences.
         * @param text the source text to process
         * @return the modified result.
         */
        public String substitute (String text) {

            StringBuilder sb = new StringBuilder("");
            int k = text.indexOf(escape), l = text.length(), o = 0;
            for (; k < l && k != -1; o = k, k = text.indexOf(escape, k)) {
                sb.append(text.substring(o, k));
                ++k;
                if (k < l) {
                    sb.append(mapEscape(text.charAt(k)));
                    ++k;
                }
                else {
                    sb.append(escape);
                }
            }
            if (k == -1) {
                sb.append(text.substring(o));
            }
            return sb.toString();
        }
    }
}
