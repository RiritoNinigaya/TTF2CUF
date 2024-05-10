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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.RestrictedSource;
import org.europabarbarorum.cuf.font.RestrictedSource.Restriction;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.PageComponent;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.Tab;
import org.europabarbarorum.cuf.gui.fontwizard.FontOverviewPage.Tabs;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.BuildException;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.RestrictedFontNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.WrappedFontNode;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizard;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage;
import org.europabarbarorum.cuf.gui.fontwizard.MessageScripts;
import org.europabarbarorum.cuf.gui.fontwizard.WizardInstructionPage;
import org.europabarbarorum.cuf.gui.fontwizard.common.ArrayRestrictionUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.CharacterRestrictionUI;
import org.europabarbarorum.cuf.gui.fontwizard.common.ScriptUI;
import org.europabarbarorum.cuf.gui.fontwizard.create.CreateNextFontPage.FontTypeOption;
import org.europabarbarorum.cuf.gui.fontwizard.create.CreateRestrictedFontPage.FontType;
import org.europabarbarorum.cuf.gui.support.CharacterSettingArea;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.DefaultOption;

/**
 *
 * @author Johan Ouwerkerk
 */
public class CreateRestrictedFontPage extends CreateNextFontPage<FontType> {

    /**
     * Create a new {@link CreateRestrictedFontPage}.
     * @param prev the previous {@link FontWizardPage}.
     */
    public CreateRestrictedFontPage (CreateFontPage prev) {
        super(prev);
    }

    @Override
    protected Class<FontType> autoType () {
        return FontType.class;
    }

    //<editor-fold defaultstate="collapsed" desc="Scripted restriction logic">
    /**
     * A {@link WrappedFontNode} which creates {@link RestrictedSource} fonts from
     * user supplied script code.
     */
    private static class RestrictedScriptNode extends RestrictedFontNode {

        private RestrictedScriptNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private ScriptUI.Model script;

