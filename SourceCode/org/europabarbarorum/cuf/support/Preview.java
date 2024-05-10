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

package org.europabarbarorum.cuf.support;

/**
 * An interface to describe a GUI model for obtaining preview text for a font
 * from an (arbitrary) input source.
 * @author Johan Ouwerkerk
 */
public interface Preview {

    /**
     * Select new preview text to render using a string key.
     * This method is used for changing preview text as the result of user input.
     * @param key the key to select a new preview text with.
     */
    public void setKey(String key);

    /**
     * Get the default value used for populating a preview at GUI initialisation.
     * @return a string which would select the default preview text in this preview model.
     */
    public String defaultKey();

    /**
     * Get the text that should be previewed.
     * @return the text that a preview of a font should render.
     */
    public String getPreview();

    /**
     * Get the key that is used to select the preview (text).
     * @return the key that was used to obtain the current preview.
     */
    public String getKey();

    /**
     * Abstract base implementation that handles selection of preview keys.
     * Subclasses have to implement {@link Preview#getPreview() } only.
     */
    public abstract static class AbstractPreview implements Preview {
        private String key;

        @Override
        public String getKey () {
            return key;
        }

        @Override
        public void setKey (String key) {
            this.key = key;
        }
    }
}
