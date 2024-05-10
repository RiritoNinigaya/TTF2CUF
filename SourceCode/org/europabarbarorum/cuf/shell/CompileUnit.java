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
package org.europabarbarorum.cuf.shell;

import java.awt.EventQueue;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.font.CUFWriter;
import org.europabarbarorum.cuf.macro.Converter;
import org.europabarbarorum.cuf.shell.Shell.Makeup;
import org.europabarbarorum.cuf.strings.StringsWriter;
import org.europabarbarorum.cuf.strings.StringsWriter.StringsFeeder;
import org.europabarbarorum.cuf.support.Classloader;
import org.europabarbarorum.cuf.support.CompileJob;
import org.europabarbarorum.cuf.support.CompileJob.CompileListener;
import org.europabarbarorum.cuf.support.CompileJob.CompileListenerImpl;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.IllegalMethodCallException;
import org.europabarbarorum.cuf.support.OptionMap;
import org.europabarbarorum.cuf.support.ProgressMonitor;
import org.europabarbarorum.cuf.support.ProgressMonitor.Progress;
import org.europabarbarorum.cuf.support.ResourceHelp.PositiveInteger;
import org.europabarbarorum.cuf.support.Setting;
import org.europabarbarorum.cuf.support.Setting.Modifiable;

/**
 * Class to oversee compilation of some input source to some output file.
 * Its primary task is to provide life-cycle management and status update routing to
 * {@link Shell} and/or GUI.
 * @author Johan Ouwerkerk
 */
public class CompileUnit {

    /**
     * Attach this {@link CompileUnit} to the given {@link Shell}.
     * The result is that when compilation finishes the {@link Shell#exit(int) }
     * method is called with an error code.
     * @param shell the {@link Shell} that is to be tied to this compilation task.
     */
    public void attach (Shell shell) {
        if (shell == null || !shell.isLive()) {
            throw new IllegalArgumentException();
        }
        if (attached != null) {
            throw new IllegalStateException();
        }
        attached = shell;
        listen(defaultListener(shell));
    }
    private Shell attached;

