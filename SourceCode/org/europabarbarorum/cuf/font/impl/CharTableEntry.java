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
package org.europabarbarorum.cuf.font.impl;

/**
 * A {@link CharTableEntry} enscapsulates the data required to look up a glyph in a CUF font.
 * @author Johan Ouwerkerk
 */
public class CharTableEntry {
    private final int code;
    private final char chr;

    /**
     * Create a new {@link CharTableEntry}.
     * @param code the index of the glyph in various meta data tables.
     * @param chr the character associated with a glyph
     */
    public CharTableEntry (int code, char chr) {
	this.code = code;
	this.chr = chr;
    }
    /**
     * Return the character that corresponds to this glyph.
     * @return the character this glyph represents.
     */
    public char getChar () {
	return chr;
    }
    /**
     * Return the integer code used to indentify the associated glyph within a CUF file.
     * @return the index of this glyph within various tables of meta information.
     */
    public int getCode () {
	return code;
    }

    /**
     * Create a string representation of this {@link CharTableEntry}.
     * @return a string representation of this {@link CharTableEntry}.
     */
    @Override
    public String toString () {
        return String.format("%1$s{ '%2$s', '%3$d' }", getClass().getName(), chr, code);
    }

}
