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

import org.europabarbarorum.cuf.font.impl.FormatConstants;
import org.europabarbarorum.cuf.gui.support.CharacterSettingField.Model;
import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 * A kind of {@link SettingField} for entering a character code point (or integer amount).
 * @author Johan Ouwerkerk
 */
public class CharacterSettingField extends SettingField implements ComponentState<Model> {

    /**
     * Create a new {@link CharacterSettingField}.
     */
    public CharacterSettingField () {
        setText(Messages.CharacterFieldExample.getText());
    }

    enum Parsers {

        Int(10) {

            @Override
            public Integer tryParse (String s) {
                return super.value(s);
            }
        },
        Hex(16) {

            private final String[] prefixes = new String[] { "U+", "\\u", "0x",
                                                             "\\x" };

            @Override
            public Integer tryParse (String s) {
                int l = s.length();
                if (s.startsWith("x")) {
                    return l == 1 ? null : super.value(s.substring(1));
                }
                if (l > 2) {
                    for (String p : prefixes) {
                        if (s.startsWith(p)) {
                            return super.value(s.substring(2));
                        }
                    }
                }
                return super.value(s);
            }
        },
        Char {

            @Override
            public Integer tryParse (String s) {
                int l=s.length();
                switch(l) {
                    case 1:
                    return IOHelp.codeOf(s.charAt(0));
                    case 3:
                        Integer open=find(s.charAt(0),openChars);
                        Integer close=find(s.charAt(2), closeChars);
                        if(open!=null && close!=null && open == close) {
                            return IOHelp.codeOf(s.charAt(1));
                        }
                    default:
                        return null;
                }
            }
            private final String openChars="'\"‘“";
            private final String closeChars="'\"’”";
            private Integer find(char c, String s) {
                for(int i=0,l=s.length();i<l;++i) {
                    if(s.charAt(i) == c) {
                        return i;
                    }
                }
                return null;
            }
        },
        Entity {

            @Override
            public Integer tryParse (String s) {
                int l = s.length();
                if (s.endsWith(";")) {
                    if (l < 4) { // too short to be valid.
                        return null;
                    }
                    s = s.substring(0, l - 1);
                }
                if (s.startsWith("&#")) {
                    if (l == 2) {
                        return null;
                    }
                    s = s.substring(2);
                    if (s.charAt(0) == 'x') {
                        return l == 3 ? null : Hex.value(s.substring(1));
                    }
                    return Int.value(s);
                }
                return null;
            }
        };

        public abstract Integer tryParse (String s);

        private Parsers () {
            this(0);
        }

        private Parsers (int r) {
            this.radix = r;
        }
        private final int radix;

        private Integer value (String s) {
            try {
                return Integer.parseInt(s, radix);
            }
            catch (Exception e) {
                return null;
            }
        }

        static Integer parseString (String s) {
            Integer result = null;
            for (Parsers p : Parsers.values()) {
                result = p.tryParse(s);
                if (result != null) {
                    return result;
                }
            }
            return result;
        }
    }

    private Integer code = null;
    /**
     * Get the value entered in this {@link CharacterSettingField}. This method should be called only after
     * {@link #checkUI() validating} the input in this component, otherwise stale copies might be returned.
     * @return a cached copy of the result of parsing the {@link #getValue() value} of this field.
     * The result will be null if the value entered was invalid or if no value has been entered yet.
     */
    public Integer code() {
        return code;
    }

    @Override
    public boolean checkUI () {
        drop();
        String s = getValue();
        if (s != null) {
            Integer i = Parsers.parseString(s);
            if (i != null && 0 <= i && i <= FormatConstants.__LIMIT__.value()) {
                code = i;
                return true;
            }
        }
        code = null;
        return reset();
    }

    @Override
    public Model createModel () {
        return new Model(getValue(), code);
    }
    
    /**
     * Mark the {@link CharacterSettingField} as invalid.
     * This method causes a default error message to be displayed.
     * @return false.
     * @see #reset(java.lang.String) 
     */
    public boolean reset() {
        reset(Messages.CharacterFieldError.getText());
        return false;
    }

    /**
     * A {@link ComponentModel} for a {@link CharacterSettingField}.
     */
    public static class Model implements ComponentModel<CharacterSettingField> {

        private Model (String text, int code) {
            this.code = code;
            this.t = text;
        }
        private final String t;
        /**
         * The value of the code point or amount entered.
         */
        public final int code;

        @Override
        public void populate (CharacterSettingField ui) {
            ui.setValue(t);
            ui.code = code;
        }
    }
}
