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
package org.europabarbarorum.cuf.gui.support;

import javax.swing.filechooser.FileNameExtensionFilter;
import org.europabarbarorum.cuf.gui.support.FileInput.FileDescription;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * Enumeration of a list of file types going by filename extensions. This enumeration is used to
 * obtain {@link FileDescription} objects.
 * Note that it is not intended to check for a MIME type with certainty, rather it is
 * supposed to be used for hinting the user at possible files of that type in a GUI.
 * @see #derive(boolean, boolean)
 * @author Johan Ouwerkerk
 */
public enum FileType implements BundleKey {

    /**
     * Strings file: .strings.bin.
     */
    Strings("strings.bin"),
    /**
     * XML data: .xml and .txt for (backwards) compatibility reasons.
     */
    XML("xml", "txt"),
    /**
     * XSLT stylesheet: .xsl.
     */
    XSLT("xsl"),
    /**
     * CUF font: .cuf.
     */
    CUF("cuf"),
    /**
     * Macro file: .macros.
     */
    Macros("macros"),
    /**
     * Supported font formats (post script type 1 and true type): .pfa, .ttf.
     */
    Font("ttf", "pfa");
    private final String[] extension;

    private FileType (String... exts) {
        this.extension = exts;
        final String _name = name();
        errorMessage = new BundleKey() {

            @Override
            public String getText () {
                return findText(this);
            }

            @Override
            public String name () {
                return _name + ".error";
            }

            @Override
            public Class type () {
                return FileType.class;
            }

            @Override
            public String format (Object... args) {
                return ResourceHelp.formatValue(this, FileType.class, args);
            }
        };
    }

    @Override
    public String getText () {
        return findText(this);
    }

    private String findText (BundleKey message) {
        return ResourceHelp.getValue(message, FileType.class);
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, FileType.class, args);
    }

    /**
     * Get the preferred extension for this {@link FileType}.
     * @return the preferred file name extension to use.
     */
    public String extension () {
        return extension[0];
    }

    @Override
    public Class type () {
        return FileNameExtensionFilter.class;
    }
    /**
     * A {@link BundleKey} to lookup error messages when file input for this
     * {@link FileType} is invalid.
     */
    public final BundleKey errorMessage;

    /**
     * Construct a {@link FileDescription} from this {@link FileType}.
     * @param optional whether the user must select a file in some UI or may omit this data.
     * @param includeAllTypes whether or not a UI for selecting files should allow the user to
     * select all file types/names.
     * @return a new {@link FileDescription} that uses this {@link FileType} to provide most
     * of its settings.
     */
    public FileDescription derive (final boolean optional,
                                   final boolean includeAllTypes) {

        return new FileDescription() {

            @Override
            public boolean isOptional () {
                return optional;
            }

            @Override
            public boolean allowDirectories () {
                return false;
            }

            @Override
            public boolean includeAllTypesFilter () {
                return includeAllTypes;
            }

            @Override
            public String[] getExtensions () {
                return extension;
            }

            @Override
            public String description () {
                return getText();
            }

            @Override
            public String errorMessage () {
                return errorMessage.getText();
            }
        };
    }
}
