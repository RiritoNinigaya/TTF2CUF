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

/**
 * This interface describes a mapping between {@link FontInformationKey} type keys and
 * arbitrary objects. The type of object is dependent on its key. This interface is mainly used to
 * interact with system fonts through {@link SystemFontSource}.
 * @author Johan Ouwerkerk
 */
public interface FontInformation {

    /**
     * Get the value associated with a given {@link FontInformationKey}.
     * @param key the key to lookup.
     * @return the value stored in this {@link FontInformation} map, or null.
     */
    public Object get(FontInformationKey key);

    /**
     * Key to a {@link FontInformation} object.
     * @param <T> type of value associated with this key.
     */
    public static interface FontInformationKey<T> {
        /**
         * Cast the object obtained from {@link FontInformation#get(org.europabarbarorum.cuf.font.pipes.FontInformation.FontInformationKey) }
         * to the type associated with this key.
         * @param value the value to cast
         * @return this value, but now cast to its proper type.
         */
        T cast(Object value);
        /**
         * Check if the value obtained from a {@link FontInformation} map is valid.
         * Typical implementation would be:<blockquote><pre>
         * {@code @Override
         * boolean isValid(T cast) {
         *     return cast != null;
         * }
         * }</pre></blockquote>
         * @param cast the value to check.
         * @return true if the value is valid, false if not.
         */
        boolean isValid(T cast);
    }
    /**
     * Standard {@link FontInformationKey} values that {@link SystemFontSource} exposes.
     * This enumeration represents values that come from the underlying system font resource.
     */
    public static enum StandardFloatKeys implements FontInformationKey<Float> {

        /**
         * Offset at which a strikethrough should be drawn if applicable.
         */
        StrikeThroughOffset,
        /**
         * Thickness of the strikethrough if applicable.
         */
        StrikeThroughThickness,
        /**
         * Offset at which an underline should be drawn if applicable.
         */
        UnderlineOffset,
        /**
         * Thickness of the underline if applicable.
         */
        UnderlineThickness;

        @Override
        public boolean isValid (Float cast) {
            return cast!=null;
        }

        @Override
        public Float cast (Object value) {
            return value==null ? null : (Float) value;
        }
    }
}
