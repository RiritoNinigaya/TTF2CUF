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

import org.europabarbarorum.cuf.font.impl.WrappedSource.AlgorithmSource;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 *
 * @author Johan Ouwerkerk
 */
public enum Messages implements BundleKey {

    /**
     * Error when the user provides a list of character values
     * to be used as substitute for the given list of keys for a certain font,
     * but the values are too few to cover all the keys or there are no keys given.
     * Takes number of keys and number of values supplied as argument.
     */
    TooFewEntriesError(MappedSource.class),
    /**
     * Error when the user provides two characters to represent a range of characters, 
     * but the `lower' bound is not in fact lower than the `upper' bound.
     * Takes two character arguments.
     */
    InvalidCharRange(RestrictedSource.class),
    /**
     * Warning emitted when fixing chartable compatibility caused some characters to be 
     * discarded. Takes source/name of the fixed font as argument.
     */
    LeftOverChars(AlgorithmSource.class),
    InvalidCUFPropertyIndex(CUFProperty.class),
    RenderDone(CUFFont.class),
    PaintedGlyph(CUFFont.class),
    RenderedGlyph(CUFFont.class),
    CalculateRenderDimensions(CUFFont.class),
    CreateEmptyRender(CUFFont.class),
    StartRendering(CUFFont.class),
    MixedInvalidError(MixedSource.class),
    MixedContainsError(MixedSource.class),
    MixedListsLengthError(MixedSource.class),
    MixedArgumentNullError(MixedSource.class),
    WrappedInvalidError(org.europabarbarorum.cuf.font.impl.WrappedSource.class),
    WriterProgress(CUFWriter.class),
    Start(CUFWriter.class),
    Stage1(CUFWriter.class),
    OpenTemp(CUFWriter.class),
    OrderChars(CUFWriter.class),
    WriteChars(CUFWriter.class),
    WriteKerning(CUFWriter.class),
    WriteGlyphs(CUFWriter.class),
    Stage2(CUFWriter.class),
    Open(CUFWriter.class),
    PrepareKerning(CUFWriter.class),
    CUFProps(CUFWriter.class),
    CopyKerning(CUFWriter.class),
    MetaData(CUFWriter.class),
    CopyGlyphs(CUFWriter.class),
    JobTitle(CUFWriter.class);

    private Messages (Class type) {
        this.type = type;
    }
    private final Class type;

    
    @Override
    public String getText () {
        return ResourceHelp.getValue(this, Messages.class);
    }

    @Override
    public Class type () {
        return type;
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, Messages.class, args);
    }
}
