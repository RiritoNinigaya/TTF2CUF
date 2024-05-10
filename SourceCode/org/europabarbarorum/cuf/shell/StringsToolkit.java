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
package org.europabarbarorum.cuf.shell;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.shell.Shell.Toolkit;
import org.europabarbarorum.cuf.strings.StringsReader;
import org.europabarbarorum.cuf.strings.StringsReader.KeyList;
import org.europabarbarorum.cuf.strings.StringsWriter.EmptyStringOption;
import org.europabarbarorum.cuf.strings.StringsWriter.EncodingOption;
import org.europabarbarorum.cuf.strings.StringsWriter.FormatOption;
import org.europabarbarorum.cuf.strings.StringsWriter.StringsFeeder;
import org.europabarbarorum.cuf.strings.impl.EscapeMapping;
import org.europabarbarorum.cuf.strings.impl.IdentityMapping;
import org.europabarbarorum.cuf.strings.impl.KeyResolver;
import org.europabarbarorum.cuf.strings.impl.KeyResolver.KeyResolverImpl;
import org.europabarbarorum.cuf.strings.impl.KeyResolver.SimpleKeyResolver;
import org.europabarbarorum.cuf.strings.impl.PlainSource;
import org.europabarbarorum.cuf.strings.impl.RawHandler.LineBreakOption;
import org.europabarbarorum.cuf.strings.impl.RawHandler.NullCharacterOption;
import org.europabarbarorum.cuf.strings.impl.RawHandler.SpaceSequenceOption;
import org.europabarbarorum.cuf.strings.impl.RawHandler.TabOption;
import org.europabarbarorum.cuf.strings.impl.SAXHandler.IgnorableWhitespaceOption;
import org.europabarbarorum.cuf.strings.impl.SimpleStringsFeeder;
import org.europabarbarorum.cuf.strings.impl.SimpleStringsFeeder.EditorSource;
import org.europabarbarorum.cuf.strings.impl.SimpleStringsFeeder.IteratorSource;
import org.europabarbarorum.cuf.strings.impl.SimpleStringsFeeder.MapSource;
import org.europabarbarorum.cuf.strings.impl.SimpleStringsFeeder.StringsData;
import org.europabarbarorum.cuf.strings.impl.StringMapping;
import org.europabarbarorum.cuf.strings.impl.StringMapping.BasicMapping;
import org.europabarbarorum.cuf.strings.impl.URIResolver;
import org.europabarbarorum.cuf.strings.impl.URIResolver.InvalidURIException;
import org.europabarbarorum.cuf.strings.impl.URIResolver.MapConfiguration;
import org.europabarbarorum.cuf.strings.impl.URIResolver.MixedURIResolver;
import org.europabarbarorum.cuf.strings.impl.URIResolver.PreConfiguredURIResolver;
import org.europabarbarorum.cuf.strings.impl.URIResolver.ResolverConfiguration;
import org.europabarbarorum.cuf.strings.impl.URIResolver.URIResolverImpl;
import org.europabarbarorum.cuf.strings.impl.WidthMapping;
import org.europabarbarorum.cuf.strings.impl.XMLSource;
import org.europabarbarorum.cuf.strings.impl.XMLSource.XSLTOption;
import org.europabarbarorum.cuf.support.MimeTag;
import org.europabarbarorum.cuf.support.OptionMap;
import org.europabarbarorum.cuf.support.PathParser;

/**
 * This class provides a simpe way for scripts to compile Strings file without having
 * to do the low level configurations and imports.
 * @author Johan Ouwerkerk
 */
public class StringsToolkit extends Toolkit {

    /**
     * Create a new {@link StringsToolkit}.
     * @param context the {@link Shell} which provides context for resolving
     * file names.
     */
    public StringsToolkit (Shell context) {
        this(context.pathParser());
    }

    /**
     * Create a new {@link StringsToolkit}.
     * @param context the {@link PathParser} which provides context for resolving
     * file names.
     */
    public StringsToolkit (PathParser context) {
        super(context);
        options = new OptionMap();
    }
    private OptionMap options;

    /**
     * Configure how tabs should be handled.
     * @param mode the name of a {@link TabOption} value.
     * Use one of “Discard”, “Ignore” or “Keep”.
     * @see TabOption
     */
    public void tabMode (String mode) {
        options.put(TabOption.class, TabOption.valueOf(mode));
    }

    /**
     * Configure how ignorable whitespace in input XML should be treated.
     * @param mode the name of a {@link IgnorableWhitespaceOption} value.
     * Use one of “Ignore”, “Warning” or “Compile”.
     * @see IgnorableWhitespaceOption
     */
    public void whitespaceMode (String mode) {
        options.put(IgnorableWhitespaceOption.class, IgnorableWhitespaceOption.
                valueOf(mode));
    }

    /**
     * Configure how linebreaks should be handled.
     * @param mode the name of a {@link LineBreakOption} value.
     * Use one of “Keep”, “Discard”, “Ignore”, “Convert” or “Normalize”.
     * @see LineBreakOption
     */
    public void linebreakMode (String mode) {
        options.put(LineBreakOption.class, LineBreakOption.valueOf(mode));
    }

