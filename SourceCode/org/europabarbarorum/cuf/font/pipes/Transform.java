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

import java.awt.Shape;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.europabarbarorum.cuf.font.impl.CUFGlyph;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension.CUFGlyphDimensionImpl;

/**
 * <p>Interface to describe an “operation” in a {@link Phase} of the “rendering pipeline”.
 * Multiple transform instances can be chained together in a {@link Phase} to create complex rendering pipelines.
 * For example you could combine applying an {@link UnderLineTransform} with a rotation and inversion operations.
 * </p>
 * <p>
 * Transform operations should be pure functions as much as possible: given the same input twice, the output results
 * must not differ. In particular the operation should not have side effects. The reason for this constraint is that
 * the order in which transform operations are applied depends on the {@link Phase} implementation
 * they are associated with. (The {@link Phase} interface does not stipulate order of operations.)
 * </p>
 * <p>
 * Note that the type parameters of a transform should match those of the {@link Phase} it is applied to.
 * Otherwise various exceptions may occur due to invalid cast operations.
 * </p>
 * @param <O> type of output this operation produces
 * @param <I> type of input this operation is based on
 * @param <C> type of context information argument that is used to pass around additional parameters
 * @see Phase#add(org.europabarbarorum.cuf.font.pipes.Transform)
 * @see Phase#set(org.europabarbarorum.cuf.font.pipes.Transform, int)
 * @author Johan Ouwerkerk
 */
public interface Transform<O, I, C> {

    /**
     * Apply a {@link Transform} operation to the given input.
     * @param args input argument, the object that is the result of previous {@link Transform} operations or input to
     * the {@link Phase} in which the operation occurs.
     * @param entry context argument to this {@link Transform}. Used to pass additional information around.
     * @return the result output of this {@link Transform}. Whether or not output is
     * valid is depends on the {@link Phase} in which the operation is performed.
     * @throws Exception if some error occurs. This signature simply makes it
     * explicit that a {@link Phase} should catch exceptions.
     * @see Phase#validate(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public O apply (final I args, final C entry) throws Exception;

    /**
     * <p>This interface describes a mechanism to provide compatibility between {@link TextAttribute} and
     * {@link Transform} objects. Internally the JVM uses a mapping of {@link TextAttribute} for describing fonts, and
     * more importantly some of the text decorations such as underline and strikethrough. </p><p>
     * This interface provides methods to check if a {@link java.awt.Font} contains attributes that would require
     * registering of the {@link Transform} in the CUF model. The intention is that CUF fonts that manipulate/use
     * {@link java.awt.Font} objects automatically register the required {@link Transform} instances.</p>
     */
    public interface ConditionalTransform {

        /**
         * Get the key to the {@link java.awt.Font#getAttributes() attributes} of a {@link java.awt.Font}.
         * @return the {@link TextAttribute} to check for in the font.
         */
        TextAttribute conditionalKey ();

        /**
         * Check whether or not the {@link ConditionalTransform} should be registered.
         * @param o the value obtained from looking up {@link #conditionalKey() } in {@link java.awt.Font#getAttributes() }.
         * @return true if this {@link Transform} should be used, false if not.
         */
        boolean isSatisfied (Object o);

        /**
         * Get the {@link Transform} that provides the compatibility.
         * @return the {@link Transform} that handles this bit of rendering compatibility.
         */
        Transform getTransform ();
    }

    /**
     * A {@link ConditionalTransform} implementation to provide underlines for {@link SystemFontSource}.
     * @see SingleLineTransform
     */
    public static class UnderLineTransform extends SingleLineTransform implements
            ConditionalTransform {

        @Override
        public Transform getTransform () {
            return this;
        }

        @Override
        public TextAttribute conditionalKey () {
            return TextAttribute.UNDERLINE;
        }

        @Override
        public boolean isSatisfied (Object o) {
            return o.equals(TextAttribute.UNDERLINE_ON);
        }

        @Override
        protected Float getOffset (FontInformation entry) {
            FontInformation.FontInformationKey<Float> k =
                    FontInformation.StandardFloatKeys.UnderlineOffset;
            return k.cast(entry.get(k));
        }

        @Override
        protected Float getThickness (FontInformation entry) {
            FontInformation.FontInformationKey<Float> k =
                    FontInformation.StandardFloatKeys.UnderlineThickness;
            return k.cast(entry.get(k));
        }
    }

