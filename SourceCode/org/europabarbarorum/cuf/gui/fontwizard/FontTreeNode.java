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

import org.europabarbarorum.cuf.gui.fontwizard.create.CreateFontPage;
import org.europabarbarorum.cuf.gui.fontwizard.common.MixerListUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.FontTableUI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.tree.TreeNode;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.MappedSource;
import org.europabarbarorum.cuf.font.MappedSource.Mapping;
import org.europabarbarorum.cuf.font.MixedSource;
import org.europabarbarorum.cuf.font.MixedSource.MixinMode;
import org.europabarbarorum.cuf.font.RestrictedSource;
import org.europabarbarorum.cuf.font.RestrictedSource.Restriction;
import org.europabarbarorum.cuf.font.impl.WrappedSource.ReadThroughSource;
import org.europabarbarorum.cuf.gui.FontTableWidgets.FontWizardWidgetsModel;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.PageComponent;
import org.europabarbarorum.cuf.gui.fontwizard.common.AttachUI.Model;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.Tab;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.Tabs;
import org.europabarbarorum.cuf.gui.fontwizard.common.AttachUI;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.shell.Shell.CallBack;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.IllegalMethodCallException;
import org.europabarbarorum.cuf.support.OptionMap;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * A {@link TreeNode} which holds additional data to generate a {@link CUFSource} from it 
 * and to provide some information needed in {@link FontWizard} to direct control flow.
 * @author Johan Ouwerkerk
 */
public abstract class FontTreeNode implements TreeNode {

    /**
     * Creates a {@link FontWizardPage} to continue where editing of this 
     * {@link FontTreeNode} previously left off. This method is only used for 
     * editing unfinished nodes.
     * @return a {@link FontWizardPage} that provides the right editor “page” to continue
     * editing this node.
     * @see #finished() 
     */
    public abstract FontWizardPage continueAt ();

    /**
     * Creates a {@link FontWizardPage} to show an overview of a
     * {@link FontTreeNode} after editing has finished. This method is only used for
     * editing finished nodes.
     * @return a {@link FontWizardPage} that provides the right overview “page” to describe this node.
     * @see #finished()
     */
    public abstract FontWizardPage overviewPage ();

    /**
     * Get the names of the child nodes of this {@link FontTreeNode}.
     * @return an array of node names, or null if this {@link FontTreeNode} is a {@link #isLeaf() leaf node}.
     */
    public abstract String[] nodeNames ();

    /**
     * Configures an {@link OptionMap} with settings to control behaviour of 
     * {@link org.europabarbarorum.cuf.font.CUFWriter CUFWriter}. This is intended for nodes to specify which
     * {@link org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty CUFProperties} should <em>not</em> automatically
     * be generated/overwritten when the {@link #create() font} is written to a file.
     * <p>
     * This implementation does nothing. Subclasses that need the control must override this method like this:
     * <blockquote><pre>
     * {@code
     * protected void setBuildOptions(OptionMap opts) {
     *      super.setShadowOptions(opts);
     *      DefaultOption opt = ... ;
     *      opts.put(opt.getClass(), opt);
     * }
     * }</pre>
     * </blockquote>     *
     * @param opts the {@link OptionMap} to initialise.
     */
    protected void setBuildOptions (OptionMap opts) {
        return;
    }

    /**
     * Obtain the {@link CUFSource} represented by this {@link FontTreeNode}.
     * @return a {@link CUFSource} created from an initialised with data set on this
     * font node.
     * @throws BuildException if an error occurs.
     */
    public abstract CUFSource create () throws BuildException;

    /**
     * Get the {@link MixinMode mode} used when mixing this {@link FontTreeNode} with other nodes.
     * @return the {@link MixinMode mode} {@link #setMode(org.europabarbarorum.cuf.font.MixedSource.MixinMode) set} on this node.
     */
    public abstract MixinMode getMode ();

    /**
     * Set the {@link MixinMode mode} used when mixing this {@link FontTreeNode} with other nodes.
     * @param mode the {@link MixinMode} to set.
     */
    protected abstract void setMode (MixinMode mode);

