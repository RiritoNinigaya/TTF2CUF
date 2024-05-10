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
package org.europabarbarorum.cuf.macro;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.MimeTag;
import org.europabarbarorum.cuf.support.ResourceHelp.CharacterArrayResource;

/**
 * Object to read and parse a given {@link File} into an
 * {@link List} of {@link Macro} objects.
 * @author Johan Ouwerkerk
 */
public class MacroFile implements Macro.MacroProvider {

    /**
     * {@link File} input file.
     */
    private final File f;
    /**
     * {@link List} of {@link Macro} objects populated with the parsed contents of {@link #f}.
     */
    private final List<Macro> list;

    @Override
    public int number () {
        return list.size();
    }

    @Override
    public List<Macro> macros () {
        return list;
    }

    /**
     * Interprets a file as {@link Macro.MacroProvider}. 
     * This constructor reads {@link Macro} objects from a file into a list.
     * @param f the file to read. A {@link IllegalArgumentException} is thrown if the file is not a 
     * macro file according to {@link MimeTag#MacroFile}.
     */
    @SuppressWarnings("unchecked")
    public MacroFile (File f) {
        this.f = f;
        MimeTag.MacroFile.check(f);
        List<Macro> _list = init();
        if (_list == null) {
            list = Collections.EMPTY_LIST;
        }
        else {
            list = Collections.unmodifiableList(_list);
        }
    }

    private class FileMacro extends Macro.SimpleMacro {

        public FileMacro (String name) {
            super(name);
        }

        public void init (String keys, String vals, Properties ps) throws
                Exception {
            String __key__ = ps.getProperty(keys);
            String __val__ = ps.getProperty(vals);
            _check(keys, __key__);
            _check(vals, __val__);

            Character[] _keys = charArrayParser.parse(__key__);
            Character[] _vals = charArrayParser.parse(__val__);
            if (_keys.length != _vals.length) {
                throw new IllegalArgumentException(
                        Messages.KeyValueMismatch.format(name(),
                                                         _keys.length,
                                                         _vals.length));
            }
            for (int i = 0; i < _keys.length; ++i) {
                mapping.put(_keys[i], _vals[i]);
            }
        }

        private void _check (String propname, String propVal) {
            if (propVal == null || propVal.length() == 0) {
                throw new IllegalArgumentException(
                        Messages.EmptyProperty.format(name(), propname));
            }
        }
    }
    private static final CharacterArrayResource charArrayParser =
            new CharacterArrayResource();

    /**
     * Parse a string into an array of strings.
     * @param s the string to parse.
     * @return the result array of strings.
     */
    private static String[] fromString (String s) {
        s = s.substring(1, s.length() - 1);
        return s.split(", ");
    }

    private Reader open () throws Exception {
        return new InputStreamReader(new FileInputStream(f), Macro.macroEncoding);
    }

    private List<Macro> read () throws Exception {
        Reader r = null;
        try {
            r = open();
            Properties ps = new Properties();
            ps.load(r);
            ArrayList<Macro> macros = new ArrayList<Macro>();
            String[] names = fromString(
                    ps.getProperty(Macro.macro_name_decl));
            FileMacro macro;
            for (String n : names) {
                macro = new FileMacro(n);
                macro.init(String.format(Macro.macro_keys_decl, n),
                           String.format(Macro.macro_vals_decl, n),
                           ps);
                macros.add(macro);
            }
            return macros;
        }
        finally {
            if (r != null) {
                r.close();
            }
        }
    }

    private List<Macro> init () {
        try {
            return read();
        }
        catch (Exception e) {
            IOHelp.handleExceptions(MacroFile.class, "init", e, e.
                    getLocalizedMessage());
            return null;
        }
    }
}
