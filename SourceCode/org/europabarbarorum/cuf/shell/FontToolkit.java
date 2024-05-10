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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.CUFWriter;
import org.europabarbarorum.cuf.font.MappedSource;
import org.europabarbarorum.cuf.font.MappedSource.Mapping;
import org.europabarbarorum.cuf.font.MixedSource;
import org.europabarbarorum.cuf.font.MixedSource.MixinMode;
import org.europabarbarorum.cuf.font.RestrictedSource;
import org.europabarbarorum.cuf.font.RestrictedSource.Restriction;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.font.impl.WrappedSource.CompatibleSource;
import org.europabarbarorum.cuf.font.impl.WrappedSource.ControlSource;
import org.europabarbarorum.cuf.font.pipes.CUFReader;
import org.europabarbarorum.cuf.font.pipes.SystemFontSource;
import org.europabarbarorum.cuf.gui.PreviewWindow;
import org.europabarbarorum.cuf.shell.Shell.Toolkit;
import org.europabarbarorum.cuf.strings.impl.ControlCharacter;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.MimeTag;
import org.europabarbarorum.cuf.support.OptionMap;
import org.europabarbarorum.cuf.support.PathParser;
import org.europabarbarorum.cuf.support.Preview;

/**
 * This class provides convenience methods and safety-catches for the construction and initialisation of {@link CUFSource} objects.
 * It is very much recommended to build CUF fonts using the {@link FontToolkit},
 * if you do not want to program your own custom {@link CUFSource} objects.
 * @author Johan Ouwerkerk
 */
public class FontToolkit extends Toolkit {

    /**
     * Creates a new {@link FontToolkit}.
     * @param parse a {@link PathParser} which provides context for resolving
     * relative file names.
     */
    public FontToolkit (PathParser parse) {
        super(parse);
    }

    /**
     * Creates a new {@link FontToolkit}.
     * @param context the {@link Shell} which provides context for resolving
     * relative file names.
     */
    public FontToolkit (Shell context) {
        super(context);
    }

    /**
     * Removes gaps in the chartable of a {@link CUFSource}.
     * This method compacts a font by ensuring that all its characters form a single contiguous block in
     * its chartable.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this
     * type information is preserved.
     * @param toCompact the {@link CUFSource} to compact.
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link MappedSource} that is a compacted version of the given font.
     */
    public <C> MappedSource<C> compact (CUFSource<C> toCompact, String name) {
        Mapping m = MappedSource.compact();
        MappedSource<C> ms = new MappedSource<C>(toCompact, m);
        ms.init(name);
        return ms;
    }

    /**
     * Re-maps the characters in the chartable of a given {@link CUFSource} according to a lookup table.
     * Characters in the source font that do not appear as
     * keys in the lookup table are silently dropped from the result.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this
     * type information is preserved.
     * @param toMap the {@link CUFSource} to re-map.
     * @param mapping a {@link Map} that provides a lookup table to map characters from the font to their
     * new values.
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link MappedSource} that contains all characters contained in <em>both</em> the given font and
     * the keys in the given {@link Map}, remapped to their corresponding values in the lookup table.
     * @see MappedSource#map(java.util.Map)
     */
    public <C> MappedSource<C> map (final CUFSource<C> toMap,
                                    final Map<Character, Character> mapping,
                                    String name) {
        MappedSource<C> ms = new MappedSource<C>(toMap,
                                                 MappedSource.map(mapping));
        ms.init(name);
        return ms;
    }

    /**
     * Re-maps the characters in a font by shifting the entries in the chartable.
     * This method discards characters which would fall outside of the range of characters
     * supported by the CUF file format as a result of shifting them by the given amount.
     * This method is the opposite of {@link #shiftDown(org.europabarbarorum.cuf.font.CUFSource, java.lang.Character, java.lang.String) }.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this 
     * type information is preserved.
     * @param toMap the {@link CUFSource} to re-map.
     * @param lowerBound character that marks the lower bound. No character in the result will fall below it; and the
     * first character in the original font will be shifted to occupy this position.
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link MappedSource} with the entries in its chartable shifted such that no character falls
     * below the given lower bound.
     * @see #shift(org.europabarbarorum.cuf.font.CUFSource, int, java.lang.String)
     */
    public <C> MappedSource<C> shiftUp (final CUFSource<C> toMap,
                                        final Character lowerBound, String name) {
        return shift(toMap, IOHelp.codeOf(lowerBound), name);
    }

