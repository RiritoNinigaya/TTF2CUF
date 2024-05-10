/*
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
 */

/*
 * ScriptDialog.java
 *
 * Created on Jul 25, 2010, 5:01:32 PM
 */
package org.europabarbarorum.cuf.gui;

import org.europabarbarorum.cuf.gui.support.SettingField;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import org.europabarbarorum.cuf.gui.support.UIHelp.Dialog;
import org.europabarbarorum.cuf.gui.support.FileInput;
import org.europabarbarorum.cuf.shell.ScriptRunner;
import org.europabarbarorum.cuf.shell.Shell.CallBack;
import org.europabarbarorum.cuf.shell.Shell;

/**
 *
 * @author Johan Ouwerkerk
 */
public class ScriptDialog extends Dialog {

    private final Shell context;
    private final FileInput file;
    private final boolean fork;
    private final String lang;

    /**
     * Creates a new {@link ScriptDialog}.
     * @param parent the parent {@link Frame}.
     * @param modal whether or not the dialog is modal.
     * @param shell the {@link Shell} which provides context for parsing file names and
     * constructing the preview.
     * @param fork controls whether the dialog uses
     * {@link Shell#fork(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.PrintStream, java.io.PrintStream, org.europabarbarorum.cuf.shell.Shell.CallBack, boolean) fork} (true),
     * or {@link Shell#interpret(java.lang.String, java.lang.String) interpret} instead (false).
     */
    public ScriptDialog (Frame parent, boolean modal, Shell shell, boolean fork) {
        super(parent, modal);
        this.file = new FileInput(FileInput.createDescription(shell),
                                  FileInput.Access.Read,
                                  shell,
                                  this);
        this.lang = shell.loadedLanguage();
        this.fork = fork;
        this.context = shell;
        initComponents();
        this.file.focusButton(true);
    }

    private void runScript () {
        final String src = file.getValue();
        if (fork) {
            context.fork(src,
                         lang(),
                         null,
                         Shell.ShellName.get(),
                         null,
                         null,
                         new CallBack<Integer>() {

                @Override
                public void callback (Integer result) {
                    ScriptRunner.callBackImpl(context, src, result);
                }
            }, true);
        }
        else {
            context.interpret(src, lang());
        }
    }

    private String lang () {
        langField.drop();
        String val = langField.getValue();
        if (val == null || val.equals(this.lang)) {
            return null;
        }
        return val;
    }

    @Override
    protected void close () {
        this.file.dispose();
        super.close();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        SettingField fileField = file.getTextField();
        langField = new SettingField();
        JLabel fileLabel = new JLabel();
        JLabel langLabel = new JLabel();
        JButton cancelButton = new JButton();
        JButton runButton = new JButton();
        fileButton = file.getButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/ScriptDialog"); // NOI18N
        setTitle(bundle.getString("ScriptDialog.title")); // NOI18N
        setName("Form"); // NOI18N

        fileField.setText(bundle.getString("ScriptDialog.fileField.text")); // NOI18N
        fileField.setToolTipText(bundle.getString("ScriptDialog.fileField.toolTipText")); // NOI18N
        fileField.setName("fileField"); // NOI18N

        langField.setText(bundle.getString("ScriptDialog.langField.text")); // NOI18N
        langField.setToolTipText(bundle.getString("ScriptDialog.langField.toolTipText")); // NOI18N
        langField.setName("langField"); // NOI18N

        fileLabel.setDisplayedMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/ScriptDialog").getString("ScriptDialog.fileLabel.mnemonic").charAt(0));
        fileLabel.setLabelFor(fileField);
        fileLabel.setText(bundle.getString("ScriptDialog.fileLabel.text")); // NOI18N
        fileLabel.setName("fileLabel"); // NOI18N

        langLabel.setDisplayedMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/ScriptDialog").getString("ScriptDialog.langLabel.mnemonic").charAt(0));
        langLabel.setLabelFor(langField);
        langLabel.setText(bundle.getString("ScriptDialog.langLabel.text")); // NOI18N
        langLabel.setName("langLabel"); // NOI18N

        cancelButton.addActionListener(closeListener);
        cancelButton.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/ScriptDialog").getString("ScriptDialog.cancelButton.mnemonic").charAt(0));
        cancelButton.setText(bundle.getString("ScriptDialog.cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        runButton.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/ScriptDialog").getString("ScriptDialog.runButton.mnemonic").charAt(0));
        runButton.setText(bundle.getString("ScriptDialog.runButton.text")); // NOI18N
        runButton.setName("runButton"); // NOI18N
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                run(evt);
            }
        });

        fileButton.setMnemonic(ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/ScriptDialog").getString("ScriptDialog.fileButton.mnemonic").charAt(0));
        fileButton.setText(bundle.getString("ScriptDialog.fileButton.text")); // NOI18N
        fileButton.setName("fileButton"); // NOI18N

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fileButton)
                        .addPreferredGap(ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                        .addComponent(runButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(langLabel)
                            .addComponent(fileLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(fileField, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                            .addComponent(langField, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, fileButton, runButton});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {fileLabel, langLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(fileField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(langField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(langLabel))
                .addPreferredGap(ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(runButton)
                    .addComponent(fileButton))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {fileField, langField});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cancelButton, fileButton, runButton});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {fileLabel, langLabel});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void run (ActionEvent evt) {//GEN-FIRST:event_run
        if (file.check()) {
            close();
            runScript();
        }
    }//GEN-LAST:event_run

    /**
     * Displays a {@link ScriptDialog}.
     * @param owner the parent {@link Frame}.
     * @param fork controls whether the dialog uses
     * {@link Shell#fork(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.PrintStream, java.io.PrintStream, org.europabarbarorum.cuf.shell.Shell.CallBack, boolean) fork} (true),
     * or {@link Shell#interpret(java.lang.String, java.lang.String) interpret} instead (false).
     * @param host the {@link Shell} which provides context for parsing file names and
     * constructing the preview.
     */
    public static void display (final Frame owner,
                                final boolean fork,
                                final Shell host) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run () {
                ScriptDialog dialog =
                        new ScriptDialog(owner, true, host, fork);
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton fileButton;
    private SettingField langField;
    // End of variables declaration//GEN-END:variables
}