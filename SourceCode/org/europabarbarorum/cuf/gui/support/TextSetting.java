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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Johan Ouwerkerk
 */
public interface TextSetting {

    /**
     * Get the raw text in the UI component.
     * @return the text displayed in the GUI for this control.
     * @see #getValue() 
     */
    String getText ();

    /**
     * Causes the given label to be displayed by this {@link SettingField}.
     * <p>
     * Normally, this method should not be used directly.
     * Instead {@link #reset(java.lang.String) } and
     * {@link #setValue(java.lang.String) } are preferred.
     * <p>
     * Note that
     * the first call to this method after construction sets a “hint”, rather than a value and it
     * must not be null: this behaviour helps with GUI builders.
     * @param text label to display.
     * @see #getText()
     * @see #reset(java.lang.String)
     */
    void setText (String text);

    /**
     * Reset the displayed text to a pre-defined value. This method is used to set
     * error messages in case of wrong input.
     * @param text the message to display.
     * @see #setText(java.lang.String)
     */
    void reset (String text);

    /**
     * Causes this GUI component to lose focus and evaluate the value.
     * @see #getValue()
     */
    void drop ();

    /**
     * Return the last saved value. Note that this method only works properly if
     * {@link #drop() } has been called first.
     * @return a value entered by the user or null if no such value was entered or it is not
     * available yet (because the text field still has focus).
     * @see #setValue(java.lang.String)
     * @see #setText(java.lang.String)
     */
    public String getValue ();

    /**
     * Pre-populate this text field with a particular value.
     * Note that this method should <em>not</em> be used to set a hint.
     * @param value the value to pre-populate this field with.
     * @see #getValue()
     * @see #getText() 
     */
    public void setValue (String value);

    /**
     * A helper object to delegate a lot of implementation details for a 
     * {@link TextSetting} object to. Also implements {@link FocusListener} to auto-save 
     * values (and optionally reset labels) as focus is lost/gained.
     */
    static class SettingHelper implements FocusListener {

        private static final MouseAdapter focusHelper = new MouseAdapter() {

            @Override
            public void mouseClicked (MouseEvent e) {
                ((JComponent) e.getSource()).requestFocus();
            }
        };
        private final JTextComponent f;

        /**
         * Create a new {@link SettingHelper}.
         * @param c the {@link JTextComponent} that delegates the task of
         * handling implementation details to this {@link SettingHelper}.
         */
        public SettingHelper (JTextComponent c) {
            this.f = c;
            this.value = null;
            this.help = null;
            c.addFocusListener(this);
            c.addMouseListener(focusHelper);
            TextActions.setPopupMenu(c,null);
        }
        private String value;
        private String help;

        /**
         * Return the last saved value. Note that this method only works properly if
         * {@link #focusLost(java.awt.event.FocusEvent) } has been called first; using
         * e.g.: {@code focusLost(null);}.
         * @return a value entered by the user or null if no such value was entered or it is not
         * available yet (because the text field still has focus).
         */
        public String getValue () {
            return value;
        }

        /**
         * Pre-populate this text field with a particular value.
         * Note that this method should <em>not</em> be used to set a hint.
         * @param value the value to pre-populate this field with.
         */
        public void setValue (String value) {
            this.value = value;
        }

        /**
         * Implements reversal of the displayed text to the previously set hint, if
         * no input has been entered by the user. If the user did input text, this
         * method ensures it can be retrieved by {@link #getValue() }.
         * @param e a {@link FocusEvent} which is otherwise ignored.
         */
        @Override
        public void focusLost (FocusEvent e) {
            if (change) {
                value = f.getText();
            }
            if (value == null || value.equals("")) {
                f.setText(help);
                value = null;
            }
            change = false;
        }

        /**
         * Implementation for {@link TextSetting#drop() }.
         */
        public void drop () {
            focusLost(new FocusEvent((Component) f, FocusEvent.FOCUS_LOST, true));
        }

        /**
         * Store the value for the {@link TextSetting}.
         * If the current help string/label is null (e.g. after {@link #reset()}) the
         * text is assumed to refer to the help string/label.
         * This is useful behaviour for working with GUI builders.
         * @param t the text to store.
         */
        public void set (String t) {
            if (help == null) {
                this.help = t;
            }
            else {
                if (value == null) {
                    this.value = t;
                }
            }
        }
        private boolean change = false;

        /**
         * Reset the help string/label.
         */
        public void reset () {
            this.help = null;
        }

        /**
         * Implements focus-gained effect: error/hint messages are removed.
         * In case there was previous (wrong) input it is re-displayed,
         * so the user can fix typos and the like.
         * @param e a {@link FocusEvent} which is otherwise ignored.
         */
        @Override
        public void focusGained (FocusEvent e) {
            change = true;
            if (value == null) {
                f.setText("");
                return;
            }
            if (!value.equals(f.getText())) {
                f.setText(value);
            }
        }
    }
}
