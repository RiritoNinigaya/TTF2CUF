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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.europabarbarorum.cuf.strings.StringsWriter.FormatOption;
import org.europabarbarorum.cuf.strings.StringsWriter.StringWriter;
import org.europabarbarorum.cuf.strings.impl.StringMapping.ConfiguredMapping;
import org.europabarbarorum.cuf.strings.impl.URIResolver.PreConfiguredURIResolver;
import org.europabarbarorum.cuf.strings.impl.URIResolver.ResolverConfiguration;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.OptionMap;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class to handle SAX events/callbacks for parsing an XML document.
 * This class validates its input, throwing exceptions whenever it encounters invalid input.
 * It is somewhat lenient in that it discards structure it does not explicitly understand, which currently
 * amounts to attributes only. (Simply put: this class only looks for attributes it recognizes, and all
 * others are discarded.)
 * This class is a bit <s>strict</s> allergic
 * in how it handles namespaces: it expects them to be declared on the root
 * element which is contrary to how XML is supposed to work. However it is non-trivial to fix that
 * because of what namespaces mean in context of XML format.
 * @author Johan Ouwerkerk
 */
public class SAXHandler extends DefaultHandler {

    /**
     * List of XML elements recognized by the {@link SAXHandler} class as “keywords” for values it
     * needs to process.
     */
    private enum KeyWord {

        /**
         * Name of the root element that of an XML document that is compiled to strings file.
         */
        document,
        /**
         * Name of the attribute that is recognized as a “key” for a strings file.
         */
        key,
        /**
         * Name of the attribute that is recognized as a “output format” directive.
         * It instructs the compiler to generate specific type of Strings file: use 
         * either “ordered” or “keyed”.
         */
        format,
        /**
         * Name of the element that is recognized as a “value” for a strings file.
         */
        value;
    }
    /**
     * Namespace of the structure elements of an XML document that is compiled to a strings file.
     */
    public static final String docNameSpace = "org.europabarbarorum.cuf.strings";
    /**
     * A map of URI string keys, and {@link StringMapping} objects.
     * This allows the SAXHandler to lookup the right macro depending on the {@link #currentURI} being used.
     */
    protected final Map<String, StringMapping> namespaceMapping;
    /**
     * Object to interpret an XML namespace URI and produce a {@link StringMapping} instance.
     */
    protected final URIResolver resolver;
    /**
     * Object to write out a string in the strings.bin format.
     */
    protected final StringWriter writer;
    /**
     * Current XML namespace being used to interpret input.
     */
    private String currentURI;
    /**
     * Shadow copy of {@link #currentURI} for use in {@link #endElement(java.lang.String, java.lang.String, java.lang.String) }.
     * This value tracks the URI of the {@link StringMapping} to use for obtaining raw output through 
     * {@link StringMapping#getMappedString() }.
     */
    private String mappingURI;
    /**
     * The name of the Strings key for which data is currently being processed.
     */
    private String currentKey;
    /**
     * Stack of macro names. These are just element names tracked in a stacked fashion
     * (to represents the XML structure) so that the right macro can be selected for input text.
     */
    private final Stack<String> stack;
    /**
     * Settings that the compiler should honour.
     */
    protected final OptionMap compileOpts;

    /**
     * Create a SAXHandler instance.
     * @param resolver the {@link URIResolver} to use.
     * @param writer the {@link StringWriter} to use.
     * @param options a {@link Map} of {@link DefaultOption} class keys to use with
     * corresponding values for the settings represented. This {@link Map} must be
     * modifiable and must not be null.
     */
    public SAXHandler (URIResolver resolver,
                       StringWriter writer,
                       OptionMap options) {
        this.resolver = resolver;
        this.writer = writer;
        compileOpts = options;
        stack = new Stack<String>();
        currentURI = docNameSpace;
        namespaceMapping = new HashMap<String, StringMapping>();
    }
    private boolean inSection = false;
    private String prevElement = "";

    /**
     * Signals the {@link SAXHandler} that a namespace was found in the source XML.
     * This method ignores the {@link #docNameSpace standard namespace} since its purpose is to
     * act as implementation for importing Macros from namespace URIs.
     * @param prefix the namespace prefix this namespace URI is bound to. This is a technical XML detail that
     * is of no (further) relevance to the {@link SAXHandler}.
     * @param uri the namespace URI to resolve
     * @throws SAXException if the namespace was not found on the root element, or
     * if it cannot be resolved.
     * @see URIResolver#resolve(java.lang.String)
     */
    @Override
    public void startPrefixMapping (String prefix, String uri) throws
            SAXException {
        if (!uri.equals(docNameSpace)) {
            try {
                if (!prevElement.equals("")) {
                    throw new IllegalStateException(
                            Messages.NestedURI.format(prefix,
                                                      uri,
                                                      currentURI));
                }
                namespaceMapping.put(uri, resolver.resolve(uri));
            }
            catch (Exception e) {
                _err(e, "startPrefixMapping");
            }
        }
    }

