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

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.europabarbarorum.cuf.font.PhasedCUFSource;
import org.europabarbarorum.cuf.font.impl.AbstractCUFSource;
import org.europabarbarorum.cuf.font.impl.CUFSourceEditor;

/**
 * This class provides a base implementation of {@link PhasedCUFSource}.
 * @param <C> the type of values used in chartable entries
 * @author Johan Ouwerkerk
 */
public abstract class AbstractPhaseSource<C> extends AbstractCUFSource<C> implements
        PhasedCUFSource<C> {

    @Override
    public boolean exposes (Class<Phase> p) {
        return __exposed__.contains(p);
    }

    /**
     * Enumeration that exposes the index of the next element as well.
     * Intended usage:
     * <blockquote><pre>
     * {@code
     * PipeLineEnumeration ple=pipeLine.enumerate();
     * while(ple.hasNext()) {
     *     Phase p=ple.nextElement();
     *     switch(ple.index()) {
     *          // use the index variable to determine
     *          // how to cast the Phase and what to do with it.
     *     }
     * }
     * }
     * </pre></blockquote>
     */
    protected static interface PipeLineEnumeration extends Enumeration<Phase> {

        /**
         * Exposes the index from which the next element would be read.
         * @return the current position in the {@link PipeLineEnumeration}.
         */
        int index ();
    }

    /**
     * Class to model the “pipeline” part of {@link PhasedCUFSource}.
     */
    protected static class RenderingPipeLine {

        private final List<Class> keys;
        private final Phase[] phases;

        private RenderingPipeLine (Phase[] ps) {
            if (ps == null) {
                throw new IllegalArgumentException(Messages.NullPipeLineError.
                        getText());
            }
            if (ps.length == 0) {
                throw new IllegalArgumentException(Messages.EmptyPipeLineError.
                        getText());
            }
            this.phases = ps;
            Class[] cs = new Class[ps.length];
            for (int i = 0; i < ps.length; ++i) {
                cs[i] = ps[i].getClass();
            }
            keys = Collections.unmodifiableList(Arrays.asList(cs));
        }

        /**
         * Check whether or not a given {@link Class} refers to a {@link Phase} in the pipeline.
         * @param k the class to check.
         * @return true if it is part of the pipeline, false if not.
         */
        public boolean contains (Class k) {
            return keys.contains(k);
        }

        /**
         * Provides implementation for {@link AbstractPhaseSource#register(java.lang.Class, org.europabarbarorum.cuf.font.pipes.Transform) },
         * without the sanity checks.
         * @param p phase to register at
         * @param t transform to register.
         */
        @SuppressWarnings("unchecked")
        public void register (Class<? extends Phase> p, Transform t) {
            if (contains(p)) {
                phases[keys.indexOf(p)].add(t);
            }
        }

        /**
         * Provides implementation for {@link AbstractPhaseSource#register(java.lang.Class, org.europabarbarorum.cuf.font.pipes.Transform, int) },
         * without the sanity checks.
         * @param p phase to register at
         * @param t transform to register.
         * @param priority the index to use when registering.
         */
        @SuppressWarnings("unchecked")
        public void register (Class<? extends Phase> p, Transform t,
                              int priority) {
            if (contains(p)) {
                phases[keys.indexOf(p)].set(t, priority);
            }
        }

        /**
         * Provide an {@link PipeLineEnumeration} for convenience when implementing the rendering aspect of the
         * pipeline.
         * @return an enumeration of all {@link Phase} objects in the pipeline.
         */
        public PipeLineEnumeration enumerate () {
            return new PipeLineEnumeration() {

                private int k = 0;

                @Override
                public int index () {
                    return k;
                }

                @Override
                public boolean hasMoreElements () {
                    return k < phases.length;
                }

                @Override
                public Phase nextElement () {
                    Phase p = phases[k];
                    ++k;
                    return p;
                }
            };
        }
    }
    /**
     * The “pipeline” of this {@link PhasedCUFSource}.
     */
    protected final RenderingPipeLine pipeLine;

    /**
     * Create a new {@link PhasedCUFSource} instance.
     */
    public AbstractPhaseSource () {
        pipeLine = new RenderingPipeLine(phases());
        __exposed__ = __expose();
    }

    /**
     * Creates a new {@link AbstractPhaseSource}. It will have support for editing.
     * @param b use true to claim intrinsic support for kerning, false otherwise.
     */
    protected AbstractPhaseSource (boolean b) {
        this(new CUFSourceEditor(), b);
    }

    /**
     * Creates a new {@link AbstractPhaseSource}.
     * @param editor a {@link CUFSourceEditor} or null to disable editing.
     * @param b use true to claim intrinsic support for kerning, false otherwise.
     */
    protected AbstractPhaseSource (CUFSourceEditor editor, boolean b) {
        super(editor, b);
        pipeLine = new RenderingPipeLine(phases());
        __exposed__ = __expose();
    }

    /**
     * List of all {@link Phase} objects in the “rendering pipeline”.
     * This method should not return an empty array or null.
     * @return an array containing all phases in order that they are to be applied.
     */
    protected abstract Phase[] phases ();

    /**
     * List of all {@link Phase} objects that are exposed to the outside world.
     * @return an array containing all exposed phases in any order.
     * @see #exposes(java.lang.Class)
     */
    protected abstract Class<? extends Phase>[] exposed ();
    final private List<Class<? extends Phase>> __exposed__;

    @SuppressWarnings("unchecked")
    private List<Class<? extends Phase>> __expose () {
        Class<? extends Phase>[] cls = exposed();
        if (cls == null) {
            return Collections.unmodifiableList(Collections.EMPTY_LIST);
        }
        for (Class c : cls) {
            if (!pipeLine.contains(c)) {
                if (c == null) {
                    throw new IllegalArgumentException(
                            Messages.ExposeInvalidError.getText());
                }
                throw new IllegalArgumentException(
                        Messages.NotExposedError.format(c.getCanonicalName()));
            }
        }
        return Collections.unmodifiableList(Arrays.asList(cls));

    }

    @Override
    public void register (Class<Phase> p, Transform t) {
        if (exposes(p)) {
            this.pipeLine.register(p, t);
        }
    }

    @Override
    public void register (Class<Phase> p, Transform t, int position) {
        if (exposes(p)) {
            this.pipeLine.register(p, t, position);
        }
    }
}
