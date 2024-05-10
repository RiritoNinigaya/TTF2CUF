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

import java.awt.image.BufferedImage;
import org.europabarbarorum.cuf.font.CUFFont;
import org.europabarbarorum.cuf.font.CUFSource;

/**
 * @author Johan Ouwerkerk
 *
 * Object to hold the data that makes up a ‘glyph’ in an object pretending to be a {@link CUFFont}.
 * Glyphs are not obtained directly from the {@link CUFFont}, rather the {@link CUFFont} object has a
 * {@link CUFSource} backend which provides a specific implementation to retrieve the data from its actual source.
 * @see org.europabarbarorum.cuf.font.CUFFont
 * @see org.europabarbarorum.cuf.font.CUFSource
 */
public class CUFGlyph {

    /**
     * The object that encodes render parameters for this CUFGlyph object.
     * This object is used by {@link org.europabarbarorum.cuf.font.CUFFont} for laying out multiple glyphs; and to allocate
     * a properly sized image to render text on.
     */
    protected CUFGlyphDimension dimension;
    /**
     * Bytes of the bitmap representing this glyph.
     * These bytes actually form an alpha map of the glyph with each byte corresponding to a single pixel.
     */
    protected byte[] bitmapData;

    /**
     * Get the {@link #dimension} object.
     * @return the {@link #dimension} object of this glyph
     */
    public CUFGlyphDimension getDimension () {
        return dimension;
    }

    /**
     * Get the “bitmap” data used for rendering this glyph.
     * @return flattened representation of the alpha values in the “bitmap”
     */
    public byte[] getBitMapData () {
        return bitmapData;
    }

    /**
     * Draws this glyph on a {@link BufferedImage}
     * @param image image to draw on
     * @param baseX x-offset to start drawing at on the image
     * @param baseY y-offset to start drawing at on the image
     * @param colorCode color to use for “black” pixels in the bitmap (“white” ones will be completely translucent).
     * @return the image supplied with this glyph drawn onto it.
     */
    public BufferedImage writeToBitMap (BufferedImage image, int baseX,
                                        int baseY, int colorCode) {
        byte[] bs = bitmapData;
        int pictureHeight = image.getHeight();
        int pictureWidth = image.getWidth();

        // per y coord: provide boundary checking on the image
        for (int count = 0, i = 0, y = baseY; i < dimension.getHeight() && y < pictureHeight; ++i, ++y, count =
                        i * dimension.getWidth()) {
            // draw horizontals: provide boundary checking on the image
            for (int j = 0, x = baseX; j < dimension.getWidth() && x < pictureWidth; ++j, ++x, ++count) {
                if (x >= 0 && y >= 0) {
                    // set the alpha value of the colour to the value of the pixel read
                    if ((bs[count] & 0xFF) > 0) {
                        image.setRGB(x, y, colorCode | (bs[count] << 24));
                    }
                }
            }
        }
        return image;
    }

    /**
     * @param dimension the {@link #dimension} object of this CUFGlyph.
     * @param bitMapData the {@link #bitmapData} of the alpha map of this CUFGlyph.
     * @see CUFGlyphDimension
     */
    public CUFGlyph (CUFGlyphDimension dimension,
                     byte[] bitMapData) {
        this.dimension = dimension;
        // this.charCode = charCode;
        this.bitmapData = bitMapData;
    }

    /**
     * Compares two CUFGlyphs to each other.
     * @param other the other CUFGlyph to compare this one against.
     * @return true if the corresponding fields (properties) of both CUFGlyphs equal each other.
     */
    public boolean equals (CUFGlyph other) {
        return other.getBitMapData() == bitmapData
                // && other.getCharCode() == charCode
                && other.getDimension().equals(dimension);
    }
}
