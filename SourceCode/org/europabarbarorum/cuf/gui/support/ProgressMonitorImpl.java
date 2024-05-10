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

import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.europabarbarorum.cuf.support.ProgressMonitor;

/**
 * A {@link ProgressMonitor} which updates a {@link JLabel} and
 * a {@link JProgressBar}.
 * @author Johan Ouwerkerk
 */
public class ProgressMonitorImpl implements ProgressMonitor {

    private final JProgressBar progress;
    private final JLabel statusLabel;
    /**
     * Create a new {@link ProgressMonitorImpl}.
     * @param text the {@link JLabel} used to show messages.
     * @param bar the {@link JProgressBar} used to indicate amount of progress.
     */
    public ProgressMonitorImpl(JLabel text, JProgressBar bar) {
        this.statusLabel=text;
        this.progress=bar;
    }

    @Override
    public void start (Progress progress) {
        adjustSourceCount(true);
        setText(progress);
    }
    private int jobCount = 0;

    private synchronized void adjustSourceCount (boolean up) {
        if (up) {
            jobCount++;
        }
        else {
            jobCount--;
        }

        // disable status components, marks them for reset
        if (jobCount <= 0) {
            progress.setIndeterminate(false);
            jobCount = 0;
            progress.setValue(0);
            toggleStatuses(false);
        }
    }

    private void toggleStatuses (boolean b) {
        statusLabel.setEnabled(b);
        progress.setEnabled(b);
    }

    private void _setText (String msg, boolean which, boolean determinate, int val, int max) {
        String status = which ? statusLabel.getText() : progress.getString();

        if (status == null || !status.equals(msg)) {
            if (which) {
                statusLabel.setText(msg);
            }
            else {
                progress.setString(msg);
                if (determinate) {
                    progress.setIndeterminate(false);
                    progress.setMaximum(max);
                    progress.setValue(val);
                }
                else {
                    progress.setIndeterminate(true);
                }
            }
        }
    }

    @Override
    public void setText (Progress progress) {
        toggleStatuses(true); // might have been disabled by a job that completed previously
        _setText(progress.job, false, progress.isDeterminate, progress.val, progress.max);
        _setText(progress.message, true, progress.isDeterminate, progress.val, progress.max);
    }

    @Override
    public void done () {
        adjustSourceCount(false);

        final String text = statusLabel.getText();
        final String string = progress.getString();
        // reset status components after timeout if they are still disabled (if no job is in progress)
        uiReset.schedule(new TimerTask() {

            @Override
            public void run () {
                if (statusLabel.getText().equals(text)) {
                    statusLabel.setText("");
                }
                if (progress.getString().equals(string)) {
                    progress.setString("");
                }
            }
        }, 1000);
    }

    private Timer uiReset = new Timer(false);
}
