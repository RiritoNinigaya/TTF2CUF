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
package org.europabarbarorum.cuf.gui;

import javax.swing.tree.TreeNode;
import org.europabarbarorum.cuf.font.CUFSource;

/**
 * Interface to provide the default {@link TreeNode} with additional methods so that each
 * {@link CUFSource} in the hierarchy of a given font can be configured with its own (possibly unique)
 * preview. This is useful for providing more meaningful previews targeted at the individual components of a
 * CUF font; e.g. the US ASCII characters of the font may use the canonical “Quick Brown Fox” and the
 * <abbr title="Common Chinese/Japanese/Korean ideograms">CJK</abbr> component may
 * have a different preview since “Jumps Over The Lazy Dog” contains no CJK character.
 * @author Johan Ouwerkerk
 */
public interface CUFTreeNode extends TreeNode {

    /**
     * Get the preview text used to preview the {@link CUFSource} object at this {@link CUFTreeNode}.
     * @return a string to use for previewing the font.
     */
    String getPreview ();

    /**
     * Set the preview text used to preview the {@link CUFSource} object at this {@link CUFTreeNode}.
     * @param text the preview text to use.
     */
    void setPreview (String text);

    /**
     * Get the {@link CUFSource} stored at this node.
     * @return the font stored at this node.
     */
    CUFSource getSource ();
}
