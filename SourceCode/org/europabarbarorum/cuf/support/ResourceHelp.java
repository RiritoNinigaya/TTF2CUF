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

import java.io.File;
import java.lang.reflect.Array;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import org.europabarbarorum.cuf.support.PathParser.Glob;

/**
 * Class to help with loading resources named by actual Java symbols, rather
 * than arbitrary strings. This allows for automated verification that the required
 * resources are present in the apropriate resource bundles, and also that these
 * reserved bundles contain nothing but these resources.
 *
 * @author Johan Ouwerkerk
 */
public class ResourceHelp {
    
    private static boolean logResourceErrors = true;

    /**
     * Toggle logging of resource lookup through {@link #getValue(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey, java.lang.Class) }
     * that fails to retrieve a value.
     * @param set true to log errors, false to disable it.
     */
    protected static void logResourceErrors (boolean set) {
        logResourceErrors = set;
    }

    /**
     * Description of the data required to programmatically identify a 
     * resource key, and resource bundle at runtime.
     */
    public static interface BundleKey {

        /**
         * {@link BundleKey} objects should call
         * {@link ResourceHelp#getValue(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey, java.lang.Class) }
         * for the implementation of this method.
         * @return the resource value named by this {@link BundleKey}.
         */
        public String getText ();

        /**
         * {@link BundleKey} objects should call
         * {@link ResourceHelp#formatValue(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey, java.lang.Class, java.lang.Object[]) }
         * for the implementation of this method.
         * @param args the arguments to the format string named by this {@link BundleKey}.
         * @return this resource value formatted with the given arguments.
         * @see #getText() 
         */
        public String format (Object... args);

        /**
         * Name of the key. This provides the suffix part of the key used in querying
         * a {@link ResourceBundle} for a value corresponding to this {@link BundleKey}.
         * @return a unique identifier. The idea is to use {@link Enum} types
         * as {@link BundleKey} implementations.
         */
        public String name ();

        /**
         * Class that uses this {@link BundleKey} for some localized mnessage.
         * Its {@link Class#getCanonicalName() } method provides the prefix part of the key
         * used in querying the corresponding {@link ResourceBundle} by remvoing
         * the package declaration from it.
         * @return the class associated with the message/value in represented by this {@link BundleKey}.
         */
        public Class type ();
    }
    private static boolean auto_load = false;

    /**
     * Reset {@link ResourceHelp} state, this method is used by the bootstrap 
     * logic to ensure that {@link ResourceBundle} objects are loaded through the 
     * custom {@link Classloader} where possible.
     */
    protected static void clear () {
        if (!auto_load) {
            auto_load=true;
            ResourceBundle.clearCache();
            ResourceBundle.clearCache(Classloader.get());
        }
    }

    /**
     * Get the {@link ResourceBundle} denoted by the context {@link Class} argument.
     * @param cls the context class that provides the canonical name which
     * corresponds to a bundle.
     * @return the {@link ResourceBundle} obtained by using the value of
     * {@link Class#getCanonicalName() } from the context input as its basename.
     * @see ResourceBundle#getBundle(java.lang.String)
     */
    public static ResourceBundle getBundle (Class cls) {
        String n = cls.getCanonicalName();
        
        Locale loc=Locale.getDefault();
        return auto_load? ResourceBundle.getBundle(n,loc, Classloader.get()):
                 ResourceBundle.getBundle(n, loc);
    }

    /**
     * Get the key used for querying a {@link ResourceBundle} for a resource corresponding 
     * to the given {@link BundleKey}.
     * @param key the {@link BundleKey} to convert into a resource property key.
     * @return the (property) key used when querying resource bundles for 
     * a value corresponding to this key.
     * @see BundleKey#name() 
     * @see BundleKey#type()
     */
    public static String getKey (BundleKey key) {
        Class type = key.type();
        return type.getCanonicalName().substring(type.getPackage().
                getName().length() + 1)
                + "." + key.name();
    }

