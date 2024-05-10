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

import org.europabarbarorum.cuf.support.CompileJob.FileJob;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 * A list of {@link BundleKey} messages used in the
 * {@code org.europabarbarorum.cuf.support} package.
 * @author Johan Ouwerkerk
 */
public enum Messages implements BundleKey {

    /**
     * Emitted when the user attempts to load a file with a different MIME type than
     * expected. Takes file name and description of the MIME type found as parameters.
     */
    MimeTypeInvalid(MimeTag.class),
    /**
     * Emitted when a {@link BundleKey} cannot be formatted.
     * Takes key name, bundle classname, and resource value as arguments.
     */
    ResourceFormatError(ResourceHelp.class),
    /**
     * Emitted when an illegal {@link DefaultOption} is encountered in the strings
     * subsystem. Takes the canonical option type and
     * encountered error message as arguments.
     */
    IllegalOptionType(OptionMap.class),
    /**
     * Emitted when a value is supplied for a {@link DefaultOption} type key which
     * is not itself an instance of the same {@link DefaultOption} type.
     * Takes canonical type name, encountered value and default option value as arguments.
     */
    InvalidOptionValue(OptionMap.class),
    /**
     * Emitted when a {@link IllegalMethodCallException} is thrown.
     * Takes no arguments
     */
    IllegalMethodCall(IllegalMethodCallException.class),
    /**
     * Emitted when feature support was compiled in but feature support is not
     * available at run time <em>or</em> if feature support was disabled at
     * compile time. Takes a feature name argument.
     */
    FeatureInitError(AbstractFeature.class),
    /**
     * Emitted when feature support is invoked even though feature support was
     * disabled at compile time. Takes a feature name argument.
     */
    FeatureNotCompiledError(AbstractFeature.class),
    /**
     * Emitted when a feature fails a check on its meta information. Takes a feature name as argument.
     */
    NoFeatureFound(AbstractFeature.class),
    /**
     * Emitted when a feature implementation fails to initialise.
     * Takes a feature name, class name, and details message as arguments.
     */
    FeatureProviderInvalid(AbstractFeature.class),
    /**
     * Emitted when a feature implementation is not of the right type.
     * Takes feature name, feature class and required base class arguments.
     */
    FeatureProviderTypeError(AbstractFeature.class),
    /**
     * Emitted when a feature implementation fails to initialise.
     */
    FeatureProviderInitError(AbstractFeature.class),
    /**
     * Emitted when parsing context cannot be set.
     * Takes a context file and home directory argument.
     */
    ResetGlobContext(PathParser.Glob.class),
    /**
     * Emitted when a path cannot be fully resolved (made unambiguous).
     * Takes a file path and an error details message argument.
     */
    PathResolveError(PathParser.class),
    /**
     * Emitted when a path cannot be opened with a reader.
     * Takes a file path and an error details message argument.
     */
    ReaderInvalidFileError(PathParser.class),
    /**
     * Emitted when a path cannot be opened with a writer.
     * Takes a file path and an error details message argument.
     */
    WriterInvalidFileError(PathParser.class),
    /**
     * Emitted when a path cannot be removed.
     * Takes a file path argument.
     */
    RemoveInvalidFileError(PathParser.class),
    /**
     * Emitted when a file is created for storing temporary results.
     * Takes no arguments.
     */
    Tempfile(FileJob.class),
    /**
     * Emitted when compilation finishes. Takes no arguments.
     */
    CompilationDone(FileJob.class),
    /**
     * Emitted when compilation is aborted. Takes an exception argument.
     */
    CompilationAborted(FileJob.class),
    /**
     * Emitted when a syntax error is encountered in a string parsed as character array.
     * Takes the index, the expected character and the offending character found as parameters.
     */
    CharacterArraySyntaxError(ResourceHelp.CharacterArrayResource.class),
    /**
     * Emitted when a resource string is not of the right length to be a valid
     * character array. Takes the length of the string as parameter.
     */
    CharacterArrayLengthError(ResourceHelp.CharacterArrayResource.class),
    /**
     * Emitted when a {@link BundleKey} cannot be used to lookup a resource.
     * Takes a name, bundle and key argument.
     */
    NoSuchResource(ResourceHelp.class),
    /**
     * Emitted when an edit operation is invoked on read-only data.
     * Takes no arguments.
     */
    NotEditableMessage(NotEditableException.class),
    /**
     * Emitted when a file cannot be read to obtain (enough) Mime information.
     * Takes a file path and an error details message argument.
     */
    UnexpectedMimeError(MimeTag.class),
    /**
     * Emitted when no classpath configuration properties file can be found.
     * Takes path, and error message arguments.
     */
    NoExpansionPath(Classloader.class),
    /**
     * Emitted when a classpath entry can not be found.
     * Takes 4 string arguments: raw path element, expanded path element, base dir, property key.
     */
    ExpansionPathInvalid(Classloader.class),
    /**
     * Emitted when a classpath entry has been added to the runtime classpath.
     * Takes a single argument: the URL added.
     */
    ExpansionAdded(Classloader.class),
    /**
     * Emitted when a {@link Classloader} pushes an expansion path up the
     * loader hierarchy. Takes an expansion path argument.
     */
    ExpansionPushed(Classloader.class),
    /**
     * Emitted when a feature class is registed with the {@link Classloader}.
     * Takes a class name argument.
     */
    FeatureRegistered(Classloader.class),
    /**
     * Emitted when a class is loaded.
     * Takes class name and code base URL as arguments.
     */
    ClassLoaded(Classloader.class),
    /**
     * Emitted when the classpath config file can not be found.
     * Takes file path as argument.
     */
    ClasspathConfNotFound(Classloader.class),
    /**
     * Emitted when a setting value is null (empty).
     */
    SettingValueInvalid(Setting.class),
    /**
     * Emitted when a new setting value cannot be parsed.
     * Takes a setting name, old value, new value, and error message as arguments.
     */
    SettingParseError(Setting.class),
    /**
     * Emitted when the settings collection cannot be cleared in its entirety.
     * Takes an error message argument.
     */
    SettingCleanError(Setting.class),
    /**
     * Emitted when a setting cannot be cleared.
     * Takes setting name and error message as arguments.
     */
    SettingClearError(Setting.class);

    private Messages (Class type) {
        this.type = type;
    }
    private final Class type;

    @Override
    public String getText () {
        return ResourceHelp.getValue(this, Messages.class);
    }

    @Override
    public String format (Object... args) {
        return ResourceHelp.formatValue(this, Messages.class, args);
    }

    @Override
    public Class type () {
        return type;
    }
}
