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

import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * Enumeration of exit codes reserved by for internal purposes. In general,
 * users of the CUF shells are prevented from causing the shell to exit with such a code.
 * The exception is {@link #EXIT_OK_CODE}.
 * @author Johan Ouwerkerk
 */
public enum ReservedExitCode implements BundleKey {

    /**
     * Exit with the success code (0). This code is used by default exit() methods of subshells.
     * @see #exitCode()
     */
    EXIT_OK_CODE(0x0),
    /**
     * Exit with a code specifying that a forked subshell was unable to locate its input file.
     */
    EXIT_NO_FILE(0x1F),
    /**
     * Exit with a code specifying that a subshell attempted to use a reserved exit code other than
     * {@link #EXIT_OK_CODE}.
     * @see #exitCode()
     */
    EXIT_ILLEGAL_CODE(0x2F),
    /**
     * Exit with a code specifying that a subshell attempted to use {@link #EXIT_ILLEGAL_CODE}.
     * @see #exitCode()
     */
    EXIT_ILLEGAL_CODE2(0x3F),
    /**
     * Exit with a code specifying that a subshell attempted was terminated due to lack of input
     * (premature End Of File) after it remained the only active (sub) shell.
     * @see #exitCode()
     */
    EXIT_NO_INPUT(0x4F),
    /**
     * Exit with status code that signals a compilation task failed.
     * @see org.europabarbarorum.cuf.support.CompileJob.CompileListener#fail(java.lang.Exception)
     */
    EXIT_COMPILE_FAIL(0x5F),
    /**
     * Exit with status code that signals a compilation task was cancelled.
     * @see org.europabarbarorum.cuf.support.CompileJob.CompileListener#abort(java.lang.Exception)
     */
    EXIT_COMPILE_ABORT(0x6F),
    /**
     * Exit with a status code that signals a shell was killed.
     * @see org.europabarbarorum.cuf.shell.ShellX#killAll()
     */
    EXIT_SHELL_KILLED(0x7F);

    private ReservedExitCode (int code) {
        this.code = code;
    }
    private final int code;

    /**
     * Find the {@link ReservedExitCode} matching the given exit code.
     * @param i the given code.
     * @return an {@link ReservedExitCode} object if found, or null if the code does not match any.
     */
    public static ReservedExitCode fromCode (int i) {
        for (ReservedExitCode r : ReservedExitCode.values()) {
            if (r.code == i) {
                return r;
            }
        }
        return null;
    }

    /**
     * Get the actual integer exit code.
     * @return the exit code to use.
     */
    public int exitCode () {
        return code;
    }

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, ReservedExitCode.class);
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, ReservedExitCode.class, args);
    }

    @Override
    public Class type () {
        return ReservedExitCode.class;
    }
}