    /**
     * Return the value corresponding to a given {@link BundleKey} in the
     * context {@link Class} formatted with the given arguments. This method catches exceptions that may be thrown by
     * {@link String#format(java.lang.String, java.lang.Object[]) }, and returns null if any
     * is thrown. Additionally if such an exception is thrown it is also reported
     * through the
     * {@link IOHelp#handleExceptions(java.lang.Class, java.lang.String, java.lang.Throwable, java.lang.String) }
     * method. This has the benefit of reporting broken resources while at the same
     * time allowing the program to continue.
     * @param key the key to use for querying the {@link ResourceBundle}.
     * @param cls the context class to use for determining which {@link ResourceBundle} to use.
     * @param args the arguments to the format string
     * @return the value found and formatted with the given arguments, or null if an error occurs.
     */
    public static String formatValue (final BundleKey key, final Class cls,
                                      Object... args) {
        String t = key.getText();
        if (t == null) {
            return null;
        }
        try {
            return String.format(t, args);
        }
        catch (Exception e) {
            if (logResourceErrors) {
                String s;
                if (key == Messages.ResourceFormatError) {
                    s = String.format(
                            "Unable to format resource %1$s in %2$s. Details:%n%3$s",
                            key.name(),
                            cls.getCanonicalName(),
                            t);
                }
                else {
                    s = Messages.ResourceFormatError.format(key.name(),
                                                            cls.getCanonicalName(),
                                                            t);
                }
                IOHelp.handleExceptions(ResourceHelp.class, "formatValue", e, s);
            }
            return null;
        }
        
    }

    /**
     * Return the value corresponding to a given {@link BundleKey} in the
     * context {@link Class}. This method catches exceptions that may be thrown by
     * {@link ResourceBundle#getString(java.lang.String) }, and returns null if any
     * is thrown. Additionally if such an exception is thrown it is also reported
     * through the
     * {@link IOHelp#handleExceptions(java.lang.Class, java.lang.String, java.lang.Throwable, java.lang.String) }
     * method. This has the benefit of reporting missing resources while at the same
     * time allowing the program to continue.
     * @param key the key to use for querying the {@link ResourceBundle}.
     * @param cls the context class to use for determining which {@link ResourceBundle} to use.
     * @return the value found, or null if nothing was found.
     */
    public static String getValue (final BundleKey key, final Class cls) {
        String __key__ = getKey(key);
        String value;
        try {
            ResourceBundle rb = getBundle(cls);
            value = rb.getString(__key__);
            if (value == null) {
                throw new NullPointerException(__key__); // cause this to be logged
            }
            return value;
        }
        catch (Exception e) {
            if (logResourceErrors) {
                String s;
                if (key == Messages.NoSuchResource) {
                    s = String.format(
                            "Unable to find resource for %1$s in %2$s.%nKey: %3$s",
                            key.name(),
                            cls.getCanonicalName(),
                            __key__);
                }
                else {
                    s = Messages.NoSuchResource.format(key.name(),
                                                       cls.getCanonicalName(),
                                                       __key__);
                }
                IOHelp.handleExceptions(ResourceHelp.class, "getValue", e, s);
            }
            return null;
        }
    }

    /**
     * Interface to describe minimal parsing required by the CUF program.
     * @param <T> the type of object parsed by this instance.
     */
    public static interface ResourceType<T> {

        /**
         * Parse an input string to an instance of the object type.
         * @param s the string to parse
         * @return an instance object of {@link #resourceType() }
         * @throws Exception if an error occurs (in particular: if the input is not valid).
         */
        public T parse (String s) throws Exception;

        /**
         * A {@link Class} which corresponds to the type of objects parsed by this
         * {@link ResourceType}.
         * @return the {@link Class} of objects returned by {@link #parse(java.lang.String) }.
         */
        public Class resourceType ();
    }

    /**
     * Pass-through version of {@link ResourceType} which returns input strings without modification.
     */
    public static class StringResource extends Type<String> {
        
        @Override
        protected Object parseString (String s) throws Exception {
            return s;
        }
        
        @Override
        public Class resourceType () {
            return String.class;
        }
    }

    /**
     * A flavour of {@link ResourceType} to parse single characters.
     */
    public static class CharResource extends Type<Character> {
        
        @Override
        protected Object parseString (String s) throws Exception {
            return s.charAt(0);
        }
        
        @Override
        public Class resourceType () {
            return Character.class;
        }
    }

    /**
     * A flavour of {@link ResourceType} to parse {@link File} objects.
     * This {@link Glob} is initialised with the context of the current working directory.
     * @see #setContext(java.io.File)
     */
    public static class FileResource extends Glob implements
            ResourceType<File> {
        
        @Override
        public Class resourceType () {
            return File.class;
        }
        
        @Override
        public File parse (String s) throws Exception {
            return parsePath(s);
        }
    }

