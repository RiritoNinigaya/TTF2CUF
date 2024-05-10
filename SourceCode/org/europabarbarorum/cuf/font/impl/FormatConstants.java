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

import org.europabarbarorum.cuf.font.Messages;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * Constant values used in, or required by other components to interpret/render properties of, the CUF file format.
 * @author Johan Ouwerkerk
 */
public enum FormatConstants {

    /**
     * Number of global ‘properties’ of a CUF font.
     */
    NumCUFProps(13),
    /**
     * Number of editable global ‘properties’ of a CUF font.
     */
    NumEditableCUFProps(11),
    /**
     * Number of bytes in the CUF props table.
     */
    CUF_TABLE_SIZE(0x1C),
    /**
     * Number of bytes in the CUF file header.
     */
    HEADER_SIZE(0x20),
    /**
     * Commonly used limit/blank value in the CUF file format.
     */
    __LIMIT__(0xFFFF),
    /**
     * Number of bytes used to encode all components of a glyph dimension.
     */
    GlyphDimensionSize(4),
    /**
     * Number of bytes used to encode a glyph offset pointer.
     */
    GlyphOffsetSize(4),
    /**
     * Offset of the byte corresponding to the allocated width of a glyph inside the
     * integer describing the dimensions of this glyph.
     */
    GlyphAllocWidth(0x1),
    /**
     * Offset of the byte corresponding to the y-offset of a glyph inside the
     * integer describing the dimensions of this glyph.
     * Note that the y-origin corresponds to the lower corner (as opposed to the upper [left] corner many raster formats use).
     */
    GlyphAllocHeight(0x0),
    /**
     * Offset of the byte corresponding to the width of a glyph inside the
     * integer describing the dimensions of this glyph.
     */
    GlyphWidth(0x2),
    /**
     * Offset of the byte corresponding to the height of a glyph inside the
     * integer describing the dimensions of this glyph.
     */
    GlyphHeight(0x3),
    /**
     * Number of tables used to expose meta data about a glyph in a CUF file.
     */
    NUM_GLYPH_META_TABLES(0x2),
    /**
     * Offset at which meta tables about individual glyphs start in the CUF file.
     */
    START_OF_GLYPH_META_TABLES(0x20020),
    /**
     * Number of bytes in the kerning meta table.
     */
    KerningPropTableSize(0x04),
    /**
     * The index of the property that specifies the number of glyphs for which
     * no kerning information is available.
     */
    KerningSkip(1),
    /**
     * The index of the property that specifies the number of glyphs for which
     * kerning information is available.
     */
    KerningSize(0);
    /**
     * Actual constant.
     */
    private final int value;

    /**
     * Construct a CUF FormatConstant.
     * @param i some constant pertaining to the CUF format
     */
    private FormatConstants (int i) {
        this.value = i;
    }

    /**
     * Get the value of a FormatConstant.
     * @return the value of this constant.
     */
    public int value () {
        return value;
    }

    /**
     * Implementation of {@link BundleKey} for certain
     * {@link FormatConstants} that represent a CUF property.
     * This keeps the logic of that local to this file. This file maintains only the
     * keys required to provide localized descriptions of the {@link FormatConstants} to do with
     * CUF properties. Furthermore {@link #name() } matches that of the corresponding {@link FormatConstants};
     * and the order of the declaration matches the {@link FormatConstants#value() } of the corresponding
     * {@link FormatConstants}. (The tests package provides unit tests for these properties.)
     */
    public enum CUFProperty implements BundleKey {

        /**
         * Unknown purpose. First CUF property.
         */
        FirstProp(0),
        /**
         * Unknown purpose. Second CUF property.
         */
        SecondProp(1),
        /**
         * Index of the value which appears to have something to do with line height.
         * Underscore line? Base line?
         */
        LineHeight(2),
        /**
         * Unknown purpose. Fourth CUF property.
         */
        FourthProp(3),
        /**
         * Unknown purpose. Fifth CUF property.
         */
        FifthProp(4),
        /**
         * Index of the value which appears to correspond to a ‘baseline’ of sorts in the CUF file format.
         */
        Baseline(5),
        /**
         * Index of the value which determines y-offset w.r.t. the bounding box of a string of text in this font.
         */
        LayoutYOffset(6, Translator.SignedShort),
        /**
         * Used to specify how wide a space is for justification and text wrapping calculations.
         * <p>
         * Note that the tool itself currently ignores this property in the calculations: it relies on
         * advance widths of any space characters used.
         */
        SpaceJustify(7),
        /**
         * Index of the value which determines x-offset w.r.t. the bounding box of a string of text in this font.
         */
        LayoutXOffset(8, Translator.SignedShort),
        /**
         * Index of the value which determines a maximum width for glyphs.
         * Glyphs which are wider than the maximum specified for this property will appear cut-off.
         *  There appears to be no effect on the position of a glyph
         * after a glyph of which the advance is larger than the value specified for this setting.
         * <p>
         * Note that individual glyphs contain sufficient information to calculate a much more optimal bounding box than by simply using
         * multiples of the value corresponding to this index.
         */
        HSize(9),
        /**
         * Index of the value which determines a maximum height for glyphs.
         * The corresponding value probably should include leading.
         * Glyphs which are taller than the maximum specified for this property will appear cut-off.
         * <p>Too small values for this property may result in crashes or unspecified errors on exit in M2TW.
         * <p>
         * Note that individual glyphs contain sufficient information to calculate a much more optimal bounding box than by simply using
         * multiples of the value corresponding to this index.
         */
        VSize(10),
        /**
         * Index in the property model/table/array of the property
         * declaring the number of glyphs in a CUF font.
         */
        NumberOfGlyphs(11, 2, false, Translator.UnsignedValue),
        /**
         *Size of the glyph data section.
         */
        GlyphDataSize(12, 4, false, Translator.UnsignedValue);

