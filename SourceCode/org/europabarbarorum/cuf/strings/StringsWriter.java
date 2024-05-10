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
package org.europabarbarorum.cuf.strings;

import org.europabarbarorum.cuf.strings.impl.SimpleStringsFeeder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.MimeTag;
import org.europabarbarorum.cuf.support.CompileJob;
import org.europabarbarorum.cuf.support.CompileJob.FileJob;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.OptionMap;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * This class bridges all components of the Strings compiler together as a single
 * {@link CompileJob}. It is responsible for the I/O streams and progress updates.
 * @author Johan Ouwerkerk
 */
public class StringsWriter extends FileJob {

    /**
     * A {@link DefaultOption} which sets the character encoding to use for reading (XML) data
     * from disk.
     */
    public static class EncodingOption implements DefaultOption {

        /**
         * The default option value.
         */
        public static final String defaultEncoding = "UTF-8";
        private final String encoding;

        /**
         * Create a default {@link EncodingOption}.
         * This constructor assumes the {@link #defaultEncoding}.
         */
        public EncodingOption () {
            this(defaultEncoding);
        }

        /**
         * Create an {@link EncodingOption}.
         * @param encoding the character encoding to use. A value of null means
         * that the program does not know the encoding and underlying library code should
         * be left to figure this out on its own.
         */
        public EncodingOption (String encoding) {
            this.encoding = encoding;
        }

        @Override
        public String name () {
            return "ENCODING";
        }

        @Override
        public DefaultOption defaultOption () {
            return new EncodingOption(null);
        }

        /**
         * Get the value of this {@link EncodingOption}.
         * @return a string specifying a character encoding to use.
         * This method returns null if underlying library code should be left to figure
         * out encoding information on its own.
         */
        public String value () {
            return this.encoding;
        }
    }

    /**
     * Setting which determines what to do with empty strings for keys and values in
     * Strings records.
     */
    public static enum EmptyStringOption implements DefaultOption {

        /**
         * Writes empty strings to result file when compiling, does nothing for reading.
         */
        Enable,
        /**
         * Accepts empty strings for value data only.
         * When compiling, Strings keys which are empty trigger an exception.
         * When reading records with an empty key trigger a warning but
         * are otherwise ignored.
         * This is the default option.
         */
        ValueOnly,
        /**
         * Same as {@link #Enable} when compiling, except warning messages are generated.
         * Same as {@link #Disable} when reading, except warning messages are generated.
         */
        Warning,
        /**
         * When compiling any empty string triggers an exception.
         * When reading any empty string causes the corresponding record to be ignored; no
         * warnings are generated.
         */
        Disable;

        @Override
        public DefaultOption defaultOption () {
            return Disable;
        }
    }

    /**
     * Setting which determines the Strings format in which Strings records are written to the
     * result file.
     */
    public static enum FormatOption implements DefaultOption {

        /**
         * “Keyed” format: both key and value fields of Strings records are written to the
         * output file, hence records do not need to be written in a specific order.
         */
        Keyed,
        /**
         * “Keyed” format: both key and value fields of Strings records are written to the
         * output file, hence records do not need to be written in a specific order.
         * This version of {@link #Keyed} does not append a lookup table.
         */
        PlainKeys,
        /**
         * “Ordered” format, only value fields of Strings records are written to the output file;
         * hence order in which records are written is significant.
         */
        Ordered;

        @Override
        public DefaultOption defaultOption () {
            return Keyed;
        }
    }

    /**
     * This interface describes the mechanism of feeding output to the compiler: it provides a way to
     * use the same compiler structure with different input sources.
     * @author Johan Ouwerkerk
     */
    public static interface StringsFeeder {

        /**
         * Deliver {@link StringWriter#key(java.lang.String) } and {@link StringWriter#value(java.lang.String) }
         * events to the part of the compiler which handles writing output in the Strings file format.
         * @param writer a {@link StringWriter} created and initialised by the compiler for writing output data.
         * @throws Exception if an error occurs
         */
        public void deliverEvents (StringWriter writer) throws Exception;

