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

import java.io.BufferedOutputStream;
import org.europabarbarorum.cuf.font.impl.FormatConstants;
import org.europabarbarorum.cuf.font.impl.CUFGlyph;
import org.europabarbarorum.cuf.font.impl.CharTableEntry;
import org.europabarbarorum.cuf.font.impl.CUFGlyphDimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.support.CompileJob.FileJob;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.MimeTag;
import org.europabarbarorum.cuf.support.OptionMap;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * Object to write out a font in CUF format.
 * @param <C> the kind of {@link CharTableEntry} used by the compiled {@link CUFSource}.
 * @author Johan Ouwerkerk
 */
public class CUFWriter<C> extends FileJob {

    private CUFSource<C> data;
    // buffer to hold meta data, at most ~128KB or so.
    private byte[] glyphMetaTables;
    // shadowed cuf properties
    private int glyphDataSize = 0;
    private int vsize = 0;
    private int hsize = 0;
    private final File outfile;
    // character tables
    private final TreeMap<Character, Integer> cTable;  // chartable
    private final TreeMap<Character, KernInfo> kTable; // kerning table
    private final OptionMap options;

    /**
     * Creates a {@link CUFWriter} instance.
     * @param data the {@link CUFSource} to compile
     * @param resultFile the destination to store results in.
     * @param opts a {@link OptionMap} of {@link DefaultOption} class keys and corresponding values to
     * configure {@link CUFWriter} behaviour.
     */
    public CUFWriter (CUFSource<C> data, File resultFile, OptionMap opts) {
        super(Messages.JobTitle.format(resultFile), resultFile);
        if (data == null) {
            throw CUFExceptions.NoFontToCompile.illegalArgument();
        }
        if (resultFile == null) {
            throw CUFExceptions.NoFontToCompile.illegalArgument();
        }
        this.data = data;
        this.options = opts;
        this.outfile = resultFile;
        this.cTable = new TreeMap<Character, Integer>();
        this.kTable = data.kerningEnabled() ? new TreeMap<Character, KernInfo>() : null;
    }

    @Override
    protected void compile () throws Exception {
        postUpdate(Messages.Start, outfile);
        File tempfile = createTempFile("temporary-cuf-file-", ".cuf",
                                       null);
        stage1(tempfile);
        stage2(tempfile, outfile);
        tempfile.delete();
    }

    /**
     * Checks if a shadow copy of a given CUF property should be used.
     * @param prop the {@link CUFProperty} to check.
     */
    private boolean useAutoValue (CUFProperty prop) {
        switch (prop) {
            case VSize:
                return options.getOption(SetVSize.class) == SetVSize.Enabled;
            case HSize:
                return options.getOption(SetHSize.class) == SetHSize.Enabled;
            case GlyphDataSize:
                return options.getOption(SetGlyphDataSize.class) == SetGlyphDataSize.Enabled;
            default:
                return false; // shadowing not available.
        }
    }

    /**
     * Controls whether or not a value will be automatically generated/overwritten for a given {@link CUFProperty}
     * if the given {@link OptionMap} is used.
     * @param prop the {@link CUFProperty}  the property to configure.
     * @param enabled whether or not the value for the property will be drawn from the {@link CUFSource} or from what
     * shadow copies are generated in a {@link CUFWriter}.
     * @param opts the {@link OptionMap} to initialise.
     */
    public static void useAutoValue (CUFProperty prop, boolean enabled,
                                     OptionMap opts) {
        switch (prop) {
            case HSize:
                opts.put(SetHSize.class,
                         enabled ? SetHSize.Enabled : SetHSize.Disabled);
                break;
            case GlyphDataSize:
                opts.put(SetGlyphDataSize.class,
                         enabled ? SetGlyphDataSize.Enabled : SetGlyphDataSize.Disabled);
                break;
            case VSize:
                opts.put(SetVSize.class,
                         enabled ? SetVSize.Enabled : SetVSize.Disabled);
                break;
            default:
                break;
        }
    }

    /**
     * Looks up a shadow copy for a given CUF property.
     * @param prop the {@link CUFProperty} to lookup.
     */
    private Integer autoValue (CUFProperty prop) {
        switch (prop) {
            case VSize:
                return vsize;
            case HSize:
                return hsize;
            case GlyphDataSize:
                return glyphDataSize;
            default:
                return null; // shadowing not available
        }
    }