        /**
         * Get a descriptive string (name) indentifying this {@link FormatConstants} in a sensible manner.
         * This method relies on the {@link BundleKey} resource loading mechanism.
         * @return a descriptive string for the {@link FormatConstants} object.
         */
        @Override
        public String getText () {
            return ResourceHelp.getValue(this, FormatConstants.class);
        }

        @Override
        public String format (Object... args) {
            return ResourceHelp.formatValue(this, FormatConstants.class, args);
        }

        @Override
        public Class type () {
            return CUFProperty.class;
        }

        private CUFProperty (int index, int amount, boolean modifiable,
                             Translator t) {
            this.amount = amount;
            this.modifiable = modifiable;
            this.translator = t;
            this.index = index;
        }

        private CUFProperty (int index) {
            this(index, Translator.UnsignedValue);
        }

        private CUFProperty (int index, Translator t) {
            this(index, 2, true, t);
        }
        private final int amount;
        private final int index;

        /**
         * Number of bytes this property takes up in a CUF file.
         * @return the number of bytes the property requires for serialisation to a CUF file
         */
        public int byteAmount () {
            return amount;
        }
        private final boolean modifiable;

        /**
         * Suggests whether or not the property should be editable by objects other than the
         * CUF font that initialised the value itself. Fonts may ignore this suggestion; this
         * method merely serves to provide a simple way within Java to look up the most current
         * idea about which CUF properties can be freely edited.
         * @return true if it is suggested that the CUF property represented by this object
         * should be editable.
         */
        public boolean isModifiable () {
            return modifiable;
        }

        /**
         * Get the index of this property in the table. This method is useful for ordering
         * a {@link CUFProperty} according to its position in such a table.
         * This is merely a shorthand for looking up the {@link FormatConstants#value() value} of
         * the {@link FormatConstants format constant} with the same name.
         * @return the logical index of the property in a table containing all CUF properties.
         */
        public int index () {
            return index;
        }

        /**
         * Get the byte offset of the property in a CUF file.
         * @return the offset (zero-based index)
         * in a file or byte array at which this CUF property would be encoded.
         */
        public int offset () {
            if (this == CUFProperty.FirstProp) {
                return 0;
            }
            else {
                CUFProperty prev = forIndex(index - 1);
                return prev.offset() + prev.byteAmount();
            }
        }

        /**
         * Get the {@link CUFProperty} corresponding to a logical index.
         * @param index the index of the property in the CUF table.
         * @return the instance representing the given index.
         */
        public static CUFProperty forIndex (int index) {
            if (index > -1 && index < FormatConstants.NumCUFProps.value()) {
                return CUFProperty.values()[index];
            }
            throw new IllegalArgumentException(
                    Messages.InvalidCUFPropertyIndex.format(index));
        }
        /**
         * The {@link Translator} that this {@link CUFProperty} uses.
         */
        public final Translator translator;

        /**
         * Enumeration of “translation” algorithms that convert between
         * “human readable” and raw {@link CUFProperty} values.
         */
        public enum Translator {

            /**
             * Behaves like a signed short.
             * A value V for the property that uses this {@link Translator}
             * should satisfy: 0 &lt;= |V| &lt;= 0x7fff.
             */
            SignedShort {

                @Override
                public Integer setTranslate (int value) {
                    return IOHelp.unsign(value, 2);
                }

                @Override
                public Integer getTranslate (int value) {
                    return IOHelp.sign(value, 2);
                }
            },
            /**
             * Behaves like an unsigned value: this is the identity translation.
             */
            UnsignedValue {

                @Override
                public Integer setTranslate (int value) {
                    return value;
                }

                @Override
                public Integer getTranslate (int value) {
                    return value;
                }
            };

            /**
             * Translate between human-readable decimal integer value and its raw form.
             * Additionally this method performs validation to check that the given value is
             * within bounds.
             * @param value the value as it is understood by the user.
             * @return the raw form of the value as it would be encoded in a CUF file, or
             * null if the given value is invalid.
             */
            abstract public Integer setTranslate (int value);

            /**
             * Translate between raw (unsigned) integer form and human-readable decimal integer value.
             * @param value the raw value.
             * @return the value as it is understood by the user and rendering algorithms.
             */
            abstract public Integer getTranslate (int value);
        }
    }
}
