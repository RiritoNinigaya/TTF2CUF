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
package org.europabarbarorum.cuf.strings.impl;

import org.europabarbarorum.cuf.strings.impl.KeyResolver.KeyResolverImpl;
import org.europabarbarorum.cuf.strings.impl.RawHandler.LineBreakOption;
import org.europabarbarorum.cuf.strings.impl.RawHandler.NullCharacterOption;
import org.europabarbarorum.cuf.strings.impl.RawHandler.TabOption;
import org.europabarbarorum.cuf.strings.impl.SAXHandler.IgnorableWhitespaceOption;
import org.europabarbarorum.cuf.strings.impl.StringMapping.BasicMapping;
import org.europabarbarorum.cuf.strings.impl.URIResolver.PreConfiguredURIResolver;
import org.europabarbarorum.cuf.support.ResourceHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.BundleKey;

/**
 *
 * @author Johan Ouwerkerk
 */
public enum Messages implements BundleKey {

    /**
     * Emitted when an unexpected {@link Exception} occurs in a {@link RawHandler}.
     * Takes key string,
     * event index,
     * event character (or description), context string, and error message as arguments.
     */
    DebugException(RawHandler.class),
    /**
     * Emitted when a {@link WidthMapping} is configured with a tab char width of less than 1.
     * Takes the given width as argument.
     */
    WidthMappingTabWidthError(WidthMapping.class),
    /**
     * Emitted when a {@link WidthMapping} is configured with a tab char width that is not 1.
     * Takes the given width as argument.
     */
    WidthMappingTabWidthWarning(WidthMapping.class),
    /**
     * Emitted when a {@link WidthMapping} is configured with an invalid value for some attribute.
     * Takes the following arguments:
     * <ol>
     * <li>The attribute name</li>
     * <li>The attribute value</li>
     * <li>A description of the attribute</li>
     * <li>Error message describing the error encountered</li>
     * </ol>
     */
    WidthMappingAttributeInvalid(WidthMapping.class),
    /**
     * Emitted when a {@link RawHandler} converts a carriage return
     * to a line feed. Takes key string, event index and context string as arguments.
     */
    LineBreakConvertWarning(LineBreakOption.class),
    /**
     * Emitted when a {@link RawHandler} discards a carriage return character.
     * Takes key string, event index and context string as arguments.
     */
    LineBreakDiscardCRWarning(LineBreakOption.class),
    /**
     * Emitted when a {@link RawHandler} discards a line feed character.
     * Takes key string, event index and context string as arguments.
     */
    LineBreakDiscardLFWarning(LineBreakOption.class),
    /**
     * Emitted when a {@link RawHandler} discards a tab character.
     * Takes key string, event index and context string as arguments.
     */
    TabWarning(TabOption.class),
    /**
     * Emitted when a {@link RawHandler} encounters a null character.
     * Takes key string, event index and context string as arguments.
     */
    IllegalNullCharacter(NullCharacterOption.class),
    /**
     * Emitted when a {@link SAXHandler} encounteres a nested 
     * {@link SAXHandler.KeyWord#document document} element.
     * Takes the name of the root (document) element as parameter.
     */
    NestedRoot(SAXHandler.class),
    /**
     * Emitted when a {@link SAXHandler} encounters an elements that is invalid in 
     * the root (document) namespace. Takes the element name and the root (document) 
     * namespace as arguments.
     */
    IllegalElement(SAXHandler.class),
    /**
     * Fancy version of {@link #IgnoredWhitespaceWarningBasic} with line and column number.
     * Takes line and column number arguments.
     */
    IgnoredWhitespaceWarningFancy(IgnorableWhitespaceOption.class),
    /**
     * Emitted when a {@link SAXHandler} encounters “ignorable whitespace” as defined by the XML
     * spec and is configured to emit a warning about this
     * Takes no arguments.
     */
    IgnoredWhitespaceWarningBasic(IgnorableWhitespaceOption.class),
    /**
     * Emitted when a {@link SAXHandler} encounters a nested XML namespace declaration.
     * Takes 3 arguments: 
     * <ol>
     * <li>declared prefix</li>
     * <li>declared uri</li>
     * <li>current URI (of the block in which the declaration occurs)</li></ol>
     */
    NestedURI(SAXHandler.class),
    /**
     * Emitted when a {@link SAXHandler} encounters a {@link SAXHandler.KeyWord#value value} element
     * nested in another. Takes the name of the value element as argument.
     */
    NestedVal(SAXHandler.class),
    /**
     * Emitted when a {@link SAXHandler} encounters a {@link SAXHandler.KeyWord#value value} element
     * which lacks a {@link SAXHandler.KeyWord#key key} attribute.
     * Takes the names of the key attribute and the value element as arguments.
     */
    MissingKey(SAXHandler.class),
    /**
     * Emitted when a {@link SAXHandler} encounters an element that is not bound to an XML namespace.
     * Takes the name of the element as argument.
     */
    MissingURI(SAXHandler.class),
    /**
     * Emitted when a {@link KeyResolverImpl} is unable to link a string key 
     * back to an (XML) namespace URI. Takes the string key as argument.
     */
    NoURIForKey(KeyResolverImpl.class),
    /**
     * Emitted when a {@link KeyResolverImpl} is unable to link a string key
     * back to a macro name. Takes the string key as argument.
     */
    NoMacroForKey(KeyResolverImpl.class),
    /**
     * Fancy version of {@link #PreviousErrorBasic} with line and column number.
     * Takes method name, exception message, line and column number arguments.
     */
    PreviousErrorFancy(SAXHandler.class),
    /**
     * Emitted when a previous error prevents further parsing by the {@link SAXHandler}.
     * Takes method name and exception message as arguments.
     */
    PreviousErrorBasic(SAXHandler.class),
    /**
     * Emitted when a {@link SAXHandler} encounters an invalid value for a format attribute.
     * Takes 2 arguments: the attribute value specified and an exception.
     */
    IllegalFormatValue(SAXHandler.class),
    /**
     * Emitted when a {@link SAXHandler} encounters text
     * which is not nested inside a {@link SAXHandler.KeyWord#value value} element.
     * Takes the name of the value element and the text found as arguments.
     */
    UnnestedText(SAXHandler.class),
    /**
     * Emitted when a {@link SAXHandler} encounters an element which appears to correspond to a 
     * macro element, but is not nested inside a {@link SAXHandler.KeyWord#value value} element.
     * Takes the element name and the name of the value element as arguments.
     */
    UnnestedMacro(SAXHandler.class),
    /**
     * Emitted when a macro is selected but not available in a {@link BasicMapping}.
     * Takes the selected macro name as argument.
     */
    NoSuchMacro(BasicMapping.class),
    /**
     * Emitted when an (XML) namespace URI can not be resolved.
     * Takes the given URI as argument.
     */
    InvalidURI(URIResolver.class),
    /**
     * Emitted when a required value for a given configuration key/attribute is not found in the given
     * (XML) namespace.
     * Takes a configuration key/attribute name and a namespace URI as arguments.
     */
    ConfigurationKeyNotFound(PreConfiguredURIResolver.class),
    /**
     * Error message emitted when string mapping fails. Takes a string (key)
     * that identifies the mapped body of text and a details message as arguments.
     */
    MappingError(BasicMapping.class);

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
