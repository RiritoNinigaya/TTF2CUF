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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadFactory;
import org.europabarbarorum.cuf.support.ResourceHelp.StringResource;

/**
 * A {@link ClassLoader} that is used to extend the program classpath at runtime and
 * link to additional libraries.
 * @author Johan Ouwerkerk
 */
public class Classloader extends URLClassLoader {

    /**
     * Startup logic to insert an instance of {@link Classloader} in the
     * {@link ClassLoader} chain.
     * @param next the point of re-entry into the main application logic.
     * This must be the cannonical name of a class which defines a {@code public static void main(String[] args)}
     * method.
     * @param args arguments to pass through to the re-entry point in the application logic.
     * @throws Exception if an error occurs.
     * In particular if this method has been called before.
     */
    public static void boot (String next, String[] args) throws
            Exception {
        if (loader != null || getContextLoader() instanceof Classloader) {
            throw new IllegalMethodCallException();
        }
        else {
            Classloader l = Classloader.get(getContextLoader());
            Thread.currentThread().setContextClassLoader(l);
            ResourceHelp.clear();
            Class<?> c = l.loadClass(next);
            Method m = c.getDeclaredMethod("main", args.getClass());
            m.invoke(m, (Object) args);
        }
    }
    /**
     * The configuration file read at program start for including additional classpath elements.
     * Each property of which the name ends with “classpath” is taken to refer to additional
     * classpath elements that must be included on the classpath.
     * This file should be located in the lib/ directory.
     */
    public static final Setting<String> ClassPathConfigFile = new Setting<String>(
            "cuf.classpath.conf",
            "nblibraries.properties",
            new StringResource(),
            Setting.Modifiable.Conf);
    /**
     * Root namespace of all classes in the CUF program.
     */
    public static final String rootNameSpace = "org.europabarbarorum.cuf";

    private Classloader (URL[] src, ClassLoader parent) {
        super(src, parent);
        this.configure();
    }