        /**
         * Dispose of any IO or similar resources created by this {@link SimpleStringsFeeder}.
         * This does not include closing the {@link StringWriter} given in
         * {@link #deliverEvents(org.europabarbarorum.cuf.strings.StringsWriter.StringWriter) }:
         * the compiler manages that object lifespan itself.
         * @throws Exception if an error occurs.
         */
        public void dispose () throws Exception;
    }

    /**
     * Wrapper around a {@link BufferedOutputStream} to write data in the
     * Strings file format.
     */
    public final class StringWriter {

        private int k, v;
        private final EmptyStringOption emptyOption;
        private final BufferedOutputStream output;

        private StringWriter (OutputStream stream) throws
                Exception {
            output = new BufferedOutputStream(stream);
            k = 0;
            v = 0;
            format(options.getOption(FormatOption.class));
            emptyOption = options.getOption(EmptyStringOption.class);
        }

        /**
         * Configures the Strings format of the output to be written
         * @param format a {@link FormatOption} instance. Must not be null.
         * @throws IllegalStateException if this method is called when output has already
         * been written to this {@link StringWriter}.
         */
        public void format (FormatOption format) throws IllegalStateException {
            if (k != 0) {
                throw new IllegalStateException(Messages.IllegalFormatCall.
                        getText());
            }
            options.put(FormatOption.class, format);
        }

        /**
         * Write a key to the result Strings file.
         * Note that before the next key can be written its corresponding value must have
         * been passed to {@link #value(java.lang.String) } first.
         * @param key the key to write.
         * @throws Exception if an error occurs.
         */
        public void key (String key) throws Exception {
            if (k > v) {
                throw time("key");
            }
            ++k;
            switch (options.getOption(FormatOption.class)) {
                case Keyed:
                    lookup(key);
                case PlainKeys:
                    write(key);
                    progress(Messages.WriterKeyedProgress, k, v);
                default:
                    return;
            }
        }

        private int records () {
            return v;
        }

        private byte[] map (String s) {
            int l = s.length();
            if (l > 0xFFFF) {
                throw new IllegalArgumentException(
                        Messages.IllegalStringLength.format(l));
            }
            byte[] map = new byte[((l + 1) << 1)]; // buffer containing 2* l + 2 bytes
            map = IOHelp.fillBuffer(l, 0, 2, map, true);
            for (int i = 0, j = 2; i < l; ++i, j += 2) {
                map = IOHelp.fillBuffer(s.codePointAt(i), j, 2, map, true);
            }
            return map;
        }

        private IllegalStateException time (String method) {
            return new IllegalStateException(method);
        }

        /**
         * Write a value to the result Strings file.
         * Note that before a value can be written its corresponding
         * key must have been passed to {@link #key(java.lang.String) } first.
         * @param value the value to write.
         * @throws Exception if an error occurs.
         */
        public void value (String value) throws Exception {
            if (v == k) {
                throw time("value");
            }
            ++v;
            write(value);
            if (Shell.ChattyShell.get()) {
                FormatOption f = options.getOption(FormatOption.class);
                if (FormatOption.Ordered == f) {
                    progress(Messages.WriterOrderedProgress, v);
                }
                else {
                    progress(Messages.WriterKeyedProgress, k, v);
                }
            }
        }

        /**
         * Sink to handle progress signals.
         * @param m a {@link BundleKey} containing a progress format string.
         * @param vals arguments to the format string.
         */
        protected void progress (Messages m, Object... vals) {
            postUpdate(m, vals);
        }

        private String getEmptyMessage () {
            if (v <= k) {
                return Messages.EmptyKey.format(
                        k,
                        v,
                        EmptyStringOption.class.getCanonicalName(),
                        EmptyStringOption.Enable.name());
            }
            else {
                return Messages.EmptyValue.format(
                        k,
                        v,
                        EmptyStringOption.class.getCanonicalName(),
                        EmptyStringOption.ValueOnly.name());
            }
        }
        private LinkedList<String> lookup;

