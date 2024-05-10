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
package org.europabarbarorum.cuf.shell;

import java.awt.font.TextAttribute;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.europabarbarorum.cuf.font.CUFSource;

/**
 * A class to hide the {@link TextAttribute}, {@link HashMap} from view in
 * font loading, and also to provide more explicit type safety of the values for
 * the {@link TextAttribute} keys.
 * @author Johan Ouwerkerk
 */
public final class FontStyle extends HashMap<TextAttribute, Object> {

    /**
     * Creates an empty {@link FontStyle}. This is mostly useful if you intend to use
     * it for a delta map in
     * {@link FontToolkit#fromFile(java.lang.String, java.util.Map) }.
     */
    public FontStyle () {
    }

    /**
     * Creates a minimal {@link FontStyle} for a functional font. This constructor is only
     * useful for obtaining a stand-alone {@link CUFSource} later on: it is not useful for
     * specifying a delta map in
     * {@link FontToolkit#fromFile(java.lang.String, java.util.Map) }.
     * @param family a string which specifies either a font file on disk, or the
     * font family (name) of an installed system font.
     * @param size the size (in points) which should be used. A value of 72
     * (points) should correspond to one inch here.
     */
    public FontStyle (String family, float size) {
        select(family);
        width(TextAttribute.WIDTH_REGULAR);
        weight(TextAttribute.WEIGHT_REGULAR);
        put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
        size(size);
    }

    /**
     * Specify a size in points.
     * A value of 72 (points) should correspond to one inch here.
     * @param size the new size to use.
     */
    public void size (float size) {
        put(TextAttribute.SIZE, size);
    }

    /**
     * Selects the source (system font or file path) to use for obtaining the font.
     * @param family a font family (name) of an installed system font,
     * or a file path of a font on disk.
     */
    public void select (String family) {
        put(TextAttribute.FAMILY, family);
    }

    /**
     * Request an oblique (slanted) posture for the font.
     */
    public void oblique () {
        put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
    }

    /**
     * Shorthand for requesting a bold font.
     */
    public void bold () {
        weight(TextAttribute.WEIGHT_BOLD);
    }

    /**
     * Request an underline for the font.
     */
    public void underline () {
        put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    }

    /**
     * Request a strikethrough for the font.
     */
    public void strikethrough () {
        put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
    }

    /**
     * Requests that kerning be performed.
     */
    public void requestKerning () {
        put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    }

    /**
     * Shorthand for requesting a extended font.
     */
    public void extended () {
        width(TextAttribute.WIDTH_EXTENDED);
    }

    /**
     * Shorthand for requesting a condensed font.
     */
    public void condensed () {
        width(TextAttribute.WIDTH_CONDENSED);
    }

    /**
     * Use introspection to obtain the value of a static {@link TextAttribute} field.
     * @param prefix the prefix of the field name e.g. “WEIGHT_”. This is case sensitive; and
     * required for resolving shorthand names.
     * @param name the name of the field requested e.g. “WEIGHT_BOLD” or “BOLD”.
     * This is not case sensitive: it is converted to all upper case before being used.
     * If the name does not start with the given prefix, then this
     * prefix is added to it first.
     * @return the value of the given field.
     * @throws IllegalArgumentException if the field does not exist.
     */
    protected Object ispectAttrField (String prefix, String name) {
        name = name.toUpperCase();
        name = name.startsWith(prefix) ? name : prefix + name;
        try {
            Field f = TextAttribute.class.getField(name);
            return f.get(null);
        }
        catch (Exception e) {
            throw new IllegalArgumentException(name, e);
        }
    }

    /**
     * Request a specific thickness (weight) for strokes in the font.
     * @param weight a factor that determines weight of strokes.
     * Regular corresponds to 1.0, bold to 2.0.
     * @see TextAttribute
     */
    public void weight (float weight) {
        put(TextAttribute.WEIGHT, weight);
    }

    /**
     * Request a specific width of the glyphs/strokes in the font.
     * @param width a factor that determines the width of strokes.
     * @see TextAttribute
     */
    public void width (float width) {
        put(TextAttribute.WIDTH, width);
    }

    /**
     * Request a specific width of the glyphs/strokes in the font.
     * @param width the name of the width value, e.g. “semi_condensed” (case insensitive).
     * @see TextAttribute
     */
    public void width (String width) {
        put(TextAttribute.WIDTH, ispectAttrField("WIDTH_", width));
    }

    /**
     * Request a specific thickness (weight) for strokes in the font.
     * @param weight the name of the weight value, e.g. “BOLD” (case insensitive).
     * @see TextAttribute
     */
    public void weight (String weight) {
        put(TextAttribute.WIDTH, ispectAttrField("WEIGHT_", weight));
    }

    /**
     * Shorthand for obtaining the specified font family or file path.
     * @return the font family or file path used by this {@link FontStyle}.
     */
    public String load () {
        Object name = get(TextAttribute.FAMILY);
        return name == null ? null : name.toString();
    }

    /**
     * Get a {@link CUFSource} represented by this {@link FontStyle}.
     * @param context the {@link Shell} which provides context for resolving
     * relative file names.
     * @return a {@link CUFSource} that represents this {@link FontStyle}.
     * Note that the underlying JVM may use defaults for some arguments if they are found invalid (e.g.
     * if a font is requested that is not available the JVM may substitute a default font).
     */
    public CUFSource getFont (Shell context) {
        FontToolkit kit = new FontToolkit(context);
        return getFont(kit);
    }

    /**
     * Get a {@link CUFSource} represented by this {@link FontStyle}.
     * @param kit the {@link FontToolkit} which is used to create the font.
     * @return a {@link CUFSource} that represents this {@link FontStyle}.
     * Note that the underlying JVM may use defaults for some arguments if they are found invalid (e.g.
     * if a font is requested that is not available the JVM may substitute a default font).
     */
    public CUFSource getFont (FontToolkit kit) {
        String load = load();
        if (load != null) {
            File f = kit.file(load);
            if (f != null && f.exists()) {
                return kit.fromFile(load(), this);
            }
        }
        return kit.fromFont(this);
    }
}
