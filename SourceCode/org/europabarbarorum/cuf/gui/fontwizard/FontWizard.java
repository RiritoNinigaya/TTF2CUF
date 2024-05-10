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
 * FontWizard.java
 *
 * Created on Sep 19, 2010, 1:29:12 AM
 */
package org.europabarbarorum.cuf.gui.fontwizard;

import org.europabarbarorum.cuf.gui.fontwizard.create.CreateFontPage;
import org.europabarbarorum.cuf.gui.support.ScrollingSupport;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.gui.fontwizard.FontTree.FontTreeModel;
import org.europabarbarorum.cuf.gui.fontwizard.FontTree.Insertion;
import org.europabarbarorum.cuf.gui.support.ButtonPanel;
import org.europabarbarorum.cuf.gui.support.ReplacementPanel;
import org.europabarbarorum.cuf.gui.support.UIHelp;
import org.europabarbarorum.cuf.gui.support.UIHelp.Command;
import org.europabarbarorum.cuf.shell.CompileUnit;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.shell.Shell.CallBack;
import org.europabarbarorum.cuf.support.PathParser;
import org.europabarbarorum.cuf.support.ResourceHelp;

/**
 * A “wizard” type user interface to create a CUF font through a GUI.
 * @author Johan Ouwerkerk
 */
public class FontWizard extends JFrame {

    /**
     * A context for building a {@link CUFSource} from a {@link FontTreeNode}.
     * This context provides the necessary parameters that are omitted from the
     * {@link FontWizard}, and also allows access to the {@link PathParser context used for parsing path names}.
     */
    public static class FontWizardContext {

        private final String name, lang;
        private final Shell ctx;

        private FontWizardContext (Shell s) {
            this.name = s.shellName();
            this.lang = s.loadedLanguage();
            this.ctx = s;
        }

        /**
         * Get the context used for parsing path names.
         * @return the {@link PathParser} used for resolving file paths.
         */
        public PathParser pathParser () {
            return ctx.pathParser();
        }

        /**
         * Run a {@link CompileUnit compilation task}.
         * @param unit the {@link CompileUnit task} to run.
         * @param call a {@link CallBack} to receive an error code when the given
         * task finishes or is aborted. This {@link CallBack} runs out of context.
         * @see Shell#fork(org.europabarbarorum.cuf.shell.CompileUnit, java.lang.String, java.lang.String, java.io.PrintStream, java.io.PrintStream, org.europabarbarorum.cuf.shell.Shell.CallBack, boolean)
         */
        public void run (final CompileUnit unit, final CallBack<Integer> call) {
            ctx.fork(unit, lang, name, null, null, call, false);
        }
    }
    private FontWizardContext context;

    /**
     * Create a  {@link FontWizard}.
     * @param context the context {@link Shell} to use.
     */
    public FontWizard (FontWizardContext context) {
        this.context = context;
        action = new UIHelp<FontWizard, FontTreeNode>(this) {

            @Override
            public Command<FontWizard, FontTreeNode> find (String name) {
                return Actions.valueOf(name);
            }
        };
        initComponents();
        action.update(null);

    }
    private final UIHelp<FontWizard, FontTreeNode> action;

    /**
     * Insert a {@link FontTreeNode} into the font tree and continue editing the font tree.
     * @param node the {@link FontTreeNode} to insert.
     */
    public void inject (FontTreeNode node) {
        fontTree.insert(node, action.status());
        rootExported = false;
        this.continueWizard(findUnfinishedNode(node), node);
    }

    private void start () {
        setWizardPage(new CreateFontPage(null, context.ctx));
    }

    /**
     * Cancel the current series of {@link FontWizardPage pages} being displayed.
     * @param parent the {@link FontTreeNode} at which the series is canceled.
     */
    protected void cancelWizard (FontTreeNode parent) {
        FontTreeModel model = fontTree.getModel();
        setOverview(parent == null ? model.getRoot() : parent);
    }

