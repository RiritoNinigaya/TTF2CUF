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
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.TreeMap;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension.CUFGlyphDimensionImpl;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.support.Escapes;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * This class performs rendering of source text for a given {@link CUFSource}.
 * Also, it exposes methods to calculate appropriate render
 * dimensions for text in a given {@link CUFSource}.
 * @author Johan Ouwerkerk
 */
public class CUFFont {

    /**
     * Get the {@link CUFSource} object supplying this CUFFont with data.
     *
     * @return the value of {@link #backend}/
     */
    public CUFSource getBackend () {
        return backend;
    }
    /**
     * The {@link CUFSource} backend providing this CUFFont with data.
     */
    private CUFSource backend = null;

    /**
     * Constructs a CUFFont object.
     * @param reader the {@link #backend} used by this CUFFont.
     */
    public CUFFont (CUFSource reader) {

        this.backend = reader;
    }

    /**
     * Renders text as an image then draws it on the Graphics object
     * @param source text to render
     * @param g {@link Graphics} object to draw on
     * @param baseX x-offset of the render relative to the origin of the Graphic's bounding box
     * @param baseY y-offset of the render relative to the origin of the Graphic's bounding box
     * @param colorCode color code to use for rendering
     */
    public void drawText (String source, Graphics g, int baseX, int baseY,
                          int colorCode) {
        if (isPrepared()) {
            g.drawImage(render(source, colorCode), baseX, baseY, null);
        }
    }
    private int jobSize;
    private TreeMap<Character, CUFGlyph> job;
    /**
     * A pattern for use in {@link String#split(java.lang.String) } to split source text around newline characters.
     * This is used for calculating render dimensions of arbitrary text in {@link #drawText(java.lang.String, java.awt.Graphics, int, int, int) }.
     */
    public static final String splitPattern = "\\Q" + String.valueOf(Escapes.Newline.
            character) + "\\E";

    /**
     * Renders text as a new image.
     * @param source the text to render
     * @param colorCode the color code to use
     * @return an image containing the render of the text in this font
     */
    public BufferedImage render (String source, int colorCode) {
        signalProgress(Messages.CalculateRenderDimensions);
        jobSize = source.length();
        job = new TreeMap<Character, CUFGlyph>();
        String[] lines = source.split(splitPattern);
        CUFGlyphDimension dim = calculateRenderDimensions(lines);
        signalProgress(Messages.CreateEmptyRender);
        BufferedImage image = allocateImage(dim, false);
        signalProgress(Messages.StartRendering);
        return renderOnImage(lines, image, 0, backend.getCUFProperties(
                CUFProperty.Baseline), colorCode);
    }