    /**
     * A flavour of {@link ResourceType} to parse arrays of characters.
     */
    public static class CharacterArrayResource implements
            ResourceType<Character[]> {
        
        private IllegalArgumentException syntax (int k,
                                                 Character at,
                                                 Character ex) {
            return new IllegalArgumentException(
                    Messages.CharacterArraySyntaxError.format(k, ex, at));
        }
        
        private IllegalArgumentException length (int l) {
            return new IllegalArgumentException(
                    Messages.CharacterArrayLengthError.format(l));
        }
        
        @Override
        public Character[] parse (String s) throws Exception {
            int l = s.length();
            if (l == 0) {
                throw length(l);
            }
            if (!s.startsWith("[")) {
                throw syntax(0, s.charAt(0), '[');
            }
            if (!s.endsWith("]")) {
                l = l - 1;
                throw syntax(l, s.charAt(l), ']');
            }
            s = s.substring(1, s.length() - 1);
            l = s.length();
            if ((l % 3) != 1) {
                throw length(l + 2);
            }
            
            int mod, j = 0, alloc = l / 3 + 1;
            Character[] array = new Character[alloc];
            String sep = ", ";
            Character at, ex = null;
            for (int k = 0; k < l; ++k) {
                mod = k % 3;
                at = s.charAt(k);
                switch (mod) {
                    case 0:
                        array[j] = at;
                        ++j;
                        break;
                    case 1:
                    case 2:
                        ex = sep.charAt(mod - 1);
                        if (at == ex) {
                            break;
                        }
                    default: // only reached if an exception should be trown
                        throw syntax(k, at, ex);
                }
            }
            
            return array;
        }
        
        @Override
        public Class resourceType () {
            return Array.newInstance(Character.class, 0).getClass();
        }
    }

    /**
     * A flavour of {@link ResourceType} to parse integers.
     */
    public static class IntegerResource extends Type<Integer> {
        
        @Override
        protected Object parseString (String s) throws Exception {
            return Integer.parseInt(s);
        }
        
        @Override
        public Class resourceType () {
            return Integer.class;
        }
    }

    /**
     * A flavour of {@link ResourceType} to parse booleans. The parsing logic is slightly different
     * from {@link Boolean#parseBoolean(java.lang.String) }, it accepts only the following values 
     * (case insensitive):
     * <dl>
     * <dt>True values:</dt>
     * <dd><code>yes</code>, <code>true</code>, or <code>1</code>.</dd>
     * <dt>False values:</dt>
     * <dd><code>no</code>, <code>false</code>, or <code>0</code>.</dd>
     * </dl>
     * Any other values causes an {@link IllegalArgumentException} to be thrown.
     */
    public static class BooleanResource extends Type<Boolean> {
        
        @Override
        protected Object parseString (String s) throws Exception {
            s = s.toUpperCase();
            if (s.equals("YES") || s.equals("TRUE") || s.equals("1")) {
                return true;
            }
            if (s.equals("NO") || s.equals("FALSE") || s.equals("0")) {
                return false;
            }
            throw new IllegalArgumentException();
        }
        
        @Override
        public Class resourceType () {
            return Boolean.class;
        }
    }

    /**
     * A flavour of {@link ResourceType} which parses {@link Level} constants for use in log settings.
     */
    public static class LogLevelResource extends Type<Level> {
        
        @Override
        public Object parseString (String s) throws Exception {
            return Level.parse(s);
        }
        
        @Override
        public Class resourceType () {
            return Level.class;
        }
    }

    /**
     * A version of {@link IntegerResource} that throws exceptions if the resulting
     * output is not positive (&gt;=1).
     */
    public static class PositiveInteger extends IntegerResource {
        
        @Override
        public Integer parse (String s) throws Exception {
            Integer k = super.parse(s);
            if (k < 1) {
                throw new IllegalArgumentException(s);
            }
            return k;
        }
    }

    /**
     * A flavour of {@link ResourceType} to parse {@link Enum} constants.
     */
    public static class EnumConstant extends Type<Enum> {
        
        private final Class<? extends java.lang.Enum> type;

        /**
         * Create the parser.
         * @param cls the actual {@link Enum} class to use for parsing constants.
         */
        public EnumConstant (Class<? extends java.lang.Enum> cls) {
            this.type = cls;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected Object parseString (String s) throws Exception {
            return java.lang.Enum.valueOf(type, s.trim());
        }

        /**
         * Return the class with which this {@link EnumConstant} was created.
         * @return the actual {@link Enum} type that is used for parsing strings into
         * object instances (enum fields).
         */
        @Override
        public Class resourceType () {
            return type;
        }
    }
    
    private abstract static class Type<T> implements ResourceType<T> {
        
        @Override
        @SuppressWarnings("unchecked")
        public T parse (String s) throws Exception {
            Object result = parseString(s);
            if (result == null) {
                throw new IllegalArgumentException();
            }
            return (T) result;
        }

        /**
         * Does the actual parsing for {@link #parse(java.lang.String) }, leaving the
         * main interface method free to apply validation logic.
         * @param s the string to parse
         * @return the object instance as parsed from the string. If the result is null
         * it will cause an {@link IllegalArgumentException} to be thrown later on.
         * @throws Exception if the string cannot be parsed.
         */
        protected abstract Object parseString (String s) throws Exception;
    }
}
