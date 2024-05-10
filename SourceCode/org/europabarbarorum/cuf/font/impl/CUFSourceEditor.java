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

import java.util.TreeMap;
import org.europabarbarorum.cuf.support.NotEditableException;

/**
 * An editor object which collects a delta (or diff) to apply per glyph for a given font.
 * @author Johan Ouwerkerk
 */
public class CUFSourceEditor {

    private TreeMap<Character, EditsImpl> editMap;
    private boolean kerning;

    /**
     * Creates a new {@link CUFSourceEditor}. By default it disables kerning.
     */
    public CUFSourceEditor () {
        this.kerning = false;
        this.editMap = null;
    }

    /**
     * Whether or not kerning is enabled.
     * @return true if the editor specifies kerning should be enabled, false if not.
     */
    public boolean kerningEnabled () {
        return kerning;
    }

    /**
     * Toggle kerning.
     * @param on true specifies kerning should be enabled, false that it should be disabled.
     */
    public void toggleKerning (boolean on) {
        boolean old = this.kerning;
        if (old != on && editMap != null) {
            uinstallKerning(on);
        }
        this.kerning = on;
    }

    private void uinstallKerning (boolean install) {
        if (install) {
            for (Character c : editMap.keySet()) {
                editMap.get(c).installKerning();
            }
        }
        else {
            for (Character c : editMap.keySet()) {
                editMap.get(c).uninstallKerning();
            }
        }
    }

    /**
     * Get a set of {@link Edits} which defines a delta for the glyph represented by the given character.
     * @param c the character which corresponds to the glyph to be edited.
     * @return the delta of {@link Edits} specified for the given character.
     */
    public Edits edit (Character c) {
        if (editMap == null) {
            editMap = new TreeMap<Character, EditsImpl>();
        }
        EditsImpl edits = editMap.get(c);
        if (edits == null) {
            edits = new EditsImpl();
            if (kerning) {
                edits.installKerning();
            }
            editMap.put(c, edits);
        }
        return edits;
    }

    /**
     * Checks if any changes have been made to the glyph corresponding to the given character.
     * @param c the character to check
     * @return true if changes have been made, false if not.
     */
    public boolean isEdited (Character c) {
        return editMap != null && editMap.containsKey(c);
    }

    /**
     * Get a readonly copy of the delta of changes made to the glyph corresponding to the given character.
     * @param c the character to lookup.
     * @return a {@link ReadOnlyEdits} delta of changes, or null if no changes have been made to the glyph yet.
     */
    public Edits getEdits (Character c) {
        if (editMap == null) {
            return null;
        }
        Edits v = editMap.get(c);
        return v == null ? null : new ReadOnlyEditsImpl(v);
    }

    /**
     * Undo any changes made via this {@link CUFSourceEditor} to the glyph corresponding to the
     * given character.
     * @param c the character for which any changes to its glyph are to be undone.
     */
    public void undoEdits (Character c) {
        if (editMap != null && editMap.containsKey(c)) {
            editMap.remove(c);
        }
    }

    /**
     * Undo all changes made to any glyphs via this {@link CUFSourceEditor}.
     */
    public void undoAllEdits () {
        editMap = null;
    }

    /**
     * Undo all changes made to kerning of any glyphs via this {@link CUFSourceEditor}.
     */
    public void undoAllKerning () {
        if (editMap != null) {
            for (Character c : editMap.keySet()) {
                editMap.get(c).undoKerning();
            }
        }
    }

    /**
     * An interface to describe a delta of changes which must be applied
     * for a particular glyph.
     * @see CUFSourceEditor#edit(java.lang.Character)
     */
    public interface Edits {

        /**
         * A default {@link Edits} instance which specifies an empty delta.
         */
        public static final Edits nullEdits = new NullEdits();

        /**
         * Lookup changes to the y-offset at which the top of the glyph is
         * rendered.
         * @return the new y-offset to use for positioning the glyph relative to
         * the baseline, or null if no changes have occurred.
         */
        Integer getY ();

        /**
         * Set the y-offset relative to the “baseline” at which the top of the glyph
         * should be positioned.
         * @param height the new y-offset to use. Use null to undo all previous
         * changes to the offset.
         */
        void setY (Integer height);

        /**
         * Lookup changes to the advance width of the glyph.
         * @return the changed advance width to use, or null if no changes occurred.
         */
        Integer getAdvanceWidth ();

        /**
         * Set the advance width to use for the glyph.
         * @param width new advance width to use. Use null to undo all
         * previous changes to the advance width.
         */
        void setAdvanceWidth (Integer width);

        /**
         * Lookup kerning information for this character when followed by the given character.
         * Kerning in CUF fonts works
         * in terms of character pairs; the first character is represented by this
         * {@link Edits} object itself.
         * @param c the second character of the pair.
         * @return the (kerned) advance width to use for this (first) character if it is
         * followed in rendered text by the given (second) character. Returns null if
         * no changes have been specified to the (default) kerning for the given pair.
         */
        Integer kerning (Character c);

