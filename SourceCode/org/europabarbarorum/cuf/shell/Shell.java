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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.europabarbarorum.cuf.shell.EscapeSequence.Background;
import org.europabarbarorum.cuf.shell.EscapeSequence.EscapeType;
import org.europabarbarorum.cuf.shell.EscapeSequence.Foreground;
import org.europabarbarorum.cuf.shell.EscapeSequence.Style;
import org.europabarbarorum.cuf.shell.IOService.IOCallWriter;
import org.europabarbarorum.cuf.shell.IOService.InputService;
import org.europabarbarorum.cuf.shell.IOService.OutputService;
import org.europabarbarorum.cuf.shell.IOService.StdStream;
import org.europabarbarorum.cuf.support.BlockingArrayList;
import org.europabarbarorum.cuf.support.BlockingArrayList.Operator;
import org.europabarbarorum.cuf.support.Classloader;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.IOHelp.Log;
import org.europabarbarorum.cuf.support.MimeTag;
import org.europabarbarorum.cuf.support.PathParser;
import org.europabarbarorum.cuf.support.PathParser.Glob;
import org.europabarbarorum.cuf.support.ResourceHelp.BooleanResource;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;
import org.europabarbarorum.cuf.support.ResourceHelp.EnumConstant;
import org.europabarbarorum.cuf.support.ResourceHelp.StringResource;
import org.europabarbarorum.cuf.support.Setting;
import org.europabarbarorum.cuf.support.Setting.Modifiable;

/**
 * This class provides an interpreter to expose the software library to the
 * user.
 *
 * @author Johan Ouwerkerk
 */
public abstract class Shell {

    private static BlockingArrayList<Shell> shells = new BlockingArrayList<Shell>();
    private static ExecutorService service = Executors.newCachedThreadPool(
            Classloader.threadFactory("ShellX#%1$d"));

    /**
     * This layer of indirection is necessary to ensure that the
     * {@link #queue} does not choke on null input (e.g. end-of-line).
     */
    private static interface PromptResult {

        /**
         * Get the entered command.
         *
         * @return the line entered at the prompt, possibly null if an
         * end-of-line is reached (ctrl+D).
         */
        String get ();
    }
    /**
     * Queue of entered commands: the “inbox” of this {@link Shell}.
     */
    private final LinkedBlockingQueue<PromptResult> queue;
    /**
     * {@link LinkedBlockingQueue Queue} of {@link Callable tasks} to perform
     * before continuing with {@link #shellTask}.
     */
    protected final LinkedBlockingQueue<Callable> tasks;
    /**
     * Main task to perform indefinitely in {@link #run()}.
     */
    private Runnable shellTask;
    /**
     * Object to manage finding/loading a script engine.
     */
    private ScriptEngineManager man;
    /**
     * Output stream to dump results to.
     */
    private PrintStream _out_;
    /**
     * Error stream to dump error/diagnostics to.
     */
    private PrintStream _err_;
    /**
     * Interpreter that provides the shell with a runtime (language)
     * environment.
     */
    private ScriptEngine shell;
    /**
     * Name of this shell object within the current language environment.
     */
    private final String boundName;
    /**
     * Name/file of the script being executed by this {@link Shell}.
     */
    private final String scriptName;
    /**
     * Simple {@link Callable} version of the logic needed to process all
     * pending {@link #tasks}.
     */
    private final Callable<Void> processTasks = new Callable<Void>() {

        @Override
        @SuppressWarnings("unchecked")
        public Void call () throws Exception {
            Callable c;
            Object r = null;
            while (!tasks.isEmpty() && isLive) {
                c = tasks.take();
                if (isLive) {
                    r = runRisk(c);
                }
                if (isLive) {
                    out(r);
                }
            }
            return null;
        }
    };

    /**
     * Create a default {@link Shell shell} for interpreting command line
     * scripts. This constructor is purely for the benefit of subclasses.
     *
     * @param n a {@link PredefinedScriptNames predefined script name}. This
     * must be the the {@link #defaultScriptName() default name}.
     */
    protected Shell (PredefinedScriptNames n) {
        this(ShellName.get(), null, null, null, n.getText());
    }

    /**
     * Create a new {@link Shell shell}.
     *
     * @param name name of the shell to use. This string is used as the name of
     * the variable in the language environment that provides access to this {@link Shell}.
     * @param scriptName the name of the script being executed (used for
     * reporting errors)
     * @param stdout the {@link PrintStream} to use for writing/returning output
     * messages.
     * @param stderr the {@link PrintStream} to use for writing warning/error
     * messages.
     * @param job the {@link CompileUnit compilation task} to perform.
     */
    protected Shell (String name, String scriptName, PrintStream stdout,
                     PrintStream stderr, CompileUnit job) {
        boundName = name;
        this.scriptName = scriptName;
        this.shellTask = new CompileUnitInput(job);
        setOutput(_out_);
        setError(_err_);
        tasks = new LinkedBlockingQueue<Callable>();
        queue = new LinkedBlockingQueue<PromptResult>();
    }

    /**
     * Create a new {@link Shell shell}.
     *
     * @param name name of the shell to use. This string is used as the name of
     * the variable in the language environment that provides access to this {@link Shell}.
     * @param input the input {@link BufferedReader} to use for reading the
     * script. If it is null, the {@link Shell} will use STDIN.
     * @param stdout the {@link PrintStream} to use for writing/returning output
     * messages.
     * @param stderr the {@link PrintStream} to use for writing warning/error
     * messages.
     * @param scriptName the name of the script being executed (used for
     * reporting errors)
     */
    protected Shell (String name, BufferedReader input, PrintStream stdout,
                     PrintStream stderr, String scriptName) {
        boundName = name;
        this.scriptName = scriptName;
        setInput(input);
        setOutput(stdout);
        setError(stderr);
        tasks = new LinkedBlockingQueue<Callable>();
        queue = new LinkedBlockingQueue<PromptResult>();
    }
    /**
     * Implementation of the {@link PathParser} used by all {@link Shell}
     * objects. This object should not be used directly, instead call {@link #pathParser()
     * } on a {@link Shell} instance. Doing so allows for instances of {@link Shell}
     * to manipulate internal path name expansion by returing a different
     * implementation of the {@link PathParser} interface.
     */
    private final Glob glob = new Glob();

    /**
     * The object that implements shell expansion of pathnames for this {@link Shell}.
     *
     * @return the {@link PathParser} used by this shell.
     */
    public PathParser pathParser () {
        return glob;
    }
    /**
     * {@link String} preference corresponding to the (fallback) language to use
     * in the shell.
     */
    public static final Setting<String> SystemLanguage =
            new Setting<String>("cuf.shell.script",
                                "text/javascript",
                                new StringResource(),
                                Modifiable.Conf);

    /**
     * Get the value of the query used when loading the current language
     * environment.
     *
     * @return the query submitted to {@link #init(java.lang.String) } when the
     * current environment was loaded.
     */
    public String loadedLanguage () {
        checkLive();
        return mime;
    }

    /**
     * Get the language environment that produced the current interpreter
     * backing the {@link Shell}. This object may be queried for details on the
     * interpreter.
     *
     * @return the {@link ScriptEngineFactory} that created the current
     * interpreter.
     */
    public ScriptEngineFactory getFactory () {
        checkLive();
        return shell.getFactory();
    }

    /**
     * Allows the user to display the welcome screen.
     */
    public void welcome () {
        checkLive();
        welcome(mime, getFactory());
    }

