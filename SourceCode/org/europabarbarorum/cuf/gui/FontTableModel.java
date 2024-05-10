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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.impl.WrappedSource.ReadThroughSource;
import org.europabarbarorum.cuf.font.impl.FormatConstants;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.gui.FontTableModel.DelegateModel;
import org.europabarbarorum.cuf.gui.support.UpdatableModel;
import org.europabarbarorum.cuf.gui.support.UpdatableModel.UpdatableFont;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 *
 * @author Johan Ouwerkerk
 */
public class FontTableModel implements
        UpdatableFont,
        UpdatableModel<DelegateModel>,
        TableModel {

    /**
     * Various radices supported by the {@link FontTableModel} class. These values are used for
     * formatting the various values in a table.
     */
    public enum Radix implements DefaultOption {

        /**
         * Use hexadecimal.
         */
        hexadecimal(16),
        /**
         * Use binary.
         */
        binary(2),
        /**
         * Use decimal.
         */
        decimal(10);
        private final int r;

        private Radix (int i) {
            this.r = i;
        }

        /**
         * Return the integer to use as radix when formatting/parsing strings as integer within the
         * {@link FontTableModel}.
         * @return the radix in integer form
         */
        public int value () {
            return r;
        }

        @Override
        public Radix defaultOption () {
            return decimal;
        }
    }

    /**
     * Set the {@link Radix} to use.
     * @param r the radix to use.
     */
    public void setRadix (Radix r) {
        radix = r;
        updateTable();
    }

    /**
     * Get the {@link Radix} being used.
     * @return the radix currently used.
     */
    public Radix getRadix () {
        return radix;
    }
    private Radix radix;

    /**
     * Create a model for a table. This maps the content of
     * {@link CUFSource#getCUFProperties() CUF properties} to a
     * {@link TableModel}.
     * @param radix the {@link Radix} to use.
     * @param model the {@link DelegateModel} to use. If null the table will be empty.
     * @see #getRadix() 
     * @see #setRadix(org.europabarbarorum.cuf.gui.FontTableModel.Radix)
     * @see DelegateModel
     * @see #updateModel(org.europabarbarorum.cuf.font.CUFSource)
     */
    public FontTableModel (Radix radix, DelegateModel model) {
        this.radix = radix;
        this.delegate = model;
    }

    /**
     * Create an empty model for a table. This maps the content of
     * {@link CUFSource#getCUFProperties() CUF properties} to a
     * {@link TableModel}.
     * @param radix the {@link Radix} to use.
     * @see #getRadix() 
     * @see #setRadix(org.europabarbarorum.cuf.gui.FontTableModel.Radix) 
     * @see DelegateModel
     * @see #updateModel(org.europabarbarorum.cuf.font.CUFSource) 
     */
    public FontTableModel (Radix radix) {
        this(radix, null);
    }

    /**
     * Get the type of object displayed at the given column in this {@link TableModel}.
     * @param columnIndex the column to select
     * @return either a String.class (if columnIndex is 0) or Integer.class object (if columnIndex is 1).
     */
    @Override
    public Class<?> getColumnClass (int columnIndex) {
        return columnIndex == 0 ? String.class : Integer.class;
    }

    /**
     * Signal that some update occurred in this {@link TableModel}.
     * This method simply fires a new {@link TableModelEvent} on the registered {@link TableModelListener} if
     * that is not null.
     */
    public void updateTable () {
        if (tml != null) {
            tml.tableChanged(new TableModelEvent(this));
        }
    }

    /**
     * Class to encapsulate the express the variables between various flavours of
     * {@link FontTableModel}. The idea is that the same code can be re-used without
     * modification to present different table models depending on
     * what {@link CUFProperty properties} need to be displayed.
     */
    public static abstract class DelegateModel {

        /**
         * Fetch a value for a given {@link CUFProperty}.
         * @param key the {@link CUFProperty} to retrieve.
         * @return the value for the given property.
         */
        protected abstract int get (CUFProperty key);

        /**
         * Commit a value for a given {@link CUFProperty}.
         * @param key the {@link CUFProperty} to commit.
         * @param val the value to commit.
         */
        protected abstract void set (CUFProperty key, int val);

        /**
         * Check if this model contains the given {@link CUFProperty}.
         * @param key the {@link CUFProperty} to check.
         * @return <code>true</code> if this model may contain a value for the given {@link CUFProperty property}, 
         * <code>false</code> if it is not supported.
         */
        protected abstract boolean contains (CUFProperty key);

        /**
         * Returns the number of rows in a {@link FontTableModel}.
         * @return the number of {@link CUFProperty properties} covered by this delegate.
         */
        protected abstract int size ();
    }

    /**
     * A {@link DelegateModel} which provides minimal support for an editable model
     * on top of an integer array.
     */
    public static class EditorDelegate extends DelegateModel {

        private final Integer[] props;

        /**
         * Create a new {@link EditorDelegate}.
         */
        public EditorDelegate () {
            props = new Integer[FormatConstants.NumEditableCUFProps.value()];
        }

        @Override
        protected int get (CUFProperty key) {
            return props[key.index()];
        }

        @Override
        protected void set (CUFProperty key, int val) {
            props[key.index()] = val;
        }

        /**
         * Returns the number of rows in a {@link FontTableModel}.
         * @return the value of {@link FormatConstants#NumEditableCUFProps}.
         */
        @Override
        public int size () {
            return props.length;
        }

        @Override
        protected boolean contains (CUFProperty key) {
            int k = key.index();
            return k < size() && props[k] != null;
        }
    }

    /**
     * A custom {@link ReadThroughSource} to present an editable {@link CUFSource}
     * without actually committing any edits.
     */
    public static class TableSource extends ReadThroughSource {

        private final EditorDelegate ed;

        /**
         * Create a new {@link TableSource}.
         * @param toWrap the {@link CUFSource} to shield from any edits through the table.
         */
        @SuppressWarnings("unchecked")
        public TableSource (CUFSource toWrap) {
            super(toWrap.getCufSource(), toWrap);
            ed = new EditorDelegate();
        }

        @Override
        public void setCUFProperties (CUFProperty index, int value) {
            ed.set(index, value);
        }

        @Override
        public boolean isAvailable (CUFProperty index) {
            return ed.contains(index) || super.isAvailable(index);
        }

        @Override
        public int getCUFProperties (CUFProperty index) {
            return ed.contains(index)
                    ? ed.get(index)
                    : super.getCUFProperties(index);
        }
    }

    /**
     * Wrapper {@link DelegateModel} around a {@link CUFSource}.
     */
    public static class CUFSourceDelegate extends DelegateModel {

        private final CUFSource src;

        /**
         * Create a new {@link CUFSourceDelegate}.
         * @param src the {@link CUFSource} to wrap.
         */
        public CUFSourceDelegate (CUFSource src) {
            this.src = src;
        }

        @Override
        protected int get (CUFProperty key) {
            return src.getCUFProperties(key);
        }

        @Override
        protected void set (CUFProperty key, int val) {
            src.setCUFProperties(key, val);
        }

        @Override
        protected boolean contains (CUFProperty key) {
            return src.isAvailable(key);
        }

        /**
         * Returns the number of rows in a {@link FontTableModel}.
         * @return the value of {@link FormatConstants#NumCUFProps}.
         */
        @Override
        public int size () {
            return FormatConstants.NumCUFProps.value();
        }
    }
    private DelegateModel delegate;

    /**
     * Support for extracting a copy of the values of all
     * {@link CUFProperty#isModifiable() editable} {@link CUFProperty properties}
     * from the table.
     * @return a new {@link EditorDelegate} initialised with a copy of the values of all
     * {@link CUFProperty#isModifiable() editable} {@link CUFProperty properties}
     * from the table.
     */
    public EditorDelegate copyEdits () {
        EditorDelegate ed = new EditorDelegate();
        for (CUFProperty p : CUFProperty.values()) {
            if (p.isModifiable() && delegate.contains(p)) {
                ed.set(p, delegate.get(p));
            }
        }
        return ed;
    }

    @Override
    public void updateModel (CUFSource source) {
        updateModel(new CUFSourceDelegate(source));
    }

    @Override
    public void updateModel (DelegateModel source) {
        this.delegate = source;
        updateTable();
    }

    /**
     * Returns the numbr of rows in a {@link FontTableModel}.
     * @return the value of {@link FormatConstants#NumCUFProps}.
     */
    @Override
    public int getRowCount () {
        return delegate == null ? 0 : delegate.size();
    }

    /**
     * Returns the number of columns in a {@link FontTableModel}.
     * @return 2
     */
    @Override
    public int getColumnCount () {
        return 2;
    }

    /**
     * Get the type of value for the given column.
     * @param columnIndex the give column.
     * @return the type of the value.
     */
    @Override
    public String getColumnName (int columnIndex) {
        return columnIndex == 0
                ? Messages.PropNameHeading.getText()
                : Messages.PropValueHeading.getText();
    }

    /**
     * Returns whether or not a cell is allowed to be edited through the GUI.
     * @param rowIndex the row of the cell
     * @param columnIndex the column of the cell
     * @return true if the {@link FontTableModel} is properly populated and the given position
     * corresponds to the value of a CUF property that is {@link CUFProperty#isModifiable() editable}.
     */
    @Override
    public boolean isCellEditable (int rowIndex, int columnIndex) {
        CUFProperty key = CUFProperty.forIndex(rowIndex);
        return delegate != null && columnIndex != 0 && key.isModifiable();
    }

    /**
     * Get the value at a given cell.
     * @param rowIndex the row of the cell
     * @param columnIndex the value of the cell
     * @return a property name, the constant {@link Messages#PropUnavailable} or the value of
     * a {@link DelegateModel#get(org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty) property}
     * formatted according to the currently used {@link Radix}.
     */
    @Override
    public Object getValueAt (int rowIndex, int columnIndex) {
        CUFProperty key = CUFProperty.forIndex(rowIndex);
        if (columnIndex == 0) {
            return key.getText();
        }
        if (delegate == null) {
            return Messages.PropUnavailable.getText();
        }
        else {
            if (delegate.contains(key)) {
                int i = delegate.get(key);
                if (key.isModifiable() && radix != Radix.decimal) {
                    i = key.translator.setTranslate(i);
                }
                return Integer.toString(i, radix.value());
            }
            else {
                return Messages.PropUnavailable.getText();
            }
        }
    }

    /**
     * Attempt to parse an object value to an integer.
     * @param aValue object value to parse. Value is simply converted to string, and the
     * result is parsed as integer using currently selected {@link Radix} for this model.
     * @param rowIndex index of the {@link CUFProperty} to use for translating between
     * integer values.
     * @return a parsed Integer or null if the given value is invalid.
     * @see CUFProperty#translator
     */
    public Integer parse (Object aValue, int rowIndex) {
        CUFProperty key = CUFProperty.forIndex(rowIndex);
        String text = "" + aValue;
        try {
            if (!key.isModifiable()) {
                throw new IllegalArgumentException(key.getText());
            }
            int i = Integer.parseInt(text, radix.value());
            Integer k = key.translator.getTranslate(i);
            if (k != null) {
                return k;
            }
            if (key.translator.setTranslate(i) != null) {
                return i;
            }
            throw new IllegalArgumentException(text);
        }
        catch (Exception e) {
            IOHelp.handleExceptions(FontTableModel.class,
                                    "parse",
                                    e,
                                    Messages.PropValueIllegal,
                                    aValue,
                                    key.getText());
            return null;
        }

    }

    /**
     * Change a property value of a {@link CUFSource}, if the cell is editable.
     * @param aValue the value to change the given property to.
     * @param rowIndex the row this property occupies in the {@link FontTableModel}
     * @param columnIndex should be 1 to edit the value (otherwise the cell won't be editable).
     * @see #isCellEditable(int, int)
     */
    @Override
    public void setValueAt (Object aValue, int rowIndex, int columnIndex) {
        if (isCellEditable(rowIndex, columnIndex)) {
            CUFProperty key = CUFProperty.forIndex(rowIndex);
            try {
                if (aValue instanceof Integer) {
                    delegate.set(key, (Integer) aValue);
                    updateTable();
                }
            }
            catch (Exception e) {
                IOHelp.handleExceptions(FontTableModel.class,
                                        "setValueAt",
                                        e,
                                        Messages.PropValueIllegal,
                                        aValue,
                                        key.getText());
            }
        }
    }

    /**
     * Register a {@link TableModelListener} with this {@link TableModel}.
     * Note this model supports only one listener and that slot is intended for the
     * GUI itself. So this method probably does not do what you want anyway.
     * @param l the listener object that is to receive updates on the {@link FontTableModel}
     */
    @Override
    public void addTableModelListener (TableModelListener l) {
        this.tml = l;
    }
    private TableModelListener tml;

    /**
     * Remove the {@link TableModelListener} from this {@link TableModel}.
     * Note this model supports only one listener and that slot is intended for the
     * GUI itself. So this method probably does not do what you want anyway.
     * @param l the listener to remove.
     */
    @Override
    public void removeTableModelListener (TableModelListener l) {
        tml = null;
    }
}
