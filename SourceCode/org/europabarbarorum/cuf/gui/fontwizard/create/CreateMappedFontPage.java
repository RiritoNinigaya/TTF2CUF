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
package org.europabarbarorum.cuf.gui.fontwizard.create;

import java.util.List;
import java.util.concurrent.Callable;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.MappedSource;
import org.europabarbarorum.cuf.font.MappedSource.Mapping;
import org.europabarbarorum.cuf.font.impl.WrappedSource.CompatibleSource;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.PageComponent;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.Tab;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.Tabs;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.BuildException;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.MappedFontNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.WrappedFontNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizard;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage;
import org.europabarbarorum.cuf.gui.fontwizard.MessageScripts;
import org.europabarbarorum.cuf.gui.fontwizard.WizardInstructionPage;
import org.europabarbarorum.cuf.gui.fontwizard.common.ArrayMappingUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.NormaliseUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.ScriptUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.ShiftUI;
import org.europabarbarorum.cuf.gui.fontwizard.create.CreateMappedFontPage.FontType;
import org.europabarbarorum.cuf.gui.fontwizard.create.CreateNextFontPage.FontTypeOption;
import org.europabarbarorum.cuf.gui.support.CharacterSettingArea;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.shell.FontToolkit;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.DefaultOption;

/**
 *
 * @author Johan Ouwerkerk
 */
public class CreateMappedFontPage extends CreateNextFontPage<FontType> {

    /**
     * Create a new {@link CreateMappedFontPage}.
     * @param page the previous {@link CreateFontPage}.
     */
    public CreateMappedFontPage (CreateFontPage page) {
        super(page);
    }

    @Override
    protected Class<FontType> autoType () {
        return FontType.class;
    }

    //<editor-fold defaultstate="collapsed" desc="Array mapping logic">
    /**
     * A {@link WrappedFontNode} which creates a mapped {@link CUFSource} by mapping
     * characters defined in one list to characters defined in another.
     */
    private static class MappedArrayNode extends MappedFontNode {

        private MappedArrayNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private ArrayMappingUI.Model mapping;

        @Override
        protected Mapping wrapObject () throws BuildException {
            return MappedSource.arrays(parse(mapping.from), parse(mapping.to));
        }

