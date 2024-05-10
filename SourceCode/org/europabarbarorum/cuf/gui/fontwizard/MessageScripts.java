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
package org.europabarbarorum.cuf.gui.fontwizard;

import org.europabarbarorum.cuf.gui.fontwizard.common.ScriptUI;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * List of {@link BundleKey} messages used in lieu of example scripts.
 * All scripts take a language and a shell name as arguments.
 * @author Johan Ouwerkerk
 */
public enum MessageScripts implements BundleKey {
    /**
     * Used in the GUI form for a restricted font based around user code.
     */
    RestrictionScript,
    /**
     * Used in the GUI form for a mapped font based around user code.
     */
    MappingScript,
    /**
     * Used in the GUI form for a font created from user code.
     */
    ScriptFont,
    /**
     * Used in the GUI form for attaching a script as meta information to
     * a font node.
     */
    AttachUI;

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, MessageScripts.class);
    }

    @Override
    public Class type () {
        return MessageScripts.class;
    }

    /**
     * Convenience method to create a {@link ScriptUI} for this {@link MessageScripts script}.
     * @param context the {@link Shell} used to resolve relative path names and evaluate
     * user script code.
     * @param optional whether or not the user may leave the form blank.
     * @return the {@link ScriptUI} created and initialised to display this example {@link MessageScripts script}.
     */
    public ScriptUI getUI(Shell context, boolean optional) {
        ScriptUI ui = new ScriptUI(context, optional);
        ui.setScript(this);
        return ui;
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, MessageScripts.class, args);
    }
}
