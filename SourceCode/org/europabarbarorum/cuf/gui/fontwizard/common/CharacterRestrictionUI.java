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
 * CharacterRestrictionUI.java
 *
 * Created on Dec 17, 2010, 6:29:35 PM
 */
package org.europabarbarorum.cuf.gui.fontwizard.common;

import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.europabarbarorum.cuf.gui.fontwizard.common.CharacterRestrictionUI.Model;
import org.europabarbarorum.cuf.gui.support.CharacterSettingField;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 *
 * @author Johan Ouwerkerk
 */
public class CharacterRestrictionUI extends JPanel implements
        ComponentState<Model> {

    /** Creates new form CharacterRestrictionUI */
    public CharacterRestrictionUI () {
        initComponents();
    }

    @Override
    public boolean checkUI () {
        boolean uValid = upper.checkUI(), lValid = lower.checkUI();
        if(uValid && lValid) {
            int lCode = lower.code(), uCode = upper.code();
            if(!lowerIncluded.isSelected()) {
                lCode++;
            }
            if(!upperIncluded.isSelected()) {
                uCode--;
            }
            return uCode <= lCode? upper.reset() : true;
        }
        return false;
    }

    @Override
    public Model createModel () {
        return new Model(exclude.isSelected(),
                         upperIncluded.isSelected(),
                         lowerIncluded.isSelected(),
                         upper.createModel(),
                         lower.createModel());
    }

    /**
     * A {@link ComponentModel} for {@link CharacterRestrictionUI}.
     */
    public static class Model implements ComponentModel<CharacterRestrictionUI> {

        private Model (boolean e,
                       boolean uI,
                       boolean lI,
                       CharacterSettingField.Model u,
                       CharacterSettingField.Model l) {
            this.excludesRange = e;
            this.lowerIncluded = lI;
            this.upperIncluded = uI;
            this.lower = l;
            this.upper = u;
        }
        /**
         * If true, the specified range determines the characters to exclude,
         * rather than to include.
         */
        public final boolean excludesRange;
        private final boolean upperIncluded;
        private final boolean lowerIncluded;
        private final CharacterSettingField.Model upper;
        private final CharacterSettingField.Model lower;

        /**
         * Get the lower bound of the specified range (inclusive).
         * @return the lowest code point included in the specified range.
         */
        public int lower () {
            return lowerIncluded ? lower.code : (lower.code + 1);
        }

        /**
         * Get the upper bound of the specified range (inclusive).
         * @return the highest code point included in the specified range.
         */
        public int upper () {
            return upperIncluded ? upper.code : (upper.code - 1);
        }

        /**
         * Get the upper bound of the specified range as character (inclusive).
         * @return the character corresponding to the {@link #lower() upper bound}.
         */
        public Character upperChar () {
            return IOHelp.fromCode(upper());
        }

        /**
         * Get the lower bound of the specified range as character (inclusive).
         * @return the character corresponding to the {@link #lower() lower bound}.
         */
        public Character lowerChar () {
            return IOHelp.fromCode(lower());
        }

        @Override
        public void populate (CharacterRestrictionUI ui) {
            lower.populate(ui.lower);
            upper.populate(ui.upper);
            ui.lowerIncluded.setSelected(lowerIncluded);
            ui.upperIncluded.setSelected(upperIncluded);
            ui.exclude.setSelected(excludesRange);
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
        JLabel lowerLabel = new JLabel();
        JLabel upperLabel = new JLabel();
        lower = new CharacterSettingField();
        upper = new CharacterSettingField();
        exclude = new JCheckBox();
        lowerIncluded = new JCheckBox();
        upperIncluded = new JCheckBox();

        setName("Form"); // NOI18N

        ResourceBundle bundle = ResourceBundle.getBundle("org/europabarbarorum/cuf/gui/fontwizard/common/CommonUI"); // NOI18N
        lowerLabel.setText(bundle.getString("CharacterRestrictionUI.lowerLabel.text")); // NOI18N
        lowerLabel.setName("lowerLabel"); // NOI18N

        upperLabel.setText(bundle.getString("CharacterRestrictionUI.upperLabel.text")); // NOI18N
        upperLabel.setName("upperLabel"); // NOI18N

        lower.setName("lower"); // NOI18N

        upper.setName("upper"); // NOI18N

        exclude.setText(bundle.getString("CharacterRestrictionUI.exclude.text")); // NOI18N
        exclude.setName("exclude"); // NOI18N

        lowerIncluded.setText(bundle.getString("CharacterRestrictionUI.lowerIncluded.text")); // NOI18N
        lowerIncluded.setHorizontalTextPosition(SwingConstants.LEADING);
        lowerIncluded.setName("lowerIncluded"); // NOI18N

        upperIncluded.setText(bundle.getString("CharacterRestrictionUI.upperIncluded.text")); // NOI18N
        upperIncluded.setHorizontalTextPosition(SwingConstants.LEADING);
        upperIncluded.setName("upperIncluded"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(exclude)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(lowerLabel)
                            .addComponent(upperLabel))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(upper, GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                            .addComponent(lower, GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(lowerIncluded)
                            .addComponent(upperIncluded))))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {lowerLabel, upperLabel});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {lowerIncluded, upperIncluded});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lower, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lowerIncluded)
                    .addComponent(lowerLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(upper, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(upperIncluded)
                    .addComponent(upperLabel))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(exclude)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {lower, upper});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {lowerLabel, upperLabel});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {lowerIncluded, upperIncluded});

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox exclude;
    private CharacterSettingField lower;
    private JCheckBox lowerIncluded;
    private CharacterSettingField upper;
    private JCheckBox upperIncluded;
    // End of variables declaration//GEN-END:variables
}