    /**
     * Cause the {@link Classloader} to configure itself with additional classpath elements.
     */
    protected final void configure () {
        urls = new ConcurrentSkipListSet<URL>(new Comparator<URL>() {

            @Override
            public int compare (URL o1, URL o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        urls.addAll(Arrays.asList(getURLs()));
        init();
    }

    /**
     * Loads a {@link Class}.
     * @param name the binary name of the class.
     * @param resolve whether or not the {@link Class} should be resolved (linked).
     * @return the {@link Class} corresponding to the give name.
     * @throws ClassNotFoundException if no such {@link Class} can be found.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected synchronized Class<?> loadClass (String name, boolean resolve) throws
            ClassNotFoundException {

        Class c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        URL element = subvert(name);
        if (element != null) {

            IOHelp.Log.Trace.log(Classloader.class,
                                 Messages.ClassLoaded,
                                 name,
                                 element);
            c = findClass(name);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }

        return super.loadClass(name, resolve);
    }

    private boolean compare (URL base, URL element) {
        return base.toString().contains(element.toString());
    }

    private URL subvert (String name) {
        if (!name.contains(rootNameSpace) || featureClasses.contains(name)) {
            return checkIfLocal(name);
        }
        else {
            return null;
        }
    }
    private Set<String> featureClasses = new HashSet<String>();

    /**
     * Mark a class as a “feature class” which means that if this
     * {@link Classloader} is asked to load the class it will not attempt to load this class itself
     * rather than deferring to its {@link #getParent() parent loader}.
     * @param name the canonical name of the class to mark.
     */
    public synchronized void featureClass (String name) {
        IOHelp.Log.Debug.log(Classloader.class,
                             Messages.FeatureRegistered,
                             name);
        this.featureClasses.add(name);
    }

    private URL normalize (URL u) {
        if (u == null) {
            return u;
        }
        try {
            URL k = u.toURI().normalize().toURL();
            return k;
        }
        catch (Exception e) {
            return u;
        }
    }

    private URL classURL (String name) {
        URL u = normalize(getResource(name.replace('.', '/') + ".class"));
        return u;
    }

    private URL checkIfLocal (String name) {
        URL u1 = classURL(name);

        if (u1 != null) {
            for (URL element : getURLs()) {
                element = normalize(element);
                if (compare(u1, element)) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Bootstrap method.
     * @param parent the {@link ClassLoader} which loaded the class that uses a
     * {@link Classloader}.
     * @return the {@link Classloader} used for loading the program.
     * Note that the {@link Classloader} needs to be {@link #configure() configured}.
     */
    protected static Classloader get (ClassLoader parent) {
        if (loader == null) {
            URL[] src = new URL[] {};
            if (parent instanceof URLClassLoader) {
                src = ((URLClassLoader) parent).getURLs();
            }
            loader = new Classloader(src, parent);
        }
        return loader;
    }

    /**
     * Get the program {@link Classloader}.
     * @return the {@link Classloader} used by the program.
     */
    public static Classloader get () {
        if (loader == null) {
            throw new IllegalStateException();
        }
        return loader;
    }

    private void initImpl (String cpath, String base, String propKey) throws
            Exception {
        if (cpath != null && cpath.length() > 0) {
            String[] paths = cpath.split("\\Q:\\E");
            File expanded;
            URL url;
            for (String p : paths) {
                expanded = new File(p.replace("${base}", base));
                if (expanded.exists()) {
                    url = expanded.toURI().toURL();
                    this.extendCpath(url);
                }
                else {
                    IOHelp.warn(Classloader.class,
                                Messages.ExpansionPathInvalid,
                                p,
                                expanded,
                                base,
                                propKey);
                }
            }
        }
    }

    private void init () {
        try {
            initImpl();
        }
        catch (Exception e) {
            IOHelp.warn(Classloader.class,
                        Messages.NoExpansionPath,
                        ClassPathConfigFile.get(),
                        e.getLocalizedMessage());
        }
    }

    private void initImpl () throws Exception {
        InputStream confData = null;
        try {
            String conf = ClassPathConfigFile.get();
            URL u = getResource(conf);
            if (u == null) {
                throw new Exception(Messages.ClasspathConfNotFound.format(conf));
            }
            String base = new File(u.getPath()).getParentFile().getPath();
            confData = getResourceAsStream(conf);
            Properties ps = new Properties();
            ps.load(confData);

            for (String key : ps.stringPropertyNames()) {
                if (key.endsWith("classpath")) {
                    initImpl(ps.getProperty(key), base, key);
                }
            }
        }
        finally {
            if (confData != null) {
                confData.close();
            }
        }
    }
    private ConcurrentSkipListSet<URL> urls;

    /**
     * Add another classpath element to the runtime classpath of this {@link Classloader}.
     * @param u the {@link URL} of a library or resource to link to.
     * @return true if the given {@link URL} was a new classpath element or false if not.
     */
    public synchronized boolean extendCpath (URL u) {
        if (!urls.contains(u)) {
            super.addURL(u);
            urls.add(u);
            IOHelp.Log.Config.log(Classloader.class, Messages.ExpansionAdded, u);
            return true;
        }
        return false;
    }
    /**
     * Check if the given {@link URL} is on the classpath of this {@link Classloader}.
     * @param u the {@link URL} to check.
     * @return true if this {@link URL} is on the classpath of this {@link Classloader}.
     */
    public synchronized boolean isLinked(URL u) {
        return urls.contains(u);
    }
    private static Classloader loader;

    /**
     * Convenience method to get the {@link ClassLoader} of the current thread.
     * @return the {@link ClassLoader} which is the context loader for
     * the {@link Thread#currentThread() current thread}.
     */
    public static ClassLoader getContextLoader () {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Convenience method to set the program {@link Classloader} as context loader for
     * the {@link Thread#currentThread() current thread}.
     */
    public static void setAsContextLoader () {
        Thread.currentThread().setContextClassLoader(get());
    }

    /**
     * Create a {@link ThreadFactory} which returns threads set to use
     * {@link Classloader#get() the shared instance loader} as context
     * {@link ClassLoader}.
     * @param format a format string to generate {@link Thread} names from.
     * @return a new {@link ThreadFactory}.
     */
    public static ThreadFactory threadFactory (final String format) {
        return new ThreadFactory() {

            private int i = 0;

            @Override
            public Thread newThread (Runnable r) {
                ++i;
                Thread t = new Thread(r, String.format(format, i));
                t.setContextClassLoader(get());
                return t;
            }
        };
    }
}
