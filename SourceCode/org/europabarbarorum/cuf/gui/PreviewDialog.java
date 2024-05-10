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
 * PreviewDialog.java
 *
 * Created on Jul 12, 2010, 6:46:22 PM
 */
package org.europabarbarorum.cuf.gui;

import org.europabarbarorum.cuf.gui.support.SettingField;
import org.europabarbarorum.cuf.gui.support.FileInput;
import org.europabarbarorum.cuf.gui.support.FileType;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.gui.support.UIHelp.Dialog;
import org.europabarbarorum.cuf.shell.FontToolkit;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.shell.StringsToolkit;
import org.europabarbarorum.cuf.strings.StringsReader;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 * A {@link Dialog} to prompt the user for CUF (and optionally Strings) files to preview.
 * @author Johan Ouwerkerk
 */
public class PreviewDialog extends Dialog {

    /**
     * Creates a new {@link PreviewDialog}.
     * @param parent the parent window of this dialog.
     * @param modal whether or not this dialog should be modal.
     * @param useStrings whether or not the dialog uses the controls to select a Strings file.
     * @param shell the {@link Shell} which provides context for parsing file names and
     * constructing the preview.
     */
    public PreviewDialog (Frame parent, boolean modal,
                          boolean useStrings, Shell shell) {
        super(parent, modal);
        this.useStrings = useStrings;
        this.shell = shell;
        this.cufInput = new FileInput(FileType.CUF.derive(false, true),
                                      FileInput.Access.Read, shell, this);
        this.binInput = new FileInput(FileType.Strings.derive(false, true),
                                      FileInput.Access.Read, shell, this);

        initComponents();
        if (useStrings) {
            this.binInput.focusButton(true);
        }
        else {
            this.cufInput.focusButton(true);
        }
    }

    @Override
    protected void close () {
        this.binInput.dispose();
        this.cufInput.dispose();
        super.close();
    }
    private FileInput cufInput;
    private Shell shell;
    private FileInput binInput;
    private boolean useStrings;

    private SettingField field (boolean cuf) {
        SettingField field = cuf ? cufInput.getTextField() : binInput.
                getTextField();
        field.setVisible(cuf || useStrings);
        return field;
    }

    private JButton button (boolean cuf) {
        JButton field = cuf ? cufInput.getButton() : binInput.getButton();
        field.setVisible(cuf || useStrings);
        return field;
    }

    private boolean canPreview () {
        boolean c = cufInput.check();
        if (useStrings) {
            return binInput.check() && c;
        }
        return c;
    }

    private void triggerPreview () {
        SwingWorker sw = new SwingWorker<Void, Void>() {

            private StringsReader preview = null;
            private CUFSource font = null;

            @Override
            protected Void doInBackground () throws Exception {
                font = new FontToolkit(shell).fromFile(cufInput.getValue());
                if (useStrings) {
                    preview = new StringsToolkit(shell).fromFile(
                            binInput.getValue());
                }
                return null;
            }

            @Override
            protected void done () {
                try {
                    get();
                    if (useStrings) {
                        StringsToolkit.preview(preview, font,
                                               binInput.getValue());
                    }
                    else {
                        FontToolkit.preview(font);
                    }
                }
                catch (Exception e) {
                    e.fillInStackTrace();
                    displayError(e);
                }
            }
        };
        sw.execute();
    }

    private void displayError (Throwable e) {
        Throwable orig = e;
        Throwable cause;
        do {
            cause = e;
            e = e.getCause();
        }
        while (e != null);

        IOHelp.handleExceptions(PreviewDialog.class,
                                "displayError",
                                orig,
                                orig.getLocalizedMessage());

        String msg = useStrings
                ? Messages.StringsPreviewLoadError.format(binInput.getValue(),
                                                          cufInput.getValue(),
                                                          cause.getLocalizedMessage())
                : Messages.CUFPreviewLoadError.format(cufInput.getValue(),
                                                      cause.getLocalizedMessage());

        JOptionPane.showMessageDialog(this,
                                      msg,
                                      Messages.PreviewLoadErrorTitle.getText(),
                                      JOptionPane.ERROR_MESSAGE);
    }