    /**
     * Show a welcome screen to the user.
     *
     * @param lang_name language name as reported by the {@link ScriptEngineFactory}.
     * @param sef an {@link ScriptEngineFactory} associated with the current
     * interpreter.
     */
    private void welcome (String lang_name, ScriptEngineFactory sef) {
        String extension;
        String mime_type;

        // some engines do not properly report file extensions
        try {
            extension = sef.getExtensions().get(0);
        }
        catch (Exception e) {
            extension = Messages.InitDefaultExtension.getText();
            chatError(Makeup.Error.makeup(this,
                                          Messages.InitNoExtensionError,
                                          e));
        }
        // some engines do not properly report MIME types
        try {
            mime_type = sef.getMimeTypes().get(0);
        }
        catch (Exception e) {
            mime_type = Messages.InitDefaultMime.getText();
            chatError(Makeup.Error.makeup(this,
                                          Messages.InitNoMimeError,
                                          e));
        }
        // Give a little welcome screen with various commands to use so the user feels less like:
        // “a fish out of water from out of space”.
        chatOutput(
                Makeup.Notification.makeup(this,
                                           Messages.InitWelcome,
                                           lang_name,
                                           sef.getLanguageVersion(),
                                           sef.getEngineName(),
                                           sef.getEngineVersion(),
                                           extension,
                                           mime_type,
                                           sef.getMethodCallSyntax(boundName,
                                                                   "exit"),
                                           sef.getMethodCallSyntax(boundName,
                                                                   "exit",
                                                                   "<code>"),
                                           sef.getMethodCallSyntax(boundName,
                                                                   "load",
                                                                   "<arg>"),
                                           "<arg>",
                                           sef.getMethodCallSyntax(boundName,
                                                                   "getEngine"),
                                           sef.getMethodCallSyntax(boundName,
                                                                   "getFactory")));
    }

    private String initShell (ScriptEngine can, String mime) {
        // Set our language environment
        if (can == null) {
            error(Makeup.Warning.makeup(this,
                                        Messages.InitNoEnvironmentError,
                                        mime));
            if (shell == null) {
                shell = man.getEngineByMimeType(SystemLanguage.get());
            }
            this.mime = shell.getFactory().getLanguageName();
            return this.mime;
        }
        else {
            shell = can;
            this.mime = mime;
            return shell.getFactory().getLanguageName();
        }
    }

    /**
     * Initialize language environment.
     *
     * @param mime a query to the JVM to look up a javax.script interpreter.
     */
    protected void init (String mime) {
        man = new ScriptEngineManager();

        man.put(boundName, this);

        ScriptEngine can = null;
        if (mime != null) {
            for (int i = 0; can == null && i < 3; ++i) {
                switch (i) {
                    case 0:
                        can = man.getEngineByMimeType(mime);
                        break;
                    case 1:
                        can = man.getEngineByExtension(mime);
                        break;
                    default:
                        can = man.getEngineByName(mime);
                        break;
                }
            }
        }

        String lang_name = initShell(can, mime);
        ScriptContext ctx = shell.getContext();
        ctx.setWriter(new IOCallWriter() {

            @Override
            public void flush () throws IOException {
                Shell.this.flushOutput();
            }

            @Override
            public void print (Object s) throws Exception {
                printOut(s);
            }

            @Override
            public void println (Object s) throws Exception {
                out(s);
            }
        });
        ctx.setErrorWriter(new IOCallWriter() {

            @Override
            public void flush () throws IOException {
                Shell.this.flushError();
            }

            @Override
            public void print (Object s) throws Exception {
                printError(s);
            }

            @Override
            public void println (Object s) throws Exception {
                error(s);
            }
        });
        shell.setContext(ctx);
        shell.put(ScriptEngine.FILENAME, scriptName);

        if (isInteractive()) {
            welcome(lang_name, shell.getFactory());
        }
        if (!(shell instanceof Invocable)) {
            chatError(Makeup.Warning.makeup(this, Messages.NotInvocableError));
        }
    }

    /**
     * Cause this shell to reload its language environment. This is required to
     * take advantage of resources linked to through {@link #link(java.lang.String)}
     * at runtime.
     */
    public void reload () {
        checkLive();
        preload(mime, mime);
    }

    /**
     * Causes the interpreter to load a language environment matching providing
     * support for the programming language matching the given MIME type, file
     * extension, language, or name.
     *
     * @param query the language/MIME/extension/name of the new language
     * enviroment to use. This value will be converted to all lower case before
     * being used.
     */
    public void load (String query) {
        checkLive();
        preload(PreloadScript.mapQuery(query), mime);
    }

    private void preload (final String query, final String old) {
        tasks.add(new Callable() {

            @Override
            public Void call () throws Exception {
                new PreloadScript(old, query).run(Shell.this);
                return null;
            }
        });
    }
    private String mime = null;

    /**
     * Get the (file) name of the script being executed.
     *
     * @return the script name used for creating this {@link Shell}.
     */
    public String scriptName () {
        if (shell == null) {
            return scriptName;
        }
        Object s = shell.get(ScriptEngine.FILENAME);
        return s == null ? scriptName : s.toString();
    }

    /**
     * Redirect STDERR to the specified output stream.
     *
     * @param err the {@link PrintStream} to println error messages to.
     */
    public final void setError (PrintStream err) {
        checkLive();
        this._err_ = err;
    }

    /**
     * Redirect STDIN to the specified reader argument.
     *
     * @param input the {@link BufferedReader} to read commands from. Use null
     * to redirect the other way. (Forces the shell to read STDIN instead of its
     * previous input source).
     */
    public final void setInput (BufferedReader input) {
        checkLive();
        this.shellTask = input == null
                ? new InteractiveInput() : new StreamScriptInput(input);
    }

    /**
     * Redirect output to the specified output stream.
     *
     * @param out the output stream to println to.
     */
    public final void setOutput (PrintStream out) {
        checkLive();
        this._out_ = out;
    }

    /**
     * Return whether or not this {@link Shell} is interactive.
     *
     * @return true if the shell prompts a user for input; false if it doesn't.
     * (I.e. when it reads its input from a file.)
     */
    public boolean isInteractive () {
        return shellTask instanceof InteractiveInput;
    }

    /**
     * Create a new {@link Shell shell} object. This method is responsible for
     * returning a {@link Shell} object of the same type as the object which
     * created it. (E.g. subclasses must return an object of the subtype.) This
     * method is invoked post-validation only (so the parameters should be
     * assumed to be valid).
     *
     * @param name name to use for binding the resulting shell object to a
     * variable in its language environment.
     * @param r a {@link BufferedReader} to read the script from. Use null to
     * read from STDIN.
     * @param stdout the {@link PrintStream} to use for reporting output. Use
     * null to forward data to {@link StdStream#Stdout STDOUT}.
     * @param stderr the {@link PrintStream} to use for reporting errors. Use
     * null to forward data to {@link StdStream#Stderr STDERR}.
     * @param scriptName (file) name of the script being executed.
     * @return a {@link Shell shell} object that has been minimally initialised.
     * It has not yet been configured with a working language environment.
     */
    protected abstract Shell createShellImpl (String name, BufferedReader r,
                                              PrintStream stdout,
                                              PrintStream stderr,
                                              String scriptName);

    /**
     * Create a new {@link Shell shell} object. This method is responsible for
     * returning a {@link Shell} object of the same type as the object which
     * created it. (E.g. subclasses must return an object of the subtype.) This
     * method is invoked post-validation only (so the parameters should be
     * assumed to be valid).
     *
     * @param name name to use for binding the resulting shell object to a
     * variable in its language environment.
     * @param scriptName (file) name of the script being executed.
     * @param u a {@link CompileUnit compilation task} to perform.
     * @param out the {@link PrintStream} to use for reporting output. Use null
     * to forward data to {@link StdStream#Stdout STDOUT}.
     * @param err the {@link PrintStream} to use for reporting errors. Use null
     * to forward data to {@link StdStream#Stderr STDERR}.
     * @return a {@link Shell shell} object that has been minimally initialised.
     * It has not yet been configured with a working language environment.
     */
    protected abstract Shell createShellImpl (String name, String scriptName,
                                              CompileUnit u, PrintStream out,
                                              PrintStream err);