    /**
     * Changes the name of the {@link FontTreeNode}.
     * @param name the new name of the node.
     */
    protected abstract void setName (String name);
    private FontTreeNode parent;
    private final List<FontTreeNode> list;

    /**
     * Checks whether or not editing of this {@link FontTreeNode} completed.
     * @return true if this {@link FontTreeNode} contains all data it needs to
     * {@link #create() create} a {@link CUFSource}, false if not.
     */
    public abstract boolean finished ();

    /**
     * Finds an unfinished sub tree of this {@link FontTreeNode} (inclusive).
     * Nodes which encapsulated child nodes must check child nodes here, too.
     * @return null if this node is finished, or a {@link FontTreeNode} from
     * a sub tree (this node inclusive) that is found to be not yet {@link #finished() finished}.
     */
    protected FontTreeNode unfinished () {
        return finished() ? null : this;
    }

    private FontTreeNode (FontTreeNode parent) {

        this.parent = parent;
        this.list = isLeaf() ? null : new ArrayList<FontTreeNode>();
    }

    /**
     * Get the child {@link FontTreeNode} at the specified index.
     * @param childIndex the index of the child node to retrieve.
     * @return the child node or null if not found or if this {@link FontTreeNode} is
     * a {@link #isLeaf() leaf node}.
     */
    @Override
    public FontTreeNode getChildAt (int childIndex) {
        return isLeaf() ? null : list.get(childIndex);
    }

    /**
     * Checks if this {@link FontTreeNode} is a wrapper node.
     * @return true if this node is intended to wrap a single child node.
     */
    public abstract boolean wrapper ();

    /**
     * Get the name of this {@link FontTreeNode}.
     * @return the name of this font node.
     */
    public abstract String getName ();

    /**
     * Adds a child font node to this {@link FontTreeNode}.
     * This method may only be called if this {@link FontTreeNode} is a
     * {@link #wrapper() wrapper node} and does not contain a wrapped node already or
     * if this {@link FontTreeNode} is a {@link #mixer() nixer node}.
     * @param node the child {@link FontTreeNode} to add.
     */
    public void add (FontTreeNode node) {
        if (isLeaf() || (wrapper() && list.size() > 0)) {
            throw new IllegalMethodCallException();
        }
        list.add(node);
    }

    /**
     * Inserts a child font node to this {@link FontTreeNode}.
     * This method may only be called if this {@link FontTreeNode} is a
     * {@link #wrapper() wrapper node} and does not contain a wrapped node already or
     * if this {@link FontTreeNode} is a {@link #mixer() nixer node}.
     * @param node the child {@link FontTreeNode} to insert.
     * @param index the index at which the child node is to be inserted.
     */
    public void insert (FontTreeNode node, int index) {
        if (isLeaf() || (wrapper() && list.size() > 0)) {
            throw new IllegalMethodCallException();
        }
        list.add(index, node);
    }

    /**
     * Inserts this {@link FontTreeNode} before a sibling at its {@link #getParent() parent} node.
     * This method may be called only if the parent node contains the sibling {@link FontTreeNode} and
     * it allows {@link #insert(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode, int) insertions}.
     * @param sibling the child {@link FontTreeNode} to insert.
     */
    public void insertBefore (FontTreeNode sibling) {
        int i = parent == null ? -1 : parent.list.indexOf(sibling);
        if (i == -1) {
            throw new IllegalMethodCallException();
        }
        parent.insert(this, i);
    }

    /**
     * Inserts this {@link FontTreeNode} after a sibling at its {@link #getParent() parent} node.
     * This method may be called only if the parent node contains the sibling {@link FontTreeNode} and
     * it allows {@link #insert(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode, int) insertions}.
     * @param sibling the child {@link FontTreeNode} to insert.
     */
    public void insertAfter (FontTreeNode sibling) {
        int i = parent == null ? -1 : parent.list.indexOf(sibling);
        if (i == -1) {
            throw new IllegalMethodCallException();
        }
        parent.insert(this, ++i);
    }

