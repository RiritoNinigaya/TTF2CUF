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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * An interface to describe various convenient shortcuts on Java file I/O operations that the
 * a subshell exposes through an auxilliary object.
 * The main points are to avoid the need to wrap IO streams; and provide path name expansion in Strings.
 * @author Johan Ouwerkerk
 */
public interface PathParser {

    /**
     * Provide pathname expansion.
     * A leading tilde and directory delimiter (file separator) is expanded to the user's
     * home directory. E.g. "~/" is expanded to "/home/doe/" but "~" is not.
     * @param path input string
     * @return a {@link File} object that represents this path.
     */
    File parsePath (String path);

    /**
     * Resolve an arbitrary {@link File} path against the context path if the given one is
     * not absolute. What exactly an absolute file path is depends on the underlying JVM/OS.
     * @param relative the {@link File} to resolve.
     * @return an absolute, resolved {@link File}.
     */
    File resolveFile (File relative);

    /**
     * Sets the context directory used to interpret parse relative file paths.
     * @param ctx a file which resides in this directory, or the directory itself
     * @throws InvalidFileException
     * if a given relative file path cannot be converted to its canonical form.
     */
    void setContext (File ctx) throws InvalidFileException;

    /**
     * Get working directory.
     * @return the context directory used to interpret relative file paths.
     */
    File getPWD ();

    /**
     * Open a file for writing;
     * optionally appends to the end of the file instead of truncating a file should it exist.
     * @param path the path to the file to write
     * @param append true to append to the file, false to truncate it if it should exist.
     * @return a {@link PrintWriter} to write file contents to.
     * @throws InvalidFileException if the file could not be opened
     * for writing
     */
    PrintWriter writer (String path, boolean append) throws InvalidFileException;

    /**
     * Removes a file from disk. Some language interpreters (notably the JavaScript interpreter supplied by
     * the SUN JVM) want to prevent you from removing files from disk. Why that would be is beyond me since they
     * are otherwise not exactly a straitjacket of security??
     * @param path the path to parse. Is sent to {@link #parsePath(java.lang.String) } for convenience.
     * @return true if the removal was successful, false if not.
     * @throws InvalidFileException if the file could not be deleted
     */
    boolean remove (String path) throws InvalidFileException;

    /**
     * Open a file for reading.
     * @param filepath the path to the file to open. This argument is parsed with {@link #parsePath(java.lang.String) }
     * for convenience.
     * @return a {@link BufferedReader} to read the file from.
     * @throws InvalidFileException if the file could not be opened
     * for reading
     */
    public BufferedReader reader (String filepath) throws InvalidFileException;

    /**
     * Resolve symlinks and similar layers of indirection in a file path.
     * @param path 
     * @return a {@link File} that is the canonical equivalent of the given file.
     * @throws InvalidFileException if the file cannot be resolved
     * to its canonical form
     */
    public File canonise (File path) throws InvalidFileException;

    /**
     * An implementation of {@link PathParser} with a some simple wildcards.
     */
    public static class Glob implements PathParser {

        /**
         * Create a default {@link Glob} instance using the current working directory
         * for context.
         */
        public Glob () {
            setContext(null);
        }

        /**
         * Create a {@link Glob} instance for the given context.
         * @param ctx a context {@link File} used for parsing relative file paths.
         */
        public Glob (File ctx) {
            setContext(ctx);
        }

        /**
         * Method that implements the wildcard expansion in path names.
         * @param path the path to parse
         * @return a {@link File} constructed from the expanded path.
         */
        protected File glob (String path) {
            String prefix = "~" + File.separator;
            if (path.startsWith(prefix)) {
                path = System.getProperty("user.home") + path.substring(1);
            }
            return new File(path);
        }
        private File ctx;

