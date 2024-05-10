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

import java.awt.event.FocusEvent;
import javax.swing.JTextArea;

/**
 *
 * @author Johan Ouwerkerk
 */
public class SettingArea extends JTextArea implements TextSetting {
private final SettingHelper s;

    /**
     * Create a new {@link SettingField}.
     */
    public SettingArea() {
        s = new SettingHelper(this);
    }

    @Override
    public void setText (String t) {
        s.set(t);
        super.setText(t);
    }

    @Override
    public String getValue () {
        return s.getValue();
    }

    @Override
    public void setValue (String value) {
        this.s.setValue(value);
        this.setText(value);
    }

    /**
     * Reset the displayed text to a pre-defined value. This method is used to set
     * error messages in case of wrong input.
     * @param message the text to display.
     */
    @Override
    public void reset (String message) {
        s.reset();
        this.setText(message);
    }

    @Override
    public void drop () {
        s.drop();
    }
}
