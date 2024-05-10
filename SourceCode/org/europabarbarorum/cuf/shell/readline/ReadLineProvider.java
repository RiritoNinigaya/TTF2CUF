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

import java.io.IOException;
//ANT-DISABLE-READLINE /*
import jline.ConsoleReader;
import jline.Terminal;
//ANT-DISABLE-READLINE */
import org.europabarbarorum.cuf.support.AbstractFeature.AbstractFeatureProvider;

/**
 *This class encapsulates the JLine specific code and insulates it from the
 * rest of the program. If this class is never loaded the program can continue to run
 * without any requirement for a working JLine library.
 * @author Johan Ouwerkerk
 */
public class ReadLineProvider extends AbstractFeatureProvider implements ReadLineFeature {

    private Object reader;

    /**
     * Create a new {@link ReadLineProvider}.
     * @param featureName name of the feature.
     */
    public ReadLineProvider (String featureName) {
        super(featureName);
    }

    /**
     * Uses a {@link ConsoleReader} to read text from the console.
     * @return text read or null for end of file.
     * @throws IOException if an error occurs.
     */
    @Override
    public String readLine () throws IOException {
        //ANT-DISABLE-READLINE disabled(); 
        //ANT-DISABLE-READLINE return null;
        //ANT-DISABLE-READLINE /*
        return cast().readLine();
        //ANT-DISABLE-READLINE */
    }

    //ANT-DISABLE-READLINE /*
    private ConsoleReader cast () {
        return (ConsoleReader) reader;
    }
    //ANT-DISABLE-READLINE */

    @Override
    protected Object test () throws IOException {
        //ANT-DISABLE-READLINE disabled();
        //ANT-DISABLE-READLINE return null;
        //ANT-DISABLE-READLINE /*
        Terminal.setupTerminal();
        reader= new ConsoleReader();
        return reader;
        //ANT-DISABLE-READLINE */
    }
}