    /**
     * Configure how empty strings (key or value) should be treated.
     * @param mode the name of a {@link EmptyStringOption} value.
     * Use one of “Disable”, “Enable”, “ValueOnly”, or “Warning”.
     */
    public void emptyStringMode (String mode) {
        options.put(EmptyStringOption.class, EmptyStringOption.valueOf(mode));
    }

    /**
     * Configure how NULL characters (aka \x0 or &#92;u0000) should be treated.
     * @param mode the name of a {@link NullCharacterOption} value.
     * Use one of “Enable”, “Disable” or “Discard”.
     * @see NullCharacterOption
     */
    public void nullCharMode (String mode) {
        options.put(NullCharacterOption.class, NullCharacterOption.valueOf(mode));
    }

    /**
     * Configure how a sequence of multiple non-breaking space characters (aka \x20 or &#92;u0020)
     * should be treated.
     * @param mode the name of a {@link SpaceSequenceOption} value.
     * Use one of “Coalesce”, “Warning” or “Compile”.
     * @see SpaceSequenceOption
     */
    public void spaceMode (String mode) {
        options.put(SpaceSequenceOption.class, SpaceSequenceOption.valueOf(mode));
    }

    /**
     * Sets up a {@link CompileUnit} for compilation of an XML file to a Strings file.
     * This method uses the compiler options you have set on the {@link StringsToolkit},
     * and specifies simple file based namespace resolution: XML namespaces in the source XML
     * should correspond to Macro files.
     * @param source the input XML file to compile.
     * @param result the file to save the Strings result to.
     * @param ordered whether or not the strings are ordered by definition of the source data.
     * Typically you should specify “false” here, unless you know that you need this compatibility.
     * @return a {@link CompileUnit} to compile the XML file.
     * @see #compile(java.lang.String, java.lang.String, org.europabarbarorum.cuf.strings.impl.URIResolver, boolean)
     * @see #compile(org.europabarbarorum.cuf.strings.StringsWriter.StringsFeeder, java.lang.String) 
     * @see CompileUnit#run()
     */
    public CompileUnit compile (String source, String result, boolean ordered) {
        return compile(xmlSource(source, null), result, ordered);
    }

    /**
     * Sets up a {@link CompileUnit} for compilation of a formatted text file to a Strings file.
     * This method uses the compiler options you have set on the {@link StringsToolkit}.
     * @param source the input text file to compile.
     * @param result the file to save the Strings result to.
     * @param resolver the {@link KeyResolver} which handles looks up the necessary macro imports for
     * a Strings key.
     * @param ordered whether or not the strings are ordered by definition of the source data.
     * Typically you should specify “false” here, unless you know that you need this compatibility.
     * @return a {@link CompileUnit} to compile the text file.
     * @see #compile(org.europabarbarorum.cuf.strings.StringsWriter.StringsFeeder, java.lang.String)
     * @see CompileUnit#run()
     */
    public CompileUnit compile (String source, String result,
                                KeyResolver resolver, boolean ordered) {
        return compile(new PlainSource(file(source), resolver, options),
                       result, ordered);
    }

    /**
     * Associates an XSLT stylesheet with the input file. This option is used for working
     * with custom XML formats. The compiler will run the XML source through the
     * given XSLT stylesheet and compile the result, rather than
     * compile directly from a source XML file.
     * @param path a valid file path which refers to an XSLT stylesheet that transforms input
     * XML to the format the Strings compiler understands.
     * @see #useXSLT(java.lang.String, java.lang.String)
     * @see XSLTOption
     */
    public void useXSLT (String path) {
        useXSLT(path, EncodingOption.defaultEncoding);
    }

    /**
     * Specify which character encoding is assumed when reading (XML) data.
     * @param encoding the character encoding to use, or null to use the default.
     * @see EncodingOption
     */
    public void encoding (String encoding) {
        options.put(EncodingOption.class, new EncodingOption(encoding));
    }

    /**
     * Associates an XSLT stylesheet with the input file. This option is used for working
     * with custom XML formats. The compiler will run the XML source through the
     * given XSLT stylesheet and compile the result, rather than
     * compile directly from a source XML file.
     * @param path a valid file path which refers to an XSLT stylesheet that transforms input
     * XML to the format the Strings compiler understands.
     * @param encoding the character encoding to use for reading the stylesheet.
     * @see #useXSLT(java.lang.String) 
     * @see XSLTOption
     * @see EncodingOption
     */
    public void useXSLT (String path, String encoding) {
        File f = file(path);
        this.options.put(XSLTOption.class, new XSLTOption(f, encoding));
    }

    /**
     * Sets up a {@link CompileUnit} for compilation of an XML file to a Strings file.
     * This method uses the compiler options you have set on the {@link StringsToolkit}.
     * @param source the input XML file to compile.
     * @param result the file to save the Strings result to.
     * @param namespaceResolver the {@link URIResolver} which handles macro imports in source XML through
     * XML namespaces. If this argument is null, this method call is equivalent to
     * {@link #compile(java.lang.String, java.lang.String, boolean) }.
     * @param ordered whether or not the strings are ordered by definition of the source data.
     * Typically you should specify “false” here, unless you know that you need this compatibility.
     * @return a {@link CompileUnit} to compile the XML file.
     * @see #compile(org.europabarbarorum.cuf.strings.StringsWriter.StringsFeeder, java.lang.String)
     * @see CompileUnit#run()
     */
    public CompileUnit compile (String source,
                                String result,
                                URIResolver namespaceResolver,
                                boolean ordered) {
        return compile(xmlSource(source, namespaceResolver),
                       result,
                       ordered);
    }