    /**
     * A {@link ConditionalTransform} implementation to provide strikethroughs for {@link SystemFontSource}.
     * @see SingleLineTransform
     */
    public static class StrikeThroughTransform extends SingleLineTransform implements
            ConditionalTransform {

        @Override
        public Transform getTransform () {
            return this;
        }

        @Override
        public TextAttribute conditionalKey () {
            return TextAttribute.STRIKETHROUGH;
        }

        @Override
        public boolean isSatisfied (Object o) {
            return o.equals(TextAttribute.STRIKETHROUGH_ON);
        }

        @Override
        protected Float getOffset (FontInformation entry) {
            FontInformation.FontInformationKey<Float> k =
                    FontInformation.StandardFloatKeys.StrikeThroughOffset;
            return k.cast(entry.get(k));
        }

        @Override
        protected Float getThickness (FontInformation entry) {
            FontInformation.FontInformationKey<Float> k =
                    FontInformation.StandardFloatKeys.StrikeThroughThickness;
            return k.cast(entry.get(k));
        }
    }

    /**
     * A {@link Transform} that provides the “alignment box” used to vertically align glyphs
     * to a logical baseline. This type of {@link Transform} is used by {@link SystemFontSource} objects.
     * Note that this implementation exposes an additional method to configure vertical alignment.
     * @see #adjustVerticalAligment(double)
     */
    public static class AlignmentBox implements
            Transform<Rectangle2D, GlyphVector, GlyphMetrics> {

        @Override
        public Rectangle2D apply (GlyphVector gVector, GlyphMetrics gMetrics) throws
                Exception {
            Point2D p1 = gVector.getGlyphPosition(0);
            Point2D p2 = gVector.getGlyphPosition(1);

            Rectangle2D g2d = gMetrics.getBounds2D();
            Rectangle2D log = gVector.getGlyphVisualBounds(0).getBounds2D();
            double width = SystemFontSource.round(p2.getX() - p1.getX());
            double height = g2d.getHeight();
            g2d.setRect(p2.getX(),
                        p2.getY() /*- g2d.getY()*/ + vAlignment,
                        width == 0.0 ? log.getWidth() : width,
                        height == 0.0 ? log.getHeight() : height);
            return g2d;
        }

        /**
         * Set an adjustment factor to control vertical alignment. This
         * shifts the glyph up/down by the given amount, before any rounding occurs.
         * @param amount
         */
        public void adjustVerticalAligment (double amount) {
            this.vAlignment = amount;
        }
        private double vAlignment = 0.0;
    }

    /**
     * An implementation of {@link ShapeGlyph} 
     * which starts with an opaque pixel (defined as 0x100) and substracts from the alpha down to the final alpha value.
     * <p>
     * The algorithm used substracts a value if it determines for a 
     * combination of x coordinates from the set X { x0 -r, x0, x0 + r }, and 
     * y coordinates from the set Y { y0 -r , y0, y0 + r} where r is the given sampling radius and x0 and y0 the 
     * given x and y coordinate respectively that the position falls outside the glyph.
     * The result of these operations is returned, except if it is still 0x100 in which case 0xFF is returned.
     * </p>
     * <p>
     * The substracted value is calculated for each coordinate 
     * in terms of distance on the vertical axes expressed in r:
     * assuming a coordinate (v,w) then the value substracted is 0x40 for (v,w) equal to (x,y) 
     * but (0x100*r*r)/(2*2*|x-v|*|y-w|) if not. 
     * </p>
     * Note that this class exposes an additional method to control the radius.
     * @see #setSamplingRadius(double)
     */
    public static class SampleRadiusGlyph extends ShapeGlyph {

        @Override
        protected int alpha (Shape s, double x, double y) {
            int b0 = 0x100;

            for (double b1 = -1; b1 < 2; ++b1) {
                for (double b2 = -1; b2 < 2; ++b2) {
                    if (!s.contains(x + samplingRadius * b1,
                                    y + samplingRadius * b2)) {
                        b0 -= ((b1 == 0 && b2 == 0)
                               ? 0x40
                               : (0x10 * (b1 * b1 + b2 * b2)));
                    }
                }
            }
            return b0 == 0x100 ? 0xFF : b0;
        }

        /**
         * Adjust the radius factor being used to evaluate {@link #alpha(java.awt.Shape, double, double) }.
         * @param radius the new radius to use.
         * @see SampleRadiusGlyph the description of the algorithm
         */
        public void setSamplingRadius (double radius) {
            this.samplingRadius = radius;
        }
        private double samplingRadius = 0.4;
    }

