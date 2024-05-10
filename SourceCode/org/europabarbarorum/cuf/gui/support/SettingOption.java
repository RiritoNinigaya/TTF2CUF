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
package org.europabarbarorum.cuf.gui.support;

import java.awt.Component;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * Wraps a setting value into an item object that can be used in a {@link ComboBoxModel}.
 * @param <T> type of data object that item represents.
 * @author Johan Ouwerkerk
 */
public abstract class SettingOption<T> implements BundleKey {

    /**
     * Create a new {@link SettingOption}.
     * @param opt the option value (data).
     */
    public SettingOption (T opt) {
        this.opt = opt;
    }
    /**
     * The {@link DefaultOption} encapsulated by this {@link SettingOption}.
     */
    public final T opt;

    /**
     * Get a {@link BundleKey} for retrieving tooltip text.
     * @return a {@link BundleKey} which
     * provides tooltip text for this {@link SettingOption}.
     */
    public BundleKey tooltip () {
        return tooltip;
    }

    /**
     * Get the {@link Class} used to denote the resource bundle in the {@link BundleKey}
     * implementation.
     * @return the {@link Class} whose canonical classname corresponds to the
     * resource bundle used by this {@link SettingOption} for looking up text.
     * @see ResourceHelp#getBundle(java.lang.Class)
     * @see ResourceHelp#getValue(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey, java.lang.Class)
     * @see #getText()
     */
    protected abstract Class<?> bundleClass ();

    /**
     * Get the {@link Class} used to denote the {@link #type() type} in the {@link BundleKey}
     * implementation.
     * @return the {@link Class} whose canonical classname corresponds to the
     * resource key used by this {@link SettingOption} for looking up text.
     * @see ResourceHelp#getKey(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey)
     * @see ResourceHelp#getValue(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey, java.lang.Class)
     * @see #getText()
     */
    protected abstract Class<?> optionClass ();