    /**
     * Compiles a regular expression into a {@link Pattern}.
     * @param regex the regular expression to compile.
     * @return the corresponding {@link Pattern}.
     */
    public Pattern fromString (String regex) {
        return Pattern.compile(regex);
    }

    /**
     * List of possible ways to match a Strings key against a literal substring.
     */
    public static enum MatchType {

        /**
         * Check if a Strings key starts with a given substring.
         */
        Prefix,
        /**
         * Check if a Strings key ends with a given substring.
         */
        Suffix,
        /**
         * Check if a Strings key contains a given substring.
         */
        Infix;

        private boolean returnValue (String pattern, String key) {
            switch (this) {
                case Prefix:
                    return key.startsWith(pattern);
                case Suffix:
                    return key.endsWith(pattern);
                case Infix:
                    return key.contains(pattern);
            }
            return false;
        }
    }

    /**
     * Shorthand for creating a {@link Matcher} with proper {@link MatchType}.
     * @param pattern the (sub-)string pattern to search for.
     * @param type name of a {@link MatchType}. Use one of “Suffix”, “Infix”, or “Prefix”.
     * @return a new {@link Matcher}.
     */
    public Matcher matcher (String pattern, String type) {
        return new Matcher(pattern, MatchType.valueOf(type));
    }

    /**
     * Class to combine a search pattern (string) with a more specific direction/location
     * where the pattern is supposed to occur (e.g. at the end of a searched string).
     */
    public final static class Matcher {

        private final String pattern;
        private final MatchType type;

        /**
         * Create a {@link Matcher}.
         * @param pattern sub string to search for.
         * @param type {@link MatchType} type of match to look for.
         */
        public Matcher (String pattern, MatchType type) {
            this.type = type;
            this.pattern = pattern;
        }
    }

    /**
     * Creates a {@link KeyResolver} that attempts to match Strings keys to against literal substrings.
     * @param uriResolver the {@link URIResolver} to use for producing {@link StringMapping} objects from
     * URI strings.
     * @param macros a {@link Map} of string key value pairs. Keys correspond to URIs given as values in patterns,
     * values correspond to valid macro names in the {@link StringMapping} instances produced from these URIs by the
     * given {@link URIResolver}.
     * @param matches a {@link Map} of string key value pairs. Keys correspond to substrings, and values to
     * the URI to use in {@link URIResolver#resolve(java.lang.String) } when a Strings key matches the substring.
     * Note that the order in which these substrings are tried to check for a match is ill-defined, so you should
     * not rely on any particular order for generating the right URI.
     * @param defaultURI a default namespace to use (may be null). This is useful for
     * providing sensible default behaviour if the wrapped {@link URIResolver}
     * is set up to handle a default namespace as well.
     * @return a new {@link SimpleKeyResolver}.
     */
    public KeyResolver keyResolver (URIResolver uriResolver,
                                    Map<String, String> macros,
                                    final Map<Matcher, String> matches,
                                    final String defaultURI) {
        return new SimpleKeyResolver(uriResolver, macros) {

            @Override
            protected String getURI (String key) {
                for (Matcher pattern : matches.keySet()) {
                    if (pattern.type.returnValue(pattern.pattern, key)) {
                        return matches.get(pattern);
                    }
                }
                return defaultURI;
            }
        };
    }

    /**
     * Get a sensible default {@link MixedURIResolver} for use with
     * {@link #mixURIResolver(java.lang.String, org.europabarbarorum.cuf.strings.impl.URIResolver, org.europabarbarorum.cuf.strings.impl.URIResolver.MixedURIResolver) }
     * @param defaultURI the default namespace URI to use.
     * @return a {@link MixedURIResolver} which is set up in such a way that by default
     * input text is not re-mapped except for escape sequences (e.g.: \n or \r).
     */
    public MixedURIResolver sensibleDefaultMix (String defaultURI) {
        checkURI(defaultURI);
        return new MixedURIResolver(identityResolver(defaultURI));
    }

    /**
     * Creates a {@link KeyResolver} which uses a single {@link StringMapping} for all
     * Strings values.
     * @param uriResolver the {@link URIResolver} which resolves the {@link StringMapping} to use.
     * @param uri the URI string to resolve to a {@link StringMapping}.
     * @param macro the macro to use when performing macro transformations.
     * @return a new {@link KeyResolver}.
     * @throws Exception if the given URI is null, empty or cannot be resolved by the
     * given {@link URIResolver}.
     */
    public KeyResolver keyResolver (URIResolver uriResolver, String uri,
                                    String macro) throws Exception {
        checkURI(uri);
        return KeyResolverImpl.wrap(uriResolver.resolve(uri), macro);
    }

    /**
     * Get a {@link KeyResolver} set up in such a way that any input text is not 
     * re-mapped except for escape sequences (e.g.: \n or \r).
     * @param uri namespace URI to use.
     * @return a new {@link KeyResolver}.
     * @throws Exception if the given URI is null or empty
     * @see #identityResolver(java.lang.String) 
     * @see #keyResolver(org.europabarbarorum.cuf.strings.impl.URIResolver, java.lang.String, java.lang.String) 
     */
    public KeyResolver identityKeyResolver (String uri) throws Exception {
        return keyResolver(identityResolver(uri), uri, IdentityMapping.macroName);
    }

