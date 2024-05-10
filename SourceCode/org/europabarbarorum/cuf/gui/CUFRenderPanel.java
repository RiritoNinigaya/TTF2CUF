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

import org.europabarbarorum.cuf.gui.support.UpdatableModel;
import org.europabarbarorum.cuf.support.ProgressMonitor.Progress;
import org.europabarbarorum.cuf.support.ProgressMonitor;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.SwingWorker;
import org.europabarbarorum.cuf.font.CUFFont;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.impl.FormatConstants.CUFProperty;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * Provides a panel to render text on with caching of the result.
 * @author Johan Ouwerkerk
 */
public class CUFRenderPanel extends JPanel implements UpdatableModel<CUFSource> {

    /**
     * Create a new {@link CUFRenderPanel}.
     */
    public CUFRenderPanel () {
        setOpaque(false); // fix for scrolling artefacts (image is transparent).
        setEnabled();
    }
    private ProgressMonitor monitor;

    /**
     * Configure this {@link CUFRenderPanel} with a {@link ProgressMonitor}.
     * This monitor is signalled whenever the panel starts and stopd rendering text, so that it
     * can inform the user a render is in progress (and that the blank panel is not some weird error).
     * @param monitor the object that is to receive status updates.
     */
    public void setMonitor (ProgressMonitor monitor) {
        this.monitor = monitor;
    }
    /**
     * The {@link CUFSource} to preview.
     */
    protected CUFSource cufFont = null;
    private static int xOffset = 5;
    private static int yOffset = 5;