    /**
     * Re-maps the characters in a font by shifting the entries in the chartable.
     * This method discards characters which would fall outside of the range of characters
     * supported by the CUF file format as a result of shifting them by the given amount.
     * This method is the opposite of {@link #shiftUp(org.europabarbarorum.cuf.font.CUFSource, java.lang.Character, java.lang.String) }.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this 
     * type information is preserved.
     * @param toMap the {@link CUFSource} to re-map.
     * @param upperBound character that marks the upper bound. No character in the result will rise above it; and the
     * last character in the original font will be shifted to occupy this position.
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link MappedSource} with the entries in its chartable shifted such that no character rises
     * above the given upper bound.
     * @see #shift(org.europabarbarorum.cuf.font.CUFSource, int, java.lang.String)
     */
    public <C> MappedSource<C> shiftDown (final CUFSource<C> toMap,
                                          final Character upperBound,
                                          final String name) {
        return shift(toMap, -IOHelp.codeOf(upperBound), name);
    }

    /**
     * Re-maps the characters in a font by shifting the entries in the chartable.
     * This method discards characters which would fall outside of the range of characters
     * supported by the CUF file format as a result of shifting them by the given amount.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this 
     * type information is preserved.
     * @param toMap the {@link CUFSource} to re-map.
     * @param amount the amount to shift positions in the chartable by.
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link MappedSource} with the position of the characters in its chartable shifted by the given
     * amount.
     * @see MappedSource#shift(int)
     */
    public <C> MappedSource<C> shift (final CUFSource<C> toMap, final int amount,
                                      String name) {
        MappedSource<C> ms = new MappedSource<C>(toMap, MappedSource.shift(
                amount));
        ms.init(name);
        return ms;
    }

    /**
     * Remaps the characters in the chartable of a given {@link CUFSource}. This method may throw an {@link IllegalArgumentException} if the size of the values {@link Collection} is
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
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this 
     * type information is preserved.
     * @param toMap the {@link CUFSource} to re-map
     * @param keys the {@link Collection} of keys to re-map.
     * @param values the {@link Collection} of values to use for the corresponding keys in the result font
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link MappedSource} with each key re-mapped to its corresponding value
     * @see #map(org.europabarbarorum.cuf.font.CUFSource, java.util.Map, java.lang.String)
     * @see #mapArrays(org.europabarbarorum.cuf.font.CUFSource, java.lang.Character[], java.lang.Character[], java.lang.String)
     * @see MappedSource#collections(java.util.Collection, java.util.Collection)
     */
    public <C> MappedSource<C> mapCollections (final CUFSource<C> toMap,
                                               final Collection<Character> keys,
                                               final Collection<Character> values,
                                               String name) {
        MappedSource<C> ms = new MappedSource<C>(toMap,
                                                 MappedSource.collections(keys,
                                                                          values));
        ms.init(name);
        return ms;
    }

    /**
     * Remaps the characters in the chartable of a given {@link CUFSource}. This method may throw an {@link IllegalArgumentException} if the size of the values array is
     * not large enough to cover for all the given keys.
     * <p>Each character from the chartable in the given font that is
     * also present in the array of given keys is mapped to a corresponding value obtained from the given
     * array of values. Characters that appear in the source font but not among the given keys are silently
     * dropped. Furthermore if a key appears more than once in the given keys this method will silently drop all but the
     * first of those key-value pairs which use that key.</p>
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this 
     * type information is preserved.
     * @param toMap the {@link CUFSource} to re-map
     * @param keys the array of keys to re-map.
     * @param values the array of values to use for the corresponding keys in the result font
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link MappedSource} with each key re-mapped to its corresponding value
     * @see #mapCollections(org.europabarbarorum.cuf.font.CUFSource, java.util.Collection, java.util.Collection, java.lang.String)
     * @see MappedSource#arrays(java.lang.Character[], java.lang.Character[]) 
     */
    public <C> MappedSource<C> mapArrays (final CUFSource<C> toMap,
                                          Character[] keys,
                                          Character[] values, String name) {
        MappedSource<C> ms = new MappedSource<C>(toMap,
                                                 MappedSource.arrays(keys,
                                                                     values));
        ms.init(name);
        return ms;
    }