    /**
     * Convert this {@link SettingOption} to a name, used to denote the {@link #name() name}
     * in the {@link BundleKey} implementation.
     * @param value the data value of the option.
     * @return the name of the option.
     * @see ResourceHelp#getKey(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey)
     * @see ResourceHelp#getValue(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey, java.lang.Class)
     * @see #getText()
     */
    protected abstract String nameOf (T value);

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, bundleClass());
    }

    /**
     * Shorthand for {@code tooltip().description()}.
     * @return tooltip text for this {@link SettingOption}.
     */
    public String getTooltipText () {
        return tooltip.getText();
    }

    @Override
    public String name () {
        return nameOf(opt);
    }

    @Override
    public Class type () {
        return optionClass();
    }
    private BundleKey tooltip = new BundleKey() {

        @Override
        public String getText () {
            return ResourceHelp.getValue(this, bundleClass());
        }

        @Override
        public String format (Object... args) {
            return ResourceHelp.formatValue(this, bundleClass(), args);
        }

        @Override
        public String name () {
            return nameOf(opt) + ".tooltip";
        }

        @Override
        public Class type () {
            return optionClass();
        }
    };

    /**
     * Equivalent to {@link #getText() }.
     * @return the value returned by {@link #getText() }.
     */
    @Override
    public String toString () {
        return getText();
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, bundleClass(), args);
    }

    /**
     * A {@link ComboBoxModel} that uses {@link SettingOption} objects for items.
     * @param <T> type of data object that items represent
     */
    public abstract static class SettingModel<T> implements ComboBoxModel {

        /**
         * Create a {@link SettingModel}.
         * @param opts an array of data values for the setting. All options must be
         * of the same setting type and they must all be non null,
         * but neither of those constraints is actively enforced here.
         */
        @SuppressWarnings("unchecked")
        public SettingModel (T[] opts) {
            values = new SettingOption[opts.length];
            SettingOption def = getOption(defaultOption(opts[0]));
            for (int k = 0; k < opts.length; ++k) {
                values[k] = def.opt.equals(opts[k]) ? def : getOption(opts[k]);
            }
            setSelectedItem(def);
        }
        private final SettingOption<T>[] values;

        /**
         * Get the default option.
         * @param opt some option of a certain type.
         * @return the default setting (data) for that option type.
         */
        protected abstract T defaultOption (T opt);

        /**
         * Wrap setting data into a {@link SettingOption}.
         * @param value the data value to wrap
         * @return a corresponding {@link SettingOption} item.
         */
        protected abstract SettingOption<T> getOption (T value);

        /**
         * Retrieve option values by index.
         * @param index the index to look up.
         * @return a {@link SettingOption} value corresponding to the given index.
         */
        @Override
        public SettingOption<T> getElementAt (int index) {
            return values[index];
        }

        /**
         * Get the number of options to choose from.
         * @return the number of {@link SettingOption} values encapsulated by this model.
         */
        @Override
        public int getSize () {
            return values.length;
        }
        private SettingOption item;

        /**
         * Set the currently selected item.
         * @param anItem object to select, must be a {@link SettingOption}.
         */
        @Override
        public final void setSelectedItem (Object anItem) {
            this.item = (SettingOption) anItem;
        }

        /**
         * Get the selected {@link SettingOption}.
         * @return the currently selected {@link SettingOption}.
         */
        @Override
        @SuppressWarnings("unchecked")
        public SettingOption<T> getSelectedItem () {
            return item;
        }

        /**
         * Does nothing
         * @param l a {@link ListDataListener}.
         */
        @Override
        public void addListDataListener (ListDataListener l) {
        }

        /**
         * Does nothing
         * @param l a {@link ListDataListener}.
         */
        @Override
        public void removeListDataListener (ListDataListener l) {
        }
    }

    /**
     * Custom {@link JComboBox} which uses a {@link SettingModel} as model, and
     * adds a custom {@link BasicComboBoxRenderer} for rendering of custom
     * tooltips depending on the selected item.
     * @param <T> type of data object that items represents.
     */
    public abstract static class SettingBox<T> extends JComboBox {

        /**
         * Create a new {@link SettingBox}.
         * @param model the {@link SettingModel model} to use.
         */
        public SettingBox (SettingModel<T> model) {
            super(model);
            this.setRenderer(new BasicComboBoxRenderer() {

                @Override
                public Component getListCellRendererComponent (JList list,
                                                               Object value,
                                                               int index,
                                                               boolean isSelected,
                                                               boolean cellHasFocus) {
                    if (isSelected) {
                        setBackground(list.getSelectionBackground());
                        setForeground(list.getSelectionForeground());
                        if (index > -1) {
                            setToolTipText(((SettingOption) value).
                                    getTooltipText());
                        }
                    }
                    else {
                        setBackground(list.getBackground());
                        setForeground(list.getForeground());
                    }
                    setFont(list.getFont());

                    setText(value == null ? "" : value.toString());
                    return this;
                }
            });
        }

        /**
         * Return the current {@link SettingModel}.
         * @return the current {@link SettingModel}.
         */
        @Override
        @SuppressWarnings("unchecked")
        public SettingModel<T> getModel () {
            return (SettingModel<T>) super.getModel();
        }

        /**
         * Set the {@link SettingModel} to use.
         * @param m the model to use, must be a {@link SettingModel}
         */
        @Override
        public void setModel (ComboBoxModel m) {
            if (m instanceof SettingModel) {
                Object opt = ((SettingModel) m).getSelectedItem().opt;
                if (getType().isInstance(opt)) {
                    super.setModel(m);
                    return;
                }
            }
            throw new IllegalArgumentException("Not a valid model type!");
        }

        /**
         * Get the type of data object that items in the {@link SettingBox} represent.
         * @return a {@link Class} denoting the type of data represented by the combobox items.
         */
        protected abstract Class<T> getType ();
    }

    /**
     * A {@link SettingBox} which uses {@link DefaultOption} data for item values.
     */
    public static class DropDown extends SettingBox<DefaultOption> {

        /**
         * Create a new {@link DropDown}.
         * @param data an array of {@link DefaultOption} objects. All must be of the same type and
         * all must be non-null but neither constraint is actively enforced here.
         * @param bundle the {@link Class} used to denote the resource bundle
         * in the {@link BundleKey} implementation of combobox items.
         * @param opt the {@link Class} used to denote
         * {@link BundleKey#type() type} in the {@link BundleKey} implementation of
         * combobox items.
         */
        public DropDown (DefaultOption[] data, Class bundle, Class opt) {
            super(new DropDownModel(data, bundle, opt));
        }

        @Override
        protected Class<DefaultOption> getType () {
            return DefaultOption.class;
        }

        @Override
        public SettingModel<DefaultOption> getModel () {
            return super.getModel();
        }
    }

    /**
     * A {@link SettingModel} which uses {@link DefaultOption} data for item values.
     */
    public static class DropDownModel extends SettingModel<DefaultOption> {

        private final Class bundleClass;
        private final Class optionClass;

        /**
         * Create a new {@link DropDownModel}.
         * @param opts an array of {@link DefaultOption} objects. All must be of the same type and
         * all must be non-null but neither constraint is actively enforced here.
         * @param bundleClass the {@link Class} used to denote the resource bundle
         * in the {@link BundleKey} implementation of combobox items.
         * @param optionClass the {@link Class} used to denote
         * {@link BundleKey#type() type} in the {@link BundleKey} implementation of
         * combobox items.
         */
        public DropDownModel (DefaultOption[] opts, Class bundleClass,
                              Class optionClass) {
            super(opts);
            this.bundleClass = bundleClass;
            this.optionClass = optionClass;
        }

        @Override
        protected DefaultOption defaultOption (DefaultOption opt) {
            return opt.defaultOption();
        }

        @Override
        protected SettingOption<DefaultOption> getOption (DefaultOption value) {
            return new SettingOption<DefaultOption>(value) {

                @Override
                protected Class<?> bundleClass () {
                    return bundleClass;
                }

                @Override
                protected String nameOf (DefaultOption value) {
                    return value.name();
                }

                @Override
                protected Class<?> optionClass () {
                    return optionClass;
                }
            };
        }
    }
}
