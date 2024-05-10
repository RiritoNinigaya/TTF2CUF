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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import org.europabarbarorum.cuf.strings.StringsWriter.EncodingOption;
import org.europabarbarorum.cuf.strings.StringsWriter.StringWriter;
import org.europabarbarorum.cuf.strings.StringsWriter.StringsFeeder;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.Escapes;
import org.europabarbarorum.cuf.support.OptionMap;

/**
 * A {@link StringsFeeder} which feeds a formatted “plain” text file to the compiler.
 * The file is formatted like this:
 * <blockquote><pre>
 * ¬ Comments start with ¬
 * {first_key} value
 * {second_key} second value
 * spans multiple multiple lines
 * ¬ perhaps another comment
 * which spans multiple lines also
 * {third_key_extends_the_whole_line
 * value and data here
 * </pre></blockquote>
 * @author Johan Ouwerkerk
 */
public class PlainSource implements StringsFeeder {

    private final OptionMap opts;
    private final File textFile;
    private final KeyResolver resolver;
    private BufferedReader reader;

    /**
     * Create a new {@link PlainSource}
     * @param textFile the {@link File} to read data from.
     * @param resolver the {@link KeyResolver} to use for
     * @param opts a {@link Map} of {@link DefaultOption} keys and corresponding setting values.
     */
    public PlainSource (File textFile, KeyResolver resolver, OptionMap opts) {
        this.resolver = resolver;
        this.textFile = textFile;
        this.opts = opts;
    }

    private void append (StringMapping map, String data, String key)throws Exception {
        map.appendString(data + Escapes.Newline.character,key);
    }

    private InputStreamReader open () throws Exception {
        EncodingOption opt = opts.getOption(EncodingOption.class);
        String encoding = opt.value();
        FileInputStream fis = new FileInputStream(textFile);
        return encoding == null
                ? new InputStreamReader(fis)
                : new InputStreamReader(fis, encoding);
    }

    @Override
    public void deliverEvents (StringWriter writer) throws Exception {
        reader = new BufferedReader(open());
        String key = null; // key of current working entry
        int splitAt = -1; // index used to split strings around tags.
        StringMapping map = null;
        for (String line = reader.readLine(); line != null; line = reader.
                        readLine()) {
            if (line.length() > 0) {
                switch (line.charAt(0)) {
                    // new entry found in the file
                    case '{':
                        // old entry needs to be flushed
                        if (key != null) {
                            sendData(map, key, writer);
                        }

                        // find key for  new entry
                        splitAt = line.indexOf("}");
                        /*
                         * assume that if no closing brace is found
                         * the whole line is acceptable substitute key
                         */
                        if (splitAt == -1) {
                            key = line.substring(1);
                            map = resolver.forKey(key);
                            map.select(resolver.getMacro(key),key);
                        }
                        else {
                            key = line.substring(1, splitAt);
                            map = resolver.forKey(key);
                            map.select(resolver.getMacro(key), key);
                            // append trailing content to new buffer
                            append(map, line.substring(splitAt + 1), key);
                        }
                        break;
                    // skip comments
                    case '¬':
                        /*
                         * send data early & avoid creating buffers that aren't
                         * used for consecutive comments
                         */
                        if (key != null) {
                            sendData(map, key, writer);
                            key = null;
                        }
                        break;
                    default:
                        /*
                         * only append if in a working entry
                         */
                        if (key != null) {
                            append(map, line,key);
                        }
                        break;
                }
            }
            else {
                /*
                 * only append if in a working entry
                 */
                if (key != null) {
                    append(map, "",key);
                }
            }
        }
    }

    private void sendData (StringMapping map, String key, StringWriter writer) throws
            Exception {
        writer.key(key);
        writer.value(new RawHandler(opts, map, key).toString());
    }

    @Override
    public void dispose () throws Exception {
        if (reader != null) {
            reader.close();
        }
    }
}