    /**
     * Removes a given child node from this {@link FontTreeNode}.
     * This method may only be called if this {@link FontTreeNode} contains the
     * given child node.
     * @param node the node the remove.
     */
    public void remove (FontTreeNode node) {
        if (isLeaf() || !list.contains(node)) {
            throw new IllegalMethodCallException();
        }
        list.remove(node);
    }

    /**
     * Removes this {@link FontTreeNode} and any of its children from the entire
     * font tree.
     * @return true if this node was the root node of the font tree.
     */
    public boolean remove () {
        if (!isLeaf()) {
            this.list.clear();
        }
        if (parent != null) {
            parent.remove(this);
        }
        return parent == null;
    }

    /**
     * Get the number of child nodes of this {@link FontTreeNode}.
     * @return the number of child nodes of this node, or 0 if this node is a
     * {@link #isLeaf() leaf node}.
     */
    @Override
    public int getChildCount () {
        return isLeaf() ? 0 : list.size();
    }

    /**
     * Get the parent node of this {@link FontTreeNode}.
     * @return the parent node of this node, or null if this node is the root node.
     */
    @Override
    public FontTreeNode getParent () {
        return this.parent;
    }

    /**
     * Get the node index of this {@link FontTreeNode} in its parent node.
     * This method may only be called if this node is not the root node.
     * @return the index of this {@link FontTreeNode} among its sibling nodes.
     */
    public int index () {
        if (parent == null) {
            throw new IllegalMethodCallException();
        }
        return parent.getIndex(this);
    }

    /**
     * Checks whether or not this {@link FontTreeNode} is a mixer node.
     * @return true if this node is intended to combine multiple fonts into a single
     * one.
     */
    public abstract boolean mixer ();

    /**
     * {@link #remove(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) Removes}
     * this {@link FontTreeNode} from its parent and
     * {@link #add(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) adds} it back in the font tree
     * at the new parent.
     * This method may only be called if the given parent node allows new child nodes to
     * be {@link #add(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) added}.
     * @param parent the new parent {@link FontTreeNode} to use.
     */
    public void setParent (FontTreeNode parent) {
        if (this.parent != null) {
            this.parent.remove(this);
        }
        if (parent != null) {
            parent.add(this);
        }
        this.parent = parent;
    }

    /**
     * Get the index of a child {@link TreeNode}.
     * @param node the child node to lookup.
     * @return the index of the given {@link TreeNode} or -1 if it is not found among
     * the child nodes or if this {@link FontTreeNode} is a {@link #isLeaf() leaf node}.
     */
    @Override
    public int getIndex (TreeNode node) {
        return isLeaf() ? -1 : list.indexOf(node);
    }

    /**
     * Checks if this {@link FontTreeNode} allows children to be added.
     * @return false if this {@link FontTreeNode} is a {@link #isLeaf() leaf node}.
     */
    @Override
    public boolean getAllowsChildren () {
        return !isLeaf();
    }

    /**
     * Get the child nodes from this {@link FontTreeNode}.
     * @return an {@link Enumeration} of child nodes.
     */
    @Override
    public Enumeration<FontTreeNode> children () {
        final Iterator<FontTreeNode> iter = list.iterator();
        return new Enumeration<FontTreeNode>() {

            @Override
            public boolean hasMoreElements () {
                return iter.hasNext();
            }

            @Override
            public FontTreeNode nextElement () {
                return iter.next();
            }
        };
    }

    /**
     * Equivalent to {@link #getName() }.
     * @return the name of this {@link FontTreeNode}.
     */
    @Override
    public String toString () {
        return getName();
    }

    /**
     * A {@link BasicModel} leaf node. This model uses {@link ReadThroughSource} to abstract
     * away differences in underlying {@link CUFSource} implementations of leaf nodes.
     * @see #source() 
     */
    public static abstract class ReadThroughModel extends BasicModel {

        /**
         * Create a new {@link ReadThroughModel}.
         * @param parent the parent {@link FontTreeNode}.
         * @param shell the context {@link Shell} to use for evaluating user supplied code or
         * resolving relative file paths.
         */
        public ReadThroughModel (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }

