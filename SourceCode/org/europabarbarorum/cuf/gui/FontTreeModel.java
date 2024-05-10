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

import org.europabarbarorum.cuf.gui.support.UpdatableModel;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.TopLevelSource;

/**
 * Class to map the hierarchy of a {@link CUFSource} to that of a {@link DefaultTreeModel}.
 * @author Johan Ouwerkerk
 */
public class FontTreeModel extends DefaultTreeModel implements
        UpdatableModel<CUFSource> {

    /**
     * Create an (empty) {@link FontTreeModel}.
     */
    protected FontTreeModel () {
        super(null);
    }


    @Override
    public void updateModel (CUFSource source) {
        this.setRoot(new Node(source, null));
    }

    /**
     *
     * @param string the string to set as default preview text for all nodes in a {@link FontTreeModel}.
     */
    public static void setPreviewDefault (String string) {
        Node.__preview__ = string;
    }
}

class Node implements CUFTreeNode {

    protected static String __preview__;
    
    private final CUFSource self;
    private final CUFSource[] srcs;
    private final ArrayList<CUFTreeNode> tree;
    private final CUFTreeNode parent;

    protected Node (CUFSource cs, CUFTreeNode pn) {

        self = cs;
        parent = pn;
        if (self != null && self instanceof TopLevelSource) {
            srcs = ((TopLevelSource) self).getSources();
            tree = new ArrayList<CUFTreeNode>(srcs.length);
            for (CUFSource s : srcs) {
                tree.add(new Node(s, pn));
            }
        }
        else {
            srcs = null;
            tree = null;
        }
    }

    @Override
    public CUFSource getSource () {
        return self;
    }

    @Override
    public TreeNode getChildAt (int childIndex) {
        if (tree == null) {
            return null;
        }

        return tree.get(childIndex);
    }

    @Override
    public int getChildCount () {
        return tree == null ? 0 : tree.size();
    }

    @Override
    public TreeNode getParent () {
        return parent;
    }

    @Override
    public int getIndex (TreeNode node) {
        return tree.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren () {
        return true;
    }

    @Override
    public boolean isLeaf () {
        return getChildCount() == 0;
    }

    @Override
    public Enumeration children () {
        return new Enumeration() {

            private int k = 0;

            @Override
            public boolean hasMoreElements () {
                return k < getChildCount();
            }

            @Override
            public Object nextElement () {
                Object o = getChildAt(k);
                ++k;
                return o;
            }
        };
    }

    @Override
    public String toString () {
        return self == null ? Messages.FontNodeIllegal.getText() : self.getCufSource();
    }
    private String preview;

    @Override
    public String getPreview () {
        return preview == null ? __preview__ : preview;
    }

    @Override
    public void setPreview (String text) {
        preview = text;
    }
}
