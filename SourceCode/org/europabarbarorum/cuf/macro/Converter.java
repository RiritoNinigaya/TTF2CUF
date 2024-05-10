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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.MacroSource;
import org.europabarbarorum.cuf.font.impl.CharTableEntry;
import org.europabarbarorum.cuf.macro.Macro.MacroProvider;
import org.europabarbarorum.cuf.macro.Macro.Mapping;
import org.europabarbarorum.cuf.support.CompileJob.FileJob;
import org.europabarbarorum.cuf.support.Escapes;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 * Class to encode a {@link CUFSource} as a Macro file.
 * @author Johan Ouwerkerk
 */
public class Converter extends FileJob implements MacroProvider {

    /**
     * Unmodifiable {@link List} of extracted {@link Macro} objects from the input {@link CUFSource}.
     * @see #Converter(org.europabarbarorum.cuf.font.CUFSource, java.io.File)
     */
    private final List<Macro> convert;

    /**
     * Creates a {@link Converter} from a given {@link CUFSource} to store {@link Macro} objects
     * in a macro file.
     * @param toConvert the {@link CUFSource} to obtain the {@link Macro} objects from.
     * @param f the {@link File} to store any output results to if written.
     * @see #fromCUFSource(org.europabarbarorum.cuf.font.CUFSource)
     */
    public Converter (CUFSource toConvert, File f) {
        this(String.format(Messages.JobTitle.getText(),
                           toConvert.getCufSource()),
             fromCUFSource(toConvert), f);
    }

    /**
     * Creates a {@link Converter} to store the given {@link Macro} objects in the given file.
     * @param jobTitle a string (title) used for identifying this compiler job when displaying progress messages
     * @param toConvert the {@link List} of {@link Macro} objects to serialize to a file
     * @param f the {@link File} to store output if it is generated
     */
    public Converter (String jobTitle, List<Macro> toConvert, File f) {
        super(jobTitle, f);
        convert = Collections.unmodifiableList(toConvert);
    }