        @Override
        public String[] nodeNames () {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected CUFSource build (String name) throws BuildException {
            CUFSource src = null;
            try {
                src = source();
            }
            catch (Throwable e) {
                throw new BuildException(e,
                                         this,
                                         Messages.UnspecifiedError,
                                         e.getLocalizedMessage());
            }
            try {
                return new ReadThroughSource(name, src);
            }
            catch (Throwable badInit) {
                throw new BuildException(badInit,
                                         this,
                                         Messages.FontUnprepared,
                                         badInit.getLocalizedMessage());
            }
        }

        /**
         * Builds the wrapped source {@link CUFSource}.
         * @return a fully initialised {@link CUFSource} created from the
         * data supplied in this {@link ReadThroughModel}.
         * @throws Exception if an error occurs.
         */
        protected abstract CUFSource source () throws Exception;
    }

    private static class FontTableTab extends Tab<FontTableUI, BasicModel> {

        private FontTableTab (final BasicModel node) {
            super(Tabs.Properties, node, new PageComponent<FontTableUI>() {

                @Override
                protected FontTableUI createUI () {
                    return new FontTableUI(node.widgetsModel);
                }
            });
        }

        @Override
        protected void apply (BasicModel node) {
            node.widgetsModel = state().createModel();
        }
    }

    private static class BasicModelTab extends Tab<AttachUI, BasicModel> {

        private BasicModelTab (final BasicModel node) {
            super(Tabs.Meta, node, new PageComponent<AttachUI>() {

                @Override
                protected AttachUI createUI () {
                    AttachUI a = new AttachUI(node.shell(), mixer(node), node.
                            getParent(), node.getName());
                    return sectionState(node.model, a);
                }

                private boolean mixer (BasicModel node) {
                    FontTreeNode n = node.getParent();
                    return n != null && n.mixer();
                }
            });
        }

        @Override
        protected void apply (BasicModel node) {
            node.model = state().createModel();
        }
    }

    /**
     * Basic abstract implementation of a {@link FontTreeNode}. This model handles additional
     * meta data/post-processing instructions attached to a {@link FontTreeNode}.
     */
    public static abstract class BasicModel extends FontTreeNode {

        /**
         * Wraps execution of a {@link Callable} to avoid arbitrary {@link Throwable errors} in
         * order to raise proper {@link BuildException} where necessary.
         * @param <R> the return type of the wrapped {@link Callable}.
         * @param call the {@link Callable} to execute.
         * @return the result of the wrapped call.
         * @throws org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.BuildException
         * if the wrapped {@link Callable} throws any {@link Throwable error}.
         */
        protected <R> R call (Callable<R> call) throws BuildException {
            try {
                return call.call();
            }
            catch (Throwable t) {
                throw new BuildException(t,
                                         this,
                                         Messages.FontUnprepared,
                                         t.getLocalizedMessage());
            }
        }

        /**
         * This {@link FontTreeNode} is assumed to be a leaf node.
         * @return true.
         */
        @Override
        public boolean isLeaf () {
            return true;
        }

        @Override
        public FontWizardPage continueAt () {
            return new CreateFontPage(this, shell);
        }