    private void checkURI (String uri) {
        if (uri == null || uri.length() == 0) {
            throw new InvalidURIException(uri);
        }
    }

    /**
     * Create a new {@link URIResolver} which returns a type of {@link StringMapping}
     * suitable for use with the portions of text which
     * must be preserved in its original form in a Strings file.
     * @param match the namespace to use for this {@link URIResolver}.
     * @return a {@link URIResolver} which works with the given namespace only, and
     * provides a {@link IdentityMapping} for it.
     */
    public URIResolver identityResolver (final String match) {
        checkURI(match);
        return new URIResolver() {

            private StringMapping map = new IdentityMapping(match);

            @Override
            public StringMapping resolve (String uri) throws Exception {
                if (uri.equals(match)) {
                    return map;
                }
                throw new InvalidURIException(uri);
            }
        };
    }

    /**
     * Creates or updates a {@link URIResolver} which bridges multiple resolvers together.
     * This is useful when a single input document requires different types of {@link URIResolver}
     * for correctly resolving namespaces and these namespaces are known in advance.
     * <p>
     * In other words the result {@link URIResolver} can be used with a {@link KeyResolver}
     * that generates such namespaces from Strings keys.
     * @param uri a namespace string.
     * @param resolve the {@link URIResolver} which is to be bound to the given namespace.
     * @param mix the {@link MixedURIResolver} to update, or null if a new one is to be created.
     * @return the newly created or updated {@link MixedURIResolver}.
     * @see #sensibleDefaultMix(java.lang.String)
     */
    public MixedURIResolver mixURIResolver (String uri,
                                            URIResolver resolve,
                                            MixedURIResolver mix) {
        checkURI(uri);
        if (mix == null) {
            mix = new MixedURIResolver();
        }
        mix.put(uri, resolve);
        return mix;
    }

    /**
     * Creates a {@link KeyResolver} using regular expression patterns to determine
     * URI values for Strings keys.
     * @param uriResolver the {@link URIResolver} to use for producing {@link StringMapping} objects from
     * URI strings.
     * @param macros a {@link Map} of string key value pairs. Keys correspond to URIs given as values in patterns,
     * values correspond to valid macro names in the {@link StringMapping} instances produced from these URIs by the
     * given {@link URIResolver}.
     * @param patterns a {@link Map} of {@link Pattern} keys and string value pairs. Keys correspond to regular expressions, and values to
     * the URI to use in {@link URIResolver#resolve(java.lang.String) } when a Strings key matches the regular expression.
     * Note that the order in which these expressions are evaluated to check for a match is ill-defined, so you should
     * not rely on any particular order for generating the right URI.
     * @return a new {@link SimpleKeyResolver}.
     * @see #fromString(java.lang.String)
     */
    public KeyResolver keyResolver (URIResolver uriResolver,
                                    final Map<String, String> macros,
                                    final Map<Pattern, String> patterns) {
        return new SimpleKeyResolver(uriResolver, macros) {

            @Override
            protected String getURI (String key) {
                for (Pattern pattern : patterns.keySet()) {
                    if (pattern.matcher(key).matches()) {
                        return patterns.get(pattern);
                    }
                }
                return null;
            }
        };
    }

    /**
     * Creates a {@link KeyResolver} that attempts to match Strings keys to against literal substrings.
     * @param map a {@link Map} of string key and {@link StringMapping} value pairs. Keys correspond to Strings keys,
     * values correspond to the {@link StringMapping} to use.
     * @param macros a {@link Map} of string key value pairs. Keys correspond to Strings keys,
     * values correspond to valid macro names in the {@link StringMapping} values found in map.
     * @return a new {@link SimpleKeyResolver}.
     */
    public KeyResolver keyResolver (final Map<String, StringMapping> map,
                                    final Map<String, String> macros) {
        return new KeyResolver() {

            @Override
            public StringMapping forKey (String key) throws Exception {
                return map.get(key);
            }

            @Override
            public String getMacro (String key) {
                return macros.get(key);
            }
        };
    }

    /**
     * Sets up a {@link CompileUnit} for compilation of a given {@link StringsFeeder} to a Strings file.
     * @param source a {@link StringsFeeder} which generates the strings to compile.
     * @param result the file to use for saving the Strings output produced by the compiler.
     * @return a {@link CompileUnit} to compile the file.
     * @see #format(java.lang.String)
     * @see #compile(java.lang.String, java.lang.String, boolean) 
     * @see CompileUnit#run()
     */
    public CompileUnit compile (StringsFeeder source, String result) {
        return new CompileUnit(CompileUnit.getJob(file(result),
                                                  CompileUnit.FileType.Strings,
                                                  options,
                                                  source));

    }

    /**
     * Sets up a {@link CompileUnit} for compilation of a given {@link StringsFeeder} to a Strings file.
     * @param source a {@link StringsFeeder} which generates the strings to compile.
     * @param result the file to use for saving the Strings output produced by the compiler.
     * @param ordered whether or not the strings are ordered by definition of the source data.
     * Typically you should specify “false” here, unless you know that you need this compatibility.
     * @return a {@link CompileUnit} to compile the file.
     * @see #compile(java.lang.String, java.lang.String, boolean) 
     * @see CompileUnit#run()
     */
    public CompileUnit compile (StringsFeeder source, String result,
                                boolean ordered) {
        format(ordered);
        return this.compile(source, result);
    }