    /** Restricts a {@link CUFSource} to remove all characters that map to a
     * {@link ControlCharacter} except the non breaking space.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this
     * type information is preserved.
     * @param toEdit the {@link CUFSource} font to restrict
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link RestrictedSource} version of the given font with the control characters removed.
     * @see ControlCharacter
     * @see ControlCharacter#NonBreakingSpace
     */
    @SuppressWarnings("unchecked")
    public <C> RestrictedSource<C> removeControlCharacters (
            CUFSource<C> toEdit,
            String name) {
        HashSet<Character> s = new HashSet<Character>();
        Set<Character> ctls = ControlCharacter.charset();
        ctls.remove(ControlCharacter.NonBreakingSpace.charValue);
        for (Character c : toEdit.getCharTable().keySet()) {
            if (ctls.contains(toEdit.trackCharacter(c))) {
                s.add(c);
            }
        }
        Restriction r = RestrictedSource.collection(s, false);
        RestrictedSource<C> rs = new RestrictedSource(toEdit, r);
        rs.init(name);
        return rs;
    }

    /**
     * Re-maps a (subset of) a chartable so that (some) characters in it are located at
     * positions corresponding to their proper UTF-16LE code point. This enhances compatibility of the
     * font outside of the CUF program because programs that assume a direct correspondence between
     * character (code point) and glyph will better be able to render text with it without need for
     * Strings files or similar.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this
     * type information is preserved.
     * @param edit the {@link CUFSource} font to restrict
     * @param src a {@link CUFSource} which contains (or is) the font that provided the
     * glyphs for the characters that are to be re-mapped in the font to edit.
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link CompatibleSource} version of the {@link CUFSource} to edit.
     */
    public <C> CompatibleSource<C> fixChartable (CUFSource<C> edit,
                                                 CUFSource src,
                                                 String name) {
        CompatibleSource<C> ms = new CompatibleSource<C>(edit, src);
        ms.init(name);
        return ms;
    }

    /**
     * Edits a given font to use 1pixel dummies for glyphs that correspond to {@link ControlCharacter control characters}
     * in the font. This method ensures that all control characters will be present if the {@link CUFSource} is
     * compiled to a Macro file, and that tab support will work reliably when compiling strings files against the font.
     * @param src the {@link CUFSource} to edit
     * @param name a name to identify the font in a logical hierarchy of {@link CUFSource} objects.
     * @return a {@link ControlSource} consisting of the original font with control characters replaced by
     * 1px dummies.
     */
    @SuppressWarnings("unchecked")
    public ControlSource editControlCharacters (CUFSource<?> src, String name) {
        ControlSource ms = new ControlSource(src);
        ms.init(name);
        return ms;
    }

    /**
     * Restrict a given font to the subset of characters represented by the given collection.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this
     * type information is preserved.
     * @param toRestrict the {@link CUFSource} to restrict.
     * @param range the {@link Collection} of characters to restrict the font to.
     * @param name the logical name of the new font. This is used for identification purposes.
     * @param includeRange controls whether or not the range parameter defines the characters to include.
     * @return a {@link RestrictedSource} version of the font.
     * @see RestrictedSource#collection(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public <C> RestrictedSource<C> restrict (final CUFSource<C> toRestrict,
                                             final Collection<Character> range,
                                             String name,
                                             final boolean includeRange) {
        Restriction r = RestrictedSource.collection(range, includeRange);
        RestrictedSource rs = new RestrictedSource(toRestrict, r);
        rs.init(name);
        return rs;
    }

    /**
     * Restrict a given font to the given subset of (supported) characters.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this
     * type information is preserved.
     * @param toRestrict the {@link CUFSource} to restrict.
     * @param range the array of characters to restrict the font to.
     * @param name the logical name of the new font. This is used for identification purposes.
     * @param includeRange controls whether or not the range parameter defines the characters to include.
     * @return a {@link RestrictedSource} version of the font.
     * @see #restrict(org.europabarbarorum.cuf.font.CUFSource, java.util.Collection, java.lang.String, boolean)
     */
    public <C> RestrictedSource<C> restrict (final CUFSource<C> toRestrict,
                                             final Character[] range,
                                             String name,
                                             boolean includeRange) {
        return restrict(toRestrict, Arrays.asList(range), name, includeRange);
    }
    private OptionMap options = new OptionMap();

    /**
     * Controls whether or not a value will be automatically generated/overwritten for a given {@link CUFProperty}
     * when this {@link FontToolkit} is used to compile a CUF file.
     * @param prop the {@link CUFProperty} to configure.
     * @param enabled whether or not the value for the property will be drawn from a {@link CUFSource} or from what
     * shadow copies are generated in a {@link CUFWriter}.
     */
    public void useAutoValue (CUFProperty prop, boolean enabled) {
        CUFWriter.useAutoValue(prop, enabled, options);
    }