    /**
     * An implementation of {@link ShapeGlyph}
     * which starts with a traslucent pixel and adds to the alpha up to the final alpha value.
     * <p>
     * The algorithm used divides a square with an area of 1 and center (x0, y0) where x0 and y0 are
     * given x and y coordinate respectively into r*r sub-squares where r is the given resolution.
     * </p>
     * <p>
     * For each such sub-square the algorithm adds 1/r to the value of the alpha if its center does
     * not fall outside the glyph. The end result is rounded to an integer and returned
     * except if it is larger than 0xFF in which case 0xFF is returned.
     * </p>
     * Note that this class exposes an additional method to control the resolution.
     * @see #setResolution(int)
     */
    public static class SubResolutionGlyph extends ShapeGlyph {

        @Override
        protected int alpha (Shape s, double x, double y) {
            int b0 = 0;
            double xm = x - 0.5;
            double ym = y - 0.5;
            double r = 1.0 / resolution;
            for (int k = 0; k < resolution; ++k) {
                for (int l = 0; l < resolution; ++l) {
                    if (s.contains(xm + k * r, ym + l * r)) {
                        ++b0;
                    }
                }
            }
            b0 =
                    SystemFontSource.round(
                    (0x100 * b0) / (resolution * resolution));
            return (b0 > 0xFF ? 0xFF : b0);
        }

        /**
         * Adjust the resolution being used to evaluate {@link #alpha(java.awt.Shape, double, double) }.
         * @param res the new resolution to use.
         * @see SubResolutionGlyph the description of the algorithm
         */
        public void setResolution (int res) {
            this.resolution = res;
        }
        private int resolution = 16;
    }

    /**
     * A {@link Transform} implementation to convert a {@link Shape} to a {@link CUFGlyph} for a {@link SystemFontSource}.
     */
    public static abstract class ShapeGlyph implements
            Transform<CUFGlyph, Shape, Rectangle2D> {

        /**
         * Hook for plugging in different rendering algorithms.
         * @param s the glyph shape object to render
         * @param x the x coordinate of the position (pixel) to render
         * @param y the y coordinate of the position (pixel) to render
         * @return an integer between 0 and 0xFF inclusive.
         */
        protected abstract int alpha (Shape s, double x, double y);

        @Override
        public CUFGlyph apply (Shape args, Rectangle2D box) throws Exception {
            Rectangle2D r2d = args.getBounds2D();
            double x = r2d.getX(), y = r2d.getY();
            double w = r2d.getWidth(), j = 0;
            double h = r2d.getHeight(), i = 0;

            ArrayList<Byte> list = new ArrayList<Byte>(((int) h) * ((int) w));

            w += x;
            h += y;
            for (i = y; i < h; i += 1.0) {
                for (j = x; j < w; j += 1.0) {
                    list.add((byte) (alpha(args, j, i) & 0xFF));
                }
            }

            int width = SystemFontSource.round(j - x),
                    height = SystemFontSource.round(i - y),
                    alloc_w = SystemFontSource.round(box.getWidth()),
                    alloc_h = SystemFontSource.round(height - box.getHeight() - y + box.
                    getY()),
                    c = 0;
            byte[] data = new byte[list.size()];

            for (Byte b : list) {
                data[c] = b;
                ++c;
            }

            return new CUFGlyph(
                    new CUFGlyphDimensionImpl(width,
                                          height,
                                          alloc_w,
                                          alloc_h),
                    data);
        }
    }

    /**
     * A base class {@link Transform} implementation to draw a line on a {@link Shape} from a {@link SystemFontSource}.
     * This class provides the drawing of a single line at a given offset with a given thickness obtainted from a {@link FontInformation}.
     * Subclasses must override
     * {@link #getOffset(org.europabarbarorum.cuf.font.pipes.FontInformation) } and {@link #getThickness(org.europabarbarorum.cuf.font.pipes.FontInformation) }
     * to extract these values from a given {@link FontInformation} object.
     */
    public static abstract class SingleLineTransform implements
            Transform<Shape, Shape, FontInformation> {

        /**
         * Get the thickness of the line to be drawn.
         * @param entry the {@link FontInformation} which provides that detail
         * @return the thickness of the line.
         */
        protected abstract Float getThickness (FontInformation entry);

        /**
         * Get the offset of the line to be drawn.
         * @param entry the {@link FontInformation} which provides that detail
         * @return the offset of the line.
         */
        protected abstract Float getOffset (FontInformation entry);

        @Override
        public Shape apply (Shape args, FontInformation entry) throws Exception {
            if (args == null) {
                return null;
            }
            final Float of = getOffset(entry);
            final Float tk = getThickness(entry);

            Area area = new Area(args);
            Rectangle2D r2d = area.getBounds2D();
            r2d.setRect(SystemFontSource.round(r2d.getX()),
                        SystemFontSource.round(of - tk),
                        SystemFontSource.round(r2d.getWidth()),
                        SystemFontSource.round(tk));

            area.add(new Area(r2d));
            return area;
        }
    }
}
