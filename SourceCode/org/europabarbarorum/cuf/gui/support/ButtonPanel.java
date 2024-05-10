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

import javax.swing.AbstractButton;

/**
 * A component which displays a single {@link AbstractButton button} from a list of
 * given items.
 * @author Johan Ouwerkerk
 */
public class ButtonPanel extends ReplacementPanel<AbstractButton> {

    private final AbstractButton[] buttons;
    private int current = NO_SELECT;
    /**
     * Constant used to disable {@link #show(int) showing} the default item
     * in the {@link ButtonPanel} constructor.
     */
    public static final int NO_SELECT = -1;

    /**
     * Create a new {@link ButtonPanel}.
     * @param buttons the array of {@link AbstractButton buttons} to present.
     * @param show the index in the given array
     * of the {@link AbstractButton button} to {@link #show(int) show}, or
     * {@link #NO_SELECT} to disable default selection.
     */
    public ButtonPanel (AbstractButton[] buttons, int show /*, LayoutOption opt*/) {
        this.buttons = buttons;
        if (buttons != null && show != NO_SELECT) {
            show(show);
        }
    }

    /**
     * Dummy constructor so this class is a valid Java Bean.
     */
    public ButtonPanel () {
        this(null, NO_SELECT);
    }

    /**
     * Get the number of {@link AbstractButton buttons} covered by this {@link ButtonPanel}.
     * @return the number of {@link AbstractButton buttons} available to this component.
     */
    public int count () {
        return buttons == null ? 0 : buttons.length;
    }

    /**
     * Select and show a specific {@link AbstractButton button} to show from the
     * the items covered by this {@link ButtonPanel}.
     * @param i the index of the {@link AbstractButton button} to show.
     * @return the {@link AbstractButton button} which was previously showing, or null
     * if no {@link AbstractButton button} had been selected yet.
     * @see ReplacementPanel#swap(javax.swing.JComponent) 
     */
    public final AbstractButton show (int i) {
        if (i < 0 || count() <= i) {
            throw new IllegalArgumentException("" + i);
        }
        current = i;
        return super.swap(buttons[i]);
    }

    /**
     * Get the index of the currently selected {@link AbstractButton button}.
     * @return the index of the currently selected item, or {@link #NO_SELECT} if 
     * no item has been selected yet.
     */
    public int current () {
        return current;
    }

    /**
     * Get a specific {@link AbstractButton button} from the
     * the items covered by this {@link ButtonPanel}.
     * @param i the index of the {@link AbstractButton button} to show.
     * @return the {@link AbstractButton button} with the given index.
     */
    public final AbstractButton get (int i) {
        if (i < 0 || count() <= i) {
            throw new IllegalArgumentException("" + i);
        }
        return buttons[i];
    }
}