    /**
     * Controls whether or not a value will be automatically generated/overwritten for a given {@link CUFProperty}
     * when this {@link FontToolkit} is used to compile a CUF file.
     * @param propName the name of the {@link CUFProperty} to configure.
     * @param enabled whether or not the value for the property will be drawn from a {@link CUFSource} or from what
     * shadow copies are generated in a {@link CUFWriter}.
     */
    public void useAutoValue (String propName, boolean enabled) {
        CUFProperty p = CUFProperty.valueOf(propName);
        this.useAutoValue(p, enabled);
    }

    /**
     * Sets up a {@link CompileUnit} for compilation of either
     * a {@link CUFSource} to either a CUF file or Macro file depending on
     * the given type flag.
     * @param toCompile the {@link CUFSource} to compile.
     * @param result the file to save the result to
     * @param type a boolean flag that indicates what output format to generate from the
     * given {@link CUFSource}. Use true to build a CUF file and false to build a Macro file.
     * @return a {@link CompileUnit} to compile the font.
     * @see CompileUnit#run()
     */
    public CompileUnit compile (CUFSource toCompile, String result, boolean type) {
        return new CompileUnit(CompileUnit.getJob(
                file(result),
                type ? CompileUnit.FileType.CUF : CompileUnit.FileType.Macro,
                options,
                toCompile));
    }

    /**
     * Sets up a {@link CompileUnit} for compilation of
     * a {@link CUFSource} to a CUF file.
     * @param toCompile the {@link CUFSource} to compile.
     * @param result the file to save the result to.
     * @return a {@link CompileUnit} to compile the font.
     * @see #compile(org.europabarbarorum.cuf.font.CUFSource, java.lang.String, boolean)
     */
    public CompileUnit compile (CUFSource toCompile, String result) {
        return compile(toCompile, result, true);
    }

    /**
     * Restrict a given font to a subset of its supported characters.
     * This subset is all characters between ‘start’ and ‘end’ inclusive; characters are compared to each other according to
     * {@link Character#compareTo(java.lang.Character) }.
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this
     * type information is preserved.
     * @param toRestrict the {@link CUFSource} to restrict.
     * @param start the first character (inclusive) of the subset that the font is to be restricted to
     * @param end the last character (inclusive) of the subset that the font is to be restricted to
     * @param name the logical name of the new font. This is used for identification purposes.
     * @param includeRange controls whether or not the start and end parameters define the characters to include.
     * @return a {@link RestrictedSource} version of the font.
     * @see #restrict(org.europabarbarorum.cuf.font.CUFSource, java.util.Collection, java.lang.String, boolean)
     * @see RestrictedSource#range(java.lang.Character, java.lang.Character, boolean)
     */
    @SuppressWarnings("unchecked")
    public <C> RestrictedSource<C> restrict (final CUFSource<C> toRestrict,
                                             final Character start,
                                             final Character end,
                                             String name,
                                             final boolean includeRange) {
        Restriction r = RestrictedSource.range(start, end, includeRange);
        RestrictedSource rs = new RestrictedSource(toRestrict, r);
        rs.init(name);
        return rs;
    }

    /**
     * Restrict a given font to a subset of its supported characters.
     * This subset is all characters between first character of ‘start’ and first character of ‘end’ inclusive.
     * This method is a convenience version of
     * {@link #restrict(org.europabarbarorum.cuf.font.CUFSource, java.lang.Character, java.lang.Character, java.lang.String, boolean) restrict} for
     * scripts. 
     * @param <C> type parameter of the {@link CUFSource} edited. This type simply expresses that this
     * type information is preserved.
     * @param toRestrict the {@link CUFSource} to restrict.
     * @param startChar string containing just the first character (inclusive) of the subset that the font is to be restricted to.
     * This character should be the first in the string, all other characters are ignored.
     * @param endChar string containing just the last character (inclusive) of the subset that the font is to be restricted to.
     * This character should be the first in the string, all other characters are ignored.
     * @param name the logical name of the new font. This is used for identification purposes.
     * @param includeRange controls whether or not the startChar and endChar parameters define the characters to include.
     * @return a {@link RestrictedSource} version of the font.
     * @see #restrict(org.europabarbarorum.cuf.font.CUFSource, java.util.Collection, java.lang.String, boolean) 
     */
    public <C> RestrictedSource<C> restrict (CUFSource<C> toRestrict,
                                             String startChar,
                                             String endChar,
                                             String name,
                                             boolean includeRange) {
        if (startChar == null || startChar.equals("")) {
            throw new IllegalArgumentException(
                    Messages.FontStartCharStringInvalid.getText());
        }
        if (endChar == null || endChar.equals("")) {
            throw new IllegalArgumentException(
                    Messages.FontEndCharStringInvalid.getText());
        }
        return restrict(toRestrict,
                        startChar.charAt(0),
                        endChar.charAt(0),
                        name,
                        includeRange);
    }