    private Shell createShell (String name, String scriptName, CompileUnit u,
                               PrintStream out, PrintStream err) {
        checkLive();
        if (scriptName == null || scriptName.equals("")) {
            throw new IllegalArgumentException(
                    Messages.NoScriptNameError.getText());
        }
        if (u == null) {
            throw new IllegalArgumentException(
                    Messages.NoCompileUnitError.getText());
        }
        return createShellImpl(resolveName(name), scriptName, u, out, err);
    }

    /**
     * Create a new shell object with pre-configured I/O streams.
     *
     * @param name name to use for binding the resulting shell object to a
     * variable in its language environment.
     * @param input {@link InputStream} which feeds script statements to the {@link Shell}
     * @param encoding the character encoding to use. Specify null to use the
     * default.
     * @param stdout {@link PrintStream} which corresponds to the logical STDOUT
     * stream
     * @param stderr {@link PrintStream} which corresponds to the logical STDERR
     * stream
     * @param scriptName file name of the script being executed.
     * @return a shell object that has been minimally initialised. It has not
     * yet been configured with a working language environment.
     * @throws Exception if method arguments are not valid.
     * @see #createShellImpl(java.lang.String, java.io.BufferedReader,
     * java.io.PrintStream, java.io.PrintStream, java.lang.String)
     */
    public Shell createShell (String name, InputStream input, String encoding,
                              PrintStream stdout,
                              PrintStream stderr, String scriptName) throws
            Exception {
        checkLive();
        Reader r;
        boolean cleanup = true;
        try {
            if (input == null) {
                throw new IllegalArgumentException(
                        Messages.NoInputStreamError.getText());
            }
            if (scriptName == null || scriptName.equals("")) {
                throw new IllegalArgumentException(
                        Messages.NoScriptNameError.getText());
            }
            r = encoding == null ? new InputStreamReader(input) : new InputStreamReader(
                    input, encoding);
            cleanup = false;
            return createShellImpl(resolveName(name), new BufferedReader(r),
                                   stdout, stderr,
                                   scriptName);
        }
        finally {
            if (cleanup) {
                input.close();
                stdout.close();
                stderr.close();
            }
        }
    }

    /**
     * Create a new default shell object. This method provides the shell with
     * the
     * {@link System#in} stream wrapped in a {@link BufferedReader} if {@link System#console()
     * } returns null.
     *
     * @param name name to use for binding the resulting shell object to a
     * variable in its language environment
     * @return a shell object that has been minimally initialised. It has not
     * yet been configured with a working language environment.
     */
    public Shell createShell (String name) {
        return createShellImpl(resolveName(name),
                               null,
                               null,
                               null,
                               defaultScriptName().getText());
    }

    /**
     * Get a {@link PredefinedScriptNames predefined scriptname} to use as a
     * default.
     *
     * @return a default script name for this type of {@link Shell}.
     */
    protected abstract PredefinedScriptNames defaultScriptName ();

    /**
     * Resolve the name to use for binding a {@link Shell} to a variable in its
     * language environment.
     *
     * @param name the name to use
     * @return a default name if the name is invalid, otherwise the name is
     * returned as is.
     */
    static String resolveName (String name) {
        return name == null || name.equals("")
                ? ShellName.get()
                : name;
    }
    /**
     * {@link String} preference which denotes the (default) name to use when
     * binding a {@link Shell} to a variable in its language enviroment.
     */
    public static final Setting<String> ShellName =
            new Setting<String>("cuf.shell.name", "shell", new StringResource(),
                                Modifiable.Conf);

    /**
     * Get the name of the current shell. This is the name of the variable any
     * interpreted scripts can use to access this {@link Shell} instance.
     *
     * @return the name of the variable used to access this {@link Shell}
     * instance from script code.
     */
    public String shellName () {
        return boundName;
    }

    /**
     * Transfers input “focus” to the shell referred by the given index. This
     * method will cause this {@link Shell shell} to block until focus is
     * transferred back again. If the given index is invalid, then input “focus”
     * is transferred back to this shell.
     *
     * @param k the index of the {@link Shell shell} to gain input “focus”.
     */
    public void jumpTo (final int k) {
        checkLive();
        Boolean worked = runRisk(new Callable<Boolean>() {

            @Override
            public Boolean call () throws Exception {
                lock.acquire();
                int sz = shells.size();
                if (k < 0 || sz <= k) {
                    throw new IllegalArgumentException(
                            Messages.JumpToInvalidIndex.format(k, sz));
                }
                Shell s = shells.get(k);
                s.current = true;
                current = false;
                s.lock.release();
                return true;
            }
        });
        if (worked == null || !worked) {
            this.lock.release();
            this.current = true;
        }
    }

    /**
     * Get the interpreter that parses and executes command.
     *
     * @return the {@link ScriptEngine} that evaluates shell commands and
     * scripts.
     */
    public ScriptEngine getEngine () {
        checkLive();
        return shell;
    }
    /**
     * {@link Boolean} preference which determines whether or not to print full
     * stack traces for exceptions in shell output.
     */
    public static final Setting<Boolean> Debug = new Setting<Boolean>(
            "cuf.shell.debug",
            "true",
            new BooleanResource(),
            Modifiable.Conf);

    /**
     * Exception-safe method to execute arbitrary {@link Callable} tasks.
     *
     * @param <T> type parameter of the {@link Callable} to avoid requiring
     * explicit casts.
     * @param script the {@link Callable} task to run.
     * @return the result of the given {@link Callable}, possibly null. In
     * particular if an error occurs during the task, this method returns null.
     */
    @SuppressWarnings("unchecked")
    protected <T> T runRisk (Callable<T> script) {
        Object r = interpret(script, Messages.UnexpectedError, boundName);
        return r == null ? null : (T) r;
    }

    /**
     * Interpret a script or evaluate a {@link Callable}. Errors are sent to {@link #error(java.lang.Object)
     * } and optionally
     * {@link #debug(java.lang.Exception)}.
     *
     * @param script the script provided through a {@link Reader} or string to
     * interpret. Alternatively provide a {@link Callable}, in which case this
     * method merely provides exception safety.
     * @param m the {@link Messages error message} to use if anything goes
     * wrong.
     * @param arg first argument given to the error message. (The details
     * message is supplied by this method itself.)
     * @return the result produced by the evaluated code, possibly null if the
     * return type was void or if an error occurs.
     */
    private Object interpret (Object script, Messages m, Object arg) {
        try {
            return script instanceof Callable
                    ? ((Callable) script).call() : exec(script);
        }
        catch (Throwable e) {
            error(Makeup.Error.makeup(this,
                                      m,
                                      arg,
                                      e.getLocalizedMessage()));
            if (Debug.get()) {
                e.fillInStackTrace();
                debug(e);
            }
            return null;
        }
    }

    private Object exec (Object r) throws Exception {
        try {
            return r instanceof Reader
                    ? shell.eval((Reader) r) : shell.eval(r.toString());
        }
        finally {
            if (r instanceof Reader) {
                ((Reader) r).close();
            }
        }
    }

    /**
     * Evaluate a shell command. Errors are sent to {@link #error(java.lang.Object)
     * } and optionally to {@link #debug(java.lang.Throwable)}.
     *
     * @param line the command to evaluate.
     * @return the result produced by the evaluated code, possibly null if the
     * return type was void or if an error occurs.
     */
    public Object eval (String line) {
        checkLive();
        return interpret(line, Messages.EvalInvalidError, line);
    }

