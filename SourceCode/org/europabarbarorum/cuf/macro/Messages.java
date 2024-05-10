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

import org.europabarbarorum.cuf.macro.Macro.SimpleMacro;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 *
 * @author Johan Ouwerkerk
 */
public enum Messages implements BundleKey {

    /**
     * Progress message emitted when a {@link Converter} begins compilation.
     * Takes output filename as argument.
     */
    Start(Converter.class),
    /**
     * Progress message emitted when a {@link Converter} writes output to its
     * destination file. Takes output filename as argument.
     */
    Writing(Converter.class),
    /**
     * Progress message emitted when a {@link Converter} has finished preparing a single
     * macro. Takes number of macro's done and total number of macro's to do as arguments.
     */
    Progress(Converter.class),
    /**
     * Title of a {@link Converter conversion task}. 
     * Takes font description as argument.
     */
    JobTitle(Converter.class),
    /**
     * Error emitted when a Macro property is empty (missing a value).
     * Takes name of the macro and property name as arguments.
     */
    EmptyProperty(MacroFile.class),
    /**
     * Error when an input string contains a character that cannot be mapped.
     * Takes the offending character, name of the macro, index of the offending
     * character, and a bit of input context as arguments.
     * @see SimpleMacro#map(java.lang.String)  
     */
    IllegalCharacterError(SimpleMacro.class),
    /**
     * Error emitted when the number of keys is different from the number of values in a
     * Macro read from file.
     * Takes name of the macro, number of keys found and number of values found as arguments.
     */
    KeyValueMismatch(MacroFile.class);

    private Messages (Class type) {
        this.type = type;
    }
    private final Class type;

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, Messages.class);
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, Messages.class, args);
    }

    @Override
    public Class type () {
        return type;
    }
}