        private Character[] parse (final String parse) throws BuildException {
            return call(new Callable<Character[]>() {

                @Override
                public Character[] call () throws Exception {
                    return CharacterSettingArea.parse(parse);
                }
            });
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new ArrayTab(this));
            return tabs;
        }
    }

    private static class ArrayTab extends Tab<ArrayMappingUI, MappedArrayNode> {

        private ArrayTab (final MappedArrayNode n) {
            super(Tabs.MappedArray, n, new PageComponent<ArrayMappingUI>() {

                @Override
                protected ArrayMappingUI createUI () {
                    return sectionState(n.mapping, new ArrayMappingUI());
                }
            });
        }

        @Override
        protected void apply (MappedArrayNode node) {
            node.mapping = state().createModel();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Scripted mapping logic">
    /**
     * A {@link WrappedFontNode} which creates a {@link MappedSource} from
     * user supplied script code.
     */
    private static class MappedScriptNode extends MappedFontNode {

        private MappedScriptNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private ScriptUI.Model script;

        @Override
        protected Mapping wrapObject () throws BuildException {
            return script(Mapping.class, script.code, script.file);
        }

        @Override
        protected CUFSource init (CUFSource result, String name) throws
                BuildException {
            return scriptInit(result, name);
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new ScriptTab(this));
            return tabs;
        }
    }

    private static class ScriptTab extends Tab<ScriptUI, MappedScriptNode> {

        private ScriptTab (final MappedScriptNode n) {
            super(Tabs.MappedScript, n, new PageComponent<ScriptUI>() {

                @Override
                protected ScriptUI createUI () {
                    return sectionState(n.script,
                                        MessageScripts.MappingScript.getUI(n.
                            shell(), false));
                }
            });
        }

        @Override
        protected void apply (MappedScriptNode node) {
            node.script = state().createModel();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Shift mapping logic">
    /**
     * A {@link WrappedFontNode} which creates {@link CUFSource} fonts by shifting
     * the positions of character to glyph mappings in a wrapped {@link CUFSource}.
     * It uses a user supplied character/amount and direction (up/down) as parameters
     * for the shift operation.
     */
    private static class ShiftNode extends MappedFontNode {

        private ShiftNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private ShiftUI.Model shift;

        @Override
        protected Mapping wrapObject () throws BuildException {
            return MappedSource.shift(shift.amount());
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new ShiftTab(this));
            return tabs;
        }
    }

    private static class ShiftTab extends Tab<ShiftUI, ShiftNode> {

        private ShiftTab (final ShiftNode node) {
            super(Tabs.MappedShift, node, new PageComponent<ShiftUI>() {

                @Override
                protected ShiftUI createUI () {
                    return sectionState(node.shift, new ShiftUI());
                }
            });
        }

        @Override
        protected void apply (ShiftNode node) {
            node.shift = state().createModel();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Normalise mapping logic">
    /**
     * A {@link WrappedFontNode} which uses the
     * {@link FontToolkit#fixChartableCompatibilityFor(org.europabarbarorum.cuf.font.CUFSource, java.lang.String, org.europabarbarorum.cuf.font.CUFSource) chartable normalisation}
     * algorithm to remap its wrapped {@link CUFSource}.
     */
    private static class NormalisedNode extends WrappedFontNode<CUFSource> {

        /**
         * Create a new {@link NormalisedNode}.
         * @param parent the parent {@link FontTreeNode} to use.
         * @param shell the context {@link Shell} to use.
         */
        private NormalisedNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private NormaliseUI.Model tree;

        @Override
        protected CUFSource wrapObject () throws BuildException {
            return tree.realise();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected CUFSource build (CUFSource toWrap, CUFSource wrapObject) throws
                Exception {
            return new CompatibleSource(toWrap, wrapObject);
        }

        @Override
        public boolean finished () {
            return super.finished() && tree != null; // && tree.node != null;
        }

        @Override
        public FontWizardPage continueAt () {
            if (super.finished()) {
                return new NormalisedFontPage(this, shell());
            }
            else {
                return super.continueAt();
            }
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new NormalisedTab(this));
            return tabs;
        }
    }

    private static class NormalisedFontPage extends CreateFinalFontPage<NormaliseUI, NormalisedNode> {

        public NormalisedFontPage (final NormalisedNode node, Shell shell) {
            super(node, shell, new PageComponent<NormaliseUI>() {

                @Override
                protected NormaliseUI createUI () {
                    return new NormaliseUI(node);
                }
            });
        }

        @Override
        protected NormalisedNode current () {
            return (NormalisedNode) parentNode();
        }

        @Override
        public void nextAction (FontWizard wiz, FontWizardPage prev) {
            NormalisedNode c = current();
            fillIn(c);
            wiz.continueWizard(c);
        }

        @Override
        protected void cancelAction (FontWizard wiz) {
            FontTreeNode n = parentNode().getParent();
            if (n != null) {
                wiz.continueWizard(n);
            }
        }

        @Override
        protected void fillIn (NormalisedNode node) {
            node.tree = ui().createModel();
        }
    }

    private static class NormalisedTab extends Tab<NormaliseUI, NormalisedNode> {

        private NormalisedTab (final NormalisedNode node) {
            super(Tabs.Normalised, node, new PageComponent<NormaliseUI>() {

                @Override
                protected NormaliseUI createUI () {
                    return sectionState(node.tree, new NormaliseUI(node));
                }
            });
        }

        @Override
        protected void apply (NormalisedNode node) {
            node.tree = state().createModel();
        }
    }
    //</editor-fold>

    /**
     * List of {@link FontTypeOption fonts types} that can be selected in a
     * {@link CreateMappedFontPage}.
     */
    public static enum FontType implements FontTypeOption {

        /**
         * Mapping defined by {@link MappedSource#compact() compaction}.
         */
        Compact {

            @Override
            @SuppressWarnings("unchecked")
            public void nextAction (FontWizard wiz, FontWizardPage page) {
                page.initAndInject(wiz, new MappedFontNode(page.parentNode(),
                                                           page.getShell()) {

                    @Override
                    protected Mapping wrapObject () throws BuildException {
                        return MappedSource.compact();
                    }

                });
            }
        },
        /**
         * Mapping defined by two lists of characters re-mapping current positions (keys)
         * to new ones (values).
         */
        Lists {
            //<editor-fold defaultstate="collapsed" desc="Array mapping logic">

            @Override
            protected ComponentState create (Shell s) {
                return new ArrayMappingUI();
            }

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (CreateMappedFontPage prev) {
                return new CreateFinalFontPage<ArrayMappingUI, MappedArrayNode>(
                        prev, super.pc(prev)) {

                    @Override
                    protected MappedArrayNode current () {
                        return new MappedArrayNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (MappedArrayNode node) {
                        node.mapping = ui().createModel();
                    }
                };
            }
            //</editor-fold>
        },
        /**
         * Mapping defined by {@link CompatibleSource}.
         */
        Normalise {

            @Override
            @SuppressWarnings("unchecked")
            public void nextAction (FontWizard wiz, FontWizardPage prev) {
                prev.initAndInject(wiz, new NormalisedNode(prev.parentNode(),
                                                           prev.getShell()));
            }
        },
        /**
         * Mapping defined by script.
         */
        Script {
            //<editor-fold defaultstate="collapsed" desc="Scripted mapping logic">

            @Override
            protected ComponentState create (Shell s) {
                return MessageScripts.MappingScript.getUI(s, false);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (CreateMappedFontPage prev) {
                return new CreateFinalFontPage<ScriptUI, MappedScriptNode>(
                        prev,
                        super.pc(prev)) {

                    @Override
                    protected MappedScriptNode current () {
                        return new MappedScriptNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (MappedScriptNode node) {
                        node.script = ui().createModel();
                    }
                };
            }
            //</editor-fold>
        },
        /**
         * Mapping defined by shifting characters up/down a given amount.
         */
        Shift {
            //<editor-fold defaultstate="collapsed" desc="Shift mapping logic">

            @Override
            protected ComponentState create (Shell s) {
                return new ShiftUI();
            }

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (CreateMappedFontPage prev) {
                return new CreateFinalFontPage<ShiftUI, ShiftNode>(
                        prev,
                        super.pc(prev)) {

                    @Override
                    protected ShiftNode current () {
                        return new ShiftNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (ShiftNode node) {
                        node.shift = ui().createModel();
                    }
                };
            }
            //</editor-fold>
        };

        @Override
        public DefaultOption defaultOption () {
            return Script;
        }

        /**
         * Create a {@link FontWizardPage}.
         * @param prev a previous {@link CreateNextFontPage}.
         * @return the {@link FontWizardPage} to continue with.
         */
        protected FontWizardPage page (CreateMappedFontPage prev) {
            return new WizardInstructionPage(prev.parentNode(), prev.getShell());
        }

        @Override
        public void nextAction (FontWizard wiz, FontWizardPage prev) {
            wiz.setWizardPage(page((CreateMappedFontPage) prev));
        }

        /**
         * Create the relevant {@link ComponentState GUI component}.
         * @param s context {@link Shell} to pass around.
         * @return null by default: instances that need this method should override it.
         */
        protected ComponentState create (final Shell s) {
            return null;
        }

        /**
         * Generate a {@link PageComponent} for this {@link FontType}.
         * @param page the (current) context {@link FontWizardPage}.
         * @return a {@link PageComponent} which creates its {@link ComponentState GUI}
         * through {@link #create(org.europabarbarorum.cuf.shell.Shell) }.
         */
        private PageComponent pc (FontWizardPage page) {
            final Shell s = page.getShell();
            return new PageComponent() {

                @Override
                protected ComponentState createUI () {
                    return create(s);
                }
            };
        }
    }
}
