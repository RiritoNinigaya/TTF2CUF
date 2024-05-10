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

import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import org.europabarbarorum.cuf.support.Setting.Modifiable;
import org.europabarbarorum.cuf.shell.Shell.Makeup;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;
import org.europabarbarorum.cuf.support.ResourceHelp.LogLevelResource;
import org.europabarbarorum.cuf.support.ResourceHelp.PositiveInteger;
import org.europabarbarorum.cuf.support.ResourceHelp.StringResource;

/**
 * Provides auxiliary methods for low-level code. This class name refers to the fact that it
 * (originally) is used mainly to handle IO related exceptions.
 * @author Johan Ouwerkerk
 */
public class IOHelp {

    /**
     * {@link String} preference to control the file used for storing
     * the CUF log. By default it is set to “cuf.log”.
     */
    public static Setting<String> LoggerFile = new Setting<String>(
            "cuf.log.file",
            "cuf.log",
            new StringResource(),
            Modifiable.Conf);
    /**
     * Setting used for determining the length of context strings.
     * @see #contextString(java.lang.String, int, int) 
     */
    public static Setting<Integer> ContextLength = new Setting<Integer>(
            "cuf.macro.context", "9", new PositiveInteger(),
            Modifiable.Conf);

    /**
     * Grab a bit of context text leading up to a given position from a given string.
     * This method is useful for providing a bit of text which may help a user
     * locate a particular position in the input.
     * @param input the input text which supplies the context to return
     * @param at the position of the last character to grab
     * @param length the length of the given input string.
     * @return text leading up to (and including) the given position.
     * The length of the returned text is at most equal to the value of
     * {@link #ContextLength}.
     */
    public static String contextString (String input, int at, int length) {
        at++;
        int cl = at - ContextLength.get();
        return input.substring(cl < 0 ? 0 : cl, at);
    }

    /**
     * Convenience “view” onto the log, which partitions it in multiple sections (subsystems).
     */
    public static enum Log {

        /**
         * Trace: {@link Level#FINEST}.
         */
        Trace(Level.FINEST),
        /**
         * Default debug: {@link Level#FINER}.
         */
        Debug(Level.FINER),
        /**
         * Config: {@link Level#CONFIG}.
         */
        Config(Level.CONFIG),
        /**
         * Warning: {@link Level#WARNING}.
         */
        Warning(Level.WARNING);

        private Log (Level l) {
            this.lvl = l;
        }
        private Level lvl;

        /**
         * Log a message to a subsystem.
         * @param cls a {@link Class} denoting the namespace to log to.
         * @param message a {@link BundleKey} providing a localised log message.
         * @param args arguments to the format string of the given message.
         */
        public void log (Class cls, BundleKey message, Object... args) {
            log(mapClass(cls), message.format(args));
        }

        /**
         * Log a message to a subsystem.
         * @param cls a {@link Class} denoting the namespace to log to.
         * @param message the raw message to log.
         */
        public void log (Class cls, String message) {
            log(mapClass(cls), message);
        }

        /**
         * Log a message to a subsystem.
         * @param subsystem raw subsystem name.
         * @param message raw message to log.
         */
        public void log (String subsystem, String message) {
            getLogger(subsystem).log(lvl, message);
        }

        private String mapClass (Class cls) {
            return cls.getSimpleName();
        }

        /**
         * Log a message with meta information about where it was logged.
         * Normally classes should not need to use this method. Instead
         * {@link IOHelp#handleExceptions(java.lang.Class, java.lang.String, java.lang.Throwable, java.lang.String) the raw IOHelp method}
         * or
         * {@link IOHelp#handleExceptions(java.lang.Class, java.lang.String, java.lang.Throwable, org.europabarbarorum.cuf.support.ResourceHelp.BundleKey, java.lang.Object[]) its localised form}
         * should suffice.
         * @param cls the {@link Class} in which the message is logged.
         * @param method the name of the method in which the message is logged.
         * @param message the message to log.
         * @param cause the {@link Throwable} which caused this message to be logged.
         */
        public void logp (Class cls,
                          String method,
                          String message,
                          Throwable cause) {
            logp(mapClass(cls), cls.getCanonicalName(), method, message, cause);
        }