    private Object interpret (Reader script, String f) {
        Object old = shell.get(ScriptEngine.FILENAME);
        shell.put(ScriptEngine.FILENAME, f);
        Object r = interpret(script, Messages.InterpretInvalidError, f);
        shell.put(ScriptEngine.FILENAME, old);
        return r;
    }

    /**
     * Interpret the script referred to by the given path name.
     *
     * @param script the path to the file to interpret.
     * @return the result of the script, possibly null. In particular if an
     * error occurs in the script, this method will return null.
     */
    public Object interpret (final String script) {
        checkLive();
        return runRisk(new Callable<Object>() {

            @Override
            public Object call () throws Exception {
                return interpret(pathParser().reader(script), script);
            }
        });
    }

    /**
     * Schedule a script to be interpreted.
     *
     * @param script the path to the file to interpret.
     * @param query the script language of the file.
     */
    public void interpret (final String script, final String query) {
        checkLive();
        final String old = mime;
        tasks.add(new Callable() {

            @Override
            public Object call () throws Exception {
                new PreloadScript(old, query).run(Shell.this);
                return Shell.this.interpret(script);
            }
        });
    }

    /**
     * Get the index of this {@link Shell shell}. This method blocks until any
     * pending operations which might affect this number have completed.
     *
     * @return the index number of this {@link Shell shell}.
     */
    public Integer index () {
        checkLive();
        return runRisk(new Callable<Integer>() {

            @Override
            public Integer call () throws Exception {
                return shells.indexOf(Shell.this);
            }
        });
    }
    private boolean isLive = true;

    /**
     * Start this {@link Shell} with a given language environment. Input “focus”
     * is transferred to this shell. This method will cause the {@link #getCurrentShell() current shell}
     * to block until focus is transferred back again.
     *
     * @param language a query to look up a particular language environment.
     * This might be a language name, interpreter name, file extension or MIME
     * type.
     * @see #jumpTo(int)
     */
    public void start (String language) {
        checkLive();
        if (mime != null) {
            throw new IllegalStateException(Messages.StartShellError.format(
                    boundName));
        }
        start(language, true);
    }

    private void start (String language, final boolean acquire) {
        preload(language, mime == null ? SystemLanguage.get() : mime);
        Shell r = runRisk(new Callable<Shell>() {

            @Override
            public Shell call () throws Exception {
                shells.add(Shell.this);
                Shell y = Shell.getCurrentShell();
                if (acquire) {
                    if (y != null) {
                        if (y.lock.availablePermits() > 0) {
                            y.lock.acquire();
                        }
                        y.current = false;
                    }
                    Shell.this.current = true;
                }
                service.submit(Shell.this.threadTask());
                Shell.this.outputThread().start();
                Shell.this.inputThread().start();
                return Shell.this;
            }
        });
        if (r == null) {
            this.lock.release();
        }
    }
    private Semaphore lock = new Semaphore(1, true);
    private CallBack<Integer> exitSlot;

    /**
     * Get the number of subshells currently running. This method blocks until
     * any pending operations which might affect this number have completed.
     *
     * @return the number of {@link Shell shells} when this thread queries the
     * {@link #shells underlying collection} for it.
     * @throws InterruptedException if this thread is interrupted
     */
    public int number () throws InterruptedException {
        return shells.size();
    }

    /**
     * Attempt to kill all {@link Shell subshells} with the given error code.
     *
     * @param code the error code to use.
     * @return true if this method succeeded, false if not.
     */
    public boolean killAll (int code) {
        final int k = checkExitCode(code);
        return runRisk(new Callable<Boolean>() {

            @Override
            public Boolean call () throws Exception {
                kill(k);
                return true;
            }
        });
    }

    /**
     * Attempt to kill all {@link Shell subshells} with the {@link ReservedExitCode#EXIT_SHELL_KILLED default kill}
     * code.
     *
     * @return true if this method succeeded, false if not.
     */
    public boolean killAll () {
        return runRisk(new Callable<Boolean>() {

            @Override
            public Boolean call () throws Exception {
                kill(ReservedExitCode.EXIT_SHELL_KILLED.exitCode());
                return true;
            }
        });
    }

    private void kill (final int code) throws InterruptedException {
        shells.operation(new Operator<Shell, ArrayList<Shell>>() {

            @Override
            public Shell callback (ArrayList<Shell> operand) {
                Shell x;
                for (int k = operand.size() - 1, i = -1; i < k; --k) {
                    x = operand.get(k);
                    if (!x.equals(Shell.this)) {
                        x.doExit(code, false);
                        operand.remove(x);
                    }
                }
                return Shell.this;
            }
        }).doExit(code, true);
    }

    private void exitSlot (final CallBack<Integer> call) {
        checkLive();
        if (exitSlotTaken()) {
            throw new IllegalStateException(Messages.ExitSlotTaken.getText());
        }
        if (call == null) {
            throw new NullPointerException(Messages.InvalidCallBack.getText());
        }
        this.exitSlot = call;
    }
    private long thread;

    /**
     * Blocks the current thread until the given {@link Shell} exits.
     *
     * @param waitFor the {@link Shell} to wait for.
     * @param additional an additional {@link CallBack} to intercept the exit
     * code signal.
     * @throws InterruptedException if this thread is interrupted while waiting
     * for the given {@link Shell} to exit.
     */
    public void blockUntilExit (Shell waitFor,
                                final CallBack<Integer> additional) throws
            InterruptedException {
        checkLive();
        Thread t = Thread.currentThread();
        long id = t.getId();
        if (id == thread || EventQueue.isDispatchThread()) {
            throw new IllegalStateException(
                    Messages.ThreadBlockError.format(t.getName(), id));
        }
        final Semaphore s = new Semaphore(0);
        final CallBack<Integer> call = new CallBack<Integer>() {

            @Override
            public void callback (final Integer signal) {
                s.release();
                if (additional != null) {
                    exitTask(additional, signal);
                }
            }
        };
        waitFor.exitSlot(call);
        s.acquire();
    }

    /**
     * Check if a {@link CallBack} has been
     * {@link #exitSlot(org.europabarbarorum.cuf.shell.Shell.CallBack, org.europabarbarorum.cuf.shell.Shell) registered}
     * for the exit event.
     *
     * @return true if a {@link CallBack} has been registered false if not.
     */
    public boolean exitSlotTaken () {
        return this.exitSlot != null;
    }

    private void exitTask (final CallBack<Integer> call, final Integer signal) {
        if (isLive) {
            tasks.add(new Callable() {

                @Override
                public Void call () throws Exception {
                    call.callback(signal);
                    return null;
                }
            });
        }
    }

    /**
     * Register a {@link CallBack} to receive {@link #exitCode() exit signals}.
     *
     * @param call the {@link CallBack} to register: it must take an integer
     * (exit code) as argument.
     * @param context the {@link Shell} which is to run the {@link CallBack}
     * when this
     * {@link Shell} emits the exit signal.
     */
    public void exitSlot (final CallBack<Integer> call,
                          final Shell context) {
        exitSlot(new CallBack<Integer>() {

            @Override
            public void callback (final Integer signal) {
                context.exitTask(call, signal);
            }
        });
    }

    private void dispose () {
        _out_ = null;
        shellTask = null;
        _err_ = null;
        shell = null;
        man = null;
        System.gc();
    }
    private boolean current;

    /**
     * Get the currently selected/focused shell.
     *
     * @return the {@link ShellX} which currently has focus.
     */
    public static Shell getCurrentShell () {
        try {
            return shells.operation(new BlockingArrayList.Operator<Shell, ArrayList<Shell>>() {

                @Override
                public Shell callback (ArrayList<Shell> operand) {
                    for (Shell x : operand) {
                        if (x.current) {
                            return x;
                        }
                    }
                    return null;
                }
            });
        }
        catch (Throwable t) {
            IOHelp.handleExceptions(ShellX.class, "getCurrentShell", t,
                                    "unable to get the current shell.");
            return null;
        }
    }

