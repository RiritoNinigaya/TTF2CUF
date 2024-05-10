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
package org.europabarbarorum.cuf.gui.fontwizard.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import org.europabarbarorum.cuf.gui.fontwizard.FontTreeNode;
import org.europabarbarorum.cuf.gui.fontwizard.Messages;
import org.europabarbarorum.cuf.gui.fontwizard.common.AbstractNameUI.Model;
import org.europabarbarorum.cuf.gui.support.ComponentState;
import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;
import org.europabarbarorum.cuf.gui.support.SettingField;

/**
 * A base class form elements used to enter names for {@link FontTreeNode} and equivalent items.
 * @author Johan Ouwerkerk
 */
public abstract class AbstractNameUI extends JPanel implements
        ComponentState<Model> {

    private final Set<String> taken;
    private final SettingField nameField;

    /**
     * Determine which names are already taken.
     * @param parent the context (parent) {@link FontTreeNode} to use for 
     * determining which names are taken.
     * @return an array of names which are already taken in the given context.
     */
    protected abstract String[] names (FontTreeNode parent);

    @SuppressWarnings("unchecked")
    private Set<String> nameSet (FontTreeNode parent, String name) {
        if (parent == null) {
            return Collections.EMPTY_SET;
        }
        String[] childs = names(parent);
        if (childs == null) {
            return Collections.EMPTY_SET;
        }

        HashSet<String> set = new HashSet<String>();
        for (String s : childs) {
            if (name == null || !name.equals(s)) {
                set.add(s);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Creates a new {@link AbstractNameUI}.
     * @param parent the parent {@link FontTreeNode} which determines what names
     * can not be chosen in this {@link AbstractNameUI}.
     * @param name the original name of the current node, or null if no name for the node
     * has been set yet.
     */
    public AbstractNameUI (FontTreeNode parent, String name) {
        this.taken = nameSet(parent, name);
        this.nameField = new SettingField();
    }

    /**
     * Access the underlying {@link SettingField}.
     * @return the underlying {@link SettingField}.
     */
    protected SettingField field () {
        return nameField;
    }

    /**
     * Validate user input. This method is responsible for checking that the 
     * entered text is a “valid” name. It does not need to ensure that the same name 
     * is still available.
     * @param input the name entered in the underlying {@link SettingField}.
     * @return true if the name is <em>invalid</em> and must be discarded, false if not.
     */
    protected abstract boolean discard (String input);

    /**
     * A {@link ComponentModel} for a {@link FontNameUI}.
     */
    public static class Model implements ComponentModel<AbstractNameUI> {

        private Model (String name) {
            this.value = name;
        }
        /**
         * The name set in the {@link FontNameUI} represented by this {@link Model}.
         */
        public String value;

        @Override
        public void populate (AbstractNameUI ui) {
            ui.nameField.setValue(value);
        }
    }

    @Override
    public Model createModel () {
        return new Model(nameField.getValue());
    }

    @Override
    public boolean checkUI () {
        nameField.drop();
        String v = this.nameField.getValue();
        if (v == null || discard(v)) {
            nameField.reset(Messages.InvalidFontNodeName.getText());
            return false;
        }
        if (taken.contains(v)) {
            nameField.reset(Messages.FontNodeNameTaken.getText());
            return false;
        }
        return true;
    }
}
