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
package org.europabarbarorum.cuf.font.pipes;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.io.RandomAccessFile;
import org.europabarbarorum.cuf.font.impl.CUFGlyph;
import org.europabarbarorum.cuf.font.impl.CharTableEntry;

/**
 * Enumeration of {@link Phase} implementations supported by default by {@link SystemFontSource} and
 * {@link CUFReader}. Note that not all of these are supported by both.
 * @author Johan Ouwerkerk
 */
public enum StandardPhases {

    /**
     * {@link Phase} when a glyph is read from a file in {@link CUFReader}. This {@link Phase} is mentioned for completeness,
     * but it is not accessible to code outside of {@link CUFReader}.
     */
    ReadGlyph,

    /**
     * {@link Phase} when a glyph is obtained from a {@link GlyphVector}. This {@link Phase} is mentioned for
     * completeness, but it is not accessible to code outside {@link SystemFontSource}.
     */
    GetGlyphVector,
    /**
     * {@link Phase} when a {@link Shape} is adjusted to better fit a raster model.
     * This phase serves to provide improved vertical alignment of glyphs produced by
     * a {@link SystemFontSource}.
     */
    AligmentBox,
    /**
     * {@link Phase} when a {@link Shape} is transformed in {@link SystemFontSource}.
     */
    ShapeShape,
    /**
     * {@link Phase} when a {@link Shape} is converted to a {@link CUFGlyph} (more precisely: when it is converted to the
     * bytes that make up its bitmap). This {@link Phase} is supported by {@link SystemFontSource}.
     */
    ShapeGlyph,
    /**
     * {@link Phase} when the contents of the bitmap of a {@link CUFGlyph} are post processed. This {@link Phase} is
     * supported by both {@link SystemFontSource} and {@link CUFReader}.
     */
    PostProcess;

    /**
     * Get the {@link Class} of the actual {@link Phase} implementation.
     * @return the class of the {@link Phase} represented by this enum constant.
     */
    public Class<? extends Phase> phase () {
        switch (this) {
            case ReadGlyph:
                return GetGlyph.class;
            case GetGlyphVector:
                return GetGlyphVector.class;
            case ShapeShape:
                return ShapeShape.class;
            case PostProcess:
                return PostProcess.class;
            case ShapeGlyph:
                return ShapeGlyph.class;
            case AligmentBox:
                return AlignmentBox.class;
            default:
                return null;
        }
    }

    /**
     * The {@link Phase} implementation that obtains a {@link CUFGlyph} from a {@link RandomAccessFile}.
     * @see Phase.PhaseImpl
     * @see CUFReader
     */
    public static class GetGlyph extends Phase.PhaseImpl<CUFGlyph, RandomAccessFile,CharTableEntry> {

        /**
         * Creates this {@link Phase}.
         */
        public GetGlyph () {
            super(1, true);
        }

        @Override
        public boolean validate (CUFGlyph output, RandomAccessFile input,CharTableEntry arg) {
            return output != null;
        }
    }

    /**
     * The {@link Phase} implementation that improves vertical alignment of a {@link Shape}.
     * @see Phase.PhaseImpl
     * @see SystemFontSource
     */
    public static class AlignmentBox extends Phase.PhaseImpl<Rectangle2D, GlyphVector, GlyphMetrics> {
        /**
         * Creates this {@link Phase}.
         */
        public AlignmentBox() {
            super(1,true);
        }

        @Override
        public boolean validate (Rectangle2D output, GlyphVector vec, GlyphMetrics arg) {
            return output!=null;
        }
    }
    
    /**
     * The {@link Phase} implementation that obtains a {@link GlyphVector} from a {@link Font}.
     * @see Phase.PhaseImpl
     * @see SystemFontSource
     */
    public static class GetGlyphVector extends Phase.PhaseImpl<GlyphVector, Font, String> {

        /**
         * Creates this {@link Phase}.
         */
        public GetGlyphVector () {
            super(1, true);
        }

        @Override
        public boolean validate (GlyphVector output, Font input, String arg) {
            return output != null;
        }
    }

    /**
     * The {@link Phase} implementation that obtains a {@link CUFGlyph} from a {@link Shape}.
     * @see Phase.PhaseImpl
     * @see SystemFontSource
     */
    public static class ShapeGlyph extends Phase.PhaseImpl<CUFGlyph, Shape,Rectangle2D> {
        
        /**
         * Creates this {@link Phase}.
         */
        public ShapeGlyph () {
            super(1, true);
        }

        @Override
        public boolean validate (CUFGlyph output, Shape input,Rectangle2D box) {
            return output != null;
        }
    }
    
    /**
     * The {@link Phase} implementation that post-process the bitmap data of a {@link CUFGlyph}.
     * @see Phase.PhaseImpl
     * @see SystemFontSource
     * @see CUFReader
     */
    public static class PostProcess extends Phase.PhaseImpl<byte[], byte[], Void> {

        /**
         * Creates this {@link Phase}.
         */
        public PostProcess () {
            super(Integer.MAX_VALUE, false);
        }

        @Override
        public boolean validate (byte[] output, byte[] input, Void arg) {
            return output.length == input.length;
        }
    }

    /**
     * The {@link Phase} implementation that transforms an input {@link Shape} to the final output {@link Shape}.
     * @see Phase.PhaseImpl
     * @see SystemFontSource
     */
    public static class ShapeShape extends Phase.PhaseImpl<Shape, Shape, FontInformation> {

        /**
         * Creates this {@link Phase}.
         */
        public ShapeShape () {
            super(Integer.MAX_VALUE, false);
        }

        @Override
        public boolean validate (Shape output, Shape input, FontInformation arg) {
            return output != null;
        }
    }
}
