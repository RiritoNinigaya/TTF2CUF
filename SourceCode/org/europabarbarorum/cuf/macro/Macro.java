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
package org.europabarbarorum.cuf.macro;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.europabarbarorum.cuf.strings.impl.ControlCharacter;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.NotEditableException;

/**
 * Object to represent a mapping (relation) between input text read from some input source and actual
 * output text that is serialized to represent the input. Note that the {@link Iterator} returned as part of the
 * {@link Iterable} contract is read-only and in particular it
 * is not required to implement {@link Iterator#remove() }.
 * <p>Simply put Macros are the glue that bind the characters provided by a font to the
 * characters required by an input source.</p>
 * <p>This allows the strings.bin compiler to combine multipe instances of the same
 * characters to multiple output characters.
 * Since macros are intended to be provided by
 * a font source, this will allow one font to provide multiple renders (glyphs) of the
 * same characters; in other words: render two same characters in two different styles under the
 * pretence that these characters are actually two different ones.</p><p>This requires that some “character” space in the
 * a font is sacrificed to store duplicate entries. However even very large fonts with broad support for different scripts tend
 * to require less than a third or so of the total “character space” available.</p>
 * @author Johan Ouwerkerk
 */
public interface Macro extends
        Iterable<org.europabarbarorum.cuf.macro.Macro.Mapping> {

    /**
     * A minimal interface to describe a single character mapping inside a {@link Macro}.
     * This interface makes iterating over a custom written {@link Macro} easier, as the alternative of
     * for instance a {@link Entry} is a much more elaborate construct.
     */
    public static interface Mapping {

        /**
         * The original character.
         * @return the characer as it appears in source text.
         */
        Character source ();

        /**
         * The result character.
         * @return the corresponding character in ouput of applying the
         * {@link Macro} which contains this {@link Mapping} to source text.
         */
        Character mapped ();
    }

    /**
     * Exposes all {@link Macro} instances obtained from some input source (the provider).
     */
    static interface MacroProvider {

        /**
         * Get the number of {@link Macro} objects this {@link MacroProvider} supplies.
         * @return the number of {@link Macro} objects contained within this {@link MacroProvider}
         * @see #macros()
         */
        int number ();

        /**
         * Get the list of all {@link Macro} objects contained within this {@link MacroProvider}.
         * @return an unmodifiable {@link List} that contains all {@link Macro} objects provided.
         */
        List<Macro> macros ();
    }
    /**
     * Key used for the property that encodes all input characters recognized by a given
     * macro.
     */
    public static final String macro_keys_decl = "macro-%s-keys";
    /**
     * Key used for the property that encodes all macros in a given macro file.
     */
    public static final String macro_name_decl = "macro-names";
    /**
     * Key used for the property that encodes the strings that correspond to the
     * input characters recognized by this macro.
     */
    public static final String macro_vals_decl = "macro-%s-values";
    /**
     * Character encoding which is to be used for reading and writing macros to files.
     */
    public static final String macroEncoding = "UTF-8";

    /**
     * Map a single character to its corresponding output string.
     * @param in the character to map
     * @return the result character found for this character, or null if the
     * character is not recognized as valid input.
     */
    Character map (Character in);

    /**
     * Map all characters in the given text to their corresponding output string.
     * This method should behave as a repeated invocation of {@link #map(java.lang.Character) }.
     * Note: this method is free to throw exceptions when it encounters null input, or null output
     * as a result of invalid input.
     * @param in the string to map
     * @return the result (concatenated) string of mapping each input character in the input string to its
     * corresponding output string in order.
     * @throws Exception if an error occurs when mapping the input string to
     * a corresponding output string.
     */
    String map (String in) throws Exception;

    /**
     * Name of the Macro, used to uniquely identify its properties 
     * when save to a file in its serialized form.
     * @return some string that must be suitable for use as XML element name
     */
    String name ();

    /**
     * Return the number of input characters that are mapped to output strings
     * by this macro.
     * @return the number of mappings provided by this {@link Macro}.
     */
    int size ();

    /**
     * Simple {@link HashMap} backed implementation of the {@link Macro} interface.
     */
    static class SimpleMacro implements Macro {

        /**
         * Create an {@link Macro} with the given name.
         * This constructor initializes the {@link Macro} with the characters defined in
         * {@link ControlCharacter}.
         * @param name string used to identify this {@link Macro}, returned by
         * a call to {@link #name() }. Note that it must meet the same restrictions.
         */
        public SimpleMacro (String name) {
            this.__name__ = name;
            for (ControlCharacter c : ControlCharacter.values()) {
                if (c.fixed) {
                    mapping.put(c.charValue, c.charValue);
                }
            }
        }

        @Override
        public int size () {
            return mapping.size();
        }

        /**
         * Implementation of the {@link Iterable} contract in {@link Macro}.
         * @return an {@link Iterator} for use in walking the {@link Macro} during
         * for-each loops.
         */
        @Override
        @SuppressWarnings("unchecked")
        public Iterator<Mapping> iterator () {
            return new Iterator<Mapping>() {

                private Iterator<Entry<Character, Character>> iterator =
                        mapping.entrySet().iterator();

                @Override
                public boolean hasNext () {
                    return iterator.hasNext();
                }

                @Override
                public Mapping next () {
                    return new Mapping() {

                        private Entry<Character, Character> entry = iterator.
                                next();

                        @Override
                        public Character source () {
                            return entry.getKey();
                        }

                        @Override
                        public Character mapped () {
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

        @Override
        public String name () {
            return __name__;
        }
        /**
         * Name returned by a call to {@link #name() }.
         */
        protected final String __name__;
        /**
         * {@link HashMap} backing this {@link SimpleMacro} object.
         */
        protected HashMap<Character, Character> mapping =
                new HashMap<Character, Character>();

        /**
         * Maps an input string to its corresponding output string according to the 
         * mappings defined in this {@link SimpleMacro}.
         * <p>This implementation of {@link Macro#map(java.lang.String) } 
         * injects NULL characters for invalid input characters.</p>
         * @param in input string to transform.
         * @return the result of applying this {@link SimpleMacro} to the input string.
         */
        @Override
        public String map (String in) throws Exception {

            int l = in.length();

            StringBuilder sb = new StringBuilder();
            Character temp;
            for (int i = 0; i < l; ++i) {
                temp = map(in.charAt(i));
                if (temp == null) {
                    throw new Exception(badCharMessage(name(), i, in, l));
                }
                sb.append(temp);
            }
            return sb.toString();
        }

        @Override
        public Character map (Character in) {
            return mapping.get(in);
        }

        private static String badCharMessage (String n, int i, String s, int l) {
            String ctx = IOHelp.contextString(s, i, l);
            Character c = s.charAt(i);
            ControlCharacter ctl = ControlCharacter.forMacroCharacter(c);
            return Messages.IllegalCharacterError.format(ctl == null ? c : ctl,
                                                         n,
                                                         i,
                                                         ctx);
        }
    }
}
