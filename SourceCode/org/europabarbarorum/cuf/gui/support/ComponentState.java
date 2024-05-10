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

import org.europabarbarorum.cuf.gui.support.ComponentState.ComponentModel;

/**
 * An extension to {@link ValidatedUI}, this interface captures getting/setting component
 * state for GUI forms.
 * @param <M> type of {@link ComponentModel} used to encapsulate component state of this object.
 * @author Johan Ouwerkerk
 */
public interface ComponentState<M extends ComponentModel> extends ValidatedUI {
    /**
     * Creates a {@link ComponentModel} snapshot of this component. 
     * This method extracts a {@link ComponentModel} which encapsulates all relevant component
     * state data of the component and returns that.
     * This method is only safe to call on {@link #checkUI() valid} components,
     * results returned when the component is not {@link #checkUI() valid} are undefined.
     * <p>For valid {@link ComponentState} objects the 
     * returned {@link ComponentModel} object must be able to restore a 
     * copy of this {@link ComponentState} to an equivalent state to that of 
     * this component when the method call was made.
     * @return a {@link ComponentModel} containing component state data.
     * @see ComponentModel#populate(org.europabarbarorum.cuf.gui.support.ComponentState)
     */
    public M createModel();

    /**
     * A snapshot of a state information from a {@link ComponentState} object.
     * @param <S> the type of {@link ComponentState} that this object can restore.
     */
    public static interface ComponentModel<S extends ComponentState> {
        /**
         * Restore component state from the given {@link ComponentModel}.
         * @param ui the {@link ComponentState} object to restore.
         * @see ComponentState#createModel() 
         */
        public void populate(S ui);
    }
}
