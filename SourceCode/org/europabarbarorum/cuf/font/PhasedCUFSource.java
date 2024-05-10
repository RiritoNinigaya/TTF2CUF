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

package org.europabarbarorum.cuf.font;

import org.europabarbarorum.cuf.font.pipes.Phase;
import org.europabarbarorum.cuf.font.pipes.Transform;

/**
 * <p>PhasedCUFSources are a type of {@link CUFSource} that implement a conceptual “rendering pipeline”
 * consisting of multiple {@link Phase} instances which are chained together to produce font output.
 *</p><p>
 * In turn each {@link Phase} consists of a series of {@link Transform} objects that are chained together to
 * mold the input data into output (glyphs). This mechanism can be used to e.g. rotate glyphs 180°,
 * invert colours, and (depending on the {@link CUFSource} that implements this interface)
 * even determine how glyphs are obtained in the first place.</p>
 * <p>
 * This interface itself models how a “rendering pipeline” may be altered. This behaviour will allow
 * fonts to be adapted by plugging in bits or rendering code (styles) instead of writing an entire font
 * from scratch. Furthermore this behaviour provides encapulation of the rendering code itself, keeping it
 * seperate from the main low level logic of handling a CUF file format; and high level logic of assembling
 * a font from its components
 * </p>
 * @param <C> the type of entry objects to use in the chartable.
 * @author Johan Ouwerkerk
 */
public interface PhasedCUFSource<C> extends  CUFSource<C> {

    /**
     * Check if a given part of the “rendering pipeline” is exposed by the font.
     * If so, it means that you can add your own {@link Transform} instances to the pipeline at that
     * point so you can (take over) control (of) how this phase is handled.
     * @param phaseClass the class of the {@link Phase} instance to check for.
     * @return true if this {@link PhasedCUFSource} exposes that phase, false if not.
     * @see #register(java.lang.Class, org.europabarbarorum.cuf.font.pipes.Transform)
     * @see #register(java.lang.Class, org.europabarbarorum.cuf.font.pipes.Transform, int)
     */
    public boolean exposes (Class<Phase> phaseClass);

    /**
     * Adds the {@link Transform} object to the “rendering pipeline”  in the given position
     * for the given {@link Phase}.
     * This method should fail if {@link #exposes(java.lang.Class) } returns false for the given {@link Phase}.
     * @param p the type of {@link Phase} at which to register the {@link Transform}.
     * @param t the {@link Transform} instance to register.
     * @param position the index/position at which to register the {@link Transform}
     * @see Phase#set(org.europabarbarorum.cuf.font.pipes.Transform, int)
     */
    public void register (Class<Phase> p, Transform t, int position);

    /**
     * Adds a {@link Transform} to the “rendering pipeline” at the default position within the
     * given {@link Phase}.
     * @param p the {@link Phase} at which to register the {@link Transform}.
     * @param t the {@link Transform} instance to register.
     * @see Phase#add(org.europabarbarorum.cuf.font.pipes.Transform)
     */
    public void register (Class<Phase> p, Transform t);
}