        /**
         * Gathers a {@link List} of {@link Tab} items to display in an {@link #overviewPage() overview}.
         * Subclasses which add their own {@link Tab tabs} should override this method as follows:
         * <blockquote><pre>
         * {@code
         * protected List<Tab> createTabs() {
         *      List<Tab> tabs = super.createTabs();
         *      Tab tabToAdd = ...; // create tab
         *      tabs.add(tabToAdd);
         *      return tabs;
         * }
         * }</pre>
         * </blockquote>
         * @return a {@link List} of {@link Tab} items in the order in which they are displayed.
         */
        protected List<Tab> createTabs () {
            ArrayList<Tab> tabs = new ArrayList<Tab>();
            tabs.add(new BasicModelTab(this));
            // not all BasicModel nodes need to support editing of CUF properties
            // so the model might still be null, in which case no tab must be created
            if (widgetsModel != null) {
                tabs.add(new FontTableTab(this));
            }
            return tabs;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FontWizardPage overviewPage () {
            return new FontOverviewPage(this, shell) {

                @Override
                protected Tab[] tabs () {
                    return createTabs().toArray(new Tab[] {});
                }
            };
        }
        private final Shell shell;
        private Model model;
        private FontWizardWidgetsModel widgetsModel;

        /**
         * Set the {@link FontWizardWidgetsModel} used by this {@link BasicModel}.
         * @param model the {@link FontWizardWidgetsModel} to use.
         */
        public void setTableWidgetsModel (FontWizardWidgetsModel model) {
            this.widgetsModel = model;
        }

        @Override
        public String getName () {
            return model.name.value;
        }

        @Override
        public MixinMode getMode () {
            return model.mode;
        }

        @Override
        public void setMode (MixinMode mode) {
            model.mode = mode;
        }

        @Override
        protected void setName (String name) {
            model.name.value = name;
        }

        /**
         * Set meta-data on this {@link BasicModel}.
         * @param m the {@link AttachUI} model to set.
         */
        public void setModel (Model m) {
            this.model = m;
        }

        /**
         * Create a new {@link BasicModel}.
         * @param parent the parent {@link FontTreeNode}.
         * @param shell the context {@link Shell} to use for evaluating user supplied code or
         * resolving relative file paths.
         */
        public BasicModel (FontTreeNode parent, Shell shell) {
            super(parent);
            this.shell = shell;
        }

        /**
         * Get the {@link Shell} used as context for resolving relative pathnames and executing user supplied script code.
         * @return the {@link Shell} used by this {@link BasicModel}.
         */
        public Shell shell () {
            return shell;
        }

        /**
         * Convenience method to obtain typed object instances from evaluated
         * user script code.
         * @param <R> the type of object required.
         * @param type the {@link Class} denoting the type of object required.
         * Use {@link Void} to denote a {@code void} return type.
         * @param code the raw script code to evaluate or path to script file to interpret.
         * @param fromFile true if the given code string is a path to a script file, false if it
         * is raw script code to evaluate.
         * @return the evaluated object instance required.
         * @throws ScriptException
         */
        protected <R> R script (Class<R> type,
                                String code,
                                boolean fromFile) throws ScriptException {
            Object result = fromFile ? shell.interpret(code) : shell.eval(code);
            return verifyType(type, result);
        }

        @SuppressWarnings("unchecked")
        private <R> R verifyType (Class<R> type, Object result) throws
                ScriptException {
            if (type.equals(Void.class)) {
                if (result == null) {
                    return null;
                }
                throw new ScriptException(this);
            }
            if (type.isInstance(result)) {
                return (R) result;
            }
            throw new ScriptException(this);
        }

        /**
         * Analogous method to {@link #call(java.util.concurrent.Callable) call}: this method
         * is intended to wrap a {@link Runnable} which touches objects generated by user supplied script code.
         * @param r the {@link Runnable} to wrap.
         * @throws org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.ScriptException
         * if the wrapped {@link Runnable} throws any {@link Throwable error}.
         */
        protected void script (Runnable r) throws ScriptException {
            try {
                r.run();
            }
            catch (Throwable t) {
                throw new ScriptException(this, t);
            }
        }

        @Override
        public boolean wrapper () {
            return false;
        }

        @Override
        public boolean mixer () {
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public CUFSource create () throws BuildException {
            final CUFSource f = build(getName());

            // setting a script was optional
            // so the model may point to an invalid script.
            if (model.script.valid) {
                final CallBack<CUFSource> c = script(CallBack.class,
                                                     model.script.code,
                                                     model.script.file);
                script(new Runnable() {

                    @Override
                    public void run () {
                        c.callback(f);
                    }
                });
            }
            // not all BasicModel nodes need support editing of properties
            // so this model might still be null.
            if (widgetsModel != null) {
                widgetsModel.apply(f);
            }
            return f;
        }

        /**
         * Builds the {@link CUFSource}.
         * @param name the name to use for the created {@link CUFSource}.
         * @return a {@link CUFSource} created from the
         * data supplied in this {@link ReadThroughModel}.
         * @throws BuildException if an error occurs.
         */
        protected abstract CUFSource build (String name) throws BuildException;

        @Override
        public boolean finished () {
            return isLeaf();
        }
    }

    /**
     * An {@link Exception} to indicated a failure to build a {@link CUFSource} at a specific
     * {@link FontTreeNode} chain in the font tree.
     */
    public static class BuildException extends Exception {

        /**
         * Create a new {@link BuildException}.
         * @param node the {@link FontTreeNode} which is faulty.
         * @param m the {@link BundleKey} providing a localised message format string.
         * @param args arguments to the format string.
         */
        public BuildException (FontTreeNode node, BundleKey m, Object... args) {
            super(m.format(args));
            this.node = node;
        }

        /**
         * Create a new {@link BuildException}.
         * @param cause the {@link Throwable} which caused to build to fail.
         * @param node the {@link FontTreeNode} which is faulty.
         * @param m the {@link BundleKey} providing a localised message format string.
         * @param args arguments to the format string.
         */
        public BuildException (Throwable cause, FontTreeNode node, BundleKey m,
                               Object... args) {
            super(m.format(args), cause);
            this.node = node;
        }
        private final FontTreeNode node;

        /**
         * Get the {@link FontTreeNode} which caused the {@link BuildException}.
         * @return the chain in the font tree where the build failed.
         */
        public FontTreeNode getNode () {
            return node;
        }
    }

    /**
     * A special type of {@link BuildException} that indicates failure in while executing
     * a user supplied script.
     */
    static class ScriptException extends BuildException {

        /**
         * Creates a {@link ScriptException}.
         * @param node the {@link FontTreeNode} which is faulty.
         */
        private ScriptException (FontTreeNode node) {
            super(node, Messages.ScriptError);
        }

        private ScriptException (FontTreeNode node, Throwable cause) {
            super(cause, node, Messages.ScriptError);
        }
    }

    private static class MixedFontsTab extends Tab<MixerListUI, MixedFontNode> {

        private MixedFontsTab (final MixedFontNode node) {
            super(Tabs.Mixed, node, new PageComponent<MixerListUI>() {

                @Override
                protected MixerListUI createUI () {
                    return new MixerListUI(node);
                }
            });
        }

        @Override
        protected void apply (MixedFontNode node) {

            MixerListUI.Model m = state().createModel();
            FontTreeNode child;
            List<String> names = m.names();
            List<MixinMode> modes = m.modes();
            for (int i = 0, l = names.size(); i < l; ++i) {
                child = node.getChildAt(i);
                child.setName(names.get(i));
                child.setMode(modes.get(i));
            }
        }
    }

    /**
     * A kind of {@link ParentNode} which mixes many child {@link FontTreeNode} objects into one.
     */
    public static class MixedFontNode extends ParentNode {

        /**
         * Creates a new {@link MixedFontNode}.
         * @param parent the parent {@link FontTreeNode}.
         * @param shell the context {@link Shell} to use for evaluating user supplied code or
         * resolving relative file paths.
         */
        public MixedFontNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }

        @Override
        public boolean mixer () {
            return true;
        }

        @Override
        public boolean finished () {
            return getChildCount() > 0;
        }

        @Override
        protected FontTreeNode unfinished () {
            if (finished()) {
                Enumeration<FontTreeNode> childs = children();
                FontTreeNode un = null;
                while (childs.hasMoreElements() && un == null) {
                    un = childs.nextElement().unfinished();
                }
                return un;
            }
            return this;
        }

        @Override
        public CUFSource build (String name) throws BuildException {
            CUFSource[] childs = childNodes(CUFSource.class);
            try {
                MixedSource ms = new MixedSource(childs,
                                                 childNodes(MixinMode.class));
                ms.init(name);
                return ms;
            }
            catch (Exception e) {
                throw new BuildException(this,
                                         Messages.FontMixError,
                                         e.getLocalizedMessage());
            }
        }

        @SuppressWarnings("unchecked")
        private <T> T childNode (Class<T> cls, FontTreeNode n) throws
                BuildException {
            if (cls.equals(String.class)) {
                return (T) n.getName();
            }
            if (cls.equals(MixinMode.class)) {
                return (T) n.getMode();
            }
            if (cls.equals(CUFSource.class)) {
                return (T) realise(n);
            }
            throw new IllegalMethodCallException();
        }

        @SuppressWarnings("unchecked")
        private <T> T[] childNodes (Class<T> cls) throws BuildException {
            int c = getChildCount();
            Object[] ms = cls.equals(String.class) ? new String[c] : new Object[c];
            c = 0;
            Enumeration<FontTreeNode> nodes = children();
            while (nodes.hasMoreElements()) {
                ms[c] = childNode(cls, nodes.nextElement());
                ++c;
            }
            return (T[]) ms;
        }

        @Override
        public String[] nodeNames () {
            try {
                if (getChildCount() == 0) {
                    return null;
                }
                return childNodes(String.class);
            }
            catch (Exception e) {
                IOHelp.handleExceptions(FontTreeNode.class, "nodeNames", e, e.
                        getLocalizedMessage());
                return null;
            }
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new MixedFontsTab(this));
            return tabs;
        }
    }

