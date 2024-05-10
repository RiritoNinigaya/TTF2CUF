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
 * CreateNextFontPage.java
 *
 * Created on Sep 20, 2010, 3:15:58 AM
 */
package org.europabarbarorum.cuf.gui.fontwizard.create;

import org.europabarbarorum.cuf.gui.fontwizard.common.FontTableUI;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.BasicModel;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage.FontTypeAction;
import org.europabarbarorum.cuf.gui.fontwizard.FontWizardPage.NextFontWizardPage;
import org.europabarbarorum.cuf.gui.fontwizard.create.CreateNextFontPage.FontTypeOption;
import org.europabarbarorum.cuf.gui.support.SettingOption.DropDown;
import org.europabarbarorum.cuf.support.DefaultOption;

/**
 * A base class for {@link NextFontWizardPage wizard pages} that select {@link FontTypeOption type of font node} to create
 * and additionally allow editing of meta information.
 * @param <A> the type of the {@link FontTypeOption} implementation used.
 * Valid implementations must be {@link Enum enums} which implement {@link FontTypeOption}.
 * @author Johan Ouwerkerk
 */
public abstract class CreateNextFontPage<A extends Enum<? extends FontTypeOption>> extends NextFontWizardPage<BasicModel> {

    /**
     * Create a new {@link CreateNextFontPage}
     * @param page the previous {@link FontWizardPage}.
     */
    public CreateNextFontPage (CreateFontPage page) {
        super(page);
        initComponents();
    }

    @Override
    protected FontTypeAction getSelected () {
        DropDown d = ((DropDown) typeOption);
        return ((FontTypeAction) d.getModel().getSelectedItem().opt);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JLabel typeLabel = new JLabel();
        typeOption = types();
        fontTableUI = new FontTableUI();

        setName("Form"); // NOI18N

        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/fontwizard/create/PageUI"); // NOI18N
        typeLabel.setText(bundle.getString("CreateNextFontPage.typeLabel.text")); // NOI18N
        typeLabel.setName("typeLabel"); // NOI18N

        typeOption.setName("typeOption"); // NOI18N

        fontTableUI.setName("fontTableUI"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(typeOption, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(typeLabel)))
            .addComponent(fontTableUI, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(typeLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(typeOption, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(fontTableUI, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private FontTableUI fontTableUI;
    protected JComboBox typeOption;
    // End of variables declaration//GEN-END:variables

    @Override
    public boolean checkUI () {
        return true;
    }

    @Override
    protected void fillIn (BasicModel node) {
        node.setTableWidgetsModel(fontTableUI.createModel());
    }

    private DropDown types () {
        Class<A> cls = autoType();
        if (cls == null) {
            return new DropDown(new DefaultOption[0], getClass(), getClass());
        }
        return new DropDown((DefaultOption[]) cls.getEnumConstants(), getClass(),
                            cls);
    }

    /**
     * Get the specific type of {@link FontTypeOption} used by this {@link CreateNextFontPage}.
     * @return  the {@link Class} of the {@link FontTypeOption} implementation used by this
     * {@link CreateNextFontPage}.
     */
    protected abstract Class<A> autoType ();

    /**
     * An interface to bridge {@link DefaultOption} with {@link FontTypeAction}.
     * This is necessary since each instance represents a selectable combobox item which
     * refers to a {@link FontTypeAction}.
     */
    public static interface FontTypeOption extends FontTypeAction, DefaultOption {
    }
    // End of variables declaration
}
