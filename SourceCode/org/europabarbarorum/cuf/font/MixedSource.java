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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.europabarbarorum.cuf.font.impl.AbstractCUFSource;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.font.MixedSource.MixerCharTableEntry;

/**
 * This class provides a “read-only” view of multiple {@link CUFSource} objects combined into a single font.
 * @author Johan Ouwerkerk
 */
public class MixedSource extends AbstractCUFSource<MixerCharTableEntry>
        implements TopLevelSource<MixerCharTableEntry> {

    /**
     * Create a {@link MixedSource}. Arguments should be of equal lenght, and may not contain nulls. 
     * If the arguments are not of equal length an {@link IllegalArgumentException} will be thrown. If 
     * an argument is null 
     * @param sources an array of {@link CUFSource} sources to combine into a single font. All fonts should 
     * be prepared, according to their {@link #isPrepared() } methods.
     * @param modes an array of {@link MixinMode} flags, one per {@link CUFSource} to determine if a {@link CUFSource} 
     * may overwrite chartable entries added to the combined font prior to its own.
     */
    public MixedSource (CUFSource[] sources, MixinMode[] modes) {
        if (sources.length != modes.length) {
            throw new IllegalArgumentException(
                    Messages.MixedListsLengthError.format(sources.length,
                                                          modes.length));
        }
        mixedFonts = new HashMap<String, Mixed>(sources.length);
        int i = 0;
        for (CUFSource src : sources) {
            if (src == null) {
                throw new NullPointerException(
                        Messages.MixedArgumentNullError.format(
                        i,
                        CUFSource.class.getCanonicalName()));
            }
            if (modes[i] == null) {
                throw new NullPointerException(
                        Messages.MixedArgumentNullError.format(
                        i,
                        MixinMode.class.getCanonicalName()));
            }
            addFont(src, modes[i]);
            ++i;
        }
    }
    private HashMap<String, Mixed> mixedFonts;

    private void checkPrepared (CUFSource source) {
        if (!source.isPrepared()) {
            throw new IllegalStateException(
                    Messages.MixedInvalidError.format(source.getCufSource()));
        }
    }

    private void initProps () {
        for (CUFSource cs : getSources()) {
            int[] canprops = cs.getCUFProperties();
            for (int i = 0; i < canprops.length; ++i) {
                if (CUFProperties[i] < canprops[i]) {
                    CUFProperties[i] = canprops[i];
                }
            }
        }

        this.setCUFPropertiesInternal(CUFProperty.NumberOfGlyphs,
                                      getCharLimit(false));
        this.setCUFPropertiesInternal(CUFProperty.GlyphDataSize,
                                      PROP_UNAVAILABLE);
    }

    /**
     * Add another font to the mix of various {@link CUFSource} objects in use by this {@link MixedSource}.
     * This method merely performs the sanity checks: it requires a regeneration of the chartable through {@link #deriveCharTable()}
     * for the result to show up.
     * @param source the {@link CUFSource} to add.
     * @param mode some {@link MixinMode} that informs this method of what to do in case a character in the
     * source to add is already claimed by another font in this {@link MixedSource}.
     */
    protected final void addFont (final CUFSource source, MixinMode mode) {
        checkPrepared(source);
        String src = source.getCufSource();
        if (mixedFonts.containsKey(src)) {
            throw new IllegalArgumentException(
                    Messages.MixedContainsError.format(source));
        }
        mixedFonts.put(src, new Mixed(source, mode));
    }

    /**
     * Low level implementation of {@link #addFont(org.europabarbarorum.cuf.font.CUFSource, org.europabarbarorum.cuf.font.MixedSource.MixinMode) }
     * without the sanity checks.
     * @param source the {@link CUFSource} to add.
     * @param mode the {@link MixinMode} to use.
     * @see #addFont(org.europabarbarorum.cuf.font.CUFSource, org.europabarbarorum.cuf.font.MixedSource.MixinMode)
     */
    @SuppressWarnings("unchecked")
    protected void addSource (final CUFSource source, MixinMode mode) {

        Map<Character, CharTableEntry> _source = source.getCharTable();
        Character key;
        for (Entry<Character, CharTableEntry> _entry : _source.entrySet()) {
            key = _entry.getKey();
            if (charTable.containsKey(key)) {
                if (MixinMode.Remove == mode) {
                    charTable.put(key, new MixerCharTableEntry(source, key));
                }
            }
            else {
                charTable.put(key, new MixerCharTableEntry(source, key));
            }
        }
        this.setCUFPropertiesInternal(CUFProperty.NumberOfGlyphs,
                                      getCharLimit(false));
    }

    @Override
    protected void deriveCharTable () {
        charTable = new TreeMap<Character, MixerCharTableEntry>();
        for (Entry<String, Mixed> entry : mixedFonts.entrySet()) {
            addSource(entry.getValue().src, entry.getValue().mix);
        }
    }

    @Override
    public void init (String cufSource) {
        this.cufSource = cufSource;
        deriveCharTable();
        initProps();
        this.prepared = true;
    }

    @Override
    public boolean isPrepared () {
        return prepared;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CUFGlyph getGlyph (MixerCharTableEntry entry) {
        if (entry == null) {
            return null;
        }
        return entry.source.getGlyph(entry.source.getCharTable().get(
                entry.character));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CUFGlyphDimension getGlyphDimension (MixerCharTableEntry entry) {
        if (entry == null) {
            return null;
        }
        return entry.source.getGlyphDimension(entry.source.getCharTable().get(
                entry.character));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CUFSource<MixerCharTableEntry>[] getSources () {
        Mixed[] mixed = mixedFonts.values().toArray(new Mixed[] {});
        CUFSource[] srcs = new CUFSource[mixed.length];
        for (int i = 0; i < mixed.length; ++i) {
            srcs[i] = mixed[i].src;
        }
        return srcs;
    }

    @Override
    public CUFSource backTrack (Character c) {
        Map<Character, MixerCharTableEntry> table = getCharTable();
        if (table.containsKey(c)) {
            MixerCharTableEntry m = table.get(c);
            return m.source.backTrack(m.character);
        }
        else {
            return null;
        }
    }

    @Override
    public Character trackCharacter (Character c) {
        Map<Character, MixerCharTableEntry> table = getCharTable();
        if (table.containsKey(c)) {
            MixerCharTableEntry m = table.get(c);
            return m.source.trackCharacter(m.character);
        }
        else {
            return null;
        }
    }

    /**
     * A kind a chartable entry to keep track of a character and the {@link CUFSource} that supplied it.
     */
    public class MixerCharTableEntry {

        private final CUFSource source;
        private final Character character;

        /**
         * Create a {@link MixerCharTableEntry}.
         * @param source the {@link CUFSource} that supplied this entry.
         * @param chr the character to which this entry corresponds.
         */
        public MixerCharTableEntry (CUFSource source, char chr) {
            this.character = chr;
            this.source = source;
        }
    }

    /**
     * Setting indicating per mixed {@link CUFSource} how characters should be treated that are already
     * claimed by other fonts (added earlier) in the {@link MixedSource}.
     */
    public static enum MixinMode {

        /**
         * Replace older entries with newer ones.
         */
        Remove,
        /**
         * Favour older entries to newer ones.
         */
        Keep;
    }
}

class Mixed {

    protected final CUFSource src;
    protected final MixedSource.MixinMode mix;

    public Mixed (CUFSource src, MixedSource.MixinMode mix) {
        this.src = src;
        this.mix = mix;
    }
}