    private CompileListener defaultListener (final Shell toKill) {
        return new CompileListenerImpl() {

            @Override
            public void succeed () {
                exit(ReservedExitCode.EXIT_OK_CODE);
            }

            private void exit (ReservedExitCode code) {
                if (toKill != null && toKill.isLive()) {
                    toKill.doExit(code.exitCode(), true);
                }
                if (monitor != null) {
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            monitor.done();
                        }
                    });
                }
            }

            @Override
            public void abort (Exception e) {
                super.fail(e);
                exit(ReservedExitCode.EXIT_COMPILE_ABORT);
            }

            @Override
            public void fail (Exception e) {
                super.fail(e);
                exit(ReservedExitCode.EXIT_COMPILE_FAIL);
            }

            /**
             * Report an exception to the user.
             * @param e the exception to report.
             */
            @Override
            protected void report (Exception e) {

                Shell s = getShell();
                if (s == null) {
                    e.printStackTrace(System.err);
                }
                else {
                    s.debug(e);
                }
                
            }

            private Shell getShell () {
                if (toKill == null || !toKill.isLive()) {
                    Shell s = Shell.getCurrentShell();
                    if (s != null && s.isLive()) {
                        return s;
                    }
                    else {
                        return null;
                    }
                }
                else {
                    return toKill;
                }
            }

            private void status (Shell s, final Progress text) {
                if (s == null) {
                    System.out.println(text.message);
                }
                else {
                    s.out(Makeup.Progress.makeup(s, text.message));
                }
            }

            @Override
            public void status (final Progress text) {
                boolean b = Shell.ChattyShell.get();
                if (!text.isDeterminate || b) {
                    status(getShell(), text);
                }

                if (monitor != null) {
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            monitor.setText(text);
                        }
                    });
                }
            }
        };
    }

    /**
     * Attach a {@link CompileListener} to the {@link CompileJob} used by this
     * {@link CompileUnit}. The default listener routes {@link Progress} updates if it is set.
     * @param listener a {@link CompileListener} or null to use the default.
     */
    public void listen (CompileListener listener) {
        if (listener == null) {
            listener = defaultListener(null);
        }
        this.job.listen(listener);
    }
    private ProgressMonitor monitor;

    /**
     * Register a {@link ProgressMonitor} with the {@link CompileUnit} to receive notifications of
     * status updated.
     * @param monitor the monitor that tracks the status of this {@link CompileUnit}.
     */
    public void setMonitor (ProgressMonitor monitor) {
        this.monitor = monitor;
    }
    private final CompileJob job;

    /**
     * Create a new compile unit.
     * @param job the compilation job to use/run.
     */
    public CompileUnit (CompileJob job) {
        if (job == null) {
            throw new IllegalArgumentException(
                    Messages.InvalidCompileJob.getText());
        }
        this.job = job;
        listen(null);
    }

    /**
     * Enumeration of supported targets.
     * Used by the factory-style method {@link #getJob(java.io.File, org.europabarbarorum.cuf.shell.CompileUnit.FileType, org.europabarbarorum.cuf.support.OptionMap, java.lang.Object) }.
     */
    public static enum FileType {

        /**
         * Compile output to a CUF file. This setting expects that input is a {@link CUFSource}.
         */
        CUF,
        /**
         * Compile output to a .strings.bin file. This setting expects that input is a {@link StringsFeeder}.
         */
        Strings,
        /**
         * Compile output to a macro file. This setting expects that input is a {@link CUFSource}.
         */
        Macro
    }

    /**
     * Create a new {@link CompileJob} to compile the given input to the given file. This method is intended to
     * be used by toolkit or snapshot type classes.
     * @param outfile output file to save the results to.
     * @param target type of output file to generate
     * @param options a {@link OptionMap} to configure any settings that may be supported for the given target.
     * @param source object matching the expected input type for the given target.
     * @return a {@link CompileJob} that transforms the input source to the output file format saved at
     * the result destination.
     * @see FileType
     */
    @SuppressWarnings("unchecked")
    protected static CompileJob getJob (File outfile,
                                        FileType target,
                                        OptionMap options,
                                        Object source) {
        switch (target) {
            case CUF:
                return new CUFWriter((CUFSource) source, outfile, options);
            case Macro:
                return new Converter((CUFSource) source, outfile);
            case Strings:
                return new StringsWriter((StringsFeeder) source,
                                         outfile,
                                         options);
            default:
                throw new IllegalMethodCallException();
        }
    }
    private static ExecutorService compileService;

    private static ExecutorService lazyService () {
        if (compileService == null) {
            compileService = Executors.newScheduledThreadPool(
                    CompilePoolSize.get(),
                    Classloader.threadFactory("CompileUnit.compileService(%d)"));
        }
        return compileService;
    }
    /**
     * {@link Integer} preference which determines how many threads can be dedicated to
     * compiling files. Optimum number of threads active at any time in the entire program is
     * likely to be the number of cores + 1. This setting defaults to: 1 + half the number of
     * cores available to the program.
     */
    public static final Setting<Integer> CompilePoolSize =
            new Setting<Integer>("cuf.compiler.poolsize",
                                 "" + (1 + (Runtime.getRuntime().
                                            availableProcessors() / 2)),
                                 new PositiveInteger(),
                                 Modifiable.Conf);

    /**
     * Schedule compilation to be run at some point in the future.
     */
    public void submit () {
        if (!hasStarted) {
            hasStarted = true;
            lazyService().execute(job);
        }
    }

    /**
     * Run the compilation. This method blocks until compilation is terminated (either 
     * because the compiler is finished, or because an error occurs) and attending
     * events have run on the EDT.
     */
    public void run () {
        if (!hasStarted) {
            try {
                hasStarted = true;
                job.run();
            }
            catch (Exception ignore) {
                IOHelp.handleExceptions(CompileUnit.class, "run", ignore, ignore.
                        toString());
                // ignore exceptions
            }
        }
    }

    /**
     * Cancel compilation.
     * @return whether or not the {@link CompileUnit} was able to cancel compilation.
     */
    public boolean cancel () {
        return job.cancel(true);
    }
    private boolean hasStarted = false;
}