    /**
     * Check if the exit code is reserved for internal use by the {@link Shell}.
     *
     * @param exitCode the code to check
     * @return true if the code is reserved, false if it can be used.
     */
    public boolean isReserved (int exitCode) {
        return ReservedExitCode.fromCode(exitCode) != null;
    }

    /**
     * Underlying implementation of {@link #exit() } and {@link #exit(int) }.
     * This matter takes care of notifying any registered {@link CallBack}
     * object, of disposing the shell and of exiting the JVM when required. This
     * method throws an {@link IllegalStateException} if the shell has already
     * been disposed of.
     *
     * @param i the exit code to use.
     * @param remove whether or not to run the full disposal logic. If false,
     * the caller must ensure that this shell is removed from {@link #shells the collection of shells}.
     * @return the given exit code.
     */
    protected int doExit (final int i, final boolean remove) {
        if (isLive()) {
            exitCode = new Integer(i);
            runRisk(new Callable<Void>() {

                private int k = 0;

                @Override
                public Void call () throws Exception {
                    if (remove) {
                        if (exitSlot != null) {
                            exitSlot.callback(i);
                        }
                        k = shells.size();
                        if (k == 1) {
                            System.exit(i);
                        }
                        else {
                            shells.remove(Shell.this);
                        }
                        jumpTo(jump(k));
                    }
                    return null;
                }
            });
            isLive = false;
            dispose();
        }
        else {
            throw new IllegalStateException(Messages.ExitIllegalError.format(
                    boundName,
                    exitCode,
                    Thread.currentThread().getName()));
        }
        return i;
    }
    private Integer exitCode;

    /**
     * Fork a subshell and start the new subshell.
     *
     * @param script path to the file to interpret as script in the subshell
     * @param call the {@link CallBack} to evaluate the exit code of the
     * subshell after it is done interpreting the script.
     * @see #fork(java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.io.PrintStream, java.io.PrintStream,
     * org.europabarbarorum.cuf.shell.Shell.CallBack, boolean)
     */
    public void fork (String script, CallBack<Integer> call) {
        fork(script, null, null, null, null, null, call, true);
    }

    /**
     * Fork a subshell and start the new subshell.
     *
     * @param unit the {@link CompileUnit compilation task} to run
     * @param language the language to use for interpreting the script, or null
     * to use the same language as the current shell
     * @param shellName the name of the shell to use, or null to use the same
     * name as the current shell
     * @param out the STDOUT stream to use, or null to use the stream of the
     * current shell
     * @param err the STDERR stream to use, or null to use the stream of the
     * current shell
     * @param call the {@link CallBack} to evaluate the exit code of the
     * subshell after the given {@link CompileUnit compilation task} finishes.
     * @param inContext whether or not the given {@link CallBack} must run in
     * the context of the
     * {@link Shell shell} which forks the new subshell.
     */
    public void fork (final CompileUnit unit, String language, String shellName,
                      PrintStream out, PrintStream err, CallBack<Integer> call,
                      boolean inContext) {
        Shell fork = createShell(
                shellName == null || shellName.equals("") ? boundName : shellName,
                scriptName,
                unit,
                out == null ? _out_ : out,
                err == null ? _err_ : err);
        forkImpl(fork, language, call, inContext);
    }

    private void forkImpl (Shell fork, String language, CallBack<Integer> call,
                           boolean inContext) {
        if (call != null) {
            if (inContext) {
                fork.exitSlot(call, this);
            }
            else {
                fork.exitSlot(call);
            }
        }
        fork.start(language == null || language.equals("") ? mime : language,
                   false);
    }

    /**
     * Fork a subshell and start the new subshell.
     *
     * @param script path to the file to interpret in the subshell
     * @param language the language to use for interpreting the script, or null
     * to use the same language as the current shell
     * @param encoding the character encoding to use for reading scripts from
     * file. Specify null to use the default.
     * @param shellName the name of the shell to use, or null to use the same
     * name as the current shell
     * @param out the STDOUT stream to use, or null to use the stream of the
     * current shell
     * @param err the STDERR stream to use, or null to use the stream of the
     * current shell
     * @param call the {@link CallBack} to evaluate the exit code of the
     * subshell after it is done interpreting the script.
     * @param inContext whether or not the given {@link CallBack} must run in
     * the context of the
     * {@link Shell shell} which forks the new subshell.
     * @see #fork(java.lang.String,
     * org.europabarbarorum.cuf.shell.Shell.CallBack)
     */
    public void fork (final String script,
                      final String language,
                      final String encoding,
                      final String shellName,
                      final PrintStream out,
                      final PrintStream err,
                      final CallBack<Integer> call,
                      final boolean inContext) {
        try {
            File path = pathParser().parsePath(script);
            Shell fork = createShell(
                    shellName == null || shellName.equals("") ? boundName : shellName,
                    new FileInputStream(path),
                    encoding,
                    out == null ? _out_ : out,
                    err == null ? _err_ : err,
                    script);
            forkImpl(fork, language, call, inContext);
        }
        catch (Exception e) {
            chatError(Makeup.Error.makeup(this,
                                          Messages.InterpretInvalidError,
                                          script,
                                          e.getMessage()));

            if (call != null) {
                call.callback(
                        ReservedExitCode.EXIT_NO_FILE.exitCode());
            }
        }
    }

    /**
     * Link this shell object against a file. This method adds it to the list of
     * URLs from which executable code and resources may be loaded. Exceptions
     * are suppressed, but errors are sent to errror streams. On success a
     * message is sent to the output stream as well.
     *
     * @param file path of the library to link against
     * @return true if successful, false if not.
     */
    public boolean link (String file) {
        checkLive();
        synchronized (Classloader.get()) {
            File path = pathParser().parsePath(file);
            try {
                if (!path.exists()) {
                    throw new PathParser.InvalidFileException(
                            Messages.LinkNonExistingError, file);
                }
                path = pathParser().canonise(path);
                if (!path.canRead()) {
                    throw new PathParser.InvalidFileException(
                            Messages.LinkUnreadableError, file);
                }

                if (!path.isDirectory()) {
                    MimeTag tag = MimeTag.getType(path);
                    if (tag != MimeTag.ZipArchive) {
                        throw new PathParser.InvalidFileException(
                                Messages.LinkTypeInvalidError,
                                file,
                                tag.getText());
                    }
                }
                String cp = "java.class.path",
                        cpath = System.getProperty(cp);
                if (cpath.contains(path.toString())) {
                    throw new PathParser.InvalidFileException(
                            Messages.LinkClasspathError, file);
                }
                URL u = path.toURI().toURL();
                if (Classloader.get().extendCpath(u)) {
                    out(Makeup.Notification.makeup(this,
                                                   Messages.LinkSuccessful,
                                                   file));
                    return true;
                }
                throw new PathParser.InvalidFileException(
                        Messages.LinkLinksError,
                                                          file);
            }
            catch (PathParser.InvalidFileException ivfe) {
                chatError(Makeup.Error.makeup(this,
                                              ivfe.getLocalizedMessage()));

            }
            catch (Exception e) {
                chatError(Makeup.Error.makeup(this,
                                              Messages.LinkURLError,
                                              file,
                                              e.getLocalizedMessage()));
            }
            return false;
        }
    }

