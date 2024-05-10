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

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * A class to manage a single shared I/O stream.
 * This class orders {@link #submit(java.util.concurrent.Callable) requests} for
 * access to the managed I/O stream when the service is {@link #start() started} its {@link Thread}
 * will serve these requests in order.
 * @author Johan Ouwerkerk
 */
public abstract class IOService {

    private LinkedBlockingDeque<Callable<Void>> requestQueue;
    private Thread t;

    private LinkedBlockingDeque<Callable<Void>> queue () {
        if (requestQueue == null) {
            requestQueue = new LinkedBlockingDeque<Callable<Void>>();
        }
        return requestQueue;
    }

    /**
     * Create the {@link Thread} used to serve the requests made to this {@link IOService}.
     * @param task the task to pass on to the constructor of the {@link Thread}.
     * @return a {@link Thread} which has not started yet.
     */
    protected abstract Thread createThread (Runnable task);

    /**
     * Submit a request to access the managed I/O stream.
     * @param call a {@link Callable} which groups all required operations as
     * a single request.
     */
    public abstract void submit (Callable<Void> call);

    /**
     * Start the {@link IOService} if it hasn't started yet.
     * @return true if this call caused the service to start, false if not.
     */
    public final boolean start () {
        if (t == null) {
            final LinkedBlockingDeque<Callable<Void>> q=queue();
            t = createThread(new Runnable() {

                @Override
                public final void run () {

                    while (true) {
                        try {
                            q.take().call();
                        }
                        catch (Exception e) {
                            e.printStackTrace(System.err);
                        }
                    }
                }
            });
            t.start();
            return true;
        }
        else {
            return false;
        }
    }

    abstract static class IOCallWriter extends Writer {

        @Override
        public final void close () throws IOException {
        }

        @Override
        public void write (char[] cbuf, int off, int len) throws IOException {
            try {
                print(new String(cbuf, off, len));
            }
            catch (Exception e) {
                throw new IOException(e);
            }
        }

        public abstract void print (Object s) throws Exception;

        public abstract void println (Object s) throws Exception;
    }


    /**
     * Enumeration of standard “output” streams.
     */
    public static enum StdStream {

        /**
         * Error stream.
         */
        Stderr,
        /**
         * Output stream.
         */
        Stdout;
    }

    /**
     * An {@link IOService} which manages a shared output stream.
     */
    public static abstract class OutputService extends IOService {

        @Override
        public final void submit (Callable<Void> call) {
            super.queue().add(call);
        }
    }

    /**
     * A {@link IOService} which manages a shared input stream.
     */
    public static abstract class InputService extends IOService {

        @Override
        public final void submit (Callable<Void> call) {
            super.queue().addFirst(call);
        }
    }

    static enum PromptService {

        Instance;
        private InputService service;

        public InputService get () {
            if (service == null) {
                service = new InputService() {

                    @Override
                    protected Thread createThread (Runnable task) {
                        return new Thread(task, "prompt service");
                    }
                };
            }
            return service;
        }
    }

    static enum OutputWriter {

        Instance;
        private OutputService service;

        public OutputService get () {
            if (service == null) {
                service = new OutputService() {

                    @Override
                    protected Thread createThread (Runnable task) {
                        return new Thread(task, "output writer service");
                    }
                };
            }
            return service;
        }
    }
}
