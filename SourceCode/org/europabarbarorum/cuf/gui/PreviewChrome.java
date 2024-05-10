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
 * PreviewChrome.java
 *
 * Created on Feb 16, 2010, 3:38:38 PM
 */
package org.europabarbarorum.cuf.gui;

import org.europabarbarorum.cuf.gui.support.ScrollingSupport;
import org.europabarbarorum.cuf.gui.support.PreviewInput;
import org.europabarbarorum.cuf.support.ProgressMonitor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JToolBar.Separator;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.europabarbarorum.cuf.font.CUFFont;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.gui.FontTableModel.TableSource;
import org.europabarbarorum.cuf.gui.support.UpdatableModel.UpdatableFont;
import org.europabarbarorum.cuf.gui.support.UpdatableModel.UpdatablePreview;
import org.europabarbarorum.cuf.support.Preview;

/**
 * This class provides a GUI to preview a font.
 * It includes GUI components to preview text as rendered by a {@link CUFFont},
 * inspect the values of various CUF properties, and a tree to inspect the hierarchical structure of the font.
 * @author Johan Ouwerkerk
 */
public class PreviewChrome extends JPanel implements
        UpdatableFont,
        UpdatablePreview {

    private final FontTableWidgets tableWidgets;

    /**
     * Creates a new preview control.
     */
    public PreviewChrome () {
        tableWidgets = new FontTableWidgets();
        initComponents();
    }

    /**
     * Install a {@link ProgressMonitor} on this {@link PreviewChrome} panel.
     * This method installs the monitor on its internal {@link CUFRenderPanel} so that 
     * text which takes a time to render is duly monitored.
     * @param monitor the monitor to install.
     * @see CUFRenderPanel#setMonitor(org.europabarbarorum.cuf.support.ProgressMonitor)
     */
    public void setMonitor (ProgressMonitor monitor) {
        cufRender.setMonitor(monitor);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        JSplitPane jSplitPane1 = new JSplitPane();
        JPanel jPanel2 = new JPanel();
        JSeparator jSeparator2 = new JSeparator();
        JScrollPane jScrollPane1 = new ScrollingSupport();
        cufTree = new JTree();
        JScrollPane jScrollPane2 = new ScrollingSupport();
        JTable tableUI = tableWidgets.table();
        JPanel jPanel1 = new JPanel();
        JScrollPane jScrollPane3 = new JScrollPane();
        cufRender = new CUFRenderPanel();
        JToolBar toolbar = new JToolBar();
        Separator jSeparator6 = new Separator();
        JLabel preview = new JLabel();
        Separator jSeparator4 = new Separator();
        input = new PreviewInput();
        Separator jSeparator5 = new Separator();
        JButton render = new JButton();
        Separator jSeparator7 = new Separator();
        JLabel radix = new JLabel();
        Separator jSeparator3 = new Separator();
        JRadioButton binary = tableWidgets.binary();
        JRadioButton decimal = tableWidgets.decimal();
        JRadioButton hexadecimal = tableWidgets.hexadecimal();
        Separator jSeparator8 = new Separator();

        setName("Form"); // NOI18N

        jSplitPane1.setResizeWeight(0.7);
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new Dimension(160, 480));

        jSeparator2.setName("jSeparator2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        cufTree.setModel(browse);
        // connect listener to receive selection updates
        cufTree.addTreeSelectionListener(treeListener);
        cufTree.setName("cufTree"); // NOI18N
        jScrollPane1.setViewportView(cufTree);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        tableUI.setName("tableUI"); // NOI18N
        jScrollPane2.setViewportView(tableUI);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jScrollPane2, Alignment.CENTER, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                    .addComponent(jSeparator2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N
        jScrollPane3.setPreferredSize(new Dimension(480, 480));

        cufRender.setName("cufRender"); // NOI18N

        GroupLayout cufRenderLayout = new GroupLayout(cufRender);
        cufRender.setLayout(cufRenderLayout);
        cufRenderLayout.setHorizontalGroup(
            cufRenderLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 464, Short.MAX_VALUE)
        );
        cufRenderLayout.setVerticalGroup(
            cufRenderLayout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 414, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(cufRender);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
        );

        jSplitPane1.setLeftComponent(jPanel1);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setName("toolbar"); // NOI18N

        jSeparator6.setName("jSeparator6"); // NOI18N
        toolbar.add(jSeparator6);

        preview.setDisplayedMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewChrome").getString("PreviewChrome.preview.mnemonic").charAt(0));
        preview.setLabelFor(input);
        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewChrome"); // NOI18N
        preview.setText(bundle.getString("PreviewChrome.preview.text")); // NOI18N
        preview.setToolTipText(bundle.getString("PreviewChrome.preview.toolTipText")); // NOI18N
        preview.setName("preview"); // NOI18N
        toolbar.add(preview);

        jSeparator4.setName("jSeparator4"); // NOI18N
        toolbar.add(jSeparator4);

        input.setName("input"); // NOI18N
        toolbar.add(input);

        jSeparator5.setName("jSeparator5"); // NOI18N
        toolbar.add(jSeparator5);

        render.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewChrome").getString("PreviewChrome.render.mnemonic").charAt(0));
        render.setText(bundle.getString("PreviewChrome.render.text")); // NOI18N
        render.setToolTipText(bundle.getString("PreviewChrome.render.toolTipText")); // NOI18N
        render.setHorizontalTextPosition(SwingConstants.CENTER);
        render.setName("render"); // NOI18N
        render.setVerticalTextPosition(SwingConstants.BOTTOM);
        // connect listener to receive action events
        render.addActionListener(updateListener);
        toolbar.add(render);

        jSeparator7.setName("jSeparator7"); // NOI18N
        toolbar.add(jSeparator7);

        radix.setText(bundle.getString("PreviewChrome.radix.text")); // NOI18N
        radix.setToolTipText(bundle.getString("PreviewChrome.radix.toolTipText")); // NOI18N
        radix.setName("radix"); // NOI18N
        toolbar.add(radix);

        jSeparator3.setName("jSeparator3"); // NOI18N
        toolbar.add(jSeparator3);

        binary.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewChrome").getString("PreviewChrome.binary.mnemonic").charAt(0));
        binary.setText(bundle.getString("PreviewChrome.binary.text")); // NOI18N
        binary.setToolTipText(bundle.getString("PreviewChrome.binary.toolTipText")); // NOI18N
        binary.setName("binary"); // NOI18N
        toolbar.add(binary);

        decimal.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewChrome").getString("PreviewChrome.decimal.mnemonic").charAt(0));
        decimal.setText(bundle.getString("PreviewChrome.decimal.text")); // NOI18N
        decimal.setToolTipText(bundle.getString("PreviewChrome.decimal.toolTipText")); // NOI18N
        decimal.setName("decimal"); // NOI18N
        toolbar.add(decimal);

        hexadecimal.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewChrome").getString("PreviewChrome.hexadecimal.mnemonic").charAt(0));
        hexadecimal.setText(bundle.getString("PreviewChrome.hexadecimal.text")); // NOI18N
        hexadecimal.setToolTipText(bundle.getString("PreviewChrome.hexadecimal.toolTipText")); // NOI18N
        hexadecimal.setName("hexadecimal"); // NOI18N
        toolbar.add(hexadecimal);

        jSeparator8.setName("jSeparator8"); // NOI18N
        toolbar.add(jSeparator8);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                    .addComponent(toolbar, GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbar, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private CUFRenderPanel cufRender;
    protected JTree cufTree;
    protected PreviewInput input;
    // End of variables declaration//GEN-END:variables
    private FontTreeModel browse = new FontTreeModel();
    private TreeSelectionListener treeListener = new TreeSelectionListener() {

        @Override
        public void valueChanged (TreeSelectionEvent e) {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                CUFTreeNode ctn = (CUFTreeNode) path.getLastPathComponent();
                input.setText(ctn.getPreview());
                updateNode(ctn);
            }
        }
    };
    private CUFTreeNode currentNode;
    private ActionListener updateListener = new ActionListener() {

        @Override
        public void actionPerformed (ActionEvent e) {
            currentNode.setPreview(input.getText());
            input.selectPreview(currentNode.getPreview());

            cufRender.setRenderText(input.getPreview().getPreview());
            cufRender.update();
        }
    };

    private void updateNode (CUFTreeNode source) {
        currentNode = source;
        updateRender();
    }

    private void updateRender () {
        input.setText(currentNode.getPreview());

        TableSource source = new TableSource(currentNode.getSource());
        tableWidgets.model().updateModel(source);

        cufRender.updateModel(source);
        cufRender.setRenderText(input.getPreview().getPreview());
        cufRender.update();
    }

    @Override
    public void updateModel (CUFSource font) {
        FontTreeModel.setPreviewDefault(input.getText());
        browse.updateModel(font);
        updateNode((CUFTreeNode) browse.getRoot());
    }

    @Override
    public void updateModel (Preview preview) {
        this.input.updateModel(preview);
    }
}