    /**
     * Check if a path is part of the additional list of linked libraries in
     * this {@link Shell}.
     *
     * @param file the path to check
     * @return true if the given path is part of the additional list of URL's
     * used by the {@link Shell} to load code (and program resources). Returns
     * false if an error occurs or if the path is not linked against.
     */
    public boolean isLinked (String file) {
        checkLive();
        File path = pathParser().parsePath(file);
        try {
            path = pathParser().canonise(path);
            URL u = path.toURI().toURL();
            return Classloader.get().isLinked(u);
        }
        catch (PathParser.InvalidFileException ivfe) {

            chatError(Makeup.Error.makeup(this,
                                          ivfe.getLocalizedMessage()));
        }
        catch (Exception e) {
            chatError(Makeup.Error.makeup(this,
                                          Messages.LinkURLError,
                                          file,
                                          e.getLocalizedMessage()));
        }
        return false;
    }

    /**
     * Get the exit status of the disposed shell. This method will throw an {@link IllegalStateException}
     * if the shell has not yet exited. Use {@link #isLive() } to determine in
     * advance whether the shell has exited before calling this method.
     *
     * @return the exit code of the shell if it has exited.
     */
    public int exitCode () {
        if (exitCode == null) {
            throw new IllegalStateException(
                    Messages.NoExitCodeError.format(boundName));
        }
        return exitCode;
    }

    /**
     * Check if this {@link Shell shell} is “live”.
     *
     * @return true if this {@link Shell shell} is still running, or false if it
     * has been disposed of.
     */
    public boolean isLive () {
        return isLive;
    }

    /**
     * Cause the shell to exit with the reserved ‘0’ (success) exit code. This
     * method is the same as {@link #exit(int) } except that it allows you to
     * use the reserved ‘0’ exit code.
     *
     * @return 0
     * @see ReservedExitCode#EXIT_OK_CODE
     * @see #exitCode()
     */
    public int exit () {
        return doExit(ReservedExitCode.EXIT_OK_CODE.exitCode(), true);
    }

    private int checkExitCode (int i) {
        if (i > 0xFF || isReserved(i)) {
            int j = i == ReservedExitCode.EXIT_ILLEGAL_CODE.exitCode()
                    ? ReservedExitCode.EXIT_ILLEGAL_CODE2.exitCode()
                    : ReservedExitCode.EXIT_ILLEGAL_CODE.exitCode();
            chatError(Makeup.Warning.makeup(this,
                                            Messages.ReservedExitCodeError,
                                            i,
                                            j));

            return j;
        }
        return i;
    }

    /**
     * Exit with specific exit code. This method may alter the exit code if the
     * user attempts to exit with a {@link ReservedExitCode}. This method causes
     * the {@link Shell} to be {@link #dispose() disposed of interally}. If no
     * subshells would remain after this {@link Shell} has exited then this
     * method also calls {@link System#exit(int) }.
     *
     * @param i exit code to use
     * @return the code with which this {@link Shell} exits.
     * @see ReservedExitCode#EXIT_ILLEGAL_CODE
     * @see ReservedExitCode#EXIT_ILLEGAL_CODE2
     * @see #exitCode()
     */
    public int exit (int i) {
        return doExit(checkExitCode(i), true);
    }

    /**
     * Schedule a task to run. This method must only be used from within the
     * context (thread) of this {@link Shell shell} itself.
     *
     * @param run the {@link Callable} to evaluate. Note that its output is
     * written to the {@link #setOutput(java.io.PrintStream) output stream} of
     * this
     * {@link Shell shell}.
     */
    public void schedule (final Callable run) {
        checkLive();
        final String name = Thread.currentThread().getName();
        tasks.add(new Callable() {

            @Override
            public Object call () throws Exception {
                long k = Thread.currentThread().getId();
                if (k != thread) {
                    String n = Thread.currentThread().getName();
                    throw new IllegalStateException(
                            Messages.ScheduleInvalidError.format(thread,
                                                                 name,
                                                                 k,
                                                                 n));
                }
                return run.call();
            }
        });
    }

    private int jump (int size) {
        return (index() + 1) % size;
    }

    private class StreamScriptInput implements Runnable {

        private final BufferedReader br;

        private StreamScriptInput (BufferedReader br) {
            this.br = br;
        }

        @Override
        public void run () {
            interpret(br, Messages.InterpretInvalidError, scriptName());
            if (exitCode == null) {
                chatError(Makeup.Warning.makeup(Shell.this,
                                                Messages.PromptAutoJump,
                                                boundName,
                                                index()));
                setInput(null);
            }
        }
    }

    private class CompileUnitInput implements Runnable {

        private final CompileUnit u;

        private CompileUnitInput (CompileUnit u) {
            this.u = u;
        }

        @Override
        public void run () {
            u.attach(Shell.this);
            u.run();
        }
    }

    private class InteractiveInput implements Runnable {

        private String read;
        private Object out;
        private final PromptSettings settings = new PromptSettings(Shell.this);
        private final Callable<String> readLines = new Callable<String>() {

            @Override
            public String call () throws Exception {
                return readCode(new StringBuilder(), settings.getPS1());
            }
        };

        private String readCode (StringBuilder buffer, String prompt) throws
                InterruptedException {
            String code = prompt(settings.formatPrompt(prompt));
            if (code == null) {
                return null;
            }
            if (code.endsWith(nextprompt)) {
                buffer.append(code.substring(0, code.length() - 1));
                return readCode(buffer, settings.getPS2());
            }
            else {
                buffer.append(code);
                return buffer.toString();
            }
        }

        @Override
        public void run () {
            read = runRisk(readLines);
            if (read == null) {
                Integer sz = runRisk(new Callable<Integer>() {

                    @Override
                    public Integer call () throws Exception {
                        return shells.size();
                    }
                });
                if (sz == null || sz == 1) {
                    int code = ReservedExitCode.EXIT_NO_INPUT.exitCode();
                    chatError(Makeup.Warning.makeup(Shell.this,
                                                    Messages.PromptAutoExit,
                                                    boundName,
                                                    code));

                    doExit(code, true);
                }
                int i = jump(sz);
                chatError(Makeup.Warning.makeup(Shell.this,
                                                Messages.PromptAutoJump,
                                                boundName,
                                                i));

                jumpTo(i);
            }
            else {
                out = eval(read);
                if (isLive) {
                    out(out);
                }
                out = null;
                read = null;
            }
        }
    }

    /**
     * Runs the task of this {@link Shell shell}. This method will return an
     * indefinite cycle of
     * {@link #schedule(java.util.concurrent.Callable) scheduled} tasks followed
     * by the main shell task (prompting for & evaluating input or running a
     * script or a {@link CompileUnit compilation task}).
     *
     * @return the task of this {@link Shell}.
     */
    protected Runnable threadTask () {
        return new Runnable() {

            @Override
            public void run () {
                thread = Thread.currentThread().getId();
                try {
                    do {
                        runRisk(processTasks);
                        shellTask.run();
                    }
                    while (isLive);
                }
                catch (Exception e) {
                    IOHelp.handleExceptions(Shell.class, "run", e, e.toString());
                }
            }
        };
    }

    /**
     * Send data to the error stream of this {@link Shell}. The argument will be
     * converted to string using {@link Object#toString() }, if it is not null.
     * Otherwise this method simply returns. This method ensures that any output
     * following this message begins on a new line.
     *
     * @param s the data/text to print.
     */
    public void error (final Object s) {
        checkLive();
        if (s != null) {
            if (_err_ == null) {
                outputThread().submit(new Callable<Void>() {

                    @Override
                    public Void call () throws Exception {
                        forwardOutput(StdStream.Stderr, s, true);
                        return null;
                    }
                });
            }
            else {
                _err_.println(s);
            }
        }
    }

