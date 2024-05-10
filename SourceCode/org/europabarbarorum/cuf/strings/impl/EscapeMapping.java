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

import java.io.File;
import org.europabarbarorum.cuf.strings.impl.StringMapping.BasicMapping;
import org.europabarbarorum.cuf.support.Escapes;

/**
 * A kind of {@link BasicMapping} which requires explicit escape sequences for some types of
 * control characters.
 * @see Escapes
 * @author Johan Ouwerkerk
 */
public class EscapeMapping extends BasicMapping {

    /**
     * Create an {@link EscapeMapping}.
     * @param f the {@link File} which supplies the macros used by this {@link EscapeMapping}.
     */
    public EscapeMapping (File f) {
        super(f);
    }

    @Override
    public void appendString (String text,String ctx) throws Exception {
        super.appendString(process(text),ctx);
    }

    /**
     * Alters input text.
     * @param text the original text passed to {@link #appendString(java.lang.String, java.lang.String) append}.
     * @return the text processed according to {@link Escapes#substitute(java.lang.String) }.
     * @see Escapes
     */
    protected String process(String text) {
        return Escapes.substitute(text);
    }

}
