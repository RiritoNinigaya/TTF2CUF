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
import java.io.PrintStream;
import java.util.concurrent.Callable;
import org.europabarbarorum.cuf.shell.IOService.InputService;
import org.europabarbarorum.cuf.shell.IOService.OutputService;
import org.europabarbarorum.cuf.shell.IOService.OutputWriter;
import org.europabarbarorum.cuf.shell.IOService.PromptService;
import org.europabarbarorum.cuf.shell.IOService.StdStream;
import org.europabarbarorum.cuf.shell.readline.ReadLineSupport;

/**
 * A command line oriented implementation of {@link Shell}.
 * @author Johan Ouwerkerk
 */
public class ShellX extends Shell {

    /**
     * Create a default {@link ShellX shell} for interpreting command line scripts.
     * This constructor is purely for the benefit of subclasses.
     */
    protected ShellX () {
        super(PredefinedScriptNames.StandardInput);
    }

    /**
     * Create a new {@link ShellX shell}.
     * @param name name of the shell to use. This string is used as the name of the variable in the
     * language environment that provides access to this {@link ShellX}.
     * @param input the input {@link BufferedReader} to use for reading the script. If it is null,
     * the {@link Shell} will use STDIN.
     * @param stdout the {@link PrintStream} to use for writing/returning output messages.
     * @param stderr the {@link PrintStream} to use for writing warning/error messages.
     * @param scriptName  the name of the script being executed (used for reporting errors)
     */
    protected ShellX (String name, BufferedReader input, PrintStream stdout,
                      PrintStream stderr, String scriptName) {
        super(name, input, stdout, stderr, scriptName);
    }

    /**
     * Create a new {@link ShellX shell}.
     * @param name name of the shell to use. This string is used as the name of the variable in the
     * language environment that provides access to this {@link ShellX}.
     * @param scriptName the name of the script being executed (used for reporting errors)
     * @param stdout the {@link PrintStream} to use for writing/returning output messages.
     * @param stderr the {@link PrintStream} to use for writing warning/error messages.
     * @param job the {@link CompileUnit compilation task} to perform.
     */
    protected ShellX (String name, String scriptName, PrintStream stdout,
                      PrintStream stderr, CompileUnit job) {
        super(name, scriptName, stdout, stderr, job);
    }

    @Override
    protected Shell createShellImpl (String name, String scriptName,
                                     CompileUnit u, PrintStream out,
                                     PrintStream err) {
        return new ShellX(name, scriptName, out, err, u);
    }

    /**
     * Create a new shell object with pre-configured I/O streams.
     * This method is responsible for returning a {@link Shell} object of the same type
     * as the object which created it. (E.g. subclasses must return an object of the subtype.)
     * This method is invoked post-validation only (so the parameters should be assumed to be valid).
     * @param name name to use for binding the resulting shell object to a variable in
     * its language environment.
     * @param r STDIN stream
     * @param stdout STDOUT stream
     * @param stderr STDERR stream
     * @param scriptName file name of the script being executed.
     * @return a shell object that has been minimally initialised. It has not yet been
     * configured with a working language environment.
     */
    @Override
    protected ShellX createShellImpl (String name, BufferedReader r,
                                      PrintStream stdout, PrintStream stderr,
                                      String scriptName) {
        return new ShellX(name, new BufferedReader(r), stdout,
                          stderr, scriptName);
    }

    @Override
    protected final PredefinedScriptNames defaultScriptName () {
        return PredefinedScriptNames.StandardInput;
    }

    @Override
    protected final OutputService outputThread () {
        return OutputWriter.Instance.get();
    }

    @Override
    protected final InputService inputThread () {
        return PromptService.Instance.get();
    }

    @Override
    protected final void forwardOutput (final StdStream type, final Object toWrite,
                                  final boolean ln) throws Exception {
        if (ln) {
            stream(type).println(toWrite);
        }
        else {
            stream(type).print(toWrite);
        }
    }

    /**
     * Get the {@link PrintStream} which corresponds to this {@link StdStream}.
     * @return the output stream to use for this {@link StdStream}.
     */
    private PrintStream stream (StdStream s) {
        return s == StdStream.Stderr ? System.err : System.out;
    }

    @Override
    protected final String readLineImpl () throws Exception {
        return ReadLineSupport.JLine.readLine();
    }

    /**
     * Hook to display a prompt. By default this method prints the prompt to its STDOUT stream and
     * flushes the stream. Subclasses may use it to update for instance a title on a window.
     * @param ps the string that prompts the user for input.
     */
    @Override
    protected final void displayPrompt (final String ps) {
        outputThread().submit(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                forwardOutput(StdStream.Stdout, ps, false);
                forwardFlush(StdStream.Stdout);
                return null;
            }
        });
    }

    @Override
    protected final void forwardFlush (StdStream stream) throws Exception {
        stream(stream).flush();
    }
}
