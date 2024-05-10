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
package org.europabarbarorum.cuf.gui.support;

import org.europabarbarorum.cuf.font.CUFSource;
import org.europabarbarorum.cuf.support.Preview;

/**
 * Interface to re-configure GUI widgets with a new data model.
 * @param <T> the type of model that is used by the GUI widget.
 * @author Johan Ouwerkerk
 */
public interface UpdatableModel<T> {

    /**
     * Update the GUI widget with the given data model.
     * @param source the new data to display in the GUI.
     */
    void updateModel (T source);

    /**
     * Equivalent to {@link UpdatableModel} except defined for the specific {@link Preview} type.
     * This interface serves to avoid confusing the compiler when multiple update interfaces are
     * implemented in the same class.
     */
    static interface UpdatablePreview {

        /**
         * Configure a component to use a different {@link Preview} as preview model.
         * @param preview the new model to use.
         */
        void updateModel (Preview preview);
    }

    /**
     * Equivalent to {@link UpdatableModel} except defined for the specific {@link CUFSource} type.
     * This interface serves to avoid confusing the compiler when multiple update interfaces are
     * implemented in the same class.
     */
    static interface UpdatableFont {
        /**
         * Update the GUI model with the given {@link CUFSource}.
         * @param font the new {@link CUFSource} source to use as model for font renders and 
         * other font specific information.
         */
        void updateModel (CUFSource font);
    }
}
