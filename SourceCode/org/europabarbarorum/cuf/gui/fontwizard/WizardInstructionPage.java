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
 * WizardInstructionPage.java
 *
 * Created on Sep 26, 2010, 2:23:30 AM
 */
package org.europabarbarorum.cuf.gui.fontwizard;

import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage.FontTypeAction;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage.NextFontWizardPage;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.support.IllegalMethodCallException;

/**
 * A default {@link FontWizardPage page} used when no other page can be used.
 * Since this page has basically no real information to display, it should contain a nice
 * information message/graphic which gives some (needless?) instructions on working with the
 * {@link FontWizard}.
 * @author Johan Ouwerkerk
 */
public class WizardInstructionPage extends NextFontWizardPage implements FontTypeAction{

    /**
     * Create a {@link WizardInstructionPage}.
     * @param parent the parent {@link FontTreeNode} to use.
     * @param shell the context {@link Shell} to use.
     */
    public WizardInstructionPage (FontTreeNode parent, Shell shell) {
        super(parent, shell);
        initComponents();
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
        JScrollPane jScrollPane1 = new JScrollPane();
        JTextArea details = new JTextArea();

        setName("Form"); // NOI18N

        instruction.setHorizontalAlignment(SwingConstants.CENTER);
        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/fontwizard/WizardInstructionPage"); // NOI18N
        instruction.setText(bundle.getString("WizardInstructionPage.instruction.text")); // NOI18N
        instruction.setName("instruction"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        details.setColumns(20);
        details.setRows(5);
        details.setText(bundle.getString("WizardInstructionPage.details.text")); // NOI18N
        details.setName("details"); // NOI18N
        jScrollPane1.setViewportView(details);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(instruction, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(instruction)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public boolean checkUI () {
        return true;
    }

    @Override
    protected void fillIn (FontTreeNode node) {
        throw new IllegalMethodCallException();
    }


    @Override
    protected FontTypeAction getSelected () {
        return this;
    }

    @Override
    protected void cancelAction (FontWizard wiz) {
        wiz.dispatchEvent(new WindowEvent(wiz, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void nextAction (FontWizard wiz, FontWizardPage prev) {
        cancelAction(wiz);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
