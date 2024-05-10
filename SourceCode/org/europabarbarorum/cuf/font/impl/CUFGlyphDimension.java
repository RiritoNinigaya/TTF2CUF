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

import org.europabarbarorum.cuf.font.impl.CUFSourceEditor.Edits;

/**
 * This interface represent dimension parameters for a {@link CUFGlyph}.
 * It contains information on its {@link #getWidth() width}, {@link #getHeight() height}, {@link #getSize() size}; as well as
 * layout information in terms of its {@link #getY() baseline} and its
 * {@link #getAdvanceWith() width when laid out}.
 * @author Johan Ouwerkerk
 */
public interface CUFGlyphDimension {

    /**
     * Get the y coordinate relative to the baseline of the CUF font at which the glyph should
     * be positioned. To position a glyph on an image with an origin of (0,0) in the top left corner, use:
     * {@code y = baseline - dim.getY()}.
     * @return y coordinate relative to the baseline of a font at which the glyph should be positioned,
     * assuming an origin of (0,0) in bottom left corner.
     * Not necessarily the same as the actual {@link #getHeight() height} of alpha mask of this glyph.
     */
    public int getY ();

    /**
     * This method attempts to perform kerning with respect to the given
     * character. If no kerning information is available (or if kerning is not supported) it will return the
     * {@link #getAdvanceWith() advance width} of this {@link CUFGlyphDimension}.
     * <p>
     * Kerning in CUF fonts works in terms of character pairs, where this
     * {@link CUFGlyphDimension} represents the first character and the given character
     * corresponds to the following character.
     * @param c the character with respect to which
     * the current {@link CUFGlyphDimension} should be kerned.
     * @return the amount of space along the x-axis which should be reserved for the
     * this glyph.
     */
    public int kerning (Character c);

    /**
     * Get the <em>advance width</em> which should be used for this glyph, except
     * when kerning is applied. Use {@link #kerning(java.lang.Character) } when
     * performing text layout instead since it takes scenarios into account.
     * @return the advance along the x-axis which should be reserved for this glyph.
     * Note that it is not necessarily the same as the actual {@link #getWidth() width}
     * of the alpha mask of this glyph.
     */
    public int getAdvanceWith ();

    /**
     * Get the height of this glyph.
     * @return the height of the alpha mask which is used to render this glyph.
     */
    public int getHeight ();

    /**
     * Get the width of this glyph.
     * @return the width of the alpha mask which is used to render this glyph.
     */
    public int getWidth ();

    /**
     * Get the size of this glyph.
     * It is a shorthand for {@link #getHeight() } × {@link #getWidth() }.
     * @return the size of this glyph.
     */
    public int getSize ();

    /**
     * A class which implements a merging mechanism for combining a
     * {@link CUFGlyphDimension} from one source with a delta of {@link Edits} from another.
     */
    public static class MergedDimension implements CUFGlyphDimension {

        private final CUFGlyphDimension merged;
        private final Edits edits;

        /**
         * Create a new {@link MergedDimension} without any {@link Edits} merged in.
         * The resulting {@link CUFGlyphDimension} is essentially wrapped.
         * @param dim a {@link CUFGlyphDimension} to merge.
         */
        public MergedDimension (CUFGlyphDimension dim) {
            this(dim, null);
        }

        /**
         * Create a new {@link MergedDimension} with the given delta of {@link Edits} merged in.
         * @param dim a {@link CUFGlyphDimension} to merge.
         * @param edits a delta of {@link Edits} to merge with the given {@link CUFGlyphDimension}.
         */
        public MergedDimension (CUFGlyphDimension dim, Edits edits) {
            this.merged = dim;
            this.edits = edits == null ? Edits.nullEdits : edits;
        }

        @Override
        public int getY () {
            Integer y = edits.getY();
            return y == null ? merged.getY() : y;
        }

        @Override
        public int kerning (Character c) {
            Integer k = edits.kerning(c);
            return k == null ? merged.kerning(c) : k;
        }

        @Override
        public int getAdvanceWith () {
            Integer a = edits.getAdvanceWidth();
            return a == null ? merged.getAdvanceWith() : a;
        }