    /** This method is called from within the constructor to
     * initialise the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        JLabel binLabel = new JLabel();
        SettingField binField = field(false);
        SettingField cufField = field(true);
        JButton binButton = button(false);
        JButton cufButton = button(true);
        JButton cancelButton = new JButton();
        JButton previewButton = new JButton();
        JLabel cufLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        binLabel.setDisplayedMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewDialog").getString("PreviewDialog.binLabel.mnemonic").charAt(0));
        binLabel.setLabelFor(binField);
        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewDialog"); // NOI18N
        binLabel.setText(bundle.getString("PreviewDialog.binLabel.text")); // NOI18N
        binLabel.setName("binLabel"); // NOI18N
        binLabel.setVisible(useStrings);

        binField.setText(bundle.getString("PreviewDialog.binField.text")); // NOI18N
        binField.setToolTipText(bundle.getString("PreviewDialog.binField.toolTipText")); // NOI18N
        binField.setName("binField"); // NOI18N

        cufField.setText(bundle.getString("PreviewDialog.cufField.text")); // NOI18N
        cufField.setToolTipText(bundle.getString("PreviewDialog.cufField.toolTipText")); // NOI18N
        cufField.setName("cufField"); // NOI18N

        binButton.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewDialog").getString("PreviewDialog.binButton.mnemonic").charAt(0));
        binButton.setText(bundle.getString("PreviewDialog.binButton.text")); // NOI18N
        binButton.setToolTipText(bundle.getString("PreviewDialog.binButton.toolTipText")); // NOI18N
        binButton.setName("binButton"); // NOI18N

        cufButton.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewDialog").getString("PreviewDialog.cufButton.mnemonic").charAt(0));
        cufButton.setText(bundle.getString("PreviewDialog.cufButton.text")); // NOI18N
        cufButton.setToolTipText(bundle.getString("PreviewDialog.cufButton.toolTipText")); // NOI18N
        cufButton.setName("cufButton"); // NOI18N

        cancelButton.addActionListener(closeListener);
        cancelButton.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewDialog").getString("PreviewDialog.cancelButton.mnemonic").charAt(0));
        cancelButton.setText(bundle.getString("PreviewDialog.cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        previewButton.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewDialog").getString("PreviewDialog.previewButton.mnemonic").charAt(0));
        previewButton.setText(bundle.getString("PreviewDialog.previewButton.text")); // NOI18N
        previewButton.setName("previewButton"); // NOI18N
        previewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                previewAction(evt);
            }
        });

        cufLabel.setDisplayedMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/PreviewDialog").getString("PreviewDialog.cufLabel.mnemonic").charAt(0));
        cufLabel.setLabelFor(cufField);
        cufLabel.setText(bundle.getString("PreviewDialog.cufLabel.text")); // NOI18N
        cufLabel.setToolTipText(bundle.getString("PreviewDialog.cufButton.toolTipText")); // NOI18N
        cufLabel.setName("cufLabel"); // NOI18N

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(cufLabel)
                            .addComponent(binLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(cufField, GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                            .addComponent(binField, GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(cufButton)
                            .addComponent(binButton)))
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(previewButton, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {binButton, cufButton});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, previewButton});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {binLabel, cufLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(binLabel)
                    .addComponent(binField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(binButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cufField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cufButton)
                    .addComponent(cufLabel))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(previewButton))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {binField, cufField});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cancelButton, previewButton});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {binLabel, cufLabel});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void previewAction (ActionEvent evt) {//GEN-FIRST:event_previewAction

        if (canPreview()) {
            triggerPreview();
            close();
        }
    }//GEN-LAST:event_previewAction

    /**
     * Displays a {@link PreviewDialog}.
     * @param owner the parent {@link JFrame}.
     * @param useStrings whether or not the dialog uses the controls to select a Strings file.
     * @param host the {@link Shell} which provides context for parsing file names and
     * constructing the preview.
     */
    public static void display (final JFrame owner, final boolean useStrings,
                                final Shell host) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run () {
                PreviewDialog dialog =
                        new PreviewDialog(owner, true, useStrings, host);
                dialog.setTitle(useStrings
                        ? Messages.PreviewStringsTitle.getText()
                        : Messages.PreviewCUFTitle.getText());
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