    /**
     * A type of {@link BasicModel} which adds convenience methods for constructing
     * child {@link FontTreeNode} objects safely.
     * @see #realise(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) 
     */
    protected static abstract class ParentNode extends BasicModel {

        /**
         * This {@link FontTreeNode} is not a leaf node.
         * @return false.
         */
        @Override
        public boolean isLeaf () {
            return false;
        }

        /**
         * Creates a new {@link ParentNode}.
         * @param parent the parent {@link FontTreeNode}.
         * @param shell the context {@link Shell} to use for evaluating user supplied code or
         * resolving relative file paths.
         */
        public ParentNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }

        /**
         * Safely create and initialise a child {@link FontTreeNode}.
         * @param child the {@link FontTreeNode} to realise.
         * @return the {@link FontTreeNode#create() created} {@link CUFSource}.
         * @throws BuildException if an error occurs. In particular an exception is
         * throw when the given child node is null.
         */
        protected CUFSource realise (FontTreeNode child) throws BuildException {
            if (child == null) {
                throw new BuildException(this, Messages.NodeMissing);
            }
            try {
                return child.create();
            }
            catch (BuildException rethrown) {
                throw rethrown;
            }
            catch (Throwable other) {
                throw new BuildException(other,
                                         child,
                                         Messages.UnspecifiedError,
                                         other.getLocalizedMessage());
            }
        }
    }