        @Override
        protected Restriction wrapObject () throws BuildException {
            return script(Restriction.class, script.code, script.file);
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

    private static class ScriptTab extends Tab<ScriptUI, RestrictedScriptNode> {

        private ScriptTab (final RestrictedScriptNode n) {
            super(Tabs.RestrictedScript, n, new PageComponent<ScriptUI>() {

                @Override
                protected ScriptUI createUI () {
                    return sectionState(n.script,
                                        MessageScripts.RestrictionScript.getUI(n.
                            shell(), false));
                }
            });
        }

        @Override
        protected void apply (RestrictedScriptNode node) {
            node.script = state().createModel();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Array restriction logic">
    /**
     * A {@link WrappedFontNode} which creates {@link CUFSource} fonts by restricting
     * a wrapped {@link CUFSource}. It uses a user supplied list of characters to
     * include or exclude.
     */
    private static class ArrayRestrictionNode extends RestrictedFontNode {

        private ArrayRestrictionNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private ArrayRestrictionUI.Model restriction;

        @Override
        protected Restriction wrapObject () throws BuildException {
            Character[] list = call(new Callable<Character[]>() {

                @Override
                public Character[] call () throws Exception {
                    return CharacterSettingArea.parse(restriction.list);
                }
            });
            return RestrictedSource.collection(Arrays.asList(list),
                                               !restriction.invert);
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new ArrayTab(this));
            return tabs;
        }
    }

    private static class ArrayTab extends Tab<ArrayRestrictionUI, ArrayRestrictionNode> {

        private ArrayTab (final ArrayRestrictionNode n) {
            super(Tabs.RestrictedArray, n, new PageComponent<ArrayRestrictionUI>() {

                @Override
                protected ArrayRestrictionUI createUI () {
                    return sectionState(n.restriction, new ArrayRestrictionUI());
                }
            });
        }

        @Override
        protected void apply (ArrayRestrictionNode node) {
            node.restriction = state().createModel();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Range restriction logic">
    /**
     * A {@link WrappedFontNode} which creates {@link CUFSource} fonts by restricting
     * a wrapped {@link CUFSource}. It uses a user supplied range of characters to
     * include or exclude.
     */
    private static class RangeNode extends RestrictedFontNode {

        private RangeNode (FontTreeNode parent, Shell shell) {
            super(parent, shell);
        }
        private CharacterRestrictionUI.Model range;

        @Override
        protected Restriction wrapObject () throws BuildException {
            return RestrictedSource.range(range.lowerChar(),
                                          range.upperChar(),
                                          !range.excludesRange);
        }

        @Override
        protected List<Tab> createTabs () {
            List<Tab> tabs = super.createTabs();
            tabs.add(new RangeTab(this));
            return tabs;
        }
    }

    private static class RangeTab extends Tab<CharacterRestrictionUI, RangeNode> {

        private RangeTab (final RangeNode node) {
            super(Tabs.RestrictedRange, node, new PageComponent<CharacterRestrictionUI>() {

                @Override
                protected CharacterRestrictionUI createUI () {
                    return sectionState(node.range, new CharacterRestrictionUI());
                }
            });
        }

        @Override
        protected void apply (RangeNode node) {
            node.range = state().createModel();
        }
    }
    //</editor-fold>

    /**
     * List of {@link FontTypeOption fonts types} that can be selected in a
     * {@link CreateRestrictedFontPage}.
     */
    public static enum FontType implements FontTypeOption {

        /** 
         * Restriction defined by script. 
         */
        Script {
            //<editor-fold defaultstate="collapsed" desc="Scripted restriction logic">

            @Override
            protected ComponentState create (Shell s) {
                return MessageScripts.RestrictionScript.getUI(s, false);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (CreateRestrictedFontPage prev) {
                return new CreateFinalFontPage<ScriptUI, RestrictedScriptNode>(
                        prev,
                        super.pc(prev)) {

                    @Override
                    protected RestrictedScriptNode current () {
                        return new RestrictedScriptNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (RestrictedScriptNode node) {
                        node.script = ui().createModel();
                    }
                };
            }
            //</editor-fold>
        },
        /**
         * Restriction defined by a list of characters to include/exclude.
         */
        List {
            //<editor-fold defaultstate="collapsed" desc="Array restriction logic">

            @Override
            protected ComponentState create (Shell s) {
                return new ArrayRestrictionUI();
            }

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (CreateRestrictedFontPage prev) {
                return new CreateFinalFontPage<ArrayRestrictionUI, ArrayRestrictionNode>(
                        prev,
                        super.pc(prev)) {

                    @Override
                    protected ArrayRestrictionNode current () {
                        return new ArrayRestrictionNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (ArrayRestrictionNode node) {
                        node.restriction = ui().createModel();
                    }
                };
            }
            //</editor-fold>
        },
        /**
         * Restriction defined by a range of characters to include/exclude.
         */
        Range {
            //<editor-fold defaultstate="collapsed" desc="Range restriction logic">

            @Override
            protected ComponentState create (Shell s) {
                return new CharacterRestrictionUI();
            }

            @Override
            @SuppressWarnings("unchecked")
            protected FontWizardPage page (CreateRestrictedFontPage prev) {
                return new CreateFinalFontPage<CharacterRestrictionUI, RangeNode>(
                        prev,
                        super.pc(prev)) {

                    @Override
                    protected RangeNode current () {
                        return new RangeNode(parentNode(), getShell());
                    }

                    @Override
                    protected void fillIn (RangeNode node) {
                        node.range = ui().createModel();
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
         * @param prev a previous {@link CreateRestrictedFontPage}.
         * @return the {@link FontWizardPage} to continue with.
         */
        protected FontWizardPage page (CreateRestrictedFontPage prev) {
            return new WizardInstructionPage(prev.parentNode(), prev.getShell());
        }

        @Override
        public void nextAction (FontWizard wiz, FontWizardPage prev) {
            wiz.setWizardPage(page((CreateRestrictedFontPage) prev));
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
