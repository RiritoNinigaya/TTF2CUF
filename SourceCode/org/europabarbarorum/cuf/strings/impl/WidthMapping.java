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
package org.europabarbarorum.cuf.strings.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension;
import org.europabarbarorum.cuf.shell.FontToolkit;
import org.europabarbarorum.cuf.strings.impl.StringMapping.ConfigurationKey;
import org.europabarbarorum.cuf.strings.impl.StringMapping.ConfiguredMapping;
import org.europabarbarorum.cuf.strings.impl.StringMapping.LayoutMapping;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.NotEditableException;
import org.europabarbarorum.cuf.support.PathParser;
import org.europabarbarorum.cuf.support.PathParser.Glob;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;
import org.europabarbarorum.cuf.support.ResourceHelp.CharacterArrayResource;
import org.europabarbarorum.cuf.support.ResourceHelp.PositiveInteger;
import org.europabarbarorum.cuf.support.ResourceHelp.ResourceType;

/**
 * A {@link ConfiguredMapping} which is aware of width constraints on bodies of text,
 * and also attempts to layout text based on certain rules. For instance this
 * class provides the ability to use tabs for aligning text to horizontal offsets.
 * @author Johan Ouwerkerk
 */
public class WidthMapping extends EscapeMapping implements ConfiguredMapping,
                                                           LayoutMapping {

    // internal data:
    private CUFSource font; // font used for rendering text
    private final String uri; // namespace uri used for looking up attributes
    private int cursor = 0; // tracks current offset from 0 to width
    private int eventc = 0; // tracks the index of the last breakpoint
    private int eventt = 0; // tracks the index of the last character laid out
    private int tabCharWidth; // width of a single \t character in font
    // configurable attribute fields:
    private int width; // width of the UI element that displays the label/tex
    private int blockSize = 19; // see numTabs() for details
    private Character[] breaks = new Character[] {
        ControlCharacter.NonBreakingSpace.charValue
    }; // list of characters that may cause the a word to be pushed to the next line

    /**
     * Get the number of tab characters required to align the text to a positive number of blocks.
     * @return the number of tab characters required or -1 if the text cannot be aligned.
     */
    protected int numTabs () {
        int start = blockSize - (cursor % blockSize), cap = width - cursor;
        for (int k = start; k < cap; k += blockSize) {
            if (k % tabCharWidth == 0) {
                return k / tabCharWidth;
            }
        }
        return -1;
    }

    @Override
    public String indent () {
        int length = numTabs();
        if (length == -1) {
            return null;
        }
        else {
            char[] tabs = new char[length];
            Arrays.fill(tabs, ControlCharacter.Tab.charValue);
            cursor += (length * tabCharWidth);
            /*
             * (re)set some state to make sure that if a following
             * layout() call cannot break on a character prior to the tab
             * ... otherwise layout would be broken.
             */
            eventt += length;
            eventc = eventt;
            nobreaks = true;
            return new String(tabs);
        }
    }

    @Override
    public void advance (String text) {
        cursor = 0;
        nobreaks = true;
        eventt = text.length();
        eventc = eventt;
        for (int k = 0; k < eventt; ++k) {
            cursor += forChar(text.charAt(k));
        }
    }

    /**
     * Initialises data that depends on the font being used by this {@link WidthMapping}.
     * @param src the {@link CUFSource} which supplies information about the dimensions of
     * individual characters/glyphs, used for calculating text width/layout.
     */
    protected void init (CUFSource src) {
        this.font = src;
        this.tabCharWidth = forChar(ControlCharacter.Tab.charValue);
        if (tabCharWidth < 1) {
            throw new IllegalArgumentException(
                    Messages.WidthMappingTabWidthError.format(tabCharWidth));
        }
        if (tabCharWidth > 1) {
            IOHelp.warn(
                    WidthMapping.class,
                    Messages.WidthMappingTabWidthWarning,
                    tabCharWidth);
        }
    }

    /**
     * Implementation of the {@link Iterable} contract required by
     * {@link ConfiguredMapping}. This method allows other objects to walk the configuration
     * attributes of this {@link WidthMapping} in a for-each loop.
     * @return an {@link Iterator} of {@link ConfigurationKey} instances that represent the attributes used
     * for configuring this {@link WidthMapping}.
     */
    @Override
    public Iterator<ConfigurationKey> iterator () {
        return ConfigAttributes.getIterator(this);
    }

    /**
     * List of attributes that are required to configure a {@link WidthMapping}.
     */
    public static enum ConfigAttributes implements BundleKey {

        /**
         * Font, a string for use in {@link FontToolkit#fromFont(java.lang.String) }
         */
        font(new FontParser()),
        /**
         * Width, in pixels. A (positive) integer.
         */
        width(new PositiveInteger()),
        /**
         * Width of a horizontal tab in pixels. 
         * Used for horitzonally aligning text to the nearest multiple of this value.
         */
        tabWidth(new PositiveInteger()),
        /**
         * Characters that can be used to break lines for the sake of text layout.
         */
        breaks(false, new CharacterArrayResource());

        private ConfigAttributes (ResourceType type) {
            this(true, type);
        }

        private ConfigAttributes (boolean req, ResourceType type) {
            this.required = req;
            this.type = type;
        }

        private static class FontParser implements ResourceType<CUFSource> {

            private FontToolkit kit;

            private void init (WidthMapping context) {
                this.kit = new FontToolkit(context.pathParser);
            }

            @Override
            public CUFSource parse (String s) throws Exception {
                return kit.fromFont(s);
            }

            @Override
            public Class resourceType () {
                return CUFSource.class;
            }
        }
        private final boolean required;
        private final ResourceType type;

        private void set (String value, WidthMapping mapping) throws Exception {
            try {
                switch (this) {
                    case breaks:
                        mapping.breaks = (Character[]) type.parse(
                                mapping.process(value));
                        break;
                    case font:
                        ((FontParser) type).init(mapping);
                        mapping.init((CUFSource) type.parse(value));
                        break;
                    case width:
                        mapping.width = (Integer) type.parse(value);
                        break;
                    case tabWidth:
                        mapping.blockSize = (Integer) type.parse(value);
                        break;
                }
            }
            catch (Exception e) {
                throw new IllegalArgumentException(
                        Messages.WidthMappingAttributeInvalid.format(
                        name(),
                        value,
                        getText(),
                        e.getLocalizedMessage()));
            }
        }

        private ConfigurationKey getKeyFor (final WidthMapping mapping) {
            final ConfigAttributes attr = this;

            return new ConfigurationKey() {

                @Override
                public String name () {
                    return attr.name();
                }

                @Override
                public String uri () {
                    return mapping.uri;
                }

                @Override
                public void set (String value) throws Exception {
                    attr.set(value, mapping);
                }

                @Override
                public boolean required () {
                    return attr.required;
                }
            };
        }
        private static ConfigAttributes[] attrs = values();

        private static Iterator<ConfigurationKey> getIterator (
                final WidthMapping mapping) {

            return new Iterator<ConfigurationKey>() {

                private int index = 0;

                @Override
                public boolean hasNext () {
                    return index < attrs.length;
                }

                @Override
                public ConfigurationKey next () {
                    ConfigurationKey k = attrs[index].getKeyFor(mapping);
                    ++index;
                    return k;
                }

                @Override
                public void remove () {
                    throw new NotEditableException();
                }
            };
        }

        @Override
        public String getText () {
            return ResourceHelp.getValue(this, WidthMapping.class);
        }

        @Override
        public String format (Object... args) {
            return ResourceHelp.formatValue(this, WidthMapping.class, args);
        }

        @Override
        public Class type () {
            return WidthMapping.class;
        }
    }

    @Override
    public String layout (String source) {
        cursor = 0;
        nobreaks = true;
        eventt = 0;
        eventc = 0;
        return source;
    }

    /**
     * Finds a width offset/value for a given character to be used by this {@link WidthMapping}.
     * @param c the character to look up.
     * @return the allocated width of the given character in the {@link CUFSource}
     * used for computing text flow/layout (may be 0), or
     * 0 if the character is not supported by the font.
     * @see CUFSource#getGlyphDimension(java.lang.Object)
     * @see CUFGlyphDimension#getAdvanceWith()
     */
    @SuppressWarnings("unchecked")
    protected int forChar (Character c) {
        Object entry = font.getCharTable().get(c);
        CUFGlyphDimension dim = font.getGlyphDimension(entry);
        return dim == null ? 0 : dim.getAdvanceWith();
    }
    private boolean nobreaks = true;

    @Override
    public int layout (char c) {
        cursor += forChar(c);
        int result = cursor <= width ? NO_BREAK : eventc;
        ++eventt;
        if (isBreak(c)) {
            eventc = eventt;
            nobreaks = false;
        }
        if (nobreaks) {
            eventc = eventt;
        }
        return result;
    }

    private boolean isBreak (Character character) {
        for (Character c : breaks) {
            if (mappedEquals(c, character)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a {@link WidthMapping} for a given source file, using a given
     * font to calculate text width/layout.
     * @param macroFile a {@link File} encoding macros to use for performing macro transformations.
     * @param sourceXML file or directory which is to be used as basis for parsing relative file paths.
     * Typically this would be the file name of the source XML document.
     * @param uri the URI string associated with this {@link WidthMapping}.
     */
    public WidthMapping (File macroFile, File sourceXML, String uri) {
        this(macroFile, new Glob(sourceXML), uri);
    }

    /**
     * Creates a {@link WidthMapping} for a given source file, using a given
     * font to calculate text width/layout.
     * @param macroFile a {@link File} encoding macros to use for performing macro transformations.
     * @param parser a {@link PathParser} which provides context for parsing relative file paths.
     * @param uri the URI string associated with this {@link WidthMapping}.
     */
    public WidthMapping (File macroFile, PathParser parser, String uri) {
        super(macroFile);
        this.uri = uri;
        this.pathParser = parser;
    }
    private final PathParser pathParser;
}
