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

import java.io.File;
import java.io.RandomAccessFile;
import java.util.TreeMap;
import org.europabarbarorum.cuf.font.impl.FormatConstants;
import org.europabarbarorum.cuf.font.impl.CUFGlyph;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension.CUFGlyphDimensionImpl;
import org.europabarbarorum.cuf.font.impl.CUFSourceEditor.Edits;
import org.europabarbarorum.cuf.font.impl.CharTableEntry;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.font.impl.Kerner;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.MimeTag;

/**
 * Object to represent the contents of a CUF file as a font.
 * @author Johan Ouwerkerk
 */
public class CUFReader extends AbstractPhaseSource<CharTableEntry> {

    private Transform<CUFGlyph, RandomAccessFile, CharTableEntry> readT () {
        return new Transform<CUFGlyph, RandomAccessFile, CharTableEntry>() {

            @Override
            public CUFGlyph apply (RandomAccessFile args, CharTableEntry entry) throws
                    Exception {
                int offset = getGlyphOffset(entry.getCode());
                long start = offset + getGlyphDataOffset();

                CUFGlyphDimension dimension = getGlyphDimension(entry);
                args.seek(start);
                byte[] buf = new byte[dimension.getSize()];

                int r = args.read(buf, 0, dimension.getSize());
                if (r == dimension.getSize()) {
                    return new CUFGlyph(dimension, buf);
                }
                else {
                    throw new Exception(Messages.NotEnoughData.format(
                            start,
                            dimension.getSize(),
                            r));
                }
            }
        };
    }

    /**
     * Create a new {@link CUFReader}.
     */
    public CUFReader () {
        super(false);
        this.pipeLine.register(StandardPhases.ReadGlyph.phase(), readT());
    }
    private byte[] glyphOffsetTable = null;
    private byte[] glyphDimensionTable = null;
    private RandomAccessFile reader = null;

