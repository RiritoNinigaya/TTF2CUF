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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.europabarbarorum.cuf.strings.StringsWriter.EncodingOption;
import org.europabarbarorum.cuf.strings.StringsWriter.StringWriter;
import org.europabarbarorum.cuf.strings.StringsWriter.StringsFeeder;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.OptionMap;
import org.europabarbarorum.cuf.support.SAXHelp;

/**
 * A {@link StringsFeeder} which feeds an XML document using a SAX parser to the compiler.
 * @author Johan Ouwerkerk
 */
public class XMLSource extends SAXHelp implements StringsFeeder {

    /**
     * A {@link DefaultOption} which denotes an (optional) XSLT stylesheet to use for
     * compiling XML files.
     */
    public static class XSLTOption extends EncodingOption {

        private final URI xsl;

        /**
         * A default option: use no stylesheet, a {@link XMLSource} will compile a
         * given XML file directly to Strings format.
         */
        public XSLTOption () {
            this(null, defaultEncoding);
        }

        /**
         * Create an {@link XSLTOption} with a given stylesheet.
         * If the given stylesheet is not null, a {@link XMLSource}
         * will first apply the given stylesheet, before compiling the result.
         * @param xsl a {@link File} corresponding to the XSLT stylesheet to use.
         * A null value means that the {@link XMLSource} will compile a given XML file
         * directly to Strings format.
         */
        public XSLTOption (File xsl) {
            this(xsl, defaultEncoding);
        }
        /**
         * Create an {@link XSLTOption} which refers to some XSLT stylesheet at an arbitrary URL.
         * @param encoding specifies the character encoding of the given stylesheet.
         * @param url the {@link URL} of the stylesheet to load.
         * @throws URISyntaxException if the given {@link URL} cannot be converted to a valid {@link URI}.
         */
        protected XSLTOption(String encoding, URL url) throws URISyntaxException{
            super(encoding);
            this.xsl = url.toURI();
        }
        /**
         * Create an {@link XSLTOption} which refers to some XSLT stylesheet at an arbitrary URI.
         * @param encoding specifies the character encoding of the given stylesheet.
         * @param uri the {@link URI} of the stylesheet to load.
         */
        protected XSLTOption(String encoding, URI uri) {
            super(encoding);
            this.xsl = uri;
        }

        /**
         * Create an {@link XSLTOption} with a given stylesheet.
         * If the given stylesheet is not null, a {@link XMLSource}
         * will first apply the given stylesheet, before compiling the result.
         * @param xsl a {@link File} corresponding to the XSLT stylesheet to use.
         * A null value means that the {@link XMLSource} will compile a given XML file
         * directly to Strings format.
         * @param encoding specifies the character encoding of the given stylesheet.
         */
        public XSLTOption (File xsl, String encoding) {
            super(encoding);
            this.xsl = xsl.toURI();
        }

        @Override
        public String name () {
            return "XSLT";
        }

        @Override
        public DefaultOption defaultOption () {
            return new XSLTOption(null, defaultEncoding);
        }
        /**
         * Get the URI of the stylesheet associated with this {@link XSLTOption}.
         * @return the URI of the stylesheet to load.
         */
        public URI styleSheet() { return xsl; }
        /**
         * Get the character encoding implied by this {@link XSLTOption}. This 
         * method is a more readable alias of {@link #value() }.
         * @return the character encoding to use for loading the stylesheet referred to 
         * by this {@link XSLTOption}.
         */
        public String encoding() {return value(); }
    }
    private final File xmlFile;
    private final URIResolver uriResolver;
    private final OptionMap options;

    /**
     * Creates an {@link XMLSource} which can be compiled into a strings file.
     * @param xmlFile {@link File} supplying the source XML data to compile.
     * @param uriResolver the {@link URIResolver} to use when resolving XML namespace import statements.
     * @param options a {@link Map} of {@link DefaultOption} settings honoured by the
     * {@link SAXHandler} object responsible for splitting the input in chunks that are compiled to the output file.
     */
    public XMLSource (File xmlFile,
                      URIResolver uriResolver,
                      OptionMap options) {
        this.xmlFile = xmlFile;
        this.uriResolver = uriResolver;
        this.options = options;
    }

    @Override
    public void deliverEvents (StringWriter writer) throws Exception {
        XSLTOption opt = options.getOption(XSLTOption.class);
        SAXHandler sax = new SAXHandler(uriResolver, writer, options);
        String enc = options.getOption(EncodingOption.class).value();
        if (opt.xsl == null) {
            parse(xmlFile, enc, sax);
        }
        else {
            transform(source(xmlFile, enc), source(opt.xsl, opt.value()), handler(sax));
        }
    }

    @Override
    public void dispose () throws Exception {
    }
}