    /**
     * Signal that a {@link FontTreeNode} was updated. This causes the GUI to refresh itself.
     * @param node the node which was updated.
     */
    protected void update (FontTreeNode node) {
        fontTree.update(node);
        rootExported = false;
        this.continueWizard(findUnfinishedNode(node), node);
    }

    /**
     * Attempt to find an {@link FontTreeNode#unfinished() unfinished node} to continue with
     * from the root of the font tree. Alternatively displays
     * status information on the given {@link FontTreeNode}.
     * @param node the {@link FontTreeNode} to display as alternative.
     */
    public void continueWizard (FontTreeNode node) {
        this.continueWizard(findUnfinishedNode(fontTree.getModel().getRoot()),
                            node);
    }

    private void setOverview (FontTreeNode node) {
        autoSelect(node);

        /*
         * Condition below should be true only if:
         * the user just cancelled a root node or:
         * fixed the last details of a font, making it now <finished>.
         * This means that the selected <node> just changed status,
         * which needs to be propagated to the UI.
         */
        if (node == null || node.equals(action.status())) {
            action.ensureUpdate(node);
            showOverview(node);
        }
    }

    private void autoSelect (FontTreeNode n) {
        FontTreeNode n2 = action.status();
        if (n2 == null || n == null || !n2.equals(n)) {
            treeListener.touch(false);
            fontTree.select(n);
        }
    }

    /**
     * Tries to continue with a given {@link FontTreeNode#unfinished() unfinished node} if
     * possible. Alternatively displays status information on a given alternative {@link FontTreeNode}.
     * @param cont the {@link FontTreeNode#unfinished() unfinished node}
     * to continue with. May be null if no such node can be found.
     * @param node the {@link FontTreeNode} to display as alternative.
     */
    protected void continueWizard (FontTreeNode cont, FontTreeNode node) {
        if (cont == null) {
            setOverview(node);
        }
        else {
            autoSelect(cont);
            setWizardPage(cont.continueAt());
        }
    }

    private FontTreeNode findUnfinishedNode (FontTreeNode subtree) {
        FontTreeNode can = subtree.unfinished();
        if (can == null) {
            FontTreeNode p = subtree.getParent();
            if (p != null) {
                return findUnfinishedNode(p);
            }
        }
        return can;
    }

    /**
     * Set the {@link FontWizardPage} to be displayed by this {@link FontWizard}.
     * @param page the {@link FontWizardPage} to use.
     */
    @SuppressWarnings("unchecked")
    public void setWizardPage (FontWizardPage page) {
        if (wizardPanel.swap(page) instanceof CreateFontPage || page instanceof CreateFontPage) {
            action.ensureUpdate(null);
        }
        int select = page instanceof FontOverviewPage ? 1 : 0;
        if (select != cancelBackPanel.current()) {
            cancelBackPanel.show(select);
            applyNextPanel.show(select);
        }
        wizardScroll.invalidate();
    }

    /**
     * Get the {@link FontWizardPage} currently displayed by this {@link FontWizard}.
     * @return the currently used {@link FontWizardPage}. May be null if no page is yet in use.
     */
    protected FontWizardPage page () {
        Object p = wizardPanel.panel();
        return p instanceof FontWizardPage ? (FontWizardPage) p : null;
    }
    private TouchTreeSelectionListener treeListener =
            new TouchTreeSelectionListener();

    private class TouchTreeSelectionListener implements TreeSelectionListener {

        private boolean _touch = true;

        private void touch (boolean b) {
            _touch = b;
        }

        @Override
        public void valueChanged (TreeSelectionEvent e) {
            TreePath p = e.getNewLeadSelectionPath();
            Object o = p == null ? null : p.getLastPathComponent();
            FontTreeNode node = o == null ? null : (FontTreeNode) o;
            FontTreeNode status = action.status();
            action.update(node);
            if (_touch) {
                if ((node == null && status != null)
                        || (status == null || !status.equals(node))) {
                    showOverview(node);
                }
            }
            else {
                _touch = true;
            }
        }
    }