    /**
     * Previews a Strings file using a given {@link CUFSource}.
     * This method may throw {@link IllegalArgumentException} if the given file can not be found, or if it
     * is not a keyed Strings file.
     * @param stringsFile the file to preview.
     * @param font the font to use for rendering the preview.
     * @return true if preview could be successfully initialised, false if not.
     * @see #preview(java.lang.String, org.europabarbarorum.cuf.font.CUFSource, java.util.List)
     * @see #preview(org.europabarbarorum.cuf.strings.StringsReader, org.europabarbarorum.cuf.font.CUFSource)
     */
    public boolean preview (final String stringsFile, final CUFSource font) {
        return preview(new StringsReader(file(stringsFile)), font, stringsFile);
    }

    /**
     * Previews a Strings file using a given {@link CUFSource}.
     * @param file the {@link StringsReader} used to extract preview text from the Strings file.
     * @param font the {@link CUFSource} used for generating preview renders from Strings text.
     * @return true if preview could be successfully initialised, false if not.
     * @see StringsReader#isPrepared() 
     * @see #preview(java.lang.String, org.europabarbarorum.cuf.font.CUFSource, java.util.List) 
     * @see #preview(java.lang.String, org.europabarbarorum.cuf.font.CUFSource) 
     */
    public boolean preview (StringsReader file, CUFSource font) {
        return preview(file, font, file.getFile().toString());
    }

    /**
     * Read a Strings file into a {@link StringsReader}.
     * @param stringsFile the path to the Strings file to read
     * @return a {@link StringsReader} which can be used to traverse the Strings file and
     * extract Strings records.
     */
    @SuppressWarnings("unchecked")
    public StringsReader fromFile (String stringsFile) {
        return fromFile(stringsFile, Collections.EMPTY_LIST);
    }

    /**
     * Read a Strings file into a {@link StringsReader}.
     * @param stringsFile the path to the Strings file to read
     * @param keys a {@link List} of strings keys to be used if the given Strings file
     * contains no keys.
     * @return a {@link StringsReader} which can be used to traverse the Strings file and
     * extract Strings records.
     */
    public StringsReader fromFile (String stringsFile, List<String> keys) {
        File file = file(stringsFile);
        MimeTag tag = MimeTag.getType(file);

        switch (tag) {
            case KeyedStringsFile:
                return new StringsReader(file);
            case OrderedStringsFile:
                return new StringsReader(file, new KeyList(keys));
            default:
                throw tag.exception(stringsFile);
        }
    }

    /**
     * Previews an ordered Strings file using a given {@link CUFSource} with a given key list.
     * This method may throw {@link IllegalArgumentException} if the given file can not be found, or if it
     * is not a Strings file; or if the keys list is invalid (too few entries).
     * @param stringsFile the file to preview.
     * @param font the font to use for rendering the preview.
     * @param keys a list of keys.
     * @return true if a preview could be constructed or false if no preview could be
     * generated.
     * @see #preview(java.lang.String, org.europabarbarorum.cuf.font.CUFSource) 
     */
    public boolean preview (String stringsFile,
                            CUFSource font,
                            List<String> keys) {

        return preview(new StringsReader(file(stringsFile), getKeyList(keys)),
                       font,
                       stringsFile);
    }

    private KeyList getKeyList (List<String> keys) {
        return keys == null ? new KeyList() : new KeyList(keys);
    }

    /**
     * Attempts to create a preview of the given Strings file with the given font.
     * @param reader a {@link StringsReader} object which provides the text to be previewed.
     * @param font a {@link CUFSource} which provides the font to render the preview.
     * @param fileName  a file path that corresponds to the file read by the given
     * {@link StringsReader}.
     * @return true if a preview could be constructed or false if no preview could be
     * generated. In particular this method returns false if either:
     * <ul>
     * <li>{@link java.awt.GraphicsEnvironment#isHeadless() } returns false</li>
     * <li>{@link CUFSource#isPrepared() } for the given font returns false</li>
     * <li>{@link StringsReader#isPrepared() } for the given reader returns false</li>
     * </ul>
     * @see FontToolkit#preview(org.europabarbarorum.cuf.font.CUFSource, org.europabarbarorum.cuf.support.Preview, java.lang.String)
     */
    public static boolean preview (final StringsReader reader,
                                   final CUFSource font, String fileName) {
        if (reader.isPrepared()) {
            return FontToolkit.preview(font,
                                       reader,
                                       Messages.StringsPreviewTitle.format(
                    fileName,
                    font.getCufSource()));
        }
        else {
            reader.close();
            return false;
        }
    }

    /**
     * Creates a {@link URIResolver} that generates {@link WidthMapping} instances.
     * @param source the a source/context file to resolve relative path name URIs to macro files.
     * @param conf the {@link ResolverConfiguration} that provides the necessary settings for the generated
     * {@link WidthMapping} instaces.
     * @return a new {@link PreConfiguredURIResolver}.
     */
    public URIResolver widthResolver (String source,
                                      ResolverConfiguration conf) {
        return new PreConfiguredURIResolver(xmlWidthResolver(source), conf);
    }

