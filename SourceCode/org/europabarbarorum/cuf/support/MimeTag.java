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
package org.europabarbarorum.cuf.support;

import java.io.File;
import java.io.FileInputStream;

/**
 * Enumeration of known file types; containing:
 * <ul><li>“Magic words”: the header bytes that indentify a file type.
 * </li><li>Bitmasks: to provide precise control over the compared bits/bytes
 * </li></ul>
 * <p>
 * Typical usage of these objects is for file type identification through the
 * auxiliary method {@link #getType(java.io.File) }.
 *
 * @author Johan Ouwerkerk
 */
public enum MimeTag implements ResourceHelp.BundleKey {

    /**
     * Macro files start with 9 bytes: ASCII for “#CUFMacro”
     */
    MacroFile(new byte[] { (byte) 0x23,
                           (byte) 0x43,
                           (byte) 0x55,
                           (byte) 0x46,
                           (byte) 0x4D,
                           (byte) 0x61,
                           (byte) 0x63,
                           (byte) 0x72,
                           (byte) 0x6F },
              new byte[] { (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF }),
    /**
     * Ordered Strings files don't store their original keys. They start with 0x01000008: no ASCII there.
     */
    OrderedStringsFile(new byte[] { (byte) 0x01,
                                    (byte) 0x00,
                                    (byte) 0x00,
                                    (byte) 0x08 },
                       new byte[] { (byte) 0xFF,
                                    (byte) 0xFF,
                                    (byte) 0xFF,
                                    (byte) 0xFF }),
    /**
     * Keyed Strings files (those that store the original keys) start with 0x02000008. No ASCII there.
     */
    KeyedStringsFile(new byte[] { (byte) 0x02,
                                  (byte) 0x00,
                                  (byte) 0x00,
                                  (byte) 0x08 },
                     new byte[] { (byte) 0xFF,
                                  (byte) 0xFF,
                                  (byte) 0xFF,
                                  (byte) 0xFF }),
    /**
     * ZIP files (and JAR files too) start with 1 group of 4 bytes. The first
     * two are ASCII for "PK" after the original PKZIP file format.
     */
    ZipArchive(new byte[] { (byte) 0x50,
                            (byte) 0x4B,
                            (byte) 0x03,
                            (byte) 0x04 },
               new byte[] { (byte) 0xFF,
                            (byte) 0xFF,
                            (byte) 0xFF,
                            (byte) 0xFF }),
    /**
     * CUF files start with 1 group of 4 bytes. ASCII for “CUF0”.
     */
    CUFFont(new byte[] { (byte) 0x43,
                         (byte) 0x55,
                         (byte) 0x46,
                         (byte) 0x30 },
            new byte[] { (byte) 0xFF,
                         (byte) 0xFF,
                         (byte) 0xFF,
                         (byte) 0xFF }),
    /**
     * PFA files (ASCII PostScript fonts; aka PostScript Type1 fonts) start with 5 groups of 4 bytes. ASCII for: “%!PS-AdobeFont-1.0: ”.
     */
    Type1Font(new byte[] { (byte) 0x25,
                           (byte) 0x21,
                           (byte) 0x50,
                           (byte) 0x53,
                           (byte) 0x2D,
                           (byte) 0x41,
                           (byte) 0x64,
                           (byte) 0x6F,
                           (byte) 0x62,
                           (byte) 0x65,
                           (byte) 0x46,
                           (byte) 0x6F,
                           (byte) 0x6E,
                           (byte) 0x74,
                           (byte) 0x2D,
                           (byte) 0x31,
                           (byte) 0x2E,
                           (byte) 0x30,
                           (byte) 0x3A,
                           (byte) 0x20 },
              new byte[] { (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF,
                           (byte) 0xFF }),
    /**
     * TTF files stat with 5 bytes. No ASCII niceties.
     */
    TrueTypeFont(new byte[] { (byte) 0x00,
                              (byte) 0x01,
                              (byte) 0x00,
                              (byte) 0x00,
                              (byte) 0x00, },
                 new byte[] { (byte) 0xFF,
                              (byte) 0xFF,
                              (byte) 0xFF,
                              (byte) 0xFF,
                              (byte) 0xFF }),
    /**
     * OTF files aren't actually supported but common enough one might try to load them anyways.
     * The app should give a hint to convert to PFA files and then try the PFA instead.
     *
     * OTF files start with 1 group of 4 bytes. ASCII for “OTTO”.
     */
    OpenTypeFont(new byte[] { (byte) 0x4F,
                              (byte) 0x54,
                              (byte) 0x54,
                              (byte) 0x4F },
                 new byte[] { (byte) 0xFF,
                              (byte) 0xFF,
                              (byte) 0xFF,
                              (byte) 0xFF }), // this type is not supported!
    /**
     * Dummy entry to mark that a file type is not recognized.
     */
    FileTypeNotRecognized(new byte[] {}, new byte[] {});
    /**
     * Reference data in comparisons.
     * @see #matches(byte[])
     * @see #tag()
     */
    private final byte[] tag;
    /**
     * Collection of bitmasks to provide bit-level control over what is to be compared.
     * @see #matches(byte[])
     * @see #mask()
     */
    private final byte[] mask;

    /**
     * Constructs a MimeTag
     * @param tag array of bytes that are considered a file (MIME) signature.
     * @param mask array of integers defining bitmasks for bit-level control over what is compared
     * in the {@link #matches(byte[]) } method.
     */
    private MimeTag (byte[] tag, byte[] mask) {
        if (tag.length == mask.length) {
            this.tag = tag;
            this.mask = mask;
        }
        else {
            throw new IllegalArgumentException(
                    "Arrays must be of the same length!");
        }
    }

    /**
     * Get the reference data used in {@link #matches} to compare a given value against.
     * This corresponds to the first few bytes in a given input source that identify the source
     * as a certain MIME type.
     * @return an array of bytes that is used as unique signature to identify a file type.
     * @see #mask()
     */
    public byte[] tag () {
        return tag;
    }

    /**
     * Get the mask used in {@link #matches} to select individual bits for the comparison.
     * @return an array of bitmasks corresponding to the various bytes in the result of
     * {@link #tag() }.
     */
    public byte[] mask () {
        return mask;
    }

    /**
     * Check if a given byte buffer matches with this {@link MimeTag}.
     * Matching means that the buffer corresponds with the value of {@link #tag() } in the
     * bits defined by {@link #mask()}. In practice; a result of true means that a file (or
     * other input source) can be assumed to be a file of the MIME type denoted by this {@link MimeTag}.
     * @param otherValue an array of bytes, presumably of length equal to {@link #getBufferSize() }
     * read from some input source.
     * @return true if otherValue matches this {@link MimeTag}, false if not.
     */
    public boolean matches (byte[] otherValue) {
        if (this.tag.length > otherValue.length) {
            return false;
        }
        for (int i = 0; i < this.tag.length; ++i) {
            if ((otherValue[i] & this.mask[i]) != (this.tag[i] & this.mask[i])) {
                return false;
            }
        }
        return true;
    }
    /**
     * This field caches a value that determines the (minimum) value of the length
     * of a byte buffer so it can be compared to any byte buffer in any {@link MimeTag}.
     * @see #matches(byte[])
     */
    private static final int __size__;

    static {
        // determine the minimum size a byte array should have
        // so it can be compared to all MimeTags.

        int i = 0;
        MimeTag[] tags = MimeTag.values();

        for (MimeTag m : tags) {
            if (m.tag.length > i) {
                i = m.tag.length;
            }
        }
        __size__ = i;

    }

    /**
     * The length a byte buffer (byte array) should have so that it can be compared to any 
     * MimeTag buffer using the {@link #matches(byte[]) } method.
     * @return the value of the field {@link #__size__}
     */
    public static int getBufferSize () {
        return __size__;
    }

    private static MimeTag typeOf (File fileName) throws Exception {
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            byte[] buf = new byte[__size__];
            in.read(buf);

            for (MimeTag t : MimeTag.values()) {
                if (t.matches(buf)) {
                    return t;
                }
            }
            return FileTypeNotRecognized;
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Reads a portion of an input file to determine its file type.
     * This method attempts to read the first few bytes of the file; exactly how
     * many is determined by {@link #getBufferSize() }. If an exception should occur, or
     * if these bytes do not match with any {@link MimeTag} then this method returns null.
     * @param fileName the {@link File} to identify.
     * @return a {@link MimeTag} instance. Possibly {@link MimeTag#FileTypeNotRecognized} if the file type
     * could not be indentified.
     * @see #matches(byte[])
     */
    public static MimeTag getType (final File fileName) {
        try {
            return typeOf(fileName);
        }
        catch (Exception e) {
            IOHelp.handleExceptions(MimeTag.class,
                                    "getType",
                                    e,
                                    Messages.UnexpectedMimeError,
                                    fileName,
                                    e.getMessage());
            return FileTypeNotRecognized;
        }
    }

    @Override
    public Class type () {
        return MimeTag.class;
    }

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, MimeTag.class);
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, MimeTag.class, args);
    }

    /**
     * Create a suitable {@link IllegalArgumentException} to throw assuming that this
     * {@link MimeTag type} is the type {@link #getType(java.io.File) found} for the
     * given file. Intended usage:
     * <blockquote>
     * <pre>
     * {@code MimeTag t = MimeTag.getType(myFile);
     * switch(t) {
     *     case ...  break; // one valid type for myFile
     *     case ...  break; // another valid type
     *     default:  // invalid type found for myFile
     *         throw t.exception(myFile.toString());
     * }}
     * </pre>
     * </blockquote>
     * @param file the file which is the illegal argument.
     * @return an {@link IllegalArgumentException} to throw.
     */
    public IllegalArgumentException exception (String file) {
        return new IllegalArgumentException(
                Messages.MimeTypeInvalid.format(file, getText()));
    }

    /**
     * Check if a given {@link File} has this {@link MimeTag MIME type}.
     * @param file the file to check
     * @return true if the given file appears to have the expected MIME type.
     * Throws an {@link IllegalArgumentException} if not.
     * @see #getType(java.io.File) 
     */
    public boolean check ( File file) {
        MimeTag t =MimeTag.getType(file);
        if (this != t) {
            throw t.exception(file.toString());
        }
        else {
            return true;
        }
    }
}
