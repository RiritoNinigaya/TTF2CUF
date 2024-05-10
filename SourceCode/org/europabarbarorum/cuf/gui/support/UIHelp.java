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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * This class provides a delegate {@link AbstractAction} which is used to route many
 * {@link ActionEvent} signals to their target {@link Command} objects rather than providing
 * individual {@link ActionListener} objects to handle each signal.
 * <p>
 * Subclasses must implement {@link #find(java.lang.String) } to retrieve {@link Command} objects
 * for an action (command) name.
 * @param <T> the type of context object in which actions occur. Typically this type corresponds to the
 * UI form/component that uses an {@link UIHelp} as instance field.
 * @param <S> type of object state tracked by the {@link UIHelp}.
 * @author Johan Ouwerkerk
 */
public abstract class UIHelp<T, S> extends AbstractAction {

    private final T context;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    /**
     * Name of the status property.
     */
    public static final String PROP_STATUS = UIHelp.class.getCanonicalName();
    private S old;

    /**
     * Update any attached GUI component with new enabled state. This method will not 
     * do anything if the given state object is already equal to the current {@link #status() status}.
     * @param status object containing the new state of this {@link UIHelp}. Used when 
     * determining whether attached UI components should be enabled.
     */
    public void update (S status) {
        support.firePropertyChange(PROP_STATUS, old, status);
        old = status;
    }

    /**
     * Force all enabled state for any attached UI component to be re-evaluated.
     * @param status object containing the new state of this {@link UIHelp}. Used when
     * determining whether attached UI components should be enabled.
     */
    public void ensureUpdate (S status) {
        old = null;
        update(status);
    }

    /**
     * Create a {@link UIHelp}.
     * @param context the GUI context on which {@link Command} objects
     * managed by the {@link UIHelp} operate.
     */
    public UIHelp (T context) {
        this.context = context;
    }

    /**
     * The implementation of an action. This interface main purpose is to be able to
     * use actions that are somewhat de-coupled from the specific GUI in which they reside.
     * <p>
     * Note that this interface uses {@link BundleKey#getText() } to provide key strokes (hotkeys)
     * for the action.
     * @param <T> the type of GUI context object on which the command operates.
     * @param <S> the type of state object used to determine whether or not associated UI components are enabled.
     */
    public interface Command<T, S> extends BundleKey {

        /**
         * Performs an action on the context GUI.
         * @param context the context GUI which the action manipulates.
         */
        public void actionPerformed (T context);

        /**
         * Map a status updated to a boolean enabled code.  The result determines whether or not
         * UI components associated with this {@link Command} will be enabled (active).
         * @param status the state to map
         * @param context the context to which the state refers.
         * @return true if UI component associated with this {@link Command} should be enabled,
         * false if they should be disabled.
         */
        public boolean enabled (S status, T context);
    }

    /**
     * A {@link JDialog} which adds support for closing the window as a button action.
     */
    public static class Dialog extends JDialog {

        /**
         * Create a {@link Dialog}.
         * @param parent parent {@link java.awt.Window}.
         * @param modal whether or not the dialog is modal (blocks underlying windows).
         */
        public Dialog (java.awt.Window parent, boolean modal) {
            super(parent,
                  modal ? Dialog.DEFAULT_MODALITY_TYPE : Dialog.ModalityType.MODELESS);
            this.setLocationRelativeTo(parent);
        }
        /**
         * A {@link ActionListener} that sends {@link WindowEvent#WINDOW_CLOSING} signals to
         * any window listeners registered on this window in response to an {@link ActionEvent}.
         */
        protected final ActionListener closeListener = closeActionListener(this);

        /**
         * Close this {@link Dialog}.
         * This method sends a {@link WindowEvent#WINDOW_CLOSING} signal to 
         * any window listeners registered on this window.
         */
        protected void close () {
            this.closeListener.actionPerformed(null);
        }
    }

    /**
     * A {@link JFrame} which adds support for closing the window as a button action.
     */
    public static class Frame extends JFrame {

        /**
         * Default no-arg constructor. The created {@link Frame} is positioned according to
         * platform defaults.
         */
        public Frame () {
            this.setLocationByPlatform(true);
        }

        /**
         * Create a {@link Frame}.
         * @param parent the parent {@link java.awt.Frame} on top of which the new {@link Frame}
         * is to be drawn.
         */
        public Frame (java.awt.Frame parent) {
            this.setLocationRelativeTo(parent);
        }
        /**
         * A {@link ActionListener} that sends {@link WindowEvent#WINDOW_CLOSING} signals to
         * any window listeners registered on this window in response to an {@link ActionEvent}.
         */
        protected final ActionListener closeListener = closeActionListener(this);

        /**
         * Close this {@link Frame}.
         * This method sends a {@link WindowEvent#WINDOW_CLOSING} signal to
         * any window listeners registered on this window.
         */
        protected void close () {
            this.closeListener.actionPerformed(null);
        }
    }

    /**
     * Get an {@link ActionListener} for closing windows on an {@link ActionEvent}.
     * @param w the {@link Window} to send signals to (close).
     * @return a {@link ActionListener} that sends {@link WindowEvent#WINDOW_CLOSING} signals to
     * any window listeners registered on this window in response to an {@link ActionEvent}
     */
    public static ActionListener closeActionListener (final Window w) {
        return new ActionListener() {

            @Override
            public void actionPerformed (ActionEvent e) {
                w.dispatchEvent(new WindowEvent(w,
                                                WindowEvent.WINDOW_CLOSING));
            }
        };
    }

    /**
     * Install a {@link Command} on a {@link JMenuItem}.
     * This method binds {@link Command#name() } as action command on the given
     * {@link JMenuItem} to this {@link UIHelp} action. 
     * @param item the {@link JMenuItem} to install the action on.
     * @param action the {@link Command} object to use.
     * @param addKeyStroke whether or not to register a {@link KeyStroke} if the given
     * {@link Command} provides a usable one. If true it is registered under the
     * same {@link Command#name()} action key as the main action.
     */
    public void bind (JMenuItem item, Command<T, S> action, boolean addKeyStroke) {
        bindToMe(item, action);
        if (addKeyStroke) {
            KeyStroke stroke = getKeyStroke(action);
            if (stroke != null) {
                IOHelp.Log.Config.log(UIHelp.class,
                                      Messages.DebugKeyStrokes,
                                      stroke,
                                      action.name(),
                                      item.getName());
                item.setAccelerator(stroke);
            }
        }
    }

    /**
     * Install a {@link Command} on a {@link JMenuItem}.
     * This method binds {@link Command#name() } as action command on the given
     * {@link JMenuItem} to this {@link UIHelp} action. If the given
     * {@link Command} provides a usable {@link KeyStroke} then it is registered with the
     * same {@link Command#name()} action key.
     * @param item the {@link JMenuItem} to install the action on.
     * @param action the {@link Command} object to use.
     */
    public void bind (JMenuItem item, Command<T, S> action) {
        this.bind(item, action, true);
    }

    private void bindToMe (AbstractButton btn, Command<T, S> action) {
        btn.addActionListener(this);
        btn.setActionCommand(action.name());
        support.addPropertyChangeListener(new Wire(btn));
    }

    private class Wire implements PropertyChangeListener {

        private final AbstractButton btn;

        private Wire (AbstractButton btn) {
            this.btn = btn;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void propertyChange (PropertyChangeEvent evt) {
            if (evt != null && evt.getPropertyName().equals(PROP_STATUS)) {
                Command<T, S> c = find(btn.getActionCommand());
                if (c != null) {
                    btn.setEnabled(c.enabled((S) evt.getNewValue(), context));
                }
            }
        }

        @Override
        public boolean equals (Object obj) {
            // reduces to: obj==null || btn.equals(obj.btn)
            // but also safe if Object is instance of btn.
            return obj != null && obj.equals(btn);
        }

        @Override
        public int hashCode () {
            return btn.hashCode();
        }
    }

    /**
     * Install a {@link Command} on a {@link JButton}.
     * This method binds {@link Command#name() } as action command on the given
     * {@link JButton} to this {@link UIHelp} action. If the given
     * {@link Command} provides a usable {@link KeyStroke} then it is registered with the
     * same {@link Command#name()} action key.
     * @param button the {@link JButton} to install the action on.
     * @param action the {@link Command} object to use.
     */
    public void bind (JButton button, Command<T, S> action) {
        bindToMe(button, action);
    }

    /**
     * Lookup a {@link KeyStroke} for a {@link Command}.
     * @param action the {@link Command} to query for a key stroke.
     * @return a {@link KeyStroke} represented by the given {@link Command} or
     * null if {@link Command#getText() } returns no usable value.
     */
    public KeyStroke getKeyStroke (Command action) {
        String stroke = action.getText();
        return stroke == null || stroke.equals("") ? null : KeyStroke.
                getKeyStroke(stroke);
    }

    /**
     * Find the {@link Command} associated with the given action name.
     * @param name the name of the {@link Command} to find.
     * @return the {@link Command} which corresponds to the given name or null
     * if not found.
     */
    public abstract Command<T, S> find (String name);

    /**
     * Performs a {@link Command} action.
     * This method looks up the {@link Command} associated with the value of
     * {@link ActionEvent#getActionCommand() } for the given event and runs it.
     * @param e the {@link ActionEvent} describing the action to perform.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed (ActionEvent e) {
        currentEvent = e;
        Command<T, S> c = find(e.getActionCommand());
        if (c != null && c.enabled(old,context)) {
            c.actionPerformed(context);
        }
        currentEvent = null;
    }
    private ActionEvent currentEvent;
    /**
     * Return the {@link ActionEvent} currently being {@link #actionPerformed(java.awt.event.ActionEvent) processed}
     * by this {@link UIHelp}. This method is public for the benefit of {@link Command commands} which might need
     * access to the original event.
     * @return the {@link ActionEvent} currently being processed, or null if it no event is being
     * handled.
     */
    public ActionEvent currentEvent() {
        return currentEvent;
    }

    /**
     * Get the current {@link UIHelp} state.
     * @return the current status.
     */
    public S status () {
        return old;
    }
}
