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

import java.io.File;
import java.util.List;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.impl.WrappedSource.ControlSource;
import org.europabarbarorum.cuf.font.impl.WrappedSource.MacroSource;
import org.europabarbarorum.cuf.font.pipes.CUFReader;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.PageComponent;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.Tab;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.Tabs;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.BuildException;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.ReadThroughModel;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.WrappedFontNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizard;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage;
import org.europabarbarorum.cuf.gui.fontwizard.MessageScripts;
import org.europabarbarorum.cuf.gui.fontwizard.WizardInstructionPage;
import org.europabarbarorum.cuf.gui.fontwizard.common.AbstractNameUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.AbstractNameUI.Model;
import org.europabarbarorum.cuf.gui.fontwizard.common.CUFFileUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.FontNameUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.MacroUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.ScriptUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.SystemFontUI;
import org.europabarbarorum.cuf.gui.fontwizard.create.CreateNextFontPage.FontTypeOption;
import org.europabarbarorum.cuf.gui.fontwizard.create.CreateOtherFontPage.FontType;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.shell.FontToolkit;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.MimeTag;

/**
 *
 * @author Johan Ouwerkerk
 */
public class CreateOtherFontPage extends CreateNextFontPage<FontType> {

    /**
     * Create a new {@link CreateOtherFontPage}.
     * @param prev the previous {@link CreateFontPage page}.
     */
    public CreateOtherFontPage (CreateFontPage prev) {
        super(prev);
    }

    @Override
    protected Class<FontType> autoType () {
        return FontType.class;
    }

    //<editor-fold defaultstate="collapsed" desc="CUF File node logic">
    /**
     * A {@link ReadThroughModel} to load CUF fonts from file.
     */
    private static class CUFFontNode extends ReadThroughModel {

        /**
         * Create a new {@link CUFFontNode}.
         * @param parent parent {@link FontTreeNode} to use.
         * @param shell context {@link Shell} to use.
         */
        private CUFFontNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private CUFFileUI.Model cufFile;