        /**
         * Log a message with meta information about where it was logged.
         * Normally classes should not need to use this method. Instead
         * {@link IOHelp#handleExceptions(java.lang.Class, java.lang.String, java.lang.Throwable, java.lang.String) the raw IOHelp method}
         * or
         * {@link IOHelp#handleExceptions(java.lang.Class, java.lang.String, java.lang.Throwable, org.europabarbarorum.cuf.support.ResourceHelp.BundleKey, java.lang.Object[]) its localised form}
         * should suffice.
         * @param subsystem the subsystem in which the error occurred.
         * @param cls the name of the {@link Class} in which the message is logged.
         * @param method the name of the method in which the message is logged.
         * @param message the message to log.
         * @param cause the {@link Throwable} which caused this message to be logged.
         * @see #logp(java.lang.Class, java.lang.String, java.lang.String, java.lang.Throwable) 
         */
        public void logp (String subsystem,
                          String cls,
                          String method,
                          String message,
                          Throwable cause) {
            getLogger(subsystem).logp(lvl, cls, method, message, cause);
        }

        private Logger getLogger (String name) {
            if (names.contains(name)) {
                return Logger.getLogger(name);
            }
            else {
                return init(name);
            }
        }

        private static FileHandler init () {
            try {
                FileHandler h = new FileHandler(LoggerFile.get(), true);
                h.setLevel(Level.ALL);
                return h;
            }
            catch (Exception e) {
                System.err.println(e);
                return null;
            }
        }

        private static void flushAll (Handler[] handlers) {
            for (Handler h : handlers) {
                h.flush();
            }
        }

        /**
         * Flush all handlers of all loggers created through {@link Log}.
         */
        public static void flushAll () {
            for (String name : names) {
                flushAll(Logger.getLogger(name).getHandlers());
            }
            writer().flush();
        }

        private Logger init (String name) {

            Logger l = Logger.getLogger(name);
            try {
                for (Handler h : l.getHandlers()) {
                    l.removeHandler(h);
                }
                Level level = Level.ALL;
                Handler h = new MemoryHandler(writer(),
                                              LogBufferSize.get(),
                                              getPushLevelPrefs(name).get());
                h.setLevel(level);
                l.setLevel(level);
                l.setUseParentHandlers(false);
                l.addHandler(h);
            }
            catch (Exception e) {
                if (name.equals(IOHelp.class.getCanonicalName())) {
                    System.err.println(e);
                }
                else {
                    IOHelp.handleExceptions(Log.class, "init", e, e.
                            getLocalizedMessage());
                }
            }
            return l;
        }
        private static HashSet<String> names = new HashSet<String>();

        /**
         * Push {@link Level} preference per named subsystem.
         * @param name the name of the subsystem
         * @return a {@link Prefs} object to get {@code "cuf.log.pushlevel." + name}.
         */
        private static Setting<Level> getPushLevelPrefs (String name) {
            return new Setting<Level>("cuf.log.level." + name,
                                      "FINE",
                                      new LogLevelResource(),
                                      Modifiable.Conf);
        }
        /**
         * {@link Integer} preference which controls number of log records kept in 
         * memory by each logger.
         */
        public static Setting<Integer> LogBufferSize = new Setting<Integer>(
                "cuf.log.buffer",
                "200",
                new PositiveInteger(),
                Modifiable.Conf);

        private static FileHandler writer () {
            return writer;
        }
        private static FileHandler writer = init();
    }

    private static boolean inRange (Character c, Character l, Character u) {
        return c.compareTo(l) >= 0 && c.compareTo(u) <= 0;
    }

    private static boolean inRange (char[] c, int l, int u) {
        Integer i = Character.codePointAt(c, 0);
        return i.compareTo(l) >= 0 && i.compareTo(u) <= 0;
    }