        private void lookup (String key) {
            if (lookup == null) {
                lookup = new LinkedList<String>();
            }
            lookup.add(key);
        }

        private void finish () throws Exception {
            try {
                if (lookup != null) {
                    progress(Messages.AppendLookupTable);
                    output.write(IOHelp.fillBuffer(records(),
                                                   0,
                                                   4,
                                                   new byte[4],
                                                   true));
                    for (String s : lookup) {
                        write(s);
                    }
                }
                flush();
            }
            finally {
                close();
            }
        }

        private void flush () throws IOException {
            output.flush();
        }

        private void close () throws IOException {
            output.close();
        }

        /**
         * Write a string in the .strings.bin binary format.
         * @param toWrite the string to write.
         * @throws Exception if an error occurs
         */
        private void write (String toWrite) throws Exception {
            if (toWrite.length() == 0) {
                switch (emptyOption) {
                    case Disable:
                        throw new IllegalArgumentException(getEmptyMessage());
                    case ValueOnly:
                        if (v < k) {
                            throw new IllegalArgumentException(getEmptyMessage());
                        }
                        break;
                    case Warning:
                        IOHelp.warn(StringsWriter.class, getEmptyMessage());
                        break;
                    case Enable:
                    default:
                        break;
                }
            }
            output.write(map(toWrite));
        }
    }
    private final StringsFeeder src;
    private final OptionMap options;
    private final File out;
    /**
     * Field to track how many strings have been written.
     */
    private int calls = 0;

    /**
     * Create a {@link StringsWriter}.
     * @param src a {@link SimpleStringsFeeder} which feeds strings to the compiler.
     * @param outfile the file to store the compiled .strings.bin result.
     * @param opts a {@link OptionMap} of keys and corresponding values to control
     * compiler settings.
     */
    public StringsWriter (StringsFeeder src, File outfile, OptionMap opts) {
        super(Messages.JobTitle.format(outfile), outfile);
        this.src = src;
        this.options = opts;
        this.out = outfile;
    }

    @Override
    protected void compile () throws Exception {
        postUpdate(Messages.Start, out);
        File temp = createTempFile("strings-temporary", ".strings.bin", null);
        stage1(temp);
        stage2(temp);
        temp.delete();
    }

    private void stage2 (File infile) throws Exception {
        postUpdate(Messages.Stage2);
        postUpdate(Messages.Open, infile, out);
        BufferedInputStream read =
                new BufferedInputStream(new FileInputStream(infile));
        BufferedOutputStream write =
                new BufferedOutputStream(new FileOutputStream(out));
        try {
            postUpdate(Messages.MetaData, out);
            write.write(options.getOption(FormatOption.class)
                    == FormatOption.Ordered
                    ? MimeTag.OrderedStringsFile.tag()
                    : MimeTag.KeyedStringsFile.tag());
            write.write(IOHelp.fillBuffer(calls, 0, 4, new byte[4], true));

            postUpdate(Messages.Copying, infile, out);
            byte[] buf;
            int bufsize = 65536, cap;
            while (read.available() > 0) {
                buf = new byte[bufsize];
                cap = read.read(buf);
                write.write(buf, 0, cap);
            }
            write.flush();
        }
        finally {
            write.close();
            read.close();
        }
    }

    private void stage1 (File out) throws Exception {
        postUpdate(Messages.Stage1);
        postUpdate(Messages.OpenTemp, out);
        postUpdate(Messages.PreparingInput);
        StringWriter writer = new StringWriter(new FileOutputStream(out));

        try {
            postUpdate(Messages.ParsingInput);
            src.deliverEvents(writer);
            calls = writer.records();
            writer.finish();
        }
        finally {
            writer.close();
            src.dispose();
        }
    }
}