        @Override
        final public void setContext (File ctx) {
            if (ctx != null) {
                try {
                    this.ctx = basedir(ctx.isAbsolute() ? ctx : canonise(ctx));
                    return;
                }
                catch (InvalidFileException e) {
                    IOHelp.handleExceptions(Glob.class,
                                            "Glob.setContext",
                                            e,
                                            Messages.ResetGlobContext,
                                            ctx,
                                            System.getProperty("user.dir"));
                }
            }
            this.ctx = basedir(new File(System.getProperty("user.dir")));
        }

        @Override
        public File getPWD () {
            return ctx;
        }

        /**
         * Get a root directory from the given file path.
         * Different platforms may exhibit different behaviour w.r.t. root directories.
         * @param f the file path to resolve to a root directory.
         * @return a the root directory of the given file.
         */
        protected File getRoot (File f) {
            File p = f.getParentFile();
            return p == null ? f : getRoot(p);
        }

        /**
         * Get base directory from the given file path.
         * @param f the file path to use
         * @return the file itself if it is a directory,
         * or the directory which is given as the parent file
         */
        protected File basedir (File f) {
            if (f == null) {
                return null;
            }
            if (f.isDirectory()) {
                return f;
            }
            return basedir(f.getParentFile());
        }

        @Override
        public File canonise (File path) throws InvalidFileException {
            try {
                return path.getCanonicalFile();
            }
            catch (Exception e) {
                throw new InvalidFileException(e,
                                               Messages.PathResolveError,
                                               path,
                                               e.getLocalizedMessage());
            }
        }

        @Override
        public File parsePath (String path) {
            File f = glob(path);
            return resolveFile(f);
        }

        @Override
        public File resolveFile (File f) {
            return f.isAbsolute() ? f : new File(getPWD(), f.toString());
        }

        @Override
        public PrintWriter writer (String filepath, boolean append) throws
                InvalidFileException {
            try {
                return new PrintWriter(new FileWriter(parsePath(filepath),
                                                      append),
                                       true);
            }
            catch (Exception e) {
                throw new InvalidFileException(e,
                                               Messages.WriterInvalidFileError,
                                               filepath,
                                               e.getLocalizedMessage());

            }
        }

        @Override
        public BufferedReader reader (String in) throws InvalidFileException {
            return reader(parsePath(in));
        }

        /**
         * Equivalent of {@link #reader(java.lang.String) }.
         * @param in the {@link File} to open.
         * @return a {@link BufferedReader} to read from the file.
         * @throws InvalidFileException if the file cannot be opened
         */
        public BufferedReader reader (File in) throws InvalidFileException {
            try {
                return new BufferedReader(new FileReader(in));
            }
            catch (Exception e) {
                throw new InvalidFileException(e,
                                               Messages.ReaderInvalidFileError,
                                               in,
                                               e.getLocalizedMessage());
            }
        }

        @Override
        public boolean remove (String path) throws InvalidFileException {
            File f = parsePath(path);
            if (!f.exists()) {
                throw new InvalidFileException(Messages.RemoveInvalidFileError,
                                               path);
            }
            return f.delete();
        }
    }

    /**
     * An exception type to signal errors in {@link PathParser}
     * with localised error messages. This type of exception is used in favour of the various exceptions
     * throw by the java.io layers.
     */
    public static class InvalidFileException extends Exception {

        /**
         * Create an {@link InvalidFileException} without an underlying exception as root cause.
         * @param m a {@link BundleKey} that provides a format string for the exception message.
         * @param args arguments to the given format string message.
         */
        public InvalidFileException (BundleKey m, Object... args) {
            super(m.format(args));
        }

        /**
         * Create an {@link InvalidFileException} with an underlying exception as root cause.
         * @param cause the {@link Exception} that caused this {@link InvalidFileException} to be thrown.
         * @param m a {@link BundleKey} that provides a format string for the exception message.
         * @param args arguments to the given format string message.
         */
        public InvalidFileException (Exception cause, BundleKey m,
                                     Object... args) {
            super(m.format(args), cause);
        }
    }
}