    /**
     * Check if the given character is a <em>NameStartChar</em> according to the XML spec.
     * The definition for a <em>NameStartChar</em> is:
     * <blockquote><pre>
     * ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] |
     * [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] |
     * [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] |
     * [#x10000-#xEFFFF]
     * </pre></blockquote>
     * @param chr the character to check
     * @return true if the given character is a <em>NameStartChar</em>, false if not
     * @see <a href="http://www.w3.org/TR/REC-xml/#NT-Name">Extensible Markup Language (XML) 1.0 (Fifth Edition)</a>
     */
    public static boolean isXMLNameStartChar (Character chr) {
        char[] c = new char[] { chr };
        return chr == ':'
                || inRange(chr, 'A', 'Z')
                || chr == '_'
                || inRange(c, 'a', 'z')
                || inRange(c, 0xc0, 0xd6)
                || inRange(c, 0xd8, 0xf6)
                || inRange(c, 0xf8, 0x2ff)
                || inRange(c, 0x370, 0x37d)
                || inRange(c, 0x37f, 0x1fff)
                || inRange(c, 0x200c, 0x200d)
                || inRange(c, 0x2070, 0x218f)
                || inRange(c, 0x2c00, 0x2fef)
                || inRange(c, 0x3001, 0xd7ff)
                || inRange(c, 0xf900, 0xfdcf)
                || inRange(c, 0xfdf0, 0xfffd)
                || inRange(c, 0x10000, 0xeffff);
    }

    /**
     * Check if the given character is a <em>NameChar</em> according to the XML spec.
     * The definition for a <em>NameChar</em> is:
     * <blockquote><pre>
     * NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] |
     * [#x203F-#x2040]
     * </pre></blockquote>
     * @param chr the character to check
     * @return true if the given character is a <em>NameChar</em>, false if not
     * @see #isXMLNameStartChar(java.lang.Character)
     * @see <a href="http://www.w3.org/TR/REC-xml/#NT-Name">Extensible Markup Language (XML) 1.0 (Fifth Edition)</a>
     */
    public static boolean isXMLNameChar (Character chr) {
        return chr == '-'
                || chr == '.'
                || chr == ((char) 0xB7)
                || inRange(chr, '0', '9')
                || isXMLNameStartChar(chr)
                || inRange(new char[] { chr }, 0x300, 0x36f);
    }