    /**
     * Creates a {@link URIResolver} that generates {@link WidthMapping} instances.
     * This {@link URIResolver} should only be used in conjunction with either
     * {@link #compile(java.lang.String, java.lang.String, boolean) } or
     * {@link #compile(java.lang.String, java.lang.String, org.europabarbarorum.cuf.strings.impl.URIResolver, boolean) }.
     * @param source the a source/context path to resolve relative path name URIs to macro files.
     * @return a new {@link URIResolverImpl}.
     */
    public URIResolver xmlWidthResolver (String source) {
        return new URIResolverImpl(file(source)) {

            private String current;

            @Override
            public StringMapping resolve (String uri) throws Exception {
                current = uri;
                return super.resolve(uri);
            }

            @Override
            public StringMapping createMapping (File f) {
                return new WidthMapping(f, this, current);
            }
        };
    }

    /**
     * Creates and initialises a {@link MapConfiguration}.
     * @param conf a previous {@link MapConfiguration} to update or
     * null to create a new {@link MapConfiguration}.
     * @param uri the URI for which the given settings should apply.
     * @param settings a {@link Map} of strings key value pairs. Keys correspond to the names of the settings, values
     * to their values.
     * @return a new {@link MapConfiguration} or updated version of the given one.
     * @see MapConfiguration#put(java.lang.String, java.lang.String, java.lang.String)
     */
    public MapConfiguration configure (MapConfiguration conf, String uri,
                                       Map<String, String> settings) {
        MapConfiguration result = conf == null ? new MapConfiguration() : conf;
        for (Entry<String, String> entry : settings.entrySet()) {
            result.put(uri, entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Creates a {@link URIResolver}.
     * @param source the a source/context path to resolve relative path name URIs to macro files.
     * @param implementEscapes whether {@link StringMapping} instances generated by the
     * {@link URIResolver} should implement escape characters. If true the resolver generates
     * {@link EscapeMapping} instances, if false it generates {@link BasicMapping} instances instead.
     * @return a new {@link URIResolverImpl}.
     */
    public URIResolver uriResolver (String source,
                                    final boolean implementEscapes) {
        return new URIResolverImpl(file(source)) {

            @Override
            public StringMapping createMapping (File f) {
                return implementEscapes
                        ? new EscapeMapping(f)
                        : new BasicMapping(f);
            }
        };
    }

    /**
     * Creates an {@link EditorSource}.
     * @param feed the {@link SimpleStringsFeeder} to edit.
     * @param edits a {@link Map} of string key value pairs where the keys correspond to keys
     * producted in the given {@link SimpleStringsFeeder} and the values are replacement values for the corresponding
     * equivalents in the given {@link SimpleStringsFeeder}.
     * @return a new {@link EditorSource}.
     */
    public EditorSource edit (SimpleStringsFeeder feed,
                              Map<String, String> edits) {
        return new EditorSource(edits, feed);
    }

    /**
     * Creates an {@link EditorSource}.
     * @param file path to the Strings file to edit.
     * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
     * a given Strings key being edited.
     * @param edits a {@link Map} of string key value pairs where the keys correspond to keys
     * produced in the given Strings file and the values are replacement values for the corresponding
     * equivalents in the given Strings file.
     * @param keys a {@link Set} of Strings key values if the given Strings file is ordered, or null
     * if it not.
     * @return a new {@link EditorSource}.
     */
    public EditorSource edit (String file, KeyResolver resolver,
                              Map<String, String> edits, Set<String> keys) {

        StringsReader sr = keys == null
                ? new StringsReader(file(file))
                : new StringsReader(file(file), keys);
        return edit(fromIterable(sr, resolver, keys != null), edits);
    }

    /**
     * Creates an {@link EditorSource}.
     * @param file path to the Strings file to edit.
     * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
     * a given Strings key being edited.
     * @param edits a {@link Map} of string key value pairs where the keys correspond to keys
     * produced in the given Strings file and the values are replacement values for the corresponding
     * equivalents in the given Strings file.
     * @param keys a {@link List} of Strings key values if the given Strings file is ordered, or null
     * if it not.
     * @return a new {@link EditorSource}.
     */
    public EditorSource edit (String file, KeyResolver resolver,
                              Map<String, String> edits, List<String> keys) {
        return this.edit(file, resolver, edits, keys == null ? null : new KeyList(
                keys));
    }

    /**
     * Creates a {@link SimpleStringsFeeder}.
     * @param data a {@link Map} of string key value pairs.
     * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
     * a given Strings key.
     * @param ordered whether the output Strings file should be written in
     * ordered mode (true), or keyed mode (false).
     * If true, the given data {@link Map} must be an instance of {@link SortedMap}; if
     * false it should be a different kind of map.
     * @return a new {@link MapSource}.
     */
    public SimpleStringsFeeder fromMap (Map<String, String> data,
                                        KeyResolver resolver,
                                        boolean ordered) {
        if (ordered) {
            if (data instanceof SortedMap) {
                return new MapSource((SortedMap<String, String>) data, resolver,
                                     options);
            }
            throw new IllegalArgumentException(
                    Messages.OrderedSourceTypeMismatch.format(SortedMap.class,
                                                              data.getClass()));
        }
        else {
            return new MapSource(data, resolver, options);
        }
    }

    /**
     * Creates a {@link StringsData} record.
     * @param key key to use.
     * @param value value to use.
     * @return a new {@link StringsData} object.
     */
    public StringsData createData (final String key, final String value) {
        return new StringsData() {

            @Override
            public String key () {
                return key;
            }

            @Override
            public String value () {
                return value;
            }

            @Override
            public boolean equals (Object obj) {
                return obj != null
                        && obj instanceof StringsData
                        && equal(this, (StringsData) obj, true);
            }

            @Override
            public int hashCode () {
                return hash(this, true);
            }
        };
    }

    /**
     * Creates a {@link SimpleStringsFeeder} from a {@link SortedSet} of {@link StringsData} records.
     * @param data an {@link SortedSet} of {@link StringsData} records.
     * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
     * a given Strings key.
     * @return a new {@link IteratorSource}.
     */
    public SimpleStringsFeeder orderedSource (SortedSet<StringsData> data,
                                              KeyResolver resolver) {
        return fromIterable(data, resolver, true);
    }

    /**
     * Sorts an array of {@link StringsData} records against a reference map.
     * @param data an array of {@link StringsData} records.
     * @param pList reference {@link Map} that binds reference values (indicec/ids/priorities or whatever)
     * to key values. These reference values determine the order of the {@link StringsData} records.
     * @return a {@link SortedSet} of {@link StringsData} records.
     */
    @SuppressWarnings("unchecked")
    public SortedSet<StringsData> sort (StringsData[] data,
                                        Map<String, Integer> pList) {
        return sort(Arrays.asList(data), new LookupOrder(pList), false);
    }

    /**
     * Converts an array of {@link StringsData} records into a {@link SortedSet}.
     * @param data an array of {@link StringsData} records.
     * @return a {@link SortedSet} which maintains the order of the elements as they are given in the array.
     */
    public SortedSet<StringsData> sort (StringsData[] data) {
        return sort(Arrays.asList(data));
    }

    /**
     * Sorts a {@link Collection} of {@link StringsData} records against a reference list.
     * @param pList reference list of key values in the order that their corresponding records are supposed to be sorted.
     * @param data a {@link Collection} of {@link StringsData} records.
     * @return a {@link SortedSet} of {@link StringsData} records.
     */
    public SortedSet<StringsData> sort (List<String> pList,
                                        Collection<StringsData> data) {
        return sort(data, new DefinedOrder(pList), false);
    }

    /**
     * Sorts a {@link Collection} of {@link StringsData} records against a reference map.
     * @param pList reference map that binds reference values (indicec/ids/priorities or whatever)
     * to key values. These reference values determine the order of the {@link StringsData} records.
     * @param data a {@link Collection} of {@link StringsData} records.
     * @return a {@link SortedSet} of {@link StringsData} records.
     */
    public SortedSet<StringsData> sort (Map<String, Integer> pList,
                                        Collection<StringsData> data) {
        return sort(data, new LookupOrder(pList), false);
    }

    /**
     * Converts a {@link List} of {@link StringsData} records into a {@link SortedSet}.
     * @param data a {@link List} of {@link StringsData} records.
     * @return a {@link SortedSet} which maintains the order of the elements as they are given in the {@link List}.
     */
    public SortedSet<StringsData> sort (List<StringsData> data) {
        return sort(data, new DefinedOrder(data), true);
    }

    /**
     * Implicitly defined order: elements compare by their index in a reference list.
     */
    private class DefinedOrder implements Comparator {

        private DefinedOrder (List reference) {
            this.reference = reference;
        }
        private final List reference;

        /**
         * Compares two elements against their index in a reference list.
         * @param o1 first element to compare.
         * @param o2 second element to compare.
         * @return an integer that falls in one of three categories:
         * <dl>
         * <dt>Negative</dt><dd>the first element is considered “less” than the second.
         * </dd><dt>Zero</dt><dd>both elements are considered equal.
         * </dd><dt>Negative</dt><dd>
         * the second element is considered “greater” than the second.
         * </dd></dl>
         */
        @Override
        public int compare (Object o1, Object o2) {
            return reference.indexOf(o1)
                    - reference.indexOf(o2);
        }
    }

    /**
     * Explicitly defined order: elements compare by their assigned value in a reference map.
     */
    private static class LookupOrder implements Comparator<String> {

        /**
         * Creates a new {@link LookupOrder}.
         * @param reference a reference {@link Map} that binds integer values to string keys.
         */
        public LookupOrder (Map<String, Integer> reference) {
            this.reference = reference;
        }
        private final Map<String, Integer> reference;

        /**
         * Compares two elements against their assigned value in a reference map.
         * @param o1 first element to compare.
         * @param o2 second element to compare.
         * @return an integer that falls in one of three categories:
         * <dl>
         * <dt>Negative</dt><dd>the first element is considered “less” than the second.
         * </dd><dt>Zero</dt><dd>both elements are considered equal.
         * </dd><dt>Negative</dt><dd>
         * the second element is considered “greater” than the second.
         * </dd></dl>
         */
        @Override
        public int compare (String o1, String o2) {
            return reference.get(o1) - reference.get(o2);
        }
    }

    private class SortOrder implements Comparator {

        private final boolean full;
        private final Comparator comp;

        public SortOrder (boolean full, Comparator order) {
            this.full = full;
            this.comp = order;
        }

        /**
         * Compares two elements using a reference {@link Comparator}.
         * This method unifies string and {@link StringsData} comparison.
         * @param o1 first element to compare.
         * @param o2 second element to compare.
         * @return an integer that falls in one of three categories:
         * <dl>
         * <dt>Negative</dt><dd>the first element is considered “less” than the second.
         * </dd><dt>Zero</dt><dd>both elements are considered equal.
         * </dd><dt>Negative</dt><dd>
         * the second element is considered “greater” than the second.
         * </dd></dl>
         */
        @Override
        @SuppressWarnings("unchecked")
        public int compare (Object o1, Object o2) {
            return full
                    ? comp.compare(o1, o2)
                    : comp.compare(((StringsData) o1).key(),
                                   ((StringsData) o2).key());
        }
    }

    /**
     * Sorts a {@link Map} of strings key value pairs by a reference {@link Map} which assigns
     * values to individual Strings keys.
     * @param data a {@link Map} of strings key value pairs that represent {@link StringsData} records.
     * @param pList reference map that binds reference values (indicec/ids/priorities or whatever)
     * to key values. These reference values determine the order of the {@link StringsData} records.
     * Keys correspond to Strings keys.
     * @return a {@link SortedMap} of string key value pairs.
     */
    public SortedMap<String, String> sort (Map<String, String> data,
                                           Map<String, Integer> pList) {
        return sort(data, new LookupOrder(pList));
    }

    private SortedMap<String, String> sort (Map<String, String> data,
                                            LookupOrder comp) {
        @SuppressWarnings("unchecked")
        TreeMap<String, String> map = new TreeMap<String, String>(comp);
        map.putAll(data);
        return map;
    }

    private SortedSet<StringsData> sort (Collection<StringsData> data,
                                         Comparator comp,
                                         boolean full) {
        @SuppressWarnings("unchecked")
        TreeSet<StringsData> set = new TreeSet<StringsData>(new SortOrder(full,
                                                                          comp));
        set.addAll(data);
        return set;
    }

    private void format (boolean type) {
        options.put(FormatOption.class,
                    type ? FormatOption.Ordered : FormatOption.Keyed);
    }

    /**
     * Specify the {@link FormatOption format} of the Strings file to generate.
     * @param spec the name of a {@link FormatOption}.
     * Use one of: “Keyed”, “Ordered” or “KeyedTable”.
     */
    public void format (String spec) {
        options.put(FormatOption.class, FormatOption.valueOf(spec));
    }

    /**
     * Creates a {@link SimpleStringsFeeder} from a collection of {@link StringsData} records.
     * @param data an {@link Iterator} that produces {@link StringsData} records.
     * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
     * a given Strings key.
     * @param ordered whether the output Strings file should be written in
     * ordered mode (true), or keyed mode (false).
     * @return a new {@link IteratorSource}.
     */
    public SimpleStringsFeeder fromIterator (Iterator<StringsData> data,
                                             KeyResolver resolver,
                                             boolean ordered) {
        format(ordered);
        return new IteratorSource(data, resolver, options);
    }

    /**
     * Creates a {@link SimpleStringsFeeder} from a collection of {@link StringsData} records.
     * @param data an {@link Iterable} collection of {@link StringsData} records.
     * @param resolver a {@link KeyResolver} to produce macro names and {@link StringMapping} instances for
     * a given Strings key.
     * @param ordered whether the output Strings file should be written in
     * ordered mode (true), or keyed mode (false).
     * @return a new {@link IteratorSource}.
     */
    public SimpleStringsFeeder fromIterable (Iterable<StringsData> data,
                                             KeyResolver resolver,
                                             boolean ordered) {
        format(ordered);
        return new IteratorSource(data, resolver, options);
    }

    /**
     * Compares two {@link StringsData} records for equality.
     * @param datum1 the first {@link StringsData} record.
     * @param datum2 the second {@link StringsData} record.
     * @param full whether or not to take both {@link StringsData#value()} into account when determining equality.
     * @return true if both records are equal, false if not.
     */
    public boolean equal (StringsData datum1, StringsData datum2, boolean full) {
        boolean result = datum1.key().equals(datum2.key());
        return full ? result && datum1.value().equals(datum2.value()) : result;
    }

    /**
     * Calculates a hash code for a {@link StringsData} record.
     * @param datum the record to hash.
     * @param full whether or not to take both {@link StringsData#value()} into account when generating the
     * hash code.
     * @return a hash code based on hashing the component parts of a {@link StringsData}.
     */
    public int hash (StringsData datum, boolean full) {
        return full
                ? MapConfiguration.mapKey(datum.key(), datum.value()).hashCode()
                : datum.key().hashCode();
    }
    
    /**
     * Create a {@link StringsFeeder} which will feed an XML document to the 
     * strings compiler
     * @param required the source XML document to feed. This argument is required 
     * and must point to an existing XML file.
     * @param optional optional custom {@link URIResolver}. If this argument is 
     * null, a default {@link URIResolver} will be used which will resolve XML 
     * namespace URIs as relative to the XML source file
     * @return a new {@link XMLSource}.
     */
    public XMLSource xmlSource (String required, URIResolver optional) {
        File fd=file(required);
        return new XMLSource(fd,
                             optional == null
                ? new URIResolverImpl(fd)
                : optional, options);
    }
}
