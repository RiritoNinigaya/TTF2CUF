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
 * ExportDialog.java
 *
 * Created on Oct 4, 2010, 1:10:17 AM
 */
package org.europabarbarorum.cuf.gui.fontwizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.CUFWriter;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizard.FontWizardContext;
import org.europabarbarorum.cuf.gui.support.FileInput;
import org.europabarbarorum.cuf.gui.support.FileInput.Access;
import org.europabarbarorum.cuf.gui.support.FileType;
import org.europabarbarorum.cuf.gui.support.ProgressMonitorImpl;
import org.europabarbarorum.cuf.gui.support.SettingField;
import org.europabarbarorum.cuf.gui.support.UIHelp;
import org.europabarbarorum.cuf.gui.support.UIHelp.Command;
import org.europabarbarorum.cuf.gui.support.UIHelp.Dialog;
import org.europabarbarorum.cuf.macro.Converter;
import org.europabarbarorum.cuf.shell.CompileUnit;
import org.europabarbarorum.cuf.shell.FontToolkit;
import org.europabarbarorum.cuf.shell.ReservedExitCode;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.shell.Shell.CallBack;
import org.europabarbarorum.cuf.support.CompileJob;
import org.europabarbarorum.cuf.support.IllegalMethodCallException;
import org.europabarbarorum.cuf.support.OptionMap;
import org.europabarbarorum.cuf.support.ProgressMonitor.Progress;
import org.europabarbarorum.cuf.support.ResourceHelp;

/**
 * A {@link Dialog} to export a {@link FontTreeNode} to a preview or CUF file.
 * @author Johan Ouwerkerk
 */
public class ExportDialog extends Dialog {

    private final FileInput cufFile, macroFile;
    private final FontTreeNode node;
    private final FontWizardContext context;
    private final UIHelp<ExportDialog, Boolean> actions;

    private String title () {
        return Messages.ExportDialogTitle.format(node.getName());
    }

