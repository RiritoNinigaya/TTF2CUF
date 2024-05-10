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
package org.europabarbarorum.cuf.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableModel;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.gui.FontTableModel.DelegateModel;
import org.europabarbarorum.cuf.gui.FontTableModel.Radix;
import org.europabarbarorum.cuf.gui.FontTableWidgets.FontWizardTableWidgets;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;
import org.europabarbarorum.cuf.gui.support.TextActions;
import org.europabarbarorum.cuf.support.OptionMap;

/**
 * A class to combine various bits of GUI logic needed for a displaying a table of CUF properties for a CUF font.
 * @author Johan Ouwerkerk
 */
public class FontTableWidgets {

    private FontTableModel model;
    private FontTable table;
    private RadixButton binary, decimal, hexadecimal;
    private ButtonGroup radixGroup;
    private ActionListener radixAction;

    private ButtonGroup group () {
        if (radixGroup == null) {
            radixGroup = new ButtonGroup();
        }
        return radixGroup;
    }

    private ActionListener action () {
        if (radixAction == null) {
            radixAction = new ActionListener() {

                @Override
                public void actionPerformed (ActionEvent e) {
                    RadixButton b = (RadixButton) e.getSource();
                    model().setRadix(b.radix);
                }
            };
        }
        return radixAction;
    }

    private boolean isDefault (Radix r) {
        return model == null ? r == defaultRadix() : model.getRadix() == r;
    }

    private Radix defaultRadix () {
        return OptionMap.getDefault(Radix.class);
    }

    private RadixButton button (Radix r) {
        RadixButton b = new RadixButton(r);
        group().add(b);
        b.addActionListener(action());
        b.setSelected(isDefault(r));
        return b;
    }

    /**
     * Get the {@link JRadioButton button} which selects {@link Radix#decimal decimal} mode.
     * @return the {@link JRadioButton button} used to select {@link Radix#decimal decimal} mode.
     */
    public JRadioButton decimal () {
        if (decimal == null) {
            decimal = button(Radix.decimal);
        }
        return decimal;
    }


    /**
     * Get the {@link JRadioButton button} which selects {@link Radix#hexadecimal hexadecimal} mode.
     * @return the {@link JRadioButton button} used to select {@link Radix#hexadecimal hexadecimal} mode.
     */
    public JRadioButton hexadecimal () {
        if (hexadecimal == null) {
            hexadecimal = button(Radix.hexadecimal);
        }
        return hexadecimal;
    }


    /**
     * Get the {@link JRadioButton button} which selects {@link Radix#binary binary} mode.
     * @return the {@link JRadioButton button} used to select {@link Radix#binary binary} mode.
     */
    public JRadioButton binary () {
        if (binary == null) {
            binary = button(Radix.binary);
        }
        return binary;
    }

    /**
     * Get the {@link FontTableModel} managed by this {@link FontTableWidgets} object.
     * @return the {@link FontTableModel} covered by this {@link FontTableWidgets} object.
     */
    public FontTableModel model () {
        if (model == null) {
            model = new FontTableModel(defaultRadix());
        }
        return model;
    }

    /**
     * Get the {@link JTable} used to display the {@link #model() managed}
     * {@link FontTableModel} object.
     * @return the {@link JTable} managed by this {@link FontTableWidgets} object.
     */
    public JTable table () {
        if (table == null) {
            table = new FontTable(model());
        }
        return table;
    }

    /**
     * A class which adds support for presenting a {@link FontTableWidgets} as
     * {@link ComponentState}.
     */
    public static class FontWizardTableWidgets extends FontTableWidgets implements
            ComponentState<FontWizardWidgetsModel> {

        @Override
        public FontWizardWidgetsModel createModel () {
            FontTableModel m = model();
            return new FontWizardWidgetsModel(m.getRadix(), m.copyEdits());
        }

        @Override
        public boolean checkUI () {
            return true;
        }

        private void select (Radix r) {
            RadixButton[] opts = new RadixButton[] { super.binary,
                                                     super.hexadecimal,
                                                     super.decimal };
            for (RadixButton b : opts) {
                if (b.radix == r) {
                    b.setSelected(true);
                    return;
                }
            }
        }
    }

    /**
     * A {@link ComponentModel} for {@link FontWizardTableWidgets}.
     */
    public static class FontWizardWidgetsModel implements
            ComponentModel<FontWizardTableWidgets> {

        /**
         * Create a new {@link FontWizardWidgetsModel}.
         * @param r the selected {@link Radix}.
         * @param d a {@link DelegateModel model} of the values selected
         * for the various {@link CUFProperty CUF properties}
         */
        public FontWizardWidgetsModel (Radix r, DelegateModel d) {
            this.d = d;
            this.r = r;
        }
        private final DelegateModel d;
        private final Radix r;
        /**
         * Apply the values specified in this {@link FontWizardWidgetsModel} for the 
         * various {@link CUFProperty CUF properties} to a given {@link CUFSource font}.
         * @param source the {@link CUFSource font} to update.
         */
        public void apply(CUFSource source) {
            for(CUFProperty p: CUFProperty.values()) {
                if(d.contains(p)) {
                    source.setCUFProperties(p, d.get(p));
                }
            }
        }
        
        @Override
        public void populate (FontWizardTableWidgets ui) {
            FontTableModel m = ui.model();
            m.setRadix(r);
            m.updateModel(d);
            ui.select(r);
        }
    }
}

class RadixButton extends JRadioButton {

    public final Radix radix;

    public RadixButton (Radix r) {
        this.radix = r;
    }
}

class FontTable extends JTable {

    public FontTable (FontTableModel model) {
        super.putClientProperty("terminateEditOnFocusLost", true);
        setModel(model);
        setDefaultEditor(Integer.class, new CellEditor(new JTextField()));
    }

    @Override
    public final FontTableModel getModel () {
        return (FontTableModel) super.getModel();
    }

    @Override
    public final void setModel (TableModel dataModel) {
        if (dataModel instanceof FontTableModel) {
            super.setModel(dataModel);
        }
    }

    private class CellEditor extends DefaultCellEditor {

        private CellEditor (JTextField textfield) {
            super(textfield);
            textfield.setBorder(new LineBorder(getSelectionBackground()));
            this.setClickCountToStart(1);
            TextActions.setPopupMenu(textfield, null);
        }
        private int row;

        @Override
        public Component getTableCellEditorComponent (JTable table,
                                                      Object value,
                                                      boolean isSelected,
                                                      int row, int column) {
            this.row = row;
            return super.getTableCellEditorComponent(table,
                                                     value,
                                                     isSelected,
                                                     row,
                                                     column);
        }

        @Override
        public Object getCellEditorValue () {
            return getModel().parse(super.getCellEditorValue(), row);
        }
    }
}
