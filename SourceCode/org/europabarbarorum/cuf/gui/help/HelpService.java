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
package org.europabarbarorum.cuf.gui.help;

import java.awt.Window;
import org.europabarbarorum.cuf.support.AbstractFeature.AbstractFeatureImpl;
import org.europabarbarorum.cuf.support.FeatureAPI.Feature;
import org.europabarbarorum.cuf.support.IOHelp;

/**
 * Class to provide support for {@code javax.help} functionality provided by the JavaHelp libraries.
 * @author Johan Ouwerkerk
 */
public enum HelpService {

    /**
     * The single JavaHelp feature instance.
     */
    JavaHelp;

    /**
     * Wrapped version of {@link AbstractFeatureImpl#featureEnabled() }.
     * @return true if this feature can be safely enabled (is available and appears to be functional),
     * false if not.
     */
    public boolean featureEnabled () {
        return support.featureEnabled();
    }

    /**
     * Wrapped version of {@link AbstractFeatureImpl#supports() }.
     * @return true if libraries for this feature are available. This does not necessarily
     * mean that the feature itself can be safely used.
     * @see #featureEnabled() 
     */
    public boolean featureSupported () {
        return support.supports();
    }

    /**
     * Maps a helpset name to the URL of a helpset file.
     * @param name name of the helpset. Use null for the default (program) helpset.
     * @return a relative URL that can be used to lookup a helpset file.
     */
    public static String getHelpSet (String name) {
        if (name == null) {
            return getHelpSet(appHelpsetDefault);
        }
        return (new StringBuilder()).append(helpsetDirectory).
                append(name).
                append(helpsetExtension).toString();
    }

    /**
     * Convert an URL relative to a helpset file to a proper relative URL that can be used for
     * navigating the help browser.
     * @param target the relative URL of a help page, or null to get the {@link #helpsetDirectory help root directory}.
     * @return the converted URL to the help page.
     */
    public static String getTarget (String target) {
        if (target == null) {
            return helpsetDirectory;
        }
        return helpsetDirectory + target;
    }

    /**
     * Navigates the help browser to the given help page.
     * @param item a well known {@link HelpItem}.
     * @param w the {@link Window} which is to act as the “parent” window of the help browser.
     * @return true if successful, false if some error occurred.
     */
    public boolean navigateTo (HelpItem item, Window w) {
        if (support.supports()) {
            try {
                return support.navigateTo(item, w);
            }
            catch (Exception e) {
                IOHelp.handleExceptions(HelpService.class,
                                        "navigateTo",
                                        e, e.getLocalizedMessage());
            }
        }
        else {
            IOHelp.warn(HelpService.class,
                        Messages.HelpServiceNotSupported,
                        item.target(),
                        item.helpset());
        }
        return false;
    }
    /**
     * File extension used by helpset files in the project.
     */
    public static final String helpsetExtension = ".help.xml";
    /**
     * Name of the default (program) helpset.
     */
    public static final String appHelpsetDefault = "cuf";
    /**
     * Name of the root directory in which all helpsets are to be installed.
     */
    public static final String helpsetDirectory = "help/";
    private final HelpFeatureImpl support = new HelpFeatureImpl(name());

    @Feature(name = "org.europabarbarorum.cuf.gui.help.HelpProvider",
    types = { "org.europabarbarorum.cuf.gui.help.HelpProvider$1" })
    private static class HelpFeatureImpl extends AbstractFeatureImpl<HelpFeature> {

        private HelpFeatureImpl (String name) {
            super(name);
        }

        /**
         * Get the list of classes to check during startup to see if JavaHelp is supported.
         * @return a list of {@code javax.help} classnames that are used by this program to
         * provide JavaHelp.
         */
        @Override
        public String[] requiredClasses () {
            return new String[] {
                        "javax.help.HelpSet",
                        "javax.help.HelpBroker"
                    };
        }

        @Override
        public boolean disable () {
            //ANT-DISABLE-JAVAHELP return true;
            //ANT-DISABLE-JAVAHELP /*
            return false;
            //ANT-DISABLE-JAVAHELP */
        }

        private boolean navigateTo (HelpItem item, Window w) throws Exception {
            //ANT-DISABLE-JAVAHELP disabled();
            //ANT-DISABLE-JAVAHELP return false;
            //ANT-DISABLE-JAVAHELP /*
            getFeature().navigateTo(item, w);
            return true;
            //ANT-DISABLE-JAVAHELP */
        }
    }

    /**
     * Description of a well-known help page.
     */
    public interface HelpItem {

        /**
         * Get the page to display.
         * @return URL of the page, relative to the helpset file.
         */
        String target ();

        /**
         * Get the helpset to use.
         * @return the name of the helpset (without {@link #helpsetExtension file extension},
         * relative to {@link #helpsetDirectory the help directory}.
         */
        String helpset ();

        /**
         * Get the ID of the help topic to display.
         * @return the ID of the help topic to display.
         */
        String id ();

        /**
         * Navigate the help browser to this help page.
         * @param parent the {@link Window} which is to be the “parent” window of the help browser.
         */
        void navigate (Window parent);
    }
}
