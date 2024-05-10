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
//ANT-DISABLE-JAVAHELP /*
import java.awt.event.WindowEvent;
import javax.help.DefaultHelpBroker;
import javax.help.HelpSet;
//ANT-DISABLE-JAVAHELP */
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.net.URL;
import javax.swing.JScrollPane;
import org.europabarbarorum.cuf.support.Classloader;
import org.europabarbarorum.cuf.gui.help.HelpService.HelpItem;
import org.europabarbarorum.cuf.gui.support.ScrollingSupport;
import org.europabarbarorum.cuf.support.AbstractFeature.AbstractFeatureProvider;

/**
 * This class encapsulates the {@code javax.help} specific code and insulates it from the
 * rest of the program. If this class is never loaded the program can continue to run
 * without any requirement for a working {@code javax.help} implementation or any helpset.
 * @author Johan Ouwerkerk
 */
public class HelpProvider extends AbstractFeatureProvider implements HelpFeature {

    /**
     * Creates a new {@link HelpProvider}.
     * @param featureName name of the feature.
     */
    public HelpProvider (String featureName) {
        super(featureName);
    }

    @Override
    protected Object test () throws Exception {
        //ANT-DISABLE-JAVAHELP disabled();
        //ANT-DISABLE-JAVAHELP return null;
        //ANT-DISABLE-JAVAHELP /*
        return getHelpSet(HelpService.appHelpsetDefault);
        //ANT-DISABLE-JAVAHELP */
    }
    //ANT-DISABLE-JAVAHELP /*
    private DefaultHelpBroker broker;

    private HelpSet getHelpSet (String name) throws Exception {
        return new HelpSet(getLoader(), getURL(HelpService.getHelpSet(name)));
    }

    private DefaultHelpBroker getBroker (String name) throws Exception {
        HelpSet set = getHelpSet(name);
        if (broker == null) {
            broker = new DefaultHelpBroker(set);
        }
        return broker;
    }
    private Window help_window = null;
    //ANT-DISABLE-JAVAHELP */

    private ClassLoader getLoader () {
        return Classloader.get();
    }

    private URL getURL (String path) throws Exception {
        URL u = getLoader().getResource(path);
        if (u == null) {
            throw new IllegalArgumentException(
                    Messages.HelpURLNotFound.format(path));
        }
        return u;
    }

    private void injectScrollSupport (Container k) {
        Component[] ks = k.getComponents();
        Component c;
        for (int i = 0; i < ks.length; ++i) {
            c = ks[i];
            if (c instanceof JScrollPane) {
                ScrollingSupport.fix((JScrollPane) c);
                continue;
            }
            if (c instanceof Container) {
                injectScrollSupport((Container) c);
                continue;
            }
        }
    }

    @Override
    public void navigateTo (HelpItem item, Window w) throws Exception {
        //ANT-DISABLE-JAVAHELP disabled();
        //ANT-DISABLE-JAVAHELP /*

        DefaultHelpBroker d = getBroker(item.helpset());
        d.setActivationWindow(w);

        d.setCurrentURL(getURL(item));
        if (!d.isDisplayed()) {
            d.initPresentation();
            if (help_window == null) {
                help_window = d.getWindowPresentation().getHelpWindow();
                help_window.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing (WindowEvent e) {
                        help_window.removeWindowListener(this);
                        help_window = null;
                    }
                });
                injectScrollSupport(help_window);
            }
            help_window.setLocationRelativeTo(w);
            d.setDisplayed(true);
        }

        //ANT-DISABLE-JAVAHELP */
    }

    private URL getURL (HelpItem item) throws Exception {
        return getURL(HelpService.getTarget(item.target()));
    }
}
