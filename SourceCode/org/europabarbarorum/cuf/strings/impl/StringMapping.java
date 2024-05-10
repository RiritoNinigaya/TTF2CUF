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
import java.util.Map;
import org.europabarbarorum.cuf.macro.Macro;
import org.europabarbarorum.cuf.macro.MacroFile;

/**
 * Interface to define callback methods that the Strings compiler uses to transform
 * input strings into output that is compiled.
 */
public interface StringMapping {

    /**
     * Append character data to the data already supplied.
     * This method may remap the given input text.
     * @param text the string to append.
     * @param ctx a name which identifies the body of text currently being mapped.
     * Should be used in an error message when an {@link Exception} is thrown to
     * help users locate the source of the error.
     * @throws Exception if the given input string cannot be mapped to its corresponding
     * output.
     */
    public void appendString (String text, String ctx) throws Exception;

    /**
     * Select a new macro to use for the next {@link #appendString(java.lang.String, java.lang.String) append} operation.
     * @param macro the name of the macro to select.
     * @param ctx a name which identifies the body of text currently being mapped.
     * Should be used in an error message when an {@link Exception} is thrown to
     * help users locate the source of the error.
     * @throws Exception when the given macro is not found.
     */
    public void select (String macro, String ctx) throws Exception;

    /**
     * Get the result created by {@link #appendString(java.lang.String, java.lang.String) appending} chunks of
     * text. This method may remap the input text {@link #appendString(java.lang.String, java.lang.String) appended}
     * to the {@link StringMapping}.
     * The returned text should be removed from the {@link StringMapping}. Example:
     * <blockquote><pre>
     * StringMapping m = getStringMapping();
     * m.select(getMacro());
     * m.appendString("hello", "key");
     * String s1 = m.getMappedString("key"), // returns mapped version of "hello"
     *        s2 = m.getMappedString("key"); // returns ""
     * m.appendString("world", "key");
     * String s3 = m.getMappedString("key"); // returns mapped version of "world";
     * @param ctx a name which identifies the body of text currently being mapped.
     * Should be used in an error message when an {@link Exception} is thrown to 
     * help users locate the source of the error.
     * @return the mapped version of (all) the given input characters.
     * @throws Exception if the given input string cannot be mapped to its corresponding
     * output.
     */
    public String getMappedString (String ctx) throws Exception;

    /**
     * Allows the compiler to assert whether or not a mapped character was originally a given, known,
     * character. The idea is that control characters can be detected *after* re-mapping them.
     * @param original the character that is assumed to have been the original form
     * @param mapped the character found in a mapped string.
     * @return true if the original character was indeed the source of the mapped character found.
     */
    public boolean mappedEquals (Character original, Character mapped);

    /**
     * A {@link StringMapping} which depends on additional attributes declared
     * in the source document. The {@link java.util.Iterator} provide by this interface is
     * read-only: it does not (need to) support removing elements.
     */
    public interface ConfiguredMapping extends StringMapping,
                                               Iterable<ConfigurationKey> {
    }

    /**
     * A {@link StringMapping} which defines a layout for text it maps.
     */
    public interface LayoutMapping extends StringMapping {

        /**
         * Attempt to perform text-layout on a substring of the raw output.
         * This method is used to inject a sense of text-formatting/layout in the output result.
         * @param source the raw output substring.
         * @return a post-processed substring which may be altered to fit the constraints of a
         * layout better.
         */
        public String layout (String source);

        /**
         * Syncs the {@link LayoutMapping} with a body of pre-computed text.
         * @param precomputed a litteral string that the {@link LayoutMapping} must not attempt to
         * reinterpret.
         */
        public void advance (String precomputed);

        /**
         * Attempts to align text. This method is called when a
         * {@link ControlCharacter#Tab} is encountered.
         * @return a indent string or null if text cannot be aligned on the same line.
         */
        public String indent ();

