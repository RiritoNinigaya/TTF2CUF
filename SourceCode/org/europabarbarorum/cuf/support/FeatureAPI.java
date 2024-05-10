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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Super interface for feature interfaces. Its purpose is to define the minimum
 * set of methods required for feature functionality in {@link AbstractFeature} based code.
 * @author Johan Ouwerkerk
 */
public interface FeatureAPI {

    /**
     * Checks whether a functional feature implementation is available.
     * @return true if this feature is supported, false if not.
     */
    boolean supports();

    /**
     * Annotation for use with {@link AbstractFeature.AbstractFeatureImpl feature implementations} 
     * which triggers greedy loading of
     * the feature in {@link Classloader}. Should be present on all feature implementations.
     */
    @Retention (RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    public static @interface Feature{
        
        /**
         * List of class names to force through the custom {@link org.europabarbarorum.cuf.support.Classloader}.
         * @return array of canonical names of classes that must be loaded by 
         * {@link org.europabarbarorum.cuf.support.Classloader the custom classloader}. 
         * This value may be null or empty. 
         */
        String[] types () default default_types;
        /**
         * Get the name of the class which implements the feature interface.
         * Classes that implement a feature interface must extend {@link AbstractFeature.AbstractFeatureProvider}.
         * @return the canonical name of the provider object for the feature.
         * This value must not be null nor empty.
         */
        String name();
        
        public static final String default_types="package-name";
    }
    
}