    private void setFormat (String format) throws Exception {
        if (format == null) {
            return;
        }
        try {
            FormatOption f = FormatOption.valueOf(format);
            writer.format(f);
        }
        catch (Exception except) {
            throw new IllegalArgumentException(
                    Messages.IllegalFormatValue.format(KeyWord.format.name(),
                                                       format),
                    except);
        }
    }

    private void initMappings (final Attributes attrs) throws Exception {
        if (prevElement.equals("")) {
            prevElement = KeyWord.document.name();
        }
        else {
            throw new Exception(
                    Messages.NestedRoot.format(KeyWord.document.name()));
        }

        ResolverConfiguration conf = new ResolverConfiguration() {

            @Override
            public String get (String uri, String name) {
                return attrs.getValue(uri, name);
            }
        };
        setFormat(conf.get(docNameSpace, KeyWord.format.name()));
        for (StringMapping mapping : namespaceMapping.values()) {
            if (mapping instanceof ConfiguredMapping) {
                PreConfiguredURIResolver.configureMapping(
                        (ConfiguredMapping) mapping,
                        conf);
            }
        }
    }

    /**
     * Implementation of {@link DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes) }
     * This method validates that the input element is allowed at this position and sets internal state accordingly.
     * @param uri the URI of the element. Must not be null.
     * @param localName the local name of the XML element.
     * @param qName the FQN according to XML spec. Not used.
     * @param attributes attributes of the XML element, used to extract strings keys.
     * @throws SAXException if this element is not allowed here or some other error occurs
     * @see #startCore(java.lang.String)
     * @see #startIncluded(java.lang.String)
     */
    @Override
    public void startElement (String uri,
                              String localName,
                              String qName,
                              Attributes attributes) throws SAXException {
        try {
            if (uri == null) {
                _err(Messages.MissingURI.format(localName));
            }
            currentURI = uri;
            if (uri.equals(docNameSpace)) {
                if (localName.equals(KeyWord.document.name())) {
                    initMappings(attributes);
                    return;
                }
                if (localName.equals(KeyWord.value.name())) {
                    startCore(attributes.getValue(uri,
                                                  KeyWord.key.name()));
                    return;
                }
                else {
                    throw new IllegalArgumentException(
                            Messages.IllegalElement.format(localName,
                                                           docNameSpace));
                }
            }
            else {
                startIncluded(localName);
            }
        }
        catch (Exception e) {
            _err(e, "startElement");
        }
    }

    /**
     * Callback to handle “macro” (included) elements. This method checks if the element appears
     * inside a {@link #sectionName} element; and pushes it to the stack if it is.
     * @param localName the name of the element.
     * @throws Exception if the element occurs outside of an {@link #sectionName} element.
     */
    private void startIncluded (String localName) throws Exception {
        if (!inSection) {
            _err(Messages.UnnestedMacro.format(localName, KeyWord.value.name()));
        }
        stack.push(localName);
    }

    /**
     * Callback to handle “core” elements (namely those in the {@link #docNameSpace} XML namespace).
     * This method check if the element does not appear inside a {@link #sectionName} and if its
     * {@link #keyAttrName} attribute is set. If those checks succeed, the key is written to the output.
     * @param key the key of the strings entry. Must not be null.
     * @throws Exception if some error occurs (the element is invalid)
     */
    private void startCore (String key) throws Exception {
        if (inSection) {
            _err(Messages.NestedVal.format(KeyWord.value.name()));
        }
        inSection = true;

        if (key == null) {
            _err(Messages.MissingKey.format(KeyWord.key.name(),
                                            KeyWord.value.name()));
        }
        writer.key(key);
        currentKey = key;
    }

    private void _err (String message) throws SAXException {
        if (canUseLocator == 3) {
            throw new SAXParseException(message, locator);
        }
        else {
            throw new SAXException(message);
        }
    }

    private void _err (Exception wrap, String method) throws SAXException {
        String mthod = "SAXHandler." + method;
        String msg = canUseLocator == 3
                ? Messages.PreviousErrorFancy.format(mthod,
                                                     wrap.getMessage(),
                                                     locator.getLineNumber(),
                                                     locator.getColumnNumber())
                : Messages.PreviousErrorBasic.format(mthod,
                                                     wrap.getMessage());

        IOHelp.handleExceptions(SAXHandler.class, method, wrap, msg);

        if (canUseLocator == 3) {
            throw new SAXParseException(msg, locator, wrap);
        }
        else {
            throw new SAXException(msg, wrap);
        }
    }

