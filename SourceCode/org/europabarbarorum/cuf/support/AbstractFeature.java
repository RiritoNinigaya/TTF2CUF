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

import java.lang.reflect.Constructor;

/**
 * A class that provides the basic shared methods for features that depend on external libraries to
 * be available at both compile and run time.
 * @see AbstractFeatureImpl
 * @see AbstractFeatureProvider
 * @author Johan Ouwerkerk
 */
public abstract class AbstractFeature implements FeatureAPI {

    private final String featureName;

    /**
     * Create a new {@link AbstractFeature}.
     * @param featureName name of the feature.
     */
    protected AbstractFeature (String featureName) {
        this.featureName = featureName;
    }

    /**
     * Shortcut to throw an {@link NotCompiledException}.
     */
    protected void disabled () {
        throw new NotCompiledException(featureName);
    }
    private boolean supports;

    @Override
    public boolean supports () {
        return supports;
    }

    /**
     * A base class for the part of the feature implementation which is closely tied
     * to the 3rd party library that it exposes. Essentially it provides implementation
     * of an internal API that encapsulated the 3rd party library.
     * @see AbstractFeatureImpl
     */
    public static abstract class AbstractFeatureProvider extends AbstractFeature {

        /**
         * Create a new {@link AbstractFeatureImpl}.
         * @param featureName name of the feature.
         */
        protected AbstractFeatureProvider (String featureName) {
            super(featureName);
            super.supports = init();
        }

        /**
         * Tests to see if a relevant object can be constructed.
         * This method is used to check if required classes are not just available, but
         * the whole feature actually works properly.
         * @return a test object.
         * @throws Exception if an error occurs.
         */
        protected abstract Object test () throws Exception;

        private boolean init () {
            boolean result = false;
            final String method = "init";
            try {
                result = test() != null;
            }
            catch (Throwable t) {
                IOHelp.handleExceptions(AbstractFeatureProvider.class, method, t,
                                        t.getLocalizedMessage());
            }
            return result;
        }
    }

    /**
     * A base class which is used to provide an internal safe API on top of a 3rd party library
     * that allows the program to function even if the required 3rd party library is not available.
     * @param <P> type of {@link AbstractFeatureProvider} object used for implementing the API.
     */
    public static abstract class AbstractFeatureImpl<P extends FeatureAPI> extends AbstractFeature {

        private P providerObject;

        /**
         * Create a new {@link AbstractFeatureImpl}.
         * @param featureName name of the feature.
         */
        protected AbstractFeatureImpl (String featureName) {
            super(featureName);
            init();
        }

        private void init () {
            super.supports = check();
            if (super.supports) {
                providerObject = createProvider();
            }
        }

        /**
         * Get the provider object used. Will be null if the feature failed to
         * initialise.
         * @return the provider created during startup.
         * @see #featureEnabled() 
         * @see AbstractFeatureProvider
         */
        public P getFeature () {
            return providerObject;
        }

        @SuppressWarnings("unchecked")
        private Class<P> castClass (Class<?> cls, String specified) {
            if (!AbstractFeatureProvider.class.isAssignableFrom(cls)) {
                throw new FeatureImplementationError(super.featureName,
                                                     specified,
                                                     AbstractFeatureProvider.class.
                        getCanonicalName());
            }
            return (Class<P>) cls;
        }

        private Constructor<P> constructor (Class<?> cls, String spec) throws
                Exception {
            return castClass(cls, spec).getConstructor(String.class);
        }

        private P constructProvider (Class<?> cls, String spec) throws Exception {
            return constructor(cls, spec).newInstance(super.featureName);
        }

        private P provider (String[] f, String n) throws Exception {
            try {
                Classloader l = Classloader.get();
                if (f != null) {
                    for (String s : f) {
                        if (s != null && !s.equals(Feature.default_types)) {
                            l.featureClass(s);
                        }
                    }
                }
                l.featureClass(n);
                Class<?> c = l.loadClass(n);
                return constructProvider(c, n);
            }
            catch (Throwable t) {
                throw new FeatureImplementationError(super.featureName, n, t);
            }
        }

        /**
         * Hook for subclasses to provide the right type of {@link AbstractFeatureProvider}.
         * @param featureName name of the feature.
         * @return a new {@link AbstractFeatureProvider} which can later be retrieved through
         * {@link #getFeature() }.
         */
        private P createProvider () {
            String f = feature().name();
            String[] cs = feature().types();
            try {
                return provider(cs, f);
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
                IOHelp.handleExceptions(AbstractFeatureImpl.class,
                                        "createProvider",
                                        e,
                                        Messages.FeatureProviderInitError,
                                        super.featureName,
                                        f,
                                        e.getLocalizedMessage());
                return null;
            }
        }

        /**
         * Check if this {@link AbstractFeatureImpl} should disable itself.
         * @return true if this features was disabled at compile time, false if not.
         */
        public abstract boolean disable ();

        private Feature feature () {
            Feature f = getClass().getAnnotation(Feature.class);
            if (f == null) {
                throw new FeatureImplementationError(super.featureName);
            }
            return f;
        }

        private boolean check () {
            boolean result = false;
            final String method = "check";
            String cname = null;
            try {
                if (disable()) {
                    throw new NotCompiledException(super.featureName);
                }
                String f = feature().name();
                if (f == null || f.equals("")) {
                    throw new FeatureImplementationError(
                            super.featureName,
                            f,
                            AbstractFeatureProvider.class.getCanonicalName());
                }
                for (String name : requiredClasses()) {
                    cname = name;
                    Class.forName(cname, false, Classloader.get());
                }
                result = true;
            }
            catch (Throwable t) {
                IOHelp.handleExceptions(AbstractFeatureImpl.class, method, t,
                                        Messages.FeatureInitError,
                                        super.featureName,
                                        cname,
                                        t.getLocalizedMessage());
            }

            return result;
        }

        /**
         * Get the classes which are to be tested for by this {@link AbstractFeatureImpl}.
         * @return a list of classes required to be available at run time for this feature to work.
         */
        public abstract String[] requiredClasses ();

        /**
         * Checks whether support for this feature has been properly installed.
         * @return true if both a working feature implementation is available
         * and the feature has been initialised successfully.
         */
        public boolean featureEnabled () {
            return super.supports && providerObject != null && providerObject.
                    supports();
        }
    }

    /**
     * A type of {@link UnsupportedOperationException} thrown to indicate a method has
     * been disabled at compile time.
     */
    public static class NotCompiledException extends UnsupportedOperationException {

        /**
         * Create a new {@link NotCompiledException}.
         * @param feature name of the feature.
         */
        private NotCompiledException (String feature) {
            super(Messages.FeatureNotCompiledError.format(feature));
        }
    }

    private static class FeatureImplementationError extends IllegalStateException {

        private FeatureImplementationError (String feature) {
            super(Messages.NoFeatureFound.format(feature));
        }

        private FeatureImplementationError (String feature, String classname,
                                            String basename) {
            super(Messages.FeatureProviderTypeError.format(feature,
                                                           classname,
                                                           basename));
        }

        private FeatureImplementationError (String feature, String classname,
                                            Throwable t) {
            super(Messages.FeatureProviderInvalid.format(feature,
                                                         classname,
                                                         t.getLocalizedMessage()),
                  t);
        }
    }
}