    /**
     * Send data to the output stream of this {@link Shell shell}. The argument
     * will be converted to string using {@link Object#toString() }, if it is
     * not null. Otherwise this method simply returns. This method ensures that
     * any output following this message begins on a new line.
     *
     * @param s the data/text to print.
     */
    public void out (final Object s) {
        checkLive();
        if (s != null) {
            if (_out_ == null) {
                outputThread().submit(new Callable<Void>() {

                    @Override
                    public Void call () throws Exception {
                        forwardOutput(StdStream.Stdout, s, true);
                        return null;
                    }
                });
            }
            else {
                _out_.println(s);
            }
        }
    }

    /**
     * Send data to the error stream of this {@link Shell}. The argument will be
     * converted to string using {@link Object#toString() }, if it is not null.
     * Otherwise this method simply returns. This method does <em>not</em>
     * ensure that any output following this message begins on a new line.
     *
     * @param s the data/text to print.
     */
    public void printError (final Object s) {
        checkLive();
        if (s != null) {
            if (_err_ == null) {
                outputThread().submit(new Callable<Void>() {

                    @Override
                    public Void call () throws Exception {
                        forwardOutput(StdStream.Stderr, s, false);
                        return null;
                    }
                });
            }
            else {
                _err_.print(s);
            }
        }
    }

    /**
     * Send data to the output stream of this {@link Shell shell}. The argument
     * will be converted to string using {@link Object#toString() }, if it is
     * not null. Otherwise this method simply returns. This method does
     * <em>not</em> ensure that any output following this message begins on a
     * new line.
     *
     * @param s the data/text to print.
     */
    public void printOut (final Object s) {
        checkLive();
        if (s != null) {
            if (_out_ == null) {
                outputThread().submit(new Callable<Void>() {

                    @Override
                    public Void call () throws Exception {
                        forwardOutput(StdStream.Stdout, s, false);
                        return null;
                    }
                });
            }
            else {
                _out_.print(s);
            }
        }
    }
    //<editor-fold defaultstate="collapsed" desc="Filtered error/output">
    /**
     * {@link Boolean} preference which determines whether or not {@link Shell}
     * will emit additional feedback messages apart from return codes or
     * exceptions or absence thereof.
     */
    public static final Setting<Boolean> ChattyShell = new Setting<Boolean>(
            "cuf.shell.chatty",
            "true",
            new BooleanResource(),
            Modifiable.Conf);

    /**
     * This method filters error messages depending on whether or not the
     * “chatty shell” is activated (default case). This allows users to filter
     * out many repetitive messages and avoid clobbering the output of scripts.
     *
     * @param s the text to send to {@link #error(java.lang.Object) } depending
     * on whether or not the {@link #ChattyShell} preference is set to true.
     */
    public void chatError (Object s) {
        if (ChattyShell.get()) {
            error(s);
        }
    }

    /**
     * This method filters output messages depending on whether or not the
     * “chatty shell” is activated (default case). This allows users to filter
     * out many repetitive messages and avoid clobbering the output of scripts.
     *
     * @param s the text to send to {@link #out(java.lang.Object) } depending on
     * whether or not the {@link #ChattyShell} preference is set to true.
     */
    public void chatOutput (Object s) {
        if (ChattyShell.get()) {
            out(s);
        }
    }
    //</editor-fold>
    /**
     * {@link Boolean} preference which determines whether or not {@link Shell}
     * will show stack traces when exceptions/errors are {@link #debug(java.lang.Throwable) reported}.
     */
    public static final Setting<Boolean> ShowStackTrace = new Setting<Boolean>(
            "cuf.shell.trace",
            "true",
            new BooleanResource(),
            Modifiable.Conf);
    /**
     * {@link Boolean} preference which determines whether or not {@link Shell}
     * will log exceptions/errors which are {@link #debug(java.lang.Throwable) reported}.
     */
    public static final Setting<Boolean> LogExceptions = new Setting<Boolean>(
            "cuf.shell.debuglog", "true",
            new BooleanResource(),
            Modifiable.Conf);

    /**
     * This method provides pretty-printing of a stack trace of an {@link Exception}.
     * It sends its output to {@link #error(java.lang.Object) }.
     *
     * @param e the {@link Throwable} to print.
     */
    public void debug (Throwable e) {
        checkLive();
        if (e != null) {
            error(Makeup.Error.makeup(this, e.toString()));
            if (LogExceptions.get()) {
                Log.Warning.logp(Shell.class, "debug", e.getLocalizedMessage(),
                                 e);
            }
            if (ShowStackTrace.get()) {
                String file, cls, method, format;
                int line;
                for (StackTraceElement ste : e.getStackTrace()) {
                    line = ste.getLineNumber();
                    file = ste.getFileName();
                    cls = ste.getClassName();
                    method = ste.getMethodName();
                    format =
                            Makeup.Error.makeup(this, "\t%1$s.%2$s();", cls,
                                                method);
                    if (file != null) {
                        format += (" " + Makeup.FilePath.makeup(this, file));
                    }
                    if (line >= 0) {
                        format += Makeup.Error.makeup(this, " #%1$d", line);
                    }
                    error(format);
                }
            }
        }
    }

    private String readLine () {
        try {
            String result = readLineImpl();
            if (result == null) {
                throw new Exception(Messages.PromptNoInputError.getText());
            }
            return result;
        }
        catch (Exception e) {
            chatError(Makeup.Error.makeup(this,
                                          Messages.PromptUnexpectedError,
                                          e));
            return null;
        }
    }

    /**
     * Cause the {@link StdStream#Stdout STDOUT} stream of this {@link Shell shell}
     * to be flushed.
     */
    public void flushOutput () {
        checkLive();
        if (_out_ == null) {
            outputThread().submit(new Callable<Void>() {

                @Override
                public Void call () throws Exception {
                    forwardFlush(StdStream.Stdout);
                    return null;
                }
            });
        }
        else {
            _out_.flush();
        }
    }

    /**
     * Cause the {@link StdStream#Stderr STDERR} stream of this {@link Shell shell}
     * to be flushed.
     */
    public void flushError () {
        checkLive();
        if (_err_ == null) {
            outputThread().submit(new Callable<Void>() {

                @Override
                public Void call () throws Exception {
                    forwardFlush(StdStream.Stderr);
                    return null;
                }
            });
        }
        else {
            _err_.flush();
        }
    }

    private void checkLive () {
        if (!isLive) {
            throw new IllegalStateException(Messages.ShellNecromancerAlert.
                    format(boundName, exitCode));
        }
    }

    /**
     * Display a prompt string.
     *
     * @param ps the string that prompts the user for input.
     */
    protected abstract void displayPrompt (final String ps);

    /**
     * Read a line of input from the STDIN stream of this {@link Shell}.
     *
     * @return the line read, possibly null if an end-of-file is reached
     * (ctrl+D).
     * @throws Exception if any error occurs
     */
    protected abstract String readLineImpl () throws Exception;

    /**
     * Prompt for input. This method will block until this {@link Shell shell}
     * is given input “focus” and it receives a line of input (or an
     * end-of-file).
     *
     * @param s the prompt.
     * @return a line of whatever text is entered at the prompt. This will be
     * null if an end-of-file is reached (ctrl+D).
     * @throws InterruptedException if this thread is interrupted while waiting
     * for either condition.
     */
    public String prompt (final String s) throws InterruptedException {
        checkLive();
        lock.acquire();
        inputThread().submit(new Callable<Void>() {

            @Override
            public Void call () throws Exception {
                displayPrompt(s);
                final String result = readLine();
                queue.add(new PromptResult() {

                    @Override
                    public String get () {
                        return result;
                    }
                });
                lock.release();
                return null;
            }
        });
        return queue.take().get();
    }
    private static String nextprompt = "" + PromptSettings.ContinuePromptChar.
            get();