    private byte[] prepareCUFProperties () throws Exception {

        byte[] CUFProperties = new byte[FormatConstants.CUF_TABLE_SIZE.value()];
        int offset = 0;
        Integer raw;
        boolean use;
        for (CUFProperty key : CUFProperty.values()) {
            use = useAutoValue(key);
            if (use || data.isAvailable(key)) {
                raw = use
                        ? autoValue(key)
                        : data.getCUFProperties(key);
                if (raw == null) {
                    throw CUFExceptions.PropertyValueError.create(key.getText());
                }
                raw = key.translator.setTranslate(raw);
                if (raw == null) {
                    throw CUFExceptions.PropertyValueError.create(key.getText());
                }
                CUFProperties = IOHelp.fillBuffer(raw,
                                                  offset,
                                                  key.byteAmount(),
                                                  CUFProperties,
                                                  true);
            }
            else {
                IOHelp.Log.Debug.log(CUFWriter.class,
                                     String.format("[Value missing] : %1$s",
                                                   key.getText()));
            }
            offset += key.byteAmount();
        }
        return CUFProperties;
    }

    private void stage1 (File outfile) throws Exception {
        postUpdate(Messages.Stage1);

        BufferedOutputStream writer = null;
        try {
            writer = new BufferedOutputStream(new FileOutputStream(
                    outfile));
            postUpdate(Messages.OpenTemp, outfile);
            postUpdate(Messages.WriteGlyphs, outfile);
            convert(writeGlyphs(writer));
        }
        finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    class KernInfo {

        private final CUFGlyphDimension dim;

        private KernInfo () {
            this.dim = null;
        }

        private boolean nulltype () {
            return this.dim == null;
        }

        private KernInfo (CUFGlyphDimension dim) {
            this.dim = dim;
        }

        private void write (BufferedOutputStream writer, Set<Character> order,
                            int def) throws
                Exception {
            byte[] buf = new byte[order.size()];
            if (nulltype()) {
                Arrays.fill(buf, cast(def));
            }
            else {
                int i = 0;
                for (Character c : order) {
                    buf[i] = cast(dim.kerning(c));
                    ++i;
                }
            }
            writer.write(buf);
        }
    }

    private void kern (Character c, CUFGlyphDimension dim) {
        if (this.data.kerningEnabled()) {
            int def = dim.getAdvanceWith(), w;
            KernInfo info = kTable.get(c);
            for (Character k : data.getCharTable().keySet()) {
                w = dim.kerning(k);
                if (w != def) {
                    if (info == null || info.nulltype()) {
                        info = new KernInfo(dim);
                        kTable.put(c, info);
                    }
                    if (!kTable.containsKey(k)) {
                        kTable.put(k, new KernInfo());
                    }
                }
            }
        }
    }

    private int extractAdvanceWidth (int dim) throws Exception {
        return dim & dimensionComponent(FormatConstants.GlyphAllocWidth, 0xFF);
    }

    private int[][] padKerning (int[][] tables, BufferedOutputStream writer) throws
            Exception {
        postUpdate(Messages.OrderChars);
        // copy of original tables which will be the re-ordered result
        int[][] tables2 = new int[][] { new int[tables[0].length],
                                        new int[tables[1].length] };

        Set<Character> set = cTable.keySet();
        int code = 0, oldCode;

        // re-order charactes so that those without kerning are pulled to the front
        for (Character c : set) {
            if (!this.data.kerningEnabled() || !kTable.containsKey(c)) {

                oldCode = cTable.get(c);
                tables2[0][code] = tables[0][oldCode];
                tables2[1][code] = tables[1][oldCode];
                cTable.put(c, code);
                ++code;
            }
        }

        if (this.data.kerningEnabled()) {
            postUpdate(Messages.PrepareKerning);
            set = kTable.keySet();
            int kz = kTable.size(); // number of glyphs with kerning

            // write kerning meta data, `code' will be the correct offset
            writer.write(IOHelp.fillBuffer(kz, 0, 2, new byte[2], true));
            writer.write(IOHelp.fillBuffer(code, 0, 2, new byte[2], true));

            // add any characters with kerning tables to the end
            postUpdate(Messages.OrderChars);
            for (Character c : set) {
                oldCode = cTable.get(c);
                tables2[0][code] = tables[0][oldCode];
                tables2[1][code] = tables[1][oldCode];
                cTable.put(c, code);
                //kTable.get(c).pad(); // ensure uniform lenght of kerning tables
                ++code;
            }

            // reorder characters in kerning tables so they are output in order of glyph code.
            TreeSet<Character> kSet = new TreeSet<Character>(new Comparator<Character>() {

                @Override
                public int compare (Character o1, Character o2) {
                    return cTable.get(o1) - cTable.get(o2);
                }
            });
            // add any characters for which kerning is used
            kSet.addAll(set);
            code = 1;
            // write kerning tables
            for (Character c : kSet) {

                kTable.get(c).write(writer,
                                    kSet,
                                    extractAdvanceWidth(
                        tables2[0][cTable.get(c)]));
                postUpdate(code, kz, Messages.WriteKerning, code, kz);
                ++code;
            }
        }
        return tables2;
    }

    private void convert (int[][] metaTables) throws Exception {
        this.glyphMetaTables = new byte[(metaTables[0].length << 3)];

        int ptr = 0;
        for (int[] table : metaTables) {
            for (int cell : table) {
                this.glyphMetaTables = IOHelp.fillBuffer(cell,
                                                         ptr,
                                                         4,
                                                         this.glyphMetaTables,
                                                         true);
                ptr += 4;
            }
        }

    }

    /**
     * Controls behaviour of {@link CUFWriter} when {@link CUFProperty#VSize} is written to a file.
     */
    public static enum SetVSize implements DefaultOption {

        /**
         * Value will be automatically generated/overwritten.
         */
        Enabled,
        /**
         * Value will not be automatically generated/overwritten.
         */
        Disabled;

        @Override
        public DefaultOption defaultOption () {
            return Enabled;
        }
    }

    /**
     * Controls behaviour of {@link CUFWriter} when {@link CUFProperty#HSize} is written to a file.
     */
    public static enum SetHSize implements DefaultOption {

        /**
         * Value will be automatically generated/overwritten.
         */
        Enabled,
        /**
         * Value will not be automatically generated/overwritten.
         */
        Disabled;

        @Override
        public DefaultOption defaultOption () {
            return Enabled;
        }
    }

    /**
     * Controls behaviour of {@link CUFWriter} when {@link CUFProperty#GlyphDataSize} is written to a file.
     */
    public static enum SetGlyphDataSize implements DefaultOption {

        /**
         * Value will be automatically generated/overwritten.
         */
        Enabled,
        /**
         * Value will not be automatically generated/overwritten.
         */
        Disabled;

        @Override
        public DefaultOption defaultOption () {
            return Enabled;
        }
    }

    /**
     * A list of standard {@link BundleKey exception messages} that a {@link CUFWriter}
     * uses to signal errors.
     */
    public static enum CUFExceptions implements BundleKey {

        /**
         * Emitted when a font incorrectly implements get/set contract for 
         * {@link CUFProperty CUF properties}. Takes the {@link CUFProperty} as argument. 
         */
        PropertyValueError,
        /**
         * Emitted when either file or font arguments to the constructor are null.
         * Takes no arguments
         */
        NoFontToCompile,
        /**
         * Emitted when a font contains no glyphs to write. Takes a `size' argument.
         */
        NoGlyphsToWrite,
        /**
         * Emitted when a font uses a value for a byte field/attribute which does not
         * fall within byte range. Takes the value as argument.
         */
        ByteValueError;

        @Override
        public String getText () {
            return ResourceHelp.getValue(this, CUFWriter.class);
        }

        @Override
        public Class type () {
            return CUFExceptions.class;
        }

        @Override
        public String format (Object... args) {
            return ResourceHelp.formatValue(this, CUFWriter.class, args);
        }

        IllegalArgumentException illegalArgument () {
            return new IllegalArgumentException(format());
        }

        Exception create (Object... args) {
            return new Exception(format(args));
        }

        Exception create (Throwable cause, Object... args) {
            return new Exception(format(args), cause);
        }
    }

    private int[][] writeGlyphs (BufferedOutputStream writer) throws Exception {

        Map<Character, C> map = data.getCharTable();

        int max = map.size();
        if (max < 1) {
            throw CUFExceptions.NoGlyphsToWrite.create(max);
        }
        CUFGlyph glyph;
        CUFGlyphDimension dim;

        int offset = 0, done = 1, k, index = 0;
        int[] offsets = new int[max], dimensions = new int[max];

        byte[] bitmap;
        for (Character key : map.keySet()) {
            k = IOHelp.codeOf(key);
            if (k != FormatConstants.__LIMIT__.value()) { // omit null characters; the CUF format doesn't support those

                glyph = data.getGlyph(map.get(key));
                if (glyph != null) {
                    cTable.put(key, index);
                    offsets[index] = offset;
                    dim = glyph.getDimension();
                    dimensions[index] = insertDimension(dim);
                    kern(key, dim);
                    bitmap = glyph.getBitMapData();
                    offset += bitmap.length;

                    writer.write(bitmap);
                    postUpdate(done, max, Messages.WriterProgress, done, max);
                }
                else {
                    dimensions[index] = 0;
                    offsets[index] = 0;
                }
                ++index;
            }
            ++done;
        }
        this.glyphDataSize = offset;
        return padKerning(new int[][] {
                    dimensions,
                    offsets
                }, writer);
    }

    private int dimensionComponent (FormatConstants f, int byteValue) throws
            Exception {
        return (cast(byteValue) & 0xFF) << (f.value() * 8);
    }

    private int insertDimension (CUFGlyphDimension dim) throws
            Exception {
        int gH = dim.getHeight(),
                gW = dim.getWidth(),
                aH = IOHelp.unsign(dim.getY(), 1),
                aW = dim.getAdvanceWith();
        if (gH > this.vsize) {
            this.vsize = gH;
        }
        if (gW > this.hsize) {
            this.hsize = gW;
        }
        aW = dimensionComponent(FormatConstants.GlyphAllocWidth, aW);
        aH = dimensionComponent(FormatConstants.GlyphAllocHeight, aH);
        gH = dimensionComponent(FormatConstants.GlyphHeight, gH);
        gW = dimensionComponent(FormatConstants.GlyphWidth, gW);

        return aW | aH | gH | gW;
    }

    /**
     * Casts an int to an unsigned byte if it can be represented as such.
     * @param value an int to cast to an unsigned byte
     * @return a byte equivalent to the given value
     * @throws Exception if the value does not fall within 0 &lt; value &lt; 255
     */
    protected byte cast (int value) throws Exception {
        if (value < 0 || value > 0xFF) {
            throw CUFExceptions.ByteValueError.create(value);
        }
        return (byte) (value & 0xFF);
    }

    private byte[] prepareCharTable () {

        // size of char table: this is a fixed value and does not depend on the actual data map.
        byte[] charTable = new byte[FormatConstants.START_OF_GLYPH_META_TABLES.
                value() - FormatConstants.HEADER_SIZE.value()];

        // mark all char table entries as blank/unsupported
        Arrays.fill(charTable, (byte) FormatConstants.__LIMIT__.value());

        // fill in the char table entries that have actual meaning
        for (Entry<Character, Integer> entry : cTable.entrySet()) {
            charTable = IOHelp.fillBuffer(
                    entry.getValue(),
                    IOHelp.codeOf(entry.getKey()) << 1, 2, charTable, true);
        }
        return charTable;
    }

    private void copyBuf (RandomAccessFile reader, FileOutputStream writer,
                          long offset, int length) throws Exception {
        byte[] buf = new byte[length];
        reader.seek(offset);
        reader.read(buf);
        writer.write(buf);
    }

    private void stage2 (File infile, File outfile) throws Exception {
        postUpdate(Messages.Stage2);

        postUpdate(Messages.Open, infile, outfile);

        RandomAccessFile reader = null;
        FileOutputStream writer = null;

        try {
            reader = new RandomAccessFile(infile, "r");
            writer = new FileOutputStream(outfile);

            writer.write(MimeTag.CUFFont.tag());
            postUpdate(Messages.CUFProps, outfile);

            writer.write(prepareCUFProperties());

            postUpdate(Messages.WriteChars, outfile);
            writer.write(prepareCharTable());

            postUpdate(Messages.MetaData, outfile);
            writer.write(this.glyphMetaTables);

            postUpdate(Messages.CopyGlyphs, infile, outfile);
            copyBuf(reader, writer, 0, glyphDataSize);

            if (this.data.kerningEnabled()) {
                postUpdate(Messages.CopyKerning, infile, outfile);

                int kz = kTable.size();
                copyBuf(reader, writer, glyphDataSize, 4 + (kz * kz));
            }
        }
        finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }
}