        /**
         * Undo all kerning changes.
         */
        void undoKerning ();

        /**
         * Set kerning information for the given character. Kerning in CUF fonts works
         * in terms of character pairs; the first character is represented by this
         * {@link Edits} object itself.
         * @param c the second character of the pair.
         * @param width the (kerned) advance width to use for this (first) character if it is
         * followed in rendered text by the given (second) character. Use null to erase previously
         * set values for the given character.
         */
        void kern (Character c, Integer width);
        /**
         * Check is kerning support is requested.
         * @return true if kerning is supposed to be supported, false if not.
         */
        boolean supportsKerning();
    }

    /**
     * A type of {@link ReadOnlyEdits} which returns null for all read queries.
     */
    public static class NullEdits extends ReadOnlyEdits {

        @Override
        public Integer getY () {
            return null;
        }

        @Override
        public Integer getAdvanceWidth () {
            return null;
        }

        @Override
        public Integer kerning (Character c) {
            return null;
        }

        @Override
        public boolean supportsKerning () {
            return false;
        }

    }

    /**
     * A type of {@link ReadOnlyEdits} which reads through to a wrapped {@link Edits} delta.
     */
    public static class ReadOnlyEditsImpl extends ReadOnlyEdits {

        private final Edits impl;

        private ReadOnlyEditsImpl (Edits impl) {
            this.impl = impl;
        }

        @Override
        public Integer getY () {
            return impl.getY();
        }

        @Override
        public Integer getAdvanceWidth () {
            return impl.getAdvanceWidth();
        }

        @Override
        public Integer kerning (Character c) {
            return impl.kerning(c);
        }

        @Override
        public boolean supportsKerning () {
            return impl.supportsKerning();
        }

    }

    /**
     * A type of {@link Edits} for which only supports read operations on the delta.
     * Any attempt to write/modify the delta throws an {@link NotEditableException}.
     */
    public static abstract class ReadOnlyEdits implements Edits {

        @Override
        public void setY (Integer height) {
            throw new NotEditableException();
        }

        @Override
        public void setAdvanceWidth (Integer width) {
            throw new NotEditableException();
        }

        @Override
        public void kern (Character c, Integer width) {
            throw new NotEditableException();
        }

        @Override
        public void undoKerning () {
            throw new NotEditableException();
        }
    }

    /**
     * An {@link Edits} implementation which provides full support for all operations.
     */
    public class EditsImpl implements Edits {

        private Integer allocHeight = null, allocWidth = null;
        private TreeMap<Character, Integer> kerning = null;

        private void installKerning () {
            kerning = new TreeMap<Character, Integer>();
        }

        private void uninstallKerning () {
            kerning = null;
        }

        /**
         * Get the value of allocHeight
         *
         * @return the value of allocHeight
         */
        @Override
        public Integer getY () {
            return allocHeight;
        }

        /**
         * Set the value of allocHeight
         *
         * @param allocHeight new value of allocHeight
         */
        @Override
        public void setY (Integer allocHeight) {
            this.allocHeight = allocHeight;
        }

        /**
         * Get the value of allocWidth
         *
         * @return the value of allocWidth
         */
        @Override
        public Integer getAdvanceWidth () {
            return allocWidth;
        }

        /**
         * Set the value of allocWidth
         *
         * @param allocWidth new value of allocWidth
         */
        @Override
        public void setAdvanceWidth (Integer allocWidth) {
            this.allocWidth = allocWidth;
        }

        @Override
        public Integer kerning (Character c) {
            return kerning == null ? null : kerning.get(c);
        }

        @Override
        public void undoKerning () {
            if (kerning == null) {
                throw new KerningNotSupportedException();
            }
            kerning = new TreeMap<Character, Integer>();
        }

        @Override
        public void kern (Character c, Integer width) {
            if (kerning == null) {
                throw new KerningNotSupportedException();
            }
            if (width == null) {
                this.kerning.remove(c);
            }
            else {
                this.kerning.put(c, width);
            }
        }

        @Override
        public boolean supportsKerning () {
            return kerning!=null;
        }
    }

    /**
     * A type of {@link UnsupportedOperationException} throw when kerning has been
     * disabled.
     */
    class KerningNotSupportedException extends UnsupportedOperationException {
    }

    /**
     * An extension to {@link org.europabarbarorum.cuf.font.CUFSource CUFSource} which
     * introduces support for more declarative editing of glyphs.
     */
    public interface EditableSource {

        /**
         * Checks if this font supports editing.
         * @return true if an {@link CUFSourceEditor editor} has been installed, false if not.
         */
        public boolean editingEnabled ();

        /**
         * Gets the {@link CUFSourceEditor editor} installed on the font. If no editor has been
         * installed, implementations may throw an kind of {@link UnsupportedOperationException} to indicate this.
         * @return the {@link CUFSourceEditor editor} installed on the font. Implementations must not return null.
         */
        public CUFSourceEditor getEditor ();
    }
}
