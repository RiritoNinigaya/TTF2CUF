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

import org.europabarbarorum.cuf.font.impl.CUFGlyph;
import org.europabarbarorum.cuf.font.impl.CharTableEntry;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension;
import java.util.ArrayList;
import java.util.Map;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;

/**
 * Interface to define what a CUFSource (the data source/backend used by a {@link CUFFont} actually is/provides.
 * @param <C> the type of entry used in the char table.
 * @author Johan Ouwerkerk
 */
public interface CUFSource<C> {

    /**
     * Track a character from the {@link #getCharTable() } to its source.
     * This method is intended to extract “macros” in a divide and conquer fashion from a complex 
     * {@link CUFSource} hierarchy. Fonts should return the precise
     * {@link CUFSource} that supplies the glyph for
     * this character.
     * @param c character to track
     * @return the original {@link CUFSource} which supplies the glyph for the given character.
     */
    public CUFSource backTrack (Character c);

    /**
     * Track a character from the {@link #getCharTable() } to its source entry.
     * This method is intended to find out mappings of input characters to different glyphs for the
     * generation of “macros” from a {@link CUFSource} hierarchy.
     * Fonts should return the precise character which visually corresponds with the glyph allocated for
     * the given character.
     * E.g.: if a font maps the character ‘A’ to the glyph corresponding rendering a ‘B’
     * it should return ‘B’ when asked to track ‘A’ here.
     * @param c the character to track
     * @return the character which is actually represented by the glyph that is allocated to the given
     * character in the chartable of this {@link CUFSource}.
     */
    public Character trackCharacter (Character c);

    /**
     * Get a copy of all CUF properties of this font. Implementations must
     * return a copy of the raw properties rather than a reference to same (as they might get overwritten).
     * @return a copy of the CUF properties of this font in their raw untranslated form.
     */
    int[] getCUFProperties ();

    /**
     * Get the value of a specific CUF property.
     * @param index the {@link CUFProperty} to lookup.
     * @return a value translated with {@link CUFProperty#translator} if available.
     * Note that if {@link #isAvailable(org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty) }
     * returns false for the given property then the return value should be considered meaningless.
     */
    int getCUFProperties (CUFProperty index);

    /**
     * Set the value of a CUF property at the specified index.
     * @param index index of the property to change
     * @param value new value of the property. Implementations translate the given
     * value with {@link CUFProperty#translator} before using it. If
     * translation fails (i.e. the given value is invalid or the property is not modifiable)
     * then this method does nothing.
     */
    void setCUFProperties (CUFProperty index, int value);

    /**
     * Check whether the value of the given {@link CUFProperty} is available.
     * @param index the {@link CUFProperty} to check.
     * @return true if a meaningful value is available, false if not.
     */
    boolean isAvailable (CUFProperty index);

    /**
     * Get the limit below which all char codes must fall (number of characters actually provided for in the CUF File).
     *
     * @param fromProperty whether to get the limit by reading from the property or by “manually” counting the characters.
     * @return the character limit of this CUF File
     */
    int getCharLimit (boolean fromProperty);

    /**
     * Get an {@link CUFGlyph} representing a glyph inside this font.
     * @param entry a valid entry found among those in {@link #getCharTable()}.
     * @return the {@link CUFGlyph} object that represents the given entry.
     */
    CUFGlyph getGlyph (C entry);

    /**
     * Get the dimension of a glyph in the CUF File.
     * This method works by reading from a pre-cached glyph dimension table returned by getGlyphDimensionTable() rather than reading from
     * the file.
     * This means that this method avoids doing a single small file read in favour of performing one (large) read of the entire table for faster best/average performance when used repeatedly.
     *
     * @param charCode entry of the glyph in the character table
     * @return object representing decoded dimension data
     */
    CUFGlyphDimension getGlyphDimension (C charCode);

    /**
     * Check if this {@link CUFSource} is prepared.
     * If this method returns false operations on this object are likely to result in
     * failure, or throw an {@link IllegalStateException} or an
     * {@link IllegalArgumentException}.
     * @return true if this {@link CUFSource} can be safely used, false if not.
     */
    boolean isPrepared ();

    /**
     * Maps a string to the indices of the glyph that would be used to render thi string.
     * <p>
     * While this method accepts any String input (including null), it will silently convert “wrong” input to either
     * the NULL character. In particular the NULL string is converted to the NULL character, and any character that does not
     * exist in the CUF font is converted to the NULL character as well.
     *
     * @param toMap the string to map to glyph indices
     * @return the indices of the glyphs associated with this string or 0's in case a character does not exist in the font
     */
    ArrayList<C> stringToCodes (String toMap);

    /**
     * Get the object that represents the mapping between input characters and
     * output {@link CharTableEntry} entries inside this {@link CUFSource}.
     * @return a {@link Map} object with {@link Character} keys and
     * objects that extend {@link CharTableEntry} as values.
     */
    Map<Character, C> getCharTable ();

    /**
     * 
     * Get a (short) descriptive string representation of this {@link CUFSource} object.
     * @return a string indentifying this {@link CUFSource} object
     */
    String getCufSource ();
    /**
     * Property name.
     * @see #getCufSource()
     */
    public static final String PROP_CUFSOURCE = "cufSource";
    /**
     * Property name.
     * @see #getCUFProperties()
     * @see #getCUFProperties(org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty)
     */
    public static final String PROP_CUFPROPERTIES = "CUFProperties";

    /**
     * Initialise this {@link CUFSource} object.
     * @param cufSource the (short) descriptive string to use when indentifying
     * this {@link CUFSource}.
     * @see #getCufSource()
     */
    void init (String cufSource);

    /**
     * Checks whether or not kerning is enabled for this {@link CUFSource}.
     * @return true if this font supports kerning, false if not.
     */
    public boolean kerningEnabled ();
}
