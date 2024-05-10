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
 *
 * @author Johan Ouwerkerk
 */
public enum Messages implements BundleKey {

    /**
     * Emitted when a jump is requested to the index of a non-existing subshell.
     * Takes the given index and number of shells as arguments.
     * @see Shell#jumpTo(int) 
     */
    JumpToInvalidIndex(Shell.class),
    /**
     * Emitted when a {@link Shell shell} is asked to start running when it is already running.
     * Takes the name of the shell as argument.
     */
    StartShellError(Shell.class),
    /**
     * Emitted when a script forked exits with a custom exit code and
     * the program runs as {@link ScriptRunner}. Takes source path and
     * exit code as arguments.
     */
    GenericScriptExit(ScriptRunner.class),
    /**
     * Emitted when a subshell is created without a valid script name.
     * Takes no arguments.
     */
    NoScriptNameError(Shell.class),
    /**
     * Emitted when a subshell is created without a valid {@link CompileUnit} task.
     */
    NoCompileUnitError(Shell.class),
    /**
     * Message to say the first shell was not preloaded.
     * Takes query argument.
     */
    StartupPreloadOmitted(PreloadScript.class),
    /**
     * Error/warning when the language environment does not support the method invocation API (javax.script.invocable).
     * Takes no arguments.
     */
    NotInvocableError(Shell.class),
    /**
     * Emitted when a user attempts to exit a shell with a code reserved
     * for internal use. Takes given and new error codes as arguments.
     * @see Shell#exit(int)
     */
    ReservedExitCodeError(Shell.class),
    /**
     * Emitted when a subshell is created without STDIN stream.
     * Takes no arguments.
     */
    NoInputStreamError(Shell.class),
    /**
     * Error when a shell calls exit on a subshell that has already exited.
     * Takes name of the callee, exit code of the callee and name of the calling shell as parameters.
     */
    ExitIllegalError(Shell.class),
    /**
     * Message when the user attempts to obtain an exit code from a subshell
     * when that shell is still active. Takes shell name argument.
     */
    NoExitCodeError(Shell.class),
    /**
     * Error when the user attempts to link against a non-existing lib.
     * Takes given file name as argument.
     * @see Shell#link(java.lang.String) 
     */
    LinkNonExistingError(Shell.class),
    /**
     * Error when the user attempts to link against a lib that cannot be read from.
     * Takes a file name as argument.
     * @see Shell#link(java.lang.String)
     */
    LinkUnreadableError(Shell.class),
    /**
     * Error when the user attempts to link against something that isn't a
     * ZIP file. Takes file path and mime type found as arguments.
     * @see Shell#link(java.lang.String)
     */
    LinkTypeInvalidError(Shell.class),
    /**
     * Emitted when the java.class.path property contains an entry for this lib already.
     * Takes a file name as argument.
     * @see Shell#link(java.lang.String)
     */
    LinkClasspathError(Shell.class),
    /**
     * Emitted when the user has already linked to the lib before.
     * Takes a file name as argument.
     * @see Shell#link(java.lang.String)
     */
    LinkLinksError(Shell.class),
    /**
     * Emitted when lib is successfully linked against.
     * Takes file name as argument.
     * @see Shell#link(java.lang.String)
     */
    LinkSuccessful(Shell.class),
    /**
     * Emitted when the god of "linking against 3rd party libs" is angry with you.
     * Takes file path and error message arguments.
     * @see Shell#isLinked(java.lang.String) 
     * @see Shell#link(java.lang.String) 
     */
    LinkURLError(Shell.class),
    /**
     * Error when no interpreter matches the given query: takes query argument.
     */
    InitNoEnvironmentError(Shell.class),
    /**
     * Error when the language environment fails to return a MIME type when queried for it.
     * Takes exception argument.
     */
    InitNoMimeError(Shell.class),
    /**
     * Dummy value to mark a missing MIME type: takes no arguments.
     * @see #InitNoMimeError
     */
    InitDefaultMime(Shell.class),
    /**
     * Error when the language environment does not return a file extension when queried for it.
     * Takes exception argument.
     */
    InitNoExtensionError(Shell.class),
    /**
     * Dummy value to mark a missing file extension: takes no arguments.
     * @see #InitNoExtensionError
     */
    InitDefaultExtension(Shell.class),
    /**
     * Emitted when an unexpected error occurs inside shell logic.
     * Takes shell name and details message as arguments.
     */
    UnexpectedError(Shell.class),
    /**
     * Emitted when a user attempts to block a shell on its own thread (deadlock) or 
     * if the user attempts to block the EDT (GUI freeze). Takes thread name and id of the
     * current thread as arguments.
     * @see Shell#blockUntilExit(org.europabarbarorum.cuf.shell.Shell, org.europabarbarorum.cuf.shell.Shell.CallBack) 
     */
    ThreadBlockError(Shell.class),
    /**
     * Error emitted when the exit eventslot of a {@link Shell} is already taken.
     * Takes no arguments.
     */
    ExitSlotTaken(Shell.class),
    /**
     * Error emitted when the {@link Shell.CallBack} to be registered on the exit
     * eventslot of a {@link Shell} is invalid (null). Takes no arguments.
     */
    InvalidCallBack(Shell.class),
    /**
     * Welcome message.
     * @see Shell#welcome()
     */
    InitWelcome(Shell.class),
    /**
     * Warning when an expected preload script is not found/unreadble.
     * Takes query, loaded environment and expected file path as arguments.
     */
    NoSuchPreloadScript(PreloadScript.class),
    /**
     * Evaluated command is invalid: takes command & details message arguments.
     */
    EvalInvalidError(Shell.class),
    /**
     * Evaluated script is invalid: takes file name & details message arguments.
     */
    InterpretInvalidError(Shell.class),
    /**
     * Error when the input stream abruptly stops. Takes no arguments.
     */
    PromptNoInputError(Shell.class),
    /**
     * Generic error when unable to read input. Takes no arguments.
     */
    PromptUnexpectedError(Shell.class),
    /**
     * Warning when infinite loop as result of null input is detected and
     * it can be prevented by jumping to the next subshell. Takes current shell
     * name and index of the next subshell as arguments.
     */
    PromptAutoJump(Shell.class),
    /**
     * Emitted when an infinite loop as result of null input is detected and it
     * can only be prevented by system exit. Takes current shell name and error
     * code as arguments.
     */
    PromptAutoExit(Shell.class),
    /**
     * Error message of the exception thrown when the user attempts to resurrect a dead shell.
     * Takes name of the dead shell and its exit code as parameters.
     */
    ShellNecromancerAlert(Shell.class),
    /**
     * Error emitted when a scheduled task is run from a different thread than the one it
     * was schedule from. Takes id, and name of the scheduling thread as well as
     * id and name of the executing thread as arguments.
     */
    ScheduleInvalidError(Shell.class),
    /**
     * Format of the window title for a strings.bin preview against a given font.
     * Takes a strings file name and a
     * {@link org.europabarbarorum.cuf.font.CUFSource#getCufSource() CUF source identifier} as
     * arguments.
     */
    StringsPreviewTitle(StringsToolkit.class),
    /**
     * Error when the user attempts to generate an ordered Strings file from
     * unordered data. Takes name of the expected data type and the name of the data type
     * found as arguments.
     * @see StringsToolkit#fromMap(java.util.Map, org.europabarbarorum.cuf.strings.impl.KeyResolver, boolean) 
     */
    OrderedSourceTypeMismatch(StringsToolkit.class),
    /**
     * Emitted when the string that represents start of range is in fact empty.
     * Takes no arguments.
     * @see FontToolkit#restrict(org.europabarbarorum.cuf.font.CUFSource, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    FontStartCharStringInvalid(FontToolkit.class),
    /**
     * Emitted when the string that represents end of range is in fact empty.
     * Takes no arguments.
     * @see FontToolkit#restrict(org.europabarbarorum.cuf.font.CUFSource, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    FontEndCharStringInvalid(FontToolkit.class),
    /**
     * Emitted when the user attempts to mix multiple fonts into one,
     * without providing any fonts at all. Takes no arguments.
     * @see FontToolkit#mix(org.europabarbarorum.cuf.font.CUFSource[], org.europabarbarorum.cuf.font.MixedSource.MixinMode[], java.lang.String)
     */
    SourcesListInvalid(FontToolkit.class),
    /**
     * Emitted when the user attempts to mix multiple fonts into one, without
     * providing a corresponding list of flags that indicate how fonts should
     * be mixed. Takes no arguments.
     * @see FontToolkit#mix(org.europabarbarorum.cuf.font.CUFSource[], org.europabarbarorum.cuf.font.MixedSource.MixinMode[], java.lang.String)
     */
    MixinModeListInvalid(FontToolkit.class),
    /**
     * Emitted when the user attempts to load a file type that is recognised as a font,
     * but not supported by the program. 
     * Takes a file name and its MIME type as arguments.
     * @see FontToolkit#fromFile(java.lang.String, java.util.Map) 
     */
    MimeTypeNotSupportedError(FontToolkit.class),
    /**
     * Emitted when the user loads a TTF or similar file from disk without specifying additional
     * font attributes (e.g.: size, stroke width, and stroke weight).
     * @see FontToolkit#fromFile(java.lang.String, java.util.Map) 
     */
    NoAttributesWarning(FontToolkit.class),
    /**
     * Emitted when the user attempts to load a file that cannot be read.
     * @see FontToolkit#fromFile(java.lang.String, java.util.Map) 
     */
    FontFileUnreadable(FontToolkit.class),
    /**
     * Message when a user attempts to create a compile unit without valid
     * compilation job. Takes no arguments.
     */
    InvalidCompileJob(CompileUnit.class),
    /**
     * Format of the window title for a font preview
     * Takes a {@link org.europabarbarorum.cuf.font.CUFSource#getCufSource() CUF source identifier}
     * as argument.
     */
    FontWindowTitleFormat(FontToolkit.class);

    private Messages (Class type) {
        this.type = type;
    }
    private final Class type;

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, Messages.class);
    }

    @Override
    public Class type () {
        return type;
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, Messages.class, args);
    }
}
