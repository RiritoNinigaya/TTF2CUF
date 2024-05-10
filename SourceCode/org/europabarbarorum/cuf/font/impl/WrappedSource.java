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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.Messages;
import org.europabarbarorum.cuf.font.TopLevelSource;
import org.europabarbarorum.cuf.font.impl.AbstractCUFSource;
import org.europabarbarorum.cuf.font.impl.CUFGlyph;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension.CUFGlyphDimensionImpl;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension.MergedDimension;
import org.europabarbarorum.cuf.font.impl.CUFSourceEditor;
import org.europabarbarorum.cuf.font.impl.CUFSourceEditor.EditableSource;
import org.europabarbarorum.cuf.font.impl.CUFSourceEditor.Edits;
import org.europabarbarorum.cuf.font.impl.CharTableEntry;
import org.europabarbarorum.cuf.font.impl.FormatConstants;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.strings.impl.ControlCharacter;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.IllegalMethodCallException;
import org.europabarbarorum.cuf.support.NotEditableException;

/**
 * This class provides a base implementation of {@link TopLevelSource} with a single wrapped
 * {@link CUFSource}. The idea is that the chartable of this font is a modified version of that of the
 * wrapped font.
 * @param <C> type of object bound to characters in the chartable of the wrapped source font.
 * @param <I> the type of {@link CharTableEntry} to use.
 * @author Johan Ouwerkerk
 */