    /**
     * Show a {@link ExportDialog}.
     * @param parent the parent {@link Window}.
     * @param modal whether or not the dialog will be modal.
     * @param node the {@link FontTreeNode} to export.
     * @param ctx the {@link FontWizardContext} to use.
     */
    public static void showDialog (final Window parent, final boolean modal,
                                   final FontTreeNode node,
                                   final FontWizardContext ctx) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run () {
                ExportDialog d = new ExportDialog(parent, modal, node, ctx);
                d.setVisible(true);
            }
        });
    }

    /**
     * Creates a new {@link ExportDialog}.
     * @param parent the parent {@link Window}.
     * @param modal whether or not the dialog is modal.
     * @param node the {@link FontTreeNode} to export.
     * @param ctx the {@link FontWizardContext} to use.
     */
    public ExportDialog (Window parent, boolean modal, FontTreeNode node,
                         FontWizardContext ctx) {
        super(parent, modal);

        this.node = node;
        this.context = ctx;
        this.actions = new UIHelp<ExportDialog, Boolean>(this) {

            @Override
            public Command<ExportDialog, Boolean> find (String name) {
                return Actions.valueOf(name);
            }
        };
        cufFile = new FileInput(FileType.CUF.derive(false, true),
                                Access.Write,
                                context.pathParser(),
                                this);
        macroFile = new FileInput(FileType.Macros.derive(false, true),
                                  Access.Write,
                                  context.pathParser(),
                                  this);
        initComponents();
        cufFile.getTextField().setValue(
                node.getName() + "." + FileType.CUF.extension());
        macroFile.getTextField().setValue(
                node.getName() + "." + FileType.Macros.extension());
        actions.update(false);
        create();
    }

    private void create () {
        execJob(new FontBuilder(),
                createLabel,
                createProgess,
                Messages.FontBuildFailed,
                true);
    }

    private File file (FileInput file) {
        return context.pathParser().parsePath(file.getValue());
    }

    private OptionMap buildOpts () {
        OptionMap opts = new OptionMap();
        this.node.setBuildOptions(opts);
        return opts;
    }

    @SuppressWarnings("unchecked")
    private void compileFont () {
        execJob(new CUFWriter(cached, file(cufFile), buildOpts()),
                exportLabel,
                exportProgress,
                Messages.FontCompileFailed,
                false);
    }

    private void compileMacros () {
        execJob(new Converter(cached, file(macroFile)),
                exportLabel,
                exportProgress,
                Messages.MacroCompileFailed,
                false);
    }

    private void error (String message, String title) {
        JOptionPane.showMessageDialog(this,
                                      message,
                                      title,
                                      JOptionPane.ERROR_MESSAGE);

    }
    private ExecJob currentJob;

    private interface ExecJob {

        boolean isDone ();

        void cancel ();

        String jobTitle ();
    }

    private void execJob (final CompileJob job, JLabel label, JProgressBar bar,
                          final Messages m, final boolean ping) {
        CompileUnit u = new CompileUnit(job);
        u.setMonitor(new ProgressMonitorImpl(label, bar));
        join(job);
        CallBack<Integer> call = ping ? ping(job, m) : null;
        context.run(u, call);
    }
    private CUFSource cached;

    private SaveJob saveJob () {
        if (!(currentJob instanceof SaveJob)) {
            currentJob = new SaveJob();
        }
        return (SaveJob) currentJob;
    }

    private void join (CompileJob job) {
        if (job instanceof FontBuilder) {
            currentJob = (FontBuilder) job;
        }
        else {
            saveJob().push(job);
        }
    }

    private CallBack<Integer> ping (final CompileJob job, final Messages m) {
        return new CallBack<Integer>() {

            @Override
            public void callback (Integer signal) {
                if (signal == ReservedExitCode.EXIT_OK_CODE.exitCode()) {
                    try {
                        cached = ((FontBuilder) job).get();
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run () {
                                actions.update(true);
                            }
                        });
                    }
                    catch (Exception e) {
                        System.err.println("[Bug] " + e);
                    }
                    return;
                }
                if (signal == ReservedExitCode.EXIT_COMPILE_FAIL.exitCode()) {
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            error(job.getException().getLocalizedMessage(),
                                  m.getText());
                        }
                    });
                }
            }
        };
    }

    private static enum Actions implements Command<ExportDialog, Boolean> {

        Compile {

            private boolean check (FileInput f) {
                SettingField s = f.getTextField();
                if (s.isEnabled() && f.check()) {
                    s.setEnabled(false);
                    f.getButton().setEnabled(false);
                    return true;
                }
                else {
                    return false;
                }
            }

            @Override
            public void actionPerformed (ExportDialog context) {
                if (check(context.cufFile)) {
                    context.compileFont();
                }
                if (check(context.macroFile)) {
                    context.compileMacros();
                }
            }
        },
        Preview {

            @Override
            public void actionPerformed (ExportDialog context) {
                FontToolkit.preview(context.cached);
                context.dispose();
            }
        },
        Cancel {

            @Override
            public void actionPerformed (ExportDialog context) {
                context.close();
            }

            @Override
            public boolean enabled (Boolean status, ExportDialog d) {
                return true;
            }
        };

        @Override
        public boolean enabled (Boolean status, ExportDialog d) {
            return status;
        }

        @Override
        public String getText () {
            return ResourceHelp.getValue(this, ExportDialog.class);
        }

        @Override
        public Class type () {
            return Actions.class;
        }

        @Override
        public String format (Object... args) {
            return ResourceHelp.formatValue(this, ExportDialog.class, args);
        }
    }

    class SaveJob implements ExecJob {

        private CompileJob j1, j2;

        private void push (CompileJob j) {
            if (j1 == null) {
                j1 = j;
                return;
            }
            if (j2 == null) {
                j2 = j;
                return;
            }
            throw new IllegalMethodCallException();
        }

        @Override
        public boolean isDone () {
            return j1 != null && j1.isDone() && j1 != null && j1.isDone();
        }

        @Override
        public void cancel () {
            if (j1 != null) {
                j1.cancel(true);
            }
            if (j2 != null) {
                j2.cancel(true);
            }
        }

        @Override
        public String jobTitle () {
            if (j1 == null || j1.isDone()) {
                return j2 == null || j2.isDone() ? null : j2.jobTitle();
            }
            else {
                return j2 == null || j2.isDone()
                        ? j1.jobTitle() : Messages.ExportSaveTitle.getText();
            }
        }
    }

    class FontBuilder extends CompileJob<CUFSource> implements ExecJob {

        private FontBuilder () {
            super(Messages.FontBuildTitle.getText());
        }

        @Override
        protected CUFSource build () throws Exception {
            return node.create();
        }

        @Override
        protected Progress abortMessage (Exception e) {
            return toProgress(0, 100, Messages.FontBuildFailed);
        }

        @Override
        protected Progress doneMessage () {
            return toProgress(100, 100, Messages.FontBuildFinished);
        }

        @Override
        public void cancel () {
            cancel(true);
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
        JLabel instruction = new JLabel();
        createLabel = new JLabel();
        createProgess = new JProgressBar();
        exportLabel = new JLabel();
        exportProgress = new JProgressBar();
        JButton exportButton = new JButton();
        JButton cancelButton = new JButton();
        JButton preview = new JButton();
        JLabel cufLabel = new JLabel();
        JButton cufButton = cufFile.getButton();
        SettingField cufField = cufFile.getTextField();
        JLabel macroLabel = new JLabel();
        SettingField macroField = macroFile.getTextField();
        JButton macroButton = macroFile.getButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(title());
        setName("Form"); // NOI18N
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        instruction.setHorizontalAlignment(SwingConstants.CENTER);
        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/fontwizard/ExportDialog"); // NOI18N
        instruction.setText(bundle.getString("ExportDialog.instruction.text")); // NOI18N
        instruction.setName("instruction"); // NOI18N

        createLabel.setText(bundle.getString("ExportDialog.createLabel.text")); // NOI18N
        createLabel.setEnabled(false);
        createLabel.setName("createLabel"); // NOI18N

        createProgess.setEnabled(false);
        createProgess.setName("createProgess"); // NOI18N

        exportLabel.setText(bundle.getString("ExportDialog.exportLabel.text")); // NOI18N
        exportLabel.setEnabled(false);
        exportLabel.setName("exportLabel"); // NOI18N

        exportProgress.setEnabled(false);
        exportProgress.setName("exportProgress"); // NOI18N

        actions.bind(exportButton, Actions.Compile);
        exportButton.setText(bundle.getString("ExportDialog.exportButton.text")); // NOI18N
        exportButton.setName("exportButton"); // NOI18N

        actions.bind(cancelButton,Actions.Cancel);
        cancelButton.setText(bundle.getString("ExportDialog.cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        actions.bind(preview,Actions.Preview);
        preview.setText(bundle.getString("ExportDialog.preview.text")); // NOI18N
        preview.setName("preview"); // NOI18N

        cufLabel.setText(bundle.getString("ExportDialog.cufLabel.text")); // NOI18N
        cufLabel.setName("cufLabel"); // NOI18N

        cufButton.setText(bundle.getString("ExportDialog.cufButton.text")); // NOI18N
        cufButton.setName("cufButton"); // NOI18N

        cufField.setText(bundle.getString("ExportDialog.cufField.text")); // NOI18N
        cufField.setName("cufField"); // NOI18N

        macroLabel.setText(bundle.getString("ExportDialog.macroLabel.text")); // NOI18N
        macroLabel.setName("macroLabel"); // NOI18N

        macroField.setText(bundle.getString("ExportDialog.macroField.text")); // NOI18N
        macroField.setName("macroField"); // NOI18N

        macroButton.setText(bundle.getString("ExportDialog.macroButton.text")); // NOI18N
        macroButton.setName("macroButton"); // NOI18N

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(instruction, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(preview)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(exportButton, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(exportLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                            .addComponent(createLabel, GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(createProgess, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(exportProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(macroLabel)
                            .addComponent(cufLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(macroField, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                            .addComponent(cufField, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE))
                        .addGap(13, 13, 13)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(cufButton)
                            .addComponent(macroButton))))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, exportButton, preview});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cufButton, macroButton});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cufLabel, macroLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(instruction)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cufLabel)
                    .addComponent(cufButton)
                    .addComponent(cufField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(macroLabel)
                    .addComponent(macroField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(macroButton))
                .addPreferredGap(ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(createProgess, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(createLabel))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(exportLabel)
                    .addComponent(exportProgress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(exportButton)
                    .addComponent(preview))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cancelButton, exportButton, preview});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cufButton, macroButton});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cufField, macroField});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cufLabel, macroLabel});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    protected void close () {
        super.close();
    }

    private int cancel () {
        return JOptionPane.showOptionDialog(
                this,
                Messages.ExportDialogClose.format(currentJob.jobTitle()),
                Messages.ExportDialogCloseTitle.getText(),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, null, null);
    }

    private void formWindowClosing (WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        if (!currentJob.isDone()) {
            int c = cancel();
            switch (c) {
                case JOptionPane.YES_OPTION:
                    currentJob.cancel();
                case JOptionPane.NO_OPTION:
                    dispose();
                default:
                    break;
            }
        }
        else {
            dispose();
        }

    }//GEN-LAST:event_formWindowClosing
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel createLabel;
    private JProgressBar createProgess;
    private JLabel exportLabel;
    private JProgressBar exportProgress;
    // End of variables declaration//GEN-END:variables
}