    /**
     * Helper to factor out common update code.
     * This method schedules {@link #pic} to be rerendered,
     * and marks this {@link CUFRenderPanel} as “in-progress”.
     */
    public void update () {
        this.setEnabled();
        if (isEnabled()) {
            rendered = false;
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
                if (monitor != null) {
                    monitor.done();
                }
            }
            pic = null;
            worker = renderService.submit(new RenderWorker());
        }
    }

    /**
     * Set the {@link CUFSource} to preview.
     * This method does not cause the render to be updated.
     * @param source the font to preview.
     * @see #update() 
     */
    @Override
    public void updateModel (CUFSource source) {
        boolean needsUpdate = source == null
                || cufFont == null
                || !cufFont.equals(source);
        if (needsUpdate) {
            this.cufFont = source;
        }
    }
    /**
     * Status flag to track whether cache is current or stale.
     */
    private boolean rendered = false;
    /**
     * Cached render of the preview text.
     */
    private BufferedImage pic;
    /**
     * Preview text.
     */
    protected String renderText;

    /**
     * Get the text used for previewing the {@link #cufFont}.
     * @return the value of {@link #renderText}
     */
    public String getRenderText () {
        return renderText;
    }

    /**
     * Set the value of renderText.
     * This method does not cause the render to be updated.
     * @param renderText new value of renderText
     * @see #update()
     */
    public void setRenderText (String renderText) {
        this.renderText = renderText;
    }

    /**
     * Shorthand method that evaluates the various status flags to determine its argument to
     * {@link #setEnabled(boolean) }.
     */
    private void setEnabled () {
        this.setEnabled(cufFont != null
                && cufFont.isPrepared()
                && renderText != null);
    }

    /**
     * Custom {@link JPanel#paintComponent(java.awt.Graphics) } implementation.
     * This method first calls the superclass implementation; and then
     * if {@link #isEnabled() } returns true and the render is available draws the
     * cached image.
     * @param g the {@link Graphics} to draw on.
     */
    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent(g); // clear out old junk
        if (isEnabled()) {
            if (rendered) { // wait until the render is available...
                drawRender(g);
            }
        }
    }
    /**
     * {@link Future} that represents the task of rendering. This is tracked for the benefit of
     * being able to call {@link Future#cancel(boolean) cancel} on it, and replace it with a different one.
     * @see #update()
     * @see #finish()
     */
    private Future worker;

    private class RenderWorker extends SwingWorker<BufferedImage, Progress> {

        @Override
        protected BufferedImage doInBackground () throws Exception {

            CUFFont font = new CUFFont(cufFont) {

                @Override
                protected void signalProgress (BundleKey message, Object... args) {
                    publish(new Progress(Messages.RenderingJobTitle.getText(),
                                         message.format(args)));
                }

                @Override
                protected void signalProgress (int value, int maximum,
                                               BundleKey message, Object... args) {
                    publish(new Progress(Messages.RenderingJobTitle.getText(),
                                         message.format(value,
                                                        maximum)));
                }
            };
            publish(new Progress(Messages.RenderingJobTitle.getText(),
                                 Messages.RenderingJobTitle.getText()));
            return font.render(renderText, getForeground().getRGB() & 0x00FFFFFF);
        }

        @Override
        protected void process (java.util.List<Progress> chunks) {
            if (!started) {
                started = true;
                signalStart(chunks.get(0));
            }
            Progress last = chunks.get(chunks.size() - 1);
            if (monitor != null && chunks.size() > 1) {
                monitor.setText(last);
            }
        }
        private boolean started = false;

        private void signalStart (Progress p) {
            if (monitor != null) {
                monitor.start(p);
            }
            setToolTipText(String.format(Messages.RenderingToolTip.getText(),
                                         renderText));
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        @Override
        protected void done () {
            try {
                pic = get();
            }
            catch (ExecutionException except) {
                pic = null;
                Throwable _err = except.getCause();
                IOHelp.handleExceptions(RenderWorker.class, "done", _err, _err.
                        getLocalizedMessage());
            }
            catch (InterruptedException ignored) {
                pic = null;
            }
            finish();
        }
    }

    /**
     * Cause this {@link CUFRenderPanel} to switch to its “done” GUI state.
     * In particular the markers that this panel is intermediate (cursor/tooltip) are
     * reversed to defaults/finished status markers; and panel dimension state is re-adjusted to match
     * the current {@link #pic render}.
     */
    protected synchronized void finish () {
        worker = null;
        if (pic != null) {
            // set preferred size for the benefit of scrollbars around the corresponding Viewport in the ScrollPane
            setPreferredSize(new Dimension(pic.getWidth() + xOffset * 2, pic.
                    getHeight() + yOffset * 2));
        }
        // set rendered to true before revalidating because it may trigger a repaint == infinite loop?
        rendered = true;

        // revalidate: trigger an update of the ScrollPane (show/hide scrollbars)
        // may not be necessary, but ScrollPane tutorials use it for updates??
        revalidate();
        // repaint the view port, necessary if revalidation does not trigger it
        repaint();

        setToolTipText(Messages.RenderedToolTip.getText());
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        if (monitor != null) {
            monitor.done();
        }
    }
    private static ExecutorService renderService = Executors.
            newSingleThreadExecutor();

    private void drawRender (Graphics g) {
        if (pic != null) {
            int posX = xOffset();
            int posY = yOffset();
            int width = this.getWidth(), height = this.getHeight(), w = width - xOffset, h = height - yOffset;

            g.drawImage(pic,
                        posX > 0 ? posX + xOffset : xOffset,
                        posY > 0 ? posY + yOffset : yOffset,
                        null);
            Color k = this.getForeground();
            g.drawLine(0, yOffset, width, yOffset);
            g.drawLine(xOffset, 0, xOffset, height);
            g.drawLine(0, h, width, h);
            g.drawLine(w, 0, w, height);

            int v = (this.getForeground().getRGB() + this.getBackground().getRGB()) / 2;
            g.setColor(new Color(v));
            posY = yOffset + Integer.signum(posY) * posY;
            posX = xOffset + Integer.signum(posX) * posX;
            if (posY > yOffset) {
                g.drawLine(0, posY, width, posY);
            }
            if (posX > xOffset) {
                g.drawLine(posX, 0, posX, height);
            }
            g.setColor(k);
        }
    }

    private Integer xOffset () {
        return prop(CUFProperty.LayoutXOffset);
    }

    private int prop (CUFProperty prop) {
        if (cufFont == null) {
            return 0;
        }
        int r = cufFont.isAvailable(prop) ? cufFont.getCUFProperties(prop) : 0;
        return r;
    }

    private Integer yOffset () {
        return prop(CUFProperty.LayoutYOffset);
    }
}
