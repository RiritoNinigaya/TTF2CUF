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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.gui.support.ProgressMonitorImpl;
import org.europabarbarorum.cuf.gui.support.UpdatableModel.UpdatableFont;
import org.europabarbarorum.cuf.gui.support.UpdatableModel.UpdatablePreview;
import org.europabarbarorum.cuf.support.Preview;

/**
 * A frame to wrap a stand-alone {@link PreviewChrome} instance.
 * @author Johan Ouwerkerk
 */
public class PreviewWindow extends JFrame implements
        UpdatableFont,
        UpdatablePreview {

    /** Creates new form PreviewWindow */
    public PreviewWindow () {
        initComponents();
        preview.setMonitor(new ProgressMonitorImpl(statusLabel, progress));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        preview = new PreviewChrome();
        progress = new JProgressBar();
        statusLabel = new JLabel();
        JSeparator jSeparator1 = new JSeparator();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        progress.setEnabled(false);
        progress.setString("");
        progress.setStringPainted(true);

        statusLabel.setLabelFor(progress);
        statusLabel.setEnabled(false);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel, GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addGap(451, 451, 451)
                .addComponent(progress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jSeparator1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
            .addComponent(preview, GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(preview, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(progress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusLabel, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private PreviewChrome preview;
    protected JProgressBar progress;
    private JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

   
    @Override
    public void updateModel (CUFSource source) {
        preview.updateModel(source);
    }

    @Override
    public void updateModel(Preview preview) {
        this.preview.updateModel(preview);
    }

}