    /**
     * Creates (allocates) an image object with w and h matching relevant
     * properties of this object. The allocated image is “sized to fit”.
     * @param useActualDimension if true, allocate an image according to {@link CUFGlyphDimension#getWidth()} and
     * {@link CUFGlyphDimension#getHeight()};
     * if false, allocate an image according to {@link CUFGlyphDimension#getAdvanceWidth()} and {@link CUFGlyphDimension#getY()}.
     * @return the allocated image.
     */
    private BufferedImage allocateImage (CUFGlyphDimension dim,
                                         boolean useActualDimension) {
        int y = dim.getY(), advanceWidth = dim.getAdvanceWith();
        useActualDimension = useActualDimension || advanceWidth <= 0 || y <= 0;
        int w = useActualDimension ? dim.getWidth() : advanceWidth;
        int h = useActualDimension ? dim.getHeight() : y;

        return new BufferedImage(w == 0 ? 1 : w, h == 0 ? 1 : h,
                                 BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Renders text on an image.
     * @param source the lines of text to render
     * @param image a preallocated {@link BufferedImage} to render the source on
     * @param baseX x offset to start drawing on the image (typically 0)
     * @param baseY y offset of the conceptual “baseline” all glyphs appear to “rest”
     * @param colorCode color code to use for drawing glyphs
     * @return a {@link BufferedImage} with the source text rendered onto it
     */
    @SuppressWarnings("unchecked")
    public BufferedImage renderOnImage (String[] source, BufferedImage image,
                                        int baseX, int baseY, int colorCode) {
        if (isPrepared()) {
            //int vmap =propGet(FormatConstants.LayoutYOffset);
            int hmap = propGet(CUFProperty.LayoutXOffset);

            baseX += (hmap < 0 ? 0 : hmap);
            // Left offset (reset value)
            int xoff = baseX;

            // CUF format works with (0,0) origin as lower left corner instead of upper left... I think?
            int yoff = 0; //(vmap < 0 ? -vmap : 0);
            // Top offset
            baseY += yoff;

            int progress = 0;

            for (String s : source) {
                if (s.equals("") == false) { // ignore empty lines
                    ArrayList<?> codes = backend.stringToCodes(s);
                    CUFGlyph glyph;
                    for (int k = 0, v = 1, l = codes.size(); k < l; ++k, ++v) {
                        Object code = codes.get(k);
                        // count nulls as well
                        if (code != null) {
                            glyph = job.get(s.charAt(k));//backend.getGlyph(code);
                            image = glyph.writeToBitMap(image,
                                                        baseX,
                                                        baseY - glyph.
                                    getDimension().getY(), // compute offset so that all glyphs rest on the base line
                                                        colorCode);
                            if (v < l) {
                                baseX += glyph.getDimension().kerning(
                                        s.charAt(v)); // advance X position
                            }
                            else {
                                baseX += glyph.getDimension().getAdvanceWith();
                            }
                            ++progress;
                            signalProgress(progress,
                                           jobSize,
                                           Messages.PaintedGlyph,
                                           progress,
                                           jobSize);
                        }
                    }
                    baseX = xoff; // reset X to its original offset
                }
                baseY += propGet(CUFProperty.VSize);
            }
        }
        signalProgress(jobSize, jobSize, Messages.RenderDone);
        return image;
    }

    /**
     * Signal the progress of a render.
     * @param message describing how far the rendering process has progressed.
     * @param args any arguments applicable to the message.
     */
    protected void signalProgress (final BundleKey message, final Object... args) {
    }

    /**
     * Signal how many glyphs have been rendered already.
     * @param value the number of glyphs redered already.
     * @param maximum the total number of glyphs to render.
     * @param message a format string about what glyph was rendered.
     * @param args any arguments applicable to the message.
     */
    protected void signalProgress (final int value, final int maximum,
                                   final BundleKey message, final Object... args) {
    }

    /**
     * Calculates render dimensions for lines of text.
     * Each item in the given array is assumed to correspond to a single line in the render.
     * @param sourceLines the source text to calculate render dimensions for
     * @return a {@link CUFGlyphDimension} describing the dimensions required to render the source text.
     * @see #calculateRenderDimensions(java.lang.String)
     */
    public CUFGlyphDimension calculateRenderDimensions (String[] sourceLines) {
        jobOffset = 0;
        int[] dims = new int[] { 0,
                                 0,
                                 0,
                                 0 };
        CUFGlyphDimension curdim;
        for (String line : sourceLines) {
            curdim = calculateRenderDimensions(line);
            dims[0] = curdim.getWidth() > dims[0] ? curdim.getWidth() : dims[0];
            dims[1] += curdim.getHeight();
            dims[2] = curdim.getAdvanceWith() > dims[2] ? curdim.getAdvanceWith()
                    : dims[2];
            dims[3] += curdim.getY();
        }
        return maxDimension(dims,
                            new int[] { 0,
                                        0,
                                        propGet(CUFProperty.HSize) * 2,
                                        propGet(CUFProperty.VSize) * 2 });
    }

    private CUFGlyphDimension maxDimension (int[] dims1, int[] dims2) {
        for (int i = 0; i < 4; ++i) {
            dims1[i] = dims1[i] > dims2[i] ? dims1[i] : dims2[i];
        }
        return new CUFGlyphDimensionImpl(dims1[0], dims1[1], dims1[2], dims1[3]);
    }

    private int propGet (CUFProperty index) {
        if (backend.isAvailable(index)) {
            return backend.getCUFProperties(index);
        }
        return 0;
    }
    private int jobOffset;

    /**
     * Calculates render dimensions for a piece of text. 
     * This text is assumed to be rendered in a single line; therefore it is not suitable for calculating dimensions of text
     * that needs to span multiple lines.
     * @param source the source text to calculate render dimensions for
     * @return a {@link CUFGlyphDimension} describing the dimensions required to render the source text.
     */
    @SuppressWarnings("unchecked")
    public CUFGlyphDimension calculateRenderDimensions (String source) {

        int hsize = propGet(CUFProperty.HSize);
        int vsize = propGet(CUFProperty.VSize);
        CUFGlyph glyph;
        CUFGlyphDimension curdim;
        ArrayList<?> codes = backend.stringToCodes(
                source);

        int[] dims = new int[] { 0,
                                 0,
                                 hsize,
                                 vsize
        };
        boolean contains;
        if (codes != null && source.equals("") == false) {
            for (int k = 0, v = 1, l = codes.size(); k < l; ++k, ++v) {
                Object code = codes.get(k);
                if (code != null) {
                    contains = job.containsKey(source.charAt(k));
                    glyph = contains ? job.get(source.charAt(k)) : backend.
                            getGlyph(code);
                    if (glyph != null) {
                        if (!contains) {
                            job.put(source.charAt(k), glyph);
                        }
                        curdim = glyph.getDimension();
                        ++jobOffset;
                        dims[0] += curdim.getWidth();
                        dims[1] = dims[1] < curdim.getHeight()
                                ? curdim.getHeight()
                                : dims[1];
                        // advance X position
                        if (v < l) {
                            dims[2] += curdim.kerning(source.charAt(v));
                        }
                        else {
                            dims[2] += curdim.getAdvanceWith();
                        }
                        signalProgress(jobOffset,
                                       jobSize,
                                       Messages.RenderedGlyph,
                                       jobOffset,
                                       jobSize);
                    }
                }
            }
        }
        return new CUFGlyphDimensionImpl(dims[0], dims[1], dims[2], dims[3]);
    }

    /**
     * Return whether or not this font is fully prepared.
     * @return true if the {@link #backend} is not null and if it is also prepared.
     */
    public boolean isPrepared () {
        return backend != null && backend.isPrepared();
    }
}
