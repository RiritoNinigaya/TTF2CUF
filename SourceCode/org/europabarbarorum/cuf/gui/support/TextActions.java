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

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import org.europabarbarorum.cuf.gui.support.UIHelp.Command;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 *
 * @author Johan Ouwerkerk
 */
public enum TextActions implements Command<JTextComponent, Object> {

    /**
     * Cut action.
     */
    Cut {

        @Override
        protected String command () {
            return DefaultEditorKit.cutAction;
        }
    },
    /**
     * Copy action
     */
    Copy {

        @Override
        protected String command () {
            return DefaultEditorKit.copyAction;
        }

        @Override
        protected boolean type (JTextComponent c) {
            return true;
        }
    },
    /**
     * Paste action
     */
    Paste {

        @Override
        protected String command () {
            return DefaultEditorKit.pasteAction;
        }

        @Override
        protected boolean range (JTextComponent c) {
            return true;
        }
    };
    /**
     * A {@link BundleKey} which provides localised text for a context menu item.
     */
    protected final BundleKey data = key(this);

    private BundleKey key (final TextActions a) {
        return new BundleKey() {

            @Override
            public String getText () {
                return a.findText(this);
            }

            @Override
            public String format (Object... args) {
                return a.formatText(this, args);
            }

            @Override
            public String name () {
                return a.name() + ".text";
            }

            @Override
            public Class type () {
                return a.type();
            }
        };
    }

    private static UIHelp<JTextComponent, Object> defaultHelp (JTextComponent c) {
        return new UIHelp<JTextComponent, Object>(c) {

            @Override
            public Command<JTextComponent, Object> find (String name) {
                return TextActions.valueOf(name);
            }
        };
    }

    /**
     * Initialise the {@link JPopupMenu context menu} of the given {@link JTextComponent text component}
     * with applicable {@link TextActions}. If the given component does not yet have a
     * {@link JPopupMenu context menu}, it will be created.
     * @param c {@link JTextComponent text component} to install the given {@link TextActions} on.
     * @param help a {@link UIHelp} object to manage various action state & route action events.
     * This argument may be null, in which case a default {@link UIHelp} will be used.
     * <p>Note that any {@link UIHelp} object will do, as long as its context is an
     * instance of {@link JTextComponent}. I.e. the type of status object is not relevant here.</p>
     */
    public static void setPopupMenu (JTextComponent c,
                                     UIHelp<JTextComponent, Object> help) {
        JPopupMenu m = c.getComponentPopupMenu();
        final UIHelp<JTextComponent, Object> h = help == null ? defaultHelp(c) : help;
        if (m == null) {
            m = new JPopupMenu();
            m.addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible (PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible (PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled (PopupMenuEvent e) {
                }
            });
        }
        for (TextActions a : values()) {
            if (a.type(c)) {
                m.add(a.get(h));
            }
        }
        c.setComponentPopupMenu(m);
    }

    /**
     * Get a string to use for lookup of the actual action. This string corresponds
     * to an action defined in the action map of a text component.
     * @return name of the command string corresponding to the actual action
     * defined in {@link DefaultEditorKit}.
     */
    protected abstract String command ();

    private JMenuItem get (UIHelp<JTextComponent, Object> help) {
        JMenuItem i = new JMenuItem();
        help.bind(i, this, true);
        i.setText(data.getText());
        return i;
    }

    @Override
    public boolean enabled (Object status, JTextComponent context) {
        return type(context) && range(context);
    }

    /**
     * Check if a component is of the right type for this {@link TextActions action}.
     * @param context the component to check.
     * @return true if this action is useful/available/enabled for the component
     * false if not.
     */
    protected boolean type (JTextComponent context) {
        return context.isEditable();
    }

    /**
     * Check if a valid range has been selected in the component.
     * {@link TextActions Actions} which do not require any range should
     * override this method to always return <code>true</code>.
     * @param context the component to check.
     * @return true if at least a single character has been selected in
     * the component.
     */
    protected boolean range (JTextComponent context) {
        return context.getSelectionEnd() - context.getSelectionStart() > 0;
    }

    @Override
    public String getText () {
        return findText(this);
    }

    @Override
    public void actionPerformed (JTextComponent context) {
        ActionEvent a = new ActionEvent(context, ActionEvent.ACTION_PERFORMED,
                                        command());
        context.getActionMap().get(command()).actionPerformed(a);

    }

    private String findText (BundleKey k) {
        return ResourceHelp.getValue(k, TextActions.class);
    }

    private String formatText (BundleKey k, Object... args) {
        return ResourceHelp.formatValue(k, TextActions.class, args);
    }

    @Override
    public String format (Object... args) {
        return formatText(this, args);
    }

    @Override
    public Class type () {
        return TextActions.class;
    }
}
