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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import org.europabarbarorum.cuf.support.Setting.Modifiable;
import org.europabarbarorum.cuf.shell.Shell.Makeup;
import org.europabarbarorum.cuf.support.ResourceHelp.FileResource;
import org.europabarbarorum.cuf.support.Setting;

/**
 * A class to represent the logic of preload scripts and initialisation logic of a
 * language environment in a {@link Shell}
 *
 * <p>
 * The preload script is responsible for setting system properties and linking additional libraries so
 * that the language environment will work right. This allows the {@link Shell} to be expanded with
 * other languge enviroments than the default JVM supplied JavaScript in a straightforward manner.</p>
 *
 * Initialisation logic works as follows:
 * <ol>
 * <li>Map any aliases given using “Preload.alias” directives, so that aliases resolves to the
 * same name.
 * </li>
 * <li>Find and execute if available the associated preload script. Then, set up the
 * new language environment. The
 * {@link #run(org.europabarbarorum.cuf.shell.Shell) } method takes care of that.
 * </li>
 * </ol>
 * @author Johan Ouwerkerk
 */
public class PreloadScript {

    private final String loadedQuery;
    private final String requestQuery;

    /**
     * Create a {@link PreloadScript} instance for querying the preload settings and
     * running initialisation logic on {@link Shell} instances.
     * @param loadedQuery a string describing the loaded language environment, possibly null.
     * @param requestQuery a string describing the requested language environment.
     */
    public PreloadScript (String loadedQuery, String requestQuery) {
        this.loadedQuery = mapQuery(loadedQuery);
        this.requestQuery = mapQuery(requestQuery);
    }

    /**
     * Map a language query to its resolved form, according to Preload configuration directives.
     * Multiple queries might map to the same resolved name so that multiple queries that would yield the
     * same language interpreter in {@link Shell#init(java.lang.String) } can share the same preload script.
     * @param query the “query” to map. This is a string containing for example a mime type, language name,
     * or interpreter name. This value may be null.
     * @return the alias/name which the preload configuration assigns to the query.
     * This value is null if the query itself is null.
     */
    public static String mapQuery (String query) {
        if (query == null) {
            return null;
        }
        query = query.toLowerCase();
        return queryMapping.getProperty("Preload.alias." + query, query);
    }
    /**
     * The “home” directory in which preload configuration and similar live.
     */
    public static final File cufHome;
    /**
     * The base directory in which all preload scripts are located.
     * This directory is the root of the preload script file hierarchy.
     */
    public static final File preloadDir;
    /**
     * The absolute path of the file containing all preload configuration directives.
     */
    public static final File preloadConfFile;
    /**
     * A series of directives that configure how a language environment is (pre)loaded.
     */
    public static final Properties queryMapping;

    static {
        Setting<File> cfgDir = new Setting<File>("cuf.shell.confdir",
                                                 System.getProperty("user.home") + File.separator + ".cuf",
                                                 new FileResource(),
                                                 Modifiable.Cli);
        cufHome = cfgDir.get();
        ConfigDirectory = cfgDir;
        preloadDir = new File(cufHome, "preload");
        preloadConfFile = new File(cufHome, "preload.conf");
        queryMapping = readConfig(preloadConfFile);
    }
    /**
     * {@link File} preference corresponding to the directory to use for preload scripts, and preload configuration.
     */
    public static final Setting<File> ConfigDirectory;

    private static Properties readConfig (File conf) {
        if (conf.exists()) {
            try {
                readConfig(new Properties());
            }
            catch (Exception ignored) {
                // return empty properties
            }
        }
        return new Properties();
    }

    private static void readConfig (Properties ps) throws Exception {
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new FileReader(preloadConfFile));
            ps.load(br);
        }
        finally {
            if (br != null) {
                br.close();
            }
        }
    }

    /**
     * Finds the preload script associated with the given language that is to be loaded next.
     * This method returns the relative path of the script to associate with this {@link PreloadScript}.
     * The path is relative to the base directory in which all preload scripts are located.
     * This method is mainly suitable for querying the effect of preload script settings.
     * @return the relative path of the preload script to run.
     * @see #script() 
     */
    public File preloadScript () {
        String loaded = loadedQuery == null
                ? mapQuery(Shell.SystemLanguage.get())
                : loadedQuery;
        loaded = queryMapping.getProperty("Preload.alias." + loaded, loaded);

        String language = queryMapping.getProperty(
                "Preload.alias." + requestQuery,
                requestQuery);

        return new File(queryMapping.getProperty("Preload.directory." + language,
                                                 language),
                        queryMapping.getProperty("Preload.script." + loaded,
                                                 loaded));
    }

    /**
     * Return the full path name of the script corresponding to this {@link PreloadScript} query.
     * This method is useful for locating a preload script to run.
     * @return an absolute path of the preload script to run.
     */
    public File script () {
        return new File(preloadDir, preloadScript().toString());
    }

    /**
     * Runs the initialisation logic on a shell. This method executes a preload script and runs the
     * {@link Shell#init(java.lang.String) } method.
     * @param shell the {@link Shell} to initialise.
     */
    protected void run (Shell shell) {
        if (shell.getEngine() != null) {
            File path = script();
            if (path.exists()) {
                shell.interpret(path.toString());
            }
            else {
                shell.error(Makeup.Warning.makeup(shell,
                                                  Messages.NoSuchPreloadScript,
                                                  requestQuery,
                                                  loadedQuery,
                                                  path));
            }
        }
        else {
            shell.error(Makeup.Notification.makeup(
                    shell,
                    Messages.StartupPreloadOmitted,
                    requestQuery));
        }
        shell.init(requestQuery);
    }
}
