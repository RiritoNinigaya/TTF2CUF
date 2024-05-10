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

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import org.europabarbarorum.cuf.shell.Shell.CallBack;
import org.europabarbarorum.cuf.shell.Shell.Makeup;
import org.europabarbarorum.cuf.support.ResourceHelp.BooleanResource;
import org.europabarbarorum.cuf.support.Setting;
import org.europabarbarorum.cuf.support.Setting.Modifiable;

/**
 * Class to run a batch of scripts side by side in a non-interactive (forked) setup.
 * @author Johan Ouwerkerk
 */
public final class ScriptRunner implements Runnable {

    /**
     * Control whether or not exit messages are printed.
     * @see #callBackImpl(org.europabarbarorum.cuf.shell.Shell, java.lang.String, java.lang.Integer) 
     */
    public static final Setting<Boolean> ScriptExitMessage = new Setting<Boolean>(
            "cuf.scriptrunner.message", "yes", new BooleanResource(),
            Modifiable.Conf);
    /**
     * Control error code behaviour of the host {@link Shell} used by
     * {@link ScriptRunner}. If true the error code will always be whatever the last
     * code to be signaled was (possibly not an actual error),
     * if false it will be the code of the last actual error.
     */
    public static final Setting<Boolean> AlwaysUseLastCode = new Setting<Boolean>(
            "cuf.scriptrunner.last", "no", new BooleanResource(),
            Modifiable.Conf);

    /**
     * Create a {@link ScriptRunner} to run a batch of scripts in parallel.
     * @param argv the commandline arguments as passed to the program at launch.
     */
    public ScriptRunner (String[] argv) {
        this.host = new ShellM(argv);
    }

    /**
     * Start the host {@link Shell} and run scripts.
     */
    @Override
    public void run () {
        this.host.start(Shell.SystemLanguage.get());
    }
    private final ShellM host;

    private class ShellR extends ShellX {

        public ShellR (String name, String scriptName, PrintStream stdout,
                       PrintStream stderr, CompileUnit job) {
            super(name, scriptName, stdout, stderr, job);
        }

        public ShellR (String name, BufferedReader input, PrintStream stdout,
                       PrintStream stderr, String scriptName) {
            super(name, input, stdout, stderr, scriptName);
        }

        @Override
        protected ShellX createShellImpl (String name, BufferedReader r,
                                          PrintStream stdout, PrintStream stderr,
                                          String scriptName) {
            return new ShellR(name, r, stdout, stderr, scriptName);
        }

        @Override
        protected Shell createShellImpl (String name, String scriptName,
                                         CompileUnit u, PrintStream out,
                                         PrintStream err) {
            return new ShellR(name, scriptName, out, err, u);
        }

        @Override
        protected int doExit (int i, boolean remove) {
            int k = super.doExit(i, remove);
            host.signalForkExit(scriptName(), k);
            return k;
        }
    }

    private class ShellM extends ShellR {

        public ShellM (String[] scripts) {
            super(Shell.resolveName(null), null, null, null,
                  PredefinedScriptNames.ScriptRunner.getText());
            this.scripts = scripts;
        }
        private final String[] scripts;
        private String language = Shell.SystemLanguage.get();

        @Override
        public int exit (int i) {
            // this method must never be called.
            throw new UnsupportedOperationException();
        }

        @Override
        public int exit () {
            // this method must never be called.
            throw new UnsupportedOperationException();
        }

        /**
         * Return a task to execute the commandline arguments
         * (script/language queries) that were passed to the
         * program at launch.
         */
        @Override
        protected Runnable threadTask () {
            return new Runnable() {

                @Override
                public void run () {
                    for (String script : scripts) {
                        runScript(script);
                    }
                    doExit(runRisk(reportTask), true);
                }
            };
        }
        private final Callable<Integer> reportTask = new Callable<Integer>() {

            @Override
            @SuppressWarnings("unchecked")
            public Integer call () throws Exception {
                while (number() > 1 || tasks.size() > 0) {
                    out(runRisk(tasks.take()));
                }
                return error;
            }
        };

        private void runScript (String script) {
            File f = pathParser().parsePath(script);
            if (f.exists()) {
                super.fork(script, language, null, null, null, null, null, true);
            }
            else {
                this.language = script;
            }
        }
        private int error = ReservedExitCode.EXIT_OK_CODE.exitCode();

        private void signalForkExit (final String src, final int code) {
            tasks.add(new Callable() {

                @Override
                public Object call () throws Exception {
                    ReservedExitCode rcode = callBackImpl(host, src, code);
                    if (rcode != ReservedExitCode.EXIT_OK_CODE
                            || AlwaysUseLastCode.get()) {
                        error = code;
                    }
                    return null;
                }
            });
        }
    }

    /**
     * Implementation method for anonymous {@link CallBack} objects.
     * This method simply prints a status message to the STDOUT stream of the given host {@link Shell}
     * and returns the corresponding {@link ReservedExitCode} or null if none is found. Messages can be
     * disabled by adjusting the {@link #ScriptExitMessage} setting.
     * @param host the host {@link Shell} to use.
     * @param src the script source (typically a filename).
     * @param code exit code produced by the script.
     * @return a {@link ReservedExitCode} if the given exit code corresponds to one, or null if not.
     */
    public static ReservedExitCode callBackImpl (final Shell host,
                                                 final String src,
                                                 final Integer code) {

        ReservedExitCode rcode = ReservedExitCode.fromCode(code);
        if (ScriptExitMessage.get()) {
            if (rcode == null) {
                host.out(Makeup.Notification.makeup(host,
                                                    Messages.GenericScriptExit,
                                                    src,
                                                    code));
            }
            else {
                Makeup m = rcode == ReservedExitCode.EXIT_OK_CODE
                        ? Makeup.Notification
                        : Makeup.Error;
                host.out(m.makeup(host, rcode, src, rcode.exitCode()));
            }
        }
        return rcode;
    }
}
