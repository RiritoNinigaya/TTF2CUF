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
package org.europabarbarorum.cuf.shell.readline;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import org.europabarbarorum.cuf.support.AbstractFeature.AbstractFeatureImpl;
import org.europabarbarorum.cuf.support.FeatureAPI.Feature;

/**
 * Class to provide support for readline functionality provided by the JLine library.
 * @author Johan Ouwerkerk
 */
public enum ReadLineSupport {

    /**
     * The single JLine feature instance.
     */
    JLine;

    /**
     * Wrapped version of {@link AbstractFeatureImpl#featureEnabled() }.
     * @return true if this feature can be safely enabled (is available and appears to be functional),
     * false if not.
     */
    public boolean featureEnabled () {
        return support.featureEnabled();
    }

    /**
     * Wrapped version of {@link AbstractFeatureImpl#supports() }.
     * @return true if libraries for this feature are available. This does not necessarily
     * mean that the feature itself can be safely used.
     * @see #featureEnabled()
     */
    public boolean featureSupported () {
        return support.supports();
    }
    private BufferedReader stream;

    private BufferedReader getConsoleStream () {
        BufferedReader input;
        Console c = System.console();
        input = c == null
                ? new BufferedReader(new InputStreamReader(System.in))
                : new BufferedReader(c.reader());
        return input;
    }

    /**
     * Read a line of input from the console. This method falls back to
     * reading without readline support when necessary.
     * @return the text read, or null if end of file was reached.
     * @throws Exception if an error occurs.
     */
    public String readLine () throws Exception {
        if (support.featureEnabled()) {
            return support.getFeature().readLine();
        }
        else {
            if (stream == null) {
                stream = getConsoleStream();
            }
            return stream.readLine();
        }
    }
    private final ReadLineImpl support = new ReadLineImpl(name());

    @Feature(name = "org.europabarbarorum.cuf.shell.readline.ReadLineProvider")
    private static class ReadLineImpl extends AbstractFeatureImpl<ReadLineFeature> {

        private ReadLineImpl (String name) {
            super(name);
        }

        @Override
        public boolean disable () {
            //ANT-DISABLE-READLINE return true;
            //ANT-DISABLE-READLINE /*
            return false;
            //ANT-DISABLE-READLINE */
        }

        @Override
        public String[] requiredClasses () {
            return new String[] {
                        "jline.ConsoleReader",
                        "jline.Terminal"
                    };
        }
    }
}
