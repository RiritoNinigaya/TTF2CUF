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

import org.europabarbarorum.cuf.gui.fontwizard.common.MixerListUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.AttachUI;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.MixedFontNode;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.ParentNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.ReadThroughModel;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.ScriptException;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * List of {@link BundleKey} messages used in the {@code org.europabarbarorum.cuf.gui.fontwizard} package.
 * @author Johan Ouwerkerk
 */
public enum Messages implements BundleKey {

    /**
     * Emitted when a {@link ParentNode} is realised but it has no child node(s).
     * Takes no arguments.
     */
    NodeMissing(ParentNode.class),
    /**
     * Emitted when some unspecified error occurs during realisation of a sub font of
     * a {@link ParentNode}. Takes a single error message argument.
     */
    UnspecifiedError(ParentNode.class),
    /**
     * Emitted when a {@link ReadThroughModel} attempts to use a sub font that is
     * not fully prepared.
     * Takes no arguments.
     */
    FontUnprepared(ReadThroughModel.class),
    /**
     * Emitted when a {@link ScriptException} is thrown.
     * Takes no arguments.
     */
    ScriptError(ScriptException.class),
    /**
     * Emitted when the user fails to supply a valid font node name in
     * {@link AttachUI}. Takes no arguments.
     */
    InvalidFontNodeName(AttachUI.class),
    /**
     * Emitted when a font node name has already been used for a sibling node.
     * Takes no arguments.
     */
    FontNodeNameTaken(AttachUI.class),
    /**
     * Emitted when a the child nodes of a mixer node prove not to mix.
     * Takes an error message argument.
     */
    FontMixError(MixedFontNode.class),
    /**
     * Used for name column header of font table in mixed node overview page.
     * Takes no arguments.
     */
    FontName(MixerListUI.class),
    /**
     * Used for mode column header of font table in mixed node overview page.
     * Takes no arguments.
     */
    FontMode(MixerListUI.class),
    /**
     * Emitted when font is built successfully.
     * Takes no arguments.
     */
    FontBuildFinished(ExportDialog.class),
    /**
     * Emitted when a font failed to build.
     * Takes no arguments.
     */
    FontBuildFailed(ExportDialog.class),
    /**
     * Font compilation failed. Takes no arguments.
     */
    FontCompileFailed(ExportDialog.class),
    /**
     * Macro compilation failed. Takes no arguments.
     */
    MacroCompileFailed(ExportDialog.class),
    /**
     * Job title used when building fonts.
     * Takes no arguments.
     */
    FontBuildTitle(ExportDialog.class),
    /**
     * Job title used when saving both macros and cuf file.
     * Takes no arguments.
     */
    ExportSaveTitle(ExportDialog.class),
    /**
     * Title of an {@link ExportDialog}, takes a font node name as argument.
     */
    ExportDialogTitle(ExportDialog.class),
    /**
     * Question message to prompt for confirmation when an {@link ExportDialog} is
     * closed while a job is running. Takes a job title argument.
     */
    ExportDialogClose(ExportDialog.class),
    /**
     * Title of the dialog used to prompt for confirmation when an {@link ExportDialog} is
     * closed while a job is running. Takes no arguments.
     */
    ExportDialogCloseTitle(ExportDialog.class),
    /**
     * Message of confirmation dialog used when a {@link FontWizard} is closed and
     * some work has not yet been exported. Takes no arguments.
     */
    FontWizardClose(FontWizard.class),
    /**
     * Title of confirmation dialog used when a {@link FontWizard} is closed and
     * some work has not yet been exported. Takes no arguments.
     */
    FontWizardCloseTitle(FontWizard.class);

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
