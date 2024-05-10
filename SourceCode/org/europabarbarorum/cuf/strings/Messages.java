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

import org.europabarbarorum.cuf.strings.StringsReader.KeyList;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 *
 * @author Johan Ouwerkerk
 */
public enum Messages implements BundleKey {

    EmptyValueRead(StringsReader.class),
    EmptyKeyRead(StringsReader.class),
    EmptyValue(StringsWriter.class),
    EmptyKey(StringsWriter.class),
    StringsKeyAtIndex(KeyList.class),
    StringsKeyListRequired(StringsReader.class),
    StringsKeyListTooShort(StringsReader.class),
    StringsFileInvalid(StringsReader.class),
    StringsInitError(StringsReader.class),
    StringsReadError(StringsReader.class),
    IllegalFormatCall(StringsWriter.class),
    AppendLookupTable(StringsWriter.class),
    WriterKeyedProgress(StringsWriter.class),
    WriterOrderedProgress(StringsWriter.class),
    Stage1(StringsWriter.class),
    OpenTemp(StringsWriter.class),
    Stage2(StringsWriter.class),
    Open(StringsWriter.class),
    MetaData(StringsWriter.class),
    Copying(StringsWriter.class),
    ParsingInput(StringsWriter.class),
    PreparingInput(StringsWriter.class),
    Start(StringsWriter.class),
    JobTitle(StringsWriter.class),
    IllegalStringLength(StringsWriter.class);

    private Messages (Class type) {
        this.type = type;
    }
    private final Class type;

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, Messages.class);
    }

    @Override
    public Class type () {
        return type;
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, Messages.class, args);
    }
}