    /**
     * (Re-)initialise the reader to work with the given source.
     * @param source the source CUF File to work with.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init (String source) {
        if (reader != null) {
            // release resources, or at least ensure that they will be when GC hits:
            prepared = false;
            close();
            charTable = null;
            glyphDimensionTable = null;
            glyphOffsetTable = null;
            cufSource = null;
        }
        open(source);
    }

    /**
     * Open the CUF File and init parser
     * @param source path to file to read from
     * @return whether or not this method succeeded
     */
    private void open (final String source) {
        try {
            boolean doParse = !source.equals(cufSource); // required to set up parser?
            if (reader != null && doParse) { // if locked to another file
                throw new Exception(Messages.FileConflict.format(source,
                                                                 cufSource));
            }
            else {
                if (doParse) { // reader is null ergo not initialized/created yet.
                    cufSource = source;
                    reader = new RandomAccessFile(new File(source), "r");
                    initParser(); // this will set prepared appropriately
                }
            }
        }
        catch (Exception e) {
            IOHelp.handleExceptions(CUFReader.class, "open", e,
                                    e.getMessage());
            storeError(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void deriveCharTable () {
        byte[] bs = readCharTable();
        charTable = new TreeMap<Character, CharTableEntry>();

        for (int canCode = 0, i = 0; i < bs.length; i += 2) {

            canCode = IOHelp.endianConversion(bs, i, 2);

            if (canCode != FormatConstants.__LIMIT__.value()) {
                Character c = IOHelp.fromCode(i >> 1);

                charTable.put(c, new CharTableEntry(canCode, c));
            }
        }
    }

    /**
     * Get the offset at which the glyph bitmaps are stored in the CUF file
     * @return offset of the bitmap data section of the CUF file
     */
    public int getGlyphDataOffset () {
        return FormatConstants.START_OF_GLYPH_META_TABLES.value()
                + (getCUFProperties(CUFProperty.NumberOfGlyphs) << 3);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CUFGlyph getGlyph (CharTableEntry entry) {
        PipeLineEnumeration ple = pipeLine.enumerate();
        CUFGlyph g = null;
        while (ple.hasMoreElements()) {
            Phase p = ple.nextElement();
            switch (ple.index()) {
                case 1:
                    g = ((Phase<CUFGlyph, RandomAccessFile, CharTableEntry>) p).
                            run(
                            reader, entry);
                    break;
                case 2:
                    if (g != null) {
                        g = new CUFGlyph(g.getDimension(),
                                         ((Phase<byte[], byte[], Void>) p).run(g.
                                getBitMapData(), null));
                    }
                    break;
            }
        }
        return g;
    }

    /**
     * Fetches the CUF properties table.
     * @return the bytes representing the CUF properties table
     */
    public byte[] getCUFTable () {
        return safeRead(MimeTag.CUFFont.tag().length,
                        FormatConstants.CUF_TABLE_SIZE.value());
    }

    /**
     * Set up the {@link #CUFProperties}, and verify the CUF File using {@link #checkMagicWord}.
     */
    private void initParser () {
        this.prepared = false;

        byte[] header = this.getCUFTable();
        if (tainted()) {
            return;
        }

        CUFProperties = new int[FormatConstants.NumCUFProps.value()];
        int offset = 0;
        for (CUFProperty key : CUFProperty.values()) {
            // little to big endian conversion required
            CUFProperties[key.index()] = IOHelp.endianConversion(header, offset, key.
                    byteAmount());
            offset += key.byteAmount();
        }
        readKerningInfo();
        if (tainted()) {
            return;
        }
        this.getEditor().toggleKerning(this.supportsKerning());
        this.prepared = true;
    }

    /**
     * Get the offset of a glyph in the CUF File relative to the start of bitmap data in the file.
     * This method works by reading from a pre-cached glyph offset table returned by {@link #getGlyphOffsetTable}.
     * This means that this method avoids doing a single small file read in favour of performing one (large) read
     * of the entire table for faster best/average performance when used repeatedly.
     *
     * @param charCode index of the glyph in the character table
     * @return offset data in decoded form
     */
    public int getGlyphOffset (int charCode) {
        byte[] data = getGlyphOffsetTable();
        return IOHelp.endianConversion(data, charCode << 2, 4);
    }

    /**
     * Get the dimension of a glyph in the CUF File.
     * This method works by reading from a pre-cached glyph dimension table returned by {@link #getGlyphDimensionTable}.
     * This means that this method avoids doing a single small file read in favour of performing one (large) read
     * of the entire table for faster best/average performance when used repeatedly.
     *
     * @param entry object representing the glyph's entry in the {@link #charTable}.
     * @return object representing decoded dimension data
     */
    @Override
    public CUFGlyphDimension getGlyphDimension (CharTableEntry entry) {
        if (entry != null) {
            byte[] data = getGlyphDimensionTable();

            return convertToDimension(entry, data);
        }
        else {
            return null;
        }
    }

    private Edits getEdits (CharTableEntry code) {
        return getEdits(code.getChar());
    }

    private Kerner createKerner (CharTableEntry code) {
        if (supportsKerning()) {
            return new KernerImpl(code.getCode());
        }
        return null;
    }

    private class KernerImpl implements Kerner {

        private final int k;

        private KernerImpl (int code) {
            this.k = offset(code);
        }

        private int map (int code) {
            return code - kerningProperties[FormatConstants.KerningSkip.value()];
        }

        private int offset (int code) {
            return map(code) * kerningProperties[FormatConstants.KerningSize.
                    value()];
        }

        @Override
        public Integer kerning (Character c2) {
            CharTableEntry cte = getCharTable().get(c2);
            return cte == null ? null : lookupKerning(k,
                                                      map(cte.getCode()));
        }

        private Integer lookupKerning (int offset, int code2) {
            if (offset < 0 || code2 < 0) {
                return null;
            }
            byte[] buf = safeRead(offset + startKerningData() + code2, 1);
            return buf == null ? null : IOHelp.byteConversion(buf, 0, 1, false);
        }
    }

    private int startKerningInfo () {
        return getGlyphDataOffset() + getCUFProperties(CUFProperty.GlyphDataSize);
    }

    private int startKerningData () {
        return startKerningInfo() + FormatConstants.KerningPropTableSize.value();
    }

    @Override
    public boolean supportsKerning () {
        return kerningProperties != null;
    }
    private int[] kerningProperties = null;

    private void readKerningInfo () {
        try {
            reader.seek(startKerningInfo());
            int k = FormatConstants.KerningPropTableSize.value();
            byte[] bs = new byte[k];
            int r = reader.read(bs);

            if (r == k) {
                FormatConstants[] fcs = new FormatConstants[] {
                    FormatConstants.KerningSize,
                    FormatConstants.KerningSkip
                };
                kerningProperties = new int[fcs.length];
                for (FormatConstants fc : fcs) {
                    kerningProperties[fc.value()] = IOHelp.byteConversion(bs, fc.
                            value() * 2, 2, true);
                }
            }
        }
        catch (Exception e) {
            IOHelp.handleExceptions(CUFReader.class,
                                    "readKerningInfo", e, cufSource);
            storeError(e);
        }
    }

    /**
     * Convert raw bytes at an offset to a {@link cuf.core.CUFGlyphDimension}.
     * @param data bytes containing the dimension information
     * @param offset offset at which the data is encoded
     * @return the object representing the data in decoded form
     * @see FormatConstants#GlyphAllocHeight
     * @see FormatConstants#GlyphAllocWidth
     * @see FormatConstants#GlyphHeight
     * @see FormatConstants#GlyphWidth
     */
    private CUFGlyphDimension convertToDimension (CharTableEntry code,
                                                  byte[] data) {
        int offset = code.getCode() << 2;
        return new CUFGlyphDimensionImpl(
                data[offset + FormatConstants.GlyphWidth.value()] & 0xFF,
                data[offset + FormatConstants.GlyphHeight.value()] & 0xFF,
                data[offset + FormatConstants.GlyphAllocWidth.value()] & 0xFF,
                IOHelp.sign(
                data[offset + FormatConstants.GlyphAllocHeight.value()] & 0xFF,
                1),
                createKerner(code),
                getEdits(code));
    }

    /**
     * Fetches the table encoding dimensions of glyphs in the CUF File
     * @return bytes in the CUF File representing this dimension table
     */
    public byte[] getGlyphDimensionTable () {
        if (glyphDimensionTable == null) {
            int i = getCUFProperties(CUFProperty.NumberOfGlyphs);
            glyphDimensionTable = safeRead(
                    FormatConstants.START_OF_GLYPH_META_TABLES.value(),
                    (i << 2));
        }
        return glyphDimensionTable;
    }

    /**
     * Fetches the table encoding the offsets of glyphs in the CUF File.
     * @return bytes in the CUF File representing this offset table
     */
    public byte[] getGlyphOffsetTable () {

        if (glyphOffsetTable == null) {
            int length = getCUFProperties(CUFProperty.NumberOfGlyphs) << 2;
            glyphOffsetTable = safeRead(
                    FormatConstants.START_OF_GLYPH_META_TABLES.value() + length,
                    length);
        }
        return glyphOffsetTable;
    }

    /**
     * Fetches the character table
     * @return the bytes representing the character table in the CUF File
     */
    public byte[] readCharTable () {

        return safeRead(FormatConstants.HEADER_SIZE.value(),
                        FormatConstants.START_OF_GLYPH_META_TABLES.value()
                - FormatConstants.HEADER_SIZE.value());
    }

    @Override
    protected Phase[] phases () {
        return new Phase[] {
                    new StandardPhases.GetGlyph(),
                    new StandardPhases.PostProcess()
                };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<Phase>[] exposed () {
        return new Class[] {
                    StandardPhases.PostProcess.phase()
                };
    }

    /**
     * Close the underlying {@link RandomAccessFile}.
     */
    public void close () {
        try {
            reader.close();
        }
        catch (Exception e) {
            IOHelp.handleExceptions(CUFReader.class, "close", e, e.getMessage());

        }
        reader = null;
    }

    /**
     * Check if a (fatal) IO error has occured so far. Any fatal IO error is stored by the
     * {@link CUFReader} in a field for future examination, and any subsequent IO operations
     * fail if this method returns true.
     * @return whether or not an error has been stored.
     */
    public boolean tainted () {
        return err != null;
    }

    /**
     * Store an error, marking this {@link CUFReader} as tainted.
     * @param e the error to store for future examination.
     * @see #tainted()
     * @see #getError()
     */
    protected void storeError (Exception e) {
        prepared = false;
        if (err == null) {
            this.err = e;
        }
    }

    /**
     * Get the error that is stored for future examination.
     * @return the value of {@link #err}; possibly null if nothing was stored.
     * @see #storeError(java.lang.Exception)
     * @see #tainted()
     */
    public Exception getError () {
        return err;
    }
    /**
     * Error stored for examination.
     */
    private Exception err;

    /**
     * Checks if this {@link CUFReader} has been tainted by previous (fatal) errors.
     * This method throws an {@link IllegalArgumentException} if the object is tainted.
     * It is intended to be used for aborting operations if a previous error had occured.
     * @param method method name that wishes to check this {@link CUFReader}.
     * @see #tainted()
     */
    public void checkTainted (String method) {
        if (tainted()) {
            throw new IllegalStateException(Messages.IOTainted.format(
                    method,
                    err.getLocalizedMessage()),
                                            err);
        }
    }

    private byte[] unsafeRead (final long from, final int length) throws
            Exception {
        int got = 0;
        reader.seek(from);
        byte[] buf = new byte[length];
        got = reader.read(buf);
        if (got == length) {
            return buf;
        }
        throw new Exception(Messages.NotEnoughData.format(from, length, got));
    }

    private byte[] safeRead (final long from, final int length) {
        try {
            checkTainted("safeRead");
            return unsafeRead(from, length);
        }
        catch (Exception e) {
            IOHelp.handleExceptions(CUFReader.class, "safeRead",
                                    e, e.getMessage());
            storeError(e);
            return null;
        }
    }
}
