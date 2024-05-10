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
 * DocumentDialog.java
 *
 * Created on Mar 17, 2010, 6:44:31 PM
 */
package org.europabarbarorum.cuf.gui;

import org.europabarbarorum.cuf.gui.support.UIHelp;
import java.awt.Cursor;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import org.europabarbarorum.cuf.gui.support.ScrollingSupport;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 * This is a {@link JFrame} designed to show a single document.
 * @author Johan Ouwerkerk
 */
public class DocumentDialog extends UIHelp.Frame {

    /** Create a DocumentDialog
     * @param parent the parent window or null if there isn't any.
     * @param modal whether or not the frame is blocked by modal dialogs.
     */
    public DocumentDialog (JFrame parent, boolean modal) {
        super(parent);
        this.setModalExclusionType(modal
                ? ModalExclusionType.APPLICATION_EXCLUDE
                : ModalExclusionType.NO_EXCLUDE);
        initComponents();

        close.requestFocusInWindow();
    }

    /*
     * Create a default DocumentDialog without parent window.
    /
    public DocumentDialog () {
    super(null);
    initComponents();
    close.requestFocusInWindow();
    }*/
    private static ExecutorService documentLoader = Executors.
            newSingleThreadExecutor();

    /**
     * Loads the document at the given (relative) URL into this {@link DocumentDialog} window.
     * @param newDoc the URL to load
     * @see JTextPane#setPage(java.net.URL)
     */
    public void setDocument (final String newDoc) {
        textPane.setText(newDoc);
        documentLoader.submit(new Runnable() {

            @Override
            public void run () {
                java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run () {
                        textPane.setToolTipText(newDoc);
                        textPane.setCursor(Cursor.getPredefinedCursor(
                                Cursor.WAIT_CURSOR));
                    }
                });
                try {
                    URL u = DocumentDialog.class.getClassLoader().getResource(
                            newDoc);
                    textPane.setPage(u == null ? new URL("file:" + newDoc) : u);
                }
                catch (Exception e) {
                    IOHelp.handleExceptions(DocumentDialog.class,
                                            "setDocument",
                                            e,
                                            Messages.DocumentError,
                                            newDoc);
                    java.awt.EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            textPane.setText(
                                    Messages.DocumentError.format(newDoc));
                        }
                    });
                }
                java.awt.EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run () {
                        textPane.setCursor(Cursor.getPredefinedCursor(
                                Cursor.DEFAULT_CURSOR));
                        textPane.setToolTipText("");
                    }
                });
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        JScrollPane jScrollPane1 = new ScrollingSupport();
        textPane = new JTextPane() {
            @Override
            protected void paintComponent (Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
                super.paintComponent(g2);
            }
        };
        close = new JButton();
        close.addActionListener(closeListener);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        textPane.setEditable(false);
        textPane.setFont(new Font("Dialog", 0, 14)); // NOI18N
        textPane.setName("textPane"); // NOI18N
        jScrollPane1.setViewportView(textPane);

        close.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/DocumentDialog").getString("DocumentDialog.close.mnemonic").charAt(0));
        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/DocumentDialog"); // NOI18N
        close.setText(bundle.getString("DocumentDialog.close.text")); // NOI18N
        close.setToolTipText(bundle.getString("DocumentDialog.close.toolTipText")); // NOI18N
        close.setHideActionText(true);
        close.setName("close"); // NOI18N

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addComponent(close))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(close)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Create and display a {@link DocumentDialog}.
     * @param document (relative) path to the file containing the document to display
     * @param owner the frame which “owns” this dialog.
     * @see #setDocument(java.lang.String)
     */
    public static void display (final String document, final JFrame owner) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run () {
                final DocumentDialog dialog = new DocumentDialog(owner, true);
                dialog.textPane.setContentType("text/plain");
                dialog.setDocument(document);
                dialog.setTitle(Messages.DocumentTitle.format(document));
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton close;
    private JTextPane textPane;
    // End of variables declaration//GEN-END:variables
}
