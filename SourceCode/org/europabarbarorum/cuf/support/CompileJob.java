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

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import org.europabarbarorum.cuf.support.ProgressMonitor.Progress;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * Skeleton compilation task which takes care of exception handling, routing
 * progress updates, and threading. Subclasses are required to implement {@link #build()
 * } for the actual compilation logic. By default instances of this class are
 * completely silent: if you want to get status updates and the like you must
 * call {@link #listen(org.europabarbarorum.cuf.support.CompileJob.CompileListener)
 * }.
 *
 * @param <T> type of object to {@link #build() }
 * @author Johan Ouwerkerk
 */
public abstract class CompileJob<T> extends FutureTask<T> {

    /**
     * A listener interface to receive status updates on the progress of a {@link CompileJob}.
     */
    public static interface CompileListener {

        /**
         * Signals that compilation has failed due to some unexpected error.
         *
         * @param e the {@link Exception} which caused the compilation to be
         * aborted.
         */
        void fail (Exception e);

        /**
         * Update the outside world with a {@link Progress} message.
         *
         * @param status a {@link Progress} object that details the status of a {@link CompileJob}.
         */
        void status (Progress status);

        /**
         * Signals that compilation has succesfully finished.
         */
        void succeed ();

        /**
         * Signals that the compilation was aborted (canceled). This does not
         * refer to an unexpected error.
         *
         * @param e an {@link Exception} containing further details.
         */
        void abort (Exception e);
    }

    /**
     * Implementation of {@link CompileListener} that does nothing at all.
     */
    public static class SilentListener implements CompileListener {

        @Override
        public void fail (Exception e) {
        }

        @Override
        public void status (Progress status) {
        }

        @Override
        public void succeed () {
        }

        @Override
        public void abort (Exception e) {
        }
    }

    /**
     * Skeleton implementation of {@link CompileListener}. This implementation
     * treats cancellation the same as compile time errors. Subclasses are
     * required to implement
     * {@link CompileListener#status(org.europabarbarorum.cuf.support.ProgressMonitor.Progress)},
     * as well as {@link CompileListenerImpl#report(java.lang.Exception)}.
     */
    public static abstract class CompileListenerImpl implements CompileListener {

        @Override
        public void fail (Exception e) {
            report(e);
            Throwable err = e.getCause();
            while (err != null && err instanceof Exception) {
                e = (Exception) err;
                report(e);
                err = e.getCause();
            }
        }

        @Override
        public void abort (Exception e) {
            fail(e);
        }

        /**
         * Report an {@link Exception} to the user. This method is used by {@link #fail(java.lang.Exception)}
         * to the error that caused a {@link CompileJob} to fail.
         *
         * @param e the {@link Exception} to report.
         */
        protected abstract void report (Exception e);

        @Override
        public void succeed () {
        }
    }

    /**
     * Creates a new {@link CompileJob}.
     *
     * @param jobTitle title of the job to use; used in constructing {@link Progress}
     * status updates.
     */
    protected CompileJob (String jobTitle) {
        this(jobTitle, new Job<T>());
    }

    private CompileJob (String title, Job<T> job) {
        super(job);
        this.jobTitle = title;
        listen(null);
        init(job);
    }

    private void init (Job<T> job) {
        job.job = this;
    }

    private static class Job<R> implements Callable<R> {

        private CompileJob<R> job;

        @Override
        public R call () throws Exception {
            return job.doInBackground();
        }
    }

    /**
     * Run the compilation. This method blocks until compilation is terminated
     * (either because the compiler is finished, or because an error occurs) and
     * attending events have run on the EDT.
     *
     * @param threadPool an {@link ExecutorService} which provides the
     * concurrency plumbing.
     * @throws Exception if the EDT is interrupted during {@link #done() }.
     */
    public void invokeAndWait (ExecutorService threadPool) throws Exception {
        threadPool.submit(this).get();
        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run () {
                done();
            }
        });
    }

    /**
     * Check if the {@link CompileJob#doInBackground() } method has been called
     * yet.
     *
     * @return true if compilation has begun, false if not.
     */
    public final boolean hasStarted () {
        return hasStarted;
    }
    private final String jobTitle;
    private CompileListener listener;

    /**
     * Replace the current {@link CompileListener} with your own.
     *
     * @param listener the {@link CompileListener} to use. If this argument is
     * null a {@link SilentListener} will be used instead.
     */
    public final void listen (CompileListener listener) {
        this.listener = listener == null ? new SilentListener() : listener;
    }

    /**
     * Convert status update to {@link Progress}.
     *
     * @param message a {@link BundleKey} providing the status message.
     * @param args arguments to the format string.
     * @return an indeterminate {@link Progress} object.
     */
    protected Progress toProgress (BundleKey message, Object... args) {
        return new Progress(jobTitle, message.format(args));
    }

    /**
     * Get the job title of this {@link CompileJob}.
     *
     * @return a string describing whatever this {@link CompileJob} is doing.
     */
    public final String jobTitle () {
        return jobTitle;
    }

    /**
     * Convert status update to {@link Progress}.
     *
     * @param value current progress value relative to 0 (0%) and maximum
     * @param maximum the maximum progress possible (equivalent to 100%)
     * @param message a {@link BundleKey} providing the status message.
     * @param args arguments to the format string.
     * @return a determinate {@link Progress} object.
     */
    protected Progress toProgress (int value, int maximum, BundleKey message,
                                   Object... args) {
        return new Progress(jobTitle, message.format(args), value, maximum);
    }

    /**
     * Build the object.
     *
     * @return the object built.
     * @throws Exception if an error occurs.
     */
    protected abstract T build () throws Exception;

    /**
     * Notify the user of an update in the progress of the compilation job.
     *
     * @param message the {@link BundleKey} to use as status message.
     * @param args the arguments to the format string if applicable.
     * @see String#format(java.lang.String, java.lang.Object[])
     * @see #postUpdate(int, int,
     * org.europabarbarorum.cuf.support.ResourceHelp.BundleKey,
     * java.lang.Object[])
     */
    protected final void postUpdate (BundleKey message, Object... args) {
        this.publish(toProgress(message, args));
    }

    /**
     * Notify the user of an update in the progress of the compilation job.
     *
     * @param value the amount of progress achieved so far in total.
     * @param maximum the value that corresponds to “complete” progress
     * @param message the {@link BundleKey} to use as status message.
     * @param args the arguments to the format string if applicable.
     * @see String#format(java.lang.String, java.lang.Object[])
     * @see #postUpdate(org.europabarbarorum.cuf.support.ResourceHelp.BundleKey,
     * java.lang.Object[])
     */
    protected final void postUpdate (int value, int maximum, BundleKey message,
                                     Object... args) {
        this.publish(toProgress(value, maximum, message, args));
    }
    private ArrayDeque<Progress> queue = new ArrayDeque<Progress>();
    private long stamp;
    private static long interval = 340L;

    private void publish (Progress p) {
        synchronized (this) {
            long current = System.currentTimeMillis();
            queue.add(p);
            if ((stamp + interval) < current) {
                stamp = current;
                process();
            }
        }
    }

    private void process () {
        synchronized (this) {
            for (Progress s : queue) {
                listener.status(s);
            }
            queue.clear();
        }
    }

    /**
     * Dispose of the {@link CompileJob}. This method is intended for subclasses
     * to dispose of resources when the job is completed or aborted. By default
     * it does nothing.
     */
    protected void dispose () {
    }
    private Exception error = null;
    private boolean hasStarted = false;

    /**
     * Compiles the output file.
     *
     * @return null
     * @throws Exception if an error occurs.
     * @see #build()
     */
    private T doInBackground () throws Exception {
        try {
            hasStarted = true;
            T result = build();
            dispose();
            return result;
        }
        catch (Exception e) {
            error = e;
            listener.fail(error);
            dispose();
            throw e;
        }
    }
    private boolean doneCalled = false;

    /**
     * Signals the {@link CompileListener} that compilation is done and calls {@link #dispose()
     * }.
     */
    @Override
    protected final void done () {
        if (doneCalled) {
            return;
        }
        doneCalled = true;
        process();
        try {
            get();
            listener.status(doneMessage());
            listener.succeed();
        }
        catch (Exception e) {
            listener.status(abortMessage(e));
            if (error == null) {
                e.fillInStackTrace();
                listener.abort(e);
            }
        }
    }

    /**
     * Get the error which caused this {@link CompileJob} to fail. This method
     * is only meaningful if this {@link CompileJob} is finished, and it did
     * indeed fail.
     *
     * @return the error which caused this {@link CompileJob} to fail, or null
     * if there is/was no such error.
     */
    public Exception getException () {
        return error;
    }

    /**
     * Get the status update to use when compilation is aborted.
     *
     * @param e the {@link Exception} which caused the compilation to be
     * cancelled/aborted.
     * @return a {@link Progress} update that signals the cancellation.
     */
    protected abstract Progress abortMessage (Exception e);

    /**
     * Get the status update to use when compilation is finished.
     *
     * @return a {@link Progress} update that signals the completion.
     */
    protected abstract Progress doneMessage ();

    /**
     * A type of {@link CompileJob} to compile to {@link File files}.
     */
    public abstract static class FileJob extends CompileJob<Void> {

        @Override
        protected Progress doneMessage () {
            return toProgress(Messages.CompilationDone, outfile);
        }

        @Override
        protected Progress abortMessage (Exception e) {
            return toProgress(Messages.CompilationAborted,
                              outfile, e.getMessage());
        }

        /**
         * Compile the result file.
         *
         * @throws Exception if an error occurs.
         */
        protected abstract void compile () throws Exception;

        @Override
        protected Void build () throws Exception {
            compile();
            return null;
        }

        /**
         * Create a tempory file to store intermediate results.
         *
         * @param prefix the prefix for a random file name
         * @param suffix the suffix for a randome file name
         * @param tempdir the directory to create the file in, or null to use
         * the default temporary directory (system dependent)
         * @return a File that is guaranteed to be unique. It is guaranteed by
         * the JVM that no other files of that name exist, and that no two calls
         * to this method yield the same temporary file in during same
         * ‘session’.
         * @throws Exception if an error occurs
         */
        protected File createTempFile (final String prefix,
                                       final String suffix,
                                       final File tempdir) throws Exception {
            postUpdate(Messages.Tempfile);
            File f = File.createTempFile(prefix, suffix, tempdir);
            f.deleteOnExit();
            return f;
        }
        private final File outfile;

        /**
         * Get the “destination” of the compiled output. This method is used for
         * a constructing error messages in case a problem arises during
         * compilation, as well as for providing the “Done” status message.
         *
         * @return the file to write output to.
         */
        public File destination () {
            return outfile;
        }

        /**
         * Creates a new {@link FileJob}.
         *
         * @param jobTitle title of the job to use; used in constructing
         * {@link Progress} status updates.
         * @param outfile the destination {@link File} to write results to.
         */
        protected FileJob (String jobTitle, File outfile) {
            super(jobTitle);
            this.outfile = outfile;
        }
    }
}
