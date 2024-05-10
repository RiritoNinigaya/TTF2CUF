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
package org.europabarbarorum.cuf.font.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.MacroSource;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension.CUFGlyphDimensionImpl;
import org.europabarbarorum.cuf.font.impl.CUFSourceEditor.EditableSource;
import org.europabarbarorum.cuf.font.impl.CUFSourceEditor.Edits;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.NotEditableException;

/**
 * Base implementation methods for a {@link CUFSource}.
 * @param <C>
 * @author Johan Ouwerkerk.
 */
public abstract class AbstractCUFSource<C> implements
        CUFSource<C>, EditableSource, MacroSource {

    /**
     * Create a new {@link AbstractCUFSource}.
     * The font will have support for editing and will claim intrinsic support for kerning.
     */
    public AbstractCUFSource () {
        this(new CUFSourceEditor(), true);
    }

    /**
     * Create a new {@link AbstractCUFSource}. The font will have support for editing.
     * @param b whether or not the font should claim intrinsic support for kerning.
     */
    protected AbstractCUFSource (boolean b) {
        this(new CUFSourceEditor(), b);
    }

    /**
     * Create a new {@link AbstractCUFSource}.
     * @param editor an {@link CUFSourceEditor} to provide support for editing or null to disable editing.
     * @param b whether or not the font should claim intrinsic support for kerning.
     */
    protected AbstractCUFSource (CUFSourceEditor editor, boolean b) {
        this.editingSupport = editor;
        this.supportsKerning = b;
    }
    /**
     * String describing the actual source of data (file name or {@link java.awt.Font#decode} string).
     */
    protected String cufSource;

    @Override
    public boolean kerningEnabled () {
        return editingEnabled() ? editingSupport.kerningEnabled() : supportsKerning();
    }
    private final boolean supportsKerning;
    private String macroName;

    /**
     * Set a name (macro) for this font. Complementing method to {@link #getMacroName() }.
     * This method throws an {@link IllegalArgumentException} if the given name is not null
     * and not a {@link IOHelp#isValidXMLName(java.lang.String) valid XML name}.
     * @param macroName name of the Macro which is to correspond to this font. Use null to remove
     * previously set names.
     */
    public final void setMacroName (String macroName) {
        if (macroName == null || IOHelp.isValidXMLName(macroName)) {
            this.macroName = macroName;
        }
        else {
            throw new IllegalArgumentException(macroName);
        }
    }

    @Override
    public String getMacroName () {
        return this.macroName;
    }

    /**
     * Checks whether or not this {@link AbstractCUFSource} intrinsically supports kerning.
     * This can be overridden using the {@link #getEditor() editor} of this font.
     * @return true if the font supports kerning by default, false if not.
     */
    public boolean supportsKerning () {
        return supportsKerning;
    }

    @Override
    public boolean editingEnabled () {
        return editingSupport != null;
    }

    /**
     * Get the value of {@link #cufSource}
     *
     * @return the value of {@link #cufSource}
     */
    @Override
    public String getCufSource () {
        return cufSource;
    }

    /**
     * Default implementation of {@link CUFSource#trackCharacter(java.lang.Character) }.
     * Fonts that wrap or map chartables should override this method with their own implementation.
     * @param c character to track
     * @return the given character as-is.
     */
    @Override
    public Character trackCharacter (Character c) {
        return c;
    }
    private final CUFSourceEditor editingSupport;

    /**
     * Convenience version to access {@link CUFSourceEditor#edit(java.lang.Character) }.
     * If editing is not enabled this method will throw a {@link NotEditableException}.
     * @param c the character for which the glyph is to be edited.
     * @return a {@link Edits} delta to specify changes to the original.
     * @see #editingEnabled()
     */
    public Edits edit (Character c) {
        return getEditor().edit(c);
    }

    /**
     * Convenience version to access {@link CUFSourceEditor#edit(java.lang.Character) }.
     * @param c the character for which changes to the default glyph should be retrieved.
     * @return a {@link Edits} delta with specified changes to the original, or null if
     * no changes have occurred or editing is not supported.
     * @see #editingEnabled()
     */
    public Edits getEdits (Character c) {
        if (editingEnabled()) {
            return getEditor().getEdits(c);
        }
        return null;
    }

    @Override
    public CUFSourceEditor getEditor () {
        if (editingEnabled()) {
            return editingSupport;
        }
        throw new NotEditableException();
    }

    /**
     * Get some quick stats about a CUF Font encoded in dimensions
     * @return an array containing:
     * <ol>
     * <li>the maximum dimensions found</li>
     * <li>the minimum dimensions found</li>
     * <li>the average dimensions found</li>
     * <li>the dimensions of the largest glyph</li>
     * <li>the dimensions of the smallest glyph</li>
     * </ol>
     */
    public CUFGlyphDimension[] getDimensionStats () {
        CUFGlyphDimension cur = null, min = null, max = null;
        int[] maxs = new int[4], mins = new int[4], avgs = new int[] { 0,
                                                                       0,
                                                                       0,
                                                                       0 };
        int i = 0;
        Collection<C> entrySet = getCharTable().values();
        for (C entry : entrySet) {
            cur = getGlyphDimension(entry);
            avgs[0] += cur.getWidth();
            avgs[1] += cur.getHeight();
            avgs[2] += cur.getAdvanceWith();
            avgs[3] += cur.getY();
            if (i == 0) {
                mins = new int[] {
                            cur.getWidth(),
                            cur.getHeight(),
                            cur.getAdvanceWith(),
                            cur.getY(), };
                min = cur;
                max = cur;
            }
            if (cur.getY() > maxs[3]) {
                maxs[3] = cur.getY();
            }
            if (cur.getY() < mins[3]) {
                mins[3] = cur.getY();
            }
            if (cur.getAdvanceWith() > maxs[2]) {
                maxs[2] = cur.getAdvanceWith();
            }
            if (cur.getAdvanceWith() < mins[2]) {
                mins[2] = cur.getAdvanceWith();
            }
            if (cur.getHeight() > maxs[1]) {
                maxs[1] = cur.getHeight();
            }
            if (cur.getHeight() < mins[1]) {
                mins[1] = cur.getHeight();
            }
            if (cur.getWidth() > maxs[0]) {
                maxs[0] = cur.getWidth();
            }
            else {
                if (cur.getWidth() < mins[0]) {
                    mins[0] = cur.getWidth();
                }
            }
            if (cur.getSize() > max.getSize()) {
                max = cur;
            }
            if (cur.getSize() < min.getSize()) {
                min = cur;
            }
            ++i;
        }
        return new CUFGlyphDimension[] {
                    new CUFGlyphDimensionImpl(maxs[0], maxs[1], maxs[2], maxs[3]),
                    new CUFGlyphDimensionImpl(mins[0], mins[1], mins[2], mins[3]),
                    new CUFGlyphDimensionImpl(avgs[0] / i, avgs[1] / i,
                                              avgs[2] / i,
                                              avgs[3] / i),
                    max,
                    min
                };
    }
    /**
     * Character table providing mapping between character bytes (encoded as Integers) and glyph codes used by this font.
     */
    protected SortedMap<Character, C> charTable;
    /**
     * Flag to indicate that the font source is “ready” to be used by other objects.
     */
    protected boolean prepared = false;

    /**
     * Get the value of {@link #prepared}
     *
     * @return the value of {@link #prepared}
     */
    @Override
    public boolean isPrepared () {
        return prepared;
    }
    /**
     * Global font properties associated of this CUF Source; corresponding to these property values in a CUF file.
     */
    protected int[] CUFProperties = cleanProps();

    /**
     * Fills an array the size of {@link #CUFProperties} with the marker {@link #PROP_UNAVAILABLE}.
     * @return an array of blank CUF properties.
     */
    protected int[] cleanProps () {
        int[] ps = new int[FormatConstants.NumCUFProps.value()];
        Arrays.fill(ps, PROP_UNAVAILABLE);
        return ps;
    }

    @Override
    public int[] getCUFProperties () {
        int[] copy = new int[CUFProperties.length];
        for (int i = 0; i < CUFProperties.length; ++i) {
            copy[i] = CUFProperties[i];
        }
        return copy;
    }

    @Override
    public int getCUFProperties (CUFProperty index) {
        return index.translator.getTranslate(this.CUFProperties[index.index()]);
    }

    @Override
    public void setCUFProperties (CUFProperty index, int value) {
        Integer k = index.translator.setTranslate(value);
        if (index.isModifiable() && k != null) {
            this.CUFProperties[index.index()] = k;
        }
    }

    /**
     * Unrestricted version of {@link #setCUFProperties(org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty, int) }.
     * @param index the CUFProperty to edit
     * @param value the new value to use.
     */
    protected void setCUFPropertiesInternal (CUFProperty index, int value) {
        this.CUFProperties[index.index()] = value;
    }

    @Override
    public int getCharLimit (boolean fromProperty) {
        if (fromProperty) {
            return getCUFProperties(CUFProperty.NumberOfGlyphs);
        }
        else {
            return getCharTable().size();
        }
    }

    /**
     * (Re-) Builds the {@link #charTable}
     */
    protected abstract void deriveCharTable ();

    /**
     * Lazily creates and initialises a chartable for this {@link AbstractCUFSource}.
     * Note that subclasses must implement {@link #deriveCharTable() } to create a
     * {@link SortedMap} and initialise it with the chartable mappings.
     * <p>The benefit of usig a {@link SortedMap} is that the chartable will have implied order,
     * which is useful for code/scripts that want to manipulate it as block/ordered sequence.
     * @return the chartable.
     * @see CUFSource#getCharTable() 
     */
    @Override
    public SortedMap<Character, C> getCharTable () {
        if (charTable == null) {
            deriveCharTable();
        }
        return charTable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<C> stringToCodes (String toMap) {
        String chrs = toMap == null ? "" : toMap;
        int l = chrs.length();
        ArrayList<C> entries = new ArrayList<C>(l);
        Map<Character, C> table = getCharTable();
        for (int i = 0; i < l; ++i) {
            char c = toMap.charAt(i);
            entries.add(i, table.get(c));
        }
        return entries;
    }
    /**
     * Placeholder value to signify that a particular property is not known/available.
     */
    protected static int PROP_UNAVAILABLE = -1;

    @Override
    public boolean isAvailable (CUFProperty index) {
        return CUFProperties[index.index()] != PROP_UNAVAILABLE;
    }

    @Override
    public CUFSource backTrack (Character c) {
        if (getCharTable().containsKey(c)) {
            return this;
        }
        else {
            return null;
        }
    }
}
