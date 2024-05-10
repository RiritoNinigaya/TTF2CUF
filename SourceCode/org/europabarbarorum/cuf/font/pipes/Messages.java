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
package org.europabarbarorum.cuf.font.pipes;

import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * List of {@link BundleKey} messages used in the
 * {@code org.europabarbarorum.cuf.font.pipes} package.
 * @author Johan Ouwerkerk
 */
public enum Messages implements BundleKey {

    /**
     * Thrown when a previous error prevents further use of this {@link CUFReader}.
     * Takes a method name and an error message argument.
     */
    IOTainted(CUFReader.class),
    /**
     * Throw when a {@link CUFReader} is unable to read enough bytes.
     * Takes an offset, the number of bytes required, and the number of
     * bytes actually read as arguments.
     */
    NotEnoughData(CUFReader.class),
    /**
     * Thrown when a {@link Phase} class listed for exposure is null.
     * Takes no arguments.
     */
    ExposeInvalidError(AbstractPhaseSource.class),
    /**
     * A {@link Phase} requires at least one {@link Transform} but none were registered.
     * Takes no arguments.
     */
    MissingRequiredTransform(Phase.class),
    /**
     * Throw when code attempts to access a {@link Phase} that is not exposed by
     * the {@link AbstractPhaseSource}. Takes a class name argument.
     */
    NotExposedError(AbstractPhaseSource.class),
    /**
     * Thrown when the pipeline is fed an empty list of {@link Phase phases}.
     * Takes no arguments.
     */
    EmptyPipeLineError(AbstractPhaseSource.class),
    /**
     * Thrown when a rendering pipeline is fed a null instead of a list of
     * {@link Phase phases}. Takes no arguments.
     */
    NullPipeLineError(AbstractPhaseSource.class),
    /**
     * Emitted when a {@link CUFReader} is re-used while it is not yet {@link CUFReader#close() closed}.
     * Takes the file it currently is locked on to and
     * the file it was supposed to start using instead as arguments.
     */
    FileConflict(CUFReader.class),
    /**
     * A {@link Transform} failed. Takes an error message.
     */
    TransformFailed(Phase.class),
    /**
     * Output validation failed for a {@link Phase}. Takes number of
     * {@link Transform transorms} successfully applied and the expected number
     * as arguments.
     */
    ValidationFailed(Phase.class);

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