    /**
     * Mixes the given fonts into one.
     * The resulting font will be tracked by this toolkit. 
     * Each boolean value of the second parameter corresponds to a {@link CUFSource} in the
     * first. If that boolean is set to “true” this method removes previous character->glyph mappings in the
     * intermediate result; allowing you to override previous mappings. The fonts are added in order, thus if multiple consecutive
     * fonts are specified to override these mappings and they provide the same mappings then the mappings of the last font in the
     * sources array will be used.
     * @param sources an array of {@link CUFSource fonts} to mix.
     * @param modes an array of {@link MixinMode} flags. Each flag corresponds to a
     * {@link CUFSource} from the sources array and determines if that source may overwrite entries inserted by
     * previous {@link CUFSource} instances.
     * @param name the name to use for identifying this font in a logical hierarchy of {@link CUFSource} objects.
     * @return the mixed result.
     */
    public MixedSource mix (CUFSource[] sources, MixinMode[] modes, String name) {
        if (sources == null || sources.length == 0) {
            throw new IllegalArgumentException(
                    Messages.SourcesListInvalid.getText());
        }
        if (modes == null || modes.length == 0) {
            throw new IllegalArgumentException(
                    Messages.MixinModeListInvalid.getText());
        }
        MixedSource ms = new MixedSource(sources, modes);
        ms.init(name);
        return ms;
    }

    /**
     * This method provides interpretation of boolean primitives as {@link MixinMode}.
     * @param flag the value to interpret as {@link MixinMode}.
     * @return {@link MixinMode#Remove} if flag is true, or {@link MixinMode#Keep} if it is false.
     */
    public MixedSource.MixinMode toModeFlag (boolean flag) {
        return flag ? MixedSource.MixinMode.Remove : MixedSource.MixinMode.Keep;
    }

    /**
     * Create a font from a indentifying string.<p>
     * This method first tries to see if the argument is a valid file.
     * If it is, the method attempts to load the font from that file. 
     * If it is not then it attempts to parse the string using the {@link java.awt.Font#decode(java.lang.String) decode method
     * of the JVM}.
     * Examples:
     * </p>
     * <blockquote><pre>
     * {@code FontToolkit tool=new FontToolkit();
     * CUFSource fontString= tool.fromFont('UnBatang BOLD 32'); // will (likely) be parsed by the JVM.
     * CUFSource fontFile  = tool.fromFont('path/to/font/file.cuf'); // will be loaded from disk if it exists.
     * }
     * </pre></blockquote>
     * @param fontString the string that indentifies the font.
     * @return the loaded {@link CUFSource}, possibly null if this method failed.
     * @see Font#decode(java.lang.String) 
     * @see #fromFile(java.lang.String, java.util.Map)
     * @see #fromFont(java.awt.Font) 
     * @see #fromFont(java.util.Map)
     * @see #fromFile(java.lang.String)
     */
    public CUFSource fromFont (String fontString) {
        File f = file(fontString);
        if (f.exists()) {
            return fromFile(fontString, null);
        }
        else {
            SystemFontSource sfs = new SystemFontSource();
            sfs.init(fontString);
            return sfs;
        }
    }

    /**
     * Creates a font according to the values for the {@link TextAttribute} keys specified in the attributes {@link Map}.
     * @param fontAttrs a {@link Map} of {@link TextAttribute} keys and associated values; describing this font.
     * @return the {@link SystemFontSource} created from the given map.
     * @see Font#Font(java.util.Map)
     * @see #fromFont(java.lang.String)
     * @see #fromFont(java.awt.Font)
     * @see #fromFile(java.lang.String, java.util.Map)
     */
    public SystemFontSource fromFont (Map<TextAttribute, Object> fontAttrs) {
        Font f = new Font(fontAttrs);
        return fromFont(f);
    }