        @Override
        protected CUFSource source () throws Exception {
            File f = shell().pathParser().parsePath(cufFile.path);
            MimeTag.CUFFont.check(f);
            CUFReader cuf = new CUFReader();
            cuf.init(f.toString());
            return cuf;
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new CUFFontTab(this));
            return tabs;
        }
    }

    private static class CUFFontTab extends Tab<CUFFileUI, CUFFontNode> {

        private CUFFontTab (final CUFFontNode node) {
            super(Tabs.CUF, node, new PageComponent<CUFFileUI>() {

                @Override
                protected CUFFileUI createUI () {
                    return sectionState(node.cufFile,
                                        new CUFFileUI(node.shell()));
                }
            });
        }

        @Override
        protected void apply (CUFFontNode node) {
            node.cufFile = state().createModel();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Scripted font logic">
    /**
     * A {@link ReadThroughModel} which creates a {@link CUFSource} from user
     * supplied script code.
     */
    private static class ScriptNode extends ReadThroughModel {

        private ScriptNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private ScriptUI.Model script;

        @Override
        protected CUFSource source () throws Exception {
            return script(CUFSource.class, script.code, script.file);
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new ScriptTab(this));
            return tabs;
        }
    }

    private static class ScriptTab extends Tab<ScriptUI, ScriptNode> {

        private ScriptTab (final ScriptNode n) {
            super(Tabs.Script, n, new PageComponent<ScriptUI>() {

                @Override
                protected ScriptUI createUI () {
                    return sectionState(n.script,
                                        MessageScripts.ScriptFont.getUI(
                            n.shell(), false));
                }
            });
        }

        @Override
        protected void apply (ScriptNode node) {
            node.script = state().createModel();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="System font logic">
    private static class SystemFontTab extends Tab<SystemFontUI, SystemFontTreeNode> {

        private SystemFontTab (final SystemFontTreeNode node) {
            super(FontOverviewPage.Tabs.System, node, new PageComponent<SystemFontUI>() {

                @Override
                protected SystemFontUI createUI () {
                    return sectionState(node.font,
                                        new SystemFontUI(node.shell()));
                }
            });
        }

        @Override
        protected void apply (SystemFontTreeNode node) {
            node.font = state().createModel();
        }
    }

    /**
     * A {@link ReadThroughModel} which creates a {@link CUFSource} from a system font.
     */
    private static class SystemFontTreeNode extends ReadThroughModel {

        private SystemFontTreeNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private SystemFontUI.Model font;

        @Override
        protected CUFSource source () throws Exception {
            return font.getStyle().getFont(shell());
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new SystemFontTab(this));
            return tabs;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Control character logic">
    /**
     * A kind of {@link WrappedFontNode} which corresponds to an
     * {@link FontToolkit#editControlCharacters(org.europabarbarorum.cuf.font.CUFSource, java.lang.String, java.lang.String) edit control characters operation}.
     */
    private static class ControlNode extends WrappedFontNode {

        private ControlNode (FontTreeNode parent, Shell s) {
            super(parent, s);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected CUFSource build (CUFSource toWrap, Object n) throws
                Exception {
            return new ControlSource(toWrap);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Macro font logic">
    /**
     * A kind of {@link WrappedFontNode} to work with {@link MacroSource} fonts.
     */
    public static class MacroNode extends WrappedFontNode<String> {

        private MacroNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }

        /**
         * Get the macro name specified for this {@link MacroNode node}.
         * @return the name specified or null if nothing has been specified yet.
         */
        public String macro () {
            return macro == null ? null : macro.value;
        }

        @Override
        protected String wrapObject () throws BuildException {
            return getName();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected CUFSource build (CUFSource toWrap, String w) throws Exception {
            return new MacroSource(macro.value, w, toWrap);
        }

        @Override
        protected CUFSource init (CUFSource result, String name) throws
                BuildException {
            //return immediately: init already called on result during build
            return result;
        }
        private Model macro;

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new MacroTab(this));
            return tabs;
        }
    }

    private static class MacroTab extends Tab<AbstractNameUI, MacroNode> {

        private MacroTab (final MacroNode node) {
            super(Tabs.Macro, node, new PageComponent<AbstractNameUI>() {

                @Override
                protected AbstractNameUI createUI () {
                    FontTreeNode p = node.getParent();
                    return sectionState(node.macro,
                                        new MacroUI(p == null ? node : p,
                                                    node.macro.value));
                }
            });
        }

        @Override
        protected void apply (MacroNode node) {
            node.macro = state().createModel();
        }
    }
    //</editor-fold>

    /**
     * List of {@link FontTypeOption fonts types} that can be selected in a 
     * {@link CreateOtherFontPage}.
     */
    public static enum FontType implements FontTypeOption {

        /**
         * Font loaded from CUF file.
         */
        CUF {
            //<editor-fold defaultstate="collapsed" desc="CUF File node logic">

            @Override
            protected ComponentState create (Shell s) {
                return new CUFFileUI(s);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (FontWizardPage prev) {
                return new CreateFinalFontPage<CUFFileUI, CUFFontNode>(
                        prev,
                        super.pc(prev)) {

                    @Override
                    protected CUFFontNode current () {
                        return new CUFFontNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (CUFFontNode node) {
                        node.cufFile = ui().createModel();
                    }
                };
            }
            //</editor-fold>
        },
        /**
         * Font which defines a macro name on top of a nested font hierarchy.
         */
        Macro {
            //<editor-fold defaultstate="collapsed" desc="Macro font logic">

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (FontWizardPage prev) {
                return new CreateFinalFontPage<MacroUI, MacroNode>(prev,
                                                                   comp(prev)) {

                    @Override
                    protected MacroNode current () {
                        return new MacroNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (MacroNode node) {
                        node.macro = ui().createModel();
                    }
                };
            }

            private PageComponent<MacroUI> comp (FontWizardPage fwp) {
                final FontTreeNode p = fwp.parentNode();
                return new PageComponent<MacroUI>() {

                    @Override
                    protected MacroUI createUI () {
                        return new MacroUI(p, null);
                    }
                };
            }
            //</editor-fold>
        },
        /**
         * Font defined by script.
         */
        Script {
            //<editor-fold defaultstate="collapsed" desc="Scripted font logic">

            @Override
            protected ComponentState create (Shell s) {
                return MessageScripts.ScriptFont.getUI(s, false);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (FontWizardPage prev) {
                return new CreateFinalFontPage<ScriptUI, ScriptNode>(
                        prev,
                        super.pc(prev)) {

                    @Override
                    protected ScriptNode current () {
                        return new ScriptNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (ScriptNode node) {
                        node.script = ui().createModel();
                    }
                };
            }
            //</editor-fold>
        },
        /**
         * Font loaded from a system font or native font file.
         */
        System {
            //<editor-fold defaultstate="collapsed" desc="System font logic">

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (FontWizardPage prev) {
                return new CreateFinalFontPage<SystemFontUI, SystemFontTreeNode>(
                        prev,
                        super.pc(prev)) {

                    @Override
                    protected SystemFontTreeNode current () {
                        return new SystemFontTreeNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (SystemFontTreeNode node) {
                        node.font = ui().createModel();
                    }
                };
            }

            @Override
            protected ComponentState create (Shell s) {
                return new SystemFontUI(s);
            }
            //</editor-fold>
        },
        /**
         * Font defined by editing the control characters of an underlying font.
         * The result font uses a fixed 1×1 transparent pixel bitmap for:
         * \0, \r, \n, and \t.
         */
        Control {
            //<editor-fold defaultstate="collapsed" desc="Control character logic">

            @Override
            protected ComponentState create (Shell s) {
                return new FontNameUI();
            }

            @Override
            @SuppressWarnings("unchecked")
            public void nextAction (FontWizard wiz, FontWizardPage p) {
                p.initAndInject(wiz, new ControlNode(p.parentNode(),
                                                     p.getShell()));

            }
            //</editor-fold>
        };

        /**
         * Create a {@link FontWizardPage}.
         * @param prev a previous {@link CreateFontPage}.
         * @return the {@link FontWizardPage} to continue with.
         */
        protected FontWizardPage page (FontWizardPage prev) {
            return new WizardInstructionPage(prev.parentNode(), prev.getShell());
        }

        @Override
        public void nextAction (FontWizard wiz, FontWizardPage prev) {
            wiz.setWizardPage(page(prev));
        }

        @Override
        public DefaultOption defaultOption () {
            return Script;
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
