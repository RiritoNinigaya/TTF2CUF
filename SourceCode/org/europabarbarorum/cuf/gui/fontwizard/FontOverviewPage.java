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

/*
 * FontOverviewPage.java
 *
 * Created on Oct 2, 2010, 12:31:09 PM
 */
package org.europabarbarorum.cuf.gui.fontwizard;

import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.BasicModel;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage.FontTypeAction;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage.NextFontWizardPage;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.gui.support.SettingOption;
import org.europabarbarorum.cuf.gui.support.ValidatedUI;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.IllegalMethodCallException;

/**
 * The overview {@link FontWizardPage}. This page provides the GUI controls to
 * display information on a {@link BasicModel} and edit it after it has been created and
 * {@link FontWizardPage#initialise(org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode) initialised}.
 * @param <N> the type of {@link FontTreeNode} edited by this overview page.
 * @see FontTreeNode#overviewPage()
 * @author Johan Ouwerkerk
 */
public abstract class FontOverviewPage<N extends BasicModel> extends NextFontWizardPage<N> {

    /**
     * Create a new {@link FontOverviewPage}.
     * @param node the {@link FontTreeNode} to edit.
     * @param shell the context {@link Shell} used for resolving relative path names and evaluating
     * user supplied script code.
     */
    public FontOverviewPage (N node, Shell shell) {
        super(node, shell);
        initComponents();
        init();
    }
    private Tab[] tabs;

    private void init () {
        tabs = tabs();
        for (int i = 0; i < tabs.length; ++i) {
            tabs[i].insert(pane, i);
        }
    }

    /**
     * Get a list of {@link Tab tabs} to insert in the GUI of this {@link FontOverviewPage}.
     * @return a list {@link Tab tabs} used by this {@link FontOverviewPage}.
     */
    protected abstract Tab[] tabs ();

    @Override
    protected void fillIn (N node) {
        throw new IllegalMethodCallException();
    }

    @Override
    public boolean checkUI () {
        boolean result = true;
        for (int i = 0; i < tabs.length; ++i) {
            if (!tabs[i].checkUI()) {
                pane.setSelectedIndex(i);
                result = false;
            }
        }
        return result;
    }

    private void apply () {
        for (Tab t : tabs) {
            t.apply();
        }
    }

    @Override
    protected FontTypeAction getSelected () {
        return new FontTypeAction() {

            @Override
            public void nextAction (FontWizard wiz, FontWizardPage prev) {
                apply();
                wiz.update(parentNode());
            }
        };
    }

    /**
     * List of well-known {@link Tab tab types}.
     */
    public static enum Tabs implements DefaultOption {

        /**
         * Used for mapped->normalise nodes.
         */
        Normalised,
        /**
         * Used for CUF file nodes.
         */
        CUF,
        /**
         * Used for control nodes.
         */
        Control,
        /**
         * Used for macro nodes.
         */
        Macro,
        /**
         * Used for mixer nodes.
         */
        Mixed,
        /**
         * Used for restricted->array nodes.
         */
        RestrictedArray,
        /**
         * Used for mapped->array nodes.
         */
        MappedArray,
        /**
         * Used for restricted->script nodes.
         */
        RestrictedScript,
        /**
         * Used for restricted->range nodes.
         */
        RestrictedRange,
        /**
         * Used for mapped->script nodes.
         */
        MappedScript,
        /**
         * Used for mapped->shift nodes.
         */
        MappedShift,
        /**
         * Used for fonts built from script nodes.
         */
        Script,
        /**
         * Used for system font nodes.
         */
        System,
        /**
         * Used to display meta information about font nodes.
         */
        Meta,
        /**
         * Used to for display & editing of CUF properties associated with a font node.
         */
        Properties;

        @Override
        public DefaultOption defaultOption () {
            return Meta;
        }
        /**
         * A {@link SettingOption} to lookup localised text and tooltip text for this
         * tab type.
         */
        protected final SettingOption<Tabs> option = new SettingOption<Tabs>(
                this) {

            @Override
            protected Class bundleClass () {
                return FontOverviewPage.class;
            }

            @Override
            protected Class optionClass () {
                return Tabs.class;
            }

            @Override
            protected String nameOf (Tabs value) {
                return value.name();
            }
        };
    }

    /**
     * Class to encapsulate creation of and access to {@link ComponentState} objects
     * without the using class having to know any details about this.
     * @param <S> type of {@link ComponentState GUI object} to create and manage.
     */
    public abstract static class PageComponent<S extends ComponentState> {
        
