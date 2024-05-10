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
 * ScriptUI.java
 *
 * Created on Sep 19, 2010, 7:17:30 PM
 */
package org.europabarbarorum.cuf.gui.fontwizard.common;

import org.europabarbarorum.cuf.gui.support.ScrollingSupport;
import java.awt.Font;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.europabarbarorum.cuf.gui.fontwizard.MessageScripts;
import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;
import org.europabarbarorum.cuf.gui.fontwizard.common.ScriptUI.Model;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.gui.support.FileInput;
import org.europabarbarorum.cuf.gui.support.FileInput.AnyInputDescription;
import org.europabarbarorum.cuf.gui.support.FileInput.FileDescription;
import org.europabarbarorum.cuf.gui.support.SettingField;
import org.europabarbarorum.cuf.gui.support.TextSetting;
import org.europabarbarorum.cuf.gui.support.SettingArea;
import org.europabarbarorum.cuf.shell.Shell;

/**
 * A component to hold raw code of a user supplied script or the name of a
 * script file.
 * @author Johan Ouwerkerk
 */
public class ScriptUI extends JPanel implements ComponentState<Model> {

    private final FileInput file;
    private final String name;
    private final String lang;

    /**
     * Create a new {@link ScriptUI}.
     * @param context the context {@link Shell} to use for resolving relative file paths.
     * @param optional whether or not the user is required to fill out the component.
     */
    public ScriptUI (Shell context, boolean optional) {
        file = getInput(context, optional);
        if (context == null) {
            lang = (name = null);
        }
        else {
            lang = context.loadedLanguage();
            name = context.shellName();
        }
        initComponents();
    }

    private FileInput getInput (Shell context, boolean optional) {
        FileDescription fd = context == null
                ? new AnyInputDescription(false, optional)
                : FileInput.createDescription(context, optional);
        return new FileInput(fd, FileInput.Access.Read, context, this);
    }

    /**
     * Create a new {@link ScriptUI} for GUI builder purposes.
     * Makes this class a valid JavaBean.
     */
    public ScriptUI () {
        this(null, false);
    }

    /**
     * Set an example script/message in the {@link ScriptUI}.
     * @param script the {@link MessageScripts script} to use.
     */
    public void setScript (MessageScripts script) {
        customCode.reset(script.format(lang, name));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        JLabel scriptLabel = new JLabel();
        JScrollPane scriptScroll = new ScrollingSupport();
        customCode = new SettingArea();
        JButton fileButton = file.getButton();
        SettingField fileField = file.getTextField();

        setName("Form"); // NOI18N

        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/fontwizard/common/CommonUI"); // NOI18N
        scriptLabel.setText(bundle.getString("ScriptUI.scriptLabel.text")); // NOI18N
        scriptLabel.setName("scriptLabel"); // NOI18N

        scriptScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(bundle.getString("ScriptUI.scriptScroll.title")), scriptScroll.getBorder())); // NOI18N
        scriptScroll.setName("scriptScroll"); // NOI18N

        customCode.setColumns(20);
        customCode.setRows(5);
        customCode.setText(bundle.getString("ScriptUI.customCode.text")); // NOI18N
        customCode.setFont(new Font("Monospaced", 0, 12));
        scriptScroll.setViewportView(customCode);

        fileButton.setText(bundle.getString("ScriptUI.fileButton.text")); // NOI18N
        fileButton.setName("fileButton"); // NOI18N

        fileField.setText(bundle.getString("ScriptUI.fileField.text")); // NOI18N
        fileField.setName("fileField"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(scriptScroll, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scriptLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(fileField, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(fileButton, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(scriptLabel)
                    .addComponent(fileButton)
                    .addComponent(fileField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(scriptScroll, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private SettingArea customCode;
    // End of variables declaration//GEN-END:variables
    private boolean fromFile;

    /**
     * The {@link ComponentModel} for the {@link ScriptUI} component.
     */
    public static class Model implements ComponentModel<ScriptUI> {

        private Model (String code, boolean file, boolean valid) {
            this.code = code;
            this.file = file;
            this.valid = valid;
        }
        /**
         * The raw code string or the file path submitted.
         * @see #file
         */
        public final String code;
        /**
         * Whether or not {@link #code} is a file path.
         */
        public final boolean file;
        /**
         * Whether or not the component data was actually valid when the {@link Model}
         * was created. Note that it might not be if this data was optional.
         */
        public final boolean valid;

        @Override
        public void populate (ScriptUI ui) {
            TextSetting s = file ? ui.file.getTextField() : ui.customCode;
            s.setValue(code);
        }
    }

    @Override
    public Model createModel () {
        return new Model(fromFile ? file.getValue() : customCode.getValue(),
                         fromFile,
                         valid);
    }
    private boolean valid;

    @Override
    public boolean checkUI () {
        valid = (fromFile = file.check());
        if (file.getValue() == null) {
            customCode.drop();
            valid = customCode.getValue() != null;
            /*
             * fromFile might be true when a script is optional *and* no file 
             * has been supplied. 
             */
            boolean result = valid || fromFile; // custom code or optional?
            fromFile = false; // if optional: mark as custom code.
            return result;
        }
        return valid;
    }
}