    /**
     * A {@link WrappedFontNode} which generates {@link RestrictedSource restricted fonts}.
     */
    public static abstract class RestrictedFontNode extends WrappedFontNode<Restriction> {

        /**
         * Creates a new {@link RestrictedFontNode}.
         * @param parent the parent {@link FontTreeNode}.
         * @param shell the context {@link Shell} to use for evaluating user supplied code or
         * resolving relative file paths.
         */
        public RestrictedFontNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected CUFSource build (CUFSource toWrap, Restriction wrapObject) throws
                Exception {
            return new RestrictedSource(toWrap, wrapObject);
        }
    }

    /**
     * A {@link WrappedFontNode} which generates {@link MappedSource mapped fonts}.
     */
    public static abstract class MappedFontNode extends WrappedFontNode<Mapping> {

        /**
         * Creates a new {@link MappedFontNode}.
         * @param parent the parent {@link FontTreeNode}.
         * @param shell the context {@link Shell} to use for evaluating user supplied code or
         * resolving relative file paths.
         */
        public MappedFontNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected CUFSource build (CUFSource toWrap, Mapping wrapObject) throws
                Exception {
            return new MappedSource(toWrap, wrapObject);
        }
    }

    /**
     * A type of {@link ParentNode} which wraps a single child {@link FontTreeNode}.
     * <p>
     * This class presents a highly specific sequence of {@link #build(java.lang.String) build} steps
     * in order to ensure that any errors thrown are an instance of {@link BuildException}:
     * <ol><li>Generate an auxiliary object in {@link #wrapObject() wrapObject}.
     * </li><li>Generate the wrapped {@link CUFSource} in {@link #wrapped() wrapped}.
     * </li><li>Create an instance of the specific wrapper {@link CUFSource font} in {@link #build(org.europabarbarorum.cuf.font.CUFSource, java.lang.Object) build}.
     * </li><li>Initialise the result {@link CUFSource font} in {@link #init(org.europabarbarorum.cuf.font.CUFSource, java.lang.String) init}.
     * </li></ol>
     * @param <W> the type of auxiliary object used to build the wrapped {@link CUFSource}.
     */
    public static abstract class WrappedFontNode<W> extends ParentNode {