    /**
     * Signals that an element has ended. This method performs some cleanup and writes output if
     * applicable.
     * @param uri the namespace of the element
     * @param localName the local name of the element
     * @param qName the FQN according to the XML spec. Not used.
     * @throws SAXException if an error occurs
     */
    @Override
    public void endElement (String uri, String localName, String qName) throws
            SAXException {
        try {
            if (localName.equals(KeyWord.value.name())) {
                inSection = false;
                StringMapping map = namespaceMapping.get(mappingURI);
                RawHandler rw = new RawHandler(compileOpts, map, currentKey);
                writer.value(rw.toString());
                currentURI = docNameSpace;
                mappingURI = docNameSpace;
                return;
            }

            if (!localName.equals(KeyWord.document.name())) {
                stack.pop();
                if (stack.isEmpty()) {
                    mappingURI = currentURI;
                    currentURI = docNameSpace;
                }
                return;
            }
        }
        catch (Exception e) {
            _err(e, "endElement");
        }
    }

    /**
     * Configure this {@link SAXHandler} with a {@link Locator} object so that it can report errors detailing
     * line and column numbers in the input XML.
     * @param locator the {@link Locator} object to use.
     */
    @Override
    public void setDocumentLocator (Locator locator) {
        this.locator = locator;
        canUseLocator += 1;
    }
    /**
     * The {@link Locator} object that provides the {@link SAXHandler} with information regardig the
     * current position of input supplied by the parser in the XML source.
     */
    private Locator locator;
    /**
     * Property to track if the {@link #locator} object can be used.
     * If this value is 3, then the {@link #locator} can be safely used.
     */
    private int canUseLocator = 0;

    /**
     * Initializes the {@link #canUseLocator} property.
     */
    @Override
    public void startDocument () {
        canUseLocator += 2;
    }

    /**
     * Resets the {@link #canUseLocator} property.
     */
    @Override
    public void endDocument () {
        canUseLocator = 0;
    }

    /**
     * Handle character data in XML input.
     * @param ch the characters supplied by the parser
     * @param start the offset of the first character to use
     * @param length the number of characters to use
     * @throws SAXException if an error occurs
     * @see #chars(char[], int, int)
     */
    @Override
    public void characters (char[] ch, int start, int length) throws
            SAXException {
        try {
            chars(ch, start, length);
        }
        catch (Exception e) {
            _err(e, "characters");
        }
    }

    /**
     * Appends characters to the output.
     * @param ch the array of characters supplied by the parser
     * @param start the offset at which characters can be used
     * @param length the number of character that can be used
     * @throws Exception if an error occurs
     */
    private void chars (char[] ch, int start, int length) throws Exception {
        String text = new String(ch, start, length);

        if ((!inSection || currentURI.equals(docNameSpace)) && !text.trim().
                equals("")) {
            throw new SAXException(
                    Messages.UnnestedText.format(KeyWord.value.name(), text));
        }

        if (!stack.isEmpty()) {
            StringMapping m = namespaceMapping.get(currentURI);
            m.select(stack.peek(), currentKey);
            m.appendString(text, currentKey);
        }
    }

    /**
     * Handles ignorable whitespace as defined by the XML spec according to the {@link #compileOpts} setting.
     * @param ch the caracter array supplied by the parser
     * @param start the offset at which characters can be read
     * @param length the number of characters that can be read
     * @throws SAXException if some error occurs.
     * @see IgnorableWhitespaceOption
     */
    @Override
    public void ignorableWhitespace (
            char[] ch, int start, int length) throws
            SAXException {
        if (compileOpts.get(IgnorableWhitespaceOption.class) == IgnorableWhitespaceOption.Compile) {
            characters(ch, start, length);
            return;
        }

        if (compileOpts.get(IgnorableWhitespaceOption.class) == IgnorableWhitespaceOption.Warning) {
            fancyLog(Messages.IgnoredWhitespaceWarningBasic,
                     Messages.IgnoredWhitespaceWarningFancy);
        }
    }

    private void fancyLog (Messages plain, Messages fancy) {
        if (canUseLocator == 3) {
            IOHelp.warn(SAXHandler.class,
                        fancy,
                        this.locator.getLineNumber(),
                        this.locator.getColumnNumber());
        }
        else {
            IOHelp.warn(SAXHandler.class,
                        plain.getText());
        }
    }

    /**
     * Wraps fatal (parsing) errors in exceptions that detail the location of the error in the input file,
     * if possible.
     * @param exception the exception thrown by the parser.
     * @throws SAXException the exception thrown by this method.
     * @see #_err(java.lang.Exception, java.lang.String)
     */
    @Override
    public void fatalError (SAXParseException exception) throws SAXException {
        _err(exception, "fatalError");
    }

    /**
     * List of settings of what to do with ignorable whitespace (defined by the XML spec)
     * as well as leading and trailing whitespace of text chunks.
     * Note that these are best-effort settings; as it is not possible to predict SAX parser behaviour
     * in the general case.
     */
    public static enum IgnorableWhitespaceOption implements DefaultOption {

        /**
         * Ignore ignorable whitespace.
         */
        Ignore,
        /**
         * Warn about ignorable whitespace and discard it.
         */
        Warning,
        /**
         * Compile it into the .strings.bin output file, as if it was ordinary character data.
         */
        Compile;

        @Override
        public DefaultOption defaultOption () {
            return Warning;
        }
    }
}