        /**
         * Attempts to add a character to the output stream keeping in mind any layout
         * constraints imposed by this {@link LayoutMapping}. This method is called for all characters in the
         * output stream except the following:
         * <ul>
         * <li>{@link ControlCharacter#CarriageReturn}</li>
         * <li>{@link ControlCharacter#LineFeed}</li>
         * <li>{@link ControlCharacter#Null}</li>
         * <li>{@link ControlCharacter#Tab}</li>
         * </ul>
         * @param c the character which triggered the method call.
         * @return the index of the character in the current line
         * at which the output stream should be broken into multiple lines,
         * or {@link #NO_BREAK} if the current line of text can accomodate this additional character.
         * <p>
         * If it is not {@link #NO_BREAK} then this result must not be less than 0 or larger than
         * the number of times this method has been called.
         */
        public int layout (char c);
        /**
         * Pre-defined return value for {@link #layout(char) } which signals that
         * no break up of text is required.
         */
        public static final int NO_BREAK = -1;
    }

    /**
     * Representation of an XML attribute used to configure a {@link ConfiguredMapping} type.
     */
    public interface ConfigurationKey {

        /**
         * Name of the attribute.
         * @return the local name of the attribute as used in the source XML.
         */
        String name ();

        /**
         * Namespace of the attribute.
         * @return the namespace URI as used in the source XML.
         */
        String uri ();

        /**
         * Callback method to configure the associated {@link ConfiguredMapping}
         * with the value found for this {@link ConfigurationKey}.
         * @param value the attribute value found in the source XML for this
         * {@link ConfigurationKey}.
         * @throws Exception if the value is invalid.
         */
        void set (String value) throws Exception;

        /**
         * Whether or not the attribute denoted by this {@link ConfigurationKey}
         * is required to be present. If this method returns false, the underlying
         * {@link ConfiguredMapping} should be prepared to default for this
         * {@link ConfigurationKey} in case the attribute is not found in the source XML.
         * @return true if this attribute must occur in the XML source, false if it
         * may be omitted.
         */
        boolean required ();
    }

    /**
     * A simple {@link StringMapping} implementation that reads its macros from an input file.
     * @author Johan Ouwerkerk
     */
    public static class BasicMapping implements StringMapping {

        private final Map<String, Macro> macros;
        private Macro current;
        private StringBuilder text;

        /**
         * Create a {@link StringMapping} from a {@link File}.
         * @param f {@link File} to read with {@link MacroFile}.
         */
        public BasicMapping (File f) {
            this.macros = init(f);
            this.text = new StringBuilder();
        }

        /**
         * Extracts macros from a given file.
         * @param macrofile input file to {@link MacroFile}.
         * @return a {@link Map} of string keys (names) and {@link Macro} values.
         */
        protected Map<String, Macro> init (File macrofile) {
            MacroFile mf = new MacroFile(macrofile);
            HashMap<String, Macro> temp = new HashMap<String, Macro>(mf.number());
            for (Macro m : mf.macros()) {
                temp.put(m.name(), m);
            }
            return Collections.unmodifiableMap(temp);
        }

        @Override
        public void appendString (String text, String key) throws Exception {
            try {
                this.text.append(this.current.map(text));
            }
            catch (Exception e) {
                throw new MappingException(key, e);
            }
        }

        @Override
        public void select (String macro, String key) throws Exception {
            Macro can = macros.get(macro);
            if (can == null) {
                throw new MappingException(key,
                                           Messages.NoSuchMacro.format(
                        macro));
            }
            current = can;
        }

        @Override
        public String getMappedString (String key) {
            String s = text.toString();
            text = new StringBuilder();
            return s;
        }

        @Override
        public boolean mappedEquals (Character original, Character mapped) {
            Character found = null;
            for (Macro m : macros.values()) {
                found = m.map(original);
                if (found != null && found == mapped) {
                    return true;
                }
            }
            return false;
        }

        private static class MappingException extends Exception {

            private MappingException (String ctx, Throwable cause) {
                super(Messages.MappingError.format(ctx, cause.
                        getLocalizedMessage()), cause);
            }

            private MappingException (String ctx, String msg) {
                super(Messages.MappingError.format(ctx, msg));
            }
        }
    }
}