public abstract class WrappedSource<C, I> extends AbstractCUFSource<I> implements
        TopLevelSource<C> {

    /**
     * A {@link WrappedSource} which remaps characters from the chartable of a
     * wrapped {@link CUFSource}.
     * <p>The difference with a {@link AlgorithmSource} is that this class
     * operates on individual mappings in the chartable,
     * whereas {@link AlgorithmSource} operates on the chartable as a block
     * which may be arbitrarily manipulated.
     * More specifically: remapping an entry in the chartable should not affect
     * other entries at all. By contrast remapping an entry in {@link AlgorithmSource}
     * may imply or cause remapping of other entries as well.</p>
     * @param <C> the type of entries stored the chartable of the wrapped {@link CUFSource font}.
     */
    public abstract static class CharSource<C> extends WrappedSource<C, Character> {


        /**
         * Create a new {@link CharSource}.
         * @param toWrap the {@link CUFSource font} to wrap.
         */
        public CharSource (CUFSource<C> toWrap) {
            super(toWrap);
        }

        @Override
        protected Character reMap (Character entry) {
            return entry;
        }

        @Override
        protected boolean resetDataSize () {
            return true;
        }
    }

    /**
     * A {@link CharSource} which remaps the entire chartable of a
     * wrapped {@link CUFSource}.
     * <p>The difference with a {@link CharSource} is that this class
     * operates on the chartable as a block which may be arbitrarily manipulated,
     * whereas {@link CharSource} operates on individual mappings in the chartable.
     * More specifically: remapping an entry in the chartable may imply or cause
     * remapping of other entries as well. By contrast remapping an entry in {@link CharSource}
     * should not affect other entries at all.</p>
     * @param <C> the type of entries stored the chartable of the wrapped {@link CUFSource font}.
     */
    public abstract static class AlgorithmSource<C> extends CharSource<C> {

        /**
         * Create a new {@link AlgorithmSource}.
         * @param toWrap the {@link CUFSource font} to wrap.
         */
        public AlgorithmSource (CUFSource<C> toWrap) {
            super(toWrap);
        }

        /**
         * Attempt to append left over characters. This method is useful for relocating
         * characters which had to make way for others in the main logic of the algorithm.
         * @param set the {@link Set} of all characters to relocate
         * @param t a {@link Map} reflecting the current state of the chartable.
         */
        protected void appendTodo (Set<Character> set,
                                   Map<Character, Character> t) {
            if (set.size() > 0) {
                Iterator<Character> free = free(t.keySet()).iterator(),
                        iter = set.iterator();
                while (free.hasNext() && iter.hasNext()) {
                    t.put(free.next(), iter.next());
                    free.remove();
                    iter.remove();
                }
                if (iter.hasNext()) {
                    IOHelp.warn(AlgorithmSource.class,
                                Messages.LeftOverChars,
                                wrapped.getCufSource());
                }
            }
        }

        @Override
        protected void deriveCharTable () {
            charTable = chartable();
        }

        private Set<Character> free (Set<Character> set) {
            Set<Character> free = new HashSet<Character>();
            Character c;
            for (int i = 0, l = FormatConstants.__LIMIT__.value(); i < l; ++i) {
                c = IOHelp.fromCode(i);
                if (!set.contains(c)) {
                    free.add(c);
                }
            }
            return free;
        }

        @Override
        protected void deriveEntry (Character key, C value) {
            throw new IllegalMethodCallException();
        }

        /**
         * Generate the chartable. This method runs the
         * remapping algorithm of this {@link AlgorithmSource} on the chartable
         * of the wrapped {@link CUFSource font} and returns the result.
         * @return the result of remapping the chartable of the wrapped
         * {@link CUFSource font}.
         */
        protected abstract TreeMap<Character, Character> chartable ();
    }

    /**
     * An {@link AlgorithmSource} which substitutes
     * dummy glyphs for {@link ControlCharacter control characters}, and removes
     * control character mappings assigned in the chartable of a {@link CUFSource}.
     * @param <C> the type of entries stored the chartable of the wrapped {@link CUFSource font}.
     */
    public static class ControlSource<C> extends AlgorithmSource<C> {

        /**
         * Create a new {@link ControlSource}.
         * @param toWrap the {@link CUFSource font} to wrap.
         */
        public ControlSource (CUFSource<C> toWrap) {
            super(toWrap);
        }

        @Override
        protected TreeMap<Character, Character> chartable () {
            TreeMap<Character, Character> table = new TreeMap<Character, Character>();
            HashSet<Character> todo = new HashSet<Character>();
            for (Character c : wrapped.getCharTable().keySet()) {
                if (include(c)) {
                    table.put(c, c);
                    if(!wrapped(c)) { todo.add(c); }
                }
                else {
                    if(!wrapped(c)) { table.put(c,c); }
                }
            }
            appendTodo(todo, table);
            return table;
        }

        @Override
        public CUFSource backTrack (Character c) {
            return wrapped(c) ? super.backTrack(c) : this;
        }

        @Override
        public Character trackCharacter (Character c) {
            return wrapped(c) ? super.trackCharacter(c) : c;
        }

        @Override
        public Edits getEdits (Character c) {
            return wrapped(c) ? super.getEdits(c) : null;
        }

        @Override
        public CUFGlyph getGlyph (Character entry) {
            return wrapped(reMap(entry))
                    ? super.getGlyph(entry)
                    : new CUFGlyph(dim(), new byte[] { 0x00 });
        }

        private CUFGlyphDimension dim () {
            return new CUFGlyphDimensionImpl(1, 1, 1, 1, null, null);
        }

        @Override
        public CUFGlyphDimension getGlyphDimension (Character charCode) {
            return wrapped(reMap(charCode)) ? super.getGlyphDimension(charCode) : dim();
        }

        private boolean include (Character c) {
            return wrapped(wrapped.trackCharacter(c));
        }

        private boolean wrapped (Character c) {
            ControlCharacter cc = ControlCharacter.forMacroCharacter(c);
            return cc == null || !cc.fixed;
        }
    }

    /**
     * A {@link AlgorithmSource} which remaps the chartable of a wrapped
     * {@link CUFSource font} in such a way that a subportion of it is
     * compatible with programs that are unaware of the hierarchy of the font.
     * @param <C> the type of entries stored the chartable of the wrapped {@link CUFSource font}.
     */
    public static class CompatibleSource<C> extends AlgorithmSource<C> {

        private final CUFSource<?> compat;

        /**
         * Create a new {@link CompatibleSource}.
         * @param toWrap the {@link CUFSource font} to wrap.
         * @param src the {@link CUFSource} which provides the glyphs for the
         * compatible part of the given {@link CUFSource font} toWrap.
         */
        public CompatibleSource (CUFSource<C> toWrap, CUFSource<?> src) {
            super(toWrap);
            this.compat = src;
        }

        @Override
        protected TreeMap<Character, Character> chartable () {
            TreeMap<Character, Character> table = new TreeMap<Character, Character>();
            HashSet<Character> done = new HashSet<Character>();
            Set<Character> set = wrapped.getCharTable().keySet();
            HashSet<Character> todo = new HashSet<Character>();
            Character k;
            for (Character c : set) {
                if (compat.equals(wrapped.backTrack(c))) {
                    k = wrapped.trackCharacter(c);
                    table.put(k, c);
                    done.add(c);
                }
            }
            for (Character c : set) {
                if (!done.contains(c)) {
                    if (table.containsKey(c)) {
                        todo.add(c);
                    }
                    else {
                        table.put(c, c);
                    }
                }
            }
            appendTodo(todo, table);
            return table;
        }
    }

    /**
     * A kind of {@link ReadThroughSource} to bind all underlying {@link CUFSource} objects to
     * a single macro name. Note that it is up to the underlying {@link CUFSource font} to ensure
     * that no two input characters yield the same output character when {@link #trackCharacter(java.lang.Character) tracked}.
     * @param <C> type of entry used in the chartable of the wrapped {@link CUFSource}.
     */
    public static class MacroSource<C> extends ReadThroughSource<C> {

        /**
         * Create a new {@link MacroSource}.
         * @param macro the {@link #setMacroName(java.lang.String) macro name to use}.
         * @param name the {@link #init(java.lang.String) font name} to use.
         * @param toWrap the {@link CUFSource font} to wrap.
         */
        public MacroSource (String macro, String name, CUFSource<C> toWrap) {
            super(name, toWrap);
            setMacroName(macro);
        }

        @Override
        public CUFSource backTrack (Character c) {
            return this;
        }
    }

    /**
     * A kind of {@link WrappedSource} which does not merge edit deltas, but
     * rather reads through to the wrapped {@link CUFSourceEditor} if available.
     * @param <C> type of entry used in the chartable of the wrapped {@link CUFSource}.
     */
    public static class ReadThroughSource<C> extends WrappedSource {

        /**
         * Create a new {@link ReadThroughSource}.
         * @param name the {@link #init(java.lang.String) font name} to use.
         * @param toWrap the {@link CUFSource font} to wrap.
         */
        @SuppressWarnings("unchecked")
        public ReadThroughSource (String name, CUFSource<C> toWrap) {
            super(toWrap);
            init(name);
        }

        @Override
        @SuppressWarnings("unchecked")
        public CUFGlyphDimension getGlyphDimension (Object charCode) {
            return wrapped.getGlyphDimension(charCode);
        }

        @Override
        @SuppressWarnings("unchecked")
        public CUFGlyph getGlyph (Object entry) {
            return wrapped.getGlyph(entry);
        }

        private EditableSource ed () {
            if (editingEnabled()) {
                return (EditableSource) wrapped;
            }
            throw new NotEditableException();
        }

        @Override
        public CUFSourceEditor getEditor () {
            return ed().getEditor();
        }

        @Override
        public boolean editingEnabled () {
            if (wrapped instanceof EditableSource) {
                return ((EditableSource) wrapped).editingEnabled();
            }
            return false;
        }

        @Override
        public boolean kerningEnabled () {
            return wrapped.kerningEnabled();
        }

        @Override
        protected Character reMap (Object entry) {
            throw new IllegalMethodCallException();
        }

        @Override
        protected void deriveEntry (Character key, Object value) {
            throw new IllegalMethodCallException();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void deriveCharTable () {
            Map<Character, C> map = wrapped.getCharTable();
            if (map instanceof SortedMap) {
                this.charTable = (SortedMap<Character, C>) map;
            }
            else {
                this.charTable = new TreeMap<Character, C>(map);
            }
        }

        @Override
        public CUFSource backTrack (Character c) {
            return wrapped.backTrack(c);
        }

        @Override
        public Character trackCharacter (Character c) {
            return wrapped.trackCharacter(c);
        }
    }
    /**
     * The {@link CUFSource} that is wrapped in this font.
     */
    protected final CUFSource<C> wrapped;

    /**
     * Create a wrapped source.
     * @param toWrap the {@link CUFSource} to wrap.
     */
    public WrappedSource (CUFSource<C> toWrap) {
        super(false);
        if (!toWrap.isPrepared()) {
            throw new IllegalStateException(
                    Messages.WrappedInvalidError.format(toWrap.getCufSource()));
        }
        this.wrapped = toWrap;

    }

    @Override
    public CUFGlyph getGlyph (I e) {
        if (e == null) {
            return null;
        }
        CUFGlyph impl = wrapped.getGlyph(wrapped.getCharTable().get(reMap(e)));
        if (impl == null) {
            return null;
        }
        return merge(impl, e);
    }

    private CUFGlyph merge (CUFGlyph glyph, I code) {
        CUFGlyphDimension dim = glyph.getDimension();
        return new CUFGlyph(merge(dim, code), glyph.getBitMapData());
    }

    private MergedDimension merge (CUFGlyphDimension dim, I code) {
        return new MergedDimension(dim, getEdits(reMap(code)));
    }

    @Override
    public CUFGlyphDimension getGlyphDimension (I cc) {
        if (cc == null) {
            return null;
        }
        CUFGlyphDimension d =
                wrapped.getGlyphDimension(wrapped.getCharTable().get(reMap(cc)));
        if (d == null) {
            return null;
        }
        return merge(d, cc);
    }

    /**
     * Maps a chartable entry back to the character of the wrapped {@link AbstractCUFSource} that
     * supplied it.
     * @param entry the chartable entry to map.
     * @return the character it corresponds to in the wrapped {@link AbstractCUFSource}.
     */
    protected abstract Character reMap (I entry);

    /**
     * Auxiliary method for a {@link WrappedSource} to add entries.
     * @param key the character key to add
     * @param derived the chartable entry to add
     */
    protected void addEntry (Character key, I derived) {
        charTable.put(key, derived);
    }

    @Override
    public void init (String cufSource) {
        deriveCharTable();
        this.cufSource = cufSource;
        this.CUFProperties = wrapped.getCUFProperties();

        // short cut: NumberOfGlyphs does not need translation
        setCUFPropertiesInternal(CUFProperty.NumberOfGlyphs, getCharLimit(false));
        if (resetDataSize()) {
            setCUFPropertiesInternal(CUFProperty.GlyphDataSize, PROP_UNAVAILABLE);
        }
        prepared = true;
    }

    /**
     * Hook for subclasses to specify whether or not the
     * {@link CUFProperty#GlyphDataSize glyph data size}
     * property should be reset.
     * Subclasses should override this method if they discard
     * part of their wrapped {@link CUFSource}.
     * @return <code>false</code> by default.
     */
    protected boolean resetDataSize () {
        return false;
    }

    @Override
    protected void deriveCharTable () {
        charTable = new TreeMap<Character, I>();
        Map<Character, C> table = wrapped.getCharTable();
        for (Entry<Character, C> entry : table.entrySet()) {
            deriveEntry(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Derive a chartable entry according to the logic of this {@link WrappedSource}.
     * The implementation is responsible for storing its derived entry in {@link #charTable}.
     * @param key the original key obtained from the wrapped {@link CUFSource}.
     * @param value the original value obtained from the {@link CUFSource}.
     */
    abstract protected void deriveEntry (Character key, C value);

    @Override
    @SuppressWarnings("unchecked")
    final public CUFSource<C>[] getSources () {
        return new CUFSource[] { wrapped };
    }

    /**
     * Default implementation: defers to the wrapped {@link CUFSource}.
     * Subclasses which map chartables should override this method.
     * @param c the character to track.
     * @return the result returned by the wrapped font.
     */
    @Override
    public CUFSource backTrack (Character c) {
        if (charTable.containsKey(c)) {
            return wrapped.backTrack(reMap(getCharTable().get(c)));
        }
        else {
            return null;
        }
    }

    /**
     * Default implemention: defers to the wrapped {@link CUFSource}.
     * Subclasses the map chartables should override this method.
     * @param c the character to track.
     * @return the result returned by the wrapped font.
     */
    @Override
    public Character trackCharacter (Character c) {
        return wrapped.trackCharacter(reMap(getCharTable().get(c)));
    }
}
