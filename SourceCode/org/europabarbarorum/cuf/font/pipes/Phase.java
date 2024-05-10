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
package org.europabarbarorum.cuf.font.pipes;

import java.util.LinkedList;
import java.util.List;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 * Interface to define basic building blocks of a “rendering pipeline”.
 * @param <O> type of output to return
 * @param <I> type of input to accept
 * @param <C> type of third parameter to accept this allows for context information to be passed around
 * @see Transform
 * @author Johan Ouwerkerk
 *
 */
public interface Phase<O, I, C> {

    /**
     * Register a {@link Transform} at a given index. The effect of different indices are implementation dependent,
     * but some order (defined by some partial ordering) in executing the transforms is supposed to maintained.
     * <p>
     * If this {@link Phase} contains as many {@link Transform} objects as allowed by {@link #maxSize()} this
     * method should discard the older {@link Transform} at this index in favour of the new one. If the number of object is
     * lower this method should ensure that all previously registered {@link Transform} instances are
     * retained in their respective order (defined by some partial ordering), and also make a best effort to ensure that
     * the given {@link Transform} occupies the given index.
     * </p>
     * <p>This method must further ensure that this {@link Phase} does not encounter null {@link Transform} objects. To
     * this end it may ignore the index; and treat this method as equivalent to {@link #set(org.europabarbarorum.cuf.font.pipes.Transform, int) }
     * if the index is equal to or larger than the number of {@link Transform} objects already registered.
     * </p>
     * @param transform the transform to set
     * @param index index at which to register
     */
    void set (Transform<O, I, C> transform, int index);

    /**
     * Register a {@link Transform} at the “end” of the {@link Phase}. If this {@link Phase}
     * contains as many {@link Transform} objects as allowed by {@link #maxSize()} this method should do nothing.
     * @param transform
     */
    void add (Transform<O, I, C> transform);

    /**
     * Validate output against input and context argument. This method is intended
     * (but not required) to be invoked by the {@link Phase} itself after
     * every {@link Transform} has completed. A {@link Phase} is expected to abort if
     * an internal call to this method returns false.
     * @param output output produced by some {@link Transform}.
     * @param input input argument to the {@link Transform}
     * @param arg context argument to the {@link Transform}
     * @return true if the output was valid, false if not.
     */
    boolean validate (O output, I input, C arg);

    /**
     * Get the maximum number of {@link Transform} instances that can be registed at this {@link Phase}.
     * @return the maximum “size” of the {@link Phase}.
     */
    int maxSize ();

    /**
     * Return whether or not this {@link Phase} is required to have at least one {@link Transform}.
     * @return true if this phase must have at least one {@link Transform}, false if it may be empty.
     */
    boolean required ();

    /**
     * Produce the output of this {@link Phase} input by consecutively invoking the {@link Transform#apply(java.lang.Object, java.lang.Object) }
     * method of each {@link Transform} registered here.
     * @param input input argument to the {@link Transform}.
     * @param arg context argument to the {@link Transform}.
     * @return output produced, or null if the {@link Phase} was aborted.
     */
    O run (I input, C arg);

    /**
     * Base implementation of {@link Phase} that only requires subclasses to implement {@link Phase#validate(java.lang.Object, java.lang.Object, java.lang.Object) validation}.
     * @param <O> type of output to be produced
     * @param <I> type of input to be accepted
     * @param <A> type of argument that provides context to {@link Transform} instances.
     * @see Transform
     */
    public abstract class PhaseImpl<O, I, A> implements Phase<O, I, A> {

        private final int __cap__;
        private final boolean __req__;

        /**
         * Create a {@link PhaseImpl}.
         * @param cap
         * @param req
         */
        public PhaseImpl (int cap, boolean req) {

            this.__cap__ = cap;
            this.__req__ = req;
        }
        private List<Transform<O, I, A>> list =
                new LinkedList<Transform<O, I, A>>();

        @Override
        public void set (Transform<O, I, A> transform, int index) {
            if (index > list.size()) {
                add(transform);
                return;
            }
            if (list.size() < __cap__) {
                list.add(index, transform);
            }
            else {
                list.remove(index);
                list.add(index, transform);
            }
        }

        @Override
        public void add (Transform<O, I, A> transform) {
            if (list.size() < __cap__) {
                list.add(transform);

            }
        }

        @Override
        public int maxSize () {
            return __cap__;
        }

        @Override
        public boolean required () {
            return __req__;
        }

        @SuppressWarnings("unchecked")
        private O attempt (final I input, final A arg) throws Exception {
            int cap = list.size(), i = 0;
            if (cap == 0) {
                if (!required()) {
                    return (O) input;
                }
                throw new IllegalStateException(Messages.MissingRequiredTransform.
                        getText());
            }
            if (cap == 1) {
                return list.get(i).apply(input, arg);
            }
            boolean valid = true;
            O output = (O) input;
            do {
                output = list.get(i).apply((I) output, arg);
                valid = validate(output, input, arg);
                ++i;
            }
            while (valid && i < cap);
            if (i < cap) {
                throw new Exception(Messages.ValidationFailed.format(i, 0));
            }
            return output;
        }

        @Override
        @SuppressWarnings("unchecked")
        public O run (final I input, final A arg) {
            try {
                return attempt(input, arg);
            }
            catch (Exception e) {
                IOHelp.handleExceptions(Phase.class,
                                        "run",
                                        e,
                                        Messages.TransformFailed,
                                        e.getMessage());
                return null;
            }
        }
    }
}
