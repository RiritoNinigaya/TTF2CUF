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

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A kind of {@link JTree} which uses {@link FontTreeNode} objects for tree nodes.
 * @author Johan Ouwerkerk
 */
public class FontTree extends JTree {

    /**
     * Create a new {@link FontTree} for GUI builder purposes.
     * Makes this class a valid JavaBean.
     */
    public FontTree () {
        this(null);
    }

    /**
     * Create a new {@link FontTree}.
     * @param node the root {@link FontTreeNode} to use.
     */
    public FontTree (FontTreeNode node) {
        super(new FontTreeModel(node));
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * Get first (and only) selected {@link FontTreeNode} in this {@link FontTree}.
     * @return the {@link FontTreeNode} selected in this tree.
     */
    public FontTreeNode getSelectedNode () {
        if (getSelectionCount() == 1) {
            return (FontTreeNode) getSelectionPath().getLastPathComponent();
        }
        return null;
    }

    /**
     * A {@link DefaultTreeModel model} for a {@link FontTree}.
     * This model uses {@link FontTreeNode} for node objects.
     */
    public static class FontTreeModel extends DefaultTreeModel {

        private FontTreeModel (FontTreeNode node) {
            super(node, true);
        }

        private TreePath pathTo (FontTreeNode node) {
            return node == null ? null : new TreePath(this.getPathToRoot(node));
        }

        /**
         * Overridden version of {@link DefaultTreeModel#getRoot()} to return an instance of {@link FontTreeNode}.
         * @return the root {@link FontTreeNode} or null if this {@link FontTreeModel} is empty.
         */
        @Override
        public FontTreeNode getRoot () {
            Object o = super.getRoot();
            return o == null ? null : (FontTreeNode) o;
        }

        private void fireInsertionEvent (FontTreeNode child) {
            ping(child, inserted);
        }
        private static final int inserted = 0, removed = 1, updated = 2;

        private void fireUpdateEvent (FontTreeNode node) {
            ping(node, updated);
        }

        private void ping (FontTreeNode child, int i) {
            FontTreeNode parent = child.getParent();
            int[] indices = new int[] { parent.getIndex(child) };
            Object[] obj = new Object[] { child };
            Object[] path = getPathToRoot(parent);
            switch (i) {
                case inserted:
                    this.fireTreeNodesInserted(parent, path, indices, obj);
                    break;

                case removed:
                    this.fireTreeNodesRemoved(parent, path, indices, obj);
                    break;
                case updated:
                    this.fireTreeNodesChanged(parent, path, indices, obj);
                    break;
            }
        }

        private void fireDeletionEvent (FontTreeNode deleted) {
            ping(deleted, removed);
        }
    }

    /**
     * Signal that an {@link FontTreeNode} was updated.
     * This causes the GUI to refresh itself.
     * @param n the updated {@link FontTreeNode}.
     */
    public void update (FontTreeNode n) {
        FontTreeNode p = n.getParent();
        if (p == null) {
            getModel().setRoot(n);
        }
        else {
            getModel().fireUpdateEvent(n);
        }
    }

    /**
     * Signal that an {@link FontTreeNode} was deleted.
     * This causes the GUI to refresh itself.
     * @param n the deleted {@link FontTreeNode}.
     */
    public void delete (FontTreeNode n) {
        FontTreeNode p = n.getParent();
        if (p == null) {
            getModel().setRoot(null);
        }
        else {
            getModel().fireDeletionEvent(n);
        }
        n.remove();
    }

    /**
     * Select a {@link FontTreeNode}.
     * This causes the GUI to refresh itself.
     * @param node the {@link FontTreeNode} to select.
     */
    public void select (FontTreeNode node) {
        setSelectionPath(getModel().pathTo(node));
    }

    /**
     * Defines a number of insert operations on a {@link FontTree}.
     */
    public static enum Insertion {

        /**
         * Adds {@link FontTreeNode} objects to the parent node.
         */
        Add {

            @Override
            protected void insert (FontTreeNode node, FontTreeNode parent,
                                   FontTreeNode status) {
                parent.add(node);
            }
        },
        /**
         * Inserts {@link FontTreeNode} in the parent node before the given sibling node.
         */
        InsertBefore {

            @Override
            protected void insert (FontTreeNode node, FontTreeNode parent,
                                   FontTreeNode status) {
                node.insertBefore(status);
            }
        },
        /**
         * Inserts {@link FontTreeNode} in the parent node after the given sibling node.
         */
        InsertAfter {

            @Override
            protected void insert (FontTreeNode node, FontTreeNode parent,
                                   FontTreeNode status) {
                node.insertAfter(status);
            }
        };

        /**
         * Inserts a {@link FontTreeNode} into the {@link FontTreeModel}.
         * @param node the {@link FontTreeNode} to insert.
         * @param parent the parent {@link FontTreeNode}.
         * @param status the currently selected {@link FontTreeNode}.
         */
        protected abstract void insert (FontTreeNode node, FontTreeNode parent,
                                        FontTreeNode status);
    }
    private Insertion insertionType = Insertion.Add;

    /**
     * Configures what to do
     * when {@link #insert(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode, org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) insert}
     * is called.
     * @param type the {@link Insertion} operation to perform.
     */
    public void setInsertionType (Insertion type) {
        this.insertionType = type;
    }

    /**
     * Inserts a {@link FontTreeNode} into the {@link FontTree}.
     * This method forces a repaint of the {@link FontTreeNode} and also
     * resets the {@link Insertion insertion operation} of this {@link FontTree}
     * to {@link Insertion#Add}.
     * <p>Note that you should
     * {@link #setInsertionType(org.europabarbarorum.cuf.gui.fontwizard.FontTree.Insertion) set}
     * the appropriate {@link Insertion insertion operation} yourself before you call this method.
     * @param node the {@link FontTreeNode} to insert.
     * @param sibling a sibling {@link FontTreeNode} used to position the node in the tree.
     * Note that if the {@link FontTree} is configured to {@link Insertion#Add} add nodes then this parameter is ignored
     * and may be null.
     */
    public void insert (FontTreeNode node, FontTreeNode sibling) {
        FontTreeModel model = getModel();
        FontTreeNode parent = node.getParent();
        if (parent == null) {
            model.setRoot(node);
        }
        else {
            insertionType.insert(node, parent, sibling);
            insertionType = Insertion.Add;
            model.fireInsertionEvent(node);
            invalidate();
        }
    }

    /**
     * Overridden version of {@link JTree#getModel() } to return an instance of {@link FontTreeModel}.
     * @return the {@link FontTreeModel} used by this {@link FontTree}.
     */
    @Override
    public FontTreeModel getModel () {
        return (FontTreeModel) super.getModel();
    }
}