    private void showOverview (FontTreeNode node) {
        if (node != null) {
            setWizardPage(node.overviewPage());
        }
    }
    private boolean rootExported = true;

    private boolean doClose () {
        int i = JOptionPane.showConfirmDialog(
                this,
                Messages.FontWizardClose.getText(),
                Messages.FontWizardCloseTitle.getText(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return i == JOptionPane.YES_OPTION;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        JPopupMenu treeMenu = new JPopupMenu(); // NOI18N
        JMenuItem addItem = new JMenuItem();
        JMenuItem removeItem = new JMenuItem();
        JMenuItem insertBeforeItem = new JMenuItem();
        JMenuItem insertAfterItem = new JMenuItem();
        JMenuItem exportCurrentItem = new JMenuItem();
        JButton cancelWizard = new JButton();
        JButton nextWizard = new JButton();
        JButton applyWizard = new JButton();
        JButton backWizard = new JButton();
        JToolBar toolbar = new JToolBar();
        Separator jSeparator1 = new Separator();
        JButton addButton = new JButton();
        Separator jSeparator2 = new Separator();
        JButton removeButton = new JButton();
        Separator jSeparator5 = new Separator();
        JButton insertBeforeButton = new JButton();
        Separator jSeparator4 = new Separator();
        JButton insertAfterButton = new JButton();
        Separator jSeparator3 = new Separator();
        JButton exportCurrentButton = new JButton();
        Separator jSeparator6 = new Separator();
        JButton exportRootButton = new JButton();
        Separator jSeparator7 = new Separator();
        JScrollPane treeScroll = new ScrollingSupport();
        fontTree = new FontTree();
        wizardScroll = new ScrollingSupport();
        wizardPanel = new ReplacementPanel();
        applyNextPanel =
        new ButtonPanel(new JButton[] { nextWizard, applyWizard }, 0);
        cancelBackPanel =
        new ButtonPanel(new JButton[] { backWizard, cancelWizard },0);

        action.bind(addItem, Actions.Add, false);
        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/fontwizard/FontWizardUI");
        addItem.setText(bundle.getString("FontWizard.addButton.text")); // NOI18N
        treeMenu.add(addItem);

        action.bind(removeItem, Actions.Remove, false);
        removeItem.setText(bundle.getString("FontWizard.removeButton.text")); // NOI18N
        treeMenu.add(removeItem);

        action.bind(insertBeforeItem, Actions.InsertBefore, false);
        insertBeforeItem.setText(bundle.getString("FontWizard.insertBeforeButton.text")); // NOI18N
        treeMenu.add(insertBeforeItem);

        action.bind(insertAfterItem, Actions.InsertAfter, false);
        insertAfterItem.setText(bundle.getString("FontWizard.insertAfterButton.text")); // NOI18N
        treeMenu.add(insertAfterItem);

        action.bind(exportCurrentItem, Actions.ExportCurrent,false);
        exportCurrentItem.setText(bundle.getString("FontWizard.exportCurrentButton.text")); // NOI18N
        treeMenu.add(exportCurrentItem);

        action.bind(cancelWizard, Actions.Cancel);
        cancelWizard.setText(bundle.getString("FontWizard.cancelWizard.text")); // NOI18N

        action.bind(nextWizard, Actions.Next);
        nextWizard.setText(bundle.getString("FontWizard.nextWizard.text")); // NOI18N

        action.bind(applyWizard, Actions.Next);
        applyWizard.setText(bundle.getString("FontWizard.applyWizard.text")); // NOI18N

        action.bind(backWizard, Actions.Cancel);
        backWizard.setText(bundle.getString("FontWizard.backWizard.text")); // NOI18N

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(bundle.getString("FontWizard.title.")); // NOI18N
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(jSeparator1);

        action.bind(addButton, Actions.Add);
        addButton.setText(bundle.getString("FontWizard.addButton.text")); // NOI18N
        addButton.setFocusable(false);
        addButton.setHorizontalTextPosition(SwingConstants.CENTER);
        addButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        toolbar.add(addButton);
        toolbar.add(jSeparator2);

        action.bind(removeButton, Actions.Remove);
        removeButton.setText(bundle.getString("FontWizard.removeButton.text")); // NOI18N
        removeButton.setFocusable(false);
        removeButton.setHorizontalTextPosition(SwingConstants.CENTER);
        removeButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        toolbar.add(removeButton);
        toolbar.add(jSeparator5);

        action.bind(insertBeforeButton, Actions.InsertBefore);
        insertBeforeButton.setText(bundle.getString("FontWizard.insertBeforeButton.text")); // NOI18N
        insertBeforeButton.setFocusable(false);
        insertBeforeButton.setHorizontalTextPosition(SwingConstants.CENTER);
        insertBeforeButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        toolbar.add(insertBeforeButton);
        toolbar.add(jSeparator4);

        action.bind(insertAfterButton, Actions.InsertAfter);
        insertAfterButton.setText(bundle.getString("FontWizard.insertAfterButton.text")); // NOI18N
        insertAfterButton.setFocusable(false);
        insertAfterButton.setHorizontalTextPosition(SwingConstants.CENTER);
        insertAfterButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        toolbar.add(insertAfterButton);
        toolbar.add(jSeparator3);

        action.bind(exportCurrentButton, Actions.ExportCurrent);
        exportCurrentButton.setText(bundle.getString("FontWizard.exportCurrentButton.text")); // NOI18N
        exportCurrentButton.setFocusable(false);
        exportCurrentButton.setHorizontalTextPosition(SwingConstants.CENTER);
        exportCurrentButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        toolbar.add(exportCurrentButton);
        toolbar.add(jSeparator6);

        action.bind(exportRootButton, Actions.ExportRoot);
        exportRootButton.setText(bundle.getString("FontWizard.exportRootButton.text")); // NOI18N
        exportRootButton.setFocusable(false);
        exportRootButton.setHorizontalTextPosition(SwingConstants.CENTER);
        exportRootButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        toolbar.add(exportRootButton);
        toolbar.add(jSeparator7);

        treeScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(bundle.getString("FontWizard.treeScroll.title")), treeScroll.getBorder())); // NOI18N

        fontTree.setComponentPopupMenu(treeMenu);
        fontTree.getSelectionModel().addTreeSelectionListener(treeListener);
        treeScroll.setViewportView(fontTree);

        wizardScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(bundle.getString("FontWizard.wizardScroll.title")), wizardScroll.getBorder())); // NOI18N
        wizardScroll.setViewportView(wizardPanel);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(toolbar, GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(treeScroll, GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                .addGap(23, 23, 23)
                .addComponent(wizardScroll, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(383, Short.MAX_VALUE)
                .addComponent(cancelBackPanel, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(applyNextPanel, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {applyNextPanel, cancelBackPanel});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbar, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(treeScroll, GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                    .addComponent(wizardScroll, GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(cancelBackPanel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                    .addComponent(applyNextPanel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {applyNextPanel, cancelBackPanel});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing (WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        if (rootExported
                || wizardPanel.panel() instanceof WizardInstructionPage
                || doClose()) {
            dispose();
        }
    }//GEN-LAST:event_formWindowClosing

    /**
     * Display a {@link FontWizard}.
     * @param parent the parent {@link Window} to use.
     * @param shell the context {@link Shell} to use. This {@link Shell} must not
     * have {@link Shell#start(java.lang.String) started} yet nor can it 45
     */
    public static void display (final Window parent, final Shell shell) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run () {
                FontWizard dialog = new FontWizard(new FontWizardContext(shell));
                dialog.setLocationRelativeTo(parent);
                dialog.start();
                dialog.setVisible(true);
            }
        });
    }

    private static enum Actions implements Command<FontWizard, FontTreeNode> {

        Add {

            @Override
            public void actionPerformed (FontWizard context) {
                super.addAction(Insertion.Add, context);
            }

            @Override
            public boolean enabled (FontTreeNode status, FontWizard context) {
                return status != null && (status.mixer() || !status.finished());
            }
        },
        InsertBefore {

            @Override
            public void actionPerformed (FontWizard context) {
                super.addAction(Insertion.InsertBefore, context);
            }

            @Override
            public boolean enabled (FontTreeNode status, FontWizard context) {
                if (status == null) {
                    return false;
                }
                else {
                    status = status.getParent();
                    return status != null && status.mixer();
                }
            }
        },
        InsertAfter {

            @Override
            public void actionPerformed (FontWizard context) {
                super.addAction(Insertion.InsertAfter, context);
            }

            @Override
            public boolean enabled (FontTreeNode status, FontWizard context) {
                if (status == null) {
                    return false;
                }
                else {
                    status = status.getParent();
                    return status != null && status.mixer();
                }
            }
        },
        ExportCurrent {

            @Override
            public void actionPerformed (FontWizard context) {
                FontTreeNode node = context.fontTree.getSelectedNode();
                ExportDialog.showDialog(context, true, node, context.context);
                if (!context.rootExported) {
                    context.rootExported =
                            context.fontTree.getModel().getRoot().equals(node);
                }
            }

            @Override
            public boolean enabled (FontTreeNode status, FontWizard context) {
                return status != null && status.finished();
            }
        },
        ExportRoot {

            @Override
            public void actionPerformed (FontWizard context) {
                FontTreeNode node = context.fontTree.getModel().getRoot();
                ExportDialog.showDialog(context, true, node, context.context);
                context.rootExported = true;
            }

            @Override
            public boolean enabled (FontTreeNode status, FontWizard context) {
                status = context.fontTree.getModel().getRoot();
                return status != null && status.finished();
            }
        },
        Remove {

            @Override
            public void actionPerformed (FontWizard context) {
                FontTreeNode n = context.action.status(), p = n.getParent();
                context.fontTree.delete(n);
                context.rootExported = false;
                if (p == null) {
                    context.start();
                }
                else {
                    context.continueWizard(context.findUnfinishedNode(p), p);
                }
            }

            @Override
            public boolean enabled (FontTreeNode status, FontWizard context) {
                return status != null;
            }
        },
        Next {

            @Override
            public void actionPerformed (FontWizard context) {
                context.page().nextAction(context);
            }

            @Override
            public boolean enabled (FontTreeNode status, FontWizard context) {
                return true;
            }
        },
        Cancel {

            @Override
            public void actionPerformed (FontWizard context) {
                context.page().cancelAction(context);
            }

            @Override
            public boolean enabled (FontTreeNode status, FontWizard context) {
                return context.fontTree.getModel().getRoot() != null
                        || !(context.wizardPanel.panel() instanceof CreateFontPage);
            }
        };

        @Override
        public String getText () {
            return ResourceHelp.getValue(this, FontWizard.class);
        }

        @Override
        public String format (Object... args) {
            return ResourceHelp.formatValue(this, FontWizard.class, args);
        }

        @Override
        public Class type () {
            return FontWizard.class;
        }

        private void addAction (Insertion type, FontWizard context) {
            FontTreeNode n = context.action.status();
            if (type != Insertion.Add) {
                n = n.getParent();
            }
            context.fontTree.setInsertionType(type);
            context.setWizardPage(n.continueAt());
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonPanel applyNextPanel;
    private ButtonPanel cancelBackPanel;
    private FontTree fontTree;
    private ReplacementPanel wizardPanel;
    private JScrollPane wizardScroll;
    // End of variables declaration//GEN-END:variables
}