        /**
         * Performs initialisation of a {@link ComponentState} and returns it.
         * @param model the {@link ComponentModel} corresponding to the given UI.
         * @param ui the {@link ComponentState GUI} for this {@link Tab}
         * to initialise and return.
         * @return a {@link ComponentState GUI component} which is to be used as the GUI of the current {@link FontWizardPage}.
         */
        protected S sectionState(ComponentModel<S> model, S ui) {
            model.populate(ui);
            return ui;
        }
        private JPanel panel = null;

        /**
         * Create the relevant GUI. This method should not be accessed directly.
         * @return the {@link ComponentState GUI component} to use.
         * @see #panel()
         * @see #componentState()
         */
        protected abstract S createUI ();

        /**
         * Access the underlying {@link JPanel} object.
         * If the underlying {@link #createUI() GUI object} is not a {@link JPanel} this
         * method will wrap it appropriately.
         * @return the underlying GUI object as {@link JPanel}.
         * @see #componentState()
         */
        public JPanel panel () {
            if (panel == null) {
                panel = uiPanel(createUI()) ;
            }
            return panel;
        }

        @SuppressWarnings("unchecked")
        private JPanel uiPanel (S ui) {
            if (ui instanceof JPanel) {
                return (JPanel) ui;
            }
            if (ui instanceof Component) {
                return new OverviewPanel(ui);
            }
            throw new IllegalMethodCallException();
        }

        private class OverviewPanel<S extends ComponentState> extends JPanel {

            private final S overviewUI;

            private OverviewPanel (S ui) {
                this.overviewUI = ui;
                super.add((Component) ui);
            }
        }

        /**
         * Access the underlying {@link ComponentState} object.
         * @return the underlying GUI object as {@link ComponentState}.
         * @see #panel() 
         */
        @SuppressWarnings("unchecked")
        public S componentState () {
            JPanel p = panel();
            if (p instanceof OverviewPanel) {
                return (S) ((OverviewPanel) panel).overviewUI;
            }
            if (p instanceof ComponentState) {
                return (S) panel;
            }
            throw new IllegalMethodCallException();
        }
    }

    /**
     * A base class for implementing a tab in a {@link FontOverviewPage}.
     * @param <S> the type of {@link ComponentState GUI} used by this tab.
     * @param <N> the type of {@link FontTreeNode node} edited by this tab.
     */
    public abstract static class Tab<S extends ComponentState, N extends BasicModel> implements
            ValidatedUI {

        private final SettingOption<Tabs> option;
        private final N node;
        private final PageComponent<S> component;

        /**
         * Create a new {@link Tab}.
         * @param t the {@link Tabs well know tab type} this {@link Tab} represents.
         * @param node the {@link FontTreeNode} this {@link Tab} will edit.
         * @param component the {@link PageComponent} which provides
         * the {@link ComponentState GUI component} for this {@link Tab}.
         */
        protected Tab (Tabs t, N node, PageComponent<S> component) {
            this.option = t.option;
            this.node = node;
            this.component = component;
        }

        private void insert (JTabbedPane pane, int index) {
            pane.insertTab(option.getText(),
                           null,
                           component.panel(),
                           option.getTooltipText(),
                           index);
        }

        /**
         * Apply changed made through the GUI to the current {@link FontTreeNode node}.
         * @param node the {@link BasicModel node} being edited.
         */
        protected abstract void apply (N node);

        private void apply () {
            apply(node);
        }

        /**
         * Get the context {@link Shell}.
         * @return the {@link Shell} to use for resolving relative path names and
         * evaluating user supplied script code.
         */
        protected Shell getShell () {
            return node.shell();
        }

        /**
         * Access the underlying {@link ComponentState} object.
         * @return the underlying GUI object as {@link ComponentState} which displays 
         * the current {@link Tab}.
         */
        @SuppressWarnings("unchecked")
        protected S state () {
            return component.componentState();
        }

        @Override
        public boolean checkUI () {
            return state().checkUI();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pane = new JTabbedPane();
        JLabel instruction = new JLabel();

        setName("Form"); // NOI18N

        pane.setName("pane"); // NOI18N

        instruction.setHorizontalAlignment(SwingConstants.CENTER);
        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/fontwizard/FontOverviewPageUI"); // NOI18N
        instruction.setText(bundle.getString("FontOverviewPage.instruction.text")); // NOI18N
        instruction.setName("instruction"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(instruction, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(pane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(instruction)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(pane, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    JTabbedPane pane;
    // End of variables declaration//GEN-END:variables
}
