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
package org.europabarbarorum.cuf.support;

/**
 * Interface describing callbacks for signalling progress updates.
 *
 * @author Johan Ouwerkerk
 */
public interface ProgressMonitor {

    /**
     * Signal that a job has completed and will no longer produce status updates.
     */
    void done ();

    /**
     * Singal that a job prodocued some progress.
     * @param progress a {@link Progress} object that provides details.
     */
    void setText (Progress progress);

    /**
     * Signal that a job producing progress updates was started.
     * @param progress a {@link Progress} object that provides details.
     */
    void start (Progress progress);

    /**
     * Class to encapsulate data for progress messages. This object is intended for use in conjunction with
     * e.g. {@link javax.swing.SwingWorker} objects.
     * @author Johan Ouwerkerk
     */
    public static class Progress {

        /**
         * A title for the running job such as “Rendering preview”.
         */
        public final String job;
        /**
         * A status message such as “Allocating image”.
         */
        public final String message;
        /**
         * Wether or not this message is “determinate”. If true, this means that {@link #val} and {@link #max}
         * provide further information that can be used; if false these values must be ignored.
         */
        public final boolean isDeterminate;
        /**
         * Status values. This is relative to a minimum value of 0.
         */
        public final int val;
        /**
         * Maximum status value (used for interpreting {@link #val}). This is relative to a minimum of 0.
         */
        public final int max;

        /**
         * Construct an “indeterminate” status message.
         * @param job the value for {@link #job} to use.
         * @param message the value for {@link #message} to use.
         */
        public Progress (String job, String message) {
            this.job = job;
            this.isDeterminate = false;
            this.message = message;
            this.val = (this.max = 0);
        }

        /**
         * Construct an “determinate” status message.
         * @param job the value for {@link #job} to use.
         * @param message the value for {@link #message} to use.
         * @param val the value for {@link #val} to use.
         * @param max the value for {@link #max} to use.
         */
        public Progress (String job, String message, int val, int max) {
            this.job = job;
            this.message = message;
            this.max = max;
            this.val = val;
            this.isDeterminate = true;
        }
    }
}