        @Override
        public int getHeight () {
            return merged.getHeight();
        }

        @Override
        public int getWidth () {
            return merged.getWidth();
        }

        @Override
        public int getSize () {
            return getWidth() * getHeight();
        }
    }

    /**
     * A complete implementation of {@link CUFGlyphDimension} with support for
     * {@link Edits applying edits} and {@link Kerner using kerning}.
     */
    public static class CUFGlyphDimensionImpl implements CUFGlyphDimension {

        private final int width;
        private final int height;
        private final int advanceWidth;
        private final int y;
        private final Edits edits;
        private final Kerner kerningImpl;

        /**
         * Create a {@link CUFGlyphDimension}. This constructor assumes no edits and no kerning are applied.
         * @param width the width of the alpha mask of the glyph represented by this {@link CUFGlyphDimension}.
         * @param height the height of the alpha mask of the glyph represented by this {@link CUFGlyphDimension}.
         * @param advanceWidth the advance width of the glyph represented by this {@link CUFGlyphDimension}.
         * @param y a offset of the glyph represented by this {@link CUFGlyphDimension} relative to the baseline of some
         * CUF font.
         */
        public CUFGlyphDimensionImpl (int width, int height, int advanceWidth,
                                      int y) {
            this(width, height, advanceWidth, y, null, null);
        }

        /**
         * Create a {@link CUFGlyphDimension}.
         * @param width the width of the alpha mask of the glyph represented by this {@link CUFGlyphDimension}.
         * @param height the height of the alpha mask of the glyph represented by this {@link CUFGlyphDimension}.
         * @param advanceWidth the advance width of the glyph represented by this {@link CUFGlyphDimension}.
         * @param y a offset of the glyph represented by this {@link CUFGlyphDimension} relative to the baseline of some
         * CUF font.
         * @param kerner {@link Kerner kerning information} for the glyph represented by this {@link CUFGlyphDimension}, may be null.
         * @param edits a delta of {@link Edits} to check for edited versions of the above parameters, may be null.
         */
        public CUFGlyphDimensionImpl (int width,
                                      int height,
                                      int advanceWidth,
                                      int y,
                                      Kerner kerner,
                                      Edits edits) {
            this.width = width;
            this.height = height;
            this.y = y;
            this.advanceWidth = advanceWidth;
            this.edits = edits == null ? Edits.nullEdits : edits;
            this.kerningImpl = kerner == null ? Kerner.nullKerner : kerner;
        }

        @Override
        public int getY () {
            Integer aH = edits.getY();
            return aH == null ? y : aH;
        }

        @Override
        public int kerning (Character c) {
            Integer k = edits.kerning(c);
            if (k == null) {
                k = kerningImpl.kerning(c);
                if (k == null || k < 0) {
                    k = getAdvanceWith();
                }
            }
            return k;
        }

        @Override
        public int getAdvanceWith () {
            Integer aW = edits.getAdvanceWidth();
            return aW == null ? advanceWidth : aW;
        }

        @Override
        public int getHeight () {
            return height;
        }

        @Override
        public int getWidth () {
            return width;
        }

        @Override
        public int getSize () {
            return getHeight() * getWidth();
        }

        /**
         * Test two CUFGlyphDimensions for equality. Equality is defined as having equal property values; that is:
         * the properties of the other CUFGlyphDimension object equal those of this object.
         * @param other another CUFGlyphDimension object.
         * @return true if this CUFGlyphDimension equals the other.
         */
        public boolean equals (CUFGlyphDimension other) {
            return other.getY() == getY()
                    && other.getAdvanceWith() == getAdvanceWith()
                    && other.getHeight() == getHeight()
                    && other.getWidth() == getWidth();
        }

        /**
         * Return a string representation of this object.
         * @return custom string representation of the {@link CUFGlyphDimension}.
         */
        @Override
        public String toString () {
            return String.format("{ w: %1$d, h: %2$d, a-w: %3$d, a-h: %4$d }",
                                 width, height, advanceWidth, y);
        }
    }
}