    /**
     * A callback used to receive signals of a particular type.
     *
     * @param <S> type of object to receive in {@link #callback(java.lang.Object)
     * }.
     */
    public static interface CallBack<S> {

        /**
         * Receive a signal.
         *
         * @param signal the object to receive.
         */
        public void callback (S signal);
    }

    /**
     * Get the {@link OutputService} used to multiplex all output to STDOUT from
     * all
     * {@link Shell subshells} of this particular type.
     *
     * @return the {@link OutputService} used to submit output to.
     */
    protected abstract OutputService outputThread ();

    /**
     * Get the {@link InputService} used to multiplex all calls for input from
     * STDIN from all
     * {@link Shell subshells} of this particular type.
     *
     * @return the {@link InputService} used to receive input from.
     */
    protected abstract InputService inputThread ();

    /**
     * Write output to a given stream.
     *
     * @param type the {@link StdStream type of stream to write to}.
     * @param toWrite the object to write.
     * @param ln whether or not output following this message must start on a
     * new line.
     * @throws Exception if any error occurs
     */
    protected abstract void forwardOutput (final StdStream type,
                                           final Object toWrite,
                                           final boolean ln) throws Exception;

    /**
     * Flush a particular output stream.
     *
     * @param stream the {@link StdStream type of stream to flush}.
     * @throws Exception if any error occurs.
     */
    protected abstract void forwardFlush (StdStream stream) throws Exception;

    //<editor-fold defaultstate="collapsed" desc="markup logic">
    /**
     * Return default makeup/reset codes.
     *
     * @param type the {@link EscapeType} currently used by the current {@link Shell}.
     * @return whatever incantation resets the current style to defaults.
     */
    public static String defaultMakeup (EscapeType type) {
        switch (type) {
            case ANSI:
                return "0";
            case HTML:
                return "background:none;color:none;"
                        + "font-style:normal;font-weight:normal;"
                        + "text-decoration:none;";
            default:
                return "";
        }
    }

    /**
     * Return the bits of code that begin an “escape sequence”.
     *
     * @param type the {@link EscapeType} currently used by the current {@link Shell}.
     * @return whatever code begins style information
     */
    public static String makeupOpenTag (EscapeType type) {
        switch (type) {
            case ANSI:
                return "\033[";
            case HTML:
                return "<code style=\"font-family:monospace;";
            default:
                return null;
        }
    }

    /**
     * Return the bits of code that close an “escape sequence”.
     *
     * @param type the {@link EscapeType} currently used by the current {@link Shell}.
     * @return whatever code marks the end of style information
     */
    public static String makeupCloseTag (EscapeType type) {
        switch (type) {
            case ANSI:
                return "m";
            case HTML:
                return "\">";
            default:
                return null;
        }
    }
    /**
     * {@link EscapeType} preference determining how text can be made up with
     * colours and styles.
     */
    public static final Setting<EscapeType> EscapeSequenceType = new Setting<EscapeType>(
            "cuf.shell.escapes",
            "NONE",
            new EnumConstant(
            EscapeType.class),
            Modifiable.Conf);

    /**
     * Decorate text/data with style and colour effects.
     *
     * @param text the text to style
     * @param style the font {@link Style} to use
     * @param fg the {@link Foreground} colour to use.
     * @param bg the {@link Background} colour to use.
     * @return the decorated version of the input text
     */
    public String makeup (Object text, Style style, Foreground fg,
                          Background bg) {
        if (text == null) {
            return null;
        }

        EscapeType type = EscapeSequenceType.get();
        if (type == EscapeType.NONE) {
            return text.toString();
        }
        String close = makeupCloseTag(type);
        String open = makeupOpenTag(type);
        String print = close == null ? text.toString() : close + text.toString();
        if (style == null && fg == null && bg == null) {
            print = defaultMakeup(type) + print;
        }
        else {
            boolean add = false;
            EscapeSequence[] esc = new EscapeSequence[] { style,
                                                          fg,
                                                          bg };
            for (EscapeSequence e : esc) {
                if (e != null) {
                    print = e.sequence(type) + (add ? ";" : "") + print;
                    add = true;
                }
            }
        }
        return open == null ? print : open + print;
    }

    /**
     * Resest the makeup to the defaut.
     *
     * @param append the text to append, if null is given the empty string is
     * used instead.
     * @return the necessary output to reset makeup to its default, plus
     * appended text if given.
     */
    public String makeupReset (Object append) {
        return makeup(append == null ? "" : append, null, null, null);
    }

    /**
     * Provides standard style combinations to graphically “makeup” messages.
     */
    public enum Makeup {

        /**
         * Standard “progress” message. This {@link Makeup} makes them bold red.
         */
        Error(Foreground.RED, null, Style.BOLD),
        /**
         * Standard “warning” message. This {@link Makeup} makes them bold
         * yellow.
         */
        Warning(Foreground.YELLOW, null, Style.BOLD),
        /**
         * Standard “progress” message. This {@link Makeup} makes them green.
         */
        Progress(Foreground.GREEN, null, null),
        /**
         * Standard “plain” message.
         */
        Plain(null, null, null),
        /**
         * Standard “file path” makeup. This {@link Makeup} adds an underline.
         */
        FilePath(null, null, Style.UNDERLINE),
        /**
         * Standard “notification” message. This {@link Makeup} makes them blue.
         */
        Notification(Foreground.BLUE, null, null);
        private final Foreground fg;
        private final Background bg;
        private final Style style;

        private Makeup (Foreground fg, Background bg, Style s) {
            this.fg = fg;
            this.bg = bg;
            this.style = s;
        }

        private String impl (Shell s, String text) {
            return s.makeup(text, style, fg, bg);
        }

        /**
         * Provides a standard style for litteral messages.
         *
         * @param s the shell which provides makeup implementation.
         * @param text message to style
         * @return the input message with makeup applied.
         */
        public String makeup (Shell s, String text) {
            return impl(s, text) + s.makeupReset(null);
        }

        /**
         * Provides a standard style for formatted messages.
         *
         * @param s the shell which provides makeup implementation.
         * @param msg message to style (format string)
         * @param args arguments to the message (may be null)
         * @return the formatted input message with makeup applied.
         */
        public String makeup (Shell s, String msg, Object... args) {
            return makeup(s, String.format(msg, args));
        }

        /**
         * Convenience method for localised messages.
         *
         * @param s the shell which provides makeup implementation.
         * @param key the {@link BundleKey} to makeup.
         * @param args the arguments to the localised format string.
         * @return the localised string formatted and with makeup applied.
         */
        public String makeup (Shell s, BundleKey key, Object... args) {
            return makeup(s, key.format(args));
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Toolkit">
    /**
     * Base class for toolkits.
     *
     * @author Johan Ouwerkerk
     */
    public static class Toolkit {

        private final PathParser parser;

        /**
         * Creates a new {@link Toolkit}.
         *
         * @param context a {@link PathParser} which provides context for
         * resolving relative file names.
         */
        public Toolkit (PathParser context) {
            this.parser = context;
        }

        /**
         * Creates a new {@link Toolkit}.
         *
         * @param context the {@link Shell} which provides context for resolving
         * relative file names.
         */
        public Toolkit (Shell context) {
            this(context.pathParser());
        }

        /**
         * Auxiliary method to use the context {@link PathParser} for parsing a
         * file path.
         *
         * @param file path to parse.
         * @return a {@link File} representing the parsed path.
         */
        public File file (String file) {
            return parser.parsePath(file);
        }

        /**
         * Get the {@link PathParser} context used for {@link #file(java.lang.String) resolving file names}.
         *
         * @return the {@link PathParser} used by this {@link Toolkit}.
         */
        protected PathParser parser () {
            return parser;
        }
    }
    //</editor-fold>
}