    /**
     * Extracts {@link Macro} objects from the given {@link CUFSource} object
     * @param toConvert the {@link CUFSource} to obtain the {@link Macro} objects from.
     * @return a {@link List} of Macros found.
     * @see CUFSource#backTrack(java.lang.Character)
     */
    @SuppressWarnings("unchecked")
    public static List<Macro> fromCUFSource (CUFSource toConvert) {
        int count = 0;
        Map<Character, CharTableEntry> ctable = toConvert.getCharTable();
        HashMap<CUFSource, Accumulator> cvt =
                new HashMap<CUFSource, Accumulator>();
        HashSet<String> names = new HashSet<String>();
        Character key;
        CUFSource origin;
        Accumulator acl;
        String mName = null;
        for (Entry<Character, CharTableEntry> record : ctable.entrySet()) {
            key = record.getKey();
            origin = toConvert.backTrack(key);

            if (cvt.containsKey(origin)) {
                acl = cvt.get(origin);
            }
            else {
                if (origin instanceof MacroSource) {
                    mName = ((MacroSource) origin).getMacroName();
                    if (!IOHelp.isValidXMLName(mName)) {
                        mName = null;
                    }
                }

                while (mName == null || names.contains(mName)) {
                    ++count;
                    mName = "mid-" + count;
                }
                names.add(mName);

                acl = new Accumulator(mName);
                mName = null;
            }
            acl.accumulate(toConvert.trackCharacter(key),
                           key);

            cvt.put(origin, acl);
        }
        
        /*
         * Sort macros by name. This behaviour benefits source control. 
         * 
         * The step determines the final layout of the macro files, and ensures 
         * that files generated on two different runs of the code are byte for 
         * byte identical if the encoded macros are equivalent.
         */
        TreeSet<Macro> sorted = new TreeSet<Macro>(new Comparator<Macro>() {

            @Override
            public int compare (Macro o1, Macro o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        sorted.addAll(cvt.values());
        return Arrays.asList(sorted.toArray(new Macro[sorted.size()]));
    }

    @Override
    public List<Macro> macros () {
        return convert;
    }

    @Override
    public int number () {
        return convert.size();
    }

    /**
     * Extension to the {@link SimpleMacro} class to accumulate
     * additional mappings one by one. This is the type of {@link Macro}
     * extracted by {@link Converter#Converter(org.europabarbarorum.cuf.font.CUFSource, java.io.File)}
     * using the results of {@link CUFSource#backTrack(java.lang.Character) }.
     */
    private static class Accumulator extends Macro.SimpleMacro {

        protected Accumulator (String name) {
            super(name);
        }

        /**
         * Add a new input output mapping to {@link Macro} if it has not already been added before.
         * In particular, this means that certain `control characters' are not added to the macro.
         * @param key input to recognize
         * @param value output to produce when mapping input.
         */
        protected void accumulate (Character key, Character value) {
            if (!mapping.containsKey(key)) {
                mapping.put(key, value);
            }
        }
    }

    @Override
    public void compile () throws Exception {
        postUpdate(Messages.Start, destination());
        PrintWriter pw = open();
        try {
            postUpdate(Messages.Writing, destination());
            pw.println("#CUFMacro");
            int count = number();
            String[] names = new String[count];
            Character[] keys, values;
            int i = 0, sz = 0;            
            for (Macro m : convert) {
                names[i] = m.name();
                sz = m.size();
                keys = new Character[sz];
                values = new Character[sz];
                sz = 0;
                for (Mapping entry : m) {
                    keys[sz] = entry.source();
                    values[sz] = entry.mapped();
                    ++sz;
                }
                printProp(pw,
                          String.format(Macro.macro_keys_decl, toHex(m.name())),
                          toHexArray(keys));

                printProp(pw,
                          String.format(Macro.macro_vals_decl, toHex(m.name())),
                          toHexArray(values));

                ++i;
                postUpdate(i, count, Messages.Progress, i, count);
            }
            printProp(pw, Macro.macro_name_decl, toHexArray(names));
            pw.flush();
        }
        finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private void printProp (PrintWriter pw, String k, String v) throws Exception {
        pw.print(k);
        pw.print("=");
        pw.println(v);
    }

    private String toHex (String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, l = s.length(); i < l; ++i) {
            sb.append(toHex(s.charAt(i)));
        }
        return sb.toString();
    }

    private String toHex (Character c) {

        if (escaped.contains(c)) {
            return new String(new char[] { '\\', c });
        }
        Escapes esc = Escapes.forCharacter(c);
        if (esc == null || esc == Escapes.Null) {
            return String.format("\\u%04x", IOHelp.codeOf(c));
        }
        else {
            return new String(new char[] { '\\', esc.sequence });
        }
    }

    private String toHexArray (String[] s) {
        StringBuilder sb = new StringBuilder("[");
        int l = s.length - 1;
        if (l > -1) {
            for (int k = 0; k < l; ++k) {
                sb.append(toHex(s[k]));
                sb.append(", ");
            }
            sb.append(toHex(s[l]));
        }
        sb.append("]");
        return sb.toString();
    }

    private String toHexArray (Character[] s) {
        StringBuilder sb = new StringBuilder("[");
        int l = s.length - 1;
        if (l > -1) {
            for (int k = 0; k < l; ++k) {
                sb.append(toHex(s[k]));
                sb.append(", ");
            }
            sb.append(toHex(s[l]));
        }
        sb.append("]");
        return sb.toString();
    }
    private static final HashSet<Character> escaped;

    static {
        escaped = new HashSet<Character>();
        escaped.add('#');
        escaped.add('!');
        escaped.add(':');
        escaped.add('=');
    }

    private PrintWriter open () throws Exception {
        return new PrintWriter(destination(), Macro.macroEncoding);
    }
}
