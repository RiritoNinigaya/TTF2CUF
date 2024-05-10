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

import org.europabarbarorum.cuf.font.impl.FormatConstants;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.font.impl.CUFGlyph;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.TreeMap;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension.CUFGlyphDimensionImpl;
import org.europabarbarorum.cuf.font.impl.CharTableEntry;
import org.europabarbarorum.cuf.font.impl.Kerner;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 * This class provides a way to present system fonts (either read directly from disk or
 * accessed through other OS/graphics system layers) as a CUF font. This class provides
 * intrinsic support for {@link Kerner#kerning(java.lang.Character) kerning}; and
 * supports editing.
 * @author Johan Ouwerkerk
 */
public class SystemFontSource extends AbstractPhaseSource<Character> {

    private Font font;
    private FontRenderContext context;
    private LineMetrics metrics;

    /**
     * Creates a new {@link SystemFontSource}.
     */
    public SystemFontSource () {
        super(false);
        setDirection(true);
    }


    /**
     * This method provides a list of {@link Transform} objects intended to maintain compatibility with the
     * Java way of doing text decoration/font makeup.
     * @return the list of {@link Transform.ConditionalTransform} objects to take into account when
     * initializing a {@link SystemFontSource} from a given {@link Font}.
     */
    protected Transform.ConditionalTransform[] compatTransforms () {
        return new Transform.ConditionalTransform[] {
                    new Transform.UnderLineTransform(),
                    new Transform.StrikeThroughTransform()
                };
    }

    /**
     * Initialize this {@link SystemFontSource} with the result of {@link Font#decode(java.lang.String) }.
     * @param codeString the string describing the font to use
     */
    @Override
    public void init (String codeString) {
        preInit();
        font = Font.decode(codeString);
        postInit();
    }

    /**
     * Initializes this {@link SystemFontSource} with the given {@link Font}.
     * @param f the {@link Font} to use.
     */
    public void init (Font f) {
        preInit();
        font = f;
        postInit();
    }

    /**
     * Initialize this {@link SystemFontSource} with the given {@link Font} and set {@link #cufSource}.
     * @param f the {@link Font} to use for providing glyphs
     * @param source the new value for {@link #cufSource} to use. This string is used for identification
     * purposes.
     */
    public void init (Font f, String source) {
        this.init(f);
        this.cufSource = source;
    }

    /**
     * Common reset logic of the init methods.
     */
    @SuppressWarnings("unchecked")
    protected void preInit () {
        charTable = null;
        prepared = false;
        context = null;
        cufSource = null;
    }
    private FontInformation info;

    /**
     * Common logic of init methods after loading the {@link Font}.
     * This method gathers some CUF properties from the {@link Font} it has loaded; sets up the
     * rendering pipeline and does some other initalisation work.
     */
    protected void postInit () {
        cufSource = font.getFontName();
        CUFProperties = new int[FormatConstants.NumCUFProps.value()];
        context = new FontRenderContext(
                font.getTransform(),
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        metrics = font.getLineMetrics("irrelevant?", context);

        info = new FontInformation() {

            @Override
            public Object get (FontInformationKey key) {
                if (key instanceof StandardFloatKeys) {
                    switch ((StandardFloatKeys) key) {
                        case StrikeThroughOffset:
                            return metrics.getStrikethroughOffset();
                        case StrikeThroughThickness:
                            return metrics.getStrikethroughThickness();
                        case UnderlineOffset:
                            return metrics.getUnderlineOffset();
                        case UnderlineThickness:
                            return metrics.getUnderlineThickness();
                    }
                }

                return null;
            }
        };
        Transform<GlyphVector, Font, String> vecSrc = new Transform<GlyphVector, Font, String>() {

            @Override
            public GlyphVector apply (Font args, String entry) throws
                    Exception {
                return args.layoutGlyphVector(context, 
                        entry.toCharArray(),
                        0,
                        entry.length(),
                        direction);
            }
        };
        this.pipeLine.register(StandardPhases.GetGlyphVector.phase(), vecSrc);

        this.pipeLine.register(StandardPhases.AligmentBox.phase(),
                               new Transform.AlignmentBox());
        this.pipeLine.register(StandardPhases.ShapeGlyph.phase(),
                               new Transform.SubResolutionGlyph());

        Transform.ConditionalTransform[] compats = compatTransforms();
        if (compats != null) {
            Map<TextAttribute, ?> attrs = font.getAttributes();
            Object o = null;
            for (Transform.ConditionalTransform compat : compats) {
                o = attrs.get(compat.conditionalKey());
                if (o != null && compat.isSatisfied(o)) {
                    this.pipeLine.register(StandardPhases.ShapeShape.phase(),
                                           compat.getTransform());
                }
            }
        }

        for (CUFProperty key : CUFProperty.values()) {
            CUFProperties[key.index()] = initProp(key);
        }
        this.getEditor().toggleKerning(this.supportsKerning());
        prepared = true;
    }

    /**
     * Convenience method for rounding doubles to ints using {@link Math#round(double) }.
     * @param d the double to round to int
     * @return the result casted to int
     */
    public static int round (double d) {
        return (int) Math.round(d);
    }

    private int direction;
    /**
     * Set the run direction of the text. This setting may have an effect on kerning 
     * data.
     * The default setting specifies a left to right run direction.
     * @param leftToRight use true to specify left to right, false to specify 
     * right to left layout.
     */
    public final void setDirection (boolean leftToRight) {
        if(leftToRight) {
            direction=Font.LAYOUT_LEFT_TO_RIGHT;
        }
        else {
            direction = Font.LAYOUT_RIGHT_TO_LEFT;
        }
    }

    private int initProp (CUFProperty key) {
        switch (key) {
            case Baseline:
                return round(metrics.getAscent());
            case NumberOfGlyphs:
                return getCharTable().size();
            case LineHeight:
                return round(metrics.getHeight());
            case VSize:
                return round(font.getMaxCharBounds(context).getHeight());
            case HSize:
                return round(font.getMaxCharBounds(context).getWidth());
            case LayoutXOffset:
                return 2;
            case LayoutYOffset:
                return 0xFFFF - round(metrics.getLeading());
            default:
                return PROP_UNAVAILABLE;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public CUFGlyph getGlyph (Character entry) {
        if (entry == null) {
            return null;
        }
        Shape cache = null;
        Rectangle2D box = null;
        GlyphVector gVector = null;
        GlyphMetrics gMetrics = null;
        PipeLineEnumeration ple = pipeLine.enumerate();
        Phase p;
        CUFGlyph glyph = null;
        byte[] bs = null;

        while (ple.hasMoreElements()) {
            p = ple.nextElement();

            switch (ple.index()) {
                case 1:
                    gVector = ((Phase<GlyphVector, Font, String>) p).run(
                            font, entry.toString());
                    gMetrics = gVector.getGlyphMetrics(0);
                    cache = gVector.getGlyphOutline(0, 0, 0);
                    break;
                case 2:
                    box = ((Phase<Rectangle2D, GlyphVector, GlyphMetrics>) p).
                            run(gVector, gMetrics);
                    break;
                case 3:
                    cache = ((Phase<Shape, Shape, FontInformation>) p).run(cache,
                                                                           info);
                    break;
                case 4:
                    glyph = ((Phase<CUFGlyph, Shape, Rectangle2D>) p).run(cache,
                                                                          box);
                    break;
                case 5:
                    bs = ((Phase<byte[], byte[], Void>) p).run(glyph.
                            getBitMapData(), null);
                    break;
            }
        }
        return intercept(entry,
                         glyph.getDimension(),
                         bs == null ? new byte[] {} : bs);
    }

    /**
     * Get the {@link CUFGlyphDimension} object for the glyph represented by the
     * {@link CharTableEntry} parameter.
     * <p>
     * Due to the freeform nature of the {@link #pipeLine} used, this method
     * requires that all transformations for {@link #getGlyph(java.lang.Character) }
     * apply here as well. Hence this method is equivalent to calling {@link CUFGlyph#getDimension() } on
     * the result of {@link #getGlyph(java.lang.Character) } on this object.
     * </p>
     * @param entry the character to convert to {@link CUFGlyphDimension}.
     * @return the {@link CUFGlyphDimension} of the glyph found for the entry or null if unsuccessful.
     */
    @Override
    public CUFGlyphDimension getGlyphDimension (Character entry) {
        CUFGlyph glyph = getGlyph(entry);
        return glyph == null ? null : glyph.getDimension();
    }

    /**
     * Extracts the characters the underlying system font can render, and stores them in a char table.
     * <p>This algorithm provides no reliable handling of characters beyond the BMP. Some may get added, some
     * may not depending on the characters that have been added previously to the font.
     * The algorithm that is used is “greedy”: take as many characters as it is possible to stuff in a
     * CUF char table.</p><p>This presents the aforementioned problem because as far
     * as the CUF format is concerned some characters may actually
     * appear as duplicates of others. The reason is that the lower 16bits correspond to a character
     * already added to the font; and the CUF char table works with 16bits characters exclusively.
     * Therefore the code checks for this possibility first before adding characters; and thus the
     * code is not reliable for characters outside the BMP.</p>
     * <p>
     * If you need reliable handling of characters beyond the BMP (either explicitly discarding them, or
     * re-mapping them in a deterministic fashion) you should subclass this font type...
     * </p>
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void deriveCharTable () {
        charTable = new TreeMap<Character, Character>();
        Character c;
        int key = 0;

        for (int i = 0; i < Character.MAX_CODE_POINT
                && key < FormatConstants.__LIMIT__.value(); ++i) {
            if (Character.isValidCodePoint(i) && font.canDisplay(i)) {
                c = IOHelp.fromCode(i );
                if (!charTable.containsKey(c)) {
                    charTable.put(c, c);
                    ++key;
                }
            }
        }
    }

    @Override
    public boolean supportsKerning () {
        Object o = this.font.getAttributes().get(TextAttribute.KERNING);
        return o != null && o.equals(TextAttribute.KERNING_ON);
    }

    private Kerner getKerner (Character c) {
        if (supportsKerning()) {
            return new KernerImpl(c);
        }
        return null;
    }

    private CUFGlyph intercept (Character c, CUFGlyphDimension d, byte[] data) {
        return new CUFGlyph(new CUFGlyphDimensionImpl(d.getWidth(),
                                                  d.getHeight(),
                                                  d.getAdvanceWith(),
                                                  d.getY(),
                                                  getKerner(c),
                                                  getEdits(c)),
                            data);
    }

    private class KernerImpl implements Kerner {

        private final Character c1;
        private final StandardPhases.GetGlyphVector p;

        private KernerImpl (Character c) {
            PipeLineEnumeration ple = pipeLine.enumerate();
            p = (StandardPhases.GetGlyphVector) ple.nextElement();
            c1 = c;
        }

        @Override
        public Integer kerning (Character c2) {
            String str = new String(new char[] { c1, c2 });
            //try {
            GlyphVector vK = p.run(font, str);
            if (vK == null) {
                return null;
            }
            /*
            GlyphVector v0 = p.run(font.deriveFont(kernDelta), str);
            if(v0 ==null) { return null; }*/
            return vK.getGlyphPixelBounds(1, null, 0, 0).x;
            /*}
            catch (Exception e) {
            IOHelp.handleExceptions(SystemFontSource.class, "kerning", e,
            str);
            }*/
        }
    }

    @Override
    protected Phase[] phases () {
        return new Phase[] {
                    new StandardPhases.GetGlyphVector(),
                    new StandardPhases.AlignmentBox(),
                    new StandardPhases.ShapeShape(),
                    new StandardPhases.ShapeGlyph(),
                    new StandardPhases.PostProcess()
                };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<Phase>[] exposed () {
        return new Class[] {
                    StandardPhases.AligmentBox.phase(),
                    StandardPhases.ShapeShape.phase(),
                    StandardPhases.ShapeGlyph.phase(),
                    StandardPhases.PostProcess.phase()
                };
    }
}
