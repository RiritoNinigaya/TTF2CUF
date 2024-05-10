/*
 *  
 *  Copyright (C) 2011 The Europa Barbarorum Team
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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 * This class provides a fix/workaround for scrolling artefacts.
 * These artefacts normally occur because the RepaintManager doesn't repaint the 
 * components often enough when they are scrolled (so the image accumulates noise).
 * The fix entails installing an {@link AdjustmentListener} object and 
 * requesting a repaint on every {@link AdjustmentEvent} received. 
 * @author Johan Ouwerkerk
 */
public class ScrollingSupport extends JScrollPane {

    /**
     * Create a new {@link JScrollPane} with improved scrolling support.
     */
    public ScrollingSupport () {
        init();
    }

    /**
     * Apply the same fix/workaround of {@link ScrollingSupport} to an “external” 
     * {@link JScrollPane}. This method is intended only for those instance of 
     * {@link JScrollPane} which are created by external code that we cannot fix 
     * properly ourselves. Such “external” objects may however be patched up a bit 
     * by walking the component tree, finding the instances of {@link JScrollPane} and 
     * calling this method for each of them.
     * @param pane a {@link JScrollPane}. This method does nothing if the pane is 
     * an instance of {@link ScrollingSupport} or null.
     */
    public static void fix (final JScrollPane pane) {
        if (!(pane instanceof ScrollingSupport) && pane != null) {
            final AdjustmentListener fix = new AdjustmentListener() {

                @Override
                public void adjustmentValueChanged (AdjustmentEvent e) {
                    Component toFix = pane.getViewport().getView();
                    if (toFix != null) {
                        toFix.repaint();
                    }
                }
            };
            pane.getHorizontalScrollBar().addAdjustmentListener(fix);
            pane.getVerticalScrollBar().addAdjustmentListener(fix);
        }
    }

    private void init () {
        fix(getHorizontalScrollBar());
        fix(getVerticalScrollBar());
    }

    private void unfix (JScrollBar old) {
        if (old != null) {
            old.removeAdjustmentListener(fix);
        }
    }

    private void fix (JScrollBar bar) {
        if (bar != null) {
            bar.addAdjustmentListener(fix);
        }
    }

    @Override
    public void setVerticalScrollBar (JScrollBar verticalScrollBar) {
        unfix(getVerticalScrollBar());
        fix(verticalScrollBar);
        super.setVerticalScrollBar(verticalScrollBar);
    }

    @Override
    public void setHorizontalScrollBar (JScrollBar horizontalScrollBar) {
        unfix(getHorizontalScrollBar());
        fix(horizontalScrollBar);
        super.setHorizontalScrollBar(horizontalScrollBar);
    }
    private final AdjustmentListener fix = new AdjustmentListener() {

        @Override
        public void adjustmentValueChanged (AdjustmentEvent e) {
            Component c = getViewport().getView();
            if (c != null) {
                c.repaint();
            }
        }
    };
}
