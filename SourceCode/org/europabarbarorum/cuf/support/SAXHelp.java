/*
 *  
 *  Copyright (C) 2011 The Europa Barbarorum Team
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

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is a base class for SAX based XML processing. It contains some convenience methods to
 * run XSLT transformations or open XML files as a SAX event stream.
 * @author Johan Ouwerkerk
 */
public class SAXHelp {

    /**
     * Open an XML file as a {@link SAXSource source of SAX events}
     * @param f the {@link File} to read.
     * @param encoding the character encoding of the XML data.
     * Use null to let the underlying SAX parsers figure this out.
     * @return a {@link SAXSource} to deliver SAX events for use in
     * {@link #transform(javax.xml.transform.Source, javax.xml.transform.Source, javax.xml.transform.Result) transformations},
     * or {@link #parse(javax.xml.transform.sax.SAXSource, org.xml.sax.helpers.DefaultHandler) parsing}.
     * @throws Exception if an error occurs.
     * @see #source(java.net.URL, java.lang.String) 
     * @see #source(java.net.URI, java.lang.String) 
     */
    public SAXSource source (File f, String encoding) throws Exception {
        return source(f.getCanonicalFile().toURI(), encoding);
    }

    /**
     * Open an URL as a {@link SAXSource source of SAX events}
     * @param url the {@link URL} to fetch the contents from.
     * @param encoding the character encoding of the XML data.
     * Use null to let the underlying SAX parsers figure this out.
     * @return a {@link SAXSource} to deliver SAX events for use in
     * {@link #transform(javax.xml.transform.Source, javax.xml.transform.Source, javax.xml.transform.Result) transformations},
     * or {@link #parse(javax.xml.transform.sax.SAXSource, org.xml.sax.helpers.DefaultHandler) parsing}.
     * @throws Exception if an error occurs.
     * @see #source(java.io.File, java.lang.String) 
     * @see #source(java.net.URI, java.lang.String) 
     */
    public SAXSource source (URL url, String encoding) throws Exception {
        return source(url.toURI(), encoding);
    }

    /**
     * Open an URL as a {@link SAXSource source of SAX events}
     * @param uri the {@link URI} to fetch the contents from.
     * @param encoding the character encoding of the XML data.
     * Use null to let the underlying SAX parsers figure this out.
     * @return a {@link SAXSource} to deliver SAX events for use in
     * {@link #transform(javax.xml.transform.Source, javax.xml.transform.Source, javax.xml.transform.Result) transformations},
     * or {@link #parse(javax.xml.transform.sax.SAXSource, org.xml.sax.helpers.DefaultHandler) parsing}.
     * @throws Exception if an error occurs.
     * @see #source(java.net.URL, java.lang.String) 
     * @see #source(java.io.File, java.lang.String) 
     */
    public SAXSource source (URI uri, String encoding) throws Exception {
        InputSource in = new InputSource(uri.toASCIIString());
        if (encoding != null) {
            in.setEncoding(encoding);
        }
        return new SAXSource(getSAXParser().getXMLReader(), in);
    }
    private SAXParserFactory factory;
    private TransformerFactory trans;

    /**
     * Convenience method to wrap a {@link ContentHandler} in a {@link Result}.
     * @param handler the {@link ContentHandler} to use.
     * @return a {@link SAXResult} which feeds its input XML to the given
     * {@link ContentHandler}.
     */
    public Result handler (ContentHandler handler) {
        return new SAXResult(handler);
    }

    /**
     * Parse an XML {@link File} as a SAX event stream.
     * @param in the XML {@link File} to parse.
     * @param enc character encoding of the XML data.
     * Use null to let the underlying SAX parsers figure this out.
     * @param h the {@link DefaultHandler} which processes any generated SAX events.
     * @throws Exception if an error occurs.
     * @see #parse(javax.xml.transform.sax.SAXSource, org.xml.sax.helpers.DefaultHandler)
     */
    public void parse (File in, String enc, DefaultHandler h) throws Exception {
        parse(source(in, enc), h);
    }

    /**
     * Consume a {@link SAXSource}, parsing the underlying XML data.
     * @param input the {@link SAXSource} which provides the SAX events.
     * @param handler the {@link DefaultHandler} which processes any generated SAX events.
     * @throws Exception if an error occurs.
     */
    public void parse (SAXSource input, DefaultHandler handler) throws Exception {
        getSAXParser().parse(input.getInputSource(), handler);
    }

    private SAXParser getSAXParser () throws Exception {
        if (factory == null) {
            factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
        }
        return factory.newSAXParser();
    }

    /**
     * Convenience method to perform an XML {@link #transform(javax.xml.transform.Source, javax.xml.transform.Source, javax.xml.transform.Result) transformation}.
     * This method is suitable for applying an XSL stylesheet against XML data.
     * @param xml the XML data {@link File}
     * @param xmlEnc the character encoding of the data file.
     * Use null to let underlying SAX Parsers figure this out.
     * @param xsl the {@link File} which contains an XSL stylesheet to apply.
     * @param xslEnc the character encoding of the XSL stylesheet.
     * Use null to let underlying SAX Parsers figure this out.
     * @param tgt the target {@link Result} which is to be populated by the transformation.
     * @throws Exception if an error occurs.
     */
    public void transform (File xml, String xmlEnc, File xsl, String xslEnc,
                           Result tgt) throws Exception {
        transform(source(xml, xmlEnc), source(xsl, xslEnc), tgt);
    }

    /**
     * Perform an XML transformation.
     * @param input the {@link Source} of the XML data to transform.
     * @param style the {@link Source} of the data describing the transformation.
     * @param target the {@link Result} which is to be populated by the transformation.
     * @throws Exception if an error occurs.
     */
    public void transform (Source input, Source style, Result target) throws
            Exception {
        if (trans == null) {
            trans = TransformerFactory.newInstance();
        }
        Transformer transform = trans.newTransformer(style);
        transform.transform(input, target);
    }

    /**
     * Convenience method to obtain a {@link Result} which writes generated content to a (text) {@link File}.
     * @param file the {@link File} to write.
     * @param encoding the character encoding to use for writing output. Use null to assume the platform default.
     * @return a {@link StreamResult} suitable for writing text files.
     * @throws Exception if an error occurs.
     */
    public Result writer (File file, String encoding) throws
            Exception {
        return new StreamResult(encoding == null
                ? new PrintWriter(file)
                : new PrintWriter(file, encoding));
    }
}