    /**
     * Converts a {@link Font} object into a font that can be manipulated by this toolkit.
     * @param f the {@link Font} to use.
     * @return the {@link SystemFontSource} created.
     * @see #fromFile(java.lang.String, java.util.Map)
     * @see #fromFont(java.util.Map) 
     * @see #fromFont(java.lang.String)
     */
    public SystemFontSource fromFont (Font f) {
        SystemFontSource sfs = new SystemFontSource();
        sfs.init(f);
        return sfs;
    }

    /**
     * Loads a font file from disk.
     * @param file the CUF file to load.
     * @return the {@link CUFReader} created.
     * @see #fromFont(java.lang.String)
     * @see #fromFile(java.lang.String, java.util.Map)
     */
    public CUFSource fromFile (String file) {
        return fromFile(file, null);
    }

    /**
     * Previews a given {@link CUFSource}.
     * @param font a {@link CUFSource}.
     * @return true if a preview could be constructed or false if no preview could be
     * generated.
     * @see #preview(org.europabarbarorum.cuf.font.CUFSource, org.europabarbarorum.cuf.support.Preview, java.lang.String)
     */
    public static boolean preview (CUFSource font) {
        return preview(font,
                       null,
                       Messages.FontWindowTitleFormat.format(font.getCufSource()));
    }

    /**
     * Causes a {@link PreviewWindow} to be created and shown.
     * @param font the {@link CUFSource} to preview.
     * @param previewData a {@link Preview} model to use for obtaining text to preview.
     * @param title the window title to use.
     * @return true if a preview could be constructed or false if no preview could be
     * generated. In particular this method returns false if either:
     * <ul>
     * <li>{@link GraphicsEnvironment#isHeadless() } returns false</li>
     * <li>{@link CUFSource#isPrepared() } for the given font returns false</li>
     * </ul>
     */
    public static boolean preview (final CUFSource font,
                                   final Preview previewData,
                                   final String title) {
        if (font.isPrepared() && !GraphicsEnvironment.isHeadless()) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run () {
                    PreviewWindow window = new PreviewWindow();
                    window.updateModel(previewData);
                    window.updateModel(font);
                    window.setTitle(title);
                    window.setVisible(true);
                }
            });
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Reads a font from a file and, if supported, optionally adjusts the font according to the
     * value for the {@link TextAttribute} keys in the attributes {@link Map}.
     * @param source the {@link File} to read the font from
     * @param fontAttrs the {@link Map} of font attributes to use. If you use this method to read a 
     * CUF file, simply specify null here (the attributes will be ignored anyway). 
     * If you use this method to read a native font (TTF or PFA) from a 
     * file you are very much recommended to construct such a {@link Map}. Native font defaults are not well specified and 
     * likely to be unsuitable.
     * @return the {@link CUFSource} created from the file and attributes. If no {@link CUFSource} could be constructed this 
     * method returns null.
     * @see #fromFont(java.lang.String)
     */
    public CUFSource fromFile (String source,
                               final Map<TextAttribute, Object> fontAttrs) {
        File file = file(source);
        final MimeTag tag = MimeTag.getType(file);
        switch (tag) {
            case TrueTypeFont: // same as with PFA:
            case Type1Font: // JVM can do heavy lifting
                try {
                    Font font = Font.createFont(
                            tag == MimeTag.TrueTypeFont ? Font.TRUETYPE_FONT : Font.TYPE1_FONT,
                            new FileInputStream(file));
                    if (fontAttrs == null) {
                        throw new IllegalAccessError(
                                Messages.NoAttributesWarning.getText());
                    }
                    else {
                        // derive font without touching the original data
                        Map<TextAttribute, Object> attrs = fontAttrs;
                        // do not touch FAMILY for fonts that are not installed
                        attrs.remove(TextAttribute.FAMILY);
                        font = font.deriveFont(attrs);
                    }

                    SystemFontSource sfs = new SystemFontSource();
                    sfs.init(font, source);
                    return sfs;
                }
                catch (Exception e) {
                    throw new IllegalArgumentException(
                            Messages.FontFileUnreadable.format(source, e),
                            e);
                }
            case CUFFont:
                CUFReader src = new CUFReader();
                src.init(file.toString());
                return src;
            case OpenTypeFont:
                throw new IllegalArgumentException(
                        Messages.MimeTypeNotSupportedError.format(source,
                                                                  tag.getText()));
            default:
                throw tag.exception(source);
        }
    }
}
