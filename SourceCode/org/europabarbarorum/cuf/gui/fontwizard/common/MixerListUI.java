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
 * MixerListUI.java
 *
 * Created on Oct 3, 2010, 11:03:22 PM
 */
package org.europabarbarorum.cuf.gui.fontwizard.common;

import org.europabarbarorum.cuf.gui.support.ScrollingSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.europabarbarorum.cuf.font.MixedSource.MixinMode;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode;
import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode.MixedFontNode;
import org.europabarbarorum.cuf.gui.fontwizard.Messages;
import org.europabarbarorum.cuf.gui.fontwizard.common.MixerListUI.Model;
import org.europabarbarorum.cuf.gui.support.ComponentState;

/**
 * A form to alter how {@link FontTreeNode child nodes} are mixed in the current {@link MixedFontNode}.
 * @author Johan Ouwerkerk
 */
public class MixerListUI extends JPanel implements
        ComponentState<Model> {

    private List<String> names;
    private List<MixinMode> modes;
    private final MixTableModel model;

    /**
     * Creates a new {@link MixerListUI}.
     * @param node the {@link MixedFontNode} to edit.
     */
    public MixerListUI (MixedFontNode node) {
        this.names = new ArrayList<String>(node.getChildCount());
        this.modes = new ArrayList<MixinMode>(node.getChildCount());
        FontTreeNode n;
        Enumeration<FontTreeNode> nodes = node.children();
        while (nodes.hasMoreElements()) {
            n = nodes.nextElement();
            this.names.add(n.getName());
            this.modes.add(n.getMode());
        }
        model = new MixTableModel();
        initComponents();
    }

    /**
     * Create a new {@link MixerListUI} for GUI builder purposes.
     * Makes this class a valid JavaBean.
     */
    public MixerListUI () {
        this(null);
    }

    @Override
    public Model createModel () {
        return new Model(names, modes);
    }

    @Override
    public boolean checkUI () {
        return true;
    }

    private class MixTableModel implements TableModel {

        @Override
        public int getRowCount () {
            return names.size();
        }

        @Override
        public int getColumnCount () {
            return 2;
        }

        @Override
        public String getColumnName (int columnIndex) {
            return columnIndex == 0
                    ? Messages.FontName.getText()
                    : Messages.FontMode.getText();
        }

        @Override
        public Class<?> getColumnClass (int columnIndex) {
            return columnIndex == 0 ? String.class : Boolean.class;
        }

        @Override
        public boolean isCellEditable (int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt (int rowIndex, int columnIndex) {
            return columnIndex == 0 ? names.get(rowIndex) : keep(rowIndex);
        }

        private Boolean keep (int row) {
            return modes.get(row) == MixinMode.Keep;
        }

        private MixinMode keep (boolean mode) {
            return mode ? MixinMode.Keep : MixinMode.Remove;
        }

        private boolean set (String str, int row) {
            if (str == null || str.equals("")) {
                return false;
            }
            int i = names.indexOf(str);
            if (i > -1) {
                return false;
            }
            names.set(row, str);
            return true;
        }

        private boolean toggle (Boolean b, int row) {
            modes.set(row, keep(b));
            return true;
        }

        @Override
        public void setValueAt (Object aValue, int rowIndex, int columnIndex) {
            boolean update = columnIndex == 0
                    ? set(aValue.toString(), rowIndex)
                    : toggle((Boolean) aValue, rowIndex);
            if (update) {
                updateTable();
            }
        }

        public void updateTable () {
            if (tml != null) {
                tml.tableChanged(new TableModelEvent(this));
            }
        }

        @Override
        public void addTableModelListener (TableModelListener l) {
            this.tml = l;
        }
        private TableModelListener tml;

        @Override
        public void removeTableModelListener (TableModelListener l) {
            tml = null;
        }
    }

    /**
     * A {@link ComponentModel} for a {@link MixerListUI}.
     */
    public static class Model implements ComponentModel<MixerListUI> {

        private final List<String> n;
        private final List<MixinMode> m;

        private Model (List<String> n, List<MixinMode> m) {
            this.n = Collections.unmodifiableList(n);
            this.m = Collections.unmodifiableList(m);
        }

        /**
         * Get the names of the nodes described by this {@link Model}.
         * @return a {@link List} of names, one per node in order of appearance in a font tree.
         */
        public List<String> names () {
            return n;
        }


        /**
         * Get the {@link MixinMode modes} of the nodes described by this {@link Model}.
         * @return a {@link List} of {@link MixinMode modes}, one per node in order of appearance in a font tree.
         */
        public List<MixinMode> modes () {
            return m;
        }

        @Override
        public void populate (MixerListUI ui) {
            ui.names = n;
            ui.modes = m;
            ui.model.updateTable();
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
        JScrollPane tableScroll = new ScrollingSupport();
        JTable table = new JTable();

        setName("Form"); // NOI18N

        tableScroll.setName("tableScroll"); // NOI18N

        table.setModel(model);
        table.setName("table"); // NOI18N
        tableScroll.setViewportView(table);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableScroll, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addGap(13, 13, 13))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableScroll, GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                .addGap(13, 13, 13))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
