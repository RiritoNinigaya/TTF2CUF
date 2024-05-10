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

import javax.swing.JTextField;
import org.europabarbarorum.cuf.support.Escapes;
import org.europabarbarorum.cuf.support.Preview;
import org.europabarbarorum.cuf.support.Preview.AbstractPreview;

/**
 * Wrapper around {@link JTextField} to provide a pluggable {@link Preview}.
 * The idea is that you can instantiate this text field and select the preview by entering the key
 * at run time. If you do not configure the component with a {@link Preview} the component will use a
 * dummy one that simply returns the key text at {@link Preview#getPreview() }.
 * @author Johan Ouwerkerk
 */
public class PreviewInput extends JTextField implements UpdatableModel<Preview> {

    /**
     * Creates a new {@link PreviewInput} with a default {@link Preview} model.
     */
    public PreviewInput () {
        this(null);
    }

    /**
     * Creates a new {@link PreviewInput} with the given {@link Preview} model.
     * @param preview the {@link Preview} model to use.
     */
    public PreviewInput (Preview preview) {
        updateModel(preview);
        TextActions.setPopupMenu(this, null);
    }

    /**
     * Modified version of {@link JTextField#setText(java.lang.String) }.
     * This method additionally uses the text to select a preview
     * from the underlying {@link Preview} model being used.
     * @param t the text to set/preview to select.
     */
    @Override
    public void setText (String t) {
        preview.setKey(t);
        super.setText(t);
    }

    /**
     * Alternative to {@link #setText(java.lang.String) } which does not
     * alter the external appearance of the {@link PreviewInput}.
     * @param key a string (key) identifying the {@link Preview preview} text to select.
     */
    public void selectPreview (String key) {
        preview.setKey(key);
    }
    private Preview preview;

    /**
     * Get a default {@link Preview}.
     * @return an {@link AbstractPreview} which recognizes
     * {@link Escapes escape characters} in
     * the preview text.
     */
    public static Preview defaultPreview () {
        return new AbstractPreview() {

            @Override
            public String getPreview () {
                return Escapes.substitute(getKey());
            }

            @Override
            public String defaultKey () {
                return Messages.DefaultPreviewText.getText();
            }
        };
    }

    @Override
    public final void updateModel (Preview preview) {
        this.preview = preview == null ? defaultPreview() : preview;
        this.setText(this.preview.defaultKey());
    }

    /**
     * Get the {@link Preview} currently used to supply preview text.
     * @return the preview model currently being used.
     */
    public Preview getPreview () {
        return preview;
    }
}