        /**
         * Creates a new {@link WrappedFontNode}.
         * @param parent the parent {@link FontTreeNode}.
         * @param shell the context {@link Shell} to use for evaluating user supplied code or
         * resolving relative file paths.
         */
        public WrappedFontNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }

        @Override
        public boolean wrapper () {
            return true;
        }

        /**
         * Get the {@link CUFSource} defined by the wrapped {@link FontTreeNode}.
         * @return the {@link CUFSource} {@link #create() created} from the wrapped
         * {@link FontTreeNode}.
         * @throws BuildException if an error occurs
         */
        protected CUFSource wrapped () throws BuildException {
            return realise(getChildAt(0));
        }

        @Override
        public String[] nodeNames () {
            if (getChildCount() == 0) {
                return null;
            }
            return new String[] { getChildAt(0).getName() };
        }

        /**
         * Create an auxiliary object to generate the {@link CUFSource} represented by this
         * {@link WrappedFontNode}.
         * @return null by default: subclasses should override this method.
         * @throws org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.BuildException
         * if an error occurs.
         */
        protected W wrapObject () throws BuildException {
            return null;
        }

        /**
         * Equivalent to {@link #init(org.europabarbarorum.cuf.font.CUFSource, java.lang.String) } intended for
         * fonts which are built with user supplied script code as a prime ingredient.
         * @param f the {@link CUFSource font} to {@link CUFSource#init(java.lang.String) initialise}.
         * @param name the font name to use.
         * @return the given font after initialisation.
         * @throws org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.ScriptException
         * if an error occurs.
         */
        protected final CUFSource scriptInit (final CUFSource f,
                                              final String name) throws
                ScriptException {
            script(new Runnable() {

                @Override
                public void run () {
                    f.init(name);
                }
            });
            return f;
        }

        @Override
        protected CUFSource build (String name) throws BuildException {
            final CUFSource w = wrapped();
            final W object = wrapObject();
            CUFSource r = call(new Callable<CUFSource>() {

                @Override
                public CUFSource call () throws Exception {
                    return build(w, object);
                }
            });
            return init(r, name);
        }

        /**
         * Wrap a {@link CUFSource}. This method is responsible for creating an instance of the
         * wrapping {@link CUFSource font} only.
         * @param toWrap the {@link CUFSource} to wrap.
         * @param wrapObject addition parameter {@link #wrapObject() created} by this
         * {@link WrappedFontNode} for use here.
         * @return the wrapped font.
         * @throws Exception if an error occurs.
         */
        protected abstract CUFSource build (CUFSource toWrap, W wrapObject) throws
                Exception;

        /**
         * Initialise a created {@link CUFSource}. Subclasses which rely on user supplied script code
         * as prime ingredient of the font should override this method, probably by deferring to
         * {@link #scriptInit(org.europabarbarorum.cuf.font.CUFSource, java.lang.String) }.
         * @param result the {@link CUFSource font} to {@link CUFSource#init(java.lang.String) initialise}.
         * @param name the font name to use.
         * @return the given font after initialisation.
         * @throws org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.BuildException
         */
        protected CUFSource init (CUFSource result, String name) throws
                BuildException {
            result.init(name);
            return result;
        }

        @Override
        public boolean finished () {
            return getChildCount() == 1;
        }

        @Override
        protected FontTreeNode unfinished () {
            return finished() ? getChildAt(0).unfinished() : this;
        }
    }
}
