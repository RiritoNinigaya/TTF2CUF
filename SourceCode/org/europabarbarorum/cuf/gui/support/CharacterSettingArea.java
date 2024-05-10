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

import org.europabarbarorum.cuf.support.Escapes;
import org.europabarbarorum.cuf.support.ResourceHelp.CharacterArrayResource;

/**
 * A type of {@link SettingArea} which offers additional methods to access its
 * text parsed as an array of characters.
 * @author Johan Ouwerkerk
 */
public class CharacterSettingArea extends SettingArea implements ValidatedUI {

    /**
     * Create a new {@link CharacterSettingArea}.
     */
    public CharacterSettingArea () {
        setText(Messages.CharacterArrayExample.getText());
    }

    /**
     * Validates the user input in this component. As a side effect it causes this component to
     * update its internal copy of the character array to the latest revision in the text.
     * If the text in the component is invalid this method will cause the internal copy of the
     * character array to be reset.
     * @return true if a valid character array was input, false if not.
     */
    @Override
    public boolean checkUI () {
        drop();
        String v = getValue();
        try {
            array = parse(v);
        }
        catch (Exception e) {
            array = null;
        }
        if(array == null) {
            reset(Messages.CharacterArrayError.getText());
            return false;
        }
        else {
            return true;
        }
    }
    
    private Character[] array = null;

    /**
     * Get the parsed character array from the last UI check.
     * Note that if you use this method to extract the character array denoted by the current text in
     * the UI component you must call {@link #checkUI() } first.
     * @return a copy of the character array parsed at the last {@link #checkUI() } call, or
     * null if the text was invalid or not available/empty itself at that time.
     */
    public Character[] array () {
        return array;
    }

    /**
     * Parse logic for {@link CharacterSettingArea} and other classes which need parsing of
     * text compatible with it. This method implements the actual parsing of text into
     * a character array.
     * @param s the text to parse.
     * @return the parsed character array.
     * @throws Exception if an error occurs, in particular if the given text is null.
     * @see CharacterArrayResource#parse(java.lang.String)
     * @see Escapes#substitute(java.lang.String)
     */
    public static Character[] parse(String s) throws Exception {
        return new CharacterArrayResource().parse(Escapes.substitute(s));
    }
}
