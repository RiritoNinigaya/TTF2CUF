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

import org.europabarbarorum.cuf.gui.support.ValidatedUI;
import javax.swing.JPanel;
import org.europabarbarorum.cuf.shell.Shell;

/**
 * A “wizard page” in the {@link FontWizard} GUI.
 * @param <N> the type of {@link FontTreeNode} that this page edits.
 * @author Johan Ouwerkerk
 */
public abstract class FontWizardPage<N extends FontTreeNode> extends JPanel implements
        ValidatedUI {

    /**
     * Validates the UI and if valid performs
     * the action associated with the “next” button for this {@link FontWizardPage}.
     * @param wiz the {@link FontWizard} context in which the page and action occur.
     * @see #getSelected()
     */
    protected void nextAction (FontWizard wiz) {
        if (checkUI()) {
            getSelected().nextAction(wiz, this);
        }
    }

    /**
     * Initialise the {@link FontTreeNode} being edited by this {@link FontWizardPage}
     * with the data supplied in the {@link FontWizardPage} GUI form.
     * @param node the {@link FontTreeNode} to initialise.
     */
    public abstract void initialise (N node);

    /**
     * Shorthand for {@link #initialise(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) initialising}
     * and {@link FontWizard#inject(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) injecting} a given node.
     * @param wiz the {@link FontWizard} to inject the node into.
     * @param node the {@link FontTreeNode} to initialise and inject.
     */
    public void initAndInject (FontWizard wiz, N node) {
        initialise(node);
        wiz.inject(node);
    }

    /**
     * Get the context {@link Shell}.
     * @return the {@link Shell} used for resolving relative file paths and
     * executing user supplied script code.
     */
    public abstract Shell getShell ();

    /**
     * Performs the action associated with the “cancel” button for this {@link FontWizardPage}.
     * @param wiz the {@link FontWizard} context in which the page and action occur.
     */
    protected abstract void cancelAction (FontWizard wiz);

    /**
     * Get the parent of the {@link FontTreeNode} that is being edited on this page.
     * @return the parent {@link FontTreeNode} of the current node.
     */
    public abstract FontTreeNode parentNode ();

    /**
     * Get the {@link FontTypeAction} associated with the “next” button of the
     * context {@link FontWizard}.
     * @return the action to perform if “next” is clicked and the GUI is valid.
     * @see #nextAction(org.europabarbarorum.cuf.gui.fontwizard.FontWizard)
     */
    protected abstract FontTypeAction getSelected ();

    /**
     * Interface to enable multiple named (enum) options for {@link FontWizardPage#nextAction(org.europabarbarorum.cuf.gui.fontwizard.FontWizard) }
     * on the same {@link FontWizardPage}.
     */
    public static interface FontTypeAction {

        /**
         * Perform the action associated with the “next” button of the given
         * {@link FontWizard} on the given {@link FontWizardPage}.
         * @param wiz the context {@link FontWizard}.
         * @param prev the context {@link FontWizardPage}.
         */
        void nextAction (FontWizard wiz, FontWizardPage prev);
    }

    /**
     * Create a new {@link FontWizardPage}.
     */
    protected FontWizardPage () {
    }

    /**
     * Base class for a page for a series of {@link FontWizardPage pages}. 
     * This class enables returning back to the previous page, and implements {@link #initialise(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) initialisation}
     * in a recursive fashion.
     * @param <N> type of {@link FontTreeNode} to create.
     */
    public abstract static class NextFontWizardPage<N extends FontTreeNode> extends FontWizardPage<N> {

        private final FontWizardPage prev;

        @Override
        @SuppressWarnings("unchecked")
        public final void initialise (N node) {
            if (prev != null) {
                prev.initialise(node);
            }
            fillIn(node);
        }

        /**
         * Initialise the {@link FontTreeNode} with data from this {@link FontWizardPage}.
         * This method works in isolation of any previous or subsequent {@link FontWizardPage} which may
         * set push additional data into the node.
         * @param node the {@link FontTreeNode} to initialise.
         */
        protected abstract void fillIn (N node);

        /**
         * The previous {@link FontWizardPage} from which this page was reached.
         * @return the previous {@link FontWizardPage}.
         */
        protected FontWizardPage page () {
            return prev;
        }

        /**
         * Create a new {@link NextFontWizardPage}. This constructor is appropriate for
         * pages that depend on a previous {@link FontWizardPage} (in a series).
         * @param prev the previous {@link FontWizardPage}. Must be not null.
         */
        public NextFontWizardPage (FontWizardPage prev) {
            this.prev = prev;
            /*
             * FontWizardPage may be null during JUnit testing
             */
            this.shell = prev== null ? null: prev.getShell();
            this.parent = prev ==null ? null: prev.parentNode();
        }

        /**
         * Create a {@link NextFontWizardPage}. This constructor is appropriate
         * for the first {@link FontWizardPage} in a series or for a single stand-alone page.
         * @param parent the context {@link FontTreeNode} to use.
         * @param shell the context {@link Shell} to use.
         */
        public NextFontWizardPage (FontTreeNode parent, Shell shell) {
            this.shell = shell;
            this.parent = parent;
            this.prev = null;
        }
        private final Shell shell;
        private final FontTreeNode parent;

        @Override
        public Shell getShell () {
            return shell;
        }

        @Override
        public FontTreeNode parentNode () {
            return parent;
        }

        @Override
        protected void cancelAction (FontWizard wiz) {
            if (prev == null) {
                wiz.cancelWizard(parent);
            }
            else {
                wiz.setWizardPage(prev);
            }
        }
    }

    /**
     * Base class for the last page in a series of “create a new {@link FontTreeNode}” {@link FontWizardPage pages}.
     * This class implements adding the {@link FontTreeNode} into the font tree as “next” action.
     * @param <N> type of {@link FontTreeNode} to create.
     */
    public abstract static class CreateFontLastPage<N extends FontTreeNode> extends NextFontWizardPage<N> implements
            FontTypeAction {

        /**
         * Create an instance of the {@link FontTreeNode} being edited.
         * @return a new {@link FontTreeNode} object that is not yet initialised.
         */
        protected abstract N current ();

        /**
         * Create a new {@link CreateFontLastPage}.
         * @param page the previous {@link FontWizardPage}.
         */
        public CreateFontLastPage (FontWizardPage page) {
            super(page);
        }

        /**
         * Create a new {@link CreateFontLastPage}. This constructor is appropriate
         * for the first {@link FontWizardPage} in a series or for a single stand-alone page.
         * @param parent the context {@link FontTreeNode} to use.
         * @param shell the context {@link Shell} to use.
         */
        public CreateFontLastPage (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }

        @Override
        protected FontTypeAction getSelected () {
            return this;
        }

        @Override
        public void nextAction (FontWizard wiz, FontWizardPage prev) {
            N node = current();
            initialise(node);
            wiz.inject(node);
        }
    }
}