    /**
     * Check if the given string is a <em>Name</em> according to the XML spec.
     * The definition for a <em>Name</em> is:
     * <blockquote><pre>
     * NameStartChar (NameChar)*
     * </pre></blockquote>
     * @param name the string to check
     * @return true if the given string is a <em>Name</em>, false if not
     * @see #isXMLNameStartChar(java.lang.Character)
     * @see #isXMLNameChar(java.lang.Character)
     * @see <a href="http://www.w3.org/TR/REC-xml/#NT-Name">Extensible Markup Language (XML) 1.0 (Fifth Edition)</a>
     */
    public static boolean isXMLName (String name) {
        if (name == null || name.equals("")) {
            return false;
        }
        if (isXMLNameStartChar(name.charAt(0))) {
            for (int i = 1, l = name.length(); i < l; ++i) {
                if (!isXMLNameChar(name.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Check if the given name is a <strong>valid</strong> name according to the XML spec.
     * A <strong>valid</strong> name must be a <em>Name</em> as defined in the XML spec, 
     * and must not start with the string “xml” (case insensitive).
     * @param name the string to check.
     * @return true if the given name is a <strong>valid</strong> name, false if not.
     * @see #isXMLName(java.lang.String) 
     * @see <a href="http://www.w3.org/TR/REC-xml/#NT-Name">Extensible Markup Language (XML) 1.0 (Fifth Edition)</a>
     */
    public static boolean isValidXMLName (String name) {
        if (isXMLName(name)) {
            return name.length() > 2
                    ? !name.substring(0, 3).equalsIgnoreCase("xml")
                    : true;
        }
        else {
            return false;
        }
    }

    /**
     * Generic method to send a formatted message string to a logger as warning.
     * This method is intended to be used for low-level code to send debug messages to outputstreams,
     * when recovering from errors.
     * @param _class_ reference to the {@link Class} in which the error occurs, for debugging purposes.
     * @param method reference to the method in which the error occurs, for debugging purposes.
     * @param cause {@link Throwable}, typically an {@link Exception} which caused this error to be logged
     * @param message a {@link BundleKey} which supplys a formatted warning message.
     * @param params any arguments to the format string.
     */
    public static void handleExceptions (
            Class _class_,
            String method,
            Throwable cause,
            BundleKey message,
            Object... params) {

        handleExceptions(_class_, method, cause, message.format(params));
    }

    /**
     * Generic method to send a message to a logger as warning.
     * This method is intended to be used for low-level code to send debug messages to outputstreams,
     * when recovering from errors.
     * @param _class_ reference to the {@link Class} in which the error occurs, for debugging purposes.
     * @param method reference to the method in which the error occurs, for debugging purposes.
     * @param cause {@link Throwable}, typically an {@link Exception} which caused this error to be logged
     * @param message a warning to log
     */
    public static void handleExceptions (Class _class_,
                                         String method,
                                         Throwable cause,
                                         String message) {
        Log.Warning.logp(_class_,
                         method,
                         message,
                         cause);
        warn(_class_, false, message);
    }

    /**
     * Emit a warning.
     * @param cls the {@link Class} which denoted the subsystem that logs the message.
     * @param message a {@link BundleKey} that contains a format string for the warnig to emit.
     * @param params arguments to the format string as applicable.
     */
    public static void warn (Class cls, BundleKey message, Object... params) {
        warn(cls, true, message.format(params));
    }

    /**
     * Emit a warning.
     * @param cls the {@link Class} which denoted the subsystem that logs the message.
     * @param message warning message to emit.
     */
    public static void warn (Class cls, String message) {
        warn(cls, true, message);
    }

    private static void warn (Class cls, boolean log, String message) {
        if (log) {
            Log.Warning.log(cls, message);
        }
        Shell s = Shell.getCurrentShell();
        if (s != null) {
            s.error(Makeup.Warning.makeup(s, message));
        }
        else {
            System.err.println(message);
        }
    }

    /**
     * Handle conversion bytes in a buffer to Java ints
     * @param bytes (buffer in which to find the thing)
     * @param start (start index of first byte to include in result)
     * @param num (number of bytes to use in result; limited to  { num ∈ ℕ ∣ 0 ≤ num ≤ 4 }, wrong values silently converted )
     * @param flipBytes whether or not Endianness needs to be taken account. Specify true for Little Endian data, false
     * for Big Endian data.
     * @return result of combining the bytes to an integer
     * @see #endianConversion(byte[], int, int) 
     */
    public static int byteConversion (byte[] bytes, int start, int num,
                                      boolean flipBytes) {
        int result = 0;
        num = num < 0 ? 4 : num;
        num = bytes.length - start >= num ? num : bytes.length - start;
        num = num > 4 ? 4 : num;
        for (int i = start, j = 0, shift = 0; j < num; ++i, ++j, shift += 8) {
            if (flipBytes) {
                result = ((bytes[i] & 0xFF) << shift) | result;
            }
            else {
                result = ((result << 8) | (bytes[i]) & 0xFF);
            }
        }

        return result;
    }

    /**
     * Handle endian conversion from little to big (human) endianness
     * @param bytes (buffer in which to find the thing)
     * @param start (start index of first byte to include in result)
     * @param num (number of bytes to use in result; limited to  { num ∈ ℕ ∣ 0 ≤ num ≤ 4 }, wrong values silently converted )
     * @return result of combining the bytes to an integer taking care of endianness in CUF Files
     */
    public static int endianConversion (byte[] bytes, int start, int num) {
        return byteConversion(bytes, start, num, true);
    }

    /**
     * Returns the position in a CUF chartable this character would take.
     * This value corresponds to the index of the character's Unicode code point 
     * in the array of all Unicode codepoints x such that 0 &lt; x &lt; 65535 (0xFFFF). 
     * @param in character to convert
     * @return array index to a CUF chartable. This is not the same as index to a byte buffer/array 
     * that corresponds to the chartable instide a CUF file.
     * @see #fromCode(int) 
     * @see String#codePointAt(int) 
     */
    public static int codeOf (Character in) {
        return Character.toString(in).codePointAt(0) & 0xFFFF;
    }

    /**
     * Convert all characters in the given string according to
     * {@link #rtfCode(java.lang.Character) }.
     * @param data the the text to convert
     * @return text data which can be injected into a RTF document.
     */
    public static String rtfString (String data) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0, l = data.length(); k < l; ++k) {
            sb.append(rtfCode(data.charAt(k)));
        }
        return sb.toString();
    }

    /**
     * Convert a given character to a <abbr title="Rich Text Format">RTF</abbr>
     * Unicode escape sequence.
     * @param c the character to convert. If it's code point is less than 128 it is returned as string.
     * @return something which when injected into a RTF document causes a
     * decent RTF viewer to display the wanted Unicode character. No fallback (ANSI) character
     * will be supplied, so the result should only be used in <code>&#92;ud{}</code> blocks.
     */
    public static String rtfCode (Character c) {
        int k = codeOf(c);
        if (k < 128) {
            return c.toString();
        }
        return String.format("\\u%d", sign(k, 2));
    }

    /**
     * Converts an index to a CUF chartable (codepoint) to a character.
     * This value corresponds to the character associated with the glyph bound to this position in
     * the chartable. This method takes only the 16 lower bits of the given integer into account; in other
     * words only positions x such that 0 &lt; x &lt; 65535 (0xFFFF) are meaningful input to this method.
     * In particular this method returns the NULL character for 0x****FFFF as input.
     * @param in index of this character into a CUF chartable.
     * @return character corresponding to the given array index.
     * @see #codeOf(java.lang.Character)
     * @see Character#toChars(int)
     */
    public static Character fromCode (int in) {
        in &= 0xFFFF;

        if (in == 0xFFFF) {
            in = 0;
        }

        char[] chrs = Character.toChars(in);
        return chrs[0];
    }

    /**
     * Interprets an unsigned value into an signed value. This method enables other components to
     * convert between unsigned integers that match signed values of different size and signed integers.
     * For example a signed short read as unsigned int can be interpreted as a signed int in this way.
     * @param unsigned an unsigned form which must be interpreted as signed int.
     * Values must correspond to a valid value with the given length. E.g. if length is given as 1, values
     * must be less than 256 and greater than -1.
     * @param length the number of bytes the unsigned value is supposed to occupy/represent.
     * Values must be between 1 and 4 inclusive.
     * @return the interpreted value or null if an argument was invalid.
     */
    public static Integer sign (int unsigned, int length) {
        Long cap = cap(length);
        if (cap != null) {
            if (length == 4 || (unsigned >= 0 && unsigned < cap)) {
                long c = cap >> 1;
                return unsigned < c ? unsigned : (int) (unsigned - cap);
            }
        }
        return null;
    }

    private static Long cap (int length) {
        long b = 0x1, i = 8, l = length;
        if (l > 4 || l < 1) {
            return null;
        }
        return b << (l * i);
    }

    /**
     * Converts a signed value into an unsigned value. This method enables other components to
     * convert between signed integers and unsigned integers that match signed values of different size.
     * For example a signed int can be converted into an unsigned version that corresponds to a signed short.
     * @param signed a signed value which must be converted to unsigned form so
     * it fits within its alloted number of bytes.
     * Values must correspond to a valid value with the given length. E.g. if length is given as 1, values
     * must be greater than -128 and less than 128.
     * @param length the number of bytes the (unsigned) value is supposed to
     * occupy/represent. Values must be between 1 and 4 inclusive.
     * @return the converted value or null if an argument was invalid.
     */
    public static Integer unsign (int signed, int length) {
        Long cap = cap(length);
        if (cap != null) {
            long c = cap >> 1;
            int sig = Integer.signum(signed), amp = signed * sig;
            if (amp < c) {
                return sig == -1 ? (int) (cap - amp) : amp;
            }
        }
        return null;
    }

    /**
     * Insert a value (of up to 4 bytes) into a byte buffer, using little endian encoding.
     * @param val value to insert
     * @param off offset to insert the value at
     * @param len length of the value in bytes 1 &lt;= len &lt;= 4
     * @param buf byte buffer to inser the value in
     * @param swapBytes whether or not to swap bytes (swaps endianness)
     * @return the modified byte array.
     */
    public static byte[] fillBuffer (int val, int off, int len, byte[] buf,
                                     boolean swapBytes) {
        int rval = swapBytes ? Integer.reverseBytes(val) : val;
        for (int i = 0; i < len; ++i, ++off) {
            buf[off] = (byte) ((rval >> (24 - (i << 3))) & 0xFF);
        }
        return buf;
    }
}
