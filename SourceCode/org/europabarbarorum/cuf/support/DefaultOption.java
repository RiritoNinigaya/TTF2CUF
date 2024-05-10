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
package org.europabarbarorum.cuf.support;

/**
 * Marker interface for options that have a default.
 * This interface is used to provide a common supertype for all the actual options.
 * <p>
 * For a {@link DefaultOption} implementation to be valid it must allow for automatic retrieval of the default
 * based on {@link Class} introspection. To allow this the {@link DefaultOption} must meet either of these criteria:
 * <ul>
 * <li>Be an <code>enum</code> type.</li>
 * <li>Have a <code>public</code> no-arg (default) constructor.</li>
 * </ul>
 * @author Johan Ouwerkerk
 */
public interface DefaultOption {

    /**
     * Name of the {@link DefaultOption}.
     * @return the name of the option.
     */
    String name ();

    /**
     * Get the default value for this {@link DefaultOption} type.
     * @return the default option value (instance).
     */
    DefaultOption defaultOption ();
